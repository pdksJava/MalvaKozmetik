package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.security.entity.User;
import org.pdks.security.entity.UserRoles;

@Name("personelKullaniciAramaHome")
public class PersonelKullaniciAramaHome extends EntityHome<User> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5191938192108795274L;
	static Logger logger = Logger.getLogger(PersonelKullaniciAramaHome.class);

	@RequestParameter
	Long userId;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	String linkAdres;

	private Tanim seciliDepartman;

	private Sirket seciliSirket;

	private List<Tanim> departmanList = new ArrayList<Tanim>();

	private List<SelectItem> sirketItemList = new ArrayList<SelectItem>();
	private List<Sirket> sirketList = new ArrayList<Sirket>();
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;

	@Out(value = "seciliPersonelList", scope = ScopeType.PAGE, required = false)
	private List<Personel> seciliPersonelList = new ArrayList<Personel>();

	@Out(value = "seciliUserList", scope = ScopeType.PAGE, required = false)
	private List<User> seciliUserList = new ArrayList<User>();

	@Out(value = "seciliUser", scope = ScopeType.PAGE, required = false)
	private User seciliUser;

	@Out(value = "seciliPersonel", scope = ScopeType.PAGE, required = false)
	private Personel seciliPersonel;

	private List<User> userList = new ArrayList<User>();
	private List<Personel> personelList = new ArrayList<Personel>();

	boolean sapDepartman;

	boolean pdksDepartman;

	private boolean userArama = Boolean.FALSE;

	private boolean visibled = Boolean.FALSE;

	private boolean personelArama = Boolean.TRUE;

	private boolean checkBox;

	private boolean checkBoxDurum;

	private String reRender;

	private Sirket pdksSirket;

	private List<String> roleList;

	private Session session;

	private AramaSecenekleri aramaSecenekleri = null;

	public void fillEkSahaTanim() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		// HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		if (aramaSecenekleri == null)
			aramaSecenekleri = new AramaSecenekleri(authenticatedUser);
		HashMap sonucMap = ortakIslemler.fillEkSahaTanimAramaSecenekAta(session, Boolean.FALSE, null, aramaSecenekleri);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
		setSirketList((List<Sirket>) sonucMap.get("pdksSirketList"));
		setSirketItemList((List<SelectItem>) sonucMap.get("sirketList"));

	}

	public String getAramaTipi() {
		String aciklama = "";
		if (personelArama)
			aciklama = "Personel Arama";
		else if (userArama)
			aciklama = "Kullanıcı Arama";
		return aciklama;
	}

	public void panelDurumDegistir() {
		this.setVisibled(!this.isVisibled());
	}

	public void secimDurumDegistir() {

		if (personelArama) {
			for (Iterator iterator = personelList.iterator(); iterator.hasNext();) {
				Personel pdksPersonel = (Personel) iterator.next();
				pdksPersonel.setCheckBoxDurum(checkBoxDurum);
			}

		} else if (userArama) {
			for (Iterator iterator = userList.iterator(); iterator.hasNext();) {
				User user = (User) iterator.next();
				user.getPdksPersonel().setCheckBoxDurum(checkBoxDurum);

			}
		}
	}

	public void checkBoxSecimDevam(List list) {
		List secimList = new ArrayList();
		if (personelArama) {
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Personel pdksPersonel = (Personel) iterator.next();
				if (pdksPersonel.isCheckBoxDurum())
					secimList.add(pdksPersonel);
			}

		} else if (userArama) {
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				User user = (User) iterator.next();
				if (user.getPdksPersonel().isCheckBoxDurum())
					secimList.add(user);
			}

		}
		if (!secimList.isEmpty()) {
			if (userArama)
				setSeciliUserList(secimList);
			else if (personelArama)
				setSeciliPersonelList(secimList);

			panelDurumDegistir();
		} else {
			if (personelArama)
				PdksUtil.addMessageWarn("Lütfen personel seçimi yapınız!");
			else if (userArama)
				PdksUtil.addMessageWarn("Lütfen kullanıcı seçimi yapınız!");
		}
	}

	public void tekPersonelSecimIslemi(Personel pdksPersonel) {
		panelDurumDegistir();
		setSeciliPersonel(pdksPersonel);
		getInstance().setPdksPersonel(pdksPersonel);
	}

	public void tekKullaniciSecimIslemi(User user) {
		panelDurumDegistir();
		setSeciliUser(user);
		setInstance(user);
	}

	public void personelArama(String entityHomereRender) {
		this.setCheckBox(Boolean.FALSE);
		this.setRoleList(null);
		this.setReRender(entityHomereRender);
		aramaBaslangic(false, Boolean.TRUE);
	}

	public void personellerArama(String entityHomereRender) {
		this.setCheckBox(Boolean.TRUE);
		this.setRoleList(null);
		this.setReRender(entityHomereRender);
		aramaBaslangic(false, Boolean.TRUE);
	}

	public void kullaniciArama(String entityHomereRender, ArrayList<String> roller) {
		this.setCheckBox(Boolean.FALSE);
		this.setRoleList(roller);
		this.setReRender(entityHomereRender);
		aramaBaslangic(true, Boolean.FALSE);
	}

	public void kullanicilarArama(String entityHomereRender, ArrayList<String> roller) {
		this.setCheckBox(Boolean.TRUE);
		this.setRoleList(roller);
		this.setReRender(entityHomereRender);
		aramaBaslangic(true, Boolean.FALSE);
	}

	public void personellerSil() {
		setSeciliPersonelList(new ArrayList<Personel>());
	}

	public void kullanicilarSil() {
		setSeciliUserList(new ArrayList<User>());
	}

	public void personelSil() {
		setSeciliPersonel(null);
	}

	public void kullaniciSil() {
		setSeciliUser(null);
	}

	private void aramaBaslangic(boolean userAramaSecim, boolean personelAramaSecim) {
		fillEkSahaTanim();
		Session session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		this.setPersonelList(new ArrayList<Personel>());
		this.setUserList(new ArrayList<User>());
		this.setPersonelArama(Boolean.FALSE);
		this.setUserArama(Boolean.FALSE);
		this.setSeciliPersonel(new Personel());
		this.setSeciliUser(new User());
		this.setCheckBoxDurum(Boolean.FALSE);
		if (userAramaSecim)
			this.setUserArama(Boolean.TRUE);
		else if (personelAramaSecim)
			this.setPersonelArama(Boolean.TRUE);
		if (userAramaSecim || personelAramaSecim) {
			User user = new User();
			user.setPdksPersonel(new Personel());
			setInstance(user);

			fillDepartmanList(session);
		} else {
			setSirketList(new ArrayList<Sirket>());
			setDepartmanList(new ArrayList<Tanim>());
		}
		panelDurumDegistir();
	}

	public String fillPersonelList(User user) {

		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		List<Personel> list = new ArrayList<Personel>();
		String adi = aramaSecenekleri.getAd();
		String soyadi = aramaSecenekleri.getSoyad();
		String sicilNo = aramaSecenekleri.getSicilNo();
		HashMap parametreMap = new HashMap();

		StringBuffer sb = new StringBuffer();
		sb.append("SELECT P." + Personel.COLUMN_NAME_ID + " from " + Personel.TABLE_NAME + " P WITH(nolock)  ");
		String whereStr = " WHERE ";
		if (adi.trim().length() > 0) {
			sb.append(whereStr + " P." + Personel.COLUMN_NAME_AD + " LIKE :ad");
			whereStr = " AND ";
			parametreMap.put("ad", adi.trim() + "%");
		}
		if (soyadi.trim().length() > 0) {
			sb.append(whereStr + " P." + Personel.COLUMN_NAME_SOYAD + " LIKE :soyad");
			whereStr = " AND ";
			parametreMap.put("soyad", soyadi.trim() + "%");
		}
		if (sicilNo.trim().length() > 0) {
			sb.append(whereStr + " P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " =:sicilNo");
			whereStr = " AND ";
			parametreMap.put("sicilNo", sicilNo.trim());
		}
		Long seciliSirketId = aramaSecenekleri.getSirketId();
		if (authenticatedUser.isYoneticiKontratli()) {
			if (!(authenticatedUser.isIK() || authenticatedUser.isAdmin()))
				seciliSirketId = null;
		}
		if (seciliSirketId != null) {
			sb.append(whereStr + " P." + Personel.COLUMN_NAME_SIRKET + " =:sirketId");
			whereStr = " AND ";
			parametreMap.put("sirketId", seciliSirketId);
		}
 		if (aramaSecenekleri.getTesisId() != null) {
			sb.append(whereStr + " P." + Personel.COLUMN_NAME_TESIS + " =:tesis");
			whereStr = " AND ";
			parametreMap.put("tesis", aramaSecenekleri.getTesisId());
		}
		if (aramaSecenekleri.getEkSaha1Id() != null) {
			sb.append(whereStr + " P." + Personel.COLUMN_NAME_EK_SAHA1 + " =:ekSaha1");
			whereStr = " AND ";
			parametreMap.put("ekSaha1", aramaSecenekleri.getEkSaha1Id());
		}
		if (aramaSecenekleri.getEkSaha2Id() != null) {
			sb.append(whereStr + " P." + Personel.COLUMN_NAME_EK_SAHA2 + " =:ekSaha2");
			whereStr = " AND ";
			parametreMap.put("ekSaha2", aramaSecenekleri.getEkSaha2Id());
		}
		if (aramaSecenekleri.getEkSaha3Id() != null) {
			sb.append(whereStr + " P." + Personel.COLUMN_NAME_EK_SAHA3 + " =:ekSaha3");
			whereStr = " AND ";
			parametreMap.put("ekSaha3", aramaSecenekleri.getEkSaha3Id());
		}
		if (aramaSecenekleri.getEkSaha4Id() != null) {
			sb.append(whereStr + " P." + Personel.COLUMN_NAME_EK_SAHA4 + " =:ekSaha4");
			whereStr = " AND ";
			parametreMap.put("ekSaha4", aramaSecenekleri.getEkSaha4Id());
		}
		if (!authenticatedUser.isYoneticiKontratli() && sirketList != null && !sirketList.isEmpty()) {
			sb.append(whereStr + " P." + Personel.COLUMN_NAME_SIRKET + " :srk");
			whereStr = " AND ";
			List<Long> sList = new ArrayList<Long>();
			for (Sirket sr : sirketList)
				sList.add(sr.getId());
			parametreMap.put("srk", sList);
		}

		List<String> perNoList = new ArrayList<String>(ortakIslemler.getYetkiTumPersonelNoList());
		if (linkAdres != null) {
			if (sicilNo.trim().length() > 0 && !perNoList.contains(sicilNo.trim()))
				perNoList.add(sicilNo.trim());
		}
		if (parametreMap.isEmpty()) {
			sb.append(" INNER JOIN " + PersonelKGS.TABLE_NAME + " K ON K." + PersonelKGS.COLUMN_NAME_ID + "=P." + Personel.COLUMN_NAME_KGS_PERSONEL);
			sb.append(" AND K." + PersonelKGS.COLUMN_NAME_SICIL_NO + " :kSicilNo");
			parametreMap.put("kSicilNo", perNoList);
		}
		sb.append(whereStr + " P." + Personel.COLUMN_NAME_DURUM + " =1");
		parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			list = ortakIslemler.getPersonelList(sb, parametreMap);

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Personel pdksPersonel = (Personel) iterator.next();
			if (!pdksPersonel.isCalisiyor()) {
				iterator.remove();
				continue;
			}
			pdksPersonel.setCheckBoxDurum(Boolean.FALSE);
			if (user == null) {
				if (!perNoList.contains(pdksPersonel.getSicilNo()))
					iterator.remove();

			} else if (!user.isAdmin() && pdksPersonel.getSirket().getDepartman().getId().equals(user.getDepartman().getIcapciOlabilir()))
				iterator.remove();
		}

		if (!list.isEmpty())
			list = PdksUtil.sortObjectStringAlanList(null, list, "getAdSoyad", null);

		setPersonelList(list);
		return "";
	}

	public String fillPersonelHBList(User user) {
		List<Personel> list = new ArrayList<Personel>();
		Personel seciliPersonel = getInstance().getPdksPersonel();
		String adi = seciliPersonel.getAd();
		String soyadi = seciliPersonel.getSoyad();
		String sicilNo = seciliPersonel.getErpSicilNo();
		HashMap parametreMap = new HashMap();
		parametreMap.put("durum=", Boolean.TRUE);
		ArrayList departmanIdList = new ArrayList<String>();
		List<String> perNoList = ortakIslemler.getYetkiTumPersonelNoList();
		if (adi.trim().length() > 0)
			parametreMap.put("ad like", adi.trim() + "%");
		if (soyadi.trim().length() > 0)
			parametreMap.put("soyad like", soyadi.trim() + "%");
		if (sicilNo.trim().length() > 0)
			parametreMap.put("pdksSicilNo=", sicilNo.trim());
		else {
			for (Tanim departman : departmanList)
				departmanIdList.add(departman.getId().toString());
		}

		if (seciliPersonel.getSirket() != null)
			parametreMap.put("pdksSirket.id=", seciliPersonel.getSirket().getId());

		if (seciliPersonel.getEkSaha1() != null)
			parametreMap.put("ekSaha1.id=", seciliPersonel.getEkSaha1().getId());
		if (seciliPersonel.getEkSaha2() != null)
			parametreMap.put("ekSaha2.id=", seciliPersonel.getEkSaha2().getId());
		if (seciliPersonel.getEkSaha3() != null)
			parametreMap.put("ekSaha3.id=", seciliPersonel.getEkSaha3().getId());
		if (seciliPersonel.getEkSaha4() != null)
			parametreMap.put("ekSaha4.id=", seciliPersonel.getEkSaha4().getId());
		if (!authenticatedUser.isYoneticiKontratli() && !sirketList.isEmpty())
			parametreMap.put("pdksSirket", sirketList);

		if (parametreMap.size() == 1)
			parametreMap.put("karadagPersonel.sicilNo", perNoList);

		try {
			list = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, Personel.class);

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Personel pdksPersonel = (Personel) iterator.next();
			if (!pdksPersonel.isCalisiyor()) {
				iterator.remove();
				continue;
			}
			pdksPersonel.setCheckBoxDurum(Boolean.FALSE);
			if (user == null) {
				if (!perNoList.contains(pdksPersonel.getSicilNo()))
					iterator.remove();

			} else if (!user.isAdmin() && pdksPersonel.getSirket().getDepartman().getId().equals(user.getDepartman().getIcapciOlabilir()))
				iterator.remove();
		}

		if (!list.isEmpty())
			list = PdksUtil.sortObjectStringAlanList(null, list, "getAdSoyad", null);

		setPersonelList(list);
		return "";
	}

	public String fillKullaniciList() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		List<User> list = new ArrayList<User>();
		// Personel arananPersonel = getInstance().getPersonel();
		String adi = aramaSecenekleri.getAd();
		String soyadi = aramaSecenekleri.getSoyad();
		String sicilNo = aramaSecenekleri.getSicilNo();
		String username = getInstance().getUsername();
		String onEk = "";
		Class class1 = User.class;
		HashMap parametreMap = new HashMap();
		parametreMap.put(PdksEntityController.MAP_KEY_MAP, "getStaffId");
		if (roleList != null) {
			class1 = UserRoles.class;
			onEk = "user.";
			parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "user");
			parametreMap.put("role.rolename", roleList);
		}

		parametreMap.put(onEk + "durum=", Boolean.TRUE);
		parametreMap.put(onEk + "pdksPersonel.durum=", Boolean.TRUE);

		ArrayList<String> departmanIdList = new ArrayList<String>();
		List<String> perNoList = ortakIslemler.getYetkiTumPersonelNoList();
		if (adi.trim().length() > 0)
			parametreMap.put(onEk + "pdksPersonel.ad like", adi.trim() + "%");
		if (soyadi.trim().length() > 0)
			parametreMap.put(onEk + "pdksPersonel.soyad like", soyadi.trim() + "%");
		if (username.trim().length() > 0)
			parametreMap.put(onEk + "username like", username.trim() + "%");
		if (sicilNo.trim().length() > 0)
			parametreMap.put(onEk + "pdksPersonel.pdksSicilNo=", sicilNo.trim());
		else {
			for (Tanim departman : departmanList)
				departmanIdList.add(departman.getId().toString());
		}
		if (aramaSecenekleri.getSirketId() != null)
			parametreMap.put(onEk + "pdksPersonel.pdksSirket.id=", aramaSecenekleri.getSirketId());
		else if (sirketList != null && !sirketList.isEmpty()) {
			List<Long> idList = new ArrayList<Long>();
			for (Sirket sirket : sirketList)
				idList.add(sirket.getId());

			parametreMap.put(onEk + "pdksPersonel.pdksSirket.id", idList);
		}

 		if (aramaSecenekleri.getEkSaha1Id() != null)
			parametreMap.put(onEk + "pdksPersonel.ekSaha1.id=", aramaSecenekleri.getEkSaha1Id());
		if (aramaSecenekleri.getEkSaha2Id() != null)
			parametreMap.put(onEk + "pdksPersonel.ekSaha2.id=", aramaSecenekleri.getEkSaha2Id());
		if (aramaSecenekleri.getEkSaha3Id() != null)
			parametreMap.put(onEk + "pdksPersonel.ekSaha3.id=", aramaSecenekleri.getEkSaha3Id());
		if (aramaSecenekleri.getEkSaha3Id() != null)
			parametreMap.put(onEk + "pdksPersonel.ekSaha3.id=", aramaSecenekleri.getEkSaha4Id());
		if (parametreMap.size() > 1)
			try {
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				TreeMap map = pdksEntityController.getObjectByInnerObjectMapInLogic(parametreMap, class1, Boolean.FALSE);
				if (!map.isEmpty())
					list = new ArrayList<User>(map.values());
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				logger.debug(e.getMessage());
			}

		boolean admin = authenticatedUser.isAdmin() || authenticatedUser.isIK();

		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			User user = (User) iterator.next();
			if (!user.getPdksPersonel().isCalisiyor()) {
				iterator.remove();
				continue;
			}
			user.getPdksPersonel().setCheckBoxDurum(Boolean.FALSE);
			Personel pdksPersonel = user.getPdksPersonel();
			if (!admin && !perNoList.contains(pdksPersonel.getSicilNo()))
				iterator.remove();
		}

		if (!list.isEmpty())
			list = PdksUtil.sortObjectStringAlanList(null, list, "getAdSoyad", null);

		setUserList(list);
		return "";
	}

	public void fillDepartmanList(Session session) {
		List<Tanim> list = new ArrayList<Tanim>();
		if (sapDepartman)
			list = ortakIslemler.getTanimList(Tanim.TIPI_SAP_DEPARTMAN, session);
		if (pdksDepartman) {
			List pdksDepartmanList = ortakIslemler.getTanimList(Tanim.TIPI_PDKS_DEPARTMAN, session);
			if (!pdksDepartmanList.isEmpty())
				list.addAll(pdksDepartmanList);
		}
		setDepartmanList(list);

	}

	@Override
	public Object getId() {
		if (userId == null) {
			return super.getId();
		} else {
			return userId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public List<Tanim> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<Tanim> departmanList) {
		this.departmanList = departmanList;
	}

	public List<Sirket> getSirketList() {
		return sirketList;
	}

	public void setSirketList(List<Sirket> sirketList) {
		this.sirketList = sirketList;
	}

	public boolean isSapDepartman() {
		return sapDepartman;
	}

	public void setSapDepartman(boolean sapDepartman) {
		this.sapDepartman = sapDepartman;
	}

	public boolean isPdksDepartman() {
		return pdksDepartman;
	}

	public void setPdksDepartman(boolean pdksDepartman) {
		this.pdksDepartman = pdksDepartman;
	}

	public boolean isUserArama() {
		return userArama;
	}

	public void setUserArama(boolean userArama) {
		this.userArama = userArama;
	}

	public boolean isPersonelArama() {
		return personelArama;
	}

	public void setPersonelArama(boolean personelArama) {
		this.personelArama = personelArama;
	}

	public Tanim getSeciliDepartman() {
		return seciliDepartman;
	}

	public void setSeciliDepartman(Tanim seciliDepartman) {
		this.seciliDepartman = seciliDepartman;
	}

	public Sirket getSeciliSirket() {
		return seciliSirket;
	}

	public void setSeciliSirket(Sirket seciliSirket) {
		this.seciliSirket = seciliSirket;
	}

	public List<Personel> getPersonelList() {
		return personelList;
	}

	public void setPersonelList(List<Personel> personelList) {
		this.personelList = personelList;
	}

	public List<User> getUserList() {
		return userList;
	}

	public void setUserList(List<User> userList) {
		this.userList = userList;
	}

	public boolean isCheckBox() {
		return checkBox;
	}

	public void setCheckBox(boolean checkBox) {
		this.checkBox = checkBox;
	}

	public String getReRender() {
		return reRender;
	}

	public void setReRender(String reRender) {
		this.reRender = reRender;
	}

	public boolean isVisibled() {
		return visibled;
	}

	public void setVisibled(boolean visibled) {
		this.visibled = visibled;
	}

	public Sirket getSirket() {
		return pdksSirket;
	}

	public void setSirket(Sirket pdksSirket) {
		this.pdksSirket = pdksSirket;
	}

	public User getSeciliUser() {
		return seciliUser;
	}

	public void setSeciliUser(User seciliUser) {
		this.seciliUser = seciliUser;
	}

	public Personel getSeciliPersonel() {
		return seciliPersonel;
	}

	public void setSeciliPersonel(Personel seciliPersonel) {
		this.seciliPersonel = seciliPersonel;
	}

	public List<String> getRoleList() {
		return roleList;
	}

	public void setRoleList(List<String> roleList) {
		this.roleList = roleList;
	}

	public void kapat() {
		panelDurumDegistir();
	}

	public boolean isCheckBoxDurum() {
		return checkBoxDurum;
	}

	public void setCheckBoxDurum(boolean checkBoxDurum) {
		this.checkBoxDurum = checkBoxDurum;
	}

	public List<Personel> getSeciliPersonelList() {
		return seciliPersonelList;
	}

	public void setSeciliPersonelList(List<Personel> seciliPersonelList) {
		this.seciliPersonelList = seciliPersonelList;
	}

	public List<User> getSeciliUserList() {
		return seciliUserList;
	}

	public void setSeciliUserList(List<User> seciliUserList) {
		this.seciliUserList = seciliUserList;
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

	public List<SelectItem> getSirketItemList() {
		return sirketItemList;
	}

	public void setSirketItemList(List<SelectItem> sirketItemList) {
		this.sirketItemList = sirketItemList;
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

}
