package org.pdks.security.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.pdks.security.entity.User;
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

@Name("yoneticiDegistirHome")
public class YoneticiDegistirHome extends EntityHome<User> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1685568660947933006L;
	static Logger logger = Logger.getLogger(YoneticiDegistirHome.class);

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
	private List<User> yoneticiList = new ArrayList<User>();
	private User yonetici;
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

	public String yoneticiSec() {
		authenticatedUser.setPersonelGeciciNoList(null);
		yonetici.setYetkiliPersonelNoList(null);
		Date tarih = Calendar.getInstance().getTime();
		ortakIslemler.yoneticiIslemleri(yonetici, 2, tarih, tarih, session);
		authenticatedUser.setSeciliSuperVisor(yonetici);
		authenticatedUser.setYetkiTumPersonelNoList(null);
		authenticatedUser.setPersonelGeciciNoList(yonetici.getYetkiTumPersonelNoList());
		return "/home.xhtml";
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		if (authenticatedUser.getSeciliSuperVisor() != null)
			setYonetici(authenticatedUser.getSeciliSuperVisor());
		else
			setYonetici(new User());
		if (authenticatedUser.getUserVekaletList() != null) {
			List list = new ArrayList();
			try {
				for (Iterator<User> iterator = authenticatedUser.getUserVekaletList().iterator(); iterator.hasNext();) {
					User user = iterator.next();
					if (!user.getId().equals(yonetici.getId())) {
						yonetici.setYetkiTumPersonelNoList(new ArrayList());
						list.add(user.getId());
					}
				}

			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				list = new ArrayList();
			}
			List<User> userList = new ArrayList<User>();
			if (!list.isEmpty())
				try {
					HashMap parametreMap = new HashMap();
					parametreMap.put("id", list);
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					userList = pdksEntityController.getObjectByInnerObjectList(parametreMap, User.class);

				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());

				}
			setYoneticiList(userList);

		}

	}

	public List<User> getYoneticiList() {
		return yoneticiList;
	}

	public void setYoneticiList(List<User> yoneticiList) {
		this.yoneticiList = yoneticiList;
	}

	public User getYonetici() {
		return yonetici;
	}

	public void setYonetici(User yonetici) {
		this.yonetici = yonetici;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

}
