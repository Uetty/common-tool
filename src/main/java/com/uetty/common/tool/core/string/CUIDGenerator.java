package com.uetty.common.tool.core.string;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Chronological Ordered Universally Unique Identifier
 * 时间顺序通用唯一标识符
 * @author vince
 */
public class CUIDGenerator {

    /**
     * 开头时间戳编码表（为了保持有序性，该表即使替换字符集也需保持Ascii有序性）
     */
    final static char[] TIMESTAMP_DIGITS = {
            '0' , '1' , '2' , '3' , '4' , '5' ,
            '6' , '7' , '8' , '9' , 'a' , 'b' ,
            'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
            'j' , 'k' , 'm' , 'n' , 'p' , 'r' ,
            's' , 't' , 'u' , 'v' , 'w' , 'x' ,
            'y' , 'z'
    };

    /**
     * JVM标识-状态相关字符集
     * <p>该表根据需要随意替换相同数量字符集</p>
     */
    final static char[] JVM_STAT_DIGITS = {
            'a' , 'b' , 'c' , 'd' , 'e' , 'f' ,
            'g' , 'h' , 'j' , 'k' , 'm' , 'n' ,
            'p' , 'r' , 's' , 't' , 'u' , 'v' ,
            'w' , 'x' , 'y' , 'z' , '0' , '1' ,
            '2' , '3' , '4' , '5' , '6' , '7' ,
            '8' , '9'
    };

    /**
     * JVM标识-IP相关字符集
     * <p>如需IP隐秘性，可根据需要随意替换相同数量字符集</p>
     */
    final static char[] JVM_IP_DIGITS = {
            'a' , 'b' , 'c' , 'd' , 'e' , 'f' ,
            '0' , '1' , '2' , '3' , '4' , '5' ,
            '6' , '7' , '8' , '9' , 'g' , 'h' ,
            'j' , 'k' , 'm' , 'n' , 'p' , 'r' ,
            's' , 't' , 'u' , 'v' , 'w' , 'x' ,
            'y' , 'z'
    };

    /**
     * IP位替换
     * <p>如需IP隐秘性，可根据需要随意替换相同数量字符集</p>
     * <p>由于私网IP网段有限，容易通过确定的网段结合统计攻击猜测IP，可设置替换特定IP位增加猜测难度</p>
     */
    private final static Byte[] REWRITE_IP_SEGMENT = { (byte) 112, null, null, null};

    public static String generate() {
        return getTime(TIMESTAMP_DIGITS) +
                JVM_ID +
                getSerial();
    }

    private static String getTime(char[] digits) {
        long currentTimeMillis = System.currentTimeMillis();
        // 10位32进制，足以表示以毫秒计的3万年时间（当前系统时间肯定大于1970年，因此直接省略符号位处理）
        return format32(digits, currentTimeMillis, 10);
    }

    private static String format32(char[] digit, long val, int len) {
        char[] chars = new char[len];
        for (int i = chars.length - 1; i >= 0; i--) {
            // 获取字节的低5位有效值
            int j = (int) (val & 0x1f);
            chars[i] = digit[j];
            val = val >> 5;
        }
        return new String(chars);
    }

    public static final String JVM_ID = initJvm();

    @SuppressWarnings({"ConstantConditions", "RedundantSuppression"})
    private static String initJvm() {
        long ipAddr;
        try {
            byte[] address = InetAddress.getLocalHost().getAddress();
            for (int i = 0; i < address.length; i++) {
                if (REWRITE_IP_SEGMENT.length > i && REWRITE_IP_SEGMENT[i] != null) {
                    address[i] = REWRITE_IP_SEGMENT[i];
                }
            }
            ipAddr = toLong(address);
        } catch (Exception e) {
            ipAddr = 0;
        }
        return format32(JVM_STAT_DIGITS, ipAddr, 7) + getTime(JVM_IP_DIGITS);
    }

    private static final AtomicInteger SEQ = new AtomicInteger((int) (Math.random() * Integer.MAX_VALUE / 1000));

    private static String getSerial() {
        long serial = SEQ.incrementAndGet() & 0x1ffffff;
        // 单实例每毫秒最大允许产生 33554431 个ID，
        // MAC i7 4核下3线程测试每毫秒产生ID数在1.9万左右，故该容量导致ID重复的概率几乎为0
        return format32(TIMESTAMP_DIGITS, serial, 5);
    }

    private static long toLong(byte[] bytes) {
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result = (result << 8) + (0xff & bytes[i]);
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(generate());
    }
}