package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;
import org.pdks.security.entity.User;
import org.pdks.session.PdksUtil;

@Entity(name = PersonelHareketIslem.TABLE_NAME)
@Immutable
public class PersonelHareketIslem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2818559653323880265L;

	public static final String TABLE_NAME = "PDKS_ISLEM";
	public static final String COLUMN_NAME_ID = "ID";
	public static final int ONAY_DURUM_ISLEM_YAPILMADI = 0;
	public static final int ONAY_DURUM_ONAYLANDI = 1;
	public static final int ONAY_DURUM_ONAYLANMADI = 2;
	private Long id;
	private User guncelleyenUser, onaylayanUser;
	private Date zaman, olusturmaTarihi = new Date();
	private String aciklama, islemTipi;
	private int onayDurum;
	private Tanim neden;
	private boolean puantajOnayDurum = Boolean.TRUE;

	@Id
	@GeneratedValue
	@Column(name = COLUMN_NAME_ID, nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "GUNCELLEYEN_PERSONEL_ID", nullable = false)
	@Fetch(FetchMode.JOIN)
	public User getGuncelleyenUser() {
		return guncelleyenUser;
	}

	public void setGuncelleyenUser(User guncelleyenUser) {
		this.guncelleyenUser = guncelleyenUser;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "ONAYLAYAN_ID")
	@Fetch(FetchMode.JOIN)
	public User getOnaylayanUser() {
		return onaylayanUser;
	}

	public void setOnaylayanUser(User onaylayanUser) {
		this.onaylayanUser = onaylayanUser;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = "ZAMAN", nullable = false)
	public Date getZaman() {
		return zaman;
	}

	public void setZaman(Date zaman) {
		this.zaman = zaman;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = "OLUSTURMA_ZAMANI", nullable = false)
	public Date getOlusturmaTarihi() {
		return olusturmaTarihi;
	}

	public void setOlusturmaTarihi(Date olusturmaTarihi) {
		this.olusturmaTarihi = olusturmaTarihi;
	}

	@Column(name = "ACIKLAMA", length = 80)
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	@Column(name = "ISLEM_TIPI", length = 1)
	public String getIslemTipi() {
		return islemTipi;
	}

	public void setIslemTipi(String islemTipi) {
		this.islemTipi = islemTipi;
	}

	@Column(name = "ONAY_DURUM")
	public int getOnayDurum() {
		return onayDurum;
	}

	public void setOnayDurum(int onayDurum) {
		this.onayDurum = onayDurum;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "NEDEN_ID", nullable = false)
	@Fetch(FetchMode.JOIN)
	public Tanim getNeden() {
		return neden;
	}

	public void setNeden(Tanim neden) {
		this.neden = neden;
	}

	@Transient
	public boolean isAciklamaVar() {
		return PdksUtil.hasStringValue(aciklama);
	}

	@Transient
	public String getOnayDurumAciklama() {
		String onayladimi = "";
		if (onayDurum == PersonelHareketIslem.ONAY_DURUM_ONAYLANDI) {
			onayladimi = "onaylandi";
		} else if (onayDurum == PersonelHareketIslem.ONAY_DURUM_ONAYLANMADI) {
			onayladimi = "onaylanmadi";
		} else if (onayDurum == PersonelHareketIslem.ONAY_DURUM_ISLEM_YAPILMADI) {
			onayladimi = "islem yapilmadi";
		}
		return onayladimi;
	}

	@Transient
	public boolean isPuantajOnayDurum() {
		return puantajOnayDurum;
	}

	public void setPuantajOnayDurum(boolean puantajOnayDurum) {
		this.puantajOnayDurum = puantajOnayDurum;
	}

}
