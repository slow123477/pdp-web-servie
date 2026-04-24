package com.slow.service.controller;

import com.slow.common.exception.BusinessException;
import com.slow.common.result.Result;
import com.slow.common.utils.ThreadLocalUtil;
import com.slow.pojo.dto.UserPasswordUpdateDTO;
import com.slow.pojo.dto.UserProfileUpdateDTO;
import com.slow.pojo.entity.User;
import com.slow.pojo.vo.UserVO;
import com.slow.service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "用户模块", description = "用户查询、资料修改、密码修改、头像上传")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "根据ID查询用户", description = "查询指定ID的用户详情")
    @GetMapping("/{id}")
    public Result<UserVO> getById(@Parameter(description = "用户ID") @PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return Result.success(vo);
    }

    @Operation(summary = "修改个人资料", description = "修改当前登录用户的个人资料")
    @PutMapping
    public Result<Object> updateProfile(@RequestBody @Valid UserProfileUpdateDTO dto) {
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(dto.getId())) {
            throw new BusinessException("无权修改其他用户资料");
        }
        userService.updateProfile(currentUserId, dto);
        return Result.success("修改成功", null);
    }

    @Operation(summary = "修改密码", description = "修改当前登录用户的密码，需验证原密码")
    @PutMapping("/password")
    public Result<Object> updatePassword(@RequestBody @Valid UserPasswordUpdateDTO dto) {
        Long currentUserId = getCurrentUserId();
        userService.updatePassword(currentUserId, dto);
        return Result.success("密码修改成功", null);
    }

    @Operation(summary = "上传头像", description = "上传用户头像图片，支持 jpg/png/gif/webp")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(
            @Parameter(description = "头像图片文件") @RequestParam("file") MultipartFile file) {
        Long currentUserId = getCurrentUserId();
        String avatarUrl = userService.uploadAvatar(currentUserId, file);
        return Result.success("上传成功", avatarUrl);
    }

    private Long getCurrentUserId() {
        Claims claims = ThreadLocalUtil.get();
        Integer idInt = claims.get("id", Integer.class);
        return idInt.longValue();
    }
}
