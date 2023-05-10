package org.pdks.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.security.entity.UserMenuItemTime;
import org.pdks.session.PdksUtil;

@Entity(name = MenuItem.TABLE_NAME)
public class MenuItem extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6647696654599482807L;
	public static final String TABLE_NAME = "MENUITEM";
	public static final String COLUMN_NAME_ADI = "NAME";

	private String name = "";
	private List<MenuItem> childMenuItemList = new ArrayList<MenuItem>();;
	private Tanim description;
	private Boolean topMenu = Boolean.FALSE;
	private int orderNo;
	private Integer version;
	private Boolean status = Boolean.TRUE;
	private Boolean check = Boolean.FALSE;
	private MenuItem parentMenuItem;
	private List<UserMenuItemTime> menuItemTimeList;

	/**
	 * @return the name
	 */
	@Column(name = COLUMN_NAME_ADI, unique = true)
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "DESCRIPTION_ID", nullable = false)
	@Fetch(FetchMode.JOIN)
	public Tanim getDescription() {
		if (description == null)
			description = new Tanim();
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(Tanim description) {
		this.description = description;
	}

	/**
	 * @return the topMenu
	 */
	@Column(name = "TOPMENU")
	public Boolean getTopMenu() {

		return topMenu;
	}

	/**
	 * @param topMenu
	 *            the topMenu to set
	 */
	public void setTopMenu(Boolean topMenu) {
		this.topMenu = topMenu;
	}

	/**
	 * @return the version
	 */
	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}

	/**
	 * @return the status
	 */
	@Column(name = "STATUS")
	public Boolean getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(Boolean status) {
		this.status = status;
	}

	/**
	 * @return the childMenuItemList
	 */
	@OneToMany(cascade = CascadeType.ALL, targetEntity = MenuItem.class, fetch = FetchType.EAGER)
	@JoinTable(name = MenuIliski.TABLE_NAME, joinColumns = @JoinColumn(name = MenuIliski.COLUMN_NAME_MENU_ITEM), inverseJoinColumns = @JoinColumn(name = MenuIliski.COLUMN_NAME_CHILD_MENU_ITEM))
	public List<MenuItem> getChildMenuItemList() {
		return childMenuItemList;
	}

	/**
	 * @OneToMany(cascade = CascadeType.ALL, targetEntity = ExampleObject.class, fetch = FetchType.EAGER)
	 * @JoinColumn(name = "CHILD_OBJECT_ID", referencedColumnName = "EXAMPLE_OBJECT_ID")
	 * 
	 * @param childMenuItemList
	 *            the childMenuItemList to set
	 */
	public void setChildMenuItemList(List<MenuItem> childMenuItemList) {
		this.childMenuItemList = childMenuItemList;
	}

	@Transient
	public void addChildMenuItemList(MenuItem menuItem) {
		this.getChildMenuItemList().add(menuItem);
	}

	@Transient
	public void removeFromChildMenuItem(MenuItem tempMenuItem) {
		this.getChildMenuItemList().remove(tempMenuItem);
	}

	@Transient
	public boolean equals(MenuItem menuItem) {

		if (menuItem != null && this.getId() == menuItem.getId())
			return true;
		else
			return false;
	}

	@Column(name = "ORDERNO")
	public int getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(int orderNo) {
		this.orderNo = orderNo;
	}

	@Transient
	public Boolean getCheck() {
		return check;
	}

	public void setCheck(Boolean check) {
		this.check = check;
	}

	@Transient
	public MenuItem getParentMenuItem() {
		return parentMenuItem;
	}

	public void setParentMenuItem(MenuItem parentMenuItem) {
		this.parentMenuItem = parentMenuItem;
	}

	@Transient
	public List<MenuItem> getChildMenuItemListSirali() {
		List<MenuItem> list = (List<MenuItem>) PdksUtil.sortListByAlanAdi(childMenuItemList, "orderNo", Boolean.FALSE);
		return list;
	}

	@Transient
	public List<UserMenuItemTime> getMenuItemTimeList() {
		return menuItemTimeList;
	}

	public void setMenuItemTimeList(List<UserMenuItemTime> menuItemTimeList) {
		this.menuItemTimeList = menuItemTimeList;
	}
}
