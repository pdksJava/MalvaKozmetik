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
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.KapiView;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Sirket;
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.YemekOgun;
import org.pdks.security.entity.User;

@Name("yemekCiftBasanRaporHome")
public class YemekCiftBasanRaporHome extends EntityHome<VardiyaGun> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6264536069872443179L;

	static Logger logger = Logger.getLogger(YemekCiftBasanRaporHome.class);

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

	private List<HareketKGS> hareketList = new ArrayList<HareketKGS>();
	private List<HareketKGS> toplamYemekList = new ArrayList<HareketKGS>();
	private List<HareketKGS> ciftYemekList = new ArrayList<HareketKGS>();
	private List<YemekOgun> yemekList;
	private List<Long> yemekKapiList;

	private Date basTarih;
	private Date bitTarih;
	private HareketKGS kgs;
	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

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
		setHareketList(new ArrayList<HareketKGS>());
		setCiftYemekList(new ArrayList<HareketKGS>());
		setToplamYemekList(new ArrayList<HareketKGS>());
		HareketKGS hareket = new HareketKGS();
		hareket.setPersonel(new PersonelView());
		hareket.setKapiView(new KapiView());
		// setBasTarih(new Date());
		// setBitTarih(new Date());
		fillYemekKapiList();

	}

	public void fillYemekList() {
		HashMap parametreMapYemek = new HashMap();
		parametreMapYemek.put("bitTarih>=", basTarih);
		parametreMapYemek.put("basTarih<=", bitTarih);
		parametreMapYemek.put("durum=", Boolean.TRUE);
		if (session != null)
			parametreMapYemek.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<YemekOgun> list = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMapYemek, YemekOgun.class);
		if (list.size() > 1)
			list = PdksUtil.sortListByAlanAdi(list, "baslangicSaat", Boolean.FALSE);
		setYemekList(list);

	}

	public void fillYemekKapiList() {
		setYemekKapiList(ortakIslemler.getYemekKapiIdList(session));
	}

	public ByteArrayOutputStream excelDevam(List<HareketKGS> hareketler, boolean toplam) throws Exception {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, "Yemek Rapor", Boolean.TRUE);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddDate = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATE, wb);
		CellStyle styleOddTime = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TIME, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenDate = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATE, wb);
		CellStyle styleEvenTime = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TIME, wb);
		
		
