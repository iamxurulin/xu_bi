package com.rulin.xubibackend.bizmq;

import com.rabbitmq.client.Channel;
import com.rulin.xubibackend.common.ErrorCode;
import com.rulin.xubibackend.exception.BusinessException;
import com.rulin.xubibackend.manager.AiManager;
import com.rulin.xubibackend.model.entity.Chart;
import com.rulin.xubibackend.service.ChartService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class BiMessageConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;

    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME},ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){

        log.info("receieveMessage message = {}",message);
        if(StringUtils.isBlank(message)){
            //如果更新失败，拒绝当前消息，让消息重新进入队列
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息为空");
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);

        if(chart == null){
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"图表为空");
        }

        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        //把任务状态改为执行中
        updateChart.setStatus("running");
        boolean b = chartService.updateById(updateChart);

        if(!b){
            channel.basicNack(deliveryTag,false,false);
            handleChartUpdateError(chart.getId(),"更新图表执行中状态失败");
            return;
        }

        //调用 AI
        String result = aiManager.sendMsgToXunFeiSpark(true, buildUserInput(chart));

        //对返回结果做拆分，按照5个中括号进行拆分
        String[] splits = result.split("【【【【");
        //拆分之后还要进行校验
        if (splits.length < 3) {
            channel.basicNack(deliveryTag,false,false);
            handleChartUpdateError(chart.getId(), "AI 生成错误");
            return;
        }

        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setGenChart(genChart);
        updateChartResult.setGenResult(genResult);
        updateChartResult.setStatus("succeed");

        boolean updateResult = chartService.updateById(updateChartResult);
        if(!updateResult){
            channel.basicNack(deliveryTag,false,false);
            handleChartUpdateError(chart.getId(),"更新图表成功状态失败");
        }

        channel.basicAck(deliveryTag,false);
    }

    private String buildUserInput(Chart chart){
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csvData = chart.getChartData();

        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        //拼接分析目标
        String userGoal = goal;

        //如果图表类型不为空
        if (StringUtils.isNotBlank(chartType)) {
            //就将分析目标拼接上“请使用”+图表类型
            userGoal += ".请使用" + chartType;
        }

        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        userInput.append(csvData).append("\n");

        return userInput.toString();
    }

    private void handleChartUpdateError(long chartId,String execMessage){
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("failed");
        updateChartResult.setExecMessage(execMessage);
        boolean updateResult = chartService.updateById(updateChartResult);
        if(!updateResult){
            log.error("更新图表失败状态失败"+chartId+","+execMessage);
        }
    }
}
