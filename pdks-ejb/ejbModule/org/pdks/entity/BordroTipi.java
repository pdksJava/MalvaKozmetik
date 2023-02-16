/**
 * 
 */
package org.pdks.entity;

public enum BordroTipi {

	UCRETLI_IZIN("1"), UCRETSIZ_IZIN("2"), RAPORLU_IZIN("3"), UCRETI_ODENEN_MESAI("FM2"), RESMI_TATIL_MESAI("FM1"), HAFTA_TATIL_MESAI("FM3"), AKSAM_SAAT_MESAI("FM4"), AKSAM_GUN_MESAI("FM5");

	private final String value;

	BordroTipi(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static BordroTipi fromValue(String v) {
		for (BordroTipi c : BordroTipi.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException();
	}

}
