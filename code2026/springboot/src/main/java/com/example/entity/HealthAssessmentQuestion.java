package com.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 健康自评问卷题目表
 */
public class HealthAssessmentQuestion {
    /** 题目ID */
    private Long id;
    /** 自评类型：HYPERTENSION/DIABETES/GENERAL */
    private String type;
    /** 题目内容 */
    private String question;
    /** 选项（JSON格式） */
    private String options;
    /** 题目排序 */
    private Integer sort;
    /** 创建时间 */
    private LocalDateTime createTime;

    // Getter & Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}