package com.easypan.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easypan.entity.po.FileInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easypan.entity.po.FileShare;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 53082
* @description 针对表【file_info(文件信息表)】的数据库操作Mapper
* @createDate 2023-08-16 22:00:16
* @Entity com.easypan.entity.po.FileInfo
*/
public interface FileInfoMapper extends BaseMapper<FileInfo> {

    Long selectUseSpace(String userId);

    FileInfo selectByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId);

    IPage<FileInfo> selectAllFile(Page<FileInfo> page, @Param("ew") QueryWrapper<FileInfo> wrapper);
}




