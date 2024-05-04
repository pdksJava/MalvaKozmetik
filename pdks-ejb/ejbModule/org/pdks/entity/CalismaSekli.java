package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.security.entity.User;
import org.pdks.session.PdksUtil;

@Entity(name = CalismaSekli.TABLE_NAME)
public class CalismaSekli extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6317841082433463763L;
	static Logger logger = Logger.getLogger(CalismaSekli.class);
	public static final String TABLE_NAME = "CALISMA_SEKLI";
 	public static final String COLUMN_NAME_ADI = "ADI";
	public static final String COLUMN_NAME_ARIFE_NORMAL_CALISMA_DAKIKA = "ARIFE_NORMAL_CALISMA_DAKIKA";

	public static final String COLUMN_NAME_DURUM = "DURUM";
	public static final String COLUMN_NAME_OLUSTURAN = "OLUSTURANUSER_ID";
	public static final String COLUMN_NAME_GUNCELLEYEN = "GUNCELLEYENUSER_ID";
	public static final String COLUMN_NAME_OLUSTURMA_TARIHI = "OLUSTURMATARIHI";
	public static final String COLUMN_NAME_GUNCELLEME_TARIHI = "GUNCELLEMETARIHI";

	private Boolean durum = Boolean.TRUE, checkBoxDurum;
	private User guncelleyenUser, olusturanUser;
	private Date olusturmaTarihi = new Date(), guncellemeTarihi;
	private String adi;
	private Double arifeNormalCalismaDakika = 240d;

	@Column(name = COLUMN_NAME_ADI)
	public String getAdi() {
		return adi;
	}

	public void setAdi(String adi) {
		this.adi = adi;
	}

	@Column(name = COLUMN_NAME_ARIFE_NORMAL_CALISMA_DAKIKA)
	public Double getArifeNormalCalismaDakika() {
		return arifeNormalCalismaDakika;
	}

	public void setArifeNormalCalismaDakika(Double arifeNormalCalismaDakika) {
		this.arifeNormalCalismaDakika = arifeNormalCalismaDakika;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = COLUMN_NAME_GUNCELLEYEN, nullable = true)
	@Fetch(FetchMode.JOIN)
	public User getGuncelleyenUser() {
		return guncelleyenUser;
	}

	public void setGuncelleyenUser(User guncelleyenUser) {
		this.guncelleyenUser = guncelleyenUser;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = COLUMN_NAME_OLUSTURAN, nullable = true)
	@Fetch(FetchMode.JOIN)
	public User getOlusturanUser() {
		return olusturanUser;
	}

	public void setOlusturanUser(User olusturanUser) {
		this.olusturanUser = olusturanUser;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_OLUSTURMA_TARIHI)
	public Date getOlusturmaTarihi() {
		return olusturmaTarihi;
	}

	public void setOlusturmaTarihi(Date olusturmaTarihi) {
		this.olusturmaTarihi = olusturmaTarihi;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_GUNCELLEME_TARIHI)
	public Date getGuncellemeTarihi() {
		return guncellemeTarihi;
	}

	public void setGuncellemeTarihi(Date guncellemeTarihi) {
		this.guncellemeTarihi = guncellemeTarihi;
	}

	@Transient
	public String getDurumAciklama() {
		return User.getDurumAciklamaAna(durum);
	}

	@Transient
	public User getSonIslemYapan() {
		return guncelleyenUser != null ? guncelleyenUser : olusturanUser;
	}

	@Transient
	public Date getSonIslemTarihi() {
		return guncellemeTarihi != null ? guncellemeTarihi : olusturmaTarihi;
	}

	@Transient
	public String getSonIslemTarihiStr() {
		Date date = getSonIslemTarihi();
		String str = date != null ? PdksUtil.convertToDateString(date, PdksUtil.getDateFormat() + " H:mm:ss") : "";
		return str;
	}

	@Transient
	public Boolean getCheckBoxDurum() {
		return checkBoxDurum;
	}

	public void setCheckBoxDurum(Boolean checkBoxDurum) {
		this.checkBoxDurum = checkBoxDurum;
	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}

}
