package com.pdks.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for mesaiPDKS complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mesaiPDKS">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ay" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="masrafYeriKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mesaiKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="personelNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sirketKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tesisKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="toplamSure" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="yil" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mesaiPDKS", propOrder = { "ay", "masrafYeriKodu", "mesaiKodu", "personelNo", "sirketKodu", "tesisKodu", "toplamSure", "yil" })
public class MesaiPDKS {

	protected int ay;
	protected String masrafYeriKodu;
	protected String mesaiKodu;
	protected String personelNo;
	protected String sirketKodu;
	protected String tesisKodu;
	protected Double toplamSure;
	protected int yil;

	/**
	 * Gets the value of the ay property.
	 * 
	 */
	public int getAy() {
		return ay;
	}

	/**
	 * Sets the value of the ay property.
	 * 
	 */
	public void setAy(int value) {
		this.ay = value;
	}

	/**
	 * Gets the value of the masrafYeriKodu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getMasrafYeriKodu() {
		return masrafYeriKodu;
	}

	/**
	 * Sets the value of the masrafYeriKodu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setMasrafYeriKodu(String value) {
		this.masrafYeriKodu = value;
	}

	/**
	 * Gets the value of the mesaiKodu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getMesaiKodu() {
		return mesaiKodu;
	}

	/**
	 * Sets the value of the mesaiKodu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setMesaiKodu(String value) {
		this.mesaiKodu = value;
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
	 * Gets the value of the tesisKodu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTesisKodu() {
		return tesisKodu;
	}

	/**
	 * Sets the value of the tesisKodu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTesisKodu(String value) {
		this.tesisKodu = value;
	}

	/**
	 * Gets the value of the toplamSure property.
	 * 
	 * @return possible object is {@link Double }
	 * 
	 */
	public Double getToplamSure() {
		return toplamSure;
	}

	/**
	 * Sets the value of the toplamSure property.
	 * 
	 * @param value
	 *            allowed object is {@link Double }
	 * 
	 */
	public void setToplamSure(Double value) {
		this.toplamSure = value;
	}

	/**
	 * Gets the value of the yil property.
	 * 
	 */
	public int getYil() {
		return yil;
	}

	/**
	 * Sets the value of the yil property.
	 * 
	 */
	public void setYil(int value) {
		this.yil = value;
	}

}
