package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.pdks.session.PdksUtil;

@Entity(name = KapiSirket.TABLE_NAME)
public class KapiSirket extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2191674427143871092L;
	/**
	 * 
	 */

	public static final String TABLE_NAME = "KAPI_SIRKET";
	public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";
	public static final String COLUMN_NAME_BAS_TARIH = "BASLANGIC_TARIHI";
	public static final String COLUMN_NAME_BIT_TARIH = "BITIS_TARIHI";
	public static final String COLUMN_NAME_DURUM = "DURUM";

	private Date basTarih, bitTarih;
	private String aciklama;

	private Boolean durum = Boolean.TRUE;

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

	@Column(name = COLUMN_NAME_ACIKLAMA)
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Transient
	public boolean isAciklamaVar() {
		return PdksUtil.hasStringValue(aciklama);
	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}

}
