package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
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
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaYemekIzin;
import org.pdks.entity.YemekIzin;
import org.pdks.security.entity.User;

@Name("yemekIzinHome")
public class YemekIzinHome extends EntityHome<YemekIzin> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4110678952372352196L;
	static Logger logger = Logger.getLogger(YemekIzinHome.class);

	@RequestParameter
	Long pdksYemekId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = false)
	FacesMessages facesMessages;
	@In(required = true, create = true)
	OrtakIslemler ortakIslemler;

	private List<YemekIzin> yemekList = new ArrayList<YemekIzin>();
	private List<Vardiya> vardiyaList = new ArrayList<Vardiya>(), kayitliVardiyaList = new ArrayList<Vardiya>();
	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	@Override
	public Object getId() {
		if (pdksYemekId == null) {
			return super.getId();
		} else {
			return pdksYemekId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	@Transactional
	public String save() {

		YemekIzin pdksYemek = getInstance();
		boolean yeni = pdksYemek.getId() == null;
		try {
			if (yeni)
				pdksYemek.setOlusturanUser(authenticatedUser);
			else {
				pdksYemek.setGuncelleyenUser(authenticatedUser);
				pdksYemek.setGuncellemeTarihi(new Date());
			}

			List<VardiyaYemekIzin> kayitliVardiyaYemekIzinList = null;
			if (pdksYemek.getId() != null) {
				HashMap parametreMap = new HashMap();
				parametreMap.put("yemekIzin.id", pdksYemek.getId());
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				kayitliVardiyaYemekIzinList = pdksEntityController.getObjectByInnerObjectList(parametreMap, VardiyaYemekIzin.class);
			} else
				kayitliVardiyaYemekIzinList = new ArrayList<VardiyaYemekIzin>();

			pdksEntityController.saveOrUpdate(session, entityManager, pdksYemek);
			for (Iterator iterator = kayitliVardiyaList.iterator(); iterator.hasNext();) {
				Vardiya kayitliVardiya = (Vardiya) iterator.next();
				boolean ekle = true;
				for (Iterator iterator2 = kayitliVardiyaYemekIzinList.iterator(); iterator2.hasNext();) {
					VardiyaYemekIzin vyi = (VardiyaYemekIzin) iterator2.next();
					if (vyi.getVardiya().getId().equals(kayitliVardiya.getId())) {
						ekle = false;
						iterator2.remove();
						break;
					}

				}
				if (ekle) {
					VardiyaYemekIzin vyi = new VardiyaYemekIzin(kayitliVardiya, pdksYemek);
					pdksEntityController.saveOrUpdate(session, entityManager, vyi);
				}
			}
			for (Iterator iterator2 = kayitliVardiyaYemekIzinList.iterator(); iterator2.hasNext();) {
				VardiyaYemekIzin vyi = (VardiyaYemekIzin) iterator2.next();
				pdksEntityController.deleteObject(session, entityManager,vyi );
			}

			session.flush();
			fillPdksYemekList();

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		return "persisted";

	}

	public void fillPdksYemekList() {
		List<YemekIzin> list = new ArrayList<YemekIzin>();
		try {
			HashMap parametreMap = new HashMap();
			if (session != null) {
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				session.clear();
			}
 			list = pdksEntityController.getObjectByInnerObjectList(parametreMap, YemekIzin.class);
			if (list.size() > 1)
				list = PdksUtil.sortListByAlanAdi(list, "yemekNumeric", false);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		setYemekList(list);
	}

	/**
	 * @param yemekIzin
	 * @return
	 */
	public String yemekEkle(YemekIzin yemekIzin) {
		if (yemekIzin == null)
			yemekIzin = new YemekIzin();
		setInstance(yemekIzin);
		HashMap parametreMap = new HashMap();
		parametreMap.put("durum", Boolean.TRUE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		vardiyaList = pdksEntityController.getObjectByInnerObjectList(parametreMap, Vardiya.class);
		for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
			Vardiya vardiya = (Vardiya) iterator.next();
			if (!vardiya.isCalisma())
				iterator.remove();
		}
		if (yemekIzin.getId() != null) {
			parametreMap.clear();
			parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "vardiya");
			parametreMap.put("yemekIzin.id", yemekIzin.getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			kayitliVardiyaList = pdksEntityController.getObjectByInnerObjectList(parametreMap, VardiyaYemekIzin.class);
			for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
				Vardiya vardiya = (Vardiya) iterator.next();
				for (Vardiya vardiyaCalisma : kayitliVardiyaList) {
					if (vardiyaCalisma.getId().equals(vardiya.getId())) {
						iterator.remove();
						break;
					}

				}
			}

		} else
			kayitliVardiyaList = new ArrayList<Vardiya>();
		if (vardiyaList.size() > 1)
			vardiyaList = PdksUtil.sortListByAlanAdi(vardiyaList, "vardiyaNumeric", false);
		if (kayitliVardiyaList.size() > 1)
			kayitliVardiyaList = PdksUtil.sortListByAlanAdi(kayitliVardiyaList, "vardiyaNumeric", false);

		return "";
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
		session.clear();
		fillPdksYemekList();

	}

	public List<YemekIzin> getYemekList() {
		return yemekList;
	}

	public void setYemekList(List<YemekIzin> value) {
		this.yemekList = value;
	}

	public List<Vardiya> getVardiyaList() {
		return vardiyaList;
	}

	public void setVardiyaList(List<Vardiya> vardiyaList) {
		this.vardiyaList = vardiyaList;
	}

	public List<Vardiya> getKayitliVardiyaList() {
		return kayitliVardiyaList;
	}

	public void setKayitliVardiyaList(List<Vardiya> kayitliVardiyaList) {
		this.kayitliVardiyaList = kayitliVardiyaList;
	}

}
