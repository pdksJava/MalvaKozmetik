package org.pdks.security.entity;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.jboss.seam.annotations.security.management.RoleConditional;
import org.jboss.seam.annotations.security.management.RoleGroups;
import org.jboss.seam.annotations.security.management.RoleName;
import org.pdks.entity.BasePDKSObject;
import org.pdks.entity.Departman;
import org.pdks.session.PdksUtil;

@Entity(name = Role.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { Role.COLUMN_NAME_ROLE_NAME }) })
public class Role extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7832116917666289661L;
	public static final String TABLE_NAME = "ROLE";
 	public static final String COLUMN_NAME_ROLE_NAME = "ROLENAME";
	public static final String COLUMN_NAME_DEPARTMAN = "DEPARTMAN_ID";
	public static final String COLUMN_NAME_STATUS = "STATUS";
	public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";
	public static final String COLUMN_NAME_ADMIN_ROLE = "ADMIN_ROLE";

	public static final String TIPI_ADMIN = "admin";
	public static final String TIPI_SISTEM_YONETICI = "sistemYonetici";
	public static final String TIPI_GENEL_MUDUR = "genelMudur";
	public static final String TIPI_IK = "IK";
	public static final String TIPI_IK_Tesis = "IKTesis";
	public static final String TIPI_TESIS_YONETICI = "tesisYonetici";
	public static final String TIPI_IK_DIREKTOR = "IKDirektor";
	public static final String TIPI_PROJE_MUDURU = "projeMuduru";
	public static final String TIPI_SEKRETER = "sekreter";
	public static final String TIPI_SUPER_VISOR = "superVisor";
	public static final String TIPI_DIREKTOR_SUPER_VISOR = "superVisorDirektor";
	public static final String TIPI_YONETICI = "yonetici";
	public static final String TIPI_YONETICI_KONTRATLI = "yoneticiKontratli";
	public static final String TIPI_PERSONEL = "personel";
	public static final String TIPI_MUDUR = "mudur";
	public static final String TIPI_OPERATOR_SSK_IZIN = "operatorSSKIzin";
	public static final String TIPI_YEMEKHANE = "yemekHane";
	public static final String TIPI_TASERON_ADMIN = "kontratliAdmin";
	public static final String TIPI_ANAHTAR_KULLANICI = "keyUser";
	public static final String TIPI_IK_YETKILI_RAPOR_KULLANICI = "raporKullanici";

	private String rolename;
	private String aciklama;
	private Set<Role> groups;
	private Boolean status = Boolean.TRUE, adminRole = Boolean.FALSE;
	private Departman departman;

	@RoleName
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

	@RoleGroups
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
	@RoleConditional
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

	@Column(name = COLUMN_NAME_ADMIN_ROLE)
	public Boolean getAdminRole() {
		return adminRole;
	}

	public void setAdminRole(Boolean adminRole) {
		this.adminRole = adminRole;
	}

	@Transient
	public boolean isAciklamaVar() {
		return PdksUtil.hasStringValue(aciklama);
	}

	@Transient
	public boolean isAdminRoleMu() {
		return adminRole != null && adminRole.booleanValue();
	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}
}