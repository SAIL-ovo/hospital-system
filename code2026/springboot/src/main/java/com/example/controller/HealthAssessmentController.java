package com.example.controller;
import com.example.common.Result;
import com.example.entity.HealthAssessmentQuestion;
import com.example.entity.UserHealthAssessmentRecord;
import com.example.service.HealthAssessmentQuestionService;
import com.example.service.UserHealthAssessmentRecordService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * 健康自评与筛查控制器（纯 MyBatis 版本）
 */
@RestController
@RequestMapping("/health/assessment")
public class HealthAssessmentController {
    @Resource
    private HealthAssessmentQuestionService questionService;
    @Resource
    private UserHealthAssessmentRecordService recordService;

    // 【原有接口保留，无需修改】
    @GetMapping("/questions/{type}")
    public Result getQuestions(@PathVariable String type) {
        List<HealthAssessmentQuestion> questions = questionService.getQuestionsByType(type);
        return Result.success(questions);
    }
    @GetMapping("/questions/all")
    public Result getAllQuestions() {
        List<HealthAssessmentQuestion> questions = questionService.getAllQuestions();
        return Result.success(questions);
    }
    @PostMapping("/questions/add")
    public Result addQuestion(@RequestBody HealthAssessmentQuestion question) {
        boolean success = questionService.addQuestion(question);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }
    @PostMapping("/questions/update")
    public Result updateQuestion(@RequestBody HealthAssessmentQuestion question) {
        boolean success = questionService.updateQuestion(question);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }
    @DeleteMapping("/questions/{id}")
    public Result deleteQuestion(@PathVariable Long id) {
        boolean success = questionService.deleteQuestion(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 保存用户自评记录【核心修改：注释硬编码校验+适配Long】
     */
    @PostMapping("/record")
    public Result saveRecord(@Valid @RequestBody UserHealthAssessmentRecord record) {
        // 临时注释硬编码校验（实际项目替换为：从Token/Session获取当前登录用户ID）
        // Integer currentLoginUserId = 1001;
        // if (!currentLoginUserId.equals(record.getUserId())) {
        //     return Result.error("无权提交其他用户的自评记录");
        // }
        Long recordId = recordService.saveAssessmentRecord(record);
        // 新增：返回完整记录而非仅ID，方便前端弹窗展示风险等级/建议
        UserHealthAssessmentRecord result = new UserHealthAssessmentRecord();
        result.setId(recordId);
        result.setRiskLevel(record.getRiskLevel());
        result.setSuggestion(record.getSuggestion());
        return Result.success(result);
    }

    /**
     * 根据用户ID查询自评记录【修改：入参改为Long+注释硬编码校验】
     */
    @GetMapping("/records/{userId}")
    public Result getRecords(@PathVariable Long userId) {
        // 临时注释硬编码校验（实际项目替换为：从Token/Session获取当前登录用户ID）
        // Integer currentLoginUserId = 1001;
        // if (!currentLoginUserId.equals(userId)) {
        //     return Result.error("无权查询其他用户的自评记录");
        // }
        List<UserHealthAssessmentRecord> records = recordService.getRecordsByUserId(userId);
        return Result.success(records);
    }
}