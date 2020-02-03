package com.uetty.common.tool.core.proxy.demo;

import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class CglibProxyCallback implements MethodInterceptor, CallbackFilter {

	// Callback可以限定这几个接口继承，不能去直接继承Callback
//	NoOp
//	MethodInterceptor
//	InvocationHandler
//	LazyLoader
//	Dispatcher
//	FixedValue
//	ProxyRefDispatcher
	
	private static int index = 0;
	int id;
	
	public CglibProxyCallback() {
		id = CglibProxyCallback.index++;
	}
	
	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		System.out.println("第" + id + "个callback");
		System.out.println("----类名----"+method.getDeclaringClass().getName());
		System.out.println("----方法----"+method.getDeclaringClass().getName()+"."+method.getName());
		Object object = proxy.invokeSuper(obj, args);
		System.out.println("----返回----" + object + " of " + method.getGenericReturnType());
		return object;
	}

	
	/**
	 * 多个callback时可以根据方法确定调用哪个callback
	 * @return 返回调用哪个callback
	 */
	@Override
	public int accept(Method method) {
		int acceptIndex = (int)(Math.random() * CglibProxyCallback.index);
		System.out.println("method ==> " + method.getDeclaringClass() + "." + method.getName());
		System.out.println("accept index ==> " + acceptIndex);
		return acceptIndex;
	}

}
