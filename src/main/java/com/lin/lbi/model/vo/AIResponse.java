package com.lin.lbi.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AIResponse implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 生成的图表结果
     */
    private String genChart;

    /**
     * 生成的分析结果
     */
    private String genResult;

    private static final long serialVersionUID = 1L;
}
