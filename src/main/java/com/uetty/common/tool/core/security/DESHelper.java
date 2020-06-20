package com.uetty.common.tool.core.security;

import javax.crypto.KeyGenerator;
import java.security.Key;
import java.util.Base64;

public class DESHelper {

	public static void main(String[] args) throws Exception {
		System.out.println(generateKey());

		String str = "e111113";
		String key = "UryUswpQ98=";
		String encode = encrypt(str, null, key);
		System.out.println(encode);
		System.out.println(decrypt(encode, null, key));
	}

	public static String encrypt(String data, String charset, String key) throws Exception {
		byte[] bytes;
		if(charset != null){
			bytes = DESUtils.encrypt(data.getBytes(charset), key.getBytes());
		}else{
			bytes = DESUtils.encrypt(data.getBytes(), key.getBytes());
		}
		return bytes2HexString(bytes);
	}

	public static String decrypt(String data, String charset, String key) throws Exception {
		byte[] bytes = hexString2Bytes(data);
		byte[] bt = DESUtils.decrypt(bytes, key.getBytes());
		if(charset != null){
			return new String(bt, charset);
		}else{
			return new String(bt);
		}
	}
	
	private static byte[] hexString2Bytes(String src) {  
        int l = src.length() / 2;  
        byte[] ret = new byte[l];  
        for (int i = 0; i < l; i++) {  
            ret[i] = Integer
                    .valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();  
        }  
        return ret;  
    }

	private static String bytes2HexString(byte[] b) {
		StringBuilder ret = new StringBuilder();
		for (byte value : b) {
			String hex = Integer.toHexString(value & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret.append(hex);
		}
		return ret.toString();
	}
	
	public static String generateKey() throws Exception {
    	KeyGenerator kg = KeyGenerator.getInstance("DES");
    	Key key = kg.generateKey();
    	return Base64.getEncoder().encodeToString(key.getEncoded());
    }

}
