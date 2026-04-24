package com.slow.service.service;

import com.slow.pojo.dto.UserLoginDTO;
import com.slow.pojo.dto.UserRegisterDTO;
import com.slow.pojo.entity.User;

public interface UserService {

    /**
     * 用户注册
     * @param dto 注册参数
     * @return 注册成功后的用户实体（含自增id）
     */
    User register(UserRegisterDTO dto);

    /**
     * 用户登录
     * @param dto 登录参数（username字段支持用户名或邮箱）
     * @return 登录成功后的用户实体
     */
    User login(UserLoginDTO dto);

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户实体，不存在时返回null
     */
    User getById(Long id);
}
