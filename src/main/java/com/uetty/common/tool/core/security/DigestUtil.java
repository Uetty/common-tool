package com.uetty.common.tool.core.security;

import com.uetty.common.tool.core.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 摘要计算工具
 */
@SuppressWarnings("unused")
public class DigestUtil {

    public static String md5Sum(String str) throws NoSuchAlgorithmException {
        return calculateDigest(str, "MD5");
    }

    public static String md5Sum(File file) throws NoSuchAlgorithmException, IOException {
        return calculateDigest(file, "MD5");
    }

    public static String md5Sum(byte[] bytes) throws NoSuchAlgorithmException, IOException {
        return calculateDigest(bytes, "MD5");
    }

    public static String sha1Sum(File file) throws IOException, NoSuchAlgorithmException {
        return calculateDigest(file, "SHA-1");
    }

    public static String sha1Sum(String str) throws NoSuchAlgorithmException {
        return calculateDigest(str, "SHA-1");
    }

    public static String sha1Sum(byte[] bytes) throws NoSuchAlgorithmException {
        return calculateDigest(bytes, "SHA-1");
    }

    public static String sha224Sum(File file) throws IOException, NoSuchAlgorithmException {
        return calculateDigest(file, "SHA-224");
    }

    public static String sha224Sum(String str) throws NoSuchAlgorithmException {
        return calculateDigest(str, "SHA-224");
    }

    public static String sha224Sum(byte[] bytes) throws NoSuchAlgorithmException {
        return calculateDigest(bytes, "SHA-224");
    }

    public static String sha256Sum(File file) throws IOException, NoSuchAlgorithmException {
        return calculateDigest(file, "SHA-256");
    }

    public static String sha256Sum(String str) throws NoSuchAlgorithmException {
        return calculateDigest(str, "SHA-256");
    }

    public static String sha256Sum(byte[] bytes) throws NoSuchAlgorithmException {
        return calculateDigest(bytes, "SHA-256");
    }

    public static String sha384Sum(File file) throws IOException, NoSuchAlgorithmException {
        return calculateDigest(file, "SHA-384");
    }

    public static String sha384Sum(String str) throws NoSuchAlgorithmException {
        return calculateDigest(str, "SHA-384");
    }

    public static String sha384Sum(byte[] bytes) throws NoSuchAlgorithmException {
        return calculateDigest(bytes, "SHA-384");
    }

    public static String sha512Sum(File file) throws IOException, NoSuchAlgorithmException {
        return calculateDigest(file, "SHA-512");
    }

    public static String sha512Sum(String str) throws NoSuchAlgorithmException {
        return calculateDigest(str, "SHA-512");
    }

    public static String sha512Sum(byte[] bytes) throws NoSuchAlgorithmException {
        return calculateDigest(bytes, "SHA-512");
    }

    private static String calculateDigest(File file, String algorithm) throws NoSuchAlgorithmException, IOException {
        // 获取算法对象
        MessageDigest instance = MessageDigest
                .getInstance(algorithm);
        FileUtil.readByteByByte(file, 1024, instance::update);
        byte[] digest = instance.digest();
        return toHexString(digest);
    }

    private static String calculateDigest(String str, String algorithm) throws NoSuchAlgorithmException {
        // 获取算法对象
        MessageDigest instance = MessageDigest
                .getInstance(algorithm);

        byte[] digest = instance.digest(str.getBytes());// 对字符串加密，返回字符数组

        return toHexString(digest);
    }

    private static String calculateDigest(byte[] bytes, String algorithm) throws NoSuchAlgorithmException {
        // 获取算法对象
        MessageDigest instance = MessageDigest
                .getInstance(algorithm);

        byte[] digest = instance.digest(bytes);// 对字符串加密，返回字符数组

        return toHexString(digest);
    }

    private static String toHexString(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            int j = b & 0xff;// 获取字节的低八位有效值
            String hexString = Integer.toHexString(j);// 十进制转16进制
            if (hexString.length() < 2) {// 每个字节两位字符
                hexString = "0" + hexString;
            }
            sb.append(hexString);
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        InputStream resourceAsStream = DigestUtil.class.getResourceAsStream("DigestUtil.class");
        byte[] bytes = FileUtil.readToByte(resourceAsStream);
        System.out.println(md5Sum(bytes));
        System.out.println(sha1Sum(bytes));
        System.out.println(sha256Sum(bytes));
        System.out.println(sha512Sum(bytes));
        System.out.println(sha384Sum(bytes));
        System.out.println(sha224Sum(bytes));
    }
}
