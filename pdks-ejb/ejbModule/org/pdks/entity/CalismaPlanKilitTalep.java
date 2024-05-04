package org.pdks.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;

@Entity(name = CalismaPlanKilitTalep.TABLE_NAME)
public class CalismaPlanKilitTalep extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5680610736713876141L;

	static Logger logger = Logger.getLogger(CalismaPlanKilitTalep.class);

	public static final String TABLE_NAME = "CALISMA_PLAN_KILIT_TALEP";
	public static final String COLUMN_NAME_CALISMA_PLAN_KILIT = "CALISMA_PLAN_KILIT_ID";
	public static final String COLUMN_NAME_NEDEN = "NEDEN_ID";
	public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";
	public static final String COLUMN_NAME_ONAY_DURUM = "ONAY_DURUM";

	public CalismaPlanKilitTalep() {
		super();
	}

	public CalismaPlanKilitTalep(CalismaPlanKilit calismaPlanKilit, Tanim neden, String aciklama) {
		super();
		this.calismaPlanKilit = calismaPlanKilit;
		this.neden = neden;
		this.aciklama = aciklama;
	}

	private Integer version = 0;

	private CalismaPlanKilit calismaPlanKilit;

	private Tanim neden;

	private String aciklama;

	private Boolean onayDurum;

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_CALISMA_PLAN_KILIT, nullable = false)
	public CalismaPlanKilit getCalismaPlanKilit() {
		return calismaPlanKilit;
	}

	public void setCalismaPlanKilit(CalismaPlanKilit calismaPlanKilit) {
		this.calismaPlanKilit = calismaPlanKilit;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_NEDEN, updatable = false, insertable = false)
	public Tanim getNeden() {
		return neden;
	}

	public void setNeden(Tanim neden) {
		this.neden = neden;
	}

	@Column(name = COLUMN_NAME_ONAY_DURUM)
	public Boolean getOnayDurum() {
		return onayDurum;
	}

	public void setOnayDurum(Boolean onayDurum) {
		this.onayDurum = onayDurum;
	}

	@Column(name = COLUMN_NAME_ACIKLAMA)
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}

}
