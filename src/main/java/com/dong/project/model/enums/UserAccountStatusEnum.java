package com.dong.project.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户账号状态枚举
 */
public enum UserAccountStatusEnum {
    /**
     * 正常
     */
    NORMAL("正常",0),
    /**
     * 封号
     */
    BAN("封禁",1);

    private  final String text;
    private  final int value;

    UserAccountStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值
     */
    public static List<Integer> getValues(){
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public String getText() {
        return text;
    }

    public int getValue() {
        return value;
    }
}
