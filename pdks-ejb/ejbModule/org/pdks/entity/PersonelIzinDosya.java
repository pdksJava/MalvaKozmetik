/**
 * 
 */
package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * 
 */

@Entity(name = "PERSONEL_IZIN_DOSYA")
public class PersonelIzinDosya extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3123614718217801665L;

	private PersonelIzin personelIzin;

	private Dosya dosya;

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

	public void entityRefresh() {
		// TODO entityRefresh
		
	}

}
