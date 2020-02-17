package com.uetty.common.tool.core.net.dns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 基于近期响应统计的dns解析处理器的动态选择
 */
@SuppressWarnings("unused")
public class DynamicRoutingResolver implements Resolver {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicRoutingResolver.class);
    private final Object LOCKER = new Object();

    private volatile List<IndexedResolver> resolvers = new ArrayList<>();
    private volatile int[] weights;
    private volatile int offset = 0;
    private volatile long offsetUpdateTime = 0;
    private static final long UNIT_WEIGHT_TIME = 1500L; // 单位权重时间
    private static final int WEIGHT_SERVER_AVERAGE = 10; // 平均每个服务器的权重
    private static final int WEIGHT_BASE = 1; // 基础权重

    @SuppressWarnings("WeakerAccess")
    public DynamicRoutingResolver(List<String> servers) throws UnknownHostException {

        initResolvers(servers);
    }

    private void initResolvers(List<String> servers) throws UnknownHostException {
        Objects.requireNonNull(servers);
        servers = servers.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase())
                .distinct()
                .collect(Collectors.toList());
        if (servers.size() == 0) throw new RuntimeException("server size is 0");
        LOG.debug("initResolvers ===> {}", servers);

        this.resolvers.clear();
        weights = new int[servers.size()];
        for (int i = 0; i < servers.size(); i++) {
            Resolver resolver = new SimpleResolver(servers.get(i));
            resolver.setTimeout(5);
            IndexedResolver indexedResolver = new IndexedResolver();
            indexedResolver.setInitIndex(i);
            indexedResolver.setDnsServer(servers.get(i));
            indexedResolver.setResolver(resolver);
            indexedResolver.getStatisticWeight().addAndGet(WEIGHT_SERVER_AVERAGE);
            this.resolvers.add(indexedResolver);

            weights[i] = WEIGHT_SERVER_AVERAGE;
        }
        this.offsetUpdateTime = System.currentTimeMillis();
        this.offset = 0;
    }

    private void ajudgeWeightIfNeed() {
        long currentTimeMillis = System.currentTimeMillis();
        long timeline = this.offsetUpdateTime + this.weights[this.offset] * UNIT_WEIGHT_TIME;
        if (currentTimeMillis <= timeline) {
            return;
        }
        synchronized (LOCKER) {
            timeline = this.offsetUpdateTime + this.weights[this.offset] * UNIT_WEIGHT_TIME;
            if (currentTimeMillis <= timeline) {
                return;
            }
            this.offset = (this.offset + 1) % this.resolvers.size();
            if (this.offset == 0) { // 需要重新分配权重了
                LOG.debug("ajudge resolver dns server weight");
                ajudgeWeight();
                LOG.debug("resolver dns server lookup count statistics -> \n {}", this.resolvers);
            }
            this.offsetUpdateTime = System.currentTimeMillis();
        }
    }
    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    private void ajudgeWeight() {
        int scoreSum = 0;
        for (int i = 0; i < this.resolvers.size(); i++) {
            IndexedResolver indexedResolver = this.resolvers.get(i);
            int weight = this.weights[i];
            int score = indexedResolver.getPeriodCount().get() / weight;
            indexedResolver.getPeriodCount().set(score);
            scoreSum += score;
        }
        // sort by period count
        this.resolvers = this.resolvers.stream()
                .sorted((ir1, ir2) -> ir2.getPeriodCount().get() - ir1.getPeriodCount().get())
                .collect(Collectors.toList());

        int dynamicWeights = this.resolvers.size() * (WEIGHT_SERVER_AVERAGE - WEIGHT_BASE); // 浮动权重
        for (int i = 0; i < this.resolvers.size(); i++) {
            IndexedResolver indexedResolver = this.resolvers.get(i);
            int score = indexedResolver.getPeriodCount().get();
            int gain = scoreSum > 0 ? Math.round((float) dynamicWeights * score / scoreSum) : 0;
            int newWeight = WEIGHT_BASE + gain;

            this.weights[i] = newWeight;
            scoreSum -= score;
            dynamicWeights -= gain;
            indexedResolver.getPeriodCount().set(0);
            indexedResolver.statisticWeight.addAndGet(newWeight);
        }
    }

    private Resolver getResolver() {
        ajudgeWeightIfNeed();

        IndexedResolver indexedResolver = this.resolvers.get(offset);
        indexedResolver.getPeriodCount().incrementAndGet();
        indexedResolver.getStatisticCount().incrementAndGet();

        return indexedResolver.resolver;
    }

    @Override
    public void setPort(int port) {
        for (IndexedResolver resolver : this.resolvers) {
            resolver.getResolver().setPort(port);
        }
    }

    @Override
    public void setTCP(boolean b) {
        for (IndexedResolver resolver : this.resolvers) {
            resolver.getResolver().setTCP(b);
        }
    }

    @Override
    public void setIgnoreTruncation(boolean b) {
        for (IndexedResolver resolver : this.resolvers) {
            resolver.getResolver().setIgnoreTruncation(b);
        }
    }

    @Override
    public void setEDNS(int level) {
        for (IndexedResolver resolver : this.resolvers) {
            resolver.getResolver().setEDNS(level);
        }
    }

    @Override
    public void setEDNS(int level, int payloadSize, int flags, List options) {
        for (IndexedResolver resolver : this.resolvers) {
            resolver.getResolver().setEDNS(level, payloadSize, flags, options);
        }
    }

    @Override
    public void setTSIGKey(TSIG tsig) {
        for (IndexedResolver resolver : this.resolvers) {
            resolver.getResolver().setTSIGKey(tsig);
        }
    }

    @Override
    public void setTimeout(int secs, int msecs) {
        for (IndexedResolver resolver : this.resolvers) {
            resolver.getResolver().setTimeout(secs, msecs);
        }
    }

    @Override
    public void setTimeout(int secs) {
        for (IndexedResolver resolver : this.resolvers) {
            resolver.getResolver().setTimeout(secs, 0);
        }
    }

    @Override
    public Message send(Message query) throws IOException {
        Resolver resolver = getResolver();
        return resolver.send(query);
    }

    @Override
    public Object sendAsync(Message message, ResolverListener resolverListener) {
        Resolver resolver = getResolver();
        return resolver.sendAsync(message,resolverListener);
    }

    @SuppressWarnings("unused")
    class IndexedResolver {
        int initIndex;
        String dnsServer;
        volatile Resolver resolver;
        volatile AtomicInteger periodCount = new AtomicInteger(0);
        volatile AtomicLong statisticCount = new AtomicLong();
        volatile AtomicLong statisticWeight = new AtomicLong();
        int getInitIndex() {
            return initIndex;
        }
        void setInitIndex(int initIndex) {
            this.initIndex = initIndex;
        }
        String getDnsServer() {
            return dnsServer;
        }
        void setDnsServer(String dnsServer) {
            this.dnsServer = dnsServer;
        }

        Resolver getResolver() {
            return resolver;
        }
        void setResolver(Resolver resolver) {
            this.resolver = resolver;
        }
        AtomicInteger getPeriodCount() {
            return periodCount;
        }
        void setPeriodCount(AtomicInteger periodCount) {
            this.periodCount = periodCount;
        }
        AtomicLong getStatisticCount() {
            return statisticCount;
        }
        void setStatisticCount(AtomicLong statisticCount) {
            this.statisticCount = statisticCount;
        }
        AtomicLong getStatisticWeight() {
            return statisticWeight;
        }
        void setStatisticWeight(AtomicLong statisticWeight) {
            this.statisticWeight = statisticWeight;
        }
        @Override
        public String toString() {
            return "{\n" +
                    "\tserver=" + dnsServer +
                    ",\n\tstatistic={\n\t\tcount=" + statisticCount +
                    ",\n\t\tweight=" + statisticWeight +
                    ",\n\t\taverage=" + ((float)statisticCount.get() * 1000 / UNIT_WEIGHT_TIME / statisticWeight.get()) +
                    " / Sec" +
                    "}\n}";
        }
    }

    /**
     * 打印统计信息
     */
    public void printStatistics() {

        List<IndexedResolver> list = new ArrayList<>(this.resolvers);
        Integer[] indexes = new Integer[list.size()];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        float[] aves = new float[list.size()];
        for (int i = 0; i < aves.length; i++) {
            aves[i] = ((float)list.get(i).statisticCount.get() * 1000 / UNIT_WEIGHT_TIME / list.get(i).statisticWeight.get());
        }
        Arrays.sort(indexes, (idx1, idx2) -> Float.compare(aves[idx2], aves[idx1]));

        List<IndexedResolver> listSorted = new ArrayList<>();
        for (Integer index : indexes) {
            listSorted.add(list.get(index));
        }
        LOG.debug(listSorted.toString());
    }
}
