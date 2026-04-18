package com.example.controller;

import com.example.common.Result;
import com.example.entity.MedicalRecord;
import com.example.service.MedicalRecordService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/invoice")
public class InvoiceController {
    @Resource
    private MedicalRecordService medicalRecordService;

    // 生成电子发票（仅校验病历存在+支付状态，不存数据库）
    @GetMapping("/generate/{recordId}")
    public Result generateInvoice(@PathVariable Integer recordId) {
        // 1. 核心校验1：病历是否存在
        MedicalRecord record = medicalRecordService.selectById(recordId);
        if (record == null) {
            return Result.error("病历不存在，无法生成电子发票");
        }

        // 2. 核心校验2：是否已支付（兼容tinyint类型）
        // 你的数据库中paid是tinyint(1)，0=未支付，1=已支付
        if (record.getPaid() == null || !record.getPaid()) {
            return Result.error("未支付，无法生成电子发票！请先完成支付");
        }

        // 3. 生成电子发票信息（仅返回前端展示，不存库）
        Map<String, Object> invoice = new HashMap<>();
        invoice.put("invoiceNo", "INV" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6));
        invoice.put("patientName", record.getPatientName());
        invoice.put("department", record.getDepartment());
        invoice.put("doctorName", record.getDoctorName());
        invoice.put("visitDate", record.getVisitDate()); // 你的表中是date类型，前端可直接展示
        invoice.put("totalPrice", record.getTotalPrice());
        invoice.put("createTime", new Date()); // 开票时间

        // 4. 返回成功结果
        return Result.success(invoice);
    }
}