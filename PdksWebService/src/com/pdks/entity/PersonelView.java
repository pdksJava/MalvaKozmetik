package com.pdks.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;

@Entity(name = PersonelView.VIEW_NAME)
@Immutable
public class PersonelView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6144167331942730263L;
	static Logger logger = Logger.getLogger(PersonelView.class);
	public static final String VIEW_NAME = "VIEW_PERSONEL_SIRKET";
	public static final String COLUMN_NAME_ID = "ID";
	public static final String COLUMN_NAME_AD = "AD";
	public static final String COLUMN_NAME_SOYAD = "SOYAD";
	public static final String COLUMN_NAME_SICIL_NO = "PERSONEL_NO";
	public static final String COLUMN_NAME_DURUM = "DURUM";
	public static final String COLUMN_NAME_PERSONEL = "PERSONEL";
	public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";

	private Long id;
	private Personel pdksPersonel;
	private PersonelKGS personelKGS;
	private String adi, soyadi;
	private User kullanici;
	private String pdksPersonelAciklama, kgsSicilNo, ccAdres, bccAdres, hareketAdres;
	private Long pdksPersonelId, kullaniciId;
	private Boolean durum;

	private Personel yonetici1, yonetici2;

	@Id
	@Column(name = COLUMN_NAME_ID)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = COLUMN_NAME_AD)
	public String getAdi() {
		return adi;
	}

	public void setAdi(String adi) {
		this.adi = adi;
	}

	@Column(name = COLUMN_NAME_SOYAD)
	public String getSoyadi() {
		return soyadi;
	}

	public void setSoyadi(String soyadi) {
		this.soyadi = soyadi;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(insertable = false, updatable = false, name = COLUMN_NAME_PERSONEL, referencedColumnName = "ID", nullable = true)
	@Fetch(FetchMode.JOIN)
	public Personel getPdksPersonel() {
		return pdksPersonel;
	}

	public void setPdksPersonel(Personel pdksPersonel) {
		this.pdksPersonel = pdksPersonel;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(insertable = false, updatable = false, name = "ID", referencedColumnName = "ID", nullable = false)
	@Fetch(FetchMode.JOIN)
	public PersonelKGS getPersonelKGS() {
		return personelKGS;
	}

	public void setPersonelKGS(PersonelKGS personelKGS) {
		this.personelKGS = personelKGS;
	}

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	@JoinColumn(insertable = false, updatable = false, name = "KULLANICI", referencedColumnName = "ID", nullable = true)
	@Fetch(FetchMode.JOIN)
	public User getKullanici() {
		return kullanici;
	}

	public void setKullanici(User kullanici) {
		this.kullanici = kullanici;
	}

	@Column(name = COLUMN_NAME_PERSONEL)
	public Long getpdksPersonelId() {
		return pdksPersonelId;
	}

	public void setpdksPersonelId(Long pdksPersonelId) {
		this.pdksPersonelId = pdksPersonelId;
	}

	@Column(name = "KULLANICI")
	public Long getKullaniciId() {
		return kullaniciId;
	}

	public void setKullaniciId(Long kullaniciId) {
		this.kullaniciId = kullaniciId;
	}

	@Column(name = COLUMN_NAME_SICIL_NO)
	public String getKgsSicilNo() {
		return kgsSicilNo;
	}

	public void setKgsSicilNo(String kgsSicilNo) {
		this.kgsSicilNo = kgsSicilNo;
	}

	@Transient
	public Long getPersonelKGSId() {
		return id;
	}

	@Column(name = COLUMN_NAME_ACIKLAMA)
	public String getPdksPersonelAciklama() {
		return pdksPersonelAciklama;
	}

	public void setPdksPersonelAciklama(String value) {
		this.pdksPersonelAciklama = value;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Transient
	public String getAd() {
		return pdksPersonel != null ? pdksPersonel.getAd() : personelKGS.getAd();
	}

	@Transient
	public String getSoyad() {
		return pdksPersonel != null ? pdksPersonel.getSoyad() : personelKGS.getSoyad();
	}

	@Transient
	public Boolean getDurumu() {
		boolean st = pdksPersonel != null ? pdksPersonel.getDurum() : personelKGS.getDurum();
		return st;
	}

	@Transient
	public String getAdSoyad() {
		String adiSoyad = pdksPersonel != null && pdksPersonel.getId() != null ? pdksPersonel.getAdSoyad() : personelKGS.getAdSoyad();
		return adiSoyad;
	}

	@Transient
	public String getSicilNo() {
		String sicilNo = "";

		if (pdksPersonel != null && pdksPersonel.getId() != null && pdksPersonel.getPdksSicilNo() != null)
			sicilNo = pdksPersonel.getPdksSicilNo().trim();
		else if (personelKGS != null)
			sicilNo = personelKGS.getSicilNo().trim();
		return sicilNo;
	}

	@Transient
	public String getUserName() {
		String userName = null;
		try {
			userName = kullanici != null && kullanici.getId() != null ? kullanici.getUsername() : "";
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			userName = "";
		}

		return userName;
	}

	@Transient
	public String getCcAdres() {
		return ccAdres;
	}

	public void setCcAdres(String ccAdres) {
		this.ccAdres = ccAdres;
	}

	@Transient
	public String getBccAdres() {
		return bccAdres;
	}

	public void setBccAdres(String bccAdres) {
		this.bccAdres = bccAdres;
	}

	@Transient
	public Personel getYonetici1() {
		return yonetici1;
	}

	public void setYonetici1(Personel yonetici1) {
		this.yonetici1 = yonetici1;
	}

	@Transient
	public Personel getYonetici2() {
		return yonetici2;
	}

	public void setYonetici2(Personel yonetici2) {
		this.yonetici2 = yonetici2;
	}

	@Transient
	public String getHareketAdres() {
		return hareketAdres;
	}

	public void setHareketAdres(String hareketAdres) {
		this.hareketAdres = hareketAdres;
	}

}
