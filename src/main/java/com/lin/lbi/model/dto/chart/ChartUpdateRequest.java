package com.lin.lbi.model.dto.chart;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 更新请求
 *
 * @author L
 *
 */
@Data
public class ChartUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 分析数据
     */
    private String data;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表名称
     */
    private String chartName;

    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 生成的图表结果
     */
    private String genChart;

    /**
     * 生成的分析结果
     */
    private String genResult;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}