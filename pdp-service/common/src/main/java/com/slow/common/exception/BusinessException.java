package com.slow.common.exception;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String msg) {
        super(msg);
    }
}
