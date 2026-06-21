package com.rulin.xubibackend.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 基于 Jackson 的 ECharts JSON 校验器
 * 校验 AI 生成的 genChart 字符串是否为合法的 ECharts option JSON
 */
@Component
public class JsonChartValidator implements ChartValidator {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public ValidationResult validate(String genChart, String genResult) {
        if (StringUtils.isBlank(genChart)) {
            return ValidationResult.fail("genChart 为空");
        }
        if (StringUtils.isBlank(genResult)) {
            return ValidationResult.fail("genResult 为空");
        }

        try {
            JsonNode root = objectMapper.readTree(genChart);

            // 必须是对象类型
            if (!root.isObject()) {
                return ValidationResult.fail("genChart 不是有效的 JSON 对象");
            }

            ObjectNode json = (ObjectNode) root;

            // series 是 ECharts 核心字段，必须有且为数组
            if (!json.has("series")) {
                return ValidationResult.fail("缺少必要 ECharts 字段: series");
            }
            JsonNode seriesNode = json.get("series");
            if (!seriesNode.isArray() || seriesNode.isEmpty()) {
                return ValidationResult.fail("ECharts series 必须为非空数组");
            }

            // 检查 series 中每个元素至少有 name + data
            for (JsonNode item : seriesNode) {
                if (!item.isObject()) {
                    return ValidationResult.fail("series 元素必须是对象");
                }
                if (!item.has("name") || !item.has("data")) {
                    return ValidationResult.fail("series 元素必须包含 name 和 data 字段");
                }
            }

            // 检查 data 是数组
            JsonNode firstData = json.get("series").get(0).get("data");
            if (!firstData.isArray()) {
                return ValidationResult.fail("series[0].data 必须为数组");
            }

            return ValidationResult.success();

        } catch (Exception e) {
            return ValidationResult.fail("JSON 解析失败: " + e.getMessage());
        }
    }
}
