package com.example.entity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
/**
 * 用户健康自评记录表
 */
public class UserHealthAssessmentRecord {
    /** 记录ID */
    private Long id;
    /** 用户ID（关联user表，改为Long匹配数据库bigint） */
    private Long userId;
    /** 自评类型（数据库字段assessment_type，加注解映射） */
    private String type;
    /** 总分（数据库total_score） */
    private Integer totalScore;
    /** 风险等级：LOW/MEDIUM/HIGH */
    private String riskLevel;
    /** 健康建议 */
    private String suggestion;
    /** 测评时间（映射数据库create_time） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assessmentTime;

    // Getter & Setter 方法（全量保留，仅修改userId类型为Long）
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    public LocalDateTime getAssessmentTime() { return assessmentTime; }
    public void setAssessmentTime(LocalDateTime assessmentTime) { this.assessmentTime = assessmentTime; }
}