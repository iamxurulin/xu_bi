package com.rulin.xubibackend.manager;

import com.rulin.xubibackend.common.ErrorCode;
import com.rulin.xubibackend.exception.BusinessException;
import com.rulin.xubibackend.wxmp.AgnesAiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI服务管理类，负责与 Agnes AI 进行交互
 */
@Service
@Slf4j
public class AiManager {

    @Resource
    private AgnesAiClient agnesAiClient;

    /**
     * 发送消息到AI
     * @param isNeedTemplate 是否需要AI按照特定模板生成结果
     * @param content 用户发送的消息内容
     * @return AI的响应内容
     */
    public String sendMsgToXunFeiSpark(boolean isNeedTemplate, String content) {
        String systemPrompt = null;
        if (isNeedTemplate) {
            systemPrompt = "请严格按照下面的输出格式生成结果，且不得添加任何多余内容（例如无关文字、注释、代码块标记或反引号）：" +
                    "\n" +
                    "'【【【【'" +
                    "{ 生成 Echarts V5 的 option 配置对象 JSON 代码，要求为合法 JSON 格式且不含任何额外内容（如注释或多余字符） } '【【【【' 结论： {\n" +
                    "提供对数据的详细分析结论，内容应尽可能准确、详细，不允许添加其他无关文字或注释 }\n" +
                    "\n" +
                    "=== ECharts 视觉与布局规范（必须严格遵守） ===" +
                    "\n" +
                    "【最重要】输出必须是纯 JSON，绝不允许出现 JavaScript 表达式！禁用 new/function/Math 等 JS 关键字，必须能被 JSON.parse() 直接解析。" +
                    "\n" +
                    "渐变色 JSON 合法写法：{\"color\":{\"type\":\"linear\",\"x\":0,\"y\":0,\"x2\":0,\"y2\":1,\"colorStops\":[{\"offset\":0,\"color\":\"#636efa\"},{\"offset\":1,\"color\":\"#a855f7\"}]}}" +
                    "\n" +
                    "布局关键配置（必须全部设置，否则图例会和坐标轴重叠）：" +
                    "\n" +
                    "1. grid: {left:'8%',right:'10%',top:'20%',bottom:'12%',containLabel:true} | " +
                    "2. title: {text:'',show:false}（前端会覆盖title，所以设为空）" +
                    "\n" +
                    "3. legend: {show:true,orient:'horizontal',top:'2%',left:'center',padding:[5,0,10,0],textStyle:{fontSize:12,color:'#6b7280'},icon:'roundRect',itemWidth:15,itemHeight:8,itemGap:20}" +
                    "\n" +
                    "4. tooltip: {trigger:'axis',backgroundColor:'rgba(255,255,255,0.98)',borderColor:'#e5e7eb',textStyle:{fontSize:12,padding:[8,12]}};" +
                    "5. xAxis: {axisLabel:{fontSize:12,color:'#6b7280',rotate:0},axisLine:{lineStyle:{color:'#e5e7eb'}}}" +
                    "\n" +
                    "6. yAxis: {splitLine:{lineStyle:{color:'#f3f4f6',type:'dashed'}},axisLabel:{fontSize:12,color:'#6b7280'}}" +
                    "\n" +
                    "【yAxis name 规范（必须遵守）】yAxis.name 只能使用一个单位，不得混合多种单位（如不能同时写'万元/亿元'）。如果数据原始单位是万元，name 统一设为'万元'；如果是元，设为'元'。百分比系列放在右侧 Y 轴时，name 设为'%'。" +
                    "\n" +
                    "【字段选择规则（必须遵守）】如果数据包含多个数值字段（如营收、利润、成本等），柱状图必须将所有数值字段作为独立 series 展示在同一图表中，不得使用单系列。折线图同理。" +
                    "\n" +
                    "【时间轴排序规则（必须遵守）】如果 xAxis 是年份或时间（如 2021、2022、2023、2024），必须按升序排列（从小到大，从左到右）。" +
                    "\n" +
                    "【双Y轴/多量级规则】如果数据包含不同量级（如金额和百分比），必须使用双 Y 轴：yAxis 设为数组，第一个 type:'value'（左侧），第二个 type:'value' position:'right'（右侧），右侧 axisLabel.formatter:'{value}%'。series 中百分比系列设 yAxisIndex:1。" +
                    "\n" +
                    "配色：折线 ['#636efa','#a855f7','#ec4899']，饼图 ['#f59e42','#f43f5e','#8b5cf6','#3b82f6','#10b981']。" +
                    "\n" +
                    "折线图: smooth true, symbol circle, symbolSize 6, areaStyle opacity 0.1 | " +
                    "柱状图: barMaxWidth 40, borderRadius [4,4,0,0] | " +
                    "饼图: radius ['40%','65%']";
        }

        String responseContent;
        if (systemPrompt != null) {
            responseContent = agnesAiClient.chat(systemPrompt, content);
        } else {
            responseContent = agnesAiClient.chat(null, content);
        }

        log.info("Agnes AI返回的结果: {}", responseContent);

        // 检查分隔符是否完整
        AtomicInteger atomicInteger = new AtomicInteger(1);
        while (responseContent.split("【【【【").length < 3) {
            log.warn("AI 返回分隔符数量不足，重试第 {} 次", atomicInteger.get());
            if (systemPrompt != null) {
                responseContent = agnesAiClient.chat(systemPrompt, content);
            } else {
                responseContent = agnesAiClient.chat(null, content);
            }
            if (atomicInteger.incrementAndGet() >= 4) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成失败");
            }
        }
        return responseContent;
    }

    /**
     * 二次 AI 重试（LLM-as-Judge）
     */
    public String retryChartGeneration(String originalChart, String validationMsg, String userQuestion) {
        String prompt = "之前生成的 ECharts option JSON 有误，请修正后重新生成。\n"
                + "错误原因：" + validationMsg + "\n"
                + "错误内容：" + originalChart + "\n"
                + "用户原始需求：\n" + userQuestion + "\n\n"
                + "请严格按照以下格式输出，且不得添加任何额外内容：\n"
                + "【【【【\n"
                + "{ 合法的 ECharts V5 option JSON，必须包含 title 和 series（数组格式） }\n"
                + "【【【【\n"
                + "结论：{简要分析结论}";

        String responseContent = agnesAiClient.chat(null, prompt);
        log.info("LLM-as-Judge 修正后的结果: {}", responseContent);
        return responseContent;
    }
}
