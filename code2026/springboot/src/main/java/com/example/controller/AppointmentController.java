package com.example.controller;

import com.example.common.Result;
import com.example.entity.Appointment;
import com.example.service.AppointmentService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.List;
import java.util.Map;

/**
 * 预约管理接口
 */
@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    @Resource
    private AppointmentService appointmentService;

    /**
     * 新增预约（用户提交）
     */
    @PostMapping("/add")
    public Result add(@RequestBody Appointment appointment) {
        // 确保 appointment.getDate() 是 "2026-03-08"
        System.out.println("接收到的日期: " + appointment.getDate());
        return appointmentService.add(appointment);
    }

    /**
     * 删除预约
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        appointmentService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改预约（如取消、确认）
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Appointment appointment) {
        appointmentService.updateById(appointment);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Appointment appointment = appointmentService.selectById(id);
        return Result.success(appointment);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Appointment appointment) {
        List<Appointment> list = appointmentService.selectAll(appointment);
        return Result.success(list);
    }

    /**
     * 分页查询（后台管理用）
     */
    @GetMapping("/selectPage")
    public Result selectPage(Appointment appointment,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Appointment> page = appointmentService.selectPage(appointment, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 医生查询当日预约
     */
    @GetMapping("/today/{doctorId}")
    public Result getTodayAppointment(@PathVariable Integer doctorId) {
        List<Appointment> list = appointmentService.getTodayAppointment(doctorId);
        return Result.success(list);
    }

    /**
     * 更新预约状态
     */
    @PutMapping("/status/{id}")
    public Result updateStatus(@PathVariable Integer id, @RequestBody Map<String, String> params) {
        String status = params.get("status");
        if (status == null || status.isEmpty()) {
            return Result.error("状态不能为空");
        }
        appointmentService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    /**
     * 统计今日预约数
     */
    @GetMapping("/countToday")
    public Result countToday() {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        int count = appointmentService.countByDate(today);
        return Result.success(count);
    }

}