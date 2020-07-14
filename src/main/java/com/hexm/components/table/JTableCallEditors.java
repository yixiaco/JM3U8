package com.hexm.components.table;

import com.hexm.util.IconUtil;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author hexm
 * @date 2020/6/4 16:41
 */
public class JTableCallEditors {

    /**
     * 进度条渲染器
     *
     * @return
     */
    public static ProgressBarRenderer progressBarRenderer() {
        return new ProgressBarRenderer();
    }

    static class ProgressBarRenderer extends JProgressBar implements TableCellRenderer {
        public ProgressBarRenderer() {
            super();
            setBorder(BorderFactory.createEmptyBorder(7, 3, 7, 3));
            setOpaque(true);
            //进度百分比
            setStringPainted(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            System.out.println("下载进度：" + value);
            setValue(((Double) value).intValue());
            return this;
        }
    }

    /**
     * 操作框编辑器
     */
    public static class OperatingCellEditor extends AbstractCellEditor implements TableCellEditor {
        private int row;
        protected JPanel jPanel;
        private final JButton start;
        private final JButton pause;
        private JButton del;
        private JTable table;

        public OperatingCellEditor() {
            jPanel = new JPanel();
            start = new JButton(IconUtil.getIcon("/images/start.png", 16, 16));
            pause = new JButton(IconUtil.getIcon("/images/pause.png", 16, 16));
            del = new JButton(IconUtil.getIcon("/images/del.png", 16, 16));
            JButton edit = new JButton(IconUtil.getIcon("/images/edit.png"));
            JButton log = new JButton(IconUtil.getIcon("/images/log.png"));

            start.setPreferredSize(new Dimension(40, 25));
            pause.setPreferredSize(new Dimension(40, 25));
            del.setPreferredSize(new Dimension(40, 25));
            edit.setPreferredSize(new Dimension(40, 25));
            log.setPreferredSize(new Dimension(40, 25));

            start.addActionListener(e -> {
                fireEditingStopped();
                System.out.println("点击start:" + row);
                check();
                getModel().start(row);
            });
            pause.addActionListener(e -> {
                fireEditingStopped();
                System.out.println("点击pause:" + row);
                check();
                getModel().pause(row);
            });
            del.addActionListener(e -> {
                fireEditingStopped();
                System.out.println("点击del:" + row);
                getModel().del(row);
            });
            edit.addActionListener(e -> {
                new DownloadM3u8Edit(getModel().getM3u8(row), pause::doClick);
            });
            log.addActionListener(e -> {
                getModel().openLogDialog(row);
            });
            check();
            jPanel.add(start);
            jPanel.add(pause);
            jPanel.add(del);
            jPanel.add(edit);
            jPanel.add(log);
        }

        public DownloadingTableModel getModel() {
            return (DownloadingTableModel) table.getModel();
        }

        public int getVal() {
            return table == null ? -1 : getModel().status(row);
        }

        /**
         * 结束编辑
         *
         * @return
         */
        @Override
        public Object getCellEditorValue() {
            return getVal();
        }

        private void check() {
            int val = getVal();
            if (val == -1) {
                start.setVisible(true);
                pause.setVisible(false);
            } else if (val == 1) {
                start.setVisible(false);
                pause.setVisible(true);
            }
        }

        /**
         * 开始编辑前
         *
         * @param table
         * @param value
         * @param isSelected
         * @param row
         * @param column
         * @return
         */
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.table = table;
            this.row = row;
            check();
            return jPanel;
        }
    }

    /**
     * 操作框渲染器
     */
    public static class OperatingRenderer extends JPanel implements TableCellRenderer {
        private final JButton start;
        private final JButton pause;
        private JButton del;
        private int row;
        private JTable table;

        public OperatingRenderer() {
            super();
            setOpaque(true);
            start = new JButton(IconUtil.getIcon("/images/start.png", 16, 16));
            pause = new JButton(IconUtil.getIcon("/images/pause.png", 16, 16));
            del = new JButton(IconUtil.getIcon("/images/del.png", 16, 16));
            JButton edit = new JButton(IconUtil.getIcon("/images/edit.png"));
            JButton log = new JButton(IconUtil.getIcon("/images/log.png"));

            start.setPreferredSize(new Dimension(40, 25));
            pause.setPreferredSize(new Dimension(40, 25));
            del.setPreferredSize(new Dimension(40, 25));
            edit.setPreferredSize(new Dimension(40, 25));
            log.setPreferredSize(new Dimension(40, 25));

            check();
            add(start);
            add(pause);
            add(del);
            add(edit);
            add(log);
        }

        public int getVal() {
            return table == null ? -1 : ((DownloadingTableModel) table.getModel()).status(row);
        }

        private void check() {
            int val = getVal();
            if (val == -1) {
                start.setVisible(true);
                pause.setVisible(false);
            } else if (val == 1) {
                start.setVisible(false);
                pause.setVisible(true);
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            this.row = row;
            this.table = table;
            System.out.println("value：" + value);
            check();
            return this;
        }
    }

    /**
     * 操作框编辑器
     */
    public static class CompletedOperatingCellEditor extends AbstractCellEditor implements TableCellEditor {
        private int row;
        protected JPanel jPanel;
        private JButton del;
        private JTable table;

        public CompletedOperatingCellEditor() {
            jPanel = new JPanel();
            del = new JButton(IconUtil.getIcon("/images/del.png", 16, 16));
            JButton log = new JButton(IconUtil.getIcon("/images/log.png"));
            JButton open = new JButton(IconUtil.getIcon("/images/open.png"));
            del.setPreferredSize(new Dimension(40, 25));
            log.setPreferredSize(new Dimension(40, 25));
            open.setPreferredSize(new Dimension(40, 25));
            del.addActionListener(e -> {
                fireEditingStopped();
                System.out.println("点击del:" + row);
                getModel().removeRow(row);
            });
            open.addActionListener(e -> getModel().openDir(row));
            log.addActionListener(e -> getModel().openLogDialog(row));
            jPanel.add(del);
            jPanel.add(log);
            jPanel.add(open);
        }

        public DownloadCompletedTableModel getModel() {
            return (DownloadCompletedTableModel) table.getModel();
        }

        /**
         * 结束编辑
         *
         * @return
         */
        @Override
        public Object getCellEditorValue() {
            return null;
        }

        /**
         * 开始编辑前
         *
         * @param table
         * @param value
         * @param isSelected
         * @param row
         * @param column
         * @return
         */
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.table = table;
            this.row = row;
            return jPanel;
        }
    }

    /**
     * 操作框渲染器
     */
    public static class CompletedOperatingRenderer extends JPanel implements TableCellRenderer {

        public CompletedOperatingRenderer() {
            super();
            setOpaque(true);
            JButton del = new JButton(IconUtil.getIcon("/images/del.png", 16, 16));
            JButton log = new JButton(IconUtil.getIcon("/images/log.png"));
            JButton open = new JButton(IconUtil.getIcon("/images/open.png"));
            del.setPreferredSize(new Dimension(40, 25));
            log.setPreferredSize(new Dimension(40, 25));
            open.setPreferredSize(new Dimension(40, 25));
            add(del);
            add(log);
            add(open);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }
}

