package com.pdks.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = IzinReferansERP.TABLE_NAME)
public class IzinReferansERP implements Serializable {
	// seam-gen attributes (you should probably edit these)

	/**
	 * 
	 */
	private static final long serialVersionUID = -1508779437229330518L;
	static Logger logger = Logger.getLogger(IzinReferansERP.class);

	public static final String TABLE_NAME = "IZIN_REFERANS_ERP";
	public static final String COLUMN_NAME_ID = "REFERANS_ID";
	public static final String COLUMN_NAME_IZIN_ID = "IZIN_ID";

	private String id;

	private PersonelIzin izin;

	public IzinReferansERP() {
		super();

	}

	public IzinReferansERP(String id) {
		super();
		this.id = id;
		this.izin = new PersonelIzin();
	}

	@Id
	@Column(name = COLUMN_NAME_ID)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_IZIN_ID, nullable = false)
	@Fetch(FetchMode.JOIN)
	public PersonelIzin getIzin() {
		return izin;
	}

	public void setIzin(PersonelIzin izin) {
		this.izin = izin;
	}

}
