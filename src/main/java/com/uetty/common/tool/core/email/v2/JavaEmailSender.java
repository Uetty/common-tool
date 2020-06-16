package com.uetty.common.tool.core.email.v2;

import com.sun.mail.util.MailSSLSocketFactory;
import com.uetty.common.tool.core.email.v2.converter.ContentConverter;
import com.uetty.common.tool.core.email.v2.converter.FtlContentConverter;
import com.uetty.common.tool.core.email.v2.model.FtlMailMessage;
import com.uetty.common.tool.core.email.v2.model.MailFile;
import com.uetty.common.tool.core.email.v2.model.MailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * 不使用spring库
 * @author vince
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class JavaEmailSender {

	@SuppressWarnings("unused")
	private Logger LOG = LoggerFactory.getLogger(JavaEmailSender.class);
	
	private Properties properties;

	public static final FileTypeMap DEFAULT_FILE_TYPE_MAP = new MimetypesFileTypeMap();

	private FileTypeMap fileTypeMap = DEFAULT_FILE_TYPE_MAP;
	
	private volatile boolean initialized = false;
	
	private final Object initLock = new Object();
	
	private String username;

	private String password;

	private String smtpServer;

	private String smtpPort = "25";

	/**
	 * 需要SSL加密的地址
	 * <p>类似于HTTPS
	 */
	private boolean useSSL = false;

	private boolean useSTARTTLS;

	private final Map<Class<? extends MailMessage>, ContentConverter> contentConverterMap = new HashMap<>();

	public JavaEmailSender(String userName, String password, String smtpServer, String smtpPort, boolean useSSL) {
		this(userName, password, smtpServer, smtpPort);
		this.useSSL = useSSL;
	}

	public JavaEmailSender(String userName, String password, String smtpServer, String smtpPort) {
		this(userName, password, smtpServer);
		this.smtpPort = smtpPort;
	}

	public JavaEmailSender(String userName, String password, String smtpServer, String smtpPort, boolean useSSL, boolean useSTARTTLS) {
		this(userName, password, smtpServer, smtpPort, useSSL);
		this.useSTARTTLS = useSTARTTLS;
	}

	public JavaEmailSender(String userName, String password, String smtpServer) {
		this.username = userName;
		this.password = password;
		this.smtpServer = smtpServer;
	}
	
	private void addAddresses(MimeMessage message, MailMessage mailMessage) throws MessagingException {
		List<String> toUsers = mailMessage.getToUsers();
		if (toUsers != null) { // 目标用户
			for (String user : toUsers) {
				message.addRecipients(Message.RecipientType.TO,user);
			}
		}
		List<String> ccUsers = mailMessage.getCcUsers();
		if (ccUsers != null) { // 操纵用户
			for (String user : ccUsers) {
				message.addRecipients(Message.RecipientType.CC,user);
			}
		}
		List<String> bccUsers = mailMessage.getBccUsers();
		if (bccUsers != null) { // 密送用户
			for (String user : bccUsers) {
				message.addRecipients(Message.RecipientType.BCC,user);
			}
		}
	}

	private void init() {
		if (initialized) { return;}
		synchronized (initLock) {
			if (initialized) {
				return;
			}

			contentConverterMap.put(FtlMailMessage.class,new FtlContentConverter());

			// 处理附件文件名不对的问题
			System.setProperty("mail.mime.splitlongparameters", "false");
			System.setProperty("mail.smtp.timeout", "25000");
			
			properties = new Properties();
			
			// 设置邮件服务器
			properties.setProperty("mail.smtp.host", this.smtpServer);
			properties.put("mail.smtp.port", this.smtpPort);
			properties.put("mail.smtp.socketFactory.port", this.smtpPort);
			properties.put("mail.smtp.auth", "true");
			properties.put("-Djava.net.preferIPv4Stack", "true");
			
			if (this.useSSL) {
				MailSSLSocketFactory sf;
				try {
					sf = new MailSSLSocketFactory();
				} catch (GeneralSecurityException e) {
					throw new MailSendException(e);
				}
				sf.setTrustAllHosts(true);
				properties.put("mail.smtp.ssl.enable", "true");
				properties.put("mail.smtp.ssl.socketFactory", sf);
			} else {
				properties.put("mail.smtp.ssl.enable", "false");
			}
			if (this.useSTARTTLS) {
				properties.put("mail.smtp.starttls.enable", "true");
			}

			initialized = true;
		}
	}
 	
	private void addAttach(Multipart multipart, MailMessage mailMessage) throws MessagingException, UnsupportedEncodingException {
		List<File> files = mailMessage.getFileAttachments();
		if (files != null) { // 文件附件
			for (File file : files) {
				// 附件部分
				BodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setDisposition("attachment");
				// 设置要发送附件的文件路径
				DataSource source = new FileDataSource(file);
				messageBodyPart.setDataHandler(new DataHandler(source));
				String fileName;
				if (file instanceof MailFile) { //
					fileName = ((MailFile) file).getFileName();
				} else {
					fileName = file.getName();
				}
				// 处理附件名称中文（附带文件路径）乱码问题
				String encodeText = MimeUtility.encodeText(fileName);
				messageBodyPart.setFileName(encodeText);
				multipart.addBodyPart(messageBodyPart);
			}
		}
	}

	private void setContent(Multipart multipart, MailMessage mailMessage) throws MessagingException {
		if (mailMessage == null) return;
		ContentConverter contentConverter = contentConverterMap.get(mailMessage.getClass());
		if (contentConverter != null) {
			contentConverter.setContent(multipart, mailMessage);
			return;
		}

		// 创建消息部分
		BodyPart messageBodyPart = new MimeBodyPart();

		if (mailMessage.getHtmlType()) {
			messageBodyPart.setContent(mailMessage.getContent(), "text/html;charset=utf-8");
		} else {
			messageBodyPart.setText(mailMessage.getContent());
		}

		// 设置文本消息部分
		multipart.addBodyPart(messageBodyPart);
	}


	public void sendMail(MailMessage mailMessage) {
		try {
			init();
			// 获取默认session对象
			Session session = Session.getDefaultInstance(this.properties, new Authenticator() {
				public PasswordAuthentication getPasswordAuthentication() { // qq邮箱服务器账户、第三方登录授权码
					return new PasswordAuthentication(JavaEmailSender.this.username, JavaEmailSender.this.password); // 发件人邮件用户名、密码
				}
			});
			
			// 创建默认的 MimeMessage 对象
			MimeMessage message = new MimeMessage(session);
			// Set From: 头部头字段
			message.setFrom(new InternetAddress(this.username, mailMessage.getFromName()));
			// Set Subject: 主题文字
			message.setSubject(mailMessage.getTitle());
			// 接收人
			addAddresses(message, mailMessage);

			// 创建多重消息
			Multipart multipart = new MimeMultipart();

			// 附件
			addAttach(multipart, mailMessage);

			// 文本消息部分
			setContent(multipart, mailMessage);

			// 发送完整消息
			message.setContent(multipart);

			message.setSentDate(new Date());
			
			// 发送消息
			Transport.send(message);
			
		} catch(Throwable e) {
			throw new MailSendException(e);
		}
	}
	
	public FileTypeMap getFileTypeMap() {
		return fileTypeMap;
	}

	public void setFileTypeMap(FileTypeMap fileTypeMap) {
		this.fileTypeMap = fileTypeMap;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}

	public String getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
	}

	public boolean getUseSSL() {
		return useSSL;
	}

	public void setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
	}

	public boolean getUseSTARTTLS() {
		return useSTARTTLS;
	}

	public void setUseSTARTTLS(boolean useSTARTTLS) {
		this.useSTARTTLS = useSTARTTLS;
	}

	public void addContentConverter(Class<? extends MailMessage> infoClz, ContentConverter converter) {
		this.contentConverterMap.put(infoClz, converter);
	}

	public ContentConverter removeContentConverter(Class<? extends MailMessage> infoClz) {
		return this.contentConverterMap.remove(infoClz);
	}

	public static void main(String[] args) {
//		try {
//			System.out.println(URLDecoder.decode("valid_user_address%40company.com%0D%0ABCC%3Ame%40me.com%0D%0A", "utf-8"));
//			System.out.println(URLDecoder.decode("valid_user_address@company.com%0D%0ABCC%3Ame%40me.com%0D%0A", "utf-8"));
//			System.out.println(URLDecoder.decode("From:sender@domain.com%0ACc:recipient@domain.com%0ABcc:recipient1@domain.com", "utf-8"));
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
		String userName = System.getProperty("mail.smtp.username");
		String password = System.getProperty("mail.smtp.password");
		String smtpServer = System.getProperty("mail.smtp.host");
		String smtpPort = System.getProperty("mail.smtp.port", "465");
		boolean useSSL = "true".equalsIgnoreCase(System.getProperty("mail.smtp.ssl"));
		boolean useSTARTTLS = "true".equalsIgnoreCase(System.getProperty("mail.smtp.starttls"));

		JavaEmailSender emailSender = new JavaEmailSender(userName, password, smtpServer, smtpPort, useSSL, useSTARTTLS);

		MailMessage mailMessage = new MailMessage();
		mailMessage.setTitle("测试邮件");
		mailMessage.setFromName("Test Mail User Name");

		List<String> test = new ArrayList<>();
		test.add("wangYang@uetty.com");
		mailMessage.setToUsers(test);
		List<String> cc = new ArrayList<>();
		cc.add("JinYuan@uEtty.com");
		mailMessage.setCcUsers(cc);

		List<File> attachments = new ArrayList<>();
		MailFile mailFile2 = new MailFile("E:\\data\\i1.png");
		mailFile2.setFileName("rename.png");
		attachments.add(mailFile2);
		mailMessage.setFileAttachments(attachments);

		mailMessage.setContent("test %0A%0Atest2");
		mailMessage.setHtmlType(false);

		emailSender.sendMail(mailMessage);
	}
}
