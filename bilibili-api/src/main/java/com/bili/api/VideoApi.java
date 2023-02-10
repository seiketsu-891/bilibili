package com.bili.api;

import com.bili.api.support.UserSupport;
import com.bili.domain.*;
import com.bili.service.ElasticsearchService;
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
    @Resource
    private ElasticsearchService elasticsearchService;

    /**
     * post a new vides
     */
    @PostMapping("/videos")
    public JsonResponse<String> addVideo(@RequestBody Video video) {
        video.setUserId(userSupport.getCurrentUserId());
        videoService.addVideos(video);
        elasticsearchService.addVideo(video);
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
    public JsonResponse<String> deleteVideoFromFavourites(@RequestParam Long videoId) {
        Long userId = userSupport.getCurrentUserId();
        videoService.deleteVideoFavourites(videoId, userId);
        return JsonResponse.success();
    }

    /**
     * get the information about how many people have added current video to their favourites
     * and if the current user has added this video to his or her favorites
     */

    @GetMapping("/video-favourites")
    public JsonResponse<Map<String, Object>> getVideoFavourites(@RequestParam Long videoId) {
        Long userId = null;
        try {
            userId = userSupport.getCurrentUserId();
        } catch (Exception e) {
        }
        Map<String, Object> favInfo = videoService.getVideoFavourites(videoId, userId);
        return new JsonResponse<>(favInfo);
    }

    /**
     * give a video coins
     */
    @PostMapping("/video-coins")
    public JsonResponse<String> addVideoCoins(@RequestBody VideoCoin videoCoin) {
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoCoin(videoCoin, userId);
        return JsonResponse.success();
    }

    /**
     * get the information about coins of a video
     */
    @GetMapping("/video-coins")
    public JsonResponse<Map<String, Object>> getVideoCoins(@RequestParam Long videoId) {
        Long userId = null;
        try {
            userId = userSupport.getCurrentUserId();
        } catch (Exception e) {
        }
        Map<String, Object> coinInfo = videoService.getVideoCoins(videoId, userId);
        return new JsonResponse<>(coinInfo);
    }

    /**
     * post a comment to a video
     */
    @PostMapping("/video-comments")
    public JsonResponse<String> addVideoComments(@RequestBody VideoComment videoComment) {
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoComment(videoComment, userId);
        return JsonResponse.success();
    }

    /**
     * get the comments of a video with pagination
     */
    @GetMapping("/video-comments")
    public JsonResponse<PageResult<VideoComment>> getVideoComments(@RequestParam Integer pageNum, @RequestParam Integer pageSize, @RequestParam Long videoId) {
        PageResult<VideoComment> pageResult = videoService.getVideoCommentsPerPage(pageNum, pageSize, videoId);
        return new JsonResponse<>(pageResult);
    }

    /**
     * get the information about a video
     */
    @GetMapping("/video-details")
    public JsonResponse<Map<String, Object>> getVideoDetails(@RequestParam Long videoId) {
        Map<String, Object> vidDetails = videoService.getVideoDetails(videoId);
        return new JsonResponse<>(vidDetails);
    }

    @GetMapping("/es-videos")
    public JsonResponse<Video> getEsVideos(@RequestParam String keyword) {
        Video video = elasticsearchService.getVideo(keyword);
        return new JsonResponse<>(video);
    }

    /**
     * add a video viewing record
     */
    @PostMapping("/video-views")
    public JsonResponse<String> addVideoView(@RequestBody VideoView videoView, HttpServletRequest request) {
        Long userId;
        try {
            userId = userSupport.getCurrentUserId();
            videoView.setUserId(userId);
            videoService.addVideoView(videoView, request);
        } catch (Exception e) {
            videoService.addVideoView(videoView, request);
        }
        return JsonResponse.success();
    }

    /**
     * get the count of the video viewing records
     */
    @GetMapping("/video-view-counts")
    public JsonResponse<Integer> getVideoViewCounts(@RequestParam Long videoId) {
        Integer count = videoService.getVideoViewCount(videoId);
        return new JsonResponse<>(count);
    }
}
