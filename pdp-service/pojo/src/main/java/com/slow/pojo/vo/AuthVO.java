package com.slow.pojo.vo;

import lombok.Data;

/**
 * 登录/注册响应VO，在UserVO基础上增加token字段
 */
@Data
public class AuthVO {
    private Long id;
    private String username;
    private String email;
    private String realName;
    private String avatar;
    private String token;
}
