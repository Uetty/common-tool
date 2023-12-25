package com.uetty.common.tool.core;

import lombok.Getter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ImageUtil {

    static final String SECTION_SEPARATOR = ";";
    static final String MIMETYPE_SEPARATOR = ":";
    static final String BASE64_SEPARATOR = ",";
    static final String IMG_BASE64_FORMAT = "data:%s;base64,%s";
    static final String DEFAULT_MIMETYPE = "image/png";

    private static final String URL_CHECK_REGEX = "^([a-zA-Z]+://)?([\\-a-zA-Z0-9]+(\\.[\\-a-zA-Z0-9]+)*)(:([0-9]+))?(/\\S*)*$";

    /**
     * 图片文件转base64字符串
     */
    public static String imageToBase64(File file) throws IOException {
        byte[] bytes = FileUtil.readToByte(file);

        String name = file.getName();
        String mimetype = Mimetypes.getInstance().getMimetype(name);

        String base64 = Base64.getEncoder().encodeToString(bytes);

        return String.format(IMG_BASE64_FORMAT, mimetype, base64);
    }

    /**
     * 图片文件流转base64字符串
     */
    public static String imageToBase64(InputStream inputStream, String mimetype) throws IOException {
        byte[] bytes = FileUtil.readToByte(inputStream);

        String base64 = Base64.getEncoder().encodeToString(bytes);

        return String.format(IMG_BASE64_FORMAT, mimetype.toLowerCase(), base64);
    }

    /**
     * base64图片字符串转图片文件流
     */
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

    /**
     * 获取图片尺寸
     * @param is 输入流
     * @return : [width, height]
     * @author : Vince
     * @date : 2019/6/20 16:19
     */
    public static int[] getImageDimens(InputStream is) throws IOException {
        BufferedImage bi = ImageIO.read(is);
        if (bi == null) {
            throw new IOException("cannot convert input stream to image");
        }
        int[] wh = new int[2];
        wh[0] = bi.getWidth();
        wh[1] = bi.getHeight();
        return wh;
    }

    /**
     * 获取图片尺寸
     * @param bytes 二进制数据
     * @return : [width, height]
     * @author : Vince
     * @date : 2019/6/20 16:19
     */
    public static int[] getImageDimens(byte[] bytes) throws IOException {
        BufferedImage bi = ImageIO.read(new ByteArrayInputStream(bytes));
        if (bi == null) {
            throw new IOException("cannot convert input stream to image");
        }
        int[] wh = new int[2];
        wh[0] = bi.getWidth();
        wh[1] = bi.getHeight();
        return wh;
    }

    public static boolean isUrl (String url) {
        if (url == null || "".equals(url.trim())) {
            return false;
        }
        return url.matches(URL_CHECK_REGEX);
    }

    /**
     * 根据路径自动判断url还是disk path，获取inputStream
     * @author : Vince
     * @date : 2019/6/20 16:19
     */
    public static InputStream getInputStream(String pathOrUrl) throws IOException {
        if (pathOrUrl == null || pathOrUrl.trim().length() == 0) {
            return null;
        }
        if (isUrl(pathOrUrl)) {
            URL netUrl = new URL(pathOrUrl);
            URLConnection conn = netUrl.openConnection();
            return conn.getInputStream();
        } else {
            File file = new File(pathOrUrl);
            return new FileInputStream(file);
        }
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
