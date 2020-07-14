package com.hexm;

import com.hexm.enums.ModeType;
import com.hexm.modules.MainModule;
import com.hexm.modules.MenuModule;
import com.hexm.util.FontUtil;
import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;

/**
 * @author hexm
 * @date 2020/6/2
 */
public class Start {

    private static final int WIDTH = 900;
    private static final int HEIGHT = 800;

    public static void main(String[] args) throws Exception {
        UIManager.put("RootPane.setupButtonVisible", false);
        if (GlobalEnv.MODE == ModeType.DEV) {
            BeautyEyeLNFHelper.debug = true;
        }
        //使用本地窗口样式
        BeautyEyeLNFHelper.frameBorderStyle = BeautyEyeLNFHelper.FrameBorderStyle.osLookAndFeelDecorated;
        BeautyEyeLNFHelper.launchBeautyEyeLNF();
        //设置全局字体
        initGlobalFontSetting(FontUtil.defaultFont());
        //界面
        JFrame frame = new JFrame("M3U8下载工具 " + GlobalEnv.VERSION);
        GlobalEnv.frame = frame;
        //程序图标
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(Start.class.getResource("/images/app.png")));
        //窗口大小可以改变
        frame.setResizable(true);
        //初始化窗口大小
        frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        //点击退出
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //菜单模块加载
        MenuModule.append(frame);
        //主模块加载
        MainModule.append(frame);
        // 包裹内容，此方法会根据内容重新计算窗口的宽高（之前设置的宽高将无效），
        // 此方法会改变窗口宽高，坐标却不会改变，如果需要将窗口设置到屏幕中心，
        // 需要重新调用 jf.setLocationRelativeTo(null) 方法计算窗口的坐标。
        frame.pack();
        // 居中
        frame.setLocationRelativeTo(null);
        // 显示窗口
        frame.setVisible(true);
    }

    /**
     * 设置全局字体
     *
     * @param fnt
     */
    public static void initGlobalFontSetting(Font fnt) {
        FontUIResource fontRes = new FontUIResource(fnt);
        for (Enumeration<?> keys = UIManager.getDefaults().keys(); keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontRes);
            }
        }
    }

}
