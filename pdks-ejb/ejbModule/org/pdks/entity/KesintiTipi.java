package org.pdks.entity;

public enum KesintiTipi {

	KESINTI_YOK(0), GUN(1), SAAT(2);

	private final Integer value;

	KesintiTipi(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static KesintiTipi fromValue(Integer v) {
		KesintiTipi kesintiTipi = null;
		for (KesintiTipi c : KesintiTipi.values()) {
			if (c.value.equals(v)) {
				kesintiTipi = c;
			}
		}
		return kesintiTipi;

	}

}
