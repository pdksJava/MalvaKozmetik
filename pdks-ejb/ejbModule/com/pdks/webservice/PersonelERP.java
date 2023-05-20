package com.pdks.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for personelERP complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="personelERP">
 *   &lt;complexContent>
 *     &lt;extension base="{http://webService.pdks.com/}baseERPObject">
 *       &lt;sequence>
 *         &lt;element name="adi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bolumAdi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bolumKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bordroAltAlanAdi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bordroAltAlanKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cinsiyetKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cinsiyeti" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="departmanAdi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="departmanKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dogumTarihi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="gorevKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="gorevi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="iseGirisTarihi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="istenAyrilmaTarihi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="kidemTarihi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="masrafYeriAdi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="masrafYeriKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="personelNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="personelTipi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="personelTipiKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sanalPersonel" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="sirketAdi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sirketKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="soyadi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tesisAdi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tesisKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="yoneticiPerNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="grubaGirisTarihi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="yonetici2PerNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "personelERP", propOrder = { "adi", "bolumAdi", "bolumKodu", "bordroAltAlanAdi", "bordroAltAlanKodu", "cinsiyetKodu", "cinsiyeti", "departmanAdi", "departmanKodu", "dogumTarihi", "gorevKodu", "gorevi", "iseGirisTarihi", "istenAyrilmaTarihi", "kidemTarihi", "masrafYeriAdi",
		"masrafYeriKodu", "personelNo", "personelTipi", "personelTipiKodu", "sanalPersonel", "sirketAdi", "sirketKodu", "soyadi", "tesisAdi", "tesisKodu", "yoneticiPerNo", "grubaGirisTarihi", "yonetici2PerNo" })
public class PersonelERP extends BaseERPObject {

	protected String adi;
	protected String bolumAdi;
	protected String bolumKodu;
	protected String bordroAltAlanAdi;
	protected String bordroAltAlanKodu;
	protected String cinsiyetKodu;
	protected String cinsiyeti;
	protected String departmanAdi;
	protected String departmanKodu;
	protected String dogumTarihi;
	protected String gorevKodu;
	protected String gorevi;
	protected String iseGirisTarihi;
	protected String istenAyrilmaTarihi;
	protected String kidemTarihi;
	protected String masrafYeriAdi;
	protected String masrafYeriKodu;
	protected String personelNo;
	protected String personelTipi;
	protected String personelTipiKodu;
	protected Boolean sanalPersonel;
	protected String sirketAdi;
	protected String sirketKodu;
	protected String soyadi;
	protected String tesisAdi;
	protected String tesisKodu;
	protected String yoneticiPerNo;
	protected String grubaGirisTarihi;
	protected String yonetici2PerNo;

