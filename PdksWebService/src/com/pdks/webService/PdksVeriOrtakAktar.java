package com.pdks.webService;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.pdks.dao.PdksDAO;
import com.pdks.dao.impl.BaseDAOHibernate;
import com.pdks.entity.BordroIzinGrubu;
import com.pdks.entity.CalismaModeli;
import com.pdks.entity.DenklestirmeAy;
import com.pdks.entity.Departman;
import com.pdks.entity.ERPPersonel;
import com.pdks.entity.IzinReferansERP;
import com.pdks.entity.IzinTipi;
import com.pdks.entity.KapiSirket;
import com.pdks.entity.Parameter;
import com.pdks.entity.Personel;
import com.pdks.entity.PersonelDenklestirme;
import com.pdks.entity.PersonelDinamikAlan;
import com.pdks.entity.PersonelIzin;
import com.pdks.entity.PersonelIzinDetay;
import com.pdks.entity.PersonelKGS;
import com.pdks.entity.PersonelMesai;
import com.pdks.entity.Role;
import com.pdks.entity.ServiceData;
import com.pdks.entity.Sirket;
import com.pdks.entity.Tanim;
import com.pdks.entity.User;
import com.pdks.entity.UserRoles;
import com.pdks.entity.Vardiya;
import com.pdks.entity.VardiyaGun;
import com.pdks.entity.VardiyaSablonu;
import com.pdks.genel.model.Constants;
import com.pdks.genel.model.MailManager;
import com.pdks.genel.model.PdksUtil;
import com.pdks.mail.model.MailFile;
import com.pdks.mail.model.MailObject;
import com.pdks.mail.model.MailPersonel;
import com.pdks.mail.model.MailStatu;

