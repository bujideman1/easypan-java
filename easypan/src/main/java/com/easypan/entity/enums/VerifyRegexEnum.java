package com.easypan.entity.enums;

import lombok.Data;
import lombok.Getter;

@Getter
public enum VerifyRegexEnum {
    NO("","不校验"),
    IP("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}", "IP地址"),
    EMAIL("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$","电子邮箱"),
    PASSWORD("^[a-zA-Z0-9@#$%^&+=_\\-!]{8,18}$","只包含数字、字母和特殊字符，长度8到18"),
    PHONE("^1[3456789]\\\\d{9}$","电话号码")
    ;
     final String regex;
     final String desc;

    VerifyRegexEnum(String regex, String desc) {
        this.regex = regex;
        this.desc = desc;
    }
}
