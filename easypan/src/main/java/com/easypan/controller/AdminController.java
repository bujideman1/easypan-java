package com.easypan.controller;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.component.RedisComponent;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.SysSettingsDto;
import com.easypan.entity.enums.FileCategoryEnums;
import com.easypan.entity.enums.FileDelFlagEnums;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.service.FileInfoService;
import com.easypan.service.UserInfoService;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController extends CommonFileController{
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
    @GlobalInterceptor(checkAdmin = true,checkParams = true)
    public ResponseVO updateUserSpace(@VerifyParam(required = true)String userId,
                                      @VerifyParam(required = true)Integer changeSpace){
        userInfoService.changeUserSpace(userId,changeSpace);
        return getSuccessResponseVO(null);
    }
    @RequestMapping("/loadFileList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO updateUserStatus(FileInfoQuery query){
        PaginationResultVO resultVO=fileInfoService.findAllFile(query);
        return getSuccessResponseVO(resultVO);
    }
    @RequestMapping("/getFile/{userId}/{fileId}")
    @GlobalInterceptor(checkAdmin = true)
    public void getFile(HttpServletResponse response,
                              @PathVariable("userId")String userId,
                              @PathVariable("fileId")String fileId){
        super.getFile(response,fileId,userId);
    }
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    @RequestMapping("/ts/getVideoInfo/{userId}/{fileId}")
    public void getVideo(HttpServletResponse response,
                         @PathVariable("userId") String userId,
                         @PathVariable("fileId") String fileId
    )
    {
        super.getFile(response,fileId,userId);
    }
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    @RequestMapping("/getFolderInfo")
    public ResponseVO getFolderInfo(
                                    @VerifyParam(required = true) String path,
                                    String shareId
    ) {
        ResponseVO responseVO=super.getFolderInfo(path,null);
        return responseVO;
    }
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    @RequestMapping("/createDownloadUrl/{userId}/{fileId}")
    public ResponseVO createDownloadUrl(@PathVariable("userId") String userId,
                                        @VerifyParam(required = true)@PathVariable("fileId") String fileId
    ) {

        return  super.createDownloadUrl(fileId,userId);

    }
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    @RequestMapping("/download/{code}")
    public void createDownloadUrl(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @VerifyParam(required = true)@PathVariable("code") String code
    ) {
        super.download(request,response,code);

    }
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    @RequestMapping("/delFile")
    public ResponseVO delFile(@VerifyParam(required = true)String fileIdAndUserIds
    ) {
        String[] fileIdAndUserIdArray = fileIdAndUserIds.split(",");
        for (String s : fileIdAndUserIdArray) {
            String[] fileIdAndUserId = s.split("_");
            fileInfoService.delFileBatch(fileIdAndUserId[0],fileIdAndUserId[1],true);
        }

        return getSuccessResponseVO(null);


    }
}
