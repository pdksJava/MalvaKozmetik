package org.pdks.entity;

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
	public static final String COLUMN_NAME_SUT_IZNI_DURUM = "SUT_IZNI_DURUM";
	public static final String COLUMN_NAME_PART_TIME_DURUM = "PART_TIME";
	public static final String COLUMN_NAME_SUT_IZNI_SAAT = "SUT_IZNI_SAAT";
	public static final String COLUMN_NAME_EGITIM_SURESI_AKSAM_GUN_SAYISI = "EGITIM_SURESI_AKSAM_GUN";
	public static final String COLUMN_NAME_FAZLA_MESAI_IZIN_KULLAN = "FAZLA_MESAI_IZIN_KULLAN";

	public static double calismaSaatiSua = 7.0d, calismaSaatiPartTime = 4.5d;

	private Integer version = 0;

	private Personel personel;

	private CalismaModeliAy calismaModeliAy;

	private DenklestirmeAy denklestirmeAy;

	private PersonelDenklestirme personelDenklestirmeGecenAy, personelDenklestirmeDB;

	private Double planlanSure = 0d, hesaplananSure = 0d, resmiTatilSure = 0d, haftaCalismaSuresi = 0d, fazlaMesaiSure = 0d, odenenSure = 0d, devredenSure;

	private Double aksamVardiyaSayisi = 0d, aksamVardiyaSaatSayisi = 0d, sutIzniSaatSayisi = 0d;

	private Integer egitimSuresiAksamGunSayisi;

	private Boolean suaDurum, fazlaMesaiIzinKullan = Boolean.FALSE, fazlaMesaiOde, sutIzniDurum, partTime;

	private boolean erpAktarildi = Boolean.FALSE, onaylandi = Boolean.FALSE, denklestirme;

	private String mesaj;

	public PersonelDenklestirme() {
		super();
		guncellendi = null;
	}

	public PersonelDenklestirme(Personel pdksPersonel, DenklestirmeAy denklestirmeAy, CalismaModeliAy calismaModeliAy) {
		super();
		this.setPersonel(pdksPersonel);
		this.denklestirmeAy = denklestirmeAy;
		this.calismaModeliAy = calismaModeliAy;

		guncellendi = Boolean.FALSE;
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
			this.setFazlaMesaiOde(value.getFazlaMesaiOde());
			this.setFazlaMesaiIzinKullan(value.getFazlaMesaiIzinKullan());
		}
		this.personel = value;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "CALISMA_MODELI_AY_ID")
	@Fetch(FetchMode.JOIN)
	public CalismaModeliAy getCalismaModeliAy() {
		return calismaModeliAy;
	}

	public void setCalismaModeliAy(CalismaModeliAy calismaModeliAy) {
		this.calismaModeliAy = calismaModeliAy;
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
			guncellendi = this.odenenSure == null || this.odenenSure.doubleValue() != value.doubleValue();
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
			guncellendi = this.hesaplananSure == null || this.hesaplananSure.doubleValue() != value.doubleValue();
			if (guncellendi)
				logger.debug(value);
		}

		this.hesaplananSure = value;
	}

	@Column(name = COLUMN_NAME_HAFTA_TATIL_SURE)
	public Double getHaftaCalismaSuresi() {
		return haftaCalismaSuresi;
	}

	public void setHaftaCalismaSuresi(Double haftaCalismaSuresi) {
		this.haftaCalismaSuresi = haftaCalismaSuresi;
	}

	@Column(name = COLUMN_NAME_RESMI_TATIL_SURE)
	public Double getResmiTatilSure() {
		return resmiTatilSure;
	}

	public void setResmiTatilSure(Double value) {
		if (guncellendi != null && !guncellendi && value != null) {
			guncellendi = this.resmiTatilSure == null || this.resmiTatilSure.doubleValue() != value.doubleValue();
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
			guncellendi = this.fazlaMesaiSure == null || this.fazlaMesaiSure.doubleValue() != value.doubleValue();
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
			guncellendi = this.suaDurum == null || !this.suaDurum.equals(value);
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
			guncellendi = this.planlanSure == null || this.planlanSure.doubleValue() != value.doubleValue();
			if (guncellendi)
				logger.debug(value);
		}

		this.planlanSure = value;
	}

	@Column(name = "DEVREDEN_SURE")
	public Double getDevredenSure() {
		return devredenSure;
	}

	public void setDevredenSure(Double value) {
		if (guncellendi != null && !guncellendi && value != null) {
			guncellendi = this.devredenSure == null || this.devredenSure.doubleValue() != value.doubleValue();
			if (guncellendi)
				logger.debug(value);
		}

		this.devredenSure = value;
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

	public void setFazlaMesaiIzinKullan(Boolean fazlaMesaiIzinKullan) {
		this.fazlaMesaiIzinKullan = fazlaMesaiIzinKullan;
	}

	@Column(name = "FAZLA_MESAI_ODE")
	public Boolean getFazlaMesaiOde() {
		return fazlaMesaiOde;
	}

	public void setFazlaMesaiOde(Boolean value) {
		if (guncellendi != null && !guncellendi && value != null) {
			guncellendi = this.fazlaMesaiOde == null || !this.fazlaMesaiOde.equals(value);
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

	public void setAksamVardiyaSayisi(Double aksamVardiyaSayisi) {
		this.aksamVardiyaSayisi = aksamVardiyaSayisi;
	}

	@Column(name = COLUMN_NAME_AKSAM_VARDIYA_SAAT)
	public Double getAksamVardiyaSaatSayisi() {
		return aksamVardiyaSaatSayisi;
	}

	public void setAksamVardiyaSaatSayisi(Double value) {
		if (guncellendi != null && !guncellendi && value != null) {
			guncellendi = this.aksamVardiyaSaatSayisi == null || this.aksamVardiyaSaatSayisi.doubleValue() != value.doubleValue();
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
			guncellendi = this.partTime == null || this.partTime.booleanValue() != value.booleanValue();
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

	@Transient
	public Long getPersonelId() {
		return personel != null ? personel.getId() : 0L;
	}

	@Transient
	public double getKalanSure() {
		double kalanSure = devredenSure != null ? devredenSure.doubleValue() : 0d;
		return kalanSure;
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
			if (personel.getYoneticisi() != null)
				key = personel.getYoneticisi().getAdSoyad();
			if (personel.getEkSaha3() != null)
				key += "_" + personel.getEkSaha3().getAciklamatr();

			key += "_" + personel.getPdksSicilNo();
		}
		return key;
	}

	public static double getCalismaSaatiSua() {
		return calismaSaatiSua;
	}

	public static void setCalismaSaatiSua(double calismaSaatiSua) {
		PersonelDenklestirme.calismaSaatiSua = calismaSaatiSua;
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
	public Boolean isSutIzniVar() {
		return sutIzniDurum != null && sutIzniDurum.booleanValue();
	}

}
