package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

@Entity(name = MailGrubu.TABLE_NAME)
public class MailGrubu implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8644233609697762979L;

	static Logger logger = Logger.getLogger(MailGrubu.class);

	public static final String TABLE_NAME = "MAIL_GRUBU";
	public static final String COLUMN_NAME_ID = "ID";

	public static final String COLUMN_NAME_TIPI = "TIPI";
	public static final String COLUMN_NAME_MAIL = "EMAIL";

	public static final String TIPI_TO = "TO";
	public static final String TIPI_CC = "CC";
	public static final String TIPI_BCC = "BCC";
	public static final String TIPI_HAREKET = "HRK";
	private Long id;

	private String tipi, email;

	private Boolean guncellendi;

	public MailGrubu(String tipi, String email) {
		super();
		this.tipi = tipi;
		this.email = email;
		this.guncellendi = Boolean.FALSE;
	}

	public MailGrubu() {
		super();
		this.guncellendi = Boolean.FALSE;
	}

	@Id
	@GeneratedValue
	@Column(name = COLUMN_NAME_ID)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = COLUMN_NAME_TIPI)
	public String getTipi() {
		return tipi;
	}

	public void setTipi(String tipi) {
		this.tipi = tipi;
	}

	@Column(name = COLUMN_NAME_MAIL)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Transient
	public Boolean getGuncellendi() {
		return guncellendi;
	}

	public void setGuncellendi(Boolean guncellendi) {
		this.guncellendi = guncellendi;
	}

}