package org.pdks.session;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
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
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.AylikPuantaj;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.DepartmanDenklestirmeDonemi;
import org.pdks.entity.Liste;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.security.action.UserHome;
import org.pdks.security.entity.User;

@Name("fazlaMesaiDonemselRaporHome")
public class FazlaMesaiDonemselRaporHome extends EntityHome<DepartmanDenklestirmeDonemi> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7516859224980927543L;

	static Logger logger = Logger.getLogger(FazlaMesaiDonemselRaporHome.class);

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

	private Integer yil, sonDonem, basAy, bitAy, maxYil;
	private Long sirketId, tesisId;
	private List<SelectItem> donemler, sirketler, tesisler;
	private List<PersonelDenklestirme> perDenkList;
	private List<Personel> personelList;
	private List<DenklestirmeAy> denklestirmeAyList;
	private HashMap<String, PersonelDenklestirme> perDenkMap;
	private PersonelDenklestirme denklestirme;
	private boolean tesisVar = false;
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private String bolumAciklama;
	private Date basTarih, bitTarih;
	private Session session;

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		if (donemler == null)
			donemler = new ArrayList<SelectItem>();
		if (sirketler == null)
			sirketler = new ArrayList<SelectItem>();

		if (tesisler == null)
			tesisler = new ArrayList<SelectItem>();
		tesisler.clear();
		if (personelList == null)
			personelList = new ArrayList<Personel>();
		if (denklestirmeAyList == null)
			denklestirmeAyList = new ArrayList<DenklestirmeAy>();
		if (perDenkMap == null)
			perDenkMap = new HashMap<String, PersonelDenklestirme>();

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		yil = calendar.get(Calendar.YEAR);
		maxYil = yil;
		basAy = calendar.get(Calendar.MONTH) + 1;
		bitAy = calendar.get(Calendar.MONTH) + 1;
		sonDonem = (yil * 100) + bitAy;

		fillDonemDoldur();
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

		try {
			boolean idVar = false;
			if (sirketId != null) {
				HashMap fields = new HashMap();

				sb.append("select DISTINCT TE.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
				sb.append(" INNER  JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=D." + DenklestirmeAy.COLUMN_NAME_ID);
				sb.append(" AND PD." + PersonelDenklestirme.COLUMN_NAME_DURUM + " =1 AND  PD." + PersonelDenklestirme.COLUMN_NAME_ONAYLANDI + "=1 AND  PD." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + "=1");
				sb.append(" AND ( PD." + PersonelDenklestirme.COLUMN_NAME_ODENEN_SURE + "<>0 OR PD." + PersonelDenklestirme.COLUMN_NAME_DEVREDEN_SURE + "<>0 ) ");

				sb.append(" INNER  JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + "=PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
				sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + "=" + sirketId);
				sb.append(" INNER  JOIN " + Tanim.TABLE_NAME + " TE ON TE." + Tanim.COLUMN_NAME_ID + "=P." + Personel.COLUMN_NAME_TESIS);

				sb.append(" WHERE D." + DenklestirmeAy.COLUMN_NAME_YIL + "=" + yil);
				if (basAy != null)
					sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_AY + ">=" + basAy);

				if (bitAy != null)
					sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_AY + "<=" + bitAy);

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
			}
			if (!idVar)
				tesisId = null;
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
		denklestirmeAyList.clear();
		StringBuffer sb = new StringBuffer();
		try {
			HashMap fields = new HashMap();
			sb.append("select DISTINCT S.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
			sb.append(" INNER  JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=D." + DenklestirmeAy.COLUMN_NAME_ID);
			sb.append("  AND PD." + PersonelDenklestirme.COLUMN_NAME_DURUM + " =1 AND  PD." + PersonelDenklestirme.COLUMN_NAME_ONAYLANDI + "=1 AND  PD." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + "=1");
			sb.append(" INNER  JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + "=PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
			sb.append(" INNER  JOIN " + Sirket.TABLE_NAME + " S ON S." + Sirket.COLUMN_NAME_ID + "=P." + Personel.COLUMN_NAME_SIRKET);
			sb.append(" WHERE D." + DenklestirmeAy.COLUMN_NAME_YIL + "=:y  AND D." + DenklestirmeAy.COLUMN_NAME_AY + ">0 ");
			sb.append(" AND ((D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+" + DenklestirmeAy.COLUMN_NAME_AY + ")<=:s");
			if (basAy != null) {
				sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_AY + ">=:d1");
				fields.put("d1", basAy);
			}
			if (bitAy != null) {
				sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_AY + "<=:d2");
				fields.put("d2", bitAy);
			}
			fields.put("y", yil);
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
	 * @return
	 */
	public String fillDonemDoldur() {
		personelList.clear();
		perDenkMap.clear();
		denklestirmeAyList.clear();
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("select DISTINCT PD.* from " + DenklestirmeAy.TABLE_NAME + " PD WITH(nolock) ");
		sb.append(" WHERE PD." + DenklestirmeAy.COLUMN_NAME_YIL + "=:y");
		sb.append(" AND ((PD." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+ PD." + DenklestirmeAy.COLUMN_NAME_AY + ")<=:s");
		sb.append(" ORDER BY PD." + DenklestirmeAy.COLUMN_NAME_AY);
		fields.put("y", yil);
		fields.put("s", sonDonem);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			donemler.clear();
			sirketler.clear();
			List<DenklestirmeAy> denkList = pdksEntityController.getObjectBySQLList(sb, fields, DenklestirmeAy.class);
			List<Long> idList = new ArrayList<Long>();
			for (DenklestirmeAy denklestirmeAy : denkList) {
				idList.add(denklestirmeAy.getId());
				donemler.add(new SelectItem(denklestirmeAy.getAy(), denklestirmeAy.getAyAdi()));
			}
			if (!idList.isEmpty()) {
				String fieldName = "d";
				sb = new StringBuffer();
				sb.append("select DISTINCT S.* from " + PersonelDenklestirme.TABLE_NAME + " PD WITH(nolock) ");
				sb.append(" INNER  JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + "=PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
				sb.append(" INNER  JOIN " + Sirket.TABLE_NAME + " S ON S." + Sirket.COLUMN_NAME_ID + "=P." + Personel.COLUMN_NAME_SIRKET);
				sb.append(" WHERE PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " :" + fieldName);
				sb.append("  AND PD." + PersonelDenklestirme.COLUMN_NAME_DURUM + " =1 AND  PD." + PersonelDenklestirme.COLUMN_NAME_ONAYLANDI + "=1 AND  PD." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + "=1");
				sb.append(" ORDER BY S." + Sirket.COLUMN_NAME_ID);
				fields.put(fieldName, idList);
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				// List<Sirket> sirketList = pdksEntityController.getObjectBySQLList(sb, fields, Sirket.class);
				List<Sirket> sirketList = ortakIslemler.getSQLParamList(idList, sb, fieldName, fields, Sirket.class, session);
				if (!sirketList.isEmpty()) {
					if (sirketList.size() == 1) {
						sirketId = sirketList.get(0).getId();
						tesisDoldur();
					} else
						sirketList = PdksUtil.sortObjectStringAlanList(sirketList, "getAd", null);
					for (Sirket sirket : sirketList) {
						if (sirket.isPdksMi())
							sirketler.add(new SelectItem(sirket.getId(), sirket.getAd()));
					}
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

			Sirket sirket = null;
			Tanim tesis = null;
			if (sirketId != null) {
				HashMap fields = new HashMap();
				fields.put("id", sirketId);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);
			}
			if (tesisId != null) {
				HashMap fields = new HashMap();
				fields.put("id", tesisId);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				tesis = (Tanim) pdksEntityController.getObjectByInnerObject(fields, Tanim.class);
			}
			String donemOrj = (sirket != null ? sirket.getAd() + " " : "") + (tesis != null ? " - " + tesis.getAciklama() : "") + yil + " ";
			String donem = yil + " ";
			for (DenklestirmeAy den : denklestirmeAyList) {
				if (basAy != null && den.getAy() == basAy) {
					donem += den.getAyAdi();
					if (bitAy != null && basAy == bitAy)
						break;
				} else if (bitAy != null && den.getAy() == bitAy) {
					donem += "-" + den.getAyAdi();
					break;
				}

			}
			ByteArrayOutputStream baosDosya = fazlaMesaiExcelDevam(donem);
			if (baosDosya != null) {
				String dosyaAdi = "FazlaMesaiDonem " + donemOrj + " " + donem.trim() + ".xlsx";
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
		Sheet sheet = ExcelUtil.createSheet(wb, ("Fazla Mesai " + donem).trim(), Boolean.TRUE);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleTutarEven = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleTutarOdd = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TUTAR, wb);

		CellStyle styleDateEven = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATE, wb);
		CellStyle styleDateOdd = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATE, wb);

		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleTatil = ExcelUtil.getStyleDataCenter(wb);
		CellStyle styleIstek = ExcelUtil.getStyleDataCenter(wb);
		CellStyle styleEgitim = ExcelUtil.getStyleDataCenter(wb);
		CellStyle styleOff = ExcelUtil.getStyleDataCenter(wb);
		ExcelUtil.setFontColor(styleOff, Color.WHITE);
		CellStyle styleIzin = ExcelUtil.getStyleDataCenter(wb);
		CellStyle styleCalisma = ExcelUtil.getStyleDataCenter(wb);
		int row = 0, col = 0;

		ExcelUtil.setFillForegroundColor(styleTatil, 255, 153, 204);
		ExcelUtil.setFillForegroundColor(styleIstek, 255, 255, 0);
		ExcelUtil.setFillForegroundColor(styleIzin, 146, 208, 80);

		ExcelUtil.setFillForegroundColor(styleCalisma, 255, 255, 255);

		ExcelUtil.setFillForegroundColor(styleEgitim, 0, 0, 255);

		ExcelUtil.setFillForegroundColor(styleOff, 13, 12, 89);
		ExcelUtil.setFontColor(styleOff, 256, 256, 256);
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		boolean tesisDurum = ortakIslemler.getListTesisDurum(personelList);
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		if (tesisDurum && tesisVar)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.yoneticiAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İşe Giriş Tarihi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İşten Ayrılma Tarihi");

		CreationHelper factory = wb.getCreationHelper();
		Drawing drawing = sheet.createDrawingPatriarch();
		ClientAnchor anchor = factory.createClientAnchor();
		for (Iterator iterator = denklestirmeAyList.iterator(); iterator.hasNext();) {
			DenklestirmeAy dm = (DenklestirmeAy) iterator.next();

			Cell cell = ExcelUtil.getCell(sheet, row, col++, header);
			AylikPuantaj.baslikCell(factory, drawing, anchor, cell, dm.getAyAdi() + "\nÜÖM", "Çalışanın bu listenin sonunda ücret olarak ödediğimiz fazla mesai saati");

			cell = ExcelUtil.getCell(sheet, row, col++, header);
			AylikPuantaj.baslikCell(factory, drawing, anchor, cell, dm.getAyAdi() + "\nB", "Bakiye: Çalışanın bu liste de dahil bugüne kadarki devreden eksi/fazla mesaisi");
		}

		for (Iterator iter = personelList.iterator(); iter.hasNext();) {
			Personel personel = (Personel) iter.next();
			row++;
			col = 0;
			CellStyle styleGenel = null, styleCenter = null, styleDate = null;
			if (row % 2 != 0) {
				styleGenel = styleOdd;
				styleCenter = styleOddCenter;
				styleDate = styleDateOdd;
			} else {
				styleGenel = styleEven;
				styleCenter = styleEvenCenter;
				styleDate = styleDateEven;
			}
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getSicilNo());
			ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getAdSoyad());
			ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getSirket() != null ? personel.getSirket().getAd() : "");
			if (tesisDurum && tesisVar)
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
			ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
			ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getYoneticisi() != null ? personel.getYoneticisi().getAdSoyad() : "");
			if (personel.getIseBaslamaTarihi() != null && personel.getIseBaslamaTarihi().before(basTarih) == false)
				ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(personel.getIseBaslamaTarihi());
			else
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			if (personel.getSskCikisTarihi() != null && personel.getSskCikisTarihi().after(bitTarih) == false)
				ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(personel.getSskCikisTarihi());
			else
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			for (Iterator iterator = denklestirmeAyList.iterator(); iterator.hasNext();) {
				DenklestirmeAy dm = (DenklestirmeAy) iterator.next();

				PersonelDenklestirme pd = getPersonelDenklestirme(dm, personel);
				if (pd == null)
					pd = new PersonelDenklestirme();

				if (row % 2 != 0)
					styleGenel = styleTutarOdd;
				else
					styleGenel = styleTutarEven;
				setCell(sheet, row, col++, styleGenel, pd.getOdenenSure() != null ? pd.getOdenenSure().doubleValue() : 0.0d);
				setCell(sheet, row, col++, styleGenel, pd.getDevredenSure() != null ? pd.getDevredenSure().doubleValue() : 0.0d);

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

	/**
	 * @return
	 */
	public String fillBilgileriDoldur() {
		tesisVar = false;
		personelList.clear();
		perDenkMap.clear();
		denklestirmeAyList.clear();
		StringBuffer sb = new StringBuffer();
		HashMap fields = new HashMap();
		sb.append("select PD.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
		sb.append(" INNER  JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=D." + DenklestirmeAy.COLUMN_NAME_ID);
		sb.append(" AND PD." + PersonelDenklestirme.COLUMN_NAME_DURUM + " =1 AND  PD." + PersonelDenklestirme.COLUMN_NAME_ONAYLANDI + "=1 AND  PD." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + "=1");
		sb.append(" AND ( PD." + PersonelDenklestirme.COLUMN_NAME_ODENEN_SURE + "<>0 OR PD." + PersonelDenklestirme.COLUMN_NAME_DEVREDEN_SURE + "<>0 ) ");
		if (sirketId != null) {
			sb.append(" INNER  JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + "=PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
			sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + "=" + sirketId);
			if (tesisId != null)
				sb.append(" AND P." + Personel.COLUMN_NAME_TESIS + "=" + tesisId);
		}
		sb.append(" WHERE D." + DenklestirmeAy.COLUMN_NAME_YIL + "=" + yil);
		if (basAy != null) {
			sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_AY + ">=" + basAy);
		}
		if (bitAy != null) {
			sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_AY + "<=" + bitAy);
		}

		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PersonelDenklestirme> list = pdksEntityController.getObjectBySQLList(sb, fields, PersonelDenklestirme.class);

		if (!list.isEmpty()) {
			basTarih = PdksUtil.convertToJavaDate(String.valueOf(yil * 100 + basAy) + "01", "yyyyMMdd");
			bitTarih = PdksUtil.tariheGunEkleCikar(PdksUtil.convertToJavaDate(String.valueOf(yil * 100 + bitAy) + "01", "yyyyMMdd"), 1);

			fillEkSahaTanim();
			TreeMap<Long, DenklestirmeAy> denkMap = new TreeMap<Long, DenklestirmeAy>();
			TreeMap<Long, Personel> perMap = new TreeMap<Long, Personel>();
			List<Liste> listeler = new ArrayList<Liste>();
			for (PersonelDenklestirme personelDenklestirme : list) {
				DenklestirmeAy denklestirmeAy = personelDenklestirme.getDenklestirmeAy();
				Personel personel = personelDenklestirme.getPersonel();
				if (!tesisVar)
					tesisVar = personel.getTesis() != null;
				if (!perMap.containsKey(personel.getId())) {
					perMap.put(personel.getId(), personel);
					String key = (sirketId == null ? personel.getSirket().getAd() + "_" : "") + (tesisId == null && personel.getTesis() != null ? personel.getTesis().getAciklama() : "") + "_" + (personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "") + "_"
							+ (personel.getYoneticisi() != null ? personel.getYoneticisi().getAdSoyad() : "") + "_" + personel.getAdSoyad() + "_" + personel.getPdksSicilNo();
					listeler.add(new Liste(key, personel));
				}
				Long denkKey = (100L * denklestirmeAy.getYil()) + denklestirmeAy.getAy();
				if (!denkMap.containsKey(denkKey))
					denkMap.put(denkKey, denklestirmeAy);
				perDenkMap.put(denklestirmeAy.getId() + "_" + personel.getId(), personelDenklestirme);
			}
			listeler = PdksUtil.sortObjectStringAlanList(listeler, "getId", null);
			for (Liste liste : listeler)
				personelList.add((Personel) liste.getValue());
			denklestirmeAyList.addAll(new ArrayList<DenklestirmeAy>(denkMap.values()));
			denkMap = null;
			listeler = null;
		}

		return "";
	}

	public Integer getYil() {
		return yil;
	}

	public void setYil(Integer yil) {
		this.yil = yil;
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

	public List<SelectItem> getDonemler() {
		return donemler;
	}

	public void setDonemler(List<SelectItem> donemler) {
		this.donemler = donemler;
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

}
