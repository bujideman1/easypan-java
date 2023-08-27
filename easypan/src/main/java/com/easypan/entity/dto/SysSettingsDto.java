package com.easypan.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysSettingsDto {
    private String registerEmailTitle ="邮箱验证码";
    private String registerEmailContent ="您好，您的邮箱验证码为：%s ；15分钟有效；";
    private Integer userInitUseSpace=5;
}
