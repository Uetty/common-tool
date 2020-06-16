package com.uetty.common.tool.core.jdbc;

import java.util.Map;

/**
 * SQL GROUP条件 适配器
 * @author vince
 */
public interface GroupAdapter {

	String getGroupBy();
	
	Map<String, String> getJoinMap();
}
