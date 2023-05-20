package com.pdks.entity;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = Role.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { Role.COLUMN_NAME_ROLE_NAME }) })
public class Role implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7832116917666289661L;
	public static final String TABLE_NAME = "ROLE";
	public static final String COLUMN_NAME_ID = "ID";
	public static final String COLUMN_NAME_ROLE_NAME = "ROLENAME";
	public static final String COLUMN_NAME_DEPARTMAN = "DEPARTMAN_ID";
	public static final String COLUMN_NAME_STATUS = "STATUS";
	public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";

	public static final String TIPI_ADMIN = "admin";
	public static final String TIPI_GENEL_MUDUR = "genelMudur";
	public static final String TIPI_IK = "IK";
	public static final String TIPI_IK_DIREKTOR = "IKDirektor";
	public static final String TIPI_PROJE_MUDURU = "projeMuduru";
	public static final String TIPI_SEKRETER = "sekreter";
	public static final String TIPI_SUPER_VISOR = "superVisor";
	public static final String TIPI_DEPARTMAN_SUPER_VISOR = "superVisorHemsire";
	public static final String TIPI_YONETICI = "yonetici";
	public static final String TIPI_YONETICI_KONTRATLI = "yoneticiKontratli";
	public static final String TIPI_PERSONEL = "personel";
	public static final String TIPI_MUDUR = "mudur";
	public static final String TIPI_OPERATOR_SSK_IZIN = "operatorSSKIzin";
	public static final String TIPI_YEMEKHANE = "yemekHane";

	private Integer id;
	private String rolename;
	private String aciklama;
	private Set<Role> groups;
	private Boolean status = Boolean.TRUE;
	private Departman departman;

	@Id
	@GeneratedValue
	@Column(name = "ID")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = COLUMN_NAME_ROLE_NAME)
	public String getRolename() {
		return rolename;
	}

	public void setRolename(String rolename) {
		this.rolename = rolename;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_DEPARTMAN)
	@Fetch(FetchMode.JOIN)
	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman departman) {
		this.departman = departman;
	}

	@ManyToMany(targetEntity = Role.class)
	@JoinTable(name = "ROLEGROUPS", joinColumns = @JoinColumn(name = "ROLEID"), inverseJoinColumns = @JoinColumn(name = "GROUPID"))
	public Set<Role> getGroups() {
		return groups;
	}

	public void setGroups(Set<Role> groups) {
		this.groups = groups;
	}

	/**
	 * @return the status
	 */
	@Column(name = COLUMN_NAME_STATUS)
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

	@Column(name = COLUMN_NAME_ACIKLAMA)
	public String getAciklama() {
		String str = aciklama != null ? aciklama : rolename;
		return str;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

}