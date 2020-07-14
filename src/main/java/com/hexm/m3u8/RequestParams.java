package com.hexm.m3u8;

import java.util.HashMap;
import java.util.Map;

public class RequestParams {
    private final Map<String, String> heads = new HashMap<>();
    private final Map<String, Object> params = new HashMap<>();

    public static RequestParams getInstance() {
        return new RequestParams();
    }

    public RequestParams addHead(String name, String value) {
        heads.put(name, value);
        return this;
    }

    public RequestParams addParam(String name, Object value) {
        params.put(name, value);
        return this;
    }

    public Map<String, String> getHeads() {
        return heads;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "RequestParams{" +
                "heads=" + heads +
                ", params=" + params +
                '}';
    }
}
