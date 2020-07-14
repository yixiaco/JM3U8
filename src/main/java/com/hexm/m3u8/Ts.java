package com.hexm.m3u8;

import java.io.File;
import java.util.Arrays;

public class Ts {
    /**
     * 下载地址
     */
    private String url;
    /**
     * 文件名称
     */
    private String name;
    /**
     * 重试次数
     */
    private int retry;
    /**
     * 下载状态
     */
    private Boolean isSuccess;
    /**
     * 持续时间
     */
    private double duration;
    /**
     * 临时文件，如果使用硬盘做临时存储，否则该属性没有值
     */
    private File file;
    /**
     * 临时数据，如果使用内存临时存储，否则该属性没有值
     */
    private byte[] bytes;

    public Ts(String url, String name, double duration) {
        this.url = url;
        this.name = name;
        this.retry = 0;
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public Boolean getSuccess() {
        return isSuccess;
    }

    public void setSuccess(Boolean success) {
        isSuccess = success;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public String toString() {
        return "Ts{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", retry=" + retry +
                ", isSuccess=" + isSuccess +
                ", duration=" + duration +
                ", file=" + file +
                ", bytes=" + Arrays.toString(bytes) +
                '}';
    }
}
