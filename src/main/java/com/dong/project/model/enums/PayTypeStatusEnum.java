package com.dong.project.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 支付类型枚举类型
 */
public enum PayTypeStatusEnum {
    /**
     * 微信支付
     */
    WX("微信支付", "WX"),
    /**
     * 支付宝支付
     */
    ALIPAY("支付宝支付", "ALIPAY");

    private final String text;
    private final String value;


    /**
     * 得到值
     * @return
     */
    public static List<String> getValues(){
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    PayTypeStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }
}
