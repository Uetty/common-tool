package com.uetty.common.tool.core.net;

import org.apache.commons.net.whois.WhoisClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * whois信息查询
 */
@SuppressWarnings("unused")
public class WhoisResolver {

    private static final int DEFAULT_PORT = 43;
    public static class Address {
        String host;
        Integer port;
        Address(String host, Integer port) {
            this.host = host;
            this.port = port;
        }
    }
    static Map<String, Address> addresses = new HashMap<>();
    private static void putAddress(String name, String host, Integer port) {
        addresses.put(name, new Address(host,port));
    }
    private static void putAddress(String name, String host) {
        addresses.put(name, new Address(host,DEFAULT_PORT));
    }
    private static void initAddresses() {
        putAddress(".com","whois.verisign-grs.com");
        putAddress(".ac", "whois.nic.ac");
        putAddress(".ae", "whois.aeda.net.ae");
        putAddress(".aero", "whois.aero");
        putAddress(".af", "whois.nic.af");
        putAddress(".ag", "whois.nic.ag");
        putAddress(".ai", "whois.ai");
        putAddress(".am", "whois.amnic.net");
        putAddress(".arpa", "whois.iana.org");
        putAddress(".as", "whois.nic.as");
        putAddress(".asia", "whois.nic.asia");
        putAddress(".at", "whois.nic.at");
        putAddress(".au", "whois.audns.net.au");
        putAddress(".be", "whois.dns.be");
        putAddress(".bg", "whois.register.bg");
        putAddress(".bi", "whois1.nic.bi");
        putAddress(".biz", "whois.biz");
        putAddress(".bj", "whois.nic.bj");
        putAddress(".bo", "whois.nic.bo");
        putAddress(".br", "whois.registro.br");
        putAddress(".by", "whois.cctld.by");
        putAddress(".ca", "whois.cira.ca");
        putAddress(".cat", "whois.cat");
        putAddress(".cc", "ccwhois.verisign-grs.com");
        putAddress(".ch", "whois.nic.ch");
        putAddress(".ci", "whois.nic.ci");
        putAddress(".cl", "whois.nic.cl");
        putAddress(".cn", "whois.cnnic.cn");
        putAddress(".co", "whois.nic.co");
        putAddress(".coop", "whois.nic.coop");
        putAddress(".cx", "whois.nic.cx");
        putAddress(".cz", "whois.nic.cz");
        putAddress(".de", "whois.denic.de");
        putAddress(".dk", "whois.dk-hostmaster.dk");
        putAddress(".dm", "whois.nic.dm");
        putAddress(".dz", "whois.nic.dz");
        putAddress(".ec", "whois.nic.ec");
        putAddress(".edu", "whois.educause.edu");
        putAddress(".ee", "whois.tld.ee");
        putAddress(".es", "whois.nic.es");
        putAddress(".eu", "whois.eu");
        putAddress(".fi", "whois.fi");
        putAddress(".fo", "whois.nic.fo");
        putAddress(".fr", "whois.nic.fr");
        putAddress(".gd", "whois.adamsnames.com");
        putAddress(".gg", "whois.gg");
        putAddress(".gi", "whois2.afilias-grs.net");
        putAddress(".gl", "whois.nic.gl");
        putAddress(".gov", "whois.dotgov.gov");
        putAddress(".gs", "whois.nic.gs");
        putAddress(".gy", "whois.registry.gy");
        putAddress(".hk", "whois.hkirc.hk");
        putAddress(".hn", "whois2.afilias-grs.net");
        putAddress(".hr", "whois.dns.hr");
        putAddress(".ht", "whois.nic.ht");
        putAddress(".hu", "whois.nic.hu");
        putAddress(".ie", "whois.domainregistry.ie");
        putAddress(".il", "whois.isoc.org.il");
        putAddress(".im", "whois.nic.im");
        putAddress(".in", "whois.inregistry.net");
        putAddress(".info", "whois.afilias.net");
        putAddress(".int", "whois.iana.org");
        putAddress(".io", "whois.nic.io");
        putAddress(".iq", "whois.cmc.iq");
        putAddress(".ir", "whois.nic.ir");
        putAddress(".is", "whois.isnic.is");
        putAddress(".it", "whois.nic.it");
        putAddress(".je", "whois.nic.je");
        putAddress(".jobs", "jobswhois.verisign-grs.com");
        putAddress(".jp", "whois.jprs.jp");
        putAddress(".ke", "whois.kenic.or.ke");
        putAddress(".kg", "whois.domain.kg");
        putAddress(".ki", "whois.nic.ki");
        putAddress(".kr", "whois.kr");
        putAddress(".kz", "whois.nic.kz");
        putAddress(".la", "whois.nic.la");
        putAddress(".li", "whois.nic.li");
        putAddress(".lt", "whois.domreg.lt");
        putAddress(".lu", "whois.dns.lu");
        putAddress(".lv", "whois.nic.lv");
        putAddress(".ly", "whois.nic.ly");
        putAddress(".ma", "whois.iam.net.ma");
        putAddress(".md", "whois.nic.md");
        putAddress(".me", "whois.nic.me");
        putAddress(".mg", "whois.nic.mg");
        putAddress(".mn", "whois.nic.mn");
        putAddress(".mo", "whois.monic.mo");
        putAddress(".mobi", "whois.dotmobiregistry.net");
        putAddress(".mp", "whois.nic.mp");
        putAddress(".ms", "whois.nic.ms");
        putAddress(".mu", "whois.nic.mu");
        putAddress(".museum", "whois.museum");
        putAddress(".mx", "whois.mx");
        putAddress(".my", "whois.domainregistry.my");
        putAddress(".na", "whois.na-nic.com.na");
        putAddress(".name", "whois.nic.name");
        putAddress(".nc", "whois.nc");
        putAddress(".net", "whois.verisign-grs.com");
        putAddress(".nf", "whois.nic.net.nf");
        putAddress(".ng", "whois.nic.net.ng");
        putAddress(".nl", "whois.domain-registry.nl");
        putAddress(".no", "whois.norid.no");
        putAddress(".nu", "whois.nic.nu");
        putAddress(".nz", "whois.srs.net.nz");
        putAddress(".om", "whois.registry.om");
        putAddress(".org", "whois.pir.org");
        putAddress(".pe", "ero.yachay.pe");
        putAddress(".pl", "whois.dns.pl");
        putAddress(".pm", "whois.nic.pm");
        putAddress(".post", "whois.dotpostregistry.net");
        putAddress(".pr", "whois.nic.pr");
        putAddress(".pro", "whois.dotproregistry.net");
        putAddress(".pt", "whois.dns.pt");
        putAddress(".qa", "whois.registry.qa");
        putAddress(".re", "whois.nic.re");
        putAddress(".ro", "whois.rotld.ro");
        putAddress(".rs", "whois.rnids.rs");
        putAddress(".ru", "whois.tcinet.ru");
        putAddress(".sa", "whois.nic.net.sa");
        putAddress(".sb", "whois.nic.net.sb");
        putAddress(".sc", "whois2.afilias-grs.net");
        putAddress(".se", "whois.iis.se");
        putAddress(".sg", "whois.sgnic.sg");
        putAddress(".sh", "whois.nic.sh");
        putAddress(".si", "whois.arnes.si");
        putAddress(".sk", "whois.sk-nic.sk");
        putAddress(".sm", "whois.nic.sm");
        putAddress(".sn", "whois.nic.sn");
        putAddress(".so", "whois.nic.so");
        putAddress(".st", "whois.nic.st");
        putAddress(".su", "whois.tcinet.ru");
        putAddress(".sx", "whois.sx");
        putAddress(".tc", "whois.adamsnames.tc");
        putAddress(".tel", "whois.nic.tel");
        putAddress(".tf", "whois.nic.tf");
        putAddress(".th", "whois.thnic.co.th");
        putAddress(".tk", "whois.dot.tk");
        putAddress(".tl", "whois.nic.tl");
        putAddress(".tm", "whois.nic.tm");
        putAddress(".tn", "whois.ati.tn");
        putAddress(".to", "whois.tonic.to");
        putAddress(".tr", "whois.nic.tr");
        putAddress(".travel", "whois.nic.travel");
        putAddress(".tv", "tvwhois.verisign-grs.com");
        putAddress(".tw", "whois.twnic.net.tw");
        putAddress(".tz", "whois.tznic.or.tz");
        putAddress(".ua", "whois.ua");
        putAddress(".ug", "whois.co.ug");
        putAddress(".uk", "whois.nic.uk");
        putAddress(".us", "whois.nic.us");
        putAddress(".uy", "whois.nic.org.uy");
        putAddress(".uz", "whois.cctld.uz");
        putAddress(".vc", "whois2.afilias-grs.net");
        putAddress(".ve", "whois.nic.ve");
        putAddress(".vg", "whois.adamsnames.tc");
        putAddress(".wf", "whois.nic.wf");
        putAddress(".ws", "whois.website.ws");
        putAddress(".한국", "whois.kr");
        putAddress(".中国", "cwhois.cnnic.cn");
        putAddress(".中國", "cwhois.cnnic.cn");
        putAddress(".香港", "whois.hkirc.hk");
        putAddress(".台湾", "whois.twnic.net.tw");
        putAddress(".台灣", "whois.twnic.net.tw");
        putAddress(".新加坡", "whois.sgnic.sg");
        putAddress(".xxx", "whois.nic.xxx");
        putAddress(".xyz", "whois.nic.xyz");
        putAddress(".yt", "whois.nic.yt");
    }

    static {
        initAddresses();
    }

    public static Map<String, Address> getWhoisAddresses() {
        return new HashMap<>(addresses);
    }

    public static void resetWhoisAddresses(Map<String, Address> map) {
        addresses = Objects.requireNonNull(map);
    }

    public static String query(String domain) throws IOException {
        String[] split = domain.split("\\.");
        if (split.length < 2) throw new RuntimeException("invalid domain");
        String sld = split[split.length - 2];
        String tld = split[split.length - 1];
        if (sld.length() == 0 || tld.length() == 0) throw new RuntimeException("invalid domain");

        Address address = addresses.get("." + tld);
        if (address == null) throw new RuntimeException("whois server not found");
        WhoisClient client = new WhoisClient();
        client.connect(address.host, address.port);
        String query = client.query(sld + "." + tld);
        client.disconnect();
        return query;
    }
}
