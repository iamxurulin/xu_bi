package com.rulin.xubibackend.validator;

/**
 * 图表生成结果校验接口
 * 用于校验 AI 生成的图表数据和结论是否合法
 */
public interface ChartValidator {

    /**
     * 校验 AI 生成的图表结果
     *
     * @param genChart   AI 生成的 ECharts option JSON 字符串
     * @param genResult  AI 生成的分析结论文本
     * @return 校验结果，valid=true 表示通过，valid=false 表示失败且 message 说明原因
     */
    ValidationResult validate(String genChart, String genResult);
}
