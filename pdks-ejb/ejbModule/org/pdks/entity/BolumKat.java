package org.pdks.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.session.PdksUtil;

@Entity(name = "BOLUM_KAT")
public class BolumKat extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9213795871677729380L;
	private Tanim bolum;
	private String kodu;
	private String aciklama;

	private Integer version = 0;

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "KAT_ID", nullable = false, unique = true)
	@Fetch(FetchMode.JOIN)
	public Tanim getBolum() {
		return bolum;
	}

	public void setBolum(Tanim bolum) {
		this.bolum = bolum;
	}

	@Column(name = "ACIKLAMA", length = 128)
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	@Column(name = "KODU", length = 12)
	public String getKodu() {
		return kodu;
	}

	public void setKodu(String kodu) {
		this.kodu = kodu;
	}

	@Transient
	public boolean isAciklamaVar() {
		return PdksUtil.hasStringValue(aciklama);
	}

	public boolean equals(BolumKat obj) {
		boolean eq = Boolean.FALSE;
		if (obj != null)
			eq = this.id != null && this.id.equals(obj.getId());
		else
			eq = this.id == null;
		return eq;

	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}
}
