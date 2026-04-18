package com.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * 医生实体类
 * 对应数据库 doctor 表
 */
public class Doctor implements Serializable {
    private static final long serialVersionUID = 1L;

    // 主键ID
    private Integer id;

    //医生账号
    private String username;

    // 医生姓名
    private String name;

    // 登录密码
    private String password;

    //角色
    private String role;

    // 所属科室
    private String department;

    // 医生职称
    private String title;

    // 医生头像
    private String avatar;

    // 医生简介
    private String intro;

    // 状态：1-在职 0-离职
    private Integer status;

    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    //修改密码
    private String newPassword;

    // getter/setter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public String getRole() {

        return role;
    }

    public void setRole(String role) {

        this.role = role;
    }

    public String getDepartment() {

        return department;
    }

    public void setDepartment(String department) {

        this.department = department;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {

        this.avatar = avatar;
    }

    public String getIntro() {

        return intro;
    }

    public void setIntro(String intro) {

        this.intro = intro;
    }

    public Integer getStatus() {

        return status;
    }

    public void setStatus(Integer status) {

        this.status = status;
    }

    public Date getCreateTime() {

        return createTime;
    }

    public void setCreateTime(Date createTime) {

        this.createTime = createTime;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    // 可选：toString方法，方便调试
    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
}