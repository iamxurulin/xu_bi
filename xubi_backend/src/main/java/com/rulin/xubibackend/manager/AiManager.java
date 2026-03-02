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

/**
 * AI服务管理类，负责与讯飞星火AI进行交互
 * 提供发送消息到AI并获取响应的功能
 */
@Service
@Slf4j
public class AiManager {
    // 注入SparkClient，用于与讯飞星火AI进行通信
    @Resource
    private SparkClient sparkClient;

    /**
     * 发送消息到讯飞星火AI
     * @param isNeedTemplate 是否需要AI按照特定模板生成结果
     * @param content 用户发送的消息内容
     * @return AI的响应内容
     */
    public String sendMsgToXunFeiSpark(boolean isNeedTemplate,String content){
        // 创建消息列表，用于存储与AI的对话历史
        List<SparkMessage> messages = new ArrayList<>();
        // 如果需要模板，添加预设条件
        if(isNeedTemplate){
            //AI 生成问题的预设条件，规定了输出格式和要求
            String predefinedInformation = "请严格按照下面的输出格式生成结果，且不得添加任何多余内容（例如无关文字、注释、代码块标记或反引号）：\n" +
                    "\n" +
                    "'【【【【'" +
                    "{ 生成 Echarts V5 的 option 配置对象 JSON 代码，要求为合法 JSON 格式且不含任何额外内容（如注释或多余字符） } '【【【【' 结论： {\n" +
                    "提供对数据的详细分析结论，内容应尽可能准确、详细，不允许添加其他无关文字或注释 }\n" +
                    "\n" +
                    "示例： 输入： 分析需求： 分析网站用户增长情况，请使用柱状图展示 原始数据： 日期,用户数 1号,10 2号,20 3号,30\n" +
                    "\n" +
                    "期望输出： '【【【【' { \"title\": { \"text\": \"分析网站用户增长情况\" }, \"xAxis\": { \"type\": \"category\", \"data\": [\"1号\", \"2号\", \"3号\"] }, \"yAxis\": { \"type\": \"value\" }, \"series\": [ { \"name\": \"用户数\", \"type\": \"bar\", \"data\": [10, 20, 30] } ] } '【【【【' 结论： 从数据看，网站用户数由1号的10人增长到2号的20人，再到3号的30人，呈现出明显的上升趋势。这表明在这段时间内网站用户吸引力增强，可能与推广活动、内容更新或其他外部因素有关。";
            // 添加预设信息到消息列表
            messages.add(SparkMessage.systemContent(predefinedInformation+"\n"+"----------------------------------"));
        }

        // 添加用户消息到消息列表
        messages.add(SparkMessage.userContent(content));

        //构造请求对象
        SparkRequest sparkRequest = SparkRequest.builder()
                .messages(messages)//消息列表
                .maxTokens(2048) // 最大令牌数
                .temperature(0.6) // 温度参数，控制随机性
                .apiVersion(SparkApiVersion.V4_0) // API版本
                .build();
        //同步调用AI服务获取响应
        String responseContent = sparkClient.chatSync(sparkRequest).getContent().trim();
        // 如果不需要模板，直接返回响应内容
        if(!isNeedTemplate){
            return responseContent;
        }

        // 记录AI返回的结果
        log.info("星火AI返回的结果{}",responseContent);
        // 使用AtomicInteger实现重试计数器
        AtomicInteger atomicInteger = new AtomicInteger(1);

        // 使用while循环检查responseContent中是否包含至少3个"'【【【【'"分隔符
        // 这是为了确保获取到完整的AI响应内容
        while (responseContent.split("'【【【【'").length<3){
            // 如果分隔符数量不足3个，则重新调用星火AI接口获取响应内容
            responseContent = sparkClient.chatSync(sparkRequest).getContent().trim();
            // 使用原子计数器atomicInteger来记录重试次数
            // 如果重试次数达到4次，仍然没有获取到完整响应，则抛出业务异常
            if(atomicInteger.incrementAndGet()>=4){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"星火AI生成失败");
            }
        }
        // 返回获取到的完整响应内容
        return responseContent;
    }

}