	/**
	 * Gets the value of the adi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getAdi() {
		return adi;
	}

	/**
	 * Sets the value of the adi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setAdi(String value) {
		this.adi = value;
	}

	/**
	 * Gets the value of the bolumAdi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getBolumAdi() {
		return bolumAdi;
	}

	/**
	 * Sets the value of the bolumAdi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setBolumAdi(String value) {
		this.bolumAdi = value;
	}

	/**
	 * Gets the value of the bolumKodu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getBolumKodu() {
		return bolumKodu;
	}

	/**
	 * Sets the value of the bolumKodu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setBolumKodu(String value) {
		this.bolumKodu = value;
	}

	/**
	 * Gets the value of the bordroAltAlanAdi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getBordroAltAlanAdi() {
		return bordroAltAlanAdi;
	}

	/**
	 * Sets the value of the bordroAltAlanAdi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setBordroAltAlanAdi(String value) {
		this.bordroAltAlanAdi = value;
	}

	/**
	 * Gets the value of the bordroAltAlanKodu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getBordroAltAlanKodu() {
		return bordroAltAlanKodu;
	}

	/**
	 * Sets the value of the bordroAltAlanKodu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setBordroAltAlanKodu(String value) {
		this.bordroAltAlanKodu = value;
	}

	/**
	 * Gets the value of the cinsiyetKodu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCinsiyetKodu() {
		return cinsiyetKodu;
	}

	/**
	 * Sets the value of the cinsiyetKodu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCinsiyetKodu(String value) {
		this.cinsiyetKodu = value;
	}

	/**
	 * Gets the value of the cinsiyeti property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCinsiyeti() {
		return cinsiyeti;
	}

	/**
	 * Sets the value of the cinsiyeti property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCinsiyeti(String value) {
		this.cinsiyeti = value;
	}

	/**
	 * Gets the value of the departmanAdi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDepartmanAdi() {
		return departmanAdi;
	}

	/**
	 * Sets the value of the departmanAdi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDepartmanAdi(String value) {
		this.departmanAdi = value;
	}

	/**
	 * Gets the value of the departmanKodu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDepartmanKodu() {
		return departmanKodu;
	}

	/**
	 * Sets the value of the departmanKodu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDepartmanKodu(String value) {
		this.departmanKodu = value;
	}

	/**
	 * Gets the value of the dogumTarihi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDogumTarihi() {
		return dogumTarihi;
	}

	/**
	 * Sets the value of the dogumTarihi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDogumTarihi(String value) {
		this.dogumTarihi = value;
	}

	/**
	 * Gets the value of the gorevKodu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getGorevKodu() {
		return gorevKodu;
	}

	/**
	 * Sets the value of the gorevKodu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setGorevKodu(String value) {
		this.gorevKodu = value;
	}

	/**
	 * Gets the value of the gorevi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getGorevi() {
		return gorevi;
	}

	/**
	 * Sets the value of the gorevi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setGorevi(String value) {
		this.gorevi = value;
	}

	/**
	 * Gets the value of the iseGirisTarihi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getIseGirisTarihi() {
		return iseGirisTarihi;
	}

	/**
	 * Sets the value of the iseGirisTarihi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setIseGirisTarihi(String value) {
		this.iseGirisTarihi = value;
	}

	/**
	 * Gets the value of the istenAyrilmaTarihi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getIstenAyrilmaTarihi() {
		return istenAyrilmaTarihi;
	}

	/**
	 * Sets the value of the istenAyrilmaTarihi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setIstenAyrilmaTarihi(String value) {
		this.istenAyrilmaTarihi = value;
	}

	/**
	 * Gets the value of the kidemTarihi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getKidemTarihi() {
		return kidemTarihi;
	}

	/**
	 * Sets the value of the kidemTarihi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setKidemTarihi(String value) {
		this.kidemTarihi = value;
	}

	/**
	 * Gets the value of the masrafYeriAdi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getMasrafYeriAdi() {
		return masrafYeriAdi;
	}

	/**
	 * Sets the value of the masrafYeriAdi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setMasrafYeriAdi(String value) {
		this.masrafYeriAdi = value;
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
	 * Gets the value of the personelTipi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPersonelTipi() {
		return personelTipi;
	}

	/**
	 * Sets the value of the personelTipi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPersonelTipi(String value) {
		this.personelTipi = value;
	}

	/**
	 * Gets the value of the personelTipiKodu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPersonelTipiKodu() {
		return personelTipiKodu;
	}

	/**
	 * Sets the value of the personelTipiKodu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPersonelTipiKodu(String value) {
		this.personelTipiKodu = value;
	}

	/**
	 * Gets the value of the sanalPersonel property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean getSanalPersonel() {
		return sanalPersonel;
	}

	/**
	 * Sets the value of the sanalPersonel property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setSanalPersonel(Boolean value) {
		this.sanalPersonel = value;
	}

	/**
	 * Gets the value of the sirketAdi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSirketAdi() {
		return sirketAdi;
	}

	/**
	 * Sets the value of the sirketAdi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSirketAdi(String value) {
		this.sirketAdi = value;
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
	 * Gets the value of the soyadi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSoyadi() {
		return soyadi;
	}

	/**
	 * Sets the value of the soyadi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSoyadi(String value) {
		this.soyadi = value;
	}

	/**
	 * Gets the value of the tesisAdi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTesisAdi() {
		return tesisAdi;
	}

	/**
	 * Sets the value of the tesisAdi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTesisAdi(String value) {
		this.tesisAdi = value;
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
	 * Gets the value of the yoneticiPerNo property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getYoneticiPerNo() {
		return yoneticiPerNo;
	}

	/**
	 * Sets the value of the yoneticiPerNo property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setYoneticiPerNo(String value) {
		this.yoneticiPerNo = value;
	}

	/**
	 * Gets the value of the grubaGirisTarihi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getGrubaGirisTarihi() {
		return grubaGirisTarihi;
	}

	/**
	 * Sets the value of the grubaGirisTarihi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setGrubaGirisTarihi(String value) {
		this.grubaGirisTarihi = value;
	}

	public String getYonetici2PerNo() {
		return yonetici2PerNo;
	}

	public void setYonetici2PerNo(String yonetici2PerNo) {
		this.yonetici2PerNo = yonetici2PerNo;
	}

}
