//package com.uetty.common.tool.core.pdflowagie;

//import java.io.File;
//import java.io.InputStream;
//import java.net.URL;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.lowagie.text.FontFactoryImp;

///**
// * 依赖于旧版lowagie itext的转pdf，不过貌似新版转pdf转完后格式有点问题
// * @author vince
// */
//class ExtFontFactoryImpl extends FontFactoryImp {
//
//	public ExtFontFactoryImpl() {
//		super();
//	}
//
//	@Override
//	public int registerDirectories() {
//		int i = super.registerDirectories();
//
//		URL resource = ExtFontFactoryImpl.class.getResource("common-tool-source.anchor");
//		if (resource == null) {
//			resource = ExtFontFactoryImpl.class.getProtectionDomain().getCodeSource().getLocation();
//		}
//		System.out.println("---->" + resource);
//		System.out.println("-->" + new File("pdf").getAbsolutePath());
//
//		URL resource2 = ExtFontFactoryImpl.class.getResource("");
//		System.out.println(resource2);
//		URL resource3 = ExtFontFactoryImpl.class.getClassLoader().getResource("pdf/fonts/msyh.ttc");
//		System.out.println(resource3);
//		InputStream asStream = ExtFontFactoryImpl.class.getClassLoader().getResourceAsStream("pdf/fonts/msyh.tcc");
//		System.out.println(asStream);
//		InputStream asStream2 = ExtFontFactoryImpl.class.getClassLoader().getResourceAsStream("/pdf/fonts/msyh.tcc");
//		System.out.println(asStream2);
//
//		InputStream stream = ExtFontFactoryImpl.class.getResourceAsStream("pdf/fonts/msyh.ttc");
//		System.out.println(stream);
//
//		String classFolder = resource.getPath();
//		if (!classFolder.endsWith(File.separator)) {
//			classFolder += File.separator;
//		}
//		String fontFolder = classFolder + "pdf" + File.separator + "fonts" + File.separator;
//		System.out.println(fontFolder);
//
//		System.out.println(i);
//		i += registerDirectory(fontFolder, true);
//        System.out.println(i);
//		
//		try {
//			System.out.println(new ObjectMapper().writeValueAsString(getRegisteredFamilies()));
//		} catch (JsonProcessingException e) {
//			e.printStackTrace();
//		}
//		return i;
//	}
//}
