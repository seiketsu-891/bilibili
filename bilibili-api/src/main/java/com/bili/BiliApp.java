package com.bili;

import com.bili.service.WebSocketService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class BiliApp {
    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(BiliApp.class, args);
        WebSocketService.setApplicationContext(applicationContext);
    }
}
