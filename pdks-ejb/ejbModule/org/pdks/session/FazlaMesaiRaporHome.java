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
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.VardiyaHafta;
import org.pdks.entity.VardiyaSaat;
import org.pdks.entity.YemekIzin;
import org.pdks.security.action.UserHome;
import org.pdks.security.entity.User;

import com.pdks.webservice.MailFile;
import com.pdks.webservice.MailObject;
import com.pdks.webservice.MailStatu;

@Name("fazlaMesaiRaporHome")
public class FazlaMesaiRaporHome extends EntityHome<DepartmanDenklestirmeDonemi> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5201033120905302620L;
	static Logger logger = Logger.getLogger(FazlaMesaiRaporHome.class);

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

	private HashMap<String, List<Tanim>> ekSahaListMap;

	private List<DepartmanDenklestirmeDonemi> denklestirmeDonemiList;

	private List<PersonelDenklestirme> baslikDenklestirmeDonemiList;

	private VardiyaGun vardiyaGun;

	private Sirket sirket;

	private DenklestirmeAy denklestirmeAy;

	private Boolean hataYok, fazlaMesaiIzinKullan = Boolean.FALSE, yetkili = Boolean.FALSE, resmiTatilVar = Boolean.FALSE, haftaTatilVar = Boolean.FALSE, kaydetDurum = Boolean.FALSE;
	private Boolean sutIzniGoster = Boolean.FALSE, partTimeGoster = Boolean.FALSE, onayla, hastaneSuperVisor = Boolean.FALSE, sirketIzinGirisDurum = Boolean.FALSE;
	private boolean adminRole, ikRole;

	private Boolean aksamGun = Boolean.FALSE, aksamSaat = Boolean.FALSE, hataliPuantajGoster = Boolean.FALSE, stajerSirket, departmanBolumAyni = Boolean.FALSE;
	private Boolean modelGoster = Boolean.FALSE, kullaniciPersonel = Boolean.FALSE, fazlaMesaiSayfa = Boolean.TRUE;

	private int ay, yil, maxYil, maxFazlaMesaiRaporGun;

	private List<User> toList, ccList, bccList;

	private TreeMap<Long, List<FazlaMesaiTalep>> fmtMap;

	private List<FazlaMesaiTalep> fmtList;

	private List<SelectItem> aylar;

	private AylikPuantaj aylikPuantajDefault, seciliAylikPuantaj;

	private TreeMap<String, Tanim> ekSahaTanimMap;

	private String msgError, msgFazlaMesaiError, sanalPersonelAciklama, bolumAciklama;
	private String sicilNo = "", excelDosyaAdi, mailKonu, mailIcerik;
	private List<YemekIzin> yemekAraliklari;
	private List<VardiyaGun> vgList;
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
	private Date basTarih, bitTarih;

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
		fazlaMesaiSayfa = false;
		adminRoleDurum();
		boolean ayniSayfa = authenticatedUser.getCalistigiSayfa() != null && authenticatedUser.getCalistigiSayfa().equals("fazlaMesaiRapor");
		if (!ayniSayfa)
			authenticatedUser.setCalistigiSayfa("fazlaMesaiRapor");
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		yil = -1;
		ay = -1;
		fazlaMesaiVardiyaGun = null;
		maxFazlaMesaiRaporGun = -1;
		String maxFazlaMesaiRaporGunStr = ortakIslemler.getParameterKey("maxFazlaMesaiRaporGun");
		if (!maxFazlaMesaiRaporGunStr.equals(""))
			try {
				maxFazlaMesaiRaporGun = Integer.parseInt(maxFazlaMesaiRaporGunStr);
			} catch (Exception e) {
				maxFazlaMesaiRaporGun = -1;
			}
		if (maxFazlaMesaiRaporGun < 1)
			maxFazlaMesaiRaporGun = 7;

		if (basTarih == null) {
			basTarih = PdksUtil.getDate(new Date());
			Calendar cal = Calendar.getInstance();
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			if (dayOfWeek != Calendar.MONDAY) {
				cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				if (cal.getTime().after(basTarih))
					cal.add(Calendar.DATE, -7);
				basTarih = PdksUtil.getDate(cal.getTime());
			}
			basTarih = PdksUtil.tariheGunEkleCikar(basTarih, -7);
		}

		if (bitTarih == null)
			bitTarih = PdksUtil.tariheGunEkleCikar(basTarih, maxFazlaMesaiRaporGun - 1);

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
				hastaneSuperVisor = sayfalar != null && sayfalar.contains("fazlaMesaiRapor");

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

			String basTarihStr = (String) req.getParameter("basTarih");
			String bitTarihStr = (String) req.getParameter("bitTarih");
			String linkAdresKey = (String) req.getParameter("linkAdresKey");

			String gorevTipiIdStr = null, gorevYeriIdStr = null, sirketIdStr = null, tesisIdStr = null;
			LinkedHashMap<String, Object> veriLastMap = null;
			if (linkAdresKey == null) {
				veriLastMap = ortakIslemler.getLastParameter("fazlaMesaiRapor", session);
				if (veriLastMap != null) {
					if (veriLastMap.containsKey("basTarih"))
						basTarihStr = (String) veriLastMap.get("basTarih");
					if (veriLastMap.containsKey("bitTarih"))
						bitTarihStr = (String) veriLastMap.get("bitTarih");
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
			if (linkAdresKey != null || (basTarihStr != null && bitTarihStr != null)) {
				if (linkAdresKey != null) {
					HashMap<String, String> veriMap = PdksUtil.getDecodeMapByBase64(linkAdresKey);

					if (veriMap.containsKey("basTarih"))
						basTarihStr = (String) veriMap.get("basTarih");
					if (veriMap.containsKey("bitTarih"))
						bitTarihStr = (String) veriMap.get("bitTarih");
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

				if (basTarihStr != null && bitTarihStr != null) {
					basTarih = PdksUtil.convertToJavaDate(basTarihStr, "yyyyMMdd");
					bitTarih = PdksUtil.convertToJavaDate(bitTarihStr, "yyyyMMdd");
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

				}

			}
			linkAdres = null;
			if (!authenticatedUser.isAdmin() && !authenticatedUser.isIK() && !authenticatedUser.isYoneticiKontratli()) {
				sirket = authenticatedUser.getPdksPersonel().getSirket();
				sirketId = sirket.getId();
			}

			if (!authenticatedUser.isAdmin()) {
				if (departmanId == null && !authenticatedUser.isYoneticiKontratli())
					setDepartmanId(authenticatedUser.getDepartman().getId());

				fillSirketList();
			}
			HashMap parametreMap = new HashMap();
			if (departmanId != null)
				parametreMap.put("id", departmanId);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			setDepartman(departmanId != null ? (Departman) pdksEntityController.getObjectByInnerObject(parametreMap, Departman.class) : null);

			if (departman != null && !departman.isAdminMi()) {
				if (bolumDepartmanlari == null && departman != null)
					bolumDepartmanlari = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, null, new AylikPuantaj(basTarih, bitTarih), Boolean.TRUE, session);
			} else if (sirketId != null)
				tesisDoldur(false);
			if (tesisIdStr != null) {
				if (!tesisList.isEmpty())
					setTesisId(Long.parseLong(tesisIdStr));
				else
					tesisIdStr = null;
			}
			if (tesisIdStr != null)
				setTesisId(Long.parseLong(tesisIdStr));
			bolumDoldur();

			if (!ayniSayfa)
				authenticatedUser.setCalistigiSayfa("");
			if (denklestirmeAy != null && denklestirmeAy.isDurumu() == false)
				hataliPuantajGoster = Boolean.FALSE;
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

	public String tarihDegisti(String tipi) {
		aylikPuantajList.clear();
		if (basTarih.getTime() <= bitTarih.getTime()) {
			Date sonGun = PdksUtil.tariheGunEkleCikar(basTarih, maxFazlaMesaiRaporGun);
			if (sonGun.before(bitTarih))
				PdksUtil.addMessageAvailableWarn(maxFazlaMesaiRaporGun + " günden fazla işlem yapılmaz!");
			else
				try {
					fillSirketList();
					tesisDoldur(false);
					bolumDoldur();

				} catch (Exception e) {

				}

		} else {
			PdksUtil.addMessageAvailableWarn("Tarih hatalıdır!");
		}

		return "";
	}

	public void fillDepartmanList() {
		List<SelectItem> departmanListe = fazlaMesaiOrtakIslemler.getFazlaMesaiDepartmanList(new AylikPuantaj(basTarih, bitTarih), true, session);
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

	public String departmanDegisti() {
		sirketId = null;
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

	/**
	 * 
	 */
	private void fillSirketList() {
		if (adminRole)
			fillDepartmanList();
		List<SelectItem> sirketler = null;
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
			sirketler = fazlaMesaiOrtakIslemler.getFazlaMesaiSirketList(depId, new AylikPuantaj(basTarih, bitTarih), true, session);
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

	@Transactional
	public String fillPersonelDenklestirmeRaporList() {
		boolean devam = true;
		if (basTarih.getTime() <= bitTarih.getTime()) {
			Date sonGun = PdksUtil.tariheGunEkleCikar(basTarih, maxFazlaMesaiRaporGun);
			if (sonGun.before(bitTarih)) {
				PdksUtil.addMessageAvailableWarn(maxFazlaMesaiRaporGun + " günden fazla işlem yapılmaz!");
				devam = false;
			}

		} else {
			PdksUtil.addMessageAvailableWarn("Tarih hatalıdır!");
			devam = false;
		}
		if (!devam)
			return "";

		aksamGun = Boolean.FALSE;
		aksamSaat = Boolean.FALSE;
		haftaTatilVar = Boolean.FALSE;
		mailGonder = !(authenticatedUser.isIK() || authenticatedUser.isAdmin());
		linkAdres = null;
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.clear();

		personelDenklestirmeList.clear();
		ayrikHareketVar = false;

		try {
			DepartmanDenklestirmeDonemi denklestirmeDonemi = new DepartmanDenklestirmeDonemi();

			AylikPuantaj aylikPuantaj = new AylikPuantaj();

			aylikPuantaj.setIlkGun(basTarih);

			aylikPuantaj.setSonGun(bitTarih);
			denklestirmeDonemi.setBaslangicTarih(basTarih);
			denklestirmeDonemi.setBitisTarih(bitTarih);

			TreeMap<String, Tatil> tatilGunleriMap = ortakIslemler.getTatilGunleri(null, basTarih, bitTarih, session);
			for (Iterator iterator = aylikPuantaj.getVardiyalar().iterator(); iterator.hasNext();) {
				VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
				vardiyaGun.setTatil(tatilGunleriMap.containsKey(vardiyaGun.getVardiyaDateStr()) ? tatilGunleriMap.get(vardiyaGun.getVardiyaDateStr()) : null);
			}
			denklestirmeDonemi.setTatilGunleriMap(tatilGunleriMap);
			denklestirmeDonemi.setDenklestirmeAy(denklestirmeAy);
			fillPersonelDenklestirmeRaporDevam(aylikPuantaj, denklestirmeDonemi);
		} catch (Exception ee) {
			logger.error(ee);
			ee.printStackTrace();
		}

		if (!(authenticatedUser.isIK() || authenticatedUser.isAdmin()))
			departmanBolumAyni = false;
		return "";

	}

	/**
	 * @param aylikPuantajSablon
	 * @param denklestirmeDonemi
	 */
	public void fillPersonelDenklestirmeRaporDevam(AylikPuantaj aylikPuantajSablon, DepartmanDenklestirmeDonemi denklestirmeDonemi) {

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
		if (fmtMap == null)
			fmtMap = new TreeMap<Long, List<FazlaMesaiTalep>>();
		else
			fmtMap.clear();
		map1 = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
		saveLastParameter();
		departmanBolumAyni = sirket != null && sirket.isDepartmanBolumAynisi();
		adres = map1.containsKey("host") ? map1.get("host") : "";
		if (sicilNo != null)
			sicilNo = sicilNo.trim();
		hataYok = Boolean.FALSE;
		sutIzniGoster = Boolean.FALSE;
		partTimeGoster = Boolean.FALSE;
		aylikPuantajSablon.getVardiyalar();
		List<AylikPuantaj> puantajList = new ArrayList();
		kaydetDurum = Boolean.FALSE;
		String aksamBordroBasZamani = ortakIslemler.getParameterKey("aksamBordroBasZamani"), aksamBordroBitZamani = ortakIslemler.getParameterKey("aksamBordroBitZamani");
		Integer[] basZaman = ortakIslemler.getSaatDakika(aksamBordroBasZamani), bitZaman = ortakIslemler.getSaatDakika(aksamBordroBitZamani);
		aksamVardiyaBasSaat = basZaman[0];
		aksamVardiyaBasDakika = basZaman[1];
		aksamVardiyaBitSaat = bitZaman[0];
		aksamVardiyaBitDakika = bitZaman[1];

		try {
			seciliBolum = null;

			setVardiyaGun(null);
			HashMap map = new HashMap();

			if (aylikPuantajDefault == null)
				aylikPuantajDefault = new AylikPuantaj();
			aylikPuantajDefault.setIlkGun(basTarih);
			aylikPuantajDefault.setSonGun(bitTarih);
			List<Personel> personelList = fazlaMesaiOrtakIslemler.getFazlaMesaiPersonelList(sirket, tesisId != null ? String.valueOf(tesisId) : null, seciliEkSaha3Id, null, aylikPuantajDefault, true, session);

			if (!personelList.isEmpty()) {
				Date bugun = new Date(), sonCikisZamani = null, sonCalismaGunu = aylikPuantajSablon.getIlkGun();
				Calendar cal = Calendar.getInstance();
				for (VardiyaGun vardiyaGun : aylikPuantajSablon.getVardiyalar()) {
					cal.setTime(vardiyaGun.getVardiyaDate());
					if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY && cal.getTime().after(sonCalismaGunu))
						sonCalismaGunu = vardiyaGun.getVardiyaDate();
				}

				boolean fazlaMesaiOnayla = bugun.after(sonCalismaGunu);

				if (sirket != null && userHome.hasPermission("personelIzinGirisi", "view")) {
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
				// value = perList;
				map.clear();
				denklestirmeDonemi.setBaslangicTarih(basTarih);
				denklestirmeDonemi.setBitisTarih(bitTarih);
				setInstance(denklestirmeDonemi);

				TreeMap<String, Tatil> tatilGunleriMap = ortakIslemler.getTatilGunleri(null, PdksUtil.tariheGunEkleCikar(basTarih, -1), PdksUtil.tariheGunEkleCikar(bitTarih, 1), session);
				// boolean ayBitmedi = denklestirmeDonemi.getBitisTarih().getTime() >= PdksUtil.getDate(bugun).getTime();

				boolean renk = Boolean.FALSE;
				aylikPuantajSablon = fazlaMesaiOrtakIslemler.getAylikPuantaj(0, 0, denklestirmeDonemi, session);
				List<PersonelDenklestirmeTasiyici> list = new ArrayList<PersonelDenklestirmeTasiyici>();
				try {

					list = new ArrayList<PersonelDenklestirmeTasiyici>();
					for (Personel personel : personelList) {
						if (sicilNo.trim().length() == 0 || personel.getPdksSicilNo().trim().equals(sicilNo.trim())) {
							PersonelDenklestirmeTasiyici denklestirmeTasiyici = new PersonelDenklestirmeTasiyici();
							denklestirmeTasiyici.setPersonel(personel);
							denklestirmeTasiyici.setCalismaModeli(personel.getCalismaModeli());
							list.add(denklestirmeTasiyici);
						}
					}
					if (!list.isEmpty())
						ortakIslemler.personelDenklestirmeDuzenle(list, aylikPuantajDefault, tatilGunleriMap, session);
				} catch (Exception ex) {
					list = new ArrayList<PersonelDenklestirmeTasiyici>();
					logger.equals(ex);
					ex.printStackTrace();
				}
				if (!list.isEmpty()) {
					list = PdksUtil.sortObjectStringAlanList(list, "getAdSoyad", null);
					resmiTatilVar = Boolean.FALSE;
					haftaTatilVar = Boolean.FALSE;
					TreeMap<String, PersonelDenklestirmeTasiyici> perMap = new TreeMap<String, PersonelDenklestirmeTasiyici>();

					TreeMap<Long, Long> sirketIdMap = new TreeMap<Long, Long>(), sirketMap = new TreeMap<Long, Long>();
					for (PersonelDenklestirmeTasiyici personelDenklestirme : list) {
						Personel personel = personelDenklestirme.getPersonel();
						if (personel.getPdksYonetici() != null)
							sirketIdMap.put(personel.getPdksYonetici().getId(), personel.getId());
						if (personel.getSirket() != null)
							sirketMap.put(personel.getSirket().getId(), personel.getId());
						String key = (personel.getEkSaha3() != null ? personel.getEkSaha3().getKodu() : "");
						key += "_" + (personel.getBordroAltAlan() != null ? personel.getBordroAltAlan().getKodu() : "");
						key += "_" + personel.getPdksSicilNo() + "_" + personel.getId();
						perMap.put(key, personelDenklestirme);
					}

					list = null;
					list = new ArrayList<PersonelDenklestirmeTasiyici>(perMap.values());
					departmanBolumAyni = sirketMap.size() > 1;

					sirketIdMap = null;
					perMap = null;
					linkAdres = "<a href='http://"
							+ adres
							+ "/fazlaMesaiRapor?linkAdresKey="
							+ PdksUtil.getEncodeStringByBase64("yil=" + yil + "&ay=" + ay + (seciliEkSaha3Id != null ? "&gorevYeriId=" + seciliEkSaha3Id : "") + (tesisId != null ? "&tesisId=" + tesisId : "") + (gorevTipiId != null ? "&gorevTipiId=" + gorevTipiId : "")
									+ (sirket != null ? "&sirketId=" + sirket.getId() : "") + (sicilNo != null && sicilNo.trim().length() > 0 ? "&sicilNo=" + sicilNo.trim() : "")) + "'>" + ortakIslemler.getCalistiMenuAdi("fazlaMesaiHesapla") + " Ekranına Geri Dön</a>";

					List<String> gunList = new ArrayList<String>();
					vgList = aylikPuantajSablon.getVardiyalar();
					setAylikPuantajDefault(aylikPuantajSablon);

					for (Iterator iterator = aylikPuantajDefault.getAyinVardiyalari().iterator(); iterator.hasNext();) {
						VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
						gunList.add(vardiyaGun.getVardiyaDateStr());
					}
					personelIzinGirisiStr = ortakIslemler.getCalistiMenuAdi("personelIzinGirisi");

					personelHareketStr = ortakIslemler.getCalistiMenuAdi("personelHareket");
					personelFazlaMesaiOrjStr = ortakIslemler.getCalistiMenuAdi("personelFazlaMesai");
					vardiyaPlaniStr = ortakIslemler.getCalistiMenuAdi("vardiyaPlani");
					onayla = Boolean.FALSE;

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
						AylikPuantaj puantaj = new AylikPuantaj();
						puantaj.setVardiyalar(denklestirme.getVardiyalar());
						TreeMap<String, VardiyaGun> vgMap = new TreeMap<String, VardiyaGun>();
						puantaj.setVgMap(vgMap);
						for (VardiyaGun vg : denklestirme.getVardiyalar())
							vgMap.put(vg.getVardiyaDateStr(), vg);
						puantaj.setPdksPersonel(denklestirme.getPersonel());
						puantajDenklestirmeList.add(puantaj);
					}
					String yoneticiPuantajKontrolStr = ortakIslemler.getParameterKey("yoneticiPuantajKontrol");
					boolean yoneticiKontrolEtme = authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || yoneticiPuantajKontrolStr.equals("");
					ortakIslemler.yoneticiPuantajKontrol(puantajDenklestirmeList, Boolean.TRUE, session);
					boolean kayitVar = false;
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

					for (Iterator iterator1 = puantajDenklestirmeList.iterator(); iterator1.hasNext();) {
						AylikPuantaj puantaj = (AylikPuantaj) iterator1.next();
						int yarimYuvarla = puantaj.getYarimYuvarla();
						puantaj.setDonemBitti(Boolean.TRUE);
						puantaj.setAyrikHareketVar(false);
						puantaj.setFiiliHesapla(true);
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
						Double aksamVardiyaSaatSayisi = 0d, offSure = null;
						if (stajerSirket && denklestirmeAy.isDurumu()) {
							puantaj.planSureHesapla(tatilGunleriMap);
							offSure = 0.0D;
						}
						TreeMap<String, VardiyaGun> vardiyalar = new TreeMap<String, VardiyaGun>(), vgMap = new TreeMap<String, VardiyaGun>();
						Boolean fazlaMesaiHesapla = Boolean.FALSE;
						cal = Calendar.getInstance();
						puantaj.setHareketler(null);
						double puantajPlanlananSure = 0.0d, puantajSaatToplami = 0.0d, puantajResmiTatil = 0.0d, puantajHaftaTatil = 0.0d;
						boolean puantajFazlaMesaiHesapla = true;
						if (puantaj.getVardiyalar() != null) {
							fazlaMesaiHesapla = puantaj.getPdksPersonel().getPdks();
							for (Iterator iterator = puantaj.getVardiyalar().iterator(); iterator.hasNext();) {
								VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
								String key = vardiyaGun.getVardiyaDateStr();
								vgMap.put(key, vardiyaGun);
								vardiyaGun.setAyinGunu(true);
								if (vardiyaGun.getId() != null)
									vgIdList.add(vardiyaGun.getId());
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
								vardiyaGun.setFiiliHesapla(fazlaMesaiHesapla);
								boolean vardiyaDurum = false;
								if (vardiyaGun.getVardiya() != null && vardiyaGun.getVardiyaDate().getTime() >= basTarih.getTime() && vardiyaGun.getVardiyaDate().getTime() <= bitTarih.getTime()) {
									if (vardiyaGun.getVardiya() != null && vardiyaGun.getVardiya().getId() != null) {
										vardiyaDurum = vardiyaGun.getDurum();
										if (vardiyaGun.getIzin() == null && vardiyaGun.getVardiya().isCalisma()) {
											if (vardiyaGun.getTatil() != null) {
												if (vardiyaGun.getTatil().isYarimGunMu())
													puantajPlanlananSure += puantaj.getPdksPersonel().getCalismaModeli().getArife();
											} else
												puantajPlanlananSure += vardiyaGun.getVardiya().getNetCalismaSuresi();
										}

									}

									// vardiyaGunKontrol(puantaj, vardiyaGun, paramsMap);
									if (!vardiyaDurum)
										puantajFazlaMesaiHesapla = false;
									if (vardiyaGun.getVardiyaSaatDB() != null) {
										if (puantajFazlaMesaiHesapla)
											puantajFazlaMesaiHesapla = vardiyaDurum;
										if (vardiyaDurum) {
											VardiyaSaat vardiyaSaatDB = vardiyaGun.getVardiyaSaatDB();
											if (vardiyaSaatDB.getResmiTatilSure() > 0.0d)
												puantajResmiTatil += vardiyaSaatDB.getResmiTatilSure();
											else if (vardiyaGun.getVardiya().isHaftaTatil()) {
												puantajHaftaTatil += vardiyaSaatDB.getCalismaSuresi();
												vardiyaGun.setHaftaCalismaSuresi(vardiyaSaatDB.getCalismaSuresi());
											}

											if (!vardiyaGun.getVardiya().isHaftaTatil())
												puantajSaatToplami += vardiyaSaatDB.getCalismaSuresi() - vardiyaSaatDB.getResmiTatilSure();
											vardiyaGun.setCalismaSuresi(vardiyaSaatDB.getCalismaSuresi());
											vardiyaGun.setResmiTatilSure(vardiyaSaatDB.getResmiTatilSure());
										}

									}

								}

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

						}
						if (offSure != null)
							puantaj.setOffSure(offSure);
						puantaj.setResmiTatilToplami(0d);
						if (!fazlaMesaiHesapla)
							aksamVardiyaSayisi = 0;
						if (!fazlaMesaiMap.containsKey(AylikPuantaj.MESAI_TIPI_AKSAM_SAAT)) {
							aksamVardiyaSaatSayisi = 0.0d;
						}
						if (!fazlaMesaiMap.containsKey(AylikPuantaj.MESAI_TIPI_AKSAM_ADET)) {
							aksamVardiyaSayisi = 0;
						}
						puantaj.setAksamVardiyaSaatSayisi(aksamVardiyaSaatSayisi);
						puantaj.setAksamVardiyaSayisi(aksamVardiyaSayisi);
						puantaj.setFazlaMesaiHesapla(fazlaMesaiHesapla);
						aylikPuantajSablon.setFazlaMesaiHesapla(fazlaMesaiHesapla);
						double resmiTatilToplami = puantaj.getResmiTatilToplami();
						int izinsizGun = 0;
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
									if (vardiyaGun.getIzin() == null)
										++izinsizGun;

								}

								if (vardiyaGun.getResmiTatilSure() > 0) {

									resmiTatilToplami += vardiyaGun.getResmiTatilSure();
									// logger.info(vardiyaGun.getVardiyaKeyStr() + " " + resmiTatilToplami + " " + vardiyaGun.getResmiTatilSure());
								}
								if (vardiyaGun.getCalisilmayanAksamSure() > 0)
									aksamVardiyaSaatSayisi += vardiyaGun.getCalisilmayanAksamSure();
							}
						}
						if (izinsizGun == 0 && puantaj.getFazlaMesaiSure() != 0.0d) {
							puantaj.setSaatToplami(0.0);
							puantaj.setPlanlananSure(0.0);
						}

						kayitVar = true;

						puantaj.setResmiTatilToplami(PdksUtil.setSureDoubleTypeRounded(resmiTatilToplami, yarimYuvarla));

						puantaj.setAksamVardiyaSaatSayisi(aksamVardiyaSaatSayisi);
						puantaj.setAksamVardiyaSayisi(aksamVardiyaSayisi);
						if (!aksamGun)
							aksamGun = puantaj.getAksamVardiyaSayisi() != 0;
						if (!aksamSaat)
							aksamSaat = puantaj.getAksamVardiyaSaatSayisi() != 0.0d;

						if (gebemi)
							iterator1.remove();
						puantaj.setDonemBitti(Boolean.FALSE);
						puantaj.setIzinSuresi(0.0d);
						puantaj.setResmiTatilToplami(puantajResmiTatil);
						puantaj.setHaftaCalismaSuresi(puantajHaftaTatil);
						puantaj.setSaatToplami(puantajSaatToplami);
						puantaj.setPlanlananSure(puantajPlanlananSure);
						if (!haftaTatilVar)
							haftaTatilVar = puantaj.getHaftaCalismaSuresi() != 0.0d;
						if (!resmiTatilVar)
							resmiTatilVar = puantaj.getResmiTatilToplami() != 0.0d;
						if (sonPersonelCikisZamani != null) {
							if (puantaj.isFazlaMesaiHesapla()) {
								puantaj.setDonemBitti(bugun.after(sonPersonelCikisZamani));
								if (puantaj.isDonemBitti() && (sonCikisZamani == null || sonPersonelCikisZamani.after(sonCikisZamani)))
									sonCikisZamani = sonPersonelCikisZamani;
							}
						} else
							puantaj.setDonemBitti(personel.getIstenAyrilisTarihi().before(new Date()));
						if (yoneticiKontrolEtme == false) {
							if (personel.isSanalPersonelMi() == false && (puantaj.getYonetici() == null || puantaj.getYonetici().getId() == null)) {
								puantaj.setFazlaMesaiHesapla(false);
							}
						}
						puantaj.setFazlaMesaiHesapla(puantajFazlaMesaiHesapla);
						if (sicilNo.trim().length() > 0 || hataliPuantajGoster == false || puantaj.isFazlaMesaiHesapla() == false)
							puantajList.add(puantaj);
					}

					if (puantajList.isEmpty()) {
						if (kayitVar == false)
							PdksUtil.addMessageAvailableWarn("Fazla mesai kaydı bulunmadı!");
						else if (hataliPuantajGoster)
							PdksUtil.addMessageAvailableWarn("Hatalı personel kaydı bulunmadı!");

					} else {

						if (seciliEkSaha3Id == null)
							fazlaMesaiOrtakIslemler.sortAylikPuantajPersonelBolum(puantajList);
					}

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
				setAylikPuantajDefault(aylikPuantajSablon);
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

	}

	/**
	 * @param map1
	 */
	private void saveLastParameter() {
		LinkedHashMap<String, Object> lastMap = new LinkedHashMap<String, Object>();
		lastMap.put("basTarih", PdksUtil.convertToDateString(basTarih, "yyyyMMdd"));
		lastMap.put("bitTarih", PdksUtil.convertToDateString(bitTarih, "yyyyMMdd"));
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

			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception e) {

		}

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
	 * @param islemPuantaj
	 * @param vardiyaGun
	 * @param paramsMap
	 */
	public void vardiyaGunKontrol(AylikPuantaj islemPuantaj, VardiyaGun vardiyaGun, HashMap<String, Object> paramsMap) {
		Date aksamVardiyaBitisZamani = null, aksamVardiyaBaslangicZamani = null;
		Vardiya vardiya = vardiyaGun.getVardiya();
		String key = vardiyaGun.getVardiyaDateStr(), vardiyaKey = vardiyaGun.getVardiyaKeyStr();
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

		if (fazlaMesaiOnaylaDurum) {
			for (Iterator iterator = girisHareketleri.iterator(); iterator.hasNext();) {
				HareketKGS hareketKGS = (HareketKGS) iterator.next();
				if (hareketKGS.getId() == null || hareketKGS.getId().startsWith("V"))
					iterator.remove();
			}
			for (Iterator iterator = cikisHareketleri.iterator(); iterator.hasNext();) {
				HareketKGS hareketKGS = (HareketKGS) iterator.next();
				if (hareketKGS.getId() == null || hareketKGS.getId().startsWith("V"))
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
				if (key.equals("20210210"))
					logger.debug(vardiyaGun.getVardiyaKeyStr());

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
		if (vardiyaGun.isHataliDurum() || fazlaMesaiOnaylaDurum) {
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
		String link = "<a href='http://" + adres + "/personelHareket?planKey=" + vardiyaPlanKey + "'>" + personelHareketStr + "</a>";
		vardiyaGun.addLinkAdresler(link);
	}

	/**
	 * @param vardiyaGun
	 * @param vardiyaPlanKey
	 */
	private void vardiyaPlaniGetir(VardiyaGun vardiyaGun, String vardiyaPlanKey) {
		String link = "<a href='http://" + adres + "/vardiyaPlani?planKey=" + vardiyaPlanKey + "'>" + vardiyaPlaniStr + "</a>";
		vardiyaGun.addLinkAdresler(link);
	}

	/**
	 * @param fazlaMesaiOnayla
	 * @param vardiyaGun
	 * @param vardiyaPlanKey
	 */
	private void personelFazlaMesaiEkle(VardiyaGun vardiyaGun, String vardiyaPlanKey) {
		if (vardiyaGun.getFazlaMesaiOnayla() != null) {
			String link = "<a href='http://" + adres + "/personelFazlaMesai?planKey=" + vardiyaPlanKey + "'>" + personelFazlaMesaiStr + "</a>";
			if (vardiyaGun.getFazlaMesaiOnayla())
				vardiyaGun.addLinkAdresler(link);

			logger.debug(vardiyaGun.getVardiyaKeyStr());
		} else
			kapiGirisGetir(vardiyaGun, vardiyaPlanKey);
		vardiyaPlaniGetir(vardiyaGun, vardiyaPlanKey);
	}

	public String fazlaMesaiOnayKontrol() {
		onayla = Boolean.FALSE;
		for (AylikPuantaj puantaj : aylikPuantajList) {
			if (puantaj.isKaydet()) {
				onayla = Boolean.TRUE;
			}
		}
		seciliBolum = null;
		if (!onayla)
			PdksUtil.addMessageAvailableWarn(PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "MMMMM yyyy") + " fazla mesai onayı yapacak personel seçiniz!");
		else {

			if (seciliEkSaha3Id != null) {
				HashMap parametreMap = new HashMap();

				parametreMap.put("id", seciliEkSaha3Id);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				seciliBolum = (Tanim) pdksEntityController.getObjectByInnerObject(parametreMap, Tanim.class);

			}
		}

		return "";
	}

	@Transactional
	public String fazlaMesaiOnayla() {
		fazlaMesaiOnaylaDevam(Boolean.TRUE);
		return "";
	}

	/**
	 * @param guncellendi
	 * @return
	 */
	private String fazlaMesaiOnaylaDevam(Boolean guncellendi) {
		try {

			boolean onaylandi = Boolean.FALSE;
			TreeMap<Long, List<AylikPuantaj>> puantajMap = new TreeMap<Long, List<AylikPuantaj>>();
			LinkedHashMap<Long, Personel> yoneticiMap = new LinkedHashMap<Long, Personel>();
			boolean mailGonderildi = false;
			List<Long> sirketIdList = new ArrayList<Long>();
			for (AylikPuantaj puantajAylik : aylikPuantajList) {
				if (puantajAylik.isKaydet()) {
					PersonelDenklestirme personelDenklestirmeAy = puantajAylik.getPersonelDenklestirmeAylik();
					personelDenklestirmeAy.setGuncellendi(guncellendi);
					if (personelDenklestirmeAy.getDurum()) {
						Personel calisan = personelDenklestirmeAy.getPersonel();
						if (!personelDenklestirmeAy.isKapandi(authenticatedUser)) {
							personelDenklestirmeAy.setAksamVardiyaSaatSayisi(puantajAylik.getAksamVardiyaSaatSayisi());
							personelDenklestirmeAy.setAksamVardiyaSayisi((double) puantajAylik.getAksamVardiyaSayisi());
							personelDenklestirmeAy.setDevredenSure(puantajAylik.getDevredenSure());
						}
						personelDenklestirmeAy.setHaftaCalismaSuresi(puantajAylik.getHaftaCalismaSuresi());
						personelDenklestirmeAy.setResmiTatilSure(puantajAylik.getResmiTatilToplami());
						personelDenklestirmeAy.setOdenenSure(puantajAylik.getFazlaMesaiSure());
						if (personelDenklestirmeAy.isGuncellendi() && !authenticatedUser.isAdmin()) {
							personelDenklestirmeAy.setGuncellemeTarihi(new Date());
							personelDenklestirmeAy.setGuncelleyenUser(authenticatedUser);
						}
						pdksEntityController.saveOrUpdate(session, entityManager, personelDenklestirmeAy);
						if (mailGonder) {
							if (calisan != null && !sirketIdList.contains(calisan.getSirket().getId()))
								sirketIdList.add(calisan.getSirket().getId());
							puantajAylik.setSecili(Boolean.TRUE);
							Personel personel = null;
							if (authenticatedUser.isIK() || authenticatedUser.isAdmin())
								personel = puantajAylik.getYonetici();
							else
								personel = authenticatedUser.getPdksPersonel();
							if (personel != null && personel.getId() != null) {
								List<AylikPuantaj> aylikPuantajlar = puantajMap.containsKey(personel.getId()) ? (List<AylikPuantaj>) puantajMap.get(personel.getId()) : new ArrayList<AylikPuantaj>();
								if (aylikPuantajlar.isEmpty()) {
									yoneticiMap.put(personel.getId(), personel);
									puantajMap.put(personel.getId(), aylikPuantajlar);
								}
								aylikPuantajlar.add(puantajAylik);
							}
						}
						puantajAylik.setKaydet(Boolean.FALSE);
						onaylandi = Boolean.TRUE;
					}
				}

			}
			tekSirket = sirketIdList.size() == 1;
			if (onaylandi) {
				kaydetDurum = false;
				session.flush();
			} else
				PdksUtil.addMessageAvailableWarn("Kayıt seçiniz!");
			if (mailGonder) {

				toList = ortakIslemler.IKKullanicilariBul(new ArrayList<User>(), authenticatedUser.getPdksPersonel(), session);

				if (!toList.isEmpty() || !yoneticiMap.isEmpty()) {
					List<User> adminUserList = ortakIslemler.bccAdminAdres(session, null);
					Tanim bolum = null;
					if (seciliEkSaha3Id != null && seciliEkSaha3Id > 0) {
						HashMap parametreMap = new HashMap();

						parametreMap.put("id", seciliEkSaha3Id);
						if (session != null)
							parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						bolum = (Tanim) pdksEntityController.getObjectByInnerObject(parametreMap, Tanim.class);

					}
					boolean bolumYok = bolum == null;
					if (bccList == null)
						bccList = new ArrayList<User>();
					else
						bccList.clear();
					if (!adminUserList.isEmpty())
						bccList.addAll(adminUserList);
					if (ccList == null)
						ccList = new ArrayList<User>();
					if (authenticatedUser.isAdmin())
						bccList.add(authenticatedUser);
					String aciklama = null, veriAyrac = "_";
					LinkedHashMap<String, Object> veriMap = ortakIslemler.getListPersonelOzetVeriMap(aylikPuantajList, veriAyrac);
					if (veriMap.containsKey("bolum"))
						bolum = (Tanim) veriMap.get("bolum");
					aciklama = veriMap.containsKey("aciklama") ? (String) veriMap.get("aciklama") : null;
					if (aciklama == null && bolum != null)
						aciklama = bolum.getAciklama();

					mailKonu = PdksUtil.replaceAll(PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "MMMMM yyyy") + " " + (aciklama != null ? " " + PdksUtil.replaceAll(aciklama, veriAyrac, " ") : "") + " fazla mesai onayı", "  ", " ");
					mailIcerik = PdksUtil.replaceAll(PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "MMMMM yyyy") + " " + (aciklama != null ? " " + PdksUtil.replaceAll(aciklama, veriAyrac, " ") : "") + " fazla mesaileri " + authenticatedUser.getAdSoyad()
							+ " tarafından onaylanmıştır.", "  ", " ");
					String gorevYeriAciklama = getExcelAciklama();
					excelDosyaAdi = "fazlaMesai" + PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyyMM") + (bolum != null ? "_" + bolum.getAciklama() : "") + ".xlsx";
					for (Long yoneticiId : yoneticiMap.keySet()) {
						List<AylikPuantaj> list = puantajMap.get(yoneticiId);
						Personel personel = yoneticiMap.get(yoneticiId);
						List<Long> perIdList = new ArrayList<Long>();
						perIdList.add(personel.getId());
						boolean mudurVar = ortakIslemler.getParameterKey("yoneticiPuantajKontrol").equals("1");
						for (AylikPuantaj aylikPuantaj : list) {
							Personel mudur = mudurVar ? aylikPuantaj.getYonetici2() : aylikPuantaj.getPdksPersonel().getYonetici2();
							if (mudur != null && mudur.getId() != null) {
								if (!perIdList.contains(mudur.getId()))
									perIdList.add(mudur.getId());
							}
						}

						if (bolumYok)
							bolum = list.get(0).getPersonelDenklestirmeAylik().getPersonel().getEkSaha3();
						ccList.clear();

						try {

							if (!perIdList.isEmpty()) {
								HashMap fields = new HashMap();
								fields.put(PdksEntityController.MAP_KEY_SESSION, session);
								fields.put(PdksEntityController.MAP_KEY_MAP, "getPersonelId");
								fields.put("pdksPersonel.id", perIdList);
								if (session != null)
									fields.put(PdksEntityController.MAP_KEY_SESSION, session);
								TreeMap<Long, User> perUserMap = pdksEntityController.getObjectByInnerObjectMap(fields, User.class, Boolean.FALSE);
								if (!perUserMap.isEmpty()) {
									List<User> kullanicilar = new ArrayList<User>(perUserMap.values());
									for (Iterator iterator = kullanicilar.iterator(); iterator.hasNext();) {
										User user = (User) iterator.next();
										if (!user.isDurum() || !user.getPdksPersonel().isCalisiyor())
											iterator.remove();

									}
									if (!kullanicilar.isEmpty()) {
										if (kullanicilar.size() > 1)
											kullanicilar = PdksUtil.sortObjectStringAlanList(kullanicilar, "getUsername", null);
										ccList.addAll(kullanicilar);
									}

									kullanicilar = null;
								}

								perUserMap = null;
							}
						} catch (Exception e) {
							logger.error(e);
							e.printStackTrace();
						}
						ByteArrayOutputStream baosDosya = fazlaMesaiExcelDevam(gorevYeriAciklama, list);
						excelData = baosDosya.toByteArray();
						boolean islemDurum = false;
						try {
							islemDurum = servisMesaiOnayMailGonder();
						} catch (Exception e) {
							islemDurum = false;
							logger.error(e);
							e.printStackTrace();
						}
						if (!mailGonderildi)
							mailGonderildi = islemDurum;
						perIdList = null;
						for (AylikPuantaj puantaj : list)
							puantaj.setSecili(Boolean.FALSE);

						list = null;
					}

				}
			}
			if (onaylandi && mailKonu != null) {
				if (!mailGonderildi)
					PdksUtil.addMessageAvailableWarn(mailKonu + " gerçekleşmiştir.");
				else
					PdksUtil.addMessageAvailableWarn(mailKonu + " gerçekleşti, mail bilgilendirmeleri yapılmıştır. ");
			}

			yoneticiMap = null;
			puantajMap = null;
		} catch (Exception e111) {
			logger.error(e111);
			e111.printStackTrace();
		}
		return "";
	}

	/**
	 * @return
	 */
	private boolean servisMesaiOnayMailGonder() {
		boolean islemDurum = false;
		MailObject mailObject = new MailObject();
		mailObject.setSubject(mailKonu);
		mailObject.setBody(mailIcerik);
		if (toList != null) {
			for (User user : toList)
				if (user.getPdksPersonel() == null || user.getPdksPersonel().isCalisiyor())
					mailObject.getToList().add(user.getMailPersonel());
		}
		if (ccList != null) {
			for (User user : ccList)
				if (user.getPdksPersonel() == null || user.getPdksPersonel().isCalisiyor())
					mailObject.getCcList().add(user.getMailPersonel());
		}
		if (bccList != null) {
			for (User user : bccList)
				if (user.getPdksPersonel() == null || user.getPdksPersonel().isCalisiyor())
					if (user.getPdksPersonel() == null || user.getPdksPersonel().isCalisiyor())
						mailObject.getBccList().add(user.getMailPersonel());
		}
		MailFile mailFile = new MailFile();
		mailFile.setIcerik(excelData);
		mailFile.setDisplayName(excelDosyaAdi);
		mailObject.getAttachmentFiles().add(mailFile);
		MailStatu mailStatu = null;
		try {
			if (authenticatedUser.isAdmin()) {
				mailObject.getToList().clear();
				mailObject.getCcList().clear();
			}
			if (!authenticatedUser.isAdmin())
				mailStatu = ortakIslemler.mailSoapServisGonder(true, mailObject, renderer, "/email/fazlaMesaiOnayMail.xhtml", session);
			else
				mailStatu = ortakIslemler.mailSoapServisGonder(true, mailObject, null, null, session);
		} catch (Exception e) {
			mailStatu = new MailStatu();
			if (e.getMessage() != null)
				mailStatu.setHataMesai(e.getMessage());
			else
				mailStatu.setHataMesai("Hata oluştu!");
		}
		islemDurum = mailStatu != null && mailStatu.isDurum();

		return islemDurum;
	}

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
				String dosyaAdi = "DonemselCalisma_" + gorevYeriAciklama + PdksUtil.convertToDateString(basTarih, "yyyyMMdd") + "_" + PdksUtil.convertToDateString(bitTarih, "yyyyMMdd") + ".xlsx";
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
		Sheet sheet = ExcelUtil.createSheet(wb, "Dönemsel Çalışma", Boolean.TRUE);

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
		String aciklamaExcel = PdksUtil.replaceAll(gorevYeriAciklama + " " + PdksUtil.convertToDateString(basTarih, PdksUtil.getDateFormat()) + " - " + PdksUtil.convertToDateString(bitTarih, PdksUtil.getDateFormat() + "  "), "_", "");
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
		for (int i = 0; i < vgList.size(); i++) {
			Date tar = vgList.get(i).getVardiyaDate();
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(authenticatedUser.getTarihFormatla(tar, "dd/MM") + "\n" + authenticatedUser.getTarihFormatla(tar, "EEE"));
		}

		Cell cell = ExcelUtil.getCell(sheet, row, col++, header);
		AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "TÇS", "Toplam Çalışma Saati: Çalışanın bu listedeki toplam çalışma saati");
		cell = ExcelUtil.getCell(sheet, row, col++, header);
		AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "ÇGS", "Çalışılması Gereken Saat: Çalışanın bu listede çalışması gereken saat");
		cell = ExcelUtil.getCell(sheet, row, col++, header);
		AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "GM", "Gerçekleşen Mesai : Çalışanın bu listedeki eksi/fazla çalışma saati");

		if (resmiTatilVar) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "RÖM", "Çalışanın bu listenin sonunda ücret olarak ödediğimiz resmi tatil mesai saati");
		}
		if (haftaTatilVar) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			AylikPuantaj.baslikCell(factory, drawing, anchor, cell, AylikPuantaj.MESAI_TIPI_HAFTA_TATIL, "Çalışanın bu listenin sonunda ücret olarak ödediğimiz hafta tatil mesai saati");
		}

		for (Iterator iter = list.iterator(); iter.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();

			Personel personel = aylikPuantaj.getPdksPersonel();
			if (!aylikPuantaj.isFazlaMesaiHesapla() || !aylikPuantaj.isSecili() || personel == null || personel.getSicilNo() == null || personel.getSicilNo().trim().equals(""))
				continue;
			PersonelDenklestirme personelDenklestirme = aylikPuantaj.getPersonelDenklestirmeAylik();
			row++;
			col = 0;

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
						ExcelUtil.getCell(sheet, row, col++, header).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");

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

						if (resmiTatilVar)
							setCell(sheet, row, col++, styleGenel, aylikPuantaj.getResmiTatilToplami());
						if (haftaTatilVar)
							setCell(sheet, row, col++, styleGenel, aylikPuantaj.getHaftaCalismaSuresi());

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

	/**
	 * @param bolumDoldurDurum
	 * @throws Exception
	 */
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
			List<SelectItem> list = fazlaMesaiOrtakIslemler.getFazlaMesaiTesisList(sirket, new AylikPuantaj(basTarih, bitTarih), true, session);
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
						List<SelectItem> list = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, tesisId != null ? String.valueOf(tesisId) : null, new AylikPuantaj(basTarih, bitTarih), Boolean.TRUE, session);
						setGorevYeriList(list);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (gorevYeriList.size() == 1)
						seciliEkSaha3Id = (Long) gorevYeriList.get(0).getValue();
				} else {
					// Long depId = departman != null ? departman.getId() : null;
					// bolumDepartmanlari = fazlaMesaiOrtakIslemler.getBolumDepartmanSelectItems(depId, sirketId, yil, ay, Boolean.TRUE, session);
					bolumDepartmanlari = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, null, new AylikPuantaj(basTarih, bitTarih), Boolean.TRUE, session);
					if (bolumDepartmanlari.size() == 1)
						seciliEkSaha3Id = (Long) bolumDepartmanlari.get(0).getValue();
				}

			}
		}

		aylikPuantajList.clear();
		return "";
	}

	public void vardiyaGoster(AylikPuantaj ap, VardiyaGun data) {
		vardiyaGun = ap.getVardiyaGun(data);
		seciliAylikPuantaj = ap;
		logger.debug(vardiyaGun.getVardiyaKeyStr());
		fazlaMesaiVardiyaGun = vardiyaGun;
		toplamFazlamMesai = 0D;
		Long key = vardiyaGun.getId();
		fmtList = fmtMap.containsKey(key) ? fmtMap.get(key) : null;

		if (vardiyaGun.getIzin() == null && vardiyaGun.getIzinler() != null) {
			for (Iterator iterator = vardiyaGun.getIzinler().iterator(); iterator.hasNext();) {
				PersonelIzin personelIzin = (PersonelIzin) iterator.next();
				if (personelIzin.isGunlukOldu())
					iterator.remove();
			}
		}
		if (vardiyaGun.getOrjinalHareketler() != null) {
			for (HareketKGS hareket : vardiyaGun.getOrjinalHareketler()) {
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

	public int getMaxFazlaMesaiRaporGun() {
		return maxFazlaMesaiRaporGun;
	}

	public void setMaxFazlaMesaiRaporGun(int maxFazlaMesaiRaporGun) {
		this.maxFazlaMesaiRaporGun = maxFazlaMesaiRaporGun;
	}

	public Date getBasTarih() {
		return basTarih;
	}

	public void setBasTarih(Date basTarih) {
		this.basTarih = basTarih;
	}

	public Date getBitTarih() {
		return bitTarih;
	}

	public void setBitTarih(Date bitTarih) {
		this.bitTarih = bitTarih;
	}

	public Boolean getFazlaMesaiSayfa() {
		return fazlaMesaiSayfa;
	}

	public void setFazlaMesaiSayfa(Boolean fazlaMesaiSayfa) {
		this.fazlaMesaiSayfa = fazlaMesaiSayfa;
	}

	public List<VardiyaGun> getVgList() {
		return vgList;
	}

	public void setVgList(List<VardiyaGun> vgList) {
		this.vgList = vgList;
	}

	public AylikPuantaj getSeciliAylikPuantaj() {
		return seciliAylikPuantaj;
	}

	public void setSeciliAylikPuantaj(AylikPuantaj seciliAylikPuantaj) {
		this.seciliAylikPuantaj = seciliAylikPuantaj;
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
