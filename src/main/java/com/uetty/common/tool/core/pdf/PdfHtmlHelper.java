package com.uetty.common.tool.core.pdf;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Matrix;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class PdfHtmlHelper {

    private static final float DEFAULT_DPI = 1.0f;
    private static final String DEFAULT_FORMAT = "PNG";

//    public static byte[] combineFiles(List<String> files, PDRectangle pdRectangle) {
//        PDDocument pdDocument = new PDDocument();
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        try {
//            for (String file : files){
//                boolean isImage = FilenameUtils.isExtension(file, new String[]{"PNG","JPG","JPEG","BMP"});
//                boolean isPdf = FilenameUtils.isExtension(file, "PDF");
//                if(isImage){
//                    PDPage pdPage = new PDPage(pdRectangle);
//                    pdDocument.addPage(pdPage);
//                    fillPageFileImage(input,pdDocument,pdPage);
//                }else  if(isPdf){
//                    S3ObjectInputStream input = S3Util.downloadFile(file);
//                    PDDocument dd = PDDocument.load(input);
//                    for (int i = 0; i < dd.getNumberOfPages(); i++) {
//                        pdDocument.addPage(dd.getPage(i));
//                    }
//                }else{
//                    log.warn("file format error: {}",file);
//                }
//            }
//            pdDocument.save(outputStream);
//            pdDocument.close();
//        } catch (IOException e) {
//            log.info("pdf file combine failed", e);
//            throw new RuntimeException(e);
//        }
//        return outputStream.toByteArray();
//    }

    public static void fillPageFileImage(InputStream imgIn, PDDocument document, PDPage page) throws IOException {
        PDImageXObject img1 = PDImageXObject.createFromByteArray(document, IOUtils.toByteArray(imgIn), "PNG");
        float w0 = PDRectangle.A4.getWidth();
        float h0 = PDRectangle.A4.getHeight();
        float w = img1.getWidth();
        float h = img1.getHeight();
        float w1;
        float h1;
        if(w/h>h0/h){
            w1 = w0;
            h1 = h*w0/w;
        }else{
            h1 = h0;
            w1 = w*h0/h;
        }
        AffineTransform transform = new AffineTransform(w1, 0, 0, h1, (w0-w1)/2, h0-h1);
        PDPageContentStream contents = new PDPageContentStream(document, page);
        contents.drawImage(img1,new Matrix(transform));
        contents.close();
    }
    public static void addWaterMark(String src,String out,String markerText,String font) throws Exception {
        addWaterMark(new FileInputStream(src),new FileOutputStream(out),markerText,font);
    }

    public static void html2Pdf(String content, File targetFile, String fontDirectory){
        try{
            html2Pdf(content,new FileOutputStream(targetFile),fontDirectory);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    /**
     *
     * @param content html 文本内容
     * @param targetFile 生成的pdf目标文件
     * @param fontDirectory 生产pdf时所需字体
     * @throws IOException io exception
     * @throws DocumentException document exception
     */
    public static void html2Pdf(String content,String targetFile,String fontDirectory) throws IOException, DocumentException {
        html2Pdf(content,new FileOutputStream(targetFile),fontDirectory);
    }
    public static void html2Pdf(String content, OutputStream outputStream, String fontDirectory) throws IOException, DocumentException{
        ITextRenderer renderer = new ITextRenderer();
        ITextFontResolver fontResolver = (ITextFontResolver) renderer.getSharedContext().getFontResolver();
        File f = new File(fontDirectory);
        if (f.isDirectory()) {
            File[] files = f.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    String lower = name.toLowerCase();
                    return lower.endsWith(".otf") || lower.endsWith(".ttf") || lower.endsWith("ttc");
                }
            });
            for (int i = 0; files!=null && i < files.length; i++) {
                fontResolver.addFont(files[i].getAbsolutePath(), BaseFont.IDENTITY_H, true);
            }
        }
        renderer.setDocument(content.getBytes());
        renderer.layout();
        renderer.createPDF(outputStream);
        renderer.finishPDF();
    }
    public static List<BufferedImage> convertToImage(File file) throws IOException {
        PDDocument document = PDDocument.load(file);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        List<BufferedImage> bufferedImageList = new ArrayList<>();

        for (int page = 0;page<document.getNumberOfPages();page++){
            BufferedImage img = pdfRenderer.renderImageWithDPI(page, 100, ImageType.GRAY);
            bufferedImageList.add(img);
        }
        document.close();

        return bufferedImageList;
    }
    /**
     * 【功能描述：添加文字水印】
     * @param input 输入流
     * @param outputStream 输出流
     * @param logoText logo文本
     * @param fontUrl 字体路径
     * @throws Exception exception
     */
    public static void addWaterMark(InputStream input, OutputStream outputStream, String logoText, String fontUrl)
            throws Exception {
        // 待加水印的文件
        PdfReader reader = new PdfReader(input);
        // 加完水印的文件
        PdfStamper pdfStamper = new PdfStamper(reader, outputStream);
        int pageNumber = reader.getNumberOfPages() + 1;

        PdfContentByte content;
        // 检查字体文件存不存在
        if (!new File(fontUrl).exists()) {
            throw new RuntimeException(fontUrl+" 字体文件不存在");
        }
        //创建字体
        BaseFont font = BaseFont.createFont(fontUrl, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        // 设置水印透明度
        PdfGState gs = new PdfGState();
        // 设置填充字体不透明度为0.4f
        gs.setFillOpacity(0.4f);

        // 循环对每页插入水印
        for (int i = 1; i < pageNumber; i++) {
            Rectangle size = reader.getPageSize(1);
            float w = size.getWidth();
            float h = size.getHeight();
            float len = font.getWidthPoint(logoText,60);
            float x = (w-len)/2;
            float y = (h-60)/2;
            // 水印在之前文本下
            content = pdfStamper.getOverContent(i);

            // 开始
            content.beginText();
            // 设置水印字体参数及大小   (字体参数，字体编码格式，是否将字体信息嵌入到pdf中（一般不需要嵌入），字体大小)
            content.setFontAndSize(font, 60);
            // 设置透明度
            content.setGState(gs);
            // 设置水印对齐方式 水印内容 X坐标 Y坐标 旋转角度
            content.showTextAligned(Element.ALIGN_CENTER, logoText, w/2, y, 45);
            // 设置水印颜色(灰色)
            content.setColorFill(BaseColor.GRAY);
            //结束
            content.endText();
        }
        pdfStamper.close();
    }

    private static byte[] toByte(java.awt.Image image,int width,int height) throws IOException {
        BufferedImage bufferedimage = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB );
        bufferedimage.getGraphics().drawImage(image,0,0,width,height,null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(bufferedimage,"PNG",output);
        return output.toByteArray();
    }
    public static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }
        return image;
    }
    public static void addQrCode(File input, File finalfile, String contents,int width,int height) throws Exception {
        addQrCode(new FileInputStream(input),new FileOutputStream(finalfile),contents,width,height);
    }
    public static void addQrCode(InputStream inputStream,OutputStream outputStream,String contents,int width,int height) throws Exception {
        Map<EncodeHintType,Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN,"0");
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = multiFormatWriter.encode(contents, BarcodeFormat.QR_CODE, width*2, height*2, hints);
        BufferedImage qrcodeImg = toBufferedImage(bitMatrix);

        PdfReader reader = new PdfReader(inputStream);
        PdfStamper pdfStamper = new PdfStamper(reader, outputStream);
        PdfContentByte content = pdfStamper.getOverContent(1);
        com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(qrcodeImg,Color.BLACK);
        Rectangle rect = reader.getPageSize(1);
        content.addImage(image,width,0,0,height,rect.getWidth()-width-10,rect.getHeight()-height-10);
        pdfStamper.close();

    }
}
