package com.slow.pojo.dto;

import lombok.Data;

/**
 * 用户注册 DTO
 */
@Data
public class UserRegisterDTO {

    private String username;
    private String email;
    private String password;
}
