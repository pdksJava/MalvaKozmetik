package org.pdks.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.pdks.session.PdksUtil;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = Sirket.TABLE_NAME)
public class Sirket extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7429465630343438835L;

	public static final String TABLE_NAME = "SIRKET";
	public static final String COLUMN_NAME_DEPARTMAN = "DEPARTMAN_ID";
	public static final String COLUMN_NAME_PDKS = "PDKS_DURUM";
	public static final String COLUMN_NAME_FAZLA_MESAI = "FAZLA_MESAI";
	public static final String COLUMN_NAME_ISTEN_AYR_TAR_CALISIYOR = "ISTEN_AYR_TAR_CALISIYOR";
	public static final String COLUMN_NAME_DEPARTMAN_BOLUM_AYNI = "DEPARTMAN_BOLUM_AYNI";
	public static final String COLUMN_NAME_FAZLA_MESAI_IZIN_KULLAN = "FAZLA_MESAI_IZIN_KULLAN";
	public static final String COLUMN_NAME_FAZLA_MESAI_TALEP_GIRILEBILIR = "FAZLA_MESAI_TALEP_GIRILEBILIR";
	public static final String COLUMN_NAME_AD = "AD";
	public static final String SIRKET_ERP_KODU = "3030";
	private String ad, aciklama, erpKodu = "", lpdapOnEk = "";
	private Boolean erpDurum = Boolean.FALSE, ldapDurum = Boolean.FALSE, pdks = Boolean.FALSE, suaOlabilir = Boolean.FALSE;
	private Boolean fazlaMesaiOde = Boolean.FALSE, fazlaMesai = Boolean.FALSE, istenAyrilmaTarihindeCalisiyor = Boolean.FALSE;
	private Boolean fazlaMesaiIzinKullan = Boolean.FALSE, departmanBolumAyni = Boolean.FALSE, fazlaMesaiTalepGirilebilir = Boolean.FALSE;

	private Departman departman;
	private Integer version = 0;

	public Sirket() {
		super();

	}

	public Sirket(Long id) {
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

	@Column(name = COLUMN_NAME_AD, nullable = false)
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

	@Column(name = "ERP_KODU")
	public String getErpKodu() {
		return erpKodu;
	}

	public void setErpKodu(String erpKodu) {
		this.erpKodu = erpKodu;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = COLUMN_NAME_DEPARTMAN, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman departman) {
		this.departman = departman;
	}

	@Column(name = "LDAP_ON_EK")
	public String getLpdapOnEk() {
		return lpdapOnEk;
	}

	public void setLpdapOnEk(String lpdapOnEk) {
		this.lpdapOnEk = lpdapOnEk;
	}

	@Column(name = "ERP_DURUM", nullable = false)
	public Boolean getErpDurum() {
		return erpDurum;
	}

	public void setErpDurum(Boolean erpDurum) {
		this.erpDurum = erpDurum;
	}

	@Column(name = "LDAP_DURUM", nullable = false)
	public Boolean getLdapDurum() {
		return ldapDurum;
	}

	public void setLdapDurum(Boolean ldapDurum) {
		this.ldapDurum = ldapDurum;
	}

	@Column(name = COLUMN_NAME_PDKS)
	public Boolean getPdks() {
		return pdks;
	}

	public void setPdks(Boolean pdks) {
		this.pdks = pdks;
	}

	@Column(name = Personel.COLUMN_NAME_SUA_OLABILIR)
	public Boolean getSuaOlabilir() {
		return suaOlabilir;
	}

	public void setSuaOlabilir(Boolean suaOlabilir) {
		this.suaOlabilir = suaOlabilir;
	}

	@Column(name = Personel.COLUMN_NAME_FAZLA_MESAI_ODE)
	public Boolean getFazlaMesaiOde() {
		return fazlaMesaiOde;
	}

	public void setFazlaMesaiOde(Boolean fazlaMesaiOde) {
		this.fazlaMesaiOde = fazlaMesaiOde;
	}

	@Column(name = COLUMN_NAME_FAZLA_MESAI)
	public Boolean getFazlaMesai() {
		return fazlaMesai;
	}

	public void setFazlaMesai(Boolean fazlaMesai) {
		this.fazlaMesai = fazlaMesai;
	}

	@Column(name = COLUMN_NAME_FAZLA_MESAI_TALEP_GIRILEBILIR)
	public Boolean getFazlaMesaiTalepGirilebilir() {
		return fazlaMesaiTalepGirilebilir;
	}

	public void setFazlaMesaiTalepGirilebilir(Boolean fazlaMesaiTalepGirilebilir) {
		this.fazlaMesaiTalepGirilebilir = fazlaMesaiTalepGirilebilir;
	}

	@Column(name = COLUMN_NAME_ISTEN_AYR_TAR_CALISIYOR)
	public Boolean getIstenAyrilmaTarihindeCalisiyor() {
		return istenAyrilmaTarihindeCalisiyor;
	}

	public void setIstenAyrilmaTarihindeCalisiyor(Boolean istenAyrilmaTarihindeCalisiyor) {
		this.istenAyrilmaTarihindeCalisiyor = istenAyrilmaTarihindeCalisiyor;
	}

	@Column(name = COLUMN_NAME_FAZLA_MESAI_IZIN_KULLAN)
	public Boolean getFazlaMesaiIzinKullan() {
		return fazlaMesaiIzinKullan;
	}

	public void setFazlaMesaiIzinKullan(Boolean fazlaMesaiIzinKullan) {
		this.fazlaMesaiIzinKullan = fazlaMesaiIzinKullan;
	}

	@Column(name = COLUMN_NAME_DEPARTMAN_BOLUM_AYNI)
	public Boolean getDepartmanBolumAyni() {
		return departmanBolumAyni;
	}

	public void setDepartmanBolumAyni(Boolean departmanBolumAyni) {
		this.departmanBolumAyni = departmanBolumAyni;
	}

	@Transient
	public boolean isLdap() {
		return ldapDurum != null ? ldapDurum.booleanValue() : false;
	}

	@Transient
	public boolean isDepartmanBolumAynisi() {
		return departmanBolumAyni != null ? departmanBolumAyni.booleanValue() : false;
	}

	@Transient
	public boolean isPdksMi() {
		return pdks != null ? pdks.booleanValue() : false;
	}

	@Transient
	public boolean isErp() {
		return erpDurum != null ? erpDurum.booleanValue() : false;
	}

	@Transient
	public boolean isFazlaMesaiTalepGirer() {
		return fazlaMesaiTalepGirilebilir != null && fazlaMesaiTalepGirilebilir.booleanValue();
	}
}
