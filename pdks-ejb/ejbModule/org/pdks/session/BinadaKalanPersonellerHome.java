package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.EntityManager;

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
import org.pdks.entity.HareketKGS;
import org.pdks.entity.KapiView;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelHareketIslem;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.security.entity.User;

@Name("binadaKalanPersonellerHome")
public class BinadaKalanPersonellerHome extends EntityHome<VardiyaGun> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6765737790006932581L;
	static Logger logger = Logger.getLogger(BinadaKalanPersonellerHome.class);

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
	@In(required = false, create = true)
	List<User> userList;
	@In(required = false)
	FacesMessages facesMessages;
	List<HareketKGS> hareketList = new ArrayList<HareketKGS>();
	List<VardiyaGun> vardiyaGunList = new ArrayList<VardiyaGun>();

	private String islemTipi, bolumAciklama;
	private Date date;

	private Session session;

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
		setHareketList(new ArrayList<HareketKGS>());
		setVardiyaGunList(new ArrayList<VardiyaGun>());
		HareketKGS hareket = new HareketKGS();
		hareket.setPersonel(new PersonelView());
		hareket.setKapiView(new KapiView());
		hareket.setIslem(new PersonelHareketIslem());
		setDate(new Date());
		fillEkSahaTanim();
	}

	public void hareketGoster(VardiyaGun pdksVardiyaGun) {
		setInstance(pdksVardiyaGun);
		List<HareketKGS> kgsList = pdksVardiyaGun.getHareketler();
		setHareketList(kgsList);

	}

	private void fillEkSahaTanim() {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
	}

	public String fillHareketList() {

		try {
			if (vardiyaGunList != null)
				vardiyaGunList.clear();
			else
				vardiyaGunList = new ArrayList<VardiyaGun>();
			if (ortakIslemler.ileriTarihSeciliDegil(date))
				fillHareketListOlustur();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return "";

	}

	public String excelListe() {
		try {

			ByteArrayOutputStream baosDosya = excelDevam();
			if (baosDosya != null) {
				String dosyaAdi = "BinadaKalanPersonel  _" + PdksUtil.convertToDateString(date, "yyyyMMdd") + ".xlsx";
				PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi);
			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return "";
	}

	private ByteArrayOutputStream excelDevam() {
		boolean tesisDurum = false;
		Tanim bolum = null;
		for (VardiyaGun vg : vardiyaGunList) {
			Personel personel = vg.getPdksPersonel();
			if (!tesisDurum)
				tesisDurum = personel.getSirket().getTesisDurum();
			if (bolum == null && personel.getEkSaha3() != null)
				bolum = personel.getEkSaha3().getParentTanim();

		}
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, "Personel Listesi", false);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddDatetime = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATETIME, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenDatetime = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATETIME, wb);
		int row = 0;
		int col = 0;
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.yoneticiAciklama());
		if (tesisDurum)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolum != null ? bolum.getAciklama() : "Bölüm");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Vardiya");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İlk Giriş");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Son Çıkış");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Son Giriş");
		boolean renk = true;
		for (VardiyaGun vg : vardiyaGunList) {
			++row;
			col = 0;
			Personel personel = vg.getPdksPersonel();
			CellStyle style = null, styleCenter = null, cellStyleDatetime = null;
			Sirket sirket = personel.getSirket();
			if (renk) {
				cellStyleDatetime = styleOddDatetime;
				style = styleOdd;
				styleCenter = styleOddCenter;
			} else {
				cellStyleDatetime = styleEvenDatetime;
				style = styleEven;
				styleCenter = styleEvenCenter;
			}
			renk = !renk;
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getSicilNo());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(sirket != null ? sirket.getAd() : "");
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getYoneticisi() != null ? personel.getYoneticisi().getAdSoyad() : "");
			if (tesisDurum) {
				if (personel.getTesis() != null)
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(sirket != null && sirket.getTesisDurum() ? personel.getTesis().getAciklama() : "");
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
			}
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(vg.getVardiya() != null ? vg.getVardiya().getAciklama() : "");
			if (vg.getGirisHareket() != null)
				ExcelUtil.getCell(sheet, row, col++, cellStyleDatetime).setCellValue(vg.getGirisHareket().getOrjinalZaman());
			else
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
			if (vg.getCikisHareket() != null)
				ExcelUtil.getCell(sheet, row, col++, cellStyleDatetime).setCellValue(vg.getCikisHareket().getOrjinalZaman());
			else
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
			if (vg.getSonGirisHareket() != null)
				ExcelUtil.getCell(sheet, row, col++, cellStyleDatetime).setCellValue(vg.getSonGirisHareket().getOrjinalZaman());
			else
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
		}
		try {

			for (int i = 0; i < col; i++)
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

	private void fillHareketListOlustur() {
		List<VardiyaGun> vardiyaList = new ArrayList<VardiyaGun>();
		List<HareketKGS> kgsList = new ArrayList<HareketKGS>();
		TreeMap<String, VardiyaGun> vardiyaMap = null;
		try {
			Calendar cal = Calendar.getInstance();
			vardiyaMap = ortakIslemler.getIslemVardiyalar((List<Personel>) authenticatedUser.getTumPersoneller().clone(), date, ortakIslemler.tariheGunEkleCikar(cal, date, 1), Boolean.FALSE, session, Boolean.TRUE);

		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		if (vardiyaMap == null)
			vardiyaMap = new TreeMap<String, VardiyaGun>();
		vardiyaList = new ArrayList<VardiyaGun>(vardiyaMap.values());
		Date tarih1 = null;
		Date tarih2 = null;
		Date tarih3 = null;
		Date tarih4 = null;
		for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
			VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator.next();
			if (PdksUtil.tarihKarsilastirNumeric(pdksVardiyaGun.getVardiyaDate(), date) != 0) {
				iterator.remove();
				continue;

			}
			if (!pdksVardiyaGun.getVardiya().isCalisma())
				continue;
			if ((tarih1 == null && tarih3 == null) || pdksVardiyaGun.getIslemVardiya().getVardiyaBasZaman().getTime() < tarih3.getTime()) {
				tarih3 = pdksVardiyaGun.getIslemVardiya().getVardiyaBasZaman();
				tarih1 = pdksVardiyaGun.getIslemVardiya().getVardiyaFazlaMesaiBasZaman();

			}

			if (tarih2 == null || pdksVardiyaGun.getIslemVardiya().getVardiyaBitZaman().getTime() > tarih4.getTime()) {
				tarih4 = pdksVardiyaGun.getIslemVardiya().getVardiyaBitZaman();
				tarih2 = pdksVardiyaGun.getIslemVardiya().getVardiyaFazlaMesaiBitZaman();

			}

		}
		List<Long> kapiIdler = ortakIslemler.getPdksDonemselKapiIdler(date, date, session);

		try {
			if (kapiIdler != null && !kapiIdler.isEmpty())
				kgsList = ortakIslemler.getPdksHareketBilgileri(Boolean.TRUE, kapiIdler, (List<Personel>) authenticatedUser.getTumPersoneller().clone(), tarih1, tarih2, HareketKGS.class, session);
			else
				kgsList = new ArrayList<HareketKGS>();
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

			kgsList = new ArrayList<HareketKGS>();
		}
		if (!kgsList.isEmpty())
			kgsList = PdksUtil.sortListByAlanAdi(kgsList, "zaman", Boolean.FALSE);

		List<VardiyaGun> vardiyaList1 = new ArrayList<VardiyaGun>();
		try {

			for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
				VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
				if (vardiyaGun.getPersonel().getSicilNo().equals("90007749"))
					logger.error(vardiyaGun.getPersonel().getSicilNo() + " " + vardiyaGun.getPersonel().getAdSoyad());
				vardiyaGun.setHareketler(null);
				vardiyaGun.setGirisHareketleri(null);
				vardiyaGun.setCikisHareketleri(null);
				Vardiya islemVardiya = vardiyaGun.getIslemVardiya();

				for (Iterator iterator1 = kgsList.iterator(); iterator1.hasNext();) {

					HareketKGS kgsHareket = (HareketKGS) iterator1.next();
					if (vardiyaGun.getPersonel().getId().equals(kgsHareket.getPersonel().getPdksPersonel().getId())) {
						if (kgsHareket.getZaman().getTime() >= islemVardiya.getVardiyaFazlaMesaiBasZaman().getTime() && kgsHareket.getZaman().getTime() <= islemVardiya.getVardiyaFazlaMesaiBitZaman().getTime())
							vardiyaGun.addPersonelHareket(kgsHareket);
						iterator1.remove();

					}

				}

				if (!vardiyaGun.isHareketHatali() && vardiyaGun.getHareketler() != null) {
					int i = (vardiyaGun.getHareketler()).size() - 1;
					if (vardiyaGun.getHareketler().get(i).getKapiView().getKapi().isGirisKapi()) {
						vardiyaList1.add(vardiyaGun);
					}

				}

			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		setVardiyaGunList(vardiyaList1);

	}

	public List<HareketKGS> getHareketList() {
		return hareketList;
	}

	public void setHareketList(List<HareketKGS> hareketList) {
		this.hareketList = hareketList;
	}

	public String getIslemTipi() {
		return islemTipi;
	}

	public void setIslemTipi(String islemTipi) {
		this.islemTipi = islemTipi;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<VardiyaGun> getVardiyaGunList() {
		return vardiyaGunList;
	}

	public void setVardiyaGunList(List<VardiyaGun> vardiyaGunList) {
		this.vardiyaGunList = vardiyaGunList;
	}

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

}
