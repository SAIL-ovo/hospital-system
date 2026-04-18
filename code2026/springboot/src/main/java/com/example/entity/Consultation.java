package com.example.entity;

import java.time.LocalDateTime;

public class Consultation {
    private Integer id;
    private Integer userId;    // 用户ID
    private Integer doctorId;  // 医生ID
    private String content;    // 问诊内容
    private String imgUrls;    // 图片地址，逗号分隔
    private String replyContent; // 医生回复
    private LocalDateTime createTime; // 发起时间
    private LocalDateTime replyTime;  // 回复时间
    private Integer status;    // 0待回复 1已回复 2已关闭

    // 生成getter/setter方法（与User/Doctor类一致）
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getDoctorId() { return doctorId; }
    public void setDoctorId(Integer doctorId) { this.doctorId = doctorId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImgUrls() { return imgUrls; }
    public void setImgUrls(String imgUrls) { this.imgUrls = imgUrls; }
    public String getReplyContent() { return replyContent; }
    public void setReplyContent(String replyContent) { this.replyContent = replyContent; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getReplyTime() { return replyTime; }
    public void setReplyTime(LocalDateTime replyTime) { this.replyTime = replyTime; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}