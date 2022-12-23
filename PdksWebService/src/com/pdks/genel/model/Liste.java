/*
 * Created on 20.�ub.2006
 * 
 *  Window - Preferences - Java - Code Style - Code Templates
 */
package com.pdks.genel.model;

import java.io.Serializable;

public class Liste implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6524659265523638244L;

	private Object key;

	private String id;

	private Object value;

	private String selected = "";

	private String checked = "";

	public Liste(Object xkey, Object xvalue) {
		if (xkey instanceof String)
			this.setId((String) xkey);
		this.setKey(xkey);
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
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return Returns the key.
	 */
	public Object getKey() {
		return key;
	}

	/**
	 * @param key
	 *            The key to set.
	 */
	public void setKey(Object key) {
		this.key = key;
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
}