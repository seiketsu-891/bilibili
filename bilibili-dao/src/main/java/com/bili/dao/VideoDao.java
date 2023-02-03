package com.bili.dao;

import com.bili.domain.Video;
import com.bili.domain.VideoTag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VideoDao {
    Integer addVideos(Video video);

    void batchAddVideoTags(List<VideoTag> videoTagList);
}
