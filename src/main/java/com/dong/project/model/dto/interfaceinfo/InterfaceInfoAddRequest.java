package com.dong.project.model.dto.interfaceinfo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建请求
 *
 * @TableName product
 */
@Data
public class InterfaceInfoAddRequest implements Serializable {

    /**
     * 接口名称
     */
    private String name;
    /**
     * 返回格式
     */
    private String returnFormat;
    /**
     * 接口地址
     */
    private String url;
    /**
     * 描述信息
     */
    private String description;
    /**
     * 请求方法
     */
    private String method;
    /**
     * 减少积分个数
     */
    private Long reduceScore;
    /**
     * 接口请求参数
     */
    private List<RequestParamsField> requestParams;
    /**
     * 接口响应参数
     */
    private List<ResponseParamsField> responseParams;

    /**
     * 请求示例
     */
    private String requestExample;
    /**
     * 请求头
     */
    private String requestHeader;
    /**
     * 响应头
     */
    private String responseHeader;


    private static final long serialVersionUID = 1L;
}