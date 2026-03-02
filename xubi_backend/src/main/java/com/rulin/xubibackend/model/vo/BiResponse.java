package com.rulin.xubibackend.model.vo;

import lombok.Data;

/**
 * BI 的返回结果
 * 该类用于封装BI服务返回的数据，包含生成图表的结果和相关信息
 */
@Data  // 使用Lombok的@Data注解，自动生成getter、setter等方法
public class BiResponse {
    // 生成图表的URL或路径
    private String genChart;
    // 生成结果的描述信息
    private String genResult;
    //新生成的图标ID
    private Long chartId;
}
