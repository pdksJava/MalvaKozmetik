package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.pdks.entity.AccountPermission;
import org.pdks.entity.Parameter;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.persistence.PersistenceProvider;

@Name("accountPermissionHome")
public class AccountPermissionHome extends EntityHome<AccountPermission> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -600499670400399458L;
	@RequestParameter
	Long parameterId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false)
	User authenticatedUser;
	private Parameter currentParameter;

	private Session session;

	@Override
	public Object getId() {
		if (parameterId == null) {
			return super.getId();
		} else {
			return parameterId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	@Transactional
	public String save() {
		AccountPermission accountPermission = getInstance();
		int roleAdedi = accountPermission.getRoleList().size();
		if (roleAdedi > 0) {
			for (Role role : accountPermission.getRoleList()) {
				AccountPermission tmpPAccountPermission = new AccountPermission();
				tmpPAccountPermission.setAction(accountPermission.getAction());
				tmpPAccountPermission.setDiscriminator("Role");
				tmpPAccountPermission.setRecipient(role.getRolename());
				tmpPAccountPermission.setTarget(accountPermission.getTarget());
				entityManager.persist(tmpPAccountPermission);
			}

		} else {
			accountPermission.setDiscriminator("User");
			entityManager.persist(accountPermission);

		}

		entityManager.flush();
		assignId(PersistenceProvider.instance().getId(getInstance(), entityManager));
		// createdMessage();
		raiseAfterTransactionSuccessEvent();
		return "persisted";

	}

	public List<AccountPermission> getAccountPermissionList() {
		List<AccountPermission> parametreList = new ArrayList<AccountPermission>();
		HashMap parametreMap = new HashMap();
		parametreMap.put("status", Boolean.TRUE);
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);

		parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		parametreList = pdksEntityController.getObjectByInnerObjectList(parametreMap, AccountPermission.class);

		return parametreList;// this.entityManager.createNamedQuery("select Parameter from Parameter").getResultList();
	}

	public List<Role> getDistinctRoleList() {
		List<Role> parametreList = new ArrayList<Role>();
		HashMap parametreMap = new HashMap();
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);

		parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		parametreList = pdksEntityController.getObjectByInnerObjectList(parametreMap, Role.class);

		return parametreList;// this.entityManager.createNamedQuery("select Parameter from Parameter").getResultList();
	}

	public void setDistinctRoleList(List<Role> roleList) {
		// int i = 1;
	}

	public Parameter getCurrentParameter() {
		if (currentParameter == null)
			currentParameter = new Parameter();
		return currentParameter;
	}

	public void setCurrentParameter(Parameter currentParameter) {
		this.currentParameter = currentParameter;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

}
