package com.pdks.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for izinHakedis complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="izinHakedis">
 *   &lt;complexContent>
 *     &lt;extension base="{http://webService.pdks.com/}baseERPObject">
 *       &lt;sequence>
 *         &lt;element name="hakedisList" type="{http://webService.pdks.com/}izinHakedisDetay" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="kidemBaslangicTarihi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="personelNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "izinHakedis", propOrder = { "hakedisList", "kidemBaslangicTarihi", "personelNo" })
public class IzinHakedis extends BaseERPObject {

	@XmlElement(nillable = true)
	protected List<IzinHakedisDetay> hakedisList;
	protected String kidemBaslangicTarihi;
	protected String personelNo;

	/**
	 * Gets the value of the hakedisList property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the hakedisList property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getHakedisList().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link IzinHakedisDetay }
	 * 
	 * 
	 */
	public List<IzinHakedisDetay> getHakedisList() {
		if (hakedisList == null) {
			hakedisList = new ArrayList<IzinHakedisDetay>();
		}
		return this.hakedisList;
	}

	/**
	 * Gets the value of the kidemBaslangicTarihi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getKidemBaslangicTarihi() {
		return kidemBaslangicTarihi;
	}

	/**
	 * Sets the value of the kidemBaslangicTarihi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setKidemBaslangicTarihi(String value) {
		this.kidemBaslangicTarihi = value;
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

}
