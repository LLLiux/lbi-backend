package com.lin.lbi.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 图表信息
 * @TableName chart
 */
@TableName(value ="chart")
@Data
public class Chart implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 执行状态（0-等待中 1-执行中 2-成功 3-失败）
     */
    private int status;

    /**
     * 执行信息
     */
    private String execMessage;

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
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}