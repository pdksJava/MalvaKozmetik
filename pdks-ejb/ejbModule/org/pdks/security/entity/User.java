package org.pdks.security.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.Email;
import org.jboss.seam.annotations.security.management.UserPassword;
import org.jboss.seam.annotations.security.management.UserPrincipal;
import org.pdks.entity.BasePDKSObject;
import org.pdks.entity.Departman;
import org.pdks.entity.Personel;
import org.pdks.entity.Tanim;
import org.pdks.session.LDAPUserManager;
import org.pdks.session.PdksUtil;

import com.pdks.webservice.MailPersonel;

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
	public static final String COLUMN_NAME_PERSONEL_NO = "PER_NO";

	private String username, shortUsername, passwordHash, newPassword, firstname, lastname, staffId, email, calistigiSayfa, remoteAddr, fullName;
	private String personelNo, parametreJSON;

	private Departman departman;

	private Date lastLogin;

	private UserMenuItemTime menuItemTime;

	private List<Role> yetkiliRollerim, yetkiliRoller;

	private List<Tanim> yetkiliTesisler;

	private Personel pdksPersonel;

	private Tanim departmanTanim;

	// private Set<UserRoles> yetkilerim;

	private boolean durum = Boolean.TRUE, yeniSifre = Boolean.FALSE, admin = Boolean.FALSE, IK = Boolean.FALSE, IK_Tesis = Boolean.FALSE;
	private boolean sistemYoneticisi = Boolean.FALSE, yonetici = Boolean.FALSE, anahtarKullanici = Boolean.FALSE, yoneticiKontratli = Boolean.FALSE, genelMudur = Boolean.FALSE, sekreter = Boolean.FALSE;
	private boolean projeMuduru = Boolean.FALSE, mudur = Boolean.FALSE, superVisor = Boolean.FALSE, IKDirektor = Boolean.FALSE, personel = Boolean.FALSE;
	private boolean operatorSSK = Boolean.FALSE, yetkiSet = Boolean.FALSE, direktorSuperVisor = Boolean.FALSE, taseronAdmin = Boolean.FALSE;
	private boolean browserIE, izinGirebilir = Boolean.FALSE, izinSSKGirebilir = Boolean.FALSE, izinOnaylayabilir = Boolean.FALSE, testLogin = Boolean.FALSE;
	private boolean tesisYonetici, raporKullanici = Boolean.FALSE;
	private ArrayList<User> userVekaletList;

	private List<Personel> yetkiliPersoneller, ikinciYoneticiPersonel;

	private ArrayList<String> personelGeciciNoList, yetkiTumPersonelNoList, yetkiliPersonelNoList = new ArrayList<String>(), eskiPersonelList = new ArrayList<String>(), superVisorHemsirePersonelNoList;

	private User seciliSuperVisor;

	private Boolean vardiyaDuzeltYetki = Boolean.FALSE, secili = Boolean.FALSE, login = Boolean.FALSE;

	private HttpSession session;

	private Session sessionSQL;

	public User() {
		super();
		// this.emailCC = null;
		// this.emailBCC = null;
		// this.hareketMail = null;
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

	@UserPrincipal
	@Column(name = COLUMN_NAME_USERNAME, nullable = false)
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@UserPassword
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

	@Column(name = COLUMN_NAME_PERSONEL_NO, insertable = false, updatable = false)
	public String getPersonelNo() {
		return personelNo;
	}

	public void setPersonelNo(String personelNo) {
		this.personelNo = personelNo;
	}

	@Transient
	public boolean isAdmin() {
		if (!yetkiSet)
			PdksUtil.setUserYetki(this);
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}

	@Transient
	public boolean isMudur() {
		if (!yetkiSet)
			PdksUtil.setUserYetki(this);
		return mudur;
	}

	public void setMudur(Boolean mudur) {
		this.mudur = mudur;
	}

	@Transient
	public boolean isIK() {
		if (!yetkiSet)
			PdksUtil.setUserYetki(this);
		return IK;
	}

	@Transient
	public boolean isIK_Tesis() {
		if (!yetkiSet)
			PdksUtil.setUserYetki(this);
		return IK_Tesis;
	}

	public void setIK_Tesis(Boolean iK_Tesis) {
		IK_Tesis = iK_Tesis;
	}

	@Transient
	public boolean isSAPPersonel() {
		return pdksPersonel != null && pdksPersonel.getSirket() != null && pdksPersonel.getSirket().isErp();
	}

	public void setIK(Boolean ik) {
		IK = ik;
	}

	@Transient
	public boolean isYonetici() {
		if (!yetkiSet)
			PdksUtil.setUserYetki(this);
		boolean yoneticiDurum = yonetici || yoneticiKontratli;
		return yoneticiDurum;
	}

	public void setYonetici(Boolean yonetici) {
		this.yonetici = yonetici;
	}

	@Transient
	public boolean isGenelMudur() {
		if (!yetkiSet)
			PdksUtil.setUserYetki(this);
		return genelMudur;
	}

	public void setGenelMudur(Boolean genelMudur) {
		this.genelMudur = genelMudur;
	}

	@Transient
	public ArrayList<String> getEskiPersonelNoList() {
		return (ArrayList<String>) eskiPersonelList.clone();
	}

	public void setEskiPersonelNoList(ArrayList<String> eskiPersonelList) {
		this.eskiPersonelList = eskiPersonelList;
	}

	@Transient
	public ArrayList<String> getYetkiliPersonelNoList() {
		return (ArrayList<String>) yetkiliPersonelNoList.clone();
	}

	public void setYetkiliPersonelNoList(ArrayList<String> yetkiliPersonelNoList) {
		this.yetkiliPersonelNoList = yetkiliPersonelNoList;
	}

	@Transient
	public boolean isSekreter() {
		if (!yetkiSet)
			PdksUtil.setUserYetki(this);
		return sekreter;
	}

	public void setSekreter(Boolean sekreter) {
		this.sekreter = sekreter;
	}

	@Transient
	public boolean isProjeMuduru() {
		if (!yetkiSet)
			PdksUtil.setUserYetki(this);
		return projeMuduru;
	}

	public void setProjeMuduru(Boolean projeMuduru) {
		this.projeMuduru = projeMuduru;
	}

	@Transient
	public boolean isTesisYonetici() {
		if (!yetkiSet)
			PdksUtil.setUserYetki(this);
		return tesisYonetici;
	}

	public void setTesisYonetici(Boolean tesisYonetici) {
		this.tesisYonetici = tesisYonetici;
	}

	@Transient
	public boolean isTaseronAdmin() {
		if (!yetkiSet)
			PdksUtil.setUserYetki(this);
		return taseronAdmin;
	}

	public void setTaseronAdmin(Boolean taseronAdmin) {
		this.taseronAdmin = taseronAdmin;
	}

	@Transient
	public boolean isPersonel() {
		if (!yetkiSet)
			PdksUtil.setUserYetki(this);
		return personel;
	}

	public void setPersonel(Boolean personel) {
		this.personel = personel;
	}

	@Transient
	public boolean isSuperVisor() {
		if (!yetkiSet)
			PdksUtil.setUserYetki(this);
		return superVisor;
	}

	public void setSuperVisor(Boolean superVisor) {
		this.superVisor = superVisor;
	}

	@Transient
	public ArrayList<User> getUserVekaletList() {
		return (ArrayList<User>) userVekaletList.clone();
	}

	public void setUserVekaletList(ArrayList<User> userVekaletList) {
		this.userVekaletList = userVekaletList;
	}

	@Transient
	public ArrayList<String> getPersonelGeciciNoList() {
		return (ArrayList<String>) (seciliSuperVisor == null ? personelGeciciNoList.clone() : seciliSuperVisor.getPersonelGeciciNoList().clone());
	}

	@Transient
	public ArrayList<String> getYetkiTumPersonelNoList() {
		if (yetkiTumPersonelNoList == null || yetkiTumPersonelNoList.isEmpty()) {
			yetkiTumPersonelNoList = new ArrayList<String>();
			if (yetkiliPersonelNoList.isEmpty())
				yetkiliPersonelNoList.add(getStaffId());
			yetkiTumPersonelNoList.addAll((ArrayList) yetkiliPersonelNoList.clone());

			if (personelGeciciNoList != null)
				for (String sicilNo : personelGeciciNoList) {
					if (!yetkiTumPersonelNoList.contains(sicilNo))
						yetkiTumPersonelNoList.add(sicilNo);
				}
			if (userVekaletList != null && !isMudur())
				for (User vekaletVeren : userVekaletList) {
					for (String sicilNo : vekaletVeren.getYetkiliPersonelNoList()) {
						if (!yetkiTumPersonelNoList.contains(sicilNo))
							yetkiTumPersonelNoList.add(sicilNo);
					}
				}

		}
		ArrayList<String> list = (ArrayList<String>) yetkiTumPersonelNoList.clone();

		return list;
	}

	public void setPersonelGeciciNoList(ArrayList<String> personelGeciciNoList) {
		this.personelGeciciNoList = personelGeciciNoList;
	}

	public void setYetkiTumPersonelNoList(ArrayList<String> yetkiTumPersonelNoList) {
		this.yetkiTumPersonelNoList = yetkiTumPersonelNoList;
	}

	@Transient
	public String getEmailFromUserName() {
		String ePosta = email;

		if (pdksPersonel != null && pdksPersonel.getSirket() != null && pdksPersonel.getSirket().isLdap()) {
			User ldapUser;

			try {
				if (username.indexOf("@") < 0)
					ldapUser = LDAPUserManager.getLDAPUserAttributes(username, LDAPUserManager.USER_ATTRIBUTES_SAM_ACCOUNT_NAME);
				else
					ldapUser = LDAPUserManager.getLDAPUserAttributes(username, LDAPUserManager.USER_ATTRIBUTES_MAIL);
				if (ldapUser == null || !ldapUser.isDurum())
					ldapUser = LDAPUserManager.getLDAPUserAttributes(username);
			} catch (Exception e) {
				ldapUser = null;
			}
			if (ldapUser != null && ldapUser.isDurum() && PdksUtil.hasStringValue(ldapUser.getEmail())) {
				ePosta = ldapUser.getEmail();
				username = ldapUser.getUsername();

			}

		}
		if (PdksUtil.hasStringValue(email) == false)
			ePosta = username;
		return ePosta;
	}

	@Transient
	public String getAdSoyad() {
		return pdksPersonel != null ? pdksPersonel.getAdSoyad() : "";
	}

	@Transient
	public User getSeciliSuperVisor() {
		return seciliSuperVisor;
	}

	public void setSeciliSuperVisor(User seciliSuperVisor) {
		this.seciliSuperVisor = seciliSuperVisor;
	}

	@Transient
	public Tanim getDepartmanTanim() {
		return departmanTanim;
	}

	public void setDepartmanTanim(Tanim departmanTanim) {
		this.departmanTanim = departmanTanim;
	}

	@Transient
	public String getDatePatern() {
		return PdksUtil.getMessageBundleMessage("ortak.format.tarih");
	}

	@Transient
	public String getYesNo(Boolean durum) {
		return durum != null ? PdksUtil.getMessageBundleMessage(durum != null && durum ? "ortak.etiket.evet" : "ortak.etiket.hayir") : "";
	}

	@Transient
	public static String getDurumAciklamaAna(Boolean durumu) {
		return durumu != null ? PdksUtil.getMessageBundleMessage(durumu != null && durumu ? "ortak.etiket.aktif" : "ortak.etiket.pasif") : "";
	}

	@Transient
	public String getDurumAciklama(Boolean durumu) {
		return getDurumAciklamaAna(durumu);
	}

	@Transient
	public String getTarihFormatla(Date tarih, String pattern) {
		String tarihStr = "";
		try {
			tarihStr = tarih != null ? PdksUtil.convertToDateString(tarih, pattern) : " ";
		} catch (Exception e) {
			tarihStr = "";
		}
		return tarihStr;
	}

	@Transient
	public String getSaatFormat() {
		String saatFormat = PdksUtil.getSaatFormat();
		return saatFormat;
	}

	@Transient
	public String timeFormatla(Date tarih) {
		String tarihStr = "", pattern = getSaatFormat();
		try {
			tarihStr = tarih != null ? PdksUtil.convertToDateString(tarih, pattern) : "";
		} catch (Exception e) {
			tarihStr = "";
		}
		return tarihStr;
	}

	@Transient
	public String timeLongFormatla(Date tarih) {
		String tarihStr = "", pattern = getSaatFormat() + ":ss";
		try {
			tarihStr = tarih != null ? PdksUtil.convertToDateString(tarih, pattern) : "";
		} catch (Exception e) {
			tarihStr = "";
		}
		return tarihStr;
	}

	@Transient
	public String dateFormatla(Date tarih) {
		String tarihStr = "", pattern = getDateFormat();
		try {
			tarihStr = tarih != null ? PdksUtil.convertToDateString(tarih, pattern) : "";
		} catch (Exception e) {
			tarihStr = "";
		}
		return tarihStr;
	}

	@Transient
	public String dateTimeFormatla(Date tarih) {
		String tarihStr = "", pattern = getDateTimeFormat();
		try {
			tarihStr = tarih != null ? PdksUtil.convertToDateString(tarih, pattern) : "";
		} catch (Exception e) {
			tarihStr = "";
		}
		return tarihStr;
	}

	@Transient
	public String dateTimeLongFormatla(Date tarih) {
		String tarihStr = "", pattern = getDateTimeLongFormat();
		try {
			tarihStr = tarih != null ? PdksUtil.convertToDateString(tarih, pattern) : " ";
		} catch (Exception e) {
			tarihStr = "";
		}
		return tarihStr;
	}

	@Transient
	public static Double getYuvarla(Double deger) {
		Double yeniDeger = null;
		if (deger != null) {
			if (PdksUtil.isDoubleValueNotLong(deger)) {
				yeniDeger = PdksUtil.setSureDoubleRounded(deger);
			} else
				yeniDeger = deger;
		}

		// logger.info(deger + " " + yeniDeger);
		return yeniDeger;
	}

	/**
	 * @param deger
	 * @param yarimYuvarla
	 * @return
	 */
	@Transient
	public static Double setSureDoubleTypeRounded(Double deger, int yarimYuvarla) {
		Double yeniDeger = null;
		if (deger != null) {
			if (PdksUtil.isDoubleValueNotLong(deger)) {
				yeniDeger = PdksUtil.setSureDoubleTypeRounded(deger, yarimYuvarla);
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
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// bu class cloneable oldugu icin buraya girilmemeli...
			throw new InternalError();
		}
	}

	@Transient
	public boolean isIKDirektor() {
		if (!yetkiSet)
			PdksUtil.setUserYetki(this);
		return IKDirektor;
	}

	public void setIKDirektor(Boolean direktor) {
		IKDirektor = direktor;
	}

	@Transient
	public String getRoller() {
		StringBuilder roller = new StringBuilder();
		if (yetkiliRollerim != null) {
			for (Iterator iterator = yetkiliRollerim.iterator(); iterator.hasNext();) {
				Role role = (Role) iterator.next();
				roller.append(role.getAciklama() + (iterator.hasNext() ? ", " : ""));
			}
		}

		String str = roller.toString();
		roller = null;
		return str;
	}

	@Transient
	public String sayiFormatliGoster(Object value) {
		String str = null;
		try {
			if (value != null)
				str = PdksUtil.numericValueFormatStr(value, null);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			str = null;
		}

		return str;
	}

	@Transient
	public List<Personel> getYetkiliPersoneller() {
		return yetkiliPersoneller;
	}

	@Transient
	public List<Long> getYetkiliPersonelIdler() {
		List<Long> idler = new ArrayList<Long>();
		try {
			ArrayList<Personel> tumPersoneller = getTumPersoneller();
			if (tumPersoneller != null) {
				for (Personel pdksPersonel : tumPersoneller) {
					if (pdksPersonel.getId() != null && !idler.contains(pdksPersonel.getId()))
						idler.add(pdksPersonel.getId());
				}

			}
		} catch (Exception e) {
		}

		return idler;
	}

	public void setYetkiliPersoneller(List<Personel> yetkiliPersoneller) {
		this.yetkiliPersoneller = yetkiliPersoneller;
	}

	@Transient
	public ArrayList<Personel> getTumPersoneller() {
		ArrayList<Personel> tumPersoneller = yetkiliPersoneller != null ? (ArrayList<Personel>) yetkiliPersoneller : null;
		return tumPersoneller != null ? (ArrayList<Personel>) tumPersoneller.clone() : new ArrayList<Personel>();
	}

	@Transient
	public Date getTarihUzat() {
		Date tarihUzat = PdksUtil.getSonSistemTarih();
		return tarihUzat;
	}

	@Transient
	public boolean isYetkiSet() {
		return yetkiSet;
	}

	@Transient
	public boolean isVardiyaDuzeltebilir() {
		boolean vardiyaDuzeltebilir = isIK() || (vardiyaDuzeltYetki != null && vardiyaDuzeltYetki.booleanValue());
		return vardiyaDuzeltebilir;
	}

	public void setYetkiSet(boolean yetkiSet) {
		this.yetkiSet = yetkiSet;
	}

	@Transient
	public boolean isIKAdmin() {
		boolean adminDurum = IK && departman != null && departman.isAdminMi();
		return adminDurum;
	}

	@Transient
	public boolean isDepartmentAdmin() {
		boolean departmentAdminDurum = departman != null && departman.isAdminMi();
		return departmentAdminDurum;
	}

	@Transient
	public boolean isOperatorSSK() {
		if (!yetkiSet)
			PdksUtil.setUserYetki(this);
		return operatorSSK;
	}

	public void setOperatorSSK(Boolean operatorSSK) {
		this.operatorSSK = operatorSSK;
	}

	@Transient
	public Boolean isIkinciYoneticiIzinOnaylasin() {
		Boolean ikinciYoneticiIzinOnayla = pdksPersonel != null ? pdksPersonel.getIkinciYoneticiIzinOnayla() : null;
		return ikinciYoneticiIzinOnayla == null || ikinciYoneticiIzinOnayla.booleanValue();
	}

	@Transient
	public List<String> getIkinciYoneticiPersonelSicilleri() {
		List<String> list = null;
		if (ikinciYoneticiPersonel != null && !ikinciYoneticiPersonel.isEmpty()) {
			list = new ArrayList();
			for (Iterator iterator = ikinciYoneticiPersonel.iterator(); iterator.hasNext();) {
				Personel personel = (Personel) iterator.next();
				list.add(personel.getSicilNo());
			}
		}

		return list;
	}

	@Transient
	public List<Personel> getIkinciYoneticiPersonel() {
		return ikinciYoneticiPersonel;
	}

	public void setIkinciYoneticiPersonel(List<Personel> ikinciYoneticiPersonel) {
		this.ikinciYoneticiPersonel = ikinciYoneticiPersonel;
	}

	@Transient
	public HttpSession getSession() {
		return session;
	}

	public void setSession(HttpSession session) {
		this.session = session;
	}

	@Transient
	public Boolean getSecili() {
		return secili;
	}

	public void setSecili(Boolean secili) {
		this.secili = secili;
	}

	@Transient
	public Boolean getLogin() {
		return login;
	}

	public void setLogin(Boolean login) {
		this.login = login;
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
	public boolean isYoneticiKontratli() {
		return yoneticiKontratli;
	}

	public void setYoneticiKontratli(Boolean yoneticiKontratli) {
		this.yoneticiKontratli = yoneticiKontratli;
	}

	@Transient
	public boolean isDirektorSuperVisor() {
		if (!yetkiSet)
			PdksUtil.setUserYetki(this);
		return direktorSuperVisor;
	}

	public void setDirektorSuperVisor(Boolean direktorSuperVisor) {
		this.direktorSuperVisor = direktorSuperVisor;
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
	public boolean isYeniSifre() {
		return yeniSifre;
	}

	public void setYeniSifre(boolean yeniSifre) {
		this.yeniSifre = yeniSifre;
	}

	@Transient
	public String getCalistigiSayfa() {
		return calistigiSayfa;
	}

	public void setCalistigiSayfa(String calistigiSayfa) {
		this.calistigiSayfa = calistigiSayfa;
	}

	@Transient
	public ArrayList<String> getSuperVisorHemsirePersonelNoList() {
		return superVisorHemsirePersonelNoList;
	}

	public void setSuperVisorHemsirePersonelNoList(ArrayList<String> value) {
		this.superVisorHemsirePersonelNoList = value;
	}

	@Transient
	public Session getSessionSQL() {
		return sessionSQL;
	}

	public void setSessionSQL(Session sessionSQL) {
		this.sessionSQL = sessionSQL;
	}

	@Transient
	public boolean isBrowserIE() {
		return browserIE;
	}

	public void setBrowserIE(boolean browserIE) {
		this.browserIE = browserIE;
	}

	@Transient
	public String getScroolClass() {
		String classAdi = !browserIE ? "scroolTable" : "scroolTableNo";
		return classAdi;
	}

	@Transient
	public static Date getTime(int saat, int dakika) {
		Date bugun = new Date();
		String patern = "yyyyMMdd";
		String str = PdksUtil.convertToDateString(bugun, patern);
		Date zaman = PdksUtil.convertToJavaDate(str + " " + saat + ":" + dakika, patern + " H:m");
		bugun = null;
		return zaman;
	}

	@Transient
	public String getScroolDiv() {
		String divAdi = !browserIE ? "wrap" : "tbl-container";
		return divAdi;
	}

	@Transient
	public MailPersonel getMailPersonel() {
		MailPersonel mailPersonel = new MailPersonel();
		mailPersonel.setEPosta(email);
		mailPersonel.setAdiSoyadi(this.getAdSoyad());
		return mailPersonel;
	}

	@Transient
	public String getRemoteAddr() {
		return remoteAddr;
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	@Transient
	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@Transient
	public boolean isIzinGirebilir() {
		return izinGirebilir;
	}

	public void setIzinGirebilir(boolean izinGirebilir) {
		this.izinGirebilir = izinGirebilir;
	}

	@Transient
	public ArrayList<String> getEskiPersonelList() {
		return eskiPersonelList;
	}

	public void setEskiPersonelList(ArrayList<String> eskiPersonelList) {
		this.eskiPersonelList = eskiPersonelList;
	}

	@Transient
	public boolean isIzinSSKGirebilir() {
		return izinSSKGirebilir;
	}

	public void setIzinSSKGirebilir(boolean izinSSKGirebilir) {
		this.izinSSKGirebilir = izinSSKGirebilir;
	}

	@Transient
	public UserMenuItemTime getMenuItemTime() {
		return menuItemTime;
	}

	public void setMenuItemTime(UserMenuItemTime menuItemTime) {
		this.menuItemTime = menuItemTime;
	}

	@Transient
	public boolean isSistemYoneticisi() {
		return sistemYoneticisi;
	}

	public void setSistemYoneticisi(Boolean sistemYoneticisi) {
		this.sistemYoneticisi = sistemYoneticisi;
	}

	@Transient
	public List<Tanim> getYetkiliTesisler() {
		return yetkiliTesisler;
	}

	public void setYetkiliTesisler(List<Tanim> yetkiliTesisler) {
		this.yetkiliTesisler = yetkiliTesisler;
	}

	@Transient
	public boolean isIzinOnaylayabilir() {
		return izinOnaylayabilir;
	}

	public void setIzinOnaylayabilir(boolean izinOnaylayabilir) {
		this.izinOnaylayabilir = izinOnaylayabilir;
	}

	@Transient
	public boolean isTestLogin() {
		return testLogin;
	}

	public void setTestLogin(boolean testLogin) {
		this.testLogin = testLogin;
	}

	@Transient
	public String getDateFormat() {
		String dateFormat = PdksUtil.getDateFormat();
		return dateFormat;
	}

	@Transient
	public String getDateTimeFormat() {
		String dateTimeFormat = PdksUtil.getDateTimeFormat();
		return dateTimeFormat;
	}

	@Transient
	private String getDateTimeLongFormat() {
		String dateTimeLongFormat = PdksUtil.getDateTimeLongFormat();
		return dateTimeLongFormat;
	}

	@Transient
	public boolean isAnahtarKullanici() {
		if (!yetkiSet)
			PdksUtil.setUserYetki(this);
		return anahtarKullanici;
	}

	public void setAnahtarKullanici(boolean anahtarKullanici) {
		this.anahtarKullanici = anahtarKullanici;
	}

	@Transient
	public boolean isRaporKullanici() {
		if (!yetkiSet)
			PdksUtil.setUserYetki(this);
		return raporKullanici;
	}

	public void setRaporKullanici(boolean raporKullanici) {
		this.raporKullanici = raporKullanici;
	}

	@Transient
	public String getParametreJSON() {
		return parametreJSON;
	}

	public void setParametreJSON(String parametreJSON) {
		this.parametreJSON = parametreJSON;
	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}

}