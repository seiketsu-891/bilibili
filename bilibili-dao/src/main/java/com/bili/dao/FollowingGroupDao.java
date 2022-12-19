package com.bili.dao;

import com.bili.domain.FollowingGroup;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FollowingGroupDao {
    FollowingGroup getByType(String type);
    FollowingGroup getById(Long id);
    List<FollowingGroup> getByUserId(Long userId);
    Integer  addFollowingGroup(FollowingGroup followingGroup);
    List<FollowingGroup> getFollowingGroup(Long userId);
}
