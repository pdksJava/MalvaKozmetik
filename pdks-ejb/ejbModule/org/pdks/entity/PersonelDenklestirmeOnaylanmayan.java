package org.pdks.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;

@Entity(name = PersonelDenklestirmeOnaylanmayan.TABLE_NAME)
@Immutable
public class PersonelDenklestirmeOnaylanmayan implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6488002186767192121L;
	public static final String TABLE_NAME = "ONAYLANMAYAN_PERSONEL_DENKLESTIRME";
	public static final String COLUMN_NAME_ID = "ID";
	public static final String COLUMN_NAME_DONEM = "DONEM_ID";
	public static final String COLUMN_NAME_YIL = "YIL";
	public static final String COLUMN_NAME_AY = "AY";
	public static final String COLUMN_NAME_PDKS_SICIL_NO = "PDKS_SICIL_NO";
	public static final String COLUMN_NAME_PERSONEL = "PERSONEL";
	public static final String COLUMN_NAME_PERSONEL_ID = "PERSONEL_ID";
	public static final String COLUMN_NAME_YONETICI = "YONETICI_ID";
	public static final String COLUMN_NAME_BOLUM = "BOLUM_ID";
	public static final String COLUMN_NAME_DENKLESTIRME_ID = "DENKLESTIRME_ID";

	private BigDecimal id;

	private Integer yil, ay;

	private Personel personel, yonetici;

	private DenklestirmeAy denklestirmeAy;

	private PersonelDenklestirme personelDenklestirme;

	private String adiSoyadi, pdksSicilNo;

	public PersonelDenklestirmeOnaylanmayan() {
		super();
	}

	@Id
	@Column(name = COLUMN_NAME_ID)
	public BigDecimal getId() {
		return id;
	}

	public void setId(BigDecimal id) {
		this.id = id;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL_ID)
	@Fetch(FetchMode.JOIN)
	public Personel getPersonel() {
		return personel;
	}

	public void setPersonel(Personel personel) {
		this.personel = personel;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_DONEM)
	@Fetch(FetchMode.JOIN)
	public DenklestirmeAy getDenklestirmeAy() {
		return denklestirmeAy;
	}

	public void setDenklestirmeAy(DenklestirmeAy denklestirmeAy) {
		this.denklestirmeAy = denklestirmeAy;
	}

	@Column(name = COLUMN_NAME_YIL)
	public Integer getYil() {
		return yil;
	}

	public void setYil(Integer yil) {
		this.yil = yil;
	}

	@Column(name = COLUMN_NAME_AY)
	public Integer getAy() {
		return ay;
	}

	public void setAy(Integer ay) {
		this.ay = ay;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_YONETICI)
	@Fetch(FetchMode.JOIN)
	public Personel getYonetici() {
		return yonetici;
	}

	public void setYonetici(Personel yonetici) {
		this.yonetici = yonetici;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_DENKLESTIRME_ID)
	@Fetch(FetchMode.JOIN)
	public PersonelDenklestirme getPersonelDenklestirme() {
		return personelDenklestirme;
	}

	public void setPersonelDenklestirme(PersonelDenklestirme personelDenklestirme) {
		this.personelDenklestirme = personelDenklestirme;
	}

	@Column(name = COLUMN_NAME_PERSONEL)
	public String getAdiSoyadi() {
		return adiSoyadi;
	}

	public void setAdiSoyadi(String adiSoyadi) {
		this.adiSoyadi = adiSoyadi;
	}

	@Column(name = COLUMN_NAME_PDKS_SICIL_NO)
	public String getPdksSicilNo() {
		return pdksSicilNo;
	}

	public void setPdksSicilNo(String pdksSicilNo) {
		this.pdksSicilNo = pdksSicilNo;
	}

	@Transient
	public Personel getPdksPersonel() {
		return personel;
	}

	@Transient
	public PersonelDenklestirme getPersonelDenklestirmeAy() {
		PersonelDenklestirme denklestirme = null;
		if (personelDenklestirme == null) {
			denklestirme = new PersonelDenklestirme();
			denklestirme.setDenklestirmeAy(denklestirmeAy);
			denklestirme.setPersonel(personel);
			denklestirme.setDenklestirme(Boolean.TRUE);
		} else
			denklestirme = personelDenklestirme;
		return denklestirme;
	}
}
