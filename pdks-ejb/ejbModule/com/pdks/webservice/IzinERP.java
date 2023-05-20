package com.pdks.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for izinERP complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="izinERP">
 *   &lt;complexContent>
 *     &lt;extension base="{http://webService.pdks.com/}baseERPObject">
 *       &lt;sequence>
 *         &lt;element name="aciklama" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="basZaman" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bitZaman" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="durum" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="izinSuresi" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="izinTipi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="izinTipiAciklama" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="personelNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="referansNoERP" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sureBirimi" type="{http://webService.pdks.com/}SureBirimi" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "izinERP", propOrder = { "aciklama", "basZaman", "bitZaman", "durum", "izinSuresi", "izinTipi", "izinTipiAciklama", "personelNo", "referansNoERP", "sureBirimi" })
public class IzinERP extends BaseERPObject {

	protected String aciklama;
	protected String basZaman;
	protected String bitZaman;
	protected Boolean durum;
	protected Double izinSuresi;
	protected String izinTipi;
	protected String izinTipiAciklama;
	protected String personelNo;
	protected String referansNoERP;
	protected String sureBirimi;

	/**
	 * Gets the value of the aciklama property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getAciklama() {
		return aciklama;
	}

	/**
	 * Sets the value of the aciklama property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setAciklama(String value) {
		this.aciklama = value;
	}

	/**
	 * Gets the value of the basZaman property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getBasZaman() {
		return basZaman;
	}

	/**
	 * Sets the value of the basZaman property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setBasZaman(String value) {
		this.basZaman = value;
	}

	/**
	 * Gets the value of the bitZaman property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getBitZaman() {
		return bitZaman;
	}

	/**
	 * Sets the value of the bitZaman property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setBitZaman(String value) {
		this.bitZaman = value;
	}

	/**
	 * Gets the value of the durum property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean getDurum() {
		return durum;
	}

	/**
	 * Sets the value of the durum property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setDurum(Boolean value) {
		this.durum = value;
	}

	/**
	 * Gets the value of the izinSuresi property.
	 * 
	 * @return possible object is {@link Double }
	 * 
	 */
	public Double getIzinSuresi() {
		return izinSuresi;
	}

	/**
	 * Sets the value of the izinSuresi property.
	 * 
	 * @param value
	 *            allowed object is {@link Double }
	 * 
	 */
	public void setIzinSuresi(Double value) {
		this.izinSuresi = value;
	}

	/**
	 * Gets the value of the izinTipi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getIzinTipi() {
		return izinTipi;
	}

	/**
	 * Sets the value of the izinTipi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setIzinTipi(String value) {
		this.izinTipi = value;
	}

	/**
	 * Gets the value of the izinTipiAciklama property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getIzinTipiAciklama() {
		return izinTipiAciklama;
	}

	/**
	 * Sets the value of the izinTipiAciklama property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setIzinTipiAciklama(String value) {
		this.izinTipiAciklama = value;
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
	 * Gets the value of the referansNoERP property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getReferansNoERP() {
		return referansNoERP;
	}

	/**
	 * Sets the value of the referansNoERP property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setReferansNoERP(String value) {
		this.referansNoERP = value;
	}

	/**
	 * Gets the value of the sureBirimi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSureBirimi() {
		return sureBirimi;
	}

	/**
	 * Sets the value of the sureBirimi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSureBirimi(String value) {
		this.sureBirimi = value;
	}

}
