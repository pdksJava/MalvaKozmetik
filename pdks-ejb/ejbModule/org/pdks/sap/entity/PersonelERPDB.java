package org.pdks.sap.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Immutable;
import org.pdks.session.PdksUtil;

import com.pdks.webservice.PersonelERP;

@Entity(name = PersonelERPDB.VIEW_NAME)
@Immutable
public class PersonelERPDB implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6726325986460092413L;

	public static final String VIEW_NAME = "PERSONEL_ERP_VIEW";
 	public static final String FORMAT_DATE = "yyyy-MM-dd";
	public static final String COLUMN_NAME_ISTEN_AYRILMA_TARIHI = "ISTEN_AYRILMA_TARIHI";
	public static final String COLUMN_NAME_GUNCELLEME_TARIHI = "GUNCELLEME_TARIHI";
	public static final String COLUMN_NAME_PERSONEL_NO = "PERSONEL_NO";

	static Logger logger = Logger.getLogger(PersonelERPDB.class);

	private String personelNo;
	private String adi;
	private String bolumAdi;
	private String bolumKodu;
	private String bordroAltAlanAdi;
	private String bordroAltAlanKodu;
	private String cinsiyetKodu;
	private String cinsiyeti;
	private String departmanAdi;
	private String departmanKodu;
	private Date dogumTarihi;
	private String gorevKodu;
	private String gorevi;
	private Date iseGirisTarihi;
	private Date istenAyrilmaTarihi;
	private Date kidemTarihi;
	private String masrafYeriAdi;
	private String masrafYeriKodu;
	private String personelTipi;
	private String personelTipiKodu;
	private Boolean sanalPersonel;
	private String sirketAdi;
	private String sirketKodu;
	private String soyadi;
	private String tesisAdi;
	private String tesisKodu;
	private String yoneticiPerNo;
	private Date grubaGirisTarihi;
	private String yonetici2PerNo;
	private Date guncellemeTarihi;

	public PersonelERPDB() {
		super();
	}

	@Id
	@Column(name = COLUMN_NAME_PERSONEL_NO)
	public String getPersonelNo() {
		return personelNo;
	}

	public void setPersonelNo(String personelNo) {
		this.personelNo = personelNo;
	}

	@Column(name = "ADI")
	public String getAdi() {
		return adi;
	}

	public void setAdi(String adi) {
		this.adi = adi;
	}

	@Column(name = "BOLUM_ADI")
	public String getBolumAdi() {
		return bolumAdi;
	}

	public void setBolumAdi(String bolumAdi) {
		this.bolumAdi = bolumAdi;
	}

	@Column(name = "BOLUM_KODU")
	public String getBolumKodu() {
		return bolumKodu;
	}

	public void setBolumKodu(String bolumKodu) {
		this.bolumKodu = bolumKodu;
	}

	@Column(name = "BORDRO_ALT_ALAN_ADI")
	public String getBordroAltAlanAdi() {
		return bordroAltAlanAdi;
	}

	public void setBordroAltAlanAdi(String bordroAltAlanAdi) {
		this.bordroAltAlanAdi = bordroAltAlanAdi;
	}

	@Column(name = "BORDRO_ALT_ALAN_KODU")
	public String getBordroAltAlanKodu() {
		return bordroAltAlanKodu;
	}

	public void setBordroAltAlanKodu(String bordroAltAlanKodu) {
		this.bordroAltAlanKodu = bordroAltAlanKodu;
	}

	@Column(name = "CINSIYET_KODU")
	public String getCinsiyetKodu() {
		return cinsiyetKodu;
	}

	public void setCinsiyetKodu(String cinsiyetKodu) {
		this.cinsiyetKodu = cinsiyetKodu;
	}

	@Column(name = "CINSIYETI")
	public String getCinsiyeti() {
		return cinsiyeti;
	}

	public void setCinsiyeti(String cinsiyeti) {
		this.cinsiyeti = cinsiyeti;
	}

	@Column(name = "DEPARTMAN_ADI")
	public String getDepartmanAdi() {
		return departmanAdi;
	}

	public void setDepartmanAdi(String departmanAdi) {
		this.departmanAdi = departmanAdi;
	}

	@Column(name = "DEPARTMAN_KODU")
	public String getDepartmanKodu() {
		return departmanKodu;
	}

	public void setDepartmanKodu(String departmanKodu) {
		this.departmanKodu = departmanKodu;
	}

	@Column(name = "DOGUM_TARIHI")
	@Temporal(TemporalType.DATE)
	public Date getDogumTarihi() {
		return dogumTarihi;
	}

	public void setDogumTarihi(Date dogumTarihi) {
		this.dogumTarihi = dogumTarihi;
	}

	@Column(name = "GOREV_KODU")
	public String getGorevKodu() {
		return gorevKodu;
	}

	public void setGorevKodu(String gorevKodu) {
		this.gorevKodu = gorevKodu;
	}

	@Column(name = "GOREVI")
	public String getGorevi() {
		return gorevi;
	}

	public void setGorevi(String gorevi) {
		this.gorevi = gorevi;
	}

	@Column(name = "ISE_GIRIS_TARIHI")
	@Temporal(TemporalType.DATE)
	public Date getIseGirisTarihi() {
		return iseGirisTarihi;
	}

	public void setIseGirisTarihi(Date iseGirisTarihi) {
		this.iseGirisTarihi = iseGirisTarihi;
	}

	@Column(name = COLUMN_NAME_ISTEN_AYRILMA_TARIHI)
	@Temporal(TemporalType.DATE)
	public Date getIstenAyrilmaTarihi() {
		return istenAyrilmaTarihi;
	}

	public void setIstenAyrilmaTarihi(Date istenAyrilmaTarihi) {
		this.istenAyrilmaTarihi = istenAyrilmaTarihi;
	}

	@Column(name = "KIDEM_TARIHI")
	@Temporal(TemporalType.DATE)
	public Date getKidemTarihi() {
		return kidemTarihi;
	}

	public void setKidemTarihi(Date kidemTarihi) {
		this.kidemTarihi = kidemTarihi;
	}

	@Column(name = "MASRAF_YERI_ADI")
	public String getMasrafYeriAdi() {
		return masrafYeriAdi;
	}

	public void setMasrafYeriAdi(String masrafYeriAdi) {
		this.masrafYeriAdi = masrafYeriAdi;
	}

	@Column(name = "MASRAF_YERI_KODU")
	public String getMasrafYeriKodu() {
		return masrafYeriKodu;
	}

	public void setMasrafYeriKodu(String masrafYeriKodu) {
		this.masrafYeriKodu = masrafYeriKodu;
	}

	@Column(name = "PERSONEL_TIPI")
	public String getPersonelTipi() {
		return personelTipi;
	}

	public void setPersonelTipi(String personelTipi) {
		this.personelTipi = personelTipi;
	}

	@Column(name = "PERSONEL_TIPI_KODU")
	public String getPersonelTipiKodu() {
		return personelTipiKodu;
	}

	public void setPersonelTipiKodu(String personelTipiKodu) {
		this.personelTipiKodu = personelTipiKodu;
	}

	@Column(name = "SANAL_PERSONEL")
	public Boolean getSanalPersonel() {
		return sanalPersonel;
	}

	public void setSanalPersonel(Boolean sanalPersonel) {
		this.sanalPersonel = sanalPersonel;
	}

	@Column(name = "SIRKET_ADI")
	public String getSirketAdi() {
		return sirketAdi;
	}

	public void setSirketAdi(String sirketAdi) {
		this.sirketAdi = sirketAdi;
	}

	@Column(name = "SIRKET_KODU")
	public String getSirketKodu() {
		return sirketKodu;
	}

	public void setSirketKodu(String sirketKodu) {
		this.sirketKodu = sirketKodu;
	}

	@Column(name = "SOYADI")
	public String getSoyadi() {
		return soyadi;
	}

	public void setSoyadi(String soyadi) {
		this.soyadi = soyadi;
	}

	@Column(name = "TESIS_ADI")
	public String getTesisAdi() {
		return tesisAdi;
	}

	public void setTesisAdi(String tesisAdi) {
		this.tesisAdi = tesisAdi;
	}

	@Column(name = "TESIS_KODU")
	public String getTesisKodu() {
		return tesisKodu;
	}

	public void setTesisKodu(String tesisKodu) {
		this.tesisKodu = tesisKodu;
	}

	@Column(name = "YONETICI_PERNO")
	public String getYoneticiPerNo() {
		return yoneticiPerNo;
	}

	public void setYoneticiPerNo(String yoneticiPerNo) {
		this.yoneticiPerNo = yoneticiPerNo;
	}

	@Column(name = "GRUBA_GIRIS_TARIHI")
	@Temporal(TemporalType.DATE)
	public Date getGrubaGirisTarihi() {
		return grubaGirisTarihi;
	}

	public void setGrubaGirisTarihi(Date grubaGirisTarihi) {
		this.grubaGirisTarihi = grubaGirisTarihi;
	}

	@Column(name = "YONETICI2_PERNO")
	public String getYonetici2PerNo() {
		return yonetici2PerNo;
	}

	public void setYonetici2PerNo(String yonetici2PerNo) {
		this.yonetici2PerNo = yonetici2PerNo;
	}

	@Column(name = COLUMN_NAME_GUNCELLEME_TARIHI)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getGuncellemeTarihi() {
		return guncellemeTarihi;
	}

	public void setGuncellemeTarihi(Date guncellemeTarihi) {
		this.guncellemeTarihi = guncellemeTarihi;
	}

	@Transient
	public PersonelERP getPersonelERP() {
		String pattern = FORMAT_DATE;
		PersonelERP p = new PersonelERP();
		p.setPersonelNo(this.personelNo);
		p.setAdi(this.adi);
		p.setBolumAdi(this.bolumAdi);
		p.setBolumKodu(this.bolumKodu);
		p.setBordroAltAlanAdi(this.bordroAltAlanAdi);
		p.setBordroAltAlanKodu(this.bordroAltAlanKodu);
		p.setCinsiyetKodu(this.cinsiyetKodu);
		p.setCinsiyeti(this.cinsiyeti);
		p.setDepartmanAdi(this.departmanAdi);
		p.setDepartmanKodu(this.departmanKodu);
		p.setDogumTarihi(PdksUtil.convertToDateString(this.dogumTarihi, pattern));
		p.setGorevKodu(this.gorevKodu);
		p.setGorevi(this.gorevi);
		p.setIseGirisTarihi(PdksUtil.convertToDateString(this.iseGirisTarihi, pattern));
		p.setIstenAyrilmaTarihi(PdksUtil.convertToDateString(this.istenAyrilmaTarihi, pattern));
		p.setKidemTarihi(PdksUtil.convertToDateString(this.kidemTarihi, pattern));
		p.setMasrafYeriAdi(this.masrafYeriAdi);
		p.setMasrafYeriKodu(this.masrafYeriKodu);
		p.setPersonelTipi(this.personelTipi);
		p.setPersonelTipiKodu(this.personelTipiKodu);
		p.setSanalPersonel(this.sanalPersonel);
		p.setSirketAdi(this.sirketAdi);
		p.setSirketKodu(this.sirketKodu);
		p.setSoyadi(this.soyadi);
		p.setTesisAdi(this.tesisAdi);
		p.setTesisKodu(this.tesisKodu);
		p.setYoneticiPerNo(this.yoneticiPerNo);
		p.setGrubaGirisTarihi(PdksUtil.convertToDateString(this.grubaGirisTarihi, pattern));
		p.setYonetici2PerNo(this.yonetici2PerNo);
		return p;
	}

	@Transient
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// bu class cloneable oldugu icin buraya girilmemeli...
			throw new InternalError();
		}
	}

}