//		CellStyle style = ExcelUtil.getStyleData(wb);
//		CellStyle date = ExcelUtil.getCellStyleDate(wb);
//		DataFormat df = wb.createDataFormat();
//		CellStyle timeStamp = ExcelUtil.getCellStyleDate(wb);
//		timeStamp.setDataFormat(df.getFormat("hh:mm"));
		int row = 0, col = 0;

		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Yemek Tarih");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Yemek Zamanı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Öğün Tipi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Yemek Yeri");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Durum");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Id");
		boolean renk = true;
		for (Iterator iter = hareketler.iterator(); iter.hasNext();) {
			HareketKGS yemek = (HareketKGS) iter.next();
			row++;
			col = 0;
			CellStyle style = null, styleCenter = null, styleDate = null, styleTime = null;
			if (renk) {
				styleDate = styleOddDate;
				styleTime = styleOddTime;
				style = styleOdd;
				styleCenter = styleOddCenter;
			} else {
				styleDate = styleEvenDate;
				styleTime = styleEvenTime;
				style = styleEven;
				styleCenter = styleEvenCenter;
			}
			renk = !renk;
			ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(yemek.getZaman());
			ExcelUtil.getCell(sheet, row, col++, styleTime).setCellValue(yemek.getZaman());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(yemek.getPersonel().getPdksPersonel().getAdSoyad());
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(yemek.getPersonel().getPdksPersonel().getPdksSicilNo());
			try {

				String sirket = "", ogun = "";
				try {
					sirket = yemek.getPersonel().getPdksPersonel().getSirket().getAd();
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					sirket = "" + ortakIslemler.sirketAciklama() + " tanımsız";
				}
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(sirket);
				try {
					ogun = yemek.getYemekOgun().getYemekAciklama();
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					sirket = "Öğün tanımsız";
				}
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(ogun);
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(yemek.getKapiView().getAciklama());
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(yemek.getToplam() == 1 ? "Mükerrer Öğün" : "Mükerrer Çoklu Öğün");
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(yemek.getId());
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				logger.debug(row);

			}
			double katsayi = 3.43;
			int[] dizi = new int[] { 1500, 1500, 2000, 1000, 3000, 1500, 3000, 1500, 1000 };
			for (int i = 0; i < dizi.length; i++)
				sheet.setColumnWidth(i, (short) (dizi[i] * katsayi));
		}
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

	public String excelGunlukAktar() {
		try {
			ByteArrayOutputStream baosDosya = excelDevam(hareketList, Boolean.FALSE);
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, "gunlukYemekYiyenler.xlsx");

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
		}

		return "";
	}

	public void fillHareketList() {
		fillYemekList();
		List<HareketKGS> kgsList = new ArrayList<HareketKGS>();
		HashMap parametreMap = new HashMap();
		ortakIslemler.showSQLQuery(parametreMap);

		parametreMap.put("basTarih", PdksUtil.getDate(basTarih));
		parametreMap.put("bitTarih", PdksUtil.getDate(PdksUtil.tariheGunEkleCikar(bitTarih, 1)));

		StringBuffer qsb = new StringBuffer();
		qsb.append("SELECT S." + HareketKGS.COLUMN_NAME_ID + " FROM " + HareketKGS.TABLE_NAME + " S  WITH(nolock) ");
		qsb.append(" where  S." + HareketKGS.COLUMN_NAME_ZAMAN + " >=:basTarih AND S." + HareketKGS.COLUMN_NAME_ZAMAN + " <:bitTarih ");
		if (!yemekKapiList.isEmpty()) {
			qsb.append(" AND S." + HareketKGS.COLUMN_NAME_KAPI + " :kapiId");
			parametreMap.put("kapiId", yemekKapiList);
		}
		qsb.append(" ORDER BY  S." + HareketKGS.COLUMN_NAME_ZAMAN + ",S." + HareketKGS.COLUMN_NAME_ID);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			List list = pdksEntityController.getObjectBySQLList(qsb, parametreMap, null);
			kgsList = ortakIslemler.getHareketIdBilgileri(list, null, basTarih, bitTarih, session);
			list = null;
		} catch (Exception e) {
			kgsList = new ArrayList<HareketKGS>();
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
		}

		int yemekMukerrerAraligi = ortakIslemler.getYemekMukerrerAraligi();
		// if (!kgsList.isEmpty())
		// kgsList = pdksUtil.sortListByAlanAdi(kgsList, "zaman", Boolean.FALSE);
		List<HareketKGS> kgsSonucList = new ArrayList<HareketKGS>();

		try {

			Calendar calendar = Calendar.getInstance();
			Personel tanimsizSirketPersonel = new Personel();
			Sirket tanimsizSirket = new Sirket();
			tanimsizSirket.setAd("Sirket Tanimsiz");
			tanimsizSirket.setId(-1L);
			tanimsizSirketPersonel.setSirket(tanimsizSirket);
			tanimsizSirketPersonel.setId(-1L);
			YemekOgun tanimYemek = new YemekOgun();
			tanimYemek.setId(0L);
			tanimYemek.setYemekAciklama("Tanimsiz");
			TreeMap<String, List<HareketKGS>> veriMap = new TreeMap<String, List<HareketKGS>>();
			// kgsList.clear();
			kgsList = PdksUtil.sortListByAlanAdi(kgsList, "zaman", Boolean.TRUE);
			for (Iterator iterator = kgsList.iterator(); iterator.hasNext();) {
				HareketKGS kgsHareket = (HareketKGS) iterator.next();
				PersonelView personelView = kgsHareket.getPersonel();
				if (personelView.getPdksPersonel() == null || personelView.getPdksPersonel().getId() == null) {
					Personel pdksPersonel = new Personel();
					pdksPersonel.setAd(kgsHareket.getAdSoyad());
					pdksPersonel.setSoyad("");
					pdksPersonel.setPdksSicilNo(kgsHareket.getSicilNo());
					pdksPersonel.setSirket(tanimsizSirket);
					personelView.setPdksPersonel(pdksPersonel);
				}
				kgsHareket.setGecerliYemek(null);
				kgsHareket.setGecerliYemekAdet(0);
				kgsHareket.addYemekTeloreansZamani(yemekMukerrerAraligi);

				long yemekZamaniKgs = Long.parseLong((PdksUtil.convertToDateString(kgsHareket.getZaman(), "yyyyMMddHHmm")));
				YemekOgun pdksYemekOgun = kgsHareket.getYemekOgun();
				Date yemekGun = (Date) kgsHareket.getZaman().clone();
				if (pdksYemekOgun == null) {
					for (YemekOgun pdksYemekOgunOrj : yemekList) {
						if (PdksUtil.tarihKarsilastirNumeric(pdksYemekOgunOrj.getBasTarih(), kgsHareket.getZaman()) == 1 || PdksUtil.tarihKarsilastirNumeric(kgsHareket.getZaman(), pdksYemekOgunOrj.getBitTarih()) == 1) {
							continue;

						}
						YemekOgun pdksYemek = (YemekOgun) pdksYemekOgunOrj.clone();
						calendar.setTime(kgsHareket.getZaman());
						calendar.set(Calendar.HOUR_OF_DAY, pdksYemek.getBaslangicSaat());
						calendar.set(Calendar.MINUTE, pdksYemek.getBaslangicDakika());
						long yemekZamaniBas = Long.parseLong(PdksUtil.convertToDateString(calendar.getTime(), "yyyyMMddHHmm"));
						if (pdksYemek.getBitisSaat() < pdksYemek.getBaslangicSaat()) {
							int saat = PdksUtil.getDateField(kgsHareket.getZaman(), Calendar.HOUR_OF_DAY);
							if (saat >= pdksYemek.getBaslangicSaat())
								calendar.add(Calendar.DATE, 1);
							else {
								calendar.add(Calendar.DATE, -1);
								yemekGun = calendar.getTime();
								yemekZamaniBas = Long.parseLong(PdksUtil.convertToDateString(calendar.getTime(), "yyyyMMddHHmm"));
								calendar.setTime(kgsHareket.getZaman());
							}
						}
						calendar.set(Calendar.HOUR_OF_DAY, pdksYemek.getBitisSaat());
						calendar.set(Calendar.MINUTE, pdksYemek.getBitisDakika());
						long yemekZamaniBit = Long.parseLong(PdksUtil.convertToDateString(calendar.getTime(), "yyyyMMddHHmm"));

						if ((yemekZamaniBas <= yemekZamaniKgs && yemekZamaniKgs < yemekZamaniBit)) {
							pdksYemekOgun = (YemekOgun) pdksYemekOgunOrj.clone();
							break;
						}
					}
					if (pdksYemekOgun == null)
						pdksYemekOgun = tanimYemek;
				}

				kgsHareket.setYemekOgun(pdksYemekOgun);
				String key = PdksUtil.convertToDateString(yemekGun, "yyyyMMdd") + "_" + pdksYemekOgun.getId() + "_" + personelView.getId();
				List<HareketKGS> hareketler = veriMap.containsKey(key) ? veriMap.get(key) : new ArrayList<HareketKGS>();
				// boolean ekle = true;
				if (hareketler.isEmpty())
					veriMap.put(key, hareketler);
				// else {
				// for (KgsHareket hareket : hareketler) {
				// if (hareket.getKapiId() == kgsHareket.getKapiId())
				// ekle = false;
				// }
				//
				// }
				// if (ekle)
				hareketler.add(kgsHareket);

			}

			if (!veriMap.isEmpty()) {
				Boolean durum = Boolean.TRUE;
				for (String key : veriMap.keySet()) {
					List<HareketKGS> hareketler = veriMap.get(key);
					if (hareketler.size() != 1) {
						List<Long> kapilar = new ArrayList<Long>();
						for (HareketKGS kgsHareket2 : hareketler) {
							if (!kapilar.contains(kgsHareket2.getKapiId()))
								kapilar.add(kgsHareket2.getKapiId());
						}
						for (HareketKGS kgsHareket2 : hareketler) {
							kgsHareket2.setToplam(kapilar.size());
							kgsHareket2.setStyle(durum.toString());
						}
						durum = !durum;
						kgsSonucList.addAll(hareketler);
						kapilar = null;
						hareketler = null;
					}
				}

			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		setHareketList(kgsSonucList);

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

	public HareketKGS getKgs() {
		return kgs;
	}

	public void setKgs(HareketKGS kgs) {
		this.kgs = kgs;
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

	public List<HareketKGS> getCiftYemekList() {
		return ciftYemekList;
	}

	public void setCiftYemekList(List<HareketKGS> ciftYemekList) {
		this.ciftYemekList = ciftYemekList;
	}

	public List<HareketKGS> getToplamYemekList() {
		return toplamYemekList;
	}

	public void setToplamYemekList(List<HareketKGS> toplamYemekList) {
		this.toplamYemekList = toplamYemekList;
	}

}
