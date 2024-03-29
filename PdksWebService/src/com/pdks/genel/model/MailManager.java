package com.pdks.genel.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.pdks.mail.model.MailFile;
import com.pdks.mail.model.MailObject;
import com.pdks.mail.model.MailPersonel;
import com.sun.mail.util.MailSSLSocketFactory;

/**
 * @author Hasan Sayar Uygulamadan gönderilecek mailleri oluþturur.
 */
public class MailManager implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8028850758479494268L;

	public static Logger logger = Logger.getLogger(MailManager.class);

	private static String oddRenk = "background-color: #ECF4FE;", evenRenk = "background-color: #D5E4FB;", headerRenk = "background-color: #EEE9D1;color: #000; font-size: 10px !important;";

	/**
	 * @param bodyHTML
	 * @return
	 * @throws Exception
	 */
	private static String getHmtlString(String bodyHTML) throws Exception {

		StringBuffer sb = new StringBuffer();
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<style>");
		sb.append(".odd {");
		sb.append(oddRenk);
		sb.append("} ");

		sb.append(".even {");
		sb.append(evenRenk);
		sb.append("} ");

		sb.append(".acik {");
		sb.append(oddRenk);
		sb.append("} ");

		sb.append(".koyu {");
		sb.append(evenRenk);
		sb.append("} ");

		sb.append(".true {");
		sb.append(oddRenk);
		sb.append("	white-space: nowrap;");
		sb.append("	width: autostretch;");
		sb.append("} ");

		sb.append(".false {");
		sb.append(evenRenk);
		sb.append("	white-space: nowrap;");
		sb.append("	width: autostretch;");
		sb.append("} ");

		sb.append("table.mars { ");
		sb.append(" width: 100%;");
		sb.append(" margin: 0;");
		sb.append(" padding: 0px;");
		sb.append(" font-size: 1em;");
		sb.append(" background-repeat: no-repeat;");
		sb.append(" list-style-type: none;");
		sb.append("} ");

		sb.append(".table.mars td { ");
		sb.append(" padding: 3px 4px 3px 4px;");
		sb.append("} ");

		sb.append("table.mars thead tr { ");
		sb.append(headerRenk);
		sb.append("} ");

		sb.append("table.mars tr.even { ");
		sb.append(evenRenk);
		sb.append("} ");

		sb.append("table.mars tr.odd { ");
		sb.append(oddRenk);
		sb.append("} ");

		sb.append("table.mars tr.selected { ");
		sb.append(" color: white;");
		sb.append(" background-color: #00ff00;");
		sb.append("} ");

		sb.append("table.mars tr.araToplam { ");
		sb.append(" background-color: #ffff00;");
		sb.append("} ");

		sb.append("table.mars tr.hata { ");
		sb.append(" background: #CC0033;");
		sb.append("} ");

		sb.append("table.mars tr.true { ");
		sb.append(oddRenk);
		sb.append("} ");

		sb.append("table.mars tr.false { ");
		sb.append(evenRenk);
		sb.append("} ");

		sb.append("table.mars th { ");
		sb.append(headerRenk);
		sb.append("} ");

		sb.append("table.mars th.sorted,th.sortable { ");
		sb.append(" background-color: orange;");
		sb.append("} ");

		sb.append("table.mars th a,th a:visited { ");
		sb.append(" color: black;");
		sb.append("} ");

		sb.append("table.mars th a:hover { ");
		sb.append(" text-decoration: underline;");
		sb.append(" color: black;");
		sb.append("} ");

		sb.append("table.mars th.sorted a,th.sortable a { ");
		sb.append(" background-position: right;");
		sb.append(" display: block;");
		sb.append(" width: 100%;");
		sb.append("} ");

		sb.append("</style>");
		sb.append("</head>");
		sb.append("<body>");
		sb.append(bodyHTML);
		sb.append("</body>");
		sb.append("</html>");

		String str = sb.toString();
		sb = null;
		return str;
	}

	/**
	 * @param parameterMap
	 * @throws Exception
	 */
	public static void ePostaGonder(HashMap<String, Object> parameterMap) throws Exception {
		Properties props = null;
		boolean uyariServisMailGonder = false;
		if (parameterMap.containsKey("uyariServisMailGonder")) {
			try {
				uyariServisMailGonder = ((String) parameterMap.get("uyariServisMailGonder")).equals("1");
			} catch (Exception e) {
			}
		}
		if (parameterMap.containsKey("mailObject")) {
			if (parameterMap.containsKey("oddTableRenk")) {
				String deger = (String) parameterMap.get("oddTableRenk");
				if (deger.length() > 3)
					MailManager.setOddRenk(deger);
			}
			if (parameterMap.containsKey("evenTableRenk")) {
				String deger = (String) parameterMap.get("evenTableRenk");
				if (deger.length() > 3)
					MailManager.setEvenRenk(deger);
			}
			if (parameterMap.containsKey("headerTableRenk")) {
				String deger = (String) parameterMap.get("headerTableRenk");
				if (deger.length() > 3)
					MailManager.setHeaderRenk(deger);
			}

			props = new Properties();
			MailObject mailObject = (MailObject) parameterMap.get("mailObject");
			String konu = mailObject.getSubject();
			String mailIcerik = mailObject.getBody(), mailAdresFROM = null, smtpTLSProtokol = null;
			if (konu != null && konu.indexOf("  ") >= 0)
				konu = PdksUtil.replaceAllManuel(konu, "  ", " ");
			if (mailIcerik != null && mailIcerik.indexOf("  ") >= 0)
				mailIcerik = PdksUtil.replaceAllManuel(mailIcerik, "  ", " ");
			List<File> dosyalar = new ArrayList<File>();
			int port = 25;
			String username = null, password = null, smtpHostIp = null;
			if (parameterMap.containsKey("smtpTLSProtokol"))
				smtpTLSProtokol = (String) parameterMap.get("smtpTLSProtokol");
			if (parameterMap.containsKey("smtpHost"))
				smtpHostIp = (String) parameterMap.get("smtpHost");
			if (parameterMap.containsKey("smtpHostPort"))
				port = Integer.parseInt((String) parameterMap.get("smtpHostPort"));
			if (parameterMap.containsKey("smtpUserName"))
				username = (String) parameterMap.get("smtpUserName");
			if (parameterMap.containsKey("smtpPassword"))
				password = (String) parameterMap.get("smtpPassword");
			if (username == null) {
				if (parameterMap.containsKey("fromAdres")) {
					mailAdresFROM = (String) parameterMap.get("fromAdres");
					username = mailAdresFROM;
				}
			} else
				mailAdresFROM = username;
			if (mailAdresFROM != null && parameterMap.containsKey("fromName"))
				mailAdresFROM = "\"" + parameterMap.get("fromName") + "\" <" + mailAdresFROM + ">";
			JavaMailSenderImpl sender = new JavaMailSenderImpl();
			sender.setDefaultEncoding("utf-8");
			sender.setHost(smtpHostIp);
			sender.setPort(port);
			if (username != null)
				sender.setUsername(username);
			if (password != null)
				sender.setPassword(password);

			boolean smtpTLSDurum = false, smtpSSLDurum = false, smtpServerDebug = false;
			if (parameterMap.containsKey("smtpServerDebug"))
				smtpServerDebug = ((String) parameterMap.get("smtpServerDebug")).equals("1");
			if (parameterMap.containsKey("smtpTLSDurum"))
				smtpTLSDurum = ((String) parameterMap.get("smtpTLSDurum")).equals("1");
			if (parameterMap.containsKey("smtpSSLDurum"))
				smtpSSLDurum = ((String) parameterMap.get("smtpSSLDurum")).equals("1");
			props.setProperty("mail.smtp.host", smtpHostIp);
			props.setProperty("mail.smtp.port", String.valueOf(port));
			props.setProperty("mail.smtp.user", username);
			// properties.put("mail.smtp.starttls.enable", port != 465);
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", String.valueOf(smtpTLSDurum));
			props.setProperty("mail.transport.protocol", "smtp");
			props.setProperty("mail.debug", "false");
			if (!smtpTLSDurum)
				props.setProperty("mail.smtp.socketFactory.port", String.valueOf(port));
			else {
				if (smtpTLSProtokol != null) {
					props.put("mail.smtp.ssl.protocols", smtpTLSProtokol);

				}
				// props.setProperty("mail.smtp.socketFactory.port", String.valueOf(port));
				// props.put("mail.smtp.ssl.trust", smtpHostIp);
			}
			if (port != 25 && smtpSSLDurum) {
				props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				props.setProperty("mail.smtp.socketFactory.fallback", "false");
				MailSSLSocketFactory sf = new MailSSLSocketFactory();
				sf.setTrustAllHosts(true);
				props.put("mail.imap.ssl.trust", "*");
				props.put("mail.imap.ssl.socketFactory", sf);
			}

			javax.mail.Session session = null;
			try {
				if (smtpSSLDurum) {
					if (username != null && password != null)
						session = javax.mail.Session.getDefaultInstance(props, new GMailAuthenticator(username, password));
					else
						session = javax.mail.Session.getInstance(props);
				}
			} catch (Exception e) {

			}
			if (session == null)
				session = javax.mail.Session.getInstance(props, new GMailAuthenticator(username, password));
			session.setDebug(smtpServerDebug);
			Transport transport = session.getTransport("smtp");
			transport.connect(smtpHostIp, username, password);
			MimeMessage message = new MimeMessage(session);
			message.setSubject(konu);
			InternetAddress from = new InternetAddress();
			from.setAddress(username);
			if (parameterMap.containsKey("fromAdres"))
				from.setAddress((String) parameterMap.get("fromAdres"));
			if (parameterMap.containsKey("fromName"))
				from.setPersonal((String) parameterMap.get("fromName"), "UTF-8");
			message.setFrom(from);

			Multipart mp = new MimeMultipart();
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(getHmtlString(mailIcerik), "text/html; charset=utf-8");
			// messageBodyPart.setText(mailIcerik);
			mp.addBodyPart(messageBodyPart);
			message.setContent(mp);
			List<String> mailList = new ArrayList<String>();
			if (uyariServisMailGonder) {
				message.setRecipients(Message.RecipientType.TO, adresleriDuzenle(mailObject.getToList(), mailList));
				message.setRecipients(Message.RecipientType.CC, adresleriDuzenle(mailObject.getCcList(), mailList));
			}
			message.setRecipients(Message.RecipientType.BCC, adresleriDuzenle(mailObject.getBccList(), mailList));

			for (MailFile mailFile : mailObject.getAttachmentFiles()) {
				DataSource fds = null;
				File file = null;
				String fileName = "/tmp/" + (mailFile.getFileName() != null ? mailFile.getFileName() : mailFile.getDisplayName());
				if (mailFile.getFile() != null)
					file = (File) mailFile.getFile();
				else if (mailFile.getIcerik() != null) {
					// String icerikStr = new String(mailFile.getIcerik());
					// file = PdksUtil.getFileByInputStream(new ByteArrayInputStream(icerikStr.getBytes(StandardCharsets.UTF_8)), fileName);
					file = PdksUtil.getFileByInputStream(new ByteArrayInputStream(mailFile.getIcerik()), fileName);
				}
				if (file != null && file.exists()) {
					fds = new FileDataSource(fileName);
					MimeBodyPart attachFilePart = new MimeBodyPart();
					attachFilePart.setDataHandler(new DataHandler(fds));
					String dosyaAdi = MimeUtility.encodeText(mailFile.getDisplayName(), "UTF-8", null);
					// String dosyaAdi = PdksUtil.setTurkishStr(mailFile.getDisplayName());
					attachFilePart.setFileName(dosyaAdi);
					mp.addBodyPart(attachFilePart);
					dosyalar.add(file);
				}
			}
			Exception hata = null;
			try {
				if (!mailList.isEmpty())
					Transport.send(message);
			} catch (Exception e) {
				hata = e;
				try {
					if (e instanceof javax.mail.SendFailedException) {
						javax.mail.SendFailedException se = (javax.mail.SendFailedException) e;
						if (se.getInvalidAddresses() != null) {
							javax.mail.Address[] address = se.getInvalidAddresses();
							for (int i = 0; i < address.length; i++) {
								InternetAddress iad = (InternetAddress) address[i];
								logger.error(konu + " " + iad.getAddress());
							}
							hata = null;
						}

					}
				} catch (Exception e2) {
				}

			}
			mailList = null;

			for (File file : dosyalar) {
				if (file.exists())
					file.delete();
			}
			if (hata != null) {
				logger.error(konu + " " + hata);
				throw hata;
			}

		}
	}

	/**
	 * @param mailAdresleri
	 * @param mailList
	 * @return
	 * @throws Exception
	 */
	private static InternetAddress[] adresleriDuzenle(List<MailPersonel> mailAdresleri, List mailList) throws Exception {
		InternetAddress[] adresler = null;
		if (mailAdresleri != null && !mailAdresleri.isEmpty()) {
			List adreslerList = new ArrayList();
			for (MailPersonel mailUser : mailAdresleri) {
				String email = mailUser.getePosta();
				if (email.indexOf("@") > 0 && !mailList.contains(email)) {
					try {
						InternetAddress ia = new InternetAddress(email);
						if (mailUser.getAdiSoyadi() != null && mailUser.getAdiSoyadi().trim().length() > 0)
							ia.setPersonal(mailUser.getAdiSoyadi(), "UTF-8");
						adreslerList.add(ia);
						mailList.add(email);
					} catch (Exception e) {
					}

				}
			}
			if (!adreslerList.isEmpty()) {
				adresler = new InternetAddress[adreslerList.size()];
				for (int i = 0; i < adresler.length; i++)
					adresler[i] = (InternetAddress) adreslerList.get(i);
			}
			adreslerList = null;
		}
		return adresler;
	}

	public static String getOddRenk() {
		return oddRenk;
	}

	public static void setOddRenk(String oddRenk) {
		MailManager.oddRenk = oddRenk;
	}

	public static String getEvenRenk() {
		return evenRenk;
	}

	public static void setEvenRenk(String evenRenk) {
		MailManager.evenRenk = evenRenk;
	}

	public static String getHeaderRenk() {
		return headerRenk;
	}

	public static void setHeaderRenk(String headerRenk) {
		MailManager.headerRenk = headerRenk;
	}

}