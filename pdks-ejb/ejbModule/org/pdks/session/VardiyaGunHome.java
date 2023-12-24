package org.pdks.session;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFFont;
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
import org.pdks.entity.BordroDetayTipi;
import org.pdks.entity.CalismaModeli;
import org.pdks.entity.CalismaModeliAy;
import org.pdks.entity.CalismaModeliVardiya;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Departman;
import org.pdks.entity.DepartmanDenklestirmeDonemi;
import org.pdks.entity.DepartmanMailGrubu;
import org.pdks.entity.Dosya;
import org.pdks.entity.FazlaMesaiTalep;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Kapi;
import org.pdks.entity.KapiKGS;
import org.pdks.entity.KapiView;
import org.pdks.entity.Liste;
import org.pdks.entity.NoteTipi;
import org.pdks.entity.Notice;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelDenklestirmeBordro;
import org.pdks.entity.PersonelDenklestirmeBordroDetay;
import org.pdks.entity.PersonelDenklestirmeDinamikAlan;
import org.pdks.entity.PersonelDenklestirmeTasiyici;
import org.pdks.entity.PersonelExtra;
import org.pdks.entity.PersonelFazlaMesai;
import org.pdks.entity.PersonelHareket;
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
import org.pdks.security.action.UserHome;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;
import org.primefaces.event.FileUploadEvent;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

import com.pdks.webservice.MailFile;
import com.pdks.webservice.MailObject;
import com.pdks.webservice.MailStatu;

@Name("vardiyaGunHome")
public class VardiyaGunHome extends EntityHome<VardiyaPlan> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5067953117682032644L;
	static Logger logger = Logger.getLogger(VardiyaGunHome.class);
	@RequestParameter
	Long pdksVardiyaGunId;

	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = false, create = true)
	UserHome userHome;
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

	private List<SelectItem> fazlaMesaiTalepDurumList;

	private TreeMap<String, Boolean> baslikMap;

	private List<VardiyaGun> vardiyaGunList = new ArrayList<VardiyaGun>(), aylikVardiyaOzetList;

	private List<Vardiya> vardiyaList = new ArrayList<Vardiya>();

	protected List<Vardiya> vardiyaBolumList = new ArrayList<Vardiya>();

	private FazlaMesaiTalep fazlaMesaiTalep, islemFazlaMesaiTalep;

	private HashMap<String, List<Tanim>> ekSahaListMap;

	private boolean fileImport = Boolean.FALSE, fazlaMesaiTalepVar = Boolean.FALSE, modelGoster = Boolean.FALSE, gebeGoster = Boolean.FALSE;

	private Boolean manuelHareketEkle, vardiyaFazlaMesaiTalepGoster = Boolean.FALSE, yoneticiERP1Kontrol = Boolean.FALSE, bordroPuantajEkranindaGoster = Boolean.FALSE;

	private boolean adminRole, ikRole, gorevYeriGirisDurum, fazlaMesaiTarihGuncelle = Boolean.FALSE, offIzinGuncelle = Boolean.FALSE, gebeSutIzniGuncelle = Boolean.FALSE;

	private Dosya vardiyaPlanDosya = new Dosya();

	private HashMap<Double, ArrayList<Vardiya>> vardiyaMap = new HashMap<Double, ArrayList<Vardiya>>();

	private List<VardiyaSablonu> sablonList = new ArrayList<VardiyaSablonu>();

	private List<BolumKat> bolumKatlari;

	private List<HareketKGS> hareketPdksList = null;

	private TreeMap<String, Tatil> tatilGunleriMap, tatilMap;

	private List<CalismaModeliAy> modelList;

	private List<FazlaMesaiTalep> aylikFazlaMesaiTalepler;

	private List<Vardiya> calismaOlmayanVardiyaList;

	private boolean vardiyaVar = Boolean.FALSE, seciliDurum, mailGonder, mesaiOnayla, haftaTatilMesaiDurum = Boolean.FALSE, vardiyaGuncelle = Boolean.FALSE, hastaneSuperVisor = Boolean.FALSE;
	private boolean fazlaMesaiIzinRaporuDurum, onayDurum = Boolean.FALSE, partTimeGoster = Boolean.FALSE, sutIzniGoster = Boolean.FALSE, planGirisi, sablonGuncelle, veriGuncellendi;

	private ArrayList<Date> islemGunleri;

	private AylikPuantaj aylikPuantajDonem;

	private List<FazlaMesaiTalep> fazlaMesaiTalepler;

	private KapiView manuelGiris = null, manuelCikis = null;

	private Date basTarih, bitTarih;

	private String basTarihStr, bitTarihStr, sanalPersonelAciklama, bolumAciklama, altBolumAciklama, tesisAciklama;

	private List<SelectItem> gunler;

	private VardiyaGun seciliVardiyaGun;

	private ByteArrayOutputStream baosDosya;

	private List<Long> gorevYerileri;

	private VardiyaHafta vardiyaHafta1, vardiyaHafta2;

	private User loginUser = null;

	private String sicilNo = "", dosyaAdi, donusAdres = "";

	private Boolean denklestirmeHesapla = Boolean.FALSE, gunSec = Boolean.FALSE, gorevli = false, ozelIstek = Boolean.FALSE, islemYapiliyor = Boolean.FALSE, departmanBolumAyni = Boolean.FALSE;

	private Boolean resmiTatilVar = Boolean.FALSE, eksikMaasGoster = Boolean.FALSE, aksamGunVar = Boolean.FALSE, aksamSaatVar = Boolean.FALSE, haftaTatilVar = Boolean.FALSE;

	private Boolean topluFazlaCalismaTalep = Boolean.FALSE, denklestirmeAyDurum = Boolean.FALSE, fazlaMesaiTalepDurum = Boolean.FALSE, aylikHareketKaydiVardiyaBul = Boolean.FALSE;

	private Boolean gerceklesenMesaiKod = Boolean.FALSE, devredenBakiyeKod = Boolean.FALSE, normalCalismaSaatKod = Boolean.FALSE, haftaTatilCalismaSaatKod = Boolean.FALSE, resmiTatilCalismaSaatKod = Boolean.FALSE, izinSureSaatKod = Boolean.FALSE;

	private Boolean normalCalismaGunKod = Boolean.FALSE, haftaTatilCalismaGunKod = Boolean.FALSE, resmiTatilCalismaGunKod = Boolean.FALSE, izinSureGunKod = Boolean.FALSE, ucretliIzinGunKod = Boolean.FALSE, ucretsizIzinGunKod = Boolean.FALSE, hastalikIzinGunKod = Boolean.FALSE;

	private Boolean normalGunKod = Boolean.FALSE, haftaTatilGunKod = Boolean.FALSE, resmiTatilGunKod = Boolean.FALSE, artikGunKod = Boolean.FALSE, bordroToplamGunKod = Boolean.FALSE, devredenMesaiKod = Boolean.FALSE, ucretiOdenenKod = Boolean.FALSE;

	private DepartmanDenklestirmeDonemi denklestirmeDonemi;

	private Tanim gorevYeri;

	private TreeMap<String, Tanim> ekSahaTanimMap;

	private AylikPuantaj aylikPuantajDefault, personelAylikPuantaj;

	private List<AylikPuantaj> aylikPuantajList, aylikPuantajDosyaList, aylikPuantajMesaiTalepList;

	private DenklestirmeAy denklestirmeAy, denklestirmeOncekiAy, denklestirmeSonrakiAy;

	private PersonelDenklestirme personelDenklestirme;

	private List<SelectItem> ozelDurumList, redNedenleri;

	private Long redNedeniId, mesaiNedenId, seciliEkSaha4Id, gorevTipiId;

	private List<Tanim> gorevYeriTanimList, mesaiIptalNedenTanimList;

	private HashMap<String, Personel> gorevliPersonelMap;

	private double haftaTatilSaat = 0d;

	private List<User> toList, ccList, bccList;

	private File ekliDosya;

	private String mailKonu, mailIcerik;

	private int ay, yil, maxYil, talepOnayDurum, sonDonem;

	private List<SelectItem> aylar, mesaiNedenTanimList;

	private Date haftaTatili;

	private Boolean kaydet, degisti, kullaniciPersonel = false;

	private Departman departman;

	private Tanim planDepartman, ekSaha4Tanim;

	private VardiyaPlan vardiyaPlan;

	private TreeMap<String, VardiyaGun> vardiyalarMap = new TreeMap<String, VardiyaGun>();
	private TreeMap<String, Vardiya> kayitliVardiyalarMap = new TreeMap<String, Vardiya>();
	private TreeMap<Long, List<FazlaMesaiTalep>> vardiyaFazlaMesaiMap = new TreeMap<Long, List<FazlaMesaiTalep>>();

	private boolean stajerSirket;
	private boolean manuelVardiyaIzinGir = false;

	private byte[] mailData;
	private List<Vardiya> izinTipiVardiyaList;
	private TreeMap<String, TreeMap<String, List<VardiyaGun>>> izinTipiPersonelVardiyaMap;
	private AramaSecenekleri aramaSecenekleri = null;
	private Notice uyariNot;
	private Session session, sessionHareket;

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
		if (fazlaMesaiTalepler != null)
			fazlaMesaiTalepler.clear();
		else
			fazlaMesaiTalepler = new ArrayList<FazlaMesaiTalep>();
		if (aylikVardiyaOzetList != null)
			aylikVardiyaOzetList.clear();
		else
			aylikVardiyaOzetList = new ArrayList<VardiyaGun>();
		gorevYeriGirisDurum = false;
	}

	/**
	 * 
	 */
	private void fillEkSahaTanim() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		if (aramaSecenekleri == null)
			aramaSecenekleri = new AramaSecenekleri();
		ortakIslemler.fillEkSahaTanimAramaSecenekAta(session, Boolean.FALSE, Boolean.TRUE, aramaSecenekleri);
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
		altBolumAciklama = (String) sonucMap.get("altBolumAciklama");
		tesisAciklama = (String) sonucMap.get("tesisAciklama");
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
		ArrayList<Vardiya> vardiyalar = fillAylikVardiyaList(personelAylikPuantaj, personelDenklestirme);
		personelDenklestirme.setGuncellendi(true);
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
	 * @param vg
	 * @return
	 */
	public String getVardiyaFazlaMesailer(VardiyaGun vg) {
		fazlaMesaiTalepler.clear();
		seciliVardiyaGun = vg;
		if (planGirisi && vg != null && vg.getId() != null && vardiyaFazlaMesaiMap != null) {
			List<FazlaMesaiTalep> list = vardiyaFazlaMesaiMap.containsKey(vg.getId()) ? vardiyaFazlaMesaiMap.get(vg.getId()) : new ArrayList<FazlaMesaiTalep>();
			fazlaMesaiTalepler.addAll(list);

		}
		return "";
	}

	/**
	 * @param vg
	 * @return
	 */
	public boolean isVardiyaFazlaMesailer(VardiyaGun vg) {
		boolean fmtVar = false;
		if (vg != null && vg.getId() != null && vardiyaFazlaMesaiMap != null) {
			fmtVar = vardiyaFazlaMesaiMap.containsKey(vg.getId());
		}
		return fmtVar;
	}

	/**
	 * @param vg
	 * @return
	 */
	public String mesaiGuncelle(VardiyaGun vg) {
		seciliVardiyaGun = vg;
		hareketPdksList = null;
		mesaiNedenId = null;
		boolean kullaniciYetkili = denklestirmeAyDurum;
		// PersonelDenklestirme personelDenklestirme = personelAylikPuantaj.getPersonelDenklestirmeAylik();
		// if (personelDenklestirme != null && personelDenklestirme.getCalismaModeliAy().isHareketKaydiVardiyaBulsunmu()) {
		// kullaniciYetkili = vg.getVersion() >= 0;
		// }
		vg.setKullaniciYetkili(kullaniciYetkili);
		setFazlaMesaiTalep(null);
		if (kullaniciYetkili == false) {
			PdksUtil.addMessageAvailableWarn(PdksUtil.convertToDateString(vg.getVardiyaDate(), "dd MMMMM EEEEE") + " günü vardiyasını güncelleyiniz!");
		} else {
			fazlaMesaiTarihGuncelle = Boolean.FALSE;
			if (vg != null) {
				if (vg.getIslemVardiya() != null)
					fazlaMesaiTarihGuncelle = PdksUtil.tarihKarsilastirNumeric(vg.getIslemVardiya().getVardiyaFazlaMesaiBasZaman(), vg.getIslemVardiya().getVardiyaFazlaMesaiBitZaman()) != 0;
				HashMap fields = new HashMap();
				fields.put("vardiyaGun.id", seciliVardiyaGun.getId());
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<FazlaMesaiTalep> fazlaMesaiTalepler = pdksEntityController.getObjectByInnerObjectList(fields, FazlaMesaiTalep.class);
				if (fazlaMesaiTalepler.size() > 1)
					fazlaMesaiTalepler = PdksUtil.sortListByAlanAdi(fazlaMesaiTalepler, "id", Boolean.TRUE);
				vg.setFazlaMesaiTalepler(fazlaMesaiTalepler.isEmpty() ? null : fazlaMesaiTalepler);
				if (denklestirmeAyDurum)
					mesaiEkle(vg);

			}
		}
		aylikPuantajMesaiTalepList = null;
		return "";
	}

	/**
	 * @return
	 */
	public String mesaiSureHesapla() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(fazlaMesaiTalep.getBaslangicZamani());
		cal.set(Calendar.HOUR_OF_DAY, fazlaMesaiTalep.getBasSaat());
		cal.set(Calendar.MINUTE, fazlaMesaiTalep.getBasDakika());
		Date baslangicZamani = cal.getTime();
		cal.setTime(fazlaMesaiTalep.getBitisZamani());
		cal.set(Calendar.HOUR_OF_DAY, fazlaMesaiTalep.getBitSaat());
		cal.set(Calendar.MINUTE, fazlaMesaiTalep.getBitDakika());
		Date bitisZamani = cal.getTime();
		fazlaMesaiTalep.setBaslangicZamani(baslangicZamani);
		fazlaMesaiTalep.setBitisZamani(bitisZamani);
		if (bitisZamani.after(baslangicZamani)) {
			String talepGirisCikisHareketEkleStr = ortakIslemler.getParameterKey("talepGirisCikisHareketEkle");
			boolean kendiGirisYapiyor = false;
			if (talepGirisCikisHareketEkleStr.equals("1")) {
				Personel personel = null;
				Personel girisYapan = authenticatedUser.getPdksPersonel();
				if (aylikPuantajMesaiTalepList == null) {
					if (seciliVardiyaGun != null)
						personel = seciliVardiyaGun.getPersonel();
				} else
					for (AylikPuantaj aylikPuantaj : aylikPuantajMesaiTalepList) {
						Personel pdksPersonel = aylikPuantaj.getPdksPersonel();
						if (pdksPersonel != null && pdksPersonel.getId() != null && girisYapan.getId().equals(pdksPersonel.getId())) {
							personel = pdksPersonel;
							break;
						}
					}
				if (personel != null)
					kendiGirisYapiyor = personel.getId().equals(girisYapan.getId());
			}
			int adet = 0;
			if (kendiGirisYapiyor == false)
				hareketPdksList = null;
			else {
				if (hareketPdksList != null) {
					for (HareketKGS hareketKGS : hareketPdksList) {
						Date zaman = hareketKGS.getOrjinalZaman();
						if (zaman != null && baslangicZamani.getTime() <= zaman.getTime() && bitisZamani.getTime() >= zaman.getTime())
							++adet;
					}
				}
			}
			int yarimYuvarla = seciliVardiyaGun != null ? seciliVardiyaGun.getYarimYuvarla() : PdksUtil.getYarimYuvarlaLast();
			Vardiya islemVardiya = seciliVardiyaGun.getIslemVardiya();
			if (islemVardiya != null && islemVardiya.isCalisma()) {
				if (bitisZamani.getTime() >= islemVardiya.getVardiyaTelorans1BasZaman().getTime() && bitisZamani.getTime() <= islemVardiya.getVardiyaTelorans2BasZaman().getTime())
					bitisZamani = islemVardiya.getVardiyaBasZaman();
				if (bitisZamani.getTime() >= islemVardiya.getVardiyaTelorans1BitZaman().getTime() && bitisZamani.getTime() <= islemVardiya.getVardiyaTelorans2BitZaman().getTime())
					bitisZamani = islemVardiya.getVardiyaBitZaman();
				if (baslangicZamani.getTime() >= islemVardiya.getVardiyaTelorans1BasZaman().getTime() && baslangicZamani.getTime() <= islemVardiya.getVardiyaTelorans2BasZaman().getTime())
					baslangicZamani = islemVardiya.getVardiyaBasZaman();
				if (baslangicZamani.getTime() >= islemVardiya.getVardiyaTelorans1BitZaman().getTime() && baslangicZamani.getTime() <= islemVardiya.getVardiyaTelorans2BitZaman().getTime())
					baslangicZamani = islemVardiya.getVardiyaBitZaman();
			}
			double toplamSure = PdksUtil.setSureDoubleTypeRounded(PdksUtil.getSaatFarki(bitisZamani, baslangicZamani).doubleValue(), yarimYuvarla);
			double farkSure = 0;

			if (islemVardiya != null && islemVardiya.isCalisma() && islemVardiya.getVardiyaFazlaMesaiBasZaman().getTime() <= baslangicZamani.getTime() && islemVardiya.getVardiyaFazlaMesaiBitZaman().getTime() >= bitisZamani.getTime()) {
				if (bitisZamani.getTime() < islemVardiya.getVardiyaTelorans2BitZaman().getTime() && baslangicZamani.getTime() > islemVardiya.getVardiyaTelorans1BasZaman().getTime()) {
					farkSure = toplamSure;
				} else if (bitisZamani.getTime() < islemVardiya.getVardiyaTelorans1BitZaman().getTime() && bitisZamani.getTime() > islemVardiya.getVardiyaTelorans1BasZaman().getTime()) {
					Date bit1 = bitisZamani;
					Date bas1 = islemVardiya.getVardiyaBasZaman();
					farkSure = PdksUtil.setSureDoubleTypeRounded(PdksUtil.getSaatFarki(bit1, bas1).doubleValue(), yarimYuvarla);
					if (farkSure < 0.0d)
						farkSure = 0.0d;
				} else if (baslangicZamani.getTime() > islemVardiya.getVardiyaTelorans1BasZaman().getTime() && baslangicZamani.getTime() < islemVardiya.getVardiyaTelorans2BitZaman().getTime()) {
					Date bit1 = islemVardiya.getVardiyaBitZaman();
					Date bas1 = baslangicZamani;
					farkSure = PdksUtil.setSureDoubleTypeRounded(PdksUtil.getSaatFarki(bit1, bas1).doubleValue(), yarimYuvarla);
					if (farkSure < 0.0d)
						farkSure = 0.0d;
				} else if (baslangicZamani.getTime() < islemVardiya.getVardiyaTelorans1BasZaman().getTime() && bitisZamani.getTime() > islemVardiya.getVardiyaTelorans2BitZaman().getTime()) {
					Date bit1 = islemVardiya.getVardiyaBitZaman();
					Date bas1 = islemVardiya.getVardiyaBasZaman();
					farkSure = PdksUtil.setSureDoubleTypeRounded(PdksUtil.getSaatFarki(bit1, bas1).doubleValue(), yarimYuvarla);
					if (farkSure < 0.0d)
						farkSure = 0.0d;
				}
			}
			fazlaMesaiTalep.setMesaiSuresi(toplamSure - farkSure);
			if (kendiGirisYapiyor) {
				if (adet < 2) {
					if (manuelHareketEkle == null || !manuelHareketEkle)
						manuelHareketEkle = fazlaMesaiTalep.getMesaiSuresi().doubleValue() > 0.0d;
				} else
					manuelHareketEkle = false;

			} else
				manuelHareketEkle = null;

		} else
			fazlaMesaiTalep.setMesaiSuresi(0.0d);
		if (manuelHareketEkle != null && manuelHareketEkle) {
			Date simdi = new Date();
			manuelHareketEkle = bitisZamani.getTime() < simdi.getTime();
		}
		return "";
	}

	/**
	 * @return
	 */
	public String mesaiOnayDurumDegisti() {
		for (FazlaMesaiTalep fmt : aylikFazlaMesaiTalepler) {
			fmt.setCheckBoxDurum(islemYapiliyor && fmt.isIslemYapildi());
		}
		return "";

	}

	/**
	 * @param yilDegisti
	 * @return
	 * @throws Exception
	 */
	public String mesaiDonemDegisti(boolean yilDegisti) throws Exception {
		Date basTarih = null, bitTarih = null;
		StringBuffer sb = new StringBuffer();
		HashMap fields = new HashMap();
		List idler = null;
		Calendar cal = Calendar.getInstance();
		if (yilDegisti) {
			basTarih = PdksUtil.convertToJavaDate(((yil * 100) + 1) + "01", "yyyyMMdd");
			bitTarih = PdksUtil.convertToJavaDate(((yil * 100) + 12) + "31", "yyyyMMdd");
			sb.append("SELECT DISTINCT MONTH(" + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ") from " + VardiyaGun.TABLE_NAME + " V WITH(nolock) ");
			sb.append(" INNER JOIN " + FazlaMesaiTalep.TABLE_NAME + " FT ON FT." + FazlaMesaiTalep.COLUMN_NAME_VARDIYA_GUN + "=V." + VardiyaGun.COLUMN_NAME_ID);
			sb.append(" AND FT." + FazlaMesaiTalep.COLUMN_NAME_DURUM + " =1 ");
			sb.append(" WHERE V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ">=:ta1 AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + "<=:ta2");
			sb.append(" order by 1");
			fields.put("ta1", basTarih);
			fields.put("ta2", bitTarih);
			idler = pdksEntityController.getObjectBySQLList(sb, fields, null);
			int seciliAy = ay;
			aylar.clear();
			ay = 0;
			for (Iterator iterator = idler.iterator(); iterator.hasNext();) {
				Integer object = (Integer) iterator.next();
				if (object.intValue() == seciliAy)
					ay = seciliAy;
				cal.set(Calendar.MONTH, object.intValue() - 1);
				cal.set(Calendar.YEAR, yil);
				cal.set(Calendar.DAY_OF_MONTH, 1);
				String ayAdi = PdksUtil.convertToDateString(cal.getTime(), "MMMM");
				aylar.add(new SelectItem(object.intValue(), ayAdi));
			}
		}
		if (gunler == null)
			gunler = new ArrayList<SelectItem>();
		else
			gunler.clear();
		if (ay == 0 && !aylar.isEmpty())
			ay = (Integer) aylar.get(aylar.size() - 1).getValue();
		basTarihStr = "";
		bitTarihStr = "";
		if (ay > 0) {
			basTarih = PdksUtil.convertToJavaDate(((yil * 100) + ay) + "01", "yyyyMMdd");
			cal.setTime(basTarih);
			int sonGun = cal.getActualMaximum(Calendar.DATE);
			cal.set(Calendar.DATE, sonGun);
			bitTarih = cal.getTime();
			sb = new StringBuffer();
			fields.clear();
			sb.append("SELECT DISTINCT " + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " from " + VardiyaGun.TABLE_NAME + " V WITH(nolock) ");
			sb.append(" INNER JOIN " + FazlaMesaiTalep.TABLE_NAME + " FT ON FT." + FazlaMesaiTalep.COLUMN_NAME_VARDIYA_GUN + "=V." + VardiyaGun.COLUMN_NAME_ID);
			sb.append(" AND FT." + FazlaMesaiTalep.COLUMN_NAME_DURUM + " =1 ");
			sb.append(" WHERE V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ">=:ta1 AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + "<=:ta2");
			sb.append(" order by V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI);
			fields.put("ta1", basTarih);
			fields.put("ta2", bitTarih);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);

			try {
				idler = pdksEntityController.getObjectBySQLList(sb, fields, null);
			} catch (Exception e) {
				logger.equals(e);
				e.printStackTrace();
			}
			for (Iterator iterator = idler.iterator(); iterator.hasNext();) {
				Object object = (Object) iterator.next();
				String str = "";
				if (object instanceof Timestamp)
					str = PdksUtil.convertToDateString(new Date(((Timestamp) object).getTime()), PdksUtil.getDateFormat());
				else if (object instanceof Date)
					str = PdksUtil.convertToDateString((Date) object, PdksUtil.getDateFormat());

				if (PdksUtil.hasStringValue(str))
					gunler.add(new SelectItem(str, str));
			}
			if (!idler.isEmpty()) {
				basTarihStr = gunler.get(0).getLabel();
				bitTarihStr = gunler.get(gunler.size() - 1).getLabel();
			}
		}
		aylikFazlaMesaiTalepler.clear();
		return "";
	}

	/**
	 * @param fmt
	 * @return
	 * @throws Exception
	 */
	@Transactional
	public String mesaiOnay() throws Exception {
		boolean guncelle = false;
		for (FazlaMesaiTalep fmt : aylikFazlaMesaiTalepler) {
			if (fmt.isCheckBoxDurum()) {
				fmt.setGuncelleyenUser(authenticatedUser);
				fmt.setOnayDurumu(FazlaMesaiTalep.ONAY_DURUM_ONAYLANDI);
				fmt.setGuncellemeTarihi(new Date());
				pdksEntityController.saveOrUpdate(session, entityManager, fmt);
				session.flush();
				setIslemFazlaMesaiTalep(fmt);
				mesaiMudurOnayCevabi(true);
				fmt.setCheckBoxDurum(false);
				guncelle = true;
			}

		}
		if (guncelle)
			fillAylikMesaiTalepList();
		else
			PdksUtil.addMessageAvailableWarn("Seçili kayıt yok!");
		return "";
	}

	/**
	 * @param fmt
	 * @return
	 */
	public String mesaiIptalSec(FazlaMesaiTalep fmt) {
		setIslemFazlaMesaiTalep(fmt);
		mesaiIptalNedenTanimList = ortakIslemler.getTanimList(Tanim.TIPI_FAZLA_MESAI_IPTAL_NEDEN, session);
		return "";

	}

	/**
	 * @return
	 */
	@Transactional
	public String mesaiIptal() {
		islemFazlaMesaiTalep.setDurum(Boolean.FALSE);
		pdksEntityController.saveOrUpdate(session, entityManager, islemFazlaMesaiTalep);
		session.flush();

		if (islemFazlaMesaiTalep.getOnayDurumu() == FazlaMesaiTalep.ONAY_DURUM_ONAYLANDI)
			mesaiMudurOnayCevabi(false);
		String talepGirisCikisHareketEkleStr = ortakIslemler.getParameterKey("talepGirisCikisHareketEkle");
		if (talepGirisCikisHareketEkleStr.equals("1"))
			talepGirisCikisHareketEkle();
		islemFazlaMesaiTalep = null;
		if (seciliVardiyaGun != null)
			mesaiGuncelle(seciliVardiyaGun);
		else
			try {
				if (authenticatedUser.getCalistigiSayfa() != null && authenticatedUser.getCalistigiSayfa().equals("fazlaMesaiTalep"))
					fillFazlaMesaiTalepList();
				else
					fillAylikMesaiTalepList();
			} catch (Exception e) {

			}

		return "";
	}

	/**
	 * @param durum
	 * @return
	 */
	public String getOnayId(String durum) {
		String id = ortakIslemler.getEncodeStringByBase64("&fmtId=" + islemFazlaMesaiTalep.getId() + "&durum=" + durum);
		return id;

	}

	/**
	 * @param basTarih
	 * @param bitTarih
	 * @return
	 */
	private String getTarihArasiBitisZamanString(Date basTarih, Date bitTarih) {
		String str = "";
		if (basTarih != null && bitTarih != null && !basTarih.after(bitTarih)) {
			String pattern = PdksUtil.getDateTimeFormat();
			if (PdksUtil.tarihKarsilastirNumeric(basTarih, bitTarih) == 0) {
				pattern = PdksUtil.getSaatFormat();
			}
			str = " - " + PdksUtil.convertToDateString(bitTarih, pattern);
		}

		return str;
	}

	/**
	 * @param fmt
	 * @return
	 */
	public String mesaiMailHatirlatma(FazlaMesaiTalep fmt) {
		setIslemFazlaMesaiTalep(fmt);
		Map<String, String> map = null;
		try {
			map = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();

		} catch (Exception e) {
		}
		donusAdres = map.containsKey("host") ? map.get("host") : "";
		boolean test = donusAdres.indexOf("localhost:") >= 0;
		if (toList == null)
			toList = new ArrayList<User>();
		else
			toList.clear();
		if (!test)
			toList.add(fmt.getGuncelleyenUser());
		else
			toList.add(authenticatedUser);
		if (bccList == null)
			bccList = new ArrayList<User>();
		else
			bccList.clear();
		String servisMailAdresleri = ortakIslemler.getParameterKey("bccAdres");
		if (servisMailAdresleri != null && servisMailAdresleri.indexOf("@") > 1) {
			List<String> mailList = PdksUtil.getListFromString(servisMailAdresleri, null);
			for (String mail : mailList) {
				User user = new User();
				user.setEmail(mail);
				bccList.add(user);
			}
		}
		VardiyaGun vg = fmt.getVardiyaGun();
		Personel personel = vg.getPersonel();
		Sirket sirket = personel.getSirket();
		Tanim tesis = sirket.isTesisDurumu() ? personel.getTesis() : null;
		Tanim bolum = personel.getEkSaha3();
		Tanim altBolum = getPersonelAltBolumu(personel);
		if (tesis != null && tesisAciklama == null && tesis.getParentTanim() == null)
			fillEkSahaTanim();
		String tesisAciklamasi = tesis != null ? getAciklamasi(tesis, tesisAciklama) : "";
		String bolumAciklamasi = getAciklamasi(bolum, bolumAciklama);
		String altBolumAciklamasi = altBolum != null ? getAciklamasi(altBolum, altBolumAciklama) : null;
		String ortakAciklama = PdksUtil.replaceAll(sirket.getAd() + " " + ortakIslemler.sirketAciklama().toLowerCase(PdksUtil.TR_LOCALE) + "i " + (tesis != null ? tesis.getAciklama() + " " + tesisAciklamasi + " " : "") + " " + (bolum != null ? bolum.getAciklama() + " " + bolumAciklamasi + " " : "")
				+ " " + (altBolum != null ? altBolum.getAciklama() + " " + altBolumAciklamasi + " " : "") + " çalışanı " + personel.getAdSoyad(), "  ", " ");
		mailKonu = PdksUtil.replaceAll(ortakAciklama + " fazla mesai talep ", "  ", " ");
		StringBuffer sb = new StringBuffer();
		sb.append("<p>Sayın " + fmt.getGuncelleyenUser().getAdSoyad() + ",</p>");
		sb.append("<p>" + ortakAciklama + " " + authenticatedUser.dateTimeFormatla(fmt.getBaslangicZamani()) + getTarihArasiBitisZamanString(fmt.getBaslangicZamani(), fmt.getBitisZamani()) + " arası " + authenticatedUser.sayiFormatliGoster(fmt.getMesaiSuresi()) + " saat ");
		sb.append((fmt.getMesaiNeden() != null ? "<b>\"" + fmt.getMesaiNeden().getAciklama() + (PdksUtil.hasStringValue(fmt.getAciklama()) ? " ( Açıklama : " + fmt.getAciklama().trim() + " ) " : "") + "\"</b> nedeniyle " : "") + " fazla mesai yapacaktır." + "</p>");
		mailIcerik = PdksUtil.replaceAll(sb.toString(), "  ", " ");
		try {
			MailStatu mailSatu = null;
			try {
				MailObject mail = new MailObject();
				mail.setSubject(mailKonu);
				StringBuffer body = new StringBuffer(mailIcerik);
				body.append("<p><TABLE style=\"width: 270px;\"><TR>");
				body.append("<td width=\"90px\"><a style=\"font-size: 16px;\" href=\"http://" + donusAdres + "/mesaiTalepLinkOnay?id=" + getOnayId(String.valueOf(FazlaMesaiTalep.ONAY_DURUM_ONAYLANDI)) + "\"><b>Onay</b></a></td>");
				body.append("<td width=\"90px\"><a style=\"font-size: 16px;\" href=\"http://" + donusAdres + "/mesaiTalepLinkOnay?id=" + getOnayId(String.valueOf(FazlaMesaiTalep.ONAY_DURUM_RED)) + "\"><b>Red</b></a></td>");
				body.append("</TR></TABLE></p>");
				mail.setBody(body.toString());
				body = null;
				ortakIslemler.addMailPersonelUserList(toList, mail.getToList());
				mailSatu = ortakIslemler.mailSoapServisGonder(true, mail, renderer, "/email/fazlaMesaiTalepMail.xhtml", session);
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				PdksUtil.addMessageError(e.getMessage());
			}
			if (mailSatu != null && mailSatu.isDurum())
				PdksUtil.addMessageAvailableInfo(personel.getAdSoyad() + " " + authenticatedUser.getTarihFormatla(vg.getVardiyaDate(), PdksUtil.getDateFormat()) + " günü " + authenticatedUser.sayiFormatliGoster(fmt.getMesaiSuresi()) + " saat  fazla mesai talep mesajı "
						+ fmt.getGuncelleyenUser().getAdSoyad() + "  gönderildi.");
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * @param tanim
	 * @param ozelAciklama
	 * @return
	 */
	private String getAciklamasi(Tanim tanim, String ozelAciklama) {
		String aciklama = null;
		if (tanim != null && tanim.getParentTanim() != null)
			aciklama = tanim.getParentTanim().getAciklama();
		if (ozelAciklama != null && PdksUtil.hasStringValue(aciklama) == false)
			aciklama = ozelAciklama;
		if (aciklama != null)
			aciklama = aciklama.trim().toLowerCase(PdksUtil.TR_LOCALE);
		return aciklama;
	}

	/**
	 * @param personel
	 * @return
	 */
	private Tanim getPersonelAltBolumu(Personel personel) {
		Tanim bolumu = null, altBolum = personel.getEkSaha4();
		boolean altBolumVar = false;
		try {
			bolumu = (Tanim) personel.getEkSaha3().clone();
		} catch (Exception e) {
		}
		if (PdksUtil.isPuantajSorguAltBolumGir() && altBolum != null) {
			if (!bolumu.getAciklama().equals(altBolum.getAciklama()))
				altBolumVar = true;
		}
		bolumu = null;
		if (altBolumVar) {
			bolumu = altBolum;
		}
		return bolumu;
	}

	/**
	 * @param onayDurum
	 * @return
	 */
	public String mesaiMudurOnayCevabi(boolean onayDurum) {
		uyariNot = ortakIslemler.getNotice(NoteTipi.MAIL_CEVAPLAMAMA.value(), Boolean.TRUE, session);

		FazlaMesaiTalep fmt = islemFazlaMesaiTalep;
		if (toList == null)
			toList = new ArrayList<User>();
		else
			toList.clear();
		toList.add(fmt.getOlusturanUser());
		if (ccList == null)
			ccList = new ArrayList<User>();
		else
			ccList.clear();
		if (onayDurum)
			if (ortakIslemler.getParameterKey("fazlaMesaiMudurOnayCevabiIK").equals("1"))
				ortakIslemler.IKKullanicilariBul(ccList, fmt.getVardiyaGun().getPersonel(), session);

		if (bccList == null)
			bccList = new ArrayList<User>();
		else
			bccList.clear();
		String servisMailAdresleri = ortakIslemler.getParameterKey("bccAdres");
		if (servisMailAdresleri != null && servisMailAdresleri.indexOf("@") > 1) {
			List<String> mailList = PdksUtil.getListFromString(servisMailAdresleri, null);
			for (String mail : mailList) {
				User user = new User();
				user.setEmail(mail);
				bccList.add(user);
			}

		}

		VardiyaGun vg = fmt.getVardiyaGun();
		Personel personel = vg.getPersonel();
		Sirket sirket = personel.getSirket();
		Tanim tesis = sirket.isTesisDurumu() ? personel.getTesis() : null;
		Tanim bolum = personel.getEkSaha3();
		if (tesis != null && tesisAciklama == null && tesis.getParentTanim() == null)
			fillEkSahaTanim();
		Tanim altBolum = getPersonelAltBolumu(personel);
		String tesisAciklamasi = tesis != null ? getAciklamasi(tesis, tesisAciklama) : "";
		String bolumAciklamasi = getAciklamasi(bolum, bolumAciklama);
		String altBolumAciklamasi = altBolum != null ? getAciklamasi(altBolum, altBolumAciklama) : null;
		String ortakAciklama = PdksUtil.replaceAll(sirket.getAd() + " " + ortakIslemler.sirketAciklama().toLowerCase(PdksUtil.TR_LOCALE) + "i " + (tesis != null ? tesis.getAciklama() + " " + tesisAciklamasi + " " : "") + " " + (bolum != null ? bolum.getAciklama() + " " + bolumAciklamasi + " " : "")
				+ " " + (altBolum != null ? altBolum.getAciklama() + " " + altBolumAciklamasi + " " : "") + " çalışanı " + personel.getAdSoyad(), "  ", " ");
		mailKonu = PdksUtil.replaceAll(ortakAciklama + " Fazla Mesai Talep " + fmt.getOnayDurumAciklama(), "  ", " ");
		StringBuffer sb = new StringBuffer();
		sb.append("<p>Sayın " + fmt.getOlusturanUser().getAdSoyad() + ",</p>");
		sb.append("<p>" + ortakAciklama + " " + fmt.getOlusturanUser().dateTimeFormatla(fmt.getBaslangicZamani()) + getTarihArasiBitisZamanString(fmt.getBaslangicZamani(), fmt.getBitisZamani()) + " arası " + fmt.getOlusturanUser().sayiFormatliGoster(fmt.getMesaiSuresi()) + " saat ");
		sb.append((fmt.getMesaiNeden() != null ? "<b>\"" + fmt.getMesaiNeden().getAciklama() + (PdksUtil.hasStringValue(fmt.getAciklama()) ? " ( Açıklama : " + fmt.getAciklama().trim() + " ) " : "") + "\"</b>" : " olması sebebiyle ") + " fazla mesai talebi "
				+ (onayDurum ? "onaylandı" : (fmt.getRedNedeni() != null ? "<b>\"" + fmt.getRedNedeni().getAciklama() + "\"</b> nedeniyle " : "") + (fmt.getDurum() ? " rededildi" : " iptal edildi")) + ".</p>");
		if (PdksUtil.hasStringValue(fmt.getIptalAciklama())) {
			sb.append("<p><b>İptal açıklama : " + "</b>" + fmt.getIptalAciklama().trim() + "</p>");
		}
		if (uyariNot != null)
			sb.append(uyariNot.getValue());
		mailIcerik = PdksUtil.replaceAll(sb.toString(), "  ", " ");

		MailStatu mailSatu = null;
		try {
			MailObject mail = new MailObject();
			ortakIslemler.addMailPersonelUserList(toList, mail.getToList());
			ortakIslemler.addMailPersonelUserList(ccList, mail.getCcList());
			mail.setSubject(mailKonu);
			mail.setBody(mailIcerik);
			if (!mail.getToList().isEmpty() || !mail.getCcList().isEmpty())
				mailSatu = ortakIslemler.mailSoapServisGonder(true, mail, renderer, "/email/fazlaMesaiTalepCevapMail.xhtml", session);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			PdksUtil.addMessageError(e.getMessage());
		}
		if (mailSatu != null && mailSatu.isDurum())
			PdksUtil.addMessageAvailableInfo(fmt.getOlusturanUser().getAdSoyad() + " fazla mesai talep cevabı gönderildi.");

		return "";
	}

	/**
	 * @return
	 */
	@Transactional
	public String mesaiRedIslemi() {
		HashMap fields = new HashMap();
		fields.put("id", redNedeniId);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		Tanim redNedeni = (Tanim) pdksEntityController.getObjectByInnerObject(fields, Tanim.class);
		islemFazlaMesaiTalep.setRedNedeni(redNedeni);
		islemFazlaMesaiTalep.setOnayDurumu(FazlaMesaiTalep.ONAY_DURUM_RED);
		islemFazlaMesaiTalep.setGuncellemeTarihi(new Date());
		pdksEntityController.saveOrUpdate(session, entityManager, islemFazlaMesaiTalep);
		session.flush();
		mesaiMudurOnayCevabi(false);
		mesaiIptalNedenTanimList = null;
		return "";
	}

	/**
	 * @param topluGuncelle
	 * @return
	 */
	@Transactional
	public String mesaiKaydet(boolean topluGuncelle) {
		Vardiya islemVardiya = seciliVardiyaGun.getIslemVardiya();
		if (islemVardiya != null) {
			boolean devam = true;
			String anaMesaj = seciliVardiyaGun.getPersonel().getPdksSicilNo() + " " + seciliVardiyaGun.getPersonel().getAdSoyad();
			Tanim mesaiNeden = ortakIslemler.getTanimById(mesaiNedenId, session);
			fazlaMesaiTalep.setMesaiNeden(mesaiNeden);
			if (fazlaMesaiTalep.getMesaiNeden() == null) {
				PdksUtil.addMessageAvailableWarn("Mesai nedeni seçiniz!");
				devam = false;
			} else {
				Calendar cal = Calendar.getInstance();
				cal.setTime(fazlaMesaiTalep.getBaslangicZamani());
				cal.set(Calendar.HOUR_OF_DAY, fazlaMesaiTalep.getBasSaat());
				cal.set(Calendar.MINUTE, fazlaMesaiTalep.getBasDakika());
				Date baslangicZamani = cal.getTime();
				cal.setTime(fazlaMesaiTalep.getBitisZamani());
				cal.set(Calendar.HOUR_OF_DAY, fazlaMesaiTalep.getBitSaat());
				cal.set(Calendar.MINUTE, fazlaMesaiTalep.getBitDakika());
				Date bitisZamani = cal.getTime();
				fazlaMesaiTalep.setBaslangicZamani(baslangicZamani);
				fazlaMesaiTalep.setBitisZamani(bitisZamani);
				if (seciliVardiyaGun.getVersion() >= 0) {
					if (islemVardiya.isCalisma()) {
						boolean o1 = islemVardiya.getVardiyaFazlaMesaiBasZaman() == null || islemVardiya.getVardiyaFazlaMesaiBasZaman().getTime() <= baslangicZamani.getTime();
						boolean o2 = islemVardiya.getVardiyaBasZaman().getTime() >= bitisZamani.getTime();
						boolean oncekiVardiyaSinirda = o1 && o2;
						boolean s1 = islemVardiya.getVardiyaFazlaMesaiBitZaman() == null || islemVardiya.getVardiyaFazlaMesaiBitZaman().getTime() > bitisZamani.getTime();
						boolean s2 = baslangicZamani.getTime() >= islemVardiya.getVardiyaBitZaman().getTime();
						boolean sonrakiVardiyaSinirda = s1 && s2;
						if (!oncekiVardiyaSinirda && !sonrakiVardiyaSinirda) {
							PdksUtil.addMessageAvailableWarn(anaMesaj + " mesai başlangıç ve bitiş zamanı hatalıdır");
							devam = false;
						}
					} else if (baslangicZamani.before(islemVardiya.getVardiyaFazlaMesaiBasZaman()) || bitisZamani.after(islemVardiya.getVardiyaFazlaMesaiBitZaman())) {
						PdksUtil.addMessageAvailableWarn(anaMesaj + " mesai başlangıç ve bitiş zamanı hatalıdır");
						devam = false;
					}
				}
			}
			if (authenticatedUser.isAdmin())
				devam = false;
			if (devam) {
				HashMap fields = new HashMap();
				if (fazlaMesaiTalep.getId() != null)
					fields.put("id<>", fazlaMesaiTalep.getId());
				fields.put("vardiyaGun.id=", seciliVardiyaGun.getId());
				fields.put("baslangicZamani<", fazlaMesaiTalep.getBitisZamani());
				fields.put("bitisZamani>", fazlaMesaiTalep.getBaslangicZamani());
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<FazlaMesaiTalep> list = pdksEntityController.getObjectByInnerObjectListInLogic(fields, FazlaMesaiTalep.class);
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					FazlaMesaiTalep fazlaMesaiTalep = (FazlaMesaiTalep) iterator.next();
					if (fazlaMesaiTalep.getOnayDurumu() == FazlaMesaiTalep.ONAY_DURUM_RED || fazlaMesaiTalep.getDurum().equals(Boolean.FALSE))
						iterator.remove();

				}
				if (list.isEmpty()) {
					String str = anaMesaj + " ait " + authenticatedUser.dateTimeFormatla(fazlaMesaiTalep.getBaslangicZamani()) + getTarihArasiBitisZamanString(fazlaMesaiTalep.getBaslangicZamani(), fazlaMesaiTalep.getBitisZamani()) + " arası "
							+ authenticatedUser.sayiFormatliGoster(fazlaMesaiTalep.getMesaiSuresi()) + " saat mesai ";
					if (fazlaMesaiTalep.getOlusturanUser().getId().equals(fazlaMesaiTalep.getGuncelleyenUser().getId())) {
						fazlaMesaiTalep.setOnayDurumu(FazlaMesaiTalep.ONAY_DURUM_ONAYLANDI);
						fazlaMesaiTalep.setGuncellemeTarihi(new Date());
					}
					pdksEntityController.saveOrUpdate(session, entityManager, fazlaMesaiTalep);
					session.flush();
					mesaiMailHatirlatma(fazlaMesaiTalep);
					if (manuelHareketEkle != null && manuelHareketEkle)
						talepGirisCikisHareketEkle();
					PdksUtil.addMessageAvailableInfo(str + fazlaMesaiTalep.getGuncelleyenUser().getAdSoyad() + " tarafından onaylanacaktır.");
					if (!topluGuncelle)
						mesaiGuncelle(seciliVardiyaGun);
					else
						fazlaMesaiTalep = null;
				} else {
					FazlaMesaiTalep mesaiTalep = list.get(0);
					String str = authenticatedUser.dateTimeFormatla(mesaiTalep.getBaslangicZamani()) + getTarihArasiBitisZamanString(mesaiTalep.getBaslangicZamani(), mesaiTalep.getBitisZamani()) + " arası " + authenticatedUser.sayiFormatliGoster(mesaiTalep.getMesaiSuresi()) + " saat mesai ";
					if (mesaiTalep.getOnayDurumu() == FazlaMesaiTalep.ONAY_DURUM_ONAYLANDI)
						PdksUtil.addMessageWarn(str + " daha önce " + mesaiTalep.getGuncelleyenUser().getAdSoyad() + " tarafından onaylanmıştır.");
					else
						PdksUtil.addMessageWarn(str + " daha önce " + mesaiTalep.getGuncelleyenUser().getAdSoyad() + "  onaylanması beklenmektedir!");
				}

			}
		}

		return "";
	}

	/**
	 * @param vg
	 * @return
	 */
	public String mesaiEkle(VardiyaGun vg) {
		FazlaMesaiTalep fmt = null;
		Personel mudur = personelAylikPuantaj.getYonetici2();
		if (vg != null && mudur != null) {
			User mudurKullanici = null;
			if (mudur.getId() != null) {
				if (mudur.isCalisiyor()) {
					HashMap fields = new HashMap();
					fields.put("pdksPersonel.id", mudur.getId());
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					mudurKullanici = (User) pdksEntityController.getObjectByInnerObject(fields, User.class);
					if (mudurKullanici != null && !mudurKullanici.isDurum())
						mudurKullanici = null;

					if (mudurKullanici == null)
						PdksUtil.addMessageAvailableWarn(mudur.getAdSoyad() + " personelin aktif PDKS kullanıcı yoktur!");
				}
			}
			if (mudurKullanici != null) {
				fmt = new FazlaMesaiTalep(vg);
				fmt.setOlusturanUser(authenticatedUser);
				fmt.setGuncelleyenUser(mudurKullanici);
				mesaiNedenTanimList = ortakIslemler.getTanimSelectItem(ortakIslemler.getTanimList(Tanim.TIPI_FAZLA_MESAI_NEDEN, session));
				// seciliVardiyaGun.setIslemVardiya(null);
				// seciliVardiyaGun.setIslendi(false);
				if (seciliVardiyaGun.getVardiya().isCalisma()) {
					// seciliVardiyaGun.setVardiyaZamani();
					Vardiya islemVardiya = seciliVardiyaGun.getIslemVardiya();
					fmt.setBaslangicZamani(islemVardiya.getVardiyaBitZaman());
					fmt.setBitisZamani(islemVardiya.getVardiyaBitZaman());
					Calendar cal = Calendar.getInstance();
					cal.setTime(fmt.getBaslangicZamani());
					fmt.setBasSaat(cal.get(Calendar.HOUR_OF_DAY));
					fmt.setBasDakika(cal.get(Calendar.MINUTE));
					cal.setTime(fmt.getBitisZamani());
					fmt.setBitSaat(cal.get(Calendar.HOUR_OF_DAY));
					fmt.setBitDakika(cal.get(Calendar.MINUTE));
				}
			}
		}

		setFazlaMesaiTalep(fmt);
		manuelHareketEkle = null;
		if (seciliVardiyaGun != null && seciliVardiyaGun.getIslemVardiya() != null) {
			Personel girisYapan = authenticatedUser.getPdksPersonel(), personel = vg.getPersonel();
			if (personel != null && girisYapan.getId().equals(personel.getId())) {
				Vardiya islemVardiya = seciliVardiyaGun.getIslemVardiya();
				Date basTarih = islemVardiya.getVardiyaFazlaMesaiBasZaman();
				Date bitTarih = islemVardiya.getVardiyaFazlaMesaiBitZaman();
				hareketPdksList = getPdksHareketler(girisYapan.getPersonelKGS().getId(), basTarih, bitTarih);
				manuelHareketEkle = hareketPdksList.isEmpty();
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
		Personel personel = personelAylikPuantaj.getPdksPersonel();
		PersonelDenklestirme seciliPersonelDenklestirme = personelAylikPuantaj.getPersonelDenklestirmeAylik();
		boolean sutIzin = personel.isSutIzniKullan() || seciliPersonelDenklestirme.isSutIzniVar();
		if (!sutIzin && AylikPuantaj.getGebelikGuncelle()) {
			if (seciliVardiyaGun.getVardiya().isGebelikMi())
				gebeSutIzniGuncelle = true;
			else {
				gebeSutIzniGuncelle = false;
				for (VardiyaGun pdksVardiyaGun : personelAylikPuantaj.getVardiyalar()) {
					if (!gebeSutIzniGuncelle && pdksVardiyaGun.isAyinGunu() && pdksVardiyaGun.getVardiya() != null)
						gebeSutIzniGuncelle = pdksVardiyaGun.getIzin() == null && pdksVardiyaGun.getVardiya().getGebelik();
				}
			}
		}
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

		if (PdksUtil.hasStringValue(durum)) {
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
	 * @param personelDenklestirme
	 * @param vardiyaMap
	 * @param plan
	 * @param mesaj
	 * @param excelAktar
	 * @return
	 */
	@Transactional
	public boolean vardiyaPlanKontrol(PersonelDenklestirme personelDenklestirme, TreeMap<Long, Vardiya> vardiyaMap, VardiyaPlan plan, String mesaj, boolean excelAktar) {
		boolean yaz = Boolean.TRUE;
		boolean haftaTatil = Boolean.FALSE;
		Calendar cal = Calendar.getInstance();
		Date ilkGun = aylikPuantajDefault.getIlkGun(), iseBaslamaTarihi = null, istenAyrilmaTarihi = null;
		Date sonGun = ortakIslemler.tariheAyEkleCikar(cal, ilkGun, 1);
		boolean admin = ikRole;
		StringBuffer sb = new StringBuffer();
		if (vardiyaMap != null && vardiyaMap.isEmpty())
			vardiyaMap = null;
		boolean ikMesaj = false;
		TreeMap<String, VardiyaGun> vardiyaGunMap = new TreeMap<String, VardiyaGun>();
		List<VardiyaGun> vardiyaGunHareketOnaysizList = new ArrayList<VardiyaGun>();
		Date basGun = null;
		Personel personel = plan.getPdksPersonel();
		if (personelDenklestirme != null && personel == null)
			personel = personelDenklestirme.getPdksPersonel();
		if (plan != null && plan.getVardiyaHaftaList() != null) {
			for (VardiyaHafta vardiyaHafta : plan.getVardiyaHaftaList()) {
				if (vardiyaHafta.getVardiyaGunler() != null) {
					for (VardiyaGun vg : vardiyaHafta.getVardiyaGunler()) {
						if (vg.getVardiya() != null) {
							if (personel == null)
								personel = vg.getPdksPersonel();
							if (basGun == null || basGun.before(vg.getVardiyaDate()))
								basGun = vg.getVardiyaDate();
							vardiyaGunMap.put(vg.getVardiyaDateStr(), vg);
						}

					}
				}

			}
		}
		StringBuffer sbCalismaModeliUyumsuz = new StringBuffer();
		CalismaModeli calismaModeli = personelDenklestirme != null && personelDenklestirme.getPersonel() != null ? personelDenklestirme.getPersonel().getCalismaModeli() : null;
		if (personelDenklestirme.getCalismaModeliAy() != null)
			calismaModeli = personelDenklestirme.getCalismaModeli();
		String donem = String.valueOf(yil * 100 + ay);
		Double kisaDonemSaat = null;
		try {
			String str = ortakIslemler.getParameterKey("kisaDonemSaat");
			if (PdksUtil.hasStringValue(str))
				kisaDonemSaat = Double.valueOf(Double.parseDouble(str));
			if (kisaDonemSaat.doubleValue() <= 0.0D)
				kisaDonemSaat = null;
		} catch (Exception e) {
			kisaDonemSaat = null;
		}
		Integer geceVardiyaAdetMaxSaat = null;
		try {
			String str = ortakIslemler.getParameterKey("geceVardiyaAdetMaxSaat");
			if (PdksUtil.hasStringValue(str))
				geceVardiyaAdetMaxSaat = Integer.valueOf(Integer.parseInt(str));
			if (geceVardiyaAdetMaxSaat.intValue() <= 0)
				geceVardiyaAdetMaxSaat = null;
		} catch (Exception e) {
			geceVardiyaAdetMaxSaat = null;
		}
		Integer haftaTatilCalismaGunAdetMaxSaat = null;
		try {
			String str = ortakIslemler.getParameterKey("haftaTatilCalismaGunAdetMaxSaat");
			if (PdksUtil.hasStringValue(str))
				haftaTatilCalismaGunAdetMaxSaat = Integer.valueOf(Integer.parseInt(str));
			if (haftaTatilCalismaGunAdetMaxSaat.intValue() <= 0)
				haftaTatilCalismaGunAdetMaxSaat = null;
		} catch (Exception e) {
			haftaTatilCalismaGunAdetMaxSaat = null;
		}
		VardiyaGun oncekiVardiyaGunKontrol = null;
		int geceVardiyaAdetSaat = 0;
		Integer haftaTatilCalismaGunAdetSaat = null;
		String haftaTatilCalismaGunAdetMaxSaatStr = null;
		String geceVardiyaAdetMaxSaatStr = null;
		String calismaPlanDenetimTarihStr = this.ortakIslemler.getParameterKey("calismaPlanDenetimTarih");
		Date calismaPlanDenetimTarih = null;
		try {
			if (PdksUtil.hasStringValue(calismaPlanDenetimTarihStr))
				calismaPlanDenetimTarih = PdksUtil.getDateFromString(calismaPlanDenetimTarihStr);
		} catch (Exception localException1) {
		}
		if (personel != null && haftaTatilCalismaGunAdetMaxSaat != null && calismaPlanDenetimTarih != null && calismaPlanDenetimTarih.before(ilkGun)) {
			Date basTarih = ortakIslemler.tariheGunEkleCikar(cal, ilkGun, -7), bitTarih = ortakIslemler.tariheGunEkleCikar(cal, basGun, -1);
			if (bitTarih.getTime() >= basTarih.getTime()) {
				List<Long> personeller = new ArrayList<Long>();
				personeller.add(personel.getId());
				List<VardiyaGun> list = ortakIslemler.getAllPersonelIdVardiyalar(personeller, basTarih, bitTarih, false, session);
				for (VardiyaGun vg : list) {
					vardiyaGunMap.put(vg.getVardiyaDateStr(), vg);
				}
			}
		}
		for (Iterator<String> iterator = vardiyaGunMap.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			VardiyaGun vardiyaGun = vardiyaGunMap.get(key);
			String str = vardiyaGun.getVardiyaDateStr();
			vardiyaGun.setAyinGunu(str.startsWith(donem));
			if (vardiyaGun.getVardiya() != null) {
				Vardiya vardiya = vardiyaGun.getVardiya();
				boolean calismaPlanDenetimTarihKontrol = (calismaPlanDenetimTarih != null) && (vardiyaGun.getVardiyaDate().after(calismaPlanDenetimTarih));
				if (haftaTatilCalismaGunAdetMaxSaat != null) {
					if (vardiya.isHaftaTatil()) {
						if ((haftaTatilCalismaGunAdetSaat != null) && (haftaTatilCalismaGunAdetSaat.intValue() > haftaTatilCalismaGunAdetMaxSaat.intValue())) {
							yaz = ikRole;
							if (!ikMesaj)
								ikMesaj = ikRole;
							haftaTatilCalismaGunAdetMaxSaatStr = haftaTatilCalismaGunAdetMaxSaat + " günden fazla ardışık çalışma olamaz! ";
							haftaTatilCalismaGunAdetMaxSaat = null;
						}
						if (calismaPlanDenetimTarihKontrol)
							haftaTatilCalismaGunAdetSaat = Integer.valueOf(0);
					} else if ((vardiya.isCalisma()) && (haftaTatilCalismaGunAdetSaat != null)) {
						haftaTatilCalismaGunAdetSaat = Integer.valueOf(haftaTatilCalismaGunAdetSaat.intValue() + 1);
					}
				}
				if (geceVardiyaAdetMaxSaat != null) {
					if ((!vardiya.isAksamVardiyasi()) || (vardiyaGun.getIzin() != null)) {
						if (vardiya.isCalisma()) {
							if ((geceVardiyaAdetSaat > 0) && (geceVardiyaAdetSaat > geceVardiyaAdetMaxSaat.intValue())) {
								yaz = ikRole;
								if (!ikMesaj)
									ikMesaj = ikRole;
								geceVardiyaAdetMaxSaatStr = geceVardiyaAdetMaxSaat + " günden fazla ardışık akşam çalışma olamaz! ";
								geceVardiyaAdetMaxSaat = null;
							}
							geceVardiyaAdetSaat = 0;
						}
					} else if (calismaPlanDenetimTarihKontrol) {
						if (vardiya.isAksamVardiyasi())
							geceVardiyaAdetSaat++;
						logger.debug(vardiyaGun.getVardiyaDateStr() + " " + vardiya.getKisaAdi() + " " + geceVardiyaAdetSaat);
					}

				}
				if ((kisaDonemSaat != null) && (vardiya.isCalisma()) && (vardiyaGun.getIzin() == null)) {
					if ((key.startsWith(donem)) && (oncekiVardiyaGunKontrol != null)) {
						Date basSaat = oncekiVardiyaGunKontrol.getIslemVardiya().getVardiyaBitZaman();
						Date bitSaat = vardiyaGun.getIslemVardiya().getVardiyaBasZaman();
						double toplamSure = PdksUtil.setSureDoubleTypeRounded(Double.valueOf(PdksUtil.getSaatFarki(bitSaat, basSaat).doubleValue()), vardiyaGun.getYarimYuvarla());
						if (toplamSure < kisaDonemSaat.doubleValue() && calismaPlanDenetimTarihKontrol) {
							yaz = ikRole;
							if (!ikMesaj)
								ikMesaj = ikRole;
							sb.append(PdksUtil.convertToDateString(bitSaat, "d MMMMMM EEEEE HH:ss") + " kısa çalışma olamaz! ");
						}
					}
					if (calismaPlanDenetimTarihKontrol)
						oncekiVardiyaGunKontrol = vardiyaGun;

				}
				if (vardiya.isCalisma() && vardiya.getGenel() && calismaModeli.isOrtakVardiyadir() == false && vardiyaMap != null && !vardiyaMap.containsKey(vardiya.getId())) {
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
			} else {
				geceVardiyaAdetSaat = 0;
				haftaTatil = Boolean.FALSE;
			}

		}
		if (geceVardiyaAdetMaxSaatStr != null)
			sb.append(geceVardiyaAdetMaxSaatStr);
		if (haftaTatilCalismaGunAdetMaxSaatStr != null)
			sb.append(haftaTatilCalismaGunAdetMaxSaatStr);
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
						vardiyaGunHareketOnaysizList.add(addHaftaVardiya(list, pdksVardiyaGun, excelAktar));
						if (getVardiyaDurum(pdksVardiyaGun, i))
							yaz = Boolean.FALSE;
					}
				} else {
					if (vardiyaHafta.getVardiyaGun1().getVardiya() != null) {
						vardiyaGunHareketOnaysizList.add(addHaftaVardiya(list, vardiyaHafta.getVardiyaGun1(), excelAktar));
						if (getVardiyaDurum(vardiyaHafta.getVardiyaGun1(), i))
							yaz = Boolean.FALSE;
					}
					if (vardiyaHafta.getVardiyaGun2().getVardiya() != null) {
						vardiyaGunHareketOnaysizList.add(addHaftaVardiya(list, vardiyaHafta.getVardiyaGun2(), excelAktar));
						if (getVardiyaDurum(vardiyaHafta.getVardiyaGun2(), i))
							yaz = Boolean.FALSE;
					}
					if (vardiyaHafta.getVardiyaGun3().getVardiya() != null) {
						vardiyaGunHareketOnaysizList.add(addHaftaVardiya(list, vardiyaHafta.getVardiyaGun3(), excelAktar));
						if (getVardiyaDurum(vardiyaHafta.getVardiyaGun3(), i))
							yaz = Boolean.FALSE;
					}
					if (vardiyaHafta.getVardiyaGun4().getVardiya() != null) {
						vardiyaGunHareketOnaysizList.add(addHaftaVardiya(list, vardiyaHafta.getVardiyaGun4(), excelAktar));
						if (getVardiyaDurum(vardiyaHafta.getVardiyaGun4(), i))
							yaz = Boolean.FALSE;
					}
					if (vardiyaHafta.getVardiyaGun5().getVardiya() != null) {
						vardiyaGunHareketOnaysizList.add(addHaftaVardiya(list, vardiyaHafta.getVardiyaGun5(), excelAktar));
						if (getVardiyaDurum(vardiyaHafta.getVardiyaGun5(), i))
							yaz = Boolean.FALSE;
					}
					if (vardiyaHafta.getVardiyaGun6().getVardiya() != null) {
						vardiyaGunHareketOnaysizList.add(addHaftaVardiya(list, vardiyaHafta.getVardiyaGun6(), excelAktar));
						if (getVardiyaDurum(vardiyaHafta.getVardiyaGun6(), i))
							yaz = Boolean.FALSE;
					}
					if (vardiyaHafta.getVardiyaGun7().getVardiya() != null) {
						vardiyaGunHareketOnaysizList.add(addHaftaVardiya(list, vardiyaHafta.getVardiyaGun7(), excelAktar));
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
								if (vardiyaGun.getVardiya() != null && vardiyaGun.getPersonel() != null) {

									if (vardiyaGun.getVardiya().isHaftaTatil() && vardiyaGun.getPersonel().isCalisiyorGun(vardiyaDate)) {
										if (vardiyaDate.before(ilkGun))
											haftaTatilOncekiAyList.add(vardiyaGun);
										else
											haftaTatilAyList.add(vardiyaGun);
									}

								}
							}

						} else if (parcaliBitHafta) {
							String key = PdksUtil.convertToDateString(vardiyaDate, "yyyyMMdd");
							if (plan.getVardiyaGunMap().containsKey(key)) {
								VardiyaGun vardiyaGun = plan.getVardiyaGunMap().get(key);

								if (vardiyaGun.getPersonel() != null && vardiyaGun.getVardiya().isHaftaTatil() && vardiyaGun.getPersonel().isCalisiyorGun(vardiyaDate)) {
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
						pdksEntityController.saveOrUpdate(session, entityManager, vardiyaGunOncekiAy);
					} else if (haftaTatilAyList.size() == 1 && haftaTatilSonrakiAyList.size() == 1) {
						VardiyaGun vardiyaGunBuAy = haftaTatilAyList.get(0), vardiyaGunSonrakiAy = haftaTatilSonrakiAyList.get(0);
						vardiyaGunSonrakiAy.setVardiya(vardiyaGunBuAy.getEskiVardiya());
						vardiyaGunSonrakiAy.setGuncelleyenUser(authenticatedUser);
						vardiyaGunSonrakiAy.setGuncellemeTarihi(new Date());
						vardiyaGunSonrakiAy.setDurum(Boolean.FALSE);
						pdksEntityController.saveOrUpdate(session, entityManager, vardiyaGunSonrakiAy);
					} else {
						sb.append(haftaStr + " en fazla bir tatil günü tanımlanmalıdır! ");
						yaz = Boolean.FALSE;
					}
				}
			}

		}
		boolean flush = false;
		if (!yaz || (ikMesaj && sb.length() > 0)) {
			if (sb.length() > 0)
				PdksUtil.addMessageAvailableWarn(mesaj + sb.toString());
			if (sbCalismaModeliUyumsuz.length() > 0) {
				String str = sbCalismaModeliUyumsuz.toString();
				PdksUtil.addMessageAvailableWarn(mesaj + str + " " + (calismaModeli != null ? calismaModeli.getAciklama() + " Vardiyalarına " : ortakIslemler.calismaModeliAciklama() + "ne") + " uymayan hatalı " + (str.indexOf(",") < 0 ? "vardiyadır!" : "vardiyalardır"));
			}
			if (personelDenklestirme != null) {
				if (ikMesaj)
					personelDenklestirme.setGuncelleyenUser(authenticatedUser);
				personelDenklestirme.setOnaylandi(yaz);
				personelDenklestirme.setDurum(Boolean.FALSE);
				personelDenklestirme.setGuncellemeTarihi(new Date());
				pdksEntityController.saveOrUpdate(session, entityManager, personelDenklestirme);

			}
		} else {
			for (Iterator iterator = vardiyaGunHareketOnaysizList.iterator(); iterator.hasNext();) {
				VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
				if (vardiyaGun.getId() != null && vardiyaGun.isAyinGunu() && vardiyaGun.getVersion() < 0) {
					vardiyaGun.setVersion(0);
					pdksEntityController.saveOrUpdate(session, entityManager, vardiyaGun);
					flush = true;
				}

			}
		}
		if (flush)
			session.flush();
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
	private VardiyaGun addHaftaVardiya(ArrayList<Vardiya> list, VardiyaGun vardiyaGun, boolean excelAktar) {
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
		return vardiyaGun;
	}

	/**
	 * @param gorevYeriAciklama
	 * @param puantajList
	 * @param sonucGoster
	 * @return
	 */
	public ByteArrayOutputStream aylikVardiyaExcelDevam(String gorevYeriAciklama, List<AylikPuantaj> puantajList, Boolean sonucGoster) {
		List<Boolean> onayDurumList = new ArrayList<Boolean>();
		for (AylikPuantaj aylikPuantaj : puantajList) {
			if (!onayDurumList.contains(aylikPuantaj.isOnayDurum()))
				onayDurumList.add(aylikPuantaj.isOnayDurum());
		}
		ByteArrayOutputStream baos = null;
		String aciklamaExcel = PdksUtil.replaceAll(gorevYeriAciklama + " " + PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyy MMMMMM  "), "_", "");
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "MMMMM yyyy") + " Çalışma Planı", Boolean.TRUE);

		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleTutarEvenDay = ExcelUtil.setAlignment(ExcelUtil.getStyleDayEven(ExcelUtil.FORMAT_TUTAR, wb), CellStyle.ALIGN_CENTER);
		CellStyle styleTutarOddDay = ExcelUtil.setAlignment(ExcelUtil.getStyleDayOdd(ExcelUtil.FORMAT_TUTAR, wb), CellStyle.ALIGN_CENTER);
		CellStyle styleTutarDay = null, styleGenelCenter = null, styleGenel = null, styleGenelLeft = null;
		XSSFCellStyle styleTatil = (XSSFCellStyle) ExcelUtil.getStyleDataCenter(wb);

		XSSFCellStyle styleIstek = (XSSFCellStyle) ExcelUtil.getStyleDataCenter(wb);
		XSSFCellStyle styleEgitim = (XSSFCellStyle) ExcelUtil.getStyleDataCenter(wb);
		XSSFCellStyle styleOff = (XSSFCellStyle) ExcelUtil.getStyleDataCenter(wb);
		ExcelUtil.setFontColor(styleOff, Color.WHITE);
		XSSFCellStyle style = (XSSFCellStyle) ExcelUtil.getStyleDataCenter(wb);
		XSSFCellStyle styleIzin = (XSSFCellStyle) ExcelUtil.getStyleDataCenter(wb);
		XSSFCellStyle header = (XSSFCellStyle) ExcelUtil.getStyleHeader(9, wb);

		XSSFCellStyle styleCalisma = (XSSFCellStyle) ExcelUtil.getStyleDataCenter(wb);
		int row = 0, col = 0;

		ExcelUtil.setFillForegroundColor(styleTatil, 255, 153, 204);
		ExcelUtil.setFillForegroundColor(styleIstek, 255, 255, 0);
		ExcelUtil.setFillForegroundColor(styleIzin, 146, 208, 80);
		ExcelUtil.setFillForegroundColor(styleCalisma, 255, 255, 255);

		ExcelUtil.setFillForegroundColor(styleEgitim, 0, 0, 255);

		ExcelUtil.setFillForegroundColor(styleOff, 13, 12, 89);
		styleOff.getFont().setColor(ExcelUtil.getXSSFColor(256, 256, 256));
		ExcelUtil.getCell(sheet, row, col, header).setCellValue(aciklamaExcel);
		for (int i = 0; i < 3; i++)
			ExcelUtil.getCell(sheet, row, col + i + 1, style).setCellValue("");

		try {
			sheet.addMergedRegion(ExcelUtil.getRegion((int) row, (int) 0, (int) row, (int) 4));
		} catch (Exception e) {
			e.printStackTrace();
		}
		col = 0;
		ExcelUtil.getCell(sheet, ++row, col, style).setCellValue("");
		ExcelUtil.getCell(sheet, ++row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.yoneticiAciklama());

		Calendar cal = Calendar.getInstance();
		cal.setTime(aylikPuantajDefault.getIlkGun());
		CreationHelper factory = wb.getCreationHelper();
		Drawing drawing = sheet.createDrawingPatriarch();
		ClientAnchor anchor = factory.createClientAnchor();
		CellStyle headerVardiyaGun = ExcelUtil.getStyleHeader(9, wb);
		ExcelUtil.setFillForegroundColor(headerVardiyaGun, 99, 182, 153);

		CellStyle headerVardiyaTatilYarimGun = ExcelUtil.getStyleHeader(9, wb);
		ExcelUtil.setFontColor(headerVardiyaTatilYarimGun, 255, 255, 0);
		ExcelUtil.setFillForegroundColor(headerVardiyaTatilYarimGun, 144, 185, 63);

		CellStyle headerVardiyaTatilGun = ExcelUtil.getStyleHeader(9, wb);
		ExcelUtil.setFillForegroundColor(headerVardiyaTatilGun, 92, 127, 45);
		ExcelUtil.setFontColor(headerVardiyaTatilGun, 255, 255, 0);
		for (VardiyaGun vardiyaGun : aylikPuantajDefault.getVardiyalar()) {
			try {
				if (!vardiyaGun.isAyinGunu())
					continue;
				cal.setTime(vardiyaGun.getVardiyaDate());
				CellStyle headerVardiya = headerVardiyaGun;
				String title = null;
				if (vardiyaGun.getTatil() != null) {
					Tatil tatil = vardiyaGun.getTatil();
					title = tatil.getAd();
					headerVardiya = tatil.isYarimGunMu() ? headerVardiyaTatilYarimGun : headerVardiyaTatilGun;
				}
				Cell cell = ExcelUtil.getCell(sheet, row, col++, headerVardiya);
				AylikPuantaj.baslikCell(factory, drawing, anchor, cell, cal.get(Calendar.DAY_OF_MONTH) + "\n " + authenticatedUser.getTarihFormatla(cal.getTime(), "EEE"), title);

			} catch (Exception e) {
			}
		}
		if (sonucGoster) {
			Cell cell = ExcelUtil.getCell(sheet, row, col++, header);
			AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "TÇS", "Toplam Çalışma Saati: Çalışanın bu listedeki toplam çalışma saati");
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "ÇGS", "Çalışılması Gereken Saat: Çalışanın bu listede çalışması gereken saat");
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "GM", "Gerçekleşen Mesai : Çalışanın bu listedeki eksi/fazla çalışma saati");
			if (devredenMesaiKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.devredenMesaiKod(), "Devreden Mesai: Çalisanin önceki listelerden devreden eksi/fazla mesaisi");

			}
			if (ucretiOdenenKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "ÜÖM", "Çalışanın bu listenin sonunda ücret olarak ödediğimiz fazla mesai saati");
			}
			if (eksikMaasGoster) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "NORMC", ortakIslemler.eksikCalismaAciklama() + " : Çalışanın bu listenin sonunda ücretinden kesilecek saati");
			}
			if (resmiTatilVar || bordroPuantajEkranindaGoster) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				AylikPuantaj.baslikCell(factory, drawing, anchor, cell, "RÖM", "Çalışanın bu listenin sonunda ücret olarak ödediğimiz resmi tatil mesai saati");
			}
			if (haftaTatilVar) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				AylikPuantaj.baslikCell(factory, drawing, anchor, cell, AylikPuantaj.MESAI_TIPI_HAFTA_TATIL, "Çalışanın bu listenin sonunda ücret olarak ödediğimiz hafta tatil mesai saati");
			}
			if (aksamGunVar) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				AylikPuantaj.baslikCell(factory, drawing, anchor, cell, AylikPuantaj.MESAI_TIPI_AKSAM_ADET, "Çalışanın bu listenin sonunda ücret olarak ödediğimiz gece mesai gün");
			}
			if (aksamSaatVar) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				AylikPuantaj.baslikCell(factory, drawing, anchor, cell, AylikPuantaj.MESAI_TIPI_AKSAM_SAAT, "Çalışanın bu listenin sonunda ücret olarak ödediğimiz gece mesai saati");
			}
			if (devredenBakiyeKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.devredenBakiyeKod(), "Bakiye: Çalışanın bu liste de dahil bugüne kadarki devreden eksi/fazla mesaisi");
			}
			if (modelGoster)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.calismaModeliAciklama());
			CellStyle headerIzinTipi = (XSSFCellStyle) header.clone();
			ExcelUtil.setFillForegroundColor(headerIzinTipi, 255, 153, 204);

			if (bordroPuantajEkranindaGoster) {
				XSSFCellStyle headerSiyah = (XSSFCellStyle) ExcelUtil.getStyleHeader(wb);
				headerSiyah.getFont().setColor(ExcelUtil.getXSSFColor(255, 255, 255));
				XSSFCellStyle headerSaat = (XSSFCellStyle) headerSiyah.clone();
				XSSFCellStyle headerIzin = (XSSFCellStyle) headerSiyah.clone();
				XSSFCellStyle headerBGun = (XSSFCellStyle) headerSiyah.clone();
				XSSFCellStyle headerBTGun = (XSSFCellStyle) (XSSFCellStyle) headerSiyah.clone();
				ExcelUtil.setFillForegroundColor(headerSaat, 146, 208, 62);
				ExcelUtil.setFillForegroundColor(headerIzin, 255, 255, 255);
				ExcelUtil.setFillForegroundColor(headerBGun, 255, 255, 0);
				ExcelUtil.setFillForegroundColor(headerBTGun, 236, 125, 125);

				if (normalCalismaSaatKod) {
					cell = ExcelUtil.getCell(sheet, row, col++, headerSaat);
					AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.normalCalismaSaatKod(), "N.Çalışma Saat");
				}
				if (haftaTatilCalismaSaatKod) {
					cell = ExcelUtil.getCell(sheet, row, col++, headerSaat);
					AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.haftaTatilCalismaSaatKod(), "H.Tatil Saat");
				}
				if (resmiTatilCalismaSaatKod) {
					cell = ExcelUtil.getCell(sheet, row, col++, headerSaat);
					AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.resmiTatilCalismaSaatKod(), "R.Tatil Saat");
				}
				if (izinSureSaatKod) {
					cell = ExcelUtil.getCell(sheet, row, col++, headerSaat);
					AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.izinSureSaatKod(), "İzin Saat");
				}
				if (normalCalismaGunKod) {
					cell = ExcelUtil.getCell(sheet, row, col++, header);
					AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.normalCalismaGunKod(), "N.Çalışma Gün");
				}
				if (haftaTatilCalismaGunKod) {
					cell = ExcelUtil.getCell(sheet, row, col++, header);
					AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.haftaTatilCalismaGunKod(), "H.Tatil Gün");
				}
				if (resmiTatilCalismaGunKod) {
					cell = ExcelUtil.getCell(sheet, row, col++, header);
					AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.resmiTatilCalismaGunKod(), "R.Tatil Gün");
				}
				if (izinSureGunKod) {
					cell = ExcelUtil.getCell(sheet, row, col++, header);
					AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.izinSureGunKod(), "İzin Gün");
				}
				if (ucretliIzinGunKod) {
					cell = ExcelUtil.getCell(sheet, row, col++, headerIzin);
					AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.ucretliIzinGunKod(), "Ücretli İzin Gün");
				}
				if (ucretsizIzinGunKod) {
					cell = ExcelUtil.getCell(sheet, row, col++, headerIzin);
					AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.ucretsizIzinGunKod(), "Ücretsiz İzin Gün");
				}
				if (hastalikIzinGunKod) {
					cell = ExcelUtil.getCell(sheet, row, col++, headerIzin);
					AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.hastalikIzinGunKod(), "Hastalık İzin Gün");
				}
				if (normalGunKod) {
					cell = ExcelUtil.getCell(sheet, row, col++, headerBGun);
					AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.normalGunKod(), "Normal Gün");
				}
				if (haftaTatilGunKod) {
					cell = ExcelUtil.getCell(sheet, row, col++, headerBGun);
					AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.haftaTatilGunKod(), "H.Tatil Gün");
				}
				if (resmiTatilGunKod) {
					cell = ExcelUtil.getCell(sheet, row, col++, headerBGun);
					AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.resmiTatilGunKod(), "R.Tatil Gün");
				}
				if (artikGunKod) {
					cell = ExcelUtil.getCell(sheet, row, col++, headerBGun);
					AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.artikGunKod(), "Artık Gün");
				}
				if (bordroToplamGunKod) {
					cell = ExcelUtil.getCell(sheet, row, col++, headerBTGun);
					AylikPuantaj.baslikCell(factory, drawing, anchor, cell, ortakIslemler.bordroToplamGunKod(), "Toplam Gün");
				}
			}
			if (izinTipiVardiyaList != null) {
				for (Vardiya vardiya : izinTipiVardiyaList) {
					cell = ExcelUtil.getCell(sheet, row, col++, headerIzinTipi);
					AylikPuantaj.baslikCell(factory, drawing, anchor, cell, vardiya.getKisaAdi(), vardiya.getAdi());

				}
			}

		}
		int sayac = 0;
		for (Iterator iter = puantajList.iterator(); iter.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();

			Personel personel = aylikPuantaj.getPdksPersonel();
			if (!aylikPuantaj.isSecili() || personel == null || PdksUtil.hasStringValue(personel.getSicilNo()) == false)

				continue;
			row++;
			CalismaModeli calismaModeli = aylikPuantaj.getPersonelDenklestirmeAylik().getCalismaModeliAy() != null ? aylikPuantaj.getPersonelDenklestirmeAylik().getCalismaModeli() : null;
			if (calismaModeli == null)
				calismaModeli = personel.getCalismaModeli();
			boolean help = helpPersonel(aylikPuantaj.getPdksPersonel());
			++sayac;

			try {
				if (row % 2 != 0) {
					styleTutarDay = styleTutarOddDay;
					styleGenel = styleOdd;
					styleGenelLeft = styleOdd;
					styleGenelCenter = styleOddCenter;
				} else {
					styleTutarDay = styleTutarEvenDay;
					styleGenel = styleEven;
					styleGenelLeft = styleEven;
					styleGenelCenter = styleEvenCenter;
				}

				boolean koyuRenkli = onayDurumList.size() == 2 && aylikPuantaj.isOnayDurum();
				if (koyuRenkli) {
					ExcelUtil.setFontNormalBold(wb, styleGenel);
					ExcelUtil.setFontNormalBold(wb, styleGenelCenter);
				}
				col = 0;
				ExcelUtil.getCell(sheet, row, col++, styleGenelCenter).setCellValue(personel.getSicilNo());
				Cell personelCell = ExcelUtil.getCell(sheet, row, col++, styleGenelLeft);
				personelCell.setCellValue(personel.getAdSoyad());
				String titlePersonel = null;
				if (personel.getPersonelExtra() != null && personel.getPersonelExtra().getId() != null) {
					PersonelExtra personelExtra = personel.getPersonelExtra();
					titlePersonel = personelExtra.getCepTelefon() + " " + personelExtra.getIlce() + " " + personelExtra.getOzelNot();
				}
				if (koyuRenkli) {
					PersonelDenklestirme denklestirme = aylikPuantaj.getPersonelDenklestirmeAylik();
					if (titlePersonel != null)
						titlePersonel += "\n";
					else
						titlePersonel = "";
					titlePersonel += authenticatedUser.getAdSoyad() + " planı " + authenticatedUser.dateTimeFormatla(denklestirme.getGuncellemeTarihi()) + " onaylandı.";
				}
				if (titlePersonel != null) {
					Comment comment1 = drawing.createCellComment(anchor);
					RichTextString str1 = factory.createRichTextString(titlePersonel);
					comment1.setString(str1);
					personelCell.setCellComment(comment1);
				}
				ExcelUtil.getCell(sheet, row, col++, styleGenelLeft).setCellValue(aylikPuantaj.getYonetici() != null && aylikPuantaj.getYonetici().getId() != null ? aylikPuantaj.getYonetici().getAdSoyad() : "");

				List vardiyaList = aylikPuantaj.getAyinVardiyalari();

				for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
					VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator.next();
					String styleText = pdksVardiyaGun.getAylikClassAdi(aylikPuantaj.getTrClass());
					styleGenel = styleTutarDay;
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
				if (sonucGoster && !help) {
					if (row % 2 != 0)
						styleGenel = styleOdd;
					else {
						styleGenel = styleEven;
					}
					setCell(sheet, row, col++, styleGenel, aylikPuantaj.getSaatToplami());
					Cell planlananCell = setCell(sheet, row, col++, styleGenel, aylikPuantaj.getPlanlananSure());

					if (aylikPuantaj.getCalismaModeliAy() != null && planlananCell != null && aylikPuantaj.getSutIzniDurum().equals(Boolean.FALSE)) {
						Comment comment1 = drawing.createCellComment(anchor);
						String title = aylikPuantaj.getCalismaModeli().getAciklama() + " : ";
						if (aylikPuantaj.getCalismaModeli().getToplamGunGuncelle().equals(Boolean.FALSE))
							title += authenticatedUser.sayiFormatliGoster(aylikPuantaj.getCalismaModeliAy().getSure());
						else
							title += authenticatedUser.sayiFormatliGoster(aylikPuantaj.getPersonelDenklestirmeAylik().getPlanlanSure());
						RichTextString str1 = factory.createRichTextString(title);
						comment1.setString(str1);
						planlananCell.setCellComment(comment1);
					}
					setCell(sheet, row, col++, styleGenel, aylikPuantaj.getAylikNetFazlaMesai());
					if (devredenMesaiKod)
						setCell(sheet, row, col++, styleGenel, aylikPuantaj.getGecenAyFazlaMesai());
					if (ucretiOdenenKod)
						setCell(sheet, row, col++, styleGenel, aylikPuantaj.getFazlaMesaiSure());
					if (eksikMaasGoster)
						setCell(sheet, row, col++, styleGenel, aylikPuantaj.getEksikCalismaSure());

					if (resmiTatilVar || bordroPuantajEkranindaGoster)
						setCell(sheet, row, col++, styleGenel, aylikPuantaj.getResmiTatilToplami());
					if (haftaTatilVar)
						setCell(sheet, row, col++, styleGenel, aylikPuantaj.getHaftaCalismaSuresi());
					if (aksamGunVar)
						setCell(sheet, row, col++, styleGenel, aylikPuantaj.getAksamVardiyaSayisi());
					if (aksamSaatVar)
						setCell(sheet, row, col++, styleGenel, aylikPuantaj.getAksamVardiyaSaatSayisi());
					if (devredenBakiyeKod)
						setCell(sheet, row, col++, styleGenel, aylikPuantaj.getDevredenSure());
					if (modelGoster) {
						String modelAciklama = "";
						if (calismaModeli != null)
							modelAciklama = calismaModeli.getAciklama();

						ExcelUtil.getCell(sheet, row, col++, styleGenelLeft).setCellValue(modelAciklama);
					}
					if (bordroPuantajEkranindaGoster) {
						PersonelDenklestirmeBordro denklestirmeBordro = aylikPuantaj.getDenklestirmeBordro();
						if (denklestirmeBordro == null) {
							denklestirmeBordro = new PersonelDenklestirmeBordro();
							denklestirmeBordro.setPersonelDenklestirme(aylikPuantaj.getPersonelDenklestirmeAylik());
						}

						boolean saatlikCalisma = calismaModeli.isSaatlikOdeme();
						if (denklestirmeBordro.getDetayMap() == null)
							denklestirmeBordro.setDetayMap(new HashMap<BordroDetayTipi, PersonelDenklestirmeBordroDetay>());
						if (normalCalismaSaatKod)
							setCell(sheet, row, col++, styleGenel, saatlikCalisma ? denklestirmeBordro.getSaatNormal() : 0);
						if (haftaTatilCalismaSaatKod)
							setCell(sheet, row, col++, styleGenel, saatlikCalisma ? denklestirmeBordro.getSaatHaftaTatil() : 0);
						if (resmiTatilCalismaSaatKod)
							setCell(sheet, row, col++, styleGenel, saatlikCalisma ? denklestirmeBordro.getSaatResmiTatil() : 0);
						if (izinSureSaatKod)
							setCell(sheet, row, col++, styleGenel, saatlikCalisma ? denklestirmeBordro.getSaatIzin() : 0);
						if (normalCalismaGunKod)
							setCell(sheet, row, col++, styleGenel, saatlikCalisma == false ? denklestirmeBordro.getSaatNormal() : 0);
						if (haftaTatilCalismaGunKod)
							setCell(sheet, row, col++, styleGenel, saatlikCalisma == false ? denklestirmeBordro.getSaatHaftaTatil() : 0);
						if (resmiTatilCalismaGunKod)
							setCell(sheet, row, col++, styleGenel, saatlikCalisma == false ? denklestirmeBordro.getSaatResmiTatil() : 0);
						if (izinSureGunKod)
							setCell(sheet, row, col++, styleGenel, saatlikCalisma == false ? denklestirmeBordro.getSaatIzin() : 0);
						if (ucretliIzinGunKod)
							setCell(sheet, row, col++, styleGenel, denklestirmeBordro.getUcretliIzin().doubleValue());
						if (ucretsizIzinGunKod)
							setCell(sheet, row, col++, styleGenel, denklestirmeBordro.getUcretsizIzin().doubleValue());
						if (hastalikIzinGunKod)
							setCell(sheet, row, col++, styleGenel, denklestirmeBordro.getRaporluIzin().doubleValue());
						if (normalGunKod)
							setCell(sheet, row, col++, styleGenel, denklestirmeBordro.getNormalGunAdet());
						if (haftaTatilGunKod)
							setCell(sheet, row, col++, styleGenel, denklestirmeBordro.getHaftaTatilAdet());
						if (resmiTatilGunKod)
							setCell(sheet, row, col++, styleGenel, denklestirmeBordro.getResmiTatilAdet());
						if (artikGunKod)
							setCell(sheet, row, col++, styleGenel, denklestirmeBordro.getArtikAdet());
						if (bordroToplamGunKod)
							setCell(sheet, row, col++, styleGenel, denklestirmeBordro.getBordroToplamGunAdet());

					}
					if (izinTipiVardiyaList != null) {
						for (Vardiya vardiya : izinTipiVardiyaList) {
							Integer adet = getVardiyaAdet(personel, vardiya);
							if (adet != null)
								setCell(sheet, row, col++, styleGenel, adet);
							else
								ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");

						}
					}
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
		if (sonucGoster && sayac > 1) {
			row += 2;
			boolean renk = Boolean.TRUE;
			for (VardiyaGun vardiyaGun : aylikVardiyaOzetList) {
				Vardiya vardiya = vardiyaGun.getVardiya();
				row++;
				if (renk) {
					styleGenelLeft = styleOdd;
					styleGenelCenter = styleOddCenter;
					styleTutarDay = styleTutarOddDay;

				} else {
					styleGenelLeft = styleEven;
					styleGenelCenter = styleEvenCenter;
					styleTutarDay = styleTutarEvenDay;

				}
				renk = !renk;
				String bolumAdi = "";
				Personel personel = vardiyaGun.getPersonel();
				if (personel != null)
					bolumAdi = personel.getPlanGrup2() != null ? personel.getPlanGrup2().getAciklama() : "Tanımsız";
				col = 0;
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				ExcelUtil.getCell(sheet, row, col++, styleGenelLeft).setCellValue(bolumAdi);
				Cell cellBaslik = ExcelUtil.getCell(sheet, row, col++, styleGenelCenter);
				String title = vardiya.getVardiyaAciklama();
				if (title != null) {
					Comment comment1 = drawing.createCellComment(anchor);
					RichTextString str1 = factory.createRichTextString(title);
					comment1.setString(str1);
					cellBaslik.setCellComment(comment1);

				}
				cellBaslik.setCellValue(vardiya.getKisaAciklama());

				for (Integer ay : vardiya.getGunlukList()) {
					Cell cell = ExcelUtil.getCell(sheet, row, col++, styleTutarDay);
					cell.setCellValue(ay != 0 ? ay.toString() : "");
				}
			}
		}
		try {
			for (int i = 0; i < col; i++)
				sheet.autoSizeColumn(i);

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
			baosDosya = aylikVardiyaExcelDevam(gorevYeriAciklama, aylikPuantajList, Boolean.TRUE);
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

		LinkedHashMap<String, Object> veriMap = ortakIslemler.getListPersonelOzetVeriMap(aylikPuantajList, aramaSecenekleri.getTesisId(), " ");
		String gorevYeriAciklama = "";
		if (veriMap.containsKey("sirketGrup")) {
			Tanim sirketGrup = (Tanim) veriMap.get("sirketGrup");
			gorevYeriAciklama = sirketGrup.getAciklama() + "_";
		} else if (veriMap.containsKey("sirket")) {
			Sirket sirket = (Sirket) veriMap.get("sirket");
			gorevYeriAciklama = sirket.getAd() + "_";
		}

		if (aramaSecenekleri.getEkSaha3Id() != null || aramaSecenekleri.getTesisId() != null || aramaSecenekleri.getEkSaha4Id() != null) {
			Tanim ekSaha3 = null, ekSaha4 = null, tesis = null;
			if (aramaSecenekleri.getTesisId() != null) {
				if (veriMap.containsKey("tesis"))
					tesis = (Tanim) veriMap.get("tesis");
			}
			if (aramaSecenekleri.getEkSaha3Id() != null) {
				if (veriMap.containsKey("bolum"))
					ekSaha3 = (Tanim) veriMap.get("bolum");
				if (veriMap.containsKey("altBolum"))
					ekSaha4 = (Tanim) veriMap.get("altBolum");
			}

			if (tesis != null)
				gorevYeriAciklama += tesis.getAciklama() + "_";
			if (ekSaha3 != null)
				gorevYeriAciklama += ekSaha3.getAciklama() + "_";
			if (ekSaha4 != null)
				gorevYeriAciklama += ekSaha4.getAciklama() + "_";
		}
		if (seciliEkSaha4Id != null && seciliEkSaha4Id.longValue() > 0L) {
			HashMap parametreMap = new HashMap();
			parametreMap.put("id", seciliEkSaha4Id);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			Tanim ekSaha4 = (Tanim) pdksEntityController.getObjectByInnerObject(parametreMap, Tanim.class);
			if (ekSaha4 != null)
				gorevYeriAciklama += ekSaha4.getAciklama() + "_";
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
				if (personelAylikPuantaj.isGorevYeriSec()) {
					session.clear();
					islemYapiliyor = Boolean.FALSE;
					if (personelAylikPuantaj != null && (!vardiyalarMap.isEmpty() || personelDenklestirme.isGuncellendi()) && personelAylikPuantaj.isKaydet())
						fillAylikVardiyaPlanList();

				} else if (personelAylikPuantaj != null && (!vardiyalarMap.isEmpty() || personelDenklestirme.isGuncellendi()) && personelAylikPuantaj.isKaydet()) {
					if (personelAylikPuantaj.getPersonelDenklestirmeAylik() != null && personelDenklestirme.isGuncellendi())
						session.refresh(personelDenklestirme);
					if (sablonGuncelle) {
						for (VardiyaHafta pdksVardiyaHafta : personelAylikPuantaj.getVardiyaHaftaList()) {
							try {
								if (pdksVardiyaHafta == null || pdksVardiyaHafta.getId() == null || !pdksVardiyaHafta.getGuncellendi())
									continue;
								session.refresh(pdksVardiyaHafta);
							} catch (Exception e) {

							}
						}
					}

					for (Iterator iterator = vardiyalarMap.keySet().iterator(); iterator.hasNext();) {
						String key = (String) iterator.next();
						VardiyaGun pdksVardiyaGun = vardiyalarMap.get(key);
						if (pdksVardiyaGun.getId() != null && pdksVardiyaGun.isGuncellendi()) {
							Boolean refresh = Boolean.TRUE;
							try {
								Vardiya seciliVardiya = pdksVardiyaGun.getVardiya();
								if (seciliVardiya != null && kayitliVardiyalarMap.containsKey(key)) {
									Vardiya pdksVardiya = kayitliVardiyalarMap.get(key);
									refresh = !pdksVardiya.getId().equals(seciliVardiya.getId());
								}
							} catch (Exception e1) {
							}

							if (refresh)
								session.refresh(pdksVardiyaGun);
							if (pdksVardiyaGun.getVardiyaGorev().getId() != null)
								session.refresh(pdksVardiyaGun.getVardiyaGorev());
							else
								pdksVardiyaGun.getVardiyaGorev().setYeniGorevYeri(null);
						}

					}

					if (!helpPersonel(personelAylikPuantaj.getPdksPersonel()))
						ortakIslemler.aylikPlanSureHesapla(true, personelAylikPuantaj, false, tatilGunleriMap, session);
				}
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
	 * @param aylikPuantaj
	 * @param tipi
	 * @return
	 */
	public String aylikPuantajSec(AylikPuantaj aylikPuantaj, String tipi) {
		if (personelDenklestirmeDinamikAlanList == null)
			personelDenklestirmeDinamikAlanList = new ArrayList<PersonelDenklestirmeDinamikAlan>();
		else
			personelDenklestirmeDinamikAlanList.clear();
		aylikPuantajMesaiTalepList = null;
		setPersonelAylikPuantaj(aylikPuantaj);
		manuelHareketEkle = null;
		hareketPdksList = null;
		gebeSutIzniGuncelle = false;
		fazlaMesaiTalep = null;
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

		personelDenklestirme = aylikPuantaj.getPersonelDenklestirmeAylik();

		if (personelDenklestirme.getId() != null)
			session.refresh(personelDenklestirme);
		personelDenklestirme.setGuncellendi(Boolean.FALSE);
		try {
			if (tipi.equals("P")) {
				StringBuffer sb = new StringBuffer();
				sb.append("SP_GET_PERS_DENK_DINAMIK_ALAN");
				LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
				fields.put("pdId", personelDenklestirme.getId().toString());
				fields.put("durum", 0);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List list = pdksEntityController.execSPList(fields, sb, null);
				List<Long> idList = new ArrayList<Long>();
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					Object[] object = (Object[]) iterator.next();
					if (object != null) {
						if (object[0] != null)
							idList.add(((BigDecimal) object[0]).longValue());

					}
				}
				if (!idList.isEmpty()) {
					HashMap map = new HashMap();
					map.put("id", idList);
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<PersonelDenklestirmeDinamikAlan> pdList = pdksEntityController.getObjectByInnerObjectList(map, PersonelDenklestirmeDinamikAlan.class);
					for (PersonelDenklestirmeDinamikAlan personelDenklestirmeDinamikAlan : pdList) {
						personelDenklestirmeDinamikAlan.setGuncellendi(Boolean.FALSE);
						personelDenklestirmeDinamikAlanList.add(personelDenklestirmeDinamikAlan);
					}
					pdList = null;

				}

				list = null;
				idList = null;

			}
		} catch (Exception ed) {
			logger.error(ed);
			ed.printStackTrace();
		}

		aylikPuantaj.setGorevYeriSec(Boolean.FALSE);
		setVardiyaList(fillAllVardiyaList());
		VardiyaGun oncekiVardiya = null;
		TreeMap<String, VardiyaGun> vm = new TreeMap<String, VardiyaGun>();
		if (personel.isSutIzniKullan() || personelDenklestirme.isSutIzniVar())
			gebeSutIzniGuncelle = true;
		VardiyaGun vGun = null, vgAy = null, vgIlkAy = null;
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
			if (pdksVardiyaGun.isAyinGunu())
				vgAy = pdksVardiyaGun;
			if (vGun == null) {
				if (pdksVardiyaGun.isAyinGunu())
					vgIlkAy = pdksVardiyaGun;
			}
			vGun = pdksVardiyaGun;
		}
		Calendar cal = Calendar.getInstance();
		if (vgIlkAy != null) {
			HashMap fields = new HashMap();
			fields.put("personel.id=", vGun.getPersonel().getId());
			fields.put("vardiyaDate>=", ortakIslemler.tariheGunEkleCikar(cal, vgIlkAy.getVardiyaDate(), -7));
			fields.put("vardiyaDate<=", ortakIslemler.tariheGunEkleCikar(cal, vgIlkAy.getVardiyaDate(), -1));
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<VardiyaGun> digerVardiyaGunList = pdksEntityController.getObjectByInnerObjectListInLogic(fields, VardiyaGun.class);
			for (VardiyaGun digerVardiyaGun : digerVardiyaGunList) {
				setVardiyalarToMap(digerVardiyaGun, vm, aylikPuantaj);
			}
		}
		if (vgAy != null && vGun.getId() != null && vGun.getId().equals(vgAy.getId())) {
			HashMap fields = new HashMap();
			fields.put("personel.id=", vGun.getPersonel().getId());
			fields.put("vardiyaDate>=", ortakIslemler.tariheGunEkleCikar(cal, vgAy.getVardiyaDate(), 1));
			fields.put("vardiyaDate<=", ortakIslemler.tariheGunEkleCikar(cal, vgAy.getVardiyaDate(), 7));
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<VardiyaGun> digerVardiyaGunList = pdksEntityController.getObjectByInnerObjectListInLogic(fields, VardiyaGun.class);
			for (VardiyaGun digerVardiyaGun : digerVardiyaGunList) {
				setVardiyalarToMap(digerVardiyaGun, vm, aylikPuantaj);
			}
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
		fazlaMesaiTalep = null;
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
		if (tipi.equals("M")) {
			if (!vm.isEmpty())
				ortakIslemler.fazlaMesaiSaatiAyarla(vm);
			if (denklestirmeAyDurum) {
				List<AylikPuantaj> puantajList = new ArrayList<AylikPuantaj>();
				puantajList.add(aylikPuantaj);
				List<String> list = fazlaMesaiOrtakIslemler.getFazlaMesaiUyari(yil, ay, aramaSecenekleri.getEkSaha3Id(), puantajList, session);
				for (String string : list)
					PdksUtil.addMessageAvailableWarn(string);
			}
		}

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
			for (Iterator iterator = personelDenklestirmeDinamikAlanList.iterator(); iterator.hasNext();) {
				PersonelDenklestirmeDinamikAlan pda = (PersonelDenklestirmeDinamikAlan) iterator.next();
				if (pda.isGuncellendi()) {
					if (pda.getId() != null || pda.getDurum()) {

						flush = true;
					}
				} else
					iterator.remove();

			}
			if (personelAylikPuantaj.getVardiyalar() != null && (!vardiyalarMap.isEmpty() || personelDenklestirme.isGuncellendi()) || flush)
				try {
					aylikVardiyaKontrolKaydet(Boolean.TRUE);
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}

			vardiyalarMap.clear();
			islemYapiliyor = Boolean.FALSE;
		}
		aylikHareketKaydiVardiyaBul = Boolean.FALSE;
		if (denklestirmeAyDurum)
			aylikHareketKaydiVardiyalariBul();

		return "";
	}

	/**
	 * 
	 */
	private void aylikVardiyaOzetOlustur() {
		if (aylikVardiyaOzetList == null)
			aylikVardiyaOzetList = new ArrayList<VardiyaGun>();
		else
			aylikVardiyaOzetList.clear();
		AylikPuantaj aylikPuantajToplam = new AylikPuantaj();
		TreeMap<String, VardiyaGun> vardiyaMap = new TreeMap<String, VardiyaGun>();
		VardiyaGun toplamVardiyaGun = new VardiyaGun();
		Vardiya toplamVardiya = new Vardiya();
		toplamVardiya.setKisaAdi("Toplam");
		toplamVardiya.setId(0L);
		toplamVardiyaGun.setVardiya(toplamVardiya);
		for (AylikPuantaj aylikPuantaj : aylikPuantajList) {
			aylikPuantaj.setDenklestirmeAy(denklestirmeAy);
			aylikPuantaj.setOnayDurum(aylikPuantaj.getPersonelDenklestirmeAylik() == null || aylikPuantaj.getPersonelDenklestirmeAylik().isOnaylandi() == false);
			puantajYetkilendir(vardiyaMap, aylikPuantaj, aylikPuantajToplam, toplamVardiyaGun);

		}
		if (!vardiyaMap.isEmpty()) {
			if (vardiyaMap.containsKey("0"))

				vardiyaMap.remove("0");

			aylikVardiyaOzetList.addAll(new ArrayList(vardiyaMap.values()));
			if (aylikVardiyaOzetList.size() > 1)
				aylikVardiyaOzetList.add(toplamVardiyaGun);
		}

	}

	/**
	 * @param vardiyaIdList
	 * @param tableName
	 * @param columnName
	 * @param tableClass
	 * @return
	 */
	private List getVardiyaTable(List<Long> vardiyaIdList, String tableName, String columnName, Class tableClass) {
		List list = null;
		try {
			if (vardiyaIdList != null && !vardiyaIdList.isEmpty()) {
				HashMap map = new HashMap();
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT P.* FROM " + tableName + " P WITH(nolock) ");
				sb.append(" WHERE P." + columnName + " :id  ");
				map.put("id", vardiyaIdList);
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				list = pdksEntityController.getObjectBySQLList(sb, map, tableClass);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	/**
	 * 
	 */
	private void savePlanLastParameter() {
		LinkedHashMap<String, Object> lastMap = new LinkedHashMap<String, Object>();
		lastMap.put("yil", "" + yil);
		lastMap.put("ay", "" + ay);
		if (aramaSecenekleri.getDepartmanId() != null)
			lastMap.put("departmanId", "" + aramaSecenekleri.getDepartmanId());
		if (aramaSecenekleri.getSirketId() != null)
			lastMap.put("sirketId", "" + aramaSecenekleri.getSirketId());
		if (aramaSecenekleri.getTesisId() != null)
			lastMap.put("tesisId", "" + aramaSecenekleri.getTesisId());
		if (aramaSecenekleri.getEkSaha4Id() != null)
			lastMap.put("altBolumId", "" + aramaSecenekleri.getEkSaha4Id());
		if (aramaSecenekleri.getEkSaha3Id() != null)
			lastMap.put("bolumId", "" + aramaSecenekleri.getEkSaha3Id());
		if ((ikRole) && PdksUtil.hasStringValue(sicilNo))
			lastMap.put("sicilNo", sicilNo.trim());
		try {
			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception e) {
			logger.error(e);
		}
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
		Double yemekMolasiYuzdesi = ortakIslemler.getYemekMolasiYuzdesi(aylikPuantaj.getDenklestirmeAy(), session);
		String aksamBordroAltBirim = ortakIslemler.getParameterKey("sapAksamBordroAltBirim");
		String aksamBordroVardiyaKontrol = ortakIslemler.getParameterKey("aksamBordroVardiyaKontrol");
		List<String> aksamBordroAltBirimleri = Arrays.asList(aksamBordroAltBirim.split(","));
		boolean haftaTatilDurum = ortakIslemler.getParameterKey("haftaTatilDurum").equals("1");
		double aksamVardiyaSaatSayisi = 0d, haftaCalismaSuresi = 0d;
		int aksamVardiyaSayisi = 0;
		VardiyaGun vardiyaGun = null;
		boolean bayramAksamCalismaOde = ortakIslemler.getParameterKey("bayramAksamCalismaOde").equals("1");
		String donem = String.valueOf(yil * 100 + ay);

		for (VardiyaGun pdksVardiyaGun : aylikPuantaj.getVardiyalar()) {
			if (vardiyaGun != null) {
				String tarih = pdksVardiyaGun.getVardiyaDateStr();
				pdksVardiyaGun.setDonemStr(donem);
				pdksVardiyaGun.setAyinGunu(tarih.startsWith(donem));
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
		// List<Vardiya> vardiyalar1 = null;
		// if (vardiyaBolumList == null) {
		// vardiyaBolumList = fillAylikVardiyaList(null, null);
		//
		// }
		// vardiyalar = vardiyaBolumList;

		List<YemekIzin> yemekler = null;
		Date aksamVardiyaBaslangicZamani = null, aksamVardiyaBitisZamani = null;
		String keyDonem = String.valueOf(yil * 100 + ay);
		Personel personel = aylikPuantaj.getPdksPersonel();
		CalismaModeli calismaModeli = null;
		try {
			calismaModeli = aylikPuantaj.getPersonelDenklestirmeAylik().getCalismaModeli();
		} catch (Exception e) {
			calismaModeli = aylikPuantaj.getCalismaModeli();
		}
		Calendar cal = Calendar.getInstance();
		for (VardiyaGun pdksVardiyaGun : aylikPuantaj.getVardiyalar()) {
			pdksVardiyaGun.setAyinGunu(pdksVardiyaGun.getVardiyaDateStr().startsWith(keyDonem));
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

				if (pdksVardiyaGun.getVardiya() != null && pdksVardiyaGun.getIzin() == null) {
					Tanim yeniGoreviYeri = pdksVardiyaGun.getVardiyaGorev().getYeniGorevYeri();
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
										if (haftaTatilDurum && pdksVardiyaGun.getSonrakiVardiya() != null && pdksVardiyaGun.getSonrakiVardiya().isHaftaTatil()) {
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
										logger.error(pdksVardiyaGun.getVardiyaKeyStr() + "\n" + eh);
										eh.printStackTrace();
									}

								}
							}
							vardiyaGunEkle(vardiyaMap, aylikPuantaj, pdksVardiyaGun, index);
							aylikPuantajToplam.setSablonAylikPuantaj(aylikPuantaj.getSablonAylikPuantaj());
							if (aylikPuantajToplam != null)
								vardiyaGunEkle(vardiyaMap, aylikPuantajToplam, toplamVardiyaGun, index);

						}
					if (calismaModeli != null && calismaModeli.isFazlaMesaiVarMi())
						pdksVardiyaGun.setHaftaCalismaSuresi(haftaSuresi);
					haftaCalismaSuresi += haftaSuresi;

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
			List<YemekIzin> yemekList = pdksVardiyaGun.getYemekList();

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

				double sure = girisZaman.getTime() < cikisZaman.getTime() ? PdksUtil.setSureDoubleTypeRounded(ortakIslemler.getSaatSure(girisZaman, cikisZaman, yemekList, pdksVardiyaGun, session), pdksVardiyaGun.getYarimYuvarla()) : 0.0d;

				if (sure > 0) {

					double netSure = vardiya.getNetCalismaSuresi();
					if (sure > vardiya.getNetCalismaSuresi() * yemekMolasiYuzdesi && fazlaMesaiMap.containsKey(AylikPuantaj.MESAI_TIPI_AKSAM_ADET)) {
						aksamVardiyaSayisi += 1;
						if (sure > netSure)
							sure -= netSure;
						else
							sure = 0;
					}
					if (sure > 0)
						aksamVardiyaSaatSayisi += sure;

					if (fazlaMesaiMap.containsKey(AylikPuantaj.MESAI_TIPI_AKSAM_SAAT)) {
						pdksVardiyaGun.addAksamVardiyaSaatSayisi(sure);
						aksamVardiyaSaatSayisi += pdksVardiyaGun.getAksamVardiyaSaatSayisi();
					}

				}

			}
		}
		aylikPuantaj.setVardiyaDegisti(Boolean.FALSE);
		aylikPuantaj.setVardiyaOlustu(Boolean.TRUE);
		aylikPuantaj.setVardiyaDegisti(Boolean.FALSE);
		if (aylikPuantaj.getPersonelDenklestirmeAylik().getDenklestirmeAy().getDurum() && aylikPuantaj.getPersonelDenklestirmeAylik().getEgitimSuresiAksamGunSayisi() != null
				&& (aylikPuantaj.getPersonelDenklestirmeAylik().getPersonel().getEgitimDonemi() == null || !aylikPuantaj.getPersonelDenklestirmeAylik().getPersonel().getEgitimDonemi())) {
			try {
				aylikPuantaj.getPersonelDenklestirmeAylik().setEgitimSuresiAksamGunSayisi(null);

			} catch (Exception ee) {
				ee.printStackTrace();
			}

		}

		try {
			personel = aylikPuantaj != null ? aylikPuantaj.getPdksPersonel() : null;
			if (personel != null && (aksamBordroVardiyaKontrol.equals("1")) && (personel.getBordroAltAlan() == null || !aksamBordroAltBirimleri.contains(personel.getBordroAltAlan().getKodu()))) {
				aksamVardiyaSaatSayisi = 0d;

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (haftaCalismaSuresi > 0)
			ortakIslemler.aylikPlanSureHesapla(false, aylikPuantaj, false, tatilGunleriMap, session);

		aylikPuantaj.setAksamVardiyaSaatSayisi(aksamVardiyaSaatSayisi);
		aylikPuantaj.setAksamVardiyaSayisi(aksamVardiyaSayisi);

	}

	/**
	 * @param list
	 * @param pdksVardiyaGun
	 * @param vardiyaMap
	 */
	private void setVardiyaGunleri(List<Vardiya> list, VardiyaGun pdksVardiyaGun, TreeMap<Long, Vardiya> vardiyaMap) {
		Vardiya vardiya = pdksVardiyaGun.getVardiya();
		if (vardiya != null && vardiya.isFMI() && pdksVardiyaGun.isKullaniciYetkili()) {
			pdksVardiyaGun.setKullaniciYetkili(fazlaMesaiIzinRaporuDurum);
		}
		if (list == null || ((vardiya != null && vardiyaMap.containsKey(vardiya.getId())) && pdksVardiyaGun.getVardiya().getDurum() && pdksVardiyaGun.getVardiya().isFMI() == false))
			pdksVardiyaGun.setVardiyalar((ArrayList<Vardiya>) list);
		else {
			ArrayList<Vardiya> value = new ArrayList<Vardiya>(list);
			if (pdksVardiyaGun.isAyinGunu() == false && vardiya != null && vardiya.getDurum()) {
				value.add(vardiya);
			} else
				pdksVardiyaGun.setKontrolVardiyalar(value);
		}

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
	@Transactional
	private void aylikVardiyaKontrolKaydet(Boolean aylik) throws Exception {
		boolean flush = Boolean.FALSE;
		boolean tekrarOku = Boolean.FALSE;
		boolean baskaKayitVar = Boolean.FALSE;
		String haftaTatilDurum = ortakIslemler.getParameterKey("haftaTatilDurum");
		haftaTatilMesaiDurum = haftaTatilDurum.equals("1");
		VardiyaPlan plan = personelAylikPuantaj.getVardiyaPlan();
		Personel personel = personelAylikPuantaj.getPdksPersonel();
		boolean pdGuncellendi = personelDenklestirme.isGuncellendi() && !personelDenklestirme.isKapandi();
		gorevli = helpPersonel(personel);
		if (plan.getVardiyaGunMap() == null)
			plan.setVardiyaGunMap(new TreeMap<String, VardiyaGun>());
		else
			plan.getVardiyaGunMap().clear();
		boolean durum = !vardiyalarMap.isEmpty();
		if (!helpPersonel(personelAylikPuantaj.getPdksPersonel()))
			ortakIslemler.aylikPlanSureHesapla(true, personelAylikPuantaj, false, tatilGunleriMap, session);
		TreeMap<Long, VardiyaGun> mesaiMap = new TreeMap<Long, VardiyaGun>();
		for (VardiyaGun pdksVardiyaGun : personelAylikPuantaj.getVardiyalar()) {
			if (pdksVardiyaGun.isGuncellendi() || pdksVardiyaGun.getId() == null) {
				try {
					if (pdksVardiyaGun.getId() == null && pdksVardiyaGun.getVardiya() != null) {
						tekrarOku = true;
						pdksEntityController.saveOrUpdate(session, entityManager, pdksVardiyaGun);
					}
					Long newVardiyaId = pdksVardiyaGun.getVardiya() != null ? pdksVardiyaGun.getVardiya().getId() : null;
					Long eskiVardiyaId = pdksVardiyaGun.getEskiVardiya() != null ? pdksVardiyaGun.getEskiVardiya().getId() : null;
					if (pdksVardiyaGun.getId() != null && PdksUtil.isLongDegisti(newVardiyaId, eskiVardiyaId)) {
						pdksVardiyaGun.setGuncelleyenUser(authenticatedUser);
						pdksVardiyaGun.setGuncellemeTarihi(new Date());
						pdksVardiyaGun.setDurum(Boolean.FALSE);
						if (pdksVardiyaGun.getVersion() < 0)
							pdksVardiyaGun.setVersion(0);
						mesaiMap.put(pdksVardiyaGun.getId(), pdksVardiyaGun);
						tekrarOku = Boolean.TRUE;
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
				durum = vardiyaPlanKontrol(personelDenklestirme, null, plan, "", false);
			if (durum) {
				if (personelDenklestirme.isGuncellendi())

					flush = Boolean.TRUE;

				for (VardiyaGun pdksVardiyaGun : personelAylikPuantaj.getVardiyalar()) {
					if (pdksVardiyaGun.getVardiya() != null && pdksVardiyaGun.getId() != null) {
						VardiyaGorev pdksVardiyaGorev = pdksVardiyaGun.getVardiyaGorev();
						if (pdksVardiyaGun.isGuncellendi()) {

							if (pdksVardiyaGun.getVardiya().isCalisma())
								vardiyaSaatDakika(pdksVardiyaGun, false);
							else {
								pdksVardiyaGun.setBasSaat(null);
								pdksVardiyaGun.setBasDakika(null);
								pdksVardiyaGun.setBitSaat(null);
								pdksVardiyaGun.setBitDakika(null);
							}
							logger.debug("Plan " + pdksVardiyaGun.getVardiyaKeyStr());
							try {
								if (pdksVardiyaGun.getId() != null) {
									Long newVardiyaId = pdksVardiyaGun.getVardiya() != null ? pdksVardiyaGun.getVardiya().getId() : null;
									Long eskiVardiyaId = pdksVardiyaGun.getEskiVardiya() != null ? pdksVardiyaGun.getEskiVardiya().getId() : null;
									if (PdksUtil.isLongDegisti(newVardiyaId, eskiVardiyaId)) {
										pdksVardiyaGun.setGuncelleyenUser(authenticatedUser);
										pdksVardiyaGun.setGuncellemeTarihi(new Date());
										pdksVardiyaGun.setDurum(Boolean.FALSE);
										tekrarOku = Boolean.TRUE;
									}
								}
							} catch (Exception e) {
							}

							if (pdksVardiyaGun.getId() == null || mesaiMap.containsKey(pdksVardiyaGun.getId())) {
								pdksVardiyaGun.setDurum(Boolean.FALSE);
								if (pdksVardiyaGun.getId() == null)
									tekrarOku = true;
								pdksEntityController.saveOrUpdate(session, entityManager, pdksVardiyaGun);

								if (!pdGuncellendi)
									pdGuncellendi = pdksVardiyaGun.isAyinGunu();
								if (pdGuncellendi)
									personelDenklestirme.setOnaylandi(Boolean.FALSE);
							}
							flush = Boolean.TRUE;
							if (!pdksVardiyaGun.getVardiya().isCalisma())
								pdksVardiyaGorev.setYeniGorevYeri(null);
							if (gorevli) {
								if (pdksVardiyaGorev.getYeniGorevYeri() == null) {
									pdksVardiyaGorev.setBolumKat(null);
									tekrarOku = Boolean.TRUE;
								} else if (!gorevYerileri.contains(pdksVardiyaGorev.getYeniGorevYeri().getId())) {
									pdksVardiyaGorev.setBolumKat(null);
									tekrarOku = Boolean.TRUE;
								}

							}

							if (pdksVardiyaGorev.isShiftGorevli() || !pdksVardiyaGorev.isOzelDurumYok() || pdksVardiyaGorev.getYeniGorevYeri() != null || pdksVardiyaGorev.getBolumKat() != null) {
								if (pdksVardiyaGun.getId() == null)
									tekrarOku = true;
								if (gorevYeriGirisDurum)
									pdksEntityController.saveOrUpdate(session, entityManager, pdksVardiyaGorev);
								logger.debug("Gorev " + pdksVardiyaGun.getVardiyaKeyStr());
								flush = Boolean.TRUE;
							}

							else if (pdksVardiyaGorev.getId() != null) {

								pdksEntityController.deleteObject(session, entityManager, pdksVardiyaGorev);

								pdksVardiyaGorev.setId(null);
								flush = Boolean.TRUE;
							}
							if (pdksVardiyaGun.isAyinGunu()) {
								personelDenklestirme.setGuncellendi(Boolean.TRUE);

							}

							flush = Boolean.TRUE;
						} else if (gorevli && pdksVardiyaGorev.getYeniGorevYeri() != null && !baskaKayitVar) {
							Tanim yeniGorevYeri = pdksVardiyaGorev.getYeniGorevYeri();
							baskaKayitVar = gorevYerileri.contains(yeniGorevYeri.getId());
						}

					}
				}
				if (!mesaiMap.isEmpty()) {
					HashMap fields = new HashMap();
					fields.put("vardiyaGun.id", new ArrayList(mesaiMap.keySet()));
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<PersonelFazlaMesai> list = pdksEntityController.getObjectByInnerObjectList(fields, PersonelFazlaMesai.class);
					for (PersonelFazlaMesai fazlaMesai : list) {
						if (fazlaMesai.isOnaylandi()) {
							fazlaMesai.setFazlaMesaiSaati(0.0d);
							fazlaMesai.setOnayDurum(PersonelFazlaMesai.DURUM_ONAYLANMADI);
							fazlaMesai.setGuncelleyenUser(authenticatedUser);
							fazlaMesai.setGuncellemeTarihi(new Date());
							pdksEntityController.saveOrUpdate(session, entityManager, fazlaMesai);
							flush = Boolean.TRUE;
						}
					}
				}

			}
		} else {
			flush = personelDenklestirme.isGuncellendi();
		}
		for (Iterator iterator = personelDenklestirmeDinamikAlanList.iterator(); iterator.hasNext();) {
			PersonelDenklestirmeDinamikAlan pda = (PersonelDenklestirmeDinamikAlan) iterator.next();
			if (pda.isGuncellendi()) {
				if (pda.getId() != null || pda.getDurum()) {
					pdksEntityController.saveOrUpdate(session, entityManager, pda);
					flush = true;
				}
			} else
				iterator.remove();

		}
		if (!flush)
			flush = pdGuncellendi;
		if (flush) {
			if (pdGuncellendi) {
				boolean sutIzin = personel.isSutIzniKullan() || personelDenklestirme.isSutIzniVar();
				if (!sutIzin && gebeSutIzniGuncelle == false) {
					if (personelDenklestirme.getSutIzniSaatSayisi() != null && personelDenklestirme.getSutIzniSaatSayisi().doubleValue() > 0.0d)
						personelDenklestirme.setSutIzniSaatSayisi(0.0d);
				}

				if (personelDenklestirme.getFazlaMesaiIzinKullan())
					personelDenklestirme.setFazlaMesaiOde(Boolean.FALSE);
				savePersonelDenklestirme(personelDenklestirme);
				logger.debug("Denklestirme " + personelDenklestirme.getPersonel().getPdksSicilNo());
			}
			logger.debug("Veri tabanına kayıt ediliyor");
			suaKontrol(aylikPuantajList);
			try {
				session.flush();
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
			if (flush) {
				logger.debug("Plan kayıt edildi");
				for (VardiyaGun pdksVardiyaGun : aylikPuantajDefault.getVardiyalar()) {
					if (pdksVardiyaGun.isAyinGunu()) {
						pdksVardiyaGun.setCheckBoxDurum(Boolean.FALSE);
					}
				}
				if (tekrarOku && !baskaKayitVar)
					fillAylikVardiyaPlanList();
				else if (!helpPersonel(personelAylikPuantaj.getPdksPersonel()))
					ortakIslemler.aylikPlanSureHesapla(true, personelAylikPuantaj, true, tatilGunleriMap, session);
				aylikVardiyaOzetOlustur();
			}

		}

		logger.debug("İşlem bitti.");
	}

	/**
	 * @param sessionInput
	 * @param entityManagerInput
	 * @param pdksEntityControllerInput
	 * @param ortakIslemlerInput
	 * @param fazlaMesaiOrtakIslemlerInput
	 */
	public void setInject(Session sessionInput, EntityManager entityManagerInput, PdksEntityController pdksEntityControllerInput, OrtakIslemler ortakIslemlerInput, FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemlerInput) {
		if (sessionInput != null && session == null)
			this.session = sessionInput;
		if (entityManagerInput != null && entityManager == null)
			this.entityManager = entityManagerInput;
		if (pdksEntityControllerInput != null && pdksEntityController == null)
			this.pdksEntityController = pdksEntityControllerInput;
		if (ortakIslemlerInput != null && ortakIslemler == null)
			this.ortakIslemler = ortakIslemlerInput;
		if (fazlaMesaiOrtakIslemlerInput != null && fazlaMesaiOrtakIslemler == null)
			this.fazlaMesaiOrtakIslemler = fazlaMesaiOrtakIslemlerInput;
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

		List<SelectItem> departmanListe = fazlaMesaiOrtakIslemler.getFazlaMesaiDepartmanList(denklestirmeAy != null ? aylikPuantajDonem : null, getDenklestirmeDurum(), session);
		fazlaMesaiListeGuncelle(null, "D", departmanListe);
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
	 * @param dm
	 * @return
	 */
	private void setAylikPuantajDonem(DenklestirmeAy dm) {
		AylikPuantaj apd = null;
		try {
			if (dm != null && dm.getYil() == yil && dm.getAy() == ay) {
				if (aylikPuantajDonem == null)
					apd = new AylikPuantaj(dm);
				else {
					apd = aylikPuantajDonem;
					apd.setDenklestirmeAy(dm);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (apd == null)
			apd = new AylikPuantaj();
		if (loginUser == null) {
			if (authenticatedUser != null) {
				loginUser = authenticatedUser;
			} else {
				loginUser = ortakIslemler.getSistemAdminUser(session);
				loginUser.setAdmin(Boolean.TRUE);
			}
			loginUser.setLogin(authenticatedUser != null);

		}

		apd.setLoginUser(loginUser);
		setAylikPuantajDonem(apd);
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
		sb.append("select DISTINCT D.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
		if (fazlaMesaiTalepDurum == false) {
			if (buYil > yil) {
				sb.append(" INNER  JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=D." + DenklestirmeAy.COLUMN_NAME_ID);
				sb.append("  AND  PD." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + "=1");
			}
		} else {
			sb.append(" INNER JOIN " + VardiyaGun.TABLE_NAME + " VG ON YEAR(VG." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ")=" + yil);
			sb.append(" AND MONTH(VG." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ")=D." + DenklestirmeAy.COLUMN_NAME_AY);
			sb.append(" INNER JOIN " + FazlaMesaiTalep.TABLE_NAME + " FT ON FT." + FazlaMesaiTalep.COLUMN_NAME_VARDIYA_GUN + "=VG." + VardiyaGun.COLUMN_NAME_ID);
			sb.append(" AND FT." + FazlaMesaiTalep.COLUMN_NAME_DURUM + " =1");
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
		} else
			setAylikPuantajDonem(denklestirmeAy);
		setDenklestirmeAyDurum(fazlaMesaiOrtakIslemler.getDurum(denklestirmeAy));

	}

	/**
	 * @param tesisDoldur
	 */
	public void fillSirketDoldur(boolean tesisDoldur) {
		if (aylikPuantajList != null) {
			aylikPuantajList.clear();
			aylikVardiyaOzetList.clear();
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

				sirketler = fazlaMesaiOrtakIslemler.getFazlaMesaiMudurSirketList(adminRole ? aramaSecenekleri.getDepartmanId() : null, denklestirmeAy != null ? aylikPuantajDonem : null, getDenklestirmeDurum(), fazlaMesaiTalepDurum, session);
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

		List<SelectItem> list = fazlaMesaiOrtakIslemler.getFazlaMesaiMudurTesisList(aramaSecenekleri.getSirket(), denklestirmeAy != null ? aylikPuantajDonem : null, getDenklestirmeDurum(), fazlaMesaiTalepDurum, session);
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

		list = fazlaMesaiOrtakIslemler.getFazlaMesaiMudurBolumList(sirket, tesisId, denklestirmeAy != null ? aylikPuantajDonem : null, getDenklestirmeDurum(), fazlaMesaiTalepDurum, session);
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
		fazlaMesaiTalepler.clear();
		ekSaha4Tanim = null;
		if (adminRole)
			fillDepartmanList();
		listeleriTemizle();
		setDepartman(null);
		aramaSecenekleri.setGorevYeriList(null);
		if (aramaSecenekleri.getDepartmanId() != null) {

			List<SelectItem> sirketler = fazlaMesaiOrtakIslemler.getFazlaMesaiSirketList(adminRole ? aramaSecenekleri.getDepartmanId() : null, denklestirmeAy != null ? aylikPuantajDonem : null, getDenklestirmeDurum(), session);
			Sirket sirket = null;
			fazlaMesaiListeGuncelle(null, "S", sirketler);
			if (!sirketler.isEmpty()) {
				Long onceki = aramaSecenekleri.getSirketId();
				if (sirketler.size() == 1) {
					aramaSecenekleri.setSirketId((Long) sirketler.get(0).getValue());
					onceki = aramaSecenekleri.getSirketId();
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
					ekSaha4Tanim = ortakIslemler.getEkSaha4(sirket, null, session);

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
		aylikVardiyaOzetList.clear();
		fazlaMesaiTalepler.clear();
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
			TreeMap<Long, Vardiya> vardiyaMap = new TreeMap<Long, Vardiya>();
			for (Vardiya vardiya : vardiyalar)
				vardiyaMap.put(vardiya.getId(), vardiya);
			for (VardiyaGun pdksVardiyaGun : aylikPuantaj.getVardiyalar()) {
				if (pdksVardiyaGun.getVardiya() != null) {
					List<Vardiya> list = pdksVardiyaGun.getIzin() == null || (pdksVardiyaGun.getVardiya().isFMI() && fazlaMesaiIzinRaporuDurum) ? vardiyalar : null;
					setVardiyaGunleri(list, pdksVardiyaGun, vardiyaMap);
					pdksVardiyaGun.setIslemVardiya(null);
					pdksVardiyaGun.setIslendi(false);
				}
			}
		}
	}

	/**
	 * @param aylikPuantaj
	 * @return
	 */
	public String aylikGorevYeriDegistir(AylikPuantaj aylikPuantaj) {

		vardiyalarMap.clear();
		if (aylikPuantaj != null) {
			String mesaj = "";
			boolean gorevYeriSec = Boolean.FALSE;
			HashMap<String, VardiyaGun> map = new HashMap<String, VardiyaGun>();
			for (VardiyaGun pdksVardiyaGun : aylikPuantajDefault.getAyinVardiyalari()) {
				if (pdksVardiyaGun.isCheckBoxDurum()) {
					String gun = pdksVardiyaGun.getVardiyaDateStr();
					map.put(gun, pdksVardiyaGun);
				}
			}
			if (!map.isEmpty()) {
				setVardiyaList(fillAllVardiyaList());
				setPersonelAylikPuantaj(aylikPuantaj);
				planYetkilendir(aylikPuantaj);

				for (VardiyaGun perVardiyaGun : aylikPuantaj.getVardiyalar()) {
					String gun = perVardiyaGun.getVardiyaDateStr();
					perVardiyaGun.setGuncellendi(Boolean.FALSE);
					perVardiyaGun.saklaVardiya();
					if (perVardiyaGun.isAyinGunu() && perVardiyaGun.getVardiya() != null && perVardiyaGun.getIzin() == null) {
						if (perVardiyaGun.isKullaniciYetkili()) {
							perVardiyaGun.setKullaniciYetkili(map.containsKey(gun));
							if (perVardiyaGun.isKullaniciYetkili())
								gorevYeriSec = Boolean.TRUE;
						}

					} else
						perVardiyaGun.setKullaniciYetkili(false);
				}

				if (gorevYeriSec) {
					HashMap fields = new HashMap();
					fields.put("parentTanim.kodu", "ekSaha3");
					fields.put("parentTanim.tipi", Tanim.TIPI_PERSONEL_EK_SAHA);
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					gorevYeriTanimList = pdksEntityController.getObjectByInnerObjectList(fields, Tanim.class);
					if (aramaSecenekleri.getGorevYeriList() != null && !gorevli) {
						List<Long> idler = new ArrayList<Long>();
						if ((authenticatedUser.isYonetici() || authenticatedUser.isYoneticiKontratli()) && !authenticatedUser.isDirektorSuperVisor()) {
							for (Iterator iterator2 = aramaSecenekleri.getGorevYeriList().iterator(); iterator2.hasNext();) {
								SelectItem item = (SelectItem) iterator2.next();
								idler.add((Long) item.getValue());
							}
						}
						if (aramaSecenekleri.getEkSaha3Id() != null && idler.isEmpty())
							idler.add(aramaSecenekleri.getEkSaha3Id());
						if (idler.isEmpty())
							gorevYeriTanimList.clear();
						else {
							Long gorevYeriTanimId = authenticatedUser.isIK() ? aramaSecenekleri.getEkSaha3Id() : null;
							for (Iterator iterator = gorevYeriTanimList.iterator(); iterator.hasNext();) {
								Tanim tanim = (Tanim) iterator.next();
								if (!tanim.getDurum() || (gorevYeriTanimId == null && idler.contains(tanim.getId())) || (gorevYeriTanimId != null && gorevYeriTanimId.equals(tanim.getId())))
									iterator.remove();

							}
						}

						idler = null;
					}
					if (gorevYeriTanimList.size() > 1)
						gorevYeriTanimList = PdksUtil.sortObjectStringAlanList(gorevYeriTanimList, "getAciklama", null);
					aylikPuantaj.setGorevYeriSec(Boolean.TRUE);
					aylikPuantaj.setKaydet(Boolean.TRUE);
					personelDenklestirme = aylikPuantaj.getPersonelDenklestirmeAylik();
					personelDenklestirme.setGuncellendi(Boolean.FALSE);
				} else
					mesaj = "Görev yeri değişecek çalışılan gün seçiniz!";
				map = null;
			} else
				mesaj = "Görev yeri değişecek gün seçiniz!";
			map = null;
			if (PdksUtil.hasStringValue(mesaj))
				PdksUtil.addMessageWarn(mesaj);
			if (aylikPuantaj != null)
				aylikPuantaj.setGorevYeriSec(gorevYeriSec);
			vardiyalarSec(aylikPuantaj);
		}
		setPersonelAylikPuantaj(aylikPuantaj);
		return "";
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
	 * @return
	 */
	@Transactional
	public String planOnayla() {
		boolean devam = Boolean.FALSE;
		HashMap<Long, User> yoneticiMap = new HashMap<Long, User>();
		HashMap<Long, Boolean> yoneticiOnayMap = new HashMap<Long, Boolean>();
		HashMap<Long, List<AylikPuantaj>> yoneticiPuantajMap = new HashMap<Long, List<AylikPuantaj>>();
		List<AylikPuantaj> puantajList = new ArrayList<AylikPuantaj>();
		List<Long> perIdList = new ArrayList<Long>();
		String aciklama = null, veriAyrac = "_";
		boolean mesajYaz = true;
		String genelMudurERPKoduStr = ortakIslemler.getParameterKey("genelMudurERPKodu");
		boolean tumOnaylanPlanEkle = ortakIslemler.getParameterKey("tumOnaylanPlanEkle").equals("1");
		boolean yoneticiTanimsiz = ortakIslemler.getParameterKey("yoneticiTanimsiz").equals("1");
		List<String> genelMudurERPKodlari = !PdksUtil.hasStringValue(genelMudurERPKoduStr) ? new ArrayList<String>() : PdksUtil.getListByString(genelMudurERPKoduStr, null);
		List<Long> eskiOnayList = new ArrayList<Long>();
		TreeMap<Long, TreeMap<Long, Vardiya>> calismaModeliMap = getCalismaModeliMap(aylikPuantajList);
		for (Iterator iter = aylikPuantajList.iterator(); iter.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();
			aylikPuantaj.setSecili(false);
			long yoneticiPersonelId = 0;
			TreeMap<Long, Vardiya> vardiyaMap = null;
			if (aylikPuantaj.getCalismaModeli() != null) {
				Long id = aylikPuantaj.getCalismaModeli().getId();
				if (calismaModeliMap.containsKey(id))
					vardiyaMap = calismaModeliMap.get(id);
			}
			Personel personel = aylikPuantaj.getPdksPersonel();
			if (denklestirmeAy.isDurumu() && !(authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi())) {
				if (personel.isSanalPersonelMi() == false && (yoneticiTanimsiz == false && (aylikPuantaj.getYonetici() == null || aylikPuantaj.getYonetici().getId() == null))) {
					aylikPuantaj.setOnayDurum(false);
				}
			}
			boolean onayDurum = aylikPuantaj.isOnayDurum();
			VardiyaPlan pdksVardiyaPlan = aylikPuantaj.getVardiyaPlan();
			boolean asilOnay = false;
			PersonelDenklestirme personelDenklestirme = aylikPuantaj.getPersonelDenklestirmeAylik();
			if (onayDurum) {
				mesajYaz = false;
				onayDurum = vardiyaPlanKontrol(personelDenklestirme, vardiyaMap, pdksVardiyaPlan, personel.getSicilNo() + " " + personel.getAdSoyad() + " ", false);
				asilOnay = true;
			} else if (tumOnaylanPlanEkle) {
				onayDurum = personelDenklestirme != null && personelDenklestirme.isOnaylandi();
				eskiOnayList.add(personel.getId());
			}

			if (onayDurum) {
				User userYonetici = null;
				if (personel.getPdksYonetici() != null) {
					yoneticiPersonelId = personel.getPdksYonetici().getId();
					userYonetici = yoneticiMap.containsKey(yoneticiPersonelId) ? yoneticiMap.get(yoneticiPersonelId) : null;
					if (userYonetici == null) {
						HashMap parametreMap = new HashMap();
						parametreMap.put("pdksPersonel.id", yoneticiPersonelId);
						if (session != null)
							parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						userYonetici = (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);

					}
					if (userYonetici != null) {
						if (asilOnay || !yoneticiOnayMap.containsKey(yoneticiPersonelId))
							yoneticiOnayMap.put(yoneticiPersonelId, asilOnay);
						List<AylikPuantaj> list = yoneticiPuantajMap.containsKey(yoneticiPersonelId) ? (List<AylikPuantaj>) yoneticiPuantajMap.get(yoneticiPersonelId) : new ArrayList<AylikPuantaj>();
						if (list.isEmpty()) {
							yoneticiMap.put(yoneticiPersonelId, userYonetici);
							yoneticiPuantajMap.put(yoneticiPersonelId, list);
						}
						list.add(aylikPuantaj);
					}
				}
				if (yoneticiPersonelId == 0) {
					List<AylikPuantaj> list = yoneticiPuantajMap.containsKey(yoneticiPersonelId) ? (List<AylikPuantaj>) yoneticiPuantajMap.get(yoneticiPersonelId) : new ArrayList<AylikPuantaj>();
					if (list.isEmpty())
						yoneticiPuantajMap.put(yoneticiPersonelId, list);
					list.add(aylikPuantaj);
				}

				if (!devam)
					devam = Boolean.TRUE;
				puantajList.add(aylikPuantaj);
				if (aylikPuantaj.isOnayDurum())
					perIdList.add(personel.getId());

			}

		}
		if (!devam) {
			if (mesajYaz)
				PdksUtil.addMessageAvailableWarn("Onaylanacak plan bilgisi bulunamadı!");
		}

		else {

			Boolean onayDurum = Boolean.FALSE;
			String calismaPlanOnayMailAdres = ortakIslemler.getParameterKey("calismaPlanOnayMail");
			Map<String, String> mapReq = null;
			try {

				mapReq = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
				String webAdres = mapReq.containsKey("host") ? mapReq.get("host") : "";
				if (webAdres.equals("localhost:8080"))
					calismaPlanOnayMailAdres = "";
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());

			}
			if (departman != null && !departman.isAdminMi()) {
				for (AylikPuantaj aylikPuantaj : puantajList) {
					if (aylikPuantaj.isOnayDurum()) {
						Boolean durum = saveOnay(session, aylikPuantaj);
						aylikPuantaj.setOnayDurum(false);
						if (durum)
							onayDurum = Boolean.TRUE;
					}
				}
			} else {
				HashMap fields = new HashMap();
				fields.put(PdksEntityController.MAP_KEY_MAP, "getPersonelId");
				fields.put("pdksPersonel.id", perIdList);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				TreeMap<Long, User> perUserMap = pdksEntityController.getObjectByInnerObjectMap(fields, User.class, Boolean.FALSE);
				Tanim bolum = null;
				if (aramaSecenekleri.getEkSaha3Id() != null) {
					fields.clear();
					fields.put("id", aramaSecenekleri.getEkSaha3Id());
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					bolum = (Tanim) pdksEntityController.getObjectByInnerObject(fields, Tanim.class);

				}
				if (toList == null)
					toList = new ArrayList<User>();
				else
					toList.clear();
				if (ccList == null)
					ccList = new ArrayList<User>();
				else
					ccList.clear();
				if (bccList == null)
					bccList = new ArrayList<User>();
				else
					bccList.clear();

				LinkedHashMap<String, Object> veriMap = ortakIslemler.getListPersonelOzetVeriMap(puantajList, aramaSecenekleri.getTesisId(), veriAyrac);
				if (veriMap.containsKey("bolum"))
					bolum = (Tanim) veriMap.get("bolum");
				aciklama = veriMap.containsKey("aciklama") ? (String) veriMap.get("aciklama") : null;
				if (aciklama == null && bolum != null)
					aciklama = bolum.getAciklama();
				dosyaAdi = "AylıkÇalışmaPlanı_" + PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyyMM") + (bolum != null ? "_" + bolum.getAciklama() : "") + ".xlsx";
				mailKonu = PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyy MMMMMM") + " ayı" + (aciklama != null ? " " + PdksUtil.replaceAll(aciklama, veriAyrac, " ") : "") + " çalışma planı";
				for (AylikPuantaj aylikPuantaj : puantajList) {
					Personel personel = aylikPuantaj.getPdksPersonel();
					toList.clear();
					if (eskiOnayList.contains(personel.getId()) || perUserMap.size() < 2 || !perUserMap.containsKey(personel.getId())) {
						Boolean durum = saveOnay(session, aylikPuantaj);
						aylikPuantaj.setSecili(!durum);
						if (durum)
							onayDurum = Boolean.TRUE;
						continue;
					}
					mailIcerik = "<P>Sayın " + personel.getAdSoyad() + ",</P><br/> ";
					mailIcerik += "<P>" + PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyy MMMMMM  ") + " ayı" + (aciklama != null ? " " + PdksUtil.replaceAll(aciklama, veriAyrac, " ") : "") + " çalışma planınız ekteki dosyadır.</p><br/><br/>";
					toList.add(perUserMap.get(personel.getId()));
					aylikPuantaj.setSecili(Boolean.TRUE);
					String gorevYeriAciklama = getExcelAciklama();
					baosDosya = aylikVardiyaExcelDevam(gorevYeriAciklama, puantajList, Boolean.FALSE);
					mailData = baosDosya.toByteArray();

					try {
						if (PdksUtil.hasStringValue(calismaPlanOnayMailAdres) && mailData != null) {
							MailObject mail = new MailObject();
							mail.setSubject(mailKonu);
							mail.setBody(mailIcerik);
							ortakIslemler.addMailPersonelUserList(toList, mail.getToList());
							ortakIslemler.addMailPersonelUserList(ccList, mail.getCcList());
							ortakIslemler.addMailPersonelUserList(bccList, mail.getBccList());
							if (!PdksUtil.hasStringValue(dosyaAdi))
								dosyaAdi = "AylıkÇalışmaPlanı_" + gorevYeriAciklama + PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyyMM") + ".xlsx";

							MailFile mf = new MailFile();
							mf.setDisplayName(dosyaAdi);
							mf.setIcerik(mailData);
							mail.getAttachmentFiles().add(mf);
							ortakIslemler.mailSoapServisGonder(true, mail, renderer, "/email/" + calismaPlanOnayMailAdres, session);
						}

						Boolean durum = saveOnay(session, aylikPuantaj);
						if (durum)
							onayDurum = Boolean.TRUE;
						aylikPuantaj.setSecili(!durum);
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());
						logger.error("hata /email/" + calismaPlanOnayMailAdres + " " + e.getMessage());
						PdksUtil.addMessageError("Mesaj gönderilmemiştir. " + e.getMessage());

					}
					toList.clear();

				}
				if (planDepartman != null) {
					HashMap map = new HashMap();
					map.put("parentTanim.tipi", Tanim.TIPI_PERSONEL_EK_SAHA);
					map.put("parentTanim.kodu", "ekSaha3");
					map.put("kodu", Personel.BOLUM_SUPERVISOR);
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					Tanim superVisor = (Tanim) pdksEntityController.getObjectByInnerObject(map, Tanim.class);
					if (superVisor != null) {
						map.clear();
						map.put("pdksPersonel.ekSaha1.id=", planDepartman.getId());
						map.put("pdksPersonel.iseBaslamaTarihi<=", aylikPuantajDefault.getSonGun());
						map.put("pdksPersonel.sskCikisTarihi>=", aylikPuantajDefault.getIlkGun());
						map.put("durum=", Boolean.TRUE);
						map.put("pdksPersonel.durum=", Boolean.TRUE);
						map.put("pdksPersonel.ekSaha3.id=", superVisor.getId());
						if (session != null)
							map.put(PdksEntityController.MAP_KEY_SESSION, session);
						List<User> superVisorUserList = null;
						try {
							superVisorUserList = pdksEntityController.getObjectByInnerObjectListInLogic(map, User.class);
						} catch (Exception e) {
							logger.error("Pdks hata in : \n");
							e.printStackTrace();
							logger.error("Pdks hata out : " + e.getMessage());
							logger.debug(e.getMessage());
							superVisorUserList = new ArrayList<User>();
						}
						if (!superVisorUserList.isEmpty())
							ccList.addAll(superVisorUserList);
					}
					HashMap parametreMap = new HashMap();

					parametreMap.put("departman.id", planDepartman.getId());
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<DepartmanMailGrubu> departmanMailGrubuList = pdksEntityController.getObjectByInnerObjectList(parametreMap, DepartmanMailGrubu.class);
					for (DepartmanMailGrubu departmanMailGrubu : departmanMailGrubuList) {
						User user = new User();
						user.setEmail(departmanMailGrubu.getMailAdress());
						ccList.add(user);
					}
				}
			}
			String servisMailAdresleri = ortakIslemler.getParameterKey("servisMailAdresleri");
			if (PdksUtil.hasStringValue(servisMailAdresleri)) {
				List<String> mailList = PdksUtil.getListFromString(servisMailAdresleri, null);
				for (String mail : mailList) {
					User user = new User();
					user.setEmail(mail);
					bccList.add(user);
				}

			}

			for (Iterator iterator = yoneticiPuantajMap.keySet().iterator(); iterator.hasNext();) {
				Long yoneticiPersonelId = (Long) iterator.next();
				if (!yoneticiOnayMap.containsKey(yoneticiPersonelId) || !yoneticiOnayMap.get(yoneticiPersonelId))
					continue;
				mailIcerik = "";
				if (toList == null)
					toList = new ArrayList<User>();
				else
					toList.clear();
				if (yoneticiMap.containsKey(yoneticiPersonelId)) {
					User yonetici = yoneticiMap.get(yoneticiPersonelId);
					Personel personel = yonetici.getPdksPersonel();
					if (personel.getGorevTipi() == null || !genelMudurERPKodlari.contains(personel.getGorevTipi().getErpKodu())) {
						mailIcerik += "<P>Sayın " + personel.getAdSoyad() + ",</P><br/> ";
						toList.add(yonetici);
					} else
						continue;
				}
				if ((toList != null && !toList.isEmpty()) || (ccList != null && !ccList.isEmpty()) || (bccList != null && !bccList.isEmpty())) {
					mailIcerik += "<P>" + PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyy MMMMMM") + " ayı" + (aciklama != null ? " " + PdksUtil.replaceAll(aciklama, veriAyrac, " ") : "") + " çalışma planı ekteki dosyadır.</p><br/><br/>";
					puantajList = yoneticiPuantajMap.get(yoneticiPersonelId);
					for (AylikPuantaj aylikPuantaj : puantajList) {
						aylikPuantaj.setOnayDurum(!eskiOnayList.contains(aylikPuantaj.getPdksPersonel().getId()));
						aylikPuantaj.setSecili(Boolean.TRUE);
					}

					mailData = null;
					if (PdksUtil.hasStringValue(calismaPlanOnayMailAdres)) {
						String gorevYeriAciklama = getExcelAciklama();
						baosDosya = aylikVardiyaExcelDevam(gorevYeriAciklama, puantajList, Boolean.FALSE);
						mailData = baosDosya.toByteArray();
					}
					try {
						if (mailData != null) {
							MailObject mail = new MailObject();
							mail.setSubject(mailKonu);
							mail.setBody(mailIcerik);
							ortakIslemler.addMailPersonelUserList(toList, mail.getToList());
							ortakIslemler.addMailPersonelUserList(ccList, mail.getCcList());
							ortakIslemler.addMailPersonelUserList(bccList, mail.getBccList());
							MailFile mf = new MailFile();
							mf.setDisplayName(dosyaAdi);
							mf.setIcerik(mailData);
							mail.getAttachmentFiles().add(mf);
							ortakIslemler.mailSoapServisGonder(true, mail, renderer, "/email/" + calismaPlanOnayMailAdres, session);
						}

					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());
						logger.error("hata /email/" + calismaPlanOnayMailAdres + " " + e.getMessage());
						PdksUtil.addMessageError("Mesaj gönderilmemiştir. " + e.getMessage());

					}
					for (AylikPuantaj aylikPuantaj : puantajList)
						aylikPuantaj.setSecili(Boolean.FALSE);
				}
			}

			for (AylikPuantaj aylikPuantaj : puantajList) {
				aylikPuantaj.setOnayDurum(false);
			}
			session.flush();
			if (onayDurum) {
				aylikPuantajOlusturuluyor();
				puantajList = aylikPuantajList;
				suaKontrol(puantajList);
				PdksUtil.addMessageAvailableWarn(PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyy MMMMMM  ") + " ayı çalışma onaylandı.");
			}

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
	 * 
	 */
	@Transactional
	protected void talepGirisCikisHareketEkle() {
		if (islemFazlaMesaiTalep != null && islemFazlaMesaiTalep.getId() != null) {
			Vardiya islemVardiya = seciliVardiyaGun != null ? seciliVardiyaGun.getIslemVardiya() : null;
			PersonelKGS personelKGS = null;
			try {
				Personel personel = islemFazlaMesaiTalep.getVardiyaGun().getPersonel(), girisYapan = islemFazlaMesaiTalep.getOlusturanUser() == null ? authenticatedUser.getPdksPersonel() : islemFazlaMesaiTalep.getOlusturanUser().getPdksPersonel();
				personelKGS = personel.getPersonelKGS();
				if (personel.getId().equals(girisYapan.getId())) {
					if (manuelGiris == null) {
						fillKapiPDKSList();
						if (manuelGiris == null || manuelCikis == null) {
							if (manuelGiris == null)
								manuelGiris = new KapiView();
							if (manuelCikis == null)
								manuelCikis = new KapiView();
						}
					}
					if (manuelGiris.getId() != null && manuelGiris.getId() != null) {
						Long personelKGSId = personelKGS.getId();
						Calendar cal = Calendar.getInstance();
						Long nedenId = islemFazlaMesaiTalep.getMesaiNeden() != null ? islemFazlaMesaiTalep.getMesaiNeden().getId() : null;
						Tanim fazlaMesaiSistemOnayDurum = ortakIslemler.getOtomatikKapGirisiNeden(session);
						if (fazlaMesaiSistemOnayDurum != null)
							nedenId = fazlaMesaiSistemOnayDurum.getId();
						Date tarih1 = islemFazlaMesaiTalep.getBaslangicZamani(), tarih2 = islemFazlaMesaiTalep.getBitisZamani();
						String referans = "TRef:" + islemFazlaMesaiTalep.getId();
						HashMap fields = new HashMap();
						fields.put("islem.islemTipi<>", "D");
						fields.put("personel.id=", personelKGSId);
						fields.put("zaman>=", ortakIslemler.tariheGunEkleCikar(cal, tarih1, -1));
						fields.put("zaman<=", ortakIslemler.tariheGunEkleCikar(cal, tarih2, 1));
						fields.put("islem.aciklama like ", "%" + referans + "%");
						if (session != null)
							fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						List<PersonelHareket> hareketList = pdksEntityController.getObjectByInnerObjectListInLogic(fields, PersonelHareket.class);
						PersonelHareket girisHareket = null, cikisHareket = null;
						if (islemFazlaMesaiTalep.getOnayDurumu() != FazlaMesaiTalep.ONAY_DURUM_RED && islemFazlaMesaiTalep.getDurum()) {
							boolean flush = false;
							for (PersonelHareket personelHareket : hareketList) {
								if (personelHareket.getKapiView().getKapi().isGirisKapi())
									girisHareket = personelHareket;
								else if (personelHareket.getKapiView().getKapi().isCikisKapi())
									cikisHareket = personelHareket;

							}
							Date basTarih = null, bitTarih = null;
							if (seciliVardiyaGun != null && seciliVardiyaGun.getIslemVardiya() != null) {
								basTarih = islemVardiya.getVardiyaFazlaMesaiBasZaman();
								bitTarih = islemVardiya.getVardiyaFazlaMesaiBitZaman();
							}
							if (hareketPdksList == null) {
								if (basTarih == null)
									basTarih = PdksUtil.addTarih(tarih1, Calendar.MINUTE, -15);
								if (bitTarih == null)
									bitTarih = PdksUtil.addTarih(tarih2, Calendar.MINUTE, 15);

								hareketPdksList = getPdksHareketler(personelKGSId, basTarih, bitTarih);
							}
							int doluGirisAdet = 0, doluCikisAdet = 0;
							Integer ardisikAdet = null;
							if (!hareketPdksList.isEmpty()) {
								List<HareketKGS> hareketKGSList = new ArrayList<HareketKGS>(hareketPdksList);
								List<HareketKGS> hareketKGSDeleteList = new ArrayList<HareketKGS>();
								if (islemVardiya != null) {
									seciliVardiyaGun.setHareketler(null);
									seciliVardiyaGun.setCikisHareketleri(null);
									seciliVardiyaGun.setGirisHareketleri(null);
									HareketKGS girisYeniHareket = new HareketKGS();
									girisYeniHareket.setKapiView(manuelGiris);
									girisYeniHareket.setOrjinalZaman(tarih1);
									girisYeniHareket.setZaman(tarih1);
									hareketKGSList.add(girisYeniHareket);
									HareketKGS cikisYeniHareket = new HareketKGS();
									cikisYeniHareket.setKapiView(manuelCikis);
									cikisYeniHareket.setOrjinalZaman(tarih2);
									cikisYeniHareket.setZaman(tarih2);
									hareketKGSList.add(cikisYeniHareket);
									hareketKGSList = PdksUtil.sortListByAlanAdi(hareketKGSList, "zaman", Boolean.FALSE);
									HareketKGS hareketOncekiKGS = null, hareketSonrakiKGS = null;
									Date bas1 = islemVardiya.getVardiyaTelorans1BasZaman(), bas2 = islemVardiya.getVardiyaTelorans2BasZaman();
									Date bit1 = islemVardiya.getVardiyaTelorans1BitZaman(), bit2 = islemVardiya.getVardiyaTelorans2BitZaman();
									int kayitAdet = hareketKGSList.size();
									for (int i = 0; i < kayitAdet; i++) {
										HareketKGS hareketKGS = hareketKGSList.get(i);
										Date zaman = hareketKGS.getOrjinalZaman(), zamanOnceki = hareketOncekiKGS != null ? hareketOncekiKGS.getOrjinalZaman() : null;
										Kapi kapi = hareketKGS.getKapiView().getKapi();
										if (hareketKGS.getId() == null) {
											hareketSonrakiKGS = null;
											if (kapi.isGirisKapi()) {
												doluGirisAdet = 0;
												ardisikAdet = 0;
												for (int j = i + 1; j < kayitAdet; j++) {
													HareketKGS hareketSonraki = hareketKGSList.get(j);
													Kapi kapiSonraki = hareketSonraki.getKapiView().getKapi();
													boolean sil = false;
													if (hareketSonraki.getId() != null) {
														++doluGirisAdet;
														Date zamanSonraki = hareketSonraki.getOrjinalZaman();
														if (bas1.getTime() <= zaman.getTime() && bas2.getTime() >= zamanSonraki.getTime()) {
															girisHareket = new PersonelHareket();
															hareketKGSDeleteList.add(hareketSonraki);
														} else if (bit1.getTime() <= zaman.getTime() && bit2.getTime() >= zamanSonraki.getTime()) {
															sil = kapiSonraki.isCikisKapi();
															if (sil) {
																girisHareket = new PersonelHareket();
															}
															hareketKGSDeleteList.add(hareketSonraki);

														}

													} else {
														// if (i == 0)
														// i = kayitAdet;
														break;
													}

												}
												if (hareketKGSDeleteList.isEmpty() && hareketOncekiKGS != null && doluGirisAdet < 2) {
													HareketKGS hareketOnceki = hareketOncekiKGS;
													if (bas2.getTime() >= zaman.getTime() && bas1.getTime() >= zamanOnceki.getTime()) {
														girisHareket = new PersonelHareket();
														// hareketKGS = hareketOnceki;
													} else if (bit2.getTime() >= zaman.getTime() && bit1.getTime() <= zamanOnceki.getTime()) {
														girisHareket = new PersonelHareket();
														hareketKGSDeleteList.add(hareketOnceki);
													} else if (bas2.getTime() >= zamanOnceki.getTime() && bas1.getTime() <= zamanOnceki.getTime()) {
														if (bit2.getTime() >= zaman.getTime() && bit1.getTime() <= zaman.getTime())
															girisHareket = new PersonelHareket();
													}
													if (girisHareket != null)
														break;
												}

											} else if (kapi.isCikisKapi()) {
												doluCikisAdet = 0;
												for (int j = i + 1; j < kayitAdet; j++) {
													HareketKGS hareketSonraki = hareketKGSList.get(j);
													if (hareketSonraki.getId() != null) {
														++doluCikisAdet;
														Kapi kapiSonraki = hareketSonraki.getKapiView().getKapi();
														hareketSonrakiKGS = hareketSonraki;
														Date zamanSonraki = hareketSonraki.getOrjinalZaman();
														if (bas1.getTime() <= zaman.getTime() && bas2.getTime() >= zamanSonraki.getTime()) {
															if (kapiSonraki.isGirisKapi() && doluCikisAdet == 1) {
																cikisHareket = new PersonelHareket();
																hareketKGSDeleteList.add(hareketSonraki);
															}

														} else if (bit1.getTime() <= zaman.getTime() && bit2.getTime() >= zamanSonraki.getTime()) {
															cikisHareket = new PersonelHareket();
															hareketKGSDeleteList.add(hareketSonraki);
														}
													}
												}
												if (doluCikisAdet > 0) {
													if (hareketOncekiKGS != null) {
														if (bas2.getTime() >= zaman.getTime() && bas1.getTime() <= zamanOnceki.getTime()) {
															cikisHareket = new PersonelHareket();
															hareketKGSDeleteList.add(hareketOncekiKGS);
														} else if (bit2.getTime() >= zaman.getTime() && bit1.getTime() <= zamanOnceki.getTime()) {
															cikisHareket = new PersonelHareket();
															hareketKGSDeleteList.add(hareketOncekiKGS);
														} else if (bas2.getTime() >= zamanOnceki.getTime() && bas1.getTime() <= zamanOnceki.getTime()) {
															if (bit2.getTime() >= zaman.getTime() && bit1.getTime() <= zaman.getTime())
																cikisHareket = new PersonelHareket();
														}
													} else if (hareketSonrakiKGS != null && bas2.getTime() >= zaman.getTime() && bas1.getTime() <= zaman.getTime()) {
														Date zamanSonraki = hareketSonrakiKGS.getOrjinalZaman();
														if (bit2.getTime() >= zamanSonraki.getTime() && bit1.getTime() <= zamanSonraki.getTime())
															cikisHareket = new PersonelHareket();
													}
												}
												break;
											}
										} else {
											if (ardisikAdet != null)
												++ardisikAdet;
											hareketOncekiKGS = hareketKGS;
										}

									}
								}
								if (!hareketKGSDeleteList.isEmpty() && (girisHareket == null || cikisHareket == null)) {
									for (HareketKGS hareketKGS3 : hareketKGSDeleteList) {

										long kgsId = 0, pdksId = 0;
										String str = hareketKGS3.getId();
										Long id = Long.parseLong(str.substring(1));
										if (str.startsWith(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_KGS))
											kgsId = id;
										else
											pdksId = id;
										if (kgsId + pdksId > 0) {
											User sistemUser = ortakIslemler.getSistemAdminUser(session);
											if (sistemUser == null)
												sistemUser = authenticatedUser;
											String aciklama = PdksUtil.hasStringValue(islemFazlaMesaiTalep.getAciklama()) ? islemFazlaMesaiTalep.getAciklama().trim() : "";
											pdksEntityController.hareketSil(kgsId, pdksId, sistemUser, nedenId, aciklama + (referans != null ? " " + referans.trim() : ""), hareketKGS3.getKgsSirketId(), session);
											flush = true;
										}
									}

								}
							}

							String aciklama = referans + " " + (PdksUtil.hasStringValue(islemFazlaMesaiTalep.getAciklama()) ? islemFazlaMesaiTalep.getAciklama().trim() : "") + " hareket";
							User guncelleyen = ortakIslemler.getSistemAdminUser(session);
							if (!authenticatedUser.isAdmin() && userHome.hasPermission("personelHareket", "view"))
								guncelleyen = authenticatedUser;
							if (girisHareket == null && doluGirisAdet < 2) {
								if (nedenId != null) {
									hareketEkleReturn(guncelleyen, manuelGiris, personelKGS, tarih1, nedenId, aciklama + " giriş");
									flush = true;
								}

							}
							if (cikisHareket == null && (ardisikAdet == null || ardisikAdet < 2)) {
								if (nedenId != null) {
									hareketEkleReturn(guncelleyen, manuelCikis, personelKGS, tarih2, nedenId, aciklama + " çıkış");
									flush = true;
								}

							}
							if (flush)
								session.flush();
						} else if (!hareketList.isEmpty()) {
							Long sonuc = fazlaMesaiOrtakIslemler.fazlaMesaiOtomatikHareketSil(islemFazlaMesaiTalep.getId(), session);
							if (sonuc != null && sonuc.equals(islemFazlaMesaiTalep.getId()))
								session.flush();
						}

					}
				}
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
			hareketPdksList = null;
			if (aylikPuantajMesaiTalepList == null && islemVardiya != null && personelKGS != null)
				hareketPdksList = getPdksHareketler(personelKGS.getId(), islemVardiya.getVardiyaFazlaMesaiBasZaman(), islemVardiya.getVardiyaFazlaMesaiBitZaman());

		}
	}

	/**
	 * @param personelKGSId
	 * @param tarih1
	 * @param tarih2
	 * @return
	 */
	private List<HareketKGS> getPdksHareketler(Long personelKGSId, Date tarih1, Date tarih2) {
		if (sessionHareket == null)
			sessionHareket = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		else
			sessionHareket.clear();
		List<Long> kgsPerIdler = new ArrayList<Long>();
		kgsPerIdler.add(personelKGSId);
		HashMap<Long, ArrayList<HareketKGS>> personelHareketMap = ortakIslemler.fillPersonelKGSHareketMap(kgsPerIdler, tarih1, tarih2, sessionHareket);
		List<HareketKGS> hareketPdksList = personelHareketMap.containsKey(personelKGSId) ? personelHareketMap.get(personelKGSId) : new ArrayList<HareketKGS>();
		for (Iterator iterator = hareketPdksList.iterator(); iterator.hasNext();) {
			HareketKGS hareketKGS = (HareketKGS) iterator.next();
			try {
				KapiKGS kapiKGS = hareketKGS.getKapiKGS();
				if (kapiKGS.getKapi() == null || !kapiKGS.getKapi().getPdks())
					iterator.remove();
			} catch (Exception e) {
			}
		}
		personelHareketMap = null;
		kgsPerIdler = null;
		return hareketPdksList;
	}

	/**
	 * 
	 */
	private void fillKapiPDKSList() {
		HashMap parametreMap = new HashMap();
		parametreMap.put("durum", Boolean.TRUE);
		parametreMap.put("kapi.durum", Boolean.TRUE);
		parametreMap.put("kapi.pdks", Boolean.TRUE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<KapiKGS> kapiKGSList = pdksEntityController.getObjectByInnerObjectList(parametreMap, KapiKGS.class);
		List<KapiView> list = new ArrayList<KapiView>();
		for (KapiKGS kapiKGS : kapiKGSList)
			list.add(kapiKGS.getKapiView());
		HashMap<String, KapiView> manuelKapiMap = ortakIslemler.getManuelKapiMap(list, session);
		manuelGiris = manuelKapiMap.get(Kapi.TIPI_KODU_GIRIS);
		manuelCikis = manuelKapiMap.get(Kapi.TIPI_KODU_CIKIS);
		manuelKapiMap = null;
	}

	/**
	 * @param guncelleyen
	 * @param kapi
	 * @param personelKGS
	 * @param zaman
	 * @param nedenId
	 * @param aciklama
	 * @return
	 */
	private Long hareketEkleReturn(User guncelleyen, KapiView kapi, PersonelKGS personelKGS, Date zaman, Long nedenId, String aciklama) {
		Long id = null;
		LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
		try {
			StringBuffer sp = new StringBuffer("SP_HAREKET_EKLE_RETURN");
			veriMap.put("kapi", kapi.getId());
			veriMap.put("personelKGS", personelKGS.getId());
			veriMap.put("zaman", zaman);
			veriMap.put("guncelleyen", authenticatedUser.getId());
			veriMap.put("nedenId", nedenId);
			veriMap.put("aciklama", aciklama);
			if (session != null)
				veriMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List list = pdksEntityController.execSPList(veriMap, sp, null);
			if (list != null && !list.isEmpty())
				id = ((BigDecimal) list.get(0)).longValue();
			list = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		veriMap = null;
		return id;

	}

	/**
	 * @param session
	 * @param aylikPuantaj
	 * @return
	 */

	private Boolean saveOnay(Session session, AylikPuantaj aylikPuantaj) {
		Personel personel = aylikPuantaj.getPdksPersonel();
		PersonelDenklestirme personelDenklestirme = aylikPuantaj.getPersonelDenklestirmeAylik();
		if (personelDenklestirme.isOnaylandi() == false) {
			personelDenklestirme.setOnaylandi(Boolean.TRUE);
			if (personelDenklestirme.getSutIzniDurum() == null || personelDenklestirme.getSutIzniDurum().equals(Boolean.FALSE))
				personelDenklestirme.setSutIzniDurum(personel.getSutIzni() != null && personel.getSutIzni());
			if (!personelDenklestirme.isKapandi())
				personelDenklestirme.setDevredenSure(null);
			personelDenklestirme.setGuncellemeTarihi(new Date());
			pdksEntityController.saveOrUpdate(session, entityManager, personelDenklestirme);
		}
		aylikPuantaj.setOnayDurum(Boolean.FALSE);
		Boolean durum = Boolean.TRUE;
		return durum;
	}

	/**
	 * @return
	 */
	public String bosIslem() {
		return "";
	}

	/**
	 * @return
	 */
	public String mesaiTopluKaydet() {
		List<String> mesajlar = new ArrayList<String>();
		HashMap<Long, AylikPuantaj> puantajMap = new HashMap<Long, AylikPuantaj>();
		TreeMap<Long, User> userMap = new TreeMap<Long, User>();
		Tanim mesaiNeden = ortakIslemler.getTanimById(mesaiNedenId, session);
		fazlaMesaiTalep.setMesaiNeden(mesaiNeden);
		if (fazlaMesaiTalep.getMesaiNeden() == null) {
			mesajlar.add("Mesai nedeni seçiniz!");

		} else {
			Calendar cal = Calendar.getInstance();
			cal.setTime(fazlaMesaiTalep.getBaslangicZamani());
			cal.set(Calendar.HOUR_OF_DAY, fazlaMesaiTalep.getBasSaat());
			cal.set(Calendar.MINUTE, fazlaMesaiTalep.getBasDakika());
			Date baslangicZamani = cal.getTime();
			cal.setTime(fazlaMesaiTalep.getBitisZamani());
			cal.set(Calendar.HOUR_OF_DAY, fazlaMesaiTalep.getBitSaat());
			cal.set(Calendar.MINUTE, fazlaMesaiTalep.getBitDakika());
			Date bitisZamani = cal.getTime();
			fazlaMesaiTalep.setBaslangicZamani(baslangicZamani);
			fazlaMesaiTalep.setBitisZamani(bitisZamani);
			HashMap<Long, VardiyaGun> devamMap = new HashMap<Long, VardiyaGun>();
			List<Long> perIdList = new ArrayList<Long>();
			for (AylikPuantaj aylikPuantaj : aylikPuantajMesaiTalepList) {
				VardiyaGun vardiyaGunPer = aylikPuantaj.getVardiyaGun();
				if (aylikPuantaj.getYonetici2() != null && !perIdList.contains(aylikPuantaj.getYonetici2().getId()))
					perIdList.add(aylikPuantaj.getYonetici2().getId());
				puantajMap.put(vardiyaGunPer.getId(), aylikPuantaj);
			}

			if (!perIdList.isEmpty()) {
				HashMap map = new HashMap();
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT U.* FROM " + Personel.TABLE_NAME + " P WITH(nolock) ");
				sb.append(" INNER JOIN " + User.TABLE_NAME + " U ON U." + User.COLUMN_NAME_PERSONEL + "=P." + Personel.COLUMN_NAME_ID + " AND U." + User.COLUMN_NAME_DURUM + "=1 ");
				sb.append(" WHERE P." + Personel.COLUMN_NAME_ID + " :id  ");
				map.put("id", perIdList);
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<User> userList = pdksEntityController.getObjectBySQLList(sb, map, User.class);
				for (User user : userList) {
					userMap.put(user.getPersonelId(), user);
				}
			}
			for (Iterator iterator = aylikPuantajMesaiTalepList.iterator(); iterator.hasNext();) {
				AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
				VardiyaGun vardiyaGunPer = aylikPuantaj.getVardiyaGun();
				String anaMesaj = aylikPuantaj.getPdksPersonel().getPdksSicilNo() + " " + aylikPuantaj.getPdksPersonel().getAdSoyad();
				Vardiya islemVardiya = vardiyaGunPer.getIslemVardiya();
				VardiyaGun sonrakiVardiyaGun = vardiyaGunPer.getSonrakiVardiyaGun(), oncekiVardiyaGun = vardiyaGunPer.getOncekiVardiyaGun();
				Vardiya sonrakiVardiya = null, oncekiVardiya = null;
				if (oncekiVardiyaGun != null) {
					oncekiVardiyaGun.setIslemVardiya(null);
					oncekiVardiyaGun.setVardiyaZamani();
					oncekiVardiya = oncekiVardiyaGun.getIslemVardiya();
				}
				if (sonrakiVardiyaGun != null) {
					sonrakiVardiyaGun.setIslemVardiya(null);
					sonrakiVardiyaGun.setVardiyaZamani();
					sonrakiVardiya = sonrakiVardiyaGun.getIslemVardiya();
				}
				boolean devam = true;
				if (islemVardiya.isCalisma()) {
					boolean disVardiSinirda = (oncekiVardiya == null || oncekiVardiya.getVardiyaBitZaman().getTime() < fazlaMesaiTalep.getBaslangicZamani().getTime()) && (sonrakiVardiya == null || sonrakiVardiya.getVardiyaBasZaman().getTime() > fazlaMesaiTalep.getBitisZamani().getTime());
					boolean icVardiSinirda = fazlaMesaiTalep.getBitisZamani().getTime() <= islemVardiya.getVardiyaBasZaman().getTime() || fazlaMesaiTalep.getBaslangicZamani().getTime() >= islemVardiya.getVardiyaBitZaman().getTime();
					if (!disVardiSinirda) {
						mesajlar.add(anaMesaj + " mesai başlangıç ve bitiş zamanı hatalıdır");
						devam = false;
					} else if (!icVardiSinirda) {
						mesajlar.add(anaMesaj + " mesai başlangıç ve bitiş zamanı hatalıdır");
						devam = false;
					}
				} else if (PdksUtil.tarihKarsilastirNumeric(fazlaMesaiTalep.getBaslangicZamani(), seciliVardiyaGun.getVardiyaDate()) != 0 || PdksUtil.tarihKarsilastirNumeric(fazlaMesaiTalep.getBitisZamani(), seciliVardiyaGun.getVardiyaDate()) != 0) {
					mesajlar.add(anaMesaj + " mesai başlangıç ve bitiş zamanı hatalıdır");
					devam = false;
				}
				if (devam)
					devamMap.put(vardiyaGunPer.getId(), vardiyaGunPer);
				else
					puantajMap.remove(vardiyaGunPer.getId());
			}
			if (!devamMap.isEmpty()) {
				HashMap map = new HashMap();
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT F.*," + FazlaMesaiTalep.COLUMN_NAME_ONAY_DURUMU + " FROM " + VardiyaGun.TABLE_NAME + " V WITH(nolock) ");
				sb.append(" INNER JOIN " + FazlaMesaiTalep.TABLE_NAME + " F ON F." + FazlaMesaiTalep.COLUMN_NAME_VARDIYA_GUN + "=V." + VardiyaGun.COLUMN_NAME_ID + " AND F." + FazlaMesaiTalep.COLUMN_NAME_DURUM + "=1 ");
				sb.append(" AND F." + FazlaMesaiTalep.COLUMN_NAME_BASLANGIC_ZAMANI + "<=:t2 AND F." + FazlaMesaiTalep.COLUMN_NAME_BITIS_ZAMANI + ">=:t1 ");
				sb.append(" WHERE F." + FazlaMesaiTalep.COLUMN_NAME_VARDIYA_GUN + " :id  ");
				map.put("id", new ArrayList(devamMap.keySet()));
				map.put("t1", fazlaMesaiTalep.getBaslangicZamani());
				map.put("t2", fazlaMesaiTalep.getBitisZamani());
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<FazlaMesaiTalep> mesaiList = pdksEntityController.getObjectBySQLList(sb, map, FazlaMesaiTalep.class);
				String patternTarih = PdksUtil.getDateFormat(), saatPattern = PdksUtil.getSaatFormat();
				for (FazlaMesaiTalep fazlaMesaiTalep : mesaiList) {
					Personel personel = fazlaMesaiTalep.getVardiyaGun().getPersonel();
					boolean esit = PdksUtil.tarihKarsilastirNumeric(fazlaMesaiTalep.getBaslangicZamani(), fazlaMesaiTalep.getBitisZamani()) == 0;
					String pattern = (esit ? "" : patternTarih + " ") + saatPattern;
					String anaMesaj = personel.getPdksSicilNo() + " " + personel.getAdSoyad() + " için oluşturulan " + PdksUtil.convertToDateString(fazlaMesaiTalep.getBaslangicZamani(), patternTarih + " " + saatPattern) + " - "
							+ PdksUtil.convertToDateString(fazlaMesaiTalep.getBitisZamani(), pattern);
					switch (fazlaMesaiTalep.getOnayDurumu()) {
					case FazlaMesaiTalep.ONAY_DURUM_ISLEM_YAPILMADI:
						PdksUtil.addMessageAvailableInfo(anaMesaj + " ait fazla mesai talep onayda beklemektedir.");
						break;
					case FazlaMesaiTalep.ONAY_DURUM_ONAYLANDI:
						mesajlar.add(anaMesaj + " ait fazla mesai talep onaylandı!");
						break;
					case FazlaMesaiTalep.ONAY_DURUM_RED:
						mesajlar.add(anaMesaj + " ait fazla mesai talep onaylanmadı!");
						break;
					default:
						break;
					}
					puantajMap.remove(fazlaMesaiTalep.getVardiyaGun().getId());
					devamMap.remove(fazlaMesaiTalep.getVardiyaGun().getId());
				}
			}
		}
		if (!puantajMap.isEmpty()) {
			fazlaMesaiTalep.setOlusturanUser(authenticatedUser);
			fazlaMesaiTalep.setOlusturmaTarihi(new Date());
			FazlaMesaiTalep asil = (FazlaMesaiTalep) fazlaMesaiTalep.clone();
			VardiyaGun vardiyaGunAsil = (VardiyaGun) seciliVardiyaGun.clone();
			for (Iterator iterator = aylikPuantajMesaiTalepList.iterator(); iterator.hasNext();) {
				AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
				VardiyaGun vardiyaGunPer = aylikPuantaj.getVardiyaGun();
				if (puantajMap.containsKey(vardiyaGunPer.getId())) {
					fazlaMesaiTalep = (FazlaMesaiTalep) asil.clone();
					fazlaMesaiTalep.setGuncelleyenUser(userMap.get(aylikPuantaj.getYonetici2().getId()));
					fazlaMesaiTalep.setVardiyaGun(vardiyaGunPer);
					seciliVardiyaGun = vardiyaGunPer;
					mesaiKaydet(Boolean.TRUE);
					if (fazlaMesaiTalep == null)
						iterator.remove();
				}
			}
			fazlaMesaiTalep = null;
			seciliVardiyaGun = (VardiyaGun) vardiyaGunAsil.clone();
			if (!aylikPuantajMesaiTalepList.isEmpty())
				fazlaMesaiTalep = asil;
		}

		for (String mesaj : mesajlar)
			PdksUtil.addMessageAvailableWarn(mesaj);

		return "";
	}

	/**
	 * @param vgSec
	 * @return
	 */
	public String topluTalepOlusturmaKontrol(VardiyaGun vgSec) {
		seciliVardiyaGun = vgSec;
		if (aylikPuantajMesaiTalepList == null)
			aylikPuantajMesaiTalepList = new ArrayList<AylikPuantaj>();
		else
			aylikPuantajMesaiTalepList.clear();
		seciliVardiyaGun.setIslemVardiya(null);
		manuelHareketEkle = null;
		mesaiNedenId = null;
		hareketPdksList = null;
		boolean secili = false;
		List<String> mesajlar = new ArrayList<String>();
		int index = -1;
		List<Long> perIdList = new ArrayList<Long>();
		TreeMap<String, VardiyaGun> vm = new TreeMap<String, VardiyaGun>();
		List<Long> vardiyaIdList = new ArrayList<Long>();
		Personel girisYapan = authenticatedUser.getPdksPersonel(), personel = null;
		manuelHareketEkle = null;
		hareketPdksList = null;
		TreeMap<Long, Liste> seciliMap = new TreeMap<Long, Liste>();
		try {
			for (Iterator iterator = aylikPuantajList.iterator(); iterator.hasNext();) {
				AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
				aylikPuantaj.setVardiyaGun(null);
				if (aylikPuantaj.isOnayDurum()) {
					if (index < 0) {
						for (VardiyaGun vg : aylikPuantaj.getVardiyalar()) {
							++index;
							if (vg.getVardiyaDate().getTime() == seciliVardiyaGun.getVardiyaDate().getTime()) {
								break;
							}
						}
					}
					VardiyaGun vardiyaGunPer = aylikPuantaj.getVardiyalar().get(index);
					Vardiya vardiya = vardiyaGunPer.getVardiya();
					Long id = vardiya != null ? vardiya.getId() : null;
					if (id != null) {
						Liste liste = seciliMap.containsKey(id) ? seciliMap.get(id) : new Liste(id, new ArrayList<AylikPuantaj>());
						List<AylikPuantaj> list = (List<AylikPuantaj>) liste.getValue();
						if (list.isEmpty())
							seciliMap.put(id, liste);
						list.add(aylikPuantaj);
						liste.setNumValue(list.size());
					} else
						aylikPuantaj.setOnayDurum(false);
				}
			}
			if (seciliMap.size() > 1) {
				List<Liste> list = PdksUtil.sortListByAlanAdi(new ArrayList<Liste>(seciliMap.values()), "numValue", true);
				Liste listeAna = list.get(0);
				List<AylikPuantaj> puantajList = (List<AylikPuantaj>) listeAna.getValue();
				VardiyaGun vardiyaGunPer = puantajList.get(0).getVardiyalar().get(index);
				Vardiya vardiya = vardiyaGunPer.getVardiya();
				PdksUtil.addMessageAvailableInfo("Birden fazla seçemezsiniz, " + vardiya.getKisaAdi() + (vardiya.isCalisma() ? " " + vardiyaGunPer.getVardiya().getAdi() : "") + " haricindeki vardiyalar işleme alınmadı!");
				for (int i = 1; i < list.size(); i++) {
					Liste liste = list.get(i);
					puantajList = (List<AylikPuantaj>) liste.getValue();
					for (AylikPuantaj aylikPuantaj : puantajList) {
						aylikPuantaj.setOnayDurum(false);
					}
					puantajList = null;
					liste = null;

				}
				list = null;
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		seciliMap = null;
		for (Iterator iterator = aylikPuantajList.iterator(); iterator.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
			aylikPuantaj.setVardiyaGun(null);
			if (aylikPuantaj.isOnayDurum()) {
				personel = aylikPuantaj.getPdksPersonel();
				secili = true;
				if (aylikPuantaj.getYonetici2() != null && aylikPuantaj.getPersonelDenklestirmeAylik().isOnaylandi()) {
					if (!perIdList.contains(aylikPuantaj.getYonetici2().getId()))
						perIdList.add(aylikPuantaj.getYonetici2().getId());
					if (index < 0) {
						for (VardiyaGun vg : aylikPuantaj.getVardiyalar()) {
							++index;
							if (vg.getVardiyaDate().getTime() == seciliVardiyaGun.getVardiyaDate().getTime()) {
								break;
							}
						}
					}
					VardiyaGun vardiyaGunPer = aylikPuantaj.getVardiyalar().get(index);
					if (personel.getId().equals(girisYapan.getId())) {
						manuelHareketEkle = Boolean.FALSE;
						if (vardiyaGunPer != null) {
							Vardiya islemVardiya = vardiyaGunPer.getIslemVardiya();
							if (islemVardiya != null) {
								seciliVardiyaGun.setIslemVardiya(islemVardiya);
								Date basTarih = islemVardiya.getVardiyaFazlaMesaiBasZaman();
								Date bitTarih = islemVardiya.getVardiyaFazlaMesaiBitZaman();
								hareketPdksList = getPdksHareketler(girisYapan.getPersonelKGS().getId(), basTarih, bitTarih);
								manuelHareketEkle = hareketPdksList.isEmpty();
							}
						}
					}
					if (vardiyaGunPer.getId() != null && vardiyaGunPer.getVardiya() != null && vardiyaGunPer.getVardiya().getId() != null) {
						if (vardiyaGunPer.getIzin() == null) {
							aylikPuantaj.setVardiyaGun(vardiyaGunPer);
							aylikPuantajMesaiTalepList.add(aylikPuantaj);
							personel = aylikPuantaj.getPdksPersonel();
							for (VardiyaGun vg : aylikPuantaj.getVardiyalar()) {
								setVardiyalarToMap(vg, vm, aylikPuantaj);
							}
							if (!vardiyaIdList.contains(vardiyaGunPer.getVardiya().getId())) {
								seciliVardiyaGun = (VardiyaGun) vardiyaGunPer.clone();
								seciliVardiyaGun.setId(null);
								seciliVardiyaGun.setPersonel(null);
								vardiyaIdList.add(vardiyaGunPer.getVardiya().getId());
							}
						} else {
							aylikPuantaj.setOnayDurum(Boolean.FALSE);
							PdksUtil.addMessageAvailableWarn(aylikPuantaj.getPdksPersonel().getPdksSicilNo() + " " + aylikPuantaj.getPdksPersonel().getAdSoyad() + " izinlidir!");
						}

					} else {
						aylikPuantaj.setOnayDurum(Boolean.FALSE);
						PdksUtil.addMessageAvailableWarn(aylikPuantaj.getPdksPersonel().getPdksSicilNo() + " " + aylikPuantaj.getPdksPersonel().getAdSoyad() + " çalışmamaktadır!");
					}
				} else {
					String anaMesaj = aylikPuantaj.getPdksPersonel().getPdksSicilNo() + " " + aylikPuantaj.getPdksPersonel().getAdSoyad(), mesaj = null;
					if (aylikPuantaj.getYonetici2() == null)
						mesaj = anaMesaj + " 2. yönetici tanımsız";
					if (aylikPuantaj.getPersonelDenklestirmeAylik().isOnaylandi() == false) {
						if (mesaj == null)
							mesaj = anaMesaj;
						else
							mesaj += " ve ";
						mesaj += " çalışma planı onaylanmamış!";
					} else
						mesaj += "!";
					mesajlar.add(mesaj);
				}
			}

		}
		if (!secili)
			mesajlar.add("Seçili kayıt yok!");
		else if (vardiyaIdList.size() > 1)
			mesajlar.add("Seçili günde tek tip vardiya olmalıdır!");
		else if (!perIdList.isEmpty()) {
			if (!vm.isEmpty())
				ortakIslemler.fazlaMesaiSaatiAyarla(vm);
			HashMap map = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT P.* FROM " + Personel.TABLE_NAME + " P WITH(nolock) ");
			sb.append(" LEFT JOIN " + User.TABLE_NAME + " U ON U." + User.COLUMN_NAME_PERSONEL + "=P." + Personel.COLUMN_NAME_ID + " AND U." + User.COLUMN_NAME_DURUM + "=1 ");
			sb.append(" WHERE P." + Personel.COLUMN_NAME_ID + " :id AND U." + User.COLUMN_NAME_ID + " IS NULL ");
			map.put("id", perIdList);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Personel> perList = pdksEntityController.getObjectBySQLList(sb, map, Personel.class);
			for (Personel yonetici : perList) {
				mesajlar.add(yonetici.getPdksSicilNo() + " " + yonetici.getAdSoyad() + " aktif kullanıcısı bulunmamaktadır!");
			}
		}
		if (!mesajlar.isEmpty()) {
			aylikPuantajMesaiTalepList.clear();
			for (String mesaj : mesajlar) {
				PdksUtil.addMessageAvailableWarn(mesaj);
			}
		} else {
			fazlaMesaiTalep = new FazlaMesaiTalep(seciliVardiyaGun);
			fazlaMesaiTalep.setBaslangicZamani(seciliVardiyaGun.getVardiyaDate());
			fazlaMesaiTalep.setBitisZamani(seciliVardiyaGun.getVardiyaDate());
			Vardiya vardiya = seciliVardiyaGun.getIslemVardiya();
			List<Long> personelIdler = new ArrayList<Long>();
			if (aylikPuantajMesaiTalepList != null)
				for (AylikPuantaj aylikPuantaj : aylikPuantajMesaiTalepList) {
					personelIdler.add(aylikPuantaj.getPdksPersonel().getId());
				}
			if (!personelIdler.isEmpty()) {
				List<VardiyaGun> vardiyaGunler = ortakIslemler.getAllPersonelIdVardiyalar(personelIdler, seciliVardiyaGun.getVardiyaDate(), seciliVardiyaGun.getVardiyaDate(), false, session);
				for (VardiyaGun vardiyaGun : vardiyaGunler) {
					seciliVardiyaGun.setYarimYuvarla(vardiyaGun.getYarimYuvarla());
					break;
				}
				vardiyaGunler = null;
			}
			personelIdler = null;
			if (seciliVardiyaGun.getVardiya().isCalisma()) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(vardiya.getVardiyaBitZaman());
				fazlaMesaiTalep.setBasSaat(cal.get(Calendar.HOUR_OF_DAY));
				fazlaMesaiTalep.setBasDakika(cal.get(Calendar.MINUTE));
				fazlaMesaiTalep.setBitSaat(cal.get(Calendar.HOUR_OF_DAY));
				fazlaMesaiTalep.setBitDakika(cal.get(Calendar.MINUTE));
			}

			mesaiNedenTanimList = ortakIslemler.getTanimSelectItem(ortakIslemler.getTanimList(Tanim.TIPI_FAZLA_MESAI_NEDEN, session));

		}

		return "";
	}

	/**
	 * @param seciliPer
	 * @return
	 */
	public String mesaiTalepListeSil(Personel seciliPer) {
		Long id = seciliPer != null && seciliPer.getId() != null ? seciliPer.getId() : 0L;
		Personel girisYapan = authenticatedUser.getPdksPersonel();
		if (girisYapan.getId().equals(id)) {
			manuelHareketEkle = null;
			hareketPdksList = null;
		}
		for (Iterator iterator = aylikPuantajMesaiTalepList.iterator(); iterator.hasNext();) {
			AylikPuantaj puantaj = (AylikPuantaj) iterator.next();
			if (puantaj.getPdksPersonel().getId().equals(id))
				iterator.remove();
		}
		return "";
	}

	/**
	 * 
	 */
	private void aylikHareketKaydiVardiyalariBul() {
		aylikHareketKaydiVardiyaBul = Boolean.FALSE;
		Date bugun = new Date();
		for (AylikPuantaj aylikPuantaj : aylikPuantajList) {
			CalismaModeliAy calismaModeliAy = aylikPuantaj.getPersonelDenklestirmeAylik().getCalismaModeliAy();
			if (calismaModeliAy != null && calismaModeliAy.isHareketKaydiVardiyaBulsunmu()) {
				for (VardiyaGun vg : aylikPuantaj.getVardiyalar()) {
					if (!vg.getDurum() && vg.isAyinGunu() && vg.getId() != null) {
						if (vg.getIslemVardiya() == null || bugun.before(vg.getIslemVardiya().getVardiyaTelorans2BitZaman()))
							continue;

						if (vg.getVersion() == 0)
							aylikHareketKaydiVardiyaBul = Boolean.TRUE;
					}
				}
			}
			if (aylikHareketKaydiVardiyaBul)
				break;
		}
	}

	@Transactional
	public String aylikHareketKaydiVardiyaBulGuncelle() {
		try {
			Date bugun = new Date();
			for (AylikPuantaj aylikPuantaj : aylikPuantajList) {
				CalismaModeliAy calismaModeliAy = aylikPuantaj.getPersonelDenklestirmeAylik().getCalismaModeliAy();
				if (calismaModeliAy != null && calismaModeliAy.isHareketKaydiVardiyaBulsunmu()) {
					boolean flush = false;
					for (VardiyaGun vg : aylikPuantaj.getVardiyalar()) {
						if (vg.getIslemVardiya() == null || bugun.before(vg.getIslemVardiya().getVardiyaTelorans2BitZaman()))
							continue;
						if (vg.isAyinGunu() && vg.getId() != null) {
							if (vg.getDurum() && vg.getVersion() < 0) {
								vg.setVersion(0);
								pdksEntityController.saveOrUpdate(session, entityManager, vg);
								flush = true;
							} else if (!vg.getDurum() && vg.getVersion() == 0 && vg.isIzinli() == false && vg.getVardiya().isHaftaTatil() == false) {
								vg.setVersion(-1);
								pdksEntityController.saveOrUpdate(session, entityManager, vg);
								flush = true;
							}
						}
					}
					if (flush)
						session.flush();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		aylikHareketKaydiVardiyalariBul();
		return "";
	}

	/**
	 * 
	 */
	@Transactional
	public Boolean aylikPuantajOlusturuluyor() {
		if (loginUser == null)
			loginUser = authenticatedUser;
		bordroPuantajEkranindaGoster = ortakIslemler.getParameterKey("bordroPuantajEkranindaGoster").equals("1");
		yoneticiERP1Kontrol = !ortakIslemler.getParameterKeyHasStringValue("yoneticiERP1Kontrol");
		bordroAlanKapat();
		Boolean kontrolDurum = false;
		String donem = String.valueOf(yil * 100 + ay);
		session.clear();
		vardiyalarMap.clear();
		vardiyaBolumList = null;
		aylikHareketKaydiVardiyaBul = Boolean.FALSE;
		savePlanLastParameter();
		fazlaMesaiIzinRaporuDurum = userHome != null && loginUser.getLogin() && userHome.hasPermission("fazlaMesaiIzinRaporu", "view");
		offIzinGuncelle = ortakIslemler.getParameterKey("offIzinGuncelle").equals("1");
		User user = ortakIslemler.getSistemAdminUser(session);
		if (user == null)
			user = loginUser;
		boolean testDurum = PdksUtil.getTestDurum() && PdksUtil.getCanliSunucuDurum() == false;
		testDurum = false;
		if (testDurum)
			logger.info("aylikPuantajOlusturuluyor 0000 " + PdksUtil.getCurrentTimeStampStr());
		manuelVardiyaIzinGir = ortakIslemler.getVardiyaIzinGir(session, loginUser.getDepartman());
		gorevYeriGirisDurum = ortakIslemler.getParameterKey("uygulamaTipi").equals("H") && ortakIslemler.getParameterKey("gorevYeriGiris").equals("1");
		departmanBolumAyni = Boolean.FALSE;
		fazlaMesaiTalepVar = planGirisi && aramaSecenekleri.getSirket() != null && aramaSecenekleri.getSirket().isFazlaMesaiTalepGirer() && aramaSecenekleri.getSirket().getDepartman().isFazlaMesaiTalepGirer();
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
		if (aylikPuantajList == null)
			aylikPuantajList = new ArrayList<AylikPuantaj>();
		else
			aylikPuantajList.clear();
		List<VardiyaGun> aylikSablonVardiyalar = new ArrayList<VardiyaGun>();
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
		setAylikPuantajDonem(denklestirmeAy);
		setDenklestirmeAyDurum(fazlaMesaiOrtakIslemler.getDurum(denklestirmeAy, loginUser));
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
		if (testDurum)
			logger.info("aylikPuantajOlusturuluyor 1000 " + PdksUtil.getCurrentTimeStampStr());

		AylikPuantaj defaultAylikPuantajSablon = fazlaMesaiOrtakIslemler.getAylikPuantaj(ay, yil, denklestirmeDonemi, session);
		tatilGunleriMap = null;
		boolean veriGuncelle = false;
		for (Iterator iterator = denklestirmeAy.getModeller().iterator(); iterator.hasNext();) {
			CalismaModeliAy calismaModeliAy = (CalismaModeliAy) iterator.next();
			CalismaModeli cm = calismaModeliAy.getCalismaModeli();
			if (calismaModeliAy.getSure() == 0.0d || calismaModeliAy.getToplamIzinSure() == 0.0d || ((denklestirmeAy.getSure() == 0.0d || denklestirmeAy.getToplamIzinSure() == 0.0d) && cm.getHaftaIci() == 9.0d)) {
				double sure = 0.0d, toplamIzinSure = 0.0d;
				for (VardiyaGun vg : defaultAylikPuantajSablon.getVardiyalar()) {
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

		defaultAylikPuantajSablon.setFazlaMesaiHesapla(Boolean.TRUE);
		AylikPuantaj gecenAylikPuantajSablon = null;
		if (ay != 1)
			gecenAylikPuantajSablon = fazlaMesaiOrtakIslemler.getAylikPuantaj(ay - 1, yil, denklestirmeDonemiGecenAy, session);
		else
			gecenAylikPuantajSablon = fazlaMesaiOrtakIslemler.getAylikPuantaj(12, yil - 1, denklestirmeDonemiGecenAy, session);
		basTarih = denklestirmeDonemi.getBaslangicTarih();
		bitTarih = denklestirmeDonemi.getBitisTarih();

		devam = true;
		User islemYapan = (User) loginUser.clone();

		ArrayList<String> perList = null;
		fields.clear();
		bolumKatlari = null;

		gorevYerileri = new ArrayList<Long>();
		if (gorevliPersonelMap != null)
			gorevliPersonelMap.clear();
		else
			gorevliPersonelMap = new HashMap<String, Personel>();

		perList = new ArrayList<String>();

		List<Personel> donemPerList = fazlaMesaiOrtakIslemler.getFazlaMesaiPersonelList(aramaSecenekleri.getSirket(), aramaSecenekleri.getTesisId() != null ? String.valueOf(aramaSecenekleri.getTesisId()) : null, aramaSecenekleri.getEkSaha3Id(), aramaSecenekleri.getEkSaha4Id(),
				denklestirmeAy != null ? aylikPuantajDonem : null, getDenklestirmeDurum(), session);
		if (testDurum)
			logger.info("aylikPuantajOlusturuluyor 2000 " + PdksUtil.getCurrentTimeStampStr());
		sicilNo = ortakIslemler.getSicilNo(sicilNo);
		for (Personel personel : donemPerList) {
			if (PdksUtil.hasStringValue(sicilNo) == false || sicilNo.trim().equals(personel.getPdksSicilNo().trim()))
				perList.add(personel.getPdksSicilNo());
		}

		if (!loginUser.isDirektorSuperVisor() && !loginUser.isIK() && (loginUser.isYonetici() || loginUser.isYoneticiKontratli())) {
			ortakIslemler.sistemeGirisIslemleri(islemYapan, Boolean.TRUE, basTarih, bitTarih, session);

			boolean hastane = ortakIslemler.getParameterKey("uygulamaTipi").equals("H");

			if (aramaSecenekleri.getEkSaha3Id() != null && hastane) {
				if (aramaSecenekleri.getGorevYeriList() != null) {
					if (aramaSecenekleri.getEkSaha3Id() != null)
						gorevYerileri.add(aramaSecenekleri.getEkSaha3Id());
					else
						for (Iterator iterator = aramaSecenekleri.getGorevYeriList().iterator(); iterator.hasNext();) {
							SelectItem item = (SelectItem) iterator.next();
							gorevYerileri.add((Long) item.getValue());
						}
					List<VardiyaGorev> gorevliler = ortakIslemler.getVardiyaGorevYerleri(loginUser, defaultAylikPuantajSablon.getIlkGun(), defaultAylikPuantajSablon.getSonGun(), gorevYerileri, session);
					for (VardiyaGorev pdksVardiyaGorev : gorevliler) {
						Personel personel = pdksVardiyaGorev.getVardiyaGun().getPersonel();
						String perNo = personel.getPdksSicilNo();
						if (PdksUtil.hasStringValue(perNo) == false)
							continue;
						perNo = perNo.trim();
						if (!perList.contains(perNo) && (PdksUtil.hasStringValue(sicilNo) == false || sicilNo.equals(perNo)))
							gorevliPersonelMap.put(personel.getPdksSicilNo().trim(), personel);
					}

					if (!gorevliPersonelMap.isEmpty())
						perList.addAll(new ArrayList(gorevliPersonelMap.keySet()));
				}

			}

		}
		if (adminRole || islemYapan.getYetkiTumPersonelNoList().contains(sicilNo))
			if (PdksUtil.hasStringValue(sicilNo))
				perList.add(sicilNo);

		ArrayList<Personel> personelList = null;
		ArrayList<Long> perIdler = new ArrayList<Long>();

		if (fazlaMesaiMap == null)
			fazlaMesaiMap = new TreeMap<String, Tanim>();
		else
			fazlaMesaiMap.clear();
		int okumaAdet = 0;
		List<String> talepGunList = new ArrayList<String>();
		ArrayList<String> perNoList = perList.isEmpty() ? new ArrayList<String>() : new ArrayList<String>(perList);
		while (!perNoList.isEmpty() && okumaAdet < 2) {
			Boolean tekrarOku = false;
			++okumaAdet;
			boolean flush = false;

			fazlaMesaiMap = ortakIslemler.getFazlaMesaiMap(session);
			fields.clear();
			List<String> superVisorList = null;
			if (loginUser.isSuperVisor()) {
				superVisorList = new ArrayList<String>();
				for (Personel personel : loginUser.getTumPersoneller()) {
					String sicil = personel.getSicilNo();
					if (!PdksUtil.hasStringValue(sicil))
						continue;
					superVisorList.add(sicil);
				}

			}
			for (Iterator iterator = perList.iterator(); iterator.hasNext();) {
				String sicil = (String) iterator.next();
				if (!PdksUtil.hasStringValue(sicil))
					iterator.remove();
				else if (superVisorList != null && !superVisorList.contains(sicil))
					iterator.remove();

			}

			if (!perList.isEmpty()) {
				if (gorevTipiId != null)
					fields.put("gorevTipi.id=", gorevTipiId);
				if (departmanBolumAyni == false && aramaSecenekleri.getTesisId() != null && aramaSecenekleri.getTesisId() > 0)
					fields.put("tesis.id=", aramaSecenekleri.getTesisId());
				if (aramaSecenekleri.getEkSaha3Id() != null)
					fields.put("ekSaha3.id=", aramaSecenekleri.getEkSaha3Id());
				if ((PdksUtil.hasStringValue(sicilNo) || PdksUtil.hasStringValue(donusAdres)) && perList != null && !perList.isEmpty())
					fields.put("pdksSicilNo", perList);
				if (departmanBolumAyni == false && aramaSecenekleri.getSirketId() != null)
					fields.put("sirket.id=", aramaSecenekleri.getSirketId());

				fields.put("pdksSicilNo", perList);
				if (!Personel.getGrubaGirisTarihiAlanAdi().equalsIgnoreCase(Personel.COLUMN_NAME_GRUBA_GIRIS_TARIHI))
					fields.put("iseBaslamaTarihi<=", bitTarih);
				else
					fields.put("grubaGirisTarihi<=", bitTarih);
				fields.put("sskCikisTarihi>=", basTarih);
				fields.put("sablon<>", null);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				try {
					personelList = (ArrayList<Personel>) pdksEntityController.getObjectByInnerObjectListInLogic(fields, Personel.class);
					if (aramaSecenekleri.getEkSaha3Id() != null && !personelList.isEmpty() && !gorevliPersonelMap.isEmpty()) {
						for (Iterator iterator = personelList.iterator(); iterator.hasNext();) {
							Personel personel = (Personel) iterator.next();
							if (personel.getEkSaha3() == null || personel.getIseGirisTarihi() == null || personel.getIstenAyrilisTarihi() == null)
								iterator.remove();
							else if (!personel.getEkSaha3().getId().equals(aramaSecenekleri.getEkSaha3Id()) && !helpPersonel(personel))
								iterator.remove();
						}
					}
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

					personelList = null;
				}
			}

			if (personelList != null && !personelList.isEmpty()) {
				boolean mevcutDonem = yil == cal.get(Calendar.YEAR) && ay == cal.get(Calendar.MONTH) + 1;
				TreeMap<Long, Personel> perKeyMap = new TreeMap<Long, Personel>();
				for (Personel personel : personelList) {
					perKeyMap.put(personel.getId(), personel);
				}
				perIdler.addAll(new ArrayList<Long>(perKeyMap.keySet()));
				if (testDurum)
					logger.info("aylikPuantajOlusturuluyor 3000 " + PdksUtil.getCurrentTimeStampStr());
				List<VardiyaGun> vardiyaGunList = null;
				fields.clear();
				fields.put("bitisZamani>=", ortakIslemler.tariheGunEkleCikar(cal, denklestirmeDonemiGecenAy.getBaslangicTarih(), -2));
				fields.put("baslangicZamani<=", ortakIslemler.tariheGunEkleCikar(cal, bitTarih, 1));
				fields.put("izinSahibi.id", perIdler.clone());
				fields.put("izinDurumu", ortakIslemler.getAktifIzinDurumList());
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<PersonelIzin> izinler = pdksEntityController.getObjectByInnerObjectListInLogic(fields, PersonelIzin.class);
				HashMap<Long, List<PersonelIzin>> izinMap = new HashMap<Long, List<PersonelIzin>>();
				for (Iterator iterator = izinler.iterator(); iterator.hasNext();) {
					PersonelIzin personelIzin = (PersonelIzin) iterator.next();
					IzinTipi izinTipi = personelIzin.getIzinTipi();
					if (izinTipi.getBakiyeIzinTipi() != null)
						iterator.remove();
					else {
						Long key = personelIzin.getIzinSahibi().getId();
						List<PersonelIzin> list = izinMap.containsKey(key) ? izinMap.get(key) : new ArrayList<PersonelIzin>();
						if (list.isEmpty())
							izinMap.put(key, list);
						list.add(personelIzin);
					}

				}
				TreeMap<String, VardiyaGun> yeniVardiyaMap = null;
				if (denklestirmeAyDurum) {
					try {
						vardiyaGunList = ortakIslemler.getAllPersonelIdVardiyalar(perIdler, ortakIslemler.tariheGunEkleCikar(cal, basTarih, -7), ortakIslemler.tariheGunEkleCikar(cal, bitTarih, 7), Boolean.FALSE, session);
						List<Long> olmayanPerList = new ArrayList<Long>(perIdler);
						for (VardiyaGun vardiyaGun : vardiyaGunList) {
							vardiyaGun.setAyinGunu(vardiyaGun.getVardiyaDateStr().startsWith(donem));
							if (vardiyaGun.isAyinGunu()) {
								Long perId = vardiyaGun.getPersonel().getId();
								if (olmayanPerList.contains(perId))
									olmayanPerList.remove(perId);
							}
						}
						if (!olmayanPerList.isEmpty()) {
							List<Personel> perYeniList = new ArrayList<Personel>();
							for (Long key : olmayanPerList)
								perYeniList.add(perKeyMap.get(key));
							yeniVardiyaMap = ortakIslemler.getVardiyalar(perYeniList, ortakIslemler.tariheGunEkleCikar(cal, basTarih, -7), ortakIslemler.tariheGunEkleCikar(cal, bitTarih, 7), izinMap, true, session, false);
							tekrarOku = true;
							if (!yeniVardiyaMap.isEmpty())
								vardiyaGunList = ortakIslemler.getAllPersonelIdVardiyalar(perIdler, ortakIslemler.tariheGunEkleCikar(cal, basTarih, -7), ortakIslemler.tariheGunEkleCikar(cal, bitTarih, 7), Boolean.FALSE, session);
							perYeniList = null;

						}
					} catch (Exception e) {
						vardiyaGunList = ortakIslemler.getAllPersonelIdVardiyalar(perIdler, ortakIslemler.tariheGunEkleCikar(cal, basTarih, -7), ortakIslemler.tariheGunEkleCikar(cal, bitTarih, 7), Boolean.FALSE, session);
					}
				} else
					vardiyaGunList = ortakIslemler.getPersonelIdVardiyalar(perIdler, ortakIslemler.tariheGunEkleCikar(cal, basTarih, -7), ortakIslemler.tariheGunEkleCikar(cal, bitTarih, 7), null, session);
				if (yeniVardiyaMap == null)
					yeniVardiyaMap = new TreeMap<String, VardiyaGun>();
				if (testDurum)
					logger.info("aylikPuantajOlusturuluyor 4000 " + PdksUtil.getCurrentTimeStampStr());

				perKeyMap = null;
				fields.clear();
				fields.put(PdksEntityController.MAP_KEY_MAP, "getPersonelId");
				fields.put("personel.id", perIdler.clone());
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				TreeMap<Long, PersonelExtra> extraMap = pdksEntityController.getObjectByInnerObjectMap(fields, PersonelExtra.class, Boolean.FALSE);

				fields.clear();
				fields.put("vardiyaTipi", Vardiya.TIPI_OFF);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				Vardiya offVardiya = (Vardiya) pdksEntityController.getObjectByInnerObject(fields, Vardiya.class);

				TreeMap<Long, PersonelDenklestirme> denklestirmeMap = getPersonelDenklestirme(denklestirmeAy, perIdler);
				flush = denklestirmeMap.isEmpty();
				TreeMap<Long, PersonelDenklestirme> denklestirmeGecenAyMap = getPersonelDenklestirme(denklestirmeGecenAy, perIdler);
				TreeMap<Long, PersonelDenklestirme> denklestirmeGelecekAyMap = getPersonelDenklestirme(denklestirmeGelecekAy, perIdler);

				TreeMap<String, VardiyaHafta> vardiyaHaftaMap = getVardiyaHaftaMap(perIdler);
				if (!flush)
					flush = vardiyaHaftaMap.isEmpty();

				fields.clear();
				fields.put(PdksEntityController.MAP_KEY_MAP, "getVardiyaGunId");
				fields.put("vardiyaGun", vardiyaGunList);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				TreeMap<Long, VardiyaGorev> vardiyaGunGorevMap = pdksEntityController.getObjectByInnerObjectMapInLogic(fields, VardiyaGorev.class, false);

				List<Long> idList = null;
				if ((loginUser.isYonetici() || loginUser.isYoneticiKontratli()) && !loginUser.isDirektorSuperVisor()) {
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

				for (VardiyaGun pdksVardiyaGun : vardiyaGunList) {
					pdksVardiyaGun.setAyinGunu(pdksVardiyaGun.getVardiyaDateStr().startsWith(donem));
					VardiyaGorev pdksVardiyaGorev = vardiyaGunGorevMap.containsKey(pdksVardiyaGun.getId()) ? vardiyaGunGorevMap.get(pdksVardiyaGun.getId()) : null;
					if (pdksVardiyaGorev == null) {
						pdksVardiyaGorev = new VardiyaGorev();
						pdksVardiyaGorev.setVardiyaGun(pdksVardiyaGun);
					} else if (pdksVardiyaGorev.getYeniGorevYeri() != null && idList != null && !idList.isEmpty() && !helpPersonel(pdksVardiyaGun.getPersonel()) && idList.contains(pdksVardiyaGorev.getYeniGorevYeri().getId())) {
						if (pdksVardiyaGorev.isOzelDurumYok()) {
							pdksEntityController.deleteObject(session, entityManager, pdksVardiyaGorev);
							pdksVardiyaGorev = null;
							pdksVardiyaGorev = new VardiyaGorev();
							pdksVardiyaGorev.setVardiyaGun(pdksVardiyaGun);
							flush = true;
						} else if (gorevYeriGirisDurum) {
							pdksVardiyaGorev.setYeniGorevYeri(null);
							pdksEntityController.saveOrUpdate(session, entityManager, pdksVardiyaGorev);
							flush = true;
						}
					}
					pdksVardiyaGun.setVardiyaGorev(pdksVardiyaGorev);
					setVardiyalarToMap(pdksVardiyaGun, vardiyalarMap, null);
				}
				TreeMap<Long, List> bagliIdMap = new TreeMap<Long, List>();
				if (!vardiyaGunList.isEmpty()) {
					List<YemekIzin> yemekGenelList = ortakIslemler.getYemekList(basTarih, bitTarih, session);
					ortakIslemler.setVardiyaYemekList(vardiyaGunList, yemekGenelList);
					fazlaMesaiTalepVar = false;
					List<Long> vardiyaIdList = new ArrayList<Long>();
					for (VardiyaGun vardiyaGun : vardiyaGunList) {
						if (vardiyaGun.getId() != null) {
							if (vardiyaGun.isFazlaMesaiTalepDurum() && vardiyaGun.isAyinGunu()) {
								fazlaMesaiTalepVar = true;
								if (!talepGunList.contains(vardiyaGun.getVardiyaDateStr()))
									talepGunList.add(vardiyaGun.getVardiyaDateStr());
								vardiyaIdList.add(vardiyaGun.getId());
							}

						}

					}
					if (testDurum)
						logger.info("aylikPuantajOlusturuluyor 5000 " + PdksUtil.getCurrentTimeStampStr());
					if (!vardiyaIdList.isEmpty()) {
						List<FazlaMesaiTalep> fazlaMesaiList = getVardiyaTable(vardiyaIdList, FazlaMesaiTalep.TABLE_NAME, FazlaMesaiTalep.COLUMN_NAME_VARDIYA_GUN, FazlaMesaiTalep.class);
						if (fazlaMesaiList != null && !fazlaMesaiList.isEmpty()) {
							for (FazlaMesaiTalep fazlaMesaiTalep : fazlaMesaiList) {
								Long id = fazlaMesaiTalep.getVardiyaGun().getId();
								List list = bagliIdMap.containsKey(id) ? bagliIdMap.get(id) : new ArrayList();
								if (list.isEmpty())
									bagliIdMap.put(id, list);
								list.add(fazlaMesaiTalep);
							}
						}

						List<PersonelFazlaMesai> personelFazlaMesaiList = getVardiyaTable(vardiyaIdList, PersonelFazlaMesai.TABLE_NAME, PersonelFazlaMesai.COLUMN_NAME_VARDIYA_GUN, PersonelFazlaMesai.class);
						if (personelFazlaMesaiList != null && !personelFazlaMesaiList.isEmpty()) {
							for (PersonelFazlaMesai personelFazlaMesai : personelFazlaMesaiList) {
								Long id = personelFazlaMesai.getVardiyaGun().getId();
								List list = bagliIdMap.containsKey(id) ? bagliIdMap.get(id) : new ArrayList();
								if (list.isEmpty())
									bagliIdMap.put(id, list);
								list.add(personelFazlaMesai);
							}
						}
					}
					vardiyaIdList = null;
				}

				vardiyaGunList = null;
				vardiyaGunGorevMap = null;
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
				if (testDurum)
					logger.info("aylikPuantajOlusturuluyor 6000 " + PdksUtil.getCurrentTimeStampStr());
				tatilGunleriMap = ortakIslemler.getTatilGunleri(personelList, denklestirmeDonemiGecenAy.getBaslangicTarih(), bitTarih, session);

				fazlaMesaiOrtakIslemler.haftalikVardiyaOlustur(vardiyaHaftaList, gecenAylikPuantajSablon, denklestirmeDonemiGecenAy, tatilGunleriMap, null);

				vardiyaHaftaList = new ArrayList<VardiyaHafta>();
				VardiyaPlan pdksVardiyaPlanMaster = fazlaMesaiOrtakIslemler.haftalikVardiyaOlustur(vardiyaHaftaList, defaultAylikPuantajSablon, denklestirmeDonemi, tatilGunleriMap, aylikSablonVardiyalar);
				if (testDurum)
					logger.info("aylikPuantajOlusturuluyor 7000 " + PdksUtil.getCurrentTimeStampStr());

				this.setVardiyaPlan(pdksVardiyaPlanMaster);
				if (gorevliPersonelMap != null)
					gorevliPersonelMap.clear();
				else
					gorevliPersonelMap = new HashMap<String, Personel>();

				for (Iterator iterator2 = aylikSablonVardiyalar.iterator(); iterator2.hasNext();) {
					VardiyaGun pdksVardiyaGunMaster = (VardiyaGun) iterator2.next();
					pdksVardiyaGunMaster.setDonemStr(donem);

					String tarih = PdksUtil.convertToDateString(pdksVardiyaGunMaster.getVardiyaDate(), "yyyyMMdd");
					pdksVardiyaGunMaster.setAyinGunu(tarih.startsWith(donem));
					if (tatilGunleriMap.containsKey(tarih))
						pdksVardiyaGunMaster.setTatil(tatilGunleriMap.get(tarih));
					else
						pdksVardiyaGunMaster.setTatil(null);
				}

				for (Personel personel : personelList) {

					boolean pdks = false;
					try {
						pdks = personel.getMailTakip() && personel.getPdks().equals(Boolean.FALSE);
					} catch (Exception e) {
						pdks = false;
					}
					personel.setPersonelExtra(extraMap.containsKey(personel.getId()) ? extraMap.get(personel.getId()) : new PersonelExtra());
					boolean vardiyaCalisiyor = Boolean.FALSE;
					long iseBasTarih = Long.parseLong(PdksUtil.convertToDateString(personel.getIseGirisTarihi(), "yyyyMMdd"));
					long istenAyrilmaTarih = Long.parseLong(PdksUtil.convertToDateString(personel.getSonCalismaTarihi(), "yyyyMMdd"));
					VardiyaSablonu sablonu = personel.getSablon();
					AylikPuantaj aylikPuantaj = new AylikPuantaj(), gecenAylikPuantaj = new AylikPuantaj();
					List<VardiyaGun> vardiyaGunler = new ArrayList<VardiyaGun>();

					aylikPuantaj.setGecenAylikPuantaj(gecenAylikPuantaj);
					gecenAylikPuantaj.setPdksPersonel(personel);
					PersonelDenklestirme personelDenklestirme = null;
					if (denklestirmeAyDurum) {
						personelDenklestirme = denklestirmeMap.containsKey(personel.getId()) ? denklestirmeMap.get(personel.getId()) : new PersonelDenklestirme(personel, denklestirmeAy, ortakIslemler.getCalismaModeliAy(denklestirmeAy, personel, session));
						boolean kaydet = personelDenklestirme.getId() == null;
						if (personelDenklestirme.getPersonelDenklestirmeGecenAy() == null && denklestirmeGecenAyMap.containsKey(personel.getId())) {
							kaydet = true;
							personelDenklestirme.setPersonelDenklestirmeGecenAy(denklestirmeGecenAyMap.get(personel.getId()));
							personelDenklestirme.setGuncellendi(Boolean.TRUE);
						}
						if (personelDenklestirme.getCalismaModeliAy() != null && personelDenklestirme.getPlanlanSure().doubleValue() == 0.0d && personelDenklestirme.getCalismaModeli().getToplamGunGuncelle()) {
							personelDenklestirme.setPlanlanSure(personelDenklestirme.getCalismaModeliAy().getSure());
							pdksEntityController.saveOrUpdate(session, entityManager, personelDenklestirme);
							flush = true;
							kaydet = true;
						}
						boolean denklestirme = true;
						if (kaydet) {
							if (personelDenklestirme.getId() == null) {
								if (personelDenklestirme.getCalismaModeliAy() == null || personelDenklestirme.getCalismaModeli().getToplamGunGuncelle().equals(Boolean.FALSE))
									personelDenklestirme.setPlanlanSure(-1d);
								personelDenklestirme.setOlusturanUser(loginUser);
								denklestirme = personelDenklestirme.isDenklestirme();
							}
						} else {
							denklestirme = personelDenklestirme.isDenklestirme();

							if (denklestirmeAyDurum) {
								kaydet = personelDenklestirme.isSuaDurumu() != personel.isSuaOlur();
								if (!kaydet)
									kaydet = denklestirme != personelDenklestirme.isDenklestirme();
								if (!kaydet)
									try {
										kaydet = personel.getFazlaMesaiOde() != null && personelDenklestirme.getFazlaMesaiOde() != null && !personelDenklestirme.getFazlaMesaiOde().equals(personel.getFazlaMesaiOde());
									} catch (Exception e1) {
										e1.printStackTrace();
									}

							}

						}
						if (mevcutDonem) {
							if (personelDenklestirme.getCalismaModeliAy() != null && personelDenklestirme.getCalismaModeli() != null) {
								CalismaModeli calismaModeli = personelDenklestirme.getCalismaModeli();
								if (calismaModeli.getBagliVardiyaSablonu() != null) {
									VardiyaSablonu bagliVardiyaSablonu = calismaModeli.getBagliVardiyaSablonu();
									Personel personelDenk = personelDenklestirme.getPersonel();
									if (!bagliVardiyaSablonu.getId().equals(personelDenk.getSablon().getId())) {
										personelDenk.setSablon(bagliVardiyaSablonu);
										pdksEntityController.saveOrUpdate(session, entityManager, personelDenk);
										flush = true;
										kaydet = true;
									}
								}

							}

						}

						if (kaydet) {
							personelDenklestirme.setGuncellendi(personelDenklestirme.getId() == null);
							if (personelDenklestirme.getId() != null) {

								personelDenklestirme.setGuncellemeTarihi(new Date());
							}
							if (denklestirmeAyDurum) {
								if (personel.getEgitimDonemi() != null && personel.getEgitimDonemi() && personelDenklestirme.getEgitimSuresiAksamGunSayisi() == null)
									personelDenklestirme.setEgitimSuresiAksamGunSayisi(0);
								if (personelDenklestirme.getId() == null)
									personelDenklestirme.setFazlaMesaiOde(personel.getFazlaMesaiOde());

								personelDenklestirme.setDenklestirme(denklestirme);
								if (personel.isSuaOlur() && personelDenklestirme.getSuaDurum() == null)
									personelDenklestirme.setSuaDurum(personel.isSuaOlur());
							}

							if (!personelDenklestirme.isKapandi())
								personelDenklestirme.setDevredenSure(null);
							if (personelDenklestirme.isGuncellendi()) {
								pdksEntityController.saveOrUpdate(session, entityManager, personelDenklestirme);
								flush = true;
							}

						}
						aylikPuantaj.setPersonelDenklestirmeAylik(personelDenklestirme);

					} else {
					}
					List<VardiyaGun> vardiyaGunHareketOnaysizList = new ArrayList<VardiyaGun>();

					aylikPuantaj.setPdksPersonel(personel);
					aylikPuantaj.setVardiyaPlan(new VardiyaPlan());
					aylikPuantaj.getVardiyaPlan().getVardiyaHaftaList().clear();
					aylikPuantaj.setTrClass(devam ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
					devam = !devam;
					double saatToplami = 0d;
					CalismaModeliAy calismaModeliAy = denklestirmeAyDurum && personelDenklestirme != null ? personelDenklestirme.getCalismaModeliAy() : null;
					Date sonCalismaTarihi = personel.getSonCalismaTarihi(), iseGirisTarihi = personel.getIseGirisTarihi();
					boolean personelCalmayaBasladi = false, calisiyor = false, hareketKaydiVardiyaBul = calismaModeliAy != null && calismaModeliAy.isHareketKaydiVardiyaBulsunmu();
					for (Iterator iterator2 = aylikSablonVardiyalar.iterator(); iterator2.hasNext();) {
						VardiyaGun pdksVardiyaGunMaster = (VardiyaGun) iterator2.next();
						VardiyaGun pdksVardiyaGun = new VardiyaGun(personel, null, pdksVardiyaGunMaster.getVardiyaDate());
						if (!personelCalmayaBasladi)
							personelCalmayaBasladi = pdksVardiyaGunMaster.getVardiyaDate().getTime() >= iseGirisTarihi.getTime();
						String vardiyaKey = pdksVardiyaGun.getVardiyaKey();
						if (vardiyalarMap.containsKey(vardiyaKey)) {
							pdksVardiyaGun = vardiyalarMap.get(pdksVardiyaGun.getVardiyaKey());
							pdksVardiyaGun.setAyinGunu(pdksVardiyaGun.getVardiyaDateStr().startsWith(donem));
						}
						pdksVardiyaGun.setDonemStr(pdksVardiyaGunMaster.getDonemStr());

						pdksVardiyaGun.setTdClass(aylikPuantaj.getTrClass());
						boolean kayit = false;
						if (!pdksVardiyaGun.isCalismayaBaslamadi() && !pdksVardiyaGun.isCalismayiBirakti()) {
							boolean yeniKayit = false;
							if (pdksVardiyaGun.getId() == null) {
								yeniKayit = true;
								if (pdksVardiyaGun.isAyinGunu())
									tekrarOku = true;
								if (tatilGunleriMap.containsKey(pdksVardiyaGun.getVardiyaDateStr()))
									pdksVardiyaGun.setTatil(tatilGunleriMap.get(pdksVardiyaGun.getVardiyaDateStr()));
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
								pdksVardiyaGun.setOlusturanUser(loginUser);
								if (yeniKayit && pdksVardiyaGun.getTatil() != null && pdksVardiyaGun.getVardiya().isCalisma()) {
									Tatil tatil = pdksVardiyaGun.getTatil();
									if (!tatil.isYarimGunMu())
										pdksVardiyaGun.setVardiya(offVardiya);
								}

								try {
									if (personelCalmayaBasladi && pdksVardiyaGunMaster.getVardiyaDate().getTime() <= sonCalismaTarihi.getTime()) {
										calisiyor = true;
										kayit = true;
									}

								} catch (Exception ee) {
									if (calisiyor)
										logger.error(pdksVardiyaGun.getVardiyaKeyStr() + "\n" + ee);
								}

								VardiyaGorev pdksVardiyaGorev = new VardiyaGorev();
								pdksVardiyaGorev.setVardiyaGun(pdksVardiyaGun);
								pdksVardiyaGun.setVardiyaGorev(pdksVardiyaGorev);
								flush = true;
							}
							if (pdksVardiyaGun.isAyinGunu()) {
								if (hareketKaydiVardiyaBul && !pdksVardiyaGun.getVardiya().isHaftaTatil() && pdksVardiyaGun.getVersion() >= 0) {
									if ((yeniKayit || yeniVardiyaMap.containsKey(vardiyaKey))) {
										kayit = true;
										if (pdksVardiyaGun.getVardiya().isCalisma())
											pdksVardiyaGun.setVersion(-1);
										vardiyaGunHareketOnaysizList.add(pdksVardiyaGun);

									}
								}
								vardiyalarMap.put(pdksVardiyaGun.getVardiyaKey(), pdksVardiyaGun);
								if (hareketKaydiVardiyaBul && !pdksVardiyaGun.getDurum() && pdksVardiyaGun.getVersion() == 0 && !pdksVardiyaGun.getVardiya().isHaftaTatil())
									aylikHareketKaydiVardiyaBul = Boolean.TRUE;
							}
							if (kayit) {
								if (pdksVardiyaGun.isAyinGunu() && denklestirmeAyDurum)
									pdksVardiyaGun.setDurum(false);
								pdksEntityController.saveOrUpdate(session, entityManager, pdksVardiyaGun);
								flush = true;
							}
						} else {
							if (pdksVardiyaGun.getId() != null) {
								if (pdksVardiyaGun.getVardiyaGorev().getId() != null) {
									VardiyaGorev pdksVardiyaGorev = pdksVardiyaGun.getVardiyaGorev();
									pdksEntityController.deleteObject(session, entityManager, pdksVardiyaGorev);
									pdksVardiyaGun.getVardiyaGorev().setId(null);
									pdksVardiyaGun.getVardiyaGorev().setYeniGorevYeri(null);
								}
								if (bagliIdMap.containsKey(pdksVardiyaGun.getId())) {
									List list = bagliIdMap.get(pdksVardiyaGun.getId());
									for (Iterator iterator = list.iterator(); iterator.hasNext();) {
										Object object = (Object) iterator.next();
										pdksEntityController.deleteObject(session, entityManager, object);
									}
								}
								pdksEntityController.deleteObject(session, entityManager, pdksVardiyaGun);
								flush = true;
								pdksVardiyaGun.setId(null);
								pdksVardiyaGun.setVardiya(null);

							}

						}

						vardiyaGunler.add(pdksVardiyaGun);

					}
					TreeMap<String, VardiyaGun> vardiyaGunMap = new TreeMap<String, VardiyaGun>();
					for (VardiyaGun pdksVardiyaGun : vardiyaGunler) {
						pdksVardiyaGun.setIzin(null);
						if (pdksVardiyaGun.getVardiya() != null && !pdksVardiyaGun.isAyinGunu()) {
							if (pdksVardiyaGun.getVardiyaDate().before(defaultAylikPuantajSablon.getIlkGun())) {
								if (aylikPuantaj.getPersonelDenklestirmeGecenAy() == null && denklestirmeGecenAyMap.containsKey(pdksVardiyaGun.getPersonel().getId()))
									aylikPuantaj.setPersonelDenklestirmeGecenAy(denklestirmeGecenAyMap.get(pdksVardiyaGun.getPersonel().getId()));
							} else if (pdksVardiyaGun.getVardiyaDate().after(defaultAylikPuantajSablon.getSonGun())) {
								if (aylikPuantaj.getPersonelDenklestirmeGelecekAy() == null && denklestirmeGelecekAyMap.containsKey(pdksVardiyaGun.getPersonel().getId()))
									aylikPuantaj.setPersonelDenklestirmeGelecekAy(denklestirmeGelecekAyMap.get(pdksVardiyaGun.getPersonel().getId()));
							}

						}
						vardiyaGunMap.put(String.valueOf(pdksVardiyaGun.getVardiyaKeyStr()), pdksVardiyaGun);
					}

					boolean haftaRenk = true;

					for (Iterator iterator = defaultAylikPuantajSablon.getVardiyaHaftaList().iterator(); iterator.hasNext();) {
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
						if (pdks == false || !calisiyor) {
							if (pdksVardiyaHafta.getId() != null) {
								pdksEntityController.deleteObject(session, entityManager, pdksVardiyaHafta);
								pdksVardiyaHafta.setId(null);
								flush = true;
							}
							if (!calisiyor)
								pdksVardiyaHafta.setVardiyaGunler(haftaVardiyaGunleri);
						} else {
							if (pdks && pdksVardiyaHafta.getId() == null && sablonu != null) {
								pdksVardiyaHafta.setVardiyaSablonu(sablonu);
								pdksVardiyaHafta.setOlusturanUser(loginUser);
								pdksEntityController.saveOrUpdate(session, entityManager, pdksVardiyaHafta);
								flush = true;
							}

						}
						if (calisiyor) {
							pdksVardiyaHafta.getVardiyaGunler().clear();
							for (VardiyaGun pdksVardiyaGun2 : haftaVardiyaGunleri) {
								String key = pdksVardiyaGun2.getVardiyaKeyStr();
								long ayinGunu = Long.parseLong(key.substring(key.indexOf("_") + 1));
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
						if (personelDenklestirme != null)
							gecenAylikPuantaj.setPersonelDenklestirmeAylik(personelDenklestirme.getPersonelDenklestirmeGecenAy());
						ortakIslemler.puantajHaftalikPlanOlustur(Boolean.FALSE, vardiyaGunMap, vardiyalarMap, gecenAylikPuantajSablon, gecenAylikPuantaj);
					} else
						aylikPuantaj.setGecenAylikPuantaj(null);
					ortakIslemler.puantajHaftalikPlanOlustur(Boolean.FALSE, vardiyaGunMap, vardiyalarMap, defaultAylikPuantajSablon, aylikPuantaj);
					VardiyaPlan plan = new VardiyaPlan();
					plan.setPersonel(personel);
					plan.setVardiyaGunMap(vardiyaGunMap);

					List<VardiyaGun> list = PdksUtil.sortListByAlanAdi(new ArrayList(vardiyalarMap.values()), "vardiyaDate", Boolean.FALSE);

					setIzin(plan.getPersonel().getId(), izinler, list);
					try {
						if (gecenAylikPuantaj.getPersonelDenklestirmeAylik() != null && !helpPersonel(personel) && loginUser.getLogin()) {
							gecenAylikPuantaj.setCalismaModeliAy(gecenAylikPuantaj.getPersonelDenklestirmeAylik().getCalismaModeliAy());
							gecenAylikPuantaj.setLoginUser(loginUser);
							ortakIslemler.aylikPlanSureHesapla(true, gecenAylikPuantaj, false, tatilGunleriMap, session);
						}

					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());

					}
					if (!vardiyaGunHareketOnaysizList.isEmpty()) {
						for (Iterator iterator = vardiyaGunHareketOnaysizList.iterator(); iterator.hasNext();) {
							VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
							if (vardiyaGun.getId() != null && vardiyaGun.isIzinli() && vardiyaGun.isAyinGunu() && vardiyaGun.getVersion() < 0) {
								vardiyaGun.setVersion(0);
								pdksEntityController.saveOrUpdate(session, entityManager, vardiyaGun);
								flush = true;
							}

						}

					}
					vardiyaGunHareketOnaysizList = null;
					if (personelDenklestirme != null && personelDenklestirme.getPlanlanSure() < 0d) {
						if (personelDenklestirme.getCalismaModeliAy() == null || personelDenklestirme.getCalismaModeli().getToplamGunGuncelle().equals(Boolean.FALSE))
							personelDenklestirme.setPlanlanSure(saatToplami);
						if (!personelDenklestirme.isKapandi())
							personelDenklestirme.setDevredenSure(null);
						pdksEntityController.saveOrUpdate(session, entityManager, personelDenklestirme);
						flush = true;
					}
					vardiyaPlan = null;
					vardiyaGunMap = null;
					aylikPuantaj.setVardiyaHaftaList(aylikPuantaj.getVardiyaPlan().getVardiyaHaftaList());
					if (personelDenklestirme == null) {
						personelDenklestirme = denklestirmeMap.containsKey(personel.getId()) ? denklestirmeMap.get(personel.getId()) : new PersonelDenklestirme(personel, denklestirmeAy, ortakIslemler.getCalismaModeliAy(denklestirmeAy, personel, session));
						aylikPuantaj.setPersonelDenklestirmeAylik(personelDenklestirme);
					}

					if (personelDenklestirme != null) {
						if (personelDenklestirme.getCalismaModeliAy() == null && personel.getCalismaModeli() != null && modelMap.containsKey(personel.getCalismaModeli().getId())) {
							personelDenklestirme.setCalismaModeliAy(modelMap.get(personel.getCalismaModeli().getId()));
							pdksEntityController.saveOrUpdate(session, entityManager, personelDenklestirme);
							flush = true;
						}
						aylikPuantaj.setCalismaModeliAy(personelDenklestirme.getCalismaModeliAy());
						CalismaModeli calismaModeli = personelDenklestirme.getCalismaModeli();
						aylikPuantaj.setLoginUser(loginUser);
						if (!helpPersonel(personel))
							ortakIslemler.aylikPlanSureHesapla(true, aylikPuantaj, denklestirmeAyDurum, tatilGunleriMap, session);

						if (denklestirmeAy.getSure() == 0.0d) {
							Double genelSaatToplami = aylikPuantaj.getSaatToplami();
							aylikPuantaj.setPlanlananSure(genelSaatToplami);
							int gun = new Double(genelSaatToplami / AylikPuantaj.getGunlukCalismaSuresi()).intValue();
							Double toplamIzinSure = genelSaatToplami - (gun * (AylikPuantaj.getGunlukCalismaSuresi() - 7.5d));
							denklestirmeAy.setSure(genelSaatToplami);
							denklestirmeAy.setToplamIzinSure(toplamIzinSure);
							User sistemUser = ortakIslemler.getSistemAdminUser(session);
							denklestirmeAy.setGuncellemeTarihi(new Date());
							denklestirmeAy.setGuncelleyenUser(sistemUser);
							pdksEntityController.saveOrUpdate(session, entityManager, denklestirmeAy);
							ortakIslemler.aylikPlanSureHesapla(true, aylikPuantaj, denklestirmeAyDurum, tatilGunleriMap, session);
							flush = true;
						}
						if (!denklestirmeAyDurum) {
							aylikPuantaj.setFazlaMesaiSure(personelDenklestirme.getOdenecekSure());
							aylikPuantaj.setDevredenSure(personelDenklestirme.getDevredenSure());
							aylikPuantaj.setEksikCalismaSure(personelDenklestirme.getEksikCalismaSure());
							aylikPuantaj.setResmiTatilToplami(personelDenklestirme.getResmiTatilSure());
							aylikPuantaj.setHaftaCalismaSuresi(personelDenklestirme.getHaftaCalismaSuresi());
							aylikPuantaj.setAksamVardiyaSayisi(personelDenklestirme.getAksamVardiyaSayisi().intValue());
						} else {

							if (personel.getPartTime() != null && personel.getPartTime().booleanValue()) {
								aylikPuantaj.setFazlaMesaiSure(0d);
								aylikPuantaj.setDevredenSure(0d);
								aylikPuantaj.setResmiTatilToplami(0d);
								aylikPuantaj.setHaftaCalismaSuresi(0d);
								aylikPuantaj.setAksamVardiyaSayisi(0);
								aylikPuantaj.setEksikCalismaSure(0d);
							} else {
								aylikPuantaj.setEksikCalismaSure(0.0d);
								double devredenSure = aylikPuantaj.getDevredenSure();
								if (calismaModeli.isSaatlikOdeme()) {
									if (devredenSure < 0.0d) {
										Double eksikCalismaSure = -aylikPuantaj.getDevredenSure();
										if (aylikPuantaj.getFazlaMesaiSure() > 0.0d) {
											double sure = aylikPuantaj.getFazlaMesaiSure() - eksikCalismaSure;
											eksikCalismaSure = 0.0d;
											aylikPuantaj.setFazlaMesaiSure(0.0d);
											if (sure > 0)
												aylikPuantaj.setFazlaMesaiSure(sure);
											else
												eksikCalismaSure = -sure;
										}
										aylikPuantaj.setEksikCalismaSure(eksikCalismaSure);

									} else if (devredenSure > 0.0d) {
										double sure = aylikPuantaj.getFazlaMesaiSure() + devredenSure;
										aylikPuantaj.setFazlaMesaiSure(sure);
									}
									aylikPuantaj.setDevredenSure(0.0d);
								}

							}

						}
						aylikPuantaj.setOnayDurum(!personelDenklestirme.isCheckBoxDurum());
					} else
						aylikPuantaj.setOnayDurum(Boolean.FALSE);

					if (!onayDurum)
						onayDurum = aylikPuantaj.isOnayDurum();
					int yuvarmaTipi = aylikPuantaj.getYarimYuvarla();
					if (aylikPuantaj.getResmiTatilToplami() > 0)
						aylikPuantaj.setResmiTatilToplami(PdksUtil.setSureDoubleTypeRounded(aylikPuantaj.getResmiTatilToplami(), yuvarmaTipi));
					if (aylikPuantaj.getHaftaCalismaSuresi() > 0)
						aylikPuantaj.setHaftaCalismaSuresi(PdksUtil.setSureDoubleTypeRounded(aylikPuantaj.getHaftaCalismaSuresi(), yuvarmaTipi));
					if (aylikPuantaj.getVardiyalar() != null) {
						for (VardiyaGun pdksVardiyaGun : aylikPuantaj.getVardiyalar()) {
							if (pdksVardiyaGun.isAyinGunu() && pdksVardiyaGun.getVardiya() != null) {
								vardiyaCalisiyor = Boolean.TRUE;
								break;
							}

						}
					}
					aylikPuantaj.setVardiyalar(vardiyaGunler);

					if (vardiyaCalisiyor)
						aylikPuantajList.add(aylikPuantaj);
				}

			}
			if (testDurum)
				logger.info("aylikPuantajOlusturuluyor 8000 " + PdksUtil.getCurrentTimeStampStr());

			setTatilMap(tatilGunleriMap);
			if (flush) {
				session.flush();
				veriGuncellendi = Boolean.TRUE;
			}
			if (tekrarOku) {
				tekrarOku = denklestirmeAyDurum && !aylikPuantajList.isEmpty();
				if (tekrarOku) {
					aylikPuantajList.clear();
					talepGunList.clear();
					perList.clear();
					perList.addAll(perNoList);
					session.clear();

				}

			}
			if (!tekrarOku)
				++okumaAdet;

		}
		defaultAylikPuantajSablon.setVardiyalar(aylikSablonVardiyalar);
		setAylikPuantajDefault(defaultAylikPuantajSablon);
		bitTarih = null;
		resmiTatilVar = Boolean.FALSE;
		aksamGunVar = Boolean.FALSE;
		aksamSaatVar = Boolean.FALSE;
		haftaTatilVar = Boolean.FALSE;
		eksikMaasGoster = Boolean.FALSE;
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
				if (!eksikMaasGoster)
					eksikMaasGoster = aylikPuantaj.getEksikCalismaSure() != 0d;

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
		if (aylikPuantajList != null && loginUser.getLogin())
			aylikVardiyaOzetOlustur();
		List<AylikPuantaj> aylikPuantajAllList = new ArrayList<AylikPuantaj>();
		Long userId = loginUser.getPdksPersonel().getId();
		boolean kullaniciYonetici = loginUser.isYonetici() || loginUser.isSuperVisor() || loginUser.isProjeMuduru() || loginUser.isDirektorSuperVisor();
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
			eksikMaasGoster = Boolean.FALSE;
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
					if (!eksikMaasGoster)
						eksikMaasGoster = puantaj.getEksikCalismaSure() != 0d;
				} catch (Exception e) {
					logger.info(e);
				}

				if (puantaj.getVardiyalar() != null) {
					for (Iterator iterator2 = puantaj.getVardiyalar().iterator(); iterator2.hasNext();) {
						VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator2.next();
						if (pdksVardiyaGun.isFazlaMesaiTalepDurum())
							fazlaMesaiTalepVar = Boolean.TRUE;
						pdksVardiyaGun.setAyinGunu(list.contains(pdksVardiyaGun.getVardiyaDateStr()));

					}

				}
				if (!fazlaMesaiTalepVar && planGirisi)
					fazlaMesaiTalepVar = puantaj.isFazlaMesaiTalepVar();
				if (!puantaj.getTrClass().equals("help")) {
					puantaj.setTrClass(devam ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
					devam = !devam;
				}

			}
			suaKontrol(aylikPuantajList);
			list = null;
		}
		ortakIslemler.yoneticiPuantajKontrol(loginUser, aylikPuantajList, Boolean.FALSE, session);
		if (denklestirmeAyDurum && !aylikPuantajList.isEmpty()) {
			try {
				setDenklestirmeAlanlari(aylikPuantajList);
			} catch (Exception e) {

			}

		}
		vardiyaFazlaMesaiMap.clear();
		if (fazlaMesaiTalepVar && aylikPuantajList != null && !aylikPuantajList.isEmpty())
			fazlaMesaiGunleriniBul(defaultAylikPuantajSablon.getIlkGun(), defaultAylikPuantajSablon.getSonGun(), aylikPuantajList);
		vardiyaFazlaMesaiTalepGoster = planGirisi && !vardiyaFazlaMesaiMap.isEmpty() && (loginUser.isAdmin() || PdksUtil.isSistemDestekVar());
		TreeMap<String, Object> ozetMap = fazlaMesaiOrtakIslemler.getIzinOzetMap(loginUser, null, aylikPuantajList, false);
		izinTipiVardiyaList = ozetMap.containsKey("izinTipiVardiyaList") ? (List<Vardiya>) ozetMap.get("izinTipiVardiyaList") : new ArrayList<Vardiya>();
		izinTipiPersonelVardiyaMap = ozetMap.containsKey("izinTipiPersonelVardiyaMap") ? (TreeMap<String, TreeMap<String, List<VardiyaGun>>>) ozetMap.get("izinTipiPersonelVardiyaMap") : new TreeMap<String, TreeMap<String, List<VardiyaGun>>>();
		if (izinTipiVardiyaList != null && !izinTipiVardiyaList.isEmpty()) {
			fazlaMesaiOrtakIslemler.personelIzinAdetleriOlustur(aylikPuantajList, izinTipiVardiyaList, izinTipiPersonelVardiyaMap);
		}
		if (fazlaMesaiTalepler == null)
			fazlaMesaiTalepler = new ArrayList<FazlaMesaiTalep>();
		else
			fazlaMesaiTalepler.clear();
		boolean vardiyaHareketKontrol = ortakIslemler.getParameterKey("vardiyaHareketKontrol").equals("1");
		if (vardiyaHareketKontrol && defaultAylikPuantajSablon != null && (loginUser.isAdmin()))
			vardiyaHareketKontrol(defaultAylikPuantajSablon);
		aylikPuantajAllList = null;
		if (fazlaMesaiTalepVar && denklestirmeAyDurum && aylikPuantajList.size() > 0)
			topluFazlaCalismaTalep = ortakIslemler.getParameterKey("topluFazlaCalismaTalep").equals("1") || (userHome != null && loginUser.getLogin() && userHome.hasPermission("vardiyaPlani", "topluFazlaCalismaTalep")) || loginUser.isAdmin();
		else
			topluFazlaCalismaTalep = false;

		for (Iterator iterator = aylikPuantajList.iterator(); iterator.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();

			if (!gebeGoster)
				gebeGoster = aylikPuantaj.isGebeDurum();
			PersonelDenklestirme personelDenklestirmeAylik = aylikPuantaj.getPersonelDenklestirmeAylik();
			if (topluFazlaCalismaTalep)
				topluFazlaCalismaTalep = personelDenklestirmeAylik.getPersonel().getSirket().isFazlaMesaiTalepGirer();
			if (fazlaMesaiTalepVar)
				fazlaMesaiTalepVar = personelDenklestirmeAylik.getPersonel().getSirket().isFazlaMesaiTalepGirer();
			if (!sutIzniGoster)
				sutIzniGoster = (personelDenklestirmeAylik.getSutIzniDurum() != null && personelDenklestirmeAylik.getSutIzniDurum());
			if (!partTimeGoster)
				partTimeGoster = personelDenklestirmeAylik.getPartTime() != null && personelDenklestirmeAylik.getPartTime();

		}
		int adet = 0;
		if (topluFazlaCalismaTalep) {
			topluFazlaCalismaTalep = false;
			// aylikPuantajDefault

			for (Iterator iterator = aylikPuantajList.iterator(); iterator.hasNext();) {
				AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
				if (aylikPuantaj.getPersonelDenklestirmeAylik().isOnaylandi()) {
					boolean durum = aylikPuantaj.isFazlaMesaiDurum();
					if (durum) {
						++adet;
					}

				}

			}
		}
		topluFazlaCalismaTalep = adet > 1;
		if (topluFazlaCalismaTalep) {
			for (VardiyaGun vardiyaGun : aylikPuantajDefault.getAyinVardiyalari()) {
				vardiyaGun.setFazlaMesaiTalepDurum(talepGunList.contains(vardiyaGun.getVardiyaDateStr()));
			}
		}

		aylikHareketKaydiVardiyaBul = Boolean.FALSE;
		if (denklestirmeAyDurum)
			aylikHareketKaydiVardiyalariBul();
		if (adminRole || denklestirmeAyDurum) {
			fields.clear();
			fields.put("id", aramaSecenekleri.getSirketId());
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			Sirket sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);
			if (sirket != null)
				bordroVeriOlusturBasla(sirket, aylikPuantajList);
		}
		if (!aylikPuantajList.isEmpty()) {
			ortakIslemler.sortAylikPuantajList(aylikPuantajList, true);

			modelGoster = ortakIslemler.getModelGoster(denklestirmeAy, session);
		}

		return kontrolDurum;
	}

	/**
	 * @param sirket
	 * @param puantajList
	 */
	private void bordroVeriOlusturBasla(Sirket sirket, List<AylikPuantaj> puantajList) {
		if (sirket != null && ortakIslemler.getParameterKeyHasStringValue("bordroVeriOlustur")) {
			try {
				String str = ortakIslemler.getParameterKey("bordroVeriOlustur");
				int donem = yil * 100 + ay;
				if (donem >= Integer.parseInt(str))
					baslikMap = fazlaMesaiOrtakIslemler.bordroVeriOlustur(false, puantajList, denklestirmeAyDurum, String.valueOf(donem), authenticatedUser, session);
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
		}
		boolean saatlikCalismaVar = ortakIslemler.getParameterKey("saatlikCalismaVar").equals("1");
		gerceklesenMesaiKod = saatlikCalismaVar == false || bordroPuantajEkranindaGoster == false || baslikMap.containsKey(ortakIslemler.gerceklesenMesaiKod());
		devredenBakiyeKod = saatlikCalismaVar == false || bordroPuantajEkranindaGoster == false || baslikMap.containsKey(ortakIslemler.devredenBakiyeKod());
		devredenMesaiKod = saatlikCalismaVar == false || bordroPuantajEkranindaGoster == false || baslikMap.containsKey(ortakIslemler.devredenMesaiKod());
		ucretiOdenenKod = saatlikCalismaVar == false || bordroPuantajEkranindaGoster == false || baslikMap.containsKey(ortakIslemler.ucretiOdenenKod());
		normalCalismaSaatKod = (saatlikCalismaVar && bordroPuantajEkranindaGoster) || baslikMap.containsKey(ortakIslemler.normalCalismaSaatKod());
		haftaTatilCalismaSaatKod = (saatlikCalismaVar && bordroPuantajEkranindaGoster) || (baslikMap.containsKey(ortakIslemler.haftaTatilCalismaSaatKod()));
		resmiTatilCalismaSaatKod = (saatlikCalismaVar && bordroPuantajEkranindaGoster) || (baslikMap.containsKey(ortakIslemler.resmiTatilCalismaSaatKod()));
		izinSureSaatKod = (saatlikCalismaVar && bordroPuantajEkranindaGoster) || (baslikMap.containsKey(ortakIslemler.izinSureSaatKod()));
		normalCalismaGunKod = bordroPuantajEkranindaGoster && baslikMap.containsKey(ortakIslemler.normalCalismaGunKod());
		haftaTatilCalismaGunKod = bordroPuantajEkranindaGoster && baslikMap.containsKey(ortakIslemler.haftaTatilCalismaGunKod());
		resmiTatilCalismaGunKod = bordroPuantajEkranindaGoster && baslikMap.containsKey(ortakIslemler.resmiTatilCalismaGunKod());
		izinSureGunKod = bordroPuantajEkranindaGoster && baslikMap.containsKey(ortakIslemler.izinSureGunKod());
		ucretliIzinGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.ucretliIzinGunKod());
		ucretsizIzinGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.ucretsizIzinGunKod());
		hastalikIzinGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.hastalikIzinGunKod());
		normalGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.normalGunKod());
		haftaTatilGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.haftaTatilGunKod());
		resmiTatilGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.resmiTatilGunKod());
		artikGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.artikGunKod());
		bordroToplamGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.bordroToplamGunKod());

	}

	/**
	 * @param puantajList
	 * @throws Exception
	 */
	@Transactional
	private void setDenklestirmeAlanlari(List<AylikPuantaj> puantajList) throws Exception {

		TreeMap<Long, PersonelDenklestirme> map = new TreeMap<Long, PersonelDenklestirme>();
		StringBuffer pdIdSb = new StringBuffer();
		for (Iterator iterator = puantajList.iterator(); iterator.hasNext();) {
			AylikPuantaj ap = (AylikPuantaj) iterator.next();
			PersonelDenklestirme pd = ap.getPersonelDenklestirmeAylik();
			map.put(pd.getId(), pd);
			pdIdSb.append(pd.getId() + "");
			if (iterator.hasNext())
				pdIdSb.append(",");
		}

		StringBuffer sb = new StringBuffer();
		sb.append("SP_GET_PERS_DENK_DINAMIK_ALAN");
		LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
		fields.put("pdId", pdIdSb.toString());
		fields.put("durum", 1);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List list = pdksEntityController.execSPList(fields, sb, null);
		pdIdSb = null;
		sb = null;
		List<Long> tanimIdList = new ArrayList<Long>();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Object[] object = (Object[]) iterator.next();
			if (object != null) {
				if (object[1] != null)
					tanimIdList.add(((BigDecimal) object[1]).longValue());

			}
		}

		if (!tanimIdList.isEmpty()) {
			fields.clear();
			fields.put(PdksEntityController.MAP_KEY_MAP, "getId");
			fields.put("id", tanimIdList);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			TreeMap<Long, Tanim> tanimMap = pdksEntityController.getObjectByInnerObjectMap(fields, Tanim.class, false);
			boolean flush = false;
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Object[] object = (Object[]) iterator.next();
				if (object != null) {
					Long pdId = ((BigDecimal) object[2]).longValue(), alanId = ((BigDecimal) object[1]).longValue();
					PersonelDenklestirmeDinamikAlan pdda = new PersonelDenklestirmeDinamikAlan(map.get(pdId), tanimMap.get(alanId));
					pdda.setDurum(Boolean.TRUE);
					pdksEntityController.saveOrUpdate(session, entityManager, pdda);
					flush = true;
				}
			}
			if (flush)
				session.flush();

		}
		list = null;

		tanimIdList = null;

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
	 * @param t1
	 * @param t2
	 * @param list
	 */
	private void fazlaMesaiGunleriniBul(Date t1, Date t2, List<AylikPuantaj> list) {
		List<Long> perIdList = new ArrayList<Long>();
		for (AylikPuantaj aylikPuantaj : list) {
			if (aylikPuantaj.getPdksPersonel() != null)
				perIdList.add(aylikPuantaj.getPdksPersonel().getId());
		}
		if (!perIdList.isEmpty()) {
			HashMap map = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT F.* FROM " + VardiyaGun.TABLE_NAME + " V WITH(nolock) ");
			sb.append(" INNER JOIN " + FazlaMesaiTalep.TABLE_NAME + " F ON F." + FazlaMesaiTalep.COLUMN_NAME_VARDIYA_GUN + "=V." + VardiyaGun.COLUMN_NAME_ID);
			sb.append(" AND F." + FazlaMesaiTalep.COLUMN_NAME_DURUM + "=1  AND F." + FazlaMesaiTalep.COLUMN_NAME_ONAY_DURUMU + "<> " + FazlaMesaiTalep.ONAY_DURUM_RED);
			sb.append(" WHERE V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ">=:t1 AND  V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + "<=:t2  ");
			sb.append(" AND V." + VardiyaGun.COLUMN_NAME_PERSONEL + ":p  ");
			sb.append(" ORDER BY F." + FazlaMesaiTalep.COLUMN_NAME_BASLANGIC_ZAMANI);
			map.put("p", perIdList);
			map.put("t1", t1);
			map.put("t2", t2);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			fazlaMesaiTalepler = pdksEntityController.getObjectBySQLList(sb, map, FazlaMesaiTalep.class);
			for (FazlaMesaiTalep fmt : fazlaMesaiTalepler) {
				Long key = fmt.getVardiyaGun().getId();
				List<FazlaMesaiTalep> ftmList = vardiyaFazlaMesaiMap.containsKey(key) ? vardiyaFazlaMesaiMap.get(key) : new ArrayList<FazlaMesaiTalep>();
				if (ftmList.isEmpty())
					vardiyaFazlaMesaiMap.put(key, ftmList);
				ftmList.add(fmt);
			}

		}
		perIdList = null;
	}

	/**
	 * @param aylikPuantajSablon
	 */
	private void vardiyaHareketKontrol(AylikPuantaj aylikPuantajSablon) {
		if (denklestirmeAy != null && denklestirmeAyDurum && aylikPuantajList != null && aylikPuantajList.size() > 0) {
			TreeMap<Long, AylikPuantaj> personelIdMap = new TreeMap<Long, AylikPuantaj>();
			for (AylikPuantaj aylikPuantaj : aylikPuantajList) {
				Personel personel = aylikPuantaj.getPdksPersonel();
				personelIdMap.put(personel.getPersonelKGS().getId(), aylikPuantaj);
			}

			List<Long> kapiIdler = ortakIslemler.getPdksKapiIdler(session, Boolean.TRUE);
			TreeMap<Long, TreeMap<String, List<HareketKGS>>> map = new TreeMap<Long, TreeMap<String, List<HareketKGS>>>();
			try {
				List<HareketKGS> kgsList = ortakIslemler.getHareketBilgileri(kapiIdler, new ArrayList<Long>(personelIdMap.keySet()), aylikPuantajSablon.getIlkGun(), aylikPuantajSablon.getSonGun(), HareketKGS.class, session);
				if (!kgsList.isEmpty()) {
					for (HareketKGS hareket : kgsList) {
						if (hareket.getDurum() != 0)
							continue;

						Long perId = hareket.getPersonelId();
						TreeMap<String, List<HareketKGS>> map1 = map.containsKey(perId) ? map.get(perId) : new TreeMap<String, List<HareketKGS>>();
						if (map1.isEmpty())
							map.put(perId, map1);
						String tarihStr = PdksUtil.convertToDateString(hareket.getZaman(), "yyyyMMdd");
						List<HareketKGS> list = map1.containsKey(tarihStr) ? map1.get(tarihStr) : new ArrayList<HareketKGS>();
						if (list.isEmpty())
							map1.put(tarihStr, list);
						list.add(hareket);
					}
					if (!map.isEmpty()) {
						for (Long perId : map.keySet()) {
							AylikPuantaj aylikPuantaj = personelIdMap.get(perId);
							TreeMap<String, List<HareketKGS>> map1 = map.get(perId);
							// PersonelDenklestirme personelDenklestirme = aylikPuantaj.getPersonelDenklestirmeAylik();
							// CalismaModeli calismaModeli = personelDenklestirme.getCalismaModeli();
							for (VardiyaGun vardiyaGun : aylikPuantaj.getAyinVardiyalari()) {
								String tarihStr = vardiyaGun.getVardiyaDateStr();
								List<HareketKGS> orjinalList = new ArrayList<HareketKGS>(map1.get(tarihStr));
								vardiyaGun.setYeniVardiya(null);
								map1.remove(tarihStr);
								if (vardiyaGun.getVardiya().isHaftaTatil() && orjinalList.size() > 1)
									continue;
								if (vardiyaGun.isAyinGunu() && vardiyaGun.isCalisan() && vardiyaGun.getIzin() == null) {
									for (Vardiya vardiya : vardiyaGun.getVardiyalar()) {
										if (vardiya.isCalisma()) {
											VardiyaGun vardiyaYeniGun = new VardiyaGun(vardiyaGun.getPersonel(), vardiya, vardiyaGun.getVardiyaDate());
											List<HareketKGS> list = new ArrayList<HareketKGS>(orjinalList);
											vardiyaYeniGun.setVardiyaZamani();
											Vardiya islemVardiya = vardiyaYeniGun.getIslemVardiya();
											Date basZaman = islemVardiya.getVardiyaTelorans1BasZaman(), bitZaman = islemVardiya.getVardiyaTelorans2BitZaman();
											for (Iterator iterator = list.iterator(); iterator.hasNext();) {
												HareketKGS hareket = (HareketKGS) iterator.next();
												if (hareket.getZaman().after(bitZaman) || hareket.getZaman().before(basZaman))
													continue;
												if (vardiyaYeniGun.addHareket((HareketKGS) hareket.clone(), Boolean.TRUE))
													iterator.remove();
											}
											if (list.isEmpty()) {
												if (vardiyaYeniGun.getGirisHareketleri() != null && vardiyaYeniGun.getCikisHareketleri() != null && vardiyaYeniGun.getGirisHareketleri().size() == vardiyaYeniGun.getCikisHareketleri().size()) {
													if (!vardiya.getId().equals(vardiyaGun.getVardiya().getId())) {
														vardiyaGun.setYeniVardiya(vardiya);
														logger.info(vardiyaYeniGun.getVardiyaKeyStr() + " " + vardiya.getVardiyaAdi());
													}
													break;
												}

											}

										}

									}

								}

							}
							map1 = null;

						}
					}
				}
			} catch (Exception e) {

			}

		}
	}

	/**
	 * @param list
	 */
	private void suaKontrol(List<AylikPuantaj> list) {
		if (list != null && departman != null && departman.isAdminMi()) {
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
				if (aylikPuantaj.getPersonelDenklestirmeAylik().getSuaDurum() != null && aylikPuantaj.getPersonelDenklestirmeAylik().getSuaDurum()) {
					aylikPuantaj.setVardiyaSua(Boolean.TRUE);
					continue;
				}

				boolean vardiyaSua = Boolean.FALSE;
				if (aylikPuantaj.getVardiyalar() != null) {

					for (Iterator iterator2 = aylikPuantaj.getVardiyalar().iterator(); iterator2.hasNext();) {
						VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator2.next();
						if (pdksVardiyaGun.isAyinGunu() && pdksVardiyaGun.getVardiya() != null && pdksVardiyaGun.getVardiya() != null) {
							if ((pdksVardiyaGun.getVardiya().getSua() != null && pdksVardiyaGun.getVardiya().getSua()) || (pdksVardiyaGun.getIzin() != null && pdksVardiyaGun.getIzin().getIzinTipi().isSuaIzin())) {
								vardiyaSua = Boolean.TRUE;
								break;
							}

						}

					}
					if (vardiyaSua)
						PdksUtil.addMessageAvailableWarn(aylikPuantaj.getPdksPersonel().getPdksSicilNo() + " " + aylikPuantaj.getPdksPersonel().getAdSoyad() + " şua izni veya vardiyası vardır!");

				}
				aylikPuantaj.setVardiyaSua(vardiyaSua);
			}
		}
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
	public String fillPersonelSicilAylikPlanRaporList() {
		if (!PdksUtil.hasStringValue(sicilNo))
			aylikPuantajList.clear();
		else {
			sicilNo = ortakIslemler.getSicilNo(sicilNo);
			try {
				fillAylikPlanRaporList();
			} catch (Exception e) {
				logger.equals(e);
				e.printStackTrace();
			}

		}

		return "";
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
	 * @param gorevYeriAciklama
	 * @param puantajList
	 * @param sonucGoster
	 * @return
	 */
	public ByteArrayOutputStream excelAylikMesaiTalepDevam() {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, "Fazla Mesai Talep", Boolean.TRUE);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddTutar = ExcelUtil.setAlignment(ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TUTAR, wb), CellStyle.ALIGN_CENTER);
		CellStyle styleOddNumber = ExcelUtil.setAlignment(ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_NUMBER, wb), CellStyle.ALIGN_CENTER);
		CellStyle styleOddDateTime = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATETIME, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenTutar = ExcelUtil.setAlignment(ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TUTAR, wb), CellStyle.ALIGN_CENTER);
		CellStyle styleEvenNumber = ExcelUtil.setAlignment(ExcelUtil.getStyleEven(ExcelUtil.FORMAT_NUMBER, wb), CellStyle.ALIGN_CENTER);
		CellStyle styleEvenDateTime = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATETIME, wb);

		int row = 0;
		int col = 0;
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		boolean tesisDurum = ortakIslemler.getListTesisDurum(aylikFazlaMesaiTalepler);
		if (tesisDurum)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Mesai Başlangıç Zamanı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Mesai Bitiş Zamanı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Mesai Süresi (Saat)");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Mesai Nedeni");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Onay Durum");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Onay'a Gönderen");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Onay'a Gönderme Zamanı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Onaylayan");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Onaylama Zamanı");
		boolean renk = true;
		for (Iterator iter = aylikFazlaMesaiTalepler.iterator(); iter.hasNext();) {
			FazlaMesaiTalep fmt = (FazlaMesaiTalep) iter.next();
			Personel personel = fmt.getVardiyaGun().getPersonel();
			Sirket sirket = personel.getSirket();
			++row;
			col = 0;
			CellStyle style = null, styleCenter = null, styleTutar = null, styleNumber = null, styleDateTime = null;
			if (renk) {
				styleDateTime = styleOddDateTime;
				styleNumber = styleOddNumber;
				styleTutar = styleOddTutar;
				style = styleOdd;
				styleCenter = styleOddCenter;
			} else {
				styleDateTime = styleEvenDateTime;
				styleNumber = styleEvenNumber;
				styleTutar = styleEvenTutar;
				style = styleEven;
				styleCenter = styleEvenCenter;
			}
			renk = !renk;
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getSicilNo());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(sirket.getAd());
			if (tesisDurum)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
			ExcelUtil.getCell(sheet, row, col++, styleDateTime).setCellValue(fmt.getBaslangicZamani());
			ExcelUtil.getCell(sheet, row, col++, styleDateTime).setCellValue(fmt.getBitisZamani());
			Double sure = fmt.getMesaiSuresi();
			ExcelUtil.getCell(sheet, row, col++, PdksUtil.isDoubleValueNotLong(sure) ? styleTutar : styleNumber).setCellValue(sure);
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(fmt.getMesaiNeden() != null ? fmt.getMesaiNeden().getAciklama() : "");
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(fmt.getOnayDurumAciklama());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(fmt.getOlusturanUser() != null ? fmt.getOlusturanUser().getAdSoyad() : "");
			if (fmt.getOlusturmaTarihi() != null)
				ExcelUtil.getCell(sheet, row, col++, styleDateTime).setCellValue(fmt.getOlusturmaTarihi());
			else
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");

			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(fmt.getGuncelleyenUser() != null ? fmt.getGuncelleyenUser().getAdSoyad() : "");
			if (fmt.getGuncellemeTarihi() != null)
				ExcelUtil.getCell(sheet, row, col++, styleEvenDateTime).setCellValue(fmt.getGuncellemeTarihi());
			else
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");

		}

		try {
			// double katsayi = 3.43;
			// int[] dizi = new int[] { 1575, 1056, 2011, 2056, 1575, 3722,
			// 1575, 2078, 2600, 2056, 3722, 2078, 2600, 2056, 3722, 2078, 2600,
			// 2056, 3722, 2078, 2600, 3722, 2078, 2600, 3722, 2078, 2600 };
			for (int i = 0; i < col; i++)
				sheet.autoSizeColumn(i);
			baos = new ByteArrayOutputStream();
			wb.write(baos);
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			baos = null;
		}

		return baos;

	}

	/**
	 * @return
	 * @throws Exception
	 */
	public String excelAylikMesaiTalepList() throws Exception {

		try {

			baosDosya = excelAylikMesaiTalepDevam();
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, "FazlaMesaiTalepListesi.xlsx");

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return "";

	}

	/**
	 * @return
	 * @throws Exception
	 */
	public String fillAylikMesaiTalepList() throws Exception {
		session.clear();
		LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
		veriMap.put("yil", yil);
		veriMap.put("ay", ay);
		Long perId = authenticatedUser.getPdksPersonel().getId();
		if (ikRole)
			veriMap.put("yoneticiId", 0L);
		else
			veriMap.put("yoneticiId", perId);
		veriMap.put("basTarih", basTarihStr != null ? PdksUtil.convertToDateString(PdksUtil.convertToJavaDate(basTarihStr, PdksUtil.getDateFormat()), "yyyy-MM-dd") : "");
		veriMap.put("bitTarih", bitTarihStr != null ? PdksUtil.convertToDateString(PdksUtil.convertToJavaDate(bitTarihStr, PdksUtil.getDateFormat()), "yyyy-MM-dd") : "");
		if (session != null)
			veriMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		StringBuffer sb = new StringBuffer("SP_FAZLA_MESAI_TALEP_MAIL");
		aylikFazlaMesaiTalepler = pdksEntityController.execSPList(veriMap, sb, FazlaMesaiTalep.class);
		islemYapiliyor = null;
		if (!aylikFazlaMesaiTalepler.isEmpty()) {
			HashMap fields = new HashMap();
			fields.put("yil", yil);
			fields.put("ay", ay);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
			setAylikPuantajDonem(denklestirmeAy);
			setDenklestirmeAyDurum(fazlaMesaiOrtakIslemler.getDurum(denklestirmeAy));
			List<FazlaMesaiTalep> list = new ArrayList<FazlaMesaiTalep>();
			islemYapiliyor = false;
			for (Iterator iterator = aylikFazlaMesaiTalepler.iterator(); iterator.hasNext();) {
				FazlaMesaiTalep fmt = (FazlaMesaiTalep) iterator.next();
				fmt.setCheckBoxDurum(false);
				boolean islemYapildi = false;
				if (fmt.getVardiyaGun().getPersonel().getPdksYonetici() != null && fmt.getVardiyaGun().getPersonel().getPdksYonetici().getPdksYonetici() != null)
					islemYapildi = fmt.getVardiyaGun().getPersonel().getPdksYonetici().getPdksYonetici().getId().equals(perId);
				if (!islemYapiliyor && islemYapildi)
					islemYapiliyor = fmt.isHatirlatmaMail();
				fmt.setIslemYapildi(islemYapildi);
				if (!islemYapildi) {
					list.add(fmt);
					fmt.setIslemYapildi(false);
					iterator.remove();
				}

			}
			if (!islemYapiliyor || denklestirmeAyDurum == false)
				islemYapiliyor = null;
			else
				islemYapiliyor = false;
			if (!list.isEmpty())
				aylikFazlaMesaiTalepler.addAll(list);
			list = null;

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
			if (aylikPuantajOlusturuluyor())
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
		vardiyaMap.clear();
		ArrayList<Vardiya> pdksList = new ArrayList<Vardiya>();
		String radyolojiIzinDurum = ortakIslemler.getParameterKey("radyolojiIzinDurum");
		TreeMap<Long, Vardiya> vardiyaMap = new TreeMap<Long, Vardiya>();
		try {
			if (pd == null && aylikPuantaj != null)
				pd = aylikPuantaj.getPersonelDenklestirmeAylik();
			boolean fmi = pd != null && fazlaMesaiIzinRaporuDurum && (pd.getFazlaMesaiIzinKullan() == null || pd.getFazlaMesaiIzinKullan());
			boolean calismaOlmayanVardiyalar = false;
			List<AylikPuantaj> aylikPuantajAllList = new ArrayList<AylikPuantaj>();
			Personel personel = null;
			if (aylikPuantaj != null) {
				personel = aylikPuantaj.getPdksPersonel();
				aylikPuantajAllList.add(aylikPuantaj);
			} else if (!aylikPuantajList.isEmpty())
				aylikPuantajAllList.addAll(aylikPuantajList);
			boolean gebeMi = false, sua = false, icap = false;
			for (Iterator iterator = aylikPuantajAllList.iterator(); iterator.hasNext();) {
				AylikPuantaj aylikPuantaj2 = (AylikPuantaj) iterator.next();
				personel = aylikPuantaj2.getPdksPersonel();
				if (!gebeMi)
					gebeMi = personel.isPersonelGebeMi();
				if (!sua)
					sua = personel.isSuaOlur();
				if (!icap)
					icap = personel.getIcapciOlabilir();

				String donem = String.valueOf(yil * 100 + ay);
				for (VardiyaGun pdksVardiyaGun : aylikPuantaj2.getVardiyalar()) {
					pdksVardiyaGun.setAyinGunu(pdksVardiyaGun.getVardiyaDateStr().startsWith(donem));
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
						if (pdksVardiya.isFMI() && fmi == false)
							continue;
						if (pdksVardiya.getDurum())
							vardiyaMap.put(pdksVardiya.getId(), pdksVardiya);
					}
				}
			}

			HashMap map = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("WITH VARDIYA_DATA AS ( ");
			sb.append("SELECT V.* FROM " + Vardiya.TABLE_NAME + " V WITH(nolock) ");
			if (pd != null) {

				if (pd.getCalismaModeliAy() != null) {
					try {
						CalismaModeliAy calismaModeliAy = pd.getCalismaModeliAy();
						CalismaModeli cm = calismaModeliAy.getCalismaModeli();
						if (cm.getGenelVardiya().equals(Boolean.FALSE) && cm.isOrtakVardiyadir() == false) {
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
			sb.append(" WHERE " + Vardiya.COLUMN_NAME_DURUM + "= 1   ");
			sb.append(" AND (" + Vardiya.COLUMN_NAME_DEPARTMAN + " IS NULL OR  " + Vardiya.COLUMN_NAME_DEPARTMAN + "=:departmanId) ");
			if (fmi == false) {
				sb.append(" AND  " + Vardiya.COLUMN_NAME_VARDIYA_TIPI + "<>:fm ");
				map.put("fm", Vardiya.TIPI_FMI);
			}

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
					} else if (!pdksVardiya.isIsKurMu()) {
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
			VardiyaSablonu vardiyaSablonu = personel.getSablon();
			vardiyaSablonu.setVardiyaList(null);
			List<Vardiya> list = new ArrayList<Vardiya>(vardiyaSablonu.getVardiyaList());
			List<Long> idList = new ArrayList<Long>();
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Vardiya vardiyaS = (Vardiya) iterator.next();
				if (vardiyaS == null || idList.contains(vardiyaS.getId())) {
					iterator.remove();
					continue;
				}
				idList.add(vardiyaS.getId());
				boolean ekle = true;
				for (Iterator iterator2 = vardiyaList.iterator(); iterator2.hasNext();) {
					Vardiya vardiya = (Vardiya) iterator2.next();
					if (vardiyaS.getId().equals(vardiya.getId())) {
						iterator.remove();
						ekle = false;
						break;
					}

				}
				if (ekle)
					vardiyaList.add(vardiyaS);
			}
			list = null;
			idList = null;
			if (vardiyaList.size() > 1)
				vardiyaList = PdksUtil.sortObjectStringAlanList(vardiyaList, "getKisaAdiSort", null);
			if (!ortakIslemler.getParameterKey("donemVardiyalariSiralama").equals("1"))
				donemVardiyalariSirala(vardiyaList, aylikPuantajAllList);
			aylikPuantajAllList = null;
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
	 * @param list
	 * @param aylikPuantajAllList
	 */
	private void donemVardiyalariSirala(List<Vardiya> list, List<AylikPuantaj> aylikPuantajAllList) {
		if (denklestirmeAyDurum && aylikPuantajList != null && !aylikPuantajList.isEmpty()) {
			List<Long> idList = new ArrayList<Long>();
			if (aylikPuantajAllList != null)
				for (AylikPuantaj ap : aylikPuantajAllList)
					idList.add(ap.getPdksPersonel().getId());
			int yilBas = maxYil - 1;
			int maxDonem = yil * 100 + ay;
			try {
				int sistemBaslangicYili = Integer.parseInt(ortakIslemler.getParameterKey("sistemBaslangicYili"));
				if (yilBas < sistemBaslangicYili)
					yilBas = sistemBaslangicYili;
			} catch (Exception e) {

			}
			StringBuffer sb = new StringBuffer();
			sb.append("SP_GET_PERSONEL_VARDIYA");
			LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
			veriMap.put("basYil", yilBas);
			veriMap.put("bitDonem", maxDonem);
			veriMap.put("perId", ortakIslemler.getListIdStr(idList));
			if (session != null)
				veriMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Vardiya> vardiyaDonemList = null;
			try {
				vardiyaDonemList = pdksEntityController.execSPList(veriMap, sb, Vardiya.class);

			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

			if (vardiyaDonemList != null && !vardiyaDonemList.isEmpty()) {
				for (Iterator iterator = vardiyaDonemList.iterator(); iterator.hasNext();) {
					Vardiya vardiyaDonem = (Vardiya) iterator.next();
					boolean sil = true;
					for (Iterator iterator2 = list.iterator(); iterator2.hasNext();) {
						Vardiya vardiya = (Vardiya) iterator2.next();
						if (vardiya.getId().equals(vardiyaDonem.getId())) {
							sil = false;
							iterator2.remove();
							break;
						}
					}
					if (sil)
						iterator.remove();

				}
				if (!list.isEmpty()) {
					vardiyaDonemList.addAll(list);
					list.clear();
				}
				list.addAll(vardiyaDonemList);
				vardiyaDonemList.clear();
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					Vardiya vardiya = (Vardiya) iterator.next();
					if (vardiya.isCalisma()) {
						vardiyaDonemList.add(vardiya);
						iterator.remove();
					}
				}
				if (!vardiyaDonemList.isEmpty())
					list.addAll(vardiyaDonemList);
			}
			vardiyaDonemList = null;
			idList = null;
		}
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
				else if (pdksVardiya.isIsKurMu())
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

	/**
	 * @throws Exception
	 */
	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	@Transactional
	public void sayfaMesaiTalepAction() throws Exception {
		if (session == null)
			session = PdksUtil.getSession(entityManager, Boolean.FALSE);
		session.setFlushMode(FlushMode.MANUAL);
		if (authenticatedUser != null)
			adminRoleDurum(authenticatedUser);
		fazlaMesaiTalepDurum = Boolean.TRUE;
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String id = (String) req.getParameter("id");
		islemFazlaMesaiTalep = null;
		HashMap fields = new HashMap();
		mesaiIptalNedenTanimList = null;
		if (id != null) {
			String decodeStr = OrtakIslemler.getDecodeStringByBase64(id);
			StringTokenizer st = new StringTokenizer(decodeStr, "&");
			HashMap<String, String> param = new HashMap<String, String>();
			String fmtIdStr = null, onayDurumuStr = null;
			while (st.hasMoreTokens()) {
				String tk = st.nextToken();
				String[] parStrings = tk.split("=");
				param.put(parStrings[0], parStrings[1]);
			}
			if (param.size() == 2) {
				if (param.containsKey("fmtId"))
					fmtIdStr = param.get("fmtId");
				else
					PdksUtil.addMessageAvailableWarn("Form bilgisi bulunamadı");
				if (param.containsKey("durum"))
					onayDurumuStr = param.get("durum");
				else
					PdksUtil.addMessageAvailableWarn("Form bilgisi onay bulunamadı");

			}
			boolean islemYapildi = fmtIdStr != null && onayDurumuStr != null;
			try {
				session.clear();
				if (islemYapildi) {
					int onayDurumu = Integer.parseInt(onayDurumuStr);
					long fmtId = Long.parseLong(fmtIdStr);
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					fields.put("id", new Long(fmtId));
					islemFazlaMesaiTalep = (FazlaMesaiTalep) pdksEntityController.getObjectByInnerObject(fields, FazlaMesaiTalep.class);

					if (islemFazlaMesaiTalep != null) {
						if (islemFazlaMesaiTalep.isIptalEdilebilir()) {
							if (islemFazlaMesaiTalep.getOnayDurumu() == FazlaMesaiTalep.ONAY_DURUM_ISLEM_YAPILMADI) {
								islemYapildi = false;
								if (onayDurumu == FazlaMesaiTalep.ONAY_DURUM_ONAYLANDI) {
									islemFazlaMesaiTalep.setOnayDurumu(onayDurumu);
									islemFazlaMesaiTalep.setGuncellemeTarihi(new Date());
									pdksEntityController.saveOrUpdate(session, entityManager, islemFazlaMesaiTalep);
									session.flush();
									mesaiMudurOnayCevabi(true);

								} else {
									if (redNedenleri == null)
										redNedenleri = new ArrayList<SelectItem>();
									else
										redNedenleri.clear();
									mesaiIptalNedenTanimList = ortakIslemler.getTanimList(Tanim.TIPI_FAZLA_MESAI_IPTAL_NEDEN, session);
									for (Tanim redNedeni : mesaiIptalNedenTanimList) {
										redNedenleri.add(new SelectItem(redNedeni.getId(), redNedeni.getAciklama()));
									}

								}

							}

						}
						if (islemYapildi)
							PdksUtil.addMessageAvailableWarn("Form bilgisi önceden " + islemFazlaMesaiTalep.getOnayDurumAciklama());

					} else
						PdksUtil.addMessageAvailableWarn("Form bilgisi bulunamadı");
				} else
					fillEkSahaTanim();

			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

		}
	}

	/**
	 * @param sirket
	 * @param tip
	 * @param veriList
	 */
	protected void fazlaMesaiListeGuncelle(Sirket sirket, String tip, List<SelectItem> veriList) {
		if (fazlaMesaiTalepDurum && ikRole) {

			if (denklestirmeAy != null && veriList != null && !veriList.isEmpty()) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.DATE, 1);
				cal.set(Calendar.MONTH, denklestirmeAy.getAy() - 1);
				cal.set(Calendar.YEAR, denklestirmeAy.getYil());
				Date tarih1 = PdksUtil.getDate(cal.getTime());
				cal.add(Calendar.MONTH, 1);
				cal.add(Calendar.DATE, -1);
				Date tarih2 = PdksUtil.getDate(cal.getTime());
				List list = new ArrayList();
				for (SelectItem selectItem : veriList)
					list.add(selectItem.getValue());

				StringBuffer sb = new StringBuffer();
				HashMap fields = new HashMap();
				sb.append("SELECT DISTINCT " + tip + ".ID FROM " + VardiyaGun.TABLE_NAME + " V WITH(nolock) ");
				sb.append(" INNER JOIN " + FazlaMesaiTalep.TABLE_NAME + " FT ON FT." + FazlaMesaiTalep.COLUMN_NAME_VARDIYA_GUN + "=V." + VardiyaGun.COLUMN_NAME_ID);
				sb.append(" AND FT." + FazlaMesaiTalep.COLUMN_NAME_DURUM + " =1 AND  FT." + FazlaMesaiTalep.COLUMN_NAME_ONAY_DURUMU + " NOT IN (" + FazlaMesaiTalep.ONAY_DURUM_RED + ") ");
				sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + "=V." + VardiyaGun.COLUMN_NAME_PERSONEL);
				if (sirket == null) {
					sb.append(" INNER JOIN " + Sirket.TABLE_NAME + " S ON S." + Sirket.COLUMN_NAME_ID + "=P." + Personel.COLUMN_NAME_SIRKET);
					if (tip.equals("D"))
						sb.append(" INNER JOIN " + Departman.TABLE_NAME + " D ON D." + Departman.COLUMN_NAME_ID + "=S." + Sirket.COLUMN_NAME_DEPARTMAN);
				} else {
					if (sirket.getDepartman().isAdminMi() || sirket.isTesisDurumu()) {
						if (sirket.getDepartman().isAdminMi() == false || sirket.isTesisDurumu()) {
							sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + "=:s");
							fields.put("s", sirket.getId());
						}
						if (tip.equals("T") && sirket.getDepartman().isAdminMi())
							sb.append(" INNER JOIN " + Tanim.TABLE_NAME + " T ON T." + Tanim.COLUMN_NAME_ID + "=P." + Personel.COLUMN_NAME_TESIS);
					}
				}
				if (tip.equals("B"))
					sb.append(" INNER JOIN " + Tanim.TABLE_NAME + " B ON B." + Tanim.COLUMN_NAME_ID + "=P." + Personel.COLUMN_NAME_EK_SAHA3);
				sb.append(" WHERE V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ">=:ta1 AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + "<=:ta2");
				sb.append(" AND " + tip + ".ID :l");
				fields.put("ta1", tarih1);
				fields.put("ta2", tarih2);
				fields.put("l", list);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List idler = null;
				try {
					idler = pdksEntityController.getObjectBySQLList(sb, fields, null);
				} catch (Exception e) {
					logger.equals(e);
					e.printStackTrace();
				}

				if (idler != null && veriList.size() > idler.size()) {
					list.clear();
					for (Iterator iterator = idler.iterator(); iterator.hasNext();) {
						Object id = (Object) iterator.next();
						if (id instanceof BigDecimal)
							list.add(((BigDecimal) id).longValue());
						else
							list.add(Long.parseLong(String.valueOf(id)));
					}
					for (Iterator iterator = veriList.iterator(); iterator.hasNext();) {
						SelectItem selectItem = (SelectItem) iterator.next();
						if (!list.contains(selectItem.getValue()))
							iterator.remove();
					}
				}
				veriList = null;
				list = null;
			}
		}

	}

	/**
	 * @throws Exception
	 */
	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaMesaiTalepListAction() throws Exception {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		adminRoleDurum(authenticatedUser);
		fazlaMesaiTalepDurum = Boolean.TRUE;
		session.clear();
		seciliVardiyaGun = null;
		islemYapiliyor = null;
		aylar = PdksUtil.getAyListesi(Boolean.TRUE);
		modelGoster = Boolean.FALSE;
		Calendar cal = Calendar.getInstance();
		ay = cal.get(Calendar.MONTH) + 1;
		yil = cal.get(Calendar.YEAR);
		cal.add(Calendar.MONTH, 1);
		maxYil = cal.get(Calendar.YEAR);
		if (aylikFazlaMesaiTalepler != null)
			aylikFazlaMesaiTalepler.clear();
		else
			aylikFazlaMesaiTalepler = new ArrayList<FazlaMesaiTalep>();
		sicilNo = "";
		kullaniciPersonel = ortakIslemler.getKullaniciPersonel(authenticatedUser);
		if (kullaniciPersonel)
			sicilNo = authenticatedUser.getPdksPersonel().getPdksSicilNo();
		mesaiDonemDegisti(true);
		fillEkSahaTanim();
	}

	/**
	 * @return
	 */
	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaFazlaMesaiTalepRaporAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		adminRoleDurum(authenticatedUser);

		fazlaMesaiTalepDurum = Boolean.TRUE;
		session.clear();
		mesaiOnayla = Boolean.FALSE;
		modelList = new ArrayList<CalismaModeliAy>();
		if (fazlaMesaiTalepler == null)
			fazlaMesaiTalepler = new ArrayList<FazlaMesaiTalep>();
		else
			fazlaMesaiTalepler.clear();
		try {
			departmanBolumAyni = Boolean.FALSE;
			aramaSecenekleri = new AramaSecenekleri(authenticatedUser);
			stajerSirket = Boolean.FALSE;
			boolean ayniSayfa = authenticatedUser.getCalistigiSayfa() != null && authenticatedUser.getCalistigiSayfa().equals("fazlaMesaiTalep");
			if (!ayniSayfa)
				authenticatedUser.setCalistigiSayfa("fazlaMesaiTalep");
			setPlanGirisi(Boolean.TRUE);
			islemYapiliyor = Boolean.FALSE;
			hastaneSuperVisor = authenticatedUser.isDirektorSuperVisor();

			aramaSecenekleri.setTesisList(null);
			aramaSecenekleri.setTesisId(null);
			aramaSecenekleri.setGorevYeriList(null);
			aramaSecenekleri.setEkSaha3Id(null);
			aramaSecenekleri.setSirketId(null);
			gorevYeri = null;

			Calendar cal = Calendar.getInstance();
			ay = cal.get(Calendar.MONTH) + 1;
			yil = cal.get(Calendar.YEAR);
			cal.add(Calendar.MONTH, 1);
			maxYil = cal.get(Calendar.YEAR);
			sonDonem = (maxYil * 100) + cal.get(Calendar.MONTH) + 1;
			if (basTarih == null && (ikRole)) {
				cal = Calendar.getInstance();
				setBasTarih(PdksUtil.getDate(cal.getTime()));
			}
			setBitTarih(null);
			LinkedHashMap<String, Object> veriLastMap = ortakIslemler.getLastParameter("fazlaMesaiTalep", session);

			aramaSecenekleri.setDepartman(authenticatedUser.getDepartman());
			aramaSecenekleri.setDepartmanId(authenticatedUser.getDepartman().getId());
			setDepartman(authenticatedUser.getDepartman());

			setVardiyaPlanList(new ArrayList<VardiyaPlan>());
			if (aramaSecenekleri.getGorevYeriList() == null && departman != null && !departman.isAdminMi())
				aramaSecenekleri.setGorevYeriList(fazlaMesaiOrtakIslemler.getBolumDepartmanSelectItems(departman.getId(), aramaSecenekleri.getSirketId(), yil, ay, Boolean.FALSE, session));

			donusAdres = "";
			Long tesisId = null, ekSaha4Id = null;

			if (veriLastMap != null) {
				if (veriLastMap.containsKey("yil") && veriLastMap.containsKey("ay") && veriLastMap.containsKey("sirketId")) {
					yil = Integer.parseInt((String) veriLastMap.get("yil"));
					ay = Integer.parseInt((String) veriLastMap.get("ay"));
					basTarih = null;
					if (veriLastMap.containsKey("tarih") && (ikRole))
						basTarih = PdksUtil.convertToJavaDate((String) veriLastMap.get("tarih"), "yyyy-MM-dd");
					Long sId = Long.parseLong((String) veriLastMap.get("sirketId"));
					aramaSecenekleri.setSirketId(sId);
					if (veriLastMap.containsKey("tesisId")) {
						tesisId = Long.parseLong((String) veriLastMap.get("tesisId"));
						aramaSecenekleri.setTesisId(tesisId);
					}
					if (veriLastMap.containsKey("altBolumId")) {
						ekSaha4Id = Long.parseLong((String) veriLastMap.get("altBolumId"));
						aramaSecenekleri.setEkSaha4Id(ekSaha4Id);
					}
					if (veriLastMap.containsKey("bolumId"))
						aramaSecenekleri.setEkSaha3Id(Long.parseLong((String) veriLastMap.get("bolumId")));
					if ((ikRole) && veriLastMap.containsKey("sicilNo"))
						sicilNo = (String) veriLastMap.get("sicilNo");

				}

			}
			aylariDoldur();
			if (adminRole)
				fillDepartmanList();

			fillSirketDoldur(false);
			tesisId = aramaSecenekleri.getTesisId();
			if (aramaSecenekleri.getSirketId() != null) {
				fillTesisDoldur(false);
			}
			if (aramaSecenekleri.getEkSaha3Id() != null || tesisId != null) {
				fillBolumDoldur();
			}

			if (!ayniSayfa)
				authenticatedUser.setCalistigiSayfa("");
			if (donusAdres.equals("fazlaMesaiTalep"))
				donusAdres = "";

			if (fazlaMesaiTalepDurumList == null)
				fazlaMesaiTalepDurumList = new ArrayList<SelectItem>();
			else
				fazlaMesaiTalepDurumList.clear();
			talepOnayDurum = 0;
			fazlaMesaiTalepDurumList.add(new SelectItem(talepOnayDurum, "Tüm Fazla Mesai Talepler"));
			fazlaMesaiTalepDurumList.add(new SelectItem(FazlaMesaiTalep.ONAY_DURUM_ISLEM_YAPILMADI, "İşlem Yapılmayan"));
			fazlaMesaiTalepDurumList.add(new SelectItem(FazlaMesaiTalep.ONAY_DURUM_ONAYLANDI, "Onaylanan"));
			fillEkSahaTanim();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * @return
	 */
	public String fillFazlaMesaiTalepList() {
		mailGonder = Boolean.FALSE;
		mesaiOnayla = Boolean.FALSE;
		linkAdres = null;
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.clear();

		HashMap fields = new HashMap();
		fields.put("ay", ay);
		fields.put("yil", yil);
		fazlaMesaiTalepler.clear();
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
		setAylikPuantajDonem(denklestirmeAy);
		setDenklestirmeAyDurum(fazlaMesaiOrtakIslemler.getDurum(denklestirmeAy));
		if (denklestirmeAy != null) {
			try {
				DepartmanDenklestirmeDonemi denklestirmeDonemi = new DepartmanDenklestirmeDonemi();
				AylikPuantaj aylikPuantaj = fazlaMesaiOrtakIslemler.getAylikPuantaj(ay, yil, denklestirmeDonemi, session);
				denklestirmeDonemi.setDenklestirmeAy(denklestirmeAy);
				fillFazlaMesaiTalepDevam(aylikPuantaj, denklestirmeDonemi);
			} catch (Exception ee) {
				logger.error(ee);
				ee.printStackTrace();
			}

		} else
			PdksUtil.addMessageWarn("İlgili döneme ait fazla mesai bulunamadı!");
		if (!(ikRole))
			departmanBolumAyni = false;
		return "";
	}

	/**
	 * @param aylikPuantajSablon
	 * @param denklestirmeDonemi
	 */
	public void fillFazlaMesaiTalepDevam(AylikPuantaj aylikPuantajSablon, DepartmanDenklestirmeDonemi denklestirmeDonemi) {
		mailGonder = Boolean.FALSE;
		mesaiOnayla = Boolean.FALSE;
		seciliDurum = false;

		departmanBolumAyni = Boolean.FALSE;
		LinkedHashMap<String, Object> lastMap = new LinkedHashMap<String, Object>();
		Long departmanId = aramaSecenekleri.getDepartmanId(), sirketId = aramaSecenekleri.getSirketId(), tesisId = aramaSecenekleri.getTesisId(), seciliEkSaha3Id = aramaSecenekleri.getEkSaha3Id();
		if (sirketId != null || seciliEkSaha3Id != null) {
			lastMap.put("yil", "" + yil);
			lastMap.put("ay", "" + ay);
			if (departmanId != null)
				lastMap.put("departmanId", "" + departmanId);
			if (sirketId != null)
				lastMap.put("sirketId", "" + sirketId);
			if (tesisId != null)
				lastMap.put("tesisId", "" + tesisId);
			if (seciliEkSaha3Id != null)
				lastMap.put("bolumId", "" + seciliEkSaha3Id);
			if (PdksUtil.hasStringValue(sicilNo))
				lastMap.put("sicilNo", sicilNo.trim());
		}

		if (basTarih != null)
			lastMap.put("tarih", PdksUtil.convertToDateString(basTarih, "yyyy-MM-dd"));
		try {
			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception e) {

		}
		Sirket sirket = null;
		if (sirketId != null && sirketId > 0) {
			HashMap fields = new HashMap();
			fields.put("id", sirketId);

			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);

		}
		departmanBolumAyni = sirket != null && sirket.isTesisDurumu() == false;

		if (sicilNo != null)
			sicilNo = ortakIslemler.getSicilNo(sicilNo.trim());
		else
			sicilNo = "";

		aylikPuantajSablon.getVardiyalar();
		setAylikPuantajDefault(aylikPuantajSablon);

		try {

			HashMap map = new HashMap();
			List<Personel> personelList = fazlaMesaiOrtakIslemler.getFazlaMesaiMudurPersonelList(aramaSecenekleri.getSirket(), tesisId != null ? "" + tesisId : null, seciliEkSaha3Id, (denklestirmeAy != null ? aylikPuantajDonem : null), getDenklestirmeDurum(), fazlaMesaiTalepDurum, session);
			List<String> perList = new ArrayList<String>();
			for (Personel personel : personelList) {
				if (PdksUtil.hasStringValue(sicilNo) == false || sicilNo.equals(personel.getPdksSicilNo()))
					perList.add(personel.getPdksSicilNo());

			}

			if (!perList.isEmpty()) {
				List<String> list1 = ortakIslemler.getYetkiTumPersonelNoList(), list2 = null;
				List<Personel> yetkiList = getFazlaMesaiSecimList("P");
				if (yetkiList != null && !yetkiList.isEmpty()) {
					list2 = new ArrayList<String>();
					for (Personel personel : yetkiList) {
						list2.add(personel.getPdksSicilNo());
					}
					list1.addAll(list2);
					if (ikRole) {
						list2.clear();
						if (authenticatedUser.getIkinciYoneticiPersonel() != null) {

							for (Personel personel : authenticatedUser.getIkinciYoneticiPersonel()) {
								list2.add(personel.getPdksSicilNo());
							}
						}
					}
				}
				map.clear();

				StringBuffer sb = new StringBuffer();
				sb.append("SELECT F.* FROM " + VardiyaGun.TABLE_NAME + " V WITH(nolock) ");
				sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P.ID=V." + VardiyaGun.COLUMN_NAME_PERSONEL);
				sb.append(" AND P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " :p");
				sb.append(" INNER JOIN " + FazlaMesaiTalep.TABLE_NAME + " F ON F." + FazlaMesaiTalep.COLUMN_NAME_VARDIYA_GUN + "=V.ID AND F.DURUM=1  ");
				if (talepOnayDurum > 0) {
					sb.append(" AND  F." + FazlaMesaiTalep.COLUMN_NAME_ONAY_DURUMU + "=:d  ");
					map.put("d", talepOnayDurum);
				}
				if (basTarih != null && (ikRole)) {
					sb.append(" WHERE V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + "=:t");
					map.put("t", basTarih);
				} else {
					sb.append(" WHERE V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ">=:b1 AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + "<=:b2 ");
					map.put("b2", aylikPuantajSablon.getSonGun());
					map.put("b1", aylikPuantajSablon.getIlkGun());

				}
				sb.append(" ORDER BY V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " DESC,F." + FazlaMesaiTalep.COLUMN_NAME_BASLANGIC_ZAMANI + " DESC,");
				sb.append(" " + Personel.COLUMN_NAME_AD + "," + Personel.COLUMN_NAME_SOYAD);
				map.put("p", perList);
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				fazlaMesaiTalepler = pdksEntityController.getObjectBySQLList(sb, map, FazlaMesaiTalep.class);

				Personel loginPersonel = authenticatedUser.getPdksPersonel();
				for (FazlaMesaiTalep ft : fazlaMesaiTalepler) {
					ft.setCheckBoxDurum(false);
					Personel personel = ft.getVardiyaGun().getPersonel();
					String personelNo = personel.getPdksSicilNo();
					boolean onaylayan = list2 != null && list2.contains(personelNo);
					if (!onaylayan) {
						Personel yonetici2 = personel.getYonetici2();
						if (yonetici2 != null)
							onaylayan = loginPersonel.getId().equals(yonetici2.getId());
					}
					ft.setOnaylayan(onaylayan);
					if (ft.isOnaylayan())
						ft.setYoneticisi(false);
					else {
						ft.setYoneticisi(list1 != null && list1.contains(personelNo));
					}

					if (ft.isHatirlatmaMail()) {
						if (ft.isYoneticisi())
							mailGonder = Boolean.TRUE;
						if (ft.isOnaylayan())
							mesaiOnayla = Boolean.TRUE;
					}
				}
				list1 = null;
				list2 = null;
			}

		} catch (InvalidStateException e) {
			InvalidValue[] invalidValues = e.getInvalidValues();
			if (invalidValues != null) {
				for (InvalidValue invalidValue : invalidValues) {
					Object object = invalidValue.getBean();
					if (object != null && object instanceof VardiyaGun) {
						VardiyaGun vardiyaGun = (VardiyaGun) object;
						PdksUtil.addMessageAvailableWarn(PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), PdksUtil.getDateFormat()) + " günü  alanı : " + invalidValue.getPropertyName() + " with message: " + invalidValue.getMessage());
					} else
						PdksUtil.addMessageAvailableWarn("Instance of bean class: " + invalidValue.getBeanClass().getSimpleName() + " has an invalid property: " + invalidValue.getPropertyName() + " with message: " + invalidValue.getMessage());
				}
			}
			logger.error(e);
			e.printStackTrace();

		} catch (Exception e3) {
			logger.error("Pdks hata in : \n");
			e3.printStackTrace();
			logger.error("Pdks hata out : " + e3.getMessage());

		} finally {

			logger.debug("Pdks hata in : \n");
		}

	}

	protected List getFazlaMesaiSecimList(String tip) {
		List list = null;
		if (denklestirmeAy == null) {
			HashMap fields = new HashMap();
			fields.put("ay", ay);
			fields.put("yil", yil);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
			setAylikPuantajDonem(denklestirmeAy);
			setDenklestirmeAyDurum(fazlaMesaiOrtakIslemler.getDurum(denklestirmeAy));
		}
		if (denklestirmeAy != null && authenticatedUser.getCalistigiSayfa() != null && authenticatedUser.getCalistigiSayfa().equals("fazlaMesaiTalep")) {
			String whereStr = " INNER JOIN FAZLA_MESAI_TALEP FT ON FT.VARDIYA_GUN_ID=V.ID AND FT.DURUM=1 ";
			Class class1 = null;
			if (tip.equals("S"))
				class1 = Sirket.class;
			else {
				String baglac = " WHERE";
				if (tip.equals("T") || tip.equals("B")) {
					if (aramaSecenekleri.getSirketId() != null) {
						whereStr += baglac + " D." + Personel.COLUMN_NAME_SIRKET + "=" + aramaSecenekleri.getSirketId();
						baglac = " AND ";
					}
					if (tip.equals("B") && aramaSecenekleri.getTesisId() != null) {
						whereStr += baglac + " D." + Personel.COLUMN_NAME_TESIS + "=" + aramaSecenekleri.getTesisId();
						baglac = " AND ";
					}
					class1 = Tanim.class;
				}

				else if (tip.equals("P")) {
					class1 = Personel.class;
					if (aramaSecenekleri.getSirketId() != null) {
						whereStr += baglac + " D." + Personel.COLUMN_NAME_SIRKET + "=" + aramaSecenekleri.getSirketId();
						baglac = " AND ";
					}
					if (tip.equals("B") && aramaSecenekleri.getTesisId() != null) {
						whereStr += baglac + " D." + Personel.COLUMN_NAME_TESIS + "=" + aramaSecenekleri.getTesisId();
						baglac = " AND ";
					}
					if (tip.equals("B") && aramaSecenekleri.getEkSaha3Id() != null) {
						whereStr += baglac + " D." + Personel.COLUMN_NAME_EK_SAHA3 + "=" + aramaSecenekleri.getEkSaha3Id();
						baglac = " AND ";
					}
				}
			}

			list = new ArrayList();
			StringBuffer sb = new StringBuffer();
			sb.append("SP_YONETICI_VARDIYA_BILGI_TIPI ");
			LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
			fields.put("yoneticiId", authenticatedUser.isIK() == false && authenticatedUser.isAdmin() == false ? authenticatedUser.getPdksPersonel().getId() : -1L);
			fields.put("donemId", denklestirmeAy.getId());
			fields.put("tip", tip);
			fields.put("kosul", whereStr);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				List newList = pdksEntityController.execSPList(fields, sb, class1);
				if (list != null && !newList.isEmpty())
					list.addAll(newList);
				newList = null;

			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
			sb = null;

		}
		return list;
	}

	/**
	 * @return
	 */
	public String mailSec() {
		for (FazlaMesaiTalep ft : fazlaMesaiTalepler) {
			if (ft.isHatirlatmaMail())
				ft.setCheckBoxDurum(seciliDurum);
		}
		return "";
	}

	/**
	 * @return
	 */
	@Transactional
	public String topluMesaiOnayla() {
		List<FazlaMesaiTalep> list = new ArrayList<FazlaMesaiTalep>();
		for (FazlaMesaiTalep ft : fazlaMesaiTalepler) {
			if (ft.isOnaylayan() && ft.isHatirlatmaMail() && ft.isCheckBoxDurum()) {
				list.add(ft);
				if (list.size() >= 20)
					break;
			}

		}
		if (!list.isEmpty()) {
			boolean flush = false;
			List<String> mesajList = new ArrayList<String>();
			for (FazlaMesaiTalep ft : list) {
				islemFazlaMesaiTalep = ft;
				ft.setOnayDurumu(FazlaMesaiTalep.ONAY_DURUM_ONAYLANDI);
				ft.setGuncellemeTarihi(new Date());
				ft.setGuncelleyenUser(authenticatedUser);
				pdksEntityController.saveOrUpdate(session, entityManager, ft);

				mesaiMudurOnayCevabi(true);
				String mesaj = ft.getOlusturanUser().getAdSoyad() + " fazla mesai talep cevabı gönderildi.";
				if (!mesajList.contains(mesaj)) {
					PdksUtil.addMessageAvailableInfo(mesaj);
					mesajList.add(mesaj);
				}

				flush = true;
			}
			if (flush)
				session.flush();
		} else
			PdksUtil.addMessageAvailableWarn("Seçili kayıt yok!");
		list = null;
		return "";
	}

	/**
	 * @return
	 */
	@Transactional
	public String topluMailGonder() {
		List<FazlaMesaiTalep> list = new ArrayList<FazlaMesaiTalep>();
		for (FazlaMesaiTalep ft : fazlaMesaiTalepler) {
			if (ft.isYoneticisi() && ft.isHatirlatmaMail() && ft.isCheckBoxDurum()) {
				list.add(ft);
				if (list.size() >= 20)
					break;
			}

		}
		if (!list.isEmpty()) {
			boolean flush = false;
			List<String> mesajList = new ArrayList<String>();
			for (FazlaMesaiTalep ft : list) {
				Personel yonetici2 = ft.getVardiyaGun().getPersonel().getYonetici2(), fmOnay = ft.getGuncelleyenUser().getPdksPersonel();
				String mesaj = null;
				if (fmOnay.isCalisiyor())
					yonetici2 = fmOnay;
				else if (yonetici2 != null) {
					if (yonetici2.getId().equals(fmOnay.getId())) {
						mesaj = yonetici2.getAdSoyad() + " çalışmıyor!";
						yonetici2 = null;
					} else {
						HashMap fields = new HashMap();
						fields.put("pdksPersonel.id", yonetici2.getId());
						if (session != null)
							fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						User guncelleyenUser = (User) pdksEntityController.getObjectByInnerObject(fields, User.class);
						if ((guncelleyenUser != null && guncelleyenUser.isDurum())) {
							ft.setGuncelleyenUser(guncelleyenUser);
							pdksEntityController.saveOrUpdate(session, entityManager, ft);
							flush = true;
						} else {
							if (guncelleyenUser != null)
								mesaj = guncelleyenUser.getUsername() + " " + yonetici2.getAdSoyad() + " aktif kullanıcı değildir!";
							else
								mesaj = ft.getVardiyaGun().getPersonel().getAdSoyad() + " aktif kullanıcısı yok!";
							yonetici2 = null;
						}
					}
				} else
					mesaj = ft.getVardiyaGun().getPersonel().getAdSoyad() + " aktif 2. yöneticisi yok!";
				if (mesaj != null) {
					if (!mesajList.contains(mesaj)) {
						PdksUtil.addMessageAvailableError(mesaj);
						mesajList.add(mesaj);
					}

				}
				if (yonetici2 != null) {
					mesaiMailHatirlatma(ft);
					String talepGirisCikisHareketEkleStr = ortakIslemler.getParameterKey("talepGirisCikisHareketEkle");
					if (talepGirisCikisHareketEkleStr.equals("1"))
						talepGirisCikisHareketEkle();
				}

				ft.setCheckBoxDurum(false);
			}
			if (flush)
				session.flush();
		} else
			PdksUtil.addMessageAvailableWarn("Seçili kayıt yok!");
		list = null;
		return "";
	}

	/**
	 * @return
	 */
	public String fazlaMesaiTalepExcel() {
		try {

			String gorevYeriAciklama = getExcelAciklama();
			ByteArrayOutputStream baosDosya = fazlaMesaiTalepExcelDevam();
			if (baosDosya != null) {
				String dosyaAdi = "AylıkFazlaMesaiTalep_" + gorevYeriAciklama + PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyyMM") + ".xlsx";
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
	 * @param puantajList
	 * @return
	 */
	private ByteArrayOutputStream fazlaMesaiTalepExcelDevam() {

		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		try {

			Sheet sheet = ExcelUtil.createSheet(wb, "Tüm Talepler", Boolean.TRUE);
			CellStyle header = ExcelUtil.getStyleHeader(wb);

			CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
			CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
			CellStyle styleOddTutar = ExcelUtil.setAlignment(ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TUTAR, wb), CellStyle.ALIGN_CENTER);
			CellStyle styleOddNumber = ExcelUtil.setAlignment(ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_NUMBER, wb), CellStyle.ALIGN_CENTER);
			CellStyle styleOddDateTime = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATETIME, wb);
			CellStyle styleOddDate = ExcelUtil.setBoldweight(ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATE, wb), HSSFFont.BOLDWEIGHT_BOLD);
			CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
			CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
			CellStyle styleEvenTutar = ExcelUtil.setAlignment(ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TUTAR, wb), CellStyle.ALIGN_CENTER);
			CellStyle styleEvenNumber = ExcelUtil.setAlignment(ExcelUtil.getStyleEven(ExcelUtil.FORMAT_NUMBER, wb), CellStyle.ALIGN_CENTER);
			CellStyle styleEvenDateTime = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATETIME, wb);
			CellStyle styleEvenDate = ExcelUtil.setBoldweight(ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATE, wb), HSSFFont.BOLDWEIGHT_BOLD);

			int row = 0;
			int col = 0;
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
			boolean tesisDurum = ortakIslemler.getListTesisDurum(fazlaMesaiTalepler);
			if (tesisDurum)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Tarihi");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Vardiya");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Mesai Başlangıç Zamanı");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Mesai Bitiş Zamanı");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Mesai Süresi (Saat)");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Onay Durum");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Onay'a Gönderen");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Onay'a Gönderme Zamanı");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Onaylayan");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Onaylama Zamanı");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Mesai Nedeni");
			boolean renk = true;
			for (Iterator iterator = fazlaMesaiTalepler.iterator(); iterator.hasNext();) {
				FazlaMesaiTalep ft = (FazlaMesaiTalep) iterator.next();
				VardiyaGun vg = ft.getVardiyaGun();
				Personel personel = vg.getPersonel();
				String sirket = "";
				CellStyle style = null, styleCenter = null, cellStyleDateTime = null, cellStyleDate = null, cellTutar = null, cellNumber = null;
				if (renk) {
					cellStyleDateTime = styleOddDateTime;
					cellStyleDate = styleOddDate;
					cellTutar = styleOddTutar;
					cellNumber = styleOddNumber;
					style = styleOdd;
					styleCenter = styleOddCenter;
				} else {
					cellStyleDateTime = styleEvenDateTime;
					cellStyleDate = styleEvenDate;
					cellTutar = styleEvenTutar;
					cellNumber = styleEvenNumber;
					style = styleEven;
					styleCenter = styleEvenCenter;
				}
				renk = !renk;
				try {
					sirket = personel.getSirket().getAd();
				} catch (Exception e1) {
					sirket = "" + ortakIslemler.sirketAciklama() + " tanımsız";
				}

				row++;
				col = 0;
				try {
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getSicilNo());
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(sirket);
					if (tesisDurum)
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
					ExcelUtil.getCell(sheet, row, col++, cellStyleDate).setCellValue(vg.getVardiyaDate());
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(vg.getVardiyaAciklama());
					ExcelUtil.getCell(sheet, row, col++, cellStyleDateTime).setCellValue(ft.getBaslangicZamani());
					ExcelUtil.getCell(sheet, row, col++, cellStyleDateTime).setCellValue(ft.getBitisZamani());
					Double sure = ft.getMesaiSuresi();
					ExcelUtil.getCell(sheet, row, col++, PdksUtil.isDoubleValueNotLong(sure) ? cellTutar : cellNumber).setCellValue(sure);
					String neden = ft.getMesaiNeden().getAciklama() + (PdksUtil.hasStringValue(ft.getAciklama()) ? "\nAçıklama : " + ft.getAciklama().trim() : "");
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(ft.getOnayDurumAciklama());
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(ft.getOlusturanUser().getAdSoyad());
					ExcelUtil.getCell(sheet, row, col++, cellStyleDateTime).setCellValue(ft.getOlusturmaTarihi());
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(ft.getGuncelleyenUser() != null ? ft.getGuncelleyenUser().getAdSoyad() : "");
					if (ft.getGuncellemeTarihi() != null)
						ExcelUtil.getCell(sheet, row, col++, cellStyleDateTime).setCellValue(ft.getGuncellemeTarihi());
					else
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(neden);
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					logger.debug(row);

				}

			}
			for (int i = 0; i < col; i++)
				sheet.autoSizeColumn(i);

			baos = new ByteArrayOutputStream();
			wb.write(baos);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			baos = null;
		}

		return baos;
	}

	private void bordroAlanKapat() {
		gerceklesenMesaiKod = Boolean.TRUE;
		devredenBakiyeKod = Boolean.TRUE;
		normalCalismaSaatKod = Boolean.FALSE;
		haftaTatilCalismaSaatKod = Boolean.FALSE;
		resmiTatilCalismaSaatKod = Boolean.FALSE;
		izinSureSaatKod = Boolean.FALSE;
		normalCalismaGunKod = Boolean.FALSE;
		haftaTatilCalismaGunKod = Boolean.FALSE;
		resmiTatilCalismaGunKod = Boolean.FALSE;
		izinSureGunKod = Boolean.FALSE;
		ucretliIzinGunKod = Boolean.FALSE;
		ucretsizIzinGunKod = Boolean.FALSE;
		hastalikIzinGunKod = Boolean.FALSE;
		normalGunKod = Boolean.FALSE;
		haftaTatilGunKod = Boolean.FALSE;
		resmiTatilGunKod = Boolean.FALSE;
		artikGunKod = Boolean.FALSE;
		bordroToplamGunKod = Boolean.FALSE;
		devredenMesaiKod = Boolean.TRUE;
		ucretiOdenenKod = Boolean.TRUE;
		if (baslikMap == null)
			baslikMap = new TreeMap<String, Boolean>();
		else
			baslikMap.clear();
	}

	/**
	 * @throws Exception
	 */
	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() throws Exception {
		bordroPuantajEkranindaGoster = false;
		aylikVardiyaPlanGiris("vardiyaPlani", true);

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
			ekSaha4Tanim = ortakIslemler.getEkSaha4(sirket, null, session);
		}
		aramaSecenekleri.setSirket(sirket);

		List<SelectItem> list = fazlaMesaiOrtakIslemler.getFazlaMesaiTesisList(aramaSecenekleri.getSirket(), denklestirmeAy != null ? aylikPuantajDonem : null, getDenklestirmeDurum(), session);
		fazlaMesaiListeGuncelle(sirket, "T", list);
		aramaSecenekleri.setTesisList(list);
		Long onceki = aramaSecenekleri.getTesisId();
		if (!list.isEmpty()) {
			if (list.size() == 1 || onceki == null)
				aramaSecenekleri.setTesisId((Long) list.get(0).getValue());
			else if (onceki != null) {
				for (SelectItem st : list) {
					if (st.getValue().equals(onceki))
						aramaSecenekleri.setTesisId(onceki);
				}
			}
		} else
			aramaSecenekleri.setTesisId(null);
		if (bolumDoldurDurum || sirket.getTesisDurum() == false) {
			if (sirket != null) {
				if (sirket.isTesisDurumu() == false)
					aramaSecenekleri.setTesisId(null);
			}
			bolumDoldur();
		}

		aylikPuantajList.clear();
		fazlaMesaiTalepler.clear();

	}

	/**
	 * @throws Exception
	 */
	public void bolumDoldur() throws Exception {
		stajerSirket = Boolean.FALSE;
		aramaSecenekleri.setGorevYeriList(null);
		if (!PdksUtil.hasStringValue(donusAdres)) {
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

			list = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, tesisId, denklestirmeAy != null ? aylikPuantajDonem : null, getDenklestirmeDurum(), session);
			fazlaMesaiListeGuncelle(sirket, "B", list);
		} else
			list = new ArrayList<SelectItem>();

		aramaSecenekleri.setGorevYeriList(list);
		Long onceki = aramaSecenekleri.getEkSaha3Id();
		if (!list.isEmpty()) {
			if (list.size() == 1 || onceki == null)
				aramaSecenekleri.setEkSaha3Id((Long) list.get(0).getValue());
			else if (onceki != null) {
				aramaSecenekleri.setEkSaha3Id(null);
				for (SelectItem st : list) {
					if (st.getValue().equals(onceki))
						aramaSecenekleri.setEkSaha3Id(onceki);

				}
			}
		} else
			aramaSecenekleri.setEkSaha3Id(null);

		fileImportKontrol();
		if (ekSaha4Tanim != null && aramaSecenekleri.getEkSaha3Id() != null)
			altBolumDoldur();
		else {
			aramaSecenekleri.setAltBolumIdList(null);
			aramaSecenekleri.setEkSaha4Id(null);
		}

	}

	public String altBolumDoldur() {
		aylikPuantajList.clear();
		List<SelectItem> altBolumIdList = null;
		seciliEkSaha4Id = aramaSecenekleri.getEkSaha4Id();
		if (ekSaha4Tanim != null) {
			Long tesisId = aramaSecenekleri.getTesisId();

			altBolumIdList = fazlaMesaiOrtakIslemler.getFazlaMesaiAltBolumList(aramaSecenekleri.getSirket(), tesisId != null ? String.valueOf(tesisId) : null, aramaSecenekleri.getEkSaha3Id(), denklestirmeAy != null ? aylikPuantajDonem : null, getDenklestirmeDurum(), session);
			boolean eski = altBolumIdList.size() == 1;
			if (eski)
				seciliEkSaha4Id = (Long) altBolumIdList.get(0).getValue();
			else if (seciliEkSaha4Id != null) {

				for (SelectItem st : altBolumIdList) {
					if (st.getValue().equals(seciliEkSaha4Id))
						eski = true;
				}
			}
			if (!eski)
				seciliEkSaha4Id = -1L;
		} else
			seciliEkSaha4Id = null;

		aramaSecenekleri.setEkSaha4Id(seciliEkSaha4Id);
		aramaSecenekleri.setAltBolumIdList(altBolumIdList);

		return "";
	}

	/**
	 * @return
	 * @throws Exception
	 */
	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisRaporAction() throws Exception {

		try {
			aylikVardiyaPlanGiris("aylikPlanRapor", false);

		} catch (Exception e) {

			logger.error(e);
			e.printStackTrace();
		}

	}

	/**
	 * @param calistigiSayfa
	 * @param planGirisiDurum
	 * @return
	 * @throws Exception
	 */
	private void aylikVardiyaPlanGiris(String calistigiSayfa, boolean planGirisiDurum) throws Exception {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		bordroAlanKapat();
		adminRoleDurum(authenticatedUser);
		loginUser = authenticatedUser;
		loginUser.setLogin(authenticatedUser != null);
		session.clear();
		aylikHareketKaydiVardiyaBul = Boolean.FALSE;
		fillEkSahaTanim();
		Calendar cal = Calendar.getInstance();
		yil = cal.get(Calendar.YEAR);
		ay = cal.get(Calendar.MONTH) + 1;
		cal.add(Calendar.MONTH, 1);
		maxYil = cal.get(Calendar.YEAR);
		HashMap fields = new HashMap();
		fields.put("ay", ay);
		fields.put("yil", yil);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
		if (denklestirmeAy == null) {
			try {
				ortakIslemler.yilAyKontrol(yil, null, session);
			} catch (Exception e) {
			}

		}
		setAylikPuantajDonem(denklestirmeAy);

		donusAdres = "";
		denklestirmeAyDurum = Boolean.FALSE;
		modelList = new ArrayList<CalismaModeliAy>();
		if (fazlaMesaiTalepler == null)
			fazlaMesaiTalepler = new ArrayList<FazlaMesaiTalep>();
		else
			fazlaMesaiTalepler.clear();

		if (personelDenklestirmeDinamikAlanList == null)
			personelDenklestirmeDinamikAlanList = new ArrayList<PersonelDenklestirmeDinamikAlan>();
		else
			personelDenklestirmeDinamikAlanList.clear();

		if (vardiyaFazlaMesaiMap == null)
			vardiyaFazlaMesaiMap = new TreeMap<Long, List<FazlaMesaiTalep>>();
		else
			vardiyaFazlaMesaiMap.clear();
		try {
			departmanBolumAyni = Boolean.FALSE;
			aramaSecenekleri = new AramaSecenekleri(authenticatedUser);
			stajerSirket = Boolean.FALSE;
			boolean ayniSayfa = authenticatedUser.getCalistigiSayfa() != null && authenticatedUser.getCalistigiSayfa().equals(calistigiSayfa);
			if (!ayniSayfa)
				authenticatedUser.setCalistigiSayfa(calistigiSayfa);
			setPlanGirisi(planGirisiDurum);
			islemYapiliyor = Boolean.FALSE;
			hastaneSuperVisor = authenticatedUser.isDirektorSuperVisor();

			aramaSecenekleri.setTesisList(null);
			aramaSecenekleri.setTesisId(null);
			aramaSecenekleri.setGorevYeriList(null);
			aramaSecenekleri.setEkSaha3Id(null);
			aramaSecenekleri.setSirketId(null);
			gorevYeri = null;

			if (aylikVardiyaOzetList != null)
				aylikVardiyaOzetList.clear();
			else
				aylikVardiyaOzetList = new ArrayList<VardiyaGun>();

			if (!authenticatedUser.isAdmin() && !authenticatedUser.isIK())
				planDepartman = authenticatedUser.getPdksPersonel().getEkSaha1() != null ? authenticatedUser.getPdksPersonel().getEkSaha1() : null;
			else
				planDepartman = null;

			aylar = PdksUtil.getAyListesi(Boolean.TRUE);
			if (aylikPuantajList != null)
				aylikPuantajList.clear();
			else
				aylikPuantajList = new ArrayList<AylikPuantaj>();

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
			AramaSecenekleri saveAramaSecenekleri = null;
			Long tesisId = null, ekSaha4Id = null;
			String doldurStr = null;
			if (planKey == null) {
				veriLastMap = ortakIslemler.getLastParameter(calistigiSayfa, session);
				if (veriLastMap != null) {
					if (!veriLastMap.isEmpty())
						doldurStr = "V";
					if (veriLastMap.containsKey("yil") && veriLastMap.containsKey("ay") && veriLastMap.containsKey("bolumId") && veriLastMap.containsKey("sirketId")) {

						yil = Integer.parseInt((String) veriLastMap.get("yil"));
						ay = Integer.parseInt((String) veriLastMap.get("ay"));
						aramaSecenekleri.setEkSaha3Id(Long.parseLong((String) veriLastMap.get("bolumId")));
						Long sId = Long.parseLong((String) veriLastMap.get("sirketId"));
						aramaSecenekleri.setSirketId(sId);
						if (veriLastMap.containsKey("departmanId") && adminRole) {
							Long departmanId = Long.parseLong((String) veriLastMap.get("departmanId"));
							aramaSecenekleri.setDepartmanId(departmanId);
						}
						if (veriLastMap.containsKey("altBolumId")) {
							ekSaha4Id = Long.parseLong((String) veriLastMap.get("altBolumId"));
							aramaSecenekleri.setEkSaha4Id(ekSaha4Id);
						}
						if (veriLastMap.containsKey("tesisId")) {
							tesisId = Long.parseLong((String) veriLastMap.get("tesisId"));
							aramaSecenekleri.setTesisId(tesisId);
						}

						if ((ikRole) && veriLastMap.containsKey("sicilNo"))
							sicilNo = (String) veriLastMap.get("sicilNo");
						if (yil > 0 && ay > 0 && aramaSecenekleri.getEkSaha3Id() != null) {
							dateStr = String.valueOf(yil * 100 + ay) + "01";
							perIdStr = "0";
							saveAramaSecenekleri = (AramaSecenekleri) aramaSecenekleri.clone();
						}

					}

				}
			}

			if (dateStr != null && perIdStr != null) {
				donusAdres = planGirisiDurum ? linkAdres : "";
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
					if (personel.getEkSaha4() != null) {
						ekSaha4Id = personel.getEkSaha4().getId();
						aramaSecenekleri.setEkSaha4Id(ekSaha4Id);
					}
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
				if (saveAramaSecenekleri != null) {
					aramaSecenekleri.setEkSaha3Id(saveAramaSecenekleri.getEkSaha3Id());
					if (saveAramaSecenekleri.getTesisId() != null)
						aramaSecenekleri.setTesisId(saveAramaSecenekleri.getTesisId());
				}

			}
			setSeciliDenklestirmeAy();
			aylariDoldur();
			if (doldurStr != null) {
				Long sirketId = aramaSecenekleri.getSirketId();

				fillSirketList();
				if (sirketId != null)
					aramaSecenekleri.setSirketId(sirketId);
				tesisDoldur(false);
				bolumDoldur();
				if (doldurStr.equals("F")) {
					if (planGirisiDurum)
						fillAylikVardiyaPlanList();
				} else
					donusAdres = "";
			} else {
				fillSirketList();
				if (aramaSecenekleri.getSirketId() != null) {
					tesisDoldur(false);
				}

			}
			if (!ayniSayfa)
				authenticatedUser.setCalistigiSayfa("");
			fileImportKontrol();

			if (donusAdres != null && donusAdres.equals(calistigiSayfa))
				donusAdres = "";

		} catch (Exception e) {
			e.printStackTrace();
		}

		kullaniciPersonel = ortakIslemler.getKullaniciPersonel(authenticatedUser);
		if (kullaniciPersonel)
			sicilNo = authenticatedUser.getPdksPersonel().getPdksSicilNo();
		authenticatedUser.setCalistigiSayfa(calistigiSayfa);

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
			HashMap<Long, VardiyaPlan> hashMap = new HashMap<Long, VardiyaPlan>();
			for (VardiyaPlan pdksVardiyaPlan : list)
				hashMap.put(pdksVardiyaPlan.getPersonel().getId(), pdksVardiyaPlan);
			DepartmanDenklestirmeDonemi denklestirmeDonemi = (DepartmanDenklestirmeDonemi) getDenklestirmeDonemi().clone();
			denklestirmeDonemi.setDenklestirmeAy(denklestirmeAy);
			denklestirmeDonemi.setDenklestirmeAyDurum(denklestirmeAyDurum);
			Calendar cal = Calendar.getInstance();
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
		boolean fileImportDurum = userHome.hasPermission("vardiyaPlani", "fileImportPlan");
		String fileImportStr = ortakIslemler.getParameterKey("fileImport");
		if (ay > 0 && aramaSecenekleri.getSirketId() != null && (fileImportDurum || fileImportStr.equals("1") || (ikRole && fileImportStr.equalsIgnoreCase("ik")))) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, 1);
			int donem = (cal.get(Calendar.YEAR) * 100) + cal.get(Calendar.MONTH) + 1;
			if (yil * 100 + ay <= donem) {
				HashMap fields = new HashMap();
				fields.put("yil", yil);
				fields.put("ay", ay);
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
				setAylikPuantajDonem(denklestirmeAy);
				setDenklestirmeAyDurum(fazlaMesaiOrtakIslemler.getDurum(denklestirmeAy));
				fileImport = denklestirmeAy != null && denklestirmeAyDurum;
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
		Calendar cal = Calendar.getInstance();
		boolean islemYapildi = Boolean.FALSE;
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
					PersonelDenklestirme personelDenklestirme = aylikPuantaj.getPersonelDenklestirmeAylik();
					if (vardiyaPlanKontrol(personelDenklestirme, vardiyaMap, pdksVardiyaPlan, personel.getSicilNo() + " " + personel.getAdSoyad() + " ", true)) {
						// PersonelDenklestirme personelDenklestirme = aylikPuantaj.getPersonelDenklestirmeAylik();
						perIdList.add(personel.getId());
						aylikPuantaj.setSecili(Boolean.FALSE);
						perNoList.add(aylikPuantaj.getPdksPersonel().getPdksSicilNo());
					} else
						hataOlustu = Boolean.TRUE;

				} catch (Exception e) {
					logger.equals(e);
					e.printStackTrace();
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
			if (!perIdList.isEmpty()) {
				HashMap fields = new HashMap();
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT S.* from " + PersonelDenklestirme.TABLE_NAME + " S WITH(nolock) ");
				sb.append(" WHERE S." + PersonelDenklestirme.COLUMN_NAME_DONEM + " =" + denklestirmeAy.getId() + " AND (S." + PersonelDenklestirme.COLUMN_NAME_DURUM + "=1 OR S." + PersonelDenklestirme.COLUMN_NAME_ONAYLANDI + "=1 )");
				sb.append(" AND S." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " :p ");
				fields.put("p", perIdList);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<PersonelDenklestirme> list = pdksEntityController.getObjectBySQLList(sb, fields, PersonelDenklestirme.class);
				boolean flush = false;
				for (PersonelDenklestirme personelDenklestirme : list) {
					savePersonelDenklestirme(personelDenklestirme);
					flush = true;

				}
				if (flush)
					try {
						session.flush();
					} catch (Exception e) {
					}

			}
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
	 * @param personelDenklestirme
	 */

	private void savePersonelDenklestirme(PersonelDenklestirme personelDenklestirme) {
		personelDenklestirme.setOnaylandi(Boolean.FALSE);
		personelDenklestirme.setGuncellemeTarihi(new Date());
		personelDenklestirme.setDevredenSure(null);
		pdksEntityController.saveOrUpdate(session, entityManager, personelDenklestirme);
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
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			TreeMap<Long, TreeMap<String, IzinTipi>> calIzinMap = new TreeMap<Long, TreeMap<String, IzinTipi>>();
			List<IzinTipi> izinList = pdksEntityController.getObjectByInnerObjectList(fields, IzinTipi.class);
			for (IzinTipi izinTipi : izinList) {
				if (izinTipi.getKisaAciklama() != null) {
					Long key = izinTipi.getDepartman() != null ? izinTipi.getDepartman().getId() : 0L;
					TreeMap<String, IzinTipi> map1 = calIzinMap.containsKey(key) ? calIzinMap.get(key) : new TreeMap<String, IzinTipi>();
					if (map1.isEmpty())
						calIzinMap.put(key, map1);
					map1.put(izinTipi.getKisaAciklama(), izinTipi);
				}

			}
			fields.clear();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT S.* from " + Vardiya.TABLE_NAME + " S WITH(nolock) ");
			sb.append(" WHERE (S." + Vardiya.COLUMN_NAME_DEPARTMAN + " IS NULL  OR S." + Vardiya.COLUMN_NAME_DEPARTMAN + "=:deptId )");
			sb.append(" AND  S." + Vardiya.COLUMN_NAME_KISA_ADI + "<>'' AND S." + Vardiya.COLUMN_NAME_DURUM + "=1  AND COALESCE(S." + Vardiya.COLUMN_NAME_ISKUR + ",0)<>1");
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
			setAylikPuantajDonem(denklestirmeAy);
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
				String donem = String.valueOf(yil * 100 + ay);
				for (VardiyaGun pdksVardiyaGun : sablonVardiyalar)
					pdksVardiyaGun.setAyinGunu(pdksVardiyaGun.getVardiyaDateStr().startsWith(donem));
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
					if (PdksUtil.hasStringValue(sicilNoUzunlukStr))
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
							if (!PdksUtil.hasStringValue(perSicilNo))
								break;

							adiSoyadi = ExcelUtil.getSheetStringValue(sheet, row, COL_ADI_SOYADI);
							if (!PdksUtil.hasStringValue(adiSoyadi))
								break;
							if (maxTextLength > 0 && perSicilNo != null && perSicilNo.trim().length() < maxTextLength)
								perSicilNo = PdksUtil.textBaslangicinaKarakterEkle(perSicilNo, '0', maxTextLength);
							perMap.put(perSicilNo, adiSoyadi);
							List<String> list = new ArrayList<String>();
							for (int i = 0; i < maxGun; i++) {
								vardiyaAdi = ExcelUtil.getSheetStringValueTry(sheet, row, COL_ADI_SOYADI + i + 2);
								String key = null;
								if (PdksUtil.hasStringValue(vardiyaAdi)) {
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
							try {
								vardiyalarMap = ortakIslemler.getIslemVardiyalar(new ArrayList<Personel>(personelMap.values()), denklestirmeDonemi.getBaslangicTarih(), denklestirmeDonemi.getBitisTarih(), Boolean.TRUE, session, Boolean.FALSE);
							} catch (Exception ev) {
								logger.error(ev);
								ev.printStackTrace();
							}
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
								Long depId = personel.getSirket().getDepartman().getId();
								TreeMap<String, IzinTipi> izinTipiMap = calIzinMap.containsKey(depId) ? calIzinMap.get(depId) : new TreeMap<String, IzinTipi>();
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
										String vardiyaKey = pdksVardiyaGun.getVardiyaKey();
										if (vardiyalarMap.containsKey(vardiyaKey))
											pdksVardiyaGun = vardiyalarMap.get(vardiyaKey);
										else
											vardiyalarMap.put(vardiyaKey, pdksVardiyaGun);
										pdksVardiyaGun.setAyinGunu(pdksVardiyaGunMaster.isAyinGunu());
										String key = PdksUtil.convertToDateString(pdksVardiyaGun.getVardiyaDate(), "yyyyMMdd");
										if (tatilGunleriMap == null)
											tatilGunleriMap = ortakIslemler.getTatilGunleri(null, aylikPuantajSablon.getIlkGun(), sonGunVardiya, session);
										pdksVardiyaGun.setAyinGunu(pdksVardiyaGun.getVardiyaDateStr().startsWith(donem));
										if (tatilGunleriMap.containsKey(key))
											pdksVardiyaGun.setTatil(tatilGunleriMap.get(key));
										puantajVardiyaGunler.add(pdksVardiyaGun);

									}
									haftalikSablonOlustur(aylikPuantajSablon, false, false, null, personel, personel.getSablon(), aylikPuantajSablonNew, vardiyalarMap);
									aylikPuantajSablonNew.setVardiyaHaftaList(aylikPuantajSablonNew.getVardiyaPlan().getVardiyaHaftaList());
									aylikPuantajSablonNew.setVardiyalar(puantajVardiyaGunler);
									int gun = 0;
									List<String> list = vardiyaListMap.get(sicilNo);
									for (VardiyaGun pdksVardiyaGun : aylikPuantajSablonNew.getVardiyalar()) {
										Date vardiyaDate = pdksVardiyaGun.getVardiyaDate();
										if (!pdksVardiyaGun.isAyinGunu())
											continue;

										String vardiyaKey = list.get(gun++);
										Vardiya pdksVardiya = null;
										if (vardiyaKey != null) {
											if (PdksUtil.setTurkishStr(vardiyaKey).equalsIgnoreCase("ISTIFA"))
												vardiyaKey = "";

											if (PdksUtil.hasStringValue(vardiyaKey)) {
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
													if (!izinTipiMap.containsKey(vardiyaKey)) {
														hataVar = Boolean.TRUE;
														PdksUtil.addMessageAvailableWarn(sicilNo + " " + perMap.get(sicilNo) + " " + PdksUtil.convertToDateString(vardiyaDate, "d/M/yyyyy") + " günü " + vardiyaKey + " vardiya bilgisi okunamadı!");
													} else {
														if (pdksVardiya == null)
															pdksVardiya = pdksVardiyaGun.getVardiya();
													}
												}
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
				if (vardiyaGunAllMap.containsKey(pdksVardiyaGun2.getVardiyaKeyStr()))
					pdksVardiyaGun2 = vardiyaGunAllMap.get(pdksVardiyaGun2.getVardiyaKeyStr());
				vardiyaGunMap.put(pdksVardiyaGun2.getVardiyaDateStr(), pdksVardiyaGun2);
				haftaVardiyaGunleri.add(pdksVardiyaGun2);
			}

			boolean calisiyor = planTarih2 >= iseBasTarih && istenAyrilmaTarih >= planTarih1;
			if (!calisiyor) {
				if (pdksVardiyaHafta.getId() != null) {
					pdksEntityController.deleteObject(session, entityManager, pdksVardiyaHafta);
					pdksVardiyaHafta.setId(null);

				}
				if (!calisiyor)
					pdksVardiyaHafta.setVardiyaGunler(haftaVardiyaGunleri);
			} else {
				if (pdksVardiyaHaftaSave && pdksVardiyaHafta.getId() == null && sablonu != null) {
					pdksVardiyaHafta.setVardiyaSablonu(sablonu);
					pdksVardiyaHafta.setOlusturanUser(authenticatedUser);
					pdksEntityController.saveOrUpdate(session, entityManager, pdksVardiyaHafta);

				}

			}
			if (calisiyor) {
				pdksVardiyaHafta.getVardiyaGunler().clear();
				for (VardiyaGun pdksVardiyaGun2 : pdksVardiyaHaftaMaster.getVardiyaGunler()) {
					String key = PdksUtil.convertToDateString(pdksVardiyaGun2.getVardiyaDate(), "yyyyMMdd");
					long ayinGunu = Long.parseLong(key);
					VardiyaGun pdksVardiyaGun = pdksVardiyaGun2;
					if (ayinGunu >= iseBasTarih && istenAyrilmaTarih >= ayinGunu)
						pdksVardiyaGun = vardiyaGunMap.containsKey(key) ? vardiyaGunMap.get(key) : (VardiyaGun) pdksVardiyaGun2.clone();

					pdksVardiyaHafta.getVardiyaGunler().add(pdksVardiyaGun);
				}
			}

			for (int i = 0; i < haftaVardiyaGunleri.size(); i++) {
				VardiyaGun pdksVardiyaGun = haftaVardiyaGunleri.get(i);
				int haftaGunu = PdksUtil.getDateField(pdksVardiyaGun.getVardiyaDate(), Calendar.DAY_OF_WEEK);
				if (pdksVardiyaGun.getId() == null) {
					if (vardiyaGunAllMap.containsKey(pdksVardiyaGun.getVardiyaKeyStr())) {
						pdksVardiyaGun = vardiyaGunAllMap.get(pdksVardiyaGun.getVardiyaKeyStr());
					} else if (sablonu != null) {
						pdksVardiyaGun.setPersonel(personelGelen);
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
					if (pdksVardiyaGun.getPersonel() != null)
						vardiyaGunMap.put(pdksVardiyaGun.getVardiyaDateStr(), pdksVardiyaGun);
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

	public PersonelDenklestirme getPersonelDenklestirme() {
		return personelDenklestirme;
	}

	public void setPersonelDenklestirme(PersonelDenklestirme personelDenklestirme) {
		this.personelDenklestirme = personelDenklestirme;
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

	public List<VardiyaGun> getAylikVardiyaOzetList() {
		return aylikVardiyaOzetList;
	}

	public void setAylikVardiyaOzetList(List<VardiyaGun> aylikVardiyaOzetList) {
		this.aylikVardiyaOzetList = aylikVardiyaOzetList;
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

	public FazlaMesaiTalep getFazlaMesaiTalep() {
		return fazlaMesaiTalep;
	}

	public void setFazlaMesaiTalep(FazlaMesaiTalep fazlaMesaiTalep) {
		this.fazlaMesaiTalep = fazlaMesaiTalep;
	}

	public List<SelectItem> getMesaiNedenTanimList() {
		return mesaiNedenTanimList;
	}

	public void setMesaiNedenTanimList(List<SelectItem> mesaiNedenTanimList) {
		this.mesaiNedenTanimList = mesaiNedenTanimList;
	}

	public List<Tanim> getMesaiIptalNedenTanimList() {
		return mesaiIptalNedenTanimList;
	}

	public void setMesaiIptalNedenTanimList(List<Tanim> mesaiIptalNedenTanimList) {
		this.mesaiIptalNedenTanimList = mesaiIptalNedenTanimList;
	}

	public FazlaMesaiTalep getIslemFazlaMesaiTalep() {
		return islemFazlaMesaiTalep;
	}

	public void setIslemFazlaMesaiTalep(FazlaMesaiTalep islemFazlaMesaiTalep) {
		this.islemFazlaMesaiTalep = islemFazlaMesaiTalep;
	}

	public boolean isFazlaMesaiTalepVar() {
		return fazlaMesaiTalepVar;
	}

	public void setFazlaMesaiTalepVar(boolean fazlaMesaiTalepVar) {
		this.fazlaMesaiTalepVar = fazlaMesaiTalepVar;
	}

	public List<FazlaMesaiTalep> getAylikFazlaMesaiTalepler() {
		return aylikFazlaMesaiTalepler;
	}

	public void setAylikFazlaMesaiTalepler(List<FazlaMesaiTalep> aylikFazlaMesaiTalepler) {
		this.aylikFazlaMesaiTalepler = aylikFazlaMesaiTalepler;
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

	public List<FazlaMesaiTalep> getFazlaMesaiTalepler() {
		return fazlaMesaiTalepler;
	}

	public void setFazlaMesaiTalepler(List<FazlaMesaiTalep> fazlaMesaiTalepler) {
		this.fazlaMesaiTalepler = fazlaMesaiTalepler;
	}

	public boolean isSeciliDurum() {
		return seciliDurum;
	}

	public void setSeciliDurum(boolean seciliDurum) {
		this.seciliDurum = seciliDurum;
	}

	public List<SelectItem> getFazlaMesaiTalepDurumList() {
		return fazlaMesaiTalepDurumList;
	}

	public void setFazlaMesaiTalepDurumList(List<SelectItem> fazlaMesaiTalepDurumList) {
		this.fazlaMesaiTalepDurumList = fazlaMesaiTalepDurumList;
	}

	public int getTalepOnayDurum() {
		return talepOnayDurum;
	}

	public void setTalepOnayDurum(int talepOnayDurum) {
		this.talepOnayDurum = talepOnayDurum;
	}

	public boolean isMesaiOnayla() {
		return mesaiOnayla;
	}

	public void setMesaiOnayla(boolean mesaiOnayla) {
		this.mesaiOnayla = mesaiOnayla;
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

	public Boolean getTopluFazlaCalismaTalep() {
		return topluFazlaCalismaTalep;
	}

	public void setTopluFazlaCalismaTalep(Boolean topluFazlaCalismaTalep) {
		this.topluFazlaCalismaTalep = topluFazlaCalismaTalep;
	}

	public List<AylikPuantaj> getAylikPuantajMesaiTalepList() {
		return aylikPuantajMesaiTalepList;
	}

	public void setAylikPuantajMesaiTalepList(List<AylikPuantaj> aylikPuantajMesaiTalepList) {
		this.aylikPuantajMesaiTalepList = aylikPuantajMesaiTalepList;
	}

	public TreeMap<Long, List<FazlaMesaiTalep>> getVardiyaFazlaMesaiMap() {
		return vardiyaFazlaMesaiMap;
	}

	public void setVardiyaFazlaMesaiMap(TreeMap<Long, List<FazlaMesaiTalep>> vardiyaFazlaMesaiMap) {
		this.vardiyaFazlaMesaiMap = vardiyaFazlaMesaiMap;
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

	public String getBolumAciklama() {
		if (bolumAciklama == null)
			fillEkSahaTanim();
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}

	public HashMap<String, List<Tanim>> getEkSahaListMap() {
		return ekSahaListMap;
	}

	public void setEkSahaListMap(HashMap<String, List<Tanim>> ekSahaListMap) {
		this.ekSahaListMap = ekSahaListMap;
	}

	public Boolean getAylikHareketKaydiVardiyaBul() {
		return aylikHareketKaydiVardiyaBul;
	}

	public void setAylikHareketKaydiVardiyaBul(Boolean aylikHareketKaydiVardiyaBul) {
		this.aylikHareketKaydiVardiyaBul = aylikHareketKaydiVardiyaBul;
	}

	public List<SelectItem> getRedNedenleri() {
		return redNedenleri;
	}

	public void setRedNedenleri(List<SelectItem> redNedenleri) {
		this.redNedenleri = redNedenleri;
	}

	public Long getRedNedeniId() {
		return redNedeniId;
	}

	public void setRedNedeniId(Long redNedeniId) {
		this.redNedeniId = redNedeniId;
	}

	public List<HareketKGS> getHareketPdksList() {
		return hareketPdksList;
	}

	public void setHareketPdksList(List<HareketKGS> hareketPdksList) {
		this.hareketPdksList = hareketPdksList;
	}

	public Boolean getManuelHareketEkle() {
		return manuelHareketEkle;
	}

	public void setManuelHareketEkle(Boolean manuelHareketEkle) {
		this.manuelHareketEkle = manuelHareketEkle;
	}

	public Long getMesaiNedenId() {
		return mesaiNedenId;
	}

	public void setMesaiNedenId(Long mesaiNedenId) {
		this.mesaiNedenId = mesaiNedenId;
	}

	public Long getSeciliEkSaha4Id() {
		return seciliEkSaha4Id;
	}

	public void setSeciliEkSaha4Id(Long seciliEkSaha4Id) {
		this.seciliEkSaha4Id = seciliEkSaha4Id;
	}

	public Tanim getEkSaha4Tanim() {
		return ekSaha4Tanim;
	}

	public void setEkSaha4Tanim(Tanim ekSaha4Tanim) {
		this.ekSaha4Tanim = ekSaha4Tanim;
	}

	public String getAltBolumAciklama() {
		return altBolumAciklama;
	}

	public void setAltBolumAciklama(String altBolumAciklama) {
		this.altBolumAciklama = altBolumAciklama;
	}

	public String getTesisAciklama() {
		return tesisAciklama;
	}

	public void setTesisAciklama(String tesisAciklama) {
		this.tesisAciklama = tesisAciklama;
	}

	public Boolean getVardiyaFazlaMesaiTalepGoster() {
		return vardiyaFazlaMesaiTalepGoster;
	}

	public void setVardiyaFazlaMesaiTalepGoster(Boolean vardiyaFazlaMesaiTalepGoster) {
		this.vardiyaFazlaMesaiTalepGoster = vardiyaFazlaMesaiTalepGoster;
	}

	public Boolean getEksikMaasGoster() {
		return eksikMaasGoster;
	}

	public void setEksikMaasGoster(Boolean eksikMaasGoster) {
		this.eksikMaasGoster = eksikMaasGoster;
	}

	public Boolean getBordroPuantajEkranindaGoster() {
		return bordroPuantajEkranindaGoster;
	}

	public void setBordroPuantajEkranindaGoster(Boolean bordroPuantajEkranindaGoster) {
		this.bordroPuantajEkranindaGoster = bordroPuantajEkranindaGoster;
	}

	public TreeMap<String, Boolean> getBaslikMap() {
		return baslikMap;
	}

	public void setBaslikMap(TreeMap<String, Boolean> baslikMap) {
		this.baslikMap = baslikMap;
	}

	public boolean isFazlaMesaiIzinRaporuDurum() {
		return fazlaMesaiIzinRaporuDurum;
	}

	public void setFazlaMesaiIzinRaporuDurum(boolean fazlaMesaiIzinRaporuDurum) {
		this.fazlaMesaiIzinRaporuDurum = fazlaMesaiIzinRaporuDurum;
	}

	public Boolean getGerceklesenMesaiKod() {
		return gerceklesenMesaiKod;
	}

	public void setGerceklesenMesaiKod(Boolean gerceklesenMesaiKod) {
		this.gerceklesenMesaiKod = gerceklesenMesaiKod;
	}

	public Boolean getDevredenBakiyeKod() {
		return devredenBakiyeKod;
	}

	public void setDevredenBakiyeKod(Boolean devredenBakiyeKod) {
		this.devredenBakiyeKod = devredenBakiyeKod;
	}

	public Boolean getNormalCalismaSaatKod() {
		return normalCalismaSaatKod;
	}

	public void setNormalCalismaSaatKod(Boolean normalCalismaSaatKod) {
		this.normalCalismaSaatKod = normalCalismaSaatKod;
	}

	public Boolean getHaftaTatilCalismaSaatKod() {
		return haftaTatilCalismaSaatKod;
	}

	public void setHaftaTatilCalismaSaatKod(Boolean haftaTatilCalismaSaatKod) {
		this.haftaTatilCalismaSaatKod = haftaTatilCalismaSaatKod;
	}

	public Boolean getResmiTatilCalismaSaatKod() {
		return resmiTatilCalismaSaatKod;
	}

	public void setResmiTatilCalismaSaatKod(Boolean resmiTatilCalismaSaatKod) {
		this.resmiTatilCalismaSaatKod = resmiTatilCalismaSaatKod;
	}

	public Boolean getIzinSureSaatKod() {
		return izinSureSaatKod;
	}

	public void setIzinSureSaatKod(Boolean izinSureSaatKod) {
		this.izinSureSaatKod = izinSureSaatKod;
	}

	public Boolean getNormalCalismaGunKod() {
		return normalCalismaGunKod;
	}

	public void setNormalCalismaGunKod(Boolean normalCalismaGunKod) {
		this.normalCalismaGunKod = normalCalismaGunKod;
	}

	public Boolean getHaftaTatilCalismaGunKod() {
		return haftaTatilCalismaGunKod;
	}

	public void setHaftaTatilCalismaGunKod(Boolean haftaTatilCalismaGunKod) {
		this.haftaTatilCalismaGunKod = haftaTatilCalismaGunKod;
	}

	public Boolean getResmiTatilCalismaGunKod() {
		return resmiTatilCalismaGunKod;
	}

	public void setResmiTatilCalismaGunKod(Boolean resmiTatilCalismaGunKod) {
		this.resmiTatilCalismaGunKod = resmiTatilCalismaGunKod;
	}

	public Boolean getIzinSureGunKod() {
		return izinSureGunKod;
	}

	public void setIzinSureGunKod(Boolean izinSureGunKod) {
		this.izinSureGunKod = izinSureGunKod;
	}

	public Boolean getUcretliIzinGunKod() {
		return ucretliIzinGunKod;
	}

	public void setUcretliIzinGunKod(Boolean ucretliIzinGunKod) {
		this.ucretliIzinGunKod = ucretliIzinGunKod;
	}

	public Boolean getUcretsizIzinGunKod() {
		return ucretsizIzinGunKod;
	}

	public void setUcretsizIzinGunKod(Boolean ucretsizIzinGunKod) {
		this.ucretsizIzinGunKod = ucretsizIzinGunKod;
	}

	public Boolean getHastalikIzinGunKod() {
		return hastalikIzinGunKod;
	}

	public void setHastalikIzinGunKod(Boolean hastalikIzinGunKod) {
		this.hastalikIzinGunKod = hastalikIzinGunKod;
	}

	public Boolean getNormalGunKod() {
		return normalGunKod;
	}

	public void setNormalGunKod(Boolean normalGunKod) {
		this.normalGunKod = normalGunKod;
	}

	public Boolean getHaftaTatilGunKod() {
		return haftaTatilGunKod;
	}

	public void setHaftaTatilGunKod(Boolean haftaTatilGunKod) {
		this.haftaTatilGunKod = haftaTatilGunKod;
	}

	public Boolean getResmiTatilGunKod() {
		return resmiTatilGunKod;
	}

	public void setResmiTatilGunKod(Boolean resmiTatilGunKod) {
		this.resmiTatilGunKod = resmiTatilGunKod;
	}

	public Boolean getBordroToplamGunKod() {
		return bordroToplamGunKod;
	}

	public void setBordroToplamGunKod(Boolean bordroToplamGunKod) {
		this.bordroToplamGunKod = bordroToplamGunKod;
	}

	public Boolean getArtikGunKod() {
		return artikGunKod;
	}

	public void setArtikGunKod(Boolean artikGunKod) {
		this.artikGunKod = artikGunKod;
	}

	/**
	 * @return the devredenMesaiKod
	 */
	public Boolean getDevredenMesaiKod() {
		return devredenMesaiKod;
	}

	/**
	 * @param devredenMesaiKod
	 *            the devredenMesaiKod to set
	 */
	public void setDevredenMesaiKod(Boolean devredenMesaiKod) {
		this.devredenMesaiKod = devredenMesaiKod;
	}

	/**
	 * @return the ucretiOdenenKod
	 */
	public Boolean getUcretiOdenenKod() {
		return ucretiOdenenKod;
	}

	/**
	 * @param ucretiOdenenKod
	 *            the ucretiOdenenKod to set
	 */
	public void setUcretiOdenenKod(Boolean ucretiOdenenKod) {
		this.ucretiOdenenKod = ucretiOdenenKod;
	}

	/**
	 * @return the yoneticiERP1Kontrol
	 */
	public Boolean getYoneticiERP1Kontrol() {
		return yoneticiERP1Kontrol;
	}

	/**
	 * @param yoneticiERP1Kontrol
	 *            the yoneticiERP1Kontrol to set
	 */
	public void setYoneticiERP1Kontrol(Boolean yoneticiERP1Kontrol) {
		this.yoneticiERP1Kontrol = yoneticiERP1Kontrol;
	}

	public void setAylikPuantajDonem(AylikPuantaj aylikPuantajDonem) {
		this.aylikPuantajDonem = aylikPuantajDonem;
	}

	public User getLoginUser() {
		return loginUser;
	}

	public void setLoginUser(User loginUser) {
		this.loginUser = loginUser;
	}
}
