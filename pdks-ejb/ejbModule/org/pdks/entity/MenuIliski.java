package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = MenuIliski.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { MenuIliski.COLUMN_NAME_CHILD_MENU_ITEM }) })
public class MenuIliski extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5982131290456581936L;
	/**
	 * 
	 */

	public static final String TABLE_NAME = "MENUITEM_MENUITEM";
	public static final String COLUMN_NAME_MENU_ITEM = "MENUITEM_ID";
	public static final String COLUMN_NAME_CHILD_MENU_ITEM = "CHILDMENUITEMLIST_ID";

	private MenuItem menuItem;

	private MenuItem childMenuItem;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_MENU_ITEM, nullable = false)
	@Fetch(FetchMode.JOIN)
	public MenuItem getMenuItem() {
		return menuItem;
	}

	public void setMenuItem(MenuItem menuItem) {
		this.menuItem = menuItem;
	}

	@OneToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_CHILD_MENU_ITEM, nullable = false)
	@Fetch(FetchMode.JOIN)
	public MenuItem getChildMenuItem() {
		return childMenuItem;
	}

	public void setChildMenuItem(MenuItem childMenuItem) {
		this.childMenuItem = childMenuItem;
	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}
}
