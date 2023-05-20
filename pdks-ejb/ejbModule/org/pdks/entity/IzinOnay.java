package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Embeddable
public class IzinOnay implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2243793232101002019L;

	private PersonelIzin personelIzin;

	private String onaylayanTipi;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "PERSONEL_IZIN_ID", nullable = false)
	@Fetch(FetchMode.JOIN)
	public PersonelIzin getPersonelIzin() {
		return personelIzin;
	}

	public void setPersonelIzin(PersonelIzin personelIzin) {
		this.personelIzin = personelIzin;
	}

	@Column(name = "ONAYLAYAN_TIPI", nullable = false, length = 1)
	public String getOnaylayanTipi() {
		return onaylayanTipi;
	}

	public void setOnaylayanTipi(String onaylayanTipi) {
		this.onaylayanTipi = onaylayanTipi;
	}

}
