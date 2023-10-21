package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name = ServiceData.TABLE_NAME)
public class ServiceData extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6386015906961813899L;

	public static final String TABLE_NAME = "SERVICE_DATA";

	public static final String COLUMN_NAME_OLUSTURMA_TARIHI = "TARIH";
	public static final String COLUMN_NAME_FONKSIYON_ADI = "FONKSIYON_ADI";
	public static final String COLUMN_NAME_ICERIK_IN = "ICERIK_IN";
	public static final String COLUMN_NAME_ICERIK_OUT = "ICERIK_OUT";

	protected Date olusturmaTarihi = new Date();

	private String inputData, outputData, fonksiyonAdi;

	public ServiceData() {
		super();

	}

	public ServiceData(String fonksiyonAdi) {
		super();
		this.fonksiyonAdi = fonksiyonAdi;
		this.olusturmaTarihi = new Date();
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_OLUSTURMA_TARIHI)
	public Date getOlusturmaTarihi() {
		return olusturmaTarihi;
	}

	public void setOlusturmaTarihi(Date olusturmaTarihi) {
		this.olusturmaTarihi = olusturmaTarihi;
	}

	@Column(name = COLUMN_NAME_ICERIK_IN)
	public String getInputData() {
		return inputData;
	}

	public void setInputData(String inputData) {
		this.inputData = inputData;
	}

	@Column(name = COLUMN_NAME_ICERIK_OUT)
	public String getOutputData() {
		return outputData;
	}

	public void setOutputData(String outputData) {
		this.outputData = outputData;
	}

	@Column(name = COLUMN_NAME_FONKSIYON_ADI)
	public String getFonksiyonAdi() {
		return fonksiyonAdi;
	}

	public void setFonksiyonAdi(String fonksiyonAdi) {
		this.fonksiyonAdi = fonksiyonAdi;
	}

}
