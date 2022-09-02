package com.pdks.mail.model;

import java.io.Serializable;

public class MailStatu implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1255128887462270975L;

	private String hataMesai;

	private boolean durum;

	public String getHataMesai() {
		return hataMesai;
	}

	public void setHataMesai(String hataMesai) {
		this.hataMesai = hataMesai;
	}

	public boolean isDurum() {
		return durum;
	}

	public void setDurum(boolean durum) {
		this.durum = durum;
	}

}
