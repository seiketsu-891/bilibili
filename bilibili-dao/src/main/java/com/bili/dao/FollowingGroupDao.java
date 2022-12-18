package com.bili.dao;

import com.bili.domain.FollowingGroup;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FollowingGroupDao {
    FollowingGroup getByType(String type);
    FollowingGroup getById(Long id);
}
