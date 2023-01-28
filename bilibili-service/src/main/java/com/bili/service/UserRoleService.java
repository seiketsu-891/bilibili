package com.bili.service;

import com.bili.dao.UserRoleDao;
import com.bili.domain.auth.UserRole;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserRoleService {
    @Resource
    private UserRoleDao userRoleDao;

    public List<UserRole> getUserRolesByUserId(Long userId) {
        return userRoleDao.getUserRolesByUserId(userId);
    }

    public Integer addUserRole(UserRole userRole) {
        return userRoleDao.addUserRole(userRole);
    }
}
