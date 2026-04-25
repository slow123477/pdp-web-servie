package com.slow.pojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateDTO {
    @NotNull(message = "用户ID不能为空")
    private Long id;

    @Size(max = 10, message = "真实姓名长度不能超过50")
    private String realName;

    @Size(max = 500, message = "头像URL长度不能超过500")
    private String avatar;

    @Size(max = 100, message = "专业长度不能超过100")
    private String major;

    @Min(value = 1, message = "年级不能小于1")
    @Max(value = 10, message = "年级不能大于10")
    private Integer gradeYear;

    @Size(max = 50, message = "学号长度不能超过50")
    private String studentId;
}
