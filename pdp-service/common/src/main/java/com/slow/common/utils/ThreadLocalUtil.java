package com.slow.common.utils;

/**
 * ThreadLocal 工具类
 */
public class ThreadLocalUtil {

    private static final ThreadLocal<Object> THREAD_LOCAL = new ThreadLocal<>();

    public static void set(Object value) {
        THREAD_LOCAL.set(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get() {
        return (T) THREAD_LOCAL.get();
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
