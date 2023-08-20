package com.easypan.service;

import com.easypan.entity.po.EmailCode;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 53082
* @description 针对表【email_code】的数据库操作Service
* @createDate 2023-08-10 12:25:35
*/
public interface EmailCodeService extends IService<EmailCode> {

    public void sendEmailCode(String email,Integer type);

    void checkCode(String email, String emailCode);
}
