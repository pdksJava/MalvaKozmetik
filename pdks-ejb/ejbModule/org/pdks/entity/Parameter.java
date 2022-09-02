package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.security.entity.User;
import org.pdks.session.PdksUtil;

@Entity(name = Parameter.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { Parameter.COLUMN_NAME_ADI }) })
public class Parameter implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2588841939050465016L;
	public static final String TABLE_NAME = "PARAMETRE";
	public static final String COLUMN_NAME_ADI = "ADI";
	public static final String COLUMN_NAME_DEGER = "DEGER";
	public static final String COLUMN_NAME_DURUM = "DURUM";
	public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";
	public static final String COLUMN_NAME_HELP_DESK = "HDSK";

	// seam-gen attributes (you should probably edit these)
	private Long id;
	private Integer version = 0;
	private String name = "";
	private String value = "";
	private String description = "";
	private Boolean active = Boolean.TRUE, helpDesk = Boolean.FALSE;
	private User changeUser;
	private Date changeDate;
	private Boolean guncelle = Boolean.TRUE;

	// add additional entity attributes

	// seam-gen attribute getters/setters with annotations (you probably should edit)

	@Id
	@GeneratedValue
	@Column(name = "ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Column(name = COLUMN_NAME_ADI, length = 64)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = COLUMN_NAME_DEGER)
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Column(name = COLUMN_NAME_ACIKLAMA)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		description = PdksUtil.convertUTF8(description);
		this.description = description;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	@JoinColumn(name = "CHANGEUSER_ID", nullable = true)
	@Fetch(FetchMode.JOIN)
	public User getChangeUser() {
		return changeUser;
	}

	public void setChangeUser(User changeUser) {
		this.changeUser = changeUser;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CHANGEDATE")
	public Date getChangeDate() {
		return changeDate;
	}

	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}

	@Column(name = "GUNCELLE")
	public Boolean getGuncelle() {
		return guncelle;
	}

	public void setGuncelle(Boolean guncelle) {
		this.guncelle = guncelle;
	}

	@Column(name = COLUMN_NAME_HELP_DESK)
	public Boolean getHelpDesk() {
		return helpDesk;
	}

	public void setHelpDesk(Boolean helpDesk) {
		this.helpDesk = helpDesk;
	}

	@Transient
	public boolean isHelpDeskMi() {
		return helpDesk != null && helpDesk.booleanValue();
	}

}
