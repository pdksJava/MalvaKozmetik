package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.pdks.entity.Lokasyon;
import org.pdks.entity.LokasyonView;
import org.pdks.security.entity.User;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;

@Name("lokasyonHome")
public class LokasyonHome extends EntityHome<LokasyonView> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5134819243603855681L;
	static Logger logger = Logger.getLogger(LokasyonHome.class);
	/**
	 * 
	 */
	@RequestParameter
	Long pdksLokasyonId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = false)
	FacesMessages facesMessages;
	private List<LokasyonView> lokasyonKGSList = new ArrayList<LokasyonView>();

	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	@Override
	public Object getId() {
		if (pdksLokasyonId == null) {
			return super.getId();
		} else {
			return pdksLokasyonId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public String lokasyonGuncelle(LokasyonView lokasyonView) {
		Lokasyon lokasyon = lokasyonView.getLokasyon();
		if (lokasyon == null) {
			lokasyon = new Lokasyon();
			lokasyon.setAciklama(lokasyonView.getLokasyonKGS().getAciklamaKGS());
			lokasyon.setLokasyonKGS(lokasyonView.getLokasyonKGS());
			lokasyonView.setLokasyon(lokasyon);
		}
		setInstance(lokasyonView);
		return "";
	}

	@Transactional
	public String save() {
		LokasyonView lokasyonView = getInstance();
		Lokasyon lokasyon = lokasyonView.getLokasyon();
		boolean yeni = lokasyon.getId() == null;
		try {
			if (yeni) {
				lokasyon.setOlusturanUser(authenticatedUser);
			} else {
				lokasyon.setGuncelleyenUser(authenticatedUser);
				lokasyon.setGuncellemeTarihi(new Date());
			}
			pdksEntityController.saveOrUpdate(session, entityManager, lokasyon);
			session.flush();

			fillKGSLokasyonList();

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		return "persisted";

	}

	public void fillKGSLokasyonList() {
		session.clear();
		List<LokasyonView> list = null;
		HashMap parametreMap = new HashMap();

		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			list = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, LokasyonView.class);
			if (!list.isEmpty()) {
				List<LokasyonView> list2 = new ArrayList<LokasyonView>();
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					LokasyonView lokasyonView = (LokasyonView) iterator.next();
					if (lokasyonView.getLokasyon() == null) {
						list2.add(lokasyonView);
						iterator.remove();
					}

				}
				if (!list2.isEmpty())
					list.addAll(list2);
				list2 = null;
			}
			parametreMap.clear();
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		setLokasyonKGSList(list);
	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);

		fillKGSLokasyonList();

	}

	public List<LokasyonView> getLokasyonKGSList() {
		return lokasyonKGSList;
	}

	public void setLokasyonKGSList(List<LokasyonView> lokasyonKGSList) {
		this.lokasyonKGSList = lokasyonKGSList;
	}
}
