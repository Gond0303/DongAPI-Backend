package com.dong.project.model.dto.user;


import lombok.Data;

import java.io.Serializable;

/**
 * 邮箱注册请求类
 */
@Data
public class UserEmailRegister implements Serializable {

    private static final long serialVersionUID = -6952816329314809143L;

    /*
     * 用户昵称
     */
    private String userName;

    /**
     * 邮箱号码
     */
    private String emailAccount;

    /**
     * 邀请码
     */
    private String invitationCode;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

    /*
     * 验证码
     */
    private String captcha;

}
