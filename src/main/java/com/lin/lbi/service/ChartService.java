package com.lin.lbi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lin.lbi.model.dto.chart.ChartQueryRequest;
import com.lin.lbi.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author L
* @description 针对表【chart(图表信息)】的数据库操作Service
* @createDate 2024-01-29 17:11:23
*/
public interface ChartService extends IService<Chart> {

    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);
}
