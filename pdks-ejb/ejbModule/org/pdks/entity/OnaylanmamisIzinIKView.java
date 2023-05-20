package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;

@Entity(name = OnaylanmamisIzinIKView.TABLE_NAME)
@Immutable
public class OnaylanmamisIzinIKView implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6095112354124457965L;
	/**
	 * 
	 */
	public static final String TABLE_NAME = "PERSONEL_IZIN_IK_ONAYLANMAMIS_VIEW";
	public static final String COLUMN_NAME_ID = "ID";
	private Long id;
	private Long departmanId;
	private PersonelIzinOnay onay;
	private PersonelIzin izin;
	private Date baslangicZamani, bitisZamani;

	@Id
	@Column(name = "ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "ONAY_ID", nullable = false)
	@Fetch(FetchMode.JOIN)
	public PersonelIzinOnay getOnay() {
		return onay;
	}

	public void setOnay(PersonelIzinOnay onay) {
		this.onay = onay;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "PERSONEL_IZIN_ID")
	@Fetch(FetchMode.JOIN)
	public PersonelIzin getIzin() {
		return izin;
	}

	public void setIzin(PersonelIzin izin) {
		this.izin = izin;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = "BASLANGIC_ZAMANI", nullable = false)
	public Date getBaslangicZamani() {
		return baslangicZamani;
	}

	public void setBaslangicZamani(Date baslangicZamani) {
		this.baslangicZamani = baslangicZamani;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = "BITIS_ZAMANI", nullable = false)
	public Date getBitisZamani() {
		return bitisZamani;
	}

	public void setBitisZamani(Date bitisZamani) {
		this.bitisZamani = bitisZamani;
	}

	@Column(name = "DEPARTMAN_ID", nullable = false)
	public Long getDepartmanId() {
		return departmanId;
	}

	public void setDepartmanId(Long departmanId) {
		this.departmanId = departmanId;
	}

}
