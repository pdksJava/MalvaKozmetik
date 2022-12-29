package org.pdks.entity;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = ArifeVardiyaDonem.TABLE_NAME)
public class ArifeVardiyaDonem extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6660759935925852587L;
	public static final String TABLE_NAME = "ARIFE_VARDIYA_DONEM";
	public static final String COLUMN_NAME_VARDIYA = "VARDIYA_ID";
	public static final String COLUMN_NAME_BAS_TARIHI = "BAS_TARIHI";
	public static final String COLUMN_NAME_BIT_TARIHI = "BIT_TARIHI";
	public static final String COLUMN_NAME_TATIL_BAS_TIME = "TATIL_BAS_TIME";
	public static final String COLUMN_NAME_ARIFE_VARDIYA_HESAPLA = "ARIFE_VARDIYA_HESAPLA";
	public static final String COLUMN_NAME_ARIFE_SONRA_CAL_VAR_DENK_YOK = "ARIFE_SONRA_CAL_VAR_DENK_YOK";
	public static final String COLUMN_NAME_VARDIYA_ACIKLAMA = "VARDIYA_ACIKLAMA";

	private Date basTarih, bitTarih;
	private String tatilBasZaman, vardiyaKisaAciklama;
	private Boolean arifeVardiyaHesapla = Boolean.FALSE;
	private Boolean arifeSonraVardiyaDenklestirmeVar = Boolean.FALSE;
	private Vardiya vardiya;
	private Integer version = 0;

	public ArifeVardiyaDonem() {
		super();
	}

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@OneToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_VARDIYA)
	@Fetch(FetchMode.JOIN)
	public Vardiya getVardiya() {
		return vardiya;
	}

	public void setVardiya(Vardiya vardiya) {
		this.vardiya = vardiya;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_BAS_TARIHI)
	public Date getBasTarih() {
		return basTarih;
	}

	public void setBasTarih(Date basTarih) {
		this.basTarih = basTarih;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_BIT_TARIHI)
	public Date getBitTarih() {
		return bitTarih;
	}

	public void setBitTarih(Date bitTarih) {
		this.bitTarih = bitTarih;
	}

	// @Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_TATIL_BAS_TIME)
	public String getTatilBasZaman() {
		return tatilBasZaman;
	}

	public void setTatilBasZaman(String tatilBasZaman) {
		this.tatilBasZaman = tatilBasZaman;
	}

	@Column(name = COLUMN_NAME_ARIFE_VARDIYA_HESAPLA)
	public Boolean getArifeVardiyaHesapla() {
		return arifeVardiyaHesapla;
	}

	public void setArifeVardiyaHesapla(Boolean arifeVardiyaHesapla) {
		this.arifeVardiyaHesapla = arifeVardiyaHesapla;
	}

	@Column(name = COLUMN_NAME_ARIFE_SONRA_CAL_VAR_DENK_YOK)
	public Boolean getArifeSonraVardiyaDenklestirmeVar() {
		return arifeSonraVardiyaDenklestirmeVar;
	}

	public void setArifeSonraVardiyaDenklestirmeVar(Boolean arifeSonraVardiyaDenklestirmeVar) {
		this.arifeSonraVardiyaDenklestirmeVar = arifeSonraVardiyaDenklestirmeVar;
	}

	@Column(name = COLUMN_NAME_VARDIYA_ACIKLAMA, insertable = false, updatable = false)
	public String getVardiyaKisaAciklama() {
		return vardiyaKisaAciklama;
	}

	public void setVardiyaKisaAciklama(String vardiyaKisaAciklama) {
		this.vardiyaKisaAciklama = vardiyaKisaAciklama;
	}
}
