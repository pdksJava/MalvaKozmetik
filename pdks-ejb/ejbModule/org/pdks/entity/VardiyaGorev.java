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

@Entity(name = VardiyaGorev.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { VardiyaGorev.COLUMN_NAME_VARDIYA_GUN }) })
public class VardiyaGorev extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4231082516800847788L;

	public static final String TABLE_NAME = "VARDIYA_GOREV";
	public static final String COLUMN_NAME_VARDIYA_GUN = "VARDIYA_GUN_ID";
 	public static final int OZEL_SUT_IZNI = 5;
	public static final int OZEL_ISTIFA = 4;
	public static final int OZEL_RAPOR_IZNI = 3;
	public static final int OZEL_ISTEK_EGITIM = 2;
	public static final int OZEL_ISTEK_PERSONEL = 1;
	public static final int OZEL_ISTEK_YOK = 0;

	private VardiyaGun vardiyaGun;
	private Tanim yeniGorevYeri;
	private BolumKat bolumKat;
	private Integer ozelIstekDurum = OZEL_ISTEK_YOK;
	private Boolean gorevliDurum = Boolean.FALSE;

	private Integer version = 0;

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_VARDIYA_GUN, nullable = false)
	@Fetch(FetchMode.JOIN)
	public VardiyaGun getVardiyaGun() {
		return vardiyaGun;
	}

	public void setVardiyaGun(VardiyaGun value) {
		this.vardiyaGun = value;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "BOLUM_KAT_ID")
	@Fetch(FetchMode.JOIN)
	public BolumKat getBolumKat() {
		return bolumKat;
	}

	public void setBolumKat(BolumKat value) {
		this.bolumKat = value;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "YENI_GOREV_YERI_ID")
	@Fetch(FetchMode.JOIN)
	public Tanim getYeniGorevYeri() {
		return yeniGorevYeri;
	}

	public void setYeniGorevYeri(Tanim yeniGorevYeri) {
		this.yeniGorevYeri = yeniGorevYeri;
	}

	@Column(name = "GOREVLI_DURUM")
	public Boolean getGorevliDurum() {
		return gorevliDurum;
	}

	public void setGorevliDurum(Boolean gorevliDurum) {
		this.gorevliDurum = gorevliDurum;
	}

	@Transient
	public Long getPdksVardiyaGunId() {
		return vardiyaGun != null ? vardiyaGun.getId() : 0;

	}

	@Column(name = "OZEL_ISTEK")
	public Integer getOzelIstekDurum() {
		return ozelIstekDurum;
	}

	public void setOzelIstekDurum(Integer ozelIstekDurum) {
		this.ozelIstekDurum = ozelIstekDurum;
	}

	@Transient
	public boolean isOzelIstek() {
		boolean durumu = ozelIstekDurum != null && ozelIstekDurum.intValue() == OZEL_ISTEK_PERSONEL;
		return durumu;
	}

	@Transient
	public boolean isIstifa() {
		boolean durumu = ozelIstekDurum != null && ozelIstekDurum.intValue() == OZEL_ISTIFA;
		return durumu;
	}

	@Transient
	public boolean isEgitim() {
		boolean durumu = ozelIstekDurum != null && ozelIstekDurum.intValue() == OZEL_ISTEK_EGITIM;
		return durumu;
	}

	@Transient
	public boolean isRaporIzni() {
		boolean durumu = ozelIstekDurum != null && ozelIstekDurum.intValue() == OZEL_RAPOR_IZNI;
		return durumu;
	}

	@Transient
	public boolean isSutIzni() {
		boolean durumu = ozelIstekDurum != null && ozelIstekDurum.intValue() == OZEL_SUT_IZNI;
		return durumu;
	}

	@Transient
	public boolean isOzelDurumYok() {
		boolean durumu = ozelIstekDurum == null || ozelIstekDurum.intValue() == OZEL_ISTEK_YOK;
		return durumu;
	}

	@Transient
	public boolean isShiftGorevli() {
		boolean durumu = gorevliDurum != null && gorevliDurum.booleanValue();
		return durumu;
	}

}
