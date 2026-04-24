package com.slow.service.mapper;

import com.slow.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    /**
     * 插入用户，返回自增主键到user.id
     */
    void insert(User user);

    /**
     * 根据用户名查询用户
     */
    User findByUsername(String username);

    /**
     * 根据邮箱查询用户
     */
    User findByEmail(String email);

    /**
     * 根据用户名或邮箱查询用户（登录用，支持两种方式登录）
     */
    User findByUsernameOrEmail(String username, String email);

    /**
     * 根据ID查询用户
     */
    User findById(Long id);

    /**
     * 动态更新用户字段（只更新非空字段）
     */
    void update(User user);
}
