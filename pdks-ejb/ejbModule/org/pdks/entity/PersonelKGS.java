package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;
import org.pdks.security.entity.User;

@Entity(name = PersonelKGS.TABLE_NAME)
@Immutable
public class PersonelKGS extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -258104220283554275L;
	// public static final String TABLE_NAME = "PERSONEL_KGS";
	public static final String TABLE_NAME = "PERSONEL_SIRKET_KGS";

	public static final String COLUMN_NAME_SICIL_NO = "PERSONEL_NO";
	public static final String COLUMN_NAME_KIMLIK_NO = "TC_KIMLIK_NO";
	public static final String COLUMN_NAME_PERSONEL_ID = "PERSONEL_ID";
	public static final String COLUMN_NAME_KULLANICI_ID = "KULLANICI_ID";
	public static final String COLUMN_NAME_AD = "AD";
	public static final String COLUMN_NAME_SOYAD = "SOYAD";
	public static final String COLUMN_NAME_DURUM = "DURUM";
	public static final String COLUMN_NAME_ACIKLAMA = "AD_SOYAD";
	public static final String COLUMN_NAME_KGS_ID = "KGS_ID";
	public static final String COLUMN_NAME_KGS_SIRKET = "KGS_SIRKET_ID";
	public static final String COLUMN_NAME_ISE_BASLAMA_TARIHI = "ISE_BASLAMA_TARIHI";
	public static final String COLUMN_NAME_DOGUM_TARIHI = "DOGUM_TARIHI";

	private Long kgsId;
	private KapiSirket kapiSirket;
	private String ad, soyad, sicilNo, kartNo, kimlikNo, adSoyad;
	private Boolean durum = Boolean.FALSE;
	private Personel pdksPersonel;
	private Date dogumTarihi, iseBaslamaTarihi;
	private User kullanici;

	@Column(name = COLUMN_NAME_KGS_ID)
	public Long getKgsId() {
		return kgsId;
	}

	public void setKgsId(Long kgsId) {
		this.kgsId = kgsId;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = COLUMN_NAME_KGS_SIRKET, nullable = true)
	@Fetch(FetchMode.JOIN)
	public KapiSirket getKapiSirket() {
		return kapiSirket;
	}

	public void setKapiSirket(KapiSirket kapiSirket) {
		this.kapiSirket = kapiSirket;
	}

	@Column(name = COLUMN_NAME_AD)
	public String getAd() {
		return ad;
	}

	public void setAd(String ad) {
		this.ad = ad;
	}

	@Column(name = COLUMN_NAME_SOYAD)
	public String getSoyad() {
		return soyad;
	}

	public void setSoyad(String soyad) {
		this.soyad = soyad;
	}

	@Column(name = COLUMN_NAME_SICIL_NO)
	public String getSicilNo() {
		return sicilNo;
	}

	public void setSicilNo(String sicilNo) {
		this.sicilNo = sicilNo;
	}

	@Temporal(value = TemporalType.DATE)
	@Column(name = COLUMN_NAME_DOGUM_TARIHI)
	public Date getDogumTarihi() {
		return dogumTarihi;
	}

	public void setDogumTarihi(Date dogumTarihi) {
		this.dogumTarihi = dogumTarihi;
	}

	@Temporal(value = TemporalType.DATE)
	@Column(name = COLUMN_NAME_ISE_BASLAMA_TARIHI)
	public Date getIseBaslamaTarihi() {
		return iseBaslamaTarihi;
	}

	public void setIseBaslamaTarihi(Date iseBaslamaTarihi) {
		this.iseBaslamaTarihi = iseBaslamaTarihi;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Column(name = "KART_NO")
	public String getKartNo() {
		return kartNo;
	}

	public void setKartNo(String kartNo) {
		this.kartNo = kartNo;
	}

	@Column(name = COLUMN_NAME_KIMLIK_NO)
	public String getKimlikNo() {
		return kimlikNo;
	}

	public void setKimlikNo(String kimlikNo) {
		this.kimlikNo = kimlikNo;
	}

	@Column(name = COLUMN_NAME_ACIKLAMA)
	public String getAdSoyad() {
		return adSoyad;
	}

	public void setAdSoyad(String adSoyad) {
		this.adSoyad = adSoyad;
	}

	@OneToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL_ID, nullable = true)
	@Fetch(FetchMode.JOIN)
	public Personel getPdksPersonel() {
		return pdksPersonel;
	}

	public void setPdksPersonel(Personel pdksPersonel) {
		this.pdksPersonel = pdksPersonel;
	}

	@OneToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_KULLANICI_ID, nullable = true)
	@Fetch(FetchMode.JOIN)
	public User getKullanici() {
		return kullanici;
	}

	public void setKullanici(User kullanici) {
		this.kullanici = kullanici;
	}

	@Transient
	public PersonelView getPersonelView() {
		PersonelView personelView = new PersonelView();
		personelView.setId(this.getId());
		personelView.setAdi(this.getAd());
		personelView.setSoyadi(this.getSoyad());
		personelView.setKgsSicilNo(this.getSicilNo());
		personelView.setPersonelKGS(this);
		personelView.setDurum(this.getDurum());
		if (pdksPersonel != null) {
			personelView.setPdksPersonel(pdksPersonel);
			personelView.setPdksPersonelAciklama(pdksPersonel.getAdSoyad());
			personelView.setPdksPersonelId(pdksPersonel.getId());
			personelView.setCcAdres(pdksPersonel.getEmailCC());
			personelView.setBccAdres(pdksPersonel.getEmailBCC());
			personelView.setHareketAdres(pdksPersonel.getHareketMail());
		}
		if (kullanici != null) {
			personelView.setKullanici(kullanici);
			personelView.setKullaniciId(kullanici.getId());
		}
		return personelView;
	}

}
