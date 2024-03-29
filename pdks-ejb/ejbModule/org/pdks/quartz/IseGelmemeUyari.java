package org.pdks.quartz;

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
import org.pdks.entity.HareketKGS;
import org.pdks.entity.Kapi;
import org.pdks.entity.KapiView;
import org.pdks.entity.Liste;
import org.pdks.entity.NoteTipi;
import org.pdks.entity.Notice;
import org.pdks.entity.Parameter;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelGeciciYonetici;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.pdks.security.entity.UserVekalet;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

import com.pdks.webservice.MailObject;
import com.pdks.webservice.MailStatu;

@Name("iseGelmemeUyari")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class IseGelmemeUyari implements Serializable {

	/**
	 * 
	 */
	private static final String PARAMETER_KEY = "uyariMail";
	private static final long serialVersionUID = 6477701530769282426L;
	static Logger logger = Logger.getLogger(IseGelmemeUyari.class);

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

	private String hataKonum, personelNoAciklama, tesisAciklama, bolumAciklama, altBolumAciklama, yoneticiAciklama;

	private boolean statuGoster = Boolean.FALSE, hariciPersonelVar, yoneticiMailGonderme = Boolean.FALSE, izinVar = Boolean.FALSE, tesisVar = Boolean.FALSE, hataliHareketVar = Boolean.FALSE;

	private Tanim ekSaha1, ekSaha2, ekSaha3, ekSaha4;

	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private String ekSahaAlanAdi;
	private int ekSahaAlanNo = 2;
	private List<String> ikMailList = new ArrayList<String>();
	private boolean tesisYetki = false;
	private Notice uyariNot;

	/**
	 * @param sessionx
	 */
	private void fillEkSahaTanim(Session sessionx) {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(sessionx, Boolean.FALSE, Boolean.FALSE);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
		if (ortakIslemler.isTesisDurumu())
			tesisAciklama = ortakIslemler.tesisAciklama();
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
		altBolumAciklama = (String) sonucMap.get("altBolumAciklama");
		personelNoAciklama = ortakIslemler.personelNoAciklama();
		yoneticiAciklama = ortakIslemler.yoneticiAciklama();

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
			// sb.append(" INNER JOIN " + Parameter.TABLE_NAME + " PA ON PA." + Parameter.COLUMN_NAME_ADI + "=:ad  AND PA." + Parameter.COLUMN_NAME_DURUM + "=1 and PA." + Parameter.COLUMN_NAME_DEGER + "=:deger ");
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
		logger.info("iseGelmeDurumu in " + new Date());
		if (!manuel)
			calisiyor = Boolean.TRUE;
		Calendar cal = Calendar.getInstance();
		Date bugun = cal.getTime();
		HashMap map = new HashMap();
		tesisYetki = ortakIslemler.getParameterKey("tesisYetki").equals("1");
		boolean devam = tarih == null;

		if (tarih == null)
			tarih = (Date) bugun.clone();
		else
			devam = bugun.getTime() > tarih.getTime();
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
			map.put("mailTakip=", Boolean.TRUE);
			map.put("yoneticisi<>", null);
			if (islemYapan != null && !islemYapan.isAdmin())
				map.put("sirket.departman.id=", islemYapan.getDepartman().getId());
			List<Personel> personeller = pdksEntityController.getObjectByInnerObjectListInLogic(map, Personel.class);
			if (!personeller.isEmpty()) {
				HashMap<Long, Long> yoneticiler = new HashMap<Long, Long>();
				HashMap<Long, Personel> kgsPerMap = new HashMap<Long, Personel>();
				for (Iterator iterator = personeller.iterator(); iterator.hasNext();) {
					Personel per = (Personel) iterator.next();
					PersonelKGS personelKGS = per.getPersonelKGS();
					if (personelKGS == null || personelKGS.getDurum() == null || personelKGS.getDurum().equals(Boolean.FALSE)) {
						iterator.remove();
						continue;
					}
					Sirket pdksSirket = per.getSirket();
					Personel yonetici = per.getYoneticisi();
					boolean islemDevam = true;
					if (yonetici == null || per.getSicilNo().trim().length() == 0 || per.getSirket() == null)
						islemDevam = false;
					else if (!pdksSirket.getDurum() || !pdksSirket.getPdks())
						islemDevam = false;
					if (!islemDevam) {
						// logger.info(PdksUtil.setTurkishStr(per.getPdksSicilNo() + " - " + per.getAdSoyad()));
						iterator.remove();
						continue;
					}

					yoneticiler.put(yonetici.getId(), yonetici.getId());
					kgsPerMap.put(per.getPersonelKGS().getId(), per);
				}
				if (!kgsPerMap.isEmpty() && !yoneticiler.isEmpty()) {
					// personeller = new ArrayList<AsmPersonel>(karadagPerMap.values());
					map.clear();
					map.put(PdksEntityController.MAP_KEY_MAP, "getPersonelId");
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
					map.put("pdksPersonel.id", new ArrayList<Long>(yoneticiler.values()));
					TreeMap<Long, User> userMap = pdksEntityController.getObjectByInnerObjectMap(map, User.class, Boolean.FALSE);
					TreeMap<String, Tatil> resmiTatilGunleri = ortakIslemler.getTatilGunleri(personeller, oncekiGun, tarih, session);
					Date sonrakiGun = PdksUtil.tariheGunEkleCikar(tarih, 1);
					// Vardiya kayıtları okunuyor
					TreeMap<String, VardiyaGun> vardiyalar = ortakIslemler.getIslemVardiyalar(personeller, oncekiGun, sonrakiGun, Boolean.FALSE, session, Boolean.TRUE);
					try {
						boolean islem = ortakIslemler.getVardiyaHareketIslenecekList(new ArrayList<VardiyaGun>(vardiyalar.values()), tarih, session);
						if (islem)
							vardiyalar = ortakIslemler.getIslemVardiyalar(personeller, oncekiGun, sonrakiGun, Boolean.FALSE, session, Boolean.TRUE);

					} catch (Exception e) {
					}

					// ortakIslemler.fazlaMesaiSaatiAyarla(vardiyalar);
					Date vardiyaBas = null;
					List<VardiyaGun> vardiyaList = new ArrayList<VardiyaGun>(vardiyalar.values());

					HareketKGS arifeCikis = null;
					Date vardiyaBitTar = (Date) tarih.clone();
					// İlk vardiya başlangıç zamanı okunuyor
					HashMap<Long, List<User>> depMail = new HashMap<Long, List<User>>();
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
						map.put("basTarih<=", PdksUtil.tariheGunEkleCikar((Date) tarih.clone(), 1));
						map.put("bitTarih>=", vardiyaBas);
						map.put(PdksEntityController.MAP_KEY_MAP, "getVekaletVerenId");
						TreeMap<Long, UserVekalet> vekaletMap = pdksEntityController.getObjectByInnerObjectMapInLogic(map, UserVekalet.class, Boolean.FALSE);

						map.clear();
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
						map.put("durum=", Boolean.TRUE);
						map.put("basTarih<=", PdksUtil.tariheGunEkleCikar((Date) tarih.clone(), 1));
						map.put("bitTarih>=", vardiyaBas);
						map.put(PdksEntityController.MAP_KEY_MAP, "getPersonelGeciciId");
						TreeMap<Long, PersonelGeciciYonetici> geciciYoneticiMap = pdksEntityController.getObjectByInnerObjectMapInLogic(map, PersonelGeciciYonetici.class, Boolean.FALSE);
						session.clear();
						List<Integer> izinDurumList = new ArrayList<Integer>();
						// izinDurumList.add(PersonelIzin.IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA);
						izinDurumList.add(PersonelIzin.IZIN_DURUMU_REDEDILDI);
						izinDurumList.add(PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL);
						List<Long> perIdList = new ArrayList<Long>();
						for (Long long1 : kgsPerMap.keySet())
							perIdList.add(kgsPerMap.get(long1).getId());
						map.clear();
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
						map.put("baslangicZamani<=", PdksUtil.tariheGunEkleCikar((Date) tarih.clone(), 1));
						map.put("bitisZamani>=", vardiyaBas);
						map.put("izinTipi.bakiyeIzinTipi=", null);
						map.put("izinSahibi.id", perIdList);
						if (izinDurumList.size() > 1)
							map.put("izinDurumu not ", izinDurumList);
						else
							map.put("izinDurumu <> ", PersonelIzin.IZIN_DURUMU_REDEDILDI);
						List<PersonelIzin> izinler = pdksEntityController.getObjectByInnerObjectListInLogic(map, PersonelIzin.class);
						// Hareket kayıtları okunuyor
						// HashMap<Long, ArrayList<HareketKGS>> personelHareketMap = ortakIslemler.fillKaradagPersonelHareketMap(new ArrayList(karadagPerMap.keySet()), vardiyaBas, vardiyaBitTar, session);
						HashMap<Long, ArrayList<HareketKGS>> personelHareketMap = ortakIslemler.fillPersonelKGSHareketMap(new ArrayList(kgsPerMap.keySet()), vardiyaBas, vardiyaBitTar, session);
						HashMap<Long, Personel> yoneticiMap = new HashMap<Long, Personel>();
						// Hareket kayıtları vardiya günlerine işleniyor
						boolean kayitVar = false;
						if ((!izinler.isEmpty() || (personelHareketMap != null && !personelHareketMap.isEmpty())) || (!mailGonder)) {
							TreeMap<Long, List<PersonelIzin>> izinMap = new TreeMap<Long, List<PersonelIzin>>();
							for (PersonelIzin personelIzin : izinler) {
								Long key = personelIzin.getIzinSahibi().getId();
								List<PersonelIzin> list1 = izinMap.containsKey(key) ? izinMap.get(key) : new ArrayList<PersonelIzin>();
								if (list1.isEmpty())
									izinMap.put(key, list1);
								list1.add(personelIzin);
							}
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
								Long perNoId = pdksPersonel.getPersonelKGS().getId();
								boolean hareketVar = personelHareketMap.containsKey(perNoId) && !personelHareketMap.get(perNoId).isEmpty();
								if (!hareketVar) {
									hareketVar = izinMap.containsKey(pdksPersonel.getId());
									if (hareketVar) {
										logger.debug(PdksUtil.setTurkishStr(pdksPersonel.getPdksSicilNo() + " - " + pdksPersonel.getAdSoyad()));
									}
								}
								if (!kayitVar)
									kayitVar = hareketVar;
								boolean calisma = pdksVardiyaGun.getIslemVardiya().isCalisma();
								boolean ekle = calisma;
								if (hareketVar) {
									ekle = vardiyaHareketKontrol(bugun, izinMap, personelHareketMap, pdksVardiyaGun);
									if (ekle && calisma == false)
										ekle = pdksVardiyaGun.getHareketler() != null && !pdksVardiyaGun.getHareketler().isEmpty();
								}
								if (ekle) {
									if (!calisma)
										logger.debug(PdksUtil.setTurkishStr(pdksVardiyaGun.getVardiyaKeyStr() + " " + pdksPersonel.getAdSoyad()));
									Personel yoneticisi = (Personel) pdksPersonel.getYoneticisi().clone();
									if (geciciYoneticiMap.containsKey(pdksVardiyaGun.getPersonel().getId()))
										yoneticisi = (Personel) geciciYoneticiMap.get(pdksVardiyaGun.getPersonel().getId()).getYeniYonetici().getPdksPersonel().clone();
									if (vekaletMap.containsKey(yoneticisi.getId()))
										yoneticisi = (Personel) vekaletMap.get(yoneticisi.getId()).getYeniYonetici().getPdksPersonel().clone();
									if (yoneticisi == null || !yoneticisi.isCalisiyor())
										continue;
									if (yoneticiMap.containsKey(yoneticisi.getId()))
										yoneticisi = yoneticiMap.get(yoneticisi.getId());
									else
										yoneticisi.setPersonelVardiyalari(new ArrayList<VardiyaGun>());

									if (pdksVardiyaGun.getIslemVardiya().getVardiyaBitZaman().getTime() >= bugun.getTime())
										pdksVardiyaGun.setSonCikis(null);
									yoneticisi.getPersonelVardiyalari().add(pdksVardiyaGun);
									String pdksSicilNo = pdksVardiyaGun.getPersonel().getPdksSicilNo();
									List<User> userList = null;
									Long depId = pdksVardiyaGun.getPersonel().getSirket().getDepartman().getId();
									if (depMail.containsKey(depId))
										userList = depMail.get(depId);
									else {
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

									if (hareketPersonelMap.containsKey(pdksSicilNo)) {
										List<VardiyaGun> list = hareketHataliMap.containsKey(pdksSicilNo) ? hareketHataliMap.get(pdksSicilNo) : new ArrayList<VardiyaGun>();
										if (list.isEmpty())
											hareketHataliMap.put(pdksSicilNo, list);
										list.add(pdksVardiyaGun);
									}
									yoneticiMap.put(yoneticisi.getId(), yoneticisi);
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
		logger.info("iseGelmeDurumu out " + new Date());
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
					if (yoneticisiId != null && userMap.containsKey(yoneticisiId))

						yonetici = (User) userMap.get(yoneticisiId).clone();
					eposta = yonetici != null ? yonetici.getEmail() : "";
					List<VardiyaGun> personelVardiyalari = personelYonetici.getPersonelVardiyalari();
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

						for (VardiyaGun pdksVardiyaGun : personelYonetici.getPersonelVardiyalari()) {
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
										for (Iterator iterator2 = mailList.iterator(); iterator2.hasNext();) {
											String mail = (String) iterator2.next();
											User userYonetici = new User();
											userYonetici.setEmail(mail);
											list.add(userYonetici);
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
								logger.info(PdksUtil.setTurkishStr(personelYonetici.getPdksSicilNo() + " " + personelYonetici.getAdSoyad() + " personel veya kullanıcısında sorun var!"));
							continue;
						}

						if (yoneticiMailGonderme == false)
							user.setStaffId(yonetici.getEmail());

						if (islemYapan != null) {
							eposta = islemYapan.getEmail();
						}

						userYoneticiList.add(user);
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

									logger.info(PdksUtil.setTurkishStr(userYonetici.getPdksPersonel().getSirket().getAd() + " " + user.getAdSoyad() + " " + eposta + " iseGelisUyariMail mesaj gönderildi! "));
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
				HashMap fields = new HashMap();
				boolean sort = false;
				for (String email : mailPersonelMap.keySet()) {
					personelVardiyalari.clear();
					logger.debug(email);
					User user = null;
					fields.clear();
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					fields.put("email", email);
					user = (User) pdksEntityController.getObjectByInnerObject(fields, User.class);
					if (user == null || !user.isDurum()) {
						user = bosYonetici;
						user.setEmail(email);
					} else if (!user.getPdksPersonel().isCalisiyor()) {
						logger.error(PdksUtil.setTurkishStr(user.getAdSoyad() + " MAİL GÖNDERİLEMİYOR!"));
						continue;
					}
					userYonetici = (User) user.clone();
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
								logger.info(PdksUtil.setTurkishStr(userYonetici.getPdksPersonel().getSirket().getAd() + " " + userYonetici.getAdSoyad() + " " + userYonetici.getEmail() + " iseGelisUyariMail mesaj gönderildi! "));
							userYoneticiList.add(userYonetici);
							sort = true;
						} catch (Exception ex) {
							if (islemYapan != null)
								ex.printStackTrace();
							logger.error(email + " yoneticiyeMailGonder : " + ex.getMessage());

						}

					}
				}
				if (sort)
					userYoneticiList = (ArrayList<User>) PdksUtil.sortObjectStringAlanList(userYoneticiList, "getAdSoyad", null);

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
			if (list != null && !list.isEmpty()) {
				mail.setSubject("Giriş-Çıkış problemli personeller");
				StringBuffer sb = new StringBuffer();
				sb.append("<p>Sayın " + userYonetici.getAdSoyad() + "</p>");
				sb.append("<p>Aşağıdaki personel giriş çıkışlarında problem vardır.</p>");
				sb.append("<p></p>");
				sb.append("<p>Saygılarımla,</p>");
				if (mesajIcerikOlustur(userYonetici, sb, list, null, session)) {
					if (uyariNot != null)
						sb.append(uyariNot.getValue());
					mail.setBody(sb.toString());
					try {
						if (tesisYetki && tesisVar)
							mail.getCcList().clear();
						mailSatu = ortakIslemler.mailSoapServisGonder(true, mail, renderer, renderAdres, session);
					} catch (Exception e) {
						logger.error(e);
						e.printStackTrace();
					}
				}

			}
		}

		return mailSatu;
	}

	/**
	 * @param user
	 * @param sb
	 * @param list
	 * @param session
	 */
	private boolean mesajIcerikOlustur(User user, StringBuffer sb, List<VardiyaGun> list, TreeMap<String, String> map1, Session session) {
		// if (user == null || user.getPdksPersonel() == null || user.getPersonelId().longValue() != 1517)
		// return false;
		boolean mesajGonder = false;
		TreeMap<String, List<VardiyaGun>> sirketParcalaMap = new TreeMap<String, List<VardiyaGun>>();
		List<Liste> listeler = new ArrayList<Liste>();
		List<VardiyaGun> yeniList = new ArrayList<VardiyaGun>();
		List<Long> tesisList = null;
		if (tesisYetki && session != null) {
			ortakIslemler.setUserTesisler(user, session);
			if (user.getYetkiliTesisler() != null) {
				tesisList = new ArrayList<Long>();
				for (Tanim tesis : user.getYetkiliTesisler())
					tesisList.add(tesis.getId());

			}

		}

		for (VardiyaGun vardiyaGun : list) {
			Personel personel = vardiyaGun.getPersonel();
			if (map1 != null) {
				String vardiyaKey = user.getId() + "_" + vardiyaGun.getVardiyaKeyStr();
				if (map1.containsKey(vardiyaKey))
					continue;
				map1.put(vardiyaKey, vardiyaKey);
			}

			Sirket sirket = personel.getSirket();
			Tanim tesis = sirket.isTesisDurumu() ? personel.getTesis() : null;
			if (tesisList != null && tesis != null && !tesisList.contains(tesis.getId()))
				continue;
			String key = (sirket.getSirketGrup() == null ? sirket.getAd() : sirket.getSirketGrup().getAciklama()) + (tesis != null ? "_" + tesis.getAciklama() : "");
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
			for (Liste liste : listeler) {
				List<VardiyaGun> sirketSubeList = PdksUtil.sortObjectStringAlanList((List<VardiyaGun>) liste.getValue(), "getSortBolumKey", null);
				boolean hataliHareketGundeVar = Boolean.FALSE, izinGirisVar = Boolean.FALSE, hariciPersonelPlandaVar = Boolean.FALSE, altBolumVar = Boolean.FALSE, altBolumDurum = PdksUtil.isPuantajSorguAltBolumGir();
				HashMap<Long, Tanim> bolumMap = new HashMap<Long, Tanim>();
				HashMap<String, Tanim> altBolumMap = new HashMap<String, Tanim>();
				for (VardiyaGun vardiya : sirketSubeList) {
					Personel personel = vardiya.getPersonel();
					Tanim bolum = personel != null && personel.getEkSaha3() != null ? personel.getEkSaha3() : new Tanim(0L);
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
							hariciPersonelPlandaVar = !user.getPdksPersonel().getId().equals(personel.getPdksYonetici().getId());
						} catch (Exception e) {

						}
					}
					if (!hataliHareketGundeVar)
						hataliHareketGundeVar = vardiya.isHareketHatali() && vardiya.getHareketler() != null && !vardiya.getHareketler().isEmpty();
					if (!izinGirisVar) {
						izinGirisVar = vardiya.getIzin() != null;
					}

				}
				if (altBolumMap.size() < 2)
					altBolumVar = false;
				boolean bolumVar = bolumMap.size() > 1;
				mesajGonder = true;
				Personel sirketPersonel = sirketSubeList.get(0).getPersonel();
				Sirket sirket = sirketPersonel.getSirket();
				Tanim tesis = sirket.isTesisDurumu() ? sirketPersonel.getTesis() : null;
				Tanim bolum = bolumMap.size() == 1 ? new ArrayList<Tanim>(bolumMap.values()).get(0) : null;
				Tanim altBolum = altBolumMap.size() == 1 ? new ArrayList<Tanim>(altBolumMap.values()).get(0) : null;
				if (bolum != null && altBolum != null && !PdksUtil.isStrDegisti(bolum.getAciklama(), altBolum.getAciklama()))
					altBolum = null;
				String baslik = (sirket.getSirketGrup() == null ? sirket.getAd() : sirket.getSirketGrup().getAciklama()) + (tesis != null ? " " + tesis.getAciklama() + " " + tesisAciklama.toLowerCase(PdksUtil.TR_LOCALE) : "");
				baslik += (bolum != null && bolum.getId() > 0L ? " " + bolum.getAciklama() + " " + bolumAciklama.toLowerCase(PdksUtil.TR_LOCALE) : "");
				baslik += (altBolum != null && altBolum.getId() > 0L ? " " + altBolum.getAciklama() + " " + altBolumAciklama.toLowerCase(PdksUtil.TR_LOCALE) : "");
				sb.append("<H3>" + PdksUtil.replaceAllManuel(baslik, "  ", " ") + "</H3>");
				sb.append("<TABLE class=\"mars\" style=\"border: solid 1px\" cellpadding=\"5\" cellspacing=\"0\"><THEAD> <TR>");
				if (hariciPersonelPlandaVar)
					sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>" + yoneticiAciklama + "</b></TH>");
				if (bolumVar)
					sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>" + bolumAciklama + "</b></TH>");
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
				boolean renk = true;
				for (Iterator iterator2 = sirketSubeList.iterator(); iterator2.hasNext();) {
					VardiyaGun vg = (VardiyaGun) iterator2.next();
					Personel personel = vg.getPersonel();
					sb.append("<TR class=\"" + (renk ? "odd" : "even") + "\">");
					renk = !renk;
					if (hariciPersonelPlandaVar)
						sb.append("<td nowrap style=\"border: 1px solid;\">" + (personel.getPdksYonetici() != null ? personel.getPdksYonetici().getAdSoyad() : "") + "</td>");
					if (bolumVar)
						sb.append("<td nowrap style=\"border: 1px solid;\">" + (personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "") + "</td>");
					if (altBolumVar)
						sb.append("<td nowrap style=\"border: 1px solid;\">" + (personel.getEkSaha4() != null ? personel.getEkSaha4().getAciklama() : "") + "</td>");
					sb.append("<td nowrap style=\"border: 1px solid;\">" + personel.getAdSoyad() + "</td>");
					sb.append("<td align=\"center\" style=\"border: 1px solid;\">" + personel.getSicilNo() + "</td>");
					sb.append("<td align=\"center\" style=\"border: 1px solid;\">" + vg.getVardiyaZamanAdi() + "</td>");
					sb.append("<td align=\"center\" style=\"border: 1px solid;\">" + (vg.getIlkGiris() != null ? user.getTarihFormatla(vg.getIlkGiris().getOrjinalZaman(), PdksUtil.getDateFormat() + " H:mm") : "") + "</td>");
					sb.append("<td align=\"center\" style=\"border: 1px solid;\">" + (vg.getSonCikis() != null ? user.getTarihFormatla(vg.getSonCikis().getOrjinalZaman(), PdksUtil.getDateFormat() + " H:mm") : "") + "</td>");
					if (hataliHareketGundeVar) {
						sb.append("<td align=\"center\" style=\"border: 1px solid;\">");
						if (vg.getHareketler() != null && !vg.getHareketler().isEmpty()) {
							sb.append("<TABLE>");
							for (HareketKGS hareketKGS : vg.getHareketler()) {
								sb.append("<TR><td nowrap>" + hareketKGS.getKapiView().getAciklama() + "</td>");
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
							sb.append("<td style=\"border: 1px solid;\">" + aciklama + "</td>");
						}

					}

					sb.append("</TR>");
				}
				sb.append("</TBODY></TABLE><BR/><BR/>");
			}
		}
		user.getPdksPersonel().setPersonelVardiyalari(yeniList);
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
	 * @param izinler
	 * @param personelHareketMap
	 * @param pdksVardiyaGun
	 * @return
	 */
	private boolean vardiyaHareketKontrol(Date bugun, TreeMap<Long, List<PersonelIzin>> izinMap, HashMap<Long, ArrayList<HareketKGS>> personelHareketMap, VardiyaGun pdksVardiyaGun) {
		pdksVardiyaGun.setIzin(null);

		Vardiya islemVardiya = pdksVardiyaGun.getIslemVardiya();
		boolean ekle = islemVardiya != null && bugun.getTime() > islemVardiya.getVardiyaTelorans2BasZaman().getTime();
		if (ekle) {
			Long perNoId = pdksVardiyaGun.getPersonel().getPersonelKGS().getId();
			boolean baslangicTatil = Boolean.FALSE, bitisTatil = Boolean.FALSE;// islemVardiya.getVardiyaTelorans1BitZaman().getTime()
			// <=
			// bugun.getTime();
			if (izinMap.containsKey(pdksVardiyaGun.getPersonel().getId())) {
				List<PersonelIzin> izinler = izinMap.get(pdksVardiyaGun.getPersonel().getId());
				for (PersonelIzin personelIzin : izinler) {
					if (!personelIzin.getIzinSahibi().getId().equals(pdksVardiyaGun.getPersonel().getId()))
						continue;
					PersonelIzin izin = ortakIslemler.setIzinDurum(pdksVardiyaGun, personelIzin);
					if (izin != null) {
						ekle = pdksVardiyaGun.getHareketler() != null && !pdksVardiyaGun.getHareketler().isEmpty();
						pdksVardiyaGun.setIzin(izin);
					}

				}
			}
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
				if (pdksVardiyaGun.addHareket(hareket, Boolean.TRUE))
					iterator.remove();
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
					// logger.error("Ise gelme durumu " + new Date());
					session = PdksUtil.getSession(entityManager, Boolean.TRUE);
					hataKonum = "Paramatre okunuyor ";
					Parameter parameter = ortakIslemler.getParameter(session, PARAMETER_KEY);
					String value = (parameter != null) ? parameter.getValue() : null;
					hataKonum = "Paramatre okundu ";
					if (value != null) {
						hataGonder = Boolean.TRUE;
						hataKonum = "Zaman kontrolu yapılıyor ";
						Date time = zamanlayici.getDbTime(session);
						boolean zamanDurum = PdksUtil.getTestDurum() == false && PdksUtil.zamanKontrol(PARAMETER_KEY, value, time) && ortakIslemler.getGuncellemeDurum(session);
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
						zamanlayici.mailGonder(session, "İşe gelme durumu", "İşe gelme durumu kontrolü tamamlanmadı." + e.getMessage() + " ( " + hataKonum + " )", null, Boolean.TRUE);

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
			HashMap fields = new HashMap();
			sb = new StringBuffer();
			sb.append("WITH BUGUN AS ( ");
			sb.append("		select 1 AS ID ");
			sb.append("	),");
			sb.append("	DEP_YONETICI AS (");
			sb.append("		SELECT R.ROLENAME DEP_YONETICI_ROL_ADI FROM " + Role.TABLE_NAME + " R");
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
			for (Iterator iterator = userYoneticiList.iterator(); iterator.hasNext();) {
				User user = (User) iterator.next();
				StringBuffer sbUser = new StringBuffer();
				Sirket sirket = user.getPdksPersonel().getSirket();
				sbUser.append("<BR/><BR/>" + user.getAdSoyad() + (user.getPdksPersonel().getGorevTipi() != null ? " ( " + user.getPdksPersonel().getGorevTipi().getAciklama() + " ) " : " - ") + "<BR/>" + (sirket.getSirketGrup() != null ? sirket.getSirketGrup().getAciklama() : sirket.getAd()));
				StringBuilder unvan = new StringBuilder();
				if (departmanYoneticiRolVar && user.getPdksPersonel().getEkSaha1() != null)
					unvan.append(user.getPdksPersonel().getEkSaha1().getAciklama());
				String bolum = user.getPdksPersonel().getEkSaha3() != null ? user.getPdksPersonel().getEkSaha3().getAciklama() : "";
				String altBolum = user.getPdksPersonel().getEkSaha4() != null ? user.getPdksPersonel().getEkSaha4().getAciklama() : "";
				if (!bolum.equals(""))
					unvan.append((unvan.length() > 0 ? " - " : "") + bolum);
				if (PdksUtil.isPuantajSorguAltBolumGir()) {
					if (!altBolum.equals("") && !altBolum.equals(bolum))
						unvan.append((unvan.length() > 0 ? " - " : "") + altBolum);
				}
				if (unvan.length() > 0)
					sbUser.append(" [ " + unvan.toString() + " ]");
				unvan = null;
				if (user.getPdksPersonel() != null && user.getPdksPersonel().getPersonelVardiyalari() != null && !user.getPdksPersonel().getPersonelVardiyalari().isEmpty()) {
					List<VardiyaGun> list = user.getPdksPersonel().getPersonelVardiyalari();
					if (mesajIcerikOlustur(user, sbUser, list, map1, session)) {
						sb.append(sbUser.toString());
					} else
						iterator.remove();
				}
				sbUser = null;

			}
			sb.append("<br/>");
			if (mailGonder) {
				List<User> userList = null;
				if (manuel && islemYapan != null) {
					userList = new ArrayList<User>();
					userList.add(islemYapan);
				}
				zamanlayici.mailGonder(session, "İşe gelme durumu", new String(sb), userList, Boolean.TRUE);

			}
		}
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