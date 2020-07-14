package com.hexm.m3u8;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.hexm.GlobalEnv;
import com.hexm.enums.ModeType;

import java.util.function.Consumer;

/**
 * @author hexm
 * @date 2020/6/11
 */
public class ExpandM3u8 {
    protected String relativeUrl;
    /**
     * 使用线程安全的打印日志
     */
    private final StringBuffer log = new StringBuffer();
    private Consumer<String> logListener;
    protected RequestParams requestParams;
    protected SymmetricCrypto crypto;

    /**
     * 得到一个链接
     *
     * @return
     */
    protected HttpRequest getHttpRequest(String url) {
        HttpRequest request = HttpUtil.createGet(url);
        if (requestParams != null) {
            request.addHeaders(requestParams.getHeads())
                    .form(requestParams.getParams());
        }
        if (GlobalEnv.proxy != null) {
            request.setProxy(GlobalEnv.proxy);
        }
        return request;
    }

    /**
     * 得到解密key
     *
     * @param k
     * @return
     */
    protected Xkey getKey(String k) {
        Xkey xkey = selectKey(k);
        if (xkey == null) {
            xkey = new Xkey();
        } else {
            return xkey;
        }
        String[] split1 = k.split(",", 3);
        if (split1[0].contains("METHOD")) {
            //一次加密
            xkey.setMethod(split1[0].split("=", 2)[1]);
            if (split1.length > 1 && split1[1].contains("URI")) {
                xkey.setUri(split1[1].split("=", 2)[1].replaceAll("\"", ""));
            }
            if (split1.length > 2 && split1[2].contains("IV")) {
                xkey.setIv(split1[2].split("=", 2)[1]);
            }
            if (xkey.getUri() != null) {
                //设置key
                xkey.setKey(getHttpRequest(urlHandler(xkey.getUri())).execute().bodyBytes());
            }
        } else if (split1[0].contains("MEATHOD")) {
            print("二次解密暂未实现");
            //二次加密
            xkey.setMethod(split1[0].split("=", 2)[1]);
            if (split1.length > 1 && split1[1].contains("URI")) {
                String uri = split1[1].split("=", 2)[1].replaceAll("\"", "");
//                uri = Base64.decodeStr(uri);
//                String[] uris = uri.split("/");
//                xkey.setUri(uri);
                xkey.setKey(uri.getBytes());
//                xkey.setIv(uris[1]);
            }
            return null;
        }
        return xkey;
    }

    private Xkey selectKey(String k) {
        Xkey xkey = gaiamountKey(k);
        return xkey;
    }

    /**
     * gaiamount.com网站key处理
     *
     * @param k
     * @return
     */
    private Xkey gaiamountKey(String k) {
        if (!k.contains("gaiamount.com")) {
            return null;
        }
        Xkey xkey = new Xkey();
        String[] split1 = k.split(",", 3);
        if (split1[0].contains("METHOD")) {
            //一次加密
            xkey.setMethod(split1[0].split("=", 2)[1]);
            if (split1[1].contains("URI")) {
                xkey.setUri(split1[1].split("=", 2)[1].replaceAll("\"", ""));
            }
            if (xkey.getUri() != null) {
                String response = getHttpRequest(urlHandler(xkey.getUri())).execute().body();
                if (response.length() > 16) {
                    String[] rb = response.split("o");
                    byte[] key = HexUtil.decodeHex(rb[1]);
                    byte[] iv = HexUtil.decodeHex(rb[2]);
                    //设置key
                    AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, key, iv);
                    String url = aes.decryptStr(Base64.encode(HexUtil.decodeHex(rb[0])));
                    String[] s = url.split("-");
                    byte[] kb = new byte[s.length];
                    for (int i = 0; i < s.length; i++) {
                        kb[i] = (byte) Integer.parseInt(s[i]);
                    }
                    xkey.setKey(kb);
                }
            }
            if (split1[2].contains("IV")) {
                xkey.setIv(split1[2].split("=", 2)[1]);
            }
        }
        return xkey;
    }

    /**
     * url处理
     *
     * @param http
     * @return
     */
    protected String urlHandler(String http) {
        if (!http.startsWith("http")) {
            if (http.startsWith("/")) {
                http = http.substring(1);
            }
            http = relativeUrl + http;
        }
        return http;
    }

    /**
     * 打印日志
     *
     * @param log
     */
    protected void print(String log) {
        if (GlobalEnv.MODE == ModeType.DEV) {
            System.out.println(log);
        }
        if (this.log.length() > 2000) {
            //最大长度2000个字符
            this.log.delete(0, this.log.length() - 2000);
        }
        this.log.append(log).append("\n");
        if (logListener != null) {
            logListener.accept(this.log.toString());
        }
    }

    public void setLogListener(Consumer<String> logs) {
        this.logListener = logs;
    }

    public StringBuffer getLog() {
        return log;
    }

    public RequestParams getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(RequestParams requestParams) {
        this.requestParams = requestParams;
    }

    public String getRelativeUrl() {
        return relativeUrl;
    }

    public void setRelativeUrl(String relativeUrl) {
        this.relativeUrl = relativeUrl;
    }
}
