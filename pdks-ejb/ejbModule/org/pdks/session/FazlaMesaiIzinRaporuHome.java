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
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
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
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.AylikPuantaj;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Liste;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.security.entity.User;

@Name("fazlaMesaiIzinRaporuHome")
public class FazlaMesaiIzinRaporuHome extends EntityHome<VardiyaGun> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7933990639559103435L;

	static Logger logger = Logger.getLogger(FazlaMesaiIzinRaporuHome.class);

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
	Date date;
	List<Personel> devamsizlikList = new ArrayList<Personel>();

	List<Personel> personelList = new ArrayList<Personel>();

	List<HareketKGS> hareketList = new ArrayList<HareketKGS>();
	private List<VardiyaGun> izinVardiyaGunList = new ArrayList<VardiyaGun>();
	private List<AylikPuantaj> puantajList = new ArrayList<AylikPuantaj>();
	private AramaSecenekleri aramaSecenekleri = null;
	private List<Vardiya> vardiyaList = new ArrayList<Vardiya>();
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private String bolumAciklama;
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
		if (aramaSecenekleri == null)
			aramaSecenekleri = new AramaSecenekleri(authenticatedUser);
		fillEkSahaTanim();
		Date dateBas = PdksUtil.buGun();
		setDate(dateBas);
		if (aramaSecenekleri.getDepartmanId() == null) {
			aramaSecenekleri.setDepartman(authenticatedUser.getDepartman());
			aramaSecenekleri.setDepartmanId(authenticatedUser.getDepartman().getId());
		}
		tesisDurum = false;
		fillSirketList();
	}

	private void clearVardiyaList() {
		izinVardiyaGunList.clear();

		puantajList.clear();
		vardiyaList.clear();
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
				if (sirket.getDurum() && sirket.getFazlaMesai()) {

					aramaSecenekleri.getSirketIdList().add(new SelectItem(sirket.getId(), sirket.getAd()));
				}

			}
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

	public String fillTesisList() {
		if (aramaSecenekleri.getTesisList() != null)
			aramaSecenekleri.getTesisList().clear();
		else
			aramaSecenekleri.setTesisList(new ArrayList<SelectItem>());
		clearVardiyaList();
		if (aramaSecenekleri.getSirketId() != null) {
			Date bugun = PdksUtil.getDate(date);
			List<Tanim> list = new ArrayList<Tanim>();
			HashMap map = new HashMap();
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
				Long tesisId = null;
				list = PdksUtil.sortObjectStringAlanList(new ArrayList<Sirket>(tesisMap.values()), "getAciklama", null);
				for (Tanim tesis : list) {
					if (tesisId == null)
						tesisId = tesis.getId();
					aramaSecenekleri.getTesisList().add(new SelectItem(tesis.getId(), tesis.getAciklama()));
				}
				aramaSecenekleri.setTesisId(tesisId);

			}
		} else
			aramaSecenekleri.setTesisId(null);

		return "";
	}

	private void fillEkSahaTanim() {
		ortakIslemler.fillEkSahaTanimAramaSecenekAta(session, Boolean.FALSE, Boolean.TRUE, aramaSecenekleri);
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
	}

	public Date getDate() {
		return date;
	}

	public void hareketGoster(VardiyaGun pdksVardiyaGun) {
		setInstance(pdksVardiyaGun);
		List<HareketKGS> kgsList = pdksVardiyaGun.getHareketler();
		setHareketList(kgsList);

	}

	public void vardiyaListeOlustur() throws Exception {
		/*
		 * yetkili oldugu Tum personellerin uzerinden dönülür,tek tarih icin cekilir. Vardiyadaki calismasi gereken saat ile hareketten calistigi saatler karsilastirilir. Eksik varsa izin var mi diye bakilir. Diyelim 4 saat eksik calisti 2 saat mazeret buldu. Hala 2 saat eksik vardir. Bunu
		 * gosteririrz. Diyelim hic mazeret girmemiş 4 saat gösteririz
		 */
		clearVardiyaList();
		session.clear();
		Calendar cal = Calendar.getInstance();
		Date oncekiGun = ortakIslemler.tariheGunEkleCikar(cal, date, -1);
		HashMap map = new HashMap();
		map.put("pdks=", Boolean.TRUE);
		map.put("durum=", Boolean.TRUE);
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
		Date basTarih = ortakIslemler.tariheGunEkleCikar(cal, date, -7);
		Date bitTarih = ortakIslemler.tariheGunEkleCikar(cal, date, 7);
		if (!tumPersoneller.isEmpty()) {
			List<Long> idler = new ArrayList<Long>();
			for (Iterator iterator = tumPersoneller.iterator(); iterator.hasNext();) {
				Personel personel = (Personel) iterator.next();
				idler.add(personel.getId());

			}
			String fieldName = "p";
			HashMap fields = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT DISTINCT P.* FROM " + VardiyaGun.TABLE_NAME + " VG WITH(nolock) ");
			sb.append(" INNER JOIN " + Vardiya.TABLE_NAME + " V ON VG." + VardiyaGun.COLUMN_NAME_VARDIYA + " = V." + Vardiya.COLUMN_NAME_ID);
			sb.append(" AND V." + Vardiya.COLUMN_NAME_VARDIYA_TIPI + " = :vt");
			sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON VG." + VardiyaGun.COLUMN_NAME_PERSONEL + " = P." + Personel.COLUMN_NAME_ID);
			sb.append("	WHERE VG." + VardiyaGun.COLUMN_NAME_PERSONEL + " :" + fieldName);
			sb.append("	AND VG." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " >=:b1 AND VG." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " <=:b2 ");
			fields.put("vt", Vardiya.TIPI_FMI);
			fields.put("b1", basTarih);
			fields.put("b2", bitTarih);
			fields.put(fieldName, idler);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			// tumPersoneller = (ArrayList<Personel>) pdksEntityController.getObjectBySQLList(sb, fields, Personel.class);
			tumPersoneller = (ArrayList<Personel>) ortakIslemler.getSQLParamList(idler, sb, fieldName, fields, Personel.class, session);

			fields.clear();
			sb = new StringBuffer();
			sb.append("SELECT DISTINCT P.* FROM " + PersonelIzin.TABLE_NAME + " I WITH(nolock) ");
			sb.append(" INNER JOIN " + IzinTipi.TABLE_NAME + " IT ON I." + PersonelIzin.COLUMN_NAME_IZIN_TIPI + " = IT." + IzinTipi.COLUMN_NAME_ID);
			sb.append(" INNER JOIN " + Tanim.TABLE_NAME + " T ON IT." + IzinTipi.COLUMN_NAME_IZIN_TIPI + " = T." + Tanim.COLUMN_NAME_ID);
			sb.append(" AND T." + Tanim.COLUMN_NAME_KODU + " = :k");
			sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON I." + PersonelIzin.COLUMN_NAME_PERSONEL + " = P." + Personel.COLUMN_NAME_ID);
			sb.append("	WHERE I." + PersonelIzin.COLUMN_NAME_PERSONEL + " :" + fieldName);
			sb.append("	AND I." + PersonelIzin.COLUMN_NAME_BITIS_ZAMANI + " >=:b1 AND I." + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + " <=:b2 ");
			fields.put("k", IzinTipi.FAZLA_MESAI);
			fields.put("b1", basTarih);
			fields.put("b2", bitTarih);
			fields.put(fieldName, idler);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			// List<Personel> tumPersonelIzinler = (ArrayList<Personel>) pdksEntityController.getObjectBySQLList(sb, fields, Personel.class);
			List<Personel> tumPersonelIzinler = (ArrayList<Personel>) ortakIslemler.getSQLParamList(idler, sb, fieldName, fields, Personel.class, session);
			for (Iterator iterator = tumPersonelIzinler.iterator(); iterator.hasNext();) {
				Personel personel = (Personel) iterator.next();
				for (Personel personel1 : tumPersoneller) {
					if (personel1.getId().equals(personel.getId())) {
						iterator.remove();
						break;
					}

				}

			}
			if (!tumPersonelIzinler.isEmpty()) {
				tumPersoneller.addAll(tumPersonelIzinler);
			}
			tumPersonelIzinler = null;
		}
		List<VardiyaGun> vardiyaGunList = new ArrayList<VardiyaGun>();

		List<HareketKGS> kgsList = new ArrayList<HareketKGS>();
		Date tarih1 = null;
		Date tarih2 = null;

		for (Iterator iterator = tumPersoneller.iterator(); iterator.hasNext();) {
			Personel pdksPersonel = (Personel) iterator.next();
			if (pdksPersonel.getPdks() == null || !pdksPersonel.getPdks())
				iterator.remove();

		}
		if (!tumPersoneller.isEmpty()) {
			TreeMap<String, VardiyaGun> vardiyaMap = ortakIslemler.getIslemVardiyalar((List<Personel>) tumPersoneller, basTarih, bitTarih, Boolean.FALSE, session, Boolean.TRUE);
			vardiyaGunList = new ArrayList<VardiyaGun>(vardiyaMap.values());
			long ot = oncekiGun.getTime(), dt = date.getTime();
			for (Iterator iterator = vardiyaGunList.iterator(); iterator.hasNext();) {
				VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator.next();
				Vardiya vardiya = pdksVardiyaGun.getIslemVardiya();
				long vt = pdksVardiyaGun.getVardiyaDate().getTime();
				if (vardiya == null || vt < ot || vt > dt) {
					iterator.remove();
					continue;

				}
			}
			// butun personeller icin hareket cekerken bu en kucuk tarih ile en
			// buyuk tarih araligini kullanacaktir
			// bu araliktaki tum hareketleri cekecektir.

			for (Iterator iterator = vardiyaGunList.iterator(); iterator.hasNext();) {
				VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator.next();
				Vardiya vardiya = pdksVardiyaGun.getIslemVardiya();
				if ((PdksUtil.tarihKarsilastirNumeric(pdksVardiyaGun.getVardiyaDate(), date) != 0 && vardiya.getBitSaat() > vardiya.getBasSaat()) || (PdksUtil.tarihKarsilastirNumeric(pdksVardiyaGun.getVardiyaDate(), date) == 0 && vardiya.getBitSaat() < vardiya.getBasSaat())) {
					iterator.remove();
					continue;

				}
				if (vardiya.isCalisma()) {
					if (tarih1 == null || pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans1BasZaman().getTime() < tarih1.getTime())
						tarih1 = pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans1BasZaman();

					if (tarih2 == null || pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans2BitZaman().getTime() > tarih2.getTime())
						tarih2 = pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans2BitZaman();
				}
			}
			if (!vardiyaGunList.isEmpty()) {

				List<Long> kapiIdler = ortakIslemler.getPdksDonemselKapiIdler(tarih1, tarih2, session);
				if (kapiIdler != null && !kapiIdler.isEmpty())
					kgsList = ortakIslemler.getPdksHareketBilgileri(Boolean.TRUE, kapiIdler, (List<Personel>) tumPersoneller.clone(), ortakIslemler.tariheGunEkleCikar(cal, tarih1, -1), ortakIslemler.tariheGunEkleCikar(cal, tarih2, 1), HareketKGS.class, session);
				else
					kgsList = new ArrayList<HareketKGS>();
				HashMap<Long, List<HareketKGS>> hMap = new HashMap<Long, List<HareketKGS>>();
				if (!kgsList.isEmpty()) {
					if (kgsList.size() > 1)
						kgsList = PdksUtil.sortListByAlanAdi(kgsList, "zaman", Boolean.FALSE);
					for (HareketKGS hareketKGS : kgsList) {
						Long key = hareketKGS.getPersonelId();
						List<HareketKGS> list = hMap.containsKey(key) ? hMap.get(key) : new ArrayList<HareketKGS>();
						if (list.isEmpty())
							hMap.put(key, list);
						list.add(hareketKGS);

					}

				}

				try {
					TreeMap<Long, Vardiya> vardiyaDataMap = new TreeMap<Long, Vardiya>();
					List<VardiyaGun> fmiList = new ArrayList<VardiyaGun>();
					for (Iterator iterator = vardiyaGunList.iterator(); iterator.hasNext();) {
						VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator.next();
						VardiyaGun vardiyaGun = (VardiyaGun) pdksVardiyaGun.clone();
						Vardiya vardiya = vardiyaGun.getIslemVardiya();

						vardiyaGun.setHareketler(null);
						vardiyaGun.setGirisHareketleri(null);
						vardiyaGun.setCikisHareketleri(null);

						vardiyaGun.setIlkGiris(null);
						vardiyaGun.setSonCikis(null);

						vardiyaDataMap.put(vardiya.getId(), vardiya);
						Long perId = vardiyaGun.getPersonel().getId();
						List<HareketKGS> list = hMap.containsKey(perId) ? hMap.get(perId) : new ArrayList<HareketKGS>();
						for (Iterator iterator1 = list.iterator(); iterator1.hasNext();) {
							HareketKGS kgsHareket = (HareketKGS) iterator1.next();
							if (vardiyaGun.addHareket(kgsHareket, Boolean.FALSE))
								iterator1.remove();
						}
						PersonelIzin izin = null;
						PersonelIzin personelIzin = vardiyaGun.getIzin();
						boolean fmi = vardiya.isFMI();
						if (personelIzin != null) {
							if (vardiya.isCalisma()) {
								izin = vardiyaGun.getIzin();
							}

						}

						if (izin != null) {
							continue;
						}

						if (fmi && (vardiyaGun.getHareketler() == null || vardiyaGun.getHareketler().isEmpty()))
							fmiList.add(vardiyaGun);
					}
					if (!fmiList.isEmpty()) {

						vardiyaGunSirala(izinVardiyaGunList);

						boolean renk = false;
						if (!fmiList.isEmpty()) {
							vardiyaGunSirala(fmiList);
							izinVardiyaGunList.addAll(fmiList);
						}
						fmiList = null;
						for (VardiyaGun vg : izinVardiyaGunList) {
							vg.setTdClass(renk ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
							renk = !renk;
						}

					}

				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());

				}
			}

		}
		tesisDurum = ortakIslemler.getListTesisDurum(izinVardiyaGunList);
	}

	/**
	 * @param list
	 */
	private void vardiyaGunSirala(List<VardiyaGun> list) {
		if (list != null && list.size() > 1) {
			TreeMap<String, List<VardiyaGun>> sirketParcalaMap = new TreeMap<String, List<VardiyaGun>>();
			List<Liste> listeler = new ArrayList<Liste>();
			for (VardiyaGun vardiyaGun : list) {
				Personel personel = vardiyaGun.getPersonel();
				String key = personel.getSirket().getAd() + "_" + (personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
				List<VardiyaGun> ozelList = sirketParcalaMap.containsKey(key) ? sirketParcalaMap.get(key) : new ArrayList<VardiyaGun>();
				if (ozelList.isEmpty()) {
					Liste liste = new Liste(key, ozelList);
					liste.setSelected(key);
					listeler.add(liste);
					sirketParcalaMap.put(key, ozelList);
				}
				ozelList.add(vardiyaGun);
			}
			sirketParcalaMap = null;
			if (listeler.size() > 1)
				listeler = PdksUtil.sortObjectStringAlanList(listeler, "getSelected", null);
			list.clear();
			for (Liste liste : listeler) {
				List<VardiyaGun> sirketSubeList = PdksUtil.sortObjectStringAlanList((List<VardiyaGun>) liste.getValue(), "getSortBolumKey", null);
				list.addAll(sirketSubeList);
			}
		}

	}

	public String vardiyaOzetExcel() {
		try {
			ByteArrayOutputStream baosDosya = vardiyaOzetExcellDevam();
			if (baosDosya != null) {
				String dosyaAdi = "FazlaMesaiIzinRaporu" + PdksUtil.convertToDateString(date, "yyyyMMdd") + ".xlsx";
				PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi);
			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return "";
	}

	private ByteArrayOutputStream vardiyaOzetExcellDevam() {
		ByteArrayOutputStream baosDosya = null;
		try {
			Workbook wb = new XSSFWorkbook();
			Sheet sheet = ExcelUtil.createSheet(wb, "FazlaMesaiIzinRaporu " + PdksUtil.convertToDateString(date, "yyyyMMdd"), Boolean.TRUE);
			CellStyle header = ExcelUtil.getStyleHeader(wb);
			XSSFCellStyle styleOdd = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
			styleOdd.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
			XSSFCellStyle styleOddCenter = (XSSFCellStyle) ExcelUtil.setAlignment((CellStyle) styleOdd.clone(), CellStyle.ALIGN_CENTER);
			XSSFCellStyle styleEven = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
			ExcelUtil.setFillForegroundColor(styleEven, 219, 248, 219);
			XSSFCellStyle styleEvenCenter = (XSSFCellStyle) ExcelUtil.setAlignment((CellStyle) styleEven.clone(), CellStyle.ALIGN_CENTER);

			int col = 0, row = 0;

			if (izinVardiyaGunList.size() > 0) {

				boolean renk = false;
				CellStyle satirStyle = null, satirCenter = null;
				List<Integer> baslikList = new ArrayList<Integer>();

				ExcelUtil.getCell(sheet, row, col, header).setCellValue("İZİN  TABLOSU");
				for (int i = 1; i < 6; i++)
					ExcelUtil.getCell(sheet, row, col + i + 1, header).setCellValue("");
				baslikList.add(row);

				++row;
				col = 0;
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı");
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Soyadı");
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
				boolean tesisDurum = ortakIslemler.getListTesisDurum(izinVardiyaGunList);
				if (tesisDurum)
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);

				for (VardiyaGun vg : izinVardiyaGunList) {
					++row;
					col = 0;
					if (renk == false) {
						satirStyle = styleOdd;
						satirCenter = styleOddCenter;
					} else {
						satirStyle = styleEven;
						satirCenter = styleEvenCenter;
					}
					col = vardiyaSatirEkle(sheet, col, row, satirStyle, satirCenter, vg, tesisDurum);
					renk = !renk;
				}

				++row;

				for (int i = 0; i < col; i++)
					sheet.autoSizeColumn(i);
				for (Integer r : baslikList) {
					try {
						sheet.addMergedRegion(ExcelUtil.getRegion((int) r, (int) 0, (int) r, (int) 5));
					} catch (Exception e) {
					}
				}
			}
			baosDosya = new ByteArrayOutputStream();
			wb.write(baosDosya);
		} catch (Exception e) {

		}
		return baosDosya;
	}

	/**
	 * @param sheet
	 * @param col
	 * @param row
	 * @param satirStyle
	 * @param satirCenter
	 * @param vg
	 * @param tesisDurum
	 * @return
	 */
	private int vardiyaSatirEkle(Sheet sheet, int col, int row, CellStyle satirStyle, CellStyle satirCenter, VardiyaGun vg, boolean tesisDurum) {
		Personel personel = vg.getPersonel();
		ExcelUtil.getCell(sheet, row, col++, satirCenter).setCellValue(personel.getPdksSicilNo());
		ExcelUtil.getCell(sheet, row, col++, satirStyle).setCellValue(personel.getAd());
		ExcelUtil.getCell(sheet, row, col++, satirStyle).setCellValue(personel.getSoyad());
		ExcelUtil.getCell(sheet, row, col++, satirStyle).setCellValue(personel.getSirket().getAd());
		if (tesisDurum)
			ExcelUtil.getCell(sheet, row, col++, satirStyle).setCellValue(personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
		ExcelUtil.getCell(sheet, row, col++, satirStyle).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");

		return col;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<Personel> getDevamsizlikList() {
		return devamsizlikList;
	}

	public void setDevamsizlikList(List<Personel> devamsizlikList) {
		this.devamsizlikList = devamsizlikList;
	}

	public List<HareketKGS> getHareketList() {
		return hareketList;
	}

	public void setHareketList(List<HareketKGS> hareketList) {
		this.hareketList = hareketList;
	}

	public List<Personel> getPersonelList() {
		return personelList;
	}

	public void setPersonelList(List<Personel> personelList) {
		this.personelList = personelList;
	}

	public List<AylikPuantaj> getPuantajList() {
		return puantajList;
	}

	public void setPuantajList(List<AylikPuantaj> puantajList) {
		this.puantajList = puantajList;
	}

	public AramaSecenekleri getAramaSecenekleri() {
		return aramaSecenekleri;
	}

	public void setAramaSecenekleri(AramaSecenekleri aramaSecenekleri) {
		this.aramaSecenekleri = aramaSecenekleri;
	}

	public List<VardiyaGun> getIzinVardiyaGunList() {
		return izinVardiyaGunList;
	}

	public void setIzinVardiyaGunList(List<VardiyaGun> izinVardiyaGunList) {
		this.izinVardiyaGunList = izinVardiyaGunList;
	}

	public void setVardiyaList(List<Vardiya> vardiyaList) {
		this.vardiyaList = vardiyaList;
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

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}

	public boolean isTesisDurum() {
		return tesisDurum;
	}

	public void setTesisDurum(boolean tesisDurum) {
		this.tesisDurum = tesisDurum;
	}
}
