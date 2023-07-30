package com.xmcx.audio.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Code util
 */
public class CodecUtil {

    /**
     * AES/ECB decrypt
     */
    public static byte[] aesEcbDecrypt(byte[] src, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return cipher.doFinal(src);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Base64 decrypt
     */
    public static byte[] base64Decrypt(byte[] src) {
        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(src);
    }

    /**
     * AES/ECB encrypt
     */
    public static byte[] aesEcbEncrypt(byte[] src, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return cipher.doFinal(src);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Base64 encrypt
     */
    public static byte[] base64Encrypt(byte[] src) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encode(src);
    }

}
