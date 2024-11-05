package com.dong.project.model.dto.interfaceinfo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 调用请求封装类
 */
@Data
public class InterfaceInfoInvokeRequest implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 请求参数集合
     */
    private List<Field> requestParams;


    private static final long serialVersionUID = 1L;

    /**
     * 请求参数
     */
    @Data
    public static class Field{
        /**
         * 参数名称
         */
        private String fieldName;

        /**
         * 参数值
         */
        private String value;
    }
}
