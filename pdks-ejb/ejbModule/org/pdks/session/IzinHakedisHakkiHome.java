package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.pdks.entity.Departman;
import org.pdks.entity.IzinHakedisHakki;
import org.pdks.security.entity.User;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.validator.Max;
import org.hibernate.validator.Min;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

@Name("izinHakedisHakkiHome")
public class IzinHakedisHakkiHome extends EntityHome<IzinHakedisHakki> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2035314165616514750L;
	static Logger logger = Logger.getLogger(IzinHakedisHakkiHome.class);

	@RequestParameter
	Long izinHakedisHakkiId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	private List<IzinHakedisHakki> izinHakedisHakkiList = new ArrayList<IzinHakedisHakki>();
	private List<Departman> departmanList = new ArrayList<Departman>();
	private List<SelectItem> yasTipiList = new ArrayList<SelectItem>();

	private int basYil;
	private int izinSure;
	private int bitisYil;
	private String yasTipi;
	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	@Override
	public Object getId() {
		if (izinHakedisHakkiId == null) {
			return super.getId();
		} else {
			return izinHakedisHakkiId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public void kayitEkle() {
		basYil = 0;
		izinSure = 0;
		bitisYil = 0;
		setYasTipi("");
		setInstance(new IzinHakedisHakki());
		Departman departman = null;
		if (departmanList.size() == 1)
			departman = departmanList.get(0);
		if (departman != null)
			getInstance().setDepartman(departman);
		fillYasTipiList();
	}

	@Transactional
	public String save() {
		String cikis = "";

		try {
			IzinHakedisHakki hakedisHakki = getInstance();
			hakedisHakki.setYasTipi(new Integer(yasTipi));
			List<IzinHakedisHakki> izinHakedisHakkiList = new ArrayList<IzinHakedisHakki>();
			boolean suaDurum = hakedisHakki.getDepartman().isAdminMi() ? hakedisHakki.isSuaDurum() : false;
			try {
				HashMap parametreMap = new HashMap();
				parametreMap.put("departman=", hakedisHakki.getDepartman());
				parametreMap.put("suaDurum=", suaDurum);
				parametreMap.put("yasTipi=", hakedisHakki.getYasTipi());
				parametreMap.put("kidemYili>=", basYil);
				parametreMap.put("kidemYili<=", bitisYil);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				izinHakedisHakkiList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, IzinHakedisHakki.class);
				parametreMap.clear();
				for (IzinHakedisHakki izinHakedisHakki : izinHakedisHakkiList)
					parametreMap.put(String.valueOf(izinHakedisHakki.getKidemYili()), izinHakedisHakki);
				for (int i = basYil; i <= bitisYil; i++) {
					IzinHakedisHakki izinHakedisHakki = (IzinHakedisHakki) (parametreMap.containsKey(String.valueOf(i)) ? parametreMap.get(String.valueOf(i)) : new IzinHakedisHakki());
					if (izinHakedisHakki.getId() == null) {
						izinHakedisHakki.setKidemYili(i);
						izinHakedisHakki.setOlusturanUser(authenticatedUser);
					} else if (izinHakedisHakki.getIzinSuresi() != izinSure) {
						izinHakedisHakki.setGuncellemeTarihi(new Date());
						izinHakedisHakki.setGuncelleyenUser(authenticatedUser);
					}
					izinHakedisHakki.setSuaDurum(suaDurum);
					izinHakedisHakki.setDepartman(hakedisHakki.getDepartman());
					izinHakedisHakki.setYasTipi(hakedisHakki.getYasTipi());
					izinHakedisHakki.setIzinSuresi((short) izinSure);
					pdksEntityController.saveOrUpdate(session, entityManager, izinHakedisHakki);

				}
				session.flush();

				fillIzinHakedisHakkiList();
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				logger.error("fillIzinHakedisHakkiList Hata : " + e.getMessage());
			}

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return cikis;

	}

	public Long getIzinHakedisHakkiId() {
		return izinHakedisHakkiId;
	}

	public void setIzinHakedisHakkiId(Long izinHakedisHakkiId) {
		this.izinHakedisHakkiId = izinHakedisHakkiId;
	}

	@Min(value = 0)
	public int getBasYil() {
		return basYil;
	}

	public void setBasYil(int basYil) {
		this.basYil = basYil;
	}

	@Max(value = 40)
	public int getBitisYil() {
		return bitisYil;
	}

	public void setBitisYil(int bitisYil) {
		this.bitisYil = bitisYil;
	}

	@Min(value = 0)
	@Max(value = 40)
	public int getIzinSure() {
		return izinSure;
	}

	public void fillYasTipiList() {
		IzinHakedisHakki hakedisHakki = getInstance();
		List<SelectItem> list = new ArrayList<SelectItem>();
		list.add(new SelectItem(String.valueOf(IzinHakedisHakki.YAS_TIPI_COCUK), IzinHakedisHakki.getYasTipiStr(IzinHakedisHakki.YAS_TIPI_COCUK, hakedisHakki.getDepartman(), hakedisHakki.isSuaDurum())));
		list.add(new SelectItem(String.valueOf(IzinHakedisHakki.YAS_TIPI_GENC), IzinHakedisHakki.getYasTipiStr(IzinHakedisHakki.YAS_TIPI_GENC, hakedisHakki.getDepartman(), hakedisHakki.isSuaDurum())));
		list.add(new SelectItem(String.valueOf(IzinHakedisHakki.YAS_TIPI_YASLI), IzinHakedisHakki.getYasTipiStr(IzinHakedisHakki.YAS_TIPI_YASLI, hakedisHakki.getDepartman(), hakedisHakki.isSuaDurum())));
		setYasTipiList(list);

	}

	public void fillDepartmanList() {
		List<Departman> list = null;
		try {
			list = ortakIslemler.fillDepartmanTanimList(session);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		setDepartmanList(list);

	}

	public void fillIzinHakedisHakkiList() {
		List<IzinHakedisHakki> izinHakedisHakkiList = new ArrayList();
		HashMap<String, IzinHakedisHakki> hashMap = new HashMap<String, IzinHakedisHakki>();
		try {
			HashMap parametreMap = new HashMap();
			if (!authenticatedUser.isAdmin())
				parametreMap.put("departman.id", authenticatedUser.getDepartman().getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			izinHakedisHakkiList = pdksEntityController.getObjectByInnerObjectList(parametreMap, IzinHakedisHakki.class);
			if (!izinHakedisHakkiList.isEmpty()) {

				List<IzinHakedisHakki> list = new ArrayList();
				izinHakedisHakkiList = PdksUtil.sortListByAlanAdi(izinHakedisHakkiList, "kidemYili", Boolean.FALSE);
				IzinHakedisHakki izinHakedis = null;
				for (IzinHakedisHakki izinHakedisHakki : izinHakedisHakkiList) {
					IzinHakedisHakki hakedisHakki = (IzinHakedisHakki) izinHakedisHakki;
					if (izinHakedisHakki.getDepartman() == null)
						continue;
					String key = izinHakedisHakki.getDepartman().getId() + "_" + hakedisHakki.isSuaDurum() + "_" + izinHakedisHakki.getYasTipi() + "_" + hakedisHakki.getIzinSuresi();
					if (hashMap.containsKey(key)) {
						izinHakedis = hashMap.get(key);
						if (hakedisHakki.getKidemYili() > izinHakedis.getMaxGun())
							izinHakedis.setMaxGun(hakedisHakki.getKidemYili());
						if (hakedisHakki.getKidemYili() < izinHakedis.getMinGun())
							izinHakedis.setMinGun(hakedisHakki.getKidemYili());
					} else {
						izinHakedis = (IzinHakedisHakki) hakedisHakki.clone();
						izinHakedis.setMinGun(hakedisHakki.getKidemYili());
						izinHakedis.setMaxGun(hakedisHakki.getKidemYili());
					}
					hashMap.put(key, izinHakedis);

				}

				izinHakedisHakkiList.addAll(list);

			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.error("fillIzinHakedisHakkiList Hata : " + e.getMessage());
		}

		izinHakedisHakkiList = PdksUtil.sortObjectStringAlanList(new ArrayList(hashMap.values()), "getSort", null);

		setIzinHakedisHakkiList(izinHakedisHakkiList);
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
		fillIzinHakedisHakkiList();
		fillDepartmanList();
	}

	public void setIzinSure(int izinSure) {
		this.izinSure = izinSure;
	}

	public List<IzinHakedisHakki> getIzinHakedisHakkiList() {
		return izinHakedisHakkiList;
	}

	public void setIzinHakedisHakkiList(List<IzinHakedisHakki> izinHakedisHakkiList2) {
		this.izinHakedisHakkiList = izinHakedisHakkiList2;
	}

	public List<Departman> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<Departman> departmanList) {
		this.departmanList = departmanList;
	}

	public List<SelectItem> getYasTipiList() {
		return yasTipiList;
	}

	public void setYasTipiList(List<SelectItem> yasTipiList) {
		this.yasTipiList = yasTipiList;
	}

	public String getYasTipi() {
		return yasTipi;
	}

	public void setYasTipi(String yasTipi) {
		this.yasTipi = yasTipi;
	}

}
