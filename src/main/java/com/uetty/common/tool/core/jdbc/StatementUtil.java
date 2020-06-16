package com.uetty.common.tool.core.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;


/**
 * sql preparedStatement 操作工具类
 * @author vince
 */
@SuppressWarnings("unused")
public class StatementUtil {

	public static void setValue(PreparedStatement pstmt, int index, Integer v) throws SQLException {
		if (v == null) {
			setNull(pstmt, index, Types.INTEGER);
			return;
		}
		pstmt.setInt(index, v);
	}
	public static void setValue(PreparedStatement pstmt, int index, Long v) throws SQLException {
		if (v == null) {
			setNull(pstmt, index, Types.BIGINT);
			return;
		}
		pstmt.setLong(index, v);
	}
	public static void setValue(PreparedStatement pstmt, int index, Boolean v) throws SQLException {
		if (v == null) {
			setNull(pstmt, index, Types.BIT);
			return;
		}
		pstmt.setBoolean(index, v);
	}
	public static void setValue(PreparedStatement pstmt, int index, Byte v) throws SQLException {
		if (v == null) {
			setNull(pstmt, index, Types.TINYINT);
			return;
		}
		pstmt.setByte(index, v);
	}
	public static void setValue(PreparedStatement pstmt, int index, Double v) throws SQLException {
		if (v == null) {
			setNull(pstmt, index, Types.DOUBLE);
			return;
		}
		pstmt.setDouble(index, v);
	}
	public static void setValue(PreparedStatement pstmt, int index, Float v) throws SQLException {
		if (v == null) {
			setNull(pstmt, index, Types.REAL);
			return;
		}
		pstmt.setFloat(index, v);
	}
	public static void setValue(PreparedStatement pstmt, int index, Short v) throws SQLException {
		if (v == null) {
			setNull(pstmt, index, Types.SMALLINT);
			return;
		}
		pstmt.setShort(index, v);
	}
	public static void setValue(PreparedStatement pstmt, int index, Date v) throws SQLException {
		if (v == null) {
			setNull(pstmt, index, Types.TIMESTAMP);
			return;
		}
		pstmt.setTimestamp(index, new Timestamp(v.getTime()));
	}
	public static void setValue(PreparedStatement pstmt, int index, String v) throws SQLException {
		if (v == null) {
			setNull(pstmt, index, Types.VARCHAR);
			return;
		}
		pstmt.setString(index, v);
	}
	
	public static void setNull(PreparedStatement pstmt, int index, int type) throws SQLException {
		pstmt.setNull(index, type);
	}
}
