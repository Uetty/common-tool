package com.uetty.common.doc.tester;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uetty.common.tool.core.FileTool;
import com.uetty.common.tool.core.HttpMultipartUtil;
import com.uetty.common.tool.core.security.SignatureUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestFileUpload {

	static final String ROUTER_FOLDER = "";// 路由文件所在目录
	static final String REGION_FOLDER = "";// 地区配置文件所在目录
	static final String SETTOP_FOLDER = "";// 机顶盒文件所在目录
	static final String FILE_PATH = "/var/resolutionUpd/导出版本数据/target/router/converged/会议决议模板-路由器-2.0.0.24.xls";// 文件地址
	static final String URL = "http://127.0.0.1:9080/self/service/resolution/importResolution";// 服务器地址
	static final String PRODUCE_SYSTEM_SECRET = "aba9b19ffb1c05ca";// 密钥
	
	static final int PRODUCT_TYPE_ID_1 = 1;// 融合网关
	static final int PRODUCT_TYPE_ID_2 = 2;// 智能网关
	
	static final int GATEWAY_ROUTER = 2;// 路由器
	static final int GATEWAY_SETTOP = 3;// 机顶盒
	static final int GATEWAY_REGION = 4;// 地区配置文件
	
	static final long SOFTVERSION_ID = 201;// 关联发布版本申请单
	
	static final String SUCCESS_LOG_FILE = "/var/resolutionUpd/success.log";// 上传成功，记录日志
	static final String ERROR_LOG_FILE = "/var/resolutionUpd/error.log";// 上传失败，记录日志
	
	public static void main(String[] args) {
		File successFile = new File(SUCCESS_LOG_FILE);
		File errorFile = new File(ERROR_LOG_FILE);
		File file = new File(FILE_PATH);
		try {
			String successMsg = upload(file, GATEWAY_ROUTER, PRODUCT_TYPE_ID_1, SOFTVERSION_ID);
			file.delete();// 上传成功，则删除这个文件
			String msg = file.getAbsolutePath() + "===>" + successMsg;
			FileTool.writeLine(successFile, msg);// 记录一下上传成功的log
		} catch (Exception e) {
			String msg = file.getAbsolutePath() + "===>" + e.getMessage();
			try {
				FileTool.writeLine(errorFile, msg);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
	}
	
	
	private static String upload(File file, int gateway, int prodTypeId, long svid) throws JsonParseException, JsonMappingException, IOException {
		// 加签
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("prodTypeId", String.valueOf(prodTypeId));
		params.put("gateway", String.valueOf(gateway));
		params.put("svid", String.valueOf(svid));
		params = SignatureUtil.signData(params, PRODUCE_SYSTEM_SECRET);
		
		Map<String, File> files = new HashMap<String, File>();
		files.put("file", file);
		String result = HttpMultipartUtil.simpleMultipartPost(URL, files, params);
		ObjectMapper om = new ObjectMapper();
		@SuppressWarnings("unchecked")
		Map<String, Object> readValue = om.readValue(result, Map.class);
		
		if (!"0".equals(String.valueOf(readValue.get("code")))) {
			throw new RuntimeException(String.valueOf(readValue.get("message")));
		}
		
		@SuppressWarnings("unchecked")
		Map<String, String> importMsg = (Map<String, String>) readValue.get("content");
		
		return "success[id = " + String.valueOf(importMsg.get("id")) + ", " + String.valueOf(importMsg.get("title")) + "]";
	}
}
