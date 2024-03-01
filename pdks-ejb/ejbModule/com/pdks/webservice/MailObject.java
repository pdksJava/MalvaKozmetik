package com.pdks.webservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for mailObject complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mailObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="attachmentFiles" type="{http://webService.pdks.com/}mailFile" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="bccList" type="{http://webService.pdks.com/}mailPersonel" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="body" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ccList" type="{http://webService.pdks.com/}mailPersonel" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="smtpPassword" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="smtpUser" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="subject" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="toList" type="{http://webService.pdks.com/}mailPersonel" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mailObject", propOrder = { "attachmentFiles", "bccList", "body", "ccList", "smtpPassword", "smtpUser", "subject", "toList" })
public class MailObject implements Serializable, Cloneable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5409112400274560540L;
	@XmlElement(nillable = true)
	protected List<MailFile> attachmentFiles;
	@XmlElement(nillable = true)
	protected List<MailPersonel> bccList;
	protected String body;
	@XmlElement(nillable = true)
	protected List<MailPersonel> ccList;
	protected String smtpPassword;
	protected String smtpUser;
	protected String subject;
	@XmlElement(nillable = true)
	protected List<MailPersonel> toList;

	/**
	 * Gets the value of the attachmentFiles property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the attachmentFiles property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getAttachmentFiles().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link MailFile }
	 * 
	 * 
	 */
	public List<MailFile> getAttachmentFiles() {
		if (attachmentFiles == null) {
			attachmentFiles = new ArrayList<MailFile>();
		}
		return this.attachmentFiles;
	}

	/**
	 * Gets the value of the bccList property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the bccList property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getBccList().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link MailPersonel }
	 * 
	 * 
	 */
	public List<MailPersonel> getBccList() {
		if (bccList == null) {
			bccList = new ArrayList<MailPersonel>();
		}
		return this.bccList;
	}

	/**
	 * Gets the value of the body property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getBody() {
		return body;
	}

	/**
	 * Sets the value of the body property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setBody(String value) {
		this.body = value;
	}

	/**
	 * Gets the value of the ccList property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the ccList property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getCcList().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link MailPersonel }
	 * 
	 * 
	 */
	public List<MailPersonel> getCcList() {
		if (ccList == null) {
			ccList = new ArrayList<MailPersonel>();
		}
		return this.ccList;
	}

	/**
	 * Gets the value of the smtpPassword property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSmtpPassword() {
		return smtpPassword;
	}

	/**
	 * Sets the value of the smtpPassword property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSmtpPassword(String value) {
		this.smtpPassword = value;
	}

	/**
	 * Gets the value of the smtpUser property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSmtpUser() {
		return smtpUser;
	}

	/**
	 * Sets the value of the smtpUser property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSmtpUser(String value) {
		this.smtpUser = value;
	}

	/**
	 * Gets the value of the subject property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * Sets the value of the subject property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSubject(String value) {
		this.subject = value;
	}

	/**
	 * Gets the value of the toList property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the toList property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getToList().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link MailPersonel }
	 * 
	 * 
	 */
	public List<MailPersonel> getToList() {
		if (toList == null) {
			toList = new ArrayList<MailPersonel>();
		}
		return this.toList;
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// bu class cloneable oldugu icin buraya girilmemeli...
			throw new InternalError();
		}
	}
}
