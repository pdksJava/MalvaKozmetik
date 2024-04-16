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

@Entity(name = PersonelDenklestirmeDinamikAlan.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { PersonelDenklestirmeDinamikAlan.COLUMN_NAME_PERSONEL_DENKLESTIRME, PersonelDenklestirmeDinamikAlan.COLUMN_NAME_DENKLESTIRME_ALAN_DURUM }) })
public class PersonelDenklestirmeDinamikAlan extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3008263649572064486L;

	static Logger logger = Logger.getLogger(PersonelDenklestirmeDinamikAlan.class);

	public static final String TABLE_NAME = "PERS_DENK_DINAMIK_ALAN";
	public static final String COLUMN_NAME_PERSONEL_DENKLESTIRME = "PERS_DENK_ID";
	public static final String COLUMN_NAME_ALAN = "ALAN_ID";
	public static final String COLUMN_NAME_TANIM_DEGER = "TANIM_DEGER_ID";
	public static final String COLUMN_NAME_DENKLESTIRME_ALAN_DURUM = "ALAN_DURUM";
	public static final String COLUMN_NAME_DENKLESTIRME_ISLEM_DURUM = "ISLEM_DURUM";
	public static final String COLUMN_NAME_SAYISAL_DEGER = "SAYISAL_DEGER";
	public static final String TIPI_DEVAMLILIK_PRIMI = "devamlikDurum";

	private Integer version = 0;

	private PersonelDenklestirme personelDenklestirme;

	private Tanim alan, tipi;

	private Boolean durum = Boolean.FALSE, islemDurum = Boolean.FALSE;

	private Double sayisalDeger;

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

	public void setAlan(Tanim value) {
		this.tipi = value != null ? value.getParentTanim() : null;
		this.alan = value;
	}

	@Transient
	public Tanim getTipi() {
		return tipi;
	}

	public void setTipi(Tanim tipi) {
		this.tipi = tipi;
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

	@Column(name = COLUMN_NAME_SAYISAL_DEGER)
	public Double getSayisalDeger() {
		return sayisalDeger;
	}

	public void setSayisalDeger(Double sayisalDeger) {
		this.sayisalDeger = sayisalDeger;
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
	public boolean isDevamlilikPrimi() {
		boolean devamlilikPrimi = alan != null && alan.getKodu().equals(TIPI_DEVAMLILIK_PRIMI);
		return devamlilikPrimi;
	}

	@Transient
	public boolean isIzinDurum() {
		boolean izinDurum = alan != null && alan.getKodu().startsWith("IZIN");
		return izinDurum;
	}

	@Transient
	public boolean isAciklama() {
		boolean tip = false;
		if (alan != null && alan.getKodu() != null) {
			tip = alan.getTipi().equals(Tanim.TIPI_PERSONEL_DINAMIK_SAYISAL);
		}
		return tip;
	}

	@Transient
	public boolean isCheckBox() {
		boolean tip = false;
		if (alan != null && alan.getKodu() != null) {
			tip = alan.getTipi().equals(Tanim.TIPI_PERSONEL_DENKLESTIRME_DINAMIK_DURUM);
		}
		return tip;
	}

	@Transient
	public boolean isSayisal() {
		boolean tip = false;
		if (alan != null && alan.getKodu() != null) {
			tip = alan.getTipi().equals(Tanim.TIPI_DENKLESTIRME_DINAMIK_SAYISAL);
		}
		return tip;
	}

	@Transient
	public boolean isTanim() {
		boolean tip = false;
		if (alan != null && alan.getKodu() != null) {
			tip = alan.getTipi().equals(Tanim.TIPI_DENKLESTIRME_DINAMIK_TANIM);
		}
		return tip;
	}

}
