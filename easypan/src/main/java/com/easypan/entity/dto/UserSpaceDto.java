package com.easypan.entity.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
@Getter
@Setter
@NoArgsConstructor
public class UserSpaceDto implements Serializable {
    private Long useSpace;
    private Long totalSpace;
}
