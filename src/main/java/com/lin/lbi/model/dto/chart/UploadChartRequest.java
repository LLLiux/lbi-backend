package com.lin.lbi.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传请求
 *
 * @author L
 *
 */
@Data
public class UploadChartRequest implements Serializable {

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

    private static final long serialVersionUID = 1L;
}