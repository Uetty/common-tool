package com.uetty.common.tool.core.email;

import com.google.gson.Gson;
import com.uetty.common.tool.core.email.JavaEmailSender.Attachment;
import com.uetty.common.tool.core.email.JavaEmailSender.MailInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 邮件发送工具
 * 
 * @author vince
 */
public class JavaEmailUtil {

	private static Logger logger = LoggerFactory.getLogger(JavaEmailUtil.class);

	private static final String SMTP_PORT = "25";
	private static final String SMTP_SERVER = "mail.star-net.cn";
	private static final String SMTP_USERNAME = "fangwencheng@star-net.cn";
	private static final String SMTP_PASSWORD = "starnet";
	private static final boolean USE_SSL = false;
	
	private static JavaEmailSender emailSender = new JavaEmailSender(SMTP_USERNAME, SMTP_PASSWORD, SMTP_SERVER, SMTP_PORT, USE_SSL);

	
	/**
	 * 发送邮件
	 * @param title 标题
	 * @param fromName 显示的发送者名称
	 */
	public static void sendEmailWithFile(String title, String fromName, String content, List<String> toAddrs,
			List<File> files) {
		ResendableEmail remail = ResendableEmail.newInstanceOfFile(title, fromName, content, toAddrs, files);
		EmailService.sendEmail(remail);
	}
	
	public static void sendEmailWithAtta(String title, String fromName, String content, List<String> toAddrs,
			List<Attachment> attas) {
		ResendableEmail remail = ResendableEmail.newInstanceOfAtta(title, fromName, content, toAddrs, attas);
		EmailService.sendEmail(remail);
	}
	
	public static void sendEmail(String title, String fromName, String content, List<String> toAddrs) {
		ResendableEmail remail = ResendableEmail.newInstance(title, fromName, content, toAddrs);
		EmailService.sendEmail(remail);
	}
	
	/**
	 * 可有限次数重发的邮件
	 */
	public static class ResendableEmail extends MailInfo {
		public static Long lastSendTime;// 用于出现发送失败时开启发送时间间隔限制
		public static long SEND_INTERVAL = 1000 * 60 * 1;// 一分钟后重发

		int resendCount = 0;
		private ResendableEmail() {}
		
		public static ResendableEmail newInstance(String title, String fromName, String content, List<String> toAddrs) {
			// 去除重复地址
			ResendableEmail email = new ResendableEmail();
			removeDuplicateAddress(toAddrs);
			email.setTitle(title);
			email.setFromName(fromName);
			email.setContent(content);
			email.setAddrs(toAddrs);
			return email;
		}
		
		public static ResendableEmail newInstanceOfFile(String title, String fromName, String content, List<String> toAddrs,
				List<File> files) {
			// 去除重复地址
			ResendableEmail email = new ResendableEmail();
			removeDuplicateAddress(toAddrs);
			email.setTitle(title);
			email.setFromName(fromName);
			email.setContent(content);
			email.setAddrs(toAddrs);
			email.setFiles(files);
			return email;
		}
		
		public static ResendableEmail newInstanceOfAtta(String title, String fromName, String content, List<String> toAddrs,
				List<Attachment> attas) {
			// 去除重复地址
			ResendableEmail email = new ResendableEmail();
			removeDuplicateAddress(toAddrs);
			email.setTitle(title);
			email.setFromName(fromName);
			email.setContent(content);
			email.setAddrs(toAddrs);
			email.setAttas(attas);
			return email;
		}
		
		/**
		 * 是否准备好可以发送
		 */
		public boolean ready() {
			synchronized (ResendableEmail.class) {
				if (lastSendTime != null && lastSendTime + SEND_INTERVAL > System.currentTimeMillis()) {
					// 上次发送存在发送失败的情况，开启了发送间隔限制，重发时进行时间间隔限制
					return false;
				}
				return true;
			}
		}

		private static void removeDuplicateAddress(List<String> addrs) {
			Set<String> set = new HashSet<String>();
			set.addAll(addrs);
			addrs.clear();
			addrs.addAll(set);
		}

