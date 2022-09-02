package org.pdks.entity;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = FazlaMesaiTalep.TABLE_NAME)
public class FazlaMesaiTalep extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 396302409626429921L;

	static Logger logger = Logger.getLogger(FazlaMesaiTalep.class);

	public static final String TABLE_NAME = "FAZLA_MESAI_TALEP";
	public static final String COLUMN_NAME_VARDIYA_GUN = "VARDIYA_GUN_ID";
	public static final String COLUMN_NAME_MESAI_NEDEN = "MESAI_NEDEN_ID";
	public static final String COLUMN_NAME_IPTAL_NEDEN = "IPTAL_NEDEN_ID";
	public static final String COLUMN_NAME_BASLANGIC_ZAMANI = "BASLANGIC_ZAMANI";
	public static final String COLUMN_NAME_BITIS_ZAMANI = "BITIS_ZAMANI";
	public static final String COLUMN_NAME_MESAI_SURESI = "MESAI_SURESI";
	public static final String COLUMN_NAME_ONAY_DURUMU = "ONAY_DURUMU";
	public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";
	public static final String COLUMN_NAME_IPTAL_ACIKLAMA = "IPTAL_ACIKLAMA";
	public static final int ONAY_DURUM_ISLEM_YAPILMADI = 1;
	public static final int ONAY_DURUM_ONAYLANDI = 2;
	public static final int ONAY_DURUM_RED = 3;

	private Date baslangicZamani, bitisZamani;

	private Double mesaiSuresi = 0D;

	private int onayDurumu = ONAY_DURUM_ISLEM_YAPILMADI;

	private String aciklama, iptalAciklama;

	private VardiyaGun vardiyaGun;

	private Tanim mesaiNeden, redNedeni;

	private int basSaat, basDakika, bitSaat, bitDakika;

	private boolean islemYapildi = Boolean.FALSE, onaylayan = Boolean.FALSE, yoneticisi = Boolean.FALSE;

	public FazlaMesaiTalep() {
		super();

	}

	public FazlaMesaiTalep(VardiyaGun value) {
		super();
		if (value != null && value.getVardiya() != null && !value.getVardiya().isCalisma()) {
			baslangicZamani = value.getVardiyaDate();
			bitisZamani = value.getVardiyaDate();
		}
		this.vardiyaGun = value;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_VARDIYA_GUN, nullable = false)
	@Fetch(FetchMode.JOIN)
	public VardiyaGun getVardiyaGun() {
		return vardiyaGun;
	}

	public void setVardiyaGun(VardiyaGun vardiyaGun) {
		this.vardiyaGun = vardiyaGun;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_MESAI_NEDEN)
	@Fetch(FetchMode.JOIN)
	public Tanim getMesaiNeden() {
		return mesaiNeden;
	}

	public void setMesaiNeden(Tanim mesaiNeden) {
		this.mesaiNeden = mesaiNeden;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_BASLANGIC_ZAMANI, nullable = false)
	public Date getBaslangicZamani() {
		return baslangicZamani;
	}

	public void setBaslangicZamani(Date baslangicZamani) {
		this.baslangicZamani = baslangicZamani;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_BITIS_ZAMANI, nullable = false)
	public Date getBitisZamani() {
		return bitisZamani;
	}

	public void setBitisZamani(Date bitisZamani) {
		this.bitisZamani = bitisZamani;
	}

	@Column(name = COLUMN_NAME_ACIKLAMA)
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	@Column(name = COLUMN_NAME_MESAI_SURESI, nullable = false)
	public Double getMesaiSuresi() {
		return mesaiSuresi;
	}

	public void setMesaiSuresi(Double mesaiSuresi) {
		this.mesaiSuresi = mesaiSuresi;
	}

	@Column(name = COLUMN_NAME_ONAY_DURUMU)
	public int getOnayDurumu() {
		return onayDurumu;
	}

	public void setOnayDurumu(int onayDurumu) {
		this.onayDurumu = onayDurumu;
	}

	@Column(name = COLUMN_NAME_IPTAL_ACIKLAMA)
	public String getIptalAciklama() {
		return iptalAciklama;
	}

	public void setIptalAciklama(String iptalAciklama) {
		this.iptalAciklama = iptalAciklama;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_IPTAL_NEDEN)
	@Fetch(FetchMode.JOIN)
	public Tanim getRedNedeni() {
		return redNedeni;
	}

	public void setRedNedeni(Tanim redNedeni) {
		this.redNedeni = redNedeni;
	}

	@Transient
	public boolean isIslemYapildi() {
		return islemYapildi;
	}

	public void setIslemYapildi(boolean islemYapildi) {
		this.islemYapildi = islemYapildi;
	}

	@Transient
	public String getOnayDurumAciklama() {
		String onayDurumAciklama = "";
		if (durum) {

			switch (onayDurumu) {
			case ONAY_DURUM_ISLEM_YAPILMADI:
				onayDurumAciklama = "Onay Bekliyor.";
				break;
			case ONAY_DURUM_ONAYLANDI:
				onayDurumAciklama = "Onaylandı.";
				break;
			case ONAY_DURUM_RED:
				onayDurumAciklama = "Red Edildi." + (redNedeni != null ? " ( " + redNedeni.getAciklama() + (iptalAciklama != null && iptalAciklama.trim().length() > 0 ? " - " + iptalAciklama.trim() : "") + " )" : "");
				break;
			default:
				onayDurumAciklama = "";
				break;
			}
		} else
			onayDurumAciklama = "İptal Edildi.";
		return onayDurumAciklama;
	}

	@Transient
	public int getBasSaat() {
		return basSaat;
	}

	public void setBasSaat(int basSaat) {
		this.basSaat = basSaat;
	}

	@Transient
	public int getBasDakika() {
		return basDakika;
	}

	public void setBasDakika(int basDakika) {
		this.basDakika = basDakika;
	}

	@Transient
	public int getBitSaat() {
		return bitSaat;
	}

	public void setBitSaat(int bitSaat) {
		this.bitSaat = bitSaat;
	}

	@Transient
	public int getBitDakika() {
		return bitDakika;
	}

	public void setBitDakika(int bitDakika) {
		this.bitDakika = bitDakika;
	}

	@Transient
	public boolean isIptalEdilebilir() {
		boolean iptalEdilebilir = durum && onayDurumu != ONAY_DURUM_RED;
		return iptalEdilebilir;
	}

	@Transient
	public boolean isHatirlatmaMail() {
		boolean hatirlatmaMail = durum && onayDurumu == ONAY_DURUM_ISLEM_YAPILMADI;
		return hatirlatmaMail;
	}

	@Transient
	public boolean isOnaylayan() {
		return onaylayan;
	}

	public void setOnaylayan(boolean onaylayan) {
		this.onaylayan = onaylayan;
	}

	@Transient
	public boolean isYoneticisi() {
		return yoneticisi;
	}

	public void setYoneticisi(boolean yoneticisi) {
		this.yoneticisi = yoneticisi;
	}

}
