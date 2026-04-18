package com.example.controller;

import com.example.common.Result;
import com.example.entity.Consultation;
import com.example.service.ConsultationService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/consultation")
public class ConsultationController {
    @Resource
    private ConsultationService consultationService;

    // 新增问诊：用户发起
    @PostMapping("/add")
    public Result add(@RequestBody Consultation consultation) {
        consultationService.addConsultation(consultation);
        return Result.success("问诊发起成功，等待医生回复！");
    }

    // 用户查询自身问诊：分页+状态筛选
    @GetMapping("/user/selectPage")
    public Result selectByUserId(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam Integer userId,
                                 @RequestParam(required = false) Integer status) {
        PageInfo<Consultation> pageInfo = consultationService.selectByUserId(pageNum, pageSize, userId, status);
        return Result.success(pageInfo);
    }

    // 医生查询自身问诊：分页+状态筛选
    @GetMapping("/doctor/selectPage")
    public Result selectByDoctorId(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam Integer doctorId,
                                   @RequestParam(required = false) Integer status) {
        PageInfo<Consultation> pageInfo = consultationService.selectByDoctorId(pageNum, pageSize, doctorId, status);
        return Result.success(pageInfo);
    }

    // 管理员查询所有问诊：多条件筛选
    @GetMapping("/admin/selectPage")
    public Result selectAll(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize,
                            @RequestParam(required = false) Integer userId,
                            @RequestParam(required = false) Integer doctorId,
                            @RequestParam(required = false) Integer status) {
        PageInfo<Consultation> pageInfo = consultationService.selectAll(pageNum, pageSize, userId, doctorId, status);
        return Result.success(pageInfo);
    }

    // 根据ID查问诊详情
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        return Result.success(consultationService.selectById(id));
    }

    // 医生回复问诊
    @PutMapping("/reply")
    public Result reply(@RequestParam Integer id, @RequestParam String replyContent) {
        consultationService.replyConsultation(id, replyContent);
        return Result.success("回复成功！");
    }

    // 医生统计待回复数量
    @GetMapping("/doctor/countWait/{doctorId}")
    public Result countWaitReply(@PathVariable Integer doctorId) {
        return Result.success(consultationService.countWaitReply(doctorId));
    }
}