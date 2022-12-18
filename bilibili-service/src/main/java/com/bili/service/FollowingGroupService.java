package com.bili.service;

import com.bili.dao.FollowingGroupDao;
import com.bili.domain.FollowingGroup;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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
}