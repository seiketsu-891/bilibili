package com.bili.dao;

import com.bili.domain.User;
import com.bili.domain.UserInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

@Mapper
public interface UserDao {
    User getUserById(Long id);
    User getUserByPhone(String phone) ;

    UserInfo getUserInfoByUserId(Long userId);

    Integer addUser(User user);

    Integer addUserInfo(UserInfo userInfo);

    Integer updateUsers(User user);

    Integer updateUserInfos(UserInfo userInfo);

    List<UserInfo> getUserInfoByUserIds(Set<Long> userIdSet);
}
