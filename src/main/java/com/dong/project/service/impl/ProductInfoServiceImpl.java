package com.dong.project.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.project.mapper.ProductInfoMapper;
import com.dong.project.model.entity.ProductInfo;
import com.dong.project.service.ProductInfoService;
import org.springframework.stereotype.Service;

/**
* @author 黄伟东
* @description 针对表【product_info(产品信息)】的数据库操作Service实现
* @createDate 2024-09-27 02:01:11
*/
@Service
public class ProductInfoServiceImpl extends ServiceImpl<ProductInfoMapper, ProductInfo>
    implements ProductInfoService {

}




