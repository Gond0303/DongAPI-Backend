package com.dong.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dong.project.common.ErrorCode;
import com.dong.project.exception.BusinessException;
import com.dong.project.model.entity.ProductInfo;
import com.dong.project.model.entity.ProductOrder;
import com.dong.project.model.entity.RechargeActivity;
import com.dong.project.model.enums.PaymentStatusEnum;
import com.dong.project.model.enums.ProductTypeStatusEnum;
import com.dong.project.model.vo.ProductOrderVo;
import com.dong.project.model.vo.UserVO;
import com.dong.project.service.OrderService;
import com.dong.project.service.ProductInfoService;
import com.dong.project.service.ProductOrderService;
import com.dong.project.service.RechargeActivityService;
import com.dong.project.utils.RedissonLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Resource
    private ProductOrderService productOrderService;

    @Resource
    private List<ProductOrderService> productOrderServices;

    @Resource
    private ProductInfoService productInfoService;

    //充值活动信息表
    @Resource
    private RechargeActivityService rechargeActivityService;

    @Resource
    private RedissonLockUtil redissonLockUtil;

    /**
     * 按付费类型获取产品订单服务
     * @param payType 付款类型
     * @return
     */
    @Override
    public ProductOrderService getProductOrderServiceByPayType(String payType) {
        return productOrderServices.stream()
                .filter(s -> {
                    Qualifier qualifierAnnotation = s.getClass().getAnnotation(Qualifier.class);
                    return qualifierAnnotation != null && qualifierAnnotation.value().equals(payType);
                })
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "暂无该支付方式"));
    }

    /**
     * 按照付款类型创建订单
     * @param productId 商品id
     * @param payType 付款类型
     * @param loginUser 登录用户
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductOrderVo createOrderByPayType(Long productId, String payType, UserVO loginUser) {
        // 按付费类型获取产品订单服务Bean，可能是微信支付也可能是支付宝，需要调用不同的实现类
        ProductOrderService productOrderService = getProductOrderServiceByPayType(payType);
        String redissonLock = ("getOrder_" + loginUser.getUserAccount()).intern();

        //1.查询订单是否存在（存在）
        ProductOrderVo productOrderVo = redissonLockUtil.redissonDistributedLocks(redissonLock, () -> {
            //订单存在的话就返回不再创建新的
            return productOrderService.getProductOrder(productId, loginUser, payType);
        });

        //有订单直接返回
        if (productOrderVo != null){
            return productOrderVo;
        }

        //2.订单不存在
        redissonLock = ("createOrder_"+loginUser.getUserAccount()).intern();
        //分布式锁工具
        return redissonLockUtil.redissonDistributedLocks(redissonLock,()->{
            //检查是否购买的充值活动
            checkBuyRechargeActivity(loginUser.getId(), productId);
            //订单不存在并且是第一次购买的充值活动则，创建订单，返回VO信息
            return productOrderService.saveProductOrder(productId,loginUser);
        });

    }


    /**
     * 检查是否购买的充值活动
     * @param userId
     * @param productId
     */
    private void checkBuyRechargeActivity(Long userId,Long productId){
        ProductInfo productInfo = productInfoService.getById(productId);
        //如果该商品是充值活动的话
        if (productInfo.getProductType().equals(ProductTypeStatusEnum.RECHARGE_ACTIVITY)){
            //查询订单表，是充值活动的话只能购买一次，没付款的订单跟支付成功的订单都算作一次
            LambdaQueryWrapper<ProductOrder> productOrderLambdaQueryWrapper = new LambdaQueryWrapper<>();
            productOrderLambdaQueryWrapper.eq(ProductOrder::getUserId,userId);
            productOrderLambdaQueryWrapper.eq(ProductOrder::getProductId,productId);
            //查看交易状态是否为未支付或者支付成功的
            productOrderLambdaQueryWrapper.eq(ProductOrder::getStatus, PaymentStatusEnum.NOTPAY.getValue());
            productOrderLambdaQueryWrapper.or().eq(ProductOrder::getStatus,PaymentStatusEnum.SUCCESS.getValue());
            long productOrderCount = productOrderService.count(productOrderLambdaQueryWrapper);
            if (productOrderCount > 0){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"该商品只能购买一次，请查看是否已经创建了订单，或者挑选其他商品");
            }
            //充值活动商品信息是否有订单信息
            LambdaQueryWrapper<RechargeActivity> rechargeActivityLambdaQueryWrapper = new LambdaQueryWrapper<>();
            rechargeActivityLambdaQueryWrapper.eq(RechargeActivity::getUserId,userId);
            rechargeActivityLambdaQueryWrapper.eq(RechargeActivity::getProductId,productId);
            long rechargeActivityCount = rechargeActivityService.count(rechargeActivityLambdaQueryWrapper);
            if (rechargeActivityCount > 0){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"该商品只能购买一次，请查看是否已经创建了订单，或者挑选其他商品");
            }
        }
    }
}
