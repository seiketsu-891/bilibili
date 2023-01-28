package com.bili.service;

import com.bili.domain.auth.*;
import com.bili.domain.constant.AuthRoleConstant;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserAuthService {
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private AuthRoleService authRoleService;

    public UserAuthorities getUserAuthorities(Long userId) {
        List<UserRole> userRoleList = userRoleService.getUserRolesByUserId(userId);
        Set<Long> roleIdSet = userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
        List<AuthRoleElementOperation> roleElementOperationList = authRoleService.getRoleElementOperationsByRoleIds(roleIdSet);
        List<AuthRoleMenu> authRoleMenuList = authRoleService.getAuthRoleMenusByRoleIds(roleIdSet);
        UserAuthorities userAuthorities = new UserAuthorities();
        userAuthorities.setRoleElementOperationList(roleElementOperationList);
        userAuthorities.setRoleMenuList(authRoleMenuList);
        return userAuthorities;
    }

    public void addUserDefaultRole(Long userId) {
        UserRole userRole = new UserRole();
        AuthRole defaultRole = authRoleService.getRoleByCode(AuthRoleConstant.ROLE_LV0);

        userRole.setUserId(userId);
        userRole.setRoleId(defaultRole.getId());
        userRole.setCreateTime(new Date());
        userRoleService.addUserRole(userRole);
    }
}
