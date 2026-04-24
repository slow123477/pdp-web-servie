package com.slow.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserProfileUpdateDTO {
    @NotNull(message = "用户ID不能为空")
    private Long id;

    private String realName;
    private String avatar;
    private String major;
    private Integer gradeYear;
    private String studentId;
}
