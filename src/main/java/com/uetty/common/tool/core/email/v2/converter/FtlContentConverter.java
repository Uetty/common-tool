package com.uetty.common.tool.core.email.v2.converter;

import com.uetty.common.tool.core.email.v2.FreemarkerEngine;
import com.uetty.common.tool.core.email.v2.JavaEmailSender;
import com.uetty.common.tool.core.email.v2.ftl.FtlEnum;
import com.uetty.common.tool.core.email.v2.model.FtlMailMessage;
import com.uetty.common.tool.core.email.v2.model.MailMessage;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author : Vince
 */
public class FtlContentConverter implements ContentConverter {

    private final Logger LOG = LoggerFactory.getLogger(FtlContentConverter.class);

    private boolean accept(MailMessage mailMessage) {
        return mailMessage instanceof FtlMailMessage;
    }

    private void resolveInlineFile(Multipart multipart, Map<String, Object> dataModel) {
        if (dataModel == null) {
            return;
        }
        Map<String, Object> cloneModel = new HashMap<>(dataModel);
        List<Map.Entry<String, Object>> list = cloneModel.entrySet().stream().filter(entry -> entry.getValue() instanceof File).collect(Collectors.toList());
        if (list.size() == 0) return;

        list.forEach(entry -> {
            try {
                String key = entry.getKey();
                String inlineId = UUID.randomUUID().toString();
                inlineId = MimeUtility.encodeWord(inlineId);
                File file = (File) entry.getValue();

                MimeBodyPart mimeBodyPart = new MimeBodyPart();
                mimeBodyPart.setDisposition("inline");
                mimeBodyPart.setHeader("Content-ID", "<" + inlineId + ">");
                FileDataSource fileDataSource = new FileDataSource(file);
                fileDataSource.setFileTypeMap(JavaEmailSender.DEFAULT_FILE_TYPE_MAP);
                mimeBodyPart.setDataHandler(new DataHandler(fileDataSource));
                multipart.addBodyPart(mimeBodyPart);

                dataModel.put(key, "cid:" + inlineId);
            } catch (MessagingException | UnsupportedEncodingException e) {
                LOG.warn(e.getMessage(), e);
            }
        });
    }

    private String getContent(FtlMailMessage ftlMailInfo) throws IOException, TemplateException {
        FtlEnum ftlEnum = ftlMailInfo.getFtlEnum();

        File file = new File(ftlEnum.getFilePath());

        Map<String, Object> dataModel = ftlMailInfo.getDataModel();
        return FreemarkerEngine.process(dataModel, file.getAbsolutePath());
    }
    
    @Override
    public void setContent(Multipart multipart, MailMessage mailMessage) throws MessagingException {
        if (!accept(mailMessage)) return;

        FtlMailMessage ftlMailInfo = (FtlMailMessage) mailMessage;
        Map<String, Object> dataModel = ftlMailInfo.getDataModel();
        resolveInlineFile(multipart, dataModel);

        

        // 创建消息部分
        BodyPart messageBodyPart = new MimeBodyPart();

        try {
            String content = getContent(ftlMailInfo);
            if (ftlMailInfo.getHtmlType()) {
                messageBodyPart.setContent(content, "text/html;charset=utf-8");
            } else {
                messageBodyPart.setText(content);
            }
            // 设置文本消息部分
            multipart.addBodyPart(messageBodyPart);

        } catch (IOException | TemplateException e) {
            LOG.warn(e.getMessage(), e);
        }
    }

}
