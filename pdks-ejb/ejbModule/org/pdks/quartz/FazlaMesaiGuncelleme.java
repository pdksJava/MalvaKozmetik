package org.pdks.quartz;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.jboss.seam.Component;
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
import org.jboss.seam.faces.FacesMessages;
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.AylikPuantaj;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Departman;
import org.pdks.entity.DepartmanDenklestirmeDonemi;
import org.pdks.entity.Dosya;
import org.pdks.entity.Liste;
import org.pdks.entity.Parameter;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.security.entity.User;
import org.pdks.session.FazlaMesaiHesaplaHome;
import org.pdks.session.FazlaMesaiOrtakIslemler;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

@Name("fazlaMesaiGuncelleme")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class FazlaMesaiGuncelleme implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6776373506431071650L;

	static Logger logger = Logger.getLogger(FazlaMesaiGuncelleme.class);

	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	@In(required = false, create = true)
	EntityManager entityManager;

	@In(required = false, create = true)
	Zamanlayici zamanlayici;

	@In(required = false, create = true)
	PdksEntityController pdksEntityController;

	// //@In(required = false, create = true)
	FazlaMesaiHesaplaHome fazlaMesaiHesaplaHome;

	@In(required = false, create = true)
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;

	private static final String PARAMETER_KEY = "fazlaMesaiZamanliGuncelleme";

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
				if (PdksUtil.isSistemDestekVar() && PdksUtil.getCanliSunucuDurum()) {
					session = PdksUtil.getSession(entityManager, Boolean.TRUE);
					if (session != null)
						fazlaMesaiGuncellemeBasla(false, session);
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
	 * @throws Exception
	 */
	public void fazlaMesaiGuncellemeBasla(boolean manuel, Session session) throws Exception {
		if (PdksUtil.isSistemDestekVar()) {
			Parameter parameter = ortakIslemler.getParameter(session, PARAMETER_KEY);
			String value = (parameter != null) ? parameter.getValue() : null;
			if (value != null) {
				Date time = zamanlayici.getDbTime(session);
				boolean zamanDurum = PdksUtil.zamanKontrol(PARAMETER_KEY, value, time);
				if (zamanDurum)
					fazlaMesaiGuncellemeCalistir(false, session);
			}
		}
	}

	/**
	 * @param manuel
	 * @param session
	 */
	@Transactional
	public void fazlaMesaiGuncellemeCalistir(boolean manuel, Session session) {
		loginUser = ortakIslemler != null ? ortakIslemler.getSistemAdminUser(session) : null;
		if (loginUser != null) {
			Integer otomatikOnayIKGun = null;
			String str = ortakIslemler.getParameterKey("otomatikOnayIKGun");
			if (PdksUtil.hasStringValue(str))
				try {
					otomatikOnayIKGun = Integer.parseInt(str);
					if (otomatikOnayIKGun < 1 || otomatikOnayIKGun > 28)
						otomatikOnayIKGun = null;
				} catch (Exception e) {
					otomatikOnayIKGun = null;
				}
			if (otomatikOnayIKGun == null)
				otomatikOnayIKGun = 6;
			loginUser.setAdmin(Boolean.TRUE);
			boolean denklestirme = true;
			Calendar cal = Calendar.getInstance();
			LinkedHashMap<Integer, Liste> dMap = new LinkedHashMap<Integer, Liste>();
			int yil = cal.get(Calendar.YEAR);
			int ay = cal.get(Calendar.MONTH) + 1;
			dMap.put(yil * 100 + ay, new Liste(yil, ay));
			cal.add(Calendar.DATE, -otomatikOnayIKGun);
			yil = cal.get(Calendar.YEAR);
			ay = cal.get(Calendar.MONTH) + 1;
			dMap.put(yil * 100 + ay, new Liste(yil, ay));
			HashMap fields = new HashMap();
			if (fazlaMesaiHesaplaHome == null)
				fazlaMesaiHesaplaHome = new FazlaMesaiHesaplaHome();
			fazlaMesaiHesaplaHome.setInject(entityManager, pdksEntityController, ortakIslemler, fazlaMesaiOrtakIslemler);
			fazlaMesaiHesaplaHome.setSession(session);
			fazlaMesaiHesaplaHome.setHataliPuantajGoster(false);
			fazlaMesaiHesaplaHome.setSicilNo("");
			fazlaMesaiHesaplaHome.setStajerSirket(false);
			fazlaMesaiHesaplaHome.setSession(session);
			fazlaMesaiHesaplaHome.setHataliPuantajGoster(false);
			fazlaMesaiHesaplaHome.setSicilNo("");
			fazlaMesaiHesaplaHome.setSeciliEkSaha4Id(null);
			fazlaMesaiHesaplaHome.setDenklestirmeAyDurum(true);
			HashMap<String, Object> veriMap = new HashMap<String, Object>();
			StringBuffer sb = new StringBuffer();
			List<Dosya> dosyalar = new ArrayList<Dosya>();
			for (Integer key : dMap.keySet()) {

				Liste liste = dMap.get(key);
				yil = (Integer) liste.getId();
				ay = (Integer) liste.getValue();
				fields.clear();
				fields.put("yil", yil);
				fields.put("ay", ay);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				DenklestirmeAy denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
				if (denklestirmeAy != null && denklestirmeAy.getDurum()) {
					denklestirmeDonemi = new DepartmanDenklestirmeDonemi();
					aylikPuantaj = fazlaMesaiOrtakIslemler.getAylikPuantaj(ay, yil, denklestirmeDonemi, session);
					aylikPuantaj.setUser(loginUser);
					aylikPuantaj.setDenklestirmeAy(denklestirmeAy);
					denklestirmeDonemi.setDenklestirmeAy(denklestirmeAy);
					denklestirmeDonemi.setUser(loginUser);
					List<SelectItem> depList = fazlaMesaiOrtakIslemler.getFazlaMesaiDepartmanList(aylikPuantaj, denklestirme, session);
					boolean mesajGonder = false;
					if (!depList.isEmpty()) {
						logger.info(denklestirmeAy.getAyAdi() + " " + denklestirmeAy.getYil() + " in " + new Date());
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
									veriMap.clear();
									veriMap.put("sirket", sirket);
									veriMap.put("manuel", manuel);
									veriMap.put("denklestirme", denklestirme);
									veriMap.put("denklestirmeAy", denklestirmeAy);
									veriMap.put("dosyalar", dosyalar);
									Departman departman = sirket.getDepartman();
									fazlaMesaiHesaplaHome.setDepartman(departman);
									fazlaMesaiHesaplaHome.setSirket(sirket);
									fazlaMesaiHesaplaHome.setSirketId(sirket.getId());
									mesajGonder = true;
									if (sirket.isTesisDurumu()) {
										List<SelectItem> tesisList = fazlaMesaiOrtakIslemler.getFazlaMesaiTesisList(sirket, aylikPuantaj, denklestirme, session);
										for (SelectItem selectItem3 : tesisList) {
											Long tesisId = (Long) selectItem3.getValue();
											veriMap.put("tesisId", tesisId);
											bolumFazlaMesai(veriMap, session);
										}
									} else
										bolumFazlaMesai(veriMap, session);

								}
							}
						}
						try {
							if (manuel == false && mesajGonder) {
								if (sb.length() > 0)
									sb.append(", ");
								sb.append(denklestirmeAy.getAyAdi() + " " + denklestirmeAy.getYil());
							}
						} catch (Exception e) {
							logger.error(e);
							e.printStackTrace();
						}
						logger.info(denklestirmeAy.getAyAdi() + " " + denklestirmeAy.getYil() + " out " + new Date());
					}

				}
				if (manuel) {
					FacesMessages facesMessages = (FacesMessages) Component.getInstance("facesMessages");
					facesMessages.clear();
				}

			}
			if (sb.length() > 0)
				try {
					Dosya dosya = null;
					if (!dosyalar.isEmpty()) {
						String zipDosyaAdi = "FazlaMesaiDurum_" + PdksUtil.convertToDateString(new Date(), "yyyyMMdd") + ".zip";
						File file = ortakIslemler.dosyaZipFileOlustur(zipDosyaAdi, dosyalar);
						if (file != null && file.exists()) {

							dosya = ortakIslemler.dosyaFileOlustur(zipDosyaAdi, file, Boolean.TRUE);
							file.deleteOnExit();

						}
					}
					String aylar = sb.toString();
					zamanlayici.mailGonderDosya(session, null, "Fazla Mesai Güncellemesi", aylar + " " + (aylar.indexOf(",") > 0 ? "dönemleri" : "dönemi") + " fazla mesailer güncellenmiştir.", null, dosya, Boolean.TRUE);
				} catch (Exception e) {
				}
			sb = null;
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
	private void bolumFazlaMesai(HashMap<String, Object> veriMap, Session session) {
		boolean manuelDurum = veriMap.containsKey("manuel") ? (Boolean) veriMap.get("manuel") : false;
		boolean denklestirme = veriMap.containsKey("denklestirme") ? (Boolean) veriMap.get("denklestirme") : false;
		Long tesisId = veriMap.containsKey("tesisId") ? (Long) veriMap.get("tesisId") : null;
		Sirket sirket = veriMap.containsKey("sirket") ? (Sirket) veriMap.get("sirket") : null;
		DenklestirmeAy denklestirmeAy = veriMap.containsKey("denklestirmeAy") ? (DenklestirmeAy) veriMap.get("denklestirmeAy") : null;
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
		boolean manuel = false;

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
				loginUser.setAdmin(Boolean.TRUE);
				List<AylikPuantaj> puantajList = fazlaMesaiHesaplaHome.fillPersonelDenklestirmeDevam(aylikPuantaj, denklestirmeDonemi);
				manuel = puantajList.isEmpty();
				logger.info(str + " out " + new Date());
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
				manuel = true;
			}
			if (manuel)
				break;

		}
		if (!manuel && manuelDurum == false) {
			AramaSecenekleri as = new AramaSecenekleri();
			as.setSicilNo("");
			as.setSirket(sirket);
			as.setTesisId(tesisId);
			List<AylikPuantaj> personelDenklestirmeList = fazlaMesaiOrtakIslemler.getBordoDenklestirmeList(denklestirmeAy, as, false, false, session);
			if (!personelDenklestirmeList.isEmpty()) {
				HashMap<String, Object> dataMap = new HashMap<String, Object>();
				ByteArrayOutputStream baos = null;

				veriMap.put("denklestirmeAy", denklestirmeAy);
				veriMap.put("personelDenklestirmeList", personelDenklestirmeList);

				try {
					baos = fazlaMesaiOrtakIslemler.denklestirmeExcelAktarDevam(dataMap, session);
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}
				if (baos != null) {
					try {
						List<Dosya> dosyalar = veriMap.containsKey("dosyalar") ? (List<Dosya>) veriMap.get("dosyalar") : null;
						new ArrayList<Dosya>();
						String dosyaAdi = dm.getAyAdi() + " " + dm.getYil() + "/" + sirket.getAd() + (tesis != null ? "_" + tesis.getAciklama() : " ") + ".xlsx";
						Dosya dosyaExcel = new Dosya();
						dosyaExcel.setDosyaAdi(dosyaAdi);
						Workbook wb = new XSSFWorkbook();
						wb.write(baos);
						dosyaExcel.setDosyaIcerik(baos.toByteArray());
						dosyalar.add(dosyaExcel);
					} catch (Exception e) {
					}

				}
			}
		}

	}

	public static boolean isCalisiyor() {
		return calisiyor;
	}

	public static void setCalisiyor(boolean calisiyor) {
		FazlaMesaiGuncelleme.calisiyor = calisiyor;
	}

	public User getLoginUser() {
		return loginUser;
	}

	public void setLoginUser(User loginUser) {
		this.loginUser = loginUser;
	}

}