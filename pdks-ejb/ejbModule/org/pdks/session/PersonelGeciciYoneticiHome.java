package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
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
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelGeciciYonetici;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.pdks.security.entity.UserRoles;

import com.pdks.webservice.MailObject;
import com.pdks.webservice.MailStatu;

@Name("personelGeciciYoneticiHome")
public class PersonelGeciciYoneticiHome extends EntityHome<PersonelGeciciYonetici> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8193648203540476873L;
	static Logger logger = Logger.getLogger(PersonelGeciciYoneticiHome.class);

	@RequestParameter
	Long personelGeciciYoneticiId;

	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	@In(required = false)
	FacesMessages facesMessages;

	@In(required = true, create = true)
	Renderer renderer;
	private boolean aramaVisible = Boolean.FALSE;

	private List<Personel> personelList = new ArrayList<Personel>();
	private List<User> rotasyonYoneticiList = new ArrayList<User>();
	private List<PersonelGeciciYonetici> rotasyonList = new ArrayList<PersonelGeciciYonetici>();
	private boolean yonetici = Boolean.FALSE;
	private List<User> toList = new ArrayList<User>();
	private String mailAciklamaUserList, linkAdres;
	private String mailAciklamaTarih;
	private User yeniYonetici, seciliUser, arananUser;
	private Tanim seciliDepartman;
	private List<String> roleList;
	private Sirket seciliSirket;
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private List<Tanim> departmanList = new ArrayList<Tanim>();

	private List<SelectItem> sirketItemList = new ArrayList<SelectItem>(), yoneticiler;
	private List<Sirket> sirketList = new ArrayList<Sirket>();

	private List<User> userList = new ArrayList<User>();
	private Personel seciliPersonel;
	private Long yeniYoneticiId;

	private boolean sapDepartman;

	private boolean pdksDepartman;

	private boolean userArama = Boolean.FALSE;

	private boolean visibled = Boolean.FALSE;

	private boolean personelArama = Boolean.TRUE;

	private boolean checkBox;

	private boolean checkBoxDurum;

	private String reRender, aramaTipi;

	private Sirket sirket;
	private Session session;

	public List<Personel> getArananPersonelList() {
		List<Personel> list = getPersonelList();

		if (list.isEmpty() && seciliUser != null) {
			ortakIslemler.yoneticiIslemleri(seciliUser, 2, null, null, session);
			list = personelBul(seciliUser.getYetkiliPersonelNoList());
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Personel pdksPersonel = (Personel) iterator.next();
				if (pdksPersonel.getId().equals(seciliUser.getPdksPersonel().getId()))
					iterator.remove();

			}
			if (list.size() > 1)
				list = PdksUtil.sortObjectStringAlanList(list, "getAdiSoyadi", null);
			setPersonelList(list);
			fillMevcutRotasyonList();
		}

		return list;
	}

	public List<Personel> getPersonelList() {
		return personelList;
	}

	private List<Personel> personelBul(List<String> personelNoList) {

		List personelBulList = null;
		try {
			Date bugun = Calendar.getInstance().getTime();
			HashMap parametreMap = new HashMap();
			parametreMap.put("durum=", Boolean.TRUE);
			parametreMap.put("pdksSicilNo", personelNoList);
			parametreMap.put("iseBaslamaTarihi<=", bugun);
			parametreMap.put("iseBaslamaTarihi<=", bugun);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			personelBulList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, Personel.class);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		return personelBulList;

	}

	public void setPersonelList(List<Personel> personelList) {
		this.personelList = personelList;
	}

	// public List<SelectItem> getRotasyonYoneticiList() {
	// if (yoneticiler == null)
	// yoneticiler = new ArrayList<SelectItem>();
	// else
	// yoneticiler.clear();
	// List<User> list = new ArrayList<User>(rotasyonYoneticiList);
	// if (seciliUser != null)
	// for (Iterator iterator = list.iterator(); iterator.hasNext();) {
	// User user = (User) iterator.next();
	// if (user.getId().equals(seciliUser.getId()))
	// iterator.remove();
	// else
	// yoneticiler.add(new SelectItem(user.getId(), user.getAdSoyad()));
	// }
	// return yoneticiler;
	// }

	public void setRotasyonYoneticiList(List<User> rotasyonYoneticiList) {
		this.rotasyonYoneticiList = rotasyonYoneticiList;
	}

	public List<PersonelGeciciYonetici> getRotasyonList() {
		return rotasyonList;
	}

	public void setRotasyonList(List<PersonelGeciciYonetici> rotasyonList) {
		this.rotasyonList = rotasyonList;
	}

	@Override
	public Object getId() {
		if (personelGeciciYoneticiId == null) {
			return super.getId();
		} else {
			return personelGeciciYoneticiId;
		}
	}

	public void fillMevcutRotasyonList() {

		List<PersonelGeciciYonetici> rotasyonList = new ArrayList<PersonelGeciciYonetici>();
		HashMap parammap = new HashMap();
		parammap.put("durum=", Boolean.TRUE);
		parammap.put("bitTarih>=", new Date());
		if (session != null)
			parammap.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			if (seciliUser != null && seciliUser.getId() != null) {
				parammap.put("bagliYonetici.id=", seciliUser.getId());
				rotasyonList = pdksEntityController.getObjectByInnerObjectListInLogic(parammap, PersonelGeciciYonetici.class);

			} else if (ortakIslemler.getAdminRole(authenticatedUser))
				rotasyonList = pdksEntityController.getObjectByInnerObjectListInLogic(parammap, PersonelGeciciYonetici.class);
			else if (authenticatedUser.isYonetici()) {
				parammap.put("bagliYonetici.id=", authenticatedUser.getId());
				rotasyonList = pdksEntityController.getObjectByInnerObjectListInLogic(parammap, PersonelGeciciYonetici.class);
			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		setRotasyonList(rotasyonList);

	}

	public void fillPersonelList(User user) {
		List<Personel> list = null;
		try {
			ortakIslemler.setUserRoller(user, session);
			AramaSecenekleri as = new AramaSecenekleri(null, true);
			list = ortakIslemler.getAramaSecenekleriPersonelList(user, null, as, session);
			if (!list.isEmpty()) {

				Personel yoneticiPersonel = user.getPdksPersonel();
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					Personel pdksPersonel = (Personel) iterator.next();
					if (pdksPersonel.getId().equals(yoneticiPersonel.getId())) {
						iterator.remove();
					} else
						pdksPersonel.setCheckBoxDurum(Boolean.FALSE);

				}
				if (!list.isEmpty()) {
					if (list.size() == 1)
						list.get(0).setCheckBoxDurum(Boolean.TRUE);
					else
						list = PdksUtil.sortObjectStringAlanList(list, "getAdSoyad", null);
				}

			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		setPersonelList(list);

	}

	public void personelSecimIslemi(Personel pdksPersonel) {
		logger.debug(pdksPersonel.toString() + " " + new Date());
		getInstance().setPersonelGecici(pdksPersonel);
	}

	public void cokluSecimIslemi(ArrayList parametre) {
		panelDurumDegistir();
		List<Personel> personelList = new ArrayList<Personel>();
		for (Iterator iterator = parametre.iterator(); iterator.hasNext();) {
			User user = (User) iterator.next();
			if (!user.getPdksPersonel().isCheckBoxDurum())
				continue;
			ortakIslemler.yoneticiIslemleri(user, 2, null, null, session);
			List<Personel> tempPersonelList = new ArrayList<Personel>();
			try {
				tempPersonelList = personelBul(user.getPersonelGeciciNoList());
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());

			}
			personelList.addAll(tempPersonelList);
		}
		setPersonelList(personelList);
		logger.debug(parametre.toString() + " " + new Date());
	}

	public Personel getArananPersonel() {

		Personel pdksPersonel = seciliPersonel != null ? seciliPersonel : new Personel();
		if (pdksPersonel.getId() != null)
			logger.debug("Bulunan personel : " + new Date());

		return pdksPersonel;
	}

	@Transactional
	public String yoneticiTransferGerceklestir() {
		List<Personel> transferList = getPersonelList();
		StringBuilder mailAciklamaUserList = new StringBuilder();
		String mailAciklamaTarih = "";
		List<Personel> eklenecekPersonelList = new ArrayList<Personel>();
		List eklenecekPersonelIdList = new ArrayList();
		PersonelGeciciYonetici geciciYonetici = getInstance();
		for (Iterator iterator = transferList.iterator(); iterator.hasNext();) {
			Personel pdksPersonel = (Personel) iterator.next();
			if (pdksPersonel.isCheckBoxDurum()) {
				eklenecekPersonelList.add(pdksPersonel);
				eklenecekPersonelIdList.add(pdksPersonel.getId());
			}
		}
		HashMap parametreMap = new HashMap();

		parametreMap.put("id", yeniYoneticiId);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		yeniYonetici = (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);
		HashMap param = new HashMap();
		param.put("bagliYonetici.id", seciliUser.getId());
		param.put("personelGecici.id", eklenecekPersonelIdList);
		param.put("durum", Boolean.TRUE);
		if (session != null)
			param.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PersonelGeciciYonetici> mevcutKontrolList = pdksEntityController.getObjectByInnerObjectList(param, PersonelGeciciYonetici.class);
		boolean flag = Boolean.FALSE;
		for (Iterator iterator = mevcutKontrolList.iterator(); iterator.hasNext() && !flag;) {
			PersonelGeciciYonetici mevcutRotasyon = (PersonelGeciciYonetici) iterator.next();
			if (mevcutRotasyon.getBasTarih().compareTo(geciciYonetici.getBasTarih()) == 0) // baslangic tarihleri kesisiyorsa bitie bakmaya gerek yok kesisen tarih vardir.
				flag = Boolean.TRUE;
			else if (geciciYonetici.getBasTarih().compareTo(mevcutRotasyon.getBasTarih()) < 0 && geciciYonetici.getBitTarih().compareTo(mevcutRotasyon.getBasTarih()) > 0)// tarih kesisimi daha önce
				// kaydedilmis olandan daha
				// once baslıcak ama bitisi
				// araya girip kesisecek bir
				// tarihtir.
				flag = Boolean.TRUE;
			else if (geciciYonetici.getBasTarih().compareTo(mevcutRotasyon.getBasTarih()) > 0 && geciciYonetici.getBasTarih().compareTo(mevcutRotasyon.getBitTarih()) < 0)
				// baslangic tarihi mevcut olandan buyukse ve bitisten sonra olmazsa kesisim vardir
				flag = Boolean.TRUE;
		}
		// verilen tarih aralıgında kesisen herhangi bir rotasyonu var mı diye cekelim
		// sonra kaydetmek isteği personel listesi üzerinden o personelleri
		PersonelGeciciYonetici tempGeciciYonetici = null;
		if (!flag) {
			for (Iterator iterator = eklenecekPersonelList.iterator(); iterator.hasNext();) {
				if (mailAciklamaUserList.length() > 0)
					mailAciklamaUserList.append("-");
				Personel pdksPersonel = (Personel) iterator.next();
				// session.refresh(seciliUser);
				tempGeciciYonetici = new PersonelGeciciYonetici();

				tempGeciciYonetici.setYeniYonetici(yeniYonetici);
				tempGeciciYonetici.setBasTarih(geciciYonetici.getBasTarih());
				tempGeciciYonetici.setBitTarih(geciciYonetici.getBitTarih());
				tempGeciciYonetici.setBagliYonetici(seciliUser);
				tempGeciciYonetici.setPersonelGecici(pdksPersonel);
				tempGeciciYonetici.setOlusturanUser(authenticatedUser);
				tempGeciciYonetici.setOlusturmaTarihi(new Date());
				setInstance(tempGeciciYonetici);
				pdksEntityController.saveOrUpdate(session, entityManager, tempGeciciYonetici);
				mailAciklamaUserList.append(pdksPersonel.getAdSoyad());
				if (!PdksUtil.hasStringValue(mailAciklamaTarih))
					mailAciklamaTarih = PdksUtil.convertToDateString(tempGeciciYonetici.getBasTarih(), "yyyy") + "-" + PdksUtil.convertToDateString(tempGeciciYonetici.getBitTarih(), "yyyy");
			}

			List<User> toList = mailGidecekPersonelBul(geciciYonetici);
			if (toList == null)
				toList = new ArrayList<User>();
			Map<String, String> map = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
			String path = map.containsKey("host") ? map.get("host") : "";
			if (path.indexOf("localhost") >= 0) {
				toList.clear();
				toList.add(authenticatedUser);
			}
			setToList(toList);
			setMailAciklamaUserList(mailAciklamaUserList.toString());
			mailAciklamaUserList = null;
			setMailAciklamaTarih(mailAciklamaTarih);
			setYeniYonetici(yeniYonetici);
			MailStatu mailSatu = null;
			try {
				MailObject mail = new MailObject();
				mail.setSubject("Rotasyon Bilgisi");
				String body = "<p>" + mailAciklamaTarih + " tarihleri arasında " + mailAciklamaUserList + " adlı personel/personeller " + yeniYonetici.getAdSoyad() + "  bağlı çalışacak şekilde personel rotasyonu yapılmıştır.</p>";
				mail.setBody(body);
				ortakIslemler.addMailPersonelUserList(toList, mail.getToList());

				mailSatu = ortakIslemler.mailSoapServisGonder(true, mail, renderer, "/email/rotasyonMail.xhtml", session);

			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				PdksUtil.addMessageError(e.getMessage());
			}
			if (mailSatu != null && mailSatu.isDurum())
				PdksUtil.addMessageInfo("Mesaj gönderildi");

			session.flush();
			fillMevcutRotasyonList();
		} else
			facesMessages.add("Verilen Tarih aralığı ve personel listesi için mevcut rotasyon bulunmaktadir. Lütfen kontrol ettikten sonra tekrar deneyiniz.", "");

		return "persisted";
	}

	private List<User> mailGidecekPersonelBul(PersonelGeciciYonetici geciciYonetici) {

		List<User> toList = null;

		return toList;
	}

	public User getArananKullanici() {
		User user1 = null;
		if (seciliUser != null && seciliUser.getId() != null)
			user1 = seciliUser;
		else {
			user1 = authenticatedUser;
			fillMevcutRotasyonList();
		}

		return user1;
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	public void personelArama(String entityHomereRender) {
		this.setCheckBox(Boolean.FALSE);
		this.setRoleList(null);
		this.setReRender(entityHomereRender);
		aramaBaslangic(false, Boolean.TRUE);
	}

	private void fillEkSahaTanim() {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
	}

	private void aramaBaslangic(boolean userAramaSecim, boolean personelAramaSecim) {
		fillEkSahaTanim();
		this.setPersonelList(new ArrayList<Personel>());
		this.setUserList(new ArrayList<User>());
		this.setPersonelArama(Boolean.FALSE);
		this.setUserArama(Boolean.FALSE);
		this.setSeciliPersonel(new Personel());
		this.setSeciliUser(new User());
		this.setCheckBoxDurum(Boolean.FALSE);
		if (userAramaSecim)
			this.setUserArama(Boolean.TRUE);
		else if (personelAramaSecim)
			this.setPersonelArama(Boolean.TRUE);
		if (userAramaSecim || personelAramaSecim) {
			User user = new User();
			user.setPdksPersonel(new Personel());
			setSeciliUser(user);

			fillDepartmanList();
		} else {
			setSirketList(new ArrayList<Sirket>());
			setDepartmanList(new ArrayList<Tanim>());
		}
		panelDurumDegistir();
	}

	public void fillDepartmanList() {
		List<Tanim> list = new ArrayList<Tanim>();
		if (sapDepartman)
			list = ortakIslemler.getTanimList(Tanim.TIPI_SAP_DEPARTMAN, session);
		if (pdksDepartman) {
			List pdksDepartmanList = ortakIslemler.getTanimList(Tanim.TIPI_PDKS_DEPARTMAN, session);
			if (!pdksDepartmanList.isEmpty())
				list.addAll(pdksDepartmanList);
		}
		setDepartmanList(list);

	}

	public void panelDurumDegistir() {
		this.setVisibled(!this.isVisibled());
	}

	public void tekKullaniciSecimIslemi(User bagliYonetici) {
		panelDurumDegistir();
		setSeciliUser(bagliYonetici);
		fillPersonelList(bagliYonetici);
		PersonelGeciciYonetici geciciYonetici = new PersonelGeciciYonetici();
		geciciYonetici.setBagliYonetici(bagliYonetici);
		userList.clear();
		setInstance(geciciYonetici);
	}

	public String fillKullaniciList() {
		List<User> list = new ArrayList<User>();
		Personel arananPersonel = arananUser.getPdksPersonel();

		String adi = arananPersonel.getAd();
		String soyadi = arananPersonel.getSoyad();
		String sicilNo = arananPersonel.getErpSicilNo();
		String username = arananUser.getUsername();
		String onEk = "";
		Class class1 = User.class;
		HashMap parametreMap = new HashMap();
		parametreMap.put(PdksEntityController.MAP_KEY_MAP, "getStaffId");
		if (roleList != null) {
			class1 = UserRoles.class;
			onEk = "user.";
			parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "user");
			parametreMap.put("role.rolename", roleList);
		}

		parametreMap.put(onEk + "durum=", Boolean.TRUE);
		parametreMap.put(onEk + "pdksPersonel.durum=", Boolean.TRUE);

		ArrayList<String> departmanIdList = new ArrayList<String>();
		List<String> perNoList = ortakIslemler.getYetkiTumPersonelNoList();
		if (PdksUtil.hasStringValue(adi))
			parametreMap.put(onEk + "pdksPersonel.ad like", adi.trim() + "%");
		if (PdksUtil.hasStringValue(soyadi))
			parametreMap.put(onEk + "pdksPersonel.soyad like", soyadi.trim() + "%");
		if (PdksUtil.hasStringValue(username))
			parametreMap.put(onEk + "username like", username.trim() + "%");
		if (PdksUtil.hasStringValue(sicilNo))
			parametreMap.put(onEk + "pdksPersonel.pdksSicilNo=", sicilNo.trim());
		else {
			for (Tanim departman : departmanList)
				departmanIdList.add(departman.getId().toString());
		}
		if (arananPersonel.getSirket() != null)
			parametreMap.put(onEk + "pdksPersonel.sirket.id=", arananPersonel.getSirket().getId());
		else if (!sirketList.isEmpty())
			parametreMap.put(onEk + "pdksPersonel.sirket", sirketList);
		if (arananPersonel.getEkSaha1() != null)
			parametreMap.put(onEk + "pdksPersonel.ekSaha1.id=", arananPersonel.getEkSaha1().getId());
		if (arananPersonel.getEkSaha2() != null)
			parametreMap.put(onEk + "pdksPersonel.ekSaha2.id=", arananPersonel.getEkSaha2().getId());
		if (seciliPersonel.getEkSaha3() != null)
			parametreMap.put(onEk + "pdksPersonel.ekSaha3.id=", arananPersonel.getEkSaha3().getId());
		if (arananPersonel.getEkSaha4() != null)
			parametreMap.put(onEk + "pdksPersonel.ekSaha4.id=", arananPersonel.getEkSaha4().getId());
		if (parametreMap.size() > 1)
			try {
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				TreeMap map = pdksEntityController.getObjectByInnerObjectMapInLogic(parametreMap, class1, Boolean.FALSE);
				if (!map.isEmpty())
					list = new ArrayList<User>(map.values());
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				logger.debug(e.getMessage());
			}

		boolean admin = ortakIslemler.getAdminRole(authenticatedUser);
		TreeMap<Long, User> userMap = new TreeMap<Long, User>();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			User user = (User) iterator.next();
			if (!user.getPdksPersonel().isCalisiyor()) {
				iterator.remove();
				continue;
			}
			user.getPdksPersonel().setCheckBoxDurum(Boolean.FALSE);
			Personel pdksPersonel = user.getPdksPersonel();
			if (!admin && !perNoList.contains(pdksPersonel.getSicilNo()))
				iterator.remove();
			else
				userMap.put(pdksPersonel.getId(), user);

		}
		list.clear();
		if (!userMap.isEmpty()) {
			Date bugun = PdksUtil.getDate(Calendar.getInstance().getTime());
			parametreMap.clear();
			parametreMap.put(PdksEntityController.MAP_KEY_MAP, "getId");
			parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "yoneticisi");
			parametreMap.put("yoneticisi.id", new ArrayList(userMap.keySet()));
			parametreMap.put("sskCikisTarihi>=", bugun);
			parametreMap.put("iseBaslamaTarihi<=", bugun);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			TreeMap<Long, Personel> yoneticiMap = pdksEntityController.getObjectByInnerObjectMapInLogic(parametreMap, Personel.class, false);
			for (Long ld : yoneticiMap.keySet()) {
				if (userMap.containsKey(ld))
					list.add(userMap.get(ld));

			}
			if (!list.isEmpty())
				list = PdksUtil.sortObjectStringAlanList(null, list, "getAdSoyad", null);
		}
		userMap = null;

		setUserList(list);
		return "";
	}

	public void fillGirisEkSahaTanim() {
		visibled = Boolean.FALSE;
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
		setSirketList((List<Sirket>) sonucMap.get("sirketList"));
		setSirketItemList((List<SelectItem>) sonucMap.get("sirketList"));

		User user = new User();
		user.setPdksPersonel(new Personel());
		setArananUser(user);
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		fillGirisEkSahaTanim();
		setSeciliPersonel(new Personel());
		setVisibled(Boolean.TRUE);
		setPersonelArama(Boolean.FALSE);
		setUserArama(Boolean.TRUE);
		setUserList(new ArrayList<User>());
		setPersonelList(new ArrayList<Personel>());
		setPersonelList(new ArrayList<Personel>());
		setRotasyonYoneticiList(new ArrayList<User>());
		setRotasyonList(new ArrayList<PersonelGeciciYonetici>());
		// setReRender("personelListesi");
		List<String> personelNoList = new ArrayList<String>();
		User user = new User();
		user.setPdksPersonel(new Personel());

		setSeciliUser(user);
		fillDepartmanList();
		fillEkSahaTanim();

		if (seciliUser == null && authenticatedUser.isYonetici()) {
			// ortakIslemler.yoneticiIslemleri(authenticatedUser);
			setYonetici(Boolean.TRUE);
			personelNoList = authenticatedUser.getYetkiliPersonelNoList();
			if (!authenticatedUser.isIK() && authenticatedUser.isAdmin())
				setPersonelList(personelBul(personelNoList));
			seciliUser = authenticatedUser;
		}
		// rotasyon yapilacak yonetici listesini bulmak icin yapiyoruz.
		List<String> list = new ArrayList<String>();
		list.add(Role.TIPI_YONETICI);
		setRoleList(list);
		fillKullaniciList(list);
		fillMevcutRotasyonList();

	}

	public String fillKullaniciList(List<String> roleList) {
		List<User> list = new ArrayList<User>();

		String onEk = "";
		Class class1 = User.class;
		ArrayList<String> roller = null;
		HashMap parametreMap = new HashMap();
		parametreMap.put(PdksEntityController.MAP_KEY_MAP, "getStaffId");
		if (roleList != null) {
			class1 = UserRoles.class;
			onEk = "user.";
			roller = new ArrayList<String>();
			for (String roleAdi : roleList)
				roller.add(roleAdi);
			parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "user");
			parametreMap.put("role.rolename", roller);
		}
		Date bugun = PdksUtil.getDate(Calendar.getInstance().getTime());
		parametreMap.put(onEk + "pdksPersonel.sskCikisTarihi>=", bugun);
		parametreMap.put(onEk + "pdksPersonel.iseBaslamaTarihi<=", bugun);
		parametreMap.put(onEk + "durum=", Boolean.TRUE);
		parametreMap.put(onEk + "pdksPersonel.durum=", Boolean.TRUE);

		if (seciliPersonel != null && seciliPersonel.getSirket() != null)
			parametreMap.put(onEk + "pdksPersonel.sirket=", seciliPersonel.getSirket());

		if (parametreMap.size() > 1)
			try {
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				TreeMap map = pdksEntityController.getObjectByInnerObjectMapInLogic(parametreMap, class1, Boolean.FALSE);
				if (!map.isEmpty())
					list = new ArrayList<User>(map.values());
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				logger.debug(e.getMessage());
			}

		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			User user = (User) iterator.next();
			user.getPdksPersonel().setCheckBoxDurum(Boolean.FALSE);
			if (seciliUser != null && user.getId().equals(seciliUser.getId()))
				iterator.remove();
		}

		if (!list.isEmpty())
			list = PdksUtil.sortObjectStringAlanList(null, list, "getAdSoyad", null);

		setRotasyonYoneticiList(list);
		return "";
	}

	public boolean isAramaVisible() {
		return aramaVisible;
	}

	public void setAramaVisible(boolean aramaVisible) {
		this.aramaVisible = aramaVisible;
	}

	@Transactional
	public String delete() {
		// gercek silme islemi yapilmaz. durum fasif hale getirilir.
		PersonelGeciciYonetici rotasyon = getInstance();

		if (PdksUtil.tarihKarsilastirNumeric(new Date(), rotasyon.getBasTarih()) == 1 || rotasyon.getBitTarih().compareTo(new Date()) < 0) {
			facesMessages.add("Geçmiş tarihli bir rotasyonu sistemden silemezsiniz.", "");
		} else {
			if (rotasyon != null) {
				rotasyon.setDurum(Boolean.FALSE);
				pdksEntityController.saveOrUpdate(session, entityManager, rotasyon);
			}
			session.flush();
			fillMevcutRotasyonList();

		}
		return "persisted";
	}

	public Tanim getSeciliDepartman() {
		return seciliDepartman;
	}

	public void setSeciliDepartman(Tanim seciliDepartman) {
		this.seciliDepartman = seciliDepartman;
	}

	public List<String> getRoleList() {
		return roleList;
	}

	public void setRoleList(List<String> roleList) {
		this.roleList = roleList;
	}

	public Sirket getSeciliSirket() {
		return seciliSirket;
	}

	public void setSeciliSirket(Sirket seciliSirket) {
		this.seciliSirket = seciliSirket;
	}

	public List<Tanim> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<Tanim> departmanList) {
		this.departmanList = departmanList;
	}

	public List<SelectItem> getSirketItemList() {
		return sirketItemList;
	}

	public void setSirketItemList(List<SelectItem> sirketItemList) {
		this.sirketItemList = sirketItemList;
	}

	public List<Sirket> getSirketList() {
		return sirketList;
	}

	public void setSirketList(List<Sirket> sirketList) {
		this.sirketList = sirketList;
	}

	public List<User> getUserList() {
		return userList;
	}

	public void setUserList(List<User> userList) {
		this.userList = userList;
	}

	public boolean isUserArama() {
		return userArama;
	}

	public void setUserArama(boolean userArama) {
		this.userArama = userArama;
	}

	public boolean isVisibled() {
		return visibled;
	}

	public void setVisibled(boolean visibled) {
		this.visibled = visibled;
	}

	public boolean isPersonelArama() {
		return personelArama;
	}

	public void setPersonelArama(boolean personelArama) {
		this.personelArama = personelArama;
	}

	public boolean isCheckBox() {
		return checkBox;
	}

	public void setCheckBox(boolean checkBox) {
		this.checkBox = checkBox;
	}

	public boolean isCheckBoxDurum() {
		return checkBoxDurum;
	}

	public void setCheckBoxDurum(boolean checkBoxDurum) {
		this.checkBoxDurum = checkBoxDurum;
	}

	public String getReRender() {
		return reRender;
	}

	public void setReRender(String reRender) {
		this.reRender = reRender;
	}

	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket sirket) {
		this.sirket = sirket;
	}

	public User getSeciliUser() {
		return seciliUser;
	}

	public void setSeciliUser(User seciliUser) {
		this.seciliUser = seciliUser;
	}

	public HashMap<String, List<Tanim>> getEkSahaListMap() {
		return ekSahaListMap;
	}

	public void setEkSahaListMap(HashMap<String, List<Tanim>> ekSahaListMap) {
		this.ekSahaListMap = ekSahaListMap;
	}

	public TreeMap<String, Tanim> getEkSahaTanimMap() {
		return ekSahaTanimMap;
	}

	public void setEkSahaTanimMap(TreeMap<String, Tanim> ekSahaTanimMap) {
		this.ekSahaTanimMap = ekSahaTanimMap;
	}

	public String getLinkAdres() {
		return linkAdres;
	}

	public void setLinkAdres(String linkAdres) {
		this.linkAdres = linkAdres;
	}

	public Personel getSeciliPersonel() {
		return seciliPersonel;
	}

	public void setSeciliPersonel(Personel seciliPersonel) {
		this.seciliPersonel = seciliPersonel;
	}

	public boolean isPdksDepartman() {
		return pdksDepartman;
	}

	public void setPdksDepartman(boolean pdksDepartman) {
		this.pdksDepartman = pdksDepartman;
	}

	public String getAramaTipi() {
		return aramaTipi;
	}

	public void setAramaTipi(String aramaTipi) {
		this.aramaTipi = aramaTipi;
	}

	public User getArananUser() {
		return arananUser;
	}

	public void setArananUser(User arananUser) {
		this.arananUser = arananUser;
	}

	public boolean isSapDepartman() {
		return sapDepartman;
	}

	public void setSapDepartman(boolean sapDepartman) {
		this.sapDepartman = sapDepartman;
	}

	public List<SelectItem> getYoneticiler() {

		if (yoneticiler == null)
			yoneticiler = new ArrayList<SelectItem>();
		else
			yoneticiler.clear();
		List<User> list = new ArrayList<User>(rotasyonYoneticiList);
		if (seciliUser != null)
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				User user = (User) iterator.next();
				if (user.getId().equals(seciliUser.getId()))
					iterator.remove();
				else
					yoneticiler.add(new SelectItem(user.getId(), user.getAdSoyad()));
			}

		return yoneticiler;
	}

	public void setYoneticiler(List<SelectItem> yoneticiler) {
		this.yoneticiler = yoneticiler;
	}

	public Long getYeniYoneticiId() {
		return yeniYoneticiId;
	}

	public void setYeniYoneticiId(Long yeniYoneticiId) {
		this.yeniYoneticiId = yeniYoneticiId;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	String kullaniciIslemleriMailAciklama = "PDKS Sistemi";
	String kullaniciIslemleriMailAdres = "pdks@anadolusaglik.org";

	public User getYeniYonetici() {
		return yeniYonetici;
	}

	public void setYeniYonetici(User yeniYonetici) {
		this.yeniYonetici = yeniYonetici;
	}

	public String getMailAciklamaTarih() {
		return mailAciklamaTarih;
	}

	public void setMailAciklamaTarih(String mailAciklamaTarih) {
		this.mailAciklamaTarih = mailAciklamaTarih;
	}

	public String getMailAciklamaUserList() {
		return mailAciklamaUserList;
	}

	public void setMailAciklamaUserList(String mailAciklamaUserList) {
		this.mailAciklamaUserList = mailAciklamaUserList;
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

	public List<User> getToList() {
		return toList;
	}

	public void setToList(List<User> toList) {
		this.toList = toList;
	}

	public boolean isYonetici() {
		if (seciliUser != null && seciliUser.isYonetici())
			yonetici = Boolean.TRUE;
		return yonetici;
	}

	public void setYonetici(boolean yonetici) {
		this.yonetici = yonetici;
	}
}
