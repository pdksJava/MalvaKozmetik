package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = "PERSONEL_EXTRA")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "PERSONEL_ID" }) })
public class PersonelExtra extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3881392269334061361L;

	private Personel personel;

	private String cepTelefon = "", ilce = "", ozelNot = "";

	private Integer version = 0;

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "PERSONEL_ID", nullable = false)
	@Fetch(FetchMode.JOIN)
	public Personel getPersonel() {
		return personel;
	}

	public void setPersonel(Personel personel) {
		this.personel = personel;
	}

	@Column(name = "CEP_TELEFON", length = 15)
	public String getCepTelefon() {
		return cepTelefon;
	}

	public void setCepTelefon(String cepTelefon) {
		this.cepTelefon = cepTelefon;
	}

	@Column(name = "ILCE", length = 50)
	public String getIlce() {
		return ilce;
	}

	public void setIlce(String ilce) {
		this.ilce = ilce;
	}

	@Column(name = "OZEL_NOT", length = 100)
	public String getOzelNot() {
		return ozelNot;
	}

	public void setOzelNot(String ozelNot) {
		this.ozelNot = ozelNot;
	}

	@Transient
	public Long getPersonelId() {
		return personel != null ? personel.getId() : 0;
	}

	@Transient
	public Personel getPdksPersonel() {
		return personel;
	}
}
