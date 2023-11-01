package org.pdks.session;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.validator.Max;
import org.hibernate.validator.Min;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Departman;
import org.pdks.entity.Dosya;
import org.pdks.entity.FileUpload;
import org.pdks.entity.IzinIstirahat;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.IzinTipiBirlesikHaric;
import org.pdks.entity.IzinTipiMailAdres;
import org.pdks.entity.OnaylanmamisIzinIKView;
import org.pdks.entity.PdksPersonelView;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelGeciciYonetici;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.PersonelIzinDetay;
import org.pdks.entity.PersonelIzinDosya;
import org.pdks.entity.PersonelIzinOnay;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.entity.TempIzin;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.VardiyaHafta;
import org.pdks.entity.YemekIzin;
import org.pdks.security.entity.User;
import org.pdks.security.entity.UserVekalet;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

import com.pdks.webservice.MailObject;
import com.pdks.webservice.MailPersonel;
import com.pdks.webservice.MailStatu;

@Name("personelIzinGirisiHome")
public class PersonelIzinGirisiHome extends EntityHome<PersonelIzin> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3764693434397792868L;
	static Logger logger = Logger.getLogger(PersonelIzinGirisiHome.class);

	@RequestParameter
	Long personelIzinId;
	@RequestParameter
	String mId;

	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false)
	User authenticatedUser;
	@In(required = true, create = true)
	Renderer renderer;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	@Out(required = false, scope = ScopeType.SESSION)
	PersonelIzin sessionIzin;
	@In(required = false, create = true)
	String linkAdres;

	private User seciliUser;
	private Tanim redSebebiTanim;
	private PersonelIzinOnay redOnay;
	private PersonelIzinDosya izinDosya;

	private Tanim seciliDepartman;
	private List<String> roleList;
	private Sirket seciliSirket;

	private List<Tanim> departmanList = new ArrayList<Tanim>();

	private List<SelectItem> sirketItemList = new ArrayList<SelectItem>();
	private List<Sirket> sirketList = new ArrayList<Sirket>();

	private List<User> userList = new ArrayList<User>();
	private Personel seciliPersonel, arananPersonel, listelenPersonel;

	boolean sapDepartman;

	boolean pdksDepartman;

	private boolean userArama = Boolean.FALSE;

	private boolean visibled = Boolean.FALSE;

	private boolean personelArama = Boolean.TRUE, servisAktarDurum = Boolean.FALSE;

	private boolean checkBox, izinERPGiris = Boolean.FALSE;

	private boolean checkBoxDurum;

	private String reRender, bolumAciklama;

	private Sirket pdksSirket;

	private AramaSecenekleri aramaSecenekleri = null, aramaListeSecenekleri = null;

	private List<IzinTipi> izinTipiList, guncellemeIzinTipiList = new ArrayList<IzinTipi>(), izinTipleri = new ArrayList<IzinTipi>();
	private List<PersonelIzinOnay> onayimaGelenIzinler = new ArrayList<PersonelIzinOnay>();
	private List<Tanim> redSebebiList = new ArrayList<Tanim>(), gorevTipiList;
	private List<PersonelIzin> personelIzinList = new ArrayList<PersonelIzin>();
	private List<PersonelIzinDetay> personelIzinler = new ArrayList<PersonelIzinDetay>();
	private List<String> roller, izinOnayMailList;
	private int kidemYil = 0, kidemAy = 0, kidemGun = 0, yasYil, yasAy, yasGun, baslangicSaat, baslangicDakika, bitisSaat, bitisDakika;
	private boolean saatGosterilecek, saatGunOpsiyon = Boolean.FALSE, fazlaMesaiOpsiyon = Boolean.FALSE, saatGosterilecek2;
	private boolean saatGunOpsiyon2 = Boolean.FALSE, izinIptalGoster = Boolean.FALSE, girisSSK = Boolean.FALSE, nedenSor = Boolean.FALSE;
	private int hesapTipi, seciliHesapTipi;
	public boolean mesaiCheck, basarili, guncelle = Boolean.FALSE;
	public Boolean bakiyeTipiSenelik;
	private String ad = "", soyad = "", sicilNo = "", onayDurum, adres, currentFilterValue, mailKonu = "İzin Ret", izinIptal = "rededilmiştir", redSebebi, izinMailAciklama, dosyaTipleri, donusAdres = "";

	private IzinIstirahat istirahat;
	private TreeMap<Long, User> vekilYoneticiMap;
	private List<SelectItem> istirahatKaynakList = new ArrayList<SelectItem>();
	private List<Personel> personelList = new ArrayList<Personel>();

	public List<User> toList = new ArrayList<User>(), ccList = new ArrayList<User>(), bccList = new ArrayList<User>();
	public List<String> ccMailList = new ArrayList<String>(), bccMailList = new ArrayList<String>();
	private Date filtreBaslangicZamani, filtreBitisZamani, date, basDate, bitDate;
	private PersonelIzin izin, guncellenecekIzin, mailIzin;
	private Tanim ekSaha1, ekSaha2, ekSaha3, ekSaha4;
	private TreeMap<Long, IzinIstirahat> izinIstirahatMap;
	private TreeMap<Long, PersonelIzinDosya> izinDosyaMap;
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private List<TempIzin> tempIzinList;

	private Personel izinliSahibi;
	private IzinTipi seciliIzinTipi;

	@RequestParameter(value = "personelId")
	private String personelId;
	@RequestParameter(value = "bastarih")
	String bastarih;
	@RequestParameter(value = "izimId")
	String izinId;

	@In(required = false)
	FacesMessages facesMessages;

	private ArrayList<FileUpload> files = new ArrayList<FileUpload>();
	private ArrayList<String> sicilNoList;
	private ArrayList<PersonelView> excelList = new ArrayList<PersonelView>();
	private Date sistemTarihi;

	/**
	 * @param izinOnay
	 * @return
	 * @throws Exception
	 */
	public List yilBasiIzinParcala(PersonelIzinOnay izinOnay) throws Exception {
		PersonelIzin yilBasiIzin = izinOnay.getPersonelIzin();
		List<Personel> perList = new ArrayList<Personel>();
		perList.add(yilBasiIzin.getIzinSahibi());
		TreeMap<String, Tatil> tatilMap = ortakIslemler.getTatilGunleri(perList, yilBasiIzin.getBaslangicZamani(), yilBasiIzin.getBitisZamani(), session);
		List saveList = null;
		Personel izinSahibi = yilBasiIzin.getIzinSahibi();
		if (tatilMap != null) {
			String key = PdksUtil.convertToDateString(yilBasiIzin.getBitisZamani(), "yyyy") + "0101";
			if (tatilMap.containsKey(key)) {
				double izinSuresi = PdksUtil.setDoubleRounded(yilBasiIzin.getIzinSuresi() * 10, 0, BigDecimal.ROUND_HALF_DOWN) / 10.d;
				if (izinSuresi > 0.0d) {
					List<Personel> personeller = new ArrayList<Personel>();
					personeller.add(izinSahibi);
					TreeMap<String, VardiyaGun> vardiyalar = ortakIslemler.getVardiyalar(personeller, PdksUtil.tariheGunEkleCikar(yilBasiIzin.getBaslangicZamani(), -7), PdksUtil.tariheGunEkleCikar(yilBasiIzin.getBitisZamani(), 1), Boolean.FALSE, session, Boolean.TRUE);
					try {
						Date guncellemeTarihi = new Date();
						Tatil tatil = tatilMap.get(key);
						String dateStr = PdksUtil.convertToDateString(tatil.getBitTarih(), "yyyyddMM") + PdksUtil.convertToDateString(yilBasiIzin.getBaslangicZamani(), "HHmm");
						Date ortakTarihi = PdksUtil.convertToJavaDate(dateStr, "yyyyddMMHHmm");
						PersonelIzin izin1 = (PersonelIzin) yilBasiIzin.kopyala();
						izin1.setId(null);
						izin1.setIzinDurumu(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
						izin1.setGuncellemeTarihi((Date) guncellemeTarihi.clone());
						izin1.setGuncelleyenUser(authenticatedUser);
						izin1.setHakEdisIzinler(null);
						izin1.setOnaylayanlar(null);
						izin1.setPersonelIzinler(null);
						PersonelIzin izin2 = (PersonelIzin) izin1.kopyala();
						izin1.setBitisZamani(ortakTarihi);
						izin2.setBaslangicZamani(ortakTarihi);
						try {
							double sure1 = izinSaatSuresiHesapla(vardiyalar, izin1, izin1.getIzinTipi().getHesapTipi(), tatilMap);
							double sure2 = izinSaatSuresiHesapla(vardiyalar, izin2, izin2.getIzinTipi().getHesapTipi(), tatilMap);
							if (sure1 != 0.0d && sure2 != 0.0d) {
								if (sure1 + sure2 != izinSuresi)
									sure2 = izinSuresi - sure1;
								PersonelIzin hakedesIzin = null;
								HashMap map = new HashMap();
								map.put("personelIzin.id =", yilBasiIzin.getId());
								map.put(PdksEntityController.MAP_KEY_SESSION, session);
								List<PersonelIzinDetay> list = pdksEntityController.getObjectByInnerObjectListInLogic(map, PersonelIzinDetay.class);
								if (!list.isEmpty())
									hakedesIzin = list.get(0).getHakEdisIzin();
								PersonelIzin hakedesIzin1 = getHakEdisIzin(izin1);
								if (hakedesIzin1 == null)
									hakedesIzin1 = hakedesIzin;
								PersonelIzin hakedesIzin2 = getHakEdisIzin(izin2);
								if (hakedesIzin2 == null)
									hakedesIzin2 = hakedesIzin;
								if (hakedesIzin2 != null) {
									saveList = new ArrayList();
									izin1.setIzinSuresi(sure1);
									izin2.setIzinSuresi(sure2);
									yilBasiIzin.setAciklama(izin1.getAciklama() + " (Yılbaşı İptal)");
									saveList.add(yilBasiIzin);
									saveList.add(izin1);
									saveList.add(izin2);
									map.clear();
									map.put("personelIzin.id =", yilBasiIzin.getId());
									map.put("onaylayanTipi<>", PersonelIzinOnay.ONAYLAYAN_TIPI_IK);
									map.put(PdksEntityController.MAP_KEY_SESSION, session);
									List<PersonelIzinOnay> onayIzinler = pdksEntityController.getObjectByInnerObjectListInLogic(map, PersonelIzinOnay.class);
									PersonelIzinOnay izinOnaySon = null;
									if (!onayIzinler.isEmpty()) {
										if (onayIzinler.size() > 1)
											onayIzinler = PdksUtil.sortListByAlanAdi(onayIzinler, "onaylayanTipi", false);
										if (izinOnay.getId() == null)
											izinOnaySon = onayIzinler.get(onayIzinler.size() - 1);
									}
									if (izinOnay.getId() == null) {
										if (izinOnaySon != null) {
											izinOnay.setOlusturmaTarihi(izinOnaySon.getSonIslemTarihi());
											izinOnay.setOlusturanUser(izinOnaySon.getSonIslemYapan());
										} else {
											izinOnay.setOlusturmaTarihi(izin1.getSonIslemTarihi());
											izinOnay.setOlusturanUser(izin1.getSonIslemYapan());
										}
									}

									onayIzinler.add(izinOnay);
									for (PersonelIzinOnay onay : onayIzinler) {
										PersonelIzinOnay izinOnay1 = (PersonelIzinOnay) onay.kopyala();
										izinOnay1.setId(null);
										if (izinOnay1.getOnaylayanTipi().equals(PersonelIzinOnay.ONAYLAYAN_TIPI_IK)) {
											izinOnay1.setGuncelleyenUser(authenticatedUser);
											izinOnay1.setGuncellemeTarihi((Date) guncellemeTarihi.clone());
											izinOnay1.setOnayDurum(PersonelIzinOnay.ONAY_DURUM_ONAYLANDI);
										}
										PersonelIzinOnay izinOnay2 = (PersonelIzinOnay) izinOnay1.kopyala();
										izinOnay1.setPersonelIzin(izin1);
										izinOnay2.setPersonelIzin(izin2);
										saveList.add(izinOnay1);
										saveList.add(izinOnay2);
									}
									PersonelIzinDetay izinDetay1 = new PersonelIzinDetay(hakedesIzin1, izin1, sure1);
									PersonelIzinDetay izinDetay2 = new PersonelIzinDetay(hakedesIzin2, izin2, sure2);
									User sistemAdminUser = ortakIslemler.getSistemAdminUser(session);
									yilBasiIzin.setGuncelleyenUser(sistemAdminUser);
									yilBasiIzin.setGuncellemeTarihi((Date) guncellemeTarihi.clone());
									yilBasiIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL);
									saveList.add(izinDetay1);
									saveList.add(izinDetay2);
								}

							}

						} catch (Exception e1) {
							e1.printStackTrace();
							logger.error(e1);
						}

					} catch (Exception eX) {
						eX.printStackTrace();
						logger.error(eX);
					}

				}
			}

		}

		if (saveList != null) {
			boolean flush = Boolean.FALSE;
			for (Object objectIzin : saveList) {
				if (objectIzin != null) {
					try {
						pdksEntityController.saveOrUpdate(session, entityManager, objectIzin);
						flush = Boolean.TRUE;
					} catch (Exception e) {
						flush = Boolean.FALSE;
						e.printStackTrace();
						break;
					}
				}
			}
			if (flush) {
				try {
					session.flush();
					if (izinSahibi.isCalisiyor()) {
						HashMap parametreMap = new HashMap();
						parametreMap.put("pdksPersonel.id", izinSahibi.getId());
						if (session != null)
							parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						User izinSahibiUser = (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);
						if (izinSahibiUser != null && izinSahibiUser.isDurum()) {
							mailIzin = yilBasiIzin;
							if (userList == null)
								userList = new ArrayList<User>();
							else
								userList.clear();
							userList.add(izinSahibiUser);
							seciliUser = izinSahibiUser;
							setToList(userList);
							// if (!PdksUtil.getTestDurum())

							try {
								// ortakIslemler.mailGonder(renderer, "/email/onayPersonelIzinMail.xhtml");
								MailObject mail = new MailObject();
								mail.setSubject("İzin Kaydı");
								StringBuffer body = new StringBuffer();
								body.append("<p> " + mailIzin.getIzinSahibi().getAdSoyad() + " ait izin başlangıç tarihi " + authenticatedUser.dateTimeFormatla(mailIzin.getBaslangicZamani()) + " işe başlama tarihi " + authenticatedUser.dateTimeFormatla(mailIzin.getBitisZamani()) + " "
										+ mailIzin.getIzinTipiAciklama() + " " + authenticatedUser.getAdSoyad() + " tarafından kayıt geçirilmiştir.</p>");
								body.append("<p>Saygılarımla,</p>");
								body.append("<p><a href=\"http://" + adres + "\">" + ortakIslemler.getParameterKey("fromName") + " uygulamasına girmek için buraya tıklayınız.</a></p>");
								mail.setBody(body.toString());
								if (seciliUser.getPdksPersonel() == null || seciliUser.getPdksPersonel().isCalisiyor())
									mail.getToList().add(seciliUser.getMailPersonel());
								ortakIslemler.mailSoapServisGonder(false, mail, renderer, "/email/onayPersonelIzinMail.xhtml", session);

							} catch (Exception e) {
								logger.error("PDKS hata in : \n");
								e.printStackTrace();
								logger.error("PDKS hata out : " + e.getMessage());
								PdksUtil.addMessageError(e.getMessage());
							}
						}
					}

				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());
					PdksUtil.addMessageError("Mesaj gönderilmemiştir. " + e.getMessage());

				}
			}

			else
				saveList = null;
		}

		return saveList;

	}

	/**
	 * @param masterIzin
	 * @return
	 */
	public PersonelIzin getHakEdisIzin(PersonelIzin masterIzin) {
		Date izinBaslangicZamani = PdksUtil.getDate(masterIzin.getBaslangicZamani());
		PersonelIzin hakedesIzin = null;
		try {
			Personel izinSahibi = masterIzin.getIzinSahibi();
			HashMap<Integer, Integer> kidemHesabiMap = ortakIslemler.getKidemHesabi(izinBaslangicZamani, izinSahibi, null, null, authenticatedUser, session, null, Boolean.TRUE, Boolean.FALSE);
			int artiAy = kidemHesabiMap.get(Calendar.YEAR) > 0 ? 0 : 12;
			HashMap map = new HashMap();
			map.put("izinSahibi.id =", izinSahibi.getId());
			map.put("izinTipi.bakiyeIzinTipi.id =", masterIzin.getIzinTipi().getId());
			map.put("bitisZamani<=", PdksUtil.tariheAyEkleCikar(izinBaslangicZamani, artiAy));
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelIzin> izinList = pdksEntityController.getObjectByInnerObjectListInLogic(map, PersonelIzin.class);
			if (izinList.size() > 1)
				izinList = PdksUtil.sortListByAlanAdi(izinList, "bitisZamani", true);
			for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
				PersonelIzin personelIzin = (PersonelIzin) iterator.next();
				if (!personelIzin.isRedmi()) {
					hakedesIzin = personelIzin;
					break;
				}
			}
		} catch (Exception e) {

		}

		return hakedesIzin;
	}

	public void personelArama(String entityHomereRender) {
		this.setCheckBox(Boolean.FALSE);
		this.setRoleList(null);
		this.setReRender(entityHomereRender);
		aramaBaslangic(false, Boolean.TRUE);
	}

	private void aramaBaslangic(boolean userAramaSecim, boolean personelAramaSecim) {
		fillEkSahaTanim();
		this.setPersonelList(new ArrayList<Personel>());
		this.setUserList(new ArrayList<User>());
		this.setPersonelArama(Boolean.FALSE);
		this.setUserArama(Boolean.FALSE);
		this.setSeciliPersonel(new Personel());
		this.setSeciliUser(new User());
		this.setCheckBoxDurum(Boolean.FALSE);
		if (userAramaSecim)
			this.setUserArama(Boolean.TRUE);
		else if (personelAramaSecim)
			this.setPersonelArama(Boolean.TRUE);
		if (userAramaSecim || personelAramaSecim) {
			User user = new User();
			user.setPdksPersonel(new Personel());
			setSeciliUser(user);

			fillDepartmanList();
		} else {
			setSirketList(new ArrayList<Sirket>());
			setDepartmanList(new ArrayList<Tanim>());
		}
		panelDurumDegistir();
	}

	public String durumDegistir() {
		if (onayimaGelenIzinler != null) {
			for (PersonelIzinOnay personelIzinOnay : onayimaGelenIzinler) {
				personelIzinOnay.setCheckBoxDurum(checkBoxDurum);
			}
		}
		return "";
	}

	public void fillDepartmanList() {
		List<Tanim> list = new ArrayList<Tanim>();
		if (sapDepartman)
			list = ortakIslemler.getTanimList(Tanim.TIPI_SAP_DEPARTMAN, session);
		if (pdksDepartman) {
			List pdksDepartmanList = ortakIslemler.getTanimList(Tanim.TIPI_PDKS_DEPARTMAN, session);
			if (!pdksDepartmanList.isEmpty())
				list.addAll(pdksDepartmanList);
		}
		setDepartmanList(list);

	}

	public void panelDurumDegistir() {
		boolean durum = !this.isVisibled();
		this.setVisibled(durum);
	}

	public String fillPersonelList(User user) {
		List<Personel> list = new ArrayList<Personel>();
		String adi = aramaSecenekleri.getAd();
		String soyadi = aramaSecenekleri.getSoyad();
		String sicilNo = aramaSecenekleri.getSicilNo();
		HashMap parametreMap = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT P." + Personel.COLUMN_NAME_ID + " from " + Personel.TABLE_NAME + " P WITH(nolock)  ");
		// sb.append(" WHERE P." + Personel.COLUMN_NAME_DOGUM_TARIHI + " IS NOT NULL  ");
		String whereStr = " WHERE  ";
		if (PdksUtil.hasStringValue(adi)) {
			sb.append(whereStr + " P." + Personel.COLUMN_NAME_AD + " LIKE :ad");
			whereStr = " AND ";
			parametreMap.put("ad", adi.trim() + "%");
		}
		if (PdksUtil.hasStringValue(soyadi)) {
			sb.append(whereStr + " P." + Personel.COLUMN_NAME_SOYAD + " LIKE :soyad");
			whereStr = " AND ";
			parametreMap.put("soyad", soyadi.trim() + "%");
		}
		if (PdksUtil.hasStringValue(sicilNo)) {
			sb.append(whereStr + " P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " =:sicilNo");
			whereStr = " AND ";
			parametreMap.put("sicilNo", ortakIslemler.getSicilNo(sicilNo.trim()));
		}
		Long seciliSirketId = aramaSecenekleri.getSirketId();
		if (authenticatedUser.isYoneticiKontratli()) {
			if (!(authenticatedUser.isIK() || authenticatedUser.isAdmin()))
				seciliSirketId = null;
		}
		if (seciliSirketId != null) {
			sb.append(whereStr + " P." + Personel.COLUMN_NAME_SIRKET + " =:sirketId");
			whereStr = " AND ";
			parametreMap.put("sirketId", seciliSirketId);
		}
		if (aramaSecenekleri.getEkSaha1Id() != null) {
			sb.append(whereStr + " P." + Personel.COLUMN_NAME_EK_SAHA1 + " =:ekSaha1");
			whereStr = " AND ";
			parametreMap.put("ekSaha1", aramaSecenekleri.getEkSaha1Id());
		}
		if (aramaSecenekleri.getEkSaha2Id() != null) {
			sb.append(whereStr + " P." + Personel.COLUMN_NAME_EK_SAHA2 + " =:ekSaha2");
			whereStr = " AND ";
			parametreMap.put("ekSaha2", aramaSecenekleri.getEkSaha2Id());
		}
		if (aramaSecenekleri.getEkSaha3Id() != null) {
			sb.append(whereStr + " P." + Personel.COLUMN_NAME_EK_SAHA3 + " =:ekSaha3");
			whereStr = " AND ";
			parametreMap.put("ekSaha3", aramaSecenekleri.getEkSaha3Id());
		}
		if (aramaSecenekleri.getEkSaha4Id() != null) {
			sb.append(whereStr + " P." + Personel.COLUMN_NAME_EK_SAHA4 + " =:ekSaha4");
			whereStr = " AND ";
			parametreMap.put("ekSaha4", aramaSecenekleri.getEkSaha4Id());
		}
		if (!authenticatedUser.isYoneticiKontratli() && !aramaSecenekleri.getSirketIdList().isEmpty()) {
			sb.append(whereStr + " P." + Personel.COLUMN_NAME_SIRKET + " :srk");
			whereStr = " AND ";
			List<Long> sList = new ArrayList<Long>();
			for (SelectItem sr : aramaSecenekleri.getSirketIdList())
				sList.add((Long) sr.getValue());
			parametreMap.put("srk", sList);
		}

		List<String> perNoList = new ArrayList<String>(ortakIslemler.getYetkiTumPersonelNoList());
		if (linkAdres != null) {
			if (PdksUtil.hasStringValue(sicilNo) && !perNoList.contains(sicilNo.trim()))
				perNoList.add(sicilNo.trim());
		}
		if (parametreMap.isEmpty()) {
			sb.append(" INNER JOIN " + PersonelKGS.TABLE_NAME + " K ON K." + PersonelKGS.COLUMN_NAME_ID + "=P." + Personel.COLUMN_NAME_KGS_PERSONEL);
			sb.append(" AND K." + PersonelKGS.COLUMN_NAME_SICIL_NO + " :kSicilNo");
			parametreMap.put("kSicilNo", perNoList);
		}
		sb.append(whereStr + " P." + Personel.COLUMN_NAME_DURUM + " =1");
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			list = ortakIslemler.getPersonelList(sb, parametreMap);

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}
		TreeMap<Long, Departman> departmanMap = ortakIslemler.getIzinGirenDepartmanMap(session);

		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Personel pdksPersonel = (Personel) iterator.next();
			try {
				if (pdksPersonel.getDogumTarihi() == null && departmanMap.containsKey(pdksPersonel.getSirket().getDepartman().getId())) {
					if (sicilNo == null || sicilNo.trim().length() < 1)
						PdksUtil.addMessageAvailableWarn(pdksPersonel.getPdksSicilNo() + " " + pdksPersonel.getAdSoyad() + " doğum tarihi tanımsız!");
					iterator.remove();
					continue;
				} else if (!pdksPersonel.isCalisiyor()) {
					iterator.remove();
					continue;
				} else if (pdksPersonel.getSirket().getDepartman().getIzinGirilebilir().equals(Boolean.FALSE)) {
					iterator.remove();
					continue;
				}
				pdksPersonel.setCheckBoxDurum(Boolean.FALSE);
				if (user == null) {
					if (!perNoList.contains(pdksPersonel.getSicilNo()))
						iterator.remove();

				} else if (!user.isAdmin() && pdksPersonel.getSirket().getDepartman().getId().equals(user.getDepartman().getIcapciOlabilir()))
					iterator.remove();
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

		}

		if (!list.isEmpty())
			list = PdksUtil.sortObjectStringAlanList(null, list, "getAdSoyad", null);

		setPersonelList(list);
		return "";
	}

	public int getSize() {
		if (getFiles().size() > 0) {
			return getFiles().size();
		} else {
			return 0;
		}
	}

	public String ustOnayla(PersonelIzinOnay personelIzinOnay) {
		basarili = Boolean.FALSE;
		String islem = "persist";
		try {
			izinOnayla(personelIzinOnay.getId(), null);
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			islem = "";
		}
		return islem;
	}

	@Transient
	public boolean isUstOnayla(PersonelIzinOnay izinOnay) {
		boolean tekrarOnayla = Boolean.FALSE;
		PersonelIzin izin = izinOnay.getPersonelIzin();
		// session.refresh(izin);
		Set<PersonelIzinOnay> onaylayanlar = izin.getOnaylayanlar();
		if (onaylayanlar != null) {
			HashMap<String, PersonelIzinOnay> onaylayanlarMap = new HashMap<String, PersonelIzinOnay>();
			for (PersonelIzinOnay personelIzinOnay : onaylayanlar)
				onaylayanlarMap.put(personelIzinOnay.getOnaylayanTipi(), personelIzinOnay);
			String onaylayanTipi = "", sonrakiOnaylayanTipi = "";
			if (izin.getIzinDurumu() == PersonelIzin.IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA) {
				onaylayanTipi = PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI1;
				sonrakiOnaylayanTipi = PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI2;
			} else if (izin.getIzinDurumu() == PersonelIzin.IZIN_DURUMU_IK_ONAYINDA) {
				onaylayanTipi = PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI2;
				sonrakiOnaylayanTipi = PersonelIzinOnay.ONAYLAYAN_TIPI_IK;
			} else if (izin.getIzinDurumu() == PersonelIzin.IZIN_DURUMU_IKINCI_YONETICI_ONAYINDA && izinOnay.getOnaylayanTipi().equals(PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI2)) {

				onaylayanTipi = PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI2;
				sonrakiOnaylayanTipi = PersonelIzinOnay.ONAYLAYAN_TIPI_IK;
			}
			if (onaylayanlarMap.containsKey(onaylayanTipi)) {
				PersonelIzinOnay personelIzinOnay = onaylayanlarMap.get(onaylayanTipi);
				if (personelIzinOnay.getOnayDurum() == PersonelIzinOnay.ONAY_DURUM_ONAYLANDI) {
					tekrarOnayla = !onaylayanlarMap.containsKey(sonrakiOnaylayanTipi);
				}
			}
			onaylayanlarMap = null;
		}

		return tekrarOnayla;
	}

	public boolean isTekrarOnayla(PersonelIzinOnay izinOnay) {
		boolean tekrarOnayla = Boolean.FALSE;
		if (izinOnay != null) {
			if (izinOnay.isOnayBekliyor() && (izinOnay.getOnaylayanTipi().equals(PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI1) || izinOnay.getOnaylayanTipi().equals(PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI2))) {
				if (izinOnay.getOnaylayanTipi().equals(PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI1) || izinOnay.getOnaylayanTipi().equals(PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI2)) {
					PersonelIzin personelIzin = izinOnay.getPersonelIzin();
					Personel izinSahibi = personelIzin.getIzinSahibi();
					Personel onaylayacakYonetici = izinOnay.getOnaylayanTipi().equals(PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI1) ? izinSahibi.getPdksYonetici() : izinSahibi.getYonetici2();
					boolean tekrarOnaylar = onaylayacakYonetici != null && onaylayacakYonetici.isCalisiyor() && onaylayacakYonetici.isCalisiyorGun(sistemTarihi);
					User onaylayacakYoneticiUser = null;
					if (tekrarOnaylar && onaylayacakYonetici != null) {
						HashMap map = new HashMap();
						map.put("pdksPersonel.id", onaylayacakYonetici.getId());
						map.put("durum", Boolean.TRUE);
						if (session != null)
							map.put(PdksEntityController.MAP_KEY_SESSION, session);
						onaylayacakYoneticiUser = (User) pdksEntityController.getObjectByInnerObject(map, User.class);
						if (onaylayacakYoneticiUser != null) {
							tekrarOnaylar = Boolean.TRUE;
							ortakIslemler.setUserRoller(onaylayacakYoneticiUser, session);

						}
					}
					if (izinOnay.getGuncelleyenUser() == null) {
						tekrarOnayla = tekrarOnaylar;
					} else if (tekrarOnaylar) {
						Personel yonetici = izinOnay.getGuncelleyenUser().getPdksPersonel();
						tekrarOnayla = !yonetici.getId().equals(onaylayacakYonetici.getId());
					}
				}
			}
			izinOnay.setTekrarOnayla(tekrarOnayla);
		}

		return tekrarOnayla;
	}

	private String isHaftaTatilKontrolIzin(PersonelIzin personelIzin) {
		String tatilAciklama = "";
		boolean tatilDurum = Boolean.FALSE;
		HashMap map = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT DISTINCT * FROM " + VardiyaHafta.TABLE_NAME + " WITH(nolock) ");
		sb.append(" WHERE " + VardiyaHafta.COLUMN_NAME_BAS_TARIH + "<= :bitTarih AND " + VardiyaHafta.COLUMN_NAME_BIT_TARIH + ">= :basTarih AND " + VardiyaHafta.COLUMN_NAME_PERSONEL + "= :personelId ");
		map.put("personelId", personelIzin.getIzinSahibi().getId());
		map.put("basTarih", personelIzin.getBaslangicZamani());
		map.put("bitTarih", personelIzin.getBitisZamani());
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<VardiyaHafta> vardiyaHaftalari = pdksEntityController.getObjectBySQLList(sb, map, VardiyaHafta.class);
		int sayac = 0;
		map.clear();
		if (!vardiyaHaftalari.isEmpty()) {
			List<VardiyaGun> vardiyalar = new ArrayList<VardiyaGun>();
			if (vardiyaHaftalari.size() > 1)
				vardiyaHaftalari = PdksUtil.sortListByAlanAdi(vardiyaHaftalari, "basTarih", Boolean.FALSE);
			for (VardiyaHafta pdksVardiyaHafta : vardiyaHaftalari) {
				pdksVardiyaHafta.setHaftalikVardiyaPlan();
				vardiyalar.addAll(pdksVardiyaHafta.getVardiyaGunler());
			}

			Date basTarih = vardiyaHaftalari.get(0).getBasTarih();
			Date bitTarih = vardiyaHaftalari.get(vardiyaHaftalari.size() - 1).getBitTarih();
			TreeMap<String, VardiyaGun> vardiyalarMap = new TreeMap<String, VardiyaGun>();
			ArrayList<Long> pdksPersonelIds = new ArrayList<Long>();
			pdksPersonelIds.add(personelIzin.getIzinSahibi().getId());
			List<VardiyaGun> vardiyaList = ortakIslemler.getPersonelIdVardiyalar(pdksPersonelIds, basTarih, bitTarih, Boolean.FALSE, session);
			for (VardiyaGun pdksVardiyaGun : vardiyaList) {
				vardiyalarMap.put(pdksVardiyaGun.getVardiyaDateStr(), pdksVardiyaGun);
			}
			pdksPersonelIds = null;
			vardiyaList = null;
			for (VardiyaGun pdksVardiyaGun : vardiyalar) {
				VardiyaGun vardiyaGun = vardiyalarMap.isEmpty() || !vardiyalarMap.containsKey(pdksVardiyaGun.getVardiyaDateStr()) ? pdksVardiyaGun : vardiyalarMap.get(pdksVardiyaGun.getVardiyaDateStr());
				if (vardiyaGun.getVardiya().isHaftaTatil())
					++sayac;
			}
			tatilDurum = vardiyaHaftalari.size() > 0 && sayac != vardiyaHaftalari.size();
			vardiyalarMap = null;
		}
		if (tatilDurum)
			PdksUtil.addMessageWarn("Hafta tatil sayısı izin döneminde yetersiz! [ " + sayac + " - " + vardiyaHaftalari.size() + " hafta ]");
		else
			tatilAciklama = "persist";
		return tatilAciklama;
	}

	private void fillEkSahaTanim() {
		if (aramaListeSecenekleri == null)
			aramaListeSecenekleri = new AramaSecenekleri(authenticatedUser);
		if (aramaSecenekleri == null)
			aramaSecenekleri = new AramaSecenekleri(authenticatedUser);
		ortakIslemler.fillEkSahaTanimAramaSecenekAta(session, Boolean.FALSE, null, aramaSecenekleri);
		ortakIslemler.fillEkSahaTanimAramaSecenekAta(session, Boolean.FALSE, null, aramaListeSecenekleri);
		if (aramaListeSecenekleri.getSirketIdList().size() == 1)
			aramaListeSecenekleri.setSirketId((Long) aramaListeSecenekleri.getSirketIdList().get(0).getValue());
		if (aramaSecenekleri.getSirketList() != null) {
			List<SelectItem> sirketIdList = ortakIslemler.getIzinSirketItemList(aramaSecenekleri.getSirketList());
			aramaSecenekleri.setSirketIdList(sirketIdList);
		}
		Tanim ekSaha3 = aramaSecenekleri.getEkSahaTanimMap() != null && aramaSecenekleri.getEkSahaTanimMap().containsKey("ekSaha3") ? aramaSecenekleri.getEkSahaTanimMap().get("ekSaha3") : null;
		bolumAciklama = ekSaha3 != null ? ekSaha3.getAciklama().toLowerCase(PdksUtil.TR_LOCALE) : "bölümü";
		if (aramaSecenekleri.getSirketIdList().size() == 1)
			aramaSecenekleri.setSirketId((Long) aramaSecenekleri.getSirketIdList().get(0).getValue());
	}

	public IzinIstirahat getIzinIstirahat(Long personelIzinId) {
		IzinIstirahat izinIstirahat = null;
		if (izinIstirahatMap != null && izinIstirahatMap.containsKey(personelIzinId)) {
			izinIstirahat = izinIstirahatMap.get(personelIzinId);
			izinIstirahat.aciklamaAta();
		}

		return izinIstirahat;
	}

	public boolean getIzinDosya(PersonelIzin izin) {
		PersonelIzinDosya personelIzinDosya = null;
		izin.setDosya(null);
		boolean dosyaVar = Boolean.FALSE;
		if (izin.getIzinTipi().isDosyaEklenir() && izinDosyaMap != null && izinDosyaMap.containsKey(izin.getId())) {
			personelIzinDosya = izinDosyaMap.get(izin.getId());
			izin.setDosya(personelIzinDosya.getDosya());
			dosyaVar = Boolean.TRUE;
		}
		return dosyaVar;
	}

	public void paint(OutputStream stream, Object object) throws IOException {

	}

	public void listener(UploadEvent event) throws Exception {
		UploadItem item = event.getUploadItem();
		FileUpload file = new FileUpload();
		file.setName(item.getFileName());
		file.setLength(item.getData().length);
		file.setData(item.getData());
		try {
			veriOku(file);
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			logger.debug(e.getMessage());
		}
		// File file1 = new File("Test.txt");
		// FileOutputStream fos = new FileOutputStream(file1);
		// fos.write(item.getData());
		// FileInputStream fileinputStreamXLS = new FileInputStream(file1);
		// POIFSFileSystem fs = new POIFSFileSystem(fileinputStreamXLS);
		// HSSFWorkbook wb = new HSSFWorkbook(fs);
		Dosya dosya = ortakIslemler.getDosyaFromFileUpload(file);

		Workbook wb = ortakIslemler.getWorkbook(dosya);
		sicilNoList = new ArrayList<String>();
		if (wb != null) {
			try {
				Sheet sheet = wb.getSheetAt(0);
				for (int j = 0; j <= sheet.getLastRowNum(); j++) {
					String sicilNo = "";
					try {
						sicilNo = sheet.getRow(j).getCell(0).getStringCellValue();
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());
						try {
							Double numara = sheet.getRow(j).getCell(0).getNumericCellValue();
							sicilNo = numara.longValue() + "";

						} catch (Exception e2) {
							sicilNo = "!!!!";
						}
					}
					if (ortakIslemler.getYetkiTumPersonelNoList().contains(sicilNo))
						sicilNoList.add(sicilNo);

				}
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
			}
		}
		List<PersonelView> personelList = new ArrayList<PersonelView>();
		if (!sicilNoList.isEmpty()) {
			HashMap parametreMap = new HashMap();
			parametreMap.put("pdksSicilNo", sicilNoList);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			personelList = ortakIslemler.getPersonelViewList(pdksEntityController.getObjectByInnerObjectList(parametreMap, PdksPersonelView.class));
		}

		setExcelList((ArrayList<PersonelView>) personelList);

	}

	public void veriOku(FileUpload file) throws Exception {
		logger.error(file.getData());
		// file.getData();
	}

	public String clearUploadData() {
		files.clear();

		return null;
	}

	public long getTimeStamp() {
		return System.currentTimeMillis();
	}

	@Override
	public Object getId() {
		if (personelIzinId == null) {
			return super.getId();
		} else {
			return personelIzinId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public IzinTipi getSeciliIzinTipi() {
		return seciliIzinTipi;
	}

	public void setSeciliIzinTipi(IzinTipi seciliIzinTipi) {
		this.seciliIzinTipi = seciliIzinTipi;
	}

	public List<IzinTipi> getGuncellemeIzinTipiList() {
		return guncellemeIzinTipiList;
	}

	public void setGuncellemeIzinTipiList(List<IzinTipi> guncellemeIzinTipiList) {
		this.guncellemeIzinTipiList = guncellemeIzinTipiList;
	}

	public PersonelIzin getGuncellenecekIzin() {
		return guncellenecekIzin;
	}

	public void setGuncellenecekIzin(PersonelIzin guncellenecekIzin) {
		this.guncellenecekIzin = guncellenecekIzin;
	}

	@Transient
	public List<User> getToList() {
		return toList;
	}

	public void setToList(List<User> toList) {
		this.toList = toList;
	}

	@Transient
	public Date getFiltreBaslangicZamani() {
		return filtreBaslangicZamani;
	}

	public void setFiltreBaslangicZamani(Date filtreBaslangicZamani) {
		this.filtreBaslangicZamani = filtreBaslangicZamani;
	}

	@Transient
	public Date getFiltreBitisZamani() {
		return filtreBitisZamani;
	}

	public void setFiltreBitisZamani(Date filtreBitisZamani) {
		this.filtreBitisZamani = filtreBitisZamani;
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaSSKGirisAction() throws Exception {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.clear();
		try {
			if (authenticatedUser.isAdmin() == false || aramaSecenekleri == null)
				aramaSecenekleri = new AramaSecenekleri(authenticatedUser);
			aramaSecenekleri.setStajyerOlmayanSirket(false);
			aramaSecenekleri.setSessionClear(Boolean.FALSE);
			girisIslemleri(Boolean.TRUE);
			fillGirisEkSahaTanim();
			HashMap hashMap = new HashMap();
			if (!authenticatedUser.isAdmin())
				hashMap.put("departman.id", authenticatedUser.getDepartman().getId());
			// hashMap.put("izinTipiTanim.kodu like", IzinTipi.SSK_ISTIRAHAT);
			hashMap.put("izinTipiTanim.kodu like", "%I%");
			hashMap.put("personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
			hashMap.put("durum=", Boolean.TRUE);
			if (session != null)
				hashMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			IzinTipi izinTipi = (IzinTipi) pdksEntityController.getObjectByInnerObjectInLogic(hashMap, IzinTipi.class);
			setSeciliIzinTipi(izinTipi);
			getInstance().setIzinTipi(izinTipi);
			izinTipiDegisti(izinTipi);

		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

	}

	public void fillGirisEkSahaTanim() {
		visibled = Boolean.FALSE;
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
		List<Sirket> pdksSirketList = (List<Sirket>) sonucMap.get("pdksSirketList");
		List<SelectItem> sirketItemList = ortakIslemler.getIzinSirketItemList(pdksSirketList);
		setSirketList(pdksSirketList);
		setSirketItemList(sirketItemList);
		setArananPersonel(new Personel());
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() throws Exception {
		servisAktarDurum = Boolean.FALSE;
		boolean ayniSayfa = authenticatedUser.getCalistigiSayfa() != null && authenticatedUser.getCalistigiSayfa().equals("personelIzinGirisi");
		try {
			if (session == null)
				session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
			session.clear();
			if (authenticatedUser.isAdmin() == false || aramaSecenekleri == null || aramaListeSecenekleri == null) {
				aramaListeSecenekleri = new AramaSecenekleri(authenticatedUser);
				aramaSecenekleri = new AramaSecenekleri(authenticatedUser);
			}
			aramaListeSecenekleri.setStajyerOlmayanSirket(false);
			aramaListeSecenekleri.setSessionClear(Boolean.FALSE);

			aramaSecenekleri.setStajyerOlmayanSirket(false);
			aramaSecenekleri.setSessionClear(Boolean.FALSE);
			if (!ayniSayfa)
				authenticatedUser.setCalistigiSayfa("personelIzinGirisi");

			girisIslemleri(Boolean.FALSE);
			HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			String perIdStr = (String) req.getParameter("perId");
			String izinKey = (String) req.getParameter("izinKey");
			Personel per = null;
			donusAdres = null;
			if (perIdStr != null || izinKey != null) {
				String tarih1Str = (String) req.getParameter("tarih1");
				String tarih2Str = (String) req.getParameter("tarih2");
				HashMap<String, String> veriMap = PdksUtil.getDecodeMapByBase64(izinKey);
				if (veriMap.containsKey("perId"))
					perIdStr = veriMap.get("perId");
				if (veriMap.containsKey("tarih1"))
					tarih1Str = veriMap.get("tarih1");
				if (veriMap.containsKey("tarih2"))
					tarih2Str = veriMap.get("tarih2");
				veriMap = null;
				donusAdres = linkAdres;
				if (perIdStr != null) {
					HashMap hashMap = new HashMap();
					hashMap.put("pdksPersonel.id", new Long(perIdStr));
					if (session != null)
						hashMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					PersonelKGS personelKGS = (PersonelKGS) pdksEntityController.getObjectByInnerObject(hashMap, PersonelKGS.class);
					PersonelView personelView = personelKGS != null ? personelKGS.getPersonelView() : null;
					sicilNo = personelView.getPdksPersonel().getPdksSicilNo();

					if (tarih1Str != null && tarih2Str != null) {
						filtreBaslangicZamani = PdksUtil.tariheGunEkleCikar(PdksUtil.convertToJavaDate(tarih1Str, "yyyyMMdd"), -1);
						filtreBitisZamani = PdksUtil.tariheGunEkleCikar(PdksUtil.convertToJavaDate(tarih2Str, "yyyyMMdd"), 1);

					}
					if (personelView != null && izinKey != null) {
						izinliSahibi = null;
						per = personelView.getPdksPersonel();
						getPersonelVeri(personelView.getPdksPersonel());
						izinliSahibi = personelView.getPdksPersonel();
						PersonelIzin personelIzin = getInstance();
						personelIzin.setIzinSahibi(izinliSahibi);
						baslangicDegerleri();
						izinSureleriAyarla(session, personelIzin);
						if (tarih1Str != null && tarih2Str != null) {
							PersonelIzin izin = getInstance();
							if (izin != null) {
								izin.setBaslangicZamani(filtreBaslangicZamani);
								izin.setBitisZamani(PdksUtil.tariheGunEkleCikar(filtreBaslangicZamani, 1));
							}
						}
					}
					izinListele(Boolean.TRUE, per);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!ayniSayfa)
			authenticatedUser.setCalistigiSayfa("");
	}

	public void izinleriDuzelt() {
		HashMap paramMap = new HashMap();
		paramMap.put("izinTipi.bakiyeIzinTipi=", null);
		paramMap.put("guncelleyenUser=", null);
		paramMap.put("izinDurumu<>", PersonelIzin.IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA);
		if (session != null)
			paramMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PersonelIzin> izinListesi = pdksEntityController.getObjectByInnerObjectListInLogic(paramMap, PersonelIzin.class);
		int adet = 0;
		for (Iterator iterator = izinListesi.iterator(); iterator.hasNext();) {
			PersonelIzin personelIzin = (PersonelIzin) iterator.next();
			if (personelIzin.getOnaylayanlar() != null && !personelIzin.getOnaylayanlar().isEmpty()) {
				Personel personel = personelIzin.getIzinSahibi();
				int tipi = 0;
				User guncelleyen = null;
				for (PersonelIzinOnay izinOnay : personelIzin.getOnaylayanlar()) {
					if (tipi < Integer.parseInt(izinOnay.getOnaylayanTipi())) {
						if (izinOnay.getGuncelleyenUser() != null)
							guncelleyen = izinOnay.getGuncelleyenUser();
						tipi = Integer.parseInt(izinOnay.getOnaylayanTipi());
					}

				}
				if (guncelleyen != null) {
					adet++;
					personelIzin.setGuncelleyenUser(guncelleyen);
					pdksEntityController.saveOrUpdate(session, entityManager, personelIzin);

					logger.info(personel.getSicilNo() + " " + PdksUtil.setTurkishStr(personel.getAdSoyad() + " " + guncelleyen.getAdSoyad()));
				}
				iterator.remove();

			}

		}
		if (adet > 0)
			session.flush();
	}

	/**
	 * @param sskDurum
	 * @throws Exception
	 */
	private void girisIslemleri(boolean sskDurum) throws Exception {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		sessionIzin = null;
		if (listelenPersonel == null)
			listelenPersonel = new Personel();
		Map<String, String> map = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
		setAdres(map.containsKey("host") ? map.get("host") : "");
		setGirisSSK(sskDurum);

		session.setFlushMode(FlushMode.MANUAL);
		session.clear();

		setGuncellenecekIzin(null);
		setInstance(null);
		PersonelIzin personelIzin = new PersonelIzin();
		personelIzin.setIzinSuresi(0D);
		HashMap parametreMap = new HashMap();
		parametreMap.put("id", authenticatedUser.getId());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		User updateUser = (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);
		setSeciliPersonel(updateUser.getPdksPersonel());

		personelIzin.setIzinSahibi(izinliSahibi);
		setVisibled(Boolean.FALSE);
		if (authenticatedUser.isIK() || authenticatedUser.isAdmin())
			setFiltreBaslangicZamani(PdksUtil.tariheAyEkleCikar(PdksUtil.ayinIlkGunu(), -2));
		else
			setFiltreBaslangicZamani(PdksUtil.tariheAyEkleCikar(PdksUtil.buGun(), -6));
		Date bitisZamani = PdksUtil.tariheGunEkleCikar(PdksUtil.addTarih(filtreBaslangicZamani, Calendar.YEAR, 1), -1);
		setFiltreBitisZamani(PdksUtil.setGunSonu(bitisZamani));
		setPersonelIzinList(new ArrayList<PersonelIzin>());
		// izinListele();
		setDate(new Date());
		parametreMap.clear();
		parametreMap.put("id", authenticatedUser.getPdksPersonel().getId());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		Personel personel = (Personel) pdksEntityController.getObjectByInnerObject(parametreMap, Personel.class);
		setIzinliSahibi(null);

		getPersonelVeri(personel);
		Date baslangicZamani = new Date();
		personelIzin.setBaslangicZamani(baslangicZamani);
		personelIzin.setBitisZamani(PdksUtil.addTarih(baslangicZamani, Calendar.DATE, 13));
		setIzinliSahibi(personel);
		if (izinliSahibi != null)
			setSeciliPersonel(izinliSahibi);
		personelIzin.setIzinSahibi(izinliSahibi);
		baslangicDegerleri();
		izinSureleriAyarla(session, personelIzin);
		setInstance(personelIzin);
		istirahatAta(personelIzin);
		fillIzinTipiList();
		setGorevTipiList(ortakIslemler.getTanimList(Tanim.TIPI_IZIN_GOREV_TIPI, session));
		istirahatKaynakList = ortakIslemler.getTanimSelectItemByKodu(ortakIslemler.getTanimList(Tanim.TIPI_ISTIRAHAT_KAYNAGI, session));
		if (!ortakIslemler.getParameterKey("uygulamaTipi").equalsIgnoreCase("H")) {
			for (Iterator iterator = istirahatKaynakList.iterator(); iterator.hasNext();) {
				SelectItem selectItem = (SelectItem) iterator.next();
				if (!selectItem.getValue().equals(IzinIstirahat.RAPOR_KAYNAK_KODU))
					iterator.remove();

			}
		}

		setIstirahat(new IzinIstirahat());
		setEkSaha1(null);
		setEkSaha2(null);
		setEkSaha3(null);
		setEkSaha4(null);
		fillEkSahaTanim();
		dosyaTipleri = ortakIslemler.getParameterKey("dosyaTipleri");
		if (!PdksUtil.hasStringValue(dosyaTipleri))
			dosyaTipleri = "doc,docx,pd";
		fillGirisEkSahaTanim();
		if (visibled)
			visibled = izinliSahibi != null && izinliSahibi.getSirket().isPdksMi();
		if (!visibled)
			izinliSahibi = null;
	}

	/**
	 * @param session
	 * @param personelIzin
	 * @throws Exception
	 */
	private void izinSureleriAyarla(Session session, PersonelIzin personelIzin) throws Exception {
		List<Personel> personeller = new ArrayList<Personel>();
		personeller.add(izinliSahibi);
		TreeMap<String, VardiyaGun> vardiyalar = ortakIslemler.getIslemVardiyalar(personeller, personelIzin.getBaslangicZamani(), personelIzin.getBitisZamani(), Boolean.FALSE, session, Boolean.TRUE);
		if (!vardiyalar.isEmpty()) {
			VardiyaGun pdksVardiyaGun1 = new VardiyaGun(izinliSahibi, null, personelIzin.getBaslangicZamani());
			VardiyaGun pdksVardiyaGun2 = new VardiyaGun(izinliSahibi, null, personelIzin.getBitisZamani());
			if (vardiyalar.containsKey(pdksVardiyaGun1.getVardiyaKey())) {
				try {
					pdksVardiyaGun1 = vardiyalar.get(pdksVardiyaGun1.getVardiyaKey());
					if (pdksVardiyaGun1.getIslemVardiya().isCalisma()) {
						personelIzin.setBaslangicZamani(pdksVardiyaGun1.getIslemVardiya().getVardiyaBasZaman());
						Calendar cal1 = Calendar.getInstance();
						cal1.setTime(personelIzin.getBaslangicZamani());
						setBaslangicSaat(cal1.get(Calendar.HOUR_OF_DAY));
						setBaslangicDakika(cal1.get(Calendar.MINUTE));
					}
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}

			}
			if (vardiyalar.containsKey(pdksVardiyaGun2.getVardiyaKey())) {
				try {
					pdksVardiyaGun2 = vardiyalar.get(pdksVardiyaGun2.getVardiyaKey());
					if (pdksVardiyaGun2.getIslemVardiya().isCalisma()) {
						personelIzin.setBitisZamani(pdksVardiyaGun2.getIslemVardiya().getVardiyaBasZaman());
						Calendar cal1 = Calendar.getInstance();
						cal1.setTime(personelIzin.getBitisZamani());
						setBitisSaat(cal1.get(Calendar.HOUR_OF_DAY));
						setBitisDakika(cal1.get(Calendar.MINUTE));
					}
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}
			}
			pdksVardiyaGun1 = null;
			pdksVardiyaGun2 = null;
		}
		vardiyalar = null;
	}

	private void baslangicDegerleri() {
		PersonelIzin izin = new PersonelIzin();
		if (izinliSahibi == null) {
			izin.setBaslangicZamani(Calendar.getInstance().getTime());
			izin.setBitisZamani(izin.getBaslangicZamani());
		}
		izin.setIzinSuresi(0D);
		setSeciliIzinTipi(null);
		setBaslangicSaat(8);
		setBaslangicDakika(0);
		setBitisSaat(8);
		setBitisDakika(0);
		setInstance(izin);

	}

	public void izinGoster(PersonelIzinOnay izinOnay) {
		setInstance(izinOnay.getPersonelIzin());
		Set<PersonelIzinDetay> personelIzin = izinOnay.getPersonelIzin().getPersonelIzinler();
		List<PersonelIzinDetay> izinlerList = new ArrayList<PersonelIzinDetay>();
		for (PersonelIzinDetay personelIzinDetay : personelIzin) {
			izinlerList.add(personelIzinDetay);
		}
		setPersonelIzinler(izinlerList);

	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void onayimaGelenIzinlerAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);

		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String mailId = (String) req.getParameter("mId");
		if (mailId != null)
			mId = mailId;

		Map<String, String> map1 = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
		setAdres(map1.containsKey("host") ? map1.get("host") : "");
		setRedOnay(null);
		setRedSebebi(null);
		setRedSebebiTanim(null);
		boolean calistir = false;

		if (basDate == null) {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MONTH, -2);
			calendar.set(Calendar.DATE, 1);
			setBasDate(PdksUtil.getDate(calendar.getTime()));
		}

		if (bitDate == null) {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MONTH, 2);
			calendar.set(Calendar.DATE, 1);
			calendar.add(Calendar.DATE, -1);
			setBitDate(PdksUtil.getDate(calendar.getTime()));

			calendar = Calendar.getInstance();
			calendar.set(Calendar.MONTH, Calendar.DECEMBER);
			calendar.set(Calendar.DATE, 31);
			if (bitDate.getTime() < calendar.getTime().getTime())
				setBitDate(PdksUtil.getDate(calendar.getTime()));
		}
		if (mailId == null && !authenticatedUser.isAdmin()) {
			try {
				List izinList = ortakIslemler.getIzinOnayDurum(session, authenticatedUser);
				if (!izinList.isEmpty()) {
					for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
						Object[] veriler = (Object[]) iterator.next();
						Date tarih1 = PdksUtil.getDate((Date) veriler[0]), tarih2 = PdksUtil.getDate((Date) veriler[1]);
						if (tarih1 != null)
							if (basDate == null || basDate.after(tarih1))
								basDate = tarih1;
						if (tarih2 != null)
							if (bitDate == null || tarih2.after(bitDate))
								bitDate = tarih2;
					}
					calistir = basDate != null && bitDate != null;
				}
				izinList = null;
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

		}
		mailIzin = null;
		setOnayimaGelenIzinler(new ArrayList());
		if (PdksUtil.hasStringValue(mId)) {
			try {
				HashMap parametreMap = new HashMap();
				parametreMap.put("id", new Long(mId));
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				mailIzin = (PersonelIzin) pdksEntityController.getObjectByInnerObject(parametreMap, PersonelIzin.class);
				if (mailIzin != null) {
					onayListesiOlustur();
				}
			} catch (Exception e) {
				mailIzin = null;
			}

		} else if (calistir)
			try {
				onayListesiOlustur();
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

	}

	/**
	 * @throws Exception
	 */
	public void onayListesiOlustur() throws Exception {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.clear();
		User user = (User) authenticatedUser.clone();
		// HashMap paramMap = new HashMap();
		// paramMap.clear();
		// paramMap.put("tipi", Tanim.TIPI_ONAYLAMAMA_NEDEN);
		// paramMap.put("durum", Boolean.TRUE);
		// if (session != null)
		// paramMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Tanim> sebepList = ortakIslemler.getTanimList(Tanim.TIPI_ONAYLAMAMA_NEDEN, session);
		setRedSebebiList(sebepList);
		if (vekilYoneticiMap == null)
			vekilYoneticiMap = new TreeMap<Long, User>();
		HashMap<Long, PersonelIzinOnay> liste = new HashMap<Long, PersonelIzinOnay>();
		checkBoxDurum = Boolean.FALSE;
		onaylanacakIzinler(user, liste);
		List<PersonelIzinOnay> list = new ArrayList(liste.values());
		// vekalet varsa, vekalet veren yoneticinin onay listesi bu listeye
		// eklenir.
		if (list != null) {
			HashMap<Long, PersonelIzin> map = new HashMap<Long, PersonelIzin>();
			for (PersonelIzinOnay personelIzinOnay : list) {
				if (map.containsKey(personelIzinOnay.getPersonelIzin().getId()))
					continue;
				map.put(personelIzinOnay.getPersonelIzin().getId(), personelIzinOnay.getPersonelIzin());
				if (!(personelIzinOnay.getPersonelIzin().isBakiyeVar()) && authenticatedUser.isIK()) {
					personelIzinOnay.setKayitDurum("hatali");
				}

			}

		}
		if (!list.isEmpty()) {
			List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				PersonelIzinOnay personelIzinOnay = (PersonelIzinOnay) iterator.next();
				personelIzinOnay.setCheckBoxDurum(checkBoxDurum);
				izinList.add(personelIzinOnay.getPersonelIzin());
			}
			izinDosyalariOlustur(izinList);
		}
		TreeMap<String, PersonelIzinOnay> map1 = new TreeMap<String, PersonelIzinOnay>();
		int sira = 0;
		List<Long> list1 = new ArrayList<Long>();
		for (PersonelIzinOnay personelIzinOnay : list) {
			PersonelIzin personelIzin = personelIzinOnay.getPersonelIzin();
			if (list1.contains(personelIzin.getId()))
				continue;
			list1.add(personelIzin.getId());
			String key = (personelIzin != null ? PdksUtil.convertToDateString(personelIzin.getBitisZamani(), "yyyyMMdd HH:mm") + personelIzin.getIzinSahibi().getPdksSicilNo() + "_" : "") + String.valueOf(++sira);
			map1.put(key, personelIzinOnay);

		}
		list1 = null;
		list = new ArrayList<PersonelIzinOnay>(map1.values());
		// Collections.reverse(list);
		TreeMap<String, List<PersonelIzinOnay>> map2 = new TreeMap<String, List<PersonelIzinOnay>>();
		for (PersonelIzinOnay personelIzinOnay : list) {
			List<PersonelIzinOnay> listParca = map2.containsKey(personelIzinOnay.getOnaylayanTipi()) ? map2.get(personelIzinOnay.getOnaylayanTipi()) : new ArrayList<PersonelIzinOnay>();
			if (listParca.isEmpty())
				map2.put(personelIzinOnay.getOnaylayanTipi(), listParca);
			listParca.add(personelIzinOnay);
		}
		list.clear();
		for (String key : map2.keySet())
			list.addAll(map2.get(key));
		map2 = null;

		map1 = null;

		setOnayimaGelenIzinler(list);

	}

	/**
	 * @param user
	 * @param liste
	 * @throws Exception
	 */
	public void onaylanacakIzinler(User user, HashMap<Long, PersonelIzinOnay> liste) throws Exception {

		List<PersonelIzinOnay> personelIzinOnayList = new ArrayList<PersonelIzinOnay>();
		if (user.isSekreter() && user.getPdksPersonel().getPdksYonetici() != null) {
			HashMap parametreMap = new HashMap();
			parametreMap.put("pdksPersonel.id", user.getPdksPersonel().getPdksYonetici().getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			User yoneticisiUser = (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);
			if (yoneticisiUser != null)
				user = yoneticisiUser;
		}
		Date bugun = PdksUtil.buGun();
		HashMap vekaletMap = new HashMap();
		vekaletMap.put(PdksEntityController.MAP_KEY_MAP, "getId");
		vekaletMap.put(PdksEntityController.MAP_KEY_SELECT, "vekaletVeren");
		vekaletMap.put("yeniYonetici =", user);
		vekaletMap.put("basTarih <=", bugun);
		vekaletMap.put("bitTarih >=", bugun);
		vekaletMap.put("durum =", Boolean.TRUE);
		if (session != null)
			vekaletMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		TreeMap<Long, User> yoneticiMap = pdksEntityController.getObjectByInnerObjectMapInLogic(vekaletMap, UserVekalet.class, Boolean.FALSE);
		for (Iterator iterator = new ArrayList<User>(yoneticiMap.values()).iterator(); iterator.hasNext();) {
			User vekaletVeren = (User) iterator.next();
			ortakIslemler.setUserRoller(vekaletVeren, session);
			if (!vekilYoneticiMap.containsKey(vekaletVeren.getId())) {
				ortakIslemler.sistemeGirisIslemleri(vekaletVeren, Boolean.FALSE, bugun, bugun, session);
				vekilYoneticiMap.put(vekaletVeren.getId(), vekaletVeren);
			} else
				vekaletVeren = vekilYoneticiMap.get(vekaletVeren.getId());

			onaylanacakIzinler(vekaletVeren, liste);
		}
		yoneticiMap.clear();

		yoneticiMap.put(user.getId(), user);
		HashMap parametreMap = new HashMap();
		StringBuffer builder = new StringBuffer();
		builder.append("SELECT  I.ONAY_ID  FROM  dbo.ONAY_BEKLEYEN_IZIN_VIEW I WITH(nolock)  ");
		builder.append(" INNER JOIN " + PersonelIzinOnay.TABLE_NAME + " O ON O." + PersonelIzinOnay.COLUMN_NAME_ID + "=I.ONAY_ID ");
		if (mailIzin != null && mailIzin.getId() != null) {
			builder.append(" AND O." + PersonelIzinOnay.COLUMN_NAME_PERSONEL_IZIN_ID + " =:izinId");
			parametreMap.put("izinId", mailIzin.getId());
		} else {
			builder.append(" where I.BASLANGIC_ZAMANI<=:bitDate  AND I.BITIS_ZAMANI>=:basDate AND IZIN_DURUMU IN (1,2) ");
			parametreMap.put("bitDate", bitDate);
			parametreMap.put("basDate", basDate);

		}
		builder.append("   AND I.KULLANICI_ID =:userId AND  I.ONAY_ID IS NOT NULL");
		parametreMap.put("userId", user.getId());
		if (authenticatedUser.isIK() && mailIzin == null) {
			builder.append(" UNION   ");
			builder.append("  SELECT  I.ONAY_ID  FROM  dbo.ONAY_BEKLEYEN_IZIN_VIEW I WITH(nolock)  ");
			builder.append(" INNER JOIN " + PersonelIzinOnay.TABLE_NAME + " O ON O." + PersonelIzinOnay.COLUMN_NAME_ID + "=I.ONAY_ID ");
			builder.append(" where  KULLANICI_DURUM=0 AND IZIN_DURUMU IN (1,2) AND DEPARTMAN_ID=:departmanId ");
			builder.append(" AND I.BASLANGIC_ZAMANI<=:bitDate1  AND I.BITIS_ZAMANI>=:basDate1 ");
			parametreMap.put("bitDate1", bitDate);
			parametreMap.put("basDate1", basDate);
			parametreMap.put("departmanId", authenticatedUser.getDepartman().getId());
		}
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PersonelIzinOnay> onayList = ortakIslemler.getDataByIdList(builder, parametreMap, PersonelIzinOnay.TABLE_NAME, PersonelIzinOnay.class);

		builder = null;
		TreeMap<Long, PersonelIzinOnay> personelIzinOnayMap = null;
		HashMap<Long, Personel> yoneticiPersonelMap = new HashMap<Long, Personel>();
		yoneticiPersonelMap.put(user.getPdksPersonel().getId(), user.getPdksPersonel());

		if (!onayList.isEmpty()) {
			personelIzinOnayMap = new TreeMap<Long, PersonelIzinOnay>();
			for (PersonelIzinOnay personelIzinOnay : onayList)
				personelIzinOnayMap.put(personelIzinOnay.getId(), personelIzinOnay);
		}

		vekaletMap.clear();
		vekaletMap.put(PdksEntityController.MAP_KEY_SELECT, "bagliYonetici.pdksPersonel");
		vekaletMap.put("yeniYonetici =", user);
		vekaletMap.put("basTarih <=", PdksUtil.buGun());
		vekaletMap.put("bitTarih >=", PdksUtil.buGun());
		vekaletMap.put("durum =", Boolean.TRUE);
		if (session != null)
			vekaletMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			List<Personel> personelGeciciYoneticiList = pdksEntityController.getObjectByInnerObjectListInLogic(vekaletMap, PersonelGeciciYonetici.class);
			for (Personel personel : personelGeciciYoneticiList)
				yoneticiPersonelMap.put(personel.getId(), personel);
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			PdksUtil.addMessageError(e.getMessage());

		}

		try {

			if (personelIzinOnayMap == null)
				personelIzinOnayMap = new TreeMap<Long, PersonelIzinOnay>();

			if (!authenticatedUser.isAdmin() && authenticatedUser.getTumPersoneller() != null && !authenticatedUser.getTumPersoneller().isEmpty()) {
				ArrayList<Personel> tumPersoneller = new ArrayList<Personel>(authenticatedUser.getTumPersoneller());
				List<Long> onayId = new ArrayList<Long>();
				Personel onaylayanPersonel = authenticatedUser.getPdksPersonel();
				if (authenticatedUser.isIK()) {
					for (Iterator iterator = tumPersoneller.iterator(); iterator.hasNext();) {
						Personel personel = (Personel) iterator.next();
						if (!authenticatedUser.isSekreter()) {
							if (personel.getPdksYonetici() != null && yoneticiPersonelMap.containsKey(personel.getPdksYonetici().getId()))
								continue;
							if (personel.getYonetici2() != null && yoneticiPersonelMap.containsKey(personel.getYonetici2().getId()))
								continue;
							iterator.remove();

						}
					}
				}

				while (!tumPersoneller.isEmpty() && (mailIzin == null || mailIzin.getId() == null)) {
					builder = new StringBuffer();
					builder.append(" SELECT   I.ONAY_ID   FROM  dbo.ONAY_BEKLEYEN_IZIN_VIEW I WITH(nolock)");
					builder.append(" where I.BASLANGIC_ZAMANI<=:bitDate  AND I.BITIS_ZAMANI>=:basDate AND IZIN_DURUMU IN (1,2) AND  I.ONAY_ID IS NOT NULL  ");
					for (Iterator iterator = tumPersoneller.iterator(); iterator.hasNext();) {
						Personel pdksPersonel = (Personel) iterator.next();
						if (onaylayanPersonel == null || !onaylayanPersonel.getId().equals(pdksPersonel.getId())) {
							if (onayId.isEmpty())
								builder.append(" AND  I.PERSONEL_ID :p");
							onayId.add(pdksPersonel.getId());
						}
						iterator.remove();
						if (onayId.size() >= 256)
							break;
					}
					if (!onayId.isEmpty()) {
						parametreMap.clear();

						parametreMap.put("p", onayId);

						parametreMap.put("bitDate", bitDate);
						parametreMap.put("basDate", basDate);
						if (session != null)
							parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						List<PersonelIzinOnay> list = ortakIslemler.getDataByIdList(builder, parametreMap, PersonelIzinOnay.TABLE_NAME, PersonelIzinOnay.class);
						if (!list.isEmpty())
							personelIzinOnayList.addAll(list);
						list = null;
						if (!tumPersoneller.isEmpty())
							onayId.clear();
					}
					builder = null;
				}

				tumPersoneller = null;
				onayId = null;
				if (!personelIzinOnayList.isEmpty()) {
					for (Iterator iterator = personelIzinOnayList.iterator(); iterator.hasNext();) {
						PersonelIzinOnay personelIzinOnay = (PersonelIzinOnay) iterator.next();
						if (personelIzinOnayMap.containsKey(personelIzinOnay.getId())) {
							iterator.remove();
							continue;
						}

						Personel personel = personelIzinOnay.getPersonelIzin().getIzinSahibi();
						if (!authenticatedUser.isSekreter()) {
							if (personelIzinOnay.getPersonelIzin().getIzinDurumu() == PersonelIzin.IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA) {
								if (personel.getPdksYonetici() == null || !yoneticiPersonelMap.containsKey(personel.getPdksYonetici().getId()))
									iterator.remove();

							} else {
								if (personel.getYonetici2() == null || !yoneticiPersonelMap.containsKey(personel.getYonetici2().getId()))
									iterator.remove();

							}
						}

					}
				}

			}

			if (personelIzinOnayMap != null && !personelIzinOnayMap.isEmpty())
				personelIzinOnayList.addAll(new ArrayList<PersonelIzinOnay>(personelIzinOnayMap.values()));
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}
		if (user.isIK()) {
			Long departmanId = authenticatedUser.getDepartman().getId();
			HashMap fields = new HashMap();
			fields.put("bitDate", bitDate);
			fields.put("basDate", basDate);
			builder = new StringBuffer();
			builder.append(" SELECT   I.*   FROM  dbo." + OnaylanmamisIzinIKView.TABLE_NAME + " I WITH(nolock)");
			builder.append(" where I.BASLANGIC_ZAMANI<=:bitDate  AND I.BITIS_ZAMANI>=:basDate   ");
			if (user.isIKAdmin() == false) {
				fields.put("departmanId", departmanId);
				builder.append("   AND I.DEPARTMAN_ID=:departmanId  ");
			}

			// builder.append("  AND I.ONAY_ID IS NOT NULL  ");

			// List<OnaylanmamisIzinIKView> list = ortakIslemler.getDataByIdList(builder, fields, OnaylanmamisIzinIKView.TABLE_NAME, OnaylanmamisIzinIKView.class, OnaylanmamisIzinIKView.COLUMN_NAME_ID);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<OnaylanmamisIzinIKView> list = pdksEntityController.getObjectBySQLList(builder, fields, OnaylanmamisIzinIKView.class);
			if (!list.isEmpty()) {
				TreeMap<Long, PersonelIzinOnay> onayMap = new TreeMap<Long, PersonelIzinOnay>();
				for (OnaylanmamisIzinIKView onaylanmamisIzinIKView : list) {
					PersonelIzinOnay izinOnay = onaylanmamisIzinIKView.getOnay();
					if (izinOnay == null) {
						izinOnay = new PersonelIzinOnay();
						if (onaylanmamisIzinIKView.getIzin() == null) {
							parametreMap.clear();
							parametreMap.put("id", onaylanmamisIzinIKView.getId());
							if (session != null)
								parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
							izinOnay.setPersonelIzin((PersonelIzin) pdksEntityController.getObjectByInnerObject(parametreMap, PersonelIzin.class));

						} else
							izinOnay.setPersonelIzin(onaylanmamisIzinIKView.getIzin());
						izinOnay.setOnaylayanTipi(PersonelIzinOnay.ONAYLAYAN_TIPI_IK);
					}
					izinOnay.setOnayDurum(PersonelIzinOnay.ONAY_DURUM_ISLEM_YAPILMADI);
					onayMap.put(onaylanmamisIzinIKView.getId(), izinOnay);
				}
				if (!onayMap.isEmpty())
					personelIzinOnayList.addAll(new ArrayList(onayMap.values()));
				onayMap = null;
			}
			fields = null;
		}

		if (personelIzinOnayList != null && !personelIzinOnayList.isEmpty()) {
			for (Iterator iterator2 = personelIzinOnayList.iterator(); iterator2.hasNext();) {
				PersonelIzinOnay personelIzinOnay = (PersonelIzinOnay) iterator2.next();
				try {
					liste.put(personelIzinOnay.getPersonelIzin().getId(), personelIzinOnay);
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}
			}
		}

	}

	public String yeniOnayla(PersonelIzinOnay personelIzinOnay) {
		basarili = Boolean.FALSE;
		String islem = "persist";
		try {
			PersonelIzin personelIzin = personelIzinOnay.getPersonelIzin();
			personelIzin.setGuncelleyenUser(null);
			personelIzinOnay.setOnayDurum(PersonelIzinOnay.ONAY_DURUM_ISLEM_YAPILMADI);
			personelIzin.setIzinDurumu(personelIzinOnay.getOnaylayanTipi().equals(PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI1) ? PersonelIzin.IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA : PersonelIzin.IZIN_DURUMU_IKINCI_YONETICI_ONAYINDA);
			izinOnayla(personelIzinOnay.getId(), null);
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			islem = "";
		}
		return islem;
	}

	public String tekrarOnayla(PersonelIzinOnay personelIzinOnay) {
		basarili = Boolean.FALSE;
		String islem = "persist";
		try {
			PersonelIzin personelIzin = personelIzinOnay.getPersonelIzin();
			personelIzinOnay.setOnayDurum(PersonelIzinOnay.ONAY_DURUM_ISLEM_YAPILMADI);
			if (!personelIzinOnay.isTekrarOnayla())
				personelIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA);
			izinOnayla(personelIzinOnay.getId(), null);
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			islem = "";
		}
		return islem;
	}

	public void bakiyeleriGoster(PersonelIzinOnay personelIzinOnay) {
		PersonelIzin izin = personelIzinOnay.getPersonelIzin();
		Personel pdksPersonel = izin.getIzinSahibi();
		setInstance(izin);
		HashMap<Long, TempIzin> izinMap = null;
		IzinTipi izinTipi = izin.getIzinTipi();
		ArrayList<String> sicilNoList = new ArrayList<String>();
		sicilNoList.add(pdksPersonel.getSicilNo());
		bakiyeTipiSenelik = null;
		if (izinTipi.isSenelikIzin()) {
			bakiyeTipiSenelik = Boolean.TRUE;
			izinMap = ortakIslemler.senelikIzinListesiOlustur(sicilNoList, null, pdksPersonel.getSirket(), Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, session);
		} else {
			bakiyeTipiSenelik = Boolean.FALSE;
			int yil = PdksUtil.getDateField(izin.getBaslangicZamani(), Calendar.YEAR);
			izinMap = ortakIslemler.bakiyeIzinListesiOlustur(izin.getIzinTipi().getIzinTipiTanim().getKodu(), sicilNoList, pdksPersonel.getSirket(), izin.getBaslangicZamani(), yil, Boolean.FALSE, session);
		}
		if (izinMap != null && !izinMap.isEmpty())
			setTempIzinList(new ArrayList(izinMap.values()));
		else
			setTempIzinList(new ArrayList());

	}

	public User getUser() {
		return seciliUser;
	}

	public void mailListesineEkle(List<String> anaListe, List<String> yeniListe) {
		if (anaListe != null && yeniListe != null && !yeniListe.isEmpty()) {
			for (String mailAdres : yeniListe) {
				try {
					String mail = PdksUtil.getMailAdres(mailAdres.trim());
					if (mail != null && !anaListe.contains(mail))
						anaListe.add(mail);
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());
					if (mailAdres != null)
						logger.info(mailAdres + " " + e.getMessage());

				}
			}
			try {
				Date bugun = PdksUtil.getDate(Calendar.getInstance().getTime());
				HashMap map = new HashMap();
				map.put(PdksEntityController.MAP_KEY_SELECT, "yeniYonetici");
				map.put("durum=", Boolean.TRUE);
				map.put("bitTarih>=", bugun);
				map.put("basTarih<=", bugun);
				map.put("vekaletVeren.email", yeniListe);
				map.put("yeniYonetici.durum=", Boolean.TRUE);
				map.put("yeniYonetici.pdksPersonel.durum=", Boolean.TRUE);
				map.put("yeniYonetici.pdksPersonel.sskCikisTarihi>=", bugun);
				map.put("yeniYonetici.pdksPersonel.iseBaslamaTarihi<=", bugun);
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<User> vekiller = pdksEntityController.getObjectByInnerObjectListInLogic(map, UserVekalet.class);
				for (User user : vekiller) {
					String mail = user.getEmail();
					if (!anaListe.contains(mail))
						anaListe.add(mail);
				}
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				logger.error("mailListesineEkle Hata : " + e.getMessage());
			}

		}

	}

	public String topluIzinOnayla() throws Exception {
		boolean islem = Boolean.FALSE, secili = Boolean.FALSE;
		int sayac = 0;
		for (PersonelIzinOnay personelIzinOnay : onayimaGelenIzinler) {

			if (personelIzinOnay.isCheckBoxDurum()) {
				try {
					secili = Boolean.TRUE;
					if (personelIzinOnay.getId() != null) {
						boolean yilbasi = personelIzinOnay.getPersonelIzin().getYilbasi();
						List list = null;
						if (yilbasi)
							list = yilBasiIzinParcala(personelIzinOnay);
						if (list == null || list.isEmpty())
							izinOnayla(personelIzinOnay.getId(), null);
					}

					else {
						boolean yilbasi = personelIzinOnay.getPersonelIzin().getYilbasi();
						List list = null;
						if (yilbasi)
							list = yilBasiIzinParcala(personelIzinOnay);
						if (list == null || list.isEmpty())
							izinOnayla(-personelIzinOnay.getPersonelIzin().getId(), null);
					}
					islem = Boolean.TRUE;
					++sayac;
					if (sayac == 25)
						break;
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());
					secili = Boolean.FALSE;
					islem = Boolean.FALSE;
					PdksUtil.addMessageError("Onaylama başarılı tamamlanmamıştır!");
					break;
				}
			}
			personelIzinOnay.setCheckBoxDurum(Boolean.FALSE);
		}

		if (islem) {
			mailIzin = null;
			onayListesiOlustur();
			PdksUtil.addMessageWarn("Onaylama başarılı tamamlanmıştır!");

		} else if (!secili)
			PdksUtil.addMessageWarn("Onaylanacak izin seçiniz!");

		return "";

	}

	/**
	 * @param personelIzinOnayId
	 * @param onayDurum
	 * @return
	 */
	public String izinOnayla(Long personelIzinOnayId, Integer onayDurum) {
		HashMap parametreMap = new HashMap();
		parametreMap.put("id", personelIzinOnayId);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		PersonelIzinOnay personelIzinOnay = personelIzinOnayId > 0 ? (PersonelIzinOnay) pdksEntityController.getObjectByInnerObject(parametreMap, PersonelIzinOnay.class) : null;
		if (personelIzinOnay == null && authenticatedUser.isIK()) {
			personelIzinOnay = new PersonelIzinOnay();

			parametreMap.clear();
			parametreMap.put("id", -personelIzinOnayId);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

			PersonelIzin personelIzin = (PersonelIzin) pdksEntityController.getObjectByInnerObject(parametreMap, PersonelIzin.class);
			personelIzinOnay.setOnaylayanTipi(PersonelIzinOnay.ONAYLAYAN_TIPI_IK);
			if (personelIzin != null && personelIzin.getOnaylayanlar() != null) {
				List<PersonelIzinOnay> onaylar = new ArrayList<PersonelIzinOnay>();
				for (Iterator iterator = personelIzin.getOnaylayanlar().iterator(); iterator.hasNext();) {
					PersonelIzinOnay personelIzinOnay2 = (PersonelIzinOnay) iterator.next();
					onaylar.add(personelIzinOnay2);
				}
				if (!onaylar.isEmpty()) {
					onaylar = PdksUtil.sortListByAlanAdi(onaylar, "onaylayanTipi", Boolean.TRUE);
					PersonelIzinOnay personelIzinOnay2 = onaylar.get(0);
					personelIzinOnay.setOlusturanUser(personelIzinOnay2.getGuncelleyenUser());
					personelIzinOnay.setOlusturmaTarihi(personelIzinOnay2.getGuncellemeTarihi());
				}
			}
			personelIzinOnay.setPersonelIzin(personelIzin);
		}
		Personel izinSahibi = personelIzinOnay.getPersonelIzin().getIzinSahibi();
		String mailPersonelAciklama = getMailPersonelAciklama(izinSahibi);

		boolean onaylandi = personelIzinOnay != null && personelIzinOnay.getOnayDurum() == PersonelIzinOnay.ONAY_DURUM_ONAYLANDI;
		Boolean tekrarOnayla = new Boolean(personelIzinOnay != null && personelIzinOnay.isTekrarOnayla());
		boolean listeOlustur = onayDurum != null && onayDurum.equals(PersonelIzinOnay.ONAY_DURUM_RED);
		if (!listeOlustur)
			onayDurum = PersonelIzinOnay.ONAY_DURUM_ONAYLANDI;
		if (!onaylandi && personelIzinOnay != null)
			personelIzinOnay.setGuncellemeTarihi(new Date());
		basarili = Boolean.FALSE;
		String sonuc = "";
		User izinSahibiUser = null;
		HashMap returnMap = null;
		Personel onaylayanPersonel = authenticatedUser.getPdksPersonel();

		if (tekrarOnayla)
			onaylayanPersonel = personelIzinOnay.getOnaylayanTipi().equals(PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI1) ? izinSahibi.getPdksYonetici() : izinSahibi.getYonetici2();
		parametreMap.clear();
		parametreMap.put("pdksPersonel.id", onaylayanPersonel.getId());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		User authenUser = !(listeOlustur || tekrarOnayla) ? personelIzinOnay.getGuncelleyenUser() : (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);
		if (tekrarOnayla) {
			onaylayanPersonel = personelIzinOnay.getOnaylayanTipi().equals(PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI1) ? izinSahibi.getPdksYonetici() : izinSahibi.getYonetici2();
			onayDurum = PersonelIzinOnay.ONAY_DURUM_ISLEM_YAPILMADI;
			personelIzinOnay.setGuncelleyenUser(authenUser);
			if (toList == null)
				toList = new ArrayList<User>();
			else
				toList.clear();
			toList.add(authenUser);
			try {
				User vekil = ortakIslemler.getYoneticiBul(izinSahibi, authenUser.getPdksPersonel(), session);
				if (vekil != null && !vekil.getId().equals(authenUser.getId()))
					toList.add(vekil);

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());

			}
		}
		if (authenUser == null)
			authenUser = authenticatedUser;

		List<User> userList = null;
		int izinOncekiDurum = personelIzinOnay.getPersonelIzin().getIzinDurumu();
		try {
			parametreMap.clear();
			parametreMap.put("pdksPersonel.id", personelIzinOnay.getPersonelIzin().getIzinSahibi().getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			izinSahibiUser = (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);
			if (izinSahibiUser == null) {
				izinSahibiUser = new User();
				izinSahibiUser.setPdksPersonel(personelIzinOnay.getPersonelIzin().getIzinSahibi());
			}

			returnMap = ortakIslemler.izinOnayla(personelIzinOnay, authenUser, onayDurum, redSebebiTanim, redSebebi, session);

			PersonelIzinOnay eskiOnay = null;
			PersonelIzinOnay yeniOnay = null;

			if (returnMap.containsKey("toList")) {
				userList = (List) returnMap.get("toList");

			}
			if (returnMap.containsKey("personelIzinOnay"))
				eskiOnay = (PersonelIzinOnay) returnMap.get("personelIzinOnay");
			if (returnMap.containsKey("yeniPersonelIzinOnay"))
				yeniOnay = (PersonelIzinOnay) returnMap.get("yeniPersonelIzinOnay");

			PersonelIzin personelIzin = (PersonelIzin) returnMap.get("personelIzin");
			try {
				session.merge(personelIzin);
			} catch (Exception ee) {
				logger.error(ee);
			}
			if (personelIzin.getHesapTipi() != null && personelIzin.getHesapTipi() > 2)
				personelIzin.setHesapTipi(5 - personelIzin.getHesapTipi());

			User user = new User();
			user.setPdksPersonel(personelIzin.getIzinSahibi());

			setSeciliUser(user);
			if (!tekrarOnayla && !onaylandi) {
				personelIzin.setGuncelleyenUser(authenUser);
				personelIzin.setGuncellemeTarihi(new Date());
			}
			IzinTipi izinTipi = personelIzin.getIzinTipi();
			if (izinTipi.getHesapTipi() != null)
				personelIzin.setHesapTipi(izinTipi.getHesapTipi());

			pdksEntityController.saveOrUpdate(session, entityManager, personelIzin);
			if (eskiOnay != null && eskiOnay.getId() != null) {
				eskiOnay.setGuncellemeTarihi(eskiOnay.getGuncellemeTarihi());
				pdksEntityController.saveOrUpdate(session, entityManager, eskiOnay);

			}
			if (yeniOnay != null) {
				pdksEntityController.saveOrUpdate(session, entityManager, yeniOnay);
				// if (yeniOnay.getId() == null)
				// entityManager.persist(yeniOnay);
				// else
				// entityManager.merge(yeniOnay);

			}
			ccMailList = new ArrayList<String>();
			bccMailList = new ArrayList<String>();
			String adres = "onayYoneticiIzinMail.xhtml";
			boolean onayAdres = true;
			boolean izinOnaylandi = personelIzin.getIzinDurumu() != PersonelIzin.IZIN_DURUMU_REDEDILDI;
			boolean ikOnay = Boolean.FALSE;
			if (personelIzin.getIzinDurumu() == PersonelIzin.IZIN_DURUMU_IK_ONAYINDA) {
				ikOnay = Boolean.TRUE;

				if (ccMailList == null)
					ccMailList = new ArrayList<String>();
				if (izinSahibiUser != null && izinSahibiUser.getPdksPersonel().getId().equals(personelIzin.getIzinSahibi().getId())) {
					mailListesineEkle(ccMailList, personelIzin.getIzinSahibi().getEMailCCList());
					if (izinSahibiUser.getPdksPersonel().isHekim())
						hekimCCList();

					mailListesineEkle(bccMailList, personelIzin.getIzinSahibi().getEMailBCCList());

				}
				hekimIzinGenelMudurEkle(personelIzin);
			} else if (personelIzin.getIzinDurumu() == PersonelIzin.IZIN_DURUMU_REDEDILDI) {
				String iptalAciklama = "";
				if (redSebebiTanim != null) {
					iptalAciklama = redSebebiTanim.getAciklama();
					if (PdksUtil.hasStringValue(redSebebi)) {
						iptalAciklama += " - " + redSebebi.trim();
					}
					iptalAciklama = iptalAciklama + " nedeniyle ";
				}

				izinIptal = PdksUtil.replaceAll(personelIzin.getIzinTipiAciklama() + " " + iptalAciklama + authenticatedUser.getAdSoyad() + " tarafından rededilmiştir", "  ", " ");
				mailKonu = "İzin Ret";
				adres = "retPersonelIzinMail.xhtml";
				onayAdres = false;
				if (ccMailList == null)
					ccMailList = new ArrayList<String>();
				if (izinSahibiUser != null && izinSahibiUser.getPdksPersonel().getId().equals(personelIzin.getIzinSahibi().getId()) && izinOncekiDurum == PersonelIzin.IZIN_DURUMU_IK_ONAYINDA) {
					mailListesineEkle(ccMailList, personelIzin.getIzinSahibi().getEMailCCList());
					if (izinSahibiUser.getPdksPersonel().isHekim())
						hekimCCList();

					mailListesineEkle(bccMailList, personelIzin.getIzinSahibi().getEMailBCCList());
				}
				userList = iptalUserList(personelIzin);
				izinOnaylandi = Boolean.FALSE;
			}
			setMailIzin(personelIzin);
			if (ortakIslemler.isGenelMudur(null, personelIzin.getIzinSahibi(), session)) {
				userList = null;
				izinOnaylandi = Boolean.FALSE;
				listeOlustur = Boolean.TRUE;
			}

			if (personelIzin.getIzinTipi().getMailGonderimDurumu() != null)
				bilgiMailleriniEkle(personelIzin);
			if (userList != null && !userList.isEmpty()) {
				if (izinOnaylandi) {
					if (!ikOnay)
						izinMailAciklama = mailPersonelAciklama + " ait izin başlangıç tarihi " + authenticatedUser.dateTimeFormatla(mailIzin.getBaslangicZamani()) + " işe başlama tarihi " + authenticatedUser.dateTimeFormatla(mailIzin.getBitisZamani()) + " " + mailIzin.getIzinTipiAciklama()
								+ " onayınıza gönderilmiş bulunmaktadır.";
					else
						izinMailAciklama = mailPersonelAciklama + " ait izin başlangıç tarihi " + authenticatedUser.dateTimeFormatla(mailIzin.getBaslangicZamani()) + " işe başlama tarihi " + authenticatedUser.dateTimeFormatla(mailIzin.getBitisZamani()) + " " + mailPersonelAciklama
								+ " yönetici tarafından onaylanmış olup " + mailIzin.getIzinSahibi().getSirket().getDepartman().getDepartmanTanim().getAciklama() + " kayıtları için gönderilmiştir.";

				}
				ccIlebccMailKarsilastir();
				MailStatu mailSatu = null;
				try {
					MailObject mail = new MailObject();
					String konu = mailKonu, body = null;
					if (onayAdres) {
						konu = mailIzin != null && mailIzin.getId() != null ? mailPersonelAciklama + " İzin Onayı" : "İzin Kaydı";
						body = "<p>" + izinMailAciklama + "</p><p>Saygılarımla,</p>";
						body += "<a href=\"http://" + adres + "/onayimaGelenIzinler" + (mailIzin != null && mailIzin.getId() != null ? "?mId=" + mailIzin.getId() : "") + "\">" + ortakIslemler.getParameterKey("fromName") + " uygulamasına girmek	için buraya tıklayınız.</a>";

					} else {
						body = "<p>" + mailPersonelAciklama + " ait başlangıç tarihi " + authenticatedUser.dateTimeFormatla(mailIzin.getBaslangicZamani()) + " bitiş tarihi " + authenticatedUser.dateTimeFormatla(mailIzin.getBitisZamani()) + " " + izinIptal + ".</p>" + "<p>Saygılarımla,</p>";
					}
					if (!userList.isEmpty())
						setToList(userList);
					ortakIslemler.addMailPersonelUserList(toList, mail.getToList());
					ortakIslemler.addMailPersonelList(ccMailList, mail.getCcList());
					ortakIslemler.addMailPersonelList(bccMailList, mail.getBccList());
					mail.setSubject(konu);
					mail.setBody(body);
					mailSatu = ortakIslemler.mailSoapServisGonder(false, mail, renderer, "/email/" + adres, session);

				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());
					logger.error("hata /email/" + adres + " " + e.getMessage());
					PdksUtil.addMessageError("Mesaj gönderilmemiştir. " + e.getMessage());

				}
				if (mailSatu != null && mailSatu.isDurum()) {
					if (listeOlustur)
						PdksUtil.addMessageInfo("Mesaj Gönderildi.");
				}

			}
			setSeciliUser(izinSahibiUser);
			if (izinOnaylandi) {
				setSeciliUser(personelIzin.getOlusturanUser());
				if (!personelIzin.getIzinSahibi().getId().equals(personelIzin.getOlusturanUser().getPdksPersonel().getId())) {
					parametreMap.clear();
					parametreMap.put("pdksPersonel.id", personelIzin.getIzinSahibi().getId());
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					User izinUser = (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);
					if (izinUser != null)
						setSeciliUser(izinUser);
				}

				MailStatu mailSatu = null;
				try {
					userList = new ArrayList<User>();
					userList.add(getSeciliUser());
					setToList(userList);

					// ortakIslemler.mailGonder(renderer, "/email/onayPersonelIzinMail.xhtml");
					MailObject mail = new MailObject();
					String body = null;

					body = "<p>" + mailPersonelAciklama + " ait izin başlangıç tarihi " + authenticatedUser.dateTimeFormatla(mailIzin.getBaslangicZamani()) + " işe başlama tarihi " + authenticatedUser.dateTimeFormatla(mailIzin.getBitisZamani()) + " " + mailIzin.getIzinTipiAciklama() + " "
							+ authenticatedUser.getAdSoyad() + "  tarafından kayıt geçirilmiştir.</p>";
					body += "<p>Saygılarımızla,</p>";

					body += "<p><a href=\"http://" + adres + "\">" + ortakIslemler.getParameterKey("fromName") + " uygulamasına girmek için buraya tıklayınız.</a></p>";
					if (seciliUser.getPdksPersonel() == null || seciliUser.getPdksPersonel().isCalisiyor())
						mail.getToList().add(seciliUser.getMailPersonel());
					mail.setSubject("İzin Kaydı");
					mail.setBody(body);
					mailSatu = ortakIslemler.mailSoapServisGonder(false, mail, renderer, "/email/onayPersonelIzinMail.xhtml", session);

				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());
					logger.error("hata /email/" + adres + " " + e.getMessage());
					PdksUtil.addMessageError("Mesaj gönderilmemiştir. " + e.getMessage());

				}
				if (mailSatu != null && mailSatu.isDurum()) {
					if (listeOlustur)
						PdksUtil.addMessageInfo("Onay mesaj gönderildi.");
				}

			}
			session.flush();

			if (listeOlustur) {
				mailIzin = null;
				onayListesiOlustur();
			}

			basarili = Boolean.TRUE;

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			PdksUtil.addMessageError("izinOnayla : " + e.getMessage());
		}

		return sonuc;
	}

	public void sayfaGirisPDFAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		if (sessionIzin != null)
			setIzin(sessionIzin);
		aramaSecenekleri.setSessionClear(Boolean.FALSE);
	}

	public String izinSakla(PersonelIzin personelIzin) {
		sessionIzin = personelIzin;
		// <f:setPropertyActionListener value="#{izin}"
		// target="#{personelIzinGirisiHome.sessionIzin}" />
		return "";
	}

	public String secim(PersonelIzin personelIzin) {
		setIzin(personelIzin);
		return "/izin/izinPdf.xhtml";
	}

	public void izinListele(Boolean sessionClear, Personel izinSahibi) {
		servisAktarDurum = Boolean.FALSE;
		izinERPGiris = authenticatedUser.isIzinGirebilir() || !ortakIslemler.getParameterKey("izinERPUpdate").equals("1");
		sistemTarihi = PdksUtil.buGun();
		Date startDatedt = PdksUtil.getDate(getFiltreBaslangicZamani());
		Date endDatedt = PdksUtil.getDate(PdksUtil.tariheGunEkleCikar(getFiltreBitisZamani(), 1));
		ArrayList<String> sicilNoList = null;
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		if (sessionClear != null && sessionClear) {
			session.clear();
			if (izinSahibi != null)
				getPersonelVeri(izinSahibi);
		}
		if (listelenPersonel == null)
			listelenPersonel = new Personel();
		try {
			sicilNo = ortakIslemler.getSicilNo(sicilNo);
			aramaListeSecenekleri.setSicilNo(sicilNo);
			sicilNoList = ortakIslemler.getAramaPersonelSicilNo(aramaListeSecenekleri, Boolean.FALSE, session);
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			sicilNoList = new ArrayList<String>();
			PdksUtil.addMessageError("Hata " + e.getMessage());
		}
		List<PersonelIzin> izinList = null;
		if (sicilNoList.isEmpty() && PdksUtil.hasStringValue(sicilNo) && (authenticatedUser.isIK() || authenticatedUser.isAdmin())) {
			HashMap map = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT P." + Personel.COLUMN_NAME_ID + " from " + Personel.TABLE_NAME + " P WITH(nolock) ");
			sb.append(" WHERE P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=:basTarih ");
			sb.append(" AND P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + "<:bitTarih ");
			sb.append(" AND P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " =:p ");
			map.put("basTarih", startDatedt);
			map.put("bitTarih", endDatedt);
			map.put("p", sicilNo);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Personel> personeller = ortakIslemler.getPersonelList(sb, map);
			TreeMap<Long, Departman> departmanMap = ortakIslemler.getIzinGirenDepartmanMap(session);
			for (Iterator iterator = personeller.iterator(); iterator.hasNext();) {
				Personel pdksPersonel = (Personel) iterator.next();
				if (pdksPersonel.getDogumTarihi() == null && departmanMap.containsKey(pdksPersonel.getSirket().getDepartman().getId())) {
					PdksUtil.addMessageAvailableWarn(pdksPersonel.getPdksSicilNo() + " " + pdksPersonel.getAdSoyad() + " doğum tarihi tanımsız!");
					iterator.remove();
				}
			}
			if (personeller.size() == 1)
				sicilNoList.add(sicilNo);
		}
		if (!sicilNoList.isEmpty() || isGirisSSK()) {
			if (isGirisSSK()) {
				sicilNoList.clear();
				if (PdksUtil.hasStringValue(sicilNo))
					sicilNoList.add(ortakIslemler.getSicilNo(sicilNo));
			}
			if (isGirisSSK() || (sicilNoList != null && !sicilNoList.isEmpty())) {
				HashMap paramMap = new HashMap();
				TreeMap<Long, Sirket> sirketMap = null;
				HashMap map = new HashMap();
				if (isGirisSSK() && !authenticatedUser.isAdmin() && authenticatedUser.isIK()) {
					map.put(PdksEntityController.MAP_KEY_MAP, "getId");
					if (authenticatedUser.isGenelMudur())
						map.put("departman.id", authenticatedUser.getDepartman().getId());
					map.put("pdks", Boolean.TRUE);
					map.put("durum", Boolean.TRUE);

					map.put(PdksEntityController.MAP_KEY_SESSION, session);
					sirketMap = pdksEntityController.getObjectByInnerObjectMap(map, Sirket.class, Boolean.TRUE);
					paramMap.put("pdksSirket.id", new ArrayList(sirketMap.values()));

				}
				map.clear();
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT P." + Personel.COLUMN_NAME_ID + " from " + Personel.TABLE_NAME + " P WITH(nolock) ");
				sb.append(" WHERE P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=:basTarih ");
				sb.append(" AND P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + "<=:bitTarih ");
				map.put("basTarih", startDatedt);
				map.put("bitTarih", endDatedt);
				if (sirketMap != null) {
					if (!sirketMap.isEmpty()) {
						sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + ":s ");
						map.put("s", new ArrayList(sirketMap.values()));
					} else
						sb.append(" AND 1=2 ");

				}

				if (listelenPersonel.getEkSaha1() != null) {
					sb.append(" AND P." + Personel.COLUMN_NAME_EK_SAHA1 + "=:ekSaha1 ");
					map.put("ekSaha1", listelenPersonel.getEkSaha1().getId());
				}
				if (listelenPersonel.getEkSaha2() != null) {
					sb.append(" AND P." + Personel.COLUMN_NAME_EK_SAHA2 + "=:ekSaha2 ");
					map.put("ekSaha2", listelenPersonel.getEkSaha2().getId());
				}
				if (listelenPersonel.getEkSaha3() != null) {
					sb.append(" AND P." + Personel.COLUMN_NAME_EK_SAHA3 + "=:ekSaha3 ");
					map.put("ekSaha3", listelenPersonel.getEkSaha3().getId());
				}
				if (listelenPersonel.getEkSaha4() != null) {
					sb.append(" AND P." + Personel.COLUMN_NAME_EK_SAHA4 + "=:ekSaha4 ");
					map.put("ekSaha4", listelenPersonel.getEkSaha4().getId());
				}
				if (!sicilNoList.isEmpty()) {
					sb.append(" AND P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " :p ");
					map.put("p", sicilNoList);
				}
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<Long> idList = pdksEntityController.getObjectBySQLList(sb, map, null);
				List<Personel> personeller = null;
				if (!idList.isEmpty()) {
					personeller = ortakIslemler.getPersonelByIdList(idList, session);
				} else
					personeller = new ArrayList<Personel>();

				TreeMap<Long, Departman> departmanMap = ortakIslemler.getIzinGirenDepartmanMap(session);
				idList.clear();
				for (Iterator iterator = personeller.iterator(); iterator.hasNext();) {
					Personel pdksPersonel = (Personel) iterator.next();
					if (pdksPersonel.getDogumTarihi() == null && departmanMap.containsKey(pdksPersonel.getSirket().getDepartman().getId())) {
						iterator.remove();
					} else
						idList.add(pdksPersonel.getId());
				}
				sirketMap = null;
				if (!personeller.isEmpty()) {
					paramMap.clear();
					sb = new StringBuffer();
					sb.append("SELECT DISTINCT P.* FROM " + IzinTipi.TABLE_NAME + " P WITH(nolock)");
					if (isGirisSSK()) {
						sb.append(" INNER JOIN " + Tanim.TABLE_NAME + " T ON T." + Tanim.COLUMN_NAME_ID + "=P." + IzinTipi.COLUMN_NAME_IZIN_TIPI);
						sb.append(" AND T." + Tanim.COLUMN_NAME_TIPI + "=:tipi AND T." + Tanim.COLUMN_NAME_KODU + " like '%I%'");
						paramMap.put("tipi", Tanim.TIPI_IZIN_TIPI);
						// paramMap.put("kodu", IzinTipi.SSK_ISTIRAHAT);
					}
					sb.append(" WHERE P." + IzinTipi.COLUMN_NAME_DURUM + "=1 ");
					if (!isGirisSSK())
						sb.append(" AND P." + IzinTipi.COLUMN_NAME_BAKIYE_IZIN_TIPI + " IS NULL");
					if (session != null)
						paramMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<IzinTipi> izinTipleri = pdksEntityController.getObjectBySQLList(sb, paramMap, IzinTipi.class);
					sb = null;
					paramMap.clear();
					if (!izinTipleri.isEmpty()) {
						Date bitisTarihi = (Date) endDatedt.clone();
						if (PdksUtil.tarihKarsilastirNumeric(endDatedt, startDatedt) == 0)
							bitisTarihi = PdksUtil.tariheGunEkleCikar(bitisTarihi, 1);

						ortakIslemler.showSQLQuery(paramMap);
						paramMap.put("baslangicZamani<=", bitisTarihi);
						paramMap.put("bitisZamani>=", startDatedt);
						sb = new StringBuffer();
						for (Iterator iterator = izinTipleri.iterator(); iterator.hasNext();) {
							IzinTipi izinTipi = (IzinTipi) iterator.next();
							sb.append(izinTipi.getId() + (iterator.hasNext() ? "," : ""));
						}
						String sqlAdd = PdksEntityController.SELECT_KARAKTER + ".izinTipi.id in (" + sb.toString() + ")";
						sb = null;
						paramMap.put(PdksEntityController.MAP_KEY_SQLADD, sqlAdd);
						paramMap.put("izinSahibi.id", idList);
					}
					if (session != null)
						paramMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					if (paramMap.size() > 1)
						izinList = pdksEntityController.getObjectByInnerObjectListInLogic(paramMap, PersonelIzin.class);
					else
						izinList = null;
				}
				if (izinList != null && !izinList.isEmpty()) {
					if (isGirisSSK()) {
						izinList = PdksUtil.sortListByAlanAdi(izinList, "baslangicZamani", Boolean.TRUE);
						List<Long> list = new ArrayList<Long>();
						for (PersonelIzin personelIzin : izinList)
							list.add(personelIzin.getId());
						paramMap.clear();
						paramMap.put(PdksEntityController.MAP_KEY_MAP, "getPersonelIzinId");
						paramMap.put("personelIzin.id", list);
						if (session != null)
							paramMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						TreeMap<Long, IzinIstirahat> istirahatMap = pdksEntityController.getObjectByInnerObjectMap(paramMap, IzinIstirahat.class, Boolean.FALSE);
						setIzinIstirahatMap(istirahatMap);
					} else
						izinDosyalariOlustur(izinList);
					izinList = PdksUtil.sortListByAlanAdi(izinList, "olusturmaTarihi", Boolean.TRUE);
				}
			}
		}
		if (izinList == null)
			izinList = new ArrayList<PersonelIzin>();
		servisAktarDurum = ortakIslemler.erpIzinDoldur(izinList, session);
		setPersonelIzinList(izinList);

	}

	public String excelServiceAktar() {
		ortakIslemler.excelServiceAktar(personelIzinList);

		return "";
	}

	private void izinDosyalariOlustur(List<PersonelIzin> izinList) {
		TreeMap<Long, PersonelIzinDosya> izinDosyaMap = null;
		if (izinList != null && !izinList.isEmpty()) {
			HashMap paramMap = new HashMap();
			List<Long> list = new ArrayList<Long>(), listIK = new ArrayList<Long>();
			try {
				for (PersonelIzin personelIzin : izinList) {
					list.add(personelIzin.getId());
					personelIzin.setYilbasi(Boolean.FALSE);
					if (authenticatedUser.isIK() && personelIzin.getIzinDurumu() == PersonelIzin.IZIN_DURUMU_IK_ONAYINDA)
						listIK.add(personelIzin.getId());
				}
				if (!listIK.isEmpty()) {
					HashMap fields = new HashMap();
					StringBuffer sb = new StringBuffer();
					sb.append("SELECT P.IZIN_ID   FROM  YILBASI_SENELIK_IZIN_VIEW  P WITH(nolock) ");
					if (PdksUtil.getTestDurum()) {
						sb.append(" where P.IZIN_ID :i ");
						fields.put("i", listIK);
					} else
						sb.append(" where 1=2 ");
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					try {
						List idList = pdksEntityController.getObjectBySQLList(sb, fields, null);
						for (PersonelIzin personelIzin : izinList) {
							personelIzin.setYilbasi(idList.contains(new BigDecimal(personelIzin.getId())));
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}

				}
				listIK = null;
				paramMap.put(PdksEntityController.MAP_KEY_MAP, "getPersonelIzinId");
				paramMap.put("personelIzin.id", list);
				if (session != null)
					paramMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				izinDosyaMap = pdksEntityController.getObjectByInnerObjectMap(paramMap, PersonelIzinDosya.class, Boolean.FALSE);
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());

			}
			list = null;
			paramMap = null;
		}
		if (izinDosyaMap == null)
			izinDosyaMap = new TreeMap<Long, PersonelIzinDosya>();
		setIzinDosyaMap(izinDosyaMap);
	}

	public String onaylamaIslemi() {
		if (onayDurum.equals("true")) {
			if (guncellenecekIzin.getIzinKagidiGeldi() == null || !guncellenecekIzin.getIzinKagidiGeldi()) {
				guncellenecekIzin.setIzinKagidiGeldi(Boolean.TRUE);
				pdksEntityController.save(guncellenecekIzin, session);
				session.flush();
				guncellenecekIzin = null;
				izinListele(Boolean.FALSE, null);
			}
			onayDurum = "";
		}
		return "";

	}

	public boolean izinIptalEdilebilir(PersonelIzin personelIzin) {

		if (personelIzin == null)
			return false;
		// boolean denklestirmeIptal = Boolean.TRUE;
		boolean durum = Boolean.TRUE;
		// boolean uyari = Boolean.FALSE;
		// boolean admin = authenticatedUser.isAdmin() ||
		// authenticatedUser.isIK();

		return durum;

	}

	public void izinKontrol(PersonelIzin izin) {
		izinIptalGoster = izinDonemKontrol(izin);
		if (izinIptalGoster && izinERPGiris) {
			nedenSor = Boolean.FALSE;
			setRedSebebi("");
			setRedSebebiTanim(null);
			List<Tanim> redSebebiList = null;
			if (!authenticatedUser.isAdmin() && !authenticatedUser.isIK())
				nedenSor = izin.getIzinDurumu() == PersonelIzin.IZIN_DURUMU_IKINCI_YONETICI_ONAYINDA || izin.getIzinDurumu() == PersonelIzin.IZIN_DURUMU_IK_ONAYINDA;
			if (nedenSor) {
				// HashMap paramMap = new HashMap();
				// paramMap.clear();
				// paramMap.put("tipi", Tanim.TIPI_ONAYLAMAMA_NEDEN);
				// paramMap.put("durum", Boolean.TRUE);
				// if (session != null)
				// paramMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				redSebebiList = ortakIslemler.getTanimList(Tanim.TIPI_ONAYLAMAMA_NEDEN, session);

			}
			setRedSebebiList(redSebebiList);
			izin.setIptalEdilir(true);

		}
		setGuncellenecekIzin(izin);
	}

	/**
	 * @param izin
	 * @return
	 */
	private boolean izinDonemKontrol(PersonelIzin izin) {
		boolean donemKontrol = true;
		String d1 = PdksUtil.convertToDateString(izin.getBaslangicZamani(), "yyyyMM"), d2 = PdksUtil.convertToDateString(izin.getBitisZamani(), "yyyyMM");
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("WITH DENKAY AS ( ");
		sb.append(" SELECT " + DenklestirmeAy.COLUMN_NAME_YIL + "*100+" + DenklestirmeAy.COLUMN_NAME_AY + " AS DONEM,* FROM " + DenklestirmeAy.TABLE_NAME + "  WITH(nolock) ");
		sb.append("	 WHERE " + DenklestirmeAy.COLUMN_NAME_DURUM + "=0");
		sb.append(" ) ");
		sb.append(" SELECT  PD.* FROM  DENKAY D WITH(nolock) ");
		sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON  P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + "=:p ");
		sb.append(" INNER JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON P." + Personel.COLUMN_NAME_ID + "=PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + "  AND PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=D." + DenklestirmeAy.COLUMN_NAME_ID);
		sb.append(" AND PD." + PersonelDenklestirme.COLUMN_NAME_DURUM + "=1");
		sb.append(" WHERE  D.DONEM>=:d1 AND  D.DONEM<=:d2 ");
		sb.append(" ORDER BY D.DONEM");
		fields.put("d1", Long.parseLong(d1));
		fields.put("d2", Long.parseLong(d2));
		fields.put("p", izin.getIzinSahibi().getPdksSicilNo());
		fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PersonelDenklestirme> list = pdksEntityController.getObjectBySQLList(sb, fields, PersonelDenklestirme.class);
		if (!list.isEmpty()) {
			donemKontrol = authenticatedUser.isAdmin();
			StringBuffer donemStr = new StringBuffer();
			for (Iterator iterator2 = list.iterator(); iterator2.hasNext();) {
				PersonelDenklestirme personelDenklestirme = (PersonelDenklestirme) iterator2.next();
				DenklestirmeAy denklestirmeAy = personelDenklestirme.getDenklestirmeAy();
				donemStr.append(denklestirmeAy.getAyAdi() + " " + denklestirmeAy.getYil());
				if (iterator2.hasNext())
					donemStr.append(", ");
				if (personelDenklestirme.getDurum().equals(Boolean.FALSE))
					iterator2.remove();

			}

			String str = donemStr.toString();
			PdksUtil.addMessageAvailableWarn((str + " " + (list.size() > 1 ? " dönemleri" : " dönemi") + " kapalıdır"));
		}
		return donemKontrol;
	}

	private void hekimCCList() {
		String hekimCCMail = ortakIslemler.getParameterKey("hekimCCMail");
		if (PdksUtil.hasStringValue(hekimCCMail)) {
			List<String> hekimCCListesi = Arrays.asList(hekimCCMail.split(","));
			for (Iterator iterator = hekimCCListesi.iterator(); iterator.hasNext();) {
				String str = (String) iterator.next();
				try {
					String mail = PdksUtil.getMailAdres(str);
					if (mail != null && !ccMailList.contains(mail))
						ccMailList.add(mail);
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}

			}
		}
	}

	@Transactional
	public String izinERPIptal(PersonelIzin izin) {
		String konu = mailKonu, body = null;

		mailIzin = izin;
		if (toList == null)
			toList = new ArrayList<User>();
		else
			toList.clear();
		if (ccList == null)
			ccList = new ArrayList<User>();
		else
			ccList.clear();
		if (!authenticatedUser.isAdmin())
			toList.add(authenticatedUser);
		if (ortakIslemler.getParameterKey("izinERPDelete").equals("1"))
			ccList = ortakIslemler.IKKullanicilariBul(new ArrayList<User>(), authenticatedUser.getPdksPersonel(), session);
		if (bccList == null)
			bccList = new ArrayList<User>();
		else
			bccList.clear();
		List<User> adminUserList = ortakIslemler.bccAdminAdres(session, null);
		if (!adminUserList.isEmpty())
			bccList.addAll(adminUserList);
		mailKonu = "İzin İptal";
		String mailPersonelAciklama = getMailPersonelAciklama(mailIzin.getIzinSahibi());

		ccIlebccMailKarsilastir();
		izinIptal = PdksUtil.replaceAll(izin.getIzinTipiAciklama() + " " + authenticatedUser.getAdSoyad() + " tarafından iptal edilmiştir", "  ", " ");
		body = "<p>" + mailPersonelAciklama + " ait başlangıç tarihi " + authenticatedUser.dateTimeFormatla(mailIzin.getBaslangicZamani()) + " bitiş tarihi " + authenticatedUser.dateTimeFormatla(mailIzin.getBitisZamani()) + " " + izinIptal + ".</p>" + "<p>Saygılarımla,</p>";

		MailStatu mailSatu = null;
		try {
			MailObject mail = new MailObject();

			body = "<p>" + mailIzin.getIzinSahibi().getAdSoyad() + " ait başlangıç tarihi " + authenticatedUser.dateTimeFormatla(mailIzin.getBaslangicZamani()) + " bitiş tarihi " + authenticatedUser.dateTimeFormatla(mailIzin.getBitisZamani()) + " " + izinIptal + ".</p>" + "<p>Saygılarımla,</p>";

			if (!userList.isEmpty())
				setToList(userList);
			ortakIslemler.addMailPersonelUserList(toList, mail.getToList());
			ortakIslemler.addMailPersonelUserList(ccList, mail.getCcList());
			ortakIslemler.addMailPersonelUserList(bccList, mail.getBccList());
			mail.setSubject(konu);
			mail.setBody(body);
			mailSatu = ortakIslemler.mailSoapServisGonder(false, mail, renderer, "/email/" + adres, session);

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			logger.error("hata /email/" + adres + " " + e.getMessage());
			PdksUtil.addMessageError("Mesaj gönderilmemiştir. " + e.getMessage());

		} finally {
			izin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_REDEDILDI);
			izin.setGuncellemeTarihi(new Date());
			User guncelleyenUser = !authenticatedUser.isAdmin() ? authenticatedUser : izin.getOlusturanUser();
			izin.setGuncelleyenUser(guncelleyenUser);
			pdksEntityController.saveOrUpdate(session, entityManager, izin);
			session.flush();
			setGuncellenecekIzin(null);
			izinListele(Boolean.TRUE, null);
			baslangicDegerleri();
			PdksUtil.addMessageAvailableInfo("İzin iptal edilmiştir.");
			izinIptalGoster = false;
		}
		if (mailSatu != null && mailSatu.isDurum()) {
			PdksUtil.addMessageAvailableInfo("Mesaj Gönderildi.");
		}

		return "";
	}

	/**
	 * @param personel
	 * @return
	 */
	private String getMailPersonelAciklama(Personel personel) {
		String str = personel.getSirket().getAd() + " " + (personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() + " " + bolumAciklama + " " : "") + personel.getAdSoyad();
		return str;
	}

	@Transactional
	public String izinIptal(PersonelIzin izin) {
		try {

			int izinOncekiDurum = izin.getIzinDurumu();
			HashMap parametreMap = new HashMap();
			parametreMap.put("pdksPersonel.id", izin.getIzinSahibi().getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			User izinSahibiUser = (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);
			if (izinIptalEdilebilir(izin)) {
				toList = iptalUserList(izin);
				setMailIzin(izin);
				ccMailList = new ArrayList<String>();
				bccMailList = new ArrayList<String>();
				if (izinSahibiUser != null && (izinOncekiDurum == PersonelIzin.IZIN_DURUMU_IK_ONAYINDA || izinOncekiDurum == PersonelIzin.IZIN_DURUMU_ONAYLANDI)) {
					mailListesineEkle(ccMailList, izin.getIzinSahibi().getEMailCCList());
					if (izinSahibiUser.getPdksPersonel().isHekim())
						hekimCCList();
					mailListesineEkle(bccMailList, izin.getIzinSahibi().getEMailBCCList());
				}
				MailStatu mailSatu = null;
				try {
					String mailPersonelAciklama = getMailPersonelAciklama(izin.getIzinSahibi());
					izinIptal = izin.getIzinTipiAciklama() + " " + authenticatedUser.getAdSoyad() + " tarafından iptal edilmiştir";
					mailKonu = "İzin İptal";
					MailObject mail = new MailObject();
					mail.setSubject(mailKonu);
					String body = "<p>" + mailPersonelAciklama + " ait başlangıç tarihi " + authenticatedUser.dateTimeFormatla(izin.getBaslangicZamani()) + " bitiş tarihi " + authenticatedUser.dateTimeFormatla(izin.getBitisZamani()) + " " + izinIptal + ".</p>";
					mail.setBody(body);
					if (!userList.isEmpty())
						setToList(userList);
					ortakIslemler.addMailPersonelUserList(toList, mail.getToList());
					ortakIslemler.addMailPersonelList(ccMailList, mail.getCcList());
					ortakIslemler.addMailPersonelList(bccMailList, mail.getBccList());
					mailSatu = ortakIslemler.mailSoapServisGonder(false, mail, renderer, "/email/retPersonelIzinMail.xhtml", session);
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}
				if (mailSatu != null && mailSatu.isDurum())
					PdksUtil.addMessageInfo("Mesaj gönderilmiştir.");
				if (!nedenSor) {
					redSebebiTanim = null;
					redSebebi = "";
				}

				ortakIslemler.izinIptal(izin, redSebebiTanim, redSebebi, session);
				setGuncellenecekIzin(null);
				izinListele(Boolean.TRUE, null);
				baslangicDegerleri();

				PdksUtil.addMessageInfo("İzin iptal edilmiştir.");
			}

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			PdksUtil.addMessageError("İzin iptal hata : " + e.getMessage());
		}
		setIzinIptalGoster(Boolean.FALSE);

		return "";

	}

	private List<User> iptalUserList(PersonelIzin izin) {

		List<User> list = new ArrayList<User>();
		User user = izin.getOlusturanUser();
		HashMap parametreMap = new HashMap();
		parametreMap.put("pdksPersonel.id", izin.getIzinSahibi().getId());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		User izinUser = (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);
		if (izinUser != null && !izinUser.getId().equals(user.getId()))
			list.add(izinUser);
		list.add(user);
		if (izinUser == null)
			izinUser = new User();
		izinUser.setPdksPersonel(izin.getIzinSahibi());
		setSeciliUser(izinUser);
		setGuncellenecekIzin(izin);

		return list;
	}

	public boolean ardisikIzinKontrol(PersonelIzin izin) {
		boolean izinvar = Boolean.FALSE;
		HashMap map = new HashMap();
		map.put("izinTipiTanim=", izin.getIzinTipi().getIzinTipiTanim());
		map.put("durum=", Boolean.TRUE);
		map.put("izinAralikSaat>", 0D);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<IzinTipiBirlesikHaric> birlesikIzinTipleri = null;
		try {
			birlesikIzinTipleri = pdksEntityController.getObjectByInnerObjectListInLogic(map, IzinTipiBirlesikHaric.class);

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			birlesikIzinTipleri = null;

		}
		if (birlesikIzinTipleri != null && !birlesikIzinTipleri.isEmpty()) {
			Calendar cal = Calendar.getInstance();
			for (IzinTipiBirlesikHaric izinTipiBirlesikHaric : birlesikIzinTipleri) {
				map.clear();
				int sure = new Double(izinTipiBirlesikHaric.getIzinAralikSaat() * 60).intValue();
				cal.setTime(izin.getBaslangicZamani());
				cal.add(Calendar.MINUTE, sure);
				Date bitisZaman = cal.getTime();
				cal.setTime(izin.getBitisZamani());
				cal.add(Calendar.MINUTE, -sure);
				Date baslamaZaman = cal.getTime();
				map.put("izinSahibi=", izin.getIzinSahibi());
				map.put("baslangicZamani<=", bitisZaman);
				map.put("bitisZamani>=", baslamaZaman);
				map.put("izinTipi.izinTipiTanim=", izinTipiBirlesikHaric.getBirlesikIzinTipiTanim());
				map.put("izinDurumu not ", Arrays.asList(new Integer[] { PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL, PersonelIzin.IZIN_DURUMU_REDEDILDI }));
				map.put("izinTipi.bakiyeIzinTipi=", null);
				map.put("hesapTipi=", PersonelIzin.HESAP_TIPI_GUN);
				if (izin.getId() != null)
					map.put("id<>", izin.getId());
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				try {
					PersonelIzin ardisikIzin = (PersonelIzin) pdksEntityController.getObjectByInnerObjectInLogic(map, PersonelIzin.class);
					if (ardisikIzin != null) {
						if (ardisikIzin.getBitisZamani().getTime() != baslamaZaman.getTime() && ardisikIzin.getBaslangicZamani().getTime() != bitisZaman.getTime()) {
							izinvar = Boolean.TRUE;
							PdksUtil.addMessageWarn(izinTipiBirlesikHaric.getIzinTipiTanim().getAciklama() + " ile " + izinTipiBirlesikHaric.getBirlesikIzinTipiTanim().getAciklama() + " birleştirilemez!");
							break;
						}

					}
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}

			}

		}

		return izinvar;
	}

	public void ekle() {
		PersonelIzin personelIzin = getInstance();
		if (Long.parseLong(PdksUtil.convertToDateString(personelIzin.getBaslangicZamani(), "yyyyMMddHHmm")) >= Long.parseLong(PdksUtil.convertToDateString(personelIzin.getBitisZamani(), "yyyyMMddHHmm"))) {
			PdksUtil.addMessageWarn("İzin başlangıç zamanı bitiş zamanından büyük veya eşit olamaz! ");

		}
	}

	public String guncellenenIzinSifirla() {
		try {
			if (getGuncellenecekIzin() != null && getGuncellenecekIzin().getId() != null) {
				// session.refresh(getGuncellenecekIzin());
				// if (izinDosya.getId() != null)
				// session.refresh(izinDosya);
				// if (istirahat != null && istirahat.getId() != null)
				// session.refresh(istirahat);

			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		setGuncellenecekIzin(null);
		setBaslangicSaat(8);
		setBaslangicDakika(0);
		setBitisSaat(8);
		setBitisDakika(0);
		izinListele(null, null);
		return "";
	}

	public String kapat() {
		setVisibled(Boolean.FALSE);
		return "";
	}

	public void tekPersonelSecimIslemi(Personel izinSahibi) {
		kapat();
		izinSahibi = getPersonelVeri(izinSahibi);
		setIzinliSahibi(izinSahibi);
		getInstance().setIzinSahibi(izinSahibi);

	}

	@Transactional
	public String kaydet() throws Exception {
		List<Integer> izinDurumlari = Arrays.asList(new Integer[] { PersonelIzin.IZIN_DURUMU_REDEDILDI, PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL });
		String durum = "persist";
		setSeciliHesapTipi(hesapTipi);
		setIzinIptalGoster(Boolean.FALSE);
		double izinSuresiSaat = 0;
		double izinSuresiGun = 0;
		if (guncellenecekIzin != null && guncellenecekIzin.getId() != null)
			setInstance(guncellenecekIzin);
		else
			getInstance().setIzinTipi(seciliIzinTipi);

		setIzinIptalGoster(Boolean.FALSE);
		PersonelIzin personelIzin = getInstance();
		IzinTipi izinTipi = personelIzin.getIzinTipi();
		// boolean senelikIzin = izinTipi.isSenelikIzin();
		Boolean hekim = null;
		try {
			hekim = personelIzin != null && personelIzin.getIzinSahibi() != null ? personelIzin.getIzinSahibi().isHekim() : Boolean.FALSE;
		} catch (Exception e) {
			hekim = Boolean.FALSE;
		}
		if (hekim == null)
			hekim = Boolean.FALSE;
		Double artikGun = null;
		try {
			artikGun = izinTipi.getArtikIzinGun();
		} catch (Exception e) {
			artikGun = null;
		}
		double artikIzinGun = artikGun != null && !hekim ? artikGun.doubleValue() : 0D;
		HashMap parametreMap = new HashMap();
		parametreMap.put("id", authenticatedUser.getId());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		User updateUser = (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);
		if (personelIzin.getIzinSahibi() == null)
			personelIzin.setIzinSahibi(izinliSahibi);
		Personel izinSahibi = personelIzin.getIzinSahibi();
		if (isGirisSSK()) {
			getIstirahatIzni(izinSahibi);
			personelIzin.setIzinTipi(seciliIzinTipi);
		}

		PersonelIzinDetay izinDetay = null;
		List<PersonelIzinDetay> izinDetayList = new ArrayList<PersonelIzinDetay>();

		Calendar cal = Calendar.getInstance();
		cal.setTime(PdksUtil.getDate(personelIzin.getBaslangicZamani()));
		int yil = cal.get(Calendar.YEAR);
		cal.set(Calendar.HOUR_OF_DAY, baslangicSaat);
		cal.set(Calendar.MINUTE, baslangicDakika);
		personelIzin.setBaslangicZamani(cal.getTime());
		cal.setTime(PdksUtil.getDate(personelIzin.getBitisZamani()));
		if (isSaatGosterilecek()) {
			cal.set(Calendar.HOUR_OF_DAY, bitisSaat);
			cal.set(Calendar.MINUTE, bitisDakika);
		} else {
			cal.set(Calendar.HOUR_OF_DAY, baslangicSaat);
			cal.set(Calendar.MINUTE, baslangicDakika);

		}
		personelIzin.setBitisZamani(cal.getTime());
		// KONTROL 1
		if (!saatGosterilecek && Long.parseLong(PdksUtil.convertToDateString(personelIzin.getBaslangicZamani(), "yyyyMMddHHmm")) >= Long.parseLong(PdksUtil.convertToDateString(personelIzin.getBitisZamani(), "yyyyMMddHHmm"))) {
			PdksUtil.addMessageWarn("İzin başlangıç zamanı bitiş zamanından büyük veya eşit olamaz! ");
			durum = "";
		}

		// KONTROL-2 tarih araligina denk gelen baska bir izni var mi?
		List<Personel> personeller = new ArrayList<Personel>();
		personeller.add(personelIzin.getIzinSahibi());
		TreeMap<String, VardiyaGun> vardiyaMap = ortakIslemler.getIslemVardiyalar(personeller, PdksUtil.tariheGunEkleCikar(personelIzin.getBaslangicZamani(), -7), PdksUtil.tariheGunEkleCikar(personelIzin.getBitisZamani(), 1), Boolean.FALSE, session, Boolean.TRUE);

		VardiyaGun pdksVardiyaGun1 = new VardiyaGun(izinliSahibi, null, personelIzin.getBaslangicZamani());
		VardiyaGun pdksVardiyaGun2 = new VardiyaGun(izinliSahibi, null, personelIzin.getBitisZamani());

		if (izinTipi == null) {
			durum = "";
			PdksUtil.addMessageWarn("İzin Tipi Seçiniz!");
		} else {
			if (!izinTipi.getTakvimGunumu() || izinTipi.getSaatGosterilecek()) {
				String oncekiBasGunTarihStr = personelIzin.getIzinSahibi().getPdksSicilNo() + "_" + PdksUtil.convertToDateString(PdksUtil.tariheGunEkleCikar(pdksVardiyaGun1.getVardiyaDate(), -1), "yyyyMMdd");
				VardiyaGun pdksVardiyaGunBasOncesi = vardiyaMap.containsKey(oncekiBasGunTarihStr) ? (VardiyaGun) vardiyaMap.get(oncekiBasGunTarihStr) : null;
				if (pdksVardiyaGunBasOncesi != null && !pdksVardiyaGunBasOncesi.getVardiya().isCalisma())
					pdksVardiyaGunBasOncesi = null;
				if (vardiyaMap.containsKey(pdksVardiyaGun1.getVardiyaKey()) || pdksVardiyaGunBasOncesi != null) {
					pdksVardiyaGun1 = vardiyaMap.containsKey(pdksVardiyaGun1.getVardiyaKey()) ? (VardiyaGun) vardiyaMap.get(pdksVardiyaGun1.getVardiyaKeyStr()).clone() : null;
					boolean devam = Boolean.FALSE;
					if (pdksVardiyaGun1 != null && pdksVardiyaGun1.getVardiya().isCalisma()) {
						devam = Boolean.TRUE;
					} else if (pdksVardiyaGunBasOncesi != null) {
						if (pdksVardiyaGunBasOncesi.getIslemVardiya() == null)
							pdksVardiyaGunBasOncesi.setVardiyaZamani();
						if (pdksVardiyaGunBasOncesi.getIslemVardiya().getVardiyaBitZaman().getTime() >= personelIzin.getBaslangicZamani().getTime() && pdksVardiyaGunBasOncesi.getIslemVardiya().getVardiyaBasZaman().getTime() <= personelIzin.getBaslangicZamani().getTime()) {
							devam = Boolean.TRUE;
							pdksVardiyaGun1 = pdksVardiyaGunBasOncesi;
						}

					}
					if (!devam) {
						PdksUtil.addMessageWarn("İzin başlangıç zamanı çalışma olmalıdır! " + (pdksVardiyaGun1.getVardiya() != null ? "Vardiya Adı :" + pdksVardiyaGun1.getVardiya().getAdi() + " " + PdksUtil.convertToDateString(pdksVardiyaGun1.getVardiyaDate(), PdksUtil.getDateFormat()) : ""));
						durum = "";
					} else {
						if (pdksVardiyaGun1.getIslemVardiya() == null)
							pdksVardiyaGun1.setVardiyaZamani();
						if (getHesapTipi() == PersonelIzin.HESAP_TIPI_GUN) {
							personelIzin.setBaslangicZamani(pdksVardiyaGun1.getIslemVardiya().getVardiyaBasZaman());
						} else if (pdksVardiyaGun1.getIslemVardiya().getVardiyaBasZaman().getTime() > personelIzin.getBaslangicZamani().getTime()) {
							Date vardiyaBasTarih = oncekiVardiyami(vardiyaMap, personelIzin.getBaslangicZamani(), personelIzin, Boolean.FALSE);
							personelIzin.setBaslangicZamani(vardiyaBasTarih);
						}

					}
				}
				String oncekiGunTarihStr = personelIzin.getIzinSahibi().getPdksSicilNo() + "_" + PdksUtil.convertToDateString(PdksUtil.tariheGunEkleCikar(pdksVardiyaGun2.getVardiyaDate(), -1), "yyyyMMdd");
				VardiyaGun pdksVardiyaGunBitisOncesi = vardiyaMap.containsKey(oncekiGunTarihStr) ? (VardiyaGun) vardiyaMap.get(oncekiGunTarihStr) : null;
				if (pdksVardiyaGunBitisOncesi != null && !pdksVardiyaGunBitisOncesi.getVardiya().isCalisma())
					pdksVardiyaGunBitisOncesi = null;
				if (vardiyaMap.containsKey(pdksVardiyaGun2.getVardiyaKeyStr()) || pdksVardiyaGunBitisOncesi != null) {
					pdksVardiyaGun2 = vardiyaMap.containsKey(pdksVardiyaGun2.getVardiyaKeyStr()) ? (VardiyaGun) vardiyaMap.get(pdksVardiyaGun2.getVardiyaKeyStr()).clone() : null;
					boolean devam = Boolean.FALSE;
					if (pdksVardiyaGun2 != null && pdksVardiyaGun2.getVardiya().isCalisma()) {
						devam = Boolean.TRUE;
					} else {
						if (pdksVardiyaGunBitisOncesi != null) {
							if (pdksVardiyaGunBitisOncesi.getIslemVardiya() == null)
								pdksVardiyaGunBitisOncesi.setVardiyaZamani();
							if (pdksVardiyaGunBitisOncesi.getIslemVardiya().getVardiyaBitZaman().getTime() >= personelIzin.getBitisZamani().getTime() && pdksVardiyaGunBitisOncesi.getIslemVardiya().getVardiyaBasZaman().getTime() <= personelIzin.getBitisZamani().getTime()) {
								devam = Boolean.TRUE;
								pdksVardiyaGun2 = pdksVardiyaGunBitisOncesi;
							}
						}
					}

					if (devam) {
						if (pdksVardiyaGun2.getIslemVardiya() == null)
							pdksVardiyaGun2.setVardiyaZamani();
						if (getHesapTipi() != PersonelIzin.HESAP_TIPI_GUN) {
							if (pdksVardiyaGun2.getIslemVardiya().getVardiyaBitZaman().getTime() < personelIzin.getBitisZamani().getTime())
								personelIzin.setBitisZamani(pdksVardiyaGun2.getIslemVardiya().getVardiyaBitZaman());

							if (pdksVardiyaGun2.getIslemVardiya().getVardiyaBasZaman().getTime() > personelIzin.getBitisZamani().getTime())
								personelIzin.setBitisZamani(pdksVardiyaGun2.getIslemVardiya().getVardiyaBasZaman());

						} else {
							personelIzin.setBitisZamani(pdksVardiyaGun2.getIslemVardiya().getVardiyaBasZaman());
						}

					} else {
						if (!(artikIzinGun > 0.0d && pdksVardiyaGun2.getVardiya() != null && pdksVardiyaGun2.getVardiya().isOffGun())) {
							PdksUtil.addMessageWarn("İzin bitiş zamanı çalışma olmalıdır! " + (pdksVardiyaGun2.getVardiya() != null ? "Vardiya Adı :" + pdksVardiyaGun2.getVardiya().getAdi() + " " + PdksUtil.convertToDateString(pdksVardiyaGun2.getVardiyaDate(), PdksUtil.getDateFormat()) : ""));
							durum = "";
						}

					}

				}

			} else if (izinTipi.getTakvimGunumu()) {
				if (vardiyaMap.containsKey(pdksVardiyaGun1.getVardiyaKeyStr())) {
					pdksVardiyaGun1 = (VardiyaGun) vardiyaMap.get(pdksVardiyaGun1.getVardiyaKeyStr()).clone();
					if (pdksVardiyaGun1.getIslemVardiya() == null)
						pdksVardiyaGun1.setVardiyaZamani();
					if (pdksVardiyaGun1.getVardiya().isCalisma()) {
						personelIzin.setBaslangicZamani(pdksVardiyaGun1.getIslemVardiya().getVardiyaBasZaman());
						Calendar cal1 = Calendar.getInstance();
						cal1.setTime(personelIzin.getBaslangicZamani());
						int saat = cal1.get(Calendar.HOUR_OF_DAY);
						int dakika = cal1.get(Calendar.MINUTE);
						cal1.setTime(personelIzin.getBitisZamani());
						cal1.set(Calendar.HOUR_OF_DAY, saat);
						cal1.set(Calendar.MINUTE, dakika);
						personelIzin.setBitisZamani(cal1.getTime());

					} else {
						Calendar cal1 = Calendar.getInstance();
						cal1.setTime(personelIzin.getBaslangicZamani());
						int saat = 8;
						int dakika = 30;
						cal1.set(Calendar.HOUR_OF_DAY, saat);
						cal1.set(Calendar.MINUTE, dakika);
						personelIzin.setBaslangicZamani((Date) cal1.getTime().clone());
						cal1.setTime(PdksUtil.getDate(personelIzin.getBitisZamani()));
						cal1.set(Calendar.HOUR_OF_DAY, saat);
						cal1.set(Calendar.MINUTE, dakika);
						personelIzin.setBitisZamani(cal1.getTime());
					}
				}
				if (vardiyaMap.containsKey(pdksVardiyaGun2.getVardiyaKeyStr())) {
					pdksVardiyaGun2 = (VardiyaGun) vardiyaMap.get(pdksVardiyaGun2.getVardiyaKeyStr()).clone();
					if (pdksVardiyaGun2.getIslemVardiya() == null)
						pdksVardiyaGun2.setVardiyaZamani();
					if (pdksVardiyaGun2.getVardiya().isCalisma())
						personelIzin.setBitisZamani(pdksVardiyaGun2.getIslemVardiya().getVardiyaBasZaman());
					else if (pdksVardiyaGun2.getOncekiVardiya() != null && pdksVardiyaGun2.getOncekiVardiyaGun().getIslemVardiya() != null && pdksVardiyaGun2.getOncekiVardiyaGun().getIslemVardiya().isCalisma()) {
						Vardiya pdksVardiya = pdksVardiyaGun2.getOncekiVardiyaGun().getIslemVardiya();
						Date bitTar = PdksUtil.tariheGunEkleCikar(pdksVardiya.getVardiyaBasZaman(), 1);
						if (pdksVardiya.getVardiyaBitZaman().after(personelIzin.getBitisZamani()))
							bitTar = pdksVardiya.getVardiyaBitZaman();
						personelIzin.setBitisZamani(bitTar);
					}

				}
			}
			if (PdksUtil.hasStringValue(durum) && !izinTipi.getTakvimGunumu())
				durum = isHaftaTatilKontrolIzin(personelIzin);
		}
		if (PdksUtil.hasStringValue(durum) && (izinTipi.isSenelikIzin() || izinTipi.isMazeretIzin())) {
			List tarihAraligiIzinList = null;
			HashMap param = new HashMap();
			param.put("izinSahibi=", izinSahibi);
			param.put("izinTipi.bakiyeIzinTipi=", null);
			param.put("bitisZamani>=", PdksUtil.tariheGunEkleCikar(personelIzin.getBaslangicZamani(), -1));
			param.put("baslangicZamani<=", PdksUtil.tariheGunEkleCikar(personelIzin.getBaslangicZamani(), 1));
			param.put("izinDurumu not", izinDurumlari);
			if (personelIzin.getId() != null)
				param.put("id<>", personelIzin.getId());
			if (session != null)
				param.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				tarihAraligiIzinList = pdksEntityController.getObjectByInnerObjectListInLogic(param, PersonelIzin.class);

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				tarihAraligiIzinList = null;
			}
			double saatFark = Math.abs(PdksUtil.getSaatFarki(personelIzin.getBitisZamani(), personelIzin.getBaslangicZamani()));
			boolean kesisenTarihteIzinVar = Boolean.FALSE;
			// bu durumda vekil transfer gibi islem yapiacak
			if (tarihAraligiIzinList != null && (getHesapTipi() == PersonelIzin.HESAP_TIPI_GUN || saatFark >= 24)) {

				for (Iterator iterator = tarihAraligiIzinList.iterator(); iterator.hasNext();) {
					PersonelIzin tempIzin = (PersonelIzin) iterator.next();
					if (tempIzin.getIzinTipi().isSenelikIzin() || tempIzin.getIzinTipi().isMazeretIzin()) {
						double saatIzinFark = Math.abs(PdksUtil.getSaatFarki(tempIzin.getBitisZamani(), tempIzin.getBaslangicZamani()));
						if (saatIzinFark < 24)
							continue;
						double saatFark1 = Math.abs(PdksUtil.getSaatFarki(personelIzin.getBitisZamani(), tempIzin.getBaslangicZamani()));
						if (saatFark1 < 24) {
							kesisenTarihteIzinVar = Boolean.TRUE;
							PdksUtil.addMessageWarn("İki izin arası 1 günden az olamaz! " + PdksUtil.convertToDateString(personelIzin.getBitisZamani(), "yyy/MM/dd") + "-" + PdksUtil.convertToDateString(tempIzin.getBaslangicZamani(), "yyy/MM/dd"));
						} else {
							double saatFark2 = Math.abs(PdksUtil.getSaatFarki(tempIzin.getBitisZamani(), personelIzin.getBaslangicZamani()));
							if (saatFark2 < 24) {
								kesisenTarihteIzinVar = Boolean.TRUE;
								PdksUtil.addMessageWarn("İki izin arası 1 günden az olamaz! " + PdksUtil.convertToDateString(tempIzin.getBitisZamani(), "yyy/MM/dd") + "-" + PdksUtil.convertToDateString(personelIzin.getBaslangicZamani(), "yyy/MM/dd"));

							}

						}
						if (kesisenTarihteIzinVar)
							break;

					}

				}
			}

			if (kesisenTarihteIzinVar) {
				durum = "";
			}
		}

		if (PdksUtil.hasStringValue(durum)) {
			List tarihAraligiIzinList = null;
			HashMap param = new HashMap();
			param.put("izinSahibi=", izinSahibi);
			param.put("izinTipi.bakiyeIzinTipi=", null);
			param.put("bitisZamani>=", personelIzin.getBaslangicZamani());
			param.put("baslangicZamani<=", personelIzin.getBitisZamani());
			param.put("izinDurumu not ", izinDurumlari);
			if (personelIzin.getId() != null)
				param.put("id<>", personelIzin.getId());
			if (session != null)
				param.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				tarihAraligiIzinList = pdksEntityController.getObjectByInnerObjectListInLogic(param, PersonelIzin.class);

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				tarihAraligiIzinList = null;
			}

			boolean kesisenTarihteIzinVar = Boolean.FALSE;
			// bu durumda vekil transfer gibi islem yapiacak
			if (tarihAraligiIzinList != null) {
				String basZamanStr = PdksUtil.convertToDateString(personelIzin.getBaslangicZamani(), "yyyyMMddHHmm");
				String bitZamanStr = PdksUtil.convertToDateString(personelIzin.getBitisZamani(), "yyyyMMddHHmm");
				for (Iterator iterator = tarihAraligiIzinList.iterator(); iterator.hasNext();) {
					PersonelIzin tempIzin = (PersonelIzin) iterator.next();
					if (PdksUtil.convertToDateString(tempIzin.getBaslangicZamani(), "yyyyMMddHHmm").equals(bitZamanStr) || PdksUtil.convertToDateString(tempIzin.getBitisZamani(), "yyyyMMddHHmm").equals(basZamanStr))
						continue;

					kesisenTarihteIzinVar = Boolean.TRUE;
					PdksUtil.addMessageWarn("Verilen tarih aralığında kesişen izin bulunmaktadır! " + tempIzin.getIzinTipi().getIzinTipiTanim().getAciklama() + "( " + authenticatedUser.dateTimeFormatla(tempIzin.getBaslangicZamani()) + " - "
							+ authenticatedUser.dateTimeFormatla(tempIzin.getBitisZamani()) + " )");
					break;
				}
			}

			if (kesisenTarihteIzinVar) {

				durum = "";
			}
		}
		// KONTROL 3 ardarda gelen yillik ve mazeret izni olamaz. Kontrol
		// edelim.

		if (izinTipi == null && guncellenecekIzin != null && guncellenecekIzin.getIzinTipi() != null)
			personelIzin.setIzinTipi(guncellenecekIzin.getIzinTipi());
		if (getHesapTipi() == PersonelIzin.HESAP_TIPI_GUN && PdksUtil.hasStringValue(durum)) {
			boolean ardisikIzinVar = ardisikIzinKontrol(getInstance());
			if (ardisikIzinVar)
				durum = "";
		}

		if (PdksUtil.hasStringValue(durum)) {

			Date basDate = PdksUtil.getDate(personelIzin.getBaslangicZamani()), bitDate = PdksUtil.getDate(personelIzin.getBitisZamani());
			if (izinTipi.isSenelikIzin()) {

				basDate = PdksUtil.tariheGunEkleCikar(basDate, -6);

				bitDate = PdksUtil.tariheGunEkleCikar(bitDate, 6);
			}
			List<Personel> perList = new ArrayList<Personel>();
			perList.add(personelIzin.getIzinSahibi());
			TreeMap resmiTatilGunleri = ortakIslemler.getTatilGunleri(perList, basDate, bitDate, session);
			if (izinTipi.isSenelikIzin()) {

				try {
					izinSuresiGun = izinSaatSuresiHesapla(vardiyaMap, personelIzin, izinTipi.getHesapTipi(), resmiTatilGunleri);
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");

					logger.error("Pdks hata out : " + e.getMessage());
					PdksUtil.addMessageWarn(e.getMessage());
					durum = "";
				}
				if (izinSuresiGun > izinTipi.getMaxGun()) {
					PdksUtil.addMessageWarn("Bir defada girilebilecek izin süresinden fazladır.");
					durum = "";
				} else
					personelIzin.setIzinSuresi(izinSuresiGun);

				// burada detay hesaplayan kisim vardi.
			} else if (izinTipi.isMazeretIzin()) {
				// mazeret kontrol bulunulan yıl dışında mazeret giremesin.
				cal = Calendar.getInstance();
				cal.setTime(new Date());
				int bulunulanYil = cal.get(Calendar.YEAR);
				if (bulunulanYil != yil && !authenticatedUser.isIK()) {
					PdksUtil.addMessageWarn("Mazeret izni bulunulan yıl için girilebilir.");
					durum = "";
				} else {

					try {
						izinSuresiSaat = izinSaatSuresiHesapla(vardiyaMap, personelIzin, izinTipi.getHesapTipi(), resmiTatilGunleri);
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());

					}
					personelIzin.setIzinSuresi(izinSuresiSaat);

				}

			} else {
				// yillik ve mazeret disindakiler icin izintipine gore izin
				// suresi hesaplayalim
				// girilen izin max saat max gun degerlerini asmis mi?
				if (izinTipi.getSaatGosterilecek() && hesapTipi == PersonelIzin.HESAP_TIPI_SAAT) {
					try {
						izinSuresiSaat = izinSaatSuresiHesapla(vardiyaMap, personelIzin, izinTipi.getHesapTipi(), resmiTatilGunleri);
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());

					}
					personelIzin.setIzinSuresi(izinSuresiSaat);
				} else if (!izinTipi.getTakvimGunumu()) {
					try {
						izinSuresiGun = izinSaatSuresiHesapla(vardiyaMap, personelIzin, izinTipi.getHesapTipi(), resmiTatilGunleri);
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());
						PdksUtil.addMessageWarn(e.getMessage());
						durum = "";

					}
					personelIzin.setIzinSuresi(izinSuresiGun);
				} else {
					izinSuresiGun = PdksUtil.tarihFarki(PdksUtil.getDate(personelIzin.getBaslangicZamani()), PdksUtil.getDate(personelIzin.getBitisZamani()));

					personelIzin.setIzinSuresi(izinSuresiGun);

				}
			}
		}
		double izinSuresiGunSaat = personelIzin.getIzinSuresi();
		if (PdksUtil.hasStringValue(durum) && izinTipi.isBireyselMolaIzin()) {
			String basTarihi = PdksUtil.convertToDateString(personelIzin.getBaslangicZamani(), "yyyyMMdd");
			String bitTarihi = PdksUtil.convertToDateString(personelIzin.getBitisZamani(), "yyyyMMdd");
			HashMap param = new HashMap();
			param.put("izinSahibi=", izinSahibi);
			param.put("izinTipi.id=", izinTipi.getId());
			param.put("bitisZamani>=", PdksUtil.tariheGunEkleCikar(personelIzin.getBaslangicZamani(), -1));
			param.put("baslangicZamani<=", PdksUtil.tariheGunEkleCikar(personelIzin.getBitisZamani(), 1));
			param.put("izinDurumu not ", izinDurumlari);
			if (personelIzin.getId() != null)
				param.put("id<>", personelIzin.getId());
			TreeMap<String, Double> gunlukToplam = new TreeMap<String, Double>();
			if (basTarihi.equals(bitTarihi))
				gunlukToplam.put(basTarihi, izinSuresiGunSaat);
			if (session != null)
				param.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelIzin> bireyselMolalar = pdksEntityController.getObjectByInnerObjectListInLogic(param, PersonelIzin.class);
			for (Iterator iterator = bireyselMolalar.iterator(); iterator.hasNext();) {
				PersonelIzin personelIzin2 = (PersonelIzin) iterator.next();
				basTarihi = PdksUtil.convertToDateString(personelIzin2.getBaslangicZamani(), "yyyyMMdd");
				bitTarihi = PdksUtil.convertToDateString(personelIzin2.getBitisZamani(), "yyyyMMdd");

				if (basTarihi.equals(bitTarihi)) {
					double toplamSure = (gunlukToplam.containsKey(basTarihi) ? gunlukToplam.get(basTarihi) : 0d) + personelIzin2.getIzinSuresi();
					gunlukToplam.put(basTarihi, toplamSure);
				}

			}
			if (!gunlukToplam.isEmpty()) {
				for (Iterator iterator = gunlukToplam.keySet().iterator(); iterator.hasNext();) {
					String dateStr = (String) iterator.next();
					double toplamSure = gunlukToplam.get(dateStr);
					if (toplamSure > izinTipi.getMaxSaat()) {
						durum = "";
						PdksUtil.addMessageWarn(PdksUtil.convertToDateString(PdksUtil.convertToJavaDate(dateStr, "yyyyMMdd"), PdksUtil.getDateFormat()) + " günü izin toplamı " + izinTipi.getMaxSaat() + " saati geçti!");
						break;
					}
				}
			}

		}
		// 21-12-2009 fazla mesai izni girilirken fazla mesaiyi parametre
		// tablosunda gecerli oldugu sure
		// ile toplayip gecerliligine bakilmalidir

		// KONTROL istedigi kadar izni kalmis mi , bakiye kontrolu yapilmasi
		// gereken izinler icin bu kontrol yapilir.
		if (PdksUtil.hasStringValue(durum) && !izinTipi.getBakiyeDevirTipi().equals(IzinTipi.BAKIYE_DEVIR_YOK)) {
			// dusulecek izin gunu once gecmis yillardan izin varsa eritilir

			List<Integer> izinDurumList = new ArrayList<Integer>();
			// izinDurumList.add(PersonelIzin.IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA);
			izinDurumList.add(PersonelIzin.IZIN_DURUMU_REDEDILDI);
			izinDurumList.add(PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL);
			HashMap map = new HashMap();
			map.put("izinSahibi.id =", izinSahibi.getId());
			map.put("izinTipi.bakiyeIzinTipi.id =", izinTipi.getId());
			cal.set(yil, 0, 1);
			Date baslangicZamani = PdksUtil.getDate((Date) cal.getTime().clone());
			boolean ilkKayit = false;
			if (izinTipi.getBakiyeDevirTipi().equals(IzinTipi.BAKIYE_DEVIR_SENELIK)) {
				ilkKayit = true;
				if (!izinTipi.isSuaIzin() || !authenticatedUser.isIK())
					map.put("baslangicZamani=", baslangicZamani);
			}
			map.put("izinTipi.izinTipiTanim.tipi=", Tanim.TIPI_BAKIYE_IZIN_TIPI);
			map.put("izinDurumu not ", izinDurumList);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelIzin> bakiyeIzinList = pdksEntityController.getObjectByInnerObjectListInLogic(map, PersonelIzin.class);
			if (bakiyeIzinList.size() > 1)
				bakiyeIzinList = PdksUtil.sortListByAlanAdi(bakiyeIzinList, "izinSuresi", true);
			TreeMap bakiyeYillikIzinMap = new TreeMap();
			for (PersonelIzin personelIzinBakiye : bakiyeIzinList) {
				if (personelIzinBakiye.getIzinSuresi() > 0.0d || !bakiyeYillikIzinMap.containsKey(personelIzinBakiye.getBaslangicZamani()))
					bakiyeYillikIzinMap.put(personelIzinBakiye.getBaslangicZamani(), personelIzinBakiye);
			}
			// map.put(AbhEntityController.MAP_KEY_MAP, "getBaslangicZamani");
			// TreeMap bakiyeYillikIzinMap = pdksEntityController.getObjectByInnerObjectMapInLogic(map, PersonelIzin.class, Boolean.FALSE);
			if (bakiyeYillikIzinMap.isEmpty() && izinTipi.getBakiyeDevirTipi().equals(IzinTipi.BAKIYE_DEVIR_SENELIK)) {
				cal = Calendar.getInstance();
				int buYil = PdksUtil.getDateField(new Date(), Calendar.YEAR);
				int bakiyeYil = PdksUtil.getDateField(baslangicZamani, Calendar.YEAR);
				if (bakiyeYil == buYil + 1) {

					parametreMap.clear();
					parametreMap.put("bakiyeIzinTipi.id", izinTipi.getId());
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					IzinTipi bakiyeIzinTip = (IzinTipi) pdksEntityController.getObjectByInnerObject(parametreMap, IzinTipi.class);
					if (bakiyeIzinTip != null) {
						cal.set(bakiyeYil, 0, 1);
						Date izinBaslangicZamani = PdksUtil.getDate(cal.getTime());
						HashMap<Integer, Integer> kidemHesabiMap = ortakIslemler.getKidemHesabi(izinBaslangicZamani, izinSahibi, null, null, authenticatedUser, session, null, Boolean.TRUE, Boolean.FALSE);
						int izinKidemYil = kidemHesabiMap.containsKey(Calendar.YEAR) ? kidemHesabiMap.get(Calendar.YEAR) : 0;
						User sistemAdminUser = ortakIslemler.getSistemAdminUser(session);
						ortakIslemler.getBakiyeIzin(sistemAdminUser, izinSahibi, izinBaslangicZamani, bakiyeIzinTip, null, izinKidemYil, session);
						map.put(PdksEntityController.MAP_KEY_MAP, "getBaslangicZamani");
						if (session != null)
							map.put(PdksEntityController.MAP_KEY_SESSION, session);
						bakiyeYillikIzinMap = pdksEntityController.getObjectByInnerObjectMapInLogic(map, PersonelIzin.class, Boolean.FALSE);
					}
					//
				}
			}
			Date bakiyeYil = null;
			if ((izinTipi.isSenelikIzin() || izinTipi.isSuaIzin()))
				bakiyeYil = bakiyeYilBul(izinSahibi);
			double toplamBakiye = 0d;
			List<PersonelIzin> bakiyeList = new ArrayList<PersonelIzin>(bakiyeYillikIzinMap.values());
			for (Iterator iterator = bakiyeList.iterator(); iterator.hasNext();) {
				PersonelIzin tempBakiye = (PersonelIzin) iterator.next();
				if (personelIzin.getId() != null)
					tempBakiye.setKontrolIzin(personelIzin);
				double kalanIzin = tempBakiye.getKalanIzin();
				if (bakiyeYil != null && bakiyeYil.getTime() < tempBakiye.getBaslangicZamani().getTime())
					continue;
				toplamBakiye += kalanIzin;

			}

			if (izinSuresiGunSaat > toplamBakiye) {
				PdksUtil.addMessageWarn("Mevcut bakiye izniniz yeterli değildir.");
				durum = "";
			} else {
				if (personelIzin.getId() != null) {
					if (personelIzin.getPersonelIzinler() != null) {
						for (PersonelIzinDetay personelIzinDetay1 : personelIzin.getPersonelIzinler()) {

							izinDetay = personelIzinDetay1;
							break;
						}
					}

				}

				HashMap<Integer, Integer> kidemHesabiMap = ortakIslemler.getKidemHesabi(PdksUtil.getDate(personelIzin.getBaslangicZamani()), izinSahibi, null, null, authenticatedUser, session, null, Boolean.TRUE, Boolean.FALSE);

				int izinKidemYil = kidemHesabiMap.containsKey(Calendar.YEAR) ? kidemHesabiMap.get(Calendar.YEAR) : 0;

				if (!ilkKayit)
					ilkKayit = (izinKidemYil <= 0);
				if (bakiyeList.size() > 1)
					bakiyeList = PdksUtil.sortListByAlanAdi(bakiyeList, "bitisZamani", izinKidemYil > 0);
				int bitisYil = PdksUtil.getDateField(personelIzin.getBitisZamani(), Calendar.YEAR);
				PersonelIzin hakEdisIzin = izinTipi.isSenelikIzin() || izinTipi.isSuaIzin() ? getHakEdisIzin(personelIzin) : null;
				for (Iterator iterator = bakiyeList.iterator(); iterator.hasNext();) {
					PersonelIzin tempBakiye = (PersonelIzin) iterator.next();
					int tempBakiyeYil = PdksUtil.getDateField(tempBakiye.getBitisZamani(), Calendar.YEAR);

					if (tempBakiye.getIzinSuresi() > 0.0d && tempBakiye.getIzinKagidiGeldi() == null) {
						if (ilkKayit || PdksUtil.tarihKarsilastirNumeric(personelIzin.getBaslangicZamani(), tempBakiye.getBitisZamani()) != -1
								|| (tempBakiyeYil == bitisYil && (bakiyeList.size() == 1 || PdksUtil.tarihKarsilastirNumeric(personelIzin.getBitisZamani(), tempBakiye.getBitisZamani()) != -1))) {
							if (izinDetay == null) {
								izinDetay = new PersonelIzinDetay();
								izinDetay.setPersonelIzin(personelIzin);
							}
							izinDetay.setHakEdisIzin(hakEdisIzin != null ? hakEdisIzin : tempBakiye);
							izinDetay.setIzinMiktari(izinSuresiGunSaat);
							izinDetayList.add(izinDetay);
							izinSuresiGunSaat = 0;
							break;
						}
					}
					iterator.remove();
				}

				if (izinDetay == null) {
					durum = "";
					PdksUtil.addMessageWarn(izinTipi.getIzinTipiTanim().getAciklama() + " için bakiye oluşturulacak yıl bulunamadı! ");
				}

			}

		}
		boolean resmiTatilIzni = false;
		// resmiTatilIzni = izinTipi.isResmiTatilIzin();
		if (resmiTatilIzni) {
			Boolean tatil = Boolean.FALSE;
			TreeMap<String, Tatil> tatilMap = ortakIslemler.getTatilGunleri(null, personelIzin.getBaslangicZamani(), personelIzin.getBitisZamani(), session);
			if (!tatilMap.isEmpty()) {
				tatil = Boolean.TRUE;
				Calendar cal1 = Calendar.getInstance();
				cal1.setTime(personelIzin.getBitisZamani());
				cal1.add(Calendar.DATE, -1);
				Date bitTarih = (Date) cal1.getTime().clone();
				cal1.setTime(personelIzin.getBaslangicZamani());
				Date basTarih = (Date) cal1.getTime().clone();
				if (basTarih.after(bitTarih))
					bitTarih = (Date) cal1.getTime().clone();
				while (basTarih.getTime() <= bitTarih.getTime()) {
					String key = PdksUtil.convertToDateString(basTarih, "yyyyMMdd");
					if (tatilMap.containsKey(key)) {
						Tatil pdksTatil = tatilMap.get(key);
						Date bitisGun = (Date) pdksTatil.getBitGun();
						if (pdksTatil.getBasTarih().getTime() <= basTarih.getTime() && bitisGun.getTime() > basTarih.getTime()) {
							tatil = Boolean.TRUE;
							cal1.add(Calendar.DATE, 1);
							basTarih = (Date) cal1.getTime().clone();
						} else {
							tatil = Boolean.FALSE;
							break;
						}

					} else {
						tatil = Boolean.FALSE;
						break;
					}

				}

			}

			if (!tatil) {
				durum = "";
				PdksUtil.addMessageWarn("İzin resmi tatil içinde değildir! ");

			}

		}
		if (PdksUtil.hasStringValue(durum)) {
			Integer izinHesapTipi = izinTipi.getHesapTipi();
			String mesaj = "Bir defada girilecek " + izinTipi.getIzinTipiTanim().getAciklama() + " süresi ";
			Double izinSure = personelIzin.getIzinSuresi();
			if ((izinHesapTipi != null && izinHesapTipi == PersonelIzin.HESAP_TIPI_SAAT) || (izinHesapTipi == null && (hesapTipi == PersonelIzin.HESAP_TIPI_SAAT || hesapTipi == PersonelIzin.HESAP_TIPI_GUN_SAAT_SECILDI))) {
				if (izinSure < izinTipi.getMinSaat()) {
					PdksUtil.addMessageWarn(mesaj + izinTipi.getMinSaat() + " saat'ten küçük olamaz!");
					durum = "";
				} else if (izinSure > izinTipi.getMaxSaat()) {
					PdksUtil.addMessageWarn(mesaj + izinTipi.getMaxSaat() + " saat'ten büyük olamaz!");
					durum = "";
				}
			} else {
				if (izinSure < izinTipi.getMinGun()) {
					PdksUtil.addMessageWarn(mesaj + izinTipi.getMinGun() + " gün'den küçük olamaz!");
					durum = "";
				} else if (izinSure > izinTipi.getMaxGun()) {
					PdksUtil.addMessageWarn(mesaj + izinTipi.getMaxGun() + " gün'den büyük olamaz!");
					durum = "";
				}

			}
			boolean isGenelMudur = Boolean.FALSE;
			boolean isProjeMuduru = ortakIslemler.isProjeMuduru(null, personelIzin.getIzinSahibi(), session);
			if (!isProjeMuduru)
				isGenelMudur = ortakIslemler.isGenelMudur(null, personelIzin.getIzinSahibi(), session);
			if (!(isProjeMuduru || isGenelMudur)) {
				if (personelIzin.getId() == null) {
					Personel yonetici = personelIzin.getIzinSahibi().getPdksYonetici();
					User ilkYoneticiUser = null;
					if (yonetici != null) {
						parametreMap.clear();
						parametreMap.put("pdksPersonel.id", yonetici.getId());
						parametreMap.put("durum", Boolean.TRUE);
						if (session != null)
							parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						ilkYoneticiUser = (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);
					}
					String onaylayanTipi = izinTipi.getOnaylayanTipi();
					if (yonetici == null && (onaylayanTipi.equals(IzinTipi.ONAYLAYAN_TIPI_YONETICI1) || onaylayanTipi.equals(IzinTipi.ONAYLAYAN_TIPI_YONETICI2))) {
						PdksUtil.addMessageWarn(ortakIslemler.yoneticiAciklama() + " tanımsızdır.");
						durum = "";
					} else if (ilkYoneticiUser == null && (onaylayanTipi.equals(IzinTipi.ONAYLAYAN_TIPI_YONETICI1) || onaylayanTipi.equals(IzinTipi.ONAYLAYAN_TIPI_YONETICI2))) {
						PdksUtil.addMessageWarn(yonetici.getAdSoyad() + " " + ortakIslemler.yoneticiAciklama() + " kullanıcısı tanımsızdır.");
						durum = "";
					} else if ((onaylayanTipi.equals(IzinTipi.ONAYLAYAN_TIPI_YONETICI1) || onaylayanTipi.equals(IzinTipi.ONAYLAYAN_TIPI_YONETICI2)) && !yonetici.isCalisiyor()) {
						PdksUtil.addMessageWarn("Aktif yönetici bulunmaktadır. ");
						durum = "";
					}

				}
				if (izinTipi.isOnaysiz() == false) {
					if (!(izinTipi.getPersonelGirisTipi().equals(IzinTipi.GIRIS_TIPI_IK)) && personelIzin.getIzinSahibi().getPdksYonetici() == null) {
						durum = "";
						PdksUtil.addMessageWarn(personelIzin.getIzinSahibi().getAdSoyad() + " ait yönetici alanı tanımsızdır!");
					}
				}
			}
			if (PdksUtil.hasStringValue(durum))
				durum = izniSistemKaydet(personelIzin, updateUser, izinDetayList);

		}
		setBasarili(PdksUtil.hasStringValue(durum));

		return durum;
	}

	private void getIstirahatIzni(Personel izinSahibi) {
		HashMap hashMap = new HashMap();
		hashMap.put("departman.id=", izinSahibi.getSirket().getDepartman().getId());
		hashMap.put("izinTipiTanim.kodu like", "%I%");
		// hashMap.put("izinTipiTanim.kodu=", IzinTipi.SSK_ISTIRAHAT);
		hashMap.put("personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
		hashMap.put("durum=", Boolean.TRUE);
		if (session != null)
			hashMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		seciliIzinTipi = (IzinTipi) pdksEntityController.getObjectByInnerObjectInLogic(hashMap, IzinTipi.class);
	}

	private Date bakiyeYilBul(Personel izinSahibi) {
		Date bakiyeYil = null;
		Calendar cal = Calendar.getInstance();
		int sene = cal.get(Calendar.YEAR);
		if (!authenticatedUser.isAdmin() && !authenticatedUser.isIK()) {

			Date bugun = PdksUtil.getDate(Calendar.getInstance().getTime());
			cal.setTime((Date) izinSahibi.getIzinHakEdisTarihi().clone());
			cal.set(Calendar.YEAR, sene);
			Date izinHakEdisTarihi = cal.getTime();
			if (PdksUtil.tarihKarsilastirNumeric(izinHakEdisTarihi, bugun) == 1)
				--sene;
			cal.set(sene, 0, 1);
			bakiyeYil = PdksUtil.getDate(cal.getTime());
		}
		return bakiyeYil;
	}

	/**
	 * @param personelIzin
	 * @param updateUser
	 * @param izinDetayList
	 * @return
	 */
	private String izniSistemKaydet(PersonelIzin personelIzin, User updateUser, List<PersonelIzinDetay> izinDetayList) {
		String durum = "persist";
		List<PersonelIzinDetay> list = null;

		boolean isYeni = personelIzin.getId() == null;
		IzinTipi izinTipi = personelIzin.getIzinTipi();
		if (!isYeni) {
			// izin detayları burada silelim, kontroller saglikli olabilsin diye
			HashMap parametreMap = new HashMap();
			parametreMap.put("personelIzin.id", personelIzin.getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			list = pdksEntityController.getObjectByInnerObjectList(parametreMap, PersonelIzinDetay.class);
			personelIzin.setGuncelleyenUser(updateUser);
		} else {
			list = new ArrayList<PersonelIzinDetay>();
			personelIzin.setOlusturanUser(updateUser);
		}

		try {
			// izini giren kisi IK personel ise , izintipindeki onay sirasinin
			// yada kisinin onemi yoktur.
			// Onaylanmis gibi kaydedilir, mail gitmez

			boolean isGenelMudur = Boolean.FALSE;
			boolean isProjeMuduru = ortakIslemler.isProjeMuduru(null, personelIzin.getIzinSahibi(), session);
			if (!isProjeMuduru)
				isGenelMudur = ortakIslemler.isGenelMudur(null, personelIzin.getIzinSahibi(), session);
			if (isGenelMudur || isProjeMuduru)
				personelIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_IK_ONAYINDA);

			Integer izinHesapTipi = new Integer(hesapTipi);
			if (izinTipi.getTakvimGunumu())
				izinHesapTipi = PersonelIzin.HESAP_TIPI_GUN;
			else if (!izinTipi.getOnaylayanTipi().equals(IzinTipi.ONAYLAYAN_TIPI_YOK) && personelIzin.getIzinDurumu() == PersonelIzin.IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA && izinTipi.getHesapTipi() != null && seciliHesapTipi != izinTipi.getHesapTipi())
				izinHesapTipi = new Integer(seciliHesapTipi + 2);
			if (izinTipi.getHesapTipi() != null)
				izinHesapTipi = izinTipi.getHesapTipi();
			personelIzin.setHesapTipi(izinHesapTipi);
			if (izinTipi.isResmiTatilIzin())
				personelIzin.setIzinSuresi(0D);

			pdksEntityController.saveOrUpdate(session, entityManager, personelIzin);
			for (Iterator iterator = izinDetayList.iterator(); iterator.hasNext();) {
				PersonelIzinDetay izinDetaytoSave = (PersonelIzinDetay) iterator.next();
				for (Iterator iterator2 = list.iterator(); iterator2.hasNext();) {
					PersonelIzinDetay personelIzinDetay = (PersonelIzinDetay) iterator2.next();
					if (personelIzinDetay.getHakEdisIzin().getId().equals(izinDetaytoSave.getHakEdisIzin().getId())) {
						izinDetaytoSave.setId(personelIzinDetay.getId());
						iterator2.remove();
						break;
					}

				}
				izinDetaytoSave.setPersonelIzin(personelIzin);

				pdksEntityController.saveOrUpdate(session, entityManager, izinDetaytoSave);
			}

			if (izinTipi.isSSKIstirahat()) {
				istirahat.setAciklama(!istirahat.isHastane() ? istirahat.getVerenKurum() : istirahat.getVerenHekimAdi());
				if (personelIzin.getId() != null) {
					if (istirahat.getId() == null) {
						HashMap parametreMap = new HashMap();
						parametreMap.put("personelIzin.id", personelIzin.getId());
						if (session != null)
							parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						IzinIstirahat istirahatIzin = (IzinIstirahat) pdksEntityController.getObjectByInnerObject(parametreMap, IzinIstirahat.class);
						if (istirahatIzin != null) {
							istirahatIzin.setAciklama(istirahat.getAciklama());
							istirahatIzin.setRaporKaynagi(istirahat.getRaporKaynagi());
							istirahatIzin.setTeshis(istirahat.getTeshis());
							istirahat = istirahatIzin;
						}
					}
				}

				if (istirahat.getId() == null) {
					istirahat.setOlusturanUser(updateUser);
					istirahat.setOlusturmaTarihi(new Date());
					istirahat.setPersonelIzin(personelIzin);
				} else {
					istirahat.setGuncelleyenUser(updateUser);
					istirahat.setGuncellemeTarihi(new Date());

				}

				pdksEntityController.saveOrUpdate(session, entityManager, istirahat);

			} else if (istirahat.getId() != null) {

				pdksEntityController.deleteObject(session, entityManager, istirahat);

			}
			boolean dosyaYazildi = Boolean.FALSE;

			if (izinTipi.getDosyaEkle() != null && izinTipi.getDosyaEkle()) {
				Dosya dosya = izinDosya.getDosya();
				if (dosya != null) {
					dosya.setAciklama(izinTipi.getIzinTipiTanim().getAciklama() + " dosyası");
					if (dosya.getDosyaIcerik() != null) {

						pdksEntityController.saveOrUpdate(session, entityManager, dosya);
						if (izinDosya.getId() == null) {
							izinDosya.setDosya(dosya);
							izinDosya.setPersonelIzin(personelIzin);
							pdksEntityController.saveOrUpdate(session, entityManager, izinDosya);
						}
						dosyaYazildi = Boolean.TRUE;
					}
				}

			}
			if (!dosyaYazildi && izinDosya.getId() != null) {

				pdksEntityController.deleteObject(session, entityManager, izinDosya);

			}

			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				PersonelIzinDetay personelIzinDetay = (PersonelIzinDetay) iterator.next();

				pdksEntityController.deleteObject(session, entityManager, personelIzinDetay);

			}
			if (isYeni || (personelIzin.getOnaylayanlar() == null || personelIzin.getOnaylayanlar().isEmpty()))
				durum = onayMesajiGonder(personelIzin, isGenelMudur, isProjeMuduru, session);

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			if (authenticatedUser.isAdmin())
				PdksUtil.addMessageError("İzin hata : " + e.getMessage());
			else
				PdksUtil.addMessageError("İzin kaydetme hatası ");
			durum = "";
		}
		basarili = PdksUtil.hasStringValue(durum);
		if (basarili) {
			try {
				session.flush();
				PdksUtil.addMessageAvailableInfo("İzin başarı ile kaydedilmiştir.");
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());

			}

			IzinTipi tipi = isGirisSSK() ? personelIzin.getIzinTipi() : null;
			setGuncellenecekIzin(null);
			setInstance(null);

			baslangicDegerleri();
			getPersonel();

			PersonelIzin izin = new PersonelIzin();
			if (tipi != null) {
				setSeciliIzinTipi(tipi);
				izin.setIzinTipi(tipi);
				setIstirahat(new IzinIstirahat());
			}

			izinListele(Boolean.FALSE, null);
			izin.setIzinSuresi(0D);
			setInstance(izin);
			setIzinliSahibi(null);

		}
		return durum;
	}

	/**
	 * @param mailUserMap
	 * @param user
	 */
	private void mailUserSakla(TreeMap<String, User> mailUserMap, User user) {
		if (mailUserMap != null && user != null && user.getEmail() != null) {
			String adres = PdksUtil.getMailAdres(user.getEmail().trim());
			if (user.isDurum() && user.getPdksPersonel().isCalisiyor()) {
				if (!mailUserMap.containsKey(adres))
					mailUserMap.put(adres, user);
			}

		}
	}

	/**
	 * @param personelIzin
	 * @param isGenelMudur
	 * @param isProjeMuduru
	 * @param session
	 * @return
	 */
	private String onayMesajiGonder(PersonelIzin personelIzin, boolean isGenelMudur, boolean isProjeMuduru, Session session) {

		String durum = "persist";
		Personel izinSahibi = personelIzin.getIzinSahibi();
		List<User> toList = null;
		String mailAdres = "onayYoneticiIzinMail.xhtml";
		boolean onayliIzin = true;

		ccMailList = new ArrayList<String>();
		ArrayList<String> yoneticiMailList = new ArrayList<String>();
		bccMailList = new ArrayList<String>();
		boolean onayli = Boolean.TRUE;
		boolean flush = Boolean.FALSE;
		TreeMap<String, User> mailUserMap = new TreeMap<String, User>();
		String mailPersonelAciklama = getMailPersonelAciklama(izinSahibi);
		IzinTipi izinTipi = personelIzin.getIzinTipi();

		if (izinTipi.getOnaylayanTipi().equals(IzinTipi.ONAYLAYAN_TIPI_YOK)) {
			onayli = Boolean.FALSE;

			if (izinSahibi.getPdksYonetici() != null) {
				try {
					User yonetici = ortakIslemler.getYoneticiBul(izinSahibi, izinSahibi.getPdksYonetici(), session);
					if (yonetici != null) {
						mailUserSakla(mailUserMap, yonetici);
						yoneticiMailList.add(yonetici.getEmail());
					}

				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}

			}
			if (izinSahibi.getYonetici2() != null) {

				try {
					User yonetici = ortakIslemler.getYoneticiBul(izinSahibi, izinSahibi.getYonetici2(), session);
					if (yonetici != null) {
						mailUserSakla(mailUserMap, yonetici);
						session.refresh(yonetici);
						ortakIslemler.setUserRoller(yonetici, session);
						if (yonetici.isIkinciYoneticiIzinOnaylasin() && !yonetici.isGenelMudur())
							yoneticiMailList.add(yonetici.getEmail());
					}

				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}

			}
			if (!yoneticiMailList.isEmpty())
				mailListesineEkle(ccMailList, yoneticiMailList);
			HashMap map = new HashMap();
			map.put("durum", Boolean.TRUE);
			map.put("pdksPersonel.id", izinSahibi.getId());
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			toList = pdksEntityController.getObjectByInnerObjectList(map, User.class);

			for (User izinSahibiUser : toList) {
				mailUserSakla(mailUserMap, izinSahibiUser);
			}
			mailListesineEkle(ccMailList, izinSahibi.getEMailCCList());
			mailListesineEkle(bccMailList, izinSahibi.getEMailBCCList());
			List<User> ikUserList = ortakIslemler.IKKullanicilariBul(null, izinSahibi, session);

			if (!ikUserList.isEmpty()) {
				List<String> ikMail = new ArrayList<String>();
				for (User user : ikUserList) {
					mailUserSakla(mailUserMap, user);
					ikMail.add(user.getEmail());
				}

				mailListesineEkle(bccMailList, ikMail);
			}

			mailAdres = "onaysizIzinMail.xhtml";
			onayliIzin = false;
			personelIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_ONAYLANDI);

			if (izinTipi.isSSKIstirahat() && izinSahibi.getSirket().getDepartman().isAdminMi() && izinSahibi.getSirket().isErp()) {
				List<User> bbcUser = ortakIslemler.getGenelMudurBul(session);

				if (bbcUser != null) {
					bbcUser.clear(); // Genel Mudur mail engelleniyor
					for (User genelmudur : bbcUser)
						if (!ccMailList.contains(genelmudur.getEmail()) && !bccMailList.contains(genelmudur.getEmail())) {
							mailUserSakla(mailUserMap, genelmudur);
							bccMailList.add(genelmudur.getEmail());
						}

				}
			} else if (authenticatedUser.isAdmin() || authenticatedUser.isIK())
				if (ccMailList.isEmpty()) {
					User sistUser = ortakIslemler.getSistemAdminUser(session);
					if (sistUser == null)
						sistUser = authenticatedUser;
					ccMailList.add(sistUser.getEmail());
				}

		} else {

			if (!isGenelMudur && !isProjeMuduru)
				toList = ortakIslemler.izinOnayIslemleri(personelIzin, authenticatedUser, session);
			else if (isGenelMudur || isProjeMuduru || izinTipi.getOnaylayanTipi().equals(IzinTipi.ONAYLAYAN_TIPI_IK)) {
				PersonelIzinOnay yeniPersonelIzinOnay = new PersonelIzinOnay();
				yeniPersonelIzinOnay.setOnayDurum(PersonelIzinOnay.ONAY_DURUM_ISLEM_YAPILMADI);
				yeniPersonelIzinOnay.setDurum(Boolean.TRUE);
				yeniPersonelIzinOnay.setOlusturanUser(authenticatedUser);
				yeniPersonelIzinOnay.setOlusturmaTarihi(new Date());
				yeniPersonelIzinOnay.setOnaylayanTipi(PersonelIzinOnay.ONAYLAYAN_TIPI_IK);
				yeniPersonelIzinOnay.setPersonelIzin(personelIzin);
				pdksEntityController.saveOrUpdate(session, entityManager, yeniPersonelIzinOnay);
				flush = Boolean.TRUE;
				// entityManager.persist(yeniPersonelIzinOnay);
				toList = ortakIslemler.IKKullanicilariBul(null, personelIzin.getIzinSahibi(), null);

			}

		}

		try {
			if ((onayli == false && (bccMailList == null || bccMailList.isEmpty()) && (ccMailList == null || ccMailList.isEmpty()) && (toList == null || toList.isEmpty()))) {
				durum = "";
				throw new Exception("Personelin yöneticisi tanımlanmadığı için onay mekanizması çalıştırılamadı." + personelIzin.getIzinSahibi().getAdSoyad());

			} else if (personelIzin.getIzinDurumu() == PersonelIzin.IZIN_DURUMU_IK_ONAYINDA) {

				if (ccMailList == null)
					ccMailList = new ArrayList<String>();
				HashMap map = new HashMap();
				map.put("durum", Boolean.TRUE);
				map.put("pdksPersonel.id", personelIzin.getIzinSahibi().getId());
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				User izinSahibiUser = (User) pdksEntityController.getObjectByInnerObject(map, User.class);
				if (izinSahibiUser != null) {
					mailUserSakla(mailUserMap, izinSahibiUser);
					mailListesineEkle(ccMailList, izinSahibi.getEMailCCList());
					mailListesineEkle(bccMailList, izinSahibi.getEMailBCCList());
				}
				hekimIzinGenelMudurEkle(personelIzin);
			}
			setMailIzin(personelIzin);
			String aciklama = "";
			if (personelIzin.getIzinDurumu() == PersonelIzin.IZIN_DURUMU_IK_ONAYINDA) {
				aciklama = mailPersonelAciklama + " ait izin başlangıç tarihi " + authenticatedUser.dateTimeFormatla(mailIzin.getBaslangicZamani()) + " işe başlama tarihi " + authenticatedUser.dateTimeFormatla(mailIzin.getBitisZamani()) + " " + mailIzin.getIzinTipiAciklama()
						+ " yönetici tarafından onaylanmış olup " + mailIzin.getIzinSahibi().getSirket().getDepartman().getDepartmanTanim().getAciklama() + " onayınıza gönderilmiş bulunmaktadır.";
			} else if (personelIzin.getIzinDurumu() == PersonelIzin.IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA)
				aciklama = mailPersonelAciklama + " ait izni onayınıza gönderilmiş bulunmaktadır.";

			setToList(toList);
			setIzinMailAciklama(aciklama);
			ccIlebccMailKarsilastir();

			String render = "/email/" + mailAdres;

			if ((onayli && toList != null && !toList.isEmpty()) || (onayli == false && ccMailList != null && !ccMailList.isEmpty())) {
				if (onayli && personelIzin.getIzinDurumu() != PersonelIzin.IZIN_DURUMU_IK_ONAYINDA) {
					ccMailList.clear();
					bccMailList.clear();

				}
				adminBcc();
				if (izinTipi.getMailGonderimDurumu() != null)
					bilgiMailleriniEkle(personelIzin);

				if (flush)
					try {
						session.flush();
					} catch (Exception e) {

					}
				MailStatu mailSatu = null;
				try {
					String mailKonu = null, body = null;
					if (onayliIzin) {
						mailKonu = mailPersonelAciklama + " " + (mailIzin.getId() == null ? " İzin Onayı" : " İzin Kaydı");
						body = "<p>" + izinMailAciklama + "</p>	<p>Saygılarımla,</p>";
						body += "<a href=\"http://" + adres + "/onayimaGelenIzinler" + (mailIzin != null && mailIzin.getId() != null ? "?mId=" + mailIzin.getId() : "") + "\">" + ortakIslemler.getParameterKey("fromName") + " uygulamasına girmek	için buraya tıklayınız.</a>";

					} else {
						mailKonu = mailPersonelAciklama + " " + mailIzin.getIzinTipi().getIzinTipiTanim().getAciklama();
						body = "<p> " + mailPersonelAciklama + " ait izin başlangıç tarihi " + authenticatedUser.dateTimeFormatla(mailIzin.getBaslangicZamani()) + " işe başlama tarihi " + authenticatedUser.dateTimeFormatla(mailIzin.getBitisZamani()) + " " + mailIzin.getIzinTipiAciklama()
								+ " oluşturulmuştur.</p>";

					}
					MailObject mail = new MailObject();
					mail.setSubject(mailKonu);
					mail.setBody(body);
					if (!userList.isEmpty() && !userList.isEmpty())
						setToList(userList);
					ortakIslemler.addMailPersonelUserList(toList, mail.getToList());
					ortakIslemler.addMailPersonelList(ccMailList, mail.getCcList());
					List<MailPersonel> list = new ArrayList<MailPersonel>();
					if (!mail.getCcList().isEmpty()) {
						for (MailPersonel mp : mail.getCcList()) {
							String adresKey = mp.getEPosta();
							if (adresKey != null && mp.getAdiSoyadi() == null) {
								if (mailUserMap.containsKey(adresKey))
									mp.setAdiSoyadi(mailUserMap.get(adresKey).getAdSoyad());
								if (mp.getAdiSoyadi() == null)
									list.add(mp);
							}

						}
						if (mail.getToList().isEmpty()) {
							mail.getToList().addAll(mail.getCcList());
							mail.getCcList().clear();
						}
					}
					for (MailPersonel mp : mail.getBccList()) {
						String adresKey = mp.getEPosta();
						if (adresKey != null && mp.getAdiSoyadi() == null) {
							if (mailUserMap.containsKey(adresKey))
								mp.setAdiSoyadi(mailUserMap.get(adresKey).getAdSoyad());
							if (mp.getAdiSoyadi() == null)
								list.add(mp);
						}
					}
					if (!list.isEmpty()) {
						List<String> mailList = new ArrayList<String>();
						for (MailPersonel mp : list) {
							if (!mailList.contains(mp.getEPosta()))
								mailList.add(mp.getEPosta());

						}
						HashMap fields = new HashMap();
						fields.put(PdksEntityController.MAP_KEY_MAP, "getEmail");
						fields.put("email", mailList);
						if (session != null)
							fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						TreeMap<String, User> userMap = pdksEntityController.getObjectByInnerObjectMap(fields, User.class, false);
						if (!userMap.isEmpty()) {
							for (MailPersonel mp : list) {
								if (userMap.containsKey(mp.getEPosta()))
									mp.setAdiSoyadi(userMap.get(mp.getEPosta()).getAdSoyad());

							}
						}
						mailList = null;
						userMap = null;
					}
					list = null;
					ortakIslemler.addMailPersonelList(bccMailList, mail.getBccList());
					mailSatu = ortakIslemler.mailSoapServisGonder(false, mail, renderer, render, session);
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					PdksUtil.addMessageError(e.getMessage());
				}
				if (mailSatu != null && mailSatu.isDurum() == false) {
					if (mailSatu.getHataMesai() != null)
						PdksUtil.addMessageAvailableError(mailSatu.getHataMesai());

				}

			}

			User user = new User();
			user.setPdksPersonel(personelIzin.getIzinSahibi());
			setSeciliUser(user);

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			logger.error("onayMail.xhtml :" + e.getMessage());
			PdksUtil.addMessageError("onayMail.xhtml :" + e.getMessage());

		}
		return durum;
	}

	/**
	 * @param personelIzin
	 */
	private void bilgiMailleriniEkle(PersonelIzin personelIzin) {
		if (personelIzin.getIzinDurumu() != PersonelIzin.IZIN_DURUMU_REDEDILDI) {
			IzinTipi izinTipi = personelIzin.getIzinTipi();
			int izinDurumu = personelIzin.getIzinDurumu();
			boolean onaysiz = izinTipi.isOnaysiz() && izinTipi.isMailGonderimDurumOnaysiz();
			if ((onaysiz && izinDurumu == PersonelIzin.IZIN_DURUMU_ONAYLANDI) || (izinTipi.isMailGonderimDurumllkYonetici() && izinDurumu == PersonelIzin.IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA) || (izinTipi.isMailGonderimDurumIK() && izinDurumu == PersonelIzin.IZIN_DURUMU_IK_ONAYINDA)) {
				List<User> mailUserAll = new ArrayList<User>();
				if (ccList != null && !ccList.isEmpty())
					mailUserAll.addAll(ccList);
				if (bccList != null && !bccList.isEmpty())
					mailUserAll.addAll(bccList);

				List<String> mailList = new ArrayList<String>();
				if (ccMailList != null && !ccMailList.isEmpty())
					mailList.addAll(ccMailList);
				if (bccMailList != null && !bccMailList.isEmpty())
					mailList.addAll(ccMailList);
				if (!mailList.isEmpty()) {
					for (Iterator iterator = mailList.iterator(); iterator.hasNext();) {
						String string = (String) iterator.next();
						User user = new User();
						user.setEmail(string);
						user.setDurum(Boolean.TRUE);
						mailUserAll.add(user);
					}
				}
				mailList = null;
				TreeMap<String, User> userMap = new TreeMap<String, User>();
				for (User user : mailUserAll)
					userMap.put(user.getEmail(), user);
				mailUserAll = null;
				HashMap parametreMap = new HashMap();
				parametreMap.put("izinTipi.id", izinTipi.getId());
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<IzinTipiMailAdres> adresList = pdksEntityController.getObjectByInnerObjectList(parametreMap, IzinTipiMailAdres.class);
				if (!adresList.isEmpty())
					ortakIslemler.getAktifKullanicilar(adresList, null, session);
				for (IzinTipiMailAdres izinTipiMailAdres : adresList) {
					if (!userMap.containsKey(izinTipiMailAdres.getAdres())) {
						User user = new User();
						user.setEmail(izinTipiMailAdres.getAdres());
						user.setDurum(Boolean.TRUE);
						if (izinTipiMailAdres.getTipi().equals(IzinTipiMailAdres.TIPI_CC)) {
							if (ccList == null)
								ccList = new ArrayList<User>();
							ccList.add(user);
							if (ccMailList == null)
								ccMailList = new ArrayList<String>();
							ccMailList.add(izinTipiMailAdres.getAdres());

						} else if (izinTipiMailAdres.getTipi().equals(IzinTipiMailAdres.TIPI_BCC)) {

							if (bccList == null)
								bccList = new ArrayList<User>();
							bccList.add(user);

							if (bccMailList == null)
								bccMailList = new ArrayList<String>();
							bccMailList.add(izinTipiMailAdres.getAdres());

						}
					}
				}
				userMap = null;
				adresList = null;
			}
		}
	}

	private void hekimIzinGenelMudurEkle(PersonelIzin personelIzin) {
		try {
			if (!personelIzin.getIzinTipi().getOnaylayanTipi().equals(IzinTipi.ONAYLAYAN_TIPI_YOK) && personelIzin.getIzinSahibi().isHekim()) {
				List<User> bbcGenelMudurUser = ortakIslemler.getGenelMudurBul(session);
				if (bbcGenelMudurUser != null && !bbcGenelMudurUser.isEmpty()) {
					for (User userGM : bbcGenelMudurUser) {
						if (userGM.isDurum()) {
							String mailGM = userGM.getEmail();
							if (!ccMailList.contains(mailGM) && (bccMailList == null || !bccMailList.contains(mailGM)))
								ccMailList.add(mailGM);
						}

					}
				}
			}
		} catch (Exception em) {
		}
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void fillHekimIzinleri(boolean izinli) {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();

		List<PersonelIzin> izinList = null;
		Date tarih = Calendar.getInstance().getTime();
		List<Personel> personelList = getHekimler(tarih);
		if (!personelList.isEmpty()) {
			fillEkSahaTanim();
			List<Integer> izinDurum = new ArrayList<Integer>();
			izinDurum.add(PersonelIzin.IZIN_DURUMU_IK_ONAYINDA);
			izinDurum.add(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
			izinDurum.add(PersonelIzin.IZIN_DURUMU_SAP_GONDERILDI);
			HashMap paramMap = new HashMap();
			paramMap.put("izinTipi.bakiyeIzinTipi=", null);
			paramMap.put("izinTipi.onaylayanTipi<>", IzinTipi.ONAYLAYAN_TIPI_YOK);
			paramMap.put("izinSahibi", personelList);
			paramMap.put("baslangicZamani<=", tarih);
			paramMap.put("bitisZamani>=", tarih);
			paramMap.put("izinDurumu", izinDurum);
			if (session != null)
				paramMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			izinList = pdksEntityController.getObjectByInnerObjectListInLogic(paramMap, PersonelIzin.class);
			if (!izinList.isEmpty())
				izinList = PdksUtil.sortListByAlanAdi(izinList, "olusturmaTarihi", Boolean.TRUE);

		} else
			izinList = new ArrayList<PersonelIzin>();
		setPersonelIzinList(izinList);
		if (!izinli) {
			if (!izinList.isEmpty()) {
				for (Iterator iterator = personelList.iterator(); iterator.hasNext();) {
					Personel pdksPersonel = (Personel) iterator.next();
					izinli = Boolean.FALSE;
					for (Iterator iterator2 = izinList.iterator(); iterator2.hasNext();) {
						PersonelIzin personelIzin = (PersonelIzin) iterator2.next();
						if (personelIzin.getIzinSahibi().getId().equals(pdksPersonel.getId())) {
							izinli = Boolean.TRUE;
							iterator2.remove();
						}

					}
					if (izinli)
						iterator.remove();

				}
				if (personelList.size() > 1)
					personelList = PdksUtil.sortObjectStringAlanList(personelList, "getAdSoyad", null);
				setPersonelList(personelList);

			}
		}
	}

	private List<Personel> getHekimler(Date tarih) {
		Date bugun = PdksUtil.getDate(tarih);
		HashMap map = new HashMap();
		map.put("sskCikisTarihi>=", bugun);
		map.put("iseBaslamaTarihi<=", bugun);
		map.put("ekSaha2.kodu=", Personel.STATU_HEKIM);
		map.put("pdksSicilNo<>", "");
		map.put("durum=", Boolean.TRUE);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Personel> personelList = pdksEntityController.getObjectByInnerObjectListInLogic(map, Personel.class);
		return personelList;
	}

	private void adminBcc() {
		// List<User> adminUserList = ortakIslemler.getAdminBul();
		List<User> adminUserList = ortakIslemler.bccAdminAdres(session, null);
		if (!adminUserList.isEmpty()) {
			List<String> adminMailList = new ArrayList<String>();
			for (User user : adminUserList)
				adminMailList.add(user.getEmail().trim());
			mailListesineEkle(bccMailList, adminMailList);
		}
	}

	private void ccIlebccMailKarsilastir() {
		List<String> toMailList = new ArrayList<String>();
		if (toList != null && !toList.isEmpty()) {
			for (Iterator iterator = toList.iterator(); iterator.hasNext();) {
				User toUser = (User) iterator.next();
				if (!toMailList.contains(toUser.getEmail()))
					toMailList.add(toUser.getEmail());

			}

		}
		if (!ccMailList.isEmpty() && !bccMailList.isEmpty()) {
			for (Iterator iterator = bccMailList.iterator(); iterator.hasNext();) {
				String bcc = (String) iterator.next();
				for (Iterator iterator2 = ccMailList.iterator(); iterator2.hasNext();) {
					String cc = (String) iterator2.next();
					if (cc.equals(bcc) || toMailList.contains(cc)) {
						iterator2.remove();
						break;
					}

				}
				if (toMailList.contains(bcc))
					iterator.remove();
			}
		}
	}

	public void setIzinTipi(PersonelIzin perIzin, IzinTipi izinTipi) {
		if (izinTipi.getHesapTipi() != null)
			setHesapTipi(izinTipi.getHesapTipi());
		else if (izinTipi.getSaatGosterilecek())
			setHesapTipi(PersonelIzin.HESAP_TIPI_SAAT);
		else
			setHesapTipi(PersonelIzin.HESAP_TIPI_GUN);
		setSaatGosterilecek(getHesapTipi() == PersonelIzin.HESAP_TIPI_SAAT);
		setSaatGosterilecek2(getHesapTipi() == PersonelIzin.HESAP_TIPI_SAAT);
		izinTipiDegisti(izinTipi);

	}

	/**
	 * @param vardiyalar
	 * @param personelIzin
	 * @param hesapTipiMethod
	 * @param resmiTatilGunleri
	 * @return
	 * @throws Exception
	 */
	private double izinSaatSuresiHesapla(TreeMap<String, VardiyaGun> vardiyalar, PersonelIzin personelIzin, Integer hesapTipiMethod, TreeMap resmiTatilGunleri) throws Exception {

		if (hesapTipiMethod == null)
			hesapTipiMethod = getHesapTipi();

		// vardiyasini bulup, o gun için kaç saat çalışması gerektigini bulalim.
		// istedigi tarih icin vardiyesi tanimlanmamissa sablondaki degerlere
		// gore hesaplama yapilir.

		double izinSuresiSaatGun = 0;
		Double eklenecekGun = 1.0;
		String tatilGunuKey = "";
		Calendar basCal = Calendar.getInstance();
		basCal.setTime((Date) personelIzin.getBaslangicZamani().clone());
		boolean senelikIzin = personelIzin.getIzinTipi().isSenelikIzin();
		Boolean hekim = null;
		try {
			hekim = personelIzin != null && personelIzin.getIzinSahibi() != null ? personelIzin.getIzinSahibi().isHekim() : Boolean.FALSE;
		} catch (Exception e) {
			hekim = Boolean.FALSE;
		}
		double artikIzinGun = personelIzin.getIzinTipi().getArtikIzinGun() != null && !hekim ? personelIzin.getIzinTipi().getArtikIzinGun() : 0D;

		// iznin ilk haftasi- ilk gun icin vardiya var mi diye bakilir
		if (!personelIzin.getIzinTipi().getTakvimGunumu() || hesapTipiMethod == PersonelIzin.HESAP_TIPI_SAAT) {
			Date vardiyaDate = (Date) personelIzin.getBaslangicZamani().clone();
			VardiyaGun pdksVardiyaGun = new VardiyaGun(personelIzin.getIzinSahibi(), null, vardiyaDate);
			String vardiyaKey = pdksVardiyaGun.getVardiyaKeyStr();
			IzinTipi izinTipi = personelIzin.getIzinTipi();
			if (hesapTipiMethod == PersonelIzin.HESAP_TIPI_SAAT)
				vardiyaDate = oncekiVardiyami(vardiyalar, vardiyaDate, personelIzin, Boolean.TRUE);
			pdksVardiyaGun.setVardiyaDate(vardiyaDate);
			Calendar cal = Calendar.getInstance();
			Date izinBasTarih = (Date) personelIzin.getBaslangicZamani().clone();
			Date izinBitTarih = (Date) personelIzin.getBitisZamani().clone();
			boolean ilkGun = Boolean.FALSE;
			int durum = hesapTipiMethod == PersonelIzin.HESAP_TIPI_SAAT ? 0 : 1;
			List<YemekIzin> yemekGenelList = ortakIslemler.getYemekList(session);

			int hafta = 0;
			boolean artikIizinVar = senelikIzin && artikIzinGun != 0D;
			TreeMap<Integer, List<Double>> artiklarMap = artikIizinVar ? new TreeMap<Integer, List<Double>>() : null;
			String bayramArtikIzinSifirla = resmiTatilGunleri != null && !resmiTatilGunleri.isEmpty() ? ortakIslemler.getParameterKey("bayramArtikIzinSifirla") : "";
			Date tarih = null;
			StringBuffer sb = new StringBuffer();
			Double yemekMolasiYuzdesi = ortakIslemler.getYemekMolasiYuzdesi(null, session);
			boolean cumaBasla = false;
			if (izinTipi.isCumaCumartesiTekIzinSaysin() && izinTipi.isOffDahilMi())
				cumaBasla = PdksUtil.getDateField(izinBasTarih, Calendar.DAY_OF_WEEK) == Calendar.FRIDAY;
			int cumartesi = 0;
			ortakIslemler.setVardiyaYemekList(new ArrayList<VardiyaGun>(vardiyalar.values()), yemekGenelList);
			while (PdksUtil.tarihKarsilastirNumeric(personelIzin.getBitisZamani(), vardiyaDate) >= durum) {
				pdksVardiyaGun.setVardiyaDate(vardiyaDate);
				tarih = (Date) vardiyaDate.clone();
				int haftaGunu = PdksUtil.getDateField(vardiyaDate, Calendar.DAY_OF_WEEK);
				if (cumaBasla && haftaGunu == Calendar.SATURDAY)
					++cumartesi;
				tatilGunuKey = PdksUtil.convertToDateString(vardiyaDate, "yyyyMMdd");
				vardiyaKey = pdksVardiyaGun.getVardiyaKeyStr();

				// logger.error(vardiyaKey);
				cal.setTime(vardiyaDate);
				if (vardiyalar.containsKey(vardiyaKey)) {
					VardiyaGun tempVardiya = (VardiyaGun) vardiyalar.get(vardiyaKey);
					List<YemekIzin> pdksYemekList = tempVardiya.getYemekList();
					if (artikIzinGun > 0.0D && cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && tempVardiya != null && tempVardiya.getVardiya().getKisaAdi() != null && tempVardiya.getVardiya().getKisaAdi().equals("CMT")) {
						if (sb.length() > 0)
							sb.append(", ");
						sb.append(PdksUtil.convertToDateString(vardiyaDate, PdksUtil.getDateFormat()));
					}
					if (!(tempVardiya.getVardiya().isHaftaTatil() || tempVardiya.getVardiya().isRadyasyonIzni() || (tempVardiya.getVardiya().isOffGun() && izinTipi.isOffDahilMi() == Boolean.FALSE))) {
						if (tempVardiya.getIslemVardiya() == null)
							tempVardiya.setVardiyaZamani();
						if (ilkGun) {
							vardiyaDate = tempVardiya.getIslemVardiya().getVardiyaBasZaman();
							izinBasTarih = vardiyaDate;
						}

						eklenecekGun = 1.0d;
						Tatil tatil = null;
						if (senelikIzin && resmiTatilGunleri.containsKey(tatilGunuKey)) {
							tatil = (Tatil) resmiTatilGunleri.get(tatilGunuKey);
							if (tatil.isYarimGunMu() && !tempVardiya.getVardiya().isHaftaTatil()) {
								eklenecekGun = new Double("0.5").doubleValue();
								if (izinTipi != null && izinTipi.isSenelikIzin()) {
									String arifeSenelikIzinGun = ortakIslemler.getParameterKey("arifeSenelikIzinGun");
									try {
										eklenecekGun = null;
										if (PdksUtil.hasStringValue(arifeSenelikIzinGun))
											eklenecekGun = Double.parseDouble(arifeSenelikIzinGun);

									} catch (Exception e) {
										eklenecekGun = null;
									}
									if (eklenecekGun == null || eklenecekGun.doubleValue() < 0.0d)
										throw new Exception("Arife gününü içeren izin girilemez!");
								}

							} else
								eklenecekGun = 0.0d;
						}
						if (artiklarMap != null) {
							if (!artikIizinVar && bayramArtikIzinSifirla.equals("1"))
								artiklarMap.remove(hafta);

							if (hafta == 0 || haftaGunu == Calendar.MONDAY) {

								artikIizinVar = Boolean.TRUE;
								if (hafta == 0 && bayramArtikIzinSifirla.equals("1")) {

									while (artikIizinVar && PdksUtil.getDateField(tarih, Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
										String key = PdksUtil.convertToDateString(tarih, "yyyyMMdd");
										if (resmiTatilGunleri.containsKey(key)) {
											Tatil pdksTatil = (Tatil) resmiTatilGunleri.get(key);
											if (!pdksTatil.isYarimGunMu())
												artikIizinVar = Boolean.FALSE;
										}
										tarih = PdksUtil.tariheGunEkleCikar(tarih, -1);
									}
								}
								++hafta;
							}
							if (eklenecekGun > 0) {
								List<Double> artiklar = artiklarMap.containsKey(hafta) ? artiklarMap.get(hafta) : new ArrayList<Double>();
								if (artiklar.isEmpty())
									artiklarMap.put(hafta, artiklar);
								artiklar.add(artikIzinGun);
							} else
								artikIizinVar = Boolean.FALSE;
						}
						if (eklenecekGun > 0 && hesapTipiMethod == PersonelIzin.HESAP_TIPI_SAAT) {
							Date basTarih = tempVardiya.getIslemVardiya().getVardiyaBasZaman();
							Date bitTarih = tempVardiya.getIslemVardiya().getVardiyaBitZaman();
							if (izinBasTarih.getTime() > basTarih.getTime())
								basTarih = (Date) izinBasTarih.clone();
							else if (!ilkGun) {
								izinBasTarih = basTarih;
								personelIzin.setBaslangicZamani(basTarih);

							}
							if (izinBitTarih.getTime() < bitTarih.getTime())
								bitTarih = (Date) izinBitTarih.clone();
							if (eklenecekGun > 0) {
								if (tatil != null && tatil.isYarimGunMu() && bitTarih.getTime() > tatil.getBasTarih().getTime())
									bitTarih = tatil.getBasTarih();
								if (tempVardiya.getVardiya().isCalisma()) {
									double toplamSure = PdksUtil.getSaatFarki(bitTarih, basTarih).doubleValue();
									double sure = ortakIslemler.getSaatSure(basTarih, bitTarih, pdksYemekList, tempVardiya, session);
									double yemekSure = toplamSure - sure;
									double vardiyaYemekSure = tempVardiya.getVardiya().getYemekSuresi().doubleValue() / 60.0d;
									if (tempVardiya.getVardiya().getNetCalismaSuresi() * yemekMolasiYuzdesi < toplamSure && yemekSure < vardiyaYemekSure) {
										sure += yemekSure - vardiyaYemekSure;
									}
									izinSuresiSaatGun += sure;
									if (sure > 0)
										logger.debug(vardiyaKey + " " + sure + " " + izinSuresiSaatGun);

								}

							}

							// izinSuresiSaatGun = izinSaatHesabi(izinSuresi,
							// izinSuresiSaatGun, tempVardiya.getVardiya());
						} else if (hesapTipiMethod == PersonelIzin.HESAP_TIPI_GUN)
							izinSuresiSaatGun += eklenecekGun;
						if (!ilkGun)
							ilkGun = Boolean.TRUE;
					} else if (senelikIzin && !tempVardiya.getVardiya().isHaftaTatil() && resmiTatilGunleri.containsKey(tatilGunuKey)) {
						Tatil tatil = (Tatil) resmiTatilGunleri.get(tatilGunuKey);
						if (tatil.isYarimGunMu()) {
							eklenecekGun = new Double("0.5").doubleValue();
							if (izinTipi != null && izinTipi.isSenelikIzin())
								throw new Exception("Arife gününü içeren izin girilemez!");
						}
					}

				}
				cal.add(Calendar.DATE, 1);
				vardiyaDate = cal.getTime();
			}

			if (!authenticatedUser.isIKAdmin() && sb.length() > 0) {
				String str = sb.toString();
				sb = null;
				throw new Exception(str + (str.indexOf(",") > 0 ? " günlerinde " : " günü ") + " Cumartesi vardiyası tanımlıdır.");
			}
			sb = null;
			if (artiklarMap != null) {
				if (bayramArtikIzinSifirla.equals("1")) {
					while (artikIizinVar && PdksUtil.getDateField(tarih, Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
						String key = PdksUtil.convertToDateString(tarih, "yyyyMMdd");
						if (resmiTatilGunleri.containsKey(key)) {
							Tatil pdksTatil = (Tatil) resmiTatilGunleri.get(key);
							if (!pdksTatil.isYarimGunMu())
								artikIizinVar = Boolean.FALSE;
						}
						tarih = PdksUtil.tariheGunEkleCikar(tarih, 1);
					}
					if (!artikIizinVar)
						artiklarMap.remove(hafta);
				}

				for (Iterator iterator = artiklarMap.keySet().iterator(); iterator.hasNext();) {
					Object key = (Object) iterator.next();
					List<Double> artiklar = artiklarMap.get(key);
					for (Double artik : artiklar) {
						izinSuresiSaatGun += artik;
					}
				}
			}
			artiklarMap = null;
			if (cumartesi == 1 && izinSuresiSaatGun == 2) {
				izinSuresiSaatGun = 1;
				cal.setTime(personelIzin.getBaslangicZamani());
				cal.add(Calendar.DATE, 1);
				Date bitisZamani = cal.getTime();
				personelIzin.setBitisZamani(bitisZamani);
			}
		} else if (personelIzin.getIzinTipi().getTakvimGunumu()) {
			// 2 tarih arasindaki gun sayısı kadar izinden dusulur
			// takvim gunu secilen bir izni saatlik girilecek sekilde
			// yapmamlilar.

			izinSuresiSaatGun = PdksUtil.tarihFarki(personelIzin.getBaslangicZamani(), personelIzin.getBitisZamani());

		}
		if (hesapTipiMethod != null && hesapTipiMethod == PersonelIzin.HESAP_TIPI_SAAT)
			izinSuresiSaatGun = User.getYuvarla(izinSuresiSaatGun);
		logger.debug("Sonuç : " + izinSuresiSaatGun);
		return izinSuresiSaatGun;
	}

	/**
	 * @param vardiyalar
	 * @param vardiyaDate
	 * @param personelIzin
	 * @param duzelt
	 * @return
	 */
	private Date oncekiVardiyami(TreeMap<String, VardiyaGun> vardiyalar, Date vardiyaDate, PersonelIzin personelIzin, boolean duzelt) {
		VardiyaGun pdksVardiyaGun = new VardiyaGun(personelIzin.getIzinSahibi(), null, vardiyaDate);
		String vardiyaKey = pdksVardiyaGun.getVardiyaKeyStr();
		IzinTipi izinTipi = personelIzin.getIzinTipi();
		if (vardiyalar.containsKey(vardiyaKey)) {
			VardiyaGun tempVardiyaIlk = (VardiyaGun) vardiyalar.get(vardiyaKey);
			if (tempVardiyaIlk.getVardiya() != null && !(tempVardiyaIlk.getVardiya().isHaftaTatil() || tempVardiyaIlk.getVardiya().isRadyasyonIzni() || (tempVardiyaIlk.getVardiya().isOffGun() && izinTipi.isOffDahilMi() == Boolean.FALSE))) {
				if (tempVardiyaIlk.getIslemVardiya() == null)
					tempVardiyaIlk.setVardiyaZamani();
				if (tempVardiyaIlk.getIslemVardiya().getVardiyaBitZaman().getTime() > vardiyaDate.getTime()) {
					Date oncekiVardiyaDate = PdksUtil.tariheGunEkleCikar(vardiyaDate, -1);
					pdksVardiyaGun.setVardiyaDate(oncekiVardiyaDate);
					vardiyaKey = pdksVardiyaGun.getVardiyaKeyStr();
					if (vardiyalar.containsKey(vardiyaKey)) {
						VardiyaGun tempVardiyaOnceki = (VardiyaGun) vardiyalar.get(vardiyaKey);
						if (tempVardiyaOnceki.getVardiya() != null && !(tempVardiyaOnceki.getVardiya().isHaftaTatil() || tempVardiyaIlk.getVardiya().isRadyasyonIzni() || (tempVardiyaOnceki.getVardiya().isOffGun() && izinTipi.isOffDahilMi() == Boolean.FALSE))) {
							if (tempVardiyaOnceki.getIslemVardiya() == null)
								tempVardiyaOnceki.setVardiyaZamani();
							if (tempVardiyaOnceki.getIslemVardiya().getVardiyaBasZaman().getTime() <= vardiyaDate.getTime() && tempVardiyaOnceki.getIslemVardiya().getVardiyaBitZaman().getTime() >= vardiyaDate.getTime())
								if (duzelt)
									vardiyaDate = tempVardiyaOnceki.getVardiyaDate();

						}

					}
				}
			}
		}
		return vardiyaDate;
	}

	/**
	 * @param izinSahibiInput
	 * @return
	 */
	public Personel getPersonelVeri(Personel izinSahibiInput) {
		Personel izinSahibi = null;
		setIzinIptalGoster(Boolean.FALSE);
		if (izinSahibiInput.getSirket().getDepartman().getIzinGirilebilir()) {
			izinSahibi = izinSahibiInput;
			boolean senelikKullan = ortakIslemler.getParameterKey("suaSenelikKullan").equals("1");
			if (izinliSahibi == null || !izinSahibi.getId().equals(izinliSahibi.getId())) {
				PersonelIzinDosya personelIzinDosya = new PersonelIzinDosya();
				personelIzinDosya.setPersonelIzin(izin);
				setIzinDosya(personelIzinDosya);
				if (guncellenecekIzin == null)
					getInstance().setIzinSahibi(izinSahibi);
				try {
					HashMap<Integer, Integer> kidemHesabiMap = ortakIslemler.getKidemHesabi(null, izinSahibi, null, null, authenticatedUser, session, null, Boolean.TRUE, Boolean.FALSE);
					session.flush();
					setKidemYil(kidemHesabiMap.containsKey(Calendar.YEAR) ? kidemHesabiMap.get(Calendar.YEAR) : 0);
					setKidemAy(kidemHesabiMap.containsKey(Calendar.MONTH) ? kidemHesabiMap.get(Calendar.MONTH) : 0);
					setKidemGun(kidemHesabiMap.containsKey(Calendar.DATE) ? kidemHesabiMap.get(Calendar.DATE) : 0);

				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());
					PdksUtil.addMessageError(e.getMessage());
				}
				HashMap<Integer, Integer> yasMap = ortakIslemler.getTarihMap(izinSahibi != null ? izinSahibi.getDogumTarihi() : null, Calendar.getInstance().getTime());
				setYasYil(yasMap.containsKey(Calendar.YEAR) ? yasMap.get(Calendar.YEAR) : 0);
				setYasAy(yasMap.containsKey(Calendar.MONTH) ? yasMap.get(Calendar.MONTH) : 0);
				setYasGun(yasMap.containsKey(Calendar.DATE) ? yasMap.get(Calendar.DATE) : 0);
				// Sirket izinSahibiSirket = izinSahibi.getSirket();
				HashMap map = new HashMap();
				boolean stajer = false, senelikIzin = false;
				if (!stajer) {

					double senelikSuaIzin = 0;
					if (senelikKullan || !izinSahibi.isSuaOlur()) {
						IzinTipi izinTipi = izinSahibi != null && izinSahibi.getSirket() != null ? getBakiyeIzinTipi(izinSahibi.getSirket().getDepartman(), IzinTipi.YILLIK_UCRETLI_IZIN) : null;
						if (izinTipi != null) {

							Date bakiyeYil = bakiyeYilBul(izinSahibi);
							try {
								StringBuffer sb = new StringBuffer();
								sb.append("SELECT  I." + PersonelIzin.COLUMN_NAME_ID + " FROM " + PersonelIzin.TABLE_NAME + " I WITH(nolock) ");
								sb.append(" WHERE I." + PersonelIzin.COLUMN_NAME_PERSONEL + "=:izinSahibi AND I." + PersonelIzin.COLUMN_NAME_IZIN_TIPI + "=:izinTipi");
								map.put("izinTipi", izinTipi.getId());
								map.put("izinSahibi", izinSahibi.getId());
								if (session != null)
									map.put(PdksEntityController.MAP_KEY_SESSION, session);
								List<PersonelIzin> senelikIzinler = ortakIslemler.getDataByIdList(sb, map, PersonelIzin.TABLE_NAME, PersonelIzin.class);
								for (PersonelIzin izin : senelikIzinler) {
									PersonelIzin personelIzin = (PersonelIzin) izin.clone();
									if (bakiyeYil != null && personelIzin.getBaslangicZamani().after(bakiyeYil))
										continue;
									if (personelIzin.getKalanIzin() > 0)
										senelikSuaIzin += personelIzin.getKalanIzin();
								}

							} catch (Exception e) {
								logger.error("Pdks hata in : \n");
								e.printStackTrace();
								logger.error("Pdks hata out : " + e.getMessage());

							}
						}

					}
					izinSahibi.setSenelikIzin(senelikSuaIzin);

					map.clear();

					senelikIzin = (authenticatedUser.isAdmin() || authenticatedUser.isIK()) && izinSahibi.getIzinHakEdisTarihi() != null;
					if (!senelikIzin && izinSahibi.getIzinHakEdisTarihi() != null) {
						Date izinHakEttigiTarih = PdksUtil.addTarih(izinSahibi.getIzinHakEdisTarihi(), Calendar.YEAR, 1);
						senelikIzin = PdksUtil.tarihKarsilastirNumeric(new Date(), izinHakEttigiTarih) != -1;
					}

				}
				List<IzinTipi> list = null;
				if (!isGirisSSK()) {
					map.put("bakiyeIzinTipi=", null);
					map.put("durum=", Boolean.TRUE);
					map.put("personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
					if (stajer)
						map.put("stajerKullanilir=", Boolean.TRUE);
					map.put("departman.id=", izinSahibi.getSirket().getDepartman().getId());
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					list = pdksEntityController.getObjectByInnerObjectListInLogic(map, IzinTipi.class);
					if (getInstance().getId() == null)
						getInstance().setIzinTipi(null);
					Personel bagliPersonel = null;

					if (authenticatedUser.isAdmin() || authenticatedUser.isIK())
						bagliPersonel = authenticatedUser.getPdksPersonel();
					else {
						Date bugun = new Date();
						List<Personel> bagliPersoneller = ortakIslemler.yoneticiPersonelleri(authenticatedUser.getPdksPersonel().getId(), bugun, bugun, session);
						if (!bagliPersoneller.isEmpty())
							bagliPersonel = bagliPersoneller.get(0);
						bagliPersoneller = null;
					}
					if (!izinSahibi.getSirket().isPdksMi())
						list.clear();
					for (Iterator iterator = list.iterator(); iterator.hasNext();) {
						IzinTipi izinTipi = (IzinTipi) iterator.next();
						if (izinTipi.isFazlaMesai()) {
							if (!(izinSahibi.getFazlaMesaiIzinKullan() != null && izinSahibi.getFazlaMesaiIzinKullan()))
								iterator.remove();
							continue;
						} else if (izinTipi.isSenelikIzin()) {
							if ((izinSahibi.isSuaOlur() && !senelikKullan) || izinSahibi.getSenelikIzin() == 0 || !senelikIzin)
								iterator.remove();
							else
								getInstance().setIzinTipi(izinTipi);
						} else {
							if (izinTipi.isSutIzin() && !izinSahibi.isSutIzniKullan()) {
								iterator.remove();
								continue;
							} else if (izinTipi.isGebelikMuayeneIzin() && !izinSahibi.isGebelikMuayeneIzniKullan()) {
								iterator.remove();
								continue;
							} else if (!izinTipi.getPersonelGirisTipi().equals(IzinTipi.GIRIS_TIPI_IK) && izinTipi.getOnaylayanTipi().equals(IzinTipi.ONAYLAYAN_TIPI_YOK) && !(izinSahibi.getOnaysizIzinKullanilir() != null && izinSahibi.getOnaysizIzinKullanilir())) {
								iterator.remove();
								continue;
							} else if (izinTipi.getPersonelGirisTipi().equals(IzinTipi.GIRIS_TIPI_YONETICI1) && bagliPersonel == null) {
								iterator.remove();
								continue;
							} else if (izinTipi.getPersonelGirisTipi().equals(IzinTipi.GIRIS_TIPI_IK) && !authenticatedUser.isIK() && !authenticatedUser.isAdmin()) {
								iterator.remove();
								continue;
							} else if (izinTipi.isSenelikIzin()) {
								if ((izinSahibi.isSuaOlur() && !senelikKullan) || izinSahibi.getSenelikIzin() == 0 || !senelikIzin)
									iterator.remove();
								else
									getInstance().setIzinTipi(izinTipi);
							} else if (izinTipi.isFazlaMesai() && izinSahibi.getFazlaMesaiIzin() == 0) {
								iterator.remove();
								continue;

							} else if (izinTipi.getBakiyeDevirTipi().equals(IzinTipi.BAKIYE_DEVIR_SENELIK) && !izinSahibi.getIzinBakiyeMapKey(izinTipi.getIzinTipiTanim().getKodu())) {
								iterator.remove();
								continue;
							} else if (izinTipi.isSuaIzin()) {
								if (!izinSahibi.isSuaOlur() || !izinSahibi.getIzinBakiyeMapKey(izinTipi.getIzinTipiTanim().getKodu())) {
									iterator.remove();
								}
							} else if (izinTipi.isResmiTatilIzin()) {
								if (!izinSahibi.isHekim()) {
									iterator.remove();
								}
							}
						}
					}
					if (!list.isEmpty()) {
						if (list.size() > 1)
							list = PdksUtil.sortObjectStringAlanList(null, list, "getSira", null);
						else {
							seciliIzinTipi = list.get(0);
							setIzinTipi(getInstance(), seciliIzinTipi);
						}
					}

				}
				if (list == null)
					list = new ArrayList<IzinTipi>();
				setIzinTipiList(list);

			}
			if (seciliIzinTipi == null && isGirisSSK())
				getIstirahatIzni(izinSahibi);
			if (guncellenecekIzin != null)
				istirahatAta(guncellenecekIzin);
			else
				istirahatAta(getInstance());
		} else
			setIzinliSahibi(null);
		return izinSahibi;
	}

	private IzinTipi getBakiyeIzinTipi(Departman departman, String tipiTipiKodu) {
		HashMap map = new HashMap();
		map.put("bakiyeIzinTipi <>", null);
		map.put("bakiyeIzinTipi.izinTipiTanim.kodu=", tipiTipiKodu);
		map.put("bakiyeIzinTipi.departman=", departman);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		IzinTipi izinTipi = (IzinTipi) pdksEntityController.getObjectByInnerObjectInLogic(map, IzinTipi.class);
		return izinTipi;
	}

	public void fillIzinTipiList() {
		List<IzinTipi> izinList = new ArrayList<IzinTipi>();
		HashMap parametreMap = new HashMap();
		parametreMap.put("departman.id=", authenticatedUser.getDepartman().getId());
		parametreMap.put("personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
		parametreMap.put("durum=", Boolean.TRUE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		izinList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, IzinTipi.class);
		setIzinTipleri(izinList);
	}

	public String izinRedDoldur() {
		logger.info("Red Tipi" + redSebebi);

		return "";
	}

	public Personel getPersonel() {

		return izinliSahibi;
	}

	public void izinTipiDegisti(IzinTipi izinTipi) {
		if (izinTipi == null)
			izinTipi = getSeciliIzinTipi();
		else {
			if (izinTipi.getSaatGosterilecek() && izinTipi.getGunGosterilecek()) {
				setSaatGunOpsiyon(Boolean.TRUE);
			} else {
				setSaatGunOpsiyon(Boolean.FALSE);
				setSaatGosterilecek(izinTipi.getSaatGosterilecek() && !izinTipi.getGunGosterilecek());
			}

			if (izinTipi != null && (izinTipi.getIzinTipiTanim().getKodu().equals(IzinTipi.EGITIM_IC) || izinTipi.getIzinTipiTanim().getKodu().equals(IzinTipi.EGITIM_DIS)))
				setFazlaMesaiOpsiyon(Boolean.TRUE);
		}

	}

	public void gunSaatDegisti() {

		setSaatGosterilecek(getHesapTipi() == PersonelIzin.HESAP_TIPI_SAAT);

	}

	public void guncelle(PersonelIzin izin) {
		izinIptalGoster = izinDonemKontrol(izin);
		if (izinIptalGoster) {

			Calendar cal = Calendar.getInstance();
			cal.setTime(izin.getBaslangicZamani());
			setBaslangicSaat(cal.get(Calendar.HOUR_OF_DAY));
			setBaslangicDakika(cal.get(Calendar.MINUTE));
			cal.setTime(izin.getBitisZamani());
			setBitisSaat(cal.get(Calendar.HOUR_OF_DAY));
			setBitisDakika(cal.get(Calendar.MINUTE));
			hesapTipi = new Integer(izin.getHesapTipi() - (izin.getHesapTipi() > 2 ? 2 : 0));
			setGorevTipiList(ortakIslemler.getTanimList(Tanim.TIPI_IZIN_GOREV_TIPI, session));
			saatGosterilecek = getHesapTipi() == PersonelIzin.HESAP_TIPI_SAAT;
			Personel tempIzinSahibi = (Personel) izin.getIzinSahibi().clone();
			tempIzinSahibi = (Personel) entityManager.merge(tempIzinSahibi);
			this.setIzinliSahibi(null);

			tempIzinSahibi = getPersonelVeri(tempIzinSahibi);

			Map guncellecekIzinTipiMap = new HashMap();
			if (!isGirisSSK()) {

				for (Iterator iterator = izinTipiList.iterator(); iterator.hasNext();) {
					IzinTipi type = (IzinTipi) iterator.next();
					if (!guncellecekIzinTipiMap.containsKey(type.getId().toString()))
						guncellecekIzinTipiMap.put(type.getId().toString(), type);
				}
			} else {
				istirahatAta(izin);
			}
			izin.setIzinSahibi(tempIzinSahibi);

			// setInstance(null);
			this.setIzinliSahibi(tempIzinSahibi);
			izinTipiDegisti(izin.getIzinTipi());
			if (izin.getIzinTipi().isDosyaEklenir()) {
				HashMap parametreMap = new HashMap();
				parametreMap.put("personelIzin.id", izin.getId());
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				PersonelIzinDosya personelIzinDosya = (PersonelIzinDosya) pdksEntityController.getObjectByInnerObject(parametreMap, PersonelIzinDosya.class);
				if (personelIzinDosya != null) {
					// session.refresh(personelIzinDosya.getDosya());
					setIzinDosya(personelIzinDosya);

				}
			}
			if (!guncellecekIzinTipiMap.containsKey(izin.getIzinTipi().getId().toString()))
				guncellecekIzinTipiMap.put(izin.getIzinTipi().getId().toString(), izin.getIzinTipi());
			setGuncellemeIzinTipiList(new ArrayList<IzinTipi>(guncellecekIzinTipiMap.values()));
			HashMap parametreMap = new HashMap();
			parametreMap.put("id", izin.getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			izin = (PersonelIzin) pdksEntityController.getObjectByInnerObject(parametreMap, PersonelIzin.class);
			setGuncellenecekIzin(izin);
		}
	}

	private void istirahatAta(PersonelIzin izin) {
		HashMap parametreMap = new HashMap();
		parametreMap.put("personelIzin.id", izin.getId());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		IzinIstirahat izinIstirahat = izin.getId() != null ? (IzinIstirahat) pdksEntityController.getObjectByInnerObject(parametreMap, IzinIstirahat.class) : null;
		if (izinIstirahat == null) {
			izinIstirahat = new IzinIstirahat();
			izinIstirahat.setAciklama("");
			izinIstirahat.setTeshis("");
			izinIstirahat.setRaporKaynagi("");
			izinIstirahat.setPersonelIzin(izin);
		} else {
			izinIstirahat.aciklamaAta();
		}
		setIstirahat(izinIstirahat);
	}

	public void listenerIzinDosya(UploadEvent event) throws Exception {
		UploadItem item = event.getUploadItem();
		Dosya dosya = izinDosya.getDosya();
		izinDosya.setDosya(PdksUtil.getDosya(item, dosya));

	}

	public void izinDosyaSifirla() {
		Dosya dosya = izinDosya.getDosya();
		dosya.setDosyaIcerik(null);

	}

	public void onayAkisiGoster(PersonelIzin izin) {
		List<PersonelIzinOnay> onaylayanlar = ortakIslemler.izinOnaylarGetir(izin, session);
		if (onaylayanlar != null) {
			if (onaylayanlar.size() > 1)
				onaylayanlar = PdksUtil.sortListByAlanAdi(onaylayanlar, "id", Boolean.FALSE);
			String onaylayanTipi = "";
			boolean update = Boolean.FALSE;
			for (Iterator iterator = onaylayanlar.iterator(); iterator.hasNext();) {
				PersonelIzinOnay personelIzinOnay = (PersonelIzinOnay) iterator.next();
				if (!personelIzinOnay.getOnaylayanTipi().equals(onaylayanTipi))
					onaylayanTipi = personelIzinOnay.getOnaylayanTipi();
				else {
					update = Boolean.TRUE;
					iterator.remove();
				}
			}
			if (update) {
				izin.getOnaylayanlar().clear();
				if (!onaylayanlar.isEmpty())
					izin.getOnaylayanlar().addAll(onaylayanlar);
				izin = entityManager.merge(izin);
				entityManager.flush();
			}
		}
		setMailIzin(izin);
	}

	public List<String> getRoller() {
		return roller;
	}

	public void setRoller(List<String> roller) {
		this.roller = roller;
	}

	public int getKidemYil() {
		return kidemYil;
	}

	public void setKidemYil(int kidemYil) {
		this.kidemYil = kidemYil;
	}

	public int getKidemAy() {
		return kidemAy;
	}

	public void setKidemAy(int kidemAy) {
		this.kidemAy = kidemAy;
	}

	public int getKidemGun() {
		return kidemGun;
	}

	public void setKidemGun(int kidemGun) {
		this.kidemGun = kidemGun;
	}

	public List<IzinTipi> getIzinTipiList() {
		return izinTipiList;
	}

	public void setIzinTipiList(List<IzinTipi> izinTipiList) {
		this.izinTipiList = izinTipiList;
	}

	@Min(value = 0, message = "Saat 0'dan büyük veya eşit girebilirsiniz")
	@Max(value = 23, message = "Saati 23'den küçük veya eşit girebilirsiniz")
	public int getBaslangicSaat() {
		return baslangicSaat;
	}

	public void setBaslangicSaat(int baslangicSaat) {
		this.baslangicSaat = baslangicSaat;
	}

	@Min(value = 0, message = "Dakika 0'dan büyük veya eşit girebilirsiniz")
	@Max(value = 59, message = "Dakika 59'den küçük veya eşit girebilirsiniz")
	public int getBaslangicDakika() {
		return baslangicDakika;
	}

	public void setBaslangicDakika(int baslangicDakika) {
		this.baslangicDakika = baslangicDakika;
	}

	@Min(value = 0, message = "Saat 0'dan büyük veya eşit girebilirsiniz")
	@Max(value = 23, message = "Saati 23'den küçük veya eşit girebilirsiniz")
	public int getBitisSaat() {
		return bitisSaat;
	}

	public void setBitisSaat(int bitisSaat) {
		this.bitisSaat = bitisSaat;
	}

	@Min(value = 0, message = "Dakika 0'dan büyük veya eşit girebilirsiniz")
	@Max(value = 59, message = "Dakika 59'den küçük veya eşit girebilirsiniz")
	public int getBitisDakika() {
		return bitisDakika;
	}

	public void setBitisDakika(int bitisDakika) {
		this.bitisDakika = bitisDakika;
	}

	public boolean isSaatGosterilecek() {
		return saatGosterilecek;
	}

	public void setSaatGosterilecek(boolean saatGosterilecek) {
		this.saatGosterilecek = saatGosterilecek;
	}

	public boolean isSaatGunOpsiyon() {
		return saatGunOpsiyon;
	}

	public void setSaatGunOpsiyon(boolean saatGunOpsiyon) {
		this.saatGunOpsiyon = saatGunOpsiyon;
	}

	public User getSeciliUser() {
		return seciliUser;
	}

	public void setSeciliUser(User seciliUser) {
		this.seciliUser = seciliUser;
	}

	public String getRedSebebi() {
		return redSebebi;
	}

	public void setRedSebebi(String redSebebi) {
		this.redSebebi = redSebebi;
	}

	public List<PersonelIzinOnay> getOnayimaGelenIzinler() {
		return onayimaGelenIzinler;
	}

	public void setOnayimaGelenIzinler(List<PersonelIzinOnay> onayimaGelenIzinler) {
		this.onayimaGelenIzinler = onayimaGelenIzinler;
	}

	public PersonelIzinOnay getRedOnay() {
		return redOnay;
	}

	public void setRedOnay(PersonelIzinOnay redOnay) {
		this.redOnay = redOnay;
	}

	public Tanim getRedSebebiTanim() {
		return redSebebiTanim;
	}

	public void setRedSebebiTanim(Tanim redSebebiTanim) {
		this.redSebebiTanim = redSebebiTanim;
	}

	public List<Tanim> getRedSebebiList() {
		return redSebebiList;
	}

	public void setRedSebebiList(List<Tanim> redSebebiList) {
		this.redSebebiList = redSebebiList;
	}

	public List<PersonelIzin> getPersonelIzinList() {
		return personelIzinList;
	}

	public void setPersonelIzinList(List<PersonelIzin> personelIzinList) {
		this.personelIzinList = personelIzinList;
	}

	public String getCurrentFilterValue() {
		return currentFilterValue;
	}

	public void setCurrentFilterValue(String currentFilterValue) {
		this.currentFilterValue = currentFilterValue;
	}

	public int getYasYil() {
		return yasYil;
	}

	public void setYasYil(int yasYil) {
		this.yasYil = yasYil;
	}

	public int getYasAy() {
		return yasAy;
	}

	public void setYasAy(int yasAy) {
		this.yasAy = yasAy;
	}

	public int getYasGun() {
		return yasGun;
	}

	public void setYasGun(int yasGun) {
		this.yasGun = yasGun;
	}

	public boolean isSaatGosterilecek2() {
		return saatGosterilecek2;
	}

	public void setSaatGosterilecek2(boolean saatGosterilecek2) {
		this.saatGosterilecek2 = saatGosterilecek2;
	}

	public boolean isSaatGunOpsiyon2() {
		return saatGunOpsiyon2;
	}

	public void setSaatGunOpsiyon2(boolean saatGunOpsiyon2) {
		this.saatGunOpsiyon2 = saatGunOpsiyon2;
	}

	public boolean isFazlaMesaiOpsiyon() {
		return fazlaMesaiOpsiyon;
	}

	public void setFazlaMesaiOpsiyon(boolean fazlaMesaiOpsiyon) {
		this.fazlaMesaiOpsiyon = fazlaMesaiOpsiyon;
	}

	public boolean getMesaiCheck() {
		return mesaiCheck;
	}

	public void setMesaiCheck(boolean mesaiCheck) {
		this.mesaiCheck = mesaiCheck;
	}

	public PersonelIzin getIzin() {
		return izin;
	}

	public void setIzin(PersonelIzin izin) {
		this.izin = izin;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isGuncelle() {
		return guncelle;
	}

	public void setGuncelle(boolean guncelle) {
		this.guncelle = guncelle;
	}

	public Personel getIzinliSahibi() {
		return izinliSahibi;
	}

	public void setIzinliSahibi(Personel izinliSahibi) {
		this.izinliSahibi = izinliSahibi;
	}

	public String getOnayDurum() {
		return onayDurum;
	}

	public void setOnayDurum(String onayDurum) {
		this.onayDurum = onayDurum;
	}

	public List<PersonelIzinDetay> getPersonelIzinler() {
		return personelIzinler;
	}

	public void setPersonelIzinler(List<PersonelIzinDetay> personelIzinler) {
		this.personelIzinler = personelIzinler;
	}

	@Transient
	public boolean isIkMi() {
		return authenticatedUser.isIK();
	}

	public Date getBasDate() {
		return basDate;
	}

	public void setBasDate(Date basDate) {
		this.basDate = basDate;
	}

	public Date getBitDate() {
		return bitDate;
	}

	public void setBitDate(Date bitDate) {
		this.bitDate = bitDate;
	}

	public ArrayList<FileUpload> getFiles() {
		return files;
	}

	public void setFiles(ArrayList<FileUpload> files) {
		this.files = files;
	}

	public ArrayList<String> getSicilNoList() {
		return sicilNoList;
	}

	public void setSicilNoList(ArrayList<String> sicilNoList) {
		this.sicilNoList = sicilNoList;
	}

	public ArrayList<PersonelView> getExcelList() {
		return excelList;
	}

	public void setExcelList(ArrayList<PersonelView> excelList) {
		this.excelList = excelList;
	}

	public List<IzinTipi> getIzinTipleri() {
		return izinTipleri;
	}

	public void setIzinTipleri(List<IzinTipi> izinTipleri) {
		this.izinTipleri = izinTipleri;
	}

	public String getPersonelId() {
		return personelId;
	}

	public void setPersonelId(String personelId) {
		this.personelId = personelId;
	}

	public String getBastarih() {
		return bastarih;
	}

	public void setBastarih(String bastarih) {
		this.bastarih = bastarih;
	}

	public String getAdres() {
		return adres;
	}

	public void setAdres(String adres) {
		this.adres = adres;
	}

	public String getAd() {
		return ad;
	}

	public void setAd(String ad) {
		this.ad = ad;
	}

	public String getSoyad() {
		return soyad;
	}

	public void setSoyad(String soyad) {
		this.soyad = soyad;
	}

	public String getSicilNo() {
		return sicilNo;
	}

	public void setSicilNo(String sicilNo) {
		this.sicilNo = sicilNo;
	}

	public List<String> getIzinOnayMailList() {
		return izinOnayMailList;
	}

	public void setIzinOnayMailList(List<String> izinOnayMailList) {
		this.izinOnayMailList = izinOnayMailList;
	}

	public List<Tanim> getGorevTipiList() {
		return gorevTipiList;
	}

	public void setGorevTipiList(List<Tanim> gorevTipiList) {
		this.gorevTipiList = gorevTipiList;
	}

	public List<String> getCcMailList() {
		return ccMailList;
	}

	public void setCcMailList(List<String> ccMailList) {
		this.ccMailList = ccMailList;
	}

	public boolean isIzinIptalGoster() {
		return izinIptalGoster;
	}

	public void setIzinIptalGoster(boolean izinIptalGoster) {
		this.izinIptalGoster = izinIptalGoster;
	}

	public boolean isBasarili() {
		return basarili;
	}

	public void setBasarili(boolean basarili) {
		this.basarili = basarili;
	}

	public IzinIstirahat getIstirahat() {
		return istirahat;
	}

	public void setIstirahat(IzinIstirahat istirahat) {
		this.istirahat = istirahat;
	}

	public List<SelectItem> getIstirahatKaynakList() {
		return istirahatKaynakList;
	}

	public void setIstirahatKaynakList(List<SelectItem> istirahatKaynakList) {
		this.istirahatKaynakList = istirahatKaynakList;
	}

	public String getIzinMailAciklama() {
		return izinMailAciklama;
	}

	public void setIzinMailAciklama(String izinMailAciklama) {
		this.izinMailAciklama = izinMailAciklama;
	}

	public List<User> getCcList() {
		return ccList;
	}

	public void setCcList(List<User> ccList) {
		this.ccList = ccList;
	}

	public List<User> getBccList() {
		return bccList;
	}

	public void setBccList(List<User> bccList) {
		this.bccList = bccList;
	}

	public boolean isGirisSSK() {
		return girisSSK;
	}

	public void setGirisSSK(boolean girisSSK) {
		this.girisSSK = girisSSK;
	}

	public TreeMap<Long, IzinIstirahat> getIzinIstirahatMap() {
		return izinIstirahatMap;
	}

	public void setIzinIstirahatMap(TreeMap<Long, IzinIstirahat> izinIstirahatMap) {
		this.izinIstirahatMap = izinIstirahatMap;
	}

	public List<String> getBccMailList() {
		return bccMailList;
	}

	public void setBccMailList(List<String> bccMailList) {
		this.bccMailList = bccMailList;
	}

	public Tanim getEkSaha1() {
		return ekSaha1;
	}

	public void setEkSaha1(Tanim ekSaha1) {
		this.ekSaha1 = ekSaha1;
	}

	public Tanim getEkSaha2() {
		return ekSaha2;
	}

	public void setEkSaha2(Tanim ekSaha2) {
		this.ekSaha2 = ekSaha2;
	}

	public Tanim getEkSaha3() {
		return ekSaha3;
	}

	public void setEkSaha3(Tanim ekSaha3) {
		this.ekSaha3 = ekSaha3;
	}

	public Tanim getEkSaha4() {
		return ekSaha4;
	}

	public void setEkSaha4(Tanim ekSaha4) {
		this.ekSaha4 = ekSaha4;
	}

	public HashMap<String, List<Tanim>> getEkSahaListMap() {
		return ekSahaListMap;
	}

	public void setEkSahaListMap(HashMap<String, List<Tanim>> ekSahaListMap) {
		this.ekSahaListMap = ekSahaListMap;
	}

	public TreeMap<String, Tanim> getEkSahaTanimMap() {
		return ekSahaTanimMap;
	}

	public void setEkSahaTanimMap(TreeMap<String, Tanim> ekSahaTanimMap) {
		this.ekSahaTanimMap = ekSahaTanimMap;
	}

	public int getHesapTipi() {
		return hesapTipi;
	}

	public void setHesapTipi(int hesapTipi) {
		this.hesapTipi = hesapTipi;
	}

	public PersonelIzinDosya getIzinDosya() {
		return izinDosya;
	}

	public void setIzinDosya(PersonelIzinDosya izinDosya) {
		this.izinDosya = izinDosya;
	}

	public TreeMap<Long, PersonelIzinDosya> getIzinDosyaMap() {
		return izinDosyaMap;
	}

	public void setIzinDosyaMap(TreeMap<Long, PersonelIzinDosya> izinDosyaMap) {
		this.izinDosyaMap = izinDosyaMap;
	}

	public String getDosyaTipleri() {
		return dosyaTipleri;
	}

	public void setDosyaTipleri(String dosyaTipleri) {
		this.dosyaTipleri = dosyaTipleri;
	}

	public int getSeciliHesapTipi() {
		return seciliHesapTipi;
	}

	public void setSeciliHesapTipi(int seciliHesapTipi) {
		this.seciliHesapTipi = seciliHesapTipi;
	}

	public TreeMap<Long, User> getVekilYoneticiMap() {
		return vekilYoneticiMap;
	}

	public void setVekilYoneticiMap(TreeMap<Long, User> vekilYoneticiMap) {
		this.vekilYoneticiMap = vekilYoneticiMap;
	}

	public List<TempIzin> getTempIzinList() {
		return tempIzinList;
	}

	public void setTempIzinList(List<TempIzin> tempIzinList) {
		this.tempIzinList = tempIzinList;
	}

	public Boolean getBakiyeTipiSenelik() {
		return bakiyeTipiSenelik;
	}

	public void setBakiyeTipiSenelik(Boolean bakiyeTipiSenelik) {
		this.bakiyeTipiSenelik = bakiyeTipiSenelik;
	}

	public List<Personel> getPersonelList() {
		return personelList;
	}

	public void setPersonelList(List<Personel> personelList) {
		this.personelList = personelList;
	}

	public String getIzinId() {
		return izinId;
	}

	public void setIzinId(String izinId) {
		this.izinId = izinId;
	}

	public PersonelIzin getSessionIzin() {
		return sessionIzin;
	}

	public void setSessionIzin(PersonelIzin izin) {
		this.sessionIzin = izin;
	}

	public boolean isNedenSor() {
		return nedenSor;
	}

	public void setNedenSor(boolean nedenSor) {
		this.nedenSor = nedenSor;
	}

	public Date getSistemTarihi() {
		return sistemTarihi;
	}

	public void setSistemTarihi(Date sistemTarihi) {
		this.sistemTarihi = sistemTarihi;
	}

	public String getDonusAdres() {
		return donusAdres;
	}

	public void setDonusAdres(String donusAdres) {
		this.donusAdres = donusAdres;
	}

	public Tanim getSeciliDepartman() {
		return seciliDepartman;
	}

	public void setSeciliDepartman(Tanim seciliDepartman) {
		this.seciliDepartman = seciliDepartman;
	}

	public Sirket getSeciliSirket() {
		return seciliSirket;
	}

	public void setSeciliSirket(Sirket seciliSirket) {
		this.seciliSirket = seciliSirket;
	}

	public List<Tanim> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<Tanim> departmanList) {
		this.departmanList = departmanList;
	}

	public List<SelectItem> getSirketItemList() {
		return sirketItemList;
	}

	public void setSirketItemList(List<SelectItem> sirketItemList) {
		this.sirketItemList = sirketItemList;
	}

	public List<Sirket> getSirketList() {
		return sirketList;
	}

	public void setSirketList(List<Sirket> sirketList) {
		this.sirketList = sirketList;
	}

	public List<User> getUserList() {
		return userList;
	}

	public void setUserList(List<User> userList) {
		this.userList = userList;
	}

	public boolean isUserArama() {
		return userArama;
	}

	public void setUserArama(boolean userArama) {
		this.userArama = userArama;
	}

	public boolean isVisibled() {
		return visibled;
	}

	public void setVisibled(boolean visibled) {
		this.visibled = visibled;
	}

	public boolean isPersonelArama() {
		return personelArama;
	}

	public void setPersonelArama(boolean personelArama) {
		this.personelArama = personelArama;
	}

	public boolean isCheckBox() {
		return checkBox;
	}

	public void setCheckBox(boolean checkBox) {
		this.checkBox = checkBox;
	}

	public boolean isCheckBoxDurum() {
		return checkBoxDurum;
	}

	public void setCheckBoxDurum(boolean checkBoxDurum) {
		this.checkBoxDurum = checkBoxDurum;
	}

	public String getReRender() {
		return reRender;
	}

	public void setReRender(String reRender) {
		this.reRender = reRender;
	}

	public Sirket getSirket() {
		return pdksSirket;
	}

	public void setSirket(Sirket pdksSirket) {
		this.pdksSirket = pdksSirket;
	}

	public List<String> getRoleList() {
		return roleList;
	}

	public void setRoleList(List<String> roleList) {
		this.roleList = roleList;
	}

	public Personel getSeciliPersonel() {
		return seciliPersonel;
	}

	public void setSeciliPersonel(Personel seciliPersonel) {
		this.seciliPersonel = seciliPersonel;
	}

	public Personel getArananPersonel() {
		return arananPersonel;
	}

	public void setArananPersonel(Personel arananPersonel) {
		this.arananPersonel = arananPersonel;
	}

	public Personel getListelenPersonel() {
		return listelenPersonel;
	}

	public void setListelenPersonel(Personel listelenPersonel) {
		this.listelenPersonel = listelenPersonel;
	}

	public String getmId() {
		return mId;
	}

	public void setmId(String mId) {
		this.mId = mId;
	}

	public AramaSecenekleri getAramaSecenekleri() {
		return aramaSecenekleri;
	}

	public void setAramaSecenekleri(AramaSecenekleri aramaSecenekleri) {
		this.aramaSecenekleri = aramaSecenekleri;
	}

	public Sirket getPdksSirket() {
		return pdksSirket;
	}

	public void setPdksSirket(Sirket pdksSirket) {
		this.pdksSirket = pdksSirket;
	}

	public String getIzinIptal() {
		return izinIptal;
	}

	public void setIzinIptal(String izinIptal) {
		this.izinIptal = izinIptal;
	}

	public String getMailKonu() {
		return mailKonu;
	}

	public void setMailKonu(String mailKonu) {
		this.mailKonu = mailKonu;
	}

	public AramaSecenekleri getAramaListeSecenekleri() {
		return aramaListeSecenekleri;
	}

	public void setAramaListeSecenekleri(AramaSecenekleri aramaListeSecenekleri) {
		this.aramaListeSecenekleri = aramaListeSecenekleri;
	}

	public boolean isIzinERPGiris() {
		return izinERPGiris;
	}

	public void setIzinERPGiris(boolean izinERPGiris) {
		this.izinERPGiris = izinERPGiris;
	}

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}

	public boolean isServisAktarDurum() {
		return servisAktarDurum;
	}

	public void setServisAktarDurum(boolean servisAktarDurum) {
		this.servisAktarDurum = servisAktarDurum;
	}

	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public PersonelIzin getMailIzin() {
		return mailIzin;
	}

	public void setMailIzin(PersonelIzin mailIzin) {
		this.mailIzin = mailIzin;
	}

}
