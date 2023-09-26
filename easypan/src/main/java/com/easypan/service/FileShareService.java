package com.easypan.service;

import com.easypan.entity.dto.SessionShareDto;
import com.easypan.entity.po.FileShare;
import com.baomidou.mybatisplus.extension.service.IService;
import com.easypan.entity.query.FileShareQuery;
import com.easypan.entity.vo.PaginationResultVO;

/**
 *
 */
public interface FileShareService extends IService<FileShare> {

    PaginationResultVO loadShareList(String userId, FileShareQuery query);

    FileShare saveShare(String userId, String fileId, Integer validType, String code);
    void delShareBatch(String[] shareIdArray,String userID);

    SessionShareDto checkShareCode(String shareId, String code);
}
