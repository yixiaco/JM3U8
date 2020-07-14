package com.hexm.components.table;

import com.hexm.GlobalEnv;
import com.hexm.Valid;
import com.hexm.m3u8.M3u8;
import com.hexm.m3u8.RequestParams;
import com.hexm.swing.ModifiedFlowLayout;
import com.hexm.util.Apply;
import com.hexm.util.MessageUtil;
import com.hexm.util.StringUtil;

import javax.swing.*;
import java.awt.*;

/**
 * @author hexm
 * @date 2020/7/14 0014 9:39
 */
public class DownloadM3u8Edit {

    private JTextArea head;
    private JTextField url;
    private JTextArea content;
    private JTextField relativeUrl;
    private M3u8 m3u8;
    private Apply apply;

    public DownloadM3u8Edit(M3u8 m3u8, Apply apply) {
        this.m3u8 = m3u8;
        this.apply = apply;
        openDialog(false);
    }

    /**
     * 新建任务对话框
     */
    private void openDialog(final boolean isContentModel) {
        // 创建一个模态对话框
        final JDialog dialog = new JDialog(GlobalEnv.frame, "新建下载", true);
        // 设置对话框的宽高
        dialog.setSize(516, isContentModel ? 435 : 315);
        // 设置对话框大小不可改变
        dialog.setResizable(false);
        // 设置对话框相对显示的位置
        dialog.setLocationRelativeTo(GlobalEnv.frame);

        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel1.setPreferredSize(new Dimension(405, 50));
        // 创建一个按钮用于关闭对话框
        JButton okBtn = new JButton("确定");
        okBtn.addActionListener(e -> {
            if (isContentModel) {
                if (!Valid.isNotEmpty(content, relativeUrl)) {
                    MessageUtil.error(dialog, "错误提示", "m3u8内容、相对地址、文件命名不能为空。");
                    return;
                }
            } else {
                if (!Valid.isNotEmpty(url)) {
                    MessageUtil.error(dialog, "错误提示", "M3U8-URL、文件命名不能为空。");
                    return;
                }
            }
            initM3u8(isContentModel);
            // 关闭对话框
            dialog.dispose();
        });
        // 添加组件到面板
        panel1.add(okBtn);
        //创建模式面板
        JPanel panel;
        if (isContentModel) {
            panel = contentPanel();
            // 创建一个按钮用于关闭对话框
            JButton addUrlBtn = new JButton("添加URL任务");
            addUrlBtn.addActionListener(e -> {
                // 关闭对话框
                dialog.dispose();
                openDialog(false);
            });
            // 添加组件到面板
            panel1.add(addUrlBtn);
        } else {
            // 创建对话框的内容面板, 在面板内可以根据自己的需要添加任何组件并做任意是布局
            panel = urlPanel();
            // 创建一个按钮用于关闭对话框
            JButton addContentBtn = new JButton("添加内容任务");
            addContentBtn.addActionListener(e -> {
                // 关闭对话框
                dialog.dispose();
                openDialog(true);
            });
            // 添加组件到面板
            panel1.add(addContentBtn);
        }

        panel.add(panel1);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        // 设置对话框的内容面板
        dialog.setContentPane(panel);
        dialog.setVisible(true);
        dialog.pack();
    }

    /**
     * 初始化m3u8
     *
     * @param isContentModel
     */
    private void initM3u8(boolean isContentModel) {
        if (isContentModel) {
            m3u8.setRelativeUrl(relativeUrl.getText());
            m3u8.setM3u8Content(content.getText());
        } else {
            m3u8.setUrl(url.getText());
            m3u8.setRelativeUrl(m3u8.getUrl().substring(0, m3u8.getUrl().lastIndexOf("/") + 1));
        }
        if (StringUtil.isNotEmpty(head.getText())) {
            m3u8.setRequestParams(heads(head.getText()));
        }
        if (this.apply != null) {
            this.apply.apply();
        }
        //所有线程暂停
        m3u8.pause();
        //初始化m3u8对象
        m3u8.init();
    }

