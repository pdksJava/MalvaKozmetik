package org.pdks.entity;

import java.io.Serializable;

public class FileUpload implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1903390624935424675L;
	private Integer length;
	private String name, icerikTipi;

	private byte[] data;

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcerikTipi() {
		return icerikTipi;
	}

	public void setIcerikTipi(String icerikTipi) {
		this.icerikTipi = icerikTipi;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public static boolean isInline(String tip) {
		boolean durum = tip != null && (tip.startsWith("image") || tip.equals("application/pdf"));
		return durum;
	}

	public static String getDisposition(String tip) {
		String disposition = "attachment";
		if (isInline(tip))
			disposition = "inline";
		return disposition;
	}

	public String getDisposition() {
		return getDisposition(icerikTipi);
	}

	public boolean isInline() {
		return isInline(icerikTipi);
	}
}
