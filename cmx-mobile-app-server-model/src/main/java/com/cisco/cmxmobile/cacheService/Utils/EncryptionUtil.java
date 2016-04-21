package com.cisco.cmxmobile.cacheService.Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EncryptionUtil {
    
    private static final EncryptionUtil INSTANCE = new EncryptionUtil();

    private SecureRandom secureRandom;

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionUtil.class);

    private EncryptionUtil() {
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
        }
        catch (NoSuchAlgorithmException e) {
            LOGGER.error("No such algorithm", e);
        }
    }

    public SecureRandom getSecureRandom() {
        return secureRandom;
    }

    public static EncryptionUtil getInstance() {
        return INSTANCE;
    }

    /**
     * Generates an MD5 hash of a list of Strings, where the strings are
     * separated by colons Ex: generateMD5("one", "two") will hash "one:two"
     */
    public static String generateMD5(String... args) throws NoSuchAlgorithmException {
        MessageDigest md;
        try {
            byte[] colonBytes = ":".getBytes();
            md = MessageDigest.getInstance("MD5");
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                md.update(arg.getBytes());
                if (i != args.length - 1) {
                    md.update(colonBytes);
                }
            }
            byte byteData[] = md.digest();

            // convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw e;
        }
    }

    public static String decrypt(String encryptedtext, String secretKey) throws Exception {
        MessageDigest shahash = MessageDigest.getInstance("SHA-1");
        byte[] key = shahash.digest(secretKey.getBytes("UTF-8"));
        key = Arrays.copyOf(key, 16);
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] hexData = hexStringToByteArray(encryptedtext);
        byte[] data = cipher.doFinal(hexData);
        return new String(data);
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
