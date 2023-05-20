package com.pdks.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for getERPPersonelList complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getERPPersonelList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sirketKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="personelNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="basTarih" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bitTarih" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getERPPersonelList", propOrder = { "sirketKodu", "personelNo", "basTarih", "bitTarih" })
public class GetERPPersonelList {

	protected String sirketKodu;
	protected String personelNo;
	protected String basTarih;
	protected String bitTarih;

	/**
	 * Gets the value of the sirketKodu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSirketKodu() {
		return sirketKodu;
	}

	/**
	 * Sets the value of the sirketKodu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSirketKodu(String value) {
		this.sirketKodu = value;
	}

	/**
	 * Gets the value of the personelNo property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPersonelNo() {
		return personelNo;
	}

	/**
	 * Sets the value of the personelNo property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPersonelNo(String value) {
		this.personelNo = value;
	}

	/**
	 * Gets the value of the basTarih property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getBasTarih() {
		return basTarih;
	}

	/**
	 * Sets the value of the basTarih property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setBasTarih(String value) {
		this.basTarih = value;
	}

	/**
	 * Gets the value of the bitTarih property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getBitTarih() {
		return bitTarih;
	}

	/**
	 * Sets the value of the bitTarih property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setBitTarih(String value) {
		this.bitTarih = value;
	}

}
