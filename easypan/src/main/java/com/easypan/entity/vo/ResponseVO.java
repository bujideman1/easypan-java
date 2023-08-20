package com.easypan.entity.vo;


import com.easypan.entity.enums.ResponseCodeEnum;

public class ResponseVO<T> {
    private String status;
    private Integer code;
    private String info;
    private T data;

    public ResponseVO() {
        this.code= ResponseCodeEnum.CODE_200.getCode();
        this.info=ResponseCodeEnum.CODE_200.getMsg();
    }
    public static ResponseVO okResult(){
        ResponseVO responseVO = new ResponseVO();
        return responseVO;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}

