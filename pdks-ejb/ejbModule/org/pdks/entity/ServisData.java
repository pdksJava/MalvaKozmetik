package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name = ServisData.TABLE_NAME)
public class ServisData extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4548436599108915019L;
	/**
	 * 
	 */

	public static final String TABLE_NAME = "SERVICE_DATA";
	public static final String COLUMN_NAME_FONKSIYON_ADI = "FONKSIYON_ADI";
	public static final String COLUMN_NAME_TARIH = "TARIH";
	public static final String COLUMN_NAME_ICERIK_IN = "ICERIK_IN";
	public static final String COLUMN_NAME_ICERIK_OUT = "ICERIK_OUT";

	private String adi;
	private String icerikIn, icerikOut;
	private Date tarih;

	@Column(name = COLUMN_NAME_FONKSIYON_ADI)
	public String getAdi() {
		return adi;
	}

	public void setAdi(String adi) {
		this.adi = adi;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_TARIH)
	public Date getTarih() {
		return tarih;
	}

	public void setTarih(Date tarih) {
		this.tarih = tarih;
	}

	@Column(name = COLUMN_NAME_ICERIK_IN)
	public String getIcerikIn() {
		return icerikIn;
	}

	public void setIcerikIn(String icerikIn) {
		this.icerikIn = icerikIn;
	}

	@Column(name = COLUMN_NAME_ICERIK_OUT)
	public String getIcerikOut() {
		return icerikOut;
	}

	public void setIcerikOut(String icerikOut) {
		this.icerikOut = icerikOut;
	}

}
