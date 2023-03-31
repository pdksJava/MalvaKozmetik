package com.pdks.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

import com.pdks.genel.model.PdksUtil;

@Entity(name = PersonelKGS.TABLE_NAME)
@Immutable
public class PersonelKGS implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -258104220283554275L;
	public static final String TABLE_NAME = "PERSONEL_KGS";
	public static final String COLUMN_NAME_ID = "ID";
	public static final String COLUMN_NAME_SICIL_NO = "PERSONEL_NO";
	public static final String COLUMN_NAME_KIMLIK_NO = "TC_KIMLIK_NO";
	private Long id;
	private String ad, soyad, sicilNo, kartNo, kimlikNo;
	private Boolean durum = Boolean.FALSE;

	@Id
	@GeneratedValue
	@Column(name = COLUMN_NAME_ID)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "AD")
	public String getAd() {
		return ad;
	}

	public void setAd(String value) {
		this.ad = PdksUtil.getCutFirstSpaces(value);
	}

	@Column(name = "SOYAD")
	public String getSoyad() {
		return soyad;
	}

	public void setSoyad(String value) {
		this.soyad = PdksUtil.getCutFirstSpaces(value);
	}

	@Column(name = COLUMN_NAME_SICIL_NO)
	public String getSicilNo() {
		return sicilNo;
	}

	public void setSicilNo(String sicilNo) {
		this.sicilNo = sicilNo;
	}

	@Column(name = "DURUM")
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

	@Transient
	public String getAdSoyad() {
		String adSoyad = PdksUtil.getAdSoyad(ad, soyad);
		return adSoyad;
	}

}
