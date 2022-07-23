package com.uetty.common.tool.core.security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("unused")
public class AESUtils {

    public static final String AES_MODE_ECB = "AES/ECB/PKCS5Padding";
    public static final String AES_MODE_CBC = "AES/CBC/PKCS5Padding";
    private static final String DEF_AES_MODE = AES_MODE_ECB;
    private static final String KEY_SPEC_ALGORITHM = "AES";
    private static final int OPMODE_ENCRYPTION = 1;
    private static final int OPMODE_DECRYPTION = 2;
    // keysize : 16(128 / 8), 24(192 / 8), 32(256 / 8)

    public static byte[] decrypt(byte[] key, byte[] data, int keySize) {
        return aesDone(DEF_AES_MODE, key, data, keySize, OPMODE_DECRYPTION);
    }

    public static byte[] encrypt(byte[] key, byte[] data, int keySize) {
        return aesDone(DEF_AES_MODE, key, data, keySize, OPMODE_ENCRYPTION);
    }

    public static byte[] decrypt(String aesMode, byte[] key, byte[] data, int keySize) {
        return aesDone(aesMode, key, data, keySize, OPMODE_DECRYPTION);
    }

    public static byte[] encrypt(String aesMode, byte[] key, byte[] data, int keySize) {
        return aesDone(aesMode, key, data, keySize, OPMODE_ENCRYPTION);
    }

    private static byte[] aesDone(String aesMode, byte[] key, byte[] data, int keySize, int opmode) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(trimKeySize(key, keySize), KEY_SPEC_ALGORITHM);
            Cipher instance = Cipher.getInstance(aesMode);
            instance.init(opmode, secretKeySpec);
            return instance.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] trimKeySize(byte[] key, int i) {
        byte[] bArr2 = new byte[i];
        if (key.length <= i) {
            i = key.length;
        }
        System.arraycopy(key, 0, bArr2, 0, i);
        return bArr2;
    }
}
