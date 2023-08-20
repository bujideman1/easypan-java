package com.easypan.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 文件信息表
 * @TableName file_info
 */
@TableName(value ="file_info")
@Data
public class FileInfo implements Serializable {
    /**
     * 文件id
     */
    @TableId
    private String fileId;

    /**
     * 用户id
     */
//    @TableId
    private String userId;

    /**
     * 文件MD5值
     */
    private String fileMd5;

    /**
     * 父级ID
     */
    private String filePid;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 封面
     */
    private String fileCover;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后更新时间
     */
    private Date lastUpdateTime;

    /**
     * 0:文件1：目录
     */
    private Integer folderType;

    /**
     * 文件分类1：视频2：音频3：图片4：文档5：其他
     */
    private Integer fileCategory;

    /**
     * 1：视频2：音频3：图片4：pdf5：doc6：excel7：txt8：code9：zip10：其他
     */
    private Integer fileType;

    /**
     * 0:转码中1：转码失败2：转码成功
     */
    private Integer status;

    /**
     * 进入回收站时间
     */
    private Date recoveryTime;

    /**
     * 标记删除0:删除1：回收站2：正常
     */
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}