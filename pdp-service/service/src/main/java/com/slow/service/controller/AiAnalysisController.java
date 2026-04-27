package com.slow.service.controller;

import com.slow.common.result.PageResult;
import com.slow.common.result.Result;
import com.slow.pojo.dto.AiAnalysisGenerateDTO;
import com.slow.pojo.vo.AiAnalysisListVO;
import com.slow.pojo.vo.AiAnalysisVO;
import com.slow.service.service.AiAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * AI 分析 Controller
 */
@Tag(name = "AI 智能分析", description = "AI 个人发展分析报告生成与历史记录管理")
@RestController
@RequestMapping("/ai-analysis")
public class AiAnalysisController {

    @Autowired
    private AiAnalysisService aiAnalysisService;

    /**
     * 生成 AI 分析报告
     */
    @Operation(summary = "生成 AI 分析报告", description = "调用 AI 服务分析学生综合数据，生成发展报告并保存")
    @PostMapping
    public Result<AiAnalysisVO> generate(@RequestBody AiAnalysisGenerateDTO generateDTO) {
        AiAnalysisVO vo = aiAnalysisService.generate(generateDTO);
        return Result.success("分析成功", vo);
    }

    /**
     * 分析历史列表查询
     */
    @Operation(summary = "分析历史列表", description = "查询 AI 分析历史记录，分页展示")
    @GetMapping
    public Result<PageResult> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult pageResult = aiAnalysisService.list(page, pageSize);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询分析详情
     */
    @Operation(summary = "查看分析详情", description = "根据分析记录ID查看完整的 AI 分析报告")
    @GetMapping("/{id}")
    public Result<AiAnalysisVO> getById(@Parameter(description = "分析记录ID") @PathVariable Long id) {
        AiAnalysisVO vo = aiAnalysisService.getById(id);
        return Result.success(vo);
    }

    /**
     * 删除分析记录
     */
    @Operation(summary = "删除分析记录", description = "根据ID删除 AI 分析历史记录")
    @DeleteMapping("/{id}")
    public Result<Object> delete(@Parameter(description = "分析记录ID") @PathVariable Long id) {
        aiAnalysisService.delete(id);
        return Result.success("删除成功", null);
    }
}
