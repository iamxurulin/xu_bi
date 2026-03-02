package com.rulin.xubibackend.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 图表编辑请求类
 * 用于封装图表编辑相关的请求参数
 * 实现了Serializable接口，支持序列化操作
 */
@Data  // 使用Lombok的@Data注解，自动生成getter、setter、toString等方法
public class ChartEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 图表名称
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表数据
     */
    private String chartData;

    /**
     * 图表类型
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}