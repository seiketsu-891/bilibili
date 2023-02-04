package com.bili.service;

import com.bili.dao.VideoDao;
import com.bili.domain.PageResult;
import com.bili.domain.Video;
import com.bili.domain.VideoTag;
import com.bili.domain.exception.ConditionException;
import com.bili.service.util.FastDFSUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Service
public class VideoService {
    @Resource
    private VideoDao videoDao;
    @Resource
    private FastDFSUtil fastDFSUtil;

    @Transactional
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
}
