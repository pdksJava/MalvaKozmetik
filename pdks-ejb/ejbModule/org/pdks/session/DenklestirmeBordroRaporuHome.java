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
import java.util.TreeMap;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.AylikPuantaj;
import org.pdks.entity.BordroIzinGrubu;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Departman;
import org.pdks.entity.Dosya;
import org.pdks.entity.Liste;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelDenklestirmeBordro;
import org.pdks.entity.PersonelDenklestirmeBordroDetay;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
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

	private List<AylikPuantaj> personelDenklestirmeList;

	private Boolean secimDurum = Boolean.FALSE, sureDurum, fazlaMesaiDurum, haftaTatilDurum, resmiTatilDurum, durumERP, onaylanmayanDurum, personelERP, modelGoster = Boolean.FALSE;

	private int ay, yil, maxYil, minYil;

	private List<SelectItem> aylar;

	private String sicilNo = "", bolumAciklama;

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
	private String COL_TATIL_ADET = "tatilAdet";
	private String COL_UCRETLI_IZIN = "ucretliIzin";
	private String COL_RAPORLU_IZIN = "raporluIzin";
	private String COL_UCRETSIZ_IZIN = "ucretsizIzin";
	private String COL_RESMI_TATIL_MESAI = "resmiTatilMesai";
	private String COL_UCRETI_ODENEN_MESAI = "ucretiOdenenMesai";
	private String COL_HAFTA_TATIL_MESAI = "haftaTatilMesai";
	private String COL_AKSAM_SAAT_MESAI = "aksamSaatMesai";
	private String COL_AKSAM_GUN_MESAI = "aksamGunMesai";
	private String COL_EKSIK_CALISMA = "eksikCalisma";

	private Date basGun, bitGun;

	private Sirket sirket;

	private Long sirketId, departmanId, tesisId;

	private List<SelectItem> sirketler, departmanList, tesisList;

	private Departman departman;
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private Dosya fazlaMesaiDosya = new Dosya();
	private Boolean aksamGun = Boolean.FALSE, haftaCalisma = Boolean.FALSE, aksamSaat = Boolean.FALSE, erpAktarimDurum = Boolean.FALSE, maasKesintiGoster = Boolean.FALSE;
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

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() {
		aylar = PdksUtil.getAyListesi(Boolean.TRUE);
		String str = ortakIslemler.getParameterKey("bordroVeriOlustur");
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
		LinkedHashMap<String, Object> veriLastMap = ortakIslemler.getLastParameter("denklestirmeBordroRaporu", session);
		String yilStr = null;
		String ayStr = null;
		String sirketIdStr = null;
		String tesisIdStr = null;
		String departmanIdStr = null;

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

		if (sicilNo != null && sicilNo.trim().length() > 0)
			lastMap.put("sicilNo", sicilNo.trim());

		try {

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
		DenklestirmeAy denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
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
		aksamGun = Boolean.FALSE;
		aksamSaat = Boolean.FALSE;
		haftaCalisma = Boolean.FALSE;
		resmiTatilDurum = Boolean.FALSE;
		maasKesintiGoster = Boolean.FALSE;
		HashMap fields = new HashMap();
		fields.put("ay", ay);
		fields.put("yil", yil);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		personelDenklestirmeList.clear();
		DenklestirmeAy denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
		basGun = null;
		bitGun = null;

		durumERP = Boolean.FALSE;
		onaylanmayanDurum = null;
		personelERP = Boolean.FALSE;
		if (personelDenklestirmeList == null)
			personelDenklestirmeList = new ArrayList<AylikPuantaj>();
		else
			personelDenklestirmeList.clear();
		if (denklestirmeAy != null) {
			basGun = PdksUtil.getYilAyBirinciGun(yil, ay);
			bitGun = PdksUtil.tariheAyEkleCikar(basGun, 1);
			String str = ortakIslemler.getParameterKey("bordroVeriOlustur");
			saveLastParameter();
			if (yil * 100 + ay >= Integer.parseInt(str)) {
				fields.clear();
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT  B.* FROM " + PersonelDenklestirme.TABLE_NAME + " V WITH(nolock) ");
				sb.append(" INNER JOIN  " + Personel.TABLE_NAME + " P ON  P." + Personel.COLUMN_NAME_ID + "=V." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
				sb.append(" AND  P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + "<=:bitGun AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=:basGun ");
				fields.put("basGun", basGun);
				fields.put("bitGun", bitGun);
				if (sirketId != null || (sicilNo != null && sicilNo.length() > 0)) {
					if (sirketId != null) {
						HashMap parametreMap = new HashMap();
						parametreMap.put("id", sirketId);
						sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + "= " + sirketId);
						if (session != null)
							parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						sirket = (Sirket) pdksEntityController.getObjectByInnerObject(parametreMap, Sirket.class);
					}
					if (sicilNo != null && sicilNo.length() > 0) {
						sb.append(" AND P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + "=:sicilNo ");
						fields.put("sicilNo", sicilNo);
					}
				}
				if (tesisId != null) {
					sb.append(" AND  P." + Personel.COLUMN_NAME_TESIS + "= " + tesisId);

				}
				sb.append(" INNER JOIN " + PersonelDenklestirmeBordro.TABLE_NAME + " B ON B." + PersonelDenklestirmeBordro.COLUMN_NAME_PERSONEL_DENKLESTIRME + "=V." + PersonelDenklestirme.COLUMN_NAME_ID);
				sb.append(" WHERE v." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=" + denklestirmeAy.getId() + " AND V." + PersonelDenklestirme.COLUMN_NAME_DURUM + "=1  ");
				sb.append(" AND V." + PersonelDenklestirme.COLUMN_NAME_ONAYLANDI + "=1  AND V." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + "=1");
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<PersonelDenklestirmeBordro> borDenklestirmeBordroList = pdksEntityController.getObjectBySQLList(sb, fields, PersonelDenklestirmeBordro.class);
				if (!borDenklestirmeBordroList.isEmpty()) {
					HashMap<Long, PersonelDenklestirmeBordro> idMap = new HashMap<Long, PersonelDenklestirmeBordro>();
					for (PersonelDenklestirmeBordro personelDenklestirmeBordro : borDenklestirmeBordroList) {
						PersonelDenklestirme personelDenklestirme = personelDenklestirmeBordro.getPersonelDenklestirme();
						if (!haftaCalisma)
							haftaCalisma = personelDenklestirme.getHaftaCalismaSuresi() != null && personelDenklestirme.getHaftaCalismaSuresi().doubleValue() > 0.0d;
						if (!resmiTatilDurum)
							resmiTatilDurum = personelDenklestirme.getResmiTatilSure() != null && personelDenklestirme.getResmiTatilSure().doubleValue() > 0.0d;
						if (!aksamGun)
							setAksamGun(personelDenklestirme.getAksamVardiyaSayisi() != null && personelDenklestirme.getAksamVardiyaSayisi().doubleValue() > 0.0d);
						if (!aksamSaat)
							setAksamSaat(personelDenklestirme.getAksamVardiyaSaatSayisi() != null && personelDenklestirme.getAksamVardiyaSaatSayisi().doubleValue() > 0.0d);
						if (!maasKesintiGoster)
							setMaasKesintiGoster(personelDenklestirme.getEksikCalismaSure() != null && personelDenklestirme.getEksikCalismaSure().doubleValue() > 0.0d);

						personelDenklestirmeBordro.setDetayMap(new HashMap<BordroIzinGrubu, PersonelDenklestirmeBordroDetay>());
						AylikPuantaj aylikPuantaj = new AylikPuantaj(personelDenklestirmeBordro);
						idMap.put(personelDenklestirmeBordro.getId(), personelDenklestirmeBordro);
						personelDenklestirmeList.add(aylikPuantaj);
					}
					personelDenklestirmeList = PdksUtil.sortObjectStringAlanList(personelDenklestirmeList, "getAdSoyad", null);
					boolean tesisGoster = tesisList != null && !tesisList.isEmpty() && tesisId == null;
					HashMap<String, Liste> listeMap = new HashMap<String, Liste>();
					for (AylikPuantaj aylikPuantaj : personelDenklestirmeList) {
						Personel personel = aylikPuantaj.getPdksPersonel();
						String key = (tesisGoster && personel.getTesis() != null ? personel.getTesis().getAciklama() + "_" : "") + personel.getEkSaha3().getAciklama();
						Liste liste = listeMap.containsKey(key) ? listeMap.get(key) : new Liste(key, new ArrayList<AylikPuantaj>());
						List<AylikPuantaj> list = (List<AylikPuantaj>) liste.getValue();
						if (list.isEmpty())
							listeMap.put(key, liste);
						list.add(aylikPuantaj);
					}
					List<Liste> listeler = PdksUtil.sortObjectStringAlanList(new ArrayList(listeMap.values()), "getId", null);
					personelDenklestirmeList.clear();
					for (Liste liste : listeler) {
						List<AylikPuantaj> list = (List<AylikPuantaj>) liste.getValue();
						personelDenklestirmeList.addAll(list);
					}
					listeMap = null;
					listeler = null;
					fields.clear();
					fields.put("personelDenklestirmeBordro.id", new ArrayList(idMap.keySet()));
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<PersonelDenklestirmeBordroDetay> list = pdksEntityController.getObjectByInnerObjectList(fields, PersonelDenklestirmeBordroDetay.class);
					for (PersonelDenklestirmeBordroDetay detay : list) {
						Long key = detay.getPersonelDenklestirmeBordro().getId();
						BordroIzinGrubu bordroIzinGrubu = BordroIzinGrubu.fromValue(detay.getTipi());
						idMap.get(key).getDetayMap().put(bordroIzinGrubu, detay);
					}
					idMap = null;
					list = null;

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
				if (!kartNoAciklama.equals(""))
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

		}
		setInstance(denklestirmeAy);

		return "";
	}

	public String denklestirmeExcelAktar() {
		try {
			ByteArrayOutputStream baosDosya = null;
			String dosyaAdi = null;
			dosyaAdi = "bordroVeri";
			baosDosya = denklestirmeExcelAktarDevam();
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

			String ayAdi = null;
			for (SelectItem si : aylar) {
				if (si.getValue().equals(ay))
					ayAdi = si.getLabel();

			}
			boolean kimlikNoGoster = false;
			String kartNoAciklama = ortakIslemler.getParameterKey("kartNoAciklama");
			Boolean kartNoAciklamaGoster = null;
			if (!kartNoAciklama.equals(""))
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
			List<Tanim> bordroAlanlari = ortakIslemler.getTanimList(Tanim.TIPI_BORDRDO_ALANLARI, session);
			Tanim ekSaha4Tanim = ortakIslemler.getEkSaha4(sirket, sirketId, session);

			bordroAlanlari = PdksUtil.sortObjectStringAlanList(bordroAlanlari, "getErpKodu", null);

			boolean tesisGoster = tesisList != null && !tesisList.isEmpty() && tesisId == null;
			Workbook wb = new XSSFWorkbook();
			Sheet sheet = ExcelUtil.createSheet(wb, PdksUtil.setTurkishStr(PdksUtil.convertToDateString(basGun, " MMMMM yyyy")) + " Liste", Boolean.TRUE);
			CellStyle style = ExcelUtil.getStyleData(wb);
			CellStyle styleCenter = ExcelUtil.getStyleData(wb);
			styleCenter.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			CellStyle stytleNumeric = ExcelUtil.getStyleData(wb);
			stytleNumeric.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
			CellStyle header = ExcelUtil.getStyleHeader(wb);
			CellStyle tutarStyle = ExcelUtil.getCellStyleTutar(wb);
			CellStyle numberStyle = ExcelUtil.getCellStyleTutar(wb);
			DataFormat df = wb.createDataFormat();
			numberStyle.setDataFormat(df.getFormat("###"));
			int row = 0, col = 0;
			for (Iterator iterator = bordroAlanlari.iterator(); iterator.hasNext();) {
				Tanim tanim = (Tanim) iterator.next();
				String kodu = tanim.getKodu();
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
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(tanim.getAciklama());

			}

			for (AylikPuantaj ap : personelDenklestirmeList) {
				Personel personel = ap.getPdksPersonel();
				PersonelDenklestirmeBordro denklestirmeBordro = ap.getDenklestirmeBordro();
				row++;
				col = 0;
				PersonelKGS personelKGS = personel.getPersonelKGS();
				for (Tanim tanim : bordroAlanlari) {
					String kodu = tanim.getKodu();
					if (kodu.equals(COL_SIRA))
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
						ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(denklestirmeBordro.getNormalGunAdet());
					else if (kodu.equals(COL_HAFTA_TATIL_ADET))
						ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(denklestirmeBordro.getHaftaTatilAdet());
					else if (kodu.equals(COL_TATIL_ADET))
						ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(denklestirmeBordro.getTatilAdet());
					else if (kodu.equals(COL_UCRETLI_IZIN))
						ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(denklestirmeBordro.getUcretliIzin());
					else if (kodu.equals(COL_RAPORLU_IZIN))
						ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(denklestirmeBordro.getRaporluIzin());
					else if (kodu.equals(COL_UCRETSIZ_IZIN))
						ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(denklestirmeBordro.getUcretsizIzin());
					else if (kodu.equals(COL_RESMI_TATIL_MESAI)) {
						if (denklestirmeBordro.getResmiTatilMesai() > 0)
							ExcelUtil.getCell(sheet, row, col++, tutarStyle).setCellValue(denklestirmeBordro.getResmiTatilMesai());
						else
							ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(0);
					} else if (kodu.equals(COL_UCRETI_ODENEN_MESAI)) {
						if (denklestirmeBordro.getUcretiOdenenMesai() > 0)
							ExcelUtil.getCell(sheet, row, col++, tutarStyle).setCellValue(denklestirmeBordro.getUcretiOdenenMesai());
						else
							ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(0);
					} else if (kodu.equals(COL_HAFTA_TATIL_MESAI)) {
						if (denklestirmeBordro.getHaftaTatilMesai() > 0)
							ExcelUtil.getCell(sheet, row, col++, tutarStyle).setCellValue(denklestirmeBordro.getHaftaTatilMesai());
						else
							ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(0);

					} else if (kodu.equals(COL_AKSAM_GUN_MESAI))
						ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(denklestirmeBordro.getAksamGunMesai());
					else if (kodu.equals(COL_AKSAM_SAAT_MESAI)) {
						if (denklestirmeBordro.getAksamSaatMesai() > 0)
							ExcelUtil.getCell(sheet, row, col++, tutarStyle).setCellValue(denklestirmeBordro.getAksamSaatMesai());
						else
							ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(0);

					} else if (kodu.equals(COL_EKSIK_CALISMA)) {
						if (denklestirmeBordro.getEksikCalismaSure() > 0)
							ExcelUtil.getCell(sheet, row, col++, tutarStyle).setCellValue(denklestirmeBordro.getEksikCalismaSure());
						else
							ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(0);

					}
				}

			}

			for (int i = 0; i < col; i++)
				sheet.autoSizeColumn(i);
			baos = new ByteArrayOutputStream();
			wb.write(baos);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return baos;
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
		bordroAlanlari.add(getBordroAlani(++sira, COL_TATIL_ADET, "G.Tatil Gün"));
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

	public String getCOL_TATIL_ADET() {
		return COL_TATIL_ADET;
	}

	public void setCOL_TATIL_ADET(String cOL_TATIL_ADET) {
		COL_TATIL_ADET = cOL_TATIL_ADET;
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
}
