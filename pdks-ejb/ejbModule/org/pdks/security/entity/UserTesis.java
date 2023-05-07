package org.pdks.security.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.entity.BasePDKSObject;
import org.pdks.entity.Tanim;

@Entity(name = UserTesis.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { UserTesis.COLUMN_NAME_USER, UserTesis.COLUMN_NAME_TESIS }) })
public class UserTesis extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6676275523552065087L;
	public static final String TABLE_NAME = "USER_TESIS";
 	public static final String COLUMN_NAME_USER = "USER_ID";
	public static final String COLUMN_NAME_TESIS = "TESIS_ID";

	private User user;

	private Tanim tesis;

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
	@JoinColumn(name = COLUMN_NAME_TESIS, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Tanim getTesis() {
		return tesis;
	}

	public void setTesis(Tanim tesis) {
		this.tesis = tesis;
	}

}
