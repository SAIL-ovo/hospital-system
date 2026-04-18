package com.example.service;

import com.example.entity.Consultation;
import com.example.exception.CustomException;
import com.example.mapper.ConsultationMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultationService {
    @Resource
    private ConsultationMapper consultationMapper;

    // 1. 用户发起问诊
    public void addConsultation(Consultation consultation) {
        // 校验：内容不能为空
        if (!StringUtils.hasText(consultation.getContent())) {
            throw new CustomException("问诊内容不能为空！");
        }
        // 校验：医生ID/用户ID不能为空
        if (consultation.getDoctorId() == null || consultation.getUserId() == null) {
            throw new CustomException("参数异常，请重新选择！");
        }
        consultation.setStatus(0); // 默认为待回复
        consultationMapper.insert(consultation);
    }

    // 2. 用户分页查询自身问诊（支持按状态筛选）
    public PageInfo<Consultation> selectByUserId(Integer pageNum, Integer pageSize, Integer userId, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        List<Consultation> list = consultationMapper.selectByUserId(userId, status);
        return PageInfo.of(list);
    }

    // 3. 医生分页查询自身问诊（支持按状态筛选）
    public PageInfo<Consultation> selectByDoctorId(Integer pageNum, Integer pageSize, Integer doctorId, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        List<Consultation> list = consultationMapper.selectByDoctorId(doctorId, status);
        return PageInfo.of(list);
    }

    // 4. 管理员分页查询所有问诊（支持多条件筛选）
    public PageInfo<Consultation> selectAll(Integer pageNum, Integer pageSize, Integer userId, Integer doctorId, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        List<Consultation> list = consultationMapper.selectAll(userId, doctorId, status);
        return PageInfo.of(list);
    }

    // 5. 根据ID查询问诊详情
    public Consultation selectById(Integer id) {
        if (id == null) {
            throw new CustomException("问诊ID不能为空！");
        }
        return consultationMapper.selectById(id);
    }

    // 6. 医生回复问诊
    public void replyConsultation(Integer id, String replyContent) {
        // 校验
        if (id == null || !StringUtils.hasText(replyContent)) {
            throw new CustomException("回复内容不能为空！");
        }
        Consultation consultation = selectById(id);
        if (consultation == null) {
            throw new CustomException("问诊记录不存在！");
        }
        if (consultation.getStatus() != 0) {
            throw new CustomException("该问诊已回复，无需重复操作！");
        }
        // 格式化回复时间（与现有系统时间格式一致）
        String replyTime = LocalDateTime.now().toString().replace("T", " ");
        consultationMapper.updateReply(id, replyContent, replyTime, 1); // 状态改为已回复
    }

    // 7. 医生统计待回复问诊数量（用于医生首页提醒）
    public int countWaitReply(Integer doctorId) {
        return consultationMapper.countByCondition(doctorId, 0);
    }
}