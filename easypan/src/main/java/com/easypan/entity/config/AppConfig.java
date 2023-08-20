package com.easypan.entity.config;

import com.easypan.entity.po.UserInfo;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component("appConfig")
public class AppConfig {
    @Value("${spring.mail.username:}")
    private String sendUserName;
    @Value("${admin.emails:}")
    private String adminEmails;
    @Value("${project.folder:}")
    private String projectFolder;
    @Value("${qq.app.id}")
    private String appId;

    @Value("${qq.app.key}")
    private String appKey;

    @Value("${qq.url.authorization}")
    private String authorizationUrl;

    @Value("${qq.url.access.token}")
    private String accessTokenUrl;

    @Value("${qq.url.openid}")
    private String openidUrl;

    @Value("${qq.url.user.info}")
    private String userInfoUrl;

    @Value("${qq.url.redirect}")
    private String redirectUrl;
    public boolean isAdmin(UserInfo userInfo){
        return ArrayUtils.contains(adminEmails.split(","),userInfo.getEmail()==null?"":userInfo.getEmail());
    }
}
