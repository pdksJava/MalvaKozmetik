package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

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
import org.pdks.entity.Departman;
import org.pdks.entity.PdksPersonelView;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.security.action.StartupAction;
import org.pdks.security.entity.User;

@Name("sirketHome")
public class SirketHome extends EntityHome<Sirket> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5415295499916337573L;
	static Logger logger = Logger.getLogger(SirketHome.class);
	/**
	 * 
	 */
	@RequestParameter
	Long sirketId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = true, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = false, create = true)
	StartupAction startupAction;

	private List<Departman> departmanList = new ArrayList<Departman>();
	private List<Sirket> sirketList = new ArrayList<Sirket>();
	private List<PersonelView> personelList;
	private Boolean istenAyrilanlariEkle, sirketEklenebilir, sirketGrupGoster;
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private String bolumAciklama;
	private List<SelectItem> sirketGrupList;
	private Sirket seciliSirket;
	private Session session;

	@Override
	public Object getId() {
		if (sirketId == null) {
			return super.getId();
		} else {
			return sirketId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public String excelAktar() {
		try {
			Sirket sirket = getInstance();
			ByteArrayOutputStream baosDosya = ortakIslemler.personelExcelDevam(sirket.isLdap(), personelList, ekSahaTanimMap, authenticatedUser, null, session);
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, "personelListesi.xlsx");

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return "";
	}

	public String guncelle(Sirket sirket) {
		fillBagliOlduguDepartmanTanimList();
		sirketGrupList = ortakIslemler.getTanimSelectItem(ortakIslemler.getTanimList(Tanim.TIPI_SIRKET_GRUP, session));
		if (sirket == null) {
			for (Iterator iterator = departmanList.iterator(); iterator.hasNext();) {
				Departman departman = (Departman) iterator.next();
				if (departman.getSirketEklenebilir() == null || departman.getSirketEklenebilir().equals(Boolean.FALSE))
					iterator.remove();

			}
			sirket = new Sirket();
			if (departmanList.size() == 1)
				sirket.setDepartman(departmanList.get(0));
			else if (authenticatedUser.isIK() && !authenticatedUser.isIKAdmin())
				sirket.setDepartman(authenticatedUser.getDepartman());
			Departman departman = sirket.getDepartman();
			if (departman != null) {
				sirket.setFazlaMesaiTalepGirilebilir(departman.isFazlaMesaiTalepGirer());
			}

		}
		if (personelList != null)
			personelList.clear();
		else
			personelList = new ArrayList<PersonelView>();
		setSeciliSirket(sirket);
		return "";
	}

	@Transactional
	public String save() {
		Sirket sirket = seciliSirket;

		try {
			if (sirket.getId() == null)
				sirket.setOlusturanUser(authenticatedUser);
			else {
				sirket.setGuncelleyenUser(authenticatedUser);
				sirket.setGuncellemeTarihi(new Date());
			}
			if (!sirket.isPdksMi())
				sirket.setFazlaMesai(Boolean.FALSE);
			if (!sirket.getFazlaMesai()) {
				sirket.setFazlaMesaiOde(Boolean.FALSE);
			}
			pdksEntityController.saveOrUpdate(session, entityManager, sirket);
			session.flush();
			fillsirketList();

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		return "persisted";

	}

	public void fillsirketList() {
		session.clear();
		List<Sirket> sirketList = new ArrayList<Sirket>();
		HashMap parametreMap = new HashMap();
		if (authenticatedUser.isIK() && !authenticatedUser.isIKAdmin())
			parametreMap.put("departman.id", authenticatedUser.getDepartman().getId());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		sirketList = pdksEntityController.getObjectByInnerObjectList(parametreMap, Sirket.class);
		if (sirketList.size() > 1)
			sirketList = PdksUtil.sortObjectStringAlanList(sirketList, "getAd", null);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Sirket> pasifList = new ArrayList<Sirket>(), pdksHaricList = new ArrayList<Sirket>();
		sirketGrupGoster = false;
		for (Iterator iterator = sirketList.iterator(); iterator.hasNext();) {
			Sirket sirket = (Sirket) iterator.next();
			if (!sirket.getDurum()) {
				pasifList.add(sirket);
				iterator.remove();
			} else {
				if (!sirketGrupGoster)
					sirketGrupGoster = sirket.getSirketGrupId() != null;
				if (!sirket.getFazlaMesai()) {
					pdksHaricList.add(sirket);
					iterator.remove();
				}
			}

		}
		if (!pdksHaricList.isEmpty())
			sirketList.addAll(pdksHaricList);
		if (!pasifList.isEmpty())
			sirketList.addAll(pasifList);
		pasifList = null;
		pdksHaricList = null;
		fillBagliOlduguDepartmanTanimList();
		startupAction.fillSirketList(session);
		setSirketList(sirketList);
	}

	public void fillBagliOlduguDepartmanTanimList() {
		sirketEklenebilir = false;
		List tanimList = null;
		HashMap parametreMap = new HashMap();
		parametreMap.put("durum", Boolean.TRUE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			tanimList = pdksEntityController.getObjectByInnerObjectList(parametreMap, Departman.class);
			for (Iterator iterator = tanimList.iterator(); iterator.hasNext();) {
				Departman departman = (Departman) iterator.next();
				if (!sirketEklenebilir)
					sirketEklenebilir = departman.getSirketEklenebilir() != null && departman.getSirketEklenebilir();

			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		setDepartmanList(tanimList);
	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	public void fillPersonelList() throws Exception {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		Sirket sirket = seciliSirket;
		List<PersonelView> list = new ArrayList<PersonelView>();
		HashMap parametreMap = new HashMap();
		if (!istenAyrilanlariEkle) {
			Date bugun = PdksUtil.getDate(Calendar.getInstance().getTime());
			parametreMap.put("pdksPersonel.sskCikisTarihi>=", bugun);
			parametreMap.put("pdksPersonel.iseBaslamaTarihi<=", bugun);
			parametreMap.put("pdksPersonel.durum=", Boolean.TRUE);
		}

		parametreMap.put("pdksPersonel.sirket.id=", sirket.getId());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			if (sirket.getPdks())
				list = ortakIslemler.getPersonelViewList(pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, PdksPersonelView.class));
			else
				list.clear();

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			PdksUtil.addMessageError("Hata : " + e.getMessage());
		}

		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			PersonelView personelView = (PersonelView) iterator.next();
			Personel personel = personelView.getPdksPersonel();
			if (personel == null || personel.getSicilNo() == null || personel.getSicilNo().trim().equals("")) {
				iterator.remove();
				continue;
			}
		}
		if (!list.isEmpty())
			fillEkSahaTanim();

		setPersonelList(list);
	}

	private void fillEkSahaTanim() {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.TRUE, null);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
		setSirketList((List<Sirket>) sonucMap.get("sirketList"));
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);

		fillsirketList();
	}

	public List<Departman> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<Departman> departmanList) {
		this.departmanList = departmanList;
	}

	public List<PersonelView> getPersonelList() {
		return personelList;
	}

	public void setPersonelList(List<PersonelView> personelList) {
		this.personelList = personelList;
	}

	public Boolean getIstenAyrilanlariEkle() {
		return istenAyrilanlariEkle;
	}

	public void setIstenAyrilanlariEkle(Boolean istenAyrilanlariEkle) {
		this.istenAyrilanlariEkle = istenAyrilanlariEkle;
	}

	public List<Sirket> getSirketList() {
		return sirketList;
	}

	public void setSirketList(List<Sirket> sirketList) {
		this.sirketList = sirketList;
	}

	public HashMap<String, List<Tanim>> getEkSahaListMap() {
		return ekSahaListMap;
	}

	public void setEkSahaListMap(HashMap<String, List<Tanim>> ekSahaListMap) {
		this.ekSahaListMap = ekSahaListMap;
	}

	public TreeMap<String, Tanim> getEkSahaTanimMap() {
		return ekSahaTanimMap;
	}

	public void setEkSahaTanimMap(TreeMap<String, Tanim> ekSahaTanimMap) {
		this.ekSahaTanimMap = ekSahaTanimMap;
	}

	public Sirket getSeciliSirket() {
		return seciliSirket;
	}

	public void setSeciliSirket(Sirket seciliSirket) {
		this.seciliSirket = seciliSirket;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Boolean getSirketEklenebilir() {
		return sirketEklenebilir;
	}

	public void setSirketEklenebilir(Boolean sirketEklenebilir) {
		this.sirketEklenebilir = sirketEklenebilir;
	}

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}

	public List<SelectItem> getSirketGrupList() {
		return sirketGrupList;
	}

	public void setSirketGrupList(List<SelectItem> sirketGrupList) {
		this.sirketGrupList = sirketGrupList;
	}

	public Boolean getSirketGrupGoster() {
		return sirketGrupGoster;
	}

	public void setSirketGrupGoster(Boolean sirketGrupGoster) {
		this.sirketGrupGoster = sirketGrupGoster;
	}

}
