package com.dong.project.service.impl;


import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.project.common.ErrorCode;
import com.dong.project.config.AliPayAccountConfig;
import com.dong.project.exception.BusinessException;
import com.dong.project.mapper.ProductOrderMapper;
import com.dong.project.model.entity.ProductInfo;
import com.dong.project.model.entity.ProductOrder;
import com.dong.project.model.enums.PaymentStatusEnum;
import com.dong.project.model.vo.ProductOrderVo;
import com.dong.project.model.vo.UserVO;
import com.dong.project.service.ProductInfoService;
import com.dong.project.service.ProductOrderService;
import com.ijpay.alipay.AliPayApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import static com.dong.project.constant.PayConstant.ORDER_PREFIX;
import static com.dong.project.model.enums.PayTypeStatusEnum.ALIPAY;

/**
* @author 黄伟东
* @description 针对表【product_order(商品订单)】的数据库操作Service实现
* @createDate 2024-09-27 19:26:44
*/
@Service
@Qualifier("ALIPAY")
@Slf4j
public class AliPayOrderServiceImpl extends ServiceImpl<ProductOrderMapper, ProductOrder>
    implements ProductOrderService {

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private AliPayAccountConfig aliPayAccountConfig;

    /**
     * 获取产品订单
     * 获取订单
     */
    @Override
    public ProductOrderVo getProductOrder(Long productId, UserVO loginUser, String payType) {
        LambdaQueryWrapper<ProductOrder> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ProductOrder::getProductId, productId);
        lambdaQueryWrapper.eq(ProductOrder::getStatus, PaymentStatusEnum.NOTPAY.getValue());
        lambdaQueryWrapper.eq(ProductOrder::getPayType, payType);
        lambdaQueryWrapper.eq(ProductOrder::getUserId, loginUser.getId());
        ProductOrder oldOrder = this.getOne(lambdaQueryWrapper);
        if (oldOrder == null) {
            return null;
        }
        ProductOrderVo productOrderVo = new ProductOrderVo();
        BeanUtils.copyProperties(oldOrder, productOrderVo);
        //因为vo的productInfo是个对象而数据库中的是JSON，返回给前端的价格total是string数据库中是Long，所以需要转
        productOrderVo.setProductInfo(JSONUtil.toBean(oldOrder.getProductInfo(), ProductInfo.class));
        productOrderVo.setTotal(oldOrder.getTotal().toString());
        return productOrderVo;
    }

    /**
     * 保存产品订单
     * @param productId 产品id
     * @param loginUser 登录用户
     * @return
     */
    @Override
    public ProductOrderVo saveProductOrder(Long productId, UserVO loginUser) {
        //获得商品信息
        ProductInfo productInfo = productInfoService.getById(productId);
        if (productInfo == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"商品不存在");
        }
        //5分钟有效
        Date date = DateUtil.date(System.currentTimeMillis());
        Date expirationTime = DateUtil.offset(date, DateField.MINUTE, 5);
        String orderNo = ORDER_PREFIX + RandomUtil.randomNumbers(20);

        ProductOrder productOrder = new ProductOrder();
        productOrder.setOrderNo(orderNo);
        productOrder.setUserId(loginUser.getId());
        productOrder.setProductId(productId);
        productOrder.setOrderName(productInfo.getName());
        productOrder.setTotal(productInfo.getTotal());
        productOrder.setStatus(PaymentStatusEnum.NOTPAY.getValue());
        productOrder.setPayType(ALIPAY.getValue());
        //这边的商品是被转为JSON存储到数据库,下面返回给前端的vo则是直接返回对象
        productOrder.setProductInfo(JSONUtil.toJsonPrettyStr(productInfo));
        productOrder.setAddPoints(productInfo.getAddBalance());
        productOrder.setExpirationTime(expirationTime);

        boolean saveResult = this.save(productOrder);

        /**
         * 支付调用
         */
        //1.创建一个AlipayTradePagePayModel对象，用于设置支付请求的参数
        AlipayTradePagePayModel alipayTradePagePayModel = new AlipayTradePagePayModel();
        //设置订单编号
        alipayTradePagePayModel.setOutTradeNo(orderNo);
        //设置订单的名称或标题，通常在支付宝的支付页面上显示
        alipayTradePagePayModel.setSubject(productInfo.getName());
        //设置支付宝的产品码，对于页面支付，这个值通常是"FAST_INSTANT_TRADE_PAY"
        alipayTradePagePayModel.setProductCode("FAST_INSTANT_TRADE_PAY");

        // 将产品的金额四舍五入执行除法运算，将金额从分转换为元，并保留两位小数
        BigDecimal scaledAmount = new BigDecimal(productInfo.getTotal()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        //设置收款金额
        alipayTradePagePayModel.setTotalAmount(String.valueOf(scaledAmount));
        //设置订单描述信息，这个信息在支付页面不会展示，但可以在支付宝后台管理中查看
        alipayTradePagePayModel.setBody(productInfo.getDescription());

        //2.创建一个AlipayTradePagePayRequest对象，用于设置支付请求的请求参数
        AlipayTradePagePayRequest alipayTradePagePayRequest = new AlipayTradePagePayRequest();
        //将之前设置的支付参数模型（model）设置到请求对象中
        alipayTradePagePayRequest.setBizModel(alipayTradePagePayModel);
        //设置异步通知地址，支付宝会在支付成功后异步回调这个地址
        alipayTradePagePayRequest.setNotifyUrl(aliPayAccountConfig.getNotifyUrl());
        //设置同步跳转地址，用户在支付宝完成支付后，会同步跳转到这个地址
        alipayTradePagePayRequest.setReturnUrl(aliPayAccountConfig.getReturnUrl());

        try {
            //3.调用返回的是一个 AlipayTradePagePayResponse 对象，它包含了支付宝生成的支付页面链接等信息。
            //这边的AliPayApi已经初始化接口new DefaultAlipayClient了
            AlipayTradePagePayResponse alipayTradePagePayResponse = AliPayApi.pageExecute(alipayTradePagePayRequest);
            String payUrl = alipayTradePagePayResponse.getBody();
            productOrder.setFormData(payUrl);
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }
        //todo 到时候看一下这个productOrder有没有上面的数据是不是同一个对象
        boolean updateResult = this.updateProductOrder(productOrder);

        if (!saveResult && !updateResult){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }

        //4.构建VO
        ProductOrderVo productOrderVo = new ProductOrderVo();
        BeanUtils.copyProperties(productOrder,productOrderVo);
        //由于给前端的是对象类型，得重新赋值，原本数据库的是JSON类型。将包含的商品信息放入
        productOrderVo.setProductInfo(productInfo);
        //由于给前端的是string类型，得重新转，原本数据库的是Long类型
        productOrderVo.setTotal(productInfo.getTotal().toString());
        return productOrderVo;


    }

    /**
     * 更新产品订单信息
     * @param productOrder 产品订单
     * @return
     */
    @Override
    public boolean updateProductOrder(ProductOrder productOrder) {
        //获取地址
        String formData = productOrder.getFormData();
        Long id = productOrder.getId();
        ProductOrder updateCodeUrl = new ProductOrder();
        updateCodeUrl.setFormData(formData);
        updateCodeUrl.setId(id);
        return this.updateById(updateCodeUrl);
    }

    /**
     * 通过订单号查询该订单是否存在
     * @param orderNo
     * @return
     */
    @Override
    public ProductOrder getProductOrderByOrderNo(String orderNo) {
        LambdaQueryWrapper<ProductOrder> productOrderLambdaQueryWrapper = new LambdaQueryWrapper<ProductOrder>();
        productOrderLambdaQueryWrapper.eq(ProductOrder::getOrderNo,orderNo);
        return this.getOne(productOrderLambdaQueryWrapper);
    }

    /**
     * 通过订单号关闭订单
     * @param orderNo
     * @param orderStatus
     * @return
     */
    @Override
    public boolean updateOrderStatusByOrderNo(String orderNo, String orderStatus) {
        ProductOrder productOrder = new ProductOrder();
        productOrder.setStatus(orderStatus);
        LambdaQueryWrapper<ProductOrder> productOrderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        productOrderLambdaQueryWrapper.eq(ProductOrder::getOrderNo,orderNo);
        return this.update(productOrder,productOrderLambdaQueryWrapper);
    }
}




