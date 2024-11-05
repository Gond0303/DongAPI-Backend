package com.dong.project.model.vo;

import com.dong.dongapicommon.model.entity.InterfaceInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口信息封装类
 *
 * @author dong
 * @TableName dongapi
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InterfaceInfoVO extends InterfaceInfo {


    /**
     * 统计接口调用次数
     */
    private Integer total;

    private static final long serialVersionUID = 1L;
}