package com.rulin.xubibackend.model.dto.chart;

import com.rulin.xubibackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 图表查询请求类，继承自分页请求类并实现序列化接口
 * 使用了Lombok的@EqualsAndHashCode和@Data注解，自动生成equals、hashCode和getter/setter方法
 */
@EqualsAndHashCode(callSuper = true) // 确保生成的equals和hashCode方法包含父类的字段
@Data // 自动生成getter、setter、toString等方法
public class ChartQueryRequest extends PageRequest implements Serializable {

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
     * 图表类型
     */
    private String chartType;

    /**
     * 用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}