/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.util;

import java.util.Arrays;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/21
 */
public class PIDTextInputUtil {

    /**
     *
     * @param text
     * @return
     */
    public static boolean chkPIDFormat(String text) {
        if (!text.matches("[a-zA-Z][1-2][0-9]{8}")) {
            return false;
        }

        String pid = text.toUpperCase();

        int[] headNum = new int[]{
                1, 10, 19, 28, 37,
                46, 55, 64, 39, 73,
                82, 2, 11, 20, 48,
                29, 38, 47, 56, 65,
                74, 83, 21, 3, 12, 30};

        char[] headCharUpper = new char[]{
                'A', 'B', 'C', 'D', 'E', 'F', 'G',
                'H', 'I', 'J', 'K', 'L', 'M', 'N',
                'O', 'P', 'Q', 'R', 'S', 'T', 'U',
                'V', 'W', 'X', 'Y', 'Z'
        };

        int index = Arrays.binarySearch(headCharUpper, pid.charAt(0));
        int base = 8;
        int total = 0;
        for (int i = 1; i < 10; i++) {
            int tmp = Integer.parseInt(Character.toString(pid.charAt(i))) * base;
            total += tmp;
            base--;
        }

        total += headNum[index];
        int remain = total % 10;
        int checkNum = (10 - remain) % 10;
        if (Integer.parseInt(Character.toString(pid.charAt(9))) != checkNum) {
            return false;
        }

        return true;
    }
}
