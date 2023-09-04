package com.easypan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.injector.methods.DeleteBatchByIds;
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
import com.easypan.entity.po.UserInfo;
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
import java.util.*;
import java.util.stream.Collectors;

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
        page.setCurrent(param.getPageNo());
        page.setSize(param.getPageSize());
        QueryWrapper<FileInfo> wrapper= getWrapperByParam(param);
        fileInfoMapper.selectPage(page, wrapper);
        return (PaginationResultVO<FileInfo>) new PaginationResultVO(page.getTotal(), page.getSize(), page.getCurrent(),page.getPages(), page.getRecords());
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
                .in(Objects.nonNull(param.getFileIdArray()) && param.getFileIdArray().size() > 0, FileInfo::getFileId, param.getFileIdArray())
                .in(Objects.nonNull(param.getFilePidArray()), FileInfo::getFilePid, param.getFilePidArray())
                .notIn(ArrayUtils.isNotEmpty(param.getExcludeFileIdArray()), FileInfo::getFileId, param.getExcludeFileIdArray());
        wrapper.orderByAsc(StringUtils.isNotEmpty(param.getOrderByAsc()),param.getOrderByAsc())
                .orderByDesc(StringUtils.isNotEmpty(param.getOrderByDesc()),param.getOrderByDesc());
        return wrapper;
    }
    private QueryWrapper<FileInfo> getListWrapperByParam(FileInfoQuery param){
        QueryWrapper<FileInfo> wrapper = getWrapperByParam(param);
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
            // 合并文件，转码
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileInfo rename(String fileId, String userId, String fileName) {
        FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
        if(null==fileInfo){
            throw new BusinessException("文件不存在");
        }
        String filePid=fileInfo.getFilePid();
       checkFileName(filePid,userId,fileName,fileInfo.getFolderType());
        //获取文件后缀，前端默认不能更改文件后缀
        if(fileInfo.getFolderType().equals(FileFolderTypeEnums.FILE.getType())){
            fileName=fileName+StringTools.getFileSuffix(fileInfo.getFileName());
        }
        Date curDate=new Date();
        fileInfo.setFileName(fileName);
        fileInfo.setLastUpdateTime(curDate);
        updateById(fileInfo);
        //防止并发
        FileInfoQuery query=new FileInfoQuery();
        query.setFilePid(filePid).setUserId(userId).setFileName(fileName).setDelFlag(FileDelFlagEnums.USING.getFlag());
        Long count = fileInfoMapper.selectCount(getWrapperByParam(query));
        if(count>1){
            throw new BusinessException("文件名"+fileName+"已经存在");
        }
        return fileInfo;
    }

    @Override
    public List<FileInfo> loadAllFolder(String filePid, String userId, String currentFileIds) {
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(userId).setFilePid(filePid).setFolderType(FileFolderTypeEnums.FOLDER.getType());
//        if(!StringTools.isEmpty(currentFileIds)){
//            query.setExcludeFileIdArray(currentFileIds.split(","));
//        }
        query.setOrderByDesc("create_time");
        return fileInfoMapper.selectList(getWrapperByParam(query));
    }

    /**
     *
     * @param filePid 要移动到的文件夹id
     * @param userId 用户id
     * @param fileIds 选中的文件列表,以fileId以","间隔
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeFileFolder(String filePid, String userId, String fileIds) {
        //需要先判断移动文件是否合法
        if (fileIds.contains(filePid)) {
            //判断是否移动自己
            throw new BusinessException("不能将文件移动到自身或其子目录下");
        }
        if (!Constants.ZERO_STR.equals(filePid)) {
            FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(filePid, userId);
            if (null == fileInfo || !FileDelFlagEnums.USING.getFlag().equals(fileInfo.getDelFlag())) {
                //移动到的父文件不存在或文件状态异常
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }
        //查询父文件夹文件列表
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(userId).setFilePid(filePid);
        List<FileInfo> fileInfoList = fileInfoMapper.selectList(getWrapperByParam(query));
        Map<String, FileInfo> dbFileList = fileInfoList.stream().collect(Collectors.toMap(FileInfo::getFileName, fileInfo -> fileInfo));
        //查询选中的文件
        String[] fileIdArray = fileIds.split(",");
        query = new FileInfoQuery();
        query.setUserId(userId).setFileIdArray(Arrays.asList(fileIdArray));
        List<FileInfo> selectFileList = fileInfoMapper.selectList(getWrapperByParam(query));
        //判断是否所选文件移动到子目录或自身
        //code
        Set<String> folderAndSubfolders = new HashSet<>();
        for (FileInfo folder : selectFileList) {
            //获取子目录
            folderAndSubfolders.addAll(getAllSubfolders(folder));
        }
        //检查所选文件是否移动到了子目录
        if (folderAndSubfolders.contains(filePid)) {
            throw new BusinessException("不能将文件移动到自身或其子目录下");
        }
        //重命名和设置文件状态
        for (FileInfo select : selectFileList) {
            FileInfo fileInfo = dbFileList.get(select.getFileName());
            FileInfo updateFileInfo = new FileInfo();
            if (fileInfo != null && fileInfo.getFolderType().equals(select.getFolderType())) {
                updateFileInfo.setFileName(StringTools.rename(select.getFileName()));
            }
            updateFileInfo.setFilePid(filePid);
            updateFileInfo.setUserId(userId);
            updateFileInfo.setFileId(select.getFileId());
            updateById(updateFileInfo);
        }
    }
    private List<String> getAllSubfolders(FileInfo fileInfo) {
        List<String> subfolders = new ArrayList<>();
        subfolders.add(fileInfo.getFileId());
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(fileInfo.getUserId())
                .setFilePid(fileInfo.getFileId())
                .setDelFlag(FileDelFlagEnums.USING.getFlag())
                .setFolderType(FileFolderTypeEnums.FOLDER.getType());
        List<FileInfo> childFolders = fileInfoMapper.selectList(getWrapperByParam(query));
        for (FileInfo childFolder : childFolders) {
            subfolders.addAll(getAllSubfolders(childFolder));
        }
        return subfolders;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFile2RecycleBatch(String userId, String fileIds) {
        //
        /*
          1.查询待删除文件列表
          2.获得删除文件目录的子目录
          3.将子目录更新状态为回收站状态
          4.将选中文件更新到回收站
         */
        String[] fileIdArray = fileIds.split(",");
        FileInfoQuery query = new FileInfoQuery();
        query.setFileIdArray(Arrays.asList(fileIdArray)).setUserId(userId).setDelFlag(FileDelFlagEnums.USING.getFlag());
        List<FileInfo> selectList = list(getWrapperByParam(query));
        if(selectList.isEmpty()){
            return;
        }
        //获取删除目录子目录
        List<String> delFilePidList = new ArrayList<>();
        for (FileInfo fileInfo : selectList) {
            findAllSubFolderIdList(delFilePidList,userId,fileInfo.getFileId(),FileDelFlagEnums.USING.getFlag());
        }
        //将目录所有文件更新为已删除
        if(!delFilePidList.isEmpty()){
            //选中目录下的子目录和文件都会被标记为删除状态
            FileInfo updateInfo = new FileInfo();
            updateInfo.setDelFlag(FileDelFlagEnums.DEL.getFlag());
            query=new FileInfoQuery();
            query.setUserId(userId).setFilePidArray(delFilePidList).setDelFlag(FileDelFlagEnums.USING.getFlag());
            update(updateInfo,getWrapperByParam(query));
        }
        //将选中文件更新到回收站
        FileInfo updateInfo=new FileInfo();
        updateInfo.setRecoveryTime(new Date());
        updateInfo.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
        query=new FileInfoQuery();
        query.setUserId(userId).setFileIdArray(delFilePidList).setDelFlag(FileDelFlagEnums.USING.getFlag());
        update(updateInfo,getWrapperByParam(query));

    }

    /**
     * 获取文件子目录
     * @param fileSubList
     * @param userId
     * @param filePid
     * @param delFlag
     */
    private void findAllSubFolderIdList(List<String> fileSubList, String userId, String filePid, Integer delFlag) {
        fileSubList.add(filePid);
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(userId).setFilePid(filePid).setDelFlag(delFlag).setFolderType(FileFolderTypeEnums.FOLDER.getType());
        List<FileInfo> list = list(getWrapperByParam(query));
        for (FileInfo fileInfo : list) {
            findAllSubFolderIdList(fileSubList,userId,fileInfo.getFileId(), delFlag);
        }
    }

    /**
     * 将回收站文件复原
     * @param userId ：用户id
     * @param fileIds ：待恢复文件id
     */
    @Override
    public void recoverFileBatch(String userId, String fileIds) {
        /*
         * 1.查询待恢复文件列表
         * 2.获得恢复文件目录的子目录
         * 3.将子目录更新状态为正常状态
         * 4.将选中文件更新到正常状态
         */
        Date curDate=new Date();
        //
        String[] fileIdArray = fileIds.split(",");
        //1.查询待恢复文件列表
        FileInfoQuery query = new FileInfoQuery();
        query.setFileIdArray(Arrays.asList(fileIdArray)).setUserId(userId).setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
        List<FileInfo> selectList = list(getWrapperByParam(query));
        if(selectList.isEmpty()){
            return;
        }
        //2.获取目录子目录
        List<String> delFilePidList = new ArrayList<>();
        for (FileInfo fileInfo : selectList) {
            findAllSubFolderIdList(delFilePidList,userId,fileInfo.getFileId(),FileDelFlagEnums.DEL.getFlag());
        }
        //3，将子目录所有文件更新为正常状态
        if(!delFilePidList.isEmpty()){
            //恢复选中目录下的子目录和文件
            FileInfo updateInfo = new FileInfo();
            updateInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
            updateInfo.setLastUpdateTime(curDate);
            query=new FileInfoQuery();
            query.setUserId(userId).setFilePidArray(delFilePidList).setDelFlag(FileDelFlagEnums.DEL.getFlag());
            update(updateInfo,getWrapperByParam(query));
        }
        //4.获取父级目录列表
        Map<String, Set<String>> broName = new HashMap<>();
        for (FileInfo fileInfo : selectList) {
            String filePid = fileInfo.getFilePid();
            if (!broName.containsKey(filePid)) {
                //查询同目录文件名列表
                query = new FileInfoQuery();
                query.setUserId(userId).setFileId(filePid).setDelFlag(FileDelFlagEnums.USING.getFlag());
                FileInfo parentFile = getOne(getWrapperByParam(query));
                if (parentFile != null) {
                    //查询同目录id
                    query = new FileInfoQuery();
                    query.setUserId(userId).setFilePid(filePid).setDelFlag(FileDelFlagEnums.USING.getFlag());
                    List<FileInfo> fileList = list(getWrapperByParam(query));

                    Set<String> broNames = fileList.stream()
                            .filter(file -> !file.getFileId().equals(fileInfo.getFileId()))
                            .map(FileInfo::getFileName)
                            .collect(Collectors.toSet());

                    broName.put(filePid, broNames);
                } else {
                    // 处理父文件夹被删除的情况，将选中文件恢复到根目录下，并处理重名情况
                    filePid = Constants.ROOT_PATH_PID; // 设置为根目录的ID
                    query = new FileInfoQuery();
                    query.setUserId(userId).setFilePid(filePid).setDelFlag(FileDelFlagEnums.USING.getFlag());
                    List<FileInfo> fileList = list(getWrapperByParam(query));

                    Set<String> broNames = fileList.stream()
                            .filter(file -> !file.getFileId().equals(fileInfo.getFileId()))
                            .map(FileInfo::getFileName)
                            .collect(Collectors.toSet());

                    broName.put(filePid, broNames);
                }
            }

            Set<String> broNames = broName.get(filePid);
            if (broNames != null) {
                fileInfo.setFileName(renameFile(fileInfo, broNames));

            }

            fileInfo.setFilePid(filePid);
            fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
            fileInfo.setLastUpdateTime(curDate);
        }

        //将选中文件恢复为正常状态
        updateBatchById(selectList);
    }

    /**
     * 删除文件：彻底删除
     * @param userId
     * @param fileIds
     * @param adminOp
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delFileBatch(String userId, String fileIds, boolean adminOp) {
        String[] fileIdArray = fileIds.split(",");
        //1.查询待删除文件列表
        FileInfoQuery query = new FileInfoQuery();
        isAdminDelete(query,adminOp);
        query.setFileIdArray(Arrays.asList(fileIdArray)).setUserId(userId);
        List<FileInfo> selectList = list(getWrapperByParam(query));
        if(selectList.isEmpty()){
            return;
        }
        //2.获取目录子目录
        List<String> delFileSubFolder = new ArrayList<>();
        for (FileInfo fileInfo : selectList) {
            findAllSubFolderIdList(delFileSubFolder,userId,fileInfo.getFileId(),FileDelFlagEnums.DEL.getFlag());
        }
        //3，将子目录所有文件更新为删除
        if(!delFileSubFolder.isEmpty()){
            query=new FileInfoQuery();
            query.setUserId(userId).setFilePidArray(delFileSubFolder);
            isAdminDelete(query,adminOp);
            fileInfoMapper.delete(getWrapperByParam(query));
        }
        //4.删除所选文件
        query=new FileInfoQuery();
        query.setUserId(userId).setFileIdArray(Arrays.asList(fileIdArray));
        isAdminDelete(query,adminOp);
        fileInfoMapper.delete(getWrapperByParam(query));
        //更新用户空间
        Long useSpace = fileInfoMapper.selectUseSpace(userId);
        UserInfo userInfo = new UserInfo();
        userInfo.setUseSpace(useSpace);
        userInfo.setUserId(userId);
        userInfoMapper.updateById(userInfo);
        //设置缓存
        UserSpaceDto userSpaceUse = redisComponent.getUserSpaceUse(userId);
        userSpaceUse.setUseSpace(useSpace);
        redisComponent.saveUserSpaceUse(userId,userSpaceUse);
    }
    private void isAdminDelete(FileInfoQuery query,Boolean adminOp){
        if(!adminOp){
            query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
        }
    }
    @Override
    public PaginationResultVO findAllFile(FileInfoQuery query) {
        Page<FileInfo> page = new Page<>();
        page.setCurrent(query.getPageNo());
        page.setSize(query.getPageSize());
        System.out.println("query.getFileNameFuzzy()"+query.getFileNameFuzzy());
        QueryWrapper<FileInfo> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(FileInfo::getFilePid,query.getFilePid())
                        .like(StringUtils.isNotEmpty(query.getFileNameFuzzy()),FileInfo::getFileName,query.getFileNameFuzzy());

        fileInfoMapper.selectAllFile(page, wrapper);
        return new PaginationResultVO(page.getTotal(), page.getSize(), page.getCurrent(), page.getPages(), page.getRecords());
    }

    /**
     * 将分享链接中的文件转到自己文件夹
     * @param fileId 已分享文件id
     * @param shareFileIds 选中要保存到自己网盘的文件
     * @param myFolderId 保存到自己的文件夹id
     * @param shareUserId 分享人id
     * @param userId 保存到的用户id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveShare(String fileId, String shareFileIds, String myFolderId, String shareUserId, String userId) {
        /**
         * 1.查询目标文件夹列表
         * 2.查询选中要保存的文件列表
         * 3.判断是否重命名选择的文件
         * 4.查询选中文件子目录，并设置为保存人的用户id
         * 5.将选中文件插入到数据库
         * 6.计算用户空间，并更新到redis
         */

        //1.查询目标文件夹列表
        QueryWrapper<FileInfo> wrapper = new QueryWrapper<>();
         wrapper.lambda().eq(FileInfo::getUserId, userId)
                .eq(FileInfo::getFilePid, myFolderId);
        List<FileInfo> fileInfos = list(wrapper);
        Set<String> broNames = fileInfos.stream().map(FileInfo::getFileName).collect(Collectors.toSet());
