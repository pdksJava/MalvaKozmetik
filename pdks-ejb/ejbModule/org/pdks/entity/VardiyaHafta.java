package org.pdks.entity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.pdks.session.PdksUtil;
import org.pdks.session.Constants;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = VardiyaHafta.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { VardiyaHafta.COLUMN_NAME_BAS_TARIH, VardiyaHafta.COLUMN_NAME_PERSONEL }) })
public class VardiyaHafta extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5645273176698644912L;

	public static final String TABLE_NAME = "VARDIYAHAFTA";
	public static final String COLUMN_NAME_PERSONEL = "PERSONEL_ID";
	public static final String COLUMN_NAME_BAS_TARIH = "BAS_TARIH";
	public static final String COLUMN_NAME_BIT_TARIH = "BIT_TARIH";

	private Personel personel;
	private VardiyaSablonu vardiyaSablonu;
	private Date basTarih, bitTarih;
	private VardiyaGun vardiyaGun1, vardiyaGun2, vardiyaGun3, vardiyaGun4, vardiyaGun5, vardiyaGun6, vardiyaGun7;
	private List<VardiyaGun> vardiyaGunler = new ArrayList<VardiyaGun>();
	private List<Vardiya> vardiyalar = new ArrayList<Vardiya>();
	private VardiyaPlan vardiyaPlan;
	private int hafta;
	private String trClass;
	private Integer version = 0;
	private Boolean guncellendi;

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Personel getPersonel() {
		return personel;
	}

	public void setPersonel(Personel personel) {
		this.personel = personel;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "SABLON_ID", nullable = false)
	@Fetch(FetchMode.JOIN)
	public VardiyaSablonu getVardiyaSablonu() {
		return vardiyaSablonu;
	}

	public void setVardiyaSablonu(VardiyaSablonu vardiyaSablonu) {
		this.vardiyaSablonu = vardiyaSablonu;
	}

	@Temporal(value = TemporalType.DATE)
	@Column(name = COLUMN_NAME_BAS_TARIH, nullable = false)
	public Date getBasTarih() {
		return basTarih;
	}

	public void setBasTarih(Date basTarih) {

		this.basTarih = basTarih;
	}

	@Temporal(value = TemporalType.DATE)
	@Column(name = COLUMN_NAME_BIT_TARIH, nullable = false)
	public Date getBitTarih() {
		return bitTarih;
	}

	public void setBitTarih(Date bitTarih) {
		this.bitTarih = bitTarih;
	}

	@Transient
	public int getHafta() {
		return hafta;
	}

	public void setHafta(int hafta) {
		this.hafta = hafta;
	}

	@Transient
	public long getPersonelId() {
		long perId = personel != null ? personel.getId() : 0;
		return perId;
	}

	@Transient
	public String getKey() {
		if (hafta == 0) {
			Calendar cal = new GregorianCalendar(Constants.TR_LOCALE);
			cal.setFirstDayOfWeek(Calendar.MONDAY);
			cal.setTime(basTarih);
			int haftaDeger = cal.get(Calendar.YEAR) * 100 + cal.get(Calendar.WEEK_OF_YEAR);
			setHafta(haftaDeger);
		}
		return getPersonelId() + "_" + getHafta();
	}

	@Transient
	public String getDateKey() {
		String dateKey = getPersonelId() + "_" + PdksUtil.convertToDateString(basTarih, "yyyyMMdd");
		return dateKey;
	}

	@Transient
	public VardiyaGun getVardiyaGun1() {
		return vardiyaGun1;
	}

	public void setVardiyaGun1(VardiyaGun vardiyaGun1) {
		if (vardiyaPlan != null && vardiyaGun1 != null)
			vardiyaPlan.getVardiyaGunMap().put(vardiyaGun1.getVardiyaDateStr(), vardiyaGun1);
		this.vardiyaGun1 = vardiyaGun1;
	}

	@Transient
	public VardiyaGun getVardiyaGun2() {
		return vardiyaGun2;
	}

	public void setVardiyaGun2(VardiyaGun vardiyaGun2) {
		if (vardiyaPlan != null && vardiyaGun2 != null)
			vardiyaPlan.getVardiyaGunMap().put(vardiyaGun2.getVardiyaDateStr(), vardiyaGun2);
		this.vardiyaGun2 = vardiyaGun2;
	}

	@Transient
	public VardiyaGun getVardiyaGun3() {
		return vardiyaGun3;
	}

	public void setVardiyaGun3(VardiyaGun vardiyaGun3) {
		if (vardiyaPlan != null && vardiyaGun3 != null)
			vardiyaPlan.getVardiyaGunMap().put(vardiyaGun3.getVardiyaDateStr(), vardiyaGun3);
		this.vardiyaGun3 = vardiyaGun3;
	}

	@Transient
	public VardiyaGun getVardiyaGun4() {
		return vardiyaGun4;
	}

	public void setVardiyaGun4(VardiyaGun vardiyaGun4) {
		if (vardiyaPlan != null && vardiyaGun4 != null)
			vardiyaPlan.getVardiyaGunMap().put(vardiyaGun4.getVardiyaDateStr(), vardiyaGun4);
		this.vardiyaGun4 = vardiyaGun4;
	}

	@Transient
	public VardiyaGun getVardiyaGun5() {
		return vardiyaGun5;
	}

	public void setVardiyaGun5(VardiyaGun vardiyaGun5) {
		if (vardiyaPlan != null && vardiyaGun5 != null)
			vardiyaPlan.getVardiyaGunMap().put(vardiyaGun5.getVardiyaDateStr(), vardiyaGun5);
		this.vardiyaGun5 = vardiyaGun5;
	}

	@Transient
	public VardiyaGun getVardiyaGun6() {
		return vardiyaGun6;
	}

	public void setVardiyaGun6(VardiyaGun vardiyaGun6) {
		if (vardiyaPlan != null && vardiyaGun6 != null)
			vardiyaPlan.getVardiyaGunMap().put(vardiyaGun6.getVardiyaDateStr(), vardiyaGun6);
		this.vardiyaGun6 = vardiyaGun6;
	}

	@Transient
	public VardiyaGun getVardiyaGun7() {
		return vardiyaGun7;
	}

	public void setVardiyaGun7(VardiyaGun vardiyaGun7) {
		if (vardiyaPlan != null && vardiyaGun7 != null)
			vardiyaPlan.getVardiyaGunMap().put(vardiyaGun7.getVardiyaDateStr(), vardiyaGun7);
		this.vardiyaGun7 = vardiyaGun7;
	}

	@Transient
	public List<VardiyaGun> getVardiyaGunler() {

		return vardiyaGunler;
	}

	public void setVardiyaGunler(List<VardiyaGun> vardiyaGunler) {
		this.vardiyaGunler = vardiyaGunler;
	}

	@Transient
	public List<Vardiya> getVardiyalar() {
		return vardiyalar;
	}

	public void setVardiyalar(List<Vardiya> vardiyalar) {
		this.vardiyalar = vardiyalar;
	}

	@Transient
	public String getKeyHafta() {
		String key = (personel != null ? "" + personel.getId() : "") + "_" + (basTarih != null ? PdksUtil.convertToDateString(basTarih, "yyyyMMdd") : "");
		return key;
	}

	@Transient
	public VardiyaPlan getVardiyaPlan() {
		return vardiyaPlan;
	}

	@Transient
	public VardiyaGun getVardiyaGun(Vardiya vardiya, int gunu) {
		VardiyaGun pdksVardiyaGun = new VardiyaGun(personel, vardiya, PdksUtil.tariheGunEkleCikar(basTarih, gunu - 1));
		return pdksVardiyaGun;
	}

	@Transient
	public void setHaftalikVardiyaPlan() {
		if (vardiyaGunler == null)
			vardiyaGunler = new ArrayList<VardiyaGun>();
		else
			vardiyaGunler.clear();
		for (int i = 1; i <= 7; i++) {
			Vardiya pdksVardiya = (Vardiya) PdksUtil.getMethodObject(vardiyaSablonu, "getVardiya" + i, null);
			VardiyaGun pdksVardiyaGun = getVardiyaGun(pdksVardiya, i);
			vardiyaGunler.add(pdksVardiyaGun);
		}

	}

	@Transient
	public void setVardiyaPlan(VardiyaPlan vardiyaPlan) {
		if (vardiyaPlan != null && vardiyaSablonu != null) {
			setVardiyaGun1(getVardiyaGun(vardiyaSablonu.getVardiya1(), 1));
			setVardiyaGun2(getVardiyaGun(vardiyaSablonu.getVardiya2(), 2));
			setVardiyaGun3(getVardiyaGun(vardiyaSablonu.getVardiya3(), 3));
			setVardiyaGun4(getVardiyaGun(vardiyaSablonu.getVardiya4(), 4));
			setVardiyaGun5(getVardiyaGun(vardiyaSablonu.getVardiya5(), 5));
			setVardiyaGun6(getVardiyaGun(vardiyaSablonu.getVardiya6(), 6));
			setVardiyaGun7(getVardiyaGun(vardiyaSablonu.getVardiya7(), 7));
		}
		this.vardiyaPlan = vardiyaPlan;

	}

	@Transient
	public String getTrClass() {
		return trClass;
	}

	public void setTrClass(String trClass) {
		this.trClass = trClass;
	}

	@Transient
	public Boolean getGuncellendi() {
		return guncellendi;
	}

	public void setGuncellendi(Boolean guncellendi) {
		this.guncellendi = guncellendi;
	}

	@Transient
	public Personel getPdksPersonel() {
		return personel;
	}
}
