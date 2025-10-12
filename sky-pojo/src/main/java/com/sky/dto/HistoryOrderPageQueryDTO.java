package com.sky.dto;

import lombok.Data;

@Data
public class HistoryOrderPageQueryDTO {
    private int page;
    private int pageSize;
    private Integer status;
}
