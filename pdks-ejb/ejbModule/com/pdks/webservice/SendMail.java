package com.pdks.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for sendMail complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sendMail">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mail" type="{http://webService.pdks.com/}mailObject" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sendMail", propOrder = { "mail" })
public class SendMail {

	protected MailObject mail;

	/**
	 * Gets the value of the mail property.
	 * 
	 * @return possible object is {@link MailObject }
	 * 
	 */
	public MailObject getMail() {
		return mail;
	}

	/**
	 * Sets the value of the mail property.
	 * 
	 * @param value
	 *            allowed object is {@link MailObject }
	 * 
	 */
	public void setMail(MailObject value) {
		this.mail = value;
	}

}
