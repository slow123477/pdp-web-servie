package com.slow.service.service;

import com.slow.common.result.PageResult;
import com.slow.pojo.dto.RoleAddDTO;
import com.slow.pojo.dto.RoleQueryDTO;
import com.slow.pojo.dto.RoleUpdateDTO;
import com.slow.pojo.vo.RoleVO;

/**
 * 角色 Service
 */
public interface RoleService {

    PageResult list(RoleQueryDTO queryDTO);

    RoleVO getById(Long id);

    void add(RoleAddDTO addDTO);

    void update(RoleUpdateDTO updateDTO);

    void delete(Long id);
}
