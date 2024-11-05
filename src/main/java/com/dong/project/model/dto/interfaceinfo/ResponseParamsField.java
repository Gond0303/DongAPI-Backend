package com.dong.project.model.dto.interfaceinfo;

import lombok.Data;

/**
 * 请求参数字段
 */
@Data
public class ResponseParamsField {
    private String id;
    private String fieldName;
    private String type;
    private String desc;
}