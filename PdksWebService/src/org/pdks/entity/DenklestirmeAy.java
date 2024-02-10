package org.pdks.entity;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.pdks.genel.model.PdksUtil;

@Entity(name = DenklestirmeAy.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { DenklestirmeAy.COLUMN_NAME_YIL, DenklestirmeAy.COLUMN_NAME_AY }) })
public class DenklestirmeAy extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4284922207901564192L;
	public static final String TABLE_NAME = "DENKLESTIRMEAY";
	public static final String COLUMN_NAME_YIL = "YIL";
	public static final String COLUMN_NAME_AY = "AY";
	public static final String COLUMN_NAME_IK_OTOMATIK_ONAY_TARIHI = "IK_OTOMATIK_ONAY_TARIHI";
	private int ay, yil;
	private double sure = 0, toplamIzinSure = 0;
	private String ayAdi = "";
	protected Boolean guncelleIK = Boolean.FALSE;

	@Column(name = COLUMN_NAME_AY, nullable = false)
	public int getAy() {
		return ay;
	}

	public void setAy(int ay) {
		this.ay = ay;
	}

	@Column(name = COLUMN_NAME_YIL, nullable = false)
	public int getYil() {
		return yil;
	}

	public void setYil(int yil) {
		this.yil = yil;
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

	@Column(name = "GUNCELLE_IK")
	public Boolean getGuncelleIK() {
		return guncelleIK;
	}

	public void setGuncelleIK(Boolean guncelleIK) {
		this.guncelleIK = guncelleIK;
	}

	@Transient
	public String getAyAdi() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, ay - 1);
		cal.set(Calendar.YEAR, yil);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		ayAdi = PdksUtil.convertToDateString(cal.getTime(), "MMMM");
		return ayAdi;
	}

	public void setAyAdi(String ayAdi) {
		this.ayAdi = ayAdi;
	}

	@Transient
	public boolean isDurumu() {
		boolean statu = durum;
		// if (yil == 2016 && ay == 2)
		// statu = true;
		return statu;
	}

}
