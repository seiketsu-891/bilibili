package com.bili.service;

import com.bili.dao.AuthRoleDao;
import com.bili.domain.auth.AuthRole;
import com.bili.domain.auth.AuthRoleElementOperation;
import com.bili.domain.auth.AuthRoleMenu;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Service
public class AuthRoleService {
    @Resource
    private AuthRoleElementOperationService authRoleElementOperationService;
    @Resource
    private AuthRoleMenuService authRoleMenuService;

    @Resource
    private AuthRoleDao authRoleDao;

    public List<AuthRoleElementOperation> getRoleElementOperationsByRoleIds(Set<Long> roleIdSet) {
        return authRoleElementOperationService.getRoleElementOperationsByRoleIds(roleIdSet);
    }

    public List<AuthRoleMenu> getAuthRoleMenusByRoleIds(Set<Long> roleIdSet) {
        return authRoleMenuService.getAuthRoleMenusByRoleIds(roleIdSet);
    }

    public AuthRole getRoleByCode(String roleCode) {
        return authRoleDao.getRoleByCode(roleCode);
    }
}
