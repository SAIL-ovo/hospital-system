package com.example.mapper;

import com.example.entity.MedicalRecord;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface MedicalRecordMapper {
    int insert(MedicalRecord record);
    int update(MedicalRecord record);
    int deleteById(Integer id);
    MedicalRecord selectById(Integer id);
    MedicalRecord selectByAppointmentId(@Param("appointmentId") Integer appointmentId);
    List<MedicalRecord> selectByDoctorId(@Param("doctorId") Integer doctorId);
    List<MedicalRecord> selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据医生ID和日期统计病历数量
     */
    int countByDoctorAndDate(@Param("doctorId") Long doctorId, @Param("date") String date);

    
}