package com.uetty.common.tool.core.security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("unused")
public class AESModeEcb {

    private static final String AES_MODE = "AES/ECB/PKCS5Padding";
    private static final String KEY_SPEC_ALGORITHM = "AES";
    private static final int OPMODE_ENCRYPTION = 1;
    private static final int OPMODE_DECRYPTION = 2;
    // keysize : 16(128 / 8), 24(192 / 8), 32(256 / 8)

    public static byte[] decrypt(byte[] key, byte[] data, int keySize) {
        return aesDone(key, data, keySize, OPMODE_DECRYPTION);
    }

    public static byte[] encrypt(byte[] key, byte[] data, int keySize) {
        return aesDone(key, data, keySize, OPMODE_ENCRYPTION);
    }

    private static byte[] aesDone(byte[] key, byte[] data, int keySize, int opmode) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(trimKeySize(key, keySize), KEY_SPEC_ALGORITHM);
            Cipher instance = Cipher.getInstance(AES_MODE);
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
