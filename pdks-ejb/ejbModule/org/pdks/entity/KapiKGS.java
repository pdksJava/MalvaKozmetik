package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;

@Entity(name = KapiKGS.TABLE_NAME)
@Immutable
public class KapiKGS implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1510062205167709853L;

	public static final String TABLE_NAME = "KAPI_KGS";
	public static final String COLUMN_NAME_ID = "ID";
	public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";
	private Long id;
	private String aciklamaKGS;
	private int kartYonu;
	private Long terminalNo;
	private Boolean durum = Boolean.FALSE, manuel = Boolean.FALSE;
	private Date islemZamani;
	private Kapi kapi;

	@Id
	@GeneratedValue
	@Column(name = COLUMN_NAME_ID)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = COLUMN_NAME_ACIKLAMA)
	public String getAciklamaKGS() {
		return aciklamaKGS;
	}

	public void setAciklamaKGS(String aciklamaKGS) {
		this.aciklamaKGS = aciklamaKGS;
	}

	@Column(name = "KART_YONU")
	public int getKartYonu() {
		return kartYonu;
	}

	public void setKartYonu(int kartYonu) {
		this.kartYonu = kartYonu;
	}

	@Column(name = "TERMINAL_NO")
	public Long getTerminalNo() {
		return terminalNo;
	}

	public void setTerminalNo(Long terminalNo) {
		this.terminalNo = terminalNo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ISLEM_ZAMAN")
	public Date getIslemZamani() {
		return islemZamani;
	}

	public void setIslemZamani(Date islemZamani) {
		this.islemZamani = islemZamani;
	}

	@Column(name = "DURUM")
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Column(name = "MANUEL")
	public Boolean getManuel() {
		return manuel;
	}

	public void setManuel(Boolean manuel) {
		this.manuel = manuel;
	}

	@OneToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "KAPI_ID", nullable = true, insertable = false, updatable = false)
	@Fetch(FetchMode.JOIN)
	public Kapi getKapi() {
		return kapi;
	}

	public void setKapi(Kapi kapi) {
		this.kapi = kapi;
	}

	@Transient
	public boolean isManuel() {
		return manuel != null && manuel;
	}

	@Transient
	public KapiView getKapiView() {
		KapiView kapiView = new KapiView();
		kapiView.setId(this.getId());
		kapiView.setKapiKGS(this);
		kapiView.setTerminalNo(this.getTerminalNo());
		kapiView.setKapiKGSAciklama(aciklamaKGS);
		kapiView.setKapiAciklama(kapi != null ? kapi.getAciklama() : "");
		kapiView.setKapi(kapi);
		return kapiView;

	}

}
