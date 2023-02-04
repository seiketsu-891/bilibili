package com.bili.service;

import com.bili.dao.VideoDao;
import com.bili.domain.PageResult;
import com.bili.domain.Video;
import com.bili.domain.VideoLike;
import com.bili.domain.VideoTag;
import com.bili.domain.exception.ConditionException;
import com.bili.service.util.FastDFSUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Transactional
@Service
public class VideoService {
    @Resource
    private VideoDao videoDao;
    @Resource
    private FastDFSUtil fastDFSUtil;

    public void addVideos(Video video) {
        Date now = new Date();
        // add  video
        video.setCreateTime(now);
        videoDao.addVideos(video);

        // add video-tag relationship
        Long id = video.getId();
        List<VideoTag> videoTagList = video.getVideoTagList();
        for (VideoTag videoTag : videoTagList) {
            videoTag.setVideoId(id);
            videoTag.setCreateTime(now);
        }
        videoDao.batchAddVideoTags(videoTagList);
    }

    public PageResult<Video> getVideosPerPage(Integer pageNum, Integer pageSize, String area) {
        if (pageNum == null || pageSize == null) {
            throw new ConditionException("Illegal arguments");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("start", (pageNum - 1) * pageSize);
        params.put("size", pageSize);
        params.put("area", area);
        Integer totalNum = videoDao.getTotalNum(params);
        List<Video> videos = new ArrayList<>();

        if (totalNum > 0) {
            videos = videoDao.getVideosPerPage(params);
        }
        return new PageResult<>(totalNum, videos);
    }

    public void viewVideosOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String relativePath) throws Exception {
        fastDFSUtil.getVideosOnlineBySlices(request, response, relativePath);
    }

    public void addVideoLike(Long userId, Long videoId) {
        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("This video does not exists");
        }

        VideoLike videoLike = videoDao.getVideoLikeByUserIdAndVideoId(userId, videoId);
        if (videoLike != null) {
            throw new ConditionException("You have liked this video");
        }

        videoLike = new VideoLike();
        videoLike.setUserId(userId);
        videoLike.setVideoId(videoId);
        videoLike.setCreateTime(new Date());
        videoDao.addVideoLike(videoLike);
    }

    public void deleteVideLike(Long currentUserId, Long videoId) {
        videoDao.deleteVideoLike(currentUserId, videoId);
    }

    public Map<String, Object> getVideoLikes(Long userId, Long videoId) {
        Long countOfLikes = videoDao.getVideoLikesCount(videoId);
        VideoLike videoLike = videoDao.getVideoLikeByUserIdAndVideoId(userId, videoId);
        boolean liked = (videoLike != null);
        Map<String, Object> likesInfo = new HashMap<>();
        likesInfo.put("count", countOfLikes);
        likesInfo.put("liked", liked);
        return likesInfo;
    }
}
