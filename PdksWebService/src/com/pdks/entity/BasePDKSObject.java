package com.pdks.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@MappedSuperclass
public abstract class BasePDKSObject implements Serializable, Cloneable {

 
	/**
	 * 
	 */
	private static final long serialVersionUID = 3045809958792260856L;


	public static final String COLUMN_NAME_ID = "ID";

 
	protected Long id;

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
