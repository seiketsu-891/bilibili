package com.bili.dao;

import com.bili.domain.UserFollowing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserFollowingDao{
    Integer deleteUserFollowing(@Param("userId") Long userId, @Param("followingId")Long followingId);

    Integer addUserFollowing(UserFollowing userFollowing);
}
