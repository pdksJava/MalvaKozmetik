package com.pdks.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for izinHakedisDetay complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="izinHakedisDetay">
 *   &lt;complexContent>
 *     &lt;extension base="{http://webService.pdks.com/}baseERPObject">
 *       &lt;sequence>
 *         &lt;element name="hakEdisTarihi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="izinSuresi" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="kidemYil" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="kullanilanIzinler" type="{http://webService.pdks.com/}izinERP" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "izinHakedisDetay", propOrder = { "hakEdisTarihi", "izinSuresi", "kidemYil", "kullanilanIzinler" })
public class IzinHakedisDetay extends BaseERPObject {

	protected String hakEdisTarihi;
	protected double izinSuresi;
	protected int kidemYil;
	@XmlElement(nillable = true)
	protected List<IzinERP> kullanilanIzinler;

	/**
	 * Gets the value of the hakEdisTarihi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getHakEdisTarihi() {
		return hakEdisTarihi;
	}

	/**
	 * Sets the value of the hakEdisTarihi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setHakEdisTarihi(String value) {
		this.hakEdisTarihi = value;
	}

	/**
	 * Gets the value of the izinSuresi property.
	 * 
	 */
	public double getIzinSuresi() {
		return izinSuresi;
	}

	/**
	 * Sets the value of the izinSuresi property.
	 * 
	 */
	public void setIzinSuresi(double value) {
		this.izinSuresi = value;
	}

	/**
	 * Gets the value of the kidemYil property.
	 * 
	 */
	public int getKidemYil() {
		return kidemYil;
	}

	/**
	 * Sets the value of the kidemYil property.
	 * 
	 */
	public void setKidemYil(int value) {
		this.kidemYil = value;
	}

	/**
	 * Gets the value of the kullanilanIzinler property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the kullanilanIzinler property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getKullanilanIzinler().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link IzinERP }
	 * 
	 * 
	 */
	public List<IzinERP> getKullanilanIzinler() {
		if (kullanilanIzinler == null) {
			kullanilanIzinler = new ArrayList<IzinERP>();
		}
		return this.kullanilanIzinler;
	}

}
