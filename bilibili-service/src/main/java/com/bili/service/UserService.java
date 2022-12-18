package com.bili.service;

import com.bili.dao.UserDao;
import com.bili.domain.User;
import com.bili.domain.UserInfo;
import com.bili.domain.constant.UserConstant;
import com.bili.domain.exception.ConditionException;
import com.bili.service.util.MD5Util;
import com.bili.service.util.RSAUtil;
import com.bili.service.util.TokenUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    @Resource
    private UserDao userDao;

    public void addUser(User user) {
        String phone = user.getPhone();
        if(phone == null || phone.isBlank()){
            throw new ConditionException("Phone number cannot be empty");
        }
        if(getUserByPhone(phone)!=null){
            throw new ConditionException("This phone number has been registered");
        }

        Date timeNow = new Date();
        String salt = String.valueOf(timeNow.getTime());
        String passwordRaw;
        try{
           passwordRaw =  RSAUtil.decrypt(user.getPassword());
        }catch (Exception e){
           throw new ConditionException("Password decryption failed");
        }
        String md5Password = MD5Util.sign(passwordRaw, salt, "UTF-8");

        user.setSalt(salt);
        user.setPassword(md5Password);
        user.setCreateTime(timeNow);

        userDao.addUser(user);

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setNick(UserConstant.DEFAULT_NICK);
        userInfo.setBirth(UserConstant.DEFAULT_BIRTH);
        userInfo.setGender(UserConstant.GENDER_UNKNOWN);
        userInfo.setCreateTime(timeNow);

        userDao.addUserInfo(userInfo);
    }

    public User getUserByPhone(String phone){
        return userDao.getUserByPhone(phone);
    }

    public String login(User user) throws Exception {
        String phone = user.getPhone();
        if(phone == null || phone.isBlank()){
            throw new ConditionException("Phone number cannot be empty");
        }

        User userDb  = getUserByPhone(phone);
        if(userDb == null){
            throw new ConditionException("This phone number has not been registered");
        }

        String passwordRaw;
        try{
             passwordRaw = RSAUtil.decrypt(user.getPassword());
        }catch (Exception e){
             throw new ConditionException("Password decryption failed");
        }
        String passwordMd5 = MD5Util.sign(passwordRaw, userDb.getSalt(), "UTF-8");
        if(!passwordMd5.equals(userDb.getPassword())){
            throw new ConditionException("The phone number and password don't match");
        }
        return TokenUtil.generateToken(userDb.getId());
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
        if(userDb == null) {
            throw new ConditionException("This user does not exist");
        }
        if(user.getPassword() != null && !user.getPassword().isBlank()){
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
}