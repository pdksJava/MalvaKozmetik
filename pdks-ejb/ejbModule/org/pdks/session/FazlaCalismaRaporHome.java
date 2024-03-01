package org.pdks.session;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
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
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.AylikPuantaj;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Departman;
import org.pdks.entity.DepartmanDenklestirmeDonemi;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Liste;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelDenklestirmeDinamikAlan;
import org.pdks.entity.PersonelDinamikAlan;
import org.pdks.entity.PersonelFazlaMesai;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.VardiyaHafta;
import org.pdks.entity.VardiyaSaat;
import org.pdks.security.action.UserHome;
import org.pdks.security.entity.User;

import com.google.gson.Gson;

@Name("fazlaCalismaRaporHome")
public class FazlaCalismaRaporHome extends EntityHome<DepartmanDenklestirmeDonemi> implements Serializable {

	private static final long serialVersionUID = -3864181405990033326L;
	static Logger logger = Logger.getLogger(FazlaCalismaRaporHome.class);
	public static String sayfaURL = "fazlaCalismaRapor";

	@RequestParameter
	Long personelDenklestirmeId;

	@In(required = false, create = true)
	EntityManager entityManager;

	@In(required = false, create = true)
	PdksEntityController pdksEntityController;

	@In(required = false, create = true)
	User authenticatedUser;

	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	@In(required = false, create = true)
	UserHome userHome;

	@In(required = false, create = true)
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;

	@Out(scope = ScopeType.SESSION, required = false)
	String linkAdres;

	@Out(scope = ScopeType.SESSION, required = false)
	VardiyaGun fazlaMesaiVardiyaGun;

	@In(required = true, create = true)
	Renderer renderer;

	private HashMap<String, List<Tanim>> ekSahaListMap;
	private List<DepartmanDenklestirmeDonemi> denklestirmeDonemiList;
	private List<PersonelDenklestirme> baslikDenklestirmeDonemiList;
	private List<AylikPuantaj> aylikPuantajList;
	private List<VardiyaHafta> vardiyaHaftaList;
	private HashMap<String, Double> haftaCalismaMap;
	private Sirket sirket;
	private DenklestirmeAy denklestirmeAy;
	private boolean adminRole, ikRole, talepGoster, aksamAdetGoster, aksamSaatGoster;
	private Boolean departmanBolumAyni = Boolean.FALSE, tekSirket, modelGoster = Boolean.FALSE, kullaniciPersonel = Boolean.FALSE, fazlaMesaiSayfa = Boolean.TRUE;
	private int ay, yil, maxYil, maxFazlaCalismaGun;
	private List<PersonelFazlaMesai> fazlaMesailer;
	private Drawing drawing = null;
	private ClientAnchor anchor;
	private AylikPuantaj aylikPuantajDefault;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private String sanalPersonelAciklama, bolumAciklama, raporSecim, raporAdi, sicilNo = "", excelDosyaAdi;
	private Long seciliEkSaha3Id, sirketId = null, departmanId, gorevTipiId, tesisId;
	private Tanim gorevYeri, seciliBolum, ekSaha4Tanim;
	private byte[] excelData;
	private List<SelectItem> bolumDepartmanlari, gorevYeriList, tesisList, raporList, pdksSirketList, departmanList, aylar;
	private Departman departman;
	private TreeMap<String, Tanim> fazlaMesaiMap;
	private Date basTarih, bitTarih;
	private List<VardiyaGun> vardiyaGunList, vardiyaGunPerList;
	private VardiyaGun vardiyaGun;
	private List<Tanim> denklestirmeDinamikAlanlar;
	private Double maxHaftaCalismaSaat = null, haftaCalismaSaat;
	private Session session;

	public Object getId() {
		if (personelDenklestirmeId == null) {
			return super.getId();
		}
		return personelDenklestirmeId;
	}

