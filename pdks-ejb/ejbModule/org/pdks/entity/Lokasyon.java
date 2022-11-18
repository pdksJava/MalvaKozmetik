package org.pdks.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.pdks.session.PdksUtil;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = "LOKASYON")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "KGS_ID" }) })
public class Lokasyon extends BaseObject {
	// seam-gen attributes (you should probably edit these)

	/**
	 * 
	 */
	private static final long serialVersionUID = 6083433208272956254L;
	private String aciklama = "";
	private LokasyonKGS lokasyonKGS;

	private LokasyonView lokasyonView;
	private Integer version = 0;

	public Lokasyon() {
		super();

	}

	public Lokasyon(Long id) {
		super();
		this.id = id;
	}

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@OneToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "KGS_ID", nullable = false, unique = true)
	@Fetch(FetchMode.JOIN)
	public LokasyonKGS getLokasyonKGS() {
		return lokasyonKGS;
	}

	public void setLokasyonKGS(LokasyonKGS value) {
		this.lokasyonKGS = value;
	}

	@Column(name = "ACIKLAMA", nullable = false)
	public String getAciklama() {
		return aciklama;
	}

	@Transient
	public boolean isAciklamaVar() {
		return PdksUtil.hasStringValue(aciklama);
	}

	public void setAciklama(String value) {
		value = PdksUtil.convertUTF8(value);

		this.aciklama = value;
	}

	@Transient
	public LokasyonView getLokasyonView() {
		return lokasyonView;
	}

	public void setLokasyonView(LokasyonView lokasyonView) {
		this.lokasyonView = lokasyonView;
	}

}
