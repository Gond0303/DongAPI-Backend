package com.dong.project.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户邮箱登录请求体
 *
 * @author dong
 */
@Data
public class UserEmailLoginRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 邮箱
     */
    private String emailAccount;

    /**
     * 验证码
     */
    private String captcha;
}
