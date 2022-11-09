package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;

@Entity(name = "PDKS_HAREKET")
@Immutable
public class PersonelHareket implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5219058136293602171L;

	public static final String COLUMN_NAME_PER_NO = "PER_NO";
	public static final String COLUMN_NAME_PERSONEL = "PERSONEL";

	private Long id;
	private PersonelView personel;
	private PersonelKGS personelKGS;
	private KapiView kapiView;
	private KapiKGS kapiKGS;
	private Date zaman;
	private PersonelHareketIslem islem;
	private String personelNo;
	private Integer durum = 1;
	private Long kgsHareket;

	@Id
	@GeneratedValue
	@Column(name = "ID", nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL, nullable = false)
	@Fetch(FetchMode.JOIN)
	public PersonelView getPersonel() {
		return personel;
	}

	public void setPersonel(PersonelView personel) {
		this.personel = personel;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL, nullable = false, insertable = false, updatable = false)
	@Fetch(FetchMode.JOIN)
	public PersonelKGS getPersonelKGS() {
		return personelKGS;
	}

	public void setPersonelKGS(PersonelKGS personelKGS) {
		this.personelKGS = personelKGS;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "KAPI", nullable = false)
	@Fetch(FetchMode.JOIN)
	public KapiKGS getKapiKGS() {
		return kapiKGS;
	}

	public void setKapiKGS(KapiKGS value) {
		if (value != null)
			this.kapiView = value.getKapiView();
		this.kapiKGS = value;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = "ZAMAN", nullable = false)
	public Date getZaman() {
		return zaman;
	}

	public void setZaman(Date zaman) {
		this.zaman = zaman;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "ISLEM_ID", nullable = false)
	@Fetch(FetchMode.JOIN)
	public PersonelHareketIslem getIslem() {
		return islem;
	}

	public void setIslem(PersonelHareketIslem islem) {
		this.islem = islem;
	}

	@Column(name = "DURUM", columnDefinition = "default '1'")
	public int getDurum() {
		return durum;
	}

	public void setDurum(int durum) {
		this.durum = durum;
	}

	@Column(name = "KGS_ID")
	public Long getKgsHareket() {
		return kgsHareket;
	}

	public void setKgsHareket(Long value) {
		this.kgsHareket = value;
	}

	@Column(name = COLUMN_NAME_PER_NO, insertable = false, updatable = false)
	public String getPersonelNo() {
		return personelNo;
	}

	public void setPersonelNo(String personelNo) {
		this.personelNo = personelNo;
	}

	@Transient
	public KapiView getKapiView() {
		return kapiView;
	}

	public void setKapiView(KapiView kapiView) {
		this.kapiView = kapiView;
	}

}
