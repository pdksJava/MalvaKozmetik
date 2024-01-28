package org.pdks.quartz;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.faces.Renderer;
import org.pdks.entity.CalismaModeli;
import org.pdks.entity.Dosya;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.Kapi;
import org.pdks.entity.KapiView;
import org.pdks.entity.Liste;
import org.pdks.entity.NoteTipi;
import org.pdks.entity.Notice;
import org.pdks.entity.Parameter;
import org.pdks.entity.PdksLog;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelGeciciYonetici;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.security.entity.DefaultPasswordGenerator;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.pdks.security.entity.UserVekalet;
import org.pdks.session.ExcelUtil;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

import com.pdks.webservice.MailFile;
import com.pdks.webservice.MailObject;
import com.pdks.webservice.MailStatu;

@Name("iseGelmemeUyari")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class IseGelmemeUyari implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6477701530769282426L;
	static Logger logger = Logger.getLogger(IseGelmemeUyari.class);
	private static final String PARAMETER_KEY = "uyariMail";

	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	Renderer renderer;
	@In(required = false, create = true)
	HashMap<String, String> parameterMap;
	@In(required = false, create = true)
	public Zamanlayici zamanlayici;
	@In
	EntityManager entityManager;

	private User userYonetici, userUstYonetici;

	private ArrayList<User> userIKList = new ArrayList<User>(), userYoneticiList = new ArrayList<User>();

	private static boolean calisiyor = Boolean.FALSE;

	private String hataKonum, personelNoAciklama, tesisAciklama, bolumAciklama, altBolumAciklama, yoneticiAciklama, calismaModeliBaslikAciklama;

	private boolean statuGoster = Boolean.FALSE, hariciPersonelVar, yoneticiTanimsiz = Boolean.FALSE, yoneticiMailGonderme = Boolean.FALSE, izinVar = Boolean.FALSE, tesisVar = Boolean.FALSE, hataliHareketVar = Boolean.FALSE;

	private Tanim ekSaha1, ekSaha2, ekSaha3, ekSaha4;
	private CellStyle header = null;
	private CellStyle styleOdd = null;
	private CellStyle styleOddCenter = null;
	private CellStyle styleOddDate = null;
	private CellStyle styleEven = null;
	private CellStyle styleEvenCenter = null;
	private CellStyle styleEvenDate = null;

	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private String ekSahaAlanAdi, hareketExcelGonderDurum;
	private int ekSahaAlanNo = 2;
	private List<String> ikMailList = new ArrayList<String>();
	private boolean tesisYetki = false;
	private Notice uyariNot;
	private Personel yoneticiYok;
	private Date islemTarihi;
	private HashMap<Long, List<Long>> yoneticiPerListMap;
	private HashMap<Long, List<HareketKGS>> perHareketListMap;
	private TreeMap<Long, User> userYoneticiMap;

	/**
	 * @param sessionx
	 */
	private void fillEkSahaTanim(Session sessionx) {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(sessionx, Boolean.FALSE, Boolean.FALSE);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
		if (ortakIslemler.getTesisDurumu())
			tesisAciklama = ortakIslemler.tesisAciklama();
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
		altBolumAciklama = (String) sonucMap.get("altBolumAciklama");
		personelNoAciklama = ortakIslemler.personelNoAciklama();
		yoneticiAciklama = ortakIslemler.yoneticiAciklama();
		calismaModeliBaslikAciklama = ortakIslemler.calismaModeliAciklama();

	}

	/**
	 * @param yonetici
	 */
	private void baslikAyarla(Personel yonetici) {
		hataliHareketVar = Boolean.FALSE;
		izinVar = Boolean.FALSE;
		hariciPersonelVar = Boolean.FALSE;
		tesisVar = Boolean.FALSE;
		if (yonetici != null && yonetici.getPersonelVardiyalari() != null) {
			sortVardiyalar(yonetici);
			for (VardiyaGun vardiya : yonetici.getPersonelVardiyalari()) {
				try {
					if (!tesisVar)
						tesisVar = vardiya.getPersonel() != null && vardiya.getPersonel().getSirket().isTesisDurumu() && vardiya.getPersonel().getTesis() != null;
				} catch (Exception e) {

				}

				if (!hariciPersonelVar) {
					try {
						hariciPersonelVar = !yonetici.getId().equals(vardiya.getPersonel().getPdksYonetici().getId());
					} catch (Exception e) {

					}

				}
				if (!hataliHareketVar)
					hataliHareketVar = vardiya.isHareketHatali() && vardiya.getHareketler() != null && !vardiya.getHareketler().isEmpty();
				if (!izinVar)
					izinVar = vardiya.getIzin() != null;
			}
		}
	}

	/**
	 * @param veriMap
	 * @param session
	 * @return
	 * @throws Exception
	 */
	private String hareketMailDuzenle(TreeMap<String, Object> veriMap, Session session) throws Exception {
		if (veriMap == null)
			veriMap = new TreeMap<String, Object>();
		TreeMap<String, List<String>> hareketPersonelMap = new TreeMap<String, List<String>>(), mailPersonelMap = new TreeMap<String, List<String>>();
		try {
			HashMap fields = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT P.*   FROM  " + Personel.TABLE_NAME + "  P WITH(nolock) ");
			sb.append(" where P." + Personel.COLUMN_NAME_DURUM + "=1 AND P." + Personel.COLUMN_NAME_HAREKET_MAIL_ID + " IS NOT NULL");
			sb.append(" AND P." + Personel.COLUMN_NAME_MAIL_TAKIP + "=1 AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=CAST(GETDATE() AS date)");
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			// fields.put("ad", "hareketMailGrubu");
			// fields.put("deger", "1");
			List<Personel> personelList = pdksEntityController.getObjectBySQLList(sb, fields, Personel.class);
			for (Personel per : personelList) {
				if (per == null || per.getHareketMailGrubu() == null)
					continue;

				String sicillNo = per.getPdksSicilNo();
				List<String> mailList = PdksUtil.getListFromString(per.getHareketMailGrubu().getEmail(), null), perBilgiList = new ArrayList<String>();
				for (String mail : mailList) {
					List<String> list = mailPersonelMap.containsKey(mail) ? mailPersonelMap.get(mail) : new ArrayList<String>();
					if (list.isEmpty())
						mailPersonelMap.put(mail, list);
					if (mail.indexOf("@") > 1 && !list.contains(mail)) {
						list.add(sicillNo);
						perBilgiList.add(mail);
					}

				}
				hareketPersonelMap.put(sicillNo, perBilgiList);
				mailList = null;
			}
			personelList = null;

		} catch (Exception ee) {

		}
		// hareketPersonelMap.clear();
		// mailPersonelMap.clear();
		if (!hareketPersonelMap.isEmpty())
			veriMap.put("hareketPersonelMap", hareketPersonelMap);
		if (!mailPersonelMap.isEmpty())
			veriMap.put("mailPersonelMap", mailPersonelMap);

		return "";
	}

	/**
	 * @param tarih
	 * @param islemYapan
	 * @param manuel
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public String iseGelmeDurumu(Date tarih, User islemYapan, boolean manuel, Session session, boolean mailGonder) throws Exception {
		if (islemYapan != null && session != null)
			session.clear();
		logger.info("iseGelmeDurumu in " + PdksUtil.getCurrentTimeStampStr());
		if (!manuel)
			calisiyor = Boolean.TRUE;
		Calendar cal = Calendar.getInstance();
		Date bugun = cal.getTime();
		HashMap map = new HashMap();
		tesisYetki = ortakIslemler.getParameterKey("tesisYetki").equals("1");
		String yoneticiTanimsizStr = ortakIslemler.getParameterKey("yoneticiTanimsiz");
		yoneticiTanimsiz = PdksUtil.hasStringValue(yoneticiTanimsizStr);
		boolean devam = tarih == null;

		if (tarih == null)
			tarih = (Date) bugun.clone();
		else
			devam = bugun.getTime() > tarih.getTime();
		islemTarihi = tarih;
		if (devam) {
			if (session == null)
				session = PdksUtil.getSession(entityManager, islemYapan == null);

			setEkSaha1(null);
			setEkSaha2(null);
			setEkSaha3(null);
			setEkSaha4(null);
			Date tarihAralik = PdksUtil.getDate(tarih);
			cal.setTime(tarihAralik);
			cal.add(Calendar.DATE, -1);
			Date oncekiGun = cal.getTime();
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
			map.put("iseBaslamaTarihi<=", tarihAralik);
			map.put("sskCikisTarihi>=", tarihAralik);
			map.put("durum=", Boolean.TRUE);
			if (yoneticiTanimsiz == false)
				map.put("yoneticisi<>", null);
			map.put("mailTakip=", Boolean.TRUE);
			if (islemYapan != null && !islemYapan.isAdmin())
				map.put("sirket.departman.id=", islemYapan.getDepartman().getId());
			List<Personel> personeller = pdksEntityController.getObjectByInnerObjectListInLogic(map, Personel.class);
			if (!personeller.isEmpty()) {
				HashMap<Long, Long> yoneticiler = new HashMap<Long, Long>();
				HashMap<Long, Personel> kgsPerMap = new HashMap<Long, Personel>();
				if (yoneticiTanimsiz) {
					if (yoneticiYok == null)
						yoneticiYok = new Personel();
					yoneticiYok.setIseBaslamaTarihi(PdksUtil.getDateTime(new Date()));
					yoneticiYok.setIstenAyrilisTarihi(PdksUtil.getSonSistemTarih());
					yoneticiYok.setSskCikisTarihi(PdksUtil.getSonSistemTarih());
					yoneticiYok.setId(-yoneticiYok.getIseBaslamaTarihi().getTime());
					String[] dizi = yoneticiTanimsizStr.split(" ");
					yoneticiYok.setAd("");
					yoneticiYok.setSoyad("");
					if (dizi != null && dizi.length > 0) {
						for (int i = 0; i < dizi.length; i++) {
							if (i == 0)
								yoneticiYok.setAd(dizi[i]);
							else if (i == 1)
								yoneticiYok.setSoyad(dizi[i]);
							else
								yoneticiYok.setSoyad(yoneticiYok.getSoyad() + " " + dizi[i]);
						}
					}
				}
				for (Iterator iterator = personeller.iterator(); iterator.hasNext();) {
					Personel per = (Personel) iterator.next();
					PersonelKGS personelKGS = per.getPersonelKGS();
					if (personelKGS == null || personelKGS.getDurum() == null || personelKGS.getDurum().equals(Boolean.FALSE)) {
						iterator.remove();
						continue;
					}
					Sirket pdksSirket = per.getSirket();
					Personel yonetici = per.getYoneticisi();
					if (yonetici == null)
						yonetici = yoneticiYok;

					boolean islemDevam = yonetici != null;
					if (yonetici == null || PdksUtil.hasStringValue(per.getSicilNo()) == false || per.getSirket() == null)
						islemDevam = false;
					else if (!pdksSirket.getDurum() || !pdksSirket.getPdks())
						islemDevam = false;
					if (!islemDevam) {
						// logger.info(per.getPdksSicilNo() + " - " + per.getAdSoyad()));
						iterator.remove();
						continue;
					}
					if (yonetici.getId() < 0 || yonetici.isCalisiyorGun(islemTarihi))
						yoneticiler.put(yonetici.getId(), yonetici.getId());
					kgsPerMap.put(per.getPersonelKGS().getId(), per);
				}
				if (!kgsPerMap.isEmpty() && !yoneticiler.isEmpty()) {
					List dataIdList = new ArrayList<Long>(yoneticiler.values());
					String fieldName = "pdksPersonel.id";
					map.clear();
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
					map.put(fieldName, dataIdList);
					// map.put(PdksEntityController.MAP_KEY_MAP, "getPersonelId");
					// TreeMap<Long, User> userMap = pdksEntityController.getObjectByInnerObjectMap(map, User.class, Boolean.FALSE);
					TreeMap<Long, User> userMap = ortakIslemler.getParamTreeMap(Boolean.FALSE, "getPersonelId", false, dataIdList, fieldName, map, User.class, session);
					if (userMap != null && !userMap.isEmpty())
						ortakIslemler.setUserRoller(new ArrayList<User>(userMap.values()), session);
					TreeMap<String, Tatil> resmiTatilGunleri = ortakIslemler.getTatilGunleri(personeller, oncekiGun, tarih, session);
					Date sonrakiGun = ortakIslemler.tariheGunEkleCikar(cal, tarih, 1);
					// Vardiya kayıtları okunuyor
					TreeMap<String, VardiyaGun> vardiyalar = ortakIslemler.getIslemVardiyalar(personeller, oncekiGun, sonrakiGun, Boolean.FALSE, session, Boolean.TRUE);
					try {
						boolean islem = ortakIslemler.getVardiyaHareketIslenecekList(new ArrayList<VardiyaGun>(vardiyalar.values()), tarih, session);
						if (islem)
							vardiyalar = ortakIslemler.getIslemVardiyalar(personeller, oncekiGun, sonrakiGun, Boolean.FALSE, session, Boolean.TRUE);

					} catch (Exception e) {
					}

					List<VardiyaGun> vardiyaList = new ArrayList<VardiyaGun>(vardiyalar.values());
					ortakIslemler.sonrakiGunVardiyalariAyikla(tarih, vardiyaList, session);

					Date vardiyaBas = null;

					HareketKGS arifeCikis = null;
					Date vardiyaBitTar = (Date) tarih.clone();
					// İlk vardiya başlangıç zamanı okunuyor
					HashMap<Long, List<User>> depMail = new HashMap<Long, List<User>>();
					HashMap<Long, Personel> depYoneticiMap = new HashMap<Long, Personel>();
					for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
						VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator.next();
						if (pdksVardiyaGun.getVardiyaDate().before(oncekiGun) || pdksVardiyaGun.getVardiyaDate().after(sonrakiGun) || pdksVardiyaGun.getSonrakiVardiyaGun() == null) {
							iterator.remove();
							continue;
						}
						if (pdksVardiyaGun.getIslemVardiya() != null && pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans2BitZaman().getTime() > vardiyaBitTar.getTime())
							vardiyaBitTar = (Date) pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans2BitZaman().clone();
						// Long yoneticisiId = pdksVardiyaGun.getPersonel().getYoneticisi().getId();
						Tatil arifeGun = null;
						try {
							if ((!pdksVardiyaGun.getVardiya().isIcapVardiyasi() && pdksVardiyaGun.getVardiya().isCalisma())) {
								Vardiya islemVardiya = pdksVardiyaGun.getIslemVardiya();
								String vardiyaGun = PdksUtil.convertToDateString(pdksVardiyaGun.getVardiyaDate(), "yyyyMMdd");
								if (resmiTatilGunleri.containsKey(vardiyaGun)) {
									if (resmiTatilGunleri.get(vardiyaGun).isYarimGunMu()) {
										if (arifeCikis == null) {
											arifeCikis = new HareketKGS();
											arifeGun = resmiTatilGunleri.get(vardiyaGun);
											arifeCikis.setZaman(arifeGun.getBasTarih());
											cal.setTime(arifeGun.getBasTarih());
										}
										if (!islemVardiya.isFarkliGun()) {
											if (islemVardiya.getVardiyaBitZaman().getTime() > arifeCikis.getZaman().getTime()) {
												pdksVardiyaGun.setSonCikis(arifeCikis);
												islemVardiya.setVardiyaBitZaman(arifeCikis.getZaman());
												islemVardiya.setBitSaat((short) cal.get(Calendar.HOUR_OF_DAY));
												islemVardiya.setBitDakika((short) cal.get(Calendar.MINUTE));
											}
										} else {
											if (arifeGun.getOrjTatil().getBitTarih().getTime() > islemVardiya.getVardiyaBitZaman().getTime()) {
												iterator.remove();
												continue;

											} else if (islemVardiya.getVardiyaBasZaman().getTime() > arifeCikis.getZaman().getTime()) {
												HareketKGS ilkGirisIzin = new HareketKGS();
												ilkGirisIzin.setZaman(islemVardiya.getVardiyaBasZaman());
												ilkGirisIzin.setOrjinalZaman(ilkGirisIzin.getZaman());
												pdksVardiyaGun.setIlkGiris(ilkGirisIzin);
											}
										}

									} else if (!islemVardiya.isFarkliGun()) {
										iterator.remove();
										continue;

									} else {
										Tatil tatil = resmiTatilGunleri.get(vardiyaGun).getOrjTatil();
										if (tatil.getBitTarih().getTime() > islemVardiya.getVardiyaBitZaman().getTime()) {
											iterator.remove();
											continue;
										}
									}
								}
								if (pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans1BasZaman().getTime() > bugun.getTime()) {
									iterator.remove();
								} else {
									if (vardiyaBas == null || pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans1BasZaman().getTime() < vardiyaBas.getTime())
										vardiyaBas = pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans1BasZaman();

								}
							} else if (pdksVardiyaGun.getIslemVardiya().isCalisma())
								iterator.remove();
						} catch (Exception e) {
							logger.error("Pdks hata in : \n");
							e.printStackTrace();
							logger.error("Pdks hata out : " + e.getMessage());
							logger.error("hata " + pdksVardiyaGun.getVardiyaKey());
						}

					}
					if (!vardiyaList.isEmpty()) {
						TreeMap<String, Object> dataMap = new TreeMap<String, Object>();
						try {

							hareketMailDuzenle(dataMap, session);
						} catch (Exception e) {
						}

						TreeMap<String, List<String>> hareketPersonelMap = (TreeMap<String, List<String>>) dataMap.get("hareketPersonelMap");
						if (hareketPersonelMap != null && !hareketPersonelMap.isEmpty())
							dataMap.remove("hareketPersonelMap");
						TreeMap<String, List<VardiyaGun>> hareketHataliMap = new TreeMap<String, List<VardiyaGun>>();
						TreeMap<String, List<String>> mailPersonelMap = (TreeMap<String, List<String>>) dataMap.get("mailPersonelMap");
						map.clear();
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
						map.put("name", "ikUyariCC");
						map.put("active", Boolean.TRUE);

						map.clear();
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
						map.put("durum=", Boolean.TRUE);
						map.put("basTarih<=", ortakIslemler.tariheGunEkleCikar(cal, (Date) tarih.clone(), 1));
						map.put("bitTarih>=", vardiyaBas);
						map.put(PdksEntityController.MAP_KEY_MAP, "getVekaletVerenId");
						TreeMap<Long, UserVekalet> vekaletMap = pdksEntityController.getObjectByInnerObjectMapInLogic(map, UserVekalet.class, Boolean.FALSE);

						map.clear();
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
						map.put("durum=", Boolean.TRUE);
						map.put("basTarih<=", ortakIslemler.tariheGunEkleCikar(cal, (Date) tarih.clone(), 1));
						map.put("bitTarih>=", vardiyaBas);
						map.put(PdksEntityController.MAP_KEY_MAP, "getPersonelGeciciId");
						TreeMap<Long, PersonelGeciciYonetici> geciciYoneticiMap = pdksEntityController.getObjectByInnerObjectMapInLogic(map, PersonelGeciciYonetici.class, Boolean.FALSE);
						session.clear();

						List<Long> perIdList = new ArrayList<Long>();
						for (Long long1 : kgsPerMap.keySet())
							perIdList.add(kgsPerMap.get(long1).getId());
						// Hareket kayıtları okunuyor
						// HashMap<Long, ArrayList<HareketKGS>> personelHareketMap = ortakIslemler.fillKaradagPersonelHareketMap(new ArrayList(karadagPerMap.keySet()), vardiyaBas, vardiyaBitTar, session);
						HashMap<Long, ArrayList<HareketKGS>> personelHareketMap = ortakIslemler.fillPersonelKGSHareketMap(new ArrayList(kgsPerMap.keySet()), vardiyaBas, vardiyaBitTar, session);
						HashMap<Long, Personel> yoneticiMap = new HashMap<Long, Personel>();
						// Hareket kayıtları vardiya günlerine işleniyor
						boolean kayitVar = false;

						if (((personelHareketMap != null && !personelHareketMap.isEmpty())) || (!mailGonder)) {
							hareketExcelGonderDurum = ortakIslemler.getParameterKey("hareketExcelGonder");
							userYoneticiMap = new TreeMap<Long, User>();
							yoneticiPerListMap = new HashMap<Long, List<Long>>();
							perHareketListMap = new HashMap<Long, List<HareketKGS>>();

							for (Iterator iterator1 = vardiyaList.iterator(); iterator1.hasNext();) {
								VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator1.next();
								pdksVardiyaGun.setIlkGiris(null);
								pdksVardiyaGun.setSonCikis(null);
								pdksVardiyaGun.setHareketler(null);
								pdksVardiyaGun.setGirisHareketleri(null);
								pdksVardiyaGun.setCikisHareketleri(null);
								pdksVardiyaGun.setHareketler(null);
								pdksVardiyaGun.setHareketHatali(Boolean.FALSE);
								Personel pdksPersonel = pdksVardiyaGun.getPersonel();
								Long perNoId = pdksPersonel.getPersonelKGS().getId(), personelNoId = pdksPersonel.getId();
								Long depId = pdksPersonel.getSirket().getDepartman().getId();
								boolean hareketVar = personelHareketMap.containsKey(perNoId) && !personelHareketMap.get(perNoId).isEmpty();
								if (!hareketVar) {
									hareketVar = pdksVardiyaGun.getIzin() != null;
									if (hareketVar) {
										logger.debug(pdksPersonel.getPdksSicilNo() + " - " + pdksPersonel.getAdSoyad());
									}
								}
								if (!kayitVar)
									kayitVar = hareketVar;
								boolean calisma = pdksVardiyaGun.getIslemVardiya().isCalisma();
								boolean ekle = calisma;

								Personel yoneticisi = pdksPersonel.getYoneticisi() != null ? (Personel) pdksPersonel.getYoneticisi().clone() : yoneticiYok;
								if (hareketVar) {
									List<HareketKGS> hareketler = new ArrayList<HareketKGS>();
									ekle = vardiyaHareketKontrol(bugun, hareketler, personelHareketMap, pdksVardiyaGun);
									if (ekle && calisma == false)
										ekle = pdksVardiyaGun.getHareketler() != null && !pdksVardiyaGun.getHareketler().isEmpty();

									if (!hareketler.isEmpty()) {
										List<HareketKGS> list = perHareketListMap.containsKey(perNoId) ? perHareketListMap.get(perNoId) : new ArrayList<HareketKGS>();
										if (list.isEmpty())
											perHareketListMap.put(perNoId, list);

										list.addAll(hareketler);
										Long yoneticiId = yoneticisi.getId();
										List<Long> perList = yoneticiPerListMap.containsKey(yoneticiId) ? yoneticiPerListMap.get(yoneticiId) : new ArrayList<Long>();
										if (perList.isEmpty())
											yoneticiPerListMap.put(yoneticiId, perList);
										if (!perList.contains(perNoId))
											perList.add(perNoId);
									}
									hareketler = null;
								}
								if (yoneticisi != null && ekle) {
									if (!calisma)
										logger.debug(pdksVardiyaGun.getVardiyaKeyStr() + " " + pdksPersonel.getAdSoyad());
									if (geciciYoneticiMap.containsKey(pdksVardiyaGun.getPersonel().getId()))
										yoneticisi = (Personel) geciciYoneticiMap.get(pdksVardiyaGun.getPersonel().getId()).getYeniYonetici().getPdksPersonel().clone();
									if (vekaletMap.containsKey(yoneticisi.getId()))
										yoneticisi = (Personel) vekaletMap.get(yoneticisi.getId()).getYeniYonetici().getPdksPersonel().clone();
									if (yoneticisi == null) {
										if (!depYoneticiMap.containsKey(depId))
											depYoneticiMap.put(depId, (Personel) yoneticiYok.clone());
										yoneticisi = depYoneticiMap.get(depId);
									}

									if (yoneticisi == null || !yoneticisi.isCalisiyor())
										continue;
									if (yoneticiMap.containsKey(yoneticisi.getId()))
										yoneticisi = yoneticiMap.get(yoneticisi.getId());
									else
										yoneticisi.setPersonelVardiyalari(new ArrayList<VardiyaGun>());
									try {
										if (pdksVardiyaGun.getIslemVardiya() != null && pdksVardiyaGun.getIslemVardiya().getVardiyaBitZaman().getTime() >= bugun.getTime())
											pdksVardiyaGun.setSonCikis(null);
									} catch (Exception e) {
										logger.error(e);
									}

									yoneticisi.getPersonelVardiyalari().add(pdksVardiyaGun);
									String pdksSicilNo = pdksVardiyaGun.getPersonel().getPdksSicilNo();
									List<User> userList = null;

									if (depMail.containsKey(depId)) {
										userList = depMail.get(depId);
										yoneticiYok = depYoneticiMap.get(depId);
									}

									else if (yoneticiYok != null) {
										depYoneticiMap.put(depId, (Personel) yoneticiYok.clone());
										String mailBoxStr = pdksVardiyaGun.getPersonel().getSirket().getDepartman().getMailBox();
										if (mailBoxStr != null && mailBoxStr.indexOf("@") > 0) {
											String mailBox = ortakIslemler.getAktifMailAdress(mailBoxStr, session);
											userList = ortakIslemler.getAktifMailUser(mailBox, session);
										} else
											userList = new ArrayList<User>();
										depMail.put(depId, userList);
									}
									if (mailPersonelMap == null)
										mailPersonelMap = new TreeMap<String, List<String>>();
									if (hareketPersonelMap == null)
										hareketPersonelMap = new TreeMap<String, List<String>>();
									if (userList != null) {
										for (User user : userList) {
											if (user.getId() != null) {
												Personel personel = user.getPdksPersonel();
												if (personel.getId() != null && !personel.getId().equals(yoneticisi.getId())) {
													String mail = user.getEmail();
													List<String> list = mailPersonelMap.containsKey(mail) ? mailPersonelMap.get(mail) : new ArrayList<String>();
													if (list.isEmpty())
														mailPersonelMap.put(mail, list);
													if (mail.indexOf("@") > 1 && !list.contains(mail)) {
														if (!list.contains(pdksSicilNo))
															list.add(pdksSicilNo);

													}
													Personel depYonetici = yoneticiMap.containsKey(personel.getId()) ? yoneticiMap.get(personel.getId()) : (Personel) personel.clone();
													if (depYonetici.getPersonelVardiyalari() == null)
														depYonetici.setPersonelVardiyalari(new ArrayList<VardiyaGun>());

													List<String> mailList = hareketPersonelMap.containsKey(pdksSicilNo) ? hareketPersonelMap.get(pdksSicilNo) : new ArrayList<String>();
													if (mailList.isEmpty())
														hareketPersonelMap.put(pdksSicilNo, mailList);
													if (!mailList.contains(mail))
														mailList.add(mail);
													// depYonetici.getPersonelVardiyalari().add(pdksVardiyaGun);
													yoneticiMap.put(depYonetici.getId(), depYonetici);
												}
											}

										}
									}
									Long yoneticiId = yoneticisi.getId();
									if (hareketPersonelMap.containsKey(pdksSicilNo)) {
										List<VardiyaGun> list = hareketHataliMap.containsKey(pdksSicilNo) ? hareketHataliMap.get(pdksSicilNo) : new ArrayList<VardiyaGun>();
										if (list.isEmpty())
											hareketHataliMap.put(pdksSicilNo, list);
										list.add(pdksVardiyaGun);
										List<Long> list2 = yoneticiPerListMap.containsKey(yoneticiId) ? yoneticiPerListMap.get(yoneticiId) : new ArrayList<Long>();
										if (list2.isEmpty())
											yoneticiPerListMap.put(yoneticiId, list2);
										if (!list2.contains(personelNoId))
											list2.add(personelNoId);
									}
									yoneticiMap.put(yoneticiId, yoneticisi);
								}

							}

							if (kayitVar && !yoneticiMap.isEmpty()) {

								uyariNot = ortakIslemler.getNotice(NoteTipi.MAIL_CEVAPLAMAMA.value(), Boolean.TRUE, session);
								ikMailList.clear();
								fillEkSahaTanim(session);
								hareketPersonelMap = null;
								dataMap.put("hareketHataliMap", hareketHataliMap);
								yoneticiyeMailGonder(islemYapan, userMap, yoneticiMap, dataMap, manuel, session, mailGonder);
							}

						}
						hareketHataliMap = null;
						dataMap = null;
					}
				}

			}

		}
		logger.info("iseGelmeDurumu out " + PdksUtil.getCurrentTimeStampStr());
		if (!manuel)
			calisiyor = Boolean.FALSE;

		return "";
	}

	/**
	 * @param islemYapan
	 * @param userMap
	 * @param yoneticiMap
	 * @param dataMap
	 * @param manuel
	 * @param session
	 */
	private void yoneticiyeMailGonder(User islemYapan, TreeMap<Long, User> userMap, HashMap<Long, Personel> yoneticiMap, TreeMap<String, Object> dataMap, boolean manuel, Session session, boolean mailGonder) throws Exception {
		if (yoneticiMap != null && !yoneticiMap.isEmpty()) {
			TreeMap<String, List<VardiyaGun>> hareketHataliMap = (TreeMap<String, List<VardiyaGun>>) dataMap.get("hareketHataliMap");
			if (hareketHataliMap == null)
				hareketHataliMap = new TreeMap<String, List<VardiyaGun>>();
			TreeMap<String, List<String>> mailPersonelMap = (TreeMap<String, List<String>>) dataMap.get("mailPersonelMap");
			if (mailPersonelMap == null)
				mailPersonelMap = new TreeMap<String, List<String>>();
			List<Personel> yoneticiler = new ArrayList<Personel>(yoneticiMap.values());
			boolean hareketExcelGonder = PdksUtil.hasStringValue(hareketExcelGonderDurum);
			if (hareketExcelGonder) {
				boolean hareketDurum = hareketExcelGonderDurum.equalsIgnoreCase("IK");
				List<Long> userIdList = new ArrayList<Long>();
				HashMap<Long, User> perUserMap = new HashMap<Long, User>();
				for (Long key : userMap.keySet()) {
					User user = userMap.get(key);
					perUserMap.put(user.getPersonelId(), user);
				}
				for (Personel personel : yoneticiler) {
					Long yoneticisiId = personel.getId();
					if (yoneticisiId != null && userMap.containsKey(yoneticisiId))
						userIdList.add(yoneticisiId);
				}
				for (Long userId : yoneticiPerListMap.keySet()) {
					if (perUserMap.containsKey(userId)) {
						User user = perUserMap.get(userId);
						if (!userIdList.contains(userId) && (user.isIK() || hareketDurum == false)) {
							Personel personel = user.getPdksPersonel();
							yoneticiler.add(personel);
							yoneticiMap.put(userId, personel);
							// mailPersonelMap.put(user.getEmail(), new ArrayList<String>());
						}
					}
				}
				userIdList = null;
				perUserMap = null;
			}
			yoneticiler = PdksUtil.sortObjectStringAlanList(yoneticiler, "getAdSoyad", null);
			String renderAdres = "/email/" + (PdksUtil.getTestDurum() == false ? "iseGelisUyariMail.xhtml" : "iseGelisUyariTestMail.xhtml");
			List<User> mailIKList = new ArrayList<User>();
			yoneticiMailGonderme = ortakIslemler.getParameterKey("yoneticiMailGonderme").equals("1");
			HashMap<Long, List<User>> depMail = new HashMap<Long, List<User>>();

			for (Iterator iterator = yoneticiler.iterator(); iterator.hasNext();) {
				Personel personelYonetici = (Personel) iterator.next();
				if (!personelYonetici.isCalisiyor()) {
					iterator.remove();
					continue;
				}
				List<Long> depMailList = new ArrayList<Long>();
				String eposta = null;
				try {
					Long yoneticisiId = personelYonetici.getId();
					User yonetici = null;
					if (yoneticisiId != null && userMap.containsKey(yoneticisiId)) {
						User user = userMap.get(yoneticisiId);
						List<Role> yetkiliRollerim = user.getYetkiliRollerim();
						if (yetkiliRollerim == null || yetkiliRollerim.isEmpty())
							yetkiliRollerim = user.getYetkiliRoller();
						userYonetici = (User) user.clone();
						yonetici = (User) user.clone();
						yonetici.setYetkiliRollerim(yetkiliRollerim);
					} else
						continue;

					eposta = yonetici != null ? yonetici.getEmail() : "";
					List<VardiyaGun> personelVardiyalari = personelYonetici.getPersonelVardiyalari();
					if (personelVardiyalari == null)
						personelVardiyalari = new ArrayList<VardiyaGun>();
					List<String> hariciPersoneller = mailPersonelMap.containsKey(eposta) ? mailPersonelMap.get(eposta) : new ArrayList<String>();
					hariciPersonelVar = !hariciPersoneller.isEmpty() && eposta.indexOf("@") > 0;
					if (hariciPersonelVar) {
						for (VardiyaGun pdksVardiyaGun : personelVardiyalari) {
							String sicilNo = pdksVardiyaGun.getPersonel().getPdksSicilNo();
							if (hariciPersoneller.contains(sicilNo))
								hariciPersoneller.remove(sicilNo);
						}
						for (Iterator iterator2 = hariciPersoneller.iterator(); iterator2.hasNext();) {
							String sicilNo = (String) iterator2.next();
							if (hareketHataliMap.containsKey(sicilNo))
								personelVardiyalari.addAll(hareketHataliMap.get(sicilNo));
						}
						if (!ikMailList.contains(eposta))
							mailPersonelMap.remove(eposta);
						else
							logger.debug(eposta);

					}
					hariciPersoneller = null;
					mailIKList.clear();
					if (yonetici != null)
						ortakIslemler.setUserRoller(yonetici, session);
					userUstYonetici = null;

					if (yonetici == null || !yonetici.isGenelMudur()) {
						if (userIKList != null)
							userIKList.clear();
						else
							userIKList = new ArrayList<User>();
						if (yonetici != null && yonetici.isSuperVisor() && personelYonetici.getYoneticisi() != null && personelYonetici.getYoneticisi().isCalisiyor()) {
							HashMap fields = new HashMap();
							if (session != null)
								fields.put(PdksEntityController.MAP_KEY_SESSION, session);
							fields.put("pdksPersonel.id", personelYonetici.getYoneticisi().getId());
							User yoneticiDiger = (User) pdksEntityController.getObjectByInnerObject(fields, User.class);
							if (yoneticiDiger != null && yoneticiDiger.isDurum()) {
								ortakIslemler.setUserRoller(yoneticiDiger, session);
								if (yoneticiDiger.isYoneticiKontratli())
									userUstYonetici = yoneticiDiger;
							}
						}
						Personel yoneticisi = (Personel) yoneticiMap.get(yoneticisiId).clone();

						for (VardiyaGun pdksVardiyaGun : personelVardiyalari) {
							Long departmanId = pdksVardiyaGun.getPersonel().getSirket().getDepartman().getId();
							String mailBoxStr = pdksVardiyaGun.getPersonel().getSirket().getDepartman().getMailBox();
							if (mailBoxStr != null && mailBoxStr.indexOf("@") > 0) {
								if (!depMailList.contains(departmanId)) {
									depMailList.add(departmanId);
									List<String> mailList = null;
									if (depMail.containsKey(departmanId))
										mailIKList.addAll(depMail.get(departmanId));
									else {
										List<User> list = new ArrayList<User>();
										String mailBox = ortakIslemler.getAktifMailAdress(mailBoxStr, session);
										mailList = mailBoxStr.indexOf("@") > 0 ? PdksUtil.getListFromString(mailBox, null) : new ArrayList<String>();
										for (String mail : mailList) {
											if (!ikMailList.contains(mail) && mail.indexOf("@") > 0)
												ikMailList.add(mail);
										}
										if (!mailList.isEmpty()) {
											List<User> aktifUserList = ortakIslemler.getAktifMailUser(mailBox, session);
											for (User userYonetici : aktifUserList) {
												mailList.remove(userYonetici.getEmail());
												list.add(userYonetici);
											}
										}
										TreeMap<String, User> userYoneticilerMap = null;
										if (!mailList.isEmpty()) {
											userYoneticilerMap = ortakIslemler.getUserRoller(islemTarihi, mailList, session);
										} else
											userYoneticilerMap = new TreeMap<String, User>();

										for (Iterator iterator2 = mailList.iterator(); iterator2.hasNext();) {
											String mail = (String) iterator2.next();
											User yoneticiUser = userYoneticilerMap.containsKey(mail) ? userYoneticilerMap.get(mail) : null;
											if (yoneticiUser == null) {
												yoneticiUser = new User();
												yoneticiUser.setEmail(mail);
											}
											list.add(yoneticiUser);
										}
										depMail.put(departmanId, list);
									}
								}

							}
							if (depMail.containsKey(departmanId)) {
								List<User> yoneticiList = depMail.get(departmanId);
								String sicilNo = pdksVardiyaGun.getPersonel().getPdksSicilNo();
								List<VardiyaGun> vList = hareketHataliMap.containsKey(sicilNo) ? hareketHataliMap.get(sicilNo) : new ArrayList<VardiyaGun>();
								boolean ekle = true;
								String key = pdksVardiyaGun.getSortBolumKey();
								for (VardiyaGun vardiyaGun : vList) {
									if (vardiyaGun.getSortBolumKey().equals(key)) {
										ekle = false;
										break;
									}
								}
								if (vList.isEmpty())
									hareketHataliMap.put(sicilNo, vList);

								if (ekle)
									vList.add(pdksVardiyaGun);
								for (Iterator iterator2 = yoneticiList.iterator(); iterator2.hasNext();) {
									User user2 = (User) iterator2.next();
									String mailIK = user2.getEmail();
									List<String> list = mailPersonelMap.containsKey(mailIK) ? mailPersonelMap.get(mailIK) : new ArrayList<String>();
									if (list.isEmpty())
										mailPersonelMap.put(mailIK, list);
									if (!list.contains(sicilNo))
										list.add(sicilNo);
								}
							}

						}
						User user = yonetici != null ? (User) yonetici.clone() : null;
						if (user == null || !user.isDurum() || !user.getPdksPersonel().isCalisiyor()) {
							if (personelYonetici != null && user != null)
								logger.info(personelYonetici.getPdksSicilNo() + " " + personelYonetici.getAdSoyad() + " personel veya kullanıcısında sorun var!");
							continue;
						}

						if (yoneticiMailGonderme == false)
							user.setStaffId(yonetici.getEmail());

						if (islemYapan != null) {
							eposta = islemYapan.getEmail();
						}
						if (!userYoneticiMap.containsKey(user.getId())) {
							userYoneticiMap.put(user.getId(), user);
						} else
							logger.debug(user.getId());

						user.setEmail(eposta);
						user.setPdksPersonel(yoneticisi);
						setUserYonetici(user);
						setUserIKList((ArrayList<User>) mailIKList);
						statuGoster = Boolean.FALSE;
						ekSahaAlanAdi = "ekSaha" + ekSahaAlanNo;
						MailStatu mailSatu = null;
						try {

							if (mailGonder && !mailPersonelMap.containsKey(yonetici.getEmail())) {
								baslikAyarla(userYonetici.getPdksPersonel());

								mailSatu = mailGonder(renderAdres, false, session);
								// ortakIslemler.mailGonder(renderer, renderAdres);
								if (mailSatu != null && mailSatu.isDurum())

									logger.info(userYonetici.getPdksPersonel().getSirket().getAd() + " " + user.getAdSoyad() + " " + eposta + " iseGelisUyariMail mesaj gönderildi! ");
							}
						} catch (Exception ee) {
							if (islemYapan != null)
								ee.printStackTrace();
							logger.error(user.getAdSoyad() + " " + eposta + " yoneticiyeMailGonder : " + ee.getMessage());
						}

					}
				} catch (Exception e1) {
					logger.error("Pdks hata in : \n");
					e1.printStackTrace();
					logger.error("Pdks hata out : " + e1.getMessage());
					logger.error("yoneticiyeMailGonder 2 : " + e1.getMessage());
				}

			}

			if (!hareketHataliMap.isEmpty() && !mailPersonelMap.isEmpty()) {
				hariciPersonelVar = true;
				userUstYonetici = null;
				userIKList = null;
				User bosYonetici = new User();
				Personel personelYonetici = new Personel();
				personelYonetici.setAd("");
				personelYonetici.setSoyad("");
				Sirket pdksSirket = new Sirket();
				pdksSirket.setAciklama("");
				pdksSirket.setAd("");
				personelYonetici.setSirket(pdksSirket);
				bosYonetici.setPdksPersonel(personelYonetici);
				List<VardiyaGun> personelVardiyalari = new ArrayList<VardiyaGun>();
				List<String> mailList = !mailPersonelMap.isEmpty() ? new ArrayList(mailPersonelMap.keySet()) : new ArrayList<String>();
				TreeMap<String, User> userYoneticilerMap = ortakIslemler.getUserRoller(islemTarihi, mailList, session);

				boolean sort = false;
				for (String email : mailList) {
					personelVardiyalari.clear();
					logger.debug(email);
					User user = userYoneticilerMap.containsKey(email) ? userYoneticilerMap.get(email) : null;
					if (user == null || !user.isDurum()) {
						user = bosYonetici;
						user.setEmail(email);
					} else if (!user.getPdksPersonel().isCalisiyor()) {
						logger.error(user.getAdSoyad() + " MAİL GÖNDERİLEMİYOR!");
						continue;
					}
					List<Role> yetkiliRollerim = user.getYetkiliRollerim();
					if (yetkiliRollerim == null || yetkiliRollerim.isEmpty())
						yetkiliRollerim = user.getYetkiliRoller();
					userYonetici = (User) user.clone();
					userYonetici.setYetkiliRollerim(yetkiliRollerim);

					if (islemYapan != null)
						userYonetici.setEmail(islemYapan.getEmail());
					userYonetici.getPdksPersonel().setPersonelVardiyalari(personelVardiyalari);
					List<String> hariciPersoneller = mailPersonelMap.get(email);
					for (Iterator iterator2 = hariciPersoneller.iterator(); iterator2.hasNext();) {
						String sicilNo = (String) iterator2.next();
						if (hareketHataliMap.containsKey(sicilNo))
							personelVardiyalari.addAll(hareketHataliMap.get(sicilNo));

					}
					if (!personelVardiyalari.isEmpty()) {
						MailStatu mailSatu = null;
						try {
							userYonetici.setStaffId(email);
							baslikAyarla(userYonetici.getPdksPersonel());
							// ortakIslemler.mailGonder(renderer, renderAdres);
							mailSatu = mailGonder(renderAdres, true, session);
							if (mailSatu != null && mailSatu.isDurum())
								logger.info(userYonetici.getPdksPersonel().getSirket().getAd() + " " + userYonetici.getAdSoyad() + " " + userYonetici.getEmail() + " iseGelisUyariMail mesaj gönderildi! ");

							if (!userYoneticiMap.containsKey(userYonetici.getId())) {
								userYoneticiMap.put(userYonetici.getId(), userYonetici);
							} else
								logger.debug(userYonetici.getId());

							sort = true;
						} catch (Exception ex) {
							if (islemYapan != null)
								ex.printStackTrace();
							logger.error(email + " yoneticiyeMailGonder : " + ex.getMessage());

						}

					}
				}
				if (sort) {
					// userYoneticiList = (ArrayList<User>) PdksUtil.sortObjectStringAlanList(userYoneticiList, "getAdSoyad", null);

				}

			}
		}
	}

	/**
	 * @param renderAdres
	 * @return
	 * @throws Exception
	 */
	private MailStatu mailGonder(String renderAdres, boolean hariciGonder, Session session) throws Exception {
		MailStatu mailSatu = null;
		MailObject mail = new MailObject();
		boolean devam = true;
		if (yoneticiMailGonderme == false || hariciGonder) {
			if (userYonetici.getPdksPersonel() == null || userYonetici.getPdksPersonel().isCalisiyor())
				mail.getToList().add(userYonetici.getMailPersonel());
			if (userUstYonetici != null && hariciGonder == false)
				if (userUstYonetici.getPdksPersonel() == null || userUstYonetici.getPdksPersonel().isCalisiyor())
					mail.getCcList().add(userUstYonetici.getMailPersonel());
			devam = !mail.getToList().isEmpty();
		} else
			devam = ikMailList.contains(userYonetici.getStaffId());
		if (devam) {
			List<VardiyaGun> list = userYonetici.getPdksPersonel().getPersonelVardiyalari();
			mail.setSubject("Giriş-Çıkış problemli personeller");
			StringBuffer sb = new StringBuffer();
			sb.append("<p>Sayın " + userYonetici.getAdSoyad() + "</p>");
			if (list != null && !list.isEmpty()) {
				sb.append("<p>Aşağıdaki personel giriş çıkışlarında problem vardır.</p>");
				sb.append("<p></p>");
				sb.append("<p>Saygılarımla,</p>");
				sb.append("<p></p>");
			}

			Workbook wb = new XSSFWorkbook();
			if (mesajIcerikOlustur(userYonetici, sb, list, null, wb, session)) {
				userYoneticiList.add(userYonetici);
				if (list == null || list.isEmpty()) {
					sb.append("<p></p>");
					sb.append("<p>Saygılarımla,</p>");
				}

				if (uyariNot != null)
					sb.append(uyariNot.getValue());
				mail.setBody(sb.toString());
				try {
					if (tesisYetki && tesisVar)
						mail.getCcList().clear();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					wb.write(baos);
					MailFile mailFile = new MailFile();
					mailFile.setIcerik(baos.toByteArray());
					mailFile.setDisplayName("Işe Gelmeme Durum_" + PdksUtil.convertToDateString(islemTarihi, "yyyyMMdd") + ".xlsx");
					mail.getAttachmentFiles().add(mailFile);
					mailSatu = ortakIslemler.mailSoapServisGonder(true, mail, renderer, renderAdres, session);
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}
			}

		}

		return mailSatu;
	}

	/**
	 * @param user
	 * @param sb
	 * @param vardiyaGunList
	 * @param map1
	 * @param wb
	 * @param session
	 * @return
	 */
	private boolean mesajIcerikOlustur(User user, StringBuffer sb, List<VardiyaGun> vardiyaGunList, TreeMap<String, String> map1, Workbook wb, Session session) {
		boolean mesajGonder = false;
		try {
			if (wb == null)
				wb = new XSSFWorkbook();
			boolean userIK = user != null ? user.isIK() || user.isGenelMudur() : false;
			if (userIK) {
				userIK = false;
				for (Role role : user.getYetkiliRollerim()) {
					if (role.getRolename().equals(Role.TIPI_IK) || role.getRolename().equals(Role.TIPI_GENEL_MUDUR)) {
						userIK = true;
						break;
					}
				}
			}
			boolean hareketExcelEkle = false;
			if (PdksUtil.hasStringValue(hareketExcelGonderDurum)) {
				if (userIK)
					hareketExcelEkle = true;
				else if (vardiyaGunList != null)
					hareketExcelEkle = !hareketExcelGonderDurum.equalsIgnoreCase("IK");
				else
					hareketExcelEkle = hareketExcelGonderDurum.equalsIgnoreCase("V");
			}
			header = ExcelUtil.getStyleHeader(wb);
			styleOdd = ExcelUtil.getStyleOdd(null, wb);
			styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
			styleOddDate = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATETIME, wb);
			styleEven = ExcelUtil.getStyleEven(null, wb);
			styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
			styleEvenDate = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATETIME, wb);
			TreeMap<String, List<VardiyaGun>> sirketParcalaMap = new TreeMap<String, List<VardiyaGun>>();
			List<Liste> listeler = new ArrayList<Liste>();
			List<VardiyaGun> yeniList = new ArrayList<VardiyaGun>();
			List<Long> yoneticiPerIdList = new ArrayList<Long>();
			List<Long> tesisList = null;
			if (tesisYetki && session != null) {
				ortakIslemler.setUserTesisler(user, session);
				if (user.getYetkiliTesisler() != null) {
					tesisList = new ArrayList<Long>();
					for (Tanim tesis : user.getYetkiliTesisler())
						tesisList.add(tesis.getId());

				}

			}

			Long userId = user.getPersonelId();
			if (userId != null && yoneticiPerListMap.containsKey(userId)) {
				List<Long> yPerIdList = yoneticiPerListMap.get(userId);
				if (yPerIdList != null && !yPerIdList.isEmpty()) {
					for (Long key : yPerIdList) {
						List<HareketKGS> hareketKGSList = perHareketListMap.get(key);
						if (hareketKGSList != null && !hareketKGSList.isEmpty())
							yoneticiPerIdList.add(key);
					}

				}

			}

			if (userIK) {
				List<Long> perIKList = new ArrayList<Long>();
				if (user.isIKAdmin())
					perIKList.addAll(new ArrayList<Long>(perHareketListMap.keySet()));
				else if (user.getDepartman() != null) {
					Long deparmanId = user.getDepartman().getId();
					for (Long key : perHareketListMap.keySet()) {
						List<HareketKGS> hareketKGSList = perHareketListMap.get(key);
						if (hareketKGSList != null && !hareketKGSList.isEmpty()) {
							HareketKGS hareketKGS = hareketKGSList.get(0);
							Personel personel = hareketKGS.getVardiyaGun().getPdksPersonel();
							if (personel.getSirket().getDepartman().getId().equals(deparmanId))
								perIKList.add(key);
						}
					}
				}
				for (Long key : perIKList) {
					if (!yoneticiPerIdList.contains(key)) {
						List<HareketKGS> hareketKGSList = perHareketListMap.get(key);
						if (hareketKGSList != null && !hareketKGSList.isEmpty())
							yoneticiPerIdList.add(key);
					}

				}
			}
			if (vardiyaGunList != null && !vardiyaGunList.isEmpty()) {

				for (VardiyaGun vardiyaGun : vardiyaGunList) {
					Personel personel = vardiyaGun.getPersonel();
					if (map1 != null) {
						String vardiyaKey = user.getId() + "_" + vardiyaGun.getVardiyaKeyStr();
						if (map1.containsKey(vardiyaKey))
							continue;
						map1.put(vardiyaKey, vardiyaKey);
					}
					Long perId = personel.getId();
					if (!yoneticiPerIdList.contains(perId))
						yoneticiPerIdList.add(perId);
					Sirket sirket = personel.getSirket();
					Long departmanId = null;
					if (sirket.getDepartman() != null)
						departmanId = sirket.getDepartman().getId();
					Tanim tesis = sirket.isTesisDurumu() ? personel.getTesis() : null;
					if (tesisList != null && tesis != null && !tesisList.contains(tesis.getId()))
						continue;
					String key = (departmanId != null ? departmanId : 0L) + "_" + (sirket.getSirketGrup() == null ? sirket.getAd() : sirket.getSirketGrup().getAciklama()) + (tesis != null ? "_" + tesis.getAciklama() : "");
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
				if (!listeler.isEmpty()) {
					if (listeler.size() > 1)
						listeler = PdksUtil.sortObjectStringAlanList(listeler, "getSelected", null);

					TreeMap<String, Sheet> sheetMap = new TreeMap<String, Sheet>();
					TreeMap<String, Integer> sheetSatirMap = new TreeMap<String, Integer>();
					TreeMap<String, Integer> sheetSutunMap = new TreeMap<String, Integer>();
					for (Liste liste : listeler) {
						List<VardiyaGun> sirketSubeList = PdksUtil.sortObjectStringAlanList((List<VardiyaGun>) liste.getValue(), "getSortBolumKey", null);
						boolean calismaModeliVar = Boolean.FALSE, hataliHareketGundeVar = Boolean.FALSE, izinGirisVar = Boolean.FALSE, hariciPersonelPlandaVar = Boolean.FALSE, altBolumVar = Boolean.FALSE, altBolumDurum = PdksUtil.isPuantajSorguAltBolumGir();
						HashMap<Long, Tanim> bolumMap = new HashMap<Long, Tanim>();
						HashMap<Long, CalismaModeli> calismaModeliMap = new HashMap<Long, CalismaModeli>();
						HashMap<String, Tanim> altBolumMap = new HashMap<String, Tanim>();
						for (VardiyaGun vardiyaGun : sirketSubeList) {
							Personel personel = vardiyaGun.getPersonel();
							Tanim bolum = personel != null && personel.getEkSaha3() != null ? personel.getEkSaha3() : new Tanim(0L);
							CalismaModeli calismaModeli = vardiyaGun.getCalismaModeli();
							if (calismaModeli != null && !calismaModeliMap.containsKey(calismaModeli.getId()))
								calismaModeliMap.put(calismaModeli.getId(), calismaModeli);
							if (!bolumMap.containsKey(bolum.getId()))
								bolumMap.put(bolum.getId(), bolum);
							if (altBolumDurum) {
								Tanim altBolum = personel != null && personel.getEkSaha4() != null ? personel.getEkSaha4() : new Tanim(0L);
								String key = bolum.getId() + "_" + altBolum.getId();
								if (!altBolumMap.containsKey(key))
									altBolumMap.put(key, altBolum);
								if (!altBolumVar) {
									altBolumVar = altBolum.getId() > 0L;
								}
							}
							if (!hariciPersonelPlandaVar) {
								try {
									Personel yonetici = personel.getPdksYonetici();
									hariciPersonelPlandaVar = yonetici == null || !user.getPdksPersonel().getId().equals(yonetici.getId());
								} catch (Exception e) {
									logger.error(e);
								}
							}
							if (!hataliHareketGundeVar)
								hataliHareketGundeVar = vardiyaGun.isHareketHatali() && vardiyaGun.getHareketler() != null && !vardiyaGun.getHareketler().isEmpty();
							if (!izinGirisVar) {
								izinGirisVar = vardiyaGun.getIzin() != null;
							}

						}
						String calismaModeliAciklama = "";
						if (!calismaModeliMap.isEmpty()) {
							List<CalismaModeli> calismaModeliList = new ArrayList<CalismaModeli>(calismaModeliMap.values());
							if (calismaModeliList.size() == 1)
								calismaModeliAciklama = " [ " + calismaModeliList.get(0).getAciklama() + " ]";
							else
								calismaModeliVar = true;
							calismaModeliList = null;
						}
						calismaModeliMap = null;
						if (altBolumMap.size() < 2)
							altBolumVar = false;
						boolean bolumVar = bolumMap.size() > 1;
						Personel sirketPersonel = sirketSubeList.get(0).getPersonel();
						Sirket sirket = sirketPersonel.getSirket();
						Tanim tesis = sirket.isTesisDurumu() ? sirketPersonel.getTesis() : null;
						Tanim bolum = bolumMap.size() == 1 ? new ArrayList<Tanim>(bolumMap.values()).get(0) : null;
						Tanim altBolum = altBolumMap.size() == 1 ? new ArrayList<Tanim>(altBolumMap.values()).get(0) : null;
						if (bolum != null && altBolum != null && !PdksUtil.isStrDegisti(bolum.getAciklama(), altBolum.getAciklama()))
							altBolum = null;
						String sirketBaslik = (sirket.getSirketGrup() == null ? sirket.getAd() : sirket.getSirketGrup().getAciklama());
						String baslik = sirketBaslik + (tesis != null ? " " + tesis.getAciklama() + " " + tesisAciklama.toLowerCase(PdksUtil.TR_LOCALE) : "") + (bolum != null && bolum.getId() > 0L ? " " + bolum.getAciklama() + " " + bolumAciklama.toLowerCase(PdksUtil.TR_LOCALE) : "");
						baslik += (altBolum != null && altBolum.getId() > 0L ? " " + altBolum.getAciklama() + " " + altBolumAciklama.toLowerCase(PdksUtil.TR_LOCALE) : "") + calismaModeliAciklama;
						Sheet sheet = null;
						int row = 0;
						int col = 0;
						int uz = 5 + (hariciPersonelPlandaVar ? 1 : 0) + (bolumVar ? 1 : 0) + (calismaModeliVar ? 1 : 0) + (altBolumVar ? 1 : 0) + (hataliHareketGundeVar ? 1 : 0) + (izinGirisVar ? 1 : 0);
						String sirketIdStr = sirket.getSirketGrup() == null ? "S" + sirket.getId() : "G" + sirket.getSirketGrup().getId();
						if (sheetMap.containsKey(sirketIdStr)) {
							sheet = sheetMap.get(sirketIdStr);
							row = sheetSatirMap.get(sirketIdStr) + 2;
						} else {
							try {
								sheet = ExcelUtil.createSheet(wb, sirketBaslik, false);
							} catch (Exception e) {
								sheet = ExcelUtil.createSheet(wb, sirketBaslik + "_" + sirket.getErpKodu(), false);
							}
							if (sheet != null)
								sheetMap.put(sirketIdStr, sheet);
						}

						if (sheet != null) {
							baslik = PdksUtil.replaceAllManuel(baslik, "  ", " ");
							ExcelUtil.getCell(sheet, row, col, header).setCellValue("");
							for (int i = 1; i < uz; i++)
								ExcelUtil.getCell(sheet, row, i, header).setCellValue("");
							ExcelUtil.getCell(sheet, row, col, header).setCellValue(baslik);
							try {
								sheet.addMergedRegion(ExcelUtil.getRegion((int) row, (int) 0, (int) row, (int) uz - 1));
							} catch (Exception e) {
								e.printStackTrace();
							}
							++row;
							col = 0;
							if (hariciPersonelPlandaVar)
								ExcelUtil.getCell(sheet, row, col++, header).setCellValue(yoneticiAciklama);

							if (bolumVar)
								ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
							if (calismaModeliVar)
								ExcelUtil.getCell(sheet, row, col++, header).setCellValue(calismaModeliBaslikAciklama);
							if (altBolumVar)
								ExcelUtil.getCell(sheet, row, col++, header).setCellValue(altBolumAciklama);
							ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
							ExcelUtil.getCell(sheet, row, col++, header).setCellValue(personelNoAciklama);
							ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Çalışma Zamanı");
							ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Giriş Zamanı");
							ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Çıkış Zamanı");

							if (hataliHareketGundeVar)
								ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Hatalı Giriş/Çıkış");

							if (izinGirisVar)
								ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İzin Durum");
						}
						sb.append("<H3>" + baslik + "</H3>");
						sb.append("<TABLE class=\"mars\" style=\"border: solid 1px\" cellpadding=\"5\" cellspacing=\"0\"><THEAD> <TR>");
						if (hariciPersonelPlandaVar)
							sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>" + yoneticiAciklama + "</b></TH>");
						if (bolumVar)
							sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>" + bolumAciklama + "</b></TH>");
						if (calismaModeliVar)
							sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>" + calismaModeliBaslikAciklama + "</b></TH>");
						if (altBolumVar)
							sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>" + altBolumAciklama + "</b></TH>");
						sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>Adı Soyadı</b></TH>");
						sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>" + personelNoAciklama + "</b></TH>");
						sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>Çalışma Zamanı</b></TH>");
						sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>Giriş Zamanı</b></TH>");
						sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>Çıkış Zamanı</b></TH>");
						if (hataliHareketGundeVar)
							sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>Hatalı Giriş/Çıkış</b></TH>");
						if (izinGirisVar)
							sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>İzin Durum</b></TH>");
						sb.append("</TR></THEAD><TBODY>");
						yeniList.addAll(sirketSubeList);
						Long id = null;
						boolean renk = false;
						CellStyle styleOddWrap = (CellStyle) ((XSSFCellStyle) styleOdd).clone();
						CellStyle styleEvenWrap = (CellStyle) ((XSSFCellStyle) styleEven).clone();
						styleOddWrap.setWrapText(true);
						styleEvenWrap.setWrapText(true);

						for (Iterator iterator2 = sirketSubeList.iterator(); iterator2.hasNext();) {
							VardiyaGun vg = (VardiyaGun) iterator2.next();
							Personel personel = vg.getPersonel();
							boolean degisti = false;
							if (id == null || !personel.getId().equals(id)) {
								id = personel.getId();
								degisti = true;
							}
							Personel yonetici = personel.getPdksYonetici();
							if (yonetici == null)
								yonetici = yoneticiYok;
							if (yonetici == null)
								continue;

							if (sheet != null) {

								++row;
								col = 0;
								CellStyle style = null, styleWrap = null, styleCenter = null, cellStyleDate = null;
								if (renk) {
									cellStyleDate = styleOddDate;
									style = styleOdd;
									styleWrap = styleOddWrap;
									styleCenter = styleOddCenter;
								} else {
									cellStyleDate = styleEvenDate;
									style = styleEven;
									styleWrap = styleEvenWrap;
									styleCenter = styleEvenCenter;
								}
								if (hariciPersonelPlandaVar)
									ExcelUtil.getCell(sheet, row, col++, style).setCellValue(yonetici != null && degisti ? yonetici.getAdSoyad() : "");

								if (bolumVar)
									ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null && degisti ? personel.getEkSaha3().getAciklama() : "");
								if (calismaModeliVar)
									ExcelUtil.getCell(sheet, row, col++, style).setCellValue(vg.getCalismaModeli() != null && degisti ? vg.getCalismaModeli().getAciklama() : "");
								if (altBolumVar)
									ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha4() != null && degisti ? personel.getEkSaha4().getAciklama() : "");
								ExcelUtil.getCell(sheet, row, col++, style).setCellValue(degisti ? personel.getAdSoyad() : "");
								ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(degisti ? personel.getSicilNo() : "");
								ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(vg.getVardiyaZamanAdi());
								if (vg.getIlkGiris() != null)
									ExcelUtil.getCell(sheet, row, col++, cellStyleDate).setCellValue(vg.getIlkGiris().getOrjinalZaman());

								else
									ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue("");
								if (vg.getSonCikis() != null)
									ExcelUtil.getCell(sheet, row, col++, cellStyleDate).setCellValue(vg.getSonCikis().getOrjinalZaman());

								else
									ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue("");
								Cell hareketCell = null;
								if (hataliHareketGundeVar) {
									StringBuffer sbMesaj = new StringBuffer();
									if (vg.getHareketler() != null && !vg.getHareketler().isEmpty()) {
										for (HareketKGS hareketKGS : vg.getHareketler()) {
											sbMesaj.append((sbMesaj.length() > 0 ? "\n" : "") + hareketKGS.getKapiView().getAciklama() + " " + (hareketKGS.getZaman() != null ? user.getTarihFormatla(hareketKGS.getZaman(), PdksUtil.getDateFormat() + " H:mm") : ""));
										}
									}
									hareketCell = ExcelUtil.getCell(sheet, row, col++, styleWrap);
									hareketCell.setCellValue(sbMesaj.toString());

								}

								if (izinGirisVar) {
									StringBuffer sbMesaj = new StringBuffer();
									if (vg.getIzin() != null) {
										String aciklama = vg.getIzin().getIzinTipiAciklama();
										sbMesaj.append(aciklama);
									}
									ExcelUtil.getCell(sheet, row, col++, styleWrap).setCellValue(sbMesaj.toString());

								}

							}
							renk = !renk;

							String classTR = "class=\"" + (renk ? "odd" : "even") + "\"";
							sb.append("<TR " + classTR + ">");
							if (hariciPersonelPlandaVar)
								sb.append("<td nowrap style=\"border: 1px solid;\">" + (yonetici != null && degisti ? yonetici.getAdSoyad() : "") + "</td>");
							if (bolumVar)
								sb.append("<td nowrap style=\"border: 1px solid;\">" + (personel.getEkSaha3() != null && degisti ? personel.getEkSaha3().getAciklama() : "") + "</td>");
							if (calismaModeliVar)
								sb.append("<td nowrap style=\"border: 1px solid;\">" + (vg.getCalismaModeli() != null && degisti ? vg.getCalismaModeli().getAciklama() : "") + "</td>");

							if (altBolumVar)
								sb.append("<td nowrap style=\"border: 1px solid;\">" + (personel.getEkSaha4() != null && degisti ? personel.getEkSaha4().getAciklama() : "") + "</td>");
							sb.append("<td nowrap style=\"border: 1px solid;\">" + (degisti ? personel.getAdSoyad() : "") + "</td>");
							sb.append("<td align=\"center\" style=\"border: 1px solid;\">" + (degisti ? personel.getSicilNo() : "") + "</td>");
							sb.append("<td align=\"center\" style=\"border: 1px solid;\">" + vg.getVardiyaZamanAdi() + "</td>");
							sb.append("<td align=\"center\" style=\"border: 1px solid;\">" + (vg.getIlkGiris() != null ? user.getTarihFormatla(vg.getIlkGiris().getOrjinalZaman(), PdksUtil.getDateFormat() + " H:mm") : "") + "</td>");
							sb.append("<td align=\"center\" style=\"border: 1px solid;\">" + (vg.getSonCikis() != null ? user.getTarihFormatla(vg.getSonCikis().getOrjinalZaman(), PdksUtil.getDateFormat() + " H:mm") : "") + "</td>");
							if (hataliHareketGundeVar) {
								sb.append("<td align=\"center\" style=\"border: 1px solid;\">");
								if (vg.getHareketler() != null && !vg.getHareketler().isEmpty()) {
									sb.append("<TABLE>");
									for (HareketKGS hareketKGS : vg.getHareketler()) {
										sb.append("<TR " + classTR + "><td nowrap >" + hareketKGS.getKapiView().getAciklama() + "</td>");
										sb.append("<td nowrap>" + (hareketKGS.getZaman() != null ? user.getTarihFormatla(hareketKGS.getZaman(), PdksUtil.getDateFormat() + " H:mm") : "") + "</td></TR>");
									}
									sb.append("</TABLE>");
								}

								sb.append("</td>");

							}
							if (izinGirisVar) {
								if (vg.getIzin() == null)
									sb.append("<td style=\"border: 1px solid;\"> </td>");
								else {
									String aciklama = vg.getIzin().getIzinTipiAciklama();
									sb.append("<td style=\"border: 1px solid;\" nowrap>" + aciklama + "</td>");
								}

							}

							sb.append("</TR>");

						}
						sb.append("</TBODY></TABLE><BR/><BR/>");

						if (sheet != null) {
							if (sheetSutunMap.containsKey(sirketIdStr)) {
								int uz2 = sheetSutunMap.get(sirketIdStr);
								if (uz2 > uz)
									uz = uz2;
							}
							for (int i = 0; i < uz; i++)
								sheet.autoSizeColumn(i);

						}
						sheetSatirMap.put(sirketIdStr, row);
						sheetSutunMap.put(sirketIdStr, uz);

					}
					mesajGonder = true;
				}
			} else {
				if (hareketExcelEkle)
					sb.append("<p><SPAN style='color:blue;'> Personel giriş çıkışlarında problem yoktur.</SPAN></p>");
			}
			if (hareketExcelEkle && !yoneticiPerIdList.isEmpty()) {
				TreeMap<String, Liste> listeMap = new TreeMap<String, Liste>();
				List<Long> sirketIdList = new ArrayList<Long>(), tesisIdList = new ArrayList<Long>(), altBolumIdList = new ArrayList<Long>(), calismaModeliIdList = new ArrayList<Long>();
				CalismaModeli calismaModeli = null;
				Sirket sirket = null;
				Tanim tesis = null, bolum = null, altBolum = null;
				boolean hariciPersonelPlandaVar = false;

				for (Long perNoId : yoneticiPerIdList) {
					if (perHareketListMap.containsKey(perNoId)) {
						List<HareketKGS> hareketList = perHareketListMap.get(perNoId);
						if (!hareketList.isEmpty()) {
							HareketKGS hareketKGS = hareketList.get(0);
							VardiyaGun vg = hareketKGS.getVardiyaGun();
							calismaModeli = vg.getCalismaModeli();
							if (calismaModeli != null && !calismaModeliIdList.contains(calismaModeli.getId()))
								calismaModeliIdList.add(calismaModeli.getId());
							Personel personel = vg.getPdksPersonel();
							Personel yonetici = personel.getPdksYonetici();
							if (yonetici == null)
								yonetici = yoneticiYok;
							if (!hariciPersonelPlandaVar) {
								try {

									hariciPersonelPlandaVar = yonetici == null || !user.getPdksPersonel().getId().equals(yonetici.getId());
								} catch (Exception e) {
									logger.error(e);
								}
							}
							String yoneticiAdi = yonetici != null ? yonetici.getAdSoyad() : null;
							if (!PdksUtil.hasStringValue(yoneticiAdi))
								yoneticiAdi = "";

							sirket = personel.getSirket();
							Long departmanId = sirket.getDepartman().getId();
							Long sirketId = sirket.getSirketGrup() == null ? sirket.getId() : -sirket.getSirketGrup().getId();
							if (!sirketIdList.contains(sirketId))
								sirketIdList.add(sirketId);
							tesis = sirket.isTesisDurumu() ? personel.getTesis() : null;
							if (tesis != null && !tesisIdList.contains(tesis.getId()))
								tesisIdList.add(tesis.getId());
							bolum = personel.getEkSaha3();
							altBolum = personel.getEkSaha4();
							if (altBolum != null && !altBolumIdList.contains(altBolum.getId()))
								altBolumIdList.add(altBolum.getId());
							String key = (departmanId != null ? departmanId : 0L) + "_" + (sirket.getSirketGrup() == null ? sirket.getAd() : sirket.getSirketGrup().getAciklama()) + (tesis != null ? "_" + tesis.getAciklama() : "");
							key += "_" + yoneticiAdi + (bolum != null ? "_" + bolum.getAciklama() : "") + (altBolum != null ? "_" + altBolum.getAciklama() : "");
							key += (calismaModeli != null ? "_" + calismaModeli.getAciklama() : "") + "_" + personel.getAdSoyad() + "_" + personel.getSicilNo();
							List<String> idList = new ArrayList<String>();
							for (Iterator iterator = hareketList.iterator(); iterator.hasNext();) {
								HareketKGS hareketKGS2 = (HareketKGS) iterator.next();
								if (!idList.contains(hareketKGS2.getId()))
									idList.add(hareketKGS2.getId());
								else
									iterator.remove();
							}
							if (!hareketList.isEmpty()) {
								if (listeMap.containsKey(key)) {
									Liste liste = listeMap.get(key);
									List<HareketKGS> list1 = (List<HareketKGS>) liste.getValue();
									list1.addAll(hareketList);
								} else {
									Liste list2 = new Liste(key, hareketList);
									list2.setSelected(key);
									listeMap.put(key, list2);
								}

							}
						} else
							logger.debug(perNoId);
					}
				}
				if (!listeMap.isEmpty()) {
					listeler = new ArrayList<Liste>(listeMap.values());
					if (listeler.size() > 1)
						listeler = PdksUtil.sortObjectStringAlanList(listeler, "getSelected", null);
					List<HareketKGS> hareketList = new ArrayList<HareketKGS>();
					for (Liste list2 : listeler) {
						List<HareketKGS> list1 = (List<HareketKGS>) list2.getValue();
						hareketList.addAll(list1);
					}
					Sheet sheetHareket = ExcelUtil.createSheet(wb, "Kart Girişler", false);
					int row = 0;
					int col = 0;
					boolean sirketVar = sirketIdList.size() > 1;
					boolean tesisVar = tesisIdList.size() > 0 && (sirketVar || tesisIdList.size() > 1);
					boolean calismaModeliVar = calismaModeliIdList.size() > 1;
					boolean altBolumVar = altBolumIdList.size() > 0;

					if (sirketIdList.size() == 1 && sirket != null) {
						String baslik = (sirket.getSirketGrup() == null ? sirket.getAd() : sirket.getSirketGrup().getAciklama());
						if (tesisIdList.size() == 1 && tesis != null)
							baslik += " " + tesis.getAciklama();
						if (calismaModeliIdList.size() == 1 && calismaModeli != null)
							baslik += " [ " + calismaModeli.getAciklama() + " ]";
						int uz = (hariciPersonelPlandaVar ? 1 : 0) + (sirketVar ? 1 : 0) + (tesisVar ? 1 : 0) + (calismaModeliVar ? 1 : 0) + (altBolumVar ? 1 : 0) + 6;
						for (int i = 0; i < uz; i++) {
							ExcelUtil.getCell(sheetHareket, row, i, header).setCellValue("");
						}
						ExcelUtil.getCell(sheetHareket, row, col, header).setCellValue(baslik);
						try {
							sheetHareket.addMergedRegion(ExcelUtil.getRegion((int) row, (int) 0, (int) row, (int) uz - 1));
						} catch (Exception e) {
							e.printStackTrace();
						}
						++row;
					}
					if (hariciPersonelPlandaVar)
						ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue(yoneticiAciklama);
					if (sirketVar)
						ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
					if (tesisVar)
						ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());

					ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue(bolumAciklama);
					if (calismaModeliVar)
						ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue(calismaModeliBaslikAciklama);
					if (altBolumVar)
						ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue(altBolumAciklama);
					ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue("Adı Soyadı");
					ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue(personelNoAciklama);
					ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue("Çalışma Zamanı");
					ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue("Kapı");
					ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue("Zamanı");
					boolean renk = true;
					for (Iterator iterator = hareketList.iterator(); iterator.hasNext();) {
						HareketKGS hareketKGS = (HareketKGS) iterator.next();
						VardiyaGun vg = hareketKGS.getVardiyaGun();
						Personel personel = vg.getPdksPersonel();
						calismaModeli = vg.getCalismaModeli();
						sirket = personel.getSirket();
						tesis = tesisVar && sirket.isTesisDurumu() ? personel.getTesis() : null;
						++row;
						col = 0;
						CellStyle style = null, styleCenter = null, cellStyleDate = null;
						if (renk) {
							cellStyleDate = styleOddDate;
							style = styleOdd;

							styleCenter = styleOddCenter;
						} else {
							cellStyleDate = styleEvenDate;
							style = styleEven;

							styleCenter = styleEvenCenter;
						}
						renk = !renk;
						boolean degisti = true;
						Personel yonetici = personel.getPdksYonetici();
						if (yonetici == null)
							yonetici = yoneticiYok;
						if (hariciPersonelPlandaVar)
							ExcelUtil.getCell(sheetHareket, row, col++, style).setCellValue(yonetici != null && degisti ? yonetici.getAdSoyad() : "");
						if (sirketVar)
							ExcelUtil.getCell(sheetHareket, row, col++, style).setCellValue(sirket.getSirketGrup() == null ? sirket.getAd() : sirket.getSirketGrup().getAciklama());
						if (tesisVar)
							ExcelUtil.getCell(sheetHareket, row, col++, style).setCellValue(tesis != null ? tesis.getAciklama() : "");

						ExcelUtil.getCell(sheetHareket, row, col++, style).setCellValue(personel.getEkSaha3() != null && degisti ? personel.getEkSaha3().getAciklama() : "");
						if (calismaModeliVar)
							ExcelUtil.getCell(sheetHareket, row, col++, style).setCellValue(calismaModeli != null && degisti ? calismaModeli.getAciklama() : "");
						if (altBolumVar)
							ExcelUtil.getCell(sheetHareket, row, col++, style).setCellValue(personel.getEkSaha4() != null && degisti ? personel.getEkSaha4().getAciklama() : "");
						ExcelUtil.getCell(sheetHareket, row, col++, style).setCellValue(degisti ? personel.getAdSoyad() : "");
						ExcelUtil.getCell(sheetHareket, row, col++, styleCenter).setCellValue(degisti ? personel.getSicilNo() : "");
						ExcelUtil.getCell(sheetHareket, row, col++, styleCenter).setCellValue(vg.getVardiyaZamanAdi());
						ExcelUtil.getCell(sheetHareket, row, col++, style).setCellValue(hareketKGS.getKapiKGS().getKapi().getAciklama());

						ExcelUtil.getCell(sheetHareket, row, col++, cellStyleDate).setCellValue(hareketKGS.getOrjinalZaman());

					}
					for (int i = 0; i < col; i++)
						sheetHareket.autoSizeColumn(i);
				}
				listeMap = null;
				mesajGonder = true;
			}
			yoneticiPerIdList = null;

			user.getPdksPersonel().setPersonelVardiyalari(yeniList);
		} catch (Exception em) {
			logger.error(em);
			em.printStackTrace();
			mesajGonder = false;
		}
		return mesajGonder;
	}

	/**
	 * @param yoneticiPersonel
	 */
	private void sortVardiyalar(Personel yoneticiPersonel) {
		try {
			if (yoneticiPersonel.getPersonelVardiyalari().size() > 1) {
				TreeMap<String, VardiyaGun> map1 = new TreeMap<String, VardiyaGun>();
				for (VardiyaGun pdksVardiyaGun : yoneticiPersonel.getPersonelVardiyalari())
					map1.put(pdksVardiyaGun.getSortKey(), pdksVardiyaGun);
				List<VardiyaGun> list = PdksUtil.sortObjectStringAlanList(new ArrayList(map1.values()), "getSortKey", null);
				map1 = null;
				yoneticiPersonel.setPersonelVardiyalari(list);
			}
		} catch (Exception e) {

		}

	}

	/**
	 * @param bugun
	 * @param hareketler
	 * @param personelHareketMap
	 * @param pdksVardiyaGun
	 * @return
	 */
	private boolean vardiyaHareketKontrol(Date bugun, List<HareketKGS> hareketler, HashMap<Long, ArrayList<HareketKGS>> personelHareketMap, VardiyaGun pdksVardiyaGun) {

		Vardiya islemVardiya = pdksVardiyaGun.getIslemVardiya();
		PersonelView personelView = new PersonelView();
		personelView.setPdksPersonel(pdksVardiyaGun.getPdksPersonel());
		boolean ekle = islemVardiya != null && bugun.getTime() > islemVardiya.getVardiyaTelorans2BasZaman().getTime();
		if (ekle) {
			Long perNoId = pdksVardiyaGun.getPersonel().getPersonelKGS().getId();
			boolean baslangicTatil = Boolean.FALSE, bitisTatil = Boolean.FALSE;// islemVardiya.getVardiyaTelorans1BitZaman().getTime()
			// <=
			// bugun.getTime();

			ArrayList<HareketKGS> perHareketList = null;
			if (personelHareketMap.containsKey(perNoId))
				perHareketList = personelHareketMap.get(perNoId);
			else
				perHareketList = new ArrayList<HareketKGS>();
			HareketKGS sonCikisIzin = null, ilkGirisIzin = null;
			if (baslangicTatil) {
				ilkGirisIzin = new HareketKGS();
				KapiView kapiView = new KapiView();
				Kapi kapi = new Kapi();
				Tanim tipi = new Tanim();
				tipi.setKodu(Kapi.TIPI_KODU_GIRIS);
				kapi.setTipi(tipi);
				kapiView.setKapi(kapi);
				ilkGirisIzin.setKapiView(kapiView);
				ilkGirisIzin.setZaman(islemVardiya.getVardiyaBasZaman());
				ilkGirisIzin.setOrjinalZaman(ilkGirisIzin.getZaman());
				// perHareketList.add(ilkGirisIzin);
			}
			// pdksVardiyaGun.setIlkGiris(ilkGirisIzin);

			if (bitisTatil) {
				sonCikisIzin = new HareketKGS();
				sonCikisIzin.setZaman(islemVardiya.getVardiyaBitZaman());
				sonCikisIzin.setOrjinalZaman(sonCikisIzin.getZaman());
				KapiView kapiView = new KapiView();
				Kapi kapi = new Kapi();
				Tanim tipi = new Tanim();
				tipi.setKodu(Kapi.TIPI_KODU_CIKIS);
				kapi.setTipi(tipi);
				kapiView.setKapi(kapi);
				sonCikisIzin.setKapiView(kapiView);

				// perHareketList.add(sonCikisIzin);
			}
			if (perHareketList.size() > 1)
				perHareketList = (ArrayList<HareketKGS>) PdksUtil.sortListByAlanAdi(perHareketList, "zaman", Boolean.FALSE);
			for (Iterator iterator = perHareketList.iterator(); iterator.hasNext();) {
				HareketKGS hareket = (HareketKGS) iterator.next();
				if (pdksVardiyaGun.addHareket(hareket, Boolean.TRUE)) {
					hareket.setVardiyaGun(pdksVardiyaGun);
					hareketler.add(hareket);
					iterator.remove();
				}

			}
			if (!perHareketList.isEmpty())
				personelHareketMap.put(perNoId, perHareketList);
			else
				personelHareketMap.remove(perNoId);
			if (ilkGirisIzin != null && (pdksVardiyaGun.getIlkGiris() == null || pdksVardiyaGun.getIlkGiris().getZaman().getTime() > ilkGirisIzin.getZaman().getTime())) {
				pdksVardiyaGun.setIlkGiris(ilkGirisIzin);
			}

			if (sonCikisIzin != null && (pdksVardiyaGun.getSonCikis() == null || pdksVardiyaGun.getSonCikis().getZaman().getTime() < sonCikisIzin.getZaman().getTime()))
				pdksVardiyaGun.setSonCikis(sonCikisIzin);
			if (pdksVardiyaGun.getIlkGiris() != null) {
				Date girisZaman = pdksVardiyaGun.getIlkGiris().getZaman();
				if (islemVardiya.getVardiyaBasZaman().getTime() == girisZaman.getTime()) {
					if (pdksVardiyaGun.getSonCikis() != null) {
						Date cikisZaman = pdksVardiyaGun.getSonCikis().getZaman();
						if (islemVardiya.getVardiyaBitZaman().getTime() == cikisZaman.getTime())
							ekle = Boolean.FALSE;

					}
					if (islemVardiya.getVardiyaTelorans1BitZaman().getTime() > bugun.getTime()) {
						ekle = Boolean.FALSE;

					}
				}

			}
			if (!ekle) {
				if (baslangicTatil && pdksVardiyaGun.getIlkGiris().getId() == null)
					pdksVardiyaGun.setIlkGiris(null);
				if (bitisTatil && pdksVardiyaGun.getSonCikis().getId() == null)
					pdksVardiyaGun.setSonCikis(null);

				ekle = pdksVardiyaGun.isHareketHatali();
				if (!ekle && pdksVardiyaGun.getIzin() != null)
					ekle = pdksVardiyaGun.getHareketler() != null && !pdksVardiyaGun.getHareketler().isEmpty();

			}

		}

		return ekle;
	}

	/**
	 * @param when
	 * @param interval
	 * @return
	 */
	@Asynchronous
	@SuppressWarnings("unchecked")
	@Transactional
	// @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public QuartzTriggerHandle iseGelmeDurumuTimer(@Expiration Date when, @IntervalCron String interval) {
		hataKonum = "iseGelmeDurumuTimer başladı ";
		hataKonum = "iseGelmeDurumuTimer kontrol ediliyor ";
		if (pdksEntityController != null && !isCalisiyor()) {
			boolean hataGonder = Boolean.FALSE;
			Session session = null;
			try {
				setCalisiyor(Boolean.TRUE);
				boolean zamanTamam = PdksUtil.zamanKontrolDurum();
				// zamanTamam = true;
				if (zamanTamam && PdksUtil.getCanliSunucuDurum()) {
					// logger.error("Ise gelme durumu " + PdksUtil.getCurrentTimeStampStr());
					session = PdksUtil.getSession(entityManager, Boolean.TRUE);
					hataKonum = "Paramatre okunuyor ";
					Parameter parameter = ortakIslemler.getParameter(session, PARAMETER_KEY);
					String value = (parameter != null) ? parameter.getValue() : null;
					hataKonum = "Paramatre okundu ";
					if (value != null) {
						hataGonder = Boolean.TRUE;
						hataKonum = "Zaman kontrolu yapılıyor ";
						Date time = zamanlayici.getDbTime(session);
						boolean zamanDurum = PdksUtil.getTestDurum() == false && PdksUtil.zamanKontrol(PARAMETER_KEY, value, time) && ortakIslemler.getGuncellemeDurum(PdksLog.TABLE_NAME, session);
						// if (!zamanDurum)
						// zamanDurum = PdksUtil.getTestDurum();
						if (zamanDurum)
							iseGelmemeDurumuCalistir(null, session, null, false, true);
					}
				}
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				logger.error("iseGelmeDurumuTimer : " + e.getMessage());
				if (hataGonder)
					try {
						zamanlayici.mailGonder(session, null, "İşe gelme durumu", "İşe gelme durumu kontrolü tamamlanmadı." + e.getMessage() + " ( " + hataKonum + " )", null, Boolean.TRUE);

					} catch (Exception e2) {
						logger.error("iseGelmeDurumuTimer 2 : " + e.getMessage());
					}
			} finally {
				if (session != null)
					session.close();
				setCalisiyor(Boolean.FALSE);
			}

		}
		return null;
	}

	/**
	 * @param tarih
	 * @param session
	 * @param islemYapan
	 * @param manuel
	 * @param mailGonder
	 * @return
	 * @throws Exception
	 */
	public String iseGelmemeDurumuCalistir(Date tarih, Session session, User islemYapan, boolean manuel, boolean mailGonder) throws Exception {
		uyariNot = null;

		if (userYoneticiList == null)
			userYoneticiList = new ArrayList<User>();
		else
			userYoneticiList.clear();
		hataKonum = "iseGelmeDurumu basladı ";

		try {
			iseGelmeDurumu(tarih, islemYapan, manuel, session, mailGonder);
		} catch (Exception e) {
			e.printStackTrace();
			userYoneticiList = null;
		}

		StringBuffer sb = new StringBuffer();
		sb.append("İşe gelme durumu kontrolü tamamlandı.");
		if (userYoneticiList != null && !userYoneticiList.isEmpty()) {
			MailObject mail = new MailObject();
			HashMap fields = new HashMap();
			sb = new StringBuffer();
			sb.append("WITH BUGUN AS ( ");
			sb.append("		select 1 AS ID ");
			sb.append("	),");
			sb.append("	DEP_YONETICI AS (");
			sb.append("		SELECT R.ROLENAME DEP_YONETICI_ROL_ADI FROM " + Role.TABLE_NAME + " R WITH(nolock)");
			sb.append("		WHERE R." + Role.COLUMN_NAME_ROLE_NAME + "='" + Role.TIPI_DEPARTMAN_SUPER_VISOR + "' AND R." + Role.COLUMN_NAME_STATUS + "=1");
			sb.append("	) ");
			sb.append("	SELECT COALESCE(DY.DEP_YONETICI_ROL_ADI,'') DEP_YONETICI_ROL_ADI, GETDATE() AS TARIH FROM BUGUN B ");
			sb.append("	LEFT JOIN DEP_YONETICI DY ON 1=1");
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Object[]> veriList = pdksEntityController.getObjectBySQLList(sb, fields, null);
			boolean departmanYoneticiRolVar = false;
			if (!veriList.isEmpty()) {
				Object[] veri = veriList.get(0);
				departmanYoneticiRolVar = ((String) veri[0]).length() > 0;

			}
			sb = new StringBuffer();
			TreeMap<String, String> map1 = new TreeMap<String, String>();

			logger.info("Toplu mail dosyasi hazirlaniyor in " + PdksUtil.getCurrentTimeStampStr());
			TreeMap<Long, User> userMap = new TreeMap<Long, User>();
			for (Iterator iterator = userYoneticiList.iterator(); iterator.hasNext();) {
				User user = (User) iterator.next();
				List<VardiyaGun> list = user.getPdksPersonel().getPersonelVardiyalari();
				Long key = user.getId();
				if (userMap.containsKey(key)) {
					User user2 = userMap.get(key);
					List<VardiyaGun> list2 = user2.getPdksPersonel().getPersonelVardiyalari();
					for (VardiyaGun vardiyaGun : list) {
						boolean ekle = true;
						for (VardiyaGun vardiyaGun2 : list2) {
							if (vardiyaGun2.getId().equals(vardiyaGun.getId())) {
								ekle = false;
								break;
							}
						}
						if (ekle)
							list2.add(vardiyaGun);
					}
					iterator.remove();
				} else if (list != null && !list.isEmpty())
					userMap.put(key, user);
			}
			List<Dosya> fileList = new ArrayList<Dosya>();
			String pattern = DefaultPasswordGenerator.generate(8);
			boolean hareketExcelGonder = PdksUtil.hasStringValue(hareketExcelGonderDurum);
			for (Iterator iterator = userYoneticiList.iterator(); iterator.hasNext();) {
				User user = (User) iterator.next();
				boolean hareketExcelGonderEkle = false;
				if (hareketExcelGonder)
					hareketExcelGonderEkle = user.isIK() || hareketExcelGonderDurum.equals("M");

				StringBuffer sbUser = new StringBuffer();
				Personel personel = user.getPdksPersonel();
				Sirket sirket = personel.getSirket();
				sbUser.append("<BR/><BR/>" + user.getAdSoyad() + (personel.getGorevTipi() != null ? " ( " + personel.getGorevTipi().getAciklama() + " ) " : " - ") + "<BR/>" + (sirket.getSirketGrup() != null ? sirket.getSirketGrup().getAciklama() : sirket.getAd()));
				StringBuilder unvan = new StringBuilder();
				if (departmanYoneticiRolVar && personel.getEkSaha1() != null)
					unvan.append(personel.getEkSaha1().getAciklama());
				String bolum = personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "";
				String altBolum = personel.getEkSaha4() != null ? personel.getEkSaha4().getAciklama() : "";
				if (PdksUtil.hasStringValue(bolum))
					unvan.append((unvan.length() > 0 ? " - " : "") + bolum);
				if (PdksUtil.isPuantajSorguAltBolumGir()) {
					if (PdksUtil.hasStringValue(altBolum) && !altBolum.equals(bolum))
						unvan.append((unvan.length() > 0 ? " - " : "") + altBolum);
				}
				if (unvan.length() > 0)
					sbUser.append(" [ " + unvan.toString() + " ]");
				unvan = null;
				if (personel != null) {
					List<VardiyaGun> list = personel.getPersonelVardiyalari();
					if (hareketExcelGonderEkle || (list != null && !list.isEmpty())) {
						Workbook wb = new XSSFWorkbook();
						if (mesajIcerikOlustur(user, sbUser, list, map1, wb, session)) {
							String dosyaAdi = "IseGelmeDurum_" + (user.getDepartman().isAdminMi() ? "" : sirket.getAd() + "_") + PdksUtil.convertToDateString(islemTarihi, "yyyyMMdd");
							dosyaAdi += "_" + pattern + "_" + personel.getAdSoyad() + ".xlsx";
							sb.append(sbUser.toString());
							Dosya dosyaExcel = new Dosya();
							dosyaExcel.setDosyaAdi(dosyaAdi);
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							wb.write(baos);
							dosyaExcel.setDosyaIcerik(baos.toByteArray());
							fileList.add(dosyaExcel);
						}
					}
				}
				sbUser = null;

			}
			userMap = null;
			if (!fileList.isEmpty()) {
				int sira = 0;
				int kayitAdet = fileList.size();
				int maxTextLength = String.valueOf(fileList.size()).length();
				for (Dosya dosya : fileList) {
					String dosyaAdi = PdksUtil.replaceAll(dosya.getDosyaAdi(), pattern, PdksUtil.textBaslangicinaKarakterEkle(String.valueOf(kayitAdet - sira), '0', maxTextLength));
					++sira;
					dosya.setDosyaAdi(dosyaAdi);
				}
				String zipDosyaAdi = "IseGelmeDurum_" + PdksUtil.convertToDateString(islemTarihi, "yyyyMMdd") + ".zip";
				File file = ortakIslemler.dosyaZipFileOlustur(zipDosyaAdi, fileList);
				if (file != null && file.exists()) {

					Dosya dosya = ortakIslemler.dosyaFileOlustur(zipDosyaAdi, file, Boolean.TRUE);
					file.deleteOnExit();
					MailFile mailFile = new MailFile();
					mailFile.setIcerik(dosya.getDosyaIcerik());
					mailFile.setDisplayName(zipDosyaAdi);
					mail.getAttachmentFiles().add(mailFile);
				}
				sb.append("<br/>");
				logger.info("Toplu mail dosyasi hazirlaniyor out " + PdksUtil.getCurrentTimeStampStr());
			}
			fileList = null;
			if (mailGonder) {
				List<User> userList = null;
				if (manuel && islemYapan != null) {
					userList = new ArrayList<User>();
					userList.add(islemYapan);
				}
				zamanlayici.mailGonder(session, mail, "İşe gelme durumu", new String(sb), userList, Boolean.TRUE);
				ortakIslemler.gunlukFazlaCalisanlar(session);
			}
		} else
			zamanlayici.mailGonder(session, null, "İşe gelme durumu", "Hatalı giriş bilgisi bulunamadı.", null, Boolean.TRUE);
		sb = null;

		return "";
	}

	public User getUserYonetici() {
		return userYonetici;
	}

	public void setUserYonetici(User userYonetici) {
		this.userYonetici = userYonetici;
	}

	public List<User> getUserIKList() {
		return userIKList;
	}

	public void setUserIKList(ArrayList<User> userIKList) {
		this.userIKList = userIKList;
	}

	public String getHataKonum() {
		return hataKonum;
	}

	public void setHataKonum(String hataKonum) {
		this.hataKonum = hataKonum;
	}

	public static boolean isCalisiyor() {
		return calisiyor;
	}

	public static void setCalisiyor(boolean calisiyor) {
		IseGelmemeUyari.calisiyor = calisiyor;
	}

	public List<User> getUserYoneticiList() {
		return userYoneticiList;
	}

	public void setUserYoneticiList(ArrayList<User> userYoneticiList) {
		this.userYoneticiList = userYoneticiList;
	}

	public boolean isStatuGoster() {
		return statuGoster;
	}

	public void setStatuGoster(boolean statuGoster) {
		this.statuGoster = statuGoster;
	}

	public Tanim getEkSaha1() {
		return ekSaha1;
	}

	public void setEkSaha1(Tanim ekSaha1) {
		this.ekSaha1 = ekSaha1;
	}

	public Tanim getEkSaha2() {
		return ekSaha2;
	}

	public void setEkSaha2(Tanim ekSaha2) {
		this.ekSaha2 = ekSaha2;
	}

	public Tanim getEkSaha3() {
		return ekSaha3;
	}

	public void setEkSaha3(Tanim ekSaha3) {
		this.ekSaha3 = ekSaha3;
	}

	public Tanim getEkSaha4() {
		return ekSaha4;
	}

	public void setEkSaha4(Tanim ekSaha4) {
		this.ekSaha4 = ekSaha4;
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

	public String getEkSahaAlanAdi() {
		return ekSahaAlanAdi;
	}

	public void setEkSahaAlanAdi(String ekSahaAlanAdi) {
		this.ekSahaAlanAdi = ekSahaAlanAdi;
	}

	public int getEkSahaAlanNo() {
		return ekSahaAlanNo;
	}

	public void setEkSahaAlanNo(int ekSahaAlanNo) {
		this.ekSahaAlanNo = ekSahaAlanNo;
	}

	public User getUserUstYonetici() {
		return userUstYonetici;
	}

	public void setUserUstYonetici(User userUstYonetici) {
		this.userUstYonetici = userUstYonetici;
	}

	public boolean isHariciPersonelVar() {
		return hariciPersonelVar;
	}

	public void setHariciPersonelVar(boolean hariciPersonelVar) {
		this.hariciPersonelVar = hariciPersonelVar;
	}

	public boolean isIzinVar() {
		return izinVar;
	}

	public void setIzinVar(boolean izinVar) {
		this.izinVar = izinVar;
	}

	public boolean isHataliHareketVar() {
		return hataliHareketVar;
	}

	public void setHataliHareketVar(boolean hataliHareketVar) {
		this.hataliHareketVar = hataliHareketVar;
	}

	public boolean isTesisVar() {
		return tesisVar;
	}

	public void setTesisVar(boolean tesisVar) {
		this.tesisVar = tesisVar;
	}

}