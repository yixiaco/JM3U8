package com.hexm.components.table;

import com.hexm.GlobalEnv;
import com.hexm.m3u8.DownloadStatus;
import com.hexm.m3u8.M3u8;
import com.hexm.m3u8.M3u8Event;
import com.hexm.modules.DownloadCompletedModule;
import com.hexm.util.NumberUtil;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollPaneUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 正在下载模块表格模型
 *
 * @author hexm
 * @date 2020/6/6 15:25
 */
public class DownloadingTableModel extends DefaultTableModel {
    private DownloadingTable table;
    private final List<M3u8> m3u8s = new ArrayList<>();
    private CompletableFuture<?> future;

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
        return column == DownloadingTable.OPERATING;
    }

    /**
     * 新增一行
     *
     * @param m3u8
     */
    public void addRow(M3u8 m3u8) {
        thread();
        m3u8s.add(m3u8);
        addRow(new Object[]{m3u8.getFilename(), "0/0", "0", "-", 0d, -1});
        m3u8.addListener(e -> {
            if (e == M3u8Event.SUCCESS) {
                for (int i = 0; i < m3u8s.size(); i++) {
                    if (m3u8s.get(i).getId().equals(m3u8.getId())) {
                        ((DownloadCompletedTableModel) DownloadCompletedModule.table.getModel()).addRow(m3u8);
                        removeRow(i);
                        break;
                    }
                }
                fireTableRowsUpdated(0, m3u8s.size());
                for (int i = 0; i < m3u8s.size(); i++) {
                    fireTableCellUpdated(i, DownloadingTable.OPERATING);
                }
            } else if (e == M3u8Event.CHANGE) {
                for (int i = 0; i < m3u8s.size(); i++) {
                    if (m3u8s.get(i).getId().equals(m3u8.getId())) {
                        setValueAt(m3u8.getScrap() + "/" + m3u8.getTotalScrap(), i, DownloadingTable.SCRAP);
                        setValueAt(NumberUtil.mul(NumberUtil.div(m3u8.getScrap().get(), m3u8.getTotalScrap()), 100d), i, DownloadingTable.PROGRESS_BAR);
                        fireTableCellUpdated(i, DownloadingTable.SCRAP);
                        fireTableCellUpdated(i, DownloadingTable.PROGRESS_BAR);
                        fireTableCellUpdated(i, DownloadingTable.OPERATING);
                        break;
                    }
                }
            } else {
                for (int i = 0; i < m3u8s.size(); i++) {
                    if (m3u8s.get(i).getId().equals(m3u8.getId())) {
                        fireTableCellUpdated(i, DownloadingTable.OPERATING);
                        break;
                    }
                }
            }
        });
        //重新渲染列
        table.rendererColumn();
    }

    /**
     * 统计数据
     */
    private synchronized void thread() {
        if (future == null || future.isDone()) {
            future = CompletableFuture.runAsync(() -> {
                while (true) {
                    try {
                        if (m3u8s.isEmpty()) {
                            return;
                        }
                        for (int i = 0; i < m3u8s.size(); i++) {
                            M3u8 m3u8 = m3u8s.get(i);
                            //下载速度
                            setValueAt(m3u8.speed(), i, DownloadingTable.SPEED);
                            //耗时
                            m3u8.timing();
                            setValueAt(LocalTime.of(0, 0, 0).plusSeconds(m3u8.getTiming()), i, DownloadingTable.CONSUMING);
                        }
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
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
        setLabelText(label, m3u8.getLog().toString(), jScrollPane);
        m3u8.setLogListener(s -> setLabelText(label, s, jScrollPane));

        jScrollPane.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane.setUI(new BasicScrollPaneUI());
        jScrollPane.setPreferredSize(new Dimension(490, 382));
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
    private void setLabelText(JLabel label, String s, JScrollPane jScrollPane) {
        label.setText("<html>" + s.replaceAll("\n", "<br/>") + "</html>");
        label.setHorizontalTextPosition(SwingConstants.TRAILING);
        JScrollBar sBar = jScrollPane.getVerticalScrollBar();
        sBar.setValue(sBar.getMaximum());
    }

    /**
     * 开始
     *
     * @param row
     */
    public void start(int row) {
        M3u8 m3u8 = m3u8s.get(row);
        m3u8.start();
    }

    /**
     * 暂停
     *
     * @param row
     */
    public void pause(int row) {
        M3u8 m3u8 = m3u8s.get(row);
        m3u8.pause();
    }

    /**
     * 继续
     *
     * @param row
     */
    public void proceed(int row) {
        M3u8 m3u8 = m3u8s.get(row);
        m3u8.start();
    }

    /**
     * 删除
     *
     * @param row
     */
    public void del(int row) {
        M3u8 m3u8 = m3u8s.get(row);
        m3u8.stop();
        removeRow(row);
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
        fireTableDataChanged();
    }

    public M3u8 getM3u8(int row) {
        return m3u8s.get(row);
    }

    public List<M3u8> getM3u8s() {
        return this.m3u8s;
    }

    public int status(int row) {
        M3u8 m3u8 = m3u8s.get(row);
        if (m3u8.getDownloadStatus() == DownloadStatus.DOWNLOADING) {
            return 1;
        } else {
            return -1;
        }
    }

    public void setTable(DownloadingTable table) {
        this.table = table;
    }
}
