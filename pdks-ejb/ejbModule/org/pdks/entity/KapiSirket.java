package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.pdks.session.PdksUtil;

@Entity(name = KapiSirket.TABLE_NAME)
public class KapiSirket implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2191674427143871092L;
	/**
	 * 
	 */

	public static final String TABLE_NAME = "KAPI_SIRKET";
	public static final String COLUMN_NAME_ID = "ID";
	public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";
	public static final String COLUMN_NAME_DURUM = "DURUM";

	private Long id;
	private String aciklama;

	private Boolean durum = Boolean.TRUE;

	@Id
	@Column(name = COLUMN_NAME_ID)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = COLUMN_NAME_ACIKLAMA)
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Transient
	public boolean isAciklamaVar() {
		return PdksUtil.hasStringValue(aciklama);
	}

}
