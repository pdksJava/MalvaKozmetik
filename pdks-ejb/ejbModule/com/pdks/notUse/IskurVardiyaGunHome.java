package com.pdks.notUse;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.AylikPuantaj;
import org.pdks.entity.BolumKat;
import org.pdks.entity.CalismaModeli;
import org.pdks.entity.CalismaModeliAy;
import org.pdks.entity.CalismaModeliVardiya;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Departman;
import org.pdks.entity.DepartmanDenklestirmeDonemi;
import org.pdks.entity.Dosya;
import org.pdks.entity.FazlaMesaiTalep;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelDenklestirmeDinamikAlan;
import org.pdks.entity.PersonelDenklestirmeTasiyici;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGorev;
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.VardiyaHafta;
import org.pdks.entity.VardiyaPlan;
import org.pdks.entity.VardiyaSablonu;
import org.pdks.entity.YemekIzin;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;
import org.pdks.session.ExcelUtil;
import org.pdks.session.FazlaMesaiOrtakIslemler;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;
import org.pdks.session.PersonelIzinGirisiHome;
import org.primefaces.event.FileUploadEvent;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

@Name("iskurVardiyaGunHome")
public class IskurVardiyaGunHome extends EntityHome<VardiyaPlan> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8991939602320246125L;
	static Logger logger = Logger.getLogger(IskurVardiyaGunHome.class);
	@RequestParameter
	Long pdksVardiyaGunId;

	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;

	@In(required = false)
	FacesMessages facesMessages;
	@In(create = true, required = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	HashMap parameterMap;
	@In(required = true, create = true)
	Renderer renderer;
	@In(required = false, create = true)
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;
	@In(create = true, required = true)
	PersonelIzinGirisiHome personelIzinGirisiHome;
	@In(required = false, create = true)
	String linkAdres;

	private TreeMap<String, Tanim> fazlaMesaiMap;

	private Integer aksamVardiyaBasSaat, aksamVardiyaBitSaat, aksamVardiyaBasDakika, aksamVardiyaBitDakika;

	private List<VardiyaPlan> vardiyaPlanList = new ArrayList<VardiyaPlan>();

	private List<PersonelDenklestirmeDinamikAlan> personelDenklestirmeDinamikAlanList;

	private List<VardiyaGun> vardiyaGunList = new ArrayList<VardiyaGun>();

	private List<Vardiya> vardiyaList = new ArrayList<Vardiya>();

	private boolean fileImport = Boolean.FALSE, modelGoster = Boolean.FALSE, gebeGoster = Boolean.FALSE;

	private boolean adminRole, ikRole, gorevYeriGirisDurum, fazlaMesaiTarihGuncelle = Boolean.FALSE, offIzinGuncelle = Boolean.FALSE, gebeSutIzniGuncelle = Boolean.FALSE;

	private Dosya vardiyaPlanDosya = new Dosya();

	private HashMap<Double, ArrayList<Vardiya>> vardiyaMap = new HashMap<Double, ArrayList<Vardiya>>();

	private List<VardiyaSablonu> sablonList = new ArrayList<VardiyaSablonu>();

	private List<BolumKat> bolumKatlari;

	private TreeMap<String, Tatil> tatilGunleriMap, tatilMap;

	private List<CalismaModeliAy> modelList;

	private List<Vardiya> calismaOlmayanVardiyaList;

	private boolean vardiyaVar = Boolean.FALSE, seciliDurum, mailGonder, haftaTatilMesaiDurum = Boolean.FALSE, vardiyaGuncelle = Boolean.FALSE, hastaneSuperVisor = Boolean.FALSE;
	private boolean onayDurum = Boolean.FALSE, partTimeGoster = Boolean.FALSE, sutIzniGoster = Boolean.FALSE, planGirisi, sablonGuncelle, veriGuncellendi;

	private ArrayList<Date> islemGunleri;

	private Date basTarih, bitTarih;

	private String basTarihStr, bitTarihStr, sanalPersonelAciklama, bolumAciklama;
	private List<SelectItem> gunler;

	private VardiyaGun seciliVardiyaGun;

	private ByteArrayOutputStream baosDosya;

	private List<Long> gorevYerileri;

	private VardiyaHafta vardiyaHafta1, vardiyaHafta2;

	private String sicilNo = "", dosyaAdi, donusAdres = "";

	private Boolean denklestirmeHesapla = Boolean.FALSE, gunSec = Boolean.FALSE, gorevli = false, ozelIstek = Boolean.FALSE, islemYapiliyor = Boolean.FALSE, departmanBolumAyni = Boolean.FALSE;

	private Boolean resmiTatilVar = Boolean.FALSE, aksamGunVar = Boolean.FALSE, aksamSaatVar = Boolean.FALSE, haftaTatilVar = Boolean.FALSE;

	private Boolean denklestirmeAyDurum = Boolean.FALSE, fazlaMesaiTalepDurum = Boolean.FALSE;

	private DepartmanDenklestirmeDonemi denklestirmeDonemi;

	private Tanim gorevYeri;

	private TreeMap<String, Tanim> ekSahaTanimMap;

	private AylikPuantaj aylikPuantajDefault, personelAylikPuantaj;

	private List<AylikPuantaj> aylikPuantajList, aylikPuantajDosyaList;

	private DenklestirmeAy denklestirmeAy, denklestirmeOncekiAy, denklestirmeSonrakiAy;

	private List<SelectItem> ozelDurumList;

	private HashMap<String, List<Tanim>> ekSahaListMap;

	private Long gorevTipiId;

	private List<Tanim> gorevYeriTanimList;

	private HashMap<String, Personel> gorevliPersonelMap;

	private double haftaTatilSaat = 0d;

	private List<User> toList, ccList, bccList;

	private File ekliDosya;

	private String mailKonu, mailIcerik;

	private int ay, yil, maxYil, sonDonem;

	private List<SelectItem> aylar;

	private Date haftaTatili;

	private Boolean kaydet, degisti, kullaniciPersonel = false;

	private Departman departman;

	private Tanim planDepartman;

	private VardiyaPlan vardiyaPlan;

	private TreeMap<String, VardiyaGun> vardiyalarMap = new TreeMap<String, VardiyaGun>();
	private TreeMap<String, Vardiya> kayitliVardiyalarMap = new TreeMap<String, Vardiya>();

	private boolean stajerSirket;
	private boolean manuelVardiyaIzinGir = false;

	private byte[] mailData;
	private List<Vardiya> izinTipiVardiyaList;
	private TreeMap<String, TreeMap<String, List<VardiyaGun>>> izinTipiPersonelVardiyaMap;
	private AramaSecenekleri aramaSecenekleri = null;
	private Session session;

	@Override
	public Object getId() {
		if (pdksVardiyaGunId == null) {
			return super.getId();
		} else {
			return pdksVardiyaGunId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	/**
	 * @param user
	 */
	private void adminRoleDurum(User user) {
		if (user == null)
			user = authenticatedUser;
		adminRole = user != null && (user.isAdmin() || user.isSistemYoneticisi() || user.isIKAdmin());
		ikRole = user != null && (user.isAdmin() || user.isSistemYoneticisi() || user.isIK());
		fazlaMesaiTalepDurum = Boolean.FALSE;
		if (aylikPuantajList != null) {
			aylikPuantajList.clear();
		} else
			aylikPuantajList = new ArrayList<AylikPuantaj>();

		gorevYeriGirisDurum = false;
	}

	/**
	 * @return
	 */
	public String kayitGuncelle() {
		setVardiyaVar(Boolean.TRUE);
		return "";
	}

	/**
	 * @param event
	 * @throws Exception
	 */
	public void listener(UploadEvent event) throws Exception {
		if (event != null && event.getUploadItem() != null) {
			UploadItem uploadItem = event.getUploadItem();
			vardiyaPlanDosya.setDosyaAdi(uploadItem.getFileName());
			vardiyaPlanDosya.setDosyaIcerik(uploadItem.getData());
		}

	}

	/**
	 * 
	 */
	public void onUpload() {
		vardiyaPlanDosya.setDosyaIcerik(null);
	}

	/**
	 * @param event
	 */
	public void handleFileUpload(FileUploadEvent event) {
		org.primefaces.model.UploadedFile fileUploaded = event.getFile();
		if (fileUploaded != null) {
			vardiyaPlanDosya.setDosyaAdi(fileUploaded.getFileName());
			vardiyaPlanDosya.setDosyaIcerik(fileUploaded.getContents());
		}

	}

	/**
	 * @return
	 */
	public String modelDegisti() {
		// personelAylikPuantaj
		ArrayList<Vardiya> vardiyalar = fillAylikVardiyaList(personelAylikPuantaj, null);

		for (VardiyaGun pdksVardiyaGun : personelAylikPuantaj.getVardiyalar()) {
			if (pdksVardiyaGun.getVardiya() != null) {
				pdksVardiyaGun.setVardiyalar(pdksVardiyaGun.getIzin() == null ? vardiyalar : null);
				pdksVardiyaGun.setIslemVardiya(null);
				pdksVardiyaGun.setIslendi(false);
			}
		}

		return "";
	}

	/**
	 * @param tarih
	 * @return
	 */
	public String bitTarihGuncelle(Date tarih) {
		Calendar cal = Calendar.getInstance();
		if (Calendar.MONDAY == PdksUtil.getDateField(tarih, Calendar.DAY_OF_WEEK))
			setBitTarih(ortakIslemler.tariheGunEkleCikar(cal, tarih, 13));
		else
			setBitTarih(null);
		return "";
	}

	/**
	 * 
	 */
	public void vardiyaSablonuEkle() {
		this.clearInstance();
		setVardiyaList(new ArrayList<Vardiya>());
		setVardiyaVar(Boolean.FALSE);
	}

	/**
	 * @param personel
	 * @param tarih
	 * @param vardiya
	 * @return
	 */
	public VardiyaGun getVardiyaGun(Personel personel, Date tarih, Vardiya vardiya) {
		VardiyaGun pdksVardiyaGun = new VardiyaGun(personel, vardiya, tarih);
		return pdksVardiyaGun;
	}

	/**
	 * @param seciliVardiyaGun
	 * @return
	 */
	public String vardiyaDegistir(VardiyaGun seciliVardiyaGun) {
		if (personelAylikPuantaj != null)
			personelAylikPuantaj.setVardiyaDegisti(Boolean.TRUE);
		vardiyalarMap.put(seciliVardiyaGun.getVardiyaDateStr(), seciliVardiyaGun);
		seciliVardiyaGun.setIslemVardiya(null);
		seciliVardiyaGun.setGuncellendi(Boolean.TRUE);
		seciliVardiyaGun.setHareketHatali(Boolean.FALSE);
		seciliVardiyaGun.setBasSaat(null);
		seciliVardiyaGun.setBasDakika(null);
		seciliVardiyaGun.setBitSaat(null);
		seciliVardiyaGun.setBitDakika(null);
		return "";
	}

	/**
	 * @param aylikPuantaj
	 * @param pdksVardiyaGun
	 * @return
	 */
	public String gorevYeriDegistir(AylikPuantaj aylikPuantaj, VardiyaGun pdksVardiyaGun) {
		personelAylikPuantaj = aylikPuantaj;
		seciliVardiyaGun = pdksVardiyaGun;

		return "";
	}

	/**
	 * @param pdksVardiyaGun
	 * @return
	 */
	public String gorevYeriDegistir(VardiyaGun pdksVardiyaGun) {
		seciliVardiyaGun = pdksVardiyaGun;

		return "";
	}

	/**
	 * @param vardiyaHafta
	 */
	public void sablonDegistir(VardiyaHafta vardiyaHafta) {

	}

	/**
	 * @param durum
	 * @param list
	 * @return
	 */
	public boolean veriGoster(String durum, List list) {
		boolean goster = false;

		if (durum != null && !durum.equals("")) {
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
				if (durum.equals("suaDurum")) {
					if (aylikPuantaj.getPersonelDenklestirmeAylik() != null && aylikPuantaj.getPersonelDenklestirmeAylik().getSuaDurum() != null) {
						goster = true;
						break;
					}

				} else if (durum.equals("aylikFazlaMesai")) {
					if (aylikPuantaj.getAylikNetFazlaMesai() != 0d) {
						goster = true;
						break;
					}

				} else if (durum.equals("gecenAyFazlaMesai")) {
					if (aylikPuantaj.getGecenAyFazlaMesai().doubleValue() != 0d) {
						goster = true;
						break;
					}
				} else if (durum.equals("gecenDonem")) {
					if (aylikPuantaj.getPersonelDenklestirmeGecenAylik() != null && aylikPuantaj.getPersonelDenklestirmeGecenAylik().getId() != null && aylikPuantaj.getPersonelDenklestirmeGecenAylik().getHesaplananSure() != 0) {
						goster = true;
						break;
					}
				} else if (durum.equals("fiiliDonem")) {
					if (aylikPuantaj.getPersonelDenklestirmeAylik() != null && aylikPuantaj.getPersonelDenklestirmeAylik().getId() != null && aylikPuantaj.getPersonelDenklestirmeAylik().getHesaplananSure() != 0) {
						goster = true;
						break;
					}
				}

			}
		}
		return goster;
	}

	/**
	 * @param vardiyaGun
	 * @param vardiyalar
	 * @param tatilVardiyalar
	 * @return
	 */
	public VardiyaGun vardiyaListeDoldur(VardiyaGun vardiyaGun, ArrayList<Vardiya> vardiyalar, ArrayList<Vardiya> tatilVardiyalar) {
		if (vardiyaGun.getVardiya() != null) {
			if (vardiyaGun.getTatil() == null || vardiyaGun.getTatil().isYarimGunMu())
				vardiyaGun.setVardiyalar(vardiyalar);
			else
				vardiyaGun.setVardiyalar(tatilVardiyalar);
			try {
				Departman departman = vardiyaGun.getPersonel().getSirket().getDepartman();
				if (departman != null) {
					ArrayList<Vardiya> perVardiyalar = vardiyaGun.getVardiyalar();
					for (Iterator iterator = perVardiyalar.iterator(); iterator.hasNext();) {
						Vardiya pdksVardiya = (Vardiya) iterator.next();
						if (!pdksVardiya.getDurum() || (pdksVardiya.getDepartman() != null && !pdksVardiya.getDepartman().getId().equals(departman.getId())))
							iterator.remove();

					}
				}

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				logger.error("vardiyaListeDoldur : " + e.getMessage());
			}

		}
		return vardiyaGun;
	}

	/**
	 * @param plan
	 * @param hafta
	 */
	private void haftaListesiOlustur(VardiyaPlan plan, VardiyaHafta hafta) {
		ArrayList<Vardiya> tatilVardiyaList = fillVardiyaDoldurList(hafta.getVardiyaSablonu(), plan.getPersonel());
		ArrayList<Vardiya> vardiyaList = new ArrayList<Vardiya>();
		for (Vardiya vardiya : tatilVardiyaList)

			vardiyaList.add(vardiya);
		hafta.setVardiyalar(vardiyaList);
		hafta.getVardiyaGunler().clear();
		hafta.getVardiyaGunler().add(vardiyaListeDoldur(hafta.getVardiyaGun1(), vardiyaList, tatilVardiyaList));
		hafta.getVardiyaGunler().add(vardiyaListeDoldur(hafta.getVardiyaGun2(), vardiyaList, tatilVardiyaList));
		hafta.getVardiyaGunler().add(vardiyaListeDoldur(hafta.getVardiyaGun3(), vardiyaList, tatilVardiyaList));
		hafta.getVardiyaGunler().add(vardiyaListeDoldur(hafta.getVardiyaGun4(), vardiyaList, tatilVardiyaList));
		hafta.getVardiyaGunler().add(vardiyaListeDoldur(hafta.getVardiyaGun5(), vardiyaList, tatilVardiyaList));
		hafta.getVardiyaGunler().add(vardiyaListeDoldur(hafta.getVardiyaGun6(), vardiyaList, tatilVardiyaList));
		hafta.getVardiyaGunler().add(vardiyaListeDoldur(hafta.getVardiyaGun7(), vardiyaList, tatilVardiyaList));
	}

	/**
	 * @param plan
	 * @return
	 */
	@Transactional
	public String guncelle(VardiyaPlan plan) {
		vardiyaGuncelle = ikRole || ortakIslemler.getParameterKeyHasStringValue("vardiyaGuncelle");
		setVardiyaPlan(plan);
		fillVardiyaSablonList(plan.getPersonel());
		fillVardiyaList();

		if (plan.getVardiyaHafta1().isCheckBoxDurum())
			haftaListesiOlustur(plan, plan.getVardiyaHafta1());
		if (plan.getVardiyaHafta2().isCheckBoxDurum())
			haftaListesiOlustur(plan, plan.getVardiyaHafta2());

		vardiyaSaatDakika(plan.getVardiyaHafta1().getVardiyaGun1(), Boolean.FALSE);
		vardiyaSaatDakika(plan.getVardiyaHafta1().getVardiyaGun2(), Boolean.FALSE);
		vardiyaSaatDakika(plan.getVardiyaHafta1().getVardiyaGun3(), Boolean.FALSE);
		vardiyaSaatDakika(plan.getVardiyaHafta1().getVardiyaGun4(), Boolean.FALSE);
		vardiyaSaatDakika(plan.getVardiyaHafta1().getVardiyaGun5(), Boolean.FALSE);
		vardiyaSaatDakika(plan.getVardiyaHafta1().getVardiyaGun6(), Boolean.FALSE);
		vardiyaSaatDakika(plan.getVardiyaHafta1().getVardiyaGun7(), Boolean.FALSE);
		vardiyaSaatDakika(plan.getVardiyaHafta2().getVardiyaGun1(), Boolean.FALSE);
		vardiyaSaatDakika(plan.getVardiyaHafta2().getVardiyaGun2(), Boolean.FALSE);
		vardiyaSaatDakika(plan.getVardiyaHafta2().getVardiyaGun3(), Boolean.FALSE);
		vardiyaSaatDakika(plan.getVardiyaHafta2().getVardiyaGun4(), Boolean.FALSE);
		vardiyaSaatDakika(plan.getVardiyaHafta2().getVardiyaGun5(), Boolean.FALSE);
		vardiyaSaatDakika(plan.getVardiyaHafta2().getVardiyaGun6(), Boolean.FALSE);
		vardiyaSaatDakika(plan.getVardiyaHafta2().getVardiyaGun7(), Boolean.FALSE);
		setVardiyaPlan(plan);
		setDegisti(Boolean.FALSE);

		return "";

	}

	/**
	 * @return
	 */
	public String degerDegisti() {
		setDegisti(Boolean.TRUE);
		return "";
	}

	/**
	 * @param vardiyaGun
	 * @param degistir
	 */
	public void vardiyaSaatDakika(VardiyaGun vardiyaGun, boolean degistir) {
		if (vardiyaGun.getVardiya() != null && vardiyaGun.getVardiya().isCalisma()) {
			try {
				if (degistir || vardiyaGun.getBasSaat() == null)
					vardiyaGun.setBasSaat((int) vardiyaGun.getVardiya().getBasSaat());
				if (degistir || vardiyaGun.getBasDakika() == null)
					vardiyaGun.setBasDakika((int) vardiyaGun.getVardiya().getBasDakika());
				if (degistir || vardiyaGun.getBitSaat() == null)
					vardiyaGun.setBitSaat((int) vardiyaGun.getVardiya().getBitSaat());
				if (degistir || vardiyaGun.getBitDakika() == null)
					vardiyaGun.setBitDakika((int) vardiyaGun.getVardiya().getBitDakika());
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());

			}

		}
		setDegisti(Boolean.TRUE);
	}

	/**
	 * @param vardiyaGun
	 * @return
	 */
	public String vardiyaSec(VardiyaGun vardiyaGun) {
		setSeciliVardiyaGun(vardiyaGun);
		return "";
	}

	/**
	 * @param user
	 * @param vardiyaGun
	 * @return
	 */
	public VardiyaGun vardiyaKaydet(User user, VardiyaGun vardiyaGun) {

		if (vardiyaGun.getVardiya() != null && vardiyaGun.getIsKurVardiya() != null) {
			vardiyaGun.setCheckBoxDurum(Boolean.TRUE);
			if (vardiyaGun.getId() != null) {
				if (vardiyaGun.getVardiya().isCalisma()) {
					if (vardiyaGun.getBasSaat() == vardiyaGun.getVardiya().getBasSaat() && vardiyaGun.getBasDakika() == vardiyaGun.getVardiya().getBasDakika()) {
						vardiyaGun.setBasSaat(null);
						vardiyaGun.setBasDakika(null);
					}
					if (vardiyaGun.getBitSaat() == vardiyaGun.getVardiya().getBitSaat() && vardiyaGun.getBitDakika() == vardiyaGun.getVardiya().getBitDakika()) {
						vardiyaGun.setBitSaat(null);
						vardiyaGun.setBitDakika(null);
					}

				} else {
					vardiyaGun.setBasSaat(null);
					vardiyaGun.setBasDakika(null);
					vardiyaGun.setBitSaat(null);
					vardiyaGun.setBitDakika(null);
				}
			}

			if (kaydet) {
				IsKurVardiyaGun isKurVardiya = vardiyaGun.getIsKurVardiya();
				if (isKurVardiya.getId() == null)
					isKurVardiya.setOlusturanUser(user);
				else {
					isKurVardiya.setGuncelleyenUser(user);
					isKurVardiya.setGuncellemeTarihi(new Date());
				}

				// vardiyaGun = (VardiyaGun)
				// pdksEntityController.save(vardiyaGun);

				pdksEntityController.saveOrUpdate(session, entityManager, isKurVardiya);

			}
		}
		return vardiyaGun;

	}

	/**
	 * @param hafta
	 */
	@Transactional
	public void sablonVardiyalarUygula(VardiyaHafta hafta) {
		if (hafta.isCheckBoxDurum()) {
			setDegisti(Boolean.TRUE);
			VardiyaSablonu vardiyaSablonu = hafta.getVardiyaSablonu();
			boolean vardiyaDegistir = Boolean.TRUE;
			ArrayList<Vardiya> tatilVardiyaList = null;
			try {
				tatilVardiyaList = fillVardiyaDoldurList(vardiyaSablonu, vardiyaPlan.getPersonel());

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
			}
			ArrayList<Vardiya> vardiyaList = new ArrayList<Vardiya>();
			for (Vardiya vardiya : tatilVardiyaList)
				vardiyaList.add(vardiya);

			hafta.setVardiyalar(vardiyaList);
			hafta.getVardiyaGunler().clear();
			hafta.getVardiyaGunler().add(vardiyaListeDegistir(hafta.getVardiyaGun1(), vardiyaList, tatilVardiyaList, vardiyaDegistir ? vardiyaSablonu.getVardiya1() : null));
			hafta.getVardiyaGunler().add(vardiyaListeDegistir(hafta.getVardiyaGun2(), vardiyaList, tatilVardiyaList, vardiyaDegistir ? vardiyaSablonu.getVardiya2() : null));
			hafta.getVardiyaGunler().add(vardiyaListeDegistir(hafta.getVardiyaGun3(), vardiyaList, tatilVardiyaList, vardiyaDegistir ? vardiyaSablonu.getVardiya3() : null));
			hafta.getVardiyaGunler().add(vardiyaListeDegistir(hafta.getVardiyaGun4(), vardiyaList, tatilVardiyaList, vardiyaDegistir ? vardiyaSablonu.getVardiya4() : null));
			hafta.getVardiyaGunler().add(vardiyaListeDegistir(hafta.getVardiyaGun5(), vardiyaList, tatilVardiyaList, vardiyaDegistir ? vardiyaSablonu.getVardiya5() : null));
			hafta.getVardiyaGunler().add(vardiyaListeDegistir(hafta.getVardiyaGun6(), vardiyaList, tatilVardiyaList, vardiyaDegistir ? vardiyaSablonu.getVardiya6() : null));
			hafta.getVardiyaGunler().add(vardiyaListeDegistir(hafta.getVardiyaGun7(), vardiyaList, tatilVardiyaList, vardiyaDegistir ? vardiyaSablonu.getVardiya7() : null));
		}
	}

	/**
	 * 
	 * @param vardiyaMap
	 * @param plan
	 * @param mesaj
	 * @param excelAktar
	 * @return
	 */
	public boolean vardiyaPlanKontrol(TreeMap<Long, Vardiya> vardiyaMap, VardiyaPlan plan, String mesaj, boolean excelAktar) {
		boolean yaz = Boolean.TRUE;
		boolean haftaTatil = Boolean.FALSE;
		Calendar cal = Calendar.getInstance();
		Date ilkGun = aylikPuantajDefault.getIlkGun(), iseBaslamaTarihi = null, istenAyrilmaTarihi = null;
		Date sonGun = ortakIslemler.tariheAyEkleCikar(cal, ilkGun, 1);
		boolean admin = ikRole;
		StringBuffer sb = new StringBuffer();
		if (vardiyaMap != null && vardiyaMap.isEmpty())
			vardiyaMap = null;
		TreeMap<String, VardiyaGun> vardiyaGunMap = new TreeMap<String, VardiyaGun>();
		if (plan != null && plan.getVardiyaHaftaList() != null) {
			for (VardiyaHafta vardiyaHafta : plan.getVardiyaHaftaList()) {
				if (vardiyaHafta.getVardiyaGunler() != null) {
					for (VardiyaGun vg : vardiyaHafta.getVardiyaGunler()) {
						if (vg.getVardiya() != null) {
							vardiyaGunMap.put(vg.getVardiyaDateStr(), vg);
						}

					}
				}

			}
		}
		StringBuffer sbCalismaModeliUyumsuz = new StringBuffer();

		for (Iterator<String> iterator = vardiyaGunMap.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			VardiyaGun vardiyaGun = vardiyaGunMap.get(key);
			if (vardiyaGun.getVardiya() != null) {
				Vardiya vardiya = vardiyaGun.getVardiya();
				if (vardiya.isCalisma() && vardiya.getGenel() && vardiyaMap != null && !vardiyaMap.containsKey(vardiya.getId())) {
					if (vardiyaGun.isAyinGunu()) {
						if (sbCalismaModeliUyumsuz.length() > 0)
							sbCalismaModeliUyumsuz.append(", ");
						sbCalismaModeliUyumsuz.append(PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), "d MMMMMM ") + " " + vardiya.getAciklama() + (admin ? " [ " + vardiya.getKisaAciklama() + " ] " : ""));
						yaz = Boolean.FALSE;
					}
				}
				if (iseBaslamaTarihi == null)
					iseBaslamaTarihi = vardiyaGun.getPersonel().getIseGirisTarihi();
				if (istenAyrilmaTarihi == null)
					istenAyrilmaTarihi = vardiyaGun.getPersonel().getSonCalismaTarihi();
				if (vardiyaGun.getVardiya().isHaftaTatil()) {
					if (haftaTatil) {
						yaz = Boolean.FALSE;
						sb.append("Arka arkaya hafta tatili olamaz! ");
					}
					haftaTatil = Boolean.TRUE;
				} else
					haftaTatil = Boolean.FALSE;
			} else
				haftaTatil = Boolean.FALSE;
		}
		if (yaz) {
			List<VardiyaHafta> vardiyaHaftaList = plan.getVardiyaHaftaList();
			if (vardiyaHaftaList == null) {
				vardiyaHaftaList = new ArrayList<VardiyaHafta>();
				for (int i = 1; i < 3; i++) {
					VardiyaHafta vardiyaHafta = i == 1 ? plan.getVardiyaHafta1() : plan.getVardiyaHafta2();
					vardiyaHaftaList.add(vardiyaHafta);
				}
			}
			List<VardiyaGun> haftaTatilAyList = new ArrayList<VardiyaGun>(), haftaTatilSonrakiAyList = new ArrayList<VardiyaGun>(), haftaTatilOncekiAyList = new ArrayList<VardiyaGun>();
			for (int i = 1; i <= vardiyaHaftaList.size(); i++) {
				VardiyaHafta vardiyaHafta = vardiyaHaftaList.get(i - 1);
				if (iseBaslamaTarihi == null && vardiyaHafta.getPersonel() != null)
					iseBaslamaTarihi = vardiyaHafta.getPersonel().getIseGirisTarihi();
				if (istenAyrilmaTarihi == null && vardiyaHafta.getPersonel() != null)
					istenAyrilmaTarihi = vardiyaHafta.getPersonel().getSonCalismaTarihi();

				String haftaStr = PdksUtil.convertToDateString(vardiyaHafta.getBasTarih(), PdksUtil.getDateFormat()) + " - " + PdksUtil.convertToDateString(vardiyaHafta.getBitTarih(), PdksUtil.getDateFormat()) + " tarihleri arası";
				boolean parcaliBitHafta = false, parcaliBasHafta = false;
				haftaTatilSonrakiAyList.clear();
				haftaTatilAyList.clear();
				haftaTatilOncekiAyList.clear();
				if (excelAktar) {
					parcaliBasHafta = denklestirmeOncekiAy != null && ilkGun.getTime() > vardiyaHafta.getBasTarih().getTime();
					parcaliBitHafta = denklestirmeSonrakiAy != null && sonGun.getTime() <= vardiyaHafta.getBitTarih().getTime();
				}
				ArrayList<Vardiya> list = new ArrayList<Vardiya>();
				List<VardiyaGun> vardiyaGunler = vardiyaHafta.getVardiyaGunler();
				if (vardiyaGunler != null && vardiyaGunler.size() == 7) {
					for (VardiyaGun pdksVardiyaGun : vardiyaGunler) {
						addHaftaVardiya(list, pdksVardiyaGun, excelAktar);
						if (getVardiyaDurum(pdksVardiyaGun, i))
							yaz = Boolean.FALSE;
					}
				} else {
					if (vardiyaHafta.getVardiyaGun1().getVardiya() != null) {
						addHaftaVardiya(list, vardiyaHafta.getVardiyaGun1(), excelAktar);
						if (getVardiyaDurum(vardiyaHafta.getVardiyaGun1(), i))
							yaz = Boolean.FALSE;
					}
					if (vardiyaHafta.getVardiyaGun2().getVardiya() != null) {
						addHaftaVardiya(list, vardiyaHafta.getVardiyaGun2(), excelAktar);
						if (getVardiyaDurum(vardiyaHafta.getVardiyaGun2(), i))
							yaz = Boolean.FALSE;
					}
					if (vardiyaHafta.getVardiyaGun3().getVardiya() != null) {
						addHaftaVardiya(list, vardiyaHafta.getVardiyaGun3(), excelAktar);
						if (getVardiyaDurum(vardiyaHafta.getVardiyaGun3(), i))
							yaz = Boolean.FALSE;
					}
					if (vardiyaHafta.getVardiyaGun4().getVardiya() != null) {
						addHaftaVardiya(list, vardiyaHafta.getVardiyaGun4(), excelAktar);
						if (getVardiyaDurum(vardiyaHafta.getVardiyaGun4(), i))
							yaz = Boolean.FALSE;
					}
					if (vardiyaHafta.getVardiyaGun5().getVardiya() != null) {
						addHaftaVardiya(list, vardiyaHafta.getVardiyaGun5(), excelAktar);
						if (getVardiyaDurum(vardiyaHafta.getVardiyaGun5(), i))
							yaz = Boolean.FALSE;
					}
					if (vardiyaHafta.getVardiyaGun6().getVardiya() != null) {
						addHaftaVardiya(list, vardiyaHafta.getVardiyaGun6(), excelAktar);
						if (getVardiyaDurum(vardiyaHafta.getVardiyaGun6(), i))
							yaz = Boolean.FALSE;
					}
					if (vardiyaHafta.getVardiyaGun7().getVardiya() != null) {
						addHaftaVardiya(list, vardiyaHafta.getVardiyaGun7(), excelAktar);
						if (getVardiyaDurum(vardiyaHafta.getVardiyaGun7(), i))
							yaz = Boolean.FALSE;
					}
				}
				int tatilAdet = 0, offAdet = 0, gunSayisi = 0;
				if (iseBaslamaTarihi != null && vardiyaHafta.getBasTarih().getTime() < iseBaslamaTarihi.getTime() && vardiyaHafta.getBitTarih().getTime() >= iseBaslamaTarihi.getTime()) {
					Double dakikaFarki = PdksUtil.getDakikaFarki(iseBaslamaTarihi, vardiyaHafta.getBasTarih());
					gunSayisi = new Double(dakikaFarki.doubleValue() / 1440d).intValue();
				}
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					Vardiya vardiya = (Vardiya) iterator.next();
					Date vardiyaDate = vardiya.getVardiyaTarih();
					if (vardiyaDate == null)
						vardiyaDate = ortakIslemler.tariheGunEkleCikar(cal, vardiyaHafta.getBasTarih(), gunSayisi);
					++gunSayisi;

					if (vardiya.isCalisma() && vardiya.getGenel() && vardiyaMap != null && !vardiyaMap.containsKey(vardiya.getId())) {
						String key = PdksUtil.convertToDateString(vardiya.getVardiyaTarih(), "yyyyMMdd");
						VardiyaGun vardiyaGun = vardiyaGunMap.get(key);
						if (vardiyaGun == null || vardiyaGun.isAyinGunu()) {
							if (sbCalismaModeliUyumsuz.length() > 0)
								sbCalismaModeliUyumsuz.append(", ");
							sbCalismaModeliUyumsuz.append(PdksUtil.convertToDateString(vardiya.getVardiyaTarih(), "d MMMMMM ") + " " + vardiya.getAciklama() + (admin ? " [ " + vardiya.getKisaAciklama() + " ] " : ""));
							yaz = Boolean.FALSE;
						}
					}
					if (vardiyaDate.before(iseBaslamaTarihi) || vardiyaDate.after(istenAyrilmaTarihi)) {
						iterator.remove();
						continue;
					}

					if (vardiya.isHaftaTatil()) {
						if (parcaliBasHafta) {
							String key = PdksUtil.convertToDateString(vardiyaDate, "yyyyMMdd");
							if (plan.getVardiyaGunMap().containsKey(key)) {
								VardiyaGun vardiyaGun = plan.getVardiyaGunMap().get(key);
								if (vardiyaGun.getVardiya().isHaftaTatil() && vardiyaGun.getPersonel().isCalisiyorGun(vardiyaDate)) {
									if (vardiyaDate.before(ilkGun))
										haftaTatilOncekiAyList.add(vardiyaGun);
									else
										haftaTatilAyList.add(vardiyaGun);
								}
							}

						} else if (parcaliBitHafta) {
							String key = PdksUtil.convertToDateString(vardiyaDate, "yyyyMMdd");
							if (plan.getVardiyaGunMap().containsKey(key)) {
								VardiyaGun vardiyaGun = plan.getVardiyaGunMap().get(key);
								if (vardiyaGun.getVardiya().isHaftaTatil() && vardiyaGun.getPersonel().isCalisiyorGun(vardiyaDate)) {
									if (vardiyaDate.before(sonGun))
										haftaTatilAyList.add(vardiyaGun);
									else
										haftaTatilSonrakiAyList.add(vardiyaGun);
								}
							}

						}
						++tatilAdet;
					}

					if (PdksUtil.getPlanOffAdet() > 0 && vardiya.isOffGun())
						++offAdet;

				}

				if (tatilAdet == 0 && list.size() == 7) {
					sb.append(haftaStr + " tatil tanımlanmamıştır! ");
					yaz = Boolean.FALSE;
				}
				boolean partTime = Boolean.FALSE;
				if (plan.getPersonel() != null && plan.getPersonel().getPartTime() != null)
					partTime = plan.getPersonel().getPartTime();
				if (!partTime && offAdet > 0 && offAdet > PdksUtil.getPlanOffAdet()) {
					sb.append(haftaStr + " en fazla " + (PdksUtil.getPlanOffAdet()) + " off gün tanımlanmalıdır! ");
					yaz = Boolean.FALSE;
				} else if (tatilAdet > 1) {
					if (haftaTatilAyList.size() == 1 && haftaTatilOncekiAyList.size() == 1) {
						VardiyaGun vardiyaGunBuAy = haftaTatilAyList.get(0), vardiyaGunOncekiAy = haftaTatilOncekiAyList.get(0);
						vardiyaGunOncekiAy.setVardiya(vardiyaGunBuAy.getEskiVardiya());
						vardiyaGunOncekiAy.setGuncelleyenUser(authenticatedUser);
						vardiyaGunOncekiAy.setGuncellemeTarihi(new Date());
						if (vardiyaGunOncekiAy.getIsKurVardiya() != null)
							pdksEntityController.saveOrUpdate(session, entityManager, vardiyaGunOncekiAy.getIsKurVardiya());
					} else if (haftaTatilAyList.size() == 1 && haftaTatilSonrakiAyList.size() == 1) {
						VardiyaGun vardiyaGunBuAy = haftaTatilAyList.get(0), vardiyaGunSonrakiAy = haftaTatilSonrakiAyList.get(0);
						vardiyaGunSonrakiAy.setVardiya(vardiyaGunBuAy.getEskiVardiya());
						vardiyaGunSonrakiAy.setGuncelleyenUser(authenticatedUser);
						vardiyaGunSonrakiAy.setGuncellemeTarihi(new Date());

						if (vardiyaGunSonrakiAy.getIsKurVardiya() != null)
							pdksEntityController.saveOrUpdate(session, entityManager, vardiyaGunSonrakiAy.getIsKurVardiya());
					} else {
						sb.append(haftaStr + " en fazla bir tatil günü tanımlanmalıdır! ");
						yaz = Boolean.FALSE;
					}
				}
			}

		}
		if (!yaz) {
			if (sb.length() > 0)
				PdksUtil.addMessageAvailableWarn(mesaj + sb.toString());

		}
		vardiyaGunMap = null;
		sb = null;
		sbCalismaModeliUyumsuz = null;

		return yaz;

	}

	/**
	 * @param list
	 * @param vardiyaGun
	 * @param excelAktar
	 */
	private void addHaftaVardiya(ArrayList<Vardiya> list, VardiyaGun vardiyaGun, boolean excelAktar) {
		try {
			if (vardiyaGun != null) {
				if (vardiyaGun.getVardiya() != null) {
					Vardiya vardiya = (Vardiya) vardiyaGun.getVardiya().clone();
					vardiya.setVardiyaTarih(vardiyaGun.getVardiyaDate());
					if (excelAktar == false && vardiya != null && vardiya.isOffGun() && vardiyaGun.getTatil() != null)
						vardiya = null;
					if (vardiya != null)
						list.add(vardiya);
				} else
					logger.debug(vardiyaGun.getVardiyaDateStr());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param gorevYeriAciklama
	 * @param puantajList
	 * @return
	 */
	public ByteArrayOutputStream aylikVardiyaExcelDevam(String gorevYeriAciklama, List<AylikPuantaj> puantajList) {
		List<Boolean> onayDurumList = new ArrayList<Boolean>();
		for (AylikPuantaj aylikPuantaj : puantajList) {
			if (!onayDurumList.contains(aylikPuantaj.isOnayDurum()))
				onayDurumList.add(aylikPuantaj.isOnayDurum());
		}
		ByteArrayOutputStream baos = null;
		String aciklamaExcel = PdksUtil.replaceAll(gorevYeriAciklama + " " + PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyy MMMMMM  "), "_", "");
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "MMMMM yyyy") + " İşkur Çalışma Planı", Boolean.TRUE);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		XSSFCellStyle styleTatil = (XSSFCellStyle) ExcelUtil.getStyleDataCenter(wb);
		XSSFCellStyle styleIstek = (XSSFCellStyle) ExcelUtil.getStyleDataCenter(wb);
		XSSFCellStyle styleEgitim = (XSSFCellStyle) ExcelUtil.getStyleDataCenter(wb);
		XSSFCellStyle styleOff = (XSSFCellStyle) ExcelUtil.getStyleDataCenter(wb);
		ExcelUtil.setFontColor(styleOff, Color.WHITE);
		XSSFCellStyle styleIzin = (XSSFCellStyle) ExcelUtil.getStyleDataCenter(wb);
		XSSFCellStyle styleCalisma = (XSSFCellStyle) ExcelUtil.getStyleDataCenter(wb);
		int row = 0, col = 0;

		ExcelUtil.setFillForegroundColor(styleTatil, 255, 153, 204);
		ExcelUtil.setFillForegroundColor(styleIstek, 255, 255, 0);
		ExcelUtil.setFillForegroundColor(styleIzin, 146, 208, 80);
		ExcelUtil.setFillForegroundColor(styleCalisma, 255, 255, 255);
		ExcelUtil.setFillForegroundColor(styleEgitim, 0, 0, 255);
		ExcelUtil.setFillForegroundColor(styleOff, 13, 12, 89);
		ExcelUtil.setFontColor(styleOff, 256, 256, 256);
		ExcelUtil.getCell(sheet, row, col, header).setCellValue(aciklamaExcel);
		for (int i = 0; i < 3; i++)
			ExcelUtil.getCell(sheet, row, col + i + 1, header).setCellValue("");

		try {
			sheet.addMergedRegion(ExcelUtil.getRegion((int) row, (int) 0, (int) row, (int) 4));
		} catch (Exception e) {
			e.printStackTrace();
		}
		col = 0;
		ExcelUtil.getCell(sheet, ++row, col, header).setCellValue("");
		ExcelUtil.getCell(sheet, ++row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.yoneticiAciklama());
		if (aramaSecenekleri.getSirketId() == null)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
		Calendar cal = Calendar.getInstance();
		cal.setTime(aylikPuantajDefault.getIlkGun());
		CreationHelper factory = wb.getCreationHelper();
		Drawing drawing = sheet.createDrawingPatriarch();
		ClientAnchor anchor = factory.createClientAnchor();

		for (int i = 0; i < aylikPuantajDefault.getGunSayisi(); i++) {
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(cal.get(Calendar.DAY_OF_MONTH) + "\n " + authenticatedUser.getTarihFormatla(cal.getTime(), "EEE"));
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}

		for (Iterator iter = puantajList.iterator(); iter.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();

			Personel personel = aylikPuantaj.getPdksPersonel();
			if (!aylikPuantaj.isSecili() || personel == null || personel.getSicilNo() == null || personel.getSicilNo().trim().equals(""))
				continue;
			row++;
			boolean help = helpPersonel(aylikPuantaj.getPdksPersonel());

			CellStyle styleGenelCenter = null, styleGenel = null;
			try {
				if (row % 2 != 0) {
					styleGenel = styleOdd;
					styleGenelCenter = styleOddCenter;
				} else {
					styleGenel = styleEven;
					styleGenelCenter = styleEvenCenter;
				}

				boolean koyuRenkli = onayDurumList.size() == 2 && aylikPuantaj.isOnayDurum();
				if (koyuRenkli) {
					ExcelUtil.setFontNormalBold(wb, styleGenel);
					ExcelUtil.setFontNormalBold(wb, styleGenelCenter);
				}
				col = 0;
				ExcelUtil.getCell(sheet, row, col++, styleGenelCenter).setCellValue(personel.getSicilNo());
				Cell personelCell = ExcelUtil.getCell(sheet, row, col++, styleGenel);
				personelCell.setCellValue(personel.getAdSoyad());
				String titlePersonel = "";

				if (titlePersonel != null) {
					Comment comment1 = drawing.createCellComment(anchor);
					RichTextString str1 = factory.createRichTextString(titlePersonel);
					comment1.setString(str1);
					personelCell.setCellComment(comment1);
				}
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(aylikPuantaj.getYonetici() != null && aylikPuantaj.getYonetici().getId() != null ? aylikPuantaj.getYonetici().getAdSoyad() : "");
				if (aramaSecenekleri.getSirketId() == null)
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getSirket() != null ? personel.getSirket().getAd() : "");
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
				List vardiyaList = aylikPuantaj.getAyinVardiyalari();

				for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
					VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator.next();
					String styleText = pdksVardiyaGun.getAylikClassAdi(aylikPuantaj.getTrClass());
					styleGenel = styleCalisma;
					if (styleText.equals(VardiyaGun.STYLE_CLASS_HAFTA_TATIL))
						styleGenel = styleTatil;
					else if (styleText.equals(VardiyaGun.STYLE_CLASS_IZIN))
						styleGenel = styleIzin;
					else if (styleText.equals(VardiyaGun.STYLE_CLASS_OZEL_ISTEK))
						styleGenel = styleIstek;
					else if (styleText.equals(VardiyaGun.STYLE_CLASS_EGITIM))
						styleGenel = styleEgitim;
					else if (styleText.equals(VardiyaGun.STYLE_CLASS_OFF))
						styleGenel = styleOff;
					Cell cell = ExcelUtil.getCell(sheet, row, col++, styleGenel);
					String aciklama = !help || calisan(pdksVardiyaGun) ? pdksVardiyaGun.getOzelAciklama(Boolean.TRUE) : "";
					if (aciklama.equals(".") && pdksVardiyaGun.getVardiya() != null && pdksVardiyaGun.getVardiya().isCalisma() == false) {
						aciklama = pdksVardiyaGun.getVardiya().getKisaAdi();
					}

					String title = !help || calisan(pdksVardiyaGun) ? pdksVardiyaGun.getTitle() : null;
					if (title != null) {
						Comment comment1 = drawing.createCellComment(anchor);
						RichTextString str1 = factory.createRichTextString(title);
						comment1.setString(str1);
						cell.setCellComment(comment1);

					}
					cell.setCellValue(aciklama);

				}

				styleGenel = null;
				styleGenelCenter = null;
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				logger.error(row);

			}
		}

		try {

			baos = new ByteArrayOutputStream();
			wb.write(baos);
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			baos = null;
		}
		onayDurumList = null;
		return baos;

	}

	/**
	 * @param sheet
	 * @param rowNo
	 * @param columnNo
	 * @param style
	 * @param deger
	 */
	public Cell setCell(Sheet sheet, int rowNo, int columnNo, CellStyle style, Double deger) {
		Cell cell = ExcelUtil.getCell(sheet, rowNo, columnNo, style);

		try {
			if (deger != 0.0d) {
				cell.setCellValue(authenticatedUser.sayiFormatliGoster(deger));
			}

		} catch (Exception e) {
		}
		return cell;
	}

	/**
	 * @param sheet
	 * @param rowNo
	 * @param columnNo
	 * @param style
	 * @param deger
	 */
	public Cell setCell(Sheet sheet, int rowNo, int columnNo, CellStyle style, Integer deger) {
		Cell cell = ExcelUtil.getCell(sheet, rowNo, columnNo, style);

		try {
			if (deger != 0.0d) {
				cell.setCellValue(deger);
			}

		} catch (Exception e) {
		}
		return cell;
	}

	/**
	 * @return
	 */
	public String aylikVardiyaExcel() {
		try {
			for (Iterator iter = aylikPuantajList.iterator(); iter.hasNext();) {
				AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();
				aylikPuantaj.setSecili(Boolean.TRUE);
			}
			String gorevYeriAciklama = getExcelAciklama();
			baosDosya = aylikVardiyaExcelDevam(gorevYeriAciklama, aylikPuantajList);
			if (baosDosya != null) {
				dosyaAdi = "AylıkÇalışmaPlanı_" + gorevYeriAciklama + PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyyMM") + ".xlsx";
				PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi);
			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return "";
	}

	/**
	 * @return
	 */
	private String getExcelAciklama() {
		String gorevYeriAciklama = "";
		Long sirketId = aramaSecenekleri.getSirketId();
		HashMap parametreMap = new HashMap();
		if (sirketId != null) {
			parametreMap.put("id", sirketId);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			Sirket sirket = (Sirket) pdksEntityController.getObjectByInnerObject(parametreMap, Sirket.class);
			if (sirket != null)
				gorevYeriAciklama = sirket.getAciklama() + "_";
		}
		return gorevYeriAciklama;
	}

	/**
	 * @return
	 */
	public String aylikRefresh() {
		if (!islemYapiliyor) {
			try {
				islemYapiliyor = Boolean.TRUE;
				if (personelAylikPuantaj != null && (!vardiyalarMap.isEmpty()) && personelAylikPuantaj.isKaydet()) {
					for (Iterator iterator = vardiyalarMap.keySet().iterator(); iterator.hasNext();) {
						String key = (String) iterator.next();
						VardiyaGun pdksVardiyaGun = vardiyalarMap.get(key);
						IsKurVardiyaGun isKurVardiyaGun = pdksVardiyaGun.getIsKurVardiya();
						if (isKurVardiyaGun.getId() != null && pdksVardiyaGun.isGuncellendi()) {
							Boolean refresh = Boolean.TRUE;
							try {
								Vardiya seciliVardiya = pdksVardiyaGun.getVardiya();
								if (seciliVardiya != null && kayitliVardiyalarMap.containsKey(key)) {
									Vardiya pdksVardiya = kayitliVardiyalarMap.get(key);
									refresh = !pdksVardiya.getId().equals(seciliVardiya.getId());
								}
							} catch (Exception e1) {
							}

							if (refresh && isKurVardiyaGun != null && isKurVardiyaGun.getId() != null)
								session.refresh(isKurVardiyaGun);
						}
					}
				}
			} catch (Exception ee) {
				logger.info(ee.getMessage());
			} finally {
				islemYapiliyor = Boolean.FALSE;
			}
		}

		return "";
	}

	/**
	 * @param aylikPuantaj
	 * @param tipi
	 * @return
	 */
	public String aylikPuantajSec(AylikPuantaj aylikPuantaj, String tipi) {
		if (personelDenklestirmeDinamikAlanList == null)
			personelDenklestirmeDinamikAlanList = new ArrayList<PersonelDenklestirmeDinamikAlan>();
		else
			personelDenklestirmeDinamikAlanList.clear();
		gebeSutIzniGuncelle = false;
		kayitliVardiyalarMap.clear();
		vardiyalarMap.clear();
		boolean kaydet = !authenticatedUser.isAdmin() && denklestirmeAyDurum;
		Personel personel = aylikPuantaj.getPdksPersonel();
		aylikPuantaj.setKaydet(kaydet);
		if (ozelDurumList == null)
			ozelDurumList = new ArrayList<SelectItem>();
		else
			ozelDurumList.clear();
		ozelDurumList.add(new SelectItem(VardiyaGorev.OZEL_ISTEK_YOK, ""));
		ozelDurumList.add(new SelectItem(VardiyaGorev.OZEL_ISTEK_PERSONEL, "Özel İstek"));
		if (authenticatedUser.getDepartman().isAdminMi()) {
			ozelDurumList.add(new SelectItem(VardiyaGorev.OZEL_ISTEK_EGITIM, "Eğitim"));
		}
		ozelDurumList.add(new SelectItem(VardiyaGorev.OZEL_RAPOR_IZNI, "Rapor"));
		if (aylikPuantaj.getSablonAylikPuantaj().getSonGun().getTime() < personel.getSonCalismaTarihi().getTime())
			ozelDurumList.add(new SelectItem(VardiyaGorev.OZEL_ISTIFA, "İstifa"));

		aylikPuantaj.setGorevYeriSec(Boolean.FALSE);
		setVardiyaList(fillAllVardiyaList());
		setPersonelAylikPuantaj(aylikPuantaj);
		VardiyaGun oncekiVardiya = null;
		TreeMap<String, VardiyaGun> vm = new TreeMap<String, VardiyaGun>();

		for (VardiyaGun pdksVardiyaGun : aylikPuantaj.getVardiyalar()) {
			if (oncekiVardiya != null) {
				if (pdksVardiyaGun.getVardiya() != null)
					oncekiVardiya.setSonrakiVardiyaGun(pdksVardiyaGun);
			}
			if (!gebeSutIzniGuncelle && pdksVardiyaGun.isAyinGunu() && pdksVardiyaGun.getVardiya() != null && AylikPuantaj.getGebelikGuncelle())
				gebeSutIzniGuncelle = pdksVardiyaGun.getIzin() == null && pdksVardiyaGun.getVardiya().getGebelik();
			setVardiyalarToMap(pdksVardiyaGun, vm, aylikPuantaj);
			pdksVardiyaGun.setSonrakiVardiya(null);
			pdksVardiyaGun.setOncekiVardiyaGun(oncekiVardiya);
		}
		planYetkilendir(aylikPuantaj);
		sablonGuncelle = authenticatedUser == null && denklestirmeAyDurum && personelAylikPuantaj.isKaydet() && aylikPuantaj.isKaydet();
		if (sablonGuncelle) {
			String sablonGuncelleStr = ortakIslemler.getParameterKey("sablonGuncellemeyenDepartmanlar");
			if (PdksUtil.hasStringValue(sablonGuncelleStr)) {
				List<String> depList = Arrays.asList(sablonGuncelleStr.split(","));
				sablonGuncelle = !depList.contains(personel.getSirket().getDepartman().getId().toString());
			}
			// && !personel.getSirket().getDepartman().isAdminMi();
		}

		if (sablonGuncelle) {
			sablonGuncelle = Boolean.FALSE;
			if (aylikPuantaj.getVardiyaHaftaList() != null) {
				for (VardiyaHafta pdksVardiyaHafta : aylikPuantaj.getVardiyaHaftaList()) {
					if (pdksVardiyaHafta == null)
						continue;
					pdksVardiyaHafta.setGuncellendi(Boolean.FALSE);
					Boolean durum = null;
					for (VardiyaGun vardiyaGun : pdksVardiyaHafta.getVardiyaGunler()) {
						if (vardiyaGun.isAylikGirisYap() && vardiyaGun.getIzin() == null && vardiyaGun.isKullaniciYetkili()) {
							if (durum == null)
								durum = Boolean.TRUE;
							if (vardiyaGun == null || vardiyaGun.getVardiya() == null)
								durum = Boolean.FALSE;
							else
								kayitliVardiyalarMap.put(vardiyaGun.getVardiyaDateStr(), vardiyaGun.getVardiya());
						}

					}
					pdksVardiyaHafta.setDurum(durum != null && durum);
					if (!sablonGuncelle)
						sablonGuncelle = pdksVardiyaHafta.getDurum();

				}
				if (sablonGuncelle)
					fillVardiyaSablonList(personel);
			}

		}

		vardiyalarSec(aylikPuantaj);
		if (tipi.equals("M")) {
			seciliVardiyaGun = null;
			TreeMap<Long, VardiyaGun> varMap = new TreeMap<Long, VardiyaGun>();
			try {
				for (VardiyaGun vg : aylikPuantaj.getVardiyalar()) {
					vg.setFazlaMesaiTalepler(null);
					if (vg.getId() != null && vg.isAyinGunu())
						varMap.put(vg.getId(), vg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (!varMap.isEmpty()) {
				HashMap fields = new HashMap();
				fields.put("vardiyaGun.id", new ArrayList(varMap.keySet()));
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<FazlaMesaiTalep> fazlaMesaiTalepler = pdksEntityController.getObjectByInnerObjectList(fields, FazlaMesaiTalep.class);
				if (fazlaMesaiTalepler.size() > 1)
					fazlaMesaiTalepler = PdksUtil.sortListByAlanAdi(fazlaMesaiTalepler, "id", Boolean.TRUE);
				if (!fazlaMesaiTalepler.isEmpty()) {
					for (FazlaMesaiTalep fmt : fazlaMesaiTalepler) {
						VardiyaGun vg = vm.get(fmt.getVardiyaGun().getVardiyaKeyStr());
						if (vg.getFazlaMesaiTalepler() == null)
							vg.setFazlaMesaiTalepler(new ArrayList<FazlaMesaiTalep>());
						vg.getFazlaMesaiTalepler().add(fmt);
					}
				}
			}

		}
		if (tipi.equals("M") && !vm.isEmpty())
			ortakIslemler.fazlaMesaiSaatiAyarla(vm);

		return "";
	}

	/**
	 * @param vg
	 * @param vm
	 * @param puantaj
	 */
	private void setVardiyalarToMap(VardiyaGun vg, TreeMap<String, VardiyaGun> vm, AylikPuantaj puantaj) {
		vg.setIslemVardiya(null);
		vg.setIslendi(Boolean.FALSE);
		vg.setVardiyaZamani();
		vg.setSonrakiVardiya(null);
		vg.setHareketHatali(Boolean.FALSE);
		vg.saklaVardiya();
		vg.setGuncellendi(Boolean.FALSE);
		PersonelIzin izin = vg.getIzin();
		String vardiyaKey = vg.getVardiyaKey();
		if (izin != null) {
			boolean izinGirisYok = izin.getIzinTipi().getPersonelGirisTipi().equals(IzinTipi.GIRIS_TIPI_YOK);
			CalismaModeli cm = null;
			try {
				cm = offIzinGuncelle && izinGirisYok && puantaj != null ? puantaj.getPersonelDenklestirmeAylik().getCalismaModeli() : null;
			} catch (Exception e) {
				cm = null;
			}

			boolean izinGuncelleme = cm == null;
			if (ikRole == false || denklestirmeAyDurum == false || vg.getTatil() != null || vg.getVardiya().isOffGun() == false)
				izinGuncelleme = true;
			else if (cm != null) {
				int haftaGun = vg.getHaftaninGunu();
				boolean cumartesiCalisiyor = cm.getHaftaSonu() > 0.0d;
				if (haftaGun != Calendar.SUNDAY && (cumartesiCalisiyor == false || haftaGun != Calendar.SATURDAY))
					izinGuncelleme = !offIzinGuncelle;
			}
			if (izinGuncelleme)
				vg.setIzin(izin);
		}
		vm.put(vardiyaKey, vg);

	}

	/**
	 * @param pdksVardiyaHafta
	 * @return
	 */
	public String aylikSablonGuncelle(VardiyaHafta pdksVardiyaHafta) {
		VardiyaSablonu vardiyaSablonu = pdksVardiyaHafta.getVardiyaSablonu();
		pdksVardiyaHafta.setGuncellendi(Boolean.TRUE);
		if (vardiyaSablonu != null) {
			logger.debug(pdksVardiyaHafta.getHafta() + " " + vardiyaSablonu.getAdi());
			vardiyaSablonu.vardiyaBul();
			List<Vardiya> sablonVardiyaList = vardiyaSablonu.getVardiyaList();
			int index = 0;
			for (VardiyaGun vardiyaGun : pdksVardiyaHafta.getVardiyaGunler()) {
				if (vardiyaGun.getVardiya() != null && vardiyaGun.isAylikGirisYap() && vardiyaGun.getIzin() == null && vardiyaGun.isKullaniciYetkili()) {
					Vardiya sablonVardiya = sablonVardiyaList.get(index);
					if (!sablonVardiya.getId().equals(vardiyaGun.getVardiya().getId())) {
						vardiyaGun.setVardiya(sablonVardiya);
						vardiyaDegistir(vardiyaGun);
					}
				}
				++index;
			}
		}
		return "";
	}

	/**
	 * @return
	 * @throws Exception
	 */
	@Transactional
	public String aylikKaydet() throws Exception {
		if (!islemYapiliyor) {
			islemYapiliyor = Boolean.TRUE;
			boolean flush = false;

			if (personelAylikPuantaj.getVardiyalar() != null && (!vardiyalarMap.isEmpty()) || flush)
				try {
					aylikVardiyaKontrolKaydet(Boolean.TRUE);
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}

			vardiyalarMap.clear();
			islemYapiliyor = Boolean.FALSE;
		}

		return "";
	}

	/**
	 * @return
	 */
	public String gunlukListeleriOlustur() {
		List<Integer> list = new ArrayList<Integer>();
		for (VardiyaGun pdksVardiyaGun : aylikPuantajDefault.getAyinVardiyalari()) {
			if (pdksVardiyaGun.isCheckBoxDurum()) {
				int index = PdksUtil.getDateField(pdksVardiyaGun.getVardiyaDate(), Calendar.DATE) - 1;
				list.add(index);
			}
		}
		if (list.size() == 1) {
			int index = list.get(0);
			AylikPuantaj aylikPuantajToplam = new AylikPuantaj();
			for (AylikPuantaj aylikPuantaj : aylikPuantajList) {
				puantajYetkilendir(null, aylikPuantaj, aylikPuantajToplam, null);
				List<VardiyaGun> gunler = aylikPuantaj.getAyinVardiyalari();
				VardiyaGun pdksVardiyaGun = gunler.get(index);
				pdksVardiyaGun.setGuncellendi(Boolean.FALSE);
				pdksVardiyaGun.saklaVardiya();
				aylikPuantaj.setVardiyaGun(pdksVardiyaGun);
			}

		} else if (list.size() > 1)
			PdksUtil.addMessageWarn("Bir'den fazla gün seçemezsiniz!");
		else
			PdksUtil.addMessageWarn("Bir gün seçiniz!");
		return "";
	}

	/**
	 * @return
	 */
	@Transactional
	public String gunlukListeKaydet() {

		return "";
	}

	/**
	 * @param vardiyaMap
	 * @param aylikPuantaj
	 * @param aylikPuantajToplam
	 * @param toplamVardiyaGun
	 */
	private void puantajYetkilendir(TreeMap<String, VardiyaGun> vardiyaMap, AylikPuantaj aylikPuantaj, AylikPuantaj aylikPuantajToplam, VardiyaGun toplamVardiyaGun) {
		if (toplamVardiyaGun == null) {
			toplamVardiyaGun = new VardiyaGun();
			Vardiya toplamVardiya = new Vardiya();
			toplamVardiya.setKisaAdi("Toplam");
			toplamVardiya.setId(0L);
			toplamVardiyaGun.setVardiya(toplamVardiya);

		}
		Calendar cal = Calendar.getInstance();
		String haftaTatilDurum = ortakIslemler.getParameterKey("haftaTatilDurum");

		VardiyaGun vardiyaGun = null;
		boolean bayramAksamCalismaOde = ortakIslemler.getParameterKey("bayramAksamCalismaOde").equals("1");
		for (VardiyaGun pdksVardiyaGun : aylikPuantaj.getVardiyalar()) {
			if (vardiyaGun != null) {
				Vardiya sonrakVardiya = null;
				if (pdksVardiyaGun.getVardiya() != null)
					sonrakVardiya = (Vardiya) pdksVardiyaGun.getVardiya().clone();
				else
					sonrakVardiya = null;
				vardiyaGun.setSonrakiVardiya(sonrakVardiya);
			}
			vardiyaGun = pdksVardiyaGun;

		}
		gorevli = helpPersonel(aylikPuantaj.getPdksPersonel());
		ArrayList<Vardiya> vardiyalar = fillAylikVardiyaList(aylikPuantaj, null);
		List<YemekIzin> yemekler = null;
		Date aksamVardiyaBaslangicZamani = null, aksamVardiyaBitisZamani = null;
		for (VardiyaGun pdksVardiyaGun : aylikPuantaj.getVardiyalar()) {
			if (pdksVardiyaGun != null)
				pdksVardiyaGun.setGorevliPersonelMap(gorevliPersonelMap);
			pdksVardiyaGun.setVardiyalar(null);
			aksamVardiyaBaslangicZamani = null;
			aksamVardiyaBitisZamani = null;
			Vardiya vardiya = pdksVardiyaGun.getVardiya();
			if (vardiya != null && vardiya.isAksamVardiyasi()) {
				if (aksamVardiyaBitSaat != null && aksamVardiyaBitDakika != null) {
					cal.setTime(pdksVardiyaGun.getVardiyaDate());
					cal.set(Calendar.HOUR_OF_DAY, aksamVardiyaBitSaat);
					cal.set(Calendar.MINUTE, aksamVardiyaBitDakika);
					if (vardiya.getBasSaat() > vardiya.getBitSaat())
						cal.add(Calendar.DATE, 1);
					aksamVardiyaBitisZamani = cal.getTime();
				}
				if (aksamVardiyaBasSaat != null && aksamVardiyaBasDakika != null) {
					cal.setTime(pdksVardiyaGun.getVardiyaDate());
					cal.set(Calendar.HOUR_OF_DAY, aksamVardiyaBasSaat);
					cal.set(Calendar.MINUTE, aksamVardiyaBasDakika);
					if (vardiya.getBasSaat() < vardiya.getBitSaat())
						cal.add(Calendar.DATE, -1);
					aksamVardiyaBaslangicZamani = cal.getTime();
				}
			}
			boolean kullaniciYetkili = Boolean.FALSE;
			boolean donemAcik = Boolean.FALSE;

			if (pdksVardiyaGun.getVardiya() != null) {
				if (pdksVardiyaGun.getIzin() == null) {
					setVardiyaGunleri(vardiyalar, pdksVardiyaGun);
				}
				if (pdksVardiyaGun.getVardiya() != null && pdksVardiyaGun.getIzin() == null) {
					Tanim yeniGoreviYeri = pdksVardiyaGun.getVardiyaGorev() != null ? pdksVardiyaGun.getVardiyaGorev().getYeniGorevYeri() : null;
					if (!gorevli)
						kullaniciYetkili = yeniGoreviYeri == null || gorevYerileri.isEmpty();
					else
						kullaniciYetkili = yeniGoreviYeri != null && gorevYerileri.contains(yeniGoreviYeri.getId());
				}

				if (pdksVardiyaGun.isAyinGunu()) {
					double haftaSuresi = 0d;
					if (kullaniciYetkili)
						if (vardiyaMap != null && pdksVardiyaGun.getVardiya().isCalisma() && !pdksVardiyaGun.isRaporIzni()) {
							int index = PdksUtil.getDateField(pdksVardiyaGun.getVardiyaDate(), Calendar.DATE) - 1;
							Vardiya islemVardiya = pdksVardiyaGun.getIslemVardiya();
							if (!gorevli && islemVardiya.getBasSaat() >= islemVardiya.getBitSaat()) {
								if (pdksVardiyaGun.getSonrakiVardiyaGun() != null) {
									try {
										if (pdksVardiyaGun.getSonrakiVardiya().isHaftaTatil() && (haftaTatilDurum.equals("1"))) {
											double calismaToplamSuresi = islemVardiya.getNetCalismaSuresi();
											Date haftaTatil = ortakIslemler.tariheGunEkleCikar(cal, pdksVardiyaGun.getVardiyaDate(), 1);
											String haftaGunStr = PdksUtil.convertToDateString(haftaTatil, "yyyyMMdd").substring(6);
											double aksamSure = ortakIslemler.getSaatSure(islemVardiya.getVardiyaBasZaman(), haftaTatil, yemekler, pdksVardiyaGun, session);
											haftaSuresi = ortakIslemler.getSaatSure(haftaTatil, islemVardiya.getVardiyaBitZaman(), yemekler, pdksVardiyaGun, session);
											if (calismaToplamSuresi > haftaSuresi + aksamSure) {
												double fark = calismaToplamSuresi - (haftaSuresi + aksamSure);
												if (haftaSuresi > aksamSure)
													haftaSuresi += fark;
												else
													aksamSure += fark;
											}
											pdksVardiyaGun.setCalismaSuresi(aksamSure);
											if (haftaGunStr.equals("01")) {
												haftaSuresi = 0;
											}
											// if (islemVardiya.isAksamVardiyasi())
											// aksamVardiyaSaatSayisi += pdksVardiyaGun.getCalismaSuresi();
										}
									} catch (Exception eh) {
										logger.error(eh);
										eh.printStackTrace();
									}

								}
							}
							vardiyaGunEkle(vardiyaMap, aylikPuantaj, pdksVardiyaGun, index);
							aylikPuantajToplam.setSablonAylikPuantaj(aylikPuantaj.getSablonAylikPuantaj());
							if (aylikPuantajToplam != null)
								vardiyaGunEkle(vardiyaMap, aylikPuantajToplam, toplamVardiyaGun, index);

						}
					pdksVardiyaGun.setHaftaCalismaSuresi(haftaSuresi);

				} else if (!helpPersonel(aylikPuantaj.getPdksPersonel())) {
					if (pdksVardiyaGun.getVardiyaDate().before(aylikPuantajDefault.getIlkGun())) {
						if (aylikPuantaj.getPersonelDenklestirmeGecenAy() != null)
							donemAcik = !aylikPuantaj.getPersonelDenklestirmeGecenAy().isErpAktarildi();
						else
							donemAcik = Boolean.TRUE;
						if (donemAcik && aylikPuantajDefault.getDenklestirmeGecenAy() != null)
							donemAcik = aylikPuantajDefault.getDenklestirmeGecenAy().getDurum();
					} else if (pdksVardiyaGun.getVardiyaDate().after(aylikPuantajDefault.getSonGun())) {
						if (aylikPuantaj.getPersonelDenklestirmeGelecekAy() != null)
							donemAcik = !aylikPuantaj.getPersonelDenklestirmeGelecekAy().isErpAktarildi();
						else
							donemAcik = Boolean.TRUE;
						if (donemAcik && aylikPuantajDefault.getDenklestirmeGelecekAy() != null)
							donemAcik = aylikPuantajDefault.getDenklestirmeGelecekAy().getDurum();
					}

				}
			}

			pdksVardiyaGun.setKullaniciYetkili(kullaniciYetkili);
			pdksVardiyaGun.setDonemAcik(donemAcik);
			if (pdksVardiyaGun.isAyinGunu() && aksamVardiyaBaslangicZamani != null && aksamVardiyaBitisZamani != null && pdksVardiyaGun.getIslemVardiya() != null && pdksVardiyaGun.getIslemVardiya().isAksamVardiyasi()) {
				Date cikisZaman = pdksVardiyaGun.getIslemVardiya().getVardiyaBitZaman();
				Date girisZaman = pdksVardiyaGun.getIslemVardiya().getVardiyaBasZaman();
				if (girisZaman == null || cikisZaman == null)
					continue;
				if (!(girisZaman.getTime() <= aksamVardiyaBitisZamani.getTime() && cikisZaman.getTime() >= aksamVardiyaBaslangicZamani.getTime()))
					continue;

				if (girisZaman.before(aksamVardiyaBaslangicZamani))
					girisZaman = aksamVardiyaBaslangicZamani;
				if (cikisZaman.after(aksamVardiyaBitisZamani))
					cikisZaman = aksamVardiyaBitisZamani;
				if (girisZaman.before(cikisZaman)) {
					if (girisZaman.before(aksamVardiyaBaslangicZamani))
						girisZaman = aksamVardiyaBaslangicZamani;
					if (bayramAksamCalismaOde == false) {
						Tatil tatilSakli = null;
						if (pdksVardiyaGun.getIslemVardiya() != null) {
							String key = PdksUtil.convertToDateString(pdksVardiyaGun.getVardiyaDate(), "yyyyMMdd");
							if (tatilMap.containsKey(key))
								tatilSakli = tatilMap.get(key);
							else {
								key = PdksUtil.convertToDateString(pdksVardiyaGun.getIslemVardiya().getVardiyaBitZaman(), "yyyyMMdd");
								if (tatilMap.containsKey(key))
									tatilSakli = tatilMap.get(key);
							}
						}

						if (tatilSakli != null) {
							Tatil tatilAksam = tatilSakli;
							if (tatilAksam.getOrjTatil().isTekSefer())
								tatilAksam = (Tatil) tatilAksam.getOrjTatil().clone();
							Date tatilBas = tatilAksam.getBasTarih();
							Date tatilBit = tatilAksam.getBitTarih();
							if (tatilBit.getTime() >= girisZaman.getTime() && cikisZaman.getTime() > tatilBas.getTime()) {
								if (girisZaman.before(tatilBas))
									cikisZaman = tatilBas;
								else if (cikisZaman.after(tatilBit))
									girisZaman = tatilBit;
								else
									girisZaman = cikisZaman;
							}
						}
					}
				}

			}
		}
		aylikPuantaj.setVardiyaDegisti(Boolean.FALSE);
		aylikPuantaj.setVardiyaOlustu(Boolean.TRUE);
		aylikPuantaj.setVardiyaDegisti(Boolean.FALSE);

		// if (authenticatedUser.isIK())
		// aylikPuantaj.setKaydet(aylikPuantajDefault.getAsmDenklestirmeGecenAy() == null || aylikPuantajDefault.getAsmDenklestirmeGecenAy().getDurum());
	}

	/**
	 * @param list
	 * @param pdksVardiyaGun
	 */
	private void setVardiyaGunleri(ArrayList<Vardiya> list, VardiyaGun pdksVardiyaGun) {
		if (list == null || pdksVardiyaGun.getVardiya().getDurum())
			pdksVardiyaGun.setVardiyalar(list);
		else
			pdksVardiyaGun.setKontrolVardiyalar(list);
	}

	/**
	 * @param vardiyaMap
	 * @param aylikPuantaj
	 * @param pdksVardiyaGun
	 * @param index
	 */
	private void vardiyaGunEkle(TreeMap<String, VardiyaGun> vardiyaMap, AylikPuantaj aylikPuantaj, VardiyaGun pdksVardiyaGun, int index) {
		List<Integer> gunlukList = null;
		Vardiya pdksVardiya = null;
		try {
			pdksVardiya = pdksVardiyaGun.getVardiya();
			String key = "";
			Personel personel = pdksVardiyaGun.getPersonel();
			if (personel != null)
				key += (personel.getPlanGrup2() != null ? personel.getPlanGrup2().getId().toString() : "00") + "_";
			key += pdksVardiya.getId();
			if (!vardiyaMap.containsKey(key)) {
				if (personel != null) {
					pdksVardiyaGun = (VardiyaGun) pdksVardiyaGun.clone();
					pdksVardiya = (Vardiya) pdksVardiya.clone();
					pdksVardiyaGun.setVardiya(pdksVardiya);
				}
				vardiyaMap.put(key, pdksVardiyaGun);
				gunlukList = new ArrayList<Integer>();
				pdksVardiya.setGunlukList(gunlukList);
				for (int i = 0; i < aylikPuantaj.getSablonAylikPuantaj().getGunSayisi(); i++)
					gunlukList.add(0);

			} else
				gunlukList = vardiyaMap.get(key).getVardiya().getGunlukList();
			if (gunlukList != null && index >= 0 && gunlukList.size() > index) {
				int deger = gunlukList.get(index);
				gunlukList.set(index, ++deger);
			}

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			logger.info(index + " hata " + pdksVardiya.getId());
		}

	}

	/**
	 * @param aylik
	 * @throws Exception
	 */
	private void aylikVardiyaKontrolKaydet(Boolean aylik) throws Exception {
		boolean flush = Boolean.FALSE;
		String haftaTatilDurum = ortakIslemler.getParameterKey("haftaTatilDurum");
		haftaTatilMesaiDurum = haftaTatilDurum.equals("1");
		VardiyaPlan plan = personelAylikPuantaj.getVardiyaPlan();
		Personel personel = personelAylikPuantaj.getPdksPersonel();
		boolean pdGuncellendi = false;
		gorevli = helpPersonel(personel);
		if (plan.getVardiyaGunMap() == null)
			plan.setVardiyaGunMap(new TreeMap<String, VardiyaGun>());
		else
			plan.getVardiyaGunMap().clear();
		boolean durum = !vardiyalarMap.isEmpty();
		TreeMap<Long, VardiyaGun> mesaiMap = new TreeMap<Long, VardiyaGun>();
		for (VardiyaGun pdksVardiyaGun : personelAylikPuantaj.getVardiyalar()) {
			IsKurVardiyaGun isKurVardiya = pdksVardiyaGun.getIsKurVardiya();
			if (pdksVardiyaGun.isGuncellendi() || isKurVardiya.getId() == null) {
				try {
					if (isKurVardiya.getId() == null && isKurVardiya.getVardiya() != null)
						pdksEntityController.saveOrUpdate(session, entityManager, isKurVardiya);
					Long newVardiyaId = pdksVardiyaGun.getVardiya() != null ? pdksVardiyaGun.getVardiya().getId() : null;
					Long eskiVardiyaId = pdksVardiyaGun.getEskiVardiya() != null ? pdksVardiyaGun.getEskiVardiya().getId() : null;
					if (isKurVardiya.getId() != null && PdksUtil.isLongDegisti(newVardiyaId, eskiVardiyaId)) {
						isKurVardiya.setGuncelleyenUser(authenticatedUser);
						isKurVardiya.setGuncellemeTarihi(new Date());
						mesaiMap.put(isKurVardiya.getId(), pdksVardiyaGun);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				durum = Boolean.TRUE;
			}
			plan.getVardiyaGunMap().put(pdksVardiyaGun.getVardiyaDateStr(), pdksVardiyaGun);

		}

		if (durum) {

			for (VardiyaHafta pdksVardiyaHafta : plan.getVardiyaHaftaList())
				pdksVardiyaHafta.setCheckBoxDurum(Boolean.TRUE);
			if (!vardiyalarMap.isEmpty())
				durum = vardiyaPlanKontrol(null, plan, "", false);
			if (durum) {

				for (VardiyaGun pdksVardiyaGun : personelAylikPuantaj.getVardiyalar()) {
					if (pdksVardiyaGun.getVardiya() != null && pdksVardiyaGun.getIsKurVardiya() != null) {
						if (pdksVardiyaGun.isGuncellendi()) {
							IsKurVardiyaGun isKurVardiya = pdksVardiyaGun.getIsKurVardiya();
							try {
								if (isKurVardiya.getId() != null) {
									Long newVardiyaId = pdksVardiyaGun.getVardiya() != null ? pdksVardiyaGun.getVardiya().getId() : null;
									Long eskiVardiyaId = pdksVardiyaGun.getEskiVardiya() != null ? pdksVardiyaGun.getEskiVardiya().getId() : null;
									if (PdksUtil.isLongDegisti(newVardiyaId, eskiVardiyaId)) {
										isKurVardiya.setGuncelleyenUser(authenticatedUser);
										isKurVardiya.setGuncellemeTarihi(new Date());
									}
								}
							} catch (Exception e) {
							}

							if (isKurVardiya.getId() == null || mesaiMap.containsKey(isKurVardiya.getId())) {
								logger.debug(pdksVardiyaGun.getVardiyaDateStr() + " " + isKurVardiya.getVardiya().getAdi());
								pdksEntityController.saveOrUpdate(session, entityManager, isKurVardiya);
								flush = Boolean.TRUE;
							}

						}

					}
				}

			}
		}

		if (!flush)
			flush = pdGuncellendi;
		if (flush) {

			logger.debug("Veri tabanına kayıt ediliyor");

			try {
				session.flush();
				logger.debug("Plan kayıt edildi");
			} catch (InvalidStateException e) {
				flush = Boolean.FALSE;
				InvalidValue[] invalidValues = e.getInvalidValues();
				if (invalidValues != null) {
					for (InvalidValue invalidValue : invalidValues) {
						Object object = invalidValue.getBean();
						if (object != null && object instanceof VardiyaGun) {
							VardiyaGun pdksVardiyaGun = (VardiyaGun) object;
							PdksUtil.addMessageAvailableWarn(PdksUtil.convertToDateString(pdksVardiyaGun.getVardiyaDate(), PdksUtil.getDateFormat()) + " günü  alanı : " + invalidValue.getPropertyName() + " with message: " + invalidValue.getMessage());
						} else
							PdksUtil.addMessageAvailableWarn("Instance of bean class: " + invalidValue.getBeanClass().getSimpleName() + " has an invalid property: " + invalidValue.getPropertyName() + " with message: " + invalidValue.getMessage());
					}
				}
				logger.error(e);
				e.printStackTrace();

			} catch (Exception e) {
				PdksUtil.addMessageAvailableWarn(e.getMessage());
				flush = Boolean.FALSE;
				logger.error(e);
				e.printStackTrace();

			}

		}

		logger.debug("İşlem bitti.");
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private boolean getDenklestirmeDurum() {
		boolean denklestirme = true;
		if (denklestirmeAy != null)
			denklestirme = !denklestirmeAyDurum;
		return denklestirme;
	}

	/**
	 * @throws Exception
	 */
	private void fillDepartmanList() throws Exception {
		if (denklestirmeAy == null)
			setSeciliDenklestirmeAy();
		int donem = denklestirmeAy.getYil() * 100 + denklestirmeAy.getAy();
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT DISTINCT D.* FROM " + Personel.TABLE_NAME + " P    WITH(nolock)");
		sb.append(" INNER JOIN " + Sirket.TABLE_NAME + " S  ON  S." + Sirket.COLUMN_NAME_ID + "=P." + Personel.COLUMN_NAME_SIRKET);
		sb.append(" INNER JOIN " + Departman.TABLE_NAME + " D  ON  S." + Sirket.COLUMN_NAME_DEPARTMAN + "=D." + Departman.COLUMN_NAME_ID);
		sb.append(" WHERE P." + Personel.COLUMN_NAME_ISKUR_SABLON + " IS NOT NULL ");
		sb.append(" AND YEAR(" + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + ")*100+MONTH(" + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + ")<=:d1");
		sb.append(" AND YEAR(" + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ")*100+MONTH(" + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ")>=:d2");
		sb.append(" ORDER BY D." + Departman.COLUMN_NAME_ID);
		fields.put("d1", donem);
		fields.put("d2", donem);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Departman> list = pdksEntityController.getObjectBySQLList(sb, fields, Departman.class);
		List<SelectItem> departmanListe = new ArrayList<SelectItem>();
		for (Departman dp : list)
			departmanListe.add(new SelectItem(dp.getId(), dp.getAciklama()));

		if (departmanListe.size() > 0) {
			Long depId = null;
			if (aramaSecenekleri.getDepartmanId() == null || departmanListe.size() == 1)
				depId = (Long) departmanListe.get(0).getValue();
			if (depId != null) {
				HashMap parametreMap = new HashMap();
				parametreMap.put("id", depId);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				Departman departman = (Departman) pdksEntityController.getObjectByInnerObject(parametreMap, Departman.class);
				aramaSecenekleri.setDepartmanId(departman.getId());
				aramaSecenekleri.setDepartman(departman);
			}

		}

		aramaSecenekleri.setDepartmanIdList(departmanListe);
	}

	/**
	 * @param vardiyaPlan
	 * @param base
	 * @param vardiya
	 * @param personel
	 * @return
	 */
	public VardiyaGun vardiyaGunKopyala(VardiyaPlan vardiyaPlan, VardiyaGun base, Vardiya vardiya, Personel personel) {
		VardiyaGun vardiyaGun = (VardiyaGun) base.clone();
		vardiyaGun.setPersonel(personel);
		vardiyaGun.setVardiya(vardiya);
		vardiyaPlan.getVardiyaGunMap().put(vardiyaGun.getVardiyaDateStr(), vardiyaGun);
		return vardiyaGun;
	}

	/**
	 * @param vardiyaPlan
	 * @param baseVardiya
	 * @param personel
	 * @return
	 */
	public VardiyaHafta haftaKopyala(VardiyaPlan vardiyaPlan, VardiyaHafta baseVardiya, Personel personel) {
		VardiyaHafta dataHafta = new VardiyaHafta();
		VardiyaSablonu sablonu = personel.getSablon();
		dataHafta.setPersonel(personel);
		dataHafta.setVardiyaSablonu(sablonu);
		dataHafta.setVardiyaPlan(vardiyaPlan);
		dataHafta.setVardiyaSablonu(new VardiyaSablonu());
		dataHafta.setBasTarih(baseVardiya.getBasTarih());
		dataHafta.setBitTarih(baseVardiya.getBitTarih());
		dataHafta.setHafta(baseVardiya.getHafta());
		dataHafta.setVardiyaGun1(vardiyaGunKopyala(vardiyaPlan, baseVardiya.getVardiyaGun1(), sablonu.getVardiya1(), personel));
		dataHafta.setVardiyaGun2(vardiyaGunKopyala(vardiyaPlan, baseVardiya.getVardiyaGun2(), sablonu.getVardiya2(), personel));
		dataHafta.setVardiyaGun3(vardiyaGunKopyala(vardiyaPlan, baseVardiya.getVardiyaGun3(), sablonu.getVardiya3(), personel));
		dataHafta.setVardiyaGun4(vardiyaGunKopyala(vardiyaPlan, baseVardiya.getVardiyaGun4(), sablonu.getVardiya4(), personel));
		dataHafta.setVardiyaGun5(vardiyaGunKopyala(vardiyaPlan, baseVardiya.getVardiyaGun5(), sablonu.getVardiya5(), personel));
		dataHafta.setVardiyaGun6(vardiyaGunKopyala(vardiyaPlan, baseVardiya.getVardiyaGun6(), sablonu.getVardiya6(), personel));
		dataHafta.setVardiyaGun7(vardiyaGunKopyala(vardiyaPlan, baseVardiya.getVardiyaGun7(), sablonu.getVardiya7(), personel));
		return dataHafta;

	}

	/**
	 * @param personelId
	 * @param izinler
	 * @param list
	 */
	public void setIzin(long personelId, List<PersonelIzin> izinler, List<VardiyaGun> vardiyaList) {
		List<VardiyaGun> vList = new ArrayList<VardiyaGun>();
		for (VardiyaGun pGun : vardiyaList) {
			if (pGun.getPersonel().getId().longValue() == personelId) {
				pGun.setIzinler(null);
				pGun.setIzin(null);
				vList.add(pGun);
			}
		}
		List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
		for (Iterator<PersonelIzin> iterator = izinler.iterator(); iterator.hasNext();) {
			PersonelIzin personelIzin = (PersonelIzin) iterator.next();
			if (personelIzin.getIzinSahibi().getId().longValue() == personelId) {
				izinList.add(personelIzin);
			}
		}
		if (!izinList.isEmpty())
			ortakIslemler.vardiyaIzinleriGuncelle(izinList, vList);
		izinList = null;
		vList = null;
	}

	/**
	 * @return
	 */
	public String izinGirisi() {
		personelIzinGirisiHome.setIzinliSahibi(vardiyaPlan.getPersonel());
		personelIzinGirisiHome.setBasDate(basTarih);
		personelIzinGirisiHome.setBitDate(bitTarih);
		StringBuilder donusDeger = new StringBuilder(MenuItemConstant.personelIzinGirisi);
		donusDeger.append("?personelId=" + vardiyaPlan.getPersonel().getId());
		donusDeger.append("&bastarih=" + PdksUtil.convertToDateString(basTarih, "yyyyMMdd"));
		String str = donusDeger.toString();
		donusDeger = null;
		return str;
	}

	/**
	 * @param pdksVardiyaGun
	 * @return
	 */
	public String getClass(VardiyaGun pdksVardiyaGun) {
		String classAdi = "haftaBaslik";
		try {
			if (pdksVardiyaGun != null) {
				String dateStr = PdksUtil.convertToDateString(pdksVardiyaGun.getVardiyaDate(), "yyyyMMdd");
				Tatil tatil = !getTatilMap().containsKey(dateStr) ? null : getTatilMap().get(dateStr);
				if (tatil != null)
					classAdi = !tatil.isYarimGunMu() ? "bayram" : "arife";
				pdksVardiyaGun.setTatil(tatil);
			}

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			classAdi = "";
		}
		return classAdi;
	}

	/**
	 * 
	 */
	public void aylariDoldur() {
		int buYil = PdksUtil.getDateField(new Date(), Calendar.YEAR);
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("WITH PER_TARIH AS ( ");
		sb.append(" SELECT YEAR(" + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + ")*100+MONTH(" + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + ") AS D1,");
		sb.append(" YEAR(" + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ")*100+MONTH(" + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ") AS D2 FROM " + Personel.TABLE_NAME + " WITH(nolock)");
		sb.append(" WHERE " + Personel.COLUMN_NAME_ISKUR_SABLON + " IS NOT NULL )");
		sb.append(" select DISTINCT D.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
		sb.append(" INNER  JOIN PER_TARIH PD ON PD.D1<=(D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+" + DenklestirmeAy.COLUMN_NAME_AY + " AND PD.D2>=(D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+" + DenklestirmeAy.COLUMN_NAME_AY);
		if (buYil > yil) {
			sb.append(" INNER  JOIN " + IsKurVardiyaGun.TABLE_NAME + " V ON YEAR(V." + IsKurVardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ")= D." + DenklestirmeAy.COLUMN_NAME_YIL);
			sb.append(" AND  MONTH(V." + IsKurVardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ")= D." + DenklestirmeAy.COLUMN_NAME_AY);
		}

		sb.append(" WHERE D." + DenklestirmeAy.COLUMN_NAME_YIL + "=:y");
		if (yil == maxYil) {
			sb.append(" AND ((D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+" + DenklestirmeAy.COLUMN_NAME_AY + ")<=:s");
			fields.put("s", sonDonem);
		}
		fields.put("y", yil);
		sb.append(" ORDER BY D." + DenklestirmeAy.COLUMN_NAME_AY);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<DenklestirmeAy> list = pdksEntityController.getObjectBySQLList(sb, fields, DenklestirmeAy.class);
		if (aylar != null)
			aylar.clear();
		else
			aylar = new ArrayList<SelectItem>();
		int seciliAy = ay;
		ay = 0;
		for (DenklestirmeAy denklestirmeAy : list) {
			if (denklestirmeAy.getAy() == seciliAy)
				ay = seciliAy;
			aylar.add(new SelectItem(denklestirmeAy.getAy(), denklestirmeAy.getAyAdi()));
		}

	}

	/**
	 * @param yilParametre
	 * @return
	 * @throws Exception
	 */
	public String yilAyDegisti(boolean yilParametre) throws Exception {
		denklestirmeAy = null;
		if (yilParametre) {
			aylariDoldur();
			if (aylar != null && aylar.size() == 1)
				ay = (Integer) aylar.get(0).getValue();
		} else {
			Integer seciliAy = ay;
			ay = 0;
			for (SelectItem st : aylar) {
				if (st.getValue().equals(seciliAy))
					ay = seciliAy;
			}
		}
		setSeciliDenklestirmeAy();
		if (denklestirmeAy != null) {
			if (denklestirmeAy.getFazlaMesaiMaxSure() == null)
				fazlaMesaiOrtakIslemler.setFazlaMesaiMaxSure(denklestirmeAy, session);
			aramaSecenekleri.setTesisList(null);
			aramaSecenekleri.setGorevYeriList(null);
			Long sirketOnceki = aramaSecenekleri.getSirketId(), tesisIdOnceki = aramaSecenekleri.getTesisId(), bolumIdOnceki = aramaSecenekleri.getEkSaha3Id();
			fillSirketList();
			List<SelectItem> sirketIdList = aramaSecenekleri.getSirketIdList();
			if (sirketIdList != null && !sirketIdList.isEmpty()) {
				boolean bolumDoldurulmadi = true;
				if (sirketOnceki != null)
					aramaSecenekleri.setSirketId(null);
				if (sirketIdList.size() == 1)
					aramaSecenekleri.setSirketId((Long) sirketIdList.get(0).getValue());
				else if (sirketOnceki != null) {
					for (SelectItem selectItem : sirketIdList) {
						if (selectItem.getValue().equals(sirketOnceki))
							aramaSecenekleri.setSirketId(sirketOnceki);
					}
				}
				if (aramaSecenekleri.getSirketId() != null) {
					try {
						tesisDoldur(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
					List<SelectItem> tesisList = aramaSecenekleri.getTesisList();
					if (tesisIdOnceki != null)
						aramaSecenekleri.setTesisId(null);
					if (tesisList != null && !tesisList.isEmpty()) {
						if (tesisList.size() == 1) {
							aramaSecenekleri.setTesisId((Long) tesisList.get(0).getValue());

						} else if (tesisIdOnceki != null) {
							for (SelectItem selectItem : tesisList) {
								if (selectItem.getValue().equals(tesisIdOnceki))
									aramaSecenekleri.setTesisId(tesisIdOnceki);
							}
						}
						bolumDoldur();
						bolumDoldurulmadi = false;
					}
					if (bolumIdOnceki != null)
						aramaSecenekleri.setEkSaha3Id(null);
					if (bolumDoldurulmadi)
						if (aramaSecenekleri.getTesisId() != null || bolumIdOnceki != null) {
							bolumDoldur();
						}
					List<SelectItem> bolumList = aramaSecenekleri.getGorevYeriList();
					if (bolumList != null && bolumIdOnceki != null) {
						for (SelectItem selectItem : bolumList) {
							if (selectItem.getValue().equals(bolumIdOnceki))
								aramaSecenekleri.setEkSaha3Id(bolumIdOnceki);
						}
					}

				}
			}
		}

		return "";
	}

	/**
	 * 
	 */
	private void setSeciliDenklestirmeAy() {
		if (aylikPuantajList != null)
			aylikPuantajList.clear();

		HashMap fields = new HashMap();
		fields.put("ay", ay);
		fields.put("yil", yil);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
		if (denklestirmeAy == null) {
			if (ay > 0) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.MONTH, ay - 1);
				cal.set(Calendar.YEAR, yil);
				cal.set(Calendar.DAY_OF_MONTH, 1);
				String ayAdi = PdksUtil.convertToDateString(cal.getTime(), "MMMM");
				if (fazlaMesaiTalepDurum == false)
					PdksUtil.addMessageAvailableError(yil + " " + ayAdi + " döneme ait çalışma planı tanımlanmamıştır!");
				else
					PdksUtil.addMessageAvailableError(yil + " " + ayAdi + " döneme ait fazla mesai talep tanımlanmamıştır!");
			}
		}
		setDenklestirmeAyDurum(fazlaMesaiOrtakIslemler.getDurum(denklestirmeAy));
	}

	/**
	 * @param tesisDoldur
	 */
	public void fillSirketDoldur(boolean tesisDoldur) {
		if (aylikPuantajList != null) {
			aylikPuantajList.clear();

		}
		if (tesisDoldur) {
			if (denklestirmeAy == null || denklestirmeAy.getAy() != ay || denklestirmeAy.getYil() != yil) {
				if (denklestirmeAy == null || denklestirmeAy.getYil() != yil) {
					aylariDoldur();
					if (aylar != null && aylar.size() == 1) {
						ay = (Integer) aylar.get(0).getValue();
					}
				}

			}

		}
		aramaSecenekleri.setGorevYeriList(null);
		aramaSecenekleri.setTesisList(null);
		listeleriTemizle();
		setSeciliDenklestirmeAy();
		List<SelectItem> sirketler = null;
		Sirket sirket = null;
		if (denklestirmeAy != null) {
			if (aramaSecenekleri.getDepartmanId() != null) {
				HashMap parametreMap = new HashMap();
				parametreMap.put("id", aramaSecenekleri.getDepartmanId());
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				departman = (Departman) pdksEntityController.getObjectByInnerObject(parametreMap, Departman.class);
				sirketler = fazlaMesaiOrtakIslemler.getFazlaMesaiMudurSirketList(adminRole ? aramaSecenekleri.getDepartmanId() : null, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, getDenklestirmeDurum(), fazlaMesaiTalepDurum, session);

			}
			if (sirketler == null)
				sirketler = new ArrayList<SelectItem>();

			if (!sirketler.isEmpty()) {
				Long onceki = aramaSecenekleri.getSirketId();
				if (sirketler.size() == 1) {
					aramaSecenekleri.setSirketId((Long) sirketler.get(0).getValue());
				} else if (onceki != null) {
					for (SelectItem st : sirketler) {
						if (st.getValue().equals(onceki))
							aramaSecenekleri.setSirketId(onceki);
					}
				}
				if (aramaSecenekleri.getSirketId() != null) {
					HashMap map = new HashMap();
					map.put("id", aramaSecenekleri.getSirketId());
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					sirket = (Sirket) pdksEntityController.getObjectByInnerObject(map, Sirket.class);
					if (tesisDoldur)
						fillTesisDoldur(tesisDoldur);
				}

			}
		}
		aramaSecenekleri.setSirket(sirket);
		aramaSecenekleri.setSirketIdList(sirketler);

	}

	/**
	 * @param bolumDoldur
	 */
	public void fillTesisDoldur(boolean bolumDoldur) {
		departman = aramaSecenekleri.getSirket() != null ? aramaSecenekleri.getSirket().getDepartman() : authenticatedUser.getDepartman();
		listeleriTemizle();
		aramaSecenekleri.setGorevYeriList(null);
		HashMap map = new HashMap();
		map.put("id", aramaSecenekleri.getSirketId());
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		Sirket sirket = (Sirket) pdksEntityController.getObjectByInnerObject(map, Sirket.class);
		aramaSecenekleri.setSirket(sirket);
		List<SelectItem> list = fazlaMesaiOrtakIslemler.getFazlaMesaiMudurTesisList(aramaSecenekleri.getSirket(), denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, getDenklestirmeDurum(), fazlaMesaiTalepDurum, session);
		if (list != null && !list.isEmpty()) {
			Long oncekiId = aramaSecenekleri.getTesisId();
			if (list.size() == 1)
				aramaSecenekleri.setTesisId((Long) list.get(0).getValue());
			else if (oncekiId != null) {
				for (SelectItem st : list) {
					if (st.getValue().equals(oncekiId))
						aramaSecenekleri.setTesisId(oncekiId);
				}
			}
		}
		aramaSecenekleri.setTesisList(list);
		if (bolumDoldur) {
			if (aramaSecenekleri.getTesisId() != null)
				fillBolumDoldur();
		}

	}

	/**
	 * 
	 */
	public void fillBolumDoldur() {
		Sirket sirket = aramaSecenekleri.getSirket();
		List<SelectItem> list = null;
		String tesisId = null;
		if (departman.isAdminMi() && sirket.isTesisDurumu())
			tesisId = aramaSecenekleri.getTesisId() != null ? String.valueOf(aramaSecenekleri.getTesisId()) : null;

		list = fazlaMesaiOrtakIslemler.getFazlaMesaiMudurBolumList(sirket, tesisId, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, getDenklestirmeDurum(), fazlaMesaiTalepDurum, session);
		if (list != null && !list.isEmpty()) {
			Long oncekiId = aramaSecenekleri.getEkSaha3Id();
			if (list.size() == 1)
				aramaSecenekleri.setEkSaha3Id((Long) list.get(0).getValue());
			else if (oncekiId != null) {
				for (SelectItem st : list) {
					if (st.getValue().equals(oncekiId))
						aramaSecenekleri.setEkSaha3Id(oncekiId);
				}
			}
		}
		aramaSecenekleri.setGorevYeriList(list);
	}

	/**
	 * @throws Exception
	 */
	public void fillSirketList() throws Exception {
		if (adminRole)
			fillDepartmanList();
		listeleriTemizle();
		setDepartman(null);
		aramaSecenekleri.setGorevYeriList(null);
		if (aramaSecenekleri.getDepartmanId() != null) {
			int donem = denklestirmeAy.getYil() * 100 + denklestirmeAy.getAy();
			HashMap fields = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append(" SELECT DISTINCT S.* FROM " + Personel.TABLE_NAME + " P    WITH(nolock)");
			sb.append(" INNER JOIN " + Sirket.TABLE_NAME + " S  ON  S." + Sirket.COLUMN_NAME_ID + "=P." + Personel.COLUMN_NAME_SIRKET);
			if (aramaSecenekleri.getDepartmanId() != null) {
				sb.append(" AND  S." + Sirket.COLUMN_NAME_DEPARTMAN + "=:d");
				fields.put("d", aramaSecenekleri.getDepartmanId());
			}
			sb.append(" WHERE P." + Personel.COLUMN_NAME_ISKUR_SABLON + " IS NOT NULL ");
			sb.append(" AND YEAR(" + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + ")*100+MONTH(" + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + ")<=:d1");
			sb.append(" AND YEAR(" + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ")*100+MONTH(" + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ")>=:d2");
			fields.put("d1", donem);
			fields.put("d2", donem);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Sirket> list = pdksEntityController.getObjectBySQLList(sb, fields, Sirket.class);
			if (!list.isEmpty())
				list = PdksUtil.sortObjectStringAlanList(list, "getAd", null);
			List<SelectItem> sirketler = new ArrayList<SelectItem>();
			for (Sirket s : list) {
				sirketler.add(new SelectItem(s.getId(), s.getAd()));
			}
			Sirket sirket = null;
			if (!sirketler.isEmpty()) {
				Long onceki = aramaSecenekleri.getSirketId();
				if (sirketler.size() == 1) {
					aramaSecenekleri.setSirketId((Long) sirketler.get(0).getValue());
				} else if (onceki != null) {
					for (SelectItem st : sirketler) {
						if (st.getValue().equals(onceki))
							aramaSecenekleri.setSirketId(onceki);
					}
				}
				if (aramaSecenekleri.getSirketId() != null) {
					HashMap map = new HashMap();
					map.put("id", aramaSecenekleri.getSirketId());
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					sirket = (Sirket) pdksEntityController.getObjectByInnerObject(map, Sirket.class);

				}

				aramaSecenekleri.setSirketIdList(sirketler);

			}
			aramaSecenekleri.setSirket(sirket);
		}

		gorevTipiId = null;
	}

	/**
	 * 
	 */
	private void listeleriTemizle() {
		aylikPuantajList.clear();

	}

	/**
	 * @return
	 */
	public String gunlerSecildi() {
		for (VardiyaGun pdksVardiyaGun : aylikPuantajDefault.getVardiyalar())
			pdksVardiyaGun.setCheckBoxDurum(gunSec);
		return "";

	}

	/**
	 * @param aylikPuantaj
	 */
	private void planYetkilendir(AylikPuantaj aylikPuantaj) {
		if (aylikPuantaj != null) {
			Personel personel = aylikPuantaj.getPdksPersonel();
			gorevli = helpPersonel(personel);

			if (!aylikPuantaj.isGorevYeriSec() && aramaSecenekleri.getEkSaha3Id() != null && aramaSecenekleri.getEkSaha3Id() > 0) {
				HashMap map1 = new HashMap();
				map1.put("durum", Boolean.TRUE);
				map1.put("bolum.id", aramaSecenekleri.getEkSaha3Id());
				if (session != null)
					map1.put(PdksEntityController.MAP_KEY_SESSION, session);
				bolumKatlari = pdksEntityController.getObjectByInnerObjectList(map1, BolumKat.class);
				if (bolumKatlari.size() > 1)
					bolumKatlari = PdksUtil.sortObjectStringAlanList(bolumKatlari, "getAciklama", null);
				else
					bolumKatlari.clear();
				map1 = null;

			} else
				bolumKatlari = new ArrayList<BolumKat>();
			AylikPuantaj aylikPuantajToplam = new AylikPuantaj();

			puantajYetkilendir(null, aylikPuantaj, aylikPuantajToplam, null);
		}

	}

	/**
	 * @param aylikPuantaj
	 * @return
	 */
	private void vardiyalarSec(AylikPuantaj aylikPuantaj) {
		if (aylikPuantaj != null) {
			ArrayList<Vardiya> vardiyalar = fillAylikVardiyaList(aylikPuantaj, null);
			for (VardiyaGun pdksVardiyaGun : aylikPuantaj.getVardiyalar()) {
				if (pdksVardiyaGun.getVardiya() != null) {
					ArrayList<Vardiya> list = pdksVardiyaGun.getIzin() == null ? vardiyalar : null;
					setVardiyaGunleri(list, pdksVardiyaGun);
					pdksVardiyaGun.setIslemVardiya(null);
					pdksVardiyaGun.setIslendi(false);
				}
			}
		}
	}

	/**
	 * @return
	 */
	public String onayDurumDegisti() {
		for (Iterator iter = aylikPuantajList.iterator(); iter.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();
			aylikPuantaj.setOnayDurum(onayDurum);
		}
		return "";

	}

	/**
	 * @param tempList
	 * @return
	 */
	private TreeMap<Long, TreeMap<Long, Vardiya>> getCalismaModeliMap(List<AylikPuantaj> tempList) {
		TreeMap<Long, TreeMap<Long, Vardiya>> calismaModeliMap = new TreeMap<Long, TreeMap<Long, Vardiya>>();
		if (ortakIslemler.getParameterKey("vardiyaCalismaModeliKontrol").equals("1")) {
			List<Long> cmIdList = new ArrayList<Long>();
			for (Iterator iterator = tempList.iterator(); iterator.hasNext();) {
				AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
				if (aylikPuantaj.getCalismaModeli() != null) {
					Long id = aylikPuantaj.getCalismaModeli().getId();
					if (id != null && !cmIdList.contains(id))
						cmIdList.add(id);
				}

			}
			if (!cmIdList.isEmpty()) {
				HashMap parametreMap = new HashMap();
				parametreMap.put("calismaModeli.id", cmIdList);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<CalismaModeliVardiya> list = pdksEntityController.getObjectByInnerObjectList(parametreMap, CalismaModeliVardiya.class);
				for (CalismaModeliVardiya calismaModeliVardiya : list) {
					CalismaModeli calismaModeli = calismaModeliVardiya.getCalismaModeli();
					Vardiya vardiya = calismaModeliVardiya.getVardiya();
					if (vardiya.getDurum()) {
						TreeMap<Long, Vardiya> map1 = calismaModeliMap.containsKey(calismaModeli.getId()) ? calismaModeliMap.get(calismaModeli.getId()) : new TreeMap<Long, Vardiya>();
						if (map1.isEmpty())
							calismaModeliMap.put(calismaModeli.getId(), map1);
						map1.put(vardiya.getId(), vardiya);
					}
				}

			}
		}
		return calismaModeliMap;
	}

	/**
	 * @return
	 */
	public String bosIslem() {
		return "";
	}

	/**
	 * 
	 */
	private void aylikPuantajOlusturuluyor() {
		saveLastParameter();
		offIzinGuncelle = ortakIslemler.getParameterKey("offIzinGuncelle").equals("1");
		User user = ortakIslemler.getSistemAdminUser(session);
		if (user == null)
			user = authenticatedUser;

		manuelVardiyaIzinGir = ortakIslemler.getVardiyaIzinGir(session, authenticatedUser.getDepartman());
		gorevYeriGirisDurum = ortakIslemler.getParameterKey("uygulamaTipi").equals("H") && ortakIslemler.getParameterKey("gorevYeriGiris").equals("1");
		departmanBolumAyni = Boolean.FALSE;
		modelGoster = Boolean.FALSE;
		sanalPersonelAciklama = ortakIslemler.sanalPersonelAciklama();
		String aksamBordroBasZamani = ortakIslemler.getParameterKey("aksamBordroBasZamani"), aksamBordroBitZamani = ortakIslemler.getParameterKey("aksamBordroBitZamani");
		Integer[] basZaman = ortakIslemler.getSaatDakika(aksamBordroBasZamani), bitZaman = ortakIslemler.getSaatDakika(aksamBordroBitZamani);
		aksamVardiyaBasSaat = basZaman[0];
		aksamVardiyaBasDakika = basZaman[1];
		aksamVardiyaBitSaat = bitZaman[0];
		aksamVardiyaBitDakika = bitZaman[1];
		ozelIstek = Boolean.FALSE;
		onayDurum = Boolean.FALSE;
		session.clear();
		aylikPuantajList.clear();
		List<VardiyaGun> sablonVardiyalar = new ArrayList<VardiyaGun>();
		gunSec = Boolean.FALSE;
		DepartmanDenklestirmeDonemi denklestirmeDonemi = new DepartmanDenklestirmeDonemi(), denklestirmeDonemiGecenAy = new DepartmanDenklestirmeDonemi();
		HashMap fields = new HashMap();
		sicilNo = ortakIslemler.getSicilNo(sicilNo);
		if (aramaSecenekleri.getSirketId() != null) {
			fields.clear();
			fields.put("id", aramaSecenekleri.getSirketId());
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			Sirket sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);
			if (sirket != null) {
				departmanBolumAyni = sirket.isTesisDurumu() == false;
			}

		}

		fields.clear();
		int gecenAy = ay - 1;
		int gecenYil = yil;
		if (gecenAy == 0) {
			gecenAy = 12;
			gecenYil = yil - 1;
		}
		fields.put("ay", gecenAy);
		fields.put("yil", gecenYil);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		sutIzniGoster = Boolean.FALSE;
		gebeGoster = Boolean.FALSE;
		partTimeGoster = Boolean.FALSE;
		DenklestirmeAy denklestirmeGecenAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
		if (denklestirmeGecenAy == null) {
			denklestirmeGecenAy = new DenklestirmeAy();
			denklestirmeGecenAy.setOlusturanUser(user);
			denklestirmeGecenAy.setAy(gecenAy);
			denklestirmeGecenAy.setYil(gecenYil);
			pdksEntityController.saveOrUpdate(session, entityManager, denklestirmeGecenAy);
			session.flush();

		}
		fields.clear();
		int gelecekAy = ay + 1;
		int gelecekAyYil = yil;
		if (gelecekAy > 12) {
			gelecekAy = 1;
			gelecekAyYil = yil + 1;
		}
		fields.put("ay", gelecekAy);
		fields.put("yil", gelecekAyYil);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		DenklestirmeAy denklestirmeGelecekAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
		if (denklestirmeGelecekAy == null) {
			denklestirmeGelecekAy = new DenklestirmeAy();
			denklestirmeGelecekAy.setOlusturanUser(user);
			denklestirmeGelecekAy.setAy(gelecekAy);
			denklestirmeGelecekAy.setYil(gelecekAyYil);
			pdksEntityController.saveOrUpdate(session, entityManager, denklestirmeGelecekAy);
			session.flush();
		}

		fields.clear();
		fields.put("ay", ay);
		fields.put("yil", yil);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
		if (denklestirmeAy == null) {
			denklestirmeAy = new DenklestirmeAy();
			denklestirmeAy.setOlusturanUser(user);
			denklestirmeAy.setAy(ay);
			denklestirmeAy.setYil(yil);
			denklestirmeAy.setFazlaMesaiMaxSure(ortakIslemler.getFazlaMesaiMaxSure(null));
			pdksEntityController.saveOrUpdate(session, entityManager, denklestirmeAy);
			session.flush();
		} else if (denklestirmeAy.getFazlaMesaiMaxSure() == null)
			fazlaMesaiOrtakIslemler.setFazlaMesaiMaxSure(denklestirmeAy, session);
		setDenklestirmeAyDurum(fazlaMesaiOrtakIslemler.getDurum(denklestirmeAy));
		fields.clear();

		fields.put("denklestirmeAy.id", denklestirmeAy.getId());
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		modelList = pdksEntityController.getObjectByInnerObjectList(fields, CalismaModeliAy.class);
		Departman dep = aramaSecenekleri.getSirket() != null ? aramaSecenekleri.getSirket().getDepartman() : null;
		LinkedHashMap<Long, CalismaModeliAy> modelMap = new LinkedHashMap<Long, CalismaModeliAy>();
		for (Iterator iterator = modelList.iterator(); iterator.hasNext();) {
			CalismaModeliAy cm = (CalismaModeliAy) iterator.next();
			if (dep != null && cm.getCalismaModeli().getDepartman() != null && !dep.getId().equals(cm.getCalismaModeli().getDepartman().getId()))
				iterator.remove();
			else
				modelMap.put(cm.getCalismaModeli().getId(), cm);
		}

		denklestirmeAy.setModeller(modelList);
		fields.clear();

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, ay - 1);
		cal.set(Calendar.YEAR, yil);
		boolean devam = true;
		List<VardiyaHafta> vardiyaHaftaList = null;
		AylikPuantaj aylikPuantajSablon = fazlaMesaiOrtakIslemler.getAylikPuantaj(ay, yil, denklestirmeDonemi, session);
		tatilGunleriMap = null;
		boolean veriGuncelle = false;
		for (Iterator iterator = denklestirmeAy.getModeller().iterator(); iterator.hasNext();) {
			CalismaModeliAy calismaModeliAy = (CalismaModeliAy) iterator.next();
			CalismaModeli cm = calismaModeliAy.getCalismaModeli();
			if (calismaModeliAy.getSure() == 0.0d || calismaModeliAy.getToplamIzinSure() == 0.0d || ((denklestirmeAy.getSure() == 0.0d || denklestirmeAy.getToplamIzinSure() == 0.0d) && cm.getHaftaIci() == 9.0d)) {
				double sure = 0.0d, toplamIzinSure = 0.0d;
				for (VardiyaGun vg : aylikPuantajSablon.getVardiyalar()) {
					if (vg.isAyinGunu()) {
						cal.setTime(vg.getVardiyaDate());
						int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
						if (dayOfWeek != Calendar.SUNDAY) {
							if (vg.getTatil() == null) {
								sure += dayOfWeek != Calendar.SATURDAY ? cm.getHaftaIci() : cm.getHaftaSonu();
								toplamIzinSure += dayOfWeek != Calendar.SATURDAY ? 7.5d : 0;
							} else if (vg.getTatil().isYarimGunMu()) {
								if (PdksUtil.tarihKarsilastirNumeric(vg.getVardiyaDate(), vg.getTatil().getBasTarih()) == 0) {
									if (cm.getHaftaSonu() > 0 || dayOfWeek != Calendar.SATURDAY)
										sure += cm.getArife();
									toplamIzinSure += cm.getArife();
								}

							}
						}
					}
				}
				if (cm.getHaftaIci() == 9.0d && (denklestirmeAy.getSure() == 0.0d || denklestirmeAy.getToplamIzinSure() == 0.0d)) {
					if (denklestirmeAy.getSure() == 0.0d)
						denklestirmeAy.setSure(sure);
					if (denklestirmeAy.getToplamIzinSure() == 0.0d)
						denklestirmeAy.setToplamIzinSure(toplamIzinSure);

					pdksEntityController.saveOrUpdate(session, entityManager, denklestirmeAy);
				}

				if (calismaModeliAy.getSure() == 0.0d || calismaModeliAy.getToplamIzinSure() == 0.0d) {
					if (calismaModeliAy.getSure() == 0.0d)
						calismaModeliAy.setSure(sure);
					if (calismaModeliAy.getToplamIzinSure() == 0.0d)
						calismaModeliAy.setToplamIzinSure(toplamIzinSure);
					pdksEntityController.saveOrUpdate(session, entityManager, calismaModeliAy);
				}

				veriGuncelle = true;
			}
		}
		if (veriGuncelle)
			session.flush();
		if (sicilNo != null)
			sicilNo = sicilNo.trim();

		aylikPuantajSablon.setFazlaMesaiHesapla(Boolean.TRUE);
		AylikPuantaj gecenAylikPuantajSablon = null;
		if (ay != 1)
			gecenAylikPuantajSablon = fazlaMesaiOrtakIslemler.getAylikPuantaj(ay - 1, yil, denklestirmeDonemiGecenAy, session);
		else
			gecenAylikPuantajSablon = fazlaMesaiOrtakIslemler.getAylikPuantaj(12, yil - 1, denklestirmeDonemiGecenAy, session);
		basTarih = denklestirmeDonemi.getBaslangicTarih();
		bitTarih = denklestirmeDonemi.getBitisTarih();

		devam = true;
		User islemYapan = (User) authenticatedUser.clone();

		List<String> perList = null;
		fields.clear();
		bolumKatlari = null;

		gorevYerileri = new ArrayList<Long>();
		if (gorevliPersonelMap != null)
			gorevliPersonelMap.clear();
		else
			gorevliPersonelMap = new HashMap<String, Personel>();

		perList = new ArrayList<String>();
		int donem = denklestirmeAy.getYil() * 100 + denklestirmeAy.getAy();
		fields.clear();
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT DISTINCT P.* FROM " + Personel.TABLE_NAME + " P    WITH(nolock)");
		sb.append(" WHERE P." + Personel.COLUMN_NAME_ISKUR_SABLON + " IS NOT NULL ");
		if (aramaSecenekleri.getSirket() != null) {
			sb.append(" AND  P." + Personel.COLUMN_NAME_SIRKET + "=:s");
			fields.put("s", aramaSecenekleri.getSirket().getId());
		}
		sb.append(" AND YEAR(" + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + ")*100+MONTH(" + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + ")<=:d1");
		sb.append(" AND YEAR(" + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ")*100+MONTH(" + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ")>=:d2");
		fields.put("d1", donem);
		fields.put("d2", donem);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Personel> personelList = pdksEntityController.getObjectBySQLList(sb, fields, Personel.class);

		sicilNo = ortakIslemler.getSicilNo(sicilNo);
		for (Personel personel : personelList) {
			if (sicilNo.equals("") || sicilNo.trim().equals(personel.getPdksSicilNo().trim()))
				perList.add(personel.getPdksSicilNo().trim());
		}

		if (!authenticatedUser.isDirektorSuperVisor() && !authenticatedUser.isIK() && (authenticatedUser.isYonetici() || authenticatedUser.isYoneticiKontratli())) {
			ortakIslemler.sistemeGirisIslemleri(islemYapan, Boolean.TRUE, basTarih, bitTarih, session);

		}
		if (adminRole || islemYapan.getYetkiTumPersonelNoList().contains(sicilNo))
			if (PdksUtil.hasStringValue(sicilNo))
				perList.add(sicilNo);

		ArrayList<Long> perIdler = new ArrayList<Long>();
		if (!perList.isEmpty()) {
			fields.clear();
			fields.put("pdksSicilNo", perList);

			fields.put("iseBaslamaTarihi<=", bitTarih);

			fields.put("sskCikisTarihi>=", basTarih);
			fields.put("isKurVardiyaSablonu<>", null);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {

				personelList = (ArrayList<Personel>) pdksEntityController.getObjectByInnerObjectListInLogic(fields, Personel.class);

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());

				personelList = null;
			}

		} else {
			if (fazlaMesaiMap == null)
				fazlaMesaiMap = new TreeMap<String, Tanim>();
			else
				fazlaMesaiMap.clear();
		}
		boolean flush = false;
		if (personelList != null && !personelList.isEmpty()) {

			for (Personel personel : personelList)
				perIdler.add(personel.getId());

			fields.clear();
			fields.put("vardiyaTipi", Vardiya.TIPI_OFF);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			Vardiya offVardiya = (Vardiya) pdksEntityController.getObjectByInnerObject(fields, Vardiya.class);

			TreeMap<Long, PersonelDenklestirme> denklestirmeMap = getPersonelDenklestirme(denklestirmeAy, perIdler);
			flush = denklestirmeMap.isEmpty();

			TreeMap<String, VardiyaHafta> vardiyaHaftaMap = getVardiyaHaftaMap(perIdler);
			if (!flush)
				flush = vardiyaHaftaMap.isEmpty();

			fields.clear();
			fields.put("bitisZamani>=", ortakIslemler.tariheGunEkleCikar(cal, denklestirmeDonemiGecenAy.getBaslangicTarih(), -2));
			fields.put("baslangicZamani<=", ortakIslemler.tariheGunEkleCikar(cal, bitTarih, 1));
			fields.put("izinSahibi.id", perIdler.clone());
			fields.put("izinDurumu", ortakIslemler.getAktifIzinDurumList());
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelIzin> izinler = pdksEntityController.getObjectByInnerObjectListInLogic(fields, PersonelIzin.class);

			List<VardiyaGun> vardiyaGunList = ortakIslemler.getIskurVardiyalar(perIdler, ortakIslemler.tariheGunEkleCikar(cal, basTarih, -7), ortakIslemler.tariheGunEkleCikar(cal, bitTarih, 7), session);

			List<Long> idList = null;
			if ((authenticatedUser.isYonetici() || authenticatedUser.isYoneticiKontratli()) && !authenticatedUser.isDirektorSuperVisor()) {
				idList = new ArrayList<Long>();
				if (aramaSecenekleri.getGorevYeriList() != null) {
					for (SelectItem selectItem : aramaSecenekleri.getGorevYeriList())
						idList.add((Long) selectItem.getValue());

				}
				if (departmanBolumAyni == false && aramaSecenekleri.getTesisId() != null && aramaSecenekleri.getTesisId() > 0)
					idList.add(aramaSecenekleri.getTesisId());
				if (aramaSecenekleri.getEkSaha3Id() != null && !idList.contains(aramaSecenekleri.getEkSaha3Id()))
					idList.add(aramaSecenekleri.getEkSaha3Id());
			}
			for (VardiyaGun pdksVardiyaGun : vardiyaGunList)

				setVardiyalarToMap(pdksVardiyaGun, vardiyalarMap, null);

			ortakIslemler.fazlaMesaiSaatiAyarla(vardiyalarMap);
			vardiyaGunList = null;

			//
			TreeMap<String, Personel> perMap = new TreeMap<String, Personel>();

			List<Long> kontratliPerIdList = new ArrayList<Long>();
			for (Personel personel : personelList) {
				Tanim grup = personel.getPlanGrup2();
				String key = (personel.getPdksYonetici() != null ? personel.getPdksYonetici().getPdksSicilNo() + "_" : "");
				key += (personel.getEkSaha3() != null ? personel.getEkSaha3().getId().toString() : "");
				key += "_" + (grup != null ? grup.getId().toString() : "");
				key += "_" + personel.getPdksSicilNo() + "_" + personel.getId();
				perMap.put(key, personel);
			}

			personelList = null;
			personelList = new ArrayList<Personel>(perMap.values());

			if (!personelList.isEmpty()) {
				TreeMap<Long, Long> sirketIdMap = new TreeMap<Long, Long>(), sirketMap = new TreeMap<Long, Long>();
				kontratliPerIdList.clear();
				for (Personel personel : personelList) {
					kontratliPerIdList.add(personel.getId());
					if (personel.getSirket() != null)
						sirketMap.put(personel.getSirket().getId(), personel.getId());
					if (personel.getPdksYonetici() != null)
						sirketIdMap.put(personel.getPdksYonetici().getId(), personel.getId());
				}
				departmanBolumAyni = sirketMap.size() > 1;
				sirketMap = null;
				if (kontratliPerIdList.size() > 40)
					personelList = ortakIslemler.getKontratliSiraliPersonel(kontratliPerIdList, session);
				else
					personelList = (ArrayList<Personel>) PdksUtil.sortObjectStringAlanList(personelList, "getKontratliSortKey", null);
				if (sirketIdMap.size() > 0 && sirketIdMap.size() < personelList.size()) {
					List<Personel> perDigerList = new ArrayList<Personel>();
					for (Iterator iterator = personelList.iterator(); iterator.hasNext();) {
						Personel personel = (Personel) iterator.next();
						if (!sirketIdMap.containsKey(personel.getId())) {
							perDigerList.add(personel);
							iterator.remove();
						}
					}
					if (!perDigerList.isEmpty())
						personelList.addAll(perDigerList);
					perDigerList = null;
				}
				sirketIdMap = null;
			}
			kontratliPerIdList = null;
			perMap = null;
			tatilGunleriMap = ortakIslemler.getTatilGunleri(personelList, denklestirmeDonemiGecenAy.getBaslangicTarih(), bitTarih, session);

			fazlaMesaiOrtakIslemler.haftalikVardiyaOlustur(vardiyaHaftaList, gecenAylikPuantajSablon, denklestirmeDonemiGecenAy, tatilGunleriMap, null);

			vardiyaHaftaList = new ArrayList<VardiyaHafta>();
			VardiyaPlan pdksVardiyaPlanMaster = fazlaMesaiOrtakIslemler.haftalikVardiyaOlustur(vardiyaHaftaList, aylikPuantajSablon, denklestirmeDonemi, tatilGunleriMap, sablonVardiyalar);

			this.setVardiyaPlan(pdksVardiyaPlanMaster);
			if (gorevliPersonelMap != null)
				gorevliPersonelMap.clear();
			else
				gorevliPersonelMap = new HashMap<String, Personel>();
			for (Personel personel : personelList) {

				boolean vardiyaCalisiyor = Boolean.FALSE;
				long iseBasTarih = Long.parseLong(PdksUtil.convertToDateString(personel.getIseGirisTarihi(), "yyyyMMdd"));
				long istenAyrilmaTarih = Long.parseLong(PdksUtil.convertToDateString(personel.getSonCalismaTarihi(), "yyyyMMdd"));
				VardiyaSablonu sablonu = personel.getIsKurVardiyaSablonu();
				AylikPuantaj aylikPuantaj = new AylikPuantaj(), gecenAylikPuantaj = new AylikPuantaj();
				aylikPuantaj.setGecenAylikPuantaj(gecenAylikPuantaj);
				gecenAylikPuantaj.setPdksPersonel(personel);

				aylikPuantaj.setPdksPersonel(personel);
				aylikPuantaj.setVardiyaPlan(new VardiyaPlan(personel));
				aylikPuantaj.getVardiyaPlan().getVardiyaHaftaList().clear();
				List<VardiyaGun> puantajVardiyaGunler = new ArrayList<VardiyaGun>();
				aylikPuantaj.setVardiyalar(puantajVardiyaGunler);
				aylikPuantaj.setTrClass(devam ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
				devam = !devam;
				List<VardiyaGun> vardiyaGunler = new ArrayList<VardiyaGun>();

				Date sonCalismaTarihi = personel.getSonCalismaTarihi(), iseBaslamaTarihi = personel.getIseGirisTarihi();
				boolean personelCalmayaBasladi = false, calisiyor = false;
				for (Iterator iterator2 = sablonVardiyalar.iterator(); iterator2.hasNext();) {
					VardiyaGun pdksVardiyaGunMaster = (VardiyaGun) iterator2.next();
					VardiyaGun pdksVardiyaGun = new VardiyaGun(personel, null, pdksVardiyaGunMaster.getVardiyaDate());

					if (!personelCalmayaBasladi)
						personelCalmayaBasladi = pdksVardiyaGunMaster.getVardiyaDate().getTime() >= iseBaslamaTarihi.getTime();
					if (vardiyalarMap.containsKey(pdksVardiyaGun.getVardiyaKey()))
						pdksVardiyaGun = vardiyalarMap.get(pdksVardiyaGun.getVardiyaKey());
					if (pdksVardiyaGun.getIsKurVardiya() == null) {
						IsKurVardiyaGun isKurVardiya = new IsKurVardiyaGun(personel, null, pdksVardiyaGunMaster.getVardiyaDate());
						pdksVardiyaGun.setIsKurVardiya(isKurVardiya);
						isKurVardiya.setOlusturmaTarihi(new Date());
						isKurVardiya.setOlusturanUser(authenticatedUser);
					}
					pdksVardiyaGun.setTdClass(aylikPuantaj.getTrClass());
					String key = PdksUtil.convertToDateString(pdksVardiyaGun.getVardiyaDate(), "yyyyMMdd");
					// logger.info(key);
					if (tatilGunleriMap.containsKey(key))
						pdksVardiyaGun.setTatil(tatilGunleriMap.get(key));
					pdksVardiyaGun.setAyinGunu(pdksVardiyaGunMaster.isAyinGunu());
					if (!pdksVardiyaGun.isCalismayaBaslamadi() && !pdksVardiyaGun.isCalismayiBirakti()) {
						IsKurVardiyaGun isKurVardiya = pdksVardiyaGun.getIsKurVardiya();
						if (isKurVardiya.getId() == null) {
							int haftaGunu = PdksUtil.getDateField(pdksVardiyaGun.getVardiyaDate(), Calendar.DAY_OF_WEEK);
							switch (haftaGunu) {
							case Calendar.MONDAY:
								pdksVardiyaGun.setVardiya(sablonu.getVardiya1());
								break;
							case Calendar.TUESDAY:
								pdksVardiyaGun.setVardiya(sablonu.getVardiya2());
								break;
							case Calendar.WEDNESDAY:
								pdksVardiyaGun.setVardiya(sablonu.getVardiya3());
								break;
							case Calendar.THURSDAY:
								pdksVardiyaGun.setVardiya(sablonu.getVardiya4());
								break;
							case Calendar.FRIDAY:
								pdksVardiyaGun.setVardiya(sablonu.getVardiya5());
								break;
							case Calendar.SATURDAY:
								pdksVardiyaGun.setVardiya(sablonu.getVardiya6());
								break;
							case Calendar.SUNDAY:
								pdksVardiyaGun.setVardiya(sablonu.getVardiya7());
								break;
							default:
								break;
							}

							if (pdksVardiyaGun.getId() == null && pdksVardiyaGun.getTatil() != null && !pdksVardiyaGun.getTatil().isYarimGunMu() && pdksVardiyaGun.getVardiya() != null && pdksVardiyaGun.getVardiya().isCalisma())
								pdksVardiyaGun.setVardiya(offVardiya);
							try {
								if (personelCalmayaBasladi && pdksVardiyaGunMaster.getVardiyaDate().getTime() <= sonCalismaTarihi.getTime()) {
									calisiyor = true;
									pdksEntityController.saveOrUpdate(session, entityManager, pdksVardiyaGun.getIsKurVardiya());
								}

							} catch (Exception ee) {
								if (calisiyor)
									logger.error(pdksVardiyaGun.getVardiyaKeyStr() + "\n" + ee);
							}

							flush = true;
						}
						if (pdksVardiyaGun.isAyinGunu()) {
							vardiyalarMap.put(pdksVardiyaGun.getVardiyaKey(), pdksVardiyaGun);

						}
					} else {
						if (pdksVardiyaGun.getIsKurVardiya() != null && pdksVardiyaGun.getIsKurVardiya().getId() != null) {
							IsKurVardiyaGun isKurVardiya = pdksVardiyaGun.getIsKurVardiya();

							pdksEntityController.deleteObject(session, entityManager, isKurVardiya);

							flush = true;
						}

						pdksVardiyaGun.setId(null);
						pdksVardiyaGun.setVardiya(null);
					}

					vardiyaGunler.add(pdksVardiyaGun);

				}
				TreeMap<String, VardiyaGun> vardiyaGunMap = new TreeMap<String, VardiyaGun>();
				for (VardiyaGun pdksVardiyaGun : vardiyaGunler) {
					pdksVardiyaGun.setIzin(null);

					vardiyaGunMap.put(String.valueOf(pdksVardiyaGun.getVardiyaDateStr()), pdksVardiyaGun);
				}

				boolean haftaRenk = true;

				for (Iterator iterator = aylikPuantajSablon.getVardiyaHaftaList().iterator(); iterator.hasNext();) {
					VardiyaHafta pdksVardiyaHaftaMaster = (VardiyaHafta) iterator.next();
					long planTarih1 = Long.parseLong(PdksUtil.convertToDateString(pdksVardiyaHaftaMaster.getBasTarih(), "yyyyMMdd"));
					long planTarih2 = Long.parseLong(PdksUtil.convertToDateString(pdksVardiyaHaftaMaster.getBitTarih(), "yyyyMMdd"));
					VardiyaHafta pdksVardiyaHafta = (VardiyaHafta) pdksVardiyaHaftaMaster.clone();
					pdksVardiyaHafta.setPersonel(personel);
					if (vardiyaHaftaMap.containsKey(pdksVardiyaHafta.getKeyHafta()))
						pdksVardiyaHafta = vardiyaHaftaMap.get(pdksVardiyaHafta.getKeyHafta());

					pdksVardiyaHafta.setTrClass(haftaRenk ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
					haftaRenk = !haftaRenk;
					pdksVardiyaHafta.setHafta(pdksVardiyaHaftaMaster.getHafta());

					List<VardiyaGun> haftaVardiyaGunleri = new ArrayList<VardiyaGun>();
					List<VardiyaGun> vardiyaGunleri = new ArrayList<VardiyaGun>();
					vardiyaGunleri.addAll(pdksVardiyaHaftaMaster.getVardiyaGunler());
					for (VardiyaGun pdksVardiyaGunMaster : vardiyaGunleri) {
						VardiyaGun pdksVardiyaGun2 = new VardiyaGun(personel, null, pdksVardiyaGunMaster.getVardiyaDate());
						haftaVardiyaGunleri.add(pdksVardiyaGun2);
					}
					vardiyaGunleri = null;
					calisiyor = planTarih2 >= iseBasTarih && istenAyrilmaTarih >= planTarih1;
					if (!calisiyor) {

						pdksVardiyaHafta.setVardiyaGunler(haftaVardiyaGunleri);
					} else {
						pdksVardiyaHafta.getVardiyaGunler().clear();

						for (VardiyaGun pdksVardiyaGun2 : haftaVardiyaGunleri) {
							String key = PdksUtil.convertToDateString(pdksVardiyaGun2.getVardiyaDate(), "yyyyMMdd");
							long ayinGunu = Long.parseLong(key);
							VardiyaGun pdksVardiyaGun = pdksVardiyaGun2;
							if (ayinGunu >= iseBasTarih && istenAyrilmaTarih >= ayinGunu)
								pdksVardiyaGun = vardiyaGunMap.containsKey(key) ? vardiyaGunMap.get(key) : pdksVardiyaGun2;
							pdksVardiyaHafta.getVardiyaGunler().add(pdksVardiyaGun);
						}
					}

					for (VardiyaGun pdksVardiyaGun : pdksVardiyaHafta.getVardiyaGunler()) {

						int haftaGunu = PdksUtil.getDateField(pdksVardiyaGun.getVardiyaDate(), Calendar.DAY_OF_WEEK);
						switch (haftaGunu) {
						case Calendar.MONDAY:
							pdksVardiyaHafta.setVardiyaGun1(pdksVardiyaGun);
							break;
						case Calendar.TUESDAY:

							pdksVardiyaHafta.setVardiyaGun2(pdksVardiyaGun);
							break;
						case Calendar.WEDNESDAY:

							pdksVardiyaHafta.setVardiyaGun3(pdksVardiyaGun);
							break;
						case Calendar.THURSDAY:
							pdksVardiyaHafta.setVardiyaGun4(pdksVardiyaGun);
							break;
						case Calendar.FRIDAY:
							pdksVardiyaHafta.setVardiyaGun5(pdksVardiyaGun);
							break;
						case Calendar.SATURDAY:
							pdksVardiyaHafta.setVardiyaGun6(pdksVardiyaGun);
							break;
						case Calendar.SUNDAY:
							pdksVardiyaHafta.setVardiyaGun7(pdksVardiyaGun);
							break;
						default:
							break;
						}
					}

					aylikPuantaj.getVardiyaPlan().getVardiyaHaftaList().add(pdksVardiyaHafta);

				}
				vardiyaGunMap.clear();
				aylikPuantaj.setGecenAyFazlaMesai(null);
				if (gecenAylikPuantajSablon.getIlkGun().getTime() <= personel.getSonCalismaTarihi().getTime() && gecenAylikPuantajSablon.getSonGun().getTime() >= personel.getIseGirisTarihi().getTime()) {
					gecenAylikPuantaj.setIlkGun(gecenAylikPuantajSablon.getIlkGun());
					gecenAylikPuantaj.setSonGun(gecenAylikPuantajSablon.getSonGun());
					gecenAylikPuantaj.setGunSayisi(gecenAylikPuantajSablon.getGunSayisi());
					gecenAylikPuantaj.setGecenAyFazlaMesai(null);

					ortakIslemler.puantajHaftalikPlanOlustur(Boolean.FALSE, vardiyaGunMap, vardiyalarMap, gecenAylikPuantajSablon, gecenAylikPuantaj);
				} else
					aylikPuantaj.setGecenAylikPuantaj(null);
				ortakIslemler.puantajHaftalikPlanOlustur(Boolean.FALSE, vardiyaGunMap, vardiyalarMap, aylikPuantajSablon, aylikPuantaj);
				aylikPuantaj.setVardiyalar(vardiyaGunler);
				VardiyaPlan plan = new VardiyaPlan(personel);
				plan.setVardiyaGunMap(vardiyaGunMap);

				List<VardiyaGun> list = PdksUtil.sortListByAlanAdi(new ArrayList(vardiyalarMap.values()), "vardiyaDate", Boolean.FALSE);

				setIzin(plan.getPersonel().getId(), izinler, list);

				vardiyaPlan = null;
				vardiyaGunMap = null;
				aylikPuantaj.setVardiyaHaftaList(aylikPuantaj.getVardiyaPlan().getVardiyaHaftaList());
				int yarimYuvarla = aylikPuantaj.getYarimYuvarla();
				if (!onayDurum)
					onayDurum = aylikPuantaj.isOnayDurum();
				if (aylikPuantaj.getResmiTatilToplami() > 0)
					aylikPuantaj.setResmiTatilToplami(PdksUtil.setSureDoubleTypeRounded(aylikPuantaj.getResmiTatilToplami(), yarimYuvarla));
				if (aylikPuantaj.getHaftaCalismaSuresi() > 0)
					aylikPuantaj.setHaftaCalismaSuresi(PdksUtil.setSureDoubleTypeRounded(aylikPuantaj.getHaftaCalismaSuresi(), yarimYuvarla));
				if (aylikPuantaj.getVardiyalar() != null) {
					for (VardiyaGun pdksVardiyaGun : aylikPuantaj.getVardiyalar()) {
						if (pdksVardiyaGun.isAyinGunu() && pdksVardiyaGun.getVardiya() != null) {
							vardiyaCalisiyor = Boolean.TRUE;
							break;
						}

					}
				}
				if (vardiyaCalisiyor)
					aylikPuantajList.add(aylikPuantaj);
			}
			modelGoster = ortakIslemler.getModelGoster(denklestirmeAy, session);
		}
		setTatilMap(tatilGunleriMap);
		if (flush) {
			session.flush();
			veriGuncellendi = Boolean.TRUE;
		}

		aylikPuantajSablon.setVardiyalar(sablonVardiyalar);
		setAylikPuantajDefault(aylikPuantajSablon);
		bitTarih = null;
		resmiTatilVar = Boolean.FALSE;
		aksamGunVar = Boolean.FALSE;
		aksamSaatVar = Boolean.FALSE;
		haftaTatilVar = Boolean.FALSE;
		for (VardiyaGun pdksVardiyaGun : aylikPuantajDefault.getVardiyalar()) {
			if (pdksVardiyaGun.isAyinGunu()) {
				if (pdksVardiyaGun.getTatil() != null)
					resmiTatilVar = Boolean.TRUE;
			}

			else {
				if (pdksVardiyaGun.getVardiyaDate().before(aylikPuantajDefault.getIlkGun()))
					aylikPuantajDefault.setDenklestirmeGecenAy(denklestirmeGecenAy);
				else if (pdksVardiyaGun.getVardiyaDate().after(aylikPuantajDefault.getSonGun()))
					aylikPuantajDefault.setDenklestirmeGelecekAy(denklestirmeGelecekAy);
			}
		}
		if (gorevliPersonelMap != null && !gorevliPersonelMap.isEmpty()) {
			List<AylikPuantaj> helpAylikPuantajList = new ArrayList<AylikPuantaj>();

			for (Iterator iterator = aylikPuantajList.iterator(); iterator.hasNext();) {
				AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
				if (!resmiTatilVar)
					resmiTatilVar = aylikPuantaj.getResmiTatilToplami() > 0d;
				if (!aksamGunVar)
					aksamGunVar = aylikPuantaj.getAksamVardiyaSayisi() > 0d;
				if (!aksamSaatVar)
					aksamSaatVar = aylikPuantaj.getAksamVardiyaSaatSayisi() > 0d;
				if (!haftaTatilVar)
					haftaTatilVar = aylikPuantaj.getHaftaCalismaSuresi() > 0d;
				if (helpPersonel(aylikPuantaj.getPdksPersonel())) {
					aylikPuantaj.setTrClass("help");
					helpAylikPuantajList.add(aylikPuantaj);
					iterator.remove();
				}

			}
			if (!helpAylikPuantajList.isEmpty())
				aylikPuantajList.addAll(helpAylikPuantajList);
			helpAylikPuantajList = null;
		}

		List<AylikPuantaj> aylikPuantajAllList = new ArrayList<AylikPuantaj>();
		Long userId = authenticatedUser.getPdksPersonel().getId();
		boolean kullaniciYonetici = authenticatedUser.isYonetici() || authenticatedUser.isSuperVisor() || authenticatedUser.isProjeMuduru() || authenticatedUser.isDirektorSuperVisor();
		for (Iterator iterator = aylikPuantajList.iterator(); iterator.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
			if (!kullaniciYonetici || !aylikPuantaj.getPdksPersonel().getId().equals(userId)) {
				aylikPuantajAllList.add(aylikPuantaj);
				iterator.remove();
			}
			aylikPuantaj.setVardiyaOlustu(Boolean.FALSE);
		}
		if (!aylikPuantajAllList.isEmpty()) {
			aylikPuantajList.addAll(aylikPuantajAllList);
			List<String> list = new ArrayList<String>();
			for (Iterator iterator = aylikPuantajDefault.getAyinVardiyalari().iterator(); iterator.hasNext();) {
				VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator.next();
				list.add(pdksVardiyaGun.getVardiyaDateStr());
			}
			devam = Boolean.TRUE;
			resmiTatilVar = Boolean.FALSE;
			aksamGunVar = Boolean.FALSE;
			aksamSaatVar = Boolean.FALSE;
			haftaTatilVar = Boolean.FALSE;
			cal = Calendar.getInstance();

			for (Iterator iterator = aylikPuantajList.iterator(); iterator.hasNext();) {
				AylikPuantaj puantaj = (AylikPuantaj) iterator.next();

				try {
					if (!aksamGunVar)
						aksamGunVar = puantaj.getAksamVardiyaSayisi() > 0d;
					if (!aksamSaatVar)
						aksamSaatVar = puantaj.getAksamVardiyaSaatSayisi() > 0d;
					if (!haftaTatilVar)
						haftaTatilVar = puantaj.getHaftaCalismaSuresi() > 0d;
					if (!resmiTatilVar)
						resmiTatilVar = puantaj.getResmiTatilToplami() > 0d;
				} catch (Exception e) {
					logger.info(e);
				}

				if (puantaj.getVardiyalar() != null) {
					for (Iterator iterator2 = puantaj.getVardiyalar().iterator(); iterator2.hasNext();) {
						VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator2.next();
						pdksVardiyaGun.setAyinGunu(list.contains(pdksVardiyaGun.getVardiyaDateStr()));

					}

				}
				if (!puantaj.getTrClass().equals("help")) {
					puantaj.setTrClass(devam ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
					devam = !devam;
				}

			}

			list = null;
		}

		if (!aylikPuantajList.isEmpty()) {
			try {
				fillEkSahaTanim();
			} catch (Exception e) {

			}

		}
		aylikPuantajAllList = null;

	}

	/**
	 * 
	 */
	private void saveLastParameter() {
		LinkedHashMap<String, Object> lastMap = new LinkedHashMap<String, Object>();
		lastMap.put("yil", "" + yil);
		lastMap.put("ay", "" + ay);
		if (aramaSecenekleri.getDepartmanId() != null)
			lastMap.put("departmanId", "" + aramaSecenekleri.getDepartmanId());
		if (aramaSecenekleri.getSirketId() != null)
			lastMap.put("sirketId", "" + aramaSecenekleri.getSirketId());
		if ((ikRole) && PdksUtil.hasStringValue(sicilNo))
			lastMap.put("sicilNo", sicilNo.trim());
		try {
			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	/**
	 * @param per
	 * @param vardiya
	 * @return
	 */
	public Integer getVardiyaAdet(Personel per, Vardiya vardiya) {
		Integer adet = fazlaMesaiOrtakIslemler.getVardiyaAdet(izinTipiPersonelVardiyaMap, per, vardiya);
		return adet;
	}

	/**
	 * @param denklestirmeAy
	 * @param idler
	 * @return
	 */
	private TreeMap<Long, PersonelDenklestirme> getPersonelDenklestirme(DenklestirmeAy denklestirmeAy, ArrayList<Long> idler) {
		HashMap fields = new HashMap();
		fields.put(PdksEntityController.MAP_KEY_MAP, "getPersonelId");

		StringBuffer sb = new StringBuffer();
		sb.append("SELECT S." + PersonelDenklestirme.COLUMN_NAME_ID + " from " + PersonelDenklestirme.TABLE_NAME + " S WITH(nolock) ");
		sb.append(" WHERE S." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=:denklestirmeAy AND S." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " :p");
		fields.put("denklestirmeAy", denklestirmeAy.getId());
		fields.put("p", idler);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		TreeMap<Long, PersonelDenklestirme> denklestirmeMap = ortakIslemler.getDataByIdMap(sb, fields, PersonelDenklestirme.TABLE_NAME, PersonelDenklestirme.class);
		for (Long key : denklestirmeMap.keySet())
			denklestirmeMap.get(key).setGuncellendi(Boolean.FALSE);

		sb = null;
		fields = null;
		return denklestirmeMap;
	}

	/**
	 * @return
	 */
	public String fillAylikPlanRaporList() {
		if (!islemYapiliyor) {
			try {
				islemYapiliyor = Boolean.TRUE;
				aylikPuantajOlusturuluyor();
			} catch (Exception ee) {
				logger.info(ee.getMessage());
				ee.printStackTrace();
			} finally {
				islemYapiliyor = Boolean.FALSE;
			}
		}
		return "";

	}

	/**
	 * @return
	 * @throws Exception
	 */
	public String fillAylikVardiyaPlanList() throws Exception {
		veriGuncellendi = Boolean.FALSE;
		haftaTatilMesaiDurum = Boolean.FALSE;
		if (!islemYapiliyor) {
			islemYapiliyor = Boolean.TRUE;
			aylikPuantajOlusturuluyor();
			islemYapiliyor = Boolean.FALSE;
		}
		if (!(ikRole))
			departmanBolumAyni = false;
		return "";
	}

	/**
	 * @param idler
	 * @return
	 */
	private TreeMap<String, VardiyaHafta> getVardiyaHaftaMap(ArrayList<Long> idler) {
		HashMap map = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT DISTINCT * FROM " + VardiyaHafta.TABLE_NAME + " WITH(nolock) ");
		sb.append(" WHERE " + VardiyaHafta.COLUMN_NAME_BAS_TARIH + "<= :bitTarih AND " + VardiyaHafta.COLUMN_NAME_BIT_TARIH + ">= :basTarih AND " + VardiyaHafta.COLUMN_NAME_PERSONEL + ":pId ");
		map.put(PdksEntityController.MAP_KEY_MAP, "getKeyHafta");
		map.put("pId", idler);
		map.put("basTarih", basTarih);
		map.put("bitTarih", bitTarih);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		TreeMap<String, VardiyaHafta> vardiyaHaftaMap = pdksEntityController.getObjectBySQLMap(sb, map, VardiyaHafta.class, Boolean.FALSE);
		idler = null;
		return vardiyaHaftaMap;
	}

	/**
	 * @param vardiyaGun
	 * @param hafta
	 * @return
	 */
	public boolean getVardiyaDurum(VardiyaGun vardiyaGun, int hafta) {
		double maxCalismaSure = 12;
		try {
			if (parameterMap.containsKey("maxCalismaSure"))
				maxCalismaSure = Double.parseDouble((String) parameterMap.get("maxCalismaSure"));
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			maxCalismaSure = 12;
		}
		boolean hata = vardiyaGun == null || vardiyaGun.getVardiya() == null || !vardiyaGun.getVardiya().isCalisma();

		if (!hata) {
			if (vardiyaGun.getVardiya() != null && vardiyaGun.getIslemVardiya() == null)
				vardiyaGun.setVardiyaZamani();
			double fark = vardiyaGun.getIslemVardiya().getNetCalismaSuresi();
			if (!authenticatedUser.isVardiyaDuzeltebilir() && fark > (maxCalismaSure + 0.5)) {
				hata = Boolean.TRUE;
				PdksUtil.addMessageAvailableWarn(PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), "d MMMMM EEEEE") + " günü çalışma süresi " + maxCalismaSure + " saati geçemez!");
			} else if (fark == 0) {
				hata = Boolean.TRUE;
				PdksUtil.addMessageAvailableWarn(PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), "d MMMMM EEEEE") + " günü çalışma süresi 0 olamaz!");
			}
		} else
			hata = Boolean.FALSE;

		return hata;
	}

	/**
	 * @param sablon
	 * @param personel
	 * @return
	 */
	public ArrayList<Vardiya> fillVardiyaDoldurList(VardiyaSablonu sablon, Personel personel) {
		ArrayList<Vardiya> list = new ArrayList<Vardiya>();
		try {
			if (vardiyaMap.containsKey(sablon.getToplamSaat()))
				list.addAll(vardiyaMap.get(sablon.getToplamSaat()));
			if (list.isEmpty())
				list.addAll(calismaOlmayanVardiyaList);

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return list;

	}

	/**
	 * @param aylikPuantaj
	 * @param pd
	 * @return
	 */
	public ArrayList<Vardiya> fillAylikVardiyaList(AylikPuantaj aylikPuantaj, PersonelDenklestirme pd) {
		Personel personel = aylikPuantaj.getPdksPersonel();
		vardiyaMap.clear();
		ArrayList<Vardiya> pdksList = new ArrayList<Vardiya>();
		String radyolojiIzinDurum = ortakIslemler.getParameterKey("radyolojiIzinDurum");
		TreeMap<Long, Vardiya> vardiyaMap = new TreeMap<Long, Vardiya>();
		try {

			boolean gebeMi = personel.isPersonelGebeMi(), sua = personel.isSuaOlur(), icap = personel.getIcapciOlabilir();

			for (VardiyaGun pdksVardiyaGun : aylikPuantaj.getVardiyalar()) {
				Vardiya pdksVardiya = pdksVardiyaGun.getVardiya();
				if (pdksVardiyaGun.isAyinGunu() && pdksVardiya != null) {
					if (pdksVardiya.isCalisma()) {
						if (!sua)
							sua = pdksVardiyaGun.getVardiya().getSua() != null && pdksVardiyaGun.getVardiya().getSua();
						if (!gebeMi)
							gebeMi = pdksVardiyaGun.getVardiya().getGebelik() != null && pdksVardiyaGun.getVardiya().getGebelik();
						if (!icap)
							icap = pdksVardiyaGun.getVardiya().getIcapVardiya() != null && pdksVardiyaGun.getVardiya().getIcapVardiya();
					}
					if (pdksVardiya.getDurum())
						vardiyaMap.put(pdksVardiya.getId(), pdksVardiya);
				}
			}

			HashMap map = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("WITH VARDIYA_DATA AS ( ");
			sb.append("SELECT V.* FROM " + Vardiya.TABLE_NAME + " V WITH(nolock) ");
			if (pd == null)
				pd = aylikPuantaj.getPersonelDenklestirmeAylik();
			boolean calismaOlmayanVardiyalar = false;
			if (pd != null) {

				if (pd.getCalismaModeliAy() != null) {
					try {
						CalismaModeliAy calismaModeliAy = pd.getCalismaModeliAy();
						if (calismaModeliAy.getCalismaModeli().getGenelVardiya().equals(Boolean.FALSE)) {
							sb.append(" INNER JOIN " + CalismaModeliVardiya.TABLE_NAME + " CV ON CV." + CalismaModeliVardiya.COLUMN_NAME_CALISMA_MODELI + "=:cm  ");
							sb.append(" AND CV." + CalismaModeliVardiya.COLUMN_NAME_VARDIYA + "=V." + Vardiya.COLUMN_NAME_ID);
							map.put("cm", calismaModeliAy.getCalismaModeli().getId());
							calismaOlmayanVardiyalar = true;

						}
					} catch (Exception eee) {
						logger.error(eee);
					}

				}
			}

			sb.append(" WHERE V." + Vardiya.COLUMN_NAME_GENEL + " =1 AND V." + Vardiya.COLUMN_NAME_GEBELIK + " = 0   ");
			sb.append(" AND V." + Vardiya.COLUMN_NAME_GEBELIK + " = 0 AND V." + Vardiya.COLUMN_NAME_ICAP + " = 0  AND V." + Vardiya.COLUMN_NAME_SUA + " = 0 ");
			sb.append(" AND V." + Vardiya.COLUMN_NAME_VARDIYA_TIPI + " <>'I'  ");
			if (gebeMi) {
				sb.append(" UNION ");
				sb.append(" SELECT * FROM " + Vardiya.TABLE_NAME + " WITH(nolock) ");
				sb.append(" WHERE " + Vardiya.COLUMN_NAME_GEBELIK + " = 1 ");
			}
			if (sua) {
				sb.append(" UNION ");
				sb.append(" SELECT * FROM " + Vardiya.TABLE_NAME + " WITH(nolock) ");
				sb.append(" WHERE " + Vardiya.COLUMN_NAME_SUA + " = 1 ");
			}
			if (icap) {
				sb.append(" UNION ");
				sb.append(" SELECT * FROM " + Vardiya.TABLE_NAME + " WITH(nolock) ");
				sb.append(" WHERE  " + Vardiya.COLUMN_NAME_ICAP + " = 1 ");

			}
			if (calismaOlmayanVardiyalar) {
				sb.append(" UNION ");
				sb.append(" SELECT * FROM " + Vardiya.TABLE_NAME + " WITH(nolock) ");
				sb.append(" WHERE  " + Vardiya.COLUMN_NAME_VARDIYA_TIPI + " <>'' AND " + Vardiya.COLUMN_NAME_VARDIYA_TIPI + " <>'I'  ");
			}
			if (manuelVardiyaIzinGir) {
				sb.append(" UNION ");
				sb.append(" SELECT DISTINCT V.* FROM " + Vardiya.TABLE_NAME + " V WITH(nolock) ");
				sb.append(" LEFT JOIN " + IzinTipi.TABLE_NAME + " I ON I." + IzinTipi.COLUMN_NAME_DEPARTMAN + "=V." + Vardiya.COLUMN_NAME_DEPARTMAN + " AND I." + IzinTipi.COLUMN_NAME_DURUM + "=1");
				sb.append("  AND I." + IzinTipi.COLUMN_NAME_GIRIS_TIPI + "=0 AND I." + IzinTipi.COLUMN_NAME_BAKIYE_IZIN_TIPI + " IS NULL");
				sb.append(" WHERE V." + Vardiya.COLUMN_NAME_DEPARTMAN + "=" + personel.getSirket().getDepartman().getId() + "  AND V." + Vardiya.COLUMN_NAME_DURUM + "=1 ");
				sb.append("  AND V." + Vardiya.COLUMN_NAME_VARDIYA_TIPI + " IN ('" + Vardiya.TIPI_IZIN + "','" + Vardiya.TIPI_HASTALIK_RAPOR + "')  AND I." + IzinTipi.COLUMN_NAME_ID + " IS NULL ");
			}
			sb.append(" )   ");
			sb.append(" SELECT DISTINCT D.* FROM VARDIYA_DATA D ");
			sb.append(" WHERE " + Vardiya.COLUMN_NAME_DURUM + "= 1 ");
			sb.append(" AND (" + Vardiya.COLUMN_NAME_DEPARTMAN + " IS NULL OR  " + Vardiya.COLUMN_NAME_DEPARTMAN + "=:departmanId) ");
			sb.append(" AND (" + Vardiya.COLUMN_NAME_ISKUR + "=1 OR " + Vardiya.COLUMN_NAME_VARDIYA_TIPI + "<>'') ");

			sb.append(" ORDER BY " + Vardiya.COLUMN_NAME_EKRAN_SIRA + "," + Vardiya.COLUMN_NAME_VARDIYA_TIPI + "," + Vardiya.COLUMN_NAME_ID);
			map.put("departmanId", personel.getSirket().getDepartman().getId());
			logger.debug(sb.toString());
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			vardiyaList = pdksEntityController.getObjectBySQLList(sb, map, Vardiya.class);
			if (!vardiyaList.isEmpty()) {
				for (Iterator<Vardiya> iterator = vardiyaList.iterator(); iterator.hasNext();) {
					Vardiya pdksVardiya = iterator.next();
					if (vardiyaMap.containsKey(pdksVardiya.getId()))
						vardiyaMap.remove(pdksVardiya.getId());
					if (!pdksVardiya.getGenel()) {

						if (pdksVardiya.isGebelikMi() && !gebeMi)
							continue;
						if (!pdksVardiya.isRadyasyonIzni() || (pdksVardiya.isRadyasyonIzni() && (sua || radyolojiIzinDurum.equals("1"))))
							pdksList.add(pdksVardiya);
					} else {
						pdksList.add(pdksVardiya);
					}

				}
			}

			if (!vardiyaMap.isEmpty()) {
				vardiyaList.clear();
				vardiyaList.addAll(new ArrayList<Vardiya>(vardiyaMap.values()));
				if (!pdksList.isEmpty())
					vardiyaList.addAll(pdksList);
				pdksList.clear();
				pdksList.addAll(vardiyaList);
				vardiyaMap.clear();
				map.clear();
				for (Vardiya pdksVardiya : vardiyaList)
					vardiyaMap.put(pdksVardiya.getId(), pdksVardiya);
				sb = new StringBuffer();
				sb.append("SELECT V.* FROM " + Vardiya.TABLE_NAME + " V WITH(nolock) ");
				sb.append(" WHERE V." + Vardiya.COLUMN_NAME_ID + ":id");
				sb.append(" AND V." + Vardiya.COLUMN_NAME_DURUM + "=1");
				map.put("id", new ArrayList(vardiyaMap.keySet()));
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				vardiyaList = pdksEntityController.getObjectBySQLList(sb, map, Vardiya.class);
			}
			if (vardiyaList.size() > 1)
				vardiyaList = PdksUtil.sortObjectStringAlanList(vardiyaList, "getKisaAdiSort", null);

			pdksList.clear();
			pdksList.addAll(vardiyaList);

			vardiyaMap = null;
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			logger.error("fillVardiyaList Hata : " + e.getMessage());
		}

		return pdksList;

	}

	/**
	 * @return
	 */
	public List<Vardiya> fillAllVardiyaList() {
		List<Vardiya> pdksList = new ArrayList<Vardiya>();
		HashMap parametreMap = new HashMap();
		try {
			parametreMap.put("durum", Boolean.TRUE);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			pdksList = pdksEntityController.getObjectByInnerObjectList(parametreMap, Vardiya.class);
			for (Iterator iterator = pdksList.iterator(); iterator.hasNext();) {
				Vardiya pdksVardiya = (Vardiya) iterator.next();
				if (pdksVardiya.getDepartman() != null && !aramaSecenekleri.getDepartmanId().equals(pdksVardiya.getDepartman().getId()))
					iterator.remove();

			}

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			logger.error("fillVardiyaList Hata : " + e.getMessage());
		} finally {
			parametreMap = null;
		}

		return pdksList;

	}

	/**
	 * 
	 */
	public void fillVardiyaList() {
		vardiyaMap.clear();
		List<Vardiya> pdksList = fillAllVardiyaList();
		boolean durum = Boolean.FALSE;
		List<Vardiya> calismaolmayanVardiyaList = new ArrayList<Vardiya>();
		try {
			for (Iterator<Vardiya> iterator = pdksList.iterator(); iterator.hasNext();) {
				Vardiya pdksVardiya = iterator.next();
				if (pdksVardiya.isCalisma()) {
					ArrayList<Vardiya> list = vardiyaMap.containsKey(pdksVardiya.getCalismaSaati()) ? vardiyaMap.get(pdksVardiya.getCalismaSaati()) : new ArrayList<Vardiya>();
					list.add(pdksVardiya);
					vardiyaMap.put(pdksVardiya.getCalismaSaati(), list);
				} else
					calismaolmayanVardiyaList.add(pdksVardiya);
			}
			if (!calismaolmayanVardiyaList.isEmpty()) {
				for (Iterator<Double> iterator = vardiyaMap.keySet().iterator(); iterator.hasNext();) {
					Double gun = iterator.next();
					ArrayList<Vardiya> list = vardiyaMap.get(gun);
					for (Vardiya pdksVardiya : calismaolmayanVardiyaList)
						list.add(pdksVardiya);
					vardiyaMap.put(gun, list);

				}
			}

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			logger.error("fillVardiyaList Hata : " + e.getMessage());
		}

		setVardiyaVar(durum);

	}

	/**
	 * @param vardiyaHafta
	 */
	public void haftaRefresh(VardiyaHafta vardiyaHafta) {
		session.refresh(vardiyaHafta);
		for (VardiyaGun pdksVardiyaGun : vardiyaHafta.getVardiyaGunler()) {
			if (pdksVardiyaGun.getId() != null)
				session.refresh(pdksVardiyaGun);

		}
	}

	/**
	 * 
	 */
	public void instanceRefresh() {
		if (degisti != null && degisti) {
			try {
				session.refresh(getVardiyaPlan().getPersonel().getSablon());
				if (getVardiyaPlan().getVardiyaHafta1().getStyle().equals(VardiyaGun.STYLE_CLASS_EVEN) || getVardiyaPlan().getVardiyaHafta2().getStyle().equals(VardiyaGun.STYLE_CLASS_EVEN)) {
					if (getVardiyaPlan().getVardiyaHafta1().isCheckBoxDurum())
						haftaRefresh(getVardiyaPlan().getVardiyaHafta1());
					if (getVardiyaPlan().getVardiyaHafta2().isCheckBoxDurum())
						haftaRefresh(getVardiyaPlan().getVardiyaHafta2());
				} else {
					fillAylikVardiyaPlanList();
				}
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				logger.error("instanceRefresh : " + e.getMessage());
			}

			degisti = Boolean.FALSE;
		}
		setKaydet(Boolean.TRUE);
	}

	private void fillEkSahaTanim() {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
	}

	/**
	 * @throws Exception
	 */
	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() throws Exception {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		adminRoleDurum(authenticatedUser);

		session.clear();
		donusAdres = "";
		denklestirmeAyDurum = Boolean.FALSE;
		modelList = new ArrayList<CalismaModeliAy>();

		if (personelDenklestirmeDinamikAlanList == null)
			personelDenklestirmeDinamikAlanList = new ArrayList<PersonelDenklestirmeDinamikAlan>();
		else
			personelDenklestirmeDinamikAlanList.clear();

		try {
			departmanBolumAyni = Boolean.FALSE;
			aramaSecenekleri = new AramaSecenekleri(authenticatedUser);
			stajerSirket = Boolean.FALSE;
			boolean ayniSayfa = authenticatedUser.getCalistigiSayfa() != null && authenticatedUser.getCalistigiSayfa().equals("isKurVardiyaPlani");
			if (!ayniSayfa)
				authenticatedUser.setCalistigiSayfa("isKurVardiyaPlani");
			setPlanGirisi(Boolean.TRUE);
			islemYapiliyor = Boolean.FALSE;
			hastaneSuperVisor = authenticatedUser.isDirektorSuperVisor();

			aramaSecenekleri.setTesisList(null);
			aramaSecenekleri.setTesisId(null);
			aramaSecenekleri.setGorevYeriList(null);
			aramaSecenekleri.setEkSaha3Id(null);
			aramaSecenekleri.setSirketId(null);
			gorevYeri = null;

			if (!authenticatedUser.isAdmin() && !authenticatedUser.isIK())
				planDepartman = authenticatedUser.getPdksPersonel().getEkSaha1() != null ? authenticatedUser.getPdksPersonel().getEkSaha1() : null;
			else
				planDepartman = null;

			aylar = PdksUtil.getAyListesi(Boolean.TRUE);

			if (aylikPuantajList != null)
				aylikPuantajList.clear();
			else
				aylikPuantajList = new ArrayList<AylikPuantaj>();
			Calendar cal = Calendar.getInstance();
			ay = cal.get(Calendar.MONTH) + 1;
			yil = cal.get(Calendar.YEAR);
			cal.add(Calendar.MONTH, 1);
			maxYil = cal.get(Calendar.YEAR);
			sonDonem = (maxYil * 100) + cal.get(Calendar.MONTH) + 1;
			if (basTarih == null) {
				cal = Calendar.getInstance();
				cal.add(Calendar.DATE, -7);
				cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				setBasTarih(PdksUtil.getDate(cal.getTime()));
			}
			setBitTarih(null);

			if (!authenticatedUser.isAdmin())
				setDepartman(authenticatedUser.getDepartman());
			aramaSecenekleri.setDepartman(authenticatedUser.getDepartman());
			aramaSecenekleri.setDepartmanId(authenticatedUser.getDepartman().getId());
			setDepartman(authenticatedUser.getDepartman());

			setVardiyaPlanList(new ArrayList<VardiyaPlan>());
			HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			String perIdStr = (String) req.getParameter("perId");
			String dateStr = (String) req.getParameter("tarih");
			String planKey = (String) req.getParameter("planKey");

			if (planKey != null) {
				HashMap<String, String> veriMap = PdksUtil.getDecodeMapByBase64(planKey);
				if (veriMap.containsKey("tarih"))
					dateStr = veriMap.get("tarih");
				if (veriMap.containsKey("perId"))
					perIdStr = veriMap.get("perId");
				veriMap = null;
			}

			LinkedHashMap<String, Object> veriLastMap = null;
			String doldurStr = null;
			if (planKey == null) {
				veriLastMap = ortakIslemler.getLastParameter("isKurVardiyaPlani", session);
				if (veriLastMap != null) {
					if (!veriLastMap.isEmpty())
						doldurStr = "V";
					if (veriLastMap.containsKey("yil") && veriLastMap.containsKey("ay") && veriLastMap.containsKey("sirketId")) {

						yil = Integer.parseInt((String) veriLastMap.get("yil"));
						ay = Integer.parseInt((String) veriLastMap.get("ay"));

						Long sId = Long.parseLong((String) veriLastMap.get("sirketId"));
						aramaSecenekleri.setSirketId(sId);
						if (veriLastMap.containsKey("departmanId") && adminRole) {
							Long departmanId = Long.parseLong((String) veriLastMap.get("departmanId"));
							aramaSecenekleri.setDepartmanId(departmanId);
						}

						if ((ikRole) && veriLastMap.containsKey("sicilNo"))
							sicilNo = (String) veriLastMap.get("sicilNo");
						if (yil > 0 && ay > 0 && aramaSecenekleri.getSirketId() != null) {
							dateStr = String.valueOf(yil * 100 + ay) + "01";
							perIdStr = "0";
						}

					}

				}
			}

			if (dateStr != null && perIdStr != null) {
				donusAdres = linkAdres;
				Date vardiyaDate = PdksUtil.convertToJavaDate(dateStr, "yyyyMMdd");
				cal.setTime(vardiyaDate);
				yil = cal.get(Calendar.YEAR);
				ay = cal.get(Calendar.MONTH) + 1;
				HashMap parametreMap = new HashMap();

				parametreMap.put("pdksPersonel.id", new Long(perIdStr));
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				PersonelKGS personelKGS = (PersonelKGS) pdksEntityController.getObjectByInnerObject(parametreMap, PersonelKGS.class);
				PersonelView personelView = personelKGS != null ? personelKGS.getPersonelView() : null;
				islemYapiliyor = false;
				if (personelView != null && personelView.getPdksPersonel() != null) {
					doldurStr = "F";
					Personel personel = personelView.getPdksPersonel();
					Sirket sirket = personel.getSirket();
					if (sirket != null) {
						aramaSecenekleri.setDepartmanId(sirket.getDepartman().getId());
						aramaSecenekleri.setSirketId(sirket.getId());

						if (personel.getTesis() != null)
							aramaSecenekleri.setTesisId(personel.getTesis().getId());

						if (personel.getEkSaha3() != null)
							aramaSecenekleri.setEkSaha3Id(personel.getEkSaha3().getId());
					}

					sicilNo = personel.getPdksSicilNo();
				}

			}
			setSeciliDenklestirmeAy();
			aylariDoldur();
			if (doldurStr != null) {
				Long sirketId = aramaSecenekleri.getSirketId();

				fillSirketList();
				if (sirketId != null)
					aramaSecenekleri.setSirketId(sirketId);

				if (doldurStr.equals("F"))
					fillAylikVardiyaPlanList();
				else
					donusAdres = "";
			} else if (denklestirmeAy != null && aramaSecenekleri.getSirketIdList() == null)
				fillSirketList();
			if (!ayniSayfa)
				authenticatedUser.setCalistigiSayfa("");
			fileImportKontrol();

			if (donusAdres != null && donusAdres.equals("isKurVardiyaPlani"))
				donusAdres = "";

		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		kullaniciPersonel = ortakIslemler.getKullaniciPersonel(authenticatedUser);
		if (kullaniciPersonel)
			sicilNo = authenticatedUser.getPdksPersonel().getPdksSicilNo();

	}

	/**
	 * @param bolumDoldurDurum
	 * @throws Exception
	 */
	public void tesisDoldur(boolean bolumDoldurDurum) throws Exception {
		Sirket sirket = null;
		listeleriTemizle();

		if (aramaSecenekleri.getSirketId() != null) {
			HashMap fields = new HashMap();
			fields.put("id", aramaSecenekleri.getSirketId());

			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);
		}
		aramaSecenekleri.setSirket(sirket);
		// List<SelectItem> list = fazlaMesaiOrtakIslemler.tesisDoldur(departman, aramaSecenekleri.getSirket(), aramaSecenekleri.getEkSaha1Id(), yil, ay, Boolean.FALSE, Boolean.FALSE, session);
		List<SelectItem> list = fazlaMesaiOrtakIslemler.getFazlaMesaiTesisList(aramaSecenekleri.getSirket(), denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, getDenklestirmeDurum(), session);
		aramaSecenekleri.setTesisList(list);
		if (!list.isEmpty()) {
			if (list.size() == 1)
				aramaSecenekleri.setTesisId((Long) list.get(0).getValue());
		}
		if (bolumDoldurDurum) {
			if (sirket != null) {
				if (sirket.isTesisDurumu() == false)
					aramaSecenekleri.setTesisId(null);
			}
			bolumDoldur();
		}

		aylikPuantajList.clear();

	}

	/**
	 * @throws Exception
	 */
	public void bolumDoldur() throws Exception {
		stajerSirket = Boolean.FALSE;
		aramaSecenekleri.setGorevYeriList(null);
		if (donusAdres == null || donusAdres.equals("")) {
			listeleriTemizle();
		}
		Sirket sirket = null;
		if (aramaSecenekleri.getSirket() != null || aramaSecenekleri.getSirketId() != null) {
			sirket = aramaSecenekleri.getSirket();
			if (aramaSecenekleri.getSirket() == null) {
				HashMap fields = new HashMap();
				fields.put(" id", aramaSecenekleri.getSirketId());
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);
				aramaSecenekleri.setSirket(sirket);
			}
			departman = sirket.getDepartman();

		}
		List<SelectItem> list = null;
		if (sirket != null) {
			String tesisId = null;
			if (departman.isAdminMi() && sirket.isTesisDurumu())
				tesisId = aramaSecenekleri.getTesisId() != null ? String.valueOf(aramaSecenekleri.getTesisId()) : null;
			list = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, tesisId, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, getDenklestirmeDurum(), session);
		} else
			list = new ArrayList<SelectItem>();

		aramaSecenekleri.setGorevYeriList(list);
		if (list.size() == 1)
			aramaSecenekleri.setEkSaha3Id((Long) list.get(0).getValue());
		fileImportKontrol();
	}

	/**
	 * @param vardiyaGun
	 * @param vardiyalar
	 * @param tatilVardiyalar
	 * @param vardiya
	 * @return
	 */
	public VardiyaGun vardiyaListeDegistir(VardiyaGun vardiyaGun, ArrayList<Vardiya> vardiyalar, ArrayList<Vardiya> tatilVardiyalar, Vardiya vardiya) {
		TreeMap<String, Tatil> tatillerMap = getTatilMap();
		String key = PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), "yyyyMMdd");
		vardiyaGun.setTatil(null);
		if (tatillerMap.containsKey(key))
			vardiyaGun.setTatil(tatillerMap.get(key));
		if (vardiyaGun.getVardiya() != null) {
			if (vardiya != null) {
				vardiyaGun.setVardiya(vardiya);
				if (vardiya.isCalisma()) {
					vardiyaGun.setBasSaat((int) vardiya.getBasSaat());
					vardiyaGun.setBasDakika((int) vardiya.getBasDakika());
					vardiyaGun.setBitSaat((int) vardiya.getBitSaat());
					vardiyaGun.setBitDakika((int) vardiya.getBitDakika());
				} else {
					vardiyaGun.setBasSaat(null);
					vardiyaGun.setBasDakika(null);
					vardiyaGun.setBitSaat(null);
					vardiyaGun.setBitDakika(null);
				}
			}
			if (vardiyaGun.getTatil() == null || vardiyaGun.getTatil().isYarimGunMu())
				vardiyaGun.setVardiyalar(vardiyalar);
			else
				vardiyaGun.setVardiyalar(tatilVardiyalar);
		}
		return vardiyaGun;
	}

	/**
	 * @param donemi
	 * @param perList
	 * @return
	 */
	public List<PersonelDenklestirme> getPersonelDenklestirme(DepartmanDenklestirmeDonemi donemi, List<Personel> perList) {
		List<PersonelDenklestirme> list = null;
		return list;
	}

	/**
	 * @param hafta
	 */
	public void personelSablonGuncelle(VardiyaHafta hafta) {
		setDegisti(Boolean.TRUE);
		VardiyaSablonu vardiyaSablonu = hafta.getVardiyaSablonu();
		boolean vardiyaDegistir = Boolean.TRUE;
		ArrayList<Vardiya> tatilVardiyaList = null;
		try {
			tatilVardiyaList = fillVardiyaDoldurList(vardiyaSablonu, vardiyaPlan.getPersonel());

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
		}
		ArrayList<Vardiya> vardiyaList = new ArrayList<Vardiya>();
		for (Vardiya vardiya : tatilVardiyaList)
			vardiyaList.add(vardiya);
		int calismaGunSayisi = vardiyaSablonu.getCalismaGunSayisi();
		double toplamSaat = vardiyaSablonu.getToplamSaat();
		for (Iterator<Vardiya> iterator = hafta.getVardiyalar().iterator(); iterator.hasNext();) {
			Vardiya vardiya = iterator.next();
			if (vardiya.isCalisma() && vardiya.getCalismaGun() == calismaGunSayisi && toplamSaat == vardiya.getCalismaSaati()) {
				vardiyaDegistir = Boolean.FALSE;
				break;
			}
		}
		hafta.setVardiyalar(vardiyaList);
		vardiyaListeDegistir(hafta.getVardiyaGun1(), vardiyaList, tatilVardiyaList, vardiyaDegistir ? vardiyaSablonu.getVardiya1() : null);
		vardiyaListeDegistir(hafta.getVardiyaGun2(), vardiyaList, tatilVardiyaList, vardiyaDegistir ? vardiyaSablonu.getVardiya2() : null);
		vardiyaListeDegistir(hafta.getVardiyaGun3(), vardiyaList, tatilVardiyaList, vardiyaDegistir ? vardiyaSablonu.getVardiya3() : null);
		vardiyaListeDegistir(hafta.getVardiyaGun4(), vardiyaList, tatilVardiyaList, vardiyaDegistir ? vardiyaSablonu.getVardiya4() : null);
		vardiyaListeDegistir(hafta.getVardiyaGun5(), vardiyaList, tatilVardiyaList, vardiyaDegistir ? vardiyaSablonu.getVardiya5() : null);
		vardiyaListeDegistir(hafta.getVardiyaGun6(), vardiyaList, tatilVardiyaList, vardiyaDegistir ? vardiyaSablonu.getVardiya6() : null);
		vardiyaListeDegistir(hafta.getVardiyaGun7(), vardiyaList, tatilVardiyaList, vardiyaDegistir ? vardiyaSablonu.getVardiya7() : null);

	}

	/**
	 * @param personel
	 */
	public void fillVardiyaSablonList(Personel personel) {
		List<VardiyaSablonu> sablonList = null;
		try {

			Departman departman = personel.getSirket() != null ? personel.getSirket().getDepartman() : null;
			HashMap parametreMap = new HashMap();
			parametreMap.put("durum", Boolean.TRUE);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			sablonList = pdksEntityController.getObjectByInnerObjectList(parametreMap, VardiyaSablonu.class);
			if (departman != null) {
				for (Iterator iterator = sablonList.iterator(); iterator.hasNext();) {
					VardiyaSablonu pdksVardiyaSablonu = (VardiyaSablonu) iterator.next();
					if (pdksVardiyaSablonu.getDepartman() != null && !departman.getId().equals(pdksVardiyaSablonu.getDepartman().getId()))
						iterator.remove();
				}
			}
			if (sablonList != null)
				sablonList.clear();
			else
				sablonList = new ArrayList<VardiyaSablonu>();
			if (!sablonList.isEmpty())
				sablonList.addAll(sablonList);

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}
		sablonList = null;

	}

	/**
	 * @param personel
	 * @return
	 */
	private boolean helpPersonel(Personel personel) {
		return personel != null && gorevliPersonelMap != null && gorevliPersonelMap.containsKey(personel.getPdksSicilNo());

	}

	/**
	 * @param vardiyaGun
	 * @return
	 */
	private boolean calisan(VardiyaGun vardiyaGun) {
		boolean calisan = vardiyaGun != null;
		if (calisan) {
			if (vardiyaGun.getVardiya() != null) {

				calisan = vardiyaGun.isKullaniciYetkili() || (vardiyaGun.getIzin() != null && !helpPersonel(vardiyaGun.getPersonel()));
			}
		}
		return calisan;
	}

	/**
	 * @param list
	 * @param vardiyaPlan
	 * @throws Exception
	 */
	protected void denklestirmeVerileriOlustur(List<VardiyaPlan> list, VardiyaPlan vardiyaPlan) throws Exception {
		if (list == null)
			list = new ArrayList<VardiyaPlan>();
		if (vardiyaPlan != null)
			list.add(vardiyaPlan);
		if (!list.isEmpty()) {
			Calendar cal = Calendar.getInstance();
			HashMap<Long, VardiyaPlan> hashMap = new HashMap<Long, VardiyaPlan>();
			for (VardiyaPlan pdksVardiyaPlan : list)
				hashMap.put(pdksVardiyaPlan.getPersonel().getId(), pdksVardiyaPlan);
			DepartmanDenklestirmeDonemi denklestirmeDonemi = (DepartmanDenklestirmeDonemi) getDenklestirmeDonemi().clone();
			denklestirmeDonemi.setDenklestirmeAy(denklestirmeAy);
			denklestirmeDonemi.setDenklestirmeAyDurum(denklestirmeAyDurum);
			for (int i = 0; i < 3; i++) {
				if (denklestirmeDonemi.getBaslangicTarih() != null && PdksUtil.tarihKarsilastirNumeric(denklestirmeDonemi.getBitisTarih(), denklestirmeDonemi.getBaslangicTarih()) == 1) {
					tatilGunleriMap = ortakIslemler.getTatilGunleri(new ArrayList<Personel>(), ortakIslemler.tariheGunEkleCikar(cal, denklestirmeDonemi.getBaslangicTarih(), -1), ortakIslemler.tariheGunEkleCikar(cal, denklestirmeDonemi.getBitisTarih(), 1), session);
					List<PersonelDenklestirmeTasiyici> personelDenklestirmeList = ortakIslemler.personelDenklestir(denklestirmeDonemi, tatilGunleriMap, "id", new ArrayList<Long>(hashMap.keySet()), Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, session);
					for (PersonelDenklestirmeTasiyici personelDenklestirme : personelDenklestirmeList) {
						if (hashMap.containsKey(personelDenklestirme.getPersonel().getId())) {

							if (personelDenklestirme.getVardiyaGunleriMap() != null) {
								List<VardiyaGun> list2 = new ArrayList<VardiyaGun>(personelDenklestirme.getVardiyaGunleriMap().values());
								for (VardiyaGun pdksVardiyaGun : list2) {
									if (pdksVardiyaGun.getVardiya() == null)
										continue;

								}
							}

							hashMap.get(personelDenklestirme.getPersonel().getId()).setPersonelDenklestirme(personelDenklestirme, i);

						}
					}
				}
				Date tarih = ortakIslemler.tariheGunEkleCikar(cal, denklestirmeDonemi.getBitisTarih(), 7);
				denklestirmeDonemi.setBitisTarih(tarih);
				denklestirmeDonemi.setBaslangicTarih(ortakIslemler.tariheGunEkleCikar(cal, tarih, -6));

			}

		}

	}

	/**
	 * 
	 */
	private void fileImportKontrol() throws Exception {
		fileImport = Boolean.FALSE;
		if (aylikPuantajDosyaList == null)
			aylikPuantajDosyaList = new ArrayList<AylikPuantaj>();
		if (authenticatedUser.isIK() && ortakIslemler.getParameterKey("fileImport").equals("1") && ay > 0 && aramaSecenekleri.getSirketId() != null) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, 1);
			int donem = (cal.get(Calendar.YEAR) * 100) + cal.get(Calendar.MONTH) + 1;
			if (yil * 100 + ay <= donem) {
				HashMap fields = new HashMap();
				fields.put("yil", yil);
				fields.put("ay", ay);
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
				setDenklestirmeAyDurum(fazlaMesaiOrtakIslemler.getDurum(denklestirmeAy));
				fileImport = denklestirmeAy != null && denklestirmeAy.getDurum();
			}

		}
		if (fileImport) {

			fileImportDosyaSifirla();

		}

	}

	/**
	 * @return
	 * @throws Exception
	 */
	public String fileImportDosyaSifirla() throws Exception {
		aylikPuantajDosyaList.clear();
		vardiyaPlanDosya.setDosyaIcerik(null);
		degisti = Boolean.FALSE;
		onayDurum = Boolean.FALSE;
		return "";
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public void listenerFileImportDosya(UploadEvent event) throws Exception {
		UploadItem item = event.getUploadItem();
		PdksUtil.getDosya(item, vardiyaPlanDosya);
		aylikPuantajDosyaList.clear();
	}

	/**
	 * @return
	 */
	public String durumDegistir() {
		for (AylikPuantaj aylikPuantaj : aylikPuantajDosyaList) {
			if (aylikPuantaj.isKaydet()) {
				aylikPuantaj.setSecili(onayDurum);
			}
		}
		return "";
	}

	/**
	 * @return
	 * @throws Exception
	 */
	@Transactional
	public String vardiyaPlanDosyaYaz() throws Exception {
		boolean islemYapildi = Boolean.FALSE;
		Calendar cal = Calendar.getInstance();
		Date sonGun = ortakIslemler.tariheAyEkleCikar(cal, aylikPuantajDefault.getIlkGun(), 1);
		List<VardiyaGun> vardiyaGunleri = new ArrayList<VardiyaGun>();

		List<String> perNoList = new ArrayList<String>();
		List<Long> perIdList = new ArrayList<Long>();
		TreeMap<Long, TreeMap<Long, Vardiya>> calismaModeliMap = getCalismaModeliMap(aylikPuantajDosyaList);
		for (AylikPuantaj aylikPuantaj : aylikPuantajDosyaList) {
			if (aylikPuantaj.isKaydet() && aylikPuantaj.isSecili()) {
				TreeMap<Long, Vardiya> vardiyaMap = null;
				if (aylikPuantaj.getCalismaModeli() != null) {
					Long id = aylikPuantaj.getCalismaModeli().getId();
					if (calismaModeliMap.containsKey(id))
						vardiyaMap = calismaModeliMap.get(id);
				}
				setPersonelAylikPuantaj(aylikPuantaj);
				Personel personel = (Personel) aylikPuantaj.getPdksPersonel().clone();
				boolean calisiyor = personel.getIseGirisTarihi().before(aylikPuantajDefault.getIlkGun()) && personel.getIstenAyrilisTarihi().after(sonGun);
				personel.setDurum(Boolean.TRUE);
				VardiyaPlan pdksVardiyaPlan = aylikPuantaj.getVardiyaPlan();
				for (VardiyaGun pdksVardiyaGun : aylikPuantaj.getVardiyalar()) {
					if (pdksVardiyaGun.isAyinGunu()) {
						if (!calisiyor && !personel.isCalisiyorGun(pdksVardiyaGun.getVardiyaDate()))
							continue;
						long basVardiyaId = (pdksVardiyaGun.getVardiya() != null ? pdksVardiyaGun.getVardiya().getIdLong() : 0L), bitVardiyaId = (pdksVardiyaGun.getYeniVardiya() != null ? pdksVardiyaGun.getYeniVardiya().getIdLong() : 0L);
						if (basVardiyaId != bitVardiyaId) {
							if (pdksVardiyaGun.getYeniVardiya() != null) {
								pdksVardiyaGun.setVardiya(pdksVardiyaGun.getYeniVardiya());
								pdksVardiyaGun.setIslendi(Boolean.FALSE);
								pdksVardiyaGun.setIslemVardiya(null);
								pdksVardiyaGun.setIslemVardiyaZamani();
								pdksEntityController.saveOrUpdate(session, entityManager, pdksVardiyaGun);
								vardiyaGunleri.add(pdksVardiyaGun);
								try {
									pdksVardiyaGun.setIslemVardiya(null);
									pdksVardiyaGun.setIslendi(Boolean.FALSE);
									pdksVardiyaGun.setIslemVardiyaZamani();
								} catch (Exception ex) {
									logger.error(ex);
									ex.printStackTrace();
								}
							} else if (pdksVardiyaGun.getId() != null) {

								pdksEntityController.deleteObject(session, entityManager, pdksVardiyaGun);

							}
						}

					}
				}
				boolean hataOlustu = Boolean.FALSE;
				try {

					if (vardiyaPlanKontrol(vardiyaMap, pdksVardiyaPlan, personel.getSicilNo() + " " + personel.getAdSoyad() + " ", true)) {
						// PersonelDenklestirme personelDenklestirme = aylikPuantaj.getPersonelDenklestirmeAylik();
						perIdList.add(personel.getId());
						aylikPuantaj.setSecili(Boolean.FALSE);
						perNoList.add(aylikPuantaj.getPdksPersonel().getPdksSicilNo());
					} else
						hataOlustu = Boolean.TRUE;

				} catch (Exception e) {
					hataOlustu = Boolean.TRUE;

				}
				if (hataOlustu) {
					for (VardiyaGun pdksVardiyaGun : vardiyaGunleri) {
						Vardiya yeniVardiya = pdksVardiyaGun.getYeniVardiya();
						session.refresh(pdksVardiyaGun);
						pdksVardiyaGun.setYeniVardiya(yeniVardiya);
					}
				}
				session.flush();
				islemYapildi = Boolean.TRUE;
				vardiyaGunleri.clear();
			}
		}

		if (!islemYapildi)
			PdksUtil.addMessageWarn("İşlem yapılacak kayıt seçiniz!");
		else {

			try {
				if (!perNoList.isEmpty()) {
					onayDurum = Boolean.FALSE;
					vardiyaPlanDosyaOku();
					if (aylikPuantajList != null && !aylikPuantajList.isEmpty()) {
						for (AylikPuantaj aylikPuantaj : aylikPuantajList) {
							if (perNoList.contains(aylikPuantaj.getPdksPersonel().getPdksSicilNo())) {
								fillAylikVardiyaPlanList();
								break;
							}
						}

					}
					aylikPuantajDosyaList.clear();
				}

			} catch (Exception e) {
			}

		}
		perNoList = null;
		return "";
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public String vardiyaPlanDosyaOku() throws Exception {
		aylikPuantajDosyaList.clear();
		onayDurum = Boolean.FALSE;
		degisti = Boolean.FALSE;
		Workbook wb = vardiyaPlanDosya.getDosyaIcerik() != null ? ortakIslemler.getWorkbook(vardiyaPlanDosya) : null;
		if (wb != null) {
			session.clear();
			HashMap fields = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT S.* from " + Vardiya.TABLE_NAME + " S WITH(nolock) ");
			sb.append(" WHERE (S." + Vardiya.COLUMN_NAME_DEPARTMAN + " IS NULL  OR S." + Vardiya.COLUMN_NAME_DEPARTMAN + "=:deptId )");
			sb.append(" AND  S." + Vardiya.COLUMN_NAME_KISA_ADI + "<>'' AND S." + Vardiya.COLUMN_NAME_DURUM + "=1 ");
			fields.put("deptId", departman.getId());
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Vardiya> vardiyalar = pdksEntityController.getObjectBySQLList(sb, fields, Vardiya.class);
			TreeMap<String, Vardiya> vardiyaMap = new TreeMap<String, Vardiya>();
			for (Vardiya pdksVardiya : vardiyalar) {
				String key = PdksUtil.setTurkishStr(pdksVardiya.getKisaAdi()).toLowerCase(Locale.ENGLISH);
				vardiyaMap.put(key, pdksVardiya);
			}

			fields.clear();
			fields.put("ay", ay);
			fields.put("yil", yil);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
			setDenklestirmeAyDurum(fazlaMesaiOrtakIslemler.getDurum(denklestirmeAy));
			fields.clear();
			if (ay > 1) {
				fields.put("ay", ay - 1);
				fields.put("yil", yil);
			} else {
				fields.put("ay", 12);
				fields.put("yil", yil - 1);
			}

			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			denklestirmeOncekiAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
			if (denklestirmeOncekiAy != null && !denklestirmeOncekiAy.getDurum())
				denklestirmeOncekiAy = null;

			fields.clear();
			if (ay < 12) {
				fields.put("ay", ay + 1);
				fields.put("yil", yil);
			} else {
				fields.put("ay", 1);
				fields.put("yil", yil + 1);
			}

			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			denklestirmeSonrakiAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
			if (denklestirmeSonrakiAy != null && !denklestirmeSonrakiAy.getDurum())
				denklestirmeSonrakiAy = null;

			vardiyalar = null;
			if (!vardiyaMap.isEmpty() && denklestirmeAy != null) {
				denklestirmeDonemi = new DepartmanDenklestirmeDonemi();
				AylikPuantaj aylikPuantajSablon = fazlaMesaiOrtakIslemler.getAylikPuantaj(ay, yil, denklestirmeDonemi, session);
				List<VardiyaHafta> vardiyaHaftaList = new ArrayList<VardiyaHafta>();
				List<VardiyaGun> sablonVardiyalar = new ArrayList<VardiyaGun>();
				VardiyaPlan pdksVardiyaPlanMaster = fazlaMesaiOrtakIslemler.haftalikVardiyaOlustur(vardiyaHaftaList, aylikPuantajSablon, denklestirmeDonemi, tatilGunleriMap, sablonVardiyalar);
				aylikPuantajSablon.setVardiyaHaftaList(pdksVardiyaPlanMaster.getVardiyaHaftaList());
				setAylikPuantajDefault(aylikPuantajSablon);
				TreeMap<String, String> perMap = new TreeMap<String, String>();
				LinkedHashMap<String, List<String>> vardiyaListMap = new LinkedHashMap<String, List<String>>();
				Sheet sheet = wb.getSheetAt(0);
				int COL_SICIL_NO = 0;
				int COL_ADI_SOYADI = 1;
				String perSicilNo = null, adiSoyadi = null;
				int gunSayisi = COL_ADI_SOYADI + 2, maxGun = aylikPuantajSablon.getGunSayisi();
				Boolean devam = Boolean.TRUE;
				String sicilNoUzunlukStr = ortakIslemler.getParameterKey("sicilNoUzunluk");
				int maxTextLength = 0;
				try {
					if (!sicilNoUzunlukStr.equals(""))
						maxTextLength = Integer.parseInt(sicilNoUzunlukStr);
				} catch (Exception e) {
					maxTextLength = 0;
				}
				int baslangic = 0;
				String str = null;
				while (devam) {
					try {
						str = ExcelUtil.getSheetStringValue(sheet, baslangic, gunSayisi++);
						String[] gunler = str.indexOf("\n") > 0 ? str.split("\n") : str.split(" ");
						if (gunler.length == 2)
							maxGun = Integer.parseInt(gunler[0]);
						else
							maxGun = Integer.parseInt(str);
					} catch (Exception e) {
						if (baslangic == 0) {
							if (gunSayisi < 28) {
								baslangic = 2;
								gunSayisi = COL_ADI_SOYADI + 2;
							} else
								devam = false;
						} else {
							if (str != null)
								logger.debug(str + " " + maxGun);
							devam = false;
						}

					}
				}
				if (aylikPuantajSablon.getGunSayisi() == maxGun) {
					List<String> perList = new ArrayList<String>();
					String vardiyaAdi = "";
					for (int row = baslangic + 1; row <= sheet.getLastRowNum(); row++) {
						try {
							perSicilNo = ExcelUtil.getSheetStringValue(sheet, row, COL_SICIL_NO);
							if (perSicilNo == null || perSicilNo.trim().equals(""))
								break;

							adiSoyadi = ExcelUtil.getSheetStringValue(sheet, row, COL_ADI_SOYADI);
							if (adiSoyadi == null || adiSoyadi.trim().equals(""))
								break;
							if (maxTextLength > 0 && perSicilNo != null && perSicilNo.trim().length() < maxTextLength)
								perSicilNo = PdksUtil.textBaslangicinaKarakterEkle(perSicilNo, '0', maxTextLength);
							perMap.put(perSicilNo, adiSoyadi);
							List<String> list = new ArrayList<String>();
							for (int i = 0; i < maxGun; i++) {
								vardiyaAdi = ExcelUtil.getSheetStringValueTry(sheet, row, COL_ADI_SOYADI + i + 2);
								String key = null;
								if (vardiyaAdi != null && !vardiyaAdi.equals("")) {
									key = PdksUtil.setTurkishStr(vardiyaAdi).toLowerCase(Locale.ENGLISH);
									if (!vardiyaMap.containsKey(key))
										key = vardiyaAdi;
								}
								list.add(key);
							}
							vardiyaListMap.put(perSicilNo, list);
							perList.add(perSicilNo);
						} catch (Exception e1) {
							logger.error(perSicilNo + " " + e1);
							e1.printStackTrace();
						}
					}
					if (!vardiyaListMap.isEmpty() && vardiyaListMap.size() == perMap.size()) {
						Date sonGun = null;
						if (aylikPuantajSablon.getVardiyalar() != null && !aylikPuantajSablon.getVardiyalar().isEmpty())
							sonGun = aylikPuantajSablon.getVardiyalar().get(aylikPuantajSablon.getVardiyalar().size() - 1).getVardiyaDate();

						if (sonGun == null) {
							Calendar cal = Calendar.getInstance();
							cal.setTime(aylikPuantajSablon.getIlkGun());
							cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
							sonGun = cal.getTime();
						}

						fields.clear();
						sb = new StringBuffer();
						sb.append("SELECT S.* from " + Personel.TABLE_NAME + " S WITH(nolock) ");
						sb.append(" INNER JOIN " + Sirket.TABLE_NAME + " SI ON SI." + Sirket.COLUMN_NAME_ID + "=S." + Personel.COLUMN_NAME_SIRKET);
						sb.append(" AND SI." + Sirket.COLUMN_NAME_DEPARTMAN + "=:deptId ");
						sb.append(" WHERE S." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=:basTarih ");
						sb.append(" AND  S." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + "<=:bitTarih ");
						sb.append(" AND  S." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " :perList ");
						fields.put("basTarih", aylikPuantajSablon.getIlkGun());
						fields.put("deptId", departman.getId());
						fields.put("bitTarih", sonGun);
						fields.put("perList", new ArrayList(perMap.keySet()));
						fields.put(PdksEntityController.MAP_KEY_MAP, "getPdksSicilNo");
						if (session != null)
							fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						TreeMap<String, Personel> personelMap = pdksEntityController.getObjectBySQLMap(sb, fields, Personel.class, false);
						if (personelMap.size() == perMap.size()) {
							fields.clear();
							fields.put("denklestirmeAy.id", denklestirmeAy.getId());
							fields.put("personel.pdksSicilNo", new ArrayList(perMap.keySet()));
							if (session != null)
								fields.put(PdksEntityController.MAP_KEY_SESSION, session);

							List<PersonelDenklestirme> personelDenklestirmeList = pdksEntityController.getObjectByInnerObjectList(fields, PersonelDenklestirme.class);
							TreeMap<String, PersonelDenklestirme> personelDenklestirmeMap = new TreeMap<String, PersonelDenklestirme>();
							List<Long> modelIdList = new ArrayList<Long>();

							for (PersonelDenklestirme personelDenklestirme : personelDenklestirmeList) {
								if (personelDenklestirme.getCalismaModeliAy() != null && personelDenklestirme.getCalismaModeli() != null) {
									CalismaModeli calismaModeli = personelDenklestirme.getCalismaModeli();
									if (calismaModeli.getGenelVardiya().equals(Boolean.FALSE) && !modelIdList.contains(calismaModeli.getId()))
										modelIdList.add(calismaModeli.getId());
								}
								if (!personelDenklestirme.isErpAktarildi())
									personelDenklestirmeMap.put(personelDenklestirme.getPersonel().getPdksSicilNo(), personelDenklestirme);
							}
							HashMap<Long, List<Long>> calismaModeliVardiyaMap = new HashMap<Long, List<Long>>();
							if (!modelIdList.isEmpty()) {
								fields.clear();
								fields.put("calismaModeli.id", modelIdList);
								if (session != null)
									fields.put(PdksEntityController.MAP_KEY_SESSION, session);
								List<CalismaModeliVardiya> calismaModeliVardiyaList = pdksEntityController.getObjectByInnerObjectList(fields, CalismaModeliVardiya.class);
								for (CalismaModeliVardiya calismaModeliVardiya : calismaModeliVardiyaList) {
									if (calismaModeliVardiya.getVardiya().getDurum()) {
										Long key = calismaModeliVardiya.getCalismaModeli().getId();
										List<Long> list = calismaModeliVardiyaMap.containsKey(key) ? calismaModeliVardiyaMap.get(key) : new ArrayList<Long>();
										if (list.isEmpty())
											calismaModeliVardiyaMap.put(key, list);
										list.add(calismaModeliVardiya.getVardiya().getId());
									}
								}
							}
							modelIdList = null;
							personelDenklestirmeList = null;
							vardiyalarMap = ortakIslemler.getIslemVardiyalar(new ArrayList<Personel>(personelMap.values()), denklestirmeDonemi.getBaslangicTarih(), denklestirmeDonemi.getBitisTarih(), Boolean.TRUE, session, Boolean.FALSE);
							// ortakIslemler.fazlaMesaiSaatiAyarla(vardiyalarMap);
							Date sonGunVardiya = (Date) sonGun.clone();
							for (String key : vardiyalarMap.keySet()) {
								VardiyaGun pdksVardiyaGun = vardiyalarMap.get(key);
								pdksVardiyaGun.setIslendi(Boolean.FALSE);
								pdksVardiyaGun.setGuncellendi(Boolean.FALSE);
								pdksVardiyaGun.saklaVardiya();
								if (pdksVardiyaGun.getIslemVardiya() == null)
									pdksVardiyaGun.setVardiyaZamani();
								if (pdksVardiyaGun.getIslemVardiya() != null)
									if (pdksVardiyaGun.getIslemVardiya().getVardiyaBitZaman().after(sonGunVardiya))
										sonGunVardiya = pdksVardiyaGun.getIslemVardiya().getVardiyaBitZaman();
							}

							fields.clear();
							fields.put("izinTipi.bakiyeIzinTipi=", null);
							fields.put("bitisZamani>=", aylikPuantajSablon.getIlkGun());
							fields.put("baslangicZamani<=", sonGunVardiya);
							fields.put("izinDurumu not ", Arrays.asList(new Integer[] { PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL, PersonelIzin.IZIN_DURUMU_REDEDILDI }));
							fields.put("izinSahibi.pdksSicilNo", new ArrayList(perMap.keySet()));
							if (session != null)
								fields.put(PdksEntityController.MAP_KEY_SESSION, session);
							List<PersonelIzin> izinler = pdksEntityController.getObjectByInnerObjectListInLogic(fields, PersonelIzin.class);
							List<VardiyaGun> vardiyaList = new ArrayList<VardiyaGun>();

							for (String sicilNo : perList) {
								Personel personel = personelMap.get(sicilNo);
								boolean hataVar = Boolean.FALSE, renk = Boolean.TRUE;
								if (personel != null) {
									personel = (Personel) personelMap.get(sicilNo).clone();
									personel.setDurum(Boolean.TRUE);
									boolean calisiyor = personel.getIseGirisTarihi().before(aylikPuantajSablon.getIlkGun()) && personel.getIstenAyrilisTarihi().after(sonGun);
									AylikPuantaj aylikPuantajSablonNew = new AylikPuantaj();
									PersonelDenklestirme personelDenklestirmeAylik = personelDenklestirmeMap.containsKey(sicilNo) ? personelDenklestirmeMap.get(sicilNo) : null;
									aylikPuantajSablonNew.setPersonelDenklestirmeAylik(personelDenklestirmeAylik);
									aylikPuantajSablonNew.setKaydet(Boolean.FALSE);
									aylikPuantajSablonNew.setSecili(Boolean.FALSE);
									aylikPuantajSablonNew.setPdksPersonel(personel);
									aylikPuantajSablonNew.setVardiyalar(new ArrayList<VardiyaGun>());
									aylikPuantajSablonNew.setVardiyaPlan(new VardiyaPlan());
									aylikPuantajSablonNew.getVardiyaPlan().getVardiyaHaftaList().clear();
									List<VardiyaGun> puantajVardiyaGunler = new ArrayList<VardiyaGun>();
									aylikPuantajSablonNew.setVardiyalar(puantajVardiyaGunler);
									devam = !devam;
									List<Long> vardiyaIdList = null;
									try {
										if (personelDenklestirmeAylik != null && personelDenklestirmeAylik.getCalismaModeliAy() != null && personelDenklestirmeAylik.getCalismaModeli() != null) {
											CalismaModeli calismaModeli = personelDenklestirmeAylik.getCalismaModeli();
											if (calismaModeliVardiyaMap.containsKey(calismaModeli.getId()))
												vardiyaIdList = calismaModeliVardiyaMap.get(calismaModeli.getId());

										}
									} catch (Exception ee2) {
										logger.error(ee2);
										ee2.printStackTrace();
									}

									for (Iterator iterator2 = sablonVardiyalar.iterator(); iterator2.hasNext();) {
										VardiyaGun pdksVardiyaGunMaster = (VardiyaGun) iterator2.next();
										VardiyaGun pdksVardiyaGun = new VardiyaGun(personel, null, pdksVardiyaGunMaster.getVardiyaDate());
										pdksVardiyaGun.setAyinGunu(Boolean.FALSE);
										if (vardiyalarMap.containsKey(pdksVardiyaGun.getVardiyaKey()))
											pdksVardiyaGun = vardiyalarMap.get(pdksVardiyaGun.getVardiyaKey());

										String key = PdksUtil.convertToDateString(pdksVardiyaGun.getVardiyaDate(), "yyyyMMdd");
										// logger.info(key);
										if (tatilGunleriMap.containsKey(key))
											pdksVardiyaGun.setTatil(tatilGunleriMap.get(key));
										haftalikSablonOlustur(aylikPuantajSablon, false, false, null, personel, personel.getSablon(), aylikPuantajSablonNew, vardiyalarMap);
										aylikPuantajSablonNew.setVardiyaHaftaList(aylikPuantajSablonNew.getVardiyaPlan().getVardiyaHaftaList());
									}

									int gun = 0;
									List<String> list = vardiyaListMap.get(sicilNo);
									for (VardiyaGun pdksVardiyaGun : aylikPuantajSablonNew.getVardiyalar()) {
										Date vardiyaDate = pdksVardiyaGun.getVardiyaDate();
										pdksVardiyaGun.setAyinGunu(!vardiyaDate.before(aylikPuantajSablon.getIlkGun()) && !vardiyaDate.after(aylikPuantajSablon.getSonGun()));
										if (!pdksVardiyaGun.isAyinGunu())
											continue;
										String vardiyaKey = list.get(gun++);
										Vardiya pdksVardiya = null;
										if (vardiyaKey != null && !vardiyaKey.equals("")) {
											if (izinler != null && !izinler.isEmpty()) {
												vardiyaList.add(pdksVardiyaGun);
												setIzin(personel.getId(), izinler, vardiyaList);
												vardiyaList.clear();
											}
											if (pdksVardiyaGun.getIzin() != null) {
												pdksVardiya = pdksVardiyaGun.getVardiya();
												if (aylikPuantajSablonNew.getCalismaModeli() != null) {
													if (vardiyaMap.containsKey(vardiyaKey)) {
														Vardiya pdksVardiyaNew = vardiyaMap.get(vardiyaKey);
														if (!pdksVardiyaNew.getId().equals(pdksVardiya.getId())) {
															pdksVardiya = pdksVardiyaNew;
														}
													}
												}
											} else if (vardiyaMap.containsKey(vardiyaKey)) {
												pdksVardiya = vardiyaMap.get(vardiyaKey);
											} else {
												hataVar = Boolean.TRUE;
												PdksUtil.addMessageAvailableWarn(sicilNo + " " + perMap.get(sicilNo) + " " + PdksUtil.convertToDateString(vardiyaDate, "d/M/yyyyy") + " günü " + vardiyaKey + " vardiya bilgisi okunamadı!");
											}
										}
										if (vardiyaIdList != null && pdksVardiya != null && pdksVardiya.getGenel() && pdksVardiya.isCalisma() && !vardiyaIdList.contains(pdksVardiya.getId()))
											pdksVardiya = pdksVardiyaGun.getVardiya();
										pdksVardiyaGun.setYeniVardiya(pdksVardiya);
										if (!calisiyor) {
											if (!personel.isCalisiyorGun(vardiyaDate)) {
												pdksVardiyaGun.setYeniVardiya(null);
												pdksVardiyaGun.setIzin(null);
												pdksVardiyaGun.setGuncellendi(Boolean.TRUE);

											}
										}
										long basVardiyaId = (pdksVardiyaGun.getVardiya() != null ? pdksVardiyaGun.getVardiya().getIdLong() : 0L), bitVardiyaId = (pdksVardiyaGun.getYeniVardiya() != null ? pdksVardiyaGun.getYeniVardiya().getIdLong() : 0L);
										pdksVardiyaGun.setGuncellendi(bitVardiyaId > 0 && basVardiyaId != bitVardiyaId);
										if (!aylikPuantajSablonNew.isKaydet())
											aylikPuantajSablonNew.setKaydet(basVardiyaId != bitVardiyaId);

									}

									aylikPuantajSablonNew.setVardiyaOlustu(!hataVar);
									if (hataVar || aylikPuantajSablonNew.getPersonelDenklestirmeAylik() == null) {
										aylikPuantajSablonNew.setKaydet(Boolean.FALSE);
										aylikPuantajSablonNew.setTrClass("hata");
									} else {
										aylikPuantajSablonNew.setTrClass(renk ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
										renk = !renk;
									}
									if (!degisti)
										degisti = aylikPuantajSablonNew.isKaydet();
									VardiyaPlan pdksVardiyaPlan = aylikPuantajSablonNew.getVardiyaPlan();
									for (VardiyaHafta vardiyaHafta : pdksVardiyaPlan.getVardiyaHaftaList())
										vardiyaHafta.setCheckBoxDurum(true);
									aylikPuantajDosyaList.add(aylikPuantajSablonNew);

								}
							}
						} else {
							for (String sicilNo : perMap.keySet()) {
								if (!personelMap.containsKey(sicilNo))
									PdksUtil.addMessageAvailableWarn(sicilNo + " " + perMap.get(sicilNo) + " personel bilgisi okunamadı!");
							}

						}

					}
				} else {
					if (baslangic == 0) {
						baslangic = 2;
					} else {
						devam = false;
						PdksUtil.addMessageAvailableWarn("Gün sayısı uyumsuz!");
					}
				}

			}
		}
		return "";
	}

	/**
	 * @param aylikPuantajSablon
	 * @param fiush
	 * @param pdksVardiyaHaftaSave
	 * @param vardiyaHaftaMap
	 * @param personelGelen
	 * @param sablonu
	 * @param aylikPuantaj
	 * @param vardiyaGunMap
	 * @return
	 */
	private boolean haftalikSablonOlustur(AylikPuantaj aylikPuantajSablon, boolean fiush, boolean pdksVardiyaHaftaSave, TreeMap<String, VardiyaHafta> vardiyaHaftaMap, Personel personelGelen, VardiyaSablonu sablonu, AylikPuantaj aylikPuantaj, TreeMap<String, VardiyaGun> vardiyaGunAllMap) {
		boolean haftaRenk = true;
		Personel personel = (Personel) personelGelen.clone();
		personel.setDurum(Boolean.TRUE);
		boolean calisiyorPersonel = aylikPuantajSablon.getSonGun().after(personel.getIseGirisTarihi()) && personel.getIstenAyrilisTarihi().after(aylikPuantajSablon.getIlkGun());
		if (vardiyaHaftaMap == null)
			vardiyaHaftaMap = new TreeMap<String, VardiyaHafta>();
		VardiyaPlan plan = aylikPuantaj.getVardiyaPlan();
		TreeMap<String, VardiyaGun> vardiyaGunMap = plan.getVardiyaGunMap();
		if (vardiyaGunMap == null) {
			vardiyaGunMap = new TreeMap<String, VardiyaGun>();
			plan.setVardiyaGunMap(vardiyaGunMap);
		} else
			vardiyaGunMap.clear();
		List<VardiyaHafta> vardiyaHaftaList = plan.getVardiyaHaftaList();
		if (vardiyaHaftaList == null) {
			vardiyaHaftaList = new ArrayList<VardiyaHafta>();
			plan.setVardiyaHaftaList(vardiyaHaftaList);
		} else
			vardiyaHaftaList.clear();
		aylikPuantaj.setVardiyaHaftaList(vardiyaHaftaList);
		long iseBasTarih = Long.parseLong(PdksUtil.convertToDateString(personel.getIseGirisTarihi(), "yyyyMMdd"));
		long istenAyrilmaTarih = Long.parseLong(PdksUtil.convertToDateString(personel.getSonCalismaTarihi(), "yyyyMMdd"));

		for (Iterator iterator = aylikPuantajSablon.getVardiyaHaftaList().iterator(); iterator.hasNext();) {
			VardiyaHafta pdksVardiyaHaftaMaster = (VardiyaHafta) iterator.next();
			long planTarih1 = Long.parseLong(PdksUtil.convertToDateString(pdksVardiyaHaftaMaster.getBasTarih(), "yyyyMMdd"));
			long planTarih2 = Long.parseLong(PdksUtil.convertToDateString(pdksVardiyaHaftaMaster.getBitTarih(), "yyyyMMdd"));
			VardiyaHafta pdksVardiyaHafta = (VardiyaHafta) pdksVardiyaHaftaMaster.clone();
			pdksVardiyaHafta.setPersonel(personel);
			pdksVardiyaHafta.setTrClass(haftaRenk ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
			haftaRenk = !haftaRenk;
			pdksVardiyaHafta.setHafta(pdksVardiyaHaftaMaster.getHafta());
			pdksVardiyaHafta.setVardiyaGunler(new ArrayList<VardiyaGun>());
			vardiyaHaftaList.add(pdksVardiyaHafta);
			List<VardiyaGun> haftaVardiyaGunleri = pdksVardiyaHafta.getVardiyaGunler();
			for (VardiyaGun pdksVardiyaGunMaster : pdksVardiyaHaftaMaster.getVardiyaGunler()) {
				VardiyaGun pdksVardiyaGun2 = new VardiyaGun(personel, null, pdksVardiyaGunMaster.getVardiyaDate());
				haftaVardiyaGunleri.add(pdksVardiyaGun2);
			}

			boolean calisiyor = planTarih2 >= iseBasTarih && istenAyrilmaTarih >= planTarih1;
			if (!calisiyor) {
				if (pdksVardiyaHafta.getId() != null) {

					pdksEntityController.deleteObject(session, entityManager, pdksVardiyaHafta);

					pdksVardiyaHafta.setId(null);
					fiush = true;
				}

			} else {

				if (pdksVardiyaHafta.getId() == null && sablonu != null) {
					pdksVardiyaHafta.setVardiyaSablonu(sablonu);
					pdksVardiyaHafta.setOlusturanUser(authenticatedUser);
					if (pdksVardiyaHaftaSave) {
						pdksEntityController.saveOrUpdate(session, entityManager, pdksVardiyaHafta);
						fiush = true;
					}
				}
			}

			for (int i = 0; i < haftaVardiyaGunleri.size(); i++) {
				VardiyaGun pdksVardiyaGun = haftaVardiyaGunleri.get(i);
				int haftaGunu = PdksUtil.getDateField(pdksVardiyaGun.getVardiyaDate(), Calendar.DAY_OF_WEEK);
				if (pdksVardiyaGun.getId() == null) {
					if (vardiyaGunAllMap.containsKey(pdksVardiyaGun.getVardiyaKeyStr())) {
						pdksVardiyaGun = vardiyaGunAllMap.get(pdksVardiyaGun.getVardiyaKeyStr());
					} else if (sablonu != null) {
						switch (haftaGunu) {
						case Calendar.MONDAY:
							pdksVardiyaGun.setVardiya(sablonu.getVardiya1());
							break;
						case Calendar.TUESDAY:
							pdksVardiyaGun.setVardiya(sablonu.getVardiya2());
							break;
						case Calendar.WEDNESDAY:
							pdksVardiyaGun.setVardiya(sablonu.getVardiya3());
							break;
						case Calendar.THURSDAY:
							pdksVardiyaGun.setVardiya(sablonu.getVardiya4());
							break;
						case Calendar.FRIDAY:
							pdksVardiyaGun.setVardiya(sablonu.getVardiya5());
							break;
						case Calendar.SATURDAY:
							pdksVardiyaGun.setVardiya(sablonu.getVardiya6());
							break;
						case Calendar.SUNDAY:
							pdksVardiyaGun.setVardiya(sablonu.getVardiya7());
							break;
						default:
							break;
						}
					}
					haftaVardiyaGunleri.set(i, pdksVardiyaGun);
				}
				String key = PdksUtil.convertToDateString(pdksVardiyaGun.getVardiyaDate(), "yyyyMMdd");
				if (!calisiyorPersonel) {
					if (!personel.isCalisiyorGun(pdksVardiyaGun.getVardiyaDate()))
						pdksVardiyaGun.setVardiya(null);
				}
				vardiyaGunMap.put(key, pdksVardiyaGun);
				switch (haftaGunu) {
				case Calendar.MONDAY:
					pdksVardiyaHafta.setVardiyaGun1(pdksVardiyaGun);
					break;
				case Calendar.TUESDAY:
					pdksVardiyaHafta.setVardiyaGun2(pdksVardiyaGun);
					break;
				case Calendar.WEDNESDAY:
					pdksVardiyaHafta.setVardiyaGun3(pdksVardiyaGun);
					break;
				case Calendar.THURSDAY:
					pdksVardiyaHafta.setVardiyaGun4(pdksVardiyaGun);
					break;
				case Calendar.FRIDAY:
					pdksVardiyaHafta.setVardiyaGun5(pdksVardiyaGun);
					break;
				case Calendar.SATURDAY:
					pdksVardiyaHafta.setVardiyaGun6(pdksVardiyaGun);
					break;
				case Calendar.SUNDAY:
					pdksVardiyaHafta.setVardiyaGun7(pdksVardiyaGun);
					break;
				default:
					break;
				}
			}

		}
		aylikPuantaj.setVardiyalar(new ArrayList<VardiyaGun>(vardiyaGunMap.values()));
		return fiush;
	}

	public Date getHaftaTatili() {
		return haftaTatili;
	}

	public void setHaftaTatili(Date haftaTatili) {
		this.haftaTatili = haftaTatili;
	}

	public Boolean getKaydet() {
		return kaydet;
	}

	public void setKaydet(Boolean kaydet) {
		this.kaydet = kaydet;
	}

	public ArrayList<Date> getIslemGunleri() {
		return islemGunleri;
	}

	public void setIslemGunleri(ArrayList<Date> islemGunleri) {
		this.islemGunleri = islemGunleri;
	}

	public List<VardiyaPlan> getVardiyaPlanList() {
		return vardiyaPlanList;
	}

	public void setVardiyaPlanList(List<VardiyaPlan> vardiyaPlanList) {
		this.vardiyaPlanList = vardiyaPlanList;
	}

	public VardiyaPlan getVardiyaPlan() {
		return vardiyaPlan;
	}

	public void setVardiyaPlan(VardiyaPlan vardiyaPlan) {
		this.vardiyaPlan = vardiyaPlan;
	}

	public VardiyaHafta getVardiyaHafta1() {
		return vardiyaHafta1;
	}

	public void setVardiyaHafta1(VardiyaHafta vardiyaHafta1) {
		this.vardiyaHafta1 = vardiyaHafta1;
	}

	public VardiyaHafta getVardiyaHafta2() {
		return vardiyaHafta2;
	}

	public void setVardiyaHafta2(VardiyaHafta vardiyaHafta2) {
		this.vardiyaHafta2 = vardiyaHafta2;
	}

	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman departman) {
		this.departman = departman;
	}

	public List<Vardiya> getCalismaOlmayanVardiyaList() {
		return calismaOlmayanVardiyaList;
	}

	public void setCalismaOlmayanVardiyaList(List<Vardiya> calismaOlmayanVardiyaList) {
		this.calismaOlmayanVardiyaList = calismaOlmayanVardiyaList;
	}

	public TreeMap<String, Tatil> getTatilMap() {
		return tatilMap;
	}

	public void setTatilMap(TreeMap<String, Tatil> tatilMap) {
		this.tatilMap = tatilMap;
	}

	public String getSicilNo() {
		return sicilNo;
	}

	public void setSicilNo(String sicilNo) {
		this.sicilNo = sicilNo;
	}

	public Boolean getDegisti() {
		return degisti;
	}

	public void setDegisti(Boolean degisti) {
		this.degisti = degisti;
	}

	public void setDenklestirmeHesapla(boolean denklestirmeHesapla) {
		this.denklestirmeHesapla = denklestirmeHesapla;
	}

	public DepartmanDenklestirmeDonemi getDenklestirmeDonemi() {
		return denklestirmeDonemi;
	}

	public void setDenklestirmeDonemi(DepartmanDenklestirmeDonemi denklestirmeDonemi) {
		this.denklestirmeDonemi = denklestirmeDonemi;
	}

	public TreeMap<String, Tanim> getEkSahaTanimMap() {
		return ekSahaTanimMap;
	}

	public void setEkSahaTanimMap(TreeMap<String, Tanim> ekSahaTanimMap) {
		this.ekSahaTanimMap = ekSahaTanimMap;
	}

	public void setVardiyaGuncelle(boolean vardiyaGuncelle) {
		this.vardiyaGuncelle = vardiyaGuncelle;
	}

	public int getAy() {
		return ay;
	}

	public void setAy(int ay) {
		this.ay = ay;
	}

	public int getYil() {
		return yil;
	}

	public void setYil(int yil) {
		this.yil = yil;
	}

	public List<SelectItem> getAylar() {
		return aylar;
	}

	public void setAylar(List<SelectItem> aylar) {
		this.aylar = aylar;
	}

	public int getMaxYil() {
		return maxYil;
	}

	public void setMaxYil(int maxYil) {
		this.maxYil = maxYil;
	}

	public AylikPuantaj getAylikPuantajDefault() {
		return aylikPuantajDefault;
	}

	public void setAylikPuantajDefault(AylikPuantaj aylikPuantajDefault) {
		this.aylikPuantajDefault = aylikPuantajDefault;
	}

	public List<AylikPuantaj> getAylikPuantajList() {
		return aylikPuantajList;
	}

	public void setAylikPuantajList(List<AylikPuantaj> aylikPuantajList) {
		this.aylikPuantajList = aylikPuantajList;
	}

	public AylikPuantaj getPersonelAylikPuantaj() {
		return personelAylikPuantaj;
	}

	public void setPersonelAylikPuantaj(AylikPuantaj personelAylikPuantaj) {
		this.personelAylikPuantaj = personelAylikPuantaj;
	}

	public DenklestirmeAy getDenklestirmeAy() {
		return denklestirmeAy;
	}

	public void setDenklestirmeAy(DenklestirmeAy denklestirmeAy) {
		this.denklestirmeAy = denklestirmeAy;
	}

	public double getHaftaTatilSaat() {
		return haftaTatilSaat;
	}

	public void setHaftaTatilSaat(double haftaTatilSaat) {
		this.haftaTatilSaat = haftaTatilSaat;
	}

	public VardiyaGun getSeciliVardiyaGun() {
		return seciliVardiyaGun;
	}

	public void setSeciliVardiyaGun(VardiyaGun seciliVardiyaGun) {
		this.seciliVardiyaGun = seciliVardiyaGun;
	}

	public void setGunSec(boolean gunSec) {
		this.gunSec = gunSec;
	}

	public HashMap<String, Personel> getGorevliPersonelMap() {
		return gorevliPersonelMap;
	}

	public void setGorevliPersonelMap(HashMap<String, Personel> gorevliPersonelMap) {
		this.gorevliPersonelMap = gorevliPersonelMap;
	}

	public void setGorevli(boolean gorevli) {
		this.gorevli = gorevli;
	}

	public Tanim getGorevYeri() {
		return gorevYeri;
	}

	public void setGorevYeri(Tanim gorevYeri) {
		this.gorevYeri = gorevYeri;
	}

	public void setHastaneSuperVisor(boolean hastaneSuperVisor) {
		this.hastaneSuperVisor = hastaneSuperVisor;
	}

	public void setOzelIstek(boolean ozelIstek) {
		this.ozelIstek = ozelIstek;
	}

	public List<User> getToList() {
		return toList;
	}

	public void setToList(List<User> toList) {
		this.toList = toList;
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

	public File getEkliDosya() {
		return ekliDosya;
	}

	public void setEkliDosya(File ekliDosya) {
		this.ekliDosya = ekliDosya;
	}

	public Date getBasTarih() {
		return basTarih;
	}

	public void setBasTarih(Date basTarih) {
		this.basTarih = basTarih;
	}

	public Date getBitTarih() {
		return bitTarih;
	}

	public void setBitTarih(Date bitTarih) {
		this.bitTarih = bitTarih;
	}

	public List<VardiyaGun> getVardiyaGunList() {
		return vardiyaGunList;
	}

	public void setVardiyaGunList(List<VardiyaGun> vardiyaGunList) {
		this.vardiyaGunList = vardiyaGunList;
	}

	public String getDosyaAdi() {
		return dosyaAdi;
	}

	public void setDosyaAdi(String dosyaAdi) {
		this.dosyaAdi = dosyaAdi;
	}

	public ByteArrayOutputStream getBaosDosya() {
		return baosDosya;
	}

	public void setBaosDosya(ByteArrayOutputStream baosDosya) {
		this.baosDosya = baosDosya;
	}

	public void setOnayDurum(boolean onayDurum) {
		this.onayDurum = onayDurum;
	}

	public String getMailKonu() {
		return mailKonu;
	}

	public void setMailKonu(String mailKonu) {
		this.mailKonu = mailKonu;
	}

	public String getMailIcerik() {
		return mailIcerik;
	}

	public void setMailIcerik(String mailIcerik) {
		this.mailIcerik = mailIcerik;
	}

	public byte[] getMailData() {
		return mailData;
	}

	public void setMailData(byte[] mailData) {
		this.mailData = mailData;
	}

	public Tanim getPlanDepartman() {
		return planDepartman;
	}

	public void setPlanDepartman(Tanim planDepartman) {
		this.planDepartman = planDepartman;
	}

	public List<SelectItem> getOzelDurumList() {
		return ozelDurumList;
	}

	public void setOzelDurumList(List<SelectItem> ozelDurumList) {
		this.ozelDurumList = ozelDurumList;
	}

	public List<BolumKat> getBolumKatlari() {
		return bolumKatlari;
	}

	public void setBolumKatlari(List<BolumKat> bolumKatlari) {
		this.bolumKatlari = bolumKatlari;
	}

	public List<Tanim> getGorevYeriTanimList() {
		return gorevYeriTanimList;
	}

	public void setGorevYeriTanimList(List<Tanim> gorevYeriTanimList) {
		this.gorevYeriTanimList = gorevYeriTanimList;
	}

	public List<Long> getGorevYerileri() {
		return gorevYerileri;
	}

	public void setGorevYerileri(List<Long> gorevYerileri) {
		this.gorevYerileri = gorevYerileri;
	}

	public void setResmiTatilVar(boolean resmiTatilVar) {
		this.resmiTatilVar = resmiTatilVar;
	}

	public void setSutIzniGoster(boolean sutIzniGoster) {
		this.sutIzniGoster = sutIzniGoster;
	}

	public TreeMap<String, VardiyaGun> getVardiyalarMap() {
		return vardiyalarMap;
	}

	public void setVardiyalarMap(TreeMap<String, VardiyaGun> vardiyalarMap) {
		this.vardiyalarMap = vardiyalarMap;
	}

	public void setIslemYapiliyor(boolean islemYapiliyor) {
		this.islemYapiliyor = islemYapiliyor;
	}

	public void setPlanGirisi(boolean planGirisi) {
		this.planGirisi = planGirisi;
	}

	public Long getGorevTipiId() {
		return gorevTipiId;
	}

	public void setGorevTipiId(Long gorevTipiId) {
		this.gorevTipiId = gorevTipiId;
	}

	public void setSablonGuncelle(boolean sablonGuncelle) {
		this.sablonGuncelle = sablonGuncelle;
	}

	public TreeMap<String, Vardiya> getKayitliVardiyalarMap() {
		return kayitliVardiyalarMap;
	}

	public void setKayitliVardiyalarMap(TreeMap<String, Vardiya> kayitliVardiyalarMap) {
		this.kayitliVardiyalarMap = kayitliVardiyalarMap;
	}

	public void setVeriGuncellendi(boolean veriGuncellendi) {
		this.veriGuncellendi = veriGuncellendi;
	}

	public String getDonusAdres() {
		return donusAdres;
	}

	public void setDonusAdres(String donusAdres) {
		this.donusAdres = donusAdres;
	}

	public void setPartTimeGoster(boolean partTimeGoster) {
		this.partTimeGoster = partTimeGoster;
	}

	public void setFileImport(boolean fileImport) {
		this.fileImport = fileImport;
	}

	public Dosya getVardiyaPlanDosya() {
		return vardiyaPlanDosya;
	}

	public void setVardiyaPlanDosya(Dosya vardiyaPlanDosya) {
		this.vardiyaPlanDosya = vardiyaPlanDosya;
	}

	public List<AylikPuantaj> getAylikPuantajDosyaList() {
		return aylikPuantajDosyaList;
	}

	public void setAylikPuantajDosyaList(List<AylikPuantaj> aylikPuantajDosyaList) {
		this.aylikPuantajDosyaList = aylikPuantajDosyaList;
	}

	public void setStajerSirket(boolean stajerSirket) {
		this.stajerSirket = stajerSirket;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public AramaSecenekleri getAramaSecenekleri() {
		return aramaSecenekleri;
	}

	public void setAramaSecenekleri(AramaSecenekleri aramaSecenekleri) {
		this.aramaSecenekleri = aramaSecenekleri;
	}

	public void setAksamGunVar(boolean aksamGunVar) {
		this.aksamGunVar = aksamGunVar;
	}

	public void setAksamSaatVar(boolean aksamSaatVar) {
		this.aksamSaatVar = aksamSaatVar;
	}

	public void setHaftaTatilVar(boolean haftaTatilVar) {
		this.haftaTatilVar = haftaTatilVar;
	}

	public Boolean getDenklestirmeHesapla() {
		return denklestirmeHesapla;
	}

	public void setDenklestirmeHesapla(Boolean denklestirmeHesapla) {
		this.denklestirmeHesapla = denklestirmeHesapla;
	}

	public Boolean getGunSec() {
		return gunSec;
	}

	public void setGunSec(Boolean gunSec) {
		this.gunSec = gunSec;
	}

	public Boolean getGorevli() {
		return gorevli;
	}

	public void setGorevli(Boolean gorevli) {
		this.gorevli = gorevli;
	}

	public Boolean getOzelIstek() {
		return ozelIstek;
	}

	public void setOzelIstek(Boolean ozelIstek) {
		this.ozelIstek = ozelIstek;
	}

	public Boolean getIslemYapiliyor() {
		return islemYapiliyor;
	}

	public void setIslemYapiliyor(Boolean islemYapiliyor) {
		this.islemYapiliyor = islemYapiliyor;
	}

	public Boolean getResmiTatilVar() {
		return resmiTatilVar;
	}

	public void setResmiTatilVar(Boolean resmiTatilVar) {
		this.resmiTatilVar = resmiTatilVar;
	}

	public Boolean getAksamGunVar() {
		return aksamGunVar;
	}

	public void setAksamGunVar(Boolean aksamGunVar) {
		this.aksamGunVar = aksamGunVar;
	}

	public Boolean getAksamSaatVar() {
		return aksamSaatVar;
	}

	public void setAksamSaatVar(Boolean aksamSaatVar) {
		this.aksamSaatVar = aksamSaatVar;
	}

	public Boolean getHaftaTatilVar() {
		return haftaTatilVar;
	}

	public void setHaftaTatilVar(Boolean haftaTatilVar) {
		this.haftaTatilVar = haftaTatilVar;
	}

	public boolean isFileImport() {
		return fileImport;
	}

	public boolean isVardiyaVar() {
		return vardiyaVar;
	}

	public boolean isVardiyaGuncelle() {
		return vardiyaGuncelle;
	}

	public boolean isHastaneSuperVisor() {
		return hastaneSuperVisor;
	}

	public boolean isOnayDurum() {
		return onayDurum;
	}

	public boolean isPartTimeGoster() {
		return partTimeGoster;
	}

	public boolean isSutIzniGoster() {
		return sutIzniGoster;
	}

	public boolean isPlanGirisi() {
		return planGirisi;
	}

	public boolean isSablonGuncelle() {
		return sablonGuncelle;
	}

	public boolean isVeriGuncellendi() {
		return veriGuncellendi;
	}

	public boolean isStajerSirket() {
		return stajerSirket;
	}

	public List<CalismaModeliAy> getModelList() {
		return modelList;
	}

	public void setModelList(List<CalismaModeliAy> modelList) {
		this.modelList = modelList;
	}

	public List<SelectItem> getGunler() {
		return gunler;
	}

	public void setGunler(List<SelectItem> gunler) {
		this.gunler = gunler;
	}

	public String getBasTarihStr() {
		return basTarihStr;
	}

	public void setBasTarihStr(String basTarihStr) {
		this.basTarihStr = basTarihStr;
	}

	public String getBitTarihStr() {
		return bitTarihStr;
	}

	public void setBitTarihStr(String bitTarihStr) {
		this.bitTarihStr = bitTarihStr;
	}

	public Boolean getDepartmanBolumAyni() {
		return departmanBolumAyni;
	}

	public void setDepartmanBolumAyni(Boolean departmanBolumAyni) {
		this.departmanBolumAyni = departmanBolumAyni;
	}

	public boolean isHaftaTatilMesaiDurum() {
		return haftaTatilMesaiDurum;
	}

	public void setHaftaTatilMesaiDurum(boolean haftaTatilMesaiDurum) {
		this.haftaTatilMesaiDurum = haftaTatilMesaiDurum;
	}

	public boolean isModelGoster() {
		return modelGoster;
	}

	public void setModelGoster(boolean modelGoster) {
		this.modelGoster = modelGoster;
	}

	public Integer getAksamVardiyaBasSaat() {
		return aksamVardiyaBasSaat;
	}

	public void setAksamVardiyaBasSaat(Integer aksamVardiyaBasSaat) {
		this.aksamVardiyaBasSaat = aksamVardiyaBasSaat;
	}

	public Integer getAksamVardiyaBitSaat() {
		return aksamVardiyaBitSaat;
	}

	public void setAksamVardiyaBitSaat(Integer aksamVardiyaBitSaat) {
		this.aksamVardiyaBitSaat = aksamVardiyaBitSaat;
	}

	public Integer getAksamVardiyaBasDakika() {
		return aksamVardiyaBasDakika;
	}

	public void setAksamVardiyaBasDakika(Integer aksamVardiyaBasDakika) {
		this.aksamVardiyaBasDakika = aksamVardiyaBasDakika;
	}

	public Integer getAksamVardiyaBitDakika() {
		return aksamVardiyaBitDakika;
	}

	public void setAksamVardiyaBitDakika(Integer aksamVardiyaBitDakika) {
		this.aksamVardiyaBitDakika = aksamVardiyaBitDakika;
	}

	public TreeMap<String, Tanim> getFazlaMesaiMap() {
		return fazlaMesaiMap;
	}

	public void setFazlaMesaiMap(TreeMap<String, Tanim> fazlaMesaiMap) {
		this.fazlaMesaiMap = fazlaMesaiMap;
	}

	public boolean isMailGonder() {
		return mailGonder;
	}

	public void setMailGonder(boolean mailGonder) {
		this.mailGonder = mailGonder;
	}

	public boolean isSeciliDurum() {
		return seciliDurum;
	}

	public void setSeciliDurum(boolean seciliDurum) {
		this.seciliDurum = seciliDurum;
	}

	public String getSanalPersonelAciklama() {
		return sanalPersonelAciklama;
	}

	public void setSanalPersonelAciklama(String sanalPersonelAciklama) {
		this.sanalPersonelAciklama = sanalPersonelAciklama;
	}

	public Boolean getKullaniciPersonel() {
		return kullaniciPersonel;
	}

	public void setKullaniciPersonel(Boolean kullaniciPersonel) {
		this.kullaniciPersonel = kullaniciPersonel;
	}

	public List<Vardiya> getIzinTipiVardiyaList() {
		return izinTipiVardiyaList;
	}

	public void setIzinTipiVardiyaList(List<Vardiya> izinTipiVardiyaList) {
		this.izinTipiVardiyaList = izinTipiVardiyaList;
	}

	public TreeMap<String, TreeMap<String, List<VardiyaGun>>> getIzinTipiPersonelVardiyaMap() {
		return izinTipiPersonelVardiyaMap;
	}

	public void setIzinTipiPersonelVardiyaMap(TreeMap<String, TreeMap<String, List<VardiyaGun>>> izinTipiPersonelVardiyaMap) {
		this.izinTipiPersonelVardiyaMap = izinTipiPersonelVardiyaMap;
	}

	public List<PersonelDenklestirmeDinamikAlan> getPersonelDenklestirmeDinamikAlanList() {
		return personelDenklestirmeDinamikAlanList;
	}

	public void setPersonelDenklestirmeDinamikAlanList(List<PersonelDenklestirmeDinamikAlan> personelDenklestirmeDinamikAlanList) {
		this.personelDenklestirmeDinamikAlanList = personelDenklestirmeDinamikAlanList;
	}

	public Boolean getDenklestirmeAyDurum() {
		return denklestirmeAyDurum;
	}

	public void setDenklestirmeAyDurum(Boolean denklestirmeAyDurum) {
		this.denklestirmeAyDurum = denklestirmeAyDurum;
	}

	public DenklestirmeAy getDenklestirmeOncekiAy() {
		return denklestirmeOncekiAy;
	}

	public void setDenklestirmeOncekiAy(DenklestirmeAy denklestirmeOncekiAy) {
		this.denklestirmeOncekiAy = denklestirmeOncekiAy;
	}

	public DenklestirmeAy getDenklestirmeSonrakiAy() {
		return denklestirmeSonrakiAy;
	}

	public void setDenklestirmeSonrakiAy(DenklestirmeAy denklestirmeSonrakiAy) {
		this.denklestirmeSonrakiAy = denklestirmeSonrakiAy;
	}

	public boolean isManuelVardiyaIzinGir() {
		return manuelVardiyaIzinGir;
	}

	public void setManuelVardiyaIzinGir(boolean manuelVardiyaIzinGir) {
		this.manuelVardiyaIzinGir = manuelVardiyaIzinGir;
	}

	public boolean isAdminRole() {
		return adminRole;
	}

	public void setAdminRole(boolean adminRole) {
		this.adminRole = adminRole;
	}

	public boolean isIkRole() {
		return ikRole;
	}

	public void setIkRole(boolean ikRole) {
		this.ikRole = ikRole;
	}

	public boolean isGorevYeriGirisDurum() {
		return gorevYeriGirisDurum;
	}

	public void setGorevYeriGirisDurum(boolean gorevYeriGirisDurum) {
		this.gorevYeriGirisDurum = gorevYeriGirisDurum;
	}

	public Boolean getFazlaMesaiTalepDurum() {
		return fazlaMesaiTalepDurum;
	}

	public void setFazlaMesaiTalepDurum(Boolean fazlaMesaiTalepDurum) {
		this.fazlaMesaiTalepDurum = fazlaMesaiTalepDurum;
	}

	public boolean isFazlaMesaiTarihGuncelle() {
		return fazlaMesaiTarihGuncelle;
	}

	public void setFazlaMesaiTarihGuncelle(boolean fazlaMesaiTarihGuncelle) {
		this.fazlaMesaiTarihGuncelle = fazlaMesaiTarihGuncelle;
	}

	public HashMap<Double, ArrayList<Vardiya>> getVardiyaMap() {
		return vardiyaMap;
	}

	public void setVardiyaMap(HashMap<Double, ArrayList<Vardiya>> vardiyaMap) {
		this.vardiyaMap = vardiyaMap;
	}

	public void setVardiyaVar(boolean vardiyaVar) {
		this.vardiyaVar = vardiyaVar;
	}

	public List<Vardiya> getVardiyaList() {
		return vardiyaList;
	}

	public void setVardiyaList(List<Vardiya> vardiyaList) {
		this.vardiyaList = vardiyaList;
	}

	public List<VardiyaSablonu> getSablonList() {
		return sablonList;
	}

	public void setSablonList(List<VardiyaSablonu> sablonList) {
		this.sablonList = sablonList;
	}

	public boolean isGebeSutIzniGuncelle() {
		return gebeSutIzniGuncelle;
	}

	public void setGebeSutIzniGuncelle(boolean gebeSutIzniGuncelle) {
		this.gebeSutIzniGuncelle = gebeSutIzniGuncelle;
	}

	public boolean isGebeGoster() {
		return gebeGoster;
	}

	public void setGebeGoster(boolean gebeGoster) {
		this.gebeGoster = gebeGoster;
	}

	public boolean isOffIzinGuncelle() {
		return offIzinGuncelle;
	}

	public void setOffIzinGuncelle(boolean offIzinGuncelle) {
		this.offIzinGuncelle = offIzinGuncelle;
	}

	public TreeMap<String, Tatil> getTatilGunleriMap() {
		return tatilGunleriMap;
	}

	public void setTatilGunleriMap(TreeMap<String, Tatil> tatilGunleriMap) {
		this.tatilGunleriMap = tatilGunleriMap;
	}

	public HashMap<String, List<Tanim>> getEkSahaListMap() {
		return ekSahaListMap;
	}

	public void setEkSahaListMap(HashMap<String, List<Tanim>> ekSahaListMap) {
		this.ekSahaListMap = ekSahaListMap;
	}

	public int getSonDonem() {
		return sonDonem;
	}

	public void setSonDonem(int sonDonem) {
		this.sonDonem = sonDonem;
	}

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}
}
