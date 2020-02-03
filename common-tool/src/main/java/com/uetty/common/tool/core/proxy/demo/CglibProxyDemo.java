package com.uetty.common.tool.core.proxy.demo;

import com.uetty.common.tool.core.proxy.CglibProxy;
import net.sf.cglib.proxy.Callback;

public class CglibProxyDemo {

	public static void main(String[] args) {
		
		CglibProxyCallback cb1 = new CglibProxyCallback();
		CglibProxyCallback cb2 = new CglibProxyCallback();
		CglibProxyCallback cb3 = new CglibProxyCallback();
		
		Be b = CglibProxy.getProxyObject(Be.class, new Callback[] {cb1, cb2, cb3}, cb1);
		b.getB();
		b.setB(4);
	}
	
}


class Be {
	
	int b = 0;
	
	public int getB() {
		return b;
	}
	
	public void setB(int b) { this.b = b;}
}