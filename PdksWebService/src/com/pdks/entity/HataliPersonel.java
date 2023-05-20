package com.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Immutable;

@Entity(name = HataliPersonel.VIEW_NAME)
@Immutable
public class HataliPersonel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -375105054967115897L;
	static Logger logger = Logger.getLogger(HataliPersonel.class);
	public static final String VIEW_NAME = "HATALI_PERSONEL_VIEW";
	public static final String COLUMN_NAME_ID = "ID";
	public static final String COLUMN_NAME_AD = "ADI";
	public static final String COLUMN_NAME_SOYAD = "SOYADI";
	public static final String COLUMN_NAME_PERSONEL_NO = "PERSONEL_NO";
	public static final String COLUMN_NAME_DURUM = "GECERLI";
	public static final String COLUMN_NAME_KART_ID = "KART_ID";
	public static final String COLUMN_NAME_SIRKET_ADI = "SIRKET_ADI";
	public static final String COLUMN_NAME_ISE_GIRIS_TARIHI = "ISE_GIRIS_TARIHI";
	public static final String COLUMN_NAME_TIP = "TIP";

	private Long id;

	private String personelNo, adi, soyadi, sirketAdi, kartNo, tip;

	private Date iseGirisTarhi;

	private Boolean durum;

	@Id
	@Column(name = COLUMN_NAME_ID)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = COLUMN_NAME_PERSONEL_NO)
	public String getPersonelNo() {
		return personelNo;
	}

	public void setPersonelNo(String personelNo) {
		this.personelNo = personelNo;
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

	@Column(name = COLUMN_NAME_SIRKET_ADI)
	public String getSirketAdi() {
		return sirketAdi;
	}

	public void setSirketAdi(String sirketAdi) {
		this.sirketAdi = sirketAdi;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = COLUMN_NAME_ISE_GIRIS_TARIHI)
	public Date getIseGirisTarhi() {
		return iseGirisTarhi;
	}

	public void setIseGirisTarhi(Date iseGirisTarhi) {
		this.iseGirisTarhi = iseGirisTarhi;
	}

	@Column(name = COLUMN_NAME_KART_ID)
	public String getKartNo() {
		return kartNo;
	}

	public void setKartNo(String kartNo) {
		this.kartNo = kartNo;
	}

	@Column(name = COLUMN_NAME_TIP)
	public String getTip() {
		return tip;
	}

	public void setTip(String tip) {
		this.tip = tip;
	}

}
