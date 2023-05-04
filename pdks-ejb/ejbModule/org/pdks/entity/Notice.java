package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.security.entity.User;
import org.pdks.session.PdksUtil;

@Entity(name = "NOTICE")
public class Notice  extends BasePDKSObject  implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3459223276596848291L;
	// seam-gen attributes (you should probably edit these)
	 
	private Integer version = 0;
	private String name = "";
	private String value = "";
	private String description = "";
	private Boolean active = Boolean.TRUE;
	private User changeUser;
	private Date changeDate;

	 

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Column(name = "NAME", length = 20)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "VALUE")
	public String getValue() {
		return value;
	}

	public void setValue(String aciklama) {
		// aciklama = aciklama != null ? PdksUtil.getHtmlAciklama(aciklama) : "";
		this.value = aciklama;
	}

	@Lob
	@Column(name = "DESCRIPTION")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		description = PdksUtil.convertUTF8(description);
		this.description = description;
	}

	@Column(name = "ACTIVE")
	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "CHANGEUSER_ID", nullable = false, unique = true)
	@Fetch(FetchMode.JOIN)
	public User getChangeUser() {
		return changeUser;
	}

	public void setChangeUser(User changeUser) {
		this.changeUser = changeUser;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "CHANGEDATE")
	public Date getChangeDate() {
		return changeDate;
	}

	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}

	@Transient
	public String getStringHTML() {
		String aciklama = value != null ? PdksUtil.getHtmlAciklama(value) : "";
		return aciklama;
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
