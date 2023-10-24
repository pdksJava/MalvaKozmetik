package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.pdks.entity.Personel;
import org.pdks.entity.IseGelmeyenDisplay;
import org.pdks.entity.PersonelHareket;
import org.pdks.entity.PersonelIzin;
import org.pdks.security.entity.User;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;

@Name("iseGelmeyenPersonelHome")
public class IseGelmeyenPersonelHome extends EntityHome<PersonelIzin> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1847161524773277729L;

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
	Date date;
	List<IseGelmeyenDisplay> iseGelmeyenList = new ArrayList<IseGelmeyenDisplay>();
	List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
	List<PersonelHareket> hareketList = new ArrayList<PersonelHareket>();
	List<Personel> personelList = new ArrayList<Personel>();
	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	@In(required = false)
	FacesMessages facesMessages;

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

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		// default bugun icin ise gelmeyen raporu cekili olsun
		setDate(PdksUtil.buGun());
		gelmeyenListOlustur();
	}

	public String gelmeyenListOlustur() {
		if (iseGelmeyenList != null)
			iseGelmeyenList.clear();
		List<IseGelmeyenDisplay> list = iseGelmeyenListeOlustur(getDate(), getDate());
		setIseGelmeyenList(list);
		return "";
	}

	/**
	 * @param dateBas
	 * @param dateBit
	 * @return
	 */
	private List<IseGelmeyenDisplay> iseGelmeyenListeOlustur(Date dateBas, Date dateBit) {
		HashMap paramMap = new HashMap();
		// paramMap.put(pdksEntityController.MAP_KEY_MAP, "getIzinUnique" );
		paramMap.put("izinTipi.bakiyeIzinTipi=", null); // nullolmayanlar bakiye izinleri
		Calendar cal = Calendar.getInstance();
		dateBas = PdksUtil.getDate(dateBas);

		cal.setTime(dateBit);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		dateBit = cal.getTime();
		paramMap.put("bitisZamani >=", dateBas);
		paramMap.put("baslangicZamani <=", dateBit);
		paramMap.put("izinSahibi", authenticatedUser.getTumPersoneller().clone());
		if (session != null)
			paramMap.put(PdksEntityController.MAP_KEY_SESSION, session);

		List<PersonelIzin> izinList = pdksEntityController.getObjectByInnerObjectListInLogic(paramMap, PersonelIzin.class);

		// setIzinList(izinList);

		paramMap.clear();
		paramMap.put("zaman >=", dateBas);
		paramMap.put("zaman <=", dateBit);
		if (session != null)
			paramMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PersonelHareket> hareketList = pdksEntityController.getObjectByInnerObjectListInLogic(paramMap, PersonelHareket.class);
		String key = "";
		Map hareketMap = new HashMap();
		for (Iterator iterator = hareketList.iterator(); iterator.hasNext();) {
			PersonelHareket personelHareket = (PersonelHareket) iterator.next();
			key = personelHareket.getPersonel().getId().toString();
			hareketMap.put(key, personelHareket);
		}

		for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
			PersonelIzin personelIzin = (PersonelIzin) iterator.next();
			if (hareketMap.containsKey(personelIzin.getIzinSahibi().getId().toString()))
				izinList.remove(personelIzin);
		}

		List<IseGelmeyenDisplay> displayList = izinListGrupla(izinList, dateBas);

		return displayList;
	}

	/**
	 * @param izinList
	 * @param raporGunu
	 * @return
	 */
	private List<IseGelmeyenDisplay> izinListGrupla(List<PersonelIzin> izinList, Date raporGunu) {
		String key = "";
		Map<String, IseGelmeyenDisplay> grupMap = new HashMap<String, IseGelmeyenDisplay>();
		int grupTotal = 0;
		IseGelmeyenDisplay tempDisplay = null;
		List<Personel> personelList = new ArrayList<Personel>();
		for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
			PersonelIzin personelIzin = (PersonelIzin) iterator.next();
			key = personelIzin.getIzinTipi().getIzinTipiTanim().getId().toString();
			if (grupMap.containsKey(key)) {
				tempDisplay = grupMap.get(key);
				grupTotal = tempDisplay.getPersonelSayisi();
				personelList = tempDisplay.getPersonelList();
				personelList.add(personelIzin.getIzinSahibi());
			} else {
				tempDisplay = new IseGelmeyenDisplay();
				tempDisplay.setIzinTipi(personelIzin.getIzinTipi());
				tempDisplay.setPersonelSayisi(0);
				tempDisplay.setDate(raporGunu);
				personelList = new ArrayList<Personel>();
				personelList.add(personelIzin.getIzinSahibi());
				tempDisplay.setPersonelList(personelList);
				grupTotal = 0;
			}

			grupTotal++;
			tempDisplay.setPersonelSayisi(grupTotal);
			grupMap.put(key, tempDisplay);
		}
		List<IseGelmeyenDisplay> list = new ArrayList(grupMap.values());
		grupMap = null;
		return list;
	}

	public void iseGelmeyenListeOlustur() {

	}

	public void personelGoster(IseGelmeyenDisplay display) {
		setPersonelList(display.getPersonelList());
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<IseGelmeyenDisplay> getIseGelmeyenList() {
		return iseGelmeyenList;
	}

	public void setIseGelmeyenList(List<IseGelmeyenDisplay> iseGelmeyenList) {
		this.iseGelmeyenList = iseGelmeyenList;
	}

	public List<PersonelIzin> getIzinList() {
		return izinList;
	}

	public void setIzinList(List<PersonelIzin> izinList) {
		this.izinList = izinList;
	}

	public List<PersonelHareket> getHareketList() {
		return hareketList;
	}

	public void setHareketList(List<PersonelHareket> hareketList) {
		this.hareketList = hareketList;
	}

	public List<Personel> getPersonelList() {
		return personelList;
	}

	public void setPersonelList(List<Personel> personelList) {
		this.personelList = personelList;
	}

}
