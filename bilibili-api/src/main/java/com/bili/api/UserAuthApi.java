package com.bili.api;

import com.bili.api.support.UserSupport;
import com.bili.domain.JsonResponse;
import com.bili.domain.auth.UserAuthorities;
import com.bili.service.UserAuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class UserAuthApi {
    @Resource
    private UserSupport userSupport;
    @Resource
    private UserAuthService userAuthService;

    @GetMapping("/user-auths")
    public JsonResponse<UserAuthorities> getUserAuthorities(){
        Long userId = userSupport.getCurrentUserId();
        UserAuthorities userAuthorities = userAuthService.getUserAuthorities(userId);
        return new JsonResponse<>(userAuthorities);
    }

}
