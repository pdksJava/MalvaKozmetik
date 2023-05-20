package org.pdks.quartz;

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
import org.hibernate.SQLQuery;
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
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Parameter;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.PersonelIzinDetay;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.security.action.StartupAction;
import org.pdks.security.entity.User;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

@Name("izinBakiyeGuncelleme")
@AutoCreate
// @Synchronized(timeout=15000)
@Scope(ScopeType.APPLICATION)
public class IzinBakiyeGuncelleme {

	/**
	 * 
	 */
	static Logger logger = Logger.getLogger(IzinBakiyeGuncelleme.class);

	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	HashMap<String, String> parameterMap;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	Zamanlayici zamanlayici;
	@In(required = false, create = true)
	Renderer renderer;
	@In(required = false, create = true)
	StartupAction startupAction;
	private static boolean calisiyor = Boolean.FALSE;
	private int yil;
	private String hataKonum;
	private List<User> toList;
	private List<PersonelIzin> izinList;
	private static final String PARAMETER_KEY = "izinBakiyeGuncelleme";
	private static boolean ozelKontrol = Boolean.FALSE;

	@Asynchronous
	@SuppressWarnings("unchecked")
	@Transactional
	// @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public QuartzTriggerHandle izinBakiyeGuncellemeTimer(@Expiration Date when, @IntervalCron String interval) {
		hataKonum = "izinBakiyeGuncellemeTimer başladı ";
		if (pdksEntityController != null && !isCalisiyor()) {
			ozelKontrol = Boolean.FALSE;

			setCalisiyor(Boolean.TRUE);
			boolean hataGonder = Boolean.FALSE;
			Session session = null;

			try {
				if (PdksUtil.getCanliSunucuDurum() || PdksUtil.getTestSunucuDurum()) {
					session = PdksUtil.getSession(entityManager, Boolean.TRUE);
					hataKonum = "Paramatre okunuyor ";
					Parameter parameter = null;
					HashMap fields = new HashMap();
					fields.put("durum=", Boolean.TRUE);
					fields.put("departman.izinGirilebilir=", Boolean.TRUE);
					fields.put("personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<IzinTipi> list = pdksEntityController.getObjectByInnerObjectListInLogic(fields, IzinTipi.class);
					if (!list.isEmpty())
						parameter = ortakIslemler.getParameter(session, PARAMETER_KEY);
					String value = (parameter != null) ? parameter.getValue() : null;
					hataKonum = "Paramatre okundu ";
					if (value != null) {
						Date time = zamanlayici.getDbTime(session);
						hataGonder = Boolean.TRUE;
						hataKonum = "Zaman kontrolu yapılıyor ";
						boolean zamanDurum = PdksUtil.zamanKontrol(PARAMETER_KEY, value, time) && ortakIslemler.getGuncellemeDurum(session);

						// if (!zamanDurum)
						// zamanDurum = PdksUtil.getTestDurum();

						if (zamanDurum)
							izinBakiyeGuncellemeCalistir(session, true);

					}
				}
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				if (hataGonder)
					try {
						zamanlayici.mailGonder(session, "İzin Bakiye Güncellemesi", "İzin bakiyeleri güncellenmemiştir." + e.getMessage() + " ( " + hataKonum + " )", null, Boolean.TRUE);

					} catch (Exception e2) {
						logger.error("izinBakiyeGuncellemeTimer 2 : " + e2.getMessage());
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
	 * @param mailGonder
	 * @throws Exception
	 */
	public void izinBakiyeGuncellemeCalistir(Session session, Boolean mailGonder) throws Exception {
		logger.info("izinleriHesapla basladi " + new Date());
		ozelKontrol = zamanlayici.getOzelKontrol(session);
		User sistemAdminUser = ortakIslemler.getSistemAdminUser(session);
		hataKonum = "izinleriHesapla başladı ";
		if (sistemAdminUser != null)
			sistemAdminUser.setLogin(Boolean.FALSE);
		izinleriHesapla(sistemAdminUser, session);
		if (mailGonder)
			zamanlayici.mailGonder(session, "İzin Bakiye Güncellemesi", "İzin bakiyeleri güncellenmiştir.", null, Boolean.TRUE);
		logger.info("izinleriHesapla bitti " + new Date());
	}

	/**
	 * @param session
	 * @throws Exception
	 */
	public void ciftBakiyeIzinKontrol(Session session) throws Exception {
		HashMap parametreMap = new HashMap();
		StringBuffer sb = new StringBuffer();
		try {
			sb.append(" SELECT DISTINCT  I.* FROM " + Personel.TABLE_NAME + " P  WITH(nolock) ");
			sb.append(" INNER JOIN " + PersonelIzin.TABLE_NAME + "  I ON I." + PersonelIzin.COLUMN_NAME_PERSONEL + "=P." + Personel.COLUMN_NAME_ID);
			sb.append(" AND I." + PersonelIzin.COLUMN_NAME_IZIN_DURUMU + " NOT IN (8,9) AND  I." + PersonelIzin.COLUMN_NAME_BITIS_ZAMANI + " >= P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI);
			sb.append(" INNER JOIN " + IzinTipi.TABLE_NAME + "   T ON T." + IzinTipi.COLUMN_NAME_ID + "=I." + PersonelIzin.COLUMN_NAME_IZIN_TIPI + " AND T." + IzinTipi.COLUMN_NAME_BAKIYE_IZIN_TIPI + " IS NOT NULL");
			sb.append(" LEFT JOIN " + PersonelIzinDetay.TABLE_NAME + "  D ON D." + PersonelIzinDetay.COLUMN_NAME_HAKEDIS_IZIN + "=I." + IzinTipi.COLUMN_NAME_ID);
			sb.append(" WHERE P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + "=<CAST(GETDATE() AS date)  and D." + PersonelIzinDetay.COLUMN_NAME_ID + " IS NULL");

			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelIzin> izinList = pdksEntityController.getObjectBySQLList(sb, parametreMap, PersonelIzin.class);
			if (!izinList.isEmpty()) {
				User guncelleyenUser = ortakIslemler.getSistemAdminUser(session);
				Date guncellemeTarihi = Calendar.getInstance().getTime();
				for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
					PersonelIzin personelIzin = (PersonelIzin) iterator.next();
					personelIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL);
					personelIzin.setGuncellemeTarihi(guncellemeTarihi);
					if (guncelleyenUser != null)
						personelIzin.setGuncelleyenUser(guncelleyenUser);
					pdksEntityController.saveOrUpdate(session, entityManager, personelIzin);
				}
				session.flush();
			}
		} catch (Exception e) {
			izinList = new ArrayList<PersonelIzin>();
		}
		parametreMap = null;
	}

	/**
	 * @param session
	 * @throws Exception
	 */
	private void senelikSuaIzinKontrol(Session session) throws Exception {

		try {
			Parameter parameter = ortakIslemler.getParameter(session, "suaSenelikKullan");
			boolean suaSenelikKullan = parameter != null && parameter.getValue().equals("1");

			HashMap parametreMap = new HashMap();
			StringBuffer sb = new StringBuffer();
			try {
				sb.append(" WITH IZIN_BAKIYELER AS ( ");
				sb.append(" SELECT   YEAR(I." + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + ") AS YIL,I." + PersonelIzin.COLUMN_NAME_PERSONEL + ",I." + PersonelIzin.COLUMN_NAME_IZIN_TIPI + ",MAX(I." + IzinTipi.COLUMN_NAME_ID + ") AS IZIN_ID FROM " + PersonelIzin.TABLE_NAME + " I  WITH(nolock) ");
				sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P on P." + Personel.COLUMN_NAME_ID + "=I." + PersonelIzin.COLUMN_NAME_PERSONEL + " AND P." + Personel.COLUMN_NAME_DURUM + "=1 AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >=CAST(GETDATE() AS date)");
				if (suaSenelikKullan)
					sb.append(" AND (P.SUA_OLABILIR IS NULL OR P.SUA_OLABILIR<>1 ) ");
				sb.append(" INNER JOIN " + IzinTipi.TABLE_NAME + " IT ON IT." + IzinTipi.COLUMN_NAME_ID + "=I." + PersonelIzin.COLUMN_NAME_IZIN_TIPI + " and IT." + IzinTipi.COLUMN_NAME_DEPARTMAN + "=1 AND IT." + IzinTipi.COLUMN_NAME_BAKIYE_IZIN_TIPI + " IS NOT NULL ");
				sb.append(" INNER JOIN " + Tanim.TABLE_NAME + " T ON T." + Tanim.COLUMN_NAME_ID + "=IT." + IzinTipi.COLUMN_NAME_IZIN_TIPI + " AND T." + Tanim.COLUMN_NAME_KODU + " IN ('" + IzinTipi.YILLIK_UCRETLI_IZIN + "','" + IzinTipi.SUA_IZNI + "') ");
				sb.append(" where   I." + PersonelIzin.COLUMN_NAME_IZIN_SURESI + ">0 and I." + PersonelIzin.COLUMN_NAME_IZIN_DURUMU + " NOT IN (8,9) ");
				sb.append(" GROUP BY YEAR(I." + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + ")  ,I." + PersonelIzin.COLUMN_NAME_PERSONEL + ",I." + PersonelIzin.COLUMN_NAME_IZIN_TIPI);
				sb.append(" ), ");
				sb.append(" CIFT_IZIN AS ( ");
				sb.append(" SELECT " + PersonelIzin.COLUMN_NAME_PERSONEL + ",YIL,MIN(IZIN_ID) AS IZIN_1,Max(IZIN_ID) AS IZIN_2  FROM IZIN_BAKIYELER ");
				sb.append(" GROUP BY " + PersonelIzin.COLUMN_NAME_PERSONEL + ",YIL ");
				sb.append(" HAVING COUNT(*)=2 ");
				sb.append(" ), ");
				sb.append(" DATA AS ( ");
				sb.append(" SELECT P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + ",P." + Personel.COLUMN_NAME_AD + " + ' '+ P." + Personel.COLUMN_NAME_SOYAD + " AS PERSONEL,V.* FROM  CIFT_IZIN V ");
				sb.append(" INNER JOIN  " + Personel.TABLE_NAME + " P on P." + Personel.COLUMN_NAME_ID + "=V." + PersonelIzin.COLUMN_NAME_PERSONEL);
				sb.append(" )  ");
				sb.append("SELECT I.* FROM DATA V  WITH(nolock) ");
				sb.append(" INNER JOIN " + PersonelIzin.TABLE_NAME + " I ON I." + PersonelIzin.COLUMN_NAME_ID + "=V.IZIN_1 OR I." + PersonelIzin.COLUMN_NAME_ID + "=V.IZIN_2");
				sb.append(" WHERE YIL>=YEAR(GETDATE()) ");
				sb.append(" ORDER BY V.PERSONEL,V." + Personel.COLUMN_NAME_PDKS_SICIL_NO + ",V.YIL ");
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				izinList = pdksEntityController.getObjectBySQLList(sb, parametreMap, PersonelIzin.class);
			} catch (Exception e) {
				izinList = new ArrayList<PersonelIzin>();
			}
			sb = null;
			parametreMap = null;
			if (!izinList.isEmpty()) {
				toList = ortakIslemler.IKKullanicilariBul(toList, null, session);
				for (Iterator iterator = toList.iterator(); iterator.hasNext();) {
					User user = (User) iterator.next();
					if (!user.getDepartman().isAdminMi())
						iterator.remove();
				}
				if (!toList.isEmpty())
					ortakIslemler.mailGonder(renderer, "/email/suaSenelikIzinMail.xhtml");

			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}

	}

	@Transactional
	// @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void izinleriHesapla(User user, Session session) throws Exception {
		logger.info("izinBakiyeGuncellemeTimer in " + new Date());

		Calendar cal = Calendar.getInstance();
		yil = cal.get(Calendar.YEAR);

		try {

			if (session == null)
				session = PdksUtil.getSession(entityManager, Boolean.TRUE);
			hataKonum = "senelikBakiyeIzinEkle basladı ";
			senelikBakiyeIzinEkle(session, user, "(B.SUA_OLABILIR IS NULL OR B.SUA_OLABILIR=0) AND B.IZIN_HAKEDIS_TARIHI<'" + yil + "-01-01'", IzinTipi.YILLIK_UCRETLI_IZIN, PersonelIzin.getYillikIzinMaxBakiye(), yil);
			senelikBakiyeIzinEkle(session, user, "(B.SUA_OLABILIR IS NULL OR B.SUA_OLABILIR=0) AND B.IZIN_HAKEDIS_TARIHI>='" + yil + "-01-01'", IzinTipi.YILLIK_UCRETLI_IZIN, PersonelIzin.getYillikIzinMaxBakiye(), yil + 1);
			senelikBakiyeIzinEkle(session, user, "B.SUA_OLABILIR =1 AND B.IZIN_HAKEDIS_TARIHI<'" + yil + "-01-01'", IzinTipi.SUA_IZNI, null, yil);
			HashMap map = new HashMap();
			List<String> haricKodlar = new ArrayList<String>();
			haricKodlar.add(IzinTipi.SUA_IZNI);
			haricKodlar.add(IzinTipi.YURT_DISI_KONGRE);
			haricKodlar.add(IzinTipi.YURT_ICI_KONGRE);
			haricKodlar.add(IzinTipi.MOLA_IZNI);
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
			map.put(PdksEntityController.MAP_KEY_MAP, "getId");
			map.put(PdksEntityController.MAP_KEY_SELECT, "bakiyeIzinTipi.izinTipiTanim");
			map.put("bakiyeIzinTipi.izinTipiTanim.kodu not ", haricKodlar);
			map.put("bakiyeIzinTipi.durum=", Boolean.TRUE);
			map.put("bakiyeIzinTipi.bakiyeDevirTipi=", IzinTipi.BAKIYE_DEVIR_SENELIK);
			map.put("bakiyeIzinTipi.onaylayanTipi<>", IzinTipi.ONAYLAYAN_TIPI_YOK);
			map.put("bakiyeIzinTipi.personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
			map.put("kotaBakiye>=", 0D);
			hataKonum = "bakiyeIzinTipleri okunuyor ";
			TreeMap<Long, Tanim> senelikBakiyeIzinTipiMap = pdksEntityController.getObjectByInnerObjectMapInLogic(map, IzinTipi.class, Boolean.TRUE);
			for (Iterator iterator = senelikBakiyeIzinTipiMap.keySet().iterator(); iterator.hasNext();) {
				Long key = (Long) iterator.next();
				String izinTipi = senelikBakiyeIzinTipiMap.get(key).getKodu();
				if (izinTipi.equals(IzinTipi.SUA_IZNI))
					continue;
				senelikBakiyeIzinEkle(session, user, null, izinTipi, null, yil);

			}

			session.flush();

			izinleriBakiyeleriniHesapla(null, null, null, user, Boolean.TRUE, Boolean.FALSE);
			if (ozelKontrol) {
				Parameter parameter = ortakIslemler.getParameter(session, "suaSenelikKullan");
				boolean suaSenelikKullan = parameter == null || parameter.getValue() == null || !parameter.getValue().equals("1");
				cal = Calendar.getInstance();
				int haftaGun = cal.get(Calendar.DAY_OF_WEEK);
				if (haftaGun == Calendar.SUNDAY)
					ciftBakiyeIzinKontrol(session);
				else if (suaSenelikKullan && haftaGun != Calendar.SATURDAY)
					senelikSuaIzinKontrol(session);
			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		hataKonum = "izinleriBakiyeleriniHesapla basladi ";

		if (user != null)
			PdksUtil.addMessageInfo("İzin bakiyeleri güncellenmiştir");

		logger.info("izinBakiyeGuncellemeTimer out " + new Date());

	}

	/**
	 * @param siciller
	 * @param sirket
	 * @param user
	 * @param yeni
	 * @param gecmisHesapla
	 */
	public void izinleriBakiyeleriniHesapla(Session userSession, List<String> siciller, Sirket sirket, User user, boolean yeni, boolean gecmisHesapla) {
		if (userSession == null)
			userSession = PdksUtil.getSession(entityManager, yeni);
		Date bugun = PdksUtil.getDate(Calendar.getInstance().getTime());
		HashMap map = new HashMap();
		map.put(PdksEntityController.MAP_KEY_SESSION, userSession);
		map.put("sskCikisTarihi>=", bugun);
		map.put("iseBaslamaTarihi<=", bugun);
		map.put("dogumTarihi<>", null);
		map.put("durum=", Boolean.TRUE);
		if (siciller != null && !siciller.isEmpty())
			map.put("pdksSicilNo", siciller);
		else if (sirket != null)
			map.put("sirket=", sirket);
		else {
			map.put("sirket.durum=", Boolean.TRUE);
			if (!yeni && user != null && !user.isAdmin())
				map.put("sirket.departman=", user.getDepartman());
		}
		List<Personel> list = pdksEntityController.getObjectByInnerObjectListInLogic(map, Personel.class);
		String sicilNo = null;
		HashMap dataMap = new HashMap();
		int sayac = 0;
		boolean logGoster = PdksUtil.getUrl().indexOf("localhost") >= 0;
		while (!list.isEmpty() && sayac < 10) {
			sayac++;
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Personel pdksPersonel = (Personel) iterator.next();
				Sirket sirketPdks = pdksPersonel.getSirket();
				if (sirketPdks == null || !sirketPdks.getFazlaMesai() || !sirketPdks.getDepartman().getIzinGirilebilir()) {
					iterator.remove();
					continue;
				}
				try {
					if (!(pdksPersonel.getSicilNo().trim().length() == 0 || pdksPersonel.getIzinHakEdisTarihi() == null || pdksPersonel.getDogumTarihi() == null)) {
						try {
							sicilNo = String.valueOf(Long.parseLong(pdksPersonel.getSicilNo().trim()));
						} catch (Exception e) {
							sicilNo = null;
						}
						if (sicilNo != null) {
							if (logGoster)
								logger.debug(sicilNo);
							ortakIslemler.getKidemHesabi(null, pdksPersonel, null, null, user, userSession, dataMap, gecmisHesapla, Boolean.TRUE);
						}

					}
					iterator.remove();

				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					logger.error(" izinBakiyeGuncelleme hata : " + sicilNo + " " + PdksUtil.setTurkishStr(pdksPersonel.getAdSoyad()) + " " + e.getMessage());

					break;
				}

			}
			if (!list.isEmpty()) {
				userSession.flush();
				if (yeni)
					userSession = PdksUtil.getSession(entityManager, yeni);

				if (list.size() > 1)
					Collections.shuffle(list);
			}
		}
		userSession.flush();
		if (yeni)
			logger.info("izinBakiyeGuncelleme tamam");

	}

	/**
	 * @param session
	 * @param user
	 * @param sql
	 * @param izinTipiKodu
	 * @param bakiye
	 * @param izinYil
	 * @throws Exception
	 */
	private void senelikBakiyeIzinEkle(Session session, User user, String sql, String izinTipiKodu, Integer bakiye, int izinYil) throws Exception {
		StringBuilder queryStr = new StringBuilder();
		String bakiyeTarih = izinYil + "0101";
		queryStr.append(" WITH IZIN_OZET AS ( ");
		queryStr.append(" select  TA.ACIKLAMATR as ACIKLAMA, convert(datetime,'" + bakiyeTarih + "', 112) AS  DONEM , ");
		queryStr.append((bakiye == null ? " T.KOTA_BAKIYE " : String.valueOf(bakiye)) + " AS IZIN_SURESI,  B." + Personel.COLUMN_NAME_ID + " PERSONEL_ID,T." + IzinTipi.COLUMN_NAME_ID + " AS IZIN_TIPI_ID FROM " + IzinTipi.TABLE_NAME + " T WITH(nolock)   ");
		queryStr.append(" INNER JOIN " + Tanim.TABLE_NAME + " TA ON TA." + Tanim.COLUMN_NAME_ID + "=T." + IzinTipi.COLUMN_NAME_IZIN_TIPI + " AND TA.kodu='" + izinTipiKodu + "'");
		queryStr.append(" INNER JOIN " + Sirket.TABLE_NAME + " S ON S." + Sirket.COLUMN_NAME_DEPARTMAN + "=T." + IzinTipi.COLUMN_NAME_DEPARTMAN + " AND S." + Sirket.COLUMN_NAME_FAZLA_MESAI + "=1 AND S." + Sirket.COLUMN_NAME_DURUM + "=1");
		queryStr.append(" INNER JOIN " + Personel.TABLE_NAME + " B ON S." + Sirket.COLUMN_NAME_ID + "=B." + Personel.COLUMN_NAME_SIRKET + " AND B." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=CAST(GETDATE() AS date) AND B." + Personel.COLUMN_NAME_DURUM + "=1 " + (sql != null ? " AND " + sql : ""));
		queryStr.append("  WHERE T." + IzinTipi.COLUMN_NAME_DURUM + "=1 and T." + IzinTipi.COLUMN_NAME_BAKIYE_IZIN_TIPI + " IS NOT NULL ");
		queryStr.append("  )");
		queryStr.append(" INSERT INTO " + PersonelIzin.TABLE_NAME + " (" + PersonelIzin.COLUMN_NAME_DURUM + ", " + PersonelIzin.COLUMN_NAME_OLUSTURMA_TARIHI + ", " + PersonelIzin.COLUMN_NAME_ACIKLAMA + ", " + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + ", " + PersonelIzin.COLUMN_NAME_BITIS_ZAMANI
				+ ",");
		queryStr.append(PersonelIzin.COLUMN_NAME_IZIN_SURESI + ", " + PersonelIzin.COLUMN_NAME_IZIN_DURUMU + ",  " + PersonelIzin.COLUMN_NAME_VERSION + ",  " + PersonelIzin.COLUMN_NAME_OLUSTURAN + ", " + PersonelIzin.COLUMN_NAME_PERSONEL + ", " + PersonelIzin.COLUMN_NAME_IZIN_TIPI + ")");
		queryStr.append(" SELECT  1 as DURUM, GETDATE() olusturmaTarihi, '" + izinYil + " YILI ' + O.ACIKLAMA AS ACIKLAMA, O.DONEM AS BASLANGIC_ZAMANI,");
		queryStr.append(" O.DONEM AS BITIS_ZAMANI,O.IZIN_SURESI, 4 AS IZIN_DURUMU, 0 AS version," + user.getId() + " olusturanUser_id ,");
		queryStr.append(" O.PERSONEL_ID, O.IZIN_TIPI_ID  FROM IZIN_OZET O ");
		queryStr.append(" LEFT JOIN " + PersonelIzin.TABLE_NAME + " I ON I." + PersonelIzin.COLUMN_NAME_IZIN_TIPI + "=O.IZIN_TIPI_ID ");
		queryStr.append("   AND I." + PersonelIzin.COLUMN_NAME_PERSONEL + "=O.PERSONEL_ID AND I." + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + "=O.DONEM");
		queryStr.append(" WHERE I." + PersonelIzin.COLUMN_NAME_ID + " IS NULL");
		String str = queryStr.toString();
		try {
			SQLQuery query = session.createSQLQuery(str);
			query.executeUpdate();
		} catch (Exception e) {
			logger.error(e + "\n" + str);
		}
		queryStr = null;
	}

	public int getYil() {
		return yil;
	}

	public void setYil(int yil) {
		this.yil = yil;
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
		IzinBakiyeGuncelleme.calisiyor = calisiyor;
	}

	public List<User> getToList() {
		return toList;
	}

	public void setToList(List<User> toList) {
		this.toList = toList;
	}

	public List<PersonelIzin> getIzinList() {
		return izinList;
	}

	public void setIzinList(List<PersonelIzin> izinList) {
		this.izinList = izinList;
	}

	public static boolean isOzelKontrol() {
		return ozelKontrol;
	}

	public static void setOzelKontrol(boolean ozelKontrol) {
		IzinBakiyeGuncelleme.ozelKontrol = ozelKontrol;
	}

}