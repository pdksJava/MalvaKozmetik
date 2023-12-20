package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.mail.internet.InternetAddress;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.Departman;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.security.entity.User;

@Name("departmanHome")
public class DepartmanHome extends EntityHome<Departman> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8254204570949087604L;
	/**
	 * 
	 */
	static Logger logger = Logger.getLogger(DepartmanHome.class);
	@RequestParameter
	Long pdksDepartmanId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = true, create = true)
	OrtakIslemler ortakIslemler;

	private List<Tanim> departmanTanimList = new ArrayList<Tanim>(), girisTipiTanimList = new ArrayList<Tanim>();
	private List<Departman> departmanList = new ArrayList<Departman>();
	private Session session;
	private List<String> adresler;
	private String mailAdres;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	@Override
	public Object getId() {
		if (pdksDepartmanId == null) {
			return super.getId();
		} else {
			return pdksDepartmanId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	@Transactional
	public String save() {
		String cikis = "";
		Departman pdksDepartman = (Departman) getInstance();
		int cocukYasUstSiniri = pdksDepartman.getCocukYasUstSiniri();
		int yasliYasAltSiniri = pdksDepartman.getYasliYasAltSiniri();
		List<String> m = new ArrayList<String>();
		if (cocukYasUstSiniri <= 0)
			m.add("Çoçuk yaş sınırı 0 dan küçük ve negatif olamaz");
		if (cocukYasUstSiniri >= yasliYasAltSiniri)
			m.add("Çoçuk yaş sınırı yaşlı çalışan değerinden büyük eşit olamaz");
		if (!m.isEmpty()) {
			FacesContext context = FacesContext.getCurrentInstance();
			for (String mesaj : m)
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, mesaj, mesaj));
		} else {
			try {
				if (pdksDepartman.getId() == null) {
					pdksDepartman.setOlusturanUser(authenticatedUser);
					pdksDepartman.setAdmin(Boolean.FALSE);
				} else {
					pdksDepartman.setGuncelleyenUser(authenticatedUser);
					pdksDepartman.setGuncellemeTarihi(new Date());
				}
				pdksEntityController.saveOrUpdate(session, entityManager, pdksDepartman);
				if (pdksDepartman.isFazlaMesaiTalepGirer() == false) {
					HashMap fields = new HashMap();
					fields.put("departman.id", pdksDepartman.getId());
					fields.put("fazlaMesaiTalepGirilebilir", Boolean.TRUE);
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<Sirket> sirketler = pdksEntityController.getObjectByInnerObjectList(fields, Sirket.class);
					for (Sirket sirket : sirketler) {
						sirket.setGuncelleyenUser(authenticatedUser);
						sirket.setGuncellemeTarihi(pdksDepartman.getGuncellemeTarihi());
						pdksEntityController.saveOrUpdate(session, entityManager, sirket);
					}
				}

				session.flush();
				fillDepartmanTanimList();
				cikis = "persisted";
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				logger.error("Save kat " + e.getMessage());
			}

		}

		return cikis;

	}

	/**
	 * @param departman
	 * @return
	 */
	public String departmanGuncelle(Departman departman) {
		if (departman == null)
			departman = new Departman();
		else {
			String mailBox = ortakIslemler.getAktifMailAdress(departman.getMailBox(), session);
			departman.setMailBox(mailBox);
		}
		setInstance(departman);

		return "";
	}

	public String adresAyarla() {
		Departman departman = getInstance();
		String adres = departman.getMailBox();
		mailAdres = "";
		adresler = new ArrayList<String>();
		if (adres != null && adres.indexOf("@") > 0) {
			String separator = PdksUtil.SEPARATOR_MAIL;
			if (adres.indexOf(separator) < 0 && adres.indexOf(",") > 0)
				separator = ",";
			if (adres.indexOf(separator) > 0)
				adresler.addAll(Arrays.asList(adres.split(separator)));
			else
				adresler.add(adres);
		}

		return "";

	}

	public String adresEkle() {
		String eMail = PdksUtil.setTurkishStr(mailAdres.trim()).toLowerCase(Locale.ENGLISH);

		try {
			if (eMail.indexOf("@") < 1)
				throw new Exception(eMail);
			InternetAddress internetAddress = new InternetAddress(eMail);
			eMail = internetAddress.getAddress();
			List<User> userList = ortakIslemler.getMailUser(eMail, false);
			if (!userList.isEmpty()) {
				if (userList.size() == 1) {
					User user = userList.get(0);
					if (user.getYetkiliPersonelNoList().size() > 1 && user.getShortUsername() != null) {
						List<User> groups = ortakIslemler.getMailUser(eMail, true);
						PdksUtil.addMessageError(user.getShortUsername() + " Grubu");
						StringBuffer sb = new StringBuffer("");
						int sira = 0;
						for (User user2 : groups)
							sb.append((sira++ > 0 ? ", " : "") + user2.getFullName() + " - " + user2.getEmail());
						PdksUtil.addMessageAvailableInfo(sb.toString());
					}
				}
			}

			if (adresler.contains(eMail))
				PdksUtil.addMessageError(eMail + " listede var!");
			else {
				adresler.add(eMail);
				mailAdres = "";
			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			PdksUtil.addMessageError(mailAdres + " hatalı adres!");
		}

		return "";

	}

	public String adresSil(String adres) {

		if (adresler.contains(adres))
			adresler.remove(adres);

		return "";

	}

	public String adresGuncelle() {
		Departman departman = getInstance();
		String sb = adresDuzelt(adresler);
		departman.setMailBox(sb.indexOf("@") > 0 ? sb : null);
		mailAdres = "";
		return "";

	}

	private String adresDuzelt(List<String> adresList) {
		StringBuilder sb = new StringBuilder();
		if (adresList.size() > 1) {
			TreeMap<String, String> map1 = new TreeMap<String, String>();
			for (String adres : adresList)
				map1.put(adres, adres);
			List<String> adresler = new ArrayList<String>(map1.values());
			adresList.clear();
			adresList.addAll(adresler);
			adresler = null;
			map1 = null;
		}
		for (Iterator iterator = adresList.iterator(); iterator.hasNext();) {
			String adres = (String) iterator.next();
			sb.append(adres.trim() + (iterator.hasNext() ? PdksUtil.SEPARATOR_MAIL : ""));
		}
		String str = sb.length() > 0 ? sb.toString() : "";
		sb = null;
		return str;
	}

	public void fillDepartmanTanimList() {
		List<Tanim> girisTipiList = ortakIslemler.getTanimList(Tanim.TIPI_GIRIS_TIPI, session);
		fillDepartmanList();
		List<Tanim> tanimList = ortakIslemler.getTanimList(Tanim.TIPI_BAGLI_DEPARTMANLAR, session);
		for (Iterator iterator2 = departmanList.iterator(); iterator2.hasNext();) {
			Departman pdksDepartman = (Departman) iterator2.next();
			for (Iterator iterator = tanimList.iterator(); iterator.hasNext();) {
				Tanim tanim = (Tanim) iterator.next();
				if (tanim.getId().equals(pdksDepartman.getDepartmanTanim().getId())) {
					iterator.remove();
					break;
				}
			}
		}
		setDepartmanTanimList(tanimList);
		setGirisTipiTanimList(girisTipiList);

	}

	public void fillDepartmanList() {
		List<Departman> list = new ArrayList<Departman>();
		HashMap parametreMap = new HashMap();
		if (!authenticatedUser.isAdmin())
			parametreMap.put("id", authenticatedUser.getDepartman().getId());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		list = pdksEntityController.getObjectByInnerObjectList(parametreMap, Departman.class);
		setDepartmanList(list);
	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		fillDepartmanTanimList();
	}

	public List<Departman> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<Departman> departmanList) {
		this.departmanList = departmanList;
	}

	public List<Tanim> getDepartmanTanimList() {
		return departmanTanimList;
	}

	public void setDepartmanTanimList(List<Tanim> departmanTanimList) {
		this.departmanTanimList = departmanTanimList;
	}

	public List<Tanim> getGirisTipiTanimList() {
		return girisTipiTanimList;
	}

	public void setGirisTipiTanimList(List<Tanim> girisTipiTanimList) {
		this.girisTipiTanimList = girisTipiTanimList;
	}

	public String getMailAdres() {
		return mailAdres;
	}

	public void setMailAdres(String mailAdres) {
		this.mailAdres = mailAdres;
	}

	public List<String> getAdresler() {
		return adresler;
	}

	public void setAdresler(List<String> adresler) {
		this.adresler = adresler;
	}

}
