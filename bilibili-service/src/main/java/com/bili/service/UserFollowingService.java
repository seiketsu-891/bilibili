package com.bili.service;

import com.bili.dao.UserFollowingDao;
import com.bili.domain.FollowingGroup;
import com.bili.domain.User;
import com.bili.domain.UserFollowing;
import com.bili.domain.constant.UserConstant;
import com.bili.domain.exception.ConditionException;
import org.apache.catalina.realm.UserDatabaseRealm;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

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

}
