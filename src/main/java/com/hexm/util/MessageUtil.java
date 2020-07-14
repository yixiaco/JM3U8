package com.hexm.util;

import javax.swing.*;
import java.awt.*;

/**
 * @author hexm
 * @date 2020/6/1
 */
public class MessageUtil {

    /**
     * 普通消息
     *
     * @param parentComponent
     * @param message
     * @param title
     */
    public static void plain(Component parentComponent, String title, Object message) {
        JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * 信息提示消息
     *
     * @param parentComponent
     * @param message
     * @param title
     */
    public static void information(Component parentComponent, String title, Object message) {
        JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 警告消息
     *
     * @param parentComponent
     * @param message
     * @param title
     */
    public static void warning(Component parentComponent, String title, Object message) {
        JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * 错误消息
     *
     * @param parentComponent
     * @param message
     * @param title
     */
    public static void error(Component parentComponent, String title, Object message) {
        JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.ERROR_MESSAGE, IconUtil.getIcon("/images/error.png"));
    }

    /**
     * 带icon的消息
     *
     * @param parentComponent
     * @param message
     * @param title
     */
    public static void icon(Component parentComponent, String title, Object message, String iconPath) {
        JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.PLAIN_MESSAGE, new ImageIcon(iconPath));
    }

    /**
     * 确认弹出框，是 否
     *
     * @param parentComponent
     * @param message
     * @param title
     * @return 0 是 1 否
     */
    public static int yesNo(Component parentComponent, String title, Object message) {
        return JOptionPane.showConfirmDialog(parentComponent, message, title, JOptionPane.YES_NO_OPTION);
    }


}
