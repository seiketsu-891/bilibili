package com.bili.dao;

import com.bili.domain.User;
import com.bili.domain.UserInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao {
    User getUserById(Long id);
    User getUserByPhone(String phone) ;

    UserInfo getUserInfoByUserId(Long userId);

    Integer addUser(User user);

    Integer addUserInfo(UserInfo userInfo);

    Integer updateUsers(User user);

    Integer updateUserInfos(UserInfo userInfo);
}
