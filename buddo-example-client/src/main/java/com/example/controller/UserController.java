package com.example.controller;

import com.example.service.UserService;
import io.github.yuriua.buddo.annotation.RpcReference;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * @author lyx
 * @date 2022/1/15 22:06
 * Description:
 */
@Controller
public class UserController {
    @RpcReference
    private UserService userService;

    public void userList(){
        List<String> list = userService.userList();
        System.out.println(list.size());
        System.out.println(list);
    }
}
