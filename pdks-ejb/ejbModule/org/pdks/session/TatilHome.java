package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.pdks.security.entity.UserRoles;

import com.pdks.webservice.MailObject;

@Name("tatilHome")
public class TatilHome extends EntityHome<Tatil> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3927468770176440280L;
	/**
	 * 
	 */
	static Logger logger = Logger.getLogger(TatilHome.class);
	@RequestParameter
	Long pdksTatilId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(create = true)
	Renderer renderer;
	@In(required = false, create = true)
	HashMap parameterMap;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	private List<String> mesajList = new ArrayList<String>();
	private List<Tanim> tatilTanimList = new ArrayList<Tanim>();
	private List<Tatil> tatilList = new ArrayList<Tatil>();
	private List<SelectItem> ayList = new ArrayList<SelectItem>();
	private List<SelectItem> basGunList = new ArrayList<SelectItem>();
	private List<SelectItem> bitisGunList = new ArrayList<SelectItem>();
	private List<User> userList = new ArrayList<User>();
	private ArrayList<PersonelIzin> izinListesi;
	private Date tarih = Calendar.getInstance().getTime();
	private Boolean kaydetHatali = Boolean.FALSE, kopyala = Boolean.FALSE;
	private int yilSayisi = 1;
	private Tatil oldPdksTatil;
	private User islemYapan;
	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public List<Tanim> getTatilTanimList() {
		return tatilTanimList;
	}

	public void setTatilTanimList(List<Tanim> tatilTanimList) {
		this.tatilTanimList = tatilTanimList;
	}

	public List<Tatil> getTatilList() {
		return tatilList;
	}

	public void setTatilList(List<Tatil> value) {
		this.tatilList = value;
	}

	@Override
	public Object getId() {
		if (pdksTatilId == null) {
			return super.getId();
		} else {
			return pdksTatilId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public void tatilEkle() {
		Tatil tatil = new Tatil();
		tatil.setArifeSonraVardiyaDenklestirmeVar(!ortakIslemler.getParameterKeyHasStringValue("arifeSonraVardiyaDenklestirmeVar"));
		setInstance(tatil);
		setBasGunList(new ArrayList<SelectItem>());
		setBitisGunList(new ArrayList<SelectItem>());
		setOldPdksTatil(null);
		setKaydetHatali(Boolean.FALSE);
	}

	public void tipDegisti() {
		setKaydetHatali(Boolean.FALSE);
	}

	public String tarihDegisti() {
		Tatil pdksTatil = getInstance();
		Calendar cal = Calendar.getInstance();
		if (pdksTatil.getBitTarih() == null) {
			if (pdksTatil.getBasTarih() != null)
				pdksTatil.setBitTarih(ortakIslemler.tariheGunEkleCikar(cal, pdksTatil.getBasTarih(), pdksTatil.isYarimGunMu() ? 1 : 0));
		} else if (pdksTatil.getBasTarih() == null) {
			if (pdksTatil.getBitTarih() != null)
				pdksTatil.setBasTarih(ortakIslemler.tariheGunEkleCikar(cal, pdksTatil.getBitTarih(), pdksTatil.isYarimGunMu() ? -1 : 0));

		}

		return "";
	}

	@Transactional
	public String save() {
		setKaydetHatali(Boolean.FALSE);
		Tatil pdksTatil = getInstance();
		Calendar cal1 = Calendar.getInstance();
		Date bugun = cal1.getTime();
		long buGunLong = Long.parseLong(PdksUtil.convertToDateString(bugun, "yyyyMMdd"));
		int buYil = cal1.get(Calendar.YEAR);
		Calendar cal2 = Calendar.getInstance();
		String cikis = "";
		ArrayList<String> buffer = new ArrayList<String>();
		int bitYil = 2999, basYil = buYil;
		boolean iptalEdildi = false;
		String bs = null;
		try {

			if (pdksTatil.isPeriyodik()) {
				try {
					int basDonem = Integer.parseInt((String) pdksTatil.getBasAy()) * 100 + 100 + Integer.parseInt((String) pdksTatil.getBasGun());
					int bitDonem = Integer.parseInt((String) pdksTatil.getBitAy()) * 100 + 100 + Integer.parseInt((String) pdksTatil.getBitGun());
					if (bitDonem < basDonem)
						buffer.add("Başlangıç ay/gün bitiş ay/günden büyük olamaz");
					else {
						int yil = pdksTatil.getId() != null ? PdksUtil.getDateField(pdksTatil.getBasTarih(), Calendar.YEAR) : buYil;
						basYil = yil;
						cal1.set(yil, Integer.parseInt((String) pdksTatil.getBasAy()), Integer.parseInt((String) pdksTatil.getBasGun()), 0, 0);
						pdksTatil.setBasTarih(PdksUtil.getDate((Date) cal1.getTime()));
						if (pdksTatil.getId() == null && pdksTatil.getBasTarih().before(bugun)) {
							cal1.set(yil + 1, Integer.parseInt((String) pdksTatil.getBasAy()), Integer.parseInt((String) pdksTatil.getBasGun()), 0, 0);
							pdksTatil.setBasTarih(PdksUtil.getDate((Date) cal1.getTime()));
						}
						bitYil = 2999;
						if (pdksTatil.getDurum().equals(Boolean.FALSE)) {
							int bitisYil = PdksUtil.getDateField(pdksTatil.getBitTarih(), Calendar.YEAR);
							if (bitisYil != bitYil) {
								bitYil = bitisYil;
							} else {
								iptalEdildi = true;
								bitYil = buYil;
								long bitisTarihi = (bitYil * 10000) + (Long.parseLong(pdksTatil.getBitAy().toString()) * 100) + Long.parseLong(pdksTatil.getBitGun().toString());
								if (buGunLong < bitisTarihi)
									--bitYil;

							}
						}
						cal2.set(bitYil, Integer.parseInt((String) pdksTatil.getBitAy()), Integer.parseInt((String) pdksTatil.getBitGun()));
						String pattern = "yyyyMMdd";
						bs = PdksUtil.convertToDateString(cal2.getTime(), pattern) + " 23:59:59";
						pdksTatil.setBitTarih(PdksUtil.convertToJavaDate(bs, pattern + " HH:mm:ss"));

					}
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					buffer.add("Tarihleri seçiniz");
					pdksTatil.setBitTarih(null);
					pdksTatil.setBasTarih(null);
				}
				if (pdksTatil.getId() != null && !pdksTatil.getDurum()) {
					cal1.setTime(pdksTatil.getBitTarih());
					cal1.set(Calendar.YEAR, buYil);
					if (cal1.getTime().after(bugun)) {
						--buYil;
						cal1.set(Calendar.YEAR, buYil);
					}
					Date bitisTarih = cal1.getTime();
					pdksTatil.setBitTarih(bitisTarih);
				}
			}
			Date basTarih = null;
			Date bitTarih = null;

			if (buffer.isEmpty()) {
				if ((pdksTatil.getId() == null || pdksTatil.isTekSefer()) && PdksUtil.tarihKarsilastirNumeric(pdksTatil.getBasTarih(), pdksTatil.getBitTarih()) == 1)
					buffer.add("Başlangıç tarihi bitiş tarihinden büyük olamaz");
				else if (authenticatedUser.isAdmin() == false && pdksTatil.getDurum() && PdksUtil.tarihKarsilastirNumeric(pdksTatil.getBasTarih(), Calendar.getInstance().getTime()) != 1)
					buffer.add("Geçmişe ait tatil giremezsiniz");
				else {
					cal1 = Calendar.getInstance();
					basTarih = PdksUtil.getDate((Date) pdksTatil.getBasTarih().clone());
					bitTarih = PdksUtil.getGunSonu((Date) pdksTatil.getBitTarih().clone());
					if (pdksTatil.isPeriyodik()) {
						int yil = cal1.get(Calendar.YEAR);
						basTarih = PdksUtil.setTarih(basTarih, Calendar.YEAR, yil);
						bitTarih = PdksUtil.setTarih(bitTarih, Calendar.YEAR, yil);
					}
					if (pdksTatil.isYarimGunMu()) {
						if (pdksTatil.getId() == null && pdksTatil.getOrjTatil() == null)
							pdksTatil.setArifeSonraVardiyaDenklestirmeVar(pdksTatil.isYarimGunMu());
						int saat = 13, dakika = 0;
						String yarimGunStr = (parameterMap.containsKey("yarimGunSaati") ? (String) parameterMap.get("yarimGunSaati") : "");
						if (yarimGunStr.indexOf(":") > 0) {
							StringTokenizer st = new StringTokenizer(yarimGunStr, ":");
							if (st.countTokens() == 2) {
								try {
									saat = Integer.parseInt(st.nextToken().trim());
								} catch (Exception e) {
									logger.error("PDKS hata in : \n");
									e.printStackTrace();
									logger.error("PDKS hata out : " + e.getMessage());
									saat = 13;
								}
								try {
									dakika = Integer.parseInt(st.nextToken().trim());
								} catch (Exception e) {
									logger.error("PDKS hata in : \n");
									e.printStackTrace();
									logger.error("PDKS hata out : " + e.getMessage());
									saat = 13;
									dakika = 0;
								}
							}
						}
						basTarih = PdksUtil.setTarih(basTarih, Calendar.HOUR_OF_DAY, saat);
						basTarih = PdksUtil.setTarih(basTarih, Calendar.MINUTE, dakika);
					}
					bs = PdksUtil.convertToDateString(bitTarih, "yyyyMMdd HH:mm:ss");
					if (oldPdksTatil == null || !oldPdksTatil.getDurum().equals(pdksTatil.getDurum()) || (pdksTatil.isTekSefer() && (basTarih.getTime() != oldPdksTatil.getBasTarih().getTime() || !bs.equals(PdksUtil.convertToDateString(oldPdksTatil.getBitTarih(), "yyyyMMdd HH:mm:ss"))))) {
						HashMap parametreMap = new HashMap();
						parametreMap.put("baslangicZamani<=", bitTarih);
						parametreMap.put("bitisZamani>=", basTarih);
						ArrayList durumList = new ArrayList();
						durumList.add(PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL);
						durumList.add(PersonelIzin.IZIN_DURUMU_REDEDILDI);
						durumList.add(PersonelIzin.IZIN_DURUMU_SAP_GONDERILDI);
						parametreMap.put("izinDurumu not", durumList);
						parametreMap.put("izinTipi.personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
						parametreMap.put("izinTipi.takvimGunumu<>", Boolean.TRUE);
						parametreMap.put("izinTipi.bakiyeIzinTipi=", null);
						if (session != null)
							parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						List<PersonelIzin> list = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, PersonelIzin.class);
						boolean izinERPUpdate = ortakIslemler.getParameterKey("izinERPUpdate").equals("1");
						if (!izinERPUpdate)
							list.clear();
						if (!list.isEmpty()) {
							HashMap<Long, ArrayList<PersonelIzin>> izinDepartmanMap = new HashMap<Long, ArrayList<PersonelIzin>>();
							for (PersonelIzin personelIzin : list) {
								long departmanId = personelIzin.getIzinSahibi().getSirket().getDepartman().getId();
								ArrayList<PersonelIzin> izinList = izinDepartmanMap.containsKey(departmanId) ? izinDepartmanMap.get(departmanId) : new ArrayList<PersonelIzin>();
								izinList.add(personelIzin);
								izinDepartmanMap.put(departmanId, izinList);
							}
							List idList = new ArrayList(izinDepartmanMap.keySet());
							String fieldName = "user.departman.id";
							parametreMap.clear();
							parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "user");
							parametreMap.put("role.rolename", Role.TIPI_IK);
							parametreMap.put(fieldName, idList);
							parametreMap.put("user.durum", Boolean.TRUE);
							parametreMap.put("user.pdksPersonel.durum", Boolean.TRUE);
							if (session != null)
								parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
							// List ikList = pdksEntityController.getObjectByInnerObjectList(parametreMap, UserRoles.class);
							List ikList = ortakIslemler.getParamList(false, idList, fieldName, parametreMap, UserRoles.class, session);
							for (Iterator<Long> iterator = izinDepartmanMap.keySet().iterator(); iterator.hasNext();) {
								Long departmanId = iterator.next();
								ArrayList<PersonelIzin> izinList = izinDepartmanMap.get(departmanId);
								setIzinListesi(izinList);
								userList.clear();
								for (Iterator<User> iterator2 = ikList.iterator(); iterator2.hasNext();) {
									User ik = iterator2.next();
									if (departmanId.equals(ik.getDepartman().getId())) {
										userList.add(ik);
										iterator2.remove();
									}

								}
								for (Iterator<PersonelIzin> iterator2 = list.iterator(); iterator2.hasNext();) {
									PersonelIzin personelIzin = iterator2.next();
									if (departmanId.equals(personelIzin.getIzinSahibi().getSirket().getDepartman().getId())) {
										if (personelIzin.getOlusturanUser() != null && !personelIzin.getOlusturanUser().isIK())
											userList.add(personelIzin.getOlusturanUser());
										iterator2.remove();
									}

								}

								try {
									MailObject mail = new MailObject();
									mail.setSubject("Tatil Tanımlama");
									StringBuffer body = new StringBuffer("<p>Girdiğiniz izin ile aynı tarihe resmi yada genel tatil tanımlaması yapılmıştır. İzni silip, tekrardan yaratınız.</p><p></p>");
									body.append("<table><thead><tr>");
									body.append("<th><b>" + ortakIslemler.personelNoAciklama() + "</b></th>");
									body.append("<th><b>İzin Sahibi</b></th>");
									body.append("<th><b>Tipi</b></th>");
									body.append("<th><b>Başlangıç Zamanı</b></th>");
									body.append("<th><b>Bitiş Zamanı</b></th></tr></thead><tbody>");
									for (PersonelIzin izin : izinListesi) {
										Personel isahibi = izin.getIzinSahibi();
										body.append("<tr>");
										body.append("<td align='center'>" + isahibi.getPdksSicilNo() + "</th>");
										body.append("<td>" + isahibi.getAdSoyad() + "</th>");
										body.append("<td>" + izin.getIzinTipiAciklama() + "</th>");
										body.append("<td align='center'>" + authenticatedUser.dateTimeFormatla(izin.getBaslangicZamani()) + "</th>");
										body.append("<td align='center'>" + authenticatedUser.dateTimeFormatla(izin.getBitisZamani()) + "</th>");
										body.append("</tr>");
									}
									body.append("</tbody></table>");
									mail.setBody(body.toString());

									ortakIslemler.addMailPersonelUserList(userList, mail.getToList());

									ortakIslemler.mailSoapServisGonder(true, mail, renderer, "/email/tatilUyariMail.xhtml", session);

								} catch (Exception e) {
									logger.error("PDKS hata in : \n");
									e.printStackTrace();
									logger.error("PDKS hata out : " + e.getMessage());
									PdksUtil.addMessageError(e.getMessage());
								}

							}
						}
					}

				}

			}
			if (!pdksTatil.isYarimGunMu())
				pdksTatil.setArifeSonraVardiyaDenklestirmeVar(null);
			if (!buffer.isEmpty()) {
				for (String string : buffer)
					PdksUtil.addMessageWarn(string);
				setKaydetHatali(Boolean.TRUE);
				cikis = "";
			} else {
				if (pdksTatil.getId() == null || pdksTatil.isTekSefer() || iptalEdildi) {
					if (pdksTatil.isPeriyodik()) {
						basTarih = PdksUtil.setTarih(basTarih, Calendar.YEAR, basYil);
						bitTarih = PdksUtil.setTarih(bitTarih, Calendar.YEAR, bitYil);
					}
					pdksTatil.setBasTarih(basTarih);
					pdksTatil.setBitTarih(bitTarih);
				}
				if (pdksTatil.getId() == null) {
					pdksTatil.setOlusturanUser(authenticatedUser);
					pdksTatil.setOlusturmaTarihi(new Date());
				} else {
					pdksTatil.setGuncelleyenUser(authenticatedUser);
					pdksTatil.setGuncellemeTarihi(new Date());
				}
				if (pdksTatil.getId() == null || pdksTatil.getBasTarih().before(pdksTatil.getBitTarih())) {
					if (pdksTatil.getId() != null && pdksTatil.isPeriyodik())
						pdksTatil.setDurum(Boolean.TRUE);
					pdksEntityController.saveOrUpdate(session, entityManager, pdksTatil);
				} else {
					pdksEntityController.deleteObject(session, entityManager, pdksTatil);
				}

				session.flush();
				fillPdksTatilList();
				cikis = "persist";

			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		return cikis;
	}

	public void fillPdksTatilList() {
		session.clear();
		HashMap parametreMap = new HashMap();
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Tatil> list = pdksEntityController.getObjectByInnerObjectList(parametreMap, Tatil.class);
		Calendar cal = Calendar.getInstance();
		cal.setTime(tarih);
		Integer seciliYil = cal.get(Calendar.YEAR);
		for (Iterator<Tatil> iterator = list.iterator(); iterator.hasNext();) {
			Tatil pdksTatil = iterator.next();
			if (PdksUtil.tarihKarsilastirNumeric(tarih, pdksTatil.getBitTarih()) == 1) {
				iterator.remove();
			} else {
				if (pdksTatil.isPeriyodik()) {
					int yil = seciliYil;
					cal.setTime(pdksTatil.getBasTarih());
					cal.set(Calendar.YEAR, yil);
					Date basTarih = cal.getTime();
					if (basTarih.before(tarih)) {
						cal.set(Calendar.YEAR, ++yil);
						basTarih = cal.getTime();
					}
					pdksTatil.setBasGun(basTarih);
					cal.setTime(pdksTatil.getBitTarih());
					cal.set(Calendar.YEAR, yil);
					Date bitTarih = cal.getTime();
					if (bitTarih.before(tarih) || bitTarih.before(basTarih)) {
						cal.set(Calendar.YEAR, ++yil);
						bitTarih = cal.getTime();
					}
					pdksTatil.setBitGun(bitTarih);
				} else {
					pdksTatil.setBasGun(pdksTatil.getBasTarih());
					pdksTatil.setBitGun(pdksTatil.getBitTarih());
				}
			}

		}
		if (list.size() > 1)
			list = PdksUtil.sortListByAlanAdi(list, "bitGun", false);
		for (Tatil pdksTatil : list) {
			pdksTatil.setBasGun(null);
			pdksTatil.setBitGun(null);
		}
		setTatilList(list);
	}

	public void fillTatilTipiTanimList() {
		List<Tanim> tanimList = null;
		HashMap parametreMap = new HashMap();
		parametreMap.put("tipi", Tanim.TIPI_TATIL_TIPI);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			tanimList = pdksEntityController.getObjectByInnerObjectList(parametreMap, Tanim.class);

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		setTatilTanimList(tanimList);
	}

	public void fillAyList() {
		List<SelectItem> list = PdksUtil.getAyListesi(Boolean.FALSE);
		setAyList(list);
	}

	public void fillGunBasList() {
		Tatil pdksTatil = getInstance();
		try {
			String ay = (String) pdksTatil.getBasAy();
			setBasGunList(fillGunList(new Integer(ay)));

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			setBasGunList(new ArrayList<SelectItem>());
		}
	}

	public void fillGunBitisList() {
		Tatil pdksTatil = getInstance();
		try {
			String ay = (String) pdksTatil.getBitAy();
			setBitisGunList(fillGunList(new Integer(ay)));

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			setBitisGunList(new ArrayList<SelectItem>());
		}
	}

	public List<SelectItem> fillGunList(Integer deger) {
		List<SelectItem> list = new ArrayList<SelectItem>();
		if (deger != null) {
			int bitis = 31;
			int ay = deger.intValue() + 1;
			if (ay == 4 || ay == 6 || ay == 9 || ay == 11)
				bitis = 30;
			else if (ay == 2)
				bitis = 29;
			for (int i = 1; i <= bitis; i++)
				list.add(new SelectItem(String.valueOf(i)));

		}
		return list;

	}

	/**
	 * @param pdksTatil
	 * @return
	 */
	public String kayitKopyala(Tatil pdksTatil) {
		setInstance(pdksTatil);
		yilSayisi = 1;
		kopyala = Boolean.TRUE;
		if (!ortakIslemler.getParameterKeyHasStringValue("cokluTatilKopyala"))
			kayitKopyalaDevam();
		return "";
	}

	@Transactional
	public String kayitKopyalaDevam() {
		Tatil pdksTatil = getInstance();
		kopyala = yilSayisi > 1;
		boolean flush = false;
		Date olusturmaTarihi = kopyala ? new Date() : null;
		session.clear();
		for (int i = 0; i < yilSayisi; i++) {
			pdksTatil = periyodikOlmayanTatilKopyala(pdksTatil);
			if (kopyala && pdksTatil.getId() == null) {
				flush = true;
				pdksTatil.setOlusturmaTarihi(olusturmaTarihi);
				pdksTatil.setOlusturanUser(authenticatedUser);
				pdksTatil.setDurum(Boolean.TRUE);
				pdksEntityController.saveOrUpdate(session, entityManager, pdksTatil);
			}
		}
		if (!kopyala) {
			pdksTatil.setOrjTatil(getInstance());
			kayitGuncelle(pdksTatil);
		} else {
			if (flush)
				session.flush();
			fillPdksTatilList();
		}

		return "";
	}

	/**
	 * @param pdksTatil
	 * @return
	 */
	private Tatil periyodikOlmayanTatilKopyala(Tatil tatil) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(tatil.getBasTarih());
		int arti = 354;
		cal.add(Calendar.DATE, arti);
		int yil = cal.get(Calendar.YEAR);
		if (yil % 4 == 0) {
			++arti;
			cal.setTime(tatil.getBasTarih());
			cal.add(Calendar.DATE, arti);
		}
		// logger.info(yil + " " + arti);
		Date basTarih = (Date) cal.getTime().clone();
		HashMap parametreMap = new HashMap();
		parametreMap.put("basTarih", basTarih);
		parametreMap.put("tatilTipi.id", tatil.getTatilTipi().getId());
		parametreMap.put("durum", Boolean.TRUE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Tatil> tatilList = pdksEntityController.getObjectByInnerObjectList(parametreMap, Tatil.class);
		Tatil tatilYeni = null;
		if (tatilList.isEmpty()) {
			tatilYeni = (Tatil) tatil.clone();
			tatilYeni.setId(null);
			tatilYeni.setBasTarih(basTarih);
			cal.setTime(tatil.getBitTarih());
			cal.add(Calendar.DATE, arti);
			tatilYeni.setYarimGun(Boolean.TRUE);
			tatilYeni.setBitTarih((Date) cal.getTime().clone());
			tatilYeni.setOlusturanUser(null);
			tatilYeni.setOlusturmaTarihi(null);
			tatilYeni.setGuncellemeTarihi(null);
			tatilYeni.setGuncelleyenUser(null);
			tatilYeni.setAciklama(yil + " Yılı " + tatil.getAd());
			tatilYeni.setDurum(Boolean.FALSE);
		} else
			tatilYeni = tatilList.get(0);

		return tatilYeni;
	}

	/**
	 * @param pdksTatil
	 */
	public void kayitGuncelle(Tatil pdksTatil) {
		fillTatilTipiTanimList();
		kopyala = Boolean.FALSE;
		if (pdksTatil == null) {
			pdksTatil = new Tatil();
			for (Tanim tatilTipi : tatilTanimList) {
				pdksTatil.setTatilTipi(tatilTipi);
				if (pdksTatil.isTekSefer())
					break;
			}
			setBasGunList(new ArrayList<SelectItem>());
			setBitisGunList(new ArrayList<SelectItem>());
			setOldPdksTatil(null);
			setKaydetHatali(Boolean.FALSE);
		} else
			setOldPdksTatil((Tatil) pdksTatil.clone());

		if (pdksTatil.getTatilTipi() != null && pdksTatil.isPeriyodik()) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(pdksTatil.getBasTarih());
			pdksTatil.setBasAy(String.valueOf(cal.get(Calendar.MONTH)));
			pdksTatil.setBasGun(String.valueOf(cal.get(Calendar.DATE)));
			cal.setTime(pdksTatil.getBitTarih());
			pdksTatil.setBitAy(String.valueOf(cal.get(Calendar.MONTH)));
			pdksTatil.setBitGun(String.valueOf(cal.get(Calendar.DATE)));
			fillGunBasList();
			fillGunBitisList();
		}

		if (pdksTatil.getTatilTipi() != null && tatilTanimList != null) {
			Long id = pdksTatil.getTatilTipi().getId();
			for (Tanim tatilTipi : tatilTanimList) {
				if (tatilTipi.getId().equals(id))
					pdksTatil.setTatilTipi(tatilTipi);

			}
		}
		setInstance(pdksTatil);
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
		setIslemYapan(authenticatedUser);
		fillPdksTatilList();
		fillAyList();
	}

	public List<SelectItem> getAyList() {
		return ayList;
	}

	public void setAyList(List<SelectItem> ayList) {
		this.ayList = ayList;
	}

	public List<SelectItem> getBasGunList() {
		return basGunList;
	}

	public void setBasGunList(List<SelectItem> basGunList) {
		this.basGunList = basGunList;
	}

	public List<SelectItem> getBitisGunList() {
		return bitisGunList;
	}

	public void setBitisGunList(List<SelectItem> bitisGunList) {
		this.bitisGunList = bitisGunList;
	}

	public Boolean getKaydetHatali() {
		return kaydetHatali;
	}

	public void setKaydetHatali(Boolean kaydetHatali) {
		this.kaydetHatali = kaydetHatali;
	}

	public Date getTarih() {
		return tarih;
	}

	public void setTarih(Date tarih) {
		this.tarih = tarih;
	}

	public Tatil getOldPdksTatil() {
		return oldPdksTatil;
	}

	public void setOldPdksTatil(Tatil oldPdksTatil) {
		this.oldPdksTatil = oldPdksTatil;
	}

	public List<User> getUserList() {
		return userList;
	}

	public void setUserList(List<User> userList) {
		this.userList = userList;
	}

	public List<String> getMesajList() {
		return mesajList;
	}

	public void setMesajList(List<String> mesajList) {
		this.mesajList = mesajList;
	}

	public User getIslemYapan() {
		return islemYapan;
	}

	public void setIslemYapan(User islemYapan) {
		this.islemYapan = islemYapan;
	}

	public ArrayList<PersonelIzin> getIzinListesi() {
		return izinListesi;
	}

	public void setIzinListesi(ArrayList<PersonelIzin> izinListesi) {
		this.izinListesi = izinListesi;
	}

	public Boolean getKopyala() {
		return kopyala;
	}

	public void setKopyala(Boolean kopyala) {
		this.kopyala = kopyala;
	}

	public int getYilSayisi() {
		return yilSayisi;
	}

	public void setYilSayisi(int yilSayisi) {
		this.yilSayisi = yilSayisi;
	}

}
