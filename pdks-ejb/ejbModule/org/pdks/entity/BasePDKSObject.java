package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.pdks.security.entity.User;

@MappedSuperclass
public abstract class BasePDKSObject implements Serializable, Cloneable,PdksInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3045809958792260856L;

	public static final String COLUMN_NAME_ID = "ID";

	protected Long id;

	protected User loginUser;

	protected Boolean guncellendi;

	@Id
	@GeneratedValue
	@Column(name = COLUMN_NAME_ID)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Transient
	public User getLoginUser() {
		return loginUser;
	}

	public void setLoginUser(User loginUser) {
		this.loginUser = loginUser;
	}

	@Transient
	public long getIdLong() {
		long value = id != null ? id.longValue() : 0;
		return value;
	}

	@Transient
	public Boolean getGuncellendi() {
		return guncellendi;
	}

	public void setGuncellendi(Boolean value) {

		this.guncellendi = value;
	}

	@Transient
	public Boolean isGuncellendi() {
		return guncellendi != null && guncellendi.booleanValue();
	}

	@Transient
	public Object cloneEmpty() {
		BasePDKSObject object = null;
		try {
			object = (BasePDKSObject) super.clone();
			object.setId(null);
		} catch (CloneNotSupportedException e) {

		}
		return object;
	}

}
