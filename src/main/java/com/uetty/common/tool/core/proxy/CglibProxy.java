package com.uetty.common.tool.core.proxy;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;

//import net.sf.cglib.proxy.Dispatcher;
//import net.sf.cglib.proxy.FixedValue;
//import net.sf.cglib.proxy.InvocationHandler;
//import net.sf.cglib.proxy.LazyLoader;
//import net.sf.cglib.proxy.MethodInterceptor;
//import net.sf.cglib.proxy.MethodProxy;
//import net.sf.cglib.proxy.NoOp;
//import net.sf.cglib.proxy.ProxyRefDispatcher;

@SuppressWarnings("unused")
public class CglibProxy {
	
	// Callback可以限定这几个接口继承，不能去直接继承Callback
//	NoOp
//	MethodInterceptor
//	InvocationHandler
//	LazyLoader
//	Dispatcher
//	FixedValue
//	ProxyRefDispatcher
	
	public static <T> T getProxyObject(Class<T> cz, Callback callback){
		return getProxyObject(cz, new Callback[]{ callback }, null);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getProxyObject(Class<T> cz, Callback[] callbacks, CallbackFilter cbFilter) {
		Enhancer hancer = new Enhancer();
		hancer.setSuperclass(cz);
		hancer.setCallbacks(callbacks);
		hancer.setCallbackFilter(cbFilter);
		hancer.setClassLoader(cz.getClassLoader());
		return (T) hancer.create();
	}
	
}
