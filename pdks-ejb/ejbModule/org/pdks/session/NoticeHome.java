package org.pdks.session;

import java.util.Date;
import java.util.HashMap;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.pdks.entity.Notice;
import org.pdks.security.action.StartupAction;
import org.pdks.security.entity.User;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.framework.EntityHome;

@Name("noticeHome")
public class NoticeHome extends EntityHome<Notice> {

	private static final long serialVersionUID = -8397934713490306777L;
	static Logger logger = Logger.getLogger(NoticeHome.class);

	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false)
	User authenticatedUser;
	@In(required = false)
	StartupAction startupAction;
	@In(required = false, create = true)
	EntityManager entityManager;
	private Session session;
	private Notice homeNotice = new Notice();

	public Notice getHomeNotice() {

		return homeNotice;
	}

	public void setHomeNotice(Notice homeNotice) {
		this.homeNotice = homeNotice;
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	@Transactional
	public String save() {
		try {
			Notice notice = getHomeNotice();
			notice.setChangeDate(new Date());
			notice.setChangeUser(authenticatedUser);
			session.saveOrUpdate(notice);
			session.flush();

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.debug("DUYURU KAYDETMEDE HATA: " + e.getMessage());
		}
		// fillNotice();
		startupAction.fillStartMethod(authenticatedUser, session);
		// session.refresh(notice);
		return "persisted";

	}

	public void fillNotice() {
		HashMap parametreMap = new HashMap();
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.clear();
		parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

		try {
			parametreMap.put("name", "anaSayfa");
			parametreMap.put("active", Boolean.TRUE);
			Notice notice = (Notice) pdksEntityController.getObjectByInnerObject(parametreMap, Notice.class);
			if (notice == null) {
				notice = new Notice();
				notice.setName("anaSayfa");
				notice.setDescription("Ana sayfa açıklama");
				notice.setActive(Boolean.TRUE);
			}
			setHomeNotice(notice);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.error(e);
		}

	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

}
