package com.hexm.util;

import java.math.BigDecimal;

/**
 * 数字计算
 *
 * @author hexm
 * @date 2020/6/4
 */
public class NumberUtil {

    /**
     * 除
     *
     * @param v1
     * @param v2
     * @return
     */
    public static double div(Integer v1, Integer v2) {
        if (d(v1) == 0 || d(v2) == 0) {
            return 0;
        }
        return div(v1.doubleValue(), v2.doubleValue());
    }

    /**
     * 除
     *
     * @param v1
     * @param v2
     * @return
     */
    public static double div(Double v1, Double v2) {
        if (d(v1) == 0 || d(v2) == 0) {
            return 0;
        }
        return BigDecimal.valueOf(d(v1)).divide(BigDecimal.valueOf(d(v2)), 4, BigDecimal.ROUND_DOWN).doubleValue();
    }

    /**
     * 乘
     *
     * @param v1
     * @param v2
     * @return
     */
    public static double mul(Double v1, Double v2) {
        if (v1 == null || v2 == null) {
            return 0;
        }
        return BigDecimal.valueOf(d(v1)).multiply(BigDecimal.valueOf(d(v2))).doubleValue();
    }

    /**
     * 乘
     *
     * @param v1
     * @param v2
     * @return
     */
    public static double mul(Integer v1, Integer v2) {
        if (v1 == null || v2 == null) {
            return 0;
        }
        return mul(v1.doubleValue(), v2.doubleValue());
    }

    private static double d(Double d) {
        if (d == null) {
            return 0;
        }
        return d;
    }

    private static double d(Integer d) {
        if (d == null) {
            return 0;
        }
        return d;
    }
}
