package org.pdks.entity;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.pdks.security.entity.User;
import org.pdks.session.PdksUtil;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@MappedSuperclass
public class YetkiBaseObject extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6655993138219145852L;
	public static final String COLUMN_NAME_BASLANGIC_TARIHI = "BASLANGIC_TARIHI";
	public static final String COLUMN_NAME_BITIS_TARIHI = "BITIS_TARIHI";
	public static final String COLUMN_NAME_YENI_YONETICI = "YENI_YONETICI";

	private User yeniYonetici;
	private Date basTarih, bitTarih;
	private Date devirBasTarih, devirBitTarih;
	private Integer version = 0;

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_YENI_YONETICI, nullable = false)
	@Fetch(FetchMode.JOIN)
	public User getYeniYonetici() {
		return yeniYonetici;
	}

	public void setYeniYonetici(User yeniYonetici) {
		this.yeniYonetici = yeniYonetici;
	}

	@Temporal(value = TemporalType.DATE)
	@Column(name = COLUMN_NAME_BASLANGIC_TARIHI, nullable = false)
	public Date getBasTarih() {
		return basTarih;
	}

	public void setBasTarih(Date basTarih) {
		this.basTarih = basTarih;
	}

	@Temporal(value = TemporalType.DATE)
	@Column(name = COLUMN_NAME_BITIS_TARIHI, nullable = false)
	public Date getBitTarih() {
		return bitTarih;
	}

	public void setBitTarih(Date bitTarih) {
		this.bitTarih = bitTarih;
	}

	@Transient
	public Date getDevirBasTarih() {
		return devirBasTarih;
	}

	public void setDevirBasTarih(Date basTarih) {
		this.devirBasTarih = basTarih;
	}

	@Transient
	public Date getDevirBitTarih() {
		return devirBitTarih;
	}

	public void setDevirBitTarih(Date bitTarih) {
		this.devirBitTarih = bitTarih;
	}

	@Transient
	public boolean isModify() {
		return PdksUtil.tarihKarsilastirNumeric(this.getBitTarih(), new Date()) == 1;
	}

	@Transient
	public boolean isDelete() {
		return PdksUtil.tarihKarsilastirNumeric(this.getBasTarih(), new Date()) == 1;
	}

}
