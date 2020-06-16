package com.uetty.common.tool.core.proxy.demo;


public class JdkProxyServiceImpl implements JdkProxyReadable,JdkProxyDemoWritable {
	 
	@Override
	public void read() {
		System.out.println("read()...");
	}
 
	@Override
	public void write() {
		System.out.println("write()...");
	}
	
	@SuppressWarnings("unused")
	public void hahaha() {
		System.out.println("hahaha()...");
	}
 
}