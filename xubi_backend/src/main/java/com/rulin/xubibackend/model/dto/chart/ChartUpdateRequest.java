package com.rulin.xubibackend.model.dto.chart;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 图表更新请求类，用于封装图表更新相关的请求参数
 * 实现Serializable接口以支持序列化操作，便于网络传输和持久化存储
 */
@Data // 使用Lombok的@Data注解，自动生成getter、setter、toString等方法
public class ChartUpdateRequest implements Serializable {

    /**
     * id
     * 主键ID，使用ASSIGN_ID策略自动生成
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 图表名称
     * 图表的唯一标识名称
     */
    private String name;

    /**
     * 分析目标
     * 图表所要分析的目标或目的
     */
    private String goal;

    /**
     * 图表数据
     * 用于生成图表的原始数据，通常为JSON格式
     */
    private String chartData;

    /**
     * 图表类型
     * 指定图表的展示类型，如折线图、柱状图等
     */
    private String chartType;

    /**
     * 生成的图表数据
     * 系统根据输入数据生成的图表数据
     */
    private String genChart;

    /**
     * 生成的分析结论
     * 系统对图表数据进行分析后得出的结论
     */
    private String genResult;

    /**
     * 创建时间
     * 记录图表信息的创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     * 记录图表信息的最后更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     * 逻辑删除标记，0-未删除，1-已删除
     */
    @TableLogic
    private Integer isDelete;

    // 序列化版本ID，用于Java序列化机制
    private static final long serialVersionUID = 1L;
}