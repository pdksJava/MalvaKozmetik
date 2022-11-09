/**
 * 
 */
package org.pdks.entity;

public enum KatSayiTipi {

	HAREKET_BEKLEME_SURESI(1), SUA_GUNLUK_SAAT_SURESI(2),  YUVARLAMA_TIPI(3);

	private final Integer value;

	KatSayiTipi(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static KatSayiTipi fromValue(Integer v) {
		for (KatSayiTipi c : KatSayiTipi.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException();
	}

}
