package com.uetty.common.tool.core;

import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ImageUtil {

    static final String SECTION_SEPARATOR = ";";
    static final String MIMETYPE_SEPARATOR = ":";
    static final String BASE64_SEPARATOR = ",";
    static final String IMG_BASE64_FORMAT = "data:%s;base64,%s";
    static final String DEFAULT_MIMETYPE = "image/png";

    public static String imageToBase64(File file) throws IOException {
        byte[] bytes = FileUtil.readToByte(file);

        String name = file.getName();
        String mimetype = Mimetypes.getInstance().getMimetype(name);

        String base64 = Base64.getEncoder().encodeToString(bytes);

        return String.format(IMG_BASE64_FORMAT, mimetype, base64);
    }

    public static String imageToBase64(InputStream inputStream, String mimetype) throws IOException {
        byte[] bytes = FileUtil.readToByte(inputStream);

        String base64 = Base64.getEncoder().encodeToString(bytes);

        return String.format(IMG_BASE64_FORMAT, mimetype.toLowerCase(), base64);
    }

    public static ImageInfo base64ToImage(String base64) throws IOException {
        String[] split = base64.split(SECTION_SEPARATOR);
        String mimetype;
        byte[] bytes;

        if (split.length != 2) {
            throw new IOException("invalid base64");
        }

        String[] split1 = split[0].split(MIMETYPE_SEPARATOR);
        if (split1.length != 2) {
            throw new IOException("invalid base64");
        }
        mimetype = split1[1];

        String[] split2 = split[1].split(BASE64_SEPARATOR);
        if (split2.length != 2) {
            throw new IOException("invalid base64");
        }
        bytes = Base64.getDecoder().decode(split2[1].getBytes(StandardCharsets.UTF_8));

        return new ImageInfo(mimetype, bytes);
    }

    @Getter
    public static class ImageInfo {

        String mimetype;

        byte[] bytes;

        int size;

        public ImageInfo(String mimetype, byte[] bytes) {
            this.mimetype = mimetype;
            this.bytes = bytes;
            size = bytes.length;
        }
    }
}
