package com.slow.service.service;

import com.slow.pojo.dto.UserLoginDTO;
import com.slow.pojo.dto.UserPasswordUpdateDTO;
import com.slow.pojo.dto.UserProfileUpdateDTO;
import com.slow.pojo.dto.UserRegisterDTO;
import com.slow.pojo.entity.User;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * 修改用户个人资料
     * @param userId 当前登录用户ID
     * @param dto 修改参数
     */
    void updateProfile(Long userId, UserProfileUpdateDTO dto);

    /**
     * 修改密码
     * @param userId 当前登录用户ID
     * @param dto 密码修改参数
     */
    void updatePassword(Long userId, UserPasswordUpdateDTO dto);

    /**
     * 上传头像
     * @param userId 当前登录用户ID
     * @param file 头像文件
     * @return 头像访问路径
     */
    String uploadAvatar(Long userId, MultipartFile file);
}
