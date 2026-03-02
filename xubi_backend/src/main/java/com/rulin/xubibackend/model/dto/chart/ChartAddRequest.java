package com.rulin.xubibackend.model.dto.chart;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

// 使用@Data注解，这是Lombok提供的注解，会自动为类生成getter、setter、equals、hashCode和toString方法
@Data
public class ChartAddRequest implements Serializable {

    /**
     * 图表名称
     * 用于存储图表的名称信息
     */
    private String name;

    /**
     * 分析目标
     * 用于描述图表的分析目标和用途
     */
    private String goal;

    /**
     * 图表数据
     * 存储用于生成图表的原始数据，通常是JSON格式的字符串
     */
    private String chartData;

    /**
     * 图表类型
     * 指定图表的类型，如折线图、柱状图、饼图等
     */
    private String chartType;


    // 序列化版本UID，用于实现Serializable接口时标识类的版本
    private static final long serialVersionUID = 1L;
}