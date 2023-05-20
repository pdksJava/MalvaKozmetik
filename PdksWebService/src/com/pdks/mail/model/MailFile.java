package com.pdks.mail.model;

import java.io.Serializable;

public class MailFile implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6722612662242272506L;

	private String fileName, displayName;

	private byte[] icerik;

	private Object file;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public byte[] getIcerik() {
		return icerik;
	}

	public void setIcerik(byte[] icerik) {
		this.icerik = icerik;
	}

	public Object getFile() {
		return file;
	}

	public void setFile(Object file) {
		this.file = file;
	}

	 

}
