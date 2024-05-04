package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

@Entity(name = VardiyaSaat.TABLE_NAME)
public class VardiyaSaat extends BasePDKSObject implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8682443348377801487L;

	static Logger logger = Logger.getLogger(VardiyaSaat.class);

	public static final String TABLE_NAME = "VARDIYA_SAAT";
	public static final String COLUMN_NAME_NORMAL_SURE = "NORMAL_SURE";
	public static final String COLUMN_NAME_CALISMA_SURESI = "CALISMA_SURESI";
	public static final String COLUMN_NAME_RESMI_TATIL_SURESI = "RESMI_TATIL_SURESI";
	public static final String COLUMN_NAME_AKSAM_VARDIYA = "AKSAM_VARDIYA_SURESI";
	public static final String COLUMN_NAME_GUNCELLEME_TARIHI = "GUNCELLEME_TARIHI";

	private double normalSure = 0d, calismaSuresi = 0d, resmiTatilSure = 0d, aksamVardiyaSaatSayisi = 0d;

	private Date guncellemeTarihi;

	public VardiyaSaat() {
		super();
		this.guncellendi = false;
	}

	@Column(name = COLUMN_NAME_NORMAL_SURE)
	public double getNormalSure() {
		return normalSure;
	}

	public void setNormalSure(double value) {
		if (!guncellendi)
			guncellendi = value != normalSure;
		this.normalSure = value;
	}

	@Column(name = COLUMN_NAME_CALISMA_SURESI)
	public double getCalismaSuresi() {
		return calismaSuresi;
	}

	public void setCalismaSuresi(double value) {
		if (!guncellendi)
			guncellendi = value != calismaSuresi;
		this.calismaSuresi = value;
	}

	@Column(name = COLUMN_NAME_RESMI_TATIL_SURESI)
	public double getResmiTatilSure() {
		return resmiTatilSure;
	}

	public void setResmiTatilSure(double value) {
		if (!guncellendi)
			guncellendi = value != resmiTatilSure;
		this.resmiTatilSure = value;
	}

	@Column(name = COLUMN_NAME_AKSAM_VARDIYA)
	public double getAksamVardiyaSaatSayisi() {
		return aksamVardiyaSaatSayisi;
	}

	public void setAksamVardiyaSaatSayisi(double value) {
		if (!guncellendi)
			guncellendi = value != aksamVardiyaSaatSayisi;
		this.aksamVardiyaSaatSayisi = value;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_GUNCELLEME_TARIHI)
	public Date getGuncellemeTarihi() {
		return guncellemeTarihi;
	}

	public void setGuncellemeTarihi(Date guncellemeTarihi) {
		this.guncellemeTarihi = guncellemeTarihi;
	}

	@Transient
	public Object clone() {
		try {

			return super.clone();
		} catch (CloneNotSupportedException e) {
			// bu class cloneable oldugu icin buraya girilmemeli...
			throw new InternalError();
		}
	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}

}
