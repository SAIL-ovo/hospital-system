package com.example.mapper;
import com.example.entity.UserHealthAssessmentRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Options;
import java.util.List;
/**
 * 用户健康自评记录Mapper
 */
@Mapper
public interface UserHealthAssessmentRecordMapper {
    /**
     * 保存自评记录（返回自增ID，匹配数据库字段+补create_time+加主键回显）
     */
    @Insert("INSERT INTO user_health_assessment_record (user_id, assessment_type, total_score, risk_level, suggestion, create_time) " +
            "VALUES (#{userId}, #{type}, #{totalScore}, #{riskLevel}, #{suggestion}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id") // 新增：返回自增主键ID到实体
    int insert(UserHealthAssessmentRecord record);

    /**
     * 根据用户ID查询所有记录（按测评时间降序，字段映射）
     */
    @Select("SELECT id, user_id as userId, assessment_type as type, total_score as totalScore, " +
            "risk_level as riskLevel, suggestion, create_time as assessmentTime " +
            "FROM user_health_assessment_record WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<UserHealthAssessmentRecord> selectByUserId(Long userId); // 入参改为Long
}