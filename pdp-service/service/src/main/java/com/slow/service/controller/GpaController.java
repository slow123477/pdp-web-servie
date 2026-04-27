package com.slow.service.controller;

import com.slow.common.result.Result;
import com.slow.pojo.vo.GpaVO;
import com.slow.pojo.vo.SemesterGpaVO;
import com.slow.service.service.GpaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * GPA 计算 Controller
 */
@Tag(name = "GPA计算", description = "GPA计算与趋势数据")
@RestController
@RequestMapping("/gpa")
public class GpaController {

    @Autowired
    private GpaService gpaService;

    /**
     * GPA 计算
     */
    @Operation(summary = "GPA计算", description = "根据用户课程计算累计GPA和各学期GPA")
    @GetMapping
    public Result<GpaVO> calculate(
            @Parameter(description = "GPA标准：4.0/5.0/weighted，默认使用用户设置")
            @RequestParam(required = false) String standard) {
        GpaVO gpaVO = gpaService.calculate(standard);
        return Result.success(gpaVO);
    }

    /**
     * GPA 趋势数据
     */
    @Operation(summary = "GPA趋势数据", description = "获取各学期GPA趋势数据")
    @GetMapping("/trend")
    public Result<List<SemesterGpaVO>> trend() {
        List<SemesterGpaVO> list = gpaService.trend();
        return Result.success(list);
    }
}
