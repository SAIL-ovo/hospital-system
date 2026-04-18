package com.example.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Account;
import com.example.entity.Doctor;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.DoctorMapper;
import com.example.service.DoctorService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorMapper doctorMapper;

    @Override
    public Doctor login(Account account) {
        // 调用Mapper查询（此时会包含avatar字段）
        Doctor doctor = doctorMapper.loginByUsernameAndPassword(
                account.getUsername(),
                account.getPassword()
        );

        // 新增日志：验证avatar是否查询到
        if (doctor != null) {
            System.out.println("登录医生信息：name=" + doctor.getName() + ", avatar=" + doctor.getAvatar());
        } else {
            System.out.println("医生登录失败：账号或密码错误");
        }
        return doctor;
    }


    @Override
    public boolean addDoctor(Doctor doctor) {
        return doctorMapper.insertDoctor(doctor) > 0;
    }

    @Override
    public boolean deleteDoctor(Integer id) {
        return doctorMapper.deleteDoctorById(id) > 0;
    }

    @Override
    public boolean updateDoctor(Doctor doctor) {
        System.out.println("前端传的原始密码：" + doctor.getPassword());
        System.out.println("前端传的新密码：" + doctor.getNewPassword());

        if (doctor.getPassword() != null && !doctor.getPassword().isEmpty()) {

        } else {
            doctor.setPassword(null);
            System.out.println("密码为空，不更新");
        }

        int rows = doctorMapper.updateDoctor(doctor);
        System.out.println("更新影响行数：" + rows); // 如果是 0，说明没匹配到数据
        return rows > 0;
    }

    @Override
    public Doctor getDoctorById(Integer id) {
        return doctorMapper.selectDoctorById(id);
    }

    @Override
    public List<Doctor> getDoctorList(Doctor doctor) {
        return doctorMapper.selectDoctorList(doctor);
    }

    @Override
    public Doctor doctorLogin(String name, String password) {
        return doctorMapper.login(name, password);
    }

    @Override
    public Map<String, Object> getDoctorPage(Integer pageNum, Integer pageSize, String name) {
        // 开启分页
        PageHelper.startPage(pageNum, pageSize);
        List<Doctor> list = doctorMapper.selectPage(name);
        PageInfo<Doctor> pageInfo = new PageInfo<>(list);

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageInfo.getList());
        result.put("total", pageInfo.getTotal());
        return result;
    }

    /**
     * 修改密码
     */
    public void updatePassword(Account account) {
        Doctor dbUser = doctorMapper.selectByUsername(account.getUsername());
        if (dbUser == null) {
            throw new CustomException("用户不存在");
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException("原密码错误");
        }
        dbUser.setPassword(account.getNewPassword());
        doctorMapper.updateDoctor(dbUser);
    }

}