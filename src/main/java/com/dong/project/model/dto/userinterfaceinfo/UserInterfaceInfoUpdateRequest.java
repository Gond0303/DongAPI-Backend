package com.dong.project.model.dto.userinterfaceinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 *
 * @TableName product
 */
@Data
public class UserInterfaceInfoUpdateRequest implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 总调用次数
     */
    private Integer totalInvokes;


    /**
     * 接口状态（0-正常 1-禁用）
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}