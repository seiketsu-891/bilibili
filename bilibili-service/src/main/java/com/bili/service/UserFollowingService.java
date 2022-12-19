package com.bili.service;

import com.bili.dao.UserFollowingDao;
import com.bili.domain.FollowingGroup;
import com.bili.domain.User;
import com.bili.domain.UserFollowing;
import com.bili.domain.UserInfo;
import com.bili.domain.constant.UserConstant;
import com.bili.domain.exception.ConditionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserFollowingService {
    @Resource
    private UserFollowingDao userFollowingDao;

    @Resource
    private  FollowingGroupService followingGroupService;
    @Resource
    private UserService userService;

    @Transactional
    public void addUserFollowing(UserFollowing userFollowing){
        // check groupId
        Long groupId =userFollowing.getGroupId();
        if( groupId == null){
            Long defaultGroupId = followingGroupService.getByType(UserConstant.USER_FOLLOWING_GROUP_TYPE_DEFAULT).getId();
            userFollowing.setGroupId(defaultGroupId);
        }else{
           FollowingGroup followingGroupDb = followingGroupService.getById(groupId);
           if(followingGroupDb == null){
               throw new ConditionException("The following group does not exist.");
           }
        }

        // check if the user a user wants to follow exists
        User followingUserDb = userService.getUserById(userFollowing.getFollowingId());
        if(followingUserDb == null){
            throw new ConditionException("The user you are trying to follow does not exist.");
        }

        // delete the existing following relationship first
        // prevent duplicate follow relationships;
        userFollowingDao.deleteUserFollowing( userFollowing.getUserId(),  userFollowing.getFollowingId());

        // add
        userFollowing.setCreateTime(new Date());
        userFollowingDao.addUserFollowing(userFollowing);
    }

    public List<FollowingGroup> getUserFollowings(Long userId) {
        // 1. get FollowingList and set corresponding userInfo
        List<UserFollowing> userFollowingList =  userFollowingDao.getUserFollowings(userId);
        Set<Long> followingIdSet  = userFollowingList.stream().map(UserFollowing::getFollowingId).collect(Collectors.toSet());

        List< UserInfo> userInfoList = new ArrayList<>();
        if(followingIdSet.size() >  0){
           userInfoList =  userService.getUserInfoByUserIds(followingIdSet);
        }
        for (UserFollowing userFollowing : userFollowingList) {
            for (UserInfo userInfo : userInfoList) {
                if(userFollowing.getFollowingId().equals(userInfo.getUserId())){
                    userFollowing.setUserInfo(userInfo);
                }
            }
        }

        // 2. set group info
        // (1)create a group which contains all following users.
        //    this group does not need to be stored in the database
        FollowingGroup followingGroupAll = new FollowingGroup();
        followingGroupAll.setName("all");
        followingGroupAll.setFollowingUserInfoList(userInfoList);

        // (2) set userInfo for other groups
        List<FollowingGroup> followingGroupList = followingGroupService.getByUserId(userId);
        List<FollowingGroup> result = new ArrayList<>();

        for (FollowingGroup group : followingGroupList) {
            List<UserInfo> infoList  = new ArrayList<>();
            for (UserFollowing userFollowing : userFollowingList) {
                if(group.getId() .equals(userFollowing.getGroupId())){
                    infoList.add(userFollowing.getUserInfo());
                }
            }
            group.setFollowingUserInfoList(infoList);
            result.add(group);
        }

        return  result;
    }

    public List<UserFollowing> getUserFollowers(Long userId){
       List<UserFollowing> followerList =  userFollowingDao.getUserFollowers(userId);

       // get userInfo and  data
       Set<Long> followerIdSet  = followerList.stream().map(UserFollowing::getUserId).collect(Collectors.toSet());
       List<UserInfo> userInfoList = new ArrayList<>();
       if(followerList.size( ) > 0){
            userInfoList = userService.getUserInfoByUserIds(followerIdSet);
        }

       // get followee data
        List<UserFollowing> userFollowingList = userFollowingDao.getUserFollowings(userId);

        // set userInfo and if is mutual follow
        for (UserFollowing follower : followerList) {
            for (UserInfo userInfo : userInfoList) {
                if(follower.getUserId().equals(userInfo.getUserId())){
                    follower.setMutualFollow(false); // initialize the value
                    follower.setUserInfo(userInfo);
                }
            }
            for (UserFollowing userFollowing : userFollowingList) {
                 if(userFollowing.getFollowingId().equals(follower.getUserId())){
                     follower.setMutualFollow(true);
                 }
            }
        }
       return followerList;
    }

    public List<UserInfo>  checkFollowingRelationship(List<UserInfo> userInfos, Long userId) {
       List<UserFollowing> userFollowingList = userFollowingDao.getUserFollowings(userId);
        for (UserInfo userInfo : userInfos) {
            userInfo.setFollowedByCurrentUser(false);
            for (UserFollowing userFollowing : userFollowingList) {
                  if(userFollowing.getFollowingId() .equals(userInfo.getUserId())){
                     userInfo.setFollowedByCurrentUser(true);
                }
            }
        }
        return userInfos;
    }
}

