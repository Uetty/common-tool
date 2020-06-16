package com.uetty.common.tool.core;

import java.util.UUID;

@SuppressWarnings("unused")
public class UuidUtil {

	private static final char[] _UU64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
			.toCharArray();

	public static String getUuid(){
		UUID uu = UUID.randomUUID();

		long L = uu.getMostSignificantBits();
		long R = uu.getLeastSignificantBits();

		char[] cs = new char[22];

		int hex;

		// 从L64位取10次，每次取6位, 0-9位
		long cur = L;
		for (int i = 9, off = 4; i >= 0; --i) {
			cur = cur >>> off;
			hex = ((int) cur & 63);
			cs[i] = _UU64[hex];
			off = 6;
		}

		// 从R64位取10次，每次取6位, 11-20位
		cur = R;
		for (int i = 20, off = 2; i >= 11; --i) {
			cur = cur >>> off;
			hex = ((int) cur & 63);
			cs[i] = _UU64[hex];
			off = 6;
		}

		// 从L64位取最后的4位 ＋ R64位头2位拼上, 10位
		hex = (((int) L & 15) << 2) | (((int) cur >>> 6) & 3);
		cs[10] = _UU64[hex];

		hex = ((int) R & 3) << 4;
		// 剩下的两位最后取, 21位
		cs[21] = _UU64[hex];

		// 返回字符串
		return new String(cs);
	}
	
}
