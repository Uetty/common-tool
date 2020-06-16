package com.uetty.common.tool.core.api.github;

import com.uetty.common.tool.core.api.AbstractApiTokenManager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class GithubTokenManager extends AbstractApiTokenManager<GithubToken> {

    /**
     * 请求结果返回后需要间隔的时间(上次归还时间和本次借出时间的间隔)
     */
    private static final long INTERVAL_AFTER_GIVEBACK = 1_000L;
    /**
     * 两次请求之间至少需要间隔的时间（上次借出时间和本次借出时间的间隔）
     */
    private static final long INTERVAL_BETWEEN_RENT = 5_000L;

    public enum RentType {
        SEARCH_CODE,
        SEARCH_CONETNT,
    }

    public static class RateLimit implements Serializable {
        Integer rateLimitRemain = 1;
        Long rateLimitReset = 0L;
        Integer retryAfter = 0;
        private Integer getRateLimitRemain() {
            return rateLimitRemain;
        }
        private void setRateLimitRemain(Integer rateLimitRemain) {
            this.rateLimitRemain = rateLimitRemain;
        }
        private Long getRateLimitReset() {
            return rateLimitReset;
        }
        private void setRateLimitReset(Long rateLimitReset) {
            this.rateLimitReset = rateLimitReset;
        }
        private Integer getRetryAfter() {
            return retryAfter;
        }
        private void setRetryAfter(Integer retryAfter) {
            this.retryAfter = retryAfter;
        }
    }

    /**
     * 覆盖TokenNode类，添加两个RateLimit参数目的，使其冷却时间可以根据多种请求类型分别计算
     */
    public class GithubTokenNode extends TokenNode implements Serializable {
        volatile Map<RentType, RateLimit> rateLimitMap = new HashMap<>();
        volatile Map<RentType, Long> lastRentTimestampMap = new HashMap<>();
        volatile Map<RentType, Long> nextColdDownTimestampMap = new HashMap<>();
        private GithubTokenNode(GithubToken token) {
            super(token);
            RentType[] rentTypes = RentType.values();
            for (RentType rentType : rentTypes) {
                setRateLimit(rentType, new RateLimit());
            }
        }
        public void setNextColdDownTimestamp(long nextColdDownTimestamp) {
            RentType rentType = ThreadLocalHolder.rentType.get();
            nextColdDownTimestampMap.put(rentType, nextColdDownTimestamp);
        }
        public long getNextColdDownTimestamp() {
            RentType rentType = ThreadLocalHolder.rentType.get();
            Long aLong = nextColdDownTimestampMap.get(rentType);
            return aLong != null ? aLong : 0L;
        }
        public void setLastRentTimestamp(long lastRentTimestamp) {
            RentType rentType = ThreadLocalHolder.rentType.get();
            lastRentTimestampMap.put(rentType, lastRentTimestamp);
        }
        public long getLastRentTimestamp() {
            RentType rentType = ThreadLocalHolder.rentType.get();
            Long aLong = lastRentTimestampMap.get(rentType);
            return aLong != null ? aLong : 0L;
        }
        private RateLimit getRateLimit(RentType rentType) {
            return rateLimitMap.get(rentType);
        }
        private void setRateLimit(RentType rentType, RateLimit rateLimit) {
            rateLimitMap.put(rentType, rateLimit);
        }
    }

    /**
     * 覆盖TokenRenter类，其归还时增加两个几个参数
     */
    public class GithubTokenRenter extends TokenRenter {
        private GithubTokenRenter(TokenNode tokenNode) {
            super(tokenNode);
        }
        @Override
        @Deprecated
        public void giveBackToken() {
            throw new UnsupportedOperationException("please use giveBackToken(Integer, Long, Integer)");
        }
        @SuppressWarnings("unused")
        public void giveBackToken(Integer limitRemain, Long limitReset, Integer retryAfter) {
            RateLimit rateLimit = new RateLimit();
            rateLimit.setRateLimitRemain(limitRemain);
            rateLimit.setRateLimitReset(limitReset);
            rateLimit.setRetryAfter(retryAfter);
            RentType rentType = ThreadLocalHolder.rentType.get();
            GithubTokenNode tokenNode = (GithubTokenNode) this.tokenNode;
            tokenNode.setRateLimit(rentType, rateLimit);
            super.giveBackToken();
        }
    }

    private static class ThreadLocalHolder {
        private static final ThreadLocal<RentType> rentType = new ThreadLocal<>();
    }

    @Override
    protected TokenNode newTokenNode(GithubToken token) {
        return new GithubTokenNode(token);
    }

    @Override
    protected TokenRenter newTokenRenter(TokenNode node) {
        return new GithubTokenRenter(node);
    }

    @Override
    protected void resetNextColdDownTimestamp(TokenNode tokenNode) {
        GithubTokenNode githubTokenNode = (GithubTokenNode) tokenNode;
        RentType rentType = ThreadLocalHolder.rentType.get();
        RateLimit rateLimit = githubTokenNode.getRateLimit(rentType); // 根据请求类型，获取请求数量限制信息

        long currentTimeMillis = System.currentTimeMillis();
        Integer retryAfter = rateLimit.getRetryAfter();
        if (retryAfter != null && retryAfter > 0) { // 上次请求被github标记为请求滥用
            tokenNode.setNextColdDownTimestamp(currentTimeMillis + retryAfter);
            return;
        }

        long lastRentTimestamp = tokenNode.getLastRentTimestamp();
        long resetTimestamp = rateLimit.getRateLimitReset() != null ? rateLimit.getRateLimitReset() : 0L;

        long nextColdDownTimestamp = Math.max(lastRentTimestamp + INTERVAL_BETWEEN_RENT,
                currentTimeMillis + INTERVAL_AFTER_GIVEBACK);
        int currentRateLimitRemain = rateLimit.getRateLimitRemain() != null ? rateLimit.getRateLimitRemain() : 1; // 重置时间之前的剩余次数
        if (currentRateLimitRemain <= 0 || (resetTimestamp > 0 && resetTimestamp < nextColdDownTimestamp)) {
            nextColdDownTimestamp = resetTimestamp;
        }
        tokenNode.setNextColdDownTimestamp(nextColdDownTimestamp);
    }

    @Override
    protected abstract List<GithubToken> getNewestTokenList();

    @Override
    protected boolean tokenEquals(GithubToken token1, GithubToken token2) {
        // token equals的比较逻辑
        if (token1 == token2) return true;
        if (token1 == null || token2 == null) return false;
        return Objects.equals(token1.getToken(), token2.getToken());
    }

    @Override
    @Deprecated
    public TokenRenter tryGetAvailableToken(long delay) {
        throw new UnsupportedOperationException("please use tryGetAvailableToken(long, RentType)");
    }

    public GithubTokenRenter tryGetAvailableToken(long delay, RentType rentType) {
        ThreadLocalHolder.rentType.set(rentType);
        return (GithubTokenRenter) super.tryGetAvailableToken(delay);
    }

}
