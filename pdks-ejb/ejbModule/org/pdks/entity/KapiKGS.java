package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;

@Entity(name = KapiKGS.TABLE_NAME)
@Immutable
public class KapiKGS extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1510062205167709853L;

	// public static final String TABLE_NAME = "KAPI_KGS";
	public static final String TABLE_NAME = "KAPI_SIRKET_KGS";
	public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";
	public static final String COLUMN_NAME_KGS_ID = "KGS_ID";
	public static final String COLUMN_NAME_KGS_SIRKET = "KGS_SIRKET_ID";
	public static final String COLUMN_NAME_TERMINAL_NO = "TERMINAL_NO";
	public static final String COLUMN_NAME_KART_YONU = "KART_YONU";
	public static final String COLUMN_NAME_MANUEL = "MANUEL";
	public static final String COLUMN_NAME_KAPI_DEGISTIR = "KAPI_DEGISTIR";
	public static final String COLUMN_NAME_KAPI = "KAPI_ID";
	public static final String COLUMN_NAME_DURUM = "DURUM";
	public static final String COLUMN_NAME_ISLEM_ZAMAN = "ISLEM_ZAMAN";

	private Long kgsId;
	private KapiSirket kapiSirket;
	private String aciklamaKGS;
	private int kartYonu;
	private Long terminalNo;
	private Boolean durum = Boolean.FALSE, manuel = Boolean.FALSE, kapiDegistir = Boolean.FALSE;
	private Date islemZamani;
	private Kapi kapi;
	private KapiKGS bagliKapiKGS;

	@Column(name = COLUMN_NAME_KGS_ID)
	public Long getKgsId() {
		return kgsId;
	}

	public void setKgsId(Long kgsId) {
		this.kgsId = kgsId;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = COLUMN_NAME_KGS_SIRKET, nullable = true)
	@Fetch(FetchMode.JOIN)
	public KapiSirket getKapiSirket() {
		return kapiSirket;
	}

	public void setKapiSirket(KapiSirket kapiSirket) {
		this.kapiSirket = kapiSirket;
	}

	@Column(name = COLUMN_NAME_ACIKLAMA)
	public String getAciklamaKGS() {
		return aciklamaKGS;
	}

	public void setAciklamaKGS(String aciklamaKGS) {
		this.aciklamaKGS = aciklamaKGS;
	}

	@Column(name = COLUMN_NAME_KART_YONU)
	public int getKartYonu() {
		return kartYonu;
	}

	public void setKartYonu(int kartYonu) {
		this.kartYonu = kartYonu;
	}

	@Column(name = COLUMN_NAME_TERMINAL_NO)
	public Long getTerminalNo() {
		return terminalNo;
	}

	public void setTerminalNo(Long terminalNo) {
		this.terminalNo = terminalNo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_ISLEM_ZAMAN)
	public Date getIslemZamani() {
		return islemZamani;
	}

	public void setIslemZamani(Date islemZamani) {
		this.islemZamani = islemZamani;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Column(name = COLUMN_NAME_MANUEL)
	public Boolean getManuel() {
		return manuel;
	}

	public void setManuel(Boolean manuel) {
		this.manuel = manuel;
	}

	@Column(name = COLUMN_NAME_KAPI_DEGISTIR)
	public Boolean getKapiDegistir() {
		return kapiDegistir;
	}

	public void setKapiDegistir(Boolean kapiDegistir) {
		this.kapiDegistir = kapiDegistir;
	}

	@OneToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_KAPI, nullable = true, insertable = false, updatable = false)
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
	public boolean isKapiDegistirir() {
		return kapiDegistir != null && kapiDegistir;
	}

	@Transient
	public boolean isPdksManuel() {
		boolean pdksManuel = false;
		if (manuel != null && manuel)
			pdksManuel = kapiSirket == null || kapiSirket.getId() <= 0L;
		return pdksManuel;
	}

	@Transient
	public KapiKGS getBagliKapiKGS() {
		return bagliKapiKGS;
	}

	public void setBagliKapiKGS(KapiKGS bagliKapiKGS) {
		this.bagliKapiKGS = bagliKapiKGS;
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
