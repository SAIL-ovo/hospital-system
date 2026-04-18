package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Account;
import com.example.entity.Appointment;
import com.example.exception.CustomException;
import com.example.mapper.AppointmentMapper;
import com.example.common.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Date;

@Service
public class AppointmentService {

    @Resource
    private AppointmentMapper appointmentMapper;

    /**
     * 新增预约（修复返回值类型 + 日期处理）
     */
    public Result add(Appointment appointment) {
        try {
            // 1. 打印接收到的日期，排查问题
            System.out.println("Service层接收到的日期: " + appointment.getDate());

            // 2. 设置默认状态为“待确认”
            appointment.setStatus("待确认");

            // 3. 插入数据库
            appointmentMapper.insert(appointment);

            // 4. 返回成功结果
            return Result.success("预约添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("预约添加失败：" + e.getMessage());
        }
    }

    /**
     * 删除预约
     */
    public void deleteById(Integer id) {
        appointmentMapper.deleteById(id);
    }

    /**
     * 修改预约
     */
    public void updateById(Appointment appointment) {
        appointmentMapper.updateById(appointment);
    }

    /**
     * 根据ID查询
     */
    public Appointment selectById(Integer id) {
        return appointmentMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Appointment> selectAll(Appointment appointment) {
        return appointmentMapper.selectAll(appointment);
    }

    /**
     * 分页查询
     */
    public PageInfo<Appointment> selectPage(Appointment appointment, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Appointment> list = appointmentMapper.selectAll(appointment);
        return PageInfo.of(list);
    }

    /**
     * 查询医生当日预约
     */
    public List<Appointment> getTodayAppointment(Integer doctorId) {
        return appointmentMapper.selectTodayAppointment(doctorId);
    }

    /**
     * 更新预约状态
     */
    public void updateStatus(Integer id, String status) {
        appointmentMapper.updateStatus(id, status);
    }

    /**
     * 根据日期统计预约数量（添加public修饰符 + 实现方法）
     */
    public int countByDate(String date) {
        return appointmentMapper.countByDate(date);
    }
}