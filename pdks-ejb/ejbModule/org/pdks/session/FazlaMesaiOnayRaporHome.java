package org.pdks.session;

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
import org.pdks.entity.FazlaMesaiTalep;
import org.pdks.entity.Liste;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelFazlaMesai;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.security.action.UserHome;
import org.pdks.security.entity.User;

@Name("fazlaMesaiOnayRaporHome")
public class FazlaMesaiOnayRaporHome extends EntityHome<DepartmanDenklestirmeDonemi> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3864181405990033326L;

	static Logger logger = Logger.getLogger(FazlaMesaiOnayRaporHome.class);

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

	private List<PersonelFazlaMesai> personelFazlaMesaiList;

	private List<SelectItem> bolumDepartmanlari, gorevYeriList, tesisList;

	private HashMap<String, List<Tanim>> ekSahaListMap;

	private List<DepartmanDenklestirmeDonemi> denklestirmeDonemiList;

	private List<PersonelDenklestirme> baslikDenklestirmeDonemiList;

	private Sirket sirket;

	private DenklestirmeAy denklestirmeAy;

	private boolean adminRole, ikRole;

	private Boolean departmanBolumAyni = Boolean.FALSE, tekSirket;
	private Boolean modelGoster = Boolean.FALSE, kullaniciPersonel = Boolean.FALSE, fazlaMesaiSayfa = Boolean.TRUE;

	private int ay, yil, maxYil, maxFazlaMesaiRaporGun;

	private List<User> toList, ccList, bccList;

	private TreeMap<Long, List<FazlaMesaiTalep>> fmtMap;

	private List<SelectItem> aylar;

	private AylikPuantaj aylikPuantajDefault;

	private TreeMap<String, Tanim> ekSahaTanimMap;

	private String sanalPersonelAciklama, bolumAciklama;
	private String sicilNo = "", excelDosyaAdi;

	private Long seciliEkSaha3Id, sirketId = null, departmanId, gorevTipiId, tesisId;
	private Tanim gorevYeri, seciliBolum;

	private byte[] excelData;

	private List<SelectItem> pdksSirketList, departmanList;
	private Departman departman;

	private TreeMap<String, Tanim> fazlaMesaiMap;
	private Date basTarih, bitTarih;
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

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() {
		fazlaMesaiSayfa = false;
		adminRoleDurum();
		boolean ayniSayfa = authenticatedUser.getCalistigiSayfa() != null && authenticatedUser.getCalistigiSayfa().equals("fazlaMesaiOnayRapor");
		if (!ayniSayfa)
			authenticatedUser.setCalistigiSayfa("fazlaMesaiOnayRapor");
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		if (personelFazlaMesaiList != null)
			personelFazlaMesaiList.clear();
		else
			personelFazlaMesaiList = new ArrayList<PersonelFazlaMesai>();
		yil = -1;
		ay = -1;
		fazlaMesaiVardiyaGun = null;
		String maxFazlaMesaiRaporGunStr = ortakIslemler.getParameterKey("maxFazlaMesaiRaporGun");
		if (!maxFazlaMesaiRaporGunStr.equals(""))
			try {
				maxFazlaMesaiRaporGun = Integer.parseInt(maxFazlaMesaiRaporGunStr);
			} catch (Exception e) {
				maxFazlaMesaiRaporGun = -1;
			}
		if (maxFazlaMesaiRaporGun < 1)
			maxFazlaMesaiRaporGun = 7;

		if (basTarih == null) {
			basTarih = PdksUtil.getDate(new Date());
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

		if (bitTarih == null)
			bitTarih = PdksUtil.tariheGunEkleCikar(basTarih, maxFazlaMesaiRaporGun - 1);

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
				veriLastMap = ortakIslemler.getLastParameter("fazlaMesaiOnayRapor", session);
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
		personelFazlaMesaiList.clear();
		if (basTarih.getTime() <= bitTarih.getTime()) {
			Date sonGun = PdksUtil.tariheGunEkleCikar(basTarih, maxFazlaMesaiRaporGun);
			if (sonGun.before(bitTarih))
				PdksUtil.addMessageAvailableWarn(maxFazlaMesaiRaporGun + " günden fazla işlem yapılmaz!");
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

		personelFazlaMesaiList.clear();

	}

	@Transactional
	public String fillPersonelDenklestirmeRaporList() {
		boolean devam = true;
		if (basTarih.getTime() <= bitTarih.getTime()) {
			Date sonGun = PdksUtil.tariheGunEkleCikar(basTarih, maxFazlaMesaiRaporGun);
			if (sonGun.before(bitTarih)) {
				PdksUtil.addMessageAvailableWarn(maxFazlaMesaiRaporGun + " günden fazla işlem yapılmaz!");
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

		personelFazlaMesaiList.clear();
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

		fazlaMesaiVardiyaGun = null;
		sanalPersonelAciklama = ortakIslemler.sanalPersonelAciklama();
		departmanBolumAyni = Boolean.FALSE;

		if (fmtMap == null)
			fmtMap = new TreeMap<Long, List<FazlaMesaiTalep>>();
		else
			fmtMap.clear();
		saveLastParameter();
		departmanBolumAyni = sirket != null && sirket.isTesisDurumu() == false;
		if (sicilNo != null)
			sicilNo = sicilNo.trim();

		try {
			seciliBolum = null;

			HashMap map = new HashMap();

			if (aylikPuantajDefault == null)
				aylikPuantajDefault = new AylikPuantaj();
			aylikPuantajDefault.setIlkGun(basTarih);
			aylikPuantajDefault.setSonGun(bitTarih);
			List<Personel> personelList = fazlaMesaiOrtakIslemler.getFazlaMesaiPersonelList(sirket, tesisId != null ? String.valueOf(tesisId) : null, seciliEkSaha3Id, null, aylikPuantajDefault, true, session);
			personelFazlaMesaiList.clear();
			if (!personelList.isEmpty()) {
				List<Long> personelIdler = new ArrayList<Long>();
				for (Personel personel : personelList)
					personelIdler.add(personel.getId());
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT F.* FROM " + VardiyaGun.TABLE_NAME + " V WITH(nolock) ");
				sb.append(" INNER JOIN  " + PersonelFazlaMesai.TABLE_NAME + " F ON F." + PersonelFazlaMesai.COLUMN_NAME_VARDIYA_GUN + "=V." + VardiyaGun.COLUMN_NAME_ID);
				sb.append(" AND F." + PersonelFazlaMesai.COLUMN_NAME_DURUM + " = 1");
				sb.append(" INNER JOIN  " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + "=V." + VardiyaGun.COLUMN_NAME_PERSONEL);
				sb.append(" AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ">=P." + Personel.getIseGirisTarihiColumn());
				sb.append(" AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + "<=P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI);
				sb.append(" WHERE V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ">= :basTarih AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + "<= :bitTarih");
				sb.append(" AND V." + VardiyaGun.COLUMN_NAME_DURUM + " = 1");
				// sb.append(" AND V." + VardiyaGun.COLUMN_NAME_PERSONEL + ":pId ");
				// map.put("pId", personelIdler);
				sb.append(" ORDER BY V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI);
				map.put("basTarih", PdksUtil.getDate(basTarih));
				map.put("bitTarih", PdksUtil.getDate(bitTarih));
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<PersonelFazlaMesai> idList = pdksEntityController.getObjectBySQLList(sb, map, PersonelFazlaMesai.class);
				TreeMap<String, Liste> listeMap = new TreeMap<String, Liste>();
				HashMap<Long, String> vardiyaAciklamaMap = new HashMap<Long, String>();
				for (Iterator iterator = idList.iterator(); iterator.hasNext();) {
					PersonelFazlaMesai personelFazlaMesai = (PersonelFazlaMesai) iterator.next();
					VardiyaGun vardiyaGun = personelFazlaMesai.getVardiyaGun();
					Personel personel = vardiyaGun.getPdksPersonel();
					if (!personelIdler.contains(personel.getId()) || personelFazlaMesai.getDurum().equals(Boolean.FALSE) || personelFazlaMesai.isOnaylandi() == false)
						iterator.remove();
					else {
						Vardiya vardiya = vardiyaGun.getVardiya();
						String str = "";
						Sirket sirket = personel.getSirket();
						if (vardiya != null) {
							Long key = vardiya.getId();
							if (key != null) {
								if (vardiyaAciklamaMap.containsKey(key)) {
									str = vardiyaAciklamaMap.get(key);
								} else {
									if (vardiya.isCalisma())
										str = authenticatedUser.timeFormatla(vardiya.getBasZaman()) + " : " + authenticatedUser.timeFormatla(vardiya.getBitZaman()) + " [ " + vardiya.getKisaAdi() + " ]";
									else
										str = vardiya.getKisaAdi();
									vardiyaAciklamaMap.put(key, str);
								}
							}
						}
						vardiyaGun.setManuelGirisHTML(str);

						String key = (sirket.getTesisDurum() && personel.getTesis() != null ? personel.getTesis().getAciklama() + "_" : "");
						key += (personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
						key += (personel.getYoneticisi() != null ? personel.getYoneticisi().getAdSoyad() : "");
						key += "_" + personel.getAdSoyad() + "_" + personel.getPdksSicilNo();
						Liste liste = null;
						if (listeMap.containsKey(key))
							liste = listeMap.get(key);
						else {
							liste = new Liste(key, new ArrayList<PersonelFazlaMesai>());
							listeMap.put(key, liste);
						}
						List<PersonelFazlaMesai> list = (List<PersonelFazlaMesai>) liste.getValue();
						list.add(personelFazlaMesai);
					}

				}
				personelIdler = null;

				vardiyaAciklamaMap = null;

				if (!listeMap.isEmpty()) {
					List<Liste> list = PdksUtil.sortObjectStringAlanList(new ArrayList(listeMap.values()), "getId", null);
					for (Liste liste : list) {
						List<PersonelFazlaMesai> fazlaMesaiList = (List<PersonelFazlaMesai>) liste.getValue();
						personelFazlaMesaiList.addAll(fazlaMesaiList);
					}

				} else {
					if (fazlaMesaiMap == null)
						fazlaMesaiMap = new TreeMap<String, Tanim>();
					else
						fazlaMesaiMap.clear();
				}

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

		if ((authenticatedUser.isIK() || authenticatedUser.isAdmin()) && sicilNo != null && sicilNo.trim().length() > 0)
			lastMap.put("sicilNo", sicilNo.trim());
		try {

			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception e) {

		}

	}

	private String getExcelAciklama() {
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
			ByteArrayOutputStream baosDosya = fazlaMesaiExcelDevam(gorevYeriAciklama, personelFazlaMesaiList);
			if (baosDosya != null) {
				String dosyaAdi = "DonemselFazlaCalisma_" + gorevYeriAciklama + PdksUtil.convertToDateString(basTarih, "yyyyMMdd") + "_" + PdksUtil.convertToDateString(bitTarih, "yyyyMMdd") + ".xlsx";
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
	private ByteArrayOutputStream fazlaMesaiExcelDevam(String gorevYeriAciklama, List<PersonelFazlaMesai> list) {

		TreeMap<String, String> sirketMap = new TreeMap<String, String>();

		for (Iterator iter = list.iterator(); iter.hasNext();) {
			PersonelFazlaMesai personelFazlaMesai = (PersonelFazlaMesai) iter.next();
			Personel personel = personelFazlaMesai.getVardiyaGun().getPdksPersonel();
			String tekSirketTesis = (personel.getSirket() != null ? personel.getSirket().getId() : "") + "_" + (personel.getTesis() != null ? personel.getTesis().getId() : "");
			String tekSirketTesisAdi = (personel.getSirket() != null ? personel.getSirket().getAd() : "") + " " + (personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
			sirketMap.put(tekSirketTesis, tekSirketTesisAdi);
		}

		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, "Dönemsel Fazla Çalışma", Boolean.TRUE);
		CellStyle styleTutar = ExcelUtil.getCellStyleTutar(wb);
		CellStyle styleNumber = ExcelUtil.getCellStyleNumber(wb);

		CellStyle styleZaman = ExcelUtil.getCellStyleTimeStamp(wb);
		CellStyle styleDate = ExcelUtil.getCellStyleDate(wb);
		CellStyle styleCenter = ExcelUtil.getStyleDataCenter(wb);
		CellStyle styleGenel = ExcelUtil.getStyleData(wb);

		CellStyle header = ExcelUtil.getStyleHeader(wb);

		int col = 0, row = 0;

		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		if (seciliEkSaha3Id == null)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.yoneticiAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Vardiya Tarihi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Vardiya");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Başlangıç Zamanı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Bitiş Zamanı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Süre");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Onay Nedeni");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Onaylayan");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Onay Zamanı");

		for (Iterator iter = list.iterator(); iter.hasNext();) {
			PersonelFazlaMesai personelFazlaMesai = (PersonelFazlaMesai) iter.next();
			VardiyaGun vardiyaGun = personelFazlaMesai.getVardiyaGun();
			Vardiya vardiya = vardiyaGun.getVardiya();
			Personel personel = vardiyaGun.getPdksPersonel();

			row++;
			col = 0;

			try {
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getSicilNo());
				Cell personelCell = ExcelUtil.getCell(sheet, row, col++, styleGenel);
				personelCell.setCellValue(personel.getAdSoyad());

				if (seciliEkSaha3Id == null)
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");

				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getYoneticisi() != null ? personel.getYoneticisi().getAdSoyad() : "");
				ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(vardiyaGun.getVardiyaDate());
				String str = vardiyaGun.getManuelGirisHTML();
				if (vardiya.isCalisma())
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(str);
				else
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(str);
				ExcelUtil.getCell(sheet, row, col++, styleZaman).setCellValue(personelFazlaMesai.getBasZaman());
				ExcelUtil.getCell(sheet, row, col++, styleZaman).setCellValue(personelFazlaMesai.getBitZaman());
				Double tutar = personelFazlaMesai.getFazlaMesaiSaati();
				if (tutar.doubleValue() > tutar.longValue())
					ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(tutar);
				else
					ExcelUtil.getCell(sheet, row, col++, styleNumber).setCellValue(tutar.longValue());
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personelFazlaMesai.getFazlaMesaiOnayDurum() != null ? personelFazlaMesai.getFazlaMesaiOnayDurum().getAciklama() : "");
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personelFazlaMesai.getOlusturanUser() != null ? personelFazlaMesai.getOlusturanUser().getAdSoyad() : "");
				if (personelFazlaMesai.getOlusturmaTarihi() != null)
					ExcelUtil.getCell(sheet, row, col++, styleZaman).setCellValue(personelFazlaMesai.getOlusturmaTarihi());
				else
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				logger.error(row);

			}

		}

		try {

			for (int i = 0; i <= col; i++)
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
	 * @param sheet
	 * @param rowNo
	 * @param columnNo
	 * @param style
	 * @param deger
	 * @return
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
		personelFazlaMesaiList.clear();
	}

	public String bolumDoldur() {
		fazlaMesaiVardiyaGun = null;
		linkAdres = null;
		if (pdksSirketList == null || pdksSirketList.isEmpty())
			setGorevYeriList(new ArrayList<SelectItem>());
		else {

			setGorevYeriList(null);
			bolumDepartmanlari = null;

			personelFazlaMesaiList.clear();
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

		personelFazlaMesaiList.clear();
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

	public TreeMap<Long, List<FazlaMesaiTalep>> getFmtMap() {
		return fmtMap;
	}

	public void setFmtMap(TreeMap<Long, List<FazlaMesaiTalep>> fmtMap) {
		this.fmtMap = fmtMap;
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

	public int getMaxFazlaMesaiRaporGun() {
		return maxFazlaMesaiRaporGun;
	}

	public void setMaxFazlaMesaiRaporGun(int maxFazlaMesaiRaporGun) {
		this.maxFazlaMesaiRaporGun = maxFazlaMesaiRaporGun;
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

	public List<PersonelFazlaMesai> getPersonelFazlaMesaiList() {
		return personelFazlaMesaiList;
	}

	public void setPersonelFazlaMesaiList(List<PersonelFazlaMesai> personelFazlaMesaiList) {
		this.personelFazlaMesaiList = personelFazlaMesaiList;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
