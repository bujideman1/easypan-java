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
import java.io.IOException;
import java.io.RandomAccessFile;
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
            RandomAccessFile raf = new RandomAccessFile(filePath, "r");
            long fileSize = raf.length();
            // 获取Range请求头
            String rangeHeader = request.getHeader("Range");

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] rangeData = rangeHeader.substring(6).split("-");
                long startByte = Long.parseLong(rangeData[0]);
                long endByte = rangeData.length > 1 ? Long.parseLong(rangeData[1]) : fileSize - 1;

                if (startByte < 0 || startByte >= fileSize || endByte >= fileSize) {
                    response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    response.setHeader("Content-Range", "bytes */" + fileSize);
                    return;            }

                long contentLength = endByte - startByte + 1;
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + fileSize);
                response.setContentLengthLong(contentLength);

                byte[] buffer = new byte[1024];
                int bytesRead;
                long bytesRemaining = contentLength;
                raf.seek(startByte);

                while (bytesRemaining > 0) {
                    bytesRead = raf.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining));
                    if (bytesRead == -1) {
                        break;
                    }
                    response.getOutputStream().write(buffer, 0, bytesRead);
                    bytesRemaining -= bytesRead;
                }
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentLengthLong(fileSize);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = raf.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, bytesRead);
                }
            }
            raf.close();
        }catch (IOException e){
            throw new BusinessException("用户暂停下载");
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
