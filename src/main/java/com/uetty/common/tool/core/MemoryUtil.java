package com.uetty.common.tool.core;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 内存工具
 * @since 1.8
 */
@SuppressWarnings("unused")
public class MemoryUtil {


    /**
     * JVM堆内存
     */
    static class HeapMemory {
        private long initMemory;
        private long maxMemory;
        private long commitMemory;
        private long freeMemory;
        private long usedMemory;
        /**
         * JVM堆内存允许的最大内存值（-Xmx）
         * @return 单位bit
         */
        public long getMaxMemory() {
            return maxMemory;
        }
        /**
         * JVM堆已向操作系统申请的内存大小（当前JVM堆内存最大值）
         * @return 单位bit
         */
        public long getCommitMemory() {
            return commitMemory;
        }
        /**
         * 当前可用内存（JVM堆已向操作系统申请的内存大小 - JVM堆内存已使用值）
         * @return 单位bit
         */
        public long getFreeMemory() {
            return freeMemory;
        }
        /**
         * JVM堆内存已使用大小
         * @return 单位bit
         */
        public long getUsedMemory() {
            return usedMemory;
        }
        /**
         * JVM堆内存初始化大小（-Xms）
         * @return 单位bit
         */
        public long getInitMemory() {
            return initMemory;
        }
    }

    /**
     * JVM非堆内存
     */
    static class NonHeapMemory {
        private long initMemory;
        private long maxMemory;
        private long commitMemory;
        private long usedMemory;
        /**
         * JVM非堆内存允许的最大内存值（-XX:MaxMetaspaceSize）
         * @return 单位bit
         */
        public long getMaxMemory() {
            return maxMemory;
        }
        /**
         * JVM非堆内存已向操作系统申请的内存大小（当前JVM非堆内存最大值）
         * @return 单位bit
         */
        public long getCommitMemory() {
            return commitMemory;
        }
        /**
         * JVM非堆内存已使用大小
         * @return 单位bit
         */
        public long getUsedMemory() {
            return usedMemory;
        }
        /**
         * JVM非堆内存初始化大小（-XX:MetaspaceSize）
         * @return 单位bit
         */
        public long getInitMemory() {
            return initMemory;
        }
    }

    /**
     * 系统内存
     */
    static class SystemMemory {
        long totalMemory;
        long freeMemory;
        long usedMemory;
        long totalSwapMemory;
        long usedSwapMemory;
        long freeSwapMemory;
        long commitVirtualMemory;

        /**
         * 系统最大内存
         * @return 单位bit
         */
        public long getTotalMemory() {
            return totalMemory;
        }

        /**
         * 系统剩余可用
         * @return 单位bit
         */
        public long getFreeMemory() {
            return freeMemory;
        }

        /**
         * 系统已用内存
         * @return 单位bit
         */
        public long getUsedMemory() {
            return usedMemory;
        }

        /**
         * 最大交换空间大小
         * @return 单位bit
         */
        public long getTotalSwapMemory() {
            return totalSwapMemory;
        }

        /**
         * 已使用交换空间大小
         * @return 单位bit
         */
        public long getUsedSwapMemory() {
            return usedSwapMemory;
        }

        /**
         * 剩余交换空间大小
         * @return 单位bit
         */
        public long getFreeSwapMemory() {
            return freeSwapMemory;
        }

        /**
         * 已提交的虚拟内存大小
         * @return 单位bit
         */
        public long getCommitVirtualMemory() {
            return commitVirtualMemory;
        }
    }

    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    private static final String[] UNIT = new String[] {"Bit", "KB", "MB", "GB", "TB"};
    /**
     * 可视化内存
     * @return 带单位的内存显示
     */
    public static String toHumanMemory(long size) {
        int i = 0;
        long floor = 1024;
        while (i < UNIT.length - 1 && size >= floor) {
            floor *= 1024;
            i++;
        }
        return BigDecimal.valueOf(size)
                .divide(BigDecimal.valueOf(floor), 2, RoundingMode.HALF_UP)
                .toString();
    }

    public static HeapMemory getHeapMemory() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        HeapMemory heapMemory = new HeapMemory();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        heapMemory.maxMemory = heapMemoryUsage.getMax();
        heapMemory.commitMemory = heapMemoryUsage.getCommitted();
        heapMemory.initMemory = heapMemoryUsage.getInit();
        heapMemory.usedMemory = heapMemoryUsage.getUsed();
        heapMemory.freeMemory = heapMemory.commitMemory - heapMemory.usedMemory;
        return heapMemory;
    }

    public static NonHeapMemory getNonHeapMemory() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        NonHeapMemory nonHeapMemory = new NonHeapMemory();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
        nonHeapMemory.maxMemory = nonHeapMemoryUsage.getMax();
        nonHeapMemory.commitMemory = nonHeapMemoryUsage.getCommitted();
        nonHeapMemory.initMemory = nonHeapMemoryUsage.getInit();
        nonHeapMemory.usedMemory = nonHeapMemoryUsage.getUsed();
        return nonHeapMemory;
    }

    public static SystemMemory getSystemMemory() {
        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        SystemMemory systemMemory = new SystemMemory();
        systemMemory.freeMemory = operatingSystemMXBean.getFreePhysicalMemorySize();
        systemMemory.totalMemory = operatingSystemMXBean.getTotalPhysicalMemorySize();
        systemMemory.usedMemory = systemMemory.totalMemory - systemMemory.freeMemory;
        systemMemory.freeSwapMemory = operatingSystemMXBean.getFreeSwapSpaceSize();
        systemMemory.totalSwapMemory = operatingSystemMXBean.getTotalSwapSpaceSize();
        systemMemory.usedSwapMemory = systemMemory.totalSwapMemory - systemMemory.freeSwapMemory;
        systemMemory.commitVirtualMemory = operatingSystemMXBean.getCommittedVirtualMemorySize();
        return systemMemory;
    }
}
