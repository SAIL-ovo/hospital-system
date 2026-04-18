package com.example.service;

import com.example.entity.MedicalRecord;
import com.example.mapper.MedicalRecordMapper;
import com.example.common.Result;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MedicalRecordService {
    @Resource
    private MedicalRecordMapper medicalRecordMapper;

    public Result add(MedicalRecord record) {
        try {
            medicalRecordMapper.insert(record);
            return Result.success("病历创建成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("病历创建失败: " + e.getMessage());
        }
    }

    public Result update(MedicalRecord record) {
        try {
            medicalRecordMapper.update(record);
            return Result.success("病历更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("病历更新失败: " + e.getMessage());
        }
    }

    public MedicalRecord getByAppointmentId(Integer appointmentId) {
        return medicalRecordMapper.selectByAppointmentId(appointmentId);
    }

    public List<MedicalRecord> getByDoctorId(Integer doctorId) {
        return medicalRecordMapper.selectByDoctorId(doctorId);
    }

    public List<MedicalRecord> selectByUserId(Integer userId) {
        return medicalRecordMapper.selectByUserId(userId);
    }

    /**
     * 根据医生ID和日期统计病历数量
     */
    public int countByDoctorAndDate(Long doctorId, String date) {
        return medicalRecordMapper.countByDoctorAndDate(doctorId, date);
    }

    //根据ID查询病历
    public MedicalRecord selectById(Integer id) {
        return medicalRecordMapper.selectById(id);
    }



}