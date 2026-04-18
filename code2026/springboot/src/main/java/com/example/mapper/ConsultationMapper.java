package com.example.mapper;

import com.example.entity.Consultation;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ConsultationMapper {
    // 新增问诊
    void insert(Consultation consultation);
    // 用户查询自己的问诊记录（可按状态筛选）
    List<Consultation> selectByUserId(@Param("userId") Integer userId, @Param("status") Integer status);
    // 医生查询自己的问诊（可按状态筛选）
    List<Consultation> selectByDoctorId(@Param("doctorId") Integer doctorId, @Param("status") Integer status);
    // 管理员查询所有问诊（可按条件筛选）
    List<Consultation> selectAll(@Param("userId") Integer userId, @Param("doctorId") Integer doctorId, @Param("status") Integer status);
    // 根据ID查询问诊详情
    Consultation selectById(Integer id);
    // 医生回复问诊（更新回复内容、回复时间、状态）
    void updateReply(@Param("id") Integer id, @Param("replyContent") String replyContent, @Param("replyTime") String replyTime, @Param("status") Integer status);
    // 统计数量（可选）
    int countByCondition(@Param("doctorId") Integer doctorId, @Param("status") Integer status);
}