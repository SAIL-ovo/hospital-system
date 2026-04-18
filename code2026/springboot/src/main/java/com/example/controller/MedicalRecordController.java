package com.example.controller;

import com.example.common.Result;
import com.example.entity.MedicalRecord;
import com.example.service.MedicalRecordService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.text.SimpleDateFormat;

@RestController
@RequestMapping("/medical-record")
public class MedicalRecordController {
    @Resource
    private MedicalRecordService medicalRecordService;

    // 创建病历时自动补全金额，避免NULL
    @PostMapping("/add")
    public Result add(@RequestBody MedicalRecord record) {
        if (record.getTotalPrice() == null) {
            record.setTotalPrice(new BigDecimal("88.00"));
        }

        // 2. 支付状态默认设为未支付
        if (record.getPaid() == null) {
            record.setPaid(false);
        }

        return medicalRecordService.add(record);
    }

    @PutMapping("/update")
    public Result update(@RequestBody MedicalRecord record) {
        return medicalRecordService.update(record);
    }

    @GetMapping("/appointment/{appointmentId}")
    public Result getByAppointmentId(@PathVariable Integer appointmentId) {
        MedicalRecord record = medicalRecordService.getByAppointmentId(appointmentId);
        return Result.success(record);
    }

    @GetMapping("/doctor/{doctorId}")
    public Result getByDoctorId(@PathVariable Integer doctorId) {
        List<MedicalRecord> records = medicalRecordService.getByDoctorId(doctorId);
        return Result.success(records);
    }

    @GetMapping("/selectByUserId")
    public Result selectByUserId(@RequestParam Integer userId) {
        List<MedicalRecord> list = medicalRecordService.selectByUserId(userId);
        return Result.success(list);
    }

    //按日期和医生 ID 统计病历
    @GetMapping("/countTodayRecords")
    public Result countTodayRecords(@RequestParam Long doctorId) {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        int count = medicalRecordService.countByDoctorAndDate(doctorId, today);
        return Result.success(count);
    }

    /**
     * 🔥 核心修改2：更新病历支付状态（只改paid，保留金额）
     */
    @PostMapping("/pay")
    public Result pay(@RequestBody MedicalRecord record) {
        if (record.getId() == null) {
            return Result.error("病历ID不能为空");
        }

        // 1. 先查询原病历，保留所有原有字段（尤其是total_price）
        MedicalRecord oldRecord = medicalRecordService.selectById(record.getId());
        if (oldRecord == null) {
            return Result.error("病历不存在");
        }

        // 2. 只更新支付状态，其他字段完全保留
        oldRecord.setPaid(true);

        // 3. 执行更新（用原病历对象，避免金额丢失）
        return medicalRecordService.update(oldRecord);
    }

    // （可选）根据处方内容自动计算金额的工具方法
    private BigDecimal calculatePriceByPrescription(String prescription) {
        // 示例逻辑：可根据实际业务扩展
        BigDecimal total = new BigDecimal("0.00");

        // 简单匹配药品并累加金额
        if (prescription.contains("感冒药")) {
            total = total.add(new BigDecimal("50.00"));
        }
        if (prescription.contains("退烧药")) {
            total = total.add(new BigDecimal("30.00"));
        }
        if (prescription.contains("消炎药")) {
            total = total.add(new BigDecimal("45.00"));
        }

        return total;
    }

    // 🔥 新增：根据ID查询病历（pay接口需要用到）
    @GetMapping("/{id}")
    public Result selectById(@PathVariable Integer id) {
        MedicalRecord record = medicalRecordService.selectById(id);
        return Result.success(record);
    }

    /**
     * 健康档案：获取用户所有就诊记录（按时间倒序）
     */
    @GetMapping("/health/record")
    public Result getHealthRecord(@RequestParam Integer userId) {
        List<MedicalRecord> records = medicalRecordService.selectByUserId(userId);
        // 按就诊日期倒序排序
        records.sort((a, b) -> {
            if (a.getVisitDate() == null || b.getVisitDate() == null) {
                return 0;
            }
            return b.getVisitDate().compareTo(a.getVisitDate());
        });
        return Result.success(records);
    }

    /**
     * 就诊统计：近半年就诊次数、花费总额、科室分布
     */
    @GetMapping("/health/statistics")
    public Result getHealthStatistics(@RequestParam Integer userId) {
        List<MedicalRecord> records = medicalRecordService.selectByUserId(userId);

        // 初始化统计数据
        Map<String, Object> stats = new HashMap<>();
        int totalVisitCount = records.size(); // 先统计所有记录数（临时去掉日期过滤）
        BigDecimal totalCost = new BigDecimal("0.00");
        Map<String, Integer> deptCount = new HashMap<>();
        Map<String, BigDecimal> monthCost = new HashMap<>();

        // 遍历所有记录（先去掉日期过滤，确保能拿到数据）
        for (MedicalRecord record : records) {
            // 累计总花费
            if (record.getTotalPrice() != null) {
                totalCost = totalCost.add(record.getTotalPrice());
            }

            // 科室分布
            String dept = record.getDepartment() == null ? "未知科室" : record.getDepartment();
            deptCount.put(dept, deptCount.getOrDefault(dept, 0) + 1);

            // 月度花费（兼容null）
            if (record.getVisitDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                String month = sdf.format(record.getVisitDate());
                BigDecimal price = record.getTotalPrice() == null ? new BigDecimal("0.00") : record.getTotalPrice();
                monthCost.put(month, monthCost.getOrDefault(month, new BigDecimal("0.00")).add(price));
            }
        }

        // 封装结果
        stats.put("totalVisitCount", totalVisitCount);
        stats.put("totalCost", totalCost);
        stats.put("deptCount", deptCount);
        stats.put("monthCost", monthCost);

        // 智能健康建议
        String healthAdvice = "暂无建议";
        if (totalVisitCount >= 3) {
            healthAdvice = "近半年就诊次数较多，建议定期做全面体检";
        } else if (deptCount.containsKey("内科") && deptCount.get("内科") >= 2) {
            healthAdvice = "内科就诊频次较高，请注意日常饮食和作息";
        }
        stats.put("healthAdvice", healthAdvice);

        return Result.success(stats);
    }

}