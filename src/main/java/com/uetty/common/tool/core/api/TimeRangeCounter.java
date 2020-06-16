package com.uetty.common.tool.core.api;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressWarnings("unused")
public class TimeRangeCounter {

    volatile Queue<Long> timeQueue = new ConcurrentLinkedQueue<>();

    private final Object sync = new Object();

    private static final long DEFAULT_SHORT_TERM_TIME_MILLIS = 60_000L;
    private static final int DEFAULT_MAX_CACHE = 5000;

    private long shortTermTimeMillis = DEFAULT_SHORT_TERM_TIME_MILLIS;
    private int maxCache = DEFAULT_MAX_CACHE;

    public void addCount() {
        timeQueue.offer(System.currentTimeMillis());
        if (timeQueue.size() > maxCache) {
            synchronized (sync) {
                if (timeQueue.size() > maxCache) {
                    timeQueue.poll();
                }
            }
        }
    }

    public int countShortTerm() {
        long currentTimeMillis = System.currentTimeMillis();
        int size = timeQueue.size();
        while (true) {
            synchronized (sync) {
                Long peek = timeQueue.peek();
                if (peek == null) break;
                if (peek < currentTimeMillis + shortTermTimeMillis) {
                    timeQueue.poll();
                    size = timeQueue.size();
                } else {
                    break;
                }
            }
        }
        return size;
    }


    public long getShortTermTimeMillis() {
        return shortTermTimeMillis;
    }

    public void setShortTermTimeMillis(long shortTermTimeMillis) {
        this.shortTermTimeMillis = shortTermTimeMillis;
    }

    public int getMaxCache() {
        return maxCache;
    }

    public void setMaxCache(int maxCache) {
        this.maxCache = maxCache;
    }
}
