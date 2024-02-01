package com.lin.lbi.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author L
 */

public enum AIGenerateStatusEnum {

    // 执行状态（0-等待中 1-执行中 2-成功 3-失败）
    WAITING("等待中", 0),
    RUNNING("执行中", 1),
    SUCCEED("成功", 2),
    FAILED("失败", 3);

    private final String text;
    private final Integer value;

    AIGenerateStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static AIGenerateStatusEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (AIGenerateStatusEnum anEnum : AIGenerateStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public Integer getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
