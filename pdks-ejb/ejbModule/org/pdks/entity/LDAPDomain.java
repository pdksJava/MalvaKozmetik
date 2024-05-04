package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name = LDAPDomain.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { LDAPDomain.COLUMN_NAME_HOST }) })
public class LDAPDomain extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2883683260589305813L;

	/**
	 * 
	 */
	public static final String TABLE_NAME = "LDAP_DOMAIN";

 	public static final String COLUMN_NAME_HOST = "HOST";

	private int sira;

	private String ldapHost = "";

	private String ldapPort = "";

	private String ldapAdminUsername = "";

	private String ldapAdminPassword = "";

	private String ldapDC = "";

	private Boolean durum = new Boolean(true);

	public LDAPDomain() {
		super();

	}

	public LDAPDomain(String ldapHost, String ldapPort, String ldapAdminUsername, String ldapAdminPassword, String ldapDC) {
		super();
		this.ldapHost = ldapHost;
		veriAktar(ldapPort, ldapAdminUsername, ldapAdminPassword, ldapDC);

	}

	public void veriAktar(String ldapPort, String ldapAdminUsername, String ldapAdminPassword, String ldapDC) {
		this.ldapPort = ldapPort;
		this.ldapAdminUsername = ldapAdminUsername;
		this.ldapAdminPassword = ldapAdminPassword;
		this.ldapDC = ldapDC;
		this.sira = 1;
		this.durum = new Boolean(true);
	}

	@Column(name = COLUMN_NAME_HOST, length = 64)
	public String getLdapHost() {
		return ldapHost;
	}

	public void setLdapHost(String ldapHost) {
		this.ldapHost = ldapHost;
	}

	@Column(name = "PORT", length = 8)
	public String getLdapPort() {
		return ldapPort;
	}

	public void setLdapPort(String ldapPort) {
		this.ldapPort = ldapPort;
	}

	@Column(name = "ADMIN_USER_NAME", length = 128)
	public String getLdapAdminUsername() {
		return ldapAdminUsername;
	}

	public void setLdapAdminUsername(String ldapAdminUsername) {
		this.ldapAdminUsername = ldapAdminUsername;
	}

	@Column(name = "ADMIN_PASSWORD", length = 32)
	public String getLdapAdminPassword() {
		return ldapAdminPassword;
	}

	public void setLdapAdminPassword(String ldapAdminPassword) {
		this.ldapAdminPassword = ldapAdminPassword;
	}

	@Column(name = "DC", length = 128)
	public String getLdapDC() {
		return ldapDC;
	}

	public void setLdapDC(String ldapDC) {
		this.ldapDC = ldapDC;
	}

	@Column(name = "DURUM")
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Column(name = "SIRA")
	public int getSira() {
		return sira;
	}

	public void setSira(int sira) {
		this.sira = sira;
	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}

}
