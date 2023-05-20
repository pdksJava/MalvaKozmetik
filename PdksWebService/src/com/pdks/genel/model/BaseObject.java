package com.pdks.genel.model;

import java.io.Serializable;

/**
 * Base class for Model objects. This is basically for the toString, equals and hashCode methods.
 * 
 * @author Hasan Sayar
 */
public class BaseObject implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1519023471643548831L;

	/**
	 * overrides derived objects' toString method and displays more meaningful information about the object
	 * 
	 * @return property values of the instance
	 */
	/*
	 * public String toString() { return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE); }
	 */
	/*
	 * public boolean equals(Object o) { return EqualsBuilder.reflectionEquals(this, o); }
	 * 
	 * public int hashCode() { return HashCodeBuilder.reflectionHashCode(this); }
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// bu class cloneable oldugu icin buraya girilmemeli...
			throw new InternalError();
		}
	}
}
