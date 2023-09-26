package com.easypan.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
@Data
@JsonIgnoreProperties(ignoreUnknown = true)//反序列化的时候不报错
public class UploadResultDto implements Serializable {
    private String fileId;
    private String status;
}
