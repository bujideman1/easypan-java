package com.easypan.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserStatusEnum {
    ENABLE(1,"启用"),
    DISABLE(0,"禁用");
    final Integer status;
    final String desc;
    public static UserStatusEnum getByStatus(Integer status){
        for (UserStatusEnum anEnum : UserStatusEnum.values()) {
            if(anEnum.getStatus().equals(status)){
                return anEnum;
            }
        }
        return null;
    }
}
