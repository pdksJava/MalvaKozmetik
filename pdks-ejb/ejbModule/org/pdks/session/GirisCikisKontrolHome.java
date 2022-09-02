package org.pdks.session;

import java.io.Serializable;
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
import org.pdks.entity.Personel;
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.KapiView;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.PersonelHareketIslem;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.PersonelView;
import org.pdks.security.entity.User;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;

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

	private String islemTipi, bolumAciklama;
	private Date date;
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
		fillEkSahaTanim();
	}

	private void fillEkSahaTanim() {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
	}

	public void hareketGoster(VardiyaGun pdksVardiyaGun) {
		setInstance(pdksVardiyaGun);
		List<HareketKGS> kgsList = pdksVardiyaGun.getHareketler();
		setHareketList(kgsList);

	}

	public void fillHareketList() throws Exception {
		List<VardiyaGun> vardiyaList = new ArrayList<VardiyaGun>();
		List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
		List<HareketKGS> kgsList = new ArrayList<HareketKGS>();
		ArrayList<Personel> perList = (ArrayList<Personel>) authenticatedUser.getTumPersoneller().clone();
		TreeMap<String, VardiyaGun> vardiyalar = ortakIslemler.getIslemVardiyalar((List<Personel>) perList.clone(), date, date, Boolean.FALSE, session, Boolean.TRUE);
		vardiyaList = new ArrayList<VardiyaGun>(vardiyalar.values());

		Date tarih1 = null;
		Date tarih2 = null;
		HashMap parametreMap = new HashMap();
		parametreMap.put("izinTipi.bakiyeIzinTipi=", null);
		parametreMap.put("izinSahibi", (List<Personel>) perList.clone());
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
			if (tarih1 == null || pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans1BasZaman().getTime() < tarih1.getTime())
				tarih1 = pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans1BasZaman();

			if (tarih2 == null || pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans2BitZaman().getTime() > tarih2.getTime())
				tarih2 = pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans2BitZaman();
			if (pdksVardiyaGun.getIslemVardiya().isCalisma() && pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans2BasZaman().getTime() > simdikiZaman.getTime())
				iterator.remove();

		}
		List<Long> kapiIdler = ortakIslemler.getPdksKapiIdler(session, Boolean.TRUE);
		if (kapiIdler != null && !kapiIdler.isEmpty())
			kgsList = ortakIslemler.getPdksHareketBilgileri(Boolean.TRUE, kapiIdler, (List<Personel>) perList.clone(), tarih1, tarih2, HareketKGS.class, session);
		else
			kgsList = new ArrayList<HareketKGS>();
		if (!kgsList.isEmpty())
			kgsList = PdksUtil.sortListByAlanAdi(kgsList, "zaman", Boolean.FALSE);

		Long basZaman1 = 0L, basZaman2 = 0L, bitZaman1 = 0L, bitZaman2 = 0L;

		try {

			for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
				VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
				vardiyaGun.setHareketler(null);
				vardiyaGun.setGirisHareketleri(null);
				vardiyaGun.setCikisHareketleri(null);

				for (Iterator iterator1 = kgsList.iterator(); iterator1.hasNext();) {
					HareketKGS kgsHareket = (HareketKGS) iterator1.next();
					if (vardiyaGun.getPersonel().getId().equals(kgsHareket.getPersonel().getPdksPersonel().getId())) {
						if (vardiyaGun.addHareket(kgsHareket, Boolean.FALSE))
							iterator1.remove();

					}
				}
				HareketKGS kgsHareketGiris = vardiyaGun.getGirisHareket(), kgsHareketCikis = vardiyaGun.getCikisHareket();
				PersonelIzin izin = null;
				for (Iterator iterator2 = izinList.iterator(); iterator2.hasNext();) {
					PersonelIzin personelIzin = (PersonelIzin) iterator2.next();
					if (vardiyaGun.getPersonel().getId() == personelIzin.getIzinSahibi().getId()) {
						izin = personelIzin;
						iterator2.remove();
						break;
					}

				}
				boolean yaz = !(vardiyaGun.getHareketDurum()) && simdikiZaman.getTime() < bitZaman2;
				if (!vardiyaGun.isHareketHatali() || vardiyaGun.getHareketler().isEmpty())
					vardiyaGun.setHareketler(null);

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
			ortakIslemler.otomatikHareketEkle(new ArrayList<VardiyaGun>(vardiyalar.values()), session);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		setVardiyaGunList(vardiyaList);

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

}
