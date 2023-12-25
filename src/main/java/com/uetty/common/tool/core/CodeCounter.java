package com.uetty.common.tool.core;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CodeCounter {


    public static long countCode(File file, Set<String> extNames, Set<String> ignoreFolders) {
        if (!file.exists()) {
            System.out.println("该文件不存在！");
            return 0;
        }
        File[] fs = file.listFiles();
        long count = 0;
        if (fs == null) {
            return count;
        }
        for (File file2 : fs) {
            final String name = file2.getName();

            if(file2.isDirectory()){
                boolean isIgnore = false;
                for (String ignoreExt : ignoreFolders) {
                    if (name.equalsIgnoreCase(ignoreExt)) {
                        isIgnore = true;
                        break;
                    }
                }

                if (isIgnore) {
                    continue;
                }
                count += countCode(file2, extNames, ignoreFolders);
            }else{

                boolean isTarget = false;
                for (String extName : extNames) {
                    if(name.endsWith(extName)){
                        isTarget = true;
                        break;
                    }
                }
                if (isTarget) {

                    final List<String> strings;
                    try {
                        strings = FileUtil.readLines(file2);
                        count += strings.size();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return count;
    }

    public static void main(String[] args) {
        final File file = new File("/Users/xxx/code");

        Set<String> extNames = new HashSet<>();
        extNames.add("java");
//        extNames.add("js");
//        extNames.add("vue");
//        extNames.add("html");

        Set<String> ignoreFolders = new HashSet<>();
        ignoreFolders.add(".git");
        ignoreFolders.add("node_modules");

        final long l = countCode(file, extNames, ignoreFolders);

        System.out.println("代码行数 --> " + l);
    }
}
