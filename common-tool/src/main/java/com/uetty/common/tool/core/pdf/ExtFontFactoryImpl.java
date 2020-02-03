package com.uetty.common.tool.core.pdf;

import com.itextpdf.text.FontFactoryImp;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class ExtFontFactoryImpl extends FontFactoryImp {

	private static final String jarPathFeture = ".jar";
	private static final String fontsPath = "pdf" + File.separator + "fonts" + File.separator;
	private static final String resourceAncho = "common-tool-source.anchor";// 定位资源的锚点
	
	public ExtFontFactoryImpl() {
		super();
	}

	@Override
	public int registerDirectories() {
		int i = super.registerDirectories();

		URL resource = this.getClass().getResource("/" + resourceAncho);
		if (resource == null) {
			resource = this.getClass().getProtectionDomain().getCodeSource().getLocation();
		}
		String classFolder = resource.getPath();

		// 去锚点路径
		int indexOf = classFolder.indexOf(resourceAncho);
		if (indexOf > 0) {
			classFolder = classFolder.substring(0, indexOf);
		}
		
		if (!classFolder.endsWith(File.separator)) {
			classFolder += File.separator;
		}
		
		if (isFolderInJar(classFolder)) {// 资源在jar包内
			try {
				i += registerFromJarFolder(classFolder);
			} catch (IOException e) {
			}
		} else {// 资源不在jar包内
			String fontFolder = classFolder + fontsPath;
			i += registerDirectory(fontFolder, true);
		}
        
		return i;
	}
	
	
	private boolean isFolderInJar(String path) {
		return path.indexOf(jarPathFeture) > 0;
	}
	
	
	private int registerFromJarFolder(String path) throws IOException {
		if (path.startsWith("file:")) {
			path = path.substring(5);
		}
		int indexOf = path.indexOf(jarPathFeture);
		JarFile jarFile = null;
		int i = 0;
		try {
			String jarPath = path.substring(0, indexOf) + ".jar";
			jarFile = new JarFile(jarPath);
			Enumeration<JarEntry> entries = jarFile.entries();
			
			while (entries.hasMoreElements()) {
				String resPath = entries.nextElement().toString();
					
				if (resPath.startsWith(fontsPath) && resPath.endsWith(".ttc")) {
					
					String iFontPath = "jar:file:" + jarPath + "!" + File.separator + resPath;

					try {
						i++;
						register(iFontPath);
					} catch (Exception e) {
						e.printStackTrace();
						i--;
					}
					
				}
			}
		} finally {
			if (jarFile != null) {
				jarFile.close();
			}
		}
		return i;
	}
}
