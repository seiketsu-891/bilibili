package com.bili.dao;

import com.bili.domain.User;
import com.bili.domain.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface UserDao {
    User getUserById(Long id);

    User getUserByPhone(String phone);

    UserInfo getUserInfoByUserId(Long userId);

    Integer addUser(User user);

    Integer addUserInfo(UserInfo userInfo);

    Integer updateUsers(User user);

    Integer updateUserInfos(UserInfo userInfo);

    List<UserInfo> getUserInfoByUserIds(Set<Long> userIdSet);

    Integer getUserInfoWithNick(String nick);

    List<UserInfo> getUserInfos(Map<String, Object> params);

    Integer deleteRefreshToken(@Param("refreshToken") String refreshToken, @Param("userId") Long userId);

    Integer addRefreshToken(@Param("refreshToken") String refreshToken, @Param("userId") Long userId, @Param("createTime") Date createTime);
}
