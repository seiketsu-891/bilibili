package com.bili.api;

import com.bili.api.support.UserSupport;
import com.bili.domain.FollowingGroup;
import com.bili.domain.JsonResponse;
import com.bili.domain.UserFollowing;
import com.bili.service.UserFollowingService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class UserFollowingApi {
    @Resource
    private UserSupport userSupport;
    @Resource
    private UserFollowingService userFollowingService;

    /**
     * add a following relationship
     */
    @PostMapping("/user-followings")
    public JsonResponse<String> addUserFollowing(@RequestBody UserFollowing userFollowing){
        Long userId  = userSupport.getCurrentUserId();
        userFollowing.setUserId(userId);
        userFollowingService.addUserFollowing(userFollowing);
        return JsonResponse.success();
    }

    /**
     * get the information about whom a user is following
     * @return  user information grouped by following type
     */
    @GetMapping("/user-followings")
    public JsonResponse<List<FollowingGroup>> getUserFollowings(){
        Long userId  = userSupport.getCurrentUserId();
        List<FollowingGroup> userFollowingList = userFollowingService.getUserFollowings(userId);
        return new JsonResponse<>(userFollowingList);
    }

    /**
     * get the followers
     */
    @GetMapping("/user-followers")
    public JsonResponse<List<UserFollowing>> getUserFollowers(){
        Long userId  = userSupport.getCurrentUserId();
        List<UserFollowing> followerList = userFollowingService.getUserFollowers(userId);
        return new JsonResponse<>(followerList);
    }
}
