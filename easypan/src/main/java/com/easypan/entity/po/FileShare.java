package com.easypan.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 文件分析
 * @TableName file_share
 */
@TableName(value ="file_share")
@Data
public class FileShare implements Serializable {
    /**
     * 
     */
    @TableId
    private String shareId;

    /**
     * 
     */
    private String fileId;

    /**
     * 
     */
    private String userId;

    /**
     * 有效期类型0:1天1:7天2:30天3：永久有效
     */
    private Integer validType;

    /**
     * 失效时间
     */
    private Date expireTime;

    /**
     * 分享时间
     */
    private Date shareTime;

    /**
     * 提取码
     */
    private String code;

    /**
     * 游览次数
     */
    private Integer showCount;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    @TableField(exist = false)
    private String fileName;
    @TableField(exist = false)
    private Integer folderType;

    /**
     * 文件分类1：视频2：音频3：图片4：文档5：其他
     */
    @TableField(exist = false)
    private Integer fileCategory;
    /**
     * 1：视频2：音频3：图片4：pdf5：doc6：excel7：txt8：code9：zip10：其他
     */
    @TableField(exist = false)
    private Integer fileType;
    /**
     * 封面
     */
    @TableField(exist = false)
    private String fileCover;
}