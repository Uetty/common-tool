package com.uetty.common.tool.core.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

/**
 * 简单查询sql处理器抽象
 * @author vince
 * @param <T> 返回值类型
 */
@SuppressWarnings("unused")
public abstract class SimpleSelectJdbcProcessor<T> {

	private static final Logger logger = LoggerFactory.getLogger(SimpleSelectJdbcProcessor.class);
	
	boolean developMode = false;
	GroupAdapter groupAdapter; // group条件适配器
	LimitAdapter limitAdapter; // limit条件适配器
	SelectAdapter<T> selectAdapter; // select条件适配器
	WhereAdapter whereAdapter; // where条件适配器
	OrderAdapter orderAdapter; // order条件适配器
	
	public boolean isDevelopMode() {
		return developMode;
	}

	public void setDevelopMode(boolean developMode) {
		this.developMode = developMode;
	}

	public GroupAdapter getGroupAdapter() {
		return groupAdapter;
	}

	public void setGroupAdapter(GroupAdapter groupAdapter) {
		this.groupAdapter = groupAdapter;
	}

	public LimitAdapter getLimitAdapter() {
		return limitAdapter;
	}

	public void setLimitAdapter(LimitAdapter limitAdapter) {
		this.limitAdapter = limitAdapter;
	}

	public SelectAdapter<T> getSelectAdapter() {
		return selectAdapter;
	}

	public void setSelectAdapter(SelectAdapter<T> selectAdapter) {
		this.selectAdapter = selectAdapter;
	}

	public WhereAdapter getWhereAdapter() {
		return whereAdapter;
	}

	public void setWhereAdapter(WhereAdapter whereAdapter) {
		this.whereAdapter = whereAdapter;
	}

	public OrderAdapter getOrderAdapter() {
		return orderAdapter;
	}

	public void setOrderAdapter(OrderAdapter orderAdapter) {
		this.orderAdapter = orderAdapter;
	}
	
	public Map<String, String> getAllJoinMap() {
		LinkedHashMap<String, String> joinMap = new LinkedHashMap<>(selectAdapter.getJoinMap());
		if (whereAdapter != null) {
			joinMap.putAll(whereAdapter.getJoinMap());
		}
		if (groupAdapter != null) {
			joinMap.putAll(groupAdapter.getJoinMap());
		}
		if (orderAdapter != null) {
			joinMap.putAll(orderAdapter.getJoinMap());
		}
		return joinMap;
	}
	
	public Map<String, String> getSearchCountJoinMap() {
		LinkedHashMap<String, String> joinMap = new LinkedHashMap<>(selectAdapter.getJoinMap());
		if (whereAdapter != null) {
			joinMap.putAll(whereAdapter.getJoinMap());
		}
		if (groupAdapter != null) {
			joinMap.putAll(groupAdapter.getJoinMap());
		}
		return joinMap;
	}
	
	@SuppressWarnings("UnusedAssignment")
	public List<T> searchList(Connection conn) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<T> list = new ArrayList<>();
		try {
			assert(selectAdapter != null);
			
			StringBuilder sb = new StringBuilder();
			// select
			String selectSql = selectAdapter.getSelectSql();
			sb.append(selectSql);
			// join
			Map<String, String> joinMap = getAllJoinMap();
			for (Entry<String, String> next : joinMap.entrySet()) {
				sb.append(" ").append(next.getValue());
			}
			// where
			if (whereAdapter != null) {
				String whereCase = whereAdapter.getWhereCase();
				sb.append(" ").append(whereCase);
			}
			// group
			if (groupAdapter != null) {
				String groupBy = groupAdapter.getGroupBy();
				sb.append(" ").append(groupBy);
			}
			// order
			if (orderAdapter != null) {
				String order = orderAdapter.getOrder();
				sb.append(" ").append(order);
			}
			// limit
			if (limitAdapter != null) {
				String limit = limitAdapter.getLimit();
				sb.append(" ").append(limit);
			}
			
			if (this.developMode) {
				logger.debug(sb.toString());
			}
			
			pstmt = conn.prepareStatement(sb.toString());
			if (whereAdapter != null) {
				int index = 1;
				index = whereAdapter.setStatements(index, pstmt);
			}
			rs = pstmt.executeQuery();
			while (rs.next()) {
				list.add(selectAdapter.readData(rs));
			}
		} catch (Throwable e) {
			logger.error("Error occured when search List", e);
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return list;
	}
	
	@SuppressWarnings("UnusedAssignment")
	public long searchCount(Connection conn) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		long count = 0;
		try {
			assert(selectAdapter != null);
			
			StringBuilder sb = new StringBuilder();
			// select
			String selectSql = selectAdapter.getSelectSql();
			sb.append(selectSql);
			// join
			Map<String, String> joinMap = getSearchCountJoinMap();
			for (Entry<String, String> next : joinMap.entrySet()) {
				sb.append(" ").append(next.getValue());
			}
			// where
			if (whereAdapter != null) {
				String whereCase = whereAdapter.getWhereCase();
				sb.append(" ").append(whereCase);
			}
			// group
			if (groupAdapter != null) {
				String groupBy = groupAdapter.getGroupBy();
				sb.append(" ").append(groupBy);
			}
			
			pstmt = conn.prepareStatement(sb.toString());
			if (whereAdapter != null) {
				int index = 1;
				index = whereAdapter.setStatements(index, pstmt);
			}
			rs = pstmt.executeQuery();
			if (rs.next()) {
				count = (Long) selectAdapter.readData(rs);
			}
		} catch (Throwable e) {
			logger.error("Error occured when search Count", e);
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return count;
	}
	
}
