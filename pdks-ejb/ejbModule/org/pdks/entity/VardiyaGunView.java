package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;

@Entity(name = "VARDIYA_GUN_VIEW")
@Immutable
public class VardiyaGunView implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3921863401103682355L;
	/**
	 * 
	 */

	private Long id;
	private VardiyaGun vardiyaGun;
	private VardiyaGorev vardiyaGorev;
	// private pdksPersonel personel;
	private Date vardiyaDate;

	@Id
	@Column(name = "ID", updatable = false, insertable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "VARDIYA_GUN_ID", updatable = false, insertable = false)
	@Fetch(FetchMode.JOIN)
	public VardiyaGun getVardiyaGun() {
		return vardiyaGun;
	}

	public void setVardiyaGun(VardiyaGun value) {
		this.vardiyaGun = value;
	}

	@OneToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "VARDIYA_GOREV_ID", updatable = false, insertable = false)
	@Fetch(FetchMode.JOIN)
	public VardiyaGorev getVardiyaGorev() {
		return vardiyaGorev;
	}

	public void setVardiyaGorev(VardiyaGorev value) {
		this.vardiyaGorev = value;
	}

	@Column(name = "VARDIYA_TARIHI", updatable = false, insertable = false)
	public Date getVardiyaDate() {
		return vardiyaDate;
	}

	public void setVardiyaDate(Date vardiyaDate) {
		this.vardiyaDate = vardiyaDate;
	}

	// @ManyToOne(cascade = CascadeType.REFRESH)
	// @JoinColumn(name = "PERSONEL_ID", updatable = false, insertable = false)
	// @Fetch(FetchMode.JOIN)
	@Transient
	public Personel getPersonel() {
		return vardiyaGun != null ? vardiyaGun.getPersonel() : null;
	}

	// public void setPersonel(pdksPersonel personel) {
	// this.personel = personel;
	// }

}
