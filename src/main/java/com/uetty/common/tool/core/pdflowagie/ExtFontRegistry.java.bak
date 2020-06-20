//package com.uetty.common.tool.core.pdflowagie;

//import java.awt.Color;
//
//import com.lowagie.text.ExceptionConverter;
//import com.lowagie.text.Font;
//import com.lowagie.text.FontFactory;
//
//import fr.opensagres.xdocreport.itext.extension.font.AbstractFontRegistry;
//import fr.opensagres.xdocreport.itext.extension.font.AbstractFontRegistry.ExtendedBaseFont;

///**
// * 依赖于旧版lowagie itext的转pdf，不过貌似新版转pdf转完后格式有点问题
// * @author vince
// */
//public class ExtFontRegistry extends AbstractFontRegistry {
//
//	public static ExtFontFactoryImpl extFontFactoryImp = new ExtFontFactoryImpl();
//	private static final ExtFontRegistry INSTANCE = new ExtFontRegistry();
//	private static boolean fontRegistryInitialized = false;
//
//	private ExtFontRegistry() {
//		FontFactory.setFontImp(extFontFactoryImp);
//	}
//
//	@Override
//	protected String resolveFamilyName(String familyName, int style) {
//		if ("宋体".equals(familyName) || "SimSun".equals(familyName)) {
//			return "simsun";
//		}
//		if ("微软雅黑".equals(familyName)) {
//			return "microsoft yahei";
//		}
//		// System.out.println(familyName + "---" + style);
//		return "microsoft yahei";
//	}
//
//	private void initFontRegistryIfNeeded() {
//		if (!fontRegistryInitialized) {
//			// clear built-in fonts which may clash with document fonts
//			ExtendedBaseFont.clearBuiltinFonts();
//			// register fonts from files (ex : for windows, load files from
//			// C:\WINDOWS\Fonts)
//			FontFactory.registerDirectories();
//			extFontFactoryImp.registerDirectories();
//			fontRegistryInitialized = true;
//		}
//	}
//
//	@Override
//	public Font getFont(String familyName, String encoding, float size, int style, Color color) {
//		initFontRegistryIfNeeded();
//		if (familyName != null) {
//			familyName = resolveFamilyName(familyName, style);
//		}
//		try {
//			return extFontFactoryImp.getFont(familyName, encoding, size, style, color);
//		} catch (ExceptionConverter e) {
//			return new Font(Font.UNDEFINED, size, style, color);
//		}
//	}
//
//	public static ExtFontRegistry getRegistry() {
//		return INSTANCE;
//	}
//}