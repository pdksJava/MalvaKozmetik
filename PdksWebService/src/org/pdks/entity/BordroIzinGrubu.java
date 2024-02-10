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
		for (BordroIzinGrubu c : BordroIzinGrubu.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException();
	}

}
