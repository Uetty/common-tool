package com.uetty.common.tool.core.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * SQL SELECT条件 适配器
 * @author vince
 */
public interface SelectAdapter<T> {

	String getSelectSql();
	
	T readData(ResultSet rs) throws SQLException;
	
	Map<String, String> getJoinMap();
}
