package org.pdks.session;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.pdks.security.entity.Role;
import org.hibernate.Session;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("roleHome")
public class RoleHome extends EntityHome<Role> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6500632692114083648L;
	static Logger logger = Logger.getLogger(RoleHome.class);
	
	@In(create = true)
	PdksEntityController pdksEntityController;
	private Session session;
	
	public void setRoleId(Integer id) {
		setId(id);
	}

	public Integer getRoleId() {
		return (Integer) getId();
	}

	@Override
	protected Role createInstance() {
		Role role = new Role();
		return role;
	}

	public void wire() {
		getInstance();
	}

	public boolean isWired() {
		return true;
	}

	public Role getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public List<Role> getAktifRolList() {
		HashMap parametreMap = new HashMap();
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		return pdksEntityController.getObjectByInnerObjectList(parametreMap, Role.class);

	}

	public void setAktifRolList(List<Role> f) {
		logger.debug("trgdf");
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
