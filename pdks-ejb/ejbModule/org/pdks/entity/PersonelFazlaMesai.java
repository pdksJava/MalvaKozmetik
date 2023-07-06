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

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = PersonelFazlaMesai.TABLE_NAME)
public class PersonelFazlaMesai extends BaseObject implements Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8455550990499778693L;

	public static final String TABLE_NAME = "PERSONELFAZLAMESAI";
	public static final String COLUMN_NAME_VARDIYA_GUN = "VARDIYA_GUN";
	public static final String COLUMN_NAME_HAREKET = "HAREKET_ID";

	public static final int DURUM_ONAYLANMADI = 0;
	public static final int DURUM_ONAYLANDI = 1;

	public static final int ARIFE = 2;
	public static final int BAYRAM = 1;

	private String hareketId;

	private VardiyaGun vardiyaGun;

	private HareketKGS hareket;

	private Tanim fazlaMesaiOnayDurum;

	private Double fazlaMesaiSaati = 0.0d;

	private Date basZaman, bitZaman;

	private FazlaMesaiTalep fazlaMesaiTalep;

	private int onayDurum = DURUM_ONAYLANDI;

	private Integer tatilDurum;

	@Column(name = COLUMN_NAME_HAREKET)
	public String getHareketId() {
		return hareketId;
	}

	public void setHareketId(String hareketId) {
		this.hareketId = hareketId;
	}

	@Transient
	// @OneToOne(cascade = CascadeType.REFRESH)
	// @JoinColumn(name = COLUMN_NAME_HAREKET)
	public HareketKGS getHareket() {
		return hareket;
	}

	public void setHareket(HareketKGS hareket) {
		if (hareket != null && hareketId == null)
			hareketId = hareket.getId();
		this.hareket = hareket;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_VARDIYA_GUN)
	@Fetch(FetchMode.JOIN)
	public VardiyaGun getVardiyaGun() {
		return vardiyaGun;
	}

	public void setVardiyaGun(VardiyaGun value) {
		this.vardiyaGun = value;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "FAZLA_MESAI_TALEP_ID")
	@Fetch(FetchMode.JOIN)
	public FazlaMesaiTalep getFazlaMesaiTalep() {
		return fazlaMesaiTalep;
	}

	public void setFazlaMesaiTalep(FazlaMesaiTalep fazlaMesaiTalep) {
		this.fazlaMesaiTalep = fazlaMesaiTalep;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "BAS_ZAMAN", nullable = false)
	public Date getBasZaman() {
		return basZaman;
	}

	public void setBasZaman(Date basZaman) {
		this.basZaman = basZaman;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "BIT_ZAMAN", nullable = false)
	public Date getBitZaman() {
		return bitZaman;
	}

	public void setBitZaman(Date bitZaman) {
		this.bitZaman = bitZaman;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "ONAY_ID")
	@Fetch(FetchMode.JOIN)
	public Tanim getFazlaMesaiOnayDurum() {
		return fazlaMesaiOnayDurum;
	}

	public void setFazlaMesaiOnayDurum(Tanim fazlaMesaiOnayDurum) {
		this.fazlaMesaiOnayDurum = fazlaMesaiOnayDurum;
	}

	@Column(name = "ONAY_DURUM")
	public int getOnayDurum() {
		return onayDurum;
	}

	public void setOnayDurum(int onayDurum) {
		this.onayDurum = onayDurum;
	}

	@Column(name = "TATIL_DURUM")
	public Integer getTatilDurum() {
		return tatilDurum;
	}

	public void setTatilDurum(Integer tatilDurum) {
		this.tatilDurum = tatilDurum;
	}

	@Column(name = "FAZLA_MESAI_SAATI")
	public Double getFazlaMesaiSaati() {
		return fazlaMesaiSaati;
	}

	public void setFazlaMesaiSaati(Double fazlaMesaiSaati) {
		this.fazlaMesaiSaati = fazlaMesaiSaati;
	}

	@Transient
	public String getOnayDurumAciklama() {
		String onayladimi = "";
		String mesaiSaati = "";
		try {
			if (onayDurum == DURUM_ONAYLANDI) {
				onayladimi = getFazlaMesaiOnayDurum().getAciklama();
				mesaiSaati = " FazlaMesai Saati : " + getFazlaMesaiSaati();
			} else if (onayDurum == DURUM_ONAYLANMADI) {
				onayladimi = getFazlaMesaiOnayDurum().getAciklama();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return onayladimi + mesaiSaati;
	}

	@Transient
	public boolean isOnaylandi() {
		return onayDurum == DURUM_ONAYLANDI;
	}

	@Transient
	public boolean isArife() {
		return tatilDurum != null && tatilDurum == ARIFE;
	}

	@Transient
	public boolean isBayram() {
		return tatilDurum != null && tatilDurum == BAYRAM;
	}

}
