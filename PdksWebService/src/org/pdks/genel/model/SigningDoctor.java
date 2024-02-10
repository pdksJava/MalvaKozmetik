package org.pdks.genel.model;

import java.io.Serializable;

public class SigningDoctor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4109058282902570945L;

	private String adiSoyadi = "", diplomaNo = "", uzmanlik = "", bolum = "";

	public SigningDoctor() {
		super();
	}

	public SigningDoctor(String adiSoyadi, String diplomaNo, String unvan, String uzmanlik, String bolum) {
		super();

		this.adiSoyadi = (unvan != null ? unvan.trim() + " " : "") + adiSoyadi;
		this.diplomaNo = diplomaNo;
		this.uzmanlik = uzmanlik;
		this.bolum = bolum;
	}

	public String getDiplomaNo() {
		return diplomaNo;
	}

	public void setDiplomaNo(String diplomaNo) {
		this.diplomaNo = diplomaNo;
	}

	public String getUzmanlik() {
		return uzmanlik;
	}

	public void setUzmanlik(String uzmanlik) {
		this.uzmanlik = uzmanlik;
	}

	public String getBolum() {
		return bolum;
	}

	public void setBolum(String bolum) {
		this.bolum = bolum;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getAdiSoyadi() {
		return adiSoyadi;
	}

	public void setAdiSoyadi(String adiSoyadi) {
		this.adiSoyadi = adiSoyadi;
	}

}
