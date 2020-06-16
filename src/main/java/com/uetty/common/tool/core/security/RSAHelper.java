package com.uetty.common.tool.core.security;

@SuppressWarnings("unused")
public class RSAHelper {

	public static String decryptByPrivateKey(String data, String charset, String privateKey)
			throws Exception {
		byte[] decode = RSAUtils.decryptByPrivateKey(hexString2Bytes(data), privateKey);
		if(charset != null){
			return new String(decode, charset);
		}else{
			return new String(decode);
		}
	}

	public static String decryptByPublicKey(String data, String charset, String publicKey) throws Exception{
		byte[] decode = RSAUtils.decryptByPublicKey(hexString2Bytes(data), publicKey);
		if(charset != null){
			return new String(decode, charset);
		}else{
			return new String(decode);
		}
	}

	public static String encryptByPrivateKey(String data, String charset, String privateKey) throws Exception{
		byte[] encode;
		if(charset != null){
			encode = RSAUtils.encryptByPrivateKey(data.getBytes(charset), privateKey);
		}else{
			encode = RSAUtils.encryptByPrivateKey(data.getBytes(), privateKey);
		}
		return bytes2HexString(encode);
	}

	public static String encryptByPublicKey(String data, String charset, String publicKey) throws Exception{
		byte[] encode;
		if(charset != null){
			encode = RSAUtils.encryptByPublicKey(data.getBytes(charset), publicKey);
		}else{
			encode = RSAUtils.encryptByPublicKey(data.getBytes(), publicKey);
		}
		return bytes2HexString(encode);
	}

	public static byte[] hexString2Bytes(String src) {
		int l = src.length() / 2;
		byte[] ret = new byte[l];
		for (int i = 0; i < l; i++) {
			ret[i] = Integer.valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
		}
		return ret;
	}

	public static String bytes2HexString(byte[] b) {
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
}
