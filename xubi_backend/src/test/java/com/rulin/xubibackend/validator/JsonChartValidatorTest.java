package com.rulin.xubibackend.validator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonChartValidator 单元测试
 * 覆盖：合法输入、非法 JSON、缺少 series、空数据、空 genResult 等场景
 */
@SpringBootTest
class JsonChartValidatorTest {

    @Autowired
    private JsonChartValidator validator;

    @Test
    void testValidChart() {
        String genChart = "{\"title\": {\"text\": \"Test\"}, \"series\": [{\"name\": \"Sales\", \"data\": [10, 20, 30]}]}";
        ValidationResult result = validator.validate(genChart, "分析结果 OK");
        assertTrue(result.isValid(), "应该通过: " + result.getMessage());
    }

    @Test
    void testMissingSeries() {
        String genChart = "{\"title\": {\"text\": \"Test\"}, \"xAxis\": {\"type\": \"category\"}}";
        ValidationResult result = validator.validate(genChart, "分析结果");
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("series"));
    }

    @Test
    void testEmptySeries() {
        String genChart = "{\"series\": []}";
        ValidationResult result = validator.validate(genChart, "分析结果");
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("非空数组"));
    }

    @Test
    void testSeriesMissingData() {
        String genChart = "{\"series\": [{\"name\": \"Sales\"}]}";
        ValidationResult result = validator.validate(genChart, "分析结果");
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("data"));
    }

    @Test
    void testBlankGenChart() {
        ValidationResult result = validator.validate("", "分析结果");
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("genChart"));
    }

    @Test
    void testBlankGenResult() {
        String genChart = "{\"series\": [{\"name\": \"Sales\", \"data\": [1]}]}";
        ValidationResult result = validator.validate(genChart, "");
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("genResult"));
    }

    @Test
    void testInvalidJson() {
        ValidationResult result = validator.validate("not json at all", "分析结果");
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("JSON 解析失败"));
    }
}
