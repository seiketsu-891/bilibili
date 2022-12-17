package com.bili.service.handler;

import com.bili.domain.JsonResponse;
import com.bili.domain.exception.ConditionException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;

// ControllerAdvice is used to define global exception handling for controller.
// methods defined here will be called whenever an exception is thrown from a controller method
@ControllerAdvice
// The order annotation is often used in conjunction with exception handling to specify priority of methods.
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonGlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    // @ResponseBody is used to indicate that the return value of a method should be written directly
    // to the HTTP response body.
    @ResponseBody
    // order can be used before a method. ex: @order(1)-- lower number, higher priority
    // why write HttpServletRequest here?
    // it's common to write it because HttpServletRequest can give us some information about the
    // Http request that caused the exception.
    public JsonResponse<String> commonExceptionHandler(HttpServletRequest req, Exception e){
        String errMessage = e.getMessage();
        if(e instanceof ConditionException){
            ConditionException ex = (ConditionException) e;
            return new JsonResponse<>(ex.getCode(), errMessage);
        }else{
            return new JsonResponse<>("500", errMessage);
        }
    }
}
