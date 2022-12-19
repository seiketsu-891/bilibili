package com.bili.api;

import com.bili.api.support.UserSupport;
import com.bili.dao.PageResult;
import com.bili.domain.FollowingGroup;
import com.bili.domain.JsonResponse;
import com.bili.domain.UserFollowing;
import com.bili.domain.UserInfo;
import com.bili.service.FollowingGroupService;
import com.bili.service.UserFollowingService;
import org.apache.ibatis.ognl.IteratorEnumeration;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class UserFollowingApi {
    @Resource
    private UserSupport userSupport;
    @Resource
    private UserFollowingService userFollowingService;

    @Resource
    private FollowingGroupService followingGroupService;

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

    /**
     * add a following group
     * @return followingGroupId
     */
    @PostMapping("/user-following-groups")
    public JsonResponse<Long> addFollowingGroup(@RequestBody FollowingGroup followingGroup){
        Long userId = userSupport.getCurrentUserId();
        followingGroup.setUserId(userId);
        Long groupId = followingGroupService.addFollowingGroup(followingGroup);
        return new JsonResponse<>(groupId);
    }

    /**
     * get all user following groups
     */
    @GetMapping("/user-following-groups")
    public JsonResponse<List<FollowingGroup>> getFollowingGroups(){
        Long userId = userSupport.getCurrentUserId();
        List<FollowingGroup> followingGroupList = followingGroupService.getFollowingGroups(userId);
        return new JsonResponse<>(followingGroupList);
    }
}
