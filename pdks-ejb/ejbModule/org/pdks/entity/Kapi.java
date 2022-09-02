package org.pdks.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.pdks.session.PdksUtil;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = Kapi.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { Kapi.COLUMN_NAME_KGS_ID }) })
public class Kapi extends BaseObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9165800693227215645L;
	public static final String TABLE_NAME = "KAPI";
	public static final String COLUMN_NAME_KGS_ID = "KGS_ID";
	public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";
	public static final String COLUMN_NAME_KAPI_TIPI = "KAPI_TIPI_ID";

	// seam-gen attributes (you should probably edit these)
	public static final String TIPI_KODU_GIRIS = "G";
	public static final String TIPI_KODU_CIKIS = "C";
	public static final String TIPI_KODU_YEMEKHANE = "Y";
	private String aciklama = "";
	private KapiKGS kapiKGS;
	private Tanim tipi;

	private Boolean pdks = Boolean.TRUE;
	private KapiView kapiView;
	private Integer version = 0;

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@OneToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_KGS_ID, nullable = false)
	@Fetch(FetchMode.JOIN)
	public KapiKGS getKapiKGS() {
		return kapiKGS;
	}

	public void setKapiKGS(KapiKGS value) {
		if (value != null)
			this.kapiView = value.getKapiView();
		this.kapiKGS = value;
	}

	@Column(name = COLUMN_NAME_ACIKLAMA)
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String pdksAciklama) {
		pdksAciklama = PdksUtil.convertUTF8(pdksAciklama);
		this.aciklama = pdksAciklama;
	}

	@Column(name = "PDKS")
	public Boolean getPdks() {
		return pdks;
	}

	public void setPdks(Boolean pdks) {
		this.pdks = pdks;
	}

	@Transient
	public KapiView getKapiView() {
		return kapiView;
	}

	public void setKapiView(KapiView kapiView) {
		this.kapiView = kapiView;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_KAPI_TIPI, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Tanim getTipi() {
		return tipi;
	}

	public void setTipi(Tanim tipi) {
		this.tipi = tipi;
	}

	@Transient
	public boolean isGirisKapi() {
		return tipi != null && tipi.getKodu().equals(TIPI_KODU_GIRIS);
	}

	@Transient
	public boolean isCikisKapi() {
		return tipi != null && tipi.getKodu().equals(TIPI_KODU_CIKIS);
	}

	@Transient
	public boolean isYemekHaneKapi() {
		return tipi != null && tipi.getKodu().equals(TIPI_KODU_YEMEKHANE);
	}

	@Transient
	public KapiView getKapiNewView() {
		KapiView kapiView = new KapiView();
		kapiView.setId(kapiKGS != null ? kapiKGS.getId() : 0);
		kapiView.setKapiKGS(kapiKGS);
		kapiView.setKapiKGSAciklama(kapiKGS != null ? kapiKGS.getAciklamaKGS() : null);
		kapiView.setKapi(this);
		kapiView.setKapiAciklama(this.getAciklama());
		return kapiView;

	}

}
