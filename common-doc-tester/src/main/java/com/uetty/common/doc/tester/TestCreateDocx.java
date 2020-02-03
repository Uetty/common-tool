package com.uetty.common.doc.tester;

import com.itextpdf.text.DocumentException;
import com.uetty.common.tool.core.DocTool;
import com.uetty.common.tool.core.FileTool;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipException;

public class TestCreateDocx {
	
	private static Object xmlEscape(Object value) {
		if (value == null) return value;
		if (value instanceof CharSequence) {
			return value.toString().replace("<", "&lt;").replace(">", "&gt;");
		}
		return value;
	}

	public static void main(String[] args) throws ZipException, IOException, TemplateException, DocumentException {
		URL resource = TestCreateDocx.class.getResource("/");
        if (resource == null) {
        	resource = TestCreateDocx.class.getClass().getProtectionDomain().getCodeSource().getLocation();
        }
        
		String path = resource.getPath();
		if (!path.endsWith(File.separator)) {
			path += File.separator;
		}
		path += "docx" + File.separator + "template" + File.separator;
		String docxPath = path + "exportProjectAudit.docx";
		String docXmlPath = path + "exportProjectAudit.ftl";
		String headXmlPath = path + "exportProjectAuditHead.ftl";
		
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("id", 233);
		dataMap.put("projName", xmlEscape("项目2222"));
		dataMap.put("priority", "中");
		dataMap.put("createUser", xmlEscape("慕容风"));
		dataMap.put("isUrgent", false);
		dataMap.put("createTime", new Date());
		dataMap.put("bizType", xmlEscape("军民融合"));
		dataMap.put("maxRoundsNum", 2);
		dataMap.put("flow", xmlEscape("aaa->bbb"));
		StringBuffer documentStr = new StringBuffer("没有文档");
		dataMap.put("documents", documentStr.toString());
		dataMap.put("majorReason", xmlEscape("没有啥原因  iu=就是随便写写\n给我额发我噶发\n给我额"));
		dataMap.put("expectedGoal", xmlEscape("<共有产权sgw>"));
		dataMap.put("marketSign", xmlEscape("没合法服务噶但是地方"));
		dataMap.put("note", xmlEscape(null));
		
		List<Map<String, Object>> opinionsList = new ArrayList<Map<String,Object>>();
		Short roundsNumCur = -1;
		List<Map<String, Object>> listCur = null;
		
		Short[] roundsNums = {2, 2, 1, 1};
		String[] operNames = {"路飞", "曾阿牛", "葛身上", "李广告"};
		for (int i = 0; i < roundsNums.length; i++) {
			
			if (!Objects.equals(roundsNumCur, roundsNums[i])) {
				Map<String, Object> opnsCur = new HashMap<String, Object>();
				opinionsList.add(opnsCur);
				roundsNumCur = roundsNums[i];
				opnsCur.put("roundsNum", roundsNumCur);
				listCur = new ArrayList<Map<String,Object>>();
				opnsCur.put("list", listCur);
			}
			Map<String, Object> opn = new HashMap<String, Object>();
			opn.put("operName", xmlEscape(operNames[i]));
			opn.put("operTime", new Date());
			opn.put("judge", ((int)(Math.random() * 3)));
			opn.put("opinion", xmlEscape("观点" + UUID.randomUUID().toString().substring(0, 8)));
			opn.put("risk", xmlEscape(Math.random() > 0.5 ? null : ("没有风险" + UUID.randomUUID().toString())));
			listCur.add(opn);
		}
		dataMap.put("opinionsList", opinionsList);
		
		Map<String, File> ftlFileMap = new HashMap<>();
		ftlFileMap.put("word/document.xml", new File(docXmlPath));
		ftlFileMap.put("word/header1.xml", new File(headXmlPath));
		
		File createDocx = DocTool.createDocx(new File(docxPath), ftlFileMap, dataMap);
		
		DocTool.docxConvertToPdf(createDocx, new File(FileTool.randomFilePathByExtName("pdf")));
	}
}
