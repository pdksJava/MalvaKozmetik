package org.pdks.security.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.pdks.security.entity.KullaniciSession;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksUtil;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

@Name("openSessionHome")
public class OpenSessionHome extends EntityHome<User> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5956942651819119993L;
	static Logger logger = Logger.getLogger(OpenSessionHome.class);

	@RequestParameter
	Long userId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	User authenticatedUser;

	private List<KullaniciSession> sessionFilterList = new ArrayList<KullaniciSession>();

	private Date tarih;

	private HttpSession httpSession;

	private Boolean secili;

	private long kapatilacakAdet;

	private String bolumAciklama;

	private Session session;

	@Override
	public Object getId() {
		if (userId == null) {
			return super.getId();
		} else {
			return userId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	/**
	 * @param session
	 * @return
	 */
	public Boolean isKapatilabir(HttpSession session) {
		Boolean kapatilabir = null;
		try {
			kapatilabir = session != null && !session.getId().equals(httpSession.getId()) && session.getAttribute(SessionListener.SESSION_USER_NAME) != null;
		} catch (Exception e) {

		}
		return kapatilabir;
	}

	public String sessionKapat() {
		String durum = "";
		if (!sessionFilterList.isEmpty()) {
			boolean seciliDurum = Boolean.FALSE;
			for (KullaniciSession kullaniciSession : sessionFilterList) {
				if (kullaniciSession.getSecili()) {
					seciliDurum = Boolean.TRUE;
					try {
						HttpSession ses = kullaniciSession.getSession();
						ses.invalidate();
					} catch (Exception e) {
						logger.error("PDKS hata in : \n");
						e.printStackTrace();
						logger.error("PDKS hata out : " + e.getMessage());
					}
				}
			}
			if (!seciliDurum)
				PdksUtil.addMessageWarn("İşlem yapacak kullanıcı seçiniz!");
			else if (seciliDurum)
				durum = MenuItemConstant.openSession;
		}
		return durum;
	}

	public String durumDegistir() {
		if (!sessionFilterList.isEmpty())
			sessionFilterList.clear();
		HashMap<String, HttpSession> map = new HashMap<String, HttpSession>();
		kapatilacakAdet = 0;
		ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
		Calendar cal = Calendar.getInstance();
		int zoneFark = cal.get(Calendar.ZONE_OFFSET);
		Date simdi = new Date();
		List<HttpSession> sessionList = new ArrayList(SessionListener.getSessionList(servletContext));
		for (HttpSession session : sessionList) {
			try {
				if (!map.containsKey(session.getId())) {
					KullaniciSession kullaniciSession = new KullaniciSession(session, simdi, zoneFark);
					if (kullaniciSession.getKullanici() != null) {
						if (!session.getId().equals(httpSession.getId())) {
							++kapatilacakAdet;
							kullaniciSession.setSecili(secili);
						}

						sessionFilterList.add(kullaniciSession);
					}
					map.put(session.getId(), session);
				}
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());

			}

		}
		if (sessionFilterList.size() > 1)
			sessionFilterList = PdksUtil.sortListByAlanAdi(sessionFilterList, "lastAccessedTime", Boolean.FALSE);
		for (Iterator iterator = sessionList.iterator(); iterator.hasNext();) {
			HttpSession httpSession = (HttpSession) iterator.next();
			Boolean status = isKapatilabir(httpSession);
			if (status == null) {
				iterator.remove();
			}

		}
		sessionList = null;
		map = null;
		return "";

	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		secili = Boolean.FALSE;
		authenticatedUser.setCalistigiSayfa("openSession");
		HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		setHttpSession(mySession);
		durumDegistir();
		fillEkSahaTanim();
	}

	private void fillEkSahaTanim() {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
	}

	public Date getTarih() {
		return tarih;
	}

	public void setTarih(Date tarih) {
		this.tarih = tarih;
	}

	public HttpSession getHttpSession() {
		return httpSession;
	}

	public void setHttpSession(HttpSession httpSession) {
		this.httpSession = httpSession;
	}

	public List<KullaniciSession> getSessionFilterList() {
		return sessionFilterList;
	}

	public void setSessionFilterList(List<KullaniciSession> sessionFilterList) {
		this.sessionFilterList = sessionFilterList;
	}

	public Boolean getSecili() {
		return secili;
	}

	public void setSecili(Boolean secili) {
		this.secili = secili;
	}

	public long getKapatilacakAdet() {
		return kapatilacakAdet;
	}

	public void setKapatilacakAdet(long kapatilacakAdet) {
		this.kapatilacakAdet = kapatilacakAdet;
	}

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

}
