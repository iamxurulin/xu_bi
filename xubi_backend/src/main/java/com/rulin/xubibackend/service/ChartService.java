package com.rulin.xubibackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rulin.xubibackend.model.dto.chart.ChartQueryRequest;
import com.rulin.xubibackend.model.entity.Chart;

/**
* @author rulin
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2026-02-09 13:18:41
*/
public interface ChartService extends IService<Chart> {

    /**
     * 分页查询图表列表（带缓存）
     * @param chartQueryRequest 查询请求
     * @return 分页结果
     */
    IPage<Chart> listMyChartByPage(ChartQueryRequest chartQueryRequest);

    /**
     * 清除图表列表缓存
     */
    void evictChartListCache();
}
