package com.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.math.BigDecimal;

public class MedicalRecord {
    /**主键ID*/
    private Integer id;
    /**预约ID*/
    private Integer appointmentId;
    /**患者ID*/
    private Integer patientId;
    /**医生ID*/
    private Integer doctorId;
    /**患者姓名*/
    private String patientName;
    /**医生姓名*/
    private String doctorName;
    /**科室*/
    private String department;
    /**就诊日期*/
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date visitDate;
    /**主诉*/
    private String chiefComplaint;
    /**现病史*/
    private String historyOfPresentIllness;
    /**既往史*/
    private String pastHistory;
    /**体格检查*/
    private String physicalExamination;
    /**诊断*/
    private String diagnosis;
    /**治疗方案*/
    private String treatmentPlan;
    /**处方*/
    private String prescription;
    /**创建时间*/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdTime;
    /**总价*/
    private BigDecimal totalPrice;
    /**支付状态*/
    private Boolean paid;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }
    public Integer getDoctorId() {
        return doctorId;
    }
    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }
    public String getPatientName() {
        return patientName;
    }
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    public String getDoctorName() {
        return doctorName;
    }
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }
    public Date getVisitDate() {
        return visitDate;
    }
    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }
    public String getChiefComplaint() {
        return chiefComplaint;
    }
    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }
    public String getHistoryOfPresentIllness() {
        return historyOfPresentIllness;
    }
    public void setHistoryOfPresentIllness(String historyOfPresentIllness) {
        this.historyOfPresentIllness = historyOfPresentIllness;
    }
    public String getPastHistory() {
        return pastHistory;
    }
    public void setPastHistory(String pastHistory) {
        this.pastHistory = pastHistory;
    }
    public String getPhysicalExamination() {
        return physicalExamination;
    }
    public void setPhysicalExamination(String physicalExamination) {
        this.physicalExamination = physicalExamination;
    }
    public String getDiagnosis() {
        return diagnosis;
    }
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }
    public String getTreatmentPlan() {
        return treatmentPlan;
    }
    public void setTreatmentPlan(String treatmentPlan) {
        this.treatmentPlan = treatmentPlan;
    }
    public String getPrescription() {
        return prescription;
    }
    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }
    public Date getCreatedTime() {
        return createdTime;
    }
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

}