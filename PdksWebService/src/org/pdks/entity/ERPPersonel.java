package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;
import org.pdks.genel.model.PdksUtil;

@Entity(name = ERPPersonel.TABLE_NAME)
@Immutable
public class ERPPersonel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -246259719247154118L;

	/**
	 * 
	 */

	public static final String TABLE_NAME = "PERSONEL_ERP";

	public static final String COLUMN_NAME_SICIL_NO = "PERSONEL_NO";

	private String sicilNo;
	private String ad;
	private String soyad;

	private Date islemZamani = new Date();

	private Boolean durum = Boolean.TRUE;

	@Id
	@Column(name = COLUMN_NAME_SICIL_NO)
	public String getSicilNo() {
		return sicilNo;
	}

	public void setSicilNo(String sicilNo) {
		this.sicilNo = sicilNo;
	}

	@Column(name = "AD")
	public String getAd() {
		return ad;
	}

	public void setAd(String ad) {
		this.ad = ad;
	}

	@Column(name = "SOYAD")
	public String getSoyad() {
		return soyad;
	}

	public void setSoyad(String soyad) {
		this.soyad = soyad;
	}

	@Column(name = "DURUM")
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ISLEM_ZAMANI")
	public Date getIslemZamani() {
		return islemZamani;
	}

	public void setIslemZamani(Date islemZamani) {
		this.islemZamani = islemZamani;
	}

	@Transient
	public String getAdSoyad() {
		String adSoyad = PdksUtil.getAdSoyad(ad, soyad);
		return adSoyad;
	}

}
