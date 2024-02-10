package org.pdks.mail.model;

import java.io.Serializable;

public class MailPersonel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5984088216426452660L;

	private String adiSoyadi;

	private String ePosta;

	public String getAdiSoyadi() {
		return adiSoyadi;
	}

	public void setAdiSoyadi(String adiSoyadi) {
		this.adiSoyadi = adiSoyadi;
	}

	public String getePosta() {
		return ePosta;
	}

	public void setePosta(String ePosta) {
		this.ePosta = ePosta;
	}

}
