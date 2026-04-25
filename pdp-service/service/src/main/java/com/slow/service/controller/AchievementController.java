package com.slow.service.controller;

import com.slow.common.result.PageResult;
import com.slow.common.result.Result;
import com.slow.pojo.dto.AchievementAddDTO;
import com.slow.pojo.dto.AchievementQueryDTO;
import com.slow.pojo.dto.AchievementUpdateDTO;
import com.slow.pojo.vo.AchievementVO;
import com.slow.service.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 成就 Controller
 */
@Tag(name = "成就管理", description = "成就的增删改查、条件筛选与分页查询")
@RestController
@RequestMapping("/achievements")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    /**
     * 成就列表查询（条件分页）
     */
    @Operation(summary = "成就列表查询", description = "支持按级别、分类筛选，分页返回")
    @GetMapping
    public Result<PageResult> list(AchievementQueryDTO queryDTO) {
        PageResult pageResult = achievementService.list(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询成就
     */
    @Operation(summary = "根据ID查询成就", description = "查询指定ID的成就详情")
    @GetMapping("/{id}")
    public Result<AchievementVO> getById(@Parameter(description = "成就ID") @PathVariable Long id) {
        AchievementVO achievementVO = achievementService.getById(id);
        return Result.success(achievementVO);
    }

    /**
     * 添加成就
     */
    @Operation(summary = "添加成就", description = "新增一条成就记录")
    @PostMapping
    public Result<Object> add(@RequestBody @Valid AchievementAddDTO addDTO) {
        achievementService.add(addDTO);
        return Result.success("添加成功", null);
    }

    /**
     * 修改成就
     */
    @Operation(summary = "修改成就", description = "修改已有成就的信息")
    @PutMapping
    public Result<Object> update(@RequestBody @Valid AchievementUpdateDTO updateDTO) {
        achievementService.update(updateDTO);
        return Result.success("修改成功", null);
    }

    /**
     * 删除成就
     */
    @Operation(summary = "删除成就", description = "根据ID删除成就")
    @DeleteMapping("/{id}")
    public Result<Object> delete(@Parameter(description = "成就ID") @PathVariable Long id) {
        achievementService.delete(id);
        return Result.success("删除成功", null);
    }
}
