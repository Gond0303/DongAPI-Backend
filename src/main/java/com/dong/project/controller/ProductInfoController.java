package com.dong.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dong.project.annotation.AuthCheck;
import com.dong.project.common.*;
import com.dong.project.exception.BusinessException;
import com.dong.project.model.dto.productinfo.ProductInfoQueryRequest;
import com.dong.project.model.entity.ProductInfo;
import com.dong.project.model.enums.ProductInfoStatusEnum;
import com.dong.project.service.ProductInfoService;
import com.dong.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.dong.project.constant.UserConstant.ADMIN_ROLE;

@RestController
@RequestMapping("/productInfo")
@Slf4j
public class ProductInfoController {

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private UserService userService;

    /**
     * 管理员获取商品列表
     * @param productInfoQueryRequest
     * @return
     */
    @AuthCheck(mustRole = ADMIN_ROLE)
    @GetMapping("/list")
    public BaseResponse<List<ProductInfo>> listProductInfo(ProductInfoQueryRequest productInfoQueryRequest) {
        if (productInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ProductInfo productInfoQuery = new ProductInfo();
        BeanUtils.copyProperties(productInfoQueryRequest, productInfoQuery);

        String name = productInfoQueryRequest.getName();
        String description = productInfoQueryRequest.getDescription();
        Long total = productInfoQueryRequest.getTotal();
        Long addBalance = productInfoQueryRequest.getAddBalance();
        String productType = productInfoQueryRequest.getProductType();
        Integer status = productInfoQueryRequest.getStatus();

        QueryWrapper<ProductInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(name),"name",name)
                .like(StringUtils.isNotBlank(description),"description",description)
                .eq(StringUtils.isNotBlank(productType),"productType",productType)
                .eq(ObjectUtils.isNotEmpty(addBalance),"addBalance",addBalance)
                .eq(ObjectUtils.isNotEmpty(total),"total",total)
                .eq(ObjectUtils.isNotEmpty(status),"status",status);
        List<ProductInfo> productInfoList =  productInfoService.list(queryWrapper);
        return ResultUtils.success(productInfoList);
    }


    /**
     * 分页获取商品数据
     * @param productInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<ProductInfo>> listProductInfoByPage(ProductInfoQueryRequest productInfoQueryRequest, HttpServletRequest request){
        if (productInfoQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        ProductInfo productInfo = new ProductInfo();
        BeanUtils.copyProperties(productInfoQueryRequest,productInfo);
        //获取页面数据
        long pageSize = productInfoQueryRequest.getPageSize();
        String sortField = productInfoQueryRequest.getSortField();
        String sortOrder = productInfoQueryRequest.getSortOrder();
        long current = productInfoQueryRequest.getCurrent();

        String name = productInfoQueryRequest.getName();
        String description = productInfoQueryRequest.getDescription();
        Long total = productInfoQueryRequest.getTotal();
        Long addBalance = productInfoQueryRequest.getAddBalance();
        String productType = productInfoQueryRequest.getProductType();
        //限制爬虫,一次获取五十条数据则报错
        if (pageSize > 50){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"大哥别爬我网站呗");
        }

        QueryWrapper<ProductInfo> productInfoQueryWrapper = new QueryWrapper<>();
        productInfoQueryWrapper.like(StringUtils.isNotBlank(name),"name",name)
                .like(StringUtils.isNotBlank(description),"description",description)
                .eq(StringUtils.isNotBlank(productType),"productType",productType)
                .eq(ObjectUtils.isNotEmpty(addBalance),"addBalance",addBalance)
                .eq(ObjectUtils.isNotEmpty(total),"total",total);
        //根据金额升序
        productInfoQueryWrapper.orderByAsc("total");
        //查询
        Page<ProductInfo> productInfoPage = productInfoService.page(new Page<>(current, pageSize), productInfoQueryWrapper);
        //不是管理员只允许查看已经上线了的商品数据
//        if (!userService.isAdmin(request)){
//            List<ProductInfo> productInfoList  = productInfoPage.getRecords().stream()
//                    .filter(product -> product.getStatus().equals(ProductInfoStatusEnum.ONLINE.getValue()))
//                    .collect(Collectors.toList());
//            productInfoPage.setRecords(productInfoList);
//        }
        List<ProductInfo> productInfoList  = productInfoPage.getRecords().stream()
                .filter(product -> product.getStatus().equals(ProductInfoStatusEnum.ONLINE.getValue()))
                .collect(Collectors.toList());
        productInfoPage.setRecords(productInfoList);
        return ResultUtils.success(productInfoPage);
    }

    @AuthCheck(mustRole = ADMIN_ROLE)
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteProductInfo(DeleteRequest deleteRequest){
        if (ObjectUtils.anyNull(deleteRequest,deleteRequest.getId()) || deleteRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = deleteRequest.getId();
        //判断是否存在
        ProductInfo productInfo = productInfoService.getById(id);
        if (productInfo == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        boolean result = productInfoService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 上线商品
     * @param idRequest
     * @param request
     * @return
     */
    @AuthCheck(mustRole = ADMIN_ROLE)
    @PostMapping("/online")
    public BaseResponse<Boolean> onlineProductInfo(IdRequest idRequest,HttpServletRequest request){
        if (ObjectUtils.anyNull(idRequest,idRequest.getId()) || idRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = idRequest.getId();
        ProductInfo productInfo = productInfoService.getById(id);
        if (productInfo == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        productInfo.setStatus(ProductInfoStatusEnum.ONLINE.getValue());
        return ResultUtils.success(productInfoService.updateById(productInfo));
    }

    /**
     * 下线商品
     * @param idRequest
     * @param request
     * @return
     */
    @AuthCheck(mustRole = ADMIN_ROLE)
    @PostMapping("/offline")
    public BaseResponse<Boolean> offlineProductInfo(IdRequest idRequest,HttpServletRequest request){
        if (ObjectUtils.anyNull(idRequest,idRequest.getId()) || idRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = idRequest.getId();
        ProductInfo productInfo = productInfoService.getById(id);
        if (productInfo == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        productInfo.setStatus(ProductInfoStatusEnum.OFFLINE.getValue());
        return ResultUtils.success(productInfoService.updateById(productInfo));
    }






}
