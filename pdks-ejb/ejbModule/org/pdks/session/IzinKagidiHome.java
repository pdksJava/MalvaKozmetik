package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelIzin;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.pdks.security.entity.UserRoles;

import com.pdks.webservice.MailObject;
import com.pdks.webservice.MailStatu;

@Name("izinKagidiHome")
public class IzinKagidiHome extends EntityHome<PersonelIzin> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1903012150134048129L;
	static Logger logger = Logger.getLogger(IzinKagidiHome.class);

	@RequestParameter
	Long personelIzinId;
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false)
	User authenticatedUser;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	@In(required = true, create = true)
	Renderer renderer;

	private Personel seciliPersonel;

	User yoneticiUser;

	String kullaniciIslemleriMailAciklama = "PDKS Sistemi";
	String kullaniciIslemleriMailAdres = "";

	private List<PersonelIzin> personelizinList = new ArrayList<PersonelIzin>();
	private List<Personel> pdksPersonelList = new ArrayList<Personel>();
	private List<User> toUserList = new ArrayList<User>(), ccUserList = new ArrayList<User>();
	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	@In(required = false)
	FacesMessages facesMessages;

	@Override
	public Object getId() {
		if (personelIzinId == null) {
			return super.getId();
		} else {
			return personelIzinId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		// setPersonelizinList(new ArrayList());
		fillIzinKagidiList();
	}

	public void fillIzinKagidiList() {

		List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
		List<Integer> durumlar = new ArrayList<Integer>();
		durumlar.add(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
		durumlar.add(PersonelIzin.IZIN_DURUMU_IK_ONAYINDA);
		durumlar.add(PersonelIzin.IZIN_DURUMU_SAP_GONDERILDI);
		HashMap parametreMap = new HashMap();
		parametreMap.put("baslangicZamani>=", PdksUtil.tariheAyEkleCikar(Calendar.getInstance().getTime(), -6));
		parametreMap.put("izinSahibi", authenticatedUser.getTumPersoneller().clone());
		parametreMap.put("izinDurumu", durumlar);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

		izinList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, PersonelIzin.class);
		for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
			PersonelIzin personelIzin = (PersonelIzin) iterator.next();
			IzinTipi izinTipi = personelIzin.getIzinTipi();
			if (!izinTipi.getIzinKagidiGeldi() || personelIzin.getIzinKagidiGeldi() != null)
				iterator.remove();

		}
		setPersonelizinList(izinList);

	}

	public String hatirlatmaIzin(PersonelIzin izin) {
		Personel personel = izin.getIzinSahibi();
		setInstance(izin);
		setSeciliPersonel(personel);
		Personel pdksPersonel = (Personel) personel.clone();

		List<User> list = new ArrayList<User>();
		HashMap parametreMap = new HashMap();
		parametreMap.put("pdksPersonel.id", pdksPersonel.getId());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		User izinSahibiUser = (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);
		if (izinSahibiUser != null) {
			if (izinSahibiUser.isSuperVisor()) {
				Date bugun = PdksUtil.getDate(Calendar.getInstance().getTime());
				HashMap map = new HashMap();
				map.put(PdksEntityController.MAP_KEY_SELECT, "user.pdksPersonel");
				map.put("user.pdksPersonel.sirket=", personel.getSirket());
				map.put("user.pdksPersonel.iseBaslamaTarihi<=", bugun);
				map.put("user.pdksPersonel.sskCikisTarihi>=", bugun);
				map.put("user.durum=", Boolean.TRUE);
				map.put("user.pdksPersonel.durum=", Boolean.TRUE);
				map.put("role.rolename=", Role.TIPI_PROJE_MUDURU);
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				Personel projeMudur = (Personel) pdksEntityController.getObjectByInnerObjectInLogic(map, UserRoles.class);
				if (projeMudur != null)
					pdksPersonel.setYoneticisiAta(projeMudur);
			} else if (izinSahibiUser.isProjeMuduru()) {
				Date bugun = PdksUtil.getDate(Calendar.getInstance().getTime());
				HashMap map = new HashMap();
				map.put(PdksEntityController.MAP_KEY_SELECT, "user");
				map.put("user.departman=", personel.getSirket().getDepartman());
				map.put("user.pdksPersonel.iseBaslamaTarihi<=", bugun);
				map.put("user.pdksPersonel.sskCikisTarihi>=", bugun);
				map.put("user.durum=", Boolean.TRUE);
				map.put("user.pdksPersonel.durum=", Boolean.TRUE);
				map.put("role.rolename=", Role.TIPI_IK);
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				List ikList = pdksEntityController.getObjectByInnerObjectListInLogic(map, UserRoles.class);
				if (!ikList.isEmpty())
					list.addAll(ikList);

			}
		}

		if (pdksPersonel.getPdksYonetici() != null) {
			parametreMap.clear();
			parametreMap.put("pdksPersonel.id", pdksPersonel.getPdksYonetici().getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			User userYonetici = (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);
			if (userYonetici != null) {
				list.add(userYonetici);
				try {
					User vekil = ortakIslemler.getYoneticiBul(pdksPersonel, userYonetici.getPdksPersonel(), session);
					if (vekil != null && !vekil.getId().equals(userYonetici.getId()))
						list.add(vekil);
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());

				}

			}
		}
		setToUserList(new ArrayList<User>());
		setCcUserList(new ArrayList<User>());
		if (izinSahibiUser != null)
			getToUserList().add(izinSahibiUser);

		if (!list.isEmpty()) {
			setCcUserList(list);
			MailStatu mailSatu = null;
			try {
				if (!list.isEmpty()) {
					MailObject mail = new MailObject();
					mail.setSubject("İzin Kağıdı");
					String body = "<p>" + personel.getAdSoyad() + " ait başlangıç tarihi " + authenticatedUser.dateTimeFormatla(izin.getBaslangicZamani()) + " bitiş tarihi " + authenticatedUser.dateTimeFormatla(izin.getBitisZamani()) + " " + izin.getIzinTipiAciklama()
							+ " ait izin kağıdı gelmemiştir.</p>";
					mail.setBody(body);
					ortakIslemler.addMailPersonelUserList(toUserList, mail.getToList());
					ortakIslemler.addMailPersonelUserList(ccUserList, mail.getCcList());
					mailSatu = ortakIslemler.mailSoapServisGonder(true, mail, renderer, "/email/izinKagidiMail.xhtml", session);
				}

			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());

				PdksUtil.addMessageError(e.getMessage());
			}
			if (mailSatu != null && mailSatu.isDurum())
				PdksUtil.addMessageInfo("Mesaj gönderildi");
		}

		return "";

	}

	public List<PersonelIzin> getPersonelizinList() {
		return personelizinList;
	}

	public void setPersonelizinList(List<PersonelIzin> personelizinList) {
		this.personelizinList = personelizinList;
	}

	public List<Personel> getpdksPersonelList() {
		return pdksPersonelList;
	}

	public void setpdksPersonelList(List<Personel> pdksPersonelList) {
		this.pdksPersonelList = pdksPersonelList;
	}

	public String getKullaniciIslemleriMailAciklama() {
		return kullaniciIslemleriMailAciklama;
	}

	public void setKullaniciIslemleriMailAciklama(String kullaniciIslemleriMailAciklama) {
		this.kullaniciIslemleriMailAciklama = kullaniciIslemleriMailAciklama;
	}

	public String getKullaniciIslemleriMailAdres() {
		return kullaniciIslemleriMailAdres;
	}

	public void setKullaniciIslemleriMailAdres(String kullaniciIslemleriMailAdres) {
		this.kullaniciIslemleriMailAdres = kullaniciIslemleriMailAdres;
	}

	public User getYoneticiUser() {
		return yoneticiUser;
	}

	public void setYoneticiUser(User yoneticiUser) {
		this.yoneticiUser = yoneticiUser;
	}

	public Personel getSeciliPersonel() {
		return seciliPersonel;
	}

	public void setSeciliPersonel(Personel seciliPersonel) {
		this.seciliPersonel = seciliPersonel;
	}

	public List<User> getToUserList() {
		return toUserList;
	}

	public void setToUserList(List<User> toUserList) {
		this.toUserList = toUserList;
	}

	public List<User> getCcUserList() {
		return ccUserList;
	}

	public void setCcUserList(List<User> ccUserList) {
		this.ccUserList = ccUserList;
	}

}
