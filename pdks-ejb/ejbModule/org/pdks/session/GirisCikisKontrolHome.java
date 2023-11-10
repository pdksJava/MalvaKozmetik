package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.poi.ss.usermodel.RichTextString;
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
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.security.entity.User;

@Name("girisCikisKontrolHome")
public class GirisCikisKontrolHome extends EntityHome<VardiyaGun> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2557956271836136251L;
	static Logger logger = Logger.getLogger(GirisCikisKontrolHome.class);

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

	List<HareketKGS> hareketList = new ArrayList<HareketKGS>();
	List<VardiyaGun> vardiyaGunList = new ArrayList<VardiyaGun>();

	private String islemTipi, bolumAciklama, sicilNo;
	private Date date;
	private AramaSecenekleri aramaSecenekleri = null;
	private boolean tesisDurum = false;
	private Session session;

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
		setHareketList(new ArrayList<HareketKGS>());
		setVardiyaGunList(new ArrayList<VardiyaGun>());
		HareketKGS hareket = new HareketKGS();
		hareket.setPersonel(new PersonelView());
		hareket.setKapiView(new KapiView());
		hareket.setIslem(new PersonelHareketIslem());
		setDate(new Date());
		if (aramaSecenekleri == null)
			aramaSecenekleri = new AramaSecenekleri(authenticatedUser);
		fillEkSahaTanim();
		if (aramaSecenekleri.getDepartmanId() == null) {
			aramaSecenekleri.setDepartman(authenticatedUser.getDepartman());
			aramaSecenekleri.setDepartmanId(authenticatedUser.getDepartman().getId());
		}
		tesisDurum = false;

		fillSirketList();
	}

	public String fillSirketList() {
		Date bugun = PdksUtil.getDate(date);
		List<Sirket> list = new ArrayList<Sirket>();
		HashMap map = new HashMap();
		map.put(PdksEntityController.MAP_KEY_MAP, "getId");
		map.put(PdksEntityController.MAP_KEY_SELECT, "sirket");
		map.put("pdks=", Boolean.TRUE);
		map.put("durum=", Boolean.TRUE);
		map.put("sskCikisTarihi>=", bugun);
		map.put("iseBaslamaTarihi<=", bugun);
		if (aramaSecenekleri.getDepartmanId() != null && (authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || authenticatedUser.isIKAdmin() || !authenticatedUser.isYoneticiKontratli()))
			map.put("sirket.departman.id=", aramaSecenekleri.getDepartmanId());
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		TreeMap sirketMap = pdksEntityController.getObjectByInnerObjectMapInLogic(map, Personel.class, Boolean.FALSE);

		aramaSecenekleri.setSirketId(null);
		if (aramaSecenekleri.getSirketIdList() != null)
			aramaSecenekleri.getSirketIdList().clear();
		else
			aramaSecenekleri.setSirketIdList(new ArrayList<SelectItem>());
		if (!sirketMap.isEmpty()) {
			Long sirketId = null;
			list = PdksUtil.sortObjectStringAlanList(new ArrayList<Sirket>(sirketMap.values()), "getAd", null);
			for (Sirket sirket : list) {
				if (sirket.getDurum() && sirket.getFazlaMesai())
					aramaSecenekleri.getSirketIdList().add(new SelectItem(sirket.getId(), sirket.getAd()));
			}
			if (aramaSecenekleri.getSirketIdList().size() == 1)
				sirketId = (Long) aramaSecenekleri.getSirketIdList().get(0).getValue();
			aramaSecenekleri.setSirketId(sirketId);
			fillTesisList();
		} else {
			if (aramaSecenekleri.getTesisList() != null)
				aramaSecenekleri.getTesisList().clear();
			else
				aramaSecenekleri.setTesisList(new ArrayList<SelectItem>());
		}
		clearVardiyaList();
		return "";
	}

	public String clearVardiyaList() {
		vardiyaGunList.clear();
		hareketList.clear();
		return "";
	}

	public String fillTesisList() {
		if (aramaSecenekleri.getTesisList() != null)
			aramaSecenekleri.getTesisList().clear();
		else
			aramaSecenekleri.setTesisList(new ArrayList<SelectItem>());
		clearVardiyaList();
		Long tesisId = null;
		if (aramaSecenekleri.getSirketId() != null) {
			Date bugun = PdksUtil.getDate(date);
			List<Tanim> list = new ArrayList<Tanim>();
			HashMap map = new HashMap();
			map.put("id ", aramaSecenekleri.getSirketId());
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			Sirket sirket = (Sirket) pdksEntityController.getObjectByInnerObject(map, Sirket.class);
			if (aramaSecenekleri.getTesisList() != null)
				aramaSecenekleri.getTesisList().clear();
			else
				aramaSecenekleri.setTesisList(new ArrayList<SelectItem>());
			if (sirket.isTesisDurumu()) {
				map.clear();
				map.put(PdksEntityController.MAP_KEY_MAP, "getId");
				map.put(PdksEntityController.MAP_KEY_SELECT, "tesis");
				map.put("pdks=", Boolean.TRUE);
				map.put("durum=", Boolean.TRUE);
				map.put("sirket.id=", aramaSecenekleri.getSirketId());
				map.put("sskCikisTarihi>=", bugun);
				map.put("iseBaslamaTarihi<=", bugun);
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				TreeMap tesisMap = pdksEntityController.getObjectByInnerObjectMapInLogic(map, Personel.class, Boolean.FALSE);
				if (!tesisMap.isEmpty()) {
					list = PdksUtil.sortObjectStringAlanList(new ArrayList(tesisMap.values()), "getAciklama", null);
					for (Tanim tesis : list) {
						if (tesisId == null)
							tesisId = tesis.getId();
						aramaSecenekleri.getTesisList().add(new SelectItem(tesis.getId(), tesis.getAciklama()));
					}
					aramaSecenekleri.setTesisId(tesisId);

				}
			} else {
				tesisId = null;

			}
		}
		aramaSecenekleri.setTesisId(tesisId);

		return "";
	}

	private void fillEkSahaTanim() {
		ortakIslemler.fillEkSahaTanimAramaSecenekAta(session, Boolean.FALSE, Boolean.TRUE, aramaSecenekleri);
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
	}

	public void hareketGoster(VardiyaGun pdksVardiyaGun) {
		setInstance(pdksVardiyaGun);
		List<HareketKGS> kgsList = pdksVardiyaGun.getHareketler();
		setHareketList(kgsList);

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

	private void fillHareketListOlustur() {
		List<VardiyaGun> vardiyaList = new ArrayList<VardiyaGun>();
		List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
		List<HareketKGS> kgsList = new ArrayList<HareketKGS>();
		Date oncekiGun = PdksUtil.tariheGunEkleCikar(date, -1);
		HashMap map = new HashMap();
		map.put("pdks=", Boolean.TRUE);
		map.put("durum=", Boolean.TRUE);
		if (PdksUtil.hasStringValue(sicilNo))
			map.put("pdksSicilNo=", sicilNo);
		if (aramaSecenekleri.getSirketId() != null)
			map.put("sirket.id=", aramaSecenekleri.getSirketId());
		else {
			map.put("sirket.pdks=", true);
			if (aramaSecenekleri.getDepartmanId() != null)
				map.put("sirket.departman.id=", aramaSecenekleri.getDepartmanId());
		}
		if (aramaSecenekleri.getTesisId() != null)
			map.put("tesis.id=", aramaSecenekleri.getTesisId());
		map.put("sskCikisTarihi>=", oncekiGun);
		map.put("iseBaslamaTarihi<=", date);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		ArrayList<Personel> tumPersoneller = (ArrayList<Personel>) pdksEntityController.getObjectByInnerObjectListInLogic(map, Personel.class);
		List<Long> perIdList = new ArrayList<Long>();
		boolean sicilDolu = PdksUtil.hasStringValue(sicilNo);
		for (Iterator iterator = tumPersoneller.iterator(); iterator.hasNext();) {
			Personel per = (Personel) iterator.next();
			if (sicilDolu) {
				if (!per.getPdksSicilNo().equals(sicilNo))
					iterator.remove();
			} else if (per.getSirket().isPdksMi() && per.getPdks().equals(Boolean.TRUE))
				perIdList.add(per.getId());
			else
				iterator.remove();
		}
		TreeMap<String, VardiyaGun> vardiyalarMap = null;
		try {
			vardiyalarMap = ortakIslemler.getIslemVardiyalar((List<Personel>) tumPersoneller.clone(), PdksUtil.tariheGunEkleCikar(date, -3), PdksUtil.tariheGunEkleCikar(date, 3), Boolean.FALSE, session, Boolean.TRUE);

		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		if (vardiyalarMap == null)
			vardiyalarMap = new TreeMap<String, VardiyaGun>();
		vardiyaList = new ArrayList<VardiyaGun>(vardiyalarMap.values());

		Date tarih1 = null;
		Date tarih2 = null;
		HashMap parametreMap = new HashMap();
		parametreMap.put("izinTipi.bakiyeIzinTipi=", null);
		parametreMap.put("izinSahibi.id", perIdList);
		parametreMap.put("izinDurumu not ", Arrays.asList(new Integer[] { PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL, PersonelIzin.IZIN_DURUMU_REDEDILDI }));
		parametreMap.put("baslangicZamani<=", PdksUtil.tariheGunEkleCikar((Date) date.clone(), 1));
		parametreMap.put("bitisZamani>=", PdksUtil.tariheGunEkleCikar((Date) date.clone(), -1));
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		izinList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, PersonelIzin.class);
		// butun personeller icin hareket cekerken bu en kucuk tarih ile en
		// buyuk tarih araligini kullanacaktir
		// bu araliktaki tum hareketleri cekecektir.
		Date simdikiZaman = Calendar.getInstance().getTime();

		for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
			VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator.next();
			if (PdksUtil.tarihKarsilastirNumeric(pdksVardiyaGun.getVardiyaDate(), date) != 0) {
				iterator.remove();
				continue;

			}
			Vardiya vardiya = pdksVardiyaGun.getIslemVardiya();
			boolean calisma = vardiya.isCalisma();
			if (tarih1 == null || (calisma && vardiya.getVardiyaFazlaMesaiBasZaman().getTime() < tarih1.getTime()))
				tarih1 = vardiya.getVardiyaFazlaMesaiBasZaman();
			if (tarih2 == null || (calisma && vardiya.getVardiyaFazlaMesaiBitZaman().getTime() > tarih2.getTime()))
				tarih2 = vardiya.getVardiyaFazlaMesaiBitZaman();
			if (calisma && vardiya.getVardiyaTelorans2BasZaman().getTime() > simdikiZaman.getTime())
				iterator.remove();

		}
		if (tarih1 == null)
			tarih1 = date;
		if (tarih2 == null)
			tarih2 = date;
		tarih1 = PdksUtil.tariheGunEkleCikar(tarih1, -1);
		tarih2 = PdksUtil.tariheGunEkleCikar(tarih2, 1);
		List<Long> kapiIdler = ortakIslemler.getPdksDonemselKapiIdler(tarih1, tarih2, session);
		if (kapiIdler != null && !kapiIdler.isEmpty())
			try {
				kgsList = null;
				kgsList = ortakIslemler.getPdksHareketBilgileri(Boolean.TRUE, kapiIdler, (List<Personel>) tumPersoneller.clone(), tarih1, tarih2, HareketKGS.class, session);

			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
		if (kgsList == null)
			kgsList = new ArrayList<HareketKGS>();
		if (!kgsList.isEmpty())
			kgsList = PdksUtil.sortListByAlanAdi(kgsList, "zaman", Boolean.FALSE);
		TreeMap<Long, List<HareketKGS>> hareketMap = new TreeMap<Long, List<HareketKGS>>();
		for (HareketKGS hareket : kgsList) {
			List<HareketKGS> list = hareketMap.containsKey(hareket.getPersonelId()) ? hareketMap.get(hareket.getPersonelId()) : new ArrayList<HareketKGS>();
			if (list.isEmpty())
				hareketMap.put(hareket.getPersonelId(), list);
			list.add(hareket);
		}

		Long basZaman1 = 0L, basZaman2 = 0L, bitZaman1 = 0L, bitZaman2 = 0L;

		try {

			for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
				VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
				vardiyaGun.setHareketler(null);
				vardiyaGun.setGirisHareketleri(null);
				vardiyaGun.setCikisHareketleri(null);
				Personel personel = vardiyaGun.getPdksPersonel();
				kgsList.clear();
				if (hareketMap.containsKey(personel.getPersonelKGS().getId()))
					kgsList.addAll(hareketMap.get(personel.getPersonelKGS().getId()));
				for (Iterator iterator1 = kgsList.iterator(); iterator1.hasNext();) {
					HareketKGS kgsHareket = (HareketKGS) iterator1.next();
					if (vardiyaGun.addHareket(kgsHareket, Boolean.FALSE))
						iterator1.remove();
				}
				HareketKGS kgsHareketGiris = vardiyaGun.getGirisHareket(), kgsHareketCikis = vardiyaGun.getCikisHareket();
				PersonelIzin izin = null;
				for (Iterator iterator2 = izinList.iterator(); iterator2.hasNext();) {
					PersonelIzin personelIzin = (PersonelIzin) iterator2.next();
					if (personel.getId() == personelIzin.getIzinSahibi().getId()) {
						izin = personelIzin;
						iterator2.remove();
						break;
					}

				}
				boolean yaz = !(vardiyaGun.getHareketDurum()) && simdikiZaman.getTime() < bitZaman2;

				if (vardiyaGun.getVardiya().isCalisma()) {
					basZaman1 = vardiyaGun.getIslemVardiya().getVardiyaTelorans1BasZaman().getTime();
					basZaman2 = vardiyaGun.getIslemVardiya().getVardiyaTelorans2BasZaman().getTime();
					bitZaman1 = vardiyaGun.getIslemVardiya().getVardiyaTelorans1BitZaman().getTime();
					bitZaman2 = vardiyaGun.getIslemVardiya().getVardiyaTelorans2BitZaman().getTime();
					if (simdikiZaman.getTime() > basZaman2) {
						if (kgsHareketGiris != null) {
							Long kgsZaman = kgsHareketGiris.getZaman().getTime();
							if (kgsZaman > basZaman2 || kgsZaman < basZaman1)
								yaz = Boolean.TRUE;

						} else {
							boolean izinDurum = Boolean.FALSE;
							if (izin != null) {
								long izinBaslangic = izin.getBaslangicZamani().getTime();
								long izinBitis = izin.getBitisZamani().getTime();
								izinDurum = vardiyaGun.getIslemVardiya().getVardiyaBasZaman().getTime() <= izinBitis && vardiyaGun.getIslemVardiya().getVardiyaBitZaman().getTime() >= izinBaslangic;
							}
							if (!izinDurum)
								yaz = Boolean.TRUE;

						}
					}

					if (simdikiZaman.getTime() > bitZaman2) {
						if (kgsHareketCikis != null) {
							Long kgsZaman = kgsHareketCikis.getZaman().getTime();
							if (kgsZaman > bitZaman2 || kgsZaman < bitZaman1)
								yaz = Boolean.TRUE;

						} else {
							boolean izinDurum = Boolean.FALSE;
							if (izin != null) {
								long izinBaslangic = izin.getBaslangicZamani().getTime();
								long izinBitis = izin.getBitisZamani().getTime();
								izinDurum = vardiyaGun.getIslemVardiya().getVardiyaBasZaman().getTime() <= izinBitis && vardiyaGun.getIslemVardiya().getVardiyaBitZaman().getTime() >= izinBaslangic;
							}
							if (!izinDurum)
								yaz = Boolean.TRUE;
						}
					}

				}

				if (!yaz)
					iterator.remove();
			}
			ortakIslemler.otomatikHareketEkle(new ArrayList<VardiyaGun>(vardiyalarMap.values()), session);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		setVardiyaGunList(vardiyaList);

	}

	public String girisCikisExcel() {
		try {
			ByteArrayOutputStream baosDosya = girisCikisExcelDevam();
			if (baosDosya != null) {
				String dosyaAdi = "GirisCikisRaporu" + PdksUtil.convertToDateString(date, "yyyyMMdd") + ".xlsx";
				PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi);
			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return "";
	}

	private ByteArrayOutputStream girisCikisExcelDevam() {
		ByteArrayOutputStream baosDosya = null;
		try {
			Workbook wb = new XSSFWorkbook();
			Sheet sheet = ExcelUtil.createSheet(wb, "Giris Cikis Raporu" + PdksUtil.convertToDateString(date, "yyyyMMdd"), Boolean.TRUE);
			CellStyle header = ExcelUtil.getStyleHeader(wb);
			CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
			CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
			CellStyle styleOddDateTime = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATETIME, wb);
			CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
			CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
			CellStyle styleEvenDateTime = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATETIME, wb);

			int colHareket = 0, rowHareket = 0;

			ExcelUtil.getCell(sheet, rowHareket, colHareket++, header).setCellValue("Personel");
			ExcelUtil.getCell(sheet, rowHareket, colHareket++, header).setCellValue(ortakIslemler.personelNoAciklama());
			ExcelUtil.getCell(sheet, rowHareket, colHareket++, header).setCellValue(ortakIslemler.yoneticiAciklama());
			ExcelUtil.getCell(sheet, rowHareket, colHareket++, header).setCellValue(ortakIslemler.sirketAciklama());
			tesisDurum = ortakIslemler.getListTesisDurum(vardiyaGunList);
			if (tesisDurum)
				ExcelUtil.getCell(sheet, rowHareket, colHareket++, header).setCellValue(ortakIslemler.tesisAciklama());
			ExcelUtil.getCell(sheet, rowHareket, colHareket++, header).setCellValue(bolumAciklama);
			ExcelUtil.getCell(sheet, rowHareket, colHareket++, header).setCellValue("Vardiya");
			ExcelUtil.getCell(sheet, rowHareket, colHareket++, header).setCellValue("Giriş");
			ExcelUtil.getCell(sheet, rowHareket, colHareket++, header).setCellValue("Çıkış");
			boolean renk = true;
			CreationHelper factory = wb.getCreationHelper();
			Drawing drawing = sheet.createDrawingPatriarch();
			ClientAnchor anchor = factory.createClientAnchor();
			for (VardiyaGun vardiyaGun : vardiyaGunList) {
				CellStyle style = null, styleCenter = null, cellStyleDateTime = null;

				if (renk) {
					cellStyleDateTime = styleOddDateTime;
					style = styleOdd;
					styleCenter = styleOddCenter;
				} else {
					cellStyleDateTime = styleEvenDateTime;
					style = styleEven;
					styleCenter = styleEvenCenter;
				}
				renk = !renk;
				Personel personel = vardiyaGun.getPdksPersonel();
				Sirket sirket = personel != null ? personel.getSirket() : null;
				Vardiya vardiya = vardiyaGun.getVardiya();

				rowHareket++;
				colHareket = 0;
				ExcelUtil.getCell(sheet, rowHareket, colHareket++, styleCenter).setCellValue(personel.getPdksSicilNo());
				ExcelUtil.getCell(sheet, rowHareket, colHareket++, style).setCellValue(personel.getAdSoyad());
				ExcelUtil.getCell(sheet, rowHareket, colHareket++, style).setCellValue(personel.getYoneticisi() != null && personel.getYoneticisi().isCalisiyorGun(vardiyaGun.getVardiyaDate()) ? personel.getYoneticisi().getAdSoyad() : "");
				ExcelUtil.getCell(sheet, rowHareket, colHareket++, style).setCellValue(sirket.getAd());
				if (tesisDurum)
					ExcelUtil.getCell(sheet, rowHareket, colHareket++, style).setCellValue(personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
				ExcelUtil.getCell(sheet, rowHareket, colHareket++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
				ExcelUtil.getCell(sheet, rowHareket, colHareket++, styleCenter).setCellValue(vardiya.isCalisma() ? authenticatedUser.dateFormatla(vardiyaGun.getVardiyaDate()) + " " + vardiya.getAciklama() : vardiya.getAdi());
				if (vardiyaGun.getGirisHareket() != null) {
					HareketKGS hareket = vardiyaGun.getGirisHareket();
					Cell cell = ExcelUtil.getCell(sheet, rowHareket, colHareket++, cellStyleDateTime);
					cell.setCellValue(hareket.getOrjinalZaman());
					RichTextString str1 = factory.createRichTextString(hareket.getKapiView().getKapi().getAciklama());
					ExcelUtil.setCellComment(drawing, anchor, cell, str1);
				} else
					ExcelUtil.getCell(sheet, rowHareket, colHareket++, style).setCellValue("");
				if (vardiyaGun.getCikisHareket() != null) {
					HareketKGS hareket = vardiyaGun.getCikisHareket();
					Cell cell = ExcelUtil.getCell(sheet, rowHareket, colHareket++, cellStyleDateTime);
					cell.setCellValue(hareket.getOrjinalZaman());
					RichTextString str1 = factory.createRichTextString(hareket.getKapiView().getKapi().getAciklama());
					ExcelUtil.setCellComment(drawing, anchor, cell, str1);
				} else
					ExcelUtil.getCell(sheet, rowHareket, colHareket++, style).setCellValue("");
			}
			for (int i = 0; i < colHareket; i++)
				sheet.autoSizeColumn(i);

			if (!vardiyaGunList.isEmpty())
				ortakIslemler.vardiyaHareketExcel(date, vardiyaGunList, bolumAciklama, wb);

			baosDosya = new ByteArrayOutputStream();
			wb.write(baosDosya);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return baosDosya;
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

	public String getSicilNo() {
		return sicilNo;
	}

	public void setSicilNo(String sicilNo) {
		this.sicilNo = sicilNo;
	}

	public AramaSecenekleri getAramaSecenekleri() {
		return aramaSecenekleri;
	}

	public void setAramaSecenekleri(AramaSecenekleri aramaSecenekleri) {
		this.aramaSecenekleri = aramaSecenekleri;
	}

}
