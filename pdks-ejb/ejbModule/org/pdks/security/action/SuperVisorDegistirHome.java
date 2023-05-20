package org.pdks.security.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.pdks.security.entity.UserRoles;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;
import org.pdks.session.OrtakIslemler;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

@Name("superVisorDegistirHome")
public class SuperVisorDegistirHome extends EntityHome<User> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2220259723768190064L;
	static Logger logger = Logger.getLogger(SuperVisorDegistirHome.class);

	@RequestParameter
	Long userId;
	@In(required = false, create = true)
	EntityManager entityManager; // sql
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = true, create = true)
	OrtakIslemler ortakIslemler;
	private List<User> superVisorList = new ArrayList<User>();
	private Session session;

	@Override
	public Object getId() {

		if (userId == null) {
			return super.getId();
		} else {
			return userId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public String superVisorSec(Session session) {
		User user = getInstance();
		ortakIslemler.sistemeGirisIslemleri(user, Boolean.FALSE, null, null, session);
		authenticatedUser.setSeciliSuperVisor(user);
		return "/home.seam";
	}

	public String superVisorIptalSec() {
		authenticatedUser.setSeciliSuperVisor(null);
		return "/home.seam";
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		Date bugun = PdksUtil.getDate(Calendar.getInstance().getTime());
		List<User> list = null;
		HashMap parametreMap = new HashMap();
		parametreMap.put(PdksEntityController.MAP_KEY_MAP, "getId");
		parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "user");
		parametreMap.put("role.rolename=", Role.TIPI_SUPER_VISOR);
		parametreMap.put("user.durum=", Boolean.TRUE);
		parametreMap.put("user.pdksPersonel.durum=", Boolean.TRUE);
		parametreMap.put("user.pdksPersonel.sskCikisTarihi>=", bugun);
		parametreMap.put("user.pdksPersonel.iseBaslamaTarihi<=", bugun);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		if (authenticatedUser.getSeciliSuperVisor() != null)
			parametreMap.put("user<>", authenticatedUser.getSeciliSuperVisor());
		if (authenticatedUser.isSuperVisor()) {
			parametreMap.put("user.pdksPersonel.sirket=", authenticatedUser.getPdksPersonel().getSirket());
		} else if (authenticatedUser.isIK())
			parametreMap.put("user.pdksPersonel.sirket.departman=", authenticatedUser.getDepartman());
		try {
			TreeMap userMap = pdksEntityController.getObjectByInnerObjectMapInLogic(parametreMap, UserRoles.class, Boolean.FALSE);
			if (!userMap.isEmpty())
				list = PdksUtil.sortObjectStringAlanList(new ArrayList(userMap.values()), "getAdSoyad", null);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				User user = (User) iterator.next();
				if (user.getId().equals(authenticatedUser.getId()))
					iterator.remove();

			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			list = new ArrayList<User>();
		}
		setSuperVisorList(list);

	}

	public List<User> getSuperVisorList() {
		return superVisorList;
	}

	public void setSuperVisorList(List<User> superVisorList) {
		this.superVisorList = superVisorList;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
