package com.bili.service;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.bili.dao.UserDao;
import com.bili.domain.PageResult;
import com.bili.domain.RefreshTokenDetail;
import com.bili.domain.User;
import com.bili.domain.UserInfo;
import com.bili.domain.constant.UserConstant;
import com.bili.domain.exception.ConditionException;
import com.bili.service.util.MD5Util;
import com.bili.service.util.RSAUtil;
import com.bili.service.util.TokenUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional
public class UserService {
    @Resource
    private UserDao userDao;
    @Resource
    private UserAuthService userAuthService;


    public void addUser(User user) {
        String phone = user.getPhone();
        if (phone == null || phone.isBlank()) {
            throw new ConditionException("Phone number cannot be empty");
        }
        if (getUserByPhone(phone) != null) {
            throw new ConditionException("This phone number has been registered");
        }

        Date timeNow = new Date();
        String salt = String.valueOf(timeNow.getTime());
        String passwordRaw;
        try {
            passwordRaw = RSAUtil.decrypt(user.getPassword());
        } catch (Exception e) {
            throw new ConditionException("Password decryption failed");
        }
        String md5Password = MD5Util.sign(passwordRaw, salt, "UTF-8");

        user.setSalt(salt);
        user.setPassword(md5Password);
        user.setCreateTime(timeNow);

        userDao.addUser(user);
        Long userId = user.getId();

        // init and add UserInfo
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setNick(UserConstant.DEFAULT_NICK);
        userInfo.setBirth(UserConstant.DEFAULT_BIRTH);
        userInfo.setGender(UserConstant.GENDER_UNKNOWN);
        userInfo.setCreateTime(timeNow);
        userDao.addUserInfo(userInfo);

        // set default role
        userAuthService.addUserDefaultRole(userId);
    }

    public User getUserByPhone(String phone) {
        return userDao.getUserByPhone(phone);
    }

    public String login(User user) throws Exception {
        String phone = user.getPhone();
        if (phone == null || phone.isBlank()) {
            throw new ConditionException("Phone number cannot be empty");
        }

        User userDb = getUserByPhone(phone);
        if (userDb == null) {
            throw new ConditionException("This phone number has not been registered");
        }

        String passwordRaw;
        try {
            passwordRaw = RSAUtil.decrypt(user.getPassword());
        } catch (Exception e) {
            throw new ConditionException("Password decryption failed");
        }
        String passwordMd5 = MD5Util.sign(passwordRaw, userDb.getSalt(), "UTF-8");
        if (!passwordMd5.equals(userDb.getPassword())) {
            throw new ConditionException("The phone number and password don't match");
        }
        return TokenUtil.generateToken(userDb.getId(), TokenUtil.TYPE_ACCESS);
    }

    public User getUserById(Long userId) {
        return userDao.getUserById(userId);
    }

    public UserInfo getUserInfoByUserId(Long userId) {
        return userDao.getUserInfoByUserId(userId);
    }

    public void updateUsers(User user) throws Exception {
        Long id = user.getId();
        User userDb = userDao.getUserById(id);
        if (userDb == null) {
            throw new ConditionException("This user does not exist");
        }
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            String passwordRaw = RSAUtil.decrypt(user.getPassword());
            String passwordMd5 = MD5Util.sign(passwordRaw, userDb.getSalt(), "UTF-8");
            user.setPassword(passwordMd5);
        }
        user.setUpdateTime(new Date());
        userDao.updateUsers(user);
    }

    public void updateUserInfo(UserInfo userInfo) {
        userInfo.setUpdateTime(new Date());
        userDao.updateUserInfos(userInfo);
    }

    public List<UserInfo> getUserInfoByUserIds(Set<Long> followingIdSet) {
        return userDao.getUserInfoByUserIds(followingIdSet);
    }


    /**
     * @param params {pageSize, pageNum, userId, name}
     */
    public PageResult<UserInfo> getUserInfos(JSONObject params) {
        Integer pageNum = params.getInteger("pageNum");
        Integer pageSize = params.getInteger("pageSize");
        Integer offset = (pageNum - 1) * pageSize;
        params.put("offset", offset);
        String nick = params.getString("nick");

        List<UserInfo> userInfoList = new ArrayList<>();
        // the reason that we check if there are any entries whose nickname include nick is that:
        // the performance of count is better by select entries
        Integer count = userDao.getUserInfoWithNick(nick);
        if (count > 0) {
            userInfoList = userDao.getUserInfos(params);
        }
        return new PageResult<>(count, userInfoList);
    }

    public Map<String, Object> loginForDts(User user) throws Exception {
        String phone = user.getPhone();
        if (phone == null || phone.isBlank()) {
            throw new ConditionException("Phone number cannot be empty");
        }

        User userDb = getUserByPhone(phone);
        if (userDb == null) {
            throw new ConditionException("This phone number has not been registered");
        }

        String passwordRaw;
        try {
            passwordRaw = RSAUtil.decrypt(user.getPassword());
        } catch (Exception e) {
            throw new ConditionException("Password decryption failed");
        }
        String passwordMd5 = MD5Util.sign(passwordRaw, userDb.getSalt(), "UTF-8");
        if (!passwordMd5.equals(userDb.getPassword())) {
            throw new ConditionException("The phone number and password don't match");
        }

        Long userId = userDb.getId();
        String accessToken = TokenUtil.generateToken(userId, TokenUtil.TYPE_ACCESS);
        String refreshToken = TokenUtil.generateToken(userId, TokenUtil.TYPE_REFRESH);

        userDao.deleteRefreshToken(refreshToken, userId);
        userDao.addRefreshToken(refreshToken, userId, new Date());
        Map<String, Object> map = new HashMap<>();
        map.put("accessToken", accessToken);
        map.put("refreshToken", refreshToken);
        return map;
    }

    public void logout(Long userId, String refreshToken) {
        userDao.deleteRefreshToken(refreshToken, userId);
    }

    public String refreshAccessToken(String refreshToken) throws Exception {
        // I think we should use verifyToken to check if the token has expired or not, but in the course,
        // the instructor didn't do this. I'm still wondering why.
        try {
            TokenUtil.verifyToken(refreshToken);
        } catch (TokenExpiredException e) {
            throw new ConditionException("555", "Token expired");
        }

        // If a user  has logout, the refreshToken may be deleted but hasn't expired.
        RefreshTokenDetail refreshTokenDetail = userDao.getRefreshTokenDetail(refreshToken);
        if (refreshTokenDetail == null) {
            throw new ConditionException("555", "Illegal token");
        }

        return TokenUtil.generateToken(refreshTokenDetail.getUserId(), TokenUtil.TYPE_ACCESS);
    }

    public List<UserInfo> batchGetUserInfoByUserIds(Set<Long> userIdList) {
        return userDao.batchGetUserInfoByUserIds(userIdList);
    }
}