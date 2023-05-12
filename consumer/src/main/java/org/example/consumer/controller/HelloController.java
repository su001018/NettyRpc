package org.example.consumer.controller;

import org.example.api.service.HelloService;
import org.example.client.annotation.RpcAutowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {
    @RpcAutowired(version = "1.0")
    HelloService helloService;

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){
        return helloService.sayHello("client");
//        return "hello";
    }
}
