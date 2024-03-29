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
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.Kapi;
import org.pdks.entity.KapiKGS;
import org.pdks.entity.KapiView;
import org.pdks.entity.Tanim;
import org.pdks.security.entity.User;

@Name("kapiHome")
public class KapiHome extends EntityHome<Kapi> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7059186935908887645L;
	static Logger logger = Logger.getLogger(KapiHome.class);
	/**
	 * 
	 */
	@RequestParameter
	Long kapiId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;

	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	private List<KapiKGS> kapiKGSList = new ArrayList<KapiKGS>();
	private List<KapiKGS> tanimsizKapiList = new ArrayList<KapiKGS>();
	private List<Kapi> kapiList = new ArrayList<Kapi>();
	private List<Tanim> kapiTipiList = new ArrayList<Tanim>();
	private KapiView kapiView;
	private boolean kgsGuncelle;
	private String birdenFazlaKGSSirketSQL;
	private Session session;

	@Override
	public Object getId() {
		if (kapiId == null) {
			return super.getId();
		} else {
			return kapiId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	@Transactional
	public String save() {
		Kapi kapi = getInstance();
		boolean yeni = kapi.getId() == null;
		try {
			if (yeni)
				kapi.setOlusturanUser(authenticatedUser);
			else {
				kapi.setGuncelleyenUser(authenticatedUser);
				kapi.setGuncellemeTarihi(new Date());
			}
			pdksEntityController.saveOrUpdate(session, entityManager, kapi);
			session.flush();
			// HashMap parametreMap = new HashMap();
			// parametreMap.put("kapi.id", kapi.getId());
			// if (session != null)
			// parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			// KapiView kapiView = ortakIslemler.getKapiView(parametreMap);
			fillkapiList();

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		return "persisted";

	}

	public String fillkapiList() {
		session.clear();
		List<Kapi> kapiList = new ArrayList<Kapi>();
		HashMap parametreMap = new HashMap();
		try {
			birdenFazlaKGSSirketSQL = ortakIslemler.getBirdenFazlaKGSSirketSQL(null, null, session);
			String sirketStr = "";
			if (!birdenFazlaKGSSirketSQL.equals(""))
				sirketStr = "_SIRKET";
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT P.*   FROM  FN_KAPI" + sirketStr + "_KGS_LIST()  P ");
			String str = " INNER  JOIN " + Kapi.TABLE_NAME + " K ON K." + Kapi.COLUMN_NAME_KGS_ID + "=P." + KapiKGS.COLUMN_NAME_ID;
			if (kapiView.getKapiAciklama() != null && kapiView.getKapiAciklama().trim().length() > 0) {
				sb.append(str);
				sb.append(" AND K." + Kapi.COLUMN_NAME_ACIKLAMA + " LIKE :k");
				parametreMap.put("k", "%" + kapiView.getKapiAciklama().trim() + "%");
				str = "";
			}
			if (kapiView.getKapi().getTipi() != null) {
				sb.append(str);
				sb.append(" AND K." + Kapi.COLUMN_NAME_KAPI_TIPI + " =:t");
				parametreMap.put("t", kapiView.getKapi().getTipi().getId());
				str = "";
			}
			if (kapiView.getKapiKGSAciklama() != null && kapiView.getKapiKGSAciklama().trim().length() > 0) {
				sb.append(" WHERE P." + KapiKGS.COLUMN_NAME_ACIKLAMA + " LIKE :ka");
				parametreMap.put("ka", "%" + kapiView.getKapiKGSAciklama().trim() + "%");
			}
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<KapiKGS> kapiKGSList = pdksEntityController.getObjectBySQLList(sb, parametreMap, KapiKGS.class);

			List<KapiView> list = new ArrayList<KapiView>();
			for (KapiKGS kapiKGS : kapiKGSList) {
				if (kapiKGS.getKartYonu() == 1 || kapiKGS.getKartYonu() == 2)
					list.add(kapiKGS.getKapiView());
			}
			kapiKGSList = null;
			List<Long> idList = new ArrayList<Long>();
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				KapiView kapiView = (KapiView) iterator.next();
				idList.add(kapiView.getId());
			}
			if (!idList.isEmpty()) {
				List<Kapi> tanimliPkdsList = new ArrayList<Kapi>(), tanimliList = new ArrayList<Kapi>();
				HashMap fields = new HashMap();
				sb = new StringBuffer();
				sb.append("SELECT P.*   FROM  " + KapiKGS.TABLE_NAME + "  P   WITH(nolock) ");
				sb.append(" where  P." + KapiKGS.COLUMN_NAME_ID + " :k  ");
				sb.append(" ORDER BY  " + KapiKGS.COLUMN_NAME_ACIKLAMA);
				fields.put("k", idList);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				kapiKGSList = pdksEntityController.getObjectBySQLList(sb, fields, KapiKGS.class);
				list.clear();
				for (Iterator iterator = kapiKGSList.iterator(); iterator.hasNext();) {
					KapiKGS kapiKGS = (KapiKGS) iterator.next();
					KapiView kapiView = kapiKGS.getKapiView();
					list.add(kapiView);
					Kapi kapi = kapiKGS.getKapi();
					if (kapi != null) {
						if (!kapi.getPdks())
							tanimliList.add(kapi);
						else
							tanimliPkdsList.add(kapi);
					} else {
						kapi = new Kapi();
						kapi.setAciklama(kapiView.getKapiAciklama());
						kapi.setOlusturmaTarihi(kapiView.getKapiKGS().getIslemZamani());
						kapi.setKapiKGS(kapiView.getKapiKGS());
						kapi.setDurum(Boolean.TRUE);
						kapi.setPdks(Boolean.FALSE);
						kapiList.add(kapi);
					}
					kapi.setKapiView(kapiView);

				}
				kapiKGSList = null;
				if (!tanimliPkdsList.isEmpty())
					kapiList.addAll(tanimliPkdsList);
				if (!tanimliList.isEmpty())
					kapiList.addAll(tanimliList);
				tanimliList = null;
				tanimliPkdsList = null;
			}
			idList = null;

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			kapiList = new ArrayList<Kapi>();
		}
		fillKapiTipleri();
		setkapiList(kapiList);
		return "";
	}

	public void fillkapiKGSList() {

		List list = null;
		HashMap parametreMap = new HashMap();
		parametreMap.put("kapi=", null);
		parametreMap.put("durum=", Boolean.TRUE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			List<KapiKGS> kapiKGSList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, KapiKGS.class);

			if (!kapiKGSList.isEmpty()) {
				list = new ArrayList();
				list.addAll(kapiKGSList);

			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		setTanimsizKapiList(list);
	}

	public void fillKapiTipleri() {

		List<Tanim> list = null;
		HashMap parametreMap = new HashMap();
		parametreMap.put("tipi ", Tanim.TIPI_KAPI_TIPI);
		parametreMap.put("durum ", Boolean.TRUE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			list = pdksEntityController.getObjectByInnerObjectList(parametreMap, Tanim.class);

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		setKapiTipiList(list);

	}

	public String kapiGuncelle(Kapi kapi) {
		kgsGuncelle = Boolean.FALSE;
		if (kapi.getId() == null) {
			int girisYonu = -1;
			if (kapi.getKapiKGS() != null)
				girisYonu = kapi.getKapiKGS().getKartYonu();
			if (girisYonu >= 0 && kapiTipiList != null) {
				String erpKodu = String.valueOf(girisYonu);
				for (Iterator iterator = kapiTipiList.iterator(); iterator.hasNext();) {
					Tanim tipi = (Tanim) iterator.next();
					if (tipi.getErpKodu() != null && tipi.getErpKodu().equals(erpKodu)) {
						kapi.setTipi(tipi);
						kapi.setAciklama(kapi.getKapiKGS().getAciklamaKGS());
						break;
					}

				}

			}
		}
		setInstance(kapi);
		return "";
	}

	public String kapiEkle() {
		kgsGuncelle = Boolean.TRUE;
		this.clearInstance();
		if (getInstance().getId() == null)
			fillkapiKGSList();
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
		kapiView = new KapiView();
		kapiView.setKapi(new Kapi());
		session.clear();
		fillKapiTipleri();
		setkapiList(new ArrayList<Kapi>());
	}

	public KapiView getKapiView() {
		return kapiView;
	}

	public void setKapiView(KapiView kapiView) {
		this.kapiView = kapiView;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public List<Tanim> getKapiTipiList() {
		return kapiTipiList;
	}

	public void setKapiTipiList(List<Tanim> kapiTipiList) {
		this.kapiTipiList = kapiTipiList;
	}

	public List<Kapi> getkapiList() {
		return kapiList;
	}

	public void setkapiList(List<Kapi> kapiList) {
		this.kapiList = kapiList;
	}

	public List<KapiKGS> getkapiKGSList() {
		return kapiKGSList;
	}

	public void setkapiKGSList(List<KapiKGS> kapiKGSList) {
		this.kapiKGSList = kapiKGSList;
	}

	public List<KapiKGS> getTanimsizKapiList() {
		return tanimsizKapiList;
	}

	public void setTanimsizKapiList(List<KapiKGS> tanimsizKapiList) {
		this.tanimsizKapiList = tanimsizKapiList;
	}

	public List<KapiKGS> getKapiKGSList() {
		return kapiKGSList;
	}

	public void setKapiKGSList(List<KapiKGS> kapiKGSList) {
		this.kapiKGSList = kapiKGSList;
	}

	public List<Kapi> getKapiList() {
		return kapiList;
	}

	public void setKapiList(List<Kapi> kapiList) {
		this.kapiList = kapiList;
	}

	public boolean isKgsGuncelle() {
		return kgsGuncelle;
	}

	public void setKgsGuncelle(boolean kgsGuncelle) {
		this.kgsGuncelle = kgsGuncelle;
	}

	public String getBirdenFazlaKGSSirketSQL() {
		return birdenFazlaKGSSirketSQL;
	}

	public void setBirdenFazlaKGSSirketSQL(String birdenFazlaKGSSirketSQL) {
		this.birdenFazlaKGSSirketSQL = birdenFazlaKGSSirketSQL;
	}
}
