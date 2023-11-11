package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.pdks.entity.Personel;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.pdks.security.entity.UserRoles;
import org.pdks.security.entity.UserVekalet;
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

@Name("userVekaletHome")
public class UserVekaletHome extends EntityHome<UserVekalet> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4101899805750967679L;
	static Logger logger = Logger.getLogger(UserVekaletHome.class);

	@RequestParameter
	Long userVekaletId;
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

	public List<User> kullaniciList = null;
	public List<User> kullaniciList2 = null;
	private List<String> roleList = new ArrayList<String>();
	private List<UserVekalet> userVekaletList = new ArrayList<UserVekalet>();
	private List<UserVekalet> userTotalVekaletList = new ArrayList<UserVekalet>();
	private String vekilAdi;
	private String vekilSoyadi;
	private String vekilSicilNo;
	private User vekilUser;
	private String vekaletVerenAdi;
	private Date devirBasTarih, devirBitTarih, basDate, bitDate;
	private String vekaletVerenSoyadi;
	private String vekaletVerenSicilNo;
	private User vekaletVerenUser;
	private String devirVekilAdi;
	private String devirVekilSoyadi;
	private String devirVekilSicilNo;
	private User devirVekilUser;
	private String aramaTipi;
	private UserVekalet devirUserVekalet;
	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public List<UserVekalet> getUserTotalVekaletList() {
		return userTotalVekaletList;
	}

	public void setUserTotalVekaletList(List<UserVekalet> userTotalVekaletList) {
		this.userTotalVekaletList = userTotalVekaletList;
	}

	public List<User> getKullaniciList() {
		return kullaniciList;
	}

	public void setKullaniciList(List<User> value) {
		this.kullaniciList = value;
	}

	public List<User> getKullaniciList2() {
		return kullaniciList2;
	}

	public void setKullaniciList2(List<User> value) {
		this.kullaniciList2 = value;
	}

	@Transient
	public boolean getEmptyPdksKullaniciList() {

		return !kullaniciList.isEmpty();
	}

	public List<UserVekalet> getUserVekaletList() {
		return userVekaletList;
	}

	public void setUserVekaletList(List<UserVekalet> userVekaletList) {
		this.userVekaletList = userVekaletList;
	}

	public String getVekilAdi() {
		if (getVekilUser() != null)
			vekilAdi = getVekilUser().getPdksPersonel().getAd();
		return vekilAdi;
	}

	public void setVekilAdi(String vekilAdi) {
		this.vekilAdi = vekilAdi;
	}

	public String getVekilSoyadi() {
		if (getVekilUser() != null)
			vekilSoyadi = getVekilUser().getPdksPersonel().getSoyad();
		return vekilSoyadi;
	}

	public void setVekilSoyadi(String vekilSoyadi) {
		this.vekilSoyadi = vekilSoyadi;
	}

	public String getVekilSicilNo() {
		if (getVekilUser() != null)
			vekilSicilNo = getVekilUser().getPdksPersonel().getPersonelKGS().getSicilNo();
		return vekilSicilNo;
	}

	public void setVekilSicilNo(String vekilSicilNo) {
		this.vekilSicilNo = vekilSicilNo;
	}

	public User getVekilUser() {
		return vekilUser;
	}

	public void setVekilUser(User vekilUser) {
		this.vekilUser = vekilUser;
	}

	@Override
	public Object getId() {
		if (userVekaletId == null) {
			return super.getId();
		} else {
			return userVekaletId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();

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
		if (bitDate == null) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, 6);
			bitDate = cal.getTime();
			cal.add(Calendar.MONTH, -18);
			basDate = cal.getTime();
		}

		girisSifirla();
		setInstance(new UserVekalet());
		roleList.clear();

		if (authenticatedUser.isYonetici()) {
			roleList.add(Role.TIPI_YONETICI);
			if (authenticatedUser.getDepartman().isAdminMi())
				roleList.add(Role.TIPI_YONETICI_KONTRATLI);

		} else if (authenticatedUser.isMudur())
			roleList.add(Role.TIPI_MUDUR);
		else {
			roleList.add(Role.TIPI_YONETICI);
			roleList.add(Role.TIPI_MUDUR);
			if (authenticatedUser.isIK() && authenticatedUser.getDepartman().isAdminMi())
				roleList.add(Role.TIPI_YONETICI_KONTRATLI);
		}

		fillUserTotalVekaletList();
	}

	public void girisSifirla() {
		UserVekalet userVekalet = new UserVekalet();
		devirUserVekalet = new UserVekalet();
		setDevirVekilAdi("");
		setDevirVekilSoyadi("");
		setDevirVekilSicilNo("");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1);
		userVekalet.setBasTarih(cal.getTime());
		userVekalet.setBitTarih(cal.getTime());
		boolean adminRole = ortakIslemler.getAdminRole(authenticatedUser);
		if (adminRole == false && (authenticatedUser.isYonetici() || authenticatedUser.isMudur())) {
			setVekaletVerenAdi(authenticatedUser.getPdksPersonel().getAd());
			setVekaletVerenSoyadi(authenticatedUser.getPdksPersonel().getSoyad());
			setVekaletVerenSicilNo(authenticatedUser.getStaffId());
			setVekaletVerenUser(authenticatedUser);
		} else {
			setVekaletVerenAdi("");
			setVekaletVerenSoyadi("");
			setVekaletVerenSicilNo("");
			setVekaletVerenUser(null);
		}
		setVekilAdi("");
		setVekilSoyadi("");
		setVekilSicilNo("");
		setVekilUser(null);
		setInstance(userVekalet);
	}

	public void kullaniciSec(User seciliYonetici) {
		if (seciliYonetici != null) {
			if (aramaTipi.equals("Vekil")) {
				setVekilUser(seciliYonetici);
				setVekilAdi(seciliYonetici.getPdksPersonel().getAd());
				setVekilSoyadi(seciliYonetici.getPdksPersonel().getSoyad());
				setVekilSicilNo(seciliYonetici.getStaffId());

			} else if (aramaTipi.equals("Vekalet Veren")) {
				setVekaletVerenUser(seciliYonetici);
				setVekaletVerenAdi(seciliYonetici.getPdksPersonel().getAd());
				setVekaletVerenSoyadi(seciliYonetici.getPdksPersonel().getSoyad());
				setVekaletVerenSicilNo(seciliYonetici.getStaffId());

			} else {
				setDevirVekilUser(seciliYonetici);
				setDevirVekilAdi(seciliYonetici.getPdksPersonel().getAd());
				setDevirVekilSoyadi(seciliYonetici.getPdksPersonel().getSoyad());
				setDevirVekilSicilNo(seciliYonetici.getStaffId());

			}

		}

	}

	public String fillPdksUserList() {
		kullaniciAra(vekilAdi, vekilSoyadi, vekilSicilNo, "Vekil", false);
		return "";

	}

	public String fillPdksUserList2() {
		kullaniciAra(vekaletVerenAdi, vekaletVerenSoyadi, vekaletVerenSicilNo, "Vekalet Veren", true);
		return "";
	}

	public String fillPdksUserList3() {
		setInstance(devirUserVekalet);
		kullaniciAra(devirVekilAdi, devirVekilSoyadi, devirVekilSicilNo, "devirVekilVeren", false);
		return "";
	}

	private void kullaniciAra(String adi, String soyadi, String sicilNo, String tipi, Boolean personelYonetici) {
		setAramaTipi(tipi);
		List<User> list = null;
		HashMap parametreMap = new HashMap();
		parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "user");
		parametreMap.put("role.rolename ", roleList);
		parametreMap.put("user.durum =", Boolean.TRUE);
		parametreMap.put("user.pdksPersonel.durum =", Boolean.TRUE);
		Date bugun = PdksUtil.getDate(Calendar.getInstance().getTime());
		if ((authenticatedUser.isYonetici() && !authenticatedUser.isIK()) || authenticatedUser.isMudur())
			parametreMap.put("user<>", authenticatedUser);
		parametreMap.put("user.pdksPersonel.sskCikisTarihi>=", bugun);
		parametreMap.put("user.pdksPersonel.iseBaslamaTarihi<=", bugun);
		if (PdksUtil.hasStringValue(sicilNo))
			parametreMap.put("user.pdksPersonel.pdksSicilNo=", sicilNo);
		else {
			if (PdksUtil.hasStringValue(adi))
				parametreMap.put("user.pdksPersonel.ad like", adi + "%");
			if (PdksUtil.hasStringValue(soyadi))
				parametreMap.put("user.pdksPersonel.soyad like", soyadi + "%");
		}
		UserVekalet userVekalet = getInstance();
		if (userVekalet.getId() != null)
			parametreMap.put("user.id<>", userVekalet.getYeniYonetici().getId());

		if (!ortakIslemler.getAdminRole(authenticatedUser))
			parametreMap.put("user.pdksPersonel.sirket.departman.id=", authenticatedUser.getDepartman().getId());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			if (parametreMap.size() > 0)
				list = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, UserRoles.class);
			if (personelYonetici && !list.isEmpty()) {
				TreeMap<Long, User> userMap = new TreeMap<Long, User>();
				for (User user : list) {
					if (user.isDurum() && user.getPdksPersonel().isCalisiyor())
						userMap.put(user.getPdksPersonel().getId(), user);
				}
				list.clear();
				parametreMap.clear();
				parametreMap.put(PdksEntityController.MAP_KEY_MAP, "getId");
				parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "yoneticisi");
				parametreMap.put("yoneticisi.id", new ArrayList(userMap.keySet()));
				parametreMap.put("sskCikisTarihi>=", bugun);
				parametreMap.put("iseBaslamaTarihi<=", bugun);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				TreeMap<Long, Personel> yoneticiMap = pdksEntityController.getObjectByInnerObjectMapInLogic(parametreMap, Personel.class, false);
				for (Long ld : yoneticiMap.keySet()) {
					if (userMap.containsKey(ld))
						list.add(userMap.get(ld));

				}
				userMap = null;
				if (!list.isEmpty())
					list = PdksUtil.sortObjectStringAlanList(null, list, "getAdSoyad", null);

			}
			if (!list.isEmpty()) {
				List<User> userList = PdksUtil.sortObjectStringAlanList(null, list, "getAdSoyad", null);
				if (tipi.equals("Vekil") || tipi.equals("Vekalet Veren")) {
					setKullaniciList(userList);
				} else {
					setKullaniciList2(userList);
				}

			} else {
				if (tipi.equals("Vekil") || tipi.equals("Vekalet Veren"))
					setKullaniciList(new ArrayList<User>());
				else
					setKullaniciList2(new ArrayList<User>());
				// setPdksKullaniciList(new ArrayList<User>());
			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		// setPdksKullaniciList(list);
	}

	public void fillUserVekaletList() {
		List<UserVekalet> list = null;

		HashMap parametreMap = new HashMap();
		parametreMap.put("durum =", Boolean.TRUE);
		if (authenticatedUser.isYonetici() || authenticatedUser.isMudur())
			parametreMap.put("vekaletVeren.id=", authenticatedUser.getId());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			if (parametreMap.size() > 0)
				list = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, UserVekalet.class);

			if (!list.isEmpty())
				list = PdksUtil.sortObjectStringAlanList(null, list, "getAdSoyad", null);
			parametreMap.clear();

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		setUserVekaletList(list);
	}

	public void fillUserTotalVekaletList() {
		List<UserVekalet> IKTarafindanYapilanVekaletList = null;
		Map totalMap = new HashMap();
		girisSifirla();
		Calendar cal = Calendar.getInstance();
		if (authenticatedUser.isYonetici() || authenticatedUser.isMudur())
			fillUserVekaletList();
		List<UserVekalet> list = null;
		HashMap parametreMap = new HashMap();
		parametreMap.put("durum =", Boolean.TRUE);
		if (!authenticatedUser.isIK())
			if (authenticatedUser.isYonetici() || authenticatedUser.isMudur())
				parametreMap.put("yeniYonetici.id=", authenticatedUser.getId());
		parametreMap.put("basTarih<=", ortakIslemler.tariheGunEkleCikar(cal, PdksUtil.getDate(bitDate), 1));
		parametreMap.put("bitTarih>=", PdksUtil.getDate(basDate));
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			if (parametreMap.size() > 0)
				list = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, UserVekalet.class);

			if (!list.isEmpty())
				list = PdksUtil.sortObjectStringAlanList(null, list, "getAdSoyad", null);
			parametreMap.clear();

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		if (authenticatedUser.isYonetici() || authenticatedUser.isMudur())
			list.addAll(getUserVekaletList());
		// vekalet veren ya da islem yapan authenticated user olanlari gostersin 9 Aralik Arzu
		// ekranda olusturan User gosterelim ki IK listeyi gorunce anlasin neden bu da ekranda geliyor diye
		if (authenticatedUser.isIK() || authenticatedUser.isAdmin()) {
			parametreMap.clear();
			parametreMap.put("durum =", Boolean.TRUE);
			parametreMap.put("olusturanUser=", authenticatedUser);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			IKTarafindanYapilanVekaletList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, UserVekalet.class);

			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				UserVekalet userVekalet = (UserVekalet) iterator.next();
				totalMap.put(userVekalet.getId().toString(), userVekalet);
			}
			for (Iterator iterator = IKTarafindanYapilanVekaletList.iterator(); iterator.hasNext();) {
				UserVekalet userVekalet = (UserVekalet) iterator.next();
				if (!totalMap.containsKey(userVekalet.getId().toString()))
					totalMap.put(userVekalet.getId().toString(), userVekalet);
			}

		}
		List<UserVekalet> userTotalVekaletList = new ArrayList<UserVekalet>(totalMap.values());
		if (userTotalVekaletList.size() > 1)
			userTotalVekaletList = PdksUtil.sortListByAlanAdi(userTotalVekaletList, "bitTarih", Boolean.TRUE);
		setUserTotalVekaletList(userTotalVekaletList);

	}

	@Transactional
	public String kayitSil() {
		// gercek silme islemi yapilmaz. durum fasif hale getirilir.
		UserVekalet vekalet = getInstance();

		if (vekalet.getBasTarih().compareTo(new Date()) < 0 || vekalet.getBitTarih().compareTo(new Date()) < 0) {
			facesMessages.add("Geçmiş tarihli bir vekaleti sistemden silemezsiniz.", "");
		} else {
			if (vekalet != null) {
				vekalet.setDurum(Boolean.FALSE);
				entityManager.merge(vekalet);
			}
			entityManager.flush();

			fillUserTotalVekaletList();

		}
		return "persisted";
	}

	@Transactional
	public String save() {
		ArrayList<String> mesajList = new ArrayList<String>();
		UserVekalet userVekalet = getInstance();
		if (userVekalet != null) {
			userVekalet.setYeniYonetici(getVekilUser());
			userVekalet.setVekaletVeren(getVekaletVerenUser());
			if (userVekalet.getYeniYonetici() == null)
				mesajList.add("Vekil yöneticiyi seçiniz");
			if (userVekalet.getVekaletVeren() == null)
				mesajList.add("Vekalet veren yöneticiyi seçiniz");
			if (PdksUtil.tarihKarsilastirNumeric(userVekalet.getBasTarih(), userVekalet.getBitTarih()) == 1)
				mesajList.add("Başlangıç tarihi bitiş tarihinden büyük olamaz");
			if (mesajList.isEmpty() && userVekalet.getVekaletVeren().getId().equals(userVekalet.getYeniYonetici().getId()))
				mesajList.add("Vekil ve vekalet veren aynı yönetici olamaz");
		} else
			mesajList.add("Hata oluştu.");
		if (mesajList.isEmpty()) {

			// vekalet veren uzerinde vekalet varsa o kullanici icin de yeni yoneticide vekalet
			// insert edilmeli
			HashMap parametreMap = new HashMap();
			parametreMap.put("durum=", Boolean.TRUE);

			parametreMap.put("vekaletVeren=", vekaletVerenUser);
			parametreMap.put("basTarih<=", userVekalet.getBasTarih());
			parametreMap.put("bitTarih>=", userVekalet.getBitTarih());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<UserVekalet> list = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, UserVekalet.class);
			boolean kesisenTarihteVekaletVar = Boolean.FALSE;
			if (list != null && !list.isEmpty()) { // bu durumda vekil transfer gibi islem yapiacak
				Date oldBas, oldBit;
				Date newBas = userVekalet.getBasTarih();
				Date newBit = userVekalet.getBitTarih();
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					UserVekalet tempVekalet = (UserVekalet) iterator.next();
					oldBas = tempVekalet.getBasTarih();
					oldBit = tempVekalet.getBitTarih();
					if (!kesisenTarihteVekaletVar) {
						if ((newBas.compareTo(oldBas) <= 0 && newBit.compareTo(oldBas) > 0) || (newBas.compareTo(oldBas) >= 0 && newBas.compareTo(oldBit) < 0))
							kesisenTarihteVekaletVar = Boolean.TRUE;
					} else
						break;

				}
				if (kesisenTarihteVekaletVar)
					mesajList.add("Verilen tarih aralığında kesişen vekalet bulunmaktadır. Önce devir işlemlerini yapınız.");

			}
			if ((userVekalet.getVekaletVeren().isMudur() && userVekalet.getYeniYonetici().isYonetici()) || (userVekalet.getVekaletVeren().isYonetici() && userVekalet.getYeniYonetici().isMudur())) {
				mesajList.add("Vekalet veren ve vekil aynı rolde olamalıdır.");

			}
			if (mesajList.isEmpty()) {
				if (PdksUtil.tarihKarsilastirNumeric(new Date(), userVekalet.getBasTarih()) == 1 || PdksUtil.tarihKarsilastirNumeric(userVekalet.getBasTarih(), userVekalet.getBitTarih()) == 1)
					mesajList.add("Başlangıç bitiş tarihlerini kontrol ediniz. Geçmişe yönelik kayıt giremezsiniz.");
				else {
					try {
						userVekalet.setVekaletVeren(vekaletVerenUser);
						if ((authenticatedUser.isYonetici() || authenticatedUser.isMudur()) && !authenticatedUser.isIK())
							userVekalet.setVekaletVeren(authenticatedUser);

						if (authenticatedUser.isIK()) {
							userVekalet.setVekaletVeren(vekaletVerenUser);
						}
						if (userVekalet.getId() == null) {
							userVekalet.setOlusturanUser(authenticatedUser);

						} else {
							userVekalet.setGuncelleyenUser(authenticatedUser);
							userVekalet.setGuncellemeTarihi(new Date());
						}
						pdksEntityController.saveOrUpdate(session, entityManager, userVekalet);

						session.flush();
						girisSifirla();
						setInstance(new UserVekalet());
						fillUserTotalVekaletList();
					} catch (Exception e) {
						logger.error("PDKS hata in : \n");
						e.printStackTrace();
						logger.error("PDKS hata out : " + e.getMessage());
						mesajList.add("Hata : " + e.getMessage());
						logger.error("Hata : " + e.getMessage());
					}
				}
			}
		}
		if (!mesajList.isEmpty()) {
			for (String mesaj : mesajList) {
				facesMessages.add(mesaj, "");

			}
		}

		return "not";

	}

	public String bitisTarihDegistir() {
		String islem = "";
		Date bitTarih = devirUserVekalet.getDevirBitTarih();
		boolean durum = Boolean.TRUE;
		if (bitTarih == null) {

			PdksUtil.addMessageError("Bitiş tarihi giriniz!");
			durum = Boolean.FALSE;
		} else if (!authenticatedUser.isAdmin() && !authenticatedUser.isIK() && PdksUtil.tarihKarsilastirNumeric(bitTarih, new Date()) != 1) {
			PdksUtil.addMessageError("Geçmiş ait bitiş tarihi girdiniz!");
			durum = Boolean.FALSE;

		} else if (PdksUtil.tarihKarsilastirNumeric(devirUserVekalet.getBasTarih(), bitTarih) == 1) {
			PdksUtil.addMessageError("Bitiş tarihi başlangıç tarihinde büyük giriniz!");
			durum = Boolean.FALSE;

		}
		if (durum) {
			islem = "persisted";
			UserVekalet vekalet = devirUserVekalet;
			vekalet.setBitTarih(bitTarih);
			vekalet.setGuncelleyenUser(authenticatedUser);
			vekalet.setGuncellemeTarihi(new Date());
			entityManager.merge(vekalet);
			entityManager.flush();
			fillUserTotalVekaletList();
		}
		return islem;

	}

	public String devret() {
		UserVekalet vekalet = devirUserVekalet;
		// vekalet.setDevirBasTarih(getDevirBasTarih());
		// vekalet.setDevirBitTarih(getDevirBitTarih());
		UserVekalet userVekaletDevir = new UserVekalet();
		UserVekalet userVekaletDevir2 = null;
		userVekaletDevir.setYeniYonetici(getDevirVekilUser());
		userVekaletDevir.setVekaletVeren(vekalet.getVekaletVeren());
		userVekaletDevir.setBasTarih(vekalet.getDevirBasTarih());
		userVekaletDevir.setBitTarih(vekalet.getDevirBitTarih());
		HashMap parametreMap = new HashMap();
		if (authenticatedUser != null)
			parametreMap.put("yeniYonetici.id=", authenticatedUser.getId());
		// KONtrol islemlerini yapalım

		if (facesMessages == null || facesMessages.getCurrentMessages().isEmpty()) {
			if (userVekaletDevir.getYeniYonetici().getId().equals(userVekaletDevir.getVekaletVeren().getId()))
				facesMessages.add("Vekil ve vekalet veren aynı yönetici olamaz.", "");
			else if (userVekaletDevir.getBasTarih().compareTo(new Date()) < 0 || userVekaletDevir.getBitTarih().compareTo(new Date()) < 0)
				facesMessages.add("Başlangıç bitiş tarihlerini kontrol ediniz. Geçmişe yönelik kayıt giremezsiniz.", "");
			else {
				if (vekalet.getDevirBasTarih().compareTo(vekalet.getBasTarih()) == 0 && vekalet.getDevirBitTarih().compareTo(vekalet.getBitTarih()) == 0)
					vekalet.setDurum(Boolean.FALSE);
				else if (vekalet.getDevirBasTarih().compareTo(vekalet.getBasTarih()) <= 0 && vekalet.getDevirBitTarih().compareTo(vekalet.getBitTarih()) < 0)
					vekalet.setBasTarih(vekalet.getDevirBitTarih());

				else if (vekalet.getDevirBasTarih().compareTo(vekalet.getBasTarih()) > 0) {
					// eski vekaletin bitisini sinirla ve bir tane de sonda kalan kısım icin insert edelim

					if (vekalet.getDevirBitTarih().compareTo(vekalet.getBitTarih()) < 0) {
						userVekaletDevir2 = new UserVekalet();
						userVekaletDevir2.setBasTarih(vekalet.getDevirBitTarih());
						userVekaletDevir2.setBitTarih(vekalet.getBitTarih());
						userVekaletDevir2.setVekaletVeren(vekalet.getVekaletVeren());
						userVekaletDevir2.setYeniYonetici(vekalet.getYeniYonetici());
						userVekaletDevir2.setOlusturanUser(authenticatedUser);
						userVekaletDevir2.setOlusturmaTarihi(new Date());
					}
					vekalet.setBitTarih(vekalet.getDevirBasTarih());
				}

				try {
					if (userVekaletDevir.getId() == null) {
						userVekaletDevir.setOlusturanUser(authenticatedUser);
						userVekaletDevir.setOlusturmaTarihi(new Date());
						pdksEntityController.saveOrUpdate(session, entityManager, userVekaletDevir);
						if (userVekaletDevir2 != null)
							pdksEntityController.saveOrUpdate(session, entityManager, userVekaletDevir2);
					} else {
						userVekaletDevir.setGuncelleyenUser(authenticatedUser);
						userVekaletDevir.setGuncellemeTarihi(new Date());
						pdksEntityController.saveOrUpdate(session, entityManager, userVekaletDevir);
					}
					session.flush();
					fillUserTotalVekaletList();

				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					logger.error("Hata : " + e.getMessage());
				}
			}
		}
		return "not";

	}

	public String getVekaletVerenAdi() {
		return vekaletVerenAdi;
	}

	public void setVekaletVerenAdi(String vekaletVerenAdi) {
		this.vekaletVerenAdi = vekaletVerenAdi;
	}

	public String getVekaletVerenSoyadi() {
		return vekaletVerenSoyadi;
	}

	public void setVekaletVerenSoyadi(String vekaletVerenSoyadi) {
		this.vekaletVerenSoyadi = vekaletVerenSoyadi;
	}

	public String getVekaletVerenSicilNo() {
		return vekaletVerenSicilNo;
	}

	public void setVekaletVerenSicilNo(String vekaletVerenSicilNo) {
		this.vekaletVerenSicilNo = vekaletVerenSicilNo;
	}

	public User getVekaletVerenUser() {
		return vekaletVerenUser;
	}

	public void setVekaletVerenUser(User vekaletVerenUser) {
		this.vekaletVerenUser = vekaletVerenUser;
	}

	public String getDevirVekilAdi() {
		return devirVekilAdi;
	}

	public void setDevirVekilAdi(String devirVekilAdi) {
		this.devirVekilAdi = devirVekilAdi;
	}

	public String getDevirVekilSoyadi() {
		return devirVekilSoyadi;
	}

	public void setDevirVekilSoyadi(String devirVekilSoyadi) {
		this.devirVekilSoyadi = devirVekilSoyadi;
	}

	public String getDevirVekilSicilNo() {
		return devirVekilSicilNo;
	}

	public void setDevirVekilSicilNo(String devirVekilSicilNo) {
		this.devirVekilSicilNo = devirVekilSicilNo;
	}

	public User getDevirVekilUser() {
		return devirVekilUser;
	}

	public void setDevirVekilUser(User devirVekilUser) {
		this.devirVekilUser = devirVekilUser;
	}

	public String getAramaTipi() {
		return aramaTipi;
	}

	public void setAramaTipi(String aramaTipi) {
		this.aramaTipi = aramaTipi;
	}

	public List<String> getRoleList() {
		return roleList;
	}

	public void setRoleList(List<String> roleList) {
		this.roleList = roleList;
	}

	public UserVekalet getDevirUserVekalet() {
		return devirUserVekalet;
	}

	public void setDevirUserVekalet(UserVekalet devirUserVekalet) {
		this.devirUserVekalet = devirUserVekalet;
	}

	public Date getDevirBasTarih() {
		return devirBasTarih;
	}

	public void setDevirBasTarih(Date devirBasTarih) {
		this.devirBasTarih = devirBasTarih;
	}

	public Date getDevirBitTarih() {
		return devirBitTarih;
	}

	public void setDevirBitTarih(Date devirBitTarih) {
		this.devirBitTarih = devirBitTarih;
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

}
