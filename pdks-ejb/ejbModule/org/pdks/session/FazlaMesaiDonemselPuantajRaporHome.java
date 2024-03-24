package org.pdks.session;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
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
import org.pdks.entity.AylikPuantaj;
import org.pdks.entity.CalismaModeli;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.DepartmanDenklestirmeDonemi;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.VardiyaSaat;
import org.pdks.security.action.UserHome;
import org.pdks.security.entity.User;

@Name("fazlaMesaiDonemselPuantajRaporHome")
public class FazlaMesaiDonemselPuantajRaporHome extends EntityHome<DepartmanDenklestirmeDonemi> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7516859224980927543L;

	static Logger logger = Logger.getLogger(FazlaMesaiDonemselPuantajRaporHome.class);

	public static String sayfaURL = "fazlaMesaiDonemselPuantajRapor";

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

	@In(required = true, create = true)
	Renderer renderer;

	private Integer basYil, bitYil, sonDonem, basAy, bitAy, maxYil;
	private Long sirketId, tesisId, bolumId;
	private List<SelectItem> donemBas, donemBit, sirketler, tesisler, bolumler;
	private List<PersonelDenklestirme> perDenkList;
	private List<Personel> personelList;
	private List<DenklestirmeAy> denklestirmeAyList;
	private HashMap<String, PersonelDenklestirme> perDenkMap;
	private PersonelDenklestirme denklestirme;
	private boolean tesisVar = false, fazlaMesaiOde = false, fazlaMesaiIzinKullan = false, fazlaMesaiVar = false, saatlikMesaiVar = false, aylikMesaiVar = false, resmiTatilVar = false, haftaTatilVar = false;
	private boolean bordroPuantajEkranindaGoster = false, kismiOdemeGoster = false, yasalFazlaCalismaAsanSaat = false;
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private String bolumAciklama;
	private Date basTarih, bitTarih;
	private Sirket sirket = null;
	private List<AylikPuantaj> puantajList;
	private VardiyaGun vardiyaGun = null;
	private Personel seciliPersonel = null;
	private AylikPuantaj gunAylikPuantaj;
	private Session session;

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {

		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		if (donemBas == null)
			donemBas = new ArrayList<SelectItem>();
		if (donemBit == null)
			donemBit = new ArrayList<SelectItem>();
		if (sirketler == null)
			sirketler = new ArrayList<SelectItem>();

		if (tesisler == null)
			tesisler = new ArrayList<SelectItem>();
		tesisler.clear();
		if (bolumler == null)
			bolumler = new ArrayList<SelectItem>();
		else
			bolumler.clear();
		if (puantajList == null)
			puantajList = new ArrayList<AylikPuantaj>();
		else
			puantajList.clear();

		if (personelList == null)
			personelList = new ArrayList<Personel>();
		if (denklestirmeAyList == null)
			denklestirmeAyList = new ArrayList<DenklestirmeAy>();
		if (perDenkMap == null)
			perDenkMap = new HashMap<String, PersonelDenklestirme>();

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		basYil = calendar.get(Calendar.YEAR);
		bitYil = calendar.get(Calendar.YEAR);
		maxYil = bitYil;
		basAy = calendar.get(Calendar.MONTH) + 1;
		bitAy = calendar.get(Calendar.MONTH) + 1;
		sonDonem = (bitYil * 100) + bitAy;
		LinkedHashMap<String, Object> veriLastMap = ortakIslemler.getLastParameter(sayfaURL, session);
		if (veriLastMap != null) {
			if (veriLastMap.containsKey("basYil"))
				basYil = Integer.parseInt((String) veriLastMap.get("basYil"));
			if (veriLastMap.containsKey("basAy"))
				basAy = Integer.parseInt((String) veriLastMap.get("basAy"));
			if (veriLastMap.containsKey("bitYil"))
				bitYil = Integer.parseInt((String) veriLastMap.get("bitYil"));
			if (veriLastMap.containsKey("bitAy"))
				bitAy = Integer.parseInt((String) veriLastMap.get("bitAy"));
			if (veriLastMap.containsKey("sirketId"))
				sirketId = Long.parseLong((String) veriLastMap.get("sirketId"));
			if (veriLastMap.containsKey("tesisId"))
				tesisId = Long.parseLong((String) veriLastMap.get("tesisId"));
			if (veriLastMap.containsKey("bolumId"))
				bolumId = Long.parseLong((String) veriLastMap.get("bolumId"));
			veriLastMap = null;
		}
		fillEkSahaTanim();
		ayDoldur(basYil, donemBas, false);
		ayDoldur(bitYil, donemBit, true);

	}

	/**
	 * 
	 */
	@Transactional
	private void saveLastParameter() {
		LinkedHashMap<String, Object> lastMap = new LinkedHashMap<String, Object>();
		lastMap.put("basYil", "" + basYil);
		lastMap.put("bitYil", "" + bitYil);
		if (basAy != null)
			lastMap.put("basAy", "" + basAy);
		if (bitAy != null)
			lastMap.put("bitAy", "" + bitAy);
		if (sirketId != null)
			lastMap.put("sirketId", "" + sirketId);
		if (tesisId != null)
			lastMap.put("tesisId", "" + tesisId);
		if (bolumId != null)
			lastMap.put("bolumId", "" + bolumId);
		lastMap.put("sayfaURL", sayfaURL);
		try {
			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception e) {

		}
	}

	/**
	 * @param denklestirmeAy
	 * @param personel
	 * @return
	 */
	public PersonelDenklestirme getPersonelDenklestirme(DenklestirmeAy denklestirmeAy, Personel personel) {
		if (perDenkMap != null)
			denklestirme = perDenkMap.get(denklestirmeAy.getId() + "_" + personel.getId());
		else
			denklestirme = null;
		return denklestirme;
	}

	public String tesisDoldur() {
		personelList.clear();
		tesisler.clear();
		StringBuffer sb = new StringBuffer();
		bolumler.clear();
		try {
			sirket = null;
			boolean idVar = false;
			if (sirketId != null) {
				HashMap fields = new HashMap();
				fields.put("id", sirketId);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);
				fields.clear();
				if (sirket.getTesisDurum()) {
					sb.append("select DISTINCT TE.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
					sb.append(" INNER  JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
					sb.append(" AND PD." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + " = 1   ");
					personelSQLBagla(sb, fields);
					sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + " = " + sirketId);
					sb.append(" INNER JOIN " + Tanim.TABLE_NAME + " TE ON TE." + Tanim.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_TESIS);
					donemSQLKontrol(sb);
					sb.append(" AND ((D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+" + DenklestirmeAy.COLUMN_NAME_AY + ")<=:s");
					fields.put("s", sonDonem);
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<Tanim> list = pdksEntityController.getObjectBySQLList(sb, fields, Tanim.class);

					if (list.isEmpty()) {
						tesisId = null;
					} else {
						list = PdksUtil.sortObjectStringAlanList(list, "getAciklama", null);
						for (Tanim tanim : list) {
							if (tesisId != null && tanim.getId().equals(tesisId))
								idVar = true;
							tesisler.add(new SelectItem(tanim.getId(), tanim.getAciklama()));
						}
					}
				} else {
					bolumDoldur();
				}

			}
			if (!idVar)
				tesisId = null;
		} catch (Exception e) {
		}
		return "";
	}

	public String bolumDoldur() {
		personelList.clear();
		bolumler.clear();
		StringBuffer sb = new StringBuffer();
		try {
			boolean idVar = false;
			if (sirketId != null || tesisId != null) {
				HashMap fields = new HashMap();
				sb.append("select DISTINCT BO.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
				sb.append(" INNER  JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
				sb.append(" AND PD." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + " = 1   ");
				personelSQLBagla(sb, fields);
				if (tesisId != null) {
					sb.append(" AND P." + Personel.COLUMN_NAME_TESIS + " = " + tesisId);

				}
				if (sirket.getSirketGrup() == null) {
					sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + " = " + sirketId);
				} else {
					sb.append(" INNER JOIN " + Sirket.TABLE_NAME + " S ON S." + Sirket.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_SIRKET);
					sb.append(" AND S." + Sirket.COLUMN_NAME_SIRKET_GRUP + " = " + sirket.getSirketGrup().getId());
				}
				sb.append(" INNER JOIN " + Tanim.TABLE_NAME + " BO ON BO." + Tanim.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_EK_SAHA3);
				donemSQLKontrol(sb);
				sb.append(" AND ((D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+" + DenklestirmeAy.COLUMN_NAME_AY + ")<=:s");

				fields.put("s", sonDonem);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<Tanim> list = pdksEntityController.getObjectBySQLList(sb, fields, Tanim.class);

				if (list.isEmpty()) {
					bolumId = null;
				} else {
					list = PdksUtil.sortObjectStringAlanList(list, "getAciklama", null);
					if (list.size() == 1)
						bolumId = list.get(0).getId();
					for (Tanim tanim : list) {
						if (bolumId != null && tanim.getId().equals(bolumId))
							idVar = true;
						bolumler.add(new SelectItem(tanim.getId(), tanim.getAciklama()));
					}
					if (bolumId != null)
						personelList.clear();
				}
			}
			if (!idVar)
				bolumId = null;
		} catch (Exception e) {
		}
		return "";
	}

	/**
	 * @return
	 */
	public String personelDoldur() {
		personelList.clear();
		puantajList.clear();
		StringBuffer sb = new StringBuffer();
		try {
			if (bolumId != null) {
				HashMap fields = new HashMap();
				sb.append("select DISTINCT P.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
				sb.append(" INNER  JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
				sb.append(" AND PD." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + " = 1   ");
				personelSQLBagla(sb, fields);
				if (tesisId != null) {
					sb.append(" AND P." + Personel.COLUMN_NAME_TESIS + " = " + tesisId);

				}
				sb.append(" AND P." + Personel.COLUMN_NAME_EK_SAHA3 + " = " + bolumId);
				if (sirket.getSirketGrup() == null) {
					sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + " = " + sirketId);
				} else {
					sb.append(" INNER JOIN " + Sirket.TABLE_NAME + " S ON S." + Sirket.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_SIRKET);
					sb.append(" AND S." + Sirket.COLUMN_NAME_SIRKET_GRUP + " = " + sirket.getSirketGrup().getId());
				}
				donemSQLKontrol(sb);
				sb.append(" AND ((D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+" + DenklestirmeAy.COLUMN_NAME_AY + ")<=:s");

				fields.put("s", sonDonem);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				personelList = pdksEntityController.getObjectBySQLList(sb, fields, Personel.class);
				if (!personelList.isEmpty()) {
					personelList = PdksUtil.sortObjectStringAlanList(personelList, "getAdSoyad", null);
					List<Personel> ayrilanlar = new ArrayList<Personel>();
					Date tarih = PdksUtil.convertToJavaDate(String.valueOf(bitYil * 100 + bitAy) + "01", "yyyyMMdd");
					for (Iterator iterator = personelList.iterator(); iterator.hasNext();) {
						Personel personel = (Personel) iterator.next();
						if (!personel.isCalisiyorGun(tarih)) {
							ayrilanlar.add(personel);
							iterator.remove();
						}
					}
					if (!ayrilanlar.isEmpty())
						personelList.addAll(ayrilanlar);
					ayrilanlar = null;
				}

			}
			saveLastParameter();

		} catch (Exception e) {
		}

		return "";
	}

	/**
	 * @return
	 */
	public String sirketDoldur() {
		personelList.clear();
		perDenkMap.clear();
		sirketler.clear();
		bolumler.clear();
		denklestirmeAyList.clear();
		basTarih = basAy != null ? PdksUtil.convertToJavaDate(String.valueOf(basYil * 100 + basAy) + "01", "yyyyMMdd") : null;
		bitTarih = bitAy != null ? PdksUtil.tariheGunEkleCikar(PdksUtil.tariheAyEkleCikar(PdksUtil.convertToJavaDate(String.valueOf(bitYil * 100 + bitAy) + "01", "yyyyMMdd"), 1), -1) : null;
		StringBuffer sb = new StringBuffer();
		try {
			HashMap fields = new HashMap();
			sb.append("select DISTINCT S.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
			sb.append(" INNER  JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
			sb.append("  AND PD." + PersonelDenklestirme.COLUMN_NAME_DURUM + " = 1 AND  PD." + PersonelDenklestirme.COLUMN_NAME_ONAYLANDI + " = 1 AND  PD." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + " = 1 ");
			personelSQLBagla(sb, fields);
			sb.append(" INNER  JOIN " + Sirket.TABLE_NAME + " S ON S." + Sirket.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_SIRKET);
			donemSQLKontrol(sb);

			sb.append(" AND ((D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+" + DenklestirmeAy.COLUMN_NAME_AY + ")<=:s");

			fields.put("s", sonDonem);
			sb.append(" ORDER BY S." + Sirket.COLUMN_NAME_ID);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Sirket> sirketList = pdksEntityController.getObjectBySQLList(sb, fields, Sirket.class);
			if (!sirketList.isEmpty()) {
				if (sirketList.size() == 1)
					sirketId = sirketList.get(0).getId();
				else
					sirketList = PdksUtil.sortObjectStringAlanList(sirketList, "getAd", null);
				for (Sirket sirket : sirketList) {
					if (sirket.isPdksMi())
						sirketler.add(new SelectItem(sirket.getId(), sirket.getAd()));
				}
			}
			tesisDoldur();
		} catch (Exception e) {
			logger.error(sb.toString());
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * @param yil
	 * @param donemler
	 * @param donemDoldur
	 * @return
	 */
	public String ayDoldur(int yil, List<SelectItem> donemler, boolean donemDoldur) {
		if (donemler == null)
			donemler = new ArrayList<SelectItem>();
		else
			donemler.clear();
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("select DISTINCT D.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
		sb.append(" WHERE D." + DenklestirmeAy.COLUMN_NAME_YIL + "=:y");
		sb.append(" AND ((D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+ D." + DenklestirmeAy.COLUMN_NAME_AY + ")<=:s");
		sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_AY + " > 0");
		sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_DURUM + " = 0");
		sb.append(" ORDER BY D." + DenklestirmeAy.COLUMN_NAME_AY);
		fields.put("y", yil);
		fields.put("s", sonDonem);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<DenklestirmeAy> denkList = pdksEntityController.getObjectBySQLList(sb, fields, DenklestirmeAy.class);
		for (DenklestirmeAy da : denkList)
			donemler.add(new SelectItem(da.getAy(), da.getAyAdi()));
		denkList = null;
		if (donemDoldur)
			fillDonemDoldur();
		return "";
	}

	/**
	 * @return
	 */
	public String fillDonemDoldur() {
		personelList.clear();
		perDenkMap.clear();
		denklestirmeAyList.clear();
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("select DISTINCT D.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
		sb.append(" WHERE ((D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+ D." + DenklestirmeAy.COLUMN_NAME_AY + ")>=:y1");
		sb.append(" AND ((D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+ D." + DenklestirmeAy.COLUMN_NAME_AY + ")<=:y2");
		sb.append(" AND ((D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+ D." + DenklestirmeAy.COLUMN_NAME_AY + ")<=:s");
		sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_AY + " > 0");
		sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_DURUM + " = 0");
		sb.append(" ORDER BY D." + DenklestirmeAy.COLUMN_NAME_AY);
		fields.put("y1", basYil * 100 + basAy);
		fields.put("y2", bitYil * 100 + bitAy);
		fields.put("s", sonDonem);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			sirketler.clear();
			List<DenklestirmeAy> denkList = pdksEntityController.getObjectBySQLList(sb, fields, DenklestirmeAy.class);
			List<Long> idList = new ArrayList<Long>();
			for (DenklestirmeAy denklestirmeAy : denkList) {
				idList.add(denklestirmeAy.getId());

			}
			if (!idList.isEmpty()) {
				String fieldName = "d";
				sb = new StringBuffer();
				sb.append("select DISTINCT S.* from " + PersonelDenklestirme.TABLE_NAME + " PD WITH(nolock) ");
				sb.append(" INNER  JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + " = PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
				sb.append(" INNER  JOIN " + Sirket.TABLE_NAME + " S ON S." + Sirket.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_SIRKET);
				sb.append(" WHERE PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " :" + fieldName);
				sb.append("   AND  PD." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + " = 1 ");
				sb.append(" ORDER BY S." + Sirket.COLUMN_NAME_ID);
				fields.put(fieldName, idList);
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				// List<Sirket> sirketList = pdksEntityController.getObjectBySQLList(sb, fields, Sirket.class);
				List<Sirket> sirketList = ortakIslemler.getSQLParamList(idList, sb, fieldName, fields, Sirket.class, session);
				if (!sirketList.isEmpty()) {
					sirketDoldur();
				}
			}
			denkList = null;
		} catch (Exception e) {
			logger.error(sb.toString());
			e.printStackTrace();
		}

		return "";
	}

	public String fazlaMesaiExcel() {
		try {

			String donemOrj = (seciliPersonel.getAdSoyad() + " " + seciliPersonel.getPdksSicilNo());
			String donem = basYil + " " + PdksUtil.getSelectItemLabel(bitAy, donemBas) + " - " + bitYil + " " + PdksUtil.getSelectItemLabel(bitAy, donemBit);

			ByteArrayOutputStream baosDosya = fazlaMesaiExcelDevam(donem);
			if (baosDosya != null) {
				String dosyaAdi = "PersonelCalismaDonem " + donemOrj + " _ " + donem.trim() + ".xlsx";
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
	 * @param donem
	 * @return
	 */
	private ByteArrayOutputStream fazlaMesaiExcelDevam(String donem) {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, donem + " Çalışma", Boolean.TRUE);
		CellStyle izinBaslik = ExcelUtil.getStyleHeader(wb);
		CellStyle styleTutarEven = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleTutarOdd = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);

		CellStyle styleCenterEvenDay = ExcelUtil.getStyleDayEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleCenterOddDay = ExcelUtil.getStyleDayOdd(ExcelUtil.ALIGN_CENTER, wb);

		CellStyle styleDay = null, styleGenel = null, styleTutar = null, styleStrDay = null;
		CellStyle styleCenter = ExcelUtil.getStyleData(wb);
		CellStyle styleTatil = ExcelUtil.getStyleDataCenter(wb);

		CellStyle styleIstek = ExcelUtil.getStyleDataCenter(wb);
		CellStyle styleEgitim = ExcelUtil.getStyleDataCenter(wb);
		CellStyle styleOff = ExcelUtil.getStyleDataCenter(wb);
		ExcelUtil.setFontColor(styleOff, Color.WHITE);
		ExcelUtil.setFillForegroundColor(izinBaslik, 146, 208, 80);

		CellStyle styleIzin = ExcelUtil.getStyleDataCenter(wb);
		ExcelUtil.setFillForegroundColor(styleIzin, 146, 208, 80);

		CellStyle styleCalisma = ExcelUtil.getStyleDataCenter(wb);
		int row = 0, col = 0;
		XSSFCellStyle header = (XSSFCellStyle) ExcelUtil.getStyleHeader(9, wb);

		ExcelUtil.setFillForegroundColor(styleTatil, 255, 153, 204);

		ExcelUtil.setFillForegroundColor(styleIstek, 255, 255, 0);

		ExcelUtil.setFillForegroundColor(styleCalisma, 255, 255, 255);

		ExcelUtil.setFillForegroundColor(styleEgitim, 0, 0, 255);

		ExcelUtil.setFillForegroundColor(styleOff, 13, 12, 89);
		ExcelUtil.setFontColor(styleOff, 256, 256, 256);

		col = 0;

		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Dönem");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.calismaModeliAciklama());
		if (fazlaMesaiOde)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("FM Ödeme");
		if (fazlaMesaiIzinKullan)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("FM İzin Kullansın");

		Calendar cal = Calendar.getInstance();

		CreationHelper factory = wb.getCreationHelper();
		Drawing drawing = sheet.createDrawingPatriarch();
		ClientAnchor anchor = factory.createClientAnchor();
		CellStyle headerVardiyaGun = ExcelUtil.getStyleHeader(9, wb);
		ExcelUtil.setFillForegroundColor(headerVardiyaGun, 99, 182, 153);

		CellStyle headerVardiyaTatilYarimGun = ExcelUtil.getStyleHeader(9, wb);
		ExcelUtil.setFontColor(headerVardiyaTatilYarimGun, 255, 255, 0);
		ExcelUtil.setFillForegroundColor(headerVardiyaTatilYarimGun, 144, 185, 63);

		CellStyle headerVardiyaTatilGun = ExcelUtil.getStyleHeader(9, wb);
		ExcelUtil.setFillForegroundColor(headerVardiyaTatilGun, 92, 127, 45);
		ExcelUtil.setFontColor(headerVardiyaTatilGun, 255, 255, 0);
		for (VardiyaGun vardiyaGun : gunAylikPuantaj.getVardiyalar()) {
			try {

				cal.setTime(vardiyaGun.getVardiyaDate());
				CellStyle headerVardiya = headerVardiyaGun;
				String title = null;

				Cell cell = ExcelUtil.getCell(sheet, row, col++, headerVardiya);
				AylikPuantaj.baslikCell(factory, drawing, anchor, cell, authenticatedUser.getTarihFormatla(cal.getTime(), "EEE"), title);

			} catch (Exception e) {
			}
		}

		Cell cell = ExcelUtil.getCell(sheet, row, col++, header);
		AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "TÇS", "Toplam Çalışma Saati: Çalışanın bu listedeki toplam çalışma saati");
		cell = ExcelUtil.getCell(sheet, row, col++, header);
		AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "ÇGS", "Çalışılması Gereken Saat: Çalışanın bu listede çalışması gereken saat");
		if (yasalFazlaCalismaAsanSaat) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.yasalFazlaCalismaAsanSaatKod(), "Yasal Çalışmayı Aşan Mesai : Saati aşan çalışma toplam miktarı");
		}
		cell = ExcelUtil.getCell(sheet, row, col++, header);
		AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "GM", "Gerçekleşen Mesai : Çalışanın bu listedeki eksi/fazla çalışma saati");

		cell = ExcelUtil.getCell(sheet, row, col++, header);
		AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.devredenMesaiKod(), "Devreden Mesai: Çalisanin önceki listelerden devreden eksi/fazla mesaisi");

		cell = ExcelUtil.getCell(sheet, row, col++, header);
		AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "ÜÖM", "Çalışanın bu listenin sonunda ücret olarak ödediğimiz fazla mesai saati");

		if (kismiOdemeGoster) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "KÖM", "Çalışanın bu listenin sonunda ücret olarak kısmi ödediğimiz fazla mesai saati ");
		}

		if (resmiTatilVar) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "RÖM", "Çalışanın bu listenin sonunda ücret olarak ödediğimiz resmi tatil mesai saati");
		}
		if (haftaTatilVar) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			AylikPuantaj.baslikCell(factory, drawing, anchor, cell, AylikPuantaj.MESAI_TIPI_HAFTA_TATIL, "Çalışanın bu listenin sonunda ücret olarak ödediğimiz hafta tatil mesai saati");
		}

		cell = ExcelUtil.getCell(sheet, row, col++, header);
		AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.devredenBakiyeKod(), "Bakiye: Çalışanın bu liste de dahil bugüne kadarki devreden eksi/fazla mesaisi");

		CellStyle headerIzinTipi = (CellStyle) header.clone();
		ExcelUtil.setFillForegroundColor(headerIzinTipi, 255, 153, 204);
		int ayAdet = 0;
		String pattern = PdksUtil.getDateFormat() + " EEEEE";
		for (Iterator iter = puantajList.iterator(); iter.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();
			if (ayAdet % 2 != 0) {
				styleCenter = styleOddCenter;
				styleStrDay = styleCenterOddDay;
				styleGenel = styleOdd;
				styleTutar = styleTutarOdd;
			} else {
				styleCenter = styleEvenCenter;
				styleStrDay = styleCenterEvenDay;
				styleGenel = styleEven;
				styleTutar = styleTutarEven;
			}
			ayAdet++;
			List vardiyaList = aylikPuantaj.getVardiyalar();
			row++;
			col = 0;
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue("");

			ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			if (fazlaMesaiOde)
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			if (fazlaMesaiIzinKullan)
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
				VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
				if (vardiyaGun.isAyinGunu()) {
					try {
						cal.setTime(vardiyaGun.getVardiyaDate());
						CellStyle headerVardiya = headerVardiyaGun;
						String title = PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), pattern);
						if (vardiyaGun.getTatil() != null) {
							Tatil tatil = vardiyaGun.getTatil();
							title += "\n" + tatil.getAd();
							headerVardiya = tatil.isYarimGunMu() ? headerVardiyaTatilYarimGun : headerVardiyaTatilGun;
						}
						cell = ExcelUtil.getCell(sheet, row, col++, headerVardiya);
						AylikPuantaj.baslikCell(factory, drawing, anchor, cell, String.valueOf(cal.get(Calendar.DAY_OF_MONTH)), title);

					} catch (Exception e) {
					}
				} else
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");

			}
			ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			if (yasalFazlaCalismaAsanSaat)
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			if (kismiOdemeGoster)
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			if (resmiTatilVar)
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			if (haftaTatilVar)
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");

			PersonelDenklestirme personelDenklestirme = aylikPuantaj.getPersonelDenklestirme();
			DenklestirmeAy da = aylikPuantaj.getDenklestirmeAy();

			CalismaModeli calismaModeli = aylikPuantaj.getCalismaModeli();
			PersonelDenklestirme personelDenklestirmeGecenAy = personelDenklestirme != null ? personelDenklestirme.getPersonelDenklestirmeGecenAy() : null;
			row++;
			col = 0;
			Comment commentGuncelleyen = null;
			if (personelDenklestirme.getGuncelleyenUser() != null && personelDenklestirme.getGuncellemeTarihi() != null)
				commentGuncelleyen = getCommentGuncelleyen(factory, drawing, anchor, personelDenklestirme);

			try {

				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(da.getAyAdi() + " " + da.getYil());

				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(calismaModeli != null ? calismaModeli.getAciklama() : "");
				if (fazlaMesaiOde)
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(authenticatedUser.getYesNo(personelDenklestirme.getFazlaMesaiOde()));
				if (fazlaMesaiIzinKullan)
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(authenticatedUser.getYesNo(personelDenklestirme.getFazlaMesaiIzinKullan()));

				for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
					VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
					if (vardiyaGun.isAyinGunu()) {
						String styleText = vardiyaGun.getAylikClassAdi(aylikPuantaj.getTrClass());
						styleDay = styleStrDay;
						if (styleText.equals(VardiyaGun.STYLE_CLASS_HAFTA_TATIL))
							styleDay = styleTatil;
						else if (styleText.equals(VardiyaGun.STYLE_CLASS_IZIN))
							styleDay = styleIzin;
						else if (styleText.equals(VardiyaGun.STYLE_CLASS_OZEL_ISTEK))
							styleDay = styleIstek;
						else if (styleText.equals(VardiyaGun.STYLE_CLASS_EGITIM))
							styleDay = styleEgitim;
						else if (styleText.equals(VardiyaGun.STYLE_CLASS_OFF))
							styleDay = styleOff;
						cell = ExcelUtil.getCell(sheet, row, col++, styleDay);
						String aciklama = calisan(vardiyaGun) ? vardiyaGun.getFazlaMesaiOzelAciklama(Boolean.TRUE, authenticatedUser.sayiFormatliGoster(vardiyaGun.getCalismaSuresi())) : "";
						String title = calisan(vardiyaGun) ? vardiyaGun.getTitle() : null;
						if (title != null) {
							Comment comment1 = drawing.createCellComment(anchor);
							if (vardiyaGun.getVardiya() != null && (vardiyaGun.getCalismaSuresi() > 0 || (vardiyaGun.getVardiya().isCalisma() && styleGenel == styleCalisma)))
								title = vardiyaGun.getVardiya().getKisaAdi() + " --> " + title;
							RichTextString str1 = factory.createRichTextString(title);
							comment1.setString(str1);
							cell.setCellComment(comment1);

						}
						cell.setCellValue(aciklama);
					} else
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");

				}

				setCell(sheet, row, col++, styleTutar, aylikPuantaj.getSaatToplami());
				Cell planlananCell = setCell(sheet, row, col++, styleTutar, aylikPuantaj.getPlanlananSure());
				if (aylikPuantaj.getCalismaModeliAy() != null && planlananCell != null && aylikPuantaj.getSutIzniDurum().equals(Boolean.FALSE)) {
					Comment comment1 = drawing.createCellComment(anchor);
					String title = aylikPuantaj.getCalismaModeli().getAciklama() + " : ";
					if (aylikPuantaj.getCalismaModeli().getToplamGunGuncelle().equals(Boolean.FALSE))
						title += authenticatedUser.sayiFormatliGoster(aylikPuantaj.getCalismaModeliAy().getSure());
					else
						title += authenticatedUser.sayiFormatliGoster(aylikPuantaj.getPersonelDenklestirme().getPlanlanSure());
					RichTextString str1 = factory.createRichTextString(title);
					comment1.setString(str1);
					planlananCell.setCellComment(comment1);
				}
				if (yasalFazlaCalismaAsanSaat) {
					if (aylikPuantaj.getUcretiOdenenMesaiSure() > 0)
						setCell(sheet, row, col++, styleTutar, aylikPuantaj.getUcretiOdenenMesaiSure());
					else
						ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue("");
				}
				setCell(sheet, row, col++, styleTutar, aylikPuantaj.getAylikNetFazlaMesai());

				Double gecenAyFazlaMesai = aylikPuantaj.getGecenAyFazlaMesai(authenticatedUser);
				Cell gecenAyFazlaMesaiCell = setCell(sheet, row, col++, styleTutar, gecenAyFazlaMesai);
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
					Cell fazlaMesaiSureCell = setCell(sheet, row, col++, styleTutar, aylikPuantaj.getFazlaMesaiSure());
					if (aylikPuantaj.getFazlaMesaiSure() != 0.0d && commentGuncelleyen != null) {
						fazlaMesaiSureCell.setCellComment(commentGuncelleyen);
						olustur = true;
					}
				} else
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");

				if (kismiOdemeGoster) {
					if (personelDenklestirme.getKismiOdemeSure() != null && personelDenklestirme.getKismiOdemeSure().doubleValue() > 0.0d)
						setCell(sheet, row, col++, styleTutar, personelDenklestirme.getKismiOdemeSure());
					else
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}
				if (resmiTatilVar)
					setCell(sheet, row, col++, styleTutar, aylikPuantaj.getResmiTatilToplami());
				if (haftaTatilVar)
					setCell(sheet, row, col++, styleTutar, aylikPuantaj.getHaftaCalismaSuresi());

				if (aylikPuantaj.isFazlaMesaiHesapla()) {
					Cell devredenSureCell = setCell(sheet, row, col++, styleTutar, aylikPuantaj.getDevredenSure());
					if (aylikPuantaj.getDevredenSure() != null && aylikPuantaj.getDevredenSure().doubleValue() != 0.0d && commentGuncelleyen != null) {
						if (olustur)
							commentGuncelleyen = getCommentGuncelleyen(factory, drawing, anchor, personelDenklestirme);
						devredenSureCell.setCellComment(commentGuncelleyen);
					}
				} else
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");

				styleGenel = null;
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				logger.error(row);

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

	/**
	 * @return
	 */
	public String fillBilgileriDoldur(Personel personel) {
		gunAylikPuantaj = null;
		puantajList.clear();
		seciliPersonel = personel;
		perDenkMap.clear();
		fazlaMesaiOde = false;
		fazlaMesaiIzinKullan = false;
		resmiTatilVar = false;
		haftaTatilVar = false;
		kismiOdemeGoster = false;
		bordroPuantajEkranindaGoster = ortakIslemler.getParameterKey("bordroPuantajEkranindaGoster").equals("1");
		yasalFazlaCalismaAsanSaat = false;
		StringBuffer sb = new StringBuffer();
		HashMap fields = new HashMap();
		sb.append("select PD.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
		sb.append(" INNER  JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
		sb.append(" AND   PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + "= " + personel.getId());
		personelSQLBagla(sb, fields);

		donemSQLKontrol(sb);
		sb.append(" ORDER BY D." + DenklestirmeAy.COLUMN_NAME_YIL + " , D." + DenklestirmeAy.COLUMN_NAME_AY);

		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PersonelDenklestirme> list = pdksEntityController.getObjectBySQLList(sb, fields, PersonelDenklestirme.class);

		if (!list.isEmpty()) {
			fields.clear();
			sb = new StringBuffer();
			sb.append("select  SUM(COALESCE(PD." + PersonelDenklestirme.COLUMN_NAME_RESMI_TATIL_SURE + ",0)) " + PersonelDenklestirme.COLUMN_NAME_RESMI_TATIL_SURE + ", ");
			sb.append("  SUM(COALESCE(PD." + PersonelDenklestirme.COLUMN_NAME_HAFTA_TATIL_SURE + ",0)) " + PersonelDenklestirme.COLUMN_NAME_HAFTA_TATIL_SURE + ", ");
			sb.append("  SUM(PD." + PersonelDenklestirme.COLUMN_NAME_FAZLA_MESAI_IZIN_KULLAN + ") " + PersonelDenklestirme.COLUMN_NAME_FAZLA_MESAI_IZIN_KULLAN + ", ");
			sb.append("  SUM(PD." + PersonelDenklestirme.COLUMN_NAME_FAZLA_MESAI_ODE + ") " + PersonelDenklestirme.COLUMN_NAME_FAZLA_MESAI_ODE + ", ");
			sb.append("  SUM(COALESCE(PD." + PersonelDenklestirme.COLUMN_NAME_KISMI_ODEME_SAAT + ",0)) " + PersonelDenklestirme.COLUMN_NAME_KISMI_ODEME_SAAT + " from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
			sb.append(" INNER  JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
			sb.append(" AND   PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + "= " + personel.getId());
			personelSQLBagla(sb, fields);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Object[]> list2 = pdksEntityController.getObjectBySQLList(sb, fields, null);
			if (!list2.isEmpty()) {
				Object[] data = list2.get(0);
				resmiTatilVar = ((Double) data[0]).doubleValue() > 0d;
				haftaTatilVar = ((Double) data[1]).doubleValue() > 0d;
				fazlaMesaiIzinKullan = ((Integer) data[2]).intValue() > 0;
				fazlaMesaiOde = ((Integer) data[3]).intValue() > 0;
				kismiOdemeGoster = ((BigDecimal) data[4]).doubleValue() > 0d;
			}
			List<AylikPuantaj> dataList = new ArrayList<AylikPuantaj>();
			Calendar cal = Calendar.getInstance();
			Date b1 = personel.getIseBaslamaTarihi().after(basTarih) ? personel.getIseBaslamaTarihi() : basTarih;
			Date b2 = personel.getSskCikisTarihi().after(bitTarih) ? bitTarih : personel.getSskCikisTarihi();
			List<Personel> perList = new ArrayList<Personel>();
			perList.add(seciliPersonel);
			TreeMap<String, VardiyaGun> vardiyaMap = null;
			try {
				vardiyaMap = ortakIslemler.getVardiyalar(perList, ortakIslemler.tariheGunEkleCikar(cal, b1, -6), ortakIslemler.tariheGunEkleCikar(cal, b2, 6), null, Boolean.FALSE, session, Boolean.FALSE);

			} catch (Exception e) {
				vardiyaMap = new TreeMap<String, VardiyaGun>();
			}
			TreeMap<String, Tatil> tatilMap = ortakIslemler.getTatilGunleri(perList, b1, b2, session);
			int sonGun = 0;
			Date bugun = new Date();
			boolean renk = Boolean.TRUE;
			fazlaMesaiVar = false;
			saatlikMesaiVar = false;
			aylikMesaiVar = false;
			for (PersonelDenklestirme pd : list) {
				double puantajHaftaTatil = 0.0d;
				DenklestirmeAy da = pd.getDenklestirmeAy();
				int yil = da.getYil();
				int ay = da.getAy();
				b1 = PdksUtil.convertToJavaDate(String.valueOf(yil * 100 + ay) + "01", "yyyyMMdd");
				b2 = PdksUtil.tariheGunEkleCikar(PdksUtil.tariheAyEkleCikar(b1, 1), -1);
				cal.setTime(b1);
				int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
				if (dayOfWeek != Calendar.MONDAY) {
					if (dayOfWeek != Calendar.SUNDAY)
						cal.add(Calendar.DATE, 2 - dayOfWeek);
					else
						cal.add(Calendar.DATE, -6);
					b1 = cal.getTime();
				}
				cal.setTime(b2);
				dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
				if (dayOfWeek != Calendar.SUNDAY) {
					cal.add(Calendar.DATE, 8 - dayOfWeek);
					b2 = cal.getTime();
				}
				cal.setTime(b1);
				Date tarih = cal.getTime();
				String donem = String.valueOf(yil * 100 + da.getAy());
				TreeMap<String, VardiyaGun> vgPerMap = new TreeMap<String, VardiyaGun>();
				boolean ayBasladi = false;
				double ucretiOdenenMesaiSure = 0.0d, fazlaMesaiMaxSure = da.getFazlaMesaiMaxSure();
				boolean fazlaMesaiOdenir = pd.getCalismaModeliAy() != null && pd.getCalismaModeliAy().isGunMaxCalismaOdenir();
				while (tarih.getTime() <= b2.getTime()) {
					VardiyaGun vg = new VardiyaGun(seciliPersonel, null, tarih);
					String key = vg.getVardiyaDateStr();
					if (tatilMap.containsKey(key))
						vg.setTatil(tatilMap.get(key));
					String perKey = vg.getVardiyaKeyStr();
					if (vg.getPdksPersonel() != null) {
						if (vardiyaMap.containsKey(perKey)) {
							vg = (VardiyaGun) vardiyaMap.get(perKey).clone();
							vardiyaGun = vg;
							Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
							if (vardiyaGun.getPersonel().isCalisiyorGun(vardiyaGun.getVardiyaDate())) {
								try {
									boolean zamanGelmedi = !bugun.after(islemVardiya.getVardiyaTelorans2BitZaman());
									if (!zamanGelmedi)
										zamanGelmedi = islemVardiya.isCalisma() == false || vardiyaGun.isIzinli();
									else if (islemVardiya.isCalisma())
										vardiyaGun.setCalismaSuresi(islemVardiya.getNetCalismaSuresi());

									vardiyaGun.setZamanGelmedi(zamanGelmedi);
								} catch (Exception e) {
								}
							}
							if (vardiyaGun.getVardiyaSaatDB() != null) {
								VardiyaSaat vardiyaSaatDB = vardiyaGun.getVardiyaSaatDB();
								if (fazlaMesaiOdenir) {
									if (vardiyaGun.getCalismaSuresi() > fazlaMesaiMaxSure)
										ucretiOdenenMesaiSure += vardiyaGun.getCalismaSuresi() - fazlaMesaiMaxSure;

								}
								if (vardiyaSaatDB.getResmiTatilSure() > 0.0d)
									vardiyaGun.setResmiTatilSure(vardiyaSaatDB.getResmiTatilSure());
								else if (pd.getHaftaCalismaSuresi() != null && vardiyaGun.getVardiya().isHaftaTatil() && pd.getHaftaCalismaSuresi() > 0.0d) {
									puantajHaftaTatil += vardiyaSaatDB.getCalismaSuresi();
									vardiyaGun.setHaftaCalismaSuresi(vardiyaSaatDB.getCalismaSuresi());
								}

								vardiyaGun.setCalismaSuresi(vardiyaSaatDB.getCalismaSuresi());

							}
						}

					}

					vg.setAyinGunu(key.startsWith(donem));
					if (vg.isAyinGunu() == false) {
						if (ayBasladi) {
							break;
						}
					} else
						ayBasladi = true;
					vgPerMap.put(key, vg);
					cal.add(Calendar.DATE, 1);
					tarih = cal.getTime();

				}
				List<VardiyaGun> gunList = new ArrayList<VardiyaGun>(vgPerMap.values());
				AylikPuantaj aylikPuantaj = new AylikPuantaj();
				aylikPuantaj.setPersonelDenklestirmeData(pd);
				CalismaModeli cm = aylikPuantaj.getCalismaModeli();
				if (!fazlaMesaiVar)
					fazlaMesaiVar = cm.isFazlaMesaiVarMi();
				if (bordroPuantajEkranindaGoster) {
					if (!saatlikMesaiVar)
						saatlikMesaiVar = cm.isSaatlikOdeme();
					if (!aylikMesaiVar)
						aylikMesaiVar = cm.isAylikOdeme();
				}
				aylikPuantaj.setTrClass(renk ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
				renk = !renk;
				aylikPuantaj.setHaftaCalismaSuresi(puantajHaftaTatil);
				aylikPuantaj.setVardiyalar(gunList);
				aylikPuantaj.setVgMap(vgPerMap);
				aylikPuantaj.setUcretiOdenenMesaiSure(ucretiOdenenMesaiSure);
				dataList.add(aylikPuantaj);
				if (gunList.size() > sonGun)
					sonGun = gunList.size();

			}
			if (!dataList.isEmpty()) {
				if (bordroPuantajEkranindaGoster)
					fazlaMesaiOrtakIslemler.setAylikPuantajBordroVeri(dataList, session);
				for (AylikPuantaj dap : dataList) {
					PersonelDenklestirme personelDenklestirme = dap.getPersonelDenklestirme();
					dap.setPlanlananSure(personelDenklestirme.getPlanlanSure());
					List<VardiyaGun> gunList = dap.getVardiyalar();
					int fark = sonGun - gunList.size();
					if (fark > 0) {
						Date tarih = gunList.get(gunList.size() - 1).getVardiyaDate();
						cal.setTime(tarih);
						for (int i = 0; i < fark; i++) {
							cal.add(Calendar.DATE, 1);
							tarih = cal.getTime();
							VardiyaGun vg = new VardiyaGun(seciliPersonel.isCalisiyorGun(tarih) ? seciliPersonel : null, null, tarih);
							if (vg.getPdksPersonel() != null && vardiyaMap.containsKey(vg.getVardiyaKeyStr()))
								vg = vardiyaMap.get(vg.getVardiyaKeyStr());
							vg.setAyinGunu(Boolean.FALSE);
							gunList.add(vg);
						}
					}
					gunAylikPuantaj = dap;

				}
			}
			setPuantajList(dataList);
		}

		return "";
	}

	/**
	 * @param sb
	 */
	private void donemSQLKontrol(StringBuffer sb) {
		if (basYil == bitYil) {
			sb.append(" WHERE D." + DenklestirmeAy.COLUMN_NAME_YIL + " = " + basYil);
			if (basAy != null) {
				sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_AY + " >= " + basAy);
			}
			if (bitAy != null) {
				sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_AY + " <= " + bitAy);
			}
		} else {
			int donem1 = basYil * 100 + (basAy != null ? basAy : 1);
			int donem2 = bitYil * 100 + (bitAy != null ? bitAy : 12);
			sb.append(" WHERE  (100 * D." + DenklestirmeAy.COLUMN_NAME_YIL + " + D." + DenklestirmeAy.COLUMN_NAME_AY + " ) > = " + donem1);
			sb.append(" AND  (100 * D." + DenklestirmeAy.COLUMN_NAME_YIL + " + D." + DenklestirmeAy.COLUMN_NAME_AY + " ) < = " + donem2);
		}

		sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_DURUM + " = 0");
	}

	/**
	 * @param vardiyaGun
	 * @return
	 */
	private boolean calisan(VardiyaGun vardiyaGun) {
		boolean calisan = vardiyaGun != null;
		if (calisan) {
			if (vardiyaGun.getVardiya() != null) {

				calisan = vardiyaGun.isKullaniciYetkili() || (vardiyaGun.getIzin() != null && !helpPersonel(vardiyaGun.getPersonel()));
			}
		}
		return calisan;
	}

	/**
	 * @param personel
	 * @return
	 */
	private boolean helpPersonel(Personel personel) {
		return false;

	}

	/**
	 * @param sb
	 * @param fields
	 */
	private void personelSQLBagla(StringBuffer sb, HashMap fields) {
		sb.append(" INNER  JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + " = PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
		if (basTarih != null && bitTarih != null) {
			sb.append(" AND P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " <= :b2  AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= :b1 ");
			fields.put("b1", basTarih);
			fields.put("b2", bitTarih);
		}
		if (authenticatedUser.getYetkiliTesisler() != null && !authenticatedUser.getYetkiliTesisler().isEmpty()) {
			List<Long> idList = new ArrayList<Long>();
			for (Tanim tanim : authenticatedUser.getYetkiliTesisler()) {
				if (tanim.getId() != null)
					idList.add(tanim.getId());
			}
			if (!idList.isEmpty()) {
				if (idList.size() > 1) {
					sb.append(" AND P." + Personel.COLUMN_NAME_TESIS + " :te ");
					fields.put("te", idList);

				} else
					sb.append(" AND P." + Personel.COLUMN_NAME_TESIS + " = " + idList.get(0));
			}
		}
	}

	public Integer getBasAy() {
		return basAy;
	}

	public void setBasAy(Integer basAy) {
		this.basAy = basAy;
	}

	public Integer getBitAy() {
		return bitAy;
	}

	public void setBitAy(Integer bitAy) {
		this.bitAy = bitAy;
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

	public Integer getMaxYil() {
		return maxYil;
	}

	public void setMaxYil(Integer maxYil) {
		this.maxYil = maxYil;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public List<PersonelDenklestirme> getPerDenkList() {
		return perDenkList;
	}

	public void setPerDenkList(List<PersonelDenklestirme> perDenkList) {
		this.perDenkList = perDenkList;
	}

	public List<Personel> getPersonelList() {
		return personelList;
	}

	public void setPersonelList(List<Personel> personelList) {
		this.personelList = personelList;
	}

	public HashMap<String, PersonelDenklestirme> getPerDenkMap() {
		return perDenkMap;
	}

	public void setPerDenkMap(HashMap<String, PersonelDenklestirme> perDenkMap) {
		this.perDenkMap = perDenkMap;
	}

	public PersonelDenklestirme getDenklestirme() {
		return denklestirme;
	}

	public void setDenklestirme(PersonelDenklestirme denklestirme) {
		this.denklestirme = denklestirme;
	}

	public List<DenklestirmeAy> getDenklestirmeAyList() {
		return denklestirmeAyList;
	}

	public void setDenklestirmeAyList(List<DenklestirmeAy> denklestirmeAyList) {
		this.denklestirmeAyList = denklestirmeAyList;
	}

	public Long getTesisId() {
		return tesisId;
	}

	public void setTesisId(Long tesisId) {
		this.tesisId = tesisId;
	}

	public List<SelectItem> getTesisler() {
		return tesisler;
	}

	public void setTesisler(List<SelectItem> tesisler) {
		this.tesisler = tesisler;
	}

	public boolean isTesisVar() {
		return tesisVar;
	}

	public void setTesisVar(boolean tesisVar) {
		this.tesisVar = tesisVar;
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

	public List<SelectItem> getBolumler() {
		return bolumler;
	}

	public void setBolumler(List<SelectItem> bolumler) {
		this.bolumler = bolumler;
	}

	public Long getBolumId() {
		return bolumId;
	}

	public void setBolumId(Long bolumId) {
		this.bolumId = bolumId;
	}

	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket sirket) {
		this.sirket = sirket;
	}

	public List<AylikPuantaj> getPuantajList() {
		return puantajList;
	}

	public void setPuantajList(List<AylikPuantaj> puantajList) {
		this.puantajList = puantajList;
	}

	public Personel getSeciliPersonel() {
		return seciliPersonel;
	}

	public void setSeciliPersonel(Personel seciliPersonel) {
		this.seciliPersonel = seciliPersonel;
	}

	public VardiyaGun getVardiyaGun() {
		return vardiyaGun;
	}

	public void setVardiyaGun(VardiyaGun vardiyaGun) {
		this.vardiyaGun = vardiyaGun;
	}

	public AylikPuantaj getGunAylikPuantaj() {
		return gunAylikPuantaj;
	}

	public void setGunAylikPuantaj(AylikPuantaj gunAylikPuantaj) {
		this.gunAylikPuantaj = gunAylikPuantaj;
	}

	public boolean isFazlaMesaiOde() {
		return fazlaMesaiOde;
	}

	public void setFazlaMesaiOde(boolean fazlaMesaiOde) {
		this.fazlaMesaiOde = fazlaMesaiOde;
	}

	public boolean isFazlaMesaiIzinKullan() {
		return fazlaMesaiIzinKullan;
	}

	public void setFazlaMesaiIzinKullan(boolean fazlaMesaiIzinKullan) {
		this.fazlaMesaiIzinKullan = fazlaMesaiIzinKullan;
	}

	public boolean isResmiTatilVar() {
		return resmiTatilVar;
	}

	public void setResmiTatilVar(boolean resmiTatilVar) {
		this.resmiTatilVar = resmiTatilVar;
	}

	public boolean isHaftaTatilVar() {
		return haftaTatilVar;
	}

	public void setHaftaTatilVar(boolean haftaTatilVar) {
		this.haftaTatilVar = haftaTatilVar;
	}

	public Integer getBasYil() {
		return basYil;
	}

	public void setBasYil(Integer basYil) {
		this.basYil = basYil;
	}

	public Integer getBitYil() {
		return bitYil;
	}

	public void setBitYil(Integer bitYil) {
		this.bitYil = bitYil;
	}

	public List<SelectItem> getDonemBas() {
		return donemBas;
	}

	public void setDonemBas(List<SelectItem> donemBas) {
		this.donemBas = donemBas;
	}

	public List<SelectItem> getDonemBit() {
		return donemBit;
	}

	public void setDonemBit(List<SelectItem> donemBit) {
		this.donemBit = donemBit;
	}

	public boolean isKismiOdemeGoster() {
		return kismiOdemeGoster;
	}

	public void setKismiOdemeGoster(boolean kismiOdemeGoster) {
		this.kismiOdemeGoster = kismiOdemeGoster;
	}

	public boolean isYasalFazlaCalismaAsanSaat() {
		return yasalFazlaCalismaAsanSaat;
	}

	public void setYasalFazlaCalismaAsanSaat(boolean yasalFazlaCalismaAsanSaat) {
		this.yasalFazlaCalismaAsanSaat = yasalFazlaCalismaAsanSaat;
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

	public boolean isBordroPuantajEkranindaGoster() {
		return bordroPuantajEkranindaGoster;
	}

	public void setBordroPuantajEkranindaGoster(boolean bordroPuantajEkranindaGoster) {
		this.bordroPuantajEkranindaGoster = bordroPuantajEkranindaGoster;
	}

}
