package com.hexm.modules;

import com.hexm.components.table.DownloadingTable;
import com.hexm.components.table.DownloadingTableModel;
import com.hexm.m3u8.M3u8;
import com.hexm.util.IconUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 正在下载模块
 *
 * @author hexm
 * @date 2020/6/2
 */
public class DownloadingModule extends JPanel {
    private final MainModule mainModule;
    public static JTable table;

    private DownloadingModule(MainModule mainModule) {
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
    public static DownloadingModule append(MainModule mainModule) {
        return new DownloadingModule(mainModule);
    }

    /**
     * 初始化
     */
    private void init() {
        initTable();
        mainModule.getTabbedPane().addTab("正在下载", IconUtil.getIcon("/images/downloading.png"), this, "正在下载的文件列表");
    }

    private void initTable() {
        // 创建一个表格，指定 所有行数据 和 表头
        table = new DownloadingTable();

        // 把 表格 放到 滚动面板 中（表头将自动添加到滚动面板顶部）
        JScrollPane scrollPane = new JScrollPane(table);

        // 添加 滚动面板 到 内容面板
        add(scrollPane);
    }

    /**
     * 获取主模块
     *
     * @return
     */
    public MainModule getMainModule() {
        return mainModule;
    }

    /**
     * 新增任务
     *
     * @param m3u8
     */
    public void addTask(M3u8 m3u8) {
        DownloadingTableModel model = (DownloadingTableModel) table.getModel();
        model.addRow(m3u8);
    }

    public List<M3u8> getTasks() {
        DownloadingTableModel model = (DownloadingTableModel) table.getModel();
        return model.getM3u8s();
    }
}
