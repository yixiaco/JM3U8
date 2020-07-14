package com.hexm.util;

import java.math.BigDecimal;

/**
 * @author hexm
 * @date 2020/6/4
 */
public class NumberUtil {

    /**
     * 除
     * @param v1
     * @param v2
     * @return
     */
    public static double div(Integer v1, Integer v2) {
        if (d(v1) == 0 || d(v2) == 0) {
            return 0;
        }
        return BigDecimal.valueOf(d(v1)).divide(BigDecimal.valueOf(d(v2)), 2, BigDecimal.ROUND_DOWN).doubleValue();
    }

    /**
     * 乘
     * @param v1
     * @param v2
     * @return
     */
    public static double mul(Double v1, Double v2) {
        return BigDecimal.valueOf(d(v1)).multiply(BigDecimal.valueOf(d(v2))).doubleValue();
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
