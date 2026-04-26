package com.slow.pojo.vo;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;

/**
 * 用户设置视图对象
 */
@Data
public class UserSettingVO {

    private Long userId;

    private String gpaStandard;

    @JsonRawValue
    private String gpaScale;

    @JsonRawValue
    private String aiDimensions;

    private String theme;
}
