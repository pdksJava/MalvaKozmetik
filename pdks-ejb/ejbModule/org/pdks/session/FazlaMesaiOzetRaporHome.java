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
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;
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
import org.pdks.entity.PersonelDenklestirmeTasiyici;
import org.pdks.entity.PersonelFazlaMesai;
import org.pdks.entity.PersonelHareketIslem;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGorev;
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

	private VardiyaGun vardiyaGun;

	private Sirket sirket;

	private DenklestirmeAy denklestirmeAy;

	private TreeMap<String, Tatil> tatilGunleriMap;

	private Boolean hataYok, fazlaMesaiIzinKullan = Boolean.FALSE, yetkili = Boolean.FALSE, resmiTatilVar = Boolean.FALSE, haftaTatilVar = Boolean.FALSE, kaydetDurum = Boolean.FALSE;
	private Boolean sutIzniGoster = Boolean.FALSE, partTimeGoster = Boolean.FALSE, onayla, hastaneSuperVisor = Boolean.FALSE, sirketIzinGirisDurum = Boolean.FALSE;

	private Boolean aksamGun = Boolean.FALSE, aksamSaat = Boolean.FALSE, hataliPuantajGoster = Boolean.FALSE, stajerSirket, departmanBolumAyni = Boolean.FALSE;
	private Boolean modelGoster = Boolean.FALSE, kullaniciPersonel = Boolean.FALSE, denklestirmeAyDurum = Boolean.FALSE;
	private boolean adminRole, ikRole;

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
	private Tanim gorevYeri, seciliBolum;

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
	private Integer aksamVardiyaBasSaat, aksamVardiyaBitSaat, aksamVardiyaBasDakika, aksamVardiyaBitDakika;
	private List<YemekIzin> yemekList;
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
			setSirket(null);
			sirketId = null;
			setTesisId(null);
			setTesisList(null);
			aylar = PdksUtil.getAyListesi(Boolean.TRUE);
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
				List<String> sayfalar = !superVisorHemsireSayfalari.equals("") ? PdksUtil.getListByString(superVisorHemsireSayfalari, null) : null;
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

	public String departmanDegisti(boolean degisti) {
		if (degisti) {
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
				if (tesisId != null || seciliEkSaha3Id != null || (sirket != null && sirket.isDepartmanBolumAynisi()))
					bolumDoldur();
		}
		return "";
	}

	private void fillSirketList() {
		if (adminRole)
			fillDepartmanList();
		List<SelectItem> sirketler = null;

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

		gorevYeriList = null;
		bolumDepartmanlari = null;
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
		if (sicilNo.trim().equals(""))
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
			PersonelDenklestirme personelDenklestirmeAylik = puantaj.getPersonelDenklestirmeAylik();
			if (puantaj.isDonemBitti() && personelDenklestirmeAylik.isOnaylandi() && personelDenklestirmeAylik.getDurum() && puantaj.isFazlaMesaiHesapla() && !personelDenklestirmeAylik.isErpAktarildi())
				puantaj.setKaydet(kaydetDurum);
			else
				puantaj.setKaydet(Boolean.FALSE);

		}
		return "";
	}

	/**
	 * @param aylikPuantajSablon
	 * @param denklestirmeDonemi
	 */
	public void fillFazlaMesaiOzetRaporDevam(AylikPuantaj aylikPuantajSablon, DepartmanDenklestirmeDonemi denklestirmeDonemi) {
		fazlaMesaiVardiyaGun = null;
		Map<String, String> map1 = null;
		sanalPersonelAciklama = ortakIslemler.sanalPersonelAciklama();
		sabahVardiya = null;
		departmanBolumAyni = Boolean.FALSE;
		aksamGun = Boolean.FALSE;
		aksamSaat = Boolean.FALSE;
		haftaTatilVar = Boolean.FALSE;
		fazlaMesaiIzinKullan = Boolean.FALSE;
		sirketIzinGirisDurum = Boolean.FALSE;
		yemekList = null;
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

		if ((authenticatedUser.isIK() || authenticatedUser.isAdmin()) && sicilNo != null && sicilNo.trim().length() > 0)
			lastMap.put("sicilNo", sicilNo.trim());
		try {

			map1 = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception e) {

		}
		departmanBolumAyni = sirket != null && sirket.isDepartmanBolumAynisi();
		adres = map1.containsKey("host") ? map1.get("host") : "";
		if (sicilNo != null)
			sicilNo = sicilNo.trim();
		hataYok = Boolean.FALSE;
		sutIzniGoster = Boolean.FALSE;
		partTimeGoster = Boolean.FALSE;
		aylikPuantajSablon.getVardiyalar();
		setAylikPuantajDefault(aylikPuantajSablon);
		List<AylikPuantaj> puantajList = new ArrayList();
		kaydetDurum = Boolean.FALSE;
		String aksamBordroBasZamani = ortakIslemler.getParameterKey("aksamBordroBasZamani"), aksamBordroBitZamani = ortakIslemler.getParameterKey("aksamBordroBitZamani");
		Integer[] basZaman = ortakIslemler.getSaatDakika(aksamBordroBasZamani), bitZaman = ortakIslemler.getSaatDakika(aksamBordroBitZamani);
		aksamVardiyaBasSaat = basZaman[0];
		aksamVardiyaBasDakika = basZaman[1];
		aksamVardiyaBitSaat = bitZaman[0];
		aksamVardiyaBitDakika = bitZaman[1];

		DenklestirmeAy gecenAy = null;
		try {
			seciliBolum = null;

			setVardiyaGun(null);
			HashMap map = new HashMap();
			List<String> perList = null;
			List<String> list1 = ortakIslemler.getYetkiTumPersonelNoList();

			HashMap fields = new HashMap();
			HashMap<String, Personel> ayrilanPersonelMap = new HashMap<String, Personel>();
			if (hastaneSuperVisor)
				fields.put("ekSaha1.id=", authenticatedUser.getPdksPersonel().getEkSaha1().getId());
			sicilNo = ortakIslemler.getSicilNo(sicilNo);
			if (departmanBolumAyni == false && tesisId != null && tesisId > 0)
				fields.put("tesis.id=", tesisId);

			if (seciliEkSaha3Id != null && seciliEkSaha3Id > 0) {
				fields.put("ekSaha3.id=", seciliEkSaha3Id);

				HashMap parametreMap = new HashMap();

				parametreMap.put("id", seciliEkSaha3Id);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				seciliBolum = (Tanim) pdksEntityController.getObjectByInnerObject(parametreMap, Tanim.class);

			}
			if (gorevTipiId != null && gorevTipiId > 0)
				fields.put("gorevTipi.id=", gorevTipiId);

			fields.put("sskCikisTarihi>=", aylikPuantajSablon.getIlkGun());
			fields.put("sskCikisTarihi<=", aylikPuantajSablon.getSonGun());
			if (departmanBolumAyni == false && authenticatedUser.isIK() && !authenticatedUser.isYoneticiKontratli())
				fields.put("sirket.departman.id=", authenticatedUser.getDepartman().getId());
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Personel> ayrilanlar = pdksEntityController.getObjectByInnerObjectListInLogic(fields, Personel.class);
			List<String> superVisorList = null;
			if (authenticatedUser.isSuperVisor()) {
				superVisorList = new ArrayList<String>();
				for (Personel personel : authenticatedUser.getTumPersoneller()) {
					String sicil = personel.getSicilNo();
					if (sicil == null || sicil.trim().equals(""))
						continue;
					superVisorList.add(sicil);
				}

			}
			for (Personel personel : ayrilanlar) {
				boolean ekle = authenticatedUser.isAdmin() || authenticatedUser.isIK();
				if (!ekle) {
					if (personel.getPdksYonetici() != null && personel.getPdksYonetici().getId().equals(authenticatedUser.getPdksPersonel().getId()))
						ekle = Boolean.TRUE;

				}
				Date cikisTarihi = personel.getSonCalismaTarihi();
				if (ekle && (cikisTarihi.getTime() >= aylikPuantajSablon.getIlkGun().getTime() && cikisTarihi.getTime() <= aylikPuantajSablon.getSonGun().getTime()))
					ayrilanPersonelMap.put(personel.getPdksSicilNo(), personel);

			}
			if (!ayrilanPersonelMap.isEmpty())
				list1.addAll(new ArrayList(ayrilanPersonelMap.keySet()));
			boolean pdksHaric = Boolean.FALSE;
			if (authenticatedUser.getDepartman().isAdminMi() == false && (authenticatedUser.isSuperVisor() || authenticatedUser.isProjeMuduru())) {
				pdksHaric = Boolean.TRUE;
				sirket = authenticatedUser.getPdksPersonel().getSirket();
			}
			if (sirketId != null && (authenticatedUser.isIK() || authenticatedUser.isAdmin())) {
				HashMap parametreMap = new HashMap();
				parametreMap.put("id", sirketId);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				sirket = (Sirket) pdksEntityController.getObjectByInnerObject(parametreMap, Sirket.class);

			}

			if (sirket != null)
				departmanBolumAyni = sirket.isDepartmanBolumAynisi();

			if (pdksHaric || authenticatedUser.isAdmin() || authenticatedUser.isIK() || (sicilNo != null && sicilNo.trim().length() > 0)) {
				if (sicilNo == null || sicilNo.trim().length() == 0) {
					map.put(PdksEntityController.MAP_KEY_SELECT, "pdksSicilNo");
					if (sirket != null) {
						if (!sirket.getDepartman().isAdminMi()) {
							if (seciliEkSaha3Id != null)
								map.put("ekSaha3.id=", seciliEkSaha3Id);
							if (departmanBolumAyni == false && tesisId != null && tesisId > 0)
								map.put("tesis.id=", tesisId);
						}
						if (departmanBolumAyni == false)
							map.put("sirket.id=", sirket.getId());

					}

					map.put("iseBaslamaTarihi<=", denklestirmeDonemi.getBitisTarih());

					map.put("sskCikisTarihi>=", denklestirmeDonemi.getBaslangicTarih());
					if (hastaneSuperVisor)
						map.put("ekSaha1.id=", authenticatedUser.getPdksPersonel().getEkSaha1().getId());
					else if (!(authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()))
						map.put("pdksSicilNo", list1);
					if (sirket.getDepartman() != null && !sirket.getDepartman().isAdminMi()) {
						if (seciliEkSaha3Id != null)
							map.put("ekSaha3.id=", seciliEkSaha3Id);
						if (tesisId != null && tesisId > 0)
							map.put("tesis.id=", tesisId);
					}

					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					perList = pdksEntityController.getObjectByInnerObjectListInLogic(map, Personel.class);

				} else {
					perList = new ArrayList<String>();
					if (authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || authenticatedUser.isIKAdmin() || list1.contains(sicilNo)) {
						perList.add(sicilNo);

					}
				}

			} else if (!hastaneSuperVisor)
				perList = list1;

			String searchKey = "sirket.id=";
			if (sirket == null)
				if (!authenticatedUser.isIK() && !authenticatedUser.isAdmin())
					sirket = authenticatedUser.getPdksPersonel().getSirket();
			Object value = sirket.getId();
			if (perList != null) {
				searchKey = "pdksSicilNo";
				if (perList.isEmpty())
					perList.add("YOKTUR");

				value = perList;
			}
			map.clear();
			searchKey = "pdksSicilNo";
			map.put(PdksEntityController.MAP_KEY_SELECT, searchKey);
			boolean test = adres.indexOf("localhost:8080") >= 0;
			if (test) {
				// String testSicilNo = "54831089";
				// map.put("pdksSicilNo=", testSicilNo);
				// logger.info("testSicilNo = " + testSicilNo);
			}

			if (seciliEkSaha3Id != null)
				map.put("ekSaha3.id=", seciliEkSaha3Id);
			if (departmanBolumAyni == false && tesisId != null && tesisId > 0)
				map.put("tesis.id=", tesisId);
			if (gorevTipiId != null && gorevTipiId > 0)
				map.put("gorevTipi.id=", gorevTipiId);
			if (hastaneSuperVisor) {
				map.put("ekSaha1.id=", authenticatedUser.getPdksPersonel().getEkSaha1().getId());
			} else
				map.put(searchKey, value);
			map.put("iseBaslamaTarihi<=", denklestirmeDonemi.getBitisTarih());
			if (!authenticatedUser.isIK() && !authenticatedUser.isAdmin()) {

				map.put("sskCikisTarihi>=", denklestirmeDonemi.getBaslangicTarih());
			}
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			perList = pdksEntityController.getObjectByInnerObjectListInLogic(map, Personel.class);
			List<PersonelDenklestirme> personelDenklestirmeler = null;
			if (!perList.isEmpty())
				personelDenklestirmeler = getPdksPersonelDenklestirmeler(perList);
			else
				personelDenklestirmeler = new ArrayList<PersonelDenklestirme>();

			HashMap<String, Personel> gorevliPersonelMap = new HashMap<String, Personel>();
			if (seciliEkSaha3Id != null) {
				List<Long> gorevYerileri = new ArrayList<Long>();
				gorevYerileri.add(seciliEkSaha3Id);

				List<VardiyaGorev> gorevliler = departman == null || departman.isAdminMi() ? ortakIslemler.getVardiyaGorevYerleri(authenticatedUser, aylikPuantajSablon.getIlkGun(), aylikPuantajSablon.getSonGun(), gorevYerileri, session) : new ArrayList<VardiyaGorev>();
				for (VardiyaGorev vardiyaGorev : gorevliler) {
					Personel personel = vardiyaGorev.getVardiyaGun().getPersonel();
					if (personel.getPdksSicilNo() != null && personel.getPdksSicilNo().trim().length() > 0)
						gorevliPersonelMap.put(personel.getPdksSicilNo().trim(), personel);
				}

				if (!gorevliPersonelMap.isEmpty()) {
					List<PersonelDenklestirme> personelHelpDenklestirmeler = getPdksPersonelDenklestirmeler(new ArrayList(gorevliPersonelMap.keySet()));
					if (!personelHelpDenklestirmeler.isEmpty())
						personelDenklestirmeler.addAll(personelHelpDenklestirmeler);
				}

			}

			HashMap<Long, PersonelDenklestirme> personelDenklestirmeMap = new HashMap<Long, PersonelDenklestirme>();
			TreeMap<Long, PersonelDenklestirme> personelDenklestirmeDonemMap = new TreeMap<Long, PersonelDenklestirme>();
			if (personelDenklestirmeler.isEmpty()) {
				perList.clear();
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
				if (personelDenklestirme.isDenklestirme()) {
					personelDenklestirmeMap.put(personelDenklestirme.getPersonelId(), personelDenklestirme);
					perList.add(personelDenklestirme.getPersonel().getPdksSicilNo());
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

			if (!perList.isEmpty()) {
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
				if (!sabahVardiyaKisaAdlari.equals(""))
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
				value = perList;
				map.clear();
				setInstance(denklestirmeDonemi);
				tatilGunleriMap = ortakIslemler.getTatilGunleri(null, PdksUtil.tariheGunEkleCikar(denklestirmeDonemi.getBaslangicTarih(), -1), PdksUtil.tariheGunEkleCikar(denklestirmeDonemi.getBitisTarih(), 1), session);
				List<PersonelDenklestirmeTasiyici> list = null;
				try {
					denklestirmeDonemi.setPersonelDenklestirmeDonemMap(personelDenklestirmeDonemMap);
					list = new ArrayList<PersonelDenklestirmeTasiyici>();
					for (PersonelDenklestirme personelDenklestirme : personelDenklestirmeler) {
						PersonelDenklestirmeTasiyici denklestirmeTasiyici = new PersonelDenklestirmeTasiyici();
						denklestirmeTasiyici.setPersonel(personelDenklestirme.getPersonel());
						denklestirmeTasiyici.setCalismaModeli(personelDenklestirme.getCalismaModeliAy().getCalismaModeli());
						denklestirmeTasiyici.setDenklestirmeAy(denklestirmeAy);
						list.add(denklestirmeTasiyici);
					}
					ortakIslemler.personelDenklestirmeDuzenle(list, aylikPuantajDefault, tatilGunleriMap, session);
				} catch (Exception ex) {
					list = new ArrayList<PersonelDenklestirmeTasiyici>();
					logger.equals(ex);
					ex.printStackTrace();
				}
				if (list.size() > 1) {
					list = PdksUtil.sortObjectStringAlanList(list, "getAdSoyad", null);
					if (seciliEkSaha3Id == null) {
						List<Tanim> bolumList = new ArrayList<Tanim>();
						HashMap<Long, List<PersonelDenklestirmeTasiyici>> map2 = new HashMap<Long, List<PersonelDenklestirmeTasiyici>>();
						for (Iterator iterator = list.iterator(); iterator.hasNext();) {
							PersonelDenklestirmeTasiyici personelDenklestirmeTasiyici = (PersonelDenklestirmeTasiyici) iterator.next();
							Personel personel = personelDenklestirmeTasiyici.getPersonel();
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

				boolean renk = Boolean.FALSE;
				aylikPuantajSablon = fazlaMesaiOrtakIslemler.getAylikPuantaj(ay, yil, denklestirmeDonemi, session);

				List<VardiyaHafta> vardiyaHaftaList = new ArrayList<VardiyaHafta>();
				fazlaMesaiOrtakIslemler.haftalikVardiyaOlustur(vardiyaHaftaList, aylikPuantajSablon, denklestirmeDonemi, tatilGunleriMap, null);
				resmiTatilVar = Boolean.FALSE;
				haftaTatilVar = Boolean.FALSE;

				linkAdres = "<a href='http://"
						+ adres
						+ "/fazlaMesaiHesapla?linkAdresKey="
						+ PdksUtil.getEncodeStringByBase64("yil=" + yil + "&ay=" + ay + (seciliEkSaha3Id != null ? "&gorevYeriId=" + seciliEkSaha3Id : "") + (tesisId != null ? "&tesisId=" + tesisId : "") + (gorevTipiId != null ? "&gorevTipiId=" + gorevTipiId : "")
								+ (sirket != null ? "&sirketId=" + sirket.getId() : "") + (sicilNo != null && sicilNo.trim().length() > 0 ? "&sicilNo=" + sicilNo.trim() : "")) + "'>" + ortakIslemler.getCalistiMenuAdi("fazlaMesaiOzetRapor") + " Ekranına Geri Dön</a>";

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
				if (msgError.equals(""))
					msgError = "msgerror.png";
				msgFazlaMesaiError = ortakIslemler.getParameterKey("msgFazlaMesaiErrorResim");
				if (msgFazlaMesaiError.equals(""))
					msgFazlaMesaiError = "msgerror.png";
				List<Long> vgIdList = new ArrayList<Long>();
				ayrikHareketVar = false;
				String str = ortakIslemler.getParameterKey("addManuelGirisCikisHareketler");
				boolean ayrikKontrol = false;
				if (sicilNo != null && sicilNo.trim().length() > 0) {
					ayrikKontrol = str.equals("A") || str.equals("1");
					if (!ayrikKontrol) {
						if (authenticatedUser.isAdmin())
							ayrikKontrol = str.equalsIgnoreCase("I") || str.equalsIgnoreCase("S");
						else if (authenticatedUser.isIK())
							ayrikKontrol = str.equalsIgnoreCase("I");

					}
				}
				List<AylikPuantaj> puantajDenklestirmeList = new ArrayList<AylikPuantaj>();
				for (Iterator iterator1 = list.iterator(); iterator1.hasNext();) {
					PersonelDenklestirmeTasiyici denklestirme = (PersonelDenklestirmeTasiyici) iterator1.next();
					AylikPuantaj puantaj = (AylikPuantaj) aylikPuantajSablon.clone();
					PersonelDenklestirme valueBuAy = personelDenklestirmeMap.get(denklestirme.getPersonel().getId());
					puantaj.setPersonelDenklestirmeAylik(valueBuAy);
					if (valueBuAy != null)
						puantaj.setPersonelDenklestirmeGecenAy(valueBuAy.getPersonelDenklestirmeGecenAy());
					if (puantaj.getPersonelDenklestirmeAylik() == null || !puantaj.getPersonelDenklestirmeAylik().isDenklestirme()) {
						iterator1.remove();
						continue;
					}
					puantaj.setPersonelDenklestirme(denklestirme);
					puantaj.setPdksPersonel(denklestirme.getPersonel());
					puantaj.setVardiyalar(denklestirme.getVardiyalar());
					// personelDenklestirme.setPlanlanSure(puantaj.getPlanlananSure());
					// personelDenklestirme.setHesaplananSure(puantaj.getSaatToplami());
					puantajDenklestirmeList.add(puantaj);
				}
				String yoneticiPuantajKontrolStr = ortakIslemler.getParameterKey("yoneticiPuantajKontrol");
				boolean yoneticiKontrolEtme = authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || yoneticiPuantajKontrolStr.equals("");
				ortakIslemler.yoneticiPuantajKontrol(puantajDenklestirmeList, Boolean.TRUE, session);

				aksamCalismaSaati = null;
				aksamCalismaSaatiYuzde = null;
				try {
					if (!ortakIslemler.getParameterKey("aksamCalismaSaatiYuzde").equals(""))
						aksamCalismaSaatiYuzde = Double.parseDouble(ortakIslemler.getParameterKey("aksamCalismaSaatiYuzde"));

				} catch (Exception e) {
				}
				if (aksamCalismaSaatiYuzde != null && (aksamCalismaSaatiYuzde.doubleValue() < 0.0d || aksamCalismaSaatiYuzde.doubleValue() > 100.0d))
					aksamCalismaSaatiYuzde = null;
				try {
					if (!ortakIslemler.getParameterKey("aksamCalismaSaati").equals(""))
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
					puantaj.setVgMap(vgMap);
					puantaj.setDonemBitti(Boolean.TRUE);
					puantaj.setAyrikHareketVar(false);
					puantaj.setFiiliHesapla(true);
					saveList.clear();
					Personel personel = puantaj.getPdksPersonel();

					perCalismaModeli = personel.getCalismaModeli();
					if (puantaj.getPersonelDenklestirmeAylik() != null && puantaj.getPersonelDenklestirmeAylik().getCalismaModeliAy() != null)
						perCalismaModeli = puantaj.getPersonelDenklestirmeAylik().getCalismaModeliAy().getCalismaModeli();
					Date sonPersonelCikisZamani = null;

					Boolean gebemi = Boolean.FALSE, calisiyor = Boolean.FALSE;
					puantaj.setKaydet(Boolean.FALSE);
					personelFazlaMesaiStr = personelFazlaMesaiOrjStr + (personel.getPdks() ? " " : "(Fazla Mesai Yok)");

					puantaj.setSablonAylikPuantaj(aylikPuantajSablon);
					puantaj.setFazlaMesaiHesapla(Boolean.FALSE);

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
					boolean puantajFazlaMesaiHesapla = true;
					if (puantaj.getVardiyalar() != null) {
						VardiyaGun vardiyaGunSon = null;
						for (Iterator iterator = puantaj.getVardiyalar().iterator(); iterator.hasNext();) {
							VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();

							vardiyaGun.setAyinGunu(gunList.contains(vardiyaGun.getVardiyaDateStr()));
							if (!vardiyaGun.isAyinGunu()) {
								iterator.remove();
								continue;
							}
							String key = vardiyaGun.getVardiyaDateStr();
							if (key.equals("20211110"))
								logger.debug(vardiyaGun.getVardiyaKeyStr());
							if (vardiyaGun.getId() != null) {
								if (vardiyaGun.getVardiya().isCalisma())
									vardiyaGunSon = vardiyaGun;
								vgIdList.add(vardiyaGun.getId());
								vgMap.put(vardiyaGun.getVardiyaDateStr(), vardiyaGun);
								if (vardiyaGun.getPersonel().isCalisiyorGun(vardiyaGun.getVardiyaDate()))
									vardiyaGun.setZamanGelmedi(!bugun.after(vardiyaGun.getIslemVardiya().getVardiyaTelorans2BitZaman()));

							}
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
								if (fazlaMesaiHesapla) {
									VardiyaSaat vardiyaSaatDB = vardiyaGun.getVardiyaSaatDB();
									if (vardiyaSaatDB.getResmiTatilSure() > 0.0d)
										vardiyaGun.setResmiTatilSure(vardiyaSaatDB.getResmiTatilSure());
									else if (vardiyaGun.getVardiya().isHaftaTatil()) {
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
						if (vardiyaGunSon != null)
							ayBitti = bugun.after(vardiyaGunSon.getIslemVardiya().getVardiyaTelorans1BitZaman());

					}

					if (offSure != null)
						puantaj.setOffSure(offSure);

					if (!haftaTatilVar)
						haftaTatilVar = puantaj.getHaftaCalismaSuresi() != 0.0d;
					if (!resmiTatilVar)
						resmiTatilVar = puantaj.getResmiTatilToplami() != 0.0d;
					ortakIslemler.puantajHaftalikPlanOlustur(Boolean.TRUE, null, vardiyalar, aylikPuantajSablon, puantaj);
					PersonelDenklestirme personelDenklestirme = puantaj.getPersonelDenklestirmeAylik();
					if (personelDenklestirme == null)
						continue;

					// puantaj.setSaatToplami(personelDenklestirme.getHesaplananSure());
					puantaj.setPlanlananSure(personelDenklestirme.getPlanlanSure());
					personelDenklestirme.setGuncellendi(Boolean.FALSE);
					PersonelDenklestirme hesaplananDenklestirmeHesaplanan = null;

					for (Iterator iterator = puantaj.getVardiyalar().iterator(); iterator.hasNext();) {
						VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
						if (!vardiyaGun.isAyinGunu()) {
							iterator.remove();
						} else {
							if (!calisiyor)
								calisiyor = vardiyaGun.getVardiya() != null;
							if (!gebemi && vardiyaGun.getVardiya() != null)
								gebemi = vardiyaGun.getVardiya().isGebelikMi();
							if (calisiyor) {

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
						puantaj.setOdenenSure(personelDenklestirme.getOdenecekSure());
						puantaj.setSaatToplami(personelDenklestirme.getHesaplananSure());
						puantajFazlaMesaiHesapla = personelDenklestirme.getDurum();
					} else if (hesaplananDenklestirmeHesaplanan != null) {
						puantaj.setOdenenSure(hesaplananDenklestirmeHesaplanan.getOdenecekSure());
						puantaj.setSaatToplami(puantajSaatToplami);
						puantaj.setDevredenSure(hesaplananDenklestirmeHesaplanan.getDevredenSure());
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
					if (!aksamGun)
						aksamGun = puantaj.getAksamVardiyaSayisi() != 0;
					if (!aksamSaat)
						aksamSaat = puantaj.getAksamVardiyaSaatSayisi() != 0.0d;
					if (!haftaTatilVar)
						haftaTatilVar = puantaj.getHaftaCalismaSuresi() != 0.0d;
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
					if (denklestirmeAyDurum && yoneticiKontrolEtme == false) {
						if (personel.isSanalPersonelMi() == false && (puantaj.getYonetici() == null || puantaj.getYonetici().getId() == null)) {
							puantaj.setFazlaMesaiHesapla(false);
						}
					}

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

		} catch (InvalidStateException e) {
			InvalidValue[] invalidValues = e.getInvalidValues();
			if (invalidValues != null) {
				for (InvalidValue invalidValue : invalidValues) {
					Object object = invalidValue.getBean();
					if (object != null && object instanceof VardiyaGun) {
						VardiyaGun vardiyaGun = (VardiyaGun) object;
						PdksUtil.addMessageAvailableWarn(PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), PdksUtil.getDateFormat()) + " günü  alanı : " + invalidValue.getPropertyName() + " with message: " + invalidValue.getMessage());
					} else
						PdksUtil.addMessageAvailableWarn("Instance of bean class: " + invalidValue.getBeanClass().getSimpleName() + " has an invalid property: " + invalidValue.getPropertyName() + " with message: " + invalidValue.getMessage());
				}
			}
			logger.error(e);
			e.printStackTrace();

		} catch (Exception e3) {
			logger.error("Pdks hata in : \n");
			e3.printStackTrace();
			logger.error("Pdks hata out : " + e3.getMessage());

		} finally {

		}
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
			Date tarihLast = PdksUtil.tariheGunEkleCikar(denklestirmeAy.getOtomatikOnayIKTarih(), 10);
			cal = Calendar.getInstance();
			Date toDay = cal.getTime();
			if (toDay.after(tarih) && (toDay.before(denklestirmeAy.getOtomatikOnayIKTarih())) || (authenticatedUser.isTestLogin() && toDay.before(tarihLast))) {
				onayla = Boolean.FALSE;
				for (AylikPuantaj puantaj : puantajList) {
					puantaj.setKaydet(puantaj.getPersonelDenklestirmeAylik().getDurum());
					if (puantaj.isKaydet())
						onayla = hataYok;
				}

			}
		}

	}

	/**
	 * @param hareketler
	 */
	private void manuelHareketSil(List<HareketKGS> hareketler) {
		if (hareketler != null) {
			for (Iterator iterator = hareketler.iterator(); iterator.hasNext();) {
				HareketKGS hareketKGS = (HareketKGS) iterator.next();
				if (hareketKGS.getId() != null && hareketKGS.getId().startsWith(HareketKGS.AYRIK_HAREKET))
					iterator.remove();
			}
		}

	}

	/**
	 * @return
	 */
	public String ayrikKayitlariOlustur() {
		List<AylikPuantaj> list = new ArrayList<AylikPuantaj>(aylikPuantajList);
		boolean devam = false;
		for (AylikPuantaj aylikPuantaj : list) {
			if (aylikPuantaj.isAyrikHareketVar() == false)
				continue;
			List<VardiyaGun> vardiyaList = new ArrayList<VardiyaGun>(aylikPuantaj.getVardiyalar());
			int ayrikVar = 0;
			for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
				VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
				if (vardiyaGun.isAyinGunu() && vardiyaGun.getId() != null) {
					if (vardiyaGun.isAyrikHareketVar()) {
						manuelHareketSil(vardiyaGun.getGirisHareketleri());
						manuelHareketSil(vardiyaGun.getCikisHareketleri());
						manuelHareketSil(vardiyaGun.getHareketler());
						++ayrikVar;
					} else if (ayrikVar == 0)
						iterator.remove();
				} else
					iterator.remove();

			}
			if (ayrikVar > 1) {
				try {
					ortakIslemler.addManuelGirisCikisHareketler(vardiyaList, true, null, session);
					devam = true;
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}

			}
			vardiyaList = null;
		}
		list = null;
		if (devam)
			fillFazlaMesaiOzetRaporList();
		return "";
	}

	/**
	 * @param islemPuantaj
	 * @param vardiyaGun
	 * @param paramsMap
	 */
	public void vardiyaGunKontrol(AylikPuantaj islemPuantaj, VardiyaGun vardiyaGun, HashMap<String, Object> paramsMap) {
		Date aksamVardiyaBitisZamani = null, aksamVardiyaBaslangicZamani = null;
		Vardiya vardiya = vardiyaGun.getVardiya();
		String key1 = vardiyaGun.getVardiyaDateStr(), vardiyaKey = vardiyaGun.getVardiyaKeyStr();
		// boolean aksamVardiyasi = vardiya.isAksamVardiyasi();
		if (perCalismaModeli != null && perCalismaModeli.getGeceCalismaOdemeVar().equals(Boolean.TRUE)) {
			if (aksamVardiyaBitSaat != null && aksamVardiyaBitDakika != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(vardiyaGun.getVardiyaDate());
				cal.set(Calendar.HOUR_OF_DAY, aksamVardiyaBitSaat);
				cal.set(Calendar.MINUTE, aksamVardiyaBitDakika);
				if (vardiya.getBasSaat() > vardiya.getBitSaat() && vardiya.isCalisma())
					cal.add(Calendar.DATE, 1);
				aksamVardiyaBitisZamani = cal.getTime();
			}
			if (aksamVardiyaBasSaat != null && aksamVardiyaBasDakika != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(vardiyaGun.getVardiyaDate());
				cal.set(Calendar.HOUR_OF_DAY, aksamVardiyaBasSaat);
				cal.set(Calendar.MINUTE, aksamVardiyaBasDakika);
				if (vardiya.getBasSaat() < vardiya.getBitSaat() || vardiya.isCalisma() == false)
					cal.add(Calendar.DATE, -1);
				aksamVardiyaBaslangicZamani = cal.getTime();
			}
		}

		List<HareketKGS> girisHareketleri = null, cikisHareketleri = null, hareketler = null;
		vardiyaGun.setFazlaMesaiOnayla(null);
		boolean hareketsiz = vardiyaGun.getVardiya() != null && vardiyaGun.getHareketler() == null && vardiyaGun.getIzin() == null;
		if (vardiyaGun.getGirisHareketleri() != null)
			girisHareketleri = new ArrayList(vardiyaGun.getGirisHareketleri());
		if (vardiyaGun.getCikisHareketleri() != null)
			cikisHareketleri = new ArrayList(vardiyaGun.getCikisHareketleri());
		hareketler = vardiyaGun.getHareketler();
		islemPuantaj.setAyrikHareketVar(true);
		if (islemPuantaj != null && hareketler != null && !hareketler.isEmpty()) {
			ArrayList<HareketKGS> tumHareketler = islemPuantaj.getHareketler() != null ? islemPuantaj.getHareketler() : new ArrayList<HareketKGS>();
			if (tumHareketler.isEmpty())
				islemPuantaj.setHareketler(tumHareketler);
			tumHareketler.addAll(hareketler);
		}

		// if (haret)

		boolean fazlaMesaiOnaylaDurum = vardiyaGun.isAyrikHareketVar() == false && girisHareketleri != null && cikisHareketleri != null && girisHareketleri.size() == cikisHareketleri.size();
		if (key1.equals("20210903"))
			logger.debug(vardiyaKey);

		if (fazlaMesaiOnaylaDurum || (vardiyaGun.isAyrikHareketVar() && vardiyaGun.getVardiya().isCalisma())) {
			for (Iterator iterator = girisHareketleri.iterator(); iterator.hasNext();) {
				HareketKGS hareketKGS = (HareketKGS) iterator.next();
				if (hareketKGS.getId() == null || hareketKGS.getId().startsWith("V") || hareketKGS.getId().startsWith("M"))
					iterator.remove();
			}
			for (Iterator iterator = cikisHareketleri.iterator(); iterator.hasNext();) {
				HareketKGS hareketKGS = (HareketKGS) iterator.next();
				if (hareketKGS.getId() == null || hareketKGS.getId().startsWith("V") || hareketKGS.getId().startsWith("M"))
					iterator.remove();
			}
			boolean cikis = false;
			int adet = 0;

			for (HareketKGS hareketKGS : hareketler) {
				try {
					if (cikis && hareketKGS.getId() != null && hareketKGS.getKapiView().getKapi().isCikisKapi())
						++adet;
				} catch (Exception e) {
				}
				cikis = !cikis;
			}
			if (adet > 0 && adet == cikisHareketleri.size()) {
				fazlaMesaiOnaylaDurum = true;
				vardiyaGun.setFazlaMesaiOnayla(true);
			}
			if (vardiyaGun.isAyrikHareketVar())
				vardiyaGun.setHareketHatali(girisHareketleri.size() != cikisHareketleri.size());

		}

		Boolean fazlaMesaiHesapla = (Boolean) paramsMap.get("fazlaMesaiHesapla");
		Integer aksamVardiyaSayisi = (Integer) paramsMap.get("aksamVardiyaSayisi");
		Double aksamVardiyaSaatSayisi = (Double) paramsMap.get("aksamVardiyaSaatSayisi");
		Double sabahAksamCikisSaatSayisi = (Double) paramsMap.get("sabahAksamCikisSaatSayisi");
		Double resmiTatilSuresi = (Double) paramsMap.get("resmiTatilSuresi");
		Double haftaCalismaSuresi = (Double) paramsMap.get("haftaCalismaSuresi");
		String izinERPUpdateStr = ortakIslemler.getParameterKey("izinERPUpdate");
		Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
		Tatil tatil = vardiyaGun.getTatil() != null ? vardiyaGun.getTatil().getOrjTatil() : null;
		PersonelIzin izin = vardiyaGun.getIzin();
		Personel personel = vardiyaGun.getPersonel();
		vardiyaGun.setAksamVardiyaSaatSayisi(0d);
		vardiyaGun.setLinkAdresler(null);
		String vardiyaPlanKey = vardiyaGun.getPlanKey();
		boolean izinDurum = false;
		if (hareketler == null && vardiyaGun.getIslemVardiya() != null) {
			vardiyaGun.setHataliDurum(vardiyaGun.getIzin() == null && vardiyaGun.getIslemVardiya().isCalisma());
			izinDurum = sirketIzinGirisDurum && vardiyaGun.getIslemVardiya().isCalisma() && vardiyaGun.isZamanGelmedi() == false && vardiyaGun.getIzin() == null;
		}
		if (vardiyaGun.isZamanGelmedi() && islemVardiya != null && islemVardiya.isCalisma() && islemVardiya.getVardiyaBitZaman().after(islemVardiya.getVardiyaFazlaMesaiBitZaman())) {
			// fazlaMesaiHesapla = Boolean.FALSE;
			vardiyaGun.setHataliDurum(Boolean.TRUE);
			String link = "<a href='http://" + adres + "/vardiyaPlani?planKey=" + vardiyaPlanKey + "'>" + vardiyaPlaniStr + "</a>";
			vardiyaGun.addLinkAdresler(link);
		} else if (vardiyaGun.getHareketDurum()) {
			try {
				if (vardiyaGun.isAyrikHareketVar())

					if ((izin != null || !vardiyaGun.getVardiya().isCalisma() || vardiyaGun.getVardiya().isHaftaTatil()) && vardiyaGun.getHareketler() != null && !vardiyaGun.getHareketler().isEmpty()) {
						PersonelFazlaMesai personelFazlaMesaiGiris = null;
						if (vardiyaGun.getGirisHareketleri() != null && !vardiyaGun.getGirisHareketleri().isEmpty()) {
							for (HareketKGS hareket : vardiyaGun.getGirisHareketleri()) {
								personelFazlaMesaiGiris = hareket.getPersonelFazlaMesai();
								if (personelFazlaMesaiGiris == null) {
									fazlaMesaiHesapla = Boolean.FALSE;
									vardiyaGun.setOnayli(Boolean.FALSE);
									break;
								}
							}

						}
						String link = null;
						if (personelFazlaMesaiGiris == null) {
							vardiyaGun.setHareketHatali(Boolean.TRUE);
							kapiGirisGetir(vardiyaGun, vardiyaPlanKey);
							if (izin != null && !izinERPUpdateStr.equals("1")) {
								link = "<a href='http://" + adres + "/personelIzinGirisi?izinKey="
										+ PdksUtil.getEncodeStringByBase64("perId=" + personel.getId() + "&tarih1=" + PdksUtil.convertToDateString(izin.getBaslangicZamani(), "yyyyMMdd") + "&tarih2=" + PdksUtil.convertToDateString(izin.getBitisZamani(), "yyyyMMdd")) + "'>" + personelIzinGirisiStr
										+ "</a>";
								vardiyaGun.addLinkAdresler(link);
							}
						}
						personelFazlaMesaiEkle(vardiyaGun, vardiyaPlanKey);

					} else if (vardiyaGun.getVardiya().isCalisma() && izin == null) {
						if (!vardiyaGun.getVardiya().isIcapVardiyasi()) {
							double sure = vardiyaGun.getVardiya().getNetCalismaSuresi();
							boolean sureAz = vardiyaGun.getCalismaSuresi() < sure;
							if (sureAz && tatil != null) {
								if (vardiyaGun.getCikisHareketleri() != null) {
									HareketKGS cikisHareket = vardiyaGun.getCikisHareketleri().get(vardiyaGun.getCikisHareketleri().size() - 1);
									sureAz = !(cikisHareket.getZaman().after(tatil.getBasTarih()) && cikisHareket.getZaman().before(tatil.getBitTarih()));
								}

							}
							if (sureAz) {

								vardiyaGun.setHataliDurum(Boolean.TRUE);
								String link = "<a href='http://" + adres + "/personelIzinGirisi?izinKey=" + PdksUtil.getEncodeStringByBase64("perId=" + personel.getId()) + "'>" + personelIzinGirisiStr + "</a>";
								if (!izinERPUpdateStr.equals("1"))
									vardiyaGun.addLinkAdresler(link);
								kapiGirisGetir(vardiyaGun, vardiyaPlanKey);
								if (vardiyaGun.getGirisHareketleri() != null) {
									HareketKGS girisHareket = vardiyaGun.getGirisHareketleri().get(0);
									HareketKGS cikisHareket = vardiyaGun.getCikisHareketleri().get(0);
									boolean hatali = Boolean.TRUE;
									if (vardiyaGun.getFazlaMesailer() != null) {
										for (PersonelFazlaMesai personelFazlaMesai : vardiyaGun.getFazlaMesailer()) {
											if (personelFazlaMesai.getHareketId().equals(girisHareket.getId()) || personelFazlaMesai.getHareketId().equals(cikisHareket.getId())) {
												hatali = Boolean.FALSE;
											}
										}
									}

									if (hatali && girisHareket.getOrjinalZaman().getTime() < islemVardiya.getVardiyaTelorans1BasZaman().getTime()) {
										personelFazlaMesaiEkle(vardiyaGun, vardiyaPlanKey);
										fazlaMesaiHesapla = Boolean.FALSE;

										vardiyaGun.setHareketHatali(Boolean.TRUE);
										vardiyaGun.setOnayli(Boolean.FALSE);
									}
								}
								if (vardiyaGun.getCikisHareketleri() != null) {
									HareketKGS cikisHareket = vardiyaGun.getCikisHareketleri().get(vardiyaGun.getCikisHareketleri().size() - 1);
									boolean hatali = Boolean.TRUE;
									if (vardiyaGun.getFazlaMesailer() != null) {
										for (PersonelFazlaMesai personelFazlaMesai : vardiyaGun.getFazlaMesailer()) {
											if (personelFazlaMesai.getHareketId().equals(cikisHareket.getId())) {
												hatali = Boolean.FALSE;
											}
										}
									}
									if (hatali && cikisHareket.getOrjinalZaman().getTime() > islemVardiya.getVardiyaTelorans2BitZaman().getTime()) {
										personelFazlaMesaiEkle(vardiyaGun, vardiyaPlanKey);
										fazlaMesaiHesapla = Boolean.FALSE;
										vardiyaGun.setOnayli(Boolean.FALSE);
										vardiyaGun.setHareketHatali(Boolean.TRUE);
									}
								}
								if (vardiyaGun.getHareketler() == null || vardiyaGun.getHareketler().isEmpty())
									vardiyaPlaniGetir(vardiyaGun, vardiyaPlanKey);

							}
							try {
								if (vardiyaGun.getGirisHareketleri() != null && !vardiyaGun.getGirisHareketleri().isEmpty()) {
									HareketKGS girisHareket = vardiyaGun.getGirisHareketleri().get(0);
									HareketKGS cikisHareket = vardiyaGun.getCikisHareketleri().get(0);
									if (girisHareket.getPersonelFazlaMesai() == null && cikisHareket.getPersonelFazlaMesai() == null && (girisHareket.getOrjinalZaman().before(islemVardiya.getVardiyaTelorans1BasZaman()))) {
										vardiyaGun.setHareketHatali(Boolean.TRUE);
										if (vardiyaGun.getLinkAdresler() == null)
											kapiGirisGetir(vardiyaGun, vardiyaPlanKey);

										if (girisHareket.getOrjinalZaman().getTime() < islemVardiya.getVardiyaTelorans1BasZaman().getTime()) {
											personelFazlaMesaiEkle(vardiyaGun, vardiyaPlanKey);
										}
										vardiyaGun.setOnayli(Boolean.FALSE);
										fazlaMesaiHesapla = Boolean.FALSE;
										vardiyaGun.setHataliDurum(Boolean.TRUE);
									}
								}
								if (vardiyaGun.getCikisHareketleri() != null && !vardiyaGun.getCikisHareketleri().isEmpty()) {
									HareketKGS cikisHareket = vardiyaGun.getCikisHareketleri().get(vardiyaGun.getCikisHareketleri().size() - 1);

									if (cikisHareket.getPersonelFazlaMesai() == null && (cikisHareket.getOrjinalZaman().getTime() > islemVardiya.getVardiyaTelorans2BitZaman().getTime() || cikisHareket.getOrjinalZaman().getTime() < islemVardiya.getVardiyaTelorans1BasZaman().getTime())) {
										vardiyaGun.setHareketHatali(Boolean.TRUE);
										if (vardiyaGun.getLinkAdresler() == null)
											kapiGirisGetir(vardiyaGun, vardiyaPlanKey);

										if (cikisHareket.getOrjinalZaman().getTime() > islemVardiya.getVardiyaTelorans2BitZaman().getTime()) {
											personelFazlaMesaiEkle(vardiyaGun, vardiyaPlanKey);
										}
										vardiyaGun.setOnayli(Boolean.FALSE);
										fazlaMesaiHesapla = Boolean.FALSE;
									}
								}

							} catch (Exception e1) {
								e1.printStackTrace();
								logger.error(vardiyaGun.getVardiyaKeyStr() + " " + e1.getMessage());
							}

						} else {
							if (vardiyaGun.getGirisHareketleri() != null) {
								for (Iterator iterator2 = vardiyaGun.getGirisHareketleri().iterator(); iterator2.hasNext();) {
									HareketKGS girisHareket = (HareketKGS) iterator2.next();
									boolean hatali = Boolean.TRUE;
									if (vardiyaGun.getFazlaMesailer() != null) {
										for (PersonelFazlaMesai personelFazlaMesai : vardiyaGun.getFazlaMesailer()) {
											if (personelFazlaMesai.getHareketId().equals(girisHareket.getId())) {
												hatali = Boolean.FALSE;

											}
										}

									}
									if (hatali) {
										personelFazlaMesaiEkle(vardiyaGun, vardiyaPlanKey);
										fazlaMesaiHesapla = Boolean.FALSE;
										vardiyaGun.setHareketHatali(Boolean.TRUE);
										vardiyaGun.setOnayli(Boolean.FALSE);
									}
								}
							}
						}
					}
				if (vardiyaGun.getVardiya() != null && vardiyaGun.getResmiTatilSure() > 0)
					resmiTatilSuresi += vardiyaGun.getResmiTatilSure();

				if (vardiyaGun.getIzin() == null && vardiyaGun.getVardiya() != null && vardiyaGun.getCalismaSuresi() > 0) {
					if (aksamVardiyaBaslangicZamani != null && aksamVardiyaBitisZamani != null) {
						boolean bayramAksamCalismaOde = ortakIslemler.getParameterKey("bayramAksamCalismaOde").equals("1");
						if (vardiyaGun.getGirisHareketleri() != null && vardiyaGun.getCikisHareketleri() != null && vardiyaGun.getGirisHareketleri().size() == vardiyaGun.getCikisHareketleri().size()) {
							double sure = 0;
							for (int i = 0; i < vardiyaGun.getGirisHareketleri().size(); i++) {
								Date cikisZaman = vardiyaGun.getCikisHareketleri().get(i).getZaman();
								Date girisZaman = vardiyaGun.getGirisHareketleri().get(i).getZaman();
								if (girisZaman == null || cikisZaman == null)
									continue;
								if (!(girisZaman.getTime() <= aksamVardiyaBitisZamani.getTime() && cikisZaman.getTime() >= aksamVardiyaBaslangicZamani.getTime()))
									continue;
								if (yemekList == null)
									yemekList = ortakIslemler.getYemekList(session);
								if (girisZaman.before(aksamVardiyaBaslangicZamani))
									girisZaman = aksamVardiyaBaslangicZamani;
								if (cikisZaman.after(aksamVardiyaBitisZamani))
									cikisZaman = aksamVardiyaBitisZamani;
								if (girisZaman.before(cikisZaman)) {
									if (girisZaman.before(aksamVardiyaBaslangicZamani))
										girisZaman = aksamVardiyaBaslangicZamani;
									if (bayramAksamCalismaOde == false && vardiyaGun.getTatil() != null) {
										Tatil tatilSakli = vardiyaGun.getTatil();
										Tatil tatilAksam = tatilSakli;
										if (tatilSakli.getOrjTatil() != null && tatilSakli.getOrjTatil().isTekSefer())
											tatilAksam = (Tatil) tatilSakli.getOrjTatil().clone();
										Date tatilBas = tatilAksam.getBasTarih();
										Date tatilBit = tatilAksam.getBitTarih();
										if (tatilBit.getTime() >= girisZaman.getTime() && cikisZaman.getTime() > tatilBas.getTime()) {
											if (girisZaman.before(tatilBas))
												cikisZaman = tatilBas;
											else if (cikisZaman.after(tatilBit))
												girisZaman = tatilBit;
											else
												girisZaman = cikisZaman;
										}
									}
								}
								double calSure1 = girisZaman.getTime() < cikisZaman.getTime() ? PdksUtil.setSureDoubleTypeRounded(ortakIslemler.getSaatSure(girisZaman, cikisZaman, yemekList, vardiyaGun, session), vardiyaGun.getYarimYuvarla()) : 0.0d;

								sure += calSure1;
							}
							if (sure > 0 && vardiya.isCalisma())
								vardiyaGun.addAksamVardiyaSaatSayisi(sure);

							if (vardiyaGun.getFazlaMesailer() != null) {
								double sureMesai = 0;
								for (PersonelFazlaMesai fazlaMesai : vardiyaGun.getFazlaMesailer()) {
									if (fazlaMesai.isOnaylandi()) {
										if (bayramAksamCalismaOde == false && fazlaMesai.isBayram())
											continue;
										if (fazlaMesai.getBitZaman().getTime() > aksamVardiyaBaslangicZamani.getTime() && fazlaMesai.getBasZaman().getTime() < aksamVardiyaBitisZamani.getTime()) {
											Date girisZaman = fazlaMesai.getBasZaman(), cikisZaman = fazlaMesai.getBitZaman();
											if (girisZaman.before(aksamVardiyaBaslangicZamani))
												girisZaman = aksamVardiyaBaslangicZamani;
											if (cikisZaman.after(aksamVardiyaBitisZamani))
												cikisZaman = aksamVardiyaBitisZamani;
											double calSure1 = PdksUtil.setSureDoubleTypeRounded(ortakIslemler.getSaatSure(girisZaman, cikisZaman, yemekList, vardiyaGun, session), vardiyaGun.getYarimYuvarla());
											if (calSure1 > fazlaMesai.getFazlaMesaiSaati())
												calSure1 = fazlaMesai.getFazlaMesaiSaati();
											sureMesai += calSure1;
										}
									}
								}
								if (sureMesai > 0)
									vardiyaGun.addAksamVardiyaSaatSayisi(sureMesai);
							}
							if (vardiyaGun.getAksamVardiyaSaatSayisi() > 0.0d) {
								double aksamSure = vardiyaGun.getAksamVardiyaSaatSayisi();
								double netSure = vardiya.getNetCalismaSuresi();
								if (vardiya.isCalisma() == false) {
									netSure = 7.5d;
								}
								Double aksamCalismaMaxSaati = new Double(aksamCalismaSaati);
								if (aksamSure > 0.0d && aksamCalismaSaatiYuzde != null && vardiyaGun.getIslemVardiya().isCalisma()) {
									aksamCalismaMaxSaati = vardiyaGun.getIslemVardiya().getNetCalismaSuresi() * aksamCalismaSaatiYuzde.doubleValue() / 100.0d;
								}
								if (aksamCalismaMaxSaati != null && aksamSure >= aksamCalismaMaxSaati && fazlaMesaiMap.containsKey(AylikPuantaj.MESAI_TIPI_AKSAM_ADET)) {
									aksamVardiyaSayisi += 1;
									if (vardiya.isCalisma() == false)
										logger.debug(vardiyaKey);

									logger.debug(aksamVardiyaSayisi + " " + vardiyaKey + " " + vardiyaGun.getVardiyaAciklama());
									if (aksamSure > netSure)
										aksamSure -= netSure;
									else
										aksamSure = 0;
								}
								if (fazlaMesaiMap.containsKey(AylikPuantaj.MESAI_TIPI_AKSAM_SAAT))
									aksamVardiyaSaatSayisi += aksamSure;
							}

						}
					}

				}
				try {
					haftaCalismaSuresi += vardiyaGun.getHaftaCalismaSuresi();
					// if (vardiyaGun.getVardiya() != null && !aksamVardiyasi && vardiyaGun.getFazlaMesailer() != null && !vardiyaGun.getFazlaMesailer().isEmpty()) {
					// ArrayList<PersonelFazlaMesai> fazlaMesailer = new ArrayList<PersonelFazlaMesai>(vardiyaGun.getFazlaMesailer());
					// for (Iterator iterator = fazlaMesailer.iterator(); iterator.hasNext();) {
					// PersonelFazlaMesai personelFazlaMesai = (PersonelFazlaMesai) iterator.next();
					// if (personelFazlaMesai.getOnayDurum() != PersonelFazlaMesai.DURUM_ONAYLANDI)
					// iterator.remove();
					//
					// }
					// if (sabahVardiyalar.contains(vardiyaGun.getVardiya().getKisaAciklama())) {
					// Vardiya iv = vardiyaGun.getIslemVardiya();
					// for (PersonelFazlaMesai fazlaMesai : fazlaMesailer) {
					// if (fazlaMesai.getFazlaMesaiSaati() != null && fazlaMesai.isOnaylandi()) {
					// if (fazlaMesai.getBasZaman().getTime() >= iv.getVardiyaBitZaman().getTime() || fazlaMesai.getBitZaman().getTime() < iv.getVardiyaBasZaman().getTime()) {
					// sabahAksamCikisSaatSayisi += fazlaMesai.getFazlaMesaiSaati();
					// vardiyaGun.addAksamVardiyaSaatSayisi(fazlaMesai.getFazlaMesaiSaati());
					// }
					// }
					// }
					//
					// } else if (sabahVardiya != null) {
					// VardiyaGun vardiyaGun2 = (VardiyaGun) vardiyaGun.clone();
					// vardiyaGun2.setVardiya(sabahVardiya);
					// // vardiyaGun2.setSonrakVardiya(null);
					// // vardiyaGun2.setVardiyaZamani();
					// Vardiya isAsmVardiya = vardiyaGun2.getIslemVardiya();
					// Date basZaman = (Date) isAsmVardiya.getVardiyaBitZaman().clone();
					// for (PersonelFazlaMesai fazlaMesai : fazlaMesailer) {
					// Date girisZaman = (Date) fazlaMesai.getBasZaman().clone(), cikisZaman = (Date) fazlaMesai.getBitZaman().clone();
					// if (fazlaMesai.getFazlaMesaiSaati() != null && fazlaMesai.isOnaylandi()) {
					// if (cikisZaman.getTime() >= basZaman.getTime()) {
					// Double sure = fazlaMesai.getFazlaMesaiSaati();
					// if (girisZaman.before(fazlaMesai.getBasZaman())) {
					// sure = User.getYuvarla(ortakIslemler.getSaatSure(fazlaMesai.getBasZaman(), cikisZaman, null, fazlaMesai.getVardiyaGun(), session));
					// if (sure > fazlaMesai.getFazlaMesaiSaati())
					// sure = fazlaMesai.getFazlaMesaiSaati();
					// }
					// sabahAksamCikisSaatSayisi += sure;
					// vardiyaGun.addAksamVardiyaSaatSayisi(sure);
					// logger.debug(vardiyaGun.getVardiyaKeyStr() + " aksam calismasi " + sure);
					// } else {
					// girisZaman = (Date) fazlaMesai.getBitZaman().clone();
					//
					// }
					// }
					//
					// }
					//
					// }
					// fazlaMesailer = null;
					// }
				} catch (Exception ee) {
					logger.error("Pdks hata in : \n");
					ee.printStackTrace();
					logger.error("Pdks hata out : " + ee.getMessage());
				}

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());

				logger.info(vardiyaGun.getVardiyaKeyStr() + " " + e.getMessage());
			}

		} else {
			String link = "<a href='http://" + adres + "/personelHareket?planKey=" + vardiyaPlanKey + "'>" + personelHareketStr + "</a>";
			vardiyaGun.addLinkAdresler(link);
			if (izin != null) {
				link = "<a href='http://" + adres + "/personelIzinGirisi?izinKey="
						+ PdksUtil.getEncodeStringByBase64("perId=" + personel.getId() + "&tarih1=" + PdksUtil.convertToDateString(izin.getBaslangicZamani(), "yyyyMMdd") + "&tarih2=" + PdksUtil.convertToDateString(izin.getBitisZamani(), "yyyyMMdd")) + "'>" + personelIzinGirisiStr + "</a>";
				vardiyaGun.addLinkAdresler(link);
			}
			vardiyaPlaniGetir(vardiyaGun, vardiyaPlanKey);
			Boolean hataVar = Boolean.TRUE;
			if (vardiyaGun.getVardiya() != null && vardiyaGun.getVardiya().isIcapVardiyasi()) {
				hataVar = Boolean.FALSE;
				vardiyaGun.setHataliDurum(Boolean.FALSE);
			}
			if (hataVar && fazlaMesaiHesapla) {
				vardiyaGun.setOnayli(Boolean.FALSE);
				fazlaMesaiHesapla = Boolean.FALSE;
			}

		}
		if (fazlaMesaiOnaylaDurum && vardiyaGun.getIslemVardiya() != null && vardiyaGun.getHareketDurum().equals(Boolean.TRUE)) {
			try {
				islemVardiya = vardiyaGun.getIslemVardiya();
				boolean calisma = islemVardiya.isCalisma() && vardiyaGun.getIzin() == null;
				Date fazlaMesaiBasZaman = calisma ? islemVardiya.getVardiyaTelorans1BasZaman() : null;
				Date fazlaMesaiBitZaman = calisma ? islemVardiya.getVardiyaTelorans2BitZaman() : null;
				if (girisHareketleri.size() == cikisHareketleri.size()) {
					for (int i = 0; i < girisHareketleri.size(); i++) {
						HareketKGS giris = girisHareketleri.get(i), cikis = cikisHareketleri.get(i);
						if (calisma == false || giris.getOrjinalZaman().before(fazlaMesaiBasZaman) || cikis.getOrjinalZaman().after(fazlaMesaiBitZaman)) {
							if (giris.getPersonelFazlaMesai() == null && cikis.getPersonelFazlaMesai() == null) {
								vardiyaGun.setFazlaMesaiOnayla(true);
								vardiyaGun.setHareketHatali(true);
								personelFazlaMesaiEkle(vardiyaGun, vardiyaPlanKey);
								break;
							}
						}
					}
				}
			} catch (Exception ex1) {
				logger.error(ex1);
				ex1.printStackTrace();
			}

		}
		girisHareketleri = null;
		cikisHareketleri = null;
		if (vardiyaGun.getFazlaMesailer() != null) {
			for (PersonelFazlaMesai personelFazlaMesai : vardiyaGun.getFazlaMesailer()) {
				if (personelFazlaMesai.isOnaylandi()) {

					if (fazlaMesaiOnaylaDurum) {
						vardiyaGun.setFazlaMesaiOnayla(false);
						personelFazlaMesaiEkle(vardiyaGun, vardiyaPlanKey);
					}
					String link = "<a href='http://" + adres + "/personelFazlaMesai?planKey=" + vardiyaPlanKey + "'>" + personelFazlaMesaiStr + "</a>";
					vardiyaGun.addLinkAdresler(link);

					break;

				}
			}
		}
		if (hareketsiz || vardiyaGun.isHataliDurum() || fazlaMesaiOnaylaDurum) {
			kapiGirisGetir(vardiyaGun, vardiyaPlanKey);
			if (fazlaMesaiOnaylaDurum == false)
				vardiyaPlaniGetir(vardiyaGun, vardiyaPlanKey);
		}

		if (vardiyaGun.getHareketDurum() != null && vardiyaGun.getHareketDurum().booleanValue() == false) {
			if (islemPuantaj.isFiiliHesapla()) {
				islemPuantaj.setFiiliHesapla(false);
				fazlaMesaiHesapla = false;
			}

		}
		if (izinDurum) {
			String link = "<a href='http://" + adres + "/personelIzinGirisi?izinKey="
					+ PdksUtil.getEncodeStringByBase64("perId=" + personel.getId() + "&tarih1=" + PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), "yyyyMMdd") + "&tarih2=" + PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), "yyyyMMdd")) + "'>" + personelIzinGirisiStr + "</a>";
			vardiyaGun.addLinkAdresler(link);

		}
		paramsMap.put("fazlaMesaiHesapla", fazlaMesaiHesapla);
		paramsMap.put("aksamVardiyaSayisi", aksamVardiyaSayisi);
		paramsMap.put("aksamVardiyaSaatSayisi", aksamVardiyaSaatSayisi);
		paramsMap.put("fazlaMesaiHesapla", fazlaMesaiHesapla);
		paramsMap.put("sabahAksamCikisSaatSayisi", sabahAksamCikisSaatSayisi);
		paramsMap.put("haftaCalismaSuresi", haftaCalismaSuresi);
		paramsMap.put("resmiTatilSuresi", resmiTatilSuresi);

	}

	/**
	 * @param vardiyaGun
	 * @param vardiyaPlanKey
	 */
	private void kapiGirisGetir(VardiyaGun vardiyaGun, String vardiyaPlanKey) {
		if (denklestirmeAyDurum) {
			String link = "<a href='http://" + adres + "/personelHareket?planKey=" + vardiyaPlanKey + "'>" + personelHareketStr + "</a>";
			vardiyaGun.addLinkAdresler(link);
		}
	}

	/**
	 * @param vardiyaGun
	 * @param vardiyaPlanKey
	 */
	private void vardiyaPlaniGetir(VardiyaGun vardiyaGun, String vardiyaPlanKey) {
		if (denklestirmeAyDurum) {
			String link = "<a href='http://" + adres + "/vardiyaPlani?planKey=" + vardiyaPlanKey + "'>" + vardiyaPlaniStr + "</a>";
			vardiyaGun.addLinkAdresler(link);
		}
	}

	/**
	 * @param fazlaMesaiOnayla
	 * @param vardiyaGun
	 * @param vardiyaPlanKey
	 */
	private void personelFazlaMesaiEkle(VardiyaGun vardiyaGun, String vardiyaPlanKey) {
		if (denklestirmeAyDurum) {
			if (vardiyaGun.getFazlaMesaiOnayla() != null) {
				String link = "<a href='http://" + adres + "/personelFazlaMesai?planKey=" + vardiyaPlanKey + "'>" + personelFazlaMesaiStr + "</a>";
				if (vardiyaGun.getFazlaMesaiOnayla())
					vardiyaGun.addLinkAdresler(link);

				logger.debug(vardiyaGun.getVardiyaKeyStr());
			} else
				kapiGirisGetir(vardiyaGun, vardiyaPlanKey);
			vardiyaPlaniGetir(vardiyaGun, vardiyaPlanKey);
		}
	}

	/**
	 * @param siciller
	 * @return
	 */
	private List<PersonelDenklestirme> getPdksPersonelDenklestirmeler(List<String> siciller) {
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT S.* from " + PersonelDenklestirme.TABLE_NAME + " S WITH(nolock) ");
		sb.append(" INNER JOIN  " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + "=S." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
		sb.append(" AND P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " IS NOT NULL AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " IS NOT NULL ");
		sb.append(" AND P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " :p");
		sb.append(" WHERE S." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=" + denklestirmeAy.getId() + " AND S." + PersonelDenklestirme.COLUMN_NAME_ONAYLANDI + "=1");
		sb.append(" AND S." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + "=1 ");
		if (aylikPuantajDefault.getSonGun().before(new Date()))
			sb.append(" AND S." + PersonelDenklestirme.COLUMN_NAME_DURUM + "=1");
		fields.put("p", siciller);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PersonelDenklestirme> list = pdksEntityController.getObjectBySQLList(sb, fields, PersonelDenklestirme.class);

		fields = null;
		sb = null;
		return list;
	}

	/**
	 * @return
	 */

	public String aylikVardiyaHareketExcel() {
		try {
			List<AylikPuantaj> list = new ArrayList<AylikPuantaj>();
			for (Iterator iter = aylikPuantajList.iterator(); iter.hasNext();) {
				AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();
				if (aylikPuantaj.getHareketler() != null)
					list.add(aylikPuantaj);

			}
			if (!list.isEmpty()) {
				String gorevYeriAciklama = getExcelAciklama();
				ByteArrayOutputStream baosDosya = aylikVardiyaHareketExcelDevam(list);
				if (baosDosya != null) {
					String dosyaAdi = "AylikCalismaHareket_" + gorevYeriAciklama + PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyyMM") + ".xlsx";
					PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi);
				}
			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return "";
	}

	/**
	 * @param puantajList
	 * @return
	 */
	private ByteArrayOutputStream aylikVardiyaHareketExcelDevam(List<AylikPuantaj> puantajList) {

		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		try {
			aylikVardiyaHareketExcelOlustur(puantajList, wb);
			baos = new ByteArrayOutputStream();
			wb.write(baos);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			baos = null;
		}

		return baos;
	}

	/**
	 * @param puantajList
	 * @param wb
	 */
	private void aylikVardiyaHareketExcelOlustur(List<AylikPuantaj> puantajList, Workbook wb) {
		Sheet sheet = ExcelUtil.createSheet(wb, "Tüm Hareketler", Boolean.TRUE);
		CreationHelper factory = wb.getCreationHelper();
		Drawing drawing = sheet.createDrawingPatriarch();
		ClientAnchor anchor = factory.createClientAnchor();
		CellStyle style = ExcelUtil.getStyleData(wb);
		CellStyle styleCenter = ExcelUtil.getStyleData(wb);
		styleCenter.setAlignment(CellStyle.ALIGN_CENTER);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle timeStamp = ExcelUtil.getCellStyleTimeStamp(wb);
		CellStyle date = ExcelUtil.getCellStyleDate(wb);
		XSSFCellStyle dateBold = (XSSFCellStyle) ExcelUtil.getCellStyleDate(wb);
		XSSFFont fontBold = dateBold.getFont();
		XSSFCellStyle styleKapi = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		XSSFFont fontKapiBold = styleKapi.getFont();
		fontKapiBold.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		fontBold.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		int row = 0;
		int col = 0;
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Tarihi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Vardiya");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Kapi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Zaman");
		if (authenticatedUser.isAdmin() || authenticatedUser.isIK())
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Oluşturma Zamanı");

		for (Iterator iterator = puantajList.iterator(); iterator.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
			Personel personel = aylikPuantaj.getPdksPersonel();
			String sirket = "";
			try {
				sirket = personel.getSirket().getAd();
			} catch (Exception e1) {
				sirket = "" + ortakIslemler.sirketAciklama() + " tanımsız";
			}
			for (VardiyaGun vg : aylikPuantaj.getVardiyalar()) {
				if (vg.getVardiya() == null)
					continue;
				String izinAciklama = null, izinKisaAciklama = null;
				if (vg.getIzin() != null) {
					izinAciklama = vg.getIzin().getIzinTipi().getIzinTipiTanim().getAciklama();
					izinKisaAciklama = vg.getIzin().getIzinTipi().getKisaAciklama();
				}

				List<HareketKGS> hareketList = vg.getHareketler();
				boolean sifirla = hareketList == null;
				if (sifirla) {
					hareketList = new ArrayList<HareketKGS>();
					hareketList.add(null);
				}
				Vardiya vardiya = vg.getVardiya();
				CellStyle dateStyle = dateBold;
				String vardiyaAdi = vardiya.isCalisma() ? authenticatedUser.timeFormatla(vardiya.getBasZaman()) + ":" + authenticatedUser.timeFormatla(vardiya.getBitZaman()) : vardiya.getVardiyaAdi();
				CellStyle kapiStyle = vg.getHareketDurum() ? style : styleKapi;
				for (Iterator iter = hareketList.iterator(); iter.hasNext();) {
					Object object = (Object) iter.next();
					HareketKGS hareket = object != null ? (HareketKGS) object : null;
					row++;
					col = 0;
					try {
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue(sirket);
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
						ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getSicilNo());
						ExcelUtil.getCell(sheet, row, col++, dateStyle).setCellValue(vg.getVardiyaDate());
						if (izinKisaAciklama == null)
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue(vardiyaAdi);
						else {
							Cell izinCell = ExcelUtil.getCell(sheet, row, col++, style);
							AylikPuantaj.baslikCell(factory, drawing, anchor, izinCell, izinKisaAciklama + "\n" + vardiyaAdi, izinAciklama);
						}
						if (hareket != null) {
							ExcelUtil.getCell(sheet, row, col++, kapiStyle).setCellValue(hareket.getKapiView().getAciklama());
							ExcelUtil.getCell(sheet, row, col++, timeStamp).setCellValue(hareket.getZaman());
							if (authenticatedUser.isAdmin() || authenticatedUser.isIK()) {
								if (hareket.getOlusturmaZamani() != null) {
									Cell createCell = setCellDate(sheet, row, col++, timeStamp, hareket.getOlusturmaZamani());
									if (hareket.getIslem() != null) {
										PersonelHareketIslem islem = hareket.getIslem();
										Comment commentGuncelleyen = drawing.createCellComment(anchor);
										String title = "Ekleyen : " + islem.getOnaylayanUser().getAdSoyad();
										if (islem.getNeden() != null)
											title += "\nNeden : " + islem.getNeden().getAciklama();
										RichTextString str1 = factory.createRichTextString(title);
										commentGuncelleyen.setString(str1);
										createCell.setCellComment(commentGuncelleyen);
									}
								} else
									ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");

							}
						} else {
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
						}

					} catch (Exception e) {
						logger.error("PDKS hata in : \n");
						e.printStackTrace();
						logger.error("PDKS hata out : " + e.getMessage());
						logger.debug(row);

					}
					dateStyle = date;
				}
				if (sifirla)
					hareketList = null;

			}

		}
		for (int i = 0; i < col; i++)
			sheet.autoSizeColumn(i);
	}

	private String getExcelAciklama() {
		String gorevYeriAciklama = "";
		if (gorevYeri != null)
			gorevYeriAciklama = gorevYeri.getAciklama() + "_";
		else if (seciliEkSaha3Id != null || tesisId != null) {
			HashMap parametreMap = new HashMap();
			Tanim ekSaha3 = null, tesis = null;
			if (tesisId != null) {
				parametreMap.clear();
				parametreMap.put("id", tesisId);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				tesis = (Tanim) pdksEntityController.getObjectByInnerObject(parametreMap, Tanim.class);
			}

			if (seciliEkSaha3Id != null) {
				parametreMap.clear();
				parametreMap.put("id", seciliEkSaha3Id);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				ekSaha3 = (Tanim) pdksEntityController.getObjectByInnerObject(parametreMap, Tanim.class);
			}
			if (tesis != null)
				gorevYeriAciklama = tesis.getAciklama() + "_";
			if (ekSaha3 != null)
				gorevYeriAciklama += ekSaha3.getAciklama() + "_";
		} else if (sirketId != null && tekSirket) {
			HashMap parametreMap = new HashMap();
			parametreMap.put("id", sirketId);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			Sirket sirket = (Sirket) pdksEntityController.getObjectByInnerObject(parametreMap, Sirket.class);
			if (sirket != null)
				gorevYeriAciklama = sirket.getAciklama() + "_";
		}
		return gorevYeriAciklama;
	}

	public String setVardiyaGunHafta(VardiyaHafta vardiyaHafta, Date tarih, int deger) {
		VardiyaGun vardiyaGun = new VardiyaGun();
		vardiyaGun.setVardiyaDate(PdksUtil.tariheGunEkleCikar(tarih, deger));
		String tarihStr = vardiyaGun.getVardiyaDateStr();
		vardiyaHafta.getVardiyaPlan().getVardiyaGunMap().put(tarihStr, vardiyaGun);
		switch (deger) {
		case 0:
			vardiyaHafta.setVardiyaGun1(vardiyaGun);
			break;
		case 1:
			vardiyaHafta.setVardiyaGun2(vardiyaGun);
			break;
		case 2:
			vardiyaHafta.setVardiyaGun3(vardiyaGun);
			break;
		case 3:
			vardiyaHafta.setVardiyaGun4(vardiyaGun);
			break;
		case 4:
			vardiyaHafta.setVardiyaGun5(vardiyaGun);
			break;
		case 5:
			vardiyaHafta.setVardiyaGun6(vardiyaGun);
			break;
		case 6:
			vardiyaHafta.setVardiyaGun7(vardiyaGun);
			break;

		default:
			break;
		}
		vardiyaHafta.getVardiyaGunler().add(vardiyaGun);
		return tarihStr;

	}

	public String fazlaMesaiExcel() {
		try {
			for (Iterator iter = aylikPuantajList.iterator(); iter.hasNext();) {
				AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();
				aylikPuantaj.setSecili(Boolean.TRUE);
			}
			String gorevYeriAciklama = getExcelAciklama();
			ByteArrayOutputStream baosDosya = fazlaMesaiExcelDevam(gorevYeriAciklama, aylikPuantajList);
			if (baosDosya != null) {
 				String dosyaAdi = "FazlaMesai_" + gorevYeriAciklama + PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyyMM") + ".xlsx";
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
	 * @param gorevYeriAciklama
	 * @param list
	 * @return
	 */
	private ByteArrayOutputStream fazlaMesaiExcelDevam(String gorevYeriAciklama, List<AylikPuantaj> list) {

		TreeMap<String, String> sirketMap = new TreeMap<String, String>();

		for (Iterator iter = list.iterator(); iter.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();
			Personel personel = aylikPuantaj.getPdksPersonel();
			String tekSirketTesis = (personel.getSirket() != null ? personel.getSirket().getId() : "") + "_" + (personel.getTesis() != null ? personel.getTesis().getId() : "");
			String tekSirketTesisAdi = (personel.getSirket() != null ? personel.getSirket().getAd() : "") + " " + (personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
			sirketMap.put(tekSirketTesis, tekSirketTesisAdi);
		}

		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "MMMMM yyyy") + " Fazla Mesai", Boolean.TRUE);

		XSSFCellStyle styleTutarEven = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		styleTutarEven.setAlignment(CellStyle.ALIGN_RIGHT);
		styleTutarEven.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleTutarEven.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 219, (byte) 248, (byte) 219 }));

		XSSFCellStyle styleTutarOdd = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		styleTutarOdd.setAlignment(CellStyle.ALIGN_RIGHT);
		styleTutarOdd.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleTutarOdd.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
		XSSFCellStyle styleGenel = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		styleGenel.setAlignment(CellStyle.ALIGN_LEFT);
		XSSFCellStyle styleOdd = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		XSSFCellStyle styleOddCenter = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		styleOddCenter.setAlignment(CellStyle.ALIGN_CENTER);
		XSSFCellStyle styleEven = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		XSSFCellStyle styleEvenCenter = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		styleEvenCenter.setAlignment(CellStyle.ALIGN_CENTER);
		XSSFCellStyle styleTatil = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		styleTatil.setAlignment(CellStyle.ALIGN_CENTER);

		XSSFCellStyle styleIstek = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		styleIstek.setAlignment(CellStyle.ALIGN_CENTER);
		XSSFCellStyle styleEgitim = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		styleEgitim.setAlignment(CellStyle.ALIGN_CENTER);
		XSSFCellStyle styleOff = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		styleOff.setAlignment(CellStyle.ALIGN_CENTER);
		XSSFFont xssfFont = styleOff.getFont();
		xssfFont.setColor(new XSSFColor(Color.WHITE));
		XSSFCellStyle styleIzin = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		styleIzin.setAlignment(CellStyle.ALIGN_CENTER);
		XSSFCellStyle header = (XSSFCellStyle) ExcelUtil.getStyleHeader(wb);
		XSSFCellStyle styleCalisma = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		styleCalisma.setAlignment(CellStyle.ALIGN_CENTER);
		int row = 0, col = 0;

		header.setWrapText(true);

		header.setWrapText(true);
		header.setFillPattern(CellStyle.SOLID_FOREGROUND);
		header.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 156, (byte) 192, (byte) 223 }));

		styleOdd.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleOdd.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
		styleOddCenter.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleOddCenter.setFillForegroundColor(new XSSFColor(new byte[] { (byte) -1, (byte) 213, (byte) 228, (byte) 251 }));
		styleEven.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleEven.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 219, (byte) 248, (byte) 219 }));
		styleEvenCenter.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleEvenCenter.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 219, (byte) 248, (byte) 219 }));
		styleTatil.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleTatil.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 255, (byte) 153, (byte) 204 }));
		styleIstek.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleIstek.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 255, (byte) 255, (byte) 0 }));
		styleIzin.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleIzin.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 146, (byte) 208, (byte) 80 }));
		styleCalisma.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleCalisma.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 255, (byte) 255, (byte) 255 }));
		styleEgitim.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleEgitim.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 0, (byte) 0, (byte) 255 }));
		styleOff.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleOff.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 13, (byte) 12, (byte) 89 }));
		styleOff.getFont().setColor(new XSSFColor(new byte[] { (byte) 256, (byte) 256, (byte) 256 }));
		String aciklamaExcel = PdksUtil.replaceAll(gorevYeriAciklama + " " + PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyy MMMMMM  "), "_", "");
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
		ExcelUtil.getCell(sheet, ++row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		if (seciliEkSaha3Id == null)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.yoneticiAciklama());
		if (fazlaMesaiIzinKullan) {
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("FM Ödeme");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("FM İzin Kullansın");
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(aylikPuantajDefault.getIlkGun());
		CreationHelper factory = wb.getCreationHelper();
		Drawing drawing = sheet.createDrawingPatriarch();
		ClientAnchor anchor = factory.createClientAnchor();
		for (int i = 0; i < aylikPuantajDefault.getGunSayisi(); i++) {
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(cal.get(Calendar.DAY_OF_MONTH) + "\n " + authenticatedUser.getTarihFormatla(cal.getTime(), "EEE"));
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}

		Cell cell = ExcelUtil.getCell(sheet, row, col++, header);
		AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "TÇS", "Toplam Çalışma Saati: Çalışanın bu listedeki toplam çalışma saati");
		cell = ExcelUtil.getCell(sheet, row, col++, header);
		AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "ÇGS", "Çalışılması Gereken Saat: Çalışanın bu listede çalışması gereken saat");
		cell = ExcelUtil.getCell(sheet, row, col++, header);
		AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "GM", "Gerçekleşen Mesai : Çalışanın bu listedeki eksi/fazla çalışma saati");
		cell = ExcelUtil.getCell(sheet, row, col++, header);
		AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "DM", "Devreden Mesai: Çalisanin önceki listelerden devreden eksi/fazla mesaisi");
		cell = ExcelUtil.getCell(sheet, row, col++, header);
		AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "ÜÖM", "Çalışanın bu listenin sonunda ücret olarak ödediğimiz fazla mesai saati");
		if (resmiTatilVar) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "RÖM", "Çalışanın bu listenin sonunda ücret olarak ödediğimiz resmi tatil mesai saati");
		}
		if (haftaTatilVar) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			AylikPuantaj.baslikCell(factory, drawing, anchor, cell, AylikPuantaj.MESAI_TIPI_HAFTA_TATIL, "Çalışanın bu listenin sonunda ücret olarak ödediğimiz hafta tatil mesai saati");
		}

		cell = ExcelUtil.getCell(sheet, row, col++, header);
		AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "B", "Bakiye: Çalışanın bu liste de dahil bugüne kadarki devreden eksi/fazla mesaisi");
		if (aksamGun) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			AylikPuantaj.baslikCell(factory, drawing, anchor, cell, AylikPuantaj.MESAI_TIPI_AKSAM_ADET, "Çalışanın bu listenin sonunda ücret olarak ödediğimiz gece mesai gün");
		}
		if (aksamSaat) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			AylikPuantaj.baslikCell(factory, drawing, anchor, cell, AylikPuantaj.MESAI_TIPI_AKSAM_SAAT, "Çalışanın bu listenin sonunda ücret olarak ödediğimiz gece mesai saati");
		}
		if (modelGoster)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Çalışma Modeli");

		for (Iterator iter = list.iterator(); iter.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();

			Personel personel = aylikPuantaj.getPdksPersonel();
			if (!aylikPuantaj.isFazlaMesaiHesapla() || !aylikPuantaj.isSecili() || personel == null || personel.getSicilNo() == null || personel.getSicilNo().trim().equals(""))
				continue;
			PersonelDenklestirme personelDenklestirme = aylikPuantaj.getPersonelDenklestirmeAylik();
			PersonelDenklestirme personelDenklestirmeGecenAy = personelDenklestirme != null ? personelDenklestirme.getPersonelDenklestirmeGecenAy() : null;
			row++;
			col = 0;
			Comment commentGuncelleyen = null;
			if (personelDenklestirme.getGuncelleyenUser() != null && personelDenklestirme.getGuncellemeTarihi() != null)
				commentGuncelleyen = fazlaMesaiOrtakIslemler.getCommentGuncelleyen(factory, drawing, anchor, personelDenklestirme);

			try {
				boolean help = helpPersonel(aylikPuantaj.getPdksPersonel());
				try {
					if (row % 2 == 0)
						styleGenel = styleOdd;
					else {
						styleGenel = styleEven;
					}
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getSicilNo());
					Cell personelCell = ExcelUtil.getCell(sheet, row, col++, styleGenel);
					personelCell.setCellValue(personel.getAdSoyad());
					if (!sirketMap.isEmpty()) {
						Sirket personelSirket = personel.getSirket();
						String title = personelSirket.getAd() + (personel.getTesis() != null ? " - " + personel.getTesis().getAciklama() : "");
						Comment comment1 = drawing.createCellComment(anchor);
						RichTextString str1 = factory.createRichTextString(title);
						comment1.setString(str1);
						personelCell.setCellComment(comment1);

					}
					if (seciliEkSaha3Id == null)
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");

					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(aylikPuantaj.getYonetici() != null && aylikPuantaj.getYonetici().getId() != null ? aylikPuantaj.getYonetici().getAdSoyad() : "");
					if (fazlaMesaiIzinKullan) {
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(authenticatedUser.getYesNo(personelDenklestirme.getFazlaMesaiOde()));
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(authenticatedUser.getYesNo(personelDenklestirme.getFazlaMesaiIzinKullan()));
					}

					List vardiyaList = aylikPuantaj.getAyinVardiyalari();

					for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
						VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
						String styleText = vardiyaGun.getAylikClassAdi(aylikPuantaj.getTrClass());
						styleGenel = styleCalisma;
						if (styleText.equals(VardiyaGun.STYLE_CLASS_HAFTA_TATIL))
							styleGenel = styleTatil;
						else if (styleText.equals(VardiyaGun.STYLE_CLASS_IZIN))
							styleGenel = styleIzin;
						else if (styleText.equals(VardiyaGun.STYLE_CLASS_OZEL_ISTEK))
							styleGenel = styleIstek;
						else if (styleText.equals(VardiyaGun.STYLE_CLASS_EGITIM))
							styleGenel = styleEgitim;
						else if (styleText.equals(VardiyaGun.STYLE_CLASS_OFF))
							styleGenel = styleOff;
						cell = ExcelUtil.getCell(sheet, row, col++, styleGenel);
						String aciklama = !help || calisan(vardiyaGun) ? vardiyaGun.getFazlaMesaiOzelAciklama(Boolean.TRUE, authenticatedUser.sayiFormatliGoster(vardiyaGun.getCalismaSuresi())) : "";
						String title = !help || calisan(vardiyaGun) ? vardiyaGun.getTitle() : null;
						if (title != null) {
							Comment comment1 = drawing.createCellComment(anchor);
							if (vardiyaGun.getVardiya() != null && (vardiyaGun.getCalismaSuresi() > 0 || (vardiyaGun.getVardiya().isCalisma() && styleGenel == styleCalisma)))
								title = vardiyaGun.getVardiya().getKisaAdi() + " --> " + title;
							RichTextString str1 = factory.createRichTextString(title);
							comment1.setString(str1);
							cell.setCellComment(comment1);

						}
						cell.setCellValue(aciklama);

					}
					if (!help) {
						if (row % 2 == 0)
							styleGenel = styleTutarOdd;
						else {
							styleGenel = styleTutarEven;
						}
						setCell(sheet, row, col++, styleGenel, aylikPuantaj.getSaatToplami());
						Cell planlananCell = setCell(sheet, row, col++, styleGenel, aylikPuantaj.getPlanlananSure());
						if (aylikPuantaj.getCalismaModeliAy() != null && planlananCell != null && aylikPuantaj.getSutIzniDurum().equals(Boolean.FALSE)) {
							Comment comment1 = drawing.createCellComment(anchor);
							String title = aylikPuantaj.getCalismaModeliAy().getCalismaModeli().getAciklama() + " : ";
							if (aylikPuantaj.getCalismaModeliAy().getCalismaModeli().getToplamGunGuncelle().equals(Boolean.FALSE))
								title += authenticatedUser.sayiFormatliGoster(aylikPuantaj.getCalismaModeliAy().getSure());
							else
								title += authenticatedUser.sayiFormatliGoster(aylikPuantaj.getPersonelDenklestirmeAylik().getPlanlanSure());
							RichTextString str1 = factory.createRichTextString(title);
							comment1.setString(str1);
							planlananCell.setCellComment(comment1);
						}
						setCell(sheet, row, col++, styleGenel, aylikPuantaj.getAylikNetFazlaMesai());
						Double gecenAyFazlaMesai = aylikPuantaj.getGecenAyFazlaMesai(authenticatedUser);
						Cell gecenAyFazlaMesaiCell = setCell(sheet, row, col++, styleGenel, gecenAyFazlaMesai);
						if (gecenAyFazlaMesai != null && personelDenklestirmeGecenAy != null && gecenAyFazlaMesai.doubleValue() != 0.0d) {
							if (personelDenklestirmeGecenAy.getGuncelleyenUser() != null && personelDenklestirmeGecenAy.getGuncellemeTarihi() != null) {
								Comment gecenAyFazlaMesaiCommnet = drawing.createCellComment(anchor);
								String title = "Onaylayan : " + personelDenklestirmeGecenAy.getGuncelleyenUser().getAdSoyad() + "\n";
								title += "Zaman : " + authenticatedUser.dateTimeFormatla(personelDenklestirmeGecenAy.getGuncellemeTarihi());
								RichTextString str1 = factory.createRichTextString(title);
								gecenAyFazlaMesaiCommnet.setString(str1);
								gecenAyFazlaMesaiCell.setCellComment(gecenAyFazlaMesaiCommnet);
							}
						}
						boolean olustur = false;
						if (aylikPuantaj.isFazlaMesaiHesapla()) {
							Cell fazlaMesaiSureCell = setCell(sheet, row, col++, styleGenel, aylikPuantaj.getFazlaMesaiSure());

							if (aylikPuantaj.getFazlaMesaiSure() != 0.0d && commentGuncelleyen != null) {
								fazlaMesaiSureCell.setCellComment(commentGuncelleyen);
								olustur = true;
							}
						} else
							ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");

						if (resmiTatilVar)
							setCell(sheet, row, col++, styleGenel, aylikPuantaj.getResmiTatilToplami());
						if (haftaTatilVar)
							setCell(sheet, row, col++, styleGenel, aylikPuantaj.getHaftaCalismaSuresi());
						if (aylikPuantaj.isFazlaMesaiHesapla()) {
							Cell devredenSureCell = setCell(sheet, row, col++, styleGenel, aylikPuantaj.getDevredenSure());
							if (aylikPuantaj.getDevredenSure() != null && aylikPuantaj.getDevredenSure().doubleValue() != 0.0d && commentGuncelleyen != null) {
								if (olustur)
									commentGuncelleyen = fazlaMesaiOrtakIslemler.getCommentGuncelleyen(factory, drawing, anchor, personelDenklestirme);
								devredenSureCell.setCellComment(commentGuncelleyen);
							}
						} else
							ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");

						if (aksamGun)
							setCell(sheet, row, col++, styleGenel, new Double(aylikPuantaj.getAksamVardiyaSayisi()));
						if (aksamSaat)
							setCell(sheet, row, col++, styleGenel, new Double(aylikPuantaj.getAksamVardiyaSaatSayisi()));
						if (modelGoster) {
							String modelAciklama = "";
							if (aylikPuantaj.getPersonelDenklestirmeAylik() != null && aylikPuantaj.getPersonelDenklestirmeAylik().getCalismaModeliAy() != null) {
								CalismaModeliAy calismaModeliAy = aylikPuantaj.getPersonelDenklestirmeAylik().getCalismaModeliAy();
								if (calismaModeliAy.getCalismaModeli() != null)
									modelAciklama = calismaModeliAy.getCalismaModeli().getAciklama();
							}
							ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(modelAciklama);
						}
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
			if (ortakIslemler.getParameterKey("aylikVardiyaHareketExcelOlustur").equals("1"))
				aylikVardiyaHareketExcelOlustur(list, wb);
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
		bakiyeGuncelle = false;
		if (pdksSirketList == null || pdksSirketList.isEmpty())
			setTesisList(new ArrayList<SelectItem>());
		else {
			if (sirketId != null) {
				HashMap fields = new HashMap();
				fields.put("id", sirketId);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);
			}
			List<SelectItem> list = fazlaMesaiOrtakIslemler.getFazlaMesaiTesisList(sirket, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, true, session);
			setTesisList(list);
			Long onceki = tesisId;
			if (list != null && !list.isEmpty()) {
				if (list.size() == 1)
					tesisId = (Long) list.get(0).getValue();
				else if (onceki != null) {
					for (SelectItem st : list) {
						if (st.getValue().equals(onceki))
							tesisId = onceki;
					}
				}
			} else
				bolumDoldurDurum = true;
			onceki = tesisId;
			if (tesisId != null || (sirket != null && (sirket.isErp() == false || sirket.isDepartmanBolumAynisi()))) {
				if (bolumDoldurDurum)
					bolumDoldur();
				setTesisId(onceki);
				if (gorevYeriList != null && list.size() > 1)
					gorevYeriList.clear();
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
					List<String> sayfalar = !superVisorHemsireSayfalari.equals("") ? PdksUtil.getListByString(superVisorHemsireSayfalari, null) : null;
					hastaneSuperVisor = sayfalar != null && sayfalar.contains(calistigiSayfa);

				}

			} else
				hastaneSuperVisor = Boolean.FALSE;
			setGorevYeriList(null);
			bolumDepartmanlari = null;
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
				if (departman.isAdminMi() && sirket.getDepartmanBolumAyni() == false) {
					try {
						// List<SelectItem> list=fazlaMesaiOrtakIslemler.bolumDoldur(departman, sirket, null, tesisId, yil, ay, Boolean.TRUE, session);
						List<SelectItem> list = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, tesisId != null ? String.valueOf(tesisId) : null, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, Boolean.TRUE, session);
						setGorevYeriList(list);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (gorevYeriList.size() == 1)
						seciliEkSaha3Id = (Long) gorevYeriList.get(0).getValue();
				} else {
					// Long depId = departman != null ? departman.getId() : null;
					// bolumDepartmanlari = fazlaMesaiOrtakIslemler.getBolumDepartmanSelectItems(depId, sirketId, yil, ay, Boolean.TRUE, session);
					bolumDepartmanlari = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, null, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, Boolean.TRUE, session);
					if (bolumDepartmanlari.size() == 1)
						seciliEkSaha3Id = (Long) bolumDepartmanlari.get(0).getValue();
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

	public List<YemekIzin> getYemekList() {
		return yemekList;
	}

	public void setYemekList(List<YemekIzin> yemekList) {
		this.yemekList = yemekList;
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

}
