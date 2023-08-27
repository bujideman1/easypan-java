package com.easypan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.enums.ResponseCodeEnum;
import com.easypan.entity.enums.ShareValidTypeEnums;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.po.FileShare;
import com.easypan.entity.query.FileShareQuery;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.exception.BusinessException;
import com.easypan.service.FileShareService;
import com.easypan.mapper.FileShareMapper;
import com.easypan.utils.DateUtils;
import com.easypan.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 *
 */
@Service
public class FileShareServiceImpl extends ServiceImpl<FileShareMapper, FileShare>
    implements FileShareService{
@Resource
private FileShareMapper fileShareMapper;
    @Override
    public PaginationResultVO loadShareList(String userId, FileShareQuery query) {

        Page<FileShare> page1 = new Page<>();
        page1.setCurrent(query.getPageNo()==null?0:query.getPageNo());
        page1.setSize(query.getPageSize()==null?15:query.getPageSize());
        QueryWrapper<FileShare> wrapper = new QueryWrapper<>();
        wrapper.eq("s.user_id",userId);
        IPage<FileShare> page=fileShareMapper.pageByCondtion(page1,wrapper);
         return (PaginationResultVO<FileShare>) new PaginationResultVO(page.getTotal(), page.getSize(), page.getCurrent(), page.getPages(), page.getRecords());
    }



    @Override
    public FileShare saveShare(String userId, String fileId, Integer validType, String code) {
        ShareValidTypeEnums validTypeEnums = ShareValidTypeEnums.getByType(validType);
        if(null==validTypeEnums){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        FileShare fileShare = new FileShare();
        fileShare.setFileId(fileId);
        fileShare.setUserId(userId);
        if(validTypeEnums!=ShareValidTypeEnums.FOREVER){
            fileShare.setExpireTime(DateUtils.getAfterDate(validTypeEnums.getDays()));
        }
        Date curDate = new Date();
        fileShare.setShareTime(curDate);
        if(StringTools.isEmpty(code)){
            fileShare.setCode(StringTools.getRandomString(Constants.VALID_CODE_LENGTH));
        }
        fileShare.setShareId(StringTools.getRandomString(Constants.LENGTH_20));
        fileShareMapper.insert(fileShare);
        return fileShare;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delShareBatch(String[] shareIdArray, String userId) {
        QueryWrapper<FileShare> wrapper=new QueryWrapper<>();
        wrapper.lambda().eq(FileShare::getUserId,userId).in(FileShare::getShareId,shareIdArray);
        int delete = fileShareMapper.delete(wrapper);
        if(delete!=shareIdArray.length){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }
}




