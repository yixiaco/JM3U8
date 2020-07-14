package com.hexm.m3u8;

/**
 * 下载状态
 *
 * @author hexm
 * @date 2020/6/4 17:58
 */
public enum  DownloadStatus {
    /***/
    WAIT("等待下载"),
    DOWNLOADING("下载中"),
    PAUSE("暂停下载"),
    SUCCESS("下载完成"),
    FAIL("下载失败"),
    CANCEL("取消下载");


    private final String desc;

    DownloadStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
