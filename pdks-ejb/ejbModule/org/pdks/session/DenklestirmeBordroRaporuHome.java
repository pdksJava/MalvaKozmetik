package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
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
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
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
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.AylikPuantaj;
import org.pdks.entity.BordroDetayTipi;
import org.pdks.entity.CalismaModeli;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Departman;
import org.pdks.entity.Dosya;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelDenklestirmeBordro;
import org.pdks.entity.PersonelDenklestirmeBordroDetay;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.VardiyaSaat;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;

/**
 * @author Hasan Sayar
 * 
 */
@Name("denklestirmeBordroRaporuHome")
public class DenklestirmeBordroRaporuHome extends EntityHome<DenklestirmeAy> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9211132861369205688L;
	public static String sayfaURL = "denklestirmeBordroRaporu";

	static Logger logger = Logger.getLogger(DenklestirmeBordroRaporuHome.class);

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
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;
	@Out(scope = ScopeType.SESSION, required = false)
	String bordroAdres;

	private List<AylikPuantaj> personelDenklestirmeList;

	private Boolean secimDurum = Boolean.FALSE, sureDurum, fazlaMesaiDurum, haftaTatilDurum, artikGunDurum, resmiTatilGunDurum, resmiTatilDurum, durumERP, onaylanmayanDurum, personelERP, modelGoster = Boolean.FALSE;
	private Boolean normalGunSaatDurum = Boolean.FALSE, haftaTatilSaatDurum = Boolean.FALSE, resmiTatilSaatDurum = Boolean.FALSE, izinSaatDurum = Boolean.FALSE;

	private int ay, yil, maxYil, minYil;

	private List<SelectItem> aylar;

	private String sicilNo = "", bolumAciklama, linkAdresKey;

	private DenklestirmeAy denklestirmeAy;

	private Tanim ekSaha4Tanim;

	private String COL_SIRA = "sira";
	private String COL_YIL = "yil";
	private String COL_AY = "ay";
	private String COL_AY_ADI = "ayAdi";
	private String COL_PERSONEL_NO = "personelNo";
	private String COL_AD = "ad";
	private String COL_SOYAD = "soyad";
	private String COL_AD_SOYAD = "adSoyad";
	private String COL_KART_NO = "kartNo";
	private String COL_KIMLIK_NO = "kimlikNo";
	private String COL_SIRKET = "sirket";
	private String COL_TESIS = "tesis";
	private String COL_BOLUM = "bolumAdi";
	private String COL_ALT_BOLUM = "altBolumAdi";
	private String COL_NORMAL_GUN_ADET = "normalGunAdet";
	private String COL_HAFTA_TATIL_ADET = "haftaTatilAdet";
	private String COL_RESMI_TATIL_ADET = "resmiTatilAdet";
	private String COL_ARTIK_ADET = "artikAdet";
	private String COL_TOPLAM_ADET = "toplamAdet";
	private String COL_NORMAL_GUN_SAAT = "normalGunSaat";
	private String COL_HAFTA_TATIL_SAAT = "haftaTatilSaat";
	private String COL_RESMI_TATIL_SAAT = "resmiTatilSaat";
	private String COL_IZIN_SAAT = "izinSaat";
	private String COL_UCRETLI_IZIN = "ucretliIzin";
	private String COL_RAPORLU_IZIN = "raporluIzin";
	private String COL_UCRETSIZ_IZIN = "ucretsizIzin";
	private String COL_RESMI_TATIL_MESAI = "resmiTatilMesai";
	private String COL_UCRETI_ODENEN_MESAI = "ucretiOdenenMesai";
	private String COL_HAFTA_TATIL_MESAI = "haftaTatilMesai";
	private String COL_AKSAM_SAAT_MESAI = "aksamSaatMesai";
	private String COL_AKSAM_GUN_MESAI = "aksamGunMesai";
	private String COL_EKSIK_CALISMA = "eksikCalisma";
	private String COL_CALISMA_MODELI = "calismaModeli";

	private Sheet sheet;

	private CellStyle tutarStyle, numberStyle;

	private Date basGun, bitGun;

	private Sirket sirket;

	private Long sirketId, departmanId, tesisId;

	private List<SelectItem> sirketler, departmanList, tesisList;

	private Departman departman;
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private Dosya fazlaMesaiDosya = new Dosya();
	private Boolean aksamGun = Boolean.FALSE, haftaCalisma = Boolean.FALSE, calismaModeliDurum = Boolean.FALSE, aksamSaat = Boolean.FALSE, erpAktarimDurum = Boolean.FALSE, maasKesintiGoster = Boolean.FALSE;
	private Boolean hataliVeriGetir, eksikCalisanVeriGetir;
	private List<Vardiya> izinTipiVardiyaList;
	private TreeMap<String, TreeMap<String, List<VardiyaGun>>> izinTipiPersonelVardiyaMap;
	private TreeMap<String, Tanim> baslikMap;
	private TreeMap<Long, Personel> izinTipiPersonelMap;
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
	 * @param aylikPuantaj
	 * @return
	 */
	public String saveLastParameter(AylikPuantaj aylikPuantaj) {
		Map<String, String> map1 = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();

		String adres = map1.containsKey("host") ? map1.get("host") : "";
		Personel personel = aylikPuantaj.getPdksPersonel();
		LinkedHashMap<String, Object> lastMap = new LinkedHashMap<String, Object>();
		lastMap.put("yil", "" + yil);
		lastMap.put("ay", "" + ay);
		if (departmanId != null)
			lastMap.put("departmanId", "" + departmanId);
		if (sirketId != null)
			lastMap.put("sirketId", "" + sirketId);
		if (tesisId != null)
			lastMap.put("tesisId", "" + tesisId);
		if (personel.getEkSaha3() != null)
			lastMap.put("bolumId", "" + personel.getEkSaha3().getId());
		if (ekSaha4Tanim != null)
			lastMap.put("altBolumId", "" + (personel.getEkSaha4() != null ? personel.getEkSaha4().getId() : "-1"));

		lastMap.put("sicilNo", personel.getPdksSicilNo());
		lastMap.put("sayfaURL", FazlaMesaiHesaplaHome.sayfaURL);

		bordroAdres = "<a href='http://" + adres + "/" + sayfaURL + "?linkAdresKey=" + aylikPuantaj.getPersonelDenklestirmeAylik().getId() + "'>" + ortakIslemler.getCalistiMenuAdi(sayfaURL) + " Ekranına Geri Dön</a>";
		try {
			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception e) {

		}
		return MenuItemConstant.fazlaMesaiHesapla;
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() {
		aylar = PdksUtil.getAyListesi(Boolean.TRUE);
		String str = ortakIslemler.getParameterKey("bordroVeriOlustur");
		boolean ayniSayfa = authenticatedUser.getCalistigiSayfa() != null && authenticatedUser.getCalistigiSayfa().equals(sayfaURL);
		if (!ayniSayfa)
			authenticatedUser.setCalistigiSayfa(sayfaURL);
		if (personelDenklestirmeList != null)
			personelDenklestirmeList.clear();
		else
			personelDenklestirmeList = new ArrayList<AylikPuantaj>();
		Calendar cal = Calendar.getInstance();
		ortakIslemler.gunCikar(cal, 2);
		modelGoster = Boolean.FALSE;
		ay = cal.get(Calendar.MONTH) + 1;
		yil = cal.get(Calendar.YEAR);
		try {
			minYil = Integer.parseInt(ortakIslemler.getParameterKey("sistemBaslangicYili"));
			if (str.length() > 5)
				minYil = Integer.parseInt(str.substring(0, 4));
		} catch (Exception e) {

		}
		if (baslikMap == null)
			baslikMap = new TreeMap<String, Tanim>();

		maxYil = yil + 1;
		sicilNo = "";
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);

		session.clear();
		setDepartmanId(null);
		setDepartman(null);
		setInstance(new DenklestirmeAy());
		setPersonelDenklestirmeList(new ArrayList<AylikPuantaj>());

		durumERP = Boolean.FALSE;
		personelERP = Boolean.FALSE;
		onaylanmayanDurum = null;
		sirket = null;
		sirketId = null;
		sirketler = null;
		if (tesisList != null)
			tesisList.clear();
		else
			tesisList = new ArrayList<SelectItem>();
		if (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin())
			filDepartmanList();
		if (departmanList.size() == 1)
			setDepartmanId((Long) departmanList.get(0).getValue());
		LinkedHashMap<String, Object> veriLastMap = ortakIslemler.getLastParameter(sayfaURL, session);
		String yilStr = null;
		String ayStr = null;
		String sirketIdStr = null;
		String tesisIdStr = null;
		String departmanIdStr = null;
		String hataliVeriGetirStr = null;
		String eksikCalisanVeriGetirStr = null;
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		linkAdresKey = (String) req.getParameter("linkAdresKey");

		departmanId = null;
		if (veriLastMap != null) {
			if (veriLastMap.containsKey("yil"))
				yilStr = (String) veriLastMap.get("yil");
			if (veriLastMap.containsKey("ay"))
				ayStr = (String) veriLastMap.get("ay");
			if (veriLastMap.containsKey("sirketId"))
				sirketIdStr = (String) veriLastMap.get("sirketId");
			if (veriLastMap.containsKey("tesisId"))
				tesisIdStr = (String) veriLastMap.get("tesisId");
			if (veriLastMap.containsKey("departmanId"))
				departmanIdStr = (String) veriLastMap.get("departmanId");
			if (veriLastMap.containsKey("sicilNo"))
				sicilNo = (String) veriLastMap.get("sicilNo");
			if (veriLastMap.containsKey("hataliVeriGetir"))
				hataliVeriGetirStr = (String) veriLastMap.get("hataliVeriGetir");
			if (veriLastMap.containsKey("eksikCalisanVeriGetir"))
				eksikCalisanVeriGetirStr = (String) veriLastMap.get("eksikCalisanVeriGetir");

			if (yilStr != null && ayStr != null && sirketIdStr != null) {
				yil = Integer.parseInt(yilStr);
				ay = Integer.parseInt(ayStr);
				sirketId = Long.parseLong(sirketIdStr);
				if (tesisIdStr != null)
					tesisId = Long.parseLong(tesisIdStr);
				if (sirketId != null) {
					HashMap parametreMap = new HashMap();
					parametreMap.put("id", sirketId);
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					sirket = (Sirket) pdksEntityController.getObjectByInnerObject(parametreMap, Sirket.class);
					if (sirket != null)
						departmanId = sirket.getDepartman().getId();
				}
				if (departmanId == null && departmanIdStr != null)
					departmanId = Long.parseLong(departmanIdStr);
				fillSirketList();

			}
		}
		if (!authenticatedUser.isAdmin()) {
			if (departmanId == null)
				setDepartmanId(authenticatedUser.getDepartman().getId());
			if (authenticatedUser.isIK())
				fillSirketList();
		}

		// return ortakIslemler.yetkiIKAdmin(Boolean.FALSE);
		fillEkSahaTanim();
		if (hataliVeriGetirStr != null)
			hataliVeriGetir = new Boolean(hataliVeriGetirStr);
		if (eksikCalisanVeriGetirStr != null)
			eksikCalisanVeriGetir = new Boolean(eksikCalisanVeriGetirStr);

		bordroAdres = null;
		if (linkAdresKey != null)
			fillPersonelDenklestirmeList();

		return "";

	}

	public String personelNoDegisti() {
		if (personelDenklestirmeList != null)
			personelDenklestirmeList.clear();
		if (PdksUtil.hasStringValue(sicilNo))
			fillPersonelDenklestirmeList();
		return "";
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
		if (PdksUtil.hasStringValue(sicilNo))
			lastMap.put("sicilNo", sicilNo.trim());
		else {
			if (hataliVeriGetir != null)
				lastMap.put("hataliVeriGetir", "" + hataliVeriGetir);
			if (eksikCalisanVeriGetir != null)
				lastMap.put("eksikCalisanVeriGetir", "" + eksikCalisanVeriGetir);
		}

		try {
			lastMap.put("sayfaURL", sayfaURL);
			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception e) {

		}
	}

	public void filDepartmanList() {
		List<SelectItem> departmanListe = new ArrayList<SelectItem>();
		List<Departman> list = ortakIslemler.fillDepartmanTanimList(session);
		if (list.size() == 1) {
			departmanId = list.get(0).getId();
			fillSirketList();

		}

		for (Departman pdksDepartman : list)
			departmanListe.add(new SelectItem(pdksDepartman.getId(), pdksDepartman.getDepartmanTanim().getAciklama()));

		setDepartmanList(departmanListe);
	}

	public void fillTesisList() {
		personelDenklestirmeList.clear();
		List<SelectItem> selectItems = new ArrayList<SelectItem>();
		Long onceki = null;
		if (sirketId != null) {
			onceki = tesisId;
			HashMap parametreMap = new HashMap();
			parametreMap.put("id", sirketId);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

			Sirket sirket = (Sirket) pdksEntityController.getObjectByInnerObject(parametreMap, Sirket.class);
			if (sirket != null) {
				if (sirket.isTesisDurumu()) {
					HashMap fields = new HashMap();
					fields.put("ay", ay);
					fields.put("yil", yil);

					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					DenklestirmeAy denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
					selectItems = fazlaMesaiOrtakIslemler.getFazlaMesaiTesisList(sirket, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, true, session);
					if (!selectItems.isEmpty()) {
						if (selectItems.size() == 1)
							onceki = (Long) selectItems.get(0).getValue();
						else {
							onceki = null;
							for (SelectItem selectItem : selectItems) {
								if (selectItem.getValue().equals(tesisId))
									onceki = tesisId;
							}
						}
					}
				} else
					onceki = null;
			}

		} else
			tesisId = null;
		setTesisId(onceki);
		setTesisList(selectItems);
	}

	public void fillSirketList() {
		personelDenklestirmeList.clear();
		HashMap parametreMap = new HashMap();
		parametreMap.put("id", departmanId);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		if (departmanId != null)
			departman = (Departman) pdksEntityController.getObjectByInnerObject(parametreMap, Departman.class);
		else
			departman = null;

		HashMap fields = new HashMap();
		fields.put("ay", ay);
		fields.put("yil", yil);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
		List<SelectItem> sirketList = fazlaMesaiOrtakIslemler.getFazlaMesaiSirketList(departmanId, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, true, session);
		Long onceki = null;
		if (!sirketList.isEmpty()) {
			onceki = sirketId;
			if (sirketList.size() == 1) {
				sirketId = (Long) sirketList.get(0).getValue();
			} else if (sirketId != null) {
				sirketId = null;
				for (SelectItem selectItem : sirketList) {
					if (selectItem.getValue().equals(onceki))
						sirketId = onceki;

				}
			}

		} else
			sirketId = onceki;
		setSirketler(sirketList);

		if (sirketId != null)
			fillTesisList();
		else {
			tesisId = null;
			tesisList = null;
		}

		setPersonelDenklestirmeList(new ArrayList<AylikPuantaj>());

	}

	private void fillEkSahaTanim() {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
	}

	/**
	 * @param kod
	 * @return
	 */
	public String getBaslikAciklama(String kod) {
		String aciklama = "";
		if (baslikMap != null && kod != null && baslikMap.containsKey(kod)) {
			aciklama = baslikMap.get(kod).getAciklama();
		}
		return aciklama;
	}

	public String fillPersonelDenklestirmeList() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.clear();
		bordroAdres = null;
		aksamGun = Boolean.FALSE;
		aksamSaat = Boolean.FALSE;
		calismaModeliDurum = Boolean.FALSE;
		haftaCalisma = Boolean.FALSE;
		resmiTatilDurum = Boolean.FALSE;
		maasKesintiGoster = Boolean.FALSE;
		artikGunDurum = Boolean.FALSE;
		resmiTatilGunDurum = Boolean.FALSE;
		normalGunSaatDurum = Boolean.FALSE;
		haftaTatilSaatDurum = Boolean.FALSE;
		resmiTatilSaatDurum = Boolean.FALSE;
		izinSaatDurum = Boolean.FALSE;
		HashMap fields = new HashMap();
		fields.put("ay", ay);
		fields.put("yil", yil);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		ekSaha4Tanim = ortakIslemler.getEkSaha4(sirket, sirketId, session);
		personelDenklestirmeList.clear();
		denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
		if (denklestirmeAy.getDurum().equals(Boolean.FALSE)) {
			eksikCalisanVeriGetir = null;
			hataliVeriGetir = null;
		}

		basGun = null;
		bitGun = null;

		durumERP = Boolean.FALSE;
		onaylanmayanDurum = null;
		personelERP = Boolean.FALSE;
		if (personelDenklestirmeList == null)
			personelDenklestirmeList = new ArrayList<AylikPuantaj>();
		else
			personelDenklestirmeList.clear();
		TreeMap<Long, AylikPuantaj> eksikCalismaMap = new TreeMap<Long, AylikPuantaj>();

		if (denklestirmeAy != null) {
			basGun = PdksUtil.getYilAyBirinciGun(yil, ay);
			bitGun = PdksUtil.tariheAyEkleCikar(basGun, 1);
			String str = ortakIslemler.getParameterKey("bordroVeriOlustur");
			saveLastParameter();
			boolean sicilDolu = PdksUtil.hasStringValue(sicilNo);
			if (yil * 100 + ay >= Integer.parseInt(str)) {
				fields.clear();
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT  B.* FROM " + PersonelDenklestirme.TABLE_NAME + " V WITH(nolock) ");
				sb.append(" INNER JOIN  " + Personel.TABLE_NAME + " P ON  P." + Personel.COLUMN_NAME_ID + "=V." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
				sb.append(" AND  P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + "<=:bitGun AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=:basGun ");
				fields.put("basGun", basGun);
				fields.put("bitGun", bitGun);
				if (sirketId != null || (PdksUtil.hasStringValue(sicilNo))) {
					if (sirketId != null) {
						HashMap parametreMap = new HashMap();
						parametreMap.put("id", sirketId);
						sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + "= " + sirketId);
						if (session != null)
							parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						sirket = (Sirket) pdksEntityController.getObjectByInnerObject(parametreMap, Sirket.class);
					}
					if (sicilDolu) {
						sb.append(" AND P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + "=:sicilNo ");
						fields.put("sicilNo", ortakIslemler.getSicilNo(sicilNo.trim()));
					}
				}
				if (tesisId != null) {
					sb.append(" AND  P." + Personel.COLUMN_NAME_TESIS + "= " + tesisId);

				}
				sb.append(" INNER JOIN " + PersonelDenklestirmeBordro.TABLE_NAME + " B ON B." + PersonelDenklestirmeBordro.COLUMN_NAME_PERSONEL_DENKLESTIRME + "=V." + PersonelDenklestirme.COLUMN_NAME_ID);
				sb.append(" WHERE v." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=" + denklestirmeAy.getId() + " AND V." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + "=1");
				if (sicilDolu == false && (hataliVeriGetir == null || hataliVeriGetir == false))
					sb.append(" AND V." + PersonelDenklestirme.COLUMN_NAME_DURUM + "=1 AND V." + PersonelDenklestirme.COLUMN_NAME_ONAYLANDI + "=1");
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<PersonelDenklestirmeBordro> borDenklestirmeBordroList = pdksEntityController.getObjectBySQLList(sb, fields, PersonelDenklestirmeBordro.class);
				if (!borDenklestirmeBordroList.isEmpty()) {
					TreeMap<Long, PersonelDenklestirmeBordro> idMap = new TreeMap<Long, PersonelDenklestirmeBordro>();
					boolean saatlikCalismaVar = ortakIslemler.getParameterKey("saatlikCalismaVar").equals("1");
					for (PersonelDenklestirmeBordro personelDenklestirmeBordro : borDenklestirmeBordroList) {
						PersonelDenklestirme pd = personelDenklestirmeBordro.getPersonelDenklestirme();
						if (saatlikCalismaVar) {
							if (!normalGunSaatDurum)
								normalGunSaatDurum = personelDenklestirmeBordro.getSaatNormal() != null && personelDenklestirmeBordro.getSaatNormal().doubleValue() > 0.0d;
							if (!haftaTatilSaatDurum)
								haftaTatilSaatDurum = personelDenklestirmeBordro.getSaatHaftaTatil() != null && personelDenklestirmeBordro.getSaatHaftaTatil().doubleValue() > 0.0d;
							if (!resmiTatilSaatDurum)
								resmiTatilSaatDurum = personelDenklestirmeBordro.getSaatResmiTatil() != null && personelDenklestirmeBordro.getSaatResmiTatil().doubleValue() > 0.0d;
							if (!izinSaatDurum)
								izinSaatDurum = personelDenklestirmeBordro.getSaatIzin() != null && personelDenklestirmeBordro.getSaatIzin().doubleValue() > 0.0d;
						}
						if (!artikGunDurum)
							artikGunDurum = personelDenklestirmeBordro.getArtikAdet() != null && personelDenklestirmeBordro.getArtikAdet().doubleValue() > 0.0d;
						if (!resmiTatilGunDurum)
							resmiTatilGunDurum = personelDenklestirmeBordro.getResmiTatilAdet() != null && personelDenklestirmeBordro.getResmiTatilAdet().doubleValue() > 0.0d;
						if (!artikGunDurum)
							artikGunDurum = personelDenklestirmeBordro.getArtikAdet() != null && personelDenklestirmeBordro.getArtikAdet().doubleValue() > 0.0d;
						if (!haftaCalisma)
							haftaCalisma = pd.getHaftaCalismaSuresi() != null && pd.getHaftaCalismaSuresi().doubleValue() > 0.0d;
						if (!resmiTatilDurum)
							resmiTatilDurum = pd.getResmiTatilSure() != null && pd.getResmiTatilSure().doubleValue() > 0.0d;
						if (!aksamGun)
							setAksamGun(pd.getAksamVardiyaSayisi() != null && pd.getAksamVardiyaSayisi().doubleValue() > 0.0d);
						if (!aksamSaat)
							setAksamSaat(pd.getAksamVardiyaSaatSayisi() != null && pd.getAksamVardiyaSaatSayisi().doubleValue() > 0.0d);
						if (!maasKesintiGoster)
							setMaasKesintiGoster(pd.getEksikCalismaSure() != null && pd.getEksikCalismaSure().doubleValue() > 0.0d);

						personelDenklestirmeBordro.setDetayMap(new HashMap<BordroDetayTipi, PersonelDenklestirmeBordroDetay>());
						AylikPuantaj aylikPuantaj = new AylikPuantaj(personelDenklestirmeBordro);
						idMap.put(personelDenklestirmeBordro.getId(), personelDenklestirmeBordro);
						if (sicilDolu == false && pd.getDurum().equals(Boolean.TRUE) && (eksikCalisanVeriGetir != null && eksikCalisanVeriGetir)) {
							double normalSaat = 0.0d, planlananSaaat = 0.0d;
							CalismaModeli cm = pd.getCalismaModeli();
							if (cm != null) {
								try {
									normalSaat = pd.getHesaplananSure().doubleValue();
								} catch (Exception e) {
									normalSaat = 0.0d;
								}
								try {
									planlananSaaat = pd.getPlanlanSure().doubleValue() - cm.getHaftaIci();
								} catch (Exception e) {
									planlananSaaat = 0.0d;
								}
								if (normalSaat <= planlananSaaat)
									eksikCalismaMap.put(pd.getId(), aylikPuantaj);
							}

						}
						personelDenklestirmeList.add(aylikPuantaj);
					}

					fields.clear();
					fields.put("personelDenklestirmeBordro.id", new ArrayList(idMap.keySet()));
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<PersonelDenklestirmeBordroDetay> list = pdksEntityController.getObjectByInnerObjectList(fields, PersonelDenklestirmeBordroDetay.class);
					for (PersonelDenklestirmeBordroDetay detay : list) {
						Long key = detay.getPersonelDenklestirmeBordro().getId();
						BordroDetayTipi bordroDetayTipi = BordroDetayTipi.fromValue(detay.getTipi());
						idMap.get(key).getDetayMap().put(bordroDetayTipi, detay);
					}
					for (PersonelDenklestirmeBordro personelDenklestirmeBordro : borDenklestirmeBordroList) {
						if (!normalGunSaatDurum)
							normalGunSaatDurum = personelDenklestirmeBordro.getSaatNormal() != null && personelDenklestirmeBordro.getSaatNormal().doubleValue() > 0.0d;
						if (!haftaTatilSaatDurum)
							haftaTatilSaatDurum = personelDenklestirmeBordro.getSaatHaftaTatil() != null && personelDenklestirmeBordro.getSaatHaftaTatil().doubleValue() > 0.0d;
						if (!resmiTatilSaatDurum)
							resmiTatilSaatDurum = personelDenklestirmeBordro.getSaatResmiTatil() != null && personelDenklestirmeBordro.getSaatResmiTatil().doubleValue() > 0.0d;
						if (!izinSaatDurum)
							izinSaatDurum = personelDenklestirmeBordro.getSaatIzin() != null && personelDenklestirmeBordro.getSaatIzin().doubleValue() > 0.0d;
					}
					idMap = null;
					list = null;

				}
				if (!eksikCalismaMap.isEmpty()) {
					HashMap map = new HashMap();
					sb = new StringBuffer();
					sb.append("SELECT DISTINCT P.* FROM VARDIYA_GUN_SAAT_VIEW V WITH(nolock) ");
					sb.append(" INNER JOIN  " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + "=V." + VardiyaGun.COLUMN_NAME_PERSONEL);
 					sb.append(" AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ">=P." + Personel.getIseGirisTarihiColumn());
					sb.append(" AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + "<=P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI);
					sb.append(" INNER JOIN  " + Vardiya.TABLE_NAME + " VA ON VA." + Vardiya.COLUMN_NAME_ID + "=V." + VardiyaGun.COLUMN_NAME_VARDIYA + " AND VA.VARDIYATIPI=''");
					sb.append(" WHERE V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ">= :basTarih AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + "< :bitTarih  ");
					sb.append("   AND  V." + VardiyaGun.COLUMN_NAME_DURUM + " = 0 AND  V." + VardiyaSaat.COLUMN_NAME_CALISMA_SURESI + " = 0 ");
					sb.append("   AND  V." + VardiyaGun.COLUMN_NAME_PERSONEL + " :p");
					Date basTarih = PdksUtil.getDateFromString((yil * 100 + ay) + "01");
					Date bitTarih = PdksUtil.tariheAyEkleCikar(basTarih, 1);
					map.put("p", new ArrayList(eksikCalismaMap.keySet()));
					map.put("basTarih", basTarih);
					map.put("bitTarih", bitTarih);
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					map.put(PdksEntityController.MAP_KEY_MAP, "getId");
					try {
						TreeMap<Long, Personel> perMap = pdksEntityController.getObjectBySQLMap(sb, map, Personel.class, false);
						for (Long key : eksikCalismaMap.keySet()) {
							if (!perMap.containsKey(key))
								eksikCalismaMap.remove(key);
						}
						perMap = null;
					} catch (Exception e) {
						logger.error(e + "\n" + sb.toString());
					}

				}
				borDenklestirmeBordroList = null;
			}
		}
		baslikMap.clear();
		if (personelDenklestirmeList.isEmpty())
			PdksUtil.addMessageWarn("İlgili döneme ait fazla mesai bulunamadı!");
		else {
			List<Tanim> bordroAlanlari = ortakIslemler.getTanimList(Tanim.TIPI_BORDRDO_ALANLARI, session);
			if (bordroAlanlari.isEmpty()) {
				boolean kimlikNoGoster = false;
				String kartNoAciklama = ortakIslemler.getParameterKey("kartNoAciklama");
				Boolean kartNoAciklamaGoster = null;
				if (PdksUtil.hasStringValue(kartNoAciklama))
					kartNoAciklamaGoster = false;

				for (AylikPuantaj aylikPuantaj : personelDenklestirmeList) {
					Personel personel = aylikPuantaj.getPdksPersonel();
					PersonelKGS personelKGS = personel.getPersonelKGS();
					if (personelKGS != null) {
						if (kartNoAciklamaGoster != null && kartNoAciklamaGoster.booleanValue() == false) {
							kartNoAciklamaGoster = PdksUtil.hasStringValue(personelKGS.getKartNo());
							if (kartNoAciklamaGoster && kimlikNoGoster)
								break;
						}

						if (!kimlikNoGoster) {
							kimlikNoGoster = PdksUtil.hasStringValue(personelKGS.getKimlikNo());
							if (kimlikNoGoster && (kartNoAciklamaGoster == null || kartNoAciklamaGoster))
								break;
						}
					}
				}
				if (kartNoAciklamaGoster == null)
					kartNoAciklamaGoster = false;
				bordroBilgiAciklamaOlustur(kimlikNoGoster, kartNoAciklama, kartNoAciklamaGoster, bordroAlanlari);
			}
			for (Tanim tanim : bordroAlanlari)
				baslikMap.put(tanim.getKodu(), tanim);
			List<AylikPuantaj> puantajList = new ArrayList<AylikPuantaj>(ortakIslemler.sortAylikPuantajList(personelDenklestirmeList, false));
			personelDenklestirmeList.clear();
			List<AylikPuantaj> aktifList = new ArrayList<AylikPuantaj>(), aktifEksikList = new ArrayList<AylikPuantaj>();
			for (AylikPuantaj aylikPuantaj : puantajList) {
				PersonelDenklestirme pd = aylikPuantaj.getPersonelDenklestirmeAylik();
				boolean hataYok = pd.getDurum().equals(Boolean.TRUE), donemBitti = true;
				CalismaModeli cm = eksikCalismaMap.containsKey(pd.getId()) && hataYok && hataliVeriGetir != null && hataliVeriGetir ? pd.getCalismaModeli() : null;
				Double eksikCalismaSure = null;
				if (cm != null && eksikCalisanVeriGetir != null && eksikCalisanVeriGetir) {
					double normalSaat = 0.0d, planlananSaaat = 0.0d;
					try {
						planlananSaaat = pd.getPlanlanSure().doubleValue() - cm.getHaftaIci();
					} catch (Exception e) {
						planlananSaaat = 0.0d;
					}
					try {
						if (cm.isSaatlikOdeme()) {
							PersonelDenklestirmeBordro pdb = aylikPuantaj.getDenklestirmeBordro();
							normalSaat = pdb != null ? pdb.getSaatNormal().doubleValue() : 0.0d;
						} else {
							normalSaat = pd.getHesaplananSure().doubleValue();
						}
					} catch (Exception e) {
						normalSaat = 0.0d;
					}
					try {
						hataYok = normalSaat >= planlananSaaat;
						if (hataYok == false)
							eksikCalismaSure = normalSaat - (planlananSaaat + cm.getHaftaIci());
					} catch (Exception e) {
						hataYok = false;
					}
					donemBitti = hataYok;
				}
				aylikPuantaj.setEksikCalismaSure(eksikCalismaSure);
				aylikPuantaj.setDonemBitti(donemBitti);
				if (hataYok)
					aktifList.add(aylikPuantaj);
				else if (eksikCalismaSure != null)
					aktifEksikList.add(aylikPuantaj);
				else
					personelDenklestirmeList.add(aylikPuantaj);
			}
			if (!aktifEksikList.isEmpty())
				personelDenklestirmeList.addAll(aktifEksikList);
			if (!aktifList.isEmpty())
				personelDenklestirmeList.addAll(aktifList);
			puantajList = null;
			eksikCalismaMap = null;
			aktifEksikList = null;
			aktifList = null;

		}
		setInstance(denklestirmeAy);

		return "";
	}

	public String denklestirmeExcelAktar() {
		try {
			ByteArrayOutputStream baosDosya = null;

			baosDosya = denklestirmeExcelAktarDevam();
			if (baosDosya != null) {
				String dosyaAdi = "bordroVeri";
				if (sirket != null)
					dosyaAdi += "_" + sirket.getAd();
				if (tesisId != null) {
					HashMap parametreMap = new HashMap();
					parametreMap.put("id", tesisId);
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					Tanim tesis = (Tanim) pdksEntityController.getObjectByInnerObject(parametreMap, Tanim.class);
					if (tesis != null)
						dosyaAdi += "_" + tesis.getAciklama();
				}
				if (baosDosya != null)
					PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi + PdksUtil.convertToDateString(basGun, "_MMMMM_yyyy") + ".xlsx");
			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		return "";
	}

	/**
	 * @param sira
	 * @param adi
	 * @param aciklama
	 * @return
	 */
	private Tanim getBordroAlani(int sira, String adi, String aciklama) {
		Tanim tanim = new Tanim();
		tanim.setKodu(adi);
		tanim.setAciklamatr(aciklama);
		tanim.setAciklamaen(aciklama);
		tanim.setErpKodu(PdksUtil.textBaslangicinaKarakterEkle(String.valueOf(sira * 50), '0', 4));
		return tanim;

	}

	/**
	 * @return
	 */
	private ByteArrayOutputStream denklestirmeExcelAktarDevam() {
		ByteArrayOutputStream baos = null;
		try {
			boolean kimlikNoGoster = false;
			Boolean kartNoAciklamaGoster = null;
			String kartNoAciklama = ortakIslemler.getParameterKey("kartNoAciklama");

			if (PdksUtil.hasStringValue(kartNoAciklama))
				kartNoAciklamaGoster = false;

			if (kartNoAciklamaGoster == null)
				kartNoAciklamaGoster = false;
			List<AylikPuantaj> list = new ArrayList<AylikPuantaj>();
			for (AylikPuantaj aylikPuantaj : personelDenklestirmeList) {
				if (aylikPuantaj.getPersonelDenklestirmeAylik().getDurum().equals(Boolean.FALSE))
					continue;
				list.add(aylikPuantaj);
			}
			if (!list.isEmpty()) {
				for (AylikPuantaj aylikPuantaj : list) {
					Personel personel = aylikPuantaj.getPdksPersonel();
					PersonelKGS personelKGS = personel.getPersonelKGS();
					if (personelKGS != null) {
						if (kartNoAciklamaGoster != null && kartNoAciklamaGoster.booleanValue() == false) {
							kartNoAciklamaGoster = PdksUtil.hasStringValue(personelKGS.getKartNo());
							if (kartNoAciklamaGoster && kimlikNoGoster)
								break;
						}

						if (!kimlikNoGoster) {
							kimlikNoGoster = PdksUtil.hasStringValue(personelKGS.getKimlikNo());
							if (kimlikNoGoster && (kartNoAciklamaGoster == null || kartNoAciklamaGoster))
								break;
						}
					}
				}
				String ayAdi = null;
				for (SelectItem si : aylar) {
					if (si.getValue().equals(ay))
						ayAdi = si.getLabel();

				}
				List<Tanim> bordroAlanlari = ortakIslemler.getTanimList(Tanim.TIPI_BORDRDO_ALANLARI, session);

				bordroAlanlari = PdksUtil.sortObjectStringAlanList(bordroAlanlari, "getErpKodu", null);

				boolean tesisGoster = tesisList != null && !tesisList.isEmpty() && tesisId == null;
				Workbook wb = new XSSFWorkbook();
				sheet = ExcelUtil.createSheet(wb, PdksUtil.setTurkishStr(PdksUtil.convertToDateString(basGun, " MMMMM yyyy")) + " Liste", Boolean.TRUE);
				XSSFCellStyle headerSiyah = (XSSFCellStyle) ExcelUtil.getStyleHeader(wb);
				XSSFCellStyle header = (XSSFCellStyle) ExcelUtil.getStyleHeader(wb);
				CellStyle style = ExcelUtil.getStyleData(wb);
				CellStyle styleCenter = ExcelUtil.getStyleDataCenter(wb);
				tutarStyle = ExcelUtil.getCellStyleTutar(wb);
				numberStyle = ExcelUtil.getCellStyleTutar(wb);
				headerSiyah.getFont().setColor(ExcelUtil.getXSSFColor(255, 255, 255));
				XSSFCellStyle headerSaat = (XSSFCellStyle) headerSiyah.clone();
				XSSFCellStyle headerIzin = (XSSFCellStyle) headerSiyah.clone();
				XSSFCellStyle headerBGun = (XSSFCellStyle) headerSiyah.clone();
				XSSFCellStyle headerBTGun = (XSSFCellStyle) (XSSFCellStyle) headerSiyah.clone();
				ExcelUtil.setFillForegroundColor(headerSaat, 146, 208, 62);
				ExcelUtil.setFillForegroundColor(headerIzin, 255, 255, 255);
				ExcelUtil.setFillForegroundColor(headerBGun, 255, 255, 0);
				ExcelUtil.setFillForegroundColor(headerBTGun, 236, 125, 125);
				DataFormat df = wb.createDataFormat();
				numberStyle.setDataFormat(df.getFormat("###"));
				int row = 0, col = 0;
				calismaModeliDurum = Boolean.FALSE;
				for (Iterator iterator = bordroAlanlari.iterator(); iterator.hasNext();) {
					Tanim tanim = (Tanim) iterator.next();
					String kodu = tanim.getKodu();
					CellStyle baslikHeader = null;
					if (kodu.startsWith(COL_TESIS) && tesisGoster == false) {
						iterator.remove();
						continue;
					}

					if (kodu.startsWith(COL_KART_NO) && kartNoAciklamaGoster == false) {
						iterator.remove();
						continue;
					}
					if (kodu.startsWith(COL_KIMLIK_NO) && kimlikNoGoster == false) {
						iterator.remove();
						continue;
					}
					if (kodu.startsWith(COL_HAFTA_TATIL_MESAI) && haftaCalisma == false) {
						iterator.remove();
						continue;
					}
					if (kodu.startsWith(COL_AKSAM_SAAT_MESAI) && aksamSaat == false) {
						iterator.remove();
						continue;
					}
					if (kodu.startsWith(COL_EKSIK_CALISMA) && maasKesintiGoster == false) {
						iterator.remove();
						continue;
					}

					if (kodu.startsWith(COL_AKSAM_GUN_MESAI) && aksamGun == false) {
						iterator.remove();
						continue;
					}
					if (kodu.startsWith(COL_ALT_BOLUM) && ekSaha4Tanim == null) {
						iterator.remove();
						continue;
					}
					if (baslikHeader == null)
						baslikHeader = header;
					if (kodu.equals(COL_UCRETLI_IZIN) || kodu.equals(COL_RAPORLU_IZIN) || kodu.equals(COL_UCRETSIZ_IZIN))
						baslikHeader = headerIzin;
					else if (kodu.equals(COL_NORMAL_GUN_SAAT) || kodu.equals(COL_HAFTA_TATIL_SAAT) || kodu.equals(COL_RESMI_TATIL_SAAT) || kodu.equals(COL_IZIN_SAAT))
						baslikHeader = headerSaat;
					else if (kodu.equals(COL_NORMAL_GUN_ADET) || kodu.equals(COL_HAFTA_TATIL_ADET) || kodu.equals(COL_RESMI_TATIL_ADET) || kodu.equals(COL_ARTIK_ADET))
						baslikHeader = headerBGun;
					else if (kodu.equals(COL_TOPLAM_ADET))
						baslikHeader = headerBTGun;

					ExcelUtil.getCell(sheet, row, col++, baslikHeader).setCellValue(tanim.getAciklama());

				}

				for (AylikPuantaj ap : list) {
					Personel personel = ap.getPdksPersonel();
					PersonelDenklestirmeBordro denklestirmeBordro = ap.getDenklestirmeBordro();
					row++;
					col = 0;
					PersonelKGS personelKGS = personel.getPersonelKGS();
					for (Tanim tanim : bordroAlanlari) {
						String kodu = tanim.getKodu();
						if (kodu.startsWith(COL_CALISMA_MODELI)) {
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue(ap.getCalismaModeli().getAciklama());
						} else if (kodu.equals(COL_SIRA))
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(row);
						else if (kodu.equals(COL_YIL))
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(yil);
						else if (kodu.equals(COL_AY))
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(ay);
						else if (kodu.equals(COL_AY_ADI))
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(ayAdi);
						else if (kodu.equals(COL_PERSONEL_NO))
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getPdksSicilNo());
						else if (kodu.equals(COL_AD_SOYAD))
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
						else if (kodu.equals(COL_AD))
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAd());
						else if (kodu.equals(COL_SOYAD))
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getSoyad());
						else if (kodu.equals(COL_KART_NO)) {
							String kartNo = "";
							if (personelKGS != null && PdksUtil.hasStringValue(personelKGS.getKartNo()))
								kartNo = personelKGS.getKartNo();
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(kartNo);
						} else if (kodu.equals(COL_KIMLIK_NO)) {
							String kimlikNo = "";
							if (personelKGS != null && PdksUtil.hasStringValue(personelKGS.getKimlikNo()))
								kimlikNo = personelKGS.getKimlikNo();
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(kimlikNo);
						} else if (kodu.startsWith(COL_SIRKET)) {
							if (personel.getSirket() != null) {
								if (kodu.startsWith(COL_SIRKET + "Kodu"))
									ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getSirket().getErpKodu());
								else if (kodu.startsWith(COL_SIRKET))
									ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getSirket().getAd());
							} else
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
						} else if (kodu.startsWith(COL_TESIS)) {
							if (personel.getTesis() != null) {
								if (kodu.startsWith(COL_TESIS + "Kodu"))
									ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getTesis().getErpKodu());
								else if (kodu.startsWith(COL_TESIS))
									ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getTesis().getAciklama());
							} else
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
						} else if (kodu.equals(COL_BOLUM))
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
						else if (kodu.equals(COL_ALT_BOLUM))
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha4() != null ? personel.getEkSaha4().getAciklama() : "");
						else if (kodu.equals(COL_NORMAL_GUN_ADET))
							setExcelNumber(row, col++, denklestirmeBordro.getNormalGunAdet());
						else if (kodu.equals(COL_HAFTA_TATIL_ADET))
							setExcelNumber(row, col++, denklestirmeBordro.getHaftaTatilAdet());
						else if (kodu.equals(COL_RESMI_TATIL_ADET))
							setExcelNumber(row, col++, denklestirmeBordro.getResmiTatilAdet());
						else if (kodu.equals(COL_ARTIK_ADET))
							ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(denklestirmeBordro.getArtikAdet());
						else if (kodu.equals(COL_TOPLAM_ADET))
							setExcelNumber(row, col++, denklestirmeBordro.getBordroToplamGunAdet());
						else if (kodu.equals(COL_NORMAL_GUN_SAAT))
							setExcelNumber(row, col++, denklestirmeBordro.getSaatNormal());
						else if (kodu.equals(COL_HAFTA_TATIL_SAAT))
							setExcelNumber(row, col++, denklestirmeBordro.getSaatHaftaTatil());
						else if (kodu.equals(COL_RESMI_TATIL_SAAT))
							setExcelNumber(row, col++, denklestirmeBordro.getSaatResmiTatil());
						else if (kodu.equals(COL_IZIN_SAAT))
							setExcelNumber(row, col++, denklestirmeBordro.getSaatIzin());
						else if (kodu.equals(COL_UCRETLI_IZIN))
							ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(denklestirmeBordro.getUcretliIzin());
						else if (kodu.equals(COL_RAPORLU_IZIN))
							ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(denklestirmeBordro.getRaporluIzin());
						else if (kodu.equals(COL_UCRETSIZ_IZIN))
							ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(denklestirmeBordro.getUcretsizIzin());
						else if (kodu.equals(COL_RESMI_TATIL_MESAI)) {
							if (denklestirmeBordro.getResmiTatilMesai() > 0)
								setExcelNumber(row, col++, denklestirmeBordro.getResmiTatilMesai());
							else
								ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(0);
						} else if (kodu.equals(COL_UCRETI_ODENEN_MESAI)) {
							if (denklestirmeBordro.getUcretiOdenenMesai() > 0)
								setExcelNumber(row, col++, denklestirmeBordro.getUcretiOdenenMesai());
							else
								ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(0);
						} else if (kodu.equals(COL_HAFTA_TATIL_MESAI)) {
							if (denklestirmeBordro.getHaftaTatilMesai() > 0)
								setExcelNumber(row, col++, denklestirmeBordro.getHaftaTatilMesai());
							else
								ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(0);

						} else if (kodu.equals(COL_AKSAM_GUN_MESAI))
							ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(denklestirmeBordro.getAksamGunMesai());
						else if (kodu.equals(COL_AKSAM_SAAT_MESAI)) {
							if (denklestirmeBordro.getAksamSaatMesai() > 0)
								setExcelNumber(row, col++, denklestirmeBordro.getAksamSaatMesai());
							else
								ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(0);

						} else if (kodu.equals(COL_EKSIK_CALISMA)) {
							if (denklestirmeBordro.getEksikCalismaSure() > 0)
								setExcelNumber(row, col++, denklestirmeBordro.getEksikCalismaSure());
							else
								ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(0);

						}
					}

				}

				for (int i = 0; i < col; i++)
					sheet.autoSizeColumn(i);
				baos = new ByteArrayOutputStream();
				wb.write(baos);
			} else
				PdksUtil.addMessageWarn("Aktarılacak hatasız veri yok!");
			list = null;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return baos;
	}

	/**
	 * @param row
	 * @param col
	 * @param tutar
	 */
	private void setExcelNumber(int row, int col, Double tutar) {
		CellStyle style = tutar.doubleValue() - tutar.longValue() > 0.0d ? tutarStyle : numberStyle;
		ExcelUtil.getCell(sheet, row, col, style).setCellValue(tutar);
	}

	/**
	 * @param kimlikNoGoster
	 * @param kartNoAciklama
	 * @param kartNoAciklamaGoster
	 * @param bordroAlanlari
	 */
	private void bordroBilgiAciklamaOlustur(boolean kimlikNoGoster, String kartNoAciklama, Boolean kartNoAciklamaGoster, List<Tanim> bordroAlanlari) {
		int sira = 0;
		Tanim ekSaha4Tanim = ortakIslemler.getEkSaha4(sirket, sirketId, session);
		bordroAlanlari.add(getBordroAlani(++sira, COL_SIRA, "Sıra"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_YIL, "Yıl"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_AY, "Ay"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_PERSONEL_NO, ortakIslemler.personelNoAciklama()));
		bordroAlanlari.add(getBordroAlani(++sira, COL_AD_SOYAD, "Personel"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_SIRKET + "Kodu", ortakIslemler.sirketAciklama() + " Kodu"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_SIRKET, ortakIslemler.sirketAciklama()));
		if (ortakIslemler.isTesisDurumu()) {
			bordroAlanlari.add(getBordroAlani(++sira, COL_TESIS + "Kodu", ortakIslemler.tesisAciklama() + " Kodu"));
			bordroAlanlari.add(getBordroAlani(++sira, COL_TESIS, ortakIslemler.tesisAciklama()));
		}
		if (kartNoAciklamaGoster)
			bordroAlanlari.add(getBordroAlani(++sira, COL_KART_NO, kartNoAciklama));
		if (kimlikNoGoster)
			bordroAlanlari.add(getBordroAlani(++sira, COL_KIMLIK_NO, ortakIslemler.kimlikNoAciklama()));
		bordroAlanlari.add(getBordroAlani(++sira, COL_BOLUM, bolumAciklama));
		if (ekSaha4Tanim != null)
			bordroAlanlari.add(getBordroAlani(++sira, COL_ALT_BOLUM, ekSaha4Tanim.getAciklama()));
		bordroAlanlari.add(getBordroAlani(++sira, COL_NORMAL_GUN_ADET, "Normal Gün"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_HAFTA_TATIL_ADET, "H.Tatil Gün"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_RESMI_TATIL_ADET, "R.Tatil Gün"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_ARTIK_ADET, "Artık Gün"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_UCRETLI_IZIN, "Ücretli İzin Gün"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_RAPORLU_IZIN, "Raporlu (Hasta)"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_UCRETSIZ_IZIN, "Ücretsiz İzin Gün"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_UCRETI_ODENEN_MESAI, "Ücreti Ödenen Mesai"));
		if (maasKesintiGoster)
			bordroAlanlari.add(getBordroAlani(++sira, COL_EKSIK_CALISMA, ortakIslemler.eksikCalismaAciklama()));
		bordroAlanlari.add(getBordroAlani(++sira, COL_RESMI_TATIL_MESAI, "Resmi Tatil Mesai"));
		if (haftaCalisma)
			bordroAlanlari.add(getBordroAlani(++sira, COL_HAFTA_TATIL_MESAI, "Hafta Tatil Mesai"));
		if (aksamSaat)
			bordroAlanlari.add(getBordroAlani(++sira, COL_AKSAM_SAAT_MESAI, "Gece Saat"));
		if (aksamGun)
			bordroAlanlari.add(getBordroAlani(++sira, COL_AKSAM_SAAT_MESAI, "Gece Adet"));
		Date islemTarihi = new Date();
		for (Tanim tanim : bordroAlanlari) {
			tanim.setTipi(Tanim.TIPI_BORDRDO_ALANLARI);
			tanim.setIslemYapan(authenticatedUser);
			tanim.setIslemTarihi(islemTarihi);
			pdksEntityController.saveOrUpdate(session, entityManager, tanim);
		}
		session.flush();
	}

	public String getSicilNo() {
		return sicilNo;
	}

	public void setSicilNo(String sicilNo) {
		this.sicilNo = sicilNo;
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

	public Boolean getSecimDurum() {
		return secimDurum;
	}

	public void setSecimDurum(Boolean secimDurum) {
		this.secimDurum = secimDurum;
	}

	public Boolean getSureDurum() {
		return sureDurum;
	}

	public void setSureDurum(Boolean sureDurum) {
		this.sureDurum = sureDurum;
	}

	public Boolean getFazlaMesaiDurum() {
		return fazlaMesaiDurum;
	}

	public void setFazlaMesaiDurum(Boolean fazlaMesaiDurum) {
		this.fazlaMesaiDurum = fazlaMesaiDurum;
	}

	public Boolean getResmiTatilDurum() {
		return resmiTatilDurum;
	}

	public void setResmiTatilDurum(Boolean resmiTatilDurum) {
		this.resmiTatilDurum = resmiTatilDurum;
	}

	public Date getBasGun() {
		return basGun;
	}

	public void setBasGun(Date basGun) {
		this.basGun = basGun;
	}

	public Date getBitGun() {
		return bitGun;
	}

	public void setBitGun(Date bitGun) {
		this.bitGun = bitGun;
	}

	public Boolean getOnaylanmayanDurum() {
		return onaylanmayanDurum;
	}

	public void setOnaylanmayanDurum(Boolean onaylanmayanDurum) {
		this.onaylanmayanDurum = onaylanmayanDurum;
	}

	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket sirket) {
		this.sirket = sirket;
	}

	public List<SelectItem> getSirketler() {
		return sirketler;
	}

	public void setSirketler(List<SelectItem> sirketler) {
		this.sirketler = sirketler;
	}

	public Long getSirketId() {
		return sirketId;
	}

	public void setSirketId(Long sirketId) {
		this.sirketId = sirketId;
	}

	public List<SelectItem> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<SelectItem> departmanList) {
		this.departmanList = departmanList;
	}

	public Long getDepartmanId() {
		return departmanId;
	}

	public void setDepartmanId(Long departmanId) {
		this.departmanId = departmanId;
	}

	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman departman) {
		this.departman = departman;
	}

	public Dosya getFazlaMesaiDosya() {
		return fazlaMesaiDosya;
	}

	public void setFazlaMesaiDosya(Dosya fazlaMesaiDosya) {
		this.fazlaMesaiDosya = fazlaMesaiDosya;
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

	public Boolean getHaftaCalisma() {
		return haftaCalisma;
	}

	public void setHaftaCalisma(Boolean haftaCalisma) {
		this.haftaCalisma = haftaCalisma;
	}

	public Boolean getPersonelERP() {
		return personelERP;
	}

	public void setPersonelERP(Boolean personelERP) {
		this.personelERP = personelERP;
	}

	public Boolean getDurumERP() {
		return durumERP;
	}

	public void setDurumERP(Boolean durumERP) {
		this.durumERP = durumERP;
	}

	public Boolean getErpAktarimDurum() {
		return erpAktarimDurum;
	}

	public void setErpAktarimDurum(Boolean erpAktarimDurum) {
		this.erpAktarimDurum = erpAktarimDurum;
	}

	public Boolean getHaftaTatilDurum() {
		return haftaTatilDurum;
	}

	public void setHaftaTatilDurum(Boolean haftaTatilDurum) {
		this.haftaTatilDurum = haftaTatilDurum;
	}

	public Boolean getModelGoster() {
		return modelGoster;
	}

	public void setModelGoster(Boolean modelGoster) {
		this.modelGoster = modelGoster;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public List<Vardiya> getIzinTipiVardiyaList() {
		return izinTipiVardiyaList;
	}

	public void setIzinTipiVardiyaList(List<Vardiya> izinTipiVardiyaList) {
		this.izinTipiVardiyaList = izinTipiVardiyaList;
	}

	public TreeMap<String, TreeMap<String, List<VardiyaGun>>> getIzinTipiPersonelVardiyaMap() {
		return izinTipiPersonelVardiyaMap;
	}

	public void setIzinTipiPersonelVardiyaMap(TreeMap<String, TreeMap<String, List<VardiyaGun>>> izinTipiPersonelVardiyaMap) {
		this.izinTipiPersonelVardiyaMap = izinTipiPersonelVardiyaMap;
	}

	public TreeMap<Long, Personel> getIzinTipiPersonelMap() {
		return izinTipiPersonelMap;
	}

	public void setIzinTipiPersonelMap(TreeMap<Long, Personel> izinTipiPersonelMap) {
		this.izinTipiPersonelMap = izinTipiPersonelMap;
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

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}

	public int getMinYil() {
		return minYil;
	}

	public void setMinYil(int minYil) {
		this.minYil = minYil;
	}

	public List<AylikPuantaj> getPersonelDenklestirmeList() {
		return personelDenklestirmeList;
	}

	public void setPersonelDenklestirmeList(List<AylikPuantaj> personelDenklestirmeList) {
		this.personelDenklestirmeList = personelDenklestirmeList;
	}

	public Long getTesisId() {
		return tesisId;
	}

	public void setTesisId(Long tesisId) {
		this.tesisId = tesisId;
	}

	public List<SelectItem> getTesisList() {
		return tesisList;
	}

	public void setTesisList(List<SelectItem> tesisList) {
		this.tesisList = tesisList;
	}

	public String getCOL_SIRA() {
		return COL_SIRA;
	}

	public void setCOL_SIRA(String cOL_SIRA) {
		COL_SIRA = cOL_SIRA;
	}

	public String getCOL_YIL() {
		return COL_YIL;
	}

	public void setCOL_YIL(String cOL_YIL) {
		COL_YIL = cOL_YIL;
	}

	public String getCOL_AY() {
		return COL_AY;
	}

	public void setCOL_AY(String cOL_AY) {
		COL_AY = cOL_AY;
	}

	public String getCOL_AY_ADI() {
		return COL_AY_ADI;
	}

	public void setCOL_AY_ADI(String cOL_AY_ADI) {
		COL_AY_ADI = cOL_AY_ADI;
	}

	public String getCOL_PERSONEL_NO() {
		return COL_PERSONEL_NO;
	}

	public void setCOL_PERSONEL_NO(String cOL_PERSONEL_NO) {
		COL_PERSONEL_NO = cOL_PERSONEL_NO;
	}

	public String getCOL_AD() {
		return COL_AD;
	}

	public void setCOL_AD(String cOL_AD) {
		COL_AD = cOL_AD;
	}

	public String getCOL_SOYAD() {
		return COL_SOYAD;
	}

	public void setCOL_SOYAD(String cOL_SOYAD) {
		COL_SOYAD = cOL_SOYAD;
	}

	public String getCOL_AD_SOYAD() {
		return COL_AD_SOYAD;
	}

	public void setCOL_AD_SOYAD(String cOL_AD_SOYAD) {
		COL_AD_SOYAD = cOL_AD_SOYAD;
	}

	public String getCOL_KART_NO() {
		return COL_KART_NO;
	}

	public void setCOL_KART_NO(String cOL_KART_NO) {
		COL_KART_NO = cOL_KART_NO;
	}

	public String getCOL_KIMLIK_NO() {
		return COL_KIMLIK_NO;
	}

	public void setCOL_KIMLIK_NO(String cOL_KIMLIK_NO) {
		COL_KIMLIK_NO = cOL_KIMLIK_NO;
	}

	public String getCOL_SIRKET() {
		return COL_SIRKET;
	}

	public void setCOL_SIRKET(String cOL_SIRKET) {
		COL_SIRKET = cOL_SIRKET;
	}

	public String getCOL_TESIS() {
		return COL_TESIS;
	}

	public void setCOL_TESIS(String cOL_TESIS) {
		COL_TESIS = cOL_TESIS;
	}

	public String getCOL_BOLUM() {
		return COL_BOLUM;
	}

	public void setCOL_BOLUM(String cOL_BOLUM) {
		COL_BOLUM = cOL_BOLUM;
	}

	public String getCOL_ALT_BOLUM() {
		return COL_ALT_BOLUM;
	}

	public void setCOL_ALT_BOLUM(String cOL_ALT_BOLUM) {
		COL_ALT_BOLUM = cOL_ALT_BOLUM;
	}

	public String getCOL_NORMAL_GUN_ADET() {
		return COL_NORMAL_GUN_ADET;
	}

	public void setCOL_NORMAL_GUN_ADET(String cOL_NORMAL_GUN_ADET) {
		COL_NORMAL_GUN_ADET = cOL_NORMAL_GUN_ADET;
	}

	public String getCOL_HAFTA_TATIL_ADET() {
		return COL_HAFTA_TATIL_ADET;
	}

	public void setCOL_HAFTA_TATIL_ADET(String cOL_HAFTA_TATIL_ADET) {
		COL_HAFTA_TATIL_ADET = cOL_HAFTA_TATIL_ADET;
	}

	public String getCOL_UCRETLI_IZIN() {
		return COL_UCRETLI_IZIN;
	}

	public void setCOL_UCRETLI_IZIN(String cOL_UCRETLI_IZIN) {
		COL_UCRETLI_IZIN = cOL_UCRETLI_IZIN;
	}

	public String getCOL_RAPORLU_IZIN() {
		return COL_RAPORLU_IZIN;
	}

	public void setCOL_RAPORLU_IZIN(String cOL_RAPORLU_IZIN) {
		COL_RAPORLU_IZIN = cOL_RAPORLU_IZIN;
	}

	public String getCOL_UCRETSIZ_IZIN() {
		return COL_UCRETSIZ_IZIN;
	}

	public void setCOL_UCRETSIZ_IZIN(String cOL_UCRETSIZ_IZIN) {
		COL_UCRETSIZ_IZIN = cOL_UCRETSIZ_IZIN;
	}

	public String getCOL_RESMI_TATIL_MESAI() {
		return COL_RESMI_TATIL_MESAI;
	}

	public void setCOL_RESMI_TATIL_MESAI(String cOL_RESMI_TATIL_MESAI) {
		COL_RESMI_TATIL_MESAI = cOL_RESMI_TATIL_MESAI;
	}

	public String getCOL_UCRETI_ODENEN_MESAI() {
		return COL_UCRETI_ODENEN_MESAI;
	}

	public void setCOL_UCRETI_ODENEN_MESAI(String cOL_UCRETI_ODENEN_MESAI) {
		COL_UCRETI_ODENEN_MESAI = cOL_UCRETI_ODENEN_MESAI;
	}

	public String getCOL_HAFTA_TATIL_MESAI() {
		return COL_HAFTA_TATIL_MESAI;
	}

	public void setCOL_HAFTA_TATIL_MESAI(String cOL_HAFTA_TATIL_MESAI) {
		COL_HAFTA_TATIL_MESAI = cOL_HAFTA_TATIL_MESAI;
	}

	public String getCOL_AKSAM_SAAT_MESAI() {
		return COL_AKSAM_SAAT_MESAI;
	}

	public void setCOL_AKSAM_SAAT_MESAI(String cOL_AKSAM_SAAT_MESAI) {
		COL_AKSAM_SAAT_MESAI = cOL_AKSAM_SAAT_MESAI;
	}

	public String getCOL_AKSAM_GUN_MESAI() {
		return COL_AKSAM_GUN_MESAI;
	}

	public void setCOL_AKSAM_GUN_MESAI(String cOL_AKSAM_GUN_MESAI) {
		COL_AKSAM_GUN_MESAI = cOL_AKSAM_GUN_MESAI;
	}

	public TreeMap<String, Tanim> getBaslikMap() {
		return baslikMap;
	}

	public void setBaslikMap(TreeMap<String, Tanim> baslikMap) {
		this.baslikMap = baslikMap;
	}

	public Boolean getMaasKesintiGoster() {
		return maasKesintiGoster;
	}

	public void setMaasKesintiGoster(Boolean maasKesintiGoster) {
		this.maasKesintiGoster = maasKesintiGoster;
	}

	public String getCOL_EKSIK_CALISMA() {
		return COL_EKSIK_CALISMA;
	}

	public void setCOL_EKSIK_CALISMA(String cOL_EKSIK_CALISMA) {
		COL_EKSIK_CALISMA = cOL_EKSIK_CALISMA;
	}

	public String getCOL_RESMI_TATIL_ADET() {
		return COL_RESMI_TATIL_ADET;
	}

	public void setCOL_RESMI_TATIL_ADET(String cOL_RESMI_TATIL_ADET) {
		COL_RESMI_TATIL_ADET = cOL_RESMI_TATIL_ADET;
	}

	public String getCOL_ARTIK_ADET() {
		return COL_ARTIK_ADET;
	}

	public void setCOL_ARTIK_ADET(String cOL_ARTIK_ADET) {
		COL_ARTIK_ADET = cOL_ARTIK_ADET;
	}

	public Boolean getArtikGunDurum() {
		return artikGunDurum;
	}

	public void setArtikGunDurum(Boolean artikGunDurum) {
		this.artikGunDurum = artikGunDurum;
	}

	public Boolean getResmiTatilGunDurum() {
		return resmiTatilGunDurum;
	}

	public void setResmiTatilGunDurum(Boolean resmiTatilGunDurum) {
		this.resmiTatilGunDurum = resmiTatilGunDurum;
	}

	public String getCOL_NORMAL_GUN_SAAT() {
		return COL_NORMAL_GUN_SAAT;
	}

	public void setCOL_NORMAL_GUN_SAAT(String cOL_NORMAL_GUN_SAAT) {
		COL_NORMAL_GUN_SAAT = cOL_NORMAL_GUN_SAAT;
	}

	public String getCOL_HAFTA_TATIL_SAAT() {
		return COL_HAFTA_TATIL_SAAT;
	}

	public void setCOL_HAFTA_TATIL_SAAT(String cOL_HAFTA_TATIL_SAAT) {
		COL_HAFTA_TATIL_SAAT = cOL_HAFTA_TATIL_SAAT;
	}

	public String getCOL_RESMI_TATIL_SAAT() {
		return COL_RESMI_TATIL_SAAT;
	}

	public void setCOL_RESMI_TATIL_SAAT(String cOL_RESMI_TATIL_SAAT) {
		COL_RESMI_TATIL_SAAT = cOL_RESMI_TATIL_SAAT;
	}

	public String getCOL_IZIN_SAAT() {
		return COL_IZIN_SAAT;
	}

	public void setCOL_IZIN_SAAT(String cOL_IZIN_SAAT) {
		COL_IZIN_SAAT = cOL_IZIN_SAAT;
	}

	public Boolean getNormalGunSaatDurum() {
		return normalGunSaatDurum;
	}

	public void setNormalGunSaatDurum(Boolean normalGunSaatDurum) {
		this.normalGunSaatDurum = normalGunSaatDurum;
	}

	public Boolean getHaftaTatilSaatDurum() {
		return haftaTatilSaatDurum;
	}

	public void setHaftaTatilSaatDurum(Boolean haftaTatilSaatDurum) {
		this.haftaTatilSaatDurum = haftaTatilSaatDurum;
	}

	public Boolean getResmiTatilSaatDurum() {
		return resmiTatilSaatDurum;
	}

	public void setResmiTatilSaatDurum(Boolean resmiTatilSaatDurum) {
		this.resmiTatilSaatDurum = resmiTatilSaatDurum;
	}

	public Boolean getIzinSaatDurum() {
		return izinSaatDurum;
	}

	public void setIzinSaatDurum(Boolean izinSaatDurum) {
		this.izinSaatDurum = izinSaatDurum;
	}

	/**
	 * @return the hataliVeriGetir
	 */
	public Boolean getHataliVeriGetir() {
		return hataliVeriGetir;
	}

	/**
	 * @param hataliVeriGetir
	 *            the hataliVeriGetir to set
	 */
	public void setHataliVeriGetir(Boolean hataliVeriGetir) {
		this.hataliVeriGetir = hataliVeriGetir;
	}

	/**
	 * @return the denklestirmeAy
	 */
	public DenklestirmeAy getDenklestirmeAy() {
		return denklestirmeAy;
	}

	/**
	 * @param denklestirmeAy
	 *            the denklestirmeAy to set
	 */
	public void setDenklestirmeAy(DenklestirmeAy denklestirmeAy) {
		this.denklestirmeAy = denklestirmeAy;
	}

	/**
	 * @return the cOL_CALISMA_MODELI
	 */
	public String getCOL_CALISMA_MODELI() {
		return COL_CALISMA_MODELI;
	}

	/**
	 * @param cOL_CALISMA_MODELI
	 *            the cOL_CALISMA_MODELI to set
	 */
	public void setCOL_CALISMA_MODELI(String cOL_CALISMA_MODELI) {
		COL_CALISMA_MODELI = cOL_CALISMA_MODELI;
	}

	/**
	 * @return the calismaModeliDurum
	 */
	public Boolean getCalismaModeliDurum() {
		return calismaModeliDurum;
	}

	/**
	 * @param calismaModeliDurum
	 *            the calismaModeliDurum to set
	 */
	public void setCalismaModeliDurum(Boolean calismaModeliDurum) {
		this.calismaModeliDurum = calismaModeliDurum;
	}

	public Tanim getEkSaha4Tanim() {
		return ekSaha4Tanim;
	}

	public void setEkSaha4Tanim(Tanim ekSaha4Tanim) {
		this.ekSaha4Tanim = ekSaha4Tanim;
	}

	/**
	 * @return the linkAdresKey
	 */
	public String getLinkAdresKey() {
		return linkAdresKey;
	}

	/**
	 * @param linkAdresKey
	 *            the linkAdresKey to set
	 */
	public void setLinkAdresKey(String linkAdresKey) {
		this.linkAdresKey = linkAdresKey;
	}

	public Boolean getEksikCalisanVeriGetir() {
		return eksikCalisanVeriGetir;
	}

	public void setEksikCalisanVeriGetir(Boolean eksikCalisanVeriGetir) {
		this.eksikCalisanVeriGetir = eksikCalisanVeriGetir;
	}
}
