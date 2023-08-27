package com.easypan.entity.query;

import lombok.Data;

@Data
public class UserInfoQuery extends BaseParam{
    private String userId;

    /**
     * 用户昵称
     */
    private String nickName;
    /**
     * 模糊查询用户昵称
     */
    private String nickNameFuzzy;

    /**
     * 邮箱地址
     */
    private String email;
    /**
     * 0：禁用1启用
     */
    private Integer status;
    /**
     * 修改用户使用空间
     */
    private Long changeSpace;

}
