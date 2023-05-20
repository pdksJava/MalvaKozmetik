package org.pdks.security.action;

import java.util.Arrays;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import org.pdks.security.entity.Role;

@Name("roleList")
public class RoleList extends EntityQuery<Role> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6501653692409523487L;

	private static final String EJBQL = "select role from Role role";

	private static final String[] RESTRICTIONS = { "lower(role.rolename) like concat(lower(#{roleList.role.rolename}),'%')", };

	private Role role = new Role();

	public RoleList() {
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public Role getRole() {
		return role;
	}
}
