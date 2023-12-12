package org.pdks.quartz;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.faces.Renderer;
import org.pdks.entity.Dosya;
import org.pdks.entity.MailGrubu;
import org.pdks.entity.Parameter;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.erp.action.ERPController;
import org.pdks.security.action.StartupAction;
import org.pdks.security.entity.User;
import org.pdks.session.LDAPUserManager;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

@Name("personelERPGuncelleme")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class PersonelERPGuncelleme implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -421170994640099576L;

	static Logger logger = Logger.getLogger(PersonelERPGuncelleme.class);

	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	HashMap<String, String> parameterMap;
	@In(required = false, create = true)
	Zamanlayici zamanlayici;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	Renderer renderer;
	@In(required = false, create = true)
	StartupAction startupAction;
	private static boolean calisiyor = Boolean.FALSE;

	private static final String PARAMETER_KEY = "personelERPZamani";
	private String hataKonum, aciklama;
	private ByteArrayOutputStream bo = null;
	private Dosya dosya = null;
	private List<User> toList;
	private List<Personel> personelList, personelERPList;
	private static boolean ozelKontrol = Boolean.FALSE;
	private User islemUser = new User();

	@Asynchronous
	@SuppressWarnings("unchecked")
	@Transactional
	// @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public QuartzTriggerHandle personelERPGuncellemeTimer(@Expiration Date when, @IntervalCron String interval) {
		hataKonum = "personelERPGuncellemeTimer başladı ";
		if (pdksEntityController != null && !isCalisiyor()) {
			ozelKontrol = Boolean.FALSE;
			setCalisiyor(Boolean.TRUE);
			boolean hataGonder = Boolean.FALSE;
			Session session = null;
			try {
				session = PdksUtil.getSession(entityManager, Boolean.TRUE);
				if (PdksUtil.getCanliSunucuDurum() || PdksUtil.getTestSunucuDurum()) {
					Parameter parameter = ortakIslemler.getParameter(session, "kgsMasterUpdate");
					if (parameter != null && parameter.getValue().equals("1"))
						ortakIslemler.kgsMasterUpdate(session);
					hataKonum = "Paramatre okunuyor ";
					parameter = ortakIslemler.getParameter(session, PARAMETER_KEY);
					String value = (parameter != null) ? parameter.getValue() : null;
					hataKonum = "Paramatre okundu ";
					if (value != null) {

						hataGonder = Boolean.TRUE;
						hataKonum = "Zaman kontrolu yapılıyor ";
						Date tarih = zamanlayici.getDbTime(session);
						boolean zamanDurum = PdksUtil.zamanKontrol(PARAMETER_KEY, value, tarih) && ortakIslemler.getGuncellemeDurum(Personel.TABLE_NAME, session);
						// if (!zamanDurum)
						// zamanDurum = pdksUtil.getUrl() != null && pdksUtil.getUrl().indexOf("localhost") >= 0;

						if (zamanDurum) {

							personelERPGuncellemeCalistir(session, tarih, true);

						}
					}
				}

			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				logger.error("personelERPGuncelle hata " + e.getMessage() + " " + new Date());
				if (hataGonder)
					try {
						zamanlayici.mailGonder(session, null, "ERP personel bilgileri güncellemesi", "ERP personel bilgileri güncelleme tamamlanmadı." + e.getMessage() + " ( " + hataKonum + " )", null, Boolean.TRUE);

					} catch (Exception e2) {
						logger.error("personelERPGuncellemeTimer 2 : " + e2.getMessage());

					}
			} finally {
				if (session != null)
					session.close();
				setCalisiyor(Boolean.FALSE);

			}
		}
		return null;
	}

	/**
	 * @param session
	 * @param time
	 * @throws Exception
	 */
	public void personelERPGuncellemeCalistir(Session session, Date time, Boolean mailGonder) throws Exception {
		logger.info("personelERPGuncelleme  basladi " + new Date());
		if (time == null)
			time = zamanlayici.getDbTime(session);
		ozelKontrol = zamanlayici.getOzelKontrol(session);
		User sistemAdminUser = ortakIslemler.getSistemAdminUser(session);

		try {
			List<Long> personelIdList = personelERPGuncelle(sistemAdminUser, session);
			if (!personelIdList.isEmpty()) {
				HashMap fields = new HashMap();
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				fields.put("id", personelIdList);
				List<PersonelView> personelList = ortakIslemler.getPersonelViewByPersonelKGSList(pdksEntityController.getObjectByInnerObjectList(fields, PersonelKGS.class));
				HashMap sonucMap = ortakIslemler.fillEkSahaTanimBul(Boolean.TRUE, null, session);
				TreeMap<String, Tanim> tanimMap = (TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap");
				if (sistemAdminUser != null)
					sistemAdminUser.setAdmin(Boolean.TRUE);
				bo = ortakIslemler.personelExcelDevam(Boolean.TRUE, personelList, tanimMap, sistemAdminUser, null, session);
				if (bo != null) {
					List<Dosya> fileList = new ArrayList<Dosya>();
					Dosya dosyaExcel = new Dosya();
					if (time == null)
						time = new Date();
					dosyaExcel.setDosyaAdi("personelERP_" + PdksUtil.convertToDateString(time, "yyyyMMdd") + "Listesi.xlsx");
					dosyaExcel.setDosyaIcerik(bo.toByteArray());
					fileList.add(dosyaExcel);
					String zipDosyaAdi = "personelERP_" + PdksUtil.convertToDateString(time, "yyyyMMdd") + ".zip";
					File file = ortakIslemler.dosyaZipFileOlustur(zipDosyaAdi, fileList);
					if (file != null && file.exists()) {
						dosya = ortakIslemler.dosyaFileOlustur(zipDosyaAdi, file, Boolean.TRUE);
						file.deleteOnExit();
					}
				}

			}

		} catch (Exception ex) {
			logger.error(ex);
			ex.printStackTrace();
		}

		try {
			logger.info("ozel islemler in  " + new Date());
			kullaniciGuncelle(session, null);

			ortakIslemler.yeniPersonelleriOlustur(session);

			if (ozelKontrol)
				hataliVeriPersonelBul(session, null);
			ortakIslemler.setIkinciYoneticiSifirla(session);
			logger.info("ozel islemler out " + new Date());
			logger.info("aktif Mail Adress Guncelle in " + new Date());
			aktifMailAdressGuncelle(session);
			logger.info("aktif Mail Adress Guncelle out " + new Date());
		} catch (Exception e) {
			hataKonum = "ozel islemler tamamlandı ";
		}
		if (mailGonder) {
			if (dosya != null)
				zamanlayici.mailGonderDosya(session, null, "SAP personel bilgileri güncellemesi", "SAP personel bilgileri güncelleme tamamlandı.", null, dosya, Boolean.TRUE);
			else
				zamanlayici.mailGonder(session, null, "SAP personel bilgileri güncellemesi", "SAP personel bilgileri güncelleme tamamlandı.", null, Boolean.TRUE);
		}
		logger.info("personelERPGuncelle bitti " + new Date());
	}

	public User getLdapKullanici(String kullaniciAdi) {
		User kisaKullanici = null;
		if (kullaniciAdi.indexOf("@") < 0)
			kisaKullanici = ortakIslemler.kullaniciBul(kullaniciAdi, LDAPUserManager.USER_ATTRIBUTES_SAM_ACCOUNT_NAME);
		else {
			kullaniciAdi = PdksUtil.getMailAdres(kullaniciAdi);
			kisaKullanici = ortakIslemler.kullaniciBul(kullaniciAdi, LDAPUserManager.USER_ATTRIBUTES_MAIL);
			if (kisaKullanici == null)
				kisaKullanici = ortakIslemler.kullaniciBul(kullaniciAdi, LDAPUserManager.USER_ATTRIBUTES_PRINCIPAL_NAME);
		}
		if (kisaKullanici != null && !kisaKullanici.isDurum())
			kisaKullanici = null;

		return kisaKullanici;
	}

	@Transactional
	public void kullaniciGuncelle(Session session, User user) {
		if (session == null)
			session = PdksUtil.getSession(entityManager, user == null);
		Parameter parameterEmailBozuk = ortakIslemler.getParameter(session, "emailBozuk");
		boolean emailBozuk = parameterEmailBozuk != null;
		HashMap fields = new HashMap();
		fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		fields.put("durum=", Boolean.TRUE);
		fields.put("pdksPersonel.durum=", Boolean.TRUE);
		fields.put("pdksPersonel.sirket.ldap=", Boolean.TRUE);
		fields.put("pdksPersonel.sskCikisTarihi>=", PdksUtil.getDate(Calendar.getInstance().getTime()));
		List<User> list = pdksEntityController.getObjectByInnerObjectListInLogic(fields, User.class);
		logger.info("kullaniciGuncelle in " + list.size() + " --> " + new Date());

		List<User> hataliList = new ArrayList<User>();
		while (!list.isEmpty()) {
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				User kullanici = (User) iterator.next();

				String kullaniciAdi = kullanici.getUsername().trim();
				try {
					User kisaKullanici = getLdapKullanici(kullaniciAdi);
					if (kisaKullanici == null && kullanici.getShortUsername() != null)
						kisaKullanici = getLdapKullanici(kullanici.getShortUsername());
					if (kisaKullanici == null && kullaniciAdi.indexOf("anadolusaglik") > 0 && kullanici.getPdksPersonel().getSicilNo().length() == 8) {
						kullaniciAdi = "ASM" + kullanici.getPdksPersonel().getSicilNo().substring(3);
						kisaKullanici = ortakIslemler.kullaniciBul(kullaniciAdi, LDAPUserManager.USER_ATTRIBUTES_SAM_ACCOUNT_NAME);
					}
					if (kisaKullanici != null && !kisaKullanici.isDurum())
						kisaKullanici = null;
					if (kisaKullanici != null) {
						if (!kullanici.getUsername().equals(kisaKullanici.getUsername()) || kullanici.getShortUsername() == null || !kullanici.getShortUsername().equals(kisaKullanici.getShortUsername())) {
							logger.info(kullanici.getId() + " : " + kisaKullanici.getUsername() + " --> " + kisaKullanici.getShortUsername() + " - " + kisaKullanici.getEmail());
							kullanici.setUsername(kisaKullanici.getUsername());
							kullanici.setShortUsername(kisaKullanici.getShortUsername());
							if (!emailBozuk)
								kullanici.setEmail(kisaKullanici.getEmail());
							try {
								session.clear();
								pdksEntityController.saveOrUpdate(session, entityManager, kullanici);
								session.flush();

							} catch (Exception ee) {
								logger.error("PDKS hata in : \n" + (kullanici != null ? kullanici.getUsername() : ""));
								ee.printStackTrace();
								logger.error("PDKS hata out : " + ee.getMessage());
								if (hataliList != null)
									hataliList.add(kullanici);
							}
						}

					}
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					logger.info("Hata : " + e.getMessage());

				}
				iterator.remove();
			}
			if (hataliList != null) {
				if (!hataliList.isEmpty()) {
					session.clear();
					Collections.shuffle(hataliList);
					list.addAll(hataliList);
				}
				hataliList = null;
			}
		}

		logger.info("kullaniciGuncelle out " + new Date());

		list = null;
		fields = null;

	}

	@Transactional
	public List<Long> personelERPGuncelle(User user, Session session) throws Exception {
		boolean ekran = user == null;
		if (ekran)
			user = ortakIslemler.getSistemAdminUser(session);
		List<Long> personelList = new ArrayList<Long>();
		setCalisiyor(Boolean.TRUE);
		logger.info("personelERPGuncelleme in " + new Date());
		Calendar cal = Calendar.getInstance();
		Date bugun = (Date) cal.getTime();
		cal.add(Calendar.HOUR_OF_DAY, -6);
		Date tarih = PdksUtil.getDate(cal.getTime());
		if (!ekran)
			session.clear();

		HashMap map = new HashMap();

		map.put(PdksEntityController.MAP_KEY_MAP, "getKodu");
		map.put("tipi", Tanim.TIPI_SAP_MASRAF_YERI);
		map.put(PdksEntityController.MAP_KEY_SESSION, session);
		TreeMap masrafYeriMap = pdksEntityController.getObjectByInnerObjectMap(map, Tanim.class, Boolean.FALSE);
		map.clear();

		map.put(PdksEntityController.MAP_KEY_MAP, "getKodu");
		map.put("tipi", Tanim.TIPI_BORDRO_ALT_BIRIMI);
		map.put(PdksEntityController.MAP_KEY_SESSION, session);
		TreeMap bordroAltBirimiMap = pdksEntityController.getObjectByInnerObjectMap(map, Tanim.class, Boolean.FALSE);
		// ArrayList<String> perList = null;
		map.clear();

		// map.put("durum=", Boolean.TRUE);
		map.put("sirket.durum=", Boolean.TRUE);
		map.put("sirket.sap=", Boolean.TRUE);
		map.put("sskCikisTarihi>=", tarih);
		map.put(PdksEntityController.MAP_KEY_SESSION, session);
		hataKonum = "personelERPGuncelle basladi ";
		List<Personel> list = pdksEntityController.getObjectByInnerObjectListInLogic(map, Personel.class);

		logger.info("personelERPGuncelle basladi. (" + list.size() + ") " + new Date());
		String sicilNo = "";
		int sayac = 0;
		List<Personel> hataliPersonelList = new ArrayList<Personel>();
		HashMap<Long, User> guncelleyenMap = new HashMap<Long, User>();

		while (!list.isEmpty() && sayac < 20) {
			sayac++;
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Personel pdksPersonel = (Personel) iterator.next();

				try {
					if (PdksUtil.hasStringValue(pdksPersonel.getSicilNo()) == false) {
						iterator.remove();
						continue;
					}
					sicilNo = String.valueOf(Long.parseLong(pdksPersonel.getSicilNo().trim()));
					hataKonum = (list.size() - guncelleyenMap.size()) + ". personelERPGuncelleme : " + pdksPersonel.getSicilNo().trim() + " " + pdksPersonel.getAdSoyad();
					// logger.info(pdksUtil.setTurkishStr(hataKonum));
				} catch (Exception e) {
					logger.error(PdksUtil.setTurkishStr("HATALI PERSONEL NO " + pdksPersonel.getSicilNo() + " " + pdksPersonel.getAdSoyad()));
					sicilNo = "";
				}
				try {
					if (sicilNo.length() == 8) {
						Boolean durum = ortakIslemler.sapVeriGuncelle(session, user, bordroAltBirimiMap, masrafYeriMap, pdksPersonel, null, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);

						if (!durum.equals(pdksPersonel.getDurum())) {
							map.clear();
							map.put("id", pdksPersonel.getId());
							map.put(PdksEntityController.MAP_KEY_SESSION, session);
							Personel personel = (Personel) pdksEntityController.getObjectByInnerObject(map, Personel.class);
							personel.setDurum(durum);
							personel.setGuncellemeTarihi(bugun);
							personel.setGuncelleyenUser(user);
							pdksEntityController.saveOrUpdate(session, entityManager, personel);
							logger.error(PdksUtil.setTurkishStr(pdksPersonel.getSicilNo() + " " + pdksPersonel.getAdSoyad() + " SAP'den anaveri bilgisi okunamadı! "));
						}

						session.flush();
						if (pdksPersonel.getPersonelKGS() != null)
							personelList.add(pdksPersonel.getPersonelKGS().getId());
					}
					iterator.remove();
				} catch (Exception e1) {
					if (hataliPersonelList != null)
						hataliPersonelList.add(pdksPersonel);
					iterator.remove();
					logger.info(PdksUtil.setTurkishStr(hataKonum));

					logger.error("personelERPGuncellemeTimer hata 1 : " + sicilNo + " " + e1.getMessage());
					break;
				}

			}
			if (!list.isEmpty()) {

				if (list.size() > 1)
					Collections.shuffle(list);
			} else if (hataliPersonelList != null && !hataliPersonelList.isEmpty()) {
				Collections.shuffle(hataliPersonelList);
				list.addAll(hataliPersonelList);
				hataliPersonelList = null;
				sayac = 5;
			}
		}
		// session.clear();

		hataKonum = "personelERPGuncelle tamamlandı ";
		if (ekran)
			PdksUtil.addMessageInfo("SAP personel bilgileri güncelleme tamamlandı.");

		logger.info("personelERPGuncelleme out " + new Date());
		return personelList;
	}

	/**
	 * @param session
	 * @throws Exception
	 */
	public void hataliVeriPersonelBul(Session session, User user) throws Exception {
		try {
			sapOlmayanPersonelBul(session);
		} catch (Exception e) {

		}

		StringBuffer sb = new StringBuffer();
		sb.append("select P.* from " + Personel.TABLE_NAME + " P  WITH(nolock) ");
		sb.append(" INNER JOIN " + Sirket.TABLE_NAME + " S ON S." + Sirket.COLUMN_NAME_ID + "=P." + Personel.COLUMN_NAME_SIRKET + " AND S." + Sirket.COLUMN_NAME_DURUM + "=1 AND S.ERP_DURUM=1 ");
		sb.append(" AND S." + Sirket.COLUMN_NAME_PDKS + "=1  AND S.FAZLA_MESAI_DURUM=1   ");
		sb.append(" WHERE P." + Personel.COLUMN_NAME_DURUM + "=0 and P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=CAST(GETDATE() AS date)");
		sb.append(" ORDER BY P." + Personel.COLUMN_NAME_PDKS_SICIL_NO);
		HashMap fields = new HashMap();
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Personel> hataliVeriPersonelList = pdksEntityController.getObjectBySQLList(sb, fields, Personel.class);
		if (!hataliVeriPersonelList.isEmpty() || !personelERPList.isEmpty())
			hataliVeriPersonelMail(hataliVeriPersonelList, user, session);
		hataliVeriPersonelList = null;
	}

	/**
	 * @param session
	 * @throws Exception
	 */
	private void sapOlmayanPersonelBul(Session session) throws Exception {
		Parameter parameter = ortakIslemler.getParameter(session, "sapKodu");
		List<Personel> list = null;
		if (parameter != null && parameter.getActive() != null && parameter.getActive()) {
			String sapKodu = parameter.getValue();
			if (sapKodu != null && sapKodu.trim().length() == 4) {
				StringBuffer sb = new StringBuffer();
				sb.append("select P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " from " + Personel.TABLE_NAME + " P  WITH(nolock)");
				sb.append(" INNER JOIN " + Sirket.TABLE_NAME + " S ON S." + Sirket.COLUMN_NAME_ID + "=P." + Personel.COLUMN_NAME_SIRKET + " AND S." + Sirket.COLUMN_NAME_DURUM + "=1 AND S.ERP_DURUM=1 ");
				sb.append(" AND S." + Sirket.COLUMN_NAME_PDKS + "=1  AND S.FAZLA_MESAI_DURUM=1 AND S.SAP_KODU=:sapKodu ");
				sb.append(" WHERE P." + Personel.COLUMN_NAME_DURUM + "=1 and P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=CAST(GETDATE() AS date)");
				sb.append(" ORDER BY P." + Personel.COLUMN_NAME_PDKS_SICIL_NO);
				HashMap fields = new HashMap();
				fields.put("sapKodu", sapKodu.trim());
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<String> pdksPersonelList = pdksEntityController.getObjectBySQLList(sb, fields, null);
				ERPController controller = ortakIslemler.getERPController();
				list = controller.pdksTanimsizPersonel(pdksPersonelList, sapKodu);
			}
		} else
			list = new ArrayList<Personel>();
		setPersonelERPList(list);

	}

	/**
	 * @param session
	 * @return
	 */
	@Transactional
	public String aktifMailAdressGuncelle(Session session) {
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT DISTINCT P.*  FROM  " + Personel.TABLE_NAME + " P WITH(nolock) ");
		sb.append(" WHERE P." + Personel.COLUMN_NAME_DURUM + "=1 AND (" + Personel.COLUMN_NAME_MAIL_CC_ID + " IS NOT NULL OR " + Personel.COLUMN_NAME_MAIL_BCC_ID + " IS NOT NULL OR " + Personel.COLUMN_NAME_HAREKET_MAIL_ID + " IS NOT NULL)");
		sb.append(" AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >=CAST(GETDATE() AS date)   ");
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			List<Personel> userList = pdksEntityController.getObjectBySQLList(sb, fields, Personel.class);
			if (!userList.isEmpty()) {
				TreeMap<String, String> mailMap = new TreeMap<String, String>();
				for (Personel user : userList) {
					if (user.getMailGrubuCC() != null)
						mailMap.put(user.getMailGrubuCC().getEmail(), user.getMailGrubuCC().getEmail());
					if (user.getMailGrubuBCC() != null)
						mailMap.put(user.getMailGrubuBCC().getEmail(), user.getMailGrubuBCC().getEmail());
					if (user.getHareketMailGrubu() != null)
						mailMap.put(user.getHareketMailGrubu().getEmail(), user.getHareketMailGrubu().getEmail());
				}
				if (!mailMap.isEmpty()) {
					List<String> mailList = new ArrayList<String>(mailMap.keySet());
					for (String mail : mailList) {
						String mailBox = ortakIslemler.getAktifMailAdress(mail, session);
						mailMap.put(mail, mailBox);
					}
					List<MailGrubu> saveMailGrubuList = new ArrayList<MailGrubu>(), deleteMailGrubuList = new ArrayList<MailGrubu>();
					for (Personel user : userList) {
						deleteMailGrubuList.clear();
						saveMailGrubuList.clear();
						if (user.getMailGrubuCC() != null) {
							String mail = mailMap.get(user.getMailGrubuCC().getEmail());
							if (!mail.equals(user.getMailGrubuCC().getEmail())) {
								if (PdksUtil.hasStringValue(mail)) {
									user.setEmailCC(mail);
									saveMailGrubuList.add(user.getMailGrubuCC());
								} else if (user.getMailGrubuCC().getId() != null) {
									deleteMailGrubuList.add(user.getMailGrubuCC());
									user.setMailGrubuCC(null);
								}
							}

						}

						if (user.getMailGrubuBCC() != null) {
							String mail = mailMap.get(user.getMailGrubuBCC().getEmail());
							if (!mail.equals(user.getMailGrubuBCC().getEmail())) {
								if (PdksUtil.hasStringValue(mail)) {
									user.setEmailBCC(mail);
									saveMailGrubuList.add(user.getMailGrubuBCC());
								} else if (user.getMailGrubuBCC().getId() != null) {
									deleteMailGrubuList.add(user.getMailGrubuBCC());
									user.setMailGrubuBCC(null);
								}
							}

						}
						if (user.getHareketMailGrubu() != null) {
							String mail = mailMap.get(user.getHareketMailGrubu().getEmail());
							if (!mail.equals(user.getHareketMailGrubu().getEmail())) {
								if (PdksUtil.hasStringValue(mail)) {
									user.setEmailBCC(mail);
									saveMailGrubuList.add(user.getHareketMailGrubu());
								} else if (user.getHareketMailGrubu().getId() != null) {
									deleteMailGrubuList.add(user.getHareketMailGrubu());
									user.setHareketMailGrubu(null);
								}
							}

						}
						if (!deleteMailGrubuList.isEmpty() || !saveMailGrubuList.isEmpty()) {
							if (!saveMailGrubuList.isEmpty()) {
								boolean userUpdate = false;
								for (MailGrubu mailGrubu : saveMailGrubuList) {
									if (!userUpdate)
										userUpdate = mailGrubu.getId() == null;
									pdksEntityController.saveOrUpdate(session, entityManager, mailGrubu);
								}
								if (userUpdate)
									pdksEntityController.saveOrUpdate(session, entityManager, user);
							}
							if (!deleteMailGrubuList.isEmpty()) {
								pdksEntityController.saveOrUpdate(session, entityManager, user);
								for (MailGrubu mailGrubu : deleteMailGrubuList) {
									try {
										pdksEntityController.deleteObject(session, entityManager, mailGrubu);
									} catch (Exception e) {
										logger.error(e);
									}
								}
							}
							session.flush();
						}

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}

		return "";

	}

	/**
	 * @param hataliVeriPersonelList
	 * @param session
	 */
	private void hataliVeriPersonelMail(List<Personel> hataliVeriPersonelList, User loginUser, Session session) {
		try {
			if (!personelERPList.isEmpty() || !hataliVeriPersonelList.isEmpty()) {
				Calendar cal = Calendar.getInstance();
				int haftaGunu = cal.get(Calendar.DAY_OF_WEEK);
				if (haftaGunu != Calendar.SATURDAY && haftaGunu != Calendar.SUNDAY) {
					List<User> userList = loginUser == null ? ortakIslemler.IKKullanicilariBul(null, null, session) : new ArrayList<User>();
					if (loginUser == null) {
						for (Iterator iterator = userList.iterator(); iterator.hasNext();) {
							User user = (User) iterator.next();
							if (!user.getDepartman().isAdminMi())
								iterator.remove();
							else {
								Sirket sirket = user.getPdksPersonel() != null ? user.getPdksPersonel().getSirket() : null;
								if (sirket == null || sirket.getDurum().booleanValue() == false || sirket.getPdks().booleanValue() == false || sirket.isErp() == false)
									iterator.remove();
							}
						}
					} else
						userList.add(loginUser);

					if (!userList.isEmpty()) {
						setPersonelList(hataliVeriPersonelList);
						setToList(userList);
						ortakIslemler.mailGonder(renderer, "/email/personelSAPProblemMail.xhtml");

					}
					userList = null;
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}

	}

	public String getHataKonum() {
		return hataKonum;
	}

	public void setHataKonum(String hataKonum) {
		this.hataKonum = hataKonum;
	}

	public static boolean isCalisiyor() {
		return calisiyor;
	}

	public static void setCalisiyor(boolean calisiyor) {
		PersonelERPGuncelleme.calisiyor = calisiyor;
	}

	public List<User> getToList() {
		return toList;
	}

	public void setToList(List<User> toList) {
		this.toList = toList;
	}

	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	public List<Personel> getPersonelList() {
		return personelList;
	}

	public void setPersonelList(List<Personel> personelList) {
		this.personelList = personelList;
	}

	public static boolean isOzelKontrol() {
		return ozelKontrol;
	}

	public static void setOzelKontrol(boolean ozelKontrol) {
		PersonelERPGuncelleme.ozelKontrol = ozelKontrol;
	}

	public List<Personel> getPersonelERPList() {
		return personelERPList;
	}

	public void setPersonelERPList(List<Personel> personelERPList) {
		this.personelERPList = personelERPList;
	}

	public User getIslemUser() {
		return islemUser;
	}

	public void setIslemUser(User islemUser) {
		this.islemUser = islemUser;
	}

}