package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.pdks.entity.Departman;
import org.pdks.entity.Kapi;
import org.pdks.entity.Sirket;
import org.pdks.entity.YemekKartsiz;
import org.pdks.entity.YemekOgun;
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

@Name("yemekHome")
public class YemekHome extends EntityHome<YemekOgun> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4110678952372352196L;
	static Logger logger = Logger.getLogger(YemekHome.class);

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

	private List<YemekOgun> yemekList = new ArrayList<YemekOgun>();
	private List<Sirket> sirketList = new ArrayList<Sirket>();
	private List<Departman> departmanList = new ArrayList<Departman>();
	private List<YemekKartsiz> kartsizYemekList = new ArrayList<YemekKartsiz>();
	private YemekKartsiz yemekKartsiz;
	private List<Kapi> yemekHaneList;

	private Date basTarih, bitTarih;
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

	public String kartSizYemekGetir(YemekKartsiz pdksYemekKartsiz) {
		if (pdksYemekKartsiz == null) {
			pdksYemekKartsiz = new YemekKartsiz();
			pdksYemekKartsiz.setTarih(new Date());
			yemekKartsiz = pdksYemekKartsiz;
			fillYemekOgunler();
			fillSirketList();
		} else
			yemekKartsiz = pdksYemekKartsiz;

		return "";
	}

	public void fillYemekHaneler() {
		HashMap fields = new HashMap();
		fields.put("tipi.kodu=", Kapi.TIPI_KODU_YEMEKHANE);
		fields.put("durum=", Boolean.TRUE);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Kapi> list = pdksEntityController.getObjectByInnerObjectListInLogic(fields, Kapi.class);
		setYemekHaneList(list);

	}

	public void fillYemekOgunler() {
		HashMap fields = new HashMap();
		fields.put("bitTarih>=", yemekKartsiz.getTarih());
		fields.put("basTarih<=", yemekKartsiz.getTarih());
		fields.put("durum=", Boolean.TRUE);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<YemekOgun> ogunList = pdksEntityController.getObjectByInnerObjectListInLogic(fields, YemekOgun.class);
		setYemekList(ogunList);

	}

	public void fillSirketList() {
		HashMap fields = new HashMap();
		fields.put("durum", Boolean.TRUE);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Sirket> list = pdksEntityController.getObjectByInnerObjectList(fields, Sirket.class);
		if (list.size() > 1)
			list = PdksUtil.sortObjectStringAlanList(list, "getAd", null);
		setSirketList(list);

	}

	public void fillKartSizYemek() {
		HashMap fields = new HashMap();
		fields.put("tarih>=", basTarih);
		fields.put("tarih<=", bitTarih);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<YemekKartsiz> kartsizList = pdksEntityController.getObjectByInnerObjectListInLogic(fields, YemekKartsiz.class);
		if (kartsizList.size() > 1)
			kartsizList = PdksUtil.sortListByAlanAdi(kartsizList, "tarih", Boolean.TRUE);
		setKartsizYemekList(kartsizList);

	}

	@Transactional
	public String saveKartSizYemek() {
		String mesaj = null;
		if (yemekKartsiz.getId() == null) {
			yemekKartsiz.setOlusturanUser(authenticatedUser);
			yemekKartsiz.setOlusturmaTarihi(new Date());
			HashMap fields = new HashMap();
			fields.put("tarih", yemekKartsiz.getTarih());
			fields.put("yemekOgun.id", yemekKartsiz.getYemekOgun().getId());
			fields.put("yemekKapi.id", yemekKartsiz.getYemekKapi().getId());
			fields.put("sirket.id", yemekKartsiz.getSirket().getId());
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			YemekKartsiz kartsiz = (YemekKartsiz) pdksEntityController.getObjectByInnerObject(fields, YemekKartsiz.class);
			if (kartsiz != null)
				mesaj = yemekKartsiz.getSirket().getAd() + " " + yemekKartsiz.getYemekKapi().getAciklama() + " de " + yemekKartsiz.getYemekOgun().getYemekAciklama() + " sistemde kayıtlıdır!";
			kartsiz = null;
			fields = null;
		} else {
			yemekKartsiz.setGuncelleyenUser(authenticatedUser);
			yemekKartsiz.setGuncellemeTarihi(new Date());
		}
		try {
			if (mesaj == null) {
				pdksEntityController.saveOrUpdate(session, entityManager, yemekKartsiz);
				session.flush();
				fillKartSizYemek();
				yemekKartsiz = new YemekKartsiz();
			} else
				PdksUtil.addMessageError(mesaj);

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			PdksUtil.addMessageError("Hatalı kayıt");
		}

		return "";

	}

	@Transactional
	public String save() {
		YemekOgun pdksYemek = getInstance();
		boolean yeni = pdksYemek.getId() == null;
		try {
			if (yeni)
				pdksYemek.setOlusturanUser(authenticatedUser);
			else {
				pdksYemek.setGuncelleyenUser(authenticatedUser);
				pdksYemek.setGuncellemeTarihi(new Date());
			}
			pdksEntityController.saveOrUpdate(session, entityManager, pdksYemek);
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
		List<YemekOgun> pdksYemekList = new ArrayList<YemekOgun>();
		HashMap parametreMap = new HashMap();
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		pdksYemekList = pdksEntityController.getObjectByInnerObjectList(parametreMap, YemekOgun.class);
		setYemekList(pdksYemekList);
	}

	public void yemekEkle() {
		this.clearInstance();

		if (getInstance().getId() == null)
			fillPdksYemekList();

	}

	public void instanceKartsizRefresh() {
		if (yemekKartsiz.getId() != null)
			session.refresh(yemekKartsiz);
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

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaExtraGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		if (basTarih == null) {
			Calendar cal = Calendar.getInstance();
			bitTarih = PdksUtil.getDate(cal.getTime());
			cal.add(Calendar.MONTH, -1);
			cal.set(Calendar.DATE, 1);
			basTarih = PdksUtil.getDate(cal.getTime());
		}
		fillKartSizYemek();
		fillYemekHaneler();
	}

	public List<YemekOgun> getYemekList() {
		return yemekList;
	}

	public void setYemekList(List<YemekOgun> value) {
		this.yemekList = value;
	}

	public List<Departman> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<Departman> value) {
		this.departmanList = value;
	}

	public Date getBasTarih() {
		return basTarih;
	}

	public void setBasTarih(Date basTarih) {
		this.basTarih = basTarih;
	}

	public Date getBitTarih() {
		return bitTarih;
	}

	public void setBitTarih(Date bitTarih) {
		this.bitTarih = bitTarih;
	}

	public List<YemekKartsiz> getKartsizYemekList() {
		return kartsizYemekList;
	}

	public void setKartsizYemekList(List<YemekKartsiz> kartsizYemekList) {
		this.kartsizYemekList = kartsizYemekList;
	}

	public YemekKartsiz getYemekKartsiz() {
		return yemekKartsiz;
	}

	public void setYemekKartsiz(YemekKartsiz yemekKartsiz) {
		this.yemekKartsiz = yemekKartsiz;
	}

	public List<Kapi> getYemekHaneList() {
		return yemekHaneList;
	}

	public void setYemekHaneList(List<Kapi> yemekHaneList) {
		this.yemekHaneList = yemekHaneList;
	}

	public List<Sirket> getSirketList() {
		return sirketList;
	}

	public void setSirketList(List<Sirket> sirketList) {
		this.sirketList = sirketList;
	}

}
