package com.bili.dao;

import com.bili.domain.UserMoment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMomentsDao {
    Integer addUserMoments(UserMoment userMoment);
}
