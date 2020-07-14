package com.hexm.modules;

import com.hexm.components.table.DownloadCompletedTable;
import com.hexm.components.table.DownloadingTable;
import com.hexm.util.IconUtil;

import javax.swing.*;
import java.awt.*;

/**
 * 下载完成模块
 *
 * @author hexm
 * @date 2020/6/2
 */
public class DownloadCompletedModule extends JPanel {
    private final MainModule mainModule;
    public static JTable table;

    private DownloadCompletedModule(MainModule mainModule) {
        super(new BorderLayout(), true);
        this.mainModule = mainModule;
        init();
    }

    /**
     * 添加到主模块中
     *
     * @param mainModule
     * @return
     */
    public static DownloadCompletedModule append(MainModule mainModule) {
        return new DownloadCompletedModule(mainModule);
    }

    /**
     * 初始化
     */
    private void init() {
        initTable();
        mainModule.getTabbedPane().addTab("下载完成", IconUtil.getIcon("/images/complete.png"), this, "已经下载完成的文件列表");
    }

    private void initTable() {
        // 创建一个表格，指定 所有行数据 和 表头
        table = new DownloadCompletedTable();

        // 把 表格 放到 滚动面板 中（表头将自动添加到滚动面板顶部）
        JScrollPane scrollPane = new JScrollPane(table);

        // 添加 滚动面板 到 内容面板
        add(scrollPane);
    }

    public MainModule getMainModule() {
        return mainModule;
    }
}
