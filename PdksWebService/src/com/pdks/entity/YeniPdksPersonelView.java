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
import org.hibernate.annotations.Immutable;

@Entity(name = YeniPdksPersonelView.TABLE_NAME)
@Immutable
public class YeniPdksPersonelView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5455483264126716446L;
	static Logger logger = Logger.getLogger(YeniPdksPersonelView.class);
	/**
	 * 
	 */
	public static final String TABLE_NAME = "YENI_PERSONEL_SIRKET_KGS";
	public static final String COLUMN_NAME_ID = "ID";
	public static final String COLUMN_NAME_PERSONEL = "PERSONEL_ID";
	public static final String COLUMN_NAME_KGS_PERSONEL = "KGS_PERSONEL_ID";

	private Long id;
	private Personel pdksPersonel;
	private PersonelKGS personelKGS;

	@Id
	@Column(name = COLUMN_NAME_ID)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL)
	@Fetch(FetchMode.JOIN)
	public Personel getPdksPersonel() {
		return pdksPersonel;
	}

	public void setPdksPersonel(Personel pdksPersonel) {
		this.pdksPersonel = pdksPersonel;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_KGS_PERSONEL)
	@Fetch(FetchMode.JOIN)
	public PersonelKGS getPersonelKGS() {
		return personelKGS;
	}

	public void setPersonelKGS(PersonelKGS personelKGS) {
		this.personelKGS = personelKGS;
	}

}
