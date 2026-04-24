package com.slow.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String email;
    private String studentId;
    private String realName;
    private String avatar;
    private String major;
    private Integer gradeYear;
    private LocalDateTime createdAt;
}
