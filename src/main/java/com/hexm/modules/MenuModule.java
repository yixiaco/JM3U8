package com.hexm.modules;

import com.hexm.GlobalEnv;
import com.hexm.swing.MyMenuItem;
import com.hexm.util.DialogUtil;
import com.hexm.util.FontUtil;
import com.hexm.util.IconUtil;
import com.hexm.util.MessageUtil;
import com.hexm.util.StringUtil;

import javax.swing.*;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;
import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * 菜单模块
 *
 * @author hexm
 * @date 2020/6/2
 */
public class MenuModule extends JMenuBar {

    private JMenu base;
    private JMenu setting;
    private JMenu help;
    private final JFrame frame;
    private String host;
    private String port;
    private Integer type;
    private Boolean open;

    private MenuModule(JFrame frame) {
        this.frame = frame;
        init();
        frame.setJMenuBar(this);
    }

    public static void append(JFrame frame) {
        new MenuModule(frame);
    }

    private void init() {
        base = new JMenu("基本");
        base.setFont(FontUtil.menu());
        base.add(MyMenuItem.of("退出")
                .setPreferredSize(100, 25)
                .addActionListeners(e -> System.exit(0)));

        setting = new JMenu("设置");
        setting.setFont(FontUtil.menu());
        setting.add(MyMenuItem.of("设置代理")
                .setPreferredSize(120, 25)
                .addActionListeners(e -> openProxyDialog()));

        help = new JMenu("帮助");
        help.setFont(FontUtil.menu());
        help.add(MyMenuItem.of("GC清理内存")
                .setPreferredSize(130, 25)
                .addActionListeners(e -> System.gc()));
        help.add(MyMenuItem.of("JVM内存信息")
                .addActionListeners(e -> openJvm()));
        help.add(MyMenuItem.of("关于")
                .addActionListeners(e -> openAbout()));

        add(base);
        add(setting);
        add(help);
    }

