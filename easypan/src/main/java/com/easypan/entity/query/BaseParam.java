package com.easypan.entity.query;

import com.easypan.entity.enums.PageSize;
import lombok.Data;
import lombok.Getter;

@Data
public class BaseParam {
    private SimplePage simplePage;
    private Integer pageNo;
    private Integer pageSize;
    private String orderBy;
    private String orderByAsc;
    private String orderByDesc;
    public Integer getPageNo(){
        if(pageNo==null)return 0;
        return pageNo;
    }
    public Integer getPageSize(){
        if(pageSize==null)return PageSize.SIZE15.getSize();
        return pageSize;
    }
}
