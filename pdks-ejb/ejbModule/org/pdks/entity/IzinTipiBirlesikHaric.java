package org.pdks.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = "IZINTIPIBIRLESIKHARIC")
public class IzinTipiBirlesikHaric extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 56230242920057020L;

	private Tanim izinTipiTanim;
	private Tanim birlesikIzinTipiTanim;
	private double izinAralikSaat;

	private Integer version = 0;

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "BIRLESIK_IZIN_TIPI_ID", nullable = false)
	@Fetch(FetchMode.JOIN)
	public Tanim getBirlesikIzinTipiTanim() {
		return birlesikIzinTipiTanim;
	}

	public void setBirlesikIzinTipiTanim(Tanim birlesikIzinTipiTanim) {
		this.birlesikIzinTipiTanim = birlesikIzinTipiTanim;
	}

	@Column(name = "IZIN_ARALIK_SAAT")
	public double getIzinAralikSaat() {
		return izinAralikSaat;
	}

	public void setIzinAralikSaat(double izinAralikSaat) {
		this.izinAralikSaat = izinAralikSaat;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "IZIN_TIPI_ID", nullable = false)
	@Fetch(FetchMode.JOIN)
	public Tanim getIzinTipiTanim() {
		return izinTipiTanim;
	}

	public void setIzinTipiTanim(Tanim izinTipiTanim) {
		this.izinTipiTanim = izinTipiTanim;
	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}

}
