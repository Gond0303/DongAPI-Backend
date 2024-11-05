package com.dong.project.model.dto.interfaceinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 修改接口图片请求
 *
 */
@Data
public class InterfaceInfoUpdateAvatarRequest implements Serializable {

    /**
     * 接口id
     */
    private long id;
    /**
     * 接口头像
     */
    private String avatarUrl;


    private static final long serialVersionUID = 1L;
}