package com.easypan.entity.query;

import lombok.Data;

@Data
public class SimplePage {
    private int pageNo;
    private int countTotal;
    private int pageSize;
    private int pageTotal;
    private int start;
    private int end;
    public SimplePage(int start, int end) {
        this.start = start;
        this.end = end;
    }
    public SimplePage(int pageNo, int countTotal, int pageSize) {
        this.pageNo = pageNo;
        this.countTotal = countTotal;
        this.pageSize = pageSize;
    }
}
