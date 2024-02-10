package org.pdks.entity;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.management.relation.Role;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.Email;
import org.pdks.genel.model.PdksUtil;

@Entity(name = User.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { User.COLUMN_NAME_PERSONEL }), @UniqueConstraint(columnNames = { User.COLUMN_NAME_USERNAME }) })
public class User extends BasePDKSObject implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1485470535994107948L;
	static Logger logger = Logger.getLogger(User.class);

	public static final String TABLE_NAME = "PDKSUSER";

	public static final String COLUMN_NAME_USERNAME = "KULLANICI_ADI";
	public static final String COLUMN_NAME_PERSONEL = "PERSONEL_ID";
	public static final String COLUMN_NAME_DURUM = "DURUM";
	public static final String COLUMN_NAME_DEPARTMAN = "DEPARTMAN_ID";
	public static final String COLUMN_NAME_LAST_LOGIN = "LAST_LOGIN";
	public static final String COLUMN_NAME_EMAIL = "EMAIL";

	public static final String COLUMN_NAME_SHORT_USER_NAME = "SHORT_USER_NAME";

	private String username, shortUsername, passwordHash, newPassword, firstname, lastname, staffId, email, calistigiSayfa, remoteAddr;

	private Departman departman;

	// private MailGrubu mailGrubuCC, mailGrubuBCC, hareketMailGrubu;

	private Date lastLogin;

	// @OneToMany
	// private List<Role> roles;

	private List<Role> yetkiliRollerim, yetkiliRoller;

	private Personel pdksPersonel;

	private Tanim departmanTanim;

	// private Set<UserRoles> yetkilerim;

	private boolean durum = Boolean.TRUE, vardiyaDuzeltYetki;

	public User() {
		super();
	}

	@Email
	@Column(name = COLUMN_NAME_EMAIL, nullable = false)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = COLUMN_NAME_SHORT_USER_NAME, length = 12)
	public String getShortUsername() {
		return shortUsername;
	}

	public void setShortUsername(String shortUsername) {
		this.shortUsername = shortUsername;
	}

	@Transient
	public boolean isLdapUse() {
		return pdksPersonel != null && pdksPersonel.getSirket() != null ? pdksPersonel.getSirket().isLdap() : false;
	}

	@Column(name = COLUMN_NAME_USERNAME, nullable = false)
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column(name = "SIFRE")
	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	@Column(name = "VARDIYA_DUZELT_YEKI")
	public Boolean getVardiyaDuzeltYetki() {
		return vardiyaDuzeltYetki;
	}

	public void setVardiyaDuzeltYetki(Boolean vardiyaDuzeltYetki) {
		this.vardiyaDuzeltYetki = vardiyaDuzeltYetki;
	}

	@OneToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = User.COLUMN_NAME_PERSONEL, nullable = true)
	@Fetch(FetchMode.JOIN)
	public Personel getPdksPersonel() {
		if (pdksPersonel == null)
			pdksPersonel = new Personel();
		return pdksPersonel;
	}

	public void setPdksPersonel(Personel pdksPersonel) {
		this.pdksPersonel = pdksPersonel;
	}

	@Column(name = COLUMN_NAME_DURUM, nullable = false)
	public boolean isDurum() {
		return durum;
	}

	public void setDurum(boolean durum) {
		this.durum = durum;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_LAST_LOGIN)
	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_DEPARTMAN, nullable = true)
	@Fetch(FetchMode.JOIN)
	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman departman) {
		this.departman = departman;
	}

	@Transient
	public String getAdSoyad() {
		return pdksPersonel != null ? pdksPersonel.getAdSoyad() : "";
	}

	@Transient
	public Tanim getDepartmanTanim() {
		return departmanTanim;
	}

	public void setDepartmanTanim(Tanim departmanTanim) {
		this.departmanTanim = departmanTanim;
	}

	@Transient
	public String getTarihFormatla(Date tarih, String pattern) {
		String tarihStr = "";
		try {
			tarihStr = tarih != null ? PdksUtil.convertToDateString(tarih, pattern) : " ";
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			tarihStr = "";
		}
		return tarihStr;
	}

	@Transient
	public static Double getYuvarla(Double deger) {
		Double yeniDeger = null;
		if (deger != null) {
			if (deger > 0 && deger.doubleValue() > deger.longValue()) {
				double fark = deger.doubleValue() - deger.longValue();
				if (fark > 0.5)
					yeniDeger = deger.longValue() + 1.0d;
				else
					yeniDeger = deger.longValue() + 0.5d;

			} else
				yeniDeger = deger;
		}

		// logger.info(deger + " " + yeniDeger);
		return yeniDeger;
	}

	@Transient
	public String getAyAdi(int ay) {
		Calendar cal = Calendar.getInstance();
		cal.set(cal.get(Calendar.YEAR), ay, 1);
		String adi = PdksUtil.convertToDateString(cal.getTime(), "MMMMM");
		return adi;
	}

	@Transient
	public String sayiFormatliGoster(Object value) {
		String str = null;
		try {
			if (value != null)
				str = PdksUtil.numericValueFormatStr(value, null);
		} catch (Exception e) {
			str = null;
		}

		return str;
	}

	@Transient
	public Date getTarihUzat() {
		Date tarihUzat = PdksUtil.getSonSistemTarih();
		return tarihUzat;
	}

	@Transient
	public boolean isDepartmentAdmin() {
		boolean departmentAdminDurum = departman != null && departman.isAdminMi();
		return departmentAdminDurum;
	}

	@Transient
	public List<Role> getYetkiliRollerim() {

		return yetkiliRollerim;
	}

	public void setYetkiliRollerim(List<Role> yetkiliRollerim) {
		this.yetkiliRollerim = yetkiliRollerim;
	}

	@Transient
	public List<Role> getYetkiliRoller() {
		return yetkiliRoller;
	}

	public void setYetkiliRoller(List<Role> yetkiliRoller) {
		this.yetkiliRoller = yetkiliRoller;
	}

	@Transient
	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	@Transient
	public String getFirstname() {
		return pdksPersonel != null ? pdksPersonel.getAd() : firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	@Transient
	public String getLastname() {
		return pdksPersonel != null ? pdksPersonel.getSoyad() : lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	@Transient
	public Long getPersonelId() {
		Long personelId = pdksPersonel != null ? pdksPersonel.getId() : 0;
		return personelId;
	}

	@Transient
	public String getStaffId() {
		if (staffId == null)
			if (pdksPersonel != null && pdksPersonel.getPersonelKGS() != null)
				staffId = pdksPersonel.getPersonelKGS().getSicilNo();
		return staffId;
	}

	public void setStaffId(String staffId) {
		this.staffId = staffId;
	}

	@Transient
	public String getCalistigiSayfa() {
		return calistigiSayfa;
	}

	public void setCalistigiSayfa(String calistigiSayfa) {
		this.calistigiSayfa = calistigiSayfa;
	}

	@Transient
	public String getRemoteAddr() {
		return remoteAddr;
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
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
}