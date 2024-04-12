package org.pdks.session;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.AylikPuantaj;
import org.pdks.entity.BordroDetayTipi;
import org.pdks.entity.CalismaModeli;
import org.pdks.entity.CalismaModeliAy;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Departman;
import org.pdks.entity.DepartmanDenklestirmeDonemi;
import org.pdks.entity.FazlaMesaiTalep;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelDenklestirmeBordro;
import org.pdks.entity.PersonelDenklestirmeBordroDetay;
import org.pdks.entity.PersonelDenklestirmeDinamikAlan;
import org.pdks.entity.PersonelDenklestirmeTasiyici;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.VardiyaHafta;
import org.pdks.entity.VardiyaSaat;
import org.pdks.entity.YemekIzin;
import org.pdks.security.action.UserHome;
import org.pdks.security.entity.User;

@Name("fazlaMesaiOzetRaporHome")
public class FazlaMesaiOzetRaporHome extends EntityHome<DepartmanDenklestirmeDonemi> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5201033120905302620L;
	static Logger logger = Logger.getLogger(FazlaMesaiOzetRaporHome.class);
	public static String sayfaURL = "fazlaMesaiOzetRapor";

	@RequestParameter
	Long personelDenklestirmeId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	UserHome userHome;
	@In(required = false, create = true)
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;

	@Out(scope = ScopeType.SESSION, required = false)
	String linkAdres;
	@Out(scope = ScopeType.SESSION, required = false)
	VardiyaGun fazlaMesaiVardiyaGun;
	@In(required = true, create = true)
	Renderer renderer;

	private List<PersonelDenklestirme> personelDenklestirmeList;

	private List<SelectItem> bolumDepartmanlari, gorevYeriList, tesisList;

	private List<AylikPuantaj> aylikPuantajList;

	private List<DepartmanDenklestirmeDonemi> denklestirmeDonemiList;

	private List<PersonelDenklestirme> baslikDenklestirmeDonemiList;

	private HashMap<String, List<Tanim>> ekSahaListMap;

	private List<Tanim> denklestirmeDinamikAlanlar;

	private VardiyaGun vardiyaGun;

	private Sirket sirket;

	private DenklestirmeAy denklestirmeAy;

	private TreeMap<String, Tatil> tatilGunleriMap;

	private Boolean hataYok, fazlaMesaiIzinKullan = Boolean.FALSE, fazlaMesaiOde = Boolean.FALSE, yetkili = Boolean.FALSE, resmiTatilVar = Boolean.FALSE, haftaTatilVar = Boolean.FALSE, kaydetDurum = Boolean.FALSE;
	private Boolean sutIzniGoster = Boolean.FALSE, partTimeGoster = Boolean.FALSE, onayla, hastaneSuperVisor = Boolean.FALSE, sirketIzinGirisDurum = Boolean.FALSE, hataliPuantajVar = Boolean.FALSE;
	private Boolean kimlikGoster = Boolean.FALSE, aksamGun = Boolean.FALSE, maasKesintiGoster = Boolean.FALSE, aksamSaat = Boolean.FALSE, hataliPuantajGoster = Boolean.FALSE, stajerSirket, departmanBolumAyni = Boolean.FALSE;
	private Boolean modelGoster = Boolean.FALSE, kullaniciPersonel = Boolean.FALSE, sirketGoster = Boolean.FALSE, denklestirmeAyDurum = Boolean.FALSE, yoneticiERP1Kontrol = Boolean.FALSE, yasalFazlaCalismaAsanSaat = Boolean.FALSE;
	private boolean adminRole, ikRole, bordroPuantajEkranindaGoster = false, fazlaMesaiVar = false, saatlikMesaiVar = false, aylikMesaiVar = false;
	private Boolean gerceklesenMesaiKod = Boolean.FALSE, devredenBakiyeKod = Boolean.FALSE, normalCalismaSaatKod = Boolean.FALSE, haftaTatilCalismaSaatKod = Boolean.FALSE, resmiTatilCalismaSaatKod = Boolean.FALSE, izinSureSaatKod = Boolean.FALSE;
	private Boolean normalCalismaGunKod = Boolean.FALSE, haftaTatilCalismaGunKod = Boolean.FALSE, resmiTatilCalismaGunKod = Boolean.FALSE, izinSureGunKod = Boolean.FALSE, ucretliIzinGunKod = Boolean.FALSE, ucretsizIzinGunKod = Boolean.FALSE, hastalikIzinGunKod = Boolean.FALSE;
	private Boolean normalGunKod = Boolean.FALSE, haftaTatilGunKod = Boolean.FALSE, resmiTatilGunKod = Boolean.FALSE, artikGunKod = Boolean.FALSE, bordroToplamGunKod = Boolean.FALSE, devredenMesaiKod = Boolean.FALSE, ucretiOdenenKod = Boolean.FALSE;
	private TreeMap<String, Boolean> baslikMap;

	private int ay, yil, maxYil;

	private List<User> toList, ccList, bccList;

	private TreeMap<Long, List<FazlaMesaiTalep>> fmtMap;

	private List<FazlaMesaiTalep> fmtList;

	private List<SelectItem> aylar;

	private AylikPuantaj aylikPuantajDefault;

	private TreeMap<String, Tanim> ekSahaTanimMap;

	private String msgError, msgFazlaMesaiError, sanalPersonelAciklama, bolumAciklama;
	private String sicilNo = "", excelDosyaAdi, mailKonu, mailIcerik;
	private List<YemekIzin> yemekAraliklari;
	private CalismaModeli perCalismaModeli;
	private Long seciliEkSaha3Id, sirketId = null, departmanId, gorevTipiId, tesisId;
	private Tanim gorevYeri, seciliBolum, tesis;

	private Double toplamFazlamMesai = 0D;
	private Double aksamCalismaSaati = null, aksamCalismaSaatiYuzde = null;
	private byte[] excelData;

	private boolean mailGonder, tekSirket;
	private Boolean bakiyeGuncelle, ayrikHareketVar;

	private List<SelectItem> pdksSirketList, departmanList;
	private Departman departman;
	private String adres, personelIzinGirisiStr, personelHareketStr, personelFazlaMesaiOrjStr, personelFazlaMesaiStr, vardiyaPlaniStr;
	private List<String> sabahVardiyalar;
	private Vardiya sabahVardiya;
	private Session session;
	private Integer aksamVardiyaBasSaat, aksamVardiyaBasDakika, aksamVardiyaBitDakika;
	private TreeMap<String, Tanim> fazlaMesaiMap;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	@Override
	public Object getId() {
		if (personelDenklestirmeId == null) {
			return super.getId();
		} else {
			return personelDenklestirmeId;
		}
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

	/**
	 * 
	 */
	private void adminRoleDurum() {
		adminRole = authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || authenticatedUser.isIKAdmin();
		ikRole = authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || authenticatedUser.isIK();
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() {
		boolean ayniSayfa = authenticatedUser.getCalistigiSayfa() != null && authenticatedUser.getCalistigiSayfa().equals("fazlaMesaiOzetRapor");
		if (!ayniSayfa)
			authenticatedUser.setCalistigiSayfa("fazlaMesaiOzetRapor");
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		if (baslikMap == null)
			baslikMap = new TreeMap<String, Boolean>();
		else
			baslikMap.clear();
		sirketGoster = Boolean.FALSE;
		fazlaMesaiVardiyaGun = null;
		adminRoleDurum();
		if (!authenticatedUser.isAdmin()) {
			if (departmanId == null && !authenticatedUser.isYoneticiKontratli())
				setDepartmanId(authenticatedUser.getDepartman().getId());

			// fillSirketList();
		}
		try {
			modelGoster = Boolean.FALSE;
			departmanBolumAyni = Boolean.FALSE;
			bakiyeGuncelle = null;
			stajerSirket = Boolean.FALSE;
			sutIzniGoster = Boolean.FALSE;
			partTimeGoster = Boolean.FALSE;
			mailGonder = Boolean.FALSE;
			kimlikGoster = Boolean.FALSE;
			yasalFazlaCalismaAsanSaat = Boolean.FALSE;
			hataliPuantajVar = Boolean.FALSE;
			fazlaMesaiOde = Boolean.FALSE;
			fazlaMesaiIzinKullan = Boolean.FALSE;
			setSirket(null);
			sirketId = null;
			setTesisId(null);
			setTesisList(null);
			seciliEkSaha3Id = null;
			Calendar cal = Calendar.getInstance();
			ortakIslemler.gunCikar(cal, 2);
			ay = cal.get(Calendar.MONTH) + 1;
			yil = cal.get(Calendar.YEAR);
			maxYil = yil + 1;
			aylikPuantajList = new ArrayList<AylikPuantaj>();

			setInstance(new DepartmanDenklestirmeDonemi());
			// setSirket(null);

			if (authenticatedUser.isSuperVisor() || authenticatedUser.isProjeMuduru()) {
				setSirket(authenticatedUser.getPdksPersonel().getSirket());
				bolumDoldur();
			}

			Departman pdksDepartman = null;
			if (!authenticatedUser.isAdmin())
				pdksDepartman = authenticatedUser.getDepartman();

			getInstance().setDepartman(pdksDepartman);

			hastaneSuperVisor = Boolean.FALSE;
			if (!(authenticatedUser.isIK() || authenticatedUser.isAdmin()) && authenticatedUser.getSuperVisorHemsirePersonelNoList() != null) {
				String superVisorHemsireSayfalari = ortakIslemler.getParameterKey("superVisorHemsireSayfalari");
				List<String> sayfalar = PdksUtil.hasStringValue(superVisorHemsireSayfalari) ? PdksUtil.getListByString(superVisorHemsireSayfalari, null) : null;
				hastaneSuperVisor = sayfalar != null && sayfalar.contains("fazlaMesaiOzetRapor");

			}

			if (!hastaneSuperVisor && (authenticatedUser.isAdmin() || authenticatedUser.getDepartman().isAdminMi())) {
				List<Tanim> statuTanimList = null;
				HashMap fields = new HashMap();
				if (authenticatedUser.isYonetici() || authenticatedUser.isYoneticiKontratli()) {
					if (!authenticatedUser.isIKAdmin())
						fields.put("pdksSicilNo<>", authenticatedUser.getPdksPersonel().getPdksSicilNo());
					fields.put("pdksSicilNo", authenticatedUser.getYetkiTumPersonelNoList());
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<Personel> list = pdksEntityController.getObjectByInnerObjectListInLogic(fields, Personel.class);
					TreeMap<Long, Tanim> tanimMap = new TreeMap<Long, Tanim>();
					for (Personel personel : list) {
						if (personel.getEkSaha3() != null)
							tanimMap.put(personel.getEkSaha3().getId(), personel.getEkSaha3());

					}
					statuTanimList = new ArrayList<Tanim>(tanimMap.values());
					tanimMap = null;
					list = null;
				} else {
					fields.put("parentTanim.kodu", "ekSaha3");
					fields.put("parentTanim.tipi", Tanim.TIPI_PERSONEL_EK_SAHA);
					fields.put("durum", Boolean.TRUE);
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					statuTanimList = pdksEntityController.getObjectByInnerObjectList(fields, Tanim.class);
				}

				if (statuTanimList != null && !statuTanimList.isEmpty()) {

					if (statuTanimList.size() > 1)
						statuTanimList = PdksUtil.sortObjectStringAlanList(statuTanimList, "getAciklama", null);
					else {
						gorevYeri = statuTanimList.get(0);
						seciliEkSaha3Id = gorevYeri.getId();
					}

				}

			}

			setPersonelDenklestirmeList(new ArrayList<PersonelDenklestirme>());
			HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

			String ayStr = (String) req.getParameter("ay");
			String yilStr = (String) req.getParameter("yil");
			String linkAdresKey = (String) req.getParameter("linkAdresKey");
			boolean hareketDoldur = false;
			String gorevTipiIdStr = null, gorevYeriIdStr = null, sirketIdStr = null, tesisIdStr = null;
			LinkedHashMap<String, Object> veriLastMap = null;
			if (linkAdresKey == null) {
				veriLastMap = ortakIslemler.getLastParameter("fazlaMesaiOzetRapor", session);
				if (veriLastMap != null) {
					if (veriLastMap.containsKey("yil"))
						yilStr = (String) veriLastMap.get("yil");
					if (veriLastMap.containsKey("ay"))
						ayStr = (String) veriLastMap.get("ay");
					if (veriLastMap.containsKey("sirketId"))
						sirketIdStr = (String) veriLastMap.get("sirketId");
					if (veriLastMap.containsKey("tesisId"))
						tesisIdStr = (String) veriLastMap.get("tesisId");
					if (veriLastMap.containsKey("bolumId"))
						gorevYeriIdStr = (String) veriLastMap.get("bolumId");
					if ((authenticatedUser.isIK() || authenticatedUser.isAdmin()) && veriLastMap.containsKey("sicilNo"))
						sicilNo = (String) veriLastMap.get("sicilNo");

				}
			}
			if (linkAdresKey != null || (ayStr != null && yilStr != null)) {
				if (linkAdresKey != null) {
					HashMap<String, String> veriMap = PdksUtil.getDecodeMapByBase64(linkAdresKey);
					if (veriMap.containsKey("yil"))
						yilStr = veriMap.get("yil");
					if (veriMap.containsKey("ay"))
						ayStr = veriMap.get("ay");
					if (veriMap.containsKey("sirketId"))
						sirketIdStr = veriMap.get("sirketId");
					if (veriMap.containsKey("tesisId"))
						tesisIdStr = veriMap.get("tesisId");
					if (veriMap.containsKey("sicilNo"))
						sicilNo = veriMap.get("sicilNo");
					if (veriMap.containsKey("gorevTipiId"))
						gorevTipiIdStr = veriMap.get("gorevTipiId");
					if (veriMap.containsKey("gorevYeriId"))
						gorevYeriIdStr = veriMap.get("gorevYeriId");
					veriMap = null;
				} else if (veriLastMap == null || veriLastMap.isEmpty()) {
					gorevTipiIdStr = (String) req.getParameter("gorevTipiId");
					gorevYeriIdStr = (String) req.getParameter("gorevYeriId");
					tesisIdStr = (String) req.getParameter("tesisId");
					sirketIdStr = (String) req.getParameter("sirketId");
				}

				if (yilStr != null && ayStr != null) {
					yil = Integer.parseInt(yilStr);
					ay = Integer.parseInt(ayStr);
					if (sirketIdStr != null) {
						sirketId = Long.parseLong(sirketIdStr);
						if (sirket != null) {
							if (!sirket.getId().equals(sirketId))
								sirket = null;
						}
						HashMap parametreMap = new HashMap();

						parametreMap.put("id", sirketId);
						if (session != null)
							parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						sirket = (Sirket) pdksEntityController.getObjectByInnerObject(parametreMap, Sirket.class);
						if (sirket != null) {
							departmanId = sirket.getDepartman().getId();
							fillSirketList();
							if (sirket != null)
								sirketId = sirket.getId();
							tesisDoldur(false);
						}

					}
					if (sirket != null) {
						departmanId = sirket.getDepartman().getId();
						setDepartman(sirket.getDepartman());
					}
					if (gorevTipiIdStr != null)
						gorevTipiId = Long.parseLong(gorevTipiIdStr);
					if (gorevYeriIdStr != null)
						seciliEkSaha3Id = Long.parseLong(gorevYeriIdStr);
					hareketDoldur = true;

				}

			}
			linkAdres = null;
			if (!authenticatedUser.isAdmin() && !authenticatedUser.isIK() && !authenticatedUser.isYoneticiKontratli()) {
				sirket = authenticatedUser.getPdksPersonel().getSirket();
				sirketId = sirket.getId();
			}

			HashMap parametreMap = new HashMap();
			if (departmanId != null)
				parametreMap.put("id", departmanId);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			setDepartman(departmanId != null ? (Departman) pdksEntityController.getObjectByInnerObject(parametreMap, Departman.class) : null);
			if (tesisIdStr != null) {
				if (!tesisList.isEmpty())
					setTesisId(Long.parseLong(tesisIdStr));
				else
					tesisIdStr = null;
			}
			if (departman != null && !departman.isAdminMi()) {
				if (bolumDepartmanlari == null && departman != null)
					bolumDepartmanlari = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, null, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, Boolean.TRUE, session);
			} else if (sirketId != null)
				tesisDoldur(false);
			if (tesisIdStr != null)
				setTesisId(Long.parseLong(tesisIdStr));
			bolumDoldur();
			if (veriLastMap == null && hareketDoldur)
				fillFazlaMesaiOzetRaporList();
			denklestirmeAyDurum = denklestirmeAy != null && denklestirmeAy.getDurum();
			if (denklestirmeAyDurum.equals(Boolean.FALSE))
				hataliPuantajGoster = denklestirmeAyDurum;
			if (!ayniSayfa)
				authenticatedUser.setCalistigiSayfa("");
		} catch (Exception e) {
			e.printStackTrace();
		}
		kullaniciPersonel = ortakIslemler.getKullaniciPersonel(authenticatedUser);
		if (kullaniciPersonel) {
			tesisList = null;
			sicilNo = authenticatedUser.getPdksPersonel().getPdksSicilNo();
		}
		fillEkSahaTanim();
		yilDegisti();
		return "";
	}

	/**
	 * 
	 */
	private void setSeciliDenklestirmeAy() {
		if (aylikPuantajList != null)
			aylikPuantajList.clear();
		if (denklestirmeAy == null && ay > 0) {
			HashMap fields = new HashMap();
			fields.put("ay", ay);
			fields.put("yil", yil);
			if (aylikPuantajList != null)
				aylikPuantajList.clear();
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
			if (denklestirmeAy != null) {
				if (denklestirmeAy.getFazlaMesaiMaxSure() == null)
					fazlaMesaiOrtakIslemler.setFazlaMesaiMaxSure(denklestirmeAy, session);
				fields.clear();
				fields.put(PdksEntityController.MAP_KEY_SELECT, "id");
				fields.put("denklestirmeAy.id", denklestirmeAy.getId());
				fields.put("denklestirme", Boolean.TRUE);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<Long> idList = pdksEntityController.getObjectByInnerObjectList(fields, PersonelDenklestirme.class);
				if (idList.isEmpty()) {
					denklestirmeAy = null;
					PdksUtil.addMessageAvailableWarn((ay > 0 ? yil + " " + (aylar.get(ay - 1).getLabel()) : "") + " döneme ait denkleştirme verisi tanımlanmamıştır!");
				}
				idList = null;
			} else
				PdksUtil.addMessageAvailableError((ay > 0 ? yil + " " + (aylar.get(ay - 1).getLabel()) : "") + " döneme ait çalışma planı tanımlanmamıştır!");
		}
		setDenklestirmeAyDurum(fazlaMesaiOrtakIslemler.getDurum(denklestirmeAy));
	}

	private void fillDepartmanList() {
		if (denklestirmeAy == null)
			setSeciliDenklestirmeAy();
		List<SelectItem> departmanListe = fazlaMesaiOrtakIslemler.getFazlaMesaiDepartmanList(denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, true, session);
		if (!departmanListe.isEmpty()) {
			Long onceki = departmanId;
			if (departmanListe.size() == 1)
				departmanId = (Long) departmanListe.get(0).getValue();
			else if (onceki != null) {
				for (SelectItem st : departmanListe) {
					if (st.getValue().equals(onceki))
						departmanId = onceki;
				}
			}
		} else
			departmanId = null;
		setDepartmanList(departmanListe);
	}

	private void yilDegisti() {
		if (aylar == null)
			aylar = new ArrayList<SelectItem>();
		ay = fazlaMesaiOrtakIslemler.aylariDoldur(yil, ay, aylar, session);
	}

	public String departmanDegisti(boolean degisti) {
		yilDegisti();
		if (ay <= 0 && !aylar.isEmpty())
			ay = (Integer) aylar.get(aylar.size() - 1).getValue();

		if (degisti) {
			setSeciliDenklestirmeAy();
			sirketId = null;
			if (tesisList != null)
				tesisList.clear();
			if (gorevYeriList != null)
				gorevYeriList.clear();
			if (bolumDepartmanlari != null)
				bolumDepartmanlari.clear();
			denklestirmeAy = null;
		}
		fillSirketList();
		if (!pdksSirketList.isEmpty()) {
			boolean bolumDoldurulmadi = true;
			if (sirketId != null || pdksSirketList.size() == 1) {
				Long tesisIdOnceki = tesisId;
				if (pdksSirketList.size() == 1)
					sirketId = (Long) pdksSirketList.get(0).getValue();
				try {

					tesisDoldur(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (tesisList.size() == 1) {
					tesisId = (Long) tesisList.get(0).getValue();
					bolumDoldur();
					bolumDoldurulmadi = false;
				} else if (tesisIdOnceki != null && !tesisList.isEmpty()) {
					for (SelectItem si : tesisList) {
						Long id = (Long) si.getValue();
						if (id.equals(tesisIdOnceki))
							tesisId = tesisIdOnceki;
					}
					if (tesisId == null)
						seciliEkSaha3Id = null;
				}
			}
			if (bolumDoldurulmadi)
				if (tesisId != null || seciliEkSaha3Id != null || (sirket != null && sirket.isTesisDurumu() == false))
					bolumDoldur();
		}
		aylikPuantajList.clear();
		return "";
	}

	private void bolumleriTemizle() {
		gorevYeriList = null;

	}

	public void fillSirketList() {
		if (adminRole)
			fillDepartmanList();
		List<SelectItem> sirketler = null;
		bolumleriTemizle();

		try {
			if (denklestirmeAy == null)
				setSeciliDenklestirmeAy();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		if (departmanId != null) {
			HashMap parametreMap = new HashMap();
			parametreMap.put("id", departmanId);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			setDepartman((Departman) pdksEntityController.getObjectByInnerObject(parametreMap, Departman.class));

		} else
			setDepartman(null);

		if (gorevYeriList != null)
			gorevYeriList.clear();
		if (ikRole || authenticatedUser.isYonetici()) {
			Long depId = departman != null ? departman.getId() : null;
			sirketler = fazlaMesaiOrtakIslemler.getFazlaMesaiSirketList(depId, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, true, session);
			sirket = null;
			if (!sirketler.isEmpty()) {
				Long onceki = sirketId;
				if (sirketler.size() == 1) {
					sirketId = (Long) sirketler.get(0).getValue();
				} else if (onceki != null) {
					if (ikRole)
						sirketId = null;
					for (SelectItem st : sirketler) {
						if (st.getValue().equals(onceki))
							sirketId = onceki;
					}
				}
				if (sirketId != null) {
					HashMap map = new HashMap();
					map.put("id", sirketId);
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					sirket = (Sirket) pdksEntityController.getObjectByInnerObject(map, Sirket.class);
				}
			}
			setPdksSirketList(sirketler);
		} else {
			setSirket(authenticatedUser.getPdksPersonel().getSirket());
		}

		if (aylikPuantajList == null)
			aylikPuantajList = new ArrayList<AylikPuantaj>();
		else
			aylikPuantajList.clear();
		setPersonelDenklestirmeList(new ArrayList<PersonelDenklestirme>());

	}

	public String fillPersonelSicilDenklestirmeList() {
		if (!PdksUtil.hasStringValue(sicilNo))
			aylikPuantajList.clear();
		else {
			sicilNo = ortakIslemler.getSicilNo(sicilNo);
			fillFazlaMesaiOzetRaporList();
		}

		return "";
	}

	@Transactional
	public String fillFazlaMesaiOzetRaporList() {
		aksamGun = Boolean.FALSE;
		aksamSaat = Boolean.FALSE;
		haftaTatilVar = Boolean.FALSE;
		maasKesintiGoster = Boolean.FALSE;
		sirketGoster = Boolean.FALSE;
		mailGonder = !(authenticatedUser.isIK() || authenticatedUser.isAdmin());
		linkAdres = null;
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.clear();
		// fillSirketList();
		HashMap fields = new HashMap();
		fields.put("ay", ay);
		fields.put("yil", yil);
		personelDenklestirmeList.clear();
		ayrikHareketVar = false;
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
		denklestirmeAyDurum = denklestirmeAy != null && denklestirmeAy.getDurum();
		if (denklestirmeAy != null) {
			try {
				DepartmanDenklestirmeDonemi denklestirmeDonemi = new DepartmanDenklestirmeDonemi();
				AylikPuantaj aylikPuantaj = fazlaMesaiOrtakIslemler.getAylikPuantaj(ay, yil, denklestirmeDonemi, session);
				denklestirmeDonemi.setDenklestirmeAy(denklestirmeAy);
				fillFazlaMesaiOzetRaporDevam(aylikPuantaj, denklestirmeDonemi);
			} catch (Exception ee) {
				logger.error(ee);
				ee.printStackTrace();
			}

		} else
			PdksUtil.addMessageWarn("İlgili döneme ait fazla mesai bulunamadı!");
		if (!(authenticatedUser.isIK() || authenticatedUser.isAdmin()))
			departmanBolumAyni = false;
		return "";
	}

	public String kaydetSec() {
		for (AylikPuantaj puantaj : aylikPuantajList) {
			PersonelDenklestirme personelDenklestirmeAylik = puantaj.getPersonelDenklestirme();
			if (puantaj.isDonemBitti() && personelDenklestirmeAylik.isOnaylandi() && personelDenklestirmeAylik.getDurum() && puantaj.isFazlaMesaiHesapla() && !personelDenklestirmeAylik.isErpAktarildi())
				puantaj.setKaydet(kaydetDurum);
			else
				puantaj.setKaydet(Boolean.FALSE);

		}
		return "";
	}

	/**
	 * @param denklestirmeDonemi
	 * @param puantajList
	 */
	protected void calismaPlaniDenklestir(DepartmanDenklestirmeDonemi denklestirmeDonemi, List<AylikPuantaj> puantajList) {
		String idariVardiyaKisaAdi = ortakIslemler.getParameterKey("idariVardiyaKisaAdi");
		Vardiya normalCalismaVardiya = ortakIslemler.getNormalCalismaVardiya(idariVardiyaKisaAdi, session);
		LinkedHashMap<String, Object> dataMap = new LinkedHashMap<String, Object>();
		dataMap.put("aylikPuantajList", puantajList);
		dataMap.put("basTarih", denklestirmeDonemi.getBaslangicTarih());
		dataMap.put("bitTarih", denklestirmeDonemi.getBitisTarih());
		dataMap.put("normalCalismaVardiya", normalCalismaVardiya);
		dataMap.put("denklestirmeAyDurum", denklestirmeAyDurum);
		dataMap.put("tatilGunleriMap", tatilGunleriMap);
		fazlaMesaiOrtakIslemler.calismaPlaniDenklestir(dataMap, session);
	}

	/**
	 * @param aylikPuantajSablon
	 * @param denklestirmeDonemi
	 */
	@Transactional
	public void fillFazlaMesaiOzetRaporDevam(AylikPuantaj aylikPuantajSablon, DepartmanDenklestirmeDonemi denklestirmeDonemi) throws Exception {
		fazlaMesaiVardiyaGun = null;
		bordroPuantajEkranindaGoster = ortakIslemler.getParameterKey("bordroPuantajEkranindaGoster").equals("1");
		fazlaMesaiVar = false;
		saatlikMesaiVar = false;
		aylikMesaiVar = false;
		yoneticiERP1Kontrol = !ortakIslemler.getParameterKeyHasStringValue("yoneticiERP1Kontrol");
		Map<String, String> map1 = null;
		sanalPersonelAciklama = ortakIslemler.sanalPersonelAciklama();
		sabahVardiya = null;
		departmanBolumAyni = Boolean.FALSE;
		aksamGun = Boolean.FALSE;
		aksamSaat = Boolean.FALSE;
		haftaTatilVar = Boolean.FALSE;
		maasKesintiGoster = Boolean.FALSE;
		fazlaMesaiIzinKullan = Boolean.FALSE;
		kimlikGoster = Boolean.FALSE;
		yasalFazlaCalismaAsanSaat = Boolean.FALSE;
		hataliPuantajVar = Boolean.FALSE;
		fazlaMesaiOde = Boolean.FALSE;
		sirketIzinGirisDurum = Boolean.FALSE;
		LinkedHashMap<String, Object> lastMap = new LinkedHashMap<String, Object>();
		if (fmtMap == null)
			fmtMap = new TreeMap<Long, List<FazlaMesaiTalep>>();
		else
			fmtMap.clear();
		lastMap.put("yil", "" + yil);
		lastMap.put("ay", "" + ay);
		if (departmanId != null)
			lastMap.put("departmanId", "" + departmanId);
		if (sirketId != null)
			lastMap.put("sirketId", "" + sirketId);
		if (tesisId != null)
			lastMap.put("tesisId", "" + tesisId);
		if (seciliEkSaha3Id != null)
			lastMap.put("bolumId", "" + seciliEkSaha3Id);

		if ((authenticatedUser.isIK() || authenticatedUser.isAdmin()) && PdksUtil.hasStringValue(sicilNo))
			lastMap.put("sicilNo", sicilNo.trim());
		try {

			map1 = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
			lastMap.put("sayfaURL", sayfaURL);
			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception e) {

		}
		List<AylikPuantaj> puantajList = new ArrayList();
		DenklestirmeAy gecenAy = null;
		try {
			departmanBolumAyni = sirket != null && sirket.isTesisDurumu() == false;
			adres = map1.containsKey("host") ? map1.get("host") : "";
			if (sicilNo != null)
				sicilNo = sicilNo.trim();
			hataYok = Boolean.FALSE;
			sutIzniGoster = Boolean.FALSE;
			partTimeGoster = Boolean.FALSE;
			aylikPuantajSablon.getVardiyalar();
			setAylikPuantajDefault(aylikPuantajSablon);
			kaydetDurum = Boolean.FALSE;
			String aksamBordroBasZamani = ortakIslemler.getParameterKey("aksamBordroBasZamani"), aksamBordroBitZamani = ortakIslemler.getParameterKey("aksamBordroBitZamani");
			Integer[] basZaman = ortakIslemler.getSaatDakika(aksamBordroBasZamani), bitZaman = ortakIslemler.getSaatDakika(aksamBordroBitZamani);
			aksamVardiyaBasSaat = basZaman[0];
			aksamVardiyaBasDakika = basZaman[1];
			aksamVardiyaBitDakika = bitZaman[1];

			seciliBolum = null;

			setVardiyaGun(null);
			HashMap map = new HashMap();

			HashMap fields = new HashMap();
			Sirket sirket = null;
			if (sirketId != null && sirketId > 0) {
				fields.put("id", sirketId);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);
				fields.clear();
			}
			List<Personel> donemPerList = fazlaMesaiOrtakIslemler.getFazlaMesaiPersonelList(sirket, tesisId != null ? String.valueOf(tesisId) : null, seciliEkSaha3Id, null, denklestirmeAy != null ? aylikPuantajSablon : null, true, session);
			List<Long> perIdList = new ArrayList<Long>();

			for (Personel personel : donemPerList) {
				if (PdksUtil.hasStringValue(sicilNo) == false || sicilNo.trim().equals(personel.getPdksSicilNo().trim())) {
					perIdList.add(personel.getId());
				}

			}
			List<PersonelDenklestirme> personelDenklestirmeler = null;
			if (!perIdList.isEmpty())
				personelDenklestirmeler = getPdksPersonelDenklestirmeler(perIdList);
			else
				personelDenklestirmeler = new ArrayList<PersonelDenklestirme>();

			HashMap<Long, PersonelDenklestirme> personelDenklestirmeMap = new HashMap<Long, PersonelDenklestirme>();
			TreeMap<Long, PersonelDenklestirme> personelDenklestirmeDonemMap = new TreeMap<Long, PersonelDenklestirme>();
			if (personelDenklestirmeler.isEmpty()) {

				PdksUtil.addMessageWarn("Çalışma planı kaydı bulunmadı!");

			}
			for (Iterator iterator = personelDenklestirmeler.iterator(); iterator.hasNext();) {
				PersonelDenklestirme personelDenklestirme = (PersonelDenklestirme) iterator.next();
				if (personelDenklestirme == null || personelDenklestirme.getPersonel() == null) {
					iterator.remove();
					continue;
				}
				personelDenklestirmeDonemMap.put(personelDenklestirme.getPersonelId(), personelDenklestirme);
				personelDenklestirme.setGuncellendi(personelDenklestirme.getId() == null);
				if (personelDenklestirme.isDenklestirmeDurum()) {
					personelDenklestirmeMap.put(personelDenklestirme.getPersonelId(), personelDenklestirme);

				} else
					iterator.remove();

			}
			Date bugun = new Date(), sonCikisZamani = null, sonCalismaGunu = aylikPuantajSablon.getIlkGun();

			Calendar cal = Calendar.getInstance();
			for (VardiyaGun vardiyaGun : aylikPuantajSablon.getVardiyalar()) {
				cal.setTime(vardiyaGun.getVardiyaDate());
				if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY && cal.getTime().after(sonCalismaGunu))
					sonCalismaGunu = vardiyaGun.getVardiyaDate();
			}

			boolean fazlaMesaiOnayla = denklestirmeDonemi.getDurum() && bugun.after(sonCalismaGunu);

			if (!personelDenklestirmeler.isEmpty()) {
				if (sirket != null && denklestirmeAyDurum && userHome.hasPermission("personelIzinGirisi", "view")) {
					map.clear();
					map.put("departman.id=", sirket.getDepartman().getId());
					map.put("durum=", Boolean.TRUE);
					map.put("personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
					map.put("bakiyeIzinTipi=", null);
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<IzinTipi> izinTipiList = pdksEntityController.getObjectByInnerObjectListInLogic(map, IzinTipi.class);
					sirketIzinGirisDurum = !izinTipiList.isEmpty();
				}
				fazlaMesaiMap = ortakIslemler.getFazlaMesaiMap(session);
				Map<String, String> requestHeaderMap = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
				adres = requestHeaderMap.containsKey("host") ? requestHeaderMap.get("host") : "";
				sabahVardiyalar = null;
				String sabahVardiyaKisaAdlari = ortakIslemler.getParameterKey("sabahVardiyaKisaAdlari");
				if (PdksUtil.hasStringValue(sabahVardiyaKisaAdlari))
					sabahVardiyalar = PdksUtil.getListByString(sabahVardiyaKisaAdlari, null);
				else
					sabahVardiyalar = Arrays.asList(new String[] { "S", "Sİ", "SI" });
				String gunduzVardiyaVar = ortakIslemler.getParameterKey("gunduzVardiyaVar");
				if (gunduzVardiyaVar.equals("1")) {
					map.clear();
					map.put("kisaAdi", sabahVardiyalar);
					map.put("departman.id", departmanId);
					map.put("durum", Boolean.TRUE);
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					sabahVardiya = (Vardiya) pdksEntityController.getObjectByInnerObject(map, Vardiya.class);
				} else
					sabahVardiya = null;

				map.clear();
				setInstance(denklestirmeDonemi);
				tatilGunleriMap = ortakIslemler.getTatilGunleri(null, ortakIslemler.tariheGunEkleCikar(cal, denklestirmeDonemi.getBaslangicTarih(), -1), ortakIslemler.tariheGunEkleCikar(cal, denklestirmeDonemi.getBitisTarih(), 1), session);
				List<PersonelDenklestirmeTasiyici> list = null;
				try {
					denklestirmeDonemi.setPersonelDenklestirmeDonemMap(personelDenklestirmeDonemMap);
					list = new ArrayList<PersonelDenklestirmeTasiyici>();
					for (PersonelDenklestirme personelDenklestirme : personelDenklestirmeler) {
						PersonelDenklestirmeTasiyici denklestirmeTasiyici = new PersonelDenklestirmeTasiyici();
						denklestirmeTasiyici.setPersonel(personelDenklestirme.getPersonel());
						denklestirmeTasiyici.setCalismaModeli(personelDenklestirme.getCalismaModeli());
						denklestirmeTasiyici.setDenklestirmeAy(denklestirmeAy);
						list.add(denklestirmeTasiyici);
					}
					ortakIslemler.personelDenklestirmeDuzenle(list, aylikPuantajDefault, tatilGunleriMap, session);
				} catch (Exception ex) {
					list = new ArrayList<PersonelDenklestirmeTasiyici>();
					logger.equals(ex);
					ex.printStackTrace();
				}
				HashMap<Long, Long> sirketMap = new HashMap<Long, Long>();
				if (list.size() > 1) {
					list = PdksUtil.sortObjectStringAlanList(list, "getAdSoyad", null);
					if (seciliEkSaha3Id == null) {
						List<Tanim> bolumList = new ArrayList<Tanim>();
						HashMap<Long, List<PersonelDenklestirmeTasiyici>> map2 = new HashMap<Long, List<PersonelDenklestirmeTasiyici>>();
						for (Iterator iterator = list.iterator(); iterator.hasNext();) {
							PersonelDenklestirmeTasiyici personelDenklestirmeTasiyici = (PersonelDenklestirmeTasiyici) iterator.next();
							Personel personel = personelDenklestirmeTasiyici.getPersonel();
							sirketMap.put(personel.getSirket().getId(), personel.getId());
							if (personel.getEkSaha3() == null)
								continue;
							Tanim tanim = personel.getEkSaha3();
							List<PersonelDenklestirmeTasiyici> list2 = map2.containsKey(tanim.getId()) ? map2.get(tanim.getId()) : new ArrayList<PersonelDenklestirmeTasiyici>();
							if (list2.isEmpty()) {
								bolumList.add(tanim);
								map2.put(tanim.getId(), list2);
							}
							list2.add(personelDenklestirmeTasiyici);
							iterator.remove();
						}
						if (bolumList.size() > 1)
							bolumList = PdksUtil.sortObjectStringAlanList(bolumList, "getAciklama", null);
						for (Tanim tanim : bolumList) {
							list.addAll(map2.get(tanim.getId()));
						}
						bolumList = null;
						map2 = null;
					}
				}
				sirketGoster = sirketMap.size() > 1 || sirket == null || sirket.getSirketGrup() != null;

				sirketMap = null;
				boolean renk = Boolean.TRUE;
				aylikPuantajSablon = fazlaMesaiOrtakIslemler.getAylikPuantaj(ay, yil, denklestirmeDonemi, session);

				List<VardiyaHafta> vardiyaHaftaList = new ArrayList<VardiyaHafta>();
				fazlaMesaiOrtakIslemler.haftalikVardiyaOlustur(vardiyaHaftaList, aylikPuantajSablon, denklestirmeDonemi, tatilGunleriMap, null);
				resmiTatilVar = Boolean.FALSE;
				haftaTatilVar = Boolean.FALSE;

				linkAdres = "<a href='http://"
						+ adres
						+ "/fazlaMesaiHesapla?linkAdresKey="
						+ PdksUtil.getEncodeStringByBase64("yil=" + yil + "&ay=" + ay + (seciliEkSaha3Id != null ? "&gorevYeriId=" + seciliEkSaha3Id : "") + (tesisId != null ? "&tesisId=" + tesisId : "") + (gorevTipiId != null ? "&gorevTipiId=" + gorevTipiId : "")
								+ (sirket != null ? "&sirketId=" + sirket.getId() : "") + (PdksUtil.hasStringValue(sicilNo) ? "&sicilNo=" + sicilNo.trim() : "")) + "'>" + ortakIslemler.getCalistiMenuAdi("fazlaMesaiOzetRapor") + " Ekranına Geri Dön</a>";

				List<String> gunList = new ArrayList<String>();
				for (Iterator iterator = aylikPuantajDefault.getAyinVardiyalari().iterator(); iterator.hasNext();) {
					VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
					gunList.add(vardiyaGun.getVardiyaDateStr());
				}
				personelIzinGirisiStr = ortakIslemler.getCalistiMenuAdi("personelIzinGirisi");

				personelHareketStr = ortakIslemler.getCalistiMenuAdi("personelHareket");
				personelFazlaMesaiOrjStr = ortakIslemler.getCalistiMenuAdi("personelFazlaMesai");
				vardiyaPlaniStr = ortakIslemler.getCalistiMenuAdi("vardiyaPlani");
				onayla = Boolean.FALSE;

				List saveList = new ArrayList();
				msgError = ortakIslemler.getParameterKey("msgErrorResim");
				if (!PdksUtil.hasStringValue(msgError))
					msgError = "msgerror.png";
				msgFazlaMesaiError = ortakIslemler.getParameterKey("msgFazlaMesaiErrorResim");
				if (!PdksUtil.hasStringValue(msgFazlaMesaiError))
					msgFazlaMesaiError = "msgerror.png";
				List<Long> vgIdList = new ArrayList<Long>();
				ayrikHareketVar = false;
				String str = ortakIslemler.getParameterKey("addManuelGirisCikisHareketler");
				boolean ayrikKontrol = false;
				if (PdksUtil.hasStringValue(sicilNo)) {
					ayrikKontrol = str.equals("A") || str.equals("1");
					if (!ayrikKontrol) {
						if (authenticatedUser.isAdmin())
							ayrikKontrol = str.equalsIgnoreCase("I") || str.equalsIgnoreCase("S");
						else if (authenticatedUser.isIK())
							ayrikKontrol = str.equalsIgnoreCase("I");

					}
				}
				List<AylikPuantaj> puantajDenklestirmeList = new ArrayList<AylikPuantaj>();
				fazlaMesaiVar = false;
				saatlikMesaiVar = false;
				aylikMesaiVar = false;
				Date toDay = PdksUtil.getDate(new Date()), sonCalismaSaat = null;
				HashMap<String, String> vardiyaZamanMap = new HashMap<String, String>();
				boolean puantajFazlaMesaiHesapla = true;
				for (Iterator iterator1 = list.iterator(); iterator1.hasNext();) {
					PersonelDenklestirmeTasiyici denklestirmeTasiyici = (PersonelDenklestirmeTasiyici) iterator1.next();
					AylikPuantaj puantaj = (AylikPuantaj) aylikPuantajSablon.clone();
					PersonelDenklestirme valueBuAy = personelDenklestirmeMap.get(denklestirmeTasiyici.getPersonel().getId());
					puantaj.setPersonelDenklestirme(valueBuAy);
					CalismaModeli cm = puantaj.getCalismaModeli();
					if (!fazlaMesaiVar)
						fazlaMesaiVar = cm.isFazlaMesaiVarMi() && cm.isAylikOdeme();
					if (bordroPuantajEkranindaGoster) {
						if (!saatlikMesaiVar)
							saatlikMesaiVar = cm.isSaatlikOdeme();
						if (!aylikMesaiVar)
							aylikMesaiVar = cm.isAylikOdeme();
					}
					if (valueBuAy != null)
						puantaj.setPersonelDenklestirmeGecenAy(valueBuAy.getPersonelDenklestirmeGecenAy());
					if (puantaj.getPersonelDenklestirme() == null || !puantaj.getPersonelDenklestirme().isDenklestirmeDurum()) {
						iterator1.remove();
						continue;
					}
					puantaj.setPersonelDenklestirmeTasiyici(denklestirmeTasiyici);
					puantaj.setPdksPersonel(denklestirmeTasiyici.getPersonel());
					puantaj.setVardiyalar(denklestirmeTasiyici.getVardiyalar());
					TreeMap<String, VardiyaGun> vgMap = new TreeMap<String, VardiyaGun>();
					puantaj.setVgMap(vgMap);
					Date sonPersonelCikisZamani = null;
					PersonelDenklestirme personelDenklestirme = puantaj.getPersonelDenklestirme();
					Personel personel = personelDenklestirme.getPdksPersonel();
					if (puantaj.getVardiyalar() != null) {
						Double offSure = null;
						String donemStr = String.valueOf(yil * 100 + ay);
						int sayac = 0;
						List<VardiyaGun> vardiyaGunList = new ArrayList<VardiyaGun>();
						TreeMap<String, VardiyaGun> vardiyalar = new TreeMap<String, VardiyaGun>();
						for (Iterator iterator = puantaj.getVardiyalar().iterator(); iterator.hasNext();) {
							VardiyaGun vg = (VardiyaGun) iterator.next();
							VardiyaGun vardiyaGun = (VardiyaGun) vg.clone();
							// vardiyaGun.setDurum(Boolean.TRUE);
							vardiyaGunList.add(vardiyaGun);
							if (vardiyaGun.getDurum() == false && vardiyaGun.getVardiya() != null) {
								if (vardiyaGun.getVardiyaDate().after(toDay))
									vardiyaGun.setDurum(vardiyaGun.getVardiyaDate().after(toDay));
							}
							String key = vardiyaGun.getVardiyaDateStr();
							vardiyaGun.setAyinGunu(key.startsWith(donemStr));
							if (!vardiyaGun.isAyinGunu()) {
								iterator.remove();
								continue;
							}
							if (vardiyaGun.getId() != null) {
								++sayac;
								Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
								String keyStr = String.valueOf(islemVardiya.getId());
								Double netSure = islemVardiya.getNetCalismaSuresi();
								boolean calisma = islemVardiya != null && islemVardiya.isCalisma();
								if (calisma) {
									if (sonCalismaSaat == null || islemVardiya.getVardiyaTelorans2BitZaman().after(sonCalismaSaat))
										sonCalismaSaat = islemVardiya.getVardiyaTelorans2BitZaman();
								}
								if (calisma && !vardiyaZamanMap.containsKey(keyStr)) {
									vardiyaZamanMap.put(keyStr, islemVardiya.getAdi());
									String aciklama = authenticatedUser.timeFormatla(islemVardiya.getBasZaman()) + " : " + authenticatedUser.timeFormatla(islemVardiya.getBitZaman());
									vardiyaZamanMap.put("S" + keyStr, aciklama);
									vardiyaZamanMap.put("K" + keyStr, islemVardiya.getKisaAdi());
									vardiyaZamanMap.put("N" + keyStr, PdksUtil.numericValueFormatStr(netSure, null));
								}

								vgIdList.add(vardiyaGun.getId());
								vgMap.put(vardiyaGun.getVardiyaDateStr(), vardiyaGun);

							} else if (vardiyaGun.getPersonel() == null)
								vardiyaGun.setPersonel(personel);
							if (fazlaMesaiOnayla && vardiyaGun.getIslemVardiya() != null && vardiyaGun.getIslemVardiya().isCalisma()) {
								if (sonPersonelCikisZamani == null || vardiyaGun.getIslemVardiya().getVardiyaTelorans1BitZaman().after(sonPersonelCikisZamani))
									sonPersonelCikisZamani = vardiyaGun.getIslemVardiya().getVardiyaTelorans1BitZaman();
							}
							if (offSure != null && vardiyaGun.getVardiya() != null && vardiyaGun.getIzin() == null && vardiyaGun.getVardiya().isOffGun()) {
								cal.setTime(vardiyaGun.getVardiyaDate());
								int haftaGunu = cal.get(Calendar.DAY_OF_WEEK);
								if (haftaGunu != Calendar.SATURDAY && haftaGunu != Calendar.SUNDAY)
									offSure += 9;

							}

							if (vardiyaGun.getVardiya() != null && vardiyaGun.isZamanGelmedi()) {
								// hataYok = Boolean.FALSE;
								puantaj.setDonemBitti(Boolean.FALSE);
							}
							vardiyaGun.setLinkAdresler(null);
							vardiyaGun.setOnayli(Boolean.TRUE);
							vardiyaGun.setHataliDurum(Boolean.FALSE);
							vardiyaGun.setPersonel(puantaj.getPdksPersonel());
							vardiyaGun.setFiiliHesapla(Boolean.FALSE);
							boolean fazlaMesaiHesapla = personelDenklestirme.isOnaylandi();

							if (!puantajFazlaMesaiHesapla)
								puantajFazlaMesaiHesapla = fazlaMesaiHesapla;

							vardiyalar.put(vardiyaGun.getVardiyaKeyStr(), vardiyaGun);

							Vardiya vardiya = vardiyaGun.getIslemVardiya();
							String pattern = PdksUtil.getDateTimeFormat();
							if (vardiya != null)
								vardiyaGun.addLinkAdresler("Fazla Çalışma Saat : " + PdksUtil.convertToDateString(vardiya.getVardiyaFazlaMesaiBasZaman(), pattern) + " - " + PdksUtil.convertToDateString(vardiya.getVardiyaFazlaMesaiBitZaman(), pattern));

						}
						if (sayac == 0)
							continue;
						puantaj.setVardiyalar(vardiyaGunList);
						if (!vardiyalar.isEmpty())
							ortakIslemler.puantajHaftalikPlanOlustur(Boolean.FALSE, null, vardiyalar, aylikPuantajSablon, puantaj);

					}
					puantajDenklestirmeList.add(puantaj);
				}
				boolean planGetir = sonCalismaSaat == null || bugun.before(sonCalismaSaat);
				if (planGetir)
					calismaPlaniDenklestir(denklestirmeDonemi, puantajDenklestirmeList);

				if (!fazlaMesaiVar) {
					fazlaMesaiOde = false;
					fazlaMesaiIzinKullan = false;
				}

				ortakIslemler.yoneticiPuantajKontrol(authenticatedUser, puantajDenklestirmeList, Boolean.TRUE, session);

				aksamCalismaSaati = null;
				aksamCalismaSaatiYuzde = null;
				try {
					if (ortakIslemler.getParameterKeyHasStringValue("aksamCalismaSaatiYuzde"))
						aksamCalismaSaatiYuzde = Double.parseDouble(ortakIslemler.getParameterKey("aksamCalismaSaatiYuzde"));

				} catch (Exception e) {
				}
				if (aksamCalismaSaatiYuzde != null && (aksamCalismaSaatiYuzde.doubleValue() < 0.0d || aksamCalismaSaatiYuzde.doubleValue() > 100.0d))
					aksamCalismaSaatiYuzde = null;
				try {
					if (ortakIslemler.getParameterKeyHasStringValue("aksamCalismaSaati"))
						aksamCalismaSaati = Double.parseDouble(ortakIslemler.getParameterKey("aksamCalismaSaati"));

				} catch (Exception e) {
				}
				if (aksamCalismaSaati == null)
					aksamCalismaSaati = 4.0d;
				double fazlaMesaiMaxSure = ortakIslemler.getFazlaMesaiMaxSure(denklestirmeAy);

				for (Iterator iterator1 = puantajDenklestirmeList.iterator(); iterator1.hasNext();) {
					AylikPuantaj puantaj = (AylikPuantaj) iterator1.next();
					int yarimYuvarla = puantaj.getYarimYuvarla();
					TreeMap<String, VardiyaGun> vgMap = new TreeMap<String, VardiyaGun>();
					PersonelDenklestirme personelDenklestirme = puantaj.getPersonelDenklestirme();
					puantaj.setVgMap(vgMap);
					puantaj.setDonemBitti(Boolean.TRUE);
					puantaj.setAyrikHareketVar(false);
					puantaj.setFiiliHesapla(true);
					saveList.clear();
					Personel personel = puantaj.getPdksPersonel();
					perCalismaModeli = personel.getCalismaModeli();
					if (personelDenklestirme != null && personelDenklestirme.getCalismaModeliAy() != null)
						perCalismaModeli = personelDenklestirme.getCalismaModeli();
					Date sonPersonelCikisZamani = null;

					Boolean gebemi = Boolean.FALSE, calisiyor = Boolean.FALSE;
					puantaj.setKaydet(Boolean.FALSE);
					personelFazlaMesaiStr = personelFazlaMesaiOrjStr + (personel.getPdks() ? " " : "(Fazla Mesai Yok)");

					puantaj.setSablonAylikPuantaj(aylikPuantajSablon);

					puantaj.setTrClass(renk ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
					renk = !renk;
					Integer aksamVardiyaSayisi = 0;
					Double aksamVardiyaSaatSayisi = 0d, haftaCalismaSuresi = 0d, offSure = null;
					if (stajerSirket && denklestirmeAyDurum) {
						puantaj.planSureHesapla(tatilGunleriMap);
						offSure = 0.0D;
					}
					TreeMap<String, VardiyaGun> vardiyalar = new TreeMap<String, VardiyaGun>();
					cal = Calendar.getInstance();
					puantaj.setHareketler(null);
					List<String> ayrikList = new ArrayList<String>();

					boolean ayBitti = false;
					double puantajSaatToplami = 0.0d, puantajResmiTatil = 0.0d, puantajHaftaTatil = 0.0d, puantajUcretiOdenenSure = 0.0d;

					int sayac = 0;
					if (puantaj.getVardiyalar() != null) {
						String donemStr = String.valueOf(yil * 100 + ay);
						VardiyaGun vardiyaGunSon = null;
						List<VardiyaGun> vardiyaGunList = new ArrayList<VardiyaGun>();
						for (Iterator iterator = puantaj.getVardiyalar().iterator(); iterator.hasNext();) {
							VardiyaGun vg = (VardiyaGun) iterator.next();
							VardiyaGun vardiyaGun = (VardiyaGun) vg.clone();
							vardiyaGunList.add(vardiyaGun);
							if (vardiyaGun.getDurum() == false && vardiyaGun.getVardiya() != null) {
								if (vardiyaGun.getVardiyaDate().after(toDay))
									vardiyaGun.setDurum(vardiyaGun.getVardiyaDate().after(toDay));
							}
							String key = vardiyaGun.getVardiyaDateStr();
							vardiyaGun.setAyinGunu(key.startsWith(donemStr));
							if (!vardiyaGun.isAyinGunu()) {
								iterator.remove();
								continue;
							}
							if (vardiyaGun.getId() != null) {
								++sayac;
								Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
								boolean calisma = islemVardiya != null && islemVardiya.isCalisma();
								if (calisma)
									vardiyaGunSon = vardiyaGun;
								vgIdList.add(vardiyaGun.getId());
								vgMap.put(vardiyaGun.getVardiyaDateStr(), vardiyaGun);
								if (vardiyaGun.getPersonel().isCalisiyorGun(vardiyaGun.getVardiyaDate())) {
									try {
										boolean zamanGelmedi = !bugun.after(islemVardiya.getVardiyaTelorans2BitZaman());
										if (!zamanGelmedi)
											zamanGelmedi = islemVardiya.isCalisma() == false || vardiyaGun.isIzinli();

										vardiyaGun.setZamanGelmedi(zamanGelmedi);
									} catch (Exception e) {
									}
								}

							} else if (vardiyaGun.getPersonel() == null)
								vardiyaGun.setPersonel(personel);
							if (fazlaMesaiOnayla && vardiyaGun.getIslemVardiya() != null && vardiyaGun.getIslemVardiya().isCalisma()) {
								if (sonPersonelCikisZamani == null || vardiyaGun.getIslemVardiya().getVardiyaTelorans1BitZaman().after(sonPersonelCikisZamani))
									sonPersonelCikisZamani = vardiyaGun.getIslemVardiya().getVardiyaTelorans1BitZaman();
							}
							if (offSure != null && vardiyaGun.getVardiya() != null && vardiyaGun.getIzin() == null && vardiyaGun.getVardiya().isOffGun()) {
								cal.setTime(vardiyaGun.getVardiyaDate());
								int haftaGunu = cal.get(Calendar.DAY_OF_WEEK);
								if (haftaGunu != Calendar.SATURDAY && haftaGunu != Calendar.SUNDAY)
									offSure += 9;

							}

							if (vardiyaGun.getVardiya() != null && vardiyaGun.isZamanGelmedi())
								puantaj.setDonemBitti(Boolean.FALSE);

							vardiyaGun.setLinkAdresler(null);
							vardiyaGun.setOnayli(Boolean.TRUE);
							vardiyaGun.setHataliDurum(Boolean.FALSE);
							vardiyaGun.setPersonel(puantaj.getPdksPersonel());
							boolean fazlaMesaiHesapla = true;
							if (vardiyaGun.getIzin() == null)
								fazlaMesaiHesapla = vardiyaGun.getDurum() || vardiyaGun.isZamanGelmedi() || vardiyaGun.getVardiya() == null;
							if (!fazlaMesaiHesapla)
								logger.debug(vardiyaGun.getVardiyaDateStr());
							vardiyaGun.setFiiliHesapla(fazlaMesaiHesapla);
							if (puantajFazlaMesaiHesapla)
								puantajFazlaMesaiHesapla = fazlaMesaiHesapla;
							double toplamSure = 0.0d;
							if (vardiyaGun.getVardiyaSaatDB() != null) {
								vardiyaGun.setPlanHareketEkle(false);
								vardiyaGun.setHareketler(null);
								vardiyaGun.setGirisHareketleri(null);
								vardiyaGun.setCikisHareketleri(null);
								if (fazlaMesaiHesapla) {
									VardiyaSaat vardiyaSaatDB = vardiyaGun.getVardiyaSaatDB();
									if (vardiyaSaatDB.getResmiTatilSure() > 0.0d)
										vardiyaGun.setResmiTatilSure(vardiyaSaatDB.getResmiTatilSure());
									else if (personelDenklestirme.getHaftaCalismaSuresi() != null && vardiyaGun.getVardiya().isHaftaTatil() && personelDenklestirme.getHaftaCalismaSuresi() > 0.0d) {
										puantajHaftaTatil += vardiyaSaatDB.getCalismaSuresi();
										vardiyaGun.setHaftaCalismaSuresi(vardiyaSaatDB.getCalismaSuresi());
									}
									if (!vardiyaGun.getVardiya().isHaftaTatil()) {
										toplamSure = vardiyaSaatDB.getCalismaSuresi() - vardiyaSaatDB.getResmiTatilSure();
									}
									vardiyaGun.setCalismaSuresi(vardiyaSaatDB.getCalismaSuresi());

								}

							}
							if (vardiyaGun.getIzin() == null && vardiyaGun.isZamanGelmedi()) {
								toplamSure = vardiyaGun.getCalismaSuresi();
							}
							if (toplamSure > fazlaMesaiMaxSure)
								puantajUcretiOdenenSure += toplamSure - fazlaMesaiMaxSure;
 							puantajSaatToplami += toplamSure;
							vardiyalar.put(vardiyaGun.getVardiyaKeyStr(), vardiyaGun);

							Vardiya vardiya = vardiyaGun.getIslemVardiya();
							String pattern = PdksUtil.getDateTimeFormat();
							if (vardiya != null)
								vardiyaGun.addLinkAdresler("Fazla Çalışma Saat : " + PdksUtil.convertToDateString(vardiya.getVardiyaFazlaMesaiBasZaman(), pattern) + " - " + PdksUtil.convertToDateString(vardiya.getVardiyaFazlaMesaiBitZaman(), pattern));

							if (vardiyaGun.isZamanGelmedi() && vardiyaGun.getHareketler() != null) {
								for (Iterator iterator2 = vardiyaGun.getHareketler().iterator(); iterator2.hasNext();) {
									HareketKGS kgsHareket = (HareketKGS) iterator2.next();
									if (kgsHareket.isGecerliDegil())
										iterator2.remove();
								}
							}
						}
						puantaj.setVardiyalar(vardiyaGunList);
						if (vardiyaGunSon != null)
							ayBitti = bugun.after(vardiyaGunSon.getIslemVardiya().getVardiyaTelorans1BitZaman());

					}
					if (sayac == 0) {
						iterator1.remove();
						continue;
					}

					if (offSure != null)
						puantaj.setOffSure(offSure);

					if (!haftaTatilVar) {
						haftaTatilVar = puantaj.getHaftaCalismaSuresi() != 0.0d;
						if (haftaTatilVar)
							logger.debug(puantaj.getPdksPersonel().getPdksSicilNo());
					}

					if (!resmiTatilVar)
						resmiTatilVar = puantaj.getResmiTatilToplami() != 0.0d;
					ortakIslemler.puantajHaftalikPlanOlustur(Boolean.TRUE, null, vardiyalar, aylikPuantajSablon, puantaj);
					if (personelDenklestirme == null)
						continue;

					// puantaj.setSaatToplami(personelDenklestirme.getHesaplananSure());
					puantaj.setPlanlananSure(personelDenklestirme.getPlanlanSure());
					personelDenklestirme.setGuncellendi(Boolean.FALSE);
					PersonelDenklestirme hesaplananDenklestirmeHesaplanan = null;
					double ucretiOdenenMesaiSure = 0.0d;
					vgMap = puantaj.getVgMap();
					for (Iterator iterator = puantaj.getVardiyalar().iterator(); iterator.hasNext();) {
						VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
						if (!vardiyaGun.isAyinGunu()) {
							iterator.remove();
						} else {
							if (vardiyaGun.getVardiyaDateStr().endsWith("31"))
								logger.debug("");
							if (vardiyaGun.getDurum() == false && vardiyaGun.getVardiya() != null) {
								if (vardiyaGun.getVardiyaDate().after(toDay)) {
									VardiyaGun vg = (VardiyaGun) vardiyaGun.clone();
									vg.setDurum(vg.getVardiyaDate().after(toDay));
									vgMap.put(vg.getVardiyaDateStr(), vg);
								}

							}

							if (!calisiyor)
								calisiyor = vardiyaGun.getVardiya() != null;
							if (!gebemi && vardiyaGun.getVardiya() != null)
								gebemi = vardiyaGun.getVardiya().isGebelikMi();
							if (calisiyor) {
								Double sure = vardiyaGun.getCalismaSuresi();
								ucretiOdenenMesaiSure += sure != null && sure.doubleValue() > fazlaMesaiMaxSure ? sure.doubleValue() - fazlaMesaiMaxSure : 0.0d;
								if (vardiyaGun.getHaftaCalismaSuresi() > 0) {
									if (!haftaTatilVar)
										haftaTatilVar = Boolean.TRUE;
								}
							}

							if (vardiyaGun.getResmiTatilSure() > 0) {
								if (!resmiTatilVar)
									resmiTatilVar = Boolean.TRUE;
								puantajResmiTatil += vardiyaGun.getResmiTatilSure();
								// logger.info(vardiyaGun.getVardiyaKeyStr() + " " + resmiTatilToplami + " " + vardiyaGun.getResmiTatilSure());
							}
							if (vardiyaGun.getCalisilmayanAksamSure() > 0)
								aksamVardiyaSaatSayisi += vardiyaGun.getCalisilmayanAksamSure();
						}
					}
					puantaj.setUcretiOdenenMesaiSure(ucretiOdenenMesaiSure);
					double gecenAydevredenSure = 0;
					if (gecenAy == null && personelDenklestirme.getPersonelDenklestirmeGecenAy() != null && personelDenklestirme.getPersonelDenklestirmeGecenAy().getDenklestirmeAy() != null)
						gecenAy = personelDenklestirme.getPersonelDenklestirmeGecenAy().getDenklestirmeAy();
					try {

						if (personelDenklestirme.getPersonelDenklestirmeGecenAy() != null && personelDenklestirme.getPersonelDenklestirmeGecenAy().getDevredenSure() != null)
							gecenAydevredenSure = personelDenklestirme.getPersonelDenklestirmeGecenAy().getDevredenSure();
						if (ayBitti == false || personelDenklestirme.getDurum() == false) {
							puantaj.setUcretiOdenenMesaiSure(puantajUcretiOdenenSure);
							hesaplananDenklestirmeHesaplanan = puantaj.getPersonelDenklestirme(personelDenklestirme.getFazlaMesaiOde(), puantajSaatToplami - puantaj.getPlanlananSure(), gecenAydevredenSure);

						} else
							puantajSaatToplami = personelDenklestirme.getHesaplananSure();

					} catch (Exception e) {
						e.printStackTrace();
					}
					if (!fazlaMesaiIzinKullan)
						fazlaMesaiIzinKullan = personelDenklestirme.getFazlaMesaiIzinKullan() != null && personelDenklestirme.getFazlaMesaiIzinKullan();
					if (!fazlaMesaiOde)
						fazlaMesaiOde = personelDenklestirme.getFazlaMesaiOde() != null && personelDenklestirme.getFazlaMesaiOde();
					if (!kimlikGoster)
						kimlikGoster = PdksUtil.hasStringValue(personel.getPersonelKGS().getKimlikNo());
					if (!yasalFazlaCalismaAsanSaat && personelDenklestirme.getCalismaModeliAy().isGunMaxCalismaOdenir())
						yasalFazlaCalismaAsanSaat = perCalismaModeli.isFazlaMesaiVarMi() && ucretiOdenenMesaiSure > 0.0d;

					if (!hataliPuantajVar)
						hataliPuantajVar = personelDenklestirme.getDurum().booleanValue() == false;

					if (!sutIzniGoster)
						sutIzniGoster = personelDenklestirme != null && personelDenklestirme.getSutIzniDurum() != null && personelDenklestirme.getSutIzniDurum();
					if (!partTimeGoster)
						partTimeGoster = personelDenklestirme != null && personelDenklestirme.getPartTime() != null && personelDenklestirme.getPartTime();
					// if (/*personelDenklestirme.isErpAktarildi() ||*/ !personelDenklestirme.getDenklestirmeAy().isDurumu()) {
					puantaj.setDevredenSure(gecenAydevredenSure);
					if (ayBitti || !denklestirmeAyDurum) {
						puantaj.setFazlaMesaiSure(personelDenklestirme.getOdenecekSure());
						puantaj.setResmiTatilToplami(personelDenklestirme.getResmiTatilSure());
						puantaj.setHaftaCalismaSuresi(personelDenklestirme.getHaftaCalismaSuresi());
						puantaj.setDevredenSure(personelDenklestirme.getDevredenSure());
						puantaj.setEksikCalismaSure(personelDenklestirme.getEksikCalismaSure());
						puantaj.setOdenenSure(personelDenklestirme.getOdenecekSure());
						puantaj.setSaatToplami(personelDenklestirme.getHesaplananSure());
						puantajFazlaMesaiHesapla = personelDenklestirme.getDurum();
					} else if (hesaplananDenklestirmeHesaplanan != null) {
						puantaj.setSaatToplami(puantajSaatToplami);
						puantaj.setDevredenSure(hesaplananDenklestirmeHesaplanan.getDevredenSure());
						if (!puantaj.isFazlaMesaiHesapla()) {
							puantajHaftaTatil = 0.0d;
							puantajResmiTatil = 0.0d;
						} else
							puantaj.setOdenenSure(hesaplananDenklestirmeHesaplanan.getOdenecekSure());
						puantaj.setEksikCalismaSure(hesaplananDenklestirmeHesaplanan.getEksikCalismaSure());
						puantaj.setHaftaCalismaSuresi(puantajHaftaTatil);
						puantaj.setResmiTatilToplami(PdksUtil.setSureDoubleTypeRounded(puantajResmiTatil, yarimYuvarla));
					}
					puantaj.setFazlaMesaiHesapla(puantajFazlaMesaiHesapla);
					if (!personelDenklestirme.getDenklestirmeAy().isDurumu()) {
						aksamVardiyaSayisi = personelDenklestirme.getAksamVardiyaSayisi().intValue();
						aksamVardiyaSaatSayisi = personelDenklestirme.getAksamVardiyaSaatSayisi();
						haftaCalismaSuresi = personelDenklestirme.getHaftaCalismaSuresi();
					}

					puantajList.add(puantaj);

					if (!denklestirmeAyDurum) {
						if (!(authenticatedUser.isAdmin() || authenticatedUser.isIK()))
							puantajResmiTatil = personelDenklestirme.getResmiTatilSure();
						else
							personelDenklestirme.setResmiTatilSure(puantajResmiTatil);
						aksamVardiyaSaatSayisi = personelDenklestirme.getAksamVardiyaSaatSayisi();
						aksamVardiyaSayisi = personelDenklestirme.getAksamVardiyaSayisi().intValue();
						haftaCalismaSuresi = personelDenklestirme.getHaftaCalismaSuresi();
					}

					if (personelDenklestirme.isGuncellendi()) {
						if ((bakiyeGuncelle != null && bakiyeGuncelle) || puantaj.isFazlaMesaiHesapla() != personelDenklestirme.getDurum() || (gecenAy != null && gecenAy.getDurum().equals(Boolean.FALSE))) {
							if (puantaj.isFazlaMesaiHesapla() != personelDenklestirme.getDurum())
								personelDenklestirme.setDurum(puantaj.isFazlaMesaiHesapla());

						}
					}
					if (!fazlaMesaiMap.containsKey(AylikPuantaj.MESAI_TIPI_AKSAM_SAAT)) {
						aksamVardiyaSaatSayisi = 0.0d;
					}
					if (!fazlaMesaiMap.containsKey(AylikPuantaj.MESAI_TIPI_AKSAM_ADET)) {
						aksamVardiyaSayisi = 0;
					}
					puantaj.setAksamVardiyaSaatSayisi(aksamVardiyaSaatSayisi);
					puantaj.setAksamVardiyaSayisi(aksamVardiyaSayisi);
					puantaj.setHaftaCalismaSuresi(haftaCalismaSuresi);

					if (!maasKesintiGoster)
						maasKesintiGoster = puantaj.getEksikCalismaSure() != 0;
					if (!aksamGun)
						aksamGun = puantaj.getAksamVardiyaSayisi() != 0;
					if (!aksamSaat)
						aksamSaat = puantaj.getAksamVardiyaSaatSayisi() != 0.0d;
					if (!haftaTatilVar) {
						haftaTatilVar = puantaj.getHaftaCalismaSuresi() != 0.0d;
						if (haftaTatilVar)
							logger.debug(puantaj.getPdksPersonel().getPdksSicilNo());
					}

					if (!resmiTatilVar)
						resmiTatilVar = puantaj.getResmiTatilToplami() != 0.0d;
					if (gebemi)
						iterator1.remove();
					puantaj.setDonemBitti(Boolean.FALSE);
					if (sonPersonelCikisZamani != null) {
						if (puantaj.isFazlaMesaiHesapla() && personelDenklestirme.getDurum()) {
							puantaj.setDonemBitti(bugun.after(sonPersonelCikisZamani));
							if (puantaj.isDonemBitti() && (sonCikisZamani == null || sonPersonelCikisZamani.after(sonCikisZamani)))
								sonCikisZamani = sonPersonelCikisZamani;
						}
					} else
						puantaj.setDonemBitti(personel.getIstenAyrilisTarihi().before(puantaj.getSonGun()) || puantaj.getSonGun().before(bugun));

					if (denklestirmeAyDurum && puantaj.isFazlaMesaiHesapla() == false) {
						if (ayrikList.size() > 1) {
							if (!ayrikHareketVar)
								ayrikHareketVar = ayrikKontrol;
							if (!PdksUtil.getTestDurum()) {
								StringBuffer sb = new StringBuffer(personel.getPdksSicilNo() + " " + personel.getAdSoyad() + " ");
								for (Iterator iterator = ayrikList.iterator(); iterator.hasNext();) {
									String string = (String) iterator.next();
									sb.append(string);
									if (iterator.hasNext()) {
										if (ayrikList.size() > 2)
											sb.append(", ");
										else
											sb.append(" ve ");
									}
								}

								PdksUtil.addMessageAvailableWarn(sb.toString() + (ayrikList.size() == 2 ? " arası" : "") + " giriş ve çıkış kayıtı vardır! ");
							}
						}
					} else
						puantaj.setAyrikHareketVar(false);
				}
				if (bordroPuantajEkranindaGoster) {
					fazlaMesaiOrtakIslemler.setAylikPuantajBordroVeri(puantajDenklestirmeList, session);
				}
				Date basTarih = PdksUtil.convertToJavaDate(String.valueOf(yil * 100 + ay) + "01", "yyyyMMdd");
				Date bitTarih = PdksUtil.tariheAyEkleCikar(basTarih, 1);
				TreeMap<String, Tatil> tatilMap = ortakIslemler.getTatilGunleri(null, PdksUtil.tariheGunEkleCikar(basTarih, -1), bitTarih, session);
				bordroVeriOlusturBasla(puantajDenklestirmeList, tatilMap);
				if (!(authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi()) && yasalFazlaCalismaAsanSaat)
					yasalFazlaCalismaAsanSaat = ortakIslemler.getParameterKey("yasalFazlaCalismaAsanSaat").equals("1");

				if (!puantajList.isEmpty() && seciliEkSaha3Id == null)
					fazlaMesaiOrtakIslemler.sortAylikPuantajPersonelBolum(puantajList);

				modelGoster = ortakIslemler.getModelGoster(denklestirmeAy, session);
			} else {
				if (fazlaMesaiMap == null)
					fazlaMesaiMap = new TreeMap<String, Tanim>();
				else
					fazlaMesaiMap.clear();
			}
			if (hataYok) {
				hataYok = sonCikisZamani != null && bugun.after(sonCikisZamani);
			}

		} catch (Exception ex) {
			ortakIslemler.loggerErrorYaz(sayfaURL, ex);
			throw new Exception(ex);

		} finally {

		}
		denklestirmeDinamikAlanlar = ortakIslemler.setDenklestirmeDinamikDurum(puantajList, session);
		ortakIslemler.sortAylikPuantajList(puantajList, true);
		setAylikPuantajList(puantajList);
		if (gecenAy != null && gecenAy.getDurum().equals(Boolean.TRUE) && (authenticatedUser.isAdmin() || authenticatedUser.isIK())) {
			hataYok = false;
			PdksUtil.addMessageAvailableError(gecenAy.getAyAdi() + " " + gecenAy.getYil() + " dönemi açıktır!");
		} else if (kullaniciPersonel.equals(Boolean.FALSE) && authenticatedUser.isIK() && denklestirmeAyDurum && denklestirmeAy.getOtomatikOnayIKTarih() != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(PdksUtil.getDate(cal.getTime()));
			cal.set(Calendar.YEAR, denklestirmeAy.getYil());
			cal.set(Calendar.MONTH, denklestirmeAy.getAy() - 1);
			cal.add(Calendar.MONTH, 1);
			cal.set(Calendar.DATE, 1);
			Date tarih = PdksUtil.getDate(cal.getTime());
			Date tarihLast = ortakIslemler.tariheGunEkleCikar(cal, denklestirmeAy.getOtomatikOnayIKTarih(), 10);
			cal = Calendar.getInstance();
			Date toDay = cal.getTime();
			if (toDay.after(tarih) && (toDay.before(denklestirmeAy.getOtomatikOnayIKTarih())) || (authenticatedUser.isTestLogin() && toDay.before(tarihLast))) {
				onayla = Boolean.FALSE;
				for (AylikPuantaj puantaj : puantajList) {
					puantaj.setKaydet(puantaj.getPersonelDenklestirme().getDurum());
					if (puantaj.isKaydet())
						onayla = hataYok;
				}

			}
		}

	}

	/**
	 * @param puantajList
	 * @param tatilMap
	 */
	private void bordroVeriOlusturBasla(List<AylikPuantaj> puantajList, TreeMap<String, Tatil> tatilMap) {
		baslikMap.clear();
		boolean saatlikCalismaVar = ortakIslemler.getParameterKey("saatlikCalismaVar").equals("1");
		for (AylikPuantaj ap : puantajList) {
			PersonelDenklestirme personelDenklestirme = ap.getPersonelDenklestirme();
			ap.setPlanlananSure(personelDenklestirme.getPlanlanSure());
			double izinGunAdet = 0.0;
			if (ap.getVardiyalar() != null) {
				for (VardiyaGun vg : ap.getVardiyalar()) {
					if (vg.isAyinGunu() && vg.getIzin() != null && vg.getTatil() == null) {
						IzinTipi izinTipi = vg.getIzin().getIzinTipi();
						if (izinTipi.isUcretliIzinTipi() && !tatilMap.containsKey(vg.getVardiyaDateStr()))
							izinGunAdet += 1.0d;
					}

				}
			}

			CalismaModeli calismaModeli = ap.getCalismaModeli();
			if (calismaModeli.isFazlaMesaiVarMi()) {
				if (ap.getGecenAyFazlaMesai(authenticatedUser) != 0)
					baslikMap.put(ortakIslemler.devredenMesaiKod(), Boolean.TRUE);
				if (ap.getFazlaMesaiSure() > 0)
					baslikMap.put(ortakIslemler.ucretiOdenenKod(), Boolean.TRUE);
				if (ap.getDevredenSure() != 0)
					baslikMap.put(ortakIslemler.devredenBakiyeKod(), Boolean.TRUE);
				if (ap.getAylikNetFazlaMesai() != 0)
					baslikMap.put(ortakIslemler.gerceklesenMesaiKod(), Boolean.TRUE);
			}
			PersonelDenklestirmeBordro denklestirmeBordro = ap.getDenklestirmeBordro();
			boolean saatlikCalisma = calismaModeli.isSaatlikOdeme();

			if (denklestirmeBordro != null) {
				if (saatlikCalismaVar) {
					String keyEk = saatlikCalisma ? "" : "G";
					fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.normalCalismaSaatKod() + keyEk, denklestirmeBordro.getSaatNormal());
					fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.haftaTatilCalismaSaatKod() + keyEk, denklestirmeBordro.getSaatHaftaTatil());
					fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.resmiTatilCalismaSaatKod() + keyEk, denklestirmeBordro.getSaatResmiTatil());
					fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.izinSureSaatKod() + keyEk, denklestirmeBordro.getSaatIzin());
					fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.izinSureGunAdetKod(), izinGunAdet);
				}
				fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.ucretliIzinGunKod(), denklestirmeBordro.getUcretliIzin().doubleValue());
				fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.ucretsizIzinGunKod(), denklestirmeBordro.getUcretsizIzin().doubleValue());
				fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.hastalikIzinGunKod(), denklestirmeBordro.getRaporluIzin().doubleValue());
				fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.normalGunKod(), denklestirmeBordro.getNormalGunAdet());
				fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.haftaTatilGunKod(), denklestirmeBordro.getHaftaTatilAdet());
				fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.artikGunKod(), denklestirmeBordro.getArtikAdet());
				fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.resmiTatilGunKod(), denklestirmeBordro.getResmiTatilAdet());
				fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.bordroToplamGunKod(), denklestirmeBordro.getBordroToplamGunAdet());
			}

		}

		gerceklesenMesaiKod = saatlikCalismaVar == false || bordroPuantajEkranindaGoster == false || baslikMap.containsKey(ortakIslemler.gerceklesenMesaiKod());
		devredenBakiyeKod = saatlikCalismaVar == false || bordroPuantajEkranindaGoster == false || baslikMap.containsKey(ortakIslemler.devredenBakiyeKod());
		devredenMesaiKod = saatlikCalismaVar == false || bordroPuantajEkranindaGoster == false || baslikMap.containsKey(ortakIslemler.devredenMesaiKod());
		ucretiOdenenKod = saatlikCalismaVar == false || bordroPuantajEkranindaGoster == false || baslikMap.containsKey(ortakIslemler.ucretiOdenenKod());
		normalCalismaSaatKod = (saatlikCalismaVar && bordroPuantajEkranindaGoster) || baslikMap.containsKey(ortakIslemler.normalCalismaSaatKod());
		haftaTatilCalismaSaatKod = (saatlikCalismaVar && bordroPuantajEkranindaGoster) || (baslikMap.containsKey(ortakIslemler.haftaTatilCalismaSaatKod()));
		resmiTatilCalismaSaatKod = (saatlikCalismaVar && bordroPuantajEkranindaGoster) || (baslikMap.containsKey(ortakIslemler.resmiTatilCalismaSaatKod()));
		izinSureSaatKod = (saatlikCalismaVar && bordroPuantajEkranindaGoster) || (baslikMap.containsKey(ortakIslemler.izinSureSaatKod()));
		normalCalismaGunKod = bordroPuantajEkranindaGoster && baslikMap.containsKey(ortakIslemler.normalCalismaGunKod());
		haftaTatilCalismaGunKod = bordroPuantajEkranindaGoster && baslikMap.containsKey(ortakIslemler.haftaTatilCalismaGunKod());
		resmiTatilCalismaGunKod = bordroPuantajEkranindaGoster && baslikMap.containsKey(ortakIslemler.resmiTatilCalismaGunKod());
		izinSureGunKod = bordroPuantajEkranindaGoster && baslikMap.containsKey(ortakIslemler.izinSureGunKod());
		ucretliIzinGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.ucretliIzinGunKod());
		ucretsizIzinGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.ucretsizIzinGunKod());
		hastalikIzinGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.hastalikIzinGunKod());
		normalGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.normalGunKod());
		haftaTatilGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.haftaTatilGunKod());
		resmiTatilGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.resmiTatilGunKod());
		artikGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.artikGunKod());
		bordroToplamGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.bordroToplamGunKod());
		if (!devredenMesaiKod || !devredenBakiyeKod) {
			for (AylikPuantaj ap : puantajList) {
				if (ap.getCalismaModeli().isSaatlikOdeme()) {
					double gecenAyFazlaMesai = ap.getGecenAyFazlaMesai(authenticatedUser);
					double devredenSure = ap.getDevredenSure();
					if (!devredenMesaiKod)
						devredenMesaiKod = gecenAyFazlaMesai != 0.0d;
					if (!!devredenBakiyeKod)
						devredenBakiyeKod = devredenSure != 0.0d;
				}
			}
		}
	}

	/**
	 * @param idList
	 * @return
	 */
	private List<PersonelDenklestirme> getPdksPersonelDenklestirmeler(List<Long> idList) {
		String fieldName = "p";
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT S.* from " + PersonelDenklestirme.TABLE_NAME + " S WITH(nolock) ");
		// sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + " = S." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
		// sb.append(" AND P." + Personel.getIseGirisTarihiColumn() + " IS NOT NULL AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " IS NOT NULL ");
		sb.append(" WHERE S." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = " + denklestirmeAy.getId() + " AND S." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " :" + fieldName);
		fields.put(fieldName, idList);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		// List<PersonelDenklestirme> list = pdksEntityController.getObjectBySQLList(sb, fields, PersonelDenklestirme.class);
		List<PersonelDenklestirme> list = ortakIslemler.getSQLParamList(idList, sb, fieldName, fields, PersonelDenklestirme.class, session);
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			PersonelDenklestirme personelDenklestirme = (PersonelDenklestirme) iterator.next();
			if (!personelDenklestirme.isDenklestirmeDurum())
				iterator.remove();
		}
		fields = null;
		sb = null;
		return list;
	}

	public String fazlaMesaiExcel() {
		try {
			for (Iterator iter = aylikPuantajList.iterator(); iter.hasNext();) {
				AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();
				aylikPuantaj.setSecili(Boolean.TRUE);
			}
			ByteArrayOutputStream baosDosya = fazlaMesaiExcelDevam(aylikPuantajList);
			if (baosDosya != null) {
				String dosyaAdi = "FazlaMesai" + (sirket != null ? "_" + (sirket.getSirketGrup() == null ? sirket.getAd() : sirket.getSirketGrup().getAciklama()) : "");
				dosyaAdi += (tesisId != null ? "_" + ortakIslemler.getSelectItemText(tesisId, tesisList) : "") + (seciliEkSaha3Id != null ? "_" + ortakIslemler.getSelectItemText(seciliEkSaha3Id, gorevYeriList) : "");
				dosyaAdi += "_" + PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyyMM") + ".xlsx";
				PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi);
			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return "";
	}

	private void fillEkSahaTanim() {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
	}

	/**
	 * @param list
	 * @return
	 */
	private ByteArrayOutputStream fazlaMesaiExcelDevam(List<AylikPuantaj> list) {
		TreeMap<String, String> sirketMap = new TreeMap<String, String>();
		sirket = null;
		tesis = null;
		boolean kismiOdemeGoster = false;
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();
			PersonelDenklestirme pd = aylikPuantaj.getPersonelDenklestirme();
			if (!kismiOdemeGoster)
				kismiOdemeGoster = pd.getKismiOdemeSure() != null && pd.getKismiOdemeSure().doubleValue() != 0.0d;
			Personel personel = aylikPuantaj.getPdksPersonel();
			if (personel.getSirket() != null) {
				sirket = personel.getSirket();
				if (sirket.isTesisDurumu() && tesis == null && tesisId != null && personel.getTesis() != null)
					tesis = personel.getTesis();
			}

			String tekSirketTesis = (personel.getSirket() != null ? personel.getSirket().getId() : "") + "_" + (personel.getTesis() != null ? personel.getTesis().getId() : "");
			String tekSirketTesisAdi = (personel.getSirket() != null ? personel.getSirket().getAd() : "") + " " + (personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
			sirketMap.put(tekSirketTesis, tekSirketTesisAdi);
		}

		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "MMMMM yyyy") + " Fazla Mesai", Boolean.TRUE);
		CreationHelper helper = wb.getCreationHelper();
		ClientAnchor anchor = helper.createClientAnchor();
		CellStyle izinBaslik = ExcelUtil.getStyleHeader(wb);
		CellStyle styleTutarEven = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleRedTutarEven = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TUTAR, wb);
		ExcelUtil.setFontColor(styleRedTutarEven, Color.RED);
		CellStyle styleTutarOdd = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleRedTutarOdd = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TUTAR, wb);
		ExcelUtil.setFontColor(styleRedTutarOdd, Color.RED);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleRedOdd = ExcelUtil.getStyleOdd(null, wb);
		ExcelUtil.setFontColor(styleRedOdd, Color.RED);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleRedEven = ExcelUtil.getStyleEven(null, wb);
		ExcelUtil.setFontColor(styleRedEven, Color.RED);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleRedOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		ExcelUtil.setFontColor(styleRedOddCenter, Color.RED);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleRedEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		ExcelUtil.setFontColor(styleRedEvenCenter, Color.RED);
		CellStyle styleTutarOddRed = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TUTAR, wb);
		ExcelUtil.setFontColor(styleTutarOddRed, Color.RED);
		CellStyle styleTutarEvenRed = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TUTAR, wb);
		ExcelUtil.setFontColor(styleTutarEvenRed, Color.RED);
		CellStyle styleCenterEvenDay = ExcelUtil.getStyleDayEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleRedCenterEvenDay = ExcelUtil.getStyleDayEven(ExcelUtil.ALIGN_CENTER, wb);
		ExcelUtil.setFontColor(styleRedCenterEvenDay, Color.RED);
		CellStyle styleCenterOddDay = ExcelUtil.getStyleDayOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleRedCenterOddDay = ExcelUtil.getStyleDayOdd(ExcelUtil.ALIGN_CENTER, wb);
		ExcelUtil.setFontColor(styleRedCenterOddDay, Color.RED);

		CellStyle styleDay = null, styleGenel = null, styleTutar = null, styleStrDay = null;
		CellStyle styleCenter = ExcelUtil.getStyleData(wb);
		CellStyle styleTatil = ExcelUtil.getStyleDataCenter(wb);
		CellStyle styleTatilRed = ExcelUtil.getStyleDataCenter(wb);
		ExcelUtil.setFontColor(styleTatilRed, Color.RED);
		CellStyle styleCenterOddDayRed = ExcelUtil.getStyleDayOdd(ExcelUtil.ALIGN_CENTER, wb);
		ExcelUtil.setFontColor(styleCenterOddDayRed, Color.RED);
		CellStyle styleCenterEvenDayRed = ExcelUtil.getStyleDayEven(ExcelUtil.ALIGN_CENTER, wb);
		ExcelUtil.setFontColor(styleCenterEvenDayRed, Color.RED);
		CellStyle styleIstek = ExcelUtil.getStyleDataCenter(wb);
		CellStyle styleEgitim = ExcelUtil.getStyleDataCenter(wb);
		CellStyle styleOff = ExcelUtil.getStyleDataCenter(wb);
		ExcelUtil.setFontColor(styleOff, Color.WHITE);
		CellStyle styleOffRed = ExcelUtil.getStyleDataCenter(wb);
		ExcelUtil.setFontColor(styleTatilRed, Color.RED);
		ExcelUtil.setFillForegroundColor(izinBaslik, 146, 208, 80);

		CellStyle styleIzin = ExcelUtil.getStyleDataCenter(wb);
		ExcelUtil.setFillForegroundColor(styleIzin, 146, 208, 80);

		CellStyle styleCalisma = ExcelUtil.getStyleDataCenter(wb);
		XSSFCellStyle header = (XSSFCellStyle) ExcelUtil.getStyleHeader(9, wb);

		ExcelUtil.setFillForegroundColor(styleTatil, 255, 153, 204);

		ExcelUtil.setFillForegroundColor(styleIstek, 255, 255, 0);

		ExcelUtil.setFillForegroundColor(styleCalisma, 255, 255, 255);

		ExcelUtil.setFillForegroundColor(styleEgitim, 0, 0, 255);

		ExcelUtil.setFillForegroundColor(styleOff, 13, 12, 89);
		ExcelUtil.setFontColor(styleOff, 256, 256, 256);
		int row = 0, col = 0;

		if (sirket != null) {
			String baslik = sirket.getSirketGrup() == null ? sirket.getAd() : sirket.getSirketGrup().getAciklama();
			baslik += (tesisId != null ? " " + ortakIslemler.getSelectItemText(tesisId, tesisList) : "") + (seciliEkSaha3Id != null ? " " + ortakIslemler.getSelectItemText(seciliEkSaha3Id, gorevYeriList) : "");

			ExcelUtil.getCell(sheet, row, col, header).setCellValue(baslik);
			for (int i = 0; i < 3; i++)
				ExcelUtil.getCell(sheet, row, col + i + 1, header).setCellValue("");

			try {
				sheet.addMergedRegion(ExcelUtil.getRegion((int) row, (int) 0, (int) row, (int) 4));
			} catch (Exception e) {
				e.printStackTrace();
			}
			col = 0;
			++row;
		}
		String aciklamaExcel = PdksUtil.replaceAll(PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyy MMMMMM  "), "_", "");
		ExcelUtil.getCell(sheet, row, col, header).setCellValue(aciklamaExcel);
		for (int i = 0; i < 3; i++)
			ExcelUtil.getCell(sheet, row, col + i + 1, header).setCellValue("");

		try {
			sheet.addMergedRegion(ExcelUtil.getRegion((int) row, (int) 0, (int) row, (int) 4));
		} catch (Exception e) {
			e.printStackTrace();
		}
		col = 0;
		ExcelUtil.getCell(sheet, ++row, col, styleGenel).setCellValue("");
		col = 0;
		ExcelUtil.getCell(sheet, ++row, col++, header).setCellValue("Sıra");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		if (kimlikGoster)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.kimlikNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		if (seciliEkSaha3Id == null)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.yoneticiAciklama());
		if (sirketGoster)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		if (fazlaMesaiOde)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("FM Ödeme");
		if (fazlaMesaiIzinKullan)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("FM İzin Kullansın");
		if (modelGoster)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.calismaModeliAciklama());
		Calendar cal = Calendar.getInstance();
		cal.setTime(aylikPuantajDefault.getIlkGun());
		CellStyle headerVardiyaGun = ExcelUtil.getStyleHeader(9, wb);
		ExcelUtil.setFillForegroundColor(headerVardiyaGun, 99, 182, 153);
		CellStyle headerVardiyaTatilYarimGun = ExcelUtil.getStyleHeader(9, wb);
		ExcelUtil.setFontColor(headerVardiyaTatilYarimGun, 255, 255, 0);
		ExcelUtil.setFillForegroundColor(headerVardiyaTatilYarimGun, 144, 185, 63);

		CellStyle headerVardiyaTatilGun = ExcelUtil.getStyleHeader(9, wb);
		ExcelUtil.setFillForegroundColor(headerVardiyaTatilGun, 92, 127, 45);
		ExcelUtil.setFontColor(headerVardiyaTatilGun, 255, 255, 0);
		Drawing drawing = sheet.createDrawingPatriarch();
		for (Iterator iterator = aylikPuantajDefault.getVardiyalar().iterator(); iterator.hasNext();) {
			VardiyaGun vg = (VardiyaGun) iterator.next();
			if (!vg.isAyinGunu())
				continue;
			cal.setTime(vg.getVardiyaDate());
			CellStyle headerVardiya = headerVardiyaGun;
			String title = null;
			if (vg.getTatil() != null) {
				Tatil tatil = vg.getTatil();
				title = tatil.getAd();
				headerVardiya = tatil.isYarimGunMu() ? headerVardiyaTatilYarimGun : headerVardiyaTatilGun;
			}
			Cell cell = ExcelUtil.getCell(sheet, row, col++, headerVardiya);
			ExcelUtil.baslikCell(cell, anchor, helper, drawing, cal.get(Calendar.DAY_OF_MONTH) + "\n " + authenticatedUser.getTarihFormatla(cal.getTime(), "EEE"), title);
		}

		Cell cell = ExcelUtil.getCell(sheet, row, col++, header);
		ExcelUtil.baslikCell(cell, anchor, helper, drawing, "TÇS", "Toplam Çalışma Saati: Çalışanın bu listedeki toplam çalışma saati");
		cell = ExcelUtil.getCell(sheet, row, col++, header);
		ExcelUtil.baslikCell(cell, anchor, helper, drawing, "ÇGS", "Çalışılması Gereken Saat: Çalışanın bu listede çalışması gereken saat");
		if (yasalFazlaCalismaAsanSaat) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.yasalFazlaCalismaAsanSaatKod(), "Yasal Çalışmayı Aşan Mesai : " + authenticatedUser.sayiFormatliGoster(denklestirmeAy.getFazlaMesaiMaxSure()) + " saati aşan çalışma toplam miktarı");
		}

		if (gerceklesenMesaiKod) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			ExcelUtil.baslikCell(cell, anchor, helper, drawing, "GM", "Gerçekleşen Mesai : Çalışanın bu listedeki eksi/fazla çalışma saati");
		}

		if (devredenMesaiKod) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.devredenMesaiKod(), "Devreden Mesai: Çalisanin önceki listelerden devreden eksi/fazla mesaisi");

		}
		if (fazlaMesaiVar) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			ExcelUtil.baslikCell(cell, anchor, helper, drawing, "ÜÖM", "Çalışanın bu listenin sonunda ücret olarak ödediğimiz fazla mesai saati");
		}
		if (kismiOdemeGoster) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			ExcelUtil.baslikCell(cell, anchor, helper, drawing, "KÖM", "Çalışanın bu listenin sonunda ücret olarak kısmi ödediğimiz fazla mesai saati ");
		}

		if (resmiTatilVar || bordroPuantajEkranindaGoster) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			ExcelUtil.baslikCell(cell, anchor, helper, drawing, "RÖM", "Çalışanın bu listenin sonunda ücret olarak ödediğimiz resmi tatil mesai saati");
		}
		if (haftaTatilVar) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			ExcelUtil.baslikCell(cell, anchor, helper, drawing, AylikPuantaj.MESAI_TIPI_HAFTA_TATIL, "Çalışanın bu listenin sonunda ücret olarak ödediğimiz hafta tatil mesai saati");
		}
		if (devredenBakiyeKod) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.devredenBakiyeKod(), "Bakiye: Çalışanın bu liste de dahil bugüne kadarki devreden eksi/fazla mesaisi");
		}

		if (aksamGun) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			ExcelUtil.baslikCell(cell, anchor, helper, drawing, AylikPuantaj.MESAI_TIPI_AKSAM_ADET, "Çalışanın bu listenin sonunda ücret olarak ödediğimiz gece mesai gün");
		}
		if (aksamSaat) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			ExcelUtil.baslikCell(cell, anchor, helper, drawing, AylikPuantaj.MESAI_TIPI_AKSAM_SAAT, "Çalışanın bu listenin sonunda ücret olarak ödediğimiz gece mesai saati");
		}
		if (denklestirmeDinamikAlanlar != null && !denklestirmeDinamikAlanlar.isEmpty()) {
			for (Tanim alan : denklestirmeDinamikAlanlar) {
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(alan.getAciklama());

			}
		}
		CellStyle headerIzinTipi = (CellStyle) header.clone();
		ExcelUtil.setFillForegroundColor(headerIzinTipi, 255, 153, 204);
		if (bordroPuantajEkranindaGoster) {
			XSSFCellStyle headerSiyah = (XSSFCellStyle) ExcelUtil.getStyleHeader(wb);
			headerSiyah.getFont().setColor(ExcelUtil.getXSSFColor(255, 255, 255));
			XSSFCellStyle headerSaat = (XSSFCellStyle) headerSiyah.clone();
			XSSFCellStyle headerIzin = (XSSFCellStyle) headerSiyah.clone();
			XSSFCellStyle headerBGun = (XSSFCellStyle) headerSiyah.clone();
			XSSFCellStyle headerBTGun = (XSSFCellStyle) (XSSFCellStyle) headerSiyah.clone();
			ExcelUtil.setFillForegroundColor(headerSaat, 146, 208, 62);
			ExcelUtil.setFillForegroundColor(headerIzin, 255, 255, 255);
			ExcelUtil.setFillForegroundColor(headerBGun, 255, 255, 0);
			ExcelUtil.setFillForegroundColor(headerBTGun, 236, 125, 125);

			if (normalCalismaSaatKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerSaat);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.normalCalismaSaatKod(), "N.Çalışma Saat");
			}
			if (haftaTatilCalismaSaatKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerSaat);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.haftaTatilCalismaSaatKod(), "H.Tatil Saat");
			}
			if (resmiTatilCalismaSaatKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerSaat);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.resmiTatilCalismaSaatKod(), "R.Tatil Saat");
			}
			if (izinSureSaatKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerSaat);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.izinSureSaatKod(), "İzin Saat");
			}
			if (normalCalismaGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.normalCalismaGunKod(), "N.Çalışma Gün");
			}
			if (haftaTatilCalismaGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.haftaTatilCalismaGunKod(), "H.Tatil Gün");
			}
			if (resmiTatilCalismaGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.resmiTatilCalismaGunKod(), "R.Tatil Gün");
			}
			if (izinSureGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.izinSureGunKod(), "İzin Gün");
			}
			if (ucretliIzinGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerIzin);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.ucretliIzinGunKod(), "Ücretli İzin Gün");
			}
			if (ucretsizIzinGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerIzin);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.ucretsizIzinGunKod(), "Ücretsiz İzin Gün");
			}
			if (hastalikIzinGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerIzin);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.hastalikIzinGunKod(), "Hastalık İzin Gün");
			}
			if (normalGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerBGun);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.normalGunKod(), "Normal Gün");
			}
			if (haftaTatilGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerBGun);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.haftaTatilGunKod(), "H.Tatil Gün");
			}
			if (resmiTatilGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerBGun);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.resmiTatilGunKod(), "R.Tatil Gün");
			}
			if (artikGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerBGun);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.artikGunKod(), "Artık Gün");
			}
			if (bordroToplamGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerBTGun);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.bordroToplamGunKod(), "Toplam Gün");
			}
		}
		if (hataliPuantajVar) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			ExcelUtil.baslikCell(cell, anchor, helper, drawing, "Hata Açıklama", "Plan onaylanmamış veya hatalı günleri olan puantaj");
		}

		int sira = 0;
		double maxSure = denklestirmeAy.getFazlaMesaiMaxSure() != null ? denklestirmeAy.getFazlaMesaiMaxSure() : 11.0d;
		Date bugun = PdksUtil.getDate(new Date());
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();
			Personel personel = aylikPuantaj.getPdksPersonel();
			if (personel == null || PdksUtil.hasStringValue(personel.getSicilNo()) == false)
				continue;
			CalismaModeli calismaModeli = aylikPuantaj.getCalismaModeli();
			boolean hataVar = aylikPuantaj.isFazlaMesaiHesapla() == false;
			PersonelDenklestirme personelDenklestirme = aylikPuantaj.getPersonelDenklestirme();
			PersonelDenklestirme personelDenklestirmeGecenAy = personelDenklestirme != null ? personelDenklestirme.getPersonelDenklestirmeGecenAy() : null;
			row++;
			col = 0;
			CellStyle styleHataCenter = null;
			try {
				boolean help = helpPersonel(aylikPuantaj.getPdksPersonel());
				try {
					if (row % 2 != 0) {
						styleCenter = styleOddCenter;
						styleStrDay = styleCenterOddDay;
						styleGenel = styleOdd;
						styleTutar = styleTutarOdd;
						if (hataVar) {
							styleCenter = styleRedOddCenter;
							styleGenel = styleRedOdd;
							styleTutar = styleRedTutarOdd;
							styleHataCenter = styleRedCenterOddDay;
						}

					} else {
						styleCenter = styleEvenCenter;
						styleStrDay = styleCenterEvenDay;
						styleGenel = styleEven;
						styleTutar = styleTutarEven;
						if (hataVar) {
							styleCenter = styleRedEvenCenter;
							styleGenel = styleRedEven;
							styleTutar = styleRedTutarEven;
							styleHataCenter = styleRedCenterEvenDay;
						}

					}

					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(++sira);
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getSicilNo());
					if (kimlikGoster) {
						if (PdksUtil.hasStringValue(personel.getPersonelKGS().getKimlikNo()))
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getPersonelKGS().getKimlikNo());
						else
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue("");
					}
					Cell personelCell = ExcelUtil.getCell(sheet, row, col++, styleGenel);
					personelCell.setCellValue(personel.getAdSoyad());

					if (seciliEkSaha3Id == null)
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");

					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(aylikPuantaj.getYonetici() != null && aylikPuantaj.getYonetici().getId() != null ? aylikPuantaj.getYonetici().getAdSoyad() : "");
					if (sirketGoster)
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getSirket() != null ? personel.getSirket().getAd() : "");
					if (fazlaMesaiOde)
						ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(personelDenklestirme.getFazlaMesaiOde()));
					if (fazlaMesaiIzinKullan)
						ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(personelDenklestirme.getFazlaMesaiIzinKullan()));
					if (modelGoster) {
						String modelAciklama = "";
						if (aylikPuantaj.getPersonelDenklestirme() != null && aylikPuantaj.getPersonelDenklestirme().getCalismaModeliAy() != null) {
							CalismaModeliAy calismaModeliAy = aylikPuantaj.getPersonelDenklestirme().getCalismaModeliAy();
							if (calismaModeliAy.getCalismaModeli() != null)
								modelAciklama = calismaModeliAy.getCalismaModeli().getAciklama();
						}
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(modelAciklama);
					}
					List vardiyaList = aylikPuantaj.getAyinVardiyalari();

					for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
						VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
						Vardiya vardiya = vardiyaGun.getVardiya();
						String styleText = vardiyaGun.getAylikClassAdi(aylikPuantaj.getTrClass());
						Double sure = vardiyaGun.getCalismaSuresi();
						boolean maxSureGecti = sure != null && sure.doubleValue() > maxSure;
						styleDay = styleStrDay;
						if (maxSureGecti)
							styleDay = row % 2 != 0 ? styleCenterOddDayRed : styleCenterEvenDayRed;
						if (styleText.equals(VardiyaGun.STYLE_CLASS_HAFTA_TATIL)) {
							styleDay = maxSureGecti == false ? styleTatil : styleTatilRed;

						} else if (styleText.equals(VardiyaGun.STYLE_CLASS_IZIN)) {
							styleDay = styleIzin;

						} else if (styleText.equals(VardiyaGun.STYLE_CLASS_OZEL_ISTEK)) {
							styleDay = styleIstek;

						} else if (styleText.equals(VardiyaGun.STYLE_CLASS_EGITIM)) {
							styleDay = styleEgitim;

						} else if (styleText.equals(VardiyaGun.STYLE_CLASS_OFF)) {
							styleDay = maxSureGecti == false ? styleOff : styleOffRed;

						}
						boolean onayHata = false;
						String aciklama = "X";
						if (vardiya != null && vardiyaGun.getDurum() == false && vardiyaGun.getVardiyaDate().before(bugun)) {
							styleDay = styleHataCenter;
							onayHata = true;
						}

						cell = ExcelUtil.getCell(sheet, row, col++, styleDay);
						if (!onayHata) {
							aciklama = !help || calisan(vardiyaGun) ? vardiyaGun.getFazlaMesaiOzelAciklama(Boolean.TRUE, authenticatedUser.sayiFormatliGoster(sure)) : "";
							if (vardiya != null) {
								if (aciklama.equals("0")) {
									if (vardiya.isCalisma() == false || vardiyaGun.getTatil() != null || vardiyaGun.isIzinli())
										aciklama = ".";

								}
							} else {
								aciklama = vardiyaGun.isCalismayiBirakti() ? "İSTİFA" : "ÇALIŞMIYOR";
							}

						}

						cell.setCellValue(aciklama);

					}

					setCell(sheet, row, col++, styleTutar, aylikPuantaj.getSaatToplami());
					Cell planlananCell = setCell(sheet, row, col++, styleTutar, aylikPuantaj.getPlanlananSure());
					if (aylikPuantaj.getCalismaModeliAy() != null && planlananCell != null && aylikPuantaj.getSutIzniDurum().equals(Boolean.FALSE)) {
						String title = aylikPuantaj.getCalismaModeli().getAciklama() + " : ";
						if (aylikPuantaj.getCalismaModeli().getToplamGunGuncelle().equals(Boolean.FALSE))
							title += authenticatedUser.sayiFormatliGoster(aylikPuantaj.getCalismaModeliAy().getSure());
						else
							title += authenticatedUser.sayiFormatliGoster(aylikPuantaj.getPersonelDenklestirme().getPlanlanSure());
						if (PdksUtil.hasStringValue(title))
							ExcelUtil.setCellComment(planlananCell, anchor, helper, drawing, title);
					}
					if (yasalFazlaCalismaAsanSaat) {
						if (aylikPuantaj.getUcretiOdenenMesaiSure() > 0)
							setCell(sheet, row, col++, styleTutar, aylikPuantaj.getUcretiOdenenMesaiSure());
						else
							ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue("");
					}
					if (gerceklesenMesaiKod)
						setCell(sheet, row, col++, styleTutar, aylikPuantaj.getAylikNetFazlaMesai());
					if (devredenMesaiKod) {
						Double gecenAyFazlaMesai = aylikPuantaj.getGecenAyFazlaMesai(authenticatedUser);
						Cell gecenAyFazlaMesaiCell = setCell(sheet, row, col++, styleTutar, gecenAyFazlaMesai);
						if (gecenAyFazlaMesai != null && personelDenklestirmeGecenAy != null && gecenAyFazlaMesai.doubleValue() != 0.0d) {
							if (personelDenklestirmeGecenAy.getGuncelleyenUser() != null && personelDenklestirmeGecenAy.getGuncellemeTarihi() != null) {
								String title = "Onaylayan : " + personelDenklestirmeGecenAy.getGuncelleyenUser().getAdSoyad() + "\n";
								title += "Zaman : " + authenticatedUser.dateTimeFormatla(personelDenklestirmeGecenAy.getGuncellemeTarihi());
								ExcelUtil.setCellComment(gecenAyFazlaMesaiCell, anchor, helper, drawing, title);
							}
						}
					}
					boolean olustur = false;
					Comment commentGuncelleyen = null;
					if (fazlaMesaiVar) {
						if (aylikPuantaj.isFazlaMesaiHesapla()) {
							Cell fazlaMesaiSureCell = setCell(sheet, row, col++, styleTutar, aylikPuantaj.getFazlaMesaiSure());
							if (aylikPuantaj.getFazlaMesaiSure() != 0.0d) {
								if (personelDenklestirme.getGuncelleyenUser() != null && personelDenklestirme.getGuncellemeTarihi() != null)
									commentGuncelleyen = fazlaMesaiOrtakIslemler.getCommentGuncelleyen(anchor, helper, drawing, personelDenklestirme);
								fazlaMesaiSureCell.setCellComment(commentGuncelleyen);
								olustur = true;
							}
						} else
							ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
					}
					if (kismiOdemeGoster) {
						if (personelDenklestirme.getKismiOdemeSure() != null && personelDenklestirme.getKismiOdemeSure().doubleValue() > 0.0d)
							setCell(sheet, row, col++, styleTutar, personelDenklestirme.getKismiOdemeSure());
						else
							ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
					}
					if (resmiTatilVar || bordroPuantajEkranindaGoster)
						setCell(sheet, row, col++, styleTutar, aylikPuantaj.getResmiTatilToplami());
					if (haftaTatilVar)
						setCell(sheet, row, col++, styleTutar, aylikPuantaj.getHaftaCalismaSuresi());
					if (devredenBakiyeKod) {
						if (aylikPuantaj.isFazlaMesaiHesapla()) {
							Cell devredenSureCell = setCell(sheet, row, col++, styleTutar, aylikPuantaj.getDevredenSure());
							if (aylikPuantaj.getDevredenSure() != null && aylikPuantaj.getDevredenSure().doubleValue() != 0.0d && commentGuncelleyen == null) {
								if (olustur)
									commentGuncelleyen = fazlaMesaiOrtakIslemler.getCommentGuncelleyen(anchor, helper, drawing, personelDenklestirme);
								if (commentGuncelleyen != null)
									devredenSureCell.setCellComment(commentGuncelleyen);
							}
						} else
							ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
					}

					if (aksamGun)
						setCell(sheet, row, col++, styleTutar, new Double(aylikPuantaj.getAksamVardiyaSayisi()));
					if (aksamSaat)
						setCell(sheet, row, col++, styleTutar, new Double(aylikPuantaj.getAksamVardiyaSaatSayisi()));

					if (denklestirmeDinamikAlanlar != null && !denklestirmeDinamikAlanlar.isEmpty()) {
						for (Tanim alan : denklestirmeDinamikAlanlar) {
							PersonelDenklestirmeDinamikAlan denklestirmeDinamikAlan = aylikPuantaj.getDinamikAlan(alan.getId());
							if (denklestirmeDinamikAlan == null)
								ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
							else {
								if (denklestirmeDinamikAlan.isDevamlilikPrimi())
									ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(denklestirmeDinamikAlan.getIslemDurum() ? "+" : "-");
								else {
									String str = authenticatedUser.getYesNo(denklestirmeDinamikAlan.getIslemDurum());
									if (denklestirmeDinamikAlan.getSayisalDeger() != null && denklestirmeDinamikAlan.getSayisalDeger().doubleValue() > 0.0d) {
										String deger = authenticatedUser.sayiFormatliGoster(denklestirmeDinamikAlan.getSayisalDeger());
										if (denklestirmeDinamikAlan.isIzinDurum())
											str += "\nSüre : " + deger;
										else
											str += "\n " + deger;
										ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(str);
									}
								}
							}

						}
					}
					if (bordroPuantajEkranindaGoster) {
						PersonelDenklestirmeBordro denklestirmeBordro = aylikPuantaj.getDenklestirmeBordro();
						if (denklestirmeBordro == null) {
							denklestirmeBordro = new PersonelDenklestirmeBordro();
							denklestirmeBordro.setPersonelDenklestirme(aylikPuantaj.getPersonelDenklestirme());
						}

						boolean saatlikCalisma = calismaModeli.isSaatlikOdeme();
						if (denklestirmeBordro.getDetayMap() == null)
							denklestirmeBordro.setDetayMap(new HashMap<BordroDetayTipi, PersonelDenklestirmeBordroDetay>());
						if (normalCalismaSaatKod)
							setCell(sheet, row, col++, styleGenel, saatlikCalisma ? denklestirmeBordro.getSaatNormal() : 0);
						if (haftaTatilCalismaSaatKod)
							setCell(sheet, row, col++, styleGenel, saatlikCalisma ? denklestirmeBordro.getSaatHaftaTatil() : 0);
						if (resmiTatilCalismaSaatKod)
							setCell(sheet, row, col++, styleGenel, saatlikCalisma ? denklestirmeBordro.getSaatResmiTatil() : 0);
						if (izinSureSaatKod)
							setCell(sheet, row, col++, styleGenel, saatlikCalisma ? denklestirmeBordro.getSaatIzin() : 0);
						if (normalCalismaGunKod)
							setCell(sheet, row, col++, styleGenel, saatlikCalisma == false ? denklestirmeBordro.getSaatNormal() : 0);
						if (haftaTatilCalismaGunKod)
							setCell(sheet, row, col++, styleGenel, saatlikCalisma == false ? denklestirmeBordro.getSaatHaftaTatil() : 0);
						if (resmiTatilCalismaGunKod)
							setCell(sheet, row, col++, styleGenel, saatlikCalisma == false ? denklestirmeBordro.getSaatResmiTatil() : 0);
						if (izinSureGunKod)
							setCell(sheet, row, col++, styleGenel, saatlikCalisma == false ? denklestirmeBordro.getSaatIzin() : 0);
						if (ucretliIzinGunKod)
							setCell(sheet, row, col++, styleGenel, denklestirmeBordro.getUcretliIzin().doubleValue());
						if (ucretsizIzinGunKod)
							setCell(sheet, row, col++, styleGenel, denklestirmeBordro.getUcretsizIzin().doubleValue());
						if (hastalikIzinGunKod)
							setCell(sheet, row, col++, styleGenel, denklestirmeBordro.getRaporluIzin().doubleValue());
						if (normalGunKod)
							setCell(sheet, row, col++, styleGenel, denklestirmeBordro.getNormalGunAdet());
						if (haftaTatilGunKod)
							setCell(sheet, row, col++, styleGenel, denklestirmeBordro.getHaftaTatilAdet());
						if (resmiTatilGunKod)
							setCell(sheet, row, col++, styleGenel, denklestirmeBordro.getResmiTatilAdet());
						if (artikGunKod)
							setCell(sheet, row, col++, styleGenel, denklestirmeBordro.getArtikAdet());
						if (bordroToplamGunKod)
							setCell(sheet, row, col++, styleGenel, denklestirmeBordro.getBordroToplamGunAdet());

					}

					if (hataliPuantajVar) {
						String hataAciklama = "";
						if (!aylikPuantaj.isFazlaMesaiHesapla()) {
							hataAciklama = personelDenklestirme.isOnaylandi() ? "Günler hatalı" : "Plan onaysız";
						}
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(hataAciklama);
					}

					styleGenel = null;
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());
					logger.error(row);

				}
			} catch (Exception ex) {
				logger.error(ex);
				ex.printStackTrace();
			}

		}

		try {

			for (int i = 0; i <= col; i++)
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

	/**
	 * @param sheet
	 * @param rowNo
	 * @param columnNo
	 * @param style
	 * @param deger
	 * @return
	 */
	public Cell setCell(Sheet sheet, int rowNo, int columnNo, CellStyle style, Double deger) {
		Cell cell = ExcelUtil.getCell(sheet, rowNo, columnNo, style);

		try {
			if (deger != 0.0d) {
				cell.setCellValue(authenticatedUser.sayiFormatliGoster(deger));
			}

		} catch (Exception e) {
		}
		return cell;
	}

	/**
	 * @param sheet
	 * @param rowNo
	 * @param columnNo
	 * @param style
	 * @param deger
	 * @return
	 */
	public Cell setCellDate(Sheet sheet, int rowNo, int columnNo, CellStyle style, Date date) {
		Cell cell = ExcelUtil.getCell(sheet, rowNo, columnNo, style);

		try {
			if (date != null) {
				cell.setCellValue(date);
			} else
				cell.setCellValue("");

		} catch (Exception e) {
		}
		return cell;
	}

	private boolean calisan(VardiyaGun vardiyaGun) {
		boolean calisan = vardiyaGun != null;
		if (calisan) {
			if (vardiyaGun.getVardiya() != null) {

				calisan = vardiyaGun.isKullaniciYetkili() || (vardiyaGun.getIzin() != null && !helpPersonel(vardiyaGun.getPersonel()));
			}
		}
		return calisan;
	}

	private boolean helpPersonel(Personel personel) {
		return false;

	}

	public void tesisDoldur(boolean bolumDoldurDurum) throws Exception {
		sirket = null;
		bolumleriTemizle();
		if (pdksSirketList == null || pdksSirketList.isEmpty())
			setTesisList(new ArrayList<SelectItem>());
		else {
			if (sirketId != null) {
				HashMap fields = new HashMap();
				fields.put("id", sirketId);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);
				if (!sirket.isTesisDurumu())
					tesisId = null;
			}
			List<SelectItem> list = fazlaMesaiOrtakIslemler.getFazlaMesaiTesisList(sirket, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, true, session);
			setTesisList(list);
			Long onceki = tesisId;
			if (list != null && !list.isEmpty()) {
				if (list.size() == 1 || onceki == null)
					tesisId = (Long) list.get(0).getValue();
				else if (onceki != null) {
					tesisId = null;
					for (SelectItem st : list) {
						if (st.getValue().equals(onceki))
							tesisId = onceki;
					}
				}
			}
			if (!bolumDoldurDurum)
				if (sirket != null && sirket.isTesisDurumu() == false)
					bolumDoldurDurum = true;
			onceki = tesisId;

			if (tesisId != null || (bolumDoldurDurum)) {
				bolumDoldur();
				setTesisId(onceki);

			}
			if (denklestirmeAyDurum == false)
				hataliPuantajGoster = Boolean.FALSE;
		}
		aylikPuantajList.clear();
	}

	public String bolumDoldur() {
		fazlaMesaiVardiyaGun = null;
		linkAdres = null;
		stajerSirket = Boolean.FALSE;
		bolumleriTemizle();
		if (pdksSirketList == null || pdksSirketList.isEmpty())
			setGorevYeriList(new ArrayList<SelectItem>());
		else {
			HashMap fields = new HashMap();
			fields.put("ay", ay);
			fields.put("yil", yil);
			if (personelDenklestirmeList != null)
				personelDenklestirmeList.clear();
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
			if (authenticatedUser.getSuperVisorHemsirePersonelNoList() != null) {
				if (hastaneSuperVisor == null) {
					String calistigiSayfa = authenticatedUser.getCalistigiSayfa();
					String superVisorHemsireSayfalari = ortakIslemler.getParameterKey("superVisorHemsireSayfalari");
					List<String> sayfalar = PdksUtil.hasStringValue(superVisorHemsireSayfalari) ? PdksUtil.getListByString(superVisorHemsireSayfalari, null) : null;
					hastaneSuperVisor = sayfalar != null && sayfalar.contains(calistigiSayfa);
				}

			} else
				hastaneSuperVisor = Boolean.FALSE;
			if (aylikPuantajList == null)
				aylikPuantajList = new ArrayList<AylikPuantaj>();
			else
				aylikPuantajList.clear();
			Sirket sirket = null;
			if (sirketId != null) {
				HashMap parametreMap = new HashMap();
				parametreMap.put("id", sirketId);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				sirket = (Sirket) pdksEntityController.getObjectByInnerObject(parametreMap, Sirket.class);
			}
			setSirket(sirket);

			if (sirket != null) {
				setDepartman(sirket.getDepartman());
				if (departman.isAdminMi() && sirket.isTesisDurumu()) {
					gorevYeriList = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, tesisId != null ? String.valueOf(tesisId) : null, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, true, session);
				} else {
					gorevYeriList = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, null, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, true, session);
				}

				if (gorevYeriList.size() == 1) {
					seciliEkSaha3Id = (Long) gorevYeriList.get(0).getValue();
				}

			}
		}

		aylikPuantajList.clear();

		return "";
	}

	public void vardiyaGoster(VardiyaGun vg) {
		setVardiyaGun(vg);
		fazlaMesaiVardiyaGun = vg;
		toplamFazlamMesai = 0D;
		Long key = vg.getId();
		fmtList = fmtMap.containsKey(key) ? fmtMap.get(key) : null;

		if (vg.getIzin() == null && vg.getIzinler() != null) {
			for (Iterator iterator = vg.getIzinler().iterator(); iterator.hasNext();) {
				PersonelIzin personelIzin = (PersonelIzin) iterator.next();
				if (personelIzin.isGunlukOldu())
					iterator.remove();
			}
		}
		if (vg.getOrjinalHareketler() != null) {
			for (HareketKGS hareket : vg.getOrjinalHareketler()) {
				if (hareket.getPersonelFazlaMesai() != null && hareket.getPersonelFazlaMesai().isOnaylandi()) {
					if (hareket.getPersonelFazlaMesai().getFazlaMesaiSaati() != null)
						toplamFazlamMesai += hareket.getPersonelFazlaMesai().getFazlaMesaiSaati();
				}
			}
		}
	}

	// Haftalık çalışma planlarından giriş çıkış hareketi, izinler ve fazla
	// mesailerden haftalık toplam çalışma durumu hesaplanır

	public List<PersonelDenklestirme> getPersonelDenklestirmeList() {
		return personelDenklestirmeList;
	}

	public void setPersonelDenklestirmeList(List<PersonelDenklestirme> personelDenklestirmeList) {
		this.personelDenklestirmeList = personelDenklestirmeList;
	}

	public List<DepartmanDenklestirmeDonemi> getDenklestirmeDonemiList() {
		return denklestirmeDonemiList;
	}

	public void setDenklestirmeDonemiList(List<DepartmanDenklestirmeDonemi> denklestirmeDonemiList) {
		this.denklestirmeDonemiList = denklestirmeDonemiList;
	}

	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket value) {
		this.sirket = value;
	}

	public List<PersonelDenklestirme> getBaslikDenklestirmeDonemiList() {
		return baslikDenklestirmeDonemiList;
	}

	public void setBaslikDenklestirmeDonemiList(List<PersonelDenklestirme> baslikDenklestirmeDonemiList) {
		this.baslikDenklestirmeDonemiList = baslikDenklestirmeDonemiList;
	}

	public Boolean getHataYok() {
		return hataYok;
	}

	public void setHataYok(Boolean hataYok) {
		this.hataYok = hataYok;
	}

	public String getSicilNo() {
		return sicilNo;
	}

	public void setSicilNo(String sicilNo) {
		this.sicilNo = sicilNo;
	}

	public List<YemekIzin> getYemekAraliklari() {
		return yemekAraliklari;
	}

	public void setYemekAraliklari(List<YemekIzin> yemekAraliklari) {
		this.yemekAraliklari = yemekAraliklari;
	}

	public int getYil() {
		return yil;
	}

	public void setYil(int yil) {
		this.yil = yil;
	}

	public int getAy() {
		return ay;
	}

	public void setAy(int ay) {
		this.ay = ay;
	}

	public List<SelectItem> getAylar() {
		return aylar;
	}

	public void setAylar(List<SelectItem> aylar) {
		this.aylar = aylar;
	}

	public int getMaxYil() {
		return maxYil;
	}

	public void setMaxYil(int maxYil) {
		this.maxYil = maxYil;
	}

	public Boolean getYetkili() {
		return yetkili;
	}

	public void setYetkili(Boolean yetkili) {
		this.yetkili = yetkili;
	}

	public void setAylikPuantajList(List<AylikPuantaj> aylikPuantajList) {
		this.aylikPuantajList = aylikPuantajList;
	}

	public List<AylikPuantaj> getAylikPuantajList() {
		return aylikPuantajList;
	}

	public AylikPuantaj getAylikPuantajDefault() {
		return aylikPuantajDefault;
	}

	public void setAylikPuantajDefault(AylikPuantaj aylikPuantajDefault) {
		this.aylikPuantajDefault = aylikPuantajDefault;
	}

	public DenklestirmeAy getDenklestirmeAy() {
		return denklestirmeAy;
	}

	public void setDenklestirmeAy(DenklestirmeAy denklestirmeAy) {
		this.denklestirmeAy = denklestirmeAy;
	}

	public VardiyaGun getVardiyaGun() {
		return vardiyaGun;
	}

	public void setVardiyaGun(VardiyaGun vardiyaGun) {
		this.vardiyaGun = vardiyaGun;
	}

	public List<SelectItem> getGorevYeriList() {
		return gorevYeriList;
	}

	public void setGorevYeriList(List<SelectItem> value) {
		this.gorevYeriList = value;
	}

	public Long getSeciliEkSaha3Id() {
		return seciliEkSaha3Id;
	}

	public void setSeciliEkSaha3Id(Long seciliEkSaha3Id) {
		this.seciliEkSaha3Id = seciliEkSaha3Id;
	}

	public Tanim getGorevYeri() {
		return gorevYeri;
	}

	public void setGorevYeri(Tanim gorevYeri) {
		this.gorevYeri = gorevYeri;
	}

	public TreeMap<String, Tanim> getEkSahaTanimMap() {
		return ekSahaTanimMap;
	}

	public void setEkSahaTanimMap(TreeMap<String, Tanim> ekSahaTanimMap) {
		this.ekSahaTanimMap = ekSahaTanimMap;
	}

	public Boolean getResmiTatilVar() {
		return resmiTatilVar;
	}

	public void setResmiTatilVar(Boolean resmiTatilVar) {
		this.resmiTatilVar = resmiTatilVar;
	}

	public Boolean getKaydetDurum() {
		return kaydetDurum;
	}

	public void setKaydetDurum(Boolean kaydetDurum) {
		this.kaydetDurum = kaydetDurum;
	}

	public Long getGorevTipiId() {
		return gorevTipiId;
	}

	public void setGorevTipiId(Long gorevTipiId) {
		this.gorevTipiId = gorevTipiId;
	}

	public Long getSirketId() {
		return sirketId;
	}

	public void setSirketId(Long sirketId) {
		this.sirketId = sirketId;
	}

	public Boolean getSutIzniGoster() {
		return sutIzniGoster;
	}

	public void setSutIzniGoster(Boolean sutIzniGoster) {
		this.sutIzniGoster = sutIzniGoster;
	}

	public byte[] getExcelData() {
		return excelData;
	}

	public void setExcelData(byte[] excelData) {
		this.excelData = excelData;
	}

	public String getExcelDosyaAdi() {
		return excelDosyaAdi;
	}

	public void setExcelDosyaAdi(String excelDosyaAdi) {
		this.excelDosyaAdi = excelDosyaAdi;
	}

	public String getMailKonu() {
		return mailKonu;
	}

	public void setMailKonu(String mailKonu) {
		this.mailKonu = mailKonu;
	}

	public String getMailIcerik() {
		return mailIcerik;
	}

	public void setMailIcerik(String mailIcerik) {
		this.mailIcerik = mailIcerik;
	}

	public List<User> getToList() {
		return toList;
	}

	public void setToList(List<User> toList) {
		this.toList = toList;
	}

	public List<User> getCcList() {
		return ccList;
	}

	public void setCcList(List<User> ccList) {
		this.ccList = ccList;
	}

	public List<User> getBccList() {
		return bccList;
	}

	public void setBccList(List<User> bccList) {
		this.bccList = bccList;
	}

	public boolean isMailGonder() {
		return mailGonder;
	}

	public void setMailGonder(boolean mailGonder) {
		this.mailGonder = mailGonder;
	}

	public Boolean getOnayla() {
		return onayla;
	}

	public void setOnayla(Boolean onayla) {
		this.onayla = onayla;
	}

	public Long getDepartmanId() {
		return departmanId;
	}

	public void setDepartmanId(Long departmanId) {
		this.departmanId = departmanId;
	}

	public List<SelectItem> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<SelectItem> departmanList) {
		this.departmanList = departmanList;
	}

	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman value) {
		this.departman = value;
	}

	public Tanim getSeciliBolum() {
		return seciliBolum;
	}

	public void setSeciliBolum(Tanim seciliBolum) {
		this.seciliBolum = seciliBolum;
	}

	public Boolean getHastaneSuperVisor() {
		return hastaneSuperVisor;
	}

	public void setHastaneSuperVisor(Boolean hastaneSuperVisor) {
		this.hastaneSuperVisor = hastaneSuperVisor;
	}

	public Double getToplamFazlamMesai() {
		return toplamFazlamMesai;
	}

	public void setToplamFazlamMesai(Double toplamFazlamMesai) {
		this.toplamFazlamMesai = toplamFazlamMesai;
	}

	public Vardiya getSabahVardiya() {
		return sabahVardiya;
	}

	public void setSabahVardiya(Vardiya sabahVardiya) {
		this.sabahVardiya = sabahVardiya;
	}

	public List<SelectItem> getBolumDepartmanlari() {
		return bolumDepartmanlari;
	}

	public void setBolumDepartmanlari(List<SelectItem> bolumDepartmanlari) {
		this.bolumDepartmanlari = bolumDepartmanlari;
	}

	public Boolean getAksamGun() {
		return aksamGun;
	}

	public void setAksamGun(Boolean aksamGun) {
		this.aksamGun = aksamGun;
	}

	public Boolean getAksamSaat() {
		return aksamSaat;
	}

	public void setAksamSaat(Boolean aksamSaat) {
		this.aksamSaat = aksamSaat;
	}

	public String getAdres() {
		return adres;
	}

	public void setAdres(String adres) {
		this.adres = adres;
	}

	public String getPersonelIzinGirisiStr() {
		return personelIzinGirisiStr;
	}

	public void setPersonelIzinGirisiStr(String personelIzinGirisiStr) {
		this.personelIzinGirisiStr = personelIzinGirisiStr;
	}

	public String getPersonelHareketStr() {
		return personelHareketStr;
	}

	public void setPersonelHareketStr(String personelHareketStr) {
		this.personelHareketStr = personelHareketStr;
	}

	public String getPersonelFazlaMesaiOrjStr() {
		return personelFazlaMesaiOrjStr;
	}

	public void setPersonelFazlaMesaiOrjStr(String personelFazlaMesaiOrjStr) {
		this.personelFazlaMesaiOrjStr = personelFazlaMesaiOrjStr;
	}

	public String getPersonelFazlaMesaiStr() {
		return personelFazlaMesaiStr;
	}

	public void setPersonelFazlaMesaiStr(String personelFazlaMesaiStr) {
		this.personelFazlaMesaiStr = personelFazlaMesaiStr;
	}

	public List<String> getSabahVardiyalar() {
		return sabahVardiyalar;
	}

	public void setSabahVardiyalar(List<String> sabahVardiyalar) {
		this.sabahVardiyalar = sabahVardiyalar;
	}

	public String getVardiyaPlaniStr() {
		return vardiyaPlaniStr;
	}

	public void setVardiyaPlaniStr(String vardiyaPlaniStr) {
		this.vardiyaPlaniStr = vardiyaPlaniStr;
	}

	public Boolean getPartTimeGoster() {
		return partTimeGoster;
	}

	public void setPartTimeGoster(Boolean partTimeGoster) {
		this.partTimeGoster = partTimeGoster;
	}

	public Boolean getStajerSirket() {
		return stajerSirket;
	}

	public void setStajerSirket(Boolean stajerSirket) {
		this.stajerSirket = stajerSirket;
	}

	public Boolean getBakiyeGuncelle() {
		return bakiyeGuncelle;
	}

	public void setBakiyeGuncelle(Boolean bakiyeGuncelle) {
		this.bakiyeGuncelle = bakiyeGuncelle;
	}

	public List<SelectItem> getPdksSirketList() {
		return pdksSirketList;
	}

	public void setPdksSirketList(List<SelectItem> value) {
		this.pdksSirketList = value;
	}

	public Boolean getHaftaTatilVar() {
		return haftaTatilVar;
	}

	public void setHaftaTatilVar(Boolean haftaTatilVar) {
		this.haftaTatilVar = haftaTatilVar;
	}

	public List<SelectItem> getTesisList() {
		return tesisList;
	}

	public void setTesisList(List<SelectItem> tesisList) {
		this.tesisList = tesisList;
	}

	public Long getTesisId() {
		return tesisId;
	}

	public void setTesisId(Long tesisId) {
		this.tesisId = tesisId;
	}

	public Boolean getDepartmanBolumAyni() {
		return departmanBolumAyni;
	}

	public void setDepartmanBolumAyni(Boolean departmanBolumAyni) {
		this.departmanBolumAyni = departmanBolumAyni;
	}

	public boolean isTekSirket() {
		return tekSirket;
	}

	public void setTekSirket(boolean tekSirket) {
		this.tekSirket = tekSirket;
	}

	public Boolean getModelGoster() {
		return modelGoster;
	}

	public void setModelGoster(Boolean modelGoster) {
		this.modelGoster = modelGoster;
	}

	public String getMsgError() {
		return msgError;
	}

	public void setMsgError(String msgError) {
		this.msgError = msgError;
	}

	public String getMsgFazlaMesaiError() {
		return msgFazlaMesaiError;
	}

	public void setMsgFazlaMesaiError(String msgFazlaMesaiError) {
		this.msgFazlaMesaiError = msgFazlaMesaiError;
	}

	public TreeMap<String, Tanim> getFazlaMesaiMap() {
		return fazlaMesaiMap;
	}

	public void setFazlaMesaiMap(TreeMap<String, Tanim> fazlaMesaiMap) {
		this.fazlaMesaiMap = fazlaMesaiMap;
	}

	public Integer getAksamVardiyaBasSaat() {
		return aksamVardiyaBasSaat;
	}

	public void setAksamVardiyaBasSaat(Integer aksamVardiyaBasSaat) {
		this.aksamVardiyaBasSaat = aksamVardiyaBasSaat;
	}

	public Integer getAksamVardiyaBasDakika() {
		return aksamVardiyaBasDakika;
	}

	public void setAksamVardiyaBasDakika(Integer aksamVardiyaBasDakika) {
		this.aksamVardiyaBasDakika = aksamVardiyaBasDakika;
	}

	public Integer getAksamVardiyaBitDakika() {
		return aksamVardiyaBitDakika;
	}

	public void setAksamVardiyaBitDakika(Integer aksamVardiyaBitDakika) {
		this.aksamVardiyaBitDakika = aksamVardiyaBitDakika;
	}

	public TreeMap<Long, List<FazlaMesaiTalep>> getFmtMap() {
		return fmtMap;
	}

	public void setFmtMap(TreeMap<Long, List<FazlaMesaiTalep>> fmtMap) {
		this.fmtMap = fmtMap;
	}

	public List<FazlaMesaiTalep> getFmtList() {
		return fmtList;
	}

	public void setFmtList(List<FazlaMesaiTalep> fmtList) {
		this.fmtList = fmtList;
	}

	public Boolean getAyrikHareketVar() {
		return ayrikHareketVar;
	}

	public void setAyrikHareketVar(Boolean ayrikHareketVar) {
		this.ayrikHareketVar = ayrikHareketVar;
	}

	public String getSanalPersonelAciklama() {
		return sanalPersonelAciklama;
	}

	public void setSanalPersonelAciklama(String sanalPersonelAciklama) {
		this.sanalPersonelAciklama = sanalPersonelAciklama;
	}

	public Boolean getHataliPuantajGoster() {
		return hataliPuantajGoster;
	}

	public void setHataliPuantajGoster(Boolean hataliPuantajGoster) {
		this.hataliPuantajGoster = hataliPuantajGoster;
	}

	public Boolean getFazlaMesaiIzinKullan() {
		return fazlaMesaiIzinKullan;
	}

	public void setFazlaMesaiIzinKullan(Boolean fazlaMesaiIzinKullan) {
		this.fazlaMesaiIzinKullan = fazlaMesaiIzinKullan;
	}

	public Boolean getSirketIzinGirisDurum() {
		return sirketIzinGirisDurum;
	}

	public void setSirketIzinGirisDurum(Boolean sirketIzinGirisDurum) {
		this.sirketIzinGirisDurum = sirketIzinGirisDurum;
	}

	public CalismaModeli getPerCalismaModeli() {
		return perCalismaModeli;
	}

	public void setPerCalismaModeli(CalismaModeli perCalismaModeli) {
		this.perCalismaModeli = perCalismaModeli;
	}

	public Boolean getKullaniciPersonel() {
		return kullaniciPersonel;
	}

	public void setKullaniciPersonel(Boolean kullaniciPersonel) {
		this.kullaniciPersonel = kullaniciPersonel;
	}

	public Boolean getDenklestirmeAyDurum() {
		return denklestirmeAyDurum;
	}

	public void setDenklestirmeAyDurum(Boolean denklestirmeAyDurum) {
		this.denklestirmeAyDurum = denklestirmeAyDurum;
	}

	public boolean isAdminRole() {
		return adminRole;
	}

	public void setAdminRole(boolean adminRole) {
		this.adminRole = adminRole;
	}

	public boolean isIkRole() {
		return ikRole;
	}

	public void setIkRole(boolean ikRole) {
		this.ikRole = ikRole;
	}

	public HashMap<String, List<Tanim>> getEkSahaListMap() {
		return ekSahaListMap;
	}

	public void setEkSahaListMap(HashMap<String, List<Tanim>> ekSahaListMap) {
		this.ekSahaListMap = ekSahaListMap;
	}

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}

	public Boolean getMaasKesintiGoster() {
		return maasKesintiGoster;
	}

	public void setMaasKesintiGoster(Boolean maasKesintiGoster) {
		this.maasKesintiGoster = maasKesintiGoster;
	}

	/**
	 * @return the yoneticiERP1Kontrol
	 */
	public Boolean getYoneticiERP1Kontrol() {
		return yoneticiERP1Kontrol;
	}

	/**
	 * @param yoneticiERP1Kontrol
	 *            the yoneticiERP1Kontrol to set
	 */
	public void setYoneticiERP1Kontrol(Boolean yoneticiERP1Kontrol) {
		this.yoneticiERP1Kontrol = yoneticiERP1Kontrol;
	}

	public Boolean getFazlaMesaiOde() {
		return fazlaMesaiOde;
	}

	public void setFazlaMesaiOde(Boolean fazlaMesaiOde) {
		this.fazlaMesaiOde = fazlaMesaiOde;
	}

	public Boolean getKimlikGoster() {
		return kimlikGoster;
	}

	public void setKimlikGoster(Boolean kimlikGoster) {
		this.kimlikGoster = kimlikGoster;
	}

	public Boolean getYasalFazlaCalismaAsanSaat() {
		return yasalFazlaCalismaAsanSaat;
	}

	public void setYasalFazlaCalismaAsanSaat(Boolean yasalFazlaCalismaAsanSaat) {
		this.yasalFazlaCalismaAsanSaat = yasalFazlaCalismaAsanSaat;
	}

	public Boolean getSirketGoster() {
		return sirketGoster;
	}

	public void setSirketGoster(Boolean sirketGoster) {
		this.sirketGoster = sirketGoster;
	}

	public boolean isBordroPuantajEkranindaGoster() {
		return bordroPuantajEkranindaGoster;
	}

	public void setBordroPuantajEkranindaGoster(boolean bordroPuantajEkranindaGoster) {
		this.bordroPuantajEkranindaGoster = bordroPuantajEkranindaGoster;
	}

	public boolean isFazlaMesaiVar() {
		return fazlaMesaiVar;
	}

	public void setFazlaMesaiVar(boolean fazlaMesaiVar) {
		this.fazlaMesaiVar = fazlaMesaiVar;
	}

	public boolean isSaatlikMesaiVar() {
		return saatlikMesaiVar;
	}

	public void setSaatlikMesaiVar(boolean saatlikMesaiVar) {
		this.saatlikMesaiVar = saatlikMesaiVar;
	}

	public boolean isAylikMesaiVar() {
		return aylikMesaiVar;
	}

	public void setAylikMesaiVar(boolean aylikMesaiVar) {
		this.aylikMesaiVar = aylikMesaiVar;
	}

	public TreeMap<String, Tatil> getTatilGunleriMap() {
		return tatilGunleriMap;
	}

	public void setTatilGunleriMap(TreeMap<String, Tatil> tatilGunleriMap) {
		this.tatilGunleriMap = tatilGunleriMap;
	}

	public Boolean getGerceklesenMesaiKod() {
		return gerceklesenMesaiKod;
	}

	public void setGerceklesenMesaiKod(Boolean gerceklesenMesaiKod) {
		this.gerceklesenMesaiKod = gerceklesenMesaiKod;
	}

	public Boolean getDevredenBakiyeKod() {
		return devredenBakiyeKod;
	}

	public void setDevredenBakiyeKod(Boolean devredenBakiyeKod) {
		this.devredenBakiyeKod = devredenBakiyeKod;
	}

	public Boolean getNormalCalismaSaatKod() {
		return normalCalismaSaatKod;
	}

	public void setNormalCalismaSaatKod(Boolean normalCalismaSaatKod) {
		this.normalCalismaSaatKod = normalCalismaSaatKod;
	}

	public Boolean getHaftaTatilCalismaSaatKod() {
		return haftaTatilCalismaSaatKod;
	}

	public void setHaftaTatilCalismaSaatKod(Boolean haftaTatilCalismaSaatKod) {
		this.haftaTatilCalismaSaatKod = haftaTatilCalismaSaatKod;
	}

	public Boolean getResmiTatilCalismaSaatKod() {
		return resmiTatilCalismaSaatKod;
	}

	public void setResmiTatilCalismaSaatKod(Boolean resmiTatilCalismaSaatKod) {
		this.resmiTatilCalismaSaatKod = resmiTatilCalismaSaatKod;
	}

	public Boolean getIzinSureSaatKod() {
		return izinSureSaatKod;
	}

	public void setIzinSureSaatKod(Boolean izinSureSaatKod) {
		this.izinSureSaatKod = izinSureSaatKod;
	}

	public Boolean getNormalCalismaGunKod() {
		return normalCalismaGunKod;
	}

	public void setNormalCalismaGunKod(Boolean normalCalismaGunKod) {
		this.normalCalismaGunKod = normalCalismaGunKod;
	}

	public Boolean getHaftaTatilCalismaGunKod() {
		return haftaTatilCalismaGunKod;
	}

	public void setHaftaTatilCalismaGunKod(Boolean haftaTatilCalismaGunKod) {
		this.haftaTatilCalismaGunKod = haftaTatilCalismaGunKod;
	}

	public Boolean getResmiTatilCalismaGunKod() {
		return resmiTatilCalismaGunKod;
	}

	public void setResmiTatilCalismaGunKod(Boolean resmiTatilCalismaGunKod) {
		this.resmiTatilCalismaGunKod = resmiTatilCalismaGunKod;
	}

	public Boolean getIzinSureGunKod() {
		return izinSureGunKod;
	}

	public void setIzinSureGunKod(Boolean izinSureGunKod) {
		this.izinSureGunKod = izinSureGunKod;
	}

	public Boolean getUcretliIzinGunKod() {
		return ucretliIzinGunKod;
	}

	public void setUcretliIzinGunKod(Boolean ucretliIzinGunKod) {
		this.ucretliIzinGunKod = ucretliIzinGunKod;
	}

	public Boolean getUcretsizIzinGunKod() {
		return ucretsizIzinGunKod;
	}

	public void setUcretsizIzinGunKod(Boolean ucretsizIzinGunKod) {
		this.ucretsizIzinGunKod = ucretsizIzinGunKod;
	}

	public Boolean getHastalikIzinGunKod() {
		return hastalikIzinGunKod;
	}

	public void setHastalikIzinGunKod(Boolean hastalikIzinGunKod) {
		this.hastalikIzinGunKod = hastalikIzinGunKod;
	}

	public Boolean getNormalGunKod() {
		return normalGunKod;
	}

	public void setNormalGunKod(Boolean normalGunKod) {
		this.normalGunKod = normalGunKod;
	}

	public Boolean getHaftaTatilGunKod() {
		return haftaTatilGunKod;
	}

	public void setHaftaTatilGunKod(Boolean haftaTatilGunKod) {
		this.haftaTatilGunKod = haftaTatilGunKod;
	}

	public Boolean getResmiTatilGunKod() {
		return resmiTatilGunKod;
	}

	public void setResmiTatilGunKod(Boolean resmiTatilGunKod) {
		this.resmiTatilGunKod = resmiTatilGunKod;
	}

	public Boolean getArtikGunKod() {
		return artikGunKod;
	}

	public void setArtikGunKod(Boolean artikGunKod) {
		this.artikGunKod = artikGunKod;
	}

	public Boolean getBordroToplamGunKod() {
		return bordroToplamGunKod;
	}

	public void setBordroToplamGunKod(Boolean bordroToplamGunKod) {
		this.bordroToplamGunKod = bordroToplamGunKod;
	}

	public Boolean getDevredenMesaiKod() {
		return devredenMesaiKod;
	}

	public void setDevredenMesaiKod(Boolean devredenMesaiKod) {
		this.devredenMesaiKod = devredenMesaiKod;
	}

	public Boolean getUcretiOdenenKod() {
		return ucretiOdenenKod;
	}

	public void setUcretiOdenenKod(Boolean ucretiOdenenKod) {
		this.ucretiOdenenKod = ucretiOdenenKod;
	}

	public Tanim getTesis() {
		return tesis;
	}

	public void setTesis(Tanim tesis) {
		this.tesis = tesis;
	}

	public Double getAksamCalismaSaati() {
		return aksamCalismaSaati;
	}

	public void setAksamCalismaSaati(Double aksamCalismaSaati) {
		this.aksamCalismaSaati = aksamCalismaSaati;
	}

	public Double getAksamCalismaSaatiYuzde() {
		return aksamCalismaSaatiYuzde;
	}

	public void setAksamCalismaSaatiYuzde(Double aksamCalismaSaatiYuzde) {
		this.aksamCalismaSaatiYuzde = aksamCalismaSaatiYuzde;
	}

	public TreeMap<String, Boolean> getBaslikMap() {
		return baslikMap;
	}

	public void setBaslikMap(TreeMap<String, Boolean> baslikMap) {
		this.baslikMap = baslikMap;
	}

	public List<Tanim> getDenklestirmeDinamikAlanlar() {
		return denklestirmeDinamikAlanlar;
	}

	public void setDenklestirmeDinamikAlanlar(List<Tanim> denklestirmeDinamikAlanlar) {
		this.denklestirmeDinamikAlanlar = denklestirmeDinamikAlanlar;
	}

	public Boolean getHataliPuantajVar() {
		return hataliPuantajVar;
	}

	public void setHataliPuantajVar(Boolean hataliPuantajVar) {
		this.hataliPuantajVar = hataliPuantajVar;
	}

}