public class PdksVeriOrtakAktar implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3593586032361511570L;

	public Logger logger = Logger.getLogger(PdksVeriOrtakAktar.class);

	public static final String FORMAT_DATE = "yyyy-MM-dd";
	public static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm";
	public static final String FORMAT_TIME = "HH:mm";
	private static final String HELP_DESK_STATUS = "helpDeskStatus";
	public static final String[] HAKEDIS_IZIN_PROP_ORDER = { "hakedisList", "kidemBaslangicTarihi", "personelNo" };
	public static final String[] IZIN_PROP_ORDER = { "aciklama", "basZaman", "bitZaman", "durum", "izinSuresi", "izinTipi", "izinTipiAciklama", "personelNo", "referansNoERP", "sureBirimi" };
	public static final String[] PERSONEL_PROP_ORDER = { "adi", "bolumAdi", "bolumKodu", "bordroAltAlanAdi", "bordroAltAlanKodu", "cinsiyetKodu", "cinsiyeti", "departmanAdi", "departmanKodu", "dogumTarihi", "gorevKodu", "gorevi", "iseGirisTarihi", "istenAyrilmaTarihi", "kidemTarihi",
			"masrafYeriAdi", "masrafYeriKodu", "personelNo", "personelTipi", "personelTipiKodu", "sanalPersonel", "sirketAdi", "sirketKodu", "soyadi", "tesisAdi", "tesisKodu", "yoneticiPerNo", "grubaGirisTarihi", "yonetici2PerNo" };

	private static final String LAST_DATE = "9999-12-31";

	private PdksDAO pdksDAO = null;

	private HashMap fields = null;

	private HashMap<String, Object> mailMap = null;

	private User islemYapan = null;

	private VardiyaSablonu vardiyaSablonu = null, isKurVardiyaSablonu = null;

	private CalismaModeli calismaModeli = null;

	private KapiSirket kapiSirket = null;

	private String mesaj = null, dosyaEkAdi, parentBordroTanimKoduStr = "", kapiGiris, uygulamaBordro, ekSahaAdi = "", servisAdi;

	private Date bugun = null, ayBasi = null, minDate = null;

	private Integer sicilNoUzunluk = null;

	private ServiceData serviceData = null;

	private boolean sistemDestekVar = false, izinGirisiVar = false, departmanYoneticiRolVar = false, yoneticiRolVarmi = false, updateYonetici2, altBolumDurum = false;

	private Tanim bosDepartman, ikinciYoneticiOlmaz;

	private TreeMap<String, Tanim> genelTanimMap;

	private List<Long> yoneticiIdList = null;

	public PdksVeriOrtakAktar() {
		super();
		pdksDAO = Constants.pdksDAO;
		yoneticiRolVarmi = false;
		sicilNoUzunluk = null;
		if (pdksDAO != null) {
			bugun = new Date();
			fields = new HashMap();
			serviceData = null;
		}

	}

	/**
	 * @param fonksiyonAdi
	 * @param bilgi
	 * @param list
	 */
	protected void mailBosGonder(String fonksiyonAdi, String bilgi, List list) {
		if (PdksUtil.isSistemDestekVar()) {
			if (list != null && list.size() > 1 && PdksUtil.hasStringValue(fonksiyonAdi) && PdksUtil.hasStringValue(bilgi)) {
				if (PdksUtil.hasStringValue(uygulamaBordro) == false)
					uygulamaBordro = mailMap.containsKey("uygulamaBordro") ? (String) mailMap.get("uygulamaBordro") : "Bordro Uygulaması ";
				MailObject mailObject = new MailObject();
				String subject = uygulamaBordro + " " + fonksiyonAdi + " fonksiyon " + bilgi + " güncellemesi";
				String body = subject + " " + list.size() + " adet kayıt için başarılı olarak tamamlandı.";
				mailObject.setSubject(subject);
				mailObject.setBody(body);
				try {
					mailAdresKontrol(mailObject, null);
					mailObject.getToList().clear();
					mailMap.put("mailObject", mailObject);
					MailManager.ePostaGonder(mailMap);
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}

			}
		}

	}

	/**
	 * @param kodu
	 * @return
	 */
	private List<Personel> saveIkinciYoneticiOlmazList(String kodu) {
		List<Personel> list = null;
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("yoneticiId", 0L);
		map.put("tipi", kodu);
		map.put(BaseDAOHibernate.MAP_KEY_SELECT, "SP_IKINCI_YONETICI_OLAMAZ");
		try {
			list = pdksDAO.execSPList(map, Personel.class);
			if (!list.isEmpty()) {
				Date guncellemeTarihi = new Date();
				for (Personel personel2 : list) {
					personel2.setAsilYonetici2(personel2.getYoneticisi());
					personel2.setGuncellemeTarihi(guncellemeTarihi);
					personel2.setGuncelleyenUser(islemYapan);
				}
				pdksDAO.saveObjectList(list);
			}

		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		if (list == null)
			list = new ArrayList<Personel>();
		return list;
	}

	/**
	 * @param dao
	 */
	private void personelKontrolVerileriAyarla(PdksDAO dao) {
		izinGirisiVar = !sistemDestekVar;
		departmanYoneticiRolVar = !sistemDestekVar;
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("WITH BUGUN AS ( ");
		sb.append("		select 1 AS ID ");
		sb.append("	),");
		sb.append("	DEP_YONETICI AS (");
		sb.append("		SELECT R.ROLENAME DEP_YONETICI_ROL_ADI FROM " + Role.TABLE_NAME + " R WITH(nolock)");
		sb.append("		WHERE R." + Role.COLUMN_NAME_ROLE_NAME + "='" + Role.TIPI_DEPARTMAN_SUPER_VISOR + "' AND R." + Role.COLUMN_NAME_STATUS + "=1");
		sb.append("	),");
		sb.append("	IZIN_DURUM AS (");
		sb.append("		SELECT COUNT(I.ID) AS IZIN_TIPI_ADET FROM " + IzinTipi.TABLE_NAME + " I WITH(nolock)");
		sb.append("			INNER JOIN " + Departman.TABLE_NAME + " D ON D." + Departman.COLUMN_NAME_ID + "=I." + IzinTipi.COLUMN_NAME_DEPARTMAN + " AND D." + Departman.COLUMN_NAME_ADMIN_DURUM + "=1 AND D." + Departman.COLUMN_NAME_DURUM + "=1");
		sb.append("		WHERE I." + IzinTipi.COLUMN_NAME_DURUM + "=1  AND I." + IzinTipi.COLUMN_NAME_BAKIYE_IZIN_TIPI + " IS NULL AND I." + IzinTipi.COLUMN_NAME_GIRIS_TIPI + "<>'" + IzinTipi.GIRIS_TIPI_YOK + "'");
		sb.append("	)");
		sb.append("	SELECT COALESCE(DY.DEP_YONETICI_ROL_ADI,'') DEP_YONETICI_ROL_ADI,");
		sb.append("		COALESCE(ID.IZIN_TIPI_ADET,0) IZIN_TIPI_ADET, GETDATE() AS TARIH FROM BUGUN B ");
		sb.append("	LEFT JOIN DEP_YONETICI DY ON 1=1");
		sb.append("	LEFT JOIN IZIN_DURUM ID ON 1=1");
		List<Object[]> veriList = dao.getNativeSQLList(fields, sb, null);
		if (!veriList.isEmpty()) {
			Object[] veri = veriList.get(0);
			departmanYoneticiRolVar = ((String) veri[0]).length() > 0;
			izinGirisiVar = ((Integer) veri[1]).intValue() > 0;
		}
		veriList = null;
		List<String> roleList = null;
		if (mailMap.containsKey("yoneticiRolleri")) {
			String yoneticiRolleri = (String) mailMap.get("yoneticiRolleri");
			if (PdksUtil.hasStringValue(yoneticiRolleri))
				roleList = PdksUtil.getListByString(yoneticiRolleri, null);
		}
		if (roleList == null || roleList.isEmpty())
			roleList = Arrays.asList(new String[] { Role.TIPI_GENEL_MUDUR, Role.TIPI_YONETICI, Role.TIPI_YONETICI_KONTRATLI });
		sb = new StringBuffer();
		sb.append("SELECT R." + Role.COLUMN_NAME_ROLE_NAME + " FROM " + Role.TABLE_NAME + " R WITH(nolock) ");
		sb.append("	WHERE R." + Role.COLUMN_NAME_STATUS + "=1 AND R.ADMIN_ROLE<>1 AND R." + Role.COLUMN_NAME_ROLE_NAME + " :r");
		fields.put("r", roleList);
		veriList = dao.getNativeSQLList(fields, sb, null);
		yoneticiRolVarmi = !veriList.isEmpty();
		veriList = null;
	}

	/**
	 * @param mailDataMap
	 * @param dao
	 * @return
	 */
	public MailObject kullaniciIKYukle(HashMap<String, Object> mailDataMap, PdksDAO dao) {
		MailObject mailObject = new MailObject();
		List<User> userList = null;
		StringBuffer sb = new StringBuffer();
		if (mailMap == null)
			mailMap = mailDataMap;
		if (!mailDataMap.containsKey("ikMailIptal")) {
			if (PdksUtil.getCanliSunucuDurum()) {
				HashMap fields = new HashMap();
				sb.append("SELECT  U.* FROM " + Role.TABLE_NAME + " R WITH(nolock)");
				sb.append(" INNER JOIN " + UserRoles.TABLE_NAME + " UR ON UR." + UserRoles.COLUMN_NAME_ROLE + "=R.ID ");
				sb.append(" INNER JOIN " + User.TABLE_NAME + " U ON U.ID=UR." + UserRoles.COLUMN_NAME_USER + " AND U.DURUM=1 ");
				sb.append(" INNER JOIN " + Departman.TABLE_NAME + " D ON D.ID=U." + User.COLUMN_NAME_DEPARTMAN + " AND D.ADMIN_DURUM=1 AND D.DURUM=1 ");
				sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P.ID=U." + User.COLUMN_NAME_PERSONEL + " AND P.DURUM=1 AND P." + Personel.COLUMN_NAME_ISTEN_AYRILIS_TARIHI + ">GETDATE() ");
				sb.append(" WHERE R." + Role.COLUMN_NAME_ROLE_NAME + "=:r ");
				fields.put("r", Role.TIPI_IK);
				userList = dao.getNativeSQLList(fields, sb, User.class);
				if (userList != null && !userList.isEmpty()) {
					userList = PdksUtil.sortObjectStringAlanList(userList, "getAdSoyad", null);
					for (Iterator iterator = userList.iterator(); iterator.hasNext();) {
						User user = (User) iterator.next();
						MailPersonel mailPersonel = new MailPersonel();
						mailPersonel.setAdiSoyadi(user.getAdSoyad());
						mailPersonel.setePosta(user.getEmail());
						mailObject.getToList().add(mailPersonel);
					}
				}
			}
		} else
			testMailObject(mailObject);

		if (mailDataMap.containsKey("konu"))
			mailObject.setSubject((String) mailDataMap.get("konu"));
		if (mailDataMap.containsKey("mailIcerik"))
			mailObject.setBody((String) mailDataMap.get("mailIcerik"));

		if (mailDataMap.containsKey("fileMap")) {
			LinkedHashMap<String, Object> fileMap = (LinkedHashMap<String, Object>) mailDataMap.get("fileMap");
			for (String fileName : fileMap.keySet()) {
				MailFile mailFile = new MailFile();
				mailFile.setDisplayName(fileName);
				String str = (String) fileMap.get(fileName);
				mailFile.setIcerik(PdksUtil.getBytesUTF8(str));
				mailObject.getAttachmentFiles().add(mailFile);
			}
		}
		sb = new StringBuffer();
		try {
			mailAdresKontrol(mailObject, sb);
		} catch (Exception e) {
			logger.error(e);
		}
		sb = null;

		mailDataMap.put("mailObject", mailObject);
		return mailObject;
	}

	/**
	 * @param sirket
	 * @return
	 */
	protected TreeMap<String, Boolean> getOnaysizIzinDurumMap(Sirket sirket) {
		TreeMap<String, Boolean> dataMap = new TreeMap<String, Boolean>();
		boolean onaysizIzinSec = false, onaysizIzinDurum = false, ikinciYoneticiIzinOnayla = false;
		try {
			long sirketId = sirket != null ? sirket.getIdLong() : 0L;
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
			map.put(BaseDAOHibernate.MAP_KEY_SELECT, "SP_SIRKET_IZIN_ONAY_BILGI");
			map.put("sirketId", sirketId);
			List<Object[]> list = pdksDAO.execSPList(map, null);
			if (list.size() == 1) {
				Object[] dizi = list.get(0);
				onaysizIzinSec = ((Integer) dizi[1]) > 0;
				ikinciYoneticiIzinOnayla = ((Integer) dizi[2]) > 0;
				onaysizIzinDurum = ((Integer) dizi[3]) == 1;
			}

		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		if (onaysizIzinSec)
			dataMap.put("onaysizIzinSec", onaysizIzinSec);
		if (onaysizIzinDurum)
			dataMap.put("onaysizIzinDurum", onaysizIzinDurum);
		if (ikinciYoneticiIzinOnayla)
			dataMap.put("ikinciYoneticiIzinOnaySec", ikinciYoneticiIzinOnayla);
		return dataMap;

	}

	/**
	 * @param dao
	 */
	public void sistemVerileriniYukle(PdksDAO dao) {
		if (dao != null) {
			mesaj = null;
			fields.clear();
			// fields.put("active", Boolean.TRUE);
			List<Parameter> list = dao.getObjectByInnerObjectList(fields, Parameter.class);
			if (mailMap == null)
				mailMap = new HashMap<String, Object>();
			else
				mailMap.clear();
			sistemDestekVar = false;
			HashMap<String, Parameter> pmMap = new HashMap<String, Parameter>();
			List<String> helpDeskList = new ArrayList<String>();
			for (Parameter parameter : list) {
				String key = parameter.getName().trim(), deger = parameter.getValue().trim();
				pmMap.put(key, parameter);
				if (parameter.getActive() != null && parameter.getActive()) {
					mailMap.put(key, deger);
					if (parameter.isHelpDeskMi())
						helpDeskList.add(key);
				}

			}
			String testSunucu = "srvglftest";
			if (mailMap.containsKey("testSunucu"))
				testSunucu = (String) mailMap.get("testSunucu");
			PdksUtil.setTestSunucu(testSunucu);
			String canliSunucu = "srvglf";
			if (mailMap.containsKey("canliSunucu"))
				canliSunucu = (String) mailMap.get("canliSunucu");
			PdksUtil.setCanliSunucu(canliSunucu);
			String dateFormat = null;
			if (mailMap.containsKey("dateFormat")) {
				String str = null;
				try {
					str = PdksUtil.convertToDateString(new Date(), (String) mailMap.get("dateFormat"));
					if (str != null && str.length() > 1)
						dateFormat = (String) mailMap.get("dateFormat");
				} catch (Exception e) {
				}
			}
			kapiGiris = mailMap.containsKey("kapiGirisUygulama") ? (String) mailMap.get("kapiGirisUygulama") : "kapı giriş";
			kapiSirket = null;
			Date bugun = PdksUtil.getDate(new Date());
			String birdenFazlaKGSSirketSQL = mailMap.containsKey("birdenFazlaKGSSirketSQL") ? (String) mailMap.get("birdenFazlaKGSSirketSQL") : "";
			if (PdksUtil.hasStringValue(birdenFazlaKGSSirketSQL)) {
				HashMap map = new HashMap();
				map.put("id>", 0L);
				map.put("basTarih<=", bugun);
				map.put("bitTarih>=", bugun);
				List<KapiSirket> kapiSirketList = pdksDAO.getObjectByInnerObjectListInLogic(map, KapiSirket.class);
				if (kapiSirketList.size() == 1) {
					kapiSirket = kapiSirketList.get(0);
					kapiGiris = kapiSirket.getAciklama();
				}
				kapiSirketList = null;

			}
			uygulamaBordro = mailMap.containsKey("uygulamaBordro") ? (String) mailMap.get("uygulamaBordro") : "Bordro Uygulaması ";
			if (dateFormat == null)
				dateFormat = "dd/MM/yyyy";
			PdksUtil.setDateFormat(dateFormat);
			if (mailMap.containsKey("helpDeskLastDate")) {
				String helpDeskLastDateStr = (String) mailMap.get("helpDeskLastDate");
				Date helpDeskLastDate = PdksUtil.getDateFromString(helpDeskLastDateStr);
				if (helpDeskLastDate == null)
					helpDeskLastDate = PdksUtil.getDateFromString(PdksUtil.getDecodeStringByBase64(helpDeskLastDateStr));
				if (helpDeskLastDate != null)
					sistemDestekVar = PdksUtil.tarihKarsilastirNumeric(new Date(), helpDeskLastDate) != 1;
			}
			if (!sistemDestekVar) {
				for (Iterator iterator = helpDeskList.iterator(); iterator.hasNext();) {
					String key = (String) iterator.next();
					mailMap.remove(key);
				}
			}
			helpDeskList = null;
			PdksUtil.setSistemBaslangicYili(mailMap.containsKey("sistemBaslangicYili") ? Integer.parseInt((String) mailMap.get("sistemBaslangicYili")) : 2010);
			PdksUtil.setSistemDestekVar(sistemDestekVar);
			String sistemAdminUserName = mailMap.containsKey("sistemAdminUserName") ? (String) mailMap.get("sistemAdminUserName") : null;
			if (sistemAdminUserName != null)
				islemYapan = (User) dao.getObjectByInnerObject("username", sistemAdminUserName, User.class);
			if (islemYapan == null)
				islemYapan = (User) dao.getObjectByInnerObject("id", 1L, User.class);
			setHelpDeskParametre(pmMap, dao);
			pmMap = null;

		}

	}

	/**
	 * @param pmMap
	 * @param dao
	 */
	private void setHelpDeskParametre(HashMap<String, Parameter> pmMap, PdksDAO dao) {
		Parameter helpDeskStatus = pmMap != null && pmMap.containsKey(HELP_DESK_STATUS) ? pmMap.get(HELP_DESK_STATUS) : new Parameter();
		Date bugun = new Date();
		if (helpDeskStatus.getId() == null) {
			Date changeDate = null;
			try {
				changeDate = PdksUtil.convertToJavaDate(PdksUtil.getSistemBaslangicYili() + "0101", "yyyyMMdd");
			} catch (Exception e) {
			}
			helpDeskStatus.setChangeDate(changeDate != null ? changeDate : bugun);
			helpDeskStatus.setChangeUser(getSistemAdminUserByParamMap(dao));
			helpDeskStatus.setVersion(0);
			helpDeskStatus.setDescription("Sistem Desktek Durumu");
			helpDeskStatus.setName(HELP_DESK_STATUS);
			helpDeskStatus.setGuncelle(false);
			helpDeskStatus.setHelpDesk(Boolean.TRUE);
		}
		Boolean durum = PdksUtil.isSistemDestekVar();
		boolean degisti = helpDeskStatus.getId() == null || !helpDeskStatus.getActive().equals(durum);
		helpDeskStatus.setActive(durum);
		helpDeskStatus.setValue(durum ? "1" : "" + (helpDeskStatus.getId() != null ? -helpDeskStatus.getId() : 0));
		if (degisti) {
			if (helpDeskStatus.getId() != null)
				helpDeskStatus.setChangeDate(bugun);
			dao.saveObject(helpDeskStatus);
		}
	}

	/**
	 * @param dao
	 * @return
	 */
	private User getSistemAdminUserByParamMap(PdksDAO dao) {
		User user = null;

		if (mailMap != null && mailMap.containsKey("sistemAdminUserName")) {
			String sistemAdminUserName = (String) mailMap.get("sistemAdminUserName");
			if (PdksUtil.hasStringValue(sistemAdminUserName)) {
				fields.put("username", sistemAdminUserName);
				user = (User) dao.getObjectByInnerObject(fields, User.class);
			}
		}
		if (user == null) {
			if (!fields.isEmpty())
				fields.clear();
			fields.put("id", 1L);
			user = (User) dao.getObjectByInnerObject(fields, User.class);
		}
		fields = null;

		return user;

	}

	/**
	 * @param fonksiyonAdi
	 * @param object
	 * @throws Exception
	 */
	private void saveFonksiyonVeri(String fonksiyonAdi, Object object) throws Exception {
		Gson gson = new Gson();
		String dataStr = object != null ? gson.toJson(object) : "";
		if (PdksUtil.hasStringValue(dataStr)) {
			if (serviceData == null) {
				if (fonksiyonAdi != null) {
					serviceData = new ServiceData(fonksiyonAdi);
					serviceData.setInputData(dataStr);
				}
			} else if (serviceData.getId() != null)
				serviceData.setOutputData(dataStr);
			try {
				// if (serviceData != null)
				// pdksDAO.saveObject(serviceData);
			} catch (Exception e) {
			}
		}

	}

	/**
	 * @param list
	 * @param mailMap
	 * @throws Exception
	 */
	private void mailListKontrol(List<MailPersonel> list, HashMap<String, MailPersonel> dataMap) throws Exception {
		if (list != null && mailMap != null) {
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				MailPersonel mailPersonel = (MailPersonel) iterator.next();
				if (dataMap.containsKey(mailPersonel.getePosta())) {
					iterator.remove();
				} else
					dataMap.put(mailPersonel.getePosta(), mailPersonel);
			}

		}

	}

	/**
	 * @param list
	 * @param userMap
	 * @throws Exception
	 */
	private void mailUserListKontrol(List<MailPersonel> list, TreeMap<String, User> userMap) throws Exception {
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			MailPersonel mailPersonel = (MailPersonel) iterator.next();
			if (userMap.containsKey(mailPersonel.getePosta()))
				mailPersonel.setAdiSoyadi(userMap.get(mailPersonel.getePosta()).getAdSoyad());

		}
	}

	/**
	 * @param list
	 * @param pasifList
	 * @param sb
	 * @throws Exception
	 */
	private void pasifListKontrol(List<MailPersonel> list, List<String> pasifList, StringBuffer sb) throws Exception {
		if (sb != null && list != null && pasifList != null) {
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				MailPersonel mailPersonel = (MailPersonel) iterator.next();
				if (pasifList.contains(mailPersonel.getePosta())) {
					if (sb.length() > 0)
						sb.append(", ");
					sb.append((PdksUtil.hasStringValue(mailPersonel.getAdiSoyadi()) ? "<" + mailPersonel.getAdiSoyadi().trim() + "> " : "") + mailPersonel.getePosta());
					iterator.remove();
				}
			}
		}
	}

	/**
	 * @param mailObject
	 * @return
	 * @throws Exception
	 */
	public MailStatu sendMail(MailObject mailObject) throws Exception {
		MailStatu mailStatu = new MailStatu();
		if (mailObject != null) {
			String subject = mailObject.getSubject() != null ? PdksUtil.setTurkishStr(mailObject.getSubject()) : null;
			if (subject != null)
				logger.info(subject + " in " + new Date());
			StringBuffer sb = new StringBuffer();
			if (!PdksUtil.hasStringValue(mailObject.getSmtpUser()))
				sb.append("Mail user belirtiniz!");
			if (PdksUtil.hasStringValue(mailObject.getSmtpPassword()))
				sb.append("Mail şifre belirtiniz!");

			if (PdksUtil.hasStringValue(mailObject.getSubject()))
				sb.append("Konu belirtiniz!");
			if (sb.length() > 0)
				mailStatu.setHataMesai(sb.toString());
			else {
				sistemVerileriniYukle(pdksDAO);
				StringBuffer pasifPersonelSB = new StringBuffer();
				String smtpUserName = mailMap.containsKey("smtpUserName") ? (String) mailMap.get("smtpUserName") : "";
				String smtpPassword = mailMap.containsKey("smtpPassword") ? (String) mailMap.get("smtpPassword") : "";
				if (mailObject.getSmtpUser().equals(smtpUserName) && mailObject.getSmtpPassword().equals(smtpPassword)) {
					mailMap.put("mailObject", mailObject);

					mailAdresKontrol(mailObject, pasifPersonelSB);
					String body = mailObject.getBody();
					if (mailObject.getToList().size() == 1) {
						MailPersonel mailPersonel = mailObject.getToList().get(0);
						if (!body.contains(mailPersonel.getAdiSoyadi())) {
							body = "<P>Sayın " + mailPersonel.getAdiSoyadi() + ",</P>" + body;
							mailObject.setBody(body);
						}
					}
					if (!body.contains("Saygılarımla")) {
						body = body + "<P>Saygılarımla</P>";
						mailObject.setBody(body);
					}

					if (mailObject.getBccList().size() + mailObject.getCcList().size() + mailObject.getToList().size() > 0) {
						MailManager.ePostaGonder(mailMap);
						mailStatu.setDurum(Boolean.TRUE);
						mailStatu.setHataMesai(pasifPersonelSB.toString());
					} else {
						mailStatu.setHataMesai("Adres giriniz!");
					}

				} else {
					mailStatu.setHataMesai("Smtp bilgileri hatalıdır!");
				}
				pasifPersonelSB = null;
			}
			sb = null;

			if (subject != null)
				logger.info(subject + " out " + new Date());
		} else
			mailStatu.setHataMesai("Boş veri geldi!");
		return mailStatu;

	}

	/**
	 * @param mailObject
	 * @param bccAdresName
	 */
	private void addMailAdresBCC(MailObject mailObject, String bccAdresName) {
		if (mailObject != null && mailMap.containsKey(bccAdresName)) {
			String bccAdres = (String) mailMap.get(bccAdresName);
			if (bccAdres.indexOf("@") > 1) {
				List<String> list = PdksUtil.getListByString(bccAdres, null);
				for (String email : list) {
					if (email.indexOf("@") > 1 && PdksUtil.isValidEmail(email)) {
						MailPersonel mailPersonel = new MailPersonel();
						mailPersonel.setePosta(email);
						mailObject.getBccList().add(mailPersonel);
					}

				}
			}
		}
	}

	/**
	 * @param mailObject
	 * @param pasifPersonelSB
	 * @throws Exception
	 */
	private void mailAdresKontrol(MailObject mailObject, StringBuffer pasifPersonelSB) throws Exception {
		if (PdksUtil.isSistemDestekVar()) {
			addMailAdresBCC(mailObject, "bccAdres");
			addMailAdresBCC(mailObject, "bccEntegrasyonAdres");
		}
		addMailAdresBCC(mailObject, "bccAdres");
		addMailAdresBCC(mailObject, "bccEntegrasyonAdres");
		HashMap<String, MailPersonel> mailDataMap = new HashMap<String, MailPersonel>();
		mailListKontrol(mailObject.getToList(), mailDataMap);
		mailListKontrol(mailObject.getCcList(), mailDataMap);
		mailListKontrol(mailObject.getBccList(), mailDataMap);
		if (!mailDataMap.isEmpty()) {
			List<String> list = new ArrayList<String>();
			for (String string : mailDataMap.keySet()) {
				// if (mailDataMap.size() > 1)
				// list.add("'" + string + "'");
				// else
				list.add(string);
			}
			HashMap map = new HashMap();
			map.put("email", list.size() > 1 ? list : list.get(0));
			TreeMap<String, User> userMap = new TreeMap<String, User>();
			List<User> userList = pdksDAO.getObjectByInnerObjectList(map, User.class);
			List<String> pasifList = new ArrayList<String>();
			for (User user : userList) {
				if (user.isDurum() && user.getPdksPersonel().isCalisiyor())
					userMap.put(user.getEmail(), user);
				else
					pasifList.add(user.getEmail());
			}
			if (!userMap.isEmpty()) {
				mailUserListKontrol(mailObject.getToList(), userMap);
				mailUserListKontrol(mailObject.getCcList(), userMap);
				mailUserListKontrol(mailObject.getBccList(), userMap);
			}
			if (!pasifList.isEmpty()) {
				pasifListKontrol(mailObject.getToList(), pasifList, pasifPersonelSB);
				pasifListKontrol(mailObject.getCcList(), pasifList, pasifPersonelSB);
				pasifListKontrol(mailObject.getBccList(), pasifList, pasifPersonelSB);

			}
			userList = null;
			list = null;
			userMap = null;
		}
	}

	/**
	 * @param sirketKodu
	 * @param personelNo
	 * @param basTarih
	 * @param bitTarih
	 * @return
	 * @throws Exception
	 */
	public List<PersonelERP> getERPPersonelList(String sirketKodu, String personelNo, String basTarih, String bitTarih) throws Exception {
		return null;
	}

	/**
	 * @param mesaiIdList
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, Object> getFazlaMesaiList(List<Long> mesaiIdList) throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		if (mesaiIdList != null && !mesaiIdList.isEmpty()) {
			map = new HashMap<String, Object>();
			List<PersonelDenklestirme> list = null;
			try {
				list = pdksDAO.getObjectByInnerObjectList("id", mesaiIdList, PersonelDenklestirme.class);

			} catch (Exception e) {
				list = null;
			}
			if (list != null && !list.isEmpty()) {
				int sira = 0;
				HashMap<String, String> mesaiKodMap = new HashMap<String, String>();
				List<Tanim> tipList = pdksDAO.getObjectByInnerObjectList("tipi", Tanim.TIPI_ERP_FAZLA_MESAI, Tanim.class);
				for (Tanim tanim : tipList)
					mesaiKodMap.put(tanim.getKodu(), tanim.getErpKodu());
				DenklestirmeAy denklestirmeAy = null;
				TreeMap<String, String> veriMap = new TreeMap<String, String>();
				for (PersonelDenklestirme personelDenklestirme : list) {
					Personel personel = personelDenklestirme.getPersonel();
					if (personel.getSanalPersonel())
						continue;
					denklestirmeAy = personelDenklestirme.getDenklestirmeAy();
					Sirket sirket = personel.getSirket();
					String basString = personel.getPdksSicilNo() + ";" + sirket.getErpKodu() + ";" + (personel.getBordroAltAlan() != null ? personel.getBordroAltAlan().getErpKodu() : "") + ";NORM;" + denklestirmeAy.getYil() + ";" + denklestirmeAy.getAy() + ";" + (++sira);
					String key = sirket.getErpKodu() + "_" + personel.getPdksSicilNo() + "_";
					satirEkle(veriMap, key, basString, "UO", personelDenklestirme.getOdenecekSure(), mesaiKodMap);
					satirEkle(veriMap, key, basString, "RT", personelDenklestirme.getResmiTatilSure(), mesaiKodMap);
					satirEkle(veriMap, key, basString, "A", personelDenklestirme.getAksamVardiyaSayisi(), mesaiKodMap);
					satirEkle(veriMap, key, basString, "AS", personelDenklestirme.getAksamVardiyaSaatSayisi(), mesaiKodMap);
					satirEkle(veriMap, key, basString, "HT", personelDenklestirme.getHaftaCalismaSuresi(), mesaiKodMap);
				}
				if (!veriMap.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					for (Iterator iterator = veriMap.keySet().iterator(); iterator.hasNext();) {
						String key = (String) iterator.next();
						String str = veriMap.get(key);
						sb.append(str);
						if (iterator.hasNext())
							sb.append("\n");
					}
					map.put("dosyaAdi", "dosya_" + denklestirmeAy.getYil() + "_" + denklestirmeAy.getAyAdi() + ".csv");
					map.put("contentType", "application/csv");
					map.put("content", sb.toString());
				}
				veriMap = null;
			}
		}
		return map;
	}

	/**
	 * @param veriMap
	 * @param startKey
	 * @param basString
	 * @param mesaiKodu
	 * @param tutar
	 * @param mesaiKodMap
	 */
	private void satirEkle(TreeMap<String, String> veriMap, String startKey, String basString, String mesaiKodu, Double tutar, HashMap<String, String> mesaiKodMap) {
		if (tutar != null && tutar.doubleValue() > 0.0d && mesaiKodMap.containsKey(mesaiKodu)) {
			String mesaiERPKodu = mesaiKodMap.get(mesaiKodu);
			String key = startKey + mesaiERPKodu + "_" + PdksUtil.textBaslangicinaKarakterEkle("" + (veriMap.size() + 1), '0', 8);
			veriMap.put(key, basString + ";" + mesaiERPKodu + ";" + tutar);
		}
	}

	/**
	 * @param mesaiIdList
	 * @return
	 * @throws Exception
	 */
	public List<MesaiPDKS> sendFazlaMesaiList(List<Long> mesaiIdList) throws Exception {
		List<MesaiPDKS> mesaiERPList = null;
		if (mesaiIdList != null && !mesaiIdList.isEmpty()) {
			List<PersonelDenklestirme> list = null;
			try {
				list = pdksDAO.getObjectByInnerObjectList("id", mesaiIdList, PersonelDenklestirme.class);
			} catch (Exception e) {
				list = null;
			}
			if (list != null && !list.isEmpty()) {
				HashMap<String, String> mesaiKodMap = new HashMap<String, String>();
				List<Tanim> tipList = pdksDAO.getObjectByInnerObjectList("tipi", Tanim.TIPI_ERP_FAZLA_MESAI, Tanim.class);
				for (Tanim tanim : tipList) {
					mesaiKodMap.put(tanim.getKodu(), tanim.getErpKodu());
				}
				TreeMap<String, MesaiPDKS> mesaiMap = new TreeMap<String, MesaiPDKS>();
				for (PersonelDenklestirme personelDenklestirme : list) {
					Personel personel = personelDenklestirme.getPersonel();
					if (personel.getSanalPersonel())
						continue;
					DenklestirmeAy denklestirmeAy = personelDenklestirme.getDenklestirmeAy();
					MesaiPDKS mesaiPDKS = new MesaiPDKS();
					mesaiPDKS.setYil(denklestirmeAy.getYil());
					mesaiPDKS.setAy(denklestirmeAy.getAy());
					mesaiPDKS.setPersonelNo(personel.getPdksSicilNo());
					mesaiPDKS.setSirketKodu(personel.getSirket() != null ? personel.getSirket().getErpKodu() : null);
					mesaiPDKS.setMasrafYeriKodu(personel.getMasrafYeri() == null ? personel.getMasrafYeri().getErpKodu() : null);
					mesaiPDKS.setTesisKodu(personel.getTesis() != null ? personel.getTesis().getErpKodu() : null);
					mesaiEkle(mesaiMap, mesaiPDKS, "UO", personelDenklestirme.getOdenecekSure(), mesaiKodMap);
					mesaiEkle(mesaiMap, mesaiPDKS, "HT", personelDenklestirme.getHaftaCalismaSuresi(), mesaiKodMap);
					mesaiEkle(mesaiMap, mesaiPDKS, "A", personelDenklestirme.getAksamVardiyaSayisi(), mesaiKodMap);
					mesaiEkle(mesaiMap, mesaiPDKS, "AS", personelDenklestirme.getAksamVardiyaSaatSayisi(), mesaiKodMap);
					mesaiEkle(mesaiMap, mesaiPDKS, "RT", personelDenklestirme.getResmiTatilSure(), mesaiKodMap);
				}
				if (!mesaiMap.isEmpty())
					mesaiERPList = new ArrayList<MesaiPDKS>(mesaiMap.values());
			}

		}
		if (mesaiERPList == null)
			mesaiERPList = new ArrayList<MesaiPDKS>();
		return mesaiERPList;
	}

	/**
	 * @param mesaiMap
	 * @param mesaiPDKSBase
	 * @param mesaiKodu
	 * @param tutar
	 * @param mesaiKodMap
	 */
	private void mesaiEkle(TreeMap<String, MesaiPDKS> mesaiMap, MesaiPDKS mesaiPDKSBase, String mesaiKodu, Double tutar, HashMap<String, String> mesaiKodMap) {
		if (tutar != null && tutar.doubleValue() > 0.0d && mesaiKodMap.containsKey(mesaiKodu)) {
			MesaiPDKS mesaiPDKS = (MesaiPDKS) mesaiPDKSBase.clone();
			String mesaiERPKodu = mesaiKodMap.get(mesaiKodu);
			mesaiPDKS.setMesaiKodu(mesaiERPKodu);
			String key = mesaiPDKSBase.getSirketKodu() + "_" + mesaiPDKSBase.getPersonelNo() + "_" + mesaiERPKodu + "_" + PdksUtil.textBaslangicinaKarakterEkle("" + (mesaiMap.size() + 1), '0', 8);
			mesaiPDKS.setToplamSure(tutar);
			mesaiMap.put(key, mesaiPDKS);
		}
	}

	/**
	 * @param izinList
	 * @return
	 * @throws Exception
	 */
	public List<IzinERP> sendERPIzinler(List<IzinERP> izinList) throws Exception {
		return null;
	}

	/**
	 * @param msg
	 */
	private void mesajInfoYaz(String msg) {
		if (PdksUtil.hasStringValue(msg))
			logger.info(PdksUtil.setTurkishStr(msg));
	}

	/**
	 * @param sirketKodu
	 * @param yil
	 * @param ay
	 * @param donemKapat
	 * @return
	 * @throws Exception
	 */
	public List<MesaiPDKS> getMesaiPDKS(String sirketKodu, Integer yil, Integer ay, Boolean donemKapat) throws Exception {
		sistemVerileriniYukle(pdksDAO);
		boolean servisDurum = !PdksUtil.getCanliSunucuDurum() || !(mailMap.containsKey("getMesaiPDKSDurum") && mailMap.get("getMesaiPDKSDurum").equals("0"));
		List<MesaiPDKS> list = null;
		if (servisDurum) {
			LinkedHashMap<String, Object> inputMap = new LinkedHashMap<String, Object>();
			if (sirketKodu != null)
				inputMap.put("sirketKodu", sirketKodu);
			if (yil != null)
				inputMap.put("yil", yil);
			if (ay != null)
				inputMap.put("ay", ay);
			if (donemKapat != null)
				inputMap.put("donemKapat", donemKapat);
			saveFonksiyonVeri("getMesaiPDKS", inputMap);

			dosyaEkAdi = (PdksUtil.hasStringValue(sirketKodu) ? sirketKodu.trim() : "") + "-" + ((yil != null ? yil : 0) * 100 + (ay != null ? +ay : 0));

			fields.clear();
			fields.put("yil", yil);
			fields.put("ay", ay);
			DenklestirmeAy denklestirmeAy = (DenklestirmeAy) pdksDAO.getObjectByInnerObject(fields, DenklestirmeAy.class);
			if (denklestirmeAy != null) {
				Sirket sirket = null;
				if (PdksUtil.hasStringValue(sirketKodu)) {
					sirket = (Sirket) pdksDAO.getObjectByInnerObject("erpKodu", sirketKodu, Sirket.class);
					if (sirket == null)
						denklestirmeAy = null;
				}
				if (denklestirmeAy != null) {
					mesaj = yil + "-" + ay + (sirket != null ? " " + sirket.getAd() : "");
					mesajInfoYaz("getMesaiPDKS --> " + mesaj + " in " + new Date());
					LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
					veriMap.put(BaseDAOHibernate.MAP_KEY_SELECT, "SP_GET_FAZLA_MESAI");
					veriMap.put("sirketId", sirket != null ? sirket.getId() : 0L);
					veriMap.put("yil", denklestirmeAy.getYil());
					veriMap.put("ay", denklestirmeAy.getAy());
					try {
						List<String> perNoList = null;
						List<PersonelMesai> personelMesaiList = pdksDAO.execSPList(veriMap, PersonelMesai.class);
						if (donemKapat != null && donemKapat.booleanValue() && !personelMesaiList.isEmpty())
							perNoList = new ArrayList<String>();
						list = new ArrayList<MesaiPDKS>();
						for (Iterator iterator = personelMesaiList.iterator(); iterator.hasNext();) {
							PersonelMesai personelMesai = (PersonelMesai) iterator.next();
							Personel personel = personelMesai.getPersonel();
							MesaiPDKS mesaiPDKS = new MesaiPDKS();
							mesaiPDKS.setYil(yil);
							mesaiPDKS.setAy(ay);
							mesaiPDKS.setPersonelNo(personel.getPdksSicilNo());
							mesaiPDKS.setMesaiKodu(personelMesai.getErpKodu());
							mesaiPDKS.setToplamSure(personelMesai.getSure());
							mesaiPDKS.setSirketKodu(personel.getSirket() != null ? personel.getSirket().getErpKodu() : null);
							mesaiPDKS.setMasrafYeriKodu(personel.getMasrafYeri() != null ? personel.getMasrafYeri().getErpKodu() : null);
							mesaiPDKS.setTesisKodu(personel.getTesis() != null ? personel.getTesis().getErpKodu() : null);
							list.add(mesaiPDKS);
							if (perNoList != null && !perNoList.contains(mesaiPDKS.getPersonelNo()))
								perNoList.add(mesaiPDKS.getPersonelNo());
						}
						if (perNoList != null && !perNoList.isEmpty()) {
							HashMap fields = new HashMap();
							StringBuffer sb = new StringBuffer();
							sb.append("SELECT D.* FROM " + PersonelDenklestirme.TABLE_NAME + " D WITH(nolock)");
							sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON  P." + Personel.COLUMN_NAME_ID + "=D." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
							sb.append(" AND P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " :p ");
							sb.append(" WHERE D." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=" + denklestirmeAy.getId());
							sb.append(" AND (D.ERP_AKTARILDI IS NULL OR D.ERP_AKTARILDI<>1)");
							fields.put("p", perNoList);
							List<PersonelDenklestirme> personelDenklestirmeList = pdksDAO.getNativeSQLList(fields, sb, PersonelDenklestirme.class);
							if (!personelDenklestirmeList.isEmpty()) {
								Date guncellemeTarihi = new Date();
								for (PersonelDenklestirme personelDenklestirme : personelDenklestirmeList) {
									personelDenklestirme.setErpAktarildi(Boolean.TRUE);
									personelDenklestirme.setGuncellemeTarihi(guncellemeTarihi);
								}
								pdksDAO.saveObjectList(personelDenklestirmeList);
							}
						}
					} catch (Exception e) {
						logger.error(e);
						e.printStackTrace();
					}
					mesajInfoYaz("getMesaiPDKS --> " + mesaj + " out " + new Date());
					try {

						mailMapGuncelle("bccEntegrasyon", "bccEntegrasyonAdres");
						MailObject mailObject = kullaniciIKYukle(mailMap, pdksDAO);
						String dosyaAdi = PdksUtil.setTurkishStr("FazlaMesai_" + +denklestirmeAy.getYil() + " " + denklestirmeAy.getAyAdi() + (sirket != null ? "_" + sirket.getAd() : "")) + ".xml";
						String subject = uygulamaBordro + " " + denklestirmeAy.getAyAdi() + " " + denklestirmeAy.getYil() + " " + (sirket != null ? sirket.getAd() + " " : "") + "fazla mesai yükleme";
						String body = denklestirmeAy.getAyAdi() + " " + denklestirmeAy.getYil() + " dönemi " + (sirket != null ? sirket.getAd() + " " : "") + " fazla mesai dosyası " + dosyaAdi + " ektedir.";
						mailObject.setSubject(subject);
						mailObject.setBody(body);
						LinkedHashMap dataMap = new LinkedHashMap(), dataInputMap = new LinkedHashMap();
						dataInputMap.put("sirketKodu", sirketKodu);
						dataInputMap.put("yil", yil);
						dataInputMap.put("ay", ay);
						dataInputMap.put("donemKapat", donemKapat);
						dataMap.put("input", dataInputMap);
						if (!list.isEmpty())
							dataMap.put("getMesaiPDKSReturn", list);
						else
							dataMap.put("mesaj", denklestirmeAy.getAyAdi() + " " + denklestirmeAy.getYil() + " dönemi " + (denklestirmeAy.getDurum() ? " ait kayıt bulunamadı!" : " kapatılmıştır!"));
						mailAdresKontrol(mailObject, null);
						Gson gs = new Gson();
						String xml = PdksUtil.getJsonToXML(gs.toJson(dataMap), "getMesaiPDKS", null);
						MailFile mailFile = new MailFile();
						mailFile.setDisplayName(dosyaAdi);
						mailFile.setIcerik(PdksUtil.getBytesUTF8(xml));
						mailObject.getAttachmentFiles().add(mailFile);
						mailMap.put("mailObject", mailObject);
						MailManager.ePostaGonder(mailMap);
						gs = null;
						mailObject = null;

					} catch (Exception e) {
						logger.error(e);
						e.printStackTrace();
					}

				}
			}
			if (list == null)
				list = new ArrayList<MesaiPDKS>();

			saveFonksiyonVeri(null, list);
		} else {
			throw new Exception(servisAdi + " servisi  kapalıdır!");
		}
		return list;
	}

	/**
	 * @param oldName
	 * @param newName
	 */
	private void mailMapGuncelle(String oldName, String newName) {
		try {
			if (mailMap != null && mailMap.containsKey(oldName)) {
				mailMap.put(newName, mailMap.get(oldName));
				mailMap.remove(oldName);
			}
		} catch (Exception e) {
		}

	}

	/**
	 * @param izinHakedisList
	 * @throws Exception
	 */
	public void saveHakedisIzinler(List<IzinHakedis> izinHakedisList) throws Exception {
		mesaj = "";
		if (izinHakedisList != null && !izinHakedisList.isEmpty()) {
			if (izinHakedisList.size() == 1)
				mesaj = izinHakedisList.get(0).getPersonelNo();
			sistemVerileriniYukle(pdksDAO);
			setSicilNoUzunluk();
			saveFonksiyonVeri("saveHakedisIzinler", izinHakedisList);
			String personelNoAciklama = personelNoAciklama();
			LinkedHashMap<String, Object> izinKeyAciklamaMap = new LinkedHashMap<String, Object>();
			LinkedHashMap<String, String> izinKeyPersonelMap = new LinkedHashMap<String, String>();
			List<IzinHakedis> izinHakedisHataList = new ArrayList<IzinHakedis>();
			for (Iterator iterator1 = izinHakedisList.iterator(); iterator1.hasNext();) {
				IzinHakedis izinHakedis = (IzinHakedis) iterator1.next();
				if (izinHakedis.getHakedisList() == null || izinHakedis.getHakedisList().isEmpty()) {
					addHatalist(izinHakedis.getHataList(), "Hakediş veri yoktur!");
					izinHakedisHataList.add(izinHakedis);
					iterator1.remove();
					continue;
				}
				String perHakedisNo = PdksUtil.textBaslangicinaKarakterEkle(izinHakedis.getPersonelNo(), '0', sicilNoUzunluk);
				// izinKeyPersonelMap.put(perHakedisNo, "'" + perHakedisNo + "'");
				izinKeyPersonelMap.put(perHakedisNo, perHakedisNo);
				for (Iterator iterator = izinHakedis.getHakedisList().iterator(); iterator.hasNext();) {
					IzinHakedisDetay detay = (IzinHakedisDetay) iterator.next();
					String key = perHakedisNo + "_" + detay.getHakEdisTarihi().substring(0, 4);
					izinKeyAciklamaMap.put(key, detay);
					if (detay.getKullanilanIzinler() == null || detay.getKullanilanIzinler().isEmpty())
						continue;
					for (IzinERP izin : detay.getKullanilanIzinler()) {
						String perNo = PdksUtil.textBaslangicinaKarakterEkle(izin.getPersonelNo(), '0', sicilNoUzunluk);
						if (!izinKeyPersonelMap.containsKey(perNo)) {
							// izinKeyPersonelMap.put(perNo, "'" + perNo + "'");
							izinKeyPersonelMap.put(perNo, perNo);
						}

						izinKeyAciklamaMap.put(izin.getReferansNoERP(), izin);
					}
				}

			}
			if (!izinKeyPersonelMap.isEmpty()) {
				TreeMap<String, Personel> personelMap = pdksDAO.getObjectByInnerObjectMap("getPdksSicilNo", "pdksSicilNo", new ArrayList(izinKeyPersonelMap.values()), Personel.class, false);
				List<String> list = new ArrayList<String>();
				for (String string : izinKeyAciklamaMap.keySet()) {
					// list.add("'" + string + "'");
					list.add(string);
				}

				TreeMap<String, IzinReferansERP> izinMap = pdksDAO.getObjectByInnerObjectMap("getId", "id", list, IzinReferansERP.class, false);
				List<Long> idList = new ArrayList<Long>();
				for (String key : izinMap.keySet())
					idList.add(izinMap.get(key).getIzin().getId());
				TreeMap<String, PersonelIzinDetay> izinDetayMap = new TreeMap<String, PersonelIzinDetay>();
				if (!idList.isEmpty())
					izinDetayMap = pdksDAO.getObjectByInnerObjectMap("getHakEdisIzinKey", "hakEdisIzin.id", idList, PersonelIzinDetay.class, false);

				list = null;
				fields.clear();
				fields.put("departman.admin=", Boolean.TRUE);
				fields.put("izinTipiTanim.kodu=", IzinTipi.YILLIK_UCRETLI_IZIN);
				fields.put("durum=", Boolean.TRUE);
				List<IzinTipi> izinTipleri = pdksDAO.getObjectByInnerObjectListInLogic(fields, IzinTipi.class);
				IzinTipi kullanilanIzinTipi = null, hakedisIzinTipi = null;
				for (IzinTipi izinTipi : izinTipleri) {
					if (izinTipi.getBakiyeIzinTipi() == null)
						kullanilanIzinTipi = izinTipi;
					else
						hakedisIzinTipi = izinTipi;
				}
				String erpIzinKodu = kullanilanIzinTipi != null ? kullanilanIzinTipi.getIzinTipiTanim().getErpKodu() : "";
				List saveList = new ArrayList();
				Calendar cal = Calendar.getInstance();
				idList.clear();
				TreeMap<Long, PersonelIzin> hakedisMap = null;
				if (hakedisIzinTipi != null) {
					for (IzinHakedis izinHakedis : izinHakedisList) {
						String perHakedisNo = PdksUtil.textBaslangicinaKarakterEkle(izinHakedis.getPersonelNo(), '0', sicilNoUzunluk);
						if (personelMap.containsKey(perHakedisNo)) {
							Long id = personelMap.get(perHakedisNo).getId();
							if (!idList.contains(id))
								idList.add(id);
						}
					}
				}
				if (idList.isEmpty())
					hakedisMap = new TreeMap<Long, PersonelIzin>();
				else {
					fields.clear();
					fields.put("Map", "getId");
					fields.put("select", "hakEdisIzin");
					fields.put("hakEdisIzin.izinSahibi.id", idList);
					hakedisMap = pdksDAO.getObjectByInnerObjectMap(fields, PersonelIzinDetay.class, false);
				}
				idList = null;

				for (IzinHakedis izinHakedis : izinHakedisList) {
					Boolean izinHakedisYazildi = true;
					if (hakedisIzinTipi == null) {
						addHatalist(izinHakedis.getHataList(), "Hakediş izin tipi tanımsız!");
						izinHakedisHataList.add(izinHakedis);
						continue;
					}
					String perHakedisNo = PdksUtil.textBaslangicinaKarakterEkle(izinHakedis.getPersonelNo(), '0', sicilNoUzunluk);
					if (!personelMap.containsKey(perHakedisNo)) {
						addHatalist(izinHakedis.getHataList(), perHakedisNo + " personel veri yoktur!");
						izinHakedisHataList.add(izinHakedis);
						continue;
					}
					Personel izinHakedisPersonel = personelMap.get(perHakedisNo);
					saveList.clear();
					TreeMap<String, Object> izinHakedisERPMap = new TreeMap<String, Object>();
					TreeMap<String, PersonelIzin> izinHakedisMap = new TreeMap<String, PersonelIzin>();
					for (Iterator iterator = izinHakedis.getHakedisList().iterator(); iterator.hasNext();) {
						IzinHakedisDetay detay = (IzinHakedisDetay) iterator.next();
						String id = PdksUtil.textBaslangicinaKarakterEkle(izinHakedis.getPersonelNo(), '0', sicilNoUzunluk) + "_" + detay.getHakEdisTarihi().substring(0, 4);
						PersonelIzin hakedisPersonelIzin = null;
						IzinReferansERP izinReferansERP = null;
						if (izinMap.containsKey(id)) {
							izinReferansERP = izinMap.get(id);
							hakedisPersonelIzin = izinReferansERP.getIzin();
							if (hakedisMap.containsKey(hakedisPersonelIzin.getId()))
								hakedisMap.remove(hakedisPersonelIzin.getId());
						} else {
							izinReferansERP = new IzinReferansERP();
							hakedisPersonelIzin = new PersonelIzin();
							izinReferansERP.setId(id);
							hakedisPersonelIzin.setIzinTipi(hakedisIzinTipi);
							hakedisPersonelIzin.setIzinSahibi(izinHakedisPersonel);
							hakedisPersonelIzin.setHesapTipi(hakedisIzinTipi.getHesapTipi());
							izinReferansERP.setIzin(hakedisPersonelIzin);
						}

						Date hakEdisBitisZamani = getTarih(detay.getHakEdisTarihi(), FORMAT_DATE);
						if (hakEdisBitisZamani == null) {
							izinHakedisYazildi = false;
							if (detay.getHakEdisTarihi() == null || detay.getHakEdisTarihi().trim().length() < FORMAT_DATE.length())
								addHatalist(izinHakedis.getHataList(), "Hakediş tarihi boş veya " + FORMAT_DATE + " formatından farklıdır!");
							else
								addHatalist(izinHakedis.getHataList(), detay.getHakEdisTarihi().trim() + " hakediş tarihi " + FORMAT_DATE + " formatından farklıdır!");
							continue;
						}
						boolean eski = hakedisPersonelIzin.getId() != null, ekli = false;
						if (!eski) {
							ekli = true;
							saveList.add(hakedisPersonelIzin);
							saveList.add(izinReferansERP);
						}
						cal.setTime(hakEdisBitisZamani);
						String aciklama = String.valueOf(detay.kidemYil);
						Date hakEdisBaslangicZamani = getTarih(cal.get(Calendar.YEAR) + "-01-01", FORMAT_DATE);

						if (eski)
							eski = hakedisPersonelIzin.getBitisZamani().getTime() == hakEdisBitisZamani.getTime();
						if (eski)
							eski = hakedisPersonelIzin.getIzinDurumu() == PersonelIzin.IZIN_DURUMU_ONAYLANDI && detay.getIzinSuresi() > 0.0d;
						if (eski)
							eski = hakedisPersonelIzin.getIzinSuresi().doubleValue() == detay.getIzinSuresi();
						if (eski)
							eski = aciklama.equals(hakedisPersonelIzin.getAciklama());
						hakedisPersonelIzin.setBaslangicZamani(hakEdisBaslangicZamani);
						hakedisPersonelIzin.setBitisZamani(hakEdisBitisZamani);
						hakedisPersonelIzin.setIzinSuresi(detay.getIzinSuresi());
						hakedisPersonelIzin.setAciklama(aciklama);
						hakedisPersonelIzin.setIzinDurumu(detay.getIzinSuresi() > 0.0d ? PersonelIzin.IZIN_DURUMU_ONAYLANDI : PersonelIzin.IZIN_DURUMU_REDEDILDI);
						if (hakedisPersonelIzin.getId() == null) {
							hakedisPersonelIzin.setOlusturanUser(islemYapan);
							hakedisPersonelIzin.setOlusturmaTarihi(new Date());
							saveList.add(izinReferansERP);
						} else {
							hakedisPersonelIzin.setGuncellemeTarihi(new Date());
							hakedisPersonelIzin.setGuncelleyenUser(islemYapan);
						}
						izinHakedisERPMap.put(id, detay);
						izinHakedisMap.put(id, hakedisPersonelIzin);
						if (eski == false && ekli == false)
							saveList.add(hakedisPersonelIzin);
						if (detay.getKullanilanIzinler() != null) {
							for (IzinERP izinERP : detay.getKullanilanIzinler()) {
								String perNo = PdksUtil.textBaslangicinaKarakterEkle(izinERP.getPersonelNo(), '0', sicilNoUzunluk);
								if (!perNo.equals(perHakedisNo) && !personelMap.containsKey(perNo)) {
									addHatalist(izinERP.getHataList(), perNo + " personel veri yoktur!");
									izinHakedisYazildi = false;
									continue;
								}
								if (kullanilanIzinTipi == null) {
									addHatalist(izinERP.getHataList(), "Kullanılan izin tipi tanımsız!");
									izinHakedisYazildi = false;
									continue;
								}
								if (!erpIzinKodu.equals(izinERP.getIzinTipi())) {
									addHatalist(izinERP.getHataList(), izinERP.getIzinTipi() + " " + izinERP.getIzinTipiAciklama() + " hatalı izin tipidir!");
									izinHakedisYazildi = false;
									continue;
								}
								id = izinERP.getReferansNoERP();
								PersonelIzin personelIzin = null;
								IzinReferansERP izinKullanilanERP = null;
								Personel izinSahibiPersonel = perNo.equals(perHakedisNo) ? izinHakedisPersonel : personelMap.get(perNo);
								Boolean izinDegisti = false;
								if (izinMap.containsKey(id)) {
									izinKullanilanERP = izinMap.get(id);
									personelIzin = izinKullanilanERP.getIzin();
								} else {
									izinKullanilanERP = new IzinReferansERP();
									personelIzin = new PersonelIzin();
									izinKullanilanERP.setId(id);
									personelIzin.setIzinTipi(kullanilanIzinTipi);
									personelIzin.setIzinSahibi(izinSahibiPersonel);
									izinKullanilanERP.setIzin(personelIzin);

								}

								Date baslangicZamani = getTarih(izinERP.getBasZaman(), FORMAT_DATE_TIME);
								Date bitisZamani = getTarih(izinERP.getBitZaman(), FORMAT_DATE_TIME);
								boolean izinDurum = personelIzin.getIzinDurumu() == PersonelIzin.IZIN_DURUMU_ONAYLANDI;
								if (personelIzin.getId() != null)
									izinDegisti = izinDurum != izinERP.getDurum().booleanValue() || baslangicZamani.getTime() != personelIzin.getBaslangicZamani().getTime() || bitisZamani.getTime() != personelIzin.getBitisZamani().getTime();
								Double izinSuresi = izinERP.getIzinSuresi();
								aciklama = izinERP.getAciklama() != null ? izinERP.getAciklama() : kullanilanIzinTipi.getMesaj();
								personelIzin.setAciklama((aciklama != null ? aciklama.trim() : "") + " ( " + uygulamaBordro + " referans no : " + izinERP.getReferansNoERP() + " )");
								Integer hesapTipi = null;
								if (izinERP.getSureBirimi() != null) {
									hesapTipi = Integer.parseInt(izinERP.getSureBirimi().value());
									if (kullanilanIzinTipi.getHesapTipi() != null && hesapTipi.intValue() != kullanilanIzinTipi.getHesapTipi().intValue()) {
										switch (hesapTipi) {
										case 1:
											izinSuresi = izinSuresi * 24d;
											break;
										case 2:
											izinSuresi = izinSuresi / 24d;
											break;
										default:
											break;
										}
										hesapTipi = kullanilanIzinTipi.getHesapTipi();
									}
								} else
									hesapTipi = kullanilanIzinTipi.getHesapTipi();
								personelIzin.setBaslangicZamani(baslangicZamani);
								personelIzin.setBitisZamani(bitisZamani);
								personelIzin.setIzinSuresi(izinSuresi);
								personelIzin.setHesapTipi(hesapTipi);
								personelIzin.setIzinDurumu(izinERP.getDurum() != null && izinERP.getDurum() ? PersonelIzin.IZIN_DURUMU_ONAYLANDI : PersonelIzin.IZIN_DURUMU_REDEDILDI);
								if (personelIzin.getId() == null) {
									personelIzin.setOlusturanUser(islemYapan);
									personelIzin.setOlusturmaTarihi(new Date());
								} else {
									personelIzin.setGuncellemeTarihi(new Date());
									personelIzin.setGuncelleyenUser(islemYapan);
								}
								if (baslangicZamani == null || bitisZamani == null)
									addHatalist(izinERP.getHataList(), "İzin başlangıç zamanı ve bitiş zamanı boş olamaz!");
								if (izinERP.getHataList().isEmpty() && baslangicZamani.after(bitisZamani))
									addHatalist(izinERP.getHataList(), "İzin başlangıç zamanı bitiş zamanında büyük olamaz!");
								if (izinERP.getHataList().isEmpty()) {
									izinHakedisERPMap.put(izinERP.getReferansNoERP(), izinERP);
									izinHakedisMap.put(izinERP.getReferansNoERP(), personelIzin);
									if (personelIzin.getId() == null || izinDegisti) {
										saveList.add(personelIzin);
										if (personelIzin.getId() == null)
											saveList.add(izinKullanilanERP);
										if (personelIzin.getId() == null || hakedisPersonelIzin.getId() == null) {
											PersonelIzinDetay personelIzinDetay = new PersonelIzinDetay();
											personelIzinDetay.setPersonelIzin(personelIzin);
											personelIzinDetay.setHakEdisIzin(hakedisPersonelIzin);
											personelIzinDetay.setIzinMiktari(izinERP.getIzinSuresi());
											saveList.add(personelIzinDetay);
										} else {
											String key = PersonelIzinDetay.getHakEdisIzinKeyStr(hakedisPersonelIzin, personelIzin);
											if (izinDetayMap.containsKey(key)) {
												PersonelIzinDetay personelIzinDetay = izinDetayMap.get(key);
												if (personelIzinDetay.getIzinMiktari() != izinERP.getIzinSuresi().doubleValue()) {
													personelIzinDetay.setIzinMiktari(izinERP.getIzinSuresi());
													saveList.add(personelIzinDetay);
												}
												izinDetayMap.remove(key);
											}
										}
									}
								}
								if (izinERP.getHataList().isEmpty()) {
									izinERP.setYazildi(true);
									izinERP.setHataList(null);
								}
							}
						}
						if (detay.getHataList().isEmpty()) {
							detay.setYazildi(true);
							detay.setHataList(null);
						}
					}
					if (!izinHakedisYazildi)
						izinHakedisHataList.add(izinHakedis);
					izinHakedis.setYazildi(izinHakedisYazildi);
					if (izinHakedisYazildi || izinHakedis.getHataList().isEmpty())
						izinHakedis.setHataList(null);
					if (!saveList.isEmpty()) {
						try {
							pdksDAO.saveObjectList(saveList);

						} catch (Exception e) {
							logger.error(e);
						}
					} else if (izinHakedis.getHataList().isEmpty()) {
						izinHakedis.setYazildi(true);
						izinHakedis.setHataList(null);
					}
					for (String key : izinHakedisMap.keySet()) {
						PersonelIzin izin = izinHakedisMap.get(key);
						if (izin.getId() != null) {
							Object object = izinHakedisERPMap.get(key);
							if (object instanceof IzinERP) {
								IzinERP izinERP = (IzinERP) object;
								izinERP.setId(izin.getId());
								izinERP.setYazildi(true);
								izinERP.setHataList(null);
							} else if (object instanceof IzinHakedisDetay) {
								IzinHakedisDetay detay = (IzinHakedisDetay) object;
								detay.setId(izin.getId());
								detay.setYazildi(true);
								detay.setHataList(null);
							}
						}
					}
				}
				saveList.clear();
				if (!hakedisMap.isEmpty()) {
					for (Long id : hakedisMap.keySet()) {
						PersonelIzin izin = hakedisMap.get(id);
						if (izin.getIzinTipi().getId().equals(hakedisIzinTipi.getId()) && izin.getIzinDurumu() == PersonelIzin.IZIN_DURUMU_ONAYLANDI) {
							izin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_REDEDILDI);
							izin.setGuncellemeTarihi(new Date());
							izin.setGuncelleyenUser(islemYapan);
							saveList.add(izin);
						}
					}

				}

				if (!izinDetayMap.isEmpty() || !saveList.isEmpty()) {
					try {
						pdksDAO.saveAndDeleteObjectList(saveList, new ArrayList(izinDetayMap.values()));
					} catch (Exception e) {
						logger.error(e);
					}
				}
				saveList = null;
				izinDetayMap = null;
				hakedisMap = null;
				personelMap = null;
			}

			saveFonksiyonVeri(null, izinHakedisList);
			mesajInfoYaz("saveHakedisIzinler --> " + mesaj + " out " + new Date());
			if (!izinHakedisHataList.isEmpty()) {
				Gson gson = new Gson();
				String jsonStr = PdksUtil.toPrettyFormat(gson.toJson(izinHakedisHataList));
				StringBuffer sb = new StringBuffer();
				sb.append("<DIV>");
				for (IzinHakedis izinHakedis : izinHakedisHataList) {
					sb.append("<LABEL><b>" + personelNoAciklama + " : </b>" + izinHakedis.getPersonelNo() + "</LABEL>");
					sb.append("<TABLE style=\"border: solid 1px\" cellpadding=\"5\" cellspacing=\"0\">");
					if (!izinHakedis.getHataList().isEmpty()) {
						sb.append("<TR><TD colspan=3 align=center><u>Hatalar</u></TD></TR>");
						for (String string : izinHakedis.getHataList())
							sb.append("<TR><TD></TD><TD colspan=2><LI>" + string + "</LI></TD></TR>");
					}
					for (IzinHakedisDetay detay : izinHakedis.getHakedisList()) {
						sb.append("<TR><TD></TD><TD nowrap><b>Hakediş Tarihi</b></TD><TD nowrap>" + detay.getHakEdisTarihi() + "</TD></TR>");
						sb.append("<TR><TD></TD><TD nowrap><b>Kıdem Yıl</b></TD><TD nowrap>" + detay.getKidemYil() + "</TD></TR>");
						sb.append("<TR><TD></TD><TD nowrap><b>İzin Süresi</b></TD><TD nowrap>" + PdksUtil.numericValueFormatStr(detay.getIzinSuresi(), Constants.TR_LOCALE) + "</TD></TR>");
						if (!detay.getHataList().isEmpty()) {
							sb.append("<TR><TD></TD><TD colspan=2 align=center><u>Hatalar</u></TD></TR>");
							for (String string : detay.getHataList())
								sb.append("<TR><TD colspan=2></TD><TD><LI>" + string + "</LI></TD></TR>");
						}

						if (!detay.getKullanilanIzinler().isEmpty()) {
							sb.append("<TR><TD></TD><TD  colspan=2 align=center><b><u>Kullanılan İzinler</u></b></TD></TR>");
							for (IzinERP izinERP : detay.getKullanilanIzinler()) {
								sb.append("<TR><TD></TD><TD valign=top nowrap><b>Referans No</b></TD><TD valign=top>" + izinERP.getReferansNoERP() + "<DIV><TABLE>");
								if (!izinERP.getPersonelNo().equals(izinHakedis.getPersonelNo()))
									sb.append("<TR><TD nowrap><b>" + personelNoAciklama + "</b></TD><TD>" + izinERP.getPersonelNo() + "</TD></TR>");
								sb.append("<TR><TD nowrap><b>Başlangıç Zamanı</b></TD><TD>" + izinERP.getBasZaman() + "</TD></TR>");
								sb.append("<TR><TD nowrap><b>Bitiş Zamanı</b></TD><TD>" + izinERP.getBitZaman() + "</TD></TR>");
								sb.append("<TR><TD nowrap><b>İzin Tipi</b></TD><TD>" + izinERP.getIzinTipiAciklama() + "</TD></TR>");
								sb.append("<TR><TD nowrap><b>İzin Süresi</b></TD><TD>" + PdksUtil.numericValueFormatStr(izinERP.getIzinSuresi(), Constants.TR_LOCALE) + "</TD></TR>");

								sb.append("</TABLE></DIV></TD></TR>");
								if (!izinERP.getHataList().isEmpty()) {
									sb.append("<TR><TD></TD><TD colspan=2 align=center><u>Hatalar</u></TD></TR>");
									for (String string : izinERP.getHataList())
										sb.append("<TR><TD colspan=2></TD><TD><LI>" + string + "</LI></TD></TR>");
								}
							}
						}
					}

					sb.append("</TABLE>");
				}
				sb.append("</DIV>");
				mailMap.put("konu", uygulamaBordro + " saveIzinHakedisler servis hatası");
				mailMap.put("mailIcerik", sb.toString());
				LinkedHashMap<String, Object> fileMap = new LinkedHashMap<String, Object>();
				fileMap.put("saveIzinHakedisler.xml", PdksUtil.getJsonToXML(jsonStr, "saveIzinHakedisler", "izinHakedis"));
				mailMap.put("fileMap", fileMap);
				mailMapGuncelle("bccEntegrasyon", "bccEntegrasyonAdres");
				kullaniciIKYukle(mailMap, pdksDAO);
				MailManager.ePostaGonder(mailMap);

			}
		}

	}

	/**
	 * @param personeller
	 * @return
	 * @throws Exception
	 */
	public List<IzinHakedis> getIzinHakedisler(List<String> personeller) throws Exception {
		return null;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public boolean helpDeskStatus() throws Exception {
		sistemVerileriniYukle(pdksDAO);
		return sistemDestekVar;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public String helpDeskDate() throws Exception {
		String helpDeskLastDateStr = "";
		sistemVerileriniYukle(pdksDAO);
		if (mailMap.containsKey("helpDeskLastDate"))
			helpDeskLastDateStr = (String) mailMap.get("helpDeskLastDate");
		return helpDeskLastDateStr;
	}

	/**
	 * @param izinList
	 * @throws Exception
	 */
	public void saveIzinler(List<IzinERP> izinList) throws Exception {
		servisAdi = "saveIzinler";
		if (pdksDAO != null && izinList != null && !izinList.isEmpty()) {
			sistemVerileriniYukle(pdksDAO);
			boolean servisDurum = !PdksUtil.getCanliSunucuDurum() || !(mailMap.containsKey(servisAdi + "Durum") && mailMap.get(servisAdi + "Durum").equals("0"));
			if (servisDurum) {

				IzinERP erp = null;
				boolean izinCok = izinList.size() > 1;
				boolean mailBosGonder = izinCok;
				HashMap<String, List<String>> izinPersonelERPMap = new HashMap<String, List<String>>();
				HashMap<String, Integer> referansNoMap = new HashMap<String, Integer>();
				TreeMap<Integer, IzinERP> referansSiraMap = new TreeMap<Integer, IzinERP>();
				for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
					IzinERP izinERP = (IzinERP) iterator.next();
					if (izinCok) {
						String personelNo = izinERP.getPersonelNo();
						if (PdksUtil.hasStringValue(personelNo)) {
							String key = personelNo.trim() + "_" + (izinERP.getBasZaman() != null ? izinERP.getBasZaman().trim() : "") + "_" + (izinERP.getBitZaman() != null ? izinERP.getBitZaman().trim() : "");
							int size = referansNoMap.size() + 1;
							if (!referansNoMap.containsKey(key)) {
								referansNoMap.put(key, size);
							} else {
								size = referansNoMap.get(key);
							}
							referansSiraMap.put(size, izinERP);
							String referansNoERP = izinERP.getReferansNoERP();
							if (PdksUtil.hasStringValue(referansNoERP)) {
								List<String> list = izinPersonelERPMap.containsKey(personelNo) ? izinPersonelERPMap.get(personelNo) : new ArrayList<String>();
								if (list.isEmpty())
									izinPersonelERPMap.put(personelNo, list);
								if (!list.contains(referansNoERP))
									list.add(referansNoERP);
							}
						}
					}

					izinERP.veriSifirla();
				}
				if (!referansSiraMap.isEmpty())
					izinList = new ArrayList<IzinERP>(referansSiraMap.values());
				referansNoMap = null;
				referansSiraMap = null;
				if (izinList.size() == 1) {

					erp = izinList.get(0);
					if (erp != null)
						dosyaEkAdi = erp.getPersonelNo();
				}
				setSicilNoUzunluk();
				saveFonksiyonVeri("saveIzinler", izinList);

				if (erp != null)
					mesaj = PdksUtil.setTurkishStr(erp.getPersonelNo() + " " + erp.getIzinTipiAciklama() + " bilgisi guncellenecektir.");
				else
					mesaj = izinList.size() + " adet izin bilgisi guncellenecektir.";
				mesajInfoYaz("saveIzinler --> " + mesaj + " in " + new Date());
				// String canliSunucu = "srvglf";
				// if (mailMap != null && mailMap.containsKey("canliSunucu"))
				// canliSunucu = (String) mailMap.get("canliSunucu");
				boolean testDurum = !PdksUtil.getCanliSunucuDurum();
				Integer izinBitisTarihiAySayisi = null;
				if (mailMap.containsKey("izinBitisTarihiAySayisi"))
					try {
						izinBitisTarihiAySayisi = Integer.parseInt((String) mailMap.get("izinBitisTarihiAySayisi"));
					} catch (Exception e) {
						izinBitisTarihiAySayisi = null;
					}
				if (izinBitisTarihiAySayisi == null || izinBitisTarihiAySayisi < 0)
					izinBitisTarihiAySayisi = 3;
				TreeMap<String, List<String>> veriSorguMap = new TreeMap<String, List<String>>();
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.DATE, 1);
				cal.add(Calendar.MONTH, -izinBitisTarihiAySayisi);
				Date gecmisTarihi = PdksUtil.getDate(cal.getTime());
				Date tarih1 = null, tarih2 = null;
				for (IzinERP izinERP : izinList) {
					Date baslangicZamani = getTarih(izinERP.getBasZaman(), FORMAT_DATE_TIME);
					Date bitisZamani = getTarih(izinERP.getBitZaman(), FORMAT_DATE_TIME);
					if (baslangicZamani != null)
						if (tarih1 == null || baslangicZamani.before(tarih1))
							tarih1 = baslangicZamani;
					if (bitisZamani != null)
						if (tarih2 == null || bitisZamani.after(tarih2))
							tarih2 = bitisZamani;
					izinERP.setYazildi(false);
					if (izinERP.getIzinSuresi() == null || izinERP.getIzinSuresi().doubleValue() == 0.0d)
						izinERP.setDurum(false);
					if (sicilNoUzunluk != null) {
						String perNo = PdksUtil.textBaslangicinaKarakterEkle(izinERP.getPersonelNo(), '0', sicilNoUzunluk);
						izinERP.setPersonelNo(perNo);
					}
					veriIsle("personel", izinERP.getPersonelNo(), veriSorguMap);
					veriIsle("izinTipi", izinERP.getIzinTipi(), veriSorguMap);
					veriIsle("personelIzin", izinERP.getReferansNoERP(), veriSorguMap);
				}
				List<Personel> personelList = pdksDAO.getObjectByInnerObjectList("pdksSicilNo", veriSorguMap.get("personel"), Personel.class);
				if (personelList.size() > 1)
					personelList = PdksUtil.sortListByAlanAdi(personelList, "iseBaslamaTarihi", Boolean.FALSE);
				TreeMap<String, Personel> personelMap = new TreeMap<String, Personel>();
				for (Personel personel : personelList) {
					if ((tarih1 == null || personel.getSskCikisTarihi().getTime() >= PdksUtil.getDate(tarih1).getTime()) && (tarih2 == null || personel.getIseBaslamaTarihi().getTime() <= PdksUtil.getDate(tarih2).getTime())) {
						personelMap.put(personel.getPdksSicilNo(), personel);
					}
				}
				personelList = null;
				// TreeMap<String, Personel> personelMap = veriSorguMap.containsKey("personel") ? pdksDAO.getObjectByInnerObjectMap("getPdksSicilNo", "pdksSicilNo", veriSorguMap.get("personel"), Personel.class, false) : new TreeMap<String, Personel>();
				TreeMap<String, ERPPersonel> personelERPHataliMap = veriSorguMap.containsKey("personel") ? pdksDAO.getObjectByInnerObjectMap("getSicilNo", "sicilNo", veriSorguMap.get("personel"), ERPPersonel.class, false) : new TreeMap<String, ERPPersonel>();
				TreeMap<String, IzinReferansERP> izinERPMap = veriSorguMap.containsKey("personelIzin") ? pdksDAO.getObjectByInnerObjectMap("getId", "id", veriSorguMap.get("personelIzin"), IzinReferansERP.class, false) : new TreeMap<String, IzinReferansERP>();
				TreeMap<String, IzinTipi> izinTipiMap = null;
				String erpIzinTipiOlusturStr = sistemDestekVar && mailMap.containsKey("erpIzinTipiOlustur") ? (String) mailMap.get("erpIzinTipiOlustur") : "";
				boolean erpIzinTipiOlustur = erpIzinTipiOlusturStr.equals("1");
				Departman departman = null;
				if (veriSorguMap.containsKey("izinTipi")) {
					fields.clear();
					fields.put("Select", "id");
					fields.put("tipi=", Tanim.TIPI_IZIN_TIPI);
					if (!erpIzinTipiOlustur)
						fields.put("durum=", Boolean.TRUE);

					fields.put("erpKodu", veriSorguMap.get("izinTipi"));
					List<Long> idList = pdksDAO.getObjectByInnerObjectListInLogic(fields, Tanim.class);
					if (!idList.isEmpty()) {
						fields.clear();
						fields.put("Map", "getKodERP");
						fields.put("bakiyeIzinTipi=", null);
						fields.put("departman.id=", 1L);
						fields.put("izinTipiTanim.id", idList);
						izinTipiMap = pdksDAO.getObjectByInnerObjectMapInLogic(fields, IzinTipi.class, false);
					}
				}
				if (izinTipiMap == null)
					izinTipiMap = new TreeMap<String, IzinTipi>();

				TreeMap<String, Tanim> izinTipiTanimMap = null;
				if (erpIzinTipiOlustur) {
					fields.clear();
					fields.put("id", 1L);
					departman = (Departman) pdksDAO.getObjectByInnerObject(fields, Departman.class);
					fields.clear();
					fields.put("Map", "getErpKodu");
					fields.put("tipi=", Tanim.TIPI_IZIN_TIPI);
					izinTipiTanimMap = pdksDAO.getObjectByInnerObjectMapInLogic(fields, Tanim.class, false);
				} else
					izinTipiTanimMap = new TreeMap<String, Tanim>();
				TreeMap<String, Tanim> izinGrupTanimMap = null;
				List saveList = new ArrayList(), deleteList = new ArrayList();
				if (!izinTipiTanimMap.isEmpty()) {
					fields.clear();
					fields.put("Map", "getErpKodu");
					fields.put("tipi=", Tanim.TIPI_IZIN_KODU_GRUPLARI);
					for (String key : izinTipiTanimMap.keySet()) {
						// saveList.add("'" + key + "'");
						saveList.add(key);
					}
					fields.put("erpKodu", saveList);
					izinGrupTanimMap = pdksDAO.getObjectByInnerObjectMapInLogic(fields, Tanim.class, false);
					saveList.clear();
				} else
					izinGrupTanimMap = new TreeMap<String, Tanim>();
				if (!saveList.isEmpty())
					pdksDAO.saveObjectList(saveList);
				saveList.clear();
				List<IzinERP> hataList = new ArrayList<IzinERP>();
				HashMap<String, PersonelIzin> izinMap = new HashMap<String, PersonelIzin>();
				HashMap fields = new HashMap();
				fields.put("beyazYakaDefault", Boolean.TRUE);
				fields.put("isKur", Boolean.FALSE);
				VardiyaSablonu beyazYakaDefaultVardiyaSablonu = (VardiyaSablonu) pdksDAO.getObjectByInnerObject(fields, VardiyaSablonu.class);
				Integer izinServisGun = null;
				if (mailMap.containsKey("izinServisGunDuzelt")) {
					try {
						izinServisGun = Integer.parseInt((String) mailMap.get("izinServisGunDuzelt"));
					} catch (Exception e) {
						izinServisGun = null;
					}
				}

				List<String> kidemHataList = new ArrayList<String>();
				String izinVardiyaKontrolStr = mailMap.containsKey("izinVardiyaKontrol") ? (String) mailMap.get("izinVardiyaKontrol") : "0";
				int izinVardiyaKontrol = 0;
				try {
					izinVardiyaKontrol = Integer.parseInt(izinVardiyaKontrolStr);
				} catch (Exception e) {
					izinVardiyaKontrol = 0;
				}
				Date izinlerBasTarih = null, izinlerBitTarih = null, olusturmaTarih = null;
				List<String> kayitIzinList = new ArrayList<String>();
				for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
					IzinERP izinERP = (IzinERP) iterator.next();
					Date islemZamani = new Date();
					kidemHataList.clear();
					Date baslangicZamani = getTarih(izinERP.getBasZaman(), FORMAT_DATE_TIME);
					Date bitisZamani = getTarih(izinERP.getBitZaman(), FORMAT_DATE_TIME);
					if (PdksUtil.hasStringValue(izinERP.getReferansNoERP()) == false)
						addHatalist(izinERP.getHataList(), "Referans numarası boş!");
					if (izinERP.getPersonelNo() == null)
						addHatalist(izinERP.getHataList(), "Personel numarası boş!");
					else if (!personelMap.containsKey(izinERP.getPersonelNo())) {
						if (bitisZamani != null && bitisZamani.before(gecmisTarihi)) {
							izinERP.setYazildi(null);
							continue;
						}
						izinERP.setYazildi(Boolean.FALSE);
						ERPPersonel erpPersonel = personelERPHataliMap.containsKey(izinERP.getPersonelNo()) ? personelERPHataliMap.get(izinERP.getPersonelNo()) : null;
						if (erpPersonel == null || erpPersonel.getDurum())
							addHatalist(izinERP.getHataList(), izinERP.getPersonelNo() + " " + kapiGiris + " personel numarası bulunamadı!");
						else {
							continue;
						}

					}
					if (izinERP.getIzinTipi() == null)
						addHatalist(izinERP.getHataList(), "İzin tipi boş!");
					else if (!izinTipiMap.containsKey(izinERP.getIzinTipi())) {
						if (erpIzinTipiOlustur) {
							Tanim izinTipiTanim = null;
							String aciklama = izinERP.getIzinTipiAciklama();
							if (!izinTipiTanimMap.containsKey(izinERP.getIzinTipi())) {
								izinTipiTanim = new Tanim();
								izinTipiTanim.setTipi(Tanim.TIPI_IZIN_TIPI);
								izinTipiTanim.setKodu("E" + izinERP.getIzinTipi());
								izinTipiTanim.setErpKodu(izinERP.getIzinTipi());
								izinTipiTanim.setAciklamatr(aciklama);
								izinTipiTanim.setAciklamaen(aciklama);
								izinTipiTanim.setDurum(Boolean.TRUE);
								izinTipiTanim.setGuncelle(Boolean.FALSE);
								izinTipiTanim.setIslemTarihi(islemZamani);
								izinTipiTanim.setIslemYapan(islemYapan);
								saveList.add(izinTipiTanim);
								izinTipiTanimMap.put(izinERP.getIzinTipi(), izinTipiTanim);
							} else
								izinTipiTanim = izinTipiTanimMap.get(izinERP.getIzinTipi());
							IzinTipi izinTipi = new IzinTipi();
							izinTipi.setIzinTipiTanim(izinTipiTanim);
							izinTipi.setErpAktarim(Boolean.TRUE);
							izinTipi.setDurum(Boolean.TRUE);
							izinTipi.setDenklestirmeDahil(Boolean.TRUE);
							izinTipi.setOffDahil(Boolean.TRUE);
							izinTipi.setArtikIzinGun(0.0d);
							izinTipi.setDepartman(departman);
							izinTipi.setBakiyeDevirTipi("0");
							izinTipi.setDokumAlmaDurum(Boolean.FALSE);
							izinTipi.setGunGosterilecek(Boolean.TRUE);
							izinTipi.setSaatGosterilecek(Boolean.FALSE);
							izinTipi.setIzinKagidiGeldi(Boolean.FALSE);
							izinTipi.setGunSigortaDahil(Boolean.TRUE);
							izinTipi.setMaxGun(0d);
							izinTipi.setMinGun(0d);
							izinTipi.setMaxSaat(0d);
							izinTipi.setMinSaat(0d);
							izinTipi.setMesaj(aciklama);
							String kisaAciklama = getIzinKisaAciklama(aciklama);
							izinTipi.setKisaAciklama(kisaAciklama);
							izinTipi.setOnaylayanTipi("0");
							izinTipi.setPersonelGirisTipi("0");
							izinTipi.setTakvimGunumu(Boolean.FALSE);
							izinTipi.setHesapTipi(1);
							izinTipi.setDurumCGS(1);
							izinTipi.setKotaBakiye(null);
							izinTipi.setOlusturanUser(islemYapan);
							if (!izinGrupTanimMap.containsKey(izinTipiTanim.getErpKodu())) {
								Tanim tanim2 = new Tanim();
								tanim2.setTipi(Tanim.TIPI_IZIN_KODU_GRUPLARI);
								tanim2.setKodu(BordroIzinGrubu.TANIMSIZ.value());
								tanim2.setErpKodu(izinTipiTanim.getErpKodu());
								tanim2.setIslemYapan(islemYapan);
								tanim2.setIslemTarihi(islemZamani);
								izinGrupTanimMap.put(izinTipiTanim.getErpKodu(), tanim2);
								saveList.add(tanim2);
							}

							saveList.add(izinTipi);
							izinTipiMap.put(izinERP.getIzinTipi(), izinTipi);
						} else
							addHatalist(izinERP.getHataList(), izinERP.getIzinTipi() + (izinERP.getIzinTipiAciklama() != null ? " - " + izinERP.getIzinTipiAciklama() : "") + " izin tipi tanımsız!");

					} else {
						IzinTipi izinTipi = izinTipiMap.get(izinERP.getIzinTipi());
						Tanim izinTipiTanim = izinTipi.getIzinTipiTanim();
						String aciklama = izinERP.getIzinTipiAciklama();
						if (!PdksUtil.setTurkishStr(izinTipiTanim.getAciklamatr().toLowerCase(Constants.TR_LOCALE)).equals(PdksUtil.setTurkishStr(aciklama.toLowerCase(Constants.TR_LOCALE)))) {
							String kisaAciklama = getIzinKisaAciklama(aciklama);
							izinTipiTanim.setAciklamatr(aciklama);
							izinTipiTanim.setAciklamaen(aciklama);
							if (!(izinTipi.isSenelikIzin() || izinTipi.isSutIzin()))
								izinTipiTanim.setKodu(kisaAciklama);
							izinTipiTanim.setIslemYapan(islemYapan);
							izinTipiTanim.setIslemTarihi(islemZamani);
							izinTipi.setMesaj(aciklama);
							izinTipi.setGuncelleyenUser(islemYapan);
							izinTipi.setGuncellemeTarihi(islemZamani);
							saveList.add(izinTipiTanim);
							saveList.add(izinTipi);
						}
					}
					if (baslangicZamani == null) {
						if (notEmptyStr(izinERP.getBasZaman()) == false)
							addHatalist(izinERP.getHataList(), "İzin başlangıç zamanı boş olamaz!");
						else
							addHatalist(izinERP.getHataList(), "İzin başlangıç zamanı hatalıdır! (" + izinERP.getBasZaman() + " --> format : " + FORMAT_DATE_TIME + " )");
					}
					if (bitisZamani == null) {
						if (notEmptyStr(izinERP.getBitZaman()) == false)
							addHatalist(izinERP.getHataList(), "İzin bitiş zamanı boş olamaz!");
						else
							addHatalist(izinERP.getHataList(), "İzin bitiş zamanı hatalıdır! (" + izinERP.getBitZaman() + " --> format : " + FORMAT_DATE_TIME + " )");
					}
					if (izinERP.getHataList().isEmpty()) {
						IzinReferansERP izinReferansERP = izinERPMap.containsKey(izinERP.getReferansNoERP().trim()) ? izinERPMap.get(izinERP.getReferansNoERP().trim()) : new IzinReferansERP(izinERP.getReferansNoERP().trim());
						PersonelIzin personelIzin = izinReferansERP.getIzin();
						Personel izinSahibi = personelMap.get(izinERP.getPersonelNo());
						personelIzin.setIzinSahibi(izinSahibi);
						boolean doktor = izinSahibi.isHekim();
						boolean mailEkle = doktor && personelIzin.getId() == null;
						Date sonCalismaTarihi = izinSahibi.getSskCikisTarihi();
						if (bitisZamani.before(baslangicZamani))
							addHatalist(izinERP.getHataList(), "İzin başlama zamanı bitiş tarihinden sonra olamaz!");
						if (baslangicZamani.before(izinSahibi.getIseBaslamaTarihi()))
							addHatalist(izinERP.getHataList(), "İzin başlangıç zamanı işe giriş tarihi " + PdksUtil.convertToDateString(izinSahibi.getIseBaslamaTarihi(), FORMAT_DATE) + " den önce olamaz! [ " + izinSahibi.getAdSoyad() + " ]");
						if (PdksUtil.getDate(bitisZamani).after(sonCalismaTarihi))
							addHatalist(izinERP.getHataList(), "İzin bitiş zamanı işten ayrılma tarihi " + PdksUtil.convertToDateString(sonCalismaTarihi, FORMAT_DATE) + " den sonra olamaz! [ " + izinSahibi.getAdSoyad() + " ]");
						IzinTipi izinTipi = izinTipiMap.get(izinERP.getIzinTipi());
						List<PersonelDenklestirme> kapaliDenklestirmeler = null;
						if (!izinERP.getHataList().isEmpty())
							kapaliDenklestirmeler = getDenklestirmeList(izinSahibi != null ? izinSahibi.getPdksSicilNo() : null, baslangicZamani, bitisZamani, false);
						boolean donemKapali = false;
						if (kapaliDenklestirmeler != null && !kapaliDenklestirmeler.isEmpty()) {
							StringBuffer donemStr = new StringBuffer();
							donemKapali = true;
							for (Iterator iterator2 = kapaliDenklestirmeler.iterator(); iterator2.hasNext();) {
								PersonelDenklestirme personelDenklestirme = (PersonelDenklestirme) iterator2.next();
								DenklestirmeAy denklestirmeAy = personelDenklestirme.getDenklestirmeAy();
								donemStr.append(denklestirmeAy.getAyAdi() + " " + denklestirmeAy.getYil());
								if (iterator2.hasNext())
									donemStr.append(", ");
								if (personelDenklestirme.getDurum().equals(Boolean.FALSE))
									iterator2.remove();
								else {
									personelDenklestirme.setDurum(Boolean.FALSE);
								}
							}
							if (!kapaliDenklestirmeler.isEmpty())
								hataList.add(izinERP);
							else
								izinERP.setDurum(null);
							String str = donemStr.toString();
							addHatalist(izinERP.getHataList(), str + " " + (kapaliDenklestirmeler.size() > 1 ? " dönemleri" : " dönemi") + " kapalıdır");
						}

						if (izinTipi != null && (donemKapali || izinERP.getHataList().isEmpty())) {

							Boolean izinDegisti = null;
							if (!mailEkle && personelIzin.getId() != null) {
								boolean izinDurum = personelIzin.getIzinDurumu() == PersonelIzin.IZIN_DURUMU_ONAYLANDI;
								izinDegisti = !izinTipi.getId().equals(personelIzin.getIzinTipi().getId()) || izinDurum != izinERP.getDurum().booleanValue() || baslangicZamani.getTime() != personelIzin.getBaslangicZamani().getTime()
										|| bitisZamani.getTime() != personelIzin.getBitisZamani().getTime();
								mailEkle = izinDegisti && doktor;
							}

							if ((izinERP.getSureBirimi() == null && izinTipi.getHesapTipi() != null && izinTipi.getHesapTipi().equals(PersonelIzin.HESAP_TIPI_GUN)) || izinERP.getSureBirimi().value().equals(SureBirimi.GUN.value())) {
								if (izinServisGun != null) {
									Date t1 = PdksUtil.getDate(baslangicZamani), t2 = PdksUtil.getDate(bitisZamani);
									VardiyaSablonu vardiyaSablonu = beyazYakaDefaultVardiyaSablonu != null ? beyazYakaDefaultVardiyaSablonu : izinSahibi.getSablon();
									Vardiya vardiya = vardiyaSablonu.getVardiya1();
									if (t1.getTime() == baslangicZamani.getTime()) {
										fields.clear();
										fields.put("personel.id", izinSahibi.getId());
										fields.put("vardiyaDate", t1);
										VardiyaGun vardiyaGun = (VardiyaGun) pdksDAO.getObjectByInnerObject(fields, VardiyaGun.class);
										if (vardiyaGun == null)
											vardiyaGun = new VardiyaGun(izinSahibi, vardiya, t1);
										vardiyaGun.setVardiyaZamani();
										Vardiya islemVardiya = vardiyaGun.getIslemVardiya(), vardiya2 = vardiyaGun.getVardiya();
										baslangicZamani = islemVardiya.getVardiyaBasZaman();
										if (vardiya2.isHaftaTatil() || (vardiyaGun.getId() == null && PdksUtil.getDateField(t2, Calendar.DAY_OF_WEEK) == Calendar.SUNDAY))
											baslangicZamani = PdksUtil.tariheGunEkleCikar(baslangicZamani, 1);
										vardiyaGun = null;
									}
									if (t2.getTime() == bitisZamani.getTime()) {
										Date vardiyaDate = PdksUtil.tariheGunEkleCikar(t2, -izinServisGun);
										fields.clear();
										fields.put("personel.id", izinSahibi.getId());
										fields.put("vardiyaDate", vardiyaDate);
										VardiyaGun vardiyaGun = (VardiyaGun) pdksDAO.getObjectByInnerObject(fields, VardiyaGun.class);
										if (vardiyaGun == null)
											vardiyaGun = new VardiyaGun(izinSahibi, vardiya, vardiyaDate);
										vardiyaGun.setVardiyaZamani();
										Vardiya islemVardiya = vardiyaGun.getIslemVardiya(), vardiya2 = vardiyaGun.getVardiya();
										bitisZamani = islemVardiya.getVardiyaBitZaman();
										if (vardiya2.isHaftaTatil() || (vardiyaGun.getId() == null && PdksUtil.getDateField(t2, Calendar.DAY_OF_WEEK) == Calendar.MONDAY))
											bitisZamani = PdksUtil.tariheGunEkleCikar(bitisZamani, -1);
										vardiyaGun = null;
									}
								}
							}
							if (izinlerBasTarih == null || baslangicZamani.before(izinlerBasTarih))
								izinlerBasTarih = baslangicZamani;
							if (izinlerBitTarih == null || bitisZamani.after(izinlerBitTarih))
								izinlerBitTarih = bitisZamani;
							if (personelIzin.getId() != null && (olusturmaTarih == null || olusturmaTarih.before(personelIzin.getOlusturmaTarihi())))
								olusturmaTarih = personelIzin.getOlusturmaTarihi();
							personelIzin.setBaslangicZamani(baslangicZamani);
							personelIzin.setBitisZamani(bitisZamani);
							if (izinERP.getDurum().booleanValue() || personelIzin.getId() != null) {
								Date bitTarih = PdksUtil.getDate(bitisZamani);
								if (izinVardiyaKontrol != 0)
									bitTarih = PdksUtil.tariheGunEkleCikar(bitTarih, -izinVardiyaKontrol);
								if (!izinSahibi.isCalisiyorGun(bitTarih) && personelIzin.getId() == null)
									addHatalist(izinERP.getHataList(), PdksUtil.convertToDateString(personelIzin.getBitisZamani(), FORMAT_DATE_TIME) + " tarihinde çalışmıyor!!");
								if (izinERP.getDurum().booleanValue() || donemKapali) {
									fields.clear();
									StringBuffer sb = new StringBuffer();
									sb.append(" SELECT R." + IzinReferansERP.COLUMN_NAME_ID + ",I." + PersonelIzin.COLUMN_NAME_ID + " AS " + IzinReferansERP.COLUMN_NAME_IZIN_ID + " FROM " + PersonelIzin.TABLE_NAME + " I WITH(nolock) ");
									sb.append(" LEFT JOIN  " + IzinReferansERP.TABLE_NAME + " R ON I." + PersonelIzin.COLUMN_NAME_ID + " =R." + IzinReferansERP.COLUMN_NAME_IZIN_ID);
									sb.append(" WHERE I." + PersonelIzin.COLUMN_NAME_PERSONEL + " = " + izinSahibi.getId() + " AND I." + PersonelIzin.COLUMN_NAME_IZIN_DURUMU + " <> " + PersonelIzin.IZIN_DURUMU_REDEDILDI);
									sb.append(" AND I." + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + " < :b2 AND I." + PersonelIzin.COLUMN_NAME_BITIS_ZAMANI + " > :b1");
									if (personelIzin.getId() != null)
										sb.append(" AND I." + PersonelIzin.COLUMN_NAME_ID + " <> " + personelIzin.getId());
									fields.put("b1", personelIzin.getBaslangicZamani());
									fields.put("b2", personelIzin.getBitisZamani());
									List<IzinReferansERP> kayitList = null;
									try {
										kayitList = pdksDAO.getNativeSQLList(fields, sb, IzinReferansERP.class);
									} catch (Exception ek) {
										logger.error(ek);
										ek.printStackTrace();
									}

									if (kayitList != null && !kayitList.isEmpty()) {
										IzinReferansERP izinReferansErp = kayitList.get(0);
										PersonelIzin digerIzin = izinReferansErp != null ? izinReferansErp.getIzin() : null;
										if (digerIzin != null) {
											logger.debug(izinERP.getPersonelNo() + " " + izinERP.getReferansNoERP() + " [ " + izinERP.getBasZaman() + " - " + izinERP.getBitZaman() + " ]");
											String basStr = PdksUtil.convertToDateString(digerIzin.getBaslangicZamani(), FORMAT_DATE_TIME), bitStr = PdksUtil.convertToDateString(digerIzin.getBitisZamani(), FORMAT_DATE_TIME);
											if (!basStr.equals(izinERP.getBitZaman()) && !bitStr.equals(izinERP.getBasZaman())) {
												boolean hataVar = !(izinERP.getBasZaman().compareTo(basStr) != 1 && izinERP.getBitZaman().compareTo(bitStr) != -1) || !izinPersonelERPMap.containsKey(izinERP.getPersonelNo());
												if (hataVar)
													addHatalist(izinERP.getHataList(), basStr + " - " + bitStr + " kayıtlı izin vardır!!");
												else {
													personelIzin = digerIzin;
													izinDegisti = true;
													if (izinReferansErp.getId() == null || !izinReferansErp.getId().equals(izinERP.getReferansNoERP())) {
														if (izinReferansErp.getId() != null)
															deleteList.add(izinReferansErp);
														izinReferansERP.setIzin(personelIzin);
														saveList.add(izinReferansERP);
													}

												}
											} else {
												IzinReferansERP izinReferansERP2 = (IzinReferansERP) pdksDAO.getObjectByInnerObject("izin.id", digerIzin.getId(), IzinReferansERP.class);
												if (izinReferansERP2 != null) {
													digerIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_REDEDILDI);
													digerIzin.setGuncelleyenUser(islemYapan);
													digerIzin.setGuncellemeTarihi(islemZamani);
													saveList.add(digerIzin);
												}
											}
										} else {
											logger.debug("hata : " + izinERP.getBasZaman() + " - " + izinERP.getBitZaman() + "\n" + sb.toString());
										}
									}
								}
								if (izinERP.getHataList().isEmpty() || donemKapali) {
									saveList.add(personelIzin);
									if (personelIzin.getId() == null) {
										personelIzin.setOlusturanUser(islemYapan);
										personelIzin.setOlusturmaTarihi(islemZamani);
										saveList.add(izinReferansERP);
										if (donemKapali && kapaliDenklestirmeler != null)
											saveList.addAll(kapaliDenklestirmeler);
									} else if (izinDegisti != null && izinDegisti) {
										personelIzin.setGuncellemeTarihi(islemZamani);
										personelIzin.setGuncelleyenUser(islemYapan);
									}

									personelIzin.setIzinTipi(izinTipi);
									Double izinSuresi = izinERP.getIzinSuresi();
									String aciklama = izinERP.getAciklama() != null ? izinERP.getAciklama() : izinTipi.getMesaj();
									personelIzin.setAciklama((aciklama != null ? aciklama.trim() : "") + " ( " + uygulamaBordro + " referans no : " + izinERP.getReferansNoERP() + " )");
									Integer hesapTipi = null;
									if (izinERP.getSureBirimi() != null) {
										hesapTipi = Integer.parseInt(izinERP.getSureBirimi().value());
										if (izinTipi.getHesapTipi() != null && hesapTipi.intValue() != izinTipi.getHesapTipi().intValue()) {
											switch (hesapTipi) {
											case 1:
												izinSuresi = izinSuresi * 24d;
												break;
											case 2:
												izinSuresi = izinSuresi / 24d;
												break;
											default:
												break;
											}
											hesapTipi = izinTipi.getHesapTipi();
										}
									} else
										hesapTipi = izinTipi.getHesapTipi();
									personelIzin.setIzinSuresi(izinSuresi);
									personelIzin.setHesapTipi(hesapTipi);
									personelIzin.setIzinDurumu(izinERP.getDurum() != null && izinERP.getDurum() ? PersonelIzin.IZIN_DURUMU_ONAYLANDI : PersonelIzin.IZIN_DURUMU_REDEDILDI);
									List<PersonelDenklestirme> acikDenklestirmeler = getDenklestirmeList(izinSahibi != null ? izinSahibi.getPdksSicilNo() : null, baslangicZamani, bitisZamani, true);
									if (acikDenklestirmeler != null && !acikDenklestirmeler.isEmpty()) {
										for (PersonelDenklestirme personelDenklestirme : acikDenklestirmeler) {
											if (personelDenklestirme.getDurum().equals(Boolean.FALSE))
												continue;
											personelDenklestirme.setDurum(Boolean.FALSE);
											saveList.add(personelDenklestirme);
										}
									}
									try {
										if (listeKaydet(izinERP.getReferansNoERP(), saveList, deleteList)) {
											if (personelIzin.getIzinDurumu() != PersonelIzin.IZIN_DURUMU_REDEDILDI && personelIzin.getIzinDurumu() != PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL) {
												kayitIzinList.add(izinERP.getReferansNoERP());
											}
											if (mailEkle)
												izinMap.put(izinReferansERP.getId(), izinReferansERP.getIzin());
											izinERP.setYazildi(Boolean.TRUE);
											izinERP.setId(personelIzin.getId());
											izinERP.setHataList(null);
										}
									} catch (Exception e) {
										if (e.getMessage() != null)
											addHatalist(izinERP.getHataList(), e.getMessage());
										else
											addHatalist(izinERP.getHataList(), "Hata oluştu!");
									}

								} else if (!izinERP.getHataList().isEmpty())
									hataList.add((IzinERP) izinERP.clone());
								kapaliDenklestirmeler = null;
							} else
								addHatalist(izinERP.getHataList(), "İptal yeni kayıt sisteme yazılmadı!");
						}
					} else if (!izinERP.getHataList().isEmpty()) {
						hataList.add((IzinERP) izinERP.clone());
					}
					izinERP.setAciklama(null);
					if (izinERP.getHataList().isEmpty()) {
						izinERP.setDurum(null);
						izinERP.setIzinTipiAciklama(null);
						izinERP.setBasZaman(null);
						izinERP.setBitZaman(null);
					}
				}
				Integer gecmisIzinKontrolAdet = null;
				try {
					if (mailMap.containsKey("gecmisIzinKontrolAdet"))
						gecmisIzinKontrolAdet = Integer.parseInt((String) mailMap.get("gecmisIzinKontrolAdet"));
					else {
						kayitIzinList.clear();
						gecmisIzinKontrolAdet = kayitIzinList.size() + 1;
					}

				} catch (Exception e) {
					gecmisIzinKontrolAdet = null;
				}
				if (gecmisIzinKontrolAdet == null) {
					kayitIzinList.clear();
					gecmisIzinKontrolAdet = kayitIzinList.size() + 1;
				}
				if (kayitIzinList.size() >= gecmisIzinKontrolAdet && izinlerBasTarih != null && izinlerBitTarih != null && izinlerBasTarih.before(izinlerBitTarih)) {
					Date tarih = PdksUtil.tariheAyEkleCikar(bugun, 2);
					Date bTarih = PdksUtil.convertToJavaDate(PdksUtil.convertToDateString(PdksUtil.tariheAyEkleCikar(bugun, -2), "yyyyMM") + "01", "yyyyMMdd");
					if (izinlerBasTarih.before(bTarih))
						izinlerBasTarih = bTarih;
					if (izinlerBitTarih.after(tarih))
						izinlerBitTarih = tarih;
					HashMap map = new HashMap();
					StringBuffer sb = new StringBuffer();
					sb.append("SELECT  R." + IzinReferansERP.COLUMN_NAME_ID + ",I." + PersonelIzin.COLUMN_NAME_PERSONEL_NO + ",R." + IzinReferansERP.COLUMN_NAME_IZIN_ID + "   FROM  " + PersonelIzin.TABLE_NAME + "  I WITH(nolock)");
					sb.append(" INNER JOIN " + IzinReferansERP.TABLE_NAME + " R ON R." + IzinReferansERP.COLUMN_NAME_IZIN_ID + "=I." + PersonelIzin.COLUMN_NAME_ID);
					sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + "=I." + PersonelIzin.COLUMN_NAME_PERSONEL);
					sb.append(" AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=:b");
					sb.append(" WHERE I." + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + "<=:b2 AND I." + PersonelIzin.COLUMN_NAME_BITIS_ZAMANI + ">=:b1 ");
					sb.append(" AND I." + PersonelIzin.COLUMN_NAME_IZIN_DURUMU + "<>:d1 AND  I." + PersonelIzin.COLUMN_NAME_IZIN_DURUMU + "<>:d2  ");
					sb.append(" AND P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + "< I." + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + "");
					if (olusturmaTarih != null) {
						sb.append(" AND I." + Personel.COLUMN_NAME_OLUSTURMA_TARIHI + "<=:o");
						map.put("o", PdksUtil.getDate(olusturmaTarih));
					}
					sb.append(" ORDER BY I." + PersonelIzin.COLUMN_NAME_PERSONEL_NO + ",I." + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI);
					map.put("b", PdksUtil.getDate(bugun));
					map.put("b1", izinlerBasTarih);
					map.put("b2", izinlerBitTarih);
					map.put("d1", PersonelIzin.IZIN_DURUMU_REDEDILDI);
					map.put("d2", PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL);
					List<Object[]> list = pdksDAO.getNativeSQLList(map, sb, null);
					map.clear();
					for (Object[] objects : list) {
						String ref = (String) objects[0];
						if (!kayitIzinList.contains(ref)) {
							map.put(((BigDecimal) objects[2]).longValue(), objects[1]);
						}
					}
					if (!map.isEmpty()) {
						List<IzinReferansERP> personelIzinList = pdksDAO.getObjectByInnerObjectList("izin.id", new ArrayList<Long>(map.keySet()), IzinReferansERP.class);
						if (!personelIzinList.isEmpty()) {
							if (mailMap.containsKey("acikAyGelmeyenIzinERPIptal")) {
								List<Long> idList = new ArrayList<Long>();
								for (IzinReferansERP izinReferansERP : personelIzinList) {
									PersonelIzin personelIzin = izinReferansERP.getIzin();
									if (PdksUtil.convertToDateString(personelIzin.getBaslangicZamani(), "yyyyMM").equals(PdksUtil.convertToDateString(personelIzin.getBitisZamani(), "yyyyMM")))
										idList.add(personelIzin.getId());
								}
								if (!idList.isEmpty()) {
									fields.clear();
									sb = new StringBuffer();
									sb.append("SELECT I." + PersonelIzin.COLUMN_NAME_ID + ",P." + PersonelDenklestirme.COLUMN_NAME_ID + " AS PD_ID FROM " + PersonelIzin.TABLE_NAME + " I WITH(nolock)");
									sb.append(" INNER JOIN  " + DenklestirmeAy.TABLE_NAME + " D ON D." + DenklestirmeAy.COLUMN_NAME_YIL + "= YEAR(I." + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + ")");
									sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_AY + "= MONTH(I." + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + ") AND D." + DenklestirmeAy.COLUMN_NAME_DURUM + "=1");
									sb.append(" INNER JOIN  " + PersonelDenklestirme.TABLE_NAME + " P ON P." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=D." + DenklestirmeAy.COLUMN_NAME_ID);
									sb.append(" AND P." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + "=I." + PersonelIzin.COLUMN_NAME_PERSONEL);
									sb.append(" WHERE I." + PersonelIzin.COLUMN_NAME_ID + " :p");
									sb.append(" ORDER BY I." + PersonelIzin.COLUMN_NAME_BITIS_ZAMANI + " DESC");
									fields.put("p", idList);
									List<Object[]> izinAcikList = pdksDAO.getNativeSQLList(fields, sb, null);
									if (!izinAcikList.isEmpty()) {
										List saveIzinList = new ArrayList();
										Date guncellemeTarihi = new Date();
										for (Object[] objects : izinAcikList) {
											Long izinId = ((BigDecimal) objects[0]).longValue(), pdId = ((BigDecimal) objects[1]).longValue();
											PersonelDenklestirme personelDenklestirme = (PersonelDenklestirme) pdksDAO.getObjectByInnerObject("id", pdId, PersonelDenklestirme.class);
											personelDenklestirme.setDurum(Boolean.FALSE);
											saveIzinList.add(personelDenklestirme);
											for (Iterator iterator = personelIzinList.iterator(); iterator.hasNext();) {
												IzinReferansERP izinErp = (IzinReferansERP) iterator.next();
												PersonelIzin izin = izinErp.getIzin();
												if (izin.getId().equals(izinId)) {
													izin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL);
													izin.setGuncellemeTarihi(guncellemeTarihi);
													izin.setGuncelleyenUser(islemYapan);
													saveIzinList.add(izin);
													iterator.remove();
													break;
												}
											}
										}
										if (!saveIzinList.isEmpty())
											pdksDAO.saveObjectList(saveIzinList);
										saveIzinList = null;
									}
									izinAcikList = null;
								}
								idList = null;
							}

						}
						if (!personelIzinList.isEmpty()) {
							personelIzinList = PdksUtil.sortListByAlanAdi(personelIzinList, "sortAlan", Boolean.TRUE);
							mailMap.put("konu", uygulamaBordro + " gelmeyen izinler");
							sb = new StringBuffer();
							sb.append("<p><b>" + mailMap.get("konu") + " var!</b></p>");
							sb.append("<TABLE class=\"mars\" style=\"width: 90%\">");
							boolean renkUyari = true;
							sb.append("<THEAD><TR><TH>" + personelNoAciklama() + "</TH>");
							sb.append("<TH>Adı Soyadı</TH>");
							sb.append("<TH>Tipi</TH><TH>Başlangıç Zamanı</TH>");
							sb.append("<TH>Bitiş Zamanı</TH>");
							sb.append("<TH>" + uygulamaBordro + " Referans No</TH></TR></THEAD><TBODY>");
							for (IzinReferansERP personelIzinTum : personelIzinList) {
								PersonelIzin personelIzin = personelIzinTum.getIzin();
								sb.append("<TR class=\"" + (renkUyari ? "odd" : "even") + "\">");
								sb.append("<TD align=\"center\">" + personelIzin.getPersonelNo() + "</TD>");
								sb.append("<TD>" + personelIzin.getIzinSahibi().getAdSoyad() + "</TD>");
								sb.append("<TD >" + (personelIzin.getIzinTipi() != null ? personelIzin.getIzinTipi().getIzinTipiTanim().getAciklamatr() : "") + "</TD>");
								sb.append("<TD align=\"center\">" + PdksUtil.convertToDateString(personelIzin.getBaslangicZamani(), FORMAT_DATE_TIME) + "</TD>");
								sb.append("<TD align=\"center\">" + PdksUtil.convertToDateString(personelIzin.getBitisZamani(), FORMAT_DATE_TIME) + "</TD>");
								sb.append("<TD >" + (personelIzinTum.getId() != null ? personelIzinTum.getId().trim() : "") + "</TD></TR>");
								renkUyari = !renkUyari;
								logger.debug(personelIzin.getPersonelNo() + " | " + PdksUtil.convertToDateString(personelIzin.getBaslangicZamani(), FORMAT_DATE_TIME) + " | " + PdksUtil.convertToDateString(personelIzin.getBitisZamani(), FORMAT_DATE_TIME) + " | " + personelIzin.getAciklama() + " | "
										+ PdksUtil.convertToDateString(personelIzin.getOlusturmaTarihi(), FORMAT_DATE_TIME) + " | " + personelIzin.getId());
							}
							sb.append("</TBODY></TABLE>");
							mailMap.put("mailIcerik", sb.toString());
							if (testDurum)
								mailMap.put("ikMailIptal", testDurum);
							mailMapGuncelle("bccEntegrasyon", "bccEntegrasyonAdres");
							kullaniciIKYukle(mailMap, pdksDAO);
							MailManager.ePostaGonder(mailMap);
						}
						personelIzinList = null;
					}
					map = null;
				}
				kayitIzinList = null;
				if (testDurum)
					hataList.clear();
				if (hataList != null) {
					for (Iterator iterator = hataList.iterator(); iterator.hasNext();) {
						IzinERP izinERP = (IzinERP) iterator.next();
						if (izinERP.getHataList().isEmpty())
							iterator.remove();
					}
					for (Iterator iterator = hataList.iterator(); iterator.hasNext();) {
						IzinERP izinERP = (IzinERP) iterator.next();
						if (izinERP == null || izinERP.getDurum() == null)
							iterator.remove();
					}
					if (!hataList.isEmpty()) {
						try {
							Gson gson = new Gson();
							String jsonStr = null;
							try {
								jsonStr = PdksUtil.toPrettyFormat(gson.toJson(hataList));
							} catch (Exception e) {
								jsonStr = null;
							}
							String konu = uygulamaBordro + " saveIzinler problem", konuEk = "";
							if (hataList.size() == 1 && personelMap != null) {
								IzinERP izinERP = hataList.get(0);
								if (PdksUtil.hasStringValue(izinERP.getPersonelNo()) && personelMap.containsKey(izinERP.getPersonelNo())) {
									Personel izinSahibi = personelMap.get(izinERP.getPersonelNo());
									konuEk = " [ " + izinSahibi.getAd() + " ]";
								}
							}
							mailMap.put("konu", konu + konuEk);
							StringBuffer sb = new StringBuffer();
							sb.append("<p><b>" + uygulamaBordro + " pdks entegrasyon servisi saveIzinler fonksiyonunda hatalı veri var!</b></p>");
							sb.append("<TABLE class=\"mars\" style=\"width: 80%\">");
							boolean gonder = false, renkUyari = true;
							for (Iterator iterator = hataList.iterator(); iterator.hasNext();) {
								IzinERP izinERP = (IzinERP) iterator.next();
								gonder = true;
								String zaman = (izinERP.getBasZaman() != null ? izinERP.getBasZaman().trim() + " - " : "") + (izinERP.getBitZaman() != null ? izinERP.getBitZaman() + " " : " ") + (izinERP.getIzinTipiAciklama() != null ? izinERP.getIzinTipiAciklama() : " ");
								Personel izinSahibi = izinERP.getPersonelNo() != null && personelMap.containsKey(izinERP.getPersonelNo()) ? personelMap.get(izinERP.getPersonelNo()) : null;
								String personelBilgisi = izinSahibi != null ? izinERP.getPersonelNo() + " - " + izinSahibi.getAdSoyad() + " " : null;
								if (personelBilgisi == null)
									personelBilgisi = izinERP.getPersonelNo() != null ? izinERP.getPersonelNo() + " " : "";
								sb.append("<TR class=\"" + (renkUyari ? "odd" : "even") + "\"><TD colspan=2><b>" + personelBilgisi + zaman + "</b></TD></TR>");
								List<String> veriHataList = izinERP.getHataList();
								for (int i = 0; i < veriHataList.size(); i++) {
									sb.append("<TR class=\"" + (renkUyari ? "odd" : "even") + "\"><TD width='10%'></TD><TD width='90%'>" + (veriHataList.size() > 1 ? (i + 1) + ". " : "") + veriHataList.get(i) + "</TD></TR>");
								}
								renkUyari = !renkUyari;
								if (iterator.hasNext())
									sb.append("<TR><TD colspan=2> </TD></TR>");
								izinERP.setIzinTipiAciklama(null);
								izinERP.setBasZaman(null);
								izinERP.setBitZaman(null);
							}
							sb.append("</TABLE>");
							if (gonder) {
								mailMap.put("mailIcerik", sb.toString());
								if (jsonStr != null) {
									LinkedHashMap<String, Object> fileMap = new LinkedHashMap<String, Object>();
									String str = getJsonToXML(jsonStr, "izin", IZIN_PROP_ORDER, "saveIzinler");
									fileMap.put("saveIzinler.xml", str);
									mailMap.put("fileMap", fileMap);
								}
								mailMapGuncelle("bccEntegrasyon", "bccEntegrasyonAdres");
								kullaniciIKYukle(mailMap, pdksDAO);
								MailManager.ePostaGonder(mailMap);
								mailBosGonder = false;
							}
						} catch (Exception em) {
							logger.error(em);
							em.printStackTrace();
						}
					}

				}
				if (!izinMap.isEmpty()) {
					List<Long> personelIdList = new ArrayList<Long>();
					for (String key : izinMap.keySet()) {
						Long personelId = izinMap.get(key).getIzinSahibi().getId();
						if (!personelIdList.contains(personelId))
							personelIdList.add(personelId);
					}
					TreeMap<Long, Personel> doktorUserMap = pdksDAO.getObjectByInnerObjectMap("getId", "id", personelIdList, Personel.class, false);
					if (!doktorUserMap.isEmpty()) {
						if (mailMap.containsKey("toEntegrasyonAdres"))
							mailMap.remove("toEntegrasyonAdres");
						if (mailMap.containsKey("ccEntegrasyonAdres"))
							mailMap.remove("ccEntegrasyonAdres");
						if (mailMap.containsKey("bccEntegrasyonAdres"))
							mailMap.remove("bccEntegrasyonAdres");
						for (String key : izinMap.keySet()) {
							PersonelIzin izin = izinMap.get(key);
							Personel izinSahibi = izin.getIzinSahibi();
							Long personelId = izin.getIzinSahibi().getId();
							if (doktorUserMap.containsKey(personelId) && (izinSahibi.getEmailCC() != null || izinSahibi.getEmailBCC() != null)) {
								// User doktorUser = doktorUserMap.get(personelId);
								String cc = izinSahibi.getEmailCC(), bcc = izinSahibi.getEmailBCC();
								if ((cc != null && cc.indexOf("@") > 0) || (bcc != null && bcc.indexOf("@") > 0)) {
									try {
										boolean gonder = false;
										if (mailMap.containsKey("mailAdresleriCC"))
											mailMap.remove("mailAdresleriCC");
										if (mailMap.containsKey("mailAdresleriBCC"))
											mailMap.remove("mailAdresleriBCC");
										if (cc != null && cc.indexOf("@") > 0) {
											List<String> list = PdksUtil.getListByString(cc, null);
											for (Iterator iterator = list.iterator(); iterator.hasNext();) {
												String string = (String) iterator.next();
												if (string.indexOf("@") < 1)
													iterator.remove();
											}
											if (!list.isEmpty()) {
												gonder = true;
												mailMap.put("mailAdresleriCC", list);
											}

										}
										if (bcc != null && bcc.indexOf("@") > 0) {
											List<String> list = PdksUtil.getListByString(bcc, null);
											for (Iterator iterator = list.iterator(); iterator.hasNext();) {
												String string = (String) iterator.next();
												if (string.indexOf("@") < 1)
													iterator.remove();
											}
											if (!list.isEmpty()) {
												gonder = true;
												mailMap.put("mailAdresleriBCC", list);
											}
										}
										if (gonder) {
											mailMap.put("konu", uygulamaBordro + " " + izinSahibi.getAdSoyad() + " izin " + (!izin.isRedmi() ? " girişi" : "iptali"));
											StringBuffer sb = new StringBuffer();
											sb.append("<p><b>" + izinSahibi.getAdSoyad() + " izin " + (!izin.isRedmi() ? " girişi " : " iptali ") + " yapıldı.</b></p>");
											sb.append("<p>" + PdksUtil.convertToDateString(izin.getBaslangicZamani(), FORMAT_DATE_TIME) + " - " + PdksUtil.convertToDateString(izin.getBitisZamani(), FORMAT_DATE_TIME) + " arası izinlidir.</p>");
											sb.append("<p></p>");
											sb.append("<p>Saygılarımla</p>");
											mailMap.put("mailIcerik", sb.toString());
											mailMapGuncelle("bccEntegrasyon", "bccEntegrasyonAdres");
											kullaniciIKYukle(mailMap, pdksDAO);
											MailManager.ePostaGonder(mailMap);
										} else
											doktorUserMap.remove(personelId);

									} catch (Exception em) {
										logger.equals(em);
										em.printStackTrace();
									}

								} else
									doktorUserMap.remove(personelId);
							}

						}
					}
					doktorUserMap = null;
				}
				if (mailBosGonder)
					mailBosGonder("saveIzinler", "izin", izinList);
				hataList = null;
				saveFonksiyonVeri(null, izinList);

				mesajInfoYaz("saveIzinler --> " + mesaj + " out " + new Date());
			} else {
				for (IzinERP izinERP : izinList) {
					izinERP.getHataList().add(servisAdi + " servisi  kapalıdır!");
				}
			}
		}
	}

	private String getIzinKisaAciklama(String aciklama) {
		String kisaAciklama = "";
		if (PdksUtil.hasStringValue(aciklama)) {
			StringTokenizer st = new StringTokenizer(aciklama, " ");
			while (st.hasMoreTokens()) {
				String tk = st.nextToken();
				kisaAciklama += tk.substring(0, 1).toUpperCase(Constants.TR_LOCALE);

			}
		}
		return kisaAciklama;
	}

	/**
	 * 
	 */
	private void testMailObject(MailObject mailObject) {
		MailPersonel mailPersonel = new MailPersonel();
		mailPersonel.setAdiSoyadi("Hasan Sayar");
		mailPersonel.setePosta("hasansayar58@gmail.com");
		mailObject.getToList().add(mailPersonel);
		// String subject = mailMap.containsKey("konu") ? (String) mailMap.get("konu") : "Konu tanımsız!";
		// String body = mailMap.containsKey("mailIcerik") ? (String) mailMap.get("mailIcerik") : "";
		// mailObject.setBody(body);
		// mailObject.setSubject(subject);
		// mailMap.put("mailObject", mailObject);
	}

	/**
	 * @param perNo
	 * @param basTarih
	 * @param bitTarih
	 * @param donemDurum
	 * @return
	 */
	protected List<PersonelDenklestirme> getDenklestirmeList(String perNo, Date basTarih, Date bitTarih, boolean donemDurum) {
		List<PersonelDenklestirme> list = null;
		if (basTarih != null && bitTarih != null && PdksUtil.hasStringValue(perNo)) {
			String d1 = PdksUtil.convertToDateString(basTarih, "yyyyMM"), d2 = PdksUtil.convertToDateString(bitTarih, "yyyyMM");
			HashMap fields = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("WITH DENKAY AS ( ");
			sb.append(" SELECT " + DenklestirmeAy.COLUMN_NAME_YIL + "*100+" + DenklestirmeAy.COLUMN_NAME_AY + " AS DONEM,* FROM " + DenklestirmeAy.TABLE_NAME + " WITH(nolock) ");
			sb.append("	 WHERE " + DenklestirmeAy.COLUMN_NAME_DURUM + "=" + (donemDurum ? "1" : "0"));
			sb.append(" ) ");
			sb.append(" SELECT  PD.* FROM  DENKAY D WITH(nolock) ");
			sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON  P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + "=:p ");
			sb.append(" INNER JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON P." + Personel.COLUMN_NAME_ID + "=PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + "  AND PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=D." + DenklestirmeAy.COLUMN_NAME_ID);
			sb.append(" AND PD." + PersonelDenklestirme.COLUMN_NAME_DURUM + "=1");
			sb.append(" WHERE  D.DONEM>=:d1 AND  D.DONEM<=:d2 ");
			sb.append(" ORDER BY D.DONEM");
			fields.put("d1", Long.parseLong(d1));
			fields.put("d2", Long.parseLong(d2));
			fields.put("p", perNo);
			list = pdksDAO.getNativeSQLList(fields, sb, PersonelDenklestirme.class);
		} else if (donemDurum)
			list = new ArrayList<PersonelDenklestirme>();

		return list;
	}

	/**
	 * @param key
	 * @param value
	 * @param veriSorguMap
	 */
	private void veriIsle(String key, Object value, TreeMap<String, List<String>> veriSorguMap) {
		if (value != null) {
			List<String> list = veriSorguMap.containsKey(key) ? veriSorguMap.get(key) : null;
			String str = null;
			if (value instanceof String) {
				String deger = ((String) value).trim();
				if (deger.length() > 0)
					str = deger; // str = "'" + deger + "'";

			} else
				str = value.toString();
			if (str != null) {
				if (list == null) {
					list = new ArrayList<String>();
					veriSorguMap.put(key, list);
				}
				if (!list.contains(str))
					list.add(str);
			}
		}

	}

	/**
	 * @param sirketKodu
	 * @param parentKey
	 * @param tanimKodu
	 * @param aciklama
	 * @param dataMap
	 * @param saveList
	 * @return
	 */
	private Tanim getTanim(String sirketKodu, String parentKey, String tanimKodu, String aciklama, TreeMap<String, TreeMap> dataMap, List saveList) {
		Tanim tanim = null;
		try {
			boolean ekle = false;
			if (PdksUtil.hasStringValue(tanimKodu) && PdksUtil.hasStringValue(aciklama)) {
				TreeMap<String, Tanim> personelEKSahaMap = dataMap.get("personelEKSahaMap");
				Tanim parentTanim = personelEKSahaMap.containsKey(parentKey) ? personelEKSahaMap.get(parentKey) : null;
				String tipi = parentTanim != null ? Tanim.TIPI_PERSONEL_EK_SAHA_ACIKLAMA : parentKey;
				TreeMap<String, Tanim> personelEKSahaVeriMap = dataMap.get("personelEKSahaVeriMap");
				String key = parentKey + "_" + (sirketKodu != null ? sirketKodu : "") + tanimKodu;
				tanim = personelEKSahaVeriMap.containsKey(key) ? personelEKSahaVeriMap.get(key) : new Tanim();
				if (tanim.getId() == null && parentTanim == null) {
					if (genelTanimMap.containsKey(tipi))
						parentTanim = genelTanimMap.get(tipi);
				}
				if (!tanim.isGuncellendi()) {
					if (tanim.getId() == null) {
						tanim.setParentTanim(parentTanim);
						tanim.setTipi(tipi);
						tanim.setKodu((sirketKodu != null ? sirketKodu : "") + tanimKodu);
						tanim.setErpKodu(tanimKodu);
						tanim.setGuncelle(Boolean.FALSE);
						tanim.setIslemYapan(islemYapan);
						tanim.setIslemTarihi(new Date());
						personelEKSahaVeriMap.put(key, tanim);

					}
					ekle = tanim.getId() == null || !tanim.getDurum().booleanValue();
					if (aciklama != null) {
						if (aciklama.indexOf("&amp;") > 0)
							aciklama = PdksUtil.replaceAllManuel(aciklama, "&amp;", "&");
						if (tanim.getAciklamatr() == null || !tanim.getAciklamatr().equalsIgnoreCase(aciklama))
							ekle = true;
						tanim.setAciklamaen(aciklama);
						tanim.setAciklamatr(aciklama);

					}
					tanim.setDurum(Boolean.TRUE);
					tanim.setGuncellendi(Boolean.TRUE);
					if (ekle)
						saveList.add(tanim);
				}
				if (tanim.getParentTanim() == null && genelTanimMap != null) {
					if (genelTanimMap.containsKey(tanim.getTipi())) {
						parentTanim = genelTanimMap.get(tanim.getTipi());
						tanim.setParentTanim(parentTanim);
						tanim.setGuncelle(Boolean.FALSE);
						tanim.setIslemYapan(islemYapan);
						tanim.setIslemTarihi(new Date());
						if (!ekle)
							saveList.add(tanim);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return tanim;

	}

	/**
	 * @param tip
	 * @return
	 */
	private void tanimGetir(TreeMap<String, Tanim> map, String tip) {
		fields.clear();
		fields.put("tipi", tip);
		List<Tanim> list = pdksDAO.getObjectByInnerObjectList(fields, Tanim.class);
		for (Tanim tanim : list)
			if (!tip.equals(Tanim.TIPI_TESIS))
				map.put(tip + "_" + tanim.getErpKodu(), tanim);
			else
				map.put(tip + "_" + tanim.getKodu(), tanim);
	}

	/**
	 * @param str
	 * @return
	 */
	private boolean notEmptyStr(String str) {
		boolean empt = sistemDestekVar && PdksUtil.hasStringValue(str);
		return empt;

	}

	/**
	 * @param str
	 * @param pattern
	 * @return
	 */
	private Date getTarih(String str, String pattern) {
		Date tarih = null;
		if (str != null && str.length() == pattern.length()) {
			tarih = PdksUtil.convertToJavaDate(str, pattern);
			if (tarih != null) {
				if (minDate == null)
					minDate = PdksUtil.convertToJavaDate("1900-01-01", FORMAT_DATE);
				if (minDate != null && tarih.before(minDate))
					tarih = null;
			}
		}

		return tarih;
	}

	/**
	 * @param str1
	 * @param str2
	 * @return
	 */
	private boolean isBenzer(String str1, String str2) {
		boolean benzer = false;
		if (PdksUtil.hasStringValue(str1) && PdksUtil.hasStringValue(str2)) {
			benzer = str1.equals(str2);
			if (!benzer) {
				String strNew1 = PdksUtil.replaceAllManuel(PdksUtil.setTurkishStr(str1).toUpperCase(Locale.ENGLISH), " ", "");
				String strNew2 = PdksUtil.replaceAllManuel(PdksUtil.setTurkishStr(str2).toUpperCase(Locale.ENGLISH), " ", "");
				benzer = strNew1.equals(strNew2) || strNew1.indexOf(strNew2) >= 0 || strNew2.indexOf(strNew1) >= 0;
			}
		}
		if (!benzer)
			logger.debug("[ " + str1 + " <> " + str2 + " ]");
		return benzer;
	}

	/**
	 * @param personelKGS
	 * @return
	 */
	private Date getGrubaGirisTarihi(PersonelKGS personelKGS) {
		Date date = null;
		if (personelKGS != null && PdksUtil.hasStringValue(personelKGS.getKimlikNo())) {
			HashMap fields = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT  P.*   FROM  " + Personel.TABLE_NAME + "  P WITH(nolock) ");
			sb.append(" INNER JOIN " + PersonelKGS.TABLE_NAME + " K ON K." + PersonelKGS.COLUMN_NAME_ID + "=P." + Personel.COLUMN_NAME_KGS_PERSONEL);
			sb.append(" WHERE K." + PersonelKGS.COLUMN_NAME_KIMLIK_NO + "=:k ");
			sb.append(" ORDER BY P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI);
			fields.put("k", personelKGS.getKimlikNo());
			List<Personel> list = pdksDAO.getNativeSQLList(fields, sb, Personel.class);
			if (list.size() > 1)
				date = list.get(0).getIseBaslamaTarihi();
		}
		return date;
	}

	public String personelNoAciklama() {
		String personelNoAciklama = getBaslikAciklama("personelNoAciklama", "Personel No");
		return personelNoAciklama;
	}

	private String sirketAciklama() {
		String sirketAciklama = getBaslikAciklama("sirketAciklama", "Şirket");
		return sirketAciklama;
	}

	private String yoneticiAciklama() {
		String yoneticiAciklama = getBaslikAciklama("yoneticiAciklama", "Yönetici");
		return yoneticiAciklama;
	}

	/**
	 * 
	 */
	private void setIkinciYoneticiSifirla() {
		Boolean yonetici2ERPKontrol = Boolean.FALSE;
		if (mailMap.containsKey("yonetici2ERPKontrol")) {
			String str = (String) mailMap.get("yonetici2ERPKontrol");
			yonetici2ERPKontrol = str.equals("1");
		}

		if (!yonetici2ERPKontrol) {
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
			map.put(BaseDAOHibernate.MAP_KEY_SELECT, "SP_GET_IKINCI_YONETICI_UPDATE");
			pdksDAO.execSP(map);
		}

	}

	/**
	 * @param yoneticiList
	 * @param dataMap
	 * @param personelList
	 * @param tip
	 */
	private void personelVeriYaz(List<Personel> yoneticiList, TreeMap<String, TreeMap> dataMap, List<String> personelList, String tip) {
		TreeMap<String, PersonelERP> personelERPMap = dataMap.get("personelERPMap"), personelYoneticiERPMap = new TreeMap<String, PersonelERP>();
		TreeMap<String, PersonelKGS> personelKGSMap = dataMap.get("personelKGSMap");
		TreeMap<String, Personel> personelPDKSMap = dataMap.get("personelPDKSMap");
		TreeMap<String, ERPPersonel> personelERPHataliMap = dataMap.get("personelERPHataliMap");
		TreeMap<String, Personel> personelDigerMap = dataMap.get("personelDigerMap");
		TreeMap<String, Sirket> sirketMap = dataMap.get("sirketMap");
		String yoneticiAciklama = yoneticiAciklama();
		boolean yoneticiBul = tip.equals("Y"), kendiYonetici = false;
		List saveList = new ArrayList();
		HashMap<String, Boolean> map1 = new HashMap<String, Boolean>();
		String genelMudurERPKoduStr = mailMap.containsKey("genelMudurERPKodu") ? (String) mailMap.get("genelMudurERPKodu") : "";
		List<String> genelMudurERPKodlari = !PdksUtil.hasStringValue(genelMudurERPKoduStr) ? new ArrayList<String>() : PdksUtil.getListByString(genelMudurERPKoduStr, null);
		String calisanIstenAyrilmaTarihi = mailMap.containsKey("calisanIstenAyrilmaTarihi") ? (String) mailMap.get("calisanIstenAyrilmaTarihi") : "";
		String iskurManuelGirisStr = mailMap.containsKey("iskurManuelGiris") ? (String) mailMap.get("iskurManuelGiris") : "";
		String yoneticiMailKontrol = mailMap.containsKey("yoneticiMailKontrol") ? (String) mailMap.get("yoneticiMailKontrol") : "1";
		String yoneticiERPKontrol = mailMap.containsKey("yoneticiERPKontrol") ? (String) mailMap.get("yoneticiERPKontrol") : "1";
		String personelERPGecmisTarihKontrolStr = mailMap.containsKey("personelERPGecmisTarihKontrol") ? (String) mailMap.get("personelERPGecmisTarihKontrol") : "0";
		String yonetici2ERPKontrolStr = mailMap.containsKey("yonetici2ERPKontrol") ? (String) mailMap.get("yonetici2ERPKontrol") : "";
		String sistemBaslangicYiliStr = mailMap.containsKey("sistemBaslangicYili") ? (String) mailMap.get("sistemBaslangicYili") : "0";
		boolean bayanSoyadKontrol = mailMap.containsKey("bayanSoyadKontrol") && ((String) mailMap.get("bayanSoyadKontrol")).equals("1");
		boolean yonetici2ERPKontrol = yonetici2ERPKontrolStr.equals("1");
		Date calisanTarihi = PdksUtil.convertToJavaDate("99991231", "yyyyMMdd");
		boolean izinERPUpdate = false, iskurManuelGiris = iskurManuelGirisStr != null && iskurManuelGirisStr.equals("1");
		if (mailMap.containsKey("izinERPUpdate")) {
			String str = (String) mailMap.get("izinERPUpdate");
			izinERPUpdate = str.equals("1");
		}
		boolean yoneticiERP1Kontrol = true;
		if (mailMap.containsKey("yoneticiERP1Kontrol"))
			yoneticiERP1Kontrol = !PdksUtil.hasStringValue(((String) mailMap.get("yoneticiERP1Kontrol")));
		Date gecmisTarih = null;

		int personelERPGecmisTarihKontrol = -1;
		try {
			personelERPGecmisTarihKontrol = Integer.parseInt(personelERPGecmisTarihKontrolStr);
		} catch (Exception e) {
			personelERPGecmisTarihKontrol = -1;
		}
		if (personelERPGecmisTarihKontrol > 0 && !sistemBaslangicYiliStr.equals("0")) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, -personelERPGecmisTarihKontrol);
			int yil = cal.get(Calendar.YEAR);
			try {
				int sistemBaslangicYili = Integer.parseInt(sistemBaslangicYiliStr);
				if (sistemBaslangicYili < yil)
					gecmisTarih = PdksUtil.getDate(cal.getTime());
			} catch (Exception e) {
			}

		}
		List<String> kidemHataList = new ArrayList<String>();
		TreeMap<String, Tanim> personelEKSahaMap = dataMap.get("personelEKSahaMap");
		Tanim parentDepartman = personelEKSahaMap != null && personelEKSahaMap.containsKey("ekSaha1") ? personelEKSahaMap.get("ekSaha1") : null;

		fields.clear();
		fields.put("tipi", Tanim.TIPI_GENEL_TANIM);
		fields.put("kodu", Tanim.TIPI_BORDRO_ALT_BIRIMI);
		Tanim parentBordroTanim = (Tanim) pdksDAO.getObjectByInnerObject(fields, Tanim.class);
		String parentBordroTanimKodu = Tanim.TIPI_BORDRO_ALT_BIRIMI;
		parentBordroTanimKoduStr = "";
		String durumParentBordroTanimKoduStr = "";
		if (parentBordroTanim != null) {
			parentBordroTanimKoduStr = parentBordroTanim.getErpKodu();
			if (parentBordroTanimKoduStr == null)
				parentBordroTanimKoduStr = "";
			else {
				parentBordroTanimKodu = parentBordroTanimKoduStr.trim();
			}

		} else
			parentBordroTanimKoduStr = Tanim.TIPI_BORDRO_ALT_BIRIMI;
		parentBordroTanimKoduStr = parentBordroTanimKodu;
		// if (parentBordroTanimKoduStr.startsWith("eksaha"))
		// parentBordroTanimKodu = Tanim.TIPI_PERSONEL_EK_SAHA_ACIKLAMA;

		List<String> parentBordroTanimKoduList = PdksUtil.getListByString(parentBordroTanimKoduStr, null);
		if (parentBordroTanimKoduList.size() <= 2) {
			switch (parentBordroTanimKoduList.size()) {
			case 1:
				durumParentBordroTanimKoduStr = parentBordroTanimKoduList.get(0);
				break;
			case 2:
				parentBordroTanimKoduStr = parentBordroTanimKoduList.get(0);
				durumParentBordroTanimKoduStr = parentBordroTanimKoduList.get(1);
				break;
			default:
				break;
			}
		}
		ekSahaAdi = parentBordroTanimKoduStr != null ? parentBordroTanimKoduStr.toLowerCase(Constants.TR_LOCALE) : "";
		Tanim istenAyrilanEkSaha = null;
		if (PdksUtil.hasStringValue(ekSahaAdi)) {
			if (ekSahaAdi.startsWith("eksaha")) {
				Tanim parentEkSaha = personelEKSahaMap != null && personelEKSahaMap.containsKey(parentBordroTanimKoduStr) ? personelEKSahaMap.get(parentBordroTanimKoduStr) : null;
				if (parentEkSaha != null) {
					fields.clear();
					fields.put("parentTanim.id", parentEkSaha.getId());
					fields.put("tipi", Tanim.TIPI_PERSONEL_EK_SAHA_ACIKLAMA);
					fields.put("kodu", "END");
					istenAyrilanEkSaha = (Tanim) pdksDAO.getObjectByInnerObject(fields, Tanim.class);
				}
			}

		}
		altBolumDurum = ekSahaAdi.startsWith("eksaha4");
		boolean durumParentBordroTanimKodu = durumParentBordroTanimKoduStr.equalsIgnoreCase("true");
		Tanim parentBolum = personelEKSahaMap != null && personelEKSahaMap.containsKey("ekSaha3") ? personelEKSahaMap.get("ekSaha3") : null;
		HashMap map = new HashMap();
		if (bosDepartman == null && departmanYoneticiRolVar && parentDepartman != null && mailMap.containsKey("bosDepartmanKodu")) {
			String bosDepartmanKodu = (String) mailMap.get("bosDepartmanKodu");
			fields.clear();
			fields.put("parentTanim.id", parentDepartman.getId());
			fields.put("tipi", Tanim.TIPI_PERSONEL_EK_SAHA_ACIKLAMA);
			fields.put("kodu", bosDepartmanKodu);
			fields.put("durum", Boolean.FALSE);
			bosDepartman = (Tanim) pdksDAO.getObjectByInnerObject(fields, Tanim.class);
		}
		Date lastDate = getTarih(LAST_DATE, FORMAT_DATE);
		Personel personelTest = new Personel();
		String personelERPGuncelleme = mailMap.containsKey("personelERPOku") ? (String) mailMap.get("personelERPOku") : "";
		boolean personelERPGuncellemeDurum = personelERPGuncelleme != null && personelERPGuncelleme.equalsIgnoreCase("M");

		for (String personelNo : personelList) {
			kidemHataList.clear();
			boolean calisiyor = false;
			if (personelERPMap.containsKey(personelNo)) {
				PersonelERP personelERP = personelERPMap.get(personelNo);
				String yoneticiNo = personelERP.getYoneticiPerNo() != null ? personelERP.getYoneticiPerNo().trim() : "";
				String yonetici2No = personelERP.getYonetici2PerNo() != null ? personelERP.getYonetici2PerNo().trim() : "";
				boolean yoneticiPersonel = yoneticiBul || yoneticiNo.equals(personelNo);

				Tanim cinsiyet = getTanim(null, Tanim.TIPI_CINSIYET, personelERP.getCinsiyetKodu(), personelERP.getCinsiyeti(), dataMap, saveList);
				String soyadi = personelERP.getSoyadi() != null ? new String(personelERP.getSoyadi()) : null;
				personelTest.setCinsiyet(bayanSoyadKontrol ? cinsiyet : null);
				boolean bayanSoyad = false;
				Personel personelSecili = null;
				Date iseBaslamaTarihi = getTarih(personelERP.getIseGirisTarihi(), FORMAT_DATE);
				boolean genelMudurDurum = personelERP.getGorevKodu() != null && genelMudurERPKodlari.contains(personelERP.getGorevKodu());
				if (PdksUtil.hasStringValue(personelERP.getIstenAyrilmaTarihi()) == false || personelERP.getIstenAyrilmaTarihi().equals(calisanIstenAyrilmaTarihi))
					personelERP.setIstenAyrilmaTarihi(LAST_DATE);

				Date grubaGirisTarihi = getTarih(personelERP.getGrubaGirisTarihi(), FORMAT_DATE);
				Date istenAyrilisTarihi = getTarih(personelERP.getIstenAyrilmaTarihi(), FORMAT_DATE);
				if (yoneticiPersonel) {
					if (!kendiYonetici)
						kendiYonetici = yoneticiNo.equals(personelNo);
					personelYoneticiERPMap.put(personelNo, personelERP);
					personelERPMap.remove(personelNo);
				}
				ERPPersonel erpPersonel = null;
				if (vardiyaSablonu == null)
					addHatalist(personelERP.getHataList(), "Beyaz yaka şablonu bulunamadı!");
				if (personelERP.getSirketKodu() == null || !sirketMap.containsKey(personelERP.getSirketKodu())) {
					if (PdksUtil.hasStringValue(personelERP.getSirketKodu()) == false)
						addHatalist(personelERP.getHataList(), sirketAciklama() + " kodu boş olamaz!");
					else {
						Sirket sirket = null;
						if (sistemDestekVar && mailMap.containsKey("erpSirketOlustur") && mailMap.get("erpSirketOlustur").equals("1")) {
							fields.clear();
							fields.put("durum", Boolean.TRUE);
							fields.put("admin", Boolean.TRUE);
							Departman departman = (Departman) pdksDAO.getObjectByInnerObject(fields, Departman.class);
							if (departman != null) {
								sirket = new Sirket();
								sirket.setDepartman(departman);
								sirket.setFazlaMesaiTalepGirilebilir(departman.getFazlaMesaiTalepGirilebilir());
								sirket.setErpKodu(personelERP.getSirketKodu());
								sirket.setErp(Boolean.TRUE);
								sirket.setAd(personelERP.getSirketAdi());
								sirket.setAciklama(personelERP.getSirketAdi());
								sirket.setPdks(Boolean.TRUE);
								sirket.setFazlaMesai(Boolean.TRUE);
								sirket.setDurum(Boolean.TRUE);
								sirket.setOlusturmaTarihi(bugun);
								sirket.setOlusturanUser(islemYapan);
								pdksDAO.saveObject(sirket);
							}
						}
						if (sirket != null) {
							sirketMap.put(personelERP.getSirketKodu(), sirket);
							sirket.setGuncellendi(Boolean.TRUE);
						} else
							addHatalist(personelERP.getHataList(), personelERP.getSirketKodu() + " hatalı " + sirketAciklama() + " kodu!");

					}
				}
				PersonelKGS personelKGSData = personelKGSMap.containsKey(personelNo) ? personelKGSMap.get(personelNo) : null, personelKGSBos = null;
				if (personelKGSData == null) {
					map.clear();
					if (kapiSirket != null)
						map.put("kapiSirket.id", kapiSirket.getId());
					map.put("ad", personelERP.getAdi());
					map.put("soyad", personelERP.getSoyadi());
					map.put("durum", Boolean.TRUE);
					List<PersonelKGS> list = pdksDAO.getObjectByInnerObjectList(map, PersonelKGS.class);
					if (list.size() == 1) {
						personelKGSBos = list.get(0);
						map.clear();
						map.put("pdksSicilNo", personelKGSBos.getSicilNo());
						List<Personel> personelPDKSList = pdksDAO.getObjectByInnerObjectList(map, Personel.class);
						if (!personelPDKSList.isEmpty())
							personelKGSBos = null;
					}
				}
				String adSoyadERP = (personelERP.getAdi() != null ? personelERP.getAdi().trim() : "Ad tanımsız") + " " + (personelERP.getSoyadi() != null ? personelERP.getSoyadi().trim() : "Soyad tanımsız");
				String ad = PdksUtil.getCutFirstSpaces(personelERP.getAdi());
				String soyad = PdksUtil.getCutFirstSpaces(personelERP.getSoyadi());
				if (personelKGSData == null || personelKGSData.getId() < 0L) {
					boolean kayitYok = true;
					if (personelKGSData == null && mailMap.containsKey("personelKGSDataOlustur") && personelERP.getAdi() != null && personelERP.getSoyadi() != null && personelERP.getPersonelNo() != null) {
						LinkedHashMap<String, Object> map2 = new LinkedHashMap<String, Object>();
						if (kapiSirket != null)
							map2.put("kapiSirket.id", kapiSirket.getId());
						map2.put("ad", personelERP.getAdi());
						map2.put("soyad", personelERP.getSoyadi());
						map2.put("perNo", personelERP.getPersonelNo());
						map2.put(BaseDAOHibernate.MAP_KEY_SELECT, "SP_ADD_PERSONEL_KGS");
						try {
							List<PersonelKGS> list2 = pdksDAO.execSPList(map2, PersonelKGS.class);
							if (!list2.isEmpty())
								personelKGSData = list2.get(0);
						} catch (Exception e) {
						}
					}
					if (personelKGSData != null && personelKGSData.getId() > 0L)
						personelKGSMap.put(personelNo, personelKGSData);
					else {

						if (gecmisTarih != null && iseBaslamaTarihi != null && iseBaslamaTarihi.before(gecmisTarih))
							kayitYok = false;

						if (kayitYok && (istenAyrilisTarihi == null || !istenAyrilisTarihi.before(ayBasi))) {

							String mesaj = adSoyadERP + " personel'in " + kapiGiris + " " + personelNo + " personel no bilgisi bulunamadı!" + (iseBaslamaTarihi != null ? " [ İşe giriş tarihi : " + PdksUtil.convertToDateString(iseBaslamaTarihi, PdksUtil.getDateFormat()) + " ]" : "");
							String ek = "";
							if (istenAyrilisTarihi != null && istenAyrilisTarihi.before(bugun))
								ek = " (İşten ayrılma tarihi : " + personelERP.getIstenAyrilmaTarihi();

							if (personelKGSBos != null && PdksUtil.hasStringValue(personelKGSBos.getSicilNo())) {
								if (!PdksUtil.hasStringValue(ek))
									ek += "(";
								else
									ek += " - ";
								ek += kapiGiris + " personel no " + personelKGSBos.getSicilNo().trim();

							}
							if (PdksUtil.hasStringValue(ek))
								ek += ")";
							mesaj += ek;

							addHatalist(personelERP.getHataList(), mesaj);
							if (!personelERPHataliMap.containsKey(personelNo)) {
								erpPersonel = new ERPPersonel();
								erpPersonel.setSicilNo(personelNo);
								personelERPHataliMap.put(personelNo, erpPersonel);
							} else {
								erpPersonel = personelERPHataliMap.get(personelNo);
								if (erpPersonel.getDurum())
									erpPersonel.setIslemZamani(new Date());
							}
							if (erpPersonel.getDurum()) {
								erpPersonel.setAd(PdksUtil.getCutFirstSpaces(personelERP.getAdi()));
								erpPersonel.setSoyad(PdksUtil.getCutFirstSpaces(personelERP.getSoyadi()));
								saveList.add(erpPersonel);
							}

						}
					}

				} else {
					if (tip.equals("P")) {

						String erpAdSoyad = PdksUtil.setTurkishStr(PdksUtil.getAdSoyad(personelERP.getAdi(), personelERP.getSoyadi()));
						String kgsAdSoyad = PdksUtil.setTurkishStr(PdksUtil.getAdSoyad(personelKGSData.getAd(), personelKGSData.getSoyad()));

						if (!kgsAdSoyad.equalsIgnoreCase(erpAdSoyad)) {
							if (personelERP.getAdi() == null || personelERP.getSoyadi() == null) {
								String mesaj = "adı ve soyadı";
								if (personelERP.getAdi() != null) {
									mesaj = "soyadı";
									bayanSoyad = personelTest.isCinsiyetBayan();
									if (bayanSoyad)
										personelERP.setSoyadi(personelKGSData.getSoyad());

								}

								else if (personelERP.getSoyadi() != null)
									mesaj = "adı";

								mesaj = personelNo + " personel " + mesaj + " girilmemiş!  ";
								addHatalist(bayanSoyad == false ? personelERP.getHataList() : kidemHataList, PdksUtil.replaceAllManuel(mesaj, "  ", " "));

							} else {
								PersonelKGS personelKGS = personelKGSMap.get(personelNo);
								String kgsAd = personelKGS.getAd();
								if (sistemDestekVar && ad != null && kgsAd != null && !ad.equals(kgsAd)) {
									if (ad.indexOf(" ") > 0 || kgsAd.indexOf(" ") > 0) {
										if (ad.indexOf(" ") > 0)
											ad = PdksUtil.replaceAllManuel(ad, " ", "");
										if (kgsAd.indexOf(" ") > 0)
											kgsAd = PdksUtil.replaceAllManuel(kgsAd, " ", "");
									}
								}
								boolean adiUyumlu = isBenzer(ad, kgsAd);
								boolean soyadiUyumlu = isBenzer(personelKGS.getSoyad(), soyad);
								if (!adiUyumlu || !soyadiUyumlu) {
									String mesaj = "";
									if (!adiUyumlu && !soyadiUyumlu)
										mesaj = "adı ve soyadı";
									if (!adiUyumlu) {
										mesaj = "adı";
									} else if (!soyadiUyumlu) {
										mesaj = "soyadi";
										bayanSoyad = personelTest.isCinsiyetBayan();
										if (bayanSoyad)
											personelERP.setSoyadi(personelKGS.getSoyad());

									}
									mesaj = personelNo + " personel " + mesaj + " uyumsuz! ( " + adSoyadERP + " farklı " + personelKGS.getAdSoyad() + (personelKGS.getKapiSirket() != null ? " [ " + personelKGS.getKapiSirket().getAciklama() + " ] " : "") + " ) ";
									addHatalist(bayanSoyad == false ? personelERP.getHataList() : kidemHataList, PdksUtil.replaceAllManuel(mesaj, "  ", " "));

								}
							}
						} else {
							if (personelERP.getAdi() == null)
								personelERP.setAdi("");
							if (personelERP.getSoyadi() == null)
								personelERP.setSoyadi("");
						}

					}
					if (personelERPHataliMap.containsKey(personelNo)) {
						ERPPersonel erpPersonel2 = personelERPHataliMap.get(personelNo);
						try {
							pdksDAO.deleteObject(erpPersonel2);
							personelERPHataliMap.remove(personelNo);
						} catch (Exception e) {
						}
					}
				}

				if (!sirketMap.containsKey(personelERP.getSirketKodu()))
					addHatalist(personelERP.getHataList(), personelERP.getSirketKodu() + " " + personelERP.getSirketAdi() + " " + sirketAciklama() + " bilgisi bulunamadı!");

				if (personelERP.getHataList().isEmpty() && personelKGSMap.containsKey(personelNo)) {
					PersonelKGS personelKGS = personelKGSMap.get(personelNo);
					Personel personel = personelPDKSMap.containsKey(personelNo) ? personelPDKSMap.get(personelNo) : null;
					if (personel != null) {
						personel.setCinsiyet(cinsiyet);
						PersonelKGS personelKGS2 = personel.getPersonelKGS();
						personel.setVeriDegisti(Boolean.FALSE);
						if (kapiSirket != null && personelKGS2.getKapiSirket() != null && !personelKGS2.getId().equals(personelKGS.getId()) && !personelKGS2.getKapiSirket().getId().equals(kapiSirket.getId())) {
							boolean adBenzer = isBenzer(personelKGS.getAd(), personelKGS2.getAd());
							boolean soyadBenzer = isBenzer(personelKGS.getSoyad(), personelKGS2.getSoyad());
							boolean kimlikBenzer = (PdksUtil.hasStringValue(personelKGS.getKimlikNo()) && PdksUtil.hasStringValue(personelKGS2.getKimlikNo())) || (personelKGS.getKimlikNo() != null && personelKGS.getKimlikNo() != null && personelKGS.getKimlikNo().equals(personelKGS2.getKimlikNo()));
							if (soyadBenzer && adBenzer && kimlikBenzer)
								personel.setPersonelKGS(personelKGS);
							else {
								String uygulamaFark = kapiSirket.getAciklama() + " ile " + personelKGS2.getKapiSirket().getAciklama();
								if (adBenzer) {
									bayanSoyad = personelTest.isCinsiyetBayan();
									if (bayanSoyad)
										personelERP.setSoyadi(personelKGS2.getSoyad());
									addHatalist(bayanSoyad == false ? personelERP.getHataList() : kidemHataList, PdksUtil.replaceAllManuel(uygulamaFark + " soyad uyumsuz!", "  ", " "));
								}

								else if (soyadBenzer)
									personelERP.getHataList().add(uygulamaFark + " ad uyumsuz!");
								else if (kimlikBenzer)
									personelERP.getHataList().add(uygulamaFark + " ad soyad uyumsuz!");
								if (!kimlikBenzer)
									personelERP.getHataList().add(uygulamaFark + " kimlik numarası uyumsuz!");
							}
						}

					} else if (personelDigerMap.containsKey(personelNo)) {
						personel = personelDigerMap.get(personelNo);
						personel.setVeriDegisti(Boolean.FALSE);
						personel.setPersonelKGS(personelKGS);
						personel.setPdksSicilNo(personelNo);
					}

					if (personel == null) {
						personel = new Personel();
						personel.setDurum(Boolean.TRUE);
						if (!yoneticiPersonel || yoneticiMailKontrol.equals("1"))
							personel.setMailTakip(Boolean.TRUE);
						personel.setPdks(Boolean.TRUE);
						personel.setPersonelKGS(personelKGS);
						personel.setSablon(vardiyaSablonu);
						personel.setFazlaMesaiIzinKullan(Boolean.FALSE);
						personelPDKSMap.put(personelNo, personel);
						personel.setOlusturmaTarihi(new Date());
						personel.setOlusturanUser(islemYapan);
						personel.setPdksSicilNo(personelNo);

					} else {
						PersonelKGS personelKGSKayitli = personel.getPersonelKGS();
						if (!personelKGSKayitli.getDurum()) {
							map.clear();
							if (kapiSirket != null)
								map.put("kapiSirket.id", kapiSirket.getId());
							map.put("sicilNo", personelNo);
							map.put("durum", Boolean.TRUE);
							List<PersonelKGS> list = pdksDAO.getObjectByInnerObjectList(map, PersonelKGS.class);
							if (!list.isEmpty()) {
								for (PersonelKGS personelKGSDiger : list) {
									boolean adiUyumlu = isBenzer(personelKGSDiger.getAd(), ad);
									boolean soyadiUyumlu = isBenzer(personelKGSDiger.getSoyad(), soyad);
									if (adiUyumlu && soyadiUyumlu) {
										personel.setPersonelKGS(personelKGSDiger);
										personel.setVeriDegisti(Boolean.TRUE);
										break;
									}
								}
							}
						}
						if (!personelNo.equals(personelKGSKayitli.getSicilNo())) {
							personel.setPersonelKGS(personelKGS);
						}
						if (yoneticiPersonel && !yoneticiMailKontrol.equals("1"))
							personel.setMailTakip(Boolean.FALSE);
					}
					setPersonel(personel, personelERP, FORMAT_DATE);
					personelERP.setSoyadi(soyadi);
					personelSecili = personel;
					Sirket sirket = sirketMap.get(personelERP.getSirketKodu());
					if (sirket != null) {
						if (!sirket.getGuncellendi().booleanValue()) {
							if (PdksUtil.isStrDegisti(sirket.getAd(), personelERP.getSirketAdi())) {
								sirket.setAd(personelERP.getSirketAdi());
								sirket.setGuncellemeTarihi(new Date());
								sirket.setGuncelleyenUser(islemYapan);
								saveList.add(sirket);
							}
							sirket.setGuncellendi(Boolean.TRUE);
						}
						if (personel.getId() == null) {
							TreeMap<String, Boolean> izinMap = getOnaysizIzinDurumMap(sirket);
							boolean onaysizIzinDurum = izinMap.containsKey("onaysizIzinDurum");
							boolean ikinciYoneticiIzinOnayla = izinMap.containsKey("ikinciYoneticiIzinOnaySec");
							personel.setIkinciYoneticiIzinOnayla(ikinciYoneticiIzinOnayla);
							personel.setOnaysizIzinKullanilir(onaysizIzinDurum);
						}
					}
					personel.setSirket(sirket);
					if (personel.getId() == null && personel.getSirket() != null) {
						personel.setFazlaMesaiOde(personel.getSirket().getFazlaMesaiOde() != null && personel.getSirket().getFazlaMesaiOde());
					}

					Tanim bolum = getTanim(null, "ekSaha3", personelERP.getBolumKodu(), personelERP.getBolumAdi(), dataMap, saveList);
					Tanim bordroAltAlan = getTanim(null, parentBordroTanimKoduStr, personelERP.getBordroAltAlanKodu(), personelERP.getBordroAltAlanAdi(), dataMap, saveList);
					personel.setTesis(getTanim(personelERP.getSirketKodu(), Tanim.TIPI_TESIS, personelERP.getTesisKodu(), personelERP.getTesisAdi(), dataMap, saveList));
					personel.setGorevTipi(getTanim(null, Tanim.TIPI_GOREV_TIPI, personelERP.getGorevKodu(), personelERP.getGorevi(), dataMap, saveList));
					personel.setMasrafYeri(getTanim(null, Tanim.TIPI_ERP_MASRAF_YERI, personelERP.getMasrafYeriKodu(), personelERP.getMasrafYeriAdi(), dataMap, saveList));
					Date dogumTarihi = getTarih(personelERP.getDogumTarihi(), FORMAT_DATE);
					Date izinHakEdisTarihi = getTarih(personelERP.getKidemTarihi(), FORMAT_DATE);
					if (iskurManuelGiris) {
						if (iseBaslamaTarihi == null && notEmptyStr(personelERP.getIseGirisTarihi()) == false)
							iseBaslamaTarihi = personel.getIseBaslamaTarihi();
						if (izinHakEdisTarihi == null && notEmptyStr(personelERP.getKidemTarihi()) == false)
							izinHakEdisTarihi = personel.getIzinHakEdisTarihi();
						if (istenAyrilisTarihi == null && notEmptyStr(personelERP.getIstenAyrilmaTarihi()) == false)
							istenAyrilisTarihi = personel.getIstenAyrilisTarihi();
					}

					boolean sanalPersonel = personelERP.getSanalPersonel() != null && personelERP.getSanalPersonel().booleanValue();
					if (iskurManuelGiris && PdksUtil.hasStringValue(personelERP.getIseGirisTarihi()) == false) {
						sanalPersonel = personel.getId() == null || personel.getSanalPersonel();
						if (personel.getSanalPersonel() != null && personel.getSanalPersonel()) {
							iseBaslamaTarihi = personel.getIseBaslamaTarihi();
							izinHakEdisTarihi = personel.getIzinHakEdisTarihi();
							istenAyrilisTarihi = personel.getIstenAyrilisTarihi();
						}
					}
					personel.setIstenAyrilisTarihi(istenAyrilisTarihi);
					personel.setIseBaslamaTarihi(iseBaslamaTarihi);
					if (istenAyrilisTarihi == null) {
						String str = null;
						if (notEmptyStr(personelERP.getIstenAyrilmaTarihi()) == false)
							str = "İşten ayrılma tarihi boş olamaz!";
						else
							str = "İşten ayrılma tarihi hatalıdır! (" + personelERP.getIstenAyrilmaTarihi() + " --> format : " + FORMAT_DATE + " )";
						addHatalist(personelERP.getHataList(), str);
					}
					calisiyor = istenAyrilisTarihi != null && iseBaslamaTarihi != null && personel.isCalisiyor();
					if (iseBaslamaTarihi == null && sanalPersonel == false) {
						if (notEmptyStr(personelERP.getIseGirisTarihi()) == false)
							addHatalist(personelERP.getHataList(), "İşe giriş tarihi boş olamaz!");
						else
							addHatalist(personelERP.getHataList(), "İşe giriş tarihi hatalıdır! (" + personelERP.getIseGirisTarihi() + " --> format : " + FORMAT_DATE + " )");
					}
					if (istenAyrilisTarihi == null && sanalPersonel == false) {
						if (notEmptyStr(personelERP.getIstenAyrilmaTarihi()) == false)
							istenAyrilisTarihi = calisanTarihi;
						else
							addHatalist(personelERP.getHataList(), "İşten ayrılma tarihi hatalıdır! (" + personelERP.getIstenAyrilmaTarihi() + " --> format : " + FORMAT_DATE + " )");
					}
					if (izinGirisiVar) {
						if (dogumTarihi == null && calisiyor) {
							String str = null;
							if (notEmptyStr(personelERP.getDogumTarihi()) == false)
								str = "Doğum tarihi boş olamaz!";
							else if (sistemDestekVar == false || personelERP.getDogumTarihi().length() > 5)
								str = "Doğum tarihi hatalıdır! (" + personelERP.getDogumTarihi() + " --> format : " + FORMAT_DATE + " )";
							if (str != null) {
								if (!izinERPUpdate)
									addHatalist(personelERP.getHataList(), str);
								else {
									kidemHataList.add(str);
								}
							}
						}
						if (izinHakEdisTarihi == null && sanalPersonel == false) {
							String str = null;
							if (notEmptyStr(personelERP.getKidemTarihi()) == false)
								str = "Kıdem tarihi boş olamaz!";
							else
								str = "Kıdem tarihi hatalıdır! (" + personelERP.getKidemTarihi() + " --> format : " + FORMAT_DATE + " )";
							if (!izinERPUpdate)
								addHatalist(personelERP.getHataList(), str);
							else {

								kidemHataList.add(str);
							}

						}

					}
					Tanim departman = getTanim(null, "ekSaha1", personelERP.getDepartmanKodu(), personelERP.getDepartmanAdi(), dataMap, saveList);
					if (departman == null) {
						departman = bosDepartman;
						if (bosDepartman != null && calisiyor)
							kidemHataList.add(parentDepartman.getAciklamatr() + " bilgisi boş olamaz!");
					}
					if (istenAyrilisTarihi != null && istenAyrilisTarihi.after(bugun) && istenAyrilisTarihi.getTime() == lastDate.getTime()) {
						if (departmanYoneticiRolVar && departman == null && parentDepartman != null && departmanYoneticiRolVar)
							addHatalist(personelERP.getHataList(), parentDepartman.getAciklamatr() + " bilgisi boş olamaz!");
						if (bolum == null && parentBolum != null)
							addHatalist(personelERP.getHataList(), parentBolum.getAciklamatr() + " bilgisi boş olamaz!");
						if (bordroAltAlan == null && parentBordroTanim != null) {
							if (durumParentBordroTanimKodu)
								addHatalist(personelERP.getHataList(), parentBordroTanim.getAciklamatr() + " bilgisi boş olamaz!");

						}

					} else if (bordroAltAlan == null)
						bordroAltAlan = istenAyrilanEkSaha;
					personel.setEkSaha1(departman);
					personel.setEkSaha3(bolum);

					if (ekSahaAdi.startsWith("eksaha2")) {
						personel.setEkSaha2(bordroAltAlan);
						if (personel.getBordroAltAlan() != null)
							personel.setBordroAltAlan(null);
					} else if (ekSahaAdi.startsWith("eksaha4")) {
						personel.setEkSaha4(bordroAltAlan);
						if (personel.getBordroAltAlan() != null)
							personel.setBordroAltAlan(null);
					} else
						personel.setBordroAltAlan(bordroAltAlan);

					personel.setDogumTarihi(dogumTarihi);
					personel.setIzinHakEdisTarihi(izinHakEdisTarihi);
					personel.setSanalPersonel(sanalPersonel);
					if (sanalPersonel == false) {
						if (personel.getIsKurVardiyaSablonu() != null) {
							personel.setIsKurVardiyaSablonu(null);
							personel.setVeriDegisti(Boolean.TRUE);
						}
					} else if (personel.getIsKurVardiyaSablonu() == null && isKurVardiyaSablonu != null) {
						personel.setIsKurVardiyaSablonu(isKurVardiyaSablonu);
						personel.setVeriDegisti(Boolean.TRUE);
					}

					if (grubaGirisTarihi == null) {
						grubaGirisTarihi = iseBaslamaTarihi;
						if (personel.getId() != null) {
							Date tarih = getGrubaGirisTarihi(personel.getPersonelKGS());
							if (tarih != null)
								grubaGirisTarihi = tarih;
						}

						if (grubaGirisTarihi != null)
							personel.setGrubaGirisTarihi(grubaGirisTarihi);

					} else {
						if (dogumTarihi != null && grubaGirisTarihi.before(dogumTarihi))
							addHatalist(personelERP.getHataList(), "Grubu giriş tarihi doğum tarihinden önce olamaz!");
						if (iseBaslamaTarihi != null && iseBaslamaTarihi.before(grubaGirisTarihi))
							addHatalist(personelERP.getHataList(), "İşe giriş tarihi grubu giriş tarihinden önce olamaz!");
						if (izinHakEdisTarihi != null && izinHakEdisTarihi.before(grubaGirisTarihi))
							addHatalist(personelERP.getHataList(), "Kıdem tarihi grubu giriş tarihinden önce olamaz!");
						if (istenAyrilisTarihi != null && istenAyrilisTarihi.before(grubaGirisTarihi))
							addHatalist(personelERP.getHataList(), "İşten ayrılma tarihi grubu giriş tarihinden önce olamaz!");
						personel.setGrubaGirisTarihi(grubaGirisTarihi);
					}

					boolean yoneticiKoduVar = PdksUtil.hasStringValue(yoneticiNo);

					Personel yoneticisi = yoneticiNo.equals(personelNo) && personel.getId() == null ? personel : null;
					if (yoneticisi == null) {
						if (yoneticiKoduVar && personelPDKSMap.containsKey(yoneticiNo)) {
							yoneticisi = personelPDKSMap.get(yoneticiNo);
							yoneticiKoduVar = yoneticisi != null && yoneticisi.isCalisiyor();
						}
						if (yoneticiKoduVar) {
							if (yoneticisi != null) {
								if (yoneticisi.getId() == null) {
									personel.setTmpYonetici(yoneticisi);
									yoneticiList.add(personel);
									yoneticisi = null;
								}

								if ((yoneticiERPKontrol.equals("1") && yoneticiERP1Kontrol) || personelERPGuncellemeDurum || personel.getYoneticisi() == null || !personel.getYoneticisi().isCalisiyor())
									personel.setYoneticisi(yoneticisi);
								personel.setAsilYonetici1(yoneticisi);

							} else if (!(iskurManuelGiris && sanalPersonel))
								map1.put(personelNo, yoneticiPersonel);

						} else {
							if (genelMudurDurum == false) {
								if (sanalPersonel == false && calisiyor) {
									if (yoneticiKoduVar == false) {
										if (personel.getId() != null && personel.getYoneticisi() != null) {
											if (yoneticiERP1Kontrol) {
												personel.setYoneticisi(null);
												personel.setAsilYonetici1(null);
											}

											personel.setGuncellemeTarihi(new Date());
											personel.setGuncelleyenUser(islemYapan);
											try {
												saveList.add(personel);
												listeKaydet(personelNo, saveList, null);
											} catch (Exception e) {
												logger.error(personelNo + "\n" + e);
												e.printStackTrace();
											}

										}
										if (yoneticiRolVarmi && yoneticiERP1Kontrol) {
											if (yoneticisi == null)
												kidemHataList.add(yoneticiAciklama + " bilgisi boş olamaz!" + (personelERP.getGorevKodu() != null && personelERP.getGorevi() != null ? "[ " + personelERP.getGorevKodu() + " - " + personelERP.getGorevi() + " ]" : ""));
											else if (yoneticisi != null)
												kidemHataList.add(yoneticisi.getPdksSicilNo() + " " + yoneticisi.getAdSoyad() + " yönetici çalışmıyor!");
										}
									}
								}
							} else {
								personel.setAsilYonetici1(null);
								personel.setYoneticisi(null);
							}
						}
					}
					Personel yoneticisi2 = yonetici2ERPKontrol ? null : personel.getAsilYonetici2();
					if (personel.getId() == null && yonetici2No.equals(personelNo))
						yoneticisi2 = personel;
					else {
						if (yonetici2ERPKontrol) {
							boolean yoneticiKodu2Var = PdksUtil.hasStringValue(yonetici2No);
							if (yoneticiKodu2Var) {
								String yonetici2PerNo = PdksUtil.textBaslangicinaKarakterEkle(yonetici2No.trim(), '0', sicilNoUzunluk);
								yoneticisi2 = personelPDKSMap.get(yonetici2PerNo);
								if (yoneticisi2 != null) {
									if (yoneticisi != null && yoneticisi.getYoneticisi() != null && yoneticisi2.getId().equals(yoneticisi.getYoneticisi().getId())) {
										yoneticisi2 = null;
									} else if (yoneticiRolVarmi && !yoneticisi2.isCalisiyor())
										kidemHataList.add("2. yönetici " + yonetici2No.trim() + " " + yoneticisi2.getAdSoyad() + " çalışmıyor!");
								} else if (yoneticiRolVarmi && sanalPersonel == false && calisiyor)
									kidemHataList.add(kapiGiris + " 2. yönetici " + yonetici2No.trim() + " personel no bilgisi bulunamadı!");
							}
							if (yoneticisi2 != null)
								logger.debug(yoneticisi2.getId());
							if (personelERP.getHataList().isEmpty())
								personel.setAsilYonetici2(yoneticisi2);
						} else {
							boolean yoneticiKodu2Var = PdksUtil.hasStringValue(yonetici2No);
							if (yoneticiKodu2Var) {
								String yonetici2PerNo = PdksUtil.textBaslangicinaKarakterEkle(yonetici2No.trim(), '0', sicilNoUzunluk);
								yoneticisi2 = personelPDKSMap.get(yonetici2PerNo);
								if (yoneticiRolVarmi) {
									if (yoneticisi2 != null)
										addHatalist(personelERP.getHataList(), "2. yönetici " + yonetici2No.trim() + " " + yoneticisi2.getAdSoyad() + " güncellemesi sistemde açık değildir!");
									else
										addHatalist(personelERP.getHataList(), "2. yönetici güncellemesi sistemde açık değildir!");
								}
							}
						}
					}
					if (personelERP.getHataList().isEmpty()) {
						if (personel.getId() != null && personel.getVeriDegisti()) {
							personel.setGuncellemeTarihi(new Date());
							personel.setGuncelleyenUser(islemYapan);
						}
						if (!updateYonetici2)
							updateYonetici2 = true;
						saveList.add(personel);
					} else if (personel.getId() == null && !calisiyor && !(iskurManuelGiris && sanalPersonel)) {
						personelERP.setYazildi(Boolean.FALSE);
						continue;
					}

				}

				if (!saveList.isEmpty()) {
					try {
						if (listeKaydet(personelNo, saveList, null)) {
							if (yoneticiIdList != null && personelSecili != null && personelSecili.getYoneticisi() != null && personelSecili.getId() != null) {
								if (!yoneticiIdList.contains(personelSecili.getId()))
									yoneticiIdList.add(personelSecili.getId());
							}
							if (personelERP.getHataList().isEmpty()) {
								if (kidemHataList.isEmpty()) {
									personelERP.setHataList(null);
									personelERP.setYazildi(Boolean.TRUE);
								} else {
									// personelERP.getHataList().addAll(kidemHataList);
									for (String string : kidemHataList)
										addHatalist(personelERP.getHataList(), string);

								}

							}
						}

					} catch (Exception e) {
						e.printStackTrace();
						addHatalist(personelERP.getHataList(), e.getMessage());
					}

				}

			}

		}
		personelTest = null;
		if (yoneticiBul || kendiYonetici) {
			for (String personelNo : personelList) {
				if (personelPDKSMap.containsKey(personelNo) && personelYoneticiERPMap.containsKey(personelNo)) {
					Personel personel = personelPDKSMap.get(personelNo);
					PersonelERP personelERP = personelYoneticiERPMap.get(personelNo);
					boolean durum = map1.containsKey(personelNo) && map1.get(personelNo);
					if (durum && personelPDKSMap.containsKey(personelERP.getYoneticiPerNo())) {
						Personel yoneticisi = personelPDKSMap.get(personelERP.getYoneticiPerNo());
						if ((personelERPGuncellemeDurum || personel.getYoneticisi() == null) && yoneticisi.getId() != null)
							personel.setYoneticisi(yoneticisi);
						personel.setAsilYonetici1(yoneticisi);
						saveList.add(personel);
					}
				}
			}
			if (!saveList.isEmpty()) {
				try {
					listeKaydet("", saveList, null);
				} catch (Exception e) {

				}

			}
		}
		personelYoneticiERPMap = null;
	}

	/**
	 * 
	 */
	private void setSicilNoUzunluk() {
		sicilNoUzunluk = null;
		if (mailMap != null && mailMap.containsKey("sicilNoUzunluk"))
			try {
				sicilNoUzunluk = Integer.parseInt(mailMap.get("sicilNoUzunluk").toString());
				if (sicilNoUzunluk < 1)
					sicilNoUzunluk = null;
			} catch (Exception e) {
				sicilNoUzunluk = null;
			}
	}

	/**
	 * @param personel
	 * @param personelERP
	 * @param pattern
	 */
	private void setPersonel(Personel personel, PersonelERP personelERP, String pattern) {
		if (personel != null && personelERP != null) {
			try {
				if (personel.getCalismaModeli() == null)
					personel.setCalismaModeli(calismaModeli);
				personel.setAd(PdksUtil.getCutFirstSpaces(personelERP.getAdi()));
				personel.setSoyad(PdksUtil.getCutFirstSpaces(personelERP.getSoyadi()));
				// personel.setDogumTarihi(PdksUtil.convertToJavaDate(personelERP.getDogumTarihi(), pattern));
				// personel.setIseBaslamaTarihi(PdksUtil.convertToJavaDate(personelERP.getIseGirisTarihi(), pattern));
				// personel.setIzinHakEdisTarihi(PdksUtil.convertToJavaDate(personelERP.getKidemTarihi(), pattern));
				// personel.setIstenAyrilisTarihi(PdksUtil.convertToJavaDate(personelERP.getIstenAyrilmaTarihi(), pattern));

			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	/**
	 * @param key
	 * @param saveList
	 * @param deleteList
	 * @return
	 * @throws Exception
	 */
	private boolean listeKaydet(String key, List saveList, List deleteList) throws Exception {
		Boolean durum = Boolean.FALSE;
		if (saveList != null || deleteList != null) {
			Object[] saveObjectArray = null;
			if (saveList != null && !saveList.isEmpty()) {
				logger.debug(key + " " + saveList.size());
				saveObjectArray = new Object[saveList.size()];
				for (int i = 0; i < saveObjectArray.length; i++) {
					saveObjectArray[i] = saveList.get(i);
				}
			}
			Object[] deleteObjectArray = null;
			if (deleteList != null && !deleteList.isEmpty()) {
				deleteObjectArray = new Object[deleteList.size()];
				for (int i = 0; i < deleteObjectArray.length; i++) {
					deleteObjectArray[i] = deleteList.get(i);
				}
			}

			pdksDAO.deleteAndSaveObject(saveObjectArray, deleteObjectArray);
			if (saveList != null)
				saveList.clear();
			if (deleteList != null)
				deleteList.clear();
			durum = Boolean.TRUE;
		}
		return durum;

	}

	/**
	 * @param personelList
	 * @throws Exception
	 */
	public void savePersoneller(List<PersonelERP> personelList) throws Exception {
		servisAdi = "savePersoneller";
		if (pdksDAO != null && personelList != null && !personelList.isEmpty()) {
			sistemVerileriniYukle(pdksDAO);
			boolean servisDurum = !PdksUtil.getCanliSunucuDurum() || !(mailMap.containsKey(servisAdi + "Durum") && mailMap.get(servisAdi + "Durum").equals("0"));
			if (servisDurum) {
				kapiSirket = null;
				Date bugun = PdksUtil.getDate(new Date());
				String birdenFazlaKGSSirketSQL = mailMap.containsKey("birdenFazlaKGSSirketSQL") ? (String) mailMap.get("birdenFazlaKGSSirketSQL") : "";
				if (PdksUtil.hasStringValue(birdenFazlaKGSSirketSQL)) {
					HashMap map = new HashMap();
					map.put("id>", 0L);
					map.put("basTarih<=", bugun);
					map.put("bitTarih>=", bugun);
					List<KapiSirket> list = pdksDAO.getObjectByInnerObjectListInLogic(map, KapiSirket.class);
					if (list.size() == 1) {
						kapiSirket = list.get(0);
						kapiGiris = kapiSirket.getAciklama();
					}

				}
				boolean mailBosGonder = personelList.size() > 1;
				altBolumDurum = false;
				HashMap fields = new HashMap();
				fields.put("tipi", Tanim.TIPI_PERSONEL_DINAMIK_DURUM);
				fields.put("kodu", Tanim.IKINCI_YONETICI_ONAYLAMAZ);
				ikinciYoneticiOlmaz = (Tanim) pdksDAO.getObjectByInnerObject(fields, Tanim.class);

				fields.clear();
				fields.put(BaseDAOHibernate.MAP_KEY_MAP, "getKodu");
				fields.put("tipi", Tanim.TIPI_GENEL_TANIM);
				genelTanimMap = pdksDAO.getObjectByInnerObjectMap(fields, Tanim.class, false);
				if (ikinciYoneticiOlmaz != null && !ikinciYoneticiOlmaz.getDurum())
					ikinciYoneticiOlmaz = null;

				yoneticiIdList = new ArrayList<Long>();
				bosDepartman = null;
				setSicilNoUzunluk();
				if (personelList.size() == 1) {
					PersonelERP personelERP = personelList.get(0);
					if (personelERP != null)
						dosyaEkAdi = personelERP.getPersonelNo();
				}
				for (PersonelERP personelERP : personelList) {
					personelERP.veriSifirla();
					if (sicilNoUzunluk != null) {
						String perNo = PdksUtil.textBaslangicinaKarakterEkle(personelERP.getPersonelNo(), '0', sicilNoUzunluk);
						personelERP.setPersonelNo(perNo);
						if (PdksUtil.hasStringValue(personelERP.getYoneticiPerNo())) {
							String yoneticiPerNo = PdksUtil.textBaslangicinaKarakterEkle(personelERP.getYoneticiPerNo(), '0', sicilNoUzunluk);
							personelERP.setYoneticiPerNo(yoneticiPerNo);
						}
						if (PdksUtil.hasStringValue(personelERP.getYonetici2PerNo())) {
							String yonetici2PerNo = PdksUtil.textBaslangicinaKarakterEkle(personelERP.getYonetici2PerNo(), '0', sicilNoUzunluk);
							personelERP.setYonetici2PerNo(yonetici2PerNo);
						}

					}
				}
				saveFonksiyonVeri("savePersoneller", personelList);

				if (personelList.size() == 1) {
					PersonelERP erp = personelList.get(0);
					mesaj = (erp.getPersonelNo() != null ? erp.getPersonelNo() + " " : "") + (erp.getAdi() != null ? erp.getAdi() + " " : "") + (erp.getSoyadi() != null ? erp.getSoyadi() + " " : "");
				} else
					mesaj = personelList.size() + " adet personel bilgisi guncellenecektir.";
				mesajInfoYaz("savePersoneller --> " + mesaj + " in " + new Date());
				TreeMap<String, PersonelERP> perMap = new TreeMap<String, PersonelERP>();
				List<String> yoneticiPerNoList = new ArrayList<String>(), kendiYoneticiPerNoList = new ArrayList<String>();
				TreeMap<String, List<String>> veriSorguMap = new TreeMap<String, List<String>>();
				TreeMap<String, TreeMap> dataMap = new TreeMap<String, TreeMap>();
				dataMap.put("personelERPMap", perMap);
				if (personelList.size() > 1) {
					List<PersonelERP> list1 = new ArrayList<PersonelERP>(), list2 = new ArrayList<PersonelERP>();
					List<String> yoneticiNoList = new ArrayList<String>();
					for (PersonelERP personelERP : personelList) {
						String yoneticiNo = personelERP.getYoneticiPerNo() != null ? personelERP.getYoneticiPerNo().trim() : "";
						if (PdksUtil.hasStringValue(yoneticiNo) && !yoneticiNoList.contains(yoneticiNo))
							yoneticiNoList.add(yoneticiNo);
					}
					for (PersonelERP personelERP : personelList) {
						String perNo = personelERP.getPersonelNo() != null ? personelERP.getPersonelNo().trim() : "";
						String yoneticiNo = personelERP.getYoneticiPerNo() != null ? personelERP.getYoneticiPerNo().trim() : "";
						if (perNo.equals(yoneticiNo) || yoneticiNoList.contains(perNo))
							list1.add(personelERP);
						else
							list2.add(personelERP);

					}
					personelList.clear();
					if (!list1.isEmpty())
						personelList.addAll(list1);
					if (!list2.isEmpty())
						personelList.addAll(list2);
					list1 = null;
					list2 = null;
					yoneticiNoList = null;
				}

				for (PersonelERP personelERP : personelList) {
					try {
						personelERP.setYazildi(false);
						String yoneticiPerNo = personelERP.getYoneticiPerNo() != null ? personelERP.getYoneticiPerNo().trim() : "";
						String yonetici2PerNo = personelERP.getYonetici2PerNo() != null ? personelERP.getYonetici2PerNo().trim() : "";
						String perNo = personelERP.getPersonelNo() != null ? personelERP.getPersonelNo().trim() : "";
						veriIsle("sirket", personelERP.getSirketKodu(), veriSorguMap);
						veriIsle("personel", perNo, veriSorguMap);
						if (perNo.equals(yoneticiPerNo)) {
							if (PdksUtil.hasStringValue(yoneticiPerNo) && !kendiYoneticiPerNoList.contains(yoneticiPerNo)) {
								kendiYoneticiPerNoList.add(yoneticiPerNo);
								veriIsle("personel", yoneticiPerNo, veriSorguMap);
							}
							if (PdksUtil.hasStringValue(yonetici2PerNo) && !kendiYoneticiPerNoList.contains(yonetici2PerNo)) {
								kendiYoneticiPerNoList.add(yonetici2PerNo);
								veriIsle("personel", yonetici2PerNo, veriSorguMap);
							}
						} else {
							if (PdksUtil.hasStringValue(yoneticiPerNo) && !yoneticiPerNoList.contains(yoneticiPerNo)) {
								yoneticiPerNoList.add(yoneticiPerNo);
								veriIsle("personel", yoneticiPerNo, veriSorguMap);
							}
							if (PdksUtil.hasStringValue(yonetici2PerNo) && !yoneticiPerNoList.contains(yonetici2PerNo)) {
								yoneticiPerNoList.add(yonetici2PerNo);
								veriIsle("personel", yonetici2PerNo, veriSorguMap);
							}
						}
						perMap.put(personelERP.getPersonelNo(), personelERP);
					} catch (Exception e) {
					}

				}
				fields.clear();
				fields.put("beyazYakaDefault", Boolean.TRUE);
				fields.put("isKur", Boolean.FALSE);
				vardiyaSablonu = (VardiyaSablonu) pdksDAO.getObjectByInnerObject(fields, VardiyaSablonu.class);
				fields.clear();
				fields.put("beyazYakaDefault", Boolean.TRUE);
				fields.put("isKur", Boolean.TRUE);
				isKurVardiyaSablonu = (VardiyaSablonu) pdksDAO.getObjectByInnerObject(fields, VardiyaSablonu.class);

				List<CalismaModeli> modeller = pdksDAO.getObjectByInnerObjectList("durum", Boolean.TRUE, CalismaModeli.class);
				if (!modeller.isEmpty()) {
					if (modeller.size() == 1)
						calismaModeli = modeller.get(0);
					else
						for (CalismaModeli cm : modeller) {
							if (cm.getHaftaIci() == 9.0)
								calismaModeli = cm;
						}
				}
				fields.clear();
				if (veriSorguMap.containsKey("personel")) {
					fields.put("Map", "getSicilNo");
					fields.put("sicilNo", veriSorguMap.get("personel"));
				}
				TreeMap<String, PersonelKGS> personelKGSMap = null;
				if (!fields.isEmpty()) {
					if (kapiSirket != null)
						fields.put("kapiSirket.id", kapiSirket.getId());
					personelKGSMap = pdksDAO.getObjectByInnerObjectMap(fields, PersonelKGS.class, true);

				} else
					personelKGSMap = new TreeMap<String, PersonelKGS>();
				fields.clear();
				if (veriSorguMap.containsKey("personel")) {
					fields.put("Map", "getSicilNo");
					fields.put("sicilNo", veriSorguMap.get("personel"));
				}
				fields.clear();
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT  P.*   FROM  " + Personel.TABLE_NAME + "  P WITH(nolock) ");
				sb.append(" INNER JOIN " + PersonelKGS.TABLE_NAME + " K ON K." + PersonelKGS.COLUMN_NAME_ID + "=P." + Personel.COLUMN_NAME_KGS_PERSONEL + " AND K.PERSONEL_NO<>P." + Personel.COLUMN_NAME_PDKS_SICIL_NO);
				sb.append(" WHERE K." + PersonelKGS.COLUMN_NAME_SICIL_NO + " :k");
				List<String> personelNoList = new ArrayList<String>();
				for (String string : veriSorguMap.get("personel")) {
					personelNoList.add(PdksUtil.replaceAll(string, "'", ""));
				}
				fields.put("k", personelNoList);
				List<Personel> personelDigerList = pdksDAO.getNativeSQLList(fields, sb, Personel.class);
				TreeMap<String, Personel> personelDigerMap = new TreeMap<String, Personel>();
				for (Personel personel : personelDigerList) {
					PersonelKGS personelKGS = personel.getPersonelKGS();
					personelDigerMap.put(personelKGS.getSicilNo(), personel);
				}
				personelDigerList = null;
				TreeMap<String, ERPPersonel> personelERPHataliMap = !fields.isEmpty() ? pdksDAO.getObjectByInnerObjectMap(fields, ERPPersonel.class, true) : new TreeMap<String, ERPPersonel>();
				TreeMap<String, Sirket> sirketMap = veriSorguMap.containsKey("sirket") ? pdksDAO.getObjectByInnerObjectMap("getErpKodu", "erpKodu", veriSorguMap.get("sirket"), Sirket.class, false) : new TreeMap<String, Sirket>();
				TreeMap<String, Personel> personelPDKSMap = veriSorguMap.containsKey("personel") ? pdksDAO.getObjectByInnerObjectMap("getPdksSicilNo", "pdksSicilNo", veriSorguMap.get("personel"), Personel.class, false) : new TreeMap<String, Personel>();
				fields.clear();
				fields.put("Map", "getKodu");
				fields.put("tipi", Tanim.TIPI_PERSONEL_EK_SAHA);
				fields.put("durum", Boolean.TRUE);
				TreeMap<String, Tanim> personelEKSahaMap = pdksDAO.getObjectByInnerObjectMap(fields, Tanim.class, false);
				TreeMap<String, Tanim> personelEKSahaVeriMap = new TreeMap<String, Tanim>();
				for (String key : personelEKSahaMap.keySet()) {
					Tanim parentTanim = personelEKSahaMap.get(key);
					fields.clear();
					fields.put("tipi", Tanim.TIPI_PERSONEL_EK_SAHA_ACIKLAMA);
					fields.put("parentTanim.id", parentTanim.getId());
					List<Tanim> personelEKSahaAciklamaList = pdksDAO.getObjectByInnerObjectList(fields, Tanim.class);
					for (Tanim tanim : personelEKSahaAciklamaList) {
						tanim.setGuncellendi(Boolean.FALSE);
						personelEKSahaVeriMap.put(key + "_" + tanim.getErpKodu(), tanim);
					}
				}
				tanimGetir(personelEKSahaVeriMap, Tanim.TIPI_GIRIS_TIPI);
				tanimGetir(personelEKSahaVeriMap, Tanim.TIPI_CINSIYET);
				tanimGetir(personelEKSahaVeriMap, Tanim.TIPI_GOREV_TIPI);
				tanimGetir(personelEKSahaVeriMap, Tanim.TIPI_BORDRO_ALT_BIRIMI);
				tanimGetir(personelEKSahaVeriMap, Tanim.TIPI_ERP_MASRAF_YERI);
				tanimGetir(personelEKSahaVeriMap, Tanim.TIPI_TESIS);
				dataMap.put("personelEKSahaVeriMap", personelEKSahaVeriMap);
				dataMap.put("personelEKSahaMap", personelEKSahaMap);
				dataMap.put("personelPDKSMap", personelPDKSMap);
				dataMap.put("personelERPHataliMap", personelERPHataliMap);
				dataMap.put("personelKGSMap", personelKGSMap);
				dataMap.put("personelDigerMap", personelDigerMap);
				dataMap.put("sirketMap", sirketMap);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(PdksUtil.getDate(bugun));
				calendar.set(Calendar.DATE, 1);
				List<Personel> yoneticiList = new ArrayList<Personel>();
				ayBasi = PdksUtil.tariheGunEkleCikar(calendar.getTime(), -6);
				personelKontrolVerileriAyarla(pdksDAO);
				updateYonetici2 = false;
				// TODO Kendi yoneticisi personeller güncelleniyor
				if (perMap != null && !perMap.isEmpty())
					personelVeriYaz(yoneticiList, dataMap, kendiYoneticiPerNoList, "P");
				// TODO Yoneticiler personeller güncelleniyor
				if (!yoneticiPerNoList.isEmpty())
					personelVeriYaz(yoneticiList, dataMap, yoneticiPerNoList, "Y");
				// TODO Yonetici olmayan personeller güncelleniyor
				if (perMap != null && !perMap.isEmpty())
					personelVeriYaz(yoneticiList, dataMap, new ArrayList<String>(perMap.keySet()), "P");
				if (!yoneticiList.isEmpty()) {
					for (Iterator iterator = yoneticiList.iterator(); iterator.hasNext();) {
						Personel personel = (Personel) iterator.next();
						if (personel.getTmpYonetici() == null || personel.getTmpYonetici().getId() == null) {
							iterator.remove();
						} else {
							personel.setYoneticisi(personel.getTmpYonetici());
						}

					}
					if (!yoneticiList.isEmpty())
						pdksDAO.saveObjectList(yoneticiList);
				}
				List<PersonelERP> hataList = new ArrayList<PersonelERP>();

				for (PersonelERP personelERP : personelList) {
					if (personelERP.getPersonelNo() != null && personelPDKSMap.containsKey(personelERP.getPersonelNo())) {
						Personel personel = personelPDKSMap.get(personelERP.getPersonelNo());
						if (personel != null)
							personelERP.setId(personel.getId());
					}
					PersonelERP hataPersonelERP = null;
					if (personelERP.getHataList() != null && !personelERP.getHataList().isEmpty()) {
						personelERP.setId(null);
						hataPersonelERP = (PersonelERP) personelERP.clone();
						hataList.add(hataPersonelERP);
					}
					personelERP.setBolumAdi(null);
					personelERP.setBolumKodu(null);
					if (altBolumDurum) {
						personelERP.setBordroAltAlanKodu(null);
						personelERP.setBordroAltAlanAdi(null);

					}
					personelERP.setSanalPersonel(null);
					personelERP.setCinsiyeti(null);
					personelERP.setCinsiyetKodu(null);
					personelERP.setDepartmanAdi(null);
					personelERP.setDepartmanKodu(null);
					personelERP.setMasrafYeriAdi(null);
					personelERP.setMasrafYeriKodu(null);
					personelERP.setDogumTarihi(null);
					personelERP.setIseGirisTarihi(null);
					personelERP.setGrubaGirisTarihi(null);
					if (personelERP.getIstenAyrilmaTarihi() != null && personelERP.getYazildi().booleanValue())
						personelERP.setIstenAyrilmaTarihi(null);
					personelERP.setKidemTarihi(null);
					personelERP.setGorevi(null);
					personelERP.setGorevKodu(null);
					personelERP.setTesisKodu(null);
					personelERP.setTesisAdi(null);
					personelERP.setYoneticiPerNo(null);
					personelERP.setYonetici2PerNo(null);
					personelERP.setGrubaGirisTarihi(null);

				}
				if (personelList.size() > 1)
					saveIkinciYoneticiOlmazList("ikinciYoneticiOlmaz");

				if (yoneticiIdList != null && !yoneticiIdList.isEmpty()) {
					sb = new StringBuffer();
					sb.append(" WITH VERI AS ( ");
					if (ikinciYoneticiOlmaz != null) {
						sb.append(" SELECT  D." + PersonelDinamikAlan.COLUMN_NAME_PERSONEL + " AS ID FROM " + PersonelDinamikAlan.TABLE_NAME + " D WITH(nolock)");
						sb.append(" INNER JOIN " + Personel.TABLE_NAME + " Y ON Y. " + Personel.COLUMN_NAME_YONETICI + " =D." + PersonelDinamikAlan.COLUMN_NAME_PERSONEL);
						sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P. " + Personel.COLUMN_NAME_YONETICI + " =Y." + Personel.COLUMN_NAME_ID);
						sb.append(" WHERE  D." + PersonelDinamikAlan.COLUMN_NAME_ALAN + "=" + ikinciYoneticiOlmaz.getId());
						sb.append(" AND  " + PersonelDinamikAlan.COLUMN_NAME_DURUM_SECIM + "=1");
						sb.append(" UNION ");
					}
					sb.append(" SELECT U." + User.COLUMN_NAME_PERSONEL + " AS ID FROM " + User.TABLE_NAME + " U WITH(nolock)");
					sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + "=U." + User.COLUMN_NAME_PERSONEL);
					sb.append(" AND P." + Personel.COLUMN_NAME_DURUM + "=1 AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=convert(datetime,Convert(CHAR(8),GETDATE(), 112),112) ");
					sb.append(" AND P.IKINCI_YONETICI_IZIN_ONAYLA=0");
					sb.append(" WHERE U." + User.COLUMN_NAME_DURUM + "=1");
					sb.append(" ) ");
					sb.append(" SELECT DISTINCT P.* FROM VERI P ");
					sb.append(" WHERE  P.ID :p ");

					fields.clear();
					fields.put("p", yoneticiIdList);
					List<BigDecimal> list2 = pdksDAO.getNativeSQLList(fields, sb, null);
					if (!list2.isEmpty()) {
						LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
						map.put(BaseDAOHibernate.MAP_KEY_SELECT, "SP_GET_IKINCI_BIRINCI_YONETICI_UPDATE");
						pdksDAO.execSP(map);
					}
				}
				// if (!PdksUtil.getCanliSunucuDurum())
				// hataList.clear();
				if (!hataList.isEmpty()) {
					try {

						for (Iterator iterator = hataList.iterator(); iterator.hasNext();) {
							PersonelERP personelERP = (PersonelERP) iterator.next();
							if (personelERP.getHataList().isEmpty())
								iterator.remove();
							else if (personelERPHataliMap.containsKey(personelERP.getPersonelNo())) {
								ERPPersonel erpPersonel = personelERPHataliMap.get(personelERP.getPersonelNo());
								if (!erpPersonel.getDurum()) {
									personelERP.setHataList(null);
									iterator.remove();

								}
							}
						}
						if (!hataList.isEmpty()) {
							Gson gson = new Gson();
							String jsonStr = PdksUtil.toPrettyFormat(gson.toJson(hataList));
							String konu = uygulamaBordro + " savePersoneller problem";
							if (hataList.size() == 1) {
								PersonelERP personelERP = hataList.get(0);
								if (personelERP != null) {
									String adSoyad = (personelERP.getAdi() != null ? personelERP.getAdi().trim() + " " : "") + (personelERP.getSoyadi() != null ? personelERP.getSoyadi() : " ");
									if (PdksUtil.hasStringValue(adSoyad))
										konu += " [ " + adSoyad.trim() + " ]";
								}
							}
							mailMap.put("konu", konu);
							sb = new StringBuffer();
							sb.append("<p><b>" + uygulamaBordro + " pdks entegrasyon servisi savePersoneller fonksiyonunda hatalı veri var!</b></p>");
							sb.append("<TABLE class=\"mars\" style=\"width: 80%\">");
							boolean renkUyari = true;
							Sirket bosSirket = new Sirket();
							if (parentBordroTanimKoduStr == null)
								parentBordroTanimKoduStr = "";

							for (Iterator iterator = hataList.iterator(); iterator.hasNext();) {
								PersonelERP personelERP = (PersonelERP) iterator.next();
								String adSoyad = (personelERP.getAdi() != null ? personelERP.getAdi().trim() + " " : "") + (personelERP.getSoyadi() != null ? personelERP.getSoyadi() : " ");
								String sirketBilgi = "";
								boolean altBolumVar = altBolumDurum && personelERP.getBordroAltAlanAdi() != null;
								if (personelERP.getSirketAdi() != null || personelERP.getBolumAdi() != null || altBolumVar) {
									sirketBilgi += "";
									if (personelERP.getSirketAdi() != null)
										sirketBilgi = personelERP.getSirketAdi();
									if (personelERP.getTesisAdi() != null) {
										Sirket sirket = sirketMap != null && sirketMap.containsKey(personelERP.getSirketKodu()) ? sirketMap.get(personelERP.getSirketKodu()) : bosSirket;
										if (sirket.isTesisDurumu()) {
											if (PdksUtil.hasStringValue(sirketBilgi))
												sirketBilgi += " - " + personelERP.getTesisAdi();
											else
												sirketBilgi = personelERP.getTesisAdi();
										}
									}

									if (personelERP.getBolumAdi() != null || altBolumVar) {
										if (personelERP.getBolumAdi() != null) {
											if (!PdksUtil.hasStringValue(sirketBilgi))
												sirketBilgi = personelERP.getBolumAdi();
											else
												sirketBilgi += " / " + personelERP.getBolumAdi();
										}
										if (altBolumVar && sirketBilgi.indexOf(personelERP.getBordroAltAlanAdi()) < 0) {
											if (!PdksUtil.hasStringValue(sirketBilgi))
												sirketBilgi = personelERP.getBordroAltAlanAdi();
											else
												sirketBilgi += (personelERP.getBolumAdi() != null ? " - " : " / ") + personelERP.getBordroAltAlanAdi();
										}
									}

									sirketBilgi = "( " + sirketBilgi + " )";
								}
								sb.append("<TR class=\"" + (renkUyari ? "odd" : "even") + "\"><TD colspan=2 nowrap><b>" + (personelERP.getPersonelNo() != null ? personelERP.getPersonelNo() + " " : "") + adSoyad + " " + sirketBilgi + "</b></TD></TR>");
								List<String> veriHataList = personelERP.getHataList();
								for (int i = 0; i < veriHataList.size(); i++) {
									sb.append("<TR class=\"" + (renkUyari ? "odd" : "even") + "\"><TD width='10%'></TD><TD width='90%' nowrap>" + (veriHataList.size() > 1 ? (i + 1) + ". " : "") + veriHataList.get(i) + "</TD></TR>");
								}
								renkUyari = !renkUyari;
								if (iterator.hasNext())
									sb.append("<TR><TD colspan=2> </TD></TR>");
							}

							sb.append("</TABLE>");
							mailMap.put("mailIcerik", sb.toString());
							LinkedHashMap<String, Object> fileMap = new LinkedHashMap<String, Object>();
							String xml = getJsonToXML(jsonStr, "personel", PERSONEL_PROP_ORDER, "savePersoneller");
							fileMap.put("savePersoneller.xml", xml);
							mailMap.put("fileMap", fileMap);
							mailMapGuncelle("bccEntegrasyon", "bccEntegrasyonAdres");
							kullaniciIKYukle(mailMap, pdksDAO);
							MailManager.ePostaGonder(mailMap);
							mailBosGonder = false;
						}
					} catch (Exception em) {
						logger.error(em);
						em.printStackTrace();
					}

				}
				if (mailBosGonder)
					mailBosGonder("savePersoneller", "personel", personelList);
				if (updateYonetici2)
					setIkinciYoneticiSifirla();

				saveFonksiyonVeri(null, personelList);
				hataList = null;

				mesajInfoYaz("savePersoneller --> " + mesaj + " out " + new Date());
			} else {
				for (PersonelERP personelERP2 : personelList) {
					personelERP2.getHataList().add(servisAdi + " servisi  kapalıdır!");
				}
			}
		}

	}

	/**
	 * @param jsonStr
	 * @param arrayTag
	 * @param dizi
	 * @param rootName
	 * @return
	 */
	private String getJsonToXML(String jsonStr, String arrayTag, String[] dizi, String rootName) {
		jsonStr = "{\"" + arrayTag + "\":" + jsonStr + "}";
		Gson gson = new Gson();
		LinkedHashMap<String, Object> headerMap = gson.fromJson(jsonStr, LinkedHashMap.class);
		jsonStr = "";
		if (headerMap != null) {
			List<LinkedTreeMap> tableArray = (List<LinkedTreeMap>) headerMap.get(arrayTag);
			if (tableArray != null) {
				LinkedHashMap<String, String> map = WSLoggingOutInterceptor.getChangeMap();
				String hataList = "hataList";
				StringBuffer sb = new StringBuffer();
				for (LinkedTreeMap linkedHashMap : tableArray) {
					sb.append("<" + arrayTag + ">");
					for (String key : dizi) {
						if (linkedHashMap.containsKey(key)) {
							String str = linkedHashMap.get(key).toString();
							for (String pattern : map.keySet()) {
								String replace = map.get(pattern);
								if (str.indexOf(replace) >= 0)
									str = PdksUtil.replaceAll(str, replace, pattern);
							}
							sb.append("<" + key + ">" + str);
							sb.append("</" + key + ">");
						}
					}
					if (linkedHashMap.containsKey(hataList)) {
						List list1 = (List) linkedHashMap.get(hataList);
						for (Object object : list1) {
							String str = object.toString();
							for (String pattern : map.keySet()) {
								String replace = map.get(pattern);
								if (str.indexOf(replace) >= 0)
									str = PdksUtil.replaceAll(str, replace, pattern);
							}
							sb.append("<" + hataList + ">" + str);
							sb.append("</" + hataList + ">");
						}
					}
					sb.append("</" + arrayTag + ">");
				}

				jsonStr = PdksUtil.getJsonToXML(sb.toString(), rootName, null);
				for (String pattern : map.keySet()) {
					String replace = map.get(pattern);
					if (jsonStr.indexOf(pattern) >= 0)
						jsonStr = PdksUtil.replaceAllManuel(jsonStr, pattern, replace);
				}

			}
		}
		return jsonStr;
	}

	/**
	 * @param list
	 * @param str
	 */
	private void addHatalist(List<String> list, String str) {
		if (PdksUtil.hasStringValue(str)) {
			list.add(str);
		} else {
			logger.debug("aa");
		}

	}

	public String getParameterKey(String key) {
		String parameterKey = null;
		try {
			parameterKey = mailMap != null && mailMap.containsKey(key) ? ((String) mailMap.get(key)).trim() : "";
		} catch (Exception e) {
			parameterKey = "";
		}
		logger.debug(key + "='" + parameterKey + "'");
		return parameterKey;

	}

	/**
	 * @param key
	 * @param defaultBaslik
	 * @return
	 */
	private String getBaslikAciklama(String key, String defaultBaslik) {
		String aciklama = getParameterKey(key);
		if (!PdksUtil.hasStringValue(aciklama))
			aciklama = defaultBaslik;
		return aciklama;
	}

	public HashMap<String, Object> getMailMap() {
		return mailMap;
	}

	public void setMailMap(HashMap<String, Object> mailMap) {
		this.mailMap = mailMap;
	}

	public User getIslemYapan() {
		return islemYapan;
	}

	public void setIslemYapan(User islemYapan) {
		this.islemYapan = islemYapan;
	}

	public VardiyaSablonu getVardiyaSablonu() {
		return vardiyaSablonu;
	}

	public void setVardiyaSablonu(VardiyaSablonu vardiyaSablonu) {
		this.vardiyaSablonu = vardiyaSablonu;
	}

	public String getMesaj() {
		return mesaj;
	}

	public void setMesaj(String mesaj) {
		this.mesaj = mesaj;
	}

	public String getDosyaEkAdi() {
		return dosyaEkAdi;
	}

	public void setDosyaEkAdi(String dosyaEkAdi) {
		this.dosyaEkAdi = dosyaEkAdi;
	}

	public Tanim getBosDepartman() {
		return bosDepartman;
	}

	public void setBosDepartman(Tanim bosDepartman) {
		this.bosDepartman = bosDepartman;
	}
}
