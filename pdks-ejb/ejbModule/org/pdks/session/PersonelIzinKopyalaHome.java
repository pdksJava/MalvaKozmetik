package org.pdks.session;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.pdks.entity.Personel;
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.Dosya;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.PersonelIzinDetay;
import org.pdks.entity.PersonelIzinOnay;
import org.pdks.entity.TempIzin;
import org.pdks.quartz.IzinBakiyeGuncelleme;
import org.pdks.security.entity.User;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

@Name("personelIzinKopyalaHome")
public class PersonelIzinKopyalaHome extends EntityHome<PersonelIzin> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4183382367447830720L;
	static Logger logger = Logger.getLogger(PersonelIzinKopyalaHome.class);

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
	@In(required = false, create = true)
	IzinBakiyeGuncelleme izinBakiyeGuncelleme;

	private Dosya izinBakiyeDosya = new Dosya();
	@In(required = false)
	FacesMessages facesMessages;

	private Session session;
	private List<TempIzin> personelBakiyeIzinList = new ArrayList<TempIzin>();
	private StringBuffer sbAll = new StringBuffer();
	private SimpleDateFormat sdf;
	private Boolean checkBoxDurum;

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
		sayfaGiris(session);

	}

	/**
	 * @param oncekiTarih
	 * @param sonrakiTarih
	 * @return
	 */
	public boolean tarihlerAyni(Date oncekiTarih, Date sonrakiTarih) {
		boolean ayni = oncekiTarih != null && sonrakiTarih != null;
		if (ayni) {
			if (sdf == null)
				sdf = new SimpleDateFormat("yyyyMMdd");
			String tarih1 = sdf.format(oncekiTarih), tarih2 = sdf.format(sonrakiTarih);
			ayni = tarih1.equals(tarih2);
		}
		return ayni;
	}

	private void sayfaGiris(Session session) {

		personelBakiyeIzinList.clear();
		izinBakiyeDosya.setDosyaIcerik(null);
		sbAll = null;
	}

	public String durumDegistir() {
		if (personelBakiyeIzinList != null) {
			for (TempIzin tempIzin : personelBakiyeIzinList) {
				if (tempIzin.getSecim() != null)
					tempIzin.setSecim(checkBoxDurum);
			}
		}
		return "";
	}

	/**
	 * @param event
	 * @throws Exception
	 */
	public void listenerIzinBakiyeDosya(UploadEvent event) throws Exception {
		UploadItem item = event.getUploadItem();
		PdksUtil.getDosya(item, izinBakiyeDosya);
		if (personelBakiyeIzinList == null)
			personelBakiyeIzinList = new ArrayList<TempIzin>();
		else
			personelBakiyeIzinList.clear();
		sbAll = null;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public String izinDosyaSifirla() throws Exception {
		personelBakiyeIzinList.clear();
		izinBakiyeDosya.setDosyaIcerik(null);
		sbAll = null;
		return "";
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public String izinBakiyeDosyaOku() throws Exception {
		personelBakiyeIzinList.clear();
		List<String> eskiler = new ArrayList<String>(), yeniler = new ArrayList<String>(), hatalar = new ArrayList<String>();
		Workbook wb = ortakIslemler.getWorkbook(izinBakiyeDosya);
		checkBoxDurum = null;
		try {
			if (wb != null) {

				User sistemAdminUser = ortakIslemler.getSistemAdminUser(session);
				if (sistemAdminUser == null)
					sistemAdminUser = authenticatedUser;
				Sheet sheet = wb.getSheetAt(0);
				int COL_ESKI_SICIL_NO = 1, COL_ESKI_PERSONEL = 3, COL_YENI_SICIL_NO = 2, COL_YENI_PERSONEL = 3;
				HashMap<String, String> excelMap = new HashMap<String, String>();

				String perEskiSicilNo = null, perYeniSicilNo = null;

				sbAll = new StringBuffer();
				for (int row = 1; row <= sheet.getLastRowNum(); row++) {
					StringBuffer sb = new StringBuffer();
					try {

						perEskiSicilNo = getSheetStringValue(sheet, row, COL_ESKI_SICIL_NO);
						perYeniSicilNo = getSheetStringValue(sheet, row, COL_YENI_SICIL_NO);
						int sayac = 0;
						if (perEskiSicilNo == null || perEskiSicilNo.trim().equals("")) {
							++sayac;
							sb.append("Eski");
						}

						if (perYeniSicilNo == null || perYeniSicilNo.trim().equals("")) {
							sb.append(sayac > 0 ? ", yeni" : "Yeni");
							++sayac;

						}
						if (sayac != 0) {
							sb = null;
							break;
						}

						if (sayac > 0 || perEskiSicilNo.equals(perYeniSicilNo)) {
							if (sayac < 2)
								sb.append(" boş olamaz!");
							else
								sb.append(" sicil numaraları farklı olmalıdır!");
							hatalar.add(row + ". satırda " + sb.toString());
							sb = null;
							continue;
						}

						sb = null;
						sb = new StringBuffer();
						sayac = 0;
						if (eskiler.contains(perEskiSicilNo) || yeniler.contains(perEskiSicilNo))
							++sayac;
						else
							sb.append("Eski");
						if (eskiler.contains(perYeniSicilNo) || yeniler.contains(perYeniSicilNo))
							++sayac;
						else {
							sb.append(sayac > 0 ? ", yeni" : "Yeni");
						}
						if (sayac > 0) {
							hatalar.add(row + ". satırda " + sb.toString() + " " + ortakIslemler.personelNoAciklama() + " önceki satırlarda var!");
							sb = null;
							continue;
						}
						excelMap.put(perEskiSicilNo, getSheetStringValue(sheet, row, COL_ESKI_PERSONEL));
						excelMap.put(perYeniSicilNo, getSheetStringValue(sheet, row, COL_YENI_PERSONEL));
						eskiler.add(perEskiSicilNo);
						yeniler.add(perYeniSicilNo);
					} catch (Exception e) {
						break;
					}
				}

				personelBakiyeIzinList.clear();
				if (sbAll.length() > 0)
					PdksUtil.addMessageAvailableWarn(sbAll.toString());
				else if (!eskiler.isEmpty()) {
					session.clear();
					HashMap parametreMap = new HashMap();
					List<String> tumPersonel = new ArrayList<String>();
					tumPersonel.addAll(eskiler);
					tumPersonel.addAll(yeniler);
					parametreMap.put("p", tumPersonel);
					parametreMap.put(PdksEntityController.MAP_KEY_MAP, "getPdksSicilNo");
					StringBuffer sb = new StringBuffer();
					sb.append("SELECT V.* FROM " + Personel.TABLE_NAME + " V WITH(nolock) ");
					sb.append(" WHERE V." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " :p");
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					TreeMap<String, Personel> map = pdksEntityController.getObjectBySQLMap(sb, parametreMap, Personel.class, Boolean.FALSE);
					boolean renk = false;

					for (int i = 0; i < eskiler.size(); i++) {
						perEskiSicilNo = eskiler.get(i);
						perYeniSicilNo = yeniler.get(i);
						TempIzin izin = new TempIzin();
						Personel personel = map.containsKey(perEskiSicilNo) ? map.get(perEskiSicilNo) : new Personel();
						if (personel.getId() == null) {
							personel.setPdksSicilNo(perEskiSicilNo);
							personel.setAd(excelMap.get(perEskiSicilNo));
							personel.setSoyad("");
						}
						Personel yeniPersonel = map.containsKey(perYeniSicilNo) ? map.get(perYeniSicilNo) : new Personel();
						if (yeniPersonel.getId() == null) {
							yeniPersonel.setPdksSicilNo(perYeniSicilNo);
							yeniPersonel.setAd(excelMap.get(perYeniSicilNo));
							yeniPersonel.setSoyad("");
							personel.setCheckBoxDurum(Boolean.FALSE);
						} else
							personel.setCheckBoxDurum(personel.getId() != null);
						int sayac = 0;
						if (personel.isCheckBoxDurum()) {

							boolean tarihAyni = tarihlerAyni(personel.getDogumTarihi(), yeniPersonel.getDogumTarihi());
							if (!tarihAyni) {
								++sayac;
								hatalar.add((i + 1) + ". satırda " + " " + perEskiSicilNo + " " + perYeniSicilNo + " " + ortakIslemler.personelNoAciklama() + " personellerin doğum tarihi farklı kontrol ediniz!");
							}
							tarihAyni = tarihlerAyni(personel.getIzinHakEdisTarihi(), yeniPersonel.getIzinHakEdisTarihi());
							if (!tarihAyni) {
								++sayac;
								hatalar.add((i + 1) + ". satırda " + " " + perEskiSicilNo + " " + perYeniSicilNo + " " + ortakIslemler.personelNoAciklama() + " personellerin " + ortakIslemler.kidemBasTarihiAciklama() + " farklı kontrol ediniz!");
							}
							tarihAyni = tarihlerAyni(personel.getIseBaslamaTarihi(), yeniPersonel.getIseBaslamaTarihi());
							if (tarihAyni) {
								++sayac;
								hatalar.add((i + 1) + ". satırda " + perEskiSicilNo + " " + perYeniSicilNo + " " + ortakIslemler.personelNoAciklama() + " personellerin işe başlama tarihi aynı kontrol ediniz!");
							}
							personel.setCheckBoxDurum(sayac == 0);
						}
						if (!personel.isCheckBoxDurum()) {
							izin.setStyleClass(VardiyaGun.STYLE_CLASS_HATA);
							izin.setSecim(null);
							if (sayac == 0)
								hatalar.add((i + 1) + ". satırda " + perEskiSicilNo + " " + perYeniSicilNo + " " + ortakIslemler.personelNoAciklama() + " personelleri kontrol ediniz!");
						} else {
							izin.setSecim(Boolean.FALSE);
							if (sayac == 0)
								izin.setStyleClass(renk ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
							else
								izin.setStyleClass(VardiyaGun.STYLE_CLASS_HATA);
							renk = !renk;
						}
						if (izin.getSecim() != null) {
							izin.setSecim(Boolean.FALSE);
							if (checkBoxDurum == null)
								checkBoxDurum = izin.getSecim();

						}
						izin.setPersonel(personel);
						izin.setYeniPersonel(yeniPersonel);
						personelBakiyeIzinList.add(izin);
					}
					map = null;
					tumPersonel = null;
				}
				excelMap = null;
				eskiler = null;
				yeniler = null;
				if (!hatalar.isEmpty()) {
					for (String hata : hatalar) {
						PdksUtil.addMessageAvailableWarn(hata);
						sbAll.append(hata + "\n");
					}
				}
				hatalar = null;
				if (!personelBakiyeIzinList.isEmpty()) {
					PdksUtil.addMessageAvailableInfo("Dosya okuma tamamlandı ");
				}

			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			PdksUtil.addMessageWarn(e.getMessage());
		} finally {
			// if (file1.exists())
			// file1.deleteOnExit();

		}

		return "";
	}

	/**
	 * @param event
	 * @throws Exception
	 */
	@Transactional
	public String izinKartiKopyala() throws Exception {
		List<Long> idler = new ArrayList<Long>();
		TreeMap<Long, Personel> iliskiMap = new TreeMap<Long, Personel>();
		TreeMap<Long, TempIzin> veriMap = new TreeMap<Long, TempIzin>();
		for (TempIzin izin : personelBakiyeIzinList) {
			if (izin.getSecim() == null || izin.getSecim().equals(Boolean.FALSE))
				continue;
			izin.setSecim(null);
			veriMap.put(izin.getPersonel().getId(), izin);
			iliskiMap.put(izin.getPersonel().getId(), izin.getYeniPersonel());
			idler.add(izin.getYeniPersonel().getId());
		}
		if (!iliskiMap.isEmpty()) {
			session.clear();
			List<String> tipler = Arrays.asList(new String[] { IzinTipi.YILLIK_UCRETLI_IZIN, IzinTipi.SUA_IZNI });
			HashMap fields = new HashMap();
			fields.put("durum=", Boolean.TRUE);
			fields.put("izinTipiTanim.kodu", tipler);
			fields.put("personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<IzinTipi> izinTipleri = pdksEntityController.getObjectByInnerObjectListInLogic(fields, IzinTipi.class);
			String idStr = "";
			for (Iterator iterator = izinTipleri.iterator(); iterator.hasNext();) {
				IzinTipi izinTipi = (IzinTipi) iterator.next();
				if (izinTipi.getBakiyeIzinTipi() == null)
					iterator.remove();
				else
					idStr += (idStr.length() > 0 ? "," : "") + String.valueOf(izinTipi.getId());
			}
			fields.clear();
			StringBuffer sb = new StringBuffer();
			if (!idStr.equals("") && !idler.isEmpty()) {
				try {
					User sistemAdminUser = ortakIslemler.getSistemAdminUser(session);
					if (sistemAdminUser == null)
						sistemAdminUser = authenticatedUser;

					// eskiKayitlariSil(idler);
					sb = new StringBuffer("dbo.SP_IZIN_KOPYALA");
					List params = Arrays.asList(new String[] { "izinSahibiId", "izinSahibiNewId", "sistemAdminUserId" });
					Calendar cal = Calendar.getInstance();
					long bugun = Long.parseLong(PdksUtil.convertToDateString(cal.getTime(), "yyyyMMdd"));
					int yil = cal.get(Calendar.YEAR);
					for (Iterator iterator = iliskiMap.keySet().iterator(); iterator.hasNext();) {
						Long izinSahibiId = (Long) iterator.next();
						Personel izinSahibiClone = iliskiMap.get(izinSahibiId);
						cal.setTime(izinSahibiClone.getIzinHakEdisTarihi());
						int izinHakEdisYil = cal.get(Calendar.YEAR);
						cal.set(Calendar.YEAR, yil);
						long izinHakEdisTarihi = Long.parseLong(PdksUtil.convertToDateString(cal.getTime(), "yyyyMMdd"));
						fields.clear();
						fields.put("izinSahibiId", izinSahibiId);
						fields.put("izinSahibiNewId", izinSahibiClone.getId());
						fields.put("sistemAdminUserId", sistemAdminUser.getId());
						fields.put(PdksEntityController.MAP_KEY_SQLPARAMS, params);
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						try {
							pdksEntityController.execSP(fields, sb);
							if (yil > izinHakEdisYil && bugun >= izinHakEdisTarihi)
								ortakIslemler.getKidemHesabi(null, izinSahibiClone, null, null, authenticatedUser, session, null, Boolean.TRUE, Boolean.FALSE);
							session.flush();
						} catch (Exception re) {
							veriMap.get(izinSahibiId).setSecim(Boolean.FALSE);
							re.printStackTrace();
							logger.error("exec dbo.SP_IZIN_KOPYALA " + izinSahibiId + "," + izinSahibiClone.getId() + "," + sistemAdminUser.getId());
							PdksUtil.addMessageAvailableWarn(re.getMessage());
						}
					}

					// izinKopyalaJAVA(idler, iliskiMap, fields, personelBakiyeIzinler, sistemAdminUser);
					PdksUtil.addMessageAvailableInfo("Aktarım tamamlanmıştır.");
				} catch (Exception e) {
					e.printStackTrace();
					PdksUtil.addMessageAvailableWarn(e.getMessage());
				}

			}

			tipler = null;
		} else
			PdksUtil.addMessageAvailableWarn("Seçili kayıt yoktur!");
		idler = null;
		iliskiMap = null;
		return "";
	}

	/**
	 * @param idler
	 */
	protected void eskiKayitlariSil(List<Long> idler) throws Exception {
		if (idler != null && !idler.isEmpty()) {
			HashMap fields = new HashMap();
			StringBuffer sb = new StringBuffer();
			for (Iterator iterator = idler.iterator(); iterator.hasNext();) {
				Long long1 = (Long) iterator.next();
				sb.append(long1.toString());
				if (iterator.hasNext())
					sb.append(", ");
			}
			List params = Arrays.asList(new String[] { "izinSahibiId" });
			fields.put("izinSahibiId", sb.toString());
			fields.put(PdksEntityController.MAP_KEY_SQLPARAMS, params);
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			pdksEntityController.execSP(fields, new StringBuffer("dbo.SP_IZINLERI_SIL"));
			fields = null;
		}

	}

	/**
	 * @param idStr
	 * @param iliskiMap
	 * @param sistemAdminUser
	 */
	protected void izinKopyalaJAVA(String idStr, TreeMap<Long, Personel> iliskiMap, User sistemAdminUser) {
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT  I.* FROM " + PersonelIzin.TABLE_NAME + " I WITH(nolock) ");
		sb.append(" INNER JOIN " + IzinTipi.TABLE_NAME + " T ON T." + IzinTipi.COLUMN_NAME_ID + "=I." + PersonelIzin.COLUMN_NAME_IZIN_TIPI);
		sb.append(" WHERE I." + PersonelIzin.COLUMN_NAME_PERSONEL + " :pId");
		if (idStr.length() > 0)
			sb.append(" AND I." + PersonelIzin.COLUMN_NAME_IZIN_TIPI + " IN (" + idStr + ") ");
		sb.append(" ORDER BY I." + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI);
		fields.put("pId", iliskiMap.keySet());
		fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PersonelIzin> personelBakiyeIzinler = pdksEntityController.getObjectBySQLList(sb, fields, PersonelIzin.class);
		if (!personelBakiyeIzinler.isEmpty()) {
			List<Long> idler = new ArrayList<Long>();
			for (PersonelIzin personelIzin : personelBakiyeIzinler)
				idler.add(personelIzin.getId());
			fields.clear();
			fields.put("hakEdisIzin.id", idler);
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelIzinDetay> personelHarcananIzinler = pdksEntityController.getObjectByInnerObjectList(fields, PersonelIzinDetay.class);
			idler.clear();
			for (PersonelIzinDetay personelIzinDetay : personelHarcananIzinler) {
				PersonelIzin personelIzin = personelIzinDetay.getPersonelIzin();
				idler.add(personelIzin.getId());
			}
			List<PersonelIzinOnay> onaylar = null;
			if (!idler.isEmpty()) {
				fields.clear();
				fields.put("personelIzin.id", idler);
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				onaylar = pdksEntityController.getObjectByInnerObjectList(fields, PersonelIzinOnay.class);

			} else
				onaylar = new ArrayList<PersonelIzinOnay>();
			idler.clear();
			if (!personelHarcananIzinler.isEmpty())
				personelHarcananIzinler = PdksUtil.sortListByAlanAdi(personelHarcananIzinler, "id", false);

			for (Iterator iterator = personelBakiyeIzinler.iterator(); iterator.hasNext();) {
				PersonelIzin hakEdisIzin = (PersonelIzin) iterator.next();
				Personel izinSahibi = hakEdisIzin.getIzinSahibi();
				Personel izinSahibiClone = iliskiMap.get(izinSahibi.getId());
				PersonelIzin hakEdisIzinClone = (PersonelIzin) hakEdisIzin.clone();
				hakEdisIzinClone.setId(null);
				hakEdisIzinClone.setIzinSahibi(izinSahibiClone);
				hakEdisIzinClone.setOlusturanUser(sistemAdminUser);
				hakEdisIzinClone.setGuncelleyenUser(null);
				hakEdisIzinClone.setGuncellemeTarihi(null);
				pdksEntityController.saveOrUpdate(session, entityManager, hakEdisIzinClone); // session.saveOrUpdate
				// List<PersonelIzinDetay> hakEdisIzinList = new ArrayList<PersonelIzinDetay>();

				for (Iterator iterator2 = personelHarcananIzinler.iterator(); iterator2.hasNext();) {
					PersonelIzinDetay personelIzinDetay = (PersonelIzinDetay) iterator2.next();
					PersonelIzin personelIzin = personelIzinDetay.getPersonelIzin();
					if (personelIzin.isRedmi())
						iterator2.remove();
					else if (personelIzinDetay.getHakEdisIzin().getId().equals(hakEdisIzin.getId())) {
						PersonelIzinDetay personelIzinDetayClone = (PersonelIzinDetay) personelIzinDetay.clone();
						PersonelIzin personelIzinClone = (PersonelIzin) personelIzin.clone();
						personelIzinClone.setId(null);
						personelIzinClone.setIzinSahibi(izinSahibiClone);
						pdksEntityController.saveOrUpdate(session, entityManager, personelIzinClone);// session.saveOrUpdate
						personelIzinDetayClone.setId(null);
						personelIzinDetayClone.setHakEdisIzin(hakEdisIzinClone);
						personelIzinDetayClone.setPersonelIzin(personelIzinClone);
						pdksEntityController.saveOrUpdate(session, entityManager, personelIzinDetayClone);// session.saveOrUpdate
						// List<PersonelIzinOnay> onaylayanlar = new ArrayList<PersonelIzinOnay>();
						for (Iterator iterator3 = onaylar.iterator(); iterator3.hasNext();) {
							PersonelIzinOnay personelIzinOnay = (PersonelIzinOnay) iterator3.next();
							if (personelIzinOnay.getPersonelIzin().getId().equals(personelIzin.getId())) {
								PersonelIzinOnay personelIzinOnayClone = (PersonelIzinOnay) personelIzinOnay.clone();
								personelIzinOnayClone.setId(null);
								personelIzinOnayClone.setPersonelIzin(personelIzinClone);
								pdksEntityController.saveOrUpdate(session, entityManager, personelIzinOnayClone);// session.saveOrUpdate
								// onaylayanlar.add(personelIzinOnayClone);
								iterator3.remove();
							}

						}
						// personelIzinClone.setOnaylayanlar(new HashSet<PersonelIzinOnay>(onaylayanlar));
						// onaylayanlar = null;
						iterator2.remove();
					}
				}
				session.refresh(hakEdisIzinClone);

			}
			idler = null;
		}
		personelBakiyeIzinler = null;
	}

	/**
	 * @param idStr
	 * @param idler
	 */
	protected void eskiKayitlariSilJava(String idStr, List<Long> idler) {
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT  I.* FROM " + PersonelIzin.TABLE_NAME + " I WITH(nolock) ");
		sb.append(" INNER JOIN " + IzinTipi.TABLE_NAME + " T ON T." + IzinTipi.COLUMN_NAME_ID + "=I." + PersonelIzin.COLUMN_NAME_IZIN_TIPI);
		sb.append(" WHERE I." + PersonelIzin.COLUMN_NAME_PERSONEL + " :pId");
		if (idStr.length() > 0)
			sb.append(" AND I." + PersonelIzin.COLUMN_NAME_IZIN_TIPI + " IN (" + idStr + ") ");
		fields.put("pId", idler);
		fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PersonelIzin> personelBakiyeIzinler = pdksEntityController.getObjectBySQLList(sb, fields, PersonelIzin.class);
		if (!personelBakiyeIzinler.isEmpty()) {
			List deleteIzinler = new ArrayList();
			idler.clear();
			for (PersonelIzin personelIzin : personelBakiyeIzinler)
				idler.add(personelIzin.getId());
			fields.clear();
			fields.put("hakEdisIzin.id", idler);
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelIzinDetay> personelHarcananIzinler = pdksEntityController.getObjectByInnerObjectList(fields, PersonelIzinDetay.class);
			idler.clear();
			if (!personelHarcananIzinler.isEmpty()) {
				for (PersonelIzinDetay personelIzinDetay : personelHarcananIzinler)
					idler.add(personelIzinDetay.getPersonelIzin().getId());
				fields.clear();
				fields.put("personelIzin.id", idler);
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<PersonelIzinOnay> izinOnaylar = pdksEntityController.getObjectByInnerObjectList(fields, PersonelIzinOnay.class);
				if (!izinOnaylar.isEmpty())
					deleteIzinler.addAll(izinOnaylar);
				deleteIzinler.addAll(personelHarcananIzinler);
				for (PersonelIzinDetay personelIzinDetay : personelHarcananIzinler)
					deleteIzinler.add(personelIzinDetay.getPersonelIzin());

			}
			deleteIzinler.addAll(personelBakiyeIzinler);
			for (Iterator iterator = deleteIzinler.iterator(); iterator.hasNext();) {
				Object personelIzinVeri = (Object) iterator.next();
				if (personelIzinVeri != null) {
					pdksEntityController.deleteObject(session, entityManager, personelIzinVeri);

				}

			}
			deleteIzinler = null;
		}
		personelBakiyeIzinler = null;
	}

	/**
	 * @param sheet
	 * @param row
	 * @param col
	 * @return
	 * @throws Exception
	 */
	private String getSheetStringValue(Sheet sheet, int row, int col) throws Exception {
		String value = null;

		try {
			value = ExcelUtil.getSheetStringValue(sheet, row, col);
		} catch (Exception e) {
			value = String.valueOf(ExcelUtil.getSheetDoubleValue(sheet, row, col).longValue());

		}
		return value;
	}

	public Dosya getIzinBakiyeDosya() {
		return izinBakiyeDosya;
	}

	public void setIzinBakiyeDosya(Dosya izinBakiyeDosya) {
		this.izinBakiyeDosya = izinBakiyeDosya;
	}

	public List<TempIzin> getPersonelBakiyeIzinList() {
		return personelBakiyeIzinList;
	}

	public void setPersonelBakiyeIzinList(List<TempIzin> personelBakiyeIzinList) {
		this.personelBakiyeIzinList = personelBakiyeIzinList;
	}

	public StringBuffer getSbAll() {
		return sbAll;
	}

	public void setSbAll(StringBuffer sbAll) {
		this.sbAll = sbAll;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public SimpleDateFormat getSdf() {
		return sdf;
	}

	public void setSdf(SimpleDateFormat sdf) {
		this.sdf = sdf;
	}

	public Boolean getCheckBoxDurum() {
		return checkBoxDurum;
	}

	public void setCheckBoxDurum(Boolean checkBoxDurum) {
		this.checkBoxDurum = checkBoxDurum;
	}

}
