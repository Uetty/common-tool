package com.uetty.common.tool.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JdkProxy<T> implements InvocationHandler {

	private final T target;
	 
	public JdkProxy(T target) {
		this.target = target;
	}
 
	@SuppressWarnings("unchecked")
	public T createDynamicProxy() {
		return (T) Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
	}
 
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println("before "+method.getName()+"()...");
		Object result = method.invoke(this.target, args);
		System.out.println("after "+method.getName()+"()...");
		return result;
	}
}
