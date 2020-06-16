package com.uetty.common.tool.core.pdf;

import com.itextpdf.text.FontFactory;
import fr.opensagres.xdocreport.itext.extension.font.AbstractFontRegistry;

public class ExtFontRegistry extends AbstractFontRegistry {

	public static ExtFontFactoryImpl extFontFactoryImp = new ExtFontFactoryImpl();
	private static final ExtFontRegistry INSTANCE = new ExtFontRegistry();
	
	private ExtFontRegistry() {
		FontFactory.setFontImp(extFontFactoryImp);
	}

	@Override
	protected String resolveFamilyName(String familyName, int style) {
		if ("\u5b8b\u4f53".equals(familyName) || "SimSun".equals(familyName)) {// 宋体
			return "simsun";
		}
		// 微软雅黑
		return "microsoft yahei";
	}

	public static ExtFontRegistry getRegistry() {
		return INSTANCE;
	}
}