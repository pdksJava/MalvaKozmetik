package org.pdks.quartz;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.Parameter;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;
import org.pdks.session.OrtakIslemler;
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

@Name("yemekMukkerrerBilgilendirme")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class YemekMukkerrerBilgilendirme implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3321720172278926677L;
	static Logger logger = Logger.getLogger(YemekMukkerrerBilgilendirme.class);

	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	Renderer renderer;
	@In(required = false, create = true)
	HashMap<String, String> parameterMap;
	@In(required = false, create = true)
	public Zamanlayici zamanlayici;
	@In
	EntityManager entityManager;

	private static boolean calisiyor = Boolean.FALSE;
	private static final String PARAMETER_KEY = "yemekMukerrer";
	private String hataKonum;

	@Asynchronous
	@SuppressWarnings("unchecked")
	@Transactional
	// @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public QuartzTriggerHandle yemekMukkerrerBilgilendirmeTimer(@Expiration Date when, @IntervalCron String interval) {
		hataKonum = "yemekMukkerrerBilgilendirmeTimer başladı ";
		hataKonum = "yemekMukkerrerBilgilendirmeTimer kontrol ediliyor ";
		if (pdksEntityController != null && !isCalisiyor()) {
			boolean hataGonder = Boolean.FALSE;
			Session session = null;
			try {
				setCalisiyor(Boolean.TRUE);
				// logger.error("Ise gelme durumu " + new Date());
				session = PdksUtil.getSession(entityManager, Boolean.TRUE);
				hataKonum = "Paramatre okunuyor ";
				Parameter parameter = ortakIslemler.getParameter(session, PARAMETER_KEY);
				String value = (parameter != null) ? parameter.getValue() : null;

				hataKonum = "Paramatre okundu ";

				if (value != null) {
					hataGonder = Boolean.TRUE;
					hataKonum = "Zaman kontrolu yapılıyor ";
					Date time = zamanlayici.getDbTime(session);
					boolean zamanDurum = PdksUtil.zamanKontrol(PARAMETER_KEY, value, time);
					// if (!zamanDurum)
					// zamanDurum = pdksUtil.getUrl().indexOf("localhost") >= 0;
					if (zamanDurum) {
						Calendar cal = Calendar.getInstance();
						Date tarih2 = (Date) cal.getTime().clone();
						Date tarih1 = PdksUtil.getDate(cal.getTime());
						yemekMukkerrerBilgilendirmeBul(session, tarih1, tarih2);
						zamanlayici.mailGonder(session, "Yemek mükerrer kontrolü", "Yemek mükerrer kontrolü tamamlandı.", null, Boolean.TRUE);

					}
				}

			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				logger.error("yemekMukkerrerBilgilendirmeTimer : " + e.getMessage());
				if (hataGonder)
					try {
						zamanlayici.mailGonder(session, "Yemek mükerrer kontrolü", "Yemek mükerrer kontrolü tamamlanmadı." + e.getMessage() + " ( " + hataKonum + " )", null, Boolean.TRUE);

					} catch (Exception e2) {
						logger.error("yemekMukkerrerBilgilendirmeTimer 2 : " + e.getMessage());
					}
			} finally {
				if (session != null)
					session.close();
				setCalisiyor(Boolean.FALSE);
			}

		}
		return null;
	}

	public String yemekMukkerrerBilgilendirmeBul(Session session, Date basTarih, Date bitTarih) throws Exception {
		List<HareketKGS> kgsList = null;
		if (basTarih != null && bitTarih != null && basTarih.before(bitTarih)) {
			List<User> kullanicilar = ortakIslemler.getRoleKullanicilari(Role.TIPI_YEMEKHANE, null, null, session);
			if (!kullanicilar.isEmpty()) {
				kgsList = ortakIslemler.getYemekHareketleri(session, basTarih, bitTarih, true);
				for (Iterator iterator = kgsList.iterator(); iterator.hasNext();) {
					HareketKGS kgsHareket = (HareketKGS) iterator.next();
					if (kgsHareket.getOncekiYemekZamani() == null || !kgsHareket.isCheckBoxDurum())
						iterator.remove();

				}
				if (!kgsList.isEmpty()) {
					StringBuffer sb = new StringBuffer();
					sb.append("<TABLE style=\"border: solid 1px\" cellpadding=\"5\" cellspacing=\"0\">");
					sb.append("<TR>");
					sb.append("<TD align='center'><B>Yemek Zamanı</B></TD>");
					sb.append("<TD align='center'><B>Adı Soyadı</B></TD>");
					sb.append("<TD align='center'><B>" + ortakIslemler.personelNoAciklama() + "</B></TD>");
					sb.append("<TD align='center'><B>" + ortakIslemler.sirketAciklama() + "</B></TD>");
					sb.append("<TD align='center'><B>Öğün Tipi</B></TD>");
					sb.append("<TD align='center'><B>Yemek Yeri</B></TD>");
					sb.append("<TD align='center'><B>Önceki Yemek Zamanı</B></TD>");
					sb.append("</TR>");
					String str = "";
					for (Iterator iterator = kgsList.iterator(); iterator.hasNext();) {
						HareketKGS yemek = (HareketKGS) iterator.next();
						sb.append("<TR>");
						sb.append("<TD align='center'> " + PdksUtil.convertToDateString(yemek.getZaman(), PdksUtil.getDateFormat() + " H:mm") + "</TD>");
						sb.append("<TD>" + yemek.getAdSoyad() + "</TD>");
						sb.append("<TD align='center'>" + yemek.getSicilNo() + "</TD>");
						try {
							str = yemek.getPersonel().getPdksPersonel().getSirket().getAd();
						} catch (Exception e) {
							logger.error("PDKS hata in : \n");
							e.printStackTrace();
							logger.error("PDKS hata out : " + e.getMessage());
							str = "";
						}
						sb.append("<TD>" + str + "</TD>");
						try {
							str = yemek.getYemekOgun().getYemekAciklama();
						} catch (Exception e) {
							logger.error("PDKS hata in : \n");
							e.printStackTrace();
							logger.error("PDKS hata out : " + e.getMessage());
							str = "";
						}
						sb.append("<TD>" + str + "</TD>");
						try {
							str = yemek.getKapiView().getAciklama();
						} catch (Exception e) {
							logger.error("PDKS hata in : \n");
							e.printStackTrace();
							logger.error("PDKS hata out : " + e.getMessage());
							str = "";
						}
						sb.append("<TD>" + str + "</TD>");
						sb.append("<TD align='center'>" + PdksUtil.convertToDateString(yemek.getOncekiYemekZamani(), PdksUtil.getDateFormat() + " H:mm") + "</TD>");
						sb.append("</TR>");
					}

					sb.append("</TABLE>");
					Collections.reverse(kgsList);
					zamanlayici.mailGonder(session, "Yemek mükerrer listesi", sb.toString(), kullanicilar, Boolean.TRUE);
					sb = null;
				}

			}

		}

		return "";
	}

	public static boolean isCalisiyor() {
		return calisiyor;
	}

	public static void setCalisiyor(boolean calisiyor) {
		YemekMukkerrerBilgilendirme.calisiyor = calisiyor;
	}

	public String getHataKonum() {
		return hataKonum;
	}

	public void setHataKonum(String hataKonum) {
		this.hataKonum = hataKonum;
	}

}