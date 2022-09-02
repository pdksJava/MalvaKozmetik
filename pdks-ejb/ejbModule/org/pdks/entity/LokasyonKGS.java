package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

@Entity(name = "LOKASYON_KGS")
@Immutable
public class LokasyonKGS implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7094265172231848399L;
	private Long id;
	private String aciklamaKGS = "";
	private Boolean durum = Boolean.FALSE;

	@Id
	@GeneratedValue
	@Column(name = "ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "ACIKLAMA")
	public String getAciklamaKGS() {
		return aciklamaKGS;
	}

	public void setAciklamaKGS(String aciklamaKGS) {
		this.aciklamaKGS = aciklamaKGS;
	}

	@Column(name = "DURUM")
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

}
