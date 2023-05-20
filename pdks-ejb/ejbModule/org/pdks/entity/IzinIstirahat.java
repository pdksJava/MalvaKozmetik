package org.pdks.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.session.PdksUtil;

@Entity(name = "IZINISTIRAHAT")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "PERSONEL_IZIN_ID" }) })
public class IzinIstirahat extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2243793232101002019L;

	public static final String RAPOR_KAYNAK_KODU = "1";
	public static final String RAPOR_KAYNAK_KODU_DISI = "2";
	public static final String RAPOR_KAYNAK_KODU_SGK = "3";

	private Integer version = 0;

	private PersonelIzin personelIzin;

	private String teshis = "", aciklama = "", verenKurum = "", verenHekimAdi = "", raporKaynagi;

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public void aciklamaAta() {
		if (isHastaneDisi())
			verenKurum = aciklama;
		else
			verenHekimAdi = aciklama;

	}

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "PERSONEL_IZIN_ID", nullable = false)
	@Fetch(FetchMode.JOIN)
	public PersonelIzin getPersonelIzin() {
		return personelIzin;
	}

	public void setPersonelIzin(PersonelIzin personelIzin) {
		this.personelIzin = personelIzin;
	}

	@Column(name = "RAPOR_KAYNAGI", nullable = false, length = 1)
	public String getRaporKaynagi() {
		return raporKaynagi;
	}

	public void setRaporKaynagi(String raporKaynagi) {
		this.raporKaynagi = raporKaynagi;
	}

	@Column(name = "TESHIS")
	public String getTeshis() {
		return teshis;
	}

	public void setTeshis(String teshis) {
		this.teshis = teshis;
	}

	@Column(name = "ACIKLAMA")
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	@Transient
	public String getVerenKurum() {
		return verenKurum;
	}

	public void setVerenKurum(String verenKurum) {
		this.verenKurum = verenKurum;
	}

	@Transient
	public String getVerenHekimAdi() {
		return verenHekimAdi;
	}

	public void setVerenHekimAdi(String verenHekimAdi) {
		this.verenHekimAdi = verenHekimAdi;
	}

	@Transient
	public boolean isHastane() {
		return raporKaynagi != null && raporKaynagi.equals(RAPOR_KAYNAK_KODU);
	}

	@Transient
	public boolean isHastaneDisi() {
		return raporKaynagi != null && raporKaynagi.equals(RAPOR_KAYNAK_KODU_DISI);
	}

	@Transient
	public boolean isHastaneSGK() {
		return raporKaynagi != null && raporKaynagi.equals(RAPOR_KAYNAK_KODU_SGK);
	}

	@Transient
	public String getRaporKaynagiAciklama() {
		return "izin.etiket.raporKaynagi" + raporKaynagi;
	}

	@Transient
	public boolean isAciklamaVar() {
		return PdksUtil.hasStringValue(aciklama);
	}

	@Transient
	public Long getPersonelIzinId() {
		long personelIzinId = personelIzin != null ? personelIzin.getId() : 0;
		return personelIzinId;

	}

}
