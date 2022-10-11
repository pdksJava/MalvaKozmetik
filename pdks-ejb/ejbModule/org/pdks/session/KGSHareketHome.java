package org.pdks.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.Departman;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.KapiView;
import org.pdks.entity.PdksPersonelView;
import org.pdks.entity.PersonelHareketIslem;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.security.entity.User;

@Name("kgsHareketHome")
public class KGSHareketHome extends EntityHome<HareketKGS> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4822510981494581092L;
	static Logger logger = Logger.getLogger(KGSHareketHome.class);

	@RequestParameter
	Long kgsHareketId;

	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false)
	User authenticatedUser;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	List<User> userList;
	@In(required = false)
	FacesMessages facesMessages;

	private List<HareketKGS> hareketList = new ArrayList<HareketKGS>();
	private String ad = "", soyad = "", sicilNo = "", islemTipi;
	private Date tarih, basTarih, bitTarih;
	private String onayDurum;
	private Tanim seciliTesis, seciliEkSaha1, seciliEkSaha2, seciliEkSaha3, seciliEkSaha4;
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private Sirket sirket;
	private List<Sirket> sirketList = new ArrayList<Sirket>();
	private boolean iptalEdilir;
	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	long kgsId = 0, pdksId = 0;

	@Override
	public Object getId() {
		if (kgsHareketId == null) {
			return super.getId();
		} else {
			return kgsHareketId;
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
		setHareketList(new ArrayList<HareketKGS>());
		HareketKGS hareket = new HareketKGS();
		hareket.setPersonel(new PersonelView());
		hareket.setKapiView(new KapiView());
		hareket.setIslem(new PersonelHareketIslem());
		setInstance(hareket);
		setTarih(new Date());
		setBasTarih(tarih);
		setBitTarih(tarih);
		setIptalEdilir(Boolean.FALSE);
		setInstance(new HareketKGS());
		fillEkSahaTanim();
		if (authenticatedUser.isIK() || authenticatedUser.isAdmin()) {
			fillSirketList();

		} else
			setSirket(authenticatedUser.getPdksPersonel().getSirket());
	}

	public void fillSirketList() {
		HashMap map = new HashMap();
		map.put("durum", Boolean.TRUE);
		map.put("pdks", Boolean.TRUE);
		if (!authenticatedUser.isAdmin())
			map.put("departman", authenticatedUser.getDepartman());

		map.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Sirket> list = pdksEntityController.getObjectByInnerObjectList(map, Sirket.class);
		ortakIslemler.digerIKSirketBul(list, Boolean.FALSE, session);
		if (list.size() > 1)
			list = PdksUtil.sortObjectStringAlanList(list, "getAd", null);
		setSirketList(list);
	}

	public void fillHareketList() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.clear();

		setIptalEdilir(true);

		List<HareketKGS> hareket1List = new ArrayList<HareketKGS>();
		if (sicilNo.trim().equals("") && sirket == null) {
			PdksUtil.addMessageWarn("" + ortakIslemler.sirketAciklama() + " seçiniz!");
			fillSirketList();
		} else {
			ArrayList<String> sicilNoList = ortakIslemler.getPersonelSicilNo(ad, soyad, sicilNo, sirket, seciliTesis, seciliEkSaha1, seciliEkSaha2, seciliEkSaha3, seciliEkSaha4, Boolean.FALSE, session);
			if (sicilNoList != null && !sicilNoList.isEmpty()) {
				HashMap<Long, Departman> departmanMap = new HashMap<Long, Departman>();
				HashMap parametreMap = new HashMap();
				parametreMap.put("pdksSicilNo", sicilNoList);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<Long> personelIdler = ortakIslemler.getPersonelViewIdList(pdksEntityController.getObjectByInnerObjectList(parametreMap, PdksPersonelView.class));
				List<HareketKGS> list = new ArrayList<HareketKGS>();
				List<Long> kapiIdler = ortakIslemler.getPdksKapiIdler(session, Boolean.TRUE);
				try {
					if (kapiIdler != null && !kapiIdler.isEmpty())
						list = ortakIslemler.getHareketBilgileri(kapiIdler, null, PdksUtil.getDate(basTarih), PdksUtil.getDate(PdksUtil.tariheGunEkleCikar(bitTarih, 1)), HareketKGS.class, session);
					if (personelIdler.isEmpty())
						list.clear();
					for (Iterator iterator = list.iterator(); iterator.hasNext();) {
						HareketKGS kgsHareket = (HareketKGS) iterator.next();
						if (!personelIdler.contains(kgsHareket.getPersonelId())) {
							iterator.remove();
							continue;
						}
						PersonelHareketIslem hareketIslem = kgsHareket.getIslem();
						if (hareketIslem != null && kgsHareket.getIslem().getIslemTipi().equals("U") && (kgsHareket.getSirket().equals(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_KGS)))
							continue;
						// kgsHareket.setVardiyaGun(vardiyaGun);
						if ((authenticatedUser.isAdmin() || authenticatedUser.isIK()) && !departmanMap.containsKey(kgsHareket.getPersonel().getPdksPersonel().getSirket().getDepartman().getId()) && hareketIslem.getOnaylayanUser() != null
								&& hareketIslem.getOnayDurum() == PersonelHareketIslem.ONAY_DURUM_ONAYLANDI) {
							if (!isIptalEdilir())
								setIptalEdilir(Boolean.TRUE);
							kgsHareket.setVardiyaGun(null);

						}
						hareket1List.add(kgsHareket);
					}
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());

				}
				if (!hareket1List.isEmpty())
					hareket1List = PdksUtil.sortListByAlanAdi(hareket1List, "zaman", Boolean.FALSE);
				setHareketList(hareket1List);
			}
		}

	}

	public void iptalIslemi() {
		if (onayDurum.equals("1")) {

			if (this.getInstance().getSirket().equals(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_KGS))
				kgsId = this.getInstance().getHareketTableId();
			else
				pdksId = this.getInstance().getHareketTableId();
			pdksEntityController.hareketOnaylama(kgsId, pdksId, authenticatedUser, session);

			fillHareketList();

		}
	}

	public String topluOnaylama() {
		boolean islem = Boolean.FALSE, secili = Boolean.FALSE;
		for (HareketKGS kgsHareket : hareketList) {
			if (kgsHareket.isCheckBoxDurum()) {
				try {
					secili = Boolean.TRUE;
					onaylamaIslemi(kgsHareket, Boolean.TRUE);
					islem = Boolean.TRUE;
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					secili = Boolean.FALSE;
					islem = Boolean.FALSE;
					PdksUtil.addMessageError("Onaylama başarılı tamamlanmamıştır!");
					break;
				}
				kgsHareket.setCheckBoxDurum(Boolean.FALSE);
			}
		}

		if (islem) {
			fillHareketList();
			PdksUtil.addMessageWarn("Onaylama başarılı tamamlanmıştır!");

		} else if (!secili)
			PdksUtil.addMessageWarn("Onaylanacak hareket seçiniz!");

		return "";
	}

	public void onaylamaIslemi(HareketKGS kgsHareket, Boolean yenile) {
		setInstance(kgsHareket);
		if (yenile)
			this.onayla();
		else
			this.onaylama();
		if (!yenile)
			fillHareketList();
	}

	public void onayla() {
		HareketKGS kgsHareket = this.getInstance();
		pdksEntityController.hareketOnayla(kgsHareket.getIslem().getId(), authenticatedUser, session);
		session.flush();

	}

	public void onaylama() {
		HareketKGS kgsHareket = this.getInstance();
		if (kgsHareket.getSirket().equals(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_PDKS)) {
			kgsId = 0;
			pdksId = kgsHareket.getHareketTableId();
		} else {
			kgsId = kgsHareket.getHareketTableId();
			pdksId = 0;
		}
		pdksEntityController.hareketOnaylama(kgsId, pdksId, authenticatedUser, session);
		session.flush();
		fillHareketList();
	}

	public Date getTarih() {
		return tarih;
	}

	public void setTarih(Date tarih) {
		this.tarih = tarih;
	}

	public List<HareketKGS> getHareketList() {
		return hareketList;
	}

	public void setHareketList(List<HareketKGS> hareketList) {
		this.hareketList = hareketList;
	}

	public String getIslemTipi() {
		return islemTipi;
	}

	public void setIslemTipi(String islemTipi) {
		this.islemTipi = islemTipi;
	}

	public String getOnayDurum() {
		return onayDurum;
	}

	public void setOnayDurum(String onayDurum) {
		this.onayDurum = onayDurum;
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

	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket sirket) {
		this.sirket = sirket;
	}

	public List<Sirket> getSirketList() {
		return sirketList;
	}

	public void setSirketList(List<Sirket> sirketList) {
		this.sirketList = sirketList;
	}

	public boolean isIptalEdilir() {
		return iptalEdilir;
	}

	public void setIptalEdilir(boolean iptalEdilir) {
		this.iptalEdilir = iptalEdilir;
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

	public Tanim getSeciliTesis() {
		return seciliTesis;
	}

	public void setSeciliTesis(Tanim seciliTesis) {
		this.seciliTesis = seciliTesis;
	}

}
