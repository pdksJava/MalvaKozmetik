package org.pdks.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.pdks.session.PdksUtil;

@Entity(name = "YEMEKOGUN")
public class YemekOgun extends BaseObject {
	// seam-gen attributes (you should probably edit these)

	/**
	 * 
	 */
	private static final long serialVersionUID = -2645411245304870287L;
	private String yemekAciklama = "";
	private int baslangicSaat, baslangicDakika, bitisSaat, bitisDakika;
	private Date basTarih, bitTarih = PdksUtil.getSonSistemTarih();
	private Integer version = 0;

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Column(name = "ACIKLAMA")
	public String getYemekAciklama() {
		return yemekAciklama;
	}

	public void setYemekAciklama(String yemekAciklama) {
		this.yemekAciklama = yemekAciklama;
	}

	@Column(name = "BAS_SAAT")
	public int getBaslangicSaat() {
		return baslangicSaat;
	}

	public void setBaslangicSaat(int baslangicSaat) {
		this.baslangicSaat = baslangicSaat;
	}

	@Column(name = "BAS_DAKIKA")
	public int getBaslangicDakika() {
		return baslangicDakika;
	}

	public void setBaslangicDakika(int baslangicDakika) {
		this.baslangicDakika = baslangicDakika;
	}

	@Column(name = "BIT_SAAT")
	public int getBitisSaat() {
		return bitisSaat;
	}

	public void setBitisSaat(int bitisSaat) {
		this.bitisSaat = bitisSaat;
	}

	@Column(name = "BIT_DAKIKA")
	public int getBitisDakika() {
		return bitisDakika;
	}

	public void setBitisDakika(int bitisDakika) {
		this.bitisDakika = bitisDakika;
	}

	@Column(name = "BASLANGIC_TARIH")
	@Temporal(TemporalType.DATE)
	public Date getBasTarih() {
		return basTarih;
	}

	public void setBasTarih(Date basTarih) {
		this.basTarih = basTarih;
	}

	@Column(name = "BITIS_TARIH")
	@Temporal(TemporalType.DATE)
	public Date getBitTarih() {
		return bitTarih;
	}

	public void setBitTarih(Date bitTarih) {
		this.bitTarih = bitTarih;
	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}

}
