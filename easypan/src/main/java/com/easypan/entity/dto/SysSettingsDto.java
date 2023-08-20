package com.easypan.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SysSettingsDto {
    private String registerMailTitle="邮箱验证码";
    private String registerMailContent="您好，您的邮箱验证码为：%s ；15分钟有效；";
    private Integer userInitUseSpace=5;
}
