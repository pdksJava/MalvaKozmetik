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
import org.pdks.entity.Personel;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.security.entity.User;
import org.pdks.session.FazlaMesaiHesaplaHome;
import org.pdks.session.FazlaMesaiOrtakIslemler;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;
import org.pdks.session.VardiyaGunHome;

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
	EntityManager entityManager;

	@In(required = false, create = true)
	Zamanlayici zamanlayici;

	@In(required = false, create = true)
	PdksEntityController pdksEntityController;

	@In(required = false, create = true)
	User authenticatedUser;

	@In(required = false, create = true)
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;

	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	// @In(required = false, create = true)
	FazlaMesaiHesaplaHome fazlaMesaiHesaplaHome;

	// @In(required = false, create = true)
	VardiyaGunHome vardiyaGunHome;

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
			logger.debug("fazlaMesaiGuncelleme in " + PdksUtil.getCurrentTimeStampStr());
			Session session = null;
			try {
				boolean canliSistem = PdksUtil.getCanliSunucuDurum();
				// canliSistem = true;
				if (PdksUtil.isSistemDestekVar() && canliSistem && zamanlayici.isPazar() == false) {
					session = PdksUtil.getSession(entityManager, Boolean.TRUE);
					if (session != null)
						fazlaMesaiGuncellemeBasla(false, session);
				}
			} catch (Exception e) {
				logger.error("PDKS hata in : \n" + e.getMessage() + " " + PdksUtil.getCurrentTimeStampStr());
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
		Parameter parameter = ortakIslemler.getParameter(session, PARAMETER_KEY);
		String value = (parameter != null) ? parameter.getValue() : null;
		if (value != null) {
			Date time = zamanlayici.getDbTime(session);
			boolean zamanDurum = PdksUtil.zamanKontrol(PARAMETER_KEY, value, time);
			// zamanDurum = true;
			if (zamanDurum)
				fazlaMesaiGuncellemeCalistir(false, session);
		}
	}

	/**
	 * @param manuel
	 * @param session
	 */
	@Transactional
	public void fazlaMesaiGuncellemeCalistir(boolean manuel, Session session) {
		if (authenticatedUser == null)
			loginUser = ortakIslemler != null ? ortakIslemler.getSistemAdminUser(session) : null;
		else
			loginUser = authenticatedUser;
		loginUser.setLogin(authenticatedUser != null);
		if (loginUser != null) {
			Date basTarih = new Date();
			Integer otomatikOnayIKGun = null;
			String str = ortakIslemler.getParameterKey("otomatikOnayIKGun");
			boolean vardiyaPlaniOtomatikOlustur = ortakIslemler.getParameterKeyHasStringValue("vardiyaPlaniOtomatikOlustur");
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
			if (loginUser.getLogin().booleanValue() == false)
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
			cal = Calendar.getInstance();
			cal.add(Calendar.DATE, otomatikOnayIKGun);
			yil = cal.get(Calendar.YEAR);
			ay = cal.get(Calendar.MONTH) + 1;
			dMap.put(yil * 100 + ay, new Liste(yil, ay));
			HashMap fields = new HashMap();
			if (vardiyaPlaniOtomatikOlustur) {
				if (vardiyaGunHome == null) {
					vardiyaGunHome = new VardiyaGunHome();
					vardiyaGunHome.setInject(session, entityManager, pdksEntityController, ortakIslemler, fazlaMesaiOrtakIslemler);
				}
				vardiyaGunHome.setSicilNo("");
				vardiyaGunHome.setDenklestirmeAyDurum(true);
				vardiyaGunHome.setLoginUser(loginUser);
			}
			if (fazlaMesaiHesaplaHome == null) {
				fazlaMesaiHesaplaHome = new FazlaMesaiHesaplaHome();
				fazlaMesaiHesaplaHome.setInject(session, entityManager, pdksEntityController, ortakIslemler, fazlaMesaiOrtakIslemler);
			}
			if (loginUser.getLogin().booleanValue() == false) {
				ortakIslemler.setInject(null, null, loginUser);
				fazlaMesaiOrtakIslemler.setInject(null, null, ortakIslemler, loginUser);
			}
			fazlaMesaiHesaplaHome.setHataliPuantajGoster(false);
			fazlaMesaiHesaplaHome.setSicilNo("");
			fazlaMesaiHesaplaHome.setStajerSirket(false);
			fazlaMesaiHesaplaHome.setSeciliEkSaha4Id(null);
			fazlaMesaiHesaplaHome.setDenklestirmeAyDurum(true);
			HashMap<String, Object> veriMap = new HashMap<String, Object>();
			StringBuffer sb = new StringBuffer();
			List<Tanim> bordroAlanlari = ortakIslemler.getTanimList(Tanim.TIPI_BORDRDO_ALANLARI, session);
			bordroAlanlari = PdksUtil.sortObjectStringAlanList(bordroAlanlari, "getErpKodu", null);
			LinkedHashMap<String, String> baslikMap = new LinkedHashMap<String, String>();
			for (Tanim tanim : bordroAlanlari)
				baslikMap.put(tanim.getKodu(), tanim.getAciklama());

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
					List<Long> sirketGrupIdList = new ArrayList<Long>();
					denklestirmeDonemi = new DepartmanDenklestirmeDonemi();
					aylikPuantaj = fazlaMesaiOrtakIslemler.getAylikPuantaj(ay, yil, denklestirmeDonemi, session);
					aylikPuantaj.setLoginUser(loginUser);
					aylikPuantaj.setDenklestirmeAy(denklestirmeAy);
					denklestirmeDonemi.setDenklestirmeAy(denklestirmeAy);
					denklestirmeDonemi.setLoginUser(loginUser);
					List<SelectItem> depList = fazlaMesaiOrtakIslemler.getFazlaMesaiDepartmanList(aylikPuantaj, !vardiyaPlaniOtomatikOlustur, session);
					boolean mesajGonder = false;
					if (!depList.isEmpty()) {
						logger.info(denklestirmeAy.getAyAdi() + " " + denklestirmeAy.getYil() + " in " + PdksUtil.getCurrentTimeStampStr());
						if (vardiyaPlaniOtomatikOlustur) {
							vardiyaGunHome.setDenklestirmeAy(denklestirmeAy);
							vardiyaGunHome.setYil(denklestirmeAy.getYil());
							vardiyaGunHome.setAy(denklestirmeAy.getAy());
						}
						fazlaMesaiHesaplaHome.setDenklestirmeAy(denklestirmeAy);
						fazlaMesaiHesaplaHome.setYil(denklestirmeAy.getYil());
						fazlaMesaiHesaplaHome.setAy(denklestirmeAy.getAy());
						for (SelectItem selectItemDepartman : depList) {
							Long departmanId = (Long) selectItemDepartman.getValue();
							fazlaMesaiHesaplaHome.setDepartmanId(departmanId);
							List<SelectItem> sirketList = fazlaMesaiOrtakIslemler.getFazlaMesaiSirketList(departmanId, aylikPuantaj, !vardiyaPlaniOtomatikOlustur, session);
							for (SelectItem selectItemSirket : sirketList) {
								Long sirketId = (Long) selectItemSirket.getValue();
								fields.clear();
								fields.put("id", sirketId);
								if (session != null)
									fields.put(PdksEntityController.MAP_KEY_SESSION, session);
								Sirket sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);
								if (sirket != null) {
									if (sirket.getSirketGrup() != null) {
										Long grupId = sirket.getSirketGrup().getId();
										if (!sirketGrupIdList.contains(grupId)) {
											sirketGrupIdList.add(grupId);
										} else
											continue;
									}
									veriMap.clear();
									if (depList.size() > 1)
										veriMap.put("departmanAdi", selectItemDepartman.getLabel());
									veriMap.put("sirket", sirket);
									if (manuel)
										veriMap.put("manuel", manuel);
									veriMap.put("denklestirme", denklestirme);
									veriMap.put("denklestirmeAy", denklestirmeAy);
									veriMap.put("dosyalar", dosyalar);
									veriMap.put("baslikMap", baslikMap);
									veriMap.put("vardiyaPlaniOtomatikOlustur", vardiyaPlaniOtomatikOlustur);
									Departman departman = sirket.getDepartman();
									fazlaMesaiHesaplaHome.setDepartman(departman);
									fazlaMesaiHesaplaHome.setSirket(sirket);
									fazlaMesaiHesaplaHome.setSirketId(sirket.getId());
									if (sirket.isTesisDurumu()) {
										List<SelectItem> tesisList = fazlaMesaiOrtakIslemler.getFazlaMesaiTesisList(sirket, aylikPuantaj, !vardiyaPlaniOtomatikOlustur, session);
										for (SelectItem selectItemTesis : tesisList) {
											Long tesisId = (Long) selectItemTesis.getValue();
											veriMap.put("tesisId", tesisId);
											bolumFazlaMesai(veriMap, session);
										}
									} else
										bolumFazlaMesai(veriMap, session);
									mesajGonder = manuel == false;
								}
							}
						}
						try {
							if (mesajGonder) {
								if (sb.length() > 0)
									sb.append(", ");
								sb.append(denklestirmeAy.getAyAdi() + " " + denklestirmeAy.getYil());
							}
						} catch (Exception e) {
							logger.error(e);
							e.printStackTrace();
						}
						logger.info(denklestirmeAy.getAyAdi() + " " + denklestirmeAy.getYil() + " out " + PdksUtil.getCurrentTimeStampStr());
					}
					sirketGrupIdList = null;
				}
				if (manuel) {
					FacesMessages facesMessages = (FacesMessages) Component.getInstance("facesMessages");
					facesMessages.clear();
				}

			}
			if (manuel == false)
				logger.debug(sb.toString() + " " + dosyalar.size() + " " + PdksUtil.getCurrentTimeStampStr());
			if (sb.length() > 0 || !dosyalar.isEmpty())
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
					Date bitTarih = new Date();
					sb = new StringBuffer();
					sb.append(aylar + " " + (aylar.indexOf(",") > 0 ? "dönemleri" : "dönemi") + " fazla mesailer güncellenmiştir.");
					sb.append("<br></br><b>Çalışma Aralığı : </b>" + loginUser.timeLongFormatla(basTarih) + " - " + loginUser.timeLongFormatla(bitTarih) + "<br></br>");
					zamanlayici.mailGonderDosya(session, null, "Fazla Mesai Güncellemesi", sb.toString(), null, dosya, Boolean.TRUE);
					sb = null;
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}
			sb = null;
			veriMap = null;
			dosyalar = null;
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
		boolean vardiyaPlaniOtomatikOlustur = veriMap.containsKey("vardiyaPlaniOtomatikOlustur") ? (Boolean) veriMap.get("vardiyaPlaniOtomatikOlustur") : false;
		String departmanOnEk = veriMap.containsKey("departmanAdi") ? (String) veriMap.get("departmanAdi") + "/" : "";
		Long tesisId = veriMap.containsKey("tesisId") ? (Long) veriMap.get("tesisId") : null;
		Sirket sirket = veriMap.containsKey("sirket") ? (Sirket) veriMap.get("sirket") : null;
		String sirketAdi = sirket.getSirketGrup() == null ? sirket.getAd() : sirket.getSirketGrup().getAciklama();
		List<SelectItem> bolumList = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, tesisId != null ? String.valueOf(tesisId) : "", aylikPuantaj, !vardiyaPlaniOtomatikOlustur, session);
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
		AramaSecenekleri as = new AramaSecenekleri();
		as.setSicilNo("");
		as.setDepartman(sirket.getDepartman());
		as.setDepartmanId(as.getDepartman().getId());
		as.setSirket(sirket);
		as.setSirketId(sirket.getId());
		as.setTesisId(tesisId);
		as.setLoginUser(loginUser);
		String baslik = dm.getAyAdi() + " " + dm.getYil() + " " + sirketAdi + (tesis != null ? " " + tesis.getAciklama() : "");
		Date basGun = PdksUtil.convertToJavaDate(String.valueOf(dm.getYil() * 100 + dm.getAy()) + "01", "yyyyMMdd"), bugun = new Date();
		boolean gelecekTarih = basGun.after(bugun);
		boolean hataVar = false;
		for (SelectItem selectItem : bolumList) {
			Long seciliEkSaha3Id = (Long) selectItem.getValue();
			fields.clear();
			fields.put("id", seciliEkSaha3Id);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			Tanim bolum = (Tanim) pdksEntityController.getObjectByInnerObject(fields, Tanim.class);
			String str = baslik + (bolum != null ? " " + bolum.getAciklama() : "");
			// session.beginTransaction().begin();
			loginUser.setAdmin(Boolean.TRUE);
			List<Personel> donemPerList = fazlaMesaiOrtakIslemler.getFazlaMesaiPersonelList(sirket, tesisId != null ? String.valueOf(tesisId) : null, seciliEkSaha3Id, null, aylikPuantaj, false, session);
			boolean kayitVar = !donemPerList.isEmpty();
			if (vardiyaPlaniOtomatikOlustur || gelecekTarih) {
				as.setEkSaha3Id(seciliEkSaha3Id);
				boolean devam = kayitVar;
				int adet = 0;
				while (devam && adet < 2) {
					session.clear();
					List<Personel> donemCPPerList = fazlaMesaiOrtakIslemler.getFazlaMesaiPersonelList(dm, donemPerList, session);
					devam = donemCPPerList.size() != donemPerList.size();
					try {
						if (devam) {
							logger.info(str + " aylikPuantajOlusturuluyor in " + PdksUtil.getCurrentTimeStampStr());
							vardiyaGunHome.setSession(session);
							vardiyaGunHome.setAramaSecenekleri(as);
							vardiyaGunHome.aylikPuantajOlusturuluyor();
							logger.info(str + " aylikPuantajOlusturuluyor out " + PdksUtil.getCurrentTimeStampStr());
						}
					} catch (Exception e) {
						System.err.println(e);
						e.printStackTrace();
					}
					++adet;
					donemCPPerList = null;
				}

			}
			as.setEkSaha3Id(null);
			fazlaMesaiHesaplaHome.setSession(session);
			fazlaMesaiHesaplaHome.setSeciliEkSaha3Id(seciliEkSaha3Id);
			fazlaMesaiHesaplaHome.setTopluGuncelle(true);
			
			try {
				if (!hataVar && gelecekTarih == false && kayitVar) {
					logger.info(str + " [ " + donemPerList.size() + " ] in " + PdksUtil.getCurrentTimeStampStr());
					loginUser.setAdmin(Boolean.TRUE);
					List<AylikPuantaj> puantajList = fazlaMesaiHesaplaHome.fillPersonelDenklestirmeDevam(aylikPuantaj, denklestirmeDonemi);
					hataVar = puantajList.isEmpty();
					logger.info(str + (puantajList != null ? " [ " + puantajList.size() + " ]" : "") + " out " + PdksUtil.getCurrentTimeStampStr());
				}
				donemPerList = null;
				session.flush();
				// session.getTransaction().commit();
			} catch (Exception e) {
				// session.getTransaction().rollback();
				logger.error(e);
				e.printStackTrace();
				hataVar = true;
			}
			if (hataVar)
				break;

		}
		if (hataVar == false && manuelDurum == false) {
			loginUser.setAdmin(Boolean.TRUE);
			List<AylikPuantaj> personelDenklestirmeList = fazlaMesaiOrtakIslemler.getBordoDenklestirmeList(dm, as, false, false, session);
			if (!personelDenklestirmeList.isEmpty()) {
				ByteArrayOutputStream baos = null;
				HashMap<String, Object> dataMap = new HashMap<String, Object>();
				dataMap.put("personelDenklestirmeList", new ArrayList(personelDenklestirmeList));
				dataMap.put("loginUser", loginUser);
				dataMap.putAll(veriMap);
				try {
					baos = fazlaMesaiOrtakIslemler.denklestirmeExcelAktarDevam(dataMap, session);
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}
				dataMap = null;
				if (baos != null) {
					try {
						List<Dosya> dosyalar = veriMap.containsKey("dosyalar") ? (List<Dosya>) veriMap.get("dosyalar") : new ArrayList<Dosya>();
						String dosyaAdi = dm.getAyAdi() + " " + dm.getYil() + "/" + departmanOnEk + (tesis != null ? sirketAdi + "/" : "") + sirketAdi + (tesis != null ? "_" + tesis.getAciklama() : "") + ".xlsx";
						Dosya dosyaExcel = new Dosya();
						dosyaExcel.setDosyaAdi(dosyaAdi);
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