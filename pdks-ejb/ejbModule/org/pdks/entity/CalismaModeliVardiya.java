package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = CalismaModeliVardiya.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { CalismaModeliVardiya.COLUMN_NAME_CALISMA_MODELI, CalismaModeliVardiya.COLUMN_NAME_VARDIYA, }) })
public class CalismaModeliVardiya extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8626070485848428888L;
	public static final String TABLE_NAME = "CALISMA_MODELI_VARDIYA";
 	public static final String COLUMN_NAME_VARDIYA = "VARDIYA_ID";
	public static final String COLUMN_NAME_CALISMA_MODELI = "CALISMA_MODELI_ID";

	private Vardiya vardiya;

	private CalismaModeli calismaModeli;

	public CalismaModeliVardiya() {
		super();
	}

	public CalismaModeliVardiya(Vardiya vardiya, CalismaModeli calismaModeli) {
		super();
		this.vardiya = vardiya;
		this.calismaModeli = calismaModeli;
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
	@JoinColumn(name = COLUMN_NAME_CALISMA_MODELI)
	@Fetch(FetchMode.JOIN)
	public CalismaModeli getCalismaModeli() {
		return calismaModeli;
	}

	public void setCalismaModeli(CalismaModeli calismaModeli) {
		this.calismaModeli = calismaModeli;
	}

	@Transient
	public static String getKey(Vardiya xVardiya, CalismaModeli xCalismaModeli) {
		String key = (xVardiya != null ? xVardiya.getId() : 0) + "_" + (xCalismaModeli != null ? xCalismaModeli.getId() : 0);
		return key;
	}

	@Transient
	public String getKey() {
		String key = getKey(vardiya, calismaModeli);
		return key;
	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}

}
