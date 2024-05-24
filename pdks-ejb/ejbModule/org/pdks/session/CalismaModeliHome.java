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
import org.pdks.entity.CalismaModeli;
import org.pdks.entity.CalismaModeliVardiya;
import org.pdks.entity.Departman;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaSablonu;
import org.pdks.security.entity.User;

@Name("calismaModeliHome")
public class CalismaModeliHome extends EntityHome<CalismaModeli> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1146930923797257560L;

	static Logger logger = Logger.getLogger(CalismaModeliHome.class);
	@RequestParameter
	Long calismaModeliId;

	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = false)
	FacesMessages facesMessages;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	private CalismaModeli calismaModeli;

	private List<CalismaModeli> calismaModeliList;
	private List<VardiyaSablonu> sablonList;
	private List<Vardiya> vardiyaList = new ArrayList<Vardiya>(), kayitliVardiyaList = new ArrayList<Vardiya>();
	private List<Departman> departmanList;

	private Boolean hareketKaydiVardiyaBul = Boolean.FALSE, saatlikCalismaVar = false, otomatikFazlaCalismaOnaylansinVar = false;

	private Session session;

	@Override
	public Object getId() {
		if (calismaModeliId == null) {
			return super.getId();
		} else {
			return calismaModeliId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public String calismaModeliEkle(CalismaModeli xCalismaModeli) {

		if (xCalismaModeli == null) {

			xCalismaModeli = new CalismaModeli();
			if (!saatlikCalismaVar)
				xCalismaModeli.setAylikMaas(Boolean.TRUE);
		}

		setCalismaModeli(xCalismaModeli);

		if (authenticatedUser.isAdmin())
			fillBagliOlduguDepartmanTanimList();
		if (xCalismaModeli.getId() == null && departmanList.size() > 0)
			xCalismaModeli.setDepartman(departmanList.get(0));

		fillVardiyalar();
		return "";
	}

	public String calismaModeliKopyala(CalismaModeli xCalismaModeli) {
		CalismaModeli calismaModeliYeni = (CalismaModeli) xCalismaModeli.cloneEmpty();

		calismaModeliYeni.setId(null);
		if (calismaModeliYeni.getAciklama() != null)
			calismaModeliYeni.setAciklama(xCalismaModeli.getAciklama() + " kopya");
		if (authenticatedUser.isAdmin())
			fillBagliOlduguDepartmanTanimList();
		setCalismaModeli(calismaModeliYeni);
		fillVardiyalar();
		return "";

	}

	public void fillBagliOlduguDepartmanTanimList() {

		List tanimList = null;

		try {
			tanimList = ortakIslemler.fillDepartmanTanimList(session);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		} finally {

		}

		setDepartmanList(tanimList);
	}

	public String fillVardiyalar() {
		HashMap parametreMap = new HashMap();
		parametreMap.put("durum", Boolean.TRUE);
		if (calismaModeli.getDepartman() != null)
			parametreMap.put("departman.id", calismaModeli.getDepartman().getId());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		sablonList = pdksEntityController.getObjectByInnerObjectList(parametreMap, VardiyaSablonu.class);
		for (Iterator iterator = sablonList.iterator(); iterator.hasNext();) {
			VardiyaSablonu sablonu = (VardiyaSablonu) iterator.next();
			if (sablonu.getCalismaModeli() != null)
				iterator.remove();

		}
		if (calismaModeli.getBagliVardiyaSablonu() != null) {
			boolean ekle = true;
			Long id = calismaModeli.getBagliVardiyaSablonu().getId();
			for (VardiyaSablonu sablonu : sablonList) {
				if (sablonu.getId().equals(id)) {
					ekle = false;
					break;
				}
			}
			if (ekle)
				sablonList.add(calismaModeli.getBagliVardiyaSablonu());
		}
		parametreMap.clear();

		parametreMap.put("durum", Boolean.TRUE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		vardiyaList = pdksEntityController.getObjectByInnerObjectList(parametreMap, Vardiya.class);
		Long cmaDepartmanId = calismaModeli.getDepartman() != null ? calismaModeli.getDepartman().getId() : null;
		for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
			Vardiya vardiya = (Vardiya) iterator.next();
			if (vardiya.getKisaAdi().equals("TA") || vardiya.getKisaAdi().equals("TG"))
				logger.info(vardiya.getId() + " " + vardiya.getKisaAdi());
			if (cmaDepartmanId != null && vardiya.getDepartman() != null && !vardiya.getDepartman().getId().equals(cmaDepartmanId))
				iterator.remove();
			else if (!vardiya.isCalisma() || vardiya.getGenel().equals(Boolean.FALSE)) {
				iterator.remove();
			}

		}
		if (calismaModeli.getId() != null) {
			parametreMap.clear();
			parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "vardiya");
			parametreMap.put("calismaModeli.id", calismaModeli.getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			kayitliVardiyaList = pdksEntityController.getObjectByInnerObjectList(parametreMap, CalismaModeliVardiya.class);
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
			vardiyaList = PdksUtil.sortObjectStringAlanList(vardiyaList, "getKisaAdi", null);
		if (kayitliVardiyaList.size() > 1)
			kayitliVardiyaList = PdksUtil.sortObjectStringAlanList(kayitliVardiyaList, "getKisaAdi", null);

		return "";
	}

	public void instanceRefresh() {
		if (calismaModeli.getId() != null)
			session.refresh(calismaModeli);
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();

		fillCalismaModeliList();
	}

	@Transactional
	public String kaydet() {

		try {
			if (calismaModeli.getId() != null) {
				calismaModeli.setGuncellemeTarihi(new Date());
				calismaModeli.setGuncelleyenUser(authenticatedUser);
			} else {
				calismaModeli.setOlusturmaTarihi(new Date());
				calismaModeli.setOlusturanUser(authenticatedUser);
			}
			List<CalismaModeliVardiya> kayitliCalismaModeliVardiyaList = null;
			if (calismaModeli.getId() != null && calismaModeli.getGenelVardiya().equals(Boolean.FALSE)) {
				HashMap parametreMap = new HashMap();
				parametreMap.put("calismaModeli.id", calismaModeli.getId());
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				kayitliCalismaModeliVardiyaList = pdksEntityController.getObjectByInnerObjectList(parametreMap, CalismaModeliVardiya.class);
			} else
				kayitliCalismaModeliVardiyaList = new ArrayList<CalismaModeliVardiya>();
			String haftaTatilDurum = ortakIslemler.getParameterKey("haftaTatilDurum");
			if (!haftaTatilDurum.equals("1"))
				calismaModeli.setHaftaTatilMesaiOde(Boolean.FALSE);
			if (calismaModeli.getHaftaTatilMesaiOde().equals(Boolean.FALSE))
				calismaModeli.setGeceHaftaTatilMesaiParcala(Boolean.FALSE);
			pdksEntityController.saveOrUpdate(session, entityManager, calismaModeli);
			if (calismaModeli.getGenelVardiya() || calismaModeli.isOrtakVardiyadir())
				kayitliVardiyaList.clear();
			for (Iterator iterator = kayitliVardiyaList.iterator(); iterator.hasNext();) {
				Vardiya kayitliVardiya = (Vardiya) iterator.next();
				boolean ekle = true;
				for (Iterator iterator2 = kayitliCalismaModeliVardiyaList.iterator(); iterator2.hasNext();) {
					CalismaModeliVardiya cmv = (CalismaModeliVardiya) iterator2.next();
					if (cmv.getVardiya().getId().equals(kayitliVardiya.getId())) {
						ekle = false;
						iterator2.remove();
						break;
					}

				}
				if (ekle) {
					CalismaModeliVardiya cmv = new CalismaModeliVardiya(kayitliVardiya, calismaModeli);
					pdksEntityController.saveOrUpdate(session, entityManager, cmv);
				}
			}
			for (Iterator iterator2 = kayitliCalismaModeliVardiyaList.iterator(); iterator2.hasNext();) {
				CalismaModeliVardiya cmv = (CalismaModeliVardiya) iterator2.next();
				pdksEntityController.deleteObject(session, entityManager, cmv);
			}
			session.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

		fillCalismaModeliList();
		return "";
	}

	public void fillCalismaModeliList() {
		hareketKaydiVardiyaBul = ortakIslemler.getParameterKey("hareketKaydiVardiyaBul").equals("1");
		saatlikCalismaVar = ortakIslemler.getParameterKey("saatlikCalismaVar").equals("1");
		otomatikFazlaCalismaOnaylansinVar = ortakIslemler.getParameterKey("otomatikFazlaCalismaOnaylansin").equals("1");
		calismaModeli = new CalismaModeli();
		HashMap parametreMap = new HashMap();
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		calismaModeliList = pdksEntityController.getObjectByInnerObjectList(parametreMap, CalismaModeli.class);
		if (!hareketKaydiVardiyaBul || !otomatikFazlaCalismaOnaylansinVar) {
			List<CalismaModeli> pasifList = new ArrayList<CalismaModeli>();
			for (Iterator iterator = calismaModeliList.iterator(); iterator.hasNext();) {
				CalismaModeli cm = (CalismaModeli) iterator.next();
				if (cm.getDurum().booleanValue() == false) {
					pasifList.add(cm);
					iterator.remove();
				}

			}
			if (authenticatedUser.isAdmin() && !pasifList.isEmpty())
				calismaModeliList.addAll(pasifList);
			pasifList = null;
			for (CalismaModeli cm : calismaModeliList) {
				if (cm.getDurum()) {
					if (!otomatikFazlaCalismaOnaylansinVar)
						otomatikFazlaCalismaOnaylansinVar = cm.isOtomatikFazlaCalismaOnaylansinmi();
					if (!hareketKaydiVardiyaBul)
						hareketKaydiVardiyaBul = cm.isHareketKaydiVardiyaBulsunmu();
				}

			}
		}
	}

	public List<Vardiya> getVardiyaList() {
		return vardiyaList;
	}

	public void setVardiyaList(List<Vardiya> vardiyaList) {
		this.vardiyaList = vardiyaList;
	}

	public List<CalismaModeli> getCalismaModeliList() {
		return calismaModeliList;
	}

	public void setCalismaModeliList(List<CalismaModeli> calismaModeliList) {
		this.calismaModeliList = calismaModeliList;
	}

	public List<Vardiya> getKayitliVardiyaList() {
		return kayitliVardiyaList;
	}

	public void setKayitliVardiyaList(List<Vardiya> kayitliVardiyaList) {
		this.kayitliVardiyaList = kayitliVardiyaList;
	}

	public CalismaModeli getCalismaModeli() {
		return calismaModeli;
	}

	public void setCalismaModeli(CalismaModeli calismaModeli) {
		this.calismaModeli = calismaModeli;
	}

	public List<VardiyaSablonu> getSablonList() {
		return sablonList;
	}

	public void setSablonList(List<VardiyaSablonu> sablonList) {
		this.sablonList = sablonList;
	}

	public List<Departman> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<Departman> departmanList) {
		this.departmanList = departmanList;
	}

	public Boolean getHareketKaydiVardiyaBul() {
		return hareketKaydiVardiyaBul;
	}

	public void setHareketKaydiVardiyaBul(Boolean hareketKaydiVardiyaBul) {
		this.hareketKaydiVardiyaBul = hareketKaydiVardiyaBul;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	/**
	 * @return the saatlikCalismaVar
	 */
	public Boolean getSaatlikCalismaVar() {
		return saatlikCalismaVar;
	}

	/**
	 * @param saatlikCalismaVar
	 *            the saatlikCalismaVar to set
	 */
	public void setSaatlikCalismaVar(Boolean saatlikCalismaVar) {
		this.saatlikCalismaVar = saatlikCalismaVar;
	}

	/**
	 * @return the otomatikFazlaCalismaOnaylansinVar
	 */
	public Boolean getOtomatikFazlaCalismaOnaylansinVar() {
		return otomatikFazlaCalismaOnaylansinVar;
	}

	/**
	 * @param otomatikFazlaCalismaOnaylansinVar
	 *            the otomatikFazlaCalismaOnaylansinVar to set
	 */
	public void setOtomatikFazlaCalismaOnaylansinVar(Boolean otomatikFazlaCalismaOnaylansinVar) {
		this.otomatikFazlaCalismaOnaylansinVar = otomatikFazlaCalismaOnaylansinVar;
	}

}
