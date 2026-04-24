package com.slow.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult implements Serializable {

    private Long total;
    private List rows;
}
