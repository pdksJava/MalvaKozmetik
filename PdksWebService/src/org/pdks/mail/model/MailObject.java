package org.pdks.mail.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MailObject implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9043256081556452280L;

	private List<MailPersonel> toList;

	private List<MailPersonel> ccList;

	private List<MailPersonel> bccList;

	private String subject, body;

	private String smtpUser, smtpPassword;

	private List<MailFile> attachmentFiles;

	public List<MailPersonel> getToList() {
		if (toList == null)
			toList = new ArrayList<MailPersonel>();
		return toList;
	}

	public void setToList(List<MailPersonel> toList) {
		this.toList = toList;
	}

	public List<MailPersonel> getCcList() {
		if (ccList == null)
			ccList = new ArrayList<MailPersonel>();
		return ccList;
	}

	public void setCcList(List<MailPersonel> ccList) {
		this.ccList = ccList;
	}

	public List<MailPersonel> getBccList() {
		if (bccList == null)
			bccList = new ArrayList<MailPersonel>();
		return bccList;
	}

	public void setBccList(List<MailPersonel> bccList) {
		this.bccList = bccList;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public List<MailFile> getAttachmentFiles() {
		if (attachmentFiles == null)
			attachmentFiles = new ArrayList<MailFile>();
		return attachmentFiles;
	}

	public void setAttachmentFiles(List<MailFile> attachmentFiles) {
		this.attachmentFiles = attachmentFiles;
	}

	public String getSmtpUser() {
		return smtpUser;
	}

	public void setSmtpUser(String smtpUser) {
		this.smtpUser = smtpUser;
	}

	public String getSmtpPassword() {
		return smtpPassword;
	}

	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
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
