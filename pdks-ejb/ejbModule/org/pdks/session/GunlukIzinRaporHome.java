package org.pdks.session;

import java.io.ByteArrayOutputStream;
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
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.security.entity.User;

@Name("gunlukIzinRaporHome")
public class GunlukIzinRaporHome extends EntityHome<PersonelIzin> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8892272759057775075L;

	static Logger logger = Logger.getLogger(GunlukIzinRaporHome.class);

	@RequestParameter
	Long personelIzinId;

	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	private Tanim seciliTesis, seciliEkSaha1, seciliEkSaha2, seciliEkSaha3, seciliEkSaha4;
	private Sirket sirket;
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private HashMap<String, PersonelIzin> izinlerMap;

	private List<PersonelIzin> personelizinList = new ArrayList<PersonelIzin>();
	private List<Personel> pdksPersonelList = new ArrayList<Personel>();
	private List<Sirket> sirketList = new ArrayList<Sirket>();
	private List<Tanim> izinTanimList = new ArrayList<Tanim>();
	private String ad = "", soyad = "", sicilNo = "";
	private TreeMap<String, Tatil> tatilMap;

	private List<Date> gunlerList = new ArrayList<Date>();

	private Date basTarih, bitTarih;
	private Integer yil;
	private Tanim izinTipiTanim;
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
		if (personelIzinId == null) {
			return super.getId();
		} else {
			return personelIzinId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	private void fillEkSahaTanim() {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		fillEkSahaTanim();
		fillTarih();
		setSirket(null);
		if (authenticatedUser.isIK() || authenticatedUser.isAdmin()) {
			fillSirketList();
			setPdksPersonelList(new ArrayList<Personel>());
		} else
			setSirket(authenticatedUser.getPdksPersonel().getSirket());
		setPdksPersonelList(new ArrayList<Personel>());
		fillIzinTanimList();
		setIzinTipiTanim(null);
	}

	public void fillIzinTanimList() {
		HashMap map = new HashMap();
		map.put(PdksEntityController.MAP_KEY_MAP, "getKodu");
		map.put(PdksEntityController.MAP_KEY_SELECT, "izinTipiTanim");
		map.put("durum=", Boolean.TRUE);
		map.put("bakiyeIzinTipi=", null);
		if (!authenticatedUser.isAdmin())
			map.put("departman=", authenticatedUser.getDepartman());
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		TreeMap<String, Tanim> izinTipiTanimMap = pdksEntityController.getObjectByInnerObjectMapInLogic(map, IzinTipi.class, Boolean.FALSE);

		List<Tanim> list = new ArrayList<Tanim>(izinTipiTanimMap.values());
		if (list.size() > 1)
			list = PdksUtil.sortObjectStringAlanList(list, "getAciklama", null);
		setIzinTanimList(list);

	}

	public void fillSirketList() {
		HashMap map = new HashMap();
		map.put("durum", Boolean.TRUE);
		map.put("pdks", Boolean.TRUE);
		if (!authenticatedUser.isAdmin())
			map.put("departman", authenticatedUser.getDepartman());

		map.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Sirket> list = pdksEntityController.getObjectByInnerObjectList(map, Sirket.class);
		if (list.size() > 1)
			list = PdksUtil.sortObjectStringAlanList(list, "getAd", null);
		setSirketList(list);
	}

	public void fillTarih() {
		Calendar cal = Calendar.getInstance();
		bitTarih = (Date) cal.getTime().clone();
		cal.add(Calendar.MONTH, -1);
		basTarih = cal.getTime();

	}

	public String getTarihClass(Date date) {
		String key = PdksUtil.convertToDateString(date, "yyyyMMdd");
		String tarihClass = "normal";
		if (tatilMap.containsKey(key)) {
			Tatil tatil = tatilMap.get(key);
			if (!tatil.isYarimGunMu())
				tarihClass = "bayram";
			else
				tarihClass = "arife";

		}
		return tarihClass;
	}

	public String getIzinClass(Date date, Personel pdksPersonel) {
		PersonelIzin personelIzin = getIzin(date, pdksPersonel);
		return personelIzin == null ? "" : "izin";
	}

	public PersonelIzin getIzin(Date date, Personel pdksPersonel) {
		String key = PdksUtil.convertToDateString(date, "yyyyMMdd") + "_" + pdksPersonel.getId();
		return izinlerMap.containsKey(key) ? izinlerMap.get(key) : null;
	}

	public String excelAktar() {
		try {
			ByteArrayOutputStream baosDosya = excelAktarDevam();
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, "gunlukIzinRapor.xlsx");
		} catch (Exception e) {

		}
		return "";
	}

	private ByteArrayOutputStream excelAktarDevam() {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, "Izin WebService Listesi", false);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);

		int row = 0;
		int col = 0;
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		boolean tesisDurum = ortakIslemler.getListTesisDurum(pdksPersonelList);
		if (tesisDurum)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
		String bolumAdi = null;
		if (ekSahaTanimMap != null && ekSahaTanimMap.containsKey("ekSaha3") && ortakIslemler.getListEkSahaDurum(pdksPersonelList, "3")) {
			bolumAdi = ekSahaTanimMap.get("ekSaha3").getAciklama();
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAdi);
		}
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Personel");

		for (Date tarih : gunlerList) {
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(authenticatedUser.dateFormatla(tarih));
		}

		boolean renk = true;
		for (Personel personel : pdksPersonelList) {

			++row;
			col = 0;
			CellStyle style = null, styleCenter = null;
			if (renk) {
				style = styleOdd;
				styleCenter = styleOddCenter;
			} else {
				style = styleEven;
				styleCenter = styleEvenCenter;
			}
			renk = !renk;

			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getSirket().getAd());
			if (tesisDurum)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getSirket().getTesisDurum() && personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
			if (bolumAdi != null)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getPdksSicilNo());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
			for (Date tarih : gunlerList) {
				PersonelIzin personelIzin = getIzin(tarih, personel);
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personelIzin != null ? personelIzin.getIzinTipi().getIzinTipiTanim().getAciklama() : "");
			}
		}

		try {

			for (int i = 0; i < col; i++)
				sheet.autoSizeColumn(i);
			baos = new ByteArrayOutputStream();
			wb.write(baos);
		} catch (Exception e) {

			e.printStackTrace();

			baos = null;
		}
		return baos;
	}

	public void fillIzinList() {
		Calendar cal = Calendar.getInstance();
		izinlerMap = new HashMap<String, PersonelIzin>();
		gunlerList = new ArrayList<Date>();
		String izinTipiKodu = null;
		if (izinTipiTanim != null)
			izinTipiKodu = izinTipiTanim.getKodu();
		List<Personel> personelList = new ArrayList<Personel>();

		long fark = PdksUtil.tarihFarki(basTarih, bitTarih);
		if (fark < 0)
			PdksUtil.addMessageWarn("Başlangıç tarihi bitiş tarihinden büyük olamaz!");
		else if (fark > 31)
			PdksUtil.addMessageWarn("31 günden fazla seçemezsiniz");
		else if (PdksUtil.hasStringValue(sicilNo) == false && sirket == null) {
			PdksUtil.addMessageWarn("" + ortakIslemler.sirketAciklama() + " seçiniz!");
		} else {
			ArrayList<String> sicilNoList = ortakIslemler.getPersonelSicilNo(ad, soyad, sicilNo, sirket, seciliTesis, seciliEkSaha1, seciliEkSaha2, seciliEkSaha3, seciliEkSaha4, Boolean.FALSE, session);
			if (sicilNoList != null && !sicilNoList.isEmpty()) {
				HashMap parametreMap = new HashMap();
				parametreMap.put("pdksSicilNo", sicilNoList.clone());
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				personelList = (ArrayList<Personel>) pdksEntityController.getObjectByInnerObjectList(parametreMap, Personel.class);
				List<Long> idList = new ArrayList<Long>();
				for (Iterator iterator = personelList.iterator(); iterator.hasNext();) {
					Personel personel = (Personel) iterator.next();
					idList.add(personel.getId());
				}

				List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
				List<Integer> izinDurumuList = new ArrayList<Integer>();
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_SAP_GONDERILDI);
				parametreMap.clear();
				parametreMap.put("izinSahibi.id", idList);
				parametreMap.put("baslangicZamani<=", ortakIslemler.tariheGunEkleCikar(cal, bitTarih, 1));
				parametreMap.put("bitisZamani>=", ortakIslemler.tariheGunEkleCikar(cal, basTarih, -1));
				parametreMap.put("izinDurumu", izinDurumuList);
				if (izinTipiKodu != null) {
					HashMap fields = new HashMap();
					fields.put(PdksEntityController.MAP_KEY_SELECT, "id");
					if (sirket != null)
						fields.put("departman.id=", sirket.getDepartman().getId());
					fields.put("bakiyeIzinTipi=", null);
					fields.put("izinTipiTanim.kodu=", izinTipiKodu);
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<Long> izinTipiIdList = pdksEntityController.getObjectByInnerObjectListInLogic(fields, IzinTipi.class);
					parametreMap.put("izinTipi.id", izinTipiIdList);
				}

				else {
					if (sirket != null)
						parametreMap.put("izinTipi.departman.id=", sirket.getDepartman().getId());
					parametreMap.put("izinTipi.bakiyeIzinTipi=", null);
				}

				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				// izinList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, PersonelIzin.class);
				izinList = ortakIslemler.getParamList(true, idList, "izinSahibi.id", parametreMap, PersonelIzin.class, session);
				idList = null;
				if (!izinList.isEmpty())
					izinList = PdksUtil.sortListByAlanAdi(izinList, "baslangicZamani", Boolean.TRUE);
				Date tarih1 = null, tarih2 = null;
				for (PersonelIzin personelIzin : izinList) {
					if (tarih1 == null || personelIzin.getBaslangicZamani().before(tarih1)) {
						tarih1 = personelIzin.getBaslangicZamani();

					}
					if (tarih2 == null || personelIzin.getBitisZamani().after(tarih2)) {
						tarih2 = personelIzin.getBitisZamani();
					}

				}
				if (tarih1.before(basTarih))
					tarih1 = basTarih;
				if (tarih2.after(bitTarih))
					tarih2 = bitTarih;

				while (PdksUtil.tarihKarsilastirNumeric(tarih2, tarih1) >= 0) {
					gunlerList.add(tarih1);
					tarih1 = ortakIslemler.tariheGunEkleCikar(cal, tarih1, 1);
				}
				
				for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
					PersonelIzin personelIzin = (PersonelIzin) iterator.next();
 					double saat = PdksUtil.getSaatFarki(personelIzin.getBitisZamani(), personelIzin.getBaslangicZamani());
					if (saat < 24)
						continue;
					tarih1 = (Date) personelIzin.getBaslangicZamani().clone();
					tarih2 = (Date) personelIzin.getBitisZamani().clone();
					while (PdksUtil.tarihKarsilastirNumeric(tarih2, tarih1) != -1) {
						String key = PdksUtil.convertToDateString(tarih1, "yyyyMMdd") + "_" + personelIzin.getIzinSahibi().getId();
						izinlerMap.put(key, personelIzin);
						tarih1 = PdksUtil.tariheGunEkleCikar(tarih1, 1);
					}
				}
			}

		}

		setPdksPersonelList(personelList);

	}

	public void izinGoster(Personel pdksPersonel) {
		getInstance().setIzinSahibi(pdksPersonel);
		List<PersonelIzin> izinList1 = pdksPersonel.getPersonelIzinList();
		setPersonelizinList(izinList1);

	}

	public List<PersonelIzin> getPersonelizinList() {
		return personelizinList;
	}

	public void setPersonelizinList(List<PersonelIzin> personelizinList) {
		this.personelizinList = personelizinList;
	}

	public List<Personel> getPdksPersonelList() {
		return pdksPersonelList;
	}

	public void setPdksPersonelList(List<Personel> pdksPersonelList) {
		this.pdksPersonelList = pdksPersonelList;
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

	public Integer getYil() {
		return yil;
	}

	public void setYil(Integer yil) {
		this.yil = yil;
	}

	public List<Sirket> getSirketList() {
		return sirketList;
	}

	public void setSirketList(List<Sirket> sirketList) {
		this.sirketList = sirketList;
	}

	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket sirket) {
		this.sirket = sirket;
	}

	public String getAd() {
		return ad;
	}

	public void setAd(String ad) {
		this.ad = ad;
	}

	public String getSoyad() {
		return soyad;
	}

	public void setSoyad(String soyad) {
		this.soyad = soyad;
	}

	public String getSicilNo() {
		return sicilNo;
	}

	public void setSicilNo(String sicilNo) {
		this.sicilNo = sicilNo;
	}

	public List<Tanim> getIzinTanimList() {
		return izinTanimList;
	}

	public void setIzinTanimList(List<Tanim> izinTanimList) {
		this.izinTanimList = izinTanimList;
	}

	public Tanim getIzinTipiTanim() {
		return izinTipiTanim;
	}

	public void setIzinTipiTanim(Tanim izinTipiTanim) {
		this.izinTipiTanim = izinTipiTanim;
	}

	public Tanim getSeciliEkSaha1() {
		return seciliEkSaha1;
	}

	public void setSeciliEkSaha1(Tanim seciliEkSaha1) {
		this.seciliEkSaha1 = seciliEkSaha1;
	}

	public Tanim getSeciliEkSaha2() {
		return seciliEkSaha2;
	}

	public void setSeciliEkSaha2(Tanim seciliEkSaha2) {
		this.seciliEkSaha2 = seciliEkSaha2;
	}

	public Tanim getSeciliEkSaha3() {
		return seciliEkSaha3;
	}

	public void setSeciliEkSaha3(Tanim seciliEkSaha3) {
		this.seciliEkSaha3 = seciliEkSaha3;
	}

	public Tanim getSeciliEkSaha4() {
		return seciliEkSaha4;
	}

	public void setSeciliEkSaha4(Tanim seciliEkSaha4) {
		this.seciliEkSaha4 = seciliEkSaha4;
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

	public List<Date> getGunlerList() {
		return gunlerList;
	}

	public void setGunlerList(List<Date> gunlerList) {
		this.gunlerList = gunlerList;
	}

	public HashMap<String, PersonelIzin> getIzinlerMap() {
		return izinlerMap;
	}

	public void setIzinlerMap(HashMap<String, PersonelIzin> izinlerMap) {
		this.izinlerMap = izinlerMap;
	}

	public TreeMap<String, Tatil> getTatilMap() {
		return tatilMap;
	}

	public void setTatilMap(TreeMap<String, Tatil> tatilMap) {
		this.tatilMap = tatilMap;
	}

	public Tanim getSeciliTesis() {
		return seciliTesis;
	}

	public void setSeciliTesis(Tanim seciliTesis) {
		this.seciliTesis = seciliTesis;
	}

}
