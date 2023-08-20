package com.easypan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SysSettingsDto;
import com.easypan.entity.po.EmailCode;
import com.easypan.entity.po.UserInfo;
import com.easypan.exception.BusinessException;
import com.easypan.mapper.UserInfoMapper;
import com.easypan.service.EmailCodeService;
import com.easypan.mapper.EmailCodeMapper;
import com.easypan.utils.StringTools;
import org.apache.ibatis.javassist.bytecode.stackmap.BasicBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.util.Date;

/**
* @author 53082
* @description 针对表【email_code】的数据库操作Service实现
* @createDate 2023-08-10 12:25:35
*/
@Service
public class EmailCodeServiceImpl extends ServiceImpl<EmailCodeMapper, EmailCode>
    implements EmailCodeService{
    private static final Logger logger= LoggerFactory.getLogger(EmailCodeServiceImpl.class);
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private JavaMailSender javaMailSender;
    @Resource
    private AppConfig appConfig;
    @Resource
    private RedisComponent redisComponent;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendEmailCode(String email, Integer type) {
        if(type==0){
           UserInfo userInfo= userInfoMapper.selectByEmail(email);
           if(null!=userInfo){
               throw new BusinessException("邮箱已经存在");
           }
        }
        String code = StringTools.getRandomNumber(Constants.LENGTH_5);
        //发送验证码
        sendMailCode(email,code);
        //重置验证码
        getBaseMapper().disableEmailCode(email);
        EmailCode emailCode = new EmailCode();
        emailCode.setCode(code);
        emailCode.setEmail(email);
        emailCode.setStatus(Constants.ZERO);
        emailCode.setCreateTime(new Date());
        getBaseMapper().insert(emailCode);

    }

    @Override
    public void checkCode(String email, String code) {
        EmailCodeMapper emailCodeMapper = getBaseMapper();
        EmailCode emailCode=emailCodeMapper.selectByEmailAndCode(email,code);
        if (null==emailCode){
            throw new BusinessException("邮箱验证码不正确");
        }
        if(emailCode.getStatus()==1||System.currentTimeMillis()-emailCode.getCreateTime().getTime()>Constants.LENGTH_5*1000*60){
            throw new BusinessException("验证码已过期");
        }
        emailCodeMapper.disableEmailCode(email);
    }

    private void sendMailCode(String toEmail,String code){
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(appConfig.getSendUserName());
            helper.setTo(toEmail);
            SysSettingsDto sysSettingsDto = redisComponent.getSysSettingsDto();

            helper.setSubject(sysSettingsDto.getRegisterMailTitle());
            helper.setText(String.format(sysSettingsDto.getRegisterMailContent(),code));
            helper.setSentDate(new Date());
            javaMailSender.send(mimeMessage);

        }
        catch (Exception e){
            logger.error("邮件发送失败",e);
            throw new BusinessException("邮件发送失败");
        }

    }
}




