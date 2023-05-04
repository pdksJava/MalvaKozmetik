package org.pdks.entity;

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

@Entity(name = IzinTipiMailAdres.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { IzinTipiMailAdres.COLUMN_NAME_IZIN_TIPI, IzinTipiMailAdres.COLUMN_NAME_MAIL_ADRES }) })
public class IzinTipiMailAdres extends BasePDKSObject implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 56230242920057020L;
	public static final String TABLE_NAME = "IZIN_TIPI_MAIL_ADRES";
	public static final String COLUMN_NAME_MAIL_TIPI = "MAIL_TIPI";
	public static final String COLUMN_NAME_MAIL_ID = "ID";
	public static final String COLUMN_NAME_IZIN_TIPI = "IZIN_TIPI_ID";
	public static final String COLUMN_NAME_MAIL_ADRES = "MAIL_ADRES";
	public static final String TIPI_CC = "CC";
	public static final String TIPI_BCC = "BC";

	private IzinTipi izinTipi;

	private String tipi, adres;

	private Integer version = 0;

	private int sira = 0;

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_IZIN_TIPI, nullable = false)
	@Fetch(FetchMode.JOIN)
	public IzinTipi getIzinTipi() {
		return izinTipi;
	}

	public void setIzinTipi(IzinTipi izinTipi) {
		this.izinTipi = izinTipi;
	}

	@Column(name = COLUMN_NAME_MAIL_TIPI, length = 2)
	public String getTipi() {
		return tipi;
	}

	public void setTipi(String tipi) {
		this.tipi = tipi;
	}

	@Column(name = COLUMN_NAME_MAIL_ADRES, length = 128)
	public String getAdres() {
		return adres;
	}

	public void setAdres(String adres) {
		this.adres = adres;
	}

	@Transient
	public int getSira() {
		return sira;
	}

	public void setSira(int sira) {
		this.sira = sira;
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

}
