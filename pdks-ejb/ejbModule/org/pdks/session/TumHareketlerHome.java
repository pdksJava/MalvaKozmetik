package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.BasitHareket;
import org.pdks.entity.Departman;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.Kapi;
import org.pdks.entity.KapiKGS;
import org.pdks.entity.KapiSirket;
import org.pdks.entity.KapiView;
import org.pdks.entity.PdksPersonelView;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelHareketIslem;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.security.entity.User;

import com.pdks.webservice.MailFile;
import com.pdks.webservice.MailObject;
import com.pdks.webservice.MailStatu;

@Name("tumHareketlerHome")
public class TumHareketlerHome extends EntityHome<HareketKGS> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8457688159920114085L;
	static Logger logger = Logger.getLogger(TumHareketlerHome.class);

	@RequestParameter
	Long kgsHareketId;
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false)
	User authenticatedUser;
	@In(required = true, create = true)
	Renderer renderer;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	EntityManager entityManager;

	private List<HareketKGS> hareketList = new ArrayList<HareketKGS>();
	private List<Kapi> kapiList = new ArrayList<Kapi>();
	private Tanim seciliEkSaha1, seciliEkSaha2, seciliEkSaha3, seciliEkSaha4;
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private Departman departman;
	private Long departmanId;
	private Long sirketId;
	private List<SelectItem> sirketList;
	private Date basTarih, bitTarih;
	private Kapi kapi;
	private boolean pdksKapi = Boolean.TRUE, pdksHaricKapi = Boolean.FALSE, yemekKapi = Boolean.FALSE, guncellenmis = Boolean.FALSE;
	private String sicilNo = "", adi = "", soyadi = "", bolumAciklama;
	private byte[] zipVeri;
	private List<SelectItem> departmanList;

	private Session session;

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

	@Transactional
	public void filDepartmanList() {
		List<SelectItem> departmanListe = new ArrayList<SelectItem>();
		List<Departman> list = ortakIslemler.fillDepartmanTanimList(session);
		if (list.size() == 1) {
			departman = list.get(0);
			departmanId = departman.getId();
			fillSirketList();

		}

		for (Departman pdksDepartman : list)
			departmanListe.add(new SelectItem(pdksDepartman.getId(), pdksDepartman.getDepartmanTanim().getAciklama()));
		if (list.size() == 1)
			list.clear();

		setDepartmanList(departmanListe);
	}

	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		boolean ayniSayfa = authenticatedUser.getCalistigiSayfa() != null && authenticatedUser.getCalistigiSayfa().equals("tumHareketler");
		if (!ayniSayfa)
			authenticatedUser.setCalistigiSayfa("tumHareketler");
		sayfaGiris(session);
		setHareketList(new ArrayList<HareketKGS>());
		HareketKGS hareket = new HareketKGS();
		hareket.setPersonel(new PersonelView());
		hareket.setKapiView(new KapiView());
		hareket.setIslem(new PersonelHareketIslem());
		setInstance(hareket);
		setBasTarih(PdksUtil.buGun());
		setBitTarih(basTarih);
		setSicilNo("");
		setAdi("");
		setSoyadi("");
		guncellenmis = false;
		if (authenticatedUser.isYonetici())
			setPdksKapi(Boolean.TRUE);
		setKapiList(new ArrayList<Kapi>());
		departmanId = null;
		if (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()) {
			filDepartmanList();
			if (departmanList.size() == 1) {
				departmanId = (Long) departmanList.get(0).getValue();
				HashMap fields = new HashMap();
				fields.put("id", departmanId);
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				departman = (Departman) pdksEntityController.getObjectByInnerObject(fields, Departman.class);

			}

		}

		if (!authenticatedUser.isAdmin()) {
			if (departmanId == null) {
				setDepartman(authenticatedUser.getDepartman());
				departmanId = authenticatedUser.getDepartman().getId();
			}

			if (authenticatedUser.isIK())
				fillSirketList();
			else
				sirketId = authenticatedUser.getPdksPersonel().getSirket().getId();
		}
		if (!ayniSayfa)
			authenticatedUser.setCalistigiSayfa("");
		fillEkSahaTanim();
	}

	private void sayfaGiris(Session session) {

		fillSirketList();
		setSirketId(null);

	}

	public void fillSirketList() {

		List<Sirket> list = new ArrayList<Sirket>();

		if (departmanId != null && (authenticatedUser.isAdmin() || authenticatedUser.isIK())) {
			HashMap parametreMap = new HashMap();
			parametreMap.put("id", departmanId);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			departman = (Departman) pdksEntityController.getObjectByInnerObject(parametreMap, Departman.class);
			HashMap map = new HashMap();
			map.put(PdksEntityController.MAP_KEY_MAP, "getId");
			map.put(PdksEntityController.MAP_KEY_SELECT, "sirket");

			map.put("durum", Boolean.TRUE);

			if (!authenticatedUser.isAdmin() && !authenticatedUser.isIKAdmin())
				map.put("sirket.departman.id", authenticatedUser.getDepartman().getId());
			else if (departman != null)
				map.put("sirket.departman.id", departmanId);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			TreeMap sirketMap = pdksEntityController.getObjectByInnerObjectMap(map, Personel.class, Boolean.FALSE);
			if (!sirketMap.isEmpty()) {
				list = new ArrayList<Sirket>(sirketMap.values());
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					Sirket sirket = (Sirket) iterator.next();
					if (!sirket.getPdks())
						iterator.remove();
				}
				setDepartman(list.get(0).getDepartman());
			}

			if (list.size() > 1)
				list = PdksUtil.sortObjectStringAlanList(list, "getAd", null);

		}

		if (!list.isEmpty()) {
			if (list.size() == 1) {
				setSirketId(list.get(0).getId());

			}
		}
		List<SelectItem> sirketListe = new ArrayList<SelectItem>();
		for (Sirket sirket : list)
			sirketListe.add(new SelectItem(sirket.getId(), sirket.getAd()));
		list.clear();
		setSirketList(sirketListe);

	}

	public void fillHareketList() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.clear();
		guncellenmis = false;
		List<BasitHareket> kgsList = new ArrayList<BasitHareket>();
		HashMap parametreMap = new HashMap();
		boolean devam = Boolean.FALSE;
		ArrayList<Long> personelId = new ArrayList<Long>();
		TreeMap<Long, PersonelView> perMap = new TreeMap<Long, PersonelView>();
		TreeMap<Long, KapiView> kapiMap = null;
		Boolean pdks = pdksKapi;
		Boolean girisCikisKapilari = Boolean.TRUE, ayikla = false;
		if (pdksKapi) {
			logger.debug(PdksUtil.setTurkishStr(authenticatedUser.getAdSoyad() + " Pdks kapıları okunuyor."));
			if (pdksHaricKapi) {
				girisCikisKapilari = Boolean.FALSE;
				pdks = null;
			}

		} else {
			pdks = null;
			if (pdksHaricKapi) {
				ayikla = true;
				girisCikisKapilari = null;
				logger.debug(PdksUtil.setTurkishStr(authenticatedUser.getAdSoyad() + " Pdks haric kapıları okunuyor."));

			}
		}
		List<Long> kapiIdler = ortakIslemler.getKapiIdler(session, pdks, girisCikisKapilari);
		kapiMap = new TreeMap<Long, KapiView>();
		if (kapiIdler == null || !kapiIdler.isEmpty()) {

			if (kapiIdler != null)
				parametreMap.put("id", kapiIdler);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<KapiKGS> list1 = pdksEntityController.getObjectByInnerObjectList(parametreMap, KapiKGS.class);
			for (KapiKGS kapiKGS : list1)
				kapiMap.put(kapiKGS.getId(), kapiKGS.getKapiView());
			list1 = null;

			if (ayikla) {
				List<Long> list = new ArrayList<Long>(kapiMap.keySet());
				for (Long key : list) {
					Kapi kapi = kapiMap.get(key).getKapi();
					if (kapi != null && kapi.getPdks())
						kapiMap.remove(key);
				}
				list = null;
			}

		}

		ArrayList<Personel> tumPersoneller = null;
		List<String> yetkiTumPersonelNoList = null;
		if (!authenticatedUser.isIKAdmin()) {
			tumPersoneller = new ArrayList<Personel>(authenticatedUser.getTumPersoneller());
			yetkiTumPersonelNoList = ortakIslemler.getYetkiTumPersonelNoList();
		}

		if (authenticatedUser.isYoneticiKontratli() && tumPersoneller != null)
			ortakIslemler.digerPersoneller(tumPersoneller, yetkiTumPersonelNoList, basTarih, bitTarih, session);
		sicilNo = ortakIslemler.getSicilNo(sicilNo);
		if (authenticatedUser.isYoneticiKontratli()) {
			if (!(authenticatedUser.isIK() || authenticatedUser.isAdmin()))
				sirketId = null;
		}
		boolean admin = authenticatedUser.isIK() || authenticatedUser.isAdmin() || authenticatedUser.isGenelMudur();
		if (sicilNo.trim().length() > 0 || adi.trim().length() > 0 || soyadi.trim().length() > 0) {
			if (sicilNo.trim().length() > 0)
				sicilNo = ortakIslemler.getSicilNo(sicilNo);
			// if (authenticatedUser.isAdmin() || (authenticatedUser.isIK() && authenticatedUser.getDepartman().isAdminMi()) || yetkiTumPersonelNoList.contains(sicilNo)) {
			HashMap map = new HashMap();

			if (authenticatedUser.isIK() || authenticatedUser.isAdmin()) {

				if (sirketId != null)
					map.put("pdksPersonel.sirket.id=", sirketId);
				else if (departmanId != null)
					map.put("pdksPersonel.sirket.departman.id=", departmanId);
			}
			if (sicilNo.trim().length() > 0)
				map.put("pdksPersonel.pdksSicilNo=", sicilNo);
			else {
				if (adi.trim().length() > 0)
					map.put("pdksPersonel.ad like ", "%" + adi.trim() + "%");
				if (soyadi.trim().length() > 0)
					map.put("pdksPersonel.soyad like ", "%" + soyadi.trim() + "%");
			}

			if (!admin && map.isEmpty())
				map.put("pdksPersonel.pdksSicilNo", yetkiTumPersonelNoList);
			logger.debug(PdksUtil.setTurkishStr(authenticatedUser.getAdSoyad() + " Personel bilgiler okunuyor."));
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelView> personeller = ortakIslemler.getPersonelViewByPersonelKGSList(pdksEntityController.getObjectByInnerObjectListInLogic(map, PersonelView.class));
			if (!admin) {
				for (Iterator iterator = personeller.iterator(); iterator.hasNext();) {
					PersonelView personelView = (PersonelView) iterator.next();
					if (!yetkiTumPersonelNoList.contains(personelView.getSicilNo()))
						iterator.remove();

				}
			}
			devam = !personeller.isEmpty();
			if (devam) {
				perMap.clear();
				for (PersonelView personelView : personeller) {
					perMap.put(personelView.getId(), personelView);
					personelId.add(personelView.getId());
				}
			}
			personeller = null;
			map = null;
			// }

		} else {
			devam = Boolean.TRUE;
			if (!authenticatedUser.isAdmin() && !authenticatedUser.isIK() && !authenticatedUser.isGenelMudur()) {
				logger.debug(PdksUtil.setTurkishStr(authenticatedUser.getAdSoyad() + " Personel bilgiler okunuyor."));
				parametreMap.clear();
				if (tumPersoneller != null && !tumPersoneller.isEmpty())
					parametreMap.put("pdksPersonel", tumPersoneller);
				if (!parametreMap.isEmpty()) {
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<PdksPersonelView> list = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, PdksPersonelView.class);
					for (PdksPersonelView pdksPersonelView : list) {
						Long long1 = pdksPersonelView.getPersonelKGSId();
						if (long1 != null)
							perMap.put(long1, pdksPersonelView.getPersonelView());
					}
				}

				personelId.addAll(new ArrayList(perMap.keySet()));

			}
		}
		if (devam && kapiMap != null && kapiMap.isEmpty())
			devam = Boolean.FALSE;

		if (devam) {
			parametreMap.clear();
			StringBuffer sb = null;
			if ((personelId == null || personelId.isEmpty()) && (sirketId != null || departmanId != null)) {
				sb = new StringBuffer();
				sb.append("SELECT  V.* FROM " + PdksPersonelView.TABLE_NAME + " V WITH(nolock) ");
				sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + "=V." + PdksPersonelView.COLUMN_NAME_PERSONEL);
				if (!admin) {
					sb.append(" AND  P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " :ys");
					ArrayList<String> list = authenticatedUser.getYetkiTumPersonelNoList();
					parametreMap.put("ys", list);
				}
				if (sicilNo.trim().length() > 0) {
					sb.append(" AND  P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + "=:sicilNo");
					parametreMap.put("sicilNo", sicilNo);
				} else if (admin) {
					if (sirketId != null) {
						sb.append(" AND  P." + Personel.COLUMN_NAME_SIRKET + "=:sirketId");
						parametreMap.put("sirketId", sirketId);
					} else if (departmanId != null) {
						sb.append(" INNER JOIN " + Sirket.TABLE_NAME + " S ON S." + Sirket.COLUMN_NAME_ID + "=P." + Personel.COLUMN_NAME_SIRKET);
						sb.append(" AND  S." + Sirket.COLUMN_NAME_DEPARTMAN + "=:departmanId");
						parametreMap.put("departmanId", departmanId);
					}
				}
				perMap.clear();
				personelId.clear();
				List<PdksPersonelView> list = pdksEntityController.getObjectBySQLList(sb, parametreMap, PdksPersonelView.class);
				for (PdksPersonelView pdksPersonelView : list) {
					Long long1 = pdksPersonelView.getPersonelKGSId();
					if (long1 != null)
						perMap.put(long1, pdksPersonelView.getPersonelView());
				}
				personelId.addAll(new ArrayList(perMap.keySet()));
			}

			sb = new StringBuffer();
			sb.append("SELECT  V." + HareketKGS.COLUMN_NAME_ID + " FROM " + HareketKGS.TABLE_NAME + " V WITH(nolock) ");

			sb.append(" WHERE V." + HareketKGS.COLUMN_NAME_ZAMAN + " >=:vardiyaBas");
			sb.append(" AND V." + HareketKGS.COLUMN_NAME_ZAMAN + " <:vardiyaBit");

			if (kapiMap != null && !kapiMap.isEmpty()) {
				sb.append(" AND  V." + HareketKGS.COLUMN_NAME_KAPI + (kapiMap.size() > 1 ? " IN  ( " : "="));
				for (Iterator iterator = kapiMap.keySet().iterator(); iterator.hasNext();) {
					Long kapiId = (Long) iterator.next();
					sb.append(kapiId + (iterator.hasNext() ? "," : ""));
				}
				if (kapiMap.size() > 1)
					sb.append(" )");
			} else
				sb.append(" AND 1=2");

			if ((sirketId != null || departmanId != null) && personelId.isEmpty())
				sb.append(" AND 1=2");
			else if (!admin && personelId != null && !personelId.isEmpty() && (kapiMap.size() + personelId.size() < 1900)) {
				sb.append(" AND V." + HareketKGS.COLUMN_NAME_PERSONEL + ":p");
				parametreMap.put("p", (ArrayList) personelId.clone());
			}

			parametreMap.put("vardiyaBit", PdksUtil.tariheGunEkleCikar(PdksUtil.getDate(bitTarih), 1));
			parametreMap.put("vardiyaBas", basTarih);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				logger.debug(PdksUtil.setTurkishStr(authenticatedUser.getAdSoyad() + " Hareket bilgileri okunuyor."));
				if (personelId != null && !personelId.isEmpty()) {
					kgsList = ortakIslemler.getHareketBilgileri(new ArrayList<Long>(kapiMap.keySet()), personelId, basTarih, PdksUtil.tariheGunEkleCikar(PdksUtil.getDate(bitTarih), 1), BasitHareket.class, session);
				} else {
					List list = pdksEntityController.getObjectBySQLList(sb, parametreMap, null);
					kgsList = ortakIslemler.getHareketIdBilgileri(list, null, basTarih, bitTarih, session);
					list = null;
				}

				// kgsList = pdksEntityController.getObjectBySQLList(sb, parametreMap, BasitHareket.class);
			} catch (Exception e) {
				kgsList = new ArrayList<BasitHareket>();
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
			}

			if (!kgsList.isEmpty()) {
				if (pdksHaricKapi && !pdksKapi) {
					for (Iterator iterator = kgsList.iterator(); iterator.hasNext();) {
						BasitHareket hareket = (BasitHareket) iterator.next();
						if (!kapiMap.containsKey(hareket.getKapiId()))
							iterator.remove();
					}
				}
				kgsList = PdksUtil.sortListByAlanAdi(kgsList, "zamanLong", Boolean.FALSE);

				HashMap<Long, Long> kapilarMap = new HashMap<Long, Long>();
				for (BasitHareket hareket : kgsList) {
					if (kapiMap == null && !kapilarMap.containsKey(hareket.getKapiId()))
						kapilarMap.put(hareket.getKapiId(), hareket.getKapiId());
					if (personelId.isEmpty() && !perMap.containsKey(hareket.getPersonelId()))
						perMap.put(hareket.getPersonelId(), null);

				}
				if (kapiMap == null) {
					kapiMap = new TreeMap<Long, KapiView>();
					logger.debug(PdksUtil.setTurkishStr(authenticatedUser.getAdSoyad() + " Kapı bilgileri okunuyor."));
					parametreMap.clear();
					parametreMap.put("id", new ArrayList(kapilarMap.values()));
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<KapiKGS> list1 = pdksEntityController.getObjectByInnerObjectList(parametreMap, KapiKGS.class);
					for (KapiKGS kapiKGS : list1)
						kapiMap.put(kapiKGS.getId(), kapiKGS.getKapiView());
					list1 = null;
				}
				if (personelId.isEmpty()) {
					logger.debug(PdksUtil.setTurkishStr(authenticatedUser.getAdSoyad() + " Personel bilgileri okunuyor."));
					parametreMap.clear();
					// parametreMap.put(PdksEntityController.MAP_KEY_MAP, "getId");
					parametreMap.put("id", new ArrayList(perMap.keySet()));
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<PersonelKGS> list = pdksEntityController.getObjectByInnerObjectList(parametreMap, PersonelKGS.class);

					perMap.clear();

					for (PersonelKGS personelKGS : list)
						perMap.put(personelKGS.getId(), personelKGS.getPersonelView());
				}
				TreeMap<Long, KapiSirket> kapiSirketMap = new TreeMap<Long, KapiSirket>();
				HashMap map = new HashMap();
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<KapiSirket> kapiSirketList = pdksEntityController.getObjectByInnerObjectList(map, KapiSirket.class);
				for (KapiSirket kapiSirket : kapiSirketList) {
					kapiSirketMap.put(kapiSirket.getId(), kapiSirket);
				}
				kapiSirketList = null;
				List<HareketKGS> list = new ArrayList<HareketKGS>();
				List<Long> islemIdler = new ArrayList<Long>();
				List<String> idList = new ArrayList<String>();
				for (BasitHareket hareket : kgsList) {
					if (hareket.getDurum() == 1 && perMap.containsKey(hareket.getPersonelId()) && kapiMap.containsKey(hareket.getKapiId())) {
						if (idList.contains(hareket.getId()))
							continue;
						idList.add(hareket.getId());
						HareketKGS kgsHareket = hareket.getKgsHareket();
						if (kgsHareket.getKgsSirketId() != null)
							kgsHareket.setKapiSirket(kapiSirketMap.get(kgsHareket.getKgsSirketId()));
						kgsHareket.setKapiView(kapiMap.get(hareket.getKapiId()));
						kgsHareket.setPersonel(perMap.get(hareket.getPersonelId()));
						if (kgsHareket.getIslemId() != null)
							islemIdler.add(kgsHareket.getIslemId());
						list.add(kgsHareket);

					}
				}
				idList = null;
				if (!islemIdler.isEmpty()) {
					logger.debug(PdksUtil.setTurkishStr(authenticatedUser.getAdSoyad() + " Hareket islem bilgileri okunuyor."));
					parametreMap.clear();
					parametreMap.put(PdksEntityController.MAP_KEY_MAP, "getId");
					parametreMap.put("id", islemIdler);
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					TreeMap<Long, PersonelHareketIslem> islemMap = pdksEntityController.getObjectByInnerObjectMap(parametreMap, PersonelHareketIslem.class, Boolean.FALSE);

					parametreMap.clear();
					sb = new StringBuffer();
					sb.append("SELECT P.ISLEM_ID,HAREKET_ZAMANI from PDKS_LOG P WITH(nolock) ");
					sb.append(" INNER JOIN PDKS_ISLEM I ON I.ID=P.ISLEM_ID AND I.ISLEM_TIPI='U' ");

					sb.append(" WHERE P.ISLEM_ID :s AND P.DURUM=0 ");
					parametreMap.put("s", islemIdler);
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					TreeMap<Long, Date> islemTarihMap = new TreeMap<Long, Date>();
					List<Object[]> islemList = pdksEntityController.getObjectBySQLList(sb, parametreMap, null);
					for (Object[] objects : islemList) {
						Long key = ((BigInteger) objects[0]).longValue();
						Date orjinalZaman = new Date(((Timestamp) objects[1]).getTime());
						islemTarihMap.put(key, orjinalZaman);
					}

					for (Iterator iterator = list.iterator(); iterator.hasNext();) {
						HareketKGS kgsHareket = (HareketKGS) iterator.next();

						if (kgsHareket.getIslemId() != null && islemMap.containsKey(kgsHareket.getIslemId())) {
							PersonelHareketIslem islem = islemMap.get(kgsHareket.getIslemId());
							kgsHareket.setIslem(islem);
							if (islem != null)
								guncellenmis = Boolean.TRUE;
							if (islem.getIslemTipi().equalsIgnoreCase("U") && islemTarihMap.containsKey(kgsHareket.getIslemId())) {
								kgsHareket.setOrjinalZaman(islemTarihMap.get(kgsHareket.getIslemId()));
								kgsHareket.setSirket("K");

							} else
								kgsHareket.setOrjinalZaman(null);
							if (kgsHareket.getIslem().getIslemTipi() == null || kgsHareket.getIslem().getIslemTipi().equals("D"))
								iterator.remove();

						}

					}
					islemMap = null;
				}
				logger.debug(PdksUtil.setTurkishStr(authenticatedUser.getAdSoyad() + " Hareket bilgileri okundu."));
				islemIdler = null;
				setHareketList(list);
			} else
				setHareketList(new ArrayList<HareketKGS>());

		} else
			setHareketList(new ArrayList<HareketKGS>());
		setZipVeri(null);
		if (!hareketList.isEmpty() && sicilNo.trim().length() == 0 && (authenticatedUser.isAdmin() || authenticatedUser.isIK())) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			String dosyaAdi = "tumHareketler" + (authenticatedUser.getShortUsername() != null ? authenticatedUser.getShortUsername().trim() : "");
			try {
				logger.debug(PdksUtil.setTurkishStr(authenticatedUser.getAdSoyad() + " " + dosyaAdi + " dosyasi olusturuluyor."));
				if (!PdksUtil.getTestDurum()) {
					textZipDosyaOlustur(bos, dosyaAdi);
					logger.debug(PdksUtil.setTurkishStr(authenticatedUser.getAdSoyad() + " " + dosyaAdi + " dosyasi mail gonderiliyor."));
					if (zipVeri != null) {
						// ortakIslemler.mailGonder(renderer, "/email/hareketMail.xhtml");
						try {
							MailObject mail = new MailObject();
							mail.setSubject("Kapı hareketleri zip");
							String body = "<p>" + authenticatedUser.getTarihFormatla(basTarih, PdksUtil.getDateFormat()) + "-" + authenticatedUser.getTarihFormatla(bitTarih, PdksUtil.getDateFormat()) + "  kapı giriş çıkış 	hareketleri ektedir.</p>";
							mail.setBody(body);
							mail.getToList().add(authenticatedUser.getMailPersonel());
							MailFile mf = new MailFile();
							mf.setDisplayName("tumHareketler.zip");
							mf.setIcerik(zipVeri);
							mail.getAttachmentFiles().add(mf);
							ortakIslemler.mailSoapServisGonder(true, mail, renderer, "/email/hareketMail.xhtml", session);
							if (authenticatedUser.isAdmin())
								PdksUtil.addMessageInfo(dosyaAdi + " dosyası mail gönderildi.");
						} catch (Exception e) {
							logger.error("PDKS hata in : \n");
							e.printStackTrace();
							logger.error("PDKS hata out : " + e.getMessage());
							PdksUtil.addMessageError(e.getMessage());
						}

					}
				}

			} catch (Exception e) {
				logger.error(e);
			}

		}
	}

	public String textDosyaAktar() {

		String dosyaAdi = "tumHareketler" + (authenticatedUser.getShortUsername() != null ? authenticatedUser.getShortUsername().trim() : "");
		try {
			if (zipVeri == null) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				String zipDosyaAdi = "tumHareketler" + (authenticatedUser.getShortUsername() != null ? authenticatedUser.getShortUsername().trim() : "");
				textZipDosyaOlustur(bos, zipDosyaAdi);
				bos = null;
			}
			byte[] bytes = zipVeri;
			if (bytes != null) {

				HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
				ServletOutputStream sos = response.getOutputStream();
				response.setContentType("application/zip");
				response.setHeader("Expires", "0");
				response.setHeader("Pragma", "cache");
				response.setHeader("Cache-Control", "cache");
				response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(dosyaAdi, "UTF-8") + ".zip");
				response.setContentLength(bytes.length);
				sos.write(bytes, 0, bytes.length);
				sos.flush();
				sos.close();
				FacesContext.getCurrentInstance().responseComplete();
			}
		} catch (Exception iox) {
			// do stuff with exception
			iox.printStackTrace();
		}

		return "";
	}

	/**
	 * @param bos
	 * @param dosyaAdi
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private byte[] textZipDosyaOlustur(ByteArrayOutputStream bos, String dosyaAdi) throws IOException, FileNotFoundException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		StringBuffer sb = new StringBuffer();
		sb.append("Zaman|" + ortakIslemler.sirketAciklama() + "|Adı Soyadı|" + ortakIslemler.personelNoAciklama() + "|Kapi|");
		if (guncellenmis) {
			sb.append("Orjinal Zamanı|İşlem Yapan|");
		}
		sb.append("Oluşturulma Zamanı");
		for (Iterator iter = hareketList.iterator(); iter.hasNext();) {
			HareketKGS hareket = (HareketKGS) iter.next();

			try {
				String sirket = "";
				try {
					sirket = hareket.getPersonel().getPdksPersonel().getSirket().getAd();
				} catch (Exception e1) {
					sirket = "" + ortakIslemler.sirketAciklama() + " tanımsız";
				}
				if (sb == null)
					sb = new StringBuffer();
				sb.append("\n" + PdksUtil.convertToDateString(hareket.getZaman(), "dd/MM/yyyy HH:mm") + "|");
				sb.append(sirket + "|");
				sb.append(hareket.getAdSoyad() + "|");
				sb.append(hareket.getSicilNo() + "|");
				sb.append(hareket.getKapiView().getAciklama() + "|");
				if (guncellenmis) {
					if (guncellenmis) {
						if ((hareket.getSirket() != null && hareket.getSirket().equals(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_KGS)) || hareket.getIslem() != null) {
							sb.append((hareket.getOrjinalZaman() != null ? PdksUtil.convertToDateString(hareket.getOrjinalZaman(), "dd/MM/yyyy HH:mm") : "") + "|");
							sb.append((hareket.getIslem() != null ? hareket.getIslem().getGuncelleyenUser().getAdSoyad() : "") + "|");
						} else {
							sb.append("||");
						}
					}
				}

				sb.append(PdksUtil.convertToDateString(hareket.getOlusturmaZamani(), "dd/MM/yyyy HH:mm"));
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());

			}
			if (sb != null) {
				byte[] b = sb.toString().getBytes("UTF-8");
				baos.write(b, 0, b.length);
				sb = null;
			}

		}

		byte[] bytes = baos.toByteArray();

		ZipOutputStream zos = new ZipOutputStream(bos);

		ZipEntry zipEntry = new ZipEntry(dosyaAdi + ".txt");
		zos.putNextEntry(zipEntry);

		int length = bytes.length;
		zos.write(bytes, 0, length);
		zos.closeEntry();
		zos.close();
		bos.close();
		bos.flush();
		byte[] bytesZip = null;
		zos.write(bytesZip);
		setZipVeri(bytesZip);
		return bytes;
	}

	public String zipAktar() {
		try {
			logger.debug(PdksUtil.setTurkishStr(authenticatedUser.getAdSoyad() + " Hareket excel dosyası hazırlanıyor."));
			ByteArrayOutputStream baos = excelDevam();
			if (baos != null) {
				logger.debug(PdksUtil.setTurkishStr(authenticatedUser.getAdSoyad() + " Hareket zip dosyası hazırlanıyor."));
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ZipOutputStream zos = new ZipOutputStream(bos);
				ZipEntry zipEntry = new ZipEntry("tumHareketler.xlsx");
				zos.putNextEntry(zipEntry);
				byte[] bytes = baos.toByteArray();
				int length = bytes.length;
				zos.write(bytes, 0, length);
				zos.closeEntry();
				zos.close();
				bos.close();
				bos.flush();

				bytes = bos.toByteArray();
				File zipDosya = PdksUtil.dosyaOlustur("/tmp/tumHareketler" + (authenticatedUser.getShortUsername() != null ? authenticatedUser.getShortUsername().trim() : "") + ".zip", bytes);
				logger.debug(PdksUtil.setTurkishStr(authenticatedUser.getAdSoyad() + " Hareket zip dosyası kayıt ediliyor."));
				setZipVeri(bytes);
				MailStatu mailSatu = null;
				try {
					// ortakIslemler.mailGonder(renderer, "/email/hareketMail.xhtml");
					if (bytes != null) {
						MailObject mail = new MailObject();
						mail.setSubject("Kapı hareketleri zip");
						String body = "<p>" + authenticatedUser.getTarihFormatla(basTarih, PdksUtil.getDateFormat()) + "-" + authenticatedUser.getTarihFormatla(bitTarih, PdksUtil.getDateFormat()) + " kapı giriş çıkış 	hareketleri ektedir.</p>";
						mail.setBody(body);
						mail.getToList().add(authenticatedUser.getMailPersonel());
						MailFile mf = new MailFile();
						mf.setDisplayName("tumHareketler.zip");
						mf.setIcerik(bytes);
						mail.getAttachmentFiles().add(mf);
						mailSatu = ortakIslemler.mailSoapServisGonder(true, mail, renderer, "/email/hareketMail.xhtml", session);
					}
					logger.debug(PdksUtil.setTurkishStr(authenticatedUser.getAdSoyad() + " Hareket zip dosyası mail olarak gönderildi."));

				} catch (Exception e) {
					logger.error("Onay Akis Hata in  : ");
					e.printStackTrace();
					logger.error("Onay Akis Hata out  : " + e.getMessage());

				}
				if (mailSatu != null && mailSatu.isDurum())
					PdksUtil.addMessageInfo("Hareket dosyası mail gönderildi");
				if (zipDosya != null && zipDosya.exists())
					zipDosya.delete();
			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		return "";
	}

	public static File dosyaOlustur(String fileName, byte[] s) {

		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			fos.write(s);
			fos.close();
		} catch (Exception e) {
			logger.error("EDefter out : \n");
			e.printStackTrace();
			logger.error("EDefter out : " + e.getMessage());

		}
		File file = new File(fileName);
		return file;
	}

	public String zipDosyaAktar() {
		try {
			ByteArrayOutputStream baos = excelDevam();
			if (baos != null) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ZipOutputStream zos = new ZipOutputStream(bos);
				ZipEntry zipEntry = new ZipEntry("tumHareketler.xlsx");
				zos.putNextEntry(zipEntry);
				byte[] bytes = baos.toByteArray();
				int length = bytes.length;
				zos.write(bytes, 0, length);
				zos.closeEntry();
				zos.close();
				bos.close();
				bos.flush();
				HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
				ServletOutputStream sos = response.getOutputStream();
				response.setContentType("application/zip");
				response.setHeader("Expires", "0");
				response.setHeader("Pragma", "cache");
				response.setHeader("Cache-Control", "cache");
				response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("tumHareketler.zip", "UTF-8"));

				response.setContentLength(bos.size());
				bytes = bos.toByteArray();
				sos.write(bytes, 0, bytes.length);
				sos.flush();
				sos.close();
				FacesContext.getCurrentInstance().responseComplete();

			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		return "";
	}

	public String excelAktar() {
		try {
			ByteArrayOutputStream baosDosya = excelDevam();
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, "tumHareketler.xlsx");

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		return "";
	}

	private void fillEkSahaTanim() {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
	}

	public ByteArrayOutputStream excelDevam() throws Exception {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, "Tüm Hareketler", Boolean.TRUE);
		CellStyle styleCenter = ExcelUtil.getStyleData(wb);
		styleCenter.setAlignment(CellStyle.ALIGN_CENTER);
		CellStyle style = ExcelUtil.getStyleData(wb);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle timeStamp = ExcelUtil.getCellStyleTimeStamp(wb);
		int row = 0;
		int col = 0;
		boolean yonetici = authenticatedUser.isAdmin() || authenticatedUser.isIK();
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Zaman");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Kapi");
		if (yonetici)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("KGS Şirket");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);

		if (guncellenmis) {
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Orjinal Zamanı");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İşlem Yapan");

		}
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Oluşturulma Zamanı");

		for (Iterator iter = hareketList.iterator(); iter.hasNext();) {
			HareketKGS hareket = (HareketKGS) iter.next();
			row++;
			col = 0;
			Personel personel = null;
			try {
				personel = hareket.getPersonel().getPdksPersonel();
			} catch (Exception ex) {
				logger.error(ex);
				ex.printStackTrace();

			}

			try {
				ExcelUtil.getCell(sheet, row, col++, timeStamp).setCellValue(hareket.getZaman());
				String sirket = "";
				try {
					sirket = personel.getSirket().getAd();
				} catch (Exception e1) {
					sirket = "" + ortakIslemler.sirketAciklama() + " tanımsız";
				}
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(sirket);
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(hareket.getSicilNo());
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(hareket.getAdSoyad());
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(hareket.getKapiView().getAciklama());
				if (yonetici)
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(hareket.getKapiSirket() != null ? hareket.getKapiSirket().getAciklama() : "");

				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel != null && personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
				if (guncellenmis) {
					if ((hareket.getSirket() != null && hareket.getSirket().equals(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_KGS)) || hareket.getIslem() != null) {
						if (hareket.getOrjinalZaman() != null)
							ExcelUtil.getCell(sheet, row, col++, timeStamp).setCellValue(hareket.getOrjinalZaman());
						else
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue(hareket.getIslem() != null ? hareket.getIslem().getGuncelleyenUser().getAdSoyad() : "");
					} else {
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
					}

				}
				ExcelUtil.getCell(sheet, row, col++, timeStamp).setCellValue(hareket.getOlusturmaZamani());
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				logger.debug(row);

			}

		}

		try {
			for (int i = 0; i < col; i++)
				sheet.autoSizeColumn(i);
			baos = new ByteArrayOutputStream();
			wb.write(baos);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			baos = null;
		}
		return baos;
	}

	public void fillkapiList() {
		List<Kapi> kapiList = new ArrayList<Kapi>();
		HashMap parametreMap = new HashMap();
		parametreMap.put("durum=", Boolean.TRUE);

		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		if (authenticatedUser.isAdmin())
			kapiList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, Kapi.class);
		setKapiList(kapiList);

	}

	public List<HareketKGS> getHareketList() {
		return hareketList;
	}

	public void setHareketList(List<HareketKGS> hareketList) {
		this.hareketList = hareketList;
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

	public List<Kapi> getKapiList() {
		return kapiList;
	}

	public void setKapiList(List<Kapi> kapiList) {
		this.kapiList = kapiList;
	}

	public Kapi getkapi() {
		return kapi;
	}

	public void setkapi(Kapi kapi) {
		this.kapi = kapi;
	}

	public String getSicilNo() {
		return sicilNo;
	}

	public void setSicilNo(String sicilNo) {
		this.sicilNo = sicilNo;
	}

	public boolean isPdksKapi() {
		return pdksKapi;
	}

	public void setPdksKapi(boolean pdksKapi) {
		this.pdksKapi = pdksKapi;
	}

	public String getAdi() {
		return adi;
	}

	public void setAdi(String adi) {
		this.adi = adi;
	}

	public String getSoyadi() {
		return soyadi;
	}

	public void setSoyadi(String soyadi) {
		this.soyadi = soyadi;
	}

	public boolean isPdksHaricKapi() {
		return pdksHaricKapi;
	}

	public void setPdksHaricKapi(boolean pdksHaricKapi) {
		this.pdksHaricKapi = pdksHaricKapi;
	}

	public byte[] getZipVeri() {
		return zipVeri;
	}

	public void setZipVeri(byte[] zipVeri) {
		this.zipVeri = zipVeri;
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

	public Long getSirketId() {
		return sirketId;
	}

	public void setSirketId(Long sirketId) {
		this.sirketId = sirketId;
	}

	public List<SelectItem> getSirketList() {
		return sirketList;
	}

	public void setSirketList(List<SelectItem> sirketList) {
		this.sirketList = sirketList;
	}

	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman departman) {
		this.departman = departman;
	}

	public Long getDepartmanId() {
		return departmanId;
	}

	public void setDepartmanId(Long departmanId) {
		this.departmanId = departmanId;
	}

	public List<SelectItem> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<SelectItem> departmanList) {
		this.departmanList = departmanList;
	}

	public boolean isYemekKapi() {
		return yemekKapi;
	}

	public void setYemekKapi(boolean yemekKapi) {
		this.yemekKapi = yemekKapi;
	}

	public boolean isGuncellenmis() {
		return guncellenmis;
	}

	public void setGuncellenmis(boolean guncellenmis) {
		this.guncellenmis = guncellenmis;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}
}
