package com.easypan.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum FileCategoryEnums {
    VIDEO(1,"video","视频"),
    MUSIC(2,"music","音频"),
    IMAGE(2,"music","音频"),
    DOC(2,"music","音频"),
    OTHERS(2,"music","音频")
    ;
    @Getter
    private final Integer category;
    @Getter
    private final String code;
    private final String desc;

    public static FileCategoryEnums getByCode(String category) {
        FileCategoryEnums[] values = FileCategoryEnums.values();
        for (FileCategoryEnums value : values) {
            if(value.getCode().equals(category)){
                return value;
            }
        }
        return null;
    }
}
