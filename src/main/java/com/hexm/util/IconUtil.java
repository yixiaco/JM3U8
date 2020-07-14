package com.hexm.util;

import javax.swing.*;
import java.awt.*;

/**
 * @author hexm
 * @date 2020/6/2
 */
public class IconUtil {

    /**
     * 得到icon
     *
     * @param filename 资源路径
     * @return
     */
    public static ImageIcon getIcon(String filename) {
        return new ImageIcon(IconUtil.class.getResource(filename));
    }

    /**
     * 得到一个自定义大小的icon
     *
     * @param filename 资源路径
     * @param width    宽
     * @param height   高
     * @return
     */
    public static ImageIcon getIcon(String filename, int width, int height) {
        ImageIcon imageIcon = new ImageIcon(IconUtil.class.getResource(filename));
        //图片压缩，只需改动20与15自行设置
        return new ImageIcon(imageIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }
}
