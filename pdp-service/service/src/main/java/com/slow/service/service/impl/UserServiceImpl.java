package com.slow.service.service.impl;

import com.slow.common.exception.BusinessException;
import com.slow.pojo.dto.UserLoginDTO;
import com.slow.pojo.dto.UserRegisterDTO;
import com.slow.pojo.entity.User;
import com.slow.service.mapper.UserMapper;
import com.slow.service.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public User register(UserRegisterDTO dto) {
        // 1. 校验用户名是否已存在
        User existByUsername = userMapper.findByUsername(dto.getUsername());
        if (existByUsername != null) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 校验邮箱是否已被注册
        User existByEmail = userMapper.findByEmail(dto.getEmail());
        if (existByEmail != null) {
            throw new BusinessException("邮箱已被注册");
        }

        // 3. 构建User实体，复制DTO字段并加密密码
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // 4. 插入数据库（useGeneratedKeys会自动回填id到user.id）
        userMapper.insert(user);

        return user;
    }

    @Override
    public User login(UserLoginDTO dto) {
        // username字段兼容用户名或邮箱，将同一个值传给两个参数
        User user = userMapper.findByUsernameOrEmail(dto.getUsername(), dto.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 密码比对（明文 vs BCrypt哈希）
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        return user;
    }

    @Override
    public User getById(Long id) {
        return userMapper.findById(id);
    }
}
