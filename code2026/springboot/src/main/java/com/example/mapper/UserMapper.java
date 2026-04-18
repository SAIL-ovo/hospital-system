package com.example.mapper;

import com.example.entity.User;

import java.util.List;

public interface UserMapper {

    List<User> selectAll(String name);

    void deleteById(Integer id);

    void insert(User user);

    User selectByUsername(String username);

    void updateByid(User user);

    User selectById(Integer id);

    int countAll();
}
