package com.bili.api;

import com.bili.api.support.UserSupport;
import com.bili.domain.JsonResponse;
import com.bili.domain.Video;
import com.bili.service.VideoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class VideoApi {
    @Resource
    private UserSupport userSupport;
    @Resource
    private VideoService videoService;

    @PostMapping("/videos")
    public JsonResponse<String> addVideo(@RequestBody Video video) {
        video.setUserId(userSupport.getCurrentUserId());
        videoService.addVideos(video);
        return JsonResponse.success();
    }
}
