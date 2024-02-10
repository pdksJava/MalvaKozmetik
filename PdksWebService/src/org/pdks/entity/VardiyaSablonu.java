package org.pdks.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import org.pdks.genel.model.PdksUtil;

@Entity(name = "VARDIYASABLONU")
public class VardiyaSablonu extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4226475989248347517L;
	public static final String COLUMN_NAME_ISKUR = "ISKUR";
	public static final String COLUMN_NAME_BEYAZ_YAKA = "BEYAZ_YAKA";

	private String adi;
	private Vardiya vardiya1, vardiya2, vardiya3, vardiya4, vardiya5, vardiya6, vardiya7;
	private CalismaModeli calismaModeli;
	private Personel personel;
	private String kayitDurum = VardiyaGun.STYLE_CLASS_EVEN;
	private HashMap tatilMap;
	private ArrayList<Date> gunler;
	private PersonelIzin izin1, izin2, izin3, izin4, izin5, izin6, izin7;
	private Departman departman;
	private Integer version = 0;
	private Boolean beyazYakaDefault = Boolean.FALSE, isKur = Boolean.FALSE;

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Column(name = "ADI", nullable = false)
	public String getAdi() {
		return adi;
	}

	public void setAdi(String adi) {
		adi = PdksUtil.convertUTF8(adi);
		this.adi = adi;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "DEPARTMAN_ID")
	@Fetch(FetchMode.JOIN)
	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman departman) {
		this.departman = departman;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "CALISMA_MODELI_ID")
	@Fetch(FetchMode.JOIN)
	public CalismaModeli getCalismaModeli() {
		return calismaModeli;
	}

	public void setCalismaModeli(CalismaModeli calismaModeli) {
		this.calismaModeli = calismaModeli;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "VARDIYA1_ID")
	@Fetch(FetchMode.JOIN)
	public Vardiya getVardiya1() {
		return vardiya1;
	}

	public void setVardiya1(Vardiya vardiya1) {
		this.vardiya1 = vardiya1;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "VARDIYA2_ID")
	@Fetch(FetchMode.JOIN)
	public Vardiya getVardiya2() {
		return vardiya2;
	}

	public void setVardiya2(Vardiya vardiya2) {
		this.vardiya2 = vardiya2;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "VARDIYA3_ID")
	@Fetch(FetchMode.JOIN)
	public Vardiya getVardiya3() {
		return vardiya3;
	}

	public void setVardiya3(Vardiya vardiya3) {
		this.vardiya3 = vardiya3;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "VARDIYA4_ID")
	@Fetch(FetchMode.JOIN)
	public Vardiya getVardiya4() {
		return vardiya4;
	}

	public void setVardiya4(Vardiya vardiya4) {
		this.vardiya4 = vardiya4;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "VARDIYA5_ID")
	@Fetch(FetchMode.JOIN)
	public Vardiya getVardiya5() {
		return vardiya5;
	}

	public void setVardiya5(Vardiya vardiya5) {
		this.vardiya5 = vardiya5;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "VARDIYA6_ID")
	@Fetch(FetchMode.JOIN)
	public Vardiya getVardiya6() {
		return vardiya6;
	}

	public void setVardiya6(Vardiya vardiya6) {
		this.vardiya6 = vardiya6;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "VARDIYA7_ID")
	@Fetch(FetchMode.JOIN)
	public Vardiya getVardiya7() {
		return vardiya7;
	}

	public void setVardiya7(Vardiya vardiya7) {
		this.vardiya7 = vardiya7;
	}

	@Column(name = COLUMN_NAME_BEYAZ_YAKA)
	public Boolean getBeyazYakaDefault() {
		return beyazYakaDefault;
	}

	public void setBeyazYakaDefault(Boolean beyazYakaDefault) {
		this.beyazYakaDefault = beyazYakaDefault;
	}

	@Column(name = COLUMN_NAME_ISKUR)
	public Boolean getIsKur() {
		return isKur;
	}

	public void setIsKur(Boolean isKur) {
		this.isKur = isKur;
	}

	@Transient
	public Personel getPersonel() {
		return personel;
	}

	public void setPersonel(Personel personel) {
		this.personel = personel;
	}

	@Transient
	public String getKayitDurum() {
		return kayitDurum;
	}

	public void setKayitDurum(String kayitDurum) {
		this.kayitDurum = kayitDurum;
	}

	@Transient
	public HashMap getTatilMap() {
		return tatilMap;
	}

	public void setTatilMap(HashMap tatilMap) {
		this.tatilMap = tatilMap;
	}

	@Transient
	public String getAdiSoyadi() {
		return personel != null ? personel.getAdSoyad() : "";
	}

	@Transient
	public ArrayList<Date> getGunler() {
		return gunler;
	}

	public void setGunler(ArrayList<Date> gunler) {
		this.gunler = gunler;
	}

	@Transient
	public PersonelIzin getIzin1() {
		return izin1;
	}

	public void setIzin1(PersonelIzin izin1) {
		this.izin1 = izin1;
	}

	@Transient
	public PersonelIzin getIzin2() {
		return izin2;
	}

	public void setIzin2(PersonelIzin izin2) {
		this.izin2 = izin2;
	}

	@Transient
	public PersonelIzin getIzin3() {
		return izin3;
	}

	public void setIzin3(PersonelIzin izin3) {
		this.izin3 = izin3;
	}

	@Transient
	public PersonelIzin getIzin4() {
		return izin4;
	}

	public void setIzin4(PersonelIzin izin4) {
		this.izin4 = izin4;
	}

	@Transient
	public PersonelIzin getIzin5() {
		return izin5;
	}

	public void setIzin5(PersonelIzin izin5) {
		this.izin5 = izin5;
	}

	@Transient
	public PersonelIzin getIzin6() {
		return izin6;
	}

	public void setIzin6(PersonelIzin izin6) {
		this.izin6 = izin6;
	}

	@Transient
	public PersonelIzin getIzin7() {
		return izin7;
	}

	public void setIzin7(PersonelIzin izin7) {
		this.izin7 = izin7;
	}

	@Transient
	public boolean isIsKurMu() {
		boolean isKurDurum = false;
		if (departman != null && departman.isAdminMi())
			isKurDurum = isKur != null && isKur;
		return isKurDurum;
	}
}
