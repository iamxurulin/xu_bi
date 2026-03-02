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
@Mapper  // 标识该接口为MyBatis Mapper接口，用于数据库操作
public interface ChartMapper extends BaseMapper<Chart> {  // 继承BaseMapper，获得基本的CRUD操作能力



    /**
     * 根据查询SQL获取图表数据
     * @param querySql 查询SQL语句，用于从数据库获取图表所需的数据
     * @return 返回一个Map列表，每个Map代表一行数据，键为列名，值为对应的值
     */
    List<Map<String,Object>> queryChartData(String querySql);  // 自定义方法，用于执行特定的SQL查询并返回图表数据
}
