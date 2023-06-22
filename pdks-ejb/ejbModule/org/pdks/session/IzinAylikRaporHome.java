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

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
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
import org.pdks.entity.VardiyaGun;
import org.pdks.security.entity.User;

@Name("izinAylikRaporHome")
public class IzinAylikRaporHome extends EntityHome<PersonelIzin> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4510203488828369554L;
	static Logger logger = Logger.getLogger(IzinAylikRaporHome.class);

	@RequestParameter
	Long personelIzinId;
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false)
	User authenticatedUser;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	@In(required = true, create = true)
	PersonelIzinGirisiHome personelIzinGirisiHome;

	User yoneticiUser;

	private int yil, maxYil, gunSayisi;
	private String izinAciklama, style, ay;

	private List<SelectItem> ayList = new ArrayList<SelectItem>();
	private List<SelectItem> dataList = new ArrayList<SelectItem>();
	private List<Integer> gunList = new ArrayList<Integer>();
	private HashMap<String, IzinTipi> izinTipiMap = new HashMap<String, IzinTipi>();
	private List<Personel> pdksPersonelList = new ArrayList<Personel>();
	private List<User> userList = new ArrayList<User>();
	private List<String> ccMailList = new ArrayList<String>();
	private Sirket sirket;
	private List<Sirket> sirketList = new ArrayList<Sirket>();
	private TreeMap<String, VardiyaGun> vardiyaMap;
	private HashMap<Integer, String> gunler;
	private Date bitDate, basDate;
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

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		fillAyList();
		setSirket(null);
		if (authenticatedUser.isIK() || authenticatedUser.isAdmin()) {
			fillSirketList();
			setpdksPersonelList(new ArrayList<Personel>());
		} else
			setSirket(authenticatedUser.getPdksPersonel().getSirket());
		setpdksPersonelList(new ArrayList<Personel>());
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

	public void fillAyList() {
		Calendar cal = Calendar.getInstance();
		setYil(cal.get(Calendar.YEAR));
		setMaxYil(cal.get(Calendar.YEAR) + 1);

		setAy(String.valueOf(cal.get(Calendar.MONTH)));
		List<SelectItem> list = PdksUtil.getAyListesi(Boolean.FALSE);
		setAyList(list);
	}

	/**
	 * @param personel
	 * @return
	 */
	public List<SelectItem> getData(Personel personel) {

		dataList.clear();
		for (Iterator iterator = gunList.iterator(); iterator.hasNext();) {
			Integer gun = (Integer) iterator.next();
			dataList.add(getSelectItem(gun, personel));
		}
		return dataList;
	}

	/**
	 * @param gun
	 * @param personel
	 * @return
	 */
	public SelectItem getSelectItem(Integer gun, Personel personel) {
		String label = "", value = "", key = "", aciklama = "";
		boolean bold = Boolean.FALSE;
		try {
			IzinTipi izinTipi = null;

			if (personel != null && personel.getId() != null && gun != null) {
				key = gun + "_" + personel.getId();
				if (izinTipiMap.containsKey(key))
					izinTipi = izinTipiMap.get(key);
				if (izinTipi != null) {
					label = izinTipi.getKisaAciklama();
					if (label == null)
						label = izinTipi.getIzinTipiTanim().getAciklama();
					else
						aciklama = izinTipi.getIzinTipiTanim().getAciklama();
					value = "izin";

				}
				if (value.equals("") && gunler.containsKey(gun)) {
					key = personel.getSicilNo() + "_" + gunler.get(gun);
					if (vardiyaMap.containsKey(key)) {
						VardiyaGun pdksVardiyaGun = vardiyaMap.get(key);
						if (pdksVardiyaGun != null && pdksVardiyaGun.getVardiya() != null && pdksVardiyaGun.getVardiya().isHaftaTatil()) {
							value = "haftaTatil";
							aciklama = "Hafta Tatil";
							label = "HT";
							bold = Boolean.TRUE;
						}

					}
				}
			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.error(key);
		}

		SelectItem selectItem = new SelectItem(value, label, aciklama);
		selectItem.setDisabled(!bold);
		return selectItem;
	}

	public String excelAktar() {
		try {
			ByteArrayOutputStream baosDosya = excelDevam();
			if (baosDosya != null) {
				String dosya = PdksUtil.convertToDateString(basDate, "MMMMM-yyyy") + "_İzinRaporu.xlsx";
				PdksUtil.setExcelHttpServletResponse(baosDosya, dosya);
			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			PdksUtil.addMessageError(e.getMessage());
		}

		return "";
	}

	public ByteArrayOutputStream excelDevam() throws Exception {
		ByteArrayOutputStream baos = null;

		Workbook wb = new XSSFWorkbook();

		Sheet sheet = ExcelUtil.createSheet(wb, PdksUtil.setTurkishStr(PdksUtil.convertToDateString(basDate, "MMMMM-yyyy") + " Aylik Izin Raporu"), Boolean.TRUE);

		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		int row = 0;
		ExcelUtil.getCell(sheet, row, 0, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, 1, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, 2, header).setCellValue(ortakIslemler.sirketAciklama());
		int sayac = 2;
		for (Iterator iterator = gunList.iterator(); iterator.hasNext();) {
			Integer gun = (Integer) iterator.next();
			ExcelUtil.getCell(sheet, row, ++sayac, header).setCellValue(gun.toString());

		}
		boolean renk = true;
		for (Iterator iter = pdksPersonelList.iterator(); iter.hasNext();) {
			Personel personel = (Personel) iter.next();
			row++;
			CellStyle style = null, styleCenter = null;
			if (renk) {
				style = styleOdd;
				styleCenter = styleOddCenter;
			} else {
				style = styleEven;
				styleCenter = styleEvenCenter;
			}
			renk = !renk;
			try {
				if (personel.getId() != null)
					ExcelUtil.getCell(sheet, row, 0, style).setCellValue(personel.getAdSoyad());
				else
					ExcelUtil.getCell(sheet, row, 0, header).setCellValue(personel.getEkSaha() != null ? personel.getEkSaha().getAciklama() : "");
				ExcelUtil.getCell(sheet, row, 1, styleCenter).setCellValue(personel.getSicilNo());
				String sirket = "";
				try {
					if ((personel.getId() != null))
						sirket = personel.getSirket().getAd();
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					sirket = "" + ortakIslemler.sirketAciklama() + " tanımsız";
				}
				ExcelUtil.getCell(sheet, row, 2, style).setCellValue(sirket);
				sayac = 2;
				for (Iterator iterator = dataList.iterator(); iterator.hasNext();) {
					SelectItem selectItem = (SelectItem) iterator.next();
					Cell cell = ExcelUtil.getCell(sheet, row, ++sayac, selectItem.isDisabled() == Boolean.FALSE ? style : header);
					cell.setCellValue(selectItem.getLabel());

				}

			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				logger.debug(row);

			}

		}
		double katsayi = 3.43;
		int[] dizi = new int[4 + dataList.size()];
		dizi[0] = 2000;
		dizi[1] = 1000;
		dizi[2] = 1500;
		dizi[3] = 1000;
		for (int i = 0; i < dataList.size(); i++)
			dizi[4 + i] = 300;

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

	public void fillIzinList() throws Exception {

		pdksPersonelList.clear();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, Integer.parseInt(ay));
		cal.set(Calendar.YEAR, yil);
		basDate = PdksUtil.getDate((Date) cal.getTime().clone());
		cal.add(Calendar.MONTH, 1);
		bitDate = PdksUtil.getDate((Date) cal.getTime().clone());
		gunler = new HashMap<Integer, String>();
		cal.setTime(basDate);
		Date tarih = PdksUtil.getDate((Date) cal.getTime().clone());
		Integer gunSayisi = 0;
		gunList = new ArrayList<Integer>();
		while (tarih.getTime() < bitDate.getTime()) {
			gunler.put(++gunSayisi, PdksUtil.convertToDateString(tarih, "yyyyMMdd"));
			gunList.add(gunSayisi);
			cal.add(Calendar.DATE, 1);
			tarih = PdksUtil.getDate((Date) cal.getTime().clone());
		}

		List<Integer> durum = new ArrayList<Integer>();
		List<Integer> hesapTipi = new ArrayList<Integer>();
		durum.add(PersonelIzin.IZIN_DURUMU_IKINCI_YONETICI_ONAYINDA);
		durum.add(PersonelIzin.IZIN_DURUMU_IK_ONAYINDA);
		durum.add(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
		durum.add(PersonelIzin.IZIN_DURUMU_SAP_GONDERILDI);

		hesapTipi.add(PersonelIzin.HESAP_TIPI_GUN);
		hesapTipi.add(PersonelIzin.HESAP_TIPI_SAAT_GUN_SECILDI);
		Tanim ekSaha = null;
		ArrayList<String> sicilNoList = ortakIslemler.getPersonelSicilNo("", "", "", sirket, null, ekSaha, ekSaha, ekSaha, ekSaha, Boolean.FALSE, session);
		HashMap parametreMap = new HashMap();
		parametreMap.put("pdksSicilNo", sicilNoList.clone());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		ArrayList<Personel> personelList = (ArrayList<Personel>) pdksEntityController.getObjectByInnerObjectList(parametreMap, Personel.class);
		for (Iterator iterator = personelList.iterator(); iterator.hasNext();) {
			Personel personel = (Personel) iterator.next();
			boolean devam = Boolean.FALSE;
			try {
				devam = personel.getIseBaslamaTarihi().getTime() <= bitDate.getTime() && basDate.getTime() <= personel.getSonCalismaTarihi().getTime();

			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				devam = Boolean.TRUE;
			}
			if (!devam) {
				iterator.remove();
				continue;
			}

			ekSaha = personel.getSirket().getDepartman().isAdminMi() ? personel.getEkSaha4() : personel.getGorevTipi();
			personel.setEkSaha(ekSaha);
			personel.setSortAlanAdi((ekSaha != null ? ekSaha.getAciklama() : "") + "_" + personel.getAdSoyad());

		}

		if (!personelList.isEmpty()) {
			parametreMap.clear();
			parametreMap.put("izinTipi.bakiyeIzinTipi=", null);
			parametreMap.put("izinDurumu", durum);
			parametreMap.put("baslangicZamani<=", bitDate);
			parametreMap.put("bitisZamani>=", basDate);
			parametreMap.put("hesapTipi", hesapTipi);
			parametreMap.put("izinSahibi", personelList.clone());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelIzin> personelizinList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, PersonelIzin.class);
			vardiyaMap = ortakIslemler.getVardiyalar((List<Personel>) personelList.clone(), basDate, bitDate, Boolean.FALSE, session, Boolean.TRUE);
			if (!personelizinList.isEmpty()) {
				TreeMap<String, Tatil> tatilGunleriMap = ortakIslemler.getTatilGunleri(personelList, basDate, bitDate, session);

				for (Iterator iterator = personelizinList.iterator(); iterator.hasNext();) {
					PersonelIzin personelIzin = (PersonelIzin) iterator.next();
					if (personelIzin.getBaslangicZamani().getTime() >= bitDate.getTime()) {
						iterator.remove();
						continue;
					}
					IzinTipi izinTipi = personelIzin.getIzinTipi();
					long baslangicZamani = Long.parseLong(PdksUtil.convertToDateString(personelIzin.getBaslangicZamani(), "yyyyMMdd"));
					long bitZamani = Long.parseLong(PdksUtil.convertToDateString(PdksUtil.tariheGunEkleCikar((Date) personelIzin.getBitisZamani().clone(), -1), "yyyyMMdd"));
					Personel personel = personelIzin.getIzinSahibi();
					for (Iterator iterator2 = gunler.keySet().iterator(); iterator2.hasNext();) {
						Integer gun = (Integer) iterator2.next();
						String gunu = gunler.get(gun);
						Long tarihi = Long.parseLong(gunu);
						if (tarihi >= baslangicZamani && tarihi <= bitZamani) {

							if (!izinTipi.getTakvimGunumu()) {
								if (tatilGunleriMap.containsKey(gunu)) {
									if (!tatilGunleriMap.get(gunu).isYarimGunMu())
										continue;
								} else {
									String key = personel.getSicilNo() + "_" + gunu;
									if (vardiyaMap.containsKey(key)) {
										VardiyaGun pdksVardiyaGun = vardiyaMap.get(key);
										if (pdksVardiyaGun == null || pdksVardiyaGun.getVardiya() == null || !pdksVardiyaGun.getVardiya().isHaftaTatil())
											vardiyaMap.remove(key);
										else
											continue;

									}
								}
							}

							String key = gun + "_" + personel.getId();
							izinTipiMap.put(key, izinTipi);

						}

					}

				}

			}

			HashMap<String, Tanim> bol = new HashMap<String, Tanim>();
			List<Personel> list = PdksUtil.sortObjectStringAlanList(null, personelList, "getSortAlanAdi", null);
			boolean sc = Boolean.FALSE;
			for (Personel pdksPersonel : list) {
				Tanim alan = pdksPersonel.getEkSaha();
				String bolum = "" + (alan != null ? alan.getId() : 0);
				if (!bol.containsKey(bolum)) {
					sc = !sc;
					Personel pdksPersonelBolum = new Personel();
					pdksPersonelBolum.setEkSaha(alan);
					pdksPersonelList.add(pdksPersonelBolum);
					pdksPersonelBolum.setSortAlanAdi(String.valueOf(sc));
					bol.put(bolum, alan);
				}
				pdksPersonel.setSortAlanAdi(String.valueOf(sc));
				pdksPersonelList.add(pdksPersonel);

			}
		}

	}

	public List<Personel> getpdksPersonelList() {
		return pdksPersonelList;
	}

	public void setpdksPersonelList(List<Personel> pdksPersonelList) {
		this.pdksPersonelList = pdksPersonelList;
	}

	public List<User> getUserList() {
		return userList;
	}

	public void setUserList(List<User> userList) {
		this.userList = userList;
	}

	public User getYoneticiUser() {
		return yoneticiUser;
	}

	public void setYoneticiUser(User yoneticiUser) {
		this.yoneticiUser = yoneticiUser;
	}

	public String getIzinAciklama() {
		return izinAciklama;
	}

	public void setIzinAciklama(String izinAciklama) {
		this.izinAciklama = izinAciklama;
	}

	public List<String> getCcMailList() {
		return ccMailList;
	}

	public void setCcMailList(List<String> ccMailList) {
		this.ccMailList = ccMailList;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public int getYil() {
		return yil;
	}

	public void setYil(int yil) {
		this.yil = yil;
	}

	public String getAy() {
		return ay;
	}

	public void setAy(String ay) {
		this.ay = ay;
	}

	public List<SelectItem> getAyList() {
		return ayList;
	}

	public void setAyList(List<SelectItem> ayList) {
		this.ayList = ayList;
	}

	public int getMaxYil() {
		return maxYil;
	}

	public void setMaxYil(int maxYil) {
		this.maxYil = maxYil;
	}

	public List<Integer> getGunList() {
		return gunList;
	}

	public void setGunList(List<Integer> gunList) {
		this.gunList = gunList;
	}

	public HashMap<String, IzinTipi> getIzinTipiMap() {
		return izinTipiMap;
	}

	public void setIzinTipiMap(HashMap<String, IzinTipi> izinTipiMap) {
		this.izinTipiMap = izinTipiMap;
	}

	public int getGunSayisi() {
		return gunSayisi;
	}

	public void setGunSayisi(int gunSayisi) {
		this.gunSayisi = gunSayisi;
	}

	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket sirket) {
		this.sirket = sirket;
	}

	public List<Sirket> getSirketList() {
		return sirketList;
	}

	public void setSirketList(List<Sirket> sirketList) {
		this.sirketList = sirketList;
	}

	public TreeMap<String, VardiyaGun> getVardiyaMap() {
		return vardiyaMap;
	}

	public void setVardiyaMap(TreeMap<String, VardiyaGun> vardiyaMap) {
		this.vardiyaMap = vardiyaMap;
	}

	public HashMap<Integer, String> getGunler() {
		return gunler;
	}

	public void setGunler(HashMap<Integer, String> gunler) {
		this.gunler = gunler;
	}

	public List<SelectItem> getDataList() {
		return dataList;
	}

	public void setDataList(List<SelectItem> dataList) {
		this.dataList = dataList;
	}

	public Date getBitDate() {
		return bitDate;
	}

	public void setBitDate(Date bitDate) {
		this.bitDate = bitDate;
	}

	public Date getBasDate() {
		return basDate;
	}

	public void setBasDate(Date basDate) {
		this.basDate = basDate;
	}

}
