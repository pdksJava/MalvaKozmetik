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

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = PersonelDinamikAlan.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { PersonelDinamikAlan.COLUMN_NAME_PERSONEL, PersonelDinamikAlan.COLUMN_NAME_ALAN }) })
public class PersonelDinamikAlan extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3881392269334061361L;

	public static final String TABLE_NAME = "PERSONEL_DINAMIK_ALAN";
	public static final String COLUMN_NAME_PERSONEL = "PERSONEL_ID";
	public static final String COLUMN_NAME_ALAN = "ALAN_ID";
	public static final String COLUMN_NAME_TANIM_DEGER = "TANIM_DEGER_ID";
	public static final String COLUMN_NAME_DURUM_SECIM = "DURUM_SECIM";
	public static final String COLUMN_NAME_SAYISAL_DEGER = "SAYISAL_DEGER";

	// public static final String TIP_ACIKLAMA = "PERSONEL_DINAMIK_ACIKLAMA";
	// public static final String TIP_DURUM = "PERSONEL_DINAMIK_DURUM";
	// public static final String TIP_SAYISAL = "PERSONEL_DINAMIK_SAYISAL";
	// public static final String TIP_TANIM = "PERSONEL_DINAMIK_TANIM";

	public PersonelDinamikAlan(Personel personel, Tanim alan) {
		super();
		this.personel = personel;
		this.alan = alan;
		if (alan != null) {
			if (alan.getTipi().equals(Tanim.TIPI_PERSONEL_DINAMIK_DURUM))
				this.durumSecim = alan.getErpKodu() != null && alan.getErpKodu().equals("1");
		}
	}

	public PersonelDinamikAlan() {
		super();

	}

	private Personel personel;

	private Tanim alan, tanimDeger, tipi;

	private Boolean durumSecim = Boolean.FALSE;

	private Double sayisalDeger;

	private Integer version = 0;

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Personel getPersonel() {
		return personel;
	}

	public void setPersonel(Personel personel) {
		this.personel = personel;
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

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_TANIM_DEGER)
	@Fetch(FetchMode.JOIN)
	public Tanim getTanimDeger() {
		return tanimDeger;
	}

	public void setTanimDeger(Tanim tanimDeger) {
		this.tanimDeger = tanimDeger;
	}

	

	@Column(name = COLUMN_NAME_SAYISAL_DEGER)
	public Double getSayisalDeger() {
		return sayisalDeger;
	}

	public void setSayisalDeger(Double sayisalDeger) {
		this.sayisalDeger = sayisalDeger;
	}
	@Column(name = COLUMN_NAME_DURUM_SECIM)
	public Boolean getDurumSecim() {
		return durumSecim;
	}
	public void setDurumSecim(Boolean durumSecim) {
		this.durumSecim = durumSecim;
	}

	@Transient
	public static String getKey(Personel personel, Tanim alan) {
		String key = (personel != null ? personel.getId() : "") + "_" + (alan != null ? alan.getId() : "");
		return key;
	}

	@Transient
	public String getKey() {
		String key = getKey(personel, alan);
		return key;
	}

	@Transient
	public boolean isDurumSecili() {
		return durumSecim != null && durumSecim.booleanValue();
	}

	@Transient
	public Personel getPdksPersonel() {
		return personel;
	}

	@Transient
	public boolean isAciklama() {
		boolean tip = false;
		if (tipi != null && tipi.getKodu() != null) {
			tip = tipi.getKodu().equals(Tanim.TIPI_PERSONEL_DINAMIK_SAYISAL);
		}
		return tip;
	}

	@Transient
	public boolean isCheckBox() {
		boolean tip = false;
		if (tipi != null && tipi.getKodu() != null) {
			tip = tipi.getKodu().equals(Tanim.TIPI_PERSONEL_DINAMIK_DURUM);
		}
		return tip;
	}

	@Transient
	public boolean isSayisal() {
		boolean tip = false;
		if (tipi != null && tipi.getKodu() != null) {
			tip = tipi.getKodu().equals(Tanim.TIPI_PERSONEL_DINAMIK_SAYISAL);
		}
		return tip;
	}

	@Transient
	public boolean isTanim() {
		boolean tip = false;
		if (tipi != null && tipi.getKodu() != null) {
			tip = tipi.getKodu().equals(Tanim.TIPI_PERSONEL_DINAMIK_TANIM);
		}
		return tip;
	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}
}
