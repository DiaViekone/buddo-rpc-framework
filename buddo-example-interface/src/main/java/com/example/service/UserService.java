package com.example.service;

import java.util.List;
import java.util.Map;

/**
 * @author lyx
 * @date 2022/1/15 21:26
 * Description:
 */
public interface UserService {
    List<String> userList();

    String getById(Integer uid);
}
