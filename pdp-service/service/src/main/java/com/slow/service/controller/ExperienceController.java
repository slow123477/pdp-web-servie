package com.slow.service.controller;

import com.slow.common.result.PageResult;
import com.slow.common.result.Result;
import com.slow.pojo.dto.ExperienceAddDTO;
import com.slow.pojo.dto.ExperienceQueryDTO;
import com.slow.pojo.dto.ExperienceUpdateDTO;
import com.slow.pojo.vo.ExperienceVO;
import com.slow.service.service.ExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 经历 Controller
 */
@Tag(name = "经历管理", description = "经历的增删改查、条件筛选与分页查询")
@RestController
@RequestMapping("/experiences")
public class ExperienceController {

    @Autowired
    private ExperienceService experienceService;

    /**
     * 经历列表查询（条件分页）
     */
    @Operation(summary = "经历列表查询", description = "支持按类型筛选，分页返回")
    @GetMapping
    public Result<PageResult> list(ExperienceQueryDTO queryDTO) {
        PageResult pageResult = experienceService.list(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询经历
     */
    @Operation(summary = "根据ID查询经历", description = "查询指定ID的经历详情")
    @GetMapping("/{id}")
    public Result<ExperienceVO> getById(@Parameter(description = "经历ID") @PathVariable Long id) {
        ExperienceVO experienceVO = experienceService.getById(id);
        return Result.success(experienceVO);
    }

    /**
     * 添加经历
     */
    @Operation(summary = "添加经历", description = "新增一条经历记录")
    @PostMapping
    public Result<Object> add(@RequestBody @Valid ExperienceAddDTO addDTO) {
        experienceService.add(addDTO);
        return Result.success("添加成功", null);
    }

    /**
     * 修改经历
     */
    @Operation(summary = "修改经历", description = "修改已有经历的信息")
    @PutMapping
    public Result<Object> update(@RequestBody @Valid ExperienceUpdateDTO updateDTO) {
        experienceService.update(updateDTO);
        return Result.success("修改成功", null);
    }

    /**
     * 删除经历
     */
    @Operation(summary = "删除经历", description = "根据ID删除经历")
    @DeleteMapping("/{id}")
    public Result<Object> delete(@Parameter(description = "经历ID") @PathVariable Long id) {
        experienceService.delete(id);
        return Result.success("删除成功", null);
    }
}
