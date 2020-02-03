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
		if ("\u5fae\u8f6f\u96c5\u9ed1".equals(familyName)) {// 微软雅黑
			return "microsoft yahei";
		}
		return "microsoft yahei";
	}

	public static ExtFontRegistry getRegistry() {
		return INSTANCE;
	}
}