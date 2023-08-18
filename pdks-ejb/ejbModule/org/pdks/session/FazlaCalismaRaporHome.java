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
import org.apache.poi.hssf.usermodel.HSSFFont;
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
import org.hibernate.validator.InvalidStateException;
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
import org.pdks.entity.Liste;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.VardiyaHafta;
import org.pdks.entity.VardiyaSaat;
import org.pdks.security.action.UserHome;
import org.pdks.security.entity.User;

@Name("fazlaCalismaRaporHome")
public class FazlaCalismaRaporHome extends EntityHome<DepartmanDenklestirmeDonemi> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3864181405990033326L;

	static Logger logger = Logger.getLogger(FazlaCalismaRaporHome.class);

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

	private List<SelectItem> bolumDepartmanlari, gorevYeriList, tesisList, raporList;

	private HashMap<String, List<Tanim>> ekSahaListMap;

	private List<DepartmanDenklestirmeDonemi> denklestirmeDonemiList;

	private List<PersonelDenklestirme> baslikDenklestirmeDonemiList;

	private List<AylikPuantaj> aylikPuantajList;

	private List<VardiyaHafta> vardiyaHaftaList;

	private HashMap<String, Double> haftaCalismaMap;

	private Sirket sirket;

	private DenklestirmeAy denklestirmeAy;

	private boolean adminRole, ikRole, talepGoster;

	private Boolean departmanBolumAyni = Boolean.FALSE, tekSirket;

	private Boolean modelGoster = Boolean.FALSE, kullaniciPersonel = Boolean.FALSE, fazlaMesaiSayfa = Boolean.TRUE;

	private int ay, yil, maxYil, maxFazlaCalismaGun;

	private List<SelectItem> aylar;

	private Drawing drawing = null;
	private ClientAnchor anchor;
	private AylikPuantaj aylikPuantajDefault;

	private TreeMap<String, Tanim> ekSahaTanimMap;

	private String sanalPersonelAciklama, bolumAciklama, raporSecim, raporAdi;
	private String sicilNo = "", excelDosyaAdi;

	private Long seciliEkSaha3Id, sirketId = null, departmanId, gorevTipiId, tesisId;

	private Tanim gorevYeri, seciliBolum, ekSaha4Tanim;

	private byte[] excelData;

	private List<SelectItem> pdksSirketList, departmanList;
	private Departman departman;

	private TreeMap<String, Tanim> fazlaMesaiMap;
	private Date basTarih, bitTarih;
	private List<VardiyaGun> vardiyaGunList, vardiyaGunPerList;
	private VardiyaGun vardiyaGun;
	private Font fontBold, font;
	private CellStyle header, styleOdd, styleOddCenter, styleOddTutar, styleOddNumber;
	private CellStyle styleEven, styleEvenCenter, styleEvenTutar, styleEvenNumber;
	private Session session;

	@Override
	public Object getId() {
		if (personelDenklestirmeId == null) {
			return super.getId();
		} else {
			return personelDenklestirmeId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	/**
	 * 
	 */
	private void adminRoleDurum() {
		adminRole = authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || authenticatedUser.isIKAdmin();
		ikRole = authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || authenticatedUser.isIK();
	}

	public String panelGuncelle() {
		listeTemizle();
		return "";
	}

	public VardiyaGun vardiyaBul(AylikPuantaj aylikPuantaj, VardiyaGun islemVardiyaGun) {
		vardiyaGun = null;
		try {
			String key = islemVardiyaGun.getVardiyaDateStr();
			if (aylikPuantaj.getVgMap().containsKey(key))
				vardiyaGun = aylikPuantaj.getVgMap().get(key);
			if (vardiyaGun != null && !vardiyaGun.getDurum()) {
				vardiyaGun.setVardiyaSaat(null);
			}
		} catch (Exception e) {

		}

		return vardiyaGun;
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() {
		fazlaMesaiSayfa = false;
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
			raporList = new ArrayList<SelectItem>();
		else
			raporList.clear();

		listeTemizle();

		if (!ortakIslemler.getParameterKey("maxGunCalismaSaat").equals(""))
			raporList.add(new SelectItem("maxGunCalismaSaat", "Günlük Çalışmayı Aşanlar"));
		if (!ortakIslemler.getParameterKey("maxHaftaCalismaSaat").equals(""))
			raporList.add(new SelectItem("maxHaftaCalismaSaat", "Haftalık Çalışmayı Aşanlar"));
		if (!ortakIslemler.getParameterKey("maxToplamMesai").equals(""))
			raporList.add(new SelectItem("maxToplamMesai", "Fazla Mesai Yapanlar"));
		if (raporList.isEmpty())
			raporSecim = null;
		else if (raporSecim == null)
			raporSecim = (String) raporList.get(0).getValue();

		String maxFazlaCalismaGunStr = ortakIslemler.getParameterKey("maxFazlaCalismaGun");
		if (!maxFazlaCalismaGunStr.equals(""))
			try {
				maxFazlaCalismaGun = Integer.parseInt(maxFazlaCalismaGunStr);
			} catch (Exception e) {
				maxFazlaCalismaGun = -1;
			}
		// if (maxFazlaMesaiOnayGun < 1)
		// maxFazlaMesaiOnayGun = 7;

		if (basTarih == null) {
			basTarih = PdksUtil.getDate(new Date());
			if (maxFazlaCalismaGun > 0) {

				Calendar cal = Calendar.getInstance();
				int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
				if (dayOfWeek != Calendar.MONDAY) {
					cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
					if (cal.getTime().after(basTarih))
						cal.add(Calendar.DATE, -7);
					basTarih = PdksUtil.getDate(cal.getTime());
				}
				basTarih = PdksUtil.tariheGunEkleCikar(basTarih, -7);
			}
		}

		if (bitTarih == null)
			bitTarih = PdksUtil.tariheGunEkleCikar(basTarih, maxFazlaCalismaGun > 0 ? maxFazlaCalismaGun - 1 : 0);

		try {
			modelGoster = Boolean.FALSE;
			departmanBolumAyni = Boolean.FALSE;
			setSirket(null);
			sirketId = null;
			setTesisId(null);
			setTesisList(null);
			aylar = PdksUtil.getAyListesi(Boolean.TRUE);
			seciliEkSaha3Id = null;
			Calendar cal = Calendar.getInstance();
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

			String gorevTipiIdStr = null, gorevYeriIdStr = null, sirketIdStr = null, tesisIdStr = null;
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
			Date sonGun = PdksUtil.tariheGunEkleCikar(basTarih, maxFazlaCalismaGun);
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
		} else
			departmanId = null;
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

	/**
	 * 
	 */
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
		if (basTarih.getTime() <= bitTarih.getTime()) {
			Date sonGun = PdksUtil.tariheGunEkleCikar(basTarih, maxFazlaCalismaGun);
			if (!raporSecim.equals("maxToplamMesai")) {
				Long gun = PdksUtil.tarihFarki(basTarih, bitTarih) + 1;
				if (raporSecim.equals("maxGunCalismaSaat")) {
					if (gun > 31) {
						PdksUtil.addMessageAvailableWarn("Bir aylık süreyi aşamazsınız!");
						devam = false;
					}
				} else if (raporSecim.equals("maxHaftaCalismaSaat")) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(basTarih);
					long mod = gun.longValue() % 7;
					if (gun > 32 || mod != 0) {
						PdksUtil.addMessageAvailableWarn("Bir aylık süreyi aşamazsınız ve gün sayısı 7'nin katları olmalıdır!");
						devam = false;
					} else if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
						PdksUtil.addMessageAvailableWarn("Başlangıç günü PAZARTESİ olmalıdır!");
						devam = false;
					}
				}

			}
			if (maxFazlaCalismaGun > 0 && sonGun.before(bitTarih)) {
				PdksUtil.addMessageAvailableWarn(maxFazlaCalismaGun + " günden fazla işlem yapılmaz!");
				devam = false;
			}

		} else {
			PdksUtil.addMessageAvailableWarn("Tarih hatalıdır!");
			devam = false;
		}
		if (!devam)
			return "";

		linkAdres = null;
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.clear();

		listeTemizle();

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
	 * @param vardiya
	 * @return
	 */

	/**
	 * @param aylikPuantajSablon
	 * @param denklestirmeDonemi
	 */
	public void fillPersonelDenklestirmeRaporDevam(AylikPuantaj aylikPuantajSablon, DepartmanDenklestirmeDonemi denklestirmeDonemi) {
		session.clear();
		fazlaMesaiVardiyaGun = null;
		sanalPersonelAciklama = ortakIslemler.sanalPersonelAciklama();
		departmanBolumAyni = Boolean.FALSE;
		saveLastParameter();
		departmanBolumAyni = sirket != null && sirket.isTesisDurumu() == false;
		if (sicilNo != null)
			sicilNo = sicilNo.trim();
		listeTemizle();
		try {
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
			if (!personelList.isEmpty()) {
				ekSaha4Tanim = ortakIslemler.getEkSaha4(null, sirketId, session);
				List<Long> personelIdler = new ArrayList<Long>();
				boolean ekSaha4Var = false;
				TreeMap<Long, AylikPuantaj> puantajMap = new TreeMap<Long, AylikPuantaj>();
				for (Personel personel : personelList) {
					if (!ekSaha4Var)
						ekSaha4Var = personel.getEkSaha4() != null;
					AylikPuantaj aylikPuantaj = new AylikPuantaj();
					aylikPuantaj.setSaatToplami(0.0d);
					aylikPuantaj.setPdksPersonel(personel);
					aylikPuantaj.setVgMap(new TreeMap<String, VardiyaGun>());
					aylikPuantaj.setVardiyalar(new ArrayList<VardiyaGun>());
					aylikPuantajList.add(aylikPuantaj);
					personelIdler.add(personel.getId());
					puantajMap.put(personel.getId(), aylikPuantaj);
				}
				if (!ekSaha4Var)
					ekSaha4Tanim = null;

				if (raporSecim.equals("maxGunCalismaSaat") || raporSecim.equals("maxHaftaCalismaSaat")) {
					fazlaCalismaHazirla(personelIdler);
				} else if (raporSecim.equals("maxToplamMesai")) {
					maxToplamMesaiHazirla(personelIdler, puantajMap);
				}

				if (raporSecim.equals("maxGunCalismaSaat"))
					maxGunCalismaSaatKontrol();
				else if (raporSecim.equals("maxHaftaCalismaSaat"))
					maxHaftaCalismaSaatKontrol();
				else if (!raporSecim.equals("maxToplamMesai"))
					aylikPuantajList.clear();
				for (SelectItem st : raporList) {
					if (st.getValue().equals(raporSecim))
						raporAdi = st.getLabel() + " Raporu";

				}
				if (!aylikPuantajList.isEmpty()) {
					TreeMap<String, Liste> listeMap = new TreeMap<String, Liste>();
					for (Iterator iterator = aylikPuantajList.iterator(); iterator.hasNext();) {
						AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
						Personel personel = aylikPuantaj.getPdksPersonel();
						String key = (tesisId == null && personel.getTesis() != null ? personel.getTesis().getAciklama() + "_" : "");
						key += (personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
						key += (personel.getYoneticisi() != null ? personel.getYoneticisi().getAdSoyad() : "");
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
					}

				}

				talepGoster = false;

			}
		} catch (InvalidStateException e) {
		} catch (Exception e3) {
			logger.error("Pdks hata in : \n");
			e3.printStackTrace();
			logger.error("Pdks hata out : " + e3.getMessage());

		} finally {

		}

	}

	/**
	 * @param personelList
	 * @throws Exception
	 */
	private void fazlaCalismaHazirla(List<Long> personelIdler) throws Exception {
		Double calSure = 0.0d;
		if (raporSecim.equals("maxGunCalismaSaat"))
			calSure = getMaxGunCalismaSaat();
		List<VardiyaGun> list = getVardiyaList(personelIdler, calSure);
		if (!list.isEmpty()) {
			// TreeMap<String, VardiyaGun> varMap = ortakIslemler.getIslemVardiyalar(personelIdler, basTarih, bitTarih, false, session, false);
			TreeMap<String, VardiyaGun> varMap = new TreeMap<String, VardiyaGun>();
			for (VardiyaGun gun : list)
				if (gun.getVardiyaSaat().getCalismaSuresi() > 0.0d)
					varMap.put(gun.getVardiyaKeyStr(), gun);

			if (!varMap.isEmpty()) {
				Calendar cal = Calendar.getInstance();
				// Long gunAdet = PdksUtil.tarihFarki(basTarih, bitTarih) + 1;
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
						aylikPuantaj.getVgMap().put(key, gun2);
						gun2.setTdClass("");
						if (personel.getIseBaslamaTarihi().after(tarih) || personel.getSskCikisTarihi().before(tarih))
							continue;
						if (gun2.getId() != null) {
							aylikPuantaj.getVardiyalar().add(gun2);
							if (vardiyaHafta != null && gun2.getDurum() && gun2.getVardiyaSaat() != null) {
								String str = vardiyaHafta.getHafta() + "_" + personel.getId();
								Double sure = haftaCalismaMap.containsKey(str) ? haftaCalismaMap.get(str) : 0.0d;
								sure += gun2.getVardiyaSaat().getCalismaSuresi();
								haftaCalismaMap.put(str, sure);
							}
						}

					}
					if (maxHaftaCalismaSaatDurum && vardiyaHafta.getVardiyaGunler().size() == 7)
						vardiyaHafta = null;
					cal.add(Calendar.DATE, 1);
				}
				varMap = null;
			}
		}
		list = null;
	}

	/**
	 * @param personelIdler
	 * @param calSure
	 * @return
	 */
	private List<VardiyaGun> getVardiyaList(List<Long> personelIdler, Double calSure) {

		StringBuffer sb = new StringBuffer();
		sb.append("SP_GET_FAZLA_MESAI_VARDIYA");
		LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<String, Object>();
		linkedHashMap.put("perList", ortakIslemler.getListIdStr(personelIdler));
		linkedHashMap.put("basTarih", PdksUtil.convertToDateString(basTarih, "yyyyMMdd"));
		linkedHashMap.put("bitTarih", PdksUtil.convertToDateString(bitTarih, "yyyyMMdd"));
		linkedHashMap.put("calSure", calSure);
		linkedHashMap.put("format", null);
		if (session != null)
			linkedHashMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<VardiyaGun> list = null;
		try {
			list = pdksEntityController.execSPList(linkedHashMap, sb, VardiyaGun.class);
		} catch (Exception e) {
			list = new ArrayList<VardiyaGun>();
		}
		return list;

	}

	/**
	 * @param personelIdler
	 * @param puantajMap
	 */
	private void maxToplamMesaiHazirla(List<Long> personelIdler, TreeMap<Long, AylikPuantaj> puantajMap) {
		List<VardiyaGun> list = getVardiyaList(personelIdler, null);

		if (!list.isEmpty()) {
			Calendar cal = Calendar.getInstance();
			TreeMap<String, VardiyaGun> gunMap = new TreeMap<String, VardiyaGun>();
			for (VardiyaGun gun : list) {
				cal.setTime(gun.getVardiyaDate());
				cal.set(Calendar.DATE, 1);
				AylikPuantaj aylikPuantaj = puantajMap.get(gun.getPdksPersonel().getId());
				aylikPuantaj.getVardiyalar().add(gun);
				Date xVardiyaDate = PdksUtil.getDate(cal.getTime());
				String key = PdksUtil.convertToDateString(xVardiyaDate, "yyyyMMdd");
				if (!gunMap.containsKey(key))
					gunMap.put(key, new VardiyaGun(null, null, xVardiyaDate));
				TreeMap<String, VardiyaGun> vgMap = aylikPuantaj.getVgMap();
				VardiyaGun vardiyaGunPer = null;
				if (vgMap.containsKey(key)) {
					vardiyaGunPer = vgMap.get(key);
				} else {
					vardiyaGunPer = new VardiyaGun(gun.getPdksPersonel(), null, xVardiyaDate);
					VardiyaSaat vardiyaSaat = new VardiyaSaat();
					vardiyaSaat.setCalismaSuresi(0.0d);
					vardiyaGunPer.setVardiyaSaat(vardiyaSaat);
					vgMap.put(key, vardiyaGunPer);
				}
				VardiyaSaat vardiyaSaat = vardiyaGunPer.getVardiyaSaat(), vardiyaSaatDB = gun.getVardiyaSaat();
				double sure = vardiyaSaatDB.getCalismaSuresi() - vardiyaSaatDB.getNormalSure();
				vardiyaSaat.setCalismaSuresi(vardiyaSaat.getCalismaSuresi() + sure);
				aylikPuantaj.setSaatToplami(aylikPuantaj.getSaatToplami() + sure);
			}
			vardiyaGunList.addAll(new ArrayList<VardiyaGun>(gunMap.values()));
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
		Double double1 = haftaCalismaMap.containsKey(key) ? haftaCalismaMap.get(key) : null;
		return double1;
	}

	private void maxGunCalismaSaatKontrol() {
		Double maxGunCalismaSaat = getMaxGunCalismaSaat();
		if (maxGunCalismaSaat > 0.0d) {
			for (Iterator iterator = aylikPuantajList.iterator(); iterator.hasNext();) {
				AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
				List<VardiyaGun> vardiyaGuns = aylikPuantaj.getVardiyalar();
				boolean sil = true;
				for (VardiyaGun gun : vardiyaGuns) {
					if (gun.getDurum() && gun.getVardiyaSaat() != null) {
						double saat = gun.getVardiyaSaat().getCalismaSuresi();
						if (saat >= maxGunCalismaSaat) {
							sil = false;
							gun.setTdClass("font-weight: bold; color: red;");
						}
					}

				}
				if (sil)
					iterator.remove();
			}
		} else
			aylikPuantajList.clear();

	}

	private Double getMaxGunCalismaSaat() {
		Double maxGunCalismaSaat = null;
		try {
			String str = ortakIslemler.getParameterKey("maxGunCalismaSaat");
			if (PdksUtil.hasStringValue(str))
				maxGunCalismaSaat = Double.parseDouble(str);
			if (maxGunCalismaSaat < 0)
				maxGunCalismaSaat = 0.0d;
		} catch (Exception e) {
			maxGunCalismaSaat = 0.0d;
		}
		return maxGunCalismaSaat;
	}

	private void maxHaftaCalismaSaatKontrol() {
		Double maxHaftaCalismaSaat = null;
		try {
			String str = ortakIslemler.getParameterKey("maxHaftaCalismaSaat");
			if (PdksUtil.hasStringValue(str))
				maxHaftaCalismaSaat = Double.parseDouble(str);
			if (maxHaftaCalismaSaat < 0)
				maxHaftaCalismaSaat = 0.0d;
		} catch (Exception e) {
			maxHaftaCalismaSaat = 0.0d;
		}
		if (maxHaftaCalismaSaat > 0.0d) {
			for (Iterator iterator = aylikPuantajList.iterator(); iterator.hasNext();) {
				AylikPuantaj aylikPuantaj = (AylikPuantaj) iterator.next();
				boolean sil = true;
				int sira = 0;
				double toplamMesai = 0;
				for (VardiyaGun isGun : vardiyaGunList) {
					vardiyaBul(aylikPuantaj, isGun);
					++sira;
					if (vardiyaGun != null && vardiyaGun.getVardiyaSaat() != null) {
						double saat = vardiyaGun.getVardiyaSaat().getCalismaSuresi();
						toplamMesai += saat;
					}
					if (sira == 7) {
						if (toplamMesai >= maxHaftaCalismaSaat) {
							sil = false;
							break;
						}
						sira = 0;
						toplamMesai = 0.0d;
					}
				}
				if (sil)
					iterator.remove();
				else
					for (VardiyaHafta vardiyaHafta : vardiyaHaftaList) {
						String str = vardiyaHafta.getHafta() + "_" + aylikPuantaj.getPdksPersonel().getId();
						if (haftaCalismaMap.containsKey(str)) {
							Double toplamSure = haftaCalismaMap.get(str);
							if (toplamSure < maxHaftaCalismaSaat)
								haftaCalismaMap.remove(str);
						}

					}
			}
		} else
			aylikPuantajList.clear();

	}

	/**
	 * @param map1
	 */
	private void saveLastParameter() {
		LinkedHashMap<String, Object> lastMap = new LinkedHashMap<String, Object>();
		lastMap.put("basTarih", PdksUtil.convertToDateString(basTarih, "yyyyMMdd"));
		lastMap.put("bitTarih", PdksUtil.convertToDateString(bitTarih, "yyyyMMdd"));
		if (departmanId != null)
			lastMap.put("departmanId", "" + departmanId);
		if (sirketId != null)
			lastMap.put("sirketId", "" + sirketId);
		if (tesisId != null)
			lastMap.put("tesisId", "" + tesisId);
		if (seciliEkSaha3Id != null)
			lastMap.put("bolumId", "" + seciliEkSaha3Id);
		lastMap.put("raporSecim", raporSecim);

		if ((authenticatedUser.isIK() || authenticatedUser.isAdmin()) && sicilNo != null && sicilNo.trim().length() > 0)
			lastMap.put("sicilNo", sicilNo.trim());
		try {

			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception e) {

		}

	}

	public String getExcelAciklama() {
		tekSirket = pdksSirketList != null && pdksSirketList.size() == 1;
		String gorevYeriAciklama = "";
		if (gorevYeri != null)
			gorevYeriAciklama = gorevYeri.getAciklama() + "_";
		else if (seciliEkSaha3Id != null || tesisId != null) {
			HashMap parametreMap = new HashMap();
			Tanim ekSaha3 = null, tesis = null;
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
				gorevYeriAciklama += ekSaha3.getAciklama() + "_";
		} else if (sirketId != null && tekSirket) {
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
			ByteArrayOutputStream baosDosya = fazlaMesaiExcelDevam(gorevYeriAciklama, aylikPuantajList, vardiyaGunPerList);

			if (baosDosya != null) {
				String dosyaAdi = raporSecim + "_" + gorevYeriAciklama + PdksUtil.convertToDateString(basTarih, "yyyyMMdd") + "_" + PdksUtil.convertToDateString(bitTarih, "yyyyMMdd") + ".xlsx";
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
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
	}

	/**
	 * @param gorevYeriAciklama
	 * @param list
	 * @return
	 */
	/**
	 * @param gorevYeriAciklama
	 * @param list1
	 * @param list2
	 * @return
	 */
	protected ByteArrayOutputStream fazlaMesaiExcelDevam(String gorevYeriAciklama, List<AylikPuantaj> list1, List<VardiyaGun> list2) {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();

		fontBold = ExcelUtil.createFont(wb, (short) 9, ExcelUtil.FONT_NAME, HSSFFont.BOLDWEIGHT_BOLD);
		font = ExcelUtil.createFont(wb, (short) 8, ExcelUtil.FONT_NAME, HSSFFont.BOLDWEIGHT_NORMAL);

		header = ExcelUtil.getStyleHeader(wb);
		styleOdd = ExcelUtil.getStyleOdd(null, wb);
		styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		styleOddTutar = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TUTAR, wb);
		styleOddNumber = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_NUMBER, wb);
		styleEven = ExcelUtil.getStyleEven(null, wb);
		styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		styleEvenTutar = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TUTAR, wb);
		styleEvenNumber = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_NUMBER, wb);
		if (list1 != null && !list1.isEmpty())
			sayfaAylikPuantajOlustur(list1, wb);
		if (list2 != null && !list2.isEmpty())
			sayfaVardiyaOlustur(list2, wb);

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

	private void sayfaVardiyaOlustur(List<VardiyaGun> list, Workbook wb) {

	}

	/**
	 * @param onayDurum
	 * @param list
	 * @param wb
	 */
	private void sayfaAylikPuantajOlustur(List<AylikPuantaj> list, Workbook wb) {
		boolean maxToplamMesaiDurum = raporSecim.equals("maxToplamMesai");
		Sheet sheet = ExcelUtil.createSheet(wb, raporAdi, Boolean.TRUE);
		CreationHelper factory = null;
		drawing = sheet.createDrawingPatriarch();
		// Drawing drawing = null;
		// ClientAnchor anchor = null;

		// drawing = sheet.createDrawingPatriarch();
		factory = wb.getCreationHelper();
		anchor = factory.createClientAnchor();
		// anchor = factory.createClientAnchor();

		TreeMap<String, String> sirketMap = new TreeMap<String, String>();
		List<VardiyaGun> vardiyaGunPersonelList = new ArrayList<VardiyaGun>();
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();
			if (aylikPuantaj.getVardiyalar() != null && !aylikPuantaj.getVardiyalar().isEmpty())
				vardiyaGunPersonelList.addAll(aylikPuantaj.getVardiyalar());

			Personel personel = aylikPuantaj.getPdksPersonel();
			String tekSirketTesis = (personel.getSirket() != null ? personel.getSirket().getId() : "") + "_" + (personel.getTesis() != null ? personel.getTesis().getId() : "");
			String tekSirketTesisAdi = (personel.getSirket() != null ? personel.getSirket().getAd() : "") + " " + (personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
			sirketMap.put(tekSirketTesis, tekSirketTesisAdi);
		}

		int col = 0, row = 0;

		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		boolean tesisVar = tesisId == null && tesisList != null && tesisList.size() > 0;
		if (tesisVar)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
		if (seciliEkSaha3Id == null)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
		if (ekSaha4Tanim != null)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ekSaha4Tanim.getAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.yoneticiAciklama());
		if (vardiyaHaftaList.isEmpty()) {
			for (VardiyaGun gun : vardiyaGunList) {
				if (maxToplamMesaiDurum == false)
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue(PdksUtil.convertToDateString(gun.getVardiyaDate(), "d") + "\n" + PdksUtil.convertToDateString(gun.getVardiyaDate(), "EEE"));
				else
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue(PdksUtil.convertToDateString(gun.getVardiyaDate(), "yyyy") + "\n" + PdksUtil.convertToDateString(gun.getVardiyaDate(), "MMM"));
			}
		} else {
			CellStyle headerBlue = ExcelUtil.getStyleHeader(wb);
			ExcelUtil.setFontColor(headerBlue, Color.BLUE);
			for (VardiyaHafta vardiyaHafta : vardiyaHaftaList) {
				for (VardiyaGun gun : vardiyaHafta.getVardiyaGunler()) {
					ExcelUtil.getCell(sheet, row, col++, header).setCellValue(PdksUtil.convertToDateString(gun.getVardiyaDate(), "d") + "\n" + PdksUtil.convertToDateString(gun.getVardiyaDate(), "EEE"));

				}
				ExcelUtil.getCell(sheet, row, col++, headerBlue).setCellValue((vardiyaHaftaList.size() > 1 ? vardiyaHafta.getHafta() + ". " : "") + "Hafta\n Toplamı");
			}
		}

		if (maxToplamMesaiDurum && vardiyaGunList.size() > 1)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Toplam Fazla Çalışma");
		boolean renk = true;
		CellStyle styleRedOddTutar = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleRedOddNumber = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_NUMBER, wb);
		CellStyle styleRedEvenTutar = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleRedEvenNumber = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_NUMBER, wb);

		ExcelUtil.setFontColor(styleRedOddTutar, Color.RED);
		ExcelUtil.setFontColor(styleRedOddNumber, Color.RED);
		ExcelUtil.setFontColor(styleRedEvenTutar, Color.RED);
		ExcelUtil.setFontColor(styleRedEvenNumber, Color.RED);
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();
			Personel personel = aylikPuantaj.getPdksPersonel();
			CellStyle styleCenter = null, styleGenel = null, styleDouble = null, styleNumber = null, styleRedDouble = null, styleRedNumber = null;
			if (renk) {
				styleGenel = styleOdd;
				styleCenter = styleOddCenter;
				styleDouble = styleOddTutar;
				styleNumber = styleOddNumber;
				styleRedDouble = styleRedOddTutar;
				styleRedNumber = styleRedOddNumber;
			} else {
				styleGenel = styleEven;
				styleCenter = styleEvenCenter;
				styleDouble = styleEvenTutar;
				styleNumber = styleEvenNumber;
				styleRedDouble = styleRedEvenTutar;
				styleRedNumber = styleRedEvenNumber;
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
				if (vardiyaHaftaList.isEmpty()) {

					for (VardiyaGun islemVardiyaGun : vardiyaGunList) {
						vardiyaBul(aylikPuantaj, islemVardiyaGun);
						VardiyaGun gun = vardiyaGun;
						if (gun != null && (gun.getVardiya() != null || maxToplamMesaiDurum)) {
							Double tutar = 0.0d;
							Cell fmtCell = null;
							boolean kirmizi = false;
							if (gun.getVardiyaSaat() != null) {
								tutar = gun.getVardiyaSaat().getCalismaSuresi();
								kirmizi = PdksUtil.hasStringValue(gun.getTdClass());
							}
							if (tutar > 0.0d) {
								if (PdksUtil.isDoubleValueNotLong(tutar))
									fmtCell = ExcelUtil.getCell(sheet, row, col++, kirmizi == false ? styleDouble : styleRedDouble);
								else
									fmtCell = ExcelUtil.getCell(sheet, row, col++, kirmizi == false ? styleNumber : styleRedNumber);
								fmtCell.setCellValue(tutar);
							} else {
								fmtCell = ExcelUtil.getCell(sheet, row, col++, styleCenter);
								fmtCell.setCellValue("");
							}
							if (maxToplamMesaiDurum == false) {
								List<String> sb = new ArrayList<String>();
								String str = gun.getFazlaMesaiTitle();
								if (str != null)
									sb.add(str);
								setCommentCell(wb, factory, fmtCell, sb);
							}

						} else
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(maxToplamMesaiDurum ? "" : "-");

					}
				} else {
					for (VardiyaHafta vardiyaHafta : vardiyaHaftaList) {
						for (VardiyaGun islemVardiyaGun : vardiyaHafta.getVardiyaGunler()) {
							vardiyaBul(aylikPuantaj, islemVardiyaGun);
							VardiyaGun gun = vardiyaGun;
							boolean yazildi = false;
							if (gun != null && gun.getVardiya() != null) {
								Double tutar = 0.0d;
								Cell fmtCell = null;
								if (gun.getVardiyaSaat() != null) {
									tutar = gun.getVardiyaSaat().getCalismaSuresi();
									if (tutar > 0.0d) {
										yazildi = true;
										fmtCell = ExcelUtil.getCell(sheet, row, col++, PdksUtil.isDoubleValueNotLong(tutar) ? styleDouble : styleNumber);
										fmtCell.setCellValue(tutar);
									}
									String str = gun.getFazlaMesaiTitle();
									if (str != null) {
										List<String> sb = new ArrayList<String>();
										sb.add(str);
										setCommentCell(wb, factory, fmtCell, sb);
										sb = null;
									}
								}
							}
							if (!yazildi)
								ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue("");

						}

						Double sure = getCalismaSuresi(vardiyaHafta.getHafta(), aylikPuantaj);
						if (sure != null)
							ExcelUtil.getCell(sheet, row, col++, PdksUtil.isDoubleValueNotLong(sure) ? styleRedDouble : styleRedNumber).setCellValue(sure);
						else
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue("");
					}

				}
				if (maxToplamMesaiDurum && vardiyaGunList.size() > 1) {
					Double tutar = aylikPuantaj.getSaatToplami();
					Cell fmtCell = ExcelUtil.getCell(sheet, row, col++, PdksUtil.isDoubleValueNotLong(tutar) ? styleRedDouble : styleRedNumber);
					fmtCell.setCellValue(tutar);
				}

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				logger.error(row);

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
		if (titles != null && !titles.isEmpty()) {
			for (Iterator iterator = titles.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				if (PdksUtil.hasStringValue(string) && string.indexOf(":") > 0)
					continue;
				iterator.hasNext();
			}
			int bas[] = new int[titles.size()], uz[] = new int[titles.size()];
			StringBuffer sb = new StringBuffer();
			int b1 = 0, b2 = 0;
			int i = 0;
			for (Iterator iterator = titles.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				String str = string.trim() + (iterator.hasNext() ? "\n" : "");
				int basYer = (i > 0 ? uz[i - 1] : 0);
				b1 = basYer + str.indexOf(":");
				b2 = basYer + str.length();
				sb.append(str);
				bas[i] = b1;
				uz[i] = b2;
				++i;
			}

			String title = sb.toString();
			rt = factory.createRichTextString(title);
			rt.applyFont(font);
			b1 = 0;
			for (int j = 0; j < uz.length; j++) {
				try {
					b2 = bas[j];
					rt.applyFont(b1, b2, fontBold);
					b1 = uz[j];
				} catch (Exception e) {
					logger.error(j + " " + b1 + " " + b2 + " " + e);
					e.printStackTrace();
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
	 * @param deger
	 * @return
	 */
	public Cell setCellDate(Sheet sheet, int rowNo, int columnNo, CellStyle style, Date date) {
		Cell cell = ExcelUtil.getCell(sheet, rowNo, columnNo, style);

		try {
			if (date != null) {
				cell.setCellValue(date);
			} else
				cell.setCellValue("");

		} catch (Exception e) {
		}
		return cell;
	}

	/**
	 * @param bolumDoldurDurum
	 * @throws Exception
	 */
	public void tesisDoldur(boolean bolumDoldurDurum) throws Exception {
		sirket = null;
		if (pdksSirketList == null || pdksSirketList.isEmpty())
			setTesisList(new ArrayList<SelectItem>());
		else {
			if (sirketId != null) {
				HashMap fields = new HashMap();
				fields.put("id", sirketId);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);
			}
			List<SelectItem> list = fazlaMesaiOrtakIslemler.getFazlaMesaiTesisList(sirket, new AylikPuantaj(basTarih, bitTarih), true, session);
			setTesisList(list);
			Long onceki = tesisId;
			if (list != null && !list.isEmpty()) {
				if (list.size() == 1)
					tesisId = (Long) list.get(0).getValue();
				else if (onceki != null) {
					for (SelectItem st : list) {
						if (st.getValue().equals(onceki))
							tesisId = onceki;
					}
				}
			} else
				bolumDoldurDurum = true;
			onceki = tesisId;
			if (tesisId != null || (sirket != null && sirket.isTesisDurumu() == false)) {
				if (bolumDoldurDurum)
					bolumDoldur();
				setTesisId(onceki);
				if (gorevYeriList != null && list.size() > 1)
					gorevYeriList.clear();
			}

		}
		listeTemizle();
	}

	public String bolumDoldur() {
		fazlaMesaiVardiyaGun = null;
		linkAdres = null;
		if (pdksSirketList == null || pdksSirketList.isEmpty())
			setGorevYeriList(new ArrayList<SelectItem>());
		else {

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
				if (departman.isAdminMi() && sirket.isTesisDurumu()) {
					try {
						// List<SelectItem> list=fazlaMesaiOrtakIslemler.bolumDoldur(departman, sirket, null, tesisId, yil, ay, Boolean.TRUE, session);
						List<SelectItem> list = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, tesisId != null ? String.valueOf(tesisId) : null, new AylikPuantaj(basTarih, bitTarih), Boolean.TRUE, session);
						setGorevYeriList(list);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (gorevYeriList.size() == 1)
						seciliEkSaha3Id = (Long) gorevYeriList.get(0).getValue();
				} else {
					// Long depId = departman != null ? departman.getId() : null;
					// bolumDepartmanlari = fazlaMesaiOrtakIslemler.getBolumDepartmanSelectItems(depId, sirketId, yil, ay, Boolean.TRUE, session);
					bolumDepartmanlari = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, null, new AylikPuantaj(basTarih, bitTarih), Boolean.TRUE, session);
					if (bolumDepartmanlari.size() == 1)
						seciliEkSaha3Id = (Long) bolumDepartmanlari.get(0).getValue();
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
		this.sirket = value;
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

}
