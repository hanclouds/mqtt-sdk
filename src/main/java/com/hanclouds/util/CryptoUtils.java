package com.hanclouds.util;


import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.concurrent.ThreadLocalRandom;



/**
 * 签名和AES加解密算法
 *
 * @author szl
 * @date 2017/9/7
 */
public class CryptoUtils {

    private final static int AES_KEY_SIZE = 16;
    private final static String AES_ECB = "AES/ECB/PKCS5Padding";
    private final static String AES_CBC = "AES/CBC/PKCS5Padding";
    private final static String HMAC_SHA1 = "HmacSHA1";
    private final static String CHARSET_UTF8 = "utf-8";
    private final static String CHARACTER_SET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final static String CHARACTER_NUMBER_SET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static Logger logger = LoggerFactory.getLogger(CryptoUtils.class);

    public static String getRandomString(int length) {
        if (length <= 0) {
            return null;
        } else {
            StringBuilder builder = new StringBuilder(length + 1);
            int rand = ThreadLocalRandom.current().nextInt(CHARACTER_SET.length());
            builder.append(CHARACTER_SET.charAt(rand));

            for (int i = 1; i < length; ++i) {
                rand = ThreadLocalRandom.current().nextInt(CHARACTER_NUMBER_SET.length());
                builder.append(CHARACTER_NUMBER_SET.charAt(rand));
            }
            return builder.toString();
        }
    }

    /**
     * 使用HMAC-SHA1对内容进行签名
     *
     * @param secret  秘钥
     * @param content 待签名的内容
     * @return 签名的结果
     */
    public static String signWithHmacsha1(String secret, String content) {
        try {
            byte[] keyBytes = secret.getBytes(CHARSET_UTF8);
            SecretKey secretKey = new SecretKeySpec(keyBytes, HMAC_SHA1);
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(content.getBytes(CHARSET_UTF8));
            return new String(Base64.encodeBase64(rawHmac));
        } catch (Exception e) {
            logger.error("signWithHmacsha1({}, {}) failed. {}", secret, content, e.getMessage());
        }
        return null;
    }


    public static byte[] encodeWithAesCbc(String secret, byte[] content) {
        if (secret.length() < AES_KEY_SIZE) {
            return null;
        }
        if (secret.length() > AES_KEY_SIZE) {
            secret = secret.substring(0, AES_KEY_SIZE);
        }
        try {
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(CHARSET_UTF8), "AES");
            Cipher cipher = Cipher.getInstance(AES_CBC);
            byte[] ivBytes = getRandomString(16).getBytes(CHARSET_UTF8);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encrypData = cipher.doFinal(content);
            if (encrypData != null) {
                byte[] result = new byte[16 + encrypData.length];
                System.arraycopy(ivBytes, 0, result, 0, 16);
                System.arraycopy(encrypData, 0, result, 16, encrypData.length);
                return result;
            }
            return null;
        } catch (Exception e) {
            logger.error("encodeWithAesCbc(...)  failed, method = AES/CBC/PKCS5Padding, cause={}", e.getMessage());
        }
        return null;
    }

    public static byte[] decodeWithAesCbc(String secret, byte[] content) {
        if (secret.length() < AES_KEY_SIZE) {
            return null;
        }
        if (secret.length() > AES_KEY_SIZE) {
            secret = secret.substring(0, AES_KEY_SIZE);
        }
        try {
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(CHARSET_UTF8), "AES");
            Cipher cipher = Cipher.getInstance(AES_CBC);
            byte[] ivBytes = new byte[16];
            System.arraycopy(content, 0, ivBytes, 0, 16);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] encBytes = new byte[content.length - 16];
            System.arraycopy(content, 16, encBytes, 0, encBytes.length);
            return cipher.doFinal(encBytes);
        } catch (Exception e) {
            logger.error("decodeWithAesCbc(...) failed, {}", e.getMessage());
        }
        return null;
    }

    public static String decodeWithAesCbc(String secret, String content) {
        if (secret == null) {
            return null;
        }
        if (secret.length() < AES_KEY_SIZE) {
            return null;
        }
        if (secret.length() > AES_KEY_SIZE) {
            secret = secret.substring(0, AES_KEY_SIZE);
        }
        try {
            byte[] data = Base64.decodeBase64(content.getBytes());
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(CHARSET_UTF8), "AES");
            Cipher cipher = Cipher.getInstance(AES_CBC);
            byte[] ivBytes = new byte[16];
            System.arraycopy(data, 0, ivBytes, 0, 16);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] encBytes = new byte[data.length - 16];
            System.arraycopy(data, 16, encBytes, 0, encBytes.length);
            byte[] decBytes = cipher.doFinal(encBytes);
            return new String(decBytes);
        } catch (Exception e) {
            logger.error("decodeWithAesCbc(...) failed, {}", e.getMessage());
        }
        return null;
    }
}
