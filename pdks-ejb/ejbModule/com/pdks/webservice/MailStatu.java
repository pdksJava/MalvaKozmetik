package com.pdks.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for mailStatu complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mailStatu">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="durum" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="hataMesai" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mailStatu", propOrder = { "durum", "hataMesai" })
public class MailStatu {

	protected boolean durum;
	protected String hataMesai;

	/**
	 * Gets the value of the durum property.
	 * 
	 */
	public boolean isDurum() {
		return durum;
	}

	/**
	 * Sets the value of the durum property.
	 * 
	 */
	public void setDurum(boolean value) {
		this.durum = value;
	}

	/**
	 * Gets the value of the hataMesai property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getHataMesai() {
		return hataMesai;
	}

	/**
	 * Sets the value of the hataMesai property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setHataMesai(String value) {
		this.hataMesai = value;
	}

}
