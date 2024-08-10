package org.pdks.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.Max;
import org.hibernate.validator.Min;

@Entity(name = Departman.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { Departman.COLUMN_NAME_DEPARTMAN }) })
public class Departman extends BaseObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4316788642188710164L;

	public static final String TABLE_NAME = "DEPARTMAN";

	public static final String COLUMN_NAME_DEPARTMAN = "DEPARTMAN_ID";

	public static final String COLUMN_NAME_ADMIN_DURUM = "ADMIN_DURUM";

	public static final String COLUMN_NAME_FAZLA_MESAI_TALEP_GIRILEBILIR = "FAZLA_MESAI_TALEP_GIRILEBILIR";
	public static final String COLUMN_NAME_SIRKET_EKLENEBILIR = "SIRKET_EKLENEBILIR";
	public static final String COLUMN_NAME_IZIN_GIRILEBILIR = "IZIN_GIRILEBILIR";
	public static final String COLUMN_NAME_HAFTA_ICI_SUT_IZNI_SURE = "HAFTA_ICI_SUT_IZNI_SURE";
	public static final String COLUMN_NAME_CUMARTESI_SUT_IZNI_SURE = "CUMARTESI_SUT_IZNI_SURE";
	public static final String COLUMN_NAME_PAZAR_SUT_IZNI_SURE = "PAZAR_SUT_IZNI_SURE";

	private Integer version = 0;

	private Tanim departmanTanim;

	private Integer cocukYasUstSiniri;

	private Integer yasliYasAltSiniri;

	private Boolean icapciOlabilir, admin, suaOlabilir = Boolean.FALSE, sirketEklenebilir = Boolean.FALSE, izinGirilebilir = Boolean.FALSE, fazlaMesaiTalepGirilebilir = Boolean.FALSE, fazlaMesaiOde = Boolean.FALSE;

	private Double haftaIciSutIzniSure = 7.5d, cumartesiSutIzniSure = 0.0d, pazarSutIzniSure = 0.0d;

	private String mailBox;

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@OneToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_DEPARTMAN, nullable = false, unique = true)
	@Fetch(FetchMode.JOIN)
	public Tanim getDepartmanTanim() {
		return departmanTanim;
	}

	public void setDepartmanTanim(Tanim departmanTanim) {
		this.departmanTanim = departmanTanim;
	}

	@Column(name = "MAIL_BOX")
	public String getMailBox() {
		return mailBox;
	}

	public void setMailBox(String mailBox) {
		this.mailBox = mailBox;
	}

	@Column(name = "ICAP_OLABILIR", nullable = false)
	public Boolean getIcapciOlabilir() {
		return icapciOlabilir;
	}

	public void setIcapciOlabilir(Boolean icapciOlabilir) {
		this.icapciOlabilir = icapciOlabilir;
	}

	@Column(name = COLUMN_NAME_ADMIN_DURUM, nullable = false)
	public Boolean getAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}

	@Transient
	public boolean isAdminMi() {
		return admin != null ? admin.booleanValue() : false;
	}

	@Min(value = 5, message = "Çoçuk üst yaş sınırı hatalı")
	@Column(name = "COCUK_YAS_UST_SINIRI")
	public Integer getCocukYasUstSiniri() {
		return cocukYasUstSiniri;
	}

	public void setCocukYasUstSiniri(Integer cocukYasUstSiniri) {
		this.cocukYasUstSiniri = cocukYasUstSiniri;
	}

	@Max(value = 99, message = "Yaşlı alt yaş sınırı hatalı")
	@Column(name = "YASLI_YAS_ALT_SINIRI")
	public Integer getYasliYasAltSiniri() {
		return yasliYasAltSiniri;
	}

	public void setYasliYasAltSiniri(Integer yasliYasAltSiniri) {
		this.yasliYasAltSiniri = yasliYasAltSiniri;
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

	@Column(name = "IZIN_GIRILEBILIR")
	public Boolean getIzinGirilebilir() {
		return izinGirilebilir;
	}

	public void setIzinGirilebilir(Boolean value) {
		this.izinGirilebilir = value;
	}

	@Column(name = COLUMN_NAME_FAZLA_MESAI_TALEP_GIRILEBILIR)
	public Boolean getFazlaMesaiTalepGirilebilir() {
		return fazlaMesaiTalepGirilebilir;
	}

	public void setFazlaMesaiTalepGirilebilir(Boolean fazlaMesaiTalepGirilebilir) {
		this.fazlaMesaiTalepGirilebilir = fazlaMesaiTalepGirilebilir;
	}

	@Column(name = COLUMN_NAME_SIRKET_EKLENEBILIR)
	public Boolean getSirketEklenebilir() {
		return sirketEklenebilir;
	}

	public void setSirketEklenebilir(Boolean sirketEklenebilir) {
		this.sirketEklenebilir = sirketEklenebilir;
	}

	@Column(name = COLUMN_NAME_HAFTA_ICI_SUT_IZNI_SURE)
	public Double getHaftaIciSutIzniSure() {
		return haftaIciSutIzniSure;
	}

	public void setHaftaIciSutIzniSure(Double haftaIciSutIzniSure) {
		this.haftaIciSutIzniSure = haftaIciSutIzniSure;
	}

	@Column(name = COLUMN_NAME_CUMARTESI_SUT_IZNI_SURE)
	public Double getCumartesiSutIzniSure() {
		return cumartesiSutIzniSure;
	}

	public void setCumartesiSutIzniSure(Double cumartesiSutIzniSure) {
		this.cumartesiSutIzniSure = cumartesiSutIzniSure;
	}

	@Column(name = COLUMN_NAME_PAZAR_SUT_IZNI_SURE)
	public Double getPazarSutIzniSure() {
		return pazarSutIzniSure;
	}

	public void setPazarSutIzniSure(Double pazarSutIzniSure) {
		this.pazarSutIzniSure = pazarSutIzniSure;
	}

	@Transient
	public String getAciklama() {
		return departmanTanim != null ? departmanTanim.getAciklama() : "";
	}

	@Transient
	public boolean isFazlaMesaiTalepGirer() {
		return fazlaMesaiTalepGirilebilir != null && fazlaMesaiTalepGirilebilir.booleanValue();
	}

	public void entityRefresh() {
		// TODO entityRefresh

	}

}
