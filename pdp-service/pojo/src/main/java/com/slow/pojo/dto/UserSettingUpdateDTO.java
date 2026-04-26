package com.slow.pojo.dto;

import lombok.Data;

import java.util.Map;

/**
 * 修改用户设置请求参数
 */
@Data
public class UserSettingUpdateDTO {

    private String gpaStandard;

    private Map<String, Object> gpaScale;

    private Map<String, Object> aiDimensions;

    private String theme;
}