	@Begin(join = true)
	public void create() {
		super.create();
	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	private void adminRoleDurum() {
		adminRole = authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || authenticatedUser.isIKAdmin();
		ikRole = authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || authenticatedUser.isIK();
	}

	public String panelGuncelle() {
		listeTemizle();
		return "";
	}

	/**
	 * @param aylikPuantaj
	 * @param islemVardiyaGun
	 * @return
	 */
	public VardiyaGun vardiyaBul(AylikPuantaj aylikPuantaj, VardiyaGun islemVardiyaGun) {
		VardiyaGun gun = null;
		fazlaMesailer = null;
		String style = aylikPuantaj.getTrClass();
		try {
			String key = islemVardiyaGun.getVardiyaDateStr();
			if (aylikPuantaj.getVgMap().containsKey(key))
				gun = aylikPuantaj.getVgMap().get(key);
			if (gun == null && raporSecim.equals("maxToplamMesai"))
				gun = (VardiyaGun) islemVardiyaGun.clone();
			if (gun != null) {
				if (!gun.getDurum())
					gun.setVardiyaSaat(null);
				fazlaMesailer = gun.getFazlaMesailer();
				String str = style.equals(VardiyaGun.STYLE_CLASS_ODD) ? VardiyaGun.STYLE_CLASS_NORMAL_CALISMA : VardiyaGun.STYLE_CLASS_NORMAL_CALISMA_EVEN;
				if (!raporSecim.equals("maxToplamMesai"))
					str = gun.getAylikClassAdi(aylikPuantaj.getTrClass());

				gun.setStyle(str);
			}
		} catch (Exception localException) {
		}
		vardiyaGun = gun;
		return gun;
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() {
		fazlaMesaiSayfa = false;
		aksamAdetGoster = false;
		aksamSaatGoster = false;
		adminRoleDurum();
		boolean ayniSayfa = authenticatedUser.getCalistigiSayfa() != null && authenticatedUser.getCalistigiSayfa().equals("fazlaCalismaRapor");
		if (!ayniSayfa)
			authenticatedUser.setCalistigiSayfa("fazlaCalismaRapor");
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		yil = -1;
		ay = -1;
		fazlaMesaiVardiyaGun = null;
		if (raporList == null)
			raporList = new ArrayList();
		else {
			raporList.clear();
		}
		listeTemizle();

		if (ortakIslemler.getParameterKeyHasStringValue(("maxGunCalismaSaat")))
			raporList.add(new SelectItem("maxGunCalismaSaat", "Günlük Çalışmayı Aşanlar"));
		if (authenticatedUser.isAdmin() || ortakIslemler.getParameterKeyHasStringValue(("minGunCalismaSaat")))
			raporList.add(new SelectItem("minGunCalismaSaat", "Günlük Eksik Çalışanlar"));

		raporList.add(new SelectItem("maxHaftaTatilCalismaGun", "Hafta Tatil Çalışanlar"));
		if (ortakIslemler.getParameterKeyHasStringValue(("maxHaftaCalismaSaat")))
			raporList.add(new SelectItem("maxHaftaCalismaSaat", "Haftalık Çalışmayı Aşanlar"));
		if (ortakIslemler.getParameterKeyHasStringValue(("maxGeceCalismaSaat")))
			raporList.add(new SelectItem("maxGeceCalismaSaat", "Gece Çalışmasını Aşanlar"));
		if (ortakIslemler.getParameterKeyHasStringValue(("maxToplamMesai")))
			raporList.add(new SelectItem("maxToplamMesai", "Fazla Mesai Yapanlar"));
		if (raporList.isEmpty())
			raporSecim = null;
		else if (raporSecim == null)
			raporSecim = (String) raporList.get(0).getValue();
		Calendar cal = Calendar.getInstance();
		String maxFazlaCalismaGunStr = ortakIslemler.getParameterKey("maxFazlaCalismaGun");
		if (PdksUtil.hasStringValue(maxFazlaCalismaGunStr)) {
			try {
				maxFazlaCalismaGun = Integer.parseInt(maxFazlaCalismaGunStr);
			} catch (Exception e) {
				maxFazlaCalismaGun = -1;
			}

		}

		if (basTarih == null) {
			basTarih = PdksUtil.getDate(new Date());
			if (maxFazlaCalismaGun > 0) {

				int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
				if (dayOfWeek != Calendar.MONDAY) {
					cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
					if (cal.getTime().after(basTarih))
						cal.add(Calendar.DATE, -7);
					basTarih = PdksUtil.getDate(cal.getTime());
				}
				basTarih = ortakIslemler.tariheGunEkleCikar(cal, basTarih, -7);
			}
		}

		if (bitTarih == null)
			bitTarih = ortakIslemler.tariheGunEkleCikar(cal, basTarih, maxFazlaCalismaGun > 0 ? maxFazlaCalismaGun - 1 : 0);
		try {
			modelGoster = Boolean.FALSE;
			departmanBolumAyni = Boolean.FALSE;
			setSirket(null);
			sirketId = null;
			setTesisId(null);
			setTesisList(null);
			aylar = PdksUtil.getAyListesi(Boolean.TRUE);
			seciliEkSaha3Id = null;
			cal = Calendar.getInstance();
			ortakIslemler.gunCikar(cal, 2);
			ay = cal.get(Calendar.MONTH) + 1;
			yil = cal.get(Calendar.YEAR);
			maxYil = yil + 1;

			setInstance(new DepartmanDenklestirmeDonemi());
			// setSirket(null);

			if (authenticatedUser.isSuperVisor() || authenticatedUser.isProjeMuduru()) {
				setSirket(authenticatedUser.getPdksPersonel().getSirket());
				bolumDoldur();
			}

			Departman pdksDepartman = null;
			if (!authenticatedUser.isAdmin())
				pdksDepartman = authenticatedUser.getDepartman();

			getInstance().setDepartman(pdksDepartman);

			if ((authenticatedUser.isAdmin() || authenticatedUser.getDepartman().isAdminMi())) {
				List<Tanim> statuTanimList = null;
				HashMap fields = new HashMap();
				if (authenticatedUser.isYonetici() || authenticatedUser.isYoneticiKontratli()) {
					if (!authenticatedUser.isIKAdmin())
						fields.put("pdksSicilNo<>", authenticatedUser.getPdksPersonel().getPdksSicilNo());
					fields.put("pdksSicilNo", authenticatedUser.getYetkiTumPersonelNoList());
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<Personel> list = pdksEntityController.getObjectByInnerObjectListInLogic(fields, Personel.class);
					TreeMap<Long, Tanim> tanimMap = new TreeMap<Long, Tanim>();
					for (Personel personel : list) {
						if (personel.getEkSaha3() != null)
							tanimMap.put(personel.getEkSaha3().getId(), personel.getEkSaha3());

					}
					statuTanimList = new ArrayList<Tanim>(tanimMap.values());
					tanimMap = null;
					list = null;
				} else {
					fields.put("parentTanim.kodu", "ekSaha3");
					fields.put("parentTanim.tipi", Tanim.TIPI_PERSONEL_EK_SAHA);
					fields.put("durum", Boolean.TRUE);
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					statuTanimList = pdksEntityController.getObjectByInnerObjectList(fields, Tanim.class);
				}

				if (statuTanimList != null && !statuTanimList.isEmpty()) {

					if (statuTanimList.size() > 1)
						statuTanimList = PdksUtil.sortObjectStringAlanList(statuTanimList, "getAciklama", null);
					else {
						gorevYeri = statuTanimList.get(0);
						seciliEkSaha3Id = gorevYeri.getId();
					}

				}

			}

			HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

			String basTarihStr = (String) req.getParameter("basTarih");
			String bitTarihStr = (String) req.getParameter("bitTarih");
			String linkAdresKey = (String) req.getParameter("linkAdresKey");

			String gorevTipiIdStr = null, gorevYeriIdStr = null, sirketIdStr = null, tesisIdStr = null, departmanIdStr = null;
			LinkedHashMap<String, Object> veriLastMap = null;
			if (linkAdresKey == null) {
				veriLastMap = ortakIslemler.getLastParameter("fazlaCalismaRapor", session);
				if (veriLastMap != null && !veriLastMap.isEmpty()) {
					if (veriLastMap.containsKey("basTarih"))
						basTarihStr = (String) veriLastMap.get("basTarih");
					if (veriLastMap.containsKey("bitTarih"))
						bitTarihStr = (String) veriLastMap.get("bitTarih");
					if (veriLastMap.containsKey("sirketId"))
						sirketIdStr = (String) veriLastMap.get("sirketId");
					if (veriLastMap.containsKey("departmanId"))
						departmanIdStr = (String) veriLastMap.get("departmanId");
					if (veriLastMap.containsKey("tesisId"))
						tesisIdStr = (String) veriLastMap.get("tesisId");
					if (veriLastMap.containsKey("bolumId"))
						gorevYeriIdStr = (String) veriLastMap.get("bolumId");
					if (veriLastMap.containsKey("raporSecim")) {
						String str = (String) veriLastMap.get("raporSecim");
						for (SelectItem st : raporList) {
							if (st.getValue().equals(str))
								raporSecim = str;

						}
						raporSecim = (String) veriLastMap.get("raporSecim");
					}

					if ((authenticatedUser.isIK() || authenticatedUser.isAdmin()) && veriLastMap.containsKey("sicilNo"))
						sicilNo = (String) veriLastMap.get("sicilNo");

				}
			}
			if (linkAdresKey != null || (basTarihStr != null && bitTarihStr != null)) {
				if (linkAdresKey != null) {
					HashMap<String, String> veriMap = PdksUtil.getDecodeMapByBase64(linkAdresKey);

					if (veriMap.containsKey("basTarih"))
						basTarihStr = (String) veriMap.get("basTarih");
					if (veriMap.containsKey("bitTarih"))
						bitTarihStr = (String) veriMap.get("bitTarih");
					if (veriMap.containsKey("sirketId"))
						sirketIdStr = veriMap.get("sirketId");
					if (veriMap.containsKey("tesisId"))
						tesisIdStr = veriMap.get("tesisId");
					if (veriMap.containsKey("sicilNo"))
						sicilNo = veriMap.get("sicilNo");
					if (veriMap.containsKey("gorevTipiId"))
						gorevTipiIdStr = veriMap.get("gorevTipiId");
					if (veriMap.containsKey("gorevYeriId"))
						gorevYeriIdStr = veriMap.get("gorevYeriId");
					veriMap = null;
				} else if (veriLastMap == null || veriLastMap.isEmpty()) {
					gorevTipiIdStr = (String) req.getParameter("gorevTipiId");
					gorevYeriIdStr = (String) req.getParameter("gorevYeriId");
					tesisIdStr = (String) req.getParameter("tesisId");
					sirketIdStr = (String) req.getParameter("sirketId");
				}

				if (basTarihStr != null && bitTarihStr != null) {
					basTarih = PdksUtil.convertToJavaDate(basTarihStr, "yyyyMMdd");
					bitTarih = PdksUtil.convertToJavaDate(bitTarihStr, "yyyyMMdd");
					if (departmanIdStr != null)
						departmanId = Long.parseLong(departmanIdStr);
					if (sirketIdStr != null) {
						sirketId = Long.parseLong(sirketIdStr);
						if (sirket != null) {
							if (!sirket.getId().equals(sirketId))
								sirket = null;
						}
						HashMap parametreMap = new HashMap();

						parametreMap.put("id", sirketId);
						if (session != null)
							parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						sirket = (Sirket) pdksEntityController.getObjectByInnerObject(parametreMap, Sirket.class);
						if (sirket != null) {
							departmanId = sirket.getDepartman().getId();
							fillSirketList();
							sirketId = sirket.getId();
							tesisDoldur(false);
						}

					}
					if (sirket != null) {
						departmanId = sirket.getDepartman().getId();
						setDepartman(sirket.getDepartman());
					}
					if (gorevTipiIdStr != null)
						gorevTipiId = Long.parseLong(gorevTipiIdStr);
					if (gorevYeriIdStr != null)
						seciliEkSaha3Id = Long.parseLong(gorevYeriIdStr);

				}

			}
			linkAdres = null;
			if (!authenticatedUser.isAdmin() && !authenticatedUser.isIK() && !authenticatedUser.isYoneticiKontratli()) {
				sirket = authenticatedUser.getPdksPersonel().getSirket();
				sirketId = sirket.getId();
			}

			if (!authenticatedUser.isAdmin()) {
				if (departmanId == null && !authenticatedUser.isYoneticiKontratli())
					setDepartmanId(authenticatedUser.getDepartman().getId());

				fillSirketList();
			}
			HashMap parametreMap = new HashMap();
			if (departmanId != null)
				parametreMap.put("id", departmanId);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			setDepartman(departmanId != null ? (Departman) pdksEntityController.getObjectByInnerObject(parametreMap, Departman.class) : null);

			if (departman != null && !departman.isAdminMi()) {
				if (bolumDepartmanlari == null && departman != null)
					bolumDepartmanlari = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, null, new AylikPuantaj(basTarih, bitTarih), Boolean.TRUE, session);
			} else if (sirketId != null)
				tesisDoldur(false);
			if (tesisIdStr != null) {
				if (!tesisList.isEmpty())
					setTesisId(Long.parseLong(tesisIdStr));
				else
					tesisIdStr = null;
			}
			if (tesisIdStr != null)
				setTesisId(Long.parseLong(tesisIdStr));
			bolumDoldur();

			if (!ayniSayfa)
				authenticatedUser.setCalistigiSayfa("");

		} catch (Exception e) {
			e.printStackTrace();
		}
		kullaniciPersonel = ortakIslemler.getKullaniciPersonel(authenticatedUser);
		if (kullaniciPersonel) {
			tesisList = null;
			sicilNo = authenticatedUser.getPdksPersonel().getPdksSicilNo();
		}
		fillEkSahaTanim();
		return "";
	}

	public String tarihDegisti(String tipi) {

		listeTemizle();

		if (basTarih.getTime() <= bitTarih.getTime()) {
			Calendar cal = Calendar.getInstance();
			Date sonGun = ortakIslemler.tariheGunEkleCikar(cal, basTarih, maxFazlaCalismaGun);
			if (maxFazlaCalismaGun > 0 && sonGun.before(bitTarih))
				PdksUtil.addMessageAvailableWarn(maxFazlaCalismaGun + " günden fazla işlem yapılmaz!");
			else
				try {
					fillSirketList();
					tesisDoldur(false);
					bolumDoldur();

				} catch (Exception e) {

				}

		} else {
			PdksUtil.addMessageAvailableWarn("Tarih hatalıdır!");
		}

		return "";
	}

	public boolean veriDolu() {
		return aylikPuantajList.size() + vardiyaGunPerList.size() > 0;
	}

	public String listeTemizle() {
		raporAdi = "";
		if (aylikPuantajList == null)
			aylikPuantajList = new ArrayList<AylikPuantaj>();
		else
			aylikPuantajList.clear();

		if (vardiyaGunList == null)
			vardiyaGunList = new ArrayList<VardiyaGun>();
		else
			vardiyaGunList.clear();

		if (vardiyaGunPerList == null)
			vardiyaGunPerList = new ArrayList<VardiyaGun>();
		else
			vardiyaGunPerList.clear();
		if (vardiyaHaftaList == null)
			vardiyaHaftaList = new ArrayList<VardiyaHafta>();
		else
			vardiyaHaftaList.clear();
		if (haftaCalismaMap == null)
			haftaCalismaMap = new HashMap<String, Double>();
		else
			haftaCalismaMap.clear();

		talepGoster = false;
		return "";
	}

	public void fillDepartmanList() {
		List<SelectItem> departmanListe = fazlaMesaiOrtakIslemler.getFazlaMesaiDepartmanList(new AylikPuantaj(basTarih, bitTarih), true, session);
		if (!departmanListe.isEmpty()) {
			Long onceki = departmanId;
			if (departmanListe.size() == 1)
				departmanId = (Long) departmanListe.get(0).getValue();
			else if (onceki != null) {
				for (SelectItem st : departmanListe) {
					if (st.getValue().equals(onceki))
						departmanId = onceki;
				}
			}
		} else {
			departmanId = null;
		}
		setDepartmanList(departmanListe);
	}

	public String departmanDegisti() {
		sirketId = null;
		fillSirketList();
		if (!pdksSirketList.isEmpty()) {
			boolean bolumDoldurulmadi = true;
			if (sirketId != null || pdksSirketList.size() == 1) {
				Long tesisIdOnceki = tesisId;
				if (pdksSirketList.size() == 1)
					sirketId = (Long) pdksSirketList.get(0).getValue();
				try {
					tesisDoldur(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (tesisList.size() == 1) {
					tesisId = (Long) tesisList.get(0).getValue();
					bolumDoldur();
					bolumDoldurulmadi = false;
				} else if (tesisIdOnceki != null && !tesisList.isEmpty()) {
					for (SelectItem si : tesisList) {
						Long id = (Long) si.getValue();
						if (id.equals(tesisIdOnceki))
							tesisId = tesisIdOnceki;
					}
					if (tesisId == null)
						seciliEkSaha3Id = null;
				}
			}
			if (bolumDoldurulmadi)
				if (tesisId != null || seciliEkSaha3Id != null || (sirket != null && sirket.isTesisDurumu() == false))
					bolumDoldur();
		}
		return "";
	}

	private void fillSirketList() {
		if (adminRole)
			fillDepartmanList();
		List<SelectItem> sirketler = null;
		if (departmanId != null) {
			HashMap parametreMap = new HashMap();
			parametreMap.put("id", departmanId);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			setDepartman((Departman) pdksEntityController.getObjectByInnerObject(parametreMap, Departman.class));

		} else
			setDepartman(null);

		gorevYeriList = null;
		bolumDepartmanlari = null;
		if (gorevYeriList != null)
			gorevYeriList.clear();
		if (ikRole || authenticatedUser.isYonetici()) {
			Long depId = departman != null ? departman.getId() : null;
			sirketler = fazlaMesaiOrtakIslemler.getFazlaMesaiSirketList(depId, new AylikPuantaj(basTarih, bitTarih), true, session);
			sirket = null;
			if (!sirketler.isEmpty()) {
				Long onceki = sirketId;
				if (sirketler.size() == 1) {
					sirketId = (Long) sirketler.get(0).getValue();
				} else if (onceki != null) {
					for (SelectItem st : sirketler) {
						if (st.getValue().equals(onceki))
							sirketId = onceki;
					}
				}
				if (sirketId != null) {
					HashMap map = new HashMap();
					map.put("id", sirketId);
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					sirket = (Sirket) pdksEntityController.getObjectByInnerObject(map, Sirket.class);
				}
			}
			setPdksSirketList(sirketler);
		} else {
			setSirket(authenticatedUser.getPdksPersonel().getSirket());
		}

		listeTemizle();

	}

	@Transactional
	public String fillPersonelDenklestirmeRaporList() {
		boolean devam = true;
		aksamAdetGoster = false;
		aksamSaatGoster = false;
		Calendar cal = Calendar.getInstance();
		listeTemizle();
		if (cal.getTime().before(bitTarih)) {
			PdksUtil.addMessageAvailableWarn("Bitiş tarihi sistem tarihinden sonra olamaz!");
			devam = false;
		} else if (basTarih.getTime() <= bitTarih.getTime()) {
			Date sonGun = ortakIslemler.tariheGunEkleCikar(cal, basTarih, maxFazlaCalismaGun);
			if (!raporSecim.equals("maxToplamMesai")) {
				Long gun = Long.valueOf(PdksUtil.tarihFarki(basTarih, bitTarih).longValue() + 1L);
				if ((raporSecim.equals("maxGunCalismaSaat")) || raporSecim.equals("minGunCalismaSaat") || (raporSecim.equals("maxHaftaTatilCalismaGun")) || (raporSecim.equals("maxGeceCalismaSaat"))) {
					if (gun.longValue() > 32L) {
						PdksUtil.addMessageAvailableWarn("Bir aylık süreyi aşamazsınız!");
						devam = false;
					}
				} else if (raporSecim.equals("maxHaftaCalismaSaat")) {
					cal.setTime(basTarih);
					long mod = gun.longValue() % 7L;
					if ((gun.longValue() > 32L) || (mod != 0L)) {
						PdksUtil.addMessageAvailableWarn("Bir aylık süreyi aşamazsınız ve gün sayısı 7'nin katları olmalıdır!");
						devam = false;
					} else if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
						PdksUtil.addMessageAvailableWarn("Başlangıç günü PAZARTESİ olmalıdır!");
						devam = false;
					}
				}

			}

			if ((maxFazlaCalismaGun > 0) && (sonGun.before(bitTarih))) {
				PdksUtil.addMessageAvailableWarn(maxFazlaCalismaGun + " günden fazla işlem yapılmaz!");
				devam = false;
			}
		} else {
			PdksUtil.addMessageAvailableWarn("Tarih hatalıdır!");
			devam = false;
		}
		if (!devam) {
			return "";
		}
		linkAdres = null;
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.clear();
		try {
			DepartmanDenklestirmeDonemi denklestirmeDonemi = new DepartmanDenklestirmeDonemi();

			AylikPuantaj aylikPuantaj = new AylikPuantaj();

			aylikPuantaj.setIlkGun(basTarih);

			aylikPuantaj.setSonGun(bitTarih);
			denklestirmeDonemi.setBaslangicTarih(basTarih);
			denklestirmeDonemi.setBitisTarih(bitTarih);

			TreeMap<String, Tatil> tatilGunleriMap = ortakIslemler.getTatilGunleri(null, basTarih, bitTarih, session);
			for (Iterator iterator = aylikPuantaj.getVardiyalar().iterator(); iterator.hasNext();) {
				VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
				vardiyaGun.setTatil(tatilGunleriMap.containsKey(vardiyaGun.getVardiyaDateStr()) ? tatilGunleriMap.get(vardiyaGun.getVardiyaDateStr()) : null);
			}
			denklestirmeDonemi.setTatilGunleriMap(tatilGunleriMap);
			denklestirmeDonemi.setDenklestirmeAy(denklestirmeAy);
			fillPersonelDenklestirmeRaporDevam(aylikPuantaj, denklestirmeDonemi);
		} catch (Exception ee) {
			logger.error(ee);
			ee.printStackTrace();
		}

		if (!(authenticatedUser.isIK() || authenticatedUser.isAdmin()))
			departmanBolumAyni = false;
		return "";
	}

	/**
	 * @param aylikPuantajSablon
	 * @param denklestirmeDonemi
	 */
	public void fillPersonelDenklestirmeRaporDevam(AylikPuantaj aylikPuantajSablon, DepartmanDenklestirmeDonemi denklestirmeDonemi) throws Exception {
		session.clear();
		sirket = null;
		denklestirmeDinamikAlanlar = null;
		if (sirketId != null) {
			HashMap map = new HashMap();
			map.put("id", sirketId);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			sirket = ((Sirket) pdksEntityController.getObjectByInnerObject(map, Sirket.class));
		}
		fazlaMesaiVardiyaGun = null;
		sanalPersonelAciklama = ortakIslemler.sanalPersonelAciklama();
		departmanBolumAyni = Boolean.FALSE;
		saveLastParameter();
		departmanBolumAyni = (sirket != null) && (!sirket.isTesisDurumu());

		try {
			if (sicilNo != null)
				sicilNo = sicilNo.trim();
			listeTemizle();
			seciliBolum = null;

			if (aylikPuantajDefault == null)
				aylikPuantajDefault = new AylikPuantaj();
			aylikPuantajDefault.setIlkGun(basTarih);
			aylikPuantajDefault.setSonGun(bitTarih);
			List<Personel> personelList = fazlaMesaiOrtakIslemler.getFazlaMesaiPersonelList(sirket, tesisId != null ? String.valueOf(tesisId) : null, seciliEkSaha3Id, null, aylikPuantajDefault, true, session);
			if (PdksUtil.hasStringValue(sicilNo)) {
				for (Iterator iterator = personelList.iterator(); iterator.hasNext();) {
					Personel personel = (Personel) iterator.next();
					if (!personel.getPdksSicilNo().equals(sicilNo))
						iterator.remove();
				}
			}
			ekSaha4Tanim = null;
			HashMap<Long, TreeMap<Long, PersonelDinamikAlan>> personelDinamikAllAlanMap = new HashMap<Long, TreeMap<Long, PersonelDinamikAlan>>();
			if (!personelList.isEmpty()) {
				if (raporSecim.equals("maxGeceCalismaSaat")) {
					List<Long> personelIdler = new ArrayList<Long>();
					for (Personel personel : personelList)
						personelIdler.add(personel.getId());
					StringBuffer sb = new StringBuffer();
					String fieldName = "p";
					HashMap fields = new HashMap();
					sb.append("SELECT  DISTINCT P.* FROM " + VardiyaGun.TABLE_NAME + " G WITH(nolock) ");
					sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + "=G." + VardiyaGun.COLUMN_NAME_PERSONEL);
					sb.append("  AND G.VARDIYA_TARIHI>=P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + "   AND G.VARDIYA_TARIHI<=P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI);
					sb.append(" INNER JOIN " + Vardiya.TABLE_NAME + " V ON V." + Vardiya.COLUMN_NAME_ID + "=G." + VardiyaGun.COLUMN_NAME_VARDIYA + " AND V.AKSAM_VARDIYA=1");
					sb.append(" INNER JOIN " + VardiyaSaat.TABLE_NAME + " S ON S." + VardiyaSaat.COLUMN_NAME_ID + "=G." + VardiyaGun.COLUMN_NAME_VARDIYA_SAAT + " AND S.CALISMA_SURESI>0");
					sb.append(" WHERE  G." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ">=:t1 AND G." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + "<=:t2 ");
					sb.append(" AND  G." + VardiyaGun.COLUMN_NAME_PERSONEL + " :" + fieldName + " AND G." + VardiyaGun.COLUMN_NAME_DURUM + "=1 ");
					fields.put("t1", basTarih);
					fields.put("t2", bitTarih);
					fields.put(fieldName, personelIdler);
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					// personelList = pdksEntityController.getObjectBySQLList(sb, fields, Personel.class);
					personelList = ortakIslemler.getSQLParamList(personelIdler, sb, fieldName, fields, Personel.class, session);

				} else if (raporSecim.equals("minGunCalismaSaat")) {

					List<Long> personelIdler = new ArrayList<Long>();
					for (Personel personel : personelList)
						personelIdler.add(personel.getId());
					vardiyaGunPerList = ortakIslemler.getPersonelEksikVardiyaCalismaList(personelIdler, basTarih, bitTarih, session);
					if (!vardiyaGunPerList.isEmpty()) {
						TreeMap<Long, Personel> perMap = new TreeMap<Long, Personel>();
						for (VardiyaGun vg : vardiyaGunPerList) {
							perMap.put(vg.getPdksPersonel().getId(), vg.getPdksPersonel());
						}
						personelList = new ArrayList<Personel>(perMap.values());
						List idList = new ArrayList(perMap.keySet());
						String fieldName = "p";
						HashMap parametreMap = new HashMap();
						StringBuffer sb = new StringBuffer();
						sb.append("SELECT PD.* FROM " + PersonelDinamikAlan.TABLE_NAME + " PD  WITH(nolock) ");
						sb.append(" INNER JOIN " + Tanim.TABLE_NAME + " T  ON T." + Tanim.COLUMN_NAME_ID + "=PD." + PersonelDinamikAlan.COLUMN_NAME_ALAN + " AND T.KODU='" + PersonelDenklestirmeDinamikAlan.TIPI_DEVAMLILIK_PRIMI + "'");
						sb.append(" AND T." + Tanim.COLUMN_NAME_DURUM + "=1");
						sb.append(" WHERE PD." + PersonelDinamikAlan.COLUMN_NAME_PERSONEL + " :" + fieldName);
						if (session != null)
							parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						parametreMap.put(fieldName, idList);
						// List<PersonelDinamikAlan> pdList = pdksEntityController.getObjectBySQLList(sb, parametreMap, PersonelDinamikAlan.class);
						List<PersonelDinamikAlan> pdList = ortakIslemler.getSQLParamList(idList, sb, fieldName, parametreMap, PersonelDinamikAlan.class, session);

						if (!pdList.isEmpty()) {
							TreeMap<Long, Tanim> alanMap = new TreeMap<Long, Tanim>();
							for (PersonelDinamikAlan personelDinamikAlan : pdList) {
								Long key = personelDinamikAlan.getPdksPersonel().getId();
								Tanim alan = personelDinamikAlan.getAlan();
								TreeMap<Long, PersonelDinamikAlan> personelDinamikAlanMap = personelDinamikAllAlanMap.containsKey(key) ? personelDinamikAllAlanMap.get(key) : new TreeMap<Long, PersonelDinamikAlan>();
								if (personelDinamikAlanMap.isEmpty())
									personelDinamikAllAlanMap.put(key, personelDinamikAlanMap);
								personelDinamikAlanMap.put(alan.getId(), personelDinamikAlan);
								if (!alanMap.containsKey(alan.getId()))
									alanMap.put(alan.getId(), alan);
							}
							denklestirmeDinamikAlanlar = PdksUtil.sortTanimList(null, new ArrayList(alanMap.values()));
							alanMap = null;
						}

						perMap = null;
					} else
						personelList.clear();

				}
			}
			List<Long> personelIdler = new ArrayList<Long>();
			if (!personelList.isEmpty()) {
				ekSaha4Tanim = ortakIslemler.getEkSaha4(null, sirketId, session);

				boolean ekSaha4Var = false;
				TreeMap<Long, AylikPuantaj> puantajMap = new TreeMap<Long, AylikPuantaj>();
				for (Personel personel : personelList) {
					if (!ekSaha4Var)
						ekSaha4Var = personel.getEkSaha4() != null;
					AylikPuantaj aylikPuantaj = new AylikPuantaj();
					if (personelDinamikAllAlanMap.containsKey(personel.getId()))
						aylikPuantaj.setPersonelDinamikAlanMap(personelDinamikAllAlanMap.get(personel.getId()));
					aylikPuantaj.setSaatToplami(0.0d);
					aylikPuantaj.setAksamVardiyaSaatSayisi(0.0d);
					aylikPuantaj.setAksamVardiyaSayisi(0);
					aylikPuantaj.setPdksPersonel(personel);
					aylikPuantaj.setVgMap(new TreeMap<String, VardiyaGun>());
					aylikPuantaj.setVardiyalar(new ArrayList<VardiyaGun>());
					aylikPuantajList.add(aylikPuantaj);
					personelIdler.add(personel.getId());
					puantajMap.put(personel.getId(), aylikPuantaj);
				}
				if (!ekSaha4Var) {
					ekSaha4Tanim = null;
				}
				if (raporSecim.equals("minGunCalismaSaat") || raporSecim.equals("maxGunCalismaSaat") || raporSecim.equals("maxGeceCalismaSaat") || raporSecim.equals("maxHaftaCalismaSaat") || raporSecim.equals("maxHaftaTatilCalismaGun"))
					fazlaCalismaHazirla(personelIdler);
				else if (raporSecim.equals("maxToplamMesai"))
					maxToplamMesaiHazirla(personelIdler, puantajMap);
				if (raporSecim.equals("maxGeceCalismaSaat"))
					maxGeceCalismaSaatKontrol();
				else if (raporSecim.equals("maxGunCalismaSaat") || raporSecim.equals("maxHaftaTatilCalismaGun"))
					maxGunCalismaSaatKontrol();
				else if (raporSecim.equals("maxHaftaCalismaSaat"))
					maxHaftaCalismaSaatKontrol();
				else if (!raporSecim.equals("maxToplamMesai") && !raporSecim.equals("minGunCalismaSaat"))
					aylikPuantajList.clear();
				raporAdi = PdksUtil.getSelectItemLabel(raporSecim, raporList);
				if (PdksUtil.hasStringValue(raporAdi))
					raporAdi = raporAdi + " Raporu";
				if (!aylikPuantajList.isEmpty()) {
					HashMap<Long, List<VardiyaGun>> vMaps = new HashMap<Long, List<VardiyaGun>>();
					TreeMap<String, Liste> listeMap = new TreeMap<String, Liste>();

					for (Iterator iterator = aylikPuantajList.iterator(); iterator.hasNext();) {
						AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();

						Personel personel = aylikPuantaj.getPdksPersonel();
						vMaps.put(personel.getId(), aylikPuantaj.getVardiyalar());
						String key = (sirket.isTesisDurumu() && tesisId == null && personel.getTesis() != null ? personel.getTesis().getAciklama() + "_" : "");
						key += (personel.getYoneticisi() != null ? personel.getYoneticisi().getAdSoyad() : "");
						key += (personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
						key += "_" + personel.getAdSoyad() + "_" + personel.getPdksSicilNo();
						Liste liste = null;
						if (listeMap.containsKey(key))
							liste = listeMap.get(key);
						else {
							liste = new Liste(key, new ArrayList<AylikPuantaj>());
							listeMap.put(key, liste);
						}
						List<AylikPuantaj> list = (List<AylikPuantaj>) liste.getValue();
						list.add(aylikPuantaj);
					}
					if (!listeMap.isEmpty()) {

						aylikPuantajList.clear();
						List<Liste> list = PdksUtil.sortObjectStringAlanList(new ArrayList(listeMap.values()), "getId", null);
						for (Liste liste : list) {
							List<AylikPuantaj> fazlaMesaiList = (List<AylikPuantaj>) liste.getValue();
							aylikPuantajList.addAll(fazlaMesaiList);
						}
						boolean devam = true;
						for (AylikPuantaj aylikPuantaj : aylikPuantajList) {
							aylikPuantaj.setTrClass(devam ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
							devam = !devam;
						}
					}
					if (!raporSecim.equals("maxToplamMesai")) {
						List idList = new ArrayList(vMaps.keySet());
						String fieldName = "izinSahibi.id";
						Calendar cal = Calendar.getInstance();
						HashMap fields = new HashMap();
						fields.put("bitisZamani>=", ortakIslemler.tariheGunEkleCikar(cal, basTarih, -2));
						fields.put("baslangicZamani<=", ortakIslemler.tariheGunEkleCikar(cal, bitTarih, 1));
						fields.put(fieldName, idList);
						fields.put("izinDurumu", ortakIslemler.getAktifIzinDurumList());
						if (session != null)
							fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						// List<PersonelIzin> izinler = pdksEntityController.getObjectByInnerObjectListInLogic(fields, PersonelIzin.class);
						List<PersonelIzin> izinler = ortakIslemler.getParamList(true, idList, fieldName, fields, PersonelIzin.class, session);
						HashMap<Long, List<PersonelIzin>> izinMap = new HashMap<Long, List<PersonelIzin>>();
						for (Iterator iterator = izinler.iterator(); iterator.hasNext();) {
							PersonelIzin personelIzin = (PersonelIzin) iterator.next();
							IzinTipi izinTipi = personelIzin.getIzinTipi();
							if (izinTipi.getBakiyeIzinTipi() != null)
								iterator.remove();
							else {
								Long key = personelIzin.getIzinSahibi().getId();
								List<PersonelIzin> izins = izinMap.containsKey(key) ? izinMap.get(key) : new ArrayList<PersonelIzin>();
								if (izins.isEmpty())
									izinMap.put(key, izins);
								izins.add(personelIzin);
							}

						}
						izinler = null;
						for (Long key : izinMap.keySet()) {
							List<PersonelIzin> izinList = izinMap.get(key);
							List<VardiyaGun> vList = vMaps.get(key);
							ortakIslemler.vardiyaIzinleriGuncelle(izinList, vList);
						}
						izinMap = null;
					}
					vMaps = null;
				}

				talepGoster = false;
			}

			if (!veriDolu())
				PdksUtil.addMessageAvailableWarn("Aranan kriterlerde veri bulunmadı!");
		} catch (Exception e3) {
			ortakIslemler.loggerErrorYaz(sayfaURL, e3);
			throw new Exception(e3);
		}
	}

	/**
	 * @param personelIdler
	 * @throws Exception
	 */
	private void fazlaCalismaHazirla(List<Long> personelIdler) throws Exception {
		Double calSure = 0.0d;
		List<Long> ekCalismaList = new ArrayList<Long>();
		TreeMap<Long, ArrayList<PersonelFazlaMesai>> fmMap = new TreeMap<Long, ArrayList<PersonelFazlaMesai>>();
		TreeMap<Long, Double> aksamSaatMap = new TreeMap<Long, Double>();
		TreeMap<Long, Integer> aksamMap = new TreeMap<Long, Integer>();
		if (raporSecim.equals("maxGunCalismaSaat"))
			calSure = getMaxGunCalismaSaat();
		else if (raporSecim.equals("maxHaftaTatilCalismaGun"))
			calSure = -1.0d;
		else if (raporSecim.equals("minGunCalismaSaat")) {
			Double saat = null;
			try {
				if (ortakIslemler.getParameterKeyHasStringValue("minGunCalismaSaat"))
					saat = Double.parseDouble(ortakIslemler.getParameterKey("minGunCalismaSaat"));
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (saat == null || saat.doubleValue() < 0.0d)
				saat = 0.5d;

			for (VardiyaGun vg : vardiyaGunPerList) {
				if ((authenticatedUser.isAdmin() || saat > 1.0) && vg.getVardiyaSaat() != null && vg.getVardiyaSaat().getAksamVardiyaSaatSayisi() != 0) {
					Long key = vg.getPdksPersonel().getId();
					double value = vg.getVardiyaSaat().getAksamVardiyaSaatSayisi();
					if (value > 0) {
						int gunAdet = aksamMap.containsKey(key) ? aksamMap.get(key) : 0;
						aksamMap.put(key, gunAdet + 1);
					} else {
						double gunAdet = aksamSaatMap.containsKey(key) ? aksamSaatMap.get(key) : 0;
						aksamSaatMap.put(key, gunAdet - value);
					}

				}
				ekCalismaList.add(vg.getId());
			}
			String fieldName = "v";
			HashMap parametreMap = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT I.* FROM " + PersonelFazlaMesai.TABLE_NAME + " I  WITH(nolock) ");
			sb.append(" WHERE I." + PersonelFazlaMesai.COLUMN_NAME_VARDIYA_GUN + " :" + fieldName);
			sb.append(" AND I." + PersonelFazlaMesai.COLUMN_NAME_DURUM + "=1");
			parametreMap.put(fieldName, ekCalismaList);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			// List<PersonelFazlaMesai> list = pdksEntityController.getObjectBySQLList(sb, parametreMap, PersonelFazlaMesai.class);
			List<PersonelFazlaMesai> list = ortakIslemler.getSQLParamList(ekCalismaList, sb, fieldName, parametreMap, PersonelFazlaMesai.class, session);

			for (PersonelFazlaMesai personelFazlaMesai : list) {
				if (personelFazlaMesai.isOnaylandi()) {
					Long key = personelFazlaMesai.getVardiyaGun().getId();
					ArrayList<PersonelFazlaMesai> fazlaMesaiList = fmMap.containsKey(key) ? fmMap.get(key) : new ArrayList<PersonelFazlaMesai>();
					if (fazlaMesaiList.isEmpty())
						fmMap.put(key, fazlaMesaiList);
					fazlaMesaiList.add(personelFazlaMesai);
				}

			}

			list = null;
		}

		List<VardiyaGun> list = ortakIslemler.getVardiyaList(personelIdler, calSure, basTarih, bitTarih, session);
		if (!list.isEmpty()) {
			TreeMap<String, VardiyaGun> varMap = new TreeMap<String, VardiyaGun>();
			for (VardiyaGun gun : list) {
				varMap.put(gun.getVardiyaKeyStr(), gun);
			}
			if (!varMap.isEmpty()) {
				Calendar cal = Calendar.getInstance();
				boolean maxHaftaCalismaSaatDurum = raporSecim.equals("maxHaftaCalismaSaat");
				cal.setTime(basTarih);
				VardiyaHafta vardiyaHafta = null;
				while (bitTarih.getTime() >= cal.getTime().getTime()) {
					Date tarih = cal.getTime();
					VardiyaGun gun = new VardiyaGun(null, null, tarih);
					vardiyaGunList.add(gun);
					if (maxHaftaCalismaSaatDurum) {
						if (vardiyaHafta == null) {
							vardiyaHafta = new VardiyaHafta();
							vardiyaHafta.setBasTarih(tarih);
							vardiyaHafta.setVardiyaGunler(new ArrayList<VardiyaGun>());
							vardiyaHaftaList.add(vardiyaHafta);
							vardiyaHafta.setHafta(vardiyaHaftaList.size());
						}
						vardiyaHafta.setBitTarih(tarih);
						vardiyaHafta.getVardiyaGunler().add(gun);

					}
					String key = gun.getVardiyaDateStr();
					for (AylikPuantaj aylikPuantaj : aylikPuantajList) {
						Personel personel = aylikPuantaj.getPdksPersonel();
						String vKey = (personel != null ? personel.getSicilNo() : "") + "_" + key;
						VardiyaGun gun2 = varMap.containsKey(vKey) ? (VardiyaGun) varMap.get(vKey).clone() : new VardiyaGun(personel, null, tarih);
						if (!gun2.getDurum() || (gun2.getVardiyaSaatDB() != null && gun2.getVardiyaSaatDB().getCalismaSuresi() == 0))
							gun2.setVardiyaSaat(null);
						aylikPuantaj.getVgMap().put(key, gun2);
						gun2.setTdClass("");
						gun2.setFazlaMesailer(null);
						if (personel.getIseBaslamaTarihi().after(tarih) || personel.getSskCikisTarihi().before(tarih))
							continue;
						if (aksamMap.containsKey(personel.getId())) {
							aksamAdetGoster = true;
							aylikPuantaj.setAksamVardiyaSayisi(aksamMap.get(personel.getId()));
						}

						if (aksamSaatMap.containsKey(personel.getId())) {
							aksamSaatGoster = true;
							aylikPuantaj.setAksamVardiyaSaatSayisi(aksamSaatMap.get(personel.getId()));
						}

						if (gun2.getId() != null && ekCalismaList.isEmpty()) {
							aylikPuantaj.getVardiyalar().add(gun2);
							if (vardiyaHafta != null && gun2.getDurum() && gun2.getVardiyaSaat() != null) {
								String str = vardiyaHafta.getHafta() + "_" + personel.getId();
								Double sure = haftaCalismaMap.containsKey(str) ? haftaCalismaMap.get(str) : 0.0d;
								sure += gun2.getVardiyaSaat().getCalismaSuresi();
								haftaCalismaMap.put(str, sure);
							}
						} else {
							if (gun2.getId() != null) {
								if (ekCalismaList.contains(gun2.getId()))
									gun2.setTdClass("font-weight: bold; color: red;");
								if (fmMap.containsKey(gun2.getId())) {
									gun2.setFazlaMesailer(fmMap.get(gun2.getId()));
								}

							}

							aylikPuantaj.getVardiyalar().add(gun2);
						}

					}
					if ((maxHaftaCalismaSaatDurum) && (vardiyaHafta.getVardiyaGunler().size() == 7))
						vardiyaHafta = null;
					cal.add(Calendar.DATE, 1);
				}
				varMap = null;
			}
		}
		aksamSaatMap = null;
		fmMap = null;
		list = null;
		ekCalismaList = null;
	}

	/**
	 * @param personelIdler
	 * @param puantajMap
	 */
	private void maxToplamMesaiHazirla(List<Long> personelIdler, TreeMap<Long, AylikPuantaj> puantajMap) {
		List<VardiyaGun> list = ortakIslemler.getVardiyaList(personelIdler, null, basTarih, bitTarih, session);
		if (!list.isEmpty()) {
			Calendar cal = Calendar.getInstance();
			TreeMap gunMap = new TreeMap();
			for (VardiyaGun gun : list) {
				cal.setTime(gun.getVardiyaDate());
				cal.set(Calendar.DATE, 1);
				AylikPuantaj aylikPuantaj = (AylikPuantaj) puantajMap.get(gun.getPdksPersonel().getId());
				aylikPuantaj.getVardiyalar().add(gun);
				Date xVardiyaDate = PdksUtil.getDate(cal.getTime());
				String key = PdksUtil.convertToDateString(xVardiyaDate, "yyyyMMdd");
				if (!gunMap.containsKey(key))
					gunMap.put(key, new VardiyaGun(null, null, xVardiyaDate));
				TreeMap<String, VardiyaGun> vgMap = aylikPuantaj.getVgMap();
				VardiyaGun vardiyaGunPer = null;
				if (vgMap.containsKey(key)) {
					vardiyaGunPer = (VardiyaGun) vgMap.get(key);
				} else {
					vardiyaGunPer = new VardiyaGun(gun.getPdksPersonel(), null, xVardiyaDate);
					VardiyaSaat vardiyaSaat = new VardiyaSaat();
					vardiyaSaat.setCalismaSuresi(0.0d);
					vardiyaGunPer.setVardiyaSaat(vardiyaSaat);
					vgMap.put(key, vardiyaGunPer);
				}
				VardiyaSaat vardiyaSaat = vardiyaGunPer.getVardiyaSaat();
				VardiyaSaat vardiyaSaatDB = gun.getVardiyaSaat();
				double sure = vardiyaSaatDB.getCalismaSuresi() - vardiyaSaatDB.getNormalSure();
				vardiyaSaat.setCalismaSuresi(vardiyaSaat.getCalismaSuresi() + sure);
				aylikPuantaj.setSaatToplami(Double.valueOf(aylikPuantaj.getSaatToplami() + sure));
			}
			vardiyaGunList.addAll(new ArrayList(gunMap.values()));
			for (Iterator iterator = aylikPuantajList.iterator(); iterator.hasNext();) {
				AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
				if (aylikPuantaj.getVgMap().isEmpty())
					iterator.remove();
			}
			gunMap = null;
		}
	}

	/**
	 * @param hafta
	 * @param aylikPuantaj
	 * @return
	 */
	public Double getCalismaSuresi(int hafta, AylikPuantaj aylikPuantaj) {
		Personel personel = aylikPuantaj.getPdksPersonel();
		String key = hafta + "_" + personel.getId();
		haftaCalismaSaat = haftaCalismaMap.containsKey(key) ? (Double) haftaCalismaMap.get(key) : null;
		return haftaCalismaSaat;
	}

	private void maxGeceCalismaSaatKontrol() {
		String maxGeceCalismaSaatStr = ortakIslemler.getParameterKey("maxGeceCalismaSaat");
		TreeMap gunMap = new TreeMap();

		for (Iterator iterator = aylikPuantajList.iterator(); iterator.hasNext(); iterator.hasNext()) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
			List<VardiyaGun> vardiyaGuns = aylikPuantaj.getVardiyalar();
			VardiyaGun oncekiVardiyaGun = null;
			for (Iterator iterator2 = vardiyaGuns.iterator(); iterator2.hasNext();) {
				VardiyaGun gun = (VardiyaGun) iterator2.next();

				Vardiya vardiya = gun.getVardiya();
				if ((vardiya != null) && (vardiya.isCalisma())) {
					if (vardiya.isAksamVardiyasi()) {
						gun.setOncekiVardiyaGun(oncekiVardiyaGun);

						VardiyaSaat vardiyaSaat = gun.getVardiyaSaat();
						if (oncekiVardiyaGun != null) {
							oncekiVardiyaGun.setSonrakiVardiyaGun(gun);
							if ((vardiyaSaat != null) && (vardiyaSaat.getCalismaSuresi() > 0.0D)) {
								gunMap.put(oncekiVardiyaGun.getId(), oncekiVardiyaGun);
								gunMap.put(gun.getId(), gun);
							}
						}
					}

					if (gun.getId() != null) {
						gun.setFazlaMesailer(new ArrayList());
						oncekiVardiyaGun = gun;
					} else {
						oncekiVardiyaGun = null;
					}
				} else {
					oncekiVardiyaGun = null;
				}
			}
		}

		if (!gunMap.isEmpty()) {
			List idList = new ArrayList(gunMap.keySet());
			String fieldName = "v";
			HashMap parametreMap = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT I.* FROM " + PersonelFazlaMesai.TABLE_NAME + " I  WITH(nolock) ");
			sb.append(" WHERE I." + PersonelFazlaMesai.COLUMN_NAME_VARDIYA_GUN + " :" + fieldName);
			sb.append(" AND I." + PersonelFazlaMesai.COLUMN_NAME_DURUM + "=1");
			parametreMap.put(fieldName, idList);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			// List<PersonelFazlaMesai> list = pdksEntityController.getObjectBySQLList(sb, parametreMap, PersonelFazlaMesai.class);
			List<PersonelFazlaMesai> list = ortakIslemler.getSQLParamList(idList, sb, fieldName, parametreMap, PersonelFazlaMesai.class, session);

			if (!list.isEmpty()) {
				String patern = "yyyyMMdd" + (maxGeceCalismaSaatStr.indexOf(":") > 0 ? "HH:mm" : "HHmm");
				Date tarih2;
				for (PersonelFazlaMesai personelFazlaMesai : list) {
					if (personelFazlaMesai.isOnaylandi()) {
						VardiyaGun gun = (VardiyaGun) gunMap.get(personelFazlaMesai.getVardiyaGun().getId());
						if ((gun.getFazlaMesailer() != null) && (gun.getSonrakiVardiyaGun() != null)) {
							String str = gun.getVardiyaDateStr();
							Date tarih1 = PdksUtil.convertToJavaDate(str + maxGeceCalismaSaatStr, patern);
							tarih2 = PdksUtil.convertToJavaDate(str + "23:59:59", "yyyyMMddHH:mm:ss");
							boolean aralikta = (tarih1 != null) && (personelFazlaMesai.getBitZaman().getTime() >= tarih1.getTime()) && (tarih2 != null) && (personelFazlaMesai.getBasZaman().getTime() <= tarih2.getTime());
							if (aralikta)
								gun.getFazlaMesailer().add(personelFazlaMesai);
						}
					}
				}
				for (Iterator iterator = aylikPuantajList.iterator(); iterator.hasNext();) {
					AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
					List<VardiyaGun> vardiyaGuns = aylikPuantaj.getVardiyalar();
					boolean sil = true;
					for (VardiyaGun gun : vardiyaGuns) {
						VardiyaGun oncekiVardiyaGun = gun.getOncekiVardiyaGun();
						if ((gun.getVardiya() != null) && (gun.getVardiya().isCalisma()) && (gun.getVardiya().isAksamVardiyasi()) && (oncekiVardiyaGun != null) && (oncekiVardiyaGun.getFazlaMesailer() != null) && (!oncekiVardiyaGun.getFazlaMesailer().isEmpty())) {
							oncekiVardiyaGun.setTdClass("font-weight: bold; color: red;");
							sil = false;
						}

					}

					if (sil)
						iterator.remove();
				}
			} else {
				aylikPuantajList.clear();
			}
			list = null;
		} else {
			aylikPuantajList.clear();
		}
	}

	private void maxGunCalismaSaatKontrol() {
		boolean maxGunCalismaSaatDurum = raporSecim.equals("maxGunCalismaSaat");
		boolean maxHaftaTatilCalismaGunDurum = raporSecim.equals("maxHaftaTatilCalismaGun");
		Double maxGunCalismaSaat = getMaxGunCalismaSaat();
		if (maxGunCalismaSaat.doubleValue() > 0.0D)
			for (Iterator iterator = aylikPuantajList.iterator(); iterator.hasNext();) {
				AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
				List<VardiyaGun> vardiyaGuns = aylikPuantaj.getVardiyalar();
				boolean sil = true;
				for (VardiyaGun gun : vardiyaGuns) {
					if ((gun.getDurum()) && (gun.getVardiyaSaat() != null)) {
						double saat = gun.getVardiyaSaat().getCalismaSuresi();
						if (maxGunCalismaSaatDurum) {
							if (saat >= maxGunCalismaSaat.doubleValue()) {
								sil = false;
								if (saat > maxGunCalismaSaat.doubleValue())
									gun.setTdClass("font-weight: bold; color: red;");
							}
						} else if ((maxHaftaTatilCalismaGunDurum) && (gun.getVardiya() != null) && (gun.getVardiya().isHaftaTatil()) && (saat > 0.0D)) {
							sil = false;
							gun.setTdClass("font-weight: bold; color: red;");
						}

					}

				}

				if (sil)
					iterator.remove();
			}
		else
			aylikPuantajList.clear();
	}

	private Double getMaxGunCalismaSaat() {
		Double maxGunCalismaSaat = null;
		try {
			String str = ortakIslemler.getParameterKey("maxGunCalismaSaat");
			if (PdksUtil.hasStringValue(str))
				maxGunCalismaSaat = Double.valueOf(Double.parseDouble(str));
			if (maxGunCalismaSaat.doubleValue() < 0.0D)
				maxGunCalismaSaat = Double.valueOf(0.0D);
		} catch (Exception e) {
			maxGunCalismaSaat = Double.valueOf(0.0D);
		}
		return maxGunCalismaSaat;
	}

	private void maxHaftaCalismaSaatKontrol() {
		maxHaftaCalismaSaat = null;
		try {
			String str = ortakIslemler.getParameterKey("maxHaftaCalismaSaat");
			if (PdksUtil.hasStringValue(str))
				maxHaftaCalismaSaat = Double.valueOf(Double.parseDouble(str));
			if (maxHaftaCalismaSaat.doubleValue() < 0.0D)
				maxHaftaCalismaSaat = Double.valueOf(0.0D);
		} catch (Exception e) {
			maxHaftaCalismaSaat = Double.valueOf(0.0D);
		}
		if (maxHaftaCalismaSaat.doubleValue() > 0.0D) {
			for (Iterator iterator = aylikPuantajList.iterator(); iterator.hasNext();) {
				AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
				boolean sil = true;
				int sira = 0;
				double toplamMesai = 0.0D;
				for (VardiyaGun isGun : vardiyaGunList) {
					vardiyaBul(aylikPuantaj, isGun);
					sira++;
					if ((vardiyaGun != null) && (vardiyaGun.getVardiyaSaat() != null)) {
						double saat = vardiyaGun.getVardiyaSaat().getCalismaSuresi();
						toplamMesai += saat;
					}
					if (sira == 7) {
						if (toplamMesai >= maxHaftaCalismaSaat.doubleValue()) {
							sil = false;
							break;
						}
						sira = 0;
						toplamMesai = 0.0D;
					}
				}
				if (sil)
					iterator.remove();
				else
					for (VardiyaHafta vardiyaHafta : vardiyaHaftaList) {
						String str = vardiyaHafta.getHafta() + "_" + aylikPuantaj.getPdksPersonel().getId();
						if (haftaCalismaMap.containsKey(str)) {
							Double toplamSure = (Double) haftaCalismaMap.get(str);
							if (toplamSure.doubleValue() < maxHaftaCalismaSaat.doubleValue())
								haftaCalismaMap.remove(str);
						}
					}
			}
		} else
			aylikPuantajList.clear();
	}

	@Transactional
	private void saveLastParameter() {
		LinkedHashMap lastMap = new LinkedHashMap();
		lastMap.put("basTarih", PdksUtil.convertToDateString(basTarih, "yyyyMMdd"));
		lastMap.put("bitTarih", PdksUtil.convertToDateString(bitTarih, "yyyyMMdd"));
		if (departmanId != null)
			lastMap.put("departmanId", departmanId + "");
		if (sirketId != null)
			lastMap.put("sirketId", sirketId + "");
		if (tesisId != null)
			lastMap.put("tesisId", tesisId + "");
		if (seciliEkSaha3Id != null)
			lastMap.put("bolumId", seciliEkSaha3Id + "");
		lastMap.put("raporSecim", raporSecim);

		if (((authenticatedUser.isIK()) || (authenticatedUser.isAdmin())) && PdksUtil.hasStringValue(sicilNo))
			lastMap.put("sicilNo", sicilNo.trim());
		try {
			lastMap.put("sayfaURL", sayfaURL);
			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception localException) {
		}
	}

	public String getExcelAciklama() {
		tekSirket = (pdksSirketList != null) && (pdksSirketList.size() == 1);
		String gorevYeriAciklama = "";
		if (gorevYeri != null) {
			gorevYeriAciklama = gorevYeri.getAciklama() + "_";
		} else if ((seciliEkSaha3Id != null) || (tesisId != null)) {
			HashMap parametreMap = new HashMap();
			Tanim ekSaha3 = null;
			Tanim tesis = null;
			if (tesisId != null) {
				parametreMap.clear();
				parametreMap.put("id", tesisId);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				tesis = (Tanim) pdksEntityController.getObjectByInnerObject(parametreMap, Tanim.class);
			}

			if (seciliEkSaha3Id != null) {
				parametreMap.clear();
				parametreMap.put("id", seciliEkSaha3Id);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				ekSaha3 = (Tanim) pdksEntityController.getObjectByInnerObject(parametreMap, Tanim.class);
			}
			if (tesis != null)
				gorevYeriAciklama = tesis.getAciklama() + "_";
			if (ekSaha3 != null)
				gorevYeriAciklama = gorevYeriAciklama + ekSaha3.getAciklama() + "_";
		} else if ((sirketId != null) && (tekSirket)) {
			HashMap parametreMap = new HashMap();
			parametreMap.put("id", sirketId);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			Sirket sirket = (Sirket) pdksEntityController.getObjectByInnerObject(parametreMap, Sirket.class);
			if (sirket != null)
				gorevYeriAciklama = sirket.getAciklama() + "_";
		}
		return gorevYeriAciklama;
	}

	public String fazlaMesaiExcel() {
		try {
			String gorevYeriAciklama = getExcelAciklama();
			ByteArrayOutputStream baosDosya = fazlaMesaiExcelDevam(gorevYeriAciklama, aylikPuantajList);

			if (baosDosya != null) {
				String dosyaAdi = raporAdi + "_" + gorevYeriAciklama + PdksUtil.convertToDateString(basTarih, "yyyyMMdd") + "_" + PdksUtil.convertToDateString(bitTarih, "yyyyMMdd") + ".xlsx";
				PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi);
			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
		}

		return "";
	}

	private void fillEkSahaTanim() {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		setEkSahaListMap((HashMap) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap) sonucMap.get("ekSahaTanimMap"));
		bolumAciklama = ((String) sonucMap.get("bolumAciklama"));
	}

	/**
	 * @param gorevYeriAciklama
	 * @param list1
	 * @return
	 */
	protected ByteArrayOutputStream fazlaMesaiExcelDevam(String gorevYeriAciklama, List<AylikPuantaj> list1) {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();

		if ((list1 != null) && (!list1.isEmpty())) {
			sayfaAylikPuantajOlustur(list1, wb);
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

		return baos;
	}

	/**
	 * @param list
	 * @param wb
	 */
	private void sayfaAylikPuantajOlustur(List<AylikPuantaj> list, Workbook wb) {
		boolean maxToplamMesaiDurum = raporSecim.equals("maxToplamMesai");
		boolean maxHaftaTatilCalismaGunDurum = raporSecim.equals("maxHaftaTatilCalismaGun");
		Sheet sheet = ExcelUtil.createSheet(wb, raporAdi, Boolean.TRUE);

		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddTutar = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleOddNumber = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_NUMBER, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenTutar = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleEvenNumber = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_NUMBER, wb);
		CellStyle styleTatil = ExcelUtil.getStyleDataCenter(wb);
		ExcelUtil.setFillForegroundColor(styleTatil, 255, 153, 204);
		CellStyle styleOff = ExcelUtil.getStyleDataCenter(wb);
		ExcelUtil.setFillForegroundColor(styleOff, 13, 12, 89);
		ExcelUtil.setFontColor(styleOff, Color.WHITE);
		ExcelUtil.setFontColor(styleOff, 256, 256, 256);
		CellStyle styleIzin = ExcelUtil.getStyleDataCenter(wb);
		ExcelUtil.setFillForegroundColor(styleIzin, 146, 208, 80);
		CreationHelper factory = null;
		drawing = sheet.createDrawingPatriarch();
		factory = wb.getCreationHelper();
		anchor = factory.createClientAnchor();
		TreeMap sirketMap = new TreeMap();
		List<VardiyaGun> vardiyaGunPersonelList = new ArrayList<VardiyaGun>();
		String tekSirketTesisAdi;
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();
			// if ((aylikPuantaj.getVardiyalar() != null) && (!aylikPuantaj.getVardiyalar().isEmpty()))

			Personel personel = aylikPuantaj.getPdksPersonel();
			String tekSirketTesis = (personel.getSirket() != null ? personel.getSirket().getId() : "") + "_" + (personel.getTesis() != null ? personel.getTesis().getId() : "");
			tekSirketTesisAdi = (personel.getSirket() != null ? personel.getSirket().getAd() : "") + " " + (personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
			sirketMap.put(tekSirketTesis, tekSirketTesisAdi);
		}

		int col = 0, row = 0;
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		boolean tesisVar = sirket.isTesisDurumu();
		if (tesisVar)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
		if (seciliEkSaha3Id == null)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
		if (ekSaha4Tanim != null)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ekSaha4Tanim.getAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.yoneticiAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Görevi");
		if (denklestirmeDinamikAlanlar != null) {
			for (Tanim alan : denklestirmeDinamikAlanlar) {
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(alan.getAciklama());

			}
		}
		if (vardiyaHaftaList.isEmpty()) {
			for (VardiyaGun gun : vardiyaGunList)
				if (!maxToplamMesaiDurum)
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue(PdksUtil.convertToDateString(gun.getVardiyaDate(), "d") + "\n" + PdksUtil.convertToDateString(gun.getVardiyaDate(), "EEE"));
				else
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue(PdksUtil.convertToDateString(gun.getVardiyaDate(), "yyyy") + "\n" + PdksUtil.convertToDateString(gun.getVardiyaDate(), "MMM"));
		} else {
			CellStyle headerBlue = ExcelUtil.getStyleHeader(wb);
			ExcelUtil.setFontColor(headerBlue, Color.BLUE);
			for (VardiyaHafta vardiyaHafta : vardiyaHaftaList) {
				for (VardiyaGun gun : vardiyaHafta.getVardiyaGunler())
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue(PdksUtil.convertToDateString(gun.getVardiyaDate(), "d") + "\n" + PdksUtil.convertToDateString(gun.getVardiyaDate(), "EEE"));
				ExcelUtil.getCell(sheet, row, col++, headerBlue).setCellValue((vardiyaHaftaList.size() > 1 ? vardiyaHafta.getHafta() + ". " : "") + "Hafta\n Toplamı");
			}
		}

		if ((maxToplamMesaiDurum) && (vardiyaGunList.size() > 1))
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Toplam Fazla Çalışma");
		if (aksamAdetGoster)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Akşam Gün");
		if (aksamSaatGoster)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Akşam Saat");
		boolean renk = true;
		CellStyle styleRedOddTutar = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleRedOddNumber = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_NUMBER, wb);
		CellStyle styleRedEvenTutar = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleRedEvenNumber = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_NUMBER, wb);

		ExcelUtil.setFontColor(styleRedOddTutar, Color.RED);
		ExcelUtil.setFontColor(styleRedOddNumber, Color.RED);
		ExcelUtil.setFontColor(styleRedEvenTutar, Color.RED);
		ExcelUtil.setFontColor(styleRedEvenNumber, Color.RED);
		CellStyle styleEvenDay = ExcelUtil.getStyleDayEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddDay = ExcelUtil.getStyleDayOdd(ExcelUtil.ALIGN_CENTER, wb);

		CellStyle styleTutarEvenDay = ExcelUtil.getStyleDayEven(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleNumberEvenDay = ExcelUtil.getStyleDayEven(ExcelUtil.FORMAT_NUMBER, wb);
		CellStyle styleTutarOddDay = ExcelUtil.getStyleDayOdd(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleNumberOddDay = ExcelUtil.getStyleDayOdd(ExcelUtil.FORMAT_NUMBER, wb);

		CellStyle styleRedTutarEvenDay = ExcelUtil.getStyleDayEven(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleRedNumberEvenDay = ExcelUtil.getStyleDayEven(ExcelUtil.FORMAT_NUMBER, wb);
		CellStyle styleRedTutarOddDay = ExcelUtil.getStyleDayOdd(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleRedNumberOddDay = ExcelUtil.getStyleDayOdd(ExcelUtil.FORMAT_NUMBER, wb);
		ExcelUtil.setFontColor(styleRedTutarEvenDay, Color.RED);
		ExcelUtil.setFontColor(styleRedNumberEvenDay, Color.RED);
		ExcelUtil.setFontColor(styleRedTutarOddDay, Color.RED);
		ExcelUtil.setFontColor(styleRedNumberOddDay, Color.RED);

		for (Iterator iter = list.iterator(); iter.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();
			Personel personel = aylikPuantaj.getPdksPersonel();
			CellStyle styleCenter = null, styleGenel = null, styleDouble = null, styleNumber = null, styleRedDouble = null, styleRedNumber = null, styleDayDouble = null, styleDay = null, styleDayNumber = null, styleDayRedDouble = null, styleDayRedNumber = null;
			if (renk) {
				styleGenel = styleOdd;
				styleCenter = styleOddCenter;
				styleDouble = styleOddTutar;
				styleNumber = styleOddNumber;
				styleRedDouble = styleRedOddTutar;
				styleRedNumber = styleRedOddNumber;
				styleDayDouble = styleTutarOddDay;
				styleDayNumber = styleNumberOddDay;
				styleDayRedDouble = styleRedTutarOddDay;
				styleDayRedNumber = styleRedNumberOddDay;
				styleDay = styleOddDay;
			} else {
				styleGenel = styleEven;
				styleCenter = styleEvenCenter;
				styleDouble = styleEvenTutar;
				styleNumber = styleEvenNumber;
				styleRedDouble = styleRedEvenTutar;
				styleRedNumber = styleRedEvenNumber;
				styleDayDouble = styleTutarEvenDay;
				styleDayNumber = styleNumberEvenDay;
				styleDayRedDouble = styleRedTutarEvenDay;
				styleDayRedNumber = styleRedNumberEvenDay;
				styleDay = styleEvenDay;
			}
			renk = !renk;
			row++;
			col = 0;
			try {
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getSicilNo());
				Cell personelCell = ExcelUtil.getCell(sheet, row, col++, styleGenel);
				personelCell.setCellValue(personel.getAdSoyad());
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getSirket().getAd());
				if (tesisVar)
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
				if (seciliEkSaha3Id == null)
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
				if (ekSaha4Tanim != null)
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getEkSaha4() != null ? personel.getEkSaha4().getAciklama() : "");

				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getYoneticisi() != null ? personel.getYoneticisi().getAdSoyad() : "");
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getGorevTipi() != null ? personel.getGorevTipi().getAciklama() : "");
				if (denklestirmeDinamikAlanlar != null) {
					for (Tanim alan : denklestirmeDinamikAlanlar) {
						PersonelDinamikAlan personelDinamikAlan = aylikPuantaj.getPersonelDinamikAlan(alan.getId());
						if (personelDinamikAlan != null) {
							if (personelDinamikAlan.isCheckBox())
								ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(personelDinamikAlan.getDurumSecim()));
							else if (personelDinamikAlan.isTanim())
								ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personelDinamikAlan.getTanimDeger() != null ? personelDinamikAlan.getTanimDeger().getAciklama() : "");
							else if (personelDinamikAlan.isSayisal()) {
								if (personelDinamikAlan.getSayisalDeger() != null)
									ExcelUtil.getCell(sheet, row, col++, styleDayNumber).setCellValue(personelDinamikAlan.getSayisalDeger());
								else
									ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue("");
							}
						} else
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue("");

					}
				}
				Vardiya vardiya = null;
				if (vardiyaHaftaList.isEmpty())
					for (VardiyaGun islemVardiyaGun : vardiyaGunList) {
						vardiyaBul(aylikPuantaj, islemVardiyaGun);
						VardiyaGun gun = vardiyaGun;
						vardiya = gun.getVardiya();
						if ((gun != null) && ((vardiya != null) || (maxToplamMesaiDurum))) {
							if (vardiya != null)
								vardiyaGunPersonelList.add(gun);
							Double tutar = 0.0d;
							Cell fmtCell = null;
							boolean kirmizi = false;
							if (gun.getVardiyaSaat() != null) {
								tutar = Double.valueOf(gun.getVardiyaSaat().getCalismaSuresi());
								kirmizi = PdksUtil.hasStringValue(gun.getTdClass());
							}
							if (tutar.doubleValue() > 0.0d) {
								if (PdksUtil.isDoubleValueNotLong(tutar))
									fmtCell = ExcelUtil.getCell(sheet, row, col++, !kirmizi ? styleDayDouble : styleDayRedDouble);
								else
									fmtCell = ExcelUtil.getCell(sheet, row, col++, !kirmizi ? styleDayNumber : styleDayRedNumber);
								fmtCell.setCellValue(tutar.doubleValue());
							} else {

								String styleText = gun.getStyle();
								if (styleText != null) {
									if (styleText.equals(VardiyaGun.STYLE_CLASS_HAFTA_TATIL))
										styleDay = styleTatil;
									else if (styleText.equals(VardiyaGun.STYLE_CLASS_IZIN))
										styleDay = styleIzin;
									else if (styleText.equals(VardiyaGun.STYLE_CLASS_OFF))
										styleDay = styleOff;
								}
								fmtCell = ExcelUtil.getCell(sheet, row, col++, styleDay);
								String str = "";
								if (gun.getIzin() != null)
									str = gun.getIzin().getIzinTipi().getKisaAciklama();
								else if (vardiya != null && vardiya.isHaftaTatil())
									str = vardiya.getVardiyaAciklama();
								fmtCell.setCellValue((!maxHaftaTatilCalismaGunDurum) || (vardiya == null) || (!vardiya.isHaftaTatil()) ? str : vardiya.getVardiyaAciklama());
							}
							if (!maxToplamMesaiDurum) {
								List sb = new ArrayList();
								String str = gun.getFazlaMesaiTitle();
								if (str != null)
									sb.add(str);
								setCommentCell(wb, factory, fmtCell, sb);
							}
						} else {

							ExcelUtil.getCell(sheet, row, col++, styleOff).setCellValue(maxToplamMesaiDurum ? "" : "-");
						}
					}
				else {
					for (VardiyaHafta vardiyaHafta : vardiyaHaftaList) {
						for (VardiyaGun islemVardiyaGun : vardiyaHafta.getVardiyaGunler()) {
							vardiyaBul(aylikPuantaj, islemVardiyaGun);
							VardiyaGun gun = vardiyaGun;
							boolean yazildi = false;
							vardiya = gun.getVardiya();
							if ((gun != null) && (vardiya != null)) {
								Double tutar = 0.0d;
								Cell fmtCell = null;
								if (gun.getVardiyaSaat() != null) {
									tutar = Double.valueOf(gun.getVardiyaSaat().getCalismaSuresi());
									if (tutar.doubleValue() > 0.0d) {
										yazildi = true;
										styleDay = PdksUtil.isDoubleValueNotLong(tutar) ? styleDayDouble : styleDayNumber;
										String styleText = gun.getStyle();
										if (styleText != null) {
											if (styleText.equals(VardiyaGun.STYLE_CLASS_HAFTA_TATIL))
												styleDay = styleTatil;
											else if (styleText.equals(VardiyaGun.STYLE_CLASS_IZIN))
												styleDay = styleIzin;
											else if (styleText.equals(VardiyaGun.STYLE_CLASS_OFF))
												styleDay = styleOff;
										}

										fmtCell = ExcelUtil.getCell(sheet, row, col++, styleDay);
										fmtCell.setCellValue(tutar.doubleValue());
									}
									String str = gun.getFazlaMesaiTitle();
									if (str != null) {
										List sb = new ArrayList();
										sb.add(str);
										setCommentCell(wb, factory, fmtCell, sb);
										sb = null;
									}
								}
							}
							if (!yazildi) {
								String styleText = gun.getStyle();
								if (styleText != null) {
									if (styleText.equals(VardiyaGun.STYLE_CLASS_HAFTA_TATIL))
										styleDay = styleTatil;
									else if (styleText.equals(VardiyaGun.STYLE_CLASS_IZIN))
										styleDay = styleIzin;
									else if (styleText.equals(VardiyaGun.STYLE_CLASS_OFF))
										styleDay = styleOff;
								}
								String str = "";
								if (gun.getIzin() != null)
									str = gun.getIzin().getIzinTipi().getKisaAciklama();
								else if (vardiya != null && vardiya.isHaftaTatil())
									str = vardiya.getVardiyaAciklama();
								ExcelUtil.getCell(sheet, row, col++, styleDay).setCellValue(str);
							}
						}

						Double sure = getCalismaSuresi(vardiyaHafta.getHafta(), aylikPuantaj);
						if (sure != null) {
							if (sure > maxHaftaCalismaSaat)
								ExcelUtil.getCell(sheet, row, col++, PdksUtil.isDoubleValueNotLong(sure) ? styleRedDouble : styleRedNumber).setCellValue(sure.doubleValue());
							else
								ExcelUtil.getCell(sheet, row, col++, PdksUtil.isDoubleValueNotLong(sure) ? styleDouble : styleNumber).setCellValue(sure.doubleValue());
						}

						else {
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue("");
						}
					}
				}
				if ((maxToplamMesaiDurum) && (vardiyaGunList.size() > 1)) {
					Double tutar = Double.valueOf(aylikPuantaj.getSaatToplami());
					Cell fmtCell = ExcelUtil.getCell(sheet, row, col++, PdksUtil.isDoubleValueNotLong(tutar) ? styleRedDouble : styleRedNumber);
					fmtCell.setCellValue(tutar.doubleValue());
				}
				if (aksamAdetGoster) {
					if (aylikPuantaj.getAksamVardiyaSayisi() > 0)
						ExcelUtil.getCell(sheet, row, col++, styleNumber).setCellValue(aylikPuantaj.getAksamVardiyaSayisi());
					else
						ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue("");
				}

