package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

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
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.KapiView;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelHareketIslem;
import org.pdks.entity.PersonelView;
import org.pdks.security.entity.User;

@Name("P10P20Home")
public class P10P20Home extends EntityHome<HareketKGS> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7496147532860243635L;
	static Logger logger = Logger.getLogger(P10P20Home.class);

	@RequestParameter
	Long kgsHareketId;

	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false)
	User authenticatedUser;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(value = "#{facesContext.externalContext}")
	ExternalContext extCtx;
	@In(value = "#{facesContext}")
	FacesContext facesContext;

	List<HareketKGS> hareketList = new ArrayList<HareketKGS>();

	private Date basTarih, bitTarih;

	private AramaSecenekleri aramaSecenekleri = null;

	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	@In(required = false)
	FacesMessages facesMessages;

	@Override
	public Object getId() {
		if (kgsHareketId == null) {
			return super.getId();
		} else {
			return kgsHareketId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		aramaSecenekleri = new AramaSecenekleri(authenticatedUser);
		setHareketList(new ArrayList<HareketKGS>());
		HareketKGS hareket = new HareketKGS();
		hareket.setPersonel(new PersonelView());
		hareket.setKapiView(new KapiView());
		hareket.setIslem(new PersonelHareketIslem());
		setInstance(hareket);
		setBasTarih(new Date());
		setBitTarih(new Date());
		ortakIslemler.fillEkSahaTanimAramaSecenekAta(session, Boolean.FALSE, Boolean.TRUE, aramaSecenekleri);

	}

	public String fillHareketList() {
		try {
			if (hareketList != null)
				hareketList.clear();
			else
				hareketList = new ArrayList<HareketKGS>();
			fillHareketListOlustur();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return "";
	}

	private void fillHareketListOlustur() {

		List<HareketKGS> hareket1List = new ArrayList<HareketKGS>();
		List<HareketKGS> kgsList = new ArrayList<HareketKGS>();
		List<Long> kapiIdler = ortakIslemler.getPdksDonemselKapiIdler(basTarih, bitTarih, session);
		try {
			Calendar cal = Calendar.getInstance();
			if (kapiIdler != null && !kapiIdler.isEmpty())
				kgsList = ortakIslemler.getPdksHareketBilgileri(Boolean.TRUE, kapiIdler, (List<Personel>) authenticatedUser.getTumPersoneller().clone(), basTarih, ortakIslemler.tariheGunEkleCikar(cal, bitTarih, 1), HareketKGS.class, session);
			else
				kgsList = new ArrayList<HareketKGS>();
		} catch (Exception e) {
			kgsList = new ArrayList<HareketKGS>();
		}

		if (!kgsList.isEmpty())
			kgsList = PdksUtil.sortListByAlanAdi(kgsList, "zaman", Boolean.FALSE);

		try {
			Long tesisId = aramaSecenekleri.getTesisId();
			for (Iterator iterator = kgsList.iterator(); iterator.hasNext();) {
				HareketKGS kgsHareket = (HareketKGS) iterator.next();
				if (tesisId != null) {
					if (kgsHareket.getPersonel().getPdksPersonel() == null || kgsHareket.getPersonel().getPdksPersonel().getTesis() == null || !tesisId.equals(kgsHareket.getPersonel().getPdksPersonel().getTesis().getId()))
						continue;
				}

				if (kgsHareket.getKapiView().getKapi().isGirisKapi() || kgsHareket.getKapiView().getKapi().isCikisKapi())

					hareket1List.add(kgsHareket);
			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		if (!hareket1List.isEmpty())
			hareket1List = PdksUtil.sortListByAlanAdi(hareket1List, "zaman", Boolean.FALSE);
		setHareketList(hareket1List);

	}

	/**
	 * @param date
	 * @return
	 */
	private HashMap<Integer, Integer> tarihiSaatiniGetir(Date date) {
		HashMap<Integer, Integer> map = null;
		if (date != null) {
			map = new HashMap<Integer, Integer>();
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			map.put(Calendar.MINUTE, cal.get(Calendar.MINUTE));
			map.put(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
			map.put(Calendar.SECOND, cal.get(Calendar.SECOND));
		}
		return map;

	}

	public String saatOlustur(Date date) {
		return PdksUtil.convertToDateString(date, "HH");
	}

	public String dakikaOlustur(Date date) {
		return PdksUtil.convertToDateString(date, "mm");
	}

	public String saniyeOlustur(Date date) {
		return PdksUtil.convertToDateString(date, "ss");
	}

	/**
	 * @param date
	 * @return
	 */
	public String tarihOlustur2(Date date) {

		return PdksUtil.convertToDateString(date, "yyyyMMddHHmmss");

	}

	public String excelAktar() {
		ByteArrayOutputStream baosDosya = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, "Izin Karti", Boolean.TRUE);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddDateTime = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATETIME, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenDateTime = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATETIME, wb);

		int row = 0, col = 0;
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		boolean tesisDurum = ortakIslemler.getListTesisDurum(hareketList);
		if (tesisDurum)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Personel");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Kapı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Hareket Tipi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Tarih");
		boolean renk = true;
		for (Iterator iterator = hareketList.iterator(); iterator.hasNext();) {
			HareketKGS hareket = (HareketKGS) iterator.next();
			KapiView kapiView = hareket.getKapiView();
			PersonelView personelView = hareket.getPersonel();
			Personel personel = personelView.getPdksPersonel();
			CellStyle style = null, styleCenter = null, styleDateTime = null;
			if (renk) {
				styleDateTime = styleOddDateTime;
				style = styleOdd;
				styleCenter = styleOddCenter;
			} else {
				styleDateTime = styleEvenDateTime;
				style = styleEven;
				styleCenter = styleEvenCenter;
			}
			renk = !renk;
			++row;
			col = 0;
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel != null ? personel.getSirket().getAd() : "");
			if (tesisDurum)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel != null && personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personelView.getAdSoyad());
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personelView.getSicilNo() != null ? personelView.getSicilNo() : "");
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(kapiView.getAciklama());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(kapiView.getKapi() != null ? kapiView.getKapi().getTipi().getAciklama() : "");
			ExcelUtil.getCell(sheet, row, col++, styleDateTime).setCellValue(hareket.getZaman());

		}
		for (int i = 0; i < col; i++)
			sheet.autoSizeColumn(i);
		try {
			baosDosya = new ByteArrayOutputStream();
			wb.write(baosDosya);
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			baosDosya = null;
		}
		if (baosDosya != null)
			PdksUtil.setExcelHttpServletResponse(baosDosya, "p10p20.xlsx");

		return "";
	}

	public String hareketYaz() {

		HashMap<Integer, Integer> saat = tarihiSaatiniGetir(new Date());
		Calendar cal = new GregorianCalendar();

		// Get the components of the date
		int year = cal.get(Calendar.YEAR); // 2002
		int month = cal.get(Calendar.MONTH); // 0=Jan, 1=Feb, ...
		int day = cal.get(Calendar.DAY_OF_MONTH); // 1...

		String currentSaat = "" + saat.get(Calendar.HOUR_OF_DAY) + ":" + saat.get(Calendar.MINUTE);
		String curentDate = "" + day + "." + month + "." + year;
		String upString1 = "------  ------  ------------00\n";
		String upString2 = currentSaat + " - " + currentSaat + " - " + curentDate + " 000\n";
		String pCondition = "";
		String strFile = curentDate + "." + saat.get(Calendar.HOUR_OF_DAY) + "." + saat.get(Calendar.MINUTE) + "." + saat.get(Calendar.SECOND) + ".txt";

		HttpServletResponse response = (HttpServletResponse) extCtx.getResponse();
		response.setContentType("text/plain");

		response.addHeader("Content-disposition", "attachment; filename=\"" + strFile + "\"");

		try {
			ServletOutputStream os = response.getOutputStream();

			os.write(upString1.getBytes());
			os.write(upString2.getBytes());
			os.write(upString1.getBytes());

			for (Iterator iterator = hareketList.iterator(); iterator.hasNext();) {

				HareketKGS personelLogu = (HareketKGS) iterator.next();

				if (personelLogu.getKapiView().getKapi().isGirisKapi())
					pCondition = "P20";
				else if (personelLogu.getKapiView().getKapi().isCikisKapi())
					pCondition = "P10";
				else
					continue;

				String line = personelLogu.getPersonel().getPdksPersonel().getSirket().getErpKodu() + ";" + (personelLogu.getPersonel().getPdksPersonel().getTesis() != null ? personelLogu.getPersonel().getPdksPersonel().getTesis().getErpKodu() : "00") + ";" + personelLogu.getPersonel().getSicilNo()
						+ ";" + tarihOlustur2(personelLogu.getZaman()) + ";" + pCondition + "\n";

				os.write(line.getBytes());
			}

			os.flush();
			os.close();
			facesContext.responseComplete();
		}

		catch (IOException e) {

		}

		return null;

	}

	public List<HareketKGS> getHareketList() {
		return hareketList;
	}

	public void setHareketList(List<HareketKGS> hareketList) {
		this.hareketList = hareketList;
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

	public AramaSecenekleri getAramaSecenekleri() {
		return aramaSecenekleri;
	}

	public void setAramaSecenekleri(AramaSecenekleri aramaSecenekleri) {
		this.aramaSecenekleri = aramaSecenekleri;
	}
}
