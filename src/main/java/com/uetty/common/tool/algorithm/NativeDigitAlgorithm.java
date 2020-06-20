package com.uetty.common.tool.algorithm;

/**
 * 位运算实现加、减、乘、除、相反数运算
 */
@SuppressWarnings("unused")
public class NativeDigitAlgorithm {

	/**
	 * 相反数运算
	 * @param n 数值
	 * @return 返回相反数
	 */
	public static int contrary(int n) {
//		n = n ^ -1;
		return add(~n, 1);
	}
	
	/**
	 * 加法运算
	 * @param n1 数值1
	 * @param n2 数值2
	 * @return 返回它们的和
	 */
	public static int add(int n1, int n2) {
		while(n2 != 0) {
			int a = n1 ^ n2;
			int b = (n1 & n2) << 1;
			n1 = a;
			n2 = b;
		}
		return n1;
	}
	
	/**
	 * 乘法运算
	 * @param n1 数值1
	 * @param n2 数值2
	 * @return  返回它们的积
	 */
	public static int multiple(int n1, int n2) {
		boolean positive = n1 > 0 && n2 > 0 || n1 < 0 && n2 < 0;
		n1 = n1 > 0 ? n1 : contrary(n1);
		n2 = n2 > 0 ? n2 : contrary(n2);
		if (add(n1, contrary(n2)) < 0) {
			n1 = n1 ^ n2;
			n2 = n1 ^ n2;
			n1 = n1 ^ n2;
		}
		
		int m = 0;
		while(n2 > 0) {
			int iv = n2 & 0x1;
			if (iv == 0x1) {
				m = add(m, n1);
			}
			n1 = n1 << 1;
			n2 = n2 >> 1;
		}
		return positive ? m : contrary(m);
	}
	
	/**
	 * 二进制数位数计算
	 * @param n 数值
	 * @return 返回二进制（复数按正数算）表示有多少位
	 */
	public static int unitCount(int n) {
		if (n == 0) return 1;
		n = n > 0 ? n : contrary(n); // 取正数
		int c = 0;
		while(n > 0) {
			n = n >> 1;
			c = add(c, 1);
		}
		return c;
	}
	
	/**
	 * 除法运算
	 * @param n1 数值1
	 * @param n2 数值2
	 * @return 返回n1 / n2的值
	 */
	public static int divide(int n1, int n2) {
		assert(n2 != 0);
		boolean positive = n1 >= 0 && n2 >= 0 || n1 < 0 && n2 < 0;
		n1 = n1 > 0 ? n1 : contrary(n1);
		n2 = n2 > 0 ? n2 : contrary(n2);
		
		int d = 0; // 商
		int rest; // 余数
		int len = unitCount(n1); // 二进制位数
		int i = add(len, contrary(1)); // i = len - 1;
		while(true) {
			int left = n1 >> i; // 被除数左边数值
			rest = add(left, contrary(n2)); // 减掉除数（二进制数，相差一位的情况下不会大于2倍，所以相除余数是否大于0可直接用相减差是否大于等于0判断）
			d = d << 1; // 商值进阶一位 
			if (rest >= 0) { // 比除数大
				d = add(d, 1); // d = d << 1 + 1;
				// 更新被除数的值（因为每次只会是一倍，所以直接减）
   				n1 = add(n1, contrary(n2 << i)); // n1 = n1 - (n2 << );
			}
			if (i == 0) break;
			i = add(i, contrary(1)); // i--
		}
		return positive ? d : contrary(d);
	}
}
