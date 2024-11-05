package com.dong.project.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author dong
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    //用户昵称
    private String userName;

    //用户账号
    private String userAccount;

    //用户密码
    private String userPassword;

    //确认密码
    private String checkPassword;

    //邀请码（可以为空）
    private String invitationCode;
}
