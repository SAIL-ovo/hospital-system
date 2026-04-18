package com.example.controller;

import com.example.common.Result;
import com.example.entity.Doctor;
import com.example.entity.Appointment;
import com.example.service.DoctorService;
import com.example.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 医生控制器
 */
@RestController
@RequestMapping("/doctor")
@CrossOrigin
public class DoctorController {

    @Autowired
    private DoctorService doctorService;
    @Autowired
    private AppointmentService appointmentService;

    /**
     * 新增医生
     */
    @PostMapping("/add")
    public Result addDoctor(@RequestBody Doctor doctor) {
        try {
            boolean success = doctorService.addDoctor(doctor);
            if (success) {
                return Result.success("医生添加成功");
            } else {
                return Result.error("医生添加失败");
            }
        } catch (Exception e) {
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 删除医生
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteDoctor(@PathVariable Integer id) {
        boolean success = doctorService.deleteDoctor(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 修改医生
     */
    @PutMapping("/update")
    public Result updateDoctor(@RequestBody Doctor doctor) {
        boolean success = doctorService.updateDoctor(doctor);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }

    /**
     * 根据ID查询医生
     */
    @GetMapping("/get/{id}")
    public Result getDoctorById(@PathVariable Integer id) {
        Doctor doctor = doctorService.getDoctorById(id);
        return Result.success(doctor); // 直接返回医生对象
    }

    /**
     * 查询医生列表
     */
    @PostMapping("/list")
    public Result getDoctorList(@RequestBody Doctor doctor) {
        List<Doctor> list = doctorService.getDoctorList(doctor);
        return Result.success(list); // 直接返回列表
    }

    /**
     * 医生登录
     */
    @PostMapping("/login")
    public Result doctorLogin(@RequestParam String name, @RequestParam String password) {
        Doctor doctor = doctorService.doctorLogin(name, password);
        if (doctor != null) {
            doctor.setPassword(null); // 隐藏密码
            return Result.success(doctor);
        } else {
            return Result.error("用户名或密码错误，或医生已离职");
        }
    }

    /**
     * 分页查询医生
     */
    @GetMapping("/selectPage")
    public Result selectPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        Map<String, Object> pageData = doctorService.getDoctorPage(pageNum, pageSize, name);
        return Result.success(pageData);
    }

    /**
     * 医生查询当日预约列表
     */
    @GetMapping("/appointment/today")
    public Result getTodayAppointment(@RequestParam Integer doctorId) {
        try {
            List<Appointment> appointmentList = appointmentService.getTodayAppointment(doctorId);
            return Result.success(appointmentList);
        } catch (Exception e) {
            return Result.error("查询当日预约失败：" + e.getMessage());
        }
    }

    /**
     * 医生更新预约状态
     */
    @PutMapping("/appointment/status/{id}")
    public Result updateAppointmentStatus(@PathVariable Integer id, @RequestBody Map<String, String> params) {
        try {
            appointmentService.updateStatus(id, params.get("status"));
            return Result.success("预约状态更新成功");
        } catch (Exception e) {
            return Result.error("更新状态失败：" + e.getMessage());
        }
    }

}