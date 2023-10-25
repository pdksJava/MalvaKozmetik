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
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.PersonelIzinOnay;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.pdks.security.entity.UserRoles;

import com.pdks.webservice.MailObject;
import com.pdks.webservice.MailStatu;

@Name("izinOnayHome")
public class IzinOnayHome extends EntityHome<PersonelIzin> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4510203488828369554L;
	static Logger logger = Logger.getLogger(IzinOnayHome.class);

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

	@In(required = true, create = true)
	PersonelIzinGirisiHome personelIzinGirisiHome;

	private Personel seciliPersonel;

	User yoneticiUser;
	private Date basDate, bitDate;
	private String izinAciklama;

	String kullaniciIslemleriMailAciklama = "PDKS Sistemi";
	String kullaniciIslemleriMailAdres = "";

	private List<PersonelIzin> personelizinList = new ArrayList<PersonelIzin>();
	private List<Personel> pdksPersonelList = new ArrayList<Personel>();
	private List<User> userList = new ArrayList<User>();
	private List<String> ccMailList = new ArrayList<String>();
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
		String fromAciklama = ortakIslemler.getParameterKey("fromName");
		if (PdksUtil.hasStringValue(fromAciklama))
			setKullaniciIslemleriMailAciklama(fromAciklama);
		personelIzinGirisiHome.setIzinIptalGoster(Boolean.FALSE);
		fillIzinOnayList();
	}

	public void fillIzinOnayList() {

		List<Integer> durum = new ArrayList<Integer>();
		durum.add(PersonelIzin.IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA);
		durum.add(PersonelIzin.IZIN_DURUMU_IKINCI_YONETICI_ONAYINDA);

		List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();

		HashMap parametreMap = new HashMap();
		parametreMap.put("izinTipi.bakiyeIzinTipi=", null);
		parametreMap.put("izinDurumu", durum);
		parametreMap.put("baslangicZamani<=", Calendar.getInstance().getTime());
		parametreMap.put("izinSahibi", authenticatedUser.getTumPersoneller().clone());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		izinList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, PersonelIzin.class);
		for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
			PersonelIzin personelIzin = (PersonelIzin) iterator.next();
			try {
				if (personelIzin.getOnaylayanlar() == null || personelIzin.getOnaylayanlar().isEmpty()) {
					iterator.remove();
					continue;
				}
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				iterator.remove();
				continue;
			}

			if ((personelIzin.getIzinDurumu() != PersonelIzin.IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA) && (personelIzin.getIzinDurumu() != PersonelIzin.IZIN_DURUMU_IKINCI_YONETICI_ONAYINDA))
				iterator.remove();

		}

		setPersonelizinList(izinList);

	}

	public String hatirlatma(PersonelIzin izin) {
		session.refresh(izin);
		Personel personel = izin.getIzinSahibi();
		Personel pdksPersonel = (Personel) personel.clone();
		HashMap parametreMap = new HashMap();
		parametreMap.put("pdksPersonel.id", pdksPersonel.getId());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		User izinSahibiUser = (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);
		User userYonetici = null;
		List<User> list = new ArrayList<User>();

		for (Iterator<PersonelIzinOnay> iterator = izin.getOnaylayanlar().iterator(); iterator.hasNext();) {
			PersonelIzinOnay personelIzinOnay = (PersonelIzinOnay) iterator.next();
			if (personelIzinOnay.getOnayDurum() == izin.getIzinDurumu())
				userYonetici = personelIzinOnay.getOnaylayan();

		}
		if (izinSahibiUser != null) {
			Date bugun = PdksUtil.getDate(Calendar.getInstance().getTime());
			if (izinSahibiUser.isSuperVisor()) {
				HashMap map = new HashMap();
				map.put(PdksEntityController.MAP_KEY_SELECT, "user");
				map.put("user.pdksPersonel.sirket=", personel.getSirket());
				map.put("user.pdksPersonel.iseBaslamaTarihi<=", bugun);
				map.put("user.pdksPersonel.sskCikisTarihi>=", bugun);
				map.put("user.durum=", Boolean.TRUE);
				map.put("user.pdksPersonel.durum=", Boolean.TRUE);
				map.put("role.rolename=", Role.TIPI_PROJE_MUDURU);
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);

				User projeMudur = (User) pdksEntityController.getObjectByInnerObjectInLogic(map, UserRoles.class);
				if (projeMudur != null)
					list.add(projeMudur);
			} else if (izinSahibiUser.isProjeMuduru()) {
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

		if (userYonetici != null) {

			list.add(userYonetici);
			User vekil = ortakIslemler.vekilYonetici(userYonetici, session);
			if (vekil != null)
				list.add(vekil);

			ccMailList = new ArrayList<String>();
			setInstance(izin);
			setUserList(list);
			MailStatu mailSatu = null;
			try {
				String mailPersonelAciklama = personel.getSirket().getAd() + " " + (personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() + " bölümü " : "") + personel.getAdSoyad();

				MailObject mail = new MailObject();
				mail.setSubject("İzin Onay");
				String body = "<p>" + mailPersonelAciklama + " ait başlangıç tarihi " + authenticatedUser.dateTimeFormatla(izin.getBaslangicZamani()) + " bitiş tarihi " + authenticatedUser.dateTimeFormatla(izin.getBitisZamani()) + " " + izin.getIzinTipiAciklama()
						+ " onayınıza gönderilmiş bulunmaktadır.</p>";
				mail.setBody(body);
				ortakIslemler.addMailPersonelUserList(userList, mail.getToList());
				ortakIslemler.addMailPersonelList(ccMailList, mail.getCcList());
				mailSatu = ortakIslemler.mailSoapServisGonder(true, mail, renderer, "/email/izinHatirlatmaMail.xhtml", session);

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

	public List<User> getUserList() {
		return userList;
	}

	public void setUserList(List<User> userList) {
		this.userList = userList;
	}

	public User getYoneticiUser() {
		return yoneticiUser;
	}

	public void setYoneticiUser(User yoneticiUser) {
		this.yoneticiUser = yoneticiUser;
	}

	public Date getBasDate() {
		return basDate;
	}

	public void setBasDate(Date basDate) {
		this.basDate = basDate;
	}

	public Date getBitDate() {
		return bitDate;
	}

	public void setBitDate(Date bitDate) {
		this.bitDate = bitDate;
	}

	public String getIzinAciklama() {
		return izinAciklama;
	}

	public void setIzinAciklama(String izinAciklama) {
		this.izinAciklama = izinAciklama;
	}

	public Personel getSeciliPersonel() {
		return seciliPersonel;
	}

	public void setSeciliPersonel(Personel seciliPersonel) {
		this.seciliPersonel = seciliPersonel;
	}

	public List<String> getCcMailList() {
		return ccMailList;
	}

	public void setCcMailList(List<String> ccMailList) {
		this.ccMailList = ccMailList;
	}

}
