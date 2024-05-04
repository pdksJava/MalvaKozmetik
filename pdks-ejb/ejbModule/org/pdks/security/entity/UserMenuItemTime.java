package org.pdks.security.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.entity.BasePDKSObject;
import org.pdks.entity.MenuItem;

@Entity(name = UserMenuItemTime.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { UserMenuItemTime.COLUMN_NAME_USER, UserMenuItemTime.COLUMN_NAME_MENU, }) })
public class UserMenuItemTime extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8339875569540893616L;

	public static final String TABLE_NAME = "USER_MENUITEM_TIME";
	public static final String COLUMN_NAME_USER = "USER_ID";
	public static final String COLUMN_NAME_MENU = "MENU_ID";
	public static final String COLUMN_NAME_LAST_PARAMETRE = "LAST_PARAMETRE";

	private User user;

	private MenuItem menu;

	private String sessionId, parametreJSON;

	private Date firstTime, lastTime;

	private BigDecimal useCount = new BigDecimal(0L);

	public UserMenuItemTime() {
		super();

	}

	public UserMenuItemTime(User user, MenuItem menu) {
		super();
		this.user = user;
		this.menu = menu;
		this.sessionId = "";
		this.useCount = new BigDecimal(0L);
		this.lastTime = new Date();
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_USER, nullable = false)
	@Fetch(FetchMode.JOIN)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_MENU, nullable = false)
	@Fetch(FetchMode.JOIN)
	public MenuItem getMenu() {
		return menu;
	}

	public void setMenu(MenuItem menu) {
		this.menu = menu;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FIRST_TIME")
	public Date getFirstTime() {
		return firstTime;
	}

	public void setFirstTime(Date firstTime) {
		this.firstTime = firstTime;
	}

	@Column(name = "SESSION_ID")
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Column(name = COLUMN_NAME_LAST_PARAMETRE)
	public String getParametreJSON() {
		return parametreJSON;
	}

	public void setParametreJSON(String parametreJSON) {
		this.parametreJSON = parametreJSON;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_TIME")
	public Date getLastTime() {
		return lastTime;
	}

	public void setLastTime(Date lastTime) {
		this.lastTime = lastTime;
	}

	@Column(name = "USE_COUNT")
	public BigDecimal getUseCount() {
		return useCount;
	}

	public void setUseCount(BigDecimal useCount) {
		this.useCount = useCount;
	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}

}
