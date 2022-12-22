package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.validator.Max;
import org.hibernate.validator.Min;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.DepartmanDenklestirmeDonemi;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.Kapi;
import org.pdks.entity.KapiView;
import org.pdks.entity.PdksPersonelView;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelHareketIslem;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;

@Name("personelHareketHome")
public class PersonelHareketHome extends EntityHome<HareketKGS> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3323735011423501133L;
	static Logger logger = Logger.getLogger(PersonelHareketHome.class);

	@RequestParameter
	Long personelHareketId;
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false)
	User authenticatedUser;
	@In(required = false, create = true)
	EntityManager entityManager;

	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;
	@In(required = false, create = true)
	PersonelKullaniciAramaHome personelKullaniciAramaHome;;

	@In(required = false, create = true)
	List<User> userList;
	@In(required = false, create = true)
	String linkAdres;
	@In(required = false, create = true)
	VardiyaGun fazlaMesaiVardiyaGun;
	@In(required = false, create = true)
	User seciliUser;
	@In(value = "seciliPersonel", required = false, create = true)
	Personel seciliPersonel;

	@In(value = "seciliPersonelList", required = false, create = true)
	List<Personel> personelList;
	@In(required = false)
	FacesMessages facesMessages;

	List<HareketKGS> hareketList = new ArrayList<HareketKGS>();
	List<SelectItem> kapiList = new ArrayList<SelectItem>();
	private LinkedHashMap<String, HareketKGS> manuelHareketMap;
	private List<SelectItem> manuelHareketList;
	private List<String> saatList = new ArrayList<String>();
	private List<String> dakikaList = new ArrayList<String>();
	private List<Tanim> hareketIslemList = new ArrayList<Tanim>();
	private KapiView manuelGiris = null, manuelCikis = null;
	private DenklestirmeAy pdksDenklestirmeAy = null;
	private VardiyaGun islemVardiyaGun;
	private String islemTipi, donusAdres = "", planKey;
	private boolean denklestirmeAyDurum;

	private List<String> roller;
	private Date tarih;
	@Min(value = 0, message = "Minumum 0 giriniz")
	@Max(value = 23, message = "Maksimum 23 giriniz")
	private int saat;
	@Min(value = 0, message = "Minumum 0 giriniz")
	@Max(value = 59, message = "Maksimum 59 giriniz")
	private int dakika;
	private String DakikaStr, SaatStr, hareketSecim;
	private boolean disabled, terminalDegistir;
	private Session session;
	private AramaSecenekleri aramaSecenekleri = null;
	private Personel fazlaMesaiPersonel;

	@Override
	public Object getId() {
		if (personelHareketId == null) {
			return super.getId();
		} else {
			return personelHareketId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	private void fillEkSahaTanim() {
		ortakIslemler.fillEkSahaTanimAramaSecenekAta(session, Boolean.FALSE, null, aramaSecenekleri);

		if (aramaSecenekleri.getSirketIdList().size() == 1)
			aramaSecenekleri.setSirketId((Long) aramaSecenekleri.getSirketIdList().get(0).getValue());
	}

	/**
	 * @return
	 */
	private TreeMap<Long, KapiView> fillKGSKapiList() {
		TreeMap<Long, KapiView> terminalGirisCikisMap = null;
		TreeMap<Long, TreeMap<Long, KapiView>> terminalMap = null;
		List<KapiView> list = ortakIslemler.fillKapiPDKSList(session);
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			KapiView kapiView = (KapiView) iterator.next();
			if (kapiView.getKapiKGS().isManuel()) {
				if (kapiView.getKapi().isGirisKapi())
					manuelGiris = kapiView;
				else if (kapiView.getKapi().isCikisKapi())
					manuelCikis = kapiView;

			}
		}
		if (islemVardiyaGun != null && islemVardiyaGun.getHareketDurum() == false && islemVardiyaGun.getHareketler() != null) {
			HashMap map1 = new HashMap();
			// map1.put("onaylandi=", Boolean.TRUE);
			map1.put("tipi", Tanim.TIPI_HAREKET_NEDEN);
			map1.put("kodu", "T");
			if (session != null)
				map1.put(PdksEntityController.MAP_KEY_SESSION, session);
			Tanim neden = (Tanim) pdksEntityController.getObjectByInnerObject(map1, Tanim.class);
			if (neden != null) {
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					KapiView kapiView = (KapiView) iterator.next();
					if (kapiView.getTerminalNo() == null)
						continue;
					if (terminalMap == null)
						terminalMap = new TreeMap<Long, TreeMap<Long, KapiView>>();
					Long terminalNo = kapiView.getTerminalNo();
					TreeMap<Long, KapiView> map = terminalMap.containsKey(terminalNo) ? terminalMap.get(terminalNo) : new TreeMap<Long, KapiView>();
					if (map.isEmpty())
						terminalMap.put(terminalNo, map);
					map.put(kapiView.getId(), kapiView);
				}
				if (terminalMap != null) {
					List<Long> terminalList = new ArrayList<Long>(terminalMap.keySet());
					for (Long terminalNo : terminalList) {
						if (terminalMap.get(terminalNo).size() == 2) {
							TreeMap<Long, KapiView> map = terminalMap.get(terminalNo);
							KapiView kapiViewGiris = null, kapiViewCikis = null;
							for (Long key : map.keySet()) {
								KapiView kapiView = map.get(key);
								if (kapiView.getKapi().isGirisKapi())
									kapiViewGiris = kapiView;
								else if (kapiView.getKapi().isCikisKapi())
									kapiViewCikis = kapiView;
							}
							if (kapiViewGiris != null && kapiViewCikis != null) {
								if (terminalGirisCikisMap == null)
									terminalGirisCikisMap = new TreeMap<Long, KapiView>();
								terminalGirisCikisMap.putAll(map);
								kapiViewGiris.setTerminal(kapiViewCikis);
								kapiViewCikis.setTerminal(kapiViewGiris);
							}

						}

					}
					terminalList = null;
					terminalMap = null;
				}
			}
		}
		if (manuelGiris != null && manuelCikis != null && ortakIslemler.getParameterKey("manuelKapiEkleme").equals("1")) {
			list.clear();
			list.add(manuelGiris);
			list.add(manuelCikis);
		}
		List<SelectItem> kapiKGSList = new ArrayList<SelectItem>();
		if (!list.isEmpty()) {
			for (KapiView kapiKGS : list) {
				kapiKGSList.add(new SelectItem(kapiKGS.getId(), kapiKGS.getKapiAciklama()));
			}
		}

		setKapiList(kapiKGSList);
		return terminalGirisCikisMap;
	}

	private Date zamanGuncelle() {

		Date zaman = PdksUtil.setTarih(tarih, Calendar.HOUR_OF_DAY, saat);
		zaman = PdksUtil.setTarih(zaman, Calendar.MINUTE, dakika);
		zaman = PdksUtil.setTarih(zaman, Calendar.SECOND, 0);
		zaman = PdksUtil.setTarih(zaman, Calendar.MILLISECOND, 0);
		return zaman;
	}

	public List<Personel> getSeciliPersoneller() {
		List<Personel> seciliPersoneller = new ArrayList<Personel>();
		if (personelList != null && !personelList.isEmpty()) {

			TreeMap<Long, DepartmanDenklestirmeDonemi> denklestirmeDonemiMap = new TreeMap<Long, DepartmanDenklestirmeDonemi>();

			HashMap<Long, Boolean> map = new HashMap<Long, Boolean>();
			Boolean durum = Boolean.TRUE;
			for (Personel pdksPersonel : personelList) {
				Personel personel = (Personel) pdksPersonel.clone();
				if (map.containsKey(personel.getSirket().getDepartman().getId()))
					durum = map.get(personel.getSirket().getDepartman().getId());
				else {
					durum = !denklestirmeDonemiMap.containsKey(personel.getSirket().getDepartman().getId()) || denklestirmeDonemiMap.get(personel.getSirket().getDepartman().getId()).isAcik();
					map.put(personel.getSirket().getDepartman().getId(), durum);
				}
				personel.setCheckBoxDurum(durum && personel.getPdks());
				seciliPersoneller.add(personel);
			}

		}
		return seciliPersoneller;
	}

	public void fillHareketIslemList() {
		List<Tanim> islemList = ortakIslemler.getTanimList(Tanim.TIPI_HAREKET_NEDEN, session);

		setHareketIslemList(islemList);
	}

	public void fillSaatler() {
		List<String> saatListesi = PdksUtil.getSayilar(0, 24, 1);
		List<String> dakikaListesi = PdksUtil.getSayilar(0, 60, 1);
		setSaatList(saatListesi);
		setDakikaList(dakikaListesi);
	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		terminalDegistir = false;
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		manuelHareketMap = null;
		manuelHareketList = null;
		manuelGiris = null;
		manuelCikis = null;
		if (authenticatedUser.isAdmin() == false || aramaSecenekleri == null)
			aramaSecenekleri = new AramaSecenekleri(authenticatedUser);
		aramaSecenekleri.setSirketId(null);
		aramaSecenekleri.setSessionClear(Boolean.FALSE);
		aramaSecenekleri.setStajyerOlmayanSirket(Boolean.FALSE);
		boolean ayniSayfa = authenticatedUser.getCalistigiSayfa() != null && authenticatedUser.getCalistigiSayfa().equals("personelHareket");
		if (!ayniSayfa)
			authenticatedUser.setCalistigiSayfa("personelHareket");
		pdksDenklestirmeAy = null;
		AramaSecenekleri aramaSecenekleriPer = (AramaSecenekleri) aramaSecenekleri.clone();
		personelKullaniciAramaHome.setAramaSecenekleri(aramaSecenekleriPer);
		personelKullaniciAramaHome.setVisibled(Boolean.FALSE);
		personelKullaniciAramaHome.setSeciliPersonelList(new ArrayList<Personel>());
		personelKullaniciAramaHome.setReRender("hareketGirisForm");
		setHareketList(new ArrayList<HareketKGS>());
		HareketKGS hareket = new HareketKGS();
		hareket.setPersonel(new PersonelView());
		hareket.setKapiView(new KapiView());
		hareket.setIslem(new PersonelHareketIslem());
		setInstance(hareket);

		setTarih(new Date());
		fazlaMesaiPersonel = null;
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		Long perKGSId = null;
		String dateStr = null;
		planKey = (String) req.getParameter("planKey");
		LinkedHashMap<String, Object> veriLastMap = ortakIslemler.getLastParameter("personelHareket", session);
		islemVardiyaGun = null;
		if (planKey != null) {
			if (fazlaMesaiVardiyaGun != null) {
				dateStr = PdksUtil.convertToDateString(fazlaMesaiVardiyaGun.getVardiyaDate(), "yyyyMMdd");
				perKGSId = fazlaMesaiVardiyaGun.getPersonel().getPersonelKGS().getId();
				islemVardiyaGun = (VardiyaGun) fazlaMesaiVardiyaGun.clone();
			}

		} else if (veriLastMap != null) {
			if (authenticatedUser.isAdmin() || authenticatedUser.isIK())
				ortakIslemler.setAramaSecenekleriFromVeriLast(aramaSecenekleri, veriLastMap);
			if (veriLastMap.containsKey("tarih"))
				tarih = PdksUtil.convertToJavaDate((String) veriLastMap.get("tarih"), "yyyy-MM-dd");

		}

		donusAdres = "";
		if (authenticatedUser.isIK() || authenticatedUser.isAdmin()) {
			fillEkSahaTanim();
			fillSirketList();
		} else
			aramaSecenekleri.setSirket(authenticatedUser.getPdksPersonel().getSirket());
		if (dateStr != null) {
			donusAdres = linkAdres;
			Date vardiyaDate = PdksUtil.convertToJavaDate(dateStr, "yyyyMMdd");
			setTarih(vardiyaDate);
			if (perKGSId != null) {
				HashMap parametreMap = new HashMap();
				parametreMap.put("id", perKGSId);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				PersonelKGS personelKGS = (PersonelKGS) pdksEntityController.getObjectByInnerObject(parametreMap, PersonelKGS.class);
				PersonelView personelView = personelKGS != null ? personelKGS.getPersonelView() : null;
				if (personelView != null)
					fazlaMesaiPersonel = personelView.getPdksPersonel();
				hareket.setPersonel(personelView);
				if (personelView != null && personelView.getPdksPersonel() != null) {
					Personel pdksPersonel = personelView.getPdksPersonel();
					Sirket pdksSirket = pdksPersonel.getSirket();
					if (pdksSirket != null) {
						aramaSecenekleri.setSirket(pdksSirket);
						aramaSecenekleri.setSirketId(pdksSirket.getId());
						aramaSecenekleriPer.setSirket(pdksSirket);
						aramaSecenekleriPer.setSirketId(pdksSirket.getId());

					}
					if (pdksPersonel.getEkSaha3() != null) {
						aramaSecenekleri.setEkSaha3Id(pdksPersonel.getEkSaha3().getId());
						aramaSecenekleriPer.setEkSaha3Id(pdksPersonel.getEkSaha3().getId());
					}
					if (pdksPersonel.getTesis() != null) {
						aramaSecenekleri.setTesisId(pdksPersonel.getTesis().getId());
						aramaSecenekleriPer.setTesisId(pdksPersonel.getTesis().getId());
					}
					if (pdksPersonel.getEkSaha1() != null) {
						aramaSecenekleri.setEkSaha1Id(pdksPersonel.getEkSaha1().getId());
						aramaSecenekleriPer.setEkSaha1Id(pdksPersonel.getEkSaha1().getId());
					}
					if (pdksPersonel.getEkSaha4() != null) {
						aramaSecenekleri.setEkSaha4Id(pdksPersonel.getEkSaha4().getId());
						aramaSecenekleriPer.setEkSaha4Id(pdksPersonel.getEkSaha4().getId());
					}
					aramaSecenekleri.setSicilNo(pdksPersonel.getPdksSicilNo());
					aramaSecenekleri.setAd(pdksPersonel.getAd());
					aramaSecenekleri.setSoyad(pdksPersonel.getSoyad());
					aramaSecenekleriPer.setSicilNo(pdksPersonel.getPdksSicilNo());
					aramaSecenekleriPer.setAd(pdksPersonel.getAd());
					aramaSecenekleriPer.setSoyad(pdksPersonel.getSoyad());
				}

			}
			fillHareketList();

		}

		donemBul(tarih);
		if (!ayniSayfa)
			authenticatedUser.setCalistigiSayfa("");
		Boolean kullaniciPersonel = ortakIslemler.getKullaniciPersonel(authenticatedUser);
		if (kullaniciPersonel) {
			PdksUtil.addMessageAvailableWarn("'" + ortakIslemler.getMenuUserAdi(session, "personelHareket") + "' sayfasına giriş yetkiniz yoktur!");
			return MenuItemConstant.home;
		}
		return "";
	}

	public String manuelGuncelle() {
		HareketKGS hareketKGS = getInstance();
		HareketKGS manuel = manuelHareketMap.get(hareketSecim);
		if (manuel != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(manuel.getZaman());
			tarih = PdksUtil.getDate(calendar.getTime());
			saat = calendar.get(Calendar.HOUR_OF_DAY);
			dakika = calendar.get(Calendar.MINUTE);
			hareketKGS.setKapiView(manuel.getKapiView());
			hareketKGS.setKapiId(manuel.getKapiView().getId());
		}
		return "";
	}

	public void fillSirketList() {
		HashMap map = new HashMap();
		map.put("durum", Boolean.TRUE);
		map.put("pdks", Boolean.TRUE);
		if (!(authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()))
			map.put("departman", authenticatedUser.getDepartman());

		map.put(PdksEntityController.MAP_KEY_SESSION, session);
		// List<Sirket> list = pdksEntityController.getObjectByInnerObjectList(map, Sirket.class);
		// ortakIslemler.digerIKSirketBul(list, Boolean.FALSE, session);
		// if (list.size() > 1)
		// list = PdksUtil.sortObjectStringAlanList(list, "getAd", null);
		// setSirketList(list);
	}

	public void ekle(Personel pdksPersonel) {
		boolean devam = Boolean.TRUE;
		Calendar cal = Calendar.getInstance();
		if (pdksPersonel == null)
			pdksPersonel = fazlaMesaiPersonel;
		fillHareketIslemList();
		hareketSecim = "";
		if (pdksPersonel != null) {
			aramaSecenekleri.setSicilNo(pdksPersonel.getPdksSicilNo());
			if (pdksPersonel.getSirket() != null)
				aramaSecenekleri.setSirketId(pdksPersonel.getSirket().getId());
		}
		if (!authenticatedUser.isIK() && !authenticatedUser.isAdmin()) {

			cal.setTime(tarih);
			HashMap map1 = new HashMap();
			// map1.put("onaylandi=", Boolean.TRUE);
			map1.put("denklestirmeAy.durum=", Boolean.FALSE);
			map1.put("denklestirmeAy.ay=", cal.get(Calendar.MONTH) + 1);
			map1.put("denklestirmeAy.yil=", cal.get(Calendar.YEAR));
			map1.put("personel.id=", pdksPersonel.getId());
			if (session != null)
				map1.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelDenklestirme> denklestirmeOnayList = pdksEntityController.getObjectByInnerObjectListInLogic(map1, PersonelDenklestirme.class);
			devam = denklestirmeOnayList.isEmpty();
			denklestirmeOnayList = null;
			map1 = null;
		}
		if (devam) {
			fillKGSKapiList();

			setIslemTipi("E");

			HareketKGS hareket = new HareketKGS();
			hareket.setPersonel(pdksPersonel.getPersonelKGS().getPersonelView());
			hareket.getPersonel().setPdksPersonel(pdksPersonel);
			hareket.setKapiView(new KapiView());

			hareket.setIslem(new PersonelHareketIslem());
			setInstance(hareket);
			cal = Calendar.getInstance();
			setSaat(cal.get(Calendar.HOUR_OF_DAY));
			setDakika(cal.get(Calendar.MINUTE));
			if (manuelHareketMap != null && hareketList.isEmpty() && manuelHareketMap.containsKey("I")) {
				HareketKGS manuel = manuelHareketMap.get("I");
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(manuel.getZaman());
				saat = calendar.get(Calendar.HOUR_OF_DAY);
				dakika = calendar.get(Calendar.MINUTE);
				tarih = PdksUtil.getDate(calendar.getTime());
				hareket.setKapiView(manuel.getKapiView());
			}
		} else {
			setIslemTipi("O");
			PdksUtil.addMessageWarn(pdksPersonel.getAdSoyad() + " ait " + PdksUtil.convertToDateString(tarih, "MMMMM yyyy") + " ayı puantajı onaylanmıştır!");
		}

	}

	public void terminalGuncelle(HareketKGS kgsHareket) {
		setInstance(kgsHareket);
	}

	public void guncelleSil(HareketKGS kgsHareket, String tip) {
		setInstance(kgsHareket);
		fillHareketIslemList();
		if (tip.equals("S") || tip.equals("D") || kgsHareket.getSirket().equals(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_KGS))
			getInstance().setIslem(new PersonelHareketIslem());
		Calendar cal = Calendar.getInstance();
		if (islemVardiyaGun != null)
			tarih = PdksUtil.getDate(kgsHareket.getZaman());
		cal.setTime(kgsHareket.getZaman());
		setSaat(cal.get(Calendar.HOUR_OF_DAY));
		setDakika(cal.get(Calendar.MINUTE));
		setIslemTipi(tip);

	}

	/**
	 * @return
	 */
	@Transactional
	public String terminalDegistir() {
		HareketKGS kgsHareket = getInstance();
		HashMap map1 = new HashMap();
		// map1.put("onaylandi=", Boolean.TRUE);
		map1.put("tipi", Tanim.TIPI_HAREKET_NEDEN);
		map1.put("kodu", "T");
		if (session != null)
			map1.put(PdksEntityController.MAP_KEY_SESSION, session);
		Tanim neden = (Tanim) pdksEntityController.getObjectByInnerObject(map1, Tanim.class);
		if (kgsHareket.getTerminalKapi() != null && neden != null) {
			Long kgsId = 0L, abhId = 0L;
			if (kgsHareket.getId().startsWith(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_KGS))
				kgsId = kgsHareket.getHareketTableId();
			else
				abhId = kgsHareket.getHareketTableId();
			try {
				String aciklama = kgsHareket.getKapiView().getKapi().getAciklama() + " güncellendi.";
				KapiView terminalKapi = terminalKapiManuelUpdate(kgsHareket.getTerminalKapi());
				pdksEntityController.hareketSil(kgsId, abhId, authenticatedUser, neden.getId(), "", session);
				abhId = pdksEntityController.hareketEkle(terminalKapi, kgsHareket.getPersonel(), kgsHareket.getZaman(), authenticatedUser, neden.getId(), aciklama, session);
				session.flush();
				session.clear();
				fillHareketList();
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

		}

		return "";

	}

	/**
	 * @param terminalKapi
	 * @return
	 */
	private KapiView terminalKapiManuelUpdate(KapiView terminalKapi) {
		if (terminalKapi != null && terminalKapi.getKapi() != null) {
			Kapi kapi = terminalKapi.getKapi();
			if (kapi.isGirisKapi() && manuelGiris != null)
				terminalKapi = manuelGiris;
			else if (kapi.isCikisKapi() && manuelCikis != null)
				terminalKapi = manuelCikis;
		}
		return terminalKapi;
	}

	@Transactional
	public String saveGirisCikis() {
		Long abhId = null;
		try {
			HareketKGS kgsHareket = getInstance();
			PersonelView personelView = kgsHareket.getPersonel();
			HashMap parametreMap = new HashMap();
			if (personelView.getId() == null) {
				parametreMap.put("pdksPersonel.id", personelView.getPdksPersonel().getId());
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				PersonelKGS personelKGS = (PersonelKGS) pdksEntityController.getObjectByInnerObject(parametreMap, PersonelKGS.class);
				personelView = personelKGS != null ? personelKGS.getPersonelView() : null;
				kgsHareket.setPersonel(personelView);
			}
			HareketKGS manuelGiris = manuelHareketMap.get("I"), manuelCikis = manuelHareketMap.get("O");
			if (manuelGiris != null && manuelCikis != null) {
				Date bugun = Calendar.getInstance().getTime();
				if (bugun.after(manuelGiris.getZaman())) {
					KapiView terminalKapi = terminalKapiManuelUpdate(manuelGiris.getKapiView());
					abhId = pdksEntityController.hareketEkle(terminalKapi, kgsHareket.getPersonel(), manuelGiris.getZaman(), authenticatedUser, kgsHareket.getIslem().getNeden().getId(), kgsHareket.getIslem().getAciklama(), session);
				}
				if (abhId != null && bugun.after(manuelCikis.getZaman())) {
					KapiView terminalKapi = terminalKapiManuelUpdate(manuelCikis.getKapiView());
					pdksEntityController.hareketEkle(terminalKapi, kgsHareket.getPersonel(), manuelCikis.getZaman(), authenticatedUser, kgsHareket.getIslem().getNeden().getId(), kgsHareket.getIslem().getAciklama(), session);

				}

			}
		} catch (Exception e) {
			logger.info(e);
			e.printStackTrace();
		}
		if (abhId != null)
			fillHareketList();
		return "";
	}

	@Transactional
	public String save() {
		String islem = "";
		HareketKGS kgsHareket = this.getInstance();
		PersonelView personelView = kgsHareket.getPersonel();
		HashMap parametreMap = new HashMap();
		if (personelView.getId() == null) {
			parametreMap.put("pdksPersonel.id", personelView.getPdksPersonel().getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			PersonelKGS personelKGS = (PersonelKGS) pdksEntityController.getObjectByInnerObject(parametreMap, PersonelKGS.class);
			personelView = personelKGS != null ? personelKGS.getPersonelView() : null;
			kgsHareket.setPersonel(personelView);
		}
		Date zaman = zamanGuncelle();
		try {
			if (islemTipi.equals("E") || islemTipi.equals("G")) {
				Date tarih = Calendar.getInstance().getTime();
				tarih = PdksUtil.addTarih(tarih, Calendar.MINUTE, -1);
				if (zaman.getTime() > tarih.getTime()) {
					islemTipi = "";
					PdksUtil.addMessageWarn(PdksUtil.convertToDateString(tarih, "d MMMMM yyyy HH:mm") + " tarihden büyük kayıt giremezsiniz");
				}
			}
			if (authenticatedUser.isAdmin() || !islemTipi.equals("")) {
				Date bugun = Calendar.getInstance().getTime();
				long kgsId = 0, abhId = 0;
				if (islemTipi.equals("E")) {
					if ((kgsHareket.getKapiView() == null || kgsHareket.getKapiView().getId() == null) && kgsHareket.getKapiId() != null) {
						KapiView kapiView = kgsHareket.getKapiView() == null ? new KapiView() : kgsHareket.getKapiView();
						kapiView.setId(kgsHareket.getKapiId());
						kgsHareket.setKapiView(kapiView);
					}
					KapiView terminalKapi = terminalKapiManuelUpdate(kgsHareket.getKapiView());
					if (bugun.after(zaman))
						abhId = pdksEntityController.hareketEkle(terminalKapi, kgsHareket.getPersonel(), zaman, authenticatedUser, kgsHareket.getIslem().getNeden().getId(), kgsHareket.getIslem().getAciklama(), session);
					session.clear();
				} else {

					if (kgsHareket.getId().startsWith(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_KGS))
						kgsId = kgsHareket.getHareketTableId();
					else
						abhId = kgsHareket.getHareketTableId();
					if (islemTipi.equals("G")) {
						if (bugun.after(zaman))
							pdksEntityController.hareketGuncelle(kgsId, abhId, zaman, authenticatedUser, kgsHareket.getIslem().getNeden().getId(), kgsHareket.getIslem().getAciklama(), session);
					} else
						pdksEntityController.hareketSil(kgsId, abhId, authenticatedUser, kgsHareket.getIslem().getNeden().getId(), kgsHareket.getIslem().getAciklama(), session);

					parametreMap.clear();
					if (kgsId != 0)
						parametreMap.put("hareketTableId", HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_KGS + kgsId);
					else
						parametreMap.put("hareketTableId", HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_PDKS + abhId);
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

				}
				session.flush();
				session.clear();
				if (islemVardiyaGun != null)
					tarih = islemVardiyaGun.getVardiyaDate();

				islem = "persist";
			}

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}
		if (islemTipi == null || !islemTipi.equals("")) {

			fillHareketList();
		}

		return islem;
	}

	public void fillHareketList() {
		session.clear();
		TreeMap<Long, KapiView> terminalGirisCikisMap = fillKGSKapiList();
		manuelHakeretDuzenle(false);
		donemBul(tarih);
		fillSaatler();
		fillHareketIslemList();
		if (authenticatedUser.isIK() || authenticatedUser.isAdmin()) {
			fillEkSahaTanim();
			fillSirketList();

		}
		DenklestirmeAy denklestirmeAy = null;
		List<HareketKGS> hareket1List = new ArrayList<HareketKGS>();
		String sicilNo = ortakIslemler.getSicilNo(aramaSecenekleri.getSicilNo());
		if (sicilNo.trim().equals("") && aramaSecenekleri.getSirketId() == null) {
			PdksUtil.addMessageWarn("" + ortakIslemler.sirketAciklama() + " seçiniz!");
		} else {
			saveLastParameter();
			// ArrayList<String> sicilNoList = ortakIslemler.getPersonelSicilNo(ad, soyad, sicilNo, sirket, seciliEkSaha1, seciliEkSaha2, seciliEkSaha3, seciliEkSaha4, Boolean.FALSE, session);
			ArrayList<String> sicilNoList = ortakIslemler.getAramaPersonelSicilNo(aramaSecenekleri, Boolean.FALSE, session);
			terminalDegistir = false;
			if (linkAdres != null && sicilNo != null && !sicilNo.equals("") && !sicilNoList.contains(sicilNo))
				sicilNoList.add(sicilNo);
			if (sicilNoList != null && !sicilNoList.isEmpty()) {
				HashMap parametreMap = new HashMap();

				parametreMap.put("pdksSicilNo", sicilNoList);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<Long> personeller = ortakIslemler.getPersonelViewIdList(pdksEntityController.getObjectByInnerObjectList(parametreMap, PdksPersonelView.class));
				List<HareketKGS> list = new ArrayList<HareketKGS>();
				List<Long> kapiIdler = ortakIslemler.getPdksKapiIdler(session, Boolean.TRUE);

				try {
					parametreMap.clear();

					try {
						if (kapiIdler != null && !kapiIdler.isEmpty()) {
							if (islemVardiyaGun == null)
								list = ortakIslemler.getHareketBilgileri(kapiIdler, personeller, PdksUtil.getDate(tarih), PdksUtil.getDate(PdksUtil.tariheGunEkleCikar(tarih, 1)), HareketKGS.class, session);
							else {
								Vardiya vardiya = islemVardiyaGun.getIslemVardiya();
								Date bitTarih = vardiya.getVardiyaFazlaMesaiBitZaman(), basTarih = vardiya.getVardiyaFazlaMesaiBasZaman();
								if (vardiya.getVardiyaBitZaman().after(bitTarih))
									bitTarih = vardiya.getVardiyaBitZaman();
								list = ortakIslemler.getHareketBilgileri(kapiIdler, personeller, basTarih, bitTarih, HareketKGS.class, session);
							}
						}
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());

						list = new ArrayList<HareketKGS>();

					}
					Calendar cal = Calendar.getInstance();
					if (aramaSecenekleri.getSicilNo() != null && aramaSecenekleri.getSicilNo().trim().length() > 0) {
						if (personeller.size() == 1) {
							HashMap map1 = new HashMap();
							map1.put("personelKGS.id", personeller.get(0));
							if (session != null)
								map1.put(PdksEntityController.MAP_KEY_SESSION, session);
							fazlaMesaiPersonel = (Personel) pdksEntityController.getObjectByInnerObject(map1, Personel.class);
						}
					} else
						fazlaMesaiPersonel = null;

					cal.setTime(tarih);
					HashMap map1 = new HashMap();
					map1.put("ay", cal.get(Calendar.MONTH) + 1);
					map1.put("yil", cal.get(Calendar.YEAR));
					if (session != null)
						map1.put(PdksEntityController.MAP_KEY_SESSION, session);
					denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(map1, DenklestirmeAy.class);
					denklestirmeAyDurum = fazlaMesaiOrtakIslemler.getDurum(denklestirmeAy);
					TreeMap<Long, PersonelDenklestirme> denklestirmeOnayMap = null;
					if (!authenticatedUser.isIK() && !authenticatedUser.isAdmin()) {
						map1.clear();
						map1.put(PdksEntityController.MAP_KEY_MAP, "getPersonelId");
						map1.put("devredenSure<>", null);
						map1.put("onaylandi=", Boolean.TRUE);
						if (denklestirmeAy != null)
							map1.put("denklestirmeAy.id=", denklestirmeAy.getId());
						else
							map1.put("denklestirmeAy.id=", -1L);
						map1.put("personel.id", personeller);
						if (session != null)
							map1.put(PdksEntityController.MAP_KEY_SESSION, session);
						denklestirmeOnayMap = pdksEntityController.getObjectByInnerObjectMapInLogic(map1, PersonelDenklestirme.class, Boolean.FALSE);

					}

					list = PdksUtil.sortListByAlanAdi(list, "zaman", Boolean.FALSE);
					HashMap<String, HareketKGS> fazlaMesaiHareketMap = null;
					if (terminalGirisCikisMap != null) {
						fazlaMesaiHareketMap = new HashMap<String, HareketKGS>();
						// for (HareketKGS kgsHareket : hareketList)
						for (HareketKGS kgsHareket : list)
							fazlaMesaiHareketMap.put(kgsHareket.getId(), kgsHareket);

					}
					HashMap<String, String> hareketMap = new HashMap<String, String>();
					for (Iterator<HareketKGS> iterator = list.iterator(); iterator.hasNext();) {
						HareketKGS kgsHareket = iterator.next();
						if (hareketMap.containsKey(kgsHareket.getId()))
							continue;
						if (kgsHareket.getIslem() != null && kgsHareket.getIslem().getIslemTipi().equals("U") && (kgsHareket.getSirket().equals(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_KGS)))
							continue;
						kgsHareket.setTerminalKapi(null);
						if (fazlaMesaiHareketMap != null && fazlaMesaiHareketMap.containsKey(kgsHareket.getId())) {
							if (terminalGirisCikisMap.containsKey(kgsHareket.getKapiView().getId())) {
								KapiView terminalKapi = terminalKapiManuelUpdate(terminalGirisCikisMap.get(kgsHareket.getKapiView().getId()).getTerminal());
								kgsHareket.setTerminalKapi(terminalKapi);
							} else
								logger.error(kgsHareket.getKapiView().getKapi().getAciklama());
						}
						hareketMap.put(kgsHareket.getId(), kgsHareket.getId());
						HareketKGS hareket = (HareketKGS) kgsHareket.clone();
						Personel personel = hareket.getPersonel() != null ? hareket.getPersonel().getPdksPersonel() : null;
						if (personel == null)
							personel = new Personel();
						boolean puantajOnayDurum = denklestirmeOnayMap != null && denklestirmeOnayMap.containsKey(personel.getId());
						kgsHareket.setPuantajOnayDurum(puantajOnayDurum);
						personel.setCheckBoxDurum(!puantajOnayDurum);
						hareket1List.add(hareket);
					}
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}

			}
		}
		setPdksDenklestirmeAy(denklestirmeAy);
		if (denklestirmeAyDurum && !hareket1List.isEmpty()) {
			boolean hareketEkle = true;
			for (Iterator iterator = hareket1List.iterator(); iterator.hasNext();) {
				HareketKGS hareketKGS = (HareketKGS) iterator.next();
				if (hareketKGS.getHareketEkleDurum())
					hareketEkle = false;
			}
			if (hareketEkle)
				hareket1List.clear();
		}

		setHareketList(hareket1List);

		if (islemVardiyaGun != null)
			islemVardiyaDuzenle();
	}

	/**
	 * 
	 */
	private void saveLastParameter() {
		LinkedHashMap<String, Object> lastMap = new LinkedHashMap<String, Object>();
		if (aramaSecenekleri.getDepartmanId() != null)
			lastMap.put("departmanId", "" + aramaSecenekleri.getDepartmanId());
		if (aramaSecenekleri.getSirketId() != null)
			lastMap.put("sirketId", "" + aramaSecenekleri.getSirketId());
		if (aramaSecenekleri.getTesisId() != null)
			lastMap.put("tesisId", "" + aramaSecenekleri.getTesisId());
		if (aramaSecenekleri.getEkSaha1Id() != null)
			lastMap.put("ekSaha1Id", "" + aramaSecenekleri.getEkSaha1Id());
		if (aramaSecenekleri.getEkSaha2Id() != null)
			lastMap.put("ekSaha2Id", "" + aramaSecenekleri.getEkSaha2Id());
		if (aramaSecenekleri.getEkSaha3Id() != null)
			lastMap.put("ekSaha3Id", "" + aramaSecenekleri.getEkSaha3Id());
		if (aramaSecenekleri.getEkSaha4Id() != null)
			lastMap.put("ekSaha4Id", "" + aramaSecenekleri.getEkSaha4Id());
		if (aramaSecenekleri.getSicilNo() != null && aramaSecenekleri.getSicilNo().trim().length() > 0)
			lastMap.put("sicilNo", "" + aramaSecenekleri.getSicilNo().trim());
		if (aramaSecenekleri.getAd() != null && aramaSecenekleri.getAd().trim().length() > 0)
			lastMap.put("ad", "" + aramaSecenekleri.getAd().trim());
		if (aramaSecenekleri.getSoyad() != null && aramaSecenekleri.getSoyad().trim().length() > 0)
			lastMap.put("soyad", "" + aramaSecenekleri.getSoyad().trim());
		if (tarih != null)
			lastMap.put("tarih", PdksUtil.convertToDateString(tarih, "yyyy-MM-dd"));

		try {
			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception e) {

		}
		lastMap = null;
	}

	/**
	 * @param hareketGetir
	 * @return
	 */
	public String manuelHakeretDuzenle(boolean hareketGetir) {
		if (islemVardiyaGun != null && manuelGiris != null && manuelCikis != null) {
			if (islemVardiyaGun.getVardiyaDate().getTime() != tarih.getTime()) {
				TreeMap<String, VardiyaGun> vardiyaMap = null;
				List<Personel> personeller = new ArrayList<Personel>();
				try {
					personeller.add(islemVardiyaGun.getPersonel());
					vardiyaMap = ortakIslemler.getIslemVardiyalar(personeller, PdksUtil.tariheGunEkleCikar(tarih, -3), PdksUtil.tariheGunEkleCikar(tarih, 3), true, session, Boolean.FALSE);

				} catch (Exception e) {
					vardiyaMap = new TreeMap<String, VardiyaGun>();
					e.printStackTrace();
				}
				if (vardiyaMap != null) {
					List<VardiyaGun> vardiyaList = new ArrayList<VardiyaGun>(vardiyaMap.values());
					for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
						VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
						if (vardiyaGun.getVardiyaDate().getTime() == tarih.getTime()) {
							if (vardiyaGun.getIslemVardiya() == null)
								vardiyaGun.setVardiyaZamani();
							islemVardiyaGun = vardiyaGun;
						}

					}
				}
				if (hareketGetir)
					fillHareketList();
				else if (hareketList != null)
					hareketList.clear();
			}

		} else
			hareketList.clear();

		return "";
	}

	private void islemVardiyaDuzenle() {
		Vardiya islemVardiya = islemVardiyaGun.getIslemVardiya();
		manuelHareketMap = new LinkedHashMap<String, HareketKGS>();

		boolean girisEkle = true, cikisEkle = true, girisMesaiEkle = true, cikisMesaiEkle = true;
		if (hareketList != null) {
			for (HareketKGS hk : hareketList) {
				try {
					Date zaman = hk.getZaman();
					if (hk.getKapiView().getKapi().isGirisKapi()) {
						if (zaman.getTime() >= islemVardiya.getVardiyaTelorans1BasZaman().getTime() && zaman.getTime() <= islemVardiya.getVardiyaTelorans2BasZaman().getTime())
							girisEkle = false;
						if (zaman.getTime() >= islemVardiya.getVardiyaFazlaMesaiBasZaman().getTime() && zaman.getTime() < islemVardiya.getVardiyaTelorans1BasZaman().getTime())
							girisMesaiEkle = false;
					} else if (hk.getKapiView().getKapi().isCikisKapi()) {
						if (zaman.getTime() >= islemVardiya.getVardiyaTelorans1BitZaman().getTime() && zaman.getTime() <= islemVardiya.getVardiyaTelorans2BitZaman().getTime())
							cikisEkle = false;
						if (zaman.getTime() > islemVardiya.getVardiyaTelorans2BitZaman().getTime() && zaman.getTime() <= islemVardiya.getVardiyaFazlaMesaiBitZaman().getTime())
							cikisMesaiEkle = false;
					}
				} catch (Exception e) {

				}
			}
		}
		if (islemVardiya.isCalisma()) {
			Date bugun = Calendar.getInstance().getTime();
			if (girisEkle && bugun.after(islemVardiya.getVardiyaBasZaman())) {
				HareketKGS giris = new HareketKGS();
				giris.setKapiView(manuelGiris);
				giris.setZaman(PdksUtil.getDateTime(islemVardiya.getVardiyaBasZaman()));
				giris.setId("Vardiya Giriş");
				manuelHareketMap.put("I", giris);
			}
			if (cikisEkle && bugun.after(islemVardiya.getVardiyaBitZaman())) {
				HareketKGS cikis = new HareketKGS();
				cikis.setKapiView(manuelCikis);
				Date zaman = PdksUtil.getDateTime(islemVardiya.getVardiyaBitZaman());
				if (islemVardiya.isCalisma())
					zaman = PdksUtil.addTarih(zaman, Calendar.SECOND, -1);
				cikis.setZaman(zaman);
				cikis.setId("Vardiya Çıkış");
				manuelHareketMap.put("O", cikis);
			}
			if (girisMesaiEkle && bugun.after(islemVardiya.getVardiyaFazlaMesaiBasZaman())) {
				HareketKGS girisMesai = new HareketKGS();
				girisMesai.setKapiView(manuelGiris);
				Date zaman = PdksUtil.getDateTime(islemVardiya.getVardiyaFazlaMesaiBasZaman());
				girisMesai.setZaman(zaman);
				girisMesai.setId("Fazla Mesai Giriş");
				manuelHareketMap.put("IM", girisMesai);
			}
			if (cikisMesaiEkle && bugun.after(islemVardiya.getVardiyaFazlaMesaiBitZaman())) {
				HareketKGS cikisMesai = new HareketKGS();
				cikisMesai.setKapiView(manuelCikis);
				Date zaman = PdksUtil.getDateTime(islemVardiya.getVardiyaFazlaMesaiBitZaman());
				if (islemVardiya.isCalisma())
					zaman = PdksUtil.addTarih(zaman, Calendar.SECOND, -1);

				cikisMesai.setZaman(zaman);
				cikisMesai.setId("Fazla Mesai Çıkış");
				manuelHareketMap.put("OM", cikisMesai);
			}
		}
		manuelHareketList = new ArrayList<SelectItem>();
		if (!PdksUtil.isSistemDestekVar())
			manuelHareketMap.clear();
		if (!manuelHareketMap.isEmpty()) {
			for (String str : manuelHareketMap.keySet())
				manuelHareketList.add(new SelectItem(str, manuelHareketMap.get(str).getId()));
		}

	}

	private void donemBul(Date tar) {
		if (tar != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(tar);
			HashMap fields = new HashMap();
			fields.put("yil", cal.get(Calendar.YEAR));
			fields.put("ay", cal.get(Calendar.MONTH) + 1);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			pdksDenklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);

		} else
			pdksDenklestirmeAy = null;

	}

	public List<User> getSeciliUserList() {
		return userList;
	}

	public List<Personel> getSeciliPersonelList() {
		List<Personel> list = new ArrayList<Personel>();
		if (personelList != null)
			list.addAll(personelList);
		for (Iterator<Personel> iterator = list.iterator(); iterator.hasNext();) {
			Personel pdksPersonel = iterator.next();
			pdksPersonel.setCheckBoxDurum(Boolean.TRUE);
			if (!pdksPersonel.getPdks() || PdksUtil.tarihKarsilastirNumeric(pdksPersonel.getIseGirisTarihi(), tarih) == 1 || PdksUtil.tarihKarsilastirNumeric(tarih, pdksPersonel.getSonCalismaTarihi()) == 1)
				pdksPersonel.setCheckBoxDurum(Boolean.FALSE);

		}
		return list;
	}

	public User getUser() {
		return seciliUser;
	}

	public List<String> getRoller() {
		return roller;
	}

	public void setRoller(List<String> roller) {
		this.roller = roller;
	}

	public Date getTarih() {
		return tarih;
	}

	public void setTarih(Date tarih) {
		this.tarih = tarih;
	}

	public List<HareketKGS> getHareketList() {
		return hareketList;
	}

	public void setHareketList(List<HareketKGS> hareketList) {
		this.hareketList = hareketList;
	}

	public List<SelectItem> getKapiList() {
		return kapiList;
	}

	public void setKapiList(List<SelectItem> kapiList) {
		this.kapiList = kapiList;
	}

	public List<String> getSaatList() {
		return saatList;
	}

	public void setSaatList(List<String> saatList) {
		this.saatList = saatList;
	}

	public List<String> getDakikaList() {
		return dakikaList;
	}

	public void setDakikaList(List<String> dakikaList) {
		this.dakikaList = dakikaList;
	}

	public String getDakikaStr() {
		DakikaStr = "" + dakika;
		if (dakika < 10)
			DakikaStr = "0" + dakika;

		return DakikaStr;
	}

	public void setDakikaStr(String dakikaStr) {
		DakikaStr = dakikaStr;
	}

	public String getSaatStr() {
		SaatStr = "" + saat;
		if (saat < 10)
			SaatStr = "0" + saat;
		return SaatStr;
	}

	public void setSaatStr(String saatStr) {
		SaatStr = saatStr;
	}

	public List<Tanim> getHareketIslemList() {
		return hareketIslemList;
	}

	public void setHareketIslemList(List<Tanim> hareketIslemList) {
		this.hareketIslemList = hareketIslemList;
	}

	public String getIslemTipi() {
		return islemTipi;
	}

	public void setIslemTipi(String islemTipi) {
		this.islemTipi = islemTipi;
	}

	public int getDakika() {
		return dakika;
	}

	public void setDakika(int dakika) {
		this.dakika = dakika;
	}

	public int getSaat() {
		return saat;
	}

	public void setSaat(int saat) {
		this.saat = saat;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getDonusAdres() {
		return donusAdres;
	}

	public void setDonusAdres(String donusAdres) {
		this.donusAdres = donusAdres;
	}

	public DenklestirmeAy getPdksDenklestirmeAy() {
		return pdksDenklestirmeAy;
	}

	public void setPdksDenklestirmeAy(DenklestirmeAy pdksDenklestirmeAy) {
		this.pdksDenklestirmeAy = pdksDenklestirmeAy;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public AramaSecenekleri getAramaSecenekleri() {
		return aramaSecenekleri;
	}

	public void setAramaSecenekleri(AramaSecenekleri aramaSecenekleri) {
		this.aramaSecenekleri = aramaSecenekleri;
	}

	public String getPlanKey() {
		return planKey;
	}

	public void setPlanKey(String planKey) {
		this.planKey = planKey;
	}

	public Personel getFazlaMesaiPersonel() {
		return fazlaMesaiPersonel;
	}

	public void setFazlaMesaiPersonel(Personel fazlaMesaiPersonel) {
		this.fazlaMesaiPersonel = fazlaMesaiPersonel;
	}

	public boolean isTerminalDegistir() {
		return terminalDegistir;
	}

	public void setTerminalDegistir(boolean terminalDegistir) {
		this.terminalDegistir = terminalDegistir;
	}

	public LinkedHashMap<String, HareketKGS> getManuelHareketMap() {
		return manuelHareketMap;
	}

	public void setManuelHareketMap(LinkedHashMap<String, HareketKGS> manuelHareketMap) {
		this.manuelHareketMap = manuelHareketMap;
	}

	public String getHareketSecim() {
		return hareketSecim;
	}

	public void setHareketSecim(String hareketSecim) {
		this.hareketSecim = hareketSecim;
	}

	public KapiView getManuelGiris() {
		return manuelGiris;
	}

	public void setManuelGiris(KapiView manuelGiris) {
		this.manuelGiris = manuelGiris;
	}

	public KapiView getManuelCikis() {
		return manuelCikis;
	}

	public void setManuelCikis(KapiView manuelCikis) {
		this.manuelCikis = manuelCikis;
	}

	public List<SelectItem> getManuelHareketList() {
		return manuelHareketList;
	}

	public void setManuelHareketList(List<SelectItem> manuelHareketList) {
		this.manuelHareketList = manuelHareketList;
	}

	public VardiyaGun getIslemVardiyaGun() {
		return islemVardiyaGun;
	}

	public void setIslemVardiyaGun(VardiyaGun islemVardiyaGun) {
		this.islemVardiyaGun = islemVardiyaGun;
	}

	public boolean isDenklestirmeAyDurum() {
		return denklestirmeAyDurum;
	}

	public void setDenklestirmeAyDurum(boolean denklestirmeAyDurum) {
		this.denklestirmeAyDurum = denklestirmeAyDurum;
	}
}
