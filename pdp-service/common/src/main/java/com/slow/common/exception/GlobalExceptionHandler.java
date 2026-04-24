package com.slow.common.exception;

import com.slow.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<String> businessExceptionHandler(BusinessException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void noResourceFoundExceptionHandler(NoResourceFoundException ex) {
        // 静态资源 404，静默处理，不打印日志
    }

    @ExceptionHandler(Exception.class)
    public Result<String> exceptionHandler(Exception ex) {
        log.error("系统异常: ", ex);
        return Result.error("系统繁忙，请稍后重试");
    }
}
