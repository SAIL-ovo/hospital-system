package com.example.mapper;

import com.example.entity.HealthAssessmentQuestion;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface HealthAssessmentQuestionMapper {

    /**
     * 按类型查询题目（用户端用）
     */
    @Select("SELECT * FROM health_assessment_question WHERE type = #{type} ORDER BY sort ASC")
    List<HealthAssessmentQuestion> selectByType(@Param("type") String type);

    /**
     * 查询所有题目（管理员用）
     */
    @Select("SELECT * FROM health_assessment_question ORDER BY type ASC, sort ASC")
    List<HealthAssessmentQuestion> selectAll();

    /**
     * 新增题目
     */
    @Insert("INSERT INTO health_assessment_question (type, question, options, sort, create_time) " +
            "VALUES (#{type}, #{question}, #{options}, #{sort}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id") // 自动返回主键ID
    int insert(HealthAssessmentQuestion question);

    /**
     * 更新题目
     */
    @Update("UPDATE health_assessment_question SET type=#{type}, question=#{question}, " +
            "options=#{options}, sort=#{sort} WHERE id=#{id}")
    int updateById(HealthAssessmentQuestion question);

    /**
     * 删除题目
     */
    @Delete("DELETE FROM health_assessment_question WHERE id=#{id}")
    int deleteById(@Param("id") Long id);
}