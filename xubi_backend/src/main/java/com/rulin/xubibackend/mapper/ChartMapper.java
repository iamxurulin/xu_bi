package com.rulin.xubibackend.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rulin.xubibackend.model.entity.Chart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
* @author rulin
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2026-02-09 13:18:41
* @Entity com.rulin.xubibackend.model.entity.Chart
*/
@Mapper
public interface ChartMapper extends BaseMapper<Chart> {

    List<Map<String,Object>> queryChartData(String querySql);
}
