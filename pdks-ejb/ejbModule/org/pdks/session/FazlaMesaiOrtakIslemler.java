package org.pdks.session;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.Identity;
import org.pdks.entity.AylikPuantaj;
import org.pdks.entity.BordroDetayTipi;
import org.pdks.entity.CalismaModeli;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Departman;
import org.pdks.entity.DepartmanDenklestirmeDonemi;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Liste;
import org.pdks.entity.MenuItem;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelDenklestirmeBordro;
import org.pdks.entity.PersonelDenklestirmeBordroDetay;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.VardiyaHafta;
import org.pdks.entity.VardiyaPlan;
import org.pdks.erp.action.PdksNoSapController;
import org.pdks.erp.action.PdksSap3Controller;
import org.pdks.erp.action.PdksSapController;
import org.pdks.security.entity.User;

import com.pdks.mail.model.MailManager;

/**
 * @author Hasan Sayar
 * 
 */
@Name("fazlaMesaiOrtakIslemler")
public class FazlaMesaiOrtakIslemler implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4677070458220259401L;

	static Logger logger = Logger.getLogger(FazlaMesaiOrtakIslemler.class);

	@In
	Identity identity;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	MailManager mailManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = true, create = true)
	PdksNoSapController pdksNoSapController;
	@In(required = true, create = true)
	PdksSapController pdksSapController;
	@In(required = true, create = true)
	PdksSap3Controller pdksSap3Controller;
	@In(required = false, create = true)
	HashMap<String, String> parameterMap;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@Out(required = false, scope = ScopeType.SESSION)
	User seciliYonetici;
	@In(scope = ScopeType.APPLICATION, required = false)
	HashMap<String, MenuItem> menuItemMap = new HashMap<String, MenuItem>();
	@In(required = false)
	FacesMessages facesMessages;

	/**
	 * @param kaydet
	 * @param puantajList
	 * @param fazlaMesaiHesapla
	 * @param donemStr
	 * @param session
	 * @return
	 */
	public TreeMap<String, Boolean> bordroVeriOlustur(boolean kaydet, List<AylikPuantaj> puantajList, Boolean fazlaMesaiHesapla, String donemStr, Session session) {
		TreeMap<String, Boolean> baslikMap = new TreeMap<String, Boolean>();
		String arifeGunuBordroYarim = ortakIslemler.getParameterKey("arifeGunuBordroYarim");
		boolean saatlikCalismaVar = ortakIslemler.getParameterKey("saatlikCalismaVar").equals("1");
		HashMap fields = new HashMap();
		fields.put("tipi", Tanim.TIPI_IZIN_GRUPLARI);
		fields.put("durum", Boolean.TRUE);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Tanim> list = pdksEntityController.getObjectByInnerObjectList(fields, Tanim.class);
		TreeMap<String, String> izinGrupMap = new TreeMap<String, String>();
		if (list.isEmpty()) {
			BordroDetayTipi[] bordroTipileri = new BordroDetayTipi[] { BordroDetayTipi.UCRETLI_IZIN, BordroDetayTipi.UCRETSIZ_IZIN, BordroDetayTipi.RAPORLU_IZIN };
			for (BordroDetayTipi bordroTipi : bordroTipileri) {
				Tanim tanim = new Tanim();
				tanim.setTipi(Tanim.TIPI_IZIN_GRUPLARI);
				tanim.setAciklamatr(bordroTipi.name());
				tanim.setAciklamaen(bordroTipi.name());
				tanim.setKodu(bordroTipi.value());
				tanim.setErpKodu("izinGrup" + bordroTipi.value());
				pdksEntityController.saveOrUpdate(session, entityManager, tanim);

				list.add(tanim);
			}
			session.flush();
		}

		fields.clear();
		fields.put("tipi", Tanim.TIPI_IZIN_KODU_GRUPLARI);
		fields.put("durum", Boolean.TRUE);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		list = pdksEntityController.getObjectByInnerObjectList(fields, Tanim.class);
		if (!list.isEmpty()) {
			for (Tanim tanim : list) {
				if (!tanim.getKodu().equals(BordroDetayTipi.TANIMSIZ.value())) {
					BordroDetayTipi bordroTipi = null;
					try {
						if (PdksUtil.hasStringValue(tanim.getKodu()))
							bordroTipi = BordroDetayTipi.fromValue(tanim.getKodu().trim());
					} catch (Exception e) {
					}
					if (bordroTipi != null)
						izinGrupMap.put(tanim.getErpKodu(), bordroTipi.value());
				}
			}
		}
		Calendar cal = Calendar.getInstance();
		// Date bugun = PdksUtil.getDate(cal.getTime());
		Date tarih = PdksUtil.convertToJavaDate(donemStr + "01", "yyyyMMdd");
		cal.setTime(tarih);
		int ayGunSayisi = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		TreeMap<Long, AylikPuantaj> puantajMap = new TreeMap<Long, AylikPuantaj>();
		for (AylikPuantaj ap : puantajList) {
			PersonelDenklestirme personelDenklestirme = ap.getPersonelDenklestirmeAylik();
			if (personelDenklestirme.getId() != null)
				puantajMap.put(personelDenklestirme.getId(), ap);
		}
		if (!puantajMap.isEmpty()) {
			fields.clear();
			fields.put("personelDenklestirme.id", new ArrayList(puantajMap.keySet()));
			fields.put(PdksEntityController.MAP_KEY_MAP, "getPersonelDenklestirmeId");
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			TreeMap<Long, PersonelDenklestirmeBordro> bordroMap = pdksEntityController.getObjectByInnerObjectMap(fields, PersonelDenklestirmeBordro.class, false);
			TreeMap<String, PersonelDenklestirmeBordroDetay> bordroDetayMap = null;
			List<Long> idList = new ArrayList<Long>();
			if (!bordroMap.isEmpty()) {
				for (Long key : bordroMap.keySet())
					idList.add(bordroMap.get(key).getId());

				fields.clear();
				fields.put("personelDenklestirmeBordro.id", idList);
				fields.put(PdksEntityController.MAP_KEY_MAP, "getDetayKey");
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				bordroDetayMap = pdksEntityController.getObjectByInnerObjectMap(fields, PersonelDenklestirmeBordroDetay.class, false);
			} else
				bordroDetayMap = new TreeMap<String, PersonelDenklestirmeBordroDetay>();

			for (AylikPuantaj ap : puantajList) {
				PersonelDenklestirme personelDenklestirme = ap.getPersonelDenklestirmeAylik();
				if (!(personelDenklestirme.getDurum() || fazlaMesaiHesapla == false))
					continue;
				boolean flush = false;
				CalismaModeli calismaModeli = personelDenklestirme.getCalismaModeli();
				try {
					for (VardiyaHafta vardiyaHafta : ap.getVardiyaHaftaList()) {
						List<VardiyaGun> gunler = vardiyaHafta.getVardiyaGunler();
						if (gunler != null) {
							VardiyaGun sonGun = null, haftaTatilGun = null;
							for (VardiyaGun vardiyaGun : gunler) {
								if (vardiyaGun.getVardiya() != null) {
									sonGun = vardiyaGun;
									if (vardiyaGun.getVardiya().isHaftaTatil() && vardiyaGun.getCalismaSuresi() == 0.0)
										haftaTatilGun = vardiyaGun;
								}
							}
							if (haftaTatilGun != null && sonGun != null && sonGun.getIzin() != null && sonGun.getIzin().getIzinTipi().isTakvimGunuMu() == false) {
								if (sonGun.getCalismaSuresi() == 0.0d && !haftaTatilGun.getId().equals(sonGun.getId())) {
									//

								}

							}
						}

					}
				} catch (Exception ex) {
					logger.error(ex);
					ex.printStackTrace();
				}

				double normalGunAdet = 0.0d, haftaTatilAdet = 0.0d, resmiTatilAdet = 0.0d, artikAdet = 0.0d, izinGunAdet = 0.0d;
				double normalSaat = 0.0d, resmiTatilSaat = 0.0d, haftaTatilSaat = 0.0d, izinGunSaat = 0.0d;
				LinkedHashMap<BordroDetayTipi, Double> detayMap = new LinkedHashMap<BordroDetayTipi, Double>();
				boolean saatlikCalisma = calismaModeli.isSaatlikOdeme();
				Double gunlukKatsayi = null;
				for (VardiyaGun vardiyaGun : ap.getVardiyalar()) {
					if (vardiyaGun.isAyinGunu() && vardiyaGun.getVardiya() != null) {
						if (gunlukKatsayi == null) {
							gunlukKatsayi = vardiyaGun.getSaatCalisanGunlukKatsayisi();
							if (gunlukKatsayi == null || gunlukKatsayi.doubleValue() < 7.5d)
								gunlukKatsayi = gunlukKatsayi.doubleValue();
						}

						Vardiya vardiya = vardiyaGun.getVardiya();
						boolean haftaTatil = vardiya.isHaftaTatil();
						Tatil tatil = vardiyaGun.getTatil();
						boolean resmiTatil = tatil != null;
						double calismaGun = 1.0d;
						if (vardiyaGun.isIzinli()) {
							// calisiyor = true;
							String izinKodu = null;
							double artiGun = 1.0d;
							// if (saatlikCalisma)
							// artiGun = vardiyaGun.getSaatCalisanIzinGunKatsayisi();
							if (vardiyaGun.getIzin() != null) {
								IzinTipi izinTipi = vardiyaGun.getIzin().getIzinTipi();
								if (izinTipi != null) {
									if (izinTipi.isUcretsizIzinTipi())
										calismaGun = 0;
									else if (saatlikCalisma) {
										if (resmiTatil == false) {
											Double sure = ortakIslemler.getVardiyaIzinSuresi(vardiyaGun.getSaatCalisanIzinGunKatsayisi(), vardiyaGun, personelDenklestirme, vardiyaGun.getVardiyaDate());
											if (sure > 0.0d)
												izinGunSaat += sure;
										}
										// if (vardiyaGun.isHaftaIci())
										// izinGunSaat += resmiTatil == false ? vardiyaGun.getSaatCalisanIzinGunKatsayisi() : 0;
									}
								}

								if (izinTipi.getTakvimGunumu() == false) {
									if (vardiyaGun.getVardiya().isOffGun()) {
										if (izinTipi.isOffDahilMi() == false)
											artiGun = 0.0d;
									} else if (haftaTatil) {
										if (izinTipi.isHTDahil())
											artiGun = 0.0d;

									}
									if (izinTipi.getUcretli())
										izinGunAdet += 1;

								}
								izinKodu = izinTipi.getIzinTipiTanim().getErpKodu();

								if (!izinGrupMap.containsKey(izinKodu))
									izinKodu = null;
								else
									izinKodu = izinGrupMap.get(izinKodu);

							} else
								izinKodu = vardiya.getStyleClass();
							if (artiGun > 0.0d && izinKodu != null) {
								BordroDetayTipi bordroTipi = null;
								try {
									if (PdksUtil.hasStringValue(izinKodu))
										bordroTipi = BordroDetayTipi.fromValue(izinKodu);
								} catch (Exception e) {
								}
								if (bordroTipi != null) {
									double miktar = (detayMap.containsKey(bordroTipi) ? detayMap.get(bordroTipi) : 0.0d) + artiGun;
									detayMap.put(bordroTipi, miktar);
									if (bordroTipi.equals(BordroDetayTipi.RAPORLU_IZIN) || bordroTipi.equals(BordroDetayTipi.UCRETSIZ_IZIN))
										calismaGun = 0;
								}
							}

						}
						if (calismaGun > 0.0d) {
							if (resmiTatil) {
								if (!tatil.isYarimGunMu()) {
									resmiTatilAdet += calismaGun;
									calismaGun = 0;
								} else if (PdksUtil.hasStringValue(arifeGunuBordroYarim)) {
									calismaGun = 0.5d;
									resmiTatilAdet += calismaGun;
								}
							}
							if (!haftaTatil)
								normalGunAdet += calismaGun;
							else {
								haftaTatilAdet += calismaGun;
							}
							// logger.info(vardiyaGun.getVardiyaDateStr() + " " + (normalGunAdet + haftaTatilAdet + resmiTatilAdet));
						}

						if (vardiyaGun.isIzinli() == false) {
							if (resmiTatil) {
								if (!tatil.isYarimGunMu())
									resmiTatilSaat += vardiyaGun.getSaatCalisanResmiTatilKatsayisi();
								else {
									resmiTatilSaat += vardiyaGun.getSaatCalisanArifeTatilKatsayisi();
									normalSaat += vardiyaGun.getSaatCalisanArifeNormalKatsayisi();
								}
							} else if (haftaTatil)
								haftaTatilSaat += vardiyaGun.getSaatCalisanHaftaTatilKatsayisi();
							else if (vardiyaGun.isHaftaIci()) {
								normalSaat += vardiyaGun.getSaatCalisanNormalGunKatsayisi();
								logger.debug(vardiyaGun.getVardiyaDateStr() + " " + normalGunAdet);
							}

						}
					}

				}

				double toplamAdet = normalGunAdet + haftaTatilAdet + resmiTatilAdet;
				double toplamSaatAdet = saatlikCalisma ? normalSaat + haftaTatilSaat + resmiTatilSaat + izinGunSaat : 0;
				double normalCalisma = ap.getSaatToplami() > ap.getPlanlananSure() ? ap.getPlanlananSure() : ap.getSaatToplami();
				if ((saatlikCalisma == false && toplamAdet > 0) || (saatlikCalisma && toplamSaatAdet > 0)) {
					if (ayGunSayisi == toplamAdet)
						normalGunAdet += 30 - ayGunSayisi;
					if (toplamAdet > 30) {
						artikAdet = 1;
					}

					PersonelDenklestirmeBordro denklestirmeBordro = bordroMap.containsKey(personelDenklestirme.getId()) ? bordroMap.get(personelDenklestirme.getId()) : null;
					if (denklestirmeBordro == null) {
						denklestirmeBordro = new PersonelDenklestirmeBordro();
						denklestirmeBordro.setPersonelDenklestirme(personelDenklestirme);
					}
					Long denklestirmeBordroId = denklestirmeBordro.getId();

					denklestirmeBordro.setGuncellendi(denklestirmeBordro.getId() == null);
					if (!saatlikCalisma) {
						normalCalisma = 0.0d;
						resmiTatilSaat = 0.0d;
						haftaTatilSaat = 0.0d;
						izinGunSaat = 0.0d;
						normalSaat = 0.0d;
					} else {
						normalGunAdet = normalCalisma / gunlukKatsayi;
						resmiTatilAdet = resmiTatilSaat / gunlukKatsayi;
						haftaTatilAdet = haftaTatilSaat / gunlukKatsayi;
						artikAdet = 0;

					}
					if (izinGunAdet > 0) {
						detayMap.put(BordroDetayTipi.IZIN_GUN, izinGunAdet);
						if (calismaModeli.isSaatlikOdeme() == false)
							normalGunAdet -= izinGunAdet;
					}

					if (normalCalisma != 0)
						detayMap.put(BordroDetayTipi.SAAT_NORMAL, normalCalisma);
					if (resmiTatilSaat > 0)
						detayMap.put(BordroDetayTipi.SAAT_RESMI_TATIL, resmiTatilSaat);
					if (haftaTatilSaat > 0)
						detayMap.put(BordroDetayTipi.SAAT_HAFTA_TATIL, haftaTatilSaat);
					if (izinGunSaat > 0)
						detayMap.put(BordroDetayTipi.SAAT_IZIN, izinGunSaat);
					if (!kaydet)
						denklestirmeBordro = (PersonelDenklestirmeBordro) denklestirmeBordro.cloneEmpty();
					denklestirmeBordro.setNormalGunAdet(normalGunAdet);
					denklestirmeBordro.setHaftaTatilAdet(haftaTatilAdet);
					denklestirmeBordro.setResmiTatilAdet(resmiTatilAdet);
					denklestirmeBordro.setArtikAdet(artikAdet);
					if (kaydet) {
						if (denklestirmeBordro.isGuncellendi()) {
							pdksEntityController.saveOrUpdate(session, entityManager, denklestirmeBordro);
							flush = true;
						}

					}
					ap.setDenklestirmeBordro(denklestirmeBordro);

					HashMap<BordroDetayTipi, PersonelDenklestirmeBordroDetay> detayMap1 = new HashMap<BordroDetayTipi, PersonelDenklestirmeBordroDetay>();
					denklestirmeBordro.setDetayMap(detayMap1);
					if (kaydet || fazlaMesaiHesapla) {
						if (!detayMap.isEmpty()) {
							for (BordroDetayTipi bordroDetayTipi : detayMap.keySet()) {
								PersonelDenklestirmeBordroDetay bordroDetay = null;
								String detayKey = PersonelDenklestirmeBordroDetay.getDetayKey(denklestirmeBordro, bordroDetayTipi.value());
								if (bordroDetayMap.containsKey(detayKey)) {
									bordroDetay = bordroDetayMap.get(detayKey);
									bordroDetayMap.remove(detayKey);
								} else {
									bordroDetay = new PersonelDenklestirmeBordroDetay(denklestirmeBordro, bordroDetayTipi);
								}
								if (!kaydet)
									bordroDetay = (PersonelDenklestirmeBordroDetay) bordroDetay.cloneEmpty();
								detayMap1.put(bordroDetayTipi, bordroDetay);
								bordroDetay.setGuncellendi(bordroDetay.getId() == null);
								bordroDetay.setMiktar(detayMap.get(bordroDetayTipi));
								if (kaydet) {
									if (bordroDetay.isGuncellendi()) {
										pdksEntityController.saveOrUpdate(session, entityManager, bordroDetay);
										flush = true;
									}
								}
							}
							if (calismaModeli.isFazlaMesaiVarMi()) {
								if (ap.getGecenAyFazlaMesai(authenticatedUser) != 0)
									baslikMap.put(ortakIslemler.devredenMesaiKod(), Boolean.TRUE);
								if (ap.getFazlaMesaiSure() > 0)
									baslikMap.put(ortakIslemler.ucretiOdenenKod(), Boolean.TRUE);
								if (ap.getDevredenSure() != 0)
									baslikMap.put(ortakIslemler.devredenBakiyeKod(), Boolean.TRUE);
								if (ap.getAylikNetFazlaMesai() != 0)
									baslikMap.put(ortakIslemler.gerceklesenMesaiKod(), Boolean.TRUE);
							}
						}
					} else if (denklestirmeBordroId != null) {
						for (String key : bordroDetayMap.keySet()) {
							PersonelDenklestirmeBordroDetay bordroDetay = bordroDetayMap.get(key);
							if (bordroDetay.getPersonelDenklestirmeBordro().getId().equals(denklestirmeBordroId)) {
								detayMap1.put(bordroDetay.getBordroDetayTipi(), bordroDetay);
							}
						}
					}

				}

				detayMap = null;
				if (flush)
					session.flush();
				if (saatlikCalismaVar) {
					String keyEk = saatlikCalisma ? "" : "G";
					baslikGuncelle(baslikMap, ortakIslemler.normalCalismaSaatKod() + keyEk, normalSaat);
					baslikGuncelle(baslikMap, ortakIslemler.haftaTatilCalismaSaatKod() + keyEk, haftaTatilSaat);
					baslikGuncelle(baslikMap, ortakIslemler.resmiTatilCalismaSaatKod() + keyEk, resmiTatilSaat);
					baslikGuncelle(baslikMap, ortakIslemler.izinSureSaatKod() + keyEk, izinGunSaat);
					baslikGuncelle(baslikMap, ortakIslemler.izinSureGunAdetKod(), izinGunAdet);
				}
				if (ap.getDenklestirmeBordro() != null) {
					baslikGuncelle(baslikMap, ortakIslemler.ucretliIzinGunKod(), ap.getDenklestirmeBordro().getUcretliIzin().doubleValue());
					baslikGuncelle(baslikMap, ortakIslemler.ucretsizIzinGunKod(), ap.getDenklestirmeBordro().getUcretsizIzin().doubleValue());
					baslikGuncelle(baslikMap, ortakIslemler.hastalikIzinGunKod(), ap.getDenklestirmeBordro().getRaporluIzin().doubleValue());
					baslikGuncelle(baslikMap, ortakIslemler.normalGunKod(), ap.getDenklestirmeBordro().getNormalGunAdet());
					baslikGuncelle(baslikMap, ortakIslemler.haftaTatilGunKod(), ap.getDenklestirmeBordro().getHaftaTatilAdet());
					baslikGuncelle(baslikMap, ortakIslemler.artikGunKod(), ap.getDenklestirmeBordro().getArtikAdet());
					baslikGuncelle(baslikMap, ortakIslemler.resmiTatilGunKod(), ap.getDenklestirmeBordro().getResmiTatilAdet());
					baslikGuncelle(baslikMap, ortakIslemler.bordroToplamGunKod(), ap.getDenklestirmeBordro().getBordroToplamGunAdet());
				}

			}
			if (kaydet && !bordroDetayMap.isEmpty()) {
				for (String key : bordroDetayMap.keySet()) {
					session.delete(bordroDetayMap.get(key));
				}
				session.flush();
			}
			bordroDetayMap = null;
			bordroMap = null;
		}
		return baslikMap;
	}

	/**
	 * @param baslikMap
	 * @param key
	 * @param deger
	 */
	private void baslikGuncelle(TreeMap<String, Boolean> baslikMap, String key, Double deger) {
		if (!baslikMap.containsKey(key) && deger != null && deger.doubleValue() != 0)
			baslikMap.put(key, Boolean.TRUE);
	}

	/**
	 * @param talepId
	 * @param session
	 */
	public Long fazlaMesaiOtomatikHareketSil(Long talepId, Session session) {
		LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
		Long sonuc = null;
		try {
			if (talepId != null) {
				StringBuffer sp = new StringBuffer("SP_DELETE_FAZLA_MESAI_TALEP_HAREKET_DATA");
				veriMap.put("talepId", talepId);
				if (session != null)
					veriMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				List list = pdksEntityController.execSPList(veriMap, sp, null);
				if (list != null && !list.isEmpty())
					sonuc = ((BigInteger) list.get(0)).longValue();
			}

		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		veriMap = null;
		return sonuc;
	}

	/**
	 * @param yil
	 * @param ay
	 * @param ardisik
	 * @param bolumId
	 * @param ardisikDurum
	 * @param idMap
	 * @param session
	 * @return
	 */
	public List<AylikPuantaj> getAylikFazlaMesaiKontrol(int yil, int ay, Integer ardisik, Long bolumId, boolean ardisikDurum, TreeMap<Long, Personel> idMap, Session session) {
		List<AylikPuantaj> aylikPuantajList = new ArrayList<AylikPuantaj>();
		HashMap fields = new HashMap();
		fields.put("ay<=", ay);
		fields.put("yil=", yil);
		fields.put(PdksEntityController.MAP_KEY_MAP, "getAy");
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		TreeMap<Integer, DenklestirmeAy> donemMap = pdksEntityController.getObjectByInnerObjectMapInLogic(fields, DenklestirmeAy.class, false);
		if (!donemMap.isEmpty()) {
			String maxCalismaSaatStr = ortakIslemler.getParameterKey("maxCalismaSaat");

			Double maxCalismaSaat = null;
			try {
				if (PdksUtil.hasStringValue(maxCalismaSaatStr))
					maxCalismaSaat = Double.parseDouble(maxCalismaSaatStr);
			} catch (Exception e) {
			}
			if (maxCalismaSaat == null || maxCalismaSaat.doubleValue() < 0.0d)
				maxCalismaSaatStr = "D." + DenklestirmeAy.COLUMN_NAME_GUN_MAX_CALISMA_SURESI;

			StringBuffer sb = new StringBuffer();
			sb.append("SP_GET_FAZLA_MESAI_KONTROL");
			LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<String, Object>();
			linkedHashMap.put("perList", ortakIslemler.getListIdStr(new ArrayList(idMap.keySet())));
			linkedHashMap.put("yil", String.valueOf(yil));
			linkedHashMap.put("ay", String.valueOf(ay));
			linkedHashMap.put("ardisik", String.valueOf(ardisikDurum ? ardisik : 32));
			linkedHashMap.put("ardisikDurum", ardisikDurum ? 1 : 0);
			linkedHashMap.put("maxCalismaSaatStr", maxCalismaSaatStr);

			if (session != null)
				linkedHashMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Object[]> alanList = null;
			try {
				alanList = pdksEntityController.execSPList(linkedHashMap, sb, null);
				// if (session != null)
				// fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				// alanList = pdksEntityController.getObjectBySQLList(sb, fields, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			ardisikDurum = false;
			if (alanList != null && !alanList.isEmpty()) {
				TreeMap<String, Liste> map = new TreeMap<String, Liste>();
				for (Object[] objects : alanList) {
					if (objects != null) {
						Long id = ((BigDecimal) objects[0]).longValue();
						Integer donemAy = (Integer) objects[1];
						Long perId = ((BigDecimal) objects[3]).longValue();
						if (donemMap.containsKey(donemAy) && idMap.containsKey(perId)) {
							boolean ardisikDeger = ((Integer) objects[4]) > 0;
							Integer limit = (Integer) objects[5];
							Double fazlaMesai = (Double) objects[6];
							DenklestirmeAy da = donemMap.get(donemAy);
							AylikPuantaj aylikPuantaj = new AylikPuantaj(da);
							Personel personel = idMap.get(perId);
							aylikPuantaj.setPdksPersonel(personel);
							aylikPuantaj.setPersonelDenklestirmeAylik(new PersonelDenklestirme(personel, da, null));
							aylikPuantaj.getPersonelDenklestirmeAylik().setId(id);
							aylikPuantaj.setAyrikHareketVar(ardisikDeger);
							if (!ardisikDurum)
								ardisikDurum = ardisikDeger;
							aylikPuantaj.setFazlaMesaiSure(fazlaMesai);
							aylikPuantaj.setGunSayisi(limit);
							String key = (bolumId == null && personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "") + (personel.getYoneticisi() != null ? personel.getYoneticisi().getAdSoyad() : "") + personel.getAdSoyad() + "" + personel.getPdksSicilNo();
							List<AylikPuantaj> puantajList = null;
							Liste liste = null;

							if (!map.containsKey(key)) {
								puantajList = new ArrayList<AylikPuantaj>();
								liste = new Liste(key, puantajList);
								map.put(key, liste);
							} else {
								liste = map.get(key);
								puantajList = (List<AylikPuantaj>) liste.getValue();
							}
							puantajList.add(aylikPuantaj);

						}
					}
				}
				if (!map.isEmpty()) {
					List<Liste> puantajList = PdksUtil.sortObjectStringAlanList(new ArrayList(map.values()), "getId", null);
					for (Liste liste : puantajList) {
						List<AylikPuantaj> pl = (List<AylikPuantaj>) liste.getValue();
						aylikPuantajList.addAll(pl);
						pl = null;
					}
				}
				map = null;
			}
			if (!ardisikDurum)
				ardisik = null;

		}
		return aylikPuantajList;
	}

	/**
	 * @param puantajList
	 * @param izinTipiVardiyaList
	 * @param izinTipiPersonelVardiyaMap
	 */
	public void personelIzinAdetleriOlustur(List<AylikPuantaj> puantajList, List<Vardiya> izinTipiVardiyaList, TreeMap<String, TreeMap<String, List<VardiyaGun>>> izinTipiPersonelVardiyaMap) {
		if (puantajList != null) {
			for (AylikPuantaj aylikPuantaj : puantajList) {
				TreeMap<String, Integer> izinAdetMap = new TreeMap<String, Integer>();
				aylikPuantaj.setIzinAdetMap(izinAdetMap);
				Personel personel = aylikPuantaj.getPdksPersonel();
				List<Integer> izinAdetList = new ArrayList<Integer>();
				for (Vardiya vardiya : izinTipiVardiyaList) {
					Integer adet = getVardiyaAdet(izinTipiPersonelVardiyaMap, personel, vardiya);
					izinAdetMap.put(vardiya.getKisaAdi(), adet);
					izinAdetList.add(adet);
				}
				aylikPuantaj.setIzinAdetList(izinAdetList);
			}
		}
	}

	/**
	 * @param yil
	 * @param ay
	 * @param bolumId
	 * @param puantajList
	 * @param session
	 * @return
	 */
	public List<String> getFazlaMesaiUyari(int yil, int ay, Long bolumId, List<AylikPuantaj> puantajList, Session session) {
		List<String> list = new ArrayList<String>();
		try {
			String maxToplamMesaiStr = ortakIslemler.getParameterKey("maxToplamMesai");
			if (PdksUtil.hasStringValue(maxToplamMesaiStr)) {
				Double maxToplamMesai = null;
				try {
					maxToplamMesai = Double.parseDouble(maxToplamMesaiStr);
				} catch (Exception e) {
					maxToplamMesai = null;
				}

				if (maxToplamMesai != null && maxToplamMesai.doubleValue() > 0.0d) {
					TreeMap<Long, Double> maxToplamMesaiMap = new TreeMap<Long, Double>();
					TreeMap<Long, AylikPuantaj> pIdList = new TreeMap<Long, AylikPuantaj>();
					TreeMap<Long, Personel> perIdMap = new TreeMap<Long, Personel>();
					for (AylikPuantaj ap : puantajList) {
						Personel per = ap.getPdksPersonel();
						if (per != null) {
							pIdList.put(per.getId(), ap);
							maxToplamMesaiMap.put(per.getId(), 0.0d);
							perIdMap.put(per.getId(), per);

						}
					}
					if (!perIdMap.isEmpty()) {
						List<AylikPuantaj> aylikPuantajList = getAylikFazlaMesaiKontrol(yil, ay, null, bolumId, false, perIdMap, session);
						for (AylikPuantaj aylikPuantaj : aylikPuantajList) {
							if (aylikPuantaj != null && aylikPuantaj.getFazlaMesaiSure() > 0.0d) {
								Personel per = aylikPuantaj.getPdksPersonel();
								double maxToplamMesaiSaat = maxToplamMesaiMap.get(per.getId()) + aylikPuantaj.getFazlaMesaiSure();
								maxToplamMesaiMap.put(per.getId(), maxToplamMesaiSaat);
							}
						}
						for (AylikPuantaj ap : puantajList) {
							double mesai = maxToplamMesaiMap.get(ap.getPdksPersonel().getId());
							if (mesai >= maxToplamMesai) {
								Personel personel = ap.getPdksPersonel();
								String str = (puantajList.size() > 1 ? personel.getPdksSicilNo() + " " : "") + personel.getAdSoyad() + " toplam " + authenticatedUser.sayiFormatliGoster(mesai) + "  saat mesaisi oluşmuştur.";
								list.add(str);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		return list;
	}

	/**
	 * @param vardiyaGun
	 * @return
	 */
	public String getFazlaMesaiSaatleri(VardiyaGun vardiyaGun) {
		StringBuffer sb = new StringBuffer();
		if (vardiyaGun != null && vardiyaGun.getIslemVardiya() != null && vardiyaGun.getVersion() >= 0) {
			String pattern = PdksUtil.getDateTimeFormat();
			Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
			List<String> list = new ArrayList<String>();
			try {
				if (islemVardiya.isCalisma()) {
					if (islemVardiya.getVardiyaTelorans1BasZaman() != null && islemVardiya.getVardiyaFazlaMesaiBasZaman() != null && islemVardiya.getVardiyaBasZaman().getTime() > vardiyaGun.getVardiyaDate().getTime()
							&& islemVardiya.getVardiyaTelorans1BasZaman().getTime() != islemVardiya.getVardiyaFazlaMesaiBasZaman().getTime())
						if (!PdksUtil.convertToDateString(islemVardiya.getVardiyaFazlaMesaiBasZaman(), pattern).equals(PdksUtil.convertToDateString(islemVardiya.getVardiyaBasZaman(), pattern)))
							list.add(PdksUtil.convertToDateString(islemVardiya.getVardiyaFazlaMesaiBasZaman(), pattern) + " - " + PdksUtil.convertToDateString(islemVardiya.getVardiyaBasZaman(), pattern));
					if (islemVardiya.getVardiyaTelorans2BitZaman() != null && islemVardiya.getVardiyaFazlaMesaiBitZaman() != null && islemVardiya.getVardiyaTelorans2BitZaman().getTime() != islemVardiya.getVardiyaFazlaMesaiBitZaman().getTime())
						if (!PdksUtil.convertToDateString(islemVardiya.getVardiyaBitZaman(), pattern).equals(PdksUtil.convertToDateString(islemVardiya.getVardiyaFazlaMesaiBitZaman(), pattern)))
							list.add(PdksUtil.convertToDateString(islemVardiya.getVardiyaBitZaman(), pattern) + " - " + PdksUtil.convertToDateString(islemVardiya.getVardiyaFazlaMesaiBitZaman(), pattern));
				} else if (islemVardiya.getVardiyaFazlaMesaiBasZaman() != null && islemVardiya.getVardiyaFazlaMesaiBitZaman() != null)
					if (!PdksUtil.convertToDateString(islemVardiya.getVardiyaFazlaMesaiBasZaman(), pattern).equals(PdksUtil.convertToDateString(islemVardiya.getVardiyaFazlaMesaiBitZaman(), pattern)))
						list.add(PdksUtil.convertToDateString(islemVardiya.getVardiyaFazlaMesaiBasZaman(), pattern) + " - " + PdksUtil.convertToDateString(islemVardiya.getVardiyaFazlaMesaiBitZaman(), pattern));
			} catch (Exception e) {
				logger.error(authenticatedUser.getAdSoyad() + " " + authenticatedUser.getCalistigiSayfa() + " " + vardiyaGun.getVardiyaKeyStr() + " --> " + e);
			}
			if (!list.isEmpty()) {
				sb.append("<p align=\"left\">");
				if (islemVardiya.isCalisma() && (authenticatedUser.isAdmin() || authenticatedUser.isIK() || authenticatedUser.isSistemYoneticisi()))
					sb.append("<B>Telorans Aralık : <B>" + PdksUtil.convertToDateString(islemVardiya.getVardiyaTelorans1BasZaman(), pattern) + " - " + PdksUtil.convertToDateString(islemVardiya.getVardiyaTelorans2BitZaman(), pattern) + "</BR>");

				sb.append("<B>Fazla Çalışma Saat : </B>");
				if (list.size() > 1) {
					sb.append("<OL>");
					for (String string : list) {
						sb.append("<LI style=\"text-align: left;\">" + string + "</LI>");
					}
					sb.append("</OL>");
				} else
					sb.append(list.get(0));
				sb.append("</p>");
			}
			if (islemVardiya.isCalisma()) {
				Tatil tatil = vardiyaGun.getTatil();
				if (tatil != null && (authenticatedUser.isIK() || authenticatedUser.isSistemYoneticisi() || authenticatedUser.isAdmin())) {
					if (tatil.isYarimGunMu()) {
						Date arifeGun = tatil.getBasTarih();
						if (PdksUtil.tarihKarsilastirNumeric(arifeGun, vardiyaGun.getVardiyaDate()) == 0) {
							sb.append("<p align=\"left\">");
							sb.append("<B>Arife Başlama Saati : </B>" + authenticatedUser.getTarihFormatla(arifeGun, authenticatedUser.getDateTimeFormat()));
							sb.append("</p>");
						}
					}

				}
			}
			list = null;
		}
		String str = sb.toString();
		sb = null;
		return str;
	}

	/**
	 * @param denklestirmeAy
	 * @param user
	 * @return
	 */
	public boolean getDurum(DenklestirmeAy denklestirmeAy, User user) {
		boolean durum = false;
		if (denklestirmeAy != null) {
			if (user == null)
				user = authenticatedUser;
			boolean adminRole = user.isAdmin() || user.isSistemYoneticisi() || user.isIKAdmin();
			if (adminRole)
				durum = denklestirmeAy.getDurum() || denklestirmeAy.getGuncelleIK();
			else
				durum = denklestirmeAy.getDurum();
		}
		return durum;
	}

	/**
	 * @param denklestirmeAy
	 * @return
	 */
	public boolean getDurum(DenklestirmeAy denklestirmeAy) {
		boolean durum = getDurum(denklestirmeAy, authenticatedUser);
		return durum;
	}

	/**
	 * @param departmanId
	 * @param aylikPuantaj
	 * @param denklestirme
	 * @param session
	 * @return
	 */
	public List<SelectItem> getFazlaMesaiDepartmanList(AylikPuantaj aylikPuantaj, boolean denklestirme, Session session) {
		List<Departman> list = ortakIslemler.getFazlaMesaiList(authenticatedUser, null, null, null, null, null, aylikPuantaj, "D", denklestirme, session);
		List<SelectItem> selectList = new ArrayList<SelectItem>();
		if (!list.isEmpty()) {
			list = PdksUtil.sortObjectStringAlanList(list, "getAciklama", null);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Departman veri = (Departman) iterator.next();
				selectList.add(new SelectItem(veri.getId(), veri.getAciklama()));
			}
		}
		return selectList;
	}

	/**
	 * @param departmanId
	 * @param aylikPuantaj
	 * @param denklestirme
	 * @param session
	 * @return
	 */
	public List<SelectItem> getFazlaMesaiSirketList(Long departmanId, AylikPuantaj aylikPuantaj, boolean denklestirme, Session session) {
		List<Sirket> list = ortakIslemler.getFazlaMesaiList(authenticatedUser, departmanId, null, null, null, null, aylikPuantaj, "S", denklestirme, session);
		List<SelectItem> selectList = new ArrayList<SelectItem>();
		if (!list.isEmpty()) {
			list = PdksUtil.sortObjectStringAlanList(list, "getAd", null);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Sirket veri = (Sirket) iterator.next();
				selectList.add(new SelectItem(veri.getId(), veri.getAd()));
			}
		}
		return selectList;
	}

	/**
	 * @param sirket
	 * @param aylikPuantaj
	 * @param denklestirme
	 * @param session
	 * @return
	 */
	public List<SelectItem> getFazlaMesaiTesisList(Sirket sirket, AylikPuantaj aylikPuantaj, boolean denklestirme, Session session) {
		List<Tanim> list = null;
		if (sirket != null && sirket.isTesisDurumu())
			list = ortakIslemler.getFazlaMesaiList(authenticatedUser, null, sirket, null, null, null, aylikPuantaj, "T", denklestirme, session);

		List<SelectItem> selectList = new ArrayList<SelectItem>();
		if (list != null && !list.isEmpty()) {
			list = PdksUtil.sortObjectStringAlanList(list, "getAciklama", null);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Tanim veri = (Tanim) iterator.next();
				selectList.add(new SelectItem(veri.getId(), veri.getAciklama()));
			}
		}

		return selectList;
	}

	/**
	 * @param sirket
	 * @param tesisId
	 * @param aylikPuantaj
	 * @param denklestirme
	 * @param session
	 * @return
	 */
	public List<SelectItem> getFazlaMesaiBolumList(Sirket sirket, String tesisId, AylikPuantaj aylikPuantaj, boolean denklestirme, Session session) {
		List<Tanim> list = ortakIslemler.getFazlaMesaiList(authenticatedUser, null, sirket, tesisId, null, null, aylikPuantaj, "B", denklestirme, session);
		List<SelectItem> selectList = new ArrayList<SelectItem>();
		if (!list.isEmpty()) {
			list = PdksUtil.sortObjectStringAlanList(list, "getAciklama", null);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Tanim veri = (Tanim) iterator.next();
				selectList.add(new SelectItem(veri.getId(), veri.getAciklama()));
			}
		}
		return selectList;
	}

	/**
	 * @param sirket
	 * @param tesisId
	 * @param bolumId
	 * @param aylikPuantaj
	 * @param denklestirme
	 * @param session
	 * @return
	 */
	public List<SelectItem> getFazlaMesaiAltBolumList(Sirket sirket, String tesisId, Long bolumId, AylikPuantaj aylikPuantaj, boolean denklestirme, Session session) {
		List<Tanim> list = ortakIslemler.getFazlaMesaiList(authenticatedUser, null, sirket, tesisId, bolumId, null, aylikPuantaj, "AB", denklestirme, session);
		List<SelectItem> selectList = new ArrayList<SelectItem>();
		if (!list.isEmpty()) {
			list = PdksUtil.sortObjectStringAlanList(list, "getAciklama", null);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Tanim veri = (Tanim) iterator.next();
				selectList.add(new SelectItem(veri.getId(), veri.getAciklama()));
			}
		}
		return selectList;
	}

	/**
	 * @param tarih
	 * @param list
	 * @param sirketId
	 * @param tesisId
	 * @param ekSaha3Id
	 * @param ekSaha4Id
	 * @param session
	 */
	public void setFazlaMesaiPersonel(Date tarih, List<String> list, Long sirketId, Long tesisId, Long ekSaha3Id, Long ekSaha4Id, Session session) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(tarih);
		HashMap parametreMap = new HashMap();
		parametreMap.put("yil", cal.get(Calendar.YEAR));
		parametreMap.put("ay", cal.get(Calendar.MONTH) + 1);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		DenklestirmeAy denklestirmeAyDonem = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(parametreMap, DenklestirmeAy.class);
		Sirket sirket = null;
		if (sirketId != null) {
			parametreMap.clear();
			parametreMap.put("id", sirketId);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			sirket = (Sirket) pdksEntityController.getObjectByInnerObject(parametreMap, Sirket.class);
		}
		List<Personel> donemPerList = getFazlaMesaiPersonelList(sirket, tesisId != null ? String.valueOf(tesisId) : null, ekSaha3Id, ekSaha4Id, denklestirmeAyDonem != null ? new AylikPuantaj(denklestirmeAyDonem) : null, false, session);
		List<String> searchSicilNoList = new ArrayList<String>(list);
		list.clear();
		if (donemPerList != null) {
			for (Personel personel : donemPerList) {
				if (searchSicilNoList.contains(personel.getPdksSicilNo()))
					if (personel.isCalisiyorGun(tarih))
						list.add(personel.getPdksSicilNo());
			}
		}
		searchSicilNoList = null;
	}

	/**
	 * @param sirket
	 * @param tesisId
	 * @param bolumId
	 * @param altBolumId
	 * @param aylikPuantaj
	 * @param denklestirme
	 * @param session
	 * @return
	 */
	public List<Personel> getFazlaMesaiPersonelList(Sirket sirket, String tesisId, Long bolumId, Long altBolumId, AylikPuantaj aylikPuantaj, boolean denklestirme, Session session) {
		List<Personel> list = ortakIslemler.getFazlaMesaiList(authenticatedUser, null, sirket, tesisId, bolumId, altBolumId, aylikPuantaj, "P", denklestirme, session);
		if (!list.isEmpty())
			list = PdksUtil.sortObjectStringAlanList(list, "getAdSoyad", null);

		return list;
	}

	/**
	 * @param departmanId
	 * @param aylikPuantaj
	 * @param denklestirme
	 * @param fazlaMesaiTalepDurum
	 * @param session
	 * @return
	 */
	public List<SelectItem> getFazlaMesaiMudurSirketList(Long departmanId, AylikPuantaj aylikPuantaj, boolean denklestirme, boolean fazlaMesaiTalepDurum, Session session) {
		List<Sirket> list = ortakIslemler.getFazlaMesaiMudurList(authenticatedUser, departmanId, null, null, null, aylikPuantaj, "S", denklestirme, fazlaMesaiTalepDurum, session);
		List<SelectItem> selectList = new ArrayList<SelectItem>();
		if (!list.isEmpty()) {
			list = PdksUtil.sortObjectStringAlanList(list, "getAd", null);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Sirket veri = (Sirket) iterator.next();
				selectList.add(new SelectItem(veri.getId(), veri.getAd()));
			}
		}
		return selectList;
	}

	/**
	 * @param sirket
	 * @param aylikPuantaj
	 * @param denklestirme
	 * @param fazlaMesaiTalepDurum
	 * @param session
	 * @return
	 */
	public List<SelectItem> getFazlaMesaiMudurTesisList(Sirket sirket, AylikPuantaj aylikPuantaj, boolean denklestirme, boolean fazlaMesaiTalepDurum, Session session) {
		List<Tanim> list = ortakIslemler.getFazlaMesaiMudurList(authenticatedUser, null, sirket, null, null, aylikPuantaj, "T", denklestirme, fazlaMesaiTalepDurum, session);
		List<SelectItem> selectList = new ArrayList<SelectItem>();
		if (!list.isEmpty()) {
			list = PdksUtil.sortObjectStringAlanList(list, "getAciklama", null);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Tanim veri = (Tanim) iterator.next();
				selectList.add(new SelectItem(veri.getId(), veri.getAciklama()));
			}
		}

		return selectList;
	}

	/**
	 * @param sirket
	 * @param tesisId
	 * @param aylikPuantaj
	 * @param denklestirme
	 * @param fazlaMesaiTalepDurum
	 * @param session
	 * @return
	 */
	public List<SelectItem> getFazlaMesaiMudurBolumList(Sirket sirket, String tesisId, AylikPuantaj aylikPuantaj, boolean denklestirme, boolean fazlaMesaiTalepDurum, Session session) {
		List<Tanim> list = ortakIslemler.getFazlaMesaiMudurList(authenticatedUser, null, sirket, tesisId, null, aylikPuantaj, "B", denklestirme, fazlaMesaiTalepDurum, session);
		List<SelectItem> selectList = new ArrayList<SelectItem>();
		if (!list.isEmpty()) {
			list = PdksUtil.sortObjectStringAlanList(list, "getAciklama", null);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Tanim veri = (Tanim) iterator.next();
				selectList.add(new SelectItem(veri.getId(), veri.getAciklama()));
			}
		}
		return selectList;
	}

	/**
	 * @param sirket
	 * @param tesisId
	 * @param bolumId
	 * @param aylikPuantaj
	 * @param denklestirme
	 * @param fazlaMesaiTalepDurum
	 * @param session
	 * @return
	 */
	public List<Personel> getFazlaMesaiMudurPersonelList(Sirket sirket, String tesisId, Long bolumId, AylikPuantaj aylikPuantaj, boolean denklestirme, boolean fazlaMesaiTalepDurum, Session session) {
		List<Personel> list = ortakIslemler.getFazlaMesaiMudurList(authenticatedUser, null, sirket, tesisId, bolumId, aylikPuantaj, "P", denklestirme, fazlaMesaiTalepDurum, session);
		if (!list.isEmpty())
			list = PdksUtil.sortObjectStringAlanList(list, "getAdSoyad", null);

		return list;
	}

	/**
	 * @param list
	 * @param yil
	 * @param ay
	 * @param session
	 * @return
	 */
	public List<Sirket> getDenklestirmeSirketList(List<Sirket> list, int yil, int ay, Session session) {
		HashMap fields = new HashMap();
		fields.put("yil", yil);
		fields.put("ay", ay);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		DenklestirmeAy denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);

		if (denklestirmeAy != null) {
			Calendar cal = Calendar.getInstance();

			cal.set(Calendar.DATE, 1);
			cal.set(Calendar.MONTH, ay - 1);
			cal.set(Calendar.YEAR, yil);
			Date basTarih = PdksUtil.getDate(cal.getTime());
			cal.add(Calendar.MONTH, 1);
			cal.add(Calendar.DATE, -1);
			Date bitTarih = PdksUtil.getDate(cal.getTime());

			fields.clear();
			List<Long> idler = new ArrayList<Long>();
			for (Sirket sirket : list)
				if (!idler.contains(sirket.getId()))
					idler.add(sirket.getId());

			StringBuffer sb = new StringBuffer();
			sb.append("SELECT DISTINCT S.* FROM " + PersonelDenklestirme.TABLE_NAME + " PD WITH(nolock) ");
			sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + "=" + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
			sb.append(" AND " + Personel.COLUMN_NAME_SIRKET + " :s");
			if (bitTarih != null) {
				sb.append(" AND P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + "<=:a1 ");
				fields.put("a1", bitTarih);
			}
			if (basTarih != null) {
				sb.append(" AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=:a2 ");
				fields.put("a2", basTarih);
			}
			sb.append(" INNER JOIN " + Sirket.TABLE_NAME + " S ON S." + Tanim.COLUMN_NAME_ID + "=" + Personel.COLUMN_NAME_SIRKET);
			sb.append(" WHERE PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=" + denklestirmeAy.getId());
			sb.append(" AND PD." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + "=1");
			sb.append(" ORDER BY S." + Sirket.COLUMN_NAME_AD);
			fields.put("s", idler);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				list = pdksEntityController.getObjectBySQLList(sb, fields, Sirket.class);
				idler = null;
			} catch (Exception ex) {
				logger.error(ex + "\n" + sb.toString());
			}

		}
		return list;
	}

	/**
	 * @param denklestirmeAy
	 * @param session
	 */
	public void setFazlaMesaiMaxSure(DenklestirmeAy denklestirmeAy, Session session) {
		denklestirmeAy.setFazlaMesaiMaxSure(ortakIslemler.getFazlaMesaiMaxSure(null));
		pdksEntityController.saveOrUpdate(session, entityManager, denklestirmeAy);
		session.flush();

	}

	/**
	 * @param vardiyaHafta
	 * @param tarih
	 * @param amount
	 * @return
	 */
	public String setVardiyaGunHafta(VardiyaHafta vardiyaHafta, Date tarih, int amount) {
		VardiyaGun vardiyaGun = new VardiyaGun();
		Date vardiyaDate1 = tarih;
		if (amount > 0) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(tarih);
			cal.add(Calendar.DATE, amount);
			vardiyaDate1 = PdksUtil.getDate(cal.getTime());
		}

		logger.debug(PdksUtil.convertToDateString(tarih, "yyyyMMdd") + " " + amount + " " + PdksUtil.convertToDateString(vardiyaDate1, "yyyyMMdd"));
		vardiyaGun.setVardiyaDate(vardiyaDate1);
		String tarihStr = vardiyaGun.getVardiyaDateStr();
		vardiyaHafta.getVardiyaPlan().getVardiyaGunMap().put(tarihStr, vardiyaGun);
		switch (amount) {
		case 0:
			vardiyaHafta.setVardiyaGun1(vardiyaGun);
			break;
		case 1:
			vardiyaHafta.setVardiyaGun2(vardiyaGun);
			break;
		case 2:
			vardiyaHafta.setVardiyaGun3(vardiyaGun);
			break;
		case 3:
			vardiyaHafta.setVardiyaGun4(vardiyaGun);
			break;
		case 4:
			vardiyaHafta.setVardiyaGun5(vardiyaGun);
			break;
		case 5:
			vardiyaHafta.setVardiyaGun6(vardiyaGun);
			break;
		case 6:
			vardiyaHafta.setVardiyaGun7(vardiyaGun);
			break;

		default:
			break;
		}
		vardiyaHafta.getVardiyaGunler().add(vardiyaGun);
		return tarihStr;

	}

	/**
	 * @param vardiyaHaftaList
	 * @param aylikPuantaj
	 * @param donemi
	 * @param tatillerMap
	 * @param sablonVardiyalar
	 * @return
	 */
	public VardiyaPlan haftalikVardiyaOlustur(List<VardiyaHafta> vardiyaHaftaList, AylikPuantaj aylikPuantaj, DepartmanDenklestirmeDonemi donemi, TreeMap<String, Tatil> tatillerMap, List<VardiyaGun> sablonVardiyalar) {
		HashMap<String, Integer> gunHaftaMap = new HashMap<String, Integer>();
		if (vardiyaHaftaList == null)
			vardiyaHaftaList = new ArrayList<VardiyaHafta>();
		VardiyaPlan vardiyaPlanMaster = new VardiyaPlan();
		vardiyaPlanMaster.setDenklestirmeDonemi(donemi);
		vardiyaPlanMaster.setCheckBoxDurum(Boolean.FALSE);
		Date tarih1 = (Date) donemi.getBaslangicTarih().clone();
		int i = 0;
		String donem = String.valueOf(aylikPuantaj.getYil() * 100 + aylikPuantaj.getAy());
		List<String> gunler = new ArrayList<String>();
		if (sablonVardiyalar != null) {
			for (VardiyaGun vg : sablonVardiyalar)
				gunler.add(vg.getVardiyaDateStr());

		}
		aylikPuantaj.setVardiyaHaftaList(vardiyaHaftaList);
		boolean devam = Boolean.TRUE;
		while (devam) {
			++i;
			VardiyaHafta vardiyaHafta = new VardiyaHafta();
			vardiyaHafta.setVardiyaPlan(vardiyaPlanMaster);
			vardiyaHafta.setHafta(i);
			for (int j = 0; j < 7; j++)
				gunHaftaMap.put(setVardiyaGunHafta(vardiyaHafta, tarih1, j), vardiyaHafta.getHafta());
			vardiyaHafta.setBasTarih(vardiyaHafta.getVardiyaGun1().getVardiyaDate());
			vardiyaHafta.setBitTarih(vardiyaHafta.getVardiyaGun7().getVardiyaDate());
			vardiyaHaftaList.add(vardiyaHafta);
			switch (i) {
			case 1:
				vardiyaPlanMaster.setVardiyaHafta1(vardiyaHafta);
				break;
			case 2:
				vardiyaPlanMaster.setVardiyaHafta2(vardiyaHafta);
				break;
			case 3:
				vardiyaPlanMaster.setVardiyaHafta3(vardiyaHafta);
				break;
			case 4:
				vardiyaPlanMaster.setVardiyaHafta4(vardiyaHafta);
				break;
			case 5:
				vardiyaPlanMaster.setVardiyaHafta5(vardiyaHafta);
				break;
			case 6:
				vardiyaPlanMaster.setVardiyaHafta6(vardiyaHafta);
				break;
			default:
				break;
			}
			for (Iterator iterator = vardiyaHafta.getVardiyaGunler().iterator(); iterator.hasNext();) {
				VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
				String vardiyaDateStr = vardiyaGun.getVardiyaDateStr();
				vardiyaGun.setAyinGunu(vardiyaDateStr.startsWith(donem));
				if (sablonVardiyalar != null && !gunler.contains(vardiyaDateStr)) {
					gunler.add(vardiyaDateStr);
					sablonVardiyalar.add(vardiyaGun);
				}
				if (tatillerMap != null && tatillerMap.containsKey(vardiyaDateStr))
					vardiyaGun.setTatil(tatillerMap.get(vardiyaDateStr));
				else if (vardiyaGun.getVardiya() != null && vardiyaGun.getVardiya().isCalisma()) {
					vardiyaGun.setVardiyaZamani();
					String key = PdksUtil.convertToDateString(vardiyaGun.getIslemVardiya().getVardiyaBitZaman(), "yyyyMMdd");
					if (tatillerMap.containsKey(key))
						vardiyaGun.setTatil(tatillerMap.get(key));
				}
			}
			tarih1 = PdksUtil.tariheGunEkleCikar(tarih1, 7);
			if (PdksUtil.tarihKarsilastirNumeric(tarih1, donemi.getBitisTarih()) == 1)
				devam = false;

		}
		gunler = null;
		vardiyaPlanMaster.getVardiyaHaftaList().addAll(vardiyaHaftaList);
		return vardiyaPlanMaster;
	}

	/**
	 * @param pdksDepartman
	 * @param yil
	 * @param ay
	 * @param session
	 * @return
	 */
	public List<SelectItem> sirketDoldur(Departman pdksDepartman, Integer yil, Integer ay, Session session) {
		Calendar cal = Calendar.getInstance();
		if (yil == null)
			yil = cal.get(Calendar.YEAR);
		if (ay == null)
			ay = cal.get(Calendar.MONTH) + 1;
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, ay - 1);
		cal.set(Calendar.YEAR, yil);
		Date basTarih = PdksUtil.getDate(cal.getTime());
		cal.add(Calendar.MONTH, 1);
		cal.add(Calendar.DATE, -1);
		Date bitTarih = PdksUtil.getDate(cal.getTime());
		HashMap fields = new HashMap();
		fields.put(PdksEntityController.MAP_KEY_MAP, "getId");
		fields.put(PdksEntityController.MAP_KEY_SELECT, "sirket");
		if (pdksDepartman != null)
			fields.put("sirket.departman.id=", pdksDepartman.getId());
		fields.put("iseBaslamaTarihi<=", bitTarih);
		fields.put("sskCikisTarihi>=", basTarih);

		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		TreeMap<Long, Sirket> sirketMap = pdksEntityController.getObjectByInnerObjectMapInLogic(fields, Personel.class, false);
		List<SelectItem> list = new ArrayList<SelectItem>();
		if (!sirketMap.isEmpty()) {
			List<Sirket> sirketList = PdksUtil.sortObjectStringAlanList(new ArrayList(sirketMap.values()), "getAd", null);
			for (Sirket sirket : sirketList) {
				if (sirket.getFazlaMesai())
					list.add(new SelectItem(sirket.getId(), sirket.getAd()));
			}
		}
		return list;
	}

	/**
	 * @param pdksDepartman
	 * @param sirket
	 * @param departmanId
	 * @param tesisId
	 * @param yil
	 * @param ay
	 * @param denklestirme
	 * @param session
	 * @return
	 */
	public List<SelectItem> bolumDoldur(Departman pdksDepartman, Sirket sirket, Long departmanId, Long tesisId, Integer yil, Integer ay, Boolean denklestirme, Session session) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("tip", "ekSaha3");
		map.put("sirket", sirket);
		map.put("pdksDepartman", pdksDepartman);
		map.put("departmanId", departmanId);
		if (tesisId != null)
			map.put("tesisId", tesisId);
		Calendar cal = Calendar.getInstance();
		if (yil == null)
			yil = cal.get(Calendar.YEAR);
		if (ay == null)
			ay = cal.get(Calendar.MONTH) + 1;
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, ay - 1);
		cal.set(Calendar.YEAR, yil);
		Date basTarih = PdksUtil.getDate(cal.getTime());
		cal.add(Calendar.MONTH, 1);
		cal.add(Calendar.DATE, -1);
		Date bitTarih = PdksUtil.getDate(cal.getTime());
		map.put("basTarih", basTarih);
		map.put("bitTarih", bitTarih);

		if (denklestirme) {
			map.put("yil", yil);
			map.put("ay", ay);
		}
		List<SelectItem> list = bolumTesisMapDoldur(map, session);
		return list;
	}

	/**
	 * @param map
	 * @param session
	 * @return
	 */
	public List<SelectItem> bolumTesisMapDoldur(HashMap<String, Object> map, Session session) {
		HashMap fields = new HashMap();
		List<SelectItem> gorevTipiList = null;
		Date basTarih = (Date) map.get("basTarih");
		Date bitTarih = (Date) map.get("bitTarih");
		String fieldAdi = (String) map.get("tip");
		Sirket sirket = (Sirket) map.get("sirket");
		Long tesisId = (Long) map.get("tesisId");
		Long departmanId = (Long) map.get("departmanId");
		Departman pdksDepartman = (Departman) map.get("pdksDepartman");
		Boolean denklestirme = (Boolean) map.get("denklestirme");
		if (fieldAdi == null)
			fieldAdi = "ekSaha3";
		fields.put(PdksEntityController.MAP_KEY_SELECT, fieldAdi);
		if (sirket != null) {
			if (sirket.isTesisDurumu()) {
				boolean tesisYetki = ortakIslemler.getParameterKey("tesisYetki").equals("1");
				if (tesisYetki && authenticatedUser.getYetkiliTesisler() != null && !authenticatedUser.getYetkiliTesisler().isEmpty()) {
					List<Long> idler = new ArrayList<Long>();
					for (Iterator iterator = authenticatedUser.getYetkiliTesisler().iterator(); iterator.hasNext();) {
						Tanim tesis = (Tanim) iterator.next();
						idler.add(tesis.getId());
					}
					if (idler.size() == 1) {
						tesisId = idler.get(0);

					} else {
						fields.put("tesis.id", idler);
					}
					idler = null;
				}

				fields.put("sirket.id=", sirket.getId());
				if (tesisId != null && tesisId > 0L)
					fields.put("tesis.id=", tesisId);
			} else {
				if (fieldAdi == null || fieldAdi.equals("tesis"))
					fields.put("sirket.id=", 0L);
				else
					fields.put("sirket.id=", sirket.getId());
			}
		} else
			fields.put("sirket.id=", 0L);

		if (departmanId == null && authenticatedUser.isDirektorSuperVisor())
			departmanId = authenticatedUser.getPdksPersonel().getEkSaha1().getId();
		else if (authenticatedUser.isIK_Tesis() && authenticatedUser.getPdksPersonel().getTesis() != null)
			fields.put("tesis.id=", authenticatedUser.getPdksPersonel().getTesis().getId());

		if (departmanId != null)
			fields.put("ekSaha1.id=", departmanId);
		else if (authenticatedUser.isYonetici() && !(authenticatedUser.isIK() || authenticatedUser.isAdmin()))
			fields.put("pdksSicilNo", ortakIslemler.getYetkiTumPersonelNoListesi(authenticatedUser));
		fields.put("iseBaslamaTarihi<=", bitTarih);
		fields.put("sskCikisTarihi>=", basTarih);
		if (pdksDepartman != null && pdksDepartman.isAdminMi() && denklestirme != null && denklestirme)
			fields.put("pdks=", Boolean.TRUE);
		fields.put(fieldAdi + "<>", null);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Tanim> tanimlar = pdksEntityController.getObjectByInnerObjectListInLogic(fields, Personel.class);
		if (!tanimlar.isEmpty() && map.containsKey("yil") && map.containsKey("ay")) {
			fields.clear();
			fields.put("yil", map.get("yil"));
			fields.put("ay", map.get("ay"));
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			DenklestirmeAy denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
			if (denklestirmeAy != null) {
				fields.clear();
				List<Long> idler = new ArrayList<Long>();
				for (Tanim tanim : tanimlar)
					if (!idler.contains(tanim.getId()))
						idler.add(tanim.getId());

				StringBuffer sb = new StringBuffer();
				sb.append("SELECT DISTINCT T.* FROM " + PersonelDenklestirme.TABLE_NAME + " PD WITH(nolock) ");
				sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + "=" + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
				if (bitTarih != null) {
					sb.append(" AND P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + "<=:a1 ");
					fields.put("a1", bitTarih);
				}
				if (basTarih != null) {
					sb.append(" AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=:a2 ");
					fields.put("a2", basTarih);
				}
				if (sirket != null)
					sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + "=" + sirket.getId());
				if (tesisId != null && tesisId > 0L)
					sb.append(" AND P." + Personel.COLUMN_NAME_TESIS + "=" + tesisId);
				sb.append(" INNER JOIN " + Tanim.TABLE_NAME + " T ON T." + Tanim.COLUMN_NAME_ID + "=" + (fieldAdi.equals("ekSaha3") ? Personel.COLUMN_NAME_EK_SAHA3 : Personel.COLUMN_NAME_TESIS));
				sb.append(" AND T." + Tanim.COLUMN_NAME_ID + " :t");
				sb.append(" WHERE PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=" + denklestirmeAy.getId());
				sb.append(" AND PD." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + "=1");
				fields.put("t", idler);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				try {
					tanimlar = pdksEntityController.getObjectBySQLList(sb, fields, Tanim.class);
					idler = null;
				} catch (Exception ex) {
					logger.error(ex + "\n" + sb.toString());
				}

			}
		}
		if (authenticatedUser.isYonetici() || authenticatedUser.isDirektorSuperVisor() || authenticatedUser.isAdmin() || authenticatedUser.isIK() || tanimlar.size() > 50) {
			HashMap<Long, Tanim> tanimMap = new HashMap<Long, Tanim>();
			for (Tanim tanim : tanimlar)
				tanimMap.put(tanim.getId(), tanim);

			tanimlar = null;
			tanimlar = PdksUtil.sortObjectStringAlanList(new ArrayList<Tanim>(tanimMap.values()), "getAciklama", null);
			gorevTipiList = ortakIslemler.getTanimSelectItem(tanimlar);
			tanimMap = null;

		}
		tanimlar = null;
		return gorevTipiList;

	}

	/**
	 * @param ay
	 * @param yil
	 * @param denklestirmeDonemi
	 * @param session
	 * @return
	 */
	public AylikPuantaj getAylikPuantaj(int ay, int yil, DepartmanDenklestirmeDonemi denklestirmeDonemi, Session session) {
		AylikPuantaj aylikPuantaj = new AylikPuantaj();
		if (yil > 0) {

			aylikPuantaj.setAy(ay);
			aylikPuantaj.setYil(yil);

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DATE, 1);
			cal.set(Calendar.MONTH, aylikPuantaj.getAy() - 1);
			cal.set(Calendar.YEAR, aylikPuantaj.getYil());

			aylikPuantaj.setIlkGun(PdksUtil.getDate((Date) cal.getTime().clone()));
			int haftaGun = cal.get(Calendar.DAY_OF_WEEK);
			if (haftaGun != Calendar.MONDAY) {
				int cikar = haftaGun != Calendar.SUNDAY ? 2 - haftaGun : -6;
				cal.add(Calendar.DATE, cikar);
				denklestirmeDonemi.setBaslangicTarih(PdksUtil.getDate((Date) cal.getTime().clone()));
			} else
				denklestirmeDonemi.setBaslangicTarih(aylikPuantaj.getIlkGun());
			cal.setTime(aylikPuantaj.getIlkGun());
			cal.add(Calendar.MONTH, 1);
			cal.add(Calendar.DATE, -1);
			aylikPuantaj.setGunSayisi(cal.get(Calendar.DATE));
			haftaGun = cal.get(Calendar.DAY_OF_WEEK);
			aylikPuantaj.setSonGun(PdksUtil.getDate((Date) cal.getTime().clone()));
			if (haftaGun != Calendar.SUNDAY) {
				int ekle = 8 - haftaGun;
				cal.add(Calendar.DATE, ekle);
				denklestirmeDonemi.setBitisTarih(PdksUtil.getDate((Date) cal.getTime().clone()));
			} else
				denklestirmeDonemi.setBitisTarih(aylikPuantaj.getSonGun());
		} else {
			aylikPuantaj.setIlkGun(denklestirmeDonemi.getBaslangicTarih());
			aylikPuantaj.setSonGun(denklestirmeDonemi.getBitisTarih());
			List<VardiyaGun> list = new ArrayList<VardiyaGun>();
			aylikPuantaj.setVardiyalar(list);
			Calendar cal = Calendar.getInstance();
			cal.setTime(denklestirmeDonemi.getBaslangicTarih());
			while (cal.getTime().getTime() <= denklestirmeDonemi.getBitisTarih().getTime()) {
				Date vardiyaDate = (Date) cal.getTime().clone();
				VardiyaGun vardiyaGun = new VardiyaGun();
				vardiyaGun.setVardiyaDate(vardiyaDate);
				cal.add(Calendar.DATE, 1);
				vardiyaGun.setAyinGunu(Boolean.TRUE);
				list.add(vardiyaGun);
			}

		}

		TreeMap<String, Tatil> tatilGunleriMap = ortakIslemler.getTatilGunleri(null, denklestirmeDonemi.getBaslangicTarih(), denklestirmeDonemi.getBitisTarih(), session);
		for (Iterator iterator = aylikPuantaj.getVardiyalar().iterator(); iterator.hasNext();) {
			VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
			vardiyaGun.setTatil(tatilGunleriMap.containsKey(vardiyaGun.getVardiyaDateStr()) ? tatilGunleriMap.get(vardiyaGun.getVardiyaDateStr()) : null);
		}
		denklestirmeDonemi.setTatilGunleriMap(tatilGunleriMap);
		return aylikPuantaj;
	}

	/**
	 * @param departmanId
	 * @param sirketId
	 * @param yil
	 * @param ay
	 * @param fazlaMesai
	 * @param session
	 * @return
	 */
	public List<SelectItem> getBolumDepartmanSelectItems(Long departmanId, Long sirketId, Integer yil, Integer ay, Boolean fazlaMesai, Session session) {
		List<SelectItem> bolumDepartmanlari = null;
		List<Tanim> bolumler = null;
		HashMap fields = new HashMap();
		if (departmanId != null) {
			Date bitTarih = null, basTarih = null;
			if (sirketId != null) {
				Calendar cal = Calendar.getInstance();
				if (yil == null)
					yil = cal.get(Calendar.YEAR);
				if (ay == null)
					ay = cal.get(Calendar.MONTH) + 1;
				cal.set(Calendar.DATE, 1);
				cal.set(Calendar.MONTH, ay - 1);
				cal.set(Calendar.YEAR, yil);
				basTarih = PdksUtil.getDate(cal.getTime());
				cal.add(Calendar.MONTH, 1);
				cal.add(Calendar.DATE, -1);
				bitTarih = PdksUtil.getDate(cal.getTime());

				fields.put(PdksEntityController.MAP_KEY_MAP, "getId");
				fields.put(PdksEntityController.MAP_KEY_SELECT, "ekSaha3");

				fields.put("sirket.id=", sirketId);
				if (authenticatedUser.isDirektorSuperVisor())
					fields.put("ekSaha1.id=", authenticatedUser.getPdksPersonel().getEkSaha1().getId());
				if (bitTarih != null)
					fields.put("iseBaslamaTarihi<=", bitTarih);
				if (basTarih != null)
					fields.put("sskCikisTarihi>=", basTarih);
				fields.put("ekSaha3<>", null);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				TreeMap<Long, Tanim> bolumMap = pdksEntityController.getObjectByInnerObjectMapInLogic(fields, Personel.class, true);
				if (!bolumMap.isEmpty())
					bolumler = new ArrayList<Tanim>(bolumMap.values());
				bolumMap = null;
			} else
				bolumler = ortakIslemler.getTanimList(Tanim.TIPI_BOLUM_DEPARTMAN + departmanId, session);
		}
		if (bolumler == null || bolumler.isEmpty())
			bolumDepartmanlari = new ArrayList<SelectItem>();
		else {
			if (fazlaMesai) {

				fields.clear();
				fields.put("yil", yil);
				fields.put("ay", ay);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				DenklestirmeAy denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
				if (denklestirmeAy != null) {
					fields.clear();
					List<Long> idler = new ArrayList<Long>();
					for (Tanim tanim : bolumler)
						if (!idler.contains(tanim.getId()))
							idler.add(tanim.getId());

					StringBuffer sb = new StringBuffer();
					sb.append("SELECT DISTINCT T.* FROM " + PersonelDenklestirme.TABLE_NAME + " PD WITH(nolock)  ");
					sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + "=" + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
					if (sirketId != null)
						sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + "=" + sirketId);

					sb.append(" INNER JOIN " + Tanim.TABLE_NAME + " T ON T." + Tanim.COLUMN_NAME_ID + "=" + Personel.COLUMN_NAME_EK_SAHA3);
					sb.append(" AND T." + Tanim.COLUMN_NAME_ID + " :t");
					sb.append(" WHERE PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=" + denklestirmeAy.getId());
					sb.append(" AND PD." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + "=1");
					fields.put("t", idler);
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					try {
						bolumler = pdksEntityController.getObjectBySQLList(sb, fields, Tanim.class);
						idler = null;
					} catch (Exception ex) {
						logger.error(ex + "\n" + sb.toString());
					}

				}

			}
			if (bolumler.size() > 1)
				bolumler = PdksUtil.sortObjectStringAlanList(bolumler, "getAciklama", null);
			bolumDepartmanlari = ortakIslemler.getTanimSelectItem(bolumler);
		}
		bolumler = null;
		return bolumDepartmanlari;
	}

	/**
	 * @param factory
	 * @param drawing
	 * @param anchor
	 * @param personelDenklestirme
	 * @return
	 */
	public Comment getCommentGuncelleyen(CreationHelper factory, Drawing drawing, ClientAnchor anchor, PersonelDenklestirme personelDenklestirme) {
		Comment commentGuncelleyen;
		commentGuncelleyen = drawing.createCellComment(anchor);
		String title = "Onaylayan : " + personelDenklestirme.getGuncelleyenUser().getAdSoyad() + "\n";
		title += "Zaman : " + authenticatedUser.dateTimeFormatla(personelDenklestirme.getGuncellemeTarihi());
		RichTextString str1 = factory.createRichTextString(title);
		commentGuncelleyen.setString(str1);
		return commentGuncelleyen;
	}

	/**
	 * @param aylikPuantaj
	 * @param gunData
	 * @return
	 */
	public String getVardiyaGun(AylikPuantaj aylikPuantaj, VardiyaGun gunData) {
		VardiyaGun gun = null;
		try {
			if (gunData != null && aylikPuantaj != null)
				gun = aylikPuantaj.getVardiyaGun(gunData);

		} catch (Exception e) {
			gun = null;
		}

		return gun != null ? "" : null;
	}

	/**
	 * @param map
	 * @param per
	 * @param vardiya
	 * @return
	 */
	public Integer getVardiyaAdet(TreeMap<String, TreeMap<String, List<VardiyaGun>>> map, Personel per, Vardiya vardiya) {
		Integer adet = null;
		if (vardiya != null && per != null && map != null) {
			if (per.getId() != null && map.containsKey(per.getId())) {
				TreeMap<String, List<VardiyaGun>> perMap = map.get(per.getId());
				if (vardiya.getKisaAdi() != null && perMap.containsKey(vardiya.getKisaAdi()))
					adet = perMap.get(vardiya.getKisaAdi()).size();
			}
		}
		return adet;
	}

	/**
	 * @param puantajList
	 */
	public void sortAylikPuantajPersonelBolum(List<AylikPuantaj> puantajList) {
		if (puantajList != null && !puantajList.isEmpty()) {
			List<AylikPuantaj> puantajYeniList = new ArrayList<AylikPuantaj>();
			TreeMap<Long, List<AylikPuantaj>> map = new TreeMap<Long, List<AylikPuantaj>>();
			TreeMap<Long, Tanim> tanimMap = new TreeMap<Long, Tanim>();
			for (AylikPuantaj aylikPuantaj : puantajList) {
				Personel personel = aylikPuantaj.getPdksPersonel();
				if (personel.getEkSaha3() != null) {
					Tanim tanim = personel.getEkSaha3();
					List<AylikPuantaj> list = map.containsKey(tanim.getId()) ? map.get(tanim.getId()) : new ArrayList<AylikPuantaj>();
					if (list.isEmpty()) {
						tanimMap.put(tanim.getId(), tanim);
						map.put(tanim.getId(), list);
					}
					list.add(aylikPuantaj);
				} else
					puantajYeniList.add(aylikPuantaj);
			}
			if (!tanimMap.isEmpty()) {
				List<Tanim> list = PdksUtil.sortObjectStringAlanList(new ArrayList(tanimMap.values()), "getAciklama", null);
				for (Tanim tanim : list) {
					puantajYeniList.addAll(map.get(tanim.getId()));
				}
			}
			puantajList.clear();
			puantajList.addAll(puantajYeniList);
			puantajYeniList = null;
			tanimMap = null;
			map = null;
		}

	}

	/**
	 * @param vgList
	 * @param puantajList
	 * @param izinGoster
	 * @return
	 */
	public TreeMap<String, Object> getIzinOzetMap(List<VardiyaGun> vgList, List<AylikPuantaj> puantajList, boolean izinGoster) {
		TreeMap<String, Object> map = new TreeMap<String, Object>();
		try {
			if (puantajList != null) {
				if (vgList == null)
					vgList = new ArrayList<VardiyaGun>();
				for (AylikPuantaj aylikPuantaj : puantajList) {
					for (VardiyaGun vg : aylikPuantaj.getVardiyalar()) {
						Vardiya vardiya = vg.getVardiya();
						if (vg.isAyinGunu() && vardiya != null && vardiya.getId() != null && vg.isIzinli()) {
							boolean ekle = vardiya.isGecerliIzin() || (vg.getIzin() != null && !(vardiya.isHaftaTatil() || vardiya.isOff()));
							if (!ekle && vg.getIzin() != null) {
								IzinTipi izinTipi = vg.getIzin().getIzinTipi();
								ekle = izinTipi.isTakvimGunuMu();
								if (!ekle) {
									ekle = true;
									if (vardiya.isOff())
										ekle = izinTipi.isOffDahilMi();
									else if (vardiya.isHaftaTatil())
										ekle = izinTipi.isHTDahil();

								}
							}
							if (ekle) {
								vgList.add(vg);
							}

						}

					}
				}
			}
			String key = ortakIslemler.getParameterKey("izinPersonelOzetGoster");

			boolean devam = (authenticatedUser.isAdmin() && izinGoster) || key.equals("1") || (authenticatedUser.isIK() && PdksUtil.hasStringValue(key)), htToplamiGuncelle = false;
			if (vgList != null && (authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || devam)) {
				TreeMap<String, Vardiya> izinTipiVardiyaMap = new TreeMap<String, Vardiya>();
				TreeMap<Long, TreeMap<String, List<VardiyaGun>>> izinTipiPersonelVardiyaMap = new TreeMap<Long, TreeMap<String, List<VardiyaGun>>>();
				TreeMap<Long, Personel> izinTipiPersonelMap = new TreeMap<Long, Personel>();
				TreeMap<String, VardiyaGun> htMap = new TreeMap<String, VardiyaGun>();
				TreeMap<String, String> izinMap = new TreeMap<String, String>();
				for (VardiyaGun vg : vgList) {
					if (vg.getVardiya() != null && vg.getVardiya().getId() != null) {
						Vardiya vardiyaIzin = null;
						String dateStr = vg.getVardiyaKeyStr();
						if (vg.getIzin() == null && vg.getVardiya().isHaftaTatil()) {
							if (vg.getVardiyaSaat() == null || vg.getVardiyaSaat().getNormalSure() == 0.0d)
								htMap.put(dateStr, vg);
							continue;
						}
						if (vg.getIzin() != null) {
							IzinTipi izinTipi = vg.getIzin().getIzinTipi();
							if (!izinTipi.isTakvimGunuMu()) {
								Vardiya vardiya = vg.getVardiya();
								boolean hataliDurum = (vardiya.isHaftaTatil() && izinTipi.isHTDahil() == false) || (vardiya.isOffGun() && izinTipi.isOffDahilMi() == false);
								if (hataliDurum)
									continue;
							}
							vardiyaIzin = izinTipi.getIzinVardiya(izinTipi.isRaporIzin() == false ? Vardiya.TIPI_IZIN : Vardiya.TIPI_HASTALIK_RAPOR);
						} else if (vg.getVardiya().isGecerliIzin()) {
							htToplamiGuncelle = true;
							vardiyaIzin = vg.getVardiya();
						}

						else
							continue;
						String izinKodu = vardiyaIzin != null ? vardiyaIzin.getKisaAdi() : null;
						if (izinKodu == null)
							continue;
						if (vardiyaIzin.isHastalikRapor())
							izinMap.put(dateStr, izinKodu);
						if (vg.isAyinGunu() == false)
							continue;
						Long perNo = vg.getPersonel().getId();
						if (!izinTipiPersonelMap.containsKey(perNo))
							izinTipiPersonelMap.put(perNo, vg.getPersonel());
						if (!izinTipiVardiyaMap.containsKey(izinKodu))
							izinTipiVardiyaMap.put(izinKodu, vardiyaIzin);

						setVardiyaIzinListesi(vg, izinKodu, izinTipiPersonelVardiyaMap);

					}
				}
				if (htToplamiGuncelle) {
					for (String dateStr : htMap.keySet()) {
						VardiyaGun vg = htMap.get(dateStr);
						if (vg.getIzin() != null)
							continue;
						String oncekiStr = vg.getOncekiVardiya() != null ? vg.getOncekiVardiyaGun().getVardiyaKeyStr() : "";
						String sonrakiStr = vg.getSonrakiVardiyaGun() != null ? vg.getSonrakiVardiyaGun().getVardiyaKeyStr() : "";
						if ((izinMap.containsKey(sonrakiStr) && izinMap.containsKey(oncekiStr)) && !izinMap.containsKey(dateStr)) {
							String izinKoduOnceki = izinMap.get(oncekiStr), izinKoduSonraki = izinMap.get(sonrakiStr);
							logger.debug(dateStr + " " + izinKoduOnceki);
							if (izinKoduSonraki.equals(izinKoduOnceki))
								setVardiyaIzinListesi(vg, izinKoduOnceki, izinTipiPersonelVardiyaMap);
						}
					}
				}
				if (!izinTipiVardiyaMap.isEmpty()) {
					map.put("izinTipiVardiyaList", PdksUtil.sortListByAlanAdi(new ArrayList(izinTipiVardiyaMap.values()), "ekranSira", false));
					map.put("izinTipiPersonelMap", izinTipiPersonelMap);
					map.put("izinTipiPersonelVardiyaMap", izinTipiPersonelVardiyaMap);
				}

			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		return map;
	}

	/**
	 * @param vg
	 * @param izinKodu
	 * @param izinTipiPersonelVardiyaMap
	 */
	private void setVardiyaIzinListesi(VardiyaGun vg, String izinKodu, TreeMap<Long, TreeMap<String, List<VardiyaGun>>> izinTipiPersonelVardiyaMap) {
		Long perNo = vg.getPersonel().getId();
		TreeMap<String, List<VardiyaGun>> perMap = izinTipiPersonelVardiyaMap.containsKey(perNo) ? izinTipiPersonelVardiyaMap.get(perNo) : new TreeMap<String, List<VardiyaGun>>();
		if (perMap.isEmpty())
			izinTipiPersonelVardiyaMap.put(perNo, perMap);
		List<VardiyaGun> perVardiyaGunList = perMap.containsKey(izinKodu) ? perMap.get(izinKodu) : new ArrayList<VardiyaGun>();
		if (perVardiyaGunList.isEmpty())
			perMap.put(izinKodu, perVardiyaGunList);
		perVardiyaGunList.add(vg);
	}
}
