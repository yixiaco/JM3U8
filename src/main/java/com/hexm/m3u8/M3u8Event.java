package com.hexm.m3u8;

/**
 * m3u8事件
 * @author hexm
 * @date 2020/6/6 9:58
 */
public enum M3u8Event {
    /**开始下载事件*/
    START,
    /**进度变更*/
    CHANGE,
    /**下载失败*/
    FAIL,
    /**下载成功*/
    SUCCESS,
    /**取消*/
    CANCEL,
    /**暂停*/
    PAUSE;
}
