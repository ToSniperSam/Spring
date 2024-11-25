package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class HelloController {
    @GetMapping("/hello")
    public String index(Model model) {
        // 动态添加数据
        model.addAttribute("message", "Hello, Spring Boot!");
        return "index"; // 返回模板名称，与 `test.html` 对应
    }

}
