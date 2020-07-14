package com.hexm;

import com.hexm.enums.ModeType;

import javax.swing.*;
import java.net.Proxy;

/**
 * 全局环境设置
 *
 * @author hexm
 * @date 2020/6/9 17:00
 */
public class GlobalEnv {

    /** 版本号 */
    public static final String VERSION = "v1.01";

    /** 生产模式 */
    public static final ModeType MODE = ModeType.PRO;

    /**
     * 总窗口
     */
    public static JFrame frame;

    /**
     * 代理设置
     */
    public static Proxy proxy;
}
