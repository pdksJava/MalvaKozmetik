package org.pdks.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.log4j.Logger;

@Entity
@Table(name = KatSayi.TABLE_NAME)
public class KatSayi implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2957604543022120666L;

	/**
	 * 
	 */

	static Logger logger = Logger.getLogger(KatSayi.class);

	public static final String TABLE_NAME = "KAT_SAYI";
	public static final String COLUMN_NAME_ID = "ID";
	public static final String COLUMN_NAME_DURUM = "DURUM";
	public static final String COLUMN_NAME_TIPI = "TIPI";
	public static final String COLUMN_NAME_BAS_TARIH = "BAS_TARIH";
	public static final String COLUMN_NAME_BIT_TARIH = "BIT_TARIH";
	public static final String COLUMN_NAME_DEGER = "DEGER";

	private Long id;
	private Date basTarih, bitTarih;
	private KatSayiTipi tipi;
	private BigDecimal deger;
	private Boolean durum;

	public KatSayi() {
		super();
	}

	@Id
	@GeneratedValue
	@Column(name = COLUMN_NAME_ID)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = COLUMN_NAME_TIPI)
	public KatSayiTipi getTipi() {
		return tipi;
	}

	public void setTipi(KatSayiTipi tipi) {
		this.tipi = tipi;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = COLUMN_NAME_BAS_TARIH)
	public Date getBasTarih() {
		return basTarih;
	}

	public void setBasTarih(Date basTarih) {
		this.basTarih = basTarih;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = COLUMN_NAME_BIT_TARIH)
	public Date getBitTarih() {
		return bitTarih;
	}

	public void setBitTarih(Date bitTarih) {
		this.bitTarih = bitTarih;
	}
	@Column(name = COLUMN_NAME_DEGER)
	public BigDecimal getDeger() {
		return deger;
	}

	public void setDeger(BigDecimal deger) {
		this.deger = deger;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	
}
