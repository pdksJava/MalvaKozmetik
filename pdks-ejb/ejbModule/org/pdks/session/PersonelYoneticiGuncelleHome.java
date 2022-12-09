package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelIzinOnay;
import org.pdks.entity.Sirket;
import org.pdks.security.entity.User;

@Name("personelYoneticiGuncelleHome")
public class PersonelYoneticiGuncelleHome extends EntityHome<Personel> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1113157704233696499L;
	static Logger logger = Logger.getLogger(PersonelYoneticiGuncelleHome.class);

	@RequestParameter
	Long pdksPersonelId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = true, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false)
	FacesMessages facesMessages;
	private List<Personel> yoneticiList, iptalYoneticiList, personelList;
	private Personel yonetici, iptalYonetici;
	private List<Sirket> sirketList;
	private Sirket sirket;
	private boolean seciliDurum;
	private String sanalPersonelAciklama;
	private Session session;

	@Override
	public Object getId() {
		if (pdksPersonelId == null) {
			return super.getId();
		} else {
			return pdksPersonelId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	@Transactional
	public String save() {
		if (yonetici != null) {
			if (!iptalYonetici.getId().equals(yonetici.getId())) {
				Date guncellemeTarihi = null;
				List<Personel> personeller = new ArrayList<Personel>();
				for (Iterator iterator = personelList.iterator(); iterator.hasNext();) {
					Personel personel = (Personel) iterator.next();
					if (personel.isCheckBoxDurum()) {
						personeller.add(personel);
						if (guncellemeTarihi == null)
							guncellemeTarihi = new Date();
						personel.setYoneticisi(yonetici);
						personel.setGuncellemeTarihi(guncellemeTarihi);
						personel.setGuncelleyenUser(authenticatedUser);
						try {
							pdksEntityController.saveOrUpdate(session, entityManager, personel);
							iterator.remove();
						} catch (Exception e) {
							logger.error("PDKS hata in : \n");
							e.printStackTrace();
							logger.error("PDKS hata out : " + e.getMessage());
							PdksUtil.addMessageWarn("Hata : " + e.getMessage());
						}
					}
				}
				if (personeller.isEmpty()) {
					if (guncellemeTarihi == null)
						PdksUtil.addMessageWarn("Seçili kayıt yoktur!");

				} else {
					HashMap fields = new HashMap();
					fields.put("guncelleyenUser.pdksPersonel", iptalYonetici);
					fields.put("onayDurum", PersonelIzinOnay.ONAY_DURUM_ISLEM_YAPILMADI);
					fields.put("personelIzin.izinSahibi", personeller);
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					try {
						List<PersonelIzinOnay> onaylanmisIzinler = pdksEntityController.getObjectByInnerObjectList(fields, PersonelIzinOnay.class);

						if (!onaylanmisIzinler.isEmpty()) {
							fields.clear();
							fields.put("pdksPersonel.id", yonetici.getId());
							if (session != null)
								fields.put(PdksEntityController.MAP_KEY_SESSION, session);
							User guncelleyenUser = (User) pdksEntityController.getObjectByInnerObject(fields, User.class);
							for (PersonelIzinOnay personelIzinOnay : onaylanmisIzinler) {
								personelIzinOnay.setGuncelleyenUser(guncelleyenUser);
								pdksEntityController.saveOrUpdate(session, entityManager, personelIzinOnay);
							}
						}

						session.flush();
						PdksUtil.addMessageWarn("Yeni yöneticiler güncellendi.");
						sirketDegisti();
					} catch (Exception e) {
						logger.error("PDKS hata in : \n");
						e.printStackTrace();
						logger.error("PDKS hata out : " + e.getMessage());
						PdksUtil.addMessageWarn("Hata : " + e.getMessage());
					}

				}
				personeller = null;
			} else
				PdksUtil.addMessageWarn("Farklı yönetici seçiniz!");
		} else
			PdksUtil.addMessageWarn("Yeni yönetici seçiniz!");
		return "";

	}

	public String yoneticiDegisti() {
		Date tarih = PdksUtil.getDate(Calendar.getInstance().getTime());
		HashMap fields = new HashMap();
		fields.put("sirket.id=", sirket.getId());
		fields.put("yoneticisi.id=", iptalYonetici.getId());
		fields.put("durum=", Boolean.TRUE);
		fields.put("iseBaslamaTarihi<=", tarih);
		fields.put("sskCikisTarihi>=", tarih);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		personelList = pdksEntityController.getObjectByInnerObjectListInLogic(fields, Personel.class);
		if (personelList.size() > 1)
			personelList = PdksUtil.sortObjectStringAlanList(personelList, "getAdSoyad", null);
		for (Iterator iterator = personelList.iterator(); iterator.hasNext();) {
			Personel personel = (Personel) iterator.next();
			personel.setCheckBoxDurum(seciliDurum);

		}
		return "";
	}

	public String sirketDegisti() {

		yoneticiList.clear();
		iptalYoneticiList.clear();
		personelList.clear();
		yonetici = null;
		seciliDurum = Boolean.FALSE;
		iptalYonetici = null;
		if (sirket != null) {
			HashMap fields = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT DISTINCT Y.* FROM " + Personel.TABLE_NAME + " P WITH(nolock) ");
			sb.append("INNER JOIN " + Personel.TABLE_NAME + " Y ON Y." + Personel.COLUMN_NAME_ID + "=P." + Personel.COLUMN_NAME_YONETICI);
			sb.append(" WHERE P." + Personel.COLUMN_NAME_SIRKET + "=:s AND P." + Personel.COLUMN_NAME_DURUM + "=1 ");
			sb.append(" AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=CAST(GETDATE() AS date)");
			sb.append(" ORDER BY Y." + Personel.COLUMN_NAME_AD + ",Y." + Personel.COLUMN_NAME_SOYAD + ",Y." + Personel.COLUMN_NAME_ID);
			fields.put("s", sirket.getId());
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			iptalYoneticiList = pdksEntityController.getObjectBySQLList(sb, fields, Personel.class);
			if (!iptalYoneticiList.isEmpty()) {
				yoneticiList = ortakIslemler.getTaseronYoneticiler(session);
				if (!yoneticiList.isEmpty()) {
					iptalYonetici = iptalYoneticiList.get(0);
					yonetici = yoneticiList.get(0);
					seciliDurum = Boolean.TRUE;
					yoneticiDegisti();
				}
			}
		}

		return "";
	}

	public void fillSirketList() {
		HashMap fields = new HashMap();
		fields.put("durum", Boolean.TRUE);
		fields.put("pdks", Boolean.TRUE);
		if (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin())
			fields.put("departman.admin", Boolean.FALSE);
		else
			fields.put("departman.id", authenticatedUser.getDepartman().getId());

		fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Sirket> list = pdksEntityController.getObjectByInnerObjectList(fields, Sirket.class);
		fields = null;
		if (list.size() > 1)
			list = PdksUtil.sortObjectStringAlanList(list, "getAd", null);
		setSirketList(list);

	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		setInstance(new Personel());
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		sanalPersonelAciklama = ortakIslemler.sanalPersonelAciklama();
		fillSirketList();

		if (yoneticiList == null)
			yoneticiList = new ArrayList<Personel>();
		else
			yoneticiList.clear();
		if (iptalYoneticiList == null)
			iptalYoneticiList = new ArrayList<Personel>();
		else
			iptalYoneticiList.clear();
		if (personelList == null)
			personelList = new ArrayList<Personel>();
		else
			personelList.clear();
		sirket = null;
		yonetici = null;
		iptalYonetici = null;

	}

	public List<Personel> getYoneticiList() {
		return yoneticiList;
	}

	public void setYoneticiList(List<Personel> yoneticiList) {
		this.yoneticiList = yoneticiList;
	}

	public List<Personel> getIptalYoneticiList() {
		return iptalYoneticiList;
	}

	public void setIptalYoneticiList(List<Personel> iptalYoneticiList) {
		this.iptalYoneticiList = iptalYoneticiList;
	}

	public Personel getYonetici() {
		return yonetici;
	}

	public void setYonetici(Personel yonetici) {
		this.yonetici = yonetici;
	}

	public Personel getIptalYonetici() {
		return iptalYonetici;
	}

	public void setIptalYonetici(Personel iptalYonetici) {
		this.iptalYonetici = iptalYonetici;
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

	public List<Personel> getPersonelList() {
		return personelList;
	}

	public void setPersonelList(List<Personel> personelList) {
		this.personelList = personelList;
	}

	public boolean isSeciliDurum() {
		return seciliDurum;
	}

	public void setSeciliDurum(boolean seciliDurum) {
		this.seciliDurum = seciliDurum;
	}

	public String getSanalPersonelAciklama() {
		return sanalPersonelAciklama;
	}

	public void setSanalPersonelAciklama(String sanalPersonelAciklama) {
		this.sanalPersonelAciklama = sanalPersonelAciklama;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

}
