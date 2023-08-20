package com.easypan.service;

import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.po.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 53082
* @description 针对表【user_info(用户信息表)】的数据库操作Service
* @createDate 2023-08-09 20:29:07
*/
public interface UserInfoService extends IService<UserInfo> {

    void register(String email, String nickName, String password, String emailCode);

    SessionWebUserDto login(String email, String password);

    void resetPwd(String email, String password, String emailCode);

    SessionWebUserDto qqLogin(String code);
}
