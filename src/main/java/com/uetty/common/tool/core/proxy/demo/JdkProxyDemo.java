package com.uetty.common.tool.core.proxy.demo;

import com.uetty.common.tool.core.proxy.JdkProxy;
 
public class JdkProxyDemo {
	public static void main(String[] args) {
		JdkProxyServiceImpl ro = new JdkProxyServiceImpl();
		JdkProxyReadable proxy0 = new JdkProxy<JdkProxyReadable>(ro).createDynamicProxy();
		proxy0.read();
		System.out.println("代理对象：" + proxy0.getClass().getName());
		System.out.println("===========");
		JdkProxyDemoWritable proxy1 = new JdkProxy<JdkProxyDemoWritable>(ro).createDynamicProxy();
		proxy1.write();
		System.out.println("代理对象：" + proxy1.getClass().getName());
	}
}