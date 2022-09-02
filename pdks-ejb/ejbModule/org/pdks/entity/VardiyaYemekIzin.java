package org.pdks.entity;

import java.io.Serializable;

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

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = VardiyaYemekIzin.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { VardiyaYemekIzin.COLUMN_NAME_VARDIYA, VardiyaYemekIzin.COLUMN_NAME_YEMEK_IZIN }) })
public class VardiyaYemekIzin implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6290964256777098002L;
	public static final String TABLE_NAME = "VARDIYA_YEMEK_IZIN";
	public static final String COLUMN_NAME_ID = "ID";
	public static final String COLUMN_NAME_VARDIYA = "VARDIYA_ID";
	public static final String COLUMN_NAME_YEMEK_IZIN = "YEMEK_IZIN_ID";

	private Long id;

	private Vardiya vardiya;

	private YemekIzin yemekIzin;

	public VardiyaYemekIzin() {
		super();
	}

	public VardiyaYemekIzin(Vardiya vardiya, YemekIzin yemekIzin) {
		super();
		this.vardiya = vardiya;
		this.yemekIzin = yemekIzin;
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
	@JoinColumn(name = COLUMN_NAME_VARDIYA)
	@Fetch(FetchMode.JOIN)
	public Vardiya getVardiya() {
		return vardiya;
	}

	public void setVardiya(Vardiya vardiya) {
		this.vardiya = vardiya;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_YEMEK_IZIN)
	@Fetch(FetchMode.JOIN)
	public YemekIzin getYemekIzin() {
		return yemekIzin;
	}

	public void setYemekIzin(YemekIzin yemekIzin) {
		this.yemekIzin = yemekIzin;
	}

	@Transient
	public Long getYemekNumeric() {
		Long yemekNumeric = yemekIzin != null ? yemekIzin.getYemekNumeric() : 0L;
		return yemekNumeric;
	}

	@Transient
	public static String getKey(Vardiya xVardiya, YemekIzin xYemekIzin) {
		String key = (xVardiya != null ? xVardiya.getId() : 0) + "_" + (xYemekIzin != null ? xYemekIzin.getId() : 0);
		return key;
	}

	@Transient
	public String getKey() {
		String key = getKey(vardiya, yemekIzin);
		return key;
	}

}
