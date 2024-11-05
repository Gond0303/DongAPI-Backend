package com.dong.project.service;

import com.dong.project.model.vo.ProductOrderVo;
import com.dong.project.model.vo.UserVO;

public interface OrderService {

    /**
     * 按付费类型获取产品订单服务
     *
     * @param payType 付款类型
     * @return {@link ProductOrderService}
     */
    ProductOrderService getProductOrderServiceByPayType(String payType);


    /**
     * 按照付款类型创建订单
     * @param productId 商品id
     * @param payType 付款类型
     * @param loginUser 登录用户
     * @return
     */
    ProductOrderVo createOrderByPayType(Long productId, String payType, UserVO loginUser);
}
