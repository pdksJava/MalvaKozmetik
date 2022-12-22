package org.pdks.session;

import java.awt.Color;
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
import org.pdks.entity.Kapi;
import org.pdks.entity.KapiView;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelDenklestirmeDinamikAlan;
import org.pdks.entity.PersonelDenklestirmeTasiyici;
import org.pdks.entity.PersonelFazlaMesai;
import org.pdks.entity.PersonelHareketIslem;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGorev;
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.VardiyaHafta;
import org.pdks.entity.VardiyaSaat;
import org.pdks.entity.YemekIzin;
import org.pdks.security.action.StartupAction;
import org.pdks.security.action.UserHome;
import org.pdks.security.entity.User;

import com.pdks.webservice.MailFile;
import com.pdks.webservice.MailObject;
import com.pdks.webservice.MailStatu;

@Name("fazlaMesaiHesaplaHome")
public class FazlaMesaiHesaplaHome extends EntityHome<DepartmanDenklestirmeDonemi> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5201033120905302620L;
	static Logger logger = Logger.getLogger(FazlaMesaiHesaplaHome.class);

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
	@In(required = false, create = true)
	StartupAction startupAction;

	@Out(scope = ScopeType.SESSION, required = false)
	String linkAdres;
	@Out(scope = ScopeType.SESSION, required = false)
	VardiyaGun fazlaMesaiVardiyaGun;
	@In(scope = ScopeType.APPLICATION, required = false)
	List<String> izinliCalisilanGunler;

	@In(required = true, create = true)
	Renderer renderer;

	private List<PersonelDenklestirme> personelDenklestirmeList;

	private List<SelectItem> gorevYeriList, tesisList, altBolumList;

	private List<AylikPuantaj> aylikPuantajList;

	private List<DepartmanDenklestirmeDonemi> denklestirmeDonemiList;

	private List<PersonelDenklestirme> baslikDenklestirmeDonemiList;

	private HashMap<String, List<Tanim>> ekSahaListMap;

	private List saveGenelList;

	private VardiyaGun seciliVardiyaGun;

	private Sirket sirket;

	private DenklestirmeAy denklestirmeAy;

	private Boolean hataYok, fazlaMesaiIzinKullan = Boolean.FALSE, fazlaMesaiTalepSil = Boolean.FALSE, yetkili = Boolean.FALSE, resmiTatilVar = Boolean.FALSE, haftaTatilVar = Boolean.FALSE, kaydetDurum = Boolean.FALSE;
	private Boolean sutIzniGoster = Boolean.FALSE, gebeGoster = Boolean.FALSE, partTimeGoster = Boolean.FALSE, onayla, hastaneSuperVisor = Boolean.FALSE, sirketIzinGirisDurum = Boolean.FALSE;
	private Boolean kesilenSureGoster = Boolean.FALSE, checkBoxDurum;
	private Boolean aksamGun = Boolean.FALSE, aksamSaat = Boolean.FALSE, hataliPuantajGoster = Boolean.FALSE, stajerSirket, departmanBolumAyni = Boolean.FALSE;
	private Boolean modelGoster = Boolean.FALSE, kullaniciPersonel = Boolean.FALSE, denklestirmeAyDurum = Boolean.FALSE, izinGoster = Boolean.FALSE, yoneticiRolVarmi = Boolean.FALSE;
	private boolean adminRole, ikRole, personelHareketDurum, personelFazlaMesaiDurum, vardiyaPlaniDurum, personelIzinGirisiDurum, fazlaMesaiTalepOnayliDurum = Boolean.FALSE;
	private Boolean izinCalismayanMailGonder = Boolean.FALSE, hatalariAyikla = Boolean.FALSE, kismiOdemeGoster = Boolean.FALSE;
	private String manuelGirisGoster = "", kapiGirisSistemAdi = "";
	private boolean yarimYuvarla = true;
	private int ay, yil, maxYil, sonDonem, pageSize;

	private List<User> toList, ccList, bccList;

	private TreeMap<Long, List<FazlaMesaiTalep>> fmtMap;

	private List<FazlaMesaiTalep> fmtList;

	private List<SelectItem> aylar;

	private AylikPuantaj aylikPuantajDefault, seciliAylikPuantaj;

	private TreeMap<String, Tanim> ekSahaTanimMap;

	private String msgError, msgFazlaMesaiError, msgFazlaMesaiInfo, sanalPersonelAciklama, bolumAciklama, tmpAlan;
	private Double eksikSaatYuzde = null;
	private String sicilNo = "", excelDosyaAdi, mailKonu, mailIcerik;
	private List<YemekIzin> yemekAraliklari;
	private CalismaModeli perCalismaModeli;
	private Long seciliEkSaha3Id, sirketId = null, departmanId, gorevTipiId, tesisId, seciliEkSaha4Id;
	private Tanim gorevYeri, seciliBolum, seciliAltBolum, ekSaha4Tanim;
	private Double toplamFazlamMesai = 0D;
	private Double aksamCalismaSaati = null, aksamCalismaSaatiYuzde = null;
	private byte[] excelData;

	private boolean mailGonder, tekSirket;
	private Boolean bakiyeGuncelle, ayrikHareketVar, fazlaMesaiOnayDurum = Boolean.FALSE;

	private List<SelectItem> pdksSirketList, departmanList;
	private Departman departman;
	private String adres, personelIzinGirisiStr, personelHareketStr, personelFazlaMesaiOrjStr, personelFazlaMesaiStr, vardiyaPlaniStr;
	private List<String> sabahVardiyalar, devamlilikPrimIzinTipleri;
	private Vardiya sabahVardiya;
	private Integer aksamVardiyaBasSaat, aksamVardiyaBitSaat, aksamVardiyaBasDakika, aksamVardiyaBitDakika;
	private List<YemekIzin> yemekList;
	private TreeMap<String, Tanim> fazlaMesaiMap;
	private List<Vardiya> izinTipiVardiyaList;
	private TreeMap<String, TreeMap<String, List<VardiyaGun>>> izinTipiPersonelVardiyaMap;
	private List<Tanim> denklestirmeDinamikAlanlar;
	private HashMap<Long, List<HareketKGS>> ciftBolumCalisanHareketMap = new HashMap<Long, List<HareketKGS>>();
	private HashMap<Long, Personel> ciftBolumCalisanMap = new HashMap<Long, Personel>();
	private List<HareketKGS> hareketler = new ArrayList<HareketKGS>();
	private Date bugun;
	private Session session;

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

	/**
	 * @param vardiyaGun1
	 * @param vardiyaGun2
	 * @return
	 */
	public boolean isGunlerEsit(Date vardiyaGun1, Date vardiyaGun2) {
		boolean esit = false;
		if (vardiyaGun1 != null && vardiyaGun2 != null)
			esit = PdksUtil.tarihKarsilastirNumeric(vardiyaGun1, vardiyaGun2) == 0;
		return esit;
	}

	public void aylariDoldur() {

		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("select DISTINCT D.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
		sb.append(" INNER  JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=D." + DenklestirmeAy.COLUMN_NAME_ID);
		sb.append("    AND  PD." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + "=1");
		sb.append(" WHERE D." + DenklestirmeAy.COLUMN_NAME_YIL + "=:y");
		if (yil == maxYil) {
			sb.append(" AND ((D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+" + DenklestirmeAy.COLUMN_NAME_AY + ")<=:s");
			fields.put("s", sonDonem);
		}
		fields.put("y", yil);
		sb.append(" ORDER BY D." + DenklestirmeAy.COLUMN_NAME_AY);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<DenklestirmeAy> list = pdksEntityController.getObjectBySQLList(sb, fields, DenklestirmeAy.class);
		aylar.clear();
		int seciliAy = ay;
		ay = 0;
		for (DenklestirmeAy denklestirmeAy : list) {
			if (denklestirmeAy.getAy() == seciliAy)
				ay = seciliAy;
			aylar.add(new SelectItem(denklestirmeAy.getAy(), denklestirmeAy.getAyAdi()));
		}

	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() {
		boolean ayniSayfa = authenticatedUser.getCalistigiSayfa() != null && authenticatedUser.getCalistigiSayfa().equals("fazlaMesaiHesapla");
		if (!ayniSayfa)
			authenticatedUser.setCalistigiSayfa("fazlaMesaiHesapla");
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		izinCalismayanMailGonder = Boolean.FALSE;
		adminRoleDurum();
		session.clear();
		fazlaMesaiVardiyaGun = null;
		bolumleriTemizle();
		boolean hareketDoldur = false;
		kismiOdemeGoster = Boolean.FALSE;
		try {
			eksikSaatYuzde = null;
			modelGoster = Boolean.FALSE;
			departmanBolumAyni = Boolean.FALSE;
			bakiyeGuncelle = null;
			stajerSirket = Boolean.FALSE;
			sutIzniGoster = Boolean.FALSE;
			gebeGoster = Boolean.FALSE;
			partTimeGoster = Boolean.FALSE;
			mailGonder = Boolean.FALSE;
			setSirket(null);
			sirketId = null;
			setTesisId(null);
			setTesisList(null);
			aylar = PdksUtil.getAyListesi(Boolean.TRUE);
			seciliEkSaha3Id = null;
			seciliEkSaha4Id = null;
			Calendar cal = Calendar.getInstance();
			ay = cal.get(Calendar.MONTH) + 1;
			yil = cal.get(Calendar.YEAR);
			cal.add(Calendar.WEEK_OF_YEAR, 1);
			maxYil = cal.get(Calendar.YEAR);
			sonDonem = (maxYil * 100) + cal.get(Calendar.MONTH) + 1;
			aylikPuantajList = new ArrayList<AylikPuantaj>();
			setInstance(new DepartmanDenklestirmeDonemi());
			// setSirket(null);

			if (authenticatedUser.isSuperVisor() || authenticatedUser.isProjeMuduru()) {
				setSirket(authenticatedUser.getPdksPersonel().getSirket());
				bolumDoldur();
			}
			if (!adminRole) {
				if (departmanId == null && !authenticatedUser.isYoneticiKontratli())
					setDepartmanId(authenticatedUser.getDepartman().getId());

				// fillSirketList();
			}

			Departman pdksDepartman = null;
			if (!authenticatedUser.isAdmin())
				pdksDepartman = authenticatedUser.getDepartman();

			getInstance().setDepartman(pdksDepartman);

			hastaneSuperVisor = Boolean.FALSE;
			if (!(ikRole) && authenticatedUser.getSuperVisorHemsirePersonelNoList() != null) {
				String superVisorHemsireSayfalari = ortakIslemler.getParameterKey("superVisorHemsireSayfalari");
				List<String> sayfalar = !superVisorHemsireSayfalari.equals("") ? PdksUtil.getListByString(superVisorHemsireSayfalari, null) : null;
				hastaneSuperVisor = sayfalar != null && sayfalar.contains("fazlaMesaiHesapla");

			}

			setPersonelDenklestirmeList(new ArrayList<PersonelDenklestirme>());
			HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

			String ayStr = (String) req.getParameter("ay");
			String yilStr = (String) req.getParameter("yil");
			String linkAdresKey = (String) req.getParameter("linkAdresKey");

			String gorevYeriIdStr = null, sirketIdStr = null, tesisIdStr = null, altBolumIdStr = null;
			LinkedHashMap<String, Object> veriLastMap = null;
			if (linkAdresKey == null) {
				veriLastMap = ortakIslemler.getLastParameter("fazlaMesaiHesapla", session);
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
					if (veriLastMap.containsKey("altBolumId"))
						altBolumIdStr = (String) veriLastMap.get("altBolumId");
					if ((ikRole) && veriLastMap.containsKey("sicilNo"))
						sicilNo = (String) veriLastMap.get("sicilNo");
					if (PdksUtil.isSistemDestekVar() && veriLastMap.containsKey("hataliPuantajGoster"))
						hataliPuantajGoster = new Boolean((String) veriLastMap.get("hataliPuantajGoster"));

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
					if (PdksUtil.isSistemDestekVar() && veriMap.containsKey("hataliPuantajGoster"))
						hataliPuantajGoster = new Boolean((String) veriMap.get("hataliPuantajGoster"));
					if (veriMap.containsKey("gorevYeriId"))
						gorevYeriIdStr = veriMap.get("gorevYeriId");
					if (veriMap.containsKey("altBolumId"))
						altBolumIdStr = veriMap.get("altBolumId");
					veriMap = null;
				} else if (veriLastMap == null || veriLastMap.isEmpty()) {
					altBolumIdStr = (String) req.getParameter("altBolumId");
					gorevYeriIdStr = (String) req.getParameter("gorevYeriId");
					tesisIdStr = (String) req.getParameter("tesisId");
					sirketIdStr = (String) req.getParameter("sirketId");
				}

				if (yilStr != null && ayStr != null) {
					yil = Integer.parseInt(yilStr);
					ay = Integer.parseInt(ayStr);
					sirket = null;
					if (gorevYeriIdStr != null)
						seciliEkSaha3Id = Long.parseLong(gorevYeriIdStr);
					if (altBolumIdStr != null)
						seciliEkSaha4Id = Long.parseLong(altBolumIdStr);
					if (tesisIdStr != null)
						tesisId = Long.parseLong(tesisIdStr);
					if (sirketIdStr != null) {
						sirketId = Long.parseLong(sirketIdStr);

					}
					if (sirketId != null && sirket == null) {
						HashMap map = new HashMap();
						map.put("id", sirketId);
						if (session != null)
							map.put(PdksEntityController.MAP_KEY_SESSION, session);
						sirket = (Sirket) pdksEntityController.getObjectByInnerObject(map, Sirket.class);
						if (ikRole) {
							departman = sirket.getDepartman();
							departmanId = departman.getId();
						}
					}
					fillSirketList();

					if (sirket != null) {
						long oncekiId = sirket.getId();
						if (sirket.isErp() && sirket.isDepartmanBolumAynisi() == false)
							tesisDoldur(false);
						if (tesisId != null || sirket.isDepartmanBolumAynisi() || seciliEkSaha3Id != null)
							bolumDoldur();
						if (altBolumIdStr != null)
							seciliEkSaha4Id = Long.parseLong(altBolumIdStr);
						if (sirket == null) {
							HashMap map = new HashMap();
							map.put("id", oncekiId);
							if (session != null)
								map.put(PdksEntityController.MAP_KEY_SESSION, session);
							sirket = (Sirket) pdksEntityController.getObjectByInnerObject(map, Sirket.class);
							if (sirketId == null)
								sirketId = oncekiId;
						}

						departmanId = sirket.getDepartman().getId();
						setDepartman(sirket.getDepartman());
					}
					hareketDoldur = seciliEkSaha3Id != null || seciliEkSaha4Id != null;

				}

			}
			linkAdres = null;
			if (denklestirmeAy == null)
				setSeciliDenklestirmeAy();

			if (hareketDoldur == false) {
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

					if (sirket != null || sirketId != null) {
						if (sirket == null) {
							parametreMap.clear();
							parametreMap.put("id", parametreMap);
							if (session != null)
								parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
							sirket = (Sirket) pdksEntityController.getObjectByInnerObject(parametreMap, Sirket.class);
						}
						gorevYeriList = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, null, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, true, session);

					}
				} else if (sirketId != null)
					tesisDoldur(false);
				if (tesisIdStr != null)
					setTesisId(Long.parseLong(tesisIdStr));
				bolumDoldur();
			} else if (veriLastMap == null)
				fillPersonelDenklestirmeList();
			eksikSaatYuzde = getDepartmanSaatlikIzin();
			setDenklestirmeAyDurum(fazlaMesaiOrtakIslemler.getDurum(denklestirmeAy));
			if (denklestirmeAyDurum.equals(Boolean.FALSE))
				hataliPuantajGoster = denklestirmeAyDurum;
			if (!ayniSayfa)
				authenticatedUser.setCalistigiSayfa("");
			fillEkSahaTanim();
		} catch (Exception e) {
			e.printStackTrace();
		}
		aylariDoldur();
		kullaniciPersonel = ortakIslemler.getKullaniciPersonel(authenticatedUser);
		if (kullaniciPersonel) {
			tesisList = null;
			sicilNo = authenticatedUser.getPdksPersonel().getPdksSicilNo();
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

	/**
	 * @param yilParametre
	 * @return
	 * @throws Exception
	 */
	public String yilAyDegisti(boolean yilParametre) throws Exception {
		denklestirmeAy = null;
		if (yilParametre) {
			aylariDoldur();
			if (aylar != null && aylar.size() == 1)
				ay = (Integer) aylar.get(0).getValue();
		}

		else {
			Integer seciliAy = ay;
			ay = 0;
			for (SelectItem st : aylar) {
				if (st.getValue().equals(seciliAy))
					ay = seciliAy;
			}
		}
		setSeciliDenklestirmeAy();
		if (denklestirmeAy != null) {
			departmanDegisti(false);
		}

		return "";
	}

	/**
	 * 
	 */
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

	/**
	 * @param degisti
	 * @return
	 */
	public String departmanDegisti(boolean degisti) {
		if (degisti) {
			sirketId = null;
			seciliEkSaha3Id = null;
			seciliEkSaha4Id = null;
			if (tesisList != null)
				tesisList.clear();
			if (gorevYeriList != null)
				gorevYeriList.clear();

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
					if (tesisId == null) {
						seciliEkSaha3Id = null;
						seciliEkSaha4Id = null;
					}

				}
			}
			if (bolumDoldurulmadi) {
				if (tesisId != null || seciliEkSaha3Id != null || (sirket != null && sirket.isDepartmanBolumAynisi()))
					bolumDoldur();
				if (seciliEkSaha3Id != null && ekSaha4Tanim != null)
					altBolumDoldur();
			}

		}
		return "";
	}

	private void fillSirketList() {
		if (adminRole)
			fillDepartmanList();
		List<SelectItem> sirketler = null;
		eksikSaatYuzde = getDepartmanSaatlikIzin();
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
		ekSaha4Tanim = null;
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
					ekSaha4Tanim = ortakIslemler.getEkSaha4(sirket, sirketId, session);
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

	/**
	 * @return
	 */
	private Double getDepartmanSaatlikIzin() {
		Double yuzde = null;
		String eksikSaatYuzdeStr = ortakIslemler.getParameterKey("eksikSaatYuzde");
		if (!eksikSaatYuzdeStr.equals("") && !eksikSaatYuzdeStr.equals("0")) {
			try {
				yuzde = Double.parseDouble(eksikSaatYuzdeStr);
				if (yuzde <= 0.0d)
					yuzde = null;
			} catch (Exception e) {
				yuzde = null;
			}
		}
		if (yuzde == null && departmanId != null) {
			HashMap fields = new HashMap();
			if (departmanId != null)
				fields.put("departman.id=", departmanId);
			fields.put("durum=", true);
			fields.put("personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
			fields.put("saatGosterilecek=", true);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<IzinTipi> izinTipiList = pdksEntityController.getObjectByInnerObjectListInLogic(fields, IzinTipi.class);
			if (!izinTipiList.isEmpty())
				yuzde = 100.0d;
		}
		return yuzde;
	}

	public String fillPersonelSicilDenklestirmeList() {
		if (sicilNo.trim().equals(""))
			aylikPuantajList.clear();
		else {
			sicilNo = ortakIslemler.getSicilNo(sicilNo);
			try {
				fillPersonelDenklestirmeList();
			} catch (Exception e) {
				logger.equals(e);
				e.printStackTrace();
			}

		}

		return "";
	}

	@Transactional
	public String fillPersonelDenklestirmeList() {
		aksamGun = Boolean.FALSE;
		aksamSaat = Boolean.FALSE;
		haftaTatilVar = Boolean.FALSE;
		mailGonder = !(ikRole);
		linkAdres = null;
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.clear();
		ciftBolumCalisanMap.clear();
		ciftBolumCalisanHareketMap.clear();
		yoneticiRolVarmi = ortakIslemler.yoneticiRolKontrol(session);
		// fillSirketList();
		HashMap fields = new HashMap();
		fields.put("ay", ay);
		fields.put("yil", yil);
		personelDenklestirmeList.clear();
		ayrikHareketVar = false;
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
		denklestirmeAyDurum = fazlaMesaiOrtakIslemler.getDurum(denklestirmeAy);
		if (denklestirmeAy != null) {
			try {
				if (denklestirmeAy.getFazlaMesaiMaxSure() == null)
					fazlaMesaiOrtakIslemler.setFazlaMesaiMaxSure(denklestirmeAy, session);
				DepartmanDenklestirmeDonemi denklestirmeDonemi = new DepartmanDenklestirmeDonemi();
				AylikPuantaj aylikPuantaj = fazlaMesaiOrtakIslemler.getAylikPuantaj(ay, yil, denklestirmeDonemi, session);
				denklestirmeDonemi.setDenklestirmeAy(denklestirmeAy);
				fillPersonelDenklestirmeDevam(aylikPuantaj, denklestirmeDonemi);
			} catch (Exception ee) {
				logger.error(ee);
				ee.printStackTrace();
			}

		} else
			PdksUtil.addMessageWarn("İlgili döneme ait fazla mesai bulunamadı!");

		if (!(ikRole))
			departmanBolumAyni = false;
		tmpAlan = "";
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
	public void fillPersonelDenklestirmeDevam(AylikPuantaj aylikPuantajSablon, DepartmanDenklestirmeDonemi denklestirmeDonemi) {
		denklestirmeDonemi.setDenklestirmeAy(denklestirmeAy);
		saveLastParameter();
		boolean testDurum = PdksUtil.getTestDurum() && false;
		Date basTarih = new Date();
		if (testDurum)
			logger.info("fillPersonelDenklestirmeDevam 0000 " + basTarih);
		String haftaTatilDurum = ortakIslemler.getParameterKey("haftaTatilDurum");
		seciliBolum = null;
		seciliAltBolum = null;
		kismiOdemeGoster = Boolean.FALSE;
		fazlaMesaiVardiyaGun = null;
		Tanim devamlilikPrimi = null;
		kesilenSureGoster = Boolean.FALSE;
		Map<String, String> map1 = null;
		sanalPersonelAciklama = ortakIslemler.sanalPersonelAciklama();
		izinGoster = (authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || !ortakIslemler.getParameterKey("izinPersonelOzetGoster").equals(""));
		sabahVardiya = null;
		departmanBolumAyni = Boolean.FALSE;
		aksamGun = Boolean.FALSE;
		aksamSaat = Boolean.FALSE;
		haftaTatilVar = Boolean.FALSE;
		fazlaMesaiIzinKullan = Boolean.FALSE;
		sirketIzinGirisDurum = Boolean.FALSE;
		yemekList = null;
		bugun = new Date();
		fazlaMesaiOnayDurum = Boolean.FALSE;
		if (fmtMap == null)
			fmtMap = new TreeMap<Long, List<FazlaMesaiTalep>>();
		else
			fmtMap.clear();
		if (saveGenelList == null)
			saveGenelList = new ArrayList();
		else
			saveGenelList.clear();

		map1 = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
		departmanBolumAyni = sirket != null && sirket.isDepartmanBolumAynisi();
		adres = map1.containsKey("host") ? map1.get("host") : "";
		if (sicilNo != null)
			sicilNo = sicilNo.trim();
		setHataYok(Boolean.FALSE);
		if (denklestirmeDinamikAlanlar == null)
			denklestirmeDinamikAlanlar = new ArrayList<Tanim>();
		else
			denklestirmeDinamikAlanlar.clear();
		sutIzniGoster = Boolean.FALSE;
		gebeGoster = Boolean.FALSE;
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
			seciliAltBolum = null;
			setSeciliVardiyaGun(null);
			HashMap map = new HashMap();

			List<String> perList = new ArrayList<String>();
			sicilNo = ortakIslemler.getSicilNo(sicilNo);
			List<Personel> donemPerList = fazlaMesaiOrtakIslemler.getFazlaMesaiPersonelList(sirket, tesisId != null ? String.valueOf(tesisId) : null, seciliEkSaha3Id, seciliEkSaha4Id, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, true, session);
			sicilNo = ortakIslemler.getSicilNo(sicilNo);
			for (Personel personel : donemPerList) {
				if (sicilNo.equals("") || sicilNo.trim().equals(personel.getPdksSicilNo().trim()))
					perList.add(personel.getPdksSicilNo());
			}

			if (authenticatedUser.getDepartman().isAdminMi() == false && (authenticatedUser.isSuperVisor() || authenticatedUser.isProjeMuduru())) {

				sirket = authenticatedUser.getPdksPersonel().getSirket();
			}
			if (sirketId != null && (ikRole)) {
				HashMap parametreMap = new HashMap();
				parametreMap.put("id", sirketId);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				sirket = (Sirket) pdksEntityController.getObjectByInnerObject(parametreMap, Sirket.class);

			}

			if (sirket != null)
				departmanBolumAyni = sirket.isDepartmanBolumAynisi();

			String searchKey = "sirket.id=";
			if (sirket == null)
				if (!authenticatedUser.isIK() && !authenticatedUser.isAdmin())
					sirket = authenticatedUser.getPdksPersonel().getSirket();
			if (perList != null) {
				searchKey = "pdksSicilNo";
				if (perList.isEmpty())
					perList.add("YOKTUR");
			}

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
					String perNo = personel.getPdksSicilNo();
					if (perNo == null || perNo.trim().length() == 0)
						continue;
					perNo = perNo.trim();
					if (!perList.contains(perNo) && (sicilNo.trim().length() == 0 || sicilNo.equals(perNo)))
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
			perList.clear();

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
			Date sonCikisZamani = null;
			Date gunBas = PdksUtil.getDate(bugun);
			Calendar cal = Calendar.getInstance();
			if (seciliEkSaha3Id != null && PdksUtil.isSistemDestekVar()) {
				HashMap parametreMap = new HashMap();
				parametreMap.put("id", seciliEkSaha3Id);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				seciliBolum = (Tanim) pdksEntityController.getObjectByInnerObject(parametreMap, Tanim.class);

			}
			if (seciliEkSaha4Id != null && PdksUtil.isSistemDestekVar()) {
				HashMap parametreMap = new HashMap();
				parametreMap.put("id", seciliEkSaha4Id);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				seciliAltBolum = (Tanim) pdksEntityController.getObjectByInnerObject(parametreMap, Tanim.class);

			}
			if (!perList.isEmpty()) {
				personelIzinGirisiDurum = userHome.hasPermission("personelIzinGirisi", "view");
				if (sirket != null && denklestirmeAyDurum && personelIzinGirisiDurum) {
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

				devamlilikPrimIzinTipleri = PdksUtil.getListByString(ortakIslemler.getParameterKey("devamlilikPrimIzinTipleri"), null);
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
				setInstance(denklestirmeDonemi);
				map.clear();
				map.put("pdksSicilNo", perList);
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<Personel> perListesi = pdksEntityController.getObjectByInnerObjectList(map, Personel.class);
				TreeMap<String, Tatil> tatilGunleriMap = ortakIslemler.getTatilGunleri(perListesi, PdksUtil.tariheGunEkleCikar(denklestirmeDonemi.getBaslangicTarih(), -1), PdksUtil.tariheGunEkleCikar(denklestirmeDonemi.getBitisTarih(), 1), session);
				boolean ayBitmedi = denklestirmeDonemi.getBitisTarih().getTime() >= PdksUtil.getDate(bugun).getTime();
				List<PersonelDenklestirmeTasiyici> list = null;
				if (testDurum)
					logger.info("fillPersonelDenklestirmeDevam 4000 " + new Date());
				try {
					denklestirmeDonemi.setPersonelDenklestirmeDonemMap(personelDenklestirmeDonemMap);
					denklestirmeDonemi.setDenklestirmeAyDurum(denklestirmeAyDurum);
					list = ortakIslemler.personelDenklestir(denklestirmeDonemi, tatilGunleriMap, searchKey, perList, Boolean.TRUE, Boolean.FALSE, ayBitmedi, session);
					if (list.isEmpty()) {
						session.flush();
						session.clear();
						denklestirmeDonemi.setDurum(Boolean.FALSE);
						tatilGunleriMap = ortakIslemler.getTatilGunleri(perListesi, PdksUtil.tariheGunEkleCikar(denklestirmeDonemi.getBaslangicTarih(), -1), PdksUtil.tariheGunEkleCikar(denklestirmeDonemi.getBitisTarih(), 1), session);
						list = ortakIslemler.personelDenklestir(denklestirmeDonemi, tatilGunleriMap, searchKey, perList, Boolean.TRUE, Boolean.FALSE, ayBitmedi, session);

					}

				} catch (Exception ex) {
					list = new ArrayList<PersonelDenklestirmeTasiyici>();
					logger.equals(ex);
					ex.printStackTrace();
				}
				if (testDurum)
					logger.info("fillPersonelDenklestirmeDevam 5000 " + new Date());
				if (!list.isEmpty()) {

					if (list.size() > 1)
						list = PdksUtil.sortObjectStringAlanList(list, "getAdSoyad", null);
				}

				boolean renk = Boolean.FALSE;
				aylikPuantajSablon = fazlaMesaiOrtakIslemler.getAylikPuantaj(ay, yil, denklestirmeDonemi, session);

				List<VardiyaHafta> vardiyaHaftaList = new ArrayList<VardiyaHafta>();
				fazlaMesaiOrtakIslemler.haftalikVardiyaOlustur(vardiyaHaftaList, aylikPuantajSablon, denklestirmeDonemi, tatilGunleriMap, null);
				resmiTatilVar = Boolean.FALSE;
				haftaTatilVar = Boolean.FALSE;
				TreeMap<String, PersonelDenklestirmeTasiyici> perMap = new TreeMap<String, PersonelDenklestirmeTasiyici>();

				List<Long> kontratliPerIdList = new ArrayList<Long>();
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
				if (!list.isEmpty()) {
					kontratliPerIdList.clear();
					for (Iterator iterator = list.iterator(); iterator.hasNext();) {
						PersonelDenklestirmeTasiyici personelDenklestirme = (PersonelDenklestirmeTasiyici) iterator.next();
						Personel personel = personelDenklestirme.getPersonel();
						kontratliPerIdList.add(personel.getId());
						perMap.put(String.valueOf(personel.getId()), personelDenklestirme);
						iterator.remove();
					}
					List<Personel> personelList = ortakIslemler.getKontratliSiraliPersonel(kontratliPerIdList, session);
					if (sirketIdMap.size() > 0 && sirketIdMap.size() < personelList.size()) {
						List<Personel> perDigerList = new ArrayList<Personel>();
						for (Iterator iterator = personelList.iterator(); iterator.hasNext();) {
							Personel personel = (Personel) iterator.next();
							if (!sirketIdMap.containsKey(personel.getId())) {
								perDigerList.add(personel);
								iterator.remove();
							}
						}
						if (!perDigerList.isEmpty())
							personelList.addAll(perDigerList);
						perDigerList = null;
					}
					sirketIdMap = null;
					for (Personel personel : personelList)
						list.add(perMap.get(String.valueOf(personel.getId())));
					personelList = null;
				}
				sirketIdMap = null;
				kontratliPerIdList = null;
				perMap = null;
				linkAdres = "<a href='http://"
						+ adres
						+ "/fazlaMesaiHesapla?linkAdresKey="
						+ PdksUtil.getEncodeStringByBase64("yil=" + yil + "&ay=" + ay + (hataliPuantajGoster != null && hataliPuantajGoster ? "&hataliPuantajGoster=" + hataliPuantajGoster : "") + (seciliEkSaha3Id != null ? "&gorevYeriId=" + seciliEkSaha3Id : "")
								+ (seciliEkSaha4Id != null ? "&altBolumId=" + seciliEkSaha4Id : "") + (tesisId != null ? "&tesisId=" + tesisId : "") + (gorevTipiId != null ? "&gorevTipiId=" + gorevTipiId : "") + (sirket != null ? "&sirketId=" + sirket.getId() : "")
								+ (sicilNo != null && sicilNo.trim().length() > 0 ? "&sicilNo=" + sicilNo.trim() : "")) + "'>" + ortakIslemler.getCalistiMenuAdi("fazlaMesaiHesapla") + " Ekranına Geri Dön</a>";
				boolean flush = Boolean.FALSE;
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

				HashMap<String, Object> paramsMap = new HashMap<String, Object>();
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
				List<Long> denklestirmeIdList = new ArrayList<Long>();
				List<PersonelDenklestirmeTasiyici> haftaSonuList = new ArrayList<PersonelDenklestirmeTasiyici>();
				List<VardiyaGun> bosCalismaList = new ArrayList<VardiyaGun>();
				for (Iterator iterator1 = list.iterator(); iterator1.hasNext();) {
					PersonelDenklestirmeTasiyici denklestirme = (PersonelDenklestirmeTasiyici) iterator1.next();
					if (personelDenklestirmeMap.containsKey(denklestirme.getPersonel().getId())) {
						PersonelDenklestirme personelDenklestirme = personelDenklestirmeMap.get(denklestirme.getPersonel().getId());
						boolean hareketKaydiVardiyaBulsunmu = personelDenklestirme.getCalismaModeliAy().isHareketKaydiVardiyaBulsunmu();
						if (hareketKaydiVardiyaBulsunmu) {
							// if (haftaTatilDurum.equals("1"))
							haftaSonuList.add(denklestirme);
							TreeMap<String, VardiyaGun> vardiyaGunleriMap = denklestirme.getVardiyaGunleriMap();
							if (vardiyaGunleriMap != null) {
								for (String key : vardiyaGunleriMap.keySet()) {
									VardiyaGun vardiyaGun = vardiyaGunleriMap.get(key);
									if (vardiyaGun.isAyinGunu() && vardiyaGun.getVardiya() != null && vardiyaGun.getVardiya().isCalisma() && vardiyaGun.getVersion() < 0) {
										if (vardiyaGun.getIslemVardiya().getVardiyaBitZaman().before(bugun) && vardiyaGun.getCalismaSuresi() == 0.0d && vardiyaGun.isIzinli() == false && vardiyaGun.getHareketler() == null)
											bosCalismaList.add(vardiyaGun);
									}
								}
							}
						}

						denklestirmeIdList.add(personelDenklestirme.getId());
					}
				}
				if (!ortakIslemler.getParameterKey("bosCalismaOffGuncelle").equals("1"))
					bosCalismaList.clear();
				if (denklestirmeAyDurum && (bosCalismaList.size() + haftaSonuList.size()) > 0) {
					if (!haftaSonuList.isEmpty())
						haftaTatilVardiyaGuncelle(haftaSonuList);
					if (!bosCalismaList.isEmpty())
						bosCalismaOffGuncelle(bosCalismaList, haftaSonuList.isEmpty());
				}
				bosCalismaList = null;
				haftaSonuList = null;
				TreeMap<String, PersonelDenklestirmeDinamikAlan> devamlilikPrimiMap = new TreeMap<String, PersonelDenklestirmeDinamikAlan>();
				devamlilikPrimi = denklestirmeMantiksalBilgiBul(PersonelDenklestirmeDinamikAlan.TIPI_DENKLESTIRME_DEVAMLILIK_PRIMI);
				if (devamlilikPrimi != null)
					setDenklestirmeDinamikDurum(denklestirmeIdList, devamlilikPrimiMap);
				Date izinCalismayanMailSonGun = PdksUtil.tariheGunEkleCikar(aylikPuantajSablon.getSonGun(), -5);
				izinCalismayanMailGonder = bugun.after(izinCalismayanMailSonGun) || authenticatedUser.isAdmin();
				List<AylikPuantaj> puantajDenklestirmeList = new ArrayList<AylikPuantaj>();
				for (Iterator iterator1 = list.iterator(); iterator1.hasNext();) {
					PersonelDenklestirmeTasiyici denklestirme = (PersonelDenklestirmeTasiyici) iterator1.next();
					AylikPuantaj puantaj = (AylikPuantaj) aylikPuantajSablon.clone();
					puantaj.setPersonelDenklestirmeAylik(personelDenklestirmeMap.get(denklestirme.getPersonel().getId()));
					if (puantaj.getPersonelDenklestirmeAylik() == null || !puantaj.getPersonelDenklestirmeAylik().isDenklestirme()) {
						iterator1.remove();
						continue;
					}
					puantaj.setPersonelDenklestirme(denklestirme);
					puantaj.setPdksPersonel(denklestirme.getPersonel());
					puantajDenklestirmeList.add(puantaj);
				}
				String yoneticiPuantajKontrolStr = ortakIslemler.getParameterKey("yoneticiPuantajKontrol");
				boolean yoneticiKontrolEtme = authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || yoneticiPuantajKontrolStr.equals("");
				if (!yoneticiKontrolEtme)
					yoneticiKontrolEtme = yoneticiRolVarmi;
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
				HashMap<Long, Boolean> personelDurumMap = getPersonelDurumMap(aylikPuantajSablon, puantajDenklestirmeList);
				String denklesmeyenBakiyeDurum = denklestirmeAyDurum ? ortakIslemler.getParameterKey("denklesmeyenBakiyeDurum") : "";
				String izinCalismaUyariDurum = denklestirmeAyDurum ? ortakIslemler.getParameterKey("izinCalismaUyariDurum") : "";
				Date sonGun = PdksUtil.tariheGunEkleCikar(aylikPuantajSablon.getSonGun(), 1);
				personelHareketDurum = userHome.hasPermission("personelHareket", "view");
				personelFazlaMesaiDurum = userHome.hasPermission("personelFazlaMesai", "view");
				vardiyaPlaniDurum = userHome.hasPermission("vardiyaPlani", "view");
				boolean denklestirilmeyenDevredenVar = Boolean.FALSE;
				String donemStr = String.valueOf(denklestirmeAy.getYil() * 100 + denklestirmeAy.getAy());
				fazlaMesaiTalepOnayliDurum = Boolean.FALSE;
				if (personelFazlaMesaiDurum && denklestirmeAyDurum && ortakIslemler.getParameterKey("fazlaMesaiTalepDurum").equals("1")) {
					msgFazlaMesaiInfo = ortakIslemler.getParameterKey("fazlaMesaiTalepOnayli");
					fazlaMesaiTalepOnayliDurum = !msgFazlaMesaiInfo.equals("");
				}
				LinkedHashMap<Long, PersonelIzin> izinMap = new LinkedHashMap<Long, PersonelIzin>();
				List<VardiyaGun> offIzinliGunler = new ArrayList<VardiyaGun>();
				kismiOdemeGoster = Boolean.FALSE;
				manuelGirisGoster = "";
				kapiGirisSistemAdi = "";
				if (ikRole || adminRole) {
					manuelGirisGoster = ortakIslemler.getParameterKey("manuelGirisGoster");
					if (manuelGirisGoster.equals("") && authenticatedUser.isAdmin())
						manuelGirisGoster = "background-color: white;font-style: italic !important;";
					kapiGirisSistemAdi = manuelGirisGoster.equals("") ? "" : ortakIslemler.getParameterKey("kapiGirisSistemAdi");
				}
				for (Iterator iterator1 = puantajDenklestirmeList.iterator(); iterator1.hasNext();) {
					AylikPuantaj puantaj = (AylikPuantaj) iterator1.next();
					double negatifBakiyeDenkSaat = 0.0;
					offIzinliGunler.clear();
					PersonelDenklestirme personelDenklestirme = null;
					puantaj.setDonemBitti(Boolean.FALSE);
					puantaj.setAyrikHareketVar(false);
					puantaj.setFiiliHesapla(true);
					saveList.clear();
					Personel personel = puantaj.getPdksPersonel();

					perCalismaModeli = personel.getCalismaModeli();
					if (puantaj.getPersonelDenklestirmeAylik() != null && puantaj.getPersonelDenklestirmeAylik().getCalismaModeliAy() != null)
						perCalismaModeli = puantaj.getPersonelDenklestirmeAylik().getCalismaModeliAy().getCalismaModeli();

					Boolean tarihGecti = Boolean.TRUE;
					Boolean gebemi = Boolean.FALSE, calisiyor = Boolean.FALSE;
					puantaj.setKaydet(Boolean.FALSE);
					personelFazlaMesaiStr = personelFazlaMesaiOrjStr + (personel.getPdks() ? " " : "(Fazla Mesai Yok)");

					puantaj.setSablonAylikPuantaj(aylikPuantajSablon);
					puantaj.setFazlaMesaiHesapla(Boolean.FALSE);

					puantaj.setTrClass(renk ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
					renk = !renk;
					Integer aksamVardiyaSayisi = 0;
					Double aksamVardiyaSaatSayisi = 0d, sabahAksamCikisSaatSayisi = 0d, haftaCalismaSuresi = 0d, resmiTatilSuresi = 0d, offSure = null;
					if (stajerSirket && denklestirmeAyDurum) {
						puantaj.planSureHesapla(tatilGunleriMap);
						offSure = 0.0D;
					}
					TreeMap<String, VardiyaGun> vardiyalar = new TreeMap<String, VardiyaGun>();
					Boolean fazlaMesaiHesapla = Boolean.FALSE;
					cal = Calendar.getInstance();
					puantaj.setHareketler(null);
					List<String> ayrikList = new ArrayList<String>();
					Date sonVardiyaBitZaman = null;
					boolean fazlaMesaiOnayla = false;
					int gunAdet = 0;

					if (puantaj.getVardiyalar() != null) {
						for (VardiyaGun vardiyaGun : puantaj.getVardiyalar()) {
							vardiyaGun.setAyinGunu(vardiyaGun.getVardiyaDateStr().startsWith(donemStr));
							if (vardiyaGun.isAyinGunu() == false || vardiyaGun.getVardiya() == null)
								continue;
							gunAdet++;
							if ((vardiyaGun.getHareketler() == null || vardiyaGun.getHareketler().isEmpty()) && (vardiyaGun.getIzin() != null || vardiyaGun.getVardiya().isIzin() || vardiyaGun.getVardiya().isCalisma() == false))
								continue;
							puantaj.setSonGun(vardiyaGun.getVardiyaDate());
							Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
							if (sonVardiyaBitZaman == null || islemVardiya.getVardiyaTelorans1BitZaman().after(sonVardiyaBitZaman))
								sonVardiyaBitZaman = islemVardiya.getVardiyaTelorans1BitZaman();
						}
						personelDenklestirme = puantaj.getPersonelDenklestirmeAylik();
						if (personelDenklestirme.getDurum()) {
							if (sonVardiyaBitZaman != null)
								fazlaMesaiOnayla = bugun.after(sonVardiyaBitZaman);
						}
						negatifBakiyeDenkSaat = personelDenklestirme.getCalismaModeliAy() != null ? personelDenklestirme.getCalismaModeliAy().getNegatifBakiyeDenkSaat() : 0.0d;
						boolean ekle = (denklestirmeAyDurum || (bakiyeGuncelle != null && bakiyeGuncelle));
						fazlaMesaiHesapla = puantaj.getPdksPersonel().getPdks();
						CalismaModeli calismaModeli = puantaj.getCalismaModeli();
						boolean cumartesiCalisiyor = calismaModeli != null && calismaModeli.getHaftaSonu() > 0.0d;
						for (Iterator iterator = puantaj.getVardiyalar().iterator(); iterator.hasNext();) {
							VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
							vardiyaGun.setAyinGunu(gunList.contains(vardiyaGun.getVardiyaDateStr()));
							if (!vardiyaGun.isAyinGunu()) {
								iterator.remove();
								continue;
							}
							if (vardiyaGun.getId() != null)
								vgIdList.add(vardiyaGun.getId());
							vardiyaGun.setStyle("");
							boolean saatEkle = false;
							vardiyaGun.addResmiTatilSure(vardiyaGun.getGecenAyResmiTatilSure());
							String key = vardiyaGun.getVardiyaDateStr();
							if (vardiyaGun.getPersonel().isCalisiyorGun(vardiyaGun.getVardiyaDate()))
								vardiyaGun.setZamanGelmedi(!bugun.after(vardiyaGun.getIslemVardiya().getVardiyaTelorans2BitZaman()));

							if (ekle && vardiyaGun.getHareketDurum() && vardiyaGun.getId() != null && vardiyaGun.getIslemVardiya() != null) {
								saatEkle = vardiyaGun.getVardiyaDate().before(gunBas);
								if (vardiyaGun.getIslemVardiya().isCalisma()) {
									if (!saatEkle)
										saatEkle = vardiyaGun.getTatil() != null || vardiyaGun.getIslemVardiya().getVardiyaBitZaman().before(bugun);

								}

							}

							if (offSure != null && vardiyaGun.getVardiya() != null && vardiyaGun.getIzin() == null && vardiyaGun.getVardiya().isOffGun()) {
								cal.setTime(vardiyaGun.getVardiyaDate());
								int haftaGunu = cal.get(Calendar.DAY_OF_WEEK);
								if (haftaGunu != Calendar.SATURDAY && haftaGunu != Calendar.SUNDAY)
									offSure += 9;

							}
							if (personel.getSskCikisTarihi().before(puantaj.getSonGun()) || puantaj.getSonGun().before(bugun)) {
								if (denklestirmeAyDurum && vardiyaGun.getVardiya() != null && vardiyaGun.isZamanGelmedi()) {
									// hataYok = Boolean.FALSE;
									puantaj.setDonemBitti(Boolean.FALSE);
								}
							}

							vardiyaGun.setLinkAdresler(null);
							vardiyaGun.setOnayli(Boolean.TRUE);
							vardiyaGun.setHataliDurum(Boolean.FALSE);
							vardiyaGun.setPersonel(puantaj.getPdksPersonel());
							vardiyaGun.setFiiliHesapla(fazlaMesaiHesapla);
							if (key.equals("20220103"))
								logger.debug(vardiyaGun.getVardiyaKeyStr());
							if (vardiyaGun.getVardiya() != null && vardiyaGun.getVardiyaDate().getTime() >= puantaj.getIlkGun().getTime() && vardiyaGun.getVardiyaDate().getTime() <= puantaj.getSonGun().getTime()) {
								paramsMap.put("fazlaMesaiHesapla", fazlaMesaiHesapla);
								paramsMap.put("aksamVardiyaSayisi", aksamVardiyaSayisi);
								paramsMap.put("aksamVardiyaSaatSayisi", aksamVardiyaSaatSayisi);
								paramsMap.put("resmiTatilSuresi", resmiTatilSuresi);
								paramsMap.put("fazlaMesaiHesapla", fazlaMesaiHesapla);
								paramsMap.put("haftaCalismaSuresi", haftaCalismaSuresi);
								paramsMap.put("sabahAksamCikisSaatSayisi", sabahAksamCikisSaatSayisi);
								vardiyaGun.setFazlaMesaiTalepOnayliDurum(Boolean.FALSE);

								vardiyaGunKontrol(puantaj, vardiyaGun, paramsMap);
								if (izinCalismaUyariDurum.equals("1") && vardiyaGun.getIzin() != null) {
									PersonelIzin izin = vardiyaGun.getIzin();
									if (vardiyaGun.isHareketHatali()) {
										if (ikRole && izin.getOrjIzin() != null) {
											Long izinId = izin.getOrjIzin().getId();
											PersonelIzin orjIzin = izinMap.containsKey(izinId) ? izinMap.get(izinId) : izin.getOrjIzin();
											if (!izinMap.containsKey(izinId)) {
												orjIzin.setCalisilanGunler(null);
												izinMap.put(izinId, orjIzin);
											}
											orjIzin.addCalisilanGunler(vardiyaGun);

										}
									} else if (vardiyaGun.getTatil() == null && vardiyaGun.getVardiya().isOffGun()) {
										int haftaGunu = vardiyaGun.getHaftaninGunu();
										if ((vardiyaGun.isHaftaIci()) || (cumartesiCalisiyor && haftaGunu == Calendar.SATURDAY)) {
											vardiyaGun.setStyle("color:red;");
											offIzinliGunler.add(vardiyaGun);
										}

									}
								}
								if (vardiyaGun.isAyrikHareketVar()) {
									ayrikList.add(PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), "d MMMMM EEEEEE"));
								}
								fazlaMesaiHesapla = (Boolean) paramsMap.get("fazlaMesaiHesapla");
								aksamVardiyaSayisi = (Integer) paramsMap.get("aksamVardiyaSayisi");
								resmiTatilSuresi = (Double) paramsMap.get("resmiTatilSuresi");
								aksamVardiyaSaatSayisi = (Double) paramsMap.get("aksamVardiyaSaatSayisi");
								sabahAksamCikisSaatSayisi = (Double) paramsMap.get("sabahAksamCikisSaatSayisi");
								haftaCalismaSuresi = (Double) paramsMap.get("haftaCalismaSuresi");
								paramsMap.clear();
							}
							Boolean hareketDurum = vardiyaGun.getDurum(), saveVardiyaGun = Boolean.FALSE;
							hareketDurum = vardiyaGun.getHareketDurum() && saatEkle;
							if (saatEkle) {
								VardiyaSaat vardiyaSaat = vardiyaGun.getVardiyaSaat();
								if (vardiyaSaat == null)
									vardiyaSaat = new VardiyaSaat();
								vardiyaSaat.setGuncellendi(false);
								double normalSure = 0.0d;
								if (vardiyaGun.getIslemVardiya() != null && vardiyaGun.getIzin() == null && vardiyaGun.getIslemVardiya().isCalisma())
									normalSure = vardiyaGun.getIslemVardiya().getNetCalismaSuresi();
								if (denklestirmeAyDurum) {
									if (hareketDurum.equals(Boolean.FALSE)) {
										vardiyaSaat.setResmiTatilSure(0.0d);
										vardiyaSaat.setAksamVardiyaSaatSayisi(0.0d);
									} else {
										vardiyaSaat.setResmiTatilSure(vardiyaGun.getResmiTatilSure());
										vardiyaSaat.setAksamVardiyaSaatSayisi(vardiyaGun.getAksamVardiyaSaatSayisi());
									}
									if (hareketDurum.equals(Boolean.TRUE) && vardiyaGun.isZamanGelmedi() == false && vardiyaGun.getHareketler() != null) {
										vardiyaSaat.setCalismaSuresi(vardiyaGun.getCalismaSuresi());
									} else {
										vardiyaSaat.setCalismaSuresi(0.0d);
									}
								}

								if (vardiyaSaat.getId() != null)
									vardiyaSaat.setNormalSure(normalSure);
								if (vardiyaSaat.isGuncellendi()) {
									if (vardiyaSaat.getId() == null)
										vardiyaSaat.setNormalSure(normalSure);
									else
										vardiyaSaat.setGuncellemeTarihi(bugun);
									saveList.add(vardiyaSaat);
									if (vardiyaGun.getVardiyaSaat() == null) {
										vardiyaGun.setVardiyaSaat(vardiyaSaat);
										saveVardiyaGun = Boolean.TRUE;

									}
								} else if (vardiyaSaat.getId() == null)
									vardiyaGun.setVardiyaSaat(null);
							}
							if (denklestirmeAyDurum && vardiyaGun.getVardiya() != null && vardiyaGun.getId() != null && !vardiyaGun.getDurum().equals(hareketDurum)) {
								vardiyaGun.setDurum(hareketDurum);
								vardiyaGun.setGuncellemeTarihi(bugun);
								saveVardiyaGun = Boolean.TRUE;
							}
							if (saveVardiyaGun)
								saveList.add(vardiyaGun);
							vardiyalar.put(vardiyaGun.getVardiyaKeyStr(), vardiyaGun);

							Vardiya vardiya = vardiyaGun.getIslemVardiya();
							vardiyaGun.setTitleStr(null);
							if (vardiya != null) {
								if (authenticatedUser.isAdmin()) {
									String titleSte = fazlaMesaiOrtakIslemler.getFazlaMesaiSaatleri(vardiyaGun);
									vardiyaGun.setTitleStr(titleSte);
									vardiyaGun.addLinkAdresler(titleSte);
								}

							}

							if (vardiyaGun.isZamanGelmedi() && vardiyaGun.getHareketler() != null) {
								for (Iterator iterator2 = vardiyaGun.getHareketler().iterator(); iterator2.hasNext();) {
									HareketKGS kgsHareket = (HareketKGS) iterator2.next();
									if (kgsHareket.isGecerliDegil())
										iterator2.remove();
								}
							}
						}
						if (!offIzinliGunler.isEmpty()) {
							Personel izinSahibi = puantaj.getPdksPersonel();
							String izinStr = izinSahibi.getPdksSicilNo() + " " + ortakIslemler.personelNoAciklama() + " " + izinSahibi.getAdSoyad() + " ait izinde ";
							String virgul = "";
							for (Iterator iterator = offIzinliGunler.iterator(); iterator.hasNext();) {
								VardiyaGun vGun = (VardiyaGun) iterator.next();
								izinStr += virgul + PdksUtil.convertToDateString(vGun.getVardiyaDate(), "d MMM EEEEE");
								virgul = ", ";
							}
							if (izinStr.indexOf(",") > 0) {
								int ind = izinStr.lastIndexOf(",");
								String str1 = izinStr.substring(0, ind), str2 = izinStr.substring(ind + 1);
								izinStr = str1 + " ve" + str2 + " günlerinde ";
							} else
								izinStr += " gününde ";
							izinStr = PdksUtil.replaceAllManuel(izinStr + " hafta içinde OFF planlanmıştır.", "  ", " ");
							PdksUtil.addMessageAvailableWarn(izinStr);
						}

						if (!saveList.isEmpty()) {
							for (Iterator iterator = saveList.iterator(); iterator.hasNext();) {
								Object object = (Object) iterator.next();
								pdksEntityController.saveOrUpdate(session, entityManager, object);
							}
							session.flush();
						}
					}
					if (offSure != null)
						puantaj.setOffSure(offSure);
					puantaj.setResmiTatilToplami(0d);
					puantaj.setHaftaCalismaSuresi(0d);
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
					ortakIslemler.puantajHaftalikPlanOlustur(Boolean.TRUE, null, vardiyalar, aylikPuantajSablon, puantaj);
					personelDenklestirme = puantaj.getPersonelDenklestirmeAylik();
					if (personelDenklestirme == null)
						continue;

					personelDenklestirme.setGuncellendi(Boolean.FALSE);
					if (gecenAy == null && personelDenklestirme.getPersonelDenklestirmeGecenAy() != null && personelDenklestirme.getPersonelDenklestirmeGecenAy().getDenklestirmeAy() != null)
						gecenAy = personelDenklestirme.getPersonelDenklestirmeGecenAy().getDenklestirmeAy();
					try {
						if (personelDenklestirme.isOnaylandi()) {
							// personelDenklestirme = ortakIslemler.aylikPlanSureHesapla(puantaj, !personelDenklestirme.isKapandi(), yemekAraliklari);
							yemekAraliklari = ortakIslemler.getYemekList(session);
							if (personelDurumMap.containsKey(personelDenklestirme.getId()))
								puantaj.setFazlaMesaiIzinKontrol(Boolean.FALSE);
							personelDenklestirme = ortakIslemler.aylikPlanSureHesapla(puantaj, !personelDenklestirme.isKapandi(authenticatedUser), yemekAraliklari, tatilGunleriMap, session);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (!fazlaMesaiIzinKullan)
						fazlaMesaiIzinKullan = personelDenklestirme.getFazlaMesaiIzinKullan() != null && personelDenklestirme.getFazlaMesaiIzinKullan();
					double resmiTatilToplami = puantaj.getResmiTatilToplami();
					double kesilenSure = personelDenklestirme.getKesilenSure();
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
								if (vardiyaGun.getHaftaCalismaSuresi() > 0) {
									if (!haftaTatilVar)
										haftaTatilVar = Boolean.TRUE;
								}
							}

							if (vardiyaGun.getResmiTatilSure() > 0) {
								if (!resmiTatilVar)
									resmiTatilVar = Boolean.TRUE;
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
						if (personelDenklestirme.getPersonelDenklestirmeGecenAy() != null)
							puantaj.setDevredenSure(personelDenklestirme.getPersonelDenklestirmeGecenAy().getDevredenSure());
					}
					if (denklestirmeAyDurum == false && !haftaTatilDurum.equals("1"))
						haftaCalismaSuresi = 0.0d;
					puantaj.setHaftaCalismaSuresi(haftaCalismaSuresi);
					if (!gebeGoster)
						gebeGoster = puantaj.isGebeDurum();

					if (!sutIzniGoster)
						sutIzniGoster = personelDenklestirme != null && personelDenklestirme.getSutIzniDurum() != null && personelDenklestirme.getSutIzniDurum();
					if (!partTimeGoster)
						partTimeGoster = personelDenklestirme != null && personelDenklestirme.getPartTime() != null && personelDenklestirme.getPartTime();
					// if (/*personelDenklestirme.isErpAktarildi() ||*/ !personelDenklestirme.getDenklestirmeAy().isDurumu()) {
					if (personelDenklestirme.isErpAktarildi() || !denklestirmeAyDurum) {
						boolean buAyIstenAyrildi = false;

						if (!personelDenklestirme.getPersonel().isCalisiyorGun(sonGun)) {
							cal.setTime(personelDenklestirme.getPersonel().getSonCalismaTarihi());
							int ayrilmaYil = cal.get(Calendar.YEAR), ayrilmaAy = cal.get(Calendar.MONTH) + 1;
							buAyIstenAyrildi = denklestirmeAy.getAy() == ayrilmaAy && ayrilmaYil == denklestirmeAy.getYil();
						}
						if (personelDenklestirme.isKapandi(authenticatedUser) && buAyIstenAyrildi == false) {
							double fazlaMesaiSure = puantaj.getFazlaMesaiSure();
							if (personelDenklestirme.isErpAktarildi() && fazlaMesaiSure != personelDenklestirme.getOdenecekSure())
								logger.debug(personelDenklestirme.getPersonel().getPdksSicilNo() + " " + fazlaMesaiSure);
							if (personelDenklestirme.isErpAktarildi() && personelDenklestirme.getOdenecekSure() > 0)
								puantaj.setFazlaMesaiSure(personelDenklestirme.getOdenecekSure());
							else
								personelDenklestirme.setOdenenSure(fazlaMesaiSure);
							if (bakiyeGuncelle == null || bakiyeGuncelle.equals(Boolean.FALSE) || puantaj.isFazlaMesaiHesapla() == false) {
								puantaj.setDevredenSure(personelDenklestirme.getDevredenSure());
								puantaj.setKesilenSure(personelDenklestirme.getKesilenSure());
							}

							else if (denklestirmeAyDurum || (bakiyeGuncelle != null && bakiyeGuncelle)) {
								personelDenklestirme.setDevredenSure(puantaj.getDevredenSure());
								personelDenklestirme.setFazlaMesaiSure(puantaj.getAylikNetFazlaMesai());
								personelDenklestirme.setHaftaCalismaSuresi(puantaj.getHaftaCalismaSuresi());
								if (denklestirmeAyDurum) {
									personelDenklestirme.setHesaplananSure(puantaj.getSaatToplami());
									personelDenklestirme.setPlanlanSure(puantaj.getPlanlananSure());
								}

							}
							puantaj.setResmiTatilToplami(personelDenklestirme.getResmiTatilSure());

						} else if (denklestirmeAyDurum) {
							personelDenklestirme.setPlanlanSure(puantaj.getPlanlananSure());
							personelDenklestirme.setHesaplananSure(puantaj.getSaatToplami());
							personelDenklestirme.setFazlaMesaiSure(puantaj.getAylikNetFazlaMesai());
							personelDenklestirme.setDevredenSure(puantaj.getDevredenSure());
							personelDenklestirme.setResmiTatilSure(puantaj.getResmiTatilToplami());
							personelDenklestirme.setHaftaCalismaSuresi(puantaj.getHaftaCalismaSuresi());
							personelDenklestirme.setKesilenSure(puantaj.getKesilenSure());
							personelDenklestirme.setDurum(puantaj.isFazlaMesaiHesapla());

						}
						if (!denklestirmeAyDurum) {
							aksamVardiyaSayisi = personelDenklestirme.getAksamVardiyaSayisi().intValue();
							aksamVardiyaSaatSayisi = personelDenklestirme.getAksamVardiyaSaatSayisi();
							haftaCalismaSuresi = personelDenklestirme.getHaftaCalismaSuresi();
						}
					} else {
						personelDenklestirme.setDurum(puantaj.isFazlaMesaiHesapla());
						if (personelDenklestirme.getDurum()) {
							personelDenklestirme.setPlanlanSure(puantaj.getPlanlananSure());
							personelDenklestirme.setHesaplananSure(puantaj.getSaatToplami());
							// personelDenklestirme.setHaftaCalismaSuresi(puantaj.getHaftaCalismaSuresi());
						} else {
							personelDenklestirme.setPlanlanSure(0d);
							personelDenklestirme.setHesaplananSure(0d);
						}

						aksamVardiyaSaatSayisi += sabahAksamCikisSaatSayisi;
						if (!fazlaMesaiHesapla || !calisiyor) {
							puantaj.setFazlaMesaiSure(0d);
							puantaj.setDevredenSure(0d);
							puantaj.setResmiTatilToplami(0d);
							puantaj.setHaftaCalismaSuresi(0d);
							if (denklestirmeAyDurum) {
								if (!personelDenklestirme.isKapandi(authenticatedUser))
									personelDenklestirme.setDevredenSure(null);
								personelDenklestirme.setResmiTatilSure(0d);
								personelDenklestirme.setOdenenSure(0d);

							}

						} else {
							if (denklestirmeAyDurum) {
								if (!puantaj.getPersonelDenklestirmeAylik().isOnaylandi()) {
									puantaj.setHaftaCalismaSuresi(0d);
									puantaj.setResmiTatilToplami(0d);
								}

								boolean partTime = stajerSirket || (personel.getPartTime() != null && personel.getPartTime().booleanValue());
								if ((personelDenklestirme.getFazlaMesaiOde().booleanValue() == false || puantaj.getFazlaMesaiSure() > 0) && calisiyor && partTime == false) {

								} else if (partTime) {
									personelDenklestirme.setOdenenSure(0D);
									personelDenklestirme.setResmiTatilSure(0D);
									if (personelDenklestirme.getPersonelDenklestirmeGecenAy() != null)
										personelDenklestirme.setDevredenSure(personelDenklestirme.getPersonelDenklestirmeGecenAy().getDevredenSure());
									else
										personelDenklestirme.setDevredenSure(0D);
									personelDenklestirme.setAksamVardiyaSaatSayisi(0D);
									personelDenklestirme.setAksamVardiyaSayisi(0D);
									personelDenklestirme.setHesaplananSure(0D);
								}

							}
							if (!hataYok)
								setHataYok(fazlaMesaiHesapla && tarihGecti);
						}
					}
					if (calisiyor) {
						kayitVar = true;
						if (sicilNo.trim().length() > 0 || denklestirmeAyDurum == false || hataliPuantajGoster == false || puantaj.isFazlaMesaiHesapla() == false)
							puantajList.add(puantaj);
					}

					else {
						iterator1.remove();
						continue;
					}

					if (denklestirmeAyDurum) {
						if (personelDenklestirme.getEgitimSuresiAksamGunSayisi() != null && (personelDenklestirme.getPersonel().getEgitimDonemi() == null || !personelDenklestirme.getPersonel().getEgitimDonemi())) {

							try {
								personelDenklestirme.setEgitimSuresiAksamGunSayisi(null);
								personelDenklestirme.setGuncellendi(Boolean.TRUE);
							} catch (Exception ee) {
								ee.printStackTrace();
							}

						}

						if (aksamVardiyaBasSaat == null || aksamVardiyaBitSaat == null) {
							aksamVardiyaSaatSayisi = 0d;
							aksamVardiyaSayisi = 0;

						} else if (personelDenklestirme.getEgitimSuresiAksamGunSayisi() != null && personelDenklestirme.getEgitimSuresiAksamGunSayisi() >= 0) {
							aksamVardiyaSaatSayisi = 0d;
							if (aksamVardiyaSayisi > personelDenklestirme.getEgitimSuresiAksamGunSayisi())
								aksamVardiyaSayisi = personelDenklestirme.getEgitimSuresiAksamGunSayisi();
						}
						if (!onayla)
							onayla = personel.getPdks() && !personelDenklestirme.isErpAktarildi() && puantaj.isDonemBitti() && puantaj.isFazlaMesaiHesapla() && personelDenklestirme.isOnaylandi();

					} else {
						if (!(ikRole))
							resmiTatilToplami = personelDenklestirme.getResmiTatilSure();
						else
							personelDenklestirme.setResmiTatilSure(resmiTatilToplami);
						aksamVardiyaSaatSayisi = personelDenklestirme.getAksamVardiyaSaatSayisi();
						aksamVardiyaSayisi = personelDenklestirme.getAksamVardiyaSayisi().intValue();
						haftaCalismaSuresi = personelDenklestirme.getHaftaCalismaSuresi();
					}
					if (denklestirmeAyDurum) {
						kesilenSure = 0;
						if (negatifBakiyeDenkSaat < 0.0d) {
							double normalCalisma = personelDenklestirme.getCalismaModeliAy().getCalismaModeli().getHaftaIci();
							if (calisiyor == false) {
								if (puantaj.getDevredenSure() < 0) {
									kesilenSure = -puantaj.getDevredenSure();
									if (!denklestirmeAy.isKesintiYok()) {
										if (denklestirmeAy.isKesintiSaat()) {
											puantaj.setDevredenSure(kesilenSure + puantaj.getDevredenSure());
										} else if (denklestirmeAy.isKesintiGun()) {
											double kesilecekSaatSure = (new Double(-puantaj.getDevredenSure() / normalCalisma)).intValue() * normalCalisma;
											kesilenSure = kesilecekSaatSure / normalCalisma;
											puantaj.setDevredenSure(kesilecekSaatSure + puantaj.getDevredenSure());
										}
									}
								}
							} else if (puantaj.getDevredenSure() <= negatifBakiyeDenkSaat && puantaj.getGecenAyFazlaMesai(authenticatedUser) < 0) {
								if (puantaj.getDevredenSure() < -normalCalisma) {
									double kesilecekSaatSure = (new Double(-puantaj.getDevredenSure() / normalCalisma)).intValue() * normalCalisma;
									if (!denklestirmeAy.isKesintiYok()) {
										if (denklestirmeAy.isKesintiSaat()) {
											kesilenSure = kesilecekSaatSure;
											puantaj.setDevredenSure(kesilecekSaatSure + puantaj.getDevredenSure());
										} else if (denklestirmeAy.isKesintiGun()) {
											kesilenSure = kesilecekSaatSure / normalCalisma;
											puantaj.setDevredenSure(kesilecekSaatSure + puantaj.getDevredenSure());
										}

									} else
										kesilenSure = kesilecekSaatSure;

								}

							}
						}
					}
					if (!kesilenSureGoster)
						kesilenSureGoster = kesilenSure > 0.0d;
					puantaj.setKesilenSure(kesilenSure);
					int yarimYuvarla = puantaj.getYarimYuvarla();
					puantaj.setResmiTatilToplami(PdksUtil.setSureDoubleTypeRounded(resmiTatilToplami, yarimYuvarla));
					if (denklestirmeAyDurum && puantaj.isFazlaMesaiHesapla() && personelDenklestirme.getPersonelDenklestirmeGecenAy() != null && personel.getIseGirisTarihi().before(aylikPuantajSablon.getIlkGun())) {
						PersonelDenklestirme personelDenklestirmeGecenAy = personelDenklestirme.getPersonelDenklestirmeGecenAy();
						DenklestirmeAy denklestirmeAyGecen = personelDenklestirmeGecenAy.getDenklestirmeAy();
						if (!personelDenklestirmeGecenAy.isOnaylandi()) {
							puantaj.setFazlaMesaiHesapla(Boolean.FALSE);
							PdksUtil.addMessageAvailableError(denklestirmeAyGecen.getAyAdi() + " " + denklestirmeAyGecen.getYil() + " " + personel.getPdksSicilNo() + " - " + personel.getAdSoyad() + " " + " çalışma planı onaylanmadı!");
						} else if (!personelDenklestirmeGecenAy.getDurum()) {
							puantaj.setFazlaMesaiHesapla(Boolean.FALSE);
							PdksUtil.addMessageAvailableError(denklestirmeAyGecen.getAyAdi() + " " + denklestirmeAyGecen.getYil() + " " + personel.getPdksSicilNo() + " - " + personel.getAdSoyad() + " fazla mesaisi onaylanmadı!");
						}

					}
					if (bakiyeGuncelle != null && bakiyeGuncelle && puantaj.isFazlaMesaiHesapla()) {
						personelDenklestirme.setDurum(puantaj.isFazlaMesaiHesapla());
						if (personelDenklestirme.isGuncellendi() && !authenticatedUser.isAdmin()) {
							personelDenklestirme.setGuncellemeTarihi(new Date());
							personelDenklestirme.setGuncelleyenUser(authenticatedUser);
						}
						personelDenklestirme.setGuncellendi(Boolean.TRUE);
					}
					boolean denklestirilmeyenPersonelDevredenVar = Boolean.FALSE;
					if (!denklesmeyenBakiyeDurum.equals("") && personel.isCalisiyorGun(sonGun) && personelDenklestirme.getDurum() && personelDenklestirme.getDevredenSure() != null && personelDenklestirme.getDevredenSure().doubleValue() < 0.0d
							&& personelDenklestirme.getPersonelDenklestirmeGecenAy() != null) {
						PersonelDenklestirme personelDenklestirmeGecenAy = personelDenklestirme.getPersonelDenklestirmeGecenAy();
						if (personelDenklestirmeGecenAy.getDevredenSure() != null && personelDenklestirmeGecenAy.getDevredenSure().doubleValue() < 0.0d) {
							if (denklesmeyenBakiyeDurum.equalsIgnoreCase("U") || denklesmeyenBakiyeDurum.equalsIgnoreCase("B")) {
								denklestirilmeyenPersonelDevredenVar = Boolean.TRUE;
								denklestirilmeyenDevredenVar = Boolean.TRUE;
								if (denklesmeyenBakiyeDurum.equalsIgnoreCase("B")) {
									puantaj.setFazlaMesaiHesapla(Boolean.FALSE);
									personelDenklestirme.setGuncellendi(Boolean.TRUE);
								}
							}

						}

					}
					puantaj.setDenklestirilmeyenDevredenVar(denklestirilmeyenPersonelDevredenVar);
					if (personelDenklestirme.isGuncellendi()) {
						if ((bakiyeGuncelle != null && bakiyeGuncelle) || puantaj.isFazlaMesaiHesapla() != personelDenklestirme.getDurum() || (gecenAy != null && gecenAy.getDurum().equals(Boolean.FALSE))) {
							if (puantaj.isFazlaMesaiHesapla() != personelDenklestirme.getDurum())
								personelDenklestirme.setDurum(puantaj.isFazlaMesaiHesapla());
							pdksEntityController.saveOrUpdate(session, entityManager, personelDenklestirme);
							flush = Boolean.TRUE;
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
					if (!denklestirmeDinamikAlanlar.isEmpty()) {
						if (puantaj.getDinamikAlanMap() == null)
							puantaj.setDinamikAlanMap(new TreeMap<Long, PersonelDenklestirmeDinamikAlan>());
						else
							puantaj.getDinamikAlanMap().clear();
					}
					if (devamlilikPrimi != null) {
						String key = PersonelDenklestirmeDinamikAlan.getKey(devamlilikPrimi, personelDenklestirme);
						if (devamlilikPrimiMap.containsKey(key)) {
							PersonelDenklestirmeDinamikAlan denklestirmeDinamikAlan = devamlilikPrimiMap.get(key);
							puantaj.getDinamikAlanMap().put(devamlilikPrimi.getId(), denklestirmeDinamikAlan);
							if (devamlilikPrimi.getDurum() && denklestirmeAyDurum) {
								try {
									if (!flush)
										flush = devamlilikPrimiHesapla(puantaj, denklestirmeDinamikAlan);
								} catch (Exception ed) {
									logger.error(ed);
									ed.printStackTrace();
								}

							}
						}

					}

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

					if (sonVardiyaBitZaman != null) {
						if (puantaj.isFazlaMesaiHesapla() && personelDenklestirme.getDurum()) {
							puantaj.setDonemBitti(bugun.after(sonVardiyaBitZaman));

						}
					} else if (fazlaMesaiOnayla || gunAdet > 0)
						puantaj.setDonemBitti(personel.getSskCikisTarihi().before(puantaj.getSonGun()) || puantaj.getSonGun().before(bugun));
					if (AylikPuantaj.getGebelikGuncelle() && denklestirmeAyDurum && puantaj.isFazlaMesaiHesapla() && puantaj.isGebeDurum() && puantaj.getCalisilanGunSayisi() > 0 && personelDenklestirme.getSutIzniSaatSayisi().doubleValue() == 0.0d) {
						puantaj.setFazlaMesaiHesapla(Boolean.FALSE);
						if (personelDenklestirme.getDurum()) {
							personelDenklestirme.setDurum(Boolean.FALSE);
							pdksEntityController.saveOrUpdate(session, entityManager, personelDenklestirme);
							flush = true;
						}
						if (ikRole)
							PdksUtil.addMessageAvailableWarn(personel.getPdksSicilNo() + " " + personel.getAdSoyad() + " gebelik ÇGS girişi yapınız! ");

					}
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
					if (denklestirmeAyDurum == false && personelDenklestirme != null && personelDenklestirme.getDurum()) {
						boolean savePersonelDenklestirme = false;

						if (ikRole) {
							boolean odemeVar = personelDenklestirme.getOdenenSure() > 0.0d;
							if (personelDenklestirme.getHesaplananSure().equals(0.0D) && (odemeVar || puantaj.getSaatToplami() > 0.0d)) {
								personelDenklestirme.setHesaplananSure(puantaj.getSaatToplami());
								savePersonelDenklestirme = true;
							}
							if (personelDenklestirme.getPlanlanSure().equals(0.0D) && (odemeVar || puantaj.getPlanlananSure() > 0.0d)) {
								personelDenklestirme.setPlanlanSure(puantaj.getPlanlananSure());
								savePersonelDenklestirme = true;
							}
						}
						if (savePersonelDenklestirme) {
							pdksEntityController.saveOrUpdate(session, entityManager, personelDenklestirme);
							flush = true;
						}
					}
					if (personelDenklestirme.getDurum() && personelDenklestirme.isOnaylandi()) {
						if (fazlaMesaiOnayDurum == false)
							fazlaMesaiOnayDurum = puantaj.isDonemBitti();
					}
					try {
						if (puantaj.isDonemBitti() && personelDenklestirme.isFazlaMesaiIzinKullanacak() && personelDenklestirme.getKismiOdemeSure() != null && personelDenklestirme.getKismiOdemeSure().doubleValue() > 0) {
							kismiOdemeGoster = ikRole;
							if (denklestirmeAyDurum && puantaj.getFazlaMesaiSure() == 0.0d) {
								PdksUtil.addMessageAvailableWarn(personel.getPdksSicilNo() + "  " + personel.getAdSoyad() + " Kısmi Ödenecek :" + authenticatedUser.sayiFormatliGoster(personelDenklestirme.getKismiOdemeSure()) + " Devreden Süre : "
										+ authenticatedUser.sayiFormatliGoster(personelDenklestirme.getDevredenSure()));
							}
						}
					} catch (Exception ex) {
						logger.error(ex);
						ex.printStackTrace();
					}

				}
				if (!devamlilikPrimiMap.isEmpty()) {
					TreeMap<Long, Tanim> dinamikTanimMap = new TreeMap<Long, Tanim>();
					for (String key : devamlilikPrimiMap.keySet()) {
						PersonelDenklestirmeDinamikAlan denklestirmeDinamikAlan = devamlilikPrimiMap.get(key);
						if (denklestirmeDinamikAlan.getIslemDurum())
							dinamikTanimMap.put(denklestirmeDinamikAlan.getAlan().getId(), denklestirmeDinamikAlan.getAlan());
					}
					denklestirmeDinamikAlanlar.clear();
					if (!dinamikTanimMap.isEmpty())
						denklestirmeDinamikAlanlar.addAll(PdksUtil.sortTanimList(null, new ArrayList(dinamikTanimMap.values())));
					dinamikTanimMap = null;
				}

				if (denklestirilmeyenDevredenVar) {
					if (denklesmeyenBakiyeDurum.equalsIgnoreCase("B"))
						PdksUtil.addMessageAvailableError("Geçen aydan devreden negatif bakiye denkleştirilemedi!");
					else
						PdksUtil.addMessageAvailableWarn("Geçen aydan devreden negatif bakiye denkleştirilemedi!");
				}
				if (denklestirmeAyDurum) {
					List<String> vGunList = ciftBolumCalisanKontrol(puantajList);
					if (!izinMap.isEmpty())
						try {
							izinCalismaUyariMesajiOlustur(vGunList, izinMap);
						} catch (Exception e) {
						}
				}

				izinMap = null;
				if (!vgIdList.isEmpty()) {
					map.clear();
					map.put("vardiyaGun.id", vgIdList);
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<FazlaMesaiTalep> fList = pdksEntityController.getObjectByInnerObjectList(map, FazlaMesaiTalep.class);
					if (!fList.isEmpty()) {
						fList = PdksUtil.sortListByAlanAdi(fList, "baslangicZamani", true);
						for (Iterator iterator = fList.iterator(); iterator.hasNext();) {
							FazlaMesaiTalep fazlaMesaiTalep = (FazlaMesaiTalep) iterator.next();
							if (fazlaMesaiTalep.getOnayDurumu() == FazlaMesaiTalep.ONAY_DURUM_RED || fazlaMesaiTalep.getDurum() == null || fazlaMesaiTalep.getDurum().booleanValue() == false)
								iterator.remove();
							else {
								Long key = fazlaMesaiTalep.getVardiyaGun().getId();
								List<FazlaMesaiTalep> icListe = fmtMap.containsKey(key) ? fmtMap.get(key) : new ArrayList<FazlaMesaiTalep>();
								if (icListe.isEmpty())
									fmtMap.put(key, icListe);
								icListe.add(fazlaMesaiTalep);
							}
						}

					}
					fList = null;
				}

				paramsMap = null;
				if (!saveGenelList.isEmpty()) {
					for (Object obj : saveGenelList) {
						pdksEntityController.saveOrUpdate(session, entityManager, obj);
					}
					flush = true;
				}
				if (puantajList.isEmpty()) {
					if (kayitVar == false)
						PdksUtil.addMessageAvailableWarn("Fazla mesai kaydı bulunmadı!");
					else if (hataliPuantajGoster)
						PdksUtil.addMessageAvailableWarn("Hatalı personel kaydı bulunmadı!");

				}

				else if (flush)
					session.flush();

			} else {
				if (fazlaMesaiMap == null)
					fazlaMesaiMap = new TreeMap<String, Tanim>();
				else
					fazlaMesaiMap.clear();
			}
			if (denklestirmeAyDurum && !hataYok) {
				setHataYok(sonCikisZamani != null && bugun.after(sonCikisZamani));
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

		TreeMap<String, Object> ozetMap = izinGoster ? fazlaMesaiOrtakIslemler.getIzinOzetMap(null, puantajList, izinGoster) : new TreeMap<String, Object>();
		izinTipiVardiyaList = ozetMap.containsKey("izinTipiVardiyaList") ? (List<Vardiya>) ozetMap.get("izinTipiVardiyaList") : new ArrayList<Vardiya>();
		izinTipiPersonelVardiyaMap = ozetMap.containsKey("izinTipiPersonelVardiyaMap") ? (TreeMap<String, TreeMap<String, List<VardiyaGun>>>) ozetMap.get("izinTipiPersonelVardiyaMap") : new TreeMap<String, TreeMap<String, List<VardiyaGun>>>();
		if (izinTipiVardiyaList != null && !izinTipiVardiyaList.isEmpty()) {
			fazlaMesaiOrtakIslemler.personelIzinAdetleriOlustur(puantajList, izinTipiVardiyaList, izinTipiPersonelVardiyaMap);
		}
		try {
			pageSize = Integer.parseInt(ortakIslemler.getParameterKey("puantajPageSize"));
		} catch (Exception e) {
			pageSize = 0;
		}
		if (pageSize < 20 || pageSize > puantajList.size())
			pageSize = puantajList.size() + 1;
		hatalariAyikla = false;
		if (denklestirmeAyDurum) {

			if (puantajList.size() > pageSize) {
				Date bitTarih = new Date();
				Date fark = new Date(bitTarih.getTime() - basTarih.getTime());
				String str = yil + " " + denklestirmeAy.getAyAdi() + " " + puantajList.size() + " adet " + PdksUtil.setTurkishStr((seciliBolum != null ? seciliBolum.getAciklama() + " personeli " : " personel ") + authenticatedUser.getAdSoyad());
				logger.info(str + " --> " + PdksUtil.convertToDateString(basTarih, "HH:mm:ss") + " - " + PdksUtil.convertToDateString(bitTarih, "HH:mm:ss") + " : " + PdksUtil.convertToDateString(fark, "mm:ss"));
			}
		}

		if (denklestirmeAyDurum == false)
			fazlaMesaiOnayDurum = Boolean.FALSE;
		if (gecenAy != null && gecenAy.getDurum().equals(Boolean.TRUE)) {
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
					PersonelDenklestirme pd = puantaj.getPersonelDenklestirmeAylik();
					boolean kaydet = pd.getDurum();
					if (kaydet) {
						kaydet = (!pd.isKapandi(authenticatedUser) && (PdksUtil.isDoubleDegisti(pd.getAksamVardiyaSaatSayisi(), puantaj.getAksamVardiyaSaatSayisi()) || PdksUtil.isDoubleDegisti(pd.getAksamVardiyaSayisi(), (double) puantaj.getAksamVardiyaSayisi()) || PdksUtil.isDoubleDegisti(
								pd.getDevredenSure(), puantaj.getDevredenSure())))
								|| PdksUtil.isDoubleDegisti(pd.getHaftaCalismaSuresi(), puantaj.getHaftaCalismaSuresi())
								|| PdksUtil.isDoubleDegisti(pd.getResmiTatilSure(), puantaj.getResmiTatilToplami())
								|| PdksUtil.isDoubleDegisti(pd.getOdenenSure(), puantaj.getFazlaMesaiSure())
								|| PdksUtil.isDoubleDegisti(pd.getKesilenSure(), puantaj.getKesilenSure());
					}
					puantaj.setKaydet(kaydet);
					if (puantaj.isKaydet())
						onayla = hataYok;
				}
				if (onayla) {
					mailGonder = Boolean.FALSE;
					try {
						fazlaMesaiOnaylaDevam(Boolean.FALSE);
					} catch (Exception eo) {
						logger.error(eo);
						eo.printStackTrace();
					}

				}
			}
		}

		if (denklestirmeAyDurum && puantajList != null && !puantajList.isEmpty()) {

			List<String> list = fazlaMesaiOrtakIslemler.getFazlaMesaiUyari(yil, ay, seciliEkSaha3Id, puantajList, session);
			for (String string : list)
				PdksUtil.addMessageAvailableWarn(string);

		}
		modelGoster = ortakIslemler.getModelGoster(denklestirmeAy, session);
		if (!modelGoster) {
			HashMap<Boolean, Long> sanalDurum = new HashMap<Boolean, Long>();
			if (puantajList != null)
				for (AylikPuantaj ap : aylikPuantajList)
					sanalDurum.put(ap.getPdksPersonel().getSanalPersonel(), ap.getPdksPersonel().getId());
			modelGoster = sanalDurum.size() > 1;
		}
		setAylikPuantajList(puantajList);
	}

	/**
	 * @param bosCalismaList
	 * @param uyariMesai
	 */
	private void bosCalismaOffGuncelle(List<VardiyaGun> bosCalismaList, boolean uyariMesai) {
		HashMap map = new HashMap();
		map.put("vardiyaTipi", Vardiya.TIPI_OFF);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		Vardiya offVardiya = (Vardiya) pdksEntityController.getObjectByInnerObject(map, Vardiya.class);
		if (offVardiya != null) {
			for (VardiyaGun vardiyaGun : bosCalismaList) {
				vardiyaGun.setVardiya(offVardiya);
				vardiyaGun.setVersion(0);
				pdksEntityController.saveOrUpdate(session, entityManager, vardiyaGun);
			}
			session.flush();
			if (uyariMesai)
				PdksUtil.addMessageWarn(offVardiya.getAciklama() + " günler güncellendi, 'Fazla Mesai Getir' tekrar çalıştırın.");
		}
	}

	/**
	 * @param list
	 */
	private void haftaTatilVardiyaGuncelle(List<PersonelDenklestirmeTasiyici> list) {
		for (PersonelDenklestirmeTasiyici personelDenklestirmeTasiyici : list) {
			boolean flush = false;
			TreeMap<String, VardiyaGun> vgMap = new TreeMap<String, VardiyaGun>();
			TreeMap<Integer, List<VardiyaGun>> haftaMap = new TreeMap<Integer, List<VardiyaGun>>();
			int hafta = 0;
			TreeMap<String, Integer> vgHaftaMap = new TreeMap<String, Integer>();
			List<PersonelDenklestirmeTasiyici> personelDenklestirmeleri = personelDenklestirmeTasiyici.getPersonelDenklestirmeleri();
			Calendar cal1 = Calendar.getInstance();
			Date bugun = PdksUtil.getDate(cal1.getTime());
			for (PersonelDenklestirmeTasiyici personelDenklestirmeTasiyici2 : personelDenklestirmeleri) {
				++hafta;
				List<VardiyaGun> vardiyaGunList = personelDenklestirmeTasiyici2.getVardiyalar();
				if (vardiyaGunList == null)
					continue;
				for (VardiyaGun vardiyaGun : vardiyaGunList) {
					String key = PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), "yyyyMMdd");
					boolean haftaTatil = vardiyaGun.getVardiya().isHaftaTatil();
					if (vardiyaGun.isAyinGunu() && vardiyaGun.getVardiya() != null && (haftaTatil || bugun.after(vardiyaGun.getVardiyaDate()))) {
						cal1.setTime(vardiyaGun.getVardiyaDate());
						if (haftaTatil || (vardiyaGun.getVardiya().isCalisma() && (vardiyaGun.getVersion() < 0 || vardiyaGun.getHareketler() == null || vardiyaGun.getHareketler().isEmpty()))) {
							if (haftaTatil)
								vgHaftaMap.put(key, hafta);
							else if (haftaTatil == false && (vardiyaGun.getHareketler() == null || vardiyaGun.getHareketler().isEmpty())) {
								List<VardiyaGun> vList = haftaMap.containsKey(hafta) ? haftaMap.get(hafta) : new ArrayList<VardiyaGun>();
								if (vList.isEmpty())
									haftaMap.put(hafta, vList);
								vList.add(vardiyaGun);
							}
							vgMap.put(key, vardiyaGun);
						}

					}

				}
			}
			if (!haftaMap.isEmpty()) {
				for (String key : vgHaftaMap.keySet()) {
					VardiyaGun vardiyaGun = vgMap.get(key);
					hafta = vgHaftaMap.get(key);
					if ((vardiyaGun.getVardiyaDate().after(bugun) || vardiyaGun.getHareketler() != null) && haftaMap.containsKey(hafta)) {
						List<VardiyaGun> vList = haftaMap.get(hafta);
						if (vList.size() == 1) {
							Vardiya vardiyaHafta = vardiyaGun.getVardiya();
							VardiyaGun vardiyaCalismaGun = vList.get(0);
							Vardiya vardiyaCalisma = vardiyaCalismaGun.getVardiya();
							logger.debug(vardiyaCalismaGun.getVardiyaKeyStr() + " " + key);
							vardiyaGun.setVardiya(vardiyaCalisma);
							vardiyaGun.setVersion(-1);
							vardiyaCalismaGun.setVardiya(vardiyaHafta);
							vardiyaCalismaGun.setVersion(0);
							pdksEntityController.saveOrUpdate(session, entityManager, vardiyaGun);
							pdksEntityController.saveOrUpdate(session, entityManager, vardiyaCalismaGun);
							flush = true;
						}
					}
				}
			}
			if (flush) {
				PdksUtil.addMessageWarn("Hafta tatilleri güncellendi, 'Fazla Mesai Getir' tekrar çalıştırın.");
				session.flush();
			}

		}
	}

	/**
	 * @param puantaj
	 * @return
	 */
	public boolean ciftBolumCalisan(AylikPuantaj puantaj) {
		boolean cal = false;
		if (puantaj != null && ciftBolumCalisanMap != null) {
			cal = ciftBolumCalisanMap.containsKey(puantaj.getPdksPersonel().getId());
		}
		return cal;
	}

	/**
	 * @param puantaj
	 * @return
	 */
	public String ciftAylikPuantajPersonelHTML(AylikPuantaj puantaj) {
		String str = "";
		if (puantaj != null) {
			Personel islemPersonel = ciftBolumCalisanMap.get(puantaj.getPdksPersonel().getId());
			if (islemPersonel != null)
				str = getPersonelAciklamaHTML(islemPersonel);
		}
		return str;
	}

	/**
	 * @param personel
	 * @return
	 */
	public String getPersonelAciklamaHTML(Personel personel) {
		String str = "";
		if (personel != null) {
			str = "<TABLE>";
			str = "<tr><td><b>" + ortakIslemler.sirketAciklama() + "</b></td><td><b> : </b>" + personel.getSirket().getAd() + "</td></tr>";
			str += "<tr><td><b>" + bolumAciklama + "</b></td><td><b> : </b>" + personel.getEkSaha3().getAciklama() + "</td></tr>";
			str += "<tr><td><b>" + ortakIslemler.personelNoAciklama() + "</b></td><td><b> : </b>" + personel.getPdksSicilNo() + "</td></tr>";
			str += "<tr><td><b>Adı Soyadı</b></td><td><b> : </b>" + personel.getAdSoyad() + "</td></tr>";
			str += "</TABLE>";
		}
		return str;
	}

	public String ciftBolumCalisanHareketGuncelle() {
		PersonelView personelView = null;
		Tanim ciftBolumCalisanKartNedenTanim = null;
		KapiView manuelGiris = null, manuelCikis = null;
		for (HareketKGS hareketKGS : hareketler) {
			if (hareketKGS.isCheckBoxDurum()) {
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
				ciftBolumCalisanKartNedenTanim = ciftBolumTanimGetir();
				break;
			}
		}
		String islemAciklama = "Seçili kayıt yok!";
		if (ciftBolumCalisanKartNedenTanim != null) {
			Long nedenId = ciftBolumCalisanKartNedenTanim.getId();
			Personel seciliPersonel = seciliAylikPuantaj.getPdksPersonel();
			Personel islemPersonel = ciftBolumCalisanMap.get(seciliPersonel.getId());
			personelView = new PersonelView();
			personelView.setId(islemPersonel.getPersonelKGS().getId());
			String seciliAciklama = seciliPersonel.getPdksSicilNo() + " - " + seciliPersonel.getAdSoyad() + " aktarım için iptal edildi.";
			islemAciklama = islemPersonel.getPdksSicilNo() + " - " + islemPersonel.getAdSoyad() + " hareket aktarımı yapıldı.";
			long pdksId = 0l;
			for (HareketKGS hareketKGS : hareketler) {
				if (hareketKGS.isCheckBoxDurum()) {
					KapiView kapiView = hareketKGS.getKapiView();
					if (kapiView != null) {
						if (kapiView.getKapi() != null && manuelCikis != null && manuelGiris != null) {
							Kapi kapi = kapiView.getKapi();
							if (kapi.isGirisKapi())
								kapiView = manuelGiris;
							else if (kapi.isCikisKapi())
								kapiView = manuelCikis;
						}
					}
					Long id = pdksEntityController.hareketEkleReturn(kapiView, personelView, hareketKGS.getOrjinalZaman(), authenticatedUser, nedenId, islemAciklama, session);
					if (id != null)
						pdksEntityController.hareketSil(Long.parseLong(hareketKGS.getId().substring(1)), pdksId, authenticatedUser, nedenId, seciliAciklama, session);
				}
			}
		}
		if (personelView != null) {
			session.flush();
			PdksUtil.addMessageInfo(islemAciklama);
			fillPersonelDenklestirmeList();
		} else
			PdksUtil.addMessageWarn(islemAciklama);
		return "";
	}

	/**
	 * @param puantaj
	 * @return
	 */
	public String ciftBolumCalisanSec() {
		for (HareketKGS hareketKGS : hareketler)
			hareketKGS.setCheckBoxDurum(checkBoxDurum);
		return "";
	}

	/**
	 * @param puantaj
	 * @return
	 */
	public String ciftBolumCalisanGetir(AylikPuantaj puantaj) {
		hareketler.clear();
		seciliAylikPuantaj = puantaj;
		tmpAlan = "";
		checkBoxDurum = !authenticatedUser.isAdmin();
		if (puantaj != null && ciftBolumCalisanHareketMap != null && ciftBolumCalisanHareketMap.containsKey(puantaj.getPdksPersonel().getId())) {
			Personel personel = ciftBolumCalisanMap.get(puantaj.getPdksPersonel().getId());
			tmpAlan = getPersonelAciklamaHTML(personel);
			List<HareketKGS> list = ciftBolumCalisanHareketMap.get(puantaj.getPdksPersonel().getId());
			for (HareketKGS hareketKGS : list) {
				hareketKGS.setCheckBoxDurum(!authenticatedUser.isAdmin());
				hareketler.add(hareketKGS);
			}
		}
		return "";
	}

	/**
	 * @param puantajList
	 */
	private List ciftBolumCalisanKontrol(List<AylikPuantaj> puantajList) {
		List<String> vGunList = new ArrayList<String>();
		String ciftBolumCalisanStr = ortakIslemler.getParameterKey("ciftBolumCalisan");
		if (ikRole && denklestirmeAy != null && seciliBolum != null && !ciftBolumCalisanStr.equals("")) {
			List<String> ciftBolumList = PdksUtil.getListByString(ciftBolumCalisanStr, null);
			Tanim ciftBolumCalisanKartNedenTanim = null;
			if (ciftBolumList.contains(seciliBolum.getErpKodu())) {
				ciftBolumCalisanKartNedenTanim = ciftBolumTanimGetir();
				if (ciftBolumCalisanKartNedenTanim != null) {
					TreeMap<Long, AylikPuantaj> puantajMap = new TreeMap<Long, AylikPuantaj>();
					for (Iterator iterator = puantajList.iterator(); iterator.hasNext();) {
						AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
						puantajMap.put(aylikPuantaj.getPdksPersonel().getId(), aylikPuantaj);
					}
					StringBuffer sb = new StringBuffer();
					sb.append("SP_CIFT_PERSONEL");
					LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<String, Object>();
					linkedHashMap.put("donemId", denklestirmeAy.getId());
					if (session != null)
						linkedHashMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<Object[]> alanList = null;
					try {
						alanList = pdksEntityController.execSPList(linkedHashMap, sb, null);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (alanList != null) {
						HashMap<Long, Long> idMap = new HashMap<Long, Long>();
						for (Iterator iterator = alanList.iterator(); iterator.hasNext();) {
							Object[] objects = (Object[]) iterator.next();
							try {
								Long perId = ((BigDecimal) objects[0]).longValue(), perKGSNewId = ((BigDecimal) objects[1]).longValue(), perNewId = ((BigDecimal) objects[2]).longValue();
								if (puantajMap.containsKey(perId)) {
									AylikPuantaj aylikPuantaj = puantajMap.get(perId);
									TreeMap<String, List<HareketKGS>> hareketMap = new TreeMap<String, List<HareketKGS>>();
									for (VardiyaGun vardiyaGun : aylikPuantaj.getVardiyalar()) {
										if (vardiyaGun.getIzin() != null) {
											if (vardiyaGun.getHareketler() != null) {
												vGunList.add(vardiyaGun.getVardiyaKeyStr());
												for (HareketKGS hareketKGS : vardiyaGun.getHareketler()) {
													if (hareketKGS.getId() != null && (hareketKGS.getFazlaMesai() == null || hareketKGS.getFazlaMesai().doubleValue() == 0.0d) && hareketKGS.getId().startsWith(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_KGS)) {
														String key = PdksUtil.convertToDateString(hareketKGS.getOrjinalZaman(), "yyyyMMdd");
														List<HareketKGS> list = hareketMap.containsKey(key) ? hareketMap.get(key) : new ArrayList<HareketKGS>();
														if (list.isEmpty())
															hareketMap.put(key, list);

														list.add(hareketKGS);
													}
												}
											}
										}
									}
									if (!hareketMap.isEmpty()) {
										List<Long> perIdList = new ArrayList<Long>();
										perIdList.add(perKGSNewId);
										List<HareketKGS> list = ortakIslemler.getHareketBilgileri(null, perIdList, aylikPuantajDefault.getIlkGun(), aylikPuantajDefault.getSonGun(), HareketKGS.class, session);
										for (Iterator iterator2 = list.iterator(); iterator2.hasNext();) {
											HareketKGS hareketKGS = (HareketKGS) iterator2.next();
											if (hareketKGS.getId() != null && hareketKGS.getKapiView() != null && hareketKGS.getKapiView().getKapi().getPdks()) {
												String key = PdksUtil.convertToDateString(hareketKGS.getOrjinalZaman(), "yyyyMMdd");
												if (hareketMap.containsKey(key))
													hareketMap.remove(key);
											}
										}
									}
									if (!hareketMap.isEmpty()) {
										List<HareketKGS> list = new ArrayList<HareketKGS>();
										for (String key : hareketMap.keySet())
											list.addAll(hareketMap.get(key));

										idMap.put(perId, perNewId);
										ciftBolumCalisanHareketMap.put(perId, list);
									} else
										hareketMap = null;

								}

							} catch (Exception ex) {
								logger.error(ex);
							}

						}
						if (!idMap.isEmpty()) {
							HashMap fields = new HashMap();
							fields.put("id", new ArrayList(idMap.values()));
							fields.put(PdksEntityController.MAP_KEY_MAP, "getId");
							if (session != null)
								fields.put(PdksEntityController.MAP_KEY_SESSION, session);
							TreeMap<Long, Personel> perMap = pdksEntityController.getObjectByInnerObjectMap(fields, Personel.class, false);
							for (Long key : idMap.keySet()) {
								Long kgID = idMap.get(key);
								if (perMap.containsKey(kgID))
									ciftBolumCalisanMap.put(key, perMap.get(kgID));
							}
						}
						idMap = null;
						alanList = null;
					}
				}
			}
			ciftBolumList = null;
		}
		return vGunList;
	}

	/**
	 * @return
	 */
	private Tanim ciftBolumTanimGetir() {
		Tanim tanim = null;
		String kodu = ortakIslemler.getParameterKey("ciftBolumCalisanKartNedenKodu");
		if (kodu.equals("") && adminRole)
			kodu = "cbc";
		if (!kodu.equals("")) {
			HashMap fields = new HashMap();
			fields.put("tipi", Tanim.TIPI_HAREKET_NEDEN);
			fields.put("kodu", kodu);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			tanim = (Tanim) pdksEntityController.getObjectByInnerObject(fields, Tanim.class);
			if (tanim != null && !tanim.getDurum())
				tanim = null;
		}
		return tanim;
	}

	/**
	 * 
	 */
	private void saveLastParameter() {
		LinkedHashMap<String, Object> lastMap = new LinkedHashMap<String, Object>();
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
		if (seciliEkSaha4Id != null)
			lastMap.put("altBolumId", "" + seciliEkSaha4Id);
		if (hataliPuantajGoster != null)
			lastMap.put("hataliPuantajGoster", "" + hataliPuantajGoster);

		if ((ikRole) && sicilNo != null && sicilNo.trim().length() > 0)
			lastMap.put("sicilNo", sicilNo.trim());
		try {

			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception e) {

		}
	}

	/**
	 * @param aylikPuantaj
	 * @param alan
	 */
	private boolean devamlilikPrimiHesapla(AylikPuantaj aylikPuantaj, PersonelDenklestirmeDinamikAlan denklestirmeDinamikAlan) {
		Boolean islemDurum = aylikPuantaj.getPersonelDenklestirmeAylik().getDurum(), flush = Boolean.FALSE;
		List<VardiyaGun> vardiyalar = aylikPuantaj.getVardiyalar();
		int adet = 0, gunAdet = 0;
		for (VardiyaGun vardiyaGun : vardiyalar) {
			if (vardiyaGun.isAyinGunu()) {
				Vardiya vardiya = vardiyaGun.getVardiya();
				if (vardiyaGun.getId() == null || vardiya == null || vardiya.getId() == null) {
					adet = 0;
					break;
				}
				if (vardiyaGun.getIzin() != null) {
					IzinTipi izinTipi = vardiyaGun.getIzin().getIzinTipi();
					if (devamlilikPrimIzinTipleri.contains(izinTipi.getIzinTipiTanim().getErpKodu())) {
						adet = 0;
						break;
					}
					continue;
				}
				double calismaSuresi = vardiyaGun.getCalismaSuresi();
				if (vardiyaGun.getFazlaMesailer() != null) {
					for (PersonelFazlaMesai fm : vardiyaGun.getFazlaMesailer()) {
						if (fm.isOnaylandi())
							calismaSuresi -= fm.getFazlaMesaiSaati();
					}
				}
				if (vardiya.isOffGun() && vardiyaGun.isHaftaIci()) {
					adet = 0;
					break;
				}
				long fark = new Double((vardiya.getNetCalismaSuresi() - calismaSuresi) * 60).longValue();
				if (fark > 0) {
					gunAdet++;
					logger.info(vardiyaGun.getCalismaSuresi() + " " + calismaSuresi + " " + fark);
					if (fark >= 50) {
						adet = 0;
						break;

					}
				}
				adet++;
			}
		}
		if (adet == 0 || gunAdet > 1)
			islemDurum = Boolean.FALSE;
		logger.debug(PdksUtil.setTurkishStr(aylikPuantaj.getPersonelDenklestirmeAylik().getPersonel().getAdSoyad()) + " " + islemDurum);
		if (!islemDurum.equals(denklestirmeDinamikAlan.getIslemDurum())) {
			denklestirmeDinamikAlan.setIslemDurum(islemDurum);
			pdksEntityController.saveOrUpdate(session, entityManager, denklestirmeDinamikAlan);
			flush = Boolean.TRUE;
		}

		return flush;
	}

	/**
	 * @param kodu
	 * @return
	 */
	private Tanim denklestirmeMantiksalBilgiBul(String kodu) {
		HashMap fields = new HashMap();
		fields.put("tipi", Tanim.TIPI_PERSONEL_DENKLESTIRME_DINAMIK_DURUM);
		fields.put("kodu", kodu);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		Tanim tanim = (Tanim) pdksEntityController.getObjectByInnerObject(fields, Tanim.class);

		return tanim;
	}

	/**
	 * @param list
	 * @param dinamikMap
	 * @return
	 */
	private void setDenklestirmeDinamikDurum(List<Long> list, TreeMap<String, PersonelDenklestirmeDinamikAlan> dinamikMap) {
		HashMap fields = new HashMap();

		if (list != null && !list.isEmpty()) {
			fields.clear();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT S.* from " + PersonelDenklestirmeDinamikAlan.TABLE_NAME + " S WITH(nolock) ");
			sb.append(" WHERE S." + PersonelDenklestirmeDinamikAlan.COLUMN_NAME_PERSONEL_DENKLESTIRME + " :s ");
			sb.append(" AND S." + PersonelDenklestirmeDinamikAlan.COLUMN_NAME_DENKLESTIRME_ALAN_DURUM + "=1");
			sb.append(" ORDER BY S." + PersonelDenklestirmeDinamikAlan.COLUMN_NAME_ALAN);
			fields.put("s", list);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelDenklestirmeDinamikAlan> alanList = pdksEntityController.getObjectBySQLList(sb, fields, PersonelDenklestirmeDinamikAlan.class);
			if (!alanList.isEmpty()) {
				Tanim tanim = null;
				List<Long> idList = new ArrayList<Long>();
				for (PersonelDenklestirmeDinamikAlan personelDenklestirmeDinamikAlan : alanList) {
					dinamikMap.put(personelDenklestirmeDinamikAlan.getKey(), personelDenklestirmeDinamikAlan);
					tanim = personelDenklestirmeDinamikAlan.getAlan();
					if (!idList.contains(tanim.getId())) {
						denklestirmeDinamikAlanlar.add(tanim);
						idList.add(tanim.getId());
					}
				}

			}
		}

	}

	/**
	 * @param vGunList
	 * @param izinMap
	 */
	private void izinCalismaUyariMesajiOlustur(List<String> vGunList, LinkedHashMap<Long, PersonelIzin> izinMap) {
		List<String> strList = new ArrayList<String>();
		try {
			Personel izinSahibiTEK = null;
			for (Long izinId : izinMap.keySet()) {
				PersonelIzin izin = izinMap.get(izinId);
				Personel izinSahibi = izin.getIzinSahibi();
				izinSahibiTEK = izinSahibi;
				String izinStr = izinSahibi.getPdksSicilNo() + " " + ortakIslemler.personelNoAciklama() + " " + izinSahibi.getAdSoyad() + " ait " + authenticatedUser.dateTimeFormatla(izin.getBaslangicZamani()) + " - ";
				boolean tekGun = PdksUtil.tarihKarsilastirNumeric(izin.getBaslangicZamani(), izin.getBitisZamani()) == 0;
				if (!tekGun)
					izinStr += authenticatedUser.dateTimeFormatla(izin.getBitisZamani()) + " tarihleri arasındaki ";
				else
					izinStr += authenticatedUser.timeFormatla(izin.getBitisZamani()) + " tarihinde ";
				boolean devam = true;
				if (izin.getCalisilanGunler() != null && !izin.getCalisilanGunler().isEmpty()) {
					if (!tekGun) {
						String virgul = "";
						devam = false;
						for (Iterator iterator = izin.getCalisilanGunler().iterator(); iterator.hasNext();) {
							VardiyaGun vGun = (VardiyaGun) iterator.next();
							if (vGunList.contains(vGun.getVardiyaKeyStr())) {
								iterator.remove();
								continue;
							}
							devam = true;
							izinStr += virgul + PdksUtil.convertToDateString(vGun.getVardiyaDate(), "d MMM EEEEE");
							virgul = ", ";
						}
						if (!izin.getCalisilanGunler().isEmpty()) {
							if (izinStr.indexOf(",") > 0) {
								int ind = izinStr.lastIndexOf(",");
								String str1 = izinStr.substring(0, ind), str2 = izinStr.substring(ind + 1);
								izinStr = str1 + " ve" + str2 + " günlerinde ";
							} else
								izinStr += " gününde ";
						}
					}
					izinStr = PdksUtil.replaceAllManuel(izinStr + " " + izin.getIzinTipiAciklama() + " olmasına rağmen  hatalı giriş mevcuttur.", "  ", " ");
				}
				if (devam) {
					PdksUtil.addMessageAvailableWarn(izinStr);
					String str = izinStr + " ( Izin Id : " + izinId + " )";
					logger.info(PdksUtil.setTurkishStr(authenticatedUser.getAdSoyad() + " --> " + str + " " + izinSahibi.getAdSoyad()));
					if (izinCalismayanMailGonder && !izinliCalisilanGunler.contains(str)) {
						strList.add(str);
						startupAction.addIzinliCalisilanGunlerList(str);
					}
				}

			}
			if (!strList.isEmpty()) {
				MailObject mail = new MailObject();
				String konu = (strList.size() > 1 ? "" : izinSahibiTEK.getPdksSicilNo() + " " + ortakIslemler.personelNoAciklama() + " " + izinSahibiTEK.getAdSoyad() + " ait ");
				mail.setSubject(konu + (denklestirmeAy != null ? denklestirmeAy.getYil() + " - " + denklestirmeAy.getAyAdi() + " dönemi " : "") + "İzin gününde hatalı girişler");
				String uolStr = strList.size() > 1 ? "OL" : "UL";
				StringBuffer sb = new StringBuffer();
				sb.append("<p align=\"left\" style=\"width: 90%\">");
				sb.append("<TABLE style=\"width: 80%\">");
				if (sirketId != null)
					sb.append("<TR><TD><B>" + ortakIslemler.sirketAciklama() + "</B></TD><TD><B> : </B>" + PdksUtil.getSelectItemLabel(sirketId, pdksSirketList) + "</TD>");
				if (tesisId != null)
					sb.append("<TR><TD><B>" + ortakIslemler.tesisAciklama() + "</B></TD><TD><B> : </B>" + PdksUtil.getSelectItemLabel(tesisId, tesisList) + "</TD>");
				if (seciliEkSaha3Id != null)
					sb.append("<TR><TD><B>Bölüm</B></TD><TD><B> : </B>" + PdksUtil.getSelectItemLabel(seciliEkSaha3Id, gorevYeriList) + "</TD>");
				if (sicilNo != null && !sicilNo.trim().equals(""))
					sb.append("<TR><TD><B>" + ortakIslemler.personelNoAciklama() + "</B></TD><TD><B> : </B>" + sicilNo.trim() + "</TD>");
				sb.append("</TABLE>");
				sb.append("<" + uolStr + ">");
				boolean renkUyari = false;
				for (String string : strList) {
					sb.append("<LI class=\"" + (renkUyari ? "odd" : "even") + "\" style=\"text-align: left;\">" + string + "</LI>");
					renkUyari = !renkUyari;
				}
				sb.append("</" + uolStr + ">");
				sb.append("</p>");
				User admin = authenticatedUser.isAdmin() ? authenticatedUser : ortakIslemler.getSistemAdminUser(session);
				mail.getToList().add(admin.getMailPersonel());
				mail.setBody(sb.toString());
				sb = null;
				ortakIslemler.mailSoapServisGonder(false, mail, null, null, session);
			}
		} catch (Exception e) {
		}
		strList = null;
	}

	/**
	 * @param per
	 * @param vardiya
	 * @return
	 */
	public Integer getVardiyaAdet(Personel per, Vardiya vardiya) {
		Integer adet = fazlaMesaiOrtakIslemler.getVardiyaAdet(izinTipiPersonelVardiyaMap, per, vardiya);
		return adet;
	}

	/**
	 * @param aylikPuantajSablon
	 * @param puantajDenklestirmeList
	 * @return
	 */
	private HashMap<Long, Boolean> getPersonelDurumMap(AylikPuantaj aylikPuantajSablon, List<AylikPuantaj> puantajDenklestirmeList) {
		HashMap<Long, Boolean> personelDurumMap = new HashMap<Long, Boolean>();
		String personelDurumKontrol = ortakIslemler.getParameterKey("personelDurumKontrol");
		if (!personelDurumKontrol.equals("") || authenticatedUser.isAdmin()) {
			List<Long> tempList = new ArrayList<Long>();
			for (Iterator iterator1 = puantajDenklestirmeList.iterator(); iterator1.hasNext();) {
				AylikPuantaj puantaj = (AylikPuantaj) iterator1.next();
				PersonelDenklestirme personelDenklestirme = puantaj.getPersonelDenklestirmeAylik();
				if (personelDenklestirme == null)
					continue;
				Personel personel = puantaj.getPdksPersonel();
				if (personelDenklestirme.getFazlaMesaiIzinKullan() != null && personelDenklestirme.getFazlaMesaiIzinKullan() && !personel.isCalisiyorGun(aylikPuantajSablon.getSonGun())) {
					tempList.add(personelDenklestirme.getId());
				}
			}
			if (!tempList.isEmpty()) {
				HashMap fields = new HashMap();
				StringBuffer sb = new StringBuffer();
				sb.append("select DISTINCT PD.* from " + PersonelDenklestirme.TABLE_NAME + " PD WITH(nolock) ");
				sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P1 ON P1." + Personel.COLUMN_NAME_ID + "=PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
				sb.append(" INNER JOIN " + PersonelKGS.TABLE_NAME + " K1 ON K1." + PersonelKGS.COLUMN_NAME_ID + "=P1." + Personel.COLUMN_NAME_KGS_PERSONEL + " AND  COALESCE(K1.TC_KIMLIK_NO,'')<>'' ");
				sb.append(" INNER JOIN " + PersonelKGS.TABLE_NAME + " K2 ON K1.TC_KIMLIK_NO=K2.TC_KIMLIK_NO AND  K1." + PersonelKGS.COLUMN_NAME_ID + "<>K2." + PersonelKGS.COLUMN_NAME_ID);
				sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P2 ON P2." + Personel.COLUMN_NAME_KGS_PERSONEL + "=K2." + PersonelKGS.COLUMN_NAME_ID + " AND P2." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">P1." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI);
				sb.append(" INNER JOIN " + PersonelDenklestirme.TABLE_NAME + " PY ON PY.PERSONEL_ID=P2." + Personel.COLUMN_NAME_ID + " AND PY." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " AND  PY.FAZLA_MESAI_IZIN_KULLAN=1 ");
				sb.append(" WHERE PD." + PersonelDenklestirme.COLUMN_NAME_ID + " :p");
				fields.put("p", tempList);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				try {
					List<PersonelDenklestirme> denkList = pdksEntityController.getObjectBySQLList(sb, fields, PersonelDenklestirme.class);
					for (PersonelDenklestirme personelDenklestirme : denkList) {
						personelDurumMap.put(personelDenklestirme.getId(), Boolean.FALSE);
					}
					denkList = null;
				} catch (Exception e) {
					logger.error(sb.toString());
				}
				fields = null;
			}
			tempList = null;
		}
		return personelDurumMap;
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
			fillPersonelDenklestirmeList();
		return "";
	}

	/**
	 * @param islemPuantaj
	 * @param vGun
	 * @param paramsMap
	 */
	private void vardiyaGunKontrol(AylikPuantaj islemPuantaj, VardiyaGun vGun, HashMap<String, Object> paramsMap) {
		boolean fazlaMesaiTalepVardiyaOnayliDurum = false;

		Date aksamVardiyaBitisZamani = null, aksamVardiyaBaslangicZamani = null;
		Vardiya vardiya = vGun.getVardiya();
		String key1 = vGun.getVardiyaDateStr(), vardiyaKey = vGun.getVardiyaKeyStr();

		if (!izinGoster)
			izinGoster = vardiya.isIzin();
		// boolean aksamVardiyasi = vardiya.isAksamVardiyasi();
		if (perCalismaModeli != null && perCalismaModeli.getGeceCalismaOdemeVar().equals(Boolean.TRUE)) {
			if (aksamVardiyaBitSaat != null && aksamVardiyaBitDakika != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(vGun.getVardiyaDate());
				cal.set(Calendar.HOUR_OF_DAY, aksamVardiyaBitSaat);
				cal.set(Calendar.MINUTE, aksamVardiyaBitDakika);
				if (vardiya.getBasSaat() > vardiya.getBitSaat() && vardiya.isCalisma())
					cal.add(Calendar.DATE, 1);
				aksamVardiyaBitisZamani = cal.getTime();
			}
			if (aksamVardiyaBasSaat != null && aksamVardiyaBasDakika != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(vGun.getVardiyaDate());
				cal.set(Calendar.HOUR_OF_DAY, aksamVardiyaBasSaat);
				cal.set(Calendar.MINUTE, aksamVardiyaBasDakika);
				if (vardiya.getBasSaat() < vardiya.getBitSaat() || vardiya.isCalisma() == false)
					cal.add(Calendar.DATE, -1);
				aksamVardiyaBaslangicZamani = cal.getTime();
			}
		}

		List<HareketKGS> girisHareketleri = null, cikisHareketleri = null, hareketler = null;
		vGun.setFazlaMesaiOnayla(null);
		boolean hareketsiz = vGun.getVardiya() != null && vGun.getHareketler() == null && vGun.getIzin() == null;
		if (vGun.getGirisHareketleri() != null)
			girisHareketleri = new ArrayList(vGun.getGirisHareketleri());
		if (vGun.getCikisHareketleri() != null)
			cikisHareketleri = new ArrayList(vGun.getCikisHareketleri());
		hareketler = vGun.getHareketler();
		boolean goster = false;
		if (!manuelGirisGoster.equals("")) {
			if (hareketler != null) {
				for (HareketKGS hareketKGS : hareketler) {
					String islemYapan = "";
					if (hareketKGS.getKapiView() != null) {
						try {
							if (hareketKGS.isManuelGiris()) {
								islemYapan = hareketKGS.getIslem() == null ? kapiGirisSistemAdi : (hareketKGS.getIslem().getOnaylayanUser() != null ? hareketKGS.getIslem().getOnaylayanUser().getAdSoyad() : "");
								if (!goster)
									goster = islemYapan != null && islemYapan.trim().length() > 0;
							}

						} catch (Exception e) {
						}

					}
					hareketKGS.setIslemYapan(islemYapan);

				}
			}
		}
		if (goster)
			vGun.setManuelGirisHTML(manuelGirisGoster);
		islemPuantaj.setAyrikHareketVar(true);
		if (islemPuantaj != null && hareketler != null && !hareketler.isEmpty()) {
			ArrayList<HareketKGS> tumHareketler = islemPuantaj.getHareketler() != null ? islemPuantaj.getHareketler() : new ArrayList<HareketKGS>();
			if (tumHareketler.isEmpty())
				islemPuantaj.setHareketler(tumHareketler);
			tumHareketler.addAll(hareketler);
		}

		// if (haret)

		boolean fazlaMesaiOnaylaDurum = vGun.isAyrikHareketVar() == false && girisHareketleri != null && cikisHareketleri != null && girisHareketleri.size() == cikisHareketleri.size();
		if (fazlaMesaiOnaylaDurum || (vGun.isAyrikHareketVar() && vGun.getVardiya().isCalisma())) {
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
				vGun.setFazlaMesaiOnayla(true);
			}
			if (vGun.isAyrikHareketVar())
				vGun.setHareketHatali(girisHareketleri.size() != cikisHareketleri.size());

		}

		Boolean fazlaMesaiHesapla = (Boolean) paramsMap.get("fazlaMesaiHesapla");
		Integer aksamVardiyaSayisi = (Integer) paramsMap.get("aksamVardiyaSayisi");
		Double aksamVardiyaSaatSayisi = (Double) paramsMap.get("aksamVardiyaSaatSayisi");
		Double sabahAksamCikisSaatSayisi = (Double) paramsMap.get("sabahAksamCikisSaatSayisi");
		Double resmiTatilSuresi = (Double) paramsMap.get("resmiTatilSuresi");
		Double haftaCalismaSuresi = (Double) paramsMap.get("haftaCalismaSuresi");
		String izinERPUpdateStr = ortakIslemler.getParameterKey("izinERPUpdate");
		Vardiya islemVardiya = vGun.getIslemVardiya();
		Tatil tatil = vGun.getTatil() != null ? vGun.getTatil().getOrjTatil() : null;
		PersonelIzin personelIzin = vGun.getIzin();
		Boolean izinli = personelIzin != null || islemVardiya.isIzin();

		Personel personel = vGun.getPersonel();
		vGun.setAksamVardiyaSaatSayisi(0d);
		vGun.setLinkAdresler(null);
		String vardiyaPlanKey = vGun.getPlanKey();
		boolean izinDurum = false;
		if (hareketler == null && vGun.getIslemVardiya() != null) {
			vGun.setHataliDurum(vGun.getIzin() == null && vGun.getIslemVardiya().isCalisma());
			izinDurum = sirketIzinGirisDurum && vGun.getIslemVardiya().isCalisma() && vGun.isZamanGelmedi() == false && vGun.getIzin() == null;
		}
		if (vGun.isAyrikHareketVar()) {
			vGun.setHareketHatali(Boolean.TRUE);
			fazlaMesaiOnaylaDurum = false;
			logger.debug(key1 + " " + vGun.getHareketDurum());
			vGun.setFazlaMesaiOnayla(null);
		} else if (vGun.isZamanGelmedi() && islemVardiya != null && islemVardiya.isCalisma() && islemVardiya.getVardiyaBitZaman().after(islemVardiya.getVardiyaFazlaMesaiBitZaman())) {
			// fazlaMesaiHesapla = Boolean.FALSE;
			vGun.setHataliDurum(Boolean.TRUE);
			if (vardiyaPlaniDurum) {
				String link = "<a href='http://" + adres + "/vardiyaPlani?planKey=" + vardiyaPlanKey + "'>" + vardiyaPlaniStr + "</a>";
				vGun.addLinkAdresler(link);
			}
		} else if (vGun.getHareketDurum()) {
			try {

				if ((izinli || !vGun.getVardiya().isCalisma() || vGun.getVardiya().isHaftaTatil()) && vGun.getHareketler() != null && !vGun.getHareketler().isEmpty()) {
					PersonelFazlaMesai personelFazlaMesaiGiris = null;
					if (vGun.getGirisHareketleri() != null && !vGun.getGirisHareketleri().isEmpty()) {
						for (HareketKGS hareket : vGun.getGirisHareketleri()) {
							if (hareket.getId() == null)
								continue;
							personelFazlaMesaiGiris = hareket.getPersonelFazlaMesai();
							if (personelFazlaMesaiGiris == null) {
								fazlaMesaiHesapla = Boolean.FALSE;
								vGun.setOnayli(Boolean.FALSE);
								break;
							}
						}

					}
					String link = null;
					if (personelFazlaMesaiGiris == null) {
						vGun.setHareketHatali(Boolean.TRUE);
						kapiGirisGetir(vGun, vardiyaPlanKey);
						if (personelIzin != null && personelIzinGirisiDurum && !izinERPUpdateStr.equals("1")) {
							link = "<a href='http://" + adres + "/personelIzinGirisi?izinKey="
									+ PdksUtil.getEncodeStringByBase64("perId=" + personel.getId() + "&tarih1=" + PdksUtil.convertToDateString(personelIzin.getBaslangicZamani(), "yyyyMMdd") + "&tarih2=" + PdksUtil.convertToDateString(personelIzin.getBitisZamani(), "yyyyMMdd")) + "'>"
									+ personelIzinGirisiStr + "</a>";
							vGun.addLinkAdresler(link);
						}
					}
					personelFazlaMesaiEkle(vGun, vardiyaPlanKey);

				} else if (vGun.getVardiya().isCalisma() && izinli == false) {
					if (!vGun.getVardiya().isIcapVardiyasi()) {
						double sure = vGun.getVardiya().getNetCalismaSuresi();
						boolean sureAz = eksikSaatYuzde != null && vGun.getCalismaSuresi() < (sure * eksikSaatYuzde / 100.0d);
						if (sureAz && tatil != null) {
							if (vGun.getCikisHareketleri() != null) {
								HareketKGS cikisHareket = vGun.getCikisHareketleri().get(vGun.getCikisHareketleri().size() - 1);
								sureAz = !(cikisHareket.getZaman().after(tatil.getBasTarih()) && cikisHareket.getZaman().before(tatil.getBitTarih()));
							}

						}
						if (sureAz) {

							vGun.setHataliDurum(eksikSaatYuzde != null);
							if (personelIzinGirisiDurum) {
								String link = "<a href='http://" + adres + "/personelIzinGirisi?izinKey=" + PdksUtil.getEncodeStringByBase64("perId=" + personel.getId()) + "'>" + personelIzinGirisiStr + "</a>";
								if (!izinERPUpdateStr.equals("1"))
									vGun.addLinkAdresler(link);
							}
							kapiGirisGetir(vGun, vardiyaPlanKey);
							if (vGun.getGirisHareketleri() != null) {
								HareketKGS girisHareket = vGun.getGirisHareketleri().get(0);
								HareketKGS cikisHareket = vGun.getCikisHareketleri().get(0);
								boolean hatali = Boolean.TRUE;
								if (vGun.getFazlaMesailer() != null) {
									for (PersonelFazlaMesai personelFazlaMesai : vGun.getFazlaMesailer()) {
										if (personelFazlaMesai.getHareketId().equals(girisHareket.getId()) || personelFazlaMesai.getHareketId().equals(cikisHareket.getId())) {
											hatali = Boolean.FALSE;
										}
									}
								}

								if (hatali && girisHareket.getOrjinalZaman().getTime() < islemVardiya.getVardiyaTelorans1BasZaman().getTime()) {
									personelFazlaMesaiEkle(vGun, vardiyaPlanKey);
									fazlaMesaiHesapla = Boolean.FALSE;

									vGun.setHareketHatali(Boolean.TRUE);
									vGun.setOnayli(Boolean.FALSE);
								}
							}
							if (vGun.getCikisHareketleri() != null) {
								HareketKGS cikisHareket = vGun.getCikisHareketleri().get(vGun.getCikisHareketleri().size() - 1);
								boolean hatali = Boolean.TRUE;
								if (vGun.getFazlaMesailer() != null) {
									for (PersonelFazlaMesai personelFazlaMesai : vGun.getFazlaMesailer()) {
										if (personelFazlaMesai.getHareketId().equals(cikisHareket.getId())) {
											hatali = Boolean.FALSE;
										}
									}
								}
								if (hatali && cikisHareket.getOrjinalZaman().getTime() > islemVardiya.getVardiyaTelorans2BitZaman().getTime()) {
									personelFazlaMesaiEkle(vGun, vardiyaPlanKey);
									fazlaMesaiHesapla = Boolean.FALSE;
									vGun.setOnayli(Boolean.FALSE);
									vGun.setHareketHatali(Boolean.TRUE);
								}
							}
							if (vGun.getHareketler() == null || vGun.getHareketler().isEmpty())
								vardiyaPlaniGetir(vGun, vardiyaPlanKey);

						}
						try {
							if (vardiyaKey.equals("00002618_20221001"))
								logger.debug(key1 + " " + vGun.getId());
							girisHareketleri = vGun.getGirisHareketleri();
							cikisHareketleri = vGun.getCikisHareketleri();
							if (vGun.getFazlaMesailer() != null) {
								List<HareketKGS> list = new ArrayList<HareketKGS>();
								if (girisHareketleri != null && !girisHareketleri.isEmpty())
									list.addAll(girisHareketleri);
								if (cikisHareketleri != null && !cikisHareketleri.isEmpty())
									list.addAll(cikisHareketleri);
								if (!list.isEmpty()) {
									HashMap<String, PersonelFazlaMesai> mesaiMap = new HashMap<String, PersonelFazlaMesai>();
									for (PersonelFazlaMesai pfm : vGun.getFazlaMesailer())
										mesaiMap.put(pfm.getHareketId(), pfm);
									for (HareketKGS hareketKGS : list) {
										if (hareketKGS.getId() != null && mesaiMap.containsKey(hareketKGS.getId()) && hareketKGS.getPersonelFazlaMesai() == null)
											hareketKGS.setPersonelFazlaMesai(mesaiMap.get(hareketKGS.getId()));
									}
									mesaiMap = null;

								}

								list = null;

							}

							int girisAdet = girisHareketleri != null ? girisHareketleri.size() : -1, cikisAdet = cikisHareketleri != null ? cikisHareketleri.size() : -1;
							if (girisAdet > 1 && cikisAdet == girisAdet && vGun.isHareketHatali() == false) {
								for (int i = 0; i < girisAdet; i++) {
									HareketKGS girisHareket = girisHareketleri.get(i), cikisHareket = cikisHareketleri.get(i);
									Date girisZaman = girisHareket != null ? girisHareket.getOrjinalZaman() : null;
									Date cikisZaman = cikisHareket != null ? cikisHareket.getOrjinalZaman() : null;
									if ((girisZaman != null && girisZaman.before(islemVardiya.getVardiyaTelorans1BasZaman())) || (cikisZaman != null && cikisZaman.after(islemVardiya.getVardiyaTelorans2BitZaman()))) {
										if (girisHareket.getPersonelFazlaMesai() == null && cikisHareket.getPersonelFazlaMesai() == null) {
											vGun.setHareketHatali(Boolean.TRUE);
											if (vGun.getLinkAdresler() == null)
												kapiGirisGetir(vGun, vardiyaPlanKey);
											if (girisZaman != null && cikisZaman != null)
												personelFazlaMesaiEkle(vGun, vardiyaPlanKey);
											vGun.setOnayli(Boolean.FALSE);
											fazlaMesaiHesapla = Boolean.FALSE;
											vGun.setHataliDurum(Boolean.TRUE);
										}
									}
								}
							} else if (girisAdet > 0 || cikisAdet > 0) {
								HareketKGS girisHareket = girisAdet > 0 ? girisHareketleri.get(0) : null;
								HareketKGS cikisHareket = cikisAdet > 0 ? cikisHareketleri.get(0) : null;
								Date girisZaman = girisHareket != null ? girisHareket.getOrjinalZaman() : null;
								Date cikisZaman = cikisHareket != null ? cikisHareket.getOrjinalZaman() : null;
								if (girisAdet > 0) {
									if (cikisHareket == null) {
										if (vGun.isZamanGelmedi())
											cikisHareket = new HareketKGS();
									}
									if (cikisHareket == null || (girisHareket.getPersonelFazlaMesai() == null && cikisHareket.getPersonelFazlaMesai() == null && girisZaman != null && girisZaman.before(islemVardiya.getVardiyaTelorans1BasZaman()))) {
										vGun.setHareketHatali(Boolean.TRUE);
										if (vGun.getLinkAdresler() == null)
											kapiGirisGetir(vGun, vardiyaPlanKey);

										if (girisZaman != null && cikisZaman != null && girisZaman.getTime() < islemVardiya.getVardiyaTelorans1BasZaman().getTime()) {
											personelFazlaMesaiEkle(vGun, vardiyaPlanKey);
										}
										vGun.setOnayli(Boolean.FALSE);
										fazlaMesaiHesapla = Boolean.FALSE;
										vGun.setHataliDurum(Boolean.TRUE);
									}
								}
								if (cikisAdet > 0) {
									if (girisHareket == null)
										girisHareket = new HareketKGS();
									if (girisHareket.getPersonelFazlaMesai() == null && cikisHareket.getPersonelFazlaMesai() == null && cikisZaman != null
											&& (cikisZaman.getTime() > islemVardiya.getVardiyaTelorans2BitZaman().getTime() || cikisZaman.getTime() < islemVardiya.getVardiyaTelorans1BasZaman().getTime())) {
										vGun.setHareketHatali(Boolean.TRUE);
										if (vGun.getLinkAdresler() == null)
											kapiGirisGetir(vGun, vardiyaPlanKey);

										if (cikisZaman.getTime() > islemVardiya.getVardiyaTelorans2BitZaman().getTime()) {
											personelFazlaMesaiEkle(vGun, vardiyaPlanKey);
										}
										vGun.setOnayli(Boolean.FALSE);
										fazlaMesaiHesapla = Boolean.FALSE;
									}
								}
							}
						} catch (Exception e1) {
							e1.printStackTrace();
							logger.error(vGun.getVardiyaKeyStr() + " " + e1.getMessage());
						}

					} else {
						if (vGun.getGirisHareketleri() != null) {
							for (Iterator iterator2 = vGun.getGirisHareketleri().iterator(); iterator2.hasNext();) {
								HareketKGS girisHareket = (HareketKGS) iterator2.next();
								boolean hatali = Boolean.TRUE;
								if (vGun.getFazlaMesailer() != null) {
									for (PersonelFazlaMesai personelFazlaMesai : vGun.getFazlaMesailer()) {
										if (personelFazlaMesai.getHareketId().equals(girisHareket.getId())) {
											hatali = Boolean.FALSE;

										}
									}

								}
								if (hatali) {
									personelFazlaMesaiEkle(vGun, vardiyaPlanKey);
									fazlaMesaiHesapla = Boolean.FALSE;
									vGun.setHareketHatali(Boolean.TRUE);
									vGun.setOnayli(Boolean.FALSE);
								}
							}
						}
					}
				}
				if (vGun.getVardiya() != null && vGun.getResmiTatilSure() > 0)
					resmiTatilSuresi += vGun.getResmiTatilSure();

				if (vGun.getIzin() == null && vGun.getVardiya() != null && vGun.getCalismaSuresi() > 0) {
					if (aksamVardiyaBaslangicZamani != null && aksamVardiyaBitisZamani != null) {
						boolean bayramAksamCalismaOde = ortakIslemler.getParameterKey("bayramAksamCalismaOde").equals("1");
						if (vGun.getGirisHareketleri() != null && vGun.getCikisHareketleri() != null && vGun.getGirisHareketleri().size() == vGun.getCikisHareketleri().size()) {
							double sure = 0;
							for (int i = 0; i < vGun.getGirisHareketleri().size(); i++) {
								Date cikisZaman = vGun.getCikisHareketleri().get(i).getZaman();
								Date girisZaman = vGun.getGirisHareketleri().get(i).getZaman();
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
									if (bayramAksamCalismaOde == false && vGun.getTatil() != null) {
										Tatil tatilSakli = vGun.getTatil();
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
								double calSure1 = girisZaman.getTime() < cikisZaman.getTime() ? PdksUtil.setSureDoubleTypeRounded(ortakIslemler.getSaatSure(girisZaman, cikisZaman, yemekList, vGun, session), vGun.getYarimYuvarla()) : 0.0d;

								sure += calSure1;
							}
							if (sure > 0 && vardiya.isCalisma())
								vGun.addAksamVardiyaSaatSayisi(sure);

							if (vGun.getFazlaMesailer() != null) {
								double sureMesai = 0;
								for (PersonelFazlaMesai fazlaMesai : vGun.getFazlaMesailer()) {
									if (fazlaMesai.isOnaylandi()) {
										if (bayramAksamCalismaOde == false && fazlaMesai.isBayram())
											continue;
										if (fazlaMesai.getBitZaman().getTime() > aksamVardiyaBaslangicZamani.getTime() && fazlaMesai.getBasZaman().getTime() < aksamVardiyaBitisZamani.getTime()) {
											Date girisZaman = fazlaMesai.getBasZaman(), cikisZaman = fazlaMesai.getBitZaman();
											if (girisZaman.before(aksamVardiyaBaslangicZamani))
												girisZaman = aksamVardiyaBaslangicZamani;
											if (cikisZaman.after(aksamVardiyaBitisZamani))
												cikisZaman = aksamVardiyaBitisZamani;
											double calSure1 = PdksUtil.setSureDoubleTypeRounded(ortakIslemler.getSaatSure(girisZaman, cikisZaman, yemekList, vGun, session), vGun.getYarimYuvarla());
											if (calSure1 > fazlaMesai.getFazlaMesaiSaati())
												calSure1 = fazlaMesai.getFazlaMesaiSaati();
											sureMesai += calSure1;
										}
									}
								}
								if (sureMesai > 0)
									vGun.addAksamVardiyaSaatSayisi(sureMesai);
							}
							if (vGun.getAksamVardiyaSaatSayisi() > 0.0d) {
								double aksamSure = vGun.getAksamVardiyaSaatSayisi();
								double netSure = vardiya.getNetCalismaSuresi();
								if (vardiya.isCalisma() == false) {
									netSure = 7.5d;
								}
								Double aksamCalismaMaxSaati = new Double(aksamCalismaSaati);
								if (aksamSure > 0.0d && aksamCalismaSaatiYuzde != null && vGun.getIslemVardiya().isCalisma()) {
									aksamCalismaMaxSaati = vGun.getIslemVardiya().getNetCalismaSuresi() * aksamCalismaSaatiYuzde.doubleValue() / 100.0d;
								}
								if (aksamCalismaMaxSaati != null && aksamSure >= aksamCalismaMaxSaati && fazlaMesaiMap.containsKey(AylikPuantaj.MESAI_TIPI_AKSAM_ADET)) {
									aksamVardiyaSayisi += 1;
									if (vardiya.isCalisma() == false)
										logger.debug(vardiyaKey);

									logger.debug(aksamVardiyaSayisi + " " + vardiyaKey + " " + vGun.getVardiyaAciklama());
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
					haftaCalismaSuresi += vGun.getHaftaCalismaSuresi();

				} catch (Exception ee) {
					logger.error("Pdks hata in : \n");
					ee.printStackTrace();
					logger.error("Pdks hata out : " + ee.getMessage());
				}

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());

				logger.info(vGun.getVardiyaKeyStr() + " " + e.getMessage());
			}

		} else {
			String link = null;

			if (personelHareketDurum) {
				link = "<a href='http://" + adres + "/personelHareket?planKey=" + vardiyaPlanKey + "'>" + personelHareketStr + "</a>";
				vGun.addLinkAdresler(link);
			}
			if (personelIzin != null && personelIzinGirisiDurum) {
				link = "<a href='http://" + adres + "/personelIzinGirisi?izinKey="
						+ PdksUtil.getEncodeStringByBase64("perId=" + personel.getId() + "&tarih1=" + PdksUtil.convertToDateString(personelIzin.getBaslangicZamani(), "yyyyMMdd") + "&tarih2=" + PdksUtil.convertToDateString(personelIzin.getBitisZamani(), "yyyyMMdd")) + "'>" + personelIzinGirisiStr
						+ "</a>";
				vGun.addLinkAdresler(link);
			}
			vardiyaPlaniGetir(vGun, vardiyaPlanKey);
			Boolean hataVar = Boolean.TRUE;
			if (vGun.getVardiya() != null && vGun.getVardiya().isIcapVardiyasi()) {
				hataVar = Boolean.FALSE;
				vGun.setHataliDurum(Boolean.FALSE);
			}
			if (hataVar && fazlaMesaiHesapla) {
				vGun.setOnayli(Boolean.FALSE);
				fazlaMesaiHesapla = Boolean.FALSE;
			}

		}
		if (fazlaMesaiOnaylaDurum && vGun.getIslemVardiya() != null && vGun.getHareketDurum().equals(Boolean.TRUE)) {
			try {
				if (key1.equals("20211111"))
					logger.debug(vardiyaKey + " " + vGun.getHareketDurum() + " " + vGun.isAyrikHareketVar());

				islemVardiya = vGun.getIslemVardiya();
				boolean calisma = islemVardiya.isCalisma() && vGun.getIzin() == null;
				Date fazlaMesaiBasZaman = calisma ? islemVardiya.getVardiyaTelorans1BasZaman() : null;
				Date fazlaMesaiBitZaman = calisma ? islemVardiya.getVardiyaTelorans2BitZaman() : null;
				if (girisHareketleri.size() == cikisHareketleri.size()) {
					TreeMap<String, PersonelFazlaMesai> pfmMap = new TreeMap<String, PersonelFazlaMesai>();
					if (vGun.getFazlaMesailer() != null) {
						for (PersonelFazlaMesai personelFazlaMesai : vGun.getFazlaMesailer()) {
							pfmMap.put(personelFazlaMesai.getHareketId(), personelFazlaMesai);
						}

					}
					for (int i = 0; i < girisHareketleri.size(); i++) {

						HareketKGS giris = girisHareketleri.get(i), cikis = cikisHareketleri.get(i);
						Date girisZaman = giris != null ? giris.getOrjinalZaman() : null;
						Date cikisZaman = cikis != null ? cikis.getOrjinalZaman() : null;
						if (calisma == false || (girisZaman != null && girisZaman.before(fazlaMesaiBasZaman)) || (cikisZaman != null && cikisZaman.after(fazlaMesaiBitZaman))) {
							String girisId = giris != null && giris.getId() != null ? giris.getId() : "";
							String cikisId = cikis != null && cikis.getId() != null ? cikis.getId() : "";
							if (!(pfmMap.containsKey(cikisId) || pfmMap.containsKey(girisId))) {
								if (giris != null && giris.getPersonelFazlaMesai() != null) {
									if (denklestirmeAyDurum) {
										PersonelFazlaMesai value = giris.getPersonelFazlaMesai();
										value.setDurum(Boolean.FALSE);
										saveGenelList.add(value);
									}

									giris.setPersonelFazlaMesai(null);
								}
								if (cikis != null && cikis.getPersonelFazlaMesai() != null) {
									if (denklestirmeAyDurum) {
										PersonelFazlaMesai value = cikis.getPersonelFazlaMesai();
										value.setDurum(Boolean.FALSE);
										saveGenelList.add(value);
									}
									cikis.setPersonelFazlaMesai(null);
								}
							}
							if (giris.getPersonelFazlaMesai() == null && cikis.getPersonelFazlaMesai() == null) {
								vGun.setFazlaMesaiOnayla(true);
								vGun.setHareketHatali(true);
								personelFazlaMesaiEkle(vGun, vardiyaPlanKey);
								break;
							}
						}
					}
					pfmMap = null;
				}
			} catch (Exception ex1) {
				logger.error(ex1);
				ex1.printStackTrace();
			}

		}
		girisHareketleri = null;
		cikisHareketleri = null;
		if (vGun.getFazlaMesailer() != null) {
			for (PersonelFazlaMesai personelFazlaMesai : vGun.getFazlaMesailer()) {
				if (personelFazlaMesai.isOnaylandi()) {

					if (fazlaMesaiOnaylaDurum) {
						vGun.setFazlaMesaiOnayla(false);
						personelFazlaMesaiEkle(vGun, vardiyaPlanKey);
					}
					if (personelFazlaMesaiDurum) {
						String link = "<a href='http://" + adres + "/personelFazlaMesai?planKey=" + vardiyaPlanKey + "'>" + personelFazlaMesaiStr + "</a>";
						vGun.addLinkAdresler(link);
					}
					break;

				}
			}
		}
		if (hareketsiz || vGun.isHataliDurum() || fazlaMesaiOnaylaDurum) {
			kapiGirisGetir(vGun, vardiyaPlanKey);
			if (fazlaMesaiOnaylaDurum == false)
				vardiyaPlaniGetir(vGun, vardiyaPlanKey);
		}

		if (vGun.getHareketDurum() != null && vGun.getHareketDurum().booleanValue() == false) {
			if (islemPuantaj.isFiiliHesapla()) {
				islemPuantaj.setFiiliHesapla(false);
				fazlaMesaiHesapla = false;
				kapiGirisGetir(vGun, vardiyaPlanKey);
				vardiyaPlaniGetir(vGun, vardiyaPlanKey);
			} else if (vGun.isAyrikHareketVar()) {
				kapiGirisGetir(vGun, vardiyaPlanKey);
				vardiyaPlaniGetir(vGun, vardiyaPlanKey);
			}

		}
		if (izinDurum && personelIzinGirisiDurum) {
			String link = "<a href='http://" + adres + "/personelIzinGirisi?izinKey="
					+ PdksUtil.getEncodeStringByBase64("perId=" + personel.getId() + "&tarih1=" + PdksUtil.convertToDateString(vGun.getVardiyaDate(), "yyyyMMdd") + "&tarih2=" + PdksUtil.convertToDateString(vGun.getVardiyaDate(), "yyyyMMdd")) + "'>" + personelIzinGirisiStr + "</a>";
			vGun.addLinkAdresler(link);

		}
		if (key1.equals("20220202"))
			logger.debug(fazlaMesaiTalepOnayliDurum + " " + vGun.getVardiyaKeyStr());
		if (fazlaMesaiTalepOnayliDurum && vGun.getFazlaMesaiOnayla() != null) {
			fazlaMesaiTalepVardiyaOnayliDurum = vardiyaFazlaMesaiTalepOnayKontrol(vGun, islemVardiya);
		}

		paramsMap.put("fazlaMesaiHesapla", fazlaMesaiHesapla);
		paramsMap.put("aksamVardiyaSayisi", aksamVardiyaSayisi);
		paramsMap.put("aksamVardiyaSaatSayisi", aksamVardiyaSaatSayisi);
		paramsMap.put("fazlaMesaiHesapla", fazlaMesaiHesapla);
		paramsMap.put("sabahAksamCikisSaatSayisi", sabahAksamCikisSaatSayisi);
		paramsMap.put("haftaCalismaSuresi", haftaCalismaSuresi);
		paramsMap.put("resmiTatilSuresi", resmiTatilSuresi);
		vGun.setFazlaMesaiTalepOnayliDurum(fazlaMesaiTalepVardiyaOnayliDurum);
	}

	/**
	 * @param vGun
	 * @param islemVardiya
	 * @return
	 */
	private boolean vardiyaFazlaMesaiTalepOnayKontrol(VardiyaGun vGun, Vardiya islemVardiya) {
		boolean onayliDurum = Boolean.FALSE;
		if (vGun.getFazlaMesaiTalepler() != null && !vGun.getFazlaMesaiTalepler().isEmpty()) {
			if (vGun.getHareketDurum().equals(Boolean.FALSE) && vGun.getHareketler() != null && !vGun.getHareketler().isEmpty()) {
				List<HareketKGS> girisList = vGun.getGirisHareketleri(), cikisList = vGun.getCikisHareketleri();
				boolean girisMesai = false, cikisMesai = false, normalCalisma = islemVardiya.isCalisma() && vGun.getIzin() == null;
				if (girisList != null && cikisList != null && girisList.size() == cikisList.size()) {
					List<Date> girisZamanList = new ArrayList<Date>(), cikisZamanList = new ArrayList<Date>();
					for (int i = 0; i < girisList.size(); i++) {
						Date giris = girisList.get(i).getOrjinalZaman(), cikis = cikisList.get(i).getOrjinalZaman();
						if (normalCalisma) {
							if (cikis.after(islemVardiya.getVardiyaTelorans2BitZaman())) {
								cikisMesai = true;
								if (giris.after(islemVardiya.getVardiyaTelorans2BitZaman()))
									girisZamanList.add(giris);
								else
									girisZamanList.add(islemVardiya.getVardiyaBitZaman());
								cikisZamanList.add(cikis);
							}
							if (giris.before(islemVardiya.getVardiyaTelorans1BasZaman())) {
								girisMesai = true;
								if (cikis.before(islemVardiya.getVardiyaTelorans1BasZaman()))
									cikisZamanList.add(cikis);
								else
									cikisZamanList.add(islemVardiya.getVardiyaBasZaman());
								girisZamanList.add(giris);
							}

						} else {
							girisZamanList.add(giris);
							cikisZamanList.add(cikis);
						}
					}
					int size = vGun.getFazlaMesaiTalepler().size();
					if (normalCalisma) {
						int adet = (girisMesai ? 1 : 0) + (cikisMesai ? 1 : 0);
						if (adet > size)
							size = adet;
					}
					if (size <= cikisZamanList.size()) {
						for (FazlaMesaiTalep fazlaMesaiTalep : vGun.getFazlaMesaiTalepler()) {
							if (fazlaMesaiTalep.isHatirlatmaMail()) {
								logger.debug(vGun.getVardiyaKeyStr());
								continue;
							}
							boolean girisTamam = !normalCalisma;
							Date tarih1 = fazlaMesaiTalep.getBaslangicZamani(), tarih2 = fazlaMesaiTalep.getBitisZamani();
							for (int i = 0; i < girisZamanList.size(); i++) {
								Date giris = girisZamanList.get(i), cikis = cikisZamanList.get(i);
								if (giris.getTime() <= tarih2.getTime() && tarih1.getTime() <= cikis.getTime()) {
									girisTamam = true;
									if (normalCalisma) {
										if (giris.before(islemVardiya.getVardiyaTelorans1BasZaman()))
											girisMesai = false;
										else if (cikis.after(islemVardiya.getVardiyaTelorans2BitZaman()))
											cikisMesai = false;

									}
								}

							}
							if (girisTamam)
								--size;

						}
						if (normalCalisma)
							onayliDurum = girisMesai == false && cikisMesai == false;
						else
							onayliDurum = size <= 0;
					}
				}

			}
		}
		return onayliDurum;
	}

	/**
	 * @param vardiyaGun
	 * @param vardiyaPlanKey
	 */
	private void kapiGirisGetir(VardiyaGun vardiyaGun, String vardiyaPlanKey) {
		if (denklestirmeAyDurum && personelHareketDurum) {
			String link = "<a href='http://" + adres + "/personelHareket?planKey=" + vardiyaPlanKey + "'>" + personelHareketStr + "</a>";
			vardiyaGun.addLinkAdresler(link);
		}
	}

	/**
	 * @param vardiyaGun
	 * @param vardiyaPlanKey
	 */
	private void vardiyaPlaniGetir(VardiyaGun vardiyaGun, String vardiyaPlanKey) {
		if (denklestirmeAyDurum && vardiyaPlaniDurum) {
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
			if (vardiyaGun.getFazlaMesaiOnayla() != null && personelFazlaMesaiDurum) {
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
		sb.append(" AND P." + Personel.getIseGirisTarihiColumn() + " IS NOT NULL AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " IS NOT NULL ");
		sb.append(" AND P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " :p");
		sb.append(" WHERE S." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=" + denklestirmeAy.getId());
		fields.put("p", siciller);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PersonelDenklestirme> list = pdksEntityController.getObjectBySQLList(sb, fields, PersonelDenklestirme.class);
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			PersonelDenklestirme personelDenklestirme = (PersonelDenklestirme) iterator.next();
			if (!personelDenklestirme.isDenklestirme())
				iterator.remove();
		}
		fields = null;
		sb = null;
		return list;
	}

	public String fazlaMesaiOnayKontrol() {
		onayla = Boolean.FALSE;
		for (AylikPuantaj puantaj : aylikPuantajList) {
			if (puantaj.isKaydet()) {
				onayla = Boolean.TRUE;
			}
		}
		seciliBolum = null;
		seciliAltBolum = null;
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
			if (seciliEkSaha4Id != null) {
				HashMap parametreMap = new HashMap();

				parametreMap.put("id", seciliEkSaha4Id);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				seciliAltBolum = (Tanim) pdksEntityController.getObjectByInnerObject(parametreMap, Tanim.class);

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
					if (personelDenklestirmeAy != null && personelDenklestirmeAy.getDurum()) {
						Personel calisan = personelDenklestirmeAy.getPersonel();
						if (!personelDenklestirmeAy.isKapandi(authenticatedUser)) {
							personelDenklestirmeAy.setAksamVardiyaSaatSayisi(puantajAylik.getAksamVardiyaSaatSayisi());
							personelDenklestirmeAy.setAksamVardiyaSayisi((double) puantajAylik.getAksamVardiyaSayisi());
							personelDenklestirmeAy.setDevredenSure(puantajAylik.getDevredenSure());
						}
						personelDenklestirmeAy.setHaftaCalismaSuresi(puantajAylik.getHaftaCalismaSuresi());
						personelDenklestirmeAy.setResmiTatilSure(puantajAylik.getResmiTatilToplami());
						personelDenklestirmeAy.setOdenenSure(puantajAylik.getFazlaMesaiSure());
						personelDenklestirmeAy.setKesilenSure(puantajAylik.getKesilenSure());
						if (personelDenklestirmeAy.isGuncellendi() && !authenticatedUser.isAdmin()) {
							personelDenklestirmeAy.setGuncellemeTarihi(new Date());
							personelDenklestirmeAy.setGuncelleyenUser(authenticatedUser);
						}
						if (guncellendi)
							pdksEntityController.saveOrUpdate(session, entityManager, personelDenklestirmeAy);
						if (mailGonder) {
							if (calisan != null && !sirketIdList.contains(calisan.getSirket().getId()))
								sirketIdList.add(calisan.getSirket().getId());
							puantajAylik.setSecili(Boolean.TRUE);
							Personel personel = null;
							if (ikRole)
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
			} else if (guncellendi)
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
					excelDosyaAdi = "fazlaMesai" + gorevYeriAciklama + PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyyMM") + (bolum != null ? "_" + bolum.getAciklama() : "") + (seciliAltBolum != null ? "_" + seciliAltBolum.getAciklama() : "") + ".xlsx";
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
		if (ikRole)
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
							if (ikRole) {
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
		if (seciliEkSaha4Id != null && seciliEkSaha4Id.longValue() > 0L) {
			HashMap parametreMap = new HashMap();
			parametreMap.put("id", seciliEkSaha4Id);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			Tanim ekSaha4 = (Tanim) pdksEntityController.getObjectByInnerObject(parametreMap, Tanim.class);
			if (ekSaha4 != null)
				gorevYeriAciklama += ekSaha4.getAciklama() + "_";
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

	/**
	 * @param gorevYeriAciklama
	 * @param list
	 * @return
	 */
	private ByteArrayOutputStream fazlaMesaiExcelDevam(String gorevYeriAciklama, List<AylikPuantaj> list) {
		boolean kimlikNoGoster = false;
		TreeMap<String, String> sirketMap = new TreeMap<String, String>();
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();
			Personel personel = aylikPuantaj.getPdksPersonel();
			if (!kimlikNoGoster && ikRole) {
				PersonelKGS personelKGS = personel.getPersonelKGS();
				if (personelKGS != null)
					kimlikNoGoster = PdksUtil.hasStringValue(personelKGS.getKimlikNo());

			}
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
		XSSFCellStyle styleGenelCenter = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		styleGenelCenter.setAlignment(CellStyle.ALIGN_CENTER);
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
		XSSFCellStyle izinBaslik = (XSSFCellStyle) ExcelUtil.getStyleHeader(wb);
		izinBaslik.setAlignment(CellStyle.ALIGN_CENTER);
		izinBaslik.setFillPattern(CellStyle.SOLID_FOREGROUND);
		izinBaslik.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 146, (byte) 208, (byte) 80 }));

		XSSFCellStyle styleIzin = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		styleIzin.setAlignment(CellStyle.ALIGN_CENTER);
		styleIzin.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleIzin.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 146, (byte) 208, (byte) 80 }));

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
		styleOddCenter.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
		styleEven.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleEven.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 219, (byte) 248, (byte) 219 }));
		styleEvenCenter.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleEvenCenter.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 219, (byte) 248, (byte) 219 }));
		styleTatil.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleTatil.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 255, (byte) 153, (byte) 204 }));
		styleIstek.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleIstek.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 255, (byte) 255, (byte) 0 }));
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
		if (kimlikNoGoster)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.kimlikNoAciklama());
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
		if (kismiOdemeGoster)
			AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "KÖM", "Çalışanın bu listenin sonunda ücret olarak kısmi ödediğimiz fazla mesai saati ");
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
		if (kesilenSureGoster) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			AylikPuantaj.baslikCell(factory, drawing, anchor, cell, AylikPuantaj.MESAI_TIPI_KESINTI_SURE, "Kesilen Süre: Çalışanın bu liste de dahil bugüne kadarki denkleşmeyen eksi bakiyesi");
		}
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
		if (denklestirmeDinamikAlanlar != null && !denklestirmeDinamikAlanlar.isEmpty()) {
			for (Tanim alan : denklestirmeDinamikAlanlar) {
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(alan.getAciklama());

			}
		}
		if (izinTipiVardiyaList != null) {
			for (Vardiya vardiya : izinTipiVardiyaList) {
				cell = ExcelUtil.getCell(sheet, row, col++, izinBaslik);
				AylikPuantaj.baslikCell(factory, drawing, anchor, cell, vardiya.getKisaAdi(), vardiya.getAdi());

			}
		}
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();

			Personel personel = aylikPuantaj.getPdksPersonel();

			if (!aylikPuantaj.isFazlaMesaiHesapla() || !aylikPuantaj.isSecili() || personel == null || personel.getSicilNo() == null || personel.getSicilNo().trim().equals(""))
				continue;
			PersonelKGS personelKGS = personel.getPersonelKGS();
			PersonelDenklestirme personelDenklestirme = aylikPuantaj.getPersonelDenklestirmeAylik();
			PersonelDenklestirme personelDenklestirmeGecenAy = personelDenklestirme != null ? personelDenklestirme.getPersonelDenklestirmeGecenAy() : null;
			row++;
			col = 0;
			Comment commentGuncelleyen = null;
			if (personelDenklestirme.getGuncelleyenUser() != null && personelDenklestirme.getGuncellemeTarihi() != null)
				commentGuncelleyen = getCommentGuncelleyen(factory, drawing, anchor, personelDenklestirme);

			try {
				boolean help = helpPersonel(aylikPuantaj.getPdksPersonel());
				try {
					if (row % 2 == 0) {
						styleGenelCenter = styleOddCenter;
						styleGenel = styleOdd;
					} else {
						styleGenelCenter = styleEvenCenter;
						styleGenel = styleEven;
					}
					ExcelUtil.getCell(sheet, row, col++, styleGenelCenter).setCellValue(personel.getSicilNo());
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
					if (kimlikNoGoster) {
						String kimlikNo = "";
						if (personelKGS != null && PdksUtil.hasStringValue(personelKGS.getKimlikNo()))
							kimlikNo = personelKGS.getKimlikNo();
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(kimlikNo);
					}
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
						if (kismiOdemeGoster) {
							if (personelDenklestirme.getKismiOdemeSure() != null && personelDenklestirme.getKismiOdemeSure().doubleValue() > 0.0d)
								setCell(sheet, row, col++, styleGenel, personelDenklestirme.getKismiOdemeSure());
							else
								ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
						}
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
									commentGuncelleyen = getCommentGuncelleyen(factory, drawing, anchor, personelDenklestirme);
								devredenSureCell.setCellComment(commentGuncelleyen);
							}
						} else
							ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
						if (kesilenSureGoster) {
							if (aylikPuantaj.getKesilenSure() > 0.0d) {
								Comment commentKesilenSure = drawing.createCellComment(anchor);
								Cell kesilenSureCell = null;
								String title = denklestirmeAy.getKesintiAciklama();
								if (denklestirmeAy.isKesintiYok()) {
									kesilenSureCell = setCellStr(sheet, row, col++, styleGenel, "X");
									title = "Denkleşmeyen negatif bakiye var! [ " + authenticatedUser.sayiFormatliGoster(aylikPuantaj.getDevredenSure()) + " ]";
								} else {
									kesilenSureCell = setCell(sheet, row, col++, styleGenel, aylikPuantaj.getKesilenSure());
								}
								RichTextString str1 = factory.createRichTextString(title);
								commentKesilenSure.setString(str1);
								if (commentKesilenSure != null)
									kesilenSureCell.setCellComment(commentKesilenSure);
							} else
								ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
						}
						if (aksamGun)
							setCell(sheet, row, col++, styleGenel, new Double(aylikPuantaj.getAksamVardiyaSayisi()));
						if (aksamSaat)
							setCell(sheet, row, col++, styleGenel, new Double(aylikPuantaj.getAksamVardiyaSaatSayisi()));
						if (modelGoster) {
							if (row % 2 == 0) {
								styleGenelCenter = styleOddCenter;
								styleGenel = styleOdd;
							} else {
								styleGenelCenter = styleEvenCenter;
								styleGenel = styleEven;
							}
							String modelAciklama = "";
							if (aylikPuantaj.getPersonelDenklestirmeAylik() != null && aylikPuantaj.getPersonelDenklestirmeAylik().getCalismaModeliAy() != null) {
								CalismaModeliAy calismaModeliAy = aylikPuantaj.getPersonelDenklestirmeAylik().getCalismaModeliAy();
								if (calismaModeliAy.getCalismaModeli() != null)
									modelAciklama = calismaModeliAy.getCalismaModeli().getAciklama();
							}
							ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(modelAciklama);
						}
						if (denklestirmeDinamikAlanlar != null && !denklestirmeDinamikAlanlar.isEmpty()) {
							for (Tanim alan : denklestirmeDinamikAlanlar) {
								PersonelDenklestirmeDinamikAlan denklestirmeDinamikAlan = aylikPuantaj.getDinamikAlan(alan.getId());
								if (denklestirmeDinamikAlan == null)
									ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
								else
									ExcelUtil.getCell(sheet, row, col++, styleGenelCenter).setCellValue(denklestirmeDinamikAlan.getIslemDurum() ? "+" : "-");

							}
						}
						if (izinTipiVardiyaList != null) {
							if (row % 2 == 0)
								styleGenel = styleTutarOdd;
							else {
								styleGenel = styleTutarEven;
							}
							for (Vardiya vardiya : izinTipiVardiyaList) {
								Integer adet = getVardiyaAdet(personel, vardiya);
								setCell(sheet, row, col++, styleGenel, new Double(adet != null ? adet : 0.0d));

							}
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
	 * @param factory
	 * @param drawing
	 * @param anchor
	 * @param personelDenklestirme
	 * @return
	 */
	private Comment getCommentGuncelleyen(CreationHelper factory, Drawing drawing, ClientAnchor anchor, PersonelDenklestirme personelDenklestirme) {
		Comment commentGuncelleyen;
		commentGuncelleyen = drawing.createCellComment(anchor);
		String title = "Onaylayan : " + personelDenklestirme.getGuncelleyenUser().getAdSoyad() + "\n";
		title += "Zaman : " + authenticatedUser.dateTimeFormatla(personelDenklestirme.getGuncellemeTarihi());
		RichTextString str1 = factory.createRichTextString(title);
		commentGuncelleyen.setString(str1);
		return commentGuncelleyen;
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

	public Cell setCellStr(Sheet sheet, int rowNo, int columnNo, CellStyle style, String str) {
		Cell cell = ExcelUtil.getCell(sheet, rowNo, columnNo, style);
		cell.setCellValue(str != null ? str : "");
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
		fazlaMesaiBakiyeGuncelleAyarla();
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
			}
			ekSaha4Tanim = ortakIslemler.getEkSaha4(sirket, sirketId, session);
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

	/**
	 * 
	 */
	private void fazlaMesaiBakiyeGuncelleAyarla() {
		bakiyeGuncelle = ortakIslemler.getParameterKey("fazlaMesaiBakiyeGuncelle").equals("1") ? Boolean.FALSE : null;
	}

	/**
	 * @return
	 */
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
					List<String> sayfalar = !superVisorHemsireSayfalari.equals("") ? PdksUtil.getListByString(superVisorHemsireSayfalari, null) : null;
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
				if (departman.isAdminMi() && sirket.getDepartmanBolumAyni() == false) {
					gorevYeriList = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, tesisId != null ? String.valueOf(tesisId) : null, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, Boolean.TRUE, session);
					if (gorevYeriList.size() == 1)
						seciliEkSaha3Id = (Long) gorevYeriList.get(0).getValue();
				} else {
					gorevYeriList = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, null, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, Boolean.TRUE, session);
					if (gorevYeriList.size() == 1)
						seciliEkSaha3Id = (Long) gorevYeriList.get(0).getValue();
				}

			}
		}

		if (ekSaha4Tanim != null) {
			if (altBolumList == null)
				altBolumList = new ArrayList<SelectItem>();
			else
				altBolumList.clear();
			if (seciliEkSaha3Id != null)
				altBolumDoldur();
		} else

			aylikPuantajList.clear();
		return "";
	}

	public String altBolumDoldur() {
		aylikPuantajList.clear();
		if (ekSaha4Tanim != null) {
			altBolumList = fazlaMesaiOrtakIslemler.getFazlaMesaiAltBolumList(sirket, tesisId != null ? String.valueOf(tesisId) : null, seciliEkSaha3Id, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, Boolean.TRUE, session);
			boolean eski = altBolumList.size() == 1;
			if (eski)
				seciliEkSaha4Id = (Long) altBolumList.get(0).getValue();
			else if (seciliEkSaha4Id != null) {
				for (SelectItem st : altBolumList) {
					if (st.getValue().equals(seciliEkSaha4Id))
						eski = true;
				}
			}
			if (!eski)
				seciliEkSaha4Id = -1L;
		}
		return "";
	}

	/**
	 * 
	 */
	private void bolumleriTemizle() {
		gorevYeriList = null;
		fazlaMesaiOnayDurum = Boolean.FALSE;
	}

	/**
	 * @param aylikPuantaj
	 * @param dateStr
	 */
	public String puantajVardiyaGoster(AylikPuantaj aylikPuantaj, String dateStr) {
		if (aylikPuantaj != null && dateStr != null) {
			VardiyaGun vg = new VardiyaGun();
			vg.setVardiyaDate(PdksUtil.convertToJavaDate(dateStr, "yyyyMMdd"));
			vardiyaGoster(aylikPuantaj.getVardiya(vg));
			vg = null;
		}
		return "";
	}

	/**
	 * @param vg
	 */
	public String vardiyaGoster(VardiyaGun vg) {
		setSeciliVardiyaGun(vg);
		fazlaMesaiVardiyaGun = vg;
		toplamFazlamMesai = 0D;
		Long key = vg.getId();
		fmtList = key != null && fmtMap != null && fmtMap.containsKey(key) ? fmtMap.get(key) : null;
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
		fazlaMesaiTalepSil = Boolean.FALSE;
		if (denklestirmeAyDurum && ikRole && vg.getHareketDurum() == false && vg.getFazlaMesaiOnayla() == null) {
			if (vg.getFazlaMesaiTalepler() != null) {
				for (FazlaMesaiTalep fazlaMesaiTalep : vg.getFazlaMesaiTalepler()) {
					String titleStr = null;
					for (HareketKGS hareketKGS : vg.getHareketler()) {
						if (hareketKGS.getIslem() != null) {
							String aciklama = hareketKGS.getIslem().getAciklama();
							if (aciklama != null && aciklama.indexOf(":" + fazlaMesaiTalep.getId()) >= 0) {
								titleStr = aciklama.trim();
								fazlaMesaiTalepSil = Boolean.TRUE;
								break;
							}
						}
					}
					fazlaMesaiTalep.setCheckBoxDurum(titleStr != null);
					fazlaMesaiTalep.setTitleStr(titleStr);

				}
			}
		}
		if (vg.getTitleStr() == null) {
			String titleStr = fazlaMesaiOrtakIslemler.getFazlaMesaiSaatleri(vg);
			vg.setTitleStr(titleStr);
			vg.addLinkAdresler(titleStr);
		}
		return "";
	}

	/**
	 * @return
	 */
	public String fazlaMesaiOtomatikHareketSil() {
		boolean secili = false;
		if (seciliVardiyaGun.getFazlaMesaiTalepler() != null) {
			for (FazlaMesaiTalep fazlaMesaiTalep : seciliVardiyaGun.getFazlaMesaiTalepler()) {
				if (fazlaMesaiTalep.isCheckBoxDurum()) {
					secili = true;
					Long id = fazlaMesaiOrtakIslemler.fazlaMesaiOtomatikHareketSil(fazlaMesaiTalep.getId(), session);
					if (id != null && id.equals(fazlaMesaiTalep.getId()))
						fazlaMesaiTalepSil = Boolean.FALSE;
				}
			}
		}
		if (!secili)
			PdksUtil.addMessageAvailableWarn("İşlem yapılacak seçili kayıt yok!");
		else if (fazlaMesaiTalepSil == false) {
			try {
				fillPersonelDenklestirmeList();
				PdksUtil.addMessageAvailableInfo("Kayıtlar seçili kayıtlar silindi.");

			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
		}

		return "";
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

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
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

	public TreeMap<String, TreeMap<String, List<VardiyaGun>>> getIzinTipiPersonelVardiyaMap() {
		return izinTipiPersonelVardiyaMap;
	}

	public void setIzinTipiPersonelVardiyaMap(TreeMap<String, TreeMap<String, List<VardiyaGun>>> izinTipiPersonelVardiyaMap) {
		this.izinTipiPersonelVardiyaMap = izinTipiPersonelVardiyaMap;
	}

	public List<Vardiya> getIzinTipiVardiyaList() {
		return izinTipiVardiyaList;
	}

	public void setIzinTipiVardiyaList(List<Vardiya> izinTipiVardiyaList) {
		this.izinTipiVardiyaList = izinTipiVardiyaList;
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

	public Boolean getFazlaMesaiOnayDurum() {
		return fazlaMesaiOnayDurum;
	}

	public void setFazlaMesaiOnayDurum(Boolean fazlaMesaiOnayDurum) {
		this.fazlaMesaiOnayDurum = fazlaMesaiOnayDurum;
	}

	public VardiyaGun getSeciliVardiyaGun() {
		return seciliVardiyaGun;
	}

	public void setSeciliVardiyaGun(VardiyaGun seciliVardiyaGun) {
		this.seciliVardiyaGun = seciliVardiyaGun;
	}

	public Boolean getYoneticiRolVarmi() {
		return yoneticiRolVarmi;
	}

	public void setYoneticiRolVarmi(Boolean yoneticiRolVarmi) {
		this.yoneticiRolVarmi = yoneticiRolVarmi;
	}

	public String getMsgFazlaMesaiInfo() {
		return msgFazlaMesaiInfo;
	}

	public void setMsgFazlaMesaiInfo(String msgFazlaMesaiInfo) {
		this.msgFazlaMesaiInfo = msgFazlaMesaiInfo;
	}

	public boolean isFazlaMesaiTalepOnayliDurum() {
		return fazlaMesaiTalepOnayliDurum;
	}

	public void setFazlaMesaiTalepOnayliDurum(boolean fazlaMesaiTalepOnayliDurum) {
		this.fazlaMesaiTalepOnayliDurum = fazlaMesaiTalepOnayliDurum;
	}

	public Boolean getKesilenSureGoster() {
		return kesilenSureGoster;
	}

	public void setKesilenSureGoster(Boolean kesilenSureGoster) {
		this.kesilenSureGoster = kesilenSureGoster;
	}

	public List<Tanim> getDenklestirmeDinamikAlanlar() {
		return denklestirmeDinamikAlanlar;
	}

	public void setDenklestirmeDinamikAlanlar(List<Tanim> denklestirmeDinamikAlanlar) {
		this.denklestirmeDinamikAlanlar = denklestirmeDinamikAlanlar;
	}

	public Boolean getGebeGoster() {
		return gebeGoster;
	}

	public void setGebeGoster(Boolean gebeGoster) {
		this.gebeGoster = gebeGoster;
	}

	public Boolean getHatalariAyikla() {
		return hatalariAyikla;
	}

	public void setHatalariAyikla(Boolean hatalariAyikla) {
		this.hatalariAyikla = hatalariAyikla;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
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

	public Boolean getKismiOdemeGoster() {
		return kismiOdemeGoster;
	}

	public void setKismiOdemeGoster(Boolean kismiOdemeGoster) {
		this.kismiOdemeGoster = kismiOdemeGoster;
	}

	public List<HareketKGS> getHareketler() {
		return hareketler;
	}

	public void setHareketler(List<HareketKGS> hareketler) {
		this.hareketler = hareketler;
	}

	public HashMap<Long, List<HareketKGS>> getCiftBolumCalisanHareketMap() {
		return ciftBolumCalisanHareketMap;
	}

	public void setCiftBolumCalisanHareketMap(HashMap<Long, List<HareketKGS>> ciftBolumCalisanHareketMap) {
		this.ciftBolumCalisanHareketMap = ciftBolumCalisanHareketMap;
	}

	public HashMap<Long, Personel> getCiftBolumCalisanMap() {
		return ciftBolumCalisanMap;
	}

	public void setCiftBolumCalisanMap(HashMap<Long, Personel> ciftBolumCalisanMap) {
		this.ciftBolumCalisanMap = ciftBolumCalisanMap;
	}

	public AylikPuantaj getSeciliAylikPuantaj() {
		return seciliAylikPuantaj;
	}

	public void setSeciliAylikPuantaj(AylikPuantaj seciliAylikPuantaj) {
		this.seciliAylikPuantaj = seciliAylikPuantaj;
	}

	public String getTmpAlan() {
		return tmpAlan;
	}

	public void setTmpAlan(String tmpAlan) {
		this.tmpAlan = tmpAlan;
	}

	public Boolean getCheckBoxDurum() {
		return checkBoxDurum;
	}

	public void setCheckBoxDurum(Boolean checkBoxDurum) {
		this.checkBoxDurum = checkBoxDurum;
	}

	public String getKapiGirisSistemAdi() {
		return kapiGirisSistemAdi;
	}

	public void setKapiGirisSistemAdi(String kapiGirisSistemAdi) {
		this.kapiGirisSistemAdi = kapiGirisSistemAdi;
	}

	public boolean isYarimYuvarla() {
		return yarimYuvarla;
	}

	public void setYarimYuvarla(boolean yarimYuvarla) {
		this.yarimYuvarla = yarimYuvarla;
	}

	public Boolean getFazlaMesaiTalepSil() {
		return fazlaMesaiTalepSil;
	}

	public void setFazlaMesaiTalepSil(Boolean fazlaMesaiTalepSil) {
		this.fazlaMesaiTalepSil = fazlaMesaiTalepSil;
	}

	public Tanim getEkSaha4Tanim() {
		return ekSaha4Tanim;
	}

	public void setEkSaha4Tanim(Tanim ekSaha4Tanim) {
		this.ekSaha4Tanim = ekSaha4Tanim;
	}

	public Long getSeciliEkSaha4Id() {
		return seciliEkSaha4Id;
	}

	public void setSeciliEkSaha4Id(Long seciliEkSaha4Id) {
		this.seciliEkSaha4Id = seciliEkSaha4Id;
	}

	public List<SelectItem> getAltBolumList() {
		return altBolumList;
	}

	public void setAltBolumList(List<SelectItem> altBolumList) {
		this.altBolumList = altBolumList;
	}

	public Tanim getSeciliAltBolum() {
		return seciliAltBolum;
	}

	public void setSeciliAltBolum(Tanim seciliAltBolum) {
		this.seciliAltBolum = seciliAltBolum;
	}

}
