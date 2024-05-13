package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
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
	private Long departmanId, sirketId;
	private List<SelectItem> sirketList;
	private Date basTarih, bitTarih;
	private Kapi kapi;
	private boolean pdksKapi = Boolean.TRUE, pdksHaricKapi = Boolean.FALSE, yemekKapi = Boolean.FALSE, guncellenmis = Boolean.FALSE, kgsUpdateGoster = Boolean.FALSE;
	private boolean ikRole = false, vardiyaOku = false;
	private Boolean vardiyaOkuDurum = null;
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
		vardiyaOku = false;
		if (vardiyaOkuDurum == null) {
			if (authenticatedUser.isAdmin() || ortakIslemler.getParameterKeyHasStringValue("hareketVardiyaOku"))
				vardiyaOkuDurum = ortakIslemler.getParameterKey("hareketVardiyaOku").equals("1");
		}
		boolean ayniSayfa = authenticatedUser.getCalistigiSayfa() != null && authenticatedUser.getCalistigiSayfa().equals("tumHareketler");
		if (!ayniSayfa)
			authenticatedUser.setCalistigiSayfa("tumHareketler");
		sayfaGiris(session);
		ikRole = ortakIslemler.getIKRolSayfa(authenticatedUser) || authenticatedUser.isRaporKullanici();
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
		if (authenticatedUser.isAdmin() || ikRole) {
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

			if (ikRole)
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

		if (departmanId != null && (authenticatedUser.isAdmin() || ikRole)) {
			HashMap parametreMap = new HashMap();
			parametreMap.put("id", departmanId);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			departman = (Departman) pdksEntityController.getObjectByInnerObject(parametreMap, Departman.class);
			HashMap map = new HashMap();
			map.put(PdksEntityController.MAP_KEY_MAP, "getId");
			map.put(PdksEntityController.MAP_KEY_SELECT, "sirket");

			map.put("durum", Boolean.TRUE);

			if (!authenticatedUser.isAdmin() && !authenticatedUser.isIKAdmin() && !authenticatedUser.isRaporKullanici())
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
			logger.debug(authenticatedUser.getAdSoyad() + " Pdks kapıları okunuyor.");
			if (pdksHaricKapi) {
				girisCikisKapilari = Boolean.FALSE;
				pdks = null;
			}

		} else {
			pdks = null;
			if (pdksHaricKapi) {
				ayikla = true;
				girisCikisKapilari = null;
				logger.debug(authenticatedUser.getAdSoyad() + " Pdks haric kapıları okunuyor.");

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
		parametreMap.clear();
		ArrayList<Personel> tumPersoneller = null;
		List<String> yetkiTumPersonelNoList = null;
		if (!authenticatedUser.isIKAdmin()) {
			tumPersoneller = new ArrayList<Personel>(authenticatedUser.getTumPersoneller());
			yetkiTumPersonelNoList = ortakIslemler.getYetkiTumPersonelNoList();
		}

		if (authenticatedUser.isYoneticiKontratli() && tumPersoneller != null)
			ortakIslemler.digerPersoneller(tumPersoneller, yetkiTumPersonelNoList, basTarih, bitTarih, session);

		sicilNo = ortakIslemler.getSicilNo(sicilNo);
		vardiyaOku = false;

		if (authenticatedUser.isYoneticiKontratli()) {
			if (!(ikRole || authenticatedUser.isAdmin()))
				sirketId = null;
		}
		boolean admin = ikRole || authenticatedUser.isAdmin() || authenticatedUser.isGenelMudur();
		HashMap<Long, Personel> personelMap = new HashMap<Long, Personel>();
		if (PdksUtil.hasStringValue(sicilNo) || PdksUtil.hasStringValue(adi) || PdksUtil.hasStringValue(soyadi)) {

			// if (authenticatedUser.isAdmin() || (authenticatedUser.isIK() && authenticatedUser.getDepartman().isAdminMi()) || yetkiTumPersonelNoList.contains(sicilNo)) {
			HashMap map = new HashMap();

			if (ikRole || authenticatedUser.isAdmin()) {

				if (sirketId != null)
					map.put("pdksPersonel.sirket.id=", sirketId);
				else if (departmanId != null)
					map.put("pdksPersonel.sirket.departman.id=", departmanId);
			}
			if (PdksUtil.hasStringValue(sicilNo)) {
				if (PdksUtil.getSicilNoUzunluk() != null)
					map.put("pdksPersonel.pdksSicilNo=", sicilNo);
				else {
					Long sayi = null;
					try {
						sayi = Long.parseLong(sicilNo);
					} catch (Exception e) {
					}
					if (sayi != null && sayi.longValue() > 0)
						map.put("pdksPersonel.pdksSicilNo like ", "%" + sicilNo.trim());
					else
						map.put("pdksPersonel.pdksSicilNo like ", sicilNo.trim() + "%");
				}
			}

			else {
				if (PdksUtil.hasStringValue(adi))
					map.put("pdksPersonel.ad like ", "%" + adi.trim() + "%");
				if (PdksUtil.hasStringValue(soyadi))
					map.put("pdksPersonel.soyad like ", "%" + soyadi.trim() + "%");
			}

			if (!admin && map.isEmpty())
				map.put("pdksPersonel.pdksSicilNo", yetkiTumPersonelNoList);
			logger.debug(authenticatedUser.getAdSoyad() + " Personel bilgiler okunuyor.");
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelView> personeller = ortakIslemler.getPersonelViewByPersonelKGSList(pdksEntityController.getObjectByInnerObjectListInLogic(map, PersonelView.class));

			if (!admin) {

				for (Iterator iterator = personeller.iterator(); iterator.hasNext();) {
					PersonelView personelView = (PersonelView) iterator.next();
					if (!yetkiTumPersonelNoList.contains(personelView.getSicilNo()))
						iterator.remove();
					else if (personelView.getPdksPersonel() != null)
						personelMap.put(personelView.getId(), personelView.getPdksPersonel());

				}
			}
			devam = !personeller.isEmpty();

			if (devam) {
				perMap.clear();
				for (PersonelView personelView : personeller) {
					perMap.put(personelView.getId(), personelView);
					if (personelView.getPdksPersonel() != null)
						personelMap.put(personelView.getId(), personelView.getPdksPersonel());
					personelId.add(personelView.getId());
				}
			}
			personeller = null;
			map = null;
			// }

		} else {
			devam = Boolean.TRUE;
			parametreMap.clear();
			if (!authenticatedUser.isAdmin() && !ikRole && !authenticatedUser.isGenelMudur()) {
				logger.debug(authenticatedUser.getAdSoyad() + " Personel bilgiler okunuyor.");
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

		parametreMap.clear();
		TreeMap<String, VardiyaGun> vardiyalar = null;

		if (devam) {
			Calendar cal = Calendar.getInstance();
			StringBuffer sb = null;
			if ((personelId == null || personelId.isEmpty()) && (sirketId != null || departmanId != null)) {
				sb = new StringBuffer();
				sb.append("SELECT  V.* FROM " + PdksPersonelView.TABLE_NAME + " V WITH(nolock) ");
				sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + " = V." + PdksPersonelView.COLUMN_NAME_PERSONEL);
				if (!admin) {
					sb.append(" AND  P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " :ys");
					ArrayList<String> list = authenticatedUser.getYetkiTumPersonelNoList();
					parametreMap.put("ys", list);
				}
				if (PdksUtil.hasStringValue(sicilNo)) {
					sb.append(" AND  P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " = :sicilNo");
					parametreMap.put("sicilNo", sicilNo);
				} else if (admin) {
					if (sirketId != null) {
						sb.append(" AND  P." + Personel.COLUMN_NAME_SIRKET + " = :sirketId");
						parametreMap.put("sirketId", sirketId);
					} else if (departmanId != null) {
						sb.append(" INNER JOIN " + Sirket.TABLE_NAME + " S ON S." + Sirket.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_SIRKET);
						sb.append(" AND  S." + Sirket.COLUMN_NAME_DEPARTMAN + " = :departmanId");
						parametreMap.put("departmanId", departmanId);
					}
				}

				perMap.clear();
				personelId.clear();
				List<PdksPersonelView> list = pdksEntityController.getObjectBySQLList(sb, parametreMap, PdksPersonelView.class);
				for (PdksPersonelView pdksPersonelView : list) {
					if (pdksPersonelView.getPdksPersonel() != null)
						personelMap.put(pdksPersonelView.getId(), pdksPersonelView.getPdksPersonel());
					Long long1 = pdksPersonelView.getPersonelKGSId();
					if (long1 != null)
						perMap.put(long1, pdksPersonelView.getPersonelView());
				}
				personelId.addAll(new ArrayList(perMap.keySet()));

			}

			parametreMap.clear();
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

			parametreMap.put("vardiyaBit", ortakIslemler.tariheGunEkleCikar(cal, PdksUtil.getDate(bitTarih), 1));
			parametreMap.put("vardiyaBas", basTarih);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				logger.debug(authenticatedUser.getAdSoyad() + " Hareket bilgileri okunuyor.");
				if (personelId != null && !personelId.isEmpty()) {
					kgsList = ortakIslemler.getHareketBilgileri(new ArrayList<Long>(kapiMap.keySet()), personelId, basTarih, ortakIslemler.tariheGunEkleCikar(cal, PdksUtil.getDate(bitTarih), 1), BasitHareket.class, session);
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
					logger.debug(authenticatedUser.getAdSoyad() + " Kapı bilgileri okunuyor.");
					String fieldName = "id";
					List idList = new ArrayList(kapilarMap.values());
					parametreMap.clear();
					parametreMap.put(fieldName, idList);
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					// List<KapiKGS> list1 = pdksEntityController.getObjectByInnerObjectList(parametreMap, KapiKGS.class);
					List<KapiKGS> list1 = ortakIslemler.getParamList(false, idList, fieldName, parametreMap, KapiKGS.class, session);

					for (KapiKGS kapiKGS : list1)
						kapiMap.put(kapiKGS.getId(), kapiKGS.getKapiView());
					list1 = null;
				}
				if (personelId.isEmpty()) {
					String fieldName = "id";
					List idList = new ArrayList(perMap.keySet());
					logger.debug(authenticatedUser.getAdSoyad() + " Personel bilgileri okunuyor.");
					parametreMap.clear();
					// parametreMap.put(PdksEntityController.MAP_KEY_MAP, "getId");
					parametreMap.put(fieldName, idList);
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					// List<PersonelKGS> list = pdksEntityController.getObjectByInnerObjectList(parametreMap, PersonelKGS.class);
					List<PersonelKGS> list = ortakIslemler.getParamList(false, idList, fieldName, parametreMap, PersonelKGS.class, session);

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
				kgsUpdateGoster = false;

				HashMap<Long, List<HareketKGS>> hareketMap = new HashMap<Long, List<HareketKGS>>();
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
						if (!kgsUpdateGoster)
							kgsUpdateGoster = kgsHareket.getIslemId() != null && kgsHareket.getKgsZaman() != null;
						if (kgsHareket.getPersonel() != null && kgsHareket.getPersonel().getPdksPersonel() != null) {
							Personel personel = kgsHareket.getPersonel().getPdksPersonel();
							personelMap.put(personel.getId(), personel);
							Long key = kgsHareket.getPersonel().getPdksPersonel().getId();
							List<HareketKGS> list2 = hareketMap.containsKey(key) ? hareketMap.get(key) : new ArrayList<HareketKGS>();
							if (list2.isEmpty())
								hareketMap.put(key, list2);
							list2.add(kgsHareket);
						}
						list.add(kgsHareket);
					}
				}

				if (vardiyaOkuDurum != null && vardiyaOkuDurum)
					try {
						vardiyalar = ortakIslemler.getIslemVardiyalar(new ArrayList<Personel>(personelMap.values()), PdksUtil.tariheGunEkleCikar(basTarih, -1), PdksUtil.tariheGunEkleCikar(bitTarih, 1), Boolean.FALSE, session, Boolean.TRUE);
					} catch (Exception e) {
						logger.error(e);
						e.printStackTrace();
					}
				vardiyaOku = false;
				if (vardiyalar != null) {
					for (String string : vardiyalar.keySet()) {
						VardiyaGun vg = vardiyalar.get(string);
						vg.setHareketler(null);
						vg.setGirisHareketleri(null);
						vg.setCikisHareketleri(null);
						Long key = vg.getPdksPersonel().getId();
						if (hareketMap.containsKey(key)) {
							List<HareketKGS> list2 = hareketMap.get(key);
							for (Iterator iterator = list2.iterator(); iterator.hasNext();) {
								HareketKGS hareketKGS = (HareketKGS) iterator.next();
								if (vg.addHareket(hareketKGS, false)) {
									hareketKGS.setVardiyaGun(vg);
									vardiyaOku = true;
								}

							}
						}
					}
				}

				idList = null;
				if (!islemIdler.isEmpty()) {
					logger.debug(authenticatedUser.getAdSoyad() + " Hareket islem bilgileri okunuyor.");
					parametreMap.clear();
					parametreMap.put(PdksEntityController.MAP_KEY_MAP, "getId");
					parametreMap.put("id", islemIdler);
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					TreeMap<Long, PersonelHareketIslem> islemMap = pdksEntityController.getObjectByInnerObjectMap(parametreMap, PersonelHareketIslem.class, Boolean.FALSE);
					String fieldName = "s";
					parametreMap.clear();
					sb = new StringBuffer();
					sb.append("SELECT P.ISLEM_ID,HAREKET_ZAMANI from PDKS_LOG P WITH(nolock) ");
					sb.append(" INNER JOIN PDKS_ISLEM I ON I.ID=P.ISLEM_ID AND I.ISLEM_TIPI='U' ");

					sb.append(" WHERE P.ISLEM_ID :" + fieldName + " AND P.DURUM=0 ");
					parametreMap.put(fieldName, islemIdler);
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					TreeMap<Long, Date> islemTarihMap = new TreeMap<Long, Date>();
					// List<Object[]> islemList = pdksEntityController.getObjectBySQLList(sb, parametreMap, null);
					List<Object[]> islemList = ortakIslemler.getSQLParamList(islemIdler, sb, fieldName, parametreMap, null, session);

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
							if (islem != null && kgsHareket.getKgsZaman() != null)
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
				logger.debug(authenticatedUser.getAdSoyad() + " Hareket bilgileri okundu.");
				islemIdler = null;
				setHareketList(list);
			} else
				setHareketList(new ArrayList<HareketKGS>());

		} else
			setHareketList(new ArrayList<HareketKGS>());
		setZipVeri(null);
		if (!hareketList.isEmpty() && PdksUtil.hasStringValue(sicilNo) == false && (authenticatedUser.isAdmin() || ikRole)) {

			String dosyaAdi = "tumHareketler" + (authenticatedUser.getShortUsername() != null ? authenticatedUser.getShortUsername().trim() : "");
			try {
				logger.debug(authenticatedUser.getAdSoyad() + " " + dosyaAdi + " dosyasi olusturuluyor.");
				if (authenticatedUser.isIK() && !PdksUtil.getTestDurum()) {
					if (hareketList.size() > 10000) {
						ByteArrayOutputStream baos = excelDevam();
						if (baos != null)
							zipVeri = PdksUtil.getFileZip(dosyaAdi + ".xlsx", baos.toByteArray());
					}
					logger.debug(authenticatedUser.getAdSoyad() + " " + dosyaAdi + " dosyasi mail gonderiliyor.");
					if (zipVeri != null) {
						// ortakIslemler.mailGonder(renderer, "/email/hareketMail.xhtml");
						try {
							MailObject mail = new MailObject();
							mail.setSubject("Kapı hareketleri zip");
							String body = "<p>" + authenticatedUser.getTarihFormatla(basTarih, PdksUtil.getDateFormat()) + "-" + authenticatedUser.getTarihFormatla(bitTarih, PdksUtil.getDateFormat()) + " kapı giriş çıkış 	hareketleri ektedir.</p>";
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
				e.printStackTrace();
			}

		}
	}

	public String textDosyaAktar() {
		String dosyaAdi = "tumHareketler" + (authenticatedUser.getShortUsername() != null ? authenticatedUser.getShortUsername().trim() : "");
		try {
			if (zipVeri == null) {
				String zipDosyaAdi = "tumHareketler" + (authenticatedUser.getShortUsername() != null ? authenticatedUser.getShortUsername().trim() : "");
				zipVeri = textZipDosyaOlustur(zipDosyaAdi + ".txt");
			}
			byte[] bytes = zipVeri;
			if (bytes != null) {
				HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
				ServletOutputStream sos = response.getOutputStream();
				response.setContentType("application/zip");
				response.setHeader("Expires", "0");
				response.setHeader("Pragma", "cache");
				response.setHeader("Cache-Control", "cache");
				response.setHeader("Content-Disposition", "attachment;filename=" + PdksUtil.encoderURL(dosyaAdi, "UTF-8") + ".zip");
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
	 * @param dosyaAdi
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private byte[] textZipDosyaOlustur(String dosyaAdi) throws Exception {
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
					if (hareket.getKgsZaman() != null && hareket.getIslem() != null) {
						sb.append((hareket.getOrjinalZaman() != null ? PdksUtil.convertToDateString(hareket.getKgsZaman(), "dd/MM/yyyy HH:mm") : "") + "|");
						sb.append((hareket.getIslem() != null ? hareket.getIslem().getGuncelleyenUser().getAdSoyad() : "") + "|");
					} else {
						sb.append("||");
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
		byte[] bytesZip = PdksUtil.getFileZip(dosyaAdi, baos.toByteArray());
		return bytesZip;
	}

	public String zipAktar() {
		try {
			logger.debug(authenticatedUser.getAdSoyad() + " Hareket excel dosyası hazırlanıyor.");
			ByteArrayOutputStream baos = excelDevam();
			if (baos != null) {
				logger.debug(authenticatedUser.getAdSoyad() + " Hareket zip dosyası hazırlanıyor.");
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
				logger.debug(authenticatedUser.getAdSoyad() + " Hareket zip dosyası kayıt ediliyor.");
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
					logger.debug(authenticatedUser.getAdSoyad() + " Hareket zip dosyası mail olarak gönderildi.");

				} catch (Exception e) {
					logger.error("Onay Akis Hata in  : ");
					e.printStackTrace();
					logger.error("Onay Akis Hata out  : " + e.getMessage());

				}
				if (mailSatu != null && mailSatu.getDurum())
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
				response.setHeader("Content-Disposition", "attachment;filename=" + PdksUtil.encoderURL("tumHareketler.zip", "UTF-8"));

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
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddDate = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATE, wb);
		CellStyle styleOddTime = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TIME, wb);
		CellStyle styleOddTimeStamp = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATETIME, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenDate = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATE, wb);
		CellStyle styleEvenTime = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TIME, wb);
		CellStyle styleEvenTimeStamp = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATETIME, wb);
		int row = 0;
		int col = 0;
		boolean admin = authenticatedUser.isAdmin();
		boolean yonetici = admin || ikRole;
		if (admin)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Id");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Tarih");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Saat");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Kapi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Kapi Tipi");
		if (vardiyaOku) {
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Vardiya Tarihi");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Vardiya Adı");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Vardiya Başlama Zamanı");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Vardiya Bitiş Zamanı");
		}
		if (yonetici)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("KGS Şirket");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);

		if (guncellenmis) {
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Orjinal Zamanı");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İşlem Yapan");

		}
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Oluşturulma Zamanı");
		boolean renk = true;
		for (Iterator iter = hareketList.iterator(); iter.hasNext();) {
			HareketKGS hareket = (HareketKGS) iter.next();
			CellStyle styleCenter = null, style = null, styleTimeStamp = null, styleTime = null, styleDate = null;
			if (renk) {
				style = styleOdd;
				styleCenter = styleOddCenter;
				styleTimeStamp = styleOddTimeStamp;
				styleTime = styleOddTime;
				styleDate = styleOddDate;
			} else {
				style = styleEven;
				styleCenter = styleEvenCenter;
				styleTimeStamp = styleEvenTimeStamp;
				styleTime = styleEvenTime;
				styleDate = styleEvenDate;
			}
			renk = !renk;
			row++;
			col = 0;
			Personel personel = null;
			try {
				personel = hareket.getPersonel().getPdksPersonel();
			} catch (Exception ex) {
				logger.error(ex);
				ex.printStackTrace();

			}
			if (admin)
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(hareket.getId());
			try {
				ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(hareket.getZaman());
				ExcelUtil.getCell(sheet, row, col++, styleTime).setCellValue(hareket.getZaman());
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
				if (hareket.getKapiView().getKapi() != null)
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(hareket.getKapiView().getKapi().getTipi().getAciklama());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				if (vardiyaOku) {
					VardiyaGun vg = hareket.getVardiyaGun();
					if (vg != null && vg.getIslemVardiya() != null) {
						Vardiya vardiya = vg.getIslemVardiya();
						ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(vg.getVardiyaDate());
						ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(vardiya.getKisaAdi());
						if (vardiya.isCalisma()) {
							ExcelUtil.getCell(sheet, row, col++, styleTimeStamp).setCellValue(vardiya.getVardiyaBasZaman());
							ExcelUtil.getCell(sheet, row, col++, styleTimeStamp).setCellValue(vardiya.getVardiyaBitZaman());
						} else {
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
						}
					} else {
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
					}
				}
				if (yonetici)
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(hareket.getKapiSirket() != null ? hareket.getKapiSirket().getAciklama() : "");

				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel != null && personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
				if (guncellenmis) {
					if (hareket.getKgsZaman() != null && hareket.getIslem() != null) {
						if (hareket.getKgsZaman() != null)
							ExcelUtil.getCell(sheet, row, col++, styleTimeStamp).setCellValue(hareket.getKgsZaman());
						else
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue(hareket.getIslem() != null ? hareket.getIslem().getGuncelleyenUser().getAdSoyad() : "");
					} else {
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
					}

				}
				ExcelUtil.getCell(sheet, row, col++, styleTimeStamp).setCellValue(hareket.getOlusturmaZamani());
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

	public void fillKapiList() {
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

	public boolean isKgsUpdateGoster() {
		return kgsUpdateGoster;
	}

	public void setKgsUpdateGoster(boolean kgsUpdateGoster) {
		this.kgsUpdateGoster = kgsUpdateGoster;
	}

	public boolean isIkRole() {
		return ikRole;
	}

	public void setIkRole(boolean ikRole) {
		this.ikRole = ikRole;
	}

	public boolean isVardiyaOku() {
		return vardiyaOku;
	}

	public void setVardiyaOku(boolean vardiyaOku) {
		this.vardiyaOku = vardiyaOku;
	}

	public Boolean getVardiyaOkuDurum() {
		return vardiyaOkuDurum;
	}

	public void setVardiyaOkuDurum(Boolean vardiyaOkuDurum) {
		this.vardiyaOkuDurum = vardiyaOkuDurum;
	}
}
