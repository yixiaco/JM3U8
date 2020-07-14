package com.hexm.components.table;

import javax.swing.*;
import javax.swing.table.TableColumnModel;

/**
 * 下载完成表格
 * @author hexm
 * @date 2020/6/8 16:52
 */
public class DownloadCompletedTable extends JTable {

    public static final int FILENAME       = 0;
    public static final int SCRAP          = 1;
    public static final int CONSUMING      = 2;
    public static final int OPERATING      = 3;

    public DownloadCompletedTable() {
        super(new DownloadCompletedTableModel());
        // 表头（列名）
        String[] tableHeads = {"文件名", "碎片", "耗时", "操作"};
        DownloadCompletedTableModel model = (DownloadCompletedTableModel) getModel();
        model.setTable(this);
        model.setColumnIdentifiers(tableHeads);
        TableColumnModel columnModel = getColumnModel();
        //这里的3指的是第几列,从0开始计数
        columnModel.getColumn(OPERATING).setCellRenderer(new JTableCallEditors.CompletedOperatingRenderer());
        columnModel.getColumn(OPERATING).setCellEditor(new JTableCallEditors.CompletedOperatingCellEditor());
        //不显示垂直线
        setShowVerticalLines(false);
        // 设置行高
        setRowHeight(35);
    }

}
