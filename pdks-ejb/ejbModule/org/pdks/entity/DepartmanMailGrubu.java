package org.pdks.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = "DEPARTMANMAILGRUBU")
public class DepartmanMailGrubu extends BaseObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8560853194023608673L;
	
	private Tanim departman;
	private String mailAdress;
	
	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "DEPARTMAN_ID")
	@Fetch(FetchMode.JOIN)
	public Tanim getDepartman() {
		return departman;
	}
	
	public void setDepartman(Tanim departman) {
		this.departman = departman;
	}
	
	@Column(name = "MAIL_ADRESS")
	public String getMailAdress() {
		return mailAdress;
	}
	public void setMailAdress(String mailAdress) {
		this.mailAdress = mailAdress;
	}

}
