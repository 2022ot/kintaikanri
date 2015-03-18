package net.shinkasystems.kintai.mail;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Date;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * entry
 * approval
 * rejection
 * withdrawal
 * passback
 * 
 * @author Aogiri
 *
 */
public enum KintaiMail {

	/**
	 * 申請メールです。
	 */
	ENTRY("【勤怠管理ツール】勤怠の申請"),

	/**
	 * 承認メールです。
	 */
	APPROVAL("【勤怠管理ツール】勤怠の承認"),

	/**
	 * 却下メールです。
	 */
	REJECTION("【勤怠管理ツール】勤怠の却下"),

	/**
	 * 取り下げメールです。
	 */
	WITHDRAWAL("【勤怠管理ツール】勤怠の取り下げ");
	
	private static final String EMAIL_CHARSET = "UTF-8";

	/**
	 * 勤怠通知メールの件名です。
	 */
	private final String subject;

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(KintaiMail.class);

	/**
	 * 
	 * @param subject
	 */
	private KintaiMail(String subject) {
		this.subject = subject;
	}

	/**
	 * 
	 * @param argument
	 */
	public void send(KintaiMailArgument argument) {

		StringBuilder builder = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					getClass().getResourceAsStream(this.toString() + ".txt"), EMAIL_CHARSET));

			int c;
			while ((c = reader.read()) != -1) {
				builder.append((char) c);
			}

		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		final String text = MessageFormat.format(
				builder.toString(),
				argument.getReceiverName(),
				argument.getSenderName(),
				argument.getTerm(),
				argument.getForm(),
				argument.getComment());

		final Session session = Session.getInstance(KintaiMailPropery.PROPERTIES, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(
						KintaiMailPropery.USER.getValue(),
						KintaiMailPropery.PASSWORD.getValue());
			}
		});

		try {
			final MimeMessage message = new MimeMessage(session);
			message.setHeader("Content-Transfer-Encoding", "7bit");
			message.setRecipients(Message.RecipientType.TO,
					new InternetAddress[] { new InternetAddress(argument.getReceiverMailAddress()) });
			message.setRecipients(Message.RecipientType.CC,
					new InternetAddress[] { new InternetAddress(argument.getSenderMailAddress()) });
			message.setReplyTo(new InternetAddress[] { new InternetAddress(argument.getSenderMailAddress()) });
			message.setFrom(new InternetAddress(KintaiMailPropery.USER.getValue()));
			message.setSubject(subject, EMAIL_CHARSET);
			message.setSentDate(new Date());
			message.setContent(text, "text/plain;charset=" + EMAIL_CHARSET);

			Transport.send(message);

		} catch (MessagingException e) {

			log.error("メール送信中に例外が発生しました。" + e.getMessage());
			e.printStackTrace();
		}
	}

}
