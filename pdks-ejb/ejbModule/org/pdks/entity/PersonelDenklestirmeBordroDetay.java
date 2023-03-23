package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
public class PersonelDenklestirmeBordroDetay implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 626292798535425843L;

	static Logger logger = Logger.getLogger(PersonelDenklestirmeBordroDetay.class);

	public static final String TABLE_NAME = "PERS_DENK_BORDRO_DETAY";
	public static final String COLUMN_NAME_ID = "ID";
	public static final String COLUMN_NAME_PERSONEL_DENKLESTIRME_BORDRO = "PERS_DENK_BORDRO_ID";
	public static final String COLUMN_NAME_TIPI = "TIPI";
	public static final String COLUMN_NAME_MIKTAR = "MIKTAR";

	private Long id;

	private PersonelDenklestirmeBordro personelDenklestirmeBordro;

	private String tipi;

	private BordroIzinGrubu bordroIzinGrubu;

	private Double miktar = 0.0d;

	private boolean guncellendi = false;

	public PersonelDenklestirmeBordroDetay() {
		super();

	}

	public PersonelDenklestirmeBordroDetay(PersonelDenklestirmeBordro personelDenklestirmeBordro, BordroIzinGrubu bordroIzinGrubu) {
		super();
		this.personelDenklestirmeBordro = personelDenklestirmeBordro;
		this.tipi = bordroIzinGrubu.value();
	}

	@Id
	@GeneratedValue
	@Column(name = COLUMN_NAME_ID)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
			this.bordroIzinGrubu = BordroIzinGrubu.fromValue(value);
		this.tipi = value;
	}

	@Column(name = COLUMN_NAME_MIKTAR)
	public Double getMiktar() {
		return miktar;
	}

	public void setMiktar(Double value) {
		if (guncellendi == false)
			this.setGuncellendi(this.miktar == null || this.miktar.doubleValue() != value.doubleValue());
		this.miktar = value;
	}

	@Transient
	public BordroIzinGrubu getBordroIzinGrubu() {
		return bordroIzinGrubu;
	}

	public void setBordroIzinGrubu(BordroIzinGrubu bordroIzinGrubu) {
		this.bordroIzinGrubu = bordroIzinGrubu;
	}

	@Transient
	public boolean isGuncellendi() {
		return guncellendi;
	}

	public void setGuncellendi(boolean guncellendi) {
		this.guncellendi = guncellendi;
	}

	@Transient
	public static String getDetayKey(PersonelDenklestirmeBordro personelDenklestirmeBordro, String tipi) {
		String detayKey = (personelDenklestirmeBordro != null ? personelDenklestirmeBordro.getId() : 0l) + "_" + (tipi != null ? tipi : "");
		return detayKey;
	}

	@Transient
	public String getDetayKey() {
		String detayKey = getDetayKey(personelDenklestirmeBordro, tipi);
		return detayKey;
	}
}
