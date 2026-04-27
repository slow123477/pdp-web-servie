package com.slow.pojo.dto;

import lombok.Data;

/**
 * 角色列表查询参数
 */
@Data
public class RoleQueryDTO {

    private Integer isCurrent;
    private Integer page = 1;
    private Integer pageSize = 10;
}
