package com.easypan.controller;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.component.RedisComponent;
import com.easypan.entity.dto.SysSettingsDto;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.service.FileInfoService;
import com.easypan.service.UserInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/admin")
public class AdminController extends ABaseController{
    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private RedisComponent redisComponent;
    @RequestMapping("/getSysSettings")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO getSysSettings(){
        return getSuccessResponseVO(redisComponent.getSysSettingsDto());
    }
    @RequestMapping("/saveSysSettings")
    @GlobalInterceptor(checkAdmin = true,checkParams = true)
    public ResponseVO saveSysSettings(
            @VerifyParam(required = true)String registerEmailTitle,
            @VerifyParam(required = true)String registerEmailContent,
            @VerifyParam(required = true)Integer userInitUseSpace
    ){
        SysSettingsDto sysSettingsDto = new SysSettingsDto(registerEmailTitle, registerEmailContent, userInitUseSpace);
        redisComponent.saveSysSettingsDto(sysSettingsDto);
        return getSuccessResponseVO(null);
    }
    @RequestMapping("/loadUserList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadUserList(UserInfoQuery query){
        PaginationResultVO resultVO =userInfoService.findListByPage(query);
        return getSuccessResponseVO(resultVO);
    }
    @RequestMapping("/updateUserStatus")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO updateUserStatus(UserInfoQuery query){
        UserInfo userInfo =userInfoService.updateUserStatus(query);
        return getSuccessResponseVO(userInfo);
    }
    @RequestMapping("/updateUserSpace")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO updateUserSpace(UserInfoQuery query){
        UserInfo userInfo =userInfoService.updateUserStatus(query);
        return getSuccessResponseVO(userInfo);
    }
}
