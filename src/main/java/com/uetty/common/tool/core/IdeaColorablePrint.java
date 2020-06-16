package com.uetty.common.tool.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : Vince
 */
@SuppressWarnings("unused")
public class IdeaColorablePrint {

    private static final Map<Long, String> printColorMap = new HashMap<>();
    private static final String[] colorCodes = {"32;4m", "33;4m", "34;4m", "35;4m", "36;4m", "37;4m"};

    public static void threadPrint(Object obj) {
        String string = obj == null ? "null" : obj.toString();
        long id = Thread.currentThread().getId();
        String color;
        if ((color = printColorMap.get(id)) == null) {
            int size = printColorMap.size();
            color = colorCodes[size % colorCodes.length];
            printColorMap.put(id, color);
        }
        System.out.println("\033[" + color + " [threadid-" + id + "]" + string + "\033[0m");
    }

    public static void print(Object obj, int color) {
        String string = obj == null ? "null" : obj.toString();
        String colorFlag = colorCodes[color % colorCodes.length];
        System.out.println("\033[" + colorFlag + " " + string + "\033[0m");
    }
}
