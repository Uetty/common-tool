package com.uetty.common.tool.core.cache.mo;

import lombok.Data;

@Data
public abstract class Lock implements AutoCloseable {

    private String key;
    private String token;

    /**
     * 释放锁
     */
    public abstract void release();

    @Override
    public void close() {
        release();
    }
}