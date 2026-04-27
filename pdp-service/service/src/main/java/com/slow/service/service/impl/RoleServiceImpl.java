package com.slow.service.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.slow.common.exception.BusinessException;
import com.slow.common.result.PageResult;
import com.slow.common.utils.ThreadLocalUtil;
import com.slow.pojo.dto.RoleAddDTO;
import com.slow.pojo.dto.RoleQueryDTO;
import com.slow.pojo.dto.RoleUpdateDTO;
import com.slow.pojo.entity.Role;
import com.slow.pojo.vo.RoleVO;
import com.slow.service.mapper.RoleMapper;
import com.slow.service.service.RoleService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色 Service 实现
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public PageResult list(RoleQueryDTO queryDTO) {
        Long userId = getCurrentUserId();
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<Role> page = roleMapper.selectList(userId, queryDTO.getIsCurrent());
        List<RoleVO> rows = page.getResult().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return new PageResult(page.getTotal(), rows);
    }

    @Override
    public RoleVO getById(Long id) {
        Long userId = getCurrentUserId();
        Role role = roleMapper.selectById(id, userId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return convertToVO(role);
    }

    @Override
    public void add(RoleAddDTO addDTO) {
        Long userId = getCurrentUserId();
        Role role = new Role();
        BeanUtils.copyProperties(addDTO, role);
        role.setUserId(userId);
        roleMapper.insert(role);
    }

    @Override
    public void update(RoleUpdateDTO updateDTO) {
        Long userId = getCurrentUserId();
        Role existing = roleMapper.selectById(updateDTO.getId(), userId);
        if (existing == null) {
            throw new BusinessException("角色不存在");
        }
        Role role = new Role();
        BeanUtils.copyProperties(updateDTO, role);
        role.setUserId(userId);
        roleMapper.update(role);
    }

    @Override
    public void delete(Long id) {
        Long userId = getCurrentUserId();
        int rows = roleMapper.deleteById(id, userId);
        if (rows == 0) {
            throw new BusinessException("角色不存在");
        }
    }

    /**
     * 从 ThreadLocal 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Claims claims = ThreadLocalUtil.get();
        if (claims == null) {
            throw new BusinessException("未登录");
        }
        Object idObj = claims.get("id");
        if (idObj == null) {
            throw new BusinessException("未登录");
        }
        return Long.valueOf(idObj.toString());
    }

    /**
     * Entity 转 VO
     */
    private RoleVO convertToVO(Role role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }
}
