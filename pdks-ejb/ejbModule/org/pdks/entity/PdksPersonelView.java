package org.pdks.entity;

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
import org.pdks.security.entity.User;
import org.pdks.session.PdksUtil;

@Entity(name = PdksPersonelView.TABLE_NAME)
@Immutable
public class PdksPersonelView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5536485828562045534L;
	static Logger logger = Logger.getLogger(PdksPersonelView.class);
	/**
	 * 
	 */
	public static final String TABLE_NAME = "PERSONEL_VIEW";
	public static final String COLUMN_NAME_PERSONEL = "PERSONEL";
	public static final String COLUMN_NAME_KULLANICI = "KULLANICI";
	public static final String COLUMN_NAME_DURUM = "DURUM";

	private Long id;
	private Personel pdksPersonel;
	private User kullanici;
	private Long pdksPersonelId, kullaniciId;
	private String pdksSicilNo;
	private Boolean durum;

	private Personel yonetici1, yonetici2;

	@Id
	@Column(name = "ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	@JoinColumn(insertable = false, updatable = false, name = COLUMN_NAME_KULLANICI, referencedColumnName = "ID", nullable = true)
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

	@Column(name = COLUMN_NAME_KULLANICI)
	public Long getKullaniciId() {
		return kullaniciId;
	}

	public void setKullaniciId(Long kullaniciId) {
		this.kullaniciId = kullaniciId;
	}

	@Transient
	public Long getPersonelKGSId() {
		PersonelKGS personelKGS = getPersonelKGS();
		Long personelKGSId = personelKGS != null ? personelKGS.getId() : null;
		return personelKGSId;
	}

	@Column(name = Personel.COLUMN_NAME_PDKS_SICIL_NO)
	public String getPdksSicilNo() {
		return pdksSicilNo;
	}

	public void setPdksSicilNo(String pdksSicilNo) {
		this.pdksSicilNo = pdksSicilNo;
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
		return pdksPersonel != null ? pdksPersonel.getAd() : getPersonelKGS().getAd();
	}

	@Transient
	public String getSoyad() {
		return pdksPersonel != null ? pdksPersonel.getSoyad() : getPersonelKGS().getSoyad();
	}

	@Transient
	public Boolean getDurumu() {
		boolean st = pdksPersonel != null ? pdksPersonel.getDurum() : getPersonelKGS().getDurum();
		return st;
	}

	@Transient
	public String getAdSoyad() {
		String adiSoyad = pdksPersonel != null && pdksPersonel.getId() != null ? pdksPersonel.getAdSoyad() : getPersonelKGS().getAdSoyad();
		return adiSoyad;
	}

	@Transient
	public String getSicilNo() {
		String sicilNo = "";

		if (pdksPersonel != null && pdksPersonel.getId() != null && pdksPersonel.getPdksSicilNo() != null)
			sicilNo = pdksPersonel.getPdksSicilNo().trim();
		else if (getPersonelKGS() != null)
			sicilNo = getPersonelKGS().getSicilNo().trim();
		return sicilNo;
	}

	@Transient
	public boolean isGuncelle(User user) {
		boolean guncelle = PdksUtil.hasStringValue(getPersonelKGS().getSicilNo());
		if (!(user.isAdmin() || user.isIKAdmin()) && pdksPersonel != null)
			guncelle = pdksPersonel.getPdksSicilNo() != null && pdksPersonel.getPdksSicilNo().trim().equals(getPersonelKGS().getSicilNo().trim());
		return guncelle;
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
	public PersonelKGS getPersonelKGS() {
		return pdksPersonel != null ? pdksPersonel.getPersonelKGS() : new PersonelKGS();
	}

	@Transient
	public PersonelView getPersonelView() {
		PersonelView personelView = new PersonelView();
		personelView.setKullanici(kullanici);
		personelView.setPdksPersonel(pdksPersonel);
		if (pdksPersonel != null) {
			personelView.setDurum(pdksPersonel.getDurum());
			personelView.setPdksPersonelId(pdksPersonel.getId());
			if (pdksPersonel.getPersonelKGS() != null) {
				personelView.setPersonelKGS(pdksPersonel.getPersonelKGS());
				personelView.setKgsSicilNo(pdksPersonel.getPersonelKGS().getSicilNo());
				personelView.setId(pdksPersonel.getPersonelKGS().getId());
			}
		}
		if (kullanici != null) {
			personelView.setKullaniciId(kullanici.getId());
			personelView.setCcAdres(pdksPersonel.getEmailCC());
			personelView.setBccAdres(pdksPersonel.getEmailBCC());
		}
		return personelView;
	}

	@Transient
	public String getKgsSicilNo() {
		return getPersonelKGS().getSicilNo();
	}

}
