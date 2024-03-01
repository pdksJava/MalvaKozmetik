package com.pdks.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for getMesaiPDKS complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getMesaiPDKS">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sirketKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="yil" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ay" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="donemKapat" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getMesaiPDKS", propOrder = { "sirketKodu", "yil", "ay", "donemKapat" })
public class GetMesaiPDKS {

	protected String sirketKodu;
	protected Integer yil;
	protected Integer ay;
	protected Boolean donemKapat;

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
	 * Gets the value of the yil property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getYil() {
		return yil;
	}

	/**
	 * Sets the value of the yil property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setYil(Integer value) {
		this.yil = value;
	}

	/**
	 * Gets the value of the ay property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getAy() {
		return ay;
	}

	/**
	 * Sets the value of the ay property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setAy(Integer value) {
		this.ay = value;
	}

	/**
	 * Gets the value of the donemKapat property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean getDonemKapat() {
		return donemKapat;
	}

	/**
	 * Sets the value of the donemKapat property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setDonemKapat(Boolean value) {
		this.donemKapat = value;
	}

}
