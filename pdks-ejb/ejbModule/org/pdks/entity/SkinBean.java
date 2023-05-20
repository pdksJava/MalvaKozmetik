package org.pdks.entity;

import java.io.Serializable;

import org.jboss.seam.annotations.Name;

@Name("skinBean")
public class SkinBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4319517878865449168L;

	private static String skinAdi = "blueSky";
	private static String skinKodu = null;

	public String getSkin() {
		return skinAdi;
	}

	public static String getSkinAdi() {
		return skinAdi;
	}

	public static void setSkinAdi(String skinAdi) {
		SkinBean.skinAdi = skinAdi;
	}

	public static String getSkinKodu() {
		return skinKodu;
	}

	public static void setSkinKodu(String skinKodu) {
		SkinBean.skinKodu = skinKodu;
	}
}