    private static RequestParams heads(String headText) {
        if (StringUtil.isNotEmpty(headText)) {
            String[] heads = headText.split("\n");
            RequestParams params = RequestParams.getInstance();
            for (String head : heads) {
                if (head.contains(":")) {
                    int index = head.indexOf(":", 1);
                    params.addHead(head.substring(0, index).trim(), head.substring(index + 1).trim());
                }
            }
            return params;
        }
        return null;
    }


    /**
     * url模式面板
     *
     * @return
     */
    private JPanel urlPanel() {
        JPanel panel = new JPanel(new ModifiedFlowLayout(FlowLayout.LEFT, 5, 10));
        head = placeHeadComponent(panel);
        url = placeUrlComponent(panel);
        return panel;
    }

    /**
     * 内容模式面板
     *
     * @return
     */
    private JPanel contentPanel() {
        JPanel panel = new JPanel(new ModifiedFlowLayout(FlowLayout.LEFT, 5, 10));
        head = placeHeadComponent(panel);
        content = placeM3u8ContentComponent(panel);
        relativeUrl = placeRelativeUrlComponent(panel);
        return panel;
    }

    /**
     * 放置请求头组件
     *
     * @param panel 面板对象
     * @return 返回有效值对象
     */
    private JTextArea placeHeadComponent(JPanel panel) {
        JLabel label = new JLabel("请求头:");
        JTextArea textArea = new JTextArea();
        JScrollPane jScrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setAutoscrolls(true);
        textArea.setEditable(true);
        textArea.setToolTipText("示例：cache-control: max-age=0");
        //分别设置水平和垂直滚动条自动出现
        jScrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        label.setPreferredSize(new Dimension(80, 25));
        jScrollPane.setPreferredSize(new Dimension(480, 25 * 3));
        if (m3u8.getRequestParams() != null) {
            StringBuilder sb = new StringBuilder();
            m3u8.getRequestParams().getHeads().forEach((key, val) -> {
                sb.append(key).append(":").append(val).append("\n");
            });
            textArea.setText(sb.toString());
        }
        panel.add(label);
        panel.add(jScrollPane);
        return textArea;
    }

    /**
     * 放置m3u8内容组件
     *
     * @param panel 面板对象
     * @return 返回有效值对象
     */
    private JTextArea placeM3u8ContentComponent(JPanel panel) {
        JLabel label = new JLabel("*m3u8内容:");
        JTextArea textArea = new JTextArea();
        JScrollPane jScrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setAutoscrolls(true);
        textArea.setEditable(true);
        textArea.setToolTipText("示例：#EXTM3U...");
        //分别设置水平和垂直滚动条自动出现
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        label.setPreferredSize(new Dimension(100, 25));
        jScrollPane.setPreferredSize(new Dimension(480, 25 * 3));
        if (m3u8.getM3u8Content() != null) {
            textArea.setText(m3u8.getM3u8Content());
        }
        panel.add(label);
        panel.add(jScrollPane);
        return textArea;
    }

    /**
     * 放置相对地址组件
     *
     * @param panel 面板对象
     * @return 返回有效值对象
     */
    private JTextField placeRelativeUrlComponent(JPanel panel) {
        JLabel label = new JLabel("*m3u8相对地址:");
        JTextField text = new JTextField();
        label.setPreferredSize(new Dimension(120, 25));
        text.setPreferredSize(new Dimension(480, 25));
        text.setToolTipText("不包含.m3u8的地址");
        if (m3u8.getRelativeUrl() != null) {
            text.setText(m3u8.getRelativeUrl());
        }
        panel.add(label);
        panel.add(text);
        return text;
    }

    /**
     * 放置url地址组件
     *
     * @param panel 面板对象
     * @return 返回有效值对象
     */
    private JTextField placeUrlComponent(JPanel panel) {
        JLabel label = new JLabel("*M3U8-URL:");
        JTextField text = new JTextField();
        label.setPreferredSize(new Dimension(100, 25));
        text.setPreferredSize(new Dimension(480, 25));
        if (m3u8.getUrl() != null) {
            text.setText(m3u8.getUrl());
        }
        panel.add(label);
        panel.add(text);
        return text;
    }

}
