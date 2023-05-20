package org.pdks.security.entity;

import java.io.Serializable;
import java.util.Date;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.pdks.security.action.SessionListener;

/**
 * @author Hasan Sayar
 * 
 */
public class KullaniciSession implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6216834831853584938L;
	static Logger logger = Logger.getLogger(KullaniciSession.class);

	private User kullanici;

	private HttpSession session;

	private int zoneFark;

	private Date simdi;

	private Boolean secili = Boolean.FALSE;

	public KullaniciSession(HttpSession xsession, Date simdix, int zoneFarkx) {
		this.session = xsession;
		if (session.getAttribute(SessionListener.SESSION_USER_NAME) != null)
			this.kullanici = (User) session.getAttribute(SessionListener.SESSION_USER_NAME);
		this.zoneFark = zoneFarkx;
		this.simdi = simdix;
	}

	public static Date getSessionTime(long deger) {
		Date date = null;
		try {
			if (deger > 0)
				date = new Date(deger);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			date = null;
		}
		return date;
	}

	public String getUserName() {
		String userName = kullanici != null ? kullanici.getUsername() : null;
		return userName;
	}

	public String getAdiSoyadi() {
		String adiSoyadi = kullanici != null ? kullanici.getAdSoyad() : null;
		return adiSoyadi;
	}

	public Date getWaitTime() {
		Date tarih = null;
		try {
			tarih = new Date(simdi.getTime() - session.getLastAccessedTime() - zoneFark);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			tarih = null;
		}

		return tarih;
	}

	public Date getCreationTime() {
		long deger = 0;
		try {
			if (session != null)
				deger = session.getCreationTime();
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			deger = 0;
		}
		Date date = getSessionTime(deger);
		return date;
	}

	public Date getLastAccessedTime() {
		long deger = 0;
		try {
			if (session != null)
				deger = session.getLastAccessedTime();
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			deger = 0;
		}
		Date date = getSessionTime(deger);
		return date;
	}

	public User getKullanici() {
		return kullanici;
	}

	public void setKullanici(User kullanici) {
		this.kullanici = kullanici;
	}

	public HttpSession getSession() {
		return session;
	}

	public void setSession(HttpSession session) {
		this.session = session;
	}

	public int getZoneFark() {
		return zoneFark;
	}

	public void setZoneFark(int zoneFark) {
		this.zoneFark = zoneFark;
	}

	public Date getSimdi() {
		return simdi;
	}

	public void setSimdi(Date simdi) {
		this.simdi = simdi;
	}

	public Boolean getSecili() {
		return secili;
	}

	public void setSecili(Boolean secili) {
		this.secili = secili;
	}

}
