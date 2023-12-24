package com.pdks.mail.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.pdks.entity.ServiceData;
import org.pdks.security.entity.User;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.google.gson.Gson;
import com.pdks.webservice.MailFile;
import com.pdks.webservice.MailObject;
import com.pdks.webservice.MailPersonel;
import com.pdks.webservice.MailStatu;
import com.sun.mail.util.MailSSLSocketFactory;

/**
 * @author Hasan Sayar Uygulamadan gönderilecek mailleri oluþturur.
 */

@Name("mailManager")
public class MailManager implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8028850758479494268L;

	public static Logger logger = Logger.getLogger(MailManager.class);

	@In(required = false, create = true)
	PdksEntityController pdksEntityController;

	@In(required = false, create = true)
	User authenticatedUser;

	@In(required = false, create = true)
	HashMap<String, String> parameterMap;

	private static String oddRenk = "background-color: #ECF4FE;", evenRenk = "background-color: #D5E4FB;", headerRenk = "background-color: #EEE9D1;color: #000; font-size: 10px !important;";

	/**
	 * @param bodyHTML
	 * @return
	 * @throws Exception
	 */
	private String getHmtlString(String bodyHTML) throws Exception {

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
	 * @param mailMap
	 * @return
	 * @throws Exception
	 */
	public MailStatu mailleriDuzenle(MailObject mailObject, Session session) throws Exception {
		MailStatu mailStatu = new MailStatu();
		String subject = mailObject.getSubject() != null ? PdksUtil.setTurkishStr(mailObject.getSubject()) : null;
		if (subject != null)
			logger.debug(subject + " in " + PdksUtil.getCurrentTimeStampStr());
		StringBuffer sb = new StringBuffer();
		if (!PdksUtil.hasStringValue(mailObject.getSmtpUser()))
			sb.append("Mail user belirtiniz!");
		if (!PdksUtil.hasStringValue(mailObject.getSmtpPassword()))
			sb.append("Mail şifre belirtiniz!");
		if (!PdksUtil.hasStringValue(mailObject.getSubject()))
			sb.append("Konu belirtiniz!");
		if (sb.length() > 0)
			mailStatu.setHataMesai(sb.toString());
		else {

			StringBuffer pasifPersonelSB = new StringBuffer();
			String smtpUserName = parameterMap.containsKey("smtpUserName") ? (String) parameterMap.get("smtpUserName") : "";
			String smtpPassword = parameterMap.containsKey("smtpPassword") ? (String) parameterMap.get("smtpPassword") : "";
			if (mailObject.getSmtpUser().equals(smtpUserName) && mailObject.getSmtpPassword().equals(smtpPassword)) {
				mailAdresKontrol(mailObject, pasifPersonelSB, session);
				String body = mailObject.getBody();
				if (mailObject.getToList().size() == 1) {
					MailPersonel mailPersonel = mailObject.getToList().get(0);
					if (!body.contains(mailPersonel.getAdiSoyadi()) && body.indexOf("Sayın ") < 0) {
						body = "<P>Sayın " + mailPersonel.getAdiSoyadi() + ",</P>" + body;
						mailObject.setBody(body);
					}
				}
				if (!body.contains("Saygılarımla")) {
					body = body + "<P>Saygılarımla</P>";
					mailObject.setBody(body);
				}
				if (mailObject.getBccList().size() + mailObject.getCcList().size() + mailObject.getToList().size() > 0) {
					mailStatu.setDurum(Boolean.TRUE);
					mailStatu.setHataMesai(pasifPersonelSB.toString());
				} else {
					mailStatu.setHataMesai("Adres giriniz!");
				}

			} else {
				mailStatu.setHataMesai("Smtp bilgileri hatalıdır!");
			}
			pasifPersonelSB = null;
		}
		sb = null;

		if (subject != null)
			logger.debug(subject + " out " + PdksUtil.getCurrentTimeStampStr());
		return mailStatu;

	}

	/**
	 * @param mailObject
	 * @return
	 * @throws Exception
	 */
	public MailStatu ePostaGonder(MailObject mailObject, Session sessionDB) throws Exception {
		MailStatu mailStatu = new MailStatu();
		Properties props = null;

		try {
			if (mailObject != null) {
				props = new Properties();
				String konu = mailObject.getSubject();
				String mailIcerik = mailObject.getBody(), mailAdresFROM = null;
				if (konu != null && konu.indexOf("  ") >= 0)
					konu = PdksUtil.replaceAllManuel(konu, "  ", " ");
				if (mailIcerik != null && mailIcerik.indexOf("  ") >= 0)
					mailIcerik = PdksUtil.replaceAllManuel(mailIcerik, "  ", " ");
				List<File> dosyalar = new ArrayList<File>();
				int port = 25;
				String username = mailObject.getSmtpUser(), password = mailObject.getSmtpPassword(), smtpHostIp = null, smtpTLSProtokol = null;
				if (parameterMap.containsKey("smtpTLSProtokol"))
					smtpTLSProtokol = (String) parameterMap.get("smtpTLSProtokol");
				if (parameterMap.containsKey("smtpHost"))
					smtpHostIp = (String) parameterMap.get("smtpHost");
				if (parameterMap.containsKey("smtpHostPort"))
					port = Integer.parseInt((String) parameterMap.get("smtpHostPort"));
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
				if (username != null) {
					props.setProperty("mail.smtp.user", username);

					props.put("mail.smtp.auth", "true");
				}
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
				} catch (Exception ee) {

				}
				if (session == null)
					if (username != null && password != null)
						session = javax.mail.Session.getInstance(props, new GMailAuthenticator(username, password));
				if (session != null)
					session.setDebug(smtpServerDebug);
				MimeMessage message = new MimeMessage(session);
				List<String> mailList = new ArrayList<String>();
				message.setRecipients(Message.RecipientType.TO, adresleriDuzenle(mailObject.getToList(), mailList));
				message.setRecipients(Message.RecipientType.CC, adresleriDuzenle(mailObject.getCcList(), mailList));
				message.setRecipients(Message.RecipientType.BCC, adresleriDuzenle(mailObject.getBccList(), mailList));
				if (!mailList.isEmpty()) {
					Transport transport = session.getTransport("smtp");
					transport.connect(smtpHostIp, username, password);
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
					message.setSubject(konu, "UTF-8");
					message.setContent(mp);
					for (MailFile mailFile : mailObject.getAttachmentFiles()) {
						DataSource fds = null;
						File file = null;
						String fileName = "/tmp/" + (mailFile.getFileName() != null ? mailFile.getFileName() : mailFile.getDisplayName());
						if (mailFile.getFile() != null)
							file = (File) mailFile.getFile();
						else if (mailFile.getIcerik() != null) {
							// String icerikStr = new String(mailFile.getIcerik());
							// file = getFileByInputStream(new ByteArrayInputStream(icerikStr.getBytes(StandardCharsets.UTF_8)), fileName);
							file = getFileByInputStream(new ByteArrayInputStream(mailFile.getIcerik()), fileName);
						}
						if (file != null && file.exists()) {
							fds = new FileDataSource(fileName);
							MimeBodyPart attachFilePart = new MimeBodyPart();
							attachFilePart.setDescription(mailFile.getDisplayName(), "UTF-8");
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
						String mesajAlan = null;
						if (mailObject.getToList() != null && mailObject.getToList().size() == 1) {
							MailPersonel mailPersonel = mailObject.getToList().get(0);
							mesajAlan = mailPersonel.getAdiSoyadi();
							if (mesajAlan != null && PdksUtil.hasStringValue(mesajAlan) == false)
								mesajAlan = null;

						}
						String aciklama = ((authenticatedUser != null ? authenticatedUser.getAdSoyad() + " " : "") + "\"" + konu + "\" konulu mail " + (mesajAlan != null ? mesajAlan + " " : "")).trim();
						logger.info(aciklama + " gönderiliyor.");
						Transport.send(message);
						mailStatu.setDurum(true);
						mailStatu.setHataMesai("");
						logger.info(aciklama + " gönderildi.");
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
					saveLog(mailObject, parameterMap, sessionDB);
					for (File file : dosyalar) {
						if (file.exists())
							file.delete();
					}
					if (hata != null) {
						logger.error(konu + " " + hata);
						throw hata;
					}
				} else
					mailStatu.setHataMesai("Mail gönderilecek e-posta yok!");
				mailList = null;

			}
		} catch (Exception e) {
			Gson gson = new Gson();
			logger.error(e + "\n" + gson.toJson(props));
			e.printStackTrace();
			if (e.toString() != null)
				mailStatu.setHataMesai(PdksUtil.replaceAll(e.toString(), "\n", ""));
		}
		if (mailStatu.isDurum() == false && mailStatu.getHataMesai() == null)
			mailStatu.setHataMesai("Hata oluştu!!");
		return mailStatu;
	}

	/**
	 * @param jsonMailStrings
	 * @param object
	 * @param gson
	 * @return
	 */
	private String getJsonObject(String jsonMailStrings, Object object, Gson gson) {
		String str = null;
		if (object != null) {
			if (gson == null)
				gson = new Gson();
			str = PdksUtil.toPrettyFormat(gson.toJson(object));
			HashMap<String, String> map = new HashMap<String, String>();
			if (PdksUtil.hasStringValue(jsonMailStrings)) {
				List<String> list = PdksUtil.getListByString(jsonMailStrings, "|");
				for (String string : list) {
					if (string.indexOf("_") > 0 && string.length() > 2) {
						String[] veri = string.split("_");
						if (veri.length == 2) {
							map.put(veri[0], veri[1]);
						} else if (veri.length == 1)
							map.put(veri[0], " ");
					}
				}
				list = null;
			} else {
				map.put("\\u003c", "<");
				map.put("\\u003e", ">");
				map.put("\\u0026", "&");
				map.put("\\u003d", "=");
				map.put("\\u0027", "'");
			}

			for (String pattern : map.keySet()) {
				if (str.indexOf(pattern) > 0)
					str = PdksUtil.replaceAllManuel(str, pattern, map.get(pattern));
			}
		}
		return str;
	}

	/**
	 * @param mailObject
	 * @param sessionDB
	 */
	@Transactional
	private void saveLog(MailObject mail, HashMap<String, String> map, Session sessionDB) {
		try {
			if (sessionDB != null) {
				MailObject mailObject = (MailObject) mail.clone();
				mailObject.setSmtpPassword("");
				ServiceData serviceData = new ServiceData("ePostaGonder");
				Gson gson = new Gson();
				serviceData.setInputData(mailObject.getSubject());
				if (pdksEntityController != null) {
					try {
						List<MailFile> attachmentFiles = new ArrayList<MailFile>();
						for (MailFile mailFile : mailObject.getAttachmentFiles()) {
							if (mailFile.getIcerik() != null && mailFile.getDisplayName() != null && mailFile.getDisplayName().indexOf(".") > 0) {
								String ext = FilenameUtils.getExtension(mailFile.getDisplayName());
								if (ext != null) {
									MailFile mailFileNew = new MailFile();
									mailFileNew.setDisplayName(mailFile.getDisplayName());
									if (ext.equalsIgnoreCase("txt") || ext.equalsIgnoreCase("xml"))
										mailFileNew.setFile(new String(mailFile.getIcerik()));
									attachmentFiles.add(mailFileNew);
									continue;
								}
							}
							attachmentFiles.add(mailFile);
						}
						if (!attachmentFiles.isEmpty()) {
							mailObject.getAttachmentFiles().clear();
							mailObject.getAttachmentFiles().addAll(attachmentFiles);
						}
						attachmentFiles = null;
						String jsonMailStrings = map.containsKey("jsonMailStrings") ? map.get("jsonMailStrings") : null;
						serviceData.setOutputData(getJsonObject(jsonMailStrings, mailObject, gson));
						pdksEntityController.save(serviceData, sessionDB);
					} catch (Exception ex) {

					}
				} else
					sessionDB.flush();
				gson = null;
			}

		} catch (Exception e) {
			logger.error(e);
		}

	}

	/**
	 * @param initialStream
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	private File getFileByInputStream(InputStream initialStream, String fileName) throws IOException {
		File targetFile = new File(fileName);
		FileUtils.copyInputStreamToFile(initialStream, targetFile);
		return targetFile;
	}

	/**
	 * @param mailObject
	 * @param pasifPersonelSB
	 * @param session
	 * @throws Exception
	 */
	private void mailAdresKontrol(MailObject mailObject, StringBuffer pasifPersonelSB, Session session) throws Exception {
		if (parameterMap.containsKey("bccAdres")) {
			String bccAdres = (String) parameterMap.get("bccAdres");
			if (bccAdres.indexOf("@") > 1) {
				List<String> list = PdksUtil.getListByString(bccAdres, null);
				for (String email : list) {
					if (email.indexOf("@") > 1 && PdksUtil.isValidEmail(email)) {
						MailPersonel mailPersonel = new MailPersonel();
						mailPersonel.setEPosta(email);
						mailObject.getBccList().add(mailPersonel);
					}

				}
			}
		}
		HashMap<String, MailPersonel> mailDataMap = new HashMap<String, MailPersonel>();
		if (parameterMap != null) {
			mailListKontrol(mailObject.getToList(), mailDataMap);
			mailListKontrol(mailObject.getCcList(), mailDataMap);
			mailListKontrol(mailObject.getBccList(), mailDataMap);
		}
		if (!mailDataMap.isEmpty()) {
			List<String> list = new ArrayList<String>();
			for (String string : mailDataMap.keySet()) {
				if (mailDataMap.size() > 1)
					list.add("'" + string + "'");
				else
					list.add(string);
			}
			HashMap map = new HashMap();
			map.put("email", list.size() > 1 ? list : list.get(0));
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			TreeMap<String, User> userMap = new TreeMap<String, User>();
			List<User> userList = pdksEntityController.getObjectByInnerObjectList(map, User.class);
			List<String> pasifList = new ArrayList<String>();
			for (User user : userList) {
				String mailStr = user.getEmail();
				if (user.isDurum() && user.getPdksPersonel().isCalisiyor())
					userMap.put(mailStr, user);
				else if (!pasifList.contains(mailStr))
					pasifList.add(mailStr);
			}
			if (!userMap.isEmpty()) {
				mailUserListKontrol(mailObject.getToList(), userMap);
				mailUserListKontrol(mailObject.getCcList(), userMap);
				mailUserListKontrol(mailObject.getBccList(), userMap);
			}
			if (!pasifList.isEmpty()) {
				for (Iterator iterator = pasifList.iterator(); iterator.hasNext();) {
					String string = (String) iterator.next();
					if (userMap.containsKey(string))
						iterator.remove();
				}
				if (!pasifList.isEmpty()) {
					pasifListKontrol(mailObject.getToList(), pasifList, pasifPersonelSB);
					pasifListKontrol(mailObject.getCcList(), pasifList, pasifPersonelSB);
					pasifListKontrol(mailObject.getBccList(), pasifList, pasifPersonelSB);
				}
			}
			userList = null;
			list = null;
			pasifList = null;
			userMap = null;
		}
	}

	/**
	 * @param list
	 * @param userMap
	 * @throws Exception
	 */
	private void mailUserListKontrol(List<MailPersonel> list, TreeMap<String, User> userMap) throws Exception {
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			MailPersonel mailPersonel = (MailPersonel) iterator.next();
			if (userMap.containsKey(mailPersonel.getEPosta()))
				mailPersonel.setAdiSoyadi(userMap.get(mailPersonel.getEPosta()).getAdSoyad());

		}
	}

	/**
	 * @param list
	 * @param mailMap
	 * @throws Exception
	 */
	private void mailListKontrol(List<MailPersonel> list, HashMap<String, MailPersonel> dataMap) throws Exception {
		if (list != null) {
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				MailPersonel mailPersonel = (MailPersonel) iterator.next();
				if (dataMap.containsKey(mailPersonel.getEPosta())) {
					iterator.remove();
				} else
					dataMap.put(mailPersonel.getEPosta(), mailPersonel);
			}

		}

	}

	/**
	 * @param list
	 * @param pasifList
	 * @param sb
	 * @throws Exception
	 */
	private void pasifListKontrol(List<MailPersonel> list, List<String> pasifList, StringBuffer sb) throws Exception {
		if (sb != null && list != null && pasifList != null) {
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				MailPersonel mailPersonel = (MailPersonel) iterator.next();
				if (pasifList.contains(mailPersonel.getEPosta())) {
					if (sb.length() > 0)
						sb.append(", ");
					sb.append((PdksUtil.hasStringValue(mailPersonel.getAdiSoyadi()) ? "<" + mailPersonel.getAdiSoyadi().trim() + "> " : "") + mailPersonel.getEPosta());
					iterator.remove();
				}
			}
		}
	}

	/**
	 * @param mailAdresleri
	 * @param mailList
	 * @return
	 * @throws Exception
	 */
	private InternetAddress[] adresleriDuzenle(List<MailPersonel> mailAdresleri, List mailList) throws Exception {
		InternetAddress[] adresler = null;
		if (mailAdresleri != null && !mailAdresleri.isEmpty()) {
			List adreslerList = new ArrayList();
			for (MailPersonel mailUser : mailAdresleri) {
				String email = mailUser.getEPosta();
				if (email.indexOf("@") > 0) {
					try {
						InternetAddress ia = new InternetAddress(email);
						if (PdksUtil.hasStringValue(mailUser.getAdiSoyadi()))
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