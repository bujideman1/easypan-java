package com.easypan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.QQInfoDto;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.SysSettingsDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.entity.enums.UserStatusEnum;
import com.easypan.entity.po.UserInfo;
import com.easypan.exception.BusinessException;
import com.easypan.mapper.FileInfoMapper;
import com.easypan.service.EmailCodeService;
import com.easypan.service.UserInfoService;
import com.easypan.mapper.UserInfoMapper;
import com.easypan.utils.CopyTools;
import com.easypan.utils.JsonUtils;
import com.easypan.utils.OKHttpUtils;
import com.easypan.utils.StringTools;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

/**
* @author 53082
* @description 针对表【user_info(用户信息表)】的数据库操作Service实现
* @createDate 2023-08-09 20:29:07
*/
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo>
    implements UserInfoService{
    private static final Logger logger= LoggerFactory.getLogger(UserInfoServiceImpl.class);
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private FileInfoMapper fileInfoMapper;
    @Resource
    private EmailCodeService emailCodeService;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private AppConfig appConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void  register(String email, String nickName, String password, String emailCode) {
        UserInfo userInfo = userInfoMapper.selectByEmail(email);
        if(null!=userInfo){
            throw new BusinessException("邮箱账号已经存在");
        }
        UserInfo nickNameUser=userInfoMapper.selectByNickName(nickName);
        if(null!=nickNameUser){
            throw new BusinessException("昵称已经存在");
        }
        //校验邮箱验证码
        emailCodeService.checkCode(email,emailCode);
        String userId = StringTools.getRandomNumber(Constants.LENGTH_10);
        userInfo=new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setEmail(email);
        userInfo.setNickName(nickName);
        userInfo.setPassword(StringTools.encodeByMd5(password));
        userInfo.setJoinTime(new Date());
        userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
        SysSettingsDto sysSettingsDto = redisComponent.getSysSettingsDto();
        userInfo.setUseSpace(0L);
        userInfo.setTotalSpace(sysSettingsDto.getUserInitUseSpace()*Constants.MB);
        userInfoMapper.insert(userInfo);
    }

    @Override
    public SessionWebUserDto login(String email, String password) {
        UserInfo userInfo = userInfoMapper.selectByEmail(email);
        if(null==userInfo||!userInfo.getPassword().equals(password)){
            throw new BusinessException("账号或密码错误");
        }
        if(UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())){
            throw new BusinessException("账号已被禁用！");
        }
        userInfo.setLastLoginTime(new Date());
        userInfoMapper.updateById(userInfo);
        SessionWebUserDto sessionWebUserDto=new SessionWebUserDto();
        sessionWebUserDto.setUserId(userInfo.getUserId());
        sessionWebUserDto.setNickName(userInfo.getNickName());
        if(ArrayUtils.contains(appConfig.getAdminEmails().split(","),email)){
            sessionWebUserDto.setIsAdmin(true);
        }
        //用户空间
        //todo 查询用户使用空间
        Long useSpace = fileInfoMapper.selectUseSpace(userInfo.getUserId());
        UserSpaceDto userSpaceDto = new UserSpaceDto();
        userSpaceDto.setUseSpace(useSpace);
        userSpaceDto.setTotalSpace(userInfo.getTotalSpace());
        redisComponent.saveUserSpaceUse(userInfo.getUserId(),userSpaceDto);
        return sessionWebUserDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPwd(String email, String password, String emailCode) {
        UserInfo userInfo = userInfoMapper.selectByEmail(email);
        if(null==userInfo){
            throw new BusinessException("邮箱账号不存在");
        }
        //校验邮箱验证码
        emailCodeService.checkCode(email,emailCode);
        userInfo.setPassword(StringTools.encodeByMd5(password));
        userInfoMapper.updateById(userInfo);
    }

    @Override
    public SessionWebUserDto qqLogin(String code) {
        //1.使用授权码获取Access Token
        String accessToken=getQQAccessToken(code);
        //2.获取qqopenid
        String qqOpenId=getQQOpenId(accessToken);
        UserInfo user=userInfoMapper.selectByQqOpenId(qqOpenId);
        if(null==user){
            //自动注册
            //获取用户qq基本信息
            QQInfoDto qqInfoDto=getQQUserInfo(accessToken,qqOpenId);
            user=new UserInfo();
            String nickName=qqInfoDto.getNickname();
            nickName=nickName.length()>20?nickName.substring(0,20):nickName;
            String avatar=StringTools.isEmpty(qqInfoDto.getFigureurl_qq_2())?qqInfoDto.getFigureurl_qq_1():qqInfoDto.getFigureurl_qq_2();
            user.setQqAvatar(avatar);
            user.setStatus(UserStatusEnum.ENABLE.getStatus());
            user.setUserId(StringTools.getRandomNumber(Constants.LENGTH_10));
            user.setJoinTime(new Date());
            user.setLastLoginTime(new Date());
            user.setUseSpace(0L);
            user.setTotalSpace(redisComponent.getSysSettingsDto().getUserInitUseSpace()*Constants.MB);
            userInfoMapper.insert(user);
        }else{
            UserInfo userInfo = new UserInfo();
            userInfo.setLastLoginTime(new Date());
            userInfoMapper.updateById(userInfo);
        }
        SessionWebUserDto webUserDto = CopyTools.copy(user, SessionWebUserDto.class);
        //判断用户管理员
        webUserDto.setIsAdmin(appConfig.isAdmin(user));
        UserSpaceDto userSpaceDto=new UserSpaceDto();
        //todo 获取用户已使用空间
        Long useSpace = fileInfoMapper.selectUseSpace(user.getUserId());
        userSpaceDto.setUseSpace(useSpace);
        userSpaceDto.setTotalSpace(user.getTotalSpace());
        redisComponent.saveUserSpaceUse(user.getUserId(), userSpaceDto);
        return webUserDto;
    }

    private QQInfoDto getQQUserInfo(String accessToken, String qqOpenId) {
        String url=String.format(appConfig.getUserInfoUrl(),accessToken,appConfig.getAppId(),qqOpenId);
        String res = OKHttpUtils.getRequest(url);
        if(StringUtils.isNotBlank(res)){
            QQInfoDto qqInfoDto = JsonUtils.convertJson2Obj(res, QQInfoDto.class);
            if(qqInfoDto.getRet()!=0){
                logger.error("qqinfo:",res);
                throw new BusinessException("掉QQ接口获取用户信息异常");
            }
            return qqInfoDto;
        }
        throw new BusinessException("掉QQ接口获取用户信息异常");
    }

    private String getQQOpenId(String accessToken) {

        String url=String.format(appConfig.getOpenidUrl(),accessToken);
        String openIdRes = OKHttpUtils.getRequest(url);
        String tmpJson=getQqresp(openIdRes);
        if(tmpJson==null){
            logger.error("调qq接口获取openid失败:tmpJson",tmpJson);
            throw new BusinessException("调qq接口获取openid失败");
        }
        Map jsonData = JsonUtils.convertJson2Obj(tmpJson, Map.class);
        if(jsonData==null||jsonData.containsKey(Constants.VIEW_OBJ_RESULT_KEY)){
            logger.error("调qq接口获取openid失败:jsonData",jsonData);
            throw new BusinessException("调qq接口获取openid失败");
        }
        return String.valueOf(jsonData.get("openid"));
    }

    private String getQqresp(String res) {
        if (StringUtils.isNotBlank(res)){
            int pos=res.indexOf("callback");
            if(pos!=-1){
                int start=res.indexOf("(");
                int end = res.lastIndexOf(")");
                String jsonStr = res.substring(start, end);
                return jsonStr;
            }
        }
        return null;
    }

    private String getQQAccessToken(String code)   {
        String accessToken=null;
        String url=null;
        try {
            url=String.format(appConfig.getAuthorizationUrl(),appConfig.getAppId(),appConfig.getAppKey(), URLEncoder.encode(appConfig.getRedirectUrl(), "UTF-8"));

        } catch (UnsupportedEncodingException e) {
            logger.error("encode失败");
            throw new RuntimeException(e);
        }
        String tokenResult= OKHttpUtils.getRequest(url);
        if(tokenResult==null||tokenResult.indexOf(Constants.VIEW_OBJ_RESULT_KEY)!=-1){
            logger.error("获取qqToken失败",tokenResult);
            throw new BusinessException("获取qqtoken失败");
        }
        String[] params = tokenResult.split("&");
        if(params!=null&&params.length>0){
            for (String param : params) {
                if(param.indexOf("access_toen")!=-1){
                    accessToken=param.split("=")[1];
                    break;
                }
            }
        }
        return accessToken;
    }
}




