package com.slow.service.controller;

import com.slow.common.exception.BusinessException;
import com.slow.common.result.Result;
import com.slow.common.utils.JwtUtil;
import com.slow.common.utils.ThreadLocalUtil;
import com.slow.pojo.dto.UserLoginDTO;
import com.slow.pojo.dto.UserRegisterDTO;
import com.slow.pojo.entity.User;
import com.slow.pojo.vo.AuthVO;
import com.slow.pojo.vo.UserVO;
import com.slow.service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "认证模块", description = "用户注册、登录、获取当前用户信息")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户注册", description = "注册新账号，用户名和邮箱必须唯一")
    @PostMapping("/register")
    public Result<AuthVO> register(@RequestBody @Valid UserRegisterDTO dto) {
        User user = userService.register(dto);
        String token = generateToken(user);

        AuthVO vo = new AuthVO();
        BeanUtils.copyProperties(user, vo);
        vo.setToken(token);

        return Result.success("注册成功", vo);
    }

    @Operation(summary = "用户登录", description = "用户名或邮箱均可登录，登录成功后返回JWT令牌")
    @PostMapping("/login")
    public Result<AuthVO> login(@RequestBody @Valid UserLoginDTO dto) {
        User user = userService.login(dto);
        String token = generateToken(user);

        AuthVO vo = new AuthVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setToken(token);

        return Result.success("登录成功", vo);
    }

    @Operation(summary = "获取当前登录用户", description = "从请求头token中解析用户信息")
    @GetMapping("/user")
    public Result<UserVO> getCurrentUser() {
        // 从ThreadLocal获取JWT解析后的Claims（由JwtTokenInterceptor放入）
        Claims claims = ThreadLocalUtil.get();

        // JWT claims中的数字默认是Integer类型，需转换为Long
        Integer idInt = claims.get("id", Integer.class);
        Long userId = idInt.longValue();

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return Result.success(vo);
    }

    /**
     * 生成JWT令牌，payload包含用户id和username
     */
    private String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        return JwtUtil.generateToken(claims);
    }
}
