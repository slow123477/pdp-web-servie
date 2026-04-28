package com.slow.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据操作记录视图对象
 */
@Data
public class DataOperationVO {

    private Long id;
    private String operationType;
    private String dataType;
    private String fileUrl;
    private String status;
    private LocalDateTime createdAt;
}
