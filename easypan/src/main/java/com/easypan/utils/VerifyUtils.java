package com.easypan.utils;

import com.easypan.entity.enums.VerifyRegexEnum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerifyUtils {
    public static boolean verify(String regex,String value){
        if(StringTools.isEmpty(value)){
            return false;
        }
        Pattern pattern=Pattern.compile(regex);
        Matcher matcher=pattern.matcher(value);
        return matcher.matches();
    }
    public static boolean verify(VerifyRegexEnum regexEnum,String value){
        return verify(regexEnum.getRegex(),value);
    }

    public static void main(String[] args) {
        boolean verify = verify("^[a-zA-Z0-9@#$%^&+=_\\-!]{8,18}$", "li1234567");
        System.out.println(verify);
    }
}
