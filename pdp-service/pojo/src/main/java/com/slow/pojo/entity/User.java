package com.slow.pojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
public class User {

    private Long id;
    private String username;
    private String email;
    private String studentId;
    @JsonIgnore
    private String password;
    private String realName;
    private String avatar;
    private String major;
    private Integer gradeYear;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
