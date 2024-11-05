package com.dong.project.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品状态枚举
 * 商品状态（0- 默认下线 1- 上线）
 */
public enum ProductInfoStatusEnum {
    /**
     * 上线
     */
    ONLINE("上线",1),
    /**
     * 下线
     */
    OFFLINE("下线",0);

    private final String text;
    private final int value;

    ProductInfoStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public int getValue() {
        return value;
    }

    /**
     * 获取值
     */
    public static List<Integer> getValues(){
        return Arrays.stream(values()).map(item->item.value).collect(Collectors.toList());
    }
}
