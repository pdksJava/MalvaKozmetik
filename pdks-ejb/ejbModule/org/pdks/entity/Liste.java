/*
 * Created on 20.�ub.2006
 *
 *  
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.pdks.entity;

import java.io.Serializable;

/**
 * @author Hasan Sayar
 * 
 * 
 */
public class Liste implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 650491322678298892L;

	private Object id;

	private Object value;
	private String selected = "";
	private String checked = "";
	private Object numValue;

	public Liste(Object xId, Object xvalue) {
		this.setId(xId);
		this.setValue(xvalue);
	}

	/**
	 * @return Returns the checked.
	 */
	public String getChecked() {
		return checked;
	}

	/**
	 * @param checked
	 *            The checked to set.
	 */
	public void setChecked(String checked) {
		this.checked = checked;
	}

	/**
	 * @return Returns the selected.
	 */
	public String getSelected() {
		return selected;
	}

	/**
	 * @param selected
	 *            The selected to set.
	 */
	public void setSelected(String selected) {
		this.selected = selected;
	}

	/**
	 * @return Returns the value.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value
	 *            The value to set.
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public Object getNumValue() {
		return numValue;
	}

	public void setNumValue(Object numValue) {
		this.numValue = numValue;
	}

	@Override
	public String toString() {
		return id.toString();
	}

	@Override
	public boolean equals(Object obj) {
		boolean rt = Boolean.FALSE;
		if (obj instanceof Liste) {
			String key = (String) ((Liste) obj).getId();
			rt = this.id.equals(key);
		}

		return rt;

	}

}
