package com.rulin.xubibackend.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * GenChartByAiRequest类是一个用于生成图表的请求类，实现了Serializable接口以支持序列化
 * 该类包含了生成图表所需的基本信息，如图表名称、分析目标和图表类型
 * 使用@Data注解自动生成getter、setter、toString等方法
 */
@Data
public class GenChartByAiRequest implements Serializable {

    /**
     * 图表名称
     * 用于标识图表的名称
     */
    private String name;

    /**
     * 分析目标
     * 描述图表需要分析的目标或内容
     */
    private String goal;

    /**
     * 图表类型
     * 指定生成图表的类型，如折线图、柱状图等
     */
    private String chartType;

    /**
     * 序列化版本UID
     * 用于序列化和反序列化过程中的版本控制
     */
    private static final long serialVersionUID = 1L;
}