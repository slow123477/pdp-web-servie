package com.slow.service.controller;

import com.slow.common.result.Result;
import com.slow.pojo.vo.DashboardVO;
import com.slow.service.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘 Controller
 */
@Tag(name = "仪表盘", description = "首页仪表盘统计数据聚合查询")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取仪表盘聚合数据
     */
    @Operation(summary = "仪表盘数据", description = "获取首页仪表盘所有统计数据，包括GPA、学分、经历、成就、角色及最近动态")
    @GetMapping
    public Result<DashboardVO> getDashboardData() {
        DashboardVO vo = dashboardService.getDashboardData();
        return Result.success(vo);
    }
}
