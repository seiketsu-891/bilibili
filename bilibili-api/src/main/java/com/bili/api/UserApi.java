package com.bili.api;

import com.bili.api.support.UserSupport;
import com.bili.domain.JsonResponse;
import com.bili.domain.User;
import com.bili.domain.UserInfo;
import com.bili.service.UserService;
import com.bili.service.util.RSAUtil;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;


@RestController
public class UserApi {
    @Resource
    private UserService userService;
    @Resource
    private UserSupport userSupport;

    /**
     * get RSA public key
     */
    @GetMapping("/rsa-public")
    public JsonResponse<String> getRsaPublicKey(){
        String key = RSAUtil.getPublicKeyStr();
        return new JsonResponse<>(key);
    }

    /**
     * user register
     */
    @PostMapping("/users")
    public JsonResponse<String> addUser(@RequestBody User user){
        userService.addUser(user);
        return JsonResponse.success();
     }

    /**
     * login
     * @return token
     */
     // login is actually getting user-token
     @PostMapping("/user-tokens")
    public JsonResponse<String> login(@RequestBody User user) throws Exception {
        String token =  userService.login(user);
        return JsonResponse.success(token);
    }

    /**
     * get user and userInfo
     */
    @GetMapping("/users")
    public JsonResponse<User> getUserInfo(){
       Long userId = userSupport.getCurrentUserId();
       User user =  userService.getUserById(userId);
       UserInfo userInfo = userService.getUserInfoByUserId(17L);
       user.setUserInfo(userInfo);
       return  new JsonResponse<>(user);
    }

    /**
     * update User
     */
    @PutMapping("/users")
    public JsonResponse<String> updateUsers(@RequestBody User user) throws Exception {
        Long userId = userSupport.getCurrentUserId();
        System.out.println(user.getPhone());
        user.setId(userId);
        userService.updateUsers(user);
        return JsonResponse.success();
    }

    /**
     * update userInfo
     */
    @PutMapping("/user-infos")
    public JsonResponse<String> updateUserInfos(@RequestBody UserInfo userInfo){
        Long userId = userSupport.getCurrentUserId();
        userInfo.setUserId(userId);
        userService.updateUserInfo(userInfo);
        return JsonResponse.success();
    }
}
