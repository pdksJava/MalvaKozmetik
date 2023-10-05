package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.Query;

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
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.PersonelIzinDetay;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.TempIzin;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;

@Name("bakiyeIzinHome")
public class BakiyeIzinHome extends EntityHome<PersonelIzin> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8279777770831737897L;
	static Logger logger = Logger.getLogger(BakiyeIzinHome.class);

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

	private PersonelIzin updateIzin;
	private TempIzin updateTempIzin;

	private List<PersonelIzin> personelizinList = new ArrayList<PersonelIzin>();
	private List<TempIzin> pdksPersonelList = new ArrayList<TempIzin>();

	private Personel personel;

	private List<String> yilList = new ArrayList<String>();

	private Date basTarih, bitTarih, tarih;
	private Integer yil;
	private Long izinTipiId;
	private Double bakiyeSuresi, izinSuresi;
	private List<PersonelIzinDetay> harcananIzinler;
	private Session session;
	private AramaSecenekleri aramaSecenekleri = null;
	private List<SelectItem> izinTanimIdList;
	private Tanim izinTipiTanim;

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
		ortakIslemler.fillEkSahaTanimAramaSecenekAta(session, Boolean.FALSE, null, aramaSecenekleri);
		if (aramaSecenekleri.getSirketList() != null) {
			List<SelectItem> sirketIdList = ortakIslemler.getIzinSirketItemList(aramaSecenekleri.getSirketList());
			aramaSecenekleri.setSirketIdList(sirketIdList);
			if (aramaSecenekleri.getSirketIdList().size() == 1)
				aramaSecenekleri.setSirketId((Long) aramaSecenekleri.getSirketIdList().get(0).getValue());
		}
	}

	public String guncelle(PersonelIzin izin) {
		setBakiyeSuresi(izin.getKalanIzin());
		setIzinSuresi(izin.getIzinSuresi());
		setUpdateIzin((PersonelIzin) izin.clone());
		return "";

	}

	@Transactional
	public String sil(PersonelIzin izin) {
		String durum = "persist";
		try {
			ortakIslemler.bakiyeIzinSil(izin, session);
			session.flush();
			fillIzinList();
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			durum = "";
			PdksUtil.addMessageError(e.getMessage());
		}
		return durum;
	}

	@Transactional
	public String kaydet() {
		String durum = "persist";
		try {
			if (getBakiyeSuresi() < 0 || getBakiyeSuresi() > getIzinSuresi())
				throw new Exception("Bakiye süresi en az 0 en fazla " + getIzinSuresi() + " olabilir.");
			double harcananSure = updateIzin.getHarcananIzin() - (updateIzin.getKullanilanIzinSuresi() != null ? updateIzin.getKullanilanIzinSuresi() : 0);
			double kullanilanIzinSuresi = getIzinSuresi() - getBakiyeSuresi() - harcananSure;
			Query query = null;
			String queryStr = "UPDATE PersonelIzin SET guncelleyenUser_id=?,guncellemeTarihi=?, KULLANILAN_IZIN_SURESI=? where id=?";
			query = entityManager.createNativeQuery(queryStr);
			query.setParameter(1, authenticatedUser.getId());
			query.setParameter(2, new Date());
			query.setParameter(3, kullanilanIzinSuresi);
			query.setParameter(4, updateIzin.getId());
			query.executeUpdate();
			HashMap parametreMap = new HashMap();

			parametreMap.put("id", updateIzin.getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			PersonelIzin izin = (PersonelIzin) pdksEntityController.getObjectByInnerObject(parametreMap, PersonelIzin.class);
			session.refresh(izin);
			entityManager.flush();
			fillIzinList();

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			durum = "";
			PdksUtil.addMessageError(e.getMessage());

		}
		return durum;

	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() {

		session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		HashMap fields = new HashMap();
		fields.put("durum=", Boolean.TRUE);
		fields.put("bakiyeIzinTipi.izinTipiTanim.kodu<>", IzinTipi.YILLIK_UCRETLI_IZIN);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<IzinTipi> list = pdksEntityController.getObjectByInnerObjectListInLogic(fields, IzinTipi.class);
		String pos = "";
		if (list != null && !list.isEmpty()) {
			if (authenticatedUser.isAdmin() == false || aramaSecenekleri == null)
				aramaSecenekleri = new AramaSecenekleri(authenticatedUser);

			aramaSecenekleri.setSirketIzinKontrolYok(Boolean.FALSE);
			aramaSecenekleri.setStajyerOlmayanSirket(Boolean.FALSE);
			setTarih(null);
			fillEkSahaTanim();
			fillTarih();
			setInstance(new PersonelIzin());
			if (authenticatedUser.isIK() || authenticatedUser.isAdmin()) {
				setPersonelList(new ArrayList<TempIzin>());
			} else
				aramaSecenekleri.setSirketId(authenticatedUser.getPdksPersonel().getSirket().getId());
			fillIzinTanimList();
			setPersonelList(new ArrayList<TempIzin>());
		} else {
			PdksUtil.addMessageWarn("Bakiye izin takibi yapılmamaktadır!");
			pos = MenuItemConstant.home;
		}

		return pos;
	}

	public void fillIzinTanimList() {
		List<String> devirTipiList = new ArrayList<String>();
		devirTipiList.add(IzinTipi.BAKIYE_DEVIR_SENELIK);
		// devirTipiList.add(IzinTipi.BAKIYE_DEVIR_DEVAM_EDER);
		HashMap map = new HashMap();
		map.put(PdksEntityController.MAP_KEY_MAP, "getKodu");
		map.put(PdksEntityController.MAP_KEY_SELECT, "bakiyeIzinTipi.izinTipiTanim");
		map.put("bakiyeIzinTipi.durum=", Boolean.TRUE);
		map.put("bakiyeIzinTipi.bakiyeDevirTipi", devirTipiList);
		map.put("bakiyeIzinTipi.personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		boolean hekim = authenticatedUser.isIK() || authenticatedUser.isAdmin() || authenticatedUser.getPdksPersonel().isHekim();
		boolean sua = authenticatedUser.isIK() || authenticatedUser.isAdmin() || authenticatedUser.getPdksPersonel().isSuaOlur();
		if (!authenticatedUser.isAdmin()) {
			map.put("departman=", authenticatedUser.getDepartman());
			if (!authenticatedUser.isIK() && !authenticatedUser.getPdksPersonel().isOnaysizIzinKullanir())
				map.put("bakiyeIzinTipi.onaylayanTipi<>", IzinTipi.ONAYLAYAN_TIPI_YOK);

		}
		List<String> haricKodlar = new ArrayList<String>();
		haricKodlar.add(IzinTipi.FAZLA_MESAI);
		if (!sua)
			haricKodlar.add(IzinTipi.SUA_IZNI);
		if (!hekim) {
			haricKodlar.add(IzinTipi.YURT_DISI_KONGRE);
			haricKodlar.add(IzinTipi.YURT_ICI_KONGRE);
			haricKodlar.add(IzinTipi.MOLA_IZNI);

		}
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		TreeMap<String, Tanim> izinTipiTanimMap = pdksEntityController.getObjectByInnerObjectMapInLogic(map, IzinTipi.class, Boolean.FALSE);
		List<Tanim> list = new ArrayList<Tanim>(izinTipiTanimMap.values());
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Tanim tanim = (Tanim) iterator.next();
			if (haricKodlar.contains(tanim.getKodu()))
				iterator.remove();

		}
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

	public void fillTarih() {

		Calendar cal = Calendar.getInstance();
		setYil(cal.get(Calendar.YEAR));
		int basYil = cal.get(Calendar.YEAR) - 5;
		if (basYil < PdksUtil.getSistemBaslangicYili())
			basYil = PdksUtil.getSistemBaslangicYili();
		List<String> yilListesi = PdksUtil.getSayilar(basYil, cal.get(Calendar.YEAR) + 1, 1);
		if (!yilListesi.isEmpty())
			yil = new Integer(yilListesi.get(yilListesi.size() - 1));
		setYilList(yilListesi);

	}

	/**
	 * 
	 */
	public void fillIzinList() {
		setInstance(null);

		HashMap<Long, TempIzin> izinMap = new HashMap<Long, TempIzin>();
		String sicilNo = aramaSecenekleri.getSicilNo();
		if (sicilNo.trim().equals("") && aramaSecenekleri.getSirketId() == null) {
			PdksUtil.addMessageWarn("" + ortakIslemler.sirketAciklama() + " seçiniz!");
		} else {
			HashMap fields = new HashMap();
			// ArrayList<String> sicilNoList = ortakIslemler.getPersonelSicilNo(ad, soyad, sicilNo, sirket, seciliEkSaha1, seciliEkSaha2, seciliEkSaha3, seciliEkSaha4, Boolean.TRUE, session);
			ArrayList<String> sicilNoList = ortakIslemler.getAramaPersonelSicilNo(aramaSecenekleri, Boolean.TRUE, session);
			if (izinTipiId != null) {
				fields.put("id", izinTipiId);
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				izinTipiTanim = (Tanim) pdksEntityController.getObjectByInnerObject(fields, Tanim.class);
			} else
				izinTipiTanim = null;

			String izinTipiKodu = izinTipiTanim != null ? izinTipiTanim.getKodu() : null;
			if (sicilNoList != null && !sicilNoList.isEmpty() && izinTipiKodu != null) {
				fields.clear();
				fields.put("id", aramaSecenekleri.getSirketId());
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				Sirket sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);
				izinMap = ortakIslemler.bakiyeIzinListesiOlustur(izinTipiKodu, sicilNoList, sirket, tarih, yil, Boolean.TRUE, session);
			}

		}
		List<TempIzin> personelList = new ArrayList<TempIzin>(izinMap.values());
		setPersonelList(personelList);
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private ByteArrayOutputStream excelDevam() {

		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		String izinAciklama = izinTipiTanim.getAciklama();
		String adi = PdksUtil.setTurkishStr("Izin Listesi");
		Sheet sheet = ExcelUtil.createSheet(wb, adi.length() <= 30 ? adi : adi.substring(0, 30), false);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddTutar = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenTutar = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TUTAR, wb);
		int row = 0;
		int col = 0;
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		boolean[] ekSahalar = authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin() ? new boolean[4] : null;

		if (ekSahalar != null) {
			for (int i = 1; i <= 4; i++) {
				boolean aciklamaVar = aramaSecenekleri.getEkSahaTanimMap().containsKey("ekSaha" + i);
				if (aciklamaVar) {
					ekSahalar[i - 1] = aciklamaVar;
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha" + i).getAciklama());
				}
			}
		}
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Yıl");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İzin Tipi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İzin Toplamı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Kullanılan İzin Toplamı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Kalan İzin");
		boolean renk = true;
		for (TempIzin tempIzin : pdksPersonelList) {
			row++;
			Personel personel = tempIzin.getPersonel();
			col = 0;
			CellStyle style = null, styleCenter = null, styleTutar = null;
			if (renk) {
				styleTutar = styleOddTutar;
				style = styleOdd;
				styleCenter = styleOddCenter;
			} else {
				styleTutar = styleEvenTutar;
				style = styleEven;
				styleCenter = styleEvenCenter;
			}
			renk = !renk;
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getSirket().getAd());
			if (ekSahalar != null) {
				for (int i = 1; i <= 4; i++) {
					if (ekSahalar[i - 1]) {
						Tanim ekSaha = null;
						try {
							ekSaha = (Tanim) PdksUtil.getMethodObject(personel, "getEkSaha" + i, null);
						} catch (Exception e) {
							ekSaha = null;
						}
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue(ekSaha != null ? ekSaha.getAciklama() : "");
					}
				}
			}
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getSicilNo());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(yil);
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(izinAciklama);
			ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(tempIzin.getToplamBakiyeIzin());
			ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(tempIzin.getKullanilanIzin());
			ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(tempIzin.getToplamKalanIzin());
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

	public String excelAktar() {
		try {
			ByteArrayOutputStream baosDosya = excelDevam();
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, "digerIzinListesi.xlsx");

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return null;
	}

	public void izinGoster(TempIzin tempIzin) {
		setUpdateTempIzin(tempIzin);
		guncelle(tempIzin.getPersonelIzin());
		HashMap parametreMap = new HashMap();
		if (!tempIzin.getIzinler().isEmpty())
			parametreMap.put("id", tempIzin.getIzinler().clone());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PersonelIzin> izinList1 = tempIzin.getIzinler().isEmpty() ? new ArrayList<PersonelIzin>() : pdksEntityController.getObjectByInnerObjectList(parametreMap, PersonelIzin.class);

		if (izinList1.size() > 1)
			izinList1 = PdksUtil.sortListByAlanAdi(izinList1, "id", Boolean.FALSE);
		setPersonelizinList(izinList1);
	}

	public List<PersonelIzin> getPersonelizinList() {
		return personelizinList;
	}

	public void setPersonelizinList(List<PersonelIzin> personelizinList) {
		this.personelizinList = personelizinList;
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

	public PersonelIzin getUpdateIzin() {
		return updateIzin;
	}

	public void setUpdateIzin(PersonelIzin updateIzin) {
		this.updateIzin = updateIzin;
	}

	public Double getBakiyeSuresi() {
		return bakiyeSuresi;
	}

	public void setBakiyeSuresi(Double bakiyeSuresi) {
		this.bakiyeSuresi = bakiyeSuresi;
	}

	public List<TempIzin> getPersonelList() {
		return pdksPersonelList;
	}

	public void setPersonelList(List<TempIzin> pdksPersonelList) {
		this.pdksPersonelList = pdksPersonelList;
	}

	public TempIzin getUpdateTempIzin() {
		return updateTempIzin;
	}

	public void setUpdateTempIzin(TempIzin updateTempIzin) {
		this.updateTempIzin = updateTempIzin;
	}

	public Personel getPersonel() {
		return personel;
	}

	public void setPersonel(Personel personel) {
		this.personel = personel;
	}

	public Double getIzinSuresi() {
		return izinSuresi;
	}

	public void setIzinSuresi(Double izinSuresi) {
		this.izinSuresi = izinSuresi;
	}

	public Date getTarih() {
		return tarih;
	}

	public void setTarih(Date tarih) {
		this.tarih = tarih;
	}

	public List<PersonelIzinDetay> getHarcananIzinler() {
		return harcananIzinler;
	}

	public void setHarcananIzinler(List<PersonelIzinDetay> harcananIzinler) {
		this.harcananIzinler = harcananIzinler;
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

	public Long getIzinTipiId() {
		return izinTipiId;
	}

	public void setIzinTipiId(Long izinTipiId) {
		this.izinTipiId = izinTipiId;
	}

	public List<SelectItem> getIzinTanimIdList() {
		return izinTanimIdList;
	}

	public void setIzinTanimIdList(List<SelectItem> izinTanimIdList) {
		this.izinTanimIdList = izinTanimIdList;
	}

	public Tanim getIzinTipiTanim() {
		return izinTipiTanim;
	}

	public void setIzinTipiTanim(Tanim izinTipiTanim) {
		this.izinTipiTanim = izinTipiTanim;
	}
}
