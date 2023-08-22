package com.easypan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.UploadResultDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.entity.enums.*;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.query.SimplePage;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.exception.BusinessException;
import com.easypan.mapper.FileInfoMapper;
import com.easypan.mapper.UserInfoMapper;
import com.easypan.service.FileInfoService;
import com.easypan.utils.DateUtils;
import com.easypan.utils.ProcessUtils;
import com.easypan.utils.ScaleFilter;
import com.easypan.utils.StringTools;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
* @author 53082
* @description 针对表【file_info(文件信息表)】的数据库操作Service实现
* @createDate 2023-08-16 22:00:16
*/
@Service
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo>
    implements FileInfoService{
    private static final Logger logger= LoggerFactory.getLogger(FileInfoServiceImpl.class);
    @Resource
    private AppConfig appConfig;
    @Resource
    public FileInfoMapper fileInfoMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    @Lazy
    private FileInfoServiceImpl fileInfoService;
@Resource
public RedisComponent redisComponent;
    @Override
    public PaginationResultVO findListByPage(FileInfoQuery param) {
        IPage<FileInfo> page=new Page<>();
//        int count = this.findCountByParam(param);
//        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
//        page.setCurrent(param.getPageNo()==null?0:param.getPageNo());
//        page.setSize(pageSize);
        page.setCurrent(param.getPageNo());
        page.setSize(param.getPageSize());
        QueryWrapper<FileInfo> wrapper= getWrapperByParam(param);
        fileInfoMapper.selectPage(page, wrapper);
        return (PaginationResultVO<FileInfo>) new PaginationResultVO((int)page.getTotal(), (int)page.getSize(), (int)page.getCurrent(), (int)page.getPages(), page.getRecords());
    }

    private QueryWrapper<FileInfo> getWrapperByParam(FileInfoQuery param) {
        QueryWrapper<FileInfo> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(StringUtils.isNotEmpty(param.getFileId()),FileInfo::getFileId,param.getFileId())
                .eq(StringUtils.isNotEmpty(param.getUserId()), FileInfo::getUserId, param.getUserId())
                .eq(StringUtils.isNotEmpty(param.getFileMd5()), FileInfo::getFileMd5, param.getFileMd5())
                .eq(StringUtils.isNotEmpty(param.getFilePid()), FileInfo::getFilePid, param.getFilePid())
                .eq(Objects.nonNull(param.getFileSize()), FileInfo::getFileSize, param.getFileSize())
                .eq(StringUtils.isNotEmpty(param.getFileName()), FileInfo::getFileName, param.getFileName())
                .eq(StringUtils.isNotEmpty(param.getFileCover()), FileInfo::getFileCover, param.getFileCover())
                .eq(StringUtils.isNotEmpty(param.getFilePath()), FileInfo::getFilePath, param.getFilePath())
                .apply(StringUtils.isNotEmpty(param.getCreateTime()), "DATE_FORMAT(create_time, '%Y-%m-%d') = '" + param.getCreateTime() + "'")
                .apply(StringUtils.isNotEmpty(param.getLastUpdateTime()), "DATE_FORMAT(last_update_time, '%Y-%m-%d') = '" + param.getLastUpdateTime() + "'")
                .eq(param.getFolderType() != null, FileInfo::getFolderType, param.getFolderType())
                .eq(param.getFileCategory() != null, FileInfo::getFileCategory, param.getFileCategory())
                .eq(param.getFileType() != null, FileInfo::getFileType, param.getFileType())
                .eq(param.getStatus() != null, FileInfo::getStatus, param.getStatus())
                .apply(StringUtils.isNotEmpty(param.getRecoveryTime()), "DATE_FORMAT(recovery_time, '%Y-%m-%d') = '" + param.getRecoveryTime() + "'")
                .eq(param.getDelFlag() != null, FileInfo::getDelFlag, param.getDelFlag())
                .in(Objects.nonNull(param.getFileIdArray()) && param.getFileIdArray().length > 0, FileInfo::getFileId, param.getFileIdArray())
                .in(ArrayUtils.isNotEmpty(param.getFilePidArray()), FileInfo::getFilePid, param.getFilePidArray())
                .notIn(ArrayUtils.isNotEmpty(param.getExcludeFileIdArray()), FileInfo::getFileId, param.getExcludeFileIdArray());
        return wrapper;
    }
    private QueryWrapper<FileInfo> getListWrapperByParam(FileInfoQuery param){
        QueryWrapper<FileInfo> wrapper = getWrapperByParam(param);
        if(StringUtils.isNotEmpty(param.getOrderBy())){
            wrapper.orderByAsc(param.getOrderBy());
        }
        if (param.getSimplePage() != null) {
            wrapper.last("LIMIT " + param.getSimplePage().getStart() + "," + param.getSimplePage().getEnd());
        }
        return wrapper;
    }
    @Override
    public Integer findCountByParam(FileInfoQuery param) {

        return null;
    }

    @Override
    public List<FileInfo> findListByParam(FileInfoQuery param) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResultDto uploadFile(SessionWebUserDto webUserDto, String fileId, MultipartFile file, String fileName, String filePid,String fileMd5, Integer chunkIndex, Integer chunks) {
        UploadResultDto resultDto = new UploadResultDto();
        Boolean successFlag=true;
        File tempFileFolder=null;
        try {
            if(StringTools.isEmpty(fileId)){
                fileId=StringTools.getRandomString(Constants.LENGTH_10);
            }
            Date curDate=new Date();
            resultDto.setFileId(fileId);
            UserSpaceDto userSpaceUse = redisComponent.getUserSpaceUse(webUserDto.getUserId());
            if(chunkIndex==0){
                FileInfoQuery query = new FileInfoQuery();
                query.setFileMd5(fileMd5);
                query.setSimplePage(new SimplePage(0,1));
                query.setStatus(FileStatusEnums.USING.getStatus());
                List<FileInfo> fileInfoList = fileInfoMapper.selectList(getListWrapperByParam(query));
//            秒传
                if(!fileInfoList.isEmpty()){
                    FileInfo dbFile = fileInfoList.get(0);
                    if (dbFile.getFileSize()+userSpaceUse.getUseSpace()>userSpaceUse.getTotalSpace()){
                        throw new BusinessException(ResponseCodeEnum.CODE_904);
                    }
                    dbFile.setFileId(fileId);
                    dbFile.setFilePid(filePid);
                    dbFile.setUserId(webUserDto.getUserId());
                    dbFile.setCreateTime(curDate);
                    dbFile.setLastUpdateTime(curDate);
                    dbFile.setStatus(FileStatusEnums.USING.getStatus());
                    dbFile.setDelFlag(FileDelFlagEnums.USING.getFlag());
                    dbFile.setFileMd5(fileMd5);
//              文件重命名
                    fileName=autoRename(filePid, webUserDto.getUserId(), fileName);
                    dbFile.setFileName(fileName);
                    fileInfoMapper.insert(dbFile);
                    resultDto.setStatus(UploadStatusEnums.UPLOAD_SECONDS.getCode());
                    //更新用户使用空间
                    updateUserSpace(webUserDto,dbFile.getFileSize());
                    return resultDto;
                }
            }
            //分片上传
            //判断用户网盘空间
            Long tempSize = redisComponent.getFileTempSize(webUserDto.getUserId(), fileId);
            if(file.getSize()+tempSize+userSpaceUse.getUseSpace()>userSpaceUse.getTotalSpace()){
                throw new BusinessException(ResponseCodeEnum.CODE_904);
            }
            String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
            String currentUserFolderName=webUserDto.getUserId()+fileId;
            tempFileFolder=new File(tempFolderName,currentUserFolderName);
            if(!tempFileFolder.exists()){
                tempFileFolder.mkdirs();
            }
            File newFile=new File(tempFileFolder.getPath()+"/"+chunkIndex);
            file.transferTo(newFile);
            if(chunkIndex<chunks-1){
                resultDto.setStatus(UploadStatusEnums.UPLOADING.getCode());
                //保存临时大小
                redisComponent.saveFileTempSize(webUserDto.getUserId(), fileId,file.getSize());
                return resultDto;
            }
            //如果只有一个分片，也需要保存临时大小
            redisComponent.saveFileTempSize(webUserDto.getUserId(), fileId,file.getSize());
            //传到最后一片时合并分片文件
            String month= DateUtils.format(new Date(),DateTimePatternEnum.YYYYMM.getPattern());
            String suffix=StringTools.getFileSuffix(fileName);
            //真实文件名
            String realFileName=currentUserFolderName+suffix;
            FileTypeEnums fileTypeEnums = FileTypeEnums.getFileTypeBySuffix(suffix);
            //重命名
            fileName=autoRename(filePid, webUserDto.getUserId(), fileName);
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileId(fileId);
            fileInfo.setFilePid(filePid);
            fileInfo.setUserId(webUserDto.getUserId());
            fileInfo.setCreateTime(curDate);
            fileInfo.setLastUpdateTime(curDate);
            fileInfo.setFileMd5(fileMd5);
            fileInfo.setFileCategory(fileTypeEnums.getCategory().getCategory());
            fileInfo.setFilePath(month+"/"+realFileName);
            fileInfo.setFileType(fileTypeEnums.getType());
            fileInfo.setFolderType(FileFolderTypeEnums.FILE.getType());
            fileInfo.setStatus(FileStatusEnums.TRANSFER.getStatus());
            fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
            fileInfo.setFileName(fileName);
            fileInfoMapper.insert(fileInfo);
            //todo 合并文件，转码
            Long totalSize=redisComponent.getFileTempSize(webUserDto.getUserId(),fileId);
            //更新数据库里用户已使用空间
            updateUserSpace(webUserDto,totalSize);
            resultDto.setStatus(UploadStatusEnums.UPLOAD_FINISH.getCode());
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    fileInfoService.transferFile(fileInfo.getFileId(),webUserDto);
                }
            });

            return resultDto;
        }catch (BusinessException e){
            logger.error("文件上传失败",e);
            successFlag=false;
            throw e;
        }
        catch (Exception e){
            successFlag = false;
            logger.error("文件上传失败", e);
            throw new BusinessException("文件上传失败");
        }
        finally {
            if(!successFlag&&tempFileFolder!=null){
                try {
                    FileUtils.deleteDirectory(tempFileFolder);
                } catch (IOException e) {
                    logger.error("删除临时目录失败",e);
                }
            }
        }
    }

    @Override
    public FileInfo getFileInfoByFileIdAndUserId(String fileId, String userId) {
        return fileInfoMapper.selectByFileIdAndUserId(fileId,userId);
    }

    /**
     * 创建新文件夹
     * @param filePid
     * @param userId
     * @param fileName
     * @return
     */
    @Override
    public FileInfo newFolder(String filePid, String userId, String fileName) {
        checkFileName(filePid,userId,fileName,FileFolderTypeEnums.FOLDER.getType());
        Date curDate=new Date();
        FileInfo fileInfo = new FileInfo();
        fileInfo.setUserId(userId);
        fileInfo.setFileId(StringTools.getRandomString(Constants.LENGTH_10));
        fileInfo.setFolderType(FileFolderTypeEnums.FOLDER.getType());
        fileInfo.setFilePid(filePid);
        fileInfo.setFileName(fileName);
        fileInfo.setStatus(FileStatusEnums.USING.getStatus());
        fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
        fileInfo.setCreateTime(curDate);
        fileInfo.setLastUpdateTime(curDate);
        fileInfoMapper.insert(fileInfo);
        return fileInfo;
    }

    @Override
    public List<FileInfo> list(FileInfoQuery query) {
        return list(getListWrapperByParam(query));
    }

    /**
     * 检查文件夹是否有重名文件
     * @param filePid
     * @param userId
     * @param fileName
     * @param fileFolderType
     */
    private void checkFileName(String filePid, String userId, String fileName, Integer fileFolderType) {
        FileInfoQuery query=new FileInfoQuery();
        query.setFilePid(filePid).setUserId(userId).setFileName(fileName).setFolderType(fileFolderType).setDelFlag(FileDelFlagEnums.USING.getFlag());
        Long count = fileInfoMapper.selectCount(getWrapperByParam(query));
        if(count>0){
            throw new BusinessException("此目录存在同名文件，请修改名称");
        }
    }

    /**
     * 查询数据库是否已经有重名文件
     * @param filePid
     * @param userId
     * @param fileName
     * @return
     */
    private String autoRename(String filePid,String userId,String fileName){
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(userId);
        query.setFilePid(filePid);
        query.setDelFlag(FileDelFlagEnums.USING.getFlag());
        query.setFileName(fileName);
        Long count = fileInfoMapper.selectCount(getWrapperByParam(query));
        if (count>0){
            fileName=StringTools.rename(fileName);
        }
        return fileName;
    }
    private void updateUserSpace(SessionWebUserDto webUserDto,Long useSpace){
        Integer i = userInfoMapper.updateUserSpace(webUserDto.getUserId(), useSpace, null);
        if(i==0){
            throw new BusinessException(ResponseCodeEnum.CODE_904);
        }
        UserSpaceDto userSpaceUse = redisComponent.getUserSpaceUse(webUserDto.getUserId());
        userSpaceUse.setUseSpace(userSpaceUse.getUseSpace()+useSpace);
        redisComponent.saveUserSpaceUse(webUserDto.getUserId(), userSpaceUse);
    }
    @Async
    public void transferFile(String fileId,SessionWebUserDto webUserDto){
        Boolean transferSuccess=true;
        String targetFilePath=null;
        String cover=null;
        FileTypeEnums fileTypeEnums=null;
        FileInfo fileInfo=fileInfoMapper.selectByFileIdAndUserId(fileId,webUserDto.getUserId());
        try {
            if(fileInfo==null||!FileStatusEnums.TRANSFER.getStatus().equals(fileInfo.getStatus())){
                    return;
            }
            //临时目录
            String tempFolderName=appConfig.getProjectFolder()+Constants.FILE_FOLDER_TEMP;
            String currentUserFolderName=webUserDto.getUserId()+fileId;
            File fileFolder = new File(tempFolderName + currentUserFolderName);
            String fileSuffix=StringTools.getFileSuffix(fileInfo.getFileName());
            String month=DateUtils.format(fileInfo.getCreateTime(),DateTimePatternEnum.YYYYMM.getPattern());
            //目标目录
            String targetFolderName=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE;
            File targetFolder = new File(targetFolderName + "/" + month);
            if(!targetFolder.exists()){
                targetFolder.mkdirs();
            }
            //真实的文件名
            String realFileName = currentUserFolderName + fileSuffix;
            targetFilePath = targetFolder.getPath() + "/" + realFileName;
            //合并文件
            union(fileFolder.getPath(),targetFilePath,fileInfo.getFileName(),true);
            //todo 视频文件切割
             fileTypeEnums = FileTypeEnums.getFileTypeBySuffix(fileSuffix);
             if(fileTypeEnums== FileTypeEnums.VIDEO){
                cutFile4Video(fileId,targetFilePath);
                //生成缩略图
                 cover=month+"/"+currentUserFolderName+Constants.IMAGE_PNG_SUFFIX;
                 String coverPath=targetFolderName+"/"+cover;
                 ScaleFilter.createCover4Video(new File(targetFilePath),Constants.LENGTH_150,new File(coverPath));
             }else if(fileTypeEnums== FileTypeEnums.IMAGE){
                //生成缩略图
                 cover=month+"/"+realFileName.replace(".","_.");
                 String coverPath=targetFolderName+"/"+cover;
                 Boolean created=ScaleFilter.createThumbnailWidthFFmpeg(new File(targetFilePath),Constants.LENGTH_150,new File(coverPath),false);
                 if(!created){
                     FileUtils.copyFile(new File(targetFilePath),new File(coverPath));
                 }
             }
        }
        catch (Exception e){
            logger.error("文件转码失败;文件id{};用户id",fileId,webUserDto.getUserId(),e);
            transferSuccess=false;
        }
        finally {
            FileInfo updateFileInfo = new FileInfo();
            updateFileInfo.setFileSize(new File(targetFilePath).length());
            updateFileInfo.setFileCover(cover);
            updateFileInfo.setStatus(transferSuccess?FileStatusEnums.USING.getStatus()  : FileStatusEnums.TRANSFER_FAIL.getStatus());
            FileInfoQuery query = new FileInfoQuery();
            query.setFileId(fileId);
            query.setUserId(webUserDto.getUserId());
            query.setStatus(FileStatusEnums.TRANSFER.getStatus());
            fileInfoMapper.update(updateFileInfo,getWrapperByParam(query));
        }
    }
    private void union(String dirPath,String toFilePath,String fileName,Boolean delSource){
        File dir = new File(dirPath);
        if(!dir.exists()){
            logger.error("合并文件失败,目录不存在");
            throw new BusinessException("目录不存在");
        }
        File[] files = dir.listFiles();
        File targetFile = new File(toFilePath);
        RandomAccessFile writeFile=null;
        try {
            writeFile=new RandomAccessFile(targetFile,"rw");
            byte[] buff = new byte[1024 * 10];
            for (int i = 0; i < files.length; i++) {
                int len=-1;
                File chunkFile=new File(dirPath+"/"+i);
                RandomAccessFile readFile=null;
                try {
                    readFile=new RandomAccessFile(chunkFile,"r");
                    while ((len=readFile.read(buff))!=-1){
                        writeFile.write(buff,0,len);
                    }
                }catch (Exception e){
                    logger.error("合并文件分片失败",e);
                    throw new BusinessException("文件合并失败");
                }
                finally {
                    readFile.close();
                }
            }
        }
        catch (Exception e){
            logger.error("合并文件：{} 失败",fileName,e);
            throw new BusinessException("合并文件"+fileName+"失败！");
        }
        finally {
            if(null!=writeFile){
                try {
                    writeFile.close();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            if(delSource&&dir.exists()){
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    private void cutFile4Video(String fileId,String videoFilePath){
        //创建同名切片目录
        File tsFolder = new File(videoFilePath.substring(0, videoFilePath.lastIndexOf(".")));
        if(!tsFolder.exists()){
            tsFolder.mkdirs();
        }
        final String CMD_TRANSFER_2TS = "ffmpeg -y -i %s  -vcodec copy -acodec copy -vbsf h264_mp4toannexb %s";
        final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";
        String tsPath=tsFolder+"/"+Constants.TS_NAME;
        //生成ts
        String cmd=String.format(CMD_TRANSFER_2TS,videoFilePath,tsPath);
        ProcessUtils.executeCommand(cmd,false);
        //生成索引文件.m3u8和切片.ts
        cmd=String.format(CMD_CUT_TS,tsPath,tsFolder.getPath()+"/"+Constants.M3U8_NAME,tsFolder.getPath(),fileId);
        ProcessUtils.executeCommand(cmd,false);
        //删除index.ts
        new File(tsPath).delete();
    }
}




