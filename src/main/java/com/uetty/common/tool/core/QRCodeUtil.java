package com.uetty.common.tool.core;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
@Data
@Accessors(chain = true)
public class QRCodeUtil {

    private int logoMaxSize = DEFAULT_LOGO_MAX_SIZE;
    private int size = DEFAULT_QR_CODE_SIZE;
    private int margin = DEFAULT_MARGIN;
    private ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.L;
    private int blankColor = DEFAULT_QR_CODE_BLANK_COLOR;
    private int solidColor = DEFAULT_QR_CODE_SOLID_COLOR;

    // 二维码尺寸
    private static final int DEFAULT_QR_CODE_SIZE = 300;
    // LOGO最大尺寸
    private static final int DEFAULT_LOGO_MAX_SIZE = 120;
    // 白色边框宽度
    private static final int DEFAULT_MARGIN = 8;

    // 空白部分颜色
    private static final int DEFAULT_QR_CODE_BLANK_COLOR = 0xFFFFFFFF;
    // 非空白部分颜色
    private static final int DEFAULT_QR_CODE_SOLID_COLOR = 0xFF000000;

    private BufferedImage createImage(String content, InputStream logoInputStream) throws WriterException, IOException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        // 容错级别，容错级别越高，生成的二维码信息量越大，密度也就越大
        hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(EncodeHintType.MARGIN, 0);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content,
                BarcodeFormat.QR_CODE, size, size, hints);

        int[] enclosingRectangle = bitMatrix.getEnclosingRectangle();

        int qrcodeRealSize = enclosingRectangle[2] * size / (size - 2 * margin);
        int realMargin = (qrcodeRealSize - enclosingRectangle[2]) / 2;
        BufferedImage image = new BufferedImage(qrcodeRealSize, qrcodeRealSize,
                BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < qrcodeRealSize; x++) {
            for (int y = 0; y < qrcodeRealSize; y++) {
                boolean isBlack;
                int enclosingX = x - realMargin;
                int enclosingY = y - realMargin;
                if (enclosingX < 0 || enclosingY < 0
                        || enclosingX >= enclosingRectangle[2]
                        || enclosingY >= enclosingRectangle[3]) {
                    isBlack = false;
                } else {
                    isBlack = bitMatrix.get(enclosingX + enclosingRectangle[0], enclosingY + enclosingRectangle[1]);
                }
                image.setRGB(x , y,  isBlack ? blankColor : solidColor);
            }
        }

        BufferedImage zoom = new BufferedImage(size, size, image.getType());
        Graphics graphics = zoom.getGraphics();
        graphics.drawImage(image, 0, 0, size, size, null);
        graphics.dispose();

        if (logoInputStream == null) {
            return zoom;
        }
        // 插入图片
        embedImage(zoom, logoInputStream);
        return zoom;
    }

    /**
     * 嵌入LOGO
     * @param source 二维码图片
     * @param logoInputStream LOGO图片输入流
     */
    private void embedImage(BufferedImage source, InputStream logoInputStream) throws IOException {
        Image src = ImageIO.read(logoInputStream);
        int width = src.getWidth(null);
        int height = src.getHeight(null);
        if (width > logoMaxSize || height > logoMaxSize) { // 压缩LOGO
            if (width > logoMaxSize) {
                height = (int) ((double) logoMaxSize / width * height);
                width = logoMaxSize;

            }
            if (height > logoMaxSize) {
                width = (int) ((double) logoMaxSize / height * width);
                height = logoMaxSize;
            }
            Image image = src.getScaledInstance(width, height,
                    Image.SCALE_SMOOTH);
            BufferedImage tag = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics g = tag.getGraphics();

            g.drawImage(image, 0, 0, null); // 绘制缩小后的图
            g.dispose();
            src = image;
        }
        // 插入LOGO
        Graphics2D graph = source.createGraphics();
        graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int x = (size - width) / 2;
        int y = (size - height) / 2;
        graph.drawImage(src, x, y, width, height, null);
        // logo轮廓
//        Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
//        graph.setStroke(new BasicStroke(3f));
//        graph.draw(shape);
        graph.dispose();
    }

    public void createQRCode(String content, File file) throws IOException, WriterException {
        createQRCode(content, null, file);
    }

    public void createQRCode(String content, OutputStream output) throws IOException, WriterException {
        createQRCode(content, null, output);
    }

    public void createQRCode(String content, InputStream logoInputStream, File file) throws IOException, WriterException {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            createQRCode(content, logoInputStream, outputStream);
        }
    }

    public void createQRCode(String content, URL logoUrl, ByteArrayOutputStream baos) throws IOException, WriterException {
        try (InputStream logoInputStream = logoUrl.openStream()) {
            createQRCode(content, logoInputStream, baos);
        }
    }

    /**
     * 生成二维码(内嵌LOGO)
     * @param content 内容
     * @param logoInputStream LOGO图片输入
     * @param output 输出流
     */
    public void createQRCode(String content, InputStream logoInputStream, OutputStream output)
            throws IOException, WriterException {

        BufferedImage image = createImage(content, logoInputStream);
        ImageIO.write(image, "PNG", output);
    }

    public String createQrCodeBase64(String content, File file) throws IOException, WriterException {
        try (InputStream is = new FileInputStream(file)) {
            return createQrCodeBase64(content, is);
        }
    }

    public String createQrCodeBase64(String content) throws IOException, WriterException {
        return createQrCodeBase64(content, (InputStream) null);
    }

    public String createQrCodeBase64(String content, InputStream logoInputStream) throws IOException, WriterException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            createQRCode(content, logoInputStream, baos);

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    public String createQrCodeBase64(String content, URL logoUrl) throws IOException, WriterException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             InputStream logoInputStream = logoUrl.openStream()) {

            createQRCode(content, logoInputStream, baos);

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    /**
     * 解析二维码
     * @param file 二维码图片
     */
    public String readFromQRCode(File file) throws IOException, NotFoundException {
        BufferedImage image = ImageIO.read(file);
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        Result result = new MultiFormatReader().decode(bitmap, hints);
        return result.getText();
    }

}
