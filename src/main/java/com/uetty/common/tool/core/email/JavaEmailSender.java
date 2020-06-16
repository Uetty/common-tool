package com.uetty.common.tool.core.email;

import com.sun.mail.util.MailSSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Properties;

/**
 * 不使用spring库
 * @author vince
 */
@SuppressWarnings("unused")
public class JavaEmailSender {

	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(JavaEmailSender.class);
	
	private Properties properties;
	
	private FileTypeMap fileTypeMap = new MimetypesFileTypeMap();
	
	private volatile boolean initialized = false;
	
	private final Object initLock = new Object();
	
	private String username;

	private String password;

	private String smtpServer;

	private String smtpPort;

	/**
	 * 需要SSL加密的地址
	 * <p>类似于HTTPS
	 */
	private boolean useSSL = false;
	/**
	 * 使用STARTTLS
	 */
	private boolean useSTARTTLS = false;
	
	public JavaEmailSender(String userName, String password, String smtpServer, String smtpPort, boolean useSSL) {
		this.username = userName;
		this.password = password;
		this.smtpServer = smtpServer;
		this.smtpPort = smtpPort;
		this.useSSL = useSSL;
	}

	public JavaEmailSender(String userName, String password, String smtpServer, String smtpPort) {
		this.username = userName;
		this.password = password;
		this.smtpServer = smtpServer;
		this.smtpPort = smtpPort;
	}
	
	private void addAddresses(MimeMessage message, MailInfo mailInfo) throws MessagingException {
		List<String> tos = mailInfo.getAddrs();
		for (String receiver : tos) {
			// Set To: 头部头字段
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
		}
	}
	
	private DataSource createDataSource(final byte[] bytes, final String name) {
		return new DataSource() {
			public InputStream getInputStream() {
				return new ByteArrayInputStream(bytes);
			}
			public OutputStream getOutputStream() {
				throw new UnsupportedOperationException("Read-only javax.activation.DataSource");
			}
			public String getContentType() {
				return fileTypeMap.getContentType(name);
			}
			public String getName() {
				return name;
			}
		};
	}
	
	private void init() {
		if (initialized) { return;}
		synchronized (initLock) {
			if (initialized) {
				return;
			}
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
			if (useSTARTTLS) {
				properties.put("mail.smtp.starttls.enable", "true");
			}
			initialized = true;
		}
	}
 	
	private void addAttach(Multipart multipart, MailInfo mailInfo) throws MessagingException, UnsupportedEncodingException {
		List<File> files = mailInfo.getFiles();
		if (files != null) {
			for (File file : files) {
				// 附件部分
				BodyPart messageBodyPart = new MimeBodyPart();
				// 设置要发送附件的文件路径
				DataSource source = new FileDataSource(file);
				messageBodyPart.setDataHandler(new DataHandler(source));
				// messageBodyPart.setFileName(filename);
				// 处理附件名称中文（附带文件路径）乱码问题
				String encodeText = MimeUtility.encodeText(file.getName());
				messageBodyPart.setFileName(encodeText);
				multipart.addBodyPart(messageBodyPart);
			}
		}
		List<Attachment> attas = mailInfo.getAttas();
		if (attas != null) {
			for (Attachment atta : attas) {
				// 附件部分
				BodyPart messageBodyPart = new MimeBodyPart();
				DataSource source = createDataSource(atta.getBytes(), atta.getName());
				messageBodyPart.setDataHandler(new DataHandler(source));
				// messageBodyPart.setFileName(filename);
				// 处理附件名称中文（附带文件路径）乱码问题
				messageBodyPart.setFileName(MimeUtility.encodeText(atta.getName()));
				multipart.addBodyPart(messageBodyPart);
			}
		}
	}
	
	
	public void sendMail(MailInfo mailInfo) {
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
			message.setFrom(new InternetAddress(this.username, mailInfo.getFromName()));
			// Set Subject: 主题文字
			message.setSubject(mailInfo.getTitle());
			// 接收人
			addAddresses(message, mailInfo);
			// 创建多重消息
			Multipart multipart = new MimeMultipart();
			// 创建消息部分
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(mailInfo.getContent());
			// 设置文本消息部分
			multipart.addBodyPart(messageBodyPart);

			addAttach(multipart, mailInfo);

			// 发送完整消息
			message.setContent(multipart);

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

	public boolean isUseSSL() {
		return useSSL;
	}

	public void setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
	}

	public boolean isUseSTARTTLS() {
		return useSTARTTLS;
	}

	public void setUseSTARTTLS(boolean useSTARTTLS) {
		this.useSTARTTLS = useSTARTTLS;
	}

	public static class MailSendException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public MailSendException(Throwable cause) {
			super(cause);
		}
		
		public MailSendException(String message) {
			super(message);
		}
	}
	
	public static class MailInfo {
		private String fromName;
		private String title;
		private List<String> addrs;
		private String content;
		private List<File> files;
		private List<Attachment> attas;
		public String getFromName() {
			return fromName;
		}
		public void setFromName(String fromName) {
			this.fromName = fromName;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public List<String> getAddrs() {
			return addrs;
		}
		public void setAddrs(List<String> addrs) {
			this.addrs = addrs;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public List<File> getFiles() {
			return files;
		}
		public void setFiles(List<File> files) {
			this.files = files;
		}
		public List<Attachment> getAttas() {
			return attas;
		}
		public void setAttas(List<Attachment> attas) {
			this.attas = attas;
		}
	}
	
	public static class Attachment {
		private String name;
		private byte[] bytes;
		public Attachment(String name, byte[] bytes) {
			this.name = name;
			this.bytes = bytes;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public byte[] getBytes() {
			return bytes;
		}
		public void setBytes(byte[] bytes) {
			this.bytes = bytes;
		}
	}

}
