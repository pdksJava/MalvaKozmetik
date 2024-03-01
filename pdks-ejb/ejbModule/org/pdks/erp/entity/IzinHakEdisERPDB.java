package org.pdks.erp.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Immutable;
import org.pdks.entity.BasePDKSObject;
import org.pdks.session.PdksUtil;

import com.pdks.webservice.IzinERP;

@Entity(name = IzinHakEdisERPDB.VIEW_NAME)
@Immutable
public class IzinHakEdisERPDB extends BasePDKSObject implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1163591238528656143L;
	public static final String VIEW_NAME = "PERSONEL_IZIN_HAKEDIS_VIEW";
	public static final String COLUMN_NAME_PERSONEL_NO = "PERSONEL_NO";
	public static final String COLUMN_NAME_KIDEM_YIL = "KIDEM_YIL";

	static Logger logger = Logger.getLogger(IzinHakEdisERPDB.class);

	private String personelNo;
	private String kodu;
	private String erpKodu;
	private String izinTipi;
	private Integer kidemYili;
	private String kidem;
	private Date kidemBaslangicTarihi;
	private Date hakEdisTarihi;
	private Integer hakEdisGunSayisi;
	private Date izinBaslangicZamani;
	private Date izinBitisZamani;
	private Double izinGunSayisi;
	private String aciklama;
	private Boolean durum;
	private String sureBirimi;

	public IzinHakEdisERPDB() {
		super();
	}

	@Column(name = COLUMN_NAME_PERSONEL_NO)
	public String getPersonelNo() {
		return personelNo;
	}

	public void setPersonelNo(String personelNo) {
		this.personelNo = personelNo;
	}

	@Column(name = "KODU")
	public String getKodu() {
		return kodu;
	}

	public void setKodu(String kodu) {
		this.kodu = kodu;
	}

	@Column(name = "ERP_KODU")
	public String getErpKodu() {
		return erpKodu;
	}

	public void setErpKodu(String erpKodu) {
		this.erpKodu = erpKodu;
	}

	@Column(name = "IZIN_TIPI")
	public String getIzinTipi() {
		return izinTipi;
	}

	public void setIzinTipi(String izinTipi) {
		this.izinTipi = izinTipi;
	}

	@Column(name = COLUMN_NAME_KIDEM_YIL)
	public Integer getKidemYili() {
		return kidemYili;
	}

	public void setKidemYili(Integer kidemYili) {
		this.kidemYili = kidemYili;
	}

	@Column(name = "KIDEM")
	public String getKidem() {
		return kidem;
	}

	public void setKidem(String kidem) {
		this.kidem = kidem;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "KIDEM_BAS_TARIHI")
	public Date getKidemBaslangicTarihi() {
		return kidemBaslangicTarihi;
	}

	public void setKidemBaslangicTarihi(Date kidemBaslangicTarihi) {
		this.kidemBaslangicTarihi = kidemBaslangicTarihi;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "HAKEDIS_TARIHI")
	public Date getHakEdisTarihi() {
		return hakEdisTarihi;
	}

	public void setHakEdisTarihi(Date hakEdisTarihi) {
		this.hakEdisTarihi = hakEdisTarihi;
	}

	@Column(name = "HAKEDIS_GUNU")
	public Integer getHakEdisGunSayisi() {
		return hakEdisGunSayisi;
	}

	public void setHakEdisGunSayisi(Integer hakEdisGunSayisi) {
		this.hakEdisGunSayisi = hakEdisGunSayisi;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "BASLANGIC_ZAMANI")
	public Date getIzinBaslangicZamani() {
		return izinBaslangicZamani;
	}

	public void setIzinBaslangicZamani(Date izinBaslangicZamani) {
		this.izinBaslangicZamani = izinBaslangicZamani;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "BITIS_ZAMANI")
	public Date getIzinBitisZamani() {
		return izinBitisZamani;
	}

	public void setIzinBitisZamani(Date izinBitisZamani) {
		this.izinBitisZamani = izinBitisZamani;
	}

	@Column(name = "IZIN_SURESI")
	public Double getIzinGunSayisi() {
		return izinGunSayisi;
	}

	public void setIzinGunSayisi(Double izinGunSayisi) {
		this.izinGunSayisi = izinGunSayisi;
	}

	@Column(name = "IZIN_ACIKLAMA")
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	@Column(name = "DURUM")
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Column(name = "SURE_BIRIMI")
	public String getSureBirimi() {
		return sureBirimi;
	}

	public void setSureBirimi(String sureBirimi) {
		this.sureBirimi = sureBirimi;
	}

	@Transient
	public static String getKey(String xPerNo, Integer xKidemYil) {
		String str = (xPerNo != null ? xPerNo : "") + "_" + (xKidemYil != null ? xKidemYil : 0);
		return str;
	}

	@Transient
	public String getKey() {
		String str = getKey(personelNo, kidemYili);
		return str;
	}

	@Transient
	public IzinERP getIzinERP() {
		IzinERP izinERP = new IzinERP();
		izinERP.setAciklama(this.getAciklama());
		izinERP.setBasZaman(PdksUtil.convertToDateString(this.getIzinBaslangicZamani(), IzinERPDB.FORMAT_DATE_TIME));
		izinERP.setBitZaman(PdksUtil.convertToDateString(this.getIzinBitisZamani(), IzinERPDB.FORMAT_DATE_TIME));
		izinERP.setDurum(this.getDurum());
		izinERP.setIzinSuresi(this.getIzinGunSayisi());
		izinERP.setIzinTipi(this.getErpKodu());
		izinERP.setIzinTipiAciklama(this.getIzinTipi());
		izinERP.setPersonelNo(this.getPersonelNo());
		izinERP.setReferansNoERP(String.valueOf(this.getId()));
		izinERP.setSureBirimi(this.getSureBirimi());
		return izinERP;
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
