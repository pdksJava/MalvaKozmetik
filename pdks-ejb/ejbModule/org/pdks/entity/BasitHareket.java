package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Immutable;
import org.pdks.session.PdksUtil;

@Entity
@Table(name = HareketKGS.TABLE_NAME)
@Immutable
public class BasitHareket implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5754124493967457600L;
	static Logger logger = Logger.getLogger(BasitHareket.class);

	public static final int DURUM_AKTIF = 1;
	public static final int DURUM_PASIF = 0;
	private String id, sirket;
	private Date zaman, olusturmaZamani;
	private Long kapiId, personelId, islemId, hareketTableId;
	private int durum;
	protected boolean checkBoxDurum;

	public BasitHareket() {
		super();
	}

	@Id
	@Column(name = HareketKGS.COLUMN_NAME_ID)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = HareketKGS.COLUMN_NAME_KAPI, nullable = false)
	public Long getKapiId() {
		return kapiId;
	}

	public void setKapiId(Long kapiId) {
		this.kapiId = kapiId;
	}

	@Column(name = HareketKGS.COLUMN_NAME_PERSONEL, nullable = false)
	public Long getPersonelId() {
		return personelId;
	}

	@Column(name = HareketKGS.COLUMN_NAME_ISLEM)
	public Long getIslemId() {
		return islemId;
	}

	public void setIslemId(Long islemId) {
		this.islemId = islemId;
	}

	public void setPersonelId(Long personelId) {
		this.personelId = personelId;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = HareketKGS.COLUMN_NAME_ZAMAN, nullable = false)
	public Date getZaman() {
		return zaman;
	}

	public void setZaman(Object veri) {
		this.zaman = PdksUtil.getDateObjectValue(veri);
	}

	@Column(name = HareketKGS.COLUMN_NAME_SIRKET)
	public String getSirket() {
		return sirket;
	}

	public void setSirket(String sirket) {
		this.sirket = sirket;
	}

	@Column(name = HareketKGS.COLUMN_NAME_TABLE_ID)
	public Long getHareketTableId() {
		return hareketTableId;
	}

	public void setHareketTableId(Long hareketTableId) {
		this.hareketTableId = hareketTableId;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = HareketKGS.COLUMN_NAME_OLUSTURMA_ZAMANI, nullable = false)
	public Date getOlusturmaZamani() {
		return olusturmaZamani;
	}

	public void setOlusturmaZamani(Object veri) {

		this.olusturmaZamani = PdksUtil.getDateObjectValue(veri);
	}

	@Column(name = HareketKGS.COLUMN_NAME_DURUM)
	public int getDurum() {
		return durum;
	}

	public void setDurum(int durum) {
		this.durum = durum;
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

	@Transient
	public boolean isCheckBoxDurum() {
		return checkBoxDurum;
	}

	public void setCheckBoxDurum(boolean checkBoxDurum) {
		this.checkBoxDurum = checkBoxDurum;
	}

	@Transient
	public String getDurumu() {
		return checkBoxDurum ? "Mükerrer" : "";
	}

	@Transient
	public Long getZamanLong() {
		long value = zaman != null ? zaman.getTime() : 0L;
		return value;
	}

	@Transient
	public HareketKGS getKgsHareket() {
		HareketKGS hareket = new HareketKGS();
		hareket.setId(id);
		hareket.setDurum(durum);
		hareket.setKapiId(kapiId);
		hareket.setPersonelId(personelId);
		hareket.setZaman(zaman);
		hareket.setOlusturmaZamani(olusturmaZamani);
		hareket.setOrjinalZaman(zaman);
		hareket.setIslemId(islemId);
		hareket.setHareketTableId(hareketTableId);
		hareket.setSirket(sirket);
		if (id != null && id.trim().length() > 1) {
			if (sirket == null)
				hareket.setSirket(id.substring(0, 1));
			try {
				if (hareketTableId == null)
					hareket.setHareketTableId(Long.parseLong(id.substring(1)));
			} catch (Exception e) {
			}
		}
		return hareket;
	}

}
