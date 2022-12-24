package com.bili.api;

import com.bili.api.support.UserSupport;
import com.bili.domain.JsonResponse;
import com.bili.domain.UserMoment;
import com.bili.service.UserMomentsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class UserMomentsApi {
    @Resource
    UserMomentsService userMomentsService;

    @Resource
    UserSupport userSupport;

    @PostMapping("/user-moments")
    public JsonResponse<String> addUserMoments(@RequestBody UserMoment userMoment) throws Exception {
        Long userId = userSupport.getCurrentUserId();
        userMoment.setUserId(userId);
        userMomentsService.addUserMoments(userMoment);
        return JsonResponse.success();
    }
}
