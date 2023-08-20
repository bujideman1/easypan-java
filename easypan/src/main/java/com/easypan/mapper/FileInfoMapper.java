package com.easypan.mapper;

import com.easypan.entity.po.FileInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author 53082
* @description 针对表【file_info(文件信息表)】的数据库操作Mapper
* @createDate 2023-08-16 22:00:16
* @Entity com.easypan.entity.po.FileInfo
*/
public interface FileInfoMapper extends BaseMapper<FileInfo> {

    Long selectUseSpace(String userId);
}