    /**
     * 打开代理
     */
    private void openProxyDialog() {
        // 创建一个模态对话框
        final JDialog dialog = new JDialog(frame, "新建下载", true);
        // 设置对话框的宽高
        dialog.setSize(400, 260);
        // 设置对话框大小不可改变
        dialog.setResizable(false);
        // 设置对话框相对显示的位置
        dialog.setLocationRelativeTo(frame);
        //内容面板
        JPanel content = new JPanel();
        //面板边距
        content.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        //放置host组件
        JTextField host = placeHostComponent(content);
        //放置port组件
        JTextField port = placePortComponent(content);
        //放置代理方式组件
        JComboBox<String> box = placeTypeComponent(content);
        //放置开关组件
        JToggleButton toggleButton = placeToggleComponent(content);
        //确定和取消按钮
        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel1.setPreferredSize(new Dimension(380, 50));
        // 创建一个按钮用于关闭对话框
        JButton okBtn = new JButton("确定");
        JButton cancelBtn = new JButton("取消");
        okBtn.addActionListener(e -> {
            if (toggleButton.isSelected()) {
                if (StringUtil.isEmpty(host.getText())) {
                    MessageUtil.warning(dialog, "警告", "代理地址不能为空！");
                    return;
                }
                if (StringUtil.isEmpty(port.getText())) {
                    MessageUtil.warning(dialog, "警告", "代理端口不能为空！");
                    return;
                }
                try {
                    if (box.getSelectedIndex() == 0) {
                        GlobalEnv.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host.getText(), Integer.parseInt(port.getText())));
                    }
                    if (box.getSelectedIndex() == 1) {
                        GlobalEnv.proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host.getText(), Integer.parseInt(port.getText())));
                    }
                } catch (Exception e1) {
                    MessageUtil.error(dialog, "错误", e1.getMessage());
                    return;
                }
            } else {
                GlobalEnv.proxy = null;
            }
            this.host = host.getText();
            this.port = port.getText();
            this.open = toggleButton.isSelected();
            this.type = box.getSelectedIndex();
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        // 添加组件到面板
        panel1.add(okBtn);
        panel1.add(cancelBtn);
        content.add(panel1);
        //设置dialog面板
        dialog.setContentPane(content);
        //显示dialog
        dialog.setVisible(true);
    }

    /**
     * 放置host组件
     *
     * @param panel 面板对象
     * @return 返回有效值对象
     */
    private JTextField placeHostComponent(JPanel panel) {
        JLabel label = new JLabel("代理地址:");
        JTextField text = new JTextField();
        label.setPreferredSize(new Dimension(80, 25));
        text.setPreferredSize(new Dimension(250, 25));
        if (host != null) {
            text.setText(host);
        }
        panel.add(label);
        panel.add(text);
        return text;
    }

    /**
     * 放置host组件
     *
     * @param panel 面板对象
     * @return 返回有效值对象
     */
    private JTextField placePortComponent(JPanel panel) {
        JLabel label = new JLabel("代理端口:");
        JTextField text = new JTextField();
        label.setPreferredSize(new Dimension(80, 25));
        text.setPreferredSize(new Dimension(250, 25));
        if (port != null) {
            text.setText(port);
        }
        panel.add(label);
        panel.add(text);
        return text;
    }

    /**
     * 放置代理方式组件
     *
     * @param panel 面板对象
     * @return 返回有效值对象
     */
    private JComboBox<String> placeTypeComponent(JPanel panel) {
        // 需要选择的条目
        String[] listData = new String[]{"HTTP", "SOCKS"};
        JLabel label = new JLabel("代理方式:");
        JComboBox<String> comboBox = new JComboBox<>(listData);
        label.setPreferredSize(new Dimension(80, 25));
        comboBox.setPreferredSize(new Dimension(250, 25));
        if (type != null) {
            comboBox.setSelectedIndex(type);
        }
        panel.add(label);
        panel.add(comboBox);
        return comboBox;
    }

    /**
     * 放置开关组件
     *
     * @param panel 面板对象
     * @return 返回有效值对象
     */
    private JToggleButton placeToggleComponent(JPanel panel) {
        JLabel label = new JLabel("代理开关:");
        // 创建开关按钮
        JToggleButton toggleBtn = new JToggleButton();
        toggleBtn.setSelected(true);
        toggleBtn.setUI(new BasicToggleButtonUI());

        if (open != null) {
            toggleBtn.setSelected(open);
        }

        // 首先设置不绘制按钮边框
        toggleBtn.setBorderPainted(false);

        // 设置 选中(开) 和 未选中(关) 时显示的图片
        toggleBtn.setSelectedIcon(IconUtil.getIcon("/images/toggle_on.png"));
        toggleBtn.setIcon(IconUtil.getIcon("/images/toggle_off.png"));

        label.setPreferredSize(new Dimension(80, 25));

        panel.add(label);
        panel.add(toggleBtn);
        return toggleBtn;
    }

    /**
     * JVM内存信息
     */
    private void openJvm() {
        String html = String.format("<html>最大可用内存(对应-Xmx): %.2fMb<br/>" +
                        "当前JVM空闲内存: %.2fMb<br/>" +
                        "当前JVM占用的内存总数: %.2fMb<br/>" +
                        "当前JVM可用处理器数量: %d<br/></html>",
                (double) Runtime.getRuntime().maxMemory() / 1024 / 1024,
                (double) Runtime.getRuntime().freeMemory() / 1024 / 1024,
                (double) Runtime.getRuntime().totalMemory() / 1024 / 1024,
                Runtime.getRuntime().availableProcessors());
        DialogUtil.sDialog(380, 140, "JVM内存信息", html);
    }

    /**
     * JVM内存信息
     */
    private void openAbout() {
        DialogUtil.sDialog(250, 100, "关于", "<html>作者： hexm<br/>当前版本号为： " + GlobalEnv.VERSION + "</html>");
    }

    public JMenu getBase() {
        return base;
    }

    public JMenu getSetting() {
        return setting;
    }

    public JMenu getHelp() {
        return help;
    }
}
