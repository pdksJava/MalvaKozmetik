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

@Entity(name = CalismaModeliGun.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { CalismaModeliGun.COLUMN_NAME_CALISMA_MODELI, CalismaModeliGun.COLUMN_NAME_GUN_TIPI, CalismaModeliGun.COLUMN_NAME_HAFTA_GUN }) })
public class CalismaModeliGun extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	public static final int GUN_SAAT = 1;
	public static final int GUN_IZIN = 2;
	private static final long serialVersionUID = -8626070485848428888L;
	public static final String TABLE_NAME = "CALISMA_MODELI_GUN";
	public static final String COLUMN_NAME_SURE = "SURE";
	public static final String COLUMN_NAME_GUN_TIPI = "GUN_TIPI";
	public static final String COLUMN_NAME_HAFTA_GUN = "HAFTA_GUN";
	public static final String COLUMN_NAME_CALISMA_MODELI = "CALISMA_MODELI_ID";

	private CalismaModeli calismaModeli;
	private int gunTipi = GUN_SAAT, haftaGun;
	private double sure;

	public CalismaModeliGun() {
		super();
	}

	public CalismaModeliGun(CalismaModeli calismaModeli, int gunTipi, int haftaGun) {
		super();
		this.calismaModeli = calismaModeli;
		this.gunTipi = gunTipi;
		this.haftaGun = haftaGun;
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

	@Column(name = COLUMN_NAME_HAFTA_GUN)
	public int getHaftaGun() {
		return haftaGun;
	}

	public void setHaftaGun(int haftaGun) {
		this.haftaGun = haftaGun;
	}

	@Column(name = COLUMN_NAME_GUN_TIPI)
	public int getGunTipi() {
		return gunTipi;
	}

	public void setGunTipi(int gunTipi) {
		this.gunTipi = gunTipi;
	}

	@Column(name = COLUMN_NAME_SURE)
	public double getSure() {
		return sure;
	}

	public void setSure(double value) {
		if (this.getGuncellendi() == null || !this.getGuncellendi())
			this.setGuncellendi(sure != value);
		this.sure = value;
	}

	@Transient
	public static String getKey(CalismaModeli xCalismaModeli, int xGunTipi, int xHaftaGun) {
		String key = (xCalismaModeli != null && xCalismaModeli.getId() != null ? xCalismaModeli.getId() : 0) + "_" + xGunTipi + "_" + xHaftaGun;
		return key;
	}

	@Transient
	public String getKey() {
		String key = getKey(calismaModeli, gunTipi, haftaGun);
		return key;
	}

	@Transient
	public String getAciklama() {
		String aciklama = "";
		switch (gunTipi) {
		case GUN_SAAT:
			aciklama = "Hafta İçi Günlük Saatler";
			break;
		case GUN_IZIN:
			aciklama = "Hafta İçi Günlük Süt İzni Saatler";
			break;
		default:
			break;
		}
		return aciklama;
	}

	@Transient
	public int getSaat() {
		return GUN_SAAT;
	}

	@Transient
	public int getIzin() {
		return GUN_IZIN;
	}

	public void entityRefresh() {
		// TODO entityRefresh

	}

}
