package com.bili.api.aop;

import com.bili.api.support.UserSupport;
import com.bili.domain.annotation.ApiLimitedRole;
import com.bili.domain.auth.UserRole;
import com.bili.domain.exception.ConditionException;
import com.bili.service.UserRoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Order(1)
@Component
public class ApiLimitedRoleAspect {
    @Resource
    private UserSupport userSupport;
    @Resource
    private UserRoleService userRoleService;

    @Pointcut("@annotation(com.bili.domain.annotation.ApiLimitedRole)")
    public void check() {
    }

    @Before("check() && @annotation(apiLimitedRole)")
    public void doBefore(JoinPoint joinPoint, ApiLimitedRole apiLimitedRole) {
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRolesList = userRoleService.getUserRolesByUserId(userId);

        Set<String> limitedRoleCodeSet = Arrays.stream(apiLimitedRole.limitedRoleCodeList()).collect(Collectors.toSet());
        Set<String> roleCodeSet = userRolesList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());

        // get the intersection of a user's role codes and the role codes restricted for a specific API
        roleCodeSet.retainAll(limitedRoleCodeSet);

        if (roleCodeSet.size() > 0) {
            throw new ConditionException("Permission denied");
        }
    }
}
