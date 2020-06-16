package com.uetty.common.tool.core;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FreemarkerEngine {
	
	private static final Map<String, Template> templateMap = new ConcurrentHashMap<>();
	
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
		Template template = templateMap.get(tempFilePath);
		if (template == null) {
			File tempFile = new File(tempFilePath);
			Configuration config = FreemarkerEngine.createConfig(tempFile.getParentFile());
			template = config.getTemplate(tempFile.getName());
			templateMap.put(tempFilePath, template);
		}
		return template;
	}
		
	private static Configuration createConfig(File tempFileFolder) throws IOException {
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
		cfg.setWrapUncheckedExceptions(true);
		cfg.setDirectoryForTemplateLoading(tempFileFolder);
		return cfg;
	}

	public static void main(String[] args) {

	}
}
