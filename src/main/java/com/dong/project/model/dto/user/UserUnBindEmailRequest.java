package com.dong.project.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 解绑邮箱
 */
@Data
public class UserUnBindEmailRequest implements Serializable {

    private static final long serialVersionUID = -2396598867364541293L;
    /**
     * 邮箱
     */
    private String emailAccount;

    /**
     * 验证码
     */
    private String captcha;
}
