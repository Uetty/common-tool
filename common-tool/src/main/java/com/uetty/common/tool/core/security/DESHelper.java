package com.uetty.common.tool.core.security;

import javax.crypto.KeyGenerator;
import java.io.IOException;
import java.security.Key;
import java.util.Base64;

public class DESHelper {

	private final static String key = "UryUswpQ98=";

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String str = "e111113";
		String encode = encrypt(str, null);
		System.out.println(encode);
		System.out.println(decrypt(encode, null));
	}

	public static String encrypt(String data, String charset) throws Exception {
		byte[] bytes;
		if(charset != null){
			bytes = DESUtils.encrypt(data.getBytes(charset), key.getBytes());
		}else{
			bytes = DESUtils.encrypt(data.getBytes(), key.getBytes());
		}
		return bytes2HexString(bytes);
	}

	/**
	 * 使用 默认key 解密
	 * 
	 * @return String
	 * @author lifq
	 * @date 2015-3-17 下午02:49:52
	 */
	public static String decrypt(String data, String charset) throws IOException, Exception {
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
            ret[i] = (byte) Integer  
                    .valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();  
        }  
        return ret;  
    }

	private static String bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex;
		}
		return ret;
	}
	
	public static void generateKey() throws Exception {
    	KeyGenerator kg = KeyGenerator.getInstance("DES");
    	Key key = kg.generateKey();
    	System.out.println(Base64.getEncoder().encodeToString(key.getEncoded()));
    }

}
