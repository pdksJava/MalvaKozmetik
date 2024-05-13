package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.KapiView;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Tanim;
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.YemekOgun;
import org.pdks.security.entity.User;

@Name("yemekYiyenlerHome")
public class YemekYiyenlerHome extends EntityHome<VardiyaGun> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6639034253796710686L;
	static Logger logger = Logger.getLogger(YemekYiyenlerHome.class);

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
	@In(required = false, create = true)
	HashMap<String, String> parameterMap;
	@In(required = false)
	FacesMessages facesMessages;

	private List<HareketKGS> hareketList = new ArrayList<HareketKGS>();
	private List<YemekOgun> yemekList;
	private List<Long> yemekKapiList;
	private boolean ogunVar = false, masrafYeriVar = false;

	private Date basTarih;
	private Date bitTarih;
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

	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.clear();
		ogunVar = false;
		masrafYeriVar = false;
		setHareketList(new ArrayList<HareketKGS>());
		HareketKGS hareket = new HareketKGS();
		hareket.setPersonel(new PersonelView());
		hareket.setKapiView(new KapiView());
		// setBasTarih(new Date());
		// setBitTarih(new Date());
		fillYemekKapiList();

	}

	private void fillYemekKapiList() {
		setYemekKapiList(ortakIslemler.getYemekKapiIdList(session));
	}

	public String fillHareketList(boolean durum) {
		ogunVar = false;
		masrafYeriVar = false;
		List<HareketKGS> kgsList = ortakIslemler.getYemekHareketleri(session, basTarih, bitTarih, durum);
		if (durum && kgsList.size() > 1) {
			TreeMap<String, List<HareketKGS>> ogunMap = new TreeMap<String, List<HareketKGS>>();
			for (HareketKGS kgsHareket : kgsList) {
				kgsHareket.setCokluOgun(false);
				if (kgsHareket.getYemekOgun() == null || kgsHareket.isCheckBoxDurum())
					continue;
				if (!ogunVar)
					ogunVar = kgsHareket.getYemekOgun() != null && kgsHareket.getYemekOgun().getId() != null && kgsHareket.getYemekOgun().getId().longValue() > 0;
				if (!masrafYeriVar)
					masrafYeriVar = kgsHareket.getPersonel() != null && kgsHareket.getPersonel().getPdksPersonel() != null && kgsHareket.getPersonel().getPdksPersonel().getMasrafYeri() != null;
				String key = PdksUtil.convertToDateString(kgsHareket.getZaman(), "yyyyMMdd") + "_" + kgsHareket.getPersonelId() + "_" + kgsHareket.getYemekOgun().getId();
				List<HareketKGS> list = ogunMap.containsKey(key) ? ogunMap.get(key) : new ArrayList<HareketKGS>();
				if (list.isEmpty())
					ogunMap.put(key, list);
				list.add(kgsHareket);
			}
			List<Long> kapiList = new ArrayList<Long>();
			for (String key : ogunMap.keySet()) {
				List<HareketKGS> list = ogunMap.get(key);
				if (list.size() > 1) {
					for (HareketKGS kgsHareket : list) {
						if (!kapiList.contains(kgsHareket.getKapiId()))
							kapiList.add(kgsHareket.getKapiId());
					}
					boolean cokluOgun = kapiList.size() > 1;
					if (cokluOgun)
						for (HareketKGS kgsHareket : list)
							kgsHareket.setCokluOgun(cokluOgun);

				}

				kapiList.clear();
			}
			kapiList = null;
			ogunMap = null;
		}
		Collections.reverse(kgsList);
		setHareketList(kgsList);
		return "";
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

	public List<YemekOgun> getYemekList() {
		return yemekList;
	}

	public void setYemekList(List<YemekOgun> yemekList) {
		this.yemekList = yemekList;
	}

	public List<Long> getYemekKapiList() {
		return yemekKapiList;
	}

	public void setYemekKapiList(List<Long> yemekKapiList) {
		this.yemekKapiList = yemekKapiList;
	}

	public String excelAktar() {
		try {
			ByteArrayOutputStream baosDosya = excelDevam();
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, "yemekYiyenler.xlsx");

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
		}

		return "";
	}

	private ByteArrayOutputStream excelDevam() throws Exception {
		ByteArrayOutputStream baos = null;

		Workbook wb = new XSSFWorkbook();

		Sheet sheet = ExcelUtil.createSheet(wb, "Yemek yiyenler", Boolean.TRUE);

		CellStyle header = ExcelUtil.getStyleHeader(wb);

		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddDateTime = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATETIME, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenDateTime = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATETIME, wb);

		int row = 0;
		ExcelUtil.getCell(sheet, row, 0, header).setCellValue("Yemek Zamanı");
		ExcelUtil.getCell(sheet, row, 1, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, 2, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, 3, header).setCellValue(ortakIslemler.sirketAciklama());
		int sayac = 4;
		if (masrafYeriVar) {
			ExcelUtil.getCell(sheet, row, sayac++, header).setCellValue("Masraf Yeri Kodu");
			ExcelUtil.getCell(sheet, row, sayac++, header).setCellValue("Masraf Yeri Açıklama");
		}

		if (ogunVar)
			ExcelUtil.getCell(sheet, row, sayac++, header).setCellValue("Öğün Tipi");
		ExcelUtil.getCell(sheet, row, sayac++, header).setCellValue("Yemek Yeri");
		ExcelUtil.getCell(sheet, row, sayac++, header).setCellValue("Yemek Durum");
		ExcelUtil.getCell(sheet, row, sayac++, header).setCellValue("Mükerrer Geçerli");
		ExcelUtil.getCell(sheet, row, sayac++, header).setCellValue("Öncek Giriş Zamanı");
		boolean renk = true;
		for (Iterator iter = hareketList.iterator(); iter.hasNext();) {
			HareketKGS yemek = (HareketKGS) iter.next();
			row++;
			String adSoyad = yemek.getAdSoyad();
			CellStyle style = null, styleCenter = null, stylDateTime = null;
			if (renk) {
				stylDateTime = styleOddDateTime;
				style = styleOdd;
				styleCenter = styleOddCenter;
			} else {
				stylDateTime = styleEvenDateTime;
				style = styleEven;
				styleCenter = styleEvenCenter;
			}
			renk = !renk;
			try {
				ExcelUtil.getCell(sheet, row, 0, stylDateTime).setCellValue(yemek.getZaman());
				ExcelUtil.getCell(sheet, row, 1, style).setCellValue(adSoyad);
				ExcelUtil.getCell(sheet, row, 2, styleCenter).setCellValue(yemek.getSicilNo());
				String sirket = "", ogun = "", durum = "";
				try {
					sirket = yemek.getPersonel().getPdksPersonel().getSirket().getAd();
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					sirket = "" + ortakIslemler.sirketAciklama() + " tanımsız";
				}
				ExcelUtil.getCell(sheet, row, 3, style).setCellValue(sirket);
				sayac = 4;
				if (masrafYeriVar) {
					String masrafYeriKodu = "", masrafYeriAciklama = "";
					try {
						if (yemek.getPersonel().getPdksPersonel().getMasrafYeri() != null) {
							Tanim masrafYeri = yemek.getPersonel().getPdksPersonel().getMasrafYeri();
							if (PdksUtil.hasStringValue(masrafYeri.getKodu())) {
								masrafYeriKodu = masrafYeri.getKodu();
								masrafYeriAciklama = masrafYeri.getAciklama();

							}
						}

					} catch (Exception e) {
						logger.error("PDKS hata in : \n");
						e.printStackTrace();
						logger.error("PDKS hata out : " + e.getMessage());
						masrafYeriAciklama = "";
					}

					ExcelUtil.getCell(sheet, row, sayac++, style).setCellValue(masrafYeriKodu);
					ExcelUtil.getCell(sheet, row, sayac++, style).setCellValue(masrafYeriAciklama);
				}

				if (ogunVar) {

					try {
						ogun = yemek.getYemekOgun().getYemekAciklama();
					} catch (Exception e) {
						logger.error("PDKS hata in : \n");
						e.printStackTrace();
						logger.error("PDKS hata out : " + e.getMessage());
						sirket = "Öğün tanımsız";
					}

					ExcelUtil.getCell(sheet, row, sayac++, style).setCellValue(ogun);
				}
				ExcelUtil.getCell(sheet, row, sayac++, style).setCellValue(yemek.getKapiView().getAciklama());
				try {
					durum = yemek.getDurumu();
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					durum = "";
				}
				ExcelUtil.getCell(sheet, row, sayac++, style).setCellValue(durum);
				ExcelUtil.getCell(sheet, row, sayac++, style).setCellValue(yemek.getGecerliYemek() != null && yemek.getGecerliYemek() ? 1 : (yemek.isCokluOgun() ? 2 : 0));
				if (yemek.getOncekiYemekZamani() != null)
					ExcelUtil.getCell(sheet, row, sayac++, stylDateTime).setCellValue(yemek.getOncekiYemekZamani());
				else
					ExcelUtil.getCell(sheet, row, sayac++, style).setCellValue("");
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				logger.debug(row);

			}
		}
		double katsayi = 3.43;
		int[] dizi = new int[] { 1575, 1056, 2011, 2056, 2011, 2056, 3722, 2078, 2600, 2000, 2000 };
		for (int i = 0; i < dizi.length; i++)
			sheet.setColumnWidth(i, (short) (dizi[i] * katsayi));

		try {
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

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public boolean isOgunVar() {
		return ogunVar;
	}

	public void setOgunVar(boolean ogunVar) {
		this.ogunVar = ogunVar;
	}

	public boolean isMasrafYeriVar() {
		return masrafYeriVar;
	}

	public void setMasrafYeriVar(boolean masrafYeriVar) {
		this.masrafYeriVar = masrafYeriVar;
	}
}
