package com.dong.project.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.mail")
@Data
public class EmailConfig {
    //邮箱发送方
    private String emailFrom = "2662914077@qq.com";
}
