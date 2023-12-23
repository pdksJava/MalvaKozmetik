package org.pdks.erp.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Departman;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelDenklestirmeTasiyici;
import org.pdks.entity.PersonelExtra;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.security.entity.User;
import org.pdks.session.Constants;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;
import org.hibernate.Session;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoTable;

@Name("pdksSap3Controller")
public class PdksSap3Controller implements ERPController, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6003454132581246078L;

	static Logger logger = Logger.getLogger(PdksSap3Controller.class);
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	HashMap<String, String> parameterMap;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	/**
	 * @param mesaiTable
	 * @param pernr
	 * @param lgart
	 * @param bordroTarihi
	 * @param sure
	 */
	private void mesaiEkle(JCoTable mesaiTable, String pernr, Date bordroTarihi, String lgart, Double sure) {
		if (sure != null && sure > 0.0d) {
			mesaiTable.appendRow();
			mesaiTable.setValue("PERNR", pernr);
			mesaiTable.setValue("LGART", lgart);
			mesaiTable.setValue("BEGDA", bordroTarihi);
			String anzhl = String.valueOf(sure);
			mesaiTable.setValue("ANZHL", PdksUtil.replaceAllManuel(anzhl, ".", ","));
		}

	}

	public void setFazlaMesaiUcretRFC(List<PersonelDenklestirme> sapMesaiList, User user, Session session) throws Exception {
		ConnectionMan sapRfcManager = new ConnectionMan();

		JCoDestination jcoClient = sapRfcManager.getJCoDestination();
		ArrayList<String> hataList = new ArrayList<String>();
		if (jcoClient != null) {
			String hataMesaj = null;
			try {

				DenklestirmeAy denklestirmeAy = sapMesaiList.get(0).getDenklestirmeAy();
				Calendar cal = Calendar.getInstance();
				Date guncellemeTarihi = (Date) cal.getTime().clone();
				cal.set(denklestirmeAy.getYil(), denklestirmeAy.getAy() - 1, 1);
				Date bordroTarihi = PdksUtil.getDate(cal.getTime());

				JCoFunction function = jcoClient.getRepository().getFunction("ZHR_PDKS_FMESAI_YAZ");
				JCoParameterList ipl = function.getTableParameterList();
				JCoTable mesaiTable = ipl.getTable("TABLO");
				JCoTable cikanTable = ipl.getTable("MSGTAB");

				for (PersonelDenklestirme personelDenklestirme : sapMesaiList) {
					mesaiTable.deleteAllRows();
					cikanTable.deleteAllRows();
					double odenecekSure = 0d;
					if (personelDenklestirme.getOdenecekSure() != null && personelDenklestirme.getOdenecekSure() > 0)
						odenecekSure = personelDenklestirme.getOdenecekSure();
					String pernr = personelDenklestirme.getPersonel().getPdksSicilNo();

					mesaiEkle(mesaiTable, pernr, bordroTarihi, PersonelDenklestirmeTasiyici.LGART_FAZLA_MESAI, personelDenklestirme.getOdenecekSure());// 1510
					mesaiEkle(mesaiTable, pernr, bordroTarihi, PersonelDenklestirmeTasiyici.LGART_RESMI_TATIL_MESAI, personelDenklestirme.getResmiTatilSure());// 1530
					mesaiEkle(mesaiTable, pernr, bordroTarihi, PersonelDenklestirmeTasiyici.LGART_AKSAM_GUN_CALISMA, personelDenklestirme.getAksamVardiyaSayisi());// 3265
					mesaiEkle(mesaiTable, pernr, bordroTarihi, PersonelDenklestirmeTasiyici.LGART_AKSAM_SAAT_CALISMA, personelDenklestirme.getAksamVardiyaSaatSayisi()); // 3268
					ArrayList<String> list = new ArrayList<String>();
					try {
						if (mesaiTable.getNumRows() > 0) {

							PdksUtil.getXMLFunction3(jcoClient, function, new String[] { "TABLO", "MSGTAB" });
							if (cikanTable.getNumRows() > 0) {

								do {
									if (cikanTable.getString("MSGTY").equals("E")) {
										String mesaj = cikanTable.getString("MSGTXT");
										if (mesaj.indexOf("&1") > 0)
											mesaj = PdksUtil.replaceAllManuel(mesaj, "&1", cikanTable.getString("MSGV1").trim());
										if (mesaj.indexOf("&2") > 0)
											mesaj = PdksUtil.replaceAllManuel(mesaj, "&2", cikanTable.getString("MSGV2").trim());
										if (mesaj.indexOf("&3") > 0)
											mesaj = PdksUtil.replaceAllManuel(mesaj, "&3", cikanTable.getString("MSGV3").trim());
										if (mesaj.indexOf("&4") > 0)
											mesaj = PdksUtil.replaceAllManuel(mesaj, "&4", cikanTable.getString("MSGV4").trim());
										mesaj = pernr + " " + personelDenklestirme.getPersonel().getAdSoyad() + " --> Sap Hata Mesajı : " + mesaj + " (Bordro Yapan İnsan Kaynakları Personel'inden Destek Alınız!) ";
										if (!list.contains(mesaj))
											list.add(mesaj);
									}
								} while (cikanTable.nextRow());
							}
							if (!list.isEmpty()) {
								hataList.addAll(list);
								logger.info(mesaiTable.toXML() + "\n" + cikanTable.toXML());
							}
							if (list.isEmpty() && user.isIK()) {
								personelDenklestirme.setErpAktarildi(Boolean.TRUE);
								if (personelDenklestirme.getId() != null) {
									personelDenklestirme.setOdenenSure(odenecekSure);
									personelDenklestirme.setGuncelleyenUser(user);
									personelDenklestirme.setGuncellemeTarihi(guncellemeTarihi);
									pdksEntityController.saveOrUpdate(session, entityManager, personelDenklestirme);
									session.flush();
								}

							}
						}

					} catch (Exception e1) {
						hataMesaj = e1.getMessage();
						logger.info(hataMesaj);
						hataList.add(hataMesaj);
					}
					list = null;
				}

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				hataMesaj = e.getMessage();
				hataList.add(hataMesaj);
			} finally {
				if (jcoClient != null)

					if (hataMesaj != null)
						throw new Exception(hataMesaj);
			}

		}

	}

	public String setIzinRFC(PersonelIzin izin) throws Exception {
		ConnectionMan sapRfcManager = new ConnectionMan();
		Calendar cal = Calendar.getInstance();
		String mesaj = "Hata Olabilir";
		JCoDestination jcoClient = sapRfcManager.getJCoDestination();
		if (jcoClient != null) {
			try {
				mesaj = "";
				JCoFunction function = jcoClient.getRepository().getFunction("ZHR_IZIN_YARAT");
				JCoParameterList ipl = function.getTableParameterList();
				JCoParameterList impParametre = function.getImportParameterList();
				JCoTable cikanTable = ipl.getTable("MESTAB");
				impParametre.setValue("PERNR", izin.getIzinSahibi().getSicilNo());
				String gidenSapKodu = izin.getIzinTipi().getIzinTipiTanim().getKodu();
				if (gidenSapKodu.indexOf("+") > -1)
					gidenSapKodu = PdksUtil.replaceAll(gidenSapKodu, "+", "");
				gidenSapKodu = PdksUtil.textBaslangicinaKarakterEkle(gidenSapKodu, '0', 4);
				impParametre.setValue("AWART", gidenSapKodu);
				boolean devamsizlik = Boolean.FALSE;
				if (izin.getIzinTipi().getIzinTipiTanim().getParentTanim().getKodu().equalsIgnoreCase("2001"))
					devamsizlik = Boolean.TRUE;
				impParametre.setValue("INFTY", izin.getIzinTipi().getIzinTipiTanim().getParentTanim().getKodu().trim());
				if (devamsizlik == Boolean.TRUE) {

					impParametre.setValue("PRM", "1");
				} else {

					impParametre.setValue("PRM", "2");
				}
				impParametre.setValue("BEGDA", izin.getBaslangicZamani());
				impParametre.setValue("ENDDA", ortakIslemler.tariheGunEkleCikar(cal, izin.getBitisZamani(), -1));

				PdksUtil.getXMLFunction3(jcoClient, function, new String[] { "MESTAB" });
				StringBuilder sb = new StringBuilder();
				if (cikanTable.getNumRows() > 0) {

					do {

						if (!cikanTable.getString("MSGTYP").equals("S"))
							sb.append(cikanTable.getString("TEXT") + " ");

					} while (cikanTable.nextRow());

				} else {

					mesaj = "sapdan cevap alınamadı.";

				}
				if (sb.length() > 0)
					mesaj = sb.toString();
				sb = null;

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				mesaj = " SAP ( " + e.getMessage() + " )";
			} finally {

			}
		} else {

			mesaj = "SAP bağlantısı kurulamadı.";
		}

		return mesaj;
	}

	public LinkedHashMap<String, Personel> topluHaldePersonelBilgisiGetir(Session session, TreeMap bordroAltBirimiMap, TreeMap masrafYeriMap, LinkedHashMap<String, Personel> personelMap, Date baslangicZamani, Date bitisZamani, Object sapRfcManagerObject, Object jcoClientObject) throws Exception {
		ConnectionMan sapRfcManager = sapRfcManagerObject != null ? (ConnectionMan) sapRfcManagerObject : new ConnectionMan();
		JCoDestination jcoClient = jcoClientObject != null ? (JCoDestination) jcoClientObject : null;
		boolean kapat = jcoClient == null;
		if (kapat)
			jcoClient = sapRfcManager.getJCoDestination();
		if (baslangicZamani == null)
			baslangicZamani = PdksUtil.buGun();
		if (bitisZamani == null)
			bitisZamani = PdksUtil.getSonSistemTarih();
		JCoFunction function = null;
		Departman departman = null;
		User olusturanUser = null;
		if (jcoClient != null) {
			try {
				function = jcoClient.getRepository().getFunction("ZHR_PDKS_PER_INFO");
				JCoParameterList rfcTableList = function.getTableParameterList();
				JCoTable girisTable = rfcTableList.getTable("TABLO_GIR");
				JCoTable sonucResultTable = rfcTableList.getTable("TABLO");
				girisTable.deleteAllRows();
				sonucResultTable.deleteAllRows();
				Personel personel = null;
				if (!personelMap.isEmpty()) {
					boolean yeni = masrafYeriMap != null;
					HashMap map = new HashMap();
					map.put(PdksEntityController.MAP_KEY_MAP, "getSapKodu");
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					// map.put("sapKodu<>", "");
					map.put("erpDurum=", Boolean.TRUE);
					map.put("durum=", Boolean.TRUE);
					// map.put("pdks=", Boolean.TRUE);
					TreeMap sirketMap = pdksEntityController.getObjectByInnerObjectMapInLogic(map, Sirket.class, Boolean.FALSE);
					map.clear();
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					map.put("tipi", Tanim.TIPI_GENEL_TANIM);
					map.put("kodu", Tanim.TIPI_SAP_MASRAF_YERI);
					Tanim bagliMasrafYeri = (Tanim) pdksEntityController.getObjectByInnerObject(map, Tanim.class);
					map.clear();
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					map.put("tipi", Tanim.TIPI_GENEL_TANIM);
					map.put("kodu", Tanim.TIPI_BORDRO_ALT_BIRIMI);
					Tanim bagliBodroAltBirimi = (Tanim) pdksEntityController.getObjectByInnerObject(map, Tanim.class);
					if (masrafYeriMap == null) {
						map.clear();
						if (session != null)
							map.put(PdksEntityController.MAP_KEY_SESSION, session);
						map.put(PdksEntityController.MAP_KEY_MAP, "getKodu");
						map.put("tipi", Tanim.TIPI_SAP_MASRAF_YERI);
						masrafYeriMap = pdksEntityController.getObjectByInnerObjectMap(map, Tanim.class, Boolean.FALSE);

					}
					if (bordroAltBirimiMap == null) {
						map.clear();
						if (session != null)
							map.put(PdksEntityController.MAP_KEY_SESSION, session);
						map.put(PdksEntityController.MAP_KEY_MAP, "getKodu");
						map.put("tipi", Tanim.TIPI_BORDRO_ALT_BIRIMI);
						bordroAltBirimiMap = pdksEntityController.getObjectByInnerObjectMap(map, Tanim.class, Boolean.FALSE);

					}
					girisTable.appendRow();
					girisTable.setValue("BEGDA", baslangicZamani);
					girisTable.setValue("ENDDA", bitisZamani);
					for (String personelNumarasi : personelMap.keySet()) {
						girisTable.setValue("PERNR", personelNumarasi);
						PdksUtil.getXMLFunction3(jcoClient, function, new String[] { "TABLO_GIR", "TABLO" });
						if (sonucResultTable.getNumRows() > 0) {
							Tanim masrafYeri = null;
							Tanim bordroAltAlan = null;
							Sirket sirket = null;
							do {
								personel = ((Personel) personelMap.get(sonucResultTable.getString("PERNR")));
								PersonelExtra personelExtra = personel.getPersonelExtra();
								if (personelExtra == null) {
									personelExtra = new PersonelExtra();
									personelExtra.setPersonel(personel);
									personel.setPersonelExtra(personelExtra);
								}
								personelExtra.setCepTelefon(PdksUtil.replaceAll(sonucResultTable.getString("GSMTL"), " ", ""));
								personelExtra.setIlce(sonucResultTable.getString("ORT02"));
								personel.setErpSicilNo(sonucResultTable.getString("PERNR"));
								String sirketKodu = sonucResultTable.getString("BUKRS").trim();
								if (!personel.getDurum())
									personel.setDurum(Boolean.TRUE);
								if (sirketMap.containsKey(sirketKodu))
									sirket = (Sirket) sirketMap.get(sirketKodu);
								else {
									try {
										boolean durumUpdate = sirketKodu.length() < 4;
										HashMap fields = new HashMap();
										if (!durumUpdate) {
											fields.put("erpKodu", sirketKodu);
											fields.put(PdksEntityController.MAP_KEY_SESSION, session);
											sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);
											durumUpdate = sirket != null;
											if (sirket == null) {
												if (departman == null) {
													fields.clear();
													fields.put("id", 1L);
													fields.put(PdksEntityController.MAP_KEY_SESSION, session);
													departman = (Departman) pdksEntityController.getObjectByInnerObject(fields, Departman.class);

												}
												if (olusturanUser == null)
													olusturanUser = ortakIslemler.getSistemAdminUser(session);
												String sirketAdi = PdksUtil.getStringKes(sonucResultTable.getString("BUTXT").trim(), 254);
												sirket = new Sirket();
												sirket.setErpKodu(sirketKodu);
												sirket.setAciklama(sirketAdi);
												sirket.setAd(sirketAdi.toUpperCase(Constants.TR_LOCALE));
												sirket.setDepartman(departman);
												sirket.setErpDurum(Boolean.TRUE);
												sirket.setDurum(Boolean.TRUE);
												sirket.setPdks(Boolean.TRUE);
												sirket.setOlusturanUser(olusturanUser);
												sirket.setOlusturmaTarihi(new Date());
												try {
													pdksEntityController.saveOrUpdate(session, entityManager, sirket);
													sirketMap.put(sirketKodu, sirket);
												} catch (Exception e) {
													sirket = personel.getSirket();
													durumUpdate = true;
												}
											} else {
												if (!sirket.isErp()) {
													if (olusturanUser == null)
														olusturanUser = ortakIslemler.getSistemAdminUser(session);
													sirket.setErpDurum(Boolean.TRUE);
													sirket.setGuncellemeTarihi(new Date());
													sirket.setGuncelleyenUser(olusturanUser);
													pdksEntityController.saveOrUpdate(session, entityManager, sirket);
												}
												sirketMap.put(sirketKodu, sirket);
											}

										}
										if (durumUpdate && personel.getId() != null && personel.getDurum()) {
											personel.setDurum(Boolean.FALSE);
											personel.setGuncellemeTarihi(new Date());
										}
									} catch (Exception e1) {
										logger.error(e1);
									}

									// personelMap.remove(sonucResultTable.getString("PERNR"));
									// continue;

								}
								try {
									if (sonucResultTable.getString("KOSTL") != null)
										masrafYeri = getTanim(masrafYeriMap, Tanim.TIPI_SAP_MASRAF_YERI, sonucResultTable.getString("KOSTL"), sonucResultTable.getString("KTEXT"), bagliMasrafYeri);
									if (masrafYeri != null && masrafYeri.isGuncellendi()) {
										if (yeni)
											session.clear();
										pdksEntityController.saveOrUpdate(session, entityManager, masrafYeri);
										session.flush();
									}

								} catch (Exception e) {
									logger.error("Pdks hata in : \n");
									e.printStackTrace();
									logger.error("Pdks hata out : " + e.getMessage());
								}

								personel.setMasrafYeri(masrafYeri);

								try {
									if (sonucResultTable.getString("ABKRS") != null)
										bordroAltAlan = getTanim(bordroAltBirimiMap, Tanim.TIPI_BORDRO_ALT_BIRIMI, sonucResultTable.getString("ABKRS"), sonucResultTable.getString("ABKRSTXT"), bagliBodroAltBirimi);
									if (bordroAltAlan != null && bordroAltAlan.isGuncellendi()) {
										if (yeni)
											session.clear();
										pdksEntityController.saveOrUpdate(session, entityManager, bordroAltAlan);
										session.flush();
									}

								} catch (Exception e) {
									logger.error("Pdks hata in : \n");
									e.printStackTrace();
									logger.error("Pdks hata out : " + e.getMessage());
								}

								personel.setBordroAltAlan(bordroAltAlan);
								if (personel.getAd() == null || !personel.getAd().trim().equals(sonucResultTable.getString("VORNA").trim()))
									personel.setAd(sonucResultTable.getString("VORNA").trim());
								if (personel.getSoyad() == null || !personel.getSoyad().trim().equals(sonucResultTable.getString("NACHN").trim()))
									personel.setSoyad(sonucResultTable.getString("NACHN").trim());

								personel.setSirket(sirket);

								try {
									personel.setGrubaGirisTarihi(sonucResultTable.getDate("GRBASTAR"));
									personel.setDogumTarihi(sonucResultTable.getDate("GBDAT"));
									personel.setIseBaslamaTarihi(sonucResultTable.getDate("SRBASTAR"));
									personel.setIzinHakEdisTarihi(sonucResultTable.getDate("TAZMTAR"));
									if (sonucResultTable.getDate("BITTAR") != null)
										personel.setIstenAyrilisTarihi(sonucResultTable.getDate("BITTAR"));
									else
										personel.setIstenAyrilisTarihi(PdksUtil.getSonSistemTarih());
								} catch (Exception e) {
									logger.error("Pdks hata in : \n");
									e.printStackTrace();
									logger.error("Pdks hata out : " + e.getMessage());

								}

							} while (sonucResultTable.nextRow());
							sonucResultTable.deleteAllRows();

						} else
							personelMap.clear();
					}
					girisTable.deleteAllRows();
				}

			} catch (Exception e) {
				if (function != null)
					PdksUtil.getXMLFunction3(jcoClient, function);
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());

			} finally {

			}
		}
		return personelMap;
	}

	public HashMap<String, Personel> topluHaldeYoneticiBulMap(int derinlik, ArrayList<String> personelNumaralariListesi, Date baslangicZamani, Date bitisZamani) throws Exception {
		return topluHaldeYoneticiBulMap(derinlik, personelNumaralariListesi, baslangicZamani, bitisZamani, null, null);
	}

	public LinkedHashMap topluHaldeIscileriVeriGetir(Session session, int icDerinlik, boolean yoneticiEkle, ArrayList<String> personelNo, Date baslangicZamani, Date bitisZamani, TreeMap bordroAltBirimiMap, TreeMap masrafYeriMap) throws Exception {
		ConnectionMan sapRfcManager = new ConnectionMan();
		if (baslangicZamani == null)
			baslangicZamani = PdksUtil.buGun();
		if (bitisZamani == null)
			bitisZamani = PdksUtil.getSonSistemTarih();
		LinkedHashMap personellerMap = new LinkedHashMap();
		LinkedHashMap<String, Personel> yoneticiMap = new LinkedHashMap<String, Personel>();

		JCoDestination jcoClient = sapRfcManager.getJCoDestination();
		JCoFunction function = null;
		if (jcoClient != null) {
			try {
				// logger.debug(personelNo + " üst yöneticileri aranıyor");
				yoneticiMap = topluHaldeYoneticiBulMap(2, personelNo, baslangicZamani, bitisZamani, jcoClient, sapRfcManager);

				function = jcoClient.getRepository().getFunction("ZHR_PDKS_ISCI_BUL2");

				JCoParameterList rfcTableList = function.getTableParameterList();
				JCoTable girisTable = rfcTableList.getTable("TABLO_GIR");
				JCoTable cikisTable = rfcTableList.getTable("TABLO");
				int sayac = 0;
				ArrayList<String> personelNumaralariListesi = (ArrayList<String>) personelNo.clone();
				personelNo.clear();
				while (!personelNumaralariListesi.isEmpty()) {
					++sayac;
					if (sayac <= icDerinlik) {
						personelNo.addAll(personelNumaralariListesi);
						for (String personelNumarasi : personelNumaralariListesi) {
							girisTable.appendRow();
							// logger.debug(personelNumarasi.trim() +
							// " altındaki personelller aranıyor");
							girisTable.setValue("PERNR", personelNumarasi.trim());
							girisTable.setValue("BEGDA", baslangicZamani);
						}

					}
					if (girisTable.getNumRows() > 0)
						PdksUtil.getXMLFunction3(jcoClient, function, new String[] { "TABLO_GIR", "TABLO" });
					personelNumaralariListesi.clear();
					String perNr = "", yonPerNr = "";
					if (cikisTable.getNumRows() > 0) {

						do {
							perNr = cikisTable.getString("PERNR");
							yonPerNr = cikisTable.getString("YONTPERNR");
							if (Integer.parseInt(perNr) == 0 || personellerMap.containsKey(perNr))
								continue;
							Personel personel = new Personel();
							personel.setAd(cikisTable.getString("VORNA"));
							personel.setSoyad(cikisTable.getString("NACHN"));
							personel.setErpSicilNo(perNr);
							personel.setYoneticisiAta((Personel) yoneticiMap.get(yonPerNr));
							personel.setDurum(Boolean.FALSE);
							personellerMap.put(perNr, personel);
							personelNumaralariListesi.add(perNr);

						} while (cikisTable.nextRow());

					}
					girisTable.deleteAllRows();
					cikisTable.deleteAllRows();

				}
				if (personellerMap.isEmpty()) {
					for (String sicilNo : personelNo) {
						personellerMap.put(sicilNo, new Personel());

					}
					topluHaldePersonelBilgisiGetir(session, bordroAltBirimiMap, masrafYeriMap, personellerMap, baslangicZamani, bitisZamani, sapRfcManager, jcoClient);
				}

				if (!personellerMap.isEmpty()) {
					if (!yoneticiMap.isEmpty()) {
						List<String> list = new ArrayList<String>(yoneticiMap.keySet());
						for (Iterator iterator = list.iterator(); iterator.hasNext();) {
							try {
								String sicilNo = (String) iterator.next();
								if (yoneticiMap.containsKey(sicilNo)) {
									Personel pdksPersonel = (Personel) yoneticiMap.get(sicilNo);
									Personel p = (Personel) pdksPersonel.clone();
									while (p.getPdksYonetici() != null) {
										String ysicil = p.getPdksYonetici().getSicilNo();
										if (PdksUtil.hasStringValue(ysicil)) {
											personelNo.add(ysicil);
											p = p.getPdksYonetici();
											yoneticiMap.put(ysicil, p);

										} else
											p = p.getPdksYonetici();

									}
								}
							} catch (Exception e) {
								logger.error("Pdks hata in : \n");
								e.printStackTrace();
								logger.error("Pdks hata out : " + e.getMessage());
								break;
							}
						}
					}

					LinkedHashMap<String, Personel> map = topluHaldeYoneticiBulMap(1, personelNo, baslangicZamani, bitisZamani, jcoClient, sapRfcManager);
					if (!yoneticiMap.isEmpty())
						map.putAll(yoneticiMap);
					for (Iterator iterator = personellerMap.keySet().iterator(); iterator.hasNext();) {
						String sicilNo = (String) iterator.next();
						Personel pdksPersonel = (Personel) personellerMap.get(sicilNo);
						if (map.containsKey(sicilNo)) {
							Personel pdksPersonel1 = (Personel) map.get(sicilNo);
							String yoneticiSicilNo = pdksPersonel1.getPdksYonetici() != null ? pdksPersonel1.getPdksYonetici().getSicilNo() : "";
							if (personellerMap.containsKey(yoneticiSicilNo))
								pdksPersonel.setYoneticisiAta((Personel) personellerMap.get(yoneticiSicilNo));
							else if (map.containsKey(sicilNo))
								pdksPersonel.setYoneticisiAta(((Personel) map.get(sicilNo)).getPdksYonetici());
							else if (yoneticiMap.containsKey(sicilNo))
								pdksPersonel.setYoneticisiAta(((Personel) yoneticiMap.get(sicilNo)).getPdksYonetici());
						}

					}

				}

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());

			} finally {

			}
		}
		if (yoneticiEkle && !yoneticiMap.isEmpty())
			personellerMap.put("0", yoneticiMap);

		return personellerMap;
	}

	/**
	 * @param derinlik
	 * @param personelNumaralariListesi
	 * @param baslangicZamani
	 * @param bitisZamani
	 * @param jcoClient
	 * @param sapRfcManager
	 * @return
	 * @throws Exception
	 */
	private LinkedHashMap<String, Personel> topluHaldeYoneticiBulMap(int derinlik, ArrayList<String> personelNumaralariListesi, Date baslangicZamani, Date bitisZamani, JCoDestination jcoClient, ConnectionMan sapRfcManager) throws Exception {
		if (sapRfcManager == null)
			sapRfcManager = new ConnectionMan();

		if (jcoClient == null)

			jcoClient = sapRfcManager.getJCoDestination();

		List<String> personelNumaralari = new ArrayList<String>();
		personelNumaralari.addAll(personelNumaralariListesi);

		if (baslangicZamani == null)
			baslangicZamani = PdksUtil.buGun();
		if (bitisZamani == null)
			bitisZamani = PdksUtil.getSonSistemTarih();

		LinkedHashMap<String, Personel> yoneticiListesi = new LinkedHashMap<String, Personel>();
		if (jcoClient != null) {
			try {

				JCoFunction function = jcoClient.getRepository().getFunction("ZHR_SAPKA_BUL");
				JCoParameterList rfcTableList = function.getTableParameterList();
				JCoTable girisTable = rfcTableList.getTable("TABLO_GIR");
				JCoTable cikisTable = rfcTableList.getTable("TABLO");
				LinkedHashMap<String, Personel> yoneticiMap = new LinkedHashMap<String, Personel>();
				boolean gir = Boolean.TRUE;
				int adet = 0;
				Personel personel = null;
				while (gir) {

					girisTable.deleteAllRows();
					cikisTable.deleteAllRows();
					for (String personelNumarasi : personelNumaralari) {
						girisTable.appendRow();
						girisTable.setValue("I_PERNR", personelNumarasi.trim());
						girisTable.setValue("I_DATE", bitisZamani);
					}
					personelNumaralari.clear();
					PdksUtil.getXMLFunction3(jcoClient, function);
					if (cikisTable.getNumRows() > 0) {
						++adet;
						Personel yoneticiPersonel = null;
						do {
							yoneticiPersonel = null;
							String yoneticiNo = Integer.parseInt(cikisTable.getString("O_PERNR").trim()) > 0 ? cikisTable.getString("O_PERNR").trim() : "";
							String sicilNo = cikisTable.getString("I_PERNR").trim();
							if (!yoneticiMap.containsKey(sicilNo)) {
								personel = new Personel();
								personel.setErpSicilNo(sicilNo);
							} else
								personel = (Personel) yoneticiMap.get(sicilNo);
							if (yoneticiMap.containsKey(yoneticiNo))
								yoneticiPersonel = (Personel) yoneticiMap.get(yoneticiNo);
							else {
								yoneticiPersonel = new Personel();
								yoneticiPersonel.setErpSicilNo(yoneticiNo);
								yoneticiMap.put(yoneticiNo, yoneticiPersonel);
							}
							personel.setYoneticisiAta(yoneticiPersonel);
							if (!personelNumaralari.contains(yoneticiNo))
								personelNumaralari.add(yoneticiNo);
							yoneticiListesi.put(sicilNo, personel);
						} while (cikisTable.nextRow());
						gir = derinlik < 0 || adet <= derinlik;
					} else
						gir = Boolean.FALSE;
				}

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());

			} finally {

			}
		}

		return yoneticiListesi;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abh.sap.action.SapController#pdksTanimsizPersonel(java.util.List, java.lang.String)
	 */
	public List<Personel> pdksTanimsizPersonel(TreeMap<String, Personel> perKayitliMap, String sapKodu, Session session) throws Exception {
		ConnectionMan sapRfcManager = new ConnectionMan();
		List<Personel> list = new ArrayList<Personel>();

		JCoDestination jcoClient = sapRfcManager.getJCoDestination();
		JCoFunction function = null;
		if (jcoClient != null) {
			function = jcoClient.getRepository().getFunction("ZHR_PERSONEL_VERILERI_GENEL");
			JCoParameterList rfcTableList = function.getTableParameterList();
			JCoTable cikanTable = rfcTableList.getTable("PERSTABLO");
			JCoParameterList impParametre = function.getImportParameterList();
			impParametre.setValue("BUKRS", sapKodu);
			impParametre.setValue("AYRILAN", "");
			impParametre.setValue("TARIH", PdksUtil.convertToJavaDate("99991231", "yyyyMMdd"));
			PdksUtil.getXMLFunction3(jcoClient, function, new String[] { "PERSTABLO" });
			if (cikanTable.getNumRows() > 0) {
				Boolean flush = Boolean.FALSE;
				TreeMap<String, Personel> perMap = new TreeMap<String, Personel>();
				HashMap<String, Tanim> pozisyonMap = new HashMap<String, Tanim>(), isMap = new HashMap<String, Tanim>(), yonKademeMap = new HashMap<String, Tanim>(), bolumMap = new HashMap<String, Tanim>();
				do {
					String perNo = cikanTable.getString("PERSNO");
					if (!perKayitliMap.containsKey(perNo)) {
						Personel personel = new Personel();
						personel.setPdksSicilNo(perNo);
						personel.setAd(cikanTable.getString("VORNA"));
						personel.setSoyad(cikanTable.getString("NACHN"));
						try {
							personel.setDogumTarihi(cikanTable.getDate("DOGTAR"));
							personel.setIseBaslamaTarihi(cikanTable.getDate("SIRK_TARIH"));
							personel.setGrubaGirisTarihi(cikanTable.getDate("GRUP_TARIH"));
							personel.setEkSaha1(getTanim(cikanTable.getString("POZISYON"), cikanTable.getString("POZ_TXT"), pozisyonMap));
							personel.setEkSaha2(getTanim(cikanTable.getString("IS"), cikanTable.getString("IS_TXT"), isMap));
							personel.setEkSaha3(getTanim(cikanTable.getString("KOSTL"), cikanTable.getString("LTEXT"), bolumMap));
							personel.setEkSaha4(getTanim(cikanTable.getString("YONKAD"), cikanTable.getString("YONKAD_TXT"), yonKademeMap));
						} catch (Exception e1) {
							logger.error(e1);
						}
						perMap.put(perNo, personel);

					}
				} while (cikanTable.nextRow());

				list.addAll(new ArrayList<Personel>(perMap.values()));
				perMap = null;
				if (flush)
					session.flush();
			}
		}
		return list;
	}

	/**
	 * @param map
	 * @param tipi
	 * @param kodu
	 * @param aciklama
	 * @param parentTanim
	 * @return
	 */
	private Tanim getTanim(TreeMap<String, Tanim> map, String tipi, String kodu, String aciklama, Tanim parentTanim) {
		Tanim tanim = null;
		kodu = kodu.trim();
		if (PdksUtil.hasStringValue(kodu)) {
			aciklama = aciklama.trim();
			if (map.containsKey(kodu)) {
				tanim = map.get(kodu);
				tanim.setGuncellendi(Boolean.FALSE);
			} else {
				tanim = new Tanim();
				tanim.setTipi(tipi);
				tanim.setKodu(kodu);
				tanim.setGuncellendi(Boolean.TRUE);
				tanim.setGuncelle(Boolean.FALSE);
			}
			if (tanim.getParentTanim() == null)
				tanim.setParentTanim(parentTanim);
			if (tanim.getId() != null)
				tanim.setGuncellendi(aciklama.equals(tanim.getAciklamatr()));
			tanim.setAciklamatr(aciklama);
			tanim.setAciklamaen(aciklama);

		}
		map.put(kodu, tanim);

		return tanim;
	}

	/**
	 * @param key
	 * @param value
	 * @param map
	 * @return
	 */
	private Tanim getTanim(String key, String value, HashMap<String, Tanim> map) {
		Tanim tanim = null;
		if (map != null) {
			if (map.containsKey(key))
				tanim = map.get(key);
			else {
				tanim = new Tanim();
				tanim.setKodu(value);
				tanim.setAciklamaen(value);
				tanim.setAciklamatr(value);
			}
		}
		return tanim;

	}

	public List<Personel> pdksTanimsizPersonel(List<String> perNoList, String sapKodu) throws Exception {

		return null;
	}

	public TreeMap<Long, String> setRFCIzinList(List<PersonelIzin> izinList) throws Exception {

		ConnectionMan sapRfcManager = new ConnectionMan();
		TreeMap<Long, String> map = null;
		JCoDestination jcoClient = sapRfcManager.getJCoDestination();
		if (jcoClient != null) {
			map = new TreeMap<Long, String>();
			try {
				JCoFunction function = jcoClient.getRepository().getFunction("ZHR_IZIN_YARAT");
				JCoParameterList ipl = function.getTableParameterList();
				JCoParameterList impParametre = function.getImportParameterList();
				JCoTable cikanTable = ipl.getTable("MESTAB");
				Calendar cal = Calendar.getInstance();
				for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
					PersonelIzin izin = (PersonelIzin) iterator.next();
					String mesaj = null;
					try {
						cikanTable.deleteAllRows();
						impParametre.setValue("PERNR", izin.getIzinSahibi().getSicilNo());
						String gidenSapKodu = izin.getIzinTipi().getIzinTipiTanim().getKodu();
						if (gidenSapKodu.indexOf("+") > -1)
							gidenSapKodu = PdksUtil.replaceAll(gidenSapKodu, "+", "");
						gidenSapKodu = PdksUtil.textBaslangicinaKarakterEkle(gidenSapKodu, '0', 4);
						impParametre.setValue("AWART", gidenSapKodu);
						boolean devamsizlik = Boolean.FALSE;
						if (izin.getIzinTipi().getIzinTipiTanim().getParentTanim().getKodu().equalsIgnoreCase("2001"))
							devamsizlik = Boolean.TRUE;
						impParametre.setValue("INFTY", izin.getIzinTipi().getIzinTipiTanim().getParentTanim().getKodu().trim());
						if (devamsizlik == Boolean.TRUE) {
							impParametre.setValue("PRM", "1");
						} else {

							impParametre.setValue("PRM", "2");
						}
						impParametre.setValue("BEGDA", izin.getBaslangicZamani());
						impParametre.setValue("ENDDA", ortakIslemler.tariheGunEkleCikar(cal, izin.getBitisZamani(), -1));
						PdksUtil.getXMLFunction3(jcoClient, function, new String[] { "MESTAB" });
						StringBuilder sb = new StringBuilder();
						if (cikanTable.getNumRows() > 0) {
							mesaj = "";
							do {
								if (!cikanTable.getString("MSGTYP").equals("S"))
									sb.append(cikanTable.getString("TEXT") + " ");
							} while (cikanTable.nextRow());

						} else
							mesaj = "ERP'den cevap alınamadı!";
						if (sb.length() > 0)
							mesaj = sb.toString();
						sb = null;
					} catch (Exception ex) {
						mesaj = "ERP'den cevap alınamadı!";
						if (ex.getMessage() != null)
							mesaj = ex.getMessage();
					}

					if (!PdksUtil.hasStringValue(mesaj))
						izin.setGuncellemeTarihi(new Date());
					izin.setMesaj(mesaj);
					map.put(izin.getId(), "");
					iterator.remove();
				}
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				if (e.getMessage() != null)
					PdksUtil.addMessageAvailableError(e.getMessage());
			} finally {

			}
		}
		return map;

	}

}
