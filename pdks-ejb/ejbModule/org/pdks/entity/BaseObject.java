package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.security.entity.User;
import org.pdks.session.PdksUtil;

@MappedSuperclass
public abstract class BaseObject extends BasePDKSObject implements Serializable, Cloneable {

	/**
	 * 
	 */

	public static final String COLUMN_NAME_DURUM = "DURUM";
	public static final String COLUMN_NAME_OLUSTURAN = "OLUSTURANUSER_ID";
	public static final String COLUMN_NAME_GUNCELLEYEN = "GUNCELLEYENUSER_ID";
	public static final String COLUMN_NAME_OLUSTURMA_TARIHI = "OLUSTURMATARIHI";
	public static final String COLUMN_NAME_GUNCELLEME_TARIHI = "GUNCELLEMETARIHI";

	private static final long serialVersionUID = 4940573223065841991L;

	protected Boolean durum = Boolean.TRUE;
	protected User guncelleyenUser, olusturanUser;
	protected Date olusturmaTarihi = new Date(), guncellemeTarihi;
	protected boolean checkBoxDurum = Boolean.FALSE;
	protected String style = VardiyaGun.STYLE_CLASS_ODD, titleStr = "";

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
	public boolean isCheckBoxDurum() {
		return checkBoxDurum;
	}

	public void setCheckBoxDurum(boolean checkBoxDurum) {
		this.checkBoxDurum = checkBoxDurum;
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
		String str = date != null ? PdksUtil.convertToDateString(date, PdksUtil.getDateTimeLongFormat()) : "";
		return str;
	}

	@Transient
	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	@Transient
	public long getIdLong() {
		return id != null ? id.longValue() : 0l;
	}

	@Transient
	public String getTitleStr() {
		return titleStr;
	}

	public void setTitleStr(String titleStr) {
		this.titleStr = titleStr;
	}

	@Transient
	public Object clone() {
		try {
			this.setGuncelleyenUser(null);
			this.setGuncellemeTarihi(null);
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// bu class cloneable oldugu icin buraya girilmemeli...
			throw new InternalError();
		}
	}

	@Transient
	public Object kopyala() {
		try {
			Object object = super.clone();
			return object;
		} catch (CloneNotSupportedException e) {
			// bu class cloneable oldugu icin buraya girilmemeli...
			throw new InternalError();
		}
	}

}
