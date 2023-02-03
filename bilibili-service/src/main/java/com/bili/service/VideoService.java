package com.bili.service;

import com.bili.dao.VideoDao;
import com.bili.domain.Video;
import com.bili.domain.VideoTag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class VideoService {
    @Resource
    private VideoDao videoDao;

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
}
