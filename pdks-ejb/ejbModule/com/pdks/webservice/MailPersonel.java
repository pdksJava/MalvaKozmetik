package com.pdks.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for mailPersonel complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mailPersonel">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="adiSoyadi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ePosta" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mailPersonel", propOrder = { "adiSoyadi", "ePosta" })
public class MailPersonel {

	protected String adiSoyadi;
	protected String ePosta;

	/**
	 * Gets the value of the adiSoyadi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getAdiSoyadi() {
		return adiSoyadi;
	}

	/**
	 * Sets the value of the adiSoyadi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setAdiSoyadi(String value) {
		this.adiSoyadi = value;
	}

	/**
	 * Gets the value of the ePosta property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getEPosta() {
		return ePosta;
	}

	/**
	 * Sets the value of the ePosta property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setEPosta(String value) {
		this.ePosta = value;
	}

}
