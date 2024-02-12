/**
 * 
 */
package org.pdks.entity;

public enum PersonelDurumTipi {

	GEBE(1), SUT_IZNI(2);

	private final Integer value;

	PersonelDurumTipi(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static PersonelDurumTipi fromValue(Integer v) {
		for (PersonelDurumTipi c : PersonelDurumTipi.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException();
	}

}
