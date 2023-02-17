package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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
import org.pdks.entity.BordroTipi;
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

	private Date basGun, bitGun;

	private Sirket sirket;

	private Long sirketId, departmanId, tesisId;

	private List<SelectItem> sirketler, departmanList, tesisList;

	private Departman departman;
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private Dosya fazlaMesaiDosya = new Dosya();
	private Boolean aksamGun = Boolean.FALSE, haftaCalisma = Boolean.FALSE, aksamSaat = Boolean.FALSE, erpAktarimDurum = Boolean.FALSE;
	private List<Vardiya> izinTipiVardiyaList;
	private TreeMap<String, TreeMap<String, List<VardiyaGun>>> izinTipiPersonelVardiyaMap;
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
			// TODO: handle exception
		}

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
			if (sirket != null && sirket.isTesisDurumu()) {
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

		}
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

	public String fillPersonelDenklestirmeList() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.clear();
		aksamGun = Boolean.FALSE;
		aksamSaat = Boolean.FALSE;
		haftaCalisma = Boolean.FALSE;
		resmiTatilDurum = Boolean.FALSE;
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
				sb.append(" AND  P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + "<:bitGun AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=:basGun ");
				sb.append(" AND  P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + "<:bitGun AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=:basGun ");
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
					sb.append(" AND  P." + Personel.COLUMN_NAME_TESIS + "=:t ");
					fields.put("t", tesisId);

				}
				sb.append(" INNER JOIN " + PersonelDenklestirmeBordro.TABLE_NAME + " B ON B." + PersonelDenklestirmeBordro.COLUMN_NAME_PERSONEL_DENKLESTIRME + "=V." + PersonelDenklestirme.COLUMN_NAME_ID);
				sb.append(" WHERE v." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=:denklestirmeAy AND V." + PersonelDenklestirme.COLUMN_NAME_DURUM + "=1  ");
				sb.append(" AND V." + PersonelDenklestirme.COLUMN_NAME_ONAYLANDI + "=1  AND V." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + "=1");
				fields.put("denklestirmeAy", denklestirmeAy.getId());
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
							aksamGun = personelDenklestirme.getAksamVardiyaSayisi() != null && personelDenklestirme.getAksamVardiyaSayisi().doubleValue() > 0.0d;
						if (!aksamSaat)
							aksamSaat = personelDenklestirme.getAksamVardiyaSaatSayisi() != null && personelDenklestirme.getAksamVardiyaSaatSayisi().doubleValue() > 0.0d;
						personelDenklestirmeBordro.setDetayMap(new HashMap<BordroTipi, PersonelDenklestirmeBordroDetay>());
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
						BordroTipi bordroTipi = BordroTipi.fromValue(detay.getTipi());
						idMap.get(key).getDetayMap().put(bordroTipi, detay);
					}
					idMap = null;
					list = null;

				}
				borDenklestirmeBordroList = null;
			}
		}
		if (personelDenklestirmeList.isEmpty())
			PdksUtil.addMessageWarn("İlgili döneme ait fazla mesai bulunamadı!");
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
	 * @return
	 */
	private ByteArrayOutputStream denklestirmeExcelAktarDevam() {
		ByteArrayOutputStream baos = null;
		try {
			boolean kimlikNoGoster = false;
			for (AylikPuantaj aylikPuantaj : personelDenklestirmeList) {
				Personel personel = aylikPuantaj.getPdksPersonel();
				if (!kimlikNoGoster) {
					PersonelKGS personelKGS = personel.getPersonelKGS();
					if (personelKGS != null)
						kimlikNoGoster = PdksUtil.hasStringValue(personelKGS.getKimlikNo());
					if (kimlikNoGoster)
						break;
				}
			}
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
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Sıra");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Personel");
			if (tesisGoster) {
				String aciklama = ortakIslemler.tesisAciklama();
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aciklama + " Kodu");
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aciklama);
			}
			if (kimlikNoGoster)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.kimlikNoAciklama());
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Normal Gün");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("H.Tatil Gün");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("G.Tatil Gün");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Ücretli İzin Gün");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Raporlu (Hasta)");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Ücretsiz İzin Gün");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Resmi Tatil Mesai");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Ücreti Ödenen Mesai");
			if (haftaCalisma)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Hafta Tatil Mesai");
			if (aksamSaat)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Gece Saat");
			for (AylikPuantaj ap : personelDenklestirmeList) {
				Personel personel = ap.getPdksPersonel();
				PersonelDenklestirmeBordro denklestirmeBordro = ap.getDenklestirmeBordro();
				row++;
				col = 0;
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(row);
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getPdksSicilNo());
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
				if (kimlikNoGoster) {
					PersonelKGS personelKGS = personel.getPersonelKGS();
					String kimlikNo = "";
					if (personelKGS != null && PdksUtil.hasStringValue(personelKGS.getKimlikNo()))
						kimlikNo = personelKGS.getKimlikNo();
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(kimlikNo);
				}
				if (tesisGoster) {
					if (personel.getTesis() != null) {
						ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getTesis().getErpKodu());
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getTesis().getAciklama());
					} else {
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
					}
				}
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
				ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(denklestirmeBordro.getNormalGunAdet());
				ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(denklestirmeBordro.getHaftaTatilAdet());
				ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(denklestirmeBordro.getTatilAdet());
				ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(denklestirmeBordro.getUcretliIzin());
				ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(denklestirmeBordro.getRaporluIzin());
				ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(denklestirmeBordro.getUcretsizIzin());
				if (denklestirmeBordro.getResmiTatilMesai() > 0)
					ExcelUtil.getCell(sheet, row, col++, tutarStyle).setCellValue(denklestirmeBordro.getResmiTatilMesai());
				else
					ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(0);
				if (denklestirmeBordro.getUcretiOdenenMesai() > 0)
					ExcelUtil.getCell(sheet, row, col++, tutarStyle).setCellValue(denklestirmeBordro.getUcretiOdenenMesai());
				else
					ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(0);
				if (haftaCalisma) {
					if (denklestirmeBordro.getHaftaTatilMesai() > 0)
						ExcelUtil.getCell(sheet, row, col++, tutarStyle).setCellValue(denklestirmeBordro.getHaftaTatilMesai());
					else
						ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(0);
				}

				if (aksamSaat) {
					if (denklestirmeBordro.getAksamSaatMesai() > 0)
						ExcelUtil.getCell(sheet, row, col++, tutarStyle).setCellValue(denklestirmeBordro.getAksamSaatMesai());
					else
						ExcelUtil.getCell(sheet, row, col++, numberStyle).setCellValue(0);
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
}
