package org.pdks.security.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = UserRoles.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { UserRoles.COLUMN_NAME_USER, UserRoles.COLUMN_NAME_ROLE }) })
public class UserRoles implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1425317555516469129L;
	public static final String TABLE_NAME = "USERROLES";
	public static final String COLUMN_NAME_ID = "ID";
	public static final String COLUMN_NAME_USER = "USERID";
	public static final String COLUMN_NAME_ROLE = "ROLEID";

	private Long id;

	private User user;

	private Role role;

	@Id
	@GeneratedValue
	@Column(name = COLUMN_NAME_ID)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
	@JoinColumn(name = COLUMN_NAME_ROLE, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
}
