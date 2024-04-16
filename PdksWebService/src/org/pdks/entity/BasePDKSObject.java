package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

@MappedSuperclass
public abstract class BasePDKSObject implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3045809958792260856L;

	static Logger logger = Logger.getLogger(BasePDKSObject.class);

	public static final String COLUMN_NAME_ID = "ID";

	protected Long id;

	protected boolean degisti = Boolean.FALSE;
	
	protected Boolean guncellendi;

	@Id
	@GeneratedValue
	@Column(name = COLUMN_NAME_ID)
	public Long getId() {
		return id;
	}

	public void setId(Long value) {
		if (value != null)
			this.degisti = true;
		this.id = value;
	}

	@Transient
	public boolean isDegisti() {
		return degisti;
	}

	public void setDegisti(boolean value) {
		if (value) {
			// if (id != null)
			// logger.debug(id);
		}

		this.degisti = value;
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
		Object object = null;
		try {
			this.setId(null);
			object = super.clone();
		} catch (CloneNotSupportedException e) {

		}
		return object;
	}

}
