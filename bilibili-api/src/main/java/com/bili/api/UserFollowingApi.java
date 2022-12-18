package com.bili.api;

import com.bili.api.support.UserSupport;
import com.bili.domain.FollowingGroup;
import com.bili.domain.JsonResponse;
import com.bili.domain.UserFollowing;
import com.bili.service.UserFollowingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class UserFollowingApi {
    @Resource
    private UserSupport userSupport;
    @Resource
    private UserFollowingService userFollowingService;

    /**
     * add a following relationship
     */
    @PostMapping("/user-following")
    public JsonResponse<String> addUserFollowing(@RequestBody UserFollowing userFollowing){
        Long userId  = userSupport.getCurrentUserId();
        userFollowing.setUserId(userId);
        userFollowingService.addUserFollowing(userFollowing);
        return JsonResponse.success();
    }
}
