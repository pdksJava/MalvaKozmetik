package com.pdks.webService;

import org.apache.cxf.annotations.WSDLDocumentation;

public class IzinERP extends BaseERPObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6559755325798188016L;

	private String personelNo, izinTipi, izinTipiAciklama, basZaman, bitZaman, referansNoERP, aciklama;

	private Double izinSuresi;

	private Boolean durum = Boolean.FALSE;

	private SureBirimi sureBirimi;

	public String getPersonelNo() {
		return personelNo;
	}

	public void setPersonelNo(String personelNo) {
		this.personelNo = personelNo;
	}

	public String getIzinTipi() {
		return izinTipi;
	}

	public void setIzinTipi(String izinTipi) {
		this.izinTipi = izinTipi;
	}

	public String getIzinTipiAciklama() {
		return izinTipiAciklama;
	}

	public void setIzinTipiAciklama(String izinTipiAciklama) {
		this.izinTipiAciklama = izinTipiAciklama;
	}

	public String getBasZaman() {
		return basZaman;
	}

	public void setBasZaman(String basZaman) {
		this.basZaman = basZaman;
	}

	public String getBitZaman() {
		return bitZaman;
	}

	public void setBitZaman(String bitZaman) {
		this.bitZaman = bitZaman;
	}

	public String getReferansNoERP() {
		return referansNoERP;
	}

	public void setReferansNoERP(String referansNoERP) {
		this.referansNoERP = referansNoERP;
	}

	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	public Double getIzinSuresi() {
		return izinSuresi;
	}

	public void setIzinSuresi(Double izinSuresi) {
		this.izinSuresi = izinSuresi;
	}

	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@WSDLDocumentation("Gun='1'/Saat='2' ")
	public SureBirimi getSureBirimi() {
		return sureBirimi;
	}

	public void setSureBirimi(SureBirimi sureBirimi) {
		this.sureBirimi = sureBirimi;
	}

}
