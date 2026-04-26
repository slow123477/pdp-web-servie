package com.slow.service.controller;

import com.slow.common.result.Result;
import com.slow.pojo.dto.UserSettingUpdateDTO;
import com.slow.pojo.vo.UserSettingVO;
import com.slow.service.service.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 设置 Controller
 */
@Tag(name = "设置管理", description = "用户设置查询与修改")
@RestController
@RequestMapping("/settings")
public class SettingController {

    @Autowired
    private SettingService settingService;

    /**
     * 查询当前用户设置
     */
    @Operation(summary = "查询用户设置", description = "返回当前登录用户的设置信息")
    @GetMapping
    public Result<UserSettingVO> getSetting() {
        UserSettingVO userSettingVO = settingService.getSetting();
        return Result.success(userSettingVO);
    }

    /**
     * 修改当前用户设置
     */
    @Operation(summary = "修改用户设置", description = "修改当前登录用户的设置信息")
    @PutMapping
    public Result<Object> updateSetting(@RequestBody UserSettingUpdateDTO updateDTO) {
        settingService.updateSetting(updateDTO);
        return Result.success("修改成功", null);
    }
}