//        2.查询选中文件列表
        String[] shareFileIdArray = shareFileIds.split(",");
        wrapper=new QueryWrapper<>();
        wrapper.lambda().eq(FileInfo::getUserId,shareUserId)
                .in(FileInfo::getFileId,shareFileIdArray);
        List<FileInfo> selectFiles=list(wrapper);
        //文件重命名
        List<FileInfo> copyFileList=new ArrayList<>();
        Date curDate = new Date();
        for (FileInfo selectFile : selectFiles) {
            //3.重命名
            selectFile.setFileName(renameFile(selectFile,broNames));
            //4.查询选中文件子目录
            findAllSubFile(copyFileList,selectFile,shareUserId,userId,curDate,myFolderId);
        }
        //5.将选中文件添加到数据库
        saveBatch(copyFileList);
        //6.计算用户使用空间
        Long useSpace = fileInfoMapper.selectUseSpace(userId);
        UserInfo userInfo = userInfoMapper.selectById(userId);
        if(useSpace>userInfo.getTotalSpace()){
            throw new BusinessException(ResponseCodeEnum.CODE_904);
        }
        userInfo=new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setUseSpace(useSpace);
        userInfoMapper.updateById(userInfo);
        //设置缓存
        UserSpaceDto userSpaceDto = redisComponent.getUserSpaceUse(userId);
        userSpaceDto.setUseSpace(useSpace);
        redisComponent.saveUserSpaceUse(userId,userSpaceDto);
    }

    /**
     * 查询选中文件子文件，并设置用户id为当前用户
     * @param copyFileList
     * @param fileInfo
     * @param userId
     * @param curDate
     * @param myFolderId
     */
    private void findAllSubFile(List<FileInfo> copyFileList, FileInfo fileInfo,String sourceUserId, String userId, Date curDate, String myFolderId) {
        QueryWrapper<FileInfo> wrapper=null;
        String sourceFileId = fileInfo.getFileId();
        fileInfo.setCreateTime(curDate);
        fileInfo.setLastUpdateTime(curDate);
        fileInfo.setFilePid(myFolderId);
        fileInfo.setUserId(userId);
        String newFileId = StringTools.getRandomString(Constants.LENGTH_10);
        fileInfo.setFileId(newFileId);
        copyFileList.add(fileInfo);
        if(FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())){
            wrapper=new QueryWrapper<>();
            wrapper.lambda().eq(FileInfo::getFilePid,sourceFileId)
                    .eq(FileInfo::getUserId,sourceUserId);
            List<FileInfo> sourceFileList = list(wrapper);
            for (FileInfo info : sourceFileList) {
                findAllSubFile(copyFileList,info,sourceUserId,userId,curDate,newFileId);
            }
        }
    }

    /**
     * 判断查看的文件是否为已分享的文件子目录
     * @param rootFilePid 已分享文件id
     * @param shareUserId 分享人id
     * @param filePid 请求加载的文件父id
     */
    @Override
    public void checkRootFilePid(String rootFilePid, String shareUserId, String filePid) {
        if(StringUtils.isEmpty(rootFilePid)){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (rootFilePid.equals(filePid)) {
            //查看分享文件根目录，放行
            return;
        }
        checkFilePid(rootFilePid,shareUserId,filePid);
    }

    private void checkFilePid(String rootFilePid, String shareUserId, String filePid) {
        FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(filePid, shareUserId);
        if(fileInfo==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if(Constants.ZERO_STR.equals(fileInfo.getFilePid())){
            //不能越级查看根目录文件
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if(fileInfo.getFilePid().equals(rootFilePid)){
            //在分享文件根目录，放行
            return;
        }
        //向父级目录查询
        checkFilePid(rootFilePid,shareUserId,fileInfo.getFilePid());
    }

    /**
     * 重命名同目录文件
     * @param fileInfo ：文件
     * @param broNames ：同目录文件名列表
     * @return ：新文件名
     */
    private String renameFile(FileInfo fileInfo,Set<String> broNames) {
        // 例如，可以在文件名后面添加数字来区别不同的重名文件
        int sequence = 0;
        String srcName = fileInfo.getFileName();
        String newName=srcName;
        while (broNames.contains(newName)) {
            sequence++;
            if(fileInfo.getFolderType().equals(FileFolderTypeEnums.FOLDER.getType())){
                //如果是文件夹重名,重命名为(i)
                newName=String.format("%s(%d)", srcName, sequence);
            }else{
                //如果是文件重名名，重命名为_{i}.extension
                int index = srcName.lastIndexOf(".");
                if (index != -1) {
                    // 存在扩展名
                    String name = srcName.substring(0, index);
                    String extension = srcName.substring(index);

                    // 构建新的文件名
                     newName = name + "_" + sequence + extension;

                } else {
                    // 不存在扩展名
                     newName = srcName + "_" + sequence;
                }
            }
        }
        //添加到文件名集合
        broNames.add(newName);
        return newName;
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
     * 自动进行重命名操作
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

    /**
     * 转码，合并分片文件
     * @param fileId
     * @param webUserDto
     */
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
            // 视频文件切割
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




