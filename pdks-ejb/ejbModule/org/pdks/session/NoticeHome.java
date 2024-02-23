package org.pdks.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.NoteTipi;
import org.pdks.entity.Notice;
import org.pdks.entity.Tanim;
import org.pdks.security.action.StartupAction;
import org.pdks.security.entity.User;

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
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	private List<SelectItem> duyuruTipleri;
	private String duyuruTip;
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
			notice.setChangeUser(authenticatedUser);
			notice.setChangeDate(new Date());
			pdksEntityController.saveOrUpdate(session, entityManager, notice);
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
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.clear();
		List<Tanim> list = ortakIslemler.getTanimList(Tanim.TIPI_DUYURU, session);
		if (duyuruTipleri == null)
			duyuruTipleri = new ArrayList<SelectItem>();
		else
			duyuruTipleri.clear();
		duyuruTip = null;
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Tanim duyuru = (Tanim) iterator.next();
			NoteTipi noteTipi = null;
			try {
				noteTipi = NoteTipi.fromValue(duyuru.getKodu());
			} catch (Exception e) {
			}
			if (noteTipi == null)
				continue;
			if (duyuruTip == null && noteTipi.equals(NoteTipi.ANA_SAYFA))
				duyuruTip = noteTipi.value();
			duyuruTipleri.add(new SelectItem(duyuru.getKodu(), duyuru.getAciklama()));
		}
		if (duyuruTip != null)
			getNotice();
	}

	public void getNotice() {
		Notice notice = null;
		try {
			session.clear();
			notice = ortakIslemler.getNotice(duyuruTip, null, session);
			if (notice == null) {
				notice = new Notice();
				notice.setName(duyuruTip);
				notice.setVersion(0);
				notice.setDescription(PdksUtil.getSelectItemLabel(duyuruTip, duyuruTipleri));
				notice.setChangeUser(authenticatedUser);
				notice.setChangeDate(new Date());
				notice.setActive(Boolean.TRUE);
			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.error(e);
		}
		setHomeNotice(notice);
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public List<SelectItem> getDuyuruTipleri() {
		return duyuruTipleri;
	}

	public void setDuyuruTipleri(List<SelectItem> duyuruTipleri) {
		this.duyuruTipleri = duyuruTipleri;
	}

	public String getDuyuruTip() {
		return duyuruTip;
	}

	public void setDuyuruTip(String duyuruTip) {
		this.duyuruTip = duyuruTip;
	}

}
