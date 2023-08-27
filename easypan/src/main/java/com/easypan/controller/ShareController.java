package com.easypan.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.enums.FileCategoryEnums;
import com.easypan.entity.enums.FileDelFlagEnums;
import com.easypan.entity.po.FileShare;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.query.FileShareQuery;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.service.FileShareService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/share")
public class ShareController extends ABaseController{
    @Resource
    private FileShareService fileShareService;
    @RequestMapping("/loadShareList")
    @GlobalInterceptor
    public ResponseVO loadShareList(HttpSession session, FileShareQuery query){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        PaginationResultVO resultVO= fileShareService.loadShareList(webUserDto.getUserId(),query);
        return getSuccessResponseVO(resultVO);
    }
    @RequestMapping("/shareFile")
    @GlobalInterceptor
    public ResponseVO loadShareList(HttpSession session,
                                    @VerifyParam(required = true) String fileId,
                                    @VerifyParam(required = true)Integer validType,
                                    String code){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        FileShare fileShare=fileShareService.saveShare(webUserDto.getUserId(),fileId,validType,code);
        return getSuccessResponseVO(fileShare);
    }
    @RequestMapping("/cancelShare")
    @GlobalInterceptor
    public ResponseVO cancelShare(HttpSession session,@VerifyParam(required = true) String shareIds){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        String[] shareIdArray = shareIds.split(",");
        fileShareService.delShareBatch(shareIdArray,webUserDto.getUserId());
        return getSuccessResponseVO(null);
    }
}
