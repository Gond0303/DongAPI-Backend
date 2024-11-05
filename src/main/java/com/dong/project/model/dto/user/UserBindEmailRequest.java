package com.dong.project.model.dto.user;

import lombok.Data;
import java.io.Serializable;

/**
 * 绑定邮箱
 */
@Data
public class UserBindEmailRequest implements Serializable {

    private static final long serialVersionUID = -6925537503662841153L;

    /**
     * 邮箱
     */
    private String emailAccount;

    /**
     * 验证码
     */
    private String captcha;
}
