package com.pdks.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = CalismaModeliAy.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "DONEM_ID", "CALISMA_MODELI_ID" }) })
public class CalismaModeliAy extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4015750209129001721L;
	public static final String TABLE_NAME = "CALISMA_MODELI_AY";

	public static final String COLUMN_NAME_DURUM = "DURUM";
	public static final String COLUMN_NAME_OLUSTURAN = "OLUSTURANUSER_ID";
	public static final String COLUMN_NAME_GUNCELLEYEN = "GUNCELLEYENUSER_ID";
	public static final String COLUMN_NAME_OLUSTURMA_TARIHI = "OLUSTURMATARIHI";
	public static final String COLUMN_NAME_GUNCELLEME_TARIHI = "GUNCELLEMETARIHI";

	private DenklestirmeAy denklestirmeAy;

	private CalismaModeli calismaModeli;

	private double sure = 0;

	public CalismaModeliAy() {
		super();
	}

	public CalismaModeliAy(DenklestirmeAy denklestirmeAy, CalismaModeli calismaModeli) {
		super();
		this.denklestirmeAy = denklestirmeAy;
		this.calismaModeli = calismaModeli;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "DONEM_ID")
	@Fetch(FetchMode.JOIN)
	public DenklestirmeAy getDenklestirmeAy() {
		return denklestirmeAy;
	}

	public void setDenklestirmeAy(DenklestirmeAy denklestirmeAy) {
		this.denklestirmeAy = denklestirmeAy;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "CALISMA_MODELI_ID")
	@Fetch(FetchMode.JOIN)
	public CalismaModeli getCalismaModeli() {
		return calismaModeli;
	}

	public void setCalismaModeli(CalismaModeli calismaModeli) {
		this.calismaModeli = calismaModeli;
	}

	@Column(name = "SURE")
	public double getSure() {
		return sure;
	}

	public void setSure(double sure) {
		this.sure = sure;
	}

	@Transient
	public static String getKey(DenklestirmeAy xDenklestirmeAy, CalismaModeli xCalismaModeli) {
		String key = (xDenklestirmeAy != null ? xDenklestirmeAy.getId() : 0) + "_" + (xCalismaModeli != null ? xCalismaModeli.getId() : 0);
		return key;
	}

	@Transient
	public String getKey() {
		String key = getKey(denklestirmeAy, calismaModeli);
		return key;
	}

}
