/**
 * 
 */
package org.pdks.entity;

public enum BordroDetayTipi {

	TANIMSIZ("0"), UCRETLI_IZIN("1"), UCRETSIZ_IZIN("2"), RAPORLU_IZIN("3"), YILLIK_IZIN("YI"), IZIN_GUN("IG"), SAAT_NORMAL("SN"), SAAT_HAFTA_TATIL("SH"), SAAT_RESMI_TATIL("SR"), SAAT_IZIN("SI");

	private final String value;

	BordroDetayTipi(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static BordroDetayTipi fromValue(String v) {
		BordroDetayTipi izinGrubu = null;
		for (BordroDetayTipi c : BordroDetayTipi.values()) {
			if (c.value.trim().equals(v.trim())) {
				izinGrubu = c;
				break;
			}
		}
		return izinGrubu;
	}

}
