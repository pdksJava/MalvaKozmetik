package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

@Entity(name = PdksLog.TABLE_NAME)
public class PdksLog extends BasePDKSObject implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4808526699163741104L;

	static Logger logger = Logger.getLogger(PdksLog.class);

	// public static final String TABLE_NAME = "KGS_HAREKET_VIEW";
	public static final String TABLE_NAME = "PDKS_LOG";

	public static final String COLUMN_NAME_PERSONEL = "USERID";
	public static final String COLUMN_NAME_KAPI = "KAPIID";
	public static final String COLUMN_NAME_ZAMAN = "HAREKET_ZAMANI";
	public static final String COLUMN_NAME_OLUSTURMA_ZAMANI = "OLUSTURMA_ZAMANI";
	public static final String COLUMN_NAME_GUNCELLEME_ZAMANI = "GUNCELLEME_ZAMANI";

	public static final String COLUMN_NAME_ISLEM = "ISLEM_ID";
	public static final String COLUMN_NAME_DURUM = "DURUM";

	public static final String COLUMN_NAME_KGS_SIRKET = "KGS_SIRKET_ID";

	public static final String COLUMN_NAME_KGS_ID = "KGS_ID";

	private Long islemId, kgsSirketId, kgsId;

	private Date zaman, olusturmaZamani, guncellemeZamani;

	private Boolean durum;

	private Long kapiId;
	private Long personelId;

	@Column(name = COLUMN_NAME_KAPI)
	public Long getKapiId() {
		return this.kapiId;
	}

	public void setKapiId(Long kapiId) {
		this.kapiId = kapiId;
	}

	@Column(name = COLUMN_NAME_PERSONEL)
	public Long getPersonelId() {
		return this.personelId;
	}

	public void setPersonelId(Long personelId) {
		this.personelId = personelId;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_ZAMAN)
	public Date getZaman() {
		return this.zaman;
	}

	public void setZaman(Date vaue) {

		this.zaman = vaue;
	}

	@Column(name = COLUMN_NAME_KGS_SIRKET)
	public Long getKgsSirketId() {
		return kgsSirketId;
	}

	public void setKgsSirketId(Long kgsSirketId) {
		this.kgsSirketId = kgsSirketId;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_OLUSTURMA_ZAMANI)
	public Date getOlusturmaZamani() {
		return olusturmaZamani;
	}

	public void setOlusturmaZamani(Date olusturmaZamani) {
		this.olusturmaZamani = olusturmaZamani;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_GUNCELLEME_ZAMANI)
	public Date getGuncellemeZamani() {
		return guncellemeZamani;
	}

	public void setGuncellemeZamani(Date guncellemeZamani) {
		this.guncellemeZamani = guncellemeZamani;
	}

	@Column(name = COLUMN_NAME_ISLEM)
	public Long getIslemId() {
		return islemId;
	}

	public void setIslemId(Long islemId) {
		this.islemId = islemId;
	}

	@Column(name = COLUMN_NAME_KGS_ID)
	public Long getKgsId() {
		return kgsId;
	}

	public void setKgsId(Long kgsId) {
		this.kgsId = kgsId;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return this.durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Transient
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		}
		throw new InternalError();
	}

	@Transient
	public long getKgsSirketIdLong() {
		return kgsSirketId != null ? kgsSirketId.longValue() : 0L;
	}

}