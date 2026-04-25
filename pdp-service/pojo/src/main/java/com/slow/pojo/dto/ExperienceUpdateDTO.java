package com.slow.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 修改经历请求参数
 */
@Data
public class ExperienceUpdateDTO {

    @NotNull(message = "经历ID不能为空")
    private Long id;

    @NotBlank(message = "经历标题不能为空")
    private String title;

    @NotBlank(message = "类型不能为空")
    private String type;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private String result;

    private List<AttachmentItemDTO> attachments;
}
