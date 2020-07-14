package com.hexm.m3u8;

import java.util.Arrays;

public class Xkey {
    private String method;
    private String uri;
    private String iv;
    private byte[] key;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "Xkey{" +
                "method='" + method + '\'' +
                ", uri='" + uri + '\'' +
                ", iv='" + iv + '\'' +
                ", key=" + Arrays.toString(key) +
                '}';
    }
}
