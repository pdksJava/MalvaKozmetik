package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.mail.internet.InternetAddress;
import javax.persistence.EntityManager;
import javax.ws.rs.HttpMethod;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.CalismaModeli;
import org.pdks.entity.Departman;
import org.pdks.entity.Dosya;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.KapiSirket;
import org.pdks.entity.Liste;
import org.pdks.entity.MailGrubu;
import org.pdks.entity.NoteTipi;
import org.pdks.entity.Notice;
import org.pdks.entity.PdksPersonelView;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDinamikAlan;
import org.pdks.entity.PersonelExtra;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.VardiyaSablonu;
import org.pdks.security.entity.DefaultPasswordGenerator;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.pdks.security.entity.UserMenuItemTime;
import org.pdks.security.entity.UserRoles;
import org.pdks.security.entity.UserTesis;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.pdks.webservice.MailObject;
import com.pdks.webservice.MailPersonel;
import com.pdks.webservice.MailStatu;
import com.pdks.webservice.PdksSoapVeriAktar;
import com.pdks.webservice.PersonelERP;

@Name("pdksPersonelHome")
public class PdksPersonelHome extends EntityHome<Personel> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8547356626329930627L;
	/**
	 * 
	 */
	static Logger logger = Logger.getLogger(PdksPersonelHome.class);
	@RequestParameter
	Long kapiId;

	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = true, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	HashMap<String, String> parameterMap;

	@In(create = true)
	Renderer renderer;

	public static final String MAIL_CC = MailGrubu.TIPI_CC;
	public static final String MAIL_BCC = MailGrubu.TIPI_BCC;
	public static final String MAIL_HAREKET = MailGrubu.TIPI_HAREKET;
	private Dosya personelDosya = new Dosya();
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private HashMap<Long, List<Tanim>> dinamikPersonelAciklamaMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private List<Tanim> bolumDepartmanlari, gorevDepartmanlari;
	private List<Tanim> dinamikDurumList, dinamikSayisalList, dinamikAciklamaList;
	private List<PersonelDinamikAlan> dinamikPersonelDurumList, dinamikPersonelSayisalList, dinamikPersonelAciklamaList;

	private PersonelDinamikAlan personelDinamikAlan;
	private HashMap<String, Boolean> personelDurumMap = new HashMap<String, Boolean>();

	private TreeMap<String, PersonelDinamikAlan> personelDinamikMap;
	private PersonelView personelView;
	private List<Departman> departmanTanimList = new ArrayList<Departman>(), departmanKullaniciList = new ArrayList<Departman>();
	private List<Tanim> unvanTanimList = new ArrayList<Tanim>(), departmanPDKSTanimList = new ArrayList<Tanim>(), cinsiyetList, masrafYeriList, tesisList, yoneticiVardiyaTipiList, vardiyaGirisTipiTanimList = new ArrayList<Tanim>();
	private List<PersonelERP> personelERPList;
	private List<VardiyaSablonu> sablonlar = new ArrayList<VardiyaSablonu>(), sablonIskurList = new ArrayList<VardiyaSablonu>();
	private List<PersonelKGS> personelKGSList = new ArrayList<PersonelKGS>();
	private List<PersonelView> tanimsizPersonelList = new ArrayList<PersonelView>();
	private List<Personel> pdksPersonelList = new ArrayList<Personel>(), yoneticiList, yonetici2List, ikinciYoneticiHataliList;
	private List<Sirket> sirketList = new ArrayList<Sirket>();
	private List<UserMenuItemTime> menuItemTimeList;
	private List<PersonelView> personelList = new ArrayList<PersonelView>();
	private List<Liste> dosyaTanimList = new ArrayList<Liste>();

	private List<CalismaModeli> calismaModeliList = new ArrayList<CalismaModeli>();

	private List<PersonelERP> personelERPReturnList;

	private List<Role> distinctRoleList = new ArrayList<Role>();
	private List<Tanim> distinctTesisList = new ArrayList<Tanim>();

	private List<String> ccAdresList = null, bccAdresList = null, hareketAdresList = null;
	private String adi, soyadi, sicilNo, yoneticiTipi, ccAdres, adresTipi, sanalPersonelAciklama, ePosta, denemeMesaj;
	private String bolumAciklama, departmanAciklama, kartNoAciklama, altBolumAciklama, sirketKodu = "";
	private User user, eskiKullanici;
	private Date tarih;
	private Tanim bosDepartman, parentDepartman, parentBordroTanim;
	private String oldUserName, bosDepartmanKodu;

	private PersonelIzin bakiyeIzin;
	private Double bakiyeIzinSuresi;
	private Sirket oldSirket;
	private Personel asilYonetici1;
	private String hataMesaj = "", personelERPGuncelleme = "";
	private Boolean pdks, servisCalisti = Boolean.FALSE, fazlaMesaiIzinKullan = Boolean.FALSE, gebeMi = Boolean.FALSE, tesisYetki = Boolean.FALSE, istenAyrilmaGoster = Boolean.FALSE;
	private Boolean sutIzni = Boolean.FALSE, kimlikNoGoster = Boolean.FALSE, kullaniciPersonel = Boolean.FALSE, sanalPersonel = Boolean.FALSE, icapDurum = Boolean.FALSE, yoneticiRolVarmi = Boolean.FALSE;
	private Boolean ustYonetici = Boolean.FALSE, fazlaMesaiOde = Boolean.FALSE, suaOlabilir = Boolean.FALSE, egitimDonemi = Boolean.FALSE, partTimeDurum = Boolean.FALSE, tesisDurum = Boolean.FALSE;
	private Boolean emailCCDurum = Boolean.FALSE, emailBCCDurum = Boolean.FALSE, taseronKulaniciTanimla = Boolean.FALSE, manuelTanimla = Boolean.FALSE, ikinciYoneticiManuelTanimla = Boolean.FALSE;
	private Boolean onaysizIzinKullanilir = Boolean.FALSE, departmanGoster = Boolean.FALSE, kartNoGoster = Boolean.FALSE, ikinciYoneticiIzinOnayla = Boolean.FALSE, izinGirisiVar = Boolean.FALSE, dosyaGuncellemeYetki = Boolean.FALSE;
	private Boolean ekSaha1Disable, ekSaha2Disable, ekSaha4Disable, transferAciklamaCiftKontrol;
	private PersonelExtra personelExtra;
	private TreeMap<Long, PersonelKGS> personelKGSMap;
	private int COL_SICIL_NO, COL_ADI, COL_SOYADI, COL_SIRKET_KODU, COL_SIRKET_ADI, COL_TESIS_KODU, COL_TESIS_ADI, COL_GOREV_KODU, COL_GOREVI, COL_BOLUM_KODU, COL_BOLUM_ADI;
	private int COL_ISE_BASLAMA_TARIHI, COL_KIDEM_TARIHI, COL_GRUBA_GIRIS_TARIHI, COL_ISTEN_AYRILMA_TARIHI, COL_DOGUM_TARIHI, COL_CINSIYET_KODU, COL_CINSIYET, COL_YONETICI_KODU, COL_YONETICI2_KODU;
	private int COL_DEPARTMAN_KODU, COL_DEPARTMAN_ADI, COL_MASRAF_YERI_KODU, COL_MASRAF_YERI_ADI, COL_BORDRO_ALT_ALAN_KODU, COL_BORDRO_ALT_ALAN_ADI, COL_BORDRO_SANAL_PERSONEL;
	private Session session;

	@Override
	public Object getId() {
		if (kapiId == null) {
			return super.getId();
		} else {
			return kapiId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	/**
	 * @param alan
	 * @return
	 */
	public String durumGebeSutIzni(String alan) {
		Personel per = getInstance();
		if (per.getCinsiyetBayan()) {
			Boolean durumu = false;
			if (alan.equalsIgnoreCase("G"))
				durumu = per.getGebeMi();
			else if (alan.equalsIgnoreCase("S"))
				durumu = per.getSutIzni();
			if (durumu != null && durumu) {
				if (alan.equalsIgnoreCase("G"))
					per.setSutIzni(Boolean.FALSE);
				else if (alan.equalsIgnoreCase("S"))
					per.setGebeMi(Boolean.FALSE);
			}
		}
		return "";
	}

	/**
	 * @param alan
	 * @param pdksPersonel
	 * @return
	 */
	public PersonelDinamikAlan getPersonelDinamikAlan(Tanim alan, Personel pdksPersonel) {
		PersonelDinamikAlan dinamikAlan = null;
		if (personelDinamikMap != null && alan != null && pdksPersonel != null) {
			String key = PersonelDinamikAlan.getKey(pdksPersonel, alan);
			if (personelDinamikMap.containsKey(key))
				dinamikAlan = personelDinamikMap.get(key);
		}
		if (dinamikAlan == null)
			dinamikAlan = new PersonelDinamikAlan(pdksPersonel, alan);
		return dinamikAlan;
	}

	/**
	 * @return
	 */
	public String adresGuncelle() {
		Personel personel = getInstance();
		if (adresTipi != null) {
			if (adresTipi.equals(MAIL_CC)) {
				String sb = adresDuzelt(ccAdresList);
				personel.setEmailCC(sb);
			} else if (adresTipi.equals(MAIL_BCC)) {
				String sb = adresDuzelt(bccAdresList);
				personel.setEmailBCC(sb);
			} else if (adresTipi.equals(MAIL_HAREKET)) {
				String sb = adresDuzelt(hareketAdresList);
				personel.setHareketMail(sb);
			}
		}

		ccAdres = "";
		return "";

	}

	public String sablonDegisti() {
		Personel personel = getInstance();
		if (personel.getSablon() != null) {
			VardiyaSablonu sablon = personel.getSablon();
			if (sablon.getCalismaModeli() != null)
				personel.setCalismaModeli(sablon.getCalismaModeli());
		}
		return "";
	}

	public String modelDegisti() {
		Personel personel = getInstance();
		if (personel.getCalismaModeli() != null) {
			CalismaModeli calismaModeli = personel.getCalismaModeli();
			if (calismaModeli.getBagliVardiyaSablonu() != null)
				personel.setSablon(calismaModeli.getBagliVardiyaSablonu());
		}
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

	public String adresSil(String adres) {
		if (adresTipi.equals(MAIL_CC)) {
			if (ccAdresList.contains(adres))
				ccAdresList.remove(adres);

		} else if (adresTipi.equals(MAIL_BCC)) {
			if (bccAdresList.contains(adres))
				bccAdresList.remove(adres);

		} else if (adresTipi.equals(MAIL_HAREKET)) {
			if (hareketAdresList.contains(adres))
				hareketAdresList.remove(adres);

		}
		return "";

	}

	public String adresEkle() {
		String eMail = PdksUtil.setTurkishStr(ccAdres.trim()).toLowerCase(Locale.ENGLISH);
		List<String> adresler = null;
		if (adresTipi != null) {
			if (adresTipi.equals(MAIL_CC)) {
				if (ccAdresList == null)
					ccAdresList = new ArrayList<String>();
				adresler = ccAdresList;
			}

			else if (adresTipi.equals(MAIL_BCC)) {
				if (bccAdresList == null)
					bccAdresList = new ArrayList<String>();
				adresler = bccAdresList;
			} else if (adresTipi.equals(MAIL_HAREKET)) {
				if (hareketAdresList == null)
					hareketAdresList = new ArrayList<String>();
				adresler = hareketAdresList;
			}
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
					PdksUtil.addMessageError(ccAdres + " listede var!");
				else {
					adresler.add(eMail);
					ccAdres = "";
				}
			} catch (Exception e) {
				logger.error("ASM Pdks hata in : \n");
				e.printStackTrace();
				logger.error("ASM Pdks hata out : " + e.getMessage());
				PdksUtil.addMessageError(ccAdres + " hatalı adres!");
			}
		}

		return "";

	}

	public String adresAyarla(String adres, String xAdresTipi) {
		ccAdres = "";
		setAdresTipi(xAdresTipi);
		if (!PdksUtil.hasStringValue(adres)) {
			if (xAdresTipi.equals(MAIL_CC))
				adres = getInstance().getEmailCC();
			else if (xAdresTipi.equals(MAIL_BCC))
				adres = getInstance().getEmailBCC();
			else if (xAdresTipi.equals(MAIL_HAREKET))
				adres = getInstance().getHareketMail();
		}
		List<String> adresler = new ArrayList<String>();
		if (adres != null && adres.indexOf("@") > 0) {
			String separator = PdksUtil.SEPARATOR_MAIL;
			if (adres.indexOf(separator) < 0 && adres.indexOf(",") > 0)
				separator = ",";
			if (adres.indexOf(separator) > 0)
				adresler.addAll(Arrays.asList(adres.split(separator)));
			else
				adresler.add(adres);
		}
		if (xAdresTipi.equals(MAIL_CC))
			ccAdresList = adresler;
		else if (xAdresTipi.equals(MAIL_BCC))
			bccAdresList = adresler;
		else if (xAdresTipi.equals(MAIL_HAREKET))
			hareketAdresList = adresler;
		return "";

	}

	public void fillPdksCalismaModeliList() {
		try {
			Personel pdksPersonel = getInstance();
			if (calismaModeliList != null)
				calismaModeliList.clear();
			else
				calismaModeliList = new ArrayList<CalismaModeli>();
			if (pdksPersonel.getSirket() != null) {
				Departman pdksDepartman = pdksPersonel.getSirket().getDepartman();
				HashMap parametreMap = new HashMap();
				parametreMap.put("durum", Boolean.TRUE);
				parametreMap.put("toplamGunGuncelle", Boolean.FALSE);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				calismaModeliList = pdksEntityController.getObjectByInnerObjectList(parametreMap, CalismaModeli.class);
				for (Iterator iterator = calismaModeliList.iterator(); iterator.hasNext();) {
					CalismaModeli cm = (CalismaModeli) iterator.next();
					if (pdksDepartman != null && cm.getDepartman() != null && !cm.getDepartman().getId().equals(pdksDepartman.getId()))
						iterator.remove();
				}
				if (calismaModeliList.size() == 1)
					pdksPersonel.setCalismaModeli(calismaModeliList.get(0));
			} else if (pdksPersonel.getId() == null) {
				pdksPersonel.setSablon(null);
				pdksPersonel.setCalismaModeli(null);
			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.error("fillPdksCalismaModeliList Hata : " + e.getMessage());
		}

	}

	public void fillPdksVardiyaSablonList() {
		List<VardiyaSablonu> sablonList = null;
		try {
			Personel pdksPersonel = getInstance();
			Departman pdksDepartman = pdksPersonel.getSirket() != null ? pdksPersonel.getSirket().getDepartman() : null;
			sablonlar.clear();
			if (sablonIskurList == null)
				sablonIskurList = new ArrayList<VardiyaSablonu>();
			else
				sablonIskurList.clear();
			if (pdksDepartman != null) {
				HashMap parametreMap = new HashMap();
				parametreMap.put("durum", Boolean.TRUE);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				sablonList = pdksEntityController.getObjectByInnerObjectList(parametreMap, VardiyaSablonu.class);
				for (Iterator iterator = sablonList.iterator(); iterator.hasNext();) {
					VardiyaSablonu vardiyaSablonu = (VardiyaSablonu) iterator.next();
					if (vardiyaSablonu.getDepartman() != null && !pdksDepartman.getId().equals(vardiyaSablonu.getDepartman().getId()))
						iterator.remove();
					else if (pdksPersonel.isSanalPersonelMi() && vardiyaSablonu.isIsKurMu())
						sablonIskurList.add(vardiyaSablonu);
				}
				if (!sablonList.isEmpty()) {
					sablonlar.addAll(sablonList);
					boolean degistir = pdksPersonel.getId() == null;
					if (!degistir) {
						VardiyaSablonu sablon = pdksPersonel.getSablon();
						degistir = true;
						for (VardiyaSablonu vardiyaSablonu : sablonList) {
							if (sablon.getId().equals(vardiyaSablonu.getId()))
								degistir = false;
						}
					}
					if (degistir) {
						VardiyaSablonu sablon = null;
						if (sablonlar.size() == 1) {
							sablon = sablonlar.get(0);
						} else if (pdksPersonel.getSablon() == null) {
							for (VardiyaSablonu vardiyaSablonu : sablonList) {
								if (vardiyaSablonu.getBeyazYakaDefault() != null && vardiyaSablonu.getBeyazYakaDefault())
									sablon = vardiyaSablonu;

							}
						}
						if (sablon != null) {
							pdksPersonel.setSablon(sablon);
							sablonDegisti();
						}
					}

				}
			} else
				sablonList = new ArrayList<VardiyaSablonu>();
			if (sablonIskurList.size() == 1 && pdksPersonel.getIsKurVardiyaSablonu() == null && pdksPersonel.isCalisiyor()) {
				pdksPersonel.setIsKurVardiyaSablonu(sablonIskurList.get(0));
				savePersonel(pdksPersonel);

			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.error("fillPdksVardiyaSablonList Hata : " + e.getMessage());
		}
		sablonList = null;

	}

	/**
	 * @param pdksPersonel
	 */
	private void savePersonel(Personel pdksPersonel) {
		if (pdksPersonel.getId() != null) {
			pdksEntityController.saveOrUpdate(session, entityManager, pdksPersonel);
			session.flush();
		}
	}

	public void ldapKontrol() {
		setHataMesaj("");
		Personel pdksPersonel = getInstance();
		User user = ortakIslemler.digerKullanici(pdksPersonel.getKullanici(), oldUserName, session);
		if (user != null) {
			setHataMesaj(user.getUsername() + " " + user.getAdSoyad() + " ait kullanÄ±cÄ±dÄ±r");
			// pdksUtil.mesajYaz(user.getUsername() + " " + user.getAdSoyad() +
			// " ait kullanÄ±cÄ±dÄ±r");
			pdksPersonel.getKullanici().setUsername(oldUserName);

		} else if (pdksPersonel.getKullanici() != null && pdksPersonel.getSirket() != null && pdksPersonel.getSirket().isLdap()) {
			user = pdksPersonel.getKullanici();
			user.setPdksPersonel(pdksPersonel);
			String email = user.getEmailFromUserName();
			if (email != null)
				user.setEmail(email);

		}

	}

	@Transactional
	public String detaysizSave() {

		Personel pdksPersonel = getInstance();
		String ok = "";
		if (pdksPersonel.getId() != null) {
			pdksPersonel.setGuncelleyenUser(authenticatedUser);
			pdksPersonel.setGuncellemeTarihi(new Date());
		} else {
			if (PdksUtil.hasStringValue(pdksPersonel.getAd()))
				pdksPersonel.setAd(pdksPersonel.getAd().toUpperCase(Constants.TR_LOCALE));
			if (PdksUtil.hasStringValue(pdksPersonel.getSoyad()))
				pdksPersonel.setSoyad(pdksPersonel.getSoyad().toUpperCase(Constants.TR_LOCALE));
			pdksPersonel.setOlusturanUser(authenticatedUser);
		}

		pdksPersonel.setPdksSicilNo(pdksPersonel.getPersonelKGS().getSicilNo());
		session.save(pdksPersonel);
		session.flush();
		session.refresh(personelView);
		return ok;

	}

	public void istenAyrilmaTarihiUzat() {
		getInstance().setIstenAyrilisTarihi(PdksUtil.getSonSistemTarih());
	}

	@Transactional
	public String saveKullanici() {
		Personel pdksPersonel = getInstance();
		User kullaniciYeni = pdksPersonel.getKullanici();
		if ((kullaniciYeni == null || kullaniciYeni.getId() == null) && eskiKullanici != null && pdksPersonel.getId() != null) {
			eskiKullanici.setPdksPersonel(pdksPersonel);
			eskiKullanici.setDurum(pdksPersonel.isCalisiyor());
			pdksEntityController.saveOrUpdate(session, entityManager, eskiKullanici);
			if (eskiKullanici.isDurum()) {
				pdksPersonel.setGuncellemeTarihi(new Date());
				pdksPersonel.setGuncelleyenUser(authenticatedUser);
				pdksEntityController.saveOrUpdate(session, entityManager, pdksPersonel);
			}
			session.flush();
			fillPersonelKGSList();
		}
		return "";
	}

	@Transactional
	public String personelDegistir(PersonelView personelView) {
		if (personelView != null) {
			try {
				Personel personel = personelView.getPdksPersonel();
				if (personel != null && personelKGSMap.containsKey(personel.getPersonelKGS().getId())) {
					PersonelKGS personelKGS = personelKGSMap.get(personel.getPersonelKGS().getId());
					personel.setPersonelKGS(personelKGS);
					if (!authenticatedUser.isAdmin()) {
						personel.setGuncellemeTarihi(new Date());
						personel.setGuncelleyenUser(authenticatedUser);
					}
					pdksEntityController.saveOrUpdate(session, entityManager, personel);
					session.flush();
					fillPersonelKGSList();
				}
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
		}
		return "";
	}

	@Transactional
	public String save() {
		Personel pdksPersonel = getInstance();
		Sirket sirket = pdksPersonel.getSirket();
		String str = ortakIslemler.getParameterKey("izinERPUpdate");
		boolean izinERPUpdate = str.equals("1");
		if (pdksPersonel.getSanalPersonel() != null && pdksPersonel.getSanalPersonel() && pdksPersonel.getIseBaslamaTarihi() != null) {
			if (pdksPersonel.getIzinHakEdisTarihi() == null)
				pdksPersonel.setIzinHakEdisTarihi(pdksPersonel.getIseBaslamaTarihi());
			if (pdksPersonel.getGrubaGirisTarihi() == null)
				pdksPersonel.setGrubaGirisTarihi(pdksPersonel.getIseBaslamaTarihi());
		}
		if (pdksPersonel.getId() == null && sirket != null) {
			if (sirket.isErp() == false) {
				if (pdksPersonel.getIseBaslamaTarihi() == null)
					pdksPersonel.setIseBaslamaTarihi(pdksPersonel.getGrubaGirisTarihi() == null ? pdksPersonel.getIzinHakEdisTarihi() : pdksPersonel.getGrubaGirisTarihi());
				if (pdksPersonel.getGrubaGirisTarihi() == null)
					pdksPersonel.setGrubaGirisTarihi(pdksPersonel.getIzinHakEdisTarihi());
			} else {
				if (sirket.isPdksMi() && pdksPersonel.getTesis() == null && tesisList != null && tesisList.size() == 1)
					pdksPersonel.setTesis(tesisList.get(0));
			}
		}

		ArrayList<String> mesajList = new ArrayList<String>();
		if (authenticatedUser.isAdmin() && pdksPersonel.getGrubaGirisTarihi() != null && PdksUtil.tarihKarsilastirNumeric(pdksPersonel.getGrubaGirisTarihi(), pdksPersonel.getIseBaslamaTarihi()) == 1)
			mesajList.add("İşe Giriş Tarihi Grubu Giriş Tarihinden önce olamaz");
		if (PdksUtil.tarihKarsilastirNumeric(pdksPersonel.getIseBaslamaTarihi(), pdksPersonel.getSonCalismaTarihi()) == 1)
			mesajList.add("İşe Giriş Tarihi İşten Ayrılma Tarihinden önce olamaz");
		if (izinGirisiVar && PdksUtil.tarihKarsilastirNumeric(pdksPersonel.getDogumTarihi(), pdksPersonel.getIzinHakEdisTarihi()) == 1)
			mesajList.add("Doğum Tarihi " + ortakIslemler.kidemBasTarihiAciklama() + "den önce olamaz");
		StringBuffer kullaniciRolMesaj = pdksPersonel.getKullanici() != null ? kullaniciRolKontrolMesaji(pdksPersonel.getKullanici(), true) : null;
		if (kullaniciRolMesaj != null)
			mesajList.add(kullaniciRolMesaj.toString());
		kullaniciRolMesaj = null;

		User kullanici = pdksPersonel.getKullanici();
		if (kullanici != null)
			PdksUtil.setUserYetki(kullanici);
		List<Role> yeniRoller = kullanici != null && kullanici.getYetkiliRollerim() != null ? new ArrayList<Role>(kullanici.getYetkiliRollerim()) : new ArrayList<Role>();
		List<Tanim> yeniTesisler = new ArrayList<Tanim>();
		if (kullanici != null && tesisYetki && (kullanici.isIK() || kullanici.isDirektorSuperVisor()) && kullanici.getYetkiliTesisler() != null)
			yeniTesisler.addAll(kullanici.getYetkiliTesisler());
		User user = null;
		String ok = "";

		try {
			boolean yeni = pdksPersonel.getId() == null;
			boolean kullaniciYaz = PdksUtil.hasStringValue(kullanici.getUsername());
			if (kullaniciYaz) {
				if (PdksUtil.isValidEMail(kullanici.getEmail()))
					user = ortakIslemler.digerKullanici(kullanici, getOldUserName(), session);
				else
					mesajList.add("Geçersiz e-posta adresi --> " + kullanici.getEmail());
			}

			if (user == null) {
				if (!yeni) {
					pdksPersonel.setGuncelleyenUser(authenticatedUser);
					pdksPersonel.setGuncellemeTarihi(new Date());
				} else {
					if (pdksPersonel.getIseBaslamaTarihi() == null)
						pdksPersonel.setIseBaslamaTarihi(pdksPersonel.getIzinHakEdisTarihi());
					if (pdksPersonel.getGrubaGirisTarihi() == null)
						pdksPersonel.setGrubaGirisTarihi(pdksPersonel.getIzinHakEdisTarihi());
					pdksPersonel.setOlusturanUser(authenticatedUser);
					if (sirket != null && !sirket.isErp()) {
						if (PdksUtil.hasStringValue(pdksPersonel.getAd()))
							pdksPersonel.setAd(pdksPersonel.getAd().toUpperCase(Constants.TR_LOCALE));
						if (PdksUtil.hasStringValue(pdksPersonel.getSoyad()))
							pdksPersonel.setSoyad(pdksPersonel.getSoyad().toUpperCase(Constants.TR_LOCALE));
					}
				}

				if (pdksPersonel.getId() == null || authenticatedUser.isAdmin())
					pdksPersonel.setPdksSicilNo(pdksPersonel.getPersonelKGS().getSicilNo());
				if (asilYonetici1 != null && sirket != null) {
					if (!sirket.isErp()) {
						pdksPersonel.setAsilYonetici1(asilYonetici1);
					} else {
						if (asilYonetici1 == null && pdksPersonel.getAsilYonetici1() != null)
							asilYonetici1 = pdksPersonel.getAsilYonetici1();
					}
					pdksPersonel.setYoneticisi(asilYonetici1);

				} else if (pdksPersonel.getAsilYonetici1() != null) {
					pdksPersonel.setYoneticisi(pdksPersonel.getAsilYonetici1());
					// pdksPersonel.setAsilYonetici1(null);
				} else if (sirket != null && !sirket.isErp()) {
					if (pdksPersonel.getYoneticisi() != null && pdksPersonel.getYoneticisi().getSirket().isErp())
						pdksPersonel.setAsilYonetici2(pdksPersonel.getYoneticisi());
					else
						pdksPersonel.setAsilYonetici2(null);
				}

				if (pdksPersonel.getSonCalismaTarihi() == null)
					pdksPersonel.setIstenAyrilisTarihi(PdksUtil.getSonSistemTarih());
				try {
					if (pdksPersonel.getIcapciOlabilir() && !sirket.getDepartman().getIcapciOlabilir())
						pdksPersonel.setIcapciOlabilir(Boolean.FALSE);

					if (kullaniciYaz) {
						ortakIslemler.setUserRoller(kullanici, session);
						if (kullanici.isProjeMuduru() && pdksPersonel.getYoneticisi() == null) {
							pdksPersonel.setYoneticisi(pdksPersonel);
							pdksPersonel.setAsilYonetici1(pdksPersonel);
						}
					}
					if (!pdksPersonel.getCinsiyetBayan()) {
						pdksPersonel.setGebeMi(Boolean.FALSE);
						pdksPersonel.setSutIzni(Boolean.FALSE);
					}
					if (pdksPersonel.getEmailCC() != null) {
						String emailCC = ortakIslemler.getPersonelCCMail(pdksPersonel);
						if (emailCC != null)
							pdksPersonel.setEmailCC(emailCC);
					}
					if (pdksPersonel.getEmailBCC() != null) {
						String emailBCC = ortakIslemler.getPersonelBCCMail(pdksPersonel);
						if (emailBCC != null)
							pdksPersonel.setEmailBCC(emailBCC);
					}
					if (pdksPersonel.getHareketMail() != null) {
						String hareketMail = ortakIslemler.getPersonelHareketMail(pdksPersonel);
						if (hareketMail != null)
							pdksPersonel.setHareketMail(hareketMail);
					}
					if (mesajList.isEmpty()) {

						ortakIslemler.personelKaydet(pdksPersonel, session);
						if (pdksPersonel.getFazlaMesaiIzinKullan())
							pdksPersonel.setFazlaMesaiOde(Boolean.FALSE);
						if (pdksPersonel.getCalismaModeli() != null && pdksPersonel.getCalismaModeli().isFazlaMesaiVarMi() == false) {
							pdksPersonel.setFazlaMesaiOde(false);
							pdksPersonel.setFazlaMesaiIzinKullan(false);
						}
						pdksEntityController.saveOrUpdate(session, entityManager, pdksPersonel);
						if (mesajList.isEmpty()) {
							try {
								for (PersonelDinamikAlan pda : dinamikPersonelDurumList) {
									if (pda.getId() != null || pda.isDurumSecili()) {
										pda.setPersonel(pdksPersonel);
										pdksEntityController.saveOrUpdate(session, entityManager, pda);
									}
								}
								for (PersonelDinamikAlan pda : dinamikPersonelAciklamaList) {
									if (pda.getId() != null || pda.getTanimDeger() != null) {
										pda.setPersonel(pdksPersonel);
										pdksEntityController.saveOrUpdate(session, entityManager, pda);
									}
								}
								for (PersonelDinamikAlan pda : dinamikPersonelSayisalList) {
									if (pda.getId() != null || pda.getSayisalDeger() != null) {
										pda.setPersonel(pdksPersonel);
										pdksEntityController.saveOrUpdate(session, entityManager, pda);
									}
								}
							} catch (Exception epda) {
							}

						}

						if (personelExtra != null)
							pdksEntityController.saveOrUpdate(session, entityManager, personelExtra);
					}
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());

				}
				try {

					if (mesajList.isEmpty() && kullaniciYaz) {
						if (pdksPersonel.getGrubaGirisTarihi() == null || !sirket.getDepartman().isAdminMi())
							pdksPersonel.setGrubaGirisTarihi(pdksPersonel.getIseBaslamaTarihi());
						kullanici.setPdksPersonel(pdksPersonel);
						if (kullanici.isYeniSifre() || kullanici.getId() == null || getOldSirket().isLdap() != sirket.isLdap()) {
							if (!sirket.isLdap() || kullanici.isYeniSifre())
								kullaniciSifreOlustur(kullanici);
							else
								kullanici.setPasswordHash("");
						}
						ortakIslemler.setUserRoller(kullanici, session);
						if (kullanici.getYetkiliRollerim() != null && kullanici.getYetkiliRollerim().isEmpty())
							kullanici = ortakIslemler.personelPdksRolAta(kullanici, Boolean.FALSE, session);
						pdksEntityController.saveOrUpdate(session, entityManager, kullanici);
						HashMap<Long, UserRoles> roller = new HashMap<Long, UserRoles>();
						HashMap<Long, UserTesis> tesisler = new HashMap<Long, UserTesis>();
						HashMap map = new HashMap();
						map.put("user.id", kullanici.getId());
						if (authenticatedUser.isAdmin() == false)
							map.put("role.adminRole", Boolean.FALSE);
						if (session != null)
							map.put(PdksEntityController.MAP_KEY_SESSION, session);
						List<UserRoles> yetkiliRoller = pdksEntityController.getObjectByInnerObjectList(map, UserRoles.class);
						for (Iterator iterator = yetkiliRoller.iterator(); iterator.hasNext();) {
							UserRoles userRoles = (UserRoles) iterator.next();
							roller.put(userRoles.getRole().getId(), userRoles);
						}
						map.clear();
						map.put("user.id", kullanici.getId());
						if (session != null)
							map.put(PdksEntityController.MAP_KEY_SESSION, session);
						List<UserTesis> yetkiliTesisler = pdksEntityController.getObjectByInnerObjectList(map, UserTesis.class);
						for (Iterator iterator = yetkiliTesisler.iterator(); iterator.hasNext();) {
							UserTesis userTesis = (UserTesis) iterator.next();
							tesisler.put(userTesis.getTesis().getId(), userTesis);
						}

						if (yeniRoller != null) {
							for (Iterator iterator = yeniRoller.iterator(); iterator.hasNext();) {
								Role role = (Role) iterator.next();
								if (roller.containsKey(role.getId())) {
									iterator.remove();
									roller.remove(role.getId());
								} else {
									UserRoles userRoles = new UserRoles();
									userRoles.setRole(role);
									userRoles.setUser(kullanici);
									pdksEntityController.saveOrUpdate(session, entityManager, userRoles);
								}

							}
							yeniRoller = null;
						}
						if (yeniTesisler != null) {
							for (Iterator iterator = yeniTesisler.iterator(); iterator.hasNext();) {
								Tanim tesis = (Tanim) iterator.next();
								if (tesisler.containsKey(tesis.getId())) {
									iterator.remove();
									tesisler.remove(tesis.getId());
								} else {
									UserTesis userTesis = new UserTesis();
									userTesis.setTesis(tesis);
									userTesis.setUser(kullanici);
									pdksEntityController.saveOrUpdate(session, entityManager, userTesis);
								}

							}
							yeniTesisler = null;
						}
						if (tesisler != null && !tesisler.isEmpty()) {
							yetkiliTesisler = new ArrayList<UserTesis>(tesisler.values());
							for (Iterator iterator = yetkiliTesisler.iterator(); iterator.hasNext();) {
								UserTesis userTesis = (UserTesis) iterator.next();

								pdksEntityController.deleteObject(session, entityManager, userTesis);

							}
						}
						if (roller != null && !roller.isEmpty()) {
							yetkiliRoller = new ArrayList<UserRoles>(roller.values());
							for (Iterator iterator = yetkiliRoller.iterator(); iterator.hasNext();) {
								UserRoles userRoles = (UserRoles) iterator.next();
								pdksEntityController.deleteObject(session, entityManager, userRoles);

							}
						}
						tesisler = null;
						roller = null;

					}

					if (mesajList.isEmpty() && !izinERPUpdate) {
						if (bakiyeIzin != null) {
							if (!sirket.getDepartman().isAdminMi()) {
								if (bakiyeIzin.getId() != null) {
									if (bakiyeIzinSuresi.doubleValue() != bakiyeIzin.getIzinSuresi().doubleValue()) {
										bakiyeIzin.setIzinSuresi(bakiyeIzinSuresi);
										bakiyeIzin.setGuncelleyenUser(authenticatedUser);
										bakiyeIzin.setGuncellemeTarihi(new Date());
										pdksEntityController.saveOrUpdate(session, entityManager, bakiyeIzin);
									}

								} else if (bakiyeIzinSuresi != null && bakiyeIzinSuresi.doubleValue() != 0.0d) {
									bakiyeIzin.setIzinSuresi(bakiyeIzinSuresi);
									bakiyeIzin.setOlusturanUser(authenticatedUser);
									bakiyeIzin.setOlusturmaTarihi(new Date());
									bakiyeIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
									// entityManager.persist();
									pdksEntityController.saveOrUpdate(session, entityManager, bakiyeIzin);
								}
							} else if (bakiyeIzin.getId() != null && bakiyeIzin.getIzinDurumu() != PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL) {
								bakiyeIzin.setIzinSuresi(0d);
								bakiyeIzin.setOlusturanUser(authenticatedUser);
								bakiyeIzin.setOlusturmaTarihi(new Date());
								bakiyeIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL);
								// entityManager.persist();
								pdksEntityController.saveOrUpdate(session, entityManager, bakiyeIzin);
							}
						}
					}
					if (mesajList.isEmpty()) {
						saveIkinciYoneticiOlmazList();

						session.flush();
						try {
							session.refresh(personelView);
						} catch (Exception e) {
							logger.error("PDKS hata in : \n");
							e.printStackTrace();
							logger.error("PDKS hata out : " + e.getMessage());
						}
						fillPersonelKGSList();
					}
					ok = "persisted";
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());

					PdksUtil.addMessageError(e.getMessage());
				}

			} else
				mesajList.add(user.getUsername() + " " + user.getPdksPersonel().getPdksSicilNo() + " sicil numaralı kullanıcısı " + user.getAdSoyad() + " personel'de tanımlıdır!");
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			mesajList.add("Hata : " + e.getMessage());
		}
		if (!mesajList.isEmpty()) {
			ok = "";
			for (String mesaj : mesajList)
				PdksUtil.addMessageWarn(mesaj);

		}
		return ok;

	}

	/**
	 * @param kullanici
	 */
	private void kullaniciSifreOlustur(User kullanici) {
		String encodePassword = DefaultPasswordGenerator.generate(8);
		if (kullanici.getId() == null)
			encodePassword = "123456";
		kullanici.setNewPassword(encodePassword);
		setUser(kullanici);
		try {
			MailObject mailObject = new MailObject();
			mailObject.setSubject("Şifre Gönderme");
			String body = "<p>Sayın " + kullanici.getMailPersonel().getAdiSoyadi() + ",</p><p>Kullanıcı şifreniz aşağıda belirtildiği şekilde " + (kullanici.getId() == null ? "tanımlanmıştır" : "değiştirilmiştir.") + " İlk kullanımda değiştirmenizi öneririz.</p>";
			body += "<p>Kullanıcı Adı : " + kullanici.getUsername() + " --->   Şifre   : " + encodePassword + "</p>";
			body += "<p>Saygılarımla,</p>";
			Map<String, String> map = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
			String donusAdres = map.containsKey("host") ? map.get("host") : "";
			if (PdksUtil.hasStringValue(donusAdres))
				body += "<p><a href=\"http://" + donusAdres + "/sifreDegistirme\">" + ortakIslemler.getParameterKey("fromName") + " uygulamasına girmek için buraya tıklayınız.</a></p>";

			mailObject.setBody(body);
			mailObject.getToList().add(kullanici.getMailPersonel());
			boolean durum = false;
			String mesaj = "";
			try {
				MailStatu ms = ortakIslemler.mailSoapServisGonder(false, mailObject, renderer, "/mail.xhtml", session);
				if (ms != null) {
					durum = ms.isDurum();
					if (!durum)
						mesaj = ms.getHataMesai();
					if (mesaj == null)
						mesaj = "Hata oluştu!";
				}
			} catch (Exception e) {

			}
			if (durum)
				PdksUtil.addMessageInfo("Mesaj Gönderildi.");
			else
				PdksUtil.addMessageWarn("Mesaj Gönderilemedi." + mesaj);

		} catch (Exception e) {
			logger.info(kullanici.getUsername() + " --> " + encodePassword);
		}

		kullanici.setPasswordHash(PdksUtil.encodePassword(encodePassword));
	}

	/**
	 * @return
	 */
	public String erpVeriGuncelle() {
		erpVeriGuncelle(null, getInstance(), Boolean.FALSE);
		return "";
	}

	/**
	 * @param personelView
	 */
	public void kayitSapGuncelle(PersonelView personelView) {
		Personel personel = personelView.getPdksPersonel();
		erpVeriGuncelle(personelView, personel, Boolean.TRUE);
	}

	/**
	 * @param personelView
	 * @param personel
	 * @param update
	 */
	private void erpVeriGuncelle(PersonelView personelView, Personel personel, Boolean update) {

		if (personel != null && personel.getSirket().isErp()) {
			try {
				boolean guncellendi = ortakIslemler.sapVeriGuncelle(session, authenticatedUser, null, null, personel, null, update, Boolean.FALSE, Boolean.TRUE);
				if (guncellendi) {
					if (update) {
						session.flush();

						PdksUtil.addMessageInfo(personel.getAdSoyad() + " SAP'den verileri güncellendi.");
						if (personelView != null)
							session.refresh(personelView);
					}
				} else
					PdksUtil.addMessageWarn(personel.getAdSoyad() + " SAP'den verileri güncellenmedi!");

			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				PdksUtil.addMessageError(personel.getAdSoyad() + " SAP'den verileri güncellenemedi!" + e.getMessage());
			}
		}
	}

	public String denemeMesajHazirla() {
		Personel personel = getInstance();
		if (ePosta == null || ePosta.indexOf("@") < 2)
			ePosta = personel.getKullanici().getEmail();
		return "";

	}

	public String denemeMesajGonder() {
		try {
			Personel personel = getInstance();
			if (denemeMesaj.indexOf("\t") > 0)
				denemeMesaj = PdksUtil.replaceAll(denemeMesaj, "\t", "");
			if (denemeMesaj.indexOf("\n") > 0)
				denemeMesaj = PdksUtil.replaceAll(denemeMesaj, "\n", "<br/>");
			MailStatu mailStatu = null;
			if (PdksUtil.isValidEMail(ePosta)) {
				Notice uyariNot = ortakIslemler.getNotice(NoteTipi.MAIL_CEVAPLAMAMA.value(), Boolean.TRUE, session);
				MailObject mailObject = new MailObject();
				mailObject.setSubject("Test mail");
				if (uyariNot != null)
					denemeMesaj += uyariNot.getValue();
				mailObject.setBody(denemeMesaj);
				MailPersonel mailUser = new MailPersonel();
				mailUser.setEPosta(ePosta);
				mailUser.setAdiSoyadi(personel.getAdSoyad());
				mailObject.getToList().add(mailUser);
				if (!ePosta.equals(authenticatedUser.getEmail()))
					mailObject.getBccList().add(authenticatedUser.getMailPersonel());
				try {
					mailStatu = ortakIslemler.mailSoapServisGonder(false, mailObject, renderer, "/email/testKullaniciMail.xhtml", session);

				} catch (Exception e) {
					mailStatu = new MailStatu();
					if (e.getMessage() != null)
						mailStatu.setHataMesai(e.getMessage());
					else
						mailStatu.setHataMesai("Hata oluştu!");
				}

				if (mailStatu != null) {
					if (mailStatu.isDurum())
						PdksUtil.addMessageInfo(ePosta + " test mesajı gönderilmiştir.");
					else
						PdksUtil.addMessageAvailableWarn(mailStatu.getHataMesai());
				}
			} else
				PdksUtil.addMessageWarn(ePosta + " geçersiz email!");
			// ortakIslemler.mailGonder(renderer, "/email/testKullaniciMail.xhtml");
			// PdksUtil.addMessageInfo(ePosta + " test mesajı gönderilmiştir.");
		} catch (Exception e) {
			PdksUtil.addMessageError(ePosta + " " + e);
			logger.error("Hata  --> " + ePosta);
		}
		if (denemeMesaj.indexOf("<br/>") > 0)
			denemeMesaj = PdksUtil.replaceAll(denemeMesaj, "<br/>", "\n");
		return "";

	}

	/**
	 * @param pdksPersonel
	 */
	private void izinGirisDurum(Personel pdksPersonel) {
		izinGirisiVar = Boolean.FALSE;
		if (pdksPersonel.getSirket() != null) {
			HashMap fields = new HashMap();
			fields.put("departman.id=", pdksPersonel.getSirket().getDepartman().getId());
			fields.put("personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
			fields.put("durum=", Boolean.TRUE);
			fields.put("bakiyeIzinTipi=", null);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<IzinTipi> list = pdksEntityController.getObjectByInnerObjectListInLogic(fields, IzinTipi.class);
			izinGirisiVar = !list.isEmpty();
			list = null;
		}
	}

	/**
	 * @param personelView
	 * @param pdksDurum
	 */
	public void kayitGuncelle(PersonelView personelView, boolean pdksDurum) {
		parentBordroTanim = null;
		izinGirisiVar = Boolean.FALSE;
		HashMap fields = new HashMap();
		denemeMesaj = "Deneme mesajı gönderilmiştir.";
		ePosta = "";
		eskiKullanici = null;
		kartNoAciklama = ortakIslemler.getParameterKey("kartNoAciklama");
		Departman pdksDepartman = null;
		ikinciYoneticiManuelTanimla = Boolean.FALSE;
		bosDepartman = null;
		bosDepartmanKodu = ortakIslemler.getParameterKey("bosDepartmanKodu");
		Personel pdksPersonel = personelView.getPdksPersonel();
		if (pdksPersonel == null) {
			pdksPersonel = new Personel();
			personelView.setPdksPersonel(pdksPersonel);
		}

		if (PdksUtil.hasStringValue(bosDepartmanKodu)) {
			if (parentDepartman == null) {
				fields.put("tipi", Tanim.TIPI_PERSONEL_EK_SAHA);
				fields.put("kodu", "ekSaha1");
				fields.put("durum", Boolean.TRUE);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				parentDepartman = (Tanim) pdksEntityController.getObjectByInnerObject(fields, Tanim.class);
			}
			if (parentDepartman != null) {
				fields.clear();
				fields.put("parentTanim.id", parentDepartman.getId());
				fields.put("tipi", Tanim.TIPI_PERSONEL_EK_SAHA_ACIKLAMA);
				fields.put("kodu", bosDepartmanKodu);
				fields.put("durum", Boolean.FALSE);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				bosDepartman = (Tanim) pdksEntityController.getObjectByInnerObject(fields, Tanim.class);
			}

		}

		try {
			if (pdksPersonel != null && !pdksPersonel.getSirket().getDepartman().isAdminMi()) {
				pdksDepartman = pdksPersonel.getSirket().getDepartman();
			}
			if (pdksDepartman == null && authenticatedUser.isIK() && !authenticatedUser.isIKAdmin())
				pdksDepartman = authenticatedUser.getDepartman();
			bolumDepartmanlari = ortakIslemler.getBolumDepartmanlari(pdksDepartman, session);
			gorevDepartmanlari = ortakIslemler.getGorevDepartmanlari(pdksDepartman, session);
		} catch (Exception e) {

		}

		asilYonetici1 = null;
		if (pdksPersonel != null && pdksPersonel.getAsilYonetici1() != null) {
			if (pdksPersonel.getSirket().isErp()) {
				if (pdksPersonel.getYoneticisi() != null && !pdksPersonel.getYoneticisi().getId().equals(pdksPersonel.getAsilYonetici1().getId()))
					asilYonetici1 = pdksPersonel.getYoneticisi();
			} else
				asilYonetici1 = pdksPersonel.getYoneticisi();
		}
		setHataMesaj("");
		setPersonelView(personelView);

		setInstance(pdksPersonel);
		userMenuList(personelView.getKullanici());

		Sirket sirket = pdksPersonel != null && pdksPersonel.getId() != null ? pdksPersonel.getSirket() : null;
		User kullaniciPer = personelView.getKullanici();
		onaysizIzinKullanilir = Boolean.FALSE;
		ikinciYoneticiIzinOnayla = Boolean.FALSE;
		if (sirket != null && pdksPersonel.isCalisiyor() && (kullaniciPer == null || kullaniciPer.getId() == null)) {
			LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
			veriMap.put("personelId", pdksPersonel.getId());
			StringBuffer sb = new StringBuffer("SP_FIND_OLD_USER");
			if (session != null)
				veriMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<User> kullanicilar = null;
			try {
				kullanicilar = pdksEntityController.execSPList(veriMap, sb, User.class);
			} catch (Exception e1) {
			}
			if (kullanicilar != null) {
				for (Iterator iterator = kullanicilar.iterator(); iterator.hasNext();) {
					User user = (User) iterator.next();
					if (!user.getPdksPersonel().isCalisiyor()) {
						eskiKullanici = user;
						break;
					}
				}
				kullanicilar = null;
			}
		}
		personelExtra = null;
		// List<IzinTipi> izinTipiList = getOnaysizIzinDurum(sirket);
		TreeMap<String, Boolean> map1 = getOnaysizIzinDurumMap(sirket);
		onaysizIzinKullanilir = map1.containsKey("onaysizIzinDurum");
		ikinciYoneticiIzinOnayla = map1.containsKey("ikinciYoneticiIzinOnaySec");

		if (sirket != null) {
			if (sirket.getDepartman().isAdminMi() == false)
				ikinciYoneticiManuelTanimla = ikinciYoneticiIzinOnayla || sirket.getDepartman().isFazlaMesaiTalepGirer();
			else if (!ortakIslemler.getParameterKey("yonetici2ERPKontrol").equals("1"))
				ikinciYoneticiManuelTanimla = ikinciYoneticiIzinOnayla || sirket.getDepartman().isFazlaMesaiTalepGirer();

			if (sirket.getDepartman().isAdminMi()) {
				fields.clear();
				fields.put("personel", pdksPersonel);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				personelExtra = (PersonelExtra) pdksEntityController.getObjectByInnerObject(fields, PersonelExtra.class);
				if (personelExtra == null) {
					personelExtra = new PersonelExtra();
					personelExtra.setPersonel(pdksPersonel);
				}
			}
		}

		setOldSirket(sirket);
		setOldUserName(personelView.getKullanici() != null ? personelView.getKullanici().getUsername() : null);
		PersonelIzin izin = null;
		try {
			if (ortakIslemler.getParameterKey("gecmisBakiyeIzin").equals("1"))
				izin = ortakIslemler.getPersonelBakiyeIzin(0, authenticatedUser, pdksPersonel, session);
			setBakiyeIzinSuresi(izin != null ? izin.getIzinSuresi() : null);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			izin = null;
		}

		setBakiyeIzin(izin);

		PersonelKGS personelKGS = personelView.getPersonelKGS();
		if (pdksPersonel != null && sirket != null && !pdksPersonel.getSirket().isErp())
			try {
				fillPdksPersonelList();
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();

			}
		User kullanici = null;
		if (pdksPersonel.getPersonelKGS() == null) {
			kullanici = new User();
			if (!authenticatedUser.isAdmin())
				kullanici.setDepartman(authenticatedUser.getDepartman());
			kullanici.setDurum(Boolean.FALSE);
			pdksPersonel.setPersonelKGS(personelKGS);
			pdksPersonel.setAd(personelKGS.getAd());
			pdksPersonel.setSoyad(personelKGS.getSoyad());
			pdksPersonel.setDurum(personelKGS.getDurum());
			pdksPersonel.setDogumTarihi(personelKGS.getDogumTarihi());
			pdksPersonel.setIseBaslamaTarihi(personelKGS.getIseBaslamaTarihi());
			pdksPersonel.setGrubaGirisTarihi(personelKGS.getIseBaslamaTarihi());
			pdksPersonel.setIzinHakEdisTarihi(personelKGS.getIseBaslamaTarihi());
		} else {

			if (personelView.getKullanici() == null) {
				kullanici = new User();
				if (!authenticatedUser.isAdmin())
					kullanici.setDepartman(authenticatedUser.getDepartman());
				kullanici.setDurum(Boolean.FALSE);
				kullanici.setYeniSifre(Boolean.FALSE);
			} else {
				kullanici = personelView.getKullanici();
			}

		}
		if (kullanici != null) {
			if (kullanici.getId() != null)
				PdksUtil.setUserYetki(kullanici);
			pdksPersonel.setKullanici(kullanici);
			// logger.info(tesisYetki + " " + kullanici.getId());
		}
		if (pdksPersonel.getCalismaModeli() == null && calismaModeliList.size() == 1) {
			pdksPersonel.setCalismaModeli(calismaModeliList.get(0));
			savePersonel(pdksPersonel);
		}
		if (pdksPersonel.getId() == null) {
			pdksPersonel.setAd(pdksPersonel.getAd().toUpperCase(Constants.TR_LOCALE));
			pdksPersonel.setSoyad(pdksPersonel.getSoyad().toUpperCase(Constants.TR_LOCALE));
			pdksPersonel.setPdksSicilNo(personelView.getSicilNo());
			pdksPersonel.setPdks(Boolean.TRUE);
			pdksPersonel.setMailTakip(Boolean.TRUE);
		}
		if (pdksPersonel.getKullanici() != null) {
			pdksPersonel.getKullanici().setYeniSifre(Boolean.FALSE);
		}
		String emailCC = ortakIslemler.getAktifMailAdress(pdksPersonel.getEmailCC(), session);
		String emailBCC = ortakIslemler.getAktifMailAdress(pdksPersonel.getEmailBCC(), session);
		String hareketMail = ortakIslemler.getAktifMailAdress(pdksPersonel.getHareketMail(), session);
		if (PdksUtil.hasStringValue(emailCC)) {
			pdksPersonel.setEmailCC(emailCC);
			adresAyarla(null, MAIL_CC);
			String yeniAdres = adresDuzelt(ccAdresList);
			if (!yeniAdres.equals(emailCC)) {
				pdksPersonel.setEmailCC(yeniAdres);
			}
		}
		if (PdksUtil.hasStringValue(emailBCC)) {
			pdksPersonel.setEmailBCC(emailBCC);
			adresAyarla(null, MAIL_BCC);
			String yeniAdres = adresDuzelt(bccAdresList);
			if (!yeniAdres.equals(emailBCC)) {
				pdksPersonel.setEmailBCC(yeniAdres);
			}
		}
		if (PdksUtil.hasStringValue(hareketMail)) {
			pdksPersonel.setHareketMail(hareketMail);
			adresAyarla(null, MAIL_HAREKET);
			String yeniAdres = adresDuzelt(hareketAdresList);
			if (!yeniAdres.equals(hareketMail)) {
				pdksPersonel.setHareketMail(yeniAdres);
			}
		}
		fillPersonelTablolar(pdksDurum);

		if (pdksPersonel.getKullanici() != null && pdksPersonel.getKullanici().getId() == null && departmanKullaniciList != null && departmanKullaniciList.size() == 1)
			pdksPersonel.getKullanici().setDepartman(departmanTanimList.get(0));
		tesisYetki = ortakIslemler.getParameterKey("tesisYetki").equals("1");
		fillDistinctRoleList();
		if (tesisYetki)
			fillDistinctTesisList();

		dinamikPersonelDurumList.clear();
		dinamikPersonelSayisalList.clear();
		dinamikPersonelAciklamaList.clear();
		dinamikPersonelAciklamaMap.clear();
		if (pdksPersonel.getId() != null)
			getPersonelDinamikMap(pdksPersonel.getId());
		else
			personelDinamikMap.clear();
		for (Tanim tanim : dinamikDurumList) {
			PersonelDinamikAlan pda = getPersonelDinamikAlan(tanim, pdksPersonel);
			if (pda.getAlan().getKodu().equals("ikinciYoneticiOlmaz")) {
				getIkinciYoneticiOlmazList(pda);
			}
			dinamikPersonelDurumList.add(pda);
		}

		for (Tanim tanim : dinamikSayisalList)
			dinamikPersonelSayisalList.add(getPersonelDinamikAlan(tanim, pdksPersonel));
		if (!dinamikAciklamaList.isEmpty()) {
			List<Long> idList = new ArrayList<Long>();
			for (Tanim tanim : dinamikAciklamaList) {
				idList.add(tanim.getId());
				dinamikPersonelAciklamaList.add(getPersonelDinamikAlan(tanim, pdksPersonel));
			}
			HashMap map = new HashMap();
			map.put("tipi", Tanim.TIPI_PERSONEL_DINAMIK_ACIKLAMA);
			map.put("parentTanim.id", idList);
			map.put("durum", Boolean.TRUE);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Tanim> tanimList = pdksEntityController.getObjectByInnerObjectList(map, Tanim.class);
			if (!tanimList.isEmpty())
				tanimList = PdksUtil.sortObjectStringAlanList(tanimList, "getErpKodu", null);
			for (Tanim tanim : tanimList) {
				Long key = tanim.getParentTanim().getId();
				List<Tanim> list = dinamikPersonelAciklamaMap.containsKey(key) ? dinamikPersonelAciklamaMap.get(key) : new ArrayList<Tanim>();
				if (list.isEmpty())
					dinamikPersonelAciklamaMap.put(key, list);
				list.add(tanim);
			}
		}

		if (pdksPersonel.getId() == null) {
			if (sirketList.isEmpty())
				PdksUtil.addMessageAvailableWarn("İşlem yapılacak ERP olamayan " + ortakIslemler.sirketAciklama() + " yoktur!");
			else if (sirketList.size() == 1) {
				pdksPersonel.setSirket(sirketList.get(0));
				fillPdksPersonelList();
				if (pdksPersonel.getSirket().getPdks()) {
					pdksPersonel.setPdks(pdksPersonel.getSirket().getPdks());
					if (pdksPersonel.getSirket().getFazlaMesaiOde())
						pdksPersonel.setFazlaMesaiOde(pdksPersonel.getSirket().getFazlaMesaiOde());
				}

			}
		}

		else {
			if (pdksPersonel.getSirket().isErp() == false) {
				erpSirketleriAyikla(sirketList);
			} else
				parentBordroTanim = ortakIslemler.getEkSaha4(pdksPersonel.getSirket(), null, session);

		}
		izinGirisDurum(pdksPersonel);
		ekSahaDisable();
	}

	private void saveIkinciYoneticiOlmazList() {
		if (ikinciYoneticiHataliList != null) {
			Date guncellemeTarihi = new Date();
			for (Personel personel2 : ikinciYoneticiHataliList) {
				if (personel2.isCheckBoxDurum()) {
					personel2.setAsilYonetici2(personel2.getYoneticisi());
					personel2.setGuncellemeTarihi(guncellemeTarihi);
					personel2.setGuncelleyenUser(authenticatedUser);
					session.saveOrUpdate(personel2);
				}
			}
		}
	}

	/**
	 * @param personelDinamikAlan
	 * @return
	 */
	public String getIkinciYoneticiOlmazList(PersonelDinamikAlan personelDinamikAlan) {
		ikinciYoneticiHataliList = null;
		if (personelDinamikAlan.getPersonel().getId() != null && personelDinamikAlan.getDurumSecim() != null) {
			Long perId = personelDinamikAlan.getPersonel().getId();
			boolean durum = personelDinamikAlan.getDurumSecim() != null && personelDinamikAlan.getDurumSecim();
			if (durum)
				ikinciYoneticiHataliList = ortakIslemler.getIkinciYoneticiOlmazList(perId, personelDinamikAlan.getAlan().getKodu(), session);
			else {
				ikinciYoneticiHataliList = ortakIslemler.getIkinciYoneticiOlmazList(0L, personelDinamikAlan.getAlan().getKodu(), session);
				for (Iterator iterator = ikinciYoneticiHataliList.iterator(); iterator.hasNext();) {
					Personel personel = (Personel) iterator.next();
					if (!personel.getId().equals(perId) && !(personel.getYoneticisi() != null && personel.getYoneticisi().getId().equals(perId)))
						iterator.remove();

				}
			}
			for (Personel p2 : ikinciYoneticiHataliList) {
				p2.setCheckBoxDurum(Boolean.TRUE);
			}
		}
		if (ikinciYoneticiHataliList == null)
			ikinciYoneticiHataliList = new ArrayList<Personel>();

		return "";
	}

	public String durumIK() {
		Personel seciliPersonel = getInstance();
		if (seciliPersonel != null) {
			User seciliKullanici = seciliPersonel.getKullanici();
			if (seciliKullanici != null) {
				if (seciliKullanici.getDepartman() != null) {
					StringBuffer sb = kullaniciRolKontrolMesaji(seciliKullanici, false);
					if (sb != null)
						sb = null;

				}
				PdksUtil.setUserYetki(seciliKullanici);
			}
		}
		return "";
	}

	/**
	 * @param seciliKullanici
	 * @param kayit
	 * @return
	 */
	private StringBuffer kullaniciRolKontrolMesaji(User seciliKullanici, boolean kayit) {
		StringBuffer sb = null;
		if (seciliKullanici != null && seciliKullanici.getYetkiliRollerim() != null) {
			sb = new StringBuffer();
			for (Iterator iterator = seciliKullanici.getYetkiliRollerim().iterator(); iterator.hasNext();) {
				Role role = (Role) iterator.next();
				if (role.getDepartman() != null && !role.getDepartman().getId().equals(seciliKullanici.getDepartman().getId())) {
					if (sb.length() > 0)
						sb.append(", ");
					sb.append("\"" + role.getAciklama() + "\"");
					distinctRoleList.add(role);
					iterator.remove();
				}
			}
			if (sb.length() > 0) {
				String hataliRol = sb.toString();
				sb = new StringBuffer(hataliRol + " " + seciliKullanici.getDepartman().getDepartmanTanim().getAciklama() + " ait rol" + (hataliRol.indexOf(",") > 0 ? "ler" : "") + " değildir!");
				if (!kayit)
					PdksUtil.addMessageError(sb.toString());
			} else
				sb = null;

		}
		return sb;
	}

	/**
	 * @param pdksDurum
	 */
	private void fillPersonelTablolar(boolean pdksDurum) {
		fillGorevTipiTanimList();
		Personel pdksPersonel = getInstance();
		if (pdksDurum) {
			fillCinsiyetList();
			fillYoneticiVardiyaTipiTanimList();
			fillMasrafYeriTanimList();
			fillVardiyaGirisTipiList();
			fillTesisTanimList();
			fillDepartmanTanimList();
			fillDepartmanPDKSTanimList();
			fillEkSahaTanim();
			fillYoneticiList();
			fillPdksVardiyaSablonList();
			fillPdksCalismaModeliList();
		}
		List<Sirket> list = ortakIslemler.fillSirketList(session, pdksDurum, Boolean.TRUE);

		if (!personelERPGuncelleme.equalsIgnoreCase("M") && (pdksPersonel == null || pdksPersonel.getId() == null)) {
			erpSirketleriAyikla(list);

		}
		if (pdksPersonel != null && pdksPersonel.getId() != null && !personelERPGuncelleme.equalsIgnoreCase("M")) {
			Sirket sirketPersonel = pdksPersonel.getSirket();
			boolean ekle = true;
			if (sirketPersonel != null && authenticatedUser.isAdmin() == false) {
				Long departmanId = sirketPersonel.getDepartman().getId();
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					Sirket sirket = (Sirket) iterator.next();
					if (!sirket.getDepartman().getId().equals(departmanId))
						iterator.remove();
				}
			}

			for (Sirket sirket : list) {
				if (sirket.getId().equals(sirketPersonel.getId()))
					ekle = false;
			}
			if (ekle) {
				list.add(sirketPersonel);
				list = PdksUtil.sortObjectStringAlanList(list, "getAd", null);
			}
		}
		setSirketList(list);
	}

	/**
	 * @param list
	 */
	private void erpSirketleriAyikla(List<Sirket> list) {
		if (personelERPGuncelleme.equalsIgnoreCase("T")) {
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Sirket sirket = (Sirket) iterator.next();
				if (sirket.isErp())
					iterator.remove();
			}
		}
	}

	public void fillYoneticiList() {
		setYoneticiList(null);
		if (authenticatedUser.isAdmin() || (authenticatedUser.isIK() && authenticatedUser.getDepartman().isAdminMi())) {
			List<Long> idList = new ArrayList<Long>();
			if (authenticatedUser.getTumPersoneller() != null)
				for (Personel per : authenticatedUser.getTumPersoneller()) {
					idList.add(per.getId());
				}

			List<Personel> list = null;

			try {

				Date bugun = PdksUtil.getDate(Calendar.getInstance().getTime());
				LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
				map.put("tarih", PdksUtil.convertToDateString(bugun, "yyyyMMdd"));
				map.put("df", "112");
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				StringBuffer sp = new StringBuffer("SP_GET_YONETICI");
				list = pdksEntityController.execSPList(map, sp, Personel.class);

				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					Personel personel = (Personel) iterator.next();
					if (!idList.contains(personel.getId()))
						iterator.remove();
				}
				idList = null;
				Personel pdksPersonel = personelView.getPdksPersonel(), yoneticisi = null, asilYonetici1 = null, asilYonetici2 = null;
				if (pdksPersonel != null) {
					yoneticisi = pdksPersonel.getYoneticisi();
					asilYonetici1 = pdksPersonel.getAsilYonetici1();
					asilYonetici2 = pdksPersonel.getAsilYonetici2();
				}
				Boolean yoneticiEkle = yoneticisi != null, yonetici1Ekle = asilYonetici1 != null, yonetici2Ekle = asilYonetici2 != null;
				if (yoneticiEkle || yonetici1Ekle || yonetici2Ekle) {
					for (Personel yonetici : list) {
						try {
							if (yoneticisi != null && yonetici.getId().equals(yoneticisi.getId())) {
								yoneticiEkle = Boolean.FALSE;
							}
							if (asilYonetici1 != null && yonetici.getId().equals(asilYonetici1.getId())) {
								yonetici1Ekle = Boolean.FALSE;
							}
							if (asilYonetici2 != null && yonetici.getId().equals(asilYonetici2.getId())) {
								yonetici2Ekle = Boolean.FALSE;
							}
						} catch (Exception e) {

						}

					}
					List<Personel> ekler = new ArrayList<Personel>();
					if (yoneticiEkle)
						ekler.add(yoneticisi);
					if (yonetici1Ekle)
						ekler.add(asilYonetici1);
					if (yonetici2Ekle)
						ekler.add(asilYonetici2);
					if (!ekler.isEmpty()) {
						for (Personel personel : ekler) {
							if (personel != null) {
								boolean ekle = true;
								for (Iterator iterator = list.iterator(); iterator.hasNext();) {
									Personel yoneticiPersonel = (Personel) iterator.next();
									if (yoneticiPersonel.getId().equals(personel.getId())) {
										ekle = false;
										break;
									}
								}
								if (ekle)
									list.add(personel);
							}
						}
					}
					ekler = null;

				}
				if (list.size() > 1)
					list = PdksUtil.sortObjectStringAlanList(list, "getAdSoyad", null);
				yonetici2List = new ArrayList<Personel>();
				setYoneticiList(list);
				if (!list.isEmpty()) {
					yonetici2List.addAll(list);
					ikinciYoneticiOlmazKontrolu();
				}

			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());

			}
		}
	}

	/**
	 * 
	 */
	private void ikinciYoneticiOlmazKontrolu() {
		List<Long> yIdList = null;
		HashMap fields = new HashMap();
		if (ikinciYoneticiIzinOnayla) {
			fields.put(PdksEntityController.MAP_KEY_SELECT, "pdksPersonel.id");
			fields.put("pdksPersonel.ikinciYoneticiIzinOnayla=", Boolean.FALSE);
			fields.put("durum=", Boolean.TRUE);
			fields.put("pdksPersonel.durum=", Boolean.TRUE);
			fields.put("pdksPersonel.sskCikisTarihi>=", PdksUtil.getDate(new Date()));
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			yIdList = pdksEntityController.getObjectByInnerObjectListInLogic(fields, User.class);
		} else
			yIdList = new ArrayList<Long>();

		fields.clear();
		fields.put("tipi", Tanim.TIPI_PERSONEL_DINAMIK_DURUM);
		fields.put("kodu", Tanim.IKINCI_YONETICI_ONAYLAMAZ);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		Tanim ikinciYoneticiOlmaz = (Tanim) pdksEntityController.getObjectByInnerObject(fields, Tanim.class);
		if (yIdList.size() > 0 || (ikinciYoneticiOlmaz != null && ikinciYoneticiOlmaz.getDurum())) {
			if (ikinciYoneticiOlmaz != null && ikinciYoneticiOlmaz.getDurum()) {
				List<Long> idList = new ArrayList<Long>();
				for (Personel personel : yonetici2List)
					if (!yIdList.contains(personel.getId()))
						idList.add(personel.getId());
				StringBuffer sb = new StringBuffer();
				sb.append(" SELECT D." + PersonelDinamikAlan.COLUMN_NAME_PERSONEL + " FROM " + PersonelDinamikAlan.TABLE_NAME + " D WITH(nolock) ");
				sb.append(" WHERE  D." + PersonelDinamikAlan.COLUMN_NAME_PERSONEL + ":p AND  " + PersonelDinamikAlan.COLUMN_NAME_ALAN + "=" + ikinciYoneticiOlmaz.getId());
				sb.append(" AND  " + PersonelDinamikAlan.COLUMN_NAME_DURUM_SECIM + "=1");
				fields.clear();
				fields.put("p", idList);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<BigDecimal> list2 = pdksEntityController.getObjectBySQLList(sb, fields, null);
				for (BigDecimal bigDecimal : list2) {
					yIdList.add(bigDecimal.longValue());
				}
			}
			for (Iterator iterator = yonetici2List.iterator(); iterator.hasNext();) {
				Personel personel = (Personel) iterator.next();
				if (yIdList.contains(personel.getId()))
					iterator.remove();

			}
		}
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void fillPersonelList() {
		personelDurumMap.clear();
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		sanalPersonelAciklama = ortakIslemler.sanalPersonelAciklama();
		yoneticiRolVarmi = ortakIslemler.yoneticiRolKontrol(session);
		fillEkSahaTanim();
		List<PersonelView> list = new ArrayList<PersonelView>();
		HashMap parametreMap = new HashMap();
		ArrayList<Personel> tumPersoneller = new ArrayList<Personel>(authenticatedUser.getTumPersoneller());
		Date bugun = PdksUtil.buGun();
		if (authenticatedUser.isYoneticiKontratli())
			ortakIslemler.digerPersoneller(tumPersoneller, null, bugun, bugun, session);

		if (!authenticatedUser.isAdmin()) {
			parametreMap.put("pdksPersonel.durum", Boolean.TRUE);
			parametreMap.put("pdksPersonel", tumPersoneller);

		} else
			parametreMap.put("durum", Boolean.TRUE);
		// parametreMap.put("pdksPersonel.pdksSicilNo", "90015532");
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			list = ortakIslemler.getPersonelViewList(pdksEntityController.getObjectByInnerObjectList(parametreMap, PdksPersonelView.class));

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			PdksUtil.addMessageError("Hata : " + e.getMessage());
		}

		TreeMap<String, Boolean> map = mantiksalAlanlariDoldur(list);
		for (String key : map.keySet())
			personelDurumMap.put(key, map.get(key));
		if (authenticatedUser.isAdmin())
			for (PersonelView personelView : list) {

				try {
					// User user = personelView.getKullanici();
					Personel pdksPersonel = personelView.getPdksPersonel();
					if (pdksPersonel.getEmailCC() == null && pdksPersonel.getEmailBCC() == null)
						continue;
					boolean degisti = Boolean.FALSE;
					if (pdksPersonel.getEmailCC() != null) {
						String emailCC = ortakIslemler.getPersonelCCMail(pdksPersonel);
						if (emailCC != null) {
							degisti = !pdksPersonel.getEmailCC().trim().equals(emailCC.trim());
							pdksPersonel.setEmailCC(emailCC);
						}
					}
					if (pdksPersonel.getEmailBCC() != null) {
						String emailBCC = ortakIslemler.getPersonelBCCMail(pdksPersonel);
						if (emailBCC != null) {
							if (!degisti)
								degisti = !pdksPersonel.getEmailBCC().trim().equals(emailBCC.trim());
							pdksPersonel.setEmailBCC(emailBCC);
						}
					}
					if (pdksPersonel.getHareketMail() != null) {
						String hareketMail = ortakIslemler.getPersonelHareketMail(pdksPersonel);
						if (hareketMail != null) {
							if (!degisti)
								degisti = !pdksPersonel.getHareketMail().trim().equals(hareketMail.trim());
							pdksPersonel.setHareketMail(hareketMail);
						}
					}

					if (degisti) {
						pdksEntityController.saveOrUpdate(session, entityManager, pdksPersonel);

						session.flush();
						session.refresh(personelView);

					}
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					logger.info(personelView.getSicilNo());
				}

			}
		setPersonelList(list);
	}

	/**
	 * 
	 */
	private void fillEkSahaTanim() {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.TRUE, null);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
		setSirketList((List<Sirket>) sonucMap.get("sirketList"));
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
		departmanAciklama = (String) sonucMap.get("departmanAciklama");
		altBolumAciklama = (String) sonucMap.get("altBolumAciklama");
	}

	/**
	 * @return
	 */
	public String yeniPersonelleriGuncelle() {
		List<PersonelView> list = null;
		try {
			list = ortakIslemler.yeniPersonelleriOlustur(session);
			if (!list.isEmpty())
				fillPersonelTablolar(true);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			PdksUtil.addMessageError("yeniPersonelleriGuncelle hata : " + e.getMessage());
		}
		setTanimsizPersonelList(list);
		return "";
	}

	/**
	 * @return
	 */
	public String fillPersonelKGSList() {
		personelDurumMap.clear();
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.clear();
		yoneticiRolVarmi = ortakIslemler.yoneticiRolKontrol(session);
		manuelTanimla = Boolean.FALSE;
		eskiKullanici = null;
		List<PersonelView> list = null;
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT V.*   FROM  " + PersonelKGS.TABLE_NAME + "  V WITH(nolock) ");
		sb.append(" LEFT JOIN " + Personel.TABLE_NAME + " Y ON Y." + Personel.COLUMN_NAME_KGS_PERSONEL + "=V." + PersonelKGS.COLUMN_NAME_ID);
		String str = " WHERE ";
		if (PdksUtil.hasStringValue(adi)) {
			fields.put("ad1", "%" + adi.trim() + "%");
			fields.put("ad2", "%" + adi.trim() + "%");
			sb.append(str + " (V." + PersonelKGS.COLUMN_NAME_AD + " like :ad1 or Y." + Personel.COLUMN_NAME_AD + " like :ad2 )");
			str = " AND ";
		}
		if (PdksUtil.hasStringValue(soyadi)) {
			fields.put("soyad1", "%" + soyadi.trim() + "%");
			fields.put("soyad2", "%" + soyadi.trim() + "%");
			sb.append(str + " (V." + PersonelKGS.COLUMN_NAME_SOYAD + " like :soyad1 or Y." + Personel.COLUMN_NAME_SOYAD + " like :soyad2 )");
			str = " AND ";
		}
		if (PdksUtil.hasStringValue(sicilNo)) {
			sicilNo = ortakIslemler.getSicilNo(sicilNo);
			fields.put("sicilNo1", sicilNo.trim());
			fields.put("sicilNo2", sicilNo.trim());
			sb.append(str + " (V." + PersonelKGS.COLUMN_NAME_SICIL_NO + " =:sicilNo1 or Y." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " =:sicilNo2 )");
			str = " AND ";
		}
		Long userTesisId = null;
		boolean bos = fields.isEmpty();
		Date bugun = PdksUtil.getDate(new Date());
		if (bos)
			sb.append(str + " V." + PersonelKGS.COLUMN_NAME_DURUM + " =1 AND V." + PersonelKGS.COLUMN_NAME_PERSONEL_ID + " IS NULL");
		if (authenticatedUser.isIK_Tesis() && authenticatedUser.getPdksPersonel().getTesis() != null)
			userTesisId = authenticatedUser.getPdksPersonel().getTesis().getId();

		boolean eksahaGoster = Boolean.FALSE;

		try {

			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			list = ortakIslemler.getPersonelViewByPersonelKGSList(pdksEntityController.getObjectBySQLList(sb, fields, PersonelKGS.class));
			if (!authenticatedUser.isAdmin() && !authenticatedUser.isIKAdmin() && authenticatedUser.getDepartman() != null) {
				for (Iterator<PersonelView> iterator = list.iterator(); iterator.hasNext();) {
					PersonelView personelView = iterator.next();
					Personel pdksPersonel = personelView.getPdksPersonel();
					PersonelKGS personelKGS = personelView.getPersonelKGS();
					if (pdksPersonel == null) {
						KapiSirket kapiSirket = personelKGS.getKapiSirket();
						if (kapiSirket != null && (!kapiSirket.getDurum() || kapiSirket.getBitTarih().before(bugun))) {
							iterator.remove();
							continue;

						}
					}

					if (pdksPersonel == null && !personelKGS.getDurum()) {
						iterator.remove();
					} else {
						if (userTesisId != null) {
							if (pdksPersonel.getTesis() == null || !pdksPersonel.getTesis().getId().equals(userTesisId)) {
								iterator.remove();
								continue;
							}
						}

						Sirket sirket = pdksPersonel != null ? pdksPersonel.getSirket() : null;
						if (pdksPersonel != null && sirket != null && !sirket.getDepartman().getId().equals(authenticatedUser.getDepartman().getId()))
							iterator.remove();
						else if (!personelView.getDurum() && pdksPersonel == null)
							iterator.remove();

					}

				}

			}
			if (!list.isEmpty()) {
				if (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()) {
					for (Iterator<PersonelView> iterator = list.iterator(); iterator.hasNext();) {
						PersonelView personelView = iterator.next();
						Personel pdksPersonel = personelView.getPdksPersonel();
						PersonelKGS personelKGS = personelView.getPersonelKGS();
						if (pdksPersonel == null) {
							if (!personelKGS.getDurum()) {
								iterator.remove();
								continue;
							} else {
								KapiSirket kapiSirket = personelKGS.getKapiSirket();
								if (kapiSirket != null && (!kapiSirket.getDurum() || kapiSirket.getBitTarih().before(bugun))) {
									iterator.remove();
									continue;
								}
							}

						}
						if (pdksPersonel == null && !personelView.getPersonelKGS().getDurum()) {
							iterator.remove();
						} else {
							Personel ppdksPersonel = personelView.getPdksPersonel();
							if (userTesisId != null) {
								if (ppdksPersonel.getTesis() == null || !ppdksPersonel.getTesis().getId().equals(userTesisId)) {
									iterator.remove();
									continue;
								}
							}

							Sirket sirket = ppdksPersonel != null ? ppdksPersonel.getSirket() : null;
							if (!eksahaGoster && sirket != null)
								eksahaGoster = sirket.getDepartman().isAdminMi();
						}
					}
				}
				if (eksahaGoster)
					fillEkSahaTanim();

				list = PdksUtil.sortObjectStringAlanList(null, list, "getAdSoyad", null);
				TreeMap<String, Boolean> map = mantiksalAlanlariDoldur(list);
				for (String key : map.keySet())
					personelDurumMap.put(key, map.get(key));

				boolean personelTanimSorgula = false;
				if (personelKGSMap == null)
					personelKGSMap = new TreeMap<Long, PersonelKGS>();
				else
					personelKGSMap.clear();
				HashMap<Long, PersonelKGS> idMap = new HashMap<Long, PersonelKGS>();
				for (PersonelView personelView : list) {
					boolean personelTanimli = false;
					Personel pdksPersonel = personelView.getPdksPersonel();
					if (pdksPersonel != null && pdksPersonel.getSirket() != null) {
						PersonelKGS personelKGS = pdksPersonel.getPersonelKGS();
						if (personelKGS.getKapiSirket() != null && personelKGS.getKapiSirket().getDurum().equals(Boolean.FALSE))
							idMap.put(personelKGS.getId(), personelKGS);
						personelTanimli = pdksPersonel.getSirket().getId() != null;

					}

					if (!personelTanimSorgula)
						personelTanimSorgula = !personelTanimli;

				}
				if (!idMap.isEmpty()) {
					try {
						String birdenFazlaKGSSirketSQL = ortakIslemler.getBirdenFazlaKGSSirketSQL(null, null, session);
						TreeMap<Long, Long> iliskiMap = new TreeMap<Long, Long>();
						fields.clear();
						sb = new StringBuffer();
						sb.append("SELECT P." + PersonelKGS.COLUMN_NAME_ID + ", K." + PersonelKGS.COLUMN_NAME_ID + " AS REF from " + PersonelKGS.TABLE_NAME + " P WITH(nolock) ");
						sb.append(" INNER JOIN " + PersonelKGS.TABLE_NAME + " K ON " + birdenFazlaKGSSirketSQL);
						sb.append(" WHERE P." + PersonelKGS.COLUMN_NAME_ID + " :p AND  P." + PersonelKGS.COLUMN_NAME_SICIL_NO + " <>''");
						fields.put("p", new ArrayList(idMap.keySet()));
						if (session != null)
							fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						List<Object[]> perList = pdksEntityController.getObjectBySQLList(sb, fields, null);
						for (Object[] objects : perList) {
							BigDecimal refId = (BigDecimal) objects[1], id = (BigDecimal) objects[0];
							if (refId.longValue() != id.longValue())
								iliskiMap.put(refId.longValue(), id.longValue());
						}
						if (!iliskiMap.isEmpty()) {
							fields.clear();
							fields.put("id", new ArrayList<Long>(iliskiMap.keySet()));
							if (session != null)
								fields.put(PdksEntityController.MAP_KEY_SESSION, session);
							List<PersonelKGS> personelKGSList = pdksEntityController.getObjectByInnerObjectList(fields, PersonelKGS.class);
							for (PersonelKGS personelKGS : personelKGSList) {
								if (personelKGS.getKapiSirket().getDurum()) {
									Long id = iliskiMap.get(personelKGS.getId());
									PersonelKGS personelKGS2 = id != null ? idMap.get(id) : null;
									if (personelKGS2 != null && !personelKGS.getKapiSirket().getId().equals(personelKGS2.getKapiSirket().getId()) && personelKGS2.getAdSoyad().equals(personelKGS.getAdSoyad()))
										personelKGSMap.put(id, personelKGS);
								}

							}
						}
						iliskiMap = null;
						sb = new StringBuffer();

					} catch (Exception ex) {
						ex.printStackTrace();
					}

				}
				idMap = null;
				if (personelTanimSorgula) {
					List<Sirket> templist = ortakIslemler.fillSirketList(session, Boolean.TRUE, Boolean.FALSE);
					for (Sirket sirket : templist) {
						if (sirket.getErpDurum().equals(Boolean.FALSE))
							manuelTanimla = Boolean.TRUE;
					}
				}

			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		dosyaGuncelleDurum();
		setTanimsizPersonelList(list);
		return "";
	}

	/**
	 * @param list
	 */
	private TreeMap<String, Boolean> mantiksalAlanlariDoldur(List<PersonelView> list) {
		TreeMap<String, Boolean> map = ortakIslemler.mantiksalAlanlariDoldur(list);
		fazlaMesaiIzinKullan = map.containsKey("fazlaMesaiIzinKullan");
		istenAyrilmaGoster = map.containsKey("istenAyrilmaGoster");
		fazlaMesaiOde = map.containsKey("fazlaMesaiOde");
		sanalPersonel = map.containsKey("sanalPersonel");
		kullaniciPersonel = map.containsKey("kullaniciPersonel");
		gebeMi = map.containsKey("gebeMi");
		sutIzni = map.containsKey("sutIzni");
		kimlikNoGoster = map.containsKey("kimlikNoGoster");
		ustYonetici = map.containsKey("ustYonetici");
		icapDurum = map.containsKey("icapDurum");
		partTimeDurum = map.containsKey("partTimeDurum");
		tesisDurum = map.containsKey("tesisDurum");
		egitimDonemi = map.containsKey("egitimDonemi");
		suaOlabilir = map.containsKey("suaOlabilir");
		emailCCDurum = map.containsKey("emailCCDurum");
		emailBCCDurum = map.containsKey("emailBCCDurum");
		departmanGoster = map.containsKey("departmanGoster");
		kartNoGoster = map.containsKey("kartNoGoster");
		try {
			if (dinamikPersonelDurumList == null)
				dinamikPersonelDurumList = new ArrayList<PersonelDinamikAlan>();
			if (dinamikPersonelSayisalList == null)
				dinamikPersonelSayisalList = new ArrayList<PersonelDinamikAlan>();
			if (dinamikPersonelAciklamaList == null)
				dinamikPersonelAciklamaList = new ArrayList<PersonelDinamikAlan>();
			if (dinamikPersonelAciklamaMap == null)
				dinamikPersonelAciklamaMap = new HashMap<Long, List<Tanim>>();
			if (dinamikDurumList == null)
				dinamikDurumList = new ArrayList<Tanim>();
			else
				dinamikDurumList.clear();
			if (dinamikSayisalList == null)
				dinamikSayisalList = new ArrayList<Tanim>();
			else
				dinamikSayisalList.clear();
			if (dinamikAciklamaList == null)
				dinamikAciklamaList = new ArrayList<Tanim>();
			else
				dinamikAciklamaList.clear();
			if (personelDinamikMap == null)
				personelDinamikMap = new TreeMap<String, PersonelDinamikAlan>();
			if (personelDinamikMap == null)
				personelDinamikMap = new TreeMap<String, PersonelDinamikAlan>();
			else
				personelDinamikMap.clear();
			if (list != null && !list.isEmpty()) {
				List<Long> perIdList = new ArrayList<Long>();
				for (PersonelView personelView : list) {
					if (personelView.getPdksPersonel() != null && personelView.getPdksPersonel().getId() != null)
						perIdList.add(personelView.getPdksPersonel().getId());
				}
				if (!perIdList.isEmpty()) {
					getPersonelDinamikMap(perIdList);
				}
				dinamikAciklamaList = ortakIslemler.getPersonelTanimList(Tanim.TIPI_PERSONEL_DINAMIK_TANIM, session);
				dinamikDurumList = ortakIslemler.getPersonelTanimList(Tanim.TIPI_PERSONEL_DINAMIK_DURUM, session);
				dinamikSayisalList = ortakIslemler.getPersonelTanimList(Tanim.TIPI_PERSONEL_DINAMIK_SAYISAL, session);
			}
		} catch (Exception ex) {
			logger.error(ex);
			ex.printStackTrace();
		}
		return map;
	}

	/**
	 * @param object
	 */
	private void getPersonelDinamikMap(Object object) {
		personelDinamikMap.clear();
		HashMap fields = new HashMap();
		fields.put("personel.id", object);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PersonelDinamikAlan> personelDinamikAlanList = pdksEntityController.getObjectByInnerObjectList(fields, PersonelDinamikAlan.class);
		for (PersonelDinamikAlan personelDinamikAlan : personelDinamikAlanList)
			personelDinamikMap.put(personelDinamikAlan.getKey(), personelDinamikAlan);

	}

	/**
	 * 
	 */
	public void fillPdksPersonelList() {
		fillEkSahaTanim();
		Date bugun = PdksUtil.getDate(Calendar.getInstance().getTime());
		Personel pdksPersonel = getInstance();
		parentBordroTanim = null;
		if (pdksPersonel.getSirket().isErp())
			parentBordroTanim = ortakIslemler.getEkSaha4(pdksPersonel.getSirket(), null, session);

		izinGirisDurum(pdksPersonel);
		Departman departman = null;
		Sirket sirket = pdksPersonel.getSirket();
		try {
			departman = pdksPersonel != null && sirket != null ? sirket.getDepartman() : null;
		} catch (Exception e) {
			departman = null;
		}
		bolumDepartmanlari = departman != null && !departman.isAdminMi() ? ortakIslemler.getBolumDepartmanlari(departman, session) : null;
		gorevDepartmanlari = departman != null && !departman.isAdminMi() ? ortakIslemler.getGorevDepartmanlari(departman, session) : null;
		if (pdksPersonel.getId() == null && sirket != null) {
			if (sirket.getPdks()) {
				pdksPersonel.setPdks(sirket.getPdks());
				if (sirket.getFazlaMesaiOde())
					pdksPersonel.setFazlaMesaiOde(sirket.getFazlaMesaiOde());
			} else {
				pdksPersonel.setPdks(Boolean.FALSE);
				pdksPersonel.setFazlaMesaiOde(Boolean.FALSE);
			}
		}
		List<Personel> list = ortakIslemler.getTaseronYoneticiler(session);
		TreeMap<String, Boolean> map1 = getOnaysizIzinDurumMap(sirket);
		onaysizIzinKullanilir = map1.containsKey("onaysizIzinDurum");
		ikinciYoneticiIzinOnayla = map1.containsKey("ikinciYoneticiIzinOnaySec");
		if (pdksPersonel.getId() == null && sirket.isErp() == false)
			pdksPersonel.setOnaysizIzinKullanilir(map1.containsKey("onaysizIzinDurum"));
		try {

			HashMap parametreMap = new HashMap();
			parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "user.pdksPersonel");
			parametreMap.put("user.pdksPersonel.sskCikisTarihi>=", bugun);
			parametreMap.put("user.pdksPersonel.iseBaslamaTarihi<=", bugun);
			parametreMap.put("user.pdksPersonel.durum=", Boolean.TRUE);
			parametreMap.put("user.durum=", Boolean.TRUE);
			if (pdksPersonel.getId() != null)
				parametreMap.put("user.pdksPersonel.id<>", pdksPersonel.getId());
			parametreMap.put("role.rolename=", Role.TIPI_YONETICI_KONTRATLI);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Personel> yoneticiDigerlist = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, UserRoles.class);
			if (!yoneticiDigerlist.isEmpty()) {
				for (Iterator iterator = yoneticiDigerlist.iterator(); iterator.hasNext();) {
					Personel personel = (Personel) iterator.next();
					boolean ekle = Boolean.TRUE;
					for (Personel pdksPersonel2 : list) {
						if (pdksPersonel2.getId().equals(personel.getId())) {
							ekle = Boolean.FALSE;
							break;
						}

					}
					if (!ekle)
						iterator.remove();

				}
				if (!yoneticiDigerlist.isEmpty())
					list.addAll(yoneticiDigerlist);
			}
			if (list.size() > 1)
				list = (ArrayList<Personel>) PdksUtil.sortObjectStringAlanList(null, list, "getAdSoyad", null);

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		setPdksPersonelList(list);
		fillPdksVardiyaSablonList();
		fillPdksCalismaModeliList();
		ekSahaDisable();
	}

	/**
	 * @param sirket
	 * @return
	 */
	private TreeMap<String, Boolean> getOnaysizIzinDurumMap(Sirket sirket) {
		TreeMap<String, Boolean> dataMap = new TreeMap<String, Boolean>();
		boolean onaysizIzinSec = false, onaysizIzinDurum = false, ikinciYoneticiIzinOnayla = false;
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		StringBuffer sb = new StringBuffer("SP_SIRKET_IZIN_ONAY_BILGI");
		try {
			long sirketId = sirket != null ? sirket.getIdLong() : 0L;
			map.put("sirketId", sirketId);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Object[]> list = pdksEntityController.execSPList(map, sb, null);
			if (list.size() == 1) {
				Object[] dizi = list.get(0);
				onaysizIzinSec = ((Integer) dizi[1]) > 0;
				ikinciYoneticiIzinOnayla = ((Integer) dizi[2]) > 0;
				onaysizIzinDurum = ((Integer) dizi[3]) == 1;
			}

		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		if (onaysizIzinSec)
			dataMap.put("onaysizIzinSec", onaysizIzinSec);
		if (onaysizIzinDurum)
			dataMap.put("onaysizIzinDurum", onaysizIzinDurum);
		if (ikinciYoneticiIzinOnayla)
			dataMap.put("ikinciYoneticiIzinOnaySec", ikinciYoneticiIzinOnayla);
		return dataMap;

	}

	/**
	 * @param sirket
	 */
	protected List<IzinTipi> getOnaysizIzinDurum(Sirket sirket) {
		onaysizIzinKullanilir = Boolean.FALSE;
		HashMap fields = new HashMap();
		if (sirket != null)
			fields.put("departman.id=", sirket.getDepartman().getId());
		fields.put("personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
		fields.put("bakiyeIzinTipi=", null);
		fields.put("durum=", Boolean.TRUE);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<IzinTipi> izinTipiList = pdksEntityController.getObjectByInnerObjectListInLogic(fields, IzinTipi.class);
		for (IzinTipi izinTipi : izinTipiList) {
			if (izinTipi.getOnaylayanTipi().equals(IzinTipi.ONAYLAYAN_TIPI_YOK))
				onaysizIzinKullanilir = Boolean.TRUE;

		}

		return izinTipiList;
	}

	/**
	 * @param menuItem
	 * @return
	 */
	private String userMenuList(User user) {
		menuItemTimeList = null;
		if (user != null && user.getId() != null) {
			HashMap parametreMap = new HashMap();
			parametreMap.put("user.id", user.getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<UserMenuItemTime> list = pdksEntityController.getObjectByInnerObjectList(parametreMap, UserMenuItemTime.class);
			if (!list.isEmpty())
				menuItemTimeList = PdksUtil.sortListByAlanAdi(list, "lastTime", Boolean.TRUE);
		}

		return "";
	}

	private void fillMasrafYeriTanimList() {
		List<Tanim> tanimList = ortakIslemler.getTanimList(Tanim.TIPI_SAP_MASRAF_YERI, session);
		setMasrafYeriList(tanimList);
	}

	private void fillVardiyaGirisTipiList() {
		List<Tanim> girisTipiList = ortakIslemler.getTanimList(Tanim.TIPI_GIRIS_TIPI, session);
		setVardiyaGirisTipiTanimList(girisTipiList);
	}

	private void fillYoneticiVardiyaTipiTanimList() {
		List<Tanim> tanimList = ortakIslemler.getTanimList(Tanim.TIPI_YONETICI_VARDIYA, session);
		setYoneticiVardiyaTipiList(tanimList);
	}

	private void fillGorevTipiTanimList() {
		List<Tanim> tanimList = ortakIslemler.getTanimList(Tanim.TIPI_GOREV_TIPI, session);
		setUnvanTanimList(tanimList);
	}

	private void fillTesisTanimList() {
		List<Tanim> tanimList = ortakIslemler.getTanimList(Tanim.TIPI_TESIS, session);
		setTesisList(tanimList);
	}

	public void fillDepartmanTanimList() {
		List<Departman> list = ortakIslemler.fillDepartmanTanimList(session);
		departmanKullaniciList = new ArrayList<Departman>(list);
		taseronKulaniciTanimla = ortakIslemler.getParameterKey("taseronKulaniciTanimla").equals("1");
		if (departmanKullaniciList.size() > 1 && !taseronKulaniciTanimla) {
			for (Iterator iterator = departmanKullaniciList.iterator(); iterator.hasNext();) {
				Departman departman = (Departman) iterator.next();
				if (!departman.getAdmin())
					iterator.remove();
			}
		}
		setDepartmanTanimList(list);
	}

	public void fillCinsiyetList() {
		List<Tanim> tanimList = ortakIslemler.getTanimList(Tanim.TIPI_CINSIYET, session);
		setCinsiyetList(tanimList);
	}

	public void fillDepartmanPDKSTanimList() {
		List<Tanim> tanimList = ortakIslemler.getTanimList(Tanim.TIPI_PDKS_DEPARTMAN, session);
		setDepartmanPDKSTanimList(tanimList);
	}

	public void instanceRefresh() {
		Personel pdksPersonel = getInstance();
		if (pdksPersonel != null && pdksPersonel.getId() != null) {
			try {
				if (pdksPersonel.getKullanici() != null && pdksPersonel.getKullanici().getId() != null)
					session.refresh(pdksPersonel.getKullanici());
				session.refresh(pdksPersonel);
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());

			}
		} else {
			personelView.setPdksPersonel(null);
			personelView.setKullanici(null);
		}

	}

	public void getYoneticiler(String tipi) {
		setYoneticiTipi(tipi);
	}

	public void setYoneticisi(Personel seciliYonetici) {

	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		fazlaMesaiIzinKullan = Boolean.FALSE;
		fazlaMesaiOde = Boolean.FALSE;
		sanalPersonel = Boolean.FALSE;
		kullaniciPersonel = Boolean.FALSE;
		sutIzni = Boolean.FALSE;
		gebeMi = Boolean.FALSE;
		ustYonetici = Boolean.FALSE;
		icapDurum = Boolean.FALSE;
		partTimeDurum = Boolean.FALSE;
		tesisDurum = Boolean.FALSE;
		egitimDonemi = Boolean.FALSE;
		suaOlabilir = Boolean.FALSE;
		emailCCDurum = Boolean.FALSE;
		emailBCCDurum = Boolean.FALSE;
		tesisYetki = Boolean.FALSE;
		ikinciYoneticiManuelTanimla = Boolean.FALSE;
		eskiKullanici = null;
		if (dinamikDurumList == null)
			dinamikDurumList = new ArrayList<Tanim>();
		else
			dinamikDurumList.clear();
		if (dinamikSayisalList == null)
			dinamikSayisalList = new ArrayList<Tanim>();
		else
			dinamikSayisalList.clear();
		if (dinamikAciklamaList == null)
			dinamikAciklamaList = new ArrayList<Tanim>();
		else
			dinamikAciklamaList.clear();
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		if (personelERPList == null)
			personelERPList = new ArrayList<PersonelERP>();
		else
			personelERPList.clear();
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		personelERPGuncelleme = ortakIslemler.getParameterKey("personelERPOku");
		sanalPersonelAciklama = ortakIslemler.sanalPersonelAciklama();
		setPdks(Boolean.TRUE);

		setAdi("");
		setSoyadi("");
		setSicilNo("");
		setHataMesaj("");
		personelDosya.setDosyaIcerik(null);
		setTanimsizPersonelList(new ArrayList<PersonelView>());
		Personel personel = Personel.newpdksPersonel();
		PersonelIzin izin = new PersonelIzin();
		izin.setIzinSuresi(0D);
		personel.setPersonelIzin(izin);
		dosyaGuncelleDurum();

	}

	/**
	 * 
	 */
	private void dosyaGuncelleDurum() {
		dosyaGuncellemeYetki = ortakIslemler.getTestDurum() && (authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || authenticatedUser.isIK());
		if (dosyaGuncellemeYetki) {
			if (authenticatedUser.isIK()) {
				String dosyaGuncellemeYetkiStr = ortakIslemler.getParameterKey("dosyaGuncellemeYetki");
				dosyaGuncellemeYetki = dosyaGuncellemeYetkiStr.equals("1");
			}

		}
	}

	public String personelDosyaYaz() throws Exception {

		TreeMap<String, PersonelERP> perMap = new TreeMap<String, PersonelERP>();
		for (PersonelERP personelERP : personelERPList) {
			perMap.put(personelERP.getPersonelNo(), personelERP);
			personelERP.setYazildi(null);
		}
		servisCalisti = Boolean.FALSE;
		try {
			personelERPReturnList = null;
			PdksSoapVeriAktar service = ortakIslemler.getPdksSoapVeriAktar();
			personelERPReturnList = service.savePersoneller(personelERPList);
			if (personelERPReturnList != null) {
				List<PersonelERP> personelERPHatasizList = new ArrayList<PersonelERP>();
				for (Iterator iterator = personelERPReturnList.iterator(); iterator.hasNext();) {
					PersonelERP returnERP = (PersonelERP) iterator.next();
					if (perMap.containsKey(returnERP.getPersonelNo())) {
						PersonelERP personelERP = perMap.get(returnERP.getPersonelNo());
						personelERP.setYazildi(returnERP.getYazildi());
						if (returnERP.getYazildi()) {
							personelERPHatasizList.add(returnERP);
							iterator.remove();
						} else {
							personelERP.getHataList().addAll(returnERP.getHataList());
						}
					} else
						returnERP.getHataList().add("İşlem yapılmadı!");
				}
				if (!personelERPReturnList.isEmpty()) {
					personelERPList.clear();
					PdksUtil.addMessageWarn("Hata oluştu!");
				} else
					PdksUtil.addMessageInfo("Güncelleme  başarılı tamamlandı.");

				personelERPHatasizList = null;
			}

			servisCalisti = Boolean.TRUE;
		} catch (Exception e) {
			personelERPReturnList = null;
			logger.error(e);
		}

		return "";
	}

	public String personelDosyaRestYaz() throws Exception {

		servisCalisti = Boolean.FALSE;
		String adres = ortakIslemler.getParameterKey("pdksWebService");
		if (!PdksUtil.hasStringValue(adres))
			adres = "http://localhost:9080/PdksWebService";

		String url = adres + "/rest/services/savePersoneller";
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		TreeMap<String, PersonelERP> perMap = new TreeMap<String, PersonelERP>();
		for (PersonelERP personelERP : personelERPList) {
			perMap.put(personelERP.getPersonelNo(), personelERP);
			personelERP.setYazildi(null);
		}
		String gsonStr = gson.toJson(personelERPList);
		String strGson = null;
		try {
			strGson = ortakIslemler.getJSONData(url, HttpMethod.POST, gsonStr, null, true);
		} catch (Exception e) {
			strGson = null;
			e.printStackTrace();
		}

		if (strGson != null) {
			servisCalisti = Boolean.TRUE;
			List<LinkedTreeMap<String, Object>> list = gson.fromJson(strGson, List.class);
			if (list != null)
				for (LinkedTreeMap<String, Object> linkedHashMap : list) {
					gsonStr = gson.toJson(linkedHashMap);
					PersonelERP returnERP = gson.fromJson(gsonStr, PersonelERP.class);
					if (perMap.containsKey(returnERP.getPersonelNo())) {
						PersonelERP personelERP = perMap.get(returnERP.getPersonelNo());
						personelERP.setYazildi(returnERP.getYazildi());
						personelERP.getHataList().addAll(returnERP.getHataList());
					}
				}
		}
		logger.info(strGson);

		// String strGson1 = ortakIslemler.getURLJSONData(true, url, HttpMethod.POST, null, true, null);

		return "";
	}

	/**
	 * @param alanAdi
	 * @param alanMap
	 * @return
	 */
	private Integer getAlanColNo(String alanAdi, HashMap<String, Integer> alanMap) {
		int colNo = alanMap.containsKey(alanAdi) ? alanMap.get(alanAdi) : -1;
		if (!alanAdi.equals("SICIL_NO") && COL_SICIL_NO < 0)
			colNo = -1;
		return colNo;
	}

	/**
	 * @param deger
	 * @param map
	 */
	private void setKodAciklama(String deger, LinkedHashMap<String, String> map) {
		if (map == null)
			map = new LinkedHashMap<String, String>();
		else
			map.clear();
		int yer = deger.indexOf(PdksUtil.SEPARATOR_KOD_ACIKLAMA);
		if (yer > 0) {
			String kod = deger.substring(0, yer).trim();
			String aciklama = deger.substring(yer + 1).trim();
			if (PdksUtil.hasStringValue(kod))
				map.put("kod", kod);
			if (PdksUtil.hasStringValue(aciklama))
				map.put("aciklama", aciklama);
		}
	}

	public String personelDosyaOkuBasla() throws Exception {
		session.clear();
		servisBilgileriOlustur();
		return "";

	}

	/**
	 * 
	 */
	private void servisBilgileriOlustur() {
		List<Tanim> personelTransferList = ortakIslemler.getTanimList(Tanim.TIPI_ERP_PERSONEL_ALAN, session);
		dosyaTanimList.clear();
		HashMap<Long, Liste> map = new HashMap<Long, Liste>();
		long bas = -1, son = -1;
		for (Tanim tanim : personelTransferList) {
			long sira = -1;
			try {
				if (PdksUtil.hasStringValue(tanim.getErpKodu())) {
					sira = Long.parseLong(tanim.getErpKodu().trim());
					if (sira > son)
						son = sira;
					if (bas < 0 || sira < bas)
						bas = sira;

				}

			} catch (Exception e) {
				sira = -1;
			}
			if (sira >= 0L && !map.containsKey(sira)) {
				Liste liste = new Liste(sira, tanim);
				map.put(sira, liste);
			}

		}
		if (!map.isEmpty()) {
			for (long i = 0; i <= son; i++) {
				Liste liste = map.get(i);
				if (liste == null) {
					Tanim tanim = new Tanim();
					tanim.setAciklamatr((i + 1) + ". sutün boş ");
					liste = new Liste(i, tanim);

				}

				dosyaTanimList.add(liste);
			}
		} else
			PdksUtil.addMessageAvailableWarn("Dosya transfer alanları tanımsız!");
		map = null;
	}

	/**
	 * @param anaMap
	 * @param veriTip
	 * @param key
	 * @param deger
	 */
	private void kontrolDosyaYaz(LinkedHashMap<String, TreeMap<String, LinkedHashMap<String, Liste>>> anaMap, String veriTip, String key, String deger) {
		if (PdksUtil.hasStringValue(veriTip)) {
			if (PdksUtil.hasStringValue(key) && deger != null) {
				veriTip = veriTip.trim();

				deger = deger.trim();
				TreeMap<String, LinkedHashMap<String, Liste>> map = anaMap.containsKey(veriTip) ? anaMap.get(veriTip) : new TreeMap<String, LinkedHashMap<String, Liste>>();
				if (map.isEmpty())
					anaMap.put(veriTip, map);
				if (transferAciklamaCiftKontrol) {
					String key1 = (sirketKodu != null ? sirketKodu.trim() + PdksUtil.SEPARATOR_KOD_ACIKLAMA : "") + deger.trim();
					LinkedHashMap<String, Liste> mapsDeger = map.containsKey(key1) ? map.get(key1) : new LinkedHashMap<String, Liste>();
					if (mapsDeger.isEmpty())
						map.put(key1, mapsDeger);
					if (!mapsDeger.containsKey(key))
						mapsDeger.put(key, new Liste(key, deger));
				}
				String key2 = (sirketKodu != null ? sirketKodu.trim() + PdksUtil.SEPARATOR_KOD_ACIKLAMA : "") + key.trim();
				LinkedHashMap<String, Liste> mapsKey = map.containsKey(key2) ? map.get(key2) : new LinkedHashMap<String, Liste>();
				if (mapsKey.isEmpty())
					map.put(key2, mapsKey);
				if (!mapsKey.containsKey(deger))
					mapsKey.put(deger, new Liste(key, deger));

			}
		}

	}

	/**
	 * @return
	 * @throws Exception
	 */
	public String personelDosyaOku() throws Exception {
		servisCalisti = Boolean.FALSE;
		if (personelERPList == null)
			personelERPList = new ArrayList<PersonelERP>();
		else
			personelERPList.clear();
		Workbook wb = ortakIslemler.getWorkbook(personelDosya);
		try {
			if (wb != null) {
				setServisAlanlar();
				Sheet sheet = wb.getSheetAt(0);
				fillEkSahaTanim();
				LinkedHashMap<String, TreeMap<String, LinkedHashMap<String, Liste>>> anaMap = new LinkedHashMap<String, TreeMap<String, LinkedHashMap<String, Liste>>>();
				HashMap<String, String> referansMap = new HashMap<String, String>();
				String REFERANS_SIRKET = ExcelUtil.getSheetStringValueTry(sheet, 0, COL_SIRKET_KODU);
				if (REFERANS_SIRKET == null)
					REFERANS_SIRKET = ExcelUtil.getSheetStringValueTry(sheet, 0, COL_SIRKET_ADI);
				String REFERANS_TESIS = ExcelUtil.getSheetStringValueTry(sheet, 0, COL_TESIS_KODU);
				if (REFERANS_TESIS == null)
					REFERANS_TESIS = ExcelUtil.getSheetStringValueTry(sheet, 0, COL_TESIS_ADI);
				String REFERANS_CINSIYET = ExcelUtil.getSheetStringValueTry(sheet, 0, COL_CINSIYET_KODU);
				if (REFERANS_CINSIYET == null)
					REFERANS_CINSIYET = ExcelUtil.getSheetStringValueTry(sheet, 0, COL_CINSIYET);
				String REFERANS_BOLUM = ExcelUtil.getSheetStringValueTry(sheet, 0, COL_BOLUM_KODU);
				if (REFERANS_BOLUM == null)
					REFERANS_BOLUM = ExcelUtil.getSheetStringValueTry(sheet, 0, COL_BOLUM_ADI);
				String REFERANS_DEPARTMAN = ExcelUtil.getSheetStringValueTry(sheet, 0, COL_DEPARTMAN_KODU);
				if (REFERANS_DEPARTMAN == null)
					REFERANS_DEPARTMAN = ExcelUtil.getSheetStringValueTry(sheet, 0, COL_DEPARTMAN_ADI);
				String REFERANS_GOREV = ExcelUtil.getSheetStringValueTry(sheet, 0, COL_GOREV_KODU);
				if (REFERANS_GOREV == null)
					REFERANS_GOREV = ExcelUtil.getSheetStringValueTry(sheet, 0, COL_GOREVI);
				String REFERANS_BORDRO_ALT_ALAN = ExcelUtil.getSheetStringValueTry(sheet, 0, COL_BORDRO_ALT_ALAN_KODU);
				if (REFERANS_BORDRO_ALT_ALAN == null)
					REFERANS_BORDRO_ALT_ALAN = ExcelUtil.getSheetStringValueTry(sheet, 0, COL_BORDRO_ALT_ALAN_ADI);
				String REFERANS_MASRAF_YERI = ExcelUtil.getSheetStringValueTry(sheet, 0, COL_MASRAF_YERI_KODU);
				if (REFERANS_MASRAF_YERI == null)
					REFERANS_MASRAF_YERI = ExcelUtil.getSheetStringValueTry(sheet, 0, COL_MASRAF_YERI_ADI);
				if (REFERANS_SIRKET != null)
					referansMap.put(REFERANS_SIRKET, ExcelUtil.getSheetStringValueTry(sheet, 0, COL_SIRKET_ADI));
				if (REFERANS_TESIS != null)
					referansMap.put(REFERANS_TESIS, ExcelUtil.getSheetStringValueTry(sheet, 0, COL_TESIS_ADI));
				if (REFERANS_CINSIYET != null)
					referansMap.put(REFERANS_CINSIYET, ExcelUtil.getSheetStringValueTry(sheet, 0, COL_CINSIYET));
				if (REFERANS_BOLUM != null)
					referansMap.put(REFERANS_BOLUM, ExcelUtil.getSheetStringValueTry(sheet, 0, COL_BOLUM_ADI));
				if (REFERANS_GOREV != null)
					referansMap.put(REFERANS_GOREV, ExcelUtil.getSheetStringValueTry(sheet, 0, COL_GOREVI));
				if (REFERANS_DEPARTMAN != null)
					referansMap.put(REFERANS_DEPARTMAN, ExcelUtil.getSheetStringValueTry(sheet, 0, COL_DEPARTMAN_ADI));
				if (REFERANS_BORDRO_ALT_ALAN != null)
					referansMap.put(REFERANS_BORDRO_ALT_ALAN, ExcelUtil.getSheetStringValueTry(sheet, 0, COL_BORDRO_ALT_ALAN_ADI));
				if (REFERANS_MASRAF_YERI != null)
					referansMap.put(REFERANS_MASRAF_YERI, ExcelUtil.getSheetStringValueTry(sheet, 0, COL_MASRAF_YERI_ADI));

				String pattern = "dd.MM.yyyy", patternServis = "yyyy-MM-dd";
				String perSicilNo = null;
				List<String> perNoList = new ArrayList<String>();
				String sicilNoUzunlukStr = ortakIslemler.getParameterKey("sicilNoUzunluk");
				int maxTextLength = 0;
				try {
					if (PdksUtil.hasStringValue(sicilNoUzunlukStr))
						maxTextLength = Integer.parseInt(sicilNoUzunlukStr);
				} catch (Exception e) {
					maxTextLength = 0;
				}
				LinkedHashMap<String, PersonelERP> perMap = new LinkedHashMap<String, PersonelERP>();
				LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
				transferAciklamaCiftKontrol = ortakIslemler.getParameterKey("transferAciklamaCiftKontrol").equals("1");
				try {
					for (int row = 1; COL_SICIL_NO >= 0 && row <= sheet.getLastRowNum(); row++) {
						try {
							perSicilNo = ExcelUtil.getSheetStringValueTry(sheet, row, COL_SICIL_NO);
							logger.debug(row + " " + perSicilNo);
							if (!PdksUtil.hasStringValue(perSicilNo))
								break;
							if (maxTextLength > 0 && perSicilNo.trim().length() < maxTextLength)
								perSicilNo = PdksUtil.textBaslangicinaKarakterEkle(perSicilNo.trim(), '0', maxTextLength);
							if (perNoList.contains(perSicilNo))
								continue;
						} catch (Exception e) {

						}
						sirketKodu = null;
						PersonelERP personelERP = new PersonelERP();

						personelERP.setPersonelNo(perSicilNo);
						if (COL_ADI >= 0)
							personelERP.setAdi(ExcelUtil.getSheetStringValueTry(sheet, row, COL_ADI));
						if (COL_SOYADI >= 0)
							personelERP.setSoyadi(ExcelUtil.getSheetStringValueTry(sheet, row, COL_SOYADI));
						if (COL_SIRKET_ADI >= 0) {
							personelERP.setSirketAdi(ExcelUtil.getSheetStringValueTry(sheet, row, COL_SIRKET_ADI));
							if (COL_SIRKET_KODU >= 0) {
								personelERP.setSirketKodu(ExcelUtil.getSheetStringValueTry(sheet, row, COL_SIRKET_KODU));
								kontrolDosyaYaz(anaMap, REFERANS_SIRKET, personelERP.getSirketKodu(), personelERP.getSirketAdi());
							}

							if (personelERP.getSirketKodu() == null && personelERP.getSirketAdi() != null && personelERP.getSirketAdi().indexOf(PdksUtil.SEPARATOR_KOD_ACIKLAMA) > 0) {
								setKodAciklama(personelERP.getSirketAdi(), map);
								personelERP.setSirketKodu(map.get("kod"));
								personelERP.setSirketAdi(map.get("aciklama"));
								kontrolDosyaYaz(anaMap, REFERANS_SIRKET, personelERP.getSirketKodu(), personelERP.getSirketAdi());
							}
						}
						if (COL_TESIS_ADI >= 0) {
							personelERP.setTesisAdi(ExcelUtil.getSheetStringValueTry(sheet, row, COL_TESIS_ADI));
							sirketKodu = personelERP.getSirketKodu();
							if (COL_TESIS_KODU >= 0) {
								personelERP.setTesisKodu(ExcelUtil.getSheetStringValueTry(sheet, row, COL_TESIS_KODU));
								String tesisKodu = personelERP.getTesisKodu();
								kontrolDosyaYaz(anaMap, REFERANS_TESIS, tesisKodu, personelERP.getTesisAdi());
							}

							if (personelERP.getTesisKodu() == null && personelERP.getTesisAdi() != null && personelERP.getTesisAdi().indexOf(PdksUtil.SEPARATOR_KOD_ACIKLAMA) > 0) {
								setKodAciklama(personelERP.getTesisAdi(), map);
								personelERP.setTesisKodu(map.get("kod"));
								String tesisKodu = personelERP.getTesisKodu();
								personelERP.setTesisAdi(map.get("aciklama"));
								kontrolDosyaYaz(anaMap, REFERANS_TESIS, tesisKodu, personelERP.getTesisAdi());
							}
						}
						sirketKodu = null;
						if (COL_DEPARTMAN_ADI >= 0) {
							personelERP.setDepartmanAdi(ExcelUtil.getSheetStringValueTry(sheet, row, COL_DEPARTMAN_ADI));
							if (COL_DEPARTMAN_KODU >= 0) {
								personelERP.setDepartmanKodu(ExcelUtil.getSheetStringValueTry(sheet, row, COL_DEPARTMAN_KODU));
								kontrolDosyaYaz(anaMap, REFERANS_DEPARTMAN, personelERP.getDepartmanKodu(), personelERP.getDepartmanAdi());
							}

							if (personelERP.getDepartmanKodu() == null && personelERP.getDepartmanAdi() != null && personelERP.getDepartmanAdi().indexOf(PdksUtil.SEPARATOR_KOD_ACIKLAMA) > 0) {
								setKodAciklama(personelERP.getDepartmanAdi(), map);
								personelERP.setDepartmanKodu(map.get("kod"));
								personelERP.setDepartmanAdi(map.get("aciklama"));
								kontrolDosyaYaz(anaMap, REFERANS_DEPARTMAN, personelERP.getDepartmanKodu(), personelERP.getDepartmanAdi());
							}
						}
						if (COL_BOLUM_ADI >= 0) {
							personelERP.setBolumAdi(ExcelUtil.getSheetStringValueTry(sheet, row, COL_BOLUM_ADI));
							if (COL_BOLUM_KODU >= 0) {
								personelERP.setBolumKodu(ExcelUtil.getSheetStringValueTry(sheet, row, COL_BOLUM_KODU));
								kontrolDosyaYaz(anaMap, REFERANS_BOLUM, personelERP.getBolumKodu(), personelERP.getBolumAdi());
							}

							if (personelERP.getBolumKodu() == null && personelERP.getBolumAdi() != null && personelERP.getBolumAdi().indexOf(PdksUtil.SEPARATOR_KOD_ACIKLAMA) > 0) {
								setKodAciklama(personelERP.getBolumAdi(), map);
								personelERP.setBolumKodu(map.get("kod"));
								personelERP.setBolumAdi(map.get("aciklama"));
								kontrolDosyaYaz(anaMap, REFERANS_BOLUM, personelERP.getBolumKodu(), personelERP.getBolumAdi());
							}
						}

						sirketKodu = personelERP.getTesisKodu();
						if (COL_BORDRO_ALT_ALAN_ADI >= 0) {
							personelERP.setBordroAltAlanAdi(ExcelUtil.getSheetStringValueTry(sheet, row, COL_BORDRO_ALT_ALAN_ADI));
							if (COL_BORDRO_ALT_ALAN_KODU >= 0) {
								personelERP.setBordroAltAlanKodu(ExcelUtil.getSheetStringValueTry(sheet, row, COL_BORDRO_ALT_ALAN_KODU));
								kontrolDosyaYaz(anaMap, REFERANS_BORDRO_ALT_ALAN, personelERP.getBordroAltAlanKodu(), personelERP.getBordroAltAlanAdi());
							}
							if (personelERP.getBordroAltAlanKodu() == null && personelERP.getBordroAltAlanAdi() != null && personelERP.getBordroAltAlanAdi().indexOf(PdksUtil.SEPARATOR_KOD_ACIKLAMA) > 0) {
								setKodAciklama(personelERP.getBordroAltAlanAdi(), map);
								personelERP.setBordroAltAlanKodu(map.get("kod"));
								personelERP.setBordroAltAlanAdi(map.get("aciklama"));
								kontrolDosyaYaz(anaMap, REFERANS_BORDRO_ALT_ALAN, personelERP.getBordroAltAlanKodu(), personelERP.getBordroAltAlanAdi());
							}
						}

						if (COL_MASRAF_YERI_ADI >= 0) {
							personelERP.setMasrafYeriAdi(ExcelUtil.getSheetStringValueTry(sheet, row, COL_MASRAF_YERI_ADI));
							if (COL_MASRAF_YERI_KODU >= 0) {
								personelERP.setMasrafYeriKodu(ExcelUtil.getSheetStringValueTry(sheet, row, COL_MASRAF_YERI_KODU));
								kontrolDosyaYaz(anaMap, REFERANS_MASRAF_YERI, personelERP.getMasrafYeriKodu(), personelERP.getMasrafYeriAdi());
							}
							if (personelERP.getMasrafYeriKodu() == null && personelERP.getMasrafYeriAdi() != null && personelERP.getMasrafYeriAdi().indexOf(PdksUtil.SEPARATOR_KOD_ACIKLAMA) > 0) {
								setKodAciklama(personelERP.getMasrafYeriAdi(), map);
								personelERP.setMasrafYeriKodu(map.get("kod"));
								personelERP.setMasrafYeriAdi(map.get("aciklama"));
								kontrolDosyaYaz(anaMap, REFERANS_MASRAF_YERI, personelERP.getMasrafYeriKodu(), personelERP.getMasrafYeriAdi());
							}
						}
						sirketKodu = null;
						if (COL_GOREVI >= 0) {
							personelERP.setGorevi(ExcelUtil.getSheetStringValueTry(sheet, row, COL_GOREVI));
							if (COL_GOREV_KODU >= 0) {
								personelERP.setGorevKodu(ExcelUtil.getSheetStringValueTry(sheet, row, COL_GOREV_KODU));
								kontrolDosyaYaz(anaMap, REFERANS_GOREV, personelERP.getGorevKodu(), personelERP.getGorevi());
							}

							if (personelERP.getGorevKodu() == null && personelERP.getGorevi() != null && personelERP.getGorevi().indexOf(PdksUtil.SEPARATOR_KOD_ACIKLAMA) > 0) {
								setKodAciklama(personelERP.getGorevi(), map);
								personelERP.setGorevKodu(map.get("kod"));
								personelERP.setGorevi(map.get("aciklama"));
								kontrolDosyaYaz(anaMap, REFERANS_GOREV, personelERP.getGorevKodu(), personelERP.getGorevi());
							}
						}
						if (COL_CINSIYET >= 0) {
							personelERP.setCinsiyeti(ExcelUtil.getSheetStringValueTry(sheet, row, COL_CINSIYET));
							if (COL_CINSIYET_KODU >= 0) {
								personelERP.setCinsiyetKodu(ExcelUtil.getSheetStringValueTry(sheet, row, COL_CINSIYET_KODU));
								kontrolDosyaYaz(anaMap, REFERANS_CINSIYET, personelERP.getCinsiyetKodu(), personelERP.getCinsiyeti());
							}

							if (personelERP.getCinsiyetKodu() == null && personelERP.getCinsiyeti() != null && personelERP.getCinsiyeti().indexOf(PdksUtil.SEPARATOR_KOD_ACIKLAMA) > 0) {
								setKodAciklama(personelERP.getCinsiyeti(), map);
								personelERP.setCinsiyetKodu(map.get("kod"));
								personelERP.setCinsiyeti(map.get("aciklama"));

							}
						}
						if (COL_BORDRO_SANAL_PERSONEL >= 0) {
							String sanalPersonel = ExcelUtil.getSheetStringValueTry(sheet, row, COL_BORDRO_SANAL_PERSONEL);
							if (sanalPersonel != null)
								try {
									personelERP.setSanalPersonel(new Boolean(sanalPersonel));
								} catch (Exception e) {
								}
						}

						String yoneticiPerNo = null, yonetici2PerNo = null;
						if (COL_YONETICI_KODU >= 0)
							yoneticiPerNo = ExcelUtil.getSheetStringValueTry(sheet, row, COL_YONETICI_KODU);
						if (PdksUtil.hasStringValue(yoneticiPerNo) && yoneticiPerNo.trim().length() < maxTextLength)
							yoneticiPerNo = PdksUtil.textBaslangicinaKarakterEkle(yoneticiPerNo.trim(), '0', maxTextLength);
						personelERP.setYoneticiPerNo(yoneticiPerNo);

						if (COL_YONETICI2_KODU >= 0)
							yonetici2PerNo = ExcelUtil.getSheetStringValueTry(sheet, row, COL_YONETICI2_KODU);
						if (PdksUtil.hasStringValue(yonetici2PerNo) && yonetici2PerNo.trim().length() < maxTextLength)
							yonetici2PerNo = PdksUtil.textBaslangicinaKarakterEkle(yonetici2PerNo.trim(), '0', maxTextLength);
						personelERP.setYonetici2PerNo(yonetici2PerNo);
						Date izinHakEdisTarihi = null, iseBaslamaTarihi = null, istenAyrilmaTarihi = null, dogumTarihi = null, grubaGirisTarihi = null;
						try {
							try {
								if (COL_KIDEM_TARIHI >= 0)
									izinHakEdisTarihi = ExcelUtil.getSheetDateValueTry(sheet, row, COL_KIDEM_TARIHI, pattern);
							} catch (Exception e) {
								PdksUtil.addMessageWarn(perSicilNo + " kıdem tarihinde sorun var!");

							}
							try {
								if (COL_GRUBA_GIRIS_TARIHI >= 0)
									grubaGirisTarihi = ExcelUtil.getSheetDateValueTry(sheet, row, COL_GRUBA_GIRIS_TARIHI, pattern);
							} catch (Exception e) {
								PdksUtil.addMessageWarn(perSicilNo + " gruba giriş tarihinde sorun var!");

							}

							try {
								if (COL_ISE_BASLAMA_TARIHI >= 0)
									iseBaslamaTarihi = ExcelUtil.getSheetDateValueTry(sheet, row, COL_ISE_BASLAMA_TARIHI, pattern);
							} catch (Exception e) {
								PdksUtil.addMessageWarn(perSicilNo + " işe giriş tarihinde sorun var!");

							}

							try {
								if (COL_ISTEN_AYRILMA_TARIHI >= 0)
									istenAyrilmaTarihi = ExcelUtil.getSheetDateValueTry(sheet, row, COL_ISTEN_AYRILMA_TARIHI, pattern);
							} catch (Exception e) {
								PdksUtil.addMessageWarn(perSicilNo + " işten ayrılma tarihinde sorun var!");

							}
							try {
								if (COL_DOGUM_TARIHI >= 0)
									dogumTarihi = ExcelUtil.getSheetDateValueTry(sheet, row, COL_DOGUM_TARIHI, pattern);
							} catch (Exception e) {
								PdksUtil.addMessageWarn(perSicilNo + " doğum tarihinde  sorun var!");

							}
							personelERP.setIstenAyrilmaTarihi(istenAyrilmaTarihi != null ? PdksUtil.convertToDateString(istenAyrilmaTarihi, patternServis) : null);
							personelERP.setIseGirisTarihi(iseBaslamaTarihi != null ? PdksUtil.convertToDateString(iseBaslamaTarihi, patternServis) : null);
							personelERP.setGrubaGirisTarihi(grubaGirisTarihi != null ? PdksUtil.convertToDateString(grubaGirisTarihi, patternServis) : null);
							personelERP.setDogumTarihi(dogumTarihi != null ? PdksUtil.convertToDateString(dogumTarihi, patternServis) : null);
							personelERP.setKidemTarihi(izinHakEdisTarihi != null ? PdksUtil.convertToDateString(izinHakEdisTarihi, patternServis) : null);
						} catch (Exception ex) {
							ex.printStackTrace();
						}

						perNoList.add(perSicilNo);
						perMap.put(perSicilNo, personelERP);
					}
				} catch (Exception exx) {
					logger.error(exx);
					exx.printStackTrace();
				}

				if (!perNoList.isEmpty()) {
					boolean listeDolu = true;
					for (String veriTip : anaMap.keySet()) {
						if (referansMap.containsKey(veriTip)) {
							StringBuffer sb = new StringBuffer();
							String aciklama = referansMap.get(veriTip);
							TreeMap<String, LinkedHashMap<String, Liste>> veriMap = anaMap.get(veriTip);
							for (String key : veriMap.keySet()) {
								LinkedHashMap<String, Liste> maps = veriMap.get(key);
								if (maps.size() > 1) {
									if (sb.length() > 0)
										sb.append(", ");
									List<Liste> listeler = new ArrayList<Liste>(maps.values());
									Liste liste1 = listeler.get(0), liste2 = listeler.get(1);
									if (liste1.getId().equals(liste2.getId())) {
										listeler = PdksUtil.sortObjectStringAlanList(listeler, "getValue", null);
										sb.append(veriTip + " = \"" + liste1.getId() + "\" : [ ");
										for (Iterator iterator = listeler.iterator(); iterator.hasNext();) {
											Liste liste = (Liste) iterator.next();
											sb.append("\"" + liste.getValue() + "\"");
											if (iterator.hasNext())
												sb.append(", ");
										}
									} else {
										listeler = PdksUtil.sortObjectStringAlanList(listeler, "getId", null);
										sb.append(veriTip + " = \"" + liste1.getValue() + "\" : [ ");
										for (Iterator iterator = listeler.iterator(); iterator.hasNext();) {
											Liste liste = (Liste) iterator.next();
											sb.append("\"" + liste.getId() + "\"");
											if (iterator.hasNext())
												sb.append(", ");
										}
									}
									sb.append(" ] " + maps.size() + " adet ");
								}
							}
							if (sb.length() > 0) {
								PdksUtil.addMessageAvailableWarn(sb.toString() + " farklı \"" + aciklama + "\"  tanımlanmıştır.");

								listeDolu = false;
							}
							sb = null;
						}
					}
					if (listeDolu) {
						session.clear();
						HashMap fields = new HashMap();
						fields.put(PdksEntityController.MAP_KEY_MAP, "getSicilNo");
						fields.put("sicilNo", new ArrayList(perMap.keySet()));
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						TreeMap<String, PersonelKGS> personelKGSMap = pdksEntityController.getObjectByInnerObjectMap(fields, PersonelKGS.class, false);
						for (String sicilNo : perNoList) {
							if (personelKGSMap.containsKey(sicilNo))
								personelERPList.add(perMap.get(sicilNo));
						}
					}
				}

			}

		} catch (Exception e) {

		}
		return "";
	}

	/**
	 * @return
	 */
	private HashMap<String, Integer> setServisAlanlar() {
		HashMap<String, Integer> alanMap = new HashMap<String, Integer>();
		if (authenticatedUser.isAdmin() || dosyaTanimList.isEmpty())
			servisBilgileriOlustur();
		String yonetici2ERPKontrolStr = ortakIslemler.getParameterKey("yonetici2ERPKontrol");
		boolean yonetici2ERPKontrol = yonetici2ERPKontrolStr.equals("1");
		for (Iterator iterator = dosyaTanimList.iterator(); iterator.hasNext();) {
			Liste liste = (Liste) iterator.next();
			try {
				Tanim tanim = (Tanim) liste.getValue();
				if (PdksUtil.hasStringValue(tanim.getErpKodu()))
					alanMap.put(tanim.getKodu(), Integer.parseInt(tanim.getErpKodu().trim()));
			} catch (Exception e) {

			}

		}
		COL_SICIL_NO = getAlanColNo("SICIL_NO", alanMap);
		COL_ADI = getAlanColNo("ADI", alanMap);
		COL_SOYADI = getAlanColNo("SOYADI", alanMap);
		COL_SIRKET_KODU = getAlanColNo("SIRKET_KODU", alanMap);
		COL_SIRKET_ADI = getAlanColNo("SIRKET_ADI", alanMap);
		COL_TESIS_KODU = getAlanColNo("TESIS_KODU", alanMap);
		COL_TESIS_ADI = getAlanColNo("TESIS_ADI", alanMap);
		COL_GOREV_KODU = getAlanColNo("GOREV_KODU", alanMap);
		COL_GOREVI = getAlanColNo("GOREVI", alanMap);
		COL_BOLUM_KODU = getAlanColNo("BOLUM_KODU", alanMap);
		COL_BOLUM_ADI = getAlanColNo("BOLUM_ADI", alanMap);
		COL_ISE_BASLAMA_TARIHI = getAlanColNo("ISE_BASLAMA_TARIHI", alanMap);
		COL_KIDEM_TARIHI = getAlanColNo("KIDEM_TARIHI", alanMap);
		COL_GRUBA_GIRIS_TARIHI = getAlanColNo("GRUBA_GIRIS_TARIHI", alanMap);
		COL_ISTEN_AYRILMA_TARIHI = getAlanColNo("ISTEN_AYRILMA_TARIHI", alanMap);
		COL_DOGUM_TARIHI = getAlanColNo("DOGUM_TARIHI", alanMap);
		COL_CINSIYET_KODU = getAlanColNo("CINSIYET_KODU", alanMap);
		COL_CINSIYET = getAlanColNo("CINSIYET", alanMap);
		COL_YONETICI_KODU = getAlanColNo("YONETICI_KODU", alanMap);
		COL_YONETICI2_KODU = yonetici2ERPKontrol ? getAlanColNo("YONETICI2_KODU", alanMap) : -1;
		COL_DEPARTMAN_KODU = getAlanColNo("DEPARTMAN_KODU", alanMap);
		COL_DEPARTMAN_ADI = getAlanColNo("DEPARTMAN_ADI", alanMap);
		COL_MASRAF_YERI_KODU = getAlanColNo("MASRAF_YERI_KODU", alanMap);
		COL_MASRAF_YERI_ADI = getAlanColNo("MASRAF_YERI_ADI", alanMap);
		COL_BORDRO_ALT_ALAN_KODU = getAlanColNo("BORDRO_ALT_ALAN_KODU", alanMap);
		COL_BORDRO_ALT_ALAN_ADI = getAlanColNo("BORDRO_ALT_ALAN_ADI", alanMap);
		COL_BORDRO_SANAL_PERSONEL = getAlanColNo("SANAL_PERSONEL", alanMap);
		return alanMap;
	}

	public String personelDosyaSifirla() throws Exception {
		servisCalisti = Boolean.FALSE;
		personelERPList.clear();
		personelDosya.setDosyaIcerik(null);
		if (personelERPReturnList == null)
			personelERPReturnList = new ArrayList<PersonelERP>();
		else
			personelERPReturnList.clear();
		return "";
	}

	/**
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public String listenerPersonelDosya(UploadEvent event) throws Exception {
		servisCalisti = Boolean.FALSE;
		UploadItem item = event.getUploadItem();
		PdksUtil.getDosya(item, personelDosya);
		if (personelERPList == null)
			personelERPList = new ArrayList<PersonelERP>();
		else
			personelERPList.clear();
		return "";

	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void detaysizSayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		setPdks(Boolean.FALSE);
		setTanimsizPersonelList(new ArrayList<PersonelView>());
		fillGorevTipiTanimList();
	}

	/**
	 * 
	 */
	public void fillDistinctRoleList() {
		Personel personel = getInstance();
		List<String> yoneticiOlmayanRoller = getPersonelYoneticiolmayanRoller(personel);
		List<Role> allRoles = ortakIslemler.yetkiRolleriGetir(session);
		for (Iterator iterator = allRoles.iterator(); iterator.hasNext();) {
			Role role = (Role) iterator.next();
			if (authenticatedUser.isAdmin() == false && role.isAdminRoleMu())
				iterator.remove();
			else if (role.getDepartman() != null && !departmanKullaniciList.contains(role.getDepartman()))
				iterator.remove();
			else if (yoneticiOlmayanRoller != null && role.isAdminRoleMu() == false) {
				String rolAdi = role.getRolename();
				if (!yoneticiOlmayanRoller.contains(rolAdi))
					iterator.remove();
			}
		}
		if (personel != null) {
			User seciliKullanici = personel.getKullanici();
			ortakIslemler.setUserRoller(seciliKullanici, session);
			if (seciliKullanici.getYetkiliRollerim() != null) {
				for (Iterator iterator = seciliKullanici.getYetkiliRollerim().iterator(); iterator.hasNext();) {
					Role role = (Role) iterator.next();
					if (authenticatedUser.isAdmin() == false && role.isAdminRoleMu()) {
						iterator.remove();
						continue;
					}
					if (allRoles.contains(role))
						allRoles.remove(role);
				}
			}
		}
		if (bosDepartman != null && personel.getId() != null && personel.getSirket().isErp()) {
			if (personel.getEkSaha1() == null || personel.getEkSaha1().getKodu().equals(bosDepartmanKodu)) {
				for (Iterator iterator = allRoles.iterator(); iterator.hasNext();) {
					Role role = (Role) iterator.next();
					if (role.getRolename().equals(Role.TIPI_DEPARTMAN_SUPER_VISOR)) {
						iterator.remove();
					}
				}
			}
		}
		yoneticiOlmayanRoller = null;
		setDistinctRoleList(allRoles);
	}

	/**
	 * @param personel
	 * @return
	 */
	private List<String> getPersonelYoneticiolmayanRoller(Personel personel) {
		boolean yoneticiPersonelEngelleDurum = !ortakIslemler.getParameterKey("yoneticiPersonelEngelleDurum").equals("1");
		List<String> yoneticiOlmayanRoller = null;
		if (yoneticiPersonelEngelleDurum && personel != null && personel.getId() != null) {
			yoneticiOlmayanRoller = Arrays.asList(new String[] { Role.TIPI_PERSONEL, Role.TIPI_IK, Role.TIPI_IK_Tesis, Role.TIPI_IK_DIREKTOR, Role.TIPI_GENEL_MUDUR });
			String yoneticiRolleriHaric = ortakIslemler.getParameterKey("yoneticiRolleriHaric");
			if (PdksUtil.hasStringValue(yoneticiRolleriHaric))
				yoneticiOlmayanRoller = PdksUtil.getListByString(yoneticiRolleriHaric, null);
			Date bugun = PdksUtil.getDate(Calendar.getInstance().getTime());
			HashMap parametreMap = new HashMap();
			parametreMap.put("yoneticisi.id=", personel.getId());
			parametreMap.put("iseBaslamaTarihi<=", bugun);
			parametreMap.put("sskCikisTarihi>=", bugun);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Personel> list = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, Personel.class);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Personel per = (Personel) iterator.next();
				if (per.getDurum().equals(Boolean.FALSE) || per.getId().equals(personel.getId()) || per.isCalisiyorGun(bugun) == false)
					iterator.remove();

			}

			if (!list.isEmpty())
				yoneticiOlmayanRoller = null;
			list = null;
		}
		return yoneticiOlmayanRoller;
	}

	public void fillDistinctTesisList() {
		List<Tanim> allTesis = ortakIslemler.isTesisDurumu() ? ortakIslemler.getTanimList(Tanim.TIPI_TESIS, session) : new ArrayList<Tanim>();
		Personel seciliPersonel = getInstance();
		if (seciliPersonel != null) {
			User seciiliKullanici = seciliPersonel.getKullanici();
			ortakIslemler.setUserTesisler(seciiliKullanici, session);
			if (seciiliKullanici.getYetkiliTesisler() != null)
				for (Tanim tesis : seciiliKullanici.getYetkiliTesisler()) {
					if (allTesis.contains(tesis))
						allTesis.remove(tesis);
				}
		}
		setDistinctTesisList(allTesis);
	}

	public String excelServiceAktar() {
		try {
			ByteArrayOutputStream baosDosya = excelServiceAktarDevam(personelList);
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, "personelWebServisListesi.xlsx");

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		return "";
	}

	/**
	 * @param personelList
	 * @param tanimMap
	 * @param user
	 * @return
	 * @throws Exception
	 */
	private ByteArrayOutputStream excelServiceAktarDevam(List<PersonelView> list) throws Exception {
		HashMap fields = new HashMap();
		fields.put("tipi", Tanim.TIPI_GENEL_TANIM);
		fields.put("kodu", Tanim.TIPI_BORDRO_ALT_BIRIMI);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		Tanim parentBordroTanim = (Tanim) pdksEntityController.getObjectByInnerObject(fields, Tanim.class);
		String bordroAltAlanStr = "";
		if (parentBordroTanim != null && PdksUtil.hasStringValue(parentBordroTanim.getErpKodu()))
			bordroAltAlanStr = parentBordroTanim.getErpKodu().trim().toLowerCase(PdksUtil.TR_LOCALE);
		HashMap<String, Integer> alanMap = setServisAlanlar();
		List<PersonelView> personelList = new ArrayList<PersonelView>(list);
		for (Iterator iterator = personelList.iterator(); iterator.hasNext();) {
			PersonelView personelView = (PersonelView) iterator.next();
			PersonelKGS personelKGS = personelView.getPersonelKGS();
			Sirket sirket = personelView.getPdksPersonel().getSirket();
			try {
				if (personelKGS.getDurum().equals(Boolean.FALSE) || sirket.isErp() == false || sirket.isPdksMi() == false)
					iterator.remove();
			} catch (Exception e) {
			}

		}

		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, "Personel WebService Listesi", false);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddDate = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATE, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenDate = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATE, wb);

		int row = 0;
		int col = 0;
		for (Iterator iterator = dosyaTanimList.iterator(); iterator.hasNext();) {
			Liste liste = (Liste) iterator.next();
			Tanim tanim = (Tanim) liste.getValue();
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(tanim.getAciklama());
		}
		boolean renk = true;
		for (Iterator iter = personelList.iterator(); iter.hasNext();) {
			PersonelView personelView = (PersonelView) iter.next();
			CellStyle style = null, styleCenter = null, styleDate = null;
			if (renk) {
				styleDate = styleOddDate;
				style = styleOdd;
				styleCenter = styleOddCenter;
			} else {
				styleDate = styleEvenDate;
				style = styleEven;
				styleCenter = styleEvenCenter;
			}
			renk = !renk;
			row++;
			col = 0;
			Personel personel = personelView.getPdksPersonel();
			try {
				Sirket sirket = personel.getSirket();
				Tanim cinsiyet = personel.getCinsiyet(), gorev = personel.getGorevTipi(), bolum = personel.getEkSaha3();
				Tanim masrafYeri = personel.getMasrafYeri(), bordroAltAlani = personel.getBordroAltAlan(), departman = personel.getEkSaha1(), tesis = personel.getTesis();
				if (bordroAltAlanStr.startsWith("eksaha2"))
					bordroAltAlani = personel.getEkSaha2();
				else if (bordroAltAlanStr.startsWith("eksaha4"))
					bordroAltAlani = personel.getEkSaha4();
				for (int i = 0; i < dosyaTanimList.size(); i++) {
					Liste liste = dosyaTanimList.get(i);
					Tanim tanim = (Tanim) liste.getValue();
					String kodu = tanim.getKodu();
					if (alanMap.containsKey(kodu)) {

						if (kodu.equals("SICIL_NO"))
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getSicilNo());
						else if (kodu.equals("ADI"))
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAd());
						else if (kodu.equals("SOYADI"))
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getSoyad());
						else if (kodu.equals("SIRKET_KODU"))
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(sirket.getErpKodu() != null ? sirket.getErpKodu() : "");
						else if (kodu.equals("SIRKET_ADI")) {
							if (COL_SIRKET_KODU >= 0)
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue(sirket.getAd());
							else
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue((sirket.getErpKodu() != null ? sirket.getErpKodu() + PdksUtil.SEPARATOR_KOD_ACIKLAMA : "") + sirket.getAd());
						} else if (kodu.equals("TESIS_KODU"))
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(tesis != null ? tesis.getErpKodu() : "");
						else if (kodu.equals("TESIS_ADI")) {
							if (COL_TESIS_KODU >= 0)
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue(tesis != null ? tesis.getAciklama() : "");
							else
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue(tesis != null ? tesis.getErpKodu() + PdksUtil.SEPARATOR_KOD_ACIKLAMA + tesis.getAciklama() : "");
						} else if (kodu.equals("GOREV_KODU"))
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(gorev != null ? gorev.getErpKodu() : "");
						else if (kodu.equals("GOREVI")) {
							if (COL_GOREV_KODU >= 0)
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue(gorev != null ? gorev.getAciklama() : "");
							else
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue(gorev != null ? gorev.getErpKodu() + PdksUtil.SEPARATOR_KOD_ACIKLAMA + gorev.getAciklama() : "");
						} else if (kodu.equals("BOLUM_KODU"))
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(bolum != null ? bolum.getErpKodu() : "");
						else if (kodu.equals("BOLUM_ADI")) {
							if (COL_BOLUM_KODU >= 0)
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue(bolum != null ? bolum.getAciklama() : "");
							else
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue(bolum != null ? bolum.getErpKodu() + PdksUtil.SEPARATOR_KOD_ACIKLAMA + bolum.getAciklama() : "");
						} else if (kodu.equals("CINSIYET_KODU"))
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(cinsiyet != null ? cinsiyet.getErpKodu() : "");
						else if (kodu.equals("CINSIYET")) {
							if (COL_CINSIYET_KODU >= 0)
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue(cinsiyet != null ? cinsiyet.getAciklama() : "");
							else
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue(cinsiyet != null ? cinsiyet.getErpKodu() + PdksUtil.SEPARATOR_KOD_ACIKLAMA + cinsiyet.getAciklama() : "");
						} else if (kodu.equals("BORDRO_ALT_ALAN_KODU"))
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(bordroAltAlani != null ? bordroAltAlani.getErpKodu() : "");
						else if (kodu.equals("BORDRO_ALT_ALAN_ADI")) {
							if (COL_BORDRO_ALT_ALAN_KODU >= 0)
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue(bordroAltAlani != null ? bordroAltAlani.getAciklama() : "");
							else
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue(bordroAltAlani != null ? bordroAltAlani.getErpKodu() + PdksUtil.SEPARATOR_KOD_ACIKLAMA + bordroAltAlani.getAciklama() : "");
						} else if (kodu.equals("DEPARTMAN_KODU"))
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(departman != null ? departman.getErpKodu() : "");
						else if (kodu.equals("DEPARTMAN_ADI")) {
							if (COL_DEPARTMAN_KODU >= 0)
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue(departman != null ? departman.getAciklama() : "");
							else
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue(departman != null ? departman.getErpKodu() + PdksUtil.SEPARATOR_KOD_ACIKLAMA + departman.getAciklama() : "");
						} else if (kodu.equals("MASRAF_YERI_KODU"))
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(masrafYeri != null ? masrafYeri.getErpKodu() : "");
						else if (kodu.equals("MASRAF_YERI_ADI")) {
							if (COL_MASRAF_YERI_KODU >= 0)
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue(masrafYeri != null ? masrafYeri.getAciklama() : "");
							else
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue(masrafYeri != null ? masrafYeri.getErpKodu() + PdksUtil.SEPARATOR_KOD_ACIKLAMA + masrafYeri.getAciklama() : "");
						} else if (kodu.equals("YONETICI_KODU"))
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getYoneticisi() != null ? personel.getYoneticisi().getPdksSicilNo() : "");
						else if (kodu.equals("SANAL_PERSONEL"))
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue(String.valueOf(personel.isSanalPersonelMi()));
						else if (kodu.equals("ISE_BASLAMA_TARIHI")) {
							if (personel.getIseBaslamaTarihi() != null)
								ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(personel.getIseBaslamaTarihi());
							else
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
						} else if (kodu.equals("KIDEM_TARIHI")) {
							if (personel.getIzinHakEdisTarihi() != null)
								ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(personel.getIzinHakEdisTarihi());
							else
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
						} else if (kodu.equals("ISTEN_AYRILMA_TARIHI")) {
							if (personel.getIstenAyrilisTarihi() != null)
								ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(personel.getIstenAyrilisTarihi());
							else
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
						} else if (kodu.equals("DOGUM_TARIHI")) {
							if (personel.getDogumTarihi() != null)
								ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(personel.getDogumTarihi());
							else
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
						} else if (kodu.equals("GRUBA_GIRIS_TARIHI")) {
							if (personel.getGrubaGirisTarihi() != null)
								ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(personel.getGrubaGirisTarihi());
							else
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
						}

					} else
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				}
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				logger.info(row + " " + personel.getPdksSicilNo());

			}
		}
		try {

			for (int i = 0; i < dosyaTanimList.size(); i++)
				sheet.autoSizeColumn(i);
			baos = new ByteArrayOutputStream();
			wb.write(baos);
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			baos = null;
		}
		return baos;
	}

	private void ekSahaDisable() {
		ekSaha1Disable = false;
		ekSaha2Disable = false;
		ekSaha4Disable = false;
		Personel personel = getInstance();
		boolean admin = authenticatedUser.isAdmin();
		if (admin == false && !personelERPGuncelleme.equals("1") && !personelERPGuncelleme.equals("M")) {
			ekSaha2Disable = true;
			ekSaha4Disable = true;
			if (personel != null && personel.getSirket() != null && personel.getSirket().getDepartman().isAdminMi()) {
				HashMap fields = new HashMap();
				fields.put("tipi", Tanim.TIPI_GENEL_TANIM);
				fields.put("kodu", Tanim.TIPI_BORDRO_ALT_BIRIMI);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				Tanim parentBordroTanim = (Tanim) pdksEntityController.getObjectByInnerObject(fields, Tanim.class);
				String parentBordroTanimKodu = Tanim.TIPI_BORDRO_ALT_BIRIMI;
				String parentBordroTanimKoduStr = "";
				if (parentBordroTanim != null) {
					parentBordroTanimKoduStr = parentBordroTanim.getErpKodu();
					if (parentBordroTanimKoduStr == null)
						parentBordroTanimKoduStr = "";
					else {
						parentBordroTanimKodu = parentBordroTanimKoduStr.trim();
					}

				}
				parentBordroTanimKoduStr = parentBordroTanimKodu.toLowerCase(Constants.TR_LOCALE);
				ekSaha1Disable = !admin;
				if (admin == false) {
					if (PdksUtil.hasStringValue(parentBordroTanimKoduStr)) {
						if (!parentBordroTanimKoduStr.equals("eksaha2"))
							ekSaha2Disable = false;
						if (!parentBordroTanimKoduStr.equals("eksaha4"))
							ekSaha4Disable = false;
					}
				}

			}

		}
	}

	public String excelAktar(List<PersonelView> list) {
		try {
			if (list == null)
				list = personelList;
			ByteArrayOutputStream baosDosya = ortakIslemler.personelExcelDevam(Boolean.TRUE, list, ekSahaTanimMap, authenticatedUser, personelDinamikMap, session);
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, "personelListesi.xlsx");

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		return "";
	}

	public List<Role> getDistinctRoleList() {
		return distinctRoleList;
	}

	public void setDistinctRoleList(List<Role> distinctRoleList) {
		this.distinctRoleList = distinctRoleList;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getTarih() {
		return tarih;
	}

	public void setTarih(Date tarih) {
		this.tarih = tarih;
	}

	public Date getBugun() {
		return PdksUtil.buGun();
	}

	public List<VardiyaSablonu> getSablonlar() {
		return sablonlar;
	}

	public void setSablonlar(List<VardiyaSablonu> sablonlar) {
		this.sablonlar = sablonlar;
	}

	public List<Sirket> getSirketList() {
		return sirketList;
	}

	public void setSirketList(List<Sirket> sirketList) {
		this.sirketList = sirketList;
	}

	public List<PersonelKGS> getpersonelKGSList() {
		return personelKGSList;
	}

	public void setpersonelKGSList(List<PersonelKGS> personelKGSList) {
		this.personelKGSList = personelKGSList;
	}

	public List<PersonelView> getTanimsizPersonelList() {
		return tanimsizPersonelList;
	}

	public void setTanimsizPersonelList(List<PersonelView> tanimsizPersonelList) {
		this.tanimsizPersonelList = tanimsizPersonelList;
	}

	public List<Personel> getPdksPersonelList() {
		return pdksPersonelList;
	}

	public void setPdksPersonelList(List<Personel> pdksPersonelList) {
		this.pdksPersonelList = pdksPersonelList;
	}

	public String getAdi() {
		return adi;
	}

	public void setAdi(String adi) {
		this.adi = adi;
	}

	public String getSoyadi() {
		return soyadi;
	}

	public void setSoyadi(String soyadi) {
		this.soyadi = soyadi;
	}

	public String getSicilNo() {
		return sicilNo;
	}

	public void setSicilNo(String sicilNo) {
		this.sicilNo = sicilNo;
	}

	public List<Departman> getDepartmanTanimList() {
		return departmanTanimList;
	}

	public void setDepartmanTanimList(List<Departman> departmanTanimList) {
		this.departmanTanimList = departmanTanimList;
	}

	public List<Tanim> getDepartmanPDKSTanimList() {
		return departmanPDKSTanimList;
	}

	public void setDepartmanPDKSTanimList(List<Tanim> departmanPDKSTanimList) {
		this.departmanPDKSTanimList = departmanPDKSTanimList;
	}

	public PersonelIzin getBakiyeIzin() {
		return bakiyeIzin;
	}

	public void setBakiyeIzin(PersonelIzin bakiyeIzin) {
		this.bakiyeIzin = bakiyeIzin;
	}

	public PersonelView getPersonelView() {
		return personelView;
	}

	public void setPersonelView(PersonelView personelView) {
		this.personelView = personelView;
	}

	public String getOldUserName() {
		return oldUserName;
	}

	public void setOldUserName(String oldUserName) {
		this.oldUserName = oldUserName;
	}

	public Sirket getOldSirket() {
		return oldSirket;
	}

	public void setOldSirket(Sirket oldSirket) {
		this.oldSirket = oldSirket;
	}

	public String getHataMesaj() {
		return hataMesaj;
	}

	public void setHataMesaj(String hataMesaj) {
		this.hataMesaj = hataMesaj;
	}

	public List<PersonelView> getPersonelList() {
		return personelList;
	}

	public void setPersonelList(List<PersonelView> personelList) {
		this.personelList = personelList;
	}

	public Boolean getPdks() {
		return pdks;
	}

	public void setPdks(Boolean pdks) {
		this.pdks = pdks;
	}

	public List<Tanim> getCinsiyetList() {
		return cinsiyetList;
	}

	public void setCinsiyetList(List<Tanim> cinsiyetList) {
		this.cinsiyetList = cinsiyetList;
	}

	public Personel getAsilYonetici1() {
		return asilYonetici1;
	}

	public void setAsilYonetici1(Personel asilYonetici1) {
		this.asilYonetici1 = asilYonetici1;
	}

	public List<Personel> getYoneticiList() {
		return yoneticiList;
	}

	public void setYoneticiList(List<Personel> yoneticiList) {
		this.yoneticiList = yoneticiList;
	}

	public String getYoneticiTipi() {
		return yoneticiTipi;
	}

	public void setYoneticiTipi(String yoneticiTipi) {
		this.yoneticiTipi = yoneticiTipi;
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

	public Double getBakiyeIzinSuresi() {
		return bakiyeIzinSuresi;
	}

	public void setBakiyeIzinSuresi(Double bakiyeIzinSuresi) {
		this.bakiyeIzinSuresi = bakiyeIzinSuresi;
	}

	public List<Tanim> getMasrafYeriList() {
		return masrafYeriList;
	}

	public void setMasrafYeriList(List<Tanim> masrafYeriList) {
		this.masrafYeriList = masrafYeriList;
	}

	public List<String> getCcAdresList() {
		return ccAdresList;
	}

	public void setCcAdresList(List<String> ccAdresList) {
		this.ccAdresList = ccAdresList;
	}

	public String getCcAdres() {
		return ccAdres;
	}

	public void setCcAdres(String ccAdres) {
		this.ccAdres = ccAdres;
	}

	public String getAdresTipi() {
		return adresTipi;
	}

	public void setAdresTipi(String adresTipi) {
		this.adresTipi = adresTipi;
	}

	public List<String> getBccAdresList() {
		return bccAdresList;
	}

	public void setBccAdresList(List<String> bccAdresList) {
		this.bccAdresList = bccAdresList;
	}

	public List<Tanim> getYoneticiVardiyaTipiList() {
		return yoneticiVardiyaTipiList;
	}

	public void setYoneticiVardiyaTipiList(List<Tanim> yoneticiVardiyaTipiList) {
		this.yoneticiVardiyaTipiList = yoneticiVardiyaTipiList;
	}

	public List<Tanim> getVardiyaGirisTipiTanimList() {
		return vardiyaGirisTipiTanimList;
	}

	public void setVardiyaGirisTipiTanimList(List<Tanim> vardiyaGirisTipiTanimList) {
		this.vardiyaGirisTipiTanimList = vardiyaGirisTipiTanimList;
	}

	public PersonelExtra getPersonelExtra() {
		return personelExtra;
	}

	public void setPersonelExtra(PersonelExtra personelExtra) {
		this.personelExtra = personelExtra;
	}

	public List<Tanim> getBolumDepartmanlari() {
		return bolumDepartmanlari;
	}

	public void setBolumDepartmanlari(List<Tanim> bolumDepartmanlari) {
		this.bolumDepartmanlari = bolumDepartmanlari;
	}

	public List<Tanim> getUnvanTanimList() {
		return unvanTanimList;
	}

	public void setUnvanTanimList(List<Tanim> unvanTanimList) {
		this.unvanTanimList = unvanTanimList;
	}

	public List<String> getHareketAdresList() {
		return hareketAdresList;
	}

	public void setHareketAdresList(List<String> hareketAdresList) {
		this.hareketAdresList = hareketAdresList;
	}

	public List<PersonelKGS> getPersonelKGSList() {
		return personelKGSList;
	}

	public void setPersonelKGSList(List<PersonelKGS> personelKGSList) {
		this.personelKGSList = personelKGSList;
	}

	public Dosya getPersonelDosya() {
		return personelDosya;
	}

	public void setPersonelDosya(Dosya personelDosya) {
		this.personelDosya = personelDosya;
	}

	public List<PersonelERP> getPersonelERPList() {
		return personelERPList;
	}

	public void setPersonelERPList(List<PersonelERP> personelERPList) {
		this.personelERPList = personelERPList;
	}

	public String getPersonelERPGuncelleme() {
		return personelERPGuncelleme;
	}

	public void setPersonelERPGuncelleme(String personelERPGuncelleme) {
		this.personelERPGuncelleme = personelERPGuncelleme;
	}

	public Boolean getServisCalisti() {
		return servisCalisti;
	}

	public void setServisCalisti(Boolean servisCalisti) {
		this.servisCalisti = servisCalisti;
	}

	public List<Tanim> getTesisList() {
		return tesisList;
	}

	public void setTesisList(List<Tanim> tesisList) {
		this.tesisList = tesisList;
	}

	public int getCOL_SICIL_NO() {
		return COL_SICIL_NO;
	}

	public void setCOL_SICIL_NO(int cOL_SICIL_NO) {
		COL_SICIL_NO = cOL_SICIL_NO;
	}

	public int getCOL_ADI() {
		return COL_ADI;
	}

	public void setCOL_ADI(int cOL_ADI) {
		COL_ADI = cOL_ADI;
	}

	public int getCOL_SOYADI() {
		return COL_SOYADI;
	}

	public void setCOL_SOYADI(int cOL_SOYADI) {
		COL_SOYADI = cOL_SOYADI;
	}

	public int getCOL_SIRKET_KODU() {
		return COL_SIRKET_KODU;
	}

	public void setCOL_SIRKET_KODU(int cOL_SIRKET_KODU) {
		COL_SIRKET_KODU = cOL_SIRKET_KODU;
	}

	public int getCOL_SIRKET_ADI() {
		return COL_SIRKET_ADI;
	}

	public void setCOL_SIRKET_ADI(int cOL_SIRKET_ADI) {
		COL_SIRKET_ADI = cOL_SIRKET_ADI;
	}

	public int getCOL_TESIS_KODU() {
		return COL_TESIS_KODU;
	}

	public void setCOL_TESIS_KODU(int cOL_TESIS_KODU) {
		COL_TESIS_KODU = cOL_TESIS_KODU;
	}

	public int getCOL_TESIS_ADI() {
		return COL_TESIS_ADI;
	}

	public void setCOL_TESIS_ADI(int cOL_TESIS_ADI) {
		COL_TESIS_ADI = cOL_TESIS_ADI;
	}

	public int getCOL_GOREV_KODU() {
		return COL_GOREV_KODU;
	}

	public void setCOL_GOREV_KODU(int cOL_GOREV_KODU) {
		COL_GOREV_KODU = cOL_GOREV_KODU;
	}

	public int getCOL_GOREVI() {
		return COL_GOREVI;
	}

	public void setCOL_GOREVI(int cOL_GOREVI) {
		COL_GOREVI = cOL_GOREVI;
	}

	public int getCOL_BOLUM_KODU() {
		return COL_BOLUM_KODU;
	}

	public void setCOL_BOLUM_KODU(int cOL_BOLUM_KODU) {
		COL_BOLUM_KODU = cOL_BOLUM_KODU;
	}

	public int getCOL_BOLUM_ADI() {
		return COL_BOLUM_ADI;
	}

	public void setCOL_BOLUM_ADI(int cOL_BOLUM_ADI) {
		COL_BOLUM_ADI = cOL_BOLUM_ADI;
	}

	public int getCOL_ISE_BASLAMA_TARIHI() {
		return COL_ISE_BASLAMA_TARIHI;
	}

	public void setCOL_ISE_BASLAMA_TARIHI(int cOL_ISE_BASLAMA_TARIHI) {
		COL_ISE_BASLAMA_TARIHI = cOL_ISE_BASLAMA_TARIHI;
	}

	public int getCOL_KIDEM_TARIHI() {
		return COL_KIDEM_TARIHI;
	}

	public void setCOL_KIDEM_TARIHI(int cOL_KIDEM_TARIHI) {
		COL_KIDEM_TARIHI = cOL_KIDEM_TARIHI;
	}

	public int getCOL_ISTEN_AYRILMA_TARIHI() {
		return COL_ISTEN_AYRILMA_TARIHI;
	}

	public void setCOL_ISTEN_AYRILMA_TARIHI(int cOL_ISTEN_AYRILMA_TARIHI) {
		COL_ISTEN_AYRILMA_TARIHI = cOL_ISTEN_AYRILMA_TARIHI;
	}

	public int getCOL_DOGUM_TARIHI() {
		return COL_DOGUM_TARIHI;
	}

	public void setCOL_DOGUM_TARIHI(int cOL_DOGUM_TARIHI) {
		COL_DOGUM_TARIHI = cOL_DOGUM_TARIHI;
	}

	public int getCOL_CINSIYET_KODU() {
		return COL_CINSIYET_KODU;
	}

	public void setCOL_CINSIYET_KODU(int cOL_CINSIYET_KODU) {
		COL_CINSIYET_KODU = cOL_CINSIYET_KODU;
	}

	public int getCOL_CINSIYET() {
		return COL_CINSIYET;
	}

	public void setCOL_CINSIYET(int cOL_CINSIYET) {
		COL_CINSIYET = cOL_CINSIYET;
	}

	public int getCOL_YONETICI_KODU() {
		return COL_YONETICI_KODU;
	}

	public void setCOL_YONETICI_KODU(int cOL_YONETICI_KODU) {
		COL_YONETICI_KODU = cOL_YONETICI_KODU;
	}

	public int getCOL_DEPARTMAN_KODU() {
		return COL_DEPARTMAN_KODU;
	}

	public void setCOL_DEPARTMAN_KODU(int cOL_DEPARTMAN_KODU) {
		COL_DEPARTMAN_KODU = cOL_DEPARTMAN_KODU;
	}

	public int getCOL_DEPARTMAN_ADI() {
		return COL_DEPARTMAN_ADI;
	}

	public void setCOL_DEPARTMAN_ADI(int cOL_DEPARTMAN_ADI) {
		COL_DEPARTMAN_ADI = cOL_DEPARTMAN_ADI;
	}

	public int getCOL_MASRAF_YERI_KODU() {
		return COL_MASRAF_YERI_KODU;
	}

	public void setCOL_MASRAF_YERI_KODU(int cOL_MASRAF_YERI_KODU) {
		COL_MASRAF_YERI_KODU = cOL_MASRAF_YERI_KODU;
	}

	public int getCOL_MASRAF_YERI_ADI() {
		return COL_MASRAF_YERI_ADI;
	}

	public void setCOL_MASRAF_YERI_ADI(int cOL_MASRAF_YERI_ADI) {
		COL_MASRAF_YERI_ADI = cOL_MASRAF_YERI_ADI;
	}

	public int getCOL_BORDRO_ALT_ALAN_KODU() {
		return COL_BORDRO_ALT_ALAN_KODU;
	}

	public void setCOL_BORDRO_ALT_ALAN_KODU(int cOL_BORDRO_ALT_ALAN_KODU) {
		COL_BORDRO_ALT_ALAN_KODU = cOL_BORDRO_ALT_ALAN_KODU;
	}

	public int getCOL_BORDRO_ALT_ALAN_ADI() {
		return COL_BORDRO_ALT_ALAN_ADI;
	}

	public void setCOL_BORDRO_ALT_ALAN_ADI(int cOL_BORDRO_ALT_ALAN_ADI) {
		COL_BORDRO_ALT_ALAN_ADI = cOL_BORDRO_ALT_ALAN_ADI;
	}

	public int getCOL_BORDRO_SANAL_PERSONEL() {
		return COL_BORDRO_SANAL_PERSONEL;
	}

	public void setCOL_BORDRO_SANAL_PERSONEL(int cOL_BORDRO_SANAL_PERSONEL) {
		COL_BORDRO_SANAL_PERSONEL = cOL_BORDRO_SANAL_PERSONEL;
	}

	public List<CalismaModeli> getCalismaModeliList() {
		return calismaModeliList;
	}

	public void setCalismaModeliList(List<CalismaModeli> calismaModeliList) {
		this.calismaModeliList = calismaModeliList;
	}

	public String getSanalPersonelAciklama() {
		return sanalPersonelAciklama;
	}

	public void setSanalPersonelAciklama(String sanalPersonelAciklama) {
		this.sanalPersonelAciklama = sanalPersonelAciklama;
	}

	public List<Tanim> getGorevDepartmanlari() {
		return gorevDepartmanlari;
	}

	public void setGorevDepartmanlari(List<Tanim> gorevDepartmanlari) {
		this.gorevDepartmanlari = gorevDepartmanlari;
	}

	public Boolean getFazlaMesaiIzinKullan() {
		return fazlaMesaiIzinKullan;
	}

	public void setFazlaMesaiIzinKullan(Boolean fazlaMesaiIzinKullan) {
		this.fazlaMesaiIzinKullan = fazlaMesaiIzinKullan;
	}

	public Boolean getFazlaMesaiOde() {
		return fazlaMesaiOde;
	}

	public void setFazlaMesaiOde(Boolean fazlaMesaiOde) {
		this.fazlaMesaiOde = fazlaMesaiOde;
	}

	public Boolean getSanalPersonel() {
		return sanalPersonel;
	}

	public void setSanalPersonel(Boolean sanalPersonel) {
		this.sanalPersonel = sanalPersonel;
	}

	public Boolean getKullaniciPersonel() {
		return kullaniciPersonel;
	}

	public void setKullaniciPersonel(Boolean kullaniciPersonel) {
		this.kullaniciPersonel = kullaniciPersonel;
	}

	public Boolean getGebeMi() {
		return gebeMi;
	}

	public void setGebeMi(Boolean gebeMi) {
		this.gebeMi = gebeMi;
	}

	public Boolean getSutIzni() {
		return sutIzni;
	}

	public void setSutIzni(Boolean sutIzni) {
		this.sutIzni = sutIzni;
	}

	public Boolean getIcapDurum() {
		return icapDurum;
	}

	public void setIcapDurum(Boolean icapDurum) {
		this.icapDurum = icapDurum;
	}

	public Boolean getUstYonetici() {
		return ustYonetici;
	}

	public void setUstYonetici(Boolean ustYonetici) {
		this.ustYonetici = ustYonetici;
	}

	public Boolean getPartTimeDurum() {
		return partTimeDurum;
	}

	public void setPartTimeDurum(Boolean partTimeDurum) {
		this.partTimeDurum = partTimeDurum;
	}

	public Boolean getEgitimDonemi() {
		return egitimDonemi;
	}

	public void setEgitimDonemi(Boolean egitimDonemi) {
		this.egitimDonemi = egitimDonemi;
	}

	public Boolean getSuaOlabilir() {
		return suaOlabilir;
	}

	public void setSuaOlabilir(Boolean suaOlabilir) {
		this.suaOlabilir = suaOlabilir;
	}

	public Boolean getEmailCCDurum() {
		return emailCCDurum;
	}

	public void setEmailCCDurum(Boolean emailCCDurum) {
		this.emailCCDurum = emailCCDurum;
	}

	public Boolean getEmailBCCDurum() {
		return emailBCCDurum;
	}

	public void setEmailBCCDurum(Boolean emailBCCDurum) {
		this.emailBCCDurum = emailBCCDurum;
	}

	public List<UserMenuItemTime> getMenuItemTimeList() {
		return menuItemTimeList;
	}

	public void setMenuItemTimeList(List<UserMenuItemTime> menuItemTimeList) {
		this.menuItemTimeList = menuItemTimeList;
	}

	public String getePosta() {
		return ePosta;
	}

	public void setePosta(String ePosta) {
		this.ePosta = ePosta;
	}

	public String getDenemeMesaj() {
		return denemeMesaj;
	}

	public void setDenemeMesaj(String denemeMesaj) {
		this.denemeMesaj = denemeMesaj;
	}

	public String getKartNoAciklama() {
		return kartNoAciklama;
	}

	public void setKartNoAciklama(String kartNoAciklama) {
		this.kartNoAciklama = kartNoAciklama;
	}

	public List<Liste> getDosyaTanimList() {
		return dosyaTanimList;
	}

	public void setDosyaTanimList(List<Liste> dosyaTanimList) {
		this.dosyaTanimList = dosyaTanimList;
	}

	public List<Tanim> getDistinctTesisList() {
		return distinctTesisList;
	}

	public void setDistinctTesisList(List<Tanim> distinctTesisList) {
		this.distinctTesisList = distinctTesisList;
	}

	public Boolean getTesisYetki() {
		return tesisYetki;
	}

	public void setTesisYetki(Boolean tesisYetki) {
		this.tesisYetki = tesisYetki;
	}

	public User getEskiKullanici() {
		return eskiKullanici;
	}

	public void setEskiKullanici(User eskiKullanici) {
		this.eskiKullanici = eskiKullanici;
	}

	public List<Departman> getDepartmanKullaniciList() {
		return departmanKullaniciList;
	}

	public List<PersonelERP> getPersonelERPReturnList() {
		return personelERPReturnList;
	}

	public void setPersonelERPReturnList(List<PersonelERP> personelERPReturnList) {
		this.personelERPReturnList = personelERPReturnList;
	}

	public void setDepartmanKullaniciList(List<Departman> departmanKullaniciList) {
		this.departmanKullaniciList = departmanKullaniciList;
	}

	public Boolean getTaseronKulaniciTanimla() {
		return taseronKulaniciTanimla;
	}

	public void setTaseronKulaniciTanimla(Boolean taseronKulaniciTanimla) {
		this.taseronKulaniciTanimla = taseronKulaniciTanimla;
	}

	public Boolean getManuelTanimla() {
		return manuelTanimla;
	}

	public void setManuelTanimla(Boolean manuelTanimla) {
		this.manuelTanimla = manuelTanimla;
	}

	public int getCOL_GRUBA_GIRIS_TARIHI() {
		return COL_GRUBA_GIRIS_TARIHI;
	}

	public void setCOL_GRUBA_GIRIS_TARIHI(int cOL_GRUBA_GIRIS_TARIHI) {
		COL_GRUBA_GIRIS_TARIHI = cOL_GRUBA_GIRIS_TARIHI;
	}

	public List<Tanim> getDinamikDurumList() {
		return dinamikDurumList;
	}

	public void setDinamikDurumList(List<Tanim> dinamikDurumList) {
		this.dinamikDurumList = dinamikDurumList;
	}

	public List<Tanim> getDinamikSayisalList() {
		return dinamikSayisalList;
	}

	public void setDinamikSayisalList(List<Tanim> dinamikSayisalList) {
		this.dinamikSayisalList = dinamikSayisalList;
	}

	public List<Tanim> getDinamikAciklamaList() {
		return dinamikAciklamaList;
	}

	public void setDinamikAciklamaList(List<Tanim> dinamikAciklamaList) {
		this.dinamikAciklamaList = dinamikAciklamaList;
	}

	public TreeMap<String, PersonelDinamikAlan> getPersonelDinamikMap() {
		return personelDinamikMap;
	}

	public void setPersonelDinamikMap(TreeMap<String, PersonelDinamikAlan> personelDinamikMap) {
		this.personelDinamikMap = personelDinamikMap;
	}

	public PersonelDinamikAlan getPersonelDinamikAlan() {
		return personelDinamikAlan;
	}

	public void setPersonelDinamikAlan(PersonelDinamikAlan personelDinamikAlan) {
		this.personelDinamikAlan = personelDinamikAlan;
	}

	public List<PersonelDinamikAlan> getDinamikPersonelDurumList() {
		return dinamikPersonelDurumList;
	}

	public void setDinamikPersonelDurumList(List<PersonelDinamikAlan> dinamikPersonelDurumList) {
		this.dinamikPersonelDurumList = dinamikPersonelDurumList;
	}

	public List<PersonelDinamikAlan> getDinamikPersonelSayisalList() {
		return dinamikPersonelSayisalList;
	}

	public void setDinamikPersonelSayisalList(List<PersonelDinamikAlan> dinamikPersonelSayisalList) {
		this.dinamikPersonelSayisalList = dinamikPersonelSayisalList;
	}

	public List<PersonelDinamikAlan> getDinamikPersonelAciklamaList() {
		return dinamikPersonelAciklamaList;
	}

	public void setDinamikPersonelAciklamaList(List<PersonelDinamikAlan> dinamikPersonelAciklamaList) {
		this.dinamikPersonelAciklamaList = dinamikPersonelAciklamaList;
	}

	public HashMap<Long, List<Tanim>> getDinamikPersonelAciklamaMap() {
		return dinamikPersonelAciklamaMap;
	}

	public void setDinamikPersonelAciklamaMap(HashMap<Long, List<Tanim>> dinamikPersonelAciklamaMap) {
		this.dinamikPersonelAciklamaMap = dinamikPersonelAciklamaMap;
	}

	public int getCOL_YONETICI2_KODU() {
		return COL_YONETICI2_KODU;
	}

	public void setCOL_YONETICI2_KODU(int cOL_YONETICI2_KODU) {
		COL_YONETICI2_KODU = cOL_YONETICI2_KODU;
	}

	public Boolean getIkinciYoneticiManuelTanimla() {
		return ikinciYoneticiManuelTanimla;
	}

	public void setIkinciYoneticiManuelTanimla(Boolean ikinciYoneticiManuelTanimla) {
		this.ikinciYoneticiManuelTanimla = ikinciYoneticiManuelTanimla;
	}

	public Boolean getOnaysizIzinKullanilir() {
		return onaysizIzinKullanilir;
	}

	public void setOnaysizIzinKullanilir(Boolean onaysizIzinKullanilir) {
		this.onaysizIzinKullanilir = onaysizIzinKullanilir;
	}

	public Boolean getIkinciYoneticiIzinOnayla() {
		return ikinciYoneticiIzinOnayla;
	}

	public void setIkinciYoneticiIzinOnayla(Boolean ikinciYoneticiIzinOnayla) {
		this.ikinciYoneticiIzinOnayla = ikinciYoneticiIzinOnayla;
	}

	public HashMap<String, Boolean> getPersonelDurumMap() {
		return personelDurumMap;
	}

	public void setPersonelDurumMap(HashMap<String, Boolean> personelDurumMap) {
		this.personelDurumMap = personelDurumMap;
	}

	public Boolean getIzinGirisiVar() {
		return izinGirisiVar;
	}

	public void setIzinGirisiVar(Boolean izinGirisiVar) {
		this.izinGirisiVar = izinGirisiVar;
	}

	public Boolean getDosyaGuncellemeYetki() {
		return dosyaGuncellemeYetki;
	}

	public void setDosyaGuncellemeYetki(Boolean dosyaGuncellemeYetki) {
		this.dosyaGuncellemeYetki = dosyaGuncellemeYetki;
	}

	public Boolean getYoneticiRolVarmi() {
		return yoneticiRolVarmi;
	}

	public void setYoneticiRolVarmi(Boolean yoneticiRolVarmi) {
		this.yoneticiRolVarmi = yoneticiRolVarmi;
	}

	public Tanim getBosDepartman() {
		return bosDepartman;
	}

	public void setBosDepartman(Tanim bosDepartman) {
		this.bosDepartman = bosDepartman;
	}

	public Tanim getParentDepartman() {
		return parentDepartman;
	}

	public void setParentDepartman(Tanim parentDepartman) {
		this.parentDepartman = parentDepartman;
	}

	public List<Personel> getYonetici2List() {
		return yonetici2List;
	}

	public void setYonetici2List(List<Personel> yonetici2List) {
		this.yonetici2List = yonetici2List;
	}

	public Boolean getDepartmanGoster() {
		return departmanGoster;
	}

	public void setDepartmanGoster(Boolean departmanGoster) {
		this.departmanGoster = departmanGoster;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public List<VardiyaSablonu> getSablonIskurList() {
		return sablonIskurList;
	}

	public void setSablonIskurList(List<VardiyaSablonu> sablonIskurList) {
		this.sablonIskurList = sablonIskurList;
	}

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}

	public Tanim getParentBordroTanim() {
		return parentBordroTanim;
	}

	public void setParentBordroTanim(Tanim parentBordroTanim) {
		this.parentBordroTanim = parentBordroTanim;
	}

	public Boolean getKimlikNoGoster() {
		return kimlikNoGoster;
	}

	public void setKimlikNoGoster(Boolean kimlikNoGoster) {
		this.kimlikNoGoster = kimlikNoGoster;
	}

	public String getDepartmanAciklama() {
		return departmanAciklama;
	}

	public void setDepartmanAciklama(String departmanAciklama) {
		this.departmanAciklama = departmanAciklama;
	}

	public String getAltBolumAciklama() {
		return altBolumAciklama;
	}

	public void setAltBolumAciklama(String altBolumAciklama) {
		this.altBolumAciklama = altBolumAciklama;
	}

	public List<Personel> getIkinciYoneticiHataliList() {
		return ikinciYoneticiHataliList;
	}

	public void setIkinciYoneticiHataliList(List<Personel> ikinciYoneticiHataliList) {
		this.ikinciYoneticiHataliList = ikinciYoneticiHataliList;
	}

	public Boolean getIstenAyrilmaGoster() {
		return istenAyrilmaGoster;
	}

	public void setIstenAyrilmaGoster(Boolean istenAyrilmaGoster) {
		this.istenAyrilmaGoster = istenAyrilmaGoster;
	}

	public Boolean getTesisDurum() {
		return tesisDurum;
	}

	public void setTesisDurum(Boolean tesisDurum) {
		this.tesisDurum = tesisDurum;
	}

	public Boolean getEkSaha1Disable() {
		return ekSaha1Disable;
	}

	public void setEkSaha1Disable(Boolean ekSaha1Disable) {
		this.ekSaha1Disable = ekSaha1Disable;
	}

	public Boolean getEkSaha2Disable() {
		return ekSaha2Disable;
	}

	public void setEkSaha2Disable(Boolean ekSaha2Disable) {
		this.ekSaha2Disable = ekSaha2Disable;
	}

	public Boolean getEkSaha4Disable() {
		return ekSaha4Disable;
	}

	public void setEkSaha4Disable(Boolean ekSaha4Disable) {
		this.ekSaha4Disable = ekSaha4Disable;
	}

	public Boolean getKartNoGoster() {
		return kartNoGoster;
	}

	public void setKartNoGoster(Boolean kartNoGoster) {
		this.kartNoGoster = kartNoGoster;
	}

	public Boolean getTransferAciklamaCiftKontrol() {
		return transferAciklamaCiftKontrol;
	}

	public void setTransferAciklamaCiftKontrol(Boolean transferAciklamaCiftKontrol) {
		this.transferAciklamaCiftKontrol = transferAciklamaCiftKontrol;
	}

	public TreeMap<Long, PersonelKGS> getPersonelKGSMap() {
		return personelKGSMap;
	}

	public void setPersonelKGSMap(TreeMap<Long, PersonelKGS> personelKGSMap) {
		this.personelKGSMap = personelKGSMap;
	}
}
