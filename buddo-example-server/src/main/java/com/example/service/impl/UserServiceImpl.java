package com.example.service.impl;

import com.example.service.UserService;
import io.github.yuriua.buddo.annotation.RpcService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lyx
 * @date 2022/1/15 21:28
 * Description:
 */
@RpcService
public class UserServiceImpl implements UserService {
    @Override
    public List<String> userList() {
        List<String> list = new ArrayList<>();
        list.add("小明");
        list.add("赵六");
        list.add("张三");
        return list;
    }

    @Override
    public String getById(Integer uid) {
        return "王岐山";
    }
}
