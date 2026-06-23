package com.rulin.xubibackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rulin.xubibackend.common.ErrorCode;
import com.rulin.xubibackend.constant.CommonConstant;
import com.rulin.xubibackend.exception.BusinessException;
import com.rulin.xubibackend.exception.ThrowUtils;
import com.rulin.xubibackend.mapper.ChartMapper;
import com.rulin.xubibackend.model.dto.chart.ChartQueryRequest;
import com.rulin.xubibackend.model.entity.Chart;
import com.rulin.xubibackend.service.ChartService;
import com.rulin.xubibackend.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
* @author rulin
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2026-02-09 13:18:41
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
implements ChartService {

    @Override
    @Cacheable(cacheNames = "chartList", key = "#chartQueryRequest.userId + ':' + #chartQueryRequest.current + ':' + #chartQueryRequest.pageSize + ':' + (#chartQueryRequest.name ?: '')")
    public IPage<Chart> listMyChartByPage(ChartQueryRequest chartQueryRequest) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long userId = chartQueryRequest.getUserId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        // 参数校验
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID无效");
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页不能超过20条");

        // 构建查询条件
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                CommonConstant.SORT_ORDER_ASC.equalsIgnoreCase(sortOrder),
                sortField);

        Page<Chart> chartPage = this.page(new Page<>(current, size), queryWrapper);
        return chartPage;
    }

    /**
     * 清除指定用户的图表列表缓存
     */
    @CacheEvict(cacheNames = "chartList", allEntries = true)
    public void evictChartListCache() {
        // 仅用于触发缓存清除
    }
}
