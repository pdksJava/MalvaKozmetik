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

@Entity(name = PersonelDenklestirmeDinamikAlan.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { PersonelDenklestirmeDinamikAlan.COLUMN_NAME_PERSONEL_DENKLESTIRME, PersonelDenklestirmeDinamikAlan.COLUMN_NAME_DENKLESTIRME_ALAN_DURUM }) })
public class PersonelDenklestirmeDinamikAlan implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3008263649572064486L;

	static Logger logger = Logger.getLogger(PersonelDenklestirmeDinamikAlan.class);

	public static final String TABLE_NAME = "PERS_DENK_DINAMIK_ALAN";
	public static final String COLUMN_NAME_ID = "ID";
	public static final String COLUMN_NAME_PERSONEL_DENKLESTIRME = "PERS_DENK_ID";
	public static final String COLUMN_NAME_ALAN = "ALAN_ID";
	public static final String COLUMN_NAME_DENKLESTIRME_ALAN_DURUM = "ALAN_DURUM";
	public static final String COLUMN_NAME_DENKLESTIRME_ISLEM_DURUM = "ISLEM_DURUM";
	public static final String TIPI_DENKLESTIRME_DEVAMLILIK_PRIMI = "devamlilikPrimi";

	private Long id;

	private Integer version = 0;

	private PersonelDenklestirme personelDenklestirme;

	private Tanim alan;

	private Boolean durum = Boolean.FALSE, guncellendi = Boolean.FALSE, islemDurum = Boolean.FALSE;

	public PersonelDenklestirmeDinamikAlan() {
		super();

	}

	public PersonelDenklestirmeDinamikAlan(PersonelDenklestirme personelDenklestirme, Tanim alan) {
		super();
		this.personelDenklestirme = personelDenklestirme;
		this.alan = alan;
		if (alan != null) {
			this.durum = alan.getErpKodu() != null && alan.getErpKodu().equals("1");
		}
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

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL_DENKLESTIRME, nullable = false)
	@Fetch(FetchMode.JOIN)
	public PersonelDenklestirme getPersonelDenklestirme() {
		return personelDenklestirme;
	}

	public void setPersonelDenklestirme(PersonelDenklestirme personelDenklestirme) {
		this.personelDenklestirme = personelDenklestirme;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_ALAN, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Tanim getAlan() {
		return alan;
	}

	public void setAlan(Tanim alan) {
		this.alan = alan;
	}

	@Column(name = COLUMN_NAME_DENKLESTIRME_ALAN_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean value) {
		Boolean oldId = durum != null && durum.booleanValue(), newId = value != null && value.booleanValue();
		if (!this.isGuncellendi())
			this.setGuncellendi(!oldId.equals(newId));

		this.durum = value;
	}

	@Column(name = COLUMN_NAME_DENKLESTIRME_ISLEM_DURUM)
	public Boolean getIslemDurum() {
		return islemDurum;
	}

	public void setIslemDurum(Boolean islemDurum) {
		this.islemDurum = islemDurum;
	}

	@Transient
	public String getKey() {
		String key = getKey(alan, personelDenklestirme);
		return key;
	}

	@Transient
	public static String getKey(Tanim alan, PersonelDenklestirme personelDenklestirme) {
		String key = (alan != null ? alan.getId() : 0) + "_" + (personelDenklestirme != null ? personelDenklestirme.getId() : 0);
		return key;
	}

	@Transient
	public Boolean getGuncellendi() {
		return guncellendi;
	}

	public void setGuncellendi(Boolean guncellendi) {
		this.guncellendi = guncellendi;
	}

	@Transient
	public boolean isGuncellendi() {
		return guncellendi && guncellendi.booleanValue();
	}

}
