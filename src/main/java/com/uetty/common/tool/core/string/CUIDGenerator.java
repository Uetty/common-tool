package com.uetty.common.tool.core.string;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Chronological Ordered Universally Unique Identifier
 * 时间顺序通用唯一标识符
 * @author vince
 */
public class CUIDGenerator {

    public static String generate() {
        return getTime() +
                JVM_ID +
                getSerial();
    }

    private static String getTime() {
        long currentTimeMillis = System.currentTimeMillis();
        // 10位32进制，足以表示以毫秒计的3万年时间（当前系统时间肯定大于1970年，因此直接省略符号位处理）
        return format32(currentTimeMillis, 10);
    }

    private static String format32(long val, int len) {
        char[] chars = Long.toString(val, 32).toCharArray();
        int charsLen = chars.length;
        if (charsLen >= len) {
            return new String(chars, charsLen - len, len);
        }
        char[] temp = new char[len];
        for (int i = 0; i < len - charsLen; i++) {
            temp[i] = '0';
        }
        System.arraycopy(chars, 0, temp, len - charsLen, charsLen);
        return new String(temp);
    }

    public static final String JVM_ID = initJvm();

    private static String initJvm() {
        long ipAddr;
        try {
            ipAddr = toLong(InetAddress.getLocalHost().getAddress());
        } catch (Exception e) {
            ipAddr = 0;
        }
        return String.format("%08x", ipAddr) + getTime();
    }

    private static final AtomicInteger SEQ = new AtomicInteger((int) (Math.random() * Integer.MAX_VALUE / 1000));

    private static String getSerial() {
        long serial = SEQ.incrementAndGet() & 0xfffff;
        // 每毫秒最大允许产生 1048575 个ID
        return format32(serial, 4);
    }

    private static long toLong(byte[] bytes) {
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result = (result << 8) - Byte.MIN_VALUE + (int) bytes[i];
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(generate());
    }
}