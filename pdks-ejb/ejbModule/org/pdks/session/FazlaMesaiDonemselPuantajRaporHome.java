package org.pdks.session;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
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
import org.pdks.entity.BordroDetayTipi;
import org.pdks.entity.CalismaModeli;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.DepartmanDenklestirmeDonemi;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelDenklestirmeBordro;
import org.pdks.entity.PersonelDenklestirmeBordroDetay;
import org.pdks.entity.PersonelDenklestirmeDinamikAlan;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.VardiyaSaat;
import org.pdks.security.action.UserHome;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;

@Name("fazlaMesaiDonemselPuantajRaporHome")
public class FazlaMesaiDonemselPuantajRaporHome extends EntityHome<DepartmanDenklestirmeDonemi> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7516859224980927543L;

	static Logger logger = Logger.getLogger(FazlaMesaiDonemselPuantajRaporHome.class);

	public static String sayfaURL = "fazlaMesaiDonemselPuantajRapor";

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
	String bordroAdres;

	@In(required = true, create = true)
	Renderer renderer;

	private Integer basYil, bitYil, sonDonem, basAy, bitAy, maxYil;
	private Long sirketId, tesisId, bolumId;
	private List<SelectItem> donemBas, donemBit, sirketler, tesisler, bolumler;
	private List<PersonelDenklestirme> perDenkList;
	private List<Personel> personelList;
	private List<DenklestirmeAy> denklestirmeAyList;
	private HashMap<String, PersonelDenklestirme> perDenkMap;
	private PersonelDenklestirme denklestirme;
	private boolean tesisVar = false, fazlaMesaiOde = false, fazlaMesaiIzinKullan = false, fazlaMesaiVar = false, saatlikMesaiVar = false, aylikMesaiVar = false, resmiTatilVar = false, haftaTatilVar = false;
	private boolean bordroPuantajEkranindaGoster = false, kismiOdemeGoster = false, yasalFazlaCalismaAsanSaat = false, istenAyrilanGoster = false;
	private Boolean gerceklesenMesaiKod = Boolean.FALSE, devredenBakiyeKod = Boolean.FALSE, normalCalismaSaatKod = Boolean.FALSE, haftaTatilCalismaSaatKod = Boolean.FALSE, resmiTatilCalismaSaatKod = Boolean.FALSE, izinSureSaatKod = Boolean.FALSE;
	private Boolean normalCalismaGunKod = Boolean.FALSE, haftaTatilCalismaGunKod = Boolean.FALSE, resmiTatilCalismaGunKod = Boolean.FALSE, izinSureGunKod = Boolean.FALSE, ucretliIzinGunKod = Boolean.FALSE, ucretsizIzinGunKod = Boolean.FALSE, hastalikIzinGunKod = Boolean.FALSE;
	private Boolean normalGunKod = Boolean.FALSE, haftaTatilGunKod = Boolean.FALSE, resmiTatilGunKod = Boolean.FALSE, artikGunKod = Boolean.FALSE, bordroToplamGunKod = Boolean.FALSE, devredenMesaiKod = Boolean.FALSE, ucretiOdenenKod = Boolean.FALSE;
	private List<Tanim> denklestirmeDinamikAlanlar;
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Boolean> baslikMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private String bolumAciklama;
	private Date basTarih, bitTarih;
	private Sirket sirket = null;
	private List<AylikPuantaj> puantajList;
	private VardiyaGun vardiyaGun = null;
	private Personel seciliPersonel = null;
	private AylikPuantaj gunAylikPuantaj;
	private Session session;

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {

		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		if (baslikMap == null)
			baslikMap = new TreeMap<String, Boolean>();
		else
			baslikMap.clear();
		if (donemBas == null)
			donemBas = new ArrayList<SelectItem>();
		if (donemBit == null)
			donemBit = new ArrayList<SelectItem>();
		if (sirketler == null)
			sirketler = new ArrayList<SelectItem>();

		if (tesisler == null)
			tesisler = new ArrayList<SelectItem>();
		tesisler.clear();
		if (bolumler == null)
			bolumler = new ArrayList<SelectItem>();
		else
			bolumler.clear();
		if (puantajList == null)
			puantajList = new ArrayList<AylikPuantaj>();
		else
			puantajList.clear();

		if (personelList == null)
			personelList = new ArrayList<Personel>();
		if (denklestirmeAyList == null)
			denklestirmeAyList = new ArrayList<DenklestirmeAy>();
		if (perDenkMap == null)
			perDenkMap = new HashMap<String, PersonelDenklestirme>();

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		basYil = calendar.get(Calendar.YEAR);
		bitYil = calendar.get(Calendar.YEAR);
		maxYil = bitYil;
		basAy = calendar.get(Calendar.MONTH) + 1;
		bitAy = calendar.get(Calendar.MONTH) + 1;
		sonDonem = (bitYil * 100) + bitAy;
		LinkedHashMap<String, Object> veriLastMap = ortakIslemler.getLastParameter(sayfaURL, session);
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

		String linkAdresKey = (String) req.getParameter("linkAdresKey");
		if (veriLastMap != null) {
			if (veriLastMap.containsKey("basYil"))
				basYil = Integer.parseInt((String) veriLastMap.get("basYil"));
			if (veriLastMap.containsKey("basAy"))
				basAy = Integer.parseInt((String) veriLastMap.get("basAy"));
			if (veriLastMap.containsKey("bitYil"))
				bitYil = Integer.parseInt((String) veriLastMap.get("bitYil"));
			if (veriLastMap.containsKey("bitAy"))
				bitAy = Integer.parseInt((String) veriLastMap.get("bitAy"));
			if (veriLastMap.containsKey("sirketId"))
				sirketId = Long.parseLong((String) veriLastMap.get("sirketId"));
			if (veriLastMap.containsKey("tesisId"))
				tesisId = Long.parseLong((String) veriLastMap.get("tesisId"));
			if (veriLastMap.containsKey("bolumId"))
				bolumId = Long.parseLong((String) veriLastMap.get("bolumId"));
			if (veriLastMap.containsKey("istenAyrilanGoster"))
				istenAyrilanGoster = Boolean.getBoolean((String) veriLastMap.get("istenAyrilanGoster"));
			veriLastMap = null;
		}
		fillEkSahaTanim();
		ayDoldur(basYil, donemBas, false);
		ayDoldur(bitYil, donemBit, true);
		if (linkAdresKey != null) {
			personelDoldur();
			HashMap fields = new HashMap();
			fields.put("id", Long.parseLong(linkAdresKey));
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			PersonelDenklestirme pd = (PersonelDenklestirme) pdksEntityController.getObjectByInnerObject(fields, PersonelDenklestirme.class);
			if (pd != null)
				fillBilgileriDoldur(pd.getPdksPersonel());
		}

	}

	/**
	 * 
	 */
	@Transactional
	private void saveLastParameter() {
		LinkedHashMap<String, Object> lastMap = new LinkedHashMap<String, Object>();
		lastMap.put("basYil", "" + basYil);
		lastMap.put("bitYil", "" + bitYil);
		if (basAy != null)
			lastMap.put("basAy", "" + basAy);
		if (bitAy != null)
			lastMap.put("bitAy", "" + bitAy);
		if (sirketId != null)
			lastMap.put("sirketId", "" + sirketId);
		if (tesisId != null)
			lastMap.put("tesisId", "" + tesisId);
		if (bolumId != null)
			lastMap.put("bolumId", "" + bolumId);
		if (istenAyrilanGoster)
			lastMap.put("istenAyrilanGoster", "" + istenAyrilanGoster);
		lastMap.put("sayfaURL", sayfaURL);
		try {
			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception e) {

		}
	}

	/**
	 * @param aylikPuantaj
	 * @return
	 */
	@Transactional
	public String saveFazlaMesaiLastParameter(AylikPuantaj aylikPuantaj) {
		Map<String, String> map1 = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
		PersonelDenklestirme personelDenklestirme = aylikPuantaj.getPersonelDenklestirme();
		DenklestirmeAy denklestirmeAy = personelDenklestirme.getDenklestirmeAy();
		String adres = map1.containsKey("host") ? map1.get("host") : "";

		LinkedHashMap<String, Object> lastMap = new LinkedHashMap<String, Object>();
		lastMap.put("yil", "" + denklestirmeAy.getYil());
		lastMap.put("ay", "" + denklestirmeAy.getAy());
		Sirket sirket = seciliPersonel.getSirket();
		if (sirket.getDepartman() != null)
			lastMap.put("departmanId", "" + sirket.getDepartman().getId());
		if (sirket != null)
			lastMap.put("sirketId", "" + sirket.getId());
		if (sirket.getTesisDurum() && seciliPersonel.getTesis() != null)
			lastMap.put("tesisId", "" + seciliPersonel.getTesis().getId());
		if (seciliPersonel.getEkSaha3() != null)
			lastMap.put("bolumId", "" + seciliPersonel.getEkSaha3().getId());
		if (seciliPersonel.getEkSaha4() != null) {
			Tanim ekSaha4Tanim = ortakIslemler.getEkSaha4(sirket, sirketId, session);
			if (ekSaha4Tanim != null)
				lastMap.put("altBolumId", "" + seciliPersonel.getEkSaha4().getId());
		}

		lastMap.put("sicilNo", seciliPersonel.getPdksSicilNo());
		String sayfa = MenuItemConstant.fazlaMesaiHesapla;
		if (personelDenklestirme.getDurum().equals(Boolean.TRUE) || personelDenklestirme.isOnaylandi())
			lastMap.put("sayfaURL", FazlaMesaiHesaplaHome.sayfaURL);
		else {
			lastMap.put("sayfaURL", VardiyaGunHome.sayfaURL);
			sayfa = MenuItemConstant.vardiyaPlani;
		}

		bordroAdres = "<a href='http://" + adres + "/" + sayfaURL + "?linkAdresKey=" + aylikPuantaj.getPersonelDenklestirme().getId() + "'>" + ortakIslemler.getCalistiMenuAdi(sayfaURL) + " Ekranına Geri Dön</a>";
		try {
			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception e) {

		}
		return sayfa;
	}

	/**
	 * @param denklestirmeAy
	 * @param personel
	 * @return
	 */
	public PersonelDenklestirme getPersonelDenklestirme(DenklestirmeAy denklestirmeAy, Personel personel) {
		if (perDenkMap != null)
			denklestirme = perDenkMap.get(denklestirmeAy.getId() + "_" + personel.getId());
		else
			denklestirme = null;
		return denklestirme;
	}

	public String tesisDoldur() {
		personelList.clear();
		tesisler.clear();
		StringBuffer sb = new StringBuffer();
		bolumler.clear();
		try {
			sirket = null;
			boolean idVar = false;
			if (sirketId != null) {
				HashMap fields = new HashMap();
				fields.put("id", sirketId);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);
				fields.clear();
				if (sirket.getTesisDurum()) {
					sb.append("select DISTINCT TE.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
					sb.append(" INNER  JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
					sb.append(" AND PD." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + " = 1   ");
					personelSQLBagla(sb, fields);
					sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + " = " + sirketId);
					sb.append(" INNER JOIN " + Tanim.TABLE_NAME + " TE ON TE." + Tanim.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_TESIS);
					donemSQLKontrol(sb);
					sb.append(" AND ((D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+" + DenklestirmeAy.COLUMN_NAME_AY + ")<=:s");
					fields.put("s", sonDonem);
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<Tanim> list = pdksEntityController.getObjectBySQLList(sb, fields, Tanim.class);

					if (list.isEmpty()) {
						tesisId = null;
					} else {
						list = PdksUtil.sortObjectStringAlanList(list, "getAciklama", null);
						for (Tanim tanim : list) {
							if (tesisId != null && tanim.getId().equals(tesisId))
								idVar = true;
							tesisler.add(new SelectItem(tanim.getId(), tanim.getAciklama()));
						}
					}
				} else {
					bolumDoldur();
				}

			}
			if (!idVar)
				tesisId = null;
		} catch (Exception e) {
		}
		return "";
	}

	public String bolumDoldur() {
		personelList.clear();
		bolumler.clear();
		StringBuffer sb = new StringBuffer();
		try {
			boolean idVar = false;
			if (sirketId != null || tesisId != null) {
				HashMap fields = new HashMap();
				sb.append("select DISTINCT BO.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
				sb.append(" INNER  JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
				sb.append(" AND PD." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + " = 1   ");
				personelSQLBagla(sb, fields);
				if (tesisId != null) {
					sb.append(" AND P." + Personel.COLUMN_NAME_TESIS + " = " + tesisId);

				}
				if (sirket.getSirketGrup() == null) {
					sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + " = " + sirketId);
				} else {
					sb.append(" INNER JOIN " + Sirket.TABLE_NAME + " S ON S." + Sirket.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_SIRKET);
					sb.append(" AND S." + Sirket.COLUMN_NAME_SIRKET_GRUP + " = " + sirket.getSirketGrup().getId());
				}
				sb.append(" INNER JOIN " + Tanim.TABLE_NAME + " BO ON BO." + Tanim.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_EK_SAHA3);
				donemSQLKontrol(sb);
				sb.append(" AND ((D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+" + DenklestirmeAy.COLUMN_NAME_AY + ")<=:s");

				fields.put("s", sonDonem);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<Tanim> list = pdksEntityController.getObjectBySQLList(sb, fields, Tanim.class);

				if (list.isEmpty()) {
					bolumId = null;
				} else {
					list = PdksUtil.sortObjectStringAlanList(list, "getAciklama", null);
					if (list.size() == 1)
						bolumId = list.get(0).getId();
					for (Tanim tanim : list) {
						if (bolumId != null && tanim.getId().equals(bolumId))
							idVar = true;
						bolumler.add(new SelectItem(tanim.getId(), tanim.getAciklama()));
					}
					if (bolumId != null)
						personelList.clear();
				}
			}
			if (!idVar)
				bolumId = null;
		} catch (Exception e) {
		}
		return "";
	}

	/**
	 * @return
	 */
	public String personelDoldur() {
		personelList.clear();
		puantajList.clear();
		StringBuffer sb = new StringBuffer();
		try {
			if (bolumId != null) {
				HashMap fields = new HashMap();
				sb.append("select DISTINCT P.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
				sb.append(" INNER  JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
				sb.append(" AND PD." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + " = 1   ");
				personelSQLBagla(sb, fields);
				if (tesisId != null) {
					sb.append(" AND P." + Personel.COLUMN_NAME_TESIS + " = " + tesisId);

				}
				sb.append(" AND P." + Personel.COLUMN_NAME_EK_SAHA3 + " = " + bolumId);
				if (sirket.getSirketGrup() == null) {
					sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + " = " + sirketId);
				} else {
					sb.append(" INNER JOIN " + Sirket.TABLE_NAME + " S ON S." + Sirket.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_SIRKET);
					sb.append(" AND S." + Sirket.COLUMN_NAME_SIRKET_GRUP + " = " + sirket.getSirketGrup().getId());
				}
				donemSQLKontrol(sb);
				sb.append(" AND ((D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+" + DenklestirmeAy.COLUMN_NAME_AY + ")<=:s");
				fields.put("s", sonDonem);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				personelList = pdksEntityController.getObjectBySQLList(sb, fields, Personel.class);
				if (!personelList.isEmpty()) {
					personelList = PdksUtil.sortObjectStringAlanList(personelList, "getAdSoyad", null);
					List<Personel> ayrilanlar = new ArrayList<Personel>();
					Date tarih = PdksUtil.convertToJavaDate(String.valueOf(bitYil * 100 + bitAy) + "01", "yyyyMMdd");
					for (Iterator iterator = personelList.iterator(); iterator.hasNext();) {
						Personel personel = (Personel) iterator.next();
						if (!personel.isCalisiyorGun(tarih)) {
							ayrilanlar.add(personel);
							iterator.remove();
						}
					}
					if (!ayrilanlar.isEmpty())
						personelList.addAll(ayrilanlar);
					ayrilanlar = null;
				}

			}
			saveLastParameter();

		} catch (Exception e) {
		}

		return "";
	}

	/**
	 * @return
	 */
	public String sirketDoldur() {
		personelList.clear();
		perDenkMap.clear();
		sirketler.clear();
		bolumler.clear();
		denklestirmeAyList.clear();
		basTarih = basAy != null ? PdksUtil.convertToJavaDate(String.valueOf(basYil * 100 + basAy) + "01", "yyyyMMdd") : null;
		bitTarih = bitAy != null ? PdksUtil.tariheGunEkleCikar(PdksUtil.tariheAyEkleCikar(PdksUtil.convertToJavaDate(String.valueOf(bitYil * 100 + bitAy) + "01", "yyyyMMdd"), 1), -1) : null;
		StringBuffer sb = new StringBuffer();
		try {
			HashMap fields = new HashMap();
			sb.append("select DISTINCT S.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
			sb.append(" INNER  JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
			sb.append("  AND PD." + PersonelDenklestirme.COLUMN_NAME_DURUM + " = 1 AND  PD." + PersonelDenklestirme.COLUMN_NAME_ONAYLANDI + " = 1 AND  PD." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + " = 1 ");
			personelSQLBagla(sb, fields);
			sb.append(" INNER  JOIN " + Sirket.TABLE_NAME + " S ON S." + Sirket.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_SIRKET);
			donemSQLKontrol(sb);

			sb.append(" AND ((D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+" + DenklestirmeAy.COLUMN_NAME_AY + ")<=:s");

			fields.put("s", sonDonem);
			sb.append(" ORDER BY S." + Sirket.COLUMN_NAME_ID);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Sirket> sirketList = pdksEntityController.getObjectBySQLList(sb, fields, Sirket.class);
			if (!sirketList.isEmpty()) {
				if (sirketList.size() == 1)
					sirketId = sirketList.get(0).getId();
				else
					sirketList = PdksUtil.sortObjectStringAlanList(sirketList, "getAd", null);
				for (Sirket sirket : sirketList) {
					if (sirket.isPdksMi())
						sirketler.add(new SelectItem(sirket.getId(), sirket.getAd()));
				}
			}
			tesisDoldur();
		} catch (Exception e) {
			logger.error(sb.toString());
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * @param yil
	 * @param donemler
	 * @param donemDoldur
	 * @return
	 */
	public String ayDoldur(int yil, List<SelectItem> donemler, boolean donemDoldur) {
		if (donemler == null)
			donemler = new ArrayList<SelectItem>();
		else
			donemler.clear();
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("select DISTINCT D.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
		sb.append(" WHERE D." + DenklestirmeAy.COLUMN_NAME_YIL + "=:y");
		sb.append(" AND ((D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+ D." + DenklestirmeAy.COLUMN_NAME_AY + ")<=:s");
		sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_AY + " > 0");
		sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_DURUM + " = 0");
		sb.append(" ORDER BY D." + DenklestirmeAy.COLUMN_NAME_AY);
		fields.put("y", yil);
		fields.put("s", sonDonem);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<DenklestirmeAy> denkList = pdksEntityController.getObjectBySQLList(sb, fields, DenklestirmeAy.class);
		for (DenklestirmeAy da : denkList)
			donemler.add(new SelectItem(da.getAy(), da.getAyAdi()));
		denkList = null;
		if (donemDoldur)
			fillDonemDoldur();
		return "";
	}

	/**
	 * @return
	 */
	public String fillDonemDoldur() {
		personelList.clear();
		perDenkMap.clear();
		denklestirmeAyList.clear();
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("select DISTINCT D.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
		sb.append(" WHERE ((D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+ D." + DenklestirmeAy.COLUMN_NAME_AY + ")>=:y1");
		sb.append(" AND ((D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+ D." + DenklestirmeAy.COLUMN_NAME_AY + ")<=:y2");
		sb.append(" AND ((D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+ D." + DenklestirmeAy.COLUMN_NAME_AY + ")<=:s");
		sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_AY + " > 0");
		sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_DURUM + " = 0");
		sb.append(" ORDER BY D." + DenklestirmeAy.COLUMN_NAME_AY);
		fields.put("y1", basYil * 100 + (basAy != null ? basAy : 1));
		fields.put("y2", bitYil * 100 + (bitAy != null ? bitAy : 12));
		fields.put("s", sonDonem);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			sirketler.clear();
			List<DenklestirmeAy> denkList = pdksEntityController.getObjectBySQLList(sb, fields, DenklestirmeAy.class);
			List<Long> idList = new ArrayList<Long>();
			for (DenklestirmeAy denklestirmeAy : denkList) {
				idList.add(denklestirmeAy.getId());

			}
			if (!idList.isEmpty()) {
				String fieldName = "d";
				sb = new StringBuffer();
				sb.append("select DISTINCT S.* from " + PersonelDenklestirme.TABLE_NAME + " PD WITH(nolock) ");
				sb.append(" INNER  JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + " = PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
				sb.append(" INNER  JOIN " + Sirket.TABLE_NAME + " S ON S." + Sirket.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_SIRKET);
				sb.append(" WHERE PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " :" + fieldName);
				sb.append("   AND  PD." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + " = 1 ");
				sb.append(" ORDER BY S." + Sirket.COLUMN_NAME_ID);
				fields.put(fieldName, idList);
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				// List<Sirket> sirketList = pdksEntityController.getObjectBySQLList(sb, fields, Sirket.class);
				List<Sirket> sirketList = ortakIslemler.getSQLParamList(idList, sb, fieldName, fields, Sirket.class, session);
				if (!sirketList.isEmpty()) {
					sirketDoldur();
				}
			}
			denkList = null;
		} catch (Exception e) {
			logger.error(sb.toString());
			e.printStackTrace();
		}

		return "";
	}

	public String fazlaMesaiExcel() {
		String donemOrj = (seciliPersonel.getAdSoyad() + " " + seciliPersonel.getPdksSicilNo());
		String donem = basYil + " " + PdksUtil.getSelectItemLabel(bitAy, donemBas) + " - " + bitYil + " " + PdksUtil.getSelectItemLabel(bitAy, donemBit);
		try {
			ByteArrayOutputStream baosDosya = fazlaMesaiExcelDevam(donem);
			if (baosDosya != null) {
				String dosyaAdi = "PersonelCalismaDonem " + donemOrj + " _ " + donem.trim() + ".xlsx";
				PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi);
			}
		} catch (Exception e) {
			logger.error(e);
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
	 * @param donem
	 * @return
	 */
	private ByteArrayOutputStream fazlaMesaiExcelDevam(String donem) {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, donem + " Çalışma", Boolean.TRUE);
		Drawing drawing = sheet.createDrawingPatriarch();
		CreationHelper helper = wb.getCreationHelper();
		ClientAnchor anchor = helper.createClientAnchor();
		CellStyle izinBaslik = ExcelUtil.getStyleHeader(wb);
		CellStyle styleTutarEven = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleTutarOdd = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);

		CellStyle styleCenterEvenDay = ExcelUtil.getStyleDayEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleCenterOddDay = ExcelUtil.getStyleDayOdd(ExcelUtil.ALIGN_CENTER, wb);

		CellStyle styleDay = null, styleGenel = null, styleTutar = null, styleStrDay = null;
		CellStyle styleCenter = ExcelUtil.getStyleData(wb);
		CellStyle styleTatil = ExcelUtil.getStyleDataCenter(wb);

		CellStyle styleIstek = ExcelUtil.getStyleDataCenter(wb);
		CellStyle styleEgitim = ExcelUtil.getStyleDataCenter(wb);
		CellStyle styleOff = ExcelUtil.getStyleDataCenter(wb);
		ExcelUtil.setFontColor(styleOff, Color.WHITE);
		ExcelUtil.setFillForegroundColor(izinBaslik, 146, 208, 80);

		CellStyle styleIzin = ExcelUtil.getStyleDataCenter(wb);
		ExcelUtil.setFillForegroundColor(styleIzin, 146, 208, 80);

		CellStyle styleCalisma = ExcelUtil.getStyleDataCenter(wb);
		XSSFCellStyle header = (XSSFCellStyle) ExcelUtil.getStyleHeader(9, wb);

		ExcelUtil.setFillForegroundColor(styleTatil, 255, 153, 204);

		ExcelUtil.setFillForegroundColor(styleIstek, 255, 255, 0);

		ExcelUtil.setFillForegroundColor(styleCalisma, 255, 255, 255);

		ExcelUtil.setFillForegroundColor(styleEgitim, 0, 0, 255);

		ExcelUtil.setFillForegroundColor(styleOff, 13, 12, 89);
		ExcelUtil.setFontColor(styleOff, 256, 256, 256);

		int row = 0, col = 0;
		;

		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Dönem");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.calismaModeliAciklama());
		if (fazlaMesaiOde)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("FM Ödeme");
		if (fazlaMesaiIzinKullan)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("FM İzin Kullansın");

		Calendar cal = Calendar.getInstance();

		CellStyle headerVardiyaGun = ExcelUtil.getStyleHeader(9, wb);
		ExcelUtil.setFillForegroundColor(headerVardiyaGun, 99, 182, 153);

		CellStyle headerVardiyaTatilYarimGun = ExcelUtil.getStyleHeader(9, wb);
		ExcelUtil.setFontColor(headerVardiyaTatilYarimGun, 255, 255, 0);
		ExcelUtil.setFillForegroundColor(headerVardiyaTatilYarimGun, 144, 185, 63);

		CellStyle headerVardiyaTatilGun = ExcelUtil.getStyleHeader(9, wb);
		ExcelUtil.setFillForegroundColor(headerVardiyaTatilGun, 92, 127, 45);
		ExcelUtil.setFontColor(headerVardiyaTatilGun, 255, 255, 0);
		for (VardiyaGun vardiyaGun : gunAylikPuantaj.getVardiyalar()) {
			cal.setTime(vardiyaGun.getVardiyaDate());
			CellStyle headerVardiya = headerVardiyaGun;
			Cell cell = ExcelUtil.getCell(sheet, row, col++, headerVardiya);
			cell.setCellValue(authenticatedUser.getTarihFormatla(cal.getTime(), "EEE"));
		}
		Cell cell = ExcelUtil.getCell(sheet, row, col++, header);
		ExcelUtil.baslikCell(cell, anchor, helper, drawing, "TÇS", "Toplam Çalışma Saati: Çalışanın bu listedeki toplam çalışma saati");
		cell = ExcelUtil.getCell(sheet, row, col++, header);
		ExcelUtil.baslikCell(cell, anchor, helper, drawing, "ÇGS", "Çalışılması Gereken Saat: Çalışanın bu listede çalışması gereken saat");
		if (fazlaMesaiVar) {
			if (yasalFazlaCalismaAsanSaat) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.yasalFazlaCalismaAsanSaatKod(), "Yasal Çalışmayı Aşan Mesai : Saati aşan çalışma toplam miktarı");
			}
		}
		if (gerceklesenMesaiKod) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			ExcelUtil.baslikCell(cell, anchor, helper, drawing, "GM", "Gerçekleşen Mesai : Çalışanın bu listedeki eksi/fazla çalışma saati");
		}
		if (devredenMesaiKod) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.devredenMesaiKod(), "Devreden Mesai: Çalisanin önceki listelerden devreden eksi/fazla mesaisi");

		}
		if (fazlaMesaiVar) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			ExcelUtil.baslikCell(cell, anchor, helper, drawing, "ÜÖM", "Çalışanın bu listenin sonunda ücret olarak ödediğimiz fazla mesai saati");

			if (kismiOdemeGoster) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, "KÖM", "Çalışanın bu listenin sonunda ücret olarak kısmi ödediğimiz fazla mesai saati ");
			}

			if (resmiTatilVar) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, "RÖM", "Çalışanın bu listenin sonunda ücret olarak ödediğimiz resmi tatil mesai saati");
			}
			if (haftaTatilVar) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, AylikPuantaj.MESAI_TIPI_HAFTA_TATIL, "Çalışanın bu listenin sonunda ücret olarak ödediğimiz hafta tatil mesai saati");
			}
		}
		if (devredenBakiyeKod) {
			cell = ExcelUtil.getCell(sheet, row, col++, header);
			ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.devredenBakiyeKod(), "Bakiye: Çalışanın bu liste de dahil bugüne kadarki devreden eksi/fazla mesaisi");
		}
		CellStyle headerIzinTipi = (CellStyle) header.clone();
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
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.normalCalismaSaatKod(), "N.Çalışma Saat");
			}
			if (haftaTatilCalismaSaatKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerSaat);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.haftaTatilCalismaSaatKod(), "H.Tatil Saat");
			}
			if (resmiTatilCalismaSaatKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerSaat);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.resmiTatilCalismaSaatKod(), "R.Tatil Saat");
			}
			if (izinSureSaatKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerSaat);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.izinSureSaatKod(), "İzin Saat");
			}
			if (normalCalismaGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.normalCalismaGunKod(), "N.Çalışma Gün");
			}
			if (haftaTatilCalismaGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.haftaTatilCalismaGunKod(), "H.Tatil Gün");
			}
			if (resmiTatilCalismaGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.resmiTatilCalismaGunKod(), "R.Tatil Gün");
			}
			if (izinSureGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, header);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.izinSureGunKod(), "İzin Gün");
			}
			if (ucretliIzinGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerIzin);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.ucretliIzinGunKod(), "Ücretli İzin Gün");
			}
			if (ucretsizIzinGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerIzin);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.ucretsizIzinGunKod(), "Ücretsiz İzin Gün");
			}
			if (hastalikIzinGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerIzin);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.hastalikIzinGunKod(), "Hastalık İzin Gün");
			}
			if (normalGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerBGun);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.normalGunKod(), "Normal Gün");
			}
			if (haftaTatilGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerBGun);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.haftaTatilGunKod(), "H.Tatil Gün");
			}
			if (resmiTatilGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerBGun);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.resmiTatilGunKod(), "R.Tatil Gün");
			}
			if (artikGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerBGun);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.artikGunKod(), "Artık Gün");
			}
			if (bordroToplamGunKod) {
				cell = ExcelUtil.getCell(sheet, row, col++, headerBTGun);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, ortakIslemler.bordroToplamGunKod(), "Toplam Gün");
			}
		}
		if (denklestirmeDinamikAlanlar != null && !denklestirmeDinamikAlanlar.isEmpty()) {
			for (Tanim alan : denklestirmeDinamikAlanlar) {
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(alan.getAciklama());

			}
		}
		int ayAdet = 0;
		String pattern = PdksUtil.getDateFormat() + " EEEEE";
		for (Iterator iter = puantajList.iterator(); iter.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();
			PersonelDenklestirme personelDenklestirme = aylikPuantaj.getPersonelDenklestirme();
			DenklestirmeAy da = personelDenklestirme != null ? personelDenklestirme.getDenklestirmeAy() : aylikPuantaj.getDenklestirmeAy();
			if (ayAdet % 2 != 0) {
				styleCenter = styleOddCenter;
				styleStrDay = styleCenterOddDay;
				styleGenel = styleOdd;
				styleTutar = styleTutarOdd;
			} else {
				styleCenter = styleEvenCenter;
				styleStrDay = styleCenterEvenDay;
				styleGenel = styleEven;
				styleTutar = styleTutarEven;
			}
			ayAdet++;
			List<VardiyaGun> vardiyaList = aylikPuantaj.getVardiyalar();
			row++;
			col = 0;
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue("");
			ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			if (fazlaMesaiOde)
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			if (fazlaMesaiIzinKullan)
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			for (VardiyaGun vardiyaGun : vardiyaList) {
				if (vardiyaGun.isAyinGunu()) {
					cal.setTime(vardiyaGun.getVardiyaDate());
					CellStyle headerVardiya = headerVardiyaGun;
					String title = PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), pattern);
					if (vardiyaGun.getTatil() != null) {
						Tatil tatil = vardiyaGun.getTatil();
						title += "\n" + tatil.getAd();
						headerVardiya = tatil.isYarimGunMu() ? headerVardiyaTatilYarimGun : headerVardiyaTatilGun;
					}
					cell = ExcelUtil.getCell(sheet, row, col++, headerVardiya);
					cell.setCellValue(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
					if (PdksUtil.hasStringValue(title))
						ExcelUtil.setCellComment(cell, anchor, helper, drawing, title);

				} else
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");

			}
			ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			if (fazlaMesaiVar) {
				if (yasalFazlaCalismaAsanSaat)
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");

			}
			if (gerceklesenMesaiKod)
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			if (devredenMesaiKod)
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			if (fazlaMesaiVar) {
				if (kismiOdemeGoster)
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				if (resmiTatilVar)
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				if (haftaTatilVar)
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");

			}
			if (devredenBakiyeKod)
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
			if (bordroPuantajEkranindaGoster) {

				if (normalCalismaSaatKod) {
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}
				if (haftaTatilCalismaSaatKod) {
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}
				if (resmiTatilCalismaSaatKod) {
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}
				if (izinSureSaatKod) {
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}
				if (normalCalismaGunKod) {
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}
				if (haftaTatilCalismaGunKod) {
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}
				if (resmiTatilCalismaGunKod) {
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}
				if (izinSureGunKod) {
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}
				if (ucretliIzinGunKod) {
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}
				if (ucretsizIzinGunKod) {
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}
				if (hastalikIzinGunKod) {
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}
				if (normalGunKod) {
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}
				if (haftaTatilGunKod) {
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}
				if (resmiTatilGunKod) {
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}
				if (artikGunKod) {
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}
				if (bordroToplamGunKod) {
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}
			}
			if (denklestirmeDinamikAlanlar != null && !denklestirmeDinamikAlanlar.isEmpty()) {
				for (Tanim alan : denklestirmeDinamikAlanlar) {
					if (alan != null)
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}
			}
			if (personelDenklestirme != null) {
				CalismaModeli calismaModeli = aylikPuantaj.getCalismaModeli();
				PersonelDenklestirme personelDenklestirmeGecenAy = personelDenklestirme != null ? personelDenklestirme.getPersonelDenklestirmeGecenAy() : null;
				row++;
				col = 0;

				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(da.getAyAdi() + " " + da.getYil());
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(calismaModeli != null ? calismaModeli.getAciklama() : "");
				if (fazlaMesaiOde)
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(authenticatedUser.getYesNo(personelDenklestirme.getFazlaMesaiOde()));
				if (fazlaMesaiIzinKullan)
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(authenticatedUser.getYesNo(personelDenklestirme.getFazlaMesaiIzinKullan()));
				for (VardiyaGun vardiyaGun : vardiyaList) {
					if (vardiyaGun.isAyinGunu() && vardiyaGun.getDurum()) {
						String styleText = vardiyaGun.getAylikClassAdi(aylikPuantaj.getTrClass());
						styleDay = styleStrDay;
						if (styleText.equals(VardiyaGun.STYLE_CLASS_HAFTA_TATIL))
							styleDay = styleTatil;
						else if (styleText.equals(VardiyaGun.STYLE_CLASS_IZIN))
							styleDay = styleIzin;
						else if (styleText.equals(VardiyaGun.STYLE_CLASS_OZEL_ISTEK))
							styleDay = styleIstek;
						else if (styleText.equals(VardiyaGun.STYLE_CLASS_EGITIM))
							styleDay = styleEgitim;
						else if (styleText.equals(VardiyaGun.STYLE_CLASS_OFF))
							styleDay = styleOff;
						cell = ExcelUtil.getCell(sheet, row, col++, styleDay);
						String aciklama = calisan(vardiyaGun) ? vardiyaGun.getFazlaMesaiOzelAciklama(Boolean.TRUE, authenticatedUser.sayiFormatliGoster(vardiyaGun.getCalismaSuresi())) : "";
						cell.setCellValue(aciklama);
						String title = calisan(vardiyaGun) ? vardiyaGun.getTitle() : null;
						if (title != null) {
							if (vardiyaGun.getVardiya() != null && (vardiyaGun.getCalismaSuresi() > 0 || (vardiyaGun.getVardiya().isCalisma() && styleGenel == styleCalisma)))
								title = vardiyaGun.getVardiya().getKisaAdi() + " --> " + title;
							if (PdksUtil.hasStringValue(title))
								ExcelUtil.setCellComment(cell, anchor, helper, drawing, title);

						}

					} else
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");

				}
				setCell(sheet, row, col++, styleTutar, aylikPuantaj.getSaatToplami());
				Cell planlananCell = setCell(sheet, row, col++, styleTutar, aylikPuantaj.getPlanlananSure());
				if (aylikPuantaj.getCalismaModeliAy() != null && planlananCell != null && aylikPuantaj.getSutIzniDurum().equals(Boolean.FALSE)) {
					String title = aylikPuantaj.getCalismaModeli().getAciklama() + " : ";
					if (aylikPuantaj.getCalismaModeli().getToplamGunGuncelle().equals(Boolean.FALSE))
						title += authenticatedUser.sayiFormatliGoster(aylikPuantaj.getCalismaModeliAy().getSure());
					else
						title += authenticatedUser.sayiFormatliGoster(aylikPuantaj.getPersonelDenklestirme().getPlanlanSure());
					if (PdksUtil.hasStringValue(title))
						ExcelUtil.setCellComment(planlananCell, anchor, helper, drawing, title);
				}
				if (fazlaMesaiVar) {
					if (yasalFazlaCalismaAsanSaat) {
						if (aylikPuantaj.getUcretiOdenenMesaiSure() > 0)
							setCell(sheet, row, col++, styleTutar, aylikPuantaj.getUcretiOdenenMesaiSure());
						else
							ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue("");
					}
					setCell(sheet, row, col++, styleTutar, aylikPuantaj.getAylikNetFazlaMesai());
				}
				if (gerceklesenMesaiKod)
					setCell(sheet, row, col++, styleTutar, aylikPuantaj.getAylikNetFazlaMesai());
				if (devredenMesaiKod) {
					Double gecenAyFazlaMesai = aylikPuantaj.getGecenAyFazlaMesai(authenticatedUser);
					Cell gecenAyFazlaMesaiCell = setCell(sheet, row, col++, styleTutar, gecenAyFazlaMesai);
					if (gecenAyFazlaMesai != null && personelDenklestirmeGecenAy != null && gecenAyFazlaMesai.doubleValue() != 0.0d) {
						if (personelDenklestirmeGecenAy.getGuncelleyenUser() != null && personelDenklestirmeGecenAy.getGuncellemeTarihi() != null) {
							String title = "Onaylayan : " + personelDenklestirmeGecenAy.getGuncelleyenUser().getAdSoyad() + "\n";
							title += "Zaman : " + authenticatedUser.dateTimeFormatla(personelDenklestirmeGecenAy.getGuncellemeTarihi());
							ExcelUtil.setCellComment(gecenAyFazlaMesaiCell, anchor, helper, drawing, title);
						}
					}
				}
				boolean olustur = false;
				Comment commentGuncelleyen = null;
				if (fazlaMesaiVar) {

					if (aylikPuantaj.isFazlaMesaiHesapla()) {
						Cell fazlaMesaiSureCell = setCell(sheet, row, col++, styleTutar, aylikPuantaj.getFazlaMesaiSure());
						if (aylikPuantaj.getFazlaMesaiSure() != 0.0d) {
							if (personelDenklestirme.getGuncelleyenUser() != null && personelDenklestirme.getGuncellemeTarihi() != null)
								commentGuncelleyen = fazlaMesaiOrtakIslemler.getCommentGuncelleyen(anchor, helper, drawing, personelDenklestirme);
							fazlaMesaiSureCell.setCellComment(commentGuncelleyen);
							olustur = true;
						}
					} else
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
					if (kismiOdemeGoster) {
						if (personelDenklestirme.getKismiOdemeSure() != null && personelDenklestirme.getKismiOdemeSure().doubleValue() > 0.0d)
							setCell(sheet, row, col++, styleTutar, personelDenklestirme.getKismiOdemeSure());
						else
							ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
					}
					if (resmiTatilVar)
						setCell(sheet, row, col++, styleTutar, aylikPuantaj.getResmiTatilToplami());
					if (haftaTatilVar)
						setCell(sheet, row, col++, styleTutar, aylikPuantaj.getHaftaCalismaSuresi());

				}
				if (devredenBakiyeKod) {
					if (aylikPuantaj.isFazlaMesaiHesapla()) {
						Cell devredenSureCell = setCell(sheet, row, col++, styleTutar, aylikPuantaj.getDevredenSure());
						if (aylikPuantaj.getDevredenSure() != null && aylikPuantaj.getDevredenSure().doubleValue() != 0.0d && commentGuncelleyen == null) {
							if (olustur)
								commentGuncelleyen = fazlaMesaiOrtakIslemler.getCommentGuncelleyen(anchor, helper, drawing, personelDenklestirme);
							if (commentGuncelleyen != null)
								devredenSureCell.setCellComment(commentGuncelleyen);
						}
					} else
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}

				if (bordroPuantajEkranindaGoster) {
					PersonelDenklestirmeBordro denklestirmeBordro = aylikPuantaj.getDenklestirmeBordro();
					if (denklestirmeBordro == null) {
						denklestirmeBordro = new PersonelDenklestirmeBordro();
						denklestirmeBordro.setPersonelDenklestirme(aylikPuantaj.getPersonelDenklestirme());
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
				if (denklestirmeDinamikAlanlar != null && !denklestirmeDinamikAlanlar.isEmpty()) {
					for (Tanim alan : denklestirmeDinamikAlanlar) {
						PersonelDenklestirmeDinamikAlan denklestirmeDinamikAlan = aylikPuantaj.getDinamikAlan(alan.getId());
						if (denklestirmeDinamikAlan == null)
							ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
						else {
							if (denklestirmeDinamikAlan.isDevamlilikPrimi())
								ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(denklestirmeDinamikAlan.getIslemDurum() ? "+" : "-");
							else {
								String str = authenticatedUser.getYesNo(denklestirmeDinamikAlan.getIslemDurum());
								if (denklestirmeDinamikAlan.getSayisalDeger() != null && denklestirmeDinamikAlan.getSayisalDeger().doubleValue() > 0.0d) {
									String deger = authenticatedUser.sayiFormatliGoster(denklestirmeDinamikAlan.getSayisalDeger());
									if (denklestirmeDinamikAlan.isIzinDurum())
										str += "\nSüre : " + deger;
									else
										str += "\n " + deger;
									ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(str);
								}
							}
						}

					}
				}

			}
			styleGenel = null;
		}
		for (int i = 0; i <= col; i++)
			sheet.autoSizeColumn(i);
		baos = new ByteArrayOutputStream();
		try {
			wb.write(baos);
		} catch (IOException e) {
			e.printStackTrace();
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
	 * @return
	 */
	public String fillBilgileriDoldur(Personel personel) {
		gunAylikPuantaj = null;
		puantajList.clear();
		seciliPersonel = personel;
		perDenkMap.clear();
		fazlaMesaiOde = false;
		fazlaMesaiIzinKullan = false;
		resmiTatilVar = false;
		haftaTatilVar = false;
		kismiOdemeGoster = false;
		session.clear();
		baslikMap.clear();
		bordroPuantajEkranindaGoster = ortakIslemler.getParameterKey("bordroPuantajEkranindaGoster").equals("1");
		yasalFazlaCalismaAsanSaat = false;
		StringBuffer sb = new StringBuffer();
		HashMap fields = new HashMap();
		sb.append("select PD.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
		sb.append(" INNER  JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
		sb.append(" AND   PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + "= " + personel.getId());
		personelSQLBagla(sb, fields);

		donemSQLKontrol(sb);
		sb.append(" ORDER BY D." + DenklestirmeAy.COLUMN_NAME_YIL + " , D." + DenklestirmeAy.COLUMN_NAME_AY);

		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PersonelDenklestirme> list = pdksEntityController.getObjectBySQLList(sb, fields, PersonelDenklestirme.class);

		if (!list.isEmpty()) {
			fields.clear();
			sb = new StringBuffer();
			sb.append("select  SUM(COALESCE(PD." + PersonelDenklestirme.COLUMN_NAME_RESMI_TATIL_SURE + ",0)) " + PersonelDenklestirme.COLUMN_NAME_RESMI_TATIL_SURE + ", ");
			sb.append("  SUM(COALESCE(PD." + PersonelDenklestirme.COLUMN_NAME_HAFTA_TATIL_SURE + ",0)) " + PersonelDenklestirme.COLUMN_NAME_HAFTA_TATIL_SURE + ", ");
			sb.append("  SUM(PD." + PersonelDenklestirme.COLUMN_NAME_FAZLA_MESAI_IZIN_KULLAN + ") " + PersonelDenklestirme.COLUMN_NAME_FAZLA_MESAI_IZIN_KULLAN + ", ");
			sb.append("  SUM(PD." + PersonelDenklestirme.COLUMN_NAME_FAZLA_MESAI_ODE + ") " + PersonelDenklestirme.COLUMN_NAME_FAZLA_MESAI_ODE + ", ");
			sb.append("  SUM(COALESCE(PD." + PersonelDenklestirme.COLUMN_NAME_KISMI_ODEME_SAAT + ",0)) " + PersonelDenklestirme.COLUMN_NAME_KISMI_ODEME_SAAT + " from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
			sb.append(" INNER  JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
			sb.append(" AND   PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + "= " + personel.getId());
			personelSQLBagla(sb, fields);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Object[]> list2 = pdksEntityController.getObjectBySQLList(sb, fields, null);
			if (!list2.isEmpty()) {
				Object[] data = list2.get(0);
				resmiTatilVar = ((Double) data[0]).doubleValue() > 0d;
				haftaTatilVar = ((Double) data[1]).doubleValue() > 0d;
				fazlaMesaiIzinKullan = ((Integer) data[2]).intValue() > 0;
				fazlaMesaiOde = ((Integer) data[3]).intValue() > 0;
				kismiOdemeGoster = ((BigDecimal) data[4]).doubleValue() > 0d;
			}
			List<AylikPuantaj> dataList = new ArrayList<AylikPuantaj>();
			Calendar cal = Calendar.getInstance();
			Date b1 = personel.getIseBaslamaTarihi().after(basTarih) ? personel.getIseBaslamaTarihi() : basTarih;
			Date b2 = personel.getSskCikisTarihi().after(bitTarih) ? bitTarih : personel.getSskCikisTarihi();
			List<Personel> perList = new ArrayList<Personel>();
			perList.add(seciliPersonel);
			TreeMap<String, VardiyaGun> vardiyaMap = null;
			try {
				vardiyaMap = ortakIslemler.getVardiyalar(perList, ortakIslemler.tariheGunEkleCikar(cal, b1, -6), ortakIslemler.tariheGunEkleCikar(cal, b2, 6), null, Boolean.FALSE, session, Boolean.FALSE);

			} catch (Exception e) {
				vardiyaMap = new TreeMap<String, VardiyaGun>();
			}
			TreeMap<String, Tatil> tatilMap = ortakIslemler.getTatilGunleri(perList, b1, b2, session);
			int sonGun = 0;
			Date bugun = new Date();
			boolean renk = Boolean.TRUE;
			fazlaMesaiVar = false;
			saatlikMesaiVar = false;
			aylikMesaiVar = false;
			for (PersonelDenklestirme pd : list) {
				double puantajHaftaTatil = 0.0d;
				DenklestirmeAy da = pd.getDenklestirmeAy();
				int yil = da.getYil();
				int ay = da.getAy();
				b1 = PdksUtil.convertToJavaDate(String.valueOf(yil * 100 + ay) + "01", "yyyyMMdd");
				b2 = PdksUtil.tariheGunEkleCikar(PdksUtil.tariheAyEkleCikar(b1, 1), -1);
				cal.setTime(b1);
				int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
				if (dayOfWeek != Calendar.MONDAY) {
					if (dayOfWeek != Calendar.SUNDAY)
						cal.add(Calendar.DATE, 2 - dayOfWeek);
					else
						cal.add(Calendar.DATE, -6);
					b1 = cal.getTime();
				}
				cal.setTime(b2);
				dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
				if (dayOfWeek != Calendar.SUNDAY) {
					cal.add(Calendar.DATE, 8 - dayOfWeek);
					b2 = cal.getTime();
				}
				cal.setTime(b1);
				Date tarih = cal.getTime();
				String donem = String.valueOf(yil * 100 + da.getAy());
				TreeMap<String, VardiyaGun> vgPerMap = new TreeMap<String, VardiyaGun>();
				boolean ayBasladi = false;
				double ucretiOdenenMesaiSure = 0.0d, fazlaMesaiMaxSure = da.getFazlaMesaiMaxSure();
				boolean fazlaMesaiOdenir = pd.getCalismaModeliAy() != null && pd.getCalismaModeliAy().isGunMaxCalismaOdenir();
				while (tarih.getTime() <= b2.getTime()) {
					VardiyaGun vg = new VardiyaGun(seciliPersonel, null, tarih);
					String key = vg.getVardiyaDateStr();
					if (tatilMap.containsKey(key))
						vg.setTatil(tatilMap.get(key));
					String perKey = vg.getVardiyaKeyStr();
					if (vg.getPdksPersonel() != null) {
						if (vardiyaMap.containsKey(perKey)) {
							vg = (VardiyaGun) vardiyaMap.get(perKey).clone();
							vardiyaGun = vg;
							Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
							if (vardiyaGun.getPersonel().isCalisiyorGun(vardiyaGun.getVardiyaDate())) {
								try {
									boolean zamanGelmedi = !bugun.after(islemVardiya.getVardiyaTelorans2BitZaman());
									if (!zamanGelmedi)
										zamanGelmedi = islemVardiya.isCalisma() == false || vardiyaGun.isIzinli();
									else if (islemVardiya.isCalisma())
										vardiyaGun.setCalismaSuresi(islemVardiya.getNetCalismaSuresi());

									vardiyaGun.setZamanGelmedi(zamanGelmedi);
								} catch (Exception e) {
								}
							}
							if (vardiyaGun.getVardiyaSaatDB() != null) {
								VardiyaSaat vardiyaSaatDB = vardiyaGun.getVardiyaSaatDB();
								if (fazlaMesaiOdenir) {
									if (vardiyaGun.getCalismaSuresi() > fazlaMesaiMaxSure)
										ucretiOdenenMesaiSure += vardiyaGun.getCalismaSuresi() - fazlaMesaiMaxSure;

								}
								if (vardiyaSaatDB.getResmiTatilSure() > 0.0d)
									vardiyaGun.setResmiTatilSure(vardiyaSaatDB.getResmiTatilSure());
								else if (pd.getHaftaCalismaSuresi() != null && vardiyaGun.getVardiya().isHaftaTatil() && pd.getHaftaCalismaSuresi() > 0.0d) {
									puantajHaftaTatil += vardiyaSaatDB.getCalismaSuresi();
									vardiyaGun.setHaftaCalismaSuresi(vardiyaSaatDB.getCalismaSuresi());
								}

								vardiyaGun.setCalismaSuresi(vardiyaSaatDB.getCalismaSuresi());

							}
						}

					}

					vg.setAyinGunu(key.startsWith(donem));
					if (vg.isAyinGunu() == false) {
						if (ayBasladi) {
							break;
						}
					} else
						ayBasladi = true;
					vgPerMap.put(key, vg);
					cal.add(Calendar.DATE, 1);
					tarih = cal.getTime();

				}
				List<VardiyaGun> gunList = new ArrayList<VardiyaGun>(vgPerMap.values());
				AylikPuantaj aylikPuantaj = new AylikPuantaj();
				aylikPuantaj.setPersonelDenklestirmeData(pd);
				CalismaModeli cm = aylikPuantaj.getCalismaModeli();
				if (!fazlaMesaiVar)
					fazlaMesaiVar = cm.isFazlaMesaiVarMi() && cm.isAylikOdeme();
				if (bordroPuantajEkranindaGoster) {
					if (!saatlikMesaiVar)
						saatlikMesaiVar = cm.isSaatlikOdeme();
					if (!aylikMesaiVar)
						aylikMesaiVar = cm.isAylikOdeme();
				}
				aylikPuantaj.setTrClass(renk ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
				renk = !renk;
				aylikPuantaj.setHaftaCalismaSuresi(puantajHaftaTatil);
				aylikPuantaj.setVardiyalar(gunList);
				aylikPuantaj.setVgMap(vgPerMap);
				aylikPuantaj.setUcretiOdenenMesaiSure(ucretiOdenenMesaiSure);
				dataList.add(aylikPuantaj);
				if (gunList.size() > sonGun)
					sonGun = gunList.size();

			}
			if (!fazlaMesaiVar) {
				fazlaMesaiOde = false;
				fazlaMesaiIzinKullan = false;
			}
			if (!dataList.isEmpty()) {
				denklestirmeDinamikAlanlar = ortakIslemler.setDenklestirmeDinamikDurum(dataList, session);
				if (bordroPuantajEkranindaGoster)
					fazlaMesaiOrtakIslemler.setAylikPuantajBordroVeri(dataList, session);

				for (AylikPuantaj dap : dataList) {
					PersonelDenklestirme personelDenklestirme = dap.getPersonelDenklestirme();
					dap.setPlanlananSure(personelDenklestirme.getPlanlanSure());
					List<VardiyaGun> gunList = dap.getVardiyalar();
					int fark = sonGun - gunList.size();
					if (fark > 0) {
						Date tarih = gunList.get(gunList.size() - 1).getVardiyaDate();
						cal.setTime(tarih);
						for (int i = 0; i < fark; i++) {
							cal.add(Calendar.DATE, 1);
							tarih = cal.getTime();
							VardiyaGun vg = new VardiyaGun(seciliPersonel.isCalisiyorGun(tarih) ? seciliPersonel : null, null, tarih);
							if (vg.getPdksPersonel() != null && vardiyaMap.containsKey(vg.getVardiyaKeyStr()))
								vg = vardiyaMap.get(vg.getVardiyaKeyStr());
							vg.setAyinGunu(Boolean.FALSE);
							gunList.add(vg);
						}
					}
					gunAylikPuantaj = dap;

				}
				bordroVeriOlusturBasla(dataList, tatilMap);
			}
			setPuantajList(dataList);
		}

		return "";
	}

	/**
	 * @param puantajList
	 * @param tatilMap
	 */
	private void bordroVeriOlusturBasla(List<AylikPuantaj> puantajList, TreeMap<String, Tatil> tatilMap) {
		baslikMap.clear();
		boolean saatlikCalismaVar = ortakIslemler.getParameterKey("saatlikCalismaVar").equals("1");
		for (AylikPuantaj ap : puantajList) {
			PersonelDenklestirme personelDenklestirme = ap.getPersonelDenklestirme();
			double izinGunAdet = 0.0;
			ap.setPlanlananSure(personelDenklestirme.getPlanlanSure());
			if (ap.getVardiyalar() != null) {
				for (VardiyaGun vg : ap.getVardiyalar()) {
					if (vg.isAyinGunu() && vg.getIzin() != null && vg.getTatil() == null) {
						IzinTipi izinTipi = vg.getIzin().getIzinTipi();
						if (izinTipi.isUcretliIzinTipi() && !tatilMap.containsKey(vg.getVardiyaDateStr()))
							izinGunAdet += 1.0d;
					}

				}
			}

			CalismaModeli calismaModeli = ap.getCalismaModeli();
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
			PersonelDenklestirmeBordro denklestirmeBordro = ap.getDenklestirmeBordro();
			boolean saatlikCalisma = calismaModeli.isSaatlikOdeme();

			if (denklestirmeBordro != null) {
				if (saatlikCalismaVar) {
					String keyEk = saatlikCalisma ? "" : "G";
					fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.normalCalismaSaatKod() + keyEk, denklestirmeBordro.getSaatNormal());
					fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.haftaTatilCalismaSaatKod() + keyEk, denklestirmeBordro.getSaatHaftaTatil());
					fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.resmiTatilCalismaSaatKod() + keyEk, denklestirmeBordro.getSaatResmiTatil());
					fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.izinSureSaatKod() + keyEk, denklestirmeBordro.getSaatIzin());
					fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.izinSureGunAdetKod(), izinGunAdet);
				}
				fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.ucretliIzinGunKod(), denklestirmeBordro.getUcretliIzin().doubleValue());
				fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.ucretsizIzinGunKod(), denklestirmeBordro.getUcretsizIzin().doubleValue());
				fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.hastalikIzinGunKod(), denklestirmeBordro.getRaporluIzin().doubleValue());
				fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.normalGunKod(), denklestirmeBordro.getNormalGunAdet());
				fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.haftaTatilGunKod(), denklestirmeBordro.getHaftaTatilAdet());
				fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.artikGunKod(), denklestirmeBordro.getArtikAdet());
				fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.resmiTatilGunKod(), denklestirmeBordro.getResmiTatilAdet());
				fazlaMesaiOrtakIslemler.baslikGuncelle(baslikMap, ortakIslemler.bordroToplamGunKod(), denklestirmeBordro.getBordroToplamGunAdet());
			}

		}

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
		bordroToplamGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.bordroToplamGunKod());
		haftaTatilGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.haftaTatilGunKod());
		resmiTatilGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.resmiTatilGunKod());
		artikGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.artikGunKod());
		artikGunKod = bordroPuantajEkranindaGoster || baslikMap.containsKey(ortakIslemler.artikGunKod());
		if (!devredenMesaiKod || !devredenBakiyeKod) {
			for (AylikPuantaj ap : puantajList) {
				if (ap.getCalismaModeli().isSaatlikOdeme()) {
					double gecenAyFazlaMesai = ap.getGecenAyFazlaMesai(authenticatedUser);
					double devredenSure = ap.getDevredenSure();
					if (!devredenMesaiKod)
						devredenMesaiKod = gecenAyFazlaMesai != 0.0d;
					if (!!devredenBakiyeKod)
						devredenBakiyeKod = devredenSure != 0.0d;
				}
			}
		}
	}

	/**
	 * @param sb
	 */
	private void donemSQLKontrol(StringBuffer sb) {
		if (basYil == bitYil) {
			sb.append(" WHERE D." + DenklestirmeAy.COLUMN_NAME_YIL + " = " + basYil);
			if (basAy != null) {
				sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_AY + " >= " + basAy);
			}
			if (bitAy != null) {
				sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_AY + " <= " + bitAy);
			}
		} else {
			int donem1 = basYil * 100 + (basAy != null ? basAy : 1);
			int donem2 = bitYil * 100 + (bitAy != null ? bitAy : 12);
			sb.append(" WHERE  (100 * D." + DenklestirmeAy.COLUMN_NAME_YIL + " + D." + DenklestirmeAy.COLUMN_NAME_AY + " ) > = " + donem1);
			sb.append(" AND  (100 * D." + DenklestirmeAy.COLUMN_NAME_YIL + " + D." + DenklestirmeAy.COLUMN_NAME_AY + " ) < = " + donem2);
		}

		sb.append(" AND D." + DenklestirmeAy.COLUMN_NAME_DURUM + " = 0");
	}

	/**
	 * @param vardiyaGun
	 * @return
	 */
	protected boolean calisan(VardiyaGun vardiyaGun) {
		boolean calisan = vardiyaGun != null;
		if (calisan) {
			if (vardiyaGun.getVardiya() != null) {

				calisan = vardiyaGun.isKullaniciYetkili() || (vardiyaGun.getIzin() != null && !helpPersonel(vardiyaGun.getPersonel()));
			}
		}
		return calisan;
	}

	/**
	 * @param personel
	 * @return
	 */
	private boolean helpPersonel(Personel personel) {
		return false;

	}

	/**
	 * @param sb
	 * @param fields
	 */
	private void personelSQLBagla(StringBuffer sb, HashMap fields) {
		sb.append(" INNER  JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + " = PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
		if (basTarih != null && bitTarih != null) {
			sb.append(" AND P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " <= :b2  AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= :b1 ");
			fields.put("b1", basTarih);
			fields.put("b2", bitTarih);
		}
		if (istenAyrilanGoster == false) {
			sb.append(" AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= :a");
			fields.put("a", bitTarih);
		}
		if (authenticatedUser.getYetkiliTesisler() != null && !authenticatedUser.getYetkiliTesisler().isEmpty()) {
			List<Long> idList = new ArrayList<Long>();
			for (Tanim tanim : authenticatedUser.getYetkiliTesisler()) {
				if (tanim.getId() != null)
					idList.add(tanim.getId());
			}
			if (!idList.isEmpty()) {
				if (idList.size() > 1) {
					sb.append(" AND P." + Personel.COLUMN_NAME_TESIS + " :te ");
					fields.put("te", idList);

				} else
					sb.append(" AND P." + Personel.COLUMN_NAME_TESIS + " = " + idList.get(0));
			}
		}
	}

	public Integer getBasAy() {
		return basAy;
	}

	public void setBasAy(Integer basAy) {
		this.basAy = basAy;
	}

	public Integer getBitAy() {
		return bitAy;
	}

	public void setBitAy(Integer bitAy) {
		this.bitAy = bitAy;
	}

	public List<SelectItem> getSirketler() {
		return sirketler;
	}

	public void setSirketler(List<SelectItem> sirketler) {
		this.sirketler = sirketler;
	}

	public Long getSirketId() {
		return sirketId;
	}

	public void setSirketId(Long sirketId) {
		this.sirketId = sirketId;
	}

	public Integer getMaxYil() {
		return maxYil;
	}

	public void setMaxYil(Integer maxYil) {
		this.maxYil = maxYil;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public List<PersonelDenklestirme> getPerDenkList() {
		return perDenkList;
	}

	public void setPerDenkList(List<PersonelDenklestirme> perDenkList) {
		this.perDenkList = perDenkList;
	}

	public List<Personel> getPersonelList() {
		return personelList;
	}

	public void setPersonelList(List<Personel> personelList) {
		this.personelList = personelList;
	}

	public HashMap<String, PersonelDenklestirme> getPerDenkMap() {
		return perDenkMap;
	}

	public void setPerDenkMap(HashMap<String, PersonelDenklestirme> perDenkMap) {
		this.perDenkMap = perDenkMap;
	}

	public PersonelDenklestirme getDenklestirme() {
		return denklestirme;
	}

	public void setDenklestirme(PersonelDenklestirme denklestirme) {
		this.denklestirme = denklestirme;
	}

	public List<DenklestirmeAy> getDenklestirmeAyList() {
		return denklestirmeAyList;
	}

	public void setDenklestirmeAyList(List<DenklestirmeAy> denklestirmeAyList) {
		this.denklestirmeAyList = denklestirmeAyList;
	}

	public Long getTesisId() {
		return tesisId;
	}

	public void setTesisId(Long tesisId) {
		this.tesisId = tesisId;
	}

	public List<SelectItem> getTesisler() {
		return tesisler;
	}

	public void setTesisler(List<SelectItem> tesisler) {
		this.tesisler = tesisler;
	}

	public boolean isTesisVar() {
		return tesisVar;
	}

	public void setTesisVar(boolean tesisVar) {
		this.tesisVar = tesisVar;
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

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
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

	public List<SelectItem> getBolumler() {
		return bolumler;
	}

	public void setBolumler(List<SelectItem> bolumler) {
		this.bolumler = bolumler;
	}

	public Long getBolumId() {
		return bolumId;
	}

	public void setBolumId(Long bolumId) {
		this.bolumId = bolumId;
	}

	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket sirket) {
		this.sirket = sirket;
	}

	public List<AylikPuantaj> getPuantajList() {
		return puantajList;
	}

	public void setPuantajList(List<AylikPuantaj> puantajList) {
		this.puantajList = puantajList;
	}

	public Personel getSeciliPersonel() {
		return seciliPersonel;
	}

	public void setSeciliPersonel(Personel seciliPersonel) {
		this.seciliPersonel = seciliPersonel;
	}

	public VardiyaGun getVardiyaGun() {
		return vardiyaGun;
	}

	public void setVardiyaGun(VardiyaGun vardiyaGun) {
		this.vardiyaGun = vardiyaGun;
	}

	public AylikPuantaj getGunAylikPuantaj() {
		return gunAylikPuantaj;
	}

	public void setGunAylikPuantaj(AylikPuantaj gunAylikPuantaj) {
		this.gunAylikPuantaj = gunAylikPuantaj;
	}

	public boolean isFazlaMesaiOde() {
		return fazlaMesaiOde;
	}

	public void setFazlaMesaiOde(boolean fazlaMesaiOde) {
		this.fazlaMesaiOde = fazlaMesaiOde;
	}

	public boolean isFazlaMesaiIzinKullan() {
		return fazlaMesaiIzinKullan;
	}

	public void setFazlaMesaiIzinKullan(boolean fazlaMesaiIzinKullan) {
		this.fazlaMesaiIzinKullan = fazlaMesaiIzinKullan;
	}

	public boolean isResmiTatilVar() {
		return resmiTatilVar;
	}

	public void setResmiTatilVar(boolean resmiTatilVar) {
		this.resmiTatilVar = resmiTatilVar;
	}

	public boolean isHaftaTatilVar() {
		return haftaTatilVar;
	}

	public void setHaftaTatilVar(boolean haftaTatilVar) {
		this.haftaTatilVar = haftaTatilVar;
	}

	public Integer getBasYil() {
		return basYil;
	}

	public void setBasYil(Integer basYil) {
		this.basYil = basYil;
	}

	public Integer getBitYil() {
		return bitYil;
	}

	public void setBitYil(Integer bitYil) {
		this.bitYil = bitYil;
	}

	public List<SelectItem> getDonemBas() {
		return donemBas;
	}

	public void setDonemBas(List<SelectItem> donemBas) {
		this.donemBas = donemBas;
	}

	public List<SelectItem> getDonemBit() {
		return donemBit;
	}

	public void setDonemBit(List<SelectItem> donemBit) {
		this.donemBit = donemBit;
	}

	public boolean isKismiOdemeGoster() {
		return kismiOdemeGoster;
	}

	public void setKismiOdemeGoster(boolean kismiOdemeGoster) {
		this.kismiOdemeGoster = kismiOdemeGoster;
	}

	public boolean isYasalFazlaCalismaAsanSaat() {
		return yasalFazlaCalismaAsanSaat;
	}

	public void setYasalFazlaCalismaAsanSaat(boolean yasalFazlaCalismaAsanSaat) {
		this.yasalFazlaCalismaAsanSaat = yasalFazlaCalismaAsanSaat;
	}

	public boolean isFazlaMesaiVar() {
		return fazlaMesaiVar;
	}

	public void setFazlaMesaiVar(boolean fazlaMesaiVar) {
		this.fazlaMesaiVar = fazlaMesaiVar;
	}

	public boolean isSaatlikMesaiVar() {
		return saatlikMesaiVar;
	}

	public void setSaatlikMesaiVar(boolean saatlikMesaiVar) {
		this.saatlikMesaiVar = saatlikMesaiVar;
	}

	public boolean isAylikMesaiVar() {
		return aylikMesaiVar;
	}

	public void setAylikMesaiVar(boolean aylikMesaiVar) {
		this.aylikMesaiVar = aylikMesaiVar;
	}

	public boolean isBordroPuantajEkranindaGoster() {
		return bordroPuantajEkranindaGoster;
	}

	public void setBordroPuantajEkranindaGoster(boolean bordroPuantajEkranindaGoster) {
		this.bordroPuantajEkranindaGoster = bordroPuantajEkranindaGoster;
	}

	public Integer getSonDonem() {
		return sonDonem;
	}

	public void setSonDonem(Integer sonDonem) {
		this.sonDonem = sonDonem;
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

	public Boolean getArtikGunKod() {
		return artikGunKod;
	}

	public void setArtikGunKod(Boolean artikGunKod) {
		this.artikGunKod = artikGunKod;
	}

	public Boolean getBordroToplamGunKod() {
		return bordroToplamGunKod;
	}

	public void setBordroToplamGunKod(Boolean bordroToplamGunKod) {
		this.bordroToplamGunKod = bordroToplamGunKod;
	}

	public Boolean getDevredenMesaiKod() {
		return devredenMesaiKod;
	}

	public void setDevredenMesaiKod(Boolean devredenMesaiKod) {
		this.devredenMesaiKod = devredenMesaiKod;
	}

	public Boolean getUcretiOdenenKod() {
		return ucretiOdenenKod;
	}

	public void setUcretiOdenenKod(Boolean ucretiOdenenKod) {
		this.ucretiOdenenKod = ucretiOdenenKod;
	}

	public TreeMap<String, Boolean> getBaslikMap() {
		return baslikMap;
	}

	public void setBaslikMap(TreeMap<String, Boolean> baslikMap) {
		this.baslikMap = baslikMap;
	}

	public boolean isIstenAyrilanGoster() {
		return istenAyrilanGoster;
	}

	public void setIstenAyrilanGoster(boolean istenAyrilanGoster) {
		this.istenAyrilanGoster = istenAyrilanGoster;
	}

	public List<Tanim> getDenklestirmeDinamikAlanlar() {
		return denklestirmeDinamikAlanlar;
	}

	public void setDenklestirmeDinamikAlanlar(List<Tanim> denklestirmeDinamikAlanlar) {
		this.denklestirmeDinamikAlanlar = denklestirmeDinamikAlanlar;
	}

}
