package com.easypan.mapper;

import com.easypan.entity.po.EmailCode;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
* @author 53082
* @description 针对表【email_code】的数据库操作Mapper
* @createDate 2023-08-10 12:25:35
* @Entity com.easypan.entity.po.EmailCode
*/
public interface EmailCodeMapper extends BaseMapper<EmailCode> {

     void disableEmailCode(String email) ;

     @Select("select * from email_code where email=#{email} and code=#{code}")
    EmailCode selectByEmailAndCode(String email, String code);
}




