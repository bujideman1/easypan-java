package com.easypan.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户信息表
 * @TableName user_info
 */
@TableName(value ="user_info")
@Data
public class UserInfo implements Serializable {
    /**
     * 用户id
     */
    @TableId
    private String userId;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * open_id
     */
    private String qqOpenId;

    /**
     * 头像地址
     */
    private String qqAvatar;

    /**
     * 密码
     */
    private String password;

    /**
     * 注册时间
     */
    private Date joinTime;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 0：禁用1启用
     */
    private Integer status;

    /**
     * 使用空间
     */
    private Long useSpace;

    /**
     * 总空间
     */
    private Long totalSpace;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        UserInfo other = (UserInfo) that;
        return (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getNickName() == null ? other.getNickName() == null : this.getNickName().equals(other.getNickName()))
            && (this.getEmail() == null ? other.getEmail() == null : this.getEmail().equals(other.getEmail()))
            && (this.getQqOpenId() == null ? other.getQqOpenId() == null : this.getQqOpenId().equals(other.getQqOpenId()))
            && (this.getQqAvatar() == null ? other.getQqAvatar() == null : this.getQqAvatar().equals(other.getQqAvatar()))
            && (this.getPassword() == null ? other.getPassword() == null : this.getPassword().equals(other.getPassword()))
            && (this.getJoinTime() == null ? other.getJoinTime() == null : this.getJoinTime().equals(other.getJoinTime()))
            && (this.getLastLoginTime() == null ? other.getLastLoginTime() == null : this.getLastLoginTime().equals(other.getLastLoginTime()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getUseSpace() == null ? other.getUseSpace() == null : this.getUseSpace().equals(other.getUseSpace()))
            && (this.getTotalSpace() == null ? other.getTotalSpace() == null : this.getTotalSpace().equals(other.getTotalSpace()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getNickName() == null) ? 0 : getNickName().hashCode());
        result = prime * result + ((getEmail() == null) ? 0 : getEmail().hashCode());
        result = prime * result + ((getQqOpenId() == null) ? 0 : getQqOpenId().hashCode());
        result = prime * result + ((getQqAvatar() == null) ? 0 : getQqAvatar().hashCode());
        result = prime * result + ((getPassword() == null) ? 0 : getPassword().hashCode());
        result = prime * result + ((getJoinTime() == null) ? 0 : getJoinTime().hashCode());
        result = prime * result + ((getLastLoginTime() == null) ? 0 : getLastLoginTime().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getUseSpace() == null) ? 0 : getUseSpace().hashCode());
        result = prime * result + ((getTotalSpace() == null) ? 0 : getTotalSpace().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", userId=").append(userId);
        sb.append(", nickName=").append(nickName);
        sb.append(", email=").append(email);
        sb.append(", qqOpenId=").append(qqOpenId);
        sb.append(", qqAvatar=").append(qqAvatar);
        sb.append(", password=").append(password);
        sb.append(", joinTime=").append(joinTime);
        sb.append(", lastLoginTime=").append(lastLoginTime);
        sb.append(", status=").append(status);
        sb.append(", useSpace=").append(useSpace);
        sb.append(", totalSpace=").append(totalSpace);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}