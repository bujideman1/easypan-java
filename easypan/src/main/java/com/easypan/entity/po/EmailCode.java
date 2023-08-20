package com.easypan.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName email_code
 */
@TableName(value ="email_code")
@Data
public class EmailCode implements Serializable {
    /**
     * 邮箱
     */
    @TableId(type=IdType.ASSIGN_ID)
    private String email;

    /**
     * 验证码
     */
//    @TableId(type =IdType.ASSIGN_ID )
    private String code;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 0:未使用1已使用
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}