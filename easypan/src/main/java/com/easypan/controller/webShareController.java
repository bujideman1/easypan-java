package com.easypan.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.component.RedisComponent;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionShareDto;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.enums.FileDelFlagEnums;
import com.easypan.entity.enums.ResponseCodeEnum;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.po.FileShare;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.entity.vo.ShareInfoVO;
import com.easypan.exception.BusinessException;
import com.easypan.service.FileInfoService;
import com.easypan.service.FileShareService;
import com.easypan.service.UserInfoService;
import com.easypan.utils.CopyTools;
import jdk.nashorn.internal.runtime.ConsString;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;

@RestController
@RequestMapping("/showShare")
public class webShareController extends CommonFileController {
    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private FileShareService fileShareService;
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private RedisComponent redisComponent;
    @RequestMapping("/getShareInfo")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO getShareInfo(@VerifyParam(required = true)String shareId){
        ShareInfoVO infoCommon = getShareInfoCommon(shareId);
        return getSuccessResponseVO(infoCommon);
    }
    @RequestMapping("/checkShareCode")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO checkShareCode(HttpSession session,@VerifyParam(required = true)String shareId,
                                     @VerifyParam(required = true)String code){
        SessionShareDto shareDto=fileShareService.checkShareCode(shareId,code);
        session.setAttribute(Constants.SESSION_SHARE_KEY+shareId,shareDto);

        return getSuccessResponseVO(shareDto);
    }
    @RequestMapping("/getShareLoginInfo")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO getShareLoginInfo(HttpSession session, @VerifyParam(required = true)String shareId){
        SessionShareDto sessionShareDto = getSessionShareFromSession(session, shareId);
        if(sessionShareDto==null){
            return getSuccessResponseVO(null);
        }
        ShareInfoVO infoCommon = getShareInfoCommon(shareId);
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        if(userDto!=null&&userDto.getUserId().equals(sessionShareDto.getShareUserId())){
            infoCommon.setCurrentUser(true);
        }else{
            infoCommon.setCurrentUser(false);
        }
        return getSuccessResponseVO(infoCommon);
    }
    @RequestMapping("/loadFileList")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO loadFileList(HttpSession session,
                                   @VerifyParam(required = true)String shareId,
                                   String filePid){
        SessionShareDto shareDto=checkShare(session,shareId);
        FileInfoQuery query = new FileInfoQuery();
        if(StringUtils.isNotEmpty(filePid)&&!Constants.ZERO_STR.equals(filePid)){
          //todo 判断是否是分析的父id  fileInfoService.checkFootFilePid(shareDto.getFileId(),shareDto.getShareUserId(),filePid);
            query.setFilePid(filePid);
        }else{
            query.setFileId(shareDto.getFileId());
        }
        query.setUserId(shareDto.getShareUserId())
                .setDelFlag(FileDelFlagEnums.USING.getFlag())
                .setOrderByDesc("last_update_time");
        PaginationResultVO paginationResultVO = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(paginationResultVO);
    }
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    @RequestMapping("/getFolderInfo")
    public ResponseVO getFolderInfo(
            HttpSession session,
            @VerifyParam(required = true) String path,
            @VerifyParam(required = true) String shareId
    ) {
        SessionShareDto shareFromSession = getSessionShareFromSession(session, shareId);
        if(shareFromSession==null){
            throw new BusinessException(ResponseCodeEnum.CODE_903);
        }
        ResponseVO responseVO=super.getFolderInfo(path,shareFromSession.getShareUserId());
        return responseVO;
    }
    @RequestMapping("/getFile/{shareId}/{fileId}")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public void getFile(HttpSession session,HttpServletResponse response,
                        @PathVariable("shareId")String shareId,
                        @PathVariable("fileId")String fileId){
        SessionShareDto shareFromSession = getSessionShareFromSession(session, shareId);
        if(shareFromSession==null){
            throw new BusinessException(ResponseCodeEnum.CODE_903);
        }
        super.getFile(response,fileId,shareFromSession.getShareUserId());
    }
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    @RequestMapping("/createDownloadUrl/{shareId}/{fileId}")
    public ResponseVO createDownloadUrl(HttpSession session,
                                        @VerifyParam(required = true)@PathVariable("shareId") String shareId,
                                        @VerifyParam(required = true)@PathVariable("fileId") String fileId
    ) {
        SessionShareDto sessionShareDto = getSessionShareFromSession(session, shareId);
        return  super.createDownloadUrl(fileId,sessionShareDto.getShareUserId());

    }
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    @RequestMapping("/download/{code}")
    public void createDownloadUrl(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @VerifyParam(required = true)@PathVariable("code") String code
    ) {
        super.download(request,response,code);

    }
    private SessionShareDto checkShare(HttpSession session, String shareId) {
        SessionShareDto shareDto = getSessionShareFromSession(session, shareId);
        if(null==shareDto){
            throw new BusinessException(ResponseCodeEnum.CODE_903);
        }
        if(null==shareDto||(shareDto.getExpireTime()!=null&&(new Date()).after(shareDto.getExpireTime()))){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        return shareDto;


    }

    private ShareInfoVO getShareInfoCommon(String shareId){
        FileShare fileShare = fileShareService.getById(shareId);
        if(null==fileShare||(fileShare.getExpireTime()!=null&&(new Date()).after(fileShare.getExpireTime()))){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        ShareInfoVO shareInfoVO = CopyTools.copy(fileShare, ShareInfoVO.class);
        FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileShare.getFileId(), fileShare.getUserId());
        if(fileInfo==null|| !FileDelFlagEnums.USING.getFlag().equals(fileInfo.getDelFlag())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        shareInfoVO.setFileName(fileInfo.getFileName());
        UserInfo userInfo = userInfoService.getById(fileShare.getUserId());
        shareInfoVO.setNickName(userInfo.getNickName());
        shareInfoVO.setAvatar(userInfo.getQqAvatar());
        shareInfoVO.setUserId(userInfo.getUserId());
        return shareInfoVO;
    }
}
