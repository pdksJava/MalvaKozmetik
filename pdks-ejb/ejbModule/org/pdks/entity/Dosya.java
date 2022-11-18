/**
 * 
 */
package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.pdks.session.PdksUtil;

/**
 * 
 */

@Entity(name = "DOSYA")
public class Dosya implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2908520509292855097L;
	private Long id;
	private Integer version = 0;

	private byte[] dosyaIcerik;
	private String icerikTipi, dosyaAdi, aciklama;

	private Integer size;
	private boolean islemYapildi = Boolean.FALSE;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Column(name = "ICERIK_TIPI", length = 128)
	public String getIcerikTipi() {
		return icerikTipi;
	}

	public void setIcerikTipi(String icerikTipi) {
		this.icerikTipi = icerikTipi;
	}

	@Column(name = "DOSYA_ADI", length = 128)
	public String getDosyaAdi() {
		return dosyaAdi;
	}

	public void setDosyaAdi(String dosyaAdi) {
		this.dosyaAdi = dosyaAdi;
	}

	@Lob
	@Column(name = "DOSYA_ICERIK")
	public byte[] getDosyaIcerik() {
		return dosyaIcerik;
	}

	public void setDosyaIcerik(byte[] dosyaIcerik) {
		this.dosyaIcerik = dosyaIcerik;
	}

	@Transient
	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	@Transient
	public boolean isIslemYapildi() {
		return islemYapildi;
	}

	public void setIslemYapildi(boolean islemYapildi) {
		this.islemYapildi = islemYapildi;
	}

	@Column(name = "ACIKLAMA", length = 128)
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	@Transient
	public boolean isAciklamaVar() {
		return PdksUtil.hasStringValue(aciklama);
	}

	@Transient
	public String getDisposition() {
		return FileUpload.getDisposition(icerikTipi);
	}

	@Transient
	public boolean isInline() {
		return FileUpload.isInline(icerikTipi);
	}
}
