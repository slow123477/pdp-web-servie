package com.slow.service.mapper;

import com.github.pagehelper.Page;
import com.slow.pojo.entity.Role;
import org.apache.ibatis.annotations.Param;

/**
 * 角色 Mapper
 */
public interface RoleMapper {

    Page<Role> selectList(@Param("userId") Long userId,
                          @Param("isCurrent") Integer isCurrent);

    Role selectById(@Param("id") Long id, @Param("userId") Long userId);

    int insert(Role role);

    int update(Role role);

    int deleteById(@Param("id") Long id, @Param("userId") Long userId);
}
