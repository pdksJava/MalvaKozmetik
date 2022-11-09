package org.pdks.entity;

import java.util.Date;

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

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = IsKurVardiyaGun.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { IsKurVardiyaGun.COLUMN_NAME_VARDIYA_TARIHI, IsKurVardiyaGun.COLUMN_NAME_PERSONEL }) })
public class IsKurVardiyaGun extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7206334028069712721L;

	static Logger logger = Logger.getLogger(IsKurVardiyaGun.class);

	public static final String TABLE_NAME = "ISKUR_VARDIYA_GUN";
	public static final String COLUMN_NAME_PERSONEL = "PERSONEL_ID";
	public static final String COLUMN_NAME_VARDIYA_TARIHI = "VARDIYA_TARIHI";
	public static final String COLUMN_NAME_VARDIYA = "VARDIYA_ID";
	public static final String COLUMN_NAME_PERSONEL_NO = "PERSONEL_NO";

	private Personel personel;
	private Vardiya vardiya;
	private Date vardiyaDate;
	private String personelNo;
	private Integer version = 0;

	public IsKurVardiyaGun() {
		super();

	}

	public IsKurVardiyaGun(Personel personel, Vardiya vardiya, Date vardiyaDate) {
		super();
		this.personel = personel;
		this.vardiya = vardiya;
		this.vardiyaDate = vardiyaDate;
	}

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
	@JoinColumn(name = COLUMN_NAME_VARDIYA, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Vardiya getVardiya() {
		return vardiya;
	}

	public void setVardiya(Vardiya value) {
		this.vardiya = value;
	}

	@Temporal(value = TemporalType.DATE)
	@Column(name = COLUMN_NAME_VARDIYA_TARIHI, nullable = false)
	public Date getVardiyaDate() {
		return vardiyaDate;
	}

	public void setVardiyaDate(Date vardiyaDate1) {
		this.vardiyaDate = vardiyaDate1;
	}

	@Column(name = COLUMN_NAME_PERSONEL_NO, insertable = false, updatable = false)
	public String getPersonelNo() {
		return personelNo;
	}

	public void setPersonelNo(String personelNo) {
		this.personelNo = personelNo;
	}

	@Transient
	public VardiyaGun getVardiyaGun() {
		VardiyaGun vardiyaGun = new VardiyaGun(personel, vardiya, vardiyaDate);
		vardiyaGun.setIsKurVardiya(this);
		return vardiyaGun;
	}

}
