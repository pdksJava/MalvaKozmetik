package org.pdks.quartz;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.faces.model.SelectItem;
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
import org.pdks.entity.AylikPuantaj;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Departman;
import org.pdks.entity.DepartmanDenklestirmeDonemi;
import org.pdks.entity.Parameter;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.security.action.StartupAction;
import org.pdks.security.entity.User;
import org.pdks.session.FazlaMesaiHesaplaHome;
import org.pdks.session.FazlaMesaiOrtakIslemler;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

@Name("fazlaMesaiGuncelleme")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class FazlaMesaiGuncelleme {

	static Logger logger = Logger.getLogger(FazlaMesaiGuncelleme.class);

	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	@In(required = false, create = true)
	HashMap<String, String> parameterMap;

	@In(required = false, create = true)
	EntityManager entityManager;

	@In(required = false, create = true)
	Zamanlayici zamanlayici;

	@In(required = false, create = true)
	StartupAction startupAction;

	@In(required = false, create = true)
	PdksEntityController pdksEntityController;

	@In(required = false, create = true)
	FazlaMesaiHesaplaHome fazlaMesaiHesaplaHome;

	@In(required = false, create = true)
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;

	private static final String PARAMETER_KEY = "fazlaMesaiGuncelleme";
	private DepartmanDenklestirmeDonemi denklestirmeDonemi = null;
	private AylikPuantaj aylikPuantaj = null;
	private User loginUser;
	private static boolean calisiyor = Boolean.FALSE;

	@Asynchronous
	@SuppressWarnings("unchecked")
	@Transactional
	// @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public QuartzTriggerHandle fazlaMesaiGuncellemeTimer(@Expiration Date when, @IntervalCron String interval) {
		if (!isCalisiyor()) {
			setCalisiyor(Boolean.TRUE);
			logger.debug("fazlaMesaiGuncelleme in " + new Date());
			Session session = null;
			try {
				if (PdksUtil.isSistemDestekVar()) {
					session = PdksUtil.getSession(entityManager, Boolean.TRUE);
					Parameter parameter = ortakIslemler.getParameter(session, PARAMETER_KEY);
					String value = (parameter != null) ? parameter.getValue() : null;
					if (value != null) {
						Date time = zamanlayici.getDbTime(session);
						boolean zamanDurum = PdksUtil.zamanKontrol(PARAMETER_KEY, value, time);
						if (zamanDurum)
							fazlaMesaiGuncellemeCalistir(false, session);
					}
				}

			} catch (Exception e) {
				logger.error("PDKS hata in : \n" + e.getMessage() + " " + new Date());
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
			} finally {
				if (session != null)
					session.close();
				setCalisiyor(Boolean.FALSE);

			}

		}

		return null;
	}

	/**
	 * @param manuel
	 * @param session
	 */
	@Transactional
	public void fazlaMesaiGuncellemeCalistir(boolean manuel, Session session) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -6);
		int yil = cal.get(Calendar.YEAR);
		int ay = cal.get(Calendar.MONTH) + 1;
		HashMap fields = new HashMap();
		fields.put("yil", yil);
		fields.put("ay", ay);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		DenklestirmeAy denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
		if (denklestirmeAy != null && fazlaMesaiHesaplaHome != null) {
			if (denklestirmeAy.getDurum() == false) {
				cal.add(Calendar.MONTH, 1);
				yil = cal.get(Calendar.YEAR);
				ay = cal.get(Calendar.MONTH) + 1;
				fields.clear();
				fields.put("yil", yil);
				fields.put("ay", ay);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
			}
			loginUser = ortakIslemler.getSistemAdminUser(session);
			if (loginUser != null && denklestirmeAy != null && denklestirmeAy.getDurum()) {
				loginUser.setAdmin(Boolean.TRUE);
				boolean denklestirme = true;
				denklestirmeDonemi = new DepartmanDenklestirmeDonemi();
				aylikPuantaj = fazlaMesaiOrtakIslemler.getAylikPuantaj(ay, yil, denklestirmeDonemi, session);
				aylikPuantaj.setDenklestirmeAy(denklestirmeAy);
				denklestirmeDonemi.setDenklestirmeAy(denklestirmeAy);
				List<SelectItem> depList = fazlaMesaiOrtakIslemler.getFazlaMesaiDepartmanList(aylikPuantaj, denklestirme, session);
				fazlaMesaiHesaplaHome.setSession(session);
				fazlaMesaiHesaplaHome.setDenklestirmeAy(denklestirmeAy);
				fazlaMesaiHesaplaHome.setYil(denklestirmeAy.getYil());
				fazlaMesaiHesaplaHome.setAy(denklestirmeAy.getAy());
				for (SelectItem selectItem : depList) {
					Long departmanId = (Long) selectItem.getValue();
					fazlaMesaiHesaplaHome.setDepartmanId(departmanId);
					List<SelectItem> sirketList = fazlaMesaiOrtakIslemler.getFazlaMesaiSirketList(departmanId, aylikPuantaj, denklestirme, session);
					for (SelectItem selectItem2 : sirketList) {
						Long sirketId = (Long) selectItem2.getValue();
						fields.clear();
						fields.put("id", sirketId);
						if (session != null)
							fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						Sirket sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);
						if (sirket != null) {
							Departman departman = sirket.getDepartman();
							fazlaMesaiHesaplaHome.setDepartman(departman);
							fazlaMesaiHesaplaHome.setSirket(sirket);
							fazlaMesaiHesaplaHome.setSirketId(sirket.getId());
							if (sirket.isTesisDurumu()) {
								List<SelectItem> tesisList = fazlaMesaiOrtakIslemler.getFazlaMesaiTesisList(sirket, aylikPuantaj, denklestirme, session);
								for (SelectItem selectItem3 : tesisList) {
									Long tesisId = (Long) selectItem3.getValue();
									bolumFazlaMesai(manuel, sirket, tesisId, denklestirme, session);
								}
							} else
								bolumFazlaMesai(manuel, sirket, null, denklestirme, session);

						}
					}
				}
				try {
					if (manuel == false)
						zamanlayici.mailGonder(session, null, "Fazla Mesai Güncellemesi", denklestirmeAy.getAyAdi() + " " + denklestirmeAy.getYil() + " fazla mesailer güncellenmiştir.", null, Boolean.TRUE);
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}

			}
		}
	}

	/**
	 * @param manuel
	 * @param sirket
	 * @param tesisId
	 * @param denklestirme
	 * @param session
	 */
	@Transactional
	private void bolumFazlaMesai(boolean manuel, Sirket sirket, Long tesisId, boolean denklestirme, Session session) {
		List<SelectItem> bolumList = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, tesisId != null ? String.valueOf(tesisId) : "", aylikPuantaj, denklestirme, session);
		fazlaMesaiHesaplaHome.setTesisId(tesisId);
		HashMap fields = new HashMap();
		Tanim tesis = null;
		DenklestirmeAy dm = aylikPuantaj.getDenklestirmeAy();
		if (tesisId != null && sirket.isTesisDurumu()) {
			fields.put("id", tesisId);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			tesis = (Tanim) pdksEntityController.getObjectByInnerObject(fields, Tanim.class);
		}
		String baslik = dm.getAyAdi() + " " + dm.getYil() + " " + sirket.getAd() + (tesis != null ? " " + tesis.getAciklama() : "");
		manuel = false;

		for (SelectItem selectItem : bolumList) {
			Long seciliEkSaha3Id = (Long) selectItem.getValue();
			fazlaMesaiHesaplaHome.setSeciliEkSaha3Id(seciliEkSaha3Id);
			fazlaMesaiHesaplaHome.setStajerSirket(false);
			try {
				fields.clear();
				fields.put("id", seciliEkSaha3Id);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				Tanim bolum = (Tanim) pdksEntityController.getObjectByInnerObject(fields, Tanim.class);
				String str = baslik + (bolum != null ? " " + bolum.getAciklama() : "");
				logger.info(str + " in " + new Date());
				fazlaMesaiHesaplaHome.fillPersonelDenklestirmeDevam(aylikPuantaj, denklestirmeDonemi, loginUser);
				logger.info(str + " out " + new Date());
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
				manuel = true;
			}
			if (manuel)
				break;

		}

	}

	public static boolean isCalisiyor() {
		return calisiyor;
	}

	public static void setCalisiyor(boolean calisiyor) {
		FazlaMesaiGuncelleme.calisiyor = calisiyor;
	}

}