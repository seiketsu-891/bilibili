package com.bili.dao;

import com.bili.domain.Video;
import com.bili.domain.VideoLike;
import com.bili.domain.VideoTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface VideoDao {
    Integer addVideos(Video video);

    void batchAddVideoTags(List<VideoTag> videoTagList);

    Integer getTotalNum(Map<String, Object> params);

    List<Video> getVideosPerPage(Map<String, Object> params);

    Video getVideoById(Long videoId);

    VideoLike getVideoLikeByUserIdAndVideoId(@Param("userId") Long currentUserId, @Param("videoId") Long videoId);

    Integer addVideoLike(VideoLike videoLike);

    void deleteVideoLike(@Param("userId") Long currentUserId, @Param("videoId") Long videoId);

    Long getVideoLikesCount(Long videoId);
}
