package com.hexm.components.table;

import javax.swing.*;
import javax.swing.table.TableColumnModel;

/**
 * 正在下载表格
 *
 * @author hexm
 * @date 2020/6/8 16:52
 */
public class DownloadingTable extends JTable {

    public static final int FILENAME = 0;
    public static final int SCRAP = 1;
    public static final int CONSUMING = 2;
    public static final int SPEED = 3;
    public static final int PROGRESS_BAR = 4;
    public static final int OPERATING = 5;

    public DownloadingTable() {
        super(new DownloadingTableModel());
        // 表头（列名）
        String[] tableHeads = {"文件名", "碎片", "耗时", "下载速度", "进度", "操作"};
        DownloadingTableModel model = (DownloadingTableModel) getModel();
        model.setTable(this);
        model.setColumnIdentifiers(tableHeads);
        rendererColumn();
        //不显示垂直线
        setShowVerticalLines(false);
        // 设置行高
        setRowHeight(35);
    }

    /**
     * 渲染列
     */
    public void rendererColumn() {
        TableColumnModel columnModel = getColumnModel();
        columnModel.getColumn(PROGRESS_BAR).setCellRenderer(JTableCallEditors.progressBarRenderer());
        columnModel.getColumn(OPERATING).setCellRenderer(new JTableCallEditors.OperatingRenderer());
        columnModel.getColumn(OPERATING).setCellEditor(new JTableCallEditors.OperatingCellEditor());
        columnModel.getColumn(OPERATING).setPreferredWidth(130);
    }

}
