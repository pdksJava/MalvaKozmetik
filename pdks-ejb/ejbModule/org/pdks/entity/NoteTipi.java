/**
 * 
 */
package org.pdks.entity;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum NoteTipi {
	@XmlEnumValue(value = "anaSayfa")
	ANA_SAYFA("anaSayfa"), @XmlEnumValue(value = "mailCevaplamama")
	MAIL_CEVAPLAMAMA("mailCevaplamama");

	private final String value;

	NoteTipi(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static NoteTipi fromValue(String v) {
		for (NoteTipi c : NoteTipi.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
