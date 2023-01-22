/**
 * 
 */
package org.pdks.entity;

public enum KatSayiTipi {

	HAREKET_BEKLEME_SURESI(1), SUA_GUNLUK_SAAT_SURESI(2), YUVARLAMA_TIPI(3), OFF_FAZLA_MESAI_TIPI(4), HT_FAZLA_MESAI_TIPI(5), ERKEN_GIRIS_TIPI(6), GEC_CIKIS_TIPI(7), FMT_DURUM(8);

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
