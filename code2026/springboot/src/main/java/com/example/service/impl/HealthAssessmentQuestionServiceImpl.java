package com.example.service.impl;

import com.example.entity.HealthAssessmentQuestion;
import com.example.mapper.HealthAssessmentQuestionMapper;
import com.example.service.HealthAssessmentQuestionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HealthAssessmentQuestionServiceImpl implements HealthAssessmentQuestionService {

    @Resource
    private HealthAssessmentQuestionMapper questionMapper;

    @Override
    public List<HealthAssessmentQuestion> getQuestionsByType(String type) {
        return questionMapper.selectByType(type);
    }

    @Override
    public List<HealthAssessmentQuestion> getAllQuestions() {
        return questionMapper.selectAll();
    }

    @Override
    public boolean addQuestion(HealthAssessmentQuestion question) {
        // insert 方法返回受影响行数，>0 表示成功
        return questionMapper.insert(question) > 0;
    }

    @Override
    public boolean updateQuestion(HealthAssessmentQuestion question) {
        return questionMapper.updateById(question) > 0;
    }

    @Override
    public boolean deleteQuestion(Long id) {
        return questionMapper.deleteById(id) > 0;
    }
}