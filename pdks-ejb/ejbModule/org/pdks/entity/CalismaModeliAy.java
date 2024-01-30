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

@Entity(name = CalismaModeliAy.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { CalismaModeliAy.COLUMN_NAME_DONEM, CalismaModeliAy.COLUMN_NAME_CALISMA_MODELI }) })
public class CalismaModeliAy extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4015750209129001721L;
	public static final String TABLE_NAME = "CALISMA_MODELI_AY";

	public static final String COLUMN_NAME_DONEM = "DONEM_ID";
	public static final String COLUMN_NAME_CALISMA_MODELI = "CALISMA_MODELI_ID";
	public static final String COLUMN_NAME_DURUM = "DURUM";
	public static final String COLUMN_NAME_SURE = "SURE";
	public static final String COLUMN_NAME_OTOMATIK_FAZLA_CALISMA_ONAYLANSIN = "OTOMATIK_FAZLA_CALISMA_ONAYLANSIN";
	public static final String COLUMN_NAME_HAREKET_KAYDI_VARDIYA_BUL = "HAREKET_KAYDI_VARDIYA_BUL";
	public static final String COLUMN_NAME_GUN_MAX_CALISMA_SURESI_ODENIR = "GUN_MAX_CALISMA_SURESI_ODENIR";

	private DenklestirmeAy denklestirmeAy;

	private CalismaModeli calismaModeli;

	private double sure = 0, toplamIzinSure = 0, negatifBakiyeDenkSaat = 0.0d;

	private Boolean hareketKaydiVardiyaBul = Boolean.FALSE, otomatikFazlaCalismaOnaylansin = Boolean.FALSE, gunMaxCalismaOdemeDurum = Boolean.TRUE;

	public CalismaModeliAy() {
		super();
	}

	public CalismaModeliAy(DenklestirmeAy denklestirmeAy, CalismaModeli calismaModeli) {
		super();
		this.denklestirmeAy = denklestirmeAy;
		this.calismaModeli = calismaModeli;
		if (calismaModeli != null) {
			this.negatifBakiyeDenkSaat = calismaModeli.getNegatifBakiyeDenkSaat();
			this.hareketKaydiVardiyaBul = calismaModeli.getHareketKaydiVardiyaBul();
			this.otomatikFazlaCalismaOnaylansin = calismaModeli.getOtomatikFazlaCalismaOnaylansin();
			this.gunMaxCalismaOdemeDurum = calismaModeli.isGunMaxCalismaOdenir();
		}
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

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_CALISMA_MODELI)
	@Fetch(FetchMode.JOIN)
	public CalismaModeli getCalismaModeli() {
		return calismaModeli;
	}

	public void setCalismaModeli(CalismaModeli calismaModeli) {
		this.calismaModeli = calismaModeli;
	}

	@Column(name = "SURE")
	public double getSure() {
		return sure;
	}

	public void setSure(double sure) {
		this.sure = sure;
	}

	@Column(name = "SUT_IZNI_SURE")
	public double getToplamIzinSure() {
		return toplamIzinSure;
	}

	public void setToplamIzinSure(double toplamIzinSure) {
		this.toplamIzinSure = toplamIzinSure;
	}

	@Column(name = "NEGATIF_BAKIYE_SAAT")
	public double getNegatifBakiyeDenkSaat() {
		return negatifBakiyeDenkSaat;
	}

	public void setNegatifBakiyeDenkSaat(double negatifBakiyeDenkSaat) {
		this.negatifBakiyeDenkSaat = negatifBakiyeDenkSaat;
	}

	@Column(name = COLUMN_NAME_HAREKET_KAYDI_VARDIYA_BUL)
	public Boolean getHareketKaydiVardiyaBul() {
		return hareketKaydiVardiyaBul;
	}

	public void setHareketKaydiVardiyaBul(Boolean hareketKaydiVardiyaBul) {
		this.hareketKaydiVardiyaBul = hareketKaydiVardiyaBul;
	}

	@Column(name = COLUMN_NAME_OTOMATIK_FAZLA_CALISMA_ONAYLANSIN)
	public Boolean getOtomatikFazlaCalismaOnaylansin() {
		return otomatikFazlaCalismaOnaylansin;
	}

	public void setOtomatikFazlaCalismaOnaylansin(Boolean otomatikFazlaCalismaOnaylansin) {
		this.otomatikFazlaCalismaOnaylansin = otomatikFazlaCalismaOnaylansin;
	}

	@Column(name = COLUMN_NAME_GUN_MAX_CALISMA_SURESI_ODENIR)
	public Boolean getGunMaxCalismaOdemeDurum() {
		return gunMaxCalismaOdemeDurum;
	}

	public void setGunMaxCalismaOdemeDurum(Boolean gunMaxCalismaOdemeDurum) {
		this.gunMaxCalismaOdemeDurum = gunMaxCalismaOdemeDurum;
	}

	@Transient
	public static String getKey(DenklestirmeAy xDenklestirmeAy, CalismaModeli xCalismaModeli) {
		String key = (xDenklestirmeAy != null ? xDenklestirmeAy.getId() : 0) + "_" + (xCalismaModeli != null ? xCalismaModeli.getId() : 0);
		return key;
	}

	@Transient
	public boolean isHareketKaydiVardiyaBulsunmu() {
		return hareketKaydiVardiyaBul != null && hareketKaydiVardiyaBul.booleanValue();
	}

	@Transient
	public boolean isOtomatikFazlaCalismaOnaylansinmi() {
		return otomatikFazlaCalismaOnaylansin != null && otomatikFazlaCalismaOnaylansin.booleanValue();
	}

	@Transient
	public String getKey() {
		String key = getKey(denklestirmeAy, calismaModeli);
		return key;
	}

	@Transient
	public boolean isGunMaxCalismaOdenir() {
		return gunMaxCalismaOdemeDurum != null && gunMaxCalismaOdemeDurum;
	}
}
