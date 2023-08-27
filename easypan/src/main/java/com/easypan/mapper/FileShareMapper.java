package com.easypan.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easypan.entity.po.FileShare;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easypan.entity.query.FileShareQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.easypan.entity.po.FileShare
 */
public interface FileShareMapper extends BaseMapper<FileShare> {

    List<FileShare> loadShareList(FileShareQuery query,String userId);

    IPage<FileShare> pageByCondtion(Page<FileShare> page,@Param("ew") QueryWrapper<FileShare> wrapper);
}




