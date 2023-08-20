package com.easypan.mapper;

import com.easypan.entity.po.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
* @author 53082
* @description 针对表【user_info(用户信息表)】的数据库操作Mapper
* @createDate 2023-08-09 20:29:07
* @Entity com.easypan.entity.po.UserInfo
*/
public interface UserInfoMapper extends BaseMapper<UserInfo> {

    UserInfo selectByEmail(String email);

    UserInfo selectByNickName(String nickName);

    @Select("select * from user_info where qq_open_id=#{qqOpenId}")
    UserInfo selectByQqOpenId(String qqOpenId);
    Integer updateUserSpace(String userId,Long useSpace,Long totalSpace);
}




