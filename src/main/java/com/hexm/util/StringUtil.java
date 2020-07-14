package com.hexm.util;

import java.util.regex.Pattern;

/**
 * @author hexm
 * @date 2020/5/30
 */
public class StringUtil {

    /**
     * 不为空
     * @param str
     * @return
     */
    public static boolean isNotEmpty(String str){
        return !isEmpty(str);
    }

    /**
     * 为空
     * @param str
     * @return
     */
    public static boolean isEmpty(String str){
        return str == null || "".equals(str);
    }

    public static boolean isUrl(String str) {
        if (isEmpty(str)) {
            return false;
        }
        str = str.trim();
        return str.matches("^(http|https)://.+");
    }

    /**
     * 文件大小格式化
     *
     * @param data 文件大小；单位byte
     * @return 格式化的文件大小；例如：1.22M
     */
    public static String formatFileSize(long data) {
        if (data > 0) {
            double size = (double) data;
            double kiloByte = size / 1024;
            if (kiloByte < 1 && kiloByte > 0) {
                //不足1K
                return String.format("%.2fb", size);
            }
            double megaByte = kiloByte / 1024;
            if (megaByte < 1) {
                //不足1M
                return String.format("%.2fKb", kiloByte);
            }
            double gigaByte = megaByte / 1024;
            if (gigaByte < 1) {
                //不足1G
                return String.format("%.2fMb", megaByte);
            }
            double teraByte = gigaByte / 1024;
            if (teraByte < 1) {
                //不足1T
                return String.format("%.2fGb", gigaByte);
            }
            return String.format("%.2fTb", teraByte);
        }
        return "0kb";
    }

    public static boolean isBase64(String str) {
        if(str.length()%4!=0){
            return false;
        }
        String pattern = "^[a-zA-Z0-9/+]*={0,2}$";
        return Pattern.matches(pattern, str);
    }

    public static boolean isHex(String hex){
        String pattern = "[A-Fa-f0-9]*";
        return hex.matches(pattern);
    }
}
