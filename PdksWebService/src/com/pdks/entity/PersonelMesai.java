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

@Entity
@Immutable
public class PersonelMesai implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7158233889486878730L;

	static Logger logger = Logger.getLogger(PersonelMesai.class);

	public static final String COLUMN_NAME_ID = "ID";
	public static final String COLUMN_NAME_PERSONEL = "PERSONEL_ID";
	public static final String COLUMN_NAME_ERP_KODU = "ERP_KODU";
	public static final String COLUMN_NAME_SURE = "SURE";

	private Long id;

	private Personel personel;

	private Double sure = 0d;
	private String erpKodu;

	@Id
	@Column(name = COLUMN_NAME_ID)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Personel getPersonel() {
		return personel;
	}

	public void setPersonel(Personel value) {

		this.personel = value;
	}

	@Column(name = COLUMN_NAME_SURE)
	public Double getSure() {
		return sure;
	}

	public void setSure(Double sure) {
		this.sure = sure;
	}

	@Column(name = COLUMN_NAME_ERP_KODU)
	public String getErpKodu() {
		return erpKodu;
	}

	public void setErpKodu(String erpKodu) {
		this.erpKodu = erpKodu;
	}

}
