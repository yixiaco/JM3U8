package com.hexm.swing;

import com.hexm.util.FontUtil;
import com.hexm.util.IconUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * 菜单item
 *
 * @author hexm
 * @date 2020/6/2 8:55
 */
public class MyMenuItem extends JMenuItem {
    public MyMenuItem() {
        this(null, null);
    }

    public MyMenuItem(Icon icon) {
        this(null, icon);
    }

    public MyMenuItem(String text) {
        this(text, null);
    }

    public MyMenuItem(Action a) {
        super(a);
    }

    public MyMenuItem(String text, Icon icon) {
        super(text, icon);
    }

    public MyMenuItem(String text, int mnemonic) {
        super(text, mnemonic);
    }

    public static MyMenuItem of(String text) {
        MyMenuItem item = new MyMenuItem(text, null);
        item.setFont(FontUtil.menu());
        item.setHorizontalAlignment(SwingConstants.CENTER);
        return item;
    }

    public static MyMenuItem of(String text, String iconPath) {
        MyMenuItem item = new MyMenuItem(text, IconUtil.getIcon(iconPath));
        item.setHorizontalAlignment(SwingConstants.CENTER);
        return item;
    }

    public MyMenuItem addActionListeners(ActionListener l) {
        super.addActionListener(l);
        return this;
    }

    public MyMenuItem setPreferredSize(int width, int height) {
        super.setPreferredSize(new Dimension(width, height));
        return this;
    }

}
