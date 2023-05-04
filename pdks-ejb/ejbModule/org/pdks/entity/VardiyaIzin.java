package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = VardiyaIzin.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { VardiyaIzin.COLUMN_NAME_IZIN_VARDIYA }) })
public class VardiyaIzin extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6101813697656858461L;

	public static final String TABLE_NAME = "VARDIYA_IZIN";

	public static final String COLUMN_NAME_ID = "ID";
	public static final String COLUMN_NAME_IZIN_VARDIYA = "IZIN_VARDIYA_ID";
	public static final String COLUMN_NAME_CALISMA_VARDIYA = "CALISMA_VARDIYA_ID";

	public VardiyaIzin() {
		super();

	}

	public VardiyaIzin(Vardiya izinVardiya) {
		super();
		this.izinVardiya = izinVardiya;
	}

	private Vardiya izinVardiya, calismaVardiya;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = COLUMN_NAME_IZIN_VARDIYA, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Vardiya getIzinVardiya() {
		return izinVardiya;
	}

	public void setIzinVardiya(Vardiya izinVardiya) {
		this.izinVardiya = izinVardiya;
	}

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = COLUMN_NAME_CALISMA_VARDIYA)
	@Fetch(FetchMode.JOIN)
	public Vardiya getCalismaVardiya() {
		return calismaVardiya;
	}

	public void setCalismaVardiya(Vardiya calismaVardiya) {
		this.calismaVardiya = calismaVardiya;
	}

}
