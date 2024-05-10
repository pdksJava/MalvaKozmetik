package org.pdks.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.pdks.security.entity.User;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksUtil;

public class AylikPuantaj implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8704746910670640583L;

	static Logger logger = Logger.getLogger(AylikPuantaj.class);

	private static Boolean gebelikGuncelle = Boolean.FALSE;

	public static final String MESAI_TIPI_AKSAM_ADET = "A";

	public static final String MESAI_TIPI_AKSAM_SAAT = "AS";

	public static final String MESAI_TIPI_HAFTA_TATIL = "HT";

	public static final String MESAI_TIPI_RESMI_TATIL = "RT";

	public static final String MESAI_TIPI_KESINTI_SURE = "KS";

	public static final String MESAI_TIPI_NORMAT = "UO";

	private static double gunlukCalismaSuresi = 9d;

	private static double gunlukAnneCalismaSuresi = 7.5d;

	private static Double eksikCalismaUyariYuzdesi = 0.85d;

	private Personel pdksPersonel, yonetici, yonetici2;

	private int yil, ay, gunSayisi, aksamVardiyaSayisi = 0, calisilanGunSayisi = 0;

	private Sirket sirket;

	private Date ilkGun, sonGun;

	private List<VardiyaGun> vardiyalar;

	private List<Integer> izinAdetList;

	private TreeMap<String, Integer> izinAdetMap;

	private List<PersonelDenklestirmeTasiyici> denklestirmeHaftalari;

	private VardiyaPlan vardiyaPlan;

	private CalismaModeliAy calismaModeliAy;

	private PersonelDenklestirme personelDenklestirme, personelDenklestirmeGelecekAy, personelDenklestirmeGecenAy;

	private PersonelDenklestirmeBordro denklestirmeBordro;

	private DenklestirmeAy denklestirmeAy, denklestirmeGecenAy, denklestirmeGelecekAy;

	private AylikPuantaj gecenAylikPuantaj, sablonAylikPuantaj;

	private VardiyaGun vardiyaGun;

	private List<VardiyaHafta> vardiyaHaftaList;

	private String trClass;

	private boolean kaydet, gorevYeriSec = false, secili, onayDurum, vardiyaOlustu = Boolean.FALSE, vardiyaDegisti = Boolean.FALSE, fiiliHesapla = Boolean.FALSE;

	private boolean donemBitti = Boolean.TRUE, ayrikHareketVar = Boolean.FALSE, fazlaMesaiIzinKontrol = Boolean.TRUE, gebeDurum = Boolean.TRUE;

	private Double saatToplami = 0d, resmiTatilToplami = 0d, haftaCalismaSuresi = 0d, ucretiOdenenMesaiSure = 0d, fazlaMesaiSure = 0d, odenenSure = 0d, planlananSure = 0d, offSure = 0.0d;

	private Double izinSuresi = 0d, saatlikIzinSuresi = 0d, eksikCalismaSure = 0d, gecenAyFazlaMesai = 0d, hesaplananSure = 0d, devredenSure = 0d, aksamVardiyaSaatSayisi = 0d, kesilenSure = 0d;

	private boolean fazlaMesaiHesapla = Boolean.FALSE, vardiyaSua = Boolean.FALSE, eksikGunVar = Boolean.FALSE, denklestirilmeyenDevredenVar = Boolean.FALSE;

	private CalismaModeli calismaModeli;

	private ArrayList<HareketKGS> hareketler;

	private TreeMap<String, VardiyaGun> vgMap;

	private TreeMap<Long, PersonelDenklestirmeDinamikAlan> dinamikAlanMap;

	private TreeMap<Long, PersonelDinamikAlan> personelDinamikAlanMap;

	private PersonelDenklestirmeDinamikAlan personelDenklestirmeDinamikAlan;

	private PersonelDinamikAlan personelDinamikAlan;

	private User loginUser;

	public AylikPuantaj(PersonelDenklestirmeBordro bordro) {
		super();
		if (bordro != null) {
			this.denklestirmeBordro = bordro;
			this.setPersonelDenklestirme(bordro.getPersonelDenklestirme());
			this.setPdksPersonel(this.getPersonelDenklestirme().getPdksPersonel());
		}
	}

	public AylikPuantaj() {
		super();
		this.haftaCalismaSuresi = 0d;
		this.vardiyaSua = Boolean.FALSE;
	}

	public AylikPuantaj(DenklestirmeAy denklestirmeAy) {
		super();
		this.denklestirmeAy = denklestirmeAy;
	}

	public AylikPuantaj(Date ilkGun, Date sonGun) {
		super();
		this.ilkGun = ilkGun;
		this.sonGun = sonGun;
	}

	public Boolean getSutIzniDurum() {
		boolean sutIzniDurum = false;
		if (personelDenklestirme != null && personelDenklestirme.getId() != null)
			sutIzniDurum = personelDenklestirme.isSutIzniVar();
		return sutIzniDurum;
	}

	public void vardiyaOlustur() {

	}

	public VardiyaGun getVardiya(VardiyaGun gun) {
		vardiyaGun = null;
		if (vgMap == null) {
			vgMap = new TreeMap<String, VardiyaGun>();
			for (Iterator iterator = vardiyalar.iterator(); iterator.hasNext();) {
				VardiyaGun v = (VardiyaGun) iterator.next();
				vgMap.put(v.getVardiyaDateStr(), v);
			}
		}
		if (gun != null && vgMap != null && gun.getVardiyaDate() != null) {
			String key = gun.getVardiyaDateStr();
			if (vgMap.containsKey(key))
				vardiyaGun = vgMap.get(key);
		}
		if (vardiyaGun == null) {
			vardiyaGun = new VardiyaGun(this.getPdksPersonel(), null, gun.getVardiyaDate());

		}

		return vardiyaGun;
	}

	public void degerSifirla() {
		this.setSaatlikIzinSuresi(0.0d);
		this.setResmiTatilToplami(0.0d);
		this.setHaftaCalismaSuresi(0.0d);
		this.setAksamVardiyaSaatSayisi(0.0d);
		this.setAksamVardiyaSayisi(0);
		this.setDevredenSure(0.0d);
		this.setEksikCalismaSure(0.0d);
		this.setFazlaMesaiSure(0.0d);
		this.setGecenAyFazlaMesai(0.0d);
		this.setHesaplananSure(0.0d);
		this.setIzinSuresi(0.0d);
		this.setOdenenSure(0.0d);
		this.setOffSure(0.0d);
		this.setSaatToplami(0.0d);
		this.setUcretiOdenenMesaiSure(0.0d);
	}

	public Personel getPdksPersonel() {
		return pdksPersonel;
	}

	public void setPdksPersonel(Personel value) {
		if (value != null) {
			Personel personel1 = value.getPdksYonetici(), personel2 = value.getYonetici2();
			if (personel1 != null && personel1.isCalisiyor())
				this.yonetici = personel1;
			else
				this.yonetici = null;
			if (personel2 != null && personel2.isCalisiyor())
				this.yonetici2 = personel2;
			else
				this.yonetici2 = null;
			this.sirket = value.getSirket();
			if (this.getCalismaModeli() == null)
				this.setCalismaModeli(value.getCalismaModeli());
		}
		this.pdksPersonel = value;
	}

	public void setPersonelCalismiyor() {
		if (pdksPersonel != null) {
			Personel personel1 = pdksPersonel.getYoneticisi();
			Personel personel2 = personel1 != null ? personel1.getYoneticisi() : null;
			if (pdksPersonel.getAsilYonetici2() != null)
				personel2 = pdksPersonel.getAsilYonetici2();
			if (personel1 != null)
				this.yonetici = personel1;
			else
				this.yonetici = null;
			if (personel2 != null)
				this.yonetici2 = personel2;
			else
				this.yonetici2 = null;
		}

	}

	public List<VardiyaGun> getAyinVardiyalari() {
		List ayinVardiyalari = new ArrayList<VardiyaGun>(getVardiyalar());
		for (Iterator iterator = ayinVardiyalari.iterator(); iterator.hasNext();) {
			VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
			if (!vardiyaGun.isAyinGunu())
				iterator.remove();
		}

		return ayinVardiyalari;
	}

	public List<VardiyaGun> getVardiyalar() {
		if (vardiyalar == null)
			vardiyalar = new ArrayList<VardiyaGun>(bosVardiya().values());
		return vardiyalar;
	}

	public void addVardiya(VardiyaGun value) {
		if (vardiyalar == null)
			vardiyalar = new ArrayList<VardiyaGun>(bosVardiya().values());
		if (value != null && value.getPersonel() != null)
			vardiyalar.add(value);

	}

	public void setVardiyalar(List<VardiyaGun> value) {

		if (value != null) {

			for (Iterator iterator = value.iterator(); iterator.hasNext();) {
				VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator.next();

				if (pdksVardiyaGun.getVardiya() != null && pdksVardiyaGun.getId() == null)
					logger.debug(pdksVardiyaGun.getVardiyaKeyStr());

			}
		}
		this.vardiyalar = value;
	}

	public VardiyaGun getVardiyaGun(VardiyaGun data) {
		VardiyaGun vg = null;
		if (data != null) {
			if (vgMap != null && vgMap.containsKey(data.getVardiyaDateStr()))
				vg = vgMap.get(data.getVardiyaDateStr());
			else
				vg = new VardiyaGun(pdksPersonel, null, data.getVardiyaDate());
			vg.setTdClass(vg.getVardiya() != null ? vg.getAylikClassAdi(this.getTrClass()) : "off");
		}
		vardiyaGun = vg;
		return vg;
	}

	public List<PersonelDenklestirmeTasiyici> getDenklestirmeHaftalari() {
		return denklestirmeHaftalari;
	}

	public void setDenklestirmeHaftalari(List<PersonelDenklestirmeTasiyici> denklestirmeHaftalari) {
		this.denklestirmeHaftalari = denklestirmeHaftalari;
	}

	public int getYil() {
		return yil;
	}

	public void setYil(int yil) {
		this.yil = yil;
	}

	public int getAy() {
		return ay;
	}

	public void setAy(int ay) {
		this.ay = ay;
	}

	public int getGunSayisi() {
		return gunSayisi;
	}

	public void setGunSayisi(int gunSayisi) {
		this.gunSayisi = gunSayisi;
	}

	public Date getIlkGun() {
		return ilkGun;
	}

	public void setIlkGun(Date ilkGun) {
		this.ilkGun = ilkGun;
	}

	public Date getSonGun() {
		return sonGun;
	}

	public void setSonGun(Date sonGun) {
		this.sonGun = sonGun;
	}

	public void setPersonelDenklestirmeTasiyici(PersonelDenklestirmeTasiyici denklestirme) {
		pdksPersonel = denklestirme.getPersonel();
		TreeMap<String, VardiyaGun> map = bosVardiya();
		if (denklestirme.getPersonelDenklestirmeleri() != null) {
			for (Iterator iterator = denklestirme.getPersonelDenklestirmeleri().iterator(); iterator.hasNext();) {
				PersonelDenklestirmeTasiyici personelDenklestirme = (PersonelDenklestirmeTasiyici) iterator.next();
				if (personelDenklestirme.getVardiyalar() != null) {

					for (VardiyaGun pdksVardiyaGun : personelDenklestirme.getVardiyalar()) {
						pdksVardiyaGun.setFazlaMesaiSure(0);
						if (pdksVardiyaGun.getVardiya() != null) {
							String key = PdksUtil.convertToDateString(pdksVardiyaGun.getVardiyaDate(), "yyyyMMdd");
							map.put(key, pdksVardiyaGun);
						}
					}
				}
			}
		}
		List<VardiyaGun> value = new ArrayList(map.values());
		map = null;
		this.setVardiyalar(value);

	}

	private TreeMap<String, VardiyaGun> bosVardiya() {
		TreeMap<String, VardiyaGun> map = new TreeMap<String, VardiyaGun>();
		if (ilkGun != null) {
			Date tarih = (Date) ilkGun.clone();
			Calendar cal = Calendar.getInstance();
			cal.setTime(tarih);
			for (int i = 0; i < gunSayisi; i++) {
				VardiyaGun pdksVardiyaGun = new VardiyaGun(pdksPersonel, null, cal.getTime());
				String key = PdksUtil.convertToDateString(pdksVardiyaGun.getVardiyaDate(), "yyyyMMdd");
				map.put(key, pdksVardiyaGun);
				cal.add(Calendar.DATE, 1);
			}
		}

		return map;
	}

	@Transient
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// bu class cloneable oldugu icin buraya girilmemeli...
			throw new InternalError();
		}
	}

	public String getTrClass() {
		return trClass;
	}

	public String getTrSonucClass() {
		String str = fazlaMesaiHesapla ? trClass : VardiyaGun.STYLE_CLASS_HATA;
		if (denklestirilmeyenDevredenVar)
			str = "warn";
		return str;
	}

	public void setTrClass(String trClass) {
		this.trClass = trClass;
	}

	public double getSaatToplami() {
		return saatToplami;
	}

	public double getResmiTatilToplami() {
		return resmiTatilToplami;
	}

	public VardiyaPlan getVardiyaPlan() {
		return vardiyaPlan;
	}

	public void setVardiyaPlan(VardiyaPlan vardiyaPlan) {
		this.vardiyaPlan = vardiyaPlan;
	}

	public boolean isKaydet() {
		return kaydet;
	}

	public void setKaydet(boolean kaydet) {
		this.kaydet = kaydet;
	}

	public double getFazlaMesaiSure() {
		return fazlaMesaiSure;
	}

	public PersonelDenklestirme getPersonelDenklestirmeGecenAylik() {
		PersonelDenklestirme personelDenklestirmeGecenAylik = personelDenklestirme != null ? personelDenklestirme.getPersonelDenklestirmeGecenAy() : null;
		return personelDenklestirmeGecenAylik;
	}

	public double getAylikNetFazlaMesai() {
		double aylikNetFazlaMesai = getAylikFazlaMesai();
		return aylikNetFazlaMesai;
	}

	public double getAylikFazlaMesai() {

		double aylikFazlaMesai = saatToplami - (planlananSure + ucretiOdenenMesaiSure);
		if (calismaModeli == null || !calismaModeli.isFazlaMesaiVarMi())
			aylikFazlaMesai = 0;

		return aylikFazlaMesai;
	}

	public double getDenklesmisMesai() {
		double aylikFazlaMesai = getAylikFazlaMesai();
		double gecenAySure = personelDenklestirme != null ? personelDenklestirme.getKalanSure() : 0d;
		double denklesmisMesai = aylikFazlaMesai + gecenAySure;
		return denklesmisMesai;
	}

	/**
	 * @param yemekList
	 * @param session
	 */
	private void planlananSureHesapla(TreeMap<String, Tatil> tatilGunleriMap) {

		double izinSure = 0.0d;
		calisilanGunSayisi = 0;

		izinSure = izinSuresi;
		if (calismaModeliAy == null)
			logger.debug(personelDenklestirme.getId());
		if (calismaModeliAy != null && personelDenklestirme.getCalismaModeliAy() == null)
			personelDenklestirme.setCalismaModeliAy(calismaModeliAy);
		CalismaModeli calismaModeli = personelDenklestirme.getCalismaModeliAy() != null ? personelDenklestirme.getCalismaModeli() : null;
		if (personelDenklestirme.isSuaDurumu()) {
			for (VardiyaGun vg : vardiyalar) {
				if (vg.isAyinGunu() && vg.getVardiya() != null && vg.getVardiya().isSuaMi() && vg.getVardiya().getId() != null)
					personelDenklestirme.setCalismaSuaSaati(vg.getCalismaSuaSaati());
			}
		}
		if (dinamikAlanMap != null) {
			for (Long key : dinamikAlanMap.keySet()) {
				PersonelDenklestirmeDinamikAlan pda = dinamikAlanMap.get(key);
				if (pda.isIzinDurum() && pda.getIslemDurum() && pda.getSayisalDeger() != null) {
					if (pda.getSayisalDeger().doubleValue() > 0.0d)
						izinSure += pda.getSayisalDeger();
				}
			}
		}
		double arifeToplamSure = getArifeToplamSure(tatilGunleriMap, calismaModeli);
		Double hesaplananSure = (personelDenklestirme != null ? personelDenklestirme.getMaksimumSure(izinSure, arifeToplamSure) : 0d);
		if (hesaplananSure < 0)
			hesaplananSure = 0.0d;
		if (tatilGunleriMap != null && vardiyalar != null && !vardiyalar.isEmpty()) {
			double yarimGun = personelDenklestirme.getCalismaModeli().getArife();
			gebeDurum = false;
			for (VardiyaGun vg : vardiyalar) {
				if (vg.getIzin() != null || vg.getVardiya() == null || vg.getVardiya().getId() == null)
					continue;
				String key = vg.getVardiyaDateStr();
				if (vg.isAyinGunu()) {
					if (!gebeDurum && vg.getVardiya() != null) {
						this.setGebeDurum(calismaModeliAy != null && vg.getIzin() == null && vg.getVardiya().getGebelik());
						if (gebeDurum)
							logger.debug("");
					}

					if (vg.getVardiya().isCalisma() && !vg.getVardiya().getGebelik())
						++calisilanGunSayisi;
					if (tatilGunleriMap.containsKey(key)) {
						Tatil tatil = tatilGunleriMap.get(key);
						if (tatil.isYarimGunMu()) {
							Vardiya vardiya = vg.getIslemVardiya();
							try {
								double calSure = vg.getCalismaSuresi() - vg.getResmiTatilSure();
								double arifeSure = vg.getCalismaSuresi() > vg.getIslemVardiya().getNetCalismaSuresi() && calSure > yarimGun ? calSure : yarimGun;
								if (arifeSure > yarimGun)
									arifeSure = yarimGun;
								if (tatil.getArifeSonraVardiyaDenklestirmeVar() != null && tatil.getArifeSonraVardiyaDenklestirmeVar()) {
									if (tatil.getVardiyaMap() != null && tatil.getVardiyaMap().containsKey(vardiya.getId())) {
										Vardiya vardiyaTatil = tatil.getVardiyaMap().get(vardiya.getId());
										vardiya.setArifeBaslangicTarihi(vardiyaTatil.getArifeBaslangicTarihi());
										if (vg.getPdksPersonel().getPdksSicilNo().equals("2737"))
											logger.debug("");
										if (vardiyaTatil.getArifeBaslangicTarihi() != null && vardiyaTatil.getArifeCalismaSure() != null) {
											if (vardiyaTatil.getArifeBaslangicTarihi().getTime() <= vardiya.getVardiyaBasZaman().getTime()) {
												if (vg.getResmiTatilSure() > 0.0d) {
													saatToplami -= vg.getCalismaSuresi() - vg.getResmiTatilSure();
													vg.setResmiTatilSure(vg.getCalismaSuresi());
												}
												if (vardiyaTatil.isArifeCalismaSaatYokCGSDussun())
													arifeSure = 0.0d;
												// yarimGun = 0.0d;
											} else if (vardiyaTatil.getArifeBaslangicTarihi().getTime() >= vardiya.getVardiyaBitZaman().getTime()) {
												// arifeSure = 0.0d;
												if (vg.getCalismaSuresi() > 0.0d) {
													yarimGun = 0.0d;
												} else if (vardiyaTatil.isArifeCalismaSaatYokCGSDussun())
													arifeSure = 0.0d;

											} else if (vg.getCalismaSuresi() <= 0.0d) {
												// TODO Arife çalışmıyorsa ÇGS sayma
												if (vardiyaTatil.isArifeCalismaSaatYokCGSDussun())
													arifeSure = 0.0d;
											}
										}
									}

									if (vg.getCalismaSuresi() > 0.0d)
										logger.debug(vg.getVardiyaKeyStr() + " " + vg.getCalismaSuresi() + " " + hesaplananSure + " " + arifeSure);
								}
								hesaplananSure += (arifeSure - yarimGun);
							} catch (Exception e) {
								logger.error(e);
								e.printStackTrace();
							}
						}
					}

				}

			}
		}
		if (gebelikGuncelle && gebeDurum && personelDenklestirme.getSutIzniSaatSayisi() != null && personelDenklestirme.getSutIzniSaatSayisi().doubleValue() > 0.0d) {
			try {
				double sutIzniSaatSayisi = personelDenklestirme.getSutIzniSaatSayisi();
				if (sutIzniSaatSayisi < calismaModeliAy.getSure())
					hesaplananSure = sutIzniSaatSayisi;

			} catch (Exception e) {
			}

		}

		planlananSure = hesaplananSure;

	}

	/**
	 * @param tatilGunleriMap
	 * @param calismaModeli
	 * @return
	 */
	private double getArifeToplamSure(TreeMap<String, Tatil> tatilGunleriMap, CalismaModeli calismaModeli) {
		double arifeToplamSure = 0.0d;
		if (personelDenklestirme.isSuaDurumu() || personelDenklestirme.isPartTimeDurumu()) {
			if (calismaModeli != null && tatilGunleriMap != null && vardiyalar != null && !vardiyalar.isEmpty()) {
				OrtakIslemler ortakIslemler = new OrtakIslemler();
				Calendar cal = Calendar.getInstance();
				for (VardiyaGun vg : vardiyalar) {
					cal.setTime(vg.getVardiyaDate());
					int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
					if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY || vg.getVardiya() == null || vg.getVardiya().getId() == null)
						continue;
					String key = vg.getVardiyaDateStr();
					if (!tatilGunleriMap.containsKey(key))
						continue;
					Tatil tatil = tatilGunleriMap.get(key);
					if (tatil.isYarimGunMu()) {
						double sure = ortakIslemler.getCalismayanSure(calismaModeli, vg);
						// if (vg.getIslemVardiya() != null) {
						// Vardiya islemVardiya = vg.getIslemVardiya();
						// if (islemVardiya.getArifeCalismaSure() != null && islemVardiya.getArifeCalismaSure().doubleValue() > 0.0d)
						// sure = islemVardiya.getArifeCalismaSure();
						// }
						arifeToplamSure += sure;
					}

				}
				ortakIslemler = null;
			}
		}
		return arifeToplamSure;
	}

	public void planSureHesapla(TreeMap<String, Tatil> tatilGunleriMap) {
		planlananSureHesapla(tatilGunleriMap);

	}

	public boolean isGorevYeriSec() {
		return gorevYeriSec;
	}

	public void setGorevYeriSec(boolean gorevYeriSec) {
		this.gorevYeriSec = gorevYeriSec;
	}

	public DenklestirmeAy getDenklestirmeAy() {
		return denklestirmeAy;
	}

	public void setDenklestirmeAy(DenklestirmeAy denklestirmeAy) {
		this.denklestirmeAy = denklestirmeAy;
	}

	public Double getPlanlananSure() {
		return planlananSure;
	}

	public static double getGunlukCalismaSuresi() {
		return gunlukCalismaSuresi;
	}

	public static void setGunlukCalismaSuresi(double gunlukCalismaSuresi) {
		AylikPuantaj.gunlukCalismaSuresi = gunlukCalismaSuresi;
	}

	// public void addIzinSuresi(double sure) {
	// izinSuresi += sure;
	// }
	//
	// public void addIzinSuresi() {
	// izinSuresi += 1.0d;
	// }

	public double getIzinSuresi() {
		return izinSuresi;
	}

	public boolean isSecili() {
		return secili;
	}

	public void setSecili(boolean secili) {
		this.secili = secili;
	}

	public boolean isOnayDurum() {
		return onayDurum;
	}

	public void setOnayDurum(boolean value) {
		if (pdksPersonel != null)
			logger.debug(pdksPersonel.getPdksSicilNo() + " " + pdksPersonel.getAdSoyad() + " " + value);
		this.onayDurum = value;
	}

	public AylikPuantaj getGecenAylikPuantaj() {
		return gecenAylikPuantaj;
	}

	public void setGecenAylikPuantaj(AylikPuantaj gecenAylikPuantaj) {
		this.gecenAylikPuantaj = gecenAylikPuantaj;
	}

	private double getGecenAyDenklestirmeHesapla() {
		Double fark = null;
		PersonelDenklestirme pdksPersonelDenklestirmeGecenAy = null;
		if (personelDenklestirme != null && personelDenklestirme.getPersonelDenklestirmeGecenAy() != null) {
			pdksPersonelDenklestirmeGecenAy = personelDenklestirme.getPersonelDenklestirmeGecenAy();
			if (pdksPersonelDenklestirmeGecenAy != null)
				if ((pdksPersonelDenklestirmeGecenAy.getDurum() || pdksPersonelDenklestirmeGecenAy.isErpAktarildi()) && pdksPersonelDenklestirmeGecenAy.getDevredenSure() != null)
					fark = pdksPersonelDenklestirmeGecenAy.getKalanSure();
		}

		if (fark == null)
			fark = 0d;
		return fark;
	}

	public Double getGecenAyFazlaMesai() {
		// if (gecenAyFazlaMesai == null)
		double deger = getGecenAyDenklestirmeHesapla();
		gecenAyFazlaMesai = deger;
		return gecenAyFazlaMesai;
	}

	public Double getGecenAyFazlaMesai(User user) {
		// if (gecenAyFazlaMesai == null)
		double deger = getGecenAyDenklestirmeHesapla(user);
		gecenAyFazlaMesai = deger;
		return gecenAyFazlaMesai;
	}

	private double getGecenAyDenklestirmeHesapla(User user) {
		Double fark = null;
		PersonelDenklestirme personelDenklestirmeGecenAy = null;
		if (personelDenklestirme != null && personelDenklestirme.getPersonelDenklestirmeGecenAy() != null) {
			personelDenklestirmeGecenAy = personelDenklestirme.getPersonelDenklestirmeGecenAy();
			if (personelDenklestirmeGecenAy != null && personelDenklestirmeGecenAy.getDurum() && personelDenklestirmeGecenAy.isOnaylandi()) {
				// if ((!personelDenklestirmeGecenAy.getDenklestirmeAy().isDurum(user) || personelDenklestirmeGecenAy.isErpAktarildi()) && personelDenklestirmeGecenAy.getDevredenSure() != null)
				if (personelDenklestirmeGecenAy.getDevredenSure() != null)
					fark = personelDenklestirmeGecenAy.getKalanSure();
			}
		}

		if (fark == null)
			fark = 0d;
		return fark;
	}

	public void setGecenAyFazlaMesai(Double gecenAyFazlaMesai) {
		this.gecenAyFazlaMesai = gecenAyFazlaMesai;
	}

	public List<VardiyaHafta> getVardiyaHaftaList() {
		return vardiyaHaftaList;
	}

	public void setVardiyaHaftaList(List<VardiyaHafta> vardiyaHaftaList) {
		this.vardiyaHaftaList = vardiyaHaftaList;
	}

	public AylikPuantaj getSablonAylikPuantaj() {
		return sablonAylikPuantaj;
	}

	public void setSablonAylikPuantaj(AylikPuantaj sablonAylikPuantaj) {
		this.sablonAylikPuantaj = sablonAylikPuantaj;
	}

	public boolean isFazlaMesaiHesapla() {
		return fazlaMesaiHesapla;
	}

	public void setFazlaMesaiHesapla(boolean value) {
		if (value) {
			if (personelDenklestirme == null || personelDenklestirme.isDenklestirmeDurum() == false || personelDenklestirme.isOnaylandi() == false)
				value = false;
			else if (pdksPersonel != null && yonetici == null) {
				Personel yoneticisi = pdksPersonel.getYoneticisi();
				if (yoneticisi != null && (yonetici == null || yonetici.getId() == null)) {
					String dateStr = (yil * 100 + ay) + "01";
					Date basTarih = PdksUtil.convertToJavaDate(dateStr, "yyyyMMdd");
					Date bitTarih = PdksUtil.tariheGunEkleCikar(PdksUtil.tariheAyEkleCikar(basTarih, 1), -1);
					try {
						if (yoneticisi.getIseGirisTarihi() != null && yoneticisi.getSskCikisTarihi() != null && yoneticisi.getIseGirisTarihi().getTime() <= bitTarih.getTime() && yoneticisi.getSskCikisTarihi().getTime() >= basTarih.getTime())
							yonetici = yoneticisi;
					} catch (Exception e) {
						logger.equals(e);
					}

				}
				value = (yonetici != null && yonetici.getId() != null) || pdksPersonel.isSanalPersonelMi();
			}

		}
		this.fazlaMesaiHesapla = value;
	}

	public void setSaatToplami(Double value) {
		if (value != null && value.doubleValue() > 0.0d)
			logger.debug(value);
		this.saatToplami = value;
	}

	public void setResmiTatilToplami(Double value) {
		if (value != null && value.doubleValue() > 0.0d)
			logger.debug(value);
		this.resmiTatilToplami = value;
	}

	public void addResmiTatilToplami(Double sure) {
		if (sure != null && sure.doubleValue() > 0.0d) {
			logger.debug(sure);
			this.resmiTatilToplami += sure;
		}

	}

	public void setFazlaMesaiSure(Double value) {
		if (value != null && value.doubleValue() != 0.0d)
			logger.debug(value);
		this.fazlaMesaiSure = value;
	}

	public void setPlanlananSure(Double value) {
		if (value != null && value.doubleValue() > 0.0d)
			logger.debug(value);
		this.planlananSure = value;
	}

	public void setIzinSuresi(Double value) {
		if (value != null && value.doubleValue() != 0.0d)
			logger.debug(value);
		this.izinSuresi = value;
	}

	public Double getHesaplananSure() {
		return hesaplananSure;
	}

	public void setHesaplananSure(Double hesaplananSure) {
		this.hesaplananSure = hesaplananSure;
	}

	public Double getDevredenSure() {
		if (devredenSure == null)
			devredenSure = 0.0d;
		return devredenSure;
	}

	public void setDevredenSure(Double value) {
		if (value != null && value.doubleValue() != 0.0d)
			logger.debug(value);
		this.devredenSure = value;
	}

	public String getBolumBordroAltBirim() {
		String deger = null;
		if (pdksPersonel != null) {
			if (pdksPersonel.getEkSaha3() != null || pdksPersonel.getPlanGrup2() != null) {
				deger = "";
				if (pdksPersonel.getEkSaha3() != null)
					deger = pdksPersonel.getEkSaha3().getAciklama();
				if (pdksPersonel.getPlanGrup2() != null)
					deger += " " + pdksPersonel.getPlanGrup2().getAciklama();
			}
		}
		return deger;

	}

	/**
	 * @param personel
	 * @param gorevliPersonelMap
	 * @return
	 */
	public static boolean helpPersonel(Personel personel, HashMap<String, Personel> gorevliPersonelMap) {
		return personel != null && gorevliPersonelMap != null && gorevliPersonelMap.containsKey(personel.getPdksSicilNo());

	}

	public Double getOdenenSure() {
		return odenenSure;
	}

	public void setOdenenSure(Double value) {
		if (value != null && value.doubleValue() != 0.0d)
			logger.debug(value);
		this.odenenSure = value;
	}

	public DenklestirmeAy getDenklestirmeGecenAy() {
		return denklestirmeGecenAy;
	}

	public void setDenklestirmeGecenAy(DenklestirmeAy value) {
		this.denklestirmeGecenAy = value;
	}

	public DenklestirmeAy getDenklestirmeGelecekAy() {
		return denklestirmeGelecekAy;
	}

	public void setDenklestirmeGelecekAy(DenklestirmeAy value) {
		this.denklestirmeGelecekAy = value;
	}

	public PersonelDenklestirme getPersonelDenklestirmeGelecekAy() {
		return personelDenklestirmeGelecekAy;
	}

	public void setPersonelDenklestirmeGelecekAy(PersonelDenklestirme personelDenklestirmeGelecekAy) {
		this.personelDenklestirmeGelecekAy = personelDenklestirmeGelecekAy;
	}

	public PersonelDenklestirme getPersonelDenklestirmeGecenAy() {
		return personelDenklestirmeGecenAy;
	}

	public void setPersonelDenklestirmeGecenAy(PersonelDenklestirme personelDenklestirmeGecenAy) {
		this.personelDenklestirmeGecenAy = personelDenklestirmeGecenAy;
	}

	public VardiyaGun getVardiyaGun() {
		return vardiyaGun;
	}

	public void setVardiyaGun(VardiyaGun value) {
		this.vardiyaGun = value;
	}

	public int getAksamVardiyaSayisi() {
		return aksamVardiyaSayisi;
	}

	public void setAksamVardiyaSayisi(int aksamVardiyaSayisi) {
		this.aksamVardiyaSayisi = aksamVardiyaSayisi;
	}

	public Double getUcretiOdenenMesaiSure() {
		return ucretiOdenenMesaiSure;
	}

	public void setUcretiOdenenMesaiSure(Double value) {
		if (value != null && value.doubleValue() != 0.0d)
			logger.debug(value);
		this.ucretiOdenenMesaiSure = value;
	}

	public boolean isVardiyaOlustu() {
		return vardiyaOlustu;
	}

	public void setVardiyaOlustu(boolean vardiyaOlustu) {
		this.vardiyaOlustu = vardiyaOlustu;
	}

	public boolean isVardiyaDegisti() {
		return vardiyaDegisti;
	}

	public void setVardiyaDegisti(boolean vardiyaDegisti) {
		this.vardiyaDegisti = vardiyaDegisti;
	}

	public double getAksamVardiyaSaatSayisi() {
		return aksamVardiyaSaatSayisi;
	}

	public boolean isDonemBitti() {
		return donemBitti;
	}

	public Boolean getUyariDurum() {
		boolean uyariDurum = this.getSonGun() == null || this.getSonGun().before(new Date());
		return uyariDurum;
	}

	public void setDonemBitti(boolean value) {
		if (value)
			logger.debug(pdksPersonel.getPdksSicilNo());
		this.donemBitti = value;
	}

	public void setAksamVardiyaSaatSayisi(Double value) {
		if (value != null && value.doubleValue() > 0.0d)
			logger.debug(value);
		this.aksamVardiyaSaatSayisi = value;
	}

	public boolean isVardiyaSua() {
		return vardiyaSua;
	}

	public void setVardiyaSua(boolean vardiyaSua) {
		this.vardiyaSua = vardiyaSua;
	}

	/**
	 * @param fazlaMesaiOde
	 * @param hesaplananBuAySure
	 * @param gecenAydevredenSure
	 * @return
	 */

	public PersonelDenklestirme getPersonelDenklestirme(Boolean fazlaMesaiOde, double hesaplananBuAySure, double gecenAyDevredenSure) {
		PersonelDenklestirme personelDenklestirme = new PersonelDenklestirme();
		double devredenSure = 0;
		double hesaplananSure = gecenAyDevredenSure + hesaplananBuAySure;
		Double odenenSure = 0d;
		Double odenenFazlaMesaiSaati = PdksUtil.getOdenenFazlaMesaiSaati();
		Double barajOdeme = odenenFazlaMesaiSaati;
		if (odenenFazlaMesaiSaati <= 0.0) {
			if (hesaplananSure > 0) {
				barajOdeme = hesaplananSure + 1;
			}
		}
		if (hesaplananSure > 0 && (fazlaMesaiOde != null && fazlaMesaiOde) || (hesaplananSure >= barajOdeme)) {
			odenenSure = hesaplananSure;
		} else {
			if (hesaplananSure > 0 && gecenAyDevredenSure > 0) {
				if (hesaplananBuAySure > 0) {
					odenenSure = gecenAyDevredenSure;
					devredenSure = hesaplananBuAySure;
				} else
					odenenSure = hesaplananSure;

			} else
				devredenSure = hesaplananSure;
		}

		if (odenenSure > 0)
			personelDenklestirme.setOdenenSure(odenenSure);
		else
			devredenSure = hesaplananSure;
		// if (personelDenklestirme.g personelDenklestirme.isOnaylandi())
		personelDenklestirme.setDevredenSure(devredenSure);
		personelDenklestirme.setHesaplananSure(hesaplananSure);
		return personelDenklestirme;
	}

	public Double getOffSure() {
		return offSure;
	}

	public void setOffSure(Double offSure) {
		this.offSure = offSure;
	}

	public Double getHaftaCalismaSuresi() {
		return haftaCalismaSuresi;
	}

	public void setHaftaCalismaSuresi(Double value) {
		if (value != null && value.doubleValue() > 0.0d) {
			logger.debug(value);
		}
		this.haftaCalismaSuresi = value;
	}

	public static double getGunlukAnneCalismaSuresi() {
		return gunlukAnneCalismaSuresi;
	}

	public void addHaftaCalismaSuresi(Double sure) {
		if (sure != null && sure.doubleValue() > 0.0d && fiiliHesapla) {
			logger.debug(sure);
			this.haftaCalismaSuresi += sure;
		}

	}

	public static void setGunlukAnneCalismaSuresi(double gunlukAnneCalismaSuresi) {
		AylikPuantaj.gunlukAnneCalismaSuresi = gunlukAnneCalismaSuresi;
	}

	public CalismaModeliAy getCalismaModeliAy() {
		if (calismaModeliAy == null && personelDenklestirme != null)
			calismaModeliAy = personelDenklestirme.getCalismaModeliAy();
		return calismaModeliAy;
	}

	public void setCalismaModeliAy(CalismaModeliAy value) {
		if (value != null) {
			this.setCalismaModeli(value.getCalismaModeli());
			if (denklestirmeAy == null)
				this.denklestirmeAy = value.getDenklestirmeAy();
		}

		this.calismaModeliAy = value;
	}

	public Double getSaatlikIzinSuresi() {
		return saatlikIzinSuresi;
	}

	public void setSaatlikIzinSuresi(Double saatlikIzinSuresi) {
		this.saatlikIzinSuresi = saatlikIzinSuresi;
	}

	public void addSaatlikIzinSuresi(double value) {
		this.saatlikIzinSuresi += value;
	}

	public CalismaModeli getCalismaModeli() {
		return calismaModeli;
	}

	public void setCalismaModeli(CalismaModeli calismaModeli) {
		this.calismaModeli = calismaModeli;
	}

	public ArrayList<HareketKGS> getHareketler() {
		return hareketler;
	}

	public void setHareketler(ArrayList<HareketKGS> hareketler) {
		this.hareketler = hareketler;
	}

	public boolean isAyrikHareketVar() {
		return ayrikHareketVar;
	}

	public void setAyrikHareketVar(boolean ayrikHareketVar) {
		this.ayrikHareketVar = ayrikHareketVar;
	}

	public Personel getYonetici() {
		return yonetici;
	}

	public void setYonetici(Personel yonetici) {
		this.yonetici = yonetici;
	}

	public Personel getYonetici2() {
		return yonetici2;
	}

	public void setYonetici2(Personel yonetici2) {
		this.yonetici2 = yonetici2;
	}

	public boolean isFiiliHesapla() {
		return fiiliHesapla;
	}

	public void setFiiliHesapla(boolean fiiliHesapla) {
		this.fiiliHesapla = fiiliHesapla;
	}

	public boolean isFazlaMesaiIzinKontrol() {
		return fazlaMesaiIzinKontrol;
	}

	public void setFazlaMesaiIzinKontrol(boolean fazlaMesaiIzinKontrol) {
		this.fazlaMesaiIzinKontrol = fazlaMesaiIzinKontrol;
	}

	public boolean isEksikCalismaVar() {
		boolean eksikCalismaVar = false;
		if (PdksUtil.isSistemDestekVar() && this.getDevredenSure().doubleValue() < 0.0d) {
			double rtGun = 0.0d;
			if (this.getResmiTatilToplami() > 0.0d) {
				for (Iterator iterator = vardiyalar.iterator(); iterator.hasNext();) {
					VardiyaGun vg = (VardiyaGun) iterator.next();
					if (vg.isAyinGunu() && vg.getResmiTatilSure() > 0.0d && vg.getResmiTatilSure() < vg.getCalismaSuresi())
						rtGun += vg.getResmiTatilSure();
				}
			}
			double saatGenelToplam = this.getSaatToplami() + rtGun;
			if (saatGenelToplam < this.getPlanlananSure()) {
				double calismaOrani = saatGenelToplam / this.getPlanlananSure();
				eksikCalismaVar = calismaOrani < eksikCalismaUyariYuzdesi;
			}
		}

		return eksikCalismaVar;
	}

	public static Double getEksikCalismaUyariYuzdesi() {

		return eksikCalismaUyariYuzdesi;
	}

	public static void setEksikCalismaUyariYuzdesi(Double eksikCalismaUyariYuzdesi) {
		AylikPuantaj.eksikCalismaUyariYuzdesi = eksikCalismaUyariYuzdesi;
	}

	public boolean isFazlaMesaiTalepVar() {
		boolean fazlaMesaiTalepVar = false;
		if (yonetici2 != null && sirket != null)
			fazlaMesaiTalepVar = sirket.getDepartman().isFazlaMesaiTalepGirer() && sirket.isFazlaMesaiTalepGirer();
		return fazlaMesaiTalepVar;
	}

	public boolean isDenklestirilmeyenDevredenVar() {
		return denklestirilmeyenDevredenVar;
	}

	public void setDenklestirilmeyenDevredenVar(boolean denklestirilmeyenDevredenVar) {
		this.denklestirilmeyenDevredenVar = denklestirilmeyenDevredenVar;
	}

	public TreeMap<String, VardiyaGun> getVgMap() {
		return vgMap;
	}

	public void setVgMap(TreeMap<String, VardiyaGun> vgMap) {
		this.vgMap = vgMap;
	}

	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket sirket) {
		this.sirket = sirket;
	}

	public Double getKesilenSure() {
		return kesilenSure;
	}

	public void setKesilenSure(Double kesilenSure) {
		this.kesilenSure = kesilenSure;
	}

	public PersonelDenklestirmeDinamikAlan getDinamikAlan(Long key) {
		personelDenklestirmeDinamikAlan = null;
		if (dinamikAlanMap != null && key != null && dinamikAlanMap.containsKey(key))
			personelDenklestirmeDinamikAlan = dinamikAlanMap.get(key);

		return personelDenklestirmeDinamikAlan;
	}

	public PersonelDinamikAlan getPersonelDinamikAlan(Long key) {
		personelDinamikAlan = null;
		if (personelDinamikAlanMap != null && key != null && personelDinamikAlanMap.containsKey(key))
			personelDinamikAlan = personelDinamikAlanMap.get(key);

		return personelDinamikAlan;
	}

	public TreeMap<Long, PersonelDenklestirmeDinamikAlan> getDinamikAlanMap() {
		return dinamikAlanMap;
	}

	public void setDinamikAlanMap(TreeMap<Long, PersonelDenklestirmeDinamikAlan> dinamikAlanMap) {
		this.dinamikAlanMap = dinamikAlanMap;
	}

	public PersonelDenklestirmeDinamikAlan getPersonelDenklestirmeDinamikAlan() {
		return personelDenklestirmeDinamikAlan;
	}

	public void setPersonelDenklestirmeDinamikAlan(PersonelDenklestirmeDinamikAlan personelDenklestirmeDinamikAlan) {
		this.personelDenklestirmeDinamikAlan = personelDenklestirmeDinamikAlan;
	}

	public List<Integer> getIzinAdetList() {
		return izinAdetList;
	}

	public void setIzinAdetList(List<Integer> izinAdetList) {
		this.izinAdetList = izinAdetList;
	}

	public boolean isGebeDurum() {
		return gebeDurum;
	}

	public void setGebeDurum(boolean gebeDurum) {
		this.gebeDurum = gebeDurum;
	}

	public int getCalisilanGunSayisi() {
		return calisilanGunSayisi;
	}

	public void setCalisilanGunSayisi(int calisilanGunSayisi) {
		this.calisilanGunSayisi = calisilanGunSayisi;
	}

	public static Boolean getGebelikGuncelle() {
		return gebelikGuncelle;
	}

	public static void setGebelikGuncelle(Boolean gebelikGuncelle) {
		AylikPuantaj.gebelikGuncelle = gebelikGuncelle;
	}

	public Integer getIzinAdet(String key) {
		Integer adet = null;
		if (izinAdetMap != null && key != null && izinAdetMap.containsKey(key))
			adet = izinAdetMap.get(key);
		return adet;
	}

	public TreeMap<String, Integer> getIzinAdetMap() {
		return izinAdetMap;
	}

	public void setIzinAdetMap(TreeMap<String, Integer> izinAdetMap) {
		this.izinAdetMap = izinAdetMap;
	}

	public int getYarimYuvarla() {
		int yuvarlamaKatsayi = PdksUtil.getYarimYuvarlaLast();
		if (vardiyalar != null) {
			for (VardiyaGun vardiyaGun : vardiyalar) {
				if (vardiyaGun.isAyinGunu())
					yuvarlamaKatsayi = vardiyaGun.getYarimYuvarla();

			}
		}
		return yuvarlamaKatsayi;
	}

	public boolean isFazlaMesaiDurum() {
		Boolean fazlaMesaiDurum = Boolean.FALSE;
		if (personelDenklestirme != null && personelDenklestirme.getCalismaModeliAy() != null)
			fazlaMesaiDurum = personelDenklestirme.getCalismaModeliAy().isHareketKaydiVardiyaBulsunmu();

		if (fazlaMesaiDurum.equals(Boolean.FALSE) && vardiyalar != null) {
			for (VardiyaGun vardiyaGun : vardiyalar) {
				if (!fazlaMesaiDurum && vardiyaGun.isAyinGunu() && vardiyaGun.getVardiya() != null && vardiyaGun.getVardiya().getId() != null) {
					fazlaMesaiDurum = vardiyaGun.isFazlaMesaiTalepDurum();
					if (fazlaMesaiDurum)
						break;
				}

			}
		}
		return fazlaMesaiDurum;
	}

	public String getAdSoyad() {
		return pdksPersonel != null ? pdksPersonel.getAdSoyad() : "";
	}

	public PersonelDenklestirmeBordro getDenklestirmeBordro() {
		return denklestirmeBordro;
	}

	public void setDenklestirmeBordro(PersonelDenklestirmeBordro denklestirmeBordro) {
		this.denklestirmeBordro = denklestirmeBordro;
	}

	public Double getEksikCalismaSure() {
		return eksikCalismaSure;
	}

	public void setEksikCalismaSure(Double eksikCalismaSure) {
		this.eksikCalismaSure = eksikCalismaSure;
	}

	/**
	 * @return the eksikGunVar
	 */
	public boolean isEksikGunVar() {
		return eksikGunVar;
	}

	/**
	 * @param eksikGunVar
	 *            the eksikGunVar to set
	 */
	public void setEksikGunVar(boolean eksikGunVar) {
		this.eksikGunVar = eksikGunVar;
	}

	public User getLoginUser() {
		return loginUser;
	}

	public void setLoginUser(User loginUser) {
		this.loginUser = loginUser;
	}

	public TreeMap<Long, PersonelDinamikAlan> getPersonelDinamikAlanMap() {
		return personelDinamikAlanMap;
	}

	public void setPersonelDinamikAlanMap(TreeMap<Long, PersonelDinamikAlan> personelDinamikAlanMap) {
		this.personelDinamikAlanMap = personelDinamikAlanMap;
	}

	public PersonelDinamikAlan getPersonelDinamikAlan() {
		return personelDinamikAlan;
	}

	public void setPersonelDinamikAlan(PersonelDinamikAlan personelDinamikAlan) {
		this.personelDinamikAlan = personelDinamikAlan;
	}

	public PersonelDenklestirme getPersonelDenklestirme() {
		return personelDenklestirme;
	}

	public void setPersonelDenklestirmeData(PersonelDenklestirme pd) {
		if (pd != null) {
			this.personelDenklestirme = pd;
			this.setFazlaMesaiHesapla(pd.getDurum());
			this.setFazlaMesaiSure(pd.getOdenecekSure());
			this.setResmiTatilToplami(pd.getResmiTatilSure());
			this.setHaftaCalismaSuresi(pd.getHaftaCalismaSuresi());
			this.setEksikCalismaSure(pd.getEksikCalismaSure());
			this.setOdenenSure(pd.getOdenecekSure());
			this.setSaatToplami(pd.getHesaplananSure());
			this.setDevredenSure(pd.getDevredenSure());
			this.setPersonelDenklestirme(pd);
		}
	}

	public void setPersonelDenklestirme(PersonelDenklestirme pd) {
		if (pd != null) {
			this.denklestirmeAy = pd.getDenklestirmeAy();
			this.setPdksPersonel(pd.getPdksPersonel());
			this.setCalismaModeliAy(pd.getCalismaModeliAy());
			if (calismaModeliAy != null)
				this.setCalismaModeli(calismaModeliAy.getCalismaModeli());

		}
		this.personelDenklestirme = pd;
	}

}
