package org.pdks.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.jboss.seam.annotations.security.permission.PermissionAction;
import org.jboss.seam.annotations.security.permission.PermissionDiscriminator;
import org.jboss.seam.annotations.security.permission.PermissionRole;
import org.jboss.seam.annotations.security.permission.PermissionTarget;
import org.jboss.seam.annotations.security.permission.PermissionUser;
import org.pdks.security.entity.Role;

@Entity(name = "ACCOUNTPERMISSION")
public class AccountPermission extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1736440142880303400L;
	public static final String ACTION_VIEW = "view";
	public static final String ACTION_READ = "read";
	public static final String ACTION_WRITE = "write";
	public static final String ACTION_DELETE = "delete";

	public static final String DISCRIMINATOR_USER = "user";
	public static final String DISCRIMINATOR_ROLE = "role";

	public static final String ADMIN_ROLE = "admin";
	public static final String IK_ROLE = "IK";
	public static final String IK_Tesis_ROLE = "IKTesis";

	private String recipient;
	private String target;
	private String action;
	private String discriminator;
	private Boolean status = Boolean.FALSE;
	private Boolean check = Boolean.FALSE;

	private String recipientDescription;
	private String targetDescription;
	private List<Role> roleList = new ArrayList<Role>();

	@PermissionUser
	@PermissionRole
	@Column(name = "RECIPIENT")
	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	@PermissionTarget
	@Column(name = "TARGET")
	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	@PermissionAction
	@Column(name = "ACTION")
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@PermissionDiscriminator
	@Column(name = "DISCRIMINATOR")
	public String getDiscriminator() {
		return discriminator;
	}

	public void setDiscriminator(String discriminator) {
		this.discriminator = discriminator;
	}

	@Generated(value = GenerationTime.ALWAYS)
	@Column(name = "RECIPIENTDESCRIPTION")
	public String getRecipientDescription() {
		return recipientDescription;
	}

	public void setRecipientDescription(String recipientDescription) {
		this.recipientDescription = recipientDescription;
	}

	@Generated(value = GenerationTime.ALWAYS)
	@Column(name = "TARGETDESCRIPTION")
	public String getTargetDescription() {
		return targetDescription;
	}

	public void setTargetDescription(String targetDescription) {
		this.targetDescription = targetDescription;
	}

	@Column(name = "STATUS")
	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	@Transient
	public List<Role> getRoleList() {
		return roleList;
	}

	public void setRoleList(List<Role> roleList) {
		this.roleList = roleList;
	}

	@Transient
	public Boolean getCheck() {
		return check;
	}

	public void setCheck(Boolean check) {
		this.check = check;
	}

}
