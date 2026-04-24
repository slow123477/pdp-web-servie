package com.slow.service.service.impl;

import com.slow.common.exception.BusinessException;
import com.slow.pojo.dto.UserLoginDTO;
import com.slow.pojo.dto.UserPasswordUpdateDTO;
import com.slow.pojo.dto.UserProfileUpdateDTO;
import com.slow.pojo.dto.UserRegisterDTO;
import com.slow.pojo.entity.User;
import com.slow.service.mapper.UserMapper;
import com.slow.service.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

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

    @Override
    public void updateProfile(Long userId, UserProfileUpdateDTO dto) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        User updateUser = new User();
        updateUser.setId(userId);

        BeanUtils.copyProperties(dto, updateUser);

        userMapper.update(updateUser);
    }

    @Override
    public void updatePassword(Long userId, UserPasswordUpdateDTO dto) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.update(updateUser);
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("请选择文件");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = getFileExtension(originalFilename);
        if (!isValidImageExtension(ext)) {
            throw new BusinessException("仅支持 jpg/png/gif/webp 格式");
        }

        // 生成随机文件名
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        try {
            String basePath = System.getProperty("user.dir");
            Path avatarDir = Paths.get(basePath, "target", "classes", "static", "upload", "avatar");
            Files.createDirectories(avatarDir);
            Path targetPath = avatarDir.resolve(filename);
            file.transferTo(targetPath.toFile());
        } catch (IOException e) {
            throw new BusinessException("文件保存失败");
        }

        String avatarUrl = "/upload/avatar/" + filename;
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setAvatar(avatarUrl);
        userMapper.update(updateUser);

        return avatarUrl;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isValidImageExtension(String ext) {
        return Arrays.asList("jpg", "jpeg", "png", "gif", "webp").contains(ext);
    }
}
