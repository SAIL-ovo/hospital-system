package com.example.service;

import com.example.entity.Account;
import com.example.entity.Doctor;
import java.util.List;
import java.util.Map;

/**
 * 医生Service接口
 * 处理医生相关业务逻辑
 */
public interface DoctorService {


    Doctor login(Account account);
    /**
     * 新增医生
     * @param doctor 医生对象
     * @return 成功返回true，失败返回false
     */
    boolean addDoctor(Doctor doctor);

    /**
     * 删除医生
     * @param id 医生ID
     * @return 成功返回true，失败返回false
     */
    boolean deleteDoctor(Integer id);

    /**
     * 修改医生
     * @param doctor 医生对象
     * @return 成功返回true，失败返回false
     */
    boolean updateDoctor(Doctor doctor);

    /**
     *修改密码
     */
    void updatePassword(Account account);
    /**
     * 根据ID查询医生
     * @param id 医生ID
     * @return 医生对象
     */
    Doctor getDoctorById(Integer id);

    /**
     * 查询医生列表
     * @param doctor 筛选条件
     * @return 医生列表
     */
    List<Doctor> getDoctorList(Doctor doctor);

    /**
     * 医生登录
     * @param name 姓名
     * @param password 密码（明文）
     * @return 登录成功返回医生对象，失败返回null
     */
    Doctor doctorLogin(String name, String password);

    /**
     * 分页查询医生
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param name     姓名（可选）
     * @return {list: 列表, total: 总数}
     */
    Map<String, Object> getDoctorPage(Integer pageNum, Integer pageSize, String name);
}