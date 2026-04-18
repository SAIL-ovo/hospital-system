package com.example.service;
import com.example.entity.UserHealthAssessmentRecord;
import com.example.mapper.UserHealthAssessmentRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
/**
 * 用户健康自评记录服务
 */
@Service
public class UserHealthAssessmentRecordService {
    @Resource
    private UserHealthAssessmentRecordMapper recordMapper;

    /**
     * 保存自评记录（自动计算风险等级和建议，适配Long userId+补时间）
     */
    public Long saveAssessmentRecord(UserHealthAssessmentRecord record) {
        // 1. 校验必填参数
        if (record.getUserId() == null || record.getType() == null || record.getTotalScore() == null) {
            throw new IllegalArgumentException("用户ID、自评类型、总分不能为空");
        }
        // 2. 自动计算风险等级
        String riskLevel = calculateRiskLevel(record.getTotalScore(), record.getType());
        record.setRiskLevel(riskLevel);
        // 3. 自动生成健康建议
        String suggestion = generateSuggestion(riskLevel, record.getType());
        record.setSuggestion(suggestion);
        // 新增：设置测评时间（与数据库create_time同步）
        record.setAssessmentTime(LocalDateTime.now());
        // 4. 保存记录
        recordMapper.insert(record);
        return record.getId(); // 依赖Mapper的@Options返回主键
    }

    /**
     * 根据用户ID查询记录（入参改为Long）
     */
    public List<UserHealthAssessmentRecord> getRecordsByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须为正整数");
        }
        return recordMapper.selectByUserId(userId);
    }

    // 私有方法：计算风险等级（原逻辑保留，无需修改）
    private String calculateRiskLevel(Integer totalScore, String type) {
        if ("HYPERTENSION".equals(type)) {
            return totalScore <= 5 ? "LOW" : (totalScore <= 10 ? "MEDIUM" : "HIGH");
        } else if ("DIABETES".equals(type)) {
            return totalScore <= 4 ? "LOW" : (totalScore <= 8 ? "MEDIUM" : "HIGH");
        } else if ("GENERAL".equals(type)) {
            return totalScore <= 8 ? "LOW" : (totalScore <= 15 ? "MEDIUM" : "HIGH");
        } else {
            return "LOW";
        }
    }

    // 私有方法：生成健康建议（原逻辑保留，无需修改）
    private String generateSuggestion(String riskLevel, String type) {
        String typeName = getTypeName(type);
        if ("LOW".equals(riskLevel)) {
            return "你的" + typeName + "风险较低，建议保持当前健康习惯，定期体检。";
        } else if ("MEDIUM".equals(riskLevel)) {
            return "你的" + typeName + "风险中等，建议减少高盐高脂饮食，增加运动，1个月后复查。";
        } else if ("HIGH".equals(riskLevel)) {
            return "你的" + typeName + "风险较高，请及时就医，遵循医生指导调整生活方式！";
        } else {
            return "请结合自身情况，咨询专业医护人员。";
        }
    }

    // 辅助方法：类型转中文名称（原逻辑保留，无需修改）
    private String getTypeName(String type) {
        if ("HYPERTENSION".equals(type)) {
            return "高血压";
        } else if ("DIABETES".equals(type)) {
            return "糖尿病";
        } else if ("GENERAL".equals(type)) {
            return "综合健康";
        } else {
            return "健康";
        }
    }
}