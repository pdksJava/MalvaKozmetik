package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.Query;

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
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.PersonelIzinDetay;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.TempIzin;
import org.pdks.security.entity.User;

@Name("fazlaMesaiIzinHome")
public class FazlaMesaiIzinHome extends EntityHome<PersonelIzin> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1297221062917679868L;
	static Logger logger = Logger.getLogger(FazlaMesaiIzinHome.class);

	@RequestParameter
	Long personelIzinId;

	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false)
	User authenticatedUser;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	private List<PersonelIzin> personelizinList = new ArrayList<PersonelIzin>();
	private List<TempIzin> pdksPersonelList = new ArrayList<TempIzin>();
	private TempIzin updateTempIzin;
	private PersonelIzin updateIzin;

	private List<String> yilList = new ArrayList<String>();
	private List<Sirket> sirketList = new ArrayList<Sirket>();
	private String ad = "", soyad = "", sicilNo = "";
	private Tanim seciliTesis, seciliEkSaha1, seciliEkSaha2, seciliEkSaha3, seciliEkSaha4;
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private Sirket sirket;

	private Date basTarih, bitTarih;
	private Double bakiyeSuresi;
	private List<PersonelIzinDetay> harcananIzinler;
	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Double getIzinSuresi() {
		return izinSuresi;
	}

	public void setIzinSuresi(Double izinSuresi) {
		this.izinSuresi = izinSuresi;
	}

	private Integer yil;

	private Double izinSuresi;

	@In(required = false)
	FacesMessages facesMessages;

	@Override
	public Object getId() {
		if (personelIzinId == null) {
			return super.getId();
		} else {
			return personelIzinId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	private void fillEkSahaTanim() {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
		setSirketList((List<Sirket>) sonucMap.get("sirketList"));

	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		fillEkSahaTanim();
		setBitTarih(new Date());
		Calendar cal = Calendar.getInstance();
		cal.setTime(bitTarih);
		cal.add(Calendar.YEAR, -1);
		setBasTarih(cal.getTime());
		setSirket(null);
		if (authenticatedUser.isIK() || authenticatedUser.isAdmin()) {

			setpdksPersonelList(new ArrayList<TempIzin>());
		} else
			setSirket(authenticatedUser.getPdksPersonel().getSirket());
		setpdksPersonelList(new ArrayList<TempIzin>());
	}

	public String guncelle(PersonelIzin izin) {
		setBakiyeSuresi(izin.getKalanIzin());
		setIzinSuresi(izin.getIzinSuresi());
		setUpdateIzin((PersonelIzin) izin.clone());
		return "";

	}

	@Transactional
	public String sil(PersonelIzin izin) {
		String durum = "persist";
		try {

			ortakIslemler.bakiyeIzinSil(izin, session);
			session.flush();
			fillIzinList();

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			durum = "";
			PdksUtil.addMessageError(e.getMessage());
		}
		return durum;
	}

	@Transactional
	public String kaydet() {
		String durum = "persist";
		try {
			double harcananSure = updateIzin.getHarcananIzin() - (updateIzin.getKullanilanIzinSuresi() != null ? updateIzin.getKullanilanIzinSuresi() : 0);
			if (getIzinSuresi() < harcananSure)
				throw new Exception("Toplam süre en az " + harcananSure + " olabilir.");
			if (updateIzin.getBakiyeSuresi() < 0 || getBakiyeSuresi() > getIzinSuresi())
				throw new Exception("Bakiye süresi en az 0 en fazla " + getIzinSuresi() + " olabilir.");
			double kullanilanIzinSuresi = getIzinSuresi() - getBakiyeSuresi() - harcananSure;
			Query query = null;
			String queryStr = "UPDATE PersonelIzin SET guncelleyenUser_id=?,guncellemeTarihi=?, KULLANILAN_IZIN_SURESI=? where id=?";
			query = entityManager.createNativeQuery(queryStr);
			query.setParameter(1, authenticatedUser.getId());
			query.setParameter(2, new Date());
			query.setParameter(3, kullanilanIzinSuresi);
			query.setParameter(4, updateIzin.getId());
			query.executeUpdate();
			HashMap parametreMap = new HashMap();
			parametreMap.put("id", updateIzin.getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			PersonelIzin izin = (PersonelIzin) pdksEntityController.getObjectByInnerObject(parametreMap, PersonelIzin.class);
			session.refresh(izin);
			session.flush();
			fillIzinList();

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			durum = "";
			PdksUtil.addMessageError(e.getMessage());

		}
		return durum;

	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	public void fillIzinList() {
		setInstance(null);
		HashMap<Long, TempIzin> izinMap = new HashMap<Long, TempIzin>();
		ArrayList<String> sicilNoList = ortakIslemler.getPersonelSicilNo(ad, soyad, sicilNo, sirket, seciliTesis, seciliEkSaha1, seciliEkSaha2, seciliEkSaha3, seciliEkSaha4, Boolean.TRUE, session);
		if (sicilNoList != null && !sicilNoList.isEmpty())
			izinMap = ortakIslemler.fazlaMesaiIzinListesiOlustur(sicilNoList, sirket, basTarih, bitTarih, Boolean.TRUE, session);

		setpdksPersonelList(new ArrayList<TempIzin>(izinMap.values()));

	}

	public void izinGoster(TempIzin tempIzin) {
		setUpdateTempIzin(tempIzin);
		HashMap parametreMap = new HashMap();
		if (!tempIzin.getIzinler().isEmpty())
			parametreMap.put("id", tempIzin.getIzinler().clone());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PersonelIzin> izinList1 = tempIzin.getIzinler().isEmpty() ? new ArrayList<PersonelIzin>() : pdksEntityController.getObjectByInnerObjectList(parametreMap, PersonelIzin.class);

		if (izinList1.size() > 1)
			izinList1 = PdksUtil.sortListByAlanAdi(izinList1, "id", Boolean.FALSE);
		setPersonelizinList(izinList1);

	}

	public List<PersonelIzin> getPersonelizinList() {
		return personelizinList;
	}

	public void setPersonelizinList(List<PersonelIzin> personelizinList) {
		this.personelizinList = personelizinList;
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

	public List<String> getYilList() {
		return yilList;
	}

	public void setYilList(List<String> yilList) {
		this.yilList = yilList;
	}

	public Integer getYil() {
		return yil;
	}

	public void setYil(Integer yil) {
		this.yil = yil;
	}

	public List<Sirket> getSirketList() {
		return sirketList;
	}

	public void setSirketList(List<Sirket> sirketList) {
		this.sirketList = sirketList;
	}

	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket sirket) {
		this.sirket = sirket;
	}

	public String getAd() {
		return ad;
	}

	public void setAd(String ad) {
		this.ad = ad;
	}

	public String getSoyad() {
		return soyad;
	}

	public void setSoyad(String soyad) {
		this.soyad = soyad;
	}

	public String getSicilNo() {
		return sicilNo;
	}

	public void setSicilNo(String sicilNo) {
		this.sicilNo = sicilNo;
	}

	public Tanim getSeciliEkSaha1() {
		return seciliEkSaha1;
	}

	public void setSeciliEkSaha1(Tanim seciliEkSaha1) {
		this.seciliEkSaha1 = seciliEkSaha1;
	}

	public Tanim getSeciliEkSaha2() {
		return seciliEkSaha2;
	}

	public void setSeciliEkSaha2(Tanim seciliEkSaha2) {
		this.seciliEkSaha2 = seciliEkSaha2;
	}

	public Tanim getSeciliEkSaha3() {
		return seciliEkSaha3;
	}

	public void setSeciliEkSaha3(Tanim seciliEkSaha3) {
		this.seciliEkSaha3 = seciliEkSaha3;
	}

	public Tanim getSeciliEkSaha4() {
		return seciliEkSaha4;
	}

	public void setSeciliEkSaha4(Tanim seciliEkSaha4) {
		this.seciliEkSaha4 = seciliEkSaha4;
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

	public Double getBakiyeSuresi() {
		return bakiyeSuresi;
	}

	public void setBakiyeSuresi(Double bakiyeSuresi) {
		this.bakiyeSuresi = bakiyeSuresi;
	}

	public List<TempIzin> getpdksPersonelList() {
		return pdksPersonelList;
	}

	public void setpdksPersonelList(List<TempIzin> pdksPersonelList) {
		this.pdksPersonelList = pdksPersonelList;
	}

	public TempIzin getUpdateTempIzin() {
		return updateTempIzin;
	}

	public void setUpdateTempIzin(TempIzin updateTempIzin) {
		this.updateTempIzin = updateTempIzin;
	}

	public PersonelIzin getUpdateIzin() {
		return updateIzin;
	}

	public void setUpdateIzin(PersonelIzin updateIzin) {
		this.updateIzin = updateIzin;
	}

	public List<PersonelIzinDetay> getHarcananIzinler() {
		return harcananIzinler;
	}

	public void setHarcananIzinler(List<PersonelIzinDetay> harcananIzinler) {
		this.harcananIzinler = harcananIzinler;
	}

	public Tanim getSeciliTesis() {
		return seciliTesis;
	}

	public void setSeciliTesis(Tanim seciliTesis) {
		this.seciliTesis = seciliTesis;
	}

}
