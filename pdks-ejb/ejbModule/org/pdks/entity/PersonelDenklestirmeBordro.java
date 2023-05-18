package org.pdks.entity;

import java.io.Serializable;
import java.util.HashMap;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = PersonelDenklestirmeBordro.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { PersonelDenklestirmeBordro.COLUMN_NAME_PERSONEL_DENKLESTIRME }) })
public class PersonelDenklestirmeBordro extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5229367345911755774L;

	static Logger logger = Logger.getLogger(PersonelDenklestirmeBordro.class);

	public static final String TABLE_NAME = "PERS_DENK_BORDRO";
	public static final String COLUMN_NAME_PERSONEL_DENKLESTIRME = "PERS_DENK_ID";
	public static final String COLUMN_NAME_NORMAL_GUN = "NORMAL_GUN_ADET";
	public static final String COLUMN_NAME_HAFTA_TATIL = "HAFTA_TATIL_ADET";
	public static final String COLUMN_NAME_G_HAFTA_TATIL = "TATIL_ADET";

	private PersonelDenklestirme personelDenklestirme;

	private Double normalGunAdet = 0.0d, haftaTatilAdet = 0.0d, tatilAdet = 0.0d;

	private boolean guncellendi = false;

	private HashMap<BordroDetayTipi, PersonelDenklestirmeBordroDetay> detayMap;

	public PersonelDenklestirmeBordro() {
		super();

	}

	public PersonelDenklestirmeBordro(PersonelDenklestirme personelDenklestirme, Double normalGunAdet, Double haftaTatilAdet, Double tatilAdet) {
		super();
		this.personelDenklestirme = personelDenklestirme;
		this.normalGunAdet = normalGunAdet;
		this.haftaTatilAdet = haftaTatilAdet;
		this.tatilAdet = tatilAdet;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL_DENKLESTIRME, nullable = false)
	@Fetch(FetchMode.JOIN)
	public PersonelDenklestirme getPersonelDenklestirme() {
		return personelDenklestirme;
	}

	public void setPersonelDenklestirme(PersonelDenklestirme personelDenklestirme) {
		this.personelDenklestirme = personelDenklestirme;
	}

	@Column(name = COLUMN_NAME_NORMAL_GUN)
	public Double getNormalGunAdet() {
		return normalGunAdet;
	}

	public void setNormalGunAdet(Double value) {
		if (guncellendi == false)
			this.setGuncellendi(this.normalGunAdet == null || this.normalGunAdet.doubleValue() != value.doubleValue());

		this.normalGunAdet = value;
	}

	@Column(name = COLUMN_NAME_HAFTA_TATIL)
	public Double getHaftaTatilAdet() {
		return haftaTatilAdet;
	}

	public void setHaftaTatilAdet(Double value) {
		if (guncellendi == false)
			this.setGuncellendi(this.haftaTatilAdet == null || this.haftaTatilAdet.doubleValue() != value.doubleValue());

		this.haftaTatilAdet = value;
	}

	@Column(name = COLUMN_NAME_G_HAFTA_TATIL)
	public Double getTatilAdet() {
		return tatilAdet;
	}

	public void setTatilAdet(Double value) {
		if (guncellendi == false)
			this.setGuncellendi(this.tatilAdet == null || this.tatilAdet.doubleValue() != value.doubleValue());
		this.tatilAdet = value;
	}

	// @Column(name = COLUMN_NAME_RESMI_TATIL_ADET)
	// public Double getResmiTatilAdet() {
	// return resmiTatilAdet;
	// }
	//
	// public void setResmiTatilAdet(Double value) {
	// if (guncellendi == false)
	// this.setGuncellendi(this.resmiTatilAdet == null || this.resmiTatilAdet.doubleValue() != value.doubleValue());
	// this.resmiTatilAdet = value;
	// }
	//
	// @Column(name = COLUMN_NAME_IZIN_ADET)
	// public Double getIzinGunAdet() {
	// return izinGunAdet;
	// }
	//
	// public void setIzinGunAdet(Double value) {
	// if (guncellendi == false)
	// this.setGuncellendi(this.izinGunAdet == null || this.izinGunAdet.doubleValue() != value.doubleValue());
	// this.izinGunAdet = value;
	// }

	@Transient
	public boolean isGuncellendi() {
		return guncellendi;
	}

	public void setGuncellendi(boolean guncellendi) {
		this.guncellendi = guncellendi;
	}

	@Transient
	public Double getUcretiOdenenMesai() {
		Double detay = personelDenklestirme != null ? personelDenklestirme.getOdenenSure() : null;
		Double value = detay != null ? detay.doubleValue() : 0.0d;
		return value;
	}

	@Transient
	public Double getEksikCalismaSure() {
		Double detay = personelDenklestirme != null ? personelDenklestirme.getEksikCalismaSure() : null;
		Double value = detay != null ? detay.doubleValue() : 0.0d;
		return value;
	}

	@Transient
	public Double getResmiTatilMesai() {
		Double detay = personelDenklestirme != null ? personelDenklestirme.getResmiTatilSure() : null;
		Double value = detay != null ? detay.doubleValue() : 0.0d;
		return value;
	}

	@Transient
	public Double getHaftaTatilMesai() {
		Double detay = personelDenklestirme != null ? personelDenklestirme.getHaftaCalismaSuresi() : null;
		Double value = detay != null ? detay.doubleValue() : 0.0d;
		return value;
	}

	@Transient
	public Integer getAksamGunMesai() {
		Double detay = personelDenklestirme != null ? personelDenklestirme.getAksamVardiyaSayisi() : null;
		Integer value = detay != null ? detay.intValue() : 0;
		return value;
	}

	@Transient
	public Double getAksamSaatMesai() {
		Double detay = personelDenklestirme != null ? personelDenklestirme.getAksamVardiyaSaatSayisi() : null;
		Double value = detay != null ? detay.doubleValue() : 0.0d;

		return value;
	}

	@Transient
	public Double getSaatResmiTatil() {
		PersonelDenklestirmeBordroDetay detay = getBordroDetay(BordroDetayTipi.SAAT_RESMI_TATIL);
		Double value = detay != null ? detay.getMiktar().doubleValue() : 0.0d;
		return value;
	}

	@Transient
	public Double getSaatHaftaTatil() {
		PersonelDenklestirmeBordroDetay detay = getBordroDetay(BordroDetayTipi.SAAT_HAFTA_TATIL);
		Double value = detay != null ? detay.getMiktar().doubleValue() : 0.0d;
		return value;
	}

	@Transient
	public Double getSaatNormal() {
		PersonelDenklestirmeBordroDetay detay = getBordroDetay(BordroDetayTipi.SAAT_NORMAL);
		Double value = detay != null ? detay.getMiktar().doubleValue() : 0.0d;
		return value;
	}

	@Transient
	public Double getSaatIzin() {
		PersonelDenklestirmeBordroDetay detay = getBordroDetay(BordroDetayTipi.SAAT_IZIN);
		Double value = detay != null ? detay.getMiktar().doubleValue() : 0.0d;
		return value;
	}

	@Transient
	public Integer getUcretliIzin() {
		PersonelDenklestirmeBordroDetay detay = getBordroDetay(BordroDetayTipi.UCRETLI_IZIN);
		Integer value = detay != null ? detay.getMiktar().intValue() : 0;
		return value;
	}
	@Transient
	public Integer getUcretliIzinGunAdet() {
		PersonelDenklestirmeBordroDetay detay = getBordroDetay(BordroDetayTipi.IZIN_GUN);
		Integer value = detay != null ? detay.getMiktar().intValue() : 0;
		return value;
	}
	@Transient
	public Integer getUcretsizIzin() {
		PersonelDenklestirmeBordroDetay detay = getBordroDetay(BordroDetayTipi.UCRETSIZ_IZIN);
		Integer value = detay != null ? detay.getMiktar().intValue() : 0;
		return value;
	}

	@Transient
	public Integer getRaporluIzin() {
		PersonelDenklestirmeBordroDetay detay = getBordroDetay(BordroDetayTipi.RAPORLU_IZIN);
		Integer value = detay != null ? detay.getMiktar().intValue() : 0;
		return value;
	}

	@Transient
	public PersonelDenklestirmeBordroDetay getBordroDetay(BordroDetayTipi bordroDetayTipi) {
		PersonelDenklestirmeBordroDetay bordroDetay = null;
		if (bordroDetayTipi != null && detayMap != null && detayMap.containsKey(bordroDetayTipi))
			bordroDetay = detayMap.get(bordroDetayTipi);
		return bordroDetay;
	}

	@Transient
	public long getPersonelDenklestirmeId() {
		return personelDenklestirme != null ? personelDenklestirme.getIdLong() : 0l;
	}

	@Transient
	public HashMap<BordroDetayTipi, PersonelDenklestirmeBordroDetay> getDetayMap() {
		return detayMap;
	}

	public void setDetayMap(HashMap<BordroDetayTipi, PersonelDenklestirmeBordroDetay> detayMap) {
		this.detayMap = detayMap;
	}

}
