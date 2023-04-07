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
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
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
import org.pdks.entity.Dosya;
import org.pdks.entity.IzinIstirahat;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.TempIzin;
import org.pdks.security.entity.User;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

import com.pdks.webservice.IzinERP;
import com.pdks.webservice.PdksSoapVeriAktar;

@Name("kullanilanIzinlerHome")
public class KullanilanIzinlerHome extends EntityHome<PersonelIzin> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1297221062917679868L;
	static Logger logger = Logger.getLogger(KullanilanIzinlerHome.class);

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

	@In(required = false)
	FacesMessages facesMessages;
	private List<PersonelIzin> personelIzinList = new ArrayList<PersonelIzin>();

	private TempIzin updateTempIzin;
	private PersonelIzin updateIzin;

	private List<String> yilList = new ArrayList<String>();
	private List<IzinERP> izinERPList = new ArrayList<IzinERP>(), izinERPReturnList;
	private TreeMap<Long, IzinIstirahat> izinIstirahatMap;
	private String islemTipi;
	private List<SelectItem> islemTipleri;
	private Dosya izinDosya = new Dosya();

	private Date basTarih, bitTarih;

	private Integer yil;

	private boolean tumIzinler = Boolean.FALSE, istenAyrilanEkle = Boolean.FALSE, servisAktarDurum, dosyaGuncellemeYetki, servisCalisti;
	private Session session;
	private AramaSecenekleri aramaSecenekleri = null;
	private List<SelectItem> izinTanimIdList;
	private Tanim izinTipiTanim;
	private Long izinTipiId;

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
		ortakIslemler.fillEkSahaTanimAramaSecenekAta(session, Boolean.FALSE, Boolean.TRUE, aramaSecenekleri);
		List<Sirket> sirketler = ortakIslemler.fillSirketList(session, null, Boolean.TRUE);

		if (aramaSecenekleri.getSirketIdList() == null)
			aramaSecenekleri.setSirketIdList(new ArrayList<SelectItem>());
		else
			aramaSecenekleri.getSirketIdList().clear();
		for (Sirket sirket : sirketler) {
			if (sirket.getFazlaMesai() && sirket.getPdks())
				aramaSecenekleri.getSirketIdList().add(new SelectItem(sirket.getId(), sirket.getAd()));
		}
		if (aramaSecenekleri.getSirketIdList().size() == 1)
			aramaSecenekleri.setSirketId((Long) aramaSecenekleri.getSirketIdList().get(0).getValue());
	}

	public String excelAktar() {
		try {
			ByteArrayOutputStream baosDosya = excelDevam();
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, "tumKullanilanIzinler.xlsx");

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return "";
	}

	public ByteArrayOutputStream excelDevam() throws Exception {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, "Izinler", Boolean.TRUE);
		CellStyle style = ExcelUtil.getStyleData(wb);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle timeStamp = ExcelUtil.getCellStyleTimeStamp(wb);
		int row = 0;
		int col = 0;
		HashMap<String, Boolean> map = ortakIslemler.getListEkSahaDurumMap(personelIzinList, null);
		boolean ekSaha1 = (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()) && aramaSecenekleri.getEkSahaTanimMap().containsKey("ekSaha1") && map.containsKey("ekSaha1");
		boolean ekSaha2 = (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()) && aramaSecenekleri.getEkSahaTanimMap().containsKey("ekSaha2") && map.containsKey("ekSaha2");
		boolean ekSaha3 = (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()) && aramaSecenekleri.getEkSahaTanimMap().containsKey("ekSaha3") && map.containsKey("ekSaha3");
		boolean ekSaha4 = (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()) && aramaSecenekleri.getEkSahaTanimMap().containsKey("ekSaha4") && map.containsKey("ekSaha4");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		if (ekSaha1)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha1").getAciklama());
		if (ekSaha2)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha2").getAciklama());
		if (ekSaha3)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha3").getAciklama());
		if (ekSaha4)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha4").getAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İzin Tipi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İzin Başlangıç Zamanı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İşe Başlama Zamanı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Süresi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Açıklama");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Son İşlem Yapan");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Son İşlem Tarihi");
		if (islemTipi != null && islemTipi.equals("B"))
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Onay Son Durum");
		for (Iterator iter = personelIzinList.iterator(); iter.hasNext();) {
			PersonelIzin izin = (PersonelIzin) iter.next();
			row++;
			col = 0;
			Personel personel = izin.getIzinSahibi();
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getSicilNo());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
			String sirket = "";
			try {
				sirket = personel.getSirket().getAd();
			} catch (Exception e1) {
				sirket = "" + ortakIslemler.sirketAciklama() + " tanımsız";
			}
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(sirket);
			if (ekSaha1)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha1() != null ? personel.getEkSaha1().getAciklama() : "");
			if (ekSaha2)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha2() != null ? personel.getEkSaha2().getAciklama() : "");
			if (ekSaha3)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
			if (ekSaha4)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha4() != null ? personel.getEkSaha4().getAciklama() : "");

			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(izin.getIzinTipiAciklama());
			ExcelUtil.getCell(sheet, row, col++, timeStamp).setCellValue(izin.getBaslangicZamani());
			ExcelUtil.getCell(sheet, row, col++, timeStamp).setCellValue(izin.getBitisZamani());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(izin.getSureAciklama());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(izin.getAciklama());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(izin.getSonIslemYapan().getAdSoyad());
			ExcelUtil.getCell(sheet, row, col++, timeStamp).setCellValue(izin.getSonIslemTarihi());
			if (islemTipi != null && islemTipi.equals("B"))
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(izin.getIzinDurumuAciklama(ortakIslemler, session));
		}

		for (int i = 0; i < col; i++)

			sheet.autoSizeColumn(i);
		try {
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

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		if (authenticatedUser.isAdmin() == false || aramaSecenekleri == null)
			aramaSecenekleri = new AramaSecenekleri(authenticatedUser);
		aramaSecenekleri.setStajyerOlmayanSirket(Boolean.FALSE);
		setBitTarih(PdksUtil.tariheAyEkleCikar(new Date(), 2));
		Calendar cal = Calendar.getInstance();
		setTumIzinler(Boolean.FALSE);
		cal.add(Calendar.YEAR, -1);
		setBasTarih(cal.getTime());
		fillEkSahaTanim();
		fillIzinTanimList();
		if (islemTipleri == null) {
			islemTipleri = new ArrayList<SelectItem>();
			islemTipleri.add(new SelectItem("K", "Onaylanan izinler"));
			if (authenticatedUser.isIzinGirebilir())
				islemTipleri.add(new SelectItem("B", "Onay bekleyen izinler"));
		}
		setIslemTipi((String) islemTipleri.get(0).getValue());
		if (!authenticatedUser.isIK() && !authenticatedUser.isAdmin()) {
			istenAyrilanEkle = Boolean.FALSE;
			aramaSecenekleri.setSirketId(authenticatedUser.getPdksPersonel().getSirket().getId());
		}
		dosyaGuncelleDurum();
		setPersonelIzinList(new ArrayList<PersonelIzin>());
	}

	public void fillIzinTanimList() {
		HashMap map = new HashMap();
		map.put(PdksEntityController.MAP_KEY_MAP, "getKodu");
		map.put(PdksEntityController.MAP_KEY_SELECT, "izinTipiTanim");
		map.put("bakiyeIzinTipi=", null);
		// map.put("izinTipiTanim.kodu<>", IzinTipi.SSK_ISTIRAHAT);
		// map.put("personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
		if (!authenticatedUser.isAdmin()) {
			map.put("departman=", authenticatedUser.getDepartman());
			if (!authenticatedUser.isIK() && !authenticatedUser.getPdksPersonel().isOnaysizIzinKullanir())
				map.put("onaylayanTipi<>", IzinTipi.ONAYLAYAN_TIPI_YOK);

		}
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		TreeMap<String, Tanim> izinTipiTanimMap = pdksEntityController.getObjectByInnerObjectMapInLogic(map, IzinTipi.class, Boolean.FALSE);
		List<Tanim> list = new ArrayList<Tanim>(izinTipiTanimMap.values());
		if (list.size() > 1)
			list = PdksUtil.sortObjectStringAlanList(list, "getAciklama", null);
		if (izinTanimIdList == null)
			izinTanimIdList = new ArrayList<SelectItem>();
		else
			izinTanimIdList.clear();
		for (Tanim tanim : list) {
			izinTanimIdList.add(new SelectItem(tanim.getId(), tanim.getAciklama()));
		}

	}

	public void fillIzinList() {
		setInstance(null);
		servisAktarDurum = Boolean.FALSE;
		List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
		// ArrayList<String> sicilNoList = ortakIslemler.getPersonelSicilNo(ad, soyad, sicilNo, sirket, seciliEkSaha1, seciliEkSaha2, seciliEkSaha3, seciliEkSaha4, Boolean.TRUE, istenAyrilanEkle, session);
		ArrayList<String> sicilNoList = ortakIslemler.getAramaPersonelSicilNo(aramaSecenekleri, Boolean.TRUE, istenAyrilanEkle, session);
		if (sicilNoList != null && !sicilNoList.isEmpty()) {
			HashMap parametreMap = new HashMap();
			parametreMap.put("pdksSicilNo", sicilNoList);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Personel> personeller = pdksEntityController.getObjectByInnerObjectList(parametreMap, Personel.class);
			parametreMap.clear();
			parametreMap.put("id", aramaSecenekleri.getSirketId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			Sirket sirket = (Sirket) pdksEntityController.getObjectByInnerObject(parametreMap, Sirket.class);
			parametreMap.clear();

			if (izinTipiTanim != null) {
				if (sirket != null)
					parametreMap.put("departman.id=", sirket.getDepartman().getId());
				else if (!authenticatedUser.isAdmin())
					parametreMap.put("departman.id=", authenticatedUser.getDepartman().getId());

			}
			parametreMap.clear();
			boolean kontrolEt = Boolean.FALSE;
			if (personeller.size() < PdksEntityController.LIST_MAX_SIZE)
				parametreMap.put("izinSahibi", personeller);
			else
				kontrolEt = Boolean.TRUE;
			parametreMap.put("izinTipi.bakiyeIzinTipi=", null);
			if (!tumIzinler) {
				if (izinTipiId != null)
					parametreMap.put("izinTipi.izinTipiTanim.id=", izinTipiId);
				else if (izinTipiTanim != null) {
					parametreMap.put("izinTipi.izinTipiTanim.id=", izinTipiTanim.getId());
				}
			}
			Date bitisTarihi = (Date) bitTarih.clone();
			if (PdksUtil.tarihKarsilastirNumeric(bitTarih, basTarih) == 0)
				bitisTarihi = PdksUtil.tariheGunEkleCikar(bitisTarihi, 1);
			parametreMap.put("baslangicZamani<=", bitisTarihi);
			parametreMap.put("bitisZamani>=", basTarih);
			List<Integer> izinDurumuList = new ArrayList<Integer>();
			if (islemTipi.equals("K")) {
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_SAP_GONDERILDI);
			} else if (islemTipi.equals("B")) {
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA);
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_IKINCI_YONETICI_ONAYINDA);
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_IK_ONAYINDA);

			}

			parametreMap.put("izinDurumu", izinDurumuList);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			if (!izinDurumuList.isEmpty())
				izinList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, PersonelIzin.class);
			if (kontrolEt) {
				TreeMap<Long, Long> perMap = new TreeMap<Long, Long>();
				for (Personel personel : personeller)
					perMap.put(personel.getId(), personel.getId());
				for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
					PersonelIzin personelIzin = (PersonelIzin) iterator.next();

					if (personelIzin.getIzinSahibi() == null || !perMap.containsKey(personelIzin.getIzinSahibi().getId()))
						iterator.remove();

				}

			}
			if (!izinList.isEmpty())
				izinList = PdksUtil.sortListByAlanAdi(izinList, "baslangicZamani", Boolean.TRUE);

		}
		if (islemTipi != null && islemTipi.equalsIgnoreCase("K"))
			servisAktarDurum = ortakIslemler.erpIzinDoldur(izinList, session);
		setPersonelIzinList(izinList);

	}

	public String excelIzinIstirahatList() {
		try {
			ByteArrayOutputStream baosDosya = excelAktarDevam();
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, "sskIstirahatIzinleri.xlsx");

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}
		return "";
	}

	public ByteArrayOutputStream excelAktarDevam() {

		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, "SSK Izin Rapor", Boolean.TRUE);
		CellStyle style = ExcelUtil.getStyleData(wb);
		CellStyle styleCenter = ExcelUtil.getStyleData(wb);
		styleCenter.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle timeStamp = ExcelUtil.getCellStyleDate(wb);
		int row = 0, col = 0;
		boolean ekSaha1 = false, ekSaha2 = false, ekSaha3 = false, ekSaha4 = false;
		HashMap<String, Boolean> map = ortakIslemler.getListEkSahaDurumMap(personelIzinList, null);
		if (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()) {
			ekSaha1 = map.containsKey("ekSaha1");
			ekSaha2 = map.containsKey("ekSaha2");
			ekSaha3 = map.containsKey("ekSaha3");
			ekSaha4 = map.containsKey("ekSaha4");
		}
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		if (ekSaha1)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha1").getAciklama());
		if (ekSaha2)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha2").getAciklama());
		if (ekSaha3)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha3").getAciklama());
		if (ekSaha4)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha4").getAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İzin Başlangıç Zamanı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İşe Başlama Zamanı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Süresi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Teşhis");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Rapor Kaynağı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Veren Kurum");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Veren Hekim Adı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Açıklama");

		for (PersonelIzin izin : personelIzinList) {
			Personel personel = izin.getIzinSahibi();
			++row;
			col = 0;
			IzinIstirahat istirahat = getIzinIstirahat(izin.getId());
			if (istirahat == null)
				istirahat = new IzinIstirahat();
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getPdksSicilNo());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getSirket().getAd());
			if (ekSaha1)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha1() != null ? personel.getEkSaha1().getAciklama() : "");
			if (ekSaha2)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha2() != null ? personel.getEkSaha2().getAciklama() : "");
			if (ekSaha3)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
			if (ekSaha4)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha4() != null ? personel.getEkSaha4().getAciklama() : "");
			ExcelUtil.getCell(sheet, row, col++, timeStamp).setCellValue(izin.getBaslangicZamani());
			ExcelUtil.getCell(sheet, row, col++, timeStamp).setCellValue(izin.getBitisZamani());
			String sure = "";
			try {
				sure = izin.getSureAciklama();
			} catch (Exception e) {
				sure = "";
			}
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(sure);
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(istirahat.getTeshis());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(PdksUtil.getMessageBundleMessage(istirahat.getRaporKaynagiAciklama()));
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(istirahat.getVerenKurum());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(istirahat.getVerenHekimAdi());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(izin.getAciklama());
		}

		for (int i = 0; i < col; i++)
			sheet.autoSizeColumn(i);
		try {
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

	public void fillIzinIstirahatList() {
		setInstance(null);
		setIzinIstirahatMap(new TreeMap<Long, IzinIstirahat>());

		List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
		// ArrayList<String> sicilNoList = ortakIslemler.getPersonelSicilNo(ad, soyad, sicilNo, sirket, seciliEkSaha1, seciliEkSaha2, seciliEkSaha3, seciliEkSaha4, Boolean.FALSE, session);
		List<String> sicilNoList = ortakIslemler.getAramaPersonelSicilNo(aramaSecenekleri, Boolean.TRUE, istenAyrilanEkle, session);
		if (sicilNoList != null && !sicilNoList.isEmpty()) {
			HashMap parametreMap = new HashMap();
			parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "id");

			parametreMap.put("pdksSicilNo", sicilNoList);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Long> personeller = pdksEntityController.getObjectByInnerObjectList(parametreMap, Personel.class);
			IzinTipi izinTipi = null;
			parametreMap.clear();
			parametreMap.put("id", aramaSecenekleri.getSirketId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			Sirket sirket = (Sirket) pdksEntityController.getObjectByInnerObject(parametreMap, Sirket.class);
			parametreMap.clear();
			if (sirket != null)
				parametreMap.put("departman.id=", sirket.getDepartman().getId());
			else if (!authenticatedUser.isAdmin())
				parametreMap.put("departman.id=", authenticatedUser.getDepartman().getId());

			if (parametreMap.containsKey("departman.id=")) {
				parametreMap.put("izinTipiTanim.kodu=", IzinTipi.SSK_ISTIRAHAT);
				parametreMap.put("bakiyeIzinTipi=", null);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				izinTipi = (IzinTipi) pdksEntityController.getObjectByInnerObjectInLogic(parametreMap, IzinTipi.class);
			}
			if (izinTipi != null) {
				parametreMap.clear();
				parametreMap.put("izinSahibi.id", personeller);
				if (izinTipi != null)
					parametreMap.put("izinTipi.id=", izinTipi.getId());

				Date bitisTarihi = (Date) bitTarih.clone();
				if (PdksUtil.tarihKarsilastirNumeric(bitTarih, basTarih) == 0)
					bitisTarihi = PdksUtil.tariheGunEkleCikar(bitisTarihi, 1);
				parametreMap.put("baslangicZamani<=", bitisTarihi);
				parametreMap.put("bitisZamani>=", basTarih);
				List<Integer> izinDurumuList = new ArrayList<Integer>();
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA);
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_IKINCI_YONETICI_ONAYINDA);
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_IK_ONAYINDA);
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_SAP_GONDERILDI);

				// parametreMap.put("izinDurumu", izinDurumuList);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				izinList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, PersonelIzin.class);
				if (!izinList.isEmpty()) {
					izinList = PdksUtil.sortListByAlanAdi(izinList, "baslangicZamani", Boolean.TRUE);
					List<Long> list = new ArrayList<Long>();
					for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
						PersonelIzin personelIzin = (PersonelIzin) iterator.next();
						if (izinDurumuList.contains(personelIzin.getIzinDurumu()))
							list.add(personelIzin.getId());
						else
							iterator.remove();
					}

					if (!list.isEmpty()) {
						parametreMap.clear();
						parametreMap.put(PdksEntityController.MAP_KEY_MAP, "getPersonelIzinId");
						parametreMap.put("personelIzin.id", list);
						if (session != null)
							parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						TreeMap<Long, IzinIstirahat> istirahatMap = pdksEntityController.getObjectByInnerObjectMap(parametreMap, IzinIstirahat.class, Boolean.FALSE);
						setIzinIstirahatMap(istirahatMap);
					}
					list = null;
				}

			}

		}
		setPersonelIzinList(izinList);

	}

	public String personelDosyaOkuBasla() throws Exception {
		izinERPList.clear();
		return "";

	}

	public String excelServiceAktar() {
		ortakIslemler.excelServiceAktar(personelIzinList);
		return "";
	}

	public void listenerizinDosya(UploadEvent event) throws Exception {
		servisCalisti = Boolean.FALSE;
		UploadItem item = event.getUploadItem();
		PdksUtil.getDosya(item, izinDosya);
		if (izinERPList == null)
			izinERPList = new ArrayList<IzinERP>();
		else
			izinERPList.clear();

	}

	public String listenerizinDosya() throws Exception {
		return "";
	}

	public String izinDosyaOkuBasla() throws Exception {
		izinDosyaSifirla();
		dosyaGuncelleDurum();
		return "";
	}

	public String izinDosyaSifirla() throws Exception {
		servisCalisti = Boolean.FALSE;
		izinERPList.clear();
		izinDosya.setDosyaIcerik(null);
		if (izinERPReturnList == null)
			izinERPReturnList = new ArrayList<IzinERP>();
		else
			izinERPReturnList.clear();

		return "";
	}

	public String izinDosyaOku() throws Exception {
		servisCalisti = Boolean.FALSE;
		if (izinERPList == null)
			izinERPList = new ArrayList<IzinERP>();
		else
			izinERPList.clear();
		Workbook wb = ortakIslemler.getWorkbook(izinDosya);
		if (wb != null) {
			Sheet sheet = wb.getSheetAt(0);
			int col = 0;
			for (int row = 1; row <= sheet.getLastRowNum(); row++) {
				col = 0;
				IzinERP izinERP = new IzinERP();
				try {
					izinERP.setAciklama(ExcelUtil.getSheetStringValueTry(sheet, row, col++));
					izinERP.setBasZaman(ExcelUtil.getSheetStringValueTry(sheet, row, col++));
					izinERP.setBitZaman(ExcelUtil.getSheetStringValueTry(sheet, row, col++));
					izinERP.setDurum(new Boolean(ExcelUtil.getSheetStringValueTry(sheet, row, col++)));
					izinERP.setIzinSuresi(ExcelUtil.getSheetDoubleValue(sheet, row, col++));
					izinERP.setIzinTipi(ExcelUtil.getSheetStringValueTry(sheet, row, col++));
					izinERP.setIzinTipiAciklama(ExcelUtil.getSheetStringValueTry(sheet, row, col++));
					izinERP.setPersonelNo(ExcelUtil.getSheetStringValueTry(sheet, row, col++));
					izinERP.setReferansNoERP(ExcelUtil.getSheetStringValueTry(sheet, row, col++));
					izinERP.setSureBirimi(ExcelUtil.getSheetStringValueTry(sheet, row, col++));
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}
				if (col > 0)
					izinERPList.add(izinERP);
			}
		}
		return "";
	}

	public String izinDosyaYaz() throws Exception {
		TreeMap<String, IzinERP> izinMap = new TreeMap<String, IzinERP>();
		for (IzinERP izinERP : izinERPList) {
			izinMap.put(izinERP.getReferansNoERP(), izinERP);
			izinERP.setYazildi(null);
		}
		servisCalisti = Boolean.FALSE;
		try {
			izinERPReturnList = null;
			PdksSoapVeriAktar service = ortakIslemler.getPdksSoapVeriAktar();
			izinERPReturnList = service.saveIzinler(izinERPList);
			if (izinERPReturnList != null) {
				izinERPList.clear();
				for (Iterator iterator = izinERPReturnList.iterator(); iterator.hasNext();) {
					IzinERP returnERP = (IzinERP) iterator.next();
					if (izinMap.containsKey(returnERP.getReferansNoERP())) {
						IzinERP izinERP = izinMap.get(returnERP.getReferansNoERP());
						izinERP.setYazildi(returnERP.getYazildi());
						if (returnERP.getYazildi()) {
							izinERP.setId(returnERP.getId());
							izinERPList.add(izinERP);
							iterator.remove();
						} else {
							izinERP.getHataList().addAll(returnERP.getHataList());
						}
					} else
						returnERP.getHataList().add("İşlem yapılmadı!");
				}
				if (!izinERPReturnList.isEmpty()) {
					izinERPList.clear();
					PdksUtil.addMessageWarn("Hata oluştu!");
				} else
					PdksUtil.addMessageInfo("Güncelleme  başarılı tamamlandı.");

			}

			servisCalisti = Boolean.TRUE;
		} catch (Exception e) {
			izinERPReturnList = null;
			logger.error(e);
		}

		return "";

	}

	/**
	 * 
	 */
	private void dosyaGuncelleDurum() {
		dosyaGuncellemeYetki = ortakIslemler.getTestDurum() && (authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || authenticatedUser.isIK());
		if (dosyaGuncellemeYetki) {
			if (authenticatedUser.isIK()) {
				String dosyaGuncellemeYetkiStr = ortakIslemler.getParameterKey("dosyaGuncellemeYetki");
				dosyaGuncellemeYetki = dosyaGuncellemeYetkiStr.equals("1");
			}

		}
	}

	public IzinIstirahat getIzinIstirahat(Long personelIzinId) {
		IzinIstirahat izinIstirahat = null;
		if (izinIstirahatMap != null && izinIstirahatMap.containsKey(personelIzinId)) {
			izinIstirahat = izinIstirahatMap.get(personelIzinId);
			izinIstirahat.aciklamaAta();
		}

		return izinIstirahat;
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

	public List<String> getYilList() {
		return yilList;
	}

	public void setYilList(List<String> yilList) {
		this.yilList = yilList;
	}

	public Integer getYil() {
		return yil;
	}

	public void setYil(Integer yil) {
		this.yil = yil;
	}

	public TempIzin getUpdateTempIzin() {
		return updateTempIzin;
	}

	public void setUpdateTempIzin(TempIzin updateTempIzin) {
		this.updateTempIzin = updateTempIzin;
	}

	public PersonelIzin getUpdateIzin() {
		return updateIzin;
	}

	public void setUpdateIzin(PersonelIzin updateIzin) {
		this.updateIzin = updateIzin;
	}

	public Tanim getIzinTipiTanim() {
		return izinTipiTanim;
	}

	public void setIzinTipiTanim(Tanim izinTipiTanim) {
		this.izinTipiTanim = izinTipiTanim;
	}

	public List<PersonelIzin> getPersonelIzinList() {
		return personelIzinList;
	}

	public void setPersonelIzinList(List<PersonelIzin> personelIzinList) {
		this.personelIzinList = personelIzinList;
	}

	public TreeMap<Long, IzinIstirahat> getIzinIstirahatMap() {
		return izinIstirahatMap;
	}

	public void setIzinIstirahatMap(TreeMap<Long, IzinIstirahat> izinIstirahatMap) {
		this.izinIstirahatMap = izinIstirahatMap;
	}

	public boolean isTumIzinler() {
		return tumIzinler;
	}

	public void setTumIzinler(boolean tumIzinler) {
		this.tumIzinler = tumIzinler;
	}

	public String getIslemTipi() {
		return islemTipi;
	}

	public void setIslemTipi(String islemTipi) {
		this.islemTipi = islemTipi;
	}

	public List<SelectItem> getIslemTipleri() {
		return islemTipleri;
	}

	public void setIslemTipleri(List<SelectItem> islemTipleri) {
		this.islemTipleri = islemTipleri;
	}

	public boolean isIstenAyrilanEkle() {
		return istenAyrilanEkle;
	}

	public void setIstenAyrilanEkle(boolean istenAyrilanEkle) {
		this.istenAyrilanEkle = istenAyrilanEkle;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public AramaSecenekleri getAramaSecenekleri() {
		return aramaSecenekleri;
	}

	public void setAramaSecenekleri(AramaSecenekleri aramaSecenekleri) {
		this.aramaSecenekleri = aramaSecenekleri;
	}

	public List<SelectItem> getIzinTanimIdList() {
		return izinTanimIdList;
	}

	public void setIzinTanimIdList(List<SelectItem> izinTanimIdList) {
		this.izinTanimIdList = izinTanimIdList;
	}

	public Long getIzinTipiId() {
		return izinTipiId;
	}

	public void setIzinTipiId(Long izinTipiId) {
		this.izinTipiId = izinTipiId;
	}

	public boolean isServisAktarDurum() {
		return servisAktarDurum;
	}

	public void setServisAktarDurum(boolean servisAktarDurum) {
		this.servisAktarDurum = servisAktarDurum;
	}

	public boolean isDosyaGuncellemeYetki() {
		return dosyaGuncellemeYetki;
	}

	public void setDosyaGuncellemeYetki(boolean dosyaGuncellemeYetki) {
		this.dosyaGuncellemeYetki = dosyaGuncellemeYetki;
	}

	public List<IzinERP> getIzinERPList() {
		return izinERPList;
	}

	public void setIzinERPList(List<IzinERP> izinERPList) {
		this.izinERPList = izinERPList;
	}

	public Dosya getIzinDosya() {
		return izinDosya;
	}

	public void setIzinDosya(Dosya izinDosya) {
		this.izinDosya = izinDosya;
	}

	public boolean isServisCalisti() {
		return servisCalisti;
	}

	public void setServisCalisti(boolean servisCalisti) {
		this.servisCalisti = servisCalisti;
	}

	public List<IzinERP> getIzinERPReturnList() {
		return izinERPReturnList;
	}

	public void setIzinERPReturnList(List<IzinERP> izinERPReturnList) {
		this.izinERPReturnList = izinERPReturnList;
	}

}
