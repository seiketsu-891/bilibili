package com.bili.api;

import com.bili.api.support.UserSupport;
import com.bili.domain.JsonResponse;
import com.bili.domain.UserMoment;
import com.bili.domain.annotation.ApiLimitedRole;
import com.bili.domain.annotation.DataLimited;
import com.bili.domain.constant.AuthRoleConstant;
import com.bili.service.UserMomentsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class UserMomentsApi {
    @Resource
    UserMomentsService userMomentsService;

    @Resource
    UserSupport userSupport;

    /**
     * add a moment
     */
    @PostMapping("/user-moments")
    @ApiLimitedRole(limitedRoleCodeList = {AuthRoleConstant.ROLE_LV0})
    @DataLimited
    public JsonResponse<String> addUserMoments(@RequestBody UserMoment userMoment) throws Exception {
        Long userId = userSupport.getCurrentUserId();
        userMoment.setUserId(userId);
        userMomentsService.addUserMoments(userMoment);
        return JsonResponse.success();
    }

    /**
     * get the moments that was posted by users that current user is following
     */
    @GetMapping("/user-subscribed-moments")
    public JsonResponse<List<UserMoment>> getUserSubscribedMoments() {
        Long userId = userSupport.getCurrentUserId();
        List<UserMoment> moments = userMomentsService.getUserSubscribedMoments(userId);
        return new JsonResponse<>(moments);
    }
}
