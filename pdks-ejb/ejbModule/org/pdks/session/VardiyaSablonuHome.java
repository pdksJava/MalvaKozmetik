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
import org.pdks.entity.Departman;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaSablonu;
import org.pdks.security.entity.User;

@Name("vardiyaSablonuHome")
public class VardiyaSablonuHome extends EntityHome<VardiyaSablonu> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6175559101056709773L;
	static Logger logger = Logger.getLogger(VardiyaSablonuHome.class);
	@RequestParameter
	Long pdksVardiyaSablonId;

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

	private List<VardiyaSablonu> vardiyaSablonList = new ArrayList<VardiyaSablonu>();
	private List<Vardiya> vardiyaList = new ArrayList<Vardiya>(), vardiyaCalisanList = new ArrayList<Vardiya>(), isKurVardiyaList = new ArrayList<Vardiya>();
	private boolean vardiyaVar = Boolean.FALSE, isKur = Boolean.FALSE, isKurGoster = Boolean.FALSE;
	private Vardiya lastVardiya;
	private List<Departman> departmanList = new ArrayList<Departman>();
	private List<CalismaModeli> modelList;
	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	@Override
	public Object getId() {
		if (pdksVardiyaSablonId == null) {
			return super.getId();
		} else {
			return pdksVardiyaSablonId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public void vardiyaDegistir(Vardiya pdksVardiya) {
		setLastVardiya(pdksVardiya);
	}

	public void kayitGuncelle() {
		fillPdksVardiyaList();
		setVardiyaVar(Boolean.TRUE);

	}

	public void fillBagliOlduguDepartmanTanimList() {

		List tanimList = null;

		try {
			tanimList = ortakIslemler.fillDepartmanTanimList(session);
			;
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		} finally {

		}

		setDepartmanList(tanimList);
	}

	public void vardiyaSablonuEkle() {
		this.clearInstance();
		fillPdksVardiyaList();
		setVardiyaList(new ArrayList<Vardiya>());
		setVardiyaVar(Boolean.FALSE);
		VardiyaSablonu sablonu = getInstance();
		if (departmanList.size() == 1)
			sablonu.setDepartman(departmanList.get(0));
	}

	@Override
	@Transactional
	public String update() {
		String cikis = "";
		VardiyaSablonu pdksVardiyaSablon = getInstance();

		boolean hata = Boolean.FALSE;
		try {
			if (pdksVardiyaSablon.getDurum()) {
				pdksVardiyaSablon.vardiyaBul();
				int vardiyaGunAdet = 0, haftaTatilGunAdet = 0, offGunAdet = 0;
				for (Iterator<Vardiya> iterator = pdksVardiyaSablon.getVardiyaList().iterator(); iterator.hasNext();) {
					Vardiya pdksVardiya = iterator.next();
					if (pdksVardiya.isCalisma() || pdksVardiya.isIzin())
						++vardiyaGunAdet;
					else if (pdksVardiya.isHaftaTatil())
						++haftaTatilGunAdet;
					else if (PdksUtil.getPlanOffAdet() > 0 && pdksVardiya.isOffGun())
						++offGunAdet;
				}
				if (offGunAdet > 0 && offGunAdet > PdksUtil.getPlanOffAdet()) {
					PdksUtil.addMessageWarn("Off gün sayısı en fazla " + PdksUtil.getPlanOffAdet() + " adet seçiniz");
					hata = Boolean.TRUE;
				}
				if (vardiyaGunAdet != pdksVardiyaSablon.getCalismaGunSayisi()) {
					PdksUtil.addMessageWarn("Çalışma gün sayısı " + pdksVardiyaSablon.getCalismaGunSayisi() + " adet seçiniz");
					hata = Boolean.TRUE;
				}
				if (haftaTatilGunAdet != 1) {
					PdksUtil.addMessageWarn("Hafta tatili sayısı toplam 1 gün seçiniz");
					hata = Boolean.TRUE;
				}
			}
			if (!hata) {
				cikis = "persisted";
				if (pdksVardiyaSablon.getId() == null)
					pdksVardiyaSablon.setOlusturanUser(authenticatedUser);
				else {
					pdksVardiyaSablon.setGuncelleyenUser(authenticatedUser);
					pdksVardiyaSablon.setGuncellemeTarihi(new Date());
				}
				if (pdksVardiyaSablon.getId() == null && !authenticatedUser.isAdmin())
					pdksVardiyaSablon.setDepartman(authenticatedUser.getDepartman());
				pdksEntityController.saveOrUpdate(session, entityManager, pdksVardiyaSablon);
				session.flush();
				fillPdksVardiyaSablonList();
			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		return cikis;

	}

	public void fillPdksVardiyaSablonList() {
		List<VardiyaSablonu> sablonList = new ArrayList<VardiyaSablonu>();
		try {
			HashMap parametreMap = new HashMap();
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			sablonList = pdksEntityController.getObjectByInnerObjectList(parametreMap, VardiyaSablonu.class);
			if (!authenticatedUser.isAdmin()) {
				for (Iterator iterator = sablonList.iterator(); iterator.hasNext();) {
					VardiyaSablonu pdksVardiyaSablonu = (VardiyaSablonu) iterator.next();
					if (pdksVardiyaSablonu.getDepartman() != null && !authenticatedUser.getDepartman().getId().equals(pdksVardiyaSablonu.getDepartman().getId()))
						iterator.remove();
				}
			}
			vardiyaSablonList.clear();
			isKurGoster = Boolean.FALSE;
			if (!sablonList.isEmpty()) {
				sablonList = PdksUtil.sortListByAlanAdi(sablonList, "sonIslemTarihi", Boolean.TRUE);
				for (Iterator iterator = sablonList.iterator(); iterator.hasNext();) {
					VardiyaSablonu pdksVardiyaSablonu = (VardiyaSablonu) iterator.next();
					if (pdksVardiyaSablonu.getDurum()) {
						if (!isKurGoster)
							isKurGoster = pdksVardiyaSablonu.isIsKurMu();
						vardiyaSablonList.add(pdksVardiyaSablonu);
						iterator.remove();
					}
				}
				if (!sablonList.isEmpty())
					vardiyaSablonList.addAll(sablonList);
			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.error("fillPdksVardiyaSablonList Hata : " + e.getMessage());
		}

	}

	private void fillCalismaModelList() {
		List<CalismaModeli> list = null;
		VardiyaSablonu pdksVardiyaSablonu = getInstance();
		try {
			HashMap parametreMap = new HashMap();
			parametreMap.put("durum", Boolean.TRUE);
			parametreMap.put("toplamGunGuncelle", Boolean.FALSE);
			if (pdksVardiyaSablonu.getDepartman() != null)
				parametreMap.put("departman.id", pdksVardiyaSablonu.getDepartman().getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			list = pdksEntityController.getObjectByInnerObjectList(parametreMap, CalismaModeli.class);
			if (list.size() > 1)
				list = PdksUtil.sortObjectStringAlanList(list, "getAciklama", null);

			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				CalismaModeli calismaModeli = (CalismaModeli) iterator.next();
				if (calismaModeli.getBagliVardiyaSablonu() != null) {
					iterator.remove();
					continue;
				}

			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.error("fillPdksVardiyaList Hata : " + e.getMessage());
		}
		setModelList(list);

	}

	public void fillPdksVardiyaList() {
		fillCalismaModelList();
		List<Vardiya> pdksList = new ArrayList<Vardiya>();
		VardiyaSablonu pdksVardiyaSablonu = getInstance();
		boolean durum = Boolean.FALSE;
		try {
			HashMap parametreMap = new HashMap();
			parametreMap.put("durum", Boolean.TRUE);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			pdksList = pdksEntityController.getObjectByInnerObjectList(parametreMap, Vardiya.class);
			if (pdksList.size() > 1)
				pdksList = PdksUtil.sortListByAlanAdi(pdksList, "vardiyaNumeric", false);
			if (isKurVardiyaList == null)
				isKurVardiyaList = new ArrayList<Vardiya>();
			else
				isKurVardiyaList.clear();
			if (vardiyaCalisanList == null)
				vardiyaCalisanList = new ArrayList<Vardiya>();
			else
				vardiyaCalisanList.clear();

			isKur = Boolean.FALSE;
			for (Iterator iterator = pdksList.iterator(); iterator.hasNext();) {
				Vardiya pdksVardiya = (Vardiya) iterator.next();
				if (pdksVardiyaSablonu.getDepartman() != null && pdksVardiya.getDepartman() != null && !pdksVardiya.getDepartman().getId().equals(pdksVardiyaSablonu.getDepartman().getId())) {
					iterator.remove();
					continue;
				}
				if (pdksVardiya.isIsKurMu()) {
					isKur = Boolean.TRUE;
					isKurVardiyaList.add(pdksVardiya);
					iterator.remove();
				} else if (pdksVardiya.isCalisma()) {
					if (pdksVardiya.getCalismaSaati() != pdksVardiyaSablonu.getToplamSaat() || pdksVardiya.getCalismaGun() != pdksVardiyaSablonu.getCalismaGunSayisi())
						iterator.remove();
				} else
					isKurVardiyaList.add(pdksVardiya);

			}
			if (!pdksList.isEmpty())
				vardiyaCalisanList.addAll(pdksList);
			if (pdksVardiyaSablonu.isIsKurMu())
				pdksList = isKurVardiyaList;
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.error("fillPdksVardiyaList Hata : " + e.getMessage());
		}
		setVardiyaList(pdksList);
		setVardiyaVar(durum);
	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	public String sablonIskurDegisti() {
		VardiyaSablonu pdksVardiyaSablonu = getInstance();
		vardiyaList = !pdksVardiyaSablonu.isIsKurMu() ? vardiyaCalisanList : isKurVardiyaList;
		return "";
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		fillPdksVardiyaSablonList();
		if (authenticatedUser.isAdmin())
			fillBagliOlduguDepartmanTanimList();
	}

	public void sablonOlustur() {

	}

	public boolean isVardiyaVar() {
		return vardiyaVar;
	}

	public void setVardiyaVar(boolean vardiyaVar) {
		this.vardiyaVar = vardiyaVar;
	}

	public Vardiya getLastVardiya() {
		return lastVardiya;
	}

	public void setLastVardiya(Vardiya lastVardiya) {
		this.lastVardiya = lastVardiya;
	}

	public List<Departman> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<Departman> departmanList) {
		this.departmanList = departmanList;
	}

	public List<Vardiya> getVardiyaList() {
		return vardiyaList;
	}

	public void setVardiyaList(List<Vardiya> vardiyaList) {
		this.vardiyaList = vardiyaList;
	}

	public List<VardiyaSablonu> getVardiyaSablonList() {
		return vardiyaSablonList;
	}

	public void setVardiyaSablonList(List<VardiyaSablonu> vardiyaSablonList) {
		this.vardiyaSablonList = vardiyaSablonList;
	}

	public List<CalismaModeli> getModelList() {
		return modelList;
	}

	public void setModelList(List<CalismaModeli> modelList) {
		this.modelList = modelList;
	}

	public List<Vardiya> getIsKurVardiyaList() {
		return isKurVardiyaList;
	}

	public void setIsKurVardiyaList(List<Vardiya> isKurVardiyaList) {
		this.isKurVardiyaList = isKurVardiyaList;
	}

	public boolean isKur() {
		return isKur;
	}

	public void setKur(boolean isKur) {
		this.isKur = isKur;
	}

	public boolean isKurGoster() {
		return isKurGoster;
	}

	public void setKurGoster(boolean isKurGoster) {
		this.isKurGoster = isKurGoster;
	}

	public List<Vardiya> getVardiyaCalisanList() {
		return vardiyaCalisanList;
	}

	public void setVardiyaCalisanList(List<Vardiya> vardiyaCalisanList) {
		this.vardiyaCalisanList = vardiyaCalisanList;
	}
}
