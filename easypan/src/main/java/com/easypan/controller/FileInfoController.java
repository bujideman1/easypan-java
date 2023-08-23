package com.easypan.controller;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.UploadResultDto;
import com.easypan.entity.enums.FileCategoryEnums;
import com.easypan.entity.enums.FileDelFlagEnums;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.service.FileInfoService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileInfoController extends CommonFileController {
    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private AppConfig appConfig;

    /**
     * 根据条件分页查询
     * @param session
     * @param query
     * @param category
     * @return
     */
    @RequestMapping("/loadDataList")
    @GlobalInterceptor
    public ResponseVO loadDataList(HttpSession session, FileInfoQuery query, String category ){
        FileCategoryEnums categoryEnum=FileCategoryEnums.getByCode(category);
        if(categoryEnum!=null){
            query.setFileCategory(categoryEnum.getCategory());
        }
        query.setUserId(getUserInfoFromSession(session).getUserId());
        query.setDelFlag(FileDelFlagEnums.USING.getFlag());
        PaginationResultVO result = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(convert2PaginationVO(result, FileInfoVO.class));
    }

    /**
     * 上传文件
     * @param session
     * @param fileId:文件id
     * @param file
     * @param fileName
     * @param filePid
     * @param fileMd5
     * @param chunkIndex
     * @param chunks
     * @return
     */
    @RequestMapping("/uploadFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO uploadFile(HttpSession session,
                                 String fileId,
                                 MultipartFile file,
                                 @VerifyParam(required = true)String fileName,
                                 @VerifyParam(required = true)String filePid,
                                 @VerifyParam(required = true)String fileMd5,
                                 @VerifyParam(required = true)Integer chunkIndex,
                                 @VerifyParam(required = true)Integer chunks
                                ){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        UploadResultDto resultDto=fileInfoService.uploadFile(webUserDto,fileId,file,fileName,filePid,fileMd5,chunkIndex,chunks);
        return getSuccessResponseVO(resultDto);
    }
    @RequestMapping("/getImage/{imageFolder}/{imageName}")
    public void getImage(HttpServletResponse response,@PathVariable("imageFolder") String imageFolder,
                         @PathVariable("imageName")String imageName
    ) {
        super.getImage(response,imageFolder,imageName);
    }
    @GlobalInterceptor
    @RequestMapping("/ts/getVideoInfo/{fileId}")
    public void getVideo(HttpSession session,HttpServletResponse response,@PathVariable("fileId") String fileId
    ) {
        SessionWebUserDto webUserDto=getUserInfoFromSession(session);
        super.getFile(response,fileId,webUserDto.getUserId());
    }
    @GlobalInterceptor()
    @RequestMapping("/getFile/{fileId}")
    public void getFile(HttpSession session,HttpServletResponse response,@PathVariable("fileId") String fileId
    ) {
        SessionWebUserDto webUserDto=getUserInfoFromSession(session);
        super.getFile(response,fileId,webUserDto.getUserId());
    }
    @GlobalInterceptor()
    @RequestMapping("/newFoloder")
    public ResponseVO newFolder(HttpSession session,@VerifyParam(required = true) String filePid,
                          @VerifyParam(required = true) String fileName
    ) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        FileInfo fileInfo=fileInfoService.newFolder(filePid,webUserDto.getUserId(),fileName);
        return getSuccessResponseVO(fileInfo);
    }

    /**
     * 获取文件路径信息
     * @param session
     * @param path
     * @param fileName
     * @return
     */
    @GlobalInterceptor()
    @RequestMapping("/getFolderInfo")
    public ResponseVO getFolderInfo(HttpSession session,@VerifyParam(required = true) String path,
                                @VerifyParam(required = true) String fileName
    ) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        ResponseVO responseVO=super.getFolderInfo(path,webUserDto.getUserId());
        return responseVO;
    }
}
