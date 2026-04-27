package com.slow.service.controller;

import com.slow.common.result.PageResult;
import com.slow.common.result.Result;
import com.slow.pojo.dto.RoleAddDTO;
import com.slow.pojo.dto.RoleQueryDTO;
import com.slow.pojo.dto.RoleUpdateDTO;
import com.slow.pojo.vo.RoleVO;
import com.slow.service.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 角色 Controller
 */
@Tag(name = "角色管理", description = "角色的增删改查、条件筛选与分页查询")
@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 角色列表查询（条件分页）
     */
    @Operation(summary = "角色列表查询", description = "支持按在职状态筛选，分页返回")
    @GetMapping
    public Result<PageResult> list(RoleQueryDTO queryDTO) {
        PageResult pageResult = roleService.list(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询角色
     */
    @Operation(summary = "根据ID查询角色", description = "查询指定ID的角色详情")
    @GetMapping("/{id}")
    public Result<RoleVO> getById(@Parameter(description = "角色ID") @PathVariable Long id) {
        RoleVO roleVO = roleService.getById(id);
        return Result.success(roleVO);
    }

    /**
     * 添加角色
     */
    @Operation(summary = "添加角色", description = "新增一条角色记录")
    @PostMapping
    public Result<Object> add(@RequestBody @Valid RoleAddDTO addDTO) {
        roleService.add(addDTO);
        return Result.success("添加成功", null);
    }

    /**
     * 修改角色
     */
    @Operation(summary = "修改角色", description = "修改已有角色的信息")
    @PutMapping
    public Result<Object> update(@RequestBody @Valid RoleUpdateDTO updateDTO) {
        roleService.update(updateDTO);
        return Result.success("修改成功", null);
    }

    /**
     * 删除角色
     */
    @Operation(summary = "删除角色", description = "根据ID删除角色")
    @DeleteMapping("/{id}")
    public Result<Object> delete(@Parameter(description = "角色ID") @PathVariable Long id) {
        roleService.delete(id);
        return Result.success("删除成功", null);
    }
}
