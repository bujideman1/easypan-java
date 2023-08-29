package com.easypan.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.easypan.entity.enums.FileDelFlagEnums;
import com.easypan.entity.po.FileInfo;
import com.easypan.service.FileInfoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FileCleanTask {
    @Resource
    private FileInfoService fileInfoService;

    /**
     * 定时清理回收站过期文件
     */
    @Scheduled(fixedDelay = 1000*60*3)
    public void execute(){
        /**
         * 1.查询回收站状态过期文件
         * 2.将文件按id分组
         * 3.批量删除文件
         */
        QueryWrapper<FileInfo> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(FileInfo::getDelFlag, FileDelFlagEnums.RECYCLE.getFlag())
                        .lt(FileInfo::getRecoveryTime, LocalDateTime.now().minusDays(10));
        List<FileInfo> fileInfos = fileInfoService.list(wrapper);
        Map<String, List<FileInfo>> fileInfoMap = fileInfos.stream().collect(Collectors.groupingBy(FileInfo::getUserId));
        for (Map.Entry<String, List<FileInfo>> entry : fileInfoMap.entrySet()) {
            List<String> fileIds = entry.getValue().stream().map(FileInfo::getFileId).collect(Collectors.toList());
            fileInfoService.delFileBatch(entry.getKey(),String.join(",",fileIds),false);
        }

    }
}
