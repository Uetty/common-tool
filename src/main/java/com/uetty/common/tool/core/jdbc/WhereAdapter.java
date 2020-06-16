package com.uetty.common.tool.core.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * SQL WHERE条件 适配器
 * @author vince
 */
public interface WhereAdapter {

	String getWhereCase();
	
	int setStatements(int index, PreparedStatement pstmt) throws SQLException;
	
	Map<String, String> getJoinMap();
}
