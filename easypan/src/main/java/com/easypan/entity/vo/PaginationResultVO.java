package com.easypan.entity.vo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class PaginationResultVO<T> {
    private Long totalCount;
    private Long pageSize;
    private Long pageNo;
    private Long pageTotal;
    private List<T> list = new ArrayList<T>();

    public PaginationResultVO(Long totalCount, Long pageSize, Long pageNo, List<T> list) {
        this.totalCount = totalCount;
        this.pageSize = pageSize;
        this.pageNo = pageNo;
        this.list = list;
    }

    public PaginationResultVO(Long totalCount, Long pageSize, Long pageNo, Long pageTotal, List<T> list) {
        if (pageNo == 0) {
            pageNo = 1L;
        }
        this.totalCount = totalCount;
        this.pageSize = pageSize;
        this.pageNo = pageNo;
        this.pageTotal = pageTotal;
        this.list = list;
    }

    public PaginationResultVO(List<T> list) {
        this.list = list;
    }

    public PaginationResultVO() {

    }
}
