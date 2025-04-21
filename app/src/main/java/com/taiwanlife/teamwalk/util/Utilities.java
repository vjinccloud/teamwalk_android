/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.util;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.andrognito.patternlockview.PatternLockView;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/12/15
 */
public class Utilities {

    private static final String salt = "taiwanlife";
    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public static String randomString() {
//        int leftLimit = 48; // numeral '0'
//        int rightLimit = 122; // letter 'z'
//        int targetStringLength = 10;
////        Random random = new Random();
//        SecureRandom random = new SecureRandom();
//
//        return random.ints(leftLimit, rightLimit + 1)
//                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
//                .limit(targetStringLength)
//                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
//                .toString();

//        int leftLimit = 48; // numeral '0'
//        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        StringBuilder sb = new StringBuilder();
        String letter = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < targetStringLength; i++) {
            int randomInt = secureRandom.nextInt(letter.length());
//            char random = (char)(leftLimit + randomInt);
            sb.append(letter.charAt(randomInt));
        }
        return sb.toString();
    }

    /**
     *
     * @param s
     * @param keyString
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static String sha1(String s, String keyString) throws
            UnsupportedEncodingException, NoSuchAlgorithmException,
            InvalidKeyException {

        SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(key);

        byte[] bytes = mac.doFinal(s.getBytes("UTF-8"));

        return new String( Base64.encodeToString(bytes, Base64.NO_WRAP) );
    }

    public static String patternToString(PatternLockView patternLockView,
                                         List<PatternLockView.Dot> pattern, String fid) {
        if (pattern == null) {
            return "";
        }
        int patternSize = pattern.size();
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < patternSize; i++) {
            PatternLockView.Dot dot = pattern.get(i);
//            stringBuilder.append((patternSize * patternLockView.getDotCount() + patternSize));
            stringBuilder.append(dot.getRow());
            stringBuilder.append(dot.getColumn());
        }
        return stringBuilder.toString() + fid;
    }


    public static String patternToSha256(PatternLockView patternLockView,
                                         List<PatternLockView.Dot> pattern, String fid) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(patternToString(patternLockView, pattern, fid).getBytes("UTF-8"));
            Log.i("patternToSha256 toStr: " , patternToString(patternLockView, pattern, fid));
            byte[] digest = messageDigest.digest();
            BigInteger bigInteger = new BigInteger(1, digest);
            return String.format((Locale) null,
                    "%0" + (digest.length * 2) + "x", bigInteger).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String getDateNow() {
        Date date = new Date();
        return dateFormat.format(date);
    }
}
