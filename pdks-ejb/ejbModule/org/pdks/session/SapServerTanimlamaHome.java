package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.faces.model.SelectItem;
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
import org.jboss.seam.framework.EntityHome;
import org.pdks.sap.entity.SAPSunucu;
import org.pdks.security.entity.User;

@Name("sapServerTanimlamaHome")
public class SapServerTanimlamaHome extends EntityHome<SAPSunucu> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1118644304223016754L;
	/**
	 * 
	 */

	static Logger logger = Logger.getLogger(SapServerTanimlamaHome.class);
	/**
	 * 
	 */
	@RequestParameter
	Long sunucuId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;

	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	private List<SAPSunucu> sunucuList = new ArrayList<SAPSunucu>();

	private List<SelectItem> sunucuTipleri;
	private SAPSunucu seciliSAPSunucu;
	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	@Override
	public Object getId() {
		if (sunucuId == null) {
			return super.getId();
		} else {
			return sunucuId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	@Transactional
	public String save() {

		try {
			pdksEntityController.saveOrUpdate(session, entityManager, seciliSAPSunucu);
			session.flush();
			fillSAPSunucuList();

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		return "persisted";

	}

	public String guncelle(SAPSunucu sapSunucu) {
		if (sapSunucu == null)
			sapSunucu = new SAPSunucu();
		setSeciliSAPSunucu(sapSunucu);
		return "";
	}

	private void fillSAPSunucuList() {
		List<SAPSunucu> list = null;
		HashMap parametreMap = new HashMap();
		try {
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			list = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, SAPSunucu.class);
			if (list.size() > 1) {
				list = PdksUtil.sortListByAlanAdi(list, "sunucuTipi", false);
				List<SAPSunucu> list1 = new ArrayList<SAPSunucu>();
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					SAPSunucu sapSunucu = (SAPSunucu) iterator.next();
					if (sapSunucu.getAktif())
						continue;
					list1.add(sapSunucu);
					iterator.remove();

				}
				if (!list1.isEmpty())
					list.addAll(list1);
				list1 = null;
			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			list = new ArrayList<SAPSunucu>();
		}
		setSunucuList(list);
	}

	private void fillSAPSunucuTipleri() {
		if (sunucuTipleri == null)
			sunucuTipleri = new ArrayList<SelectItem>();
		else
			sunucuTipleri.clear();
		for (int i = 0; i <= SAPSunucu.SUNUCU_TIPI_BW_TEST; i++) {
			String aciklama = SAPSunucu.getSunucuTipiAciklama(i);
			if (PdksUtil.hasStringValue(aciklama))
				sunucuTipleri.add(new SelectItem(i, aciklama));
		}

	}

	public void instanceRefresh() {
		if (seciliSAPSunucu.getId() != null)
			session.refresh(seciliSAPSunucu);
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		seciliSAPSunucu = new SAPSunucu();
		fillSAPSunucuList();
		fillSAPSunucuTipleri();
	}

	public List<SAPSunucu> getSunucuList() {
		return sunucuList;
	}

	public void setSunucuList(List<SAPSunucu> sunucuList) {
		this.sunucuList = sunucuList;
	}

	public List<SelectItem> getSunucuTipleri() {
		return sunucuTipleri;
	}

	public void setSunucuTipleri(List<SelectItem> sunucuTipleri) {
		this.sunucuTipleri = sunucuTipleri;
	}

	public SAPSunucu getSeciliSAPSunucu() {
		return seciliSAPSunucu;
	}

	public void setSeciliSAPSunucu(SAPSunucu seciliSAPSunucu) {
		this.seciliSAPSunucu = seciliSAPSunucu;
	}

}
