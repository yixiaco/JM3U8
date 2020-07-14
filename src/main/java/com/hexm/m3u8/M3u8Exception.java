package com.hexm.m3u8;

/**
 * @author hexm
 * @date 2020/5/30
 */
public class M3u8Exception extends RuntimeException{
    public M3u8Exception() {
        super();
    }
    public M3u8Exception(String message) {
        super(message);
    }
    public M3u8Exception(String message, Throwable cause) {
        super(message, cause);
    }
    public M3u8Exception(Throwable cause) {
        super(cause);
    }
    protected M3u8Exception(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
