package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service

public class UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 分页查询方法
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<User>  selectPage(Integer pageNum, Integer pageSize, String name) {
        try {
            PageHelper.startPage(pageNum, pageSize);
            List<User> list = userMapper.selectAll(name);
            return PageInfo.of(list);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void deleteById(Integer id) {
        userMapper.deleteById(id);
    }

    public void add(User user) {
        //校验账户是否重复(后端表单校验)
        String username = user.getUsername();
        User dbUser = userMapper.selectByUsername(username);
        if (dbUser != null) {
            throw new CustomException("账户重复，新增失败!");
        }
        //检验账号非空
        if (StrUtil.isBlank(user.getUsername())) {
            throw new CustomException("账号不能为空！");
        }
        //检验密码非空
        if (StrUtil.isBlank(user.getPassword())) {
            throw new CustomException("密码不能为空！");
        }
        user.setName(username);
        user.setRole("普通用户");
        user.setAccount(BigDecimal.ZERO);   //默认账户余额
        userMapper.insert(user);
    }

    public void update(User user) {
        //user对象里必须包含ID，否则无法更新数据
        userMapper.updateByid(user);
    }

    public Account login(Account account) {
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        return dbUser;
    }

    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }

    /**
     * 修改密码
     */
    public void updatePassword(Account account) {
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException("原密码错误");
        }
        dbUser.setPassword(account.getNewPassword());
        userMapper.updateByid(dbUser);
    }


    /**
     * 统计注册用户总数
     */
    public int countAll() {
        return userMapper.countAll();
    }
}
