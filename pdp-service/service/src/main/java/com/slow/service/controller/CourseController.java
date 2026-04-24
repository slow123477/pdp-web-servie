package com.slow.service.controller;

import com.slow.common.result.PageResult;
import com.slow.common.result.Result;
import com.slow.pojo.dto.CourseAddDTO;
import com.slow.pojo.dto.CourseQueryDTO;
import com.slow.pojo.dto.CourseUpdateDTO;
import com.slow.pojo.vo.CourseVO;
import com.slow.service.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 课程 Controller
 */
@Tag(name = "课程管理", description = "课程的增删改查、条件筛选与分页查询")
@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 课程列表查询（条件分页）
     */
    @Operation(summary = "课程列表查询", description = "支持按学期、课程类型筛选，分页返回")
    @GetMapping
    public Result<PageResult> list(CourseQueryDTO queryDTO) {
        PageResult pageResult = courseService.list(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询课程
     */
    @Operation(summary = "根据ID查询课程", description = "查询指定ID的课程详情")
    @GetMapping("/{id}")
    public Result<CourseVO> getById(@Parameter(description = "课程ID") @PathVariable Long id) {
        CourseVO courseVO = courseService.getById(id);
        return Result.success(courseVO);
    }

    /**
     * 添加课程
     */
    @Operation(summary = "添加课程", description = "新增一门课程记录")
    @PostMapping
    public Result<Object> add(@RequestBody @Valid CourseAddDTO addDTO) {
        courseService.add(addDTO);
        return Result.success("添加成功", null);
    }

    /**
     * 修改课程
     */
    @Operation(summary = "修改课程", description = "修改已有课程的信息")
    @PutMapping
    public Result<Object> update(@RequestBody @Valid CourseUpdateDTO updateDTO) {
        courseService.update(updateDTO);
        return Result.success("修改成功", null);
    }

    /**
     * 删除课程
     */
    @Operation(summary = "删除课程", description = "根据ID删除课程")
    @DeleteMapping("/{id}")
    public Result<Object> delete(@Parameter(description = "课程ID") @PathVariable Long id) {
        courseService.delete(id);
        return Result.success("删除成功", null);
    }
}
