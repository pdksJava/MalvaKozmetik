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

@Entity(name = KapiView.VIEW_NAME)
@Immutable
public class KapiView implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3434010518290512625L;
	protected static final String VIEW_NAME = "VIEW_KAPI";
	public static int KAPI_TANIMLI = 1;
	public static int KAPI_TANIMSIZ = 2;
	private Long id, terminalNo;

	private Kapi kapi;
	private KapiKGS kapiKGS;
	private KapiView terminal;

	private String kapiKGSAciklama, kapiAciklama;
	private boolean durum;

	@Id
	@Column(name = "ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "TERMINAL_NO")
	public Long getTerminalNo() {
		return terminalNo;
	}

	public void setTerminalNo(Long terminalNo) {
		this.terminalNo = terminalNo;
	}

	@OneToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "KAPI", nullable = true)
	@Fetch(FetchMode.JOIN)
	public Kapi getKapi() {
		return kapi;
	}

	public void setKapi(Kapi kapi) {
		this.kapi = kapi;
	}

	@Column(name = "ACIKLAMA_KGS")
	public String getKapiKGSAciklama() {
		return kapiKGSAciklama;
	}

	public void setKapiKGSAciklama(String kapiKGSAciklama) {
		this.kapiKGSAciklama = kapiKGSAciklama;
	}

	@Column(name = "ACIKLAMA")
	public String getKapiAciklama() {
		return kapiAciklama;
	}

	public void setKapiAciklama(String kapiAciklama) {
		this.kapiAciklama = kapiAciklama;
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
	public KapiKGS getKapiKGS() {
		return kapiKGS;
	}

	public void setKapiKGS(KapiKGS kapiKGS) {
		this.kapiKGS = kapiKGS;
	}

	@Transient
	public String getAciklama() {
		String aciklama = kapiKGSAciklama;
		if (kapi != null && kapi.getId() != null)
			aciklama = kapi.getAciklama();
		else if (kapiKGS != null && kapiKGS.getId() != null)
			aciklama = kapiKGS.getAciklamaKGS();
		return aciklama;
	}

	@Transient
	public KapiView getTerminal() {
		return terminal;
	}

	public void setTerminal(KapiView terminal) {
		this.terminal = terminal;
	}

}
