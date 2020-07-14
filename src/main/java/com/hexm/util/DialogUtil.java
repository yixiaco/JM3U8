package com.hexm.util;

import com.hexm.GlobalEnv;

import javax.swing.*;
import java.awt.*;

/**
 * 弹窗类
 *
 * @author hexm
 * @date 2020/7/13 9:34
 */
public class DialogUtil {

    /**
     * 普通的文本信息弹窗
     *
     * @param width
     * @param height
     * @param html
     */
    public static void sDialog(int width, int height, String title, String html) {
        // 创建一个模态对话框
        final JDialog dialog = new JDialog(GlobalEnv.frame, title, true);
        // 设置对话框的宽高
        dialog.setSize(width, height);
        // 设置对话框大小不可改变
        dialog.setResizable(false);
        // 设置对话框相对显示的位置
        dialog.setLocationRelativeTo(GlobalEnv.frame);

        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel1.setPreferredSize(new Dimension(width, height));
        JLabel mx = new JLabel(html);
        // 添加组件到面板
        panel1.add(mx);
        dialog.setContentPane(panel1);
        dialog.setVisible(true);
        dialog.pack();
    }
}
