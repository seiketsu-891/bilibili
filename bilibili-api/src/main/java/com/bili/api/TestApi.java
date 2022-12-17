package com.bili.api;

import com.bili.service.TestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class TestApi {
    @Resource
    private TestService testService;

    @GetMapping("/test")
    public String name(Integer id){
        return testService.getName(id);
    }
}
