package com.uetty.common.tool.core;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class QRCodeUtil {
    // 二维码尺寸
    private static final int QRCODE_SIZE = 300;
    // LOGO宽度
    private static final int LOGO_WIDTH = 60;
    // LOGO高度
    private static final int LOGO_HEIGHT = 60;
    // 白色边框宽度
    private static final int MARGIN = 8;

    private static BufferedImage createImage(String content, String logoImgPath, ErrorCorrectionLevel errorCorrectionLevel) throws WriterException, IOException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        // 容错级别，容错级别越高，生成的二维码信息量越大，密度也就越大
        hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(EncodeHintType.MARGIN, 0);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content,
                BarcodeFormat.QR_CODE, QRCODE_SIZE, QRCODE_SIZE, hints);

        int[] enclosingRectangle = bitMatrix.getEnclosingRectangle();
        int qrcodeRealSize = enclosingRectangle[2] * QRCODE_SIZE / (QRCODE_SIZE - 2 * MARGIN);
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
                image.setRGB(x , y,  isBlack ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        BufferedImage zoom = new BufferedImage(QRCODE_SIZE, QRCODE_SIZE, image.getType());
        Graphics graphics = zoom.getGraphics();
        graphics.drawImage(image, 0, 0, QRCODE_SIZE, QRCODE_SIZE, null);
        graphics.dispose();

        if (logoImgPath == null || "".equals(logoImgPath)) {
            return zoom;
        }
        // 插入图片
        embedImage(zoom, logoImgPath);
        return zoom;
    }

    /**
     * 嵌入LOGO
     * @param source 二维码图片
     * @param imgPath LOGO图片地址
     */
    private static void embedImage(BufferedImage source, String imgPath) throws IOException {
        File file = new File(imgPath);
        Image src = ImageIO.read(file);
        int width = src.getWidth(null);
        int height = src.getHeight(null);
        if (width > LOGO_WIDTH || height > LOGO_HEIGHT) { // 压缩LOGO
            if (width > LOGO_WIDTH) {
                width = LOGO_WIDTH;
            }
            if (height > LOGO_HEIGHT) {
                height = LOGO_HEIGHT;
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
        int x = (QRCODE_SIZE - width) / 2;
        int y = (QRCODE_SIZE - height) / 2;
        graph.drawImage(src, x, y, width, height, null);
        Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
        graph.setStroke(new BasicStroke(3f));
        graph.draw(shape);
        graph.dispose();
    }

    public static void createQRCode(String content, OutputStream output) throws IOException, WriterException {
        createQRCode(content, output, ErrorCorrectionLevel.L);
    }

    public static void createQRCode(String content, File file) throws IOException, WriterException {
        createQRCode(content, file, ErrorCorrectionLevel.L);
    }

    public static void createQRCode(String content, String logoImgPath, OutputStream output) throws IOException, WriterException {
        createQRCode(content, logoImgPath, output, ErrorCorrectionLevel.L);
    }

    public static void createQRCode(String content, String logoImgPath, File file) throws IOException, WriterException {
        createQRCode(content, logoImgPath, file, ErrorCorrectionLevel.L);
    }


    public static void createQRCode(String content, OutputStream output, ErrorCorrectionLevel level) throws IOException, WriterException {
        createQRCode(content, null, output, level);
    }

    public static void createQRCode(String content, File file, ErrorCorrectionLevel level) throws IOException, WriterException {
        createQRCode(content, null, file, level);
    }

    /**
     * 生成二维码(内嵌LOGO)
     * @param content 内容
     * @param logoImgPath LOGO地址
     * @param output 输出流
     * @param level 容错级别
     * @throws IOException io exception
     * @throws WriterException writer exception
     */
    public static void createQRCode(String content, String logoImgPath, OutputStream output, ErrorCorrectionLevel level)
            throws IOException, WriterException {
        BufferedImage image = createImage(content, logoImgPath, level);
        ImageIO.write(image, "JPG", output);
    }

    public static void createQRCode(String content, String logoImgPath, File file, ErrorCorrectionLevel level) throws IOException, WriterException {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            createQRCode(content, logoImgPath, outputStream, level);
        }
    }

    public static String createQrCodeBase64(String s, String logoImgPath, String content) throws IOException, WriterException {
        return createQrCodeBase64(content, ErrorCorrectionLevel.L);
    }

    public static String createQrCodeBase64(String content, String logoImgPath) throws IOException, WriterException {
        return createQrCodeBase64(content, logoImgPath, ErrorCorrectionLevel.L);
    }

    public static String createQrCodeBase64(String content, ErrorCorrectionLevel level) throws IOException, WriterException {
        return createQrCodeBase64(content, null, level);
    }

    public static String createQrCodeBase64(String content, String logoImgPath, ErrorCorrectionLevel level) throws IOException, WriterException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            BufferedImage image = createImage(content, logoImgPath, level);

            ImageIO.write(image, "JPG", baos);

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    /**
     * 解析二维码
     * @param file 二维码图片
     * @return 二维码解析后的文本
     * @throws IOException io exception
     * @throws NotFoundException not found qr code
     */
    public static String readFromQRCode(File file) throws IOException, NotFoundException {
        BufferedImage image = ImageIO.read(file);
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        Result result = new MultiFormatReader().decode(bitmap, hints);
        return result.getText();
    }

}