				if (aksamSaatGoster) {
					if (aylikPuantaj.getAksamVardiyaSaatSayisi() > 0.0d)
						ExcelUtil.getCell(sheet, row, col++, styleDayDouble).setCellValue(aylikPuantaj.getAksamVardiyaSaatSayisi());
					else
						ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue("");
				}

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				logger.error(Integer.valueOf(row));
			}

		}

		for (int i = 0; i <= col; i++)
			sheet.autoSizeColumn(i);
		if (!vardiyaGunPersonelList.isEmpty())
			ortakIslemler.vardiyaGunExcelDevam(wb, vardiyaGunPersonelList, tesisVar ? ortakIslemler.tesisAciklama() : null, bolumAciklama, ekSaha4Tanim != null ? ekSaha4Tanim.getAciklama() : null);

		vardiyaGunPersonelList = null;
	}

	/**
	 * @param wb
	 * @param factory
	 * @param cell
	 * @param titles
	 * @return
	 */
	private RichTextString setCommentCell(Workbook wb, CreationHelper factory, Cell cell, List<String> titles) {
		RichTextString rt = null;
		if ((titles != null) && (!titles.isEmpty())) {
			for (Iterator iterator = titles.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				if ((!PdksUtil.hasStringValue(string)) || (string.indexOf(":") <= 0)) {
					iterator.hasNext();
				}
			}
			int[] bas = new int[titles.size()];
			int[] uz = new int[titles.size()];
			StringBuffer sb = new StringBuffer();
			int b1 = 0;
			int b2 = 0;
			int i = 0;
			for (Iterator iterator = titles.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				String str = string.trim() + (iterator.hasNext() ? "\n" : "");
				int basYer = i > 0 ? uz[(i - 1)] : 0;
				b1 = basYer + str.indexOf(":");
				b2 = basYer + str.length();
				sb.append(str);
				bas[i] = b1;
				uz[i] = b2;
				i++;
			}
			Font fontBold = ExcelUtil.createFont(wb, (short) 9, ExcelUtil.FONT_NAME, (short) 700);
			Font font = ExcelUtil.createFont(wb, (short) 8, ExcelUtil.FONT_NAME, (short) 400);
			String title = sb.toString();
			rt = factory.createRichTextString(title);
			rt.applyFont(font);
			b1 = 0;
			for (int j = 0; j < uz.length; j++) {
				try {
					b2 = bas[j];
					if (b2 >= 0 && b1 >= 0)
						rt.applyFont(b1, b2, fontBold);
					b1 = uz[j];
				} catch (Exception e) {
					LinkedHashMap<String, Object> veriLastMap = ortakIslemler.getLastParameter("fazlaCalismaRapor", session);
					Gson gs = new Gson();
					logger.error(authenticatedUser.getAdSoyad() + " --> " + j + " " + b1 + " " + b2 + " " + e + "\n" + gs.toJson(veriLastMap));
					veriLastMap = null;
					gs = null;
				}
			}

			Comment comment = drawing.createCellComment(anchor);
			comment.setString(rt);
			cell.setCellComment(comment);
		}
		return rt;
	}

	/**
	 * @param sheet
	 * @param rowNo
	 * @param columnNo
	 * @param style
	 * @param date
	 * @return
	 */
	public Cell setCellDate(Sheet sheet, int rowNo, int columnNo, CellStyle style, Date date) {
		Cell cell = ExcelUtil.getCell(sheet, rowNo, columnNo, style);
		try {
			if (date != null)
				cell.setCellValue(date);
			else
				cell.setCellValue("");
		} catch (Exception localException) {
		}
		return cell;
	}

	/**
	 * @param bolumDoldurDurum
	 * @throws Exception
	 */
	public void tesisDoldur(boolean bolumDoldurDurum) throws Exception {
		sirket = null;
		if ((pdksSirketList == null) || (pdksSirketList.isEmpty())) {
			setTesisList(new ArrayList());
		} else {
			if (sirketId != null) {
				HashMap fields = new HashMap();
				fields.put("id", sirketId);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				sirket = ((Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class));
			}
			List<SelectItem> list = fazlaMesaiOrtakIslemler.getFazlaMesaiTesisList(sirket, new AylikPuantaj(basTarih, bitTarih), true, session);
			setTesisList(list);
			Long onceki = tesisId;
			if ((list != null) && (!list.isEmpty())) {
				if (list.size() == 1)
					tesisId = ((Long) ((SelectItem) list.get(0)).getValue());
				else if (onceki != null) {
					for (SelectItem st : list)
						if (st.getValue().equals(onceki))
							tesisId = onceki;
				}
			} else
				bolumDoldurDurum = true;
			onceki = tesisId;
			if ((tesisId != null) || ((sirket != null) && (!sirket.isTesisDurumu()))) {
				if (bolumDoldurDurum)
					bolumDoldur();
				setTesisId(onceki);
				if ((gorevYeriList != null) && (list.size() > 1)) {
					gorevYeriList.clear();
				}
			}
		}
		listeTemizle();
	}

	public String bolumDoldur() {
		fazlaMesaiVardiyaGun = null;
		linkAdres = null;
		if ((pdksSirketList == null) || (pdksSirketList.isEmpty())) {
			setGorevYeriList(new ArrayList());
		} else {
			setGorevYeriList(null);
			bolumDepartmanlari = null;

			listeTemizle();
			Sirket sirket = null;
			if (sirketId != null) {
				HashMap parametreMap = new HashMap();
				parametreMap.put("id", sirketId);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				sirket = (Sirket) pdksEntityController.getObjectByInnerObject(parametreMap, Sirket.class);
			}
			setSirket(sirket);
			if (sirket != null) {
				setDepartman(sirket.getDepartman());
				if ((departman.isAdminMi()) && (sirket.isTesisDurumu())) {
					try {
						List list = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, tesisId != null ? String.valueOf(tesisId) : null, new AylikPuantaj(basTarih, bitTarih), Boolean.TRUE, session);
						setGorevYeriList(list);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (gorevYeriList.size() == 1)
						seciliEkSaha3Id = ((Long) ((SelectItem) gorevYeriList.get(0)).getValue());
				} else {
					bolumDepartmanlari = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, null, new AylikPuantaj(basTarih, bitTarih), Boolean.TRUE, session);
					if (bolumDepartmanlari.size() == 1)
						seciliEkSaha3Id = ((Long) ((SelectItem) bolumDepartmanlari.get(0)).getValue());
					setGorevYeriList(bolumDepartmanlari);
				}
			}

		}

		listeTemizle();
		return "";
	}

	public List<DepartmanDenklestirmeDonemi> getDenklestirmeDonemiList() {
		return denklestirmeDonemiList;
	}

	public void setDenklestirmeDonemiList(List<DepartmanDenklestirmeDonemi> denklestirmeDonemiList) {
		this.denklestirmeDonemiList = denklestirmeDonemiList;
	}

	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket value) {
		sirket = value;
	}

	public List<PersonelDenklestirme> getBaslikDenklestirmeDonemiList() {
		return baslikDenklestirmeDonemiList;
	}

	public void setBaslikDenklestirmeDonemiList(List<PersonelDenklestirme> baslikDenklestirmeDonemiList) {
		this.baslikDenklestirmeDonemiList = baslikDenklestirmeDonemiList;
	}

	public String getSicilNo() {
		return sicilNo;
	}

	public void setSicilNo(String sicilNo) {
		this.sicilNo = sicilNo;
	}

	public int getYil() {
		return yil;
	}

	public void setYil(int yil) {
		this.yil = yil;
	}

	public int getAy() {
		return ay;
	}

	public void setAy(int ay) {
		this.ay = ay;
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

	public DenklestirmeAy getDenklestirmeAy() {
		return denklestirmeAy;
	}

	public void setDenklestirmeAy(DenklestirmeAy denklestirmeAy) {
		this.denklestirmeAy = denklestirmeAy;
	}

	public List<SelectItem> getGorevYeriList() {
		return gorevYeriList;
	}

	public void setGorevYeriList(List<SelectItem> value) {
		this.gorevYeriList = value;
	}

	public Long getSeciliEkSaha3Id() {
		return seciliEkSaha3Id;
	}

	public void setSeciliEkSaha3Id(Long seciliEkSaha3Id) {
		this.seciliEkSaha3Id = seciliEkSaha3Id;
	}

	public Tanim getGorevYeri() {
		return gorevYeri;
	}

	public void setGorevYeri(Tanim gorevYeri) {
		this.gorevYeri = gorevYeri;
	}

	public TreeMap<String, Tanim> getEkSahaTanimMap() {
		return ekSahaTanimMap;
	}

	public void setEkSahaTanimMap(TreeMap<String, Tanim> ekSahaTanimMap) {
		this.ekSahaTanimMap = ekSahaTanimMap;
	}

	public Long getGorevTipiId() {
		return gorevTipiId;
	}

	public void setGorevTipiId(Long gorevTipiId) {
		this.gorevTipiId = gorevTipiId;
	}

	public Long getSirketId() {
		return sirketId;
	}

	public void setSirketId(Long sirketId) {
		this.sirketId = sirketId;
	}

	public byte[] getExcelData() {
		return excelData;
	}

	public void setExcelData(byte[] excelData) {
		this.excelData = excelData;
	}

	public String getExcelDosyaAdi() {
		return excelDosyaAdi;
	}

	public void setExcelDosyaAdi(String excelDosyaAdi) {
		this.excelDosyaAdi = excelDosyaAdi;
	}

	public Long getDepartmanId() {
		return departmanId;
	}

	public void setDepartmanId(Long departmanId) {
		this.departmanId = departmanId;
	}

	public List<SelectItem> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<SelectItem> departmanList) {
		this.departmanList = departmanList;
	}

	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman value) {
		this.departman = value;
	}

	public Tanim getSeciliBolum() {
		return seciliBolum;
	}

	public void setSeciliBolum(Tanim seciliBolum) {
		this.seciliBolum = seciliBolum;
	}

	public List<SelectItem> getBolumDepartmanlari() {
		return bolumDepartmanlari;
	}

	public void setBolumDepartmanlari(List<SelectItem> bolumDepartmanlari) {
		this.bolumDepartmanlari = bolumDepartmanlari;
	}

	public List<SelectItem> getPdksSirketList() {
		return pdksSirketList;
	}

	public void setPdksSirketList(List<SelectItem> value) {
		this.pdksSirketList = value;
	}

	public List<SelectItem> getTesisList() {
		return tesisList;
	}

	public void setTesisList(List<SelectItem> tesisList) {
		this.tesisList = tesisList;
	}

	public Long getTesisId() {
		return tesisId;
	}

	public void setTesisId(Long tesisId) {
		this.tesisId = tesisId;
	}

	public Boolean getDepartmanBolumAyni() {
		return departmanBolumAyni;
	}

	public void setDepartmanBolumAyni(Boolean departmanBolumAyni) {
		this.departmanBolumAyni = departmanBolumAyni;
	}

	public boolean isTekSirket() {
		return tekSirket;
	}

	public void setTekSirket(boolean tekSirket) {
		this.tekSirket = tekSirket;
	}

	public Boolean getModelGoster() {
		return modelGoster;
	}

	public void setModelGoster(Boolean modelGoster) {
		this.modelGoster = modelGoster;
	}

	public TreeMap<String, Tanim> getFazlaMesaiMap() {
		return fazlaMesaiMap;
	}

	public void setFazlaMesaiMap(TreeMap<String, Tanim> fazlaMesaiMap) {
		this.fazlaMesaiMap = fazlaMesaiMap;
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

	public Boolean getFazlaMesaiSayfa() {
		return fazlaMesaiSayfa;
	}

	public void setFazlaMesaiSayfa(Boolean fazlaMesaiSayfa) {
		this.fazlaMesaiSayfa = fazlaMesaiSayfa;
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

	public HashMap<String, List<Tanim>> getEkSahaListMap() {
		return ekSahaListMap;
	}

	public void setEkSahaListMap(HashMap<String, List<Tanim>> ekSahaListMap) {
		this.ekSahaListMap = ekSahaListMap;
	}

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public boolean isTalepGoster() {
		return talepGoster;
	}

	public void setTalepGoster(boolean talepGoster) {
		this.talepGoster = talepGoster;
	}

	public List<SelectItem> getRaporList() {
		return raporList;
	}

	public void setRaporList(List<SelectItem> raporList) {
		this.raporList = raporList;
	}

	public String getRaporSecim() {
		return raporSecim;
	}

	public void setRaporSecim(String raporSecim) {
		this.raporSecim = raporSecim;
	}

	public List<AylikPuantaj> getAylikPuantajList() {
		return aylikPuantajList;
	}

	public void setAylikPuantajList(List<AylikPuantaj> aylikPuantajList) {
		this.aylikPuantajList = aylikPuantajList;
	}

	public int getMaxFazlaCalismaGun() {
		return maxFazlaCalismaGun;
	}

	public void setMaxFazlaCalismaGun(int maxFazlaCalismaGun) {
		this.maxFazlaCalismaGun = maxFazlaCalismaGun;
	}

	public List<VardiyaGun> getVardiyaGunList() {
		return vardiyaGunList;
	}

	public void setVardiyaGunList(List<VardiyaGun> vardiyaGunList) {
		this.vardiyaGunList = vardiyaGunList;
	}

	public VardiyaGun getVardiyaGun() {
		return vardiyaGun;
	}

	public void setVardiyaGun(VardiyaGun vardiyaGun) {
		this.vardiyaGun = vardiyaGun;
	}

	public String getRaporAdi() {
		return raporAdi;
	}

	public void setRaporAdi(String raporAdi) {
		this.raporAdi = raporAdi;
	}

	public Tanim getEkSaha4Tanim() {
		return ekSaha4Tanim;
	}

	public void setEkSaha4Tanim(Tanim ekSaha4Tanim) {
		this.ekSaha4Tanim = ekSaha4Tanim;
	}

	public List<VardiyaGun> getVardiyaGunPerList() {
		return vardiyaGunPerList;
	}

	public void setVardiyaGunPerList(List<VardiyaGun> vardiyaGunPerList) {
		this.vardiyaGunPerList = vardiyaGunPerList;
	}

	public List<VardiyaHafta> getVardiyaHaftaList() {
		return vardiyaHaftaList;
	}

	public void setVardiyaHaftaList(List<VardiyaHafta> vardiyaHaftaList) {
		this.vardiyaHaftaList = vardiyaHaftaList;
	}

	public HashMap<String, Double> getHaftaCalismaMap() {
		return haftaCalismaMap;
	}

	public void setHaftaCalismaMap(HashMap<String, Double> haftaCalismaMap) {
		this.haftaCalismaMap = haftaCalismaMap;
	}

	public List<PersonelFazlaMesai> getFazlaMesailer() {
		return fazlaMesailer;
	}

	public void setFazlaMesailer(List<PersonelFazlaMesai> fazlaMesailer) {
		this.fazlaMesailer = fazlaMesailer;
	}

	public Double getMaxHaftaCalismaSaat() {
		return maxHaftaCalismaSaat;
	}

	public void setMaxHaftaCalismaSaat(Double maxHaftaCalismaSaat) {
		this.maxHaftaCalismaSaat = maxHaftaCalismaSaat;
	}

	public Double getHaftaCalismaSaat() {
		return haftaCalismaSaat;
	}

	public void setHaftaCalismaSaat(Double haftaCalismaSaat) {
		this.haftaCalismaSaat = haftaCalismaSaat;
	}

	public List<Tanim> getDenklestirmeDinamikAlanlar() {
		return denklestirmeDinamikAlanlar;
	}

	public void setDenklestirmeDinamikAlanlar(List<Tanim> denklestirmeDinamikAlanlar) {
		this.denklestirmeDinamikAlanlar = denklestirmeDinamikAlanlar;
	}

	public boolean isAksamAdetGoster() {
		return aksamAdetGoster;
	}

	public void setAksamAdetGoster(boolean aksamAdetGoster) {
		this.aksamAdetGoster = aksamAdetGoster;
	}

	public boolean isAksamSaatGoster() {
		return aksamSaatGoster;
	}

	public void setAksamSaatGoster(boolean aksamSaatGoster) {
		this.aksamSaatGoster = aksamSaatGoster;
	}
}