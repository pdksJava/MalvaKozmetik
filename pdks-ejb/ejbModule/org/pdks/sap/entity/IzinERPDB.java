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

import com.pdks.webservice.IzinERP;

@Entity(name = "Z_NOT_USED_IZIN_ERP_DB")
@Immutable
public class IzinERPDB implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6307241769198780131L;
	public static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm";
	public static final String COLUMN_NAME_BAS_TARIHI = "BASLANGIC_ZAMANI";
	public static final String COLUMN_NAME_BIT_TARIHI = "BITIS_ZAMANI";
	public static final String COLUMN_NAME_REFERANS_NO = "REFERANS_ID";
	public static final String COLUMN_NAME_GUNCELLEME_TARIHI = "GUNCELLEME_TARIHI";

	static Logger logger = Logger.getLogger(IzinERPDB.class);

	private String aciklama;
	private Date basZaman;
	private Date bitZaman;
	private Boolean durum;
	private Double izinSuresi;
	private String izinTipi;
	private String izinTipiAciklama;
	private String personelNo;
	private String referansNoERP;
	private String sureBirimi;
	private Date guncellemeTarihi;

	public IzinERPDB() {
		super();
	}

	@Id
	@Column(name = COLUMN_NAME_REFERANS_NO)
	public String getReferansNoERP() {
		return referansNoERP;
	}

	public void setReferansNoERP(String referansNoERP) {
		this.referansNoERP = referansNoERP;
	}

	@Column(name = "PERSONEL_NO")
	public String getPersonelNo() {
		return personelNo;
	}

	public void setPersonelNo(String personelNo) {
		this.personelNo = personelNo;
	}

	@Column(name = "IZIN_TIPI")
	public String getIzinTipi() {
		return izinTipi;
	}

	public void setIzinTipi(String izinTipi) {
		this.izinTipi = izinTipi;
	}

	@Column(name = "IZIN_TIPI_ACIKLAMA")
	public String getIzinTipiAciklama() {
		return izinTipiAciklama;
	}

	@Column(name = COLUMN_NAME_BAS_TARIHI)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getBasZaman() {
		return basZaman;
	}

	public void setBasZaman(Date basZaman) {
		this.basZaman = basZaman;
	}

	@Column(name = COLUMN_NAME_BIT_TARIHI)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getBitZaman() {
		return bitZaman;
	}

	public void setBitZaman(Date bitZaman) {
		this.bitZaman = bitZaman;
	}

	@Column(name = "IZIN_SURESI")
	public Double getIzinSuresi() {
		return izinSuresi;
	}

	public void setIzinSuresi(Double izinSuresi) {
		this.izinSuresi = izinSuresi;
	}

	public void setIzinTipiAciklama(String izinTipiAciklama) {
		this.izinTipiAciklama = izinTipiAciklama;
	}

	@Column(name = "ACIKLAMA")
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	@Column(name = "SURE_BIRIMI")
	public String getSureBirimi() {
		return sureBirimi;
	}

	public void setSureBirimi(String sureBirimi) {
		this.sureBirimi = sureBirimi;
	}

	@Column(name = "DURUM")
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
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
	public IzinERP getIzinERP() {
		IzinERP i = new IzinERP();
		i.setReferansNoERP(this.getReferansNoERP());
		i.setPersonelNo(this.getPersonelNo());
		i.setBasZaman(PdksUtil.convertToDateString(this.getBasZaman(), FORMAT_DATE_TIME));
		i.setBitZaman(PdksUtil.convertToDateString(this.getBitZaman(), FORMAT_DATE_TIME));
		i.setIzinSuresi(this.getIzinSuresi());
		i.setIzinTipi(this.getIzinTipi());
		i.setIzinTipiAciklama(this.getIzinTipiAciklama());
		i.setAciklama(this.getAciklama());
		i.setDurum(this.getDurum());
		i.setSureBirimi(this.getSureBirimi());
		return i;
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
