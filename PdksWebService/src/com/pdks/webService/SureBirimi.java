/**
 * 
 */
package com.pdks.webService;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "SureBirimi")
@XmlEnum
public enum SureBirimi {
	@XmlEnumValue(value = "1")	GUN("1"),
	@XmlEnumValue(value = "2") 	SAAT("2");


	private final String value;

	SureBirimi(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static SureBirimi fromValue(String v) {
		for (SureBirimi c : SureBirimi.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException( v);
	}

}
