package com.pdks.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for sendMailResponse complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sendMailResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mailStatu" type="{http://webService.pdks.com/}mailStatu" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sendMailResponse", propOrder = { "mailStatu" })
public class SendMailResponse {

	protected MailStatu mailStatu;

	/**
	 * Gets the value of the mailStatu property.
	 * 
	 * @return possible object is {@link MailStatu }
	 * 
	 */
	public MailStatu getMailStatu() {
		return mailStatu;
	}

	/**
	 * Sets the value of the mailStatu property.
	 * 
	 * @param value
	 *            allowed object is {@link MailStatu }
	 * 
	 */
	public void setMailStatu(MailStatu value) {
		this.mailStatu = value;
	}

}