		public void send() throws MessagingException, Exception {
			if (getAddrs().size() == 0) {
				return;
			}
			logger.debug("send email to reciver ---> " + new Gson().toJson(getAddrs()));
			
			emailSender.sendMail(this);
			synchronized (ResendableEmail.class) {
				ResendableEmail.lastSendTime = null;// 发送成功了，解除发送间隔限制
			}
		}

		public boolean rebuildEmailIfCanResend(Throwable e) {
			if (this.resendCount > 3) {// 三次重发机会
				return false;
			}
			this.resendCount++;
			synchronized (ResendableEmail.class) {
				// 发送失败时，有可能是因为频率被邮件服务器限制了，开启发送间隔限制
				ResendableEmail.lastSendTime = System.currentTimeMillis();
			}
			removeInvalidAddress(e);
			return getAddrs().size() > 0;
		}

		/**
		 * 移除导致发送失败的非法邮件地址
		 */
		private void removeInvalidAddress(Throwable e) {
			if (e == null) return;
			if (e instanceof SendFailedException) {
				SendFailedException sae = (SendFailedException) e;
				Address[] invalidAddrs = sae.getInvalidAddresses();
				if (invalidAddrs == null) {
					return;
				}
				for (Address addr : invalidAddrs) {
					if (addr instanceof InternetAddress) {
						logger.debug("invalid address ==> " + addr);
						getAddrs().remove(((InternetAddress) addr).getAddress());
					}
				}
				return;
			}

			if (e instanceof MessagingException) {
				Exception mes = ((MessagingException) e).getNextException();
				removeInvalidAddress(mes);
			}
			
			Throwable cause = e.getCause();
			removeInvalidAddress(cause);
			
			Throwable[] suppressed = e.getSuppressed();
			for (Throwable se : suppressed) {
				removeInvalidAddress(se);
			}
		}
	}

	/**
	 * 邮件服务线程，实现线程数量控制
	 */
	public static class EmailService extends Thread {

		private static EmailService emailService = null;

		List<ResendableEmail> list = new ArrayList<ResendableEmail>();

		private EmailService() {
		}

		// 提取新建线程的方法，提高扇入
		private static void installService() {
			// 保证不会同时存在两条线程节省资源开销
			if (emailService == null) {// 判断线程是否存在
				// 重建新线程
				emailService = new EmailService();
				logger.debug("Intent to start email thread");
				emailService.start();
			}
		}

		/**
		 * 添加等待发送的邮件资源
		 */
		public static void sendEmail(ResendableEmail email) {
			synchronized (EmailService.class) {
				installService();
				emailService.list.add(email);// 加锁状态下添加元素，保持资源一致性
				logger.debug("task list add a email");
			}
		}

		/**
		 * 添加邮件资源
		 */
		public static void sendEmails(List<ResendableEmail> emails) {
			synchronized (EmailService.class) {
				installService();
				emailService.list.addAll(emails);// 加锁状态下添加元素，保持资源一致性
			}
		}

		/**
		 * 线程逻辑
		 */
		@Override
		public void run() {
			while (true) {
				ResendableEmail remail = null;
				try {
					synchronized (EmailService.class) {
						if (list.size() <= 0) {// 判断是否还有任务
							emailService = null;
							break;
						}
						remail = list.remove(0);// 加锁状态下取出元素，保持资源一致性

						if (!remail.ready()) {
							// 上次发送存在发送失败的情况，开启了发送间隔限制，重发时进行时间间隔限制
							list.add(0, remail);
							remail = null;
						}
					}
					if (remail != null) {
						logger.debug("send email");
						remail.send();
					}

					try {
						long sleepTime = remail != null ? 1000l : 30000l;
						Thread.sleep(sleepTime);
					} catch (Exception e) {
					}

				} catch (Exception e) {
					handException(e, remail);
				}
			}
		}

		private void handException(Exception e, ResendableEmail remail) {
			if (remail == null)
				return;
			logger.error("", e);
			if (remail != null && remail.rebuildEmailIfCanResend(e)) {// 三次重发机会
				synchronized (EmailService.class) {
					list.add(remail);
				}
			}
		}

	}

}
