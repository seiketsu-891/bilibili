package com.bili.dao;

import com.bili.domain.Video;
import com.bili.domain.VideoTag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface VideoDao {
    Integer addVideos(Video video);

    void batchAddVideoTags(List<VideoTag> videoTagList);

    Integer getTotalNum(Map<String, Object> params);

    List<Video> getVideosPerPage(Map<String, Object> params);
}
