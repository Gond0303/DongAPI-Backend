package com.dong.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.dong.project.model.entity.ProductOrder;
import com.dong.project.model.vo.ProductOrderVo;
import com.dong.project.model.vo.UserVO;

/**
* @author 黄伟东
* @description 针对表【product_order(商品订单)】的数据库操作Service
* @createDate 2024-09-27 19:26:44
*/
public interface ProductOrderService extends IService<ProductOrder> {

    /**
     * 获取产品订单
     * 获取订单
     *
     * @param productId 产品id
     * @param loginUser 登录用户
     * @param payType   付款类型
     * @return {@link ProductOrderVo}
     */
    ProductOrderVo getProductOrder(Long productId, UserVO loginUser, String payType);

    /**
     * 保存产品订单
     *
     * @param productId 产品id
     * @param loginUser 登录用户
     */
    ProductOrderVo saveProductOrder(Long productId, UserVO loginUser);

    /**
     * 更新产品订单
     *
     * @param productOrder 产品订单
     * @return boolean
     */
    boolean updateProductOrder(ProductOrder productOrder);


    /**
     * 通过订单号获得产品订单
     * @param orderNo
     * @return
     */
    ProductOrder getProductOrderByOrderNo(String orderNo);

    /**
     * 通过订单号关闭订单
     * @param orderNo
     * @param orderStatus
     * @return
     */
    boolean updateOrderStatusByOrderNo(String orderNo, String orderStatus);
}
