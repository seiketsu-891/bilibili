package com.bili.dao;

import com.bili.domain.auth.UserRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserRoleDao {
    List<UserRole> getUserRolesByUserId(Long userId);

    Integer addUserRole(UserRole userRole);
}
