package org.pdks.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.log4j.Logger;

@Entity(name = CalismaPlanKilit.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { CalismaPlanKilit.COLUMN_NAME_DONEM, CalismaPlanKilit.COLUMN_NAME_SIRKET, CalismaPlanKilit.COLUMN_NAME_TESIS, CalismaPlanKilit.COLUMN_NAME_BOLUM }) })
public class CalismaPlanKilit extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8541103664473606517L;

	static Logger logger = Logger.getLogger(CalismaPlanKilit.class);

	public static final String TABLE_NAME = "CALISMA_PLAN_KILIT";
	public static final String COLUMN_NAME_DONEM = "DONEM_ID";
	public static final String COLUMN_NAME_SIRKET = "SIRKET_ID";
	public static final String COLUMN_NAME_TESIS = "TESIS_ID";
	public static final String COLUMN_NAME_BOLUM = "BOLUM_ID";
	public static final String COLUMN_NAME_KILIT_DURUM = "KILIT_DURUM";
	public static final String COLUMN_NAME_TALEP_KONTROL = "TALEP_KONTROL";

	private Integer version = 0;

	private Long sirketId, tesisId, bolumId;

	private DenklestirmeAy denklestirmeAy;

	private Sirket sirket;

	private Tanim tesis, bolum;

	private Boolean kilitDurum = Boolean.FALSE, talepKontrol = Boolean.FALSE;

	private CalismaPlanKilitTalep talep;

	public CalismaPlanKilit() {
		super();

	}

	public CalismaPlanKilit(Sirket sirket, Long tesisId, Long bolumId, DenklestirmeAy denklestirmeAy) {
		super();
		this.denklestirmeAy = denklestirmeAy;
		this.sirketId = sirket != null ? sirket.getId() : null;
		this.tesisId = tesisId;
		this.bolumId = bolumId;
	}

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_DONEM, nullable = false)
	public DenklestirmeAy getDenklestirmeAy() {
		return denklestirmeAy;
	}

	public void setDenklestirmeAy(DenklestirmeAy denklestirmeAy) {
		this.denklestirmeAy = denklestirmeAy;
	}

	@Column(name = COLUMN_NAME_SIRKET, nullable = false)
	public Long getSirketId() {
		return sirketId;
	}

	public void setSirketId(Long sirketId) {
		this.sirketId = sirketId;
	}

	@Column(name = COLUMN_NAME_TESIS)
	public Long getTesisId() {
		return tesisId;
	}

	public void setTesisId(Long tesisId) {
		this.tesisId = tesisId;
	}

	@Column(name = COLUMN_NAME_BOLUM, nullable = false)
	public Long getBolumId() {
		return bolumId;
	}

	public void setBolumId(Long bolumId) {
		this.bolumId = bolumId;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_SIRKET, updatable = false, insertable = false)
	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket sirket) {
		this.sirket = sirket;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_TESIS, updatable = false, insertable = false)
	public Tanim getTesis() {
		return tesis;
	}

	public void setTesis(Tanim tesis) {
		this.tesis = tesis;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_BOLUM, updatable = false, insertable = false)
	public Tanim getBolum() {
		return bolum;
	}

	public void setBolum(Tanim bolum) {
		this.bolum = bolum;
	}

	@Column(name = COLUMN_NAME_KILIT_DURUM)
	public Boolean getKilitDurum() {
		return kilitDurum;
	}

	public void setKilitDurum(Boolean kilitDurum) {
		this.kilitDurum = kilitDurum;
	}

	@Column(name = COLUMN_NAME_TALEP_KONTROL)
	public Boolean getTalepKontrol() {
		return talepKontrol;
	}

	public void setTalepKontrol(Boolean talepKontrol) {
		this.talepKontrol = talepKontrol;
	}

	@Transient
	public CalismaPlanKilitTalep getTalep() {
		return talep;
	}

	public void setTalep(CalismaPlanKilitTalep talep) {
		this.talep = talep;
	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}

}
