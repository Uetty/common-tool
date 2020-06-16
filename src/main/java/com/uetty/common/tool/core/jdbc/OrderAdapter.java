package com.uetty.common.tool.core.jdbc;

import java.util.Map;

/**
 * SQL ORDER条件 适配器
 * @author vince
 */
public interface OrderAdapter {

	String getOrder();
	
	Map<String, String> getJoinMap();
}
