package com.hexm.components.table;

import com.hexm.GlobalEnv;
import com.hexm.m3u8.M3u8;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollPaneUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 下载完成模块表格模型
 *
 * @author hexm
 * @date 2020/6/6 15:25
 */
public class DownloadCompletedTableModel extends DefaultTableModel {
    private DownloadCompletedTable table;
    private final List<M3u8> m3u8s = new ArrayList<>();

    /**
     * Returns true regardless of parameter values.
     *
     * @param row    the row whose value is to be queried
     * @param column the column whose value is to be queried
     * @return true
     * @see #setValueAt
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        return column == DownloadCompletedTable.OPERATING;
    }

    /**
     * 新增一行
     *
     * @param m3u8
     */
    public void addRow(M3u8 m3u8) {
        m3u8s.add(0, m3u8);
        insertRow(0, new Object[]{m3u8.getFilename(), m3u8.getScrap() + "/" + m3u8.getTotalScrap(), LocalTime.of(0, 0, 0).plusSeconds(m3u8.getTiming()), -1});
    }

    /**
     * 打开日志弹窗
     *
     * @param row
     */
    public void openLogDialog(int row) {
        M3u8 m3u8 = m3u8s.get(row);
        openDialog(m3u8);
    }

    /**
     * 日志弹窗
     *
     * @param m3u8
     */
    private void openDialog(M3u8 m3u8) {
        // 创建一个模态对话框
        final JDialog dialog = new JDialog(GlobalEnv.frame, "日志", true);
        // 设置对话框的宽高
        dialog.setSize(500, 420);
        // 设置对话框大小不可改变
        dialog.setResizable(false);
        // 设置对话框相对显示的位置
        dialog.setLocationRelativeTo(GlobalEnv.frame);
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel();
        JScrollPane jScrollPane = new JScrollPane(label);
        Font font = new Font("微软雅黑", Font.PLAIN, 12);
        label.setAutoscrolls(true);
        label.setFont(font);
        label.setForeground(Color.white);
        label.setBackground(Color.black);
        setLabelText(label, m3u8.getLog().toString());
        m3u8.setLogListener(s -> setLabelText(label, s));

        jScrollPane.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane.setUI(new BasicScrollPaneUI());
        jScrollPane.setPreferredSize(new Dimension(490, 385));
        panel.setBackground(Color.black);
        jScrollPane.getViewport().setBackground(Color.black);
        panel.add(jScrollPane);
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    /**
     * 设置label文本
     *
     * @param label
     * @param s
     */
    private void setLabelText(JLabel label, String s) {
        label.setText("<html>" + s.replaceAll("\n", "<br/>") + "</html>");
        label.setHorizontalTextPosition(SwingConstants.TRAILING);
    }

    public void openDir(int row){
        try {
            M3u8 m3u8 = m3u8s.get(row);
            Desktop.getDesktop().open(m3u8.getStore());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除一行
     *
     * @param row
     */
    @Override
    public void removeRow(int row) {
        m3u8s.remove(row);
        super.removeRow(row);
    }

    public M3u8 getM3u8(int row) {
        return m3u8s.get(row);
    }

    public List<M3u8> getM3u8s() {
        return this.m3u8s;
    }

    public void setTable(DownloadCompletedTable table) {
        this.table = table;
    }
}
