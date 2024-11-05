package com.dong.project.model.dto.pay;

import lombok.Data;

import java.io.Serializable;

@Data
public class PayCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品id
     */
    private String productId;

    /**
     * 支付类型
     * ALIPAY 支付宝支付
     * WX     微信支付
     */
    private String payType;

}