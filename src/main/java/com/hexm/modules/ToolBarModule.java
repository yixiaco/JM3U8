package com.hexm.modules;

import com.hexm.Valid;
import com.hexm.enums.ChooserType;
import com.hexm.m3u8.M3u8;
import com.hexm.m3u8.RamM3u8;
import com.hexm.m3u8.RequestParams;
import com.hexm.swing.ModifiedFlowLayout;
import com.hexm.util.IconUtil;
import com.hexm.util.MessageUtil;
import com.hexm.util.StringUtil;

import javax.swing.*;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * 工具栏模块
 *
 * @author hexm
 * @date 2020/6/3
 */
public class ToolBarModule extends JToolBar {

    private final MainModule mainModule;
    private JTextArea head;
    private JTextField url;
    private JTextField outPut;
    private JTextField name;
    private JTextArea content;
    private JTextField relativeUrl;
    private JToggleButton ram;
    private M3u8 m3u8;

    private JToggleButton newTask;

    private ToolBarModule(MainModule mainModule) {
        super();
        this.mainModule = mainModule;
        init();
    }

    /**
     * 添加到主模块中
     *
     * @param mainModule
     * @return
     */
    public static ToolBarModule append(MainModule mainModule) {
        return new ToolBarModule(mainModule);
    }

    /**
     * 初始化
     */
    private void init() {
        // 创建 工具栏按钮
        newTask = new JToggleButton("新建", IconUtil.getIcon("/images/add.png"));
        newTask.setBorderPainted(false);
        newTask.setBorder(BorderFactory.createEmptyBorder(3, 12, 3, 12));
        newTask.addActionListener(e -> openDialog(false));
        add(newTask);
        //添加到主模块中
        mainModule.add(this, BorderLayout.PAGE_START);
    }

    /**
     * 新建任务对话框
     */
    private void openDialog(final boolean isContentModel) {
        // 创建一个模态对话框
        final JDialog dialog = new JDialog(mainModule.getFrame(), "新建下载", true);
        // 设置对话框的宽高
        dialog.setSize(516, isContentModel ? 580 : 475);
        // 设置对话框大小不可改变
        dialog.setResizable(false);
        // 设置对话框相对显示的位置
        dialog.setLocationRelativeTo(mainModule);

        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel1.setPreferredSize(new Dimension(480, 50));
        // 创建一个按钮用于关闭对话框
        JButton okBtn = new JButton("确定");
        //关闭窗口监听改变按钮的状态值
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //还原按钮未选中状态
                newTask.setSelected(false);
            }
        });
        okBtn.addActionListener(e -> {
            if (isContentModel) {
                if (!Valid.isNotEmpty(content, relativeUrl, outPut, name)) {
                    MessageUtil.error(dialog, "错误提示", "m3u8内容、相对地址、输出地址、文件命名不能为空。");
                    return;
                }
            } else {
                if (!Valid.isNotEmpty(url, outPut, name)) {
                    MessageUtil.error(dialog, "错误提示", "M3U8-URL、输出地址、文件命名不能为空。");
                    return;
                }
            }
            initM3u8(isContentModel);
            // 关闭对话框
            dialog.dispose();
            //还原按钮未选中状态
            newTask.setSelected(false);
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
        if (isContentModel && ram.isSelected()) {
            m3u8 = new RamM3u8(relativeUrl.getText(), content.getText(), new File(outPut.getText()), name.getText().trim());
        } else if (isContentModel) {
            m3u8 = new M3u8(relativeUrl.getText(), content.getText(), new File(outPut.getText()), name.getText().trim());
        } else if (ram.isSelected()) {
            m3u8 = new RamM3u8(url.getText(), new File(outPut.getText()), name.getText().trim());
        } else {
            m3u8 = new M3u8(url.getText(), new File(outPut.getText()), name.getText().trim());
        }
        if (StringUtil.isNotEmpty(head.getText())) {
            m3u8.setRequestParams(heads(head.getText()));
        }
        //添加到任务列表中
        mainModule.getDownloading().addTask(m3u8);
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
        outPut = placeOutputComponent(panel);
        name = placeNameComponent(panel);
        ram = placeRamToggleComponent(panel);
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
        outPut = placeOutputComponent(panel);
        name = placeNameComponent(panel);
        ram = placeRamToggleComponent(panel);
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
        jScrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        label.setPreferredSize(new Dimension(100, 25));
        jScrollPane.setPreferredSize(new Dimension(480, 25 * 3));
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
        panel.add(label);
        panel.add(text);
        return text;
    }

    /**
     * 放置输出地址组件
     *
     * @param panel 面板对象
     * @return 返回有效值对象
     */
    private JTextField placeOutputComponent(JPanel panel) {
        JLabel label = new JLabel("*输出地址:");
        JTextField text = new JTextField(System.getProperty("user.dir") + File.separator + "output");
        JButton button = new JButton("浏览");
        label.setPreferredSize(new Dimension(80, 25));
        text.setPreferredSize(new Dimension(480, 25));
        button.setPreferredSize(new Dimension(80, 25));
        button.addActionListener(e -> openChoose(text, ChooserType.DIRECTORIES));
        text.setToolTipText("该地址将作为临时ts存储地址与合并和存放文件的地址。");
        panel.add(label);
        panel.add(button);
        panel.add(text);
        return text;
    }

    /**
     * 放置文件名称组件
     *
     * @param panel 面板对象
     * @return 返回有效值对象
     */
    private JTextField placeNameComponent(JPanel panel) {
        JLabel label = new JLabel("*文件命名:");
        JTextField text = new JTextField();
        label.setPreferredSize(new Dimension(80, 25));
        text.setPreferredSize(new Dimension(395, 25));
        panel.add(label);
        panel.add(text);
        return text;
    }

    /**
     * 放置内存模式开关组件
     *
     * @param panel 面板对象
     * @return 返回有效值对象
     */
    private JToggleButton placeRamToggleComponent(JPanel panel) {
        JLabel label = new JLabel("使用内存做临时存储:");
        // 创建开关按钮
        JToggleButton toggleBtn = new JToggleButton();
        toggleBtn.setSelected(true);
        toggleBtn.setUI(new BasicToggleButtonUI());

        // 首先设置不绘制按钮边框
        toggleBtn.setBorderPainted(false);

        // 设置 选中(开) 和 未选中(关) 时显示的图片
        toggleBtn.setSelectedIcon(IconUtil.getIcon("/images/toggle_on.png"));
        toggleBtn.setIcon(IconUtil.getIcon("/images/toggle_off.png"));

        label.setPreferredSize(new Dimension(150, 25));

        panel.add(label);
        panel.add(toggleBtn);
        return toggleBtn;
    }

    /**
     * 选择目录
     *
     * @param textField
     */
    private static void openChoose(JTextField textField, ChooserType chooserType) {
        JFileChooser chooser = new JFileChooser(textField.getText());
        //设置文件选择器只能选择0（文件），1（文件夹）
        switch (chooserType) {
            case DIRECTORIES:
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                break;
            case FILES:
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                break;
            default:
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        }
        //打开文件浏览器，点击取消则返回1
        int status = chooser.showOpenDialog(null);
        if (status != 1) {
            File file = chooser.getSelectedFile();
            textField.setText(file.getPath());
        }
    }

    public MainModule getMainModule() {
        return mainModule;
    }
}
