package com.uetty.common.tool.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author vince
 */
public class ZipUtil {

    /**
     * 递归文件（夹）
     * @param file 文件
     * @param flat 是否扁平化
     * @return 返回所有文件map
     */
    private static Map<String, File> recursionFiles(File file, boolean flat) {
        Map<String, File> map = new HashMap<>();

        recursionFiles0(map, "", file, flat);

        return map;
    }

    /**
     * 递归文件（夹）
     * @param map 存储文件路径映射
     * @param parentDirPath 父级目录
     * @param file 文件
     * @param flat 是否扁平化
     */
    private static void recursionFiles0(Map<String, File> map, String parentDirPath, File file, boolean flat) {
        String fileName = file.getName();
        fileName = fileName.contains("/") ? fileName.replace("/", "") : fileName;

        File[] subFiles;
        if (!file.isDirectory() || (subFiles = file.listFiles()) == null || subFiles.length == 0) {
            String key;
            if (flat) {
                key = map.containsKey(fileName) ? parentDirPath + fileName : fileName;
            } else {
                key = parentDirPath + fileName;
            }
            if (file.isDirectory()) {
                file = null;
                key = key + "/";
            }
            map.put(key, file);
            return;
        }

        String baseDir = parentDirPath + fileName;
        baseDir = flat ? (baseDir + "_") : (baseDir + "/");
        for (File subFile : subFiles) {
            recursionFiles0(map, baseDir, subFile, flat);
        }
    }

    public static void zip(File zipOutFile, File... files) throws IOException {
        zip(zipOutFile, false, files);
    }

    /**
     * 生成zip文件
     * @param zipOutFile zip输出文件
     * @param flat 是否使目录层级扁平化
     * @param files 待压缩文件（夹）列表
     * @throws IOException io exception
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void zip(File zipOutFile, boolean flat, File... files) throws IOException {

        ZipOutputStream zipOut = null;

        try {
            if (!zipOutFile.exists()) {
                if (!zipOutFile.getParentFile().exists()) {
                    zipOutFile.getParentFile().mkdirs();
                }
                zipOutFile.createNewFile();
            }
            if (files == null) {
                return;
            }

            zipOut = new ZipOutputStream(new FileOutputStream(zipOutFile));

            for (File file : files) {
                Map<String, File> fileMap = recursionFiles(file, flat);
                for (Map.Entry<String, File> fileEntry : fileMap.entrySet()) {
                    String key = fileEntry.getKey();
                    File fileItem = fileEntry.getValue();
                    System.out.println(key);
                    zipOut.putNextEntry(new ZipEntry(key));

                    if (fileItem != null) {
                        FileUtil.writeFromInputStream(zipOut, new FileInputStream(fileItem));
                    }
                    zipOut.closeEntry();
                }
            }

        } finally {
            if (zipOut != null) {
                zipOut.close();
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void unzip(File outFolder, File inFile) throws IOException {
        if (!outFolder.exists()) {
            outFolder.mkdirs();
        }

        try (ZipFile zipFile = new ZipFile(inFile)) {

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry next = entries.nextElement();
                InputStream is = zipFile.getInputStream(next);

                String name = next.getName();

                System.out.println(name);

                File file = new File(outFolder, name);
                if (next.isDirectory()) {
                    file.mkdirs();
                    continue;
                }
                if (!file.exists()) {
                    File parentFile = file.getParentFile();
                    if (parentFile != null) {
                        parentFile.mkdirs();
                    }
                    file.createNewFile();
                }
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    FileUtil.writeFromInputStream(fos, is);
                }
            }
        }
    }

}
