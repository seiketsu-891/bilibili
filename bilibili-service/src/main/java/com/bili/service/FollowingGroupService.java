package com.bili.service;

import com.bili.dao.FollowingGroupDao;
import com.bili.domain.FollowingGroup;
import com.bili.domain.constant.UserConstant;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class FollowingGroupService {
    @Resource
    private FollowingGroupDao followingGroupDao;

    public FollowingGroup getByType(String type){
        return followingGroupDao.getByType(type);
    }

    public FollowingGroup getById(Long id){
        return followingGroupDao.getById(id);
    }

    public List<FollowingGroup> getByUserId(Long userId) {
        return followingGroupDao.getByUserId(userId);
    }

    public Long addFollowingGroup(FollowingGroup followingGroup) {
        followingGroup.setCreateTime(new Date());
        followingGroup.setType(UserConstant.USER_FOLLOWING_GROUP_TYPE_CUSTOM);
        followingGroupDao.addFollowingGroup(followingGroup);
        return followingGroup.getId();
    }

    public List<FollowingGroup> getFollowingGroups(Long userId) {
       return   followingGroupDao.getFollowingGroup(userId);
    }
}
