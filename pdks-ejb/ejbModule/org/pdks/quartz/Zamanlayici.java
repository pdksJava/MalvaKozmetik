package org.pdks.quartz;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.Renderer;
import org.pdks.entity.Dosya;
import org.pdks.entity.Parameter;
import org.pdks.entity.Tatil;
import org.pdks.security.action.UserHome;
import org.pdks.security.entity.User;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

import com.pdks.webservice.MailFile;
import com.pdks.webservice.MailObject;
import com.pdks.webservice.MailStatu;

@Name("zamanlayici")
@AutoCreate
public class Zamanlayici implements Serializable {

	private static final long serialVersionUID = 7609983147081676186L;
	static Logger logger = Logger.getLogger(Zamanlayici.class);

	private List<User> adminList;

	private String konu, aciklama;

	private Dosya dosya;

	@In(required = false, create = true)
	UserHome userHome;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In
	IseGelmemeUyari iseGelmemeUyari;
	@In
	PersonelERPGuncelleme personelERPGuncelleme;
	@In
	IzinBakiyeGuncelleme izinBakiyeGuncelleme;
	@In
	SertifikaSSLKontrol sertifikaSSLKontrol;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	Renderer renderer;
	@In(required = false)
	User authenticatedUser;
	@In
	FazlaMesaiGuncelleme fazlaMesaiGuncelleme;

	public void scheduleFazlaMesaiGuncellemeTimer() {
		logger.info("fazlaMesaiGuncellemeTimer start : " + new Date());
		fazlaMesaiGuncelleme.fazlaMesaiGuncellemeTimer(new Date(), "0 0/5 3-23 ? * *");
	}

	public void scheduleSertifikaSSLKontrolTimer() {
		logger.info("scheduleSertifikaSSLKontrolTimer start : " + new Date());
		sertifikaSSLKontrol.sertifikaSSLKontrolTimer(new Date(), "0 0/15 8-21 ? * *");
	}

	public void scheduleIseGelmemeUyariTimer() {
		logger.info("scheduleIseGelmemeUyariTimer start : " + new Date());
		iseGelmemeUyari.iseGelmeDurumuTimer(new Date(), "0 0/5 8-14 ? * *");
	}

	public void schedulePersonelERPGuncellemeTimer() {
		logger.info("schedulePersonelERPGuncellemeTimer start : " + new Date());
		personelERPGuncelleme.personelERPGuncellemeTimer(new Date(), "0 0/5 3-21 ? * *");
	}

	public void izinBakiyeGuncellemeTimer() {
		logger.info("izinBakiyeGuncellemeTimer start : " + new Date());
		izinBakiyeGuncelleme.izinBakiyeGuncellemeTimer(new Date(), "0 0/5 3-18 ? * *");
	}

