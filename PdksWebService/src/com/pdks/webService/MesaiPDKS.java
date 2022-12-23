package com.pdks.webService;

import java.io.Serializable;

import javax.persistence.Transient;

public class MesaiPDKS implements Serializable,Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4175844504720534001L;

	private int yil;

	private int ay;

	private String sirketKodu;

	private String personelNo;

	private String tesisKodu;

	private String masrafYeriKodu;

	private String mesaiKodu;

	private Double toplamSure;

	public int getYil() {
		return yil;
	}

	public void setYil(int yil) {
		this.yil = yil;
	}

	public int getAy() {
		return ay;
	}

	public void setAy(int ay) {
		this.ay = ay;
	}

	public String getSirketKodu() {
		return sirketKodu;
	}

	public void setSirketKodu(String sirketKodu) {
		this.sirketKodu = sirketKodu;
	}

	public String getPersonelNo() {
		return personelNo;
	}

	public void setPersonelNo(String personelNo) {
		this.personelNo = personelNo;
	}

	public String getTesisKodu() {
		return tesisKodu;
	}

	public void setTesisKodu(String tesisKodu) {
		this.tesisKodu = tesisKodu;
	}

	public String getMasrafYeriKodu() {
		return masrafYeriKodu;
	}

	public void setMasrafYeriKodu(String masrafYeriKodu) {
		this.masrafYeriKodu = masrafYeriKodu;
	}

	public String getMesaiKodu() {
		return mesaiKodu;
	}

	public void setMesaiKodu(String mesaiKodu) {
		this.mesaiKodu = mesaiKodu;
	}

	public Double getToplamSure() {
		return toplamSure;
	}

	public void setToplamSure(Double toplamSure) {
		this.toplamSure = toplamSure;
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
