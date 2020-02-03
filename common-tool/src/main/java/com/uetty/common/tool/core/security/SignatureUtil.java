package com.uetty.common.tool.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


/**
 * 平台间签名，验签处理类
 * 
 * @author vince
 */
public class SignatureUtil {
	
	private static Logger logger = LoggerFactory.getLogger(SignatureUtil.class);
	/**
	 * 时间戳超时失败
	 */
	public static final int ERR_TIMEOUT = 1;
	/**
	 * 验签失败（通常是密钥错误或数据不一致导致）
	 */
	public static final int ERR_SIGN = 2;
	/**
	 * 有效时长，防止相同参数复用
	 */
	public static final int TIMEOUT_MILLISECONDS = 5 * 60 * 1000;
	/**
	 * 时间戳参数名
	 */
	public static final String PARAM_TIMESTAMP = "timestamp";
	/**
	 * 加签结果数据参数名
	 */
	public static final String PARAM_SIGNATURE = "signature";
	private static final String PARAM_SECRET = "secret";
	
	/**
	 * 数据加签
	 * @param params 原始请求参数可以是Pojo，也可以是Map
	 * @param secret 加签时使用的密钥
	 * @return 返回map，map中的值都要作为参数传递给远程服务器，map包含以下字段：
	 * <blockquote>
	 * <p>加签前的原始请求参数值
	 * <p>Long类型加签的时间戳 键名见：{@linkplain SignatureUtil#PARAM_TIMESTAMP}
	 * <p>String类型签名字符串 键名见：{@linkplain SignatureUtil#PARAM_SIGNATURE}
	 * </blockquote>
	 */
	public static Map<String, Object> signData(Object params, String secret) {
		TreeMap<String, Object> map = obj2Map(params);
		map.put(PARAM_TIMESTAMP, System.currentTimeMillis());
		// 计算签名字符串
		String signedString = getSignedString(map, secret);
		map.put(PARAM_SIGNATURE, signedString);
		
		return map;
	}
	
	/**
	 * 数据验签
	 * @param params 验签数据值 可以是Map或者Pojo，只需满足包含正常接口入参字段以外，包含以下字段：
	 * <blockquote>
	 * <p>Long类型加签的时间戳 字段名/键名为：{@linkplain SignatureUtil#PARAM_TIMESTAMP}
	 * <p>String类型签名字符串 字段名/键名为：{@linkplain SignatureUtil#PARAM_SIGNATURE}
	 * </blockquote>
	 * @param secret 验签时使用的密钥
	 * @return
	 */
	public static void assertValid(Object params, String secret) {
		TreeMap<String, Object> map = obj2Map(params);
				
		Long t = (Long) map.get(PARAM_TIMESTAMP);
		Long cur = System.currentTimeMillis();
		if ((t + TIMEOUT_MILLISECONDS) < cur) {
			throw new SignatureException("signature time timeout: msg timestamp --> " + t +", system timestamp --> " + cur, ERR_TIMEOUT);
		}
		// 签名值计算
		String signedString = getSignedString(map, secret);
		String signature = (String) map.get(PARAM_SIGNATURE);
		if (!signedString.equals(signature)) {
			throw new SignatureException("signature invalid, value --> " + obj2JsonString(map), ERR_SIGN);
		}
	}
	
	/**
	 * 签名字符串计算
	 */
	private static String getSignedString(TreeMap<String, Object> map, String secret) {
		try {
			StringBuilder sb = new StringBuilder();
			
			Iterator<String> itr = map.keySet().iterator();
			while(itr.hasNext()) {
				String k = itr.next();
				if (PARAM_SIGNATURE.equals(k)) {
					continue;
				}
				sb.append("&");
				sb.append(k);
				sb.append("=");
				sb.append(map.get(k));
			}
			sb.append("&");
			sb.append(PARAM_SECRET);
			sb.append("=");
			sb.append(secret);
			return DigestUtil.md5Sum(sb.toString().substring(1));
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static TreeMap<String, Object> obj2Map(Object obj) {
		ObjectMapper objectMapper = new ObjectMapper();
        try {
			return objectMapper.readValue(objectMapper.writeValueAsString(obj), TreeMap.class);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
        return null;
    }
	
	public static String obj2JsonString(Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public static class SignatureException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private int errorCode;
		
		public SignatureException() {
			super();
		}
		public SignatureException(String msg, int code) {
			super(msg);
			this.errorCode = code;
		}
		public int getErrorCode() {
			return errorCode;
		}
		public void setErrorCode(int errorCode) {
			this.errorCode = errorCode;
		}
	}
	
	public static void main(String[] args) {
		Map<String, Object> inParams = new HashMap<String, Object>();
		inParams.put("id", 8);
		inParams.put("name", "lalalalala");
		inParams.put("state", "normal");
		
		System.out.println("inParam ==> " + obj2JsonString(inParams));
		
		Map<String, Object> encipher = signData(inParams, "aba9b19ffb1c05ca");
		
		System.out.println("encipher ==> " + obj2JsonString(encipher));
		
		// 验签
		assertValid(encipher, "aba9b19ffb1c05ca");
		System.out.println("sign pass");
	}

}
