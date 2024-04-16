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

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = PersonelDenklestirmeBordroDetay.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { PersonelDenklestirmeBordroDetay.COLUMN_NAME_PERSONEL_DENKLESTIRME_BORDRO, PersonelDenklestirmeBordroDetay.COLUMN_NAME_TIPI }) })
public class PersonelDenklestirmeBordroDetay extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 626292798535425843L;

	static Logger logger = Logger.getLogger(PersonelDenklestirmeBordroDetay.class);

	public static final String TABLE_NAME = "PERS_DENK_BORDRO_DETAY";
	public static final String COLUMN_NAME_PERSONEL_DENKLESTIRME_BORDRO = "PERS_DENK_BORDRO_ID";
	public static final String COLUMN_NAME_TIPI = "TIPI";
	public static final String COLUMN_NAME_MIKTAR = "MIKTAR";

	private PersonelDenklestirmeBordro personelDenklestirmeBordro;

	private String tipi;

	private BordroDetayTipi bordroDetayTipi;

	private Double miktar = 0.0d;

	public PersonelDenklestirmeBordroDetay() {
		super();

	}

	public PersonelDenklestirmeBordroDetay(PersonelDenklestirmeBordro personelDenklestirmeBordro, BordroDetayTipi bordroDetayTipi) {
		super();
		this.personelDenklestirmeBordro = personelDenklestirmeBordro;
		this.tipi = bordroDetayTipi.value();
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL_DENKLESTIRME_BORDRO, nullable = false)
	@Fetch(FetchMode.JOIN)
	public PersonelDenklestirmeBordro getPersonelDenklestirmeBordro() {
		return personelDenklestirmeBordro;
	}

	public void setPersonelDenklestirmeBordro(PersonelDenklestirmeBordro personelDenklestirmeBordro) {
		this.personelDenklestirmeBordro = personelDenklestirmeBordro;
	}

	@Column(name = COLUMN_NAME_TIPI, nullable = false)
	public String getTipi() {
		return tipi;
	}

	public void setTipi(String value) {
		if (value != null)
			this.bordroDetayTipi = BordroDetayTipi.fromValue(value);
		this.tipi = value;
	}

	@Column(name = COLUMN_NAME_MIKTAR)
	public Double getMiktar() {
		return miktar;
	}

	public void setMiktar(Double value) {
		if (this.isGuncellendi() == false)
			this.setGuncellendi(this.miktar == null || this.miktar.doubleValue() != value.doubleValue());
		this.miktar = value;
	}

	@Transient
	public static String getDetayKey(PersonelDenklestirmeBordro personelDenklestirmeBordro, String tipi) {
		String detayKey = null;
		try {
			detayKey = (personelDenklestirmeBordro != null && personelDenklestirmeBordro.getId() != null ? personelDenklestirmeBordro.getId() : 0l) + "_" + (tipi != null ? tipi : "");
		} catch (Exception e) {
		}
		return detayKey;
	}

	@Transient
	public String getDetayKey() {
		String detayKey = getDetayKey(personelDenklestirmeBordro, tipi);
		return detayKey;
	}

	@Transient
	public BordroDetayTipi getBordroDetayTipi() {
		return bordroDetayTipi;
	}

	public void setBordroDetayTipi(BordroDetayTipi bordroDetayTipi) {
		this.bordroDetayTipi = bordroDetayTipi;
	}
}
