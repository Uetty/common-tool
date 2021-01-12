package com.uetty.common.tool.core.net.dns;

import com.uetty.common.tool.core.FileUtil;
import org.xbill.DNS.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

/**
 * @author : Vince
 */
public class DnsTest {

    private static final String tomDomain = "uetty.com";
    private static DynamicRoutingResolver resolver = null;
    private static ConcurrentLinkedQueue<String> subdomainQueue = null;

    /**
     * 当前网络的默认dns服务器
     */
    private static List<String> getDefaultDnsServers() {
        String[] servers = ResolverConfig.getCurrentConfig().servers();
        Set<String> set = new HashSet<>();
        if (servers != null) {
            for (String server : servers) {
                if ("0.0.0.0".equals(server) || server.startsWith("192.")
                        || server.startsWith("10.")) {
                    continue;
                }
                if (server.startsWith("172.")) {
                    String substring = server.substring(4);
                    int i = substring.indexOf(".");
                    try {
                        if (i > 0) {
                            int l2 = Integer.parseInt(substring.substring(0, i));
                            if ((l2 & 0x10) != 0) {
                                continue;
                            }
                        }
                    } catch (Exception ignore) {}
                }
                set.add(server);
            }
        }
        set.remove("0.0.0.0");
        List<String> list = new ArrayList<>(set);
        System.out.println("default dns ---> " + list);
        return list;
    }

    private static List<String> readLines(String fileName) throws IOException {
        try (InputStream resourceAsStream = DnsTest.class.getClassLoader().getResourceAsStream(fileName)) {
            List<String> lines = FileUtil.readLines(resourceAsStream);
            for (int i = 0; i < lines.size(); i++) {
                String s = lines.get(i);
                if (s != null) {
                    s = s.trim();
                    if (!"".equals(s)) {
                        lines.set(i, s);
                        continue;
                    }
                }
                lines.remove(i--);
            }
            return lines;
        }
    }

    public static void main(String[] args) throws IOException {
        // 测试dns服务器响应速度/负载能力

        Set<String> subdomains = new HashSet<>();
        subdomains.addAll(readLines("domains/subnames.txt"));
        subdomains.addAll(readLines("domains/subnames_full.txt"));
        subdomains.addAll(readLines("domains/next_sub.txt"));
        subdomains.addAll(readLines("domains/next_sub_full.txt"));
        subdomainQueue = new ConcurrentLinkedQueue<>();
        subdomainQueue.addAll(subdomains);
        subdomains.clear();
        //noinspection UnusedAssignment
        subdomains = null;

        List<String> dnsServers = new ArrayList<>();
        dnsServers.add("182.254.116.116");  // public DNS
        dnsServers.add("180.76.76.76"); // baidu DNS
        dnsServers.add("101.226.4.6"); // DNS 派
        dnsServers.add("223.6.6.6"); // ali DNS
        dnsServers.add("218.30.118.6"); // DNS 派
        dnsServers.add("223.5.5.5"); // ali DNS
        dnsServers.add("119.29.29.29"); // public DNS
        dnsServers.add("114.114.115.115"); // 114 DNS
        dnsServers.add("114.114.114.114"); // 114 DNS
        dnsServers.add("112.124.47.27"); // One DNS
        dnsServers.addAll(getDefaultDnsServers());
        resolver = new DynamicRoutingResolver(dnsServers);


        int threadSize = 40;
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadSize);

        long startTime = System.currentTimeMillis();
        List<Future<List<String>>> futures = new ArrayList<>();
        for (int i = 0; i < threadSize; i++) {

            futures.add(executor.submit(new DomainWorkerThread(resolver)));
        }

        List<String> list = new ArrayList<>();
        while (futures.size() > 0) {
            Future<List<String>> future = futures.get(0);
            if (future.isCancelled()) {
                System.out.println("cancel");
                futures.remove(0);
            } else if (future.isDone()) {
                try {
                    List<String> list1 = future.get();
                    list.addAll(list1);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                futures.remove(0);
            } else {
                LockSupport.parkNanos(200_000_000);
            }
        }

        System.out.println("passTimemillis -> " + (System.currentTimeMillis() - startTime));
        resolver.printStatistics();

        executor.shutdown();
        System.out.println(list);
    }

//    public static class PrintStatisticsThread extends Thread {
//        volatile boolean alive = true;
//
//        PrintStatisticsThread() {
//        }
//
//        @Override
//        public void run() {
//            int round = 0;
//            while (alive) {
//                if (round == 0) {
////                    printCount();
//                }
//                // 睡眠0.5秒
//                LockSupport.parkNanos(this, 500_000_000L);
//                round = ++round % 20;
//            }
//            System.out.println("finished test");
//            printCount();
//        }
//
//        private void printCount() {
//            System.out.println("---------------------------------------");
//            resolver.printStatistics();
//        }
//
//        void shutdown() {
//            this.alive = false;
//        }
//    }

    public static class DomainWorkerThread implements Callable<List<String>> {

        NoCacheLookup lookup;
        DomainWorkerThread(DynamicRoutingResolver resolver) {
            this.lookup = new NoCacheLookup(resolver);
        }

        @Override
        public List<String> call() {
            List<String> result = new ArrayList<>();
            String subdomain;
            while ((subdomain = subdomainQueue.poll()) != null) {
                NoCacheLookup.Answer answer = lookup.run(subdomain + "." + tomDomain);

                if (answer.getResult() == Lookup.SUCCESSFUL) {
                    Record[] records = answer.getRecords();
                    List<String> strings = parseAnswers(records);
                    result.addAll(strings);
                }
            }
            return result;
        }

    }

    private static List<String> parseAnswers(Record[] answers) {
        List<String> list = new ArrayList<>();
        for (Record answer : answers) {
            String address = null;
            switch (answer.getType()) {
                case Type.A:
                    ARecord aRecord = (ARecord) answer;
                    if (aRecord.getAddress() != null) {
                        address = aRecord.getName().toString(true);
                    }
                    break;
                case Type.AAAA:
                    AAAARecord aaaaRecord = (AAAARecord) answer;
                    if (aaaaRecord.getAddress() != null) {
                        address = aaaaRecord.getName().toString(true);
                    }
                    break;
                case Type.CNAME:
                    CNAMERecord cnameRecord = (CNAMERecord) answer;
                    if (cnameRecord.getAlias() != null) {
                        address = cnameRecord.getName().toString(true);
                    }
                    break;
                default:
            }
            if (address != null) {
                list.add(address);
            }
        }
        return list;
    }
}
