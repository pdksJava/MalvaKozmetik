package org.pdks.entity;

import java.io.Serializable;
import java.util.HashMap;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
public class PersonelDenklestirmeBordro implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5229367345911755774L;

	static Logger logger = Logger.getLogger(PersonelDenklestirmeBordro.class);

	public static final String TABLE_NAME = "PERS_DENK_BORDRO";
	public static final String COLUMN_NAME_ID = "ID";
	public static final String COLUMN_NAME_PERSONEL_DENKLESTIRME = "PERS_DENK_ID";
	public static final String COLUMN_NAME_NORMAL_GUN = "NORMAL_GUN_ADET";
	public static final String COLUMN_NAME_HAFTA_TATIL = "HAFTA_TATIL_ADET";
	public static final String COLUMN_NAME_G_HAFTA_TATIL = "TATIL_ADET";

	private Long id;

	private PersonelDenklestirme personelDenklestirme;

	private Integer normalGunAdet = 0, haftaTatilAdet = 0, tatilAdet = 0;

	private boolean guncellendi = false;

	private HashMap<BordroTipi, PersonelDenklestirmeBordroDetay> detayMap;

	public PersonelDenklestirmeBordro() {
		super();

	}

	public PersonelDenklestirmeBordro(PersonelDenklestirme personelDenklestirme, Integer normalGunAdet, Integer haftaTatilAdet, Integer tatilAdet) {
		super();
		this.personelDenklestirme = personelDenklestirme;
		this.normalGunAdet = normalGunAdet;
		this.haftaTatilAdet = haftaTatilAdet;
		this.tatilAdet = tatilAdet;
	}

	@Id
	@GeneratedValue
	@Column(name = COLUMN_NAME_ID)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
	public Integer getNormalGunAdet() {
		return normalGunAdet;
	}

	public void setNormalGunAdet(Integer value) {
		if (guncellendi == false)
			this.setGuncellendi(this.normalGunAdet == null || this.normalGunAdet.intValue() != value.intValue());

		this.normalGunAdet = value;
	}

	@Column(name = COLUMN_NAME_HAFTA_TATIL)
	public Integer getHaftaTatilAdet() {
		return haftaTatilAdet;
	}

	public void setHaftaTatilAdet(Integer value) {
		if (guncellendi == false)
			this.setGuncellendi(this.haftaTatilAdet == null || this.haftaTatilAdet.intValue() != value.intValue());

		this.haftaTatilAdet = value;
	}

	@Column(name = COLUMN_NAME_G_HAFTA_TATIL)
	public Integer getTatilAdet() {
		return tatilAdet;
	}

	public void setTatilAdet(Integer value) {
		if (guncellendi == false)
			this.setGuncellendi(this.tatilAdet == null || this.tatilAdet.intValue() != value.intValue());
		this.tatilAdet = value;
	}

	@Transient
	public boolean isGuncellendi() {
		return guncellendi;
	}

	public void setGuncellendi(boolean guncellendi) {
		this.guncellendi = guncellendi;
	}

	@Transient
	public Double getUcretiOdenenMesai() {
		PersonelDenklestirmeBordroDetay detay = getBordroDetay(BordroTipi.UCRETI_ODENEN_MESAI);
		Double value = detay != null ? detay.getMiktar().doubleValue() : 0.0d;
		return value;
	}

	@Transient
	public Double getResmiTatilMesai() {
		PersonelDenklestirmeBordroDetay detay = getBordroDetay(BordroTipi.RESMI_TATIL_MESAI);
		Double value = detay != null ? detay.getMiktar().doubleValue() : 0.0d;
		return value;
	}

	@Transient
	public Double getHaftaTatilMesai() {
		PersonelDenklestirmeBordroDetay detay = getBordroDetay(BordroTipi.HAFTA_TATIL_MESAI);
		Double value = detay != null ? detay.getMiktar().doubleValue() : 0.0d;
		return value;
	}

	@Transient
	public Double getAksamGunMesai() {
		PersonelDenklestirmeBordroDetay detay = getBordroDetay(BordroTipi.AKSAM_GUN_MESAI);
		Double value = detay != null ? detay.getMiktar().doubleValue() : 0.0d;
		return value;
	}

	@Transient
	public Double getAksamSaatMesai() {
		PersonelDenklestirmeBordroDetay detay = getBordroDetay(BordroTipi.AKSAM_SAAT_MESAI);
		Double value = detay != null ? detay.getMiktar().doubleValue() : 0.0d;
		return value;
	}

	@Transient
	public Integer getUcretliIzin() {
		PersonelDenklestirmeBordroDetay detay = getBordroDetay(BordroTipi.UCRETLI_IZIN);
		Integer value = detay != null ? detay.getMiktar().intValue() : 0;
		return value;
	}

	@Transient
	public Integer getUcretsizIzin() {
		PersonelDenklestirmeBordroDetay detay = getBordroDetay(BordroTipi.UCRETSIZ_IZIN);
		Integer value = detay != null ? detay.getMiktar().intValue() : 0;
		return value;
	}

	@Transient
	public Integer getRaporluIzin() {
		PersonelDenklestirmeBordroDetay detay = getBordroDetay(BordroTipi.RAPORLU_IZIN);
		Integer value = detay != null ? detay.getMiktar().intValue() : 0;
		return value;
	}

	@Transient
	public PersonelDenklestirmeBordroDetay getBordroDetay(BordroTipi bordroTipi) {
		PersonelDenklestirmeBordroDetay bordroDetay = null;
		if (bordroTipi != null && detayMap != null && detayMap.containsKey(bordroTipi))
			bordroDetay = detayMap.get(bordroTipi);
		return bordroDetay;
	}

	@Transient
	public long getPersonelDenklestirmeId() {
		return personelDenklestirme != null ? personelDenklestirme.getIdLong() : 0l;
	}

	@Transient
	public HashMap<BordroTipi, PersonelDenklestirmeBordroDetay> getDetayMap() {
		return detayMap;
	}

	public void setDetayMap(HashMap<BordroTipi, PersonelDenklestirmeBordroDetay> detayMap) {
		this.detayMap = detayMap;
	}

}
