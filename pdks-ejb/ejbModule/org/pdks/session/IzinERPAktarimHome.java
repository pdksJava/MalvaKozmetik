package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.Sirket;
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

import org.pdks.erp.action.ERPController;
import org.pdks.security.entity.User;

@Name("izinERPAktarimHome")
public class IzinERPAktarimHome extends EntityHome<PersonelIzin> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8632935723642050940L;
	static Logger logger = Logger.getLogger(IzinERPAktarimHome.class);

	@RequestParameter
	Long personelIzinId;

	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	@In(required = false, create = true)
	EntityManager entityManager;

	@In(required = false)
	User authenticatedUser;

	@In(required = false)
	FacesMessages facesMessages;
	private Date basDate, bitDate;

	String kullaniciIslemleriMailAciklama = "PDKS Sistemi";
	String kullaniciIslemleriMailAdres = "";

	List<PersonelIzin> personelizinList = new ArrayList<PersonelIzin>();
	List<Personel> pdksPersonelList = new ArrayList<Personel>();
	private AramaSecenekleri aramaSecenekleri = null;

	private boolean istenAyrilanEkle = Boolean.FALSE, checkBoxDurum = Boolean.FALSE;
	private List<User> userList = new ArrayList<User>();

	private Session session;

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

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		if (authenticatedUser.isAdmin() == false || aramaSecenekleri == null)
			aramaSecenekleri = new AramaSecenekleri(authenticatedUser);
		session.clear();
		fillEkSahaTanim();

		setBasDate(PdksUtil.ayinIlkGunu());
		setBitDate(PdksUtil.ayinSonGunu());
		setPersonelizinList(new ArrayList());
	}

	private void fillEkSahaTanim() {
		ortakIslemler.fillEkSahaTanimAramaSecenekAta(session, Boolean.FALSE, null, aramaSecenekleri);
		List<SelectItem> sirketIdList = aramaSecenekleri.getSirketIdList();
		if (sirketIdList != null) {
			List<Long> idler = new ArrayList<Long>();
			for (SelectItem selectItem : sirketIdList)
				idler.add((Long) selectItem.getValue());
			HashMap fields = new HashMap();
			fields.put(PdksEntityController.MAP_KEY_MAP, "getId");
			fields.put("fazlaMesai", Boolean.TRUE);
			fields.put("sap", Boolean.TRUE);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			TreeMap<Long, Sirket> sirketMap = pdksEntityController.getObjectByInnerObjectMap(fields, Sirket.class, false);
			idler = null;
			for (Iterator iterator = sirketIdList.iterator(); iterator.hasNext();) {
				SelectItem selectItem = (SelectItem) iterator.next();
				if (!sirketMap.containsKey(selectItem.getValue()))
					iterator.remove();
			}
			if (sirketIdList.size() == 1)
				aramaSecenekleri.setSirketId((Long) sirketIdList.get(0).getValue());
			sirketMap = null;
		}

	}

	@Transactional
	public String topluSAPDurumKapat() {
		boolean flush = Boolean.FALSE;
		Date guncellemeTarihi = new Date();
		for (Iterator iterator = personelizinList.iterator(); iterator.hasNext();) {
			PersonelIzin personelIzin = (PersonelIzin) iterator.next();
			if (personelIzin.isCheckBoxDurum()) {
				personelIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_SAP_GONDERILDI);
				personelIzin.setGuncelleyenUser(authenticatedUser);
				personelIzin.setGuncellemeTarihi(guncellemeTarihi);
				pdksEntityController.saveOrUpdate(session, entityManager, personelIzin);
				flush = Boolean.TRUE;
				iterator.remove();
			}

		}
		if (flush) {
			session.flush();
		} else
			PdksUtil.addMessageWarn("İzin seçiniz!");

		return "";
	}

	public void fillIzinList() {
		List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
		session.clear();
		ArrayList<String> sicilNoList = ortakIslemler.getAramaPersonelSicilNo(aramaSecenekleri, Boolean.TRUE, istenAyrilanEkle, session);
		checkBoxDurum = Boolean.FALSE;
		if (!sicilNoList.isEmpty()) {
			HashMap parametreMap = new HashMap();
			parametreMap.put(PdksEntityController.MAP_KEY_MAP, "getId");
			parametreMap.put("pdksSicilNo", sicilNoList);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				TreeMap<Long, Personel> map = pdksEntityController.getObjectByInnerObjectMap(parametreMap, Personel.class, false);
				if (!map.isEmpty()) {
					List<Long> list = new ArrayList<Long>(map.keySet());
					parametreMap.clear();
					parametreMap.put("baslangicZamani<=", getBitDate());
					parametreMap.put("bitisZamani>=", getBasDate());
					if (list.size() == 1)
						parametreMap.put("izinSahibi.id=", list.get(0));
					else
						parametreMap.put("izinSahibi.id", list);
					parametreMap.put("izinDurumu=", PersonelIzin.IZIN_DURUMU_ONAYLANDI);
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					izinList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, PersonelIzin.class);
					for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
						PersonelIzin personelIzin = (PersonelIzin) iterator.next();
						IzinTipi izinTipi = personelIzin.getIzinTipi();
						if (izinTipi.getBakiyeIzinTipi() != null || !izinTipi.getErpAktarim())
							iterator.remove();
						else
							personelIzin.setCheckBoxDurum(checkBoxDurum);
						personelIzin.setMesaj("");
					}
					if (izinList.size() == 1)
						izinList.get(0).setCheckBoxDurum(Boolean.TRUE);
					list = null;
				}
				map = null;
			} catch (Exception e) {
				logger.error(e);
			}
		}
		sicilNoList = null;
		setPersonelizinList(izinList);

	}

	/**
	 * @param izin
	 * @param guncellemeTarihi
	 * @return
	 */
	private boolean izinSapAktar(PersonelIzin izin, Date guncellemeTarihi) {
		ERPController controller = ortakIslemler.getERPController();
		boolean yazildi = false, mesajDurum = guncellemeTarihi == null;
		String mesaj = "";
		try {
			mesaj = controller.setIzinRFC(izin);
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			mesaj = e.getMessage();
		}
		izin.setMesaj("");
		if (mesaj.equalsIgnoreCase("")) {
 			yazildi = true;
			izin.setCheckBoxDurum(Boolean.FALSE);
			izin.setGuncelleyenUser(authenticatedUser);
			izin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_SAP_GONDERILDI);
			if (guncellemeTarihi == null)
				guncellemeTarihi = new Date();
			izin.setGuncellemeTarihi(guncellemeTarihi);
			pdksEntityController.saveOrUpdate(session, entityManager, izin);
		} else if (mesajDurum)
			PdksUtil.addMessageWarn(mesaj);

		return yazildi;
	}

	@Transactional
	public String topluSAPGonder() {
		List<PersonelIzin> list = new ArrayList<PersonelIzin>();
		for (PersonelIzin personelIzin : personelizinList) {
			personelIzin.setMesaj(null);
			if (personelIzin.isCheckBoxDurum()) {
				list.add(personelIzin);
				if (list.size() == 250) {
					break;
				}
			}
		}
		if (!list.isEmpty()) {
			ERPController controller = ortakIslemler.getERPController();
			TreeMap<Long, String> sonucMap = null;
			try {
				sonucMap = controller.setRFCIzinList(list);
			} catch (Exception e) {

			}
			if (sonucMap != null) {
				list.clear();
				int adet = 0;
				for (Iterator iterator = personelizinList.iterator(); iterator.hasNext();) {
					PersonelIzin izin = (PersonelIzin) iterator.next();
					if (sonucMap.containsKey(izin.getId())) {
						String mesaj = izin.getMesaj();
						if (mesaj != null && mesaj.equals("")) {
							izin.setGuncelleyenUser(authenticatedUser);
							izin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_SAP_GONDERILDI);
							pdksEntityController.saveOrUpdate(session, entityManager, izin);
							list.add(izin);
							iterator.remove();
						} else
							++adet;
					}

				}
				if (!list.isEmpty()) {
					PdksUtil .addMessageInfo(list.size() + " adet izin aktarıldı.");
					session.flush();
				}
				if (adet > 0)
					PdksUtil.addMessageAvailableWarn(adet + " adet izin hatalıdır!");
			}

		} else
			PdksUtil.addMessageWarn("İzin seçiniz!");
		list = null;
		return "";
	}

	public String izinSec() {
		for (PersonelIzin personelIzin : personelizinList) {
			personelIzin.setCheckBoxDurum(checkBoxDurum);
		}
		return "";

	}

	/**
	 * @param izin
	 * @return
	 */
	public String sapAktar(PersonelIzin izin) {
		if (izinSapAktar(izin, null)) {
			izin.setGuncelleyenUser(authenticatedUser);
			izin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_SAP_GONDERILDI);
			pdksEntityController.saveOrUpdate(session, entityManager, izin);
			session.flush();
			PdksUtil.addMessageInfo("İzin başarı ile SAP sistemine aktarılmıştır.");
			fillIzinList();
		}

		return "";
	}

	public List<PersonelIzin> getPersonelizinList() {
		return personelizinList;
	}

	public void setPersonelizinList(List<PersonelIzin> personelizinList) {
		this.personelizinList = personelizinList;
	}

	public List<Personel> getpdksPersonelList() {
		return pdksPersonelList;
	}

	public void setpdksPersonelList(List<Personel> pdksPersonelList) {
		this.pdksPersonelList = pdksPersonelList;
	}

	public String getKullaniciIslemleriMailAciklama() {
		return kullaniciIslemleriMailAciklama;
	}

	public void setKullaniciIslemleriMailAciklama(String kullaniciIslemleriMailAciklama) {
		this.kullaniciIslemleriMailAciklama = kullaniciIslemleriMailAciklama;
	}

	public String getKullaniciIslemleriMailAdres() {
		return kullaniciIslemleriMailAdres;
	}

	public void setKullaniciIslemleriMailAdres(String kullaniciIslemleriMailAdres) {
		this.kullaniciIslemleriMailAdres = kullaniciIslemleriMailAdres;
	}

	public List<User> getUserList() {
		return userList;
	}

	public void setUserList(List<User> userList) {
		this.userList = userList;
	}

	public Date getBasDate() {
		return basDate;
	}

	public void setBasDate(Date basDate) {
		this.basDate = basDate;
	}

	public Date getBitDate() {
		return bitDate;
	}

	public void setBitDate(Date bitDate) {
		this.bitDate = bitDate;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public AramaSecenekleri getAramaSecenekleri() {
		return aramaSecenekleri;
	}

	public void setAramaSecenekleri(AramaSecenekleri aramaSecenekleri) {
		this.aramaSecenekleri = aramaSecenekleri;
	}

	public boolean isIstenAyrilanEkle() {
		return istenAyrilanEkle;
	}

	public void setIstenAyrilanEkle(boolean istenAyrilanEkle) {
		this.istenAyrilanEkle = istenAyrilanEkle;
	}

	public boolean isCheckBoxDurum() {
		return checkBoxDurum;
	}

	public void setCheckBoxDurum(boolean checkBoxDurum) {
		this.checkBoxDurum = checkBoxDurum;
	}

}
