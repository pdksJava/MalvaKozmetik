package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.pdks.entity.Departman;
import org.pdks.entity.Sirket;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.IzinTipiMailAdres;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.Tanim;
import org.pdks.security.entity.User;
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

@Name("izinTipiHome")
public class IzinTipiHome extends EntityHome<IzinTipi> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4250605793348432903L;
	static Logger logger = Logger.getLogger(IzinTipiHome.class);

	@RequestParameter
	Long izinTipiId;
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false)
	User authenticatedUser;
	@In(required = true, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false)
	FacesMessages facesMessages;

	private List<Departman> departmanList = new ArrayList<Departman>();
	private List<Sirket> sirketList = new ArrayList<Sirket>();
	private List<IzinTipi> izinTipiList = new ArrayList<IzinTipi>();
	private List<Tanim> izinTipiTanimList = new ArrayList<Tanim>();
	private List<Tanim> bakiyeIzinTipiTanimList = new ArrayList<Tanim>();
	private List<Tanim> bilgiTipiList = new ArrayList<Tanim>();
	private List<SelectItem> personelGirisTipiList = new ArrayList<SelectItem>(), bakiyeDevirTipiList = new ArrayList<SelectItem>(), mailTipiList;
	private List<SelectItem> onaylayanTipiList = new ArrayList<SelectItem>();
	private List<Tanim> cinsiyetList = new ArrayList<Tanim>();
	private List<SelectItem> hesapTipiList = null, durumCGSList = new ArrayList<SelectItem>(), mailGonderimDurumlari;
	private IzinTipi bakiyeIzinTipi;
	public Tanim selectedDepartman, bilgiTipi;
	private List<IzinTipiMailAdres> mailCCAdresList, mailBCCAdresList;
	private String mailTipi, mailAdres;
	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	@Override
	public Object getId() {
		if (izinTipiId == null) {
			return super.getId();
		} else {
			return izinTipiId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public void fillHesapTipiList() {
		hesapTipiList = new ArrayList<SelectItem>();
		hesapTipiList.add(new SelectItem(0, PdksUtil.getMessageBundleMessage("izin.etiket.hesapTipi0")));
		hesapTipiList.add(new SelectItem(PersonelIzin.HESAP_TIPI_GUN, PdksUtil.getMessageBundleMessage("izin.etiket.hesapTipi" + PersonelIzin.HESAP_TIPI_GUN)));
		hesapTipiList.add(new SelectItem(PersonelIzin.HESAP_TIPI_SAAT, PdksUtil.getMessageBundleMessage("izin.etiket.hesapTipi" + PersonelIzin.HESAP_TIPI_SAAT)));
	}

	public String addMailAdres() {
		List<String> mesaj = new ArrayList<String>();
		if (mailTipi != null) {
			IzinTipi izinTipi = getInstance();
			if (mailAdres != null && mailAdres.indexOf("@") > 1) {
				boolean ekle = Boolean.TRUE;
				List<String> adresler = PdksUtil.getListFromString(mailAdres, null);
				List<IzinTipiMailAdres> seciliAdresler = mailTipi.equals(IzinTipiMailAdres.TIPI_CC) ? mailCCAdresList : mailBCCAdresList;
				List<IzinTipiMailAdres> eskiAdresler = new ArrayList<IzinTipiMailAdres>(), yeniAdresler = new ArrayList<IzinTipiMailAdres>();
				if (!mailCCAdresList.isEmpty())
					eskiAdresler.addAll(mailCCAdresList);
				if (!mailBCCAdresList.isEmpty())
					eskiAdresler.addAll(mailBCCAdresList);
				int sira = seciliAdresler.isEmpty() ? 0 : seciliAdresler.get(seciliAdresler.size() - 1).getSira();
				for (Iterator iterator = adresler.iterator(); iterator.hasNext();) {
					String string = (String) iterator.next();
					iterator.remove();
					String adres = null;
					if (string.indexOf("@") > 1) {
						adres = PdksUtil.getInternetAdres(PdksUtil.setTurkishStr(string).toLowerCase(Locale.ENGLISH));
						for (IzinTipiMailAdres izinTipiMailAdres : eskiAdresler) {
							if (izinTipiMailAdres.getAdres().equals(adres)) {
								ekle = Boolean.FALSE;
								mesaj.add(string + " " + (izinTipiMailAdres.getTipi().equals(IzinTipiMailAdres.TIPI_CC) ? " CC " : " BCC ") + " mail adresi listesinde " + izinTipiMailAdres.getSira() + ". sırasındadır.");
								adres = null;
							}

						}
						if (adresler.contains(string)) {
							mesaj.add(string + " liste tekrar etmektedir! ");
							ekle = Boolean.FALSE;
							adres = null;
						}
					} else
						ekle = Boolean.FALSE;
					if (adres != null) {
						IzinTipiMailAdres tipiMailAdres = new IzinTipiMailAdres();
						tipiMailAdres.setIzinTipi(izinTipi);
						tipiMailAdres.setSira(++sira);
						tipiMailAdres.setAdres(adres);
						tipiMailAdres.setTipi(mailTipi);
						yeniAdresler.add(tipiMailAdres);
					}

				}
				if (ekle && mesaj.isEmpty()) {
					seciliAdresler.addAll(yeniAdresler);
					mailAdres = "";
					mailTipi = null;
					yeniAdresler = null;
					eskiAdresler = null;
				} else {
					if (mesaj.isEmpty())
						mesaj.add("Hatalı mail adresi!");

				}

			} else
				mesaj.add("Hatalı mail adresi!");
		} else
			mesaj.add("Mail tipi seçiniz!");
		for (Iterator iterator = mesaj.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			PdksUtil.addMessageAvailableWarn(string);
		}
		mesaj = null;

		return "";
	}

	public String deleteMailAdres(IzinTipiMailAdres izinTipiMailAdres) {
		String tipi = izinTipiMailAdres.getTipi();
		List<IzinTipiMailAdres> seciliAdresler = tipi.equals(IzinTipiMailAdres.TIPI_CC) ? mailCCAdresList : mailBCCAdresList;
		int sira = 0;
		for (Iterator iterator = seciliAdresler.iterator(); iterator.hasNext();) {
			IzinTipiMailAdres izinTipiMailAdres2 = (IzinTipiMailAdres) iterator.next();
			if (izinTipiMailAdres2.getSira() != izinTipiMailAdres.getSira())
				izinTipiMailAdres2.setSira(++sira);
			else
				iterator.remove();

		}

		return "";
	}

	public void izinTipiGuncelle(IzinTipi izinTipi) {
		setInstance(izinTipi);
		mailTipi = null;
		mailAdres = "";
		fillCinsiyetList();
		fillBilgiTipiList();
		if (mailGonderimDurumlari == null)
			mailGonderimDurumlari = new ArrayList<SelectItem>();
		else
			mailGonderimDurumlari.clear();
		mailGonderimDurumlari.add(new SelectItem(IzinTipi.MAIL_GONDERIM_DURUMU_ILK_ONAY, IzinTipi.getMailGonderimDurumAciklama(IzinTipi.MAIL_GONDERIM_DURUMU_ILK_ONAY)));
		mailGonderimDurumlari.add(new SelectItem(IzinTipi.MAIL_GONDERIM_DURUMU_IK_ONAY, IzinTipi.getMailGonderimDurumAciklama(IzinTipi.MAIL_GONDERIM_DURUMU_IK_ONAY)));
		mailGonderimDurumlari.add(new SelectItem(IzinTipi.MAIL_GONDERIM_DURUMU_ONAYSIZ, IzinTipi.getMailGonderimDurumAciklama(IzinTipi.MAIL_GONDERIM_DURUMU_ONAYSIZ)));

		if (mailTipiList == null)
			mailTipiList = new ArrayList<SelectItem>();
		else
			mailTipiList.clear();
		mailTipiList.add(new SelectItem(IzinTipiMailAdres.TIPI_CC, "CC"));
		mailTipiList.add(new SelectItem(IzinTipiMailAdres.TIPI_BCC, "BCC"));
		if (izinTipi.getId() != 0) {
			HashMap parametreMap = new HashMap();

			parametreMap.put("bakiyeIzinTipi", izinTipi);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

			IzinTipi bakiyeIzin = (IzinTipi) pdksEntityController.getObjectByInnerObject(parametreMap, IzinTipi.class);
			setBakiyeIzinTipi(bakiyeIzin);
			HashMap fields = new HashMap();
			fields.put("izinTipi.id", izinTipi.getId());
			fields.put("tipi", IzinTipiMailAdres.TIPI_CC);

			int sira = 0;
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			mailCCAdresList = pdksEntityController.getObjectByInnerObjectList(fields, IzinTipiMailAdres.class);
			for (IzinTipiMailAdres izinTipiMailAdres : mailCCAdresList)
				izinTipiMailAdres.setSira(++sira);
			sira = 0;
			fields.clear();
			fields.put("izinTipi.id", izinTipi.getId());
			fields.put("tipi", IzinTipiMailAdres.TIPI_BCC);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			mailBCCAdresList = pdksEntityController.getObjectByInnerObjectList(fields, IzinTipiMailAdres.class);
			for (IzinTipiMailAdres izinTipiMailAdres : mailBCCAdresList)
				izinTipiMailAdres.setSira(++sira);

		} else {
			if (mailCCAdresList == null)
				mailCCAdresList = new ArrayList<IzinTipiMailAdres>();
			else
				mailCCAdresList.clear();

			if (mailBCCAdresList == null)
				mailBCCAdresList = new ArrayList<IzinTipiMailAdres>();
			else
				mailBCCAdresList.clear();

		}
		setBilgiTipi(izinTipi.getIzinTipiTanim().getParentTanim());
		fillIzinTipiTanimList();
		fillBakiyeIzinTipiTanimList(izinTipi.getIzinTipiTanim());
	}

	public void izinTipiEkle() {
		departmanList = ortakIslemler.fillDepartmanTanimList(session);
		IzinTipi izinTipi = new IzinTipi();
		if (departmanList != null && departmanList.size() == 1)
			izinTipi.setDepartman(departmanList.get(0));
		else
			izinTipi.setDepartman(authenticatedUser.getDepartman());
		setInstance(izinTipi);
		setBakiyeIzinTipi(null);
		setBilgiTipi(null);
		// fillBilgiTipiList();
		getInstance().setPersonelGirisTipi(IzinTipi.GIRIS_TIPI_PERSONEL);
		setIzinTipiTanimList(new ArrayList<Tanim>());
		fillIzinTipiTanimList();
	}

	@Transactional
	public String save() {
		IzinTipi izinTipi = getInstance();
		if (izinTipi.isTakvimGunuMu()) {
			izinTipi.setOffDahil(Boolean.FALSE);
			izinTipi.setHtDahil(Boolean.FALSE);
		}
		if (bakiyeIzinTipi != null) {
			bakiyeIzinTipi.setErpAktarim(izinTipi.getErpAktarim());
			bakiyeIzinTipi.setHesapTipi(izinTipi.getHesapTipi());
			bakiyeIzinTipi.setSaatGosterilecek(izinTipi.getSaatGosterilecek());
			bakiyeIzinTipi.setGunGosterilecek(izinTipi.getGunGosterilecek());
			bakiyeIzinTipi.setDurumCGS(izinTipi.getDurumCGS());

			bakiyeIzinTipi.setOffDahil(Boolean.FALSE);
			bakiyeIzinTipi.setHtDahil(Boolean.FALSE);

		}
		String ok = "persisted";
		List<IzinTipiMailAdres> adresler = new ArrayList<IzinTipiMailAdres>();
		if (izinTipi.getMailGonderimDurumu() != null) {
			if (!mailCCAdresList.isEmpty())
				adresler.addAll(mailCCAdresList);
			if (!mailBCCAdresList.isEmpty())
				adresler.addAll(mailBCCAdresList);
			List<String> mailler = new ArrayList<String>();
			ortakIslemler.getAktifKullanicilar(adresler, mailler, session);
			if (adresler.isEmpty())
				mailler.add(izinTipi.getMailGonderimDurumAciklama() + " için mail adresleri giriniz!");
			if (izinTipi.isOnaysiz() != izinTipi.isMailGonderimDurumOnaysiz())
				mailler.add(izinTipi.getMailGonderimDurumAciklama() + " mail tipi hatalıdır!");
			if (!mailler.isEmpty()) {
				for (Iterator iterator = mailler.iterator(); iterator.hasNext();) {
					String string = (String) iterator.next();
					PdksUtil.addMessageAvailableWarn(string);
				}
				mailler = null;
				return "";
			}
			mailler = null;
		}
		try {
			if (izinTipi.getId() == null)
				izinTipi.setOlusturanUser(authenticatedUser);
			else {
				izinTipi.setGuncelleyenUser(authenticatedUser);
				izinTipi.setGuncellemeTarihi(new Date());
			}

			String mesaj = "";
			if (!izinTipi.getBakiyeDevirTipi().equals(IzinTipi.BAKIYE_DEVIR_YOK)) {
				if (bakiyeIzinTipi == null || bakiyeIzinTipi.getIzinTipiTanim() == null) {
					ok = "";
					PdksUtil.addMessageWarn("Bakiye izin tipi tanımsız!");
				} else {

					if (bakiyeIzinTipi.getId() == null) {
						Tanim izinTipiTanim = bakiyeIzinTipi.getIzinTipiTanim();
						if (bakiyeIzinTipi == null) {
							bakiyeIzinTipi = (IzinTipi) izinTipi.clone();
							bakiyeIzinTipi.setId(null);
						}
						bakiyeIzinTipi.setIzinTipiTanim(izinTipiTanim);
						bakiyeIzinTipi.setIzinKagidiGeldi(Boolean.FALSE);
						bakiyeIzinTipi.setOnaylayanTipi(IzinTipi.ONAYLAYAN_TIPI_YOK);
						bakiyeIzinTipi.setPersonelGirisTipi(IzinTipi.GIRIS_TIPI_YOK);
						bakiyeIzinTipi.setErpAktarim(Boolean.FALSE);
						bakiyeIzinTipi.setDokumAlmaDurum(Boolean.FALSE);
						bakiyeIzinTipi.setIzinKagidiGeldi(Boolean.FALSE);
						bakiyeIzinTipi.setGuncelleyenUser(null);
						bakiyeIzinTipi.setGuncellemeTarihi(null);
						bakiyeIzinTipi.setBakiyeIzinTipi(izinTipi);
						bakiyeIzinTipi.setOlusturanUser(authenticatedUser);
						bakiyeIzinTipi.setOffDahil(Boolean.FALSE);

					}
					bakiyeIzinTipi.setSaatGosterilecek(izinTipi.getSaatGosterilecek());
					bakiyeIzinTipi.setGunGosterilecek(izinTipi.getGunGosterilecek());
					bakiyeIzinTipi.setHesapTipi(izinTipi.getHesapTipi());
					bakiyeIzinTipi.setOffDahil(izinTipi.getOffDahil());
					if (!izinTipi.isBakiyeSenelik())
						bakiyeIzinTipi.setKotaBakiye(null);
					pdksEntityController.saveOrUpdate(session, entityManager, bakiyeIzinTipi);
				}
			}
			if (mesaj.equals("")) {
				izinTipi.setKotaBakiye(null);

				if (izinTipi.getTakvimGunumu()) {
					izinTipi.setGunGosterilecek(Boolean.TRUE);
					izinTipi.setSaatGosterilecek(Boolean.FALSE);
					izinTipi.setOffDahil(Boolean.FALSE);
					izinTipi.setHesapTipi(PersonelIzin.HESAP_TIPI_GUN);
				} else if (!izinTipi.getSaatGosterilecek() && izinTipi.getGunGosterilecek())
					izinTipi.setHesapTipi(PersonelIzin.HESAP_TIPI_GUN);
				else if (!izinTipi.getGunGosterilecek() && izinTipi.getSaatGosterilecek())
					izinTipi.setHesapTipi(PersonelIzin.HESAP_TIPI_SAAT);
				else if (izinTipi.getHesapTipi() == 0)
					izinTipi.setHesapTipi(null);
				pdksEntityController.saveOrUpdate(session, entityManager, izinTipi);
				if (bakiyeIzinTipi != null)
					pdksEntityController.saveOrUpdate(session, entityManager, bakiyeIzinTipi);
				HashMap fields = new HashMap();
				fields.put(PdksEntityController.MAP_KEY_MAP, "getAdres");
				fields.put("izinTipi.id", izinTipi.getId());
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				TreeMap<String, IzinTipiMailAdres> mailMap = pdksEntityController.getObjectByInnerObjectMap(fields, IzinTipiMailAdres.class, false);

				for (Iterator iterator = adresler.iterator(); iterator.hasNext();) {
					IzinTipiMailAdres izinTipiMailAdres = (IzinTipiMailAdres) iterator.next();
					if (mailMap.containsKey(izinTipiMailAdres.getAdres())) {
						IzinTipiMailAdres tipiMailAdres = mailMap.get(izinTipiMailAdres.getAdres());
						if (!tipiMailAdres.getTipi().equals(izinTipiMailAdres.getTipi())) {
							tipiMailAdres.setTipi(izinTipiMailAdres.getTipi());
							pdksEntityController.saveOrUpdate(session, entityManager, tipiMailAdres);
						}
						mailMap.remove(izinTipiMailAdres.getAdres());
					} else
						pdksEntityController.saveOrUpdate(session, entityManager, izinTipiMailAdres);

				}
				if (!mailMap.isEmpty()) {
					adresler = null;
					adresler = new ArrayList<IzinTipiMailAdres>(mailMap.values());
					for (Iterator iterator = adresler.iterator(); iterator.hasNext();) {
						IzinTipiMailAdres izinTipiMailAdres = (IzinTipiMailAdres) iterator.next();
						pdksEntityController.deleteObject(session, entityManager, izinTipiMailAdres);
 					}
				}
				adresler = null;
				mailMap = null;
				session.flush();
				fillIzinTipiList();

			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

			ok = "";
		}
		return ok;

	}

	public void fillBilgiTipiList() {
		List<Tanim> tanimList = ortakIslemler.getTanimList(Tanim.TIPI_SAP_IZIN_BILGI_TIPI, session);
		setBilgiTipiList(tanimList);
	}

	public void fillPersonelGirisTipiList(IzinTipi izinTipi) {
		List<SelectItem> list = new ArrayList<SelectItem>();
		for (int i = 0; i <= 3; i++) {
			izinTipi.setPersonelGirisTipi(String.valueOf(i));
			list.add(new SelectItem(izinTipi.getPersonelGirisTipi(), izinTipi.getPersonelGirisTipiAciklama()));
		}
		setPersonelGirisTipiList(list);
	}

	public void fillOnaylayanTipiList(IzinTipi izinTipi) {
		List<SelectItem> list = new ArrayList<SelectItem>();
		for (int i = 0; i <= 3; i++) {
			izinTipi.setOnaylayanTipi(String.valueOf(i));
			list.add(new SelectItem(izinTipi.getOnaylayanTipi(), izinTipi.getOnaylayanTipiAciklama()));
		}
		setOnaylayanTipiList(list);
	}

	public void fillBakiyeDevirTipiList(IzinTipi izinTipi) {
		List<SelectItem> list = new ArrayList<SelectItem>();
		for (int i = 0; i <= 2; i++) {
			izinTipi.setBakiyeDevirTipi(String.valueOf(i));
			list.add(new SelectItem(izinTipi.getBakiyeDevirTipi(), izinTipi.getBakiyeDevirTipiAciklama()));
		}
		setBakiyeDevirTipiList(list);
	}

	public void fillCinsiyetList() {
		List<Tanim> tanimList = ortakIslemler.getTanimList(Tanim.TIPI_CINSIYET, session);
		setCinsiyetList(tanimList);

	}

	public void fillIzinTipiList() {

		List<IzinTipi> izinTipiList = new ArrayList<IzinTipi>();
		HashMap parametreMap = new HashMap();
		try {
			parametreMap.put("bakiyeIzinTipi=", null);
			if (!authenticatedUser.isAdmin())
				parametreMap.put("departman=", authenticatedUser.getDepartman());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			izinTipiList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, IzinTipi.class);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.debug(e.getMessage());
		}
		if (izinTipiList.size() > 1)
			izinTipiList = PdksUtil.sortObjectStringAlanList(null, izinTipiList, "getSira", null);
		setIzinTipiList(izinTipiList);
	}

	public void fillBakiyeIzinTipiTanimList(Tanim izinTipitanim) {
		IzinTipi izinTipi = getInstance();
		List<Tanim> tanimList = null;
		if (izinTipitanim != null && !izinTipi.getBakiyeDevirTipi().equals(IzinTipi.BAKIYE_DEVIR_YOK)) {
			HashMap parametreMap = new HashMap();
			parametreMap.put("kodu", izinTipitanim.getKodu());
			parametreMap.put("tipi", Tanim.TIPI_BAKIYE_IZIN_TIPI);
			parametreMap.put("durum", Boolean.TRUE);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				tanimList = pdksEntityController.getObjectByInnerObjectList(parametreMap, Tanim.class);
				if (tanimList.size() > 1)
					tanimList = PdksUtil.sortObjectStringAlanList(tanimList, "getAciklama", null);
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());

			}

			if (bakiyeIzinTipi == null) {
				bakiyeIzinTipi = (IzinTipi) izinTipi.clone();
				bakiyeIzinTipi.setId(null);
				bakiyeIzinTipi.setBakiyeIzinTipi(izinTipi);
				bakiyeIzinTipi.setIzinTipiTanim(izinTipi.getIzinTipiTanim());
				setBakiyeIzinTipi(bakiyeIzinTipi);
			} else if (bakiyeIzinTipi.getId() == null) {
				bakiyeIzinTipi.setIzinTipiTanim(izinTipi.getIzinTipiTanim());
			}
		}
		setBakiyeIzinTipiTanimList(tanimList);
	}

	public void fillIzinTipiTanimList() {
		IzinTipi izinTipi = getInstance();
		List<Tanim> tanimList = null;

		HashMap parametreMap = new HashMap();
		parametreMap.put(PdksEntityController.MAP_KEY_MAP, "getId");
		parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "izinTipiTanim");
		if (izinTipi.getDepartman() != null)
			parametreMap.put("departman.id=", izinTipi.getDepartman().getId());
		if (izinTipi.getId() != null)
			parametreMap.put("id<>", izinTipi.getId());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		TreeMap tanimMap = pdksEntityController.getObjectByInnerObjectMapInLogic(parametreMap, IzinTipi.class, Boolean.FALSE);
		parametreMap.clear();
		if (bilgiTipi != null)
			parametreMap.put("parentTanim.id", bilgiTipi.getId());
		parametreMap.put("tipi", Tanim.TIPI_IZIN_TIPI);
		parametreMap.put("durum", Boolean.TRUE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			tanimList = pdksEntityController.getObjectByInnerObjectList(parametreMap, Tanim.class);
			for (Iterator iterator = tanimList.iterator(); iterator.hasNext();) {
				Tanim tanim = (Tanim) iterator.next();
				if (tanimMap.containsKey(tanim.getId()))
					iterator.remove();

			}
			if (izinTipi.getId() != null) {
				Tanim izinTipiTanim = izinTipi.getIzinTipiTanim();
				boolean ekle = true;
				for (Iterator iterator = tanimList.iterator(); iterator.hasNext();) {
					Tanim tanim = (Tanim) iterator.next();
					if (tanim.getId().equals(izinTipiTanim.getId()))
						ekle = false;

				}
				if (ekle)
					tanimList.add(izinTipiTanim);
			}
			if (tanimList.size() > 1)
				tanimList = PdksUtil.sortObjectStringAlanList(tanimList, "getAciklama", null);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		setIzinTipiTanimList(tanimList);
	}

	public void fillBagliOlduguDepartmanTanimList() {
		List<Departman> list = null;
		try {
			list = ortakIslemler.fillDepartmanTanimList(session);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Departman departman = (Departman) iterator.next();
				if (departman.getIzinGirilebilir().equals(Boolean.FALSE))
					iterator.remove();

			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		setDepartmanList(list);
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
		durumCGSList.clear();
		durumCGSList.add(new SelectItem(IzinTipi.CGS_DURUM_YOK, IzinTipi.getDurumCGSAciklama(IzinTipi.CGS_DURUM_YOK)));
		durumCGSList.add(new SelectItem(IzinTipi.CGS_DURUM_CIKAR, IzinTipi.getDurumCGSAciklama(IzinTipi.CGS_DURUM_CIKAR)));
		durumCGSList.add(new SelectItem(IzinTipi.CGS_DURUM_EKLE, IzinTipi.getDurumCGSAciklama(IzinTipi.CGS_DURUM_EKLE)));
		IzinTipi izinTipi = new IzinTipi();
		fillPersonelGirisTipiList(izinTipi);
		fillBakiyeDevirTipiList(izinTipi);
		fillOnaylayanTipiList(izinTipi);
		fillIzinTipiList();
		fillHesapTipiList();
		fillBagliOlduguDepartmanTanimList();

		setIzinTipiTanimList(new ArrayList<Tanim>());

	}

	public List<Departman> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<Departman> departmanList) {
		this.departmanList = departmanList;
	}

	public List<Tanim> getBilgiTipiList() {
		return bilgiTipiList;
	}

	public void setBilgiTipiList(List<Tanim> bilgiTipiList) {
		this.bilgiTipiList = bilgiTipiList;
	}

	public List<SelectItem> getPersonelGirisTipiList() {
		return personelGirisTipiList;
	}

	public void setPersonelGirisTipiList(List<SelectItem> personelGirisTipiList) {
		this.personelGirisTipiList = personelGirisTipiList;
	}

	public List<Tanim> getCinsiyetList() {
		return cinsiyetList;
	}

	public void setCinsiyetList(List<Tanim> cinsiyetList) {
		this.cinsiyetList = cinsiyetList;
	}

	public List<SelectItem> getBakiyeDevirTipiList() {
		return bakiyeDevirTipiList;
	}

	public void setBakiyeDevirTipiList(List<SelectItem> bakiyeDevirTipiList) {
		this.bakiyeDevirTipiList = bakiyeDevirTipiList;
	}

	public List<SelectItem> getOnaylayanTipiList() {
		return onaylayanTipiList;
	}

	public void setOnaylayanTipiList(List<SelectItem> onaylayanTipiList) {
		this.onaylayanTipiList = onaylayanTipiList;
	}

	public Tanim getBilgiTipi() {
		return bilgiTipi;
	}

	public void setBilgiTipi(Tanim bilgiTipi) {
		this.bilgiTipi = bilgiTipi;
	}

	public List<Tanim> getBakiyeIzinTipiTanimList() {
		return bakiyeIzinTipiTanimList;
	}

	public void setBakiyeIzinTipiTanimList(List<Tanim> bakiyeIzinTipiTanimList) {
		this.bakiyeIzinTipiTanimList = bakiyeIzinTipiTanimList;
	}

	public IzinTipi getBakiyeIzinTipi() {
		return bakiyeIzinTipi;
	}

	public void setBakiyeIzinTipi(IzinTipi bakiyeIzinTipi) {
		this.bakiyeIzinTipi = bakiyeIzinTipi;
	}

	public List<SelectItem> getHesapTipiList() {
		return hesapTipiList;
	}

	public void setHesapTipiList(List<SelectItem> hesapTipiList) {
		this.hesapTipiList = hesapTipiList;
	}

	public List<IzinTipiMailAdres> getMailCCAdresList() {
		return mailCCAdresList;
	}

	public void setMailCCAdresList(List<IzinTipiMailAdres> mailCCAdresList) {
		this.mailCCAdresList = mailCCAdresList;
	}

	public List<IzinTipiMailAdres> getMailBCCAdresList() {
		return mailBCCAdresList;
	}

	public void setMailBCCAdresList(List<IzinTipiMailAdres> mailBCCAdresList) {
		this.mailBCCAdresList = mailBCCAdresList;
	}

	public String getMailTipi() {
		return mailTipi;
	}

	public void setMailTipi(String mailTipi) {
		this.mailTipi = mailTipi;
	}

	public List<SelectItem> getMailTipiList() {
		return mailTipiList;
	}

	public void setMailTipiList(List<SelectItem> mailTipiList) {
		this.mailTipiList = mailTipiList;
	}

	public String getMailAdres() {
		return mailAdres;
	}

	public void setMailAdres(String mailAdres) {
		this.mailAdres = mailAdres;
	}

	public List<Tanim> getIzinTipiTanimList() {
		return izinTipiTanimList;
	}

	public void setIzinTipiTanimList(List<Tanim> izinTipiTanimList) {
		this.izinTipiTanimList = izinTipiTanimList;
	}

	public List<Sirket> getSirketList() {

		return sirketList;
	}

	public void setSirketList(List<Sirket> sirketList) {
		this.sirketList = sirketList;
	}

	public List<IzinTipi> getIzinTipiList() {
		return izinTipiList;
	}

	public void setIzinTipiList(List<IzinTipi> izinTipiList) {
		this.izinTipiList = izinTipiList;
	}

	public Tanim getSelectedDepartman() {
		return selectedDepartman;
	}

	public void setSelectedDepartman(Tanim selectedDepartman) {
		this.selectedDepartman = selectedDepartman;
	}

	public List<SelectItem> getMailGonderimDurumlari() {
		return mailGonderimDurumlari;
	}

	public void setMailGonderimDurumlari(List<SelectItem> mailGonderimDurumlari) {
		this.mailGonderimDurumlari = mailGonderimDurumlari;
	}

	public List<SelectItem> getDurumCGSList() {
		return durumCGSList;
	}

	public void setDurumCGSList(List<SelectItem> durumCGSList) {
		this.durumCGSList = durumCGSList;
	}
}
