package org.pdks.entity;

import java.util.Date;
import java.util.HashMap;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.session.PdksUtil;

@Entity(name = "TATIL")
public class Tatil extends BaseObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8647887629561748025L;
	public static final String TATIL_TIPI_PERIYODIK = "P";
	public static final String TATIL_TIPI_TEK_SEFER = "D";

	private Boolean yarimGun = Boolean.FALSE, arifeSonraVardiyaDenklestirmeVar = Boolean.FALSE;
	private String ad, aciklama;
	private Date basTarih, bitTarih;
	private Tanim tatilTipi;
	private Integer version = 0;
	private Object basAy, basGun, bitAy, bitGun;
	private Tatil orjTatil;
	private Boolean arifeVardiyaYarimHesapla;
	private HashMap<Long, Vardiya> vardiyaMap;

	public Tatil() {
		super();
	}

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Column(name = "AD", nullable = false)
	public String getAd() {
		return ad;
	}

	public void setAd(String ad) {
		ad = PdksUtil.convertUTF8(ad);

		this.ad = ad;
	}

	@Column(name = "ACIKLAMA", nullable = false)
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		aciklama = PdksUtil.convertUTF8(aciklama);

		this.aciklama = aciklama;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = "BAS_TARIH", nullable = false)
	public Date getBasTarih() {
		return basTarih;
	}

	public void setBasTarih(Date basTarih) {
		this.basTarih = basTarih;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = "BIT_TARIH", nullable = false)
	public Date getBitTarih() {
		return bitTarih;
	}

	public void setBitTarih(Date bitTarih) {
		this.bitTarih = bitTarih;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "TATIL_TIPI_ID", nullable = false)
	@Fetch(FetchMode.JOIN)
	public Tanim getTatilTipi() {
		return tatilTipi;
	}

	public void setTatilTipi(Tanim tatilTipi) {
		this.tatilTipi = tatilTipi;
	}

	@Column(name = "YARIM_GUN", nullable = false)
	public Boolean getYarimGun() {
		return yarimGun;
	}

	public void setYarimGun(Boolean yarimGun) {
		this.yarimGun = yarimGun;
	}

	@Column(name = "YARIM_GUN_VARDIYA_YARIM_HESAPLA")
	public Boolean getArifeVardiyaYarimHesapla() {
		return arifeVardiyaYarimHesapla;
	}

	public void setArifeVardiyaYarimHesapla(Boolean arifeVardiyaYarimHesapla) {
		this.arifeVardiyaYarimHesapla = arifeVardiyaYarimHesapla;
	}

	@Transient
	public String getBasTarihAciklama() {
		return getTarihAciklama(basTarih);
	}

	@Transient
	public String getBitisTarihAciklama() {
		return getTarihAciklama(bitTarih);
	}

	@Transient
	public String getTarihAciklama(Date tarih) {
		String aciklama = null;
		if (tatilTipi != null && tarih != null) {
			String pattern = "d MMMMM";
			if (tatilTipi.getKodu().equals(TATIL_TIPI_TEK_SEFER))
				pattern = "d MMMMM yyyy";
			aciklama = PdksUtil.convertToDateString(tarih, pattern);
		}
		return aciklama;
	}

	@Transient
	public boolean isPeriyodik() {
		boolean durum = Boolean.FALSE;
		if (tatilTipi != null)
			durum = tatilTipi.getKodu().equals(TATIL_TIPI_PERIYODIK);

		return durum;
	}

	@Transient
	public boolean isTekSefer() {
		boolean durum = Boolean.FALSE;
		if (tatilTipi != null)
			durum = tatilTipi.getKodu().equals(TATIL_TIPI_TEK_SEFER);
		return durum;
	}

	@Transient
	public Object getBasAy() {
		return basAy;
	}

	public void setBasAy(Object basAy) {
		this.basAy = basAy;
	}

	@Transient
	public Object getBasGun() {
		return basGun;
	}

	public void setBasGun(Object basGun) {
		this.basGun = basGun;
	}

	@Transient
	public Object getBitAy() {
		return bitAy;
	}

	public void setBitAy(Object bitAy) {
		this.bitAy = bitAy;
	}

	@Transient
	public Object getBitGun() {
		return bitGun;
	}

	public void setBitGun(Object bitGun) {
		this.bitGun = bitGun;
	}

	@Transient
	public Tatil getOrjTatil() {
		return orjTatil;
	}

	public void setOrjTatil(Tatil orjTatil) {
		this.orjTatil = orjTatil;
	}

	@Transient
	public boolean isYarimGunMu() {
		return yarimGun != null && yarimGun.booleanValue();
	}

	@Transient
	public HashMap<Long, Vardiya> getVardiyaMap() {
		return vardiyaMap;
	}

	public void setVardiyaMap(HashMap<Long, Vardiya> vardiyaMap) {
		this.vardiyaMap = vardiyaMap;
	}

	@Transient
	public Boolean getArifeSonraVardiyaDenklestirmeVar() {
		return arifeSonraVardiyaDenklestirmeVar;
	}

	public void setArifeSonraVardiyaDenklestirmeVar(Boolean arifeSonraVardiyaDenklestirmeVar) {
		this.arifeSonraVardiyaDenklestirmeVar = arifeSonraVardiyaDenklestirmeVar;
	}



}
