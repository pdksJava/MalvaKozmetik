/**
 * 
 */
package org.pdks.entity;

public enum BordroIzinGrubu {

	TANIMSIZ("0"), UCRETLI_IZIN("1"), UCRETSIZ_IZIN("2"), RAPORLU_IZIN("3");

	private final String value;

	BordroIzinGrubu(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static BordroIzinGrubu fromValue(String v) {
		BordroIzinGrubu izinGrubu = null;
		for (BordroIzinGrubu c : BordroIzinGrubu.values()) {
			if (c.value.trim().equals(v.trim())) {
				izinGrubu = c;
				break;
			}
		}
		return izinGrubu;
	}

}
