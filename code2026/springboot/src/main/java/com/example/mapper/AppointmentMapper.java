package com.example.mapper;

import com.example.entity.Appointment;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface AppointmentMapper {

    /**
     * 新增预约
     */
    int insert(Appointment appointment);

    /**
     * 删除预约
     */
    int deleteById(Integer id);

    /**
     * 修改预约
     */
    int updateById(Appointment appointment);

    /**
     * 根据ID查询
     */
    Appointment selectById(Integer id);

    /**
     * 查询所有
     */
    List<Appointment> selectAll(Appointment appointment);

    /**
     *查询医生当日预约
     */
    List<Appointment> selectTodayAppointment(@Param("doctorId") Integer doctorId);

    /**
     *更新预约状态
     */
    int updateStatus(@Param("id") Integer id, @Param("status") String status);

    //根据日期统计预约数
    int countByDate(String date);
}