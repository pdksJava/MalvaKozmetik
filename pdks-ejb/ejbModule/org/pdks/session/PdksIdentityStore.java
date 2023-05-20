package org.pdks.session;

import static org.jboss.seam.ScopeType.APPLICATION;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;

import org.apache.log4j.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.management.IdentityStore.Feature;

@Name("pdksIdentityStore")
@Scope(APPLICATION)
public class PdksIdentityStore implements Serializable {
	/**
	 * 
	 */
	static Logger logger = Logger.getLogger(PdksIdentityStore.class);
	private static final long serialVersionUID = -2115037909132364966L;

	public boolean authenticate(String arg0, String arg1) {
		logger.debug("user name :" + arg0 + " passw:  " + arg1);
		return true;
	}

	public boolean addRoleToGroup(String arg0, String arg1) {
		return false;
	}

	public boolean changePassword(String arg0, String arg1) {
		return false;
	}

	public boolean createRole(String arg0) {
		return false;
	}

	public boolean createUser(String arg0, String arg1) {
		return false;
	}

	public boolean createUser(String arg0, String arg1, String arg2, String arg3) {
		return false;
	}

	public boolean deleteRole(String arg0) {
		return false;
	}

	public boolean deleteUser(String arg0) {
		return false;
	}

	public boolean disableUser(String arg0) {
		return false;
	}

	public boolean enableUser(String arg0) {
		return false;
	}

	public List<String> getGrantedRoles(String arg0) {
		return null;
	}

	public List<String> getImpliedRoles(String arg0) {
		return null;
	}

	public List<String> getRoleGroups(String arg0) {
		return null;
	}

	public boolean grantRole(String arg0, String arg1) {
		return false;
	}

	public boolean isUserEnabled(String arg0) {
		return false;
	}

	public List<String> listGrantableRoles() {
		return null;
	}

	public List<Principal> listMembers(String arg0) {
		return null;
	}

	public List<String> listRoles() {
		return null;
	}

	public List<String> listUsers() {
		return null;
	}

	public List<String> listUsers(String arg0) {
		return null;
	}

	public boolean removeRoleFromGroup(String arg0, String arg1) {
		return false;
	}

	public boolean revokeRole(String arg0, String arg1) {
		return false;
	}

	public boolean roleExists(String arg0) {
		return false;
	}

	public boolean supportsFeature(Feature arg0) {
		return false;
	}

	public boolean userExists(String arg0) {
		return false;
	}

}
