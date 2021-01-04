package com.github.coerx.qarchiver.core.encrypt;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

public class EncryptAndDecryptForAES128 implements IEncryptAndDecrypt {
    /**
     * salt and iv 是16字节
     */
    private final static int SALT_LENGTH = 16;
    private final static int IV_LEGNTH = 16;
    private final static int ITERATION_COUNT = 40000;
    /**
     * key的长度是128bit
     */
    private final static int KEY_LENGTH = 128;

    private Random random = new Random();
    private char[] password;

    public EncryptAndDecryptForAES128(char[] password) {
        this.password = password;
    }

    @Override
    public void encrypt(InputStream inputSteam, OutputStream outputStream) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            SecretKeySpec secretKey = createSecretKey(password, salt);
            Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            pbeCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            //iv长度是16
            byte[] iv = pbeCipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
            outputStream.write(salt);
            outputStream.write(iv);
            outputStream.flush();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, pbeCipher);

            byte[] buffer = new byte[1024 * 128];
            int len;
            while ((len = inputSteam.read(buffer)) >= 0) {
                cipherOutputStream.write(buffer, 0, len);
            }
            cipherOutputStream.flush();
            cipherOutputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void decrypt(InputStream inputStream, OutputStream outputStream) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            byte[] iv = new byte[IV_LEGNTH];
            for (int i = 0; i < salt.length; i++) {
                int byt = inputStream.read();
                if (byt == -1) throw new RuntimeException("读取输入字节流错误,缺失必要字节");
                salt[i] = (byte) byt;
            }
            for (int i = 0; i < iv.length; i++) {
                int byt = inputStream.read();
                if (byt == -1) throw new RuntimeException("读取输入字节流错误,缺失必要字节");
                iv[i] = (byte) byt;
            }

            SecretKeySpec secretKey = createSecretKey(password, salt);
            Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            pbeCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            CipherInputStream cipherInputStream = new CipherInputStream(inputStream, pbeCipher);

            byte[] buffer = new byte[1024 * 128];
            int len;
            while ((len = cipherInputStream.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SecretKeySpec createSecretKey(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey keyTmp = keyFactory.generateSecret(keySpec);
        return new SecretKeySpec(keyTmp.getEncoded(), "AES");
    }
}
