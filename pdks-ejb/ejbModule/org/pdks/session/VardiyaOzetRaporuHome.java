package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
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
import org.pdks.entity.CalismaModeli;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Liste;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.security.entity.User;

@Name("vardiyaOzetRaporuHome")
public class VardiyaOzetRaporuHome extends EntityHome<VardiyaGun> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4025960383128256337L;
	static Logger logger = Logger.getLogger(VardiyaOzetRaporuHome.class);

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
	List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
	List<Personel> personelList = new ArrayList<Personel>();

	List<HareketKGS> hareketList = new ArrayList<HareketKGS>();
	private List<VardiyaGun> izinVardiyaGunList = new ArrayList<VardiyaGun>(), calismayanVardiyaGunList = new ArrayList<VardiyaGun>();
	private List<VardiyaGun> gecGelenVardiyaGunList = new ArrayList<VardiyaGun>(), erkenCikanVardiyaGunList = new ArrayList<VardiyaGun>(), gelmeyenVardiyaGunList = new ArrayList<VardiyaGun>();
	private List<AylikPuantaj> puantajList = new ArrayList<AylikPuantaj>();
	private AramaSecenekleri aramaSecenekleri = null;
	private List<Vardiya> vardiyaList = new ArrayList<Vardiya>();
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private Tanim ekSaha4Tanim;
	private Font fontRed;
	private String bolumAciklama, sicilNo, altBolumGrupGoster = "ABG";
	private boolean tesisDurum = false, digerTesisDurum = false;
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
		sicilNo = "";
		Date dateBas = PdksUtil.buGun();
		setDate(dateBas);
		if (aramaSecenekleri.getDepartmanId() == null) {
			aramaSecenekleri.setDepartman(authenticatedUser.getDepartman());
			aramaSecenekleri.setDepartmanId(authenticatedUser.getDepartman().getId());
		}
		tesisDurum = false;
		digerTesisDurum = false;
		fillSirketList();
	}

	private void clearVardiyaList() {
		izinVardiyaGunList.clear();
		gecGelenVardiyaGunList.clear();
		erkenCikanVardiyaGunList.clear();
		gelmeyenVardiyaGunList.clear();
		calismayanVardiyaGunList.clear();
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
		Date oncekiGun = PdksUtil.tariheGunEkleCikar(date, -1);
		HashMap map = new HashMap();
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

		List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
		List<HareketKGS> kgsList = new ArrayList<HareketKGS>();
		Date tarih1 = null;
		Date tarih2 = null;
		List<Personel> kgsIptaller = new ArrayList<Personel>();
		for (Iterator iterator = tumPersoneller.iterator(); iterator.hasNext();) {
			Personel pdksPersonel = (Personel) iterator.next();
			PersonelKGS personelKGS = pdksPersonel.getPersonelKGS();
			if (personelKGS == null || personelKGS.getDurum() == null || personelKGS.getDurum().equals(Boolean.FALSE)) {
				kgsIptaller.add(pdksPersonel);
			}
		}

		if (!tumPersoneller.isEmpty()) {
			Date basTarih = PdksUtil.tariheGunEkleCikar(date, -1);
			Date bitTarih = date;
			List<VardiyaGun> vardiyaGunList = getVardiyalariOku(oncekiGun, tumPersoneller, basTarih, bitTarih);
			Collections.reverse(vardiyaList);
			// butun personeller icin hareket cekerken bu en kucuk tarih ile en
			// buyuk tarih araligini kullanacaktir
			// bu araliktaki tum hareketleri cekecektir.

			try {
				boolean islem = ortakIslemler.getVardiyaHareketIslenecekList(vardiyaGunList, date, session);
				if (islem)
					vardiyaGunList = getVardiyalariOku(oncekiGun, tumPersoneller, date, date);
			} catch (Exception e) {
			}
			Date bugun = new Date();
			List<Long> perIdList = new ArrayList<Long>();

			HashMap<Long, List<PersonelIzin>> izinMap = new HashMap<Long, List<PersonelIzin>>();
			try {
				if (!vardiyaGunList.isEmpty()) {
					HashMap parametreMap = new HashMap();
					parametreMap.put("bakiyeIzinTipi", null);
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<IzinTipi> izinler = pdksEntityController.getObjectByInnerObjectList(parametreMap, IzinTipi.class);
					if (!izinler.isEmpty()) {
						HashMap parametreMap2 = new HashMap();
						parametreMap2.put("baslangicZamani<=", PdksUtil.tariheGunEkleCikar(bitTarih, 1));
						parametreMap2.put("bitisZamani>=", PdksUtil.tariheGunEkleCikar(basTarih, -1));
						parametreMap2.put("izinTipi", izinler);
						parametreMap2.put("izinSahibi", tumPersoneller.clone());
						if (session != null)
							parametreMap2.put(PdksEntityController.MAP_KEY_SESSION, session);
						izinList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap2, PersonelIzin.class);
						parametreMap2 = null;
					} else
						izinList = new ArrayList<PersonelIzin>();
					for (PersonelIzin izin : izinList) {
						Long perId = izin.getIzinSahibi().getId();
						List<PersonelIzin> list = izinMap.containsKey(perId) ? izinMap.get(perId) : new ArrayList<PersonelIzin>();
						if (list.isEmpty())
							izinMap.put(perId, list);
						list.add(izin);
					}
					izinler = null;
				}
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				logger.debug(e.getMessage());
			}

			for (Iterator iterator = vardiyaGunList.iterator(); iterator.hasNext();) {
				VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator.next();
				Vardiya islemVardiya = pdksVardiyaGun.getIslemVardiya(), vardiya = pdksVardiyaGun.getVardiya();
				Personel personel = pdksVardiyaGun.getPersonel();
				Long personelId = personel.getId();
				if (vardiya.isCalisma()) {
					if (izinMap.containsKey(personelId)) {
						List<PersonelIzin> perIzinList = izinMap.get(personelId);
						for (Iterator iterator2 = perIzinList.iterator(); iterator2.hasNext();) {
							PersonelIzin personelIzin = (PersonelIzin) iterator2.next();
							ortakIslemler.setIzinDurum(pdksVardiyaGun, personelIzin);
							if (pdksVardiyaGun.getIzin() != null) {
								iterator2.remove();
								break;
							}

						}

					}
				}
				// boolean sil = false;
				// if (gunDurum == 1 || islemVardiya == null || vardiya.getId() == null || perIdList.contains(personelId)) {
				// sil = true;
				// } else if (islemVardiya.isCalisma() == false) {
				// sil = PdksUtil.tarihKarsilastirNumeric(pdksVardiyaGun.getVardiyaDate(), date) != 0;
				// } else {
				// if (pdksVardiyaGun.getVardiyaDate().before(date)) {
				// if (!(islemVardiya.getBitSaat() < islemVardiya.getBasSaat() && gunDurum == 0) || pdksVardiyaGun.getIzin() != null)
				// sil = true;
				//
				// } else {
				// if (islemVardiya.getBitSaat() < islemVardiya.getBasSaat() && gunDurum == 0 && bugun.before(islemVardiya.getVardiyaBasZaman()))
				// sil = true;
				// }
				// }
				// if (pdksVardiyaGun.getIzin() != null && sil) {
				// sil = !PdksUtil.tarihKarsilastir(pdksVardiyaGun.getVardiyaDate(), date);
				// }
				// if (sil) {
				// iterator.remove();
				// continue;
				// } else if (islemVardiya.isCalisma())
				// logger.debug(pdksVardiyaGun.getVardiyaDateStr() + " " + islemVardiya.getAdi());
				perIdList.add(personelId);
				if (islemVardiya.isCalisma()) {
					if (tarih1 == null || pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans1BasZaman().getTime() < tarih1.getTime())
						tarih1 = pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans1BasZaman();

					if (tarih2 == null || pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans2BitZaman().getTime() > tarih2.getTime())
						tarih2 = pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans2BitZaman();
				} else if (islemVardiya.isIzin()) {
					if (tarih1 == null)
						tarih1 = date;

					if (tarih2 == null)
						tarih2 = PdksUtil.tariheGunEkleCikar(date, 1);
				}
			}
			if (!vardiyaGunList.isEmpty()) {

				if (vardiyaGunList.size() == 1) {
					Vardiya islemVardiya = vardiyaGunList.get(0).getIslemVardiya();
					if (!islemVardiya.isCalisma()) {
						if (tarih1 == null)
							tarih1 = islemVardiya.getVardiyaTelorans1BasZaman();
						if (tarih2 == null)
							tarih2 = islemVardiya.getVardiyaTelorans2BitZaman();
					}

				}
				if (tarih1 != null && tarih2 != null && !vardiyaGunList.isEmpty()) {

					List<Long> kapiIdler = ortakIslemler.getPdksDonemselKapiIdler(tarih1, tarih2, session);
					if (kapiIdler != null && !kapiIdler.isEmpty())
						kgsList = ortakIslemler.getPdksHareketBilgileri(Boolean.TRUE, kapiIdler, (List<Personel>) tumPersoneller.clone(), PdksUtil.tariheGunEkleCikar(tarih1, -1), PdksUtil.tariheGunEkleCikar(tarih2, 1), HareketKGS.class, session);
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
						TreeMap<String, Liste> veriMap = new TreeMap<String, Liste>();
						TreeMap<Long, Vardiya> vardiyaDataMap = new TreeMap<Long, Vardiya>();
						TreeMap<String, VardiyaGun> vardiyaGunDataMap = new TreeMap<String, VardiyaGun>();
						List<VardiyaGun> fmiList = new ArrayList<VardiyaGun>();
						boolean fmiVardiyaOzetRapor = ortakIslemler.getParameterKey("fmiVardiyaOzetRapor").equals("1") || authenticatedUser.isAdmin();
						List<VardiyaGun> kartBastMayanList = new ArrayList<VardiyaGun>();
						ekSaha4Tanim = ortakIslemler.getEkSaha4(null, aramaSecenekleri.getSirketId(), session);
						boolean altBolumVar = false;
						ortakIslemler.sonrakiGunVardiyalariAyikla(null, vardiyaGunList, session);
						for (Iterator iterator = vardiyaGunList.iterator(); iterator.hasNext();) {
							VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator.next();
							VardiyaGun vardiyaGun = (VardiyaGun) pdksVardiyaGun.clone();
							if (vardiyaGun.getVardiyaDate().getTime() != date.getTime())
								continue;

							Vardiya vardiya = vardiyaGun.getIslemVardiya();

							Personel personel = vardiyaGun.getPersonel();

							vardiyaGun.setHareketler(null);
							vardiyaGun.setGirisHareketleri(null);
							vardiyaGun.setCikisHareketleri(null);
							vardiyaGun.setIzin(null);
							vardiyaGun.setIzinler(null);
							vardiyaGun.setIlkGiris(null);
							vardiyaGun.setSonCikis(null);

							Date erkenCikmaZamani = vardiya.getVardiyaTelorans1BitZaman();
							Date erkenGelmeZamani = vardiya.getVardiyaTelorans1BasZaman();
							Date gecGelmeZamani = vardiya.getVardiyaTelorans2BasZaman();
							vardiyaDataMap.put(vardiya.getId(), vardiya);

							Long perKGSId = vardiyaGun.getPersonel().getPersonelKGS().getId();
							List<HareketKGS> list = hMap.containsKey(perKGSId) ? hMap.get(perKGSId) : new ArrayList<HareketKGS>();
							for (Iterator iterator1 = list.iterator(); iterator1.hasNext();) {
								HareketKGS kgsHareket = (HareketKGS) iterator1.next();
								if (vardiyaGun.addHareket(kgsHareket, Boolean.FALSE))
									iterator1.remove();

							}

							boolean gecGeldi = vardiya.isCalisma(), erkenCikti = vardiya.isCalisma();
							boolean gelmedi = vardiya.isCalisma() && (vardiyaGun.getHareketler() == null || vardiyaGun.getHareketler().isEmpty());
							List<PersonelIzin> izinler = pdksVardiyaGun.getIzinler();

							if (izinler != null && !izinler.isEmpty())
								gelmedi = false;
							HareketKGS ilkGiris = null, sonCikis = null;

							Integer girisAdet = null, cikisAdet = null;
							if (vardiyaGun.getGirisHareketleri() != null && !vardiyaGun.getGirisHareketleri().isEmpty()) {
								List<HareketKGS> hareketler = vardiyaGun.getGirisHareketleri();
								girisAdet = hareketler.size();
								if (girisAdet == 1) {
									HareketKGS hareketKGS = hareketler.get(0);
									Date zaman = hareketKGS.getZaman();
									gecGeldi = zaman.after(gecGelmeZamani);
									if (gecGeldi)
										ilkGiris = hareketKGS;
								} else {
									gecGeldi = false;
									for (HareketKGS hareketKGS : hareketler) {
										Date zaman = hareketKGS.getZaman();
										if (zaman.before(erkenGelmeZamani))
											continue;
										gecGeldi = zaman.after(gecGelmeZamani);
										if (gecGeldi)
											ilkGiris = hareketKGS;
										break;
									}
								}

							}
							if (girisAdet != null && vardiyaGun.getCikisHareketleri() != null && !vardiyaGun.getCikisHareketleri().isEmpty()) {
								List<HareketKGS> hareketler = vardiyaGun.getCikisHareketleri();
								cikisAdet = hareketler.size();
								if (girisAdet == cikisAdet) {
									if (cikisAdet == 1) {
										HareketKGS hareketKGS = hareketler.get(0);
										Date zaman = hareketKGS.getZaman();
										erkenCikti = zaman.before(erkenCikmaZamani);
										if (erkenCikti)
											sonCikis = hareketKGS;
									} else {
										erkenCikti = false;
										Collections.reverse(hareketler);
										for (HareketKGS hareketKGS : hareketler) {
											Date zaman = hareketKGS.getZaman();
											erkenCikti = zaman.before(erkenCikmaZamani);
											if (erkenCikti)
												sonCikis = hareketKGS;
											break;
										}

									}
								}
							}

							if (pdksVardiyaGun.isIzinli()) {
								izinVardiyaGunList.add(pdksVardiyaGun);
								gelmedi = false;
								gecGeldi = false;
								erkenCikti = false;
							} else {
								if (pdksVardiyaGun.getVardiya().isCalisma() == false) {
									if (girisAdet == null) {

										calismayanVardiyaGunList.add(pdksVardiyaGun);
									}
								} else if (gelmedi) {
									gelmedi = vardiya.getVardiyaTelorans2BasZaman().before(bugun);
								}
							}
							Long calismaSekliId = pdksVardiyaGun.getCalismaModeli() != null ? pdksVardiyaGun.getCalismaModeli().getId() : 0L;
							Tanim bolum = personel.getEkSaha3(), altBolum = null;
							Long bolumId = bolum != null ? bolum.getId() : 0L, altBolumId = 0L;
							if (ekSaha4Tanim != null && bolumId.longValue() > 0L && bolum.getKodu().equals(altBolumGrupGoster)) {
								altBolum = personel.getEkSaha4();
								altBolumId = altBolum != null ? altBolum.getId() : 0L;
								altBolumVar = true;
							}
							String veriKey = calismaSekliId + "_" + bolumId + "_" + altBolumId;
							String key = veriKey + "_" + vardiya.getId();

							Liste liste = null;

							AylikPuantaj aylikPuantaj = null;
							if (veriMap.containsKey(veriKey)) {
								liste = veriMap.get(veriKey);
								aylikPuantaj = (AylikPuantaj) liste.getValue();
							} else {
								aylikPuantaj = new AylikPuantaj();
								aylikPuantaj.setIzinSuresi(0.0d);
								liste = new Liste(veriKey, aylikPuantaj);
								String str = pdksVardiyaGun.getCalismaModeli() != null ? pdksVardiyaGun.getCalismaModeli().getAciklama() : "";
								liste.setSelected(str + " " + (bolum != null ? bolum.getAciklama() : "") + (altBolum != null ? " " + altBolum.getAciklama() : ""));
								Personel pdksPersonel = (Personel) personel.clone();
								pdksPersonel.setCalismaModeli(pdksVardiyaGun.getCalismaModeli());
								pdksPersonel.setId(null);
								pdksPersonel.setAd(bolum != null ? bolum.getAciklama() : "");
								pdksPersonel.setSoyad(altBolum != null && altBolumId != null && altBolumId.longValue() > 0L ? " " + altBolum.getAciklama() : "");
								if (bolum == null)
									pdksPersonel.setEkSaha3(new Tanim());
								if (pdksPersonel.getCalismaModeli() == null)
									pdksPersonel.setCalismaModeli(new CalismaModeli());
								aylikPuantaj.setPdksPersonel(pdksPersonel);
								aylikPuantaj.setCalismaModeli(pdksVardiyaGun.getCalismaModeli());
								veriMap.put(veriKey, liste);
							}
							double adet = 1.0d;
							if (pdksVardiyaGun.isIzinli()) {
								aylikPuantaj.setIzinSuresi(adet + aylikPuantaj.getIzinSuresi());
								adet = 0.0d;
							}
							if (adet > 0.0d) {
								VardiyaGun vardiyaGun2 = null;
								if (vardiyaGunDataMap.containsKey(key)) {
									vardiyaGun2 = vardiyaGunDataMap.get(key);
									vardiyaGun2.setCalismaSuresi(adet + vardiyaGun2.getCalismaSuresi());
								} else {
									vardiyaGun2 = new VardiyaGun(aylikPuantaj.getPdksPersonel(), vardiyaGun.getVardiya(), vardiyaGun.getVardiyaDate());
									vardiyaGun2.setCalismaSuresi(adet);
									vardiyaGunDataMap.put(key, vardiyaGun2);
								}
							}
							if (!gelmedi) {
								if (izinler != null && (gecGeldi || erkenCikti)) {
									for (PersonelIzin personelIzin : izinler) {
										if (erkenCikti)
											erkenCikti = !(personelIzin.getBaslangicZamani().getTime() <= erkenCikmaZamani.getTime() && personelIzin.getBitisZamani().getTime() >= erkenCikmaZamani.getTime());
										if (gecGeldi)
											gecGeldi = !(personelIzin.getBaslangicZamani().getTime() <= gecGelmeZamani.getTime() && personelIzin.getBitisZamani().getTime() >= gecGelmeZamani.getTime());

									}
								}
							} else if (vardiya.isCalisma()) {
								gelmeyenVardiyaGunList.add(vardiyaGun);
								if (personel.getMailTakip() == null || !personel.getMailTakip())

									kartBastMayanList.add(vardiyaGun);

							}

							if (gecGeldi) {
								vardiyaGun.setIlkGiris(ilkGiris);
								if (ilkGiris != null)
									gecGelenVardiyaGunList.add(vardiyaGun);

							}

							if (erkenCikti) {
								vardiyaGun.setSonCikis(sonCikis);
								if (sonCikis != null)
									erkenCikanVardiyaGunList.add(vardiyaGun);

							}
							if (fmiVardiyaOzetRapor && vardiya.isFMI() && (vardiyaGun.getHareketler() == null || vardiyaGun.getHareketler().isEmpty()))
								fmiList.add(vardiyaGun);
						}
						if (!altBolumVar)
							ekSaha4Tanim = null;
						if (!veriMap.isEmpty()) {
							List<Liste> list = new ArrayList(veriMap.values());
							list = PdksUtil.sortObjectStringAlanList(list, "getSelected", null);
							vardiyaList = new ArrayList(vardiyaDataMap.values());
							List<Vardiya> vardiyaCalismayanList = new ArrayList<Vardiya>();
							for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
								Vardiya vardiya = (Vardiya) iterator.next();
								if (vardiya.isIzin()) {
									iterator.remove();
								} else if (vardiya.isCalisma() == false) {
									vardiyaCalismayanList.add(vardiya);
									iterator.remove();
								}
							}
							if (vardiyaList.size() > 1)
								vardiyaList = PdksUtil.sortListByAlanAdi(vardiyaList, "vardiyaBasZaman", false);
							if (!vardiyaCalismayanList.isEmpty())
								vardiyaList.addAll(vardiyaCalismayanList);
							vardiyaCalismayanList = null;
							AylikPuantaj aylikDepPuantaj = null;
							Long toplamCalismaSekliId = null;
							List<VardiyaGun> vardiyalar = null;
							List<AylikPuantaj> toplamList = new ArrayList<AylikPuantaj>();

							for (Liste liste : list) {
								AylikPuantaj aylikPuantaj = (AylikPuantaj) liste.getValue();
								aylikPuantaj.setTrClass(VardiyaGun.STYLE_CLASS_ODD);
								Personel personel = aylikPuantaj.getPdksPersonel();
								Long bolumId = null, calismaSekliId = null;
								Tanim bolum = personel.getEkSaha3(), altBolum = null;

								try {
									bolumId = personel != null && personel.getEkSaha3() != null && personel.getEkSaha3().getId() != null ? personel.getEkSaha3().getId() : 0L;
									calismaSekliId = personel != null && personel.getCalismaModeli() != null && personel.getCalismaModeli().getId() != null ? personel.getCalismaModeli().getId() : 0L;
								} catch (Exception ex) {
									ex.printStackTrace();
									bolumId = 0L;
									calismaSekliId = 0L;
								}
								Long altBolumId = 0L;
								if (ekSaha4Tanim != null && bolumId.longValue() > 0L && bolum.getKodu().equals(altBolumGrupGoster)) {
									altBolum = personel.getEkSaha4();
									altBolumId = altBolum != null ? altBolum.getId() : 0L;
									altBolumVar = true;
								}
								Double toplam = 0.0d;
								if (toplamCalismaSekliId == null || !toplamCalismaSekliId.equals(calismaSekliId)) {
									if (toplamCalismaSekliId != null) {
										List<VardiyaGun> toplamVardiyalar = aylikDepPuantaj.getVardiyalar();
										if (toplamVardiyalar.isEmpty()) {
											for (VardiyaGun vardiyaGun2 : vardiyalar) {
												VardiyaGun vardiyaGun = new VardiyaGun();
												vardiyaGun.setCalismaSuresi(vardiyaGun2.getCalismaSuresi());
												toplamVardiyalar.add(vardiyaGun);
											}
										} else {
											for (int i = 0; i < toplamVardiyalar.size(); i++) {
												VardiyaGun vardiyaGunToplam = toplamVardiyalar.get(i);
												VardiyaGun vardiyaGun = vardiyalar.get(i);
												vardiyaGunToplam.setCalismaSuresi(vardiyaGun.getCalismaSuresi() + vardiyaGunToplam.getCalismaSuresi());
											}
										}
										Double fazlaMesaiSure = 0.0d;
										for (int i = 0; i < toplamVardiyalar.size(); i++) {
											VardiyaGun vardiyaGunToplam = toplamVardiyalar.get(i);
											fazlaMesaiSure += vardiyaGunToplam.getCalismaSuresi();

										}

										aylikDepPuantaj.setFazlaMesaiSure(fazlaMesaiSure);
										puantajList.add(aylikDepPuantaj);
									}

									aylikDepPuantaj = (AylikPuantaj) aylikPuantaj.clone();
									toplamList.add(aylikDepPuantaj);
									aylikDepPuantaj.setTrClass(VardiyaGun.STYLE_CLASS_EVEN);
									aylikDepPuantaj.setVardiyalar(new ArrayList<VardiyaGun>());
									Personel pdksPersonel = new Personel();
									pdksPersonel.setCalismaModeli(aylikPuantaj.getCalismaModeli());
									aylikDepPuantaj.setPdksPersonel(pdksPersonel);
									toplamCalismaSekliId = calismaSekliId;
								} else {
									List<VardiyaGun> toplamVardiyalar = aylikDepPuantaj.getVardiyalar();
									if (toplamVardiyalar.isEmpty()) {
										for (VardiyaGun vardiyaGun2 : vardiyalar) {
											VardiyaGun vardiyaGun = new VardiyaGun();
											vardiyaGun.setCalismaSuresi(vardiyaGun2.getCalismaSuresi());
											toplamVardiyalar.add(vardiyaGun);
										}

									} else {
										for (int i = 0; i < toplamVardiyalar.size(); i++) {
											VardiyaGun vardiyaGunToplam = toplamVardiyalar.get(i);
											VardiyaGun vardiyaGun = vardiyalar.get(i);
											vardiyaGunToplam.setCalismaSuresi(vardiyaGun.getCalismaSuresi() + vardiyaGunToplam.getCalismaSuresi());
										}

									}

									aylikDepPuantaj.setIzinSuresi(aylikPuantaj.getIzinSuresi() + aylikDepPuantaj.getIzinSuresi());

								}
								vardiyalar = new ArrayList<VardiyaGun>();
								for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
									Vardiya vardiya = (Vardiya) iterator.next();
									String key = calismaSekliId + "_" + bolumId + "_" + altBolumId + "_" + vardiya.getId();
									VardiyaGun vardiyaGun = null;
									if (vardiyaGunDataMap.containsKey(key)) {
										vardiyaGun = vardiyaGunDataMap.get(key);
									} else {
										vardiyaGun = new VardiyaGun(personel, vardiya, date);
										vardiyaGun.setCalismaSuresi(0.0d);
									}
									toplam += vardiyaGun.getCalismaSuresi();
									vardiyalar.add(vardiyaGun);

								}

								aylikPuantaj.setFazlaMesaiSure(toplam);
								if (aylikDepPuantaj.getVardiyalar().isEmpty()) {
									aylikDepPuantaj.setFazlaMesaiSure(toplam);
								}

								aylikPuantaj.setVardiyalar(vardiyalar);
								puantajList.add(aylikPuantaj);

							}
							if (toplamCalismaSekliId != null) {
								List<VardiyaGun> toplamVardiyalar = aylikDepPuantaj.getVardiyalar();
								if (toplamVardiyalar.isEmpty()) {
									for (VardiyaGun vardiyaGun2 : vardiyalar) {
										VardiyaGun vardiyaGun = new VardiyaGun();
										vardiyaGun.setCalismaSuresi(vardiyaGun2.getCalismaSuresi());
										toplamVardiyalar.add(vardiyaGun);
									}
								} else {
									for (int i = 0; i < toplamVardiyalar.size(); i++) {
										VardiyaGun vardiyaGunToplam = toplamVardiyalar.get(i);
										VardiyaGun vardiyaGun = vardiyalar.get(i);
										vardiyaGunToplam.setCalismaSuresi(vardiyaGun.getCalismaSuresi() + vardiyaGunToplam.getCalismaSuresi());
									}
								}
								Double fazlaMesaiSure = 0.0d;
								for (int i = 0; i < toplamVardiyalar.size(); i++) {
									VardiyaGun vardiyaGunToplam = toplamVardiyalar.get(i);
									fazlaMesaiSure += vardiyaGunToplam.getCalismaSuresi();

								}
								aylikDepPuantaj.setFazlaMesaiSure(fazlaMesaiSure);
								puantajList.add(aylikDepPuantaj);
								aylikDepPuantaj = new AylikPuantaj();
								aylikDepPuantaj.setVardiyalar(new ArrayList<VardiyaGun>());
								aylikDepPuantaj.setPdksPersonel(new Personel());
								for (AylikPuantaj aylikPuantaj2 : toplamList) {
									toplamVardiyalar = aylikDepPuantaj.getVardiyalar();
									vardiyalar = aylikPuantaj2.getVardiyalar();
									aylikDepPuantaj.setIzinSuresi(aylikPuantaj2.getIzinSuresi() + aylikDepPuantaj.getIzinSuresi());
									if (toplamVardiyalar.isEmpty()) {
										for (VardiyaGun vardiyaGun2 : vardiyalar) {
											VardiyaGun vardiyaGun = new VardiyaGun();
											vardiyaGun.setCalismaSuresi(vardiyaGun2.getCalismaSuresi());
											toplamVardiyalar.add(vardiyaGun);
										}
									} else {
										for (int i = 0; i < toplamVardiyalar.size(); i++) {
											VardiyaGun vardiyaGunToplam = toplamVardiyalar.get(i);
											VardiyaGun vardiyaGun = vardiyalar.get(i);
											vardiyaGunToplam.setCalismaSuresi(vardiyaGun.getCalismaSuresi() + vardiyaGunToplam.getCalismaSuresi());
										}
									}
								}
								Double toplamFazlaMesaiSure = 0.0d;
								for (int i = 0; i < toplamVardiyalar.size(); i++) {
									VardiyaGun vardiyaGunToplam = toplamVardiyalar.get(i);
									toplamFazlaMesaiSure += vardiyaGunToplam.getCalismaSuresi();

								}
								aylikDepPuantaj.setFazlaMesaiSure(toplamFazlaMesaiSure);
								aylikDepPuantaj.setTrClass(VardiyaGun.STYLE_CLASS_ODD);
								puantajList.add(aylikDepPuantaj);
							}

							vardiyaGunSirala(izinVardiyaGunList);
							vardiyaGunSirala(calismayanVardiyaGunList);
							vardiyaGunSirala(gecGelenVardiyaGunList);
							vardiyaGunSirala(gelmeyenVardiyaGunList);
							vardiyaGunSirala(erkenCikanVardiyaGunList);
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
							for (VardiyaGun vg : calismayanVardiyaGunList) {
								vg.setTdClass(renk ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
								renk = !renk;
							}
							for (VardiyaGun vg : gecGelenVardiyaGunList) {
								vg.setTdClass(renk ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
								renk = !renk;
							}
							for (VardiyaGun vg : gelmeyenVardiyaGunList) {
								vg.setTdClass(renk ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
								renk = !renk;
							}
							renk = false;
							for (VardiyaGun vg : erkenCikanVardiyaGunList) {
								vg.setTdClass(renk ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
								renk = !renk;
							}
						}
						if (!kartBastMayanList.isEmpty()) {
							PdksUtil.addMessageAvailableInfo("Aşağıdaki personel" + (kartBastMayanList.size() > 1 ? "ler" : "") + " mail takibi yapılmamaktadır.");
							for (VardiyaGun vardiyaGun : kartBastMayanList) {
								Personel personel = vardiyaGun.getPdksPersonel();
								PdksUtil.addMessageAvailableWarn(personel.getPdksSicilNo() + " " + personel.getAdSoyad());
							}

						}
						if (!kgsIptaller.isEmpty()) {
							PdksUtil.addMessageAvailableInfo("Aşağıdaki personel" + (kgsIptaller.size() > 1 ? "ler" : "") + " " + ortakIslemler.getParameterKey("kapiGirisUygulama") + "  bilgileri aktif değildir.");
							for (Personel personel : kgsIptaller) {
								PdksUtil.addMessageAvailableWarn(personel.getPdksSicilNo() + " " + personel.getAdSoyad());
							}
						}
						kartBastMayanList = null;
					} catch (Exception e) {
						logger.error("PDKS hata in : \n");
						e.printStackTrace();
						logger.error("PDKS hata out : " + e.getMessage());

					}
				}
			}
		}
		tesisDurumBul();

	}

	/**
	 * @param oncekiGun
	 * @param tumPersoneller
	 * @param basTarih
	 * @param bitTarih
	 * @return
	 * @throws Exception
	 */
	private List<VardiyaGun> getVardiyalariOku(Date oncekiGun, ArrayList<Personel> tumPersoneller, Date basTarih, Date bitTarih) throws Exception {
		String pattern = "yyyyMMdd";
		long ot = Long.parseLong(PdksUtil.convertToDateString(oncekiGun, pattern)), dt = Long.parseLong(PdksUtil.convertToDateString(date, pattern));
		TreeMap<String, VardiyaGun> vardiyaMap = ortakIslemler.getIslemVardiyalar((List<Personel>) tumPersoneller, basTarih, bitTarih, Boolean.FALSE, session, Boolean.TRUE);
		List<VardiyaGun> vardiyaGunList = new ArrayList<VardiyaGun>(vardiyaMap.values());
		for (Iterator iterator = vardiyaGunList.iterator(); iterator.hasNext();) {
			VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator.next();
			Vardiya vardiya = pdksVardiyaGun.getIslemVardiya();
			long vt = Long.parseLong(pdksVardiyaGun.getVardiyaDateStr());
			if (vardiya == null || vt < ot || vt > dt) {
				iterator.remove();
				continue;
			} else if (vardiya.isCalisma() == false && vt != dt) {
				iterator.remove();
				continue;
			}
			logger.debug(pdksVardiyaGun.getVardiyaKeyStr() + " " + pdksVardiyaGun.getAciklama());
		}
		return vardiyaGunList;
	}

	private void tesisDurumBul() {
		List<VardiyaGun> allVardiyaGunList = new ArrayList<VardiyaGun>();
		if (!izinVardiyaGunList.isEmpty())
			allVardiyaGunList.addAll(izinVardiyaGunList);
		if (!calismayanVardiyaGunList.isEmpty())
			allVardiyaGunList.addAll(calismayanVardiyaGunList);
		if (!gecGelenVardiyaGunList.isEmpty())
			allVardiyaGunList.addAll(gecGelenVardiyaGunList);
		if (!gelmeyenVardiyaGunList.isEmpty())
			allVardiyaGunList.addAll(gelmeyenVardiyaGunList);
		tesisDurum = ortakIslemler.getListTesisDurum(allVardiyaGunList);
		digerTesisDurum = ortakIslemler.getListTesisDurum(erkenCikanVardiyaGunList);
		allVardiyaGunList = null;
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
			ByteArrayOutputStream baosDosya = vardiyaOzetExcelDevam();
			if (baosDosya != null) {
				String dosyaAdi = "VardiyaOzetRaporu" + PdksUtil.convertToDateString(date, "yyyyMMdd") + ".xlsx";
				PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi);
			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return "";
	}

	private ByteArrayOutputStream vardiyaOzetExcelDevam() {
		ByteArrayOutputStream baosDosya = null;
		try {
			Workbook wb = new XSSFWorkbook();
			Sheet sheet = ExcelUtil.createSheet(wb, "VardiyaOzetRaporu " + PdksUtil.convertToDateString(date, "yyyyMMdd"), Boolean.TRUE);
			CellStyle header = ExcelUtil.getStyleHeader(wb);
			XSSFCellStyle styleOdd = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
			styleOdd.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
			XSSFCellStyle styleOddTutar = (XSSFCellStyle) ExcelUtil.setAlignment((CellStyle) styleOdd.clone(), CellStyle.ALIGN_RIGHT);
			XSSFCellStyle styleOddCenter = (XSSFCellStyle) ExcelUtil.setAlignment((CellStyle) styleOdd.clone(), CellStyle.ALIGN_CENTER);
			XSSFCellStyle styleEven = (XSSFCellStyle) (XSSFCellStyle) ExcelUtil.setAlignment(ExcelUtil.getStyleData(wb), CellStyle.ALIGN_LEFT);
			ExcelUtil.setFillForegroundColor(styleEven, 219, 248, 219);
			XSSFCellStyle styleEvenTutar = (XSSFCellStyle) ExcelUtil.setAlignment((CellStyle) styleEven.clone(), CellStyle.ALIGN_RIGHT);
			XSSFCellStyle styleEvenCenter = (XSSFCellStyle) ExcelUtil.setAlignment((CellStyle) styleEven.clone(), CellStyle.ALIGN_CENTER);

			int col = 0, row = 0;

			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("VARDİYA ÖZET RAPOR");

			for (Vardiya vardiya : vardiyaList) {
				String aciklama = vardiya.getAdi();
				if (vardiya.isCalisma())
					aciklama = authenticatedUser.timeFormatla(vardiya.getBasZaman()) + " : " + authenticatedUser.timeFormatla(vardiya.getBitZaman());
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aciklama);

			}
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("ÇALIŞAN SAYISI");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İZİNLİ SAYISI");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("TOPLAM SAYI");
			List<VardiyaGun> vardiyaGunList = new ArrayList<VardiyaGun>();
			for (AylikPuantaj aylikPuantaj : puantajList) {
				col = 0;
				++row;
				Personel personel = aylikPuantaj.getPdksPersonel();

				String aciklama = "";
				XSSFCellStyle satirStyle = (XSSFCellStyle) styleOdd.clone(), satirTutarStyle = (XSSFCellStyle) styleOddTutar.clone();
				Tanim bolum = personel.getEkSaha3();
				if (bolum != null) {
					aciklama = bolum.getAciklama();
					if (ekSaha4Tanim != null && bolum.getKodu().equals(altBolumGrupGoster)) {
						Tanim altBolum = personel.getEkSaha4();
						if (altBolum != null)
							aciklama += " " + altBolum.getAciklama();
					}

				} else if (personel.getCalismaModeli() != null) {
					aciklama = personel.getCalismaModeli().getAciklama();
					satirStyle = (XSSFCellStyle) styleEven.clone();
					satirTutarStyle = (XSSFCellStyle) styleEvenTutar.clone();
				} else
					aciklama = "TOPLAM";
				ExcelUtil.getCell(sheet, row, col++, satirStyle).setCellValue(aciklama);
				for (Iterator iterator = aylikPuantaj.getVardiyalar().iterator(); iterator.hasNext();) {
					VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
					vardiyaGunList.add(vardiyaGun);
					if (vardiyaGun.getCalismaSuresi() > 0)
						ExcelUtil.getCell(sheet, row, col++, satirTutarStyle).setCellValue(new Double(vardiyaGun.getCalismaSuresi()).longValue());
					else
						ExcelUtil.getCell(sheet, row, col++, satirStyle).setCellValue("");

				}
				if (aylikPuantaj.getFazlaMesaiSure() > 0)
					ExcelUtil.getCell(sheet, row, col++, satirTutarStyle).setCellValue(new Double(aylikPuantaj.getFazlaMesaiSure()).longValue());
				else
					ExcelUtil.getCell(sheet, row, col++, satirStyle).setCellValue("");
				if (aylikPuantaj.getIzinSuresi() > 0)
					ExcelUtil.getCell(sheet, row, col++, satirTutarStyle).setCellValue(new Double(aylikPuantaj.getIzinSuresi()).longValue());
				else
					ExcelUtil.getCell(sheet, row, col++, satirStyle).setCellValue("");
				if (aylikPuantaj.getFazlaMesaiSure() + aylikPuantaj.getIzinSuresi() > 0)
					ExcelUtil.getCell(sheet, row, col++, satirTutarStyle).setCellValue(new Double(aylikPuantaj.getFazlaMesaiSure() + aylikPuantaj.getIzinSuresi()).longValue());
				else
					ExcelUtil.getCell(sheet, row, col++, satirStyle).setCellValue("");

			}
			for (int i = 0; i < col; i++)
				sheet.autoSizeColumn(i);
			if (izinVardiyaGunList.size() + calismayanVardiyaGunList.size() + gelmeyenVardiyaGunList.size() + gecGelenVardiyaGunList.size() > 0 + erkenCikanVardiyaGunList.size()) {
				sheet = ExcelUtil.createSheet(wb, "DevamsizlikDetay " + PdksUtil.convertToDateString(date, "yyyyMMdd"), Boolean.TRUE);
				col = 0;
				row = 0;
				boolean renk = false;
				String tip = "";
				XSSFCellStyle satirStyle = null, satirCenter = null;
				List<Integer> baslikList = new ArrayList<Integer>();
				if (izinVardiyaGunList.size() + calismayanVardiyaGunList.size() + gelmeyenVardiyaGunList.size() + gecGelenVardiyaGunList.size() > 0) {
					if (!izinVardiyaGunList.isEmpty())
						vardiyaGunList.addAll(izinVardiyaGunList);
					if (!calismayanVardiyaGunList.isEmpty())
						vardiyaGunList.addAll(calismayanVardiyaGunList);
					if (!gelmeyenVardiyaGunList.isEmpty())
						vardiyaGunList.addAll(gelmeyenVardiyaGunList);
					if (!gecGelenVardiyaGunList.isEmpty())
						vardiyaGunList.addAll(gecGelenVardiyaGunList);
					ExcelUtil.getCell(sheet, row, col, header).setCellValue("RAPOR/İZİN/GEÇ GİRİŞ TABLOSU");
					int son = 7 + (tesisDurum ? 1 : 0);
					for (int i = 1; i < son; i++)
						ExcelUtil.getCell(sheet, row, col + i + 1, header).setCellValue("");
					baslikList.add(row);
					++row;
					col = 0;
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı");
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Soyadı");
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
					boolean tesisDurumu = tesisDurum;
					if (tesisDurumu)
						ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Tarih");
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Giriş Saati");
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Açıklama");
					tip = "I";
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
						col = vardiyaSatirEkle(sheet, col, row, satirStyle, satirCenter, vg, tip, tesisDurumu);
						renk = !renk;
					}
					tip = "O";
					for (VardiyaGun vg : calismayanVardiyaGunList) {
						++row;
						col = 0;
						if (renk == false) {
							satirStyle = styleOdd;
							satirCenter = styleOddCenter;
						} else {
							satirStyle = styleEven;
							satirCenter = styleEvenCenter;
						}
						col = vardiyaSatirEkle(sheet, col, row, satirStyle, satirCenter, vg, tip, tesisDurumu);
						renk = !renk;
					}
					tip = "G";
					for (VardiyaGun vg : gecGelenVardiyaGunList) {
						++row;
						col = 0;
						if (renk == false) {
							satirStyle = styleOdd;
							satirCenter = styleOddCenter;
						} else {
							satirStyle = styleEven;
							satirCenter = styleEvenCenter;
						}
						col = vardiyaSatirEkle(sheet, col, row, satirStyle, satirCenter, vg, tip, tesisDurum);
						renk = !renk;
					}
					tip = "B";
					for (VardiyaGun vg : gelmeyenVardiyaGunList) {
						++row;
						col = 0;
						if (renk == false) {
							satirStyle = styleOdd;
							satirCenter = styleOddCenter;
						} else {
							satirStyle = styleEven;
							satirCenter = styleEvenCenter;
						}
						col = vardiyaSatirEkle(sheet, col, row, satirStyle, satirCenter, vg, tip, tesisDurum);
						renk = !renk;
					}
					++row;
				}
				if (erkenCikanVardiyaGunList.size() > 0) {
					col = 0;
					++row;
					ExcelUtil.getCell(sheet, row++, col, header).setCellValue("");
					ExcelUtil.getCell(sheet, row, col, header).setCellValue("ERKEN ÇIKAN TABLOSU");
					for (int i = 1; i < 9; i++)
						ExcelUtil.getCell(sheet, row, col + i + 1, header).setCellValue("");
					baslikList.add(row);
					++row;
					col = 0;
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı");
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Soyadı");
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
					boolean tesisDurumu = digerTesisDurum;
					if (tesisDurumu)
						ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Tarih");
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Çıkış Saati");
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Açıklama");
					tip = "E";
					for (VardiyaGun vg : erkenCikanVardiyaGunList) {
						++row;
						col = 0;
						if (renk == false) {
							satirStyle = styleOdd;
							satirCenter = styleOddCenter;
						} else {
							satirStyle = styleEven;
							satirCenter = styleEvenCenter;
						}
						col = vardiyaSatirEkle(sheet, col, row, satirStyle, satirCenter, vg, tip, tesisDurumu);
						renk = !renk;
					}
				}
				for (int i = 0; i < col; i++)
					sheet.autoSizeColumn(i);
				for (Integer r : baslikList) {
					try {
						sheet.addMergedRegion(ExcelUtil.getRegion((int) r, (int) 0, (int) r, (int) 8));
					} catch (Exception e) {
					}
				}
			}
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

	/**
	 * @param sheet
	 * @param col
	 * @param row
	 * @param satirStyle
	 * @param satirCenter
	 * @param vg
	 * @param tip
	 * @param tesisDurum
	 * @return
	 */
	private int vardiyaSatirEkle(Sheet sheet, int col, int row, XSSFCellStyle satirStyle, XSSFCellStyle satirCenter, VardiyaGun vg, String tip, boolean tesisDurum) {
		Personel personel = vg.getPersonel();
		ExcelUtil.getCell(sheet, row, col++, satirCenter).setCellValue(personel.getPdksSicilNo());
		ExcelUtil.getCell(sheet, row, col++, satirStyle).setCellValue(personel.getAd());
		ExcelUtil.getCell(sheet, row, col++, satirStyle).setCellValue(personel.getSoyad());
		ExcelUtil.getCell(sheet, row, col++, satirStyle).setCellValue(personel.getSirket().getAd());
		if (tesisDurum)
			ExcelUtil.getCell(sheet, row, col++, satirStyle).setCellValue(personel.getTesis() != null ? personel.getTesis().getAciklama() : "");

		Tanim bolum = personel.getEkSaha3();
		String bolumAciklama = "";
		if (bolum != null) {
			bolumAciklama = bolum.getAciklama();
			if (ekSaha4Tanim != null && bolum.getKodu().equals(altBolumGrupGoster)) {
				Tanim altBolum = personel.getEkSaha4();
				if (altBolum != null)
					bolumAciklama += " " + altBolum.getAciklama();
			}
		}

		ExcelUtil.getCell(sheet, row, col++, satirStyle).setCellValue(bolumAciklama);
		ExcelUtil.getCell(sheet, row, col++, satirCenter).setCellValue(PdksUtil.convertToDateString(vg.getVardiyaDate(), PdksUtil.getDateFormat()));
		if (tip.equals("O")) {
			ExcelUtil.getCell(sheet, row, col++, satirCenter).setCellValue("");
			ExcelUtil.getCell(sheet, row, col++, satirCenter).setCellValue(vg.getVardiya().getAdi());

		} else if (tip.equals("I")) {
			ExcelUtil.getCell(sheet, row, col++, satirCenter).setCellValue("");
			if (!vg.getVardiya().isFMI()) {
				String aciklama = "";
				if (vg.isIzinli())
					aciklama = vg.getIzin() != null ? vg.getIzin().getIzinTipi().getIzinTipiTanim().getAciklama() : vg.getVardiya().getAciklama();
				ExcelUtil.getCell(sheet, row, col++, satirStyle).setCellValue(aciklama);

			} else
				ExcelUtil.getCell(sheet, row, col++, satirCenter).setCellValue(vg.getVardiya().getAdi());

		} else if (tip.equals("G")) {
			ExcelUtil.getCell(sheet, row, col++, satirCenter).setCellValue(vg.getIlkGiris() != null ? authenticatedUser.timeFormatla(vg.getIlkGiris().getZaman()) : "");
			ExcelUtil.getCell(sheet, row, col++, satirCenter).setCellValue("Geç giriş [ " + authenticatedUser.dateTimeFormatla(vg.getIslemVardiya().getVardiyaBasZaman()) + " ]");

		} else if (tip.equals("B")) {
			ExcelUtil.getCell(sheet, row, col++, satirCenter).setCellValue("");
			XSSFCellStyle satirCenterRenk = (XSSFCellStyle) satirCenter.clone();
			if (!vg.getVardiya().isFMI()) {
				if (personel.getMailTakip() == null || personel.getMailTakip().equals(Boolean.FALSE)) {
					if (fontRed == null)
						fontRed = ExcelUtil.createFont(sheet.getWorkbook(), (short) 8, "Arial", Font.BOLDWEIGHT_BOLD);
					fontRed.setColor(IndexedColors.RED.getIndex());
					satirCenterRenk.setFont(fontRed);

				}
			}
			ExcelUtil.getCell(sheet, row, col++, satirCenterRenk).setCellValue(!vg.getVardiya().isFMI() ? "DEVAMSIZLIK" : vg.getVardiya().getAdi());

		} else if (tip.equals("E")) {
			ExcelUtil.getCell(sheet, row, col++, satirCenter).setCellValue(vg.getSonCikis() != null ? authenticatedUser.timeFormatla(vg.getSonCikis().getZaman()) : "");
			ExcelUtil.getCell(sheet, row, col++, satirCenter).setCellValue("Erken Çıkış [ " + authenticatedUser.dateTimeFormatla(vg.getIslemVardiya().getVardiyaBitZaman()) + " ]");

		}
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

	public List<PersonelIzin> getIzinList() {
		return izinList;
	}

	public void setIzinList(List<PersonelIzin> izinList) {
		this.izinList = izinList;
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

	public List<VardiyaGun> getGecGelenVardiyaGunList() {
		return gecGelenVardiyaGunList;
	}

	public void setGecGelenVardiyaGunList(List<VardiyaGun> gecGelenVardiyaGunList) {
		this.gecGelenVardiyaGunList = gecGelenVardiyaGunList;
	}

	public List<VardiyaGun> getErkenCikanVardiyaGunList() {
		return erkenCikanVardiyaGunList;
	}

	public void setErkenCikanVardiyaGunList(List<VardiyaGun> erkenCikanVardiyaGunList) {
		this.erkenCikanVardiyaGunList = erkenCikanVardiyaGunList;
	}

	public List<Vardiya> getVardiyaList() {
		return vardiyaList;
	}

	public void setVardiyaList(List<Vardiya> vardiyaList) {
		this.vardiyaList = vardiyaList;
	}

	public List<VardiyaGun> getGelmeyenVardiyaGunList() {
		return gelmeyenVardiyaGunList;
	}

	public void setGelmeyenVardiyaGunList(List<VardiyaGun> gelmeyenVardiyaGunList) {
		this.gelmeyenVardiyaGunList = gelmeyenVardiyaGunList;
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

	public boolean isDigerTesisDurum() {
		return digerTesisDurum;
	}

	public void setDigerTesisDurum(boolean digerTesisDurum) {
		this.digerTesisDurum = digerTesisDurum;
	}

	public String getSicilNo() {
		return sicilNo;
	}

	public void setSicilNo(String sicilNo) {
		this.sicilNo = sicilNo;
	}

	public Tanim getEkSaha4Tanim() {
		return ekSaha4Tanim;
	}

	public void setEkSaha4Tanim(Tanim ekSaha4Tanim) {
		this.ekSaha4Tanim = ekSaha4Tanim;
	}

	public String getAltBolumGrupGoster() {
		return altBolumGrupGoster;
	}

	public void setAltBolumGrupGoster(String altBolumGrupGoster) {
		this.altBolumGrupGoster = altBolumGrupGoster;
	}

	public List<VardiyaGun> getCalismayanVardiyaGunList() {
		return calismayanVardiyaGunList;
	}

	public void setCalismayanVardiyaGunList(List<VardiyaGun> calismayanVardiyaGunList) {
		this.calismayanVardiyaGunList = calismayanVardiyaGunList;
	}
}
