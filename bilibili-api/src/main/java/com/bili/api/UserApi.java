package com.bili.api;

import com.bili.domain.JsonResponse;
import com.bili.domain.User;
import com.bili.service.UserService;
import com.bili.service.util.RSAUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.awt.desktop.UserSessionEvent;


@RestController
public class UserApi {
    @Resource
    private UserService userService;
    @GetMapping("/rsa-public")
    public JsonResponse<String> getRsaPublicKey(){
        String key = RSAUtil.getPublicKeyStr();
        return new JsonResponse<>(key);
    }

    @PostMapping("/users")
    public JsonResponse<String> addUser(@RequestBody User user){
        userService.addUser(user);
        return JsonResponse.success();
     }

     // login is actually getting user-token
     @PostMapping("/user-tokens")
    public JsonResponse<String> login(@RequestBody User user) throws Exception {
        String token =  userService.login(user);
        return JsonResponse.success(token);
    }
}
