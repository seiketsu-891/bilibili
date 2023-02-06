package com.bili.service;

import com.bili.dao.VideoDao;
import com.bili.domain.*;
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
    UserCoinService userCoinService;
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

    public void addVideoFavourites(VideoFavourites videoFavourites, Long userId) {
        Long videoId = videoFavourites.getVideoId();
        Long groupId = videoFavourites.getFavouriteGroupId();
        if (videoId == null || groupId == null) {
            throw new ConditionException("Illegal arguments");
        }

        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("This video does not exists");
        }

        videoDao.deleteVideoFavourites(videoId, userId);
        videoFavourites.setUserId(userId);
        videoFavourites.setCreateTime(new Date());
        videoDao.addVideoFavourites(videoFavourites);
    }

    public void deleteVideoFavourites(Long videoId, Long userId) {
        videoDao.deleteVideoFavourites(videoId, userId);
    }

    public Map<String, Object> getVideoFavourites(Long videoId, Long userId) {
        Long favCount = videoDao.getVideoFavCount(videoId);
        VideoFavourites videoFavourites = videoDao.getVideoFavByUserIdAndVideoId(userId, videoId);
        boolean fav = (videoFavourites != null);
        Map<String, Object> favInfo = new HashMap<>();
        favInfo.put("count", favCount);
        favInfo.put("fav", fav);
        return favInfo;
    }

    public void addVideoCoin(VideoCoin videoCoin, Long userId) {
        Long videoId = videoCoin.getVideoId();
        if (videoId == null) {
            throw new ConditionException("Illegal arguments");
        }

        Long userCoinAmount = userCoinService.getUserCoinAmount(userId);
        // If the user has never bought any coins, userCoinAmount would be  null,
        // so we deal with the null case here
        // I think when we create a user, we can insert an entry to t_user_coin,
        // in this case, serCoinAmount  would never be null
        userCoinAmount = userCoinAmount != null ? userCoinAmount : 0;
        if (videoCoin.getAmount() > userCoinAmount) {
            throw new ConditionException("You don't have enough coins");
        }

        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("This video does not exists");
        }

        Date now = new Date();
        videoCoin.setUserId(userId);
        VideoCoin videoCoinDb = videoDao.getVideoCoinByUserIdAndVideoId(userId, videoId);
        if (videoCoinDb == null) { // This user add video coins to current video for the first time
            videoCoin.setCreateTime(now);
            videoDao.addVideoCoin(videoDao);
        } else {
            videoCoin.setUpdateTime(now);
            videoCoin.setAmount(videoCoinDb.getAmount() + videoCoin.getAmount());
            videoDao.updateVideoCoin(videoCoin);
        }

        // update coin balance of the user
        userCoinAmount -= videoCoin.getUserId();
        userCoinService.updateUserCoinAmount(userId, userCoinAmount, now);
    }

    public Map<String, Object> getVideoCoins(Long videoId, Long userId) {
        Long coinCount = videoDao.getVideoCoinCount(videoId);
        VideoCoin videoCoin = videoDao.getVideoCoinByUserIdAndVideoId(userId, videoId);
        Long userVideoCoinCount = videoCoin.getAmount();
        userVideoCoinCount = userVideoCoinCount != null ? userVideoCoinCount : 0;
        Map<String, Object> coinInfo = new HashMap<>();
        coinInfo.put("count", coinCount);
        coinInfo.put("given", userVideoCoinCount);
        return coinInfo;
    }
}
