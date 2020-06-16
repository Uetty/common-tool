package com.uetty.common.tool.core.email.v2;

import freemarker.core.ParseException;
import freemarker.template.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

public class FreemarkerEngine {

	public static String process(Map<String, Object> dataMap, String tempFilePath) throws IOException, TemplateException {
		Template template = getTemplate(tempFilePath);
		StringWriter writer = new StringWriter();
		template.process(dataMap, writer);
		return writer.toString();
	}

	public static File process(Map<String, Object> dataMap, String tempFilePath, String targetFilePath) throws TemplateException, IOException {
		Template template = getTemplate(tempFilePath);
		File file = new File(targetFilePath);
		FileWriter fw = new FileWriter(new File(targetFilePath));
		template.process(dataMap, fw);
		return file;
	}
	
	private static Template getTemplate(String tempFilePath) throws IOException {
		File tempFile = new File(tempFilePath);
		Configuration config = FreemarkerEngine.createConfig(tempFile.getParentFile());
		return config.getTemplate(tempFile.getName());
	}

	private static Configuration createConfig(File tempFileFolder) throws IOException {
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
		cfg.setDefaultEncoding("UTF-8");
		cfg.setEncoding(Locale.CHINA, "utf-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
		cfg.setDirectoryForTemplateLoading(tempFileFolder);
		return cfg;
	}
}
