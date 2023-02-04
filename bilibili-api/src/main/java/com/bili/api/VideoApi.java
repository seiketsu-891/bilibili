package com.bili.api;

import com.bili.api.support.UserSupport;
import com.bili.domain.JsonResponse;
import com.bili.domain.PageResult;
import com.bili.domain.Video;
import com.bili.service.VideoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class VideoApi {
    @Resource
    private UserSupport userSupport;
    @Resource
    private VideoService videoService;

    /**
     * post a new vides
     */
    @PostMapping("/videos")
    public JsonResponse<String> addVideo(@RequestBody Video video) {
        video.setUserId(userSupport.getCurrentUserId());
        videoService.addVideos(video);
        return JsonResponse.success();
    }

    /**
     * get videos by pageNum and category
     */
    @GetMapping("/videos")
    public JsonResponse<PageResult<Video>> getVideosPerPage(Integer pageNum, Integer pageSize, String area) {
        PageResult<Video> pageResult = videoService.getVideosPerPage(pageNum, pageSize, area);
        return new JsonResponse<>(pageResult);
    }

    /**
     * get a portion of video to watch (the functionality of viewing a video online)
     */

    @GetMapping("/video-slices")
    public void viewVideosOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String relativePath) throws Exception {
        videoService.viewVideosOnlineBySlices(request, response, relativePath);
    }
}
