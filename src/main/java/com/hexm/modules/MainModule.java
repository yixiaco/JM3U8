package com.hexm.modules;

import com.hexm.util.FontUtil;

import javax.swing.*;
import java.awt.*;

/**
 * 主模块
 *
 * @author hexm
 * @date 2020/6/2
 */
public class MainModule extends JPanel {

    private final JFrame frame;
    private JTabbedPane tabbedPane;
    private DownloadingModule downloading;
    private DownloadCompletedModule downloadCompleted;
    private ToolBarModule toolBarModule;

    private MainModule(JFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        init();
    }

    public static void append(JFrame frame) {
        new MainModule(frame);
    }

    /**
     * 初始化
     */
    private void init() {
        //工具栏
        toolBarModule = ToolBarModule.append(this);
        //标签
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FontUtil.title());
        //面板
        downloading = DownloadingModule.append(this);
        downloadCompleted = DownloadCompletedModule.append(this);
        add(tabbedPane);
        //加入到JFrame中
        frame.add(this);
    }

    public JFrame getFrame() {
        return frame;
    }

    public ToolBarModule getToolBarModule() {
        return toolBarModule;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public DownloadingModule getDownloading() {
        return downloading;
    }

    public DownloadCompletedModule getDownloadCompleted() {
        return downloadCompleted;
    }
}
