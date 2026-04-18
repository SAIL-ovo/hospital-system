package com.example.service;

import com.example.entity.HealthAssessmentQuestion;
import java.util.List;

public interface HealthAssessmentQuestionService {

    /**
     * 按类型查询题目
     */
    List<HealthAssessmentQuestion> getQuestionsByType(String type);

    /**
     * 查询所有题目
     */
    List<HealthAssessmentQuestion> getAllQuestions();

    /**
     * 新增题目
     */
    boolean addQuestion(HealthAssessmentQuestion question);

    /**
     * 更新题目
     */
    boolean updateQuestion(HealthAssessmentQuestion question);

    /**
     * 删除题目
     */
    boolean deleteQuestion(Long id);
}