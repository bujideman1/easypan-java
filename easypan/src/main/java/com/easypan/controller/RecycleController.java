package com.easypan.controller;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.entity.enums.FileDelFlagEnums;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.service.FileInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/recycle")
public class RecycleController extends ABaseController {
    @Resource
    private FileInfoService fileInfoService;
    @RequestMapping("/loadRecycleList")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO loadRecycleList(HttpSession session, FileInfoQuery query ){
        query.setUserId(getUserInfoFromSession(session).getUserId());
        query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
        query.setOrderByDesc("recovery_time");
        PaginationResultVO result = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(convert2PaginationVO(result, FileInfoVO.class));
    }
    @RequestMapping("/recoverFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO recoverFile(HttpSession session, String  fileIds ){
        String userId = getUserInfoFromSession(session).getUserId();
        fileInfoService.recoverFileBatch(userId,fileIds);
        return getSuccessResponseVO(null);
    }
}
