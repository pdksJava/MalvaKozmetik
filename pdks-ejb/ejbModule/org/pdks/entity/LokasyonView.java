package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

@Entity(name = "VIEW_LOKASYON")
@Immutable
public class LokasyonView implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 575273312119064006L;
	public static int LOKASYON_TANIMLI = 1;
	public static int LOKASYON_TANIMSIZ = 2;
	private Long id;
	private Lokasyon lokasyon;
	private String aciklamaKGS, aciklamaPDKS;
	private LokasyonKGS lokasyonKGS;
	boolean durum;

	@Id
	@Column(name = "ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

//	@OneToOne(cascade = CascadeType.REFRESH)
//	@JoinColumn(name = "LOKASYON_ID", nullable = true)
//	@Fetch(FetchMode.JOIN)
//	@Type(type = "int")
	@Transient
	public Lokasyon getLokasyon() {
		return lokasyon;
	}

	public void setLokasyon(Lokasyon pdksLokasyon) {
		this.lokasyon = pdksLokasyon;
	}

	@Column(name = "ACIKLAMA")
	public String getAciklamaPDKS() {
		return aciklamaPDKS;
	}

	public void setAciklamaPDKS(String aciklamaPDKS) {
		this.aciklamaPDKS = aciklamaPDKS;
	}

	@Column(name = "ACIKLAMA_KGS")
	public String getAciklamaKGS() {
		return aciklamaKGS;
	}

	public void setAciklamaKGS(String aciklamaKGS) {
		this.aciklamaKGS = aciklamaKGS;
	}

	@Column(name = "DURUM")
	public boolean isDurum() {
		return durum;
	}

	public void setDurum(boolean durum) {
		this.durum = durum;
	}

	@OneToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "ID", nullable = false)
	@Fetch(FetchMode.JOIN)
	@Type(type = "int")
	public LokasyonKGS getLokasyonKGS() {
		return lokasyonKGS;
	}

	public void setLokasyonKGS(LokasyonKGS value) {
		this.lokasyonKGS = value;
	}

	@Transient
	public String getAciklama() {
		String aciklama = aciklamaKGS;
		if (lokasyon != null && lokasyon.getId() != null)
			aciklama = lokasyon.getAciklama();
		else if (lokasyonKGS != null && lokasyonKGS.getId() != null)
			aciklama = lokasyonKGS.getAciklamaKGS();
		return aciklama;
	}
}
