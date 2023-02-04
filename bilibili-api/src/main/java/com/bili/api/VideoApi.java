package com.bili.api;

import com.bili.api.support.UserSupport;
import com.bili.domain.JsonResponse;
import com.bili.domain.PageResult;
import com.bili.domain.Video;
import com.bili.domain.VideoFavourites;
import com.bili.service.VideoService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

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

    /**
     * give a video a like
     */
    @PostMapping("/video-likes")
    public JsonResponse<String> addVideoLike(@RequestParam Long videoId) {
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoLike(userId, videoId);
        return JsonResponse.success();
    }

    /**
     * cancel the like for a video
     */
    @DeleteMapping("/video-likes")
    public JsonResponse<String> deleteVideLike(@RequestParam Long videoId) {
        Long userId = userSupport.getCurrentUserId();
        videoService.deleteVideLike(userId, videoId);
        return JsonResponse.success();
    }

    /**
     * get the count of total likes of a video and the information about if a user has given the video a like
     */
    @GetMapping("/video-likes")
    public JsonResponse<Map<String, Object>> getVideoLikes(@RequestParam Long videoId) {
        Long userId = null;
        try {
            userSupport.getCurrentUserId();
        } catch (Exception e) {
            // Users can view videos without logging. In this case, we still need to get the count of total likes of a video.
        }
        Map<String, Object> likesInfo = videoService.getVideoLikes(userId, videoId);
        return new JsonResponse<>(likesInfo);
    }

    /**
     * add a video to favourites
     */
    @PostMapping("/video-favourites")
    public JsonResponse<String> addVideoToFavourites(@RequestBody VideoFavourites videoFavourites) {
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoFavourites(videoFavourites, userId);
        return JsonResponse.success();
    }

    /**
     * remove a video from favourites
     */
    @DeleteMapping("/video-favourites")
    public JsonResponse<String> deleteVideoFromFavourites(Long videoId) {
        Long userId = userSupport.getCurrentUserId();
        videoService.deleteVideoFavourites(videoId, userId);
        return JsonResponse.success();
    }

    @GetMapping("/video-favourites")
    public JsonResponse<Map<String, Object>> getVideoFavourites(Long videoId) {
        Long userId = null;
        try {
            userId = userSupport.getCurrentUserId();
        } catch (Exception e) {
        }
        Map<String, Object> favInfo = videoService.getVideoFavourites(videoId, userId);
        return new JsonResponse<>(favInfo);
    }
}
