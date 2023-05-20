package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;

@Name("pdksPersonelRoleHome")
public class PdksPersonelRoleHome extends EntityHome<Role> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3668437032153975254L;
	static Logger logger = Logger.getLogger(PdksPersonelRoleHome.class);

	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false)
	User authenticatedUser;
	@In(required = false)
	FacesMessages facesMessages;
	private Role roleView;
	private Boolean rolAktif;
	private List<Role> rolList = new ArrayList<Role>();
	private String rolAdi, rolAciklama;
	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String rolGetir() {
		HashMap parametreMap = new HashMap();
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		ArrayList<Role> tempPermissionList = (ArrayList<Role>) pdksEntityController.getObjectByInnerObjectList(parametreMap, Role.class);
		setRolList(tempPermissionList);
		return "";
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		rolAdi = "";
		rolAciklama = "";
		rolAktif = Boolean.TRUE;
		rolGetir();

	}

	public String rolKayitGiris() {
		return "";
	}

	@Transactional
	public String rolSil(Role roleView) {
		roleView.setStatus(Boolean.FALSE);
		try {
			pdksEntityController.saveOrUpdate(session, entityManager, roleView);

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.info(e.toString());
		}
		session.flush();
		rolGetir();
		return "";

	}

	@Transactional
	public String rolKaydet() {
		try {
			pdksEntityController.saveOrUpdate(session, entityManager, roleView);

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.info(e.toString());
		}
		session.flush();
		rolGetir();
		return "";
	}

	@Transactional
	public String rolNewKaydet() {
		Role rolKaydet = new Role();
		rolKaydet.setAciklama(rolAciklama);
		rolKaydet.setRolename(rolAdi);
		rolKaydet.setStatus(rolAktif);
		try {
			session.save(rolKaydet);

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.info(e.toString());
		}
		session.flush();
		rolGetir();

		return "";
	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	public String rolGuncelleGiris(Role rol) {
		roleView = rol;
		return "";
	}

	public List<Role> getRolList() {
		return rolList;
	}

	public void setRolList(List<Role> rolList) {
		this.rolList = rolList;
	}

	public String getRolAdi() {
		return rolAdi;
	}

	public void setRolAdi(String rolAdi) {
		this.rolAdi = rolAdi;
	}

	public String getRolAciklama() {
		return rolAciklama;
	}

	public void setRolAciklama(String rolAciklama) {
		this.rolAciklama = rolAciklama;
	}

	public Boolean getRolAktif() {
		return rolAktif;
	}

	public void setRolAktif(Boolean rolAktif) {
		this.rolAktif = rolAktif;
	}

	public Role getRoleView() {
		return roleView;
	}

	public void setRoleView(Role roleView) {
		this.roleView = roleView;
	}

}
