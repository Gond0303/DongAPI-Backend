package com.dong.project.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dong.project.common.BaseResponse;
import com.dong.project.common.ErrorCode;
import com.dong.project.common.ResultUtils;
import com.dong.project.exception.BusinessException;
import com.dong.project.model.dto.pay.PayCreateRequest;
import com.dong.project.model.dto.productOrder.ProductOrderQueryRequest;
import com.dong.project.model.entity.ProductInfo;
import com.dong.project.model.entity.ProductOrder;
import com.dong.project.model.enums.PaymentStatusEnum;
import com.dong.project.model.vo.OrderVo;
import com.dong.project.model.vo.ProductOrderVo;
import com.dong.project.model.vo.UserVO;
import com.dong.project.service.OrderService;
import com.dong.project.service.ProductOrderService;
import com.dong.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrderController {
    @Resource
    private UserService userService;

    @Resource
    private ProductOrderService productOrderService;

    @Resource
    private OrderService orderService;

    /**
     * 分页获取列表
     *
     * @param productOrderQueryRequest 接口信息查询请求
     * @param request                  请求
     */
    @GetMapping("/list/page")
    public BaseResponse<OrderVo> listProductOrderByPage(ProductOrderQueryRequest productOrderQueryRequest, HttpServletRequest request) {
        if (productOrderQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ProductOrder productOrder = new ProductOrder();
        BeanUtils.copyProperties(productOrderQueryRequest, productOrder);
        long size = productOrderQueryRequest.getPageSize();
        String orderName = productOrderQueryRequest.getOrderName();
        String orderNo = productOrderQueryRequest.getOrderNo();
        Integer total = productOrderQueryRequest.getTotal();
        String status = productOrderQueryRequest.getStatus();
        String productInfo = productOrderQueryRequest.getProductInfo();
        String productType = productOrderQueryRequest.getProductType();
        String payType = productOrderQueryRequest.getPayType();
        Integer addPoints = productOrderQueryRequest.getAddPoints();
        long current = productOrderQueryRequest.getCurrent();

        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        QueryWrapper<ProductOrder> queryWrapper = new QueryWrapper<>();

        queryWrapper.like(StringUtils.isNotBlank(orderName), "orderName", orderName)
                .like(StringUtils.isNotBlank(productInfo), "productInfo", productInfo)
                .like(StringUtils.isNotBlank(productType), "productInfo", productType)
                .eq("userId", userId)
                .eq(StringUtils.isNotBlank(orderNo), "orderNo", orderNo)
                .eq(StringUtils.isNotBlank(status), "status", status)
                .eq(StringUtils.isNotBlank(payType), "payType", payType)
                .eq(ObjectUtils.isNotEmpty(total), "total", total)
                .eq(ObjectUtils.isNotEmpty(addPoints), "addPoints", addPoints);

        // 未支付的订单前置
        queryWrapper.last("ORDER BY CASE WHEN status = 'NOTPAY' THEN 0 ELSE 1 END, status");
        Page<ProductOrder> productOrderPage = productOrderService.page(new Page<>(current, size), queryWrapper);
        OrderVo orderVo = new OrderVo();
        BeanUtils.copyProperties(productOrderPage, orderVo);
        // 处理订单信息,
        List<ProductOrderVo> productOrders = productOrderPage.getRecords().stream().map(this::formatProductOrderVo).collect(Collectors.toList());
        orderVo.setRecords(productOrders);
        return ResultUtils.success(orderVo);
    }

    /**
     * todo
     * @param productOrder
     * @return
     */
    private ProductOrderVo formatProductOrderVo(ProductOrder productOrder) {
        ProductOrderVo productOrderVo = new ProductOrderVo();
        BeanUtils.copyProperties(productOrder, productOrderVo);
        ProductInfo prodInfo = JSONUtil.toBean(productOrder.getProductInfo(), ProductInfo.class);
        productOrderVo.setDescription(prodInfo.getDescription());
        productOrderVo.setProductType(prodInfo.getProductType());
        String voTotal = String.valueOf(prodInfo.getTotal());
        BigDecimal total = new BigDecimal(voTotal).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        productOrderVo.setTotal(total.toString());
        return productOrderVo;
    }


    /**
     * 创建订单
     * @param payCreateRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/create")
    public BaseResponse<ProductOrderVo> createOrder(@RequestBody PayCreateRequest payCreateRequest, HttpServletRequest httpServletRequest){
        if (ObjectUtils.anyNull(payCreateRequest) || StringUtils.isBlank(payCreateRequest.getProductId())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long productId = Long.valueOf(payCreateRequest.getProductId());
        String payType = payCreateRequest.getPayType();
        if (StringUtils.isBlank(payType)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"暂无该支付方式");
        }
        UserVO loginUser = userService.getLoginUser(httpServletRequest);
        //通过支付类型为该用户创建订单
        ProductOrderVo productOrderVo = orderService.createOrderByPayType(productId, payType, loginUser);
        if (productOrderVo == null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"创建订单失败，请稍后再试");
        }
        return ResultUtils.success(productOrderVo);
    }

    /**
     * 按id获取产品订单
     *
     * @param id id
     */
    @GetMapping("/get")
    public BaseResponse<ProductOrderVo> getProductOrderById(String id) {
        if (StringUtils.isBlank(id)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ProductOrder productOrder = productOrderService.getById(id);
        if (productOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        ProductOrderVo productOrderVo = formatProductOrderVo(productOrder);
        return ResultUtils.success(productOrderVo);
    }

    /**
     * 关闭订单，通过订单号修改订单状态status
     * @param orderNo
     * @return
     */
    @PostMapping("/closed")
    public BaseResponse<Boolean> closedProductOrder(String orderNo){
        if (StringUtils.isBlank(orderNo)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断该订单是否存在
        ProductOrder productOrder = productOrderService.getProductOrderByOrderNo(orderNo);
        if (productOrder == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该订单不存在");
        }
        //获取支付方式的service
        ProductOrderService productOrderServiceByPayType = orderService.getProductOrderServiceByPayType(productOrder.getPayType());
        boolean closedResult = productOrderServiceByPayType.updateOrderStatusByOrderNo(orderNo, PaymentStatusEnum.CLOSED.getValue());
        return ResultUtils.success(closedResult);
    }

    /**
     * 通过订单id到订单表删除订单
     * @param id
     * @param request
     * @return
     */
    @PostMapping("delete")
    public BaseResponse<Boolean> deleteProductOrder(Long id,HttpServletRequest request){
        if (ObjectUtils.isEmpty(id)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO loginUser = userService.getLoginUser(request);
        //校验数据是否存在
        ProductOrder productOrder = productOrderService.getById(id);
        if (productOrder == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该接口不存在");
        }
        //仅限本人和管理员可以删除该账号的订单
        if (!productOrder.getUserId().equals(loginUser.getId()) && !userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"您没有权限删除该订单");
        }
        return ResultUtils.success(productOrderService.removeById(id));


    }


}
