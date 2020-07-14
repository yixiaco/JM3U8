package com.hexm.util;

import java.awt.*;

/**
 * @author hexm
 * @date 2020/6/2
 */
public class FontUtil {

    /**
     * 默认字体
     *
     * @return
     */
    public static Font defaultFont() {
        return new Font("微软雅黑", Font.PLAIN, 16);
    }

    /**
     * 标题字体
     *
     * @return
     */
    public static Font title() {
        return new Font("微软雅黑", Font.PLAIN, 17);
    }

    /**
     * 菜单字体
     * @return
     */
    public static Font menu(){
        return new Font("微软雅黑", Font.PLAIN, 13);
    }
}
