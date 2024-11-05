package com.dong.project.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求
 *
 * @author dong
 */
@Data
public class UserUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * 用户昵称
     */
    private String userName;
    /**
     * 账号
     */
    private String userAccount;
    /**
     * 用户头像
     */
    private String userAvatar;
    /**
     * 性别
     */
    private Integer gender;
    /**
     * 用户角色: user, admin
     */
    private String userRole;
    /**
     * 账号状态（0- 正常 1- 封号）
     */
    private Integer status;
    /**
     * 密码
     */
    private String userPassword;

    /**
     * 钱包余额（分）
     */
    private Integer balance;

    private static final long serialVersionUID = 1L;
}