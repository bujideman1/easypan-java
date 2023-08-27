package com.easypan.service;

import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.UploadResultDto;
import com.easypan.entity.po.FileInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.PaginationResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author 53082
* @description 针对表【file_info(文件信息表)】的数据库操作Service
* @createDate 2023-08-16 22:00:16
*/
public interface FileInfoService extends IService<FileInfo> {

    PaginationResultVO findListByPage(FileInfoQuery query);

    Integer findCountByParam(FileInfoQuery param);

    List<FileInfo> findListByParam(FileInfoQuery param);

    UploadResultDto uploadFile(SessionWebUserDto webUserDto, String fileId, MultipartFile file, String fileName,String filePid, String fileMd5, Integer chunkIndex, Integer chunks);

    FileInfo getFileInfoByFileIdAndUserId(String fileId, String userId);

    FileInfo newFolder(String filePid, String userId, String fileName);

    List<FileInfo> list(FileInfoQuery query);

    FileInfo rename(String fileId, String userId, String fileName);

    List<FileInfo> loadAllFolder(String filePid, String userId, String currentFileIds);

    void changeFileFolder(String filePid, String userId, String fileIds);

    void removeFile2RecycleBatch(String userId, String fileIds);

    void recoverFileBatch(String userId, String fileIds);

    void delFileBatch(String userId, String fileIds, boolean adminOp);
}
