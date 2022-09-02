package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;

 
@Entity(name = "Z_NOT_USED_TABLE_HOLDING_IZIN")
@Immutable
public class HoldingIzin implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7487009656840444082L;
	static Logger logger = Logger.getLogger(HoldingIzin.class);
	public static final String TABLE_NAME = "SP_HOLDING_IZIN_BAKIYE_HEPSI";
	public static final String COLUMN_NAME_ID = "PERSONEL_ID";
	public static final String COLUMN_NAME_SIRKET = "SIRKET_ID";
	public static final String COLUMN_NAME_IZIN = "IZIN_ID";

	private Long id;

	private Personel personel;

	private Integer buYilHakedilen, buYilHakedilenSua;

	private Double gecenYilBakiye, harcananIzin;

	public HoldingIzin() {
		super();

	}

	public HoldingIzin(Personel personel) {
		super();
		this.personel = personel;
		this.buYilHakedilen = 0;
		this.gecenYilBakiye = 0.0d;
		this.harcananIzin = 0.0d;
	}

	@Id
	@Column(name = COLUMN_NAME_ID)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = HoldingIzin.COLUMN_NAME_ID, nullable = false, insertable = false, updatable = false)
	@Fetch(FetchMode.JOIN)
	public Personel getPersonel() {
		return personel;
	}

	public void setPersonel(Personel personel) {
		this.personel = personel;
	}

	@Column(name = "GECEN_YIL_BAKIYE")
	public Double getGecenYilBakiye() {
		return gecenYilBakiye;
	}

	public void setGecenYilBakiye(Double gecenYilBakiye) {
		this.gecenYilBakiye = gecenYilBakiye;
	}

	@Column(name = "BU_YIL_HAKKEDILEN")
	public Integer getBuYilHakedilen() {
		return buYilHakedilen;
	}

	public void setBuYilHakedilen(Integer buYilHakedilen) {
		this.buYilHakedilen = buYilHakedilen;
	}

	@Column(name = "HARCANAN_SURE")
	public Double getHarcananIzin() {
		return harcananIzin;
	}

	public void setHarcananIzin(Double harcananIzin) {
		this.harcananIzin = harcananIzin;
	}

	@Column(name = "BU_YIL_HAKKEDILEN_SUA")
	public Integer getBuYilHakedilenSua() {
		return buYilHakedilenSua;
	}

	public void setBuYilHakedilenSua(Integer buYilHakedilenSua) {
		this.buYilHakedilenSua = buYilHakedilenSua;
	}

	@Transient
	public Double getBakiye() {
		double bakiye = gecenYilBakiye + buYilHakedilen - harcananIzin;
		return bakiye;
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
