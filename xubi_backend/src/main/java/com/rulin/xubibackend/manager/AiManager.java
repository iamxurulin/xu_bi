package com.rulin.xubibackend.manager;

import com.rulin.xubibackend.common.ErrorCode;
import com.rulin.xubibackend.exception.BusinessException;
import io.github.briqt.spark4j.SparkClient;
import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.request.SparkRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class AiManager {
    @Resource
    private SparkClient sparkClient;

    public String sendMsgToXunFeiSpark(boolean isNeedTemplate,String content){
        List<SparkMessage> messages = new ArrayList<>();
        if(isNeedTemplate){
            //AI 生成问题的预设条件
            String predefinedInformation = "请严格按照下面的输出格式生成结果，且不得添加任何多余内容（例如无关文字、注释、代码块标记或反引号）：\n" +
                    "\n" +
                    "'【【【【'" +
                    "{ 生成 Echarts V5 的 option 配置对象 JSON 代码，要求为合法 JSON 格式且不含任何额外内容（如注释或多余字符） } '【【【【' 结论： {\n" +
                    "提供对数据的详细分析结论，内容应尽可能准确、详细，不允许添加其他无关文字或注释 }\n" +
                    "\n" +
                    "示例： 输入： 分析需求： 分析网站用户增长情况，请使用柱状图展示 原始数据： 日期,用户数 1号,10 2号,20 3号,30\n" +
                    "\n" +
                    "期望输出： '【【【【' { \"title\": { \"text\": \"分析网站用户增长情况\" }, \"xAxis\": { \"type\": \"category\", \"data\": [\"1号\", \"2号\", \"3号\"] }, \"yAxis\": { \"type\": \"value\" }, \"series\": [ { \"name\": \"用户数\", \"type\": \"bar\", \"data\": [10, 20, 30] } ] } '【【【【' 结论： 从数据看，网站用户数由1号的10人增长到2号的20人，再到3号的30人，呈现出明显的上升趋势。这表明在这段时间内网站用户吸引力增强，可能与推广活动、内容更新或其他外部因素有关。";
            messages.add(SparkMessage.systemContent(predefinedInformation+"\n"+"----------------------------------"));
        }

        messages.add(SparkMessage.userContent(content));

        //构造请求
        SparkRequest sparkRequest = SparkRequest.builder()
                .messages(messages)//消息列表
                .maxTokens(2048)
                .temperature(0.6)
                .apiVersion(SparkApiVersion.V4_0)
                .build();
        //同步调用
        String responseContent = sparkClient.chatSync(sparkRequest).getContent().trim();
        if(!isNeedTemplate){
            return responseContent;
        }

        log.info("星火AI返回的结果{}",responseContent);
        AtomicInteger atomicInteger = new AtomicInteger(1);

        while (responseContent.split("'【【【【'").length<3){
            responseContent = sparkClient.chatSync(sparkRequest).getContent().trim();
            if(atomicInteger.incrementAndGet()>=4){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"星火AI生成失败");
            }
        }
        return responseContent;
    }

}
