/**
 * 
 */
package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * 
 */

@Entity(name = "PERSONEL_IZIN_DOSYA")
public class PersonelIzinDosya implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3123614718217801665L;

	private Long id;

	private PersonelIzin personelIzin;

	private Dosya dosya;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@OneToOne(cascade = CascadeType.REFRESH, optional = false)
	@JoinColumn(name = "PERSONEL_IZIN_ID")
	@Fetch(FetchMode.JOIN)
	public PersonelIzin getPersonelIzin() {
		return personelIzin;
	}

	public void setPersonelIzin(PersonelIzin personelIzin) {
		this.personelIzin = personelIzin;
	}

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "DOSYA_ID")
	@Fetch(FetchMode.JOIN)
	public Dosya getDosya() {
		return dosya;
	}

	public void setDosya(Dosya dosya) {
		this.dosya = dosya;
	}

	@Transient
	public Long getPersonelIzinId() {
		long personelIzinId = personelIzin != null ? personelIzin.getId() : 0;
		return personelIzinId;

	}

}
