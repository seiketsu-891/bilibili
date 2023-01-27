package com.bili.api.aop;

import com.bili.api.support.UserSupport;
import com.bili.domain.UserMoment;
import com.bili.domain.auth.UserRole;
import com.bili.domain.constant.AuthRoleConstant;
import com.bili.domain.exception.ConditionException;
import com.bili.service.UserRoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Order(1)
@Component
public class DataLimitedAspect {
    @Resource
    private UserSupport userSupport;
    @Resource
    private UserRoleService userRoleService;

    @Pointcut("@annotation(com.bili.domain.annotation.DataLimited)")
    public void check() {
    }

    @Before("check()")
    public void doBefore(JoinPoint joinPoint) {
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRolesList = userRoleService.getUserRolesByUserId(userId);
        Set<String> roleCodeSet = userRolesList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());

        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof UserMoment) {
                UserMoment userMoment = (UserMoment) arg;
                String type = userMoment.getType();
                // users with role Lv1 are only able to post moments of type 0
                if (roleCodeSet.contains(AuthRoleConstant.ROLE_LV1) && !"0".equals(type)) {
                    throw new ConditionException("Illegal arguments");
                }
            }
        }
    }
}
