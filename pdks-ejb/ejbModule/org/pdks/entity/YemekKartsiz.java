package org.pdks.entity;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = "YEMEK_KARTSIZ")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "TARIH", "SIRKET_ID", "KAPI_ID", "OGUN_ID" }) })
public class YemekKartsiz extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8560853194023608673L;

	private YemekOgun yemekOgun;
	private Kapi yemekKapi;
	private Sirket sirket;
	private Date tarih;
	private Integer adet;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "OGUN_ID")
	@Fetch(FetchMode.JOIN)
	public YemekOgun getYemekOgun() {
		return yemekOgun;
	}

	public void setYemekOgun(YemekOgun yemekOgun) {
		this.yemekOgun = yemekOgun;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "KAPI_ID")
	@Fetch(FetchMode.JOIN)
	public Kapi getYemekKapi() {
		return yemekKapi;
	}

	public void setYemekKapi(Kapi yemekKapi) {
		this.yemekKapi = yemekKapi;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "SIRKET_ID")
	@Fetch(FetchMode.JOIN)
	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket sirket) {
		this.sirket = sirket;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "TARIH")
	public Date getTarih() {
		return tarih;
	}

	public void setTarih(Date tarih) {
		this.tarih = tarih;
	}

	@Column(name = "ADET")
	public Integer getAdet() {
		return adet;
	}

	public void setAdet(Integer adet) {
		this.adet = adet;
	}

	@Transient
	public HareketKGS getKgsHareket() {
		HareketKGS hareket = new HareketKGS();
		hareket.setId("KY" + id);
		hareket.setYemekOgun(yemekOgun);
		hareket.setZaman(tarih);
		hareket.setYemekYiyenSayisi(adet);
		hareket.setKapiView(yemekKapi.getKapiNewView());
		return hareket;
	}

}
