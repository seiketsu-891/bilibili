package com.bili.service;

import com.bili.dao.UserCoinDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class UserCoinService {
    @Resource
    UserCoinDao userCoinDao;

    public Long getUserCoinAmount(Long userId) {
        return userCoinDao.getUserCoinAmount(userId);
    }

    public void updateUserCoinAmount(Long userId, Long userCoinAmount, Date now) {
        userCoinDao.updateUserCoinAmount(userId, userCoinAmount, now);
    }
}
