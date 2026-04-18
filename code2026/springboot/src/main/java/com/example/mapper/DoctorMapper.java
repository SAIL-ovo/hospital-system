package com.example.mapper;

import com.example.entity.Doctor;
import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 医生Mapper接口
 * 负责数据库CRUD操作
 */
@Mapper // 标记为MyBatis的Mapper接口
public interface DoctorMapper {

    /**
     * 新增医生
     * @param doctor 医生对象
     * @return 受影响行数
     */
    int insertDoctor(Doctor doctor);

    /**
     * 根据ID删除医生
     * @param id 医生ID
     * @return 受影响行数
     */
    int deleteDoctorById(Integer id);

    /**
     * 修改医生信息
     * @param doctor 医生对象
     * @return 受影响行数
     */
    int updateDoctor(Doctor doctor);

    /**
     * 根据ID查询医生
     * @param id 医生ID
     * @return 医生对象
     */
    Doctor selectDoctorById(Integer id);

    /**
     * 查询所有医生（支持条件筛选）
     * @param doctor 筛选条件（如科室、状态）
     * @return 医生列表
     */
    List<Doctor> selectDoctorList(Doctor doctor);

    /**
     * 医生登录（根据姓名+密码查询）
     * @param name 医生姓名
     * @param password 加密后的密码
     * @return 医生对象
     */
    Doctor login(String name, String password);

    /**
     * 分页查询医生
     * @param name
     * @return
     */
    List<Doctor> selectPage(@Param("name") String name);


    //通过名称查询医生
    Doctor selectByUsername(String username);

    // 医生登录查询
    Doctor loginByUsername(String username, String password);

    Doctor loginByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
}