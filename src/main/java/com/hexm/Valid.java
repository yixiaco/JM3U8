package com.hexm;

import com.hexm.util.StringUtil;

import javax.swing.text.JTextComponent;

/**
 * @author hexm
 * @date 2020/6/1
 */
public class Valid {

    /**
     * 验证组件是否为空
     *
     * @param components
     * @return
     */
    public static boolean isNotEmpty(JTextComponent... components) {
        for (JTextComponent component : components) {
            if (StringUtil.isEmpty(component.getText())) {
                return false;
            }
        }
        return true;
    }
}
