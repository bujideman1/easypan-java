package com.easypan.controller;

import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.DownloadFileDto;
import com.easypan.entity.enums.FileCategoryEnums;
import com.easypan.entity.enums.FileDelFlagEnums;
import com.easypan.entity.enums.FileFolderTypeEnums;
import com.easypan.entity.enums.ResponseCodeEnum;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.exception.BusinessException;
import com.easypan.service.FileInfoService;
import com.easypan.utils.StringTools;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class CommonFileController extends ABaseController{
    @Resource
    private AppConfig appConfig;
    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private RedisComponent redisComponent;
    protected void getImage(HttpServletResponse response,String imageFolder,String imageName){
        if(StringTools.isEmpty(imageFolder)|| StringUtils.isBlank(imageFolder)){
            return;
        }
        String imageSuffix=StringTools.getFileSuffix(imageName);
        String filePath=appConfig.getProjectFolder()+ Constants.FILE_FOLDER_FILE+imageFolder+"/"+imageName;
        imageSuffix=imageSuffix.replace(".","");
        String contentType="image/"+imageSuffix;
        response.setContentType(contentType);
        response.setHeader("Cache-Control","max-age=2592000");
        readFile(response,filePath);
    }

    /**
     * 返回文件流，判断文件是否为视频，则获取m3u8，ts结尾则视频返回分片文件
     * @param response
     * @param fileId
     * @param userId
     */
    protected void getFile(HttpServletResponse response,String fileId,String userId){
        FileInfo fileInfo=null;
        String filePath=null;
        if(fileId.endsWith(".ts")){
            String readFileId = fileId.split("_")[0];
            fileInfo=fileInfoService.getFileInfoByFileIdAndUserId(readFileId,userId);
            if(fileInfo==null){
                return;
            }
            String fileNameNoSuffix = StringTools.getFileNameNoSuffix(fileInfo.getFilePath());
            filePath=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE+"/"+fileNameNoSuffix+"/"+fileId;
        }else{
            fileInfo=fileInfoService.getFileInfoByFileIdAndUserId(fileId,userId);
            if(null==fileInfo){
                return;
            }
            if(FileCategoryEnums.VIDEO.getCategory().equals(fileInfo.getFileCategory())){
                //返回m3u8索引文件
                String fileNameNoSuffix = StringTools.getFileNameNoSuffix(fileInfo.getFilePath());
                filePath=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE+"/"+fileNameNoSuffix+"/"+Constants.M3U8_NAME;
            }
            else{
                filePath=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE+fileInfo.getFilePath();
            }
            File file = new File(filePath);
            if(!file.exists()){
                return;
            }
        }
        readFile(response,filePath);
    }

    protected ResponseVO getFolderInfo(String path, String userId) {
        String[] pathArray = path.split("/");
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(userId)
                .setDelFlag(FileDelFlagEnums.USING.getFlag())
                .setFolderType(FileFolderTypeEnums.FOLDER.getType())
                .setFileIdArray(Arrays.asList(pathArray));
        String orderBy="field(file_id,"+"\""+StringUtils.join(pathArray,"\",\"")+"\")";
        query.setOrderByAsc(orderBy);
        List<FileInfo> list=fileInfoService.list(query);
        return getSuccessResponseVO(list);
    }

    protected ResponseVO createDownloadUrl(String fileId, String userId) {
        FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId, userId);
        if(fileInfo==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if(fileInfo.getFolderType().equals(FileFolderTypeEnums.FOLDER.getType())){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String code=StringTools.getRandomString(Constants.LENGTH_30);
        DownloadFileDto downloadFileDto = new DownloadFileDto();
        downloadFileDto.setFileName(fileInfo.getFileName());
        downloadFileDto.setFilePath(fileInfo.getFilePath());
        downloadFileDto.setDownloadCode(code);
        redisComponent.saveDownloadCode(downloadFileDto);
        return getSuccessResponseVO(code);
    }

    protected void download(HttpServletRequest request, HttpServletResponse response, String code){
        DownloadFileDto downloadFileDto=redisComponent.getDownloadCode(code);
        if(null==downloadFileDto){
            return;
        }
        try {
            String filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + downloadFileDto.getFilePath();
            String fileName = downloadFileDto.getFileName();

            response.setContentType("application/octet-stream; charset=UTF-8");
            if (request.getHeader("User-Agent").toLowerCase().contains("msie")) {
                // ie游览器
                fileName = URLEncoder.encode(fileName, "UTF-8");
            } else {
                fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            }
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            readFile(response, filePath);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
