package com.uetty.common.tool.core.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 各种api的token冷却时间管理的抽象
 * <p>将token泛型化以适应不同类型的token，同时将与泛型有关的抽象到子类实现
 */
@SuppressWarnings("unused")
public abstract class AbstractApiTokenManager<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractApiTokenManager.class);

    /**
     * 设置租期的上限，防止长期未归还的情况（这种情况一般是代码失误导致，这里加一重保证）
     */
    private static final long MAX_RENT_TIMESTAMP = 120_000L;
    /**
     * 存储的TOKEN的queue，多服务器部署情景下，将queue和lock
     */
    private volatile Queue<TokenNode> tokenQueue = new ConcurrentLinkedQueue<>();
    /**
     * 已租借出去的token
     */
    private volatile Queue<TokenNode> rentedQueue = new ConcurrentLinkedQueue<>();

    protected static final ReentrantLock lock = new ReentrantLock();
    private int tokenSize;

    /**
     * 重置token的下次冷却时间
     * @param tokenNode 需要重置冷却时间的token节点
     */
    protected abstract void resetNextColdDownTimestamp(TokenNode tokenNode);
    /**
     * 获取最新的token列表，因为这里token列表都是在jvm内存存储，有可能有需要定期更新的需求
     * @return 返回最新的token列表（从数据库或其他地方）
     */
    protected abstract List<T> getNewestTokenList();
    /**
     * token的equals规则
     * @param token1 token1
     * @param token2 token2
     * @return 返回是否equals
     */
    protected abstract boolean tokenEquals(T token1, T token2);

    protected TokenNode newTokenNode(T token) {
        return new TokenNode(token);
    }

    protected TokenRenter newTokenRenter(TokenNode node) {
        return new TokenRenter(node);
    }

    private void requeueIfEquals(Queue<TokenNode> oldQueue, Queue<TokenNode> newQueue, List<T> tokenList) {
        TokenNode node;
        while ((node = oldQueue.poll()) != null) {
            for (int i = 0; i < tokenList.size(); i++) {
                T token = tokenList.get(i);
                if (tokenEquals(node.getToken(), token)) {
                    newQueue.offer(node);
                    tokenList.remove(i);
                    break;
                }
            }
        }
    }

    /*
     * 刷新TOKEN列表
     */
    public void refreshTokenList() {
        try {
            lock.lock();

            List<T> tokenList = getNewestTokenList();
            Queue<TokenNode> oldTokenQueue = tokenQueue;
            Queue<TokenNode> oldRentedQueue = rentedQueue;
            tokenQueue = new ConcurrentLinkedQueue<>();
            rentedQueue = new ConcurrentLinkedQueue<>();
            // 旧的TOKEN继续回流到TOKEN队列中
            requeueIfEquals(oldTokenQueue, tokenQueue, tokenList);
            requeueIfEquals(oldRentedQueue, rentedQueue, tokenList);

            // 新的TOKEN
            for (T token : tokenList) {
                TokenNode tokenNode = newTokenNode(token);
                tokenQueue.offer(tokenNode);
            }
            tokenSize = tokenQueue.size() + rentedQueue.size();

            if (getTokenSize() == 0) {
                LOG.warn("Token size is 0....");
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * queue中的下一个token是否可租借，如果可用则返回，不可用则返回空
     * @return 返回包含租借成功的token的包装类实例
     */
    private TokenRenter rentNextTokenIfAvailable() {
        try {
            lock.lock();
            TokenNode node = tokenQueue.poll();
            if (node == null) return null;

            // 判断是否可租借
            boolean available = false;
            // 判断是否可租借
            if (node.getLastRentTimestamp() + MAX_RENT_TIMESTAMP < System.currentTimeMillis() // 判断租期超时，防止token长时间未归还的情况（出于代码健壮性考虑）
                    || (node.status == TokenNode.STATUS_IDLE
                    && node.getNextColdDownTimestamp() < System.currentTimeMillis())) {
                available = true;
            }
            if (available) {
                node.status = TokenNode.STATUS_RENTED; // 标记token状态为租借出去
                node.setLastRentTimestamp(System.currentTimeMillis()); // 更新租借时间
                node.rentSign = UUID.randomUUID().toString(); //
                rentedQueue.offer(node);
            } else {
                tokenQueue.offer(node); // 回归到队列中
            }

            return available ? newTokenRenter(node) : null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 多次循环rentNextTokenIfAvailable，把队列中的所有token都遍历一遍
     * @return 返回包含租借成功的token的包装类实例
     */
    private TokenRenter loopGetToken() {
        int size = tokenQueue.size();
        for (int  i = 0; i < size; i++) {
            TokenRenter renter = rentNextTokenIfAvailable();
            if (renter != null) {
                return renter;
            }
        }
        return null;
    }

    /**
     * 尝试获取可用的Token，如果在限制时间内不能获取token，则返回null
     * @param delay 等待时间，超出等待时间，则获取失败
     * @return 返回包含租借成功的token的包装类实例
     */
    public TokenRenter tryGetAvailableToken(long delay) {
        long time = System.currentTimeMillis();
        do {
            TokenRenter tokenRenter = loopGetToken();
            if (tokenRenter != null) return tokenRenter;

            LockSupport.parkNanos(40_000_000L);
        } while (time + delay < System.currentTimeMillis());
        return null;
    }

    @SuppressWarnings("unused")
    public int getTokenSize() {
        return this.tokenSize;
    }


    /**
     * 内部队列使用的token节点
     */
    public class TokenNode implements Serializable {
        private static final int STATUS_IDLE = 0;
        private static final int STATUS_RENTED = 1;
        volatile long lastRentTimestamp = 0L; // 上次租借的时间戳
        volatile long nextColdDownTimestamp = 0L; // 下次冷却的时间戳（冷却时间跟归还时间有关系）
        protected volatile T token;
        protected volatile int status = STATUS_IDLE;
        volatile String rentSign = ""; // 出借签名

        protected TokenNode(T token) {
            this.token = token;
        }
        public T getToken() {
            return this.token;
        }
        public void setNextColdDownTimestamp(long nextColdDownTimestamp) {
            this.nextColdDownTimestamp = nextColdDownTimestamp;
        }
        public long getNextColdDownTimestamp() {
            return nextColdDownTimestamp;
        }
        public void setLastRentTimestamp(long lastRentTimestamp) {
            this.lastRentTimestamp = lastRentTimestamp;
        }
        public long getLastRentTimestamp() {
            return lastRentTimestamp;
        }
    }

    /**
     * token租借到外部时使用的对象
     */
    public class TokenRenter {
        protected TokenNode tokenNode;
        private final String rentSign; // 借出签名
        protected TokenRenter(TokenNode tokenNode) {
            this.tokenNode = tokenNode;
            this.rentSign = tokenNode.rentSign;
        }
        public T getToken() {
            return tokenNode.getToken();
        }
        /**
         * 归还租借的token
         */
        public void giveBackToken() {
            try {
                lock.lock();
                if (!this.rentSign.equals(tokenNode.rentSign)) {
                    // 已经因为超时而失效
                    return;
                }
                // 重置冷却时间
                resetNextColdDownTimestamp(tokenNode);
                rentedQueue.removeIf((node) -> {
                    // 如果相等从借出记录的队列移到未借出队列
                    return node != null && tokenEquals(node.token, this.tokenNode.token);
                });
                tokenQueue.offer(tokenNode);
                tokenNode.status = TokenNode.STATUS_IDLE;
            } finally {
                lock.unlock();
            }
        }
    }

}