	public boolean isPazar() {
		boolean pazar = PdksUtil.getDateField(new Date(), Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
		return pazar;
	}

	public boolean isCumartesi() {
		boolean cumartesi = PdksUtil.getDateField(new Date(), Calendar.DAY_OF_WEEK) == Calendar.SATURDAY;
		return cumartesi;
	}

	public boolean isHaftaSonu() {
		Calendar cal = Calendar.getInstance();
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		boolean haftaSonu = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
		return haftaSonu;
	}

	public boolean isHaftaIci() {
		Calendar cal = Calendar.getInstance();
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		boolean haftaIci = dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY;
		return haftaIci;
	}

	/**
	 * @param session
	 * @param mail
	 * @param xkonu
	 * @param xaciklama
	 * @param userList
	 * @param xDosya
	 * @param adresEkle
	 * @throws Exception
	 */
	public void mailGonderDosya(Session session, MailObject mail, String xkonu, String xaciklama, List<User> userList, Dosya xDosya, Boolean adresEkle) throws Exception {
		setDosya(xDosya);
		if (mail == null)
			mail = new MailObject();
		Boolean yeni = authenticatedUser == null;
		InetAddress thisIp = adresEkle == null || adresEkle ? InetAddress.getLocalHost() : null;

		String sayfaAdi = "quartzMail.xhtml";
		if (session != null)
			yeni = false;
		else if (yeni)
			session = PdksUtil.getSession(entityManager, yeni);
		if (userList == null || userList.isEmpty()) {
			userList = ortakIslemler.bccAdminAdres(session, xkonu);
			xaciklama += " ( " + PdksUtil.convertToDateString(Calendar.getInstance().getTime(), PdksUtil.getDateFormat() + " H:mm") + " ) ";
		}
		if (thisIp != null)
			logger.info(PdksUtil.setTurkishStr(xkonu + " " + thisIp));
		if (!userList.isEmpty()) {
			setAdminList(userList);
			setKonu(xkonu);
			setAciklama(xaciklama + (thisIp != null ? " --> Host Name : " + thisIp.getHostName() : ""));
			MailStatu mailSatu = null;
			try {
				// ortakIslemler.mailGonder(renderer, "/email/" + sayfaAdi);

				mail.setSubject(xkonu);
				mail.setBody(aciklama);
				if (dosya != null) {
					MailFile mf = new MailFile();
					mf.setDisplayName(dosya.getDosyaAdi());
					mf.setIcerik(dosya.getDosyaIcerik());
					mail.getAttachmentFiles().add(mf);
				}
				ortakIslemler.addMailPersonelUserList(userList, mail.getToList());
				mailSatu = ortakIslemler.mailSoapServisGonder(false, mail, renderer, "/email/" + sayfaAdi, session);
				if (mailSatu != null && mailSatu.isDurum()) {
					if (thisIp != null)
						logger.info(PdksUtil.setTurkishStr(xkonu + " " + thisIp + " tamamlandı."));
				}

			} catch (Exception e) {
				logger.info(sayfaAdi + " : " + xkonu + " --> " + e.getMessage());
			}
		}
		if (yeni && session != null)
			session.close();

	}

	/**
	 * @param session
	 * @return
	 */
	public boolean getOzelKontrol(Session session) {
		Parameter parameterOzel = ortakIslemler.getParameter(session, "ozelKontrol");
		String value = (parameterOzel != null) ? parameterOzel.getValue() : "";
		boolean ozelKontrolDurum = value == null || !value.equals("0");
		if (ozelKontrolDurum) {
			Calendar cal = Calendar.getInstance();
			Date tarih = PdksUtil.getDate(cal.getTime());
			TreeMap<String, Tatil> resmiTatilMap = null;
			try {
				resmiTatilMap = ortakIslemler.getTatilGunleri(null, tarih, ortakIslemler.tariheGunEkleCikar(cal, tarih, 1), session);
			} catch (Exception e) {
				resmiTatilMap = new TreeMap<String, Tatil>();
			}
			if (!resmiTatilMap.isEmpty()) {
				String key = PdksUtil.convertToDateString(tarih, "yyyyMMdd");
				try {
					if (resmiTatilMap.containsKey(key))
						ozelKontrolDurum = resmiTatilMap.get(key).isYarimGunMu();
				} catch (Exception e) {
				}

			}
		}
		return ozelKontrolDurum;
	}

	/**
	 * @param session
	 * @param mailx
	 * @param xkonu
	 * @param xaciklama
	 * @param userList
	 * @param adresEkle
	 * @throws Exception
	 */
	public void mailGonder(Session session, MailObject mailx, String xkonu, String xaciklama, List<User> userList, Boolean adresEkle) throws Exception {
		if (PdksUtil.isSistemDestekVar())
			mailGonderDosya(session, mailx, xkonu, xaciklama, userList, null, adresEkle);

	}

	public Dosya getDosya() {
		return dosya;
	}

	public void setDosya(Dosya dosya) {
		this.dosya = dosya;
	}

	public List<User> getAdminList() {
		return adminList;
	}

	public void setAdminList(List<User> adminList) {
		this.adminList = adminList;
	}

	public String getKonu() {
		return konu;
	}

	public void setKonu(String konu) {
		this.konu = konu;
	}

	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	/**
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public Date getDbTime(Session session) throws Exception {
		Calendar cal = Calendar.getInstance();
		Date time = cal.getTime();

		return time;
	}
	// http://www.quartz-scheduler.org/docs/tutorials/crontrigger.html

	// Expression Meaning
	// 0 0 12 * * ? Fire at 12pm (noon) every day
	// 0 15 10 ? * * Fire at 10:15am every day
	// 0 15 10 * * ? Fire at 10:15am every day
	// 0 15 10 * * ? * Fire at 10:15am every day
	// 0 15 10 * * ? 2005 Fire at 10:15am every day during the year 2005
	// 0 * 14 * * ? Fire every minute starting at 2pm and ending at 2:59pm, every day
	// 0 0/5 14 * * ? Fire every 5 minutes starting at 2pm and ending at 2:55pm, every day
	// 0 0/5 14,18 * * ? Fire every 5 minutes starting at 2pm and ending at 2:55pm, AND fire every 5 minutes starting at 6pm and ending at 6:55pm, every day
	// 0 0-5 14 * * ? Fire every minute starting at 2pm and ending at 2:05pm, every day
	// 0 10,44 14 ? 3 WED Fire at 2:10pm and at 2:44pm every Wednesday in the month of March.
	// 0 15 10 ? * MON-FRI Fire at 10:15am every Monday, Tuesday, Wednesday, Thursday and Friday
	// 0 15 10 15 * ? Fire at 10:15am on the 15th day of every month
	// 0 15 10 L * ? Fire at 10:15am on the last day of every month
	// 0 15 10 ? * 6L Fire at 10:15am on the last Friday of every month
	// 0 15 10 ? * 6L Fire at 10:15am on the last Friday of every month
	// 0 15 10 ? * 6L 2002-2005 Fire at 10:15am on every last friday of every month during the years 2002, 2003, 2004 and 2005
	// 0 15 10 ? * 6#3 Fire at 10:15am on the third Friday of every month
	// 0 0 12 1/5 * ? Fire at 12pm (noon) every 5 days every month, starting on the first day of the month.
	// 0 11 11 11 11 ? Fire every November 11th at 11:11am.

}