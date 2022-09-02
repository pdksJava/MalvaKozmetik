package org.pdks.entity;

import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.security.entity.User;

@Entity(name = PersonelDenklestirme.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { PersonelDenklestirme.COLUMN_NAME_DONEM, PersonelDenklestirme.COLUMN_NAME_PERSONEL }) })
public class PersonelDenklestirme extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9060765954851374678L;
	static Logger logger = Logger.getLogger(PersonelDenklestirme.class);

	public static final String TABLE_NAME = "PERSONELDENKLESTIRME";
	public static final String COLUMN_NAME_DONEM = "DONEM_ID";
	public static final String COLUMN_NAME_PERSONEL = "PERSONEL_ID";
	public static final String COLUMN_NAME_DENKLESTIRME_DURUM = "DENKLESTIRME_DURUM";
	public static final String COLUMN_NAME_ONAYLANDI = "ONAYLANDI";
	public static final String COLUMN_NAME_ODENEN_SURE = "ODENEN_SURE";
	public static final String COLUMN_NAME_HAFTA_TATIL_SURE = "HAFTA_TATIL_SURE";
	public static final String COLUMN_NAME_RESMI_TATIL_SURE = "RESMI_TATIL_SURE";
	public static final String COLUMN_NAME_AKSAM_VARDIYA_GUN_ADET = "AKSAM_VARDIYA_ADET";
	public static final String COLUMN_NAME_AKSAM_VARDIYA_SAAT = "AKSAM_VARDIYA_SAAT";
	public static final String COLUMN_NAME_KESILEN_SURE = "KESILEN_SURE";
	public static final String COLUMN_NAME_SUT_IZNI_DURUM = "SUT_IZNI_DURUM";
	public static final String COLUMN_NAME_PART_TIME_DURUM = "PART_TIME";
	public static final String COLUMN_NAME_SUT_IZNI_SAAT = "SUT_IZNI_SAAT";
	public static final String COLUMN_NAME_EGITIM_SURESI_AKSAM_GUN_SAYISI = "EGITIM_SURESI_AKSAM_GUN";
	public static final String COLUMN_NAME_FAZLA_MESAI_IZIN_KULLAN = "FAZLA_MESAI_IZIN_KULLAN";
	public static final String COLUMN_NAME_CALISMA_MODELI_AY = "CALISMA_MODELI_AY_ID";
	public static final String COLUMN_NAME_DEVREDEN_SURE = "DEVREDEN_SURE";
	public static final String COLUMN_NAME_KISMI_ODEME_SAAT = "KISMI_ODEME_SAAT";

	public static double calismaSaatiSua = 7.0d, calismaSaatiPartTime = 4.5d;

	private Integer version = 0;

	private Personel personel;

	private CalismaModeliAy calismaModeliAy;

	private DenklestirmeAy denklestirmeAy;

	private PersonelDenklestirme personelDenklestirmeGecenAy, personelDenklestirmeDB;

	private Double planlanSure = 0d, hesaplananSure = 0d, resmiTatilSure = 0d, haftaCalismaSuresi = 0d, fazlaMesaiSure = 0d, odenenSure = 0d;

	private Double devredenSure, kesilenSure = 0d, calismaSuaSaati = calismaSaatiSua, kismiOdemeSure = 0d, aksamVardiyaSayisi = 0d, aksamVardiyaSaatSayisi = 0d, sutIzniSaatSayisi = 0d;

	private Integer egitimSuresiAksamGunSayisi;

	private Boolean suaDurum, fazlaMesaiIzinKullan = Boolean.FALSE, fazlaMesaiOde, sutIzniDurum, partTime;

	private boolean erpAktarildi = Boolean.FALSE, onaylandi = Boolean.FALSE, denklestirme;

	private Boolean guncellendi;

	private String mesaj;

	public PersonelDenklestirme() {
		super();
		this.setGuncellendi(null);
	}

	public PersonelDenklestirme(Personel pdksPersonel, DenklestirmeAy denklestirmeAy, CalismaModeliAy calismaModeliAy) {
		super();
		this.setPersonel(pdksPersonel);
		this.denklestirmeAy = denklestirmeAy;
		this.calismaModeliAy = calismaModeliAy;
		this.setGuncellendi(Boolean.FALSE);

	}

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Personel getPersonel() {
		return personel;
	}

	public void setPersonel(Personel value) {
		if (value != null && this.getId() == null) {
			if (value.isSuaOlur())
				this.setSuaDurum(value.getSuaOlabilir());
			if (value.isPartTimeCalisan())
				this.setPartTime(value.getPartTime());
			this.setDenklestirme(value.getPdks());
			this.setFazlaMesaiIzinKullan(value.getFazlaMesaiIzinKullan());
			this.setFazlaMesaiOde(value.getFazlaMesaiIzinKullan().equals(Boolean.FALSE) && value.getFazlaMesaiOde());

		}
		this.personel = value;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_CALISMA_MODELI_AY)
	@Fetch(FetchMode.JOIN)
	public CalismaModeliAy getCalismaModeliAy() {
		return calismaModeliAy;
	}

	public void setCalismaModeliAy(CalismaModeliAy value) {
		Long oldId = calismaModeliAy != null ? calismaModeliAy.getId() : 0L, newId = value != null ? value.getId() : 0L;
		if (!this.isGuncellendi())
			this.setGuncellendi(!oldId.equals(newId));
		this.calismaModeliAy = value;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_DONEM, nullable = false)
	@Fetch(FetchMode.JOIN)
	public DenklestirmeAy getDenklestirmeAy() {
		return denklestirmeAy;
	}

	public void setDenklestirmeAy(DenklestirmeAy denklestirmeAy) {
		this.denklestirmeAy = denklestirmeAy;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "GECEN_AY_DENKLESTIRME_ID")
	@Fetch(FetchMode.JOIN)
	public PersonelDenklestirme getPersonelDenklestirmeGecenAy() {
		return personelDenklestirmeGecenAy;
	}

	public void setPersonelDenklestirmeGecenAy(PersonelDenklestirme personelDenklestirmeGecenAy) {
		this.personelDenklestirmeGecenAy = personelDenklestirmeGecenAy;
	}

	@Column(name = COLUMN_NAME_ODENEN_SURE)
	public Double getOdenenSure() {
		return odenenSure;
	}

	public void setOdenenSure(Double value) {
		if (guncellendi != null && !guncellendi && value != null) {
			this.setGuncellendi(this.odenenSure == null || this.odenenSure.doubleValue() != value.doubleValue());
			if (guncellendi)
				logger.debug(value);
		}

		this.odenenSure = value;
	}

	@Column(name = "SURE")
	public Double getHesaplananSure() {
		return hesaplananSure;
	}

	public void setHesaplananSure(Double value) {
		if (guncellendi != null && !guncellendi && value != null) {
			this.setGuncellendi(this.hesaplananSure == null || this.hesaplananSure.doubleValue() != value.doubleValue());
			if (guncellendi)
				logger.debug(value);
		}

		this.hesaplananSure = value;
	}

	@Column(name = COLUMN_NAME_HAFTA_TATIL_SURE)
	public Double getHaftaCalismaSuresi() {
		return haftaCalismaSuresi;
	}

	public void setHaftaCalismaSuresi(Double value) {
		if (guncellendi != null && !guncellendi && value != null) {
			this.setGuncellendi(this.haftaCalismaSuresi == null || this.haftaCalismaSuresi.doubleValue() != value.doubleValue());
			if (guncellendi)
				logger.debug(value);
		}
		this.haftaCalismaSuresi = value;
	}

	@Column(name = COLUMN_NAME_RESMI_TATIL_SURE)
	public Double getResmiTatilSure() {
		return resmiTatilSure;
	}

	public void setResmiTatilSure(Double value) {
		if (guncellendi != null && !guncellendi && value != null) {
			this.setGuncellendi(this.resmiTatilSure == null || this.resmiTatilSure.doubleValue() != value.doubleValue());
			if (guncellendi)
				logger.debug(value);
		}

		this.resmiTatilSure = value;
	}

	@Column(name = "FAZLA_MESAI_SURE")
	public Double getFazlaMesaiSure() {
		return fazlaMesaiSure;
	}

	public void setFazlaMesaiSure(Double value) {
		if (guncellendi != null && !guncellendi && value != null) {
			this.setGuncellendi(this.fazlaMesaiSure == null || this.fazlaMesaiSure.doubleValue() != value.doubleValue());
			if (guncellendi)
				logger.debug(value);
		}

		this.fazlaMesaiSure = value;
	}

	@Column(name = "SUA_DURUM")
	public Boolean getSuaDurum() {
		return suaDurum;
	}

	public void setSuaDurum(Boolean value) {
		if (guncellendi != null && !guncellendi && value != null) {
			this.setGuncellendi(this.suaDurum == null || !this.suaDurum.equals(value));
			if (guncellendi)
				logger.debug(value);
		}

		this.suaDurum = value;
	}

	@Column(name = "PLANLANAN_SURE")
	public Double getPlanlanSure() {
		return planlanSure;
	}

	public void setPlanlanSure(Double value) {
		if (guncellendi != null && !guncellendi && value != null) {
			this.setGuncellendi(this.planlanSure == null || this.planlanSure.doubleValue() != value.doubleValue());
			if (guncellendi)
				logger.debug(value);
		}

		this.planlanSure = value;
	}

	@Column(name = COLUMN_NAME_DEVREDEN_SURE)
	public Double getDevredenSure() {
		return devredenSure;
	}

	public void setDevredenSure(Double value) {
		if (guncellendi != null && !guncellendi && value != null) {
			this.setGuncellendi(this.devredenSure == null || this.devredenSure.doubleValue() != value.doubleValue());
			if (guncellendi)
				logger.debug(value);
		}
		this.devredenSure = value;
	}

	@Column(name = COLUMN_NAME_KISMI_ODEME_SAAT)
	public Double getKismiOdemeSure() {
		return kismiOdemeSure;
	}

	public void setKismiOdemeSure(Double value) {
		if (guncellendi != null && !guncellendi && value != null) {
			this.setGuncellendi(this.kismiOdemeSure == null || this.kismiOdemeSure.doubleValue() != value.doubleValue());
			if (guncellendi)
				logger.debug(value);
		}
		this.kismiOdemeSure = value;
	}

	@Column(name = "ERP_AKTARILDI")
	public boolean isErpAktarildi() {
		return erpAktarildi;
	}

	public void setErpAktarildi(boolean erpAktarildi) {
		this.erpAktarildi = erpAktarildi;
	}

	@Column(name = COLUMN_NAME_FAZLA_MESAI_IZIN_KULLAN)
	public Boolean getFazlaMesaiIzinKullan() {
		return fazlaMesaiIzinKullan;
	}

	public void setFazlaMesaiIzinKullan(Boolean value) {
		if (guncellendi != null && !guncellendi && value != null) {
			this.setGuncellendi(this.fazlaMesaiIzinKullan == null || !this.fazlaMesaiIzinKullan.equals(value));
			if (guncellendi)
				logger.debug(value);
		}

		this.fazlaMesaiIzinKullan = value;
	}

	@Transient
	public Boolean isFazlaMesaiIzinKullanacak() {
		return fazlaMesaiIzinKullan != null && fazlaMesaiIzinKullan.booleanValue();
	}

	@Column(name = "FAZLA_MESAI_ODE")
	public Boolean getFazlaMesaiOde() {
		return fazlaMesaiOde;
	}

	public void setFazlaMesaiOde(Boolean value) {
		if (guncellendi != null && !guncellendi && value != null) {
			this.setGuncellendi(this.fazlaMesaiOde == null || !this.fazlaMesaiOde.equals(value));
			if (guncellendi)
				logger.debug(value);
		}

		this.fazlaMesaiOde = value;
	}

	@Column(name = COLUMN_NAME_ONAYLANDI)
	public boolean isOnaylandi() {
		return onaylandi;
	}

	public void setOnaylandi(boolean onaylandi) {
		this.onaylandi = onaylandi;
	}

	@Column(name = COLUMN_NAME_DENKLESTIRME_DURUM)
	public boolean isDenklestirme() {
		return denklestirme;
	}

	public void setDenklestirme(boolean denklestirme) {
		this.denklestirme = denklestirme;
	}

	@Column(name = COLUMN_NAME_AKSAM_VARDIYA_GUN_ADET)
	public Double getAksamVardiyaSayisi() {
		return aksamVardiyaSayisi;
	}

	public void setAksamVardiyaSayisi(Double value) {
		if (guncellendi != null && !guncellendi && value != null) {
			this.setGuncellendi(this.aksamVardiyaSayisi == null || this.aksamVardiyaSayisi.doubleValue() != value.doubleValue());
			if (guncellendi)
				logger.debug(value);
		}
		this.aksamVardiyaSayisi = value;
	}

	@Column(name = COLUMN_NAME_AKSAM_VARDIYA_SAAT)
	public Double getAksamVardiyaSaatSayisi() {
		return aksamVardiyaSaatSayisi;
	}

	public void setAksamVardiyaSaatSayisi(Double value) {
		if (guncellendi != null && !guncellendi && value != null) {
			this.setGuncellendi(this.aksamVardiyaSaatSayisi == null || this.aksamVardiyaSaatSayisi.doubleValue() != value.doubleValue());
			if (guncellendi)
				logger.debug(value);
		}

		this.aksamVardiyaSaatSayisi = value;
	}

	@Column(name = COLUMN_NAME_PART_TIME_DURUM)
	public Boolean getPartTime() {
		return partTime;
	}

	public void setPartTime(Boolean value) {
		if (guncellendi != null && !guncellendi && value != null) {
			this.setGuncellendi(this.partTime == null || this.partTime.booleanValue() != value.booleanValue());
			if (guncellendi)
				logger.debug(value);
		}
		this.partTime = value;
	}

	@Column(name = COLUMN_NAME_SUT_IZNI_DURUM)
	public Boolean getSutIzniDurum() {
		return sutIzniDurum;
	}

	public void setSutIzniDurum(Boolean sutIzniDurum) {
		this.sutIzniDurum = sutIzniDurum;
	}

	@Column(name = COLUMN_NAME_SUT_IZNI_SAAT)
	public Double getSutIzniSaatSayisi() {
		return sutIzniSaatSayisi;
	}

	public void setSutIzniSaatSayisi(Double sutIzniSaatSayisi) {
		this.sutIzniSaatSayisi = sutIzniSaatSayisi;
	}

	@Column(name = COLUMN_NAME_EGITIM_SURESI_AKSAM_GUN_SAYISI)
	public Integer getEgitimSuresiAksamGunSayisi() {
		return egitimSuresiAksamGunSayisi;
	}

	public void setEgitimSuresiAksamGunSayisi(Integer egitimSuresiAksamGunSayisi) {
		this.egitimSuresiAksamGunSayisi = egitimSuresiAksamGunSayisi;
	}

	@Column(name = COLUMN_NAME_KESILEN_SURE)
	public Double getKesilenSure() {
		return kesilenSure;
	}

	public void setKesilenSure(Double kesilenSure) {
		this.kesilenSure = kesilenSure;
	}

	@Transient
	public Long getPersonelId() {
		return personel != null ? personel.getId() : 0L;
	}

	@Transient
	public Boolean getGuncellendi() {
		return guncellendi;
	}

	@Transient
	public Boolean isGuncellendi() {
		return guncellendi != null && guncellendi.booleanValue();
	}

	public void setGuncellendi(Boolean value) {
		if (value != null && value)
			logger.debug(value);
		this.guncellendi = value;
	}

	@Transient
	public double getKalanSure() {
		double kalanSure = devredenSure != null ? devredenSure.doubleValue() : 0d;
		return kalanSure;
	}

	/**
	 * @param izinSure
	 * @return
	 */
	@Transient
	public Double getMaksimumSure(double izinSure, double arifeToplamSure) {
		double aylikSure = calismaModeliAy != null ? calismaModeliAy.getSure() : denklestirmeAy.getSure();
		if (calismaModeliAy != null && calismaModeliAy.getCalismaModeli().getToplamGunGuncelle())
			aylikSure = planlanSure;
		if (izinSure != 0.0d)
			aylikSure -= izinSure;
		Double gunlukCalismaSuresi = calismaModeliAy != null ? calismaModeliAy.getCalismaModeli().getHaftaIci() : null;
		if (isSuaDurumu()) {
			aylikSure = aylikSureHesapla(aylikSure - arifeToplamSure, calismaSuaSaati, gunlukCalismaSuresi) + arifeToplamSure;
		} else if (isPartTimeDurumu())
			aylikSure = aylikSureHesapla(aylikSure - arifeToplamSure, calismaSaatiPartTime, gunlukCalismaSuresi) + arifeToplamSure;
		double sutIzniSure = sutIzniSaatSayisi != null && sutIzniSaatSayisi.doubleValue() > 0.0d && sutIzniSaatSayisi.doubleValue() != denklestirmeAy.getToplamIzinSure() && sutIzniSaatSayisi.doubleValue() <= aylikSure ? sutIzniSaatSayisi.doubleValue() : denklestirmeAy.getToplamIzinSure();
		double sure = sutIzniDurum == null || sutIzniDurum.equals(Boolean.FALSE) || planlanSure == 0 ? aylikSure : sutIzniSure;

		return sure;
	}

	/**
	 * @param sure
	 * @param katsayi
	 * @param gunlukCalismaSuresi
	 * @return
	 */
	public static double aylikSureHesapla(double sure, double katsayi, Double gunlukCalismaSuresi) {
		if (gunlukCalismaSuresi == null)
			gunlukCalismaSuresi = AylikPuantaj.getGunlukCalismaSuresi();
		int gun = new BigDecimal(sure / AylikPuantaj.getGunlukCalismaSuresi()).intValue();
		double hesaplananSure = (katsayi * gun) + (sure - gunlukCalismaSuresi * gun);
		return hesaplananSure;
	}

	/**
	 * @param sure
	 * @param gunlukSaat
	 * @return
	 */
	public static double aylikSureArtikHesapla(double sure, double gunlukSaat) {
		int gunAdet = new Double(sure / AylikPuantaj.getGunlukCalismaSuresi()).intValue();
		double kalanSure = sure - (gunAdet * AylikPuantaj.getGunlukCalismaSuresi());
		double hesaplananSure = (gunAdet * gunlukSaat) + (kalanSure > gunlukSaat ? gunlukSaat : kalanSure);
		return hesaplananSure;
	}

	@Transient
	public Double getOdenecekSure() {
		Double odenecekSure = odenenSure;
		return odenecekSure;
	}

	@Transient
	public String getMesaj() {
		return mesaj;
	}

	@Transient
	public boolean isKapandi() {
		boolean kapandi = (erpAktarildi) || (denklestirmeAy == null || !denklestirmeAy.getDurum());
		return kapandi;
	}

	public void setMesaj(String mesaj) {
		this.mesaj = mesaj;
	}

	@Transient
	public boolean isSuaDurumu() {
		return suaDurum != null && suaDurum.booleanValue();
	}

	@Transient
	public boolean isPartTimeDurumu() {
		return partTime != null && partTime.booleanValue();
	}

	@Transient
	public String getSicilNo() {
		return personel != null ? personel.getPdksSicilNo() : "";
	}

	@Transient
	public PersonelDenklestirme getPersonelDenklestirmeDB() {
		return personelDenklestirmeDB;
	}

	public void setPersonelDenklestirmeDB(PersonelDenklestirme personelDenklestirmeDB) {
		this.personelDenklestirmeDB = personelDenklestirmeDB;
	}

	@Transient
	public String getSortKey() {
		String key = "";
		if (personel != null) {
			if (personel.getPdksYonetici() != null)
				key = personel.getPdksYonetici().getAdSoyad();
			if (personel.getEkSaha3() != null)
				key += "_" + personel.getEkSaha3().getAciklamatr();

			key += "_" + personel.getPdksSicilNo();
		}
		return key;
	}

	public static double getCalismaSaatiPartTime() {
		return calismaSaatiPartTime;
	}

	public static void setCalismaSaatiPartTime(double calismaSaatiPartTime) {
		PersonelDenklestirme.calismaSaatiPartTime = calismaSaatiPartTime;
	}

	@Transient
	public String getKontratliSortKey() {
		String str = personel != null ? personel.getKontratliSortKey() : (id != null ? id.toString() : "");
		return str;
	}

	@Transient
	public boolean isSutIzniVar() {
		return sutIzniDurum != null && sutIzniDurum.booleanValue();
	}

	@Transient
	public boolean isKapandi(User user) {
		boolean kapandi = (erpAktarildi) || (denklestirmeAy == null || !denklestirmeAy.isDurum(user));
		return kapandi;
	}

	@Transient
	public Double getCalismaSuaSaati() {
		return calismaSuaSaati;
	}

	public void setCalismaSuaSaati(Double calismaSuaSaati) {
		this.calismaSuaSaati = calismaSuaSaati;
	}

	public static double getCalismaSaatiSua() {
		return calismaSaatiSua;
	}

	public static void setCalismaSaatiSua(double calismaSaatiSua) {
		PersonelDenklestirme.calismaSaatiSua = calismaSaatiSua;
	}

}
