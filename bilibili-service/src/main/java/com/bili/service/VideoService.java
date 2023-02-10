package com.bili.service;

import com.bili.dao.VideoDao;
import com.bili.domain.*;
import com.bili.domain.exception.ConditionException;
import com.bili.service.util.FastDFSUtil;
import com.bili.service.util.IpUtil;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@Transactional
@Service
public class VideoService {
    @Resource
    UserCoinService userCoinService;
    @Resource
    private VideoDao videoDao;
    @Resource
    private FastDFSUtil fastDFSUtil;

    @Resource
    private UserService userService;

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
            throw new ConditionException("This video does not exist");
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
            throw new ConditionException("This video does not exist");
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
            throw new ConditionException("This video does not exist");
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

    public void addVideoComment(VideoComment videoComment, Long userId) {
        Long videoId = videoComment.getVideoId();
        if (videoId == null) {
            throw new ConditionException("Illegal arguments");
        }

        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("This video does not exist");
        }

        // this code fragment is added by me, it's not in the course demo
        VideoComment comment = videoDao.getVideoCommentById(videoComment.getRootId());
        if (comment == null) {
            throw new ConditionException("The comment you are replying to does not exist");
        }

        videoComment.setUserId(userId);
        videoComment.setCreateTime(new Date());
        videoDao.addVideoComment(videoComment);
    }

    public PageResult<VideoComment> getVideoCommentsPerPage(Integer pageNum, Integer pageSize, Long videoId) {
        if (pageNum == null || pageSize == null) {
            throw new ConditionException("Illegal arguments");
        }
        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("This video does not exist");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("start", (pageNum - 1) * pageSize);
        params.put("size", pageSize);
        params.put("videId", videoId);
        Integer totalNum = videoDao.getVideoCommentCount(params);

        List<VideoComment> comments = new ArrayList<>();

        if (totalNum > 0) {
            comments = videoDao.getVideoCommentsPerPage(params);
            // get child comments
            List<Long> rootIdLis = comments.stream().map(VideoComment::getId).collect(Collectors.toList());
            List<VideoComment> childCommentsList = videoDao.batchGetVideoCommentByRootIds(rootIdLis);

            // get userInfos
            Set<Long> userIdList = comments.stream().map(VideoComment::getUserId).collect(Collectors.toSet());
            Set<Long> replayTargetUserIdList = comments.stream().map(VideoComment::getReplyUserId).collect(Collectors.toSet());
            userIdList.addAll(replayTargetUserIdList);
            List<UserInfo> userInfoList = userService.batchGetUserInfoByUserIds(userIdList);
            // By creating this map:
            // in the loop later, we can get UserInfos by the keys of the map(userIds)
            // otherwise, we need to loop the userInfoList;
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getUserId, userInfo -> userInfo));

            // set childComments and userInfos for comments
            for (VideoComment comment : comments) {
                Long id = comment.getId();
                List<VideoComment> childList = new ArrayList<>();
                for (VideoComment c : childCommentsList) {
                    // set userInfos from childList items
                    if (id.equals(c.getRootId())) {
                        c.setUserInfo(userInfoMap.get(c.getUserId()));
                        c.setReplyTargetUserInfo(userInfoMap.get(c.getReplyUserId()));
                        childList.add(c);
                    }
                }
                comment.setChildComment(childList);
                comment.setUserInfo(userInfoMap.get(comment.getUserId()));
            }
        }
        return new PageResult<>(totalNum, comments);
    }

    public Map<String, Object> getVideoDetails(Long videoId) {
        Video video = videoDao.getVideoDetails(videoId);
        UserInfo userInfo = userService.getUserInfoByUserId(video.getUserId());

        Map<String, Object> info = new HashMap<>();
        info.put("video", video);
        info.put("userInfo", userInfo);
        return info;
    }

    public void addVideoView(VideoView videoView, HttpServletRequest request) {
        String agent = request.getHeader("User-Agent");
        UserAgent userAgent = UserAgent.parseUserAgentString(agent);
        String clientId = String.valueOf(userAgent.getId());
        String ip = IpUtil.getIP(request);
        Map<String, Object> params = new HashMap<>();

        Long userId = videoView.getUserId();
        if (userId != null) {
            params.put("userId", userId);
        } else {
            params.put("ip", ip);
            params.put("clientId", clientId);
        }

        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        params.put("today", sdf.format(now));
        params.put("videoId", videoView.getVideoId());

        VideoView videoViewDb = videoDao.getVideoView(params);
        if (videoViewDb == null) {
            videoView.setIp(ip);
            videoView.setClientId(clientId);
            videoView.setCreateTime(now);
            videoDao.addVideoView(videoView);
        }
    }

    public Integer getVideoViewCount(Long videoId) {
        return videoDao.getVideoViewCount(videoId);
    }

    public List<Video> recommend(Long userId) throws TasteException {
        List<UserPreferences> list = videoDao.getAllUserPreferences();
        DataModel dataModel = this.createDataModel(list);
        UserSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
        System.out.println(similarity.userSimilarity(11, 12));
        UserNeighborhood userNeighborhood = new NearestNUserNeighborhood(2, similarity, dataModel);
        long[] ar = userNeighborhood.getUserNeighborhood(userId);
        Recommender recommender = new GenericUserBasedRecommender(dataModel, userNeighborhood, similarity);

        List<RecommendedItem> recommendedItems = recommender.recommend(userId, 5);
        List<Long> itemIds = recommendedItems.stream().map(RecommendedItem::getItemID).collect(Collectors.toList());
        return videoDao.batchGetVideosByIds(itemIds);
    }

    private DataModel createDataModel(List<UserPreferences> userPreferenceList) {
        FastByIDMap<PreferenceArray> fastByIdMap = new FastByIDMap<>();
        Map<Long, List<UserPreferences>> map = userPreferenceList.stream().collect(Collectors.groupingBy(UserPreferences::getUserId));
        Collection<List<UserPreferences>> list = map.values();
        for (List<UserPreferences> userPreferences : list) {
            GenericPreference[] array = new GenericPreference[userPreferences.size()];
            for (int i = 0; i < userPreferences.size(); i++) {
                UserPreferences userPreference = userPreferences.get(i);
                GenericPreference item = new GenericPreference(userPreference.getUserId(), userPreference.getVideoId(), userPreference.getValue());
                array[i] = item;
            }
            fastByIdMap.put(array[0].getUserID(), new GenericUserPreferenceArray(Arrays.asList(array)));
        }
        return new GenericDataModel(fastByIdMap);
    }
}
