package com.rulin.xubibackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rulin.xubibackend.mapper.ChartMapper;
import com.rulin.xubibackend.model.entity.Chart;
import com.rulin.xubibackend.service.ChartService;
import org.springframework.stereotype.Service;

/**
* @author rulin
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2026-02-09 13:18:41
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
implements ChartService{

}
