package org.pdks.session;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.mail.internet.InternetAddress;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.BindingProvider;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.ArifeVardiyaDonem;
import org.pdks.entity.AylikPuantaj;
import org.pdks.entity.BaseObject;
import org.pdks.entity.BasePDKSObject;
import org.pdks.entity.BasitHareket;
import org.pdks.entity.BordroDetayTipi;
import org.pdks.entity.CalismaModeli;
import org.pdks.entity.CalismaModeliAy;
import org.pdks.entity.CalismaModeliVardiya;
import org.pdks.entity.CalismaSekli;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Departman;
import org.pdks.entity.DepartmanDenklestirmeDonemi;
import org.pdks.entity.Dosya;
import org.pdks.entity.FazlaMesaiTalep;
import org.pdks.entity.FileUpload;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.IzinHakedisHakki;
import org.pdks.entity.IzinReferansERP;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.IzinTipiMailAdres;
import org.pdks.entity.Kapi;
import org.pdks.entity.KapiKGS;
import org.pdks.entity.KapiSirket;
import org.pdks.entity.KapiView;
import org.pdks.entity.KatSayi;
import org.pdks.entity.KatSayiTipi;
import org.pdks.entity.KesintiTipi;
import org.pdks.entity.Liste;
import org.pdks.entity.MailGrubu;
import org.pdks.entity.MenuItem;
import org.pdks.entity.Notice;
import org.pdks.entity.Parameter;
import org.pdks.entity.PdksLog;
import org.pdks.entity.PdksPersonelView;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelDenklestirmeDinamikAlan;
import org.pdks.entity.PersonelDenklestirmeTasiyici;
import org.pdks.entity.PersonelDinamikAlan;
import org.pdks.entity.PersonelExtra;
import org.pdks.entity.PersonelFazlaMesai;
import org.pdks.entity.PersonelGeciciYonetici;
import org.pdks.entity.PersonelHareketIslem;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.PersonelIzinDetay;
import org.pdks.entity.PersonelIzinOnay;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.entity.TempIzin;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGorev;
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.VardiyaHafta;
import org.pdks.entity.VardiyaPlan;
import org.pdks.entity.VardiyaSaat;
import org.pdks.entity.VardiyaSablonu;
import org.pdks.entity.VardiyaYemekIzin;
import org.pdks.entity.YemekIzin;
import org.pdks.entity.YemekOgun;
import org.pdks.erp.action.ERPController;
import org.pdks.erp.action.PdksNoSapController;
import org.pdks.erp.action.PdksSap3Controller;
import org.pdks.erp.action.PdksSapController;
import org.pdks.erp.entity.IzinERPDB;
import org.pdks.erp.entity.IzinHakEdisERPDB;
import org.pdks.erp.entity.PersonelERPDB;
import org.pdks.pdf.action.HeaderIText;
import org.pdks.pdf.action.HeaderLowagie;
import org.pdks.pdf.action.PDFITextUtils;
import org.pdks.pdf.action.PDFUtils;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.pdks.security.entity.UserMenuItemTime;
import org.pdks.security.entity.UserRoles;
import org.pdks.security.entity.UserTesis;
import org.pdks.security.entity.UserVekalet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.lowagie.text.Table;
import com.pdks.mail.model.MailManager;
import com.pdks.notUse.IsKurVardiyaGun;
import com.pdks.webservice.IzinERP;
import com.pdks.webservice.IzinHakedis;
import com.pdks.webservice.IzinHakedisDetay;
import com.pdks.webservice.MailFile;
import com.pdks.webservice.MailObject;
import com.pdks.webservice.MailPersonel;
import com.pdks.webservice.MailStatu;
import com.pdks.webservice.PdksSoapVeriAktar;
import com.pdks.webservice.PdksSoapVeriAktarService;
import com.pdks.webservice.PersonelERP;

/**
 * @author Hasan Sayar
 * 
 */
@Name("ortakIslemler")
public class OrtakIslemler implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8530535795343437404L;
	static Logger logger = Logger.getLogger(OrtakIslemler.class);

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
	List<Sirket> pdksSirketleri;

	@Out(required = false, scope = ScopeType.SESSION)
	User seciliYonetici;
	@In(scope = ScopeType.APPLICATION, required = false)
	HashMap<String, MenuItem> menuItemMap = new HashMap<String, MenuItem>();
	@In(required = false)
	FacesMessages facesMessages;

	/**
	 * @param obj
	 * @param list
	 */
	public void addObjectList(Object obj, List list, Boolean durum) {
		if (obj != null && list != null) {
			if (list.isEmpty()) {
				if (obj instanceof BaseObject) {
					BaseObject baseObject = (BaseObject) obj;
					if (durum != null && !baseObject.getDurum().equals(durum))
						obj = null;
				}
				if (obj != null)
					list.add(obj);
			} else {
				Long id = null;
				if (obj instanceof BaseObject) {
					BaseObject baseObject = (BaseObject) obj;
					if (durum == null || baseObject.getDurum().equals(durum))
						id = baseObject.getId();
				} else if (obj instanceof BasePDKSObject) {
					id = ((BasePDKSObject) obj).getId();
				}
				if (id != null) {
					boolean ekle = true;
					for (Object object : list) {
						Long id2 = null;
						if (object instanceof BaseObject) {
							id2 = ((BaseObject) object).getId();
						} else if (obj instanceof BasePDKSObject) {
							id2 = ((BasePDKSObject) object).getId();
						}

						if (id2 != null) {
							if (id2.equals(id)) {
								ekle = false;
								break;
							}
						}

					}

					if (ekle)
						list.add(obj);
				}
			}
		}

	}

	/**
	 * @param gunSira
	 * @param pattern
	 * @return
	 */
	public String getGunAdi(int gunSira, String pattern) {
		String str = PdksUtil.getGunAdi(gunSira, pattern);
		return str;
	}

	/**
	 * @param keyName
	 * @param value
	 * @param class1
	 * @param session
	 * @return
	 */
	public Object objectRefresh(String keyName, Object value, Class class1, Session session) {
		if (!PdksUtil.hasStringValue(keyName))
			keyName = "id";
		HashMap fields = new HashMap();
		fields.put(keyName, value);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		Object object = pdksEntityController.getObjectByInnerObject(fields, class1);
		return object;
	}

	/**
	 * @param bpo
	 * @param session
	 * @return
	 */
	public List fillCalismaModeliVardiyaList(BasePDKSObject bpo, Session session) {
		List calismaModeliVardiyaList = null;
		if (bpo != null && bpo.getId() != null) {
			HashMap parametreMap = new HashMap();
			if (bpo instanceof CalismaModeli) {
				parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "vardiya");
				parametreMap.put("calismaModeli.id", bpo.getId());
				parametreMap.put("calismaModeli.durum", Boolean.TRUE);
			} else if (bpo instanceof Vardiya) {
				parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "calismaModeli");
				parametreMap.put("vardiya.id", bpo.getId());
				parametreMap.put("vardiya.durum", Boolean.TRUE);

			}
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			calismaModeliVardiyaList = pdksEntityController.getObjectByInnerObjectList(parametreMap, CalismaModeliVardiya.class);

			if (calismaModeliVardiyaList.size() > 1)
				calismaModeliVardiyaList = PdksUtil.sortListByAlanAdi(calismaModeliVardiyaList, "id", true);
		} else
			calismaModeliVardiyaList = new ArrayList<Vardiya>();

		return calismaModeliVardiyaList;
	}

	/**
	 * @param vg
	 * @param manuelGirisHareket
	 * @param manuelCikisHareket
	 */
	public void manuelHareketEkle(VardiyaGun vg, HareketKGS girisHareket, HareketKGS cikisHareket) {
		if (vg.getVardiya() != null) {

			vg.setPlanHareketEkle(true);
			vg.setHareketler(null);
			vg.setGirisHareketleri(null);
			vg.setCikisHareketleri(null);

			vg.addHareket(girisHareket, false);
			vg.addHareket(cikisHareket, false);
			// if (vg.getTatil() == null) {
			//
			// }
		}
	}

	/**
	 * @return
	 */
	public String getCurrentTimeStampStr() {
		String str = PdksUtil.getCurrentTimeStampStr();
		return str;
	}

	/**
	 * @return
	 */
	public List<User> getAuthenticatedUserList() {
		List<User> list = new ArrayList<User>();
		if (authenticatedUser != null)
			list.add(authenticatedUser);
		return list;
	}

	/**
	 * @param prefix
	 * @param sicilNo
	 * @return
	 */
	public boolean isStringEqual(String prefix, String sicilNo) {
		boolean sonuc = false;
		if (PdksUtil.hasStringValue(prefix) && PdksUtil.hasStringValue(sicilNo)) {
			if (PdksUtil.getSicilNoUzunluk() != null) {
				sonuc = prefix.trim().equals(sicilNo.trim());
			} else
				sonuc = PdksUtil.isStringEqual(prefix, sicilNo);
		}
		return sonuc;
	}

	/**
	 * @param entityManagerInput
	 * @param pdksEntityControllerInput
	 * @param loginUser
	 */
	public void setInject(EntityManager entityManagerInput, PdksEntityController pdksEntityControllerInput, User loginUser) {
		if (entityManagerInput != null && entityManager == null)
			this.entityManager = entityManagerInput;
		if (pdksEntityControllerInput != null && pdksEntityController == null)
			this.pdksEntityController = pdksEntityControllerInput;
		if (loginUser != null && authenticatedUser == null)
			this.authenticatedUser = loginUser;
	}

	/**
	 * @param personelIdler
	 * @param calSure
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 * @return
	 */
	public List<VardiyaGun> getVardiyaList(List<Long> personelIdler, Double calSure, Date basTarih, Date bitTarih, Session session) {
		LinkedHashMap linkedHashMap = new LinkedHashMap();
		linkedHashMap.put("perList", null);
		linkedHashMap.put("basTarih", PdksUtil.convertToDateString(basTarih, "yyyyMMdd"));
		linkedHashMap.put("bitTarih", PdksUtil.convertToDateString(bitTarih, "yyyyMMdd"));
		linkedHashMap.put("calSure", calSure);
		linkedHashMap.put("format", null);
		if (session != null)
			linkedHashMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List list = null;
		try {
			list = getSPParamLongList(personelIdler, "SP_GET_FAZLA_MESAI_VARDIYA", "perList", linkedHashMap, VardiyaGun.class, session);
		} catch (Exception e) {
			list = new ArrayList();
		}
		linkedHashMap = null;
		return list;
	}

	/**
	 * @param tarih
	 * @param mailList
	 * @param session
	 * @return
	 */
	public TreeMap<String, User> getUserRoller(Date tarih, List<String> mailList, Session session) {
		TreeMap<String, User> userMap = new TreeMap<String, User>();
		HashMap fields = new HashMap();
		fields.put("email", mailList);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<User> userList = pdksEntityController.getObjectByInnerObjectList(fields, User.class);
		if (!userList.isEmpty()) {
			if (tarih == null)
				tarih = PdksUtil.getDate(new Date());
			for (Iterator iterator = userList.iterator(); iterator.hasNext();) {
				User user = (User) iterator.next();
				String ePosta = user.getEmail();
				if (user.isDurum() && user.getPdksPersonel().isCalisiyorGun(tarih)) {
					userMap.put(ePosta, user);
					if (!mailList.contains(ePosta))
						mailList.add(ePosta);
				} else {
					if (!userMap.containsKey(ePosta)) {
						if (mailList.contains(ePosta))
							mailList.remove(ePosta);
					}
					iterator.remove();
				}

			}
			setUserRoller(userList, session);
		}
		return userMap;
	}

	/**
	 * @param userList
	 * @param session
	 */
	public void setUserRoller(List<User> userList, Session session) {
		if (userList != null && session != null) {
			HashMap<Long, User> userMap = new HashMap<Long, User>();
			for (User user : userList) {
				if (user != null && user.getId() != null) {
					user.setYetkiliRollerim(null);
					user.setYetkiSet(Boolean.FALSE);
					userMap.put(user.getId(), user);
				}
			}
			if (!userMap.isEmpty()) {
				List idList = new ArrayList(userMap.keySet());
				String fieldName = "user.id";
				HashMap fields = new HashMap();
				fields.put(fieldName, idList);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				// List<UserRoles> userRoleList = pdksEntityController.getObjectByInnerObjectList(fields, UserRoles.class);
				List<UserRoles> userRoleList = getParamList(false, idList, fieldName, fields, UserRoles.class, session);
				for (UserRoles userRoles : userRoleList) {
					Long key = userRoles.getUser().getId();
					User user = userMap.get(key);
					if (user.getYetkiliRollerim() == null)
						user.setYetkiliRollerim(new ArrayList<Role>());
					user.getYetkiliRollerim().add(userRoles.getRole());
				}
				for (User user : userList)
					PdksUtil.setUserYetki(user);
			}
		}

	}

	/**
	 * @param date
	 * @return
	 */
	public boolean ileriTarihSeciliDegil(Date date) {
		boolean hata = false;
		if (date == null)
			PdksUtil.addMessageWarn("Tarih seçiniz!");
		else if (date.after(new Date()))
			PdksUtil.addMessageWarn("İleri tarihi seçmeyiniz!");
		else
			hata = true;
		return hata;
	}

	/**
	 * @param personeller
	 */
	public void personelSirketTesisSirala(List<Personel> personeller) {
		HashMap<String, Liste> listMap = new HashMap<String, Liste>();
		for (Personel personel : personeller) {
			Sirket sirket = personel.getSirket();
			if (sirket.isPdksMi()) {
				Departman departman = sirket.getDepartman();
				Long departmanId = null, sirketId = null;
				String sirketIdStr = null;
				String tesisIdStr = null;
				if (sirket != null) {
					if (sirket.isTesisDurumu() && personel.getTesis() != null)
						tesisIdStr = personel.getTesis().getAciklama();
					departmanId = departman != null ? departman.getId() : null;
					if (sirket.getSirketGrup() != null)
						sirketId = -sirket.getSirketGrup().getId();
					else
						sirketId = sirket.getId();
				}
				if (departmanId == null)
					departmanId = 0L;
				if (sirketId != null)
					sirketIdStr = (sirketId > 0L ? "S-" + sirket.getAd() : "G-" + sirket.getSirketGrup().getAciklama());
				if (sirketIdStr == null)
					sirketIdStr = "";

				String id = departmanId + "_" + sirketIdStr + (PdksUtil.hasStringValue(tesisIdStr) ? "_" + tesisIdStr : "");
				Liste liste = listMap.containsKey(id) ? listMap.get(id) : new Liste(id, null);
				List<Personel> perList = null;
				if (liste.getValue() != null)
					perList = (List<Personel>) liste.getValue();
				else {
					perList = new ArrayList<Personel>();
					liste.setValue(perList);
					listMap.put(id, liste);
				}
				perList.add(personel);
			}
		}
		if (!listMap.isEmpty()) {
			List<Liste> list = PdksUtil.sortObjectStringAlanList(new ArrayList(listMap.values()), "getId", null);
			personeller.clear();
			for (Liste liste : list) {
				List<Personel> perList = (List<Personel>) liste.getValue();
				personeller.addAll(perList);
			}
			list = null;
		}
		listMap = null;
	}

	/**
	 * @param tarih
	 * @param vardiyaList
	 * @param session
	 */
	public void sonrakiGunVardiyalariAyikla(Date tarih, List<VardiyaGun> vardiyaList, Session session) {
		if (vardiyaList != null) {
			HashMap<String, List<Long>> donemPerMap = new HashMap<String, List<Long>>();
			for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
				VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
				boolean sil = false;
				if (tarih != null) {
					try {
						sil = tarih.before(vardiyaGun.getVardiyaDate());
					} catch (Exception e) {
						sil = true;
					}
				}

				if (sil)
					iterator.remove();
				else if (vardiyaGun.getVardiya() != null) {
					String donem = vardiyaGun.getVardiyaDateStr().substring(0, 6);
					Long perId = vardiyaGun.getPdksPersonel().getId();
					List<Long> idList = donemPerMap.containsKey(donem) ? donemPerMap.get(donem) : new ArrayList<Long>();
					if (idList.isEmpty())
						donemPerMap.put(donem, idList);
					if (!idList.contains(perId))
						idList.add(perId);
				}

			}
			if (!donemPerMap.isEmpty()) {
				HashMap<String, PersonelDenklestirme> donemPerDenkMap = new HashMap<String, PersonelDenklestirme>();
				if (!donemPerMap.isEmpty()) {
					for (String key : donemPerMap.keySet()) {
						int yil = Integer.parseInt(key.substring(0, 4)), ay = Integer.parseInt(key.substring(4));
						HashMap map = new HashMap();
						map.put("yil", yil);
						map.put("ay", ay);
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
						DenklestirmeAy denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(map, DenklestirmeAy.class);
						if (denklestirmeAy != null) {
							String fieldName = "p";
							map.clear();
							StringBuffer sb = new StringBuffer();
							sb.append(" SELECT R.* FROM " + PersonelDenklestirme.TABLE_NAME + " R  WITH(nolock)");
							sb.append("		WHERE R." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = " + denklestirmeAy.getId() + " AND R." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " :" + fieldName);
							List<Long> list = donemPerMap.get(key);
							map.put(fieldName, list);
							if (session != null)
								map.put(PdksEntityController.MAP_KEY_SESSION, session);
							// List<PersonelDenklestirme> veriList = pdksEntityController.getObjectBySQLList(sb, map, PersonelDenklestirme.class);
							List<PersonelDenklestirme> veriList = getSQLParamList(list, sb, fieldName, map, PersonelDenklestirme.class, session);
							for (PersonelDenklestirme personelDenklestirme : veriList)
								donemPerDenkMap.put(key + "_" + personelDenklestirme.getPersonelId(), personelDenklestirme);
							veriList = null;
						}
					}
					if (!donemPerDenkMap.isEmpty()) {
						for (VardiyaGun vardiyaGun : vardiyaList) {
							Personel personel = vardiyaGun.getPersonel();
							String donemStr = vardiyaGun.getVardiyaDateStr().substring(0, 6) + "_" + personel.getId();
							if (donemPerDenkMap != null && donemPerDenkMap.containsKey(donemStr)) {
								PersonelDenklestirme personelDenklestirme = donemPerDenkMap.get(donemStr);
								vardiyaGun.setCalismaModeli(personelDenklestirme.getCalismaModeli());
							}
						}
					}
				}
				donemPerDenkMap = null;
			}
			donemPerMap = null;
		}

	}

	/**
	 * @param session
	 * @return
	 */
	public String gunlukFazlaCalisanlar(Session session) {
		Integer maxGunCalismaAy = null;
		try {
			String str = getParameterKey("maxGunCalismaAy");
			if (PdksUtil.hasStringValue(str)) {
				maxGunCalismaAy = Integer.parseInt(str);
				if (maxGunCalismaAy < 0)
					maxGunCalismaAy = null;
			}

		} catch (Exception e) {

		}
		if (maxGunCalismaAy == null)
			return "";
		Double maxGunCalismaSaat = null;
		try {
			String str = getParameterKey("maxGunCalismaSaat");
			if (PdksUtil.hasStringValue(str))
				maxGunCalismaSaat = Double.parseDouble(str);
			if (maxGunCalismaSaat < 0)
				maxGunCalismaSaat = 0.0d;
		} catch (Exception e) {
			maxGunCalismaSaat = 0.0d;
		}
		if (maxGunCalismaSaat > 0.0d && maxGunCalismaAy != null) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, -maxGunCalismaAy);
			int donem = Integer.parseInt(PdksUtil.convertToDateString(cal.getTime(), "yyyyMM"));
			HashMap fields = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("select D.* from " + DenklestirmeAy.TABLE_NAME + " D WITH(nolock) ");
			sb.append(" WHERE D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100+D." + DenklestirmeAy.COLUMN_NAME_AY + " >= " + donem + " AND D." + DenklestirmeAy.COLUMN_NAME_DURUM + " = 1 ");
			List<DenklestirmeAy> list = pdksEntityController.getObjectBySQLList(sb, fields, DenklestirmeAy.class);
			if (!list.isEmpty()) {
				List<VardiyaGun> fazlaCalismalar = new ArrayList<VardiyaGun>();
				for (DenklestirmeAy denklestirmeAy : list) {
					cal.set(Calendar.YEAR, denklestirmeAy.getYil());
					cal.set(Calendar.MONTH, denklestirmeAy.getAy() - 1);
					cal.set(Calendar.DATE, 1);
					Date basTarih = PdksUtil.getDate(cal.getTime());
					cal.setTime(basTarih);
					cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
					Date bitTarih = PdksUtil.getDate(cal.getTime());
					fields.clear();
					sb = new StringBuffer();
					sb.append("SELECT V." + VardiyaGun.COLUMN_NAME_ID + " FROM " + VardiyaGun.TABLE_NAME + " V WITH(nolock) ");
					sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + " = V." + VardiyaGun.COLUMN_NAME_PERSONEL);
					sb.append(" AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " >= P." + Personel.getIseGirisTarihiColumn());
					sb.append(" AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " <= P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI);
					sb.append(" INNER JOIN " + VardiyaSaat.TABLE_NAME + " S ON S." + VardiyaSaat.COLUMN_NAME_ID + " = V." + VardiyaGun.COLUMN_NAME_VARDIYA_SAAT);
					sb.append(" AND S." + VardiyaSaat.COLUMN_NAME_CALISMA_SURESI + " >= :s ");
					sb.append(" WHERE V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " >= :basTarih AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " <= :bitTarih ");
					sb.append(" ORDER BY V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ",V." + VardiyaGun.COLUMN_NAME_PERSONEL);
					fields.put("s", maxGunCalismaSaat);
					fields.put("basTarih", PdksUtil.getDate(basTarih));
					fields.put("bitTarih", PdksUtil.getDate(bitTarih));
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<BigDecimal> idList = pdksEntityController.getObjectBySQLList(sb, fields, null);
					if (!idList.isEmpty()) {
						List<Long> vIdList = new ArrayList<Long>();
						for (BigDecimal bigDecimal : idList) {
							vIdList.add(bigDecimal.longValue());
						}
						fields.clear();
						fields.put("id", vIdList);
						if (session != null)
							fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						List<VardiyaGun> vardiyaGunList = pdksEntityController.getObjectByInnerObjectList(fields, VardiyaGun.class);
						if (!vardiyaGunList.isEmpty())
							fazlaCalismalar.addAll(vardiyaGunList);
						vIdList = null;
						vardiyaGunList = null;
					}
					idList = null;

				}
				if (!fazlaCalismalar.isEmpty()) {
					TreeMap<String, Liste> listeMap = new TreeMap<String, Liste>();
					boolean tesisDurum = false, altBolumVar = false;
					Tanim ekSaha4Tanim = null;
					List<Long> idList = new ArrayList<Long>();
					for (VardiyaGun vardiyaGun : fazlaCalismalar) {
						Personel personel = vardiyaGun.getPdksPersonel();
						Sirket sirket = personel.getSirket();
						if (!altBolumVar) {
							if (!idList.contains(sirket.getId())) {
								ekSaha4Tanim = getEkSaha4(null, sirket.getId(), session);
								altBolumVar = ekSaha4Tanim != null;
								idList.add(sirket.getId());
							}

						}
						if (!tesisDurum && sirket.getTesisDurum())
							tesisDurum = personel.getTesis() != null;
						String key = sirket.getAd() + "_" + (sirket.getTesisDurum() && personel.getTesis() != null ? personel.getTesis().getAciklama() + "_" : "");
						key += (personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
						key += (personel.getYoneticisi() != null ? personel.getYoneticisi().getAdSoyad() : "");
						key += "_" + personel.getAdSoyad() + "_" + personel.getPdksSicilNo();
						Liste liste = null;
						if (listeMap.containsKey(key))
							liste = listeMap.get(key);
						else {
							liste = new Liste(key, new ArrayList<VardiyaGun>());
							listeMap.put(key, liste);
						}
						List<VardiyaGun> list1 = (List<VardiyaGun>) liste.getValue();
						list1.add(vardiyaGun);

					}
					if (!listeMap.isEmpty()) {
						fazlaCalismalar.clear();
						List<Liste> list2 = PdksUtil.sortObjectStringAlanList(new ArrayList(listeMap.values()), "getId", null);
						for (Liste liste : list2) {
							List<VardiyaGun> fazlaMesaiList = (List<VardiyaGun>) liste.getValue();
							fazlaCalismalar.addAll(fazlaMesaiList);
						}
					}
					HashMap sonucMap = fillEkSahaTanim(session, Boolean.FALSE, Boolean.FALSE);
					String tesisAciklama = null;
					if (tesisDurum)
						tesisAciklama = tesisAciklama();
					String bolumAciklama = (String) sonucMap.get("bolumAciklama");
					String altBolumAciklama = (String) sonucMap.get("altBolumAciklama");
					String personelNoAciklama = personelNoAciklama();
					String yoneticiAciklama = yoneticiAciklama();
					String sirketAciklama = sirketAciklama();

					List<User> ikList = IKKullanicilariBul(null, null, session);
					if (ikList.size() > 1)
						ikList = PdksUtil.sortObjectStringAlanList(ikList, "getAdSoyad", null);
					MailObject mail = new MailObject();
					mail.setSubject("Fazla çalışmalarında problemli personeller");
					String geciciPER = "XXXXXYZX";
					sb = new StringBuffer();
					sb.append("<p>Sayın " + geciciPER + " </p>");
					sb.append("<p>Aşağıdaki personel fazla çalışmalarında problem vardır.</p>");
					sb.append("<p></p>");
					sb.append("<p>Saygılarımla,</p>");
					sb.append("<H3>" + PdksUtil.replaceAllManuel("Günlük fazla çalışanlar", "  ", " ") + "</H3>");
					sb.append("<TABLE class=\"mars\" style=\"border: solid 1px\" cellpadding=\"5\" cellspacing=\"0\"><THEAD> <TR>");

					sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>" + yoneticiAciklama + "</b></TH>");
					sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>" + sirketAciklama + "</b></TH>");
					if (tesisAciklama != null)
						sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>" + tesisAciklama + "</b></TH>");
					sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>" + bolumAciklama + "</b></TH>");
					if (altBolumVar)
						sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>" + altBolumAciklama + "</b></TH>");
					sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>Adı Soyadı</b></TH>");
					sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>" + personelNoAciklama + "</b></TH>");
					sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>Çalışma Zamanı</b></TH>");
					sb.append("<TH align=\"center\" style=\"border: 1px solid;\"><b>Süre</b></TH>");
					sb.append("</TR></THEAD><TBODY>");
					boolean renk = false;
					Long id = null;
					for (VardiyaGun vg : fazlaCalismalar) {
						Personel personel = vg.getPersonel();
						boolean degisti = false;
						if (id == null || !personel.getId().equals(id)) {
							id = personel.getId();
							degisti = true;
						}
						renk = !renk;
						Sirket sirket = personel.getSirket();
						String classTR = "class=\"" + (renk ? "odd" : "even") + "\"";
						sb.append("<TR " + classTR + ">");
						sb.append("<td nowrap style=\"border: 1px solid;\">" + (personel.getPdksYonetici() != null && degisti ? personel.getPdksYonetici().getAdSoyad() : "") + "</td>");
						sb.append("<td nowrap style=\"border: 1px solid;\">" + sirket.getAd() + "</td>");
						if (tesisAciklama != null)
							sb.append("<td nowrap style=\"border: 1px solid;\">" + (sirket.getTesisDurum() && personel.getTesis() != null && degisti ? personel.getTesis().getAciklama() : "") + "</td>");
						sb.append("<td nowrap style=\"border: 1px solid;\">" + (personel.getEkSaha3() != null && degisti ? personel.getEkSaha3().getAciklama() : "") + "</td>");
						if (altBolumVar)
							sb.append("<td nowrap style=\"border: 1px solid;\">" + (personel.getEkSaha4() != null && degisti ? personel.getEkSaha4().getAciklama() : "") + "</td>");
						sb.append("<td nowrap style=\"border: 1px solid;\">" + (degisti ? personel.getAdSoyad() : "") + "</td>");
						sb.append("<td align=\"center\" style=\"border: 1px solid;\">" + (degisti ? personel.getSicilNo() : "") + "</td>");
						sb.append("<td align=\"center\" style=\"border: 1px solid;\">" + vg.getVardiyaZamanAdi() + "</td>");
						String str = "";
						try {
							str = PdksUtil.numericValueFormatStr(vg.getVardiyaSaat().getCalismaSuresi(), null);
						} catch (Exception e) {
						}
						sb.append("<td align=\"center\" style=\"border: 1px solid;\">" + str + "</td>");
						sb.append("</TR>");
					}
					sb.append("</TBODY></TABLE><BR/><BR/>");

					String str = sb.toString();
					ByteArrayOutputStream baosDosya = null;
					try {
						baosDosya = vardiyaGunExcelDevam(null, fazlaCalismalar, tesisAciklama, bolumAciklama, altBolumAciklama);

					} catch (Exception e) {
						e.printStackTrace();
					}
					if (baosDosya != null) {
						byte[] excelData = baosDosya.toByteArray();
						MailFile mailFile = new MailFile();
						mailFile.setIcerik(excelData);
						mailFile.setDisplayName("FazlaCalisma.xlsx");
						mail.getAttachmentFiles().add(mailFile);
					}
					for (User yonetici : ikList) {
						User userYonetici = null;
						if (authenticatedUser != null) {
							userYonetici = (User) yonetici.clone();
							userYonetici.setEmail(authenticatedUser.getEmail());
						}

						else
							userYonetici = yonetici;
						mail.getToList().clear();
						MailPersonel mailUser = new MailPersonel();
						mailUser.setEPosta(yonetici.getEmail());
						mailUser.setAdiSoyadi(yonetici.getAdSoyad());
						mail.getToList().add(mailUser);
						mail.setBody(PdksUtil.replaceAll(str, geciciPER, yonetici.getAdSoyad()));

						try {
							MailStatu mailSatu = mailSoapServisGonder(true, mail, null, null, session);
							if (mailSatu != null && mailSatu.getDurum())
								logger.info(fazlaCalismalar.size());
						} catch (Exception e) {
							e.printStackTrace();
						}

					}

				}
			}

		}
		return "";
	}

	/**
	 * @param wb
	 * @param vardiyaGunList
	 * @param tesisAciklama
	 * @param bolumAciklama
	 * @param altBolumAciklama
	 * @return
	 */
	public ByteArrayOutputStream vardiyaGunExcelDevam(Workbook wb, List<VardiyaGun> vardiyaGunList, String tesisAciklama, String bolumAciklama, String altBolumAciklama) {
		String personelNoAciklama = personelNoAciklama();
		String yoneticiAciklama = yoneticiAciklama();
		String sirketAciklama = sirketAciklama();

		ByteArrayOutputStream baos = null;
		boolean veriOlustur = wb == null;
		if (veriOlustur)
			wb = new XSSFWorkbook();

		Sheet sheet = ExcelUtil.createSheet(wb, "Vardiyalar", Boolean.TRUE);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddTutar = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleOddNumber = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_NUMBER, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenTutar = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleEvenNumber = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_NUMBER, wb);
		int col = 0, row = 0;
		CreationHelper helper = wb.getCreationHelper();
		ClientAnchor anchor = helper.createClientAnchor();
		Drawing drawing = sheet.createDrawingPatriarch();
		try {
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(personelNoAciklama);
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(sirketAciklama);

			if (tesisAciklama != null)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(tesisAciklama);

			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
			if (altBolumAciklama != null)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(altBolumAciklama);
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(yoneticiAciklama);
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Görevi");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Çalışma Zamanı");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Süre");
			boolean renk = true;
			for (VardiyaGun gun : vardiyaGunList) {
				Personel personel = gun.getPdksPersonel();
				CellStyle styleCenter = null;
				CellStyle styleGenel = null;
				CellStyle styleDouble = null;
				CellStyle styleNumber = null;
				if (renk) {
					styleGenel = styleOdd;
					styleCenter = styleOddCenter;
					styleDouble = styleOddTutar;
					styleNumber = styleOddNumber;
				} else {
					styleGenel = styleEven;
					styleCenter = styleEvenCenter;
					styleDouble = styleEvenTutar;
					styleNumber = styleEvenNumber;
				}

				renk = !renk;
				row++;
				col = 0;

				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getSicilNo());
				Cell personelCell = ExcelUtil.getCell(sheet, row, col++, styleGenel);
				personelCell.setCellValue(personel.getAdSoyad());
				Sirket sirket = personel.getSirket();
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(sirket.getAd());
				if (tesisAciklama != null)
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(sirket.getTesisDurum() && personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
				if (altBolumAciklama != null)
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getEkSaha4() != null ? personel.getEkSaha4().getAciklama() : "");

				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getYoneticisi() != null ? personel.getYoneticisi().getAdSoyad() : "");
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getGorevTipi() != null ? personel.getGorevTipi().getAciklama() : "");
				Cell cell = ExcelUtil.getCell(sheet, row, col++, styleCenter);
				VardiyaSaat vardiyaSaat = gun.getVardiyaSaat();
				if (vardiyaSaat == null)
					vardiyaSaat = new VardiyaSaat();
				Double sure = Double.valueOf(vardiyaSaat.getCalismaSuresi());
				Double normalSure = Double.valueOf(vardiyaSaat.getNormalSure());
				if ((normalSure != null) && (normalSure.doubleValue() > 0.0D)) {
					ExcelUtil.setCellComment(cell, anchor, helper, drawing, "Net Süre : " + PdksUtil.numericValueFormatStr(normalSure, null));
				}

				cell.setCellValue(gun.getVardiyaZamanAdi());
				if (sure != null && sure.doubleValue() != 0.0d)
					ExcelUtil.getCell(sheet, row, col++, PdksUtil.isDoubleValueNotLong(sure) ? styleDouble : styleNumber).setCellValue(sure.doubleValue());
				else {
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
				}
			}
			for (int i = 0; i <= col; i++)
				sheet.autoSizeColumn(i);
			if (veriOlustur) {
				baos = new ByteArrayOutputStream();
				wb.write(baos);
			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			baos = null;
		}
		return baos;
	}

	/**
	 * @param personelIzinList
	 * @return
	 */
	public void excelServiceAktar(List<PersonelIzin> personelIzinList) {
		try {
			ByteArrayOutputStream baosDosya = excelServiceAktarDevam(personelIzinList);
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, "personelIzinWebServisListesi.xlsx");
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
		}
	}

	/**
	 * @param date
	 * @param list
	 * @param bolumAciklama
	 * @param wb
	 */
	public void vardiyaHareketExcel(Date date, List<VardiyaGun> list, String bolumAciklama, Workbook wb) {
		List<VardiyaGun> vardiyaGunList = new ArrayList<VardiyaGun>(list);
		boolean tesisDurum = getListTesisDurum(vardiyaGunList);
		Sheet sheetHareket = ExcelUtil.createSheet(wb, "Hareket  Listesi", false);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleRedOdd = ExcelUtil.getStyleOdd(null, wb);
		ExcelUtil.setFontColor(styleRedOdd, Color.RED);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddDateTime = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATETIME, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleRedEven = ExcelUtil.getStyleEven(null, wb);
		ExcelUtil.setFontColor(styleRedEven, Color.RED);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenDateTime = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATETIME, wb);

		boolean manuelGiris = false, izinDurum = false, hareketDurum = false, fazlaMesaiDurum = false;
		Date bugun = new Date();
		int gunDurum = PdksUtil.tarihKarsilastirNumeric(date, bugun);
		HashMap<Long, VardiyaGun> idMap = new HashMap<Long, VardiyaGun>();
		for (Iterator iterator = vardiyaGunList.iterator(); iterator.hasNext();) {
			VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator.next();
			Vardiya islemVardiya = pdksVardiyaGun.getIslemVardiya();
			boolean sil = pdksVardiyaGun.getId() == null || islemVardiya == null;
			try {
				if (!sil) {
					if (idMap.containsKey(pdksVardiyaGun.getId())) {
						if (pdksVardiyaGun.getIzin() != null) {
							VardiyaGun vardiyaGun = idMap.get(pdksVardiyaGun.getId());
							vardiyaGun.setIzin(pdksVardiyaGun.getIzin());
						}

						sil = true;

					} else
						idMap.put(pdksVardiyaGun.getId(), pdksVardiyaGun);

					if (pdksVardiyaGun.getVardiyaDate().before(date)) {
						if (!(islemVardiya.getBitSaat() < islemVardiya.getBasSaat() && gunDurum == 0) || pdksVardiyaGun.getIzin() != null)
							sil = true;

					} else {
						if (islemVardiya.getBitSaat() < islemVardiya.getBasSaat() && gunDurum == 0 && bugun.before(islemVardiya.getVardiyaBasZaman()))
							sil = true;
					}
				}
			} catch (Exception e) {
				sil = true;
				e.printStackTrace();
			}

			if (sil)
				iterator.remove();

		}
		int col = 0, row = 0;
		ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue(personelNoAciklama());
		ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue("Personel");
		ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue(yoneticiAciklama());
		ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue(sirketAciklama());
		if (tesisDurum)
			ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue(tesisAciklama());
		ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue(bolumAciklama);

		ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue("Vardiya");
		ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue("Kapı");
		ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue("Zaman");

		for (VardiyaGun calismaPlani : vardiyaGunList) {
			if (calismaPlani.getHareketler() != null && !calismaPlani.getHareketler().isEmpty()) {
				hareketDurum = true;
				for (HareketKGS hareketKGS : calismaPlani.getHareketler()) {
					if (hareketKGS.getIslem() != null) {
						manuelGiris = true;
						break;
					}
				}

			}
			if (izinDurum == false && calismaPlani.getVardiya() != null)
				try {
					izinDurum = calismaPlani.getVardiya().isCalisma() == false || calismaPlani.isIzinli();
				} catch (Exception e) {
					e.printStackTrace();
				}

			if (izinDurum && hareketDurum && fazlaMesaiDurum && manuelGiris)
				break;
		}
		if (manuelGiris) {
			ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue("İşlem Yapan");
			ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue("İşlem Zamanı");
		}
		ExcelUtil.getCell(sheetHareket, row, col++, header).setCellValue("Durum");
		int rowHareket = 0, colHareket = 0;
		boolean renk = true;
		User loginUser = authenticatedUser != null ? authenticatedUser : new User();
		for (VardiyaGun calismaPlani : vardiyaGunList) {
			Personel personel = calismaPlani.getPersonel();

			List<HareketKGS> hareketler = calismaPlani.getHareketler();
			Sirket sirket = null;
			Vardiya vardiya = null;
			if (personel != null) {
				sirket = personel.getSirket();
				vardiya = calismaPlani.getVardiya();
			} else
				continue;

			CellStyle style = null, styleCenter = null, cellStyleDateTime = null, styleRed = null;

			if (renk) {
				cellStyleDateTime = styleOddDateTime;
				style = styleOdd;
				styleRed = styleRedOdd;
				styleCenter = styleOddCenter;
			} else {
				cellStyleDateTime = styleEvenDateTime;
				style = styleEven;
				styleRed = styleRedEven;
				styleCenter = styleEvenCenter;
			}
			renk = !renk;

			if (hareketDurum) {

				if (hareketler != null && !hareketler.isEmpty()) {
					boolean ilkGiris = true;
					for (Iterator iterator = hareketler.iterator(); iterator.hasNext();) {
						HareketKGS hareketKGS = (HareketKGS) iterator.next();
						KapiKGS kapiKGS = hareketKGS.getKapiKGS();
						StringBuffer sb = new StringBuffer();
						if (calismaPlani.getIslemVardiya() != null) {
							Date zaman = hareketKGS.getOrjinalZaman();
							Vardiya islemVardiya = calismaPlani.getIslemVardiya();
							Kapi kapi = hareketKGS.getKapiView().getKapi();
							if (kapi.isGirisKapi()) {
								if (ilkGiris) {
									if (islemVardiya.getVardiyaTelorans1BasZaman().after(zaman))
										sb.append("Erken Giriş");
									else if (islemVardiya.getVardiyaTelorans2BasZaman().before(zaman) && islemVardiya.getVardiyaTelorans1BitZaman().after(zaman))
										sb.append("Geç Giriş");
								}

							} else if (kapi.isCikisKapi()) {
								if (iterator.hasNext() == false) {
									if (islemVardiya.getVardiyaTelorans1BitZaman().after(zaman))
										sb.append("Erken Çıkış");
									else if (islemVardiya.getVardiyaTelorans2BitZaman().before(zaman))
										sb.append("Geç Çıkış");
								}
							}
						}
						ilkGiris = false;
						String kapiAciklama = kapiKGS.getKapi() != null ? kapiKGS.getKapi().getAciklama() : kapiKGS.getAciklamaKGS();
						rowHareket++;
						colHareket = 0;
						ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, styleCenter).setCellValue(personel.getPdksSicilNo());
						ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue(personel.getAdSoyad());
						ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue(personel.getYoneticisi() != null && personel.getYoneticisi().isCalisiyorGun(calismaPlani.getVardiyaDate()) ? personel.getYoneticisi().getAdSoyad() : "");
						ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue(sirket.getAd());
						if (tesisDurum)
							ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue(personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
						ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
						ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, styleCenter).setCellValue(vardiya.isCalisma() ? loginUser.dateFormatla(calismaPlani.getVardiyaDate()) + " " + vardiya.getAciklama() : vardiya.getAdi());
						ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue(kapiAciklama);
						ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, cellStyleDateTime).setCellValue(hareketKGS.getOrjinalZaman());
						if (manuelGiris) {
							PersonelHareketIslem islem = hareketKGS.getIslem();
							if (islem != null) {
								manuelGiris = true;
								ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue(islem.getOnaylayanUser() != null ? islem.getOnaylayanUser().getAdSoyad() : "");
								if (islem.getOlusturmaTarihi() != null)
									ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, cellStyleDateTime).setCellValue(islem.getOlusturmaTarihi());
								else
									ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue("");
							} else {
								ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue("");
								ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue("");

							}
						}
						if (sb.length() > 0)
							ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, styleRed).setCellValue(sb.toString());
						else
							ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue("");
						sb = null;
					}
				} else {
					rowHareket++;
					colHareket = 0;
					StringBuffer sb = new StringBuffer();
					if (calismaPlani.isIzinli()) {
						if (calismaPlani.getIzin() != null)
							sb.append(calismaPlani.getIzin().getIzinTipiAciklama());
						else
							sb.append(calismaPlani.getVardiyaAdi());

					} else if (calismaPlani.getVardiya().isCalisma())
						sb.append("Devamsız");
					ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, styleCenter).setCellValue(personel.getPdksSicilNo());
					ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue(personel.getAdSoyad());
					ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue(personel.getYoneticisi() != null && personel.getYoneticisi().isCalisiyorGun(calismaPlani.getVardiyaDate()) ? personel.getYoneticisi().getAdSoyad() : "");
					ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue(sirket.getAd());
					if (tesisDurum)
						ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue(personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
					ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
					ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, styleCenter).setCellValue(vardiya.isCalisma() ? loginUser.dateFormatla(calismaPlani.getVardiyaDate()) + " " + vardiya.getAciklama() : vardiya.getAdi());
					ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue(sb.toString());
					ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue("");
					if (manuelGiris) {
						ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue("");
						ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue("");
					}

					ExcelUtil.getCell(sheetHareket, rowHareket, colHareket++, style).setCellValue("");
				}

			}

		}
		for (int i = 0; i < colHareket; i++)
			sheetHareket.autoSizeColumn(i);
		vardiyaGunList = null;
	}

	/**
	 * @param izinList
	 * @return
	 * @throws Exception
	 */
	private ByteArrayOutputStream excelServiceAktarDevam(List<PersonelIzin> izinList) throws Exception {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, "Izin WebService Listesi", false);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);

		int row = 0;
		int col = 0;
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Açıklama");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Başlangıç Zaman");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Bitiş Zaman");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Durum");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İzin Süresi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İzin Tipi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İzin Tipi Açıklama");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Personel No");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Referans ERP No");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Süre Birimi");
		String pattern = "yyyy-MM-dd HH:mm";
		boolean renk = true;
		for (PersonelIzin personelIzin : izinList) {
			if (personelIzin.getReferansERP() == null)
				continue;
			++row;
			col = 0;
			CellStyle style = null, styleCenter = null;
			if (renk) {
				style = styleOdd;
				styleCenter = styleOddCenter;
			} else {
				style = styleEven;
				styleCenter = styleEvenCenter;
			}
			renk = !renk;
			String aciklama = personelIzin.getAciklama();
			int index = aciklama.lastIndexOf("(");
			if (index > 0)
				aciklama = aciklama.substring(0, index);
			Tanim izinTipi = personelIzin.getIzinTipi().getIzinTipiTanim();
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(aciklama.trim());
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(PdksUtil.convertToDateString(personelIzin.getBaslangicZamani(), pattern));
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(PdksUtil.convertToDateString(personelIzin.getBitisZamani(), pattern));
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(new Boolean(personelIzin.getIzinDurumu() != PersonelIzin.IZIN_DURUMU_REDEDILDI && personelIzin.getIzinDurumu() != PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL).toString());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personelIzin.getIzinSuresi());
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(izinTipi.getErpKodu());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(izinTipi.getAciklama());
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personelIzin.getIzinSahibi().getPdksSicilNo());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personelIzin.getReferansERP());
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personelIzin.getHesapTipi() != null ? personelIzin.getHesapTipi() : PersonelIzin.HESAP_TIPI_GUN);
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
		return baos;
	}

	/**
	 * @param izinList
	 * @param xSession
	 * @return
	 */
	public boolean erpIzinDoldur(List<PersonelIzin> izinList, Session xSession) {
		boolean servisAktarDurum = false;
		if (!PdksUtil.getTestSunucuDurum()) {
			TreeMap<Long, PersonelIzin> idMap = new TreeMap<Long, PersonelIzin>();
			for (PersonelIzin personelIzin : izinList) {
				personelIzin.setReferansERP(null);
				idMap.put(personelIzin.getId(), personelIzin);
			}
			if (!idMap.isEmpty()) {
				List idList = new ArrayList(idMap.keySet());
				String fieldName = "izin.id";
				HashMap parametreMap = new HashMap();
				parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "izin.id,id");
				parametreMap.put(fieldName, new ArrayList(idMap.keySet()));
				if (xSession != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, xSession);
				// List<Object[]> list = pdksEntityController.getObjectByInnerObjectList(parametreMap, IzinReferansERP.class);
				List<Object[]> list = getParamList(false, idList, fieldName, parametreMap, IzinReferansERP.class, xSession);
				for (Object[] objects : list) {
					Long key = (Long) objects[0];
					if (idMap.containsKey(key)) {
						servisAktarDurum = true;
						idMap.get(key).setReferansERP((String) objects[1]);
					}

				}
			}
		}
		return servisAktarDurum;
	}

	/**
	 * @param list
	 * @return
	 */
	public boolean getListTesisDurum(List list) {
		boolean tesisDurum = false;
		if (list != null && !list.isEmpty()) {
			if (getParameterKey("tesisDurumu").equals("1")) {
				for (Object object : list) {
					try {
						if (!tesisDurum) {
							Object objectPersonel = PdksUtil.getMethodObject(object, "getPdksPersonel", null);
							if (objectPersonel != null) {
								if (objectPersonel instanceof Personel) {
									Personel personel = (Personel) objectPersonel;
									if (personel.getSirket() != null)
										tesisDurum = personel.getSirket().isTesisDurumu();
								} else
									break;
							}
						}
						if (tesisDurum)
							break;
					} catch (Exception e) {
						break;
					}
				}
			}
		}
		return tesisDurum;
	}

	/**
	 * @param list
	 * @param index
	 * @return
	 */
	public boolean getListEkSahaDurum(List list, String index) {
		HashMap<String, Boolean> map = getListEkSahaDurumMap(list, Integer.parseInt(index));
		boolean ekSahaDurum = !map.isEmpty();
		return ekSahaDurum;
	}

	/**
	 * @param list
	 * @param ekSaha
	 * @return
	 */
	public HashMap<String, Boolean> getListEkSahaDurumMap(List list, Integer index) {
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();
		if (list != null && !list.isEmpty()) {
			List<Integer> sahalar = new ArrayList<Integer>();
			if (index == null) {
				for (int i = 0; i < 4; i++) {
					sahalar.add(i + 1);
				}
			} else
				sahalar.add(index);
			for (Object object : list) {
				try {
					Object objectPersonel = PdksUtil.getMethodObject(object, "getPdksPersonel", null);
					if (objectPersonel != null) {
						if (objectPersonel instanceof Personel) {
							Personel personel = (Personel) objectPersonel;
							for (Iterator iterator = sahalar.iterator(); iterator.hasNext();) {
								Integer ekSaha = (Integer) iterator.next();
								Tanim tanim = null;
								switch (ekSaha) {
								case 1:
									tanim = personel.getEkSaha1();
									break;
								case 2:
									tanim = personel.getEkSaha2();
									break;
								case 3:
									tanim = personel.getEkSaha3();
									break;
								case 4:
									tanim = personel.getEkSaha4();
									break;
								default:
									break;
								}
								if (tanim != null) {
									map.put("ekSaha" + ekSaha, true);
									iterator.remove();
								}
							}
						}

					} else
						break;

					if (sahalar.isEmpty())
						break;
				} catch (Exception e) {
					break;
				}
			}
			sahalar = null;
		}

		return map;
	}

	/**
	 * @param yil
	 * @param ayMap
	 * @param xSession
	 * @return
	 */
	@Transactional
	public Boolean yilAyKontrol(int yil, TreeMap<Integer, DenklestirmeAy> ayMap, Session xSession) {
		Boolean denklestirmeKesintiYap = Boolean.FALSE;
		if (ayMap == null) {
			HashMap map = new HashMap();
			map.put(PdksEntityController.MAP_KEY_MAP, "getAy");
			map.put("yil", yil);
			if (xSession != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, xSession);
			ayMap = pdksEntityController.getObjectByInnerObjectMap(map, DenklestirmeAy.class, false);
		}
		Integer denklestirmeKesintiDurum = null;
		KesintiTipi kesintiTipi = null;
		try {
			denklestirmeKesintiDurum = Integer.parseInt(getParameterKey("denklestirmeKesintiYap"));
		} catch (Exception e) {
			denklestirmeKesintiDurum = null;
		}
		if (denklestirmeKesintiDurum != null)
			kesintiTipi = KesintiTipi.fromValue(denklestirmeKesintiDurum);
		if (kesintiTipi == null)
			kesintiTipi = KesintiTipi.KESINTI_YOK;
		denklestirmeKesintiDurum = kesintiTipi.value();
		Double fazlaMesaiMaxSure = getFazlaMesaiMaxSure(null);
		Double yemekMolasiYuzdesi = getYemekMolasiYuzdesi(null, xSession) * 100.0d;
		User user = getSistemAdminUser(xSession);
		if (user == null)
			user = authenticatedUser;
		int buYil = PdksUtil.getDateField(new Date(), Calendar.YEAR);
		for (int i = 1; i <= 12; i++) {
			DenklestirmeAy denklestirmeAy = null;
			boolean flush = false;
			if (ayMap.containsKey(i)) {
				denklestirmeAy = ayMap.get(i);
				if (!denklestirmeKesintiYap)
					denklestirmeKesintiYap = !denklestirmeAy.getDenklestirmeKesintiYap().equals(KesintiTipi.KESINTI_YOK.value());
				if (denklestirmeAy.getYemekMolasiYuzdesi() == null) {
					denklestirmeAy.setYemekMolasiYuzdesi(yemekMolasiYuzdesi);
					flush = true;
				}
				if (denklestirmeAy.getFazlaMesaiMaxSure() == null) {
					denklestirmeAy.setFazlaMesaiMaxSure(fazlaMesaiMaxSure);
					flush = true;
				}
			} else {
				if (buYil > yil)
					continue;
				flush = true;
				denklestirmeAy = new DenklestirmeAy();
				denklestirmeAy.setDenklestirmeKesintiYap(denklestirmeKesintiDurum);
				denklestirmeAy.setOlusturmaTarihi(new Date());
				denklestirmeAy.setOlusturanUser(user);
				denklestirmeAy.setAy(i);
				denklestirmeAy.setYil(yil);
				denklestirmeAy.setSure(0d);
				denklestirmeAy.setYemekMolasiYuzdesi(yemekMolasiYuzdesi);
				denklestirmeAy.setFazlaMesaiMaxSure(fazlaMesaiMaxSure);
				denklestirmeAy.setDurum(Boolean.TRUE);
			}
			if (flush) {
				pdksEntityController.saveOrUpdate(xSession, entityManager, denklestirmeAy);
				xSession.flush();
			}
		}
		if (!denklestirmeKesintiYap)
			denklestirmeKesintiYap = !denklestirmeKesintiDurum.equals(KesintiTipi.KESINTI_YOK.value());
		return denklestirmeKesintiYap;
	}

	/**
	 * @param perId
	 * @param kodu
	 * @param session
	 * @return
	 */
	public List<Personel> getIkinciYoneticiOlmazList(Long perId, String kodu, Session session) {
		List<Personel> list = null;
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("yoneticiId", perId);
		map.put("tipi", kodu);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		StringBuffer sp = new StringBuffer("SP_IKINCI_YONETICI_OLAMAZ");
		try {
			list = pdksEntityController.execSPList(map, sp, Personel.class);

		} catch (Exception e) {

		}
		if (list == null)
			list = new ArrayList<Personel>();
		return list;
	}

	/**
	 * @param tarih1
	 * @param tarih2
	 * @return
	 */
	public boolean tarihEsit(Date tarih1, Date tarih2) {
		boolean esit = false;
		if (tarih1 != null && tarih2 != null)
			esit = PdksUtil.tarihKarsilastirNumeric(tarih1, tarih2) == 0;
		return esit;

	}

	/**
	 * @param sirket
	 * @param sirketId
	 * @param session
	 * @return
	 */
	public Tanim getEkSaha4(Sirket sirket, Long sirketId, Session session) {
		Tanim tanim = null;
		User loginUser = authenticatedUser != null ? authenticatedUser : new User();
		if (PdksUtil.isPuantajSorguAltBolumGir() || loginUser.isAdmin()) {
			if (sirket == null && sirketId != null) {
				HashMap parametreMap = new HashMap();
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				try {
					parametreMap.put("id", sirketId);

					sirket = (Sirket) pdksEntityController.getObjectByInnerObject(parametreMap, Sirket.class);
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}
			}
			if (sirket != null && sirket.isErp()) {
				HashMap parametreMap = new HashMap();
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				try {
					parametreMap.put("tipi", Tanim.TIPI_PERSONEL_EK_SAHA);
					parametreMap.put("durum", Boolean.TRUE);
					parametreMap.put("kodu", "ekSaha4");
					tanim = (Tanim) pdksEntityController.getObjectByInnerObject(parametreMap, Tanim.class);
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}
			}
		}

		return tanim;
	}

	/**
	 * @param name
	 * @param active
	 * @param session
	 * @return
	 */
	public Notice getNotice(String name, Boolean active, Session session) {
		HashMap parametreMap = new HashMap();
		parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		Notice notice = null;
		try {
			parametreMap.put("name", name);
			if (active != null)
				parametreMap.put("active", active);
			notice = (Notice) pdksEntityController.getObjectByInnerObject(parametreMap, Notice.class);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return notice;
	}

	/**
	 * @return
	 */
	public String getStringHelpDesk() {
		String str = null;
		File file = new File("/opt/sertifika/websrv.txt");
		if (file.exists()) {
			try {
				String servisPath = new String(PdksUtil.getFileByteArray(file)) + "/rest/services/helpDeskDate";
				String servisValue = getJSONData(servisPath, "POST", null, null, false);
				if (servisValue != null && servisValue.length() > 0) {
					JSONParser jsonParser = new JSONParser();
					JSONObject jsonObject = (JSONObject) jsonParser.parse(servisValue);
					if (jsonObject.containsKey("dt"))
						str = (String) jsonObject.get("dt");
				}

			} catch (Exception e) {

			}

		}
		return str;
	}

	/**
	 * @param helpDeskLastDateStr
	 * @return
	 */
	public static Date getHelpDeskLastDateFrom(String helpDeskLastDateStr) {
		Date helpDeskLastDate = PdksUtil.getDateFromString(helpDeskLastDateStr);
		if (helpDeskLastDate == null)
			helpDeskLastDate = PdksUtil.getDateFromString(PdksUtil.getDecodeStringByBase64(helpDeskLastDateStr));
		return helpDeskLastDate;
	}

	/**
	 * @return
	 */
	public Date getHelpDeskLastDate() {
		Date helpDeskLastDate = PdksUtil.getHelpDeskLastDate();
		return helpDeskLastDate;
	}

	/**
	 * @return
	 */
	public String getDateFormat() {
		String dateFormat = PdksUtil.getDateFormat();
		return dateFormat;
	}

	/**
	 * @return
	 */
	public String getDateTimeFormat() {
		String dateTimeFormat = PdksUtil.getDateTimeFormat();
		return dateTimeFormat;
	}

	/**
	 * @param str
	 * @return
	 */
	public boolean hasStringValue(String str) {
		boolean durum = PdksUtil.hasStringValue(str);
		return durum;
	}

	/**
	 * 
	 */
	public boolean yoneticiRolKontrol(Session session) {
		List<String> roleList = null;
		String yoneticiRolleri = getParameterKey("yoneticiRolleri");
		if (PdksUtil.hasStringValue(yoneticiRolleri))
			roleList = PdksUtil.getListByString(yoneticiRolleri, null);
		if (roleList == null || roleList.isEmpty())
			roleList = Arrays.asList(new String[] { Role.TIPI_GENEL_MUDUR, Role.TIPI_YONETICI, Role.TIPI_YONETICI_KONTRATLI });
		HashMap fields = new HashMap();
		String fieldName = "r";
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT R." + Role.COLUMN_NAME_ROLE_NAME + " FROM " + Role.TABLE_NAME + " R WITH(nolock) ");
		sb.append("	WHERE R." + Role.COLUMN_NAME_STATUS + " = 1 AND R." + Role.COLUMN_NAME_ADMIN_ROLE + " <> 1 AND R." + Role.COLUMN_NAME_ROLE_NAME + " :" + fieldName);
		fields.put(fieldName, roleList);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		// List veriList = pdksEntityController.getObjectBySQLList(sb, fields, null);
		List veriList = getSQLParamList(roleList, sb, fieldName, fields, null, session);

		boolean yoneticiRolVarmi = !veriList.isEmpty();
		veriList = null;
		fields = null;
		return yoneticiRolVarmi;
	}

	/**
	 * @param user
	 * @return
	 */
	public Boolean getKullaniciPersonel(User user) {
		boolean personelMi = false;
		User loginUser = authenticatedUser != null ? authenticatedUser : new User();
		if (user == null)
			user = loginUser;
		if (user != null) {
			if (user.isDirektorSuperVisor() == false && user.getYetkiliPersonelNoList() != null && user.getYetkiliPersonelNoList().size() == 1) {
				try {
					boolean yoneticiPersonelEngelleDurum = !getParameterKey("yoneticiPersonelEngelleDurum").equals("1");
					String perNo = user.getYetkiliPersonelNoList().get(0).trim();
					if (perNo != null && user.getPdksPersonel().getPdksSicilNo() != null)
						personelMi = user.getPdksPersonel().getPdksSicilNo().trim().equals(perNo) && yoneticiPersonelEngelleDurum;
				} catch (Exception e) {

				}

			}
		}
		return personelMi;
	}

	/**
	 * @return
	 */
	public Boolean getSistemDestekVar() {
		return PdksUtil.isSistemDestekVar();
	}

	/**
	 * @param session
	 * @param departman
	 * @return
	 */
	public Boolean getVardiyaIzinGir(Session session, Departman departman) {
		boolean manuelGir = false;
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT DISTINCT I.* FROM " + IzinTipi.TABLE_NAME + " I WITH(nolock) ");
		sb.append(" WHERE I." + IzinTipi.COLUMN_NAME_DURUM + " = 1  AND  I." + IzinTipi.COLUMN_NAME_BAKIYE_IZIN_TIPI + " IS NULL");
		if (departman != null && departman.isAdminMi())
			sb.append(" AND I." + IzinTipi.COLUMN_NAME_DEPARTMAN + " = " + departman.getId());
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			List<IzinTipi> izinList = pdksEntityController.getObjectBySQLList(sb, fields, IzinTipi.class);
			manuelGir = izinList.isEmpty();
			izinList = null;
		} catch (Exception e) {
		}

		return manuelGir;
	}

	/**
	 * @param session
	 * @param user
	 * @return
	 */
	public List getIzinOnayDurum(Session session, User user) {
		HashMap fields = new HashMap();
		User loginUser = authenticatedUser != null ? authenticatedUser : new User();
		if (user == null)
			user = loginUser;
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT MIN(P.BASLANGIC_ZAMANI) BASLANGIC_ZAMANI,MAX(BITIS_ZAMANI) BITIS_ZAMANI    FROM  ONAY_BEKLEYEN_IZIN_VIEW  P WITH(nolock) ");
		sb.append(" where (P.KULLANICI_ID=:kullaniciId AND P.ONAY_ID IS NOT NULL)");
		fields.put("kullaniciId", user.getId());
		if (user.isIK()) {
			if (user.isIKAdmin())
				sb.append(" OR (IZIN_DURUMU=3)");
			else {
				sb.append(" OR (IZIN_DURUMU=3 AND DEPARTMAN_ID=:d)");
				fields.put("d", user.getDepartman().getId());
			}
		}
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		fields.put("kullaniciId", user.getId());
		List izinList = null;
		try {
			izinList = pdksEntityController.getObjectBySQLList(sb, fields, null);
		} catch (Exception e) {
		}

		return izinList;
	}

	/**
	 * @param list
	 * @param user
	 * @param session
	 */
	public void vardiyaGunSirala(List<VardiyaGun> list, User user, Session session) {
		User loginUser = authenticatedUser != null ? authenticatedUser : new User();
		if (user == null)
			user = loginUser;
		TreeMap<String, List<VardiyaGun>> sirketParcalaMap = new TreeMap<String, List<VardiyaGun>>();
		List<Liste> listeler = new ArrayList<Liste>();
		List<Long> tesisList = null;
		boolean tesisYetki = getParameterKey("tesisYetki").equals("1");
		if (tesisYetki && session != null && user != null) {
			setUserTesisler(user, session);
			if (user.getYetkiliTesisler() != null) {
				tesisList = new ArrayList<Long>();
				for (Tanim tesis : loginUser.getYetkiliTesisler())
					tesisList.add(tesis.getId());

			}

		}
		for (VardiyaGun vardiyaGun : list) {
			Personel personel = vardiyaGun.getPersonel();
			if (tesisList != null && personel.getTesis() != null && !tesisList.contains(personel.getTesis().getId()))
				continue;
			String key = personel.getSirket().getAd() + "_" + (personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
			List<VardiyaGun> ozelList = sirketParcalaMap.containsKey(key) ? sirketParcalaMap.get(key) : new ArrayList<VardiyaGun>();
			if (ozelList.isEmpty()) {
				Liste liste = new Liste(key, ozelList);
				liste.setSelected(key);
				listeler.add(liste);
				sirketParcalaMap.put(key, ozelList);
			}
			ozelList.add(vardiyaGun);
		}
		sirketParcalaMap = null;
		list.clear();
		if (!listeler.isEmpty()) {
			if (listeler.size() > 1)
				listeler = PdksUtil.sortObjectStringAlanList(listeler, "getSelected", null);
			for (Liste liste : listeler) {
				List<VardiyaGun> sirketSubeList = PdksUtil.sortObjectStringAlanList((List<VardiyaGun>) liste.getValue(), "getSortBolumKey", null);
				list.addAll(sirketSubeList);
				sirketSubeList = null;
			}
		}
		listeler = null;
	}

	/**
	 * @param zamani
	 * @return
	 */
	public Integer[] getSaatDakika(String zamani) {
		Integer[] veri = new Integer[2];
		Integer saat = null;
		Integer dakika = null;
		if (PdksUtil.hasStringValue(zamani)) {
			String[] parca = zamani.split(":");
			if (parca.length < 3) {
				for (int i = 0; i < parca.length; i++) {
					switch (i) {
					case 0:
						try {
							saat = Integer.parseInt(parca[i]);
							if (saat > 23)
								saat = null;
							else
								dakika = 0;
						} catch (Exception e) {
							saat = null;
						}
						break;
					case 1:
						try {
							dakika = Integer.parseInt(parca[i]);
							if (dakika < 0 || dakika > 59)
								dakika = 0;
						} catch (Exception e) {
							dakika = 0;
						}
						break;
					default:
						break;
					}
				}

				if (saat == null)
					dakika = null;

			}
		}
		veri[0] = saat;
		veri[1] = dakika;
		return veri;
	}

	/**
	 * @param aramaSecenekleri
	 * @param veriLastMap
	 */
	public void setAramaSecenekleriFromVeriLast(AramaSecenekleri aramaSecenekleri, LinkedHashMap<String, Object> veriLastMap) {
		try {
			if (veriLastMap.containsKey("departmanId"))
				aramaSecenekleri.setDepartmanId(Long.parseLong((String) veriLastMap.get("departmanId")));
			if (veriLastMap.containsKey("sirketId"))
				aramaSecenekleri.setSirketId(Long.parseLong((String) veriLastMap.get("sirketId")));
			if (veriLastMap.containsKey("tesisId"))
				aramaSecenekleri.setTesisId(Long.parseLong((String) veriLastMap.get("tesisId")));
			if (veriLastMap.containsKey("ekSaha1Id"))
				aramaSecenekleri.setEkSaha1Id(Long.parseLong((String) veriLastMap.get("ekSaha1Id")));
			if (veriLastMap.containsKey("ekSaha2Id"))
				aramaSecenekleri.setEkSaha2Id(Long.parseLong((String) veriLastMap.get("ekSaha2Id")));
			if (veriLastMap.containsKey("bolumId"))
				aramaSecenekleri.setEkSaha3Id(Long.parseLong((String) veriLastMap.get("bolumId")));
			if (veriLastMap.containsKey("ekSaha3Id"))
				aramaSecenekleri.setEkSaha3Id(Long.parseLong((String) veriLastMap.get("ekSaha3Id")));
			if (veriLastMap.containsKey("ekSaha4Id"))
				aramaSecenekleri.setEkSaha4Id(Long.parseLong((String) veriLastMap.get("ekSaha4Id")));
			if (veriLastMap.containsKey("sicilNo"))
				aramaSecenekleri.setSicilNo((String) veriLastMap.get("sicilNo"));
			if (veriLastMap.containsKey("ad"))
				aramaSecenekleri.setAd((String) veriLastMap.get("ad"));
			if (veriLastMap.containsKey("soyad"))
				aramaSecenekleri.setSoyad((String) veriLastMap.get("soyad"));
		} catch (Exception e) {
			logger.error(e);
		}

	}

	/**
	 * @param denklestirmeAy
	 * @param session
	 * @return
	 */
	public Boolean getModelGoster(DenklestirmeAy denklestirmeAy, Session session) {
		Boolean modelGoster = Boolean.FALSE;
		if (denklestirmeAy != null) {
			HashMap parametreMap = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT DISTINCT D." + PersonelDenklestirme.COLUMN_NAME_CALISMA_MODELI_AY + " FROM " + PersonelDenklestirme.TABLE_NAME + " D WITH(nolock) ");
			sb.append(" WHERE D." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = :d ");
			parametreMap.put("d", denklestirmeAy.getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				List list = pdksEntityController.getObjectBySQLList(sb, parametreMap, null);
				modelGoster = list.size() > 1;
				list = null;
			} catch (Exception e) {
				logger.error(e);
			}

		}
		return modelGoster;

	}

	/**
	 * @param session
	 * @return
	 */
	public List<Departman> fillDepartmanTanimList(Session session) {
		User loginUser = authenticatedUser != null ? authenticatedUser : new User();
		HashMap parametreMap = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT DISTINCT D.* FROM " + Sirket.TABLE_NAME + " S WITH(nolock) ");
		sb.append(" INNER JOIN " + Departman.TABLE_NAME + " D ON D." + Departman.COLUMN_NAME_ID + " = S." + Sirket.COLUMN_NAME_DEPARTMAN + " AND D." + Departman.COLUMN_NAME_DURUM + " = 1 ");
		sb.append(" WHERE S." + Sirket.COLUMN_NAME_DURUM + " = 1 ");
		sb.append(" ORDER BY D." + Departman.COLUMN_NAME_ADMIN_DURUM + " DESC,D." + Departman.COLUMN_NAME_ID);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Departman> list = pdksEntityController.getObjectBySQLList(sb, parametreMap, Departman.class);
		if (loginUser.isIK() && !loginUser.getDepartman().isAdminMi()) {
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Departman pdksDepartman = (Departman) iterator.next();
				if (pdksDepartman.isAdminMi())
					iterator.remove();
			}
		}
		return list;
	}

	/**
	 * @param sirketList
	 * @return
	 */
	public List<SelectItem> getIzinSirketItemList(List<Sirket> sirketList) {
		List<SelectItem> sirketItemList = new ArrayList<SelectItem>();
		if (sirketList != null) {
			for (Sirket sirket : sirketList) {
				if (sirket.getDepartman().getIzinGirilebilir() && sirket.getFazlaMesai()) {
					SelectItem selectItem = new SelectItem(sirket.getId(), sirket.getAd());
					sirketItemList.add(selectItem);
				}
			}
		}
		return sirketItemList;
	}

	/**
	 * @return
	 */
	public PdksSoapVeriAktar getPdksSoapVeriAktar() {
		PdksSoapVeriAktar service = null;
		String servisAdres = getParameterKey("pdksWebService");
		if (!PdksUtil.hasStringValue(servisAdres))
			servisAdres = "http://localhost:9080/PdksWebService";
		if (!servisAdres.startsWith("http"))
			servisAdres = "http://" + servisAdres;
		PdksSoapVeriAktarService jaxws = new PdksSoapVeriAktarService();
		service = jaxws.getPdksSoapVeriAktarPort();
		BindingProvider bindingProvider = (BindingProvider) service;
		bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, servisAdres + "/services/PdksSoapVeriAktarPort");
		return service;
	}

	/**
	 * @param personelDenklestirme
	 * @param session
	 */
	public void setCalismaModeliAy(PersonelDenklestirme personelDenklestirme, Session session) {
		CalismaModeliAy calismaModeliAy = null;
		if (personelDenklestirme.getDenklestirmeAy() != null && personelDenklestirme.getPersonel() != null && personelDenklestirme.getPersonel().getCalismaModeli() != null) {
			HashMap fields = new HashMap();
			fields.put("denklestirmeAy.id", personelDenklestirme.getDenklestirmeAy().getId());
			fields.put("calismaModeli.id", personelDenklestirme.getPersonel().getCalismaModeli().getId());
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			calismaModeliAy = (CalismaModeliAy) pdksEntityController.getObjectByInnerObject(fields, CalismaModeliAy.class);
			personelDenklestirme.setCalismaModeliAy(calismaModeliAy);
		}

	}

	/**
	 * @param denklestirmeAy
	 * @param cm
	 * @param session
	 * @return
	 */
	public CalismaModeliAy getCalismaModeliAy(DenklestirmeAy denklestirmeAy, CalismaModeli cm, Session session) {
		CalismaModeliAy calismaModeliAy = null;
		if (denklestirmeAy != null && cm != null) {
			HashMap fields = new HashMap();
			fields.put("denklestirmeAy.id", denklestirmeAy.getId());
			fields.put("calismaModeli.id", cm.getId());
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			calismaModeliAy = (CalismaModeliAy) pdksEntityController.getObjectByInnerObject(fields, CalismaModeliAy.class);
		}
		return calismaModeliAy;
	}

	/**
	 * @param dosya
	 * @return
	 * @throws IOException
	 */
	public Workbook getWorkbook(Dosya dosya) throws IOException {
		Workbook wb = null;
		if (dosya != null && dosya.getDosyaIcerik() != null) {
			ByteArrayInputStream bis = new ByteArrayInputStream(dosya.getDosyaIcerik());
			if (dosya.getDosyaAdi() != null && dosya.getDosyaAdi().endsWith(".xls"))
				wb = new HSSFWorkbook(bis);
			else
				wb = new XSSFWorkbook(bis);
			bis = null;
		}

		return wb;
	}

	/**
	 * @param cal
	 * @param gunCikar
	 */
	public void gunCikar(Calendar cal, int gunCikar) {

		switch (cal.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.MONDAY:
			gunCikar += 3;
			break;
		case Calendar.SUNDAY:
			gunCikar += 2;
			break;
		case Calendar.SATURDAY:
			gunCikar += 1;
			break;
		default:

			break;
		}
		cal.add(Calendar.DATE, -gunCikar);
	}

	/**
	 * @param mailAdres
	 * @param grupAyristir
	 * @return
	 */
	public List<User> getMailUser(String mailAdres, boolean grupAyristir) {
		TreeMap<String, User> userMap = new TreeMap<String, User>();
		if (mailAdres != null && mailAdres.indexOf("@") > 1) {
			try {
				List<String> mailList = PdksUtil.getListByString(mailAdres, null);
				for (String eMail : mailList) {
					InternetAddress internetAddress = new InternetAddress(eMail);
					eMail = internetAddress.getAddress();
					User user = LDAPUserManager.getLDAPUserAttributes(eMail, LDAPUserManager.USER_ATTRIBUTES_MAIL);
					if (user == null) {
						User userGroup = LDAPUserManager.getLDAPUserAttributes(eMail, "");
						boolean userBos = Boolean.TRUE;
						if (userGroup != null) {
							if (!grupAyristir) {
								user = userGroup;
								userMap.put(userGroup.getEmail(), userGroup);
								userBos = Boolean.FALSE;
							} else if (!userGroup.getYetkiliPersonelNoList().isEmpty()) {
								for (String str : userGroup.getYetkiliPersonelNoList()) {
									String[] member = str.split(",");
									User user1 = LDAPUserManager.getLDAPUserAttributes(member[0].substring(3), "CN");
									if (user1 != null) {
										userBos = Boolean.FALSE;
										userMap.put(user1.getEmail(), user1);
									}
								}
							}
						}
						if (userBos) {
							user = new User();
							user.setEmail(eMail);
							userMap.put(user.getEmail(), user);
						}
					} else
						userMap.put(user.getEmail(), user);
				}

			} catch (Exception e) {
			}

		}
		List<User> allList = !userMap.isEmpty() ? new ArrayList<User>(userMap.values()) : new ArrayList<User>();
		return allList;

	}

	/**
	 * @param login
	 * @param userList
	 * @param geciciPersoneller
	 * @param method
	 */
	private void addUserList(User login, List<User> userList, List<Personel> yetkiliPersoneller, String method) {
		User user = new User();
		user.setId(login.getId());
		user.setDepartman(login.getDepartman());
		user.setPdksPersonel(login.getPdksPersonel());
		user.setYetkiliTesisler(login.getYetkiliTesisler());
		if (yetkiliPersoneller != null)
			user.setYetkiliPersoneller(yetkiliPersoneller);
		try {
			Boolean durum = new Boolean(Boolean.TRUE);
			PdksUtil.runMethodObject(user, method, new Object[] { durum });
			user.setYetkiSet(Boolean.TRUE);
		} catch (Exception e) {
			logger.error(e);
			user = null;
		}
		if (user != null)
			userList.add(user);

	}

	/**
	 * @param gelenUser
	 * @param tarih
	 * @param aramaSecenekleriPer
	 * @param session
	 * @return
	 */
	public List<Personel> getAramaSecenekleriPersonelList(User gelenUser, Date tarih, AramaSecenekleri aramaSecenekleriPer, Session session) {
		User loginUser = authenticatedUser != null ? authenticatedUser : new User();
		if (gelenUser == null)
			gelenUser = loginUser;

		if (aramaSecenekleriPer == null)
			aramaSecenekleriPer = new AramaSecenekleri();

		TreeMap<Long, Personel> perMap = new TreeMap<Long, Personel>();
		String adi = aramaSecenekleriPer.getAd();
		String soyadi = aramaSecenekleriPer.getSoyad();
		String sicilNo = aramaSecenekleriPer.getSicilNo();
		Date bugun = PdksUtil.getDate(new Date());
		if (tarih == null)
			tarih = bugun;
		List<User> userList = new ArrayList<User>(), islemUserList = new ArrayList<User>();
		islemUserList.add(gelenUser);
		HashMap parametreMap = new HashMap();
		boolean adminRole = getAdminRole(gelenUser);
		if (adminRole == false && aramaSecenekleriPer.isYetkiliPersoneller() == false) {
			parametreMap.clear();
			parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "vekaletVeren");
			parametreMap.put("yeniYonetici.id=", gelenUser.getId());
			parametreMap.put("bitTarih>=", bugun);
			parametreMap.put("basTarih<=", bugun);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<User> vekilUserList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, UserVekalet.class);
			for (User vekaletVeren : vekilUserList) {
				if (vekaletVeren.isDurum() && vekaletVeren.getPdksPersonel().isCalisiyor()) {
					setUserRoller(vekaletVeren, session);
					islemUserList.add(vekaletVeren);
				}

			}

		}

		for (User user : islemUserList) {
			adminRole = getAdminRole(user);
			List<Personel> geciciPersoneller = null;
			if (adminRole == false && aramaSecenekleriPer.isYetkiliPersoneller() == false) {
				parametreMap.clear();
				parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "personelGecici");
				parametreMap.put("bitTarih>=", bugun);
				parametreMap.put("basTarih<=", bugun);
				parametreMap.put("yeniYonetici.id=", user.getId());
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				geciciPersoneller = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, PersonelGeciciYonetici.class);
				if (geciciPersoneller != null && geciciPersoneller.isEmpty())
					geciciPersoneller = null;
			}
			if (adminRole || user.isGenelMudur() || user.isIKDirektor())
				addUserList(user, userList, geciciPersoneller, "setAdmin");
			else {
				if (user.isIK() || user.isTaseronAdmin())
					addUserList(user, userList, geciciPersoneller, "setIK");
				if (user.isProjeMuduru())
					addUserList(user, userList, geciciPersoneller, "setProjeMuduru");
				if (user.isYoneticiKontratli() || user.isYonetici())
					addUserList(user, userList, geciciPersoneller, "setYonetici");
				if (user.isDirektorSuperVisor())
					addUserList(user, userList, geciciPersoneller, "setDirektorSuperVisor");
				if (user.isSuperVisor())
					addUserList(user, userList, geciciPersoneller, "setSuperVisor");
				if (user.isTesisYonetici())
					addUserList(user, userList, geciciPersoneller, "setTesisYonetici");
			}
		}
		islemUserList = null;

		for (User islemUser : userList) {
			if (islemUser == null)
				continue;
			parametreMap.clear();
			Personel personel = islemUser.getPdksPersonel();
			Departman departman = islemUser.getDepartman();
			Sirket sirket = personel.getSirket();
			StringBuffer sb = new StringBuffer();
			sb.append("WITH PERSONELLER AS ( ");
			if (islemUser.getYetkiliPersoneller() != null) {
				StringBuffer perSb = new StringBuffer();
				for (Iterator iterator = islemUser.getYetkiliPersoneller().iterator(); iterator.hasNext();) {
					Personel personelDiger = (Personel) iterator.next();
					perSb.append(personelDiger.getId() + (iterator.hasNext() ? ", " : ""));
				}
				String str = perSb.toString();
				perSb = null;
				sb.append("SELECT P.* from " + Personel.TABLE_NAME + " P WITH(nolock)  ");
				sb.append(" INNER JOIN " + Sirket.TABLE_NAME + " S ON  S." + Sirket.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_SIRKET + " AND S." + Sirket.COLUMN_NAME_PDKS + " = 1 ");
				sb.append(" WHERE P." + Personel.COLUMN_NAME_ID + " " + (str.indexOf(",") > 0 ? " IN ( " + str + " )" : " = " + str));
				sb.append(" AND  P." + Personel.COLUMN_NAME_MAIL_TAKIP + " = 1 ");

				Long seciliSirketId = aramaSecenekleriPer.getSirketId();
				if (seciliSirketId != null)
					sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + " = " + seciliSirketId);

				Long tesisId = aramaSecenekleriPer.getTesisId();
				if (tesisId != null)
					sb.append(" AND P." + Personel.COLUMN_NAME_TESIS + " = " + tesisId);

				Long ekSaha1Id = aramaSecenekleriPer.getEkSaha1Id();
				if (ekSaha1Id != null)
					sb.append(" AND P." + Personel.COLUMN_NAME_EK_SAHA1 + " = " + ekSaha1Id);

				sb.append(" UNION ALL ");
			}
			sb.append("SELECT P.* from " + Personel.TABLE_NAME + " P WITH(nolock)  ");
			sb.append(" INNER JOIN " + Sirket.TABLE_NAME + " S ON  S." + Sirket.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_SIRKET + " AND S." + Sirket.COLUMN_NAME_PDKS + " = 1 ");

			if (islemUser.isIK())
				sb.append(" AND S." + Sirket.COLUMN_NAME_DEPARTMAN + " = " + departman.getId());

			sb.append(" WHERE P." + Personel.COLUMN_NAME_MAIL_TAKIP + " = 1 ");

			Long seciliSirketId = aramaSecenekleriPer.getSirketId();
			if (islemUser.isProjeMuduru() || islemUser.isSuperVisor())
				seciliSirketId = sirket.getId();
			if (seciliSirketId != null)
				sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + " = " + seciliSirketId);

			Long tesisId = aramaSecenekleriPer.getTesisId();
			if (islemUser.isTesisYonetici())
				tesisId = islemUser.getPdksPersonel().getTesis() != null ? islemUser.getPdksPersonel().getTesis().getId() : null;
			if (islemUser.getYetkiliTesisler() != null) {
				List<Long> list = new ArrayList<Long>();
				for (Tanim tanim : islemUser.getYetkiliTesisler())
					list.add(tanim.getId());
				if (tesisId != null) {
					if (list.contains(tesisId)) {
						tesisId = null;
					} else
						list.clear();
				}
				if (!list.isEmpty()) {
					sb.append(" AND P." + Personel.COLUMN_NAME_TESIS + " IN (");
					for (Iterator iterator = list.iterator(); iterator.hasNext();) {
						Long long1 = (Long) iterator.next();
						sb.append(" " + long1 + (iterator.hasNext() ? "," : ""));
					}
					sb.append(" )");
				}
			}
			if (tesisId != null)
				sb.append(" AND P." + Personel.COLUMN_NAME_TESIS + " = " + tesisId);

			if (islemUser.isYonetici() || islemUser.isSuperVisor())
				sb.append(" AND P." + Personel.COLUMN_NAME_YONETICI + " = " + personel.getId());

			Long ekSaha1Id = aramaSecenekleriPer.getEkSaha1Id();
			if (islemUser.isDirektorSuperVisor())
				ekSaha1Id = islemUser.getPdksPersonel().getEkSaha1() != null ? islemUser.getPdksPersonel().getEkSaha1().getId() : 0L;
			if (ekSaha1Id != null)
				sb.append(" AND P." + Personel.COLUMN_NAME_EK_SAHA1 + " = " + ekSaha1Id);

			sb.append(" ) ");
			sb.append(" SELECT DISTINCT P." + Personel.COLUMN_NAME_ID + " FROM PERSONELLER P  ");

			sb.append(" WHERE P." + Personel.COLUMN_NAME_DURUM + " = 1");
			sb.append(" AND P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " <= :t1 AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= :t2");
			parametreMap.put("t1", tarih);
			parametreMap.put("t2", tarih);

			if (PdksUtil.hasStringValue(adi)) {
				sb.append(" AND P." + Personel.COLUMN_NAME_AD + " LIKE :ad");
				parametreMap.put("ad", adi.trim() + "%");
			}

			if (PdksUtil.hasStringValue(soyadi)) {
				sb.append(" AND P." + Personel.COLUMN_NAME_SOYAD + " LIKE :soyad");
				parametreMap.put("soyad", soyadi.trim() + "%");
			}

			if (PdksUtil.hasStringValue(sicilNo)) {
				sb.append(" AND P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " = :sicilNo");
				parametreMap.put("sicilNo", sicilNo.trim());
			}
			if (aramaSecenekleriPer.getEkSaha2Id() != null)
				sb.append(" AND P." + Personel.COLUMN_NAME_EK_SAHA2 + " = " + aramaSecenekleriPer.getEkSaha2Id());

			if (aramaSecenekleriPer.getEkSaha3Id() != null)
				sb.append(" AND P." + Personel.COLUMN_NAME_EK_SAHA3 + " = " + aramaSecenekleriPer.getEkSaha3Id());

			if (aramaSecenekleriPer.getEkSaha4Id() != null)
				sb.append(" AND P." + Personel.COLUMN_NAME_EK_SAHA4 + " = " + aramaSecenekleriPer.getEkSaha4Id());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				List<Personel> personelList = getPersonelList(sb, parametreMap);
				for (Personel personel2 : personelList) {
					if (!perMap.containsKey(personel2.getId()))
						perMap.put(personel2.getId(), personel2);
				}
			} catch (Exception e) {
				logger.error(e + "\n" + sb.toString());
			}

		}
		List<Personel> list = !perMap.isEmpty() ? new ArrayList<Personel>(perMap.values()) : new ArrayList<Personel>();
		if (!list.isEmpty())
			list = PdksUtil.sortObjectStringAlanList(null, list, "getAdSoyad", null);

		return list;

	}

	/**
	 * @param user
	 * @return
	 */
	public boolean getAdminRole(User user) {
		boolean adminRole = false;
		if (user != null)
			adminRole = user.isIKAdmin() || user.isSistemYoneticisi() || user.isAdmin();
		return adminRole;
	}

	/**
	 * @param session
	 */
	public List getStajerOlmayanSirketler(List<Sirket> sirketler) {
		List<Sirket> sirketList = new ArrayList<Sirket>();
		if (sirketler != null) {
			for (Sirket sirket : sirketler) {
				sirketList.add(sirket);
			}
		}
		return sirketList;

	}

	/**
	 * @param session
	 */
	public void kgsMasterUpdate(Session session) {
		LinkedHashMap fields = new LinkedHashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SP_GET_PDKS_ISLEM");
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			pdksEntityController.execSP(fields, sb);

		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		sb = null;
	}

	/**
	 * @param idler
	 * @return
	 */
	public String getListIdStr(List idler) {
		StringBuffer sb = new StringBuffer();
		if (idler != null && !idler.isEmpty()) {
			for (Object long1 : idler) {
				if (long1 != null) {
					if (sb.length() > 0)
						sb.append(", ");
					sb.append(long1.toString());
				}
			}

		}
		String idStr = sb.length() > 0 ? sb.toString() : "";
		sb = null;
		return idStr;
	}

	/**
	 * @param kapiId
	 * @param personel
	 * @param basTarih
	 * @param bitTarih
	 * @param class1
	 * @param session
	 * @return
	 */
	public List getHareketAktifBilgileri(List<Long> kapiId, List<Long> personel, Date basTarih, Date bitTarih, Class class1, Session session) throws Exception {
		List list = getHareketBilgileri(kapiId, personel, basTarih, bitTarih, class1, session);
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Object object = (Object) iterator.next();
			if (object != null) {
				if (object instanceof HareketKGS) {
					HareketKGS hareket = (HareketKGS) object;
					if (hareket.getDurum() > HareketKGS.DURUM_PASIF)
						continue;
					iterator.remove();
				} else if (object instanceof BasitHareket) {
					BasitHareket hareket = (BasitHareket) object;
					if (hareket.getDurum() > HareketKGS.DURUM_PASIF)
						continue;
					iterator.remove();
				}
			}

		}
		return list;
	}

	/**
	 * @param hareketKGSList
	 * @param hareketKGS
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public List getHareketIdBilgileri(List<HareketKGS> hareketKGSList, HareketKGS hareketKGS, Date basTarih, Date bitTarih, Session session) throws Exception {

		List<HareketKGS> idler = new ArrayList<HareketKGS>();
		if (hareketKGSList != null && !hareketKGSList.isEmpty())
			idler.addAll(hareketKGSList);
		if (hareketKGS != null && PdksUtil.hasStringValue(hareketKGS.getId()))
			idler.add(hareketKGS);
		List list = new ArrayList();
		while (!idler.isEmpty()) {
			int sayi = 0;
			StringBuffer kgs = new StringBuffer(), pdks = new StringBuffer();
			for (Iterator iterator = idler.iterator(); iterator.hasNext();) {
				HareketKGS kgs2 = (HareketKGS) iterator.next();
				String id = kgs2.getId(), tableId = kgs2.getHareketTableId() != null ? String.valueOf(kgs2.getHareketTableId()) : null;
				if (id != null && tableId != null) {
					if (id.startsWith(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_PDKS)) {
						if (pdks.length() > 0)
							pdks.append(", ");
						pdks.append(tableId);
					} else if (id.startsWith(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_KGS)) {
						if (kgs.length() > 0)
							kgs.append(", ");
						kgs.append(tableId);
					}
					++sayi;
				}
				iterator.remove();
				if (sayi == 1000)
					break;

			}
			if (kgs.length() > 0 || pdks.length() > 0) {
				StringBuffer sb = new StringBuffer();
				sb.append("SP_GET_HAREKET_BY_ID_SIRKET");
				LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
				fields.put("kgs", kgs.length() > 0 ? kgs.toString() : null);
				fields.put("pdks", pdks.length() > 0 ? pdks.toString() : null);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List newList = pdksEntityController.execSPList(fields, sb, BasitHareket.class);
				if (!newList.isEmpty()) {
					list.clear();
					getHareketKGSByBasitHareketList(newList, null, session);
					list.addAll(newList);
				}

				newList = null;
				sb = null;
			}
			kgs = null;
			pdks = null;
		}

		idler = null;
		return list;
	}

	/**
	 * @param pdks
	 * @param kapiIdList
	 * @param personelList
	 * @param basTarih
	 * @param bitTarih
	 * @param class1
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public List getPdksHareketBilgileri(Boolean pdks, List<Long> kapiIdList, List personelList, Date basTarih, Date bitTarih, Class class1, Session session) throws Exception {
		List<Long> personelId = null;
		if (personelList != null && !personelList.isEmpty()) {
			personelId = new ArrayList<Long>();
			for (Object object : personelList) {
				Personel personel = null;
				if (object instanceof Personel)
					personel = (Personel) object;
				else if (object instanceof PersonelView) {
					PersonelView personelView = (PersonelView) object;
					personel = personelView.getPdksPersonel();
				}
				if (personel == null || pdks != null && (personel.getPdks() == null || !personel.getPdks().equals(pdks)))
					continue;
				Long id = personel.getPersonelKGS().getId();
				if (!personelId.contains(id))
					personelId.add(id);

			}
			if (personelId.isEmpty())
				personelId = null;
		}
		List list = getHareketAktifBilgileri(kapiIdList, personelId, basTarih, bitTarih, class1, session);
		return list;
	}

	/**
	 * @param id
	 * @param session
	 * @return
	 */
	public Tanim getTanimById(Long id, Session session) {
		Tanim tanim = null;
		if (id != null) {
			HashMap fields = new HashMap();
			fields.put("id", id);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			tanim = (Tanim) pdksEntityController.getObjectByInnerObject(fields, Tanim.class);
			fields = null;
		}
		return tanim;
	}

	/**
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 * @return
	 */
	public String getBirdenFazlaKGSSirketSQL(Date basTarih, Date bitTarih, Session session) {
		String str = "K." + PersonelKGS.COLUMN_NAME_SICIL_NO + " = P." + PersonelKGS.COLUMN_NAME_SICIL_NO + " AND K." + PersonelKGS.COLUMN_NAME_ID + " <> P." + PersonelKGS.COLUMN_NAME_ID;
		String birdenFazlaKGSSirketSQL = getParameterKey("birdenFazlaKGSSirketSQL"), sql = str;
		if (PdksUtil.hasStringValue(birdenFazlaKGSSirketSQL)) {
			Calendar cal = Calendar.getInstance();
			HashMap map = new HashMap();
			map.put("id>", 0L);
			if (bitTarih != null)
				map.put("basTarih<=", tariheGunEkleCikar(cal, bitTarih, +7));
			if (basTarih != null)
				map.put("bitTarih>=", tariheGunEkleCikar(cal, basTarih, -7));
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<KapiSirket> list = pdksEntityController.getObjectByInnerObjectListInLogic(map, KapiSirket.class);
			if (!list.isEmpty()) {
				sql = birdenFazlaKGSSirketSQL;
				if (sql.indexOf(str) >= 0)
					sql = PdksUtil.replaceAllManuel(sql, str, "");
				else {
					int andIndex = sql.toUpperCase().indexOf("AND");
					if (andIndex > 1 || andIndex < 0)
						sql = " AND " + sql;
				}
				sql = PdksUtil.replaceAllManuel(str + sql, "  ", " ");
			}
		}
		return sql;
	}

	/**
	 * @param logic
	 * @param method
	 * @param uzerineYaz
	 * @param dataIdList
	 * @param fieldName
	 * @param fieldsOrj
	 * @param class1
	 * @param session
	 * @return
	 */
	public TreeMap getParamTreeMap(boolean logic, String method, boolean uzerineYaz, List dataIdList, String fieldName, HashMap<String, Object> fieldsOrj, Class class1, Session session) {
		TreeMap treeMap = null;
		if (fieldsOrj != null && fieldsOrj.containsKey(PdksEntityController.MAP_KEY_MAP)) {
			if (PdksUtil.hasStringValue(method) == false)
				method = (String) fieldsOrj.get(PdksEntityController.MAP_KEY_MAP);
			fieldsOrj.remove(PdksEntityController.MAP_KEY_MAP);
		}
		List veriList = getParamList(logic, dataIdList, fieldName, fieldsOrj, class1, session);
		if (veriList != null)
			treeMap = pdksEntityController.getTreeMapByList(veriList, method, uzerineYaz);
		return treeMap;
	}

	/**
	 * @param logic
	 * @param dataIdList
	 * @param fieldName
	 * @param fieldsOrj
	 * @param class1
	 * @param session
	 * @return
	 */
	public List getParamList(boolean logic, List dataIdList, String fieldName, HashMap<String, Object> fieldsOrj, Class class1, Session session) {
		List idList = new ArrayList();
		List veriList = new ArrayList();
		try {
			int size = PdksEntityController.LIST_MAX_SIZE - fieldsOrj.size();
			if (session == null && fieldsOrj != null && fieldsOrj.containsKey(PdksEntityController.MAP_KEY_SESSION))
				session = (Session) fieldsOrj.get(PdksEntityController.MAP_KEY_SESSION);
			List idInputList = new ArrayList(dataIdList);
			while (!idInputList.isEmpty()) {
				HashMap map = new HashMap();
				for (Iterator iterator = idInputList.iterator(); iterator.hasNext();) {
					Object long1 = (Object) iterator.next();
					idList.add(long1);
					iterator.remove();
					if (idList.size() + map.size() >= size)
						break;
				}
				HashMap<String, Object> fields = new HashMap<String, Object>();
				fields.putAll(fieldsOrj);
				if (fields.containsKey(PdksEntityController.MAP_KEY_SESSION))
					fields.remove(PdksEntityController.MAP_KEY_SESSION);
				Object data = idList;
				String key = fieldName;
				if (idList.size() == 1 && fields.containsKey(fieldName)) {
					fields.remove(fieldName);
					data = idList.get(0);
					if (logic)
						key = fieldName + " = ";
				}
				fields.put(key, data);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);

				try {
					List list = logic == false ? pdksEntityController.getObjectByInnerObjectList(fields, class1) : pdksEntityController.getObjectByInnerObjectListInLogic(fields, class1);
					if (!list.isEmpty())
						veriList.addAll(list);
					list = null;
				} catch (Exception e) {
					logger.error(e);
					idInputList.clear();
				}

				fields = null;
				idList.clear();
			}
			idInputList = null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();

		}

		return veriList;

	}

	/**
	 * @param method
	 * @param uzerineYaz
	 * @param dataIdList
	 * @param sb
	 * @param fieldName
	 * @param fieldsOrj
	 * @param class1
	 * @param session
	 * @return
	 */
	public TreeMap getSQLParamTreeMap(String method, boolean uzerineYaz, List dataIdList, StringBuffer sb, String fieldName, HashMap<String, Object> fieldsOrj, Class class1, Session session) {
		TreeMap treeMap = null;
		if (fieldsOrj != null && fieldsOrj.containsKey(PdksEntityController.MAP_KEY_MAP)) {
			if (PdksUtil.hasStringValue(method) == false)
				method = (String) fieldsOrj.get(PdksEntityController.MAP_KEY_MAP);
			fieldsOrj.remove(PdksEntityController.MAP_KEY_MAP);
		}
		List veriList = getSQLParamList(dataIdList, sb, fieldName, fieldsOrj, class1, session);
		if (veriList != null)
			treeMap = pdksEntityController.getTreeMapByList(veriList, method, uzerineYaz);
		return treeMap;
	}

	/**
	 * @param dataIdList
	 * @param sb
	 * @param fieldName
	 * @param fieldsOrj
	 * @param class1
	 * @param session
	 * @return
	 */
	public List getSQLParamList(List dataIdList, StringBuffer sb, String fieldName, HashMap<String, Object> fieldsOrj, Class class1, Session session) {
		List idList = new ArrayList();
		List veriList = new ArrayList();
		try {
			if (session == null && fieldsOrj != null && fieldsOrj.containsKey(PdksEntityController.MAP_KEY_SESSION))
				session = (Session) fieldsOrj.get(PdksEntityController.MAP_KEY_SESSION);
			int size = PdksEntityController.LIST_MAX_SIZE - fieldsOrj.size();
			List idInputList = new ArrayList(dataIdList);
			String str = ":" + fieldName, sqlStr = sb.toString();
			while (!idInputList.isEmpty()) {
				HashMap map = new HashMap();
				for (Iterator iterator = idInputList.iterator(); iterator.hasNext();) {
					Object long1 = (Object) iterator.next();
					idList.add(long1);
					iterator.remove();
					if (idList.size() + map.size() >= size)
						break;
				}
				HashMap<String, Object> fields = new HashMap<String, Object>();
				fields.putAll(fieldsOrj);
				if (fields.containsKey(PdksEntityController.MAP_KEY_SESSION))
					fields.remove(PdksEntityController.MAP_KEY_SESSION);
				if (idList.size() > 1 || sqlStr.indexOf(str) < 1)
					fields.put(fieldName, idList);
				else {
					sb = new StringBuffer(PdksUtil.replaceAll(sqlStr, str, " = :" + fieldName));
					fields.put(fieldName, idList.get(0));
				}
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				try {
					List list = pdksEntityController.getObjectBySQLList(sb, fields, class1);
					if (!list.isEmpty())
						veriList.addAll(list);
					list = null;
				} catch (Exception e) {
					logger.error(sb + "\n" + e);
					idInputList.clear();
				}

				fields = null;
				idList.clear();
			}
			idInputList = null;
		} catch (Exception ex) {
			logger.error(sb + "\n" + ex);

		}

		return veriList;
	}

	/**
	 * @param personelIdler
	 * @param basTarih
	 * @param bitTarih
	 * @param saat
	 * @param session
	 * @return
	 */
	public List<VardiyaGun> getPersonelEksikVardiyaCalismaList(List<Long> personelIdler, Date basTarih, Date bitTarih, Session session) {
		Double saat = null;
		try {
			if (getParameterKeyHasStringValue("minGunCalismaSaat"))
				saat = Double.parseDouble(getParameterKey("minGunCalismaSaat"));
		} catch (Exception e) {
		}
		if (saat == null || saat.doubleValue() < 0.0d)
			saat = 0.5d;
		String formatStr = "yyyy-MM-dd HH:mm:ss";
		String basTarihStr = basTarih != null ? PdksUtil.convertToDateString(basTarih, formatStr) : null;
		String bitTarihStr = bitTarih != null ? PdksUtil.convertToDateString(bitTarih, formatStr) : null;
		String fieldName = "personel";
		LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
		fields.put("saat", saat);
		fields.put(fieldName, "");
		fields.put("basTarih", basTarihStr);
		fields.put("bitTarih", bitTarihStr);
		fields.put("df", null);
		List<VardiyaGun> list = getSPParamLongList(personelIdler, "SP_GET_EKSIK_CALISAN_VARDIYALAR", fieldName, fields, VardiyaGun.class, session);
		return list;
	}

	/**
	 * @param idInputList
	 * @param spName
	 * @param fieldName
	 * @param fieldsOrj
	 * @param class1
	 * @param session
	 * @return
	 */
	public List getSPParamLongList(List idInputList, String spName, String fieldName, LinkedHashMap<String, Object> fieldsOrj, Class class1, Session session) {
		StringBuffer sb = new StringBuffer(spName);
		List<Long> idList = new ArrayList<Long>();
		List veriList = new ArrayList();
		try {
			if (session == null && fieldsOrj != null && fieldsOrj.containsKey(PdksEntityController.MAP_KEY_SESSION))
				session = (Session) fieldsOrj.get(PdksEntityController.MAP_KEY_SESSION);
			int size = PdksEntityController.LIST_MAX_SIZE - fieldsOrj.size();
			if (idInputList == null) {
				LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
				fields.putAll(fieldsOrj);
				fields.put(fieldName, null);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				veriList = pdksEntityController.execSPList(fields, sb, class1);
			} else
				while (!idInputList.isEmpty()) {
					HashMap map = new HashMap();
					for (Iterator iterator = idInputList.iterator(); iterator.hasNext();) {
						Long long1 = (Long) iterator.next();
						idList.add(long1);
						iterator.remove();
						if (idList.size() + map.size() >= size)
							break;
					}
					LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
					fields.putAll(fieldsOrj);
					fields.put(fieldName, getListIdStr(idList));
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					List list = pdksEntityController.execSPList(fields, sb, class1);
					if (!list.isEmpty())
						veriList.addAll(list);
					list = null;
					fields = null;
					idList.clear();
				}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		return veriList;
	}

	/**
	 * @param kapiIdList
	 * @param personel
	 * @param basTarih
	 * @param bitTarih
	 * @param class1
	 * @param session
	 * @return
	 */
	public List getHareketBilgileri(List<Long> kapiIdIList, List<Long> personelIdInputList, Date basTarih, Date bitTarih, Class class1, Session session) throws Exception {
		List<Long> personelIdList = new ArrayList<Long>();
		if (personelIdInputList != null)
			personelIdList.addAll(personelIdInputList);
		Calendar cal = Calendar.getInstance();
		String formatStr = "yyyy-MM-dd HH:mm:ss";
		TreeMap<Long, Long> iliskiMap = new TreeMap<Long, Long>();
		StringBuffer sb = new StringBuffer();
		String birdenFazlaKGSSirketSQL = getBirdenFazlaKGSSirketSQL(tariheGunEkleCikar(cal, basTarih, -1), tariheGunEkleCikar(cal, bitTarih, 1), session);
		LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
		List list = new ArrayList();
		String kapi = getListIdStr(kapiIdIList);
		String basTarihStr = basTarih != null ? PdksUtil.convertToDateString(basTarih, formatStr) : null;
		String bitTarihStr = bitTarih != null ? PdksUtil.convertToDateString(bitTarih, formatStr) : null;
		Class class2 = class1.getName().equals(HareketKGS.class.getName()) ? BasitHareket.class : class1;
		List<Long> idList = new ArrayList<Long>();
		String fieldName = null;
		while (personelIdInputList == null || !personelIdList.isEmpty()) {
			sb = new StringBuffer();
			fields.clear();
			HashMap map = new HashMap();
			sb.append("SELECT P." + PersonelKGS.COLUMN_NAME_ID + ", K." + PersonelKGS.COLUMN_NAME_ID + " AS REF from " + PersonelKGS.TABLE_NAME + " P WITH(nolock) ");
			sb.append(" INNER JOIN " + PersonelKGS.TABLE_NAME + " K ON " + birdenFazlaKGSSirketSQL + " ");
			sb.append(" INNER JOIN " + KapiSirket.TABLE_NAME + " KS ON KS." + KapiSirket.COLUMN_NAME_ID + " = K." + PersonelKGS.COLUMN_NAME_KGS_SIRKET);
			if (basTarih != null) {
				sb.append(" AND KS." + KapiSirket.COLUMN_NAME_BIT_TARIH + " >= :b1 ");
				map.put("b1", tariheGunEkleCikar(cal, basTarih, -1));
			}
			if (bitTarih != null) {
				sb.append(" AND KS." + KapiSirket.COLUMN_NAME_BAS_TARIH + " <= :b2 ");
				map.put("b2", tariheGunEkleCikar(cal, bitTarih, 1));
			}
			sb.append(" WHERE P." + PersonelKGS.COLUMN_NAME_SICIL_NO + " <>''");
			if (personelIdInputList != null) {
				fieldName = "p";
				sb.append(" AND P." + PersonelKGS.COLUMN_NAME_ID + " :" + fieldName);
				for (Iterator iterator = personelIdList.iterator(); iterator.hasNext();) {
					Long long1 = (Long) iterator.next();
					idList.add(long1);
					iterator.remove();
					if (idList.size() + map.size() >= PdksEntityController.LIST_MAX_SIZE)
						break;
				}
				map.put(fieldName, idList);
			} else {
				idList = null;
				personelIdInputList = new ArrayList<Long>();
			}

			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				List<Object[]> perList = null;
				// perList = pdksEntityController.getObjectBySQLList(sb, map, null);
				if (fieldName != null)
					perList = getSQLParamList(idList, sb, fieldName, map, null, session);
				else
					perList = pdksEntityController.getObjectBySQLList(sb, map, null);
				for (Object[] objects : perList) {
					BigDecimal refId = (BigDecimal) objects[1], id = (BigDecimal) objects[0];
					if (refId.longValue() != id.longValue())
						iliskiMap.put(refId.longValue(), id.longValue());
				}
			} catch (Exception e) {
				logger.error(sb.toString() + " " + e);
			}

			List list1 = getSPPersonelHareketList(idList, kapi, basTarihStr, bitTarihStr, class2, session);
			if (!list1.isEmpty())
				list.addAll(list1);
			list1 = null;
			if (idList != null)
				idList.clear();
		}

		if (!iliskiMap.isEmpty()) {
			List list2 = getSPPersonelHareketList(new ArrayList<Long>(iliskiMap.keySet()), kapi, basTarihStr, bitTarihStr, class2, session);
			if (!list2.isEmpty())
				list.addAll(list2);
			list2 = null;
		}
		if (!list.isEmpty()) {
			if (class1.getName().equals(HareketKGS.class.getName()))
				getHareketKGSByBasitHareketList(list, iliskiMap, session);
			else if (!iliskiMap.isEmpty()) {
				List<String> hList = new ArrayList<String>();
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					BasitHareket basitHareket = (BasitHareket) iterator.next();
					String id = (basitHareket.getKgsSirketId() == null ? 0L : basitHareket.getKgsSirketId()) + basitHareket.getId();
					if (hList.contains(id)) {
						iterator.remove();
						continue;
					}
					hList.add(id);
					if (iliskiMap.containsKey(basitHareket.getPersonelId()))
						basitHareket.setPersonelId(iliskiMap.get(basitHareket.getPersonelId()));
				}
			}
		}
		sb = null;
		return list;
	}

	/**
	 * @param personelList
	 * @param kapi
	 * @param basTarihStr
	 * @param bitTarihStr
	 * @param class2
	 * @param session
	 * @return
	 * @throws Exception
	 */
	private List getSPPersonelHareketList(List<Long> personelList, String kapi, String basTarihStr, String bitTarihStr, Class class2, Session session) throws Exception {
		LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
		String fieldName = "personel";
		fields.put("kapi", kapi);
		fields.put(fieldName, "");
		fields.put("basTarih", basTarihStr);
		fields.put("bitTarih", bitTarihStr);
		fields.put("df", null);
		List list2 = getSPParamLongList(personelList, "SP_GET_HAREKET_SIRKET", fieldName, fields, class2, session);
		fields = null;
		return list2;
	}

	/**
	 * @param list
	 * @param iliskiMap
	 * @param session
	 */
	private void getHareketKGSByBasitHareketList(List list, TreeMap<Long, Long> iliskiMap, Session session) {
		HashMap<String, List<Long>> map1 = new HashMap<String, List<Long>>();
		TreeMap<Long, PersonelView> perMap = new TreeMap<Long, PersonelView>();
		TreeMap<Long, PersonelHareketIslem> islemMap = null;
		TreeMap<Long, KapiView> kapiMap = new TreeMap<Long, KapiView>();
		TreeMap<Long, KapiSirket> kapiSirketMap = new TreeMap<Long, KapiSirket>();
		List<HareketKGS> hareketKGSList = new ArrayList<HareketKGS>();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			BasitHareket basitHareket = (BasitHareket) iterator.next();
			if (iliskiMap != null && iliskiMap.containsKey(basitHareket.getPersonelId()))
				basitHareket.setPersonelId(iliskiMap.get(basitHareket.getPersonelId()));
			HareketKGS hareketKGS = basitHareket.getKgsHareket();
			hareketKGSList.add(hareketKGS);
			if (basitHareket.getKgsSirketId() != null) {
				String key = "KS";
				List<Long> idList = map1.containsKey(key) ? map1.get(key) : new ArrayList<Long>();
				if (idList.isEmpty())
					map1.put(key, idList);
				if (!idList.contains(basitHareket.getKgsSirketId()))
					idList.add(basitHareket.getKgsSirketId());
			}
			if (basitHareket.getKapiId() != null) {
				String key = "D";
				List<Long> idList = map1.containsKey(key) ? map1.get(key) : new ArrayList<Long>();
				if (idList.isEmpty())
					map1.put(key, idList);
				if (!idList.contains(basitHareket.getKapiId()))
					idList.add(basitHareket.getKapiId());
			}
			if (basitHareket.getPersonelId() != null) {
				String key = "P";
				List<Long> idList = map1.containsKey(key) ? map1.get(key) : new ArrayList<Long>();
				if (idList.isEmpty())
					map1.put(key, idList);
				if (!idList.contains(basitHareket.getPersonelId()))
					idList.add(basitHareket.getPersonelId());
			}
			if (basitHareket.getIslemId() != null) {
				String key = "I";
				List<Long> idList = map1.containsKey(key) ? map1.get(key) : new ArrayList<Long>();
				if (idList.isEmpty())
					map1.put(key, idList);
				if (!idList.contains(basitHareket.getIslemId()))
					idList.add(basitHareket.getIslemId());
			}
		}
		if (map1.containsKey("P")) {
			HashMap map = new HashMap();
			map.put("id", map1.get("P"));
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelKGS> personelKGSList = pdksEntityController.getObjectByInnerObjectList(map, PersonelKGS.class);
			for (PersonelKGS personelKGS : personelKGSList)
				perMap.put(personelKGS.getId(), personelKGS.getPersonelView());
			personelKGSList = null;
		}
		if (map1.containsKey("KS")) {
			HashMap map = new HashMap();
			map.put("id", map1.get("KS"));
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<KapiSirket> kapiSirketList = pdksEntityController.getObjectByInnerObjectList(map, KapiSirket.class);
			for (KapiSirket kapiSirket : kapiSirketList) {
				kapiSirketMap.put(kapiSirket.getId(), kapiSirket);
			}
			kapiSirketList = null;
		}
		if (map1.containsKey("D")) {
			HashMap map = new HashMap();
			map.put("id", map1.get("D"));
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<KapiKGS> kapiKGSList = pdksEntityController.getObjectByInnerObjectList(map, KapiKGS.class);
			for (KapiKGS kapiKGS : kapiKGSList)
				kapiMap.put(kapiKGS.getId(), kapiKGS.getKapiView());
			kapiKGSList = null;
		}
		if (map1.containsKey("I")) {
			HashMap map = new HashMap();
			map.put(PdksEntityController.MAP_KEY_MAP, "getId");
			map.put("id", map1.get("I"));
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			islemMap = pdksEntityController.getObjectByInnerObjectMap(map, PersonelHareketIslem.class, Boolean.TRUE);
		} else
			islemMap = new TreeMap<Long, PersonelHareketIslem>();
		list.clear();
		for (Iterator iterator = hareketKGSList.iterator(); iterator.hasNext();) {
			HareketKGS hareketKGS = (HareketKGS) iterator.next();
			if (hareketKGS.getKgsSirketId() != null && kapiSirketMap.containsKey(hareketKGS.getKgsSirketId())) {
				hareketKGS.setKapiSirket(kapiSirketMap.get(hareketKGS.getKgsSirketId()));
			}
			if (hareketKGS.getPersonelId() != null && perMap.containsKey(hareketKGS.getPersonelId())) {
				hareketKGS.setPersonel(perMap.get(hareketKGS.getPersonelId()));
				hareketKGS.setPersonelKGS(hareketKGS.getPersonel().getPersonelKGS());
			}
			if (hareketKGS.getKapiId() != null && kapiMap.containsKey(hareketKGS.getKapiId())) {
				hareketKGS.setKapiView(kapiMap.get(hareketKGS.getKapiId()));
				hareketKGS.setKapiKGS(hareketKGS.getKapiView().getKapiKGS());
			}
			if (hareketKGS.getIslemId() != null && islemMap.containsKey(hareketKGS.getIslemId()))
				hareketKGS.setIslem(islemMap.get(hareketKGS.getIslemId()));
			list.add(hareketKGS);
		}
		map1 = null;
		islemMap = null;
		kapiMap = null;
		perMap = null;
		hareketKGSList = null;

	}

	/**
	 * @param kullanici
	 * @param session
	 */
	public void kullaniciKaydet(User kullanici, Session session) {
		User loginUser = authenticatedUser != null ? authenticatedUser : new User();
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, loginUser);
		if (kullanici != null)
			pdksEntityController.saveOrUpdate(session, entityManager, kullanici);

	}

	/**
	 * @param personel
	 * @param session
	 */
	public void personelKaydet(Personel personel, Session session) {
		User loginUser = authenticatedUser != null ? authenticatedUser : new User();
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, loginUser);
		if (personel != null) {
			MailGrubu mailGrubuCC = personel.getMailGrubuCC(), mailGrubuBCC = personel.getMailGrubuBCC(), hareketMailGrubu = personel.getHareketMailGrubu();
			List<MailGrubu> deleteList = new ArrayList<MailGrubu>();
			if (mailGrubuCC != null) {
				if (mailGrubuCC.isGuncellendi())
					pdksEntityController.saveOrUpdate(session, entityManager, mailGrubuCC);
				else {
					deleteList.add(mailGrubuCC);
					personel.setMailGrubuCC(null);
				}
			}
			if (mailGrubuBCC != null) {
				if (mailGrubuBCC.isGuncellendi())
					pdksEntityController.saveOrUpdate(session, entityManager, mailGrubuBCC);
				else {
					deleteList.add(mailGrubuBCC);
					personel.setMailGrubuBCC(null);
				}

			}
			if (hareketMailGrubu != null) {
				if (hareketMailGrubu.isGuncellendi())
					pdksEntityController.saveOrUpdate(session, entityManager, hareketMailGrubu);
				else {
					deleteList.add(hareketMailGrubu);
					personel.setHareketMailGrubu(null);
				}

			}
			pdksEntityController.saveOrUpdate(session, entityManager, personel);
			for (Object del : deleteList) {
				pdksEntityController.deleteObject(session, entityManager, del);

			}

			deleteList = null;
		}
	}

	/**
	 * @param fnName
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public List<Personel> getYoneticiList(String fnName, Session session) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT * FROM " + fnName + "() P ");
		sb.append(" ORDER BY P." + Personel.COLUMN_NAME_AD + ",P." + Personel.COLUMN_NAME_SOYAD + ",P." + Personel.COLUMN_NAME_PDKS_SICIL_NO);
		HashMap map = new HashMap();
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Personel> list = pdksEntityController.getObjectBySQLList(sb, map, Personel.class);
		return list;
	}

	/**
	 * @param session
	 * @return
	 */
	public List<Personel> getTaseronYoneticiler(Session session) {
		List<Personel> list = null;

		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		StringBuffer sb = new StringBuffer("SP_YONETICI_KONTRATLI_VIEW");
		try {
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			list = pdksEntityController.execSPList(map, sb, Personel.class);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		return list;
	}

	/**
	 * @param session
	 * @throws Exception
	 */
	@Transactional
	public void setIkinciYoneticiSifirla(Session session) {
		Boolean flush = Boolean.FALSE, yonetici2ERPKontrol = getParameterKey("yonetici2ERPKontrol").equals("1");
		if (!yonetici2ERPKontrol) {
			StringBuffer sp = new StringBuffer("SP_GET_IKINCI_YONETICI_UPDATE");
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				pdksEntityController.execSP(map, sp);
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

		}
		if (flush)
			session.flush();
	}

	/**
	 * @param aylikPuantajList
	 * @param adSoyadSirali
	 * @return
	 */
	public List<AylikPuantaj> sortAylikPuantajList(List<AylikPuantaj> aylikPuantajList, boolean adSoyadSirali) {
		if (aylikPuantajList != null && aylikPuantajList.size() > 1) {
			HashMap<String, Liste> listeMap = new HashMap<String, Liste>();
			if (adSoyadSirali == false)
				aylikPuantajList = PdksUtil.sortObjectStringAlanList(aylikPuantajList, "getAdSoyad", null);
			for (AylikPuantaj aylikPuantaj : aylikPuantajList) {
				Personel personel = aylikPuantaj.getPdksPersonel();
				Sirket sirket = personel.getSirket();
				CalismaModeli cm = aylikPuantaj.getCalismaModeli();
				String key = (sirket.getTesisDurum() && personel.getTesis() != null ? personel.getTesis().getAciklama() + "_" : "");
				String bolumAdi = personel.getEkSaha3() != null ? "_" + personel.getEkSaha3().getAciklama() : "";
				if (cm != null) {
					if (cm.isSaatlikOdeme() || cm.isFazlaMesaiVarMi() == false)
						key += cm.getAciklama() + bolumAdi;
					else
						key += bolumAdi + cm.getAciklama();
				} else
					key += bolumAdi;

				Liste liste = listeMap.containsKey(key) ? listeMap.get(key) : new Liste(key, new ArrayList<AylikPuantaj>());
				List<AylikPuantaj> list = (List<AylikPuantaj>) liste.getValue();
				if (list.isEmpty())
					listeMap.put(key, liste);
				list.add(aylikPuantaj);
			}
			List<Liste> listeler = PdksUtil.sortObjectStringAlanList(new ArrayList(listeMap.values()), "getId", null);
			aylikPuantajList.clear();
			for (Liste liste : listeler) {
				List<AylikPuantaj> list = (List<AylikPuantaj>) liste.getValue();
				for (AylikPuantaj aylikPuantaj : list)
					aylikPuantajList.add(aylikPuantaj);
				list = null;
			}

			listeler = null;
			listeMap = null;
		}
		if (aylikPuantajList != null) {
			boolean renk = Boolean.TRUE;
			for (AylikPuantaj puantaj : aylikPuantajList) {
				puantaj.setTrClass(renk ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
				renk = !renk;
			}
		}

		return aylikPuantajList;
	}

	/**
	 * @param perIdList
	 * @param session
	 * @return
	 */
	public ArrayList<Personel> getKontratliSiraliPersonel(List<Long> perIdList, Session session) {
		String fieldName = "p";
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT P.* from " + Personel.TABLE_NAME + " P WITH(nolock) ");
		if (perIdList != null && !perIdList.isEmpty()) {
			sb.append(" WHERE P." + Personel.COLUMN_NAME_ID + " :" + fieldName);
			sb.append(" ORDER BY P." + Personel.COLUMN_NAME_AD + ",P." + Personel.COLUMN_NAME_SOYAD + ",P." + Personel.COLUMN_NAME_ID);

		} else
			sb.append(" WHERE 1=2");
		fields.put(fieldName, perIdList);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		// ArrayList<Personel> personelList = (ArrayList<Personel>) pdksEntityController.getObjectBySQLList(sb, fields, Personel.class);
		ArrayList<Personel> personelList = (ArrayList<Personel>) getSQLParamList(perIdList, sb, fieldName, fields, Personel.class, session);

		fields = null;
		sb = null;
		return personelList;
	}

	/**
	 * @param fileUpload
	 * @return
	 */
	public Dosya getDosyaFromFileUpload(FileUpload fileUpload) {
		Dosya dosya = new Dosya();
		dosya.setDosyaIcerik(fileUpload.getData());
		dosya.setDosyaAdi(fileUpload.getName());
		dosya.setSize(fileUpload.getLength());
		return dosya;
	}

	/**
	 * @return
	 */
	public int getYemekMukerrerAraligi() {
		int yemekMukerrerAraligi = 0;
		try {
			yemekMukerrerAraligi = parameterMap.containsKey("yemekMukerrerAraligi") ? Integer.parseInt(parameterMap.get("yemekMukerrerAraligi")) : 5;
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			yemekMukerrerAraligi = 5;
		}
		return yemekMukerrerAraligi;
	}

	/**
	 * @param departmanId
	 * @param session
	 * @return
	 */
	public List<SelectItem> getBolumDepartmanSelectItems(Long departmanId, Session session) {
		List<SelectItem> bolumDepartmanlari = null;
		User loginUser = authenticatedUser != null ? authenticatedUser : new User();
		if (loginUser != null && loginUser.isIK()) {
			if (departmanId == null && !loginUser.isIKAdmin())
				departmanId = loginUser.getDepartman().getId();

		}
		if (departmanId != null) {
			List<Tanim> bolumler = getTanimList(Tanim.TIPI_BOLUM_DEPARTMAN + departmanId, session);
			if (bolumler != null && !bolumler.isEmpty()) {
				if (bolumler.size() > 1)
					bolumler = PdksUtil.sortObjectStringAlanList(bolumler, "getAciklama", null);
				bolumDepartmanlari = getTanimSelectItem(bolumler);
			} else
				bolumDepartmanlari = new ArrayList<SelectItem>();
			bolumler = null;
		}

		return bolumDepartmanlari;
	}

	/**
	 * @param pdksDepartman
	 * @param session
	 * @return
	 */
	public List<Tanim> getBolumDepartmanlari(Departman pdksDepartman, Session session) {
		List<Tanim> bolumDepartmanlari = null;
		if (authenticatedUser != null && authenticatedUser.isIK()) {
			if (pdksDepartman == null && !authenticatedUser.isIKAdmin())
				pdksDepartman = authenticatedUser.getDepartman();

		}
		if (pdksDepartman != null)
			bolumDepartmanlari = getTanimList(Tanim.TIPI_BOLUM_DEPARTMAN + pdksDepartman.getId(), session);

		return bolumDepartmanlari;
	}

	/**
	 * @param pdksDepartman
	 * @param session
	 * @return
	 */
	public List<Tanim> getGorevDepartmanlari(Departman pdksDepartman, Session session) {
		List<Tanim> gorevDepartmanlari = null;
		if (authenticatedUser != null && authenticatedUser.isIK()) {
			if (pdksDepartman == null && !authenticatedUser.isIKAdmin())
				pdksDepartman = authenticatedUser.getDepartman();

		}
		if (pdksDepartman != null)
			gorevDepartmanlari = getTanimList(Tanim.TIPI_GOREV_DEPARTMAN + pdksDepartman.getId(), session);

		return gorevDepartmanlari;
	}

	/**
	 * @param session
	 * @param name
	 * @return
	 */
	public Parameter getParameter(Session session, String name) {
		HashMap map = new HashMap();
		map.put("adi", name);
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT   T.* FROM " + Parameter.TABLE_NAME + " T WITH(nolock) ");
		sb.append(" WHERE T." + Parameter.COLUMN_NAME_ADI + " = :adi  ");
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Parameter> list = pdksEntityController.getObjectBySQLList(sb, map, Parameter.class);
		Parameter parameter = null;
		if (!list.isEmpty())
			parameter = list.get(0);
		if (parameter != null && (parameter.getActive().equals(Boolean.FALSE) || (parameter.isHelpDeskMi() && PdksUtil.isSistemDestekVar() == false)))
			parameter = null;
		map = null;
		return parameter;
	}

	/**
	 * @param session
	 * @return
	 */
	public List<Role> yetkiRolleriGetir(Session session) {
		HashMap parametreMap = new HashMap();
		parametreMap.put("status=", Boolean.TRUE);
		if (!authenticatedUser.isAdmin())
			parametreMap.put("adminRole = ", Boolean.FALSE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Role> allRoles = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, Role.class);
		if (!authenticatedUser.isAdmin()) {
			Long departmanId = authenticatedUser.getDepartman().getId();
			for (Iterator iterator = allRoles.iterator(); iterator.hasNext();) {
				Role roleSis = (Role) iterator.next();
				if (roleSis.getDepartman() != null && !roleSis.getDepartman().getId().equals(departmanId)) {
					iterator.remove();
					continue;
				}
			}
		}
		if (allRoles.size() > 1)
			allRoles = PdksUtil.sortObjectStringAlanList(allRoles, "getRolename", null);
		return allRoles;
	}

	/**
	 * @param list
	 * @param vardiyaAksamSabahAdlari
	 * @return
	 */
	public boolean vardiyaAksamSabahVarMi(List<VardiyaGun> list, List<String> vardiyaAksamSabahAdlari) {

		boolean durum = Boolean.FALSE;
		if (list != null && vardiyaAksamSabahAdlari != null) {
			for (VardiyaGun vardiyaGun : list) {
				if (vardiyaGun.getVardiya() == null || !vardiyaGun.isAyinGunu())
					continue;
				Vardiya vardiya = vardiyaGun.getVardiya();
				if (vardiya.isCalisma() && vardiya.getKisaAdi() != null && vardiyaAksamSabahAdlari.contains(vardiya.getKisaAdi()) && vardiyaGun.getCalismaSuresi() > 0.0d) {
					durum = Boolean.TRUE;
					break;
				}

			}
		}
		return durum;
	}

	/**
	 * @param user
	 * @return
	 */
	public String getPersonelHareketMail(Personel personel) {
		StringBuilder mail = null;
		String str = null;
		if (personel.getHareketMail() != null && personel.getHareketMail().indexOf("@") > 0) {
			mail = new StringBuilder();
			List<String> mailler = personel.getEMailHareketList();
			for (Iterator iterator = mailler.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				if (mail.indexOf(string) < 0)
					mail.append(string + (iterator.hasNext() ? "," : ""));
			}
			if (!mail.equals(personel.getHareketMail())) {
				str = mail.toString();
				mail = null;
			}
		}

		return str;
	}

	/**
	 * @param personel
	 * @return
	 */
	public String getPersonelCCMail(Personel personel) {
		StringBuilder mail = null;
		String str = null;
		if (personel.getEmailCC() != null && personel.getEmailCC().indexOf("@") > 0) {
			mail = new StringBuilder();
			List<String> mailler = personel.getEMailCCList();
			for (Iterator iterator = mailler.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				if (mail.indexOf(string) < 0)
					mail.append(string + (iterator.hasNext() ? "," : ""));
			}
			if (!mail.equals(personel.getEmailCC())) {
				str = mail.toString();
				mail = null;
			}
		}

		return str;
	}

	/**
	 * @param user
	 * @param departmanId
	 * @param sirket
	 * @param tesisId
	 * @param bolumId
	 * @param aylikPuantaj
	 * @param tipi
	 * @param denklestirme
	 * @param fazlaMesaiTalepDurum
	 * @param session
	 * @return
	 */
	public List getFazlaMesaiMudurList(User user, Long departmanId, Sirket sirket, String tesisId, Long bolumId, AylikPuantaj aylikPuantaj, String tipi, boolean denklestirme, boolean fazlaMesaiTalepDurum, Session session) {
		List list = null;
		if (aylikPuantaj != null) {
			DenklestirmeAy denklestirmeAy = aylikPuantaj.getDenklestirmeAy();
			Date bitTarih = null, basTarih = null;
			if (denklestirmeAy != null) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.DATE, 1);
				cal.set(Calendar.MONTH, denklestirmeAy.getAy() - 1);
				cal.set(Calendar.YEAR, denklestirmeAy.getYil());
				basTarih = PdksUtil.getDate(cal.getTime());
				cal.add(Calendar.MONTH, 1);
				cal.add(Calendar.DATE, -1);
				bitTarih = PdksUtil.getDate(cal.getTime());
			} else {
				basTarih = aylikPuantaj.getIlkGun();
				bitTarih = aylikPuantaj.getSonGun();
			}
			if (basTarih != null && bitTarih != null && session != null) {
				if (user == null)
					user = authenticatedUser;
				boolean ikRol = user != null && (user.isAdmin() || user.isIK() || user.isSistemYoneticisi());
				Class class1 = null;
				boolean tesisYetki = false;
				Departman departman = null;
				String order = null;
				HashMap fields = new HashMap();
				if (tipi.equalsIgnoreCase("S")) {
					class1 = Sirket.class;
					tesisYetki = getParameterKey("tesisYetki").equals("1");
					if (departmanId != null && tesisYetki) {
						fields.put("id", departmanId);
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						departman = (Departman) pdksEntityController.getObjectByInnerObject(fields, Departman.class);
					}
					order = Sirket.COLUMN_NAME_AD;
				} else if (tipi.equalsIgnoreCase("B")) {
					class1 = Tanim.class;
					order = Tanim.COLUMN_NAME_ACIKLAMATR;
				} else if (tipi.equalsIgnoreCase("T")) {
					if (sirket == null || sirket.isTesisDurumu()) {
						class1 = Tanim.class;
						order = Tanim.COLUMN_NAME_ACIKLAMATR;
					}
				} else if (tipi.equalsIgnoreCase("P")) {
					class1 = Personel.class;
				} else if (tipi.equalsIgnoreCase("D")) {
					if (ikRol) {
						tesisYetki = getParameterKey("tesisYetki").equals("1");
						class1 = Departman.class;
					}
				}
				if (class1 != null) {
					if (sirket != null)
						departmanId = null;
					if (tesisYetki) {
						if ((departman == null || departman.isAdminMi()) && user.getYetkiliTesisler() != null && !user.getYetkiliTesisler().isEmpty()) {
							tesisId = "";
							for (Iterator iterator = user.getYetkiliTesisler().iterator(); iterator.hasNext();) {
								Tanim tesis = (Tanim) iterator.next();
								tesisId += String.valueOf(tesis.getId());
								if (iterator.hasNext())
									tesisId += ",";
							}
						}
					}
					StringBuffer sp = new StringBuffer("SP_GET_FAZLA_MESAI_MUDUR_DATA");
					LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
					Long denklestirmeDeger = 0L;
					if (denklestirme) {
						if (denklestirmeAy != null && denklestirmeAy.getId() != null)
							denklestirmeDeger = denklestirmeAy.getId();
						else
							denklestirmeDeger = -1L;
					}
					map.put("denklestirme", denklestirmeDeger);
					map.put("mudurId", ikRol ? 0L : user.getPersonelId());
					Long depId = ikRol && departmanId != null ? departmanId : 0L;
					if (ikRol == false && user.isTaseronAdmin()) {
						depId = -1L;
					}
					Long sirketId = 0L;
					if (sirket != null) {
						sirketId = sirket.getId();
						if (!tipi.equalsIgnoreCase("T")) {
							if (sirket.isTesisDurumu() == false) {
								tesisId = "";
								if (tipi.equalsIgnoreCase("P")) {
									depId = sirket.getDepartman().getId();
									sirketId = 0L;
								}
							}
						}
					}
					map.put("departmanId", depId);
					map.put("sirketId", sirketId);
					map.put("tesisId", tesisId != null ? tesisId : "");
					map.put("bolumId", bolumId != null ? bolumId : 0L);
					map.put("tipi", tipi);
					map.put("basTarih", PdksUtil.convertToDateString(basTarih, "yyyyMMdd"));
					map.put("bitTarih", PdksUtil.convertToDateString(bitTarih, "yyyyMMdd"));
					map.put("format", "112");
					map.put("mesaiTalep", fazlaMesaiTalepDurum ? 1 : 0);
					map.put("order", order != null ? order : "");
					Gson gson = new Gson();
					if (ikRol)
						logger.debug(tipi + "\n" + gson.toJson(map));
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
					try {
						list = pdksEntityController.execSPList(map, sp, class1);
					} catch (Exception e) {
						logger.error(e);
						e.printStackTrace();
					}
				}
			}
		}
		if (list == null)
			list = new ArrayList();
		aylikPuantaj = null;
		return list;
	}

	/**
	 * @param sayfaAdi
	 * @param ex
	 */
	public void loggerErrorYaz(String sayfaAdi, Exception ex) throws Exception {
		StringBuffer sb = new StringBuffer();
		if (authenticatedUser != null && PdksUtil.hasStringValue(sayfaAdi) && PdksUtil.hasStringValue(authenticatedUser.getParametreJSON())) {
			if (authenticatedUser.getParametreJSON().indexOf(sayfaAdi) > 0)
				sb.append(authenticatedUser.getParametreJSON() + "\n");
		}

		sb.append(ex);
		logger.error(sb.toString());
		sb = null;
		throw new Exception(ex);
	}

	/**
	 * @param user
	 * @param session
	 * @return
	 */
	private List<Personel> findIkinciYoneticiPersonel(User user, Session session) {
		Date tarih = PdksUtil.getDate(Calendar.getInstance().getTime());
		List<Personel> ikinciYoneticiPersoneller = null;
		try {
			User userYetki = (User) user.clone();
			if (userYetki.getYetkiliRollerim() != null)
				userYetki.getYetkiliRollerim().clear();
			if (userYetki.getYetkiliTesisler() != null)
				userYetki.getYetkiliTesisler().clear();
			PdksUtil.setUserYetki(userYetki);
			ikinciYoneticiPersoneller = getFazlaMesaiMudurList(userYetki, null, null, "", null, new AylikPuantaj(tarih, tarih), "P", false, false, session);
		} catch (Exception e) {
			Personel yoneticiPersonel = user.getPdksPersonel();
			HashMap map = new HashMap();
			map.put("sskCikisTarihi>=", tarih);
			map.put("iseBaslamaTarihi<=", tarih);
			map.put("durum=", Boolean.TRUE);
			List paramList = new ArrayList();
			paramList.add(yoneticiPersonel.getId());
			paramList.add(yoneticiPersonel.getId());
			String sqlADD = "( " + PdksEntityController.SELECT_KARAKTER + ".yoneticisi.yoneticisi.id=? and " + PdksEntityController.SELECT_KARAKTER + ".asilYonetici2=null ) or " + PdksEntityController.SELECT_KARAKTER + ".asilYonetici2.id=?";
			map.put(PdksEntityController.MAP_KEY_SQLADD, sqlADD);
			map.put(PdksEntityController.MAP_KEY_SQLPARAMS, paramList);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			ikinciYoneticiPersoneller = pdksEntityController.getObjectByInnerObjectListInLogic(map, Personel.class);

		} finally {
			if (ikinciYoneticiPersoneller == null)
				ikinciYoneticiPersoneller = new ArrayList<Personel>();
		}

		return ikinciYoneticiPersoneller;
	}

	/**
	 * @param tableName
	 * @param session
	 * @return
	 */
	public boolean getGuncellemeDurum(String tableName, Session session) {
		boolean durum = false;
		if (session != null) {
			StringBuffer sb = new StringBuffer();
			HashMap map = new HashMap();
			try {
				sb.append("select dbo.FN_PDKS_TABLE_UPDATE_DURUM(:t) as DURUM");
				map.put("t", tableName);
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				List list = pdksEntityController.getObjectBySQLList(sb, map, null);
				if (list != null && !list.isEmpty()) {
					Object sonuc = list.get(0);
					if (sonuc != null) {
						Byte byte1 = (Byte) sonuc;
						String str = String.valueOf(byte1);
						durum = str.equals("1");
					}

				}
				list = null;
			} catch (Exception e) {

			}
			map = null;
			sb = null;
		}
		return durum;

	}

	/**
	 * @param user
	 * @param departmanId
	 * @param sirket
	 * @param tesisId
	 * @param bolumId
	 * @param altBolumId
	 * @param aylikPuantaj
	 * @param tipi
	 * @param denklestirme
	 * @param session
	 * @return
	 */
	public List getFazlaMesaiList(User loginUser, Long departmanId, Sirket sirket, String tesisId, Long bolumId, Long altBolumId, AylikPuantaj aylikPuantaj, String tipi, boolean denklestirme, Session session) {
		List list = null;
		if (aylikPuantaj != null) {
			if (loginUser == null)
				loginUser = aylikPuantaj.getLoginUser() != null ? aylikPuantaj.getLoginUser() : authenticatedUser;
			DenklestirmeAy denklestirmeAy = aylikPuantaj.getDenklestirmeAy();
			Date bitTarih = null, basTarih = null;
			if (denklestirmeAy != null) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.DATE, 1);
				cal.set(Calendar.MONTH, denklestirmeAy.getAy() - 1);
				cal.set(Calendar.YEAR, denklestirmeAy.getYil());
				basTarih = PdksUtil.getDate(cal.getTime());
				cal.add(Calendar.MONTH, 1);
				cal.add(Calendar.DATE, -1);
				bitTarih = PdksUtil.getDate(cal.getTime());
			} else {
				basTarih = aylikPuantaj.getIlkGun();
				bitTarih = aylikPuantaj.getSonGun();
			}
			if (basTarih != null && bitTarih != null && session != null) {
				boolean ikRol = getIKRolSayfa(loginUser);
				Class class1 = null;
				boolean tesisYetki = getParameterKey("tesisYetki").equals("1");
				Departman departman = null;
				String order = null;
				HashMap fields = new HashMap();
				if (tesisYetki && loginUser.getId() != null && (loginUser.isIK() || loginUser.isDirektorSuperVisor()) && (loginUser.getYetkiliTesisler() == null || loginUser.getYetkiliTesisler().isEmpty())) {
					setUserTesisler(loginUser, session);
				}
				if (tipi.equalsIgnoreCase("S")) {
					class1 = Sirket.class;

					if (departmanId != null && tesisYetki) {
						fields.put("id", departmanId);
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						departman = (Departman) pdksEntityController.getObjectByInnerObject(fields, Departman.class);
					}
					order = Sirket.COLUMN_NAME_AD;
				} else if (tipi.equalsIgnoreCase("B")) {
					class1 = Tanim.class;
					order = Tanim.COLUMN_NAME_ACIKLAMATR;
				} else if (tipi.startsWith("AB")) {
					class1 = Tanim.class;
					order = Tanim.COLUMN_NAME_ACIKLAMATR;
				} else if (tipi.startsWith("T")) {
					if (sirket == null || sirket.isTesisDurumu()) {
						class1 = Tanim.class;
						order = Tanim.COLUMN_NAME_ACIKLAMATR;
					}
				} else if (tipi.equalsIgnoreCase("P")) {
					class1 = Personel.class;
				} else if (tipi.equalsIgnoreCase("D")) {
					if (ikRol) {
						tesisYetki = getParameterKey("tesisYetki").equals("1");
						class1 = Departman.class;
					}
				}
				if (class1 != null) {
					Personel personel = loginUser != null ? loginUser.getPdksPersonel() : new Personel();
					if (loginUser.isTesisYonetici()) {
						sirket = personel.getSirket();
						tesisId = "" + (personel.getTesis() != null && personel.getTesis().getId() != null ? personel.getTesis().getId() : 0L);
						ikRol = true;
					}
					boolean departmanYonetici = ikRol == false && loginUser.isDepartmentAdmin() && getParameterKey("tesisYetki").equals("1");
					Long direktorId = null;
					if ((loginUser.isDirektorSuperVisor() || departmanYonetici) && personel.getEkSaha1() != null)
						direktorId = personel.getEkSaha1().getId();
					if (sirket != null)
						departmanId = null;
					if (tesisYetki) {
						if ((loginUser.isDirektorSuperVisor() || departman == null || departman.isAdminMi()) && loginUser.getYetkiliTesisler() != null && !loginUser.getYetkiliTesisler().isEmpty()) {
							tesisId = "";
							for (Iterator iterator = loginUser.getYetkiliTesisler().iterator(); iterator.hasNext();) {
								Tanim tesis = (Tanim) iterator.next();
								tesisId += String.valueOf(tesis.getId());
								if (iterator.hasNext())
									tesisId += ",";
							}
						}
					}
					// String spAdi = "SP_GET_FAZLA_MESAI_DATA_ALT";
					String spAdi = "SP_GET_FAZLA_MESAI_DATA";
					StringBuffer sp = new StringBuffer(spAdi);
					LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
					Long denklestirmeDeger = 0L;
					if (denklestirme) {
						if (denklestirmeAy != null && denklestirmeAy.getId() != null)
							denklestirmeDeger = denklestirmeAy.getId();
						else
							denklestirmeDeger = -1L;
					}
					map.put("denklestirme", denklestirmeDeger);
					map.put("yoneticiId", ikRol ? 0L : loginUser.getPersonelId());
					Long depId = ikRol && departmanId != null ? departmanId : 0L;
					if (ikRol == false && loginUser.isTaseronAdmin()) {
						depId = -1L;
					}
					Long sirketId = 0L;
					if (sirket != null) {
						sirketId = sirket.getId();
						if (!tipi.equalsIgnoreCase("T")) {
							if (sirket.getSirketGrupId() != null || sirket.isTesisDurumu() == false) {
								tesisId = "";
								if (tipi.equalsIgnoreCase("P")) {
									depId = sirket.getDepartman().getId();
									if (sirket.getSirketGrupId() != null)
										sirketId = 0L;
								}
							}
						}
					}
					map.put("departmanId", depId);
					map.put("sirketId", sirketId);
					map.put("tesisId", tesisId != null ? tesisId : "");
					map.put("direktorId", direktorId != null ? direktorId : 0L);
					map.put("bolumId", bolumId != null ? bolumId : 0L);
					// if (PdksUtil.isPuantajSorguAltBolumGir() || bolumId != null)
					map.put("altBolumId", altBolumId);
					map.put("tipi", tipi);
					map.put("basTarih", PdksUtil.convertToDateString(basTarih, "yyyyMMdd"));
					map.put("bitTarih", PdksUtil.convertToDateString(bitTarih, "yyyyMMdd"));
					map.put("format", "112");
					map.put("order", order != null ? order : "");
					Gson gson = new Gson();
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
					try {
						list = pdksEntityController.execSPList(map, sp, class1);
						if (tipi.endsWith("+") && loginUser.isAdmin())
							logger.debug(spAdi + " " + tipi + " " + list.size() + "\n" + gson.toJson(map));
					} catch (Exception e) {
						logger.error(e + "\n" + spAdi + "\n" + gson.toJson(map));
						e.printStackTrace();

					}
				}
			}
		}
		aylikPuantaj = null;
		if (list == null)
			list = new ArrayList();
		return list;
	}

	/**
	 * @param user
	 * @return
	 */
	public boolean getIKRolSayfa(User user) {
		boolean ikRol = false;
		if (user == null)
			user = authenticatedUser;
		if (user != null) {
			ikRol = user.isAdmin() || user.isIK() || user.isSistemYoneticisi() || user.isGenelMudur() || user.isIKAdmin();
			if (!ikRol && user.isRaporKullanici() && user.getCalistigiSayfa() != null && PdksUtil.hasStringValue(user.getCalistigiSayfa())) {
				String ikRaporlar = getParameterKey("ikRaporlar");
				if (PdksUtil.hasStringValue(ikRaporlar)) {
					List<String> sayfalar = PdksUtil.getListStringTokenizer(ikRaporlar, null);
					ikRol = sayfalar.contains(user.getCalistigiSayfa());
				}
			}
		}
		return ikRol;
	}

	/**
	 * @param user
	 * @param session
	 * @return
	 */
	private List<Personel> araIkinciYoneticiPersonel(User user, Session session) {
		List<Personel> ikinciYoneticiPersonelleri = findIkinciYoneticiPersonel(user, session);
		Date tarih = Calendar.getInstance().getTime();
		HashMap map = new HashMap();
		map.put(PdksEntityController.MAP_KEY_SELECT, "vekaletVeren");
		map.put("bitTarih>=", tarih);
		map.put("basTarih<=", tarih);
		map.put("yeniYonetici.id=", user.getId());
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<User> vekaletYoneticileri = pdksEntityController.getObjectByInnerObjectListInLogic(map, UserVekalet.class);
		for (Iterator iterator = vekaletYoneticileri.iterator(); iterator.hasNext();) {
			User user2 = (User) iterator.next();
			List<Personel> vekilIkinciYoneticiPersonelleri = findIkinciYoneticiPersonel(user2, session);
			if (!vekilIkinciYoneticiPersonelleri.isEmpty())
				ikinciYoneticiPersonelleri.addAll(vekilIkinciYoneticiPersonelleri);
		}

		return ikinciYoneticiPersonelleri;
	}

	/**
	 * @param personel
	 * @return
	 */
	public String getPersonelBCCMail(Personel personel) {
		StringBuilder mail = null;
		String str = null;
		if (personel.getEmailBCC() != null && personel.getEmailBCC().indexOf("@") > 0) {
			mail = new StringBuilder();
			List<String> mailler = personel.getEMailBCCList();
			for (Iterator iterator = mailler.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				if (mail.toString().indexOf(string) < 0)
					mail.append(string + (iterator.hasNext() ? "," : ""));
			}
			if (!mail.toString().equals(personel.getEmailBCC())) {
				str = mail.toString();
				mail = null;
			}
		}
		return str;
	}

	/**
	 * @param personel
	 * @param yoneticiPersonel
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public User getYoneticiBul(Personel personel, Personel yoneticiPersonel, Session session) throws Exception {
		User yeniYonetici = null;
		if (yoneticiPersonel == null)
			yoneticiPersonel = personel.getPdksYonetici();
		if (yoneticiPersonel != null) {
			User yoneticiKullanici = null;
			Date tarih = Calendar.getInstance().getTime();
			HashMap map = new HashMap();
			map.put("pdksPersonel.id", yoneticiPersonel.getId());
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			yoneticiKullanici = (User) pdksEntityController.getObjectByInnerObject(map, User.class);
			if (yoneticiKullanici != null) {
				map.clear();
				map.put(PdksEntityController.MAP_KEY_SELECT, "yeniYonetici");
				map.put("bitTarih>=", tarih);
				map.put("basTarih<=", tarih);
				map.put("bagliYonetici=", yoneticiKullanici);
				map.put("personelGecici=", personel);
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				yeniYonetici = (User) pdksEntityController.getObjectByInnerObjectInLogic(map, PersonelGeciciYonetici.class);
			}
			map.clear();
			map.put(PdksEntityController.MAP_KEY_SELECT, "yeniYonetici");
			map.put("bitTarih>=", tarih);
			map.put("basTarih<=", tarih);
			if (yeniYonetici == null)
				yeniYonetici = yoneticiKullanici;
			map.put("vekaletVeren=", yeniYonetici);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			yeniYonetici = (User) pdksEntityController.getObjectByInnerObjectInLogic(map, UserVekalet.class);
			if (yeniYonetici == null)
				yeniYonetici = yoneticiKullanici;
		}
		return yeniYonetici;

	}

	/**
	 * @param user
	 * @param personel
	 * @param donem
	 * @param bakiyeIzinTipi
	 * @param sure
	 * @param kidemYil
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public PersonelIzin getBakiyeIzin(User user, Personel personel, Date donem, IzinTipi bakiyeIzinTipi, Double sure, int kidemYil, Session session) throws Exception {
		PersonelIzin bakiyeIzin = null;
		Calendar cal = Calendar.getInstance();
		Date bugun = PdksUtil.getDate(cal.getTime());
		cal.setTime(donem);
		int yil = cal.get(Calendar.YEAR);
		int sayac = 0;
		if (personel.getSirket().isPdksMi() == false)
			bakiyeIzin = null;
		cal = Calendar.getInstance();
		boolean yeni = false;
		while (bakiyeIzin == null && sayac < 3) {
			++sayac;
			cal.setTime(personel.getIzinHakEdisTarihi());
			cal.set(Calendar.YEAR, yil);
			Date hakedisTarihi = cal.getTime();
			String hakedisTarih = "convert(datetime, '" + PdksUtil.convertToDateString(hakedisTarihi, "yyyyMMdd") + "', 112)";
			cal.add(Calendar.YEAR, -1);
			Date oncekiHakEdisTarihi = cal.getTime();
			if (bugun.after(oncekiHakEdisTarihi) && !donem.after(hakedisTarihi)) {
				String bakiyeTarih = " convert(datetime,'" + PdksUtil.convertToDateString(donem, "yyyy") + "0101', 112)";
				StringBuilder queryStr = new StringBuilder("SELECT " + PersonelIzin.COLUMN_NAME_ID + " AS IZIN_ID  from " + PersonelIzin.TABLE_NAME + " WITH(nolock)  ");
				queryStr.append(" WHERE " + PersonelIzin.COLUMN_NAME_IZIN_TIPI + " = " + bakiyeIzinTipi.getId() + " AND " + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + " = " + bakiyeTarih + " AND PERSONEL_ID=" + personel.getId());
				SQLQuery query1 = session.createSQLQuery(queryStr.toString());
				queryStr = null;
				List<Object> elements = query1.list();
				if (!elements.isEmpty()) {
					BigDecimal izinId = (BigDecimal) elements.get(0);
					if (izinId != null) {
						HashMap map = new HashMap();
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
						map.put("id", izinId.longValue());
						bakiyeIzin = (PersonelIzin) pdksEntityController.getObjectByInnerObject(map, PersonelIzin.class);
					}

				} else if (kidemYil >= 0) {
					if (sure == null)
						sure = bakiyeIzinTipi.getKotaBakiye() != null ? bakiyeIzinTipi.getKotaBakiye() : 0D;
					if (user == null)
						user = getSistemAdminUser(session);

					String aciklama = bakiyeIzinTipi != null ? bakiyeIzinTipi.getIzinTipiTanim().getAciklama() : "Bakiye İzin";
					if (kidemYil >= 0)
						aciklama = kidemYil > 0 ? String.valueOf(kidemYil) : "";
					queryStr = new StringBuilder("INSERT INTO " + PersonelIzin.TABLE_NAME + " (" + PersonelIzin.COLUMN_NAME_DURUM + ", " + PersonelIzin.COLUMN_NAME_OLUSTURMA_TARIHI + ", " + PersonelIzin.COLUMN_NAME_ACIKLAMA + ", " + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + ", "
							+ PersonelIzin.COLUMN_NAME_BITIS_ZAMANI + ",");
					queryStr.append(PersonelIzin.COLUMN_NAME_IZIN_SURESI + ", " + PersonelIzin.COLUMN_NAME_IZIN_DURUMU + "," + PersonelIzin.COLUMN_NAME_VERSION + "," + PersonelIzin.COLUMN_NAME_OLUSTURAN + ", " + PersonelIzin.COLUMN_NAME_PERSONEL + ", " + PersonelIzin.COLUMN_NAME_IZIN_TIPI + ")");
					queryStr.append(" select  1 as DURUM,GETDATE() olusturmaTarihi, '" + aciklama + "' as ACIKLAMA," + bakiyeTarih + " AS  BASLANGIC_ZAMANI,");
					queryStr.append(" " + hakedisTarih + " AS BITIS_ZAMANI, " + sure + " AS IZIN_SURESI," + PersonelIzin.IZIN_DURUMU_ONAYLANDI + " AS IZIN_DURUMU, 0 AS version," + user.getId() + " olusturanUser_id ,");
					queryStr.append(" P." + Personel.COLUMN_NAME_ID + " PERSONEL_ID,T." + IzinTipi.COLUMN_NAME_ID + " AS IZIN_TIPI_ID FROM " + IzinTipi.TABLE_NAME + " T WITH(nolock)  ");
					queryStr.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + " = " + personel.getId());
					queryStr.append(" LEFT JOIN " + PersonelIzin.TABLE_NAME + " I ON I." + PersonelIzin.COLUMN_NAME_PERSONEL + " = P." + Personel.COLUMN_NAME_ID);
					queryStr.append(" AND I." + PersonelIzin.COLUMN_NAME_IZIN_TIPI + " = T." + IzinTipi.COLUMN_NAME_ID + " AND I." + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + " = " + bakiyeTarih);
					queryStr.append(" WHERE T." + IzinTipi.COLUMN_NAME_ID + " = " + bakiyeIzinTipi.getId() + " AND I." + PersonelIzin.COLUMN_NAME_ID + " IS NULL");
					String sqlStr = queryStr.toString();
					try {
						if (sure >= 0) {
							query1 = session.createSQLQuery(sqlStr);
							query1.executeUpdate();
							yeni = true;
						}

					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());
						logger.error(e.getMessage() + "\n" + sqlStr);
					}
					queryStr = null;
				}
			} else
				break;

		}
		if (bakiyeIzin != null)
			bakiyeIzin.setCheckBoxDurum(yeni && sayac == 2);

		return bakiyeIzin;
	}

	/**
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@Transactional
	public List<PersonelView> yeniPersonelleriOlustur(Session session) throws Exception {
		List<PersonelView> list = new ArrayList<PersonelView>();
		if (session == null)
			session = PdksUtil.getSession(entityManager, true);
		String parametreKey = getParametrePersonelERPTableView();
		if (getParameterKeyHasStringValue(parametreKey)) {
			StringBuffer sb = new StringBuffer();
			sb.append(" SELECT PS." + PersonelKGS.COLUMN_NAME_SICIL_NO + " FROM " + PersonelERPDB.VIEW_NAME + " D WITH(nolock) ");
			sb.append(" INNER JOIN " + PersonelKGS.TABLE_NAME + " PS ON PS." + PersonelKGS.COLUMN_NAME_SICIL_NO + " = D." + PersonelERPDB.COLUMN_NAME_PERSONEL_NO);
			sb.append(" INNER JOIN " + KapiSirket.TABLE_NAME + " K ON K." + KapiSirket.COLUMN_NAME_ID + " = PS." + PersonelKGS.COLUMN_NAME_KGS_SIRKET + " AND PS." + PersonelKGS.COLUMN_NAME_DURUM + " = 1");
			sb.append(" AND K." + KapiSirket.COLUMN_NAME_DURUM + " = 1 AND K." + KapiSirket.COLUMN_NAME_BIT_TARIH + " > GETDATE()");
			sb.append(" LEFT JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_KGS_PERSONEL + " = PS." + PersonelKGS.COLUMN_NAME_ID);
			sb.append(" WHERE P." + Personel.COLUMN_NAME_ID + " IS NULL");
			sb.append(" AND PS." + PersonelKGS.COLUMN_NAME_SICIL_NO + " NOT IN ( SELECT " + Personel.COLUMN_NAME_PDKS_SICIL_NO + " FROM " + Personel.TABLE_NAME + ")");
			HashMap fields = new HashMap();
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<String> perNoList = pdksEntityController.getObjectBySQLList(sb, fields, null);
			if (!perNoList.isEmpty()) {
				List<PersonelERPDB> personelERPDBList = getPersonelERPDBList(false, perNoList, parametreKey, session);
				if (!personelERPDBList.isEmpty()) {
					List<String> perNoDbList = new ArrayList<String>();
					for (PersonelERPDB personelERPDB : personelERPDBList) {
						perNoDbList.add(personelERPDB.getPersonelNo());
					}
					List<PersonelERP> updateList = personelERPDBGuncelle(false, perNoDbList, session);
					if (updateList != null) {
						fields.clear();
						fields.put("pdksPersonel.pdksSicilNo", perNoDbList);
						if (session != null)
							fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						List<PersonelView> personelList = pdksEntityController.getObjectByInnerObjectList(fields, PersonelView.class);
						if (!personelList.isEmpty()) {
							list = new ArrayList<PersonelView>();
							Date bugun = new Date();
							for (PersonelView personelView : personelList) {
								PersonelKGS personelKGS = personelView.getPersonelKGS();
								if (personelKGS.getKapiSirket() != null && personelKGS.getKapiSirket().getDurum() && personelKGS.getKapiSirket().getBitTarih().after(bugun))
									list.add(personelView);
							}
						}
						personelList = null;
					}
					updateList = null;
				}
			}

		} else {

			HashMap map = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT P.* from " + PersonelKGS.TABLE_NAME + " P WITH(nolock) ");
			sb.append(" WHERE P." + PersonelKGS.COLUMN_NAME_PERSONEL_ID + " IS NULL AND   P." + PersonelKGS.COLUMN_NAME_SICIL_NO + " LIKE '9%' AND  LEN(P." + PersonelKGS.COLUMN_NAME_SICIL_NO + ") = 8   AND ");
			sb.append(" (NOT (UPPER(P." + PersonelKGS.COLUMN_NAME_ACIKLAMA + ") LIKE '%İPTAL %' OR   UPPER(P." + PersonelKGS.COLUMN_NAME_ACIKLAMA + ") LIKE '%IPTAL %'))");

			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelView> perList = getPersonelViewByPersonelKGSList(pdksEntityController.getObjectBySQLList(sb, map, PersonelKGS.class));
			if (!perList.isEmpty()) {
				logger.info("yeniPersonelleriOlustur (" + perList.size() + ") in " + getCurrentTimeStampStr());
				List<String> siciller = new ArrayList<String>();
				// perList.clear();
				String sicilNo = "";
				for (Iterator iterator = perList.iterator(); iterator.hasNext();) {
					PersonelView personelView = (PersonelView) iterator.next();
					try {
						if (PdksUtil.hasStringValue(personelView.getPersonelKGS().getSicilNo()))
							sicilNo = String.valueOf(Long.parseLong(personelView.getPersonelKGS().getSicilNo()));
						else
							sicilNo = personelView.getPersonelKGS().getSicilNo();
						String personelAciklama = PdksUtil.setTurkishStr(personelView.getPdksPersonelAciklama()).toUpperCase(Locale.ENGLISH);
						if ((!sicilNo.startsWith("9")) || sicilNo.trim().length() != 8 || personelAciklama.indexOf("IPTAL") >= 0)
							iterator.remove();
						else
							siciller.add(sicilNo);
					} catch (Exception e) {
						iterator.remove();
					}
				}
				if (!siciller.isEmpty()) {
					map.clear();
					String sablonKodu = getParameterKey("sapSablonKodu");
					if (PdksUtil.hasStringValue(sablonKodu))
						map.put("adi", sablonKodu);
					else
						map.put("id", 1L);
					map.put("departman.admin", true);
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					VardiyaSablonu sablon = (VardiyaSablonu) pdksEntityController.getObjectByInnerObject(map, VardiyaSablonu.class);

					map.clear();
					map.put("durum", Boolean.TRUE);
					map.put("ldap", Boolean.TRUE);
					map.put("erpDurum", Boolean.TRUE);
					map.put("pdks", Boolean.TRUE);
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<Sirket> sirketList = pdksEntityController.getObjectByInnerObjectList(map, Sirket.class);
					for (Iterator iterator = sirketList.iterator(); iterator.hasNext();) {
						Sirket sirket = (Sirket) iterator.next();
						if (sirket.getLpdapOnEk() == null) {
							iterator.remove();
							continue;
						}
					}
					map.clear();
					map.put(PdksEntityController.MAP_KEY_MAP, "getKodu");
					map.put("tipi", Tanim.TIPI_SAP_MASRAF_YERI);
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					TreeMap masrafYeriMap = pdksEntityController.getObjectByInnerObjectMap(map, Tanim.class, Boolean.FALSE);
					map.clear();
					map.put(PdksEntityController.MAP_KEY_MAP, "getKodu");
					map.put("tipi", Tanim.TIPI_BORDRO_ALT_BIRIMI);
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					TreeMap bordroAltBirimiMap = pdksEntityController.getObjectByInnerObjectMap(map, Tanim.class, Boolean.FALSE);

					map.clear();
					map.put("pdksSicilNo", siciller);
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					TreeMap<String, Personel> personelMap = pdksEntityController.getObjectByInnerObjectMap(map, Personel.class, Boolean.FALSE);

					for (Iterator iterator1 = perList.iterator(); iterator1.hasNext();) {
						PersonelView personelView = (PersonelView) iterator1.next();
						Personel personel = null;
						sicilNo = String.valueOf(Long.parseLong(personelView.getPersonelKGS().getSicilNo()));
						if (personelMap.containsKey(sicilNo)) {
							iterator1.remove();
							continue;
						}

						for (Iterator iterator = sirketList.iterator(); iterator.hasNext();) {
							Sirket sirket = (Sirket) iterator.next();
							String kullaniciAdi = sirket.getLpdapOnEk().trim() + sicilNo.substring(3).trim();
							User ldapUser = kullaniciBul(kullaniciAdi, LDAPUserManager.USER_ATTRIBUTES_SAM_ACCOUNT_NAME);
							if (ldapUser != null && !ldapUser.isDurum())
								ldapUser = null;
							if (ldapUser != null) {
								personel = new Personel();
								personel.setPersonelKGS(personelView.getPersonelKGS());
								personel.setPdksSicilNo(sicilNo);
								personel.setSirket(sirket);
								personel.setSablon(sablon);
								personel.setDurum(Boolean.TRUE);
								try {
									sapVeriGuncelle(session, null, bordroAltBirimiMap, masrafYeriMap, personel, null, Boolean.TRUE, session == null, Boolean.TRUE);
									personel.setPdksSicilNo(personelView.getPersonelKGS().getSicilNo());
									if (personel.getId() != null) {
										ldapUser.setDurum(Boolean.FALSE);
										ldapUser.setPdksPersonel(personel);
										ldapUser.setDepartman(personel.getSirket().getDepartman());
										pdksEntityController.saveOrUpdate(session, entityManager, ldapUser);
										session.flush();
										personelView.setPdksPersonel(personel);
										personelView.setKullanici(ldapUser);
										list.add(personelView);
										iterator1.remove();
									}

								} catch (Exception e) {
									logger.error("Pdks hata in : \n");
									e.printStackTrace();
									logger.error("Pdks hata out : " + e.getMessage());
									logger.error(e.getLocalizedMessage());
								}
								break;
							}
						}

					}
				}
				logger.info("yeniPersonelleriOlustur (" + perList.size() + ") out " + getCurrentTimeStampStr());
				siciller = null;
			}
			perList = null;
		}
		return list;
	}

	/**
	 * @param izin
	 * @param session
	 */
	public void bakiyeIzinSil(PersonelIzin izin, Session session) {

		try {

			if (authenticatedUser != null)
				izin.setGuncelleyenUser(authenticatedUser);
			izin.setGuncellemeTarihi(new Date());
			izin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL);
			pdksEntityController.saveOrUpdate(session, entityManager, izin);

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			PdksUtil.addMessageError(e.getMessage());
		}
	}

	/**
	 * @param basTarih
	 * @param bitTarih
	 * @param yemekList
	 * @param session
	 * @return
	 */
	public double getSaatSure(Date basTarih, Date bitTarih, List<YemekIzin> yemekList, VardiyaGun vardiyaGun, Session session) {
		double sure = 0, toplamYemekSure = 0.0d;
		double yemekVardiyaSuresi = vardiyaGun != null && vardiyaGun.getVardiya() != null && vardiyaGun.getVardiya().getYemekSuresi() != null ? vardiyaGun.getVardiya().getYemekSuresi() / 60.0d : 0.0d;
		if (bitTarih.getTime() > basTarih.getTime()) {
			sure = PdksUtil.getSaatFarki(bitTarih, basTarih).doubleValue();
			boolean yarimdenFazla = sure > 10;
			List<Long> idList = new ArrayList<Long>();
			if (sure > 0) {
				if (yemekList == null) {
					if (vardiyaGun != null && vardiyaGun.getId() != null)
						yemekList = vardiyaGun.getYemekList();
					else
						yemekList = getYemekList(basTarih, bitTarih, session);
				}

				if (!yemekList.isEmpty()) {
					yemekList = PdksUtil.sortListByAlanAdi(yemekList, "basKey", Boolean.FALSE);
					int basGun, bitGun;
					Calendar cal = Calendar.getInstance();
					cal.setTime(basTarih);
					basGun = cal.get(Calendar.DATE);
					cal.setTime(bitTarih);
					bitGun = cal.get(Calendar.DATE);
					List<Date> basList = new ArrayList<Date>();
					List<Date> bitList = new ArrayList<Date>();
					basList.add((Date) basTarih.clone());
					if (basGun != bitGun) {
						Date tarih = PdksUtil.getDate((Date) bitTarih.clone());
						bitList.add(tarih);
						basList.add(tarih);
					}
					bitList.add((Date) bitTarih.clone());
					boolean cik = Boolean.FALSE;
					Long vardiyaId = null;
					try {
						if (vardiyaGun != null && vardiyaGun.getVardiya() != null && vardiyaGun.getVardiya().isCalisma())
							vardiyaId = vardiyaGun.getVardiya().getId();
					} catch (Exception ex) {
					}
					List<Integer> aralikList = new ArrayList<Integer>();
					for (Iterator iterator = yemekList.iterator(); iterator.hasNext();) {
						YemekIzin yemekIzin = (YemekIzin) iterator.next();
						if (idList.contains(yemekIzin.getId()))
							continue;
						if (yemekIzin.isOzelMolaVarmi() && !yemekIzin.containsKey(vardiyaId))
							continue;
						if (!(yemekIzin.getBitTarih().getTime() > basTarih.getTime() && yemekIzin.getBasTarih().getTime() < bitTarih.getTime()))
							continue;
						double yemekSure = 0;
						double yemekSaat = (double) yemekIzin.getMaxSure() / 60;
						for (int i = 0; i < basList.size(); i++) {
							if (aralikList.contains(i))
								continue;

							cal.setTime((Date) basList.get(i));
							cal.set(Calendar.HOUR_OF_DAY, yemekIzin.getBaslangicSaat());
							cal.set(Calendar.MINUTE, yemekIzin.getBaslangicDakika());
							Date basZamanYemek = cal.getTime();
							cal.setTime((Date) basList.get(i));
							cal.set(Calendar.HOUR_OF_DAY, yemekIzin.getBitisSaat());
							cal.set(Calendar.MINUTE, yemekIzin.getBitisDakika());
							Date bitZamanYemek = cal.getTime();
							Date basListDate = basList.get(i), bitListDate = bitList.get(i);
							if (bitZamanYemek.getTime() > basListDate.getTime() && bitListDate.getTime() > basZamanYemek.getTime()) {
								if (basZamanYemek.getTime() < basListDate.getTime())
									basZamanYemek = (Date) basListDate.clone();
								if (bitZamanYemek.getTime() > bitListDate.getTime())
									bitZamanYemek = (Date) bitListDate.clone();
								yemekSure += PdksUtil.getSaatFarki(bitZamanYemek, basZamanYemek).doubleValue();
								if (!yarimdenFazla)
									cik = Boolean.TRUE;
								aralikList.add(i);
							}

						}
						if (yemekSaat > 0 && yemekSure > yemekSaat)
							yemekSure = yemekSaat;
						if (yemekSure > 0.0d) {
							toplamYemekSure += yemekSure;
							if (vardiyaGun != null && vardiyaGun.getTatil() != null)
								logger.debug(vardiyaGun.getVardiyaDateStr() + " " + yemekIzin.getId() + " " + yemekIzin.getYemekAciklama() + "" + yemekSure + " " + toplamYemekSure);
							idList.add(yemekIzin.getId());
						}

						if (cik)
							break;

					}
					if (toplamYemekSure > 0) {
						if (toplamYemekSure > yemekVardiyaSuresi)
							toplamYemekSure = yemekVardiyaSuresi;
						sure = sure - toplamYemekSure;
					}
					aralikList = null;
				} else if (yemekVardiyaSuresi > 0) {

					if (sure > vardiyaGun.getVardiya().getNetCalismaSuresi()) {
						sure = sure - yemekVardiyaSuresi;
					}
				}
			}

		}

		return sure;
	}

	/**
	 * @param session
	 * @param pdks
	 * @param kendisiBul
	 * @return
	 */
	public List<Sirket> fillSirketList(Session session, Boolean pdks, Boolean kendisiBul) {
		List<Sirket> pdksSirketList = new ArrayList<Sirket>();
		HashMap parametreMap = new HashMap();
		parametreMap.put("durum", Boolean.TRUE);
		if (pdks != null)
			parametreMap.put("pdks", pdks);
		if (identity.isLoggedIn() && !authenticatedUser.isAdmin() && !authenticatedUser.isIKAdmin() && !authenticatedUser.getDepartman().isAdminMi())
			parametreMap.put("departman.id", authenticatedUser.getDepartman().getId());

		parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		pdksSirketList = pdksEntityController.getObjectByInnerObjectList(parametreMap, Sirket.class);
		if (identity.isLoggedIn() && !authenticatedUser.isAdmin() && !authenticatedUser.isIKAdmin() && authenticatedUser.getDepartman().isAdminMi()) {

			for (Iterator iterator = pdksSirketList.iterator(); iterator.hasNext();) {
				Sirket sirket = (Sirket) iterator.next();
				if (!authenticatedUser.getDepartman().getId().equals(sirket.getDepartman().getId()) && !authenticatedUser.getPdksPersonel().getSirket().getId().equals(sirket.getId()))
					iterator.remove();

			}
		}
		if (identity.isLoggedIn())
			digerIKSirketBul(pdksSirketList, kendisiBul, session);

		if (pdksSirketList.size() > 1)
			pdksSirketList = PdksUtil.sortObjectStringAlanList(pdksSirketList, "getAd", null);
		return pdksSirketList;
	}

	/**
	 * @param pdksSirketList
	 * @param kendisiBul
	 * @param session
	 */
	public void digerIKSirketBul(List<Sirket> pdksSirketList, Boolean kendisiBul, Session session) {
		if (authenticatedUser.isIK() && !authenticatedUser.getDepartman().isAdminMi()) {
			HashMap map = new HashMap();
			map.put(PdksEntityController.MAP_KEY_SELECT, "sirket");
			map.put("sirket=", authenticatedUser.getPdksPersonel().getSirket());
			map.put("pdksSicilNo", authenticatedUser.getYetkiTumPersonelNoList());
			map.put("durum=", Boolean.TRUE);
			if (!kendisiBul)
				map.put("id<>", authenticatedUser.getPdksPersonel().getId());
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				Sirket perSirket = (Sirket) pdksEntityController.getObjectByInnerObjectInLogic(map, Personel.class);
				if (perSirket != null) {
					boolean yok = Boolean.TRUE;
					for (Iterator iterator = pdksSirketList.iterator(); iterator.hasNext();) {
						Sirket sirket = (Sirket) iterator.next();
						if (sirket.getId().equals(perSirket.getId()))
							yok = Boolean.FALSE;

					}
					if (yok)
						pdksSirketList.add(perSirket);
				}

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				PdksUtil.addMessageError("digerIKSirketBul " + e.getMessage());
			}
		}
	}

	/**
	 * @param tanimlar
	 * @return
	 */
	public List<SelectItem> getTanimSelectItemByKodu(List<Tanim> tanimlar) {

		List<SelectItem> items = null;
		if (tanimlar != null) {
			items = new ArrayList<SelectItem>();
			for (Tanim tanim : tanimlar)
				if (tanim.getDurum())
					items.add(new SelectItem(tanim.getKodu(), tanim.getAciklama()));
		}
		return items;
	}

	/**
	 * @param tanimlar
	 * @return
	 */
	public List<SelectItem> getTanimSelectItem(List<Tanim> tanimlar) {

		List<SelectItem> items = null;
		if (tanimlar != null) {
			items = new ArrayList<SelectItem>();
			for (Tanim tanim : tanimlar)
				if (tanim.getDurum())
					items.add(new SelectItem(tanim.getId(), tanim.getAciklama()));
		}
		return items;
	}

	/**
	 * @return
	 */
	public String emtpyAjaxFunction() {
		return "";
	}

	/**
	 * @param fieldAdi
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
	protected List<SelectItem> bolumTesisDoldur(String fieldAdi, Departman pdksDepartman, Sirket sirket, Long departmanId, Long tesisId, Integer yil, Integer ay, Boolean denklestirme, Session session) {

		List<SelectItem> gorevTipiList = null;
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
		if (fieldAdi == null)
			fieldAdi = "ekSaha3";
		fields.put(PdksEntityController.MAP_KEY_SELECT, fieldAdi);
		if (sirket != null) {
			if (sirket.isTesisDurumu()) {
				boolean tesisYetki = getParameterKey("tesisYetki").equals("1");
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
			fields.put("pdksSicilNo", getYetkiTumPersonelNoListesi(authenticatedUser));
		fields.put("iseBaslamaTarihi<=", bitTarih);
		fields.put("sskCikisTarihi>=", basTarih);
		if (pdksDepartman != null && pdksDepartman.isAdminMi() && denklestirme != null && denklestirme)
			fields.put("pdks=", Boolean.TRUE);
		fields.put(fieldAdi + " <> ", null);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Tanim> tanimlar = pdksEntityController.getObjectByInnerObjectListInLogic(fields, Personel.class);
		if (authenticatedUser.isYonetici() || authenticatedUser.isDirektorSuperVisor() || authenticatedUser.isAdmin() || authenticatedUser.isIK() || tanimlar.size() > 50) {
			HashMap<Long, Tanim> tanimMap = new HashMap<Long, Tanim>();
			for (Tanim tanim : tanimlar)
				tanimMap.put(tanim.getId(), tanim);

			tanimlar = null;
			tanimlar = PdksUtil.sortObjectStringAlanList(new ArrayList<Tanim>(tanimMap.values()), "getAciklama", null);
			gorevTipiList = getTanimSelectItem(tanimlar);
			tanimMap = null;

		}
		tanimlar = null;
		return gorevTipiList;

	}

	/**
	 * @param kendisiBul
	 * @param sirketEkle
	 * @param session
	 * @return
	 */
	public HashMap fillEkSahaTanimBul(Boolean kendisiBul, Boolean sirketEkle, Session session) {

		HashMap sonucMap = new HashMap();
		HashMap<String, List<Tanim>> ekSahaListMap = new HashMap<String, List<Tanim>>();
		TreeMap<String, List<SelectItem>> islemEkSahaSelectListMap = new TreeMap<String, List<SelectItem>>();
		HashMap<String, List<SelectItem>> ekSahaSelectListMap = new HashMap<String, List<SelectItem>>();
		HashMap map = new HashMap();
		map.put(PdksEntityController.MAP_KEY_MAP, "getKodu");
		map.put("tipi", Tanim.TIPI_PERSONEL_EK_SAHA_ACIKLAMA);
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT DISTINCT P.* FROM " + Tanim.TABLE_NAME + " T WITH(nolock) ");
		sb.append(" INNER JOIN " + Tanim.TABLE_NAME + " P ON  P." + Tanim.COLUMN_NAME_ID + " = T." + Tanim.COLUMN_NAME_PARENT_ID + " AND P." + Tanim.COLUMN_NAME_DURUM + " = 1 ");
		sb.append(" WHERE T." + Tanim.COLUMN_NAME_TIPI + " = :tipi  AND T." + Tanim.COLUMN_NAME_DURUM + " = 1 ");
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		TreeMap<String, Tanim> ekSahaTanimMap = pdksEntityController.getObjectBySQLMap(sb, map, Tanim.class, Boolean.TRUE);

		List<Tanim> ekSahalar = new ArrayList(ekSahaTanimMap.values());
		List<Long> idler = new ArrayList<Long>();
		for (Iterator iterator = ekSahalar.iterator(); iterator.hasNext();) {
			Tanim tanim = (Tanim) iterator.next();
			idler.add(tanim.getId());
		}
		sb = null;

		map.clear();
		map.put("tipi", Tanim.TIPI_PERSONEL_EK_SAHA_ACIKLAMA);
		sb = new StringBuffer();
		sb.append("SELECT   T.* FROM " + Tanim.TABLE_NAME + " T WITH(nolock) ");
		sb.append(" WHERE T." + Tanim.COLUMN_NAME_TIPI + " = :tipi   AND T." + Tanim.COLUMN_NAME_DURUM + " = 1   ");
		String fieldName = null;
		if (!idler.isEmpty()) {
			fieldName = "pt";
			map.put("pt", idler);
			sb.append("  AND T." + Tanim.COLUMN_NAME_PARENT_ID + " :" + fieldName);
		}
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Tanim> list = fieldName != null ? getSQLParamList(idler, sb, fieldName, map, Tanim.class, session) : pdksEntityController.getObjectBySQLList(sb, map, Tanim.class);
		if (list.size() > 1)
			list = PdksUtil.sortObjectStringAlanList(list, "getAciklama", null);
		sb = null;
		map.clear();
		map.put("tipi", Tanim.TIPI_TESIS);
		sb = new StringBuffer();
		sb.append("SELECT   T.* FROM " + Tanim.TABLE_NAME + " T WITH(nolock) ");
		sb.append(" WHERE T." + Tanim.COLUMN_NAME_TIPI + " = :tipi   AND T." + Tanim.COLUMN_NAME_DURUM + " = 1   ");
		sb.append(" ORDER BY T." + Tanim.COLUMN_NAME_KODU);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Tanim> tesisTanimList = getTesisDurumu() ? pdksEntityController.getObjectBySQLList(sb, map, Tanim.class) : null;
		if (tesisTanimList != null && !tesisTanimList.isEmpty()) {
			List<SelectItem> tesisSelectList = new ArrayList<SelectItem>();
			for (Tanim tanim : tesisTanimList)
				tesisSelectList.add(new SelectItem(tanim.getId(), tanim.getAciklama()));
			sonucMap.put("tesisSelectList", tesisSelectList);
			sonucMap.put("tesisTanimList", tesisTanimList);
		}

		ekSahalar = null;
		idler = null;
		sb = null;
		sonucMap.put("ekSahaSelectListMap", ekSahaSelectListMap);
		sonucMap.put("ekSahaTanimMap", ekSahaTanimMap);
		sonucMap.put("ekSahaList", ekSahaListMap);
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Tanim tanim = (Tanim) iterator.next();
			if (tanim != null && tanim.getParentTanim() != null) {
				String key = tanim.getParentTanim().getKodu();
				List<SelectItem> ekSahaSelectList = islemEkSahaSelectListMap.containsKey(key) ? islemEkSahaSelectListMap.get(key) : new ArrayList<SelectItem>();
				if (ekSahaSelectList.isEmpty())
					islemEkSahaSelectListMap.put(key, ekSahaSelectList);
				ekSahaSelectList.add(new SelectItem(tanim.getId(), tanim.getAciklama()));

				List<Tanim> ekSahaList = ekSahaListMap.containsKey(key) ? ekSahaListMap.get(key) : new ArrayList<Tanim>();
				if (ekSahaList.isEmpty())
					ekSahaListMap.put(key, ekSahaList);
				ekSahaList.add(tanim);
			}

		}
		if (!islemEkSahaSelectListMap.isEmpty()) {
			for (Iterator iterator = islemEkSahaSelectListMap.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				ekSahaSelectListMap.put(key, islemEkSahaSelectListMap.get(key));
			}
		}
		if (sirketEkle == null || sirketEkle) {

			List<SelectItem> sirketIdList = new ArrayList<SelectItem>();
			List<Sirket> sirketList = fillSirketList(session, Boolean.TRUE, kendisiBul);
			for (Sirket sirket : sirketList)
				sirketIdList.add(new SelectItem(sirket.getId(), sirket.getAd()));
			sonucMap.put("sirketIdList", sirketIdList);
			sonucMap.put("sirketList", sirketList);

		}

		return sonucMap;

	}

	/**
	 * @param session
	 * @param kendisiBul
	 * @param sirketEkle
	 * @param aramaSecenekleri
	 */
	public HashMap fillEkSahaTanimAramaSecenekAta(Session session, Boolean kendisiBul, Boolean sirketEkle, AramaSecenekleri aramaSecenekleri) {
		if (aramaSecenekleri.getSessionClear())
			session.clear();
		HashMap sonucMap = fillEkSahaTanimBul(kendisiBul, sirketEkle, session);
		TreeMap<String, Tanim> ekSahaTanimMap = null;
		if (sonucMap != null && !sonucMap.isEmpty()) {
			if (authenticatedUser != null && (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin())) {
				List<SelectItem> departmanIdList = new ArrayList<SelectItem>();
				List<Departman> departmanList = fillDepartmanTanimList(session);
				for (Departman pdksDepartman : departmanList)
					departmanIdList.add(new SelectItem(pdksDepartman.getId(), pdksDepartman.getDepartmanTanim().getAciklama()));
				aramaSecenekleri.setDepartmanIdList(departmanIdList);
				aramaSecenekleri.setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
				ekSahaTanimMap = (TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap");
				aramaSecenekleri.setEkSahaTanimMap(ekSahaTanimMap);
				aramaSecenekleri.setEkSahaSelectListMap((HashMap<String, List<SelectItem>>) sonucMap.get("ekSahaSelectListMap"));
			}

			List<Sirket> sirketList = (List<Sirket>) sonucMap.get("sirketList");
			if (sirketList != null) {
				List<SelectItem> sirketIdList = (List<SelectItem>) sonucMap.get("sirketIdList");
				if (aramaSecenekleri.getSirketIzinKontrolYok().booleanValue() == false) {
					for (Iterator iterator = sirketList.iterator(); iterator.hasNext();) {
						Sirket sirket = (Sirket) iterator.next();
						if (sirket.getDepartman().getIzinGirilebilir() == null || sirket.getDepartman().getIzinGirilebilir().booleanValue() == false)
							iterator.remove();
					}
					sirketIdList.clear();
					List<Long> idList = new ArrayList<Long>();
					for (Iterator iterator = sirketList.iterator(); iterator.hasNext();) {
						Sirket sirket = (Sirket) iterator.next();
						idList.add(sirket.getId());
						sirketIdList.add(new SelectItem(sirket.getId(), sirket.getAd()));
					}
					if (!idList.isEmpty() && ekSahaTanimMap != null && !ekSahaTanimMap.isEmpty()) {
						String fieldName = "s";
						StringBuffer sb = new StringBuffer();
						HashMap fields = new HashMap();
						sb.append(" WITH PER AS ( ");
						sb.append(" SELECT P.* FROM " + Personel.TABLE_NAME + " P WITH(nolock) ");
						sb.append(" WHERE P." + Personel.COLUMN_NAME_SIRKET + " :" + fieldName + " ), ");
						sb.append(" EK_SAHA AS ( ");
						String str = "";
						if (ekSahaTanimMap.containsKey("ekSaha1")) {
							sb.append(" SELECT P." + Personel.COLUMN_NAME_EK_SAHA1 + " AS ID FROM PER P ");
							sb.append(" WHERE P." + Personel.COLUMN_NAME_EK_SAHA1 + " IS NOT NULL ");
							str = " UNION ALL ";
						}
						if (ekSahaTanimMap.containsKey("ekSaha2")) {
							sb.append(str);
							sb.append(" SELECT P." + Personel.COLUMN_NAME_EK_SAHA2 + " AS ID FROM PER P ");
							sb.append(" WHERE P." + Personel.COLUMN_NAME_EK_SAHA2 + " IS NOT NULL ");
							str = " UNION ALL ";
						}
						if (ekSahaTanimMap.containsKey("ekSaha3")) {
							sb.append(str);
							sb.append(" SELECT P." + Personel.COLUMN_NAME_EK_SAHA3 + " AS ID FROM PER P ");
							sb.append(" WHERE P." + Personel.COLUMN_NAME_EK_SAHA3 + " IS NOT NULL ");
							str = " UNION ALL ";
						}
						if (ekSahaTanimMap.containsKey("ekSaha4")) {
							sb.append(str);
							sb.append(" SELECT P." + Personel.COLUMN_NAME_EK_SAHA4 + " AS ID FROM PER P ");
							sb.append(" WHERE P." + Personel.COLUMN_NAME_EK_SAHA4 + " IS NOT NULL ");
						}
						sb.append(" ) ");
						sb.append(" SELECT DISTINCT T.* FROM EK_SAHA E ");
						sb.append(" INNER JOIN " + Tanim.TABLE_NAME + " T ON T." + Tanim.COLUMN_NAME_ID + " = E.ID AND T." + Tanim.COLUMN_NAME_DURUM + " = 1 ");
						fields.put(fieldName, idList);
						if (session != null)
							fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						// List<Tanim> tanimlar = pdksEntityController.getObjectBySQLList(sb, fields, Tanim.class);
						List<Tanim> tanimlar = getSQLParamList(idList, sb, fieldName, fields, Tanim.class, session);

						if (!tanimlar.isEmpty()) {
							tanimlar = PdksUtil.sortTanimList(null, tanimlar);
							HashMap<String, List<Tanim>> ekSahaListMap = new HashMap<String, List<Tanim>>();
							for (Tanim tanim : tanimlar) {
								String key = tanim.getParentTanim().getKodu();
								if (key.startsWith("BOLUM_DEPARTMAN"))
									key = "ekSaha3";
								List<Tanim> list = ekSahaListMap.containsKey(key) ? ekSahaListMap.get(key) : new ArrayList<Tanim>();
								if (list.isEmpty())
									ekSahaListMap.put(key, list);
								list.add(tanim);
							}
							List<String> list = new ArrayList<String>(ekSahaTanimMap.keySet());
							HashMap<String, List<SelectItem>> ekSahaSelectListMap = new HashMap<String, List<SelectItem>>();
							for (String key : list) {
								if (ekSahaListMap.containsKey(key)) {
									List<Tanim> tanimList = ekSahaListMap.get(key);
									List<SelectItem> selectItemList = new ArrayList<SelectItem>();
									for (Tanim tanim : tanimList) {
										selectItemList.add(new SelectItem(tanim.getId(), tanim.getAciklama()));
									}
									ekSahaSelectListMap.put(key, selectItemList);
								} else {
									ekSahaTanimMap.remove(key);
								}

							}
							list = null;
							aramaSecenekleri.setEkSahaListMap(ekSahaListMap);
							aramaSecenekleri.setEkSahaSelectListMap(ekSahaSelectListMap);
							aramaSecenekleri.setEkSahaTanimMap(ekSahaTanimMap);

						}
					}

				}
				if (aramaSecenekleri.getStajyerOlmayanSirket()) {
					sirketList = getStajerOlmayanSirketler(sirketList);
					sirketIdList.clear();
					for (Sirket sirket : sirketList)
						sirketIdList.add(new SelectItem(sirket.getId(), sirket.getAd()));
				}
				aramaSecenekleri.setSirketIdList(sirketIdList);
			}
			aramaSecenekleri.setSirketList(sirketList);

			if (sonucMap.containsKey("tesisSelectList"))
				aramaSecenekleri.setTesisList((List<SelectItem>) sonucMap.get("tesisSelectList"));
			if (sonucMap.containsKey("tesisTanimList"))
				aramaSecenekleri.setTesisTanimList((List<Tanim>) sonucMap.get("tesisTanimList"));

		}
		return sonucMap;
	}

	/**
	 * @param session
	 * @param kendisiBul
	 * @param sirketEkle
	 * @return
	 */
	public HashMap fillEkSahaTanim(Session session, Boolean kendisiBul, Boolean sirketEkle) {
		HashMap sonucMap = fillEkSahaTanimBul(kendisiBul, sirketEkle, session);
		TreeMap<String, Tanim> tanimMap = (TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap");
		String departmanAciklama = tanimMap != null && tanimMap.containsKey("ekSaha1") ? tanimMap.get("ekSaha1").getAciklama() : "Departman";
		String bolumAciklama = tanimMap != null && tanimMap.containsKey("ekSaha3") ? tanimMap.get("ekSaha3").getAciklama() : "Bölüm";
		String altBolumAciklama = tanimMap != null && tanimMap.containsKey("ekSaha4") ? tanimMap.get("ekSaha4").getAciklama() : "Alt Bölüm";
		if (bolumAciklama == null)
			bolumAciklama = bolumAciklama();
		sonucMap.put("sirketAciklama", sirketAciklama());
		sonucMap.put("tesisAciklama", tesisAciklama());
		sonucMap.put("bolumAciklama", bolumAciklama);

		sonucMap.put("departmanAciklama", departmanAciklama);
		sonucMap.put("altBolumAciklama", altBolumAciklama);
		return sonucMap;
	}

	/**
	 * @param personeller
	 * @param personelMap
	 * @param devam
	 * @param session
	 */
	private void yoneticiPersonelleriBul(List<Personel> personeller, HashMap<Long, Personel> personelMap, boolean devam, Session session) {
		List<Personel> list = new ArrayList<Personel>();
		Date bugun = PdksUtil.getDate(Calendar.getInstance().getTime());
		HashMap map = new HashMap();
		map.put(PdksEntityController.MAP_KEY_MAP, "getId");
		map.put("sskCikisTarihi>=", bugun);
		map.put("iseBaslamaTarihi<=", bugun);
		map.put("yoneticisi", personeller);
		map.put("durum=", Boolean.TRUE);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		TreeMap<Long, Personel> personellerMap = pdksEntityController.getObjectByInnerObjectMapInLogic(map, Personel.class, Boolean.FALSE);
		if (!personellerMap.isEmpty())
			list.addAll(new ArrayList<Personel>(personellerMap.values()));

		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Personel personel = (Personel) iterator.next();

			if (personelMap.containsKey(personel.getId()))
				iterator.remove();
			else
				personelMap.put(personel.getId(), personel);
		}

		if (devam && !list.isEmpty())
			yoneticiPersonelleriBul(list, personelMap, devam, session);

	}

	/**
	 * @param message
	 * @param severity
	 */
	private void addMessage(String message, Severity severity) {

		// facesMessages.clear();
		facesMessages.add(severity, message, "");

	}

	/**
	 * @param message
	 */
	public void addMessageError(String message) {
		addMessage(message, Severity.ERROR);
	}

	/**
	 * @param message
	 */
	public void addMessageWarn(String message) {
		addMessage(message, Severity.WARN);
	}

	/**
	 * @param message
	 */
	public void addMessageInfo(String message) {
		addMessage(message, Severity.INFO);
	}

	/**
	 * @param message
	 */
	public void addMessageFatal(String message) {
		addMessage(message, Severity.FATAL);
	}

	/**
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public List<Long> getResultLong(Query query) throws Exception {
		List onayList = new ArrayList();
		List<BigDecimal> resultList = query.getResultList();
		if (!resultList.isEmpty()) {
			for (BigDecimal result : resultList)
				onayList.add(result.longValue());
		}

		return onayList;
	}

	/**
	 * @param key
	 * @param session
	 * @return
	 */
	@Transactional
	public LinkedHashMap<String, Object> getLastParameter(String key, Session session) {
		LinkedHashMap<String, Object> map = null;
		String lastParameterValue = getParameterKey("lastParameterValue");
		if (key != null && (authenticatedUser.isAdmin() || lastParameterValue.equals("1"))) {
			try {
				HashMap parametreMap = new HashMap();
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT I." + UserMenuItemTime.COLUMN_NAME_ID + ",M." + MenuItem.COLUMN_NAME_ID + " AS MENU_ID,I." + UserMenuItemTime.COLUMN_NAME_LAST_PARAMETRE + " FROM " + User.TABLE_NAME + " U WITH(nolock) ");
				sb.append(" INNER JOIN " + MenuItem.TABLE_NAME + " M ON M." + MenuItem.COLUMN_NAME_ADI + " = :a ");
				sb.append(" LEFT JOIN " + UserMenuItemTime.TABLE_NAME + " I ON I." + UserMenuItemTime.COLUMN_NAME_USER + " = U." + User.COLUMN_NAME_ID);
				sb.append(" AND I." + UserMenuItemTime.COLUMN_NAME_MENU + " = M." + MenuItem.COLUMN_NAME_ID);
				sb.append(" WHERE U." + User.COLUMN_NAME_ID + " = :u");
				parametreMap.put("a", key);
				parametreMap.put("u", authenticatedUser.getId());
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<Object[]> veriler = pdksEntityController.getObjectBySQLList(sb, parametreMap, null);
				if (veriler != null) {
					Gson gson = new Gson();
					Object[] veri = veriler.get(0);
					Long id = veri[0] != null ? ((BigDecimal) veri[0]).longValue() : null;
					UserMenuItemTime menuItemTime = null;
					if (id != null) {
						parametreMap.clear();
						parametreMap.put("id", id);
						if (session != null)
							parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						menuItemTime = (UserMenuItemTime) pdksEntityController.getObjectByInnerObject(parametreMap, UserMenuItemTime.class);
					} else if (veri[1] != null) {
						Long menuId = ((BigDecimal) veri[1]).longValue();
						parametreMap.clear();
						parametreMap.put("id", menuId);
						if (session != null)
							parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						MenuItem menuItem = (MenuItem) pdksEntityController.getObjectByInnerObject(parametreMap, MenuItem.class);
						if (menuItem != null) {
							menuItemTime = new UserMenuItemTime(authenticatedUser, menuItem);
							menuItemTime.setFirstTime(new Date());
							menuItemTime.setParametreJSON(null);
							HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
							if (mySession != null)
								menuItemTime.setSessionId(mySession.getId());
							pdksEntityController.saveOrUpdate(session, entityManager, menuItemTime);
							session.flush();
						}
					}
					if (menuItemTime != null && menuItemTime.getParametreJSON() != null) {
						try {
							map = gson.fromJson(menuItemTime.getParametreJSON(), LinkedHashMap.class);
						} catch (Exception e) {
							logger.error(e);
						}
					}
					gson = null;
				}
			} catch (Exception ee) {
				logger.equals(ee);
				ee.printStackTrace();
			}

		}
		if (map == null)
			map = new LinkedHashMap<String, Object>();

		return map;

	}

	/**
	 * @param map
	 * @param session
	 */
	@Transactional
	public void saveLastParameter(LinkedHashMap<String, Object> map, Session session) {
		String key = authenticatedUser.getCalistigiSayfa(), lastParameterValue = getParameterKey("lastParameterValue");
		if (map.containsKey("sayfaURL")) {
			key = (String) map.get("sayfaURL");
		}
		if (key != null && map != null && (authenticatedUser.isAdmin() || lastParameterValue.equals("1"))) {
			try {
				HashMap parametreMap = new HashMap();
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT I.* FROM " + User.TABLE_NAME + " U WITH(nolock) ");
				sb.append(" INNER JOIN " + MenuItem.TABLE_NAME + " M ON M." + MenuItem.COLUMN_NAME_ADI + " = :a ");
				sb.append(" INNER JOIN " + UserMenuItemTime.TABLE_NAME + " I ON I." + UserMenuItemTime.COLUMN_NAME_USER + " = U." + User.COLUMN_NAME_ID);
				sb.append(" AND I." + UserMenuItemTime.COLUMN_NAME_MENU + " = M." + MenuItem.COLUMN_NAME_ID);
				sb.append(" WHERE U." + User.COLUMN_NAME_ID + " = :u");
				parametreMap.put("a", key);
				parametreMap.put("u", authenticatedUser.getId());
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<UserMenuItemTime> veriler = pdksEntityController.getObjectBySQLList(sb, parametreMap, UserMenuItemTime.class);
				Gson gson = new Gson();
				LinkedHashMap<String, Object> map1 = new LinkedHashMap<String, Object>();
				map1.put("kullanici", authenticatedUser.getAdSoyad());
				map1.put("menuAdi", getMenuUserLogAdi(null, key, false));
				map1.putAll(map);
				String parametreJSON = gson.toJson(map1);
				UserMenuItemTime menuItemTime = null;
				if (!veriler.isEmpty()) {
					menuItemTime = veriler.get(0);
				} else {
					HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
					sb = new StringBuffer();
					sb.append("SP_USER_MENUITEM");
					LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
					fields.put("sId", mySession.getId());
					fields.put("userName", authenticatedUser.getUsername());
					fields.put("menuAdi", key);
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<UserMenuItemTime> newList = pdksEntityController.execSPList(fields, sb, UserMenuItemTime.class);
					if (!newList.isEmpty())
						menuItemTime = newList.get(0);
				}

				if (menuItemTime != null) {
					menuItemTime = veriler.get(0);
					if (menuItemTime.getParametreJSON() == null || !parametreJSON.equals(menuItemTime.getParametreJSON())) {
						menuItemTime.setParametreJSON(parametreJSON);
						HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
						if (mySession != null)
							menuItemTime.setSessionId(mySession.getId());
						menuItemTime.setLastTime(new Date());
						pdksEntityController.saveOrUpdate(session, entityManager, menuItemTime);
						session.flush();
					}
					if (authenticatedUser != null && parametreJSON != null)
						authenticatedUser.setParametreJSON(parametreJSON);
					map1 = null;
					gson = null;
				}
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

		}

	}

	/**
	 * @param key
	 * @return
	 */
	public boolean getParameterKeyHasStringValue(String key) {
		String value = getParameterKey(key);
		boolean deger = PdksUtil.hasStringValue(value);
		return deger;
	}

	/**
	 * @param session
	 * @throws Exception
	 */
	public void hakEdisIzinERPDBGuncelle(Session session) throws Exception {
		String parameterName = getParametreHakEdisIzinERPTableView();
		if (getParameterKeyHasStringValue(parameterName)) {
			List<IzinHakEdisERPDB> izinHakEdisERPDBList = getHakEdisIzinDBList(parameterName, session);
			if (izinHakEdisERPDBList != null) {
				TreeMap<String, IzinHakedis> map = new TreeMap<String, IzinHakedis>();
				HashMap<String, IzinHakedisDetay> detayMap = new HashMap<String, IzinHakedisDetay>();
				for (IzinHakEdisERPDB izinHakEdisERPDB : izinHakEdisERPDBList) {
					if (izinHakEdisERPDB != null) {
						String perNo = izinHakEdisERPDB.getPersonelNo();
						IzinHakedis hakedis = map.containsKey(perNo) ? map.get(perNo) : new IzinHakedis();
						if (hakedis.getKidemBaslangicTarihi() == null)
							hakedis.setKidemBaslangicTarihi(PdksUtil.convertToDateString(izinHakEdisERPDB.getKidemBaslangicTarihi(), PersonelERPDB.FORMAT_DATE));
						if (hakedis.getPersonelNo() == null) {
							hakedis.setPersonelNo(perNo);
							map.put(perNo, hakedis);
						}

						String key = izinHakEdisERPDB.getKey();
						IzinHakedisDetay hakedisDetay = detayMap.containsKey(key) ? detayMap.get(key) : null;
						if (hakedisDetay == null) {
							hakedisDetay = new IzinHakedisDetay();
							hakedisDetay.setHakEdisTarihi(PdksUtil.convertToDateString(izinHakEdisERPDB.getHakEdisTarihi(), PersonelERPDB.FORMAT_DATE));
							hakedisDetay.setIzinSuresi(izinHakEdisERPDB.getHakEdisGunSayisi());
							hakedisDetay.setKidemYil(izinHakEdisERPDB.getKidemYili());
							hakedis.getHakedisList().add(hakedisDetay);
							detayMap.put(key, hakedisDetay);
						}
						if (izinHakEdisERPDB.getIzinBaslangicZamani() != null)
							hakedisDetay.getKullanilanIzinler().add(izinHakEdisERPDB.getIzinERP());

					}

				}
				detayMap = null;
				try {
					if (!map.isEmpty()) {
						PdksSoapVeriAktar service = getPdksSoapVeriAktar();
						if (service != null)
							service.saveIzinHakedisler(new ArrayList<IzinHakedis>(map.values()));
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				map = null;

			}

		}
	}

	/**
	 * @param guncellemeDurum
	 * @param perNoList
	 * @param parameterName
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public List<IzinHakEdisERPDB> getHakEdisIzinDBList(String parameterName, Session session) throws Exception {
		String hakEdisIzinERPTableViewAdi = getParameterKey(parameterName);
		List<Tanim> list = getTanimList(Tanim.TIPI_ERP_HAKEDIS_DB, session);
		List<IzinHakEdisERPDB> izinHakEdisERPDBList = null;
		if (!list.isEmpty()) {
			HashMap parametreMap = new HashMap();
			StringBuffer sb = new StringBuffer("WITH VERILER AS ( ");
			sb.append("SELECT ");
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Tanim tanim = (Tanim) iterator.next();
				String erpAlan = (PdksUtil.hasStringValue(tanim.getErpKodu()) ? tanim.getErpKodu() : "null");
				sb.append((erpAlan.equalsIgnoreCase(tanim.getKodu()) ? "" : erpAlan + " AS ") + "" + tanim.getKodu());
				if (iterator.hasNext())
					sb.append(", ");
			}
			sb.append(" FROM " + hakEdisIzinERPTableViewAdi + " WITH(nolock) ");
			sb.append(" ) ");
			sb.append(" SELECT V.* FROM VERILER V ");
			sb.append(" ORDER BY V." + IzinHakEdisERPDB.COLUMN_NAME_PERSONEL_NO + ", V." + IzinHakEdisERPDB.COLUMN_NAME_KIDEM_YIL + ", V." + IzinHakEdisERPDB.COLUMN_NAME_ID);

			try {
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				izinHakEdisERPDBList = pdksEntityController.getObjectBySQLList(sb, parametreMap, IzinHakEdisERPDB.class);

			} catch (Exception ex1) {
				loggerErrorYaz(null, ex1);
			}

		}
		return izinHakEdisERPDBList;
	}

	/**
	 * @param guncellemeDurum
	 * @param perNoList
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public List<PersonelERP> personelERPDBGuncelle(boolean guncellemeDurum, List<String> perNoList, Session session) throws Exception {
		List<PersonelERP> personelERPReturnList = null;
		String parameterName = getParametrePersonelERPTableView();
		if (getParameterKeyHasStringValue(parameterName)) {
			List<PersonelERPDB> personelList = getPersonelERPDBList(guncellemeDurum, perNoList, parameterName, session);
			if (personelList != null && !personelList.isEmpty()) {
				List<PersonelERP> personelERPList = new ArrayList<PersonelERP>();
				for (PersonelERPDB personelERPDB : personelList)
					personelERPList.add(personelERPDB.getPersonelERP());
				try {
					PdksSoapVeriAktar service = getPdksSoapVeriAktar();
					personelERPReturnList = service.savePersoneller(personelERPList);
					Parameter parameter = getParameter(session, parameterName);
					if (parameter != null) {
						parameter.setChangeDate(new Date());
						pdksEntityController.saveOrUpdate(session, entityManager, parameter);
						session.flush();
					}

				} catch (Exception ex) {
					loggerErrorYaz(null, ex);
				}
				if (authenticatedUser != null) {
					if (personelERPReturnList != null) {
						for (Iterator iterator = personelERPReturnList.iterator(); iterator.hasNext();) {
							PersonelERP personelERP = (PersonelERP) iterator.next();
							if (personelERP.getYazildi() != null && personelERP.getYazildi().booleanValue())
								iterator.remove();
							else if (perNoList == null) {
								for (String message : personelERP.getHataList()) {
									PdksUtil.addMessageWarn(message);
								}
							}
						}
					}
				} else
					personelERPReturnList = null;
				personelERPList = null;
			}
			personelList = null;

		}
		return personelERPReturnList;
	}

	/**
	 * @param guncellemeDurum
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public List<IzinERP> izinERPDBGuncelle(boolean guncellemeDurum, Session session) throws Exception {
		List<IzinERP> izinERPReturnList = null;
		String parameterName = getParametreIzinERPTableView();
		if (getParameterKeyHasStringValue(parameterName)) {
			List<IzinERPDB> izinList = getIzinERPDBList(guncellemeDurum, parameterName, session);
			if (izinList != null && !izinList.isEmpty()) {
				List<IzinERP> izinERPList = new ArrayList<IzinERP>();
				for (IzinERPDB izinERPDB : izinList) {
					IzinERP izinERP = izinERPDB.getIzinERP();
					izinERPList.add(izinERP);
				}
				try {
					PdksSoapVeriAktar service = getPdksSoapVeriAktar();
					izinERPReturnList = service.saveIzinler(izinERPList);
					Parameter parameter = getParameter(session, parameterName);
					if (parameter != null) {
						parameter.setChangeDate(new Date());
						pdksEntityController.saveOrUpdate(session, entityManager, parameter);
						session.flush();
					}

				} catch (Exception ex) {
					loggerErrorYaz(null, ex);
				}
				if (authenticatedUser != null) {
					if (izinERPReturnList != null) {
						for (Iterator iterator = izinERPReturnList.iterator(); iterator.hasNext();) {
							IzinERP izinERP = (IzinERP) iterator.next();
							if (izinERP.getYazildi() != null && izinERP.getYazildi().booleanValue())
								iterator.remove();

						}

					}
				} else
					izinERPReturnList = null;
				izinERPList = null;
			}
			izinList = null;

		}
		return izinERPReturnList;
	}

	/**
	 * @param guncellemeDurum
	 * @param parameterName
	 * @param session
	 * @return
	 * @throws Exception
	 */
	private List<IzinERPDB> getIzinERPDBList(boolean guncellemeDurum, String parameterName, Session session) throws Exception {
		String izinERPTableViewAdi = getParameterKey(parameterName);
		List<Tanim> list = getTanimList(Tanim.TIPI_ERP_IZIN_DB, session);
		List<IzinERPDB> izinList = null;
		Parameter parameter = null;
		if (!list.isEmpty()) {
			HashMap parametreMap = new HashMap();
			StringBuffer sb = new StringBuffer("SELECT ");
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Tanim tanim = (Tanim) iterator.next();
				String erpAlan = (PdksUtil.hasStringValue(tanim.getErpKodu()) ? tanim.getErpKodu() : "null");
				sb.append((erpAlan.equalsIgnoreCase(tanim.getKodu()) ? "" : erpAlan + " AS ") + "" + tanim.getKodu());
				if (iterator.hasNext())
					sb.append(", ");
			}
			sb.append(" FROM " + izinERPTableViewAdi + " WITH(nolock) ");

			parameter = getParameter(session, parameterName);
			Date tarih = parameter.getChangeDate();
			if (tarih != null) {
				if (guncellemeDurum == false)
					tarih = PdksUtil.tariheAyEkleCikar(PdksUtil.getDate(tarih), -5);
				else {
					tarih = getERPManuelTarih(tarih, null);
				}
				if (guncellemeDurum == false)
					sb.append(" WHERE " + IzinERPDB.COLUMN_NAME_BIT_TARIHI + " >=:t ");
				else
					sb.append(" WHERE " + IzinERPDB.COLUMN_NAME_GUNCELLEME_TARIHI + " >=:t ");
				parametreMap.put("t", PdksUtil.getDate(tarih));
			}

			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			HashMap<String, IzinERPDB> iptalMap = new HashMap<String, IzinERPDB>();
			try {
				izinList = pdksEntityController.getObjectBySQLList(sb, parametreMap, IzinERPDB.class);
				for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
					IzinERPDB izinERPDB = (IzinERPDB) iterator.next();
					if (izinERPDB.getDurum().booleanValue() == false) {
						iptalMap.put(izinERPDB.getReferansNoERP(), izinERPDB);
						iterator.remove();
					}

				}
			} catch (Exception ex1) {
				loggerErrorYaz(null, ex1);
			}
			if (!iptalMap.isEmpty()) {
				List idList = new ArrayList(iptalMap.keySet());
				String fieldName = "id";
				parametreMap.clear();
				parametreMap.put(fieldName, idList);
				parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "id");
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				// List<String> referansNoList = pdksEntityController.getObjectByInnerObjectList(parametreMap, IzinReferansERP.class);
				List<String> referansNoList = getParamList(false, idList, fieldName, parametreMap, IzinReferansERP.class, session);
				for (String key : referansNoList) {
					izinList.add(iptalMap.get(key));
				}
				referansNoList = null;
			}
			iptalMap = null;

		}
		return izinList;
	}

	/**
	 * @param tarih
	 * @param key
	 * @return
	 */
	private Date getERPManuelTarih(Date tarih, String key) {
		int eksiGun = 0;
		if (!PdksUtil.hasStringValue(key))
			key = "erpVeriGuncelleGunAdet";
		String erpVeriGuncelleGunAdet = getParameterKey(key);
		if (PdksUtil.hasStringValue(erpVeriGuncelleGunAdet)) {
			try {
				eksiGun = Integer.parseInt(erpVeriGuncelleGunAdet);
			} catch (Exception e) {
				eksiGun = 0;
			}
		}
		if (eksiGun > 0)
			tarih = PdksUtil.tariheGunEkleCikar(PdksUtil.getDate(tarih), -eksiGun);
		return tarih;
	}

	/**
	 * @param guncellemeDurum
	 * @param perNoList
	 * @param parameterName
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public List<PersonelERPDB> getPersonelERPDBList(boolean guncellemeDurum, List<String> perNoList, String parameterName, Session session) throws Exception {
		String personelERPTableViewAdi = getParameterKey(parameterName);
		List<Tanim> list = getTanimList(Tanim.TIPI_ERP_PERSONEL_DB, session);
		List<PersonelERPDB> personelList = null;
		Parameter parameter = null;
		if (!list.isEmpty()) {
			HashMap parametreMap = new HashMap();
			StringBuffer sb = new StringBuffer("SELECT ");
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Tanim tanim = (Tanim) iterator.next();
				String erpAlan = (PdksUtil.hasStringValue(tanim.getErpKodu()) ? tanim.getErpKodu() : "null");
				sb.append((erpAlan.equalsIgnoreCase(tanim.getKodu()) ? "" : erpAlan + " AS ") + "" + tanim.getKodu());
				if (iterator.hasNext())
					sb.append(", ");
			}
			sb.append(" FROM " + personelERPTableViewAdi + " WITH(nolock) ");
			Date tarih = null;
			if (perNoList != null && !perNoList.isEmpty()) {
				sb.append(" WHERE " + PersonelERPDB.COLUMN_NAME_PERSONEL_NO + " :p ");
				parametreMap.put("p", perNoList);
			} else {
				parameter = getParameter(session, parameterName);
				tarih = parameter.getChangeDate();
				if (tarih != null) {
					if (guncellemeDurum == false) {
						tarih = PdksUtil.tariheAyEkleCikar(PdksUtil.getDate(tarih), -5);
						sb.append(" WHERE " + PersonelERPDB.COLUMN_NAME_ISTEN_AYRILMA_TARIHI + " >=:t ");
					} else {
						tarih = getERPManuelTarih(tarih, null);
						sb.append(" WHERE " + PersonelERPDB.COLUMN_NAME_GUNCELLEME_TARIHI + " >=:t ");
					}

					parametreMap.put("t", PdksUtil.getDate(tarih));
				}
			}
			TreeMap<String, PersonelERPDB> ayrilanMap = new TreeMap<String, PersonelERPDB>();
			try {
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				personelList = pdksEntityController.getObjectBySQLList(sb, parametreMap, PersonelERPDB.class);
				if (tarih == null)
					tarih = new Date();
				tarih = PdksUtil.getDate(PdksUtil.tariheGunEkleCikar(tarih, -1));
				if (perNoList == null || perNoList.isEmpty()) {
					for (Iterator iterator = personelList.iterator(); iterator.hasNext();) {
						PersonelERPDB personelERPDB = (PersonelERPDB) iterator.next();
						if (personelERPDB.getIstenAyrilmaTarihi() != null && personelERPDB.getIstenAyrilmaTarihi().before(tarih)) {
							ayrilanMap.put(personelERPDB.getPersonelNo(), personelERPDB);
							iterator.remove();
						}
					}
				}
			} catch (Exception ex1) {
				loggerErrorYaz(null, ex1);
			}
			if (!ayrilanMap.isEmpty()) {
				List idList = new ArrayList(ayrilanMap.keySet());
				String fieldName = "p";
				parametreMap.clear();
				sb = new StringBuffer();
				sb.append("SELECT PS." + PersonelKGS.COLUMN_NAME_SICIL_NO + " FROM " + PersonelKGS.TABLE_NAME + " PS WITH(nolock) ");
				sb.append(" INNER JOIN " + KapiSirket.TABLE_NAME + " K ON K." + KapiSirket.COLUMN_NAME_ID + " = PS." + PersonelKGS.COLUMN_NAME_KGS_SIRKET);
				sb.append(" AND K." + KapiSirket.COLUMN_NAME_DURUM + " = 1 AND K." + KapiSirket.COLUMN_NAME_BIT_TARIH + " > GETDATE()");
				sb.append(" WHERE PS." + PersonelKGS.COLUMN_NAME_SICIL_NO + " :" + fieldName);
				// sb.append(" AND PS." + PersonelKGS.COLUMN_NAME_DURUM + " = 1 ");
				HashMap fields = new HashMap();
				parametreMap.put(fieldName, idList);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				// List<String> sicilNoList = pdksEntityController.getObjectBySQLList(sb, parametreMap, null);
				List<String> sicilNoList = getSQLParamList(idList, sb, fieldName, parametreMap, null, session);
				for (String key : sicilNoList)
					personelList.add(ayrilanMap.get(key));

				sicilNoList = null;
			}
			ayrilanMap = null;

		}
		return personelList;
	}

	/**
	 * @param key
	 * @param aciklama
	 * @return
	 */
	public String getParametreAciklama(String key, String aciklama) {
		String parameterKey = null;
		try {
			parameterKey = parameterMap != null && parameterMap.containsKey(key) ? parameterMap.get(key).trim() : aciklama;
		} catch (Exception e) {
			parameterKey = "";
		}
		return parameterKey;

	}

	/**
	 * @param key
	 * @return
	 */
	public String getParameterKey(String key) {
		String parameterKey = null;
		try {
			parameterKey = parameterMap != null && parameterMap.containsKey(key) ? parameterMap.get(key).trim() : "";
		} catch (Exception e) {
			parameterKey = "";
		}
		return parameterKey;

	}

	/**
	 * @param userList
	 * @param addMailPersonelList
	 */
	public void addMailPersonelUserList(List<User> userList, List<MailPersonel> addMailPersonelList) {
		if (addMailPersonelList != null && userList != null) {
			for (User user : userList) {
				if (user.getId() != null && !user.isDurum())
					continue;
				boolean ekle = user.getPdksPersonel() == null || user.getPdksPersonel().isCalisiyor();
				for (Iterator iterator = addMailPersonelList.iterator(); iterator.hasNext();) {
					MailPersonel mailPersonel = (MailPersonel) iterator.next();
					try {
						if (mailPersonel.getEPosta().equals(user.getEmail())) {
							ekle = false;
							break;
						}
					} catch (Exception e) {
						ekle = false;
					}
				}
				if (ekle)
					addMailPersonelList.add(user.getMailPersonel());
			}
		}
	}

	/**
	 * @param userList
	 * @param addMailPersonelList
	 */
	public void addMailPersonelList(List<String> userList, List<MailPersonel> addMailPersonelList) {
		if (addMailPersonelList != null && userList != null) {
			for (String mail : userList) {
				if (mail.indexOf("@") > 0 && PdksUtil.isValidEMail(mail)) {
					boolean ekle = true;
					for (Iterator iterator = addMailPersonelList.iterator(); iterator.hasNext();) {
						MailPersonel mailPersonel = (MailPersonel) iterator.next();
						try {
							if (mailPersonel.getEPosta().equals(mail)) {
								ekle = false;
								break;
							}
						} catch (Exception e) {
							ekle = false;
						}
					}
					if (ekle) {
						MailPersonel mailPersonel = new MailPersonel();
						mailPersonel.setEPosta(mail);
						addMailPersonelList.add(mailPersonel);
					}
				}
			}
		}
	}

	/**
	 * @param list
	 * @param tesisId
	 * @param veriAyrac
	 * @return
	 */
	public LinkedHashMap<String, Object> getListPersonelOzetVeriMap(List list, Long tesisId, String veriAyrac) {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		if (list != null) {
			HashMap<Long, Sirket> sirketMap = new HashMap<Long, Sirket>();
			HashMap<Long, Tanim> tesisMap = new HashMap<Long, Tanim>(), bolumMap = new HashMap<Long, Tanim>(), altBolumMap = new HashMap<Long, Tanim>();
			for (Object object : list) {
				if (object == null)
					continue;
				Personel personel = null;
				if (object instanceof AylikPuantaj)
					personel = ((AylikPuantaj) object).getPdksPersonel();
				else if (object instanceof PersonelDenklestirme)
					personel = ((PersonelDenklestirme) object).getPersonel();
				else if (object instanceof VardiyaGun)
					personel = ((VardiyaGun) object).getPersonel();
				else if (object instanceof PersonelIzin)
					personel = ((PersonelIzin) object).getIzinSahibi();
				else if (object instanceof Personel)
					personel = (Personel) object;
				if (personel != null) {
					if (personel.getSirket() != null && personel.getSirket().getId() != null)
						sirketMap.put(personel.getSirket().getId(), personel.getSirket());
					if (personel.getEkSaha3() != null && personel.getEkSaha3().getId() != null)
						bolumMap.put(personel.getEkSaha3().getId(), personel.getEkSaha3());
					if (personel.getEkSaha4() != null && personel.getEkSaha4().getId() != null)
						altBolumMap.put(personel.getEkSaha4().getId(), personel.getEkSaha4());
					if (tesisId != null && personel.getTesis() != null && personel.getTesis().getId() != null)
						tesisMap.put(personel.getTesis().getId(), personel.getTesis());

				}
			}
			Sirket sirket = null;
			Tanim tesis = null, bolum = null, altBolum = null, sirketGrup = null;
			String ayrac = " ";
			if (!sirketMap.isEmpty()) {
				List<Sirket> tempList = new ArrayList<Sirket>(sirketMap.values());
				for (Sirket sirket2 : tempList) {
					if (!sirket2.isTesisDurumu())
						tesisMap.clear();
					if (sirket2.getSirketGrup() != null)
						sirketGrup = sirket2.getSirketGrup();
				}
				if (tempList.size() == 1) {
					sirket = tempList.get(0);
					map.put("sirket", sirket);
				} else if (sirketGrup != null) {
					map.put("sirketGrup", sirketGrup);
				}
				tempList = null;
			}
			if (tesisMap.size() == 1) {
				List<Tanim> tempList = new ArrayList<Tanim>(tesisMap.values());
				tesis = tempList.get(0);
				map.put("tesis", tesis);
				tempList = null;
			}
			if (bolumMap.size() == 1) {
				List<Tanim> tempList = new ArrayList<Tanim>(bolumMap.values());
				bolum = tempList.get(0);
				map.put("bolum", bolum);
				tempList = null;
			}
			if (altBolumMap.size() == 1) {
				List<Tanim> tempList = new ArrayList<Tanim>(altBolumMap.values());
				altBolum = tempList.get(0);
				map.put("altBolum", altBolum);
				tempList = null;
			}
			if (!map.isEmpty()) {
				if (PdksUtil.hasStringValue(veriAyrac))
					veriAyrac = " ";
				ayrac = " ";
				StringBuffer sb = new StringBuffer();
				if (sirket != null) {
					sb.append(sirket.getAd());
					ayrac = veriAyrac;
				} else if (sirketGrup != null) {
					sb.append(sirketGrup.getAciklama());
					ayrac = veriAyrac;
				}
				if (tesis != null) {
					sb.append(ayrac + tesis.getAciklama());
					ayrac = veriAyrac;
				}
				if (bolum != null) {
					sb.append(ayrac + bolum.getAciklama());
					ayrac = veriAyrac;
				}
				map.put("aciklama", sb.toString());
				sb = null;
			}
			sirketMap = null;
			tesisMap = null;
			bolumMap = null;
		}
		return map;
	}

	/**
	 * @param temizleTOCCList
	 * @param mailObject
	 * @param rd
	 * @param sayfaAdi
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public MailStatu mailSoapServisGonder(boolean temizleTOCCList, MailObject mailObject, Renderer rd, String sayfaAdi, Session session) throws Exception {
		String servisMailGonderKey = getParameterKey("servisMailGonder");
		boolean servisMailGonder = PdksUtil.hasStringValue(servisMailGonderKey);
		MailStatu mailStatu = null;
		String bccAdres = getParameterKey("bccAdres");
		if (mailObject != null) {
			List<String> list = new ArrayList<String>();
			LinkedHashMap<String, List<MailPersonel>> mailMap = new LinkedHashMap<String, List<MailPersonel>>();
			mailMap.put("to", mailObject.getToList());
			mailMap.put("cc", mailObject.getCcList());
			mailMap.put("bcc", mailObject.getBccList());
			List<String> bbcList = bccAdres.indexOf("@") > 1 ? PdksUtil.getListFromString(bccAdres, null) : new ArrayList<String>();
			for (String ePosta : bbcList) {
				if (list.contains(ePosta))
					continue;
				MailPersonel mailPersonel = new MailPersonel();
				mailPersonel.setEPosta(ePosta);
				mailObject.getBccList().add(mailPersonel);
			}
			for (String key : mailMap.keySet()) {
				List<MailPersonel> mailList = mailMap.get(key);
				for (Iterator iterator = mailList.iterator(); iterator.hasNext();) {
					MailPersonel mailPersonel = (MailPersonel) iterator.next();
					if (!list.contains(mailPersonel.getEPosta()))
						list.add(mailPersonel.getEPosta());
					else
						iterator.remove();
				}
			}
			TreeMap<String, User> userMap = getUserRoller(null, list, session);
			for (String key : mailMap.keySet()) {
				List<MailPersonel> mailList = mailMap.get(key);
				for (Iterator iterator = mailList.iterator(); iterator.hasNext();) {
					MailPersonel mailPersonel = (MailPersonel) iterator.next();
					String ePosta = mailPersonel.getEPosta();
					if (!list.contains(ePosta))
						iterator.remove();
					else if (PdksUtil.hasStringValue(mailPersonel.getAdiSoyadi()) == false) {
						if (userMap.containsKey(ePosta))
							mailPersonel.setAdiSoyadi(userMap.get(ePosta).getAdSoyad());
					}

				}
			}
			mailMap = null;
			list = null;
			bbcList = null;
		}
		if (servisMailGonder) {
			if (temizleTOCCList && authenticatedUser != null && authenticatedUser.isAdmin()) {
				mailObject.getToList().clear();
				mailObject.getCcList().clear();
			}
			mailObject.setSmtpUser(getParameterKey("smtpUserName"));
			mailObject.setSmtpPassword(getParameterKey("smtpPassword"));
			try {
				mailStatu = mailManager.mailleriDuzenle(mailObject, session);
				if (mailStatu.getDurum())
					mailStatu = mailManager.ePostaGonder(mailObject, session);
			} catch (Exception e) {
				logger.error(e);
				try {
					PdksSoapVeriAktar pdksSoapVeriAktar = getPdksSoapVeriAktar();
					mailStatu = pdksSoapVeriAktar.sendMail(mailObject);

				} catch (Exception e2) {
					servisMailGonder = false;
					logger.error(e);
				}

			}
		}
		if (!servisMailGonder) {
			if (rd != null) {
				mailGonder(rd, sayfaAdi);
				if (mailStatu == null) {
					mailStatu = new MailStatu();
					mailStatu.setDurum(true);
					mailStatu.setHataMesai("");
				}
			}
		}
		if (mailStatu.getDurum() == false && mailStatu.getHataMesai() == null)
			mailStatu.setHataMesai("Mail gönderiminde hata oluştu");

		return mailStatu;
	}

	/**
	 * @param rd
	 * @param sayfaAdi
	 * @return
	 * @throws Exception
	 */
	public String mailGonder(Renderer rd, String sayfaAdi) throws Exception {
		String str = null;
		try {
			if (sayfaAdi != null && rd != null)
				str = rd.render(sayfaAdi);
		} catch (Exception e) {
			str = "Mail sayfaAdi : " + sayfaAdi + " " + e.getMessage();
			logger.error("Mail sayfaAdi : " + sayfaAdi + " " + e);
			e.printStackTrace();
		}
		return str;
	}

	/**
	 * @param mail
	 * @return
	 * @throws Exception
	 */
	public MailStatu mailGonder(MailObject mail) throws Exception {
		MailStatu mailStatu = null;
		String adres = getParameterKey("pdksWebService");
		if (!PdksUtil.hasStringValue(adres))
			adres = "http://localhost:9080/PdksWebService";

		String url = adres + "/rest/services/sentMail";
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String gsonStr = gson.toJson(mail);
		String strGson = null;
		try {
			strGson = getJSONData(url, HttpMethod.POST, gsonStr, null, true);
		} catch (Exception e) {
			strGson = null;
			mailStatu = new MailStatu();
			if (e.getMessage() != null)
				mailStatu.setHataMesai(e.getMessage());
			else
				mailStatu.setHataMesai("Hata oluştu!");
			logger.error(e);
			e.printStackTrace();
		}

		if (strGson != null)
			mailStatu = gson.fromJson(strGson, MailStatu.class);

		return mailStatu;
	}

	/**
	 * @param logGoster
	 * @param path
	 * @param method
	 * @param headerMap
	 * @param hataKodu
	 * @param contentType
	 * @return
	 * @throws Exception
	 */
	public String getURLJSONData(Boolean logGoster, String path, String method, LinkedHashMap<String, String> headerMap, boolean hataKodu, String contentType) throws Exception {
		LinkedHashMap<String, Object> jsonMap = new LinkedHashMap<String, Object>();
		String pattern = PdksUtil.getDateTimeLongFormat();
		jsonMap.put("path", path);
		jsonMap.put("httpMethod", method);

		jsonMap.put("headers", headerMap);
		String isim = "";
		if (authenticatedUser != null)
			jsonMap.put("kullanici", authenticatedUser.getAdSoyad());
		StringBuffer sb = null;
		Integer responseCode = null;
		try {
			if (path.lastIndexOf("//") > 5) {
				path = PdksUtil.replaceAll(path, "://", "|||");
				path = PdksUtil.replaceAll(path, "//", "/");
				path = PdksUtil.replaceAll(path, "|||", "://");
			}

			SSLImport.getCertificateInputStream(path);
			sb = new StringBuffer();

			URL url = new URL(path);
			if (logGoster)
				logger.info(isim + " " + path + " in " + PdksUtil.convertToDateString(new Date(), pattern));
			HttpURLConnection connjava = (HttpURLConnection) url.openConnection();

			if (contentType == null)
				contentType = "application/json";
			connjava.setRequestProperty("Content-Type", contentType + "; charset=UTF-8");
			// connjava.setRequestProperty("Content-Language", "tr-TR");
			connjava.setRequestMethod(method);
			connjava.setDoInput(true);
			connjava.setDoOutput(true);
			connjava.setUseCaches(false);
			int timeOutSaniye = 15;
			connjava.setReadTimeout(2 * timeOutSaniye * 1000);
			connjava.setConnectTimeout(timeOutSaniye * 1000); // set timeout to 5 seconds
			if (headerMap != null) {
				for (String key : headerMap.keySet())
					connjava.setRequestProperty(key, headerMap.get(key));

			}
			connjava.setAllowUserInteraction(true);

			responseCode = ((HttpURLConnection) connjava).getResponseCode();

			InputStream is = responseCode >= 400 ? null : connjava.getInputStream();
			if (is != null) {
				sb.append(PdksUtil.StringToByInputStream(is));
				// sb.append(org.apache.commons.io.IOUtils.toString(is, "utf-8"));

			} else {
				sb.append("responseCode : " + responseCode + "\n" + path);
				if (headerMap != null) {
					for (String key : headerMap.keySet()) {
						sb.append("\n" + key + " = " + headerMap.get(key));
					}
				}

			}

		} catch (Exception ex) {
			logger.error(ex);
		}
		String str = sb.toString();
		if (logGoster)
			logger.info(isim + " " + path + " out " + PdksUtil.convertToDateString(new Date(), pattern));
		sb = null;
		return str;

	}

	/**
	 * @param path
	 * @param httpMethod
	 * @param jsonObject
	 * @param headerMap
	 * @param hataKodu
	 * @return
	 * @throws Exception
	 */
	public String getJSONData(String path, String httpMethod, Object jsonObject, LinkedHashMap<String, String> headerMap, boolean hataKodu) throws Exception {
		StringBuffer sb = new StringBuffer();
		LinkedHashMap<String, Object> jsonMap = new LinkedHashMap<String, Object>();
		jsonMap.put("path", path);
		jsonMap.put("httpMethod", httpMethod);
		jsonMap.put("params", jsonObject);
		jsonMap.put("headers", headerMap);
		Gson gson = new Gson();
		Integer responseCode = null;
		if (authenticatedUser != null) {
			jsonMap.put("kullanici", authenticatedUser.getAdSoyad());
		}
		try {
			if (path.lastIndexOf("//") > 5) {
				path = PdksUtil.replaceAll(path, "://", "|||");
				path = PdksUtil.replaceAll(path, "//", "/");
				path = PdksUtil.replaceAll(path, "|||", "://");
			}

			SSLImport.getCertificateInputStream(path);
			java.net.URL url = new java.net.URL(path);
			java.net.HttpURLConnection connjava = (java.net.HttpURLConnection) url.openConnection();
			connjava.setRequestMethod(httpMethod);
			connjava.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8");
			connjava.setRequestProperty("Content-Language", "tr-TR");
			connjava.setDoInput(true);
			connjava.setDoOutput(true);
			connjava.setUseCaches(false);
			int timeOutSaniye = 15;
			connjava.setReadTimeout(2 * timeOutSaniye * 1000);
			connjava.setConnectTimeout(timeOutSaniye * 1000);
			if (headerMap != null) {
				for (String key : headerMap.keySet())
					connjava.setRequestProperty(key, headerMap.get(key));
			}
			connjava.setAllowUserInteraction(true);
			DataOutputStream printout = new DataOutputStream(connjava.getOutputStream());
			String jsonData = null;
			if (jsonObject != null) {

				if (jsonObject instanceof String)
					jsonData = (String) jsonObject;
				else {

					try {
						jsonData = gson.toJson(jsonObject);
					} catch (Exception e) {
						jsonData = "{}";
					}
				}
			}
			if (jsonData != null)
				printout.writeBytes(jsonData);
			printout.flush();
			printout.close();
			responseCode = connjava.getResponseCode();
			InputStream is = responseCode >= 400 ? (hataKodu ? connjava.getErrorStream() : null) : connjava.getInputStream();
			if (is != null) {
				sb.append(PdksUtil.StringToByInputStream(is));
				// sb.append(PdksUtil.StringToByInputStreamIOUtils(is));
				jsonMap.put("response", jsonData);
			}

		} catch (Exception ex) {
			logger.error(ex);
			ex.printStackTrace();
		}
		String str = sb.toString();
		sb = null;
		return str;
	}

	/**
	 * @param aramaSecenekleri
	 * @param ikinciYonetici
	 * @param session
	 * @return
	 */
	public ArrayList<String> getAramaPersonelSicilNo(AramaSecenekleri aramaSecenekleri, boolean ikinciYonetici, Session session) {
		String ad = aramaSecenekleri.getAd(), soyad = aramaSecenekleri.getSoyad(), sicilNo = aramaSecenekleri.getSicilNo();
		Sirket sirket = aramaSecenekleri.getSirketId() != null ? new Sirket(aramaSecenekleri.getSirketId()) : null;
		Tanim seciliEkSaha1 = aramaSecenekleri.getEkSaha1Id() != null ? new Tanim(aramaSecenekleri.getEkSaha1Id()) : null;
		Tanim seciliEkSaha2 = aramaSecenekleri.getEkSaha2Id() != null ? new Tanim(aramaSecenekleri.getEkSaha2Id()) : null;
		Tanim seciliEkSaha3 = aramaSecenekleri.getEkSaha3Id() != null ? new Tanim(aramaSecenekleri.getEkSaha3Id()) : null;
		Tanim seciliEkSaha4 = aramaSecenekleri.getEkSaha4Id() != null ? new Tanim(aramaSecenekleri.getEkSaha4Id()) : null;
		Tanim seciliTesis = aramaSecenekleri.getTesisId() != null ? new Tanim(aramaSecenekleri.getTesisId()) : null;
		if (aramaSecenekleri.getSessionClear())
			session.clear();
		ArrayList<String> perNo = getPersonelSicilNo(ad, soyad, sicilNo, sirket, seciliTesis, seciliEkSaha1, seciliEkSaha2, seciliEkSaha3, seciliEkSaha4, ikinciYonetici, Boolean.FALSE, session);
		return perNo;
	}

	/**
	 * @param ad
	 * @param soyad
	 * @param sicilNo
	 * @param sirket
	 * @param tesis
	 * @param ekSaha1
	 * @param ekSaha2
	 * @param ekSaha3
	 * @param ekSaha4
	 * @param ikinciYonetici
	 * @param session
	 * @return
	 */
	public ArrayList<String> getPersonelSicilNo(String ad, String soyad, String sicilNo, Sirket sirket, Tanim tesis, Tanim ekSaha1, Tanim ekSaha2, Tanim ekSaha3, Tanim ekSaha4, boolean ikinciYonetici, Session session) {
		ArrayList<String> perNo = getPersonelSicilNo(ad, soyad, sicilNo, sirket, tesis, ekSaha1, ekSaha2, ekSaha3, ekSaha4, ikinciYonetici, Boolean.FALSE, session);
		return perNo;
	}

	/**
	 * @param key
	 * @param defaultBaslik
	 * @return
	 */
	private String getBaslikAciklama(String key, String defaultBaslik) {
		String aciklama = getParameterKey(key);
		if (!PdksUtil.hasStringValue(aciklama))
			aciklama = defaultBaslik;
		return aciklama;
	}

	/**
	 * @return
	 */
	public String devredenMesaiKod() {
		String kod = getBaslikAciklama("devredenMesaiKod", "DM");
		return kod;
	}

	/**
	 * @return
	 */
	public String kismiOdemeKod() {
		String kod = getBaslikAciklama("kismiOdemeKod", "KOM");
		return kod;
	}

	/**
	 * @return
	 */
	public String ucretiOdenenKod() {
		String kod = getBaslikAciklama("ucretiOdenenKod", "UOM");
		return kod;
	}

	/**
	 * @return
	 */
	public String gerceklesenMesaiKod() {
		String kod = getBaslikAciklama("gerceklesenMesaiKod", "GM");
		return kod;
	}

	/**
	 * @return
	 */
	public String devredenBakiyeKod() {
		String kod = getBaslikAciklama("devredenBakiyeKod", "B");
		return kod;
	}

	/**
	 * @return
	 */
	public String yasalFazlaCalismaAsanSaatKod() {
		String kod = getBaslikAciklama("yasalFazlaCalismaAsanSaatKod", "FCA");
		return kod;
	}

	/**
	 * @return
	 */
	public String normalCalismaSaatKod() {
		String kod = getBaslikAciklama("normalCalismaSaatKod", "NMC");
		return kod;
	}

	/**
	 * @return
	 */
	public String normalCalismaGunKod() {
		String kod = normalCalismaSaatKod() + "G";
		return kod;
	}

	/**
	 * @return
	 */
	public String haftaTatilCalismaSaatKod() {
		String kod = getBaslikAciklama("haftaTatilCalismaSaatKod", "HTC");
		return kod;
	}

	/**
	 * @return
	 */
	public String haftaTatilCalismaGunKod() {
		String kod = haftaTatilCalismaSaatKod() + "G";
		return kod;
	}

	/**
	 * @return
	 */
	public String resmiTatilCalismaSaatKod() {
		String kod = getBaslikAciklama("resmiTatilCalismaSaatKod", "RTC");
		return kod;
	}

	/**
	 * @return
	 */
	public String resmiTatilCalismaGunKod() {
		String kod = resmiTatilCalismaSaatKod() + "G";
		return kod;
	}

	/**
	 * @return
	 */
	public String izinSureSaatKod() {
		String kod = getBaslikAciklama("izinSureSaatKod", "IZNS");
		return kod;
	}

	/**
	 * @return
	 */
	public String izinSureGunKod() {
		String kod = izinSureSaatKod() + "G";
		return kod;
	}

	/**
	 * @return
	 */
	public String izinSureGunAdetKod() {
		String kod = getBaslikAciklama("izinSureGunAdetKod", "IZGA");
		return kod;
	}

	/**
	 * @return
	 */
	public String ucretliIzinGunKod() {
		String kod = getBaslikAciklama("ucretliIzinGunKod", "ULIG");
		return kod;
	}

	/**
	 * @return
	 */
	public String ucretsizIzinGunKod() {
		String kod = getBaslikAciklama("ucretsizIzinGunKod", "USIZG");
		return kod;
	}

	/**
	 * @return
	 */
	public String hastalikIzinGunKod() {
		String kod = getBaslikAciklama("hastalikIzinGunKod", "HASIZG");
		return kod;
	}

	/**
	 * @return
	 */
	public String normalGunKod() {
		String kod = getBaslikAciklama("normalGunKod", "NG");
		return kod;
	}

	/**
	 * @return
	 */
	public String haftaTatilGunKod() {
		String kod = getBaslikAciklama("haftaTatilGunKod", "HG");
		return kod;
	}

	/**
	 * @return
	 */
	public String artikGunKod() {
		String kod = getBaslikAciklama("artikGunKod", "AG");
		return kod;
	}

	/**
	 * @return
	 */
	public String resmiTatilGunKod() {
		String kod = getBaslikAciklama("resmiTatilGunKod", "RG");
		return kod;
	}

	/**
	 * @return
	 */
	public String bordroToplamGunKod() {
		String kod = getBaslikAciklama("bordroToplamGunKod", "TG");
		return kod;
	}

	/**
	 * @return
	 */
	public String personelNoAciklama() {
		String personelNoAciklama = getBaslikAciklama("personelNoAciklama", "Personel No");
		return personelNoAciklama;
	}

	/**
	 * @return
	 */
	public String calismaModeliAciklama() {
		String calismaModeliAciklama = getBaslikAciklama("calismaModeliAciklama", "Çalışma Modeli");
		return calismaModeliAciklama;
	}

	/**
	 * @return
	 */
	public String personelTipiAciklama() {
		String personelTipiAciklama = getBaslikAciklama("personelTipiAciklama", "Personel Tipi");
		return personelTipiAciklama;
	}

	/**
	 * @return
	 */
	public String eksikCalismaAciklama() {
		String eksikCalismaAciklama = getBaslikAciklama("eksikCalismaAciklama", "Maaş Kesinti");
		return eksikCalismaAciklama;
	}

	/**
	 * @return
	 */
	public String kimlikNoAciklama() {
		String kimlikNoAciklama = getBaslikAciklama("kimlikNoAciklama", "Kimlik No");
		return kimlikNoAciklama;
	}

	/**
	 * @return
	 */
	public String sanalPersonelAciklama() {
		String sanalPersonelAciklama = getBaslikAciklama("sanalPersonelAciklama", "");
		return sanalPersonelAciklama;
	}

	/**
	 * @return
	 */
	public String yoneticiAciklama() {
		String yoneticiAciklama = getBaslikAciklama("yoneticiAciklama", "Yönetici");
		return yoneticiAciklama;
	}

	/**
	 * @return
	 */
	public String yonetici2Aciklama() {
		String yonetici2Aciklama = getBaslikAciklama("yonetici2Aciklama", "2. Yönetici");
		return yonetici2Aciklama;
	}

	/**
	 * @return
	 */
	public String tesisAciklama() {
		String tesisAciklama = getBaslikAciklama("tesisAciklama", "Tesis");
		return tesisAciklama;
	}

	/**
	 * @return
	 */
	public String bolumAciklama() {
		String bolumAciklama = getBaslikAciklama("bolumAciklama", "Bölüm");
		return bolumAciklama;
	}

	/**
	 * @return
	 */
	public Boolean getTesisDurumu() {
		String tesisDurumuStr = getParameterKey("tesisDurumu");
		boolean tesisDurumu = tesisDurumuStr.equals("1");
		if (!tesisDurumu && pdksSirketleri != null) {
			for (Sirket sirket : pdksSirketleri) {
				if (sirket.isPdksMi() && sirket.isTesisDurumu()) {
					tesisDurumu = true;
					break;
				}
			}
		}
		return tesisDurumu;
	}

	/**
	 * @return
	 */
	public String sirketAciklama() {
		String sirketAciklama = getBaslikAciklama("sirketAciklama", "Şirket");
		return sirketAciklama;
	}

	/**
	 * @return
	 */
	public String kidemBasTarihiAciklama() {
		String kidemBasTarihiAciklama = getBaslikAciklama("kidemBasTarihiAciklama", "Kıdem Başlangıç Tarihi");
		return kidemBasTarihiAciklama;
	}

	/**
	 * @param sicilNo
	 * @return
	 */
	public String getSicilNo(String sicilNo) {
		int maxTextLength = 0;
		if (PdksUtil.getSicilNoUzunluk() != null) {
			try {
				maxTextLength = PdksUtil.getSicilNoUzunluk();
			} catch (Exception e) {
				maxTextLength = 0;
			}
		}
		if (sicilNo != null)
			sicilNo = sicilNo.trim();
		if (maxTextLength > 0 && PdksUtil.hasStringValue(sicilNo) && sicilNo.length() < maxTextLength)
			sicilNo = PdksUtil.textBaslangicinaKarakterEkle(sicilNo, '0', maxTextLength);

		return sicilNo;

	}

	/**
	 * @param aramaSecenekleri
	 * @param ikinciYonetici
	 * @param istenAyrilanEkleDurum
	 * @param session
	 * @return
	 */
	public ArrayList<String> getAramaPersonelSicilNo(AramaSecenekleri aramaSecenekleri, boolean ikinciYonetici, boolean istenAyrilanEkleDurum, Session session) {

		String ad = aramaSecenekleri.getAd(), soyad = aramaSecenekleri.getSoyad(), sicilNo = aramaSecenekleri.getSicilNo();

		if (sicilNo != null) {
			aramaSecenekleri.setSicilNo(sicilNo);
		}

		Sirket sirket = aramaSecenekleri.getSirketId() != null ? new Sirket(aramaSecenekleri.getSirketId()) : null;
		Tanim seciliEkSaha1 = aramaSecenekleri.getEkSaha1Id() != null ? new Tanim(aramaSecenekleri.getEkSaha1Id()) : null;
		Tanim seciliEkSaha2 = aramaSecenekleri.getEkSaha2Id() != null ? new Tanim(aramaSecenekleri.getEkSaha2Id()) : null;
		Tanim seciliEkSaha3 = aramaSecenekleri.getEkSaha3Id() != null ? new Tanim(aramaSecenekleri.getEkSaha3Id()) : null;
		Tanim seciliEkSaha4 = aramaSecenekleri.getEkSaha4Id() != null ? new Tanim(aramaSecenekleri.getEkSaha4Id()) : null;
		Tanim seciliTesis = aramaSecenekleri.getTesisId() != null ? new Tanim(aramaSecenekleri.getTesisId()) : null;
		if (aramaSecenekleri.getSessionClear())
			session.clear();
		ArrayList<String> sicilNoList = getPersonelSicilNo(ad, soyad, sicilNo, sirket, seciliTesis, seciliEkSaha1, seciliEkSaha2, seciliEkSaha3, seciliEkSaha4, ikinciYonetici, istenAyrilanEkleDurum, session);

		return sicilNoList;
	}

	/**
	 * @param ad
	 * @param soyad
	 * @param sicilNo
	 * @param gelenSirket
	 * @param tesis
	 * @param ekSaha1
	 * @param ekSaha2
	 * @param ekSaha3
	 * @param ekSaha4
	 * @param ikinciYonetici
	 * @param istenAyrilanEkleDurum
	 * @param session
	 * @return
	 */
	public ArrayList<String> getPersonelSicilNo(String ad, String soyad, String sicilNo, Sirket gelenSirket, Tanim tesis, Tanim ekSaha1, Tanim ekSaha2, Tanim ekSaha3, Tanim ekSaha4, boolean ikinciYonetici, boolean istenAyrilanEkleDurum, Session session) {
		Sirket sirket = authenticatedUser.isYonetici() ? null : gelenSirket;
		ArrayList<String> perNoList = getYetkiTumPersonelNoListesi(authenticatedUser);
		boolean istenAyrilanEkle = istenAyrilanEkleDurum && (sirket != null || gelenSirket != null);
		boolean hata = istenAyrilanEkle != istenAyrilanEkleDurum;
		Departman departman = sirket != null ? sirket.getDepartman() : null;
		Date bugun = PdksUtil.buGun();
		String mySicilNo = null;

		if (istenAyrilanEkle == false && authenticatedUser.isYoneticiKontratli())
			digerPersoneller(null, perNoList, bugun, bugun, session);
		try {
			mySicilNo = authenticatedUser.getPdksPersonel().getPdksSicilNo();
			if (mySicilNo != null && perNoList != null && !perNoList.contains(mySicilNo))
				perNoList.add(mySicilNo);
		} catch (Exception e) {
		}
		List sicilller2 = null;
		boolean tesisDurum = getTesisDurumu();

		if (istenAyrilanEkle == false && ikinciYonetici) {
			sicilller2 = authenticatedUser.getIkinciYoneticiPersonelSicilleri();
			if (sicilller2 != null)
				perNoList.addAll(sicilller2);

		}
		if (sicilNo != null)
			sicilNo = sicilNo.trim();
		else
			sicilNo = "";
		if (PdksUtil.hasStringValue(sicilNo))
			sicilNo = getSicilNo(sicilNo);
		if (istenAyrilanEkle == false && PdksUtil.hasStringValue(sicilNo)) {
			List<String> list = new ArrayList<String>();
			if (PdksUtil.getSicilNoUzunluk() != null) {
				if (perNoList.contains(sicilNo))
					list.add(sicilNo);
			} else {
				for (String pSicil : perNoList) {
					if (isStringEqual(sicilNo, pSicil))
						list.add(pSicil);
				}
			}
			perNoList.clear();
			if (!list.isEmpty())
				perNoList.addAll(list);
			list = null;

		} else if (PdksUtil.hasStringValue(sicilNo) || PdksUtil.hasStringValue(ad) || PdksUtil.hasStringValue(soyad) || gelenSirket != null || sirket != null || tesis != null || ekSaha1 != null || ekSaha2 != null || ekSaha3 != null || ekSaha4 != null) {
			HashMap parametreMap = new HashMap();
			parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "pdksSicilNo");
			if (!authenticatedUser.isYoneticiKontratli() && sirket != null)
				parametreMap.put("sirket.id=", sirket.getId());
			else if (gelenSirket != null)
				parametreMap.put("sirket.id=", gelenSirket.getId());

			if (ekSaha1 != null && (departman == null || departman.isAdminMi()))
				parametreMap.put("ekSaha1.id=", ekSaha1.getId());
			if (ekSaha2 != null && (departman == null || departman.isAdminMi()))
				parametreMap.put("ekSaha2.id=", ekSaha2.getId());
			if (ekSaha3 != null)
				parametreMap.put("ekSaha3.id=", ekSaha3.getId());
			if (ekSaha4 != null && (departman == null || departman.isAdminMi()) && (departman == null || departman.isAdminMi()))
				parametreMap.put("ekSaha4.id=", ekSaha4.getId());
			if (tesis != null && ((sirket == null && tesisDurum) || (sirket != null && (sirket.getId() != null || sirket.isTesisDurumu()))))
				parametreMap.put("tesis.id=", tesis.getId());
			if (PdksUtil.hasStringValue(ad))
				parametreMap.put("ad like", ad.trim() + "%");
			if (PdksUtil.hasStringValue(soyad))
				parametreMap.put("soyad like", soyad.trim() + "%");
			List siciller = null;
			if (!istenAyrilanEkle) {
				siciller = (List) authenticatedUser.getYetkiTumPersonelNoList().clone();
				if (sicilller2 != null)
					siciller.addAll(sicilller2);
			} else if (PdksUtil.hasStringValue(sicilNo)) {
				siciller = new ArrayList<String>();
				siciller.add(sicilNo.trim());
			}
			String fieldName = null;
			if (siciller != null && !siciller.isEmpty()) {
				fieldName = "pdksSicilNo";
				parametreMap.put(fieldName, siciller);
			}

			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			if (!hata) {
				if (fieldName != null)
					perNoList = (ArrayList<String>) getParamList(true, siciller, fieldName, parametreMap, Personel.class, session);
				else
					perNoList = (ArrayList<String>) pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, Personel.class);
			} else {
				perNoList = new ArrayList<String>();
				PdksUtil.addMessageAvailableWarn(sirketAciklama() + " seçiniz!");
			}
		}
		for (Iterator iterator = perNoList.iterator(); iterator.hasNext();) {
			String sicil = (String) iterator.next();
			if (!PdksUtil.hasStringValue(sicil))
				iterator.remove();

		}
		return perNoList;

	}

	/**
	 * @param roleAdi
	 * @param personel
	 * @param session
	 * @return
	 */
	private boolean isYetkiKontrol(String roleAdi, Personel personel, Session session) {
		boolean durum = Boolean.FALSE;
		if (personel != null) {
			List<User> userList = getRoleKullanicilari(roleAdi, null, personel, session);
			durum = !userList.isEmpty();
		}
		return durum;
	}

	/**
	 * @param user
	 * @param personel
	 * @param session
	 * @return
	 */
	public Boolean getProjeMuduru(User user, Personel personel, Session session) {
		boolean projeMuduru = Boolean.FALSE;
		if (user != null || personel != null) {
			if (user != null) {
				setUserRoller(user, session);
				projeMuduru = user.isProjeMuduru();
			} else if (personel != null)
				projeMuduru = isYetkiKontrol(Role.TIPI_PROJE_MUDURU, personel, session);
		}
		return projeMuduru;
	}

	/**
	 * @param user
	 * @param personel
	 * @param session
	 * @return
	 */
	public Boolean getGenelMudur(User user, Personel personel, Session session) {
		boolean genelMudur = Boolean.FALSE;
		if (user != null || personel != null) {
			if (user != null) {
				setUserRoller(user, session);
				genelMudur = user.isGenelMudur();
			} else if (personel != null)
				genelMudur = isYetkiKontrol(Role.TIPI_GENEL_MUDUR, personel, session);

		}
		return genelMudur;
	}

	/**
	 * @param adresler
	 * @param mailler
	 * @param session
	 * @return
	 */
	public List<User> getAktifKullanicilar(List<IzinTipiMailAdres> adresler, List<String> mailler, Session session) {
		if (mailler == null)
			mailler = new ArrayList<String>();
		else
			mailler.clear();
		TreeMap<String, IzinTipiMailAdres> mailMap = new TreeMap<String, IzinTipiMailAdres>();
		for (IzinTipiMailAdres izinTipiMailAdres : adresler)
			mailMap.put(izinTipiMailAdres.getAdres(), izinTipiMailAdres);
		List idList = new ArrayList(mailMap.keySet());
		String fieldName = "email";
		HashMap parametreMap = new HashMap();
		parametreMap.put(fieldName, idList);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		// List<User> kullanicilar = pdksEntityController.getObjectByInnerObjectList(parametreMap, User.class);
		List<User> kullanicilar = getParamList(false, idList, fieldName, parametreMap, User.class, session);
		for (Iterator iterator = kullanicilar.iterator(); iterator.hasNext();) {
			User user = (User) iterator.next();
			if (!user.isDurum()) {
				if (mailMap.containsKey(user.getEmail()))
					mailMap.remove(user.getEmail());
				mailler.add(user.getAdSoyad() + " ait kullanıcı pasif'dir!");
			} else {
				Personel personel = user.getPdksPersonel();
				if (!personel.isCalisiyor() && personel.getId() != null) {
					if (mailMap.containsKey(user.getEmail()))
						mailMap.remove(user.getEmail());
					mailler.add(user.getAdSoyad() + " ait kullanıcısı işten ayrılmıştır!");
				} else
					iterator.remove();
			}
		}
		adresler.clear();
		if (!mailMap.isEmpty())
			adresler.addAll(new ArrayList<IzinTipiMailAdres>(mailMap.values()));
		mailMap = null;
		return kullanicilar;
	}

	/**
	 * @return
	 */
	public ERPController getERPController() {
		ERPController controller = pdksNoSapController;
		String key = parameterMap.containsKey("erpController") ? parameterMap.get("erpController") : "";
		if (key.equals("S2"))
			controller = pdksSapController;
		else if (key.equals("S3"))
			controller = pdksSap3Controller;
		return controller;
	}

	/**
	 * @param session
	 * @param user
	 * @param bordroAltBirimiMap
	 * @param masrafYeriMap
	 * @param personel
	 * @param personelBilgisiGetir
	 * @param update
	 * @param yeni
	 * @param yoneticiAta
	 * @return
	 * @throws Exception
	 */
	public Boolean sapVeriGuncelle(Session session, User user, TreeMap bordroAltBirimiMap, TreeMap masrafYeriMap, Personel personel, LinkedHashMap<String, Personel> personelBilgisiGetir, boolean update, boolean yeni, boolean yoneticiAta) throws Exception {
		ERPController sapController = getERPController();
		Boolean guncellendi = Boolean.FALSE;
		if (session == null)
			session = PdksUtil.getSession(entityManager, yeni);
		PersonelExtra personelExtra = personel.getPersonelExtra();
		if (personelExtra == null) {
			if (personel.getId() != null) {
				HashMap fields = new HashMap();
				fields.put("personel", personel);
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				personelExtra = (PersonelExtra) pdksEntityController.getObjectByInnerObject(fields, PersonelExtra.class);

			}
			if (personelExtra == null) {
				personelExtra = new PersonelExtra();
				personelExtra.setPersonel(personel);
			}
			personel.setPersonelExtra(personelExtra);
		}
		if (personelBilgisiGetir == null)
			personelBilgisiGetir = new LinkedHashMap<String, Personel>();

		if (personel != null)
			personelBilgisiGetir.put(personel.getSicilNo(), (Personel) personel.clone());

		personelBilgisiGetir = sapController.topluHaldePersonelBilgisiGetir(session, bordroAltBirimiMap, masrafYeriMap, personelBilgisiGetir, null, null, null, null);
		if (personelBilgisiGetir != null && !personelBilgisiGetir.isEmpty()) {
			ArrayList<String> pernoList = new ArrayList<String>();
			pernoList.add(personel.getSicilNo());
			HashMap<String, Personel> topluHaldeYoneticiBulMap = sapController.topluHaldeYoneticiBulMap(1, pernoList, null, null);
			Personel personelSap = personelBilgisiGetir.get(personel.getSicilNo());
			Personel yoneticisi = null;
			yoneticisi = personel.getPdksYonetici();
			if (topluHaldeYoneticiBulMap.containsKey(personel.getSicilNo())) {
				Personel yoneticiSap = topluHaldeYoneticiBulMap.get(personel.getSicilNo());
				if (yoneticiSap.getPdksYonetici() != null && yoneticiSap.getPdksYonetici().getErpSicilNo() != null) {
					if (personel.getPdksYonetici() == null || !personel.getPdksYonetici().getSicilNo().equals(yoneticiSap.getPdksYonetici().getErpSicilNo())) {
						HashMap parametreMap = new HashMap();
						parametreMap.put("pdksSicilNo", yoneticiSap.getPdksYonetici().getErpSicilNo());
						if (session != null)
							parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						yoneticisi = (Personel) pdksEntityController.getObjectByInnerObject(parametreMap, Personel.class);

					}

				}

			}
			if (yoneticiAta)
				personel.setYoneticisiAta(yoneticisi);
			personel.setAd(personelSap.getAd());
			personel.setSoyad(personelSap.getSoyad());
			personel.setBordroAltAlan(personelSap.getBordroAltAlan());
			personel.setDogumTarihi(personelSap.getDogumTarihi());
			personel.setGrubaGirisTarihi(personelSap.getGrubaGirisTarihi());
			personel.setMasrafYeri(personelSap.getMasrafYeri());
			personel.setDurum(Boolean.TRUE);
			if (personelSap.getSirket() != null)
				personel.setSirket(personelSap.getSirket());
			personel.setIseBaslamaTarihi(personelSap.getIseBaslamaTarihi());
			if (personel.getIzinHakEdisTarihi() == null || getParameterKey("hakedisSAP").equals("1"))
				personel.setIzinHakEdisTarihi(personelSap.getIzinHakEdisTarihi());
			if (personelSap.getSonCalismaTarihi() == null)
				personel.setIstenAyrilisTarihi(PdksUtil.getSonSistemTarih());
			else {
				if (personel.getSonCalismaTarihi() == null || PdksUtil.tarihKarsilastirNumeric(personel.getSonCalismaTarihi(), personelSap.getSonCalismaTarihi()) != 0) {
					personel.setGuncellemeTarihi(new Date());
					if (user != null)
						personel.setGuncelleyenUser(user);
				}
				personel.setIstenAyrilisTarihi(personelSap.getIstenAyrilisTarihi());
			}

			if (update) {
				if (personel.getId() == null) {
					if (user != null)
						personel.setOlusturanUser(user);
					personel.setOlusturmaTarihi(new Date());
				}
				if (yeni)
					session.clear();
				pdksEntityController.saveOrUpdate(session, entityManager, personel);
				if (personelSap.getPersonelExtra() != null) {
					personelExtra.setCepTelefon(personelSap.getPersonelExtra().getCepTelefon());
					personelExtra.setIlce(personelSap.getPersonelExtra().getIlce());
					pdksEntityController.saveOrUpdate(session, entityManager, personelExtra);
				}
			}
			guncellendi = Boolean.TRUE;

		}

		return guncellendi;
	}

	/**
	 * @param session
	 * @return
	 */
	private HashMap<Long, Double> vardiyaSuresiOlustur(Session session) {
		HashMap parametreMap = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT S.* from " + Vardiya.TABLE_NAME + " S WITH(nolock) ");
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Vardiya> vardiyaList = pdksEntityController.getObjectBySQLList(sb, parametreMap, Vardiya.class);
		//
		//
		// List<Vardiya> vardiyaList =
		// pdksEntityController.getObjectByInnerObjectList(parametreMap,
		// Vardiya.class);
		HashMap<Long, Double> vardiyaNetCalismaSuresiMap = new HashMap<Long, Double>();
		for (Vardiya vardiya : vardiyaList)
			try {
				if (vardiya.isCalisma())
					vardiyaNetCalismaSuresiMap.put(vardiya.getId(), vardiya.getNetCalismaSuresi());
				else
					vardiyaNetCalismaSuresiMap.put(vardiya.getId(), 0D);

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());

			}
		return vardiyaNetCalismaSuresiMap;
	}

	/**
	 * @param denklestirmeDonemi
	 * @param searchKey
	 * @param value
	 * @param pdks
	 * @param session
	 * @return
	 */
	private List denklestirmePersonelBul(DepartmanDenklestirmeDonemi denklestirmeDonemi, String searchKey, Object value, boolean pdks, Session session) {
		HashMap parametreMap = new HashMap();
		// parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "personel");
		String fieldName = "pdksPersonel." + searchKey;
		parametreMap.put(fieldName, value);
		parametreMap.put("pdksPersonel.sskCikisTarihi>=", denklestirmeDonemi.getBaslangicTarih());
		if (!Personel.getGrubaGirisTarihiAlanAdi().equalsIgnoreCase(Personel.COLUMN_NAME_GRUBA_GIRIS_TARIHI))
			parametreMap.put("pdksPersonel.iseBaslamaTarihi<=", denklestirmeDonemi.getBitisTarih());
		else
			parametreMap.put("pdksPersonel.grubaGirisTarihi<=", denklestirmeDonemi.getBitisTarih());
		// parametreMap.put("personel.durum=", Boolean.TRUE);
		// parametreMap.put("pdksSicilNo=", "90007309");
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PersonelView> perList;
		try {
			// perList = getPersonelViewList(pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, PdksPersonelView.class));
			perList = getPersonelViewList(getParamList(true, (List) value, fieldName, parametreMap, PdksPersonelView.class, session));
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

			perList = new ArrayList<PersonelView>();
		}

		return perList;
	}

	/**
	 * @param denklestirmeDonemi
	 * @param perList
	 * @return
	 */
	private TreeMap<String, VardiyaHafta> denklestirmeHaftalikVardiyaSablonuOlustur(DepartmanDenklestirmeDonemi denklestirmeDonemi, List<Personel> perList) {

		TreeMap<String, VardiyaHafta> vardiyaHaftaMap = new TreeMap<String, VardiyaHafta>();

		return vardiyaHaftaMap;
	}

	/**
	 * @param denklestirmeDonemi
	 * @param perList
	 * @param zamanGuncelle
	 * @param session
	 * @return
	 * @throws Exception
	 */
	private List<VardiyaGun> denklestirmeVardiyalariGetir(DepartmanDenklestirmeDonemi denklestirmeDonemi, ArrayList<Personel> perList, HashMap<Long, List<PersonelIzin>> izinMap, boolean zamanGuncelle, Session session) throws Exception {
		Calendar cal = Calendar.getInstance();
		TreeMap<String, VardiyaGun> vardiyaMap = getVardiyalar((List<Personel>) perList.clone(), denklestirmeDonemi.getBaslangicTarih(), tariheGunEkleCikar(cal, denklestirmeDonemi.getBitisTarih(), 1), izinMap, Boolean.FALSE, session, Boolean.FALSE);
		List<VardiyaGun> vardiyaDblar = new ArrayList<VardiyaGun>(vardiyaMap.values());

		return vardiyaDblar;
	}

	/**
	 * @param loginUser
	 * @param denklestirmeAy
	 * @param vardiyalar
	 * @param session
	 * @return
	 */
	@Transactional
	public List<PersonelFazlaMesai> denklestirmeFazlaMesaileriGetir(User loginUser, DenklestirmeAy denklestirmeAy, List<VardiyaGun> vardiyalar, Session session) {
		TreeMap<Long, VardiyaGun> vardiyaMap = new TreeMap<Long, VardiyaGun>();
		String donemKodu = denklestirmeAy != null ? String.valueOf(denklestirmeAy.getYil() * 100 + denklestirmeAy.getAy()) : null;
		List<PersonelFazlaMesai> fazlaMesailer = null;
		boolean iptalDurum = denklestirmeAy != null && (denklestirmeAy.getDurum() || ((loginUser.isIK() || loginUser.isAdmin()) && denklestirmeAy.getGuncelleIK()));
		if (vardiyalar != null) {
			for (Iterator iterator = vardiyalar.iterator(); iterator.hasNext();) {
				VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
				if (vardiyaGun.getId() != null) {
					if (donemKodu != null)
						vardiyaGun.setAyinGunu(vardiyaGun.getVardiyaDateStr().startsWith(donemKodu));
					vardiyaMap.put(vardiyaGun.getId(), vardiyaGun);
				}
			}
		}
		HashMap parametreMap = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT I.* FROM " + PersonelFazlaMesai.TABLE_NAME + " I  WITH(nolock) ");
		String fieldName = null;
		List vList = null;
		if (!vardiyaMap.isEmpty()) {
			fieldName = "v";
			vList = new ArrayList(vardiyaMap.keySet());
			sb.append(" WHERE I." + PersonelFazlaMesai.COLUMN_NAME_VARDIYA_GUN + " :" + fieldName);
			parametreMap.put(fieldName, vList);
			sb.append(" AND I." + PersonelFazlaMesai.COLUMN_NAME_DURUM + " = 1 ");
		} else {
			if (denklestirmeAy != null) {
				sb.append(" INNER JOIN " + VardiyaGun.TABLE_NAME + " V ON V." + VardiyaGun.COLUMN_NAME_ID + " = I." + PersonelFazlaMesai.COLUMN_NAME_VARDIYA_GUN);
				sb.append(" AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " >= :v1 AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " <= :v2 ");
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, denklestirmeAy.getYil());
				cal.set(Calendar.MONTH, denklestirmeAy.getAy() - 1);
				cal.set(Calendar.DATE, 1);
				Date basTarih = PdksUtil.getDate(cal.getTime());
				cal.setTime(basTarih);
				cal.set(Calendar.DATE, cal.getMaximum(Calendar.DAY_OF_MONTH));
				Date bitTarih = PdksUtil.getDate(cal.getTime());
				parametreMap.put("v1", basTarih);
				parametreMap.put("v2", bitTarih);
			}
			sb.append(" WHERE I." + PersonelFazlaMesai.COLUMN_NAME_DURUM + " = 1 ");
		}

		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		if (fieldName != null)
			fazlaMesailer = getSQLParamList(vList, sb, fieldName, parametreMap, PersonelFazlaMesai.class, session);
		else
			fazlaMesailer = pdksEntityController.getObjectBySQLList(sb, parametreMap, PersonelFazlaMesai.class);
		if (fazlaMesailer == null)
			fazlaMesailer = new ArrayList<PersonelFazlaMesai>();

		if (!fazlaMesailer.isEmpty()) {
			boolean flush = false;
			boolean kaydet = denklestirmeAy != null && denklestirmeAy.getDurum().equals(Boolean.TRUE);
			for (Iterator iterator = fazlaMesailer.iterator(); iterator.hasNext();) {
				PersonelFazlaMesai fazlaMesai = (PersonelFazlaMesai) iterator.next();
				if (fazlaMesai.isOnaylandi() && fazlaMesai.isBayram() == false) {
					VardiyaGun vardiyaGun = vardiyaMap.get(fazlaMesai.getVardiyaGun().getId());
					if (vardiyaGun.getVardiya() == null || vardiyaGun.getVardiya().isCalisma() == false)
						continue;
					Vardiya islemVardiya = vardiyaGun.getIslemVardiya();

					String str = "Hatali fazla mesai : " + vardiyaGun.getVardiyaKeyStr() + " (" + loginUser.timeFormatla(islemVardiya.getVardiyaBasZaman()) + "-" + loginUser.timeFormatla(islemVardiya.getVardiyaBitZaman()) + " --> " + loginUser.timeFormatla(fazlaMesai.getBasZaman()) + "-"
							+ loginUser.timeFormatla(fazlaMesai.getBitZaman()) + " )";
					if (islemVardiya.getVardiyaTelorans2BasZaman().getTime() >= fazlaMesai.getBitZaman().getTime() || islemVardiya.getVardiyaTelorans1BitZaman().getTime() <= fazlaMesai.getBasZaman().getTime())
						continue;
					if (kaydet) {
						parametreMap.clear();
						parametreMap.put("izinSahibi.id=", vardiyaGun.getPersonel().getId());
						parametreMap.put("baslangicZamani<=", fazlaMesai.getBitZaman());
						parametreMap.put("bitisZamani>=", fazlaMesai.getBasZaman());
						parametreMap.put("izinDurumu", getAktifIzinDurumList());
						if (session != null)
							parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						List<PersonelIzin> izinList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, PersonelIzin.class);
						for (Iterator iterator2 = izinList.iterator(); iterator2.hasNext();) {
							PersonelIzin personelIzin = (PersonelIzin) iterator2.next();
							IzinTipi izinTipi = personelIzin.getIzinTipi();
							if (izinTipi.getBakiyeIzinTipi() != null)
								iterator2.remove();
						}
						if (izinList.isEmpty()) {
							if (islemVardiya.getVardiyaBasZaman().getTime() < fazlaMesai.getBasZaman().getTime())
								logger.debug(str + " Geç çıkma");
							else
								logger.debug(str + " Erken gelme");
							if (iptalDurum) {
								fazlaMesai.setDurum(Boolean.FALSE);
								if (!loginUser.isAdmin()) {
									fazlaMesai.setGuncelleyenUser(loginUser);
									fazlaMesai.setGuncellemeTarihi(new Date());
								}
								pdksEntityController.saveOrUpdate(session, entityManager, fazlaMesai);
								iterator.remove();
								flush = Boolean.TRUE;
							}
						}

					}
				}
			}
			try {
				if (flush)
					session.flush();
			} catch (Exception e) {
			}
		}
		vardiyaMap = null;

		return fazlaMesailer;
	}

	/**
	 * @return
	 */
	public List<Integer> getAktifIzinDurumList() {
		List<Integer> izinDurumuList = new ArrayList<Integer>();
		izinDurumuList.add(PersonelIzin.IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA);
		izinDurumuList.add(PersonelIzin.IZIN_DURUMU_IKINCI_YONETICI_ONAYINDA);
		izinDurumuList.add(PersonelIzin.IZIN_DURUMU_IK_ONAYINDA);
		izinDurumuList.add(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
		izinDurumuList.add(PersonelIzin.IZIN_DURUMU_SAP_GONDERILDI);
		return izinDurumuList;
	}

	/**
	 * @param kgsPerMap
	 * @param tarih1
	 * @param tarih2
	 * @param session
	 * @return
	 */
	public HashMap<Long, ArrayList<HareketKGS>> personelHareketleriGetir(HashMap<Long, PersonelView> kgsPerMap, Date tarih1, Date tarih2, Session session) {
		HashMap<Long, ArrayList<HareketKGS>> personelHareketMap = fillPersonelKGSHareketMap(new ArrayList(kgsPerMap.keySet()), tarih1, tarih2, session);
		TreeMap<Long, HareketKGS> islemIdler = new TreeMap<Long, HareketKGS>();
		for (Long perNoId : personelHareketMap.keySet()) {
			ArrayList<HareketKGS> perHareketList = personelHareketMap.get(perNoId);
			for (HareketKGS HareketKGS : perHareketList) {
				HareketKGS.setPersonelFazlaMesai(null);
				if (HareketKGS.getIslemId() != null)
					islemIdler.put(HareketKGS.getIslemId(), HareketKGS);
				HareketKGS.setPersonel(kgsPerMap.get(HareketKGS.getPersonelId()));
			}
			personelHareketMap.put(perNoId, perHareketList);
		}
		if (!islemIdler.isEmpty()) {
			List idList = new ArrayList(islemIdler.keySet());
			String fieldName = "v";
			HashMap parametreMap = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT I.* FROM " + PersonelHareketIslem.TABLE_NAME + " I  WITH(nolock) ");
			sb.append(" WHERE I." + PersonelHareketIslem.COLUMN_NAME_ID + " :" + fieldName);
			parametreMap.put("v", idList);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			// List<PersonelHareketIslem> list = pdksEntityController.getObjectBySQLList(sb, parametreMap, PersonelHareketIslem.class);
			List<PersonelHareketIslem> list = getSQLParamList(idList, sb, fieldName, parametreMap, PersonelHareketIslem.class, session);
			for (PersonelHareketIslem islem : list)
				islemIdler.get(islem.getId()).setIslem(islem);
			list = null;
		}
		islemIdler = null;
		return personelHareketMap;
	}

	/**
	 * @param denklestirmeDonemi
	 * @param perList
	 * @param session
	 * @return
	 */
	public HashMap<Long, List<PersonelIzin>> denklestirmeIzinleriOlustur(DepartmanDenklestirmeDonemi denklestirmeDonemi, List<Personel> perList, Session session) {
		List<Long> pIdler = new ArrayList<Long>();
		for (Personel personel : perList)
			pIdler.add(personel.getId());
		Calendar cal = Calendar.getInstance();
		Date basTarih = tariheGunEkleCikar(cal, denklestirmeDonemi.getBaslangicTarih(), -2);
		Date bitTarih = tariheGunEkleCikar(cal, denklestirmeDonemi.getBitisTarih(), 1);
		HashMap<Long, List<PersonelIzin>> izinMap = getPersonelIzinMap(getBaseObjectIdList(perList), basTarih, bitTarih, session);
		return izinMap;
	}

	/**
	 * @param tarih
	 * @return
	 */
	protected int getHafta(Date tarih) {
		Calendar cal = new GregorianCalendar(Constants.TR_LOCALE);
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.setTime(tarih);
		int haftaDeger = cal.get(Calendar.YEAR) * 100 + cal.get(Calendar.WEEK_OF_YEAR);
		return haftaDeger;

	}

	/**
	 * @param map
	 * @return
	 */
	public KapiView getKapiView(HashMap map) {
		KapiView kapiView = null;
		if (map != null && map.containsKey(PdksEntityController.MAP_KEY_SESSION)) {
			try {
				KapiKGS kapiKGS = (KapiKGS) pdksEntityController.getObjectByInnerObject(map, KapiKGS.class);
				if (kapiKGS != null)
					kapiView = kapiKGS.getKapiView();
			} catch (Exception e) {
			}

		}
		return kapiView;
	}

	/**
	 * @param list
	 * @param session
	 * @return
	 */
	public List<Tanim> setDenklestirmeDinamikDurum(List<AylikPuantaj> list, Session session) {
		List<Tanim> tanimList = null;
		if (list != null) {
			HashMap<Long, AylikPuantaj> map = new HashMap<Long, AylikPuantaj>();
			for (AylikPuantaj ap : list) {
				if (ap.getPersonelDenklestirme() == null)
					continue;
				ap.setDinamikAlanMap(new TreeMap<Long, PersonelDenklestirmeDinamikAlan>());
				map.put(ap.getPersonelDenklestirme().getId(), ap);
			}
			if (!map.isEmpty()) {
				TreeMap<String, PersonelDenklestirmeDinamikAlan> dinamikMap = new TreeMap<String, PersonelDenklestirmeDinamikAlan>();
				tanimList = setDenklestirmeDinamikDurum(new ArrayList<Long>(map.keySet()), dinamikMap, session);
				for (String str : dinamikMap.keySet()) {
					PersonelDenklestirmeDinamikAlan pda = dinamikMap.get(str);
					Long key = pda.getPersonelDenklestirme().getId();
					AylikPuantaj ap = map.get(key);
					ap.getDinamikAlanMap().put(pda.getAlan().getId(), pda);
				}
				dinamikMap = null;
			}
			map = null;
		}

		return tanimList;
	}

	/**
	 * @param list
	 * @param dinamikMap
	 * @return
	 */
	public List<Tanim> setDenklestirmeDinamikDurum(List<Long> list, TreeMap<String, PersonelDenklestirmeDinamikAlan> dinamikMap, Session session) {
		HashMap fields = new HashMap();
		if (dinamikMap == null)
			dinamikMap = new TreeMap<String, PersonelDenklestirmeDinamikAlan>();
		List<Tanim> denklestirmeDinamikAlanlar = new ArrayList<Tanim>();
		if (list != null && !list.isEmpty()) {
			String fieldName = "s";
			fields.clear();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT S.* from " + PersonelDenklestirmeDinamikAlan.TABLE_NAME + " S WITH(nolock) ");
			sb.append(" WHERE S." + PersonelDenklestirmeDinamikAlan.COLUMN_NAME_PERSONEL_DENKLESTIRME + " :" + fieldName);
			sb.append(" AND S." + PersonelDenklestirmeDinamikAlan.COLUMN_NAME_DENKLESTIRME_ALAN_DURUM + " = 1 ");
			sb.append(" ORDER BY S." + PersonelDenklestirmeDinamikAlan.COLUMN_NAME_ALAN);
			fields.put(fieldName, list);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			// List<PersonelDenklestirmeDinamikAlan> alanList = pdksEntityController.getObjectBySQLList(sb, fields, PersonelDenklestirmeDinamikAlan.class);
			List<PersonelDenklestirmeDinamikAlan> alanList = getSQLParamList(list, sb, fieldName, fields, PersonelDenklestirmeDinamikAlan.class, session);
			if (!alanList.isEmpty()) {
				Tanim tanim = null;
				List<Long> idList = new ArrayList<Long>();
				List<Tanim> tanimList = new ArrayList<Tanim>();
				for (PersonelDenklestirmeDinamikAlan personelDenklestirmeDinamikAlan : alanList) {
					dinamikMap.put(personelDenklestirmeDinamikAlan.getKey(), personelDenklestirmeDinamikAlan);
					tanim = personelDenklestirmeDinamikAlan.getAlan();
					if (!idList.contains(tanim.getId())) {
						tanimList.add(tanim);
						idList.add(tanim.getId());
					}
				}
				if (!tanimList.isEmpty()) {
					if (tanimList.size() > 1)
						denklestirmeDinamikAlanlar.addAll(PdksUtil.sortTanimList(null, tanimList));
					else
						denklestirmeDinamikAlanlar.addAll(tanimList);
				}

				tanimList = null;
			}
		}
		return denklestirmeDinamikAlanlar;

	}

	/**
	 * @param pdIdMap
	 * @param session
	 * @return
	 */
	public List<Tanim> dinamikAlanlariDoldur(HashMap<Long, AylikPuantaj> pdIdMap, Session session) {
		TreeMap<String, PersonelDenklestirmeDinamikAlan> dinamikMap = new TreeMap<String, PersonelDenklestirmeDinamikAlan>();
		List<Tanim> dinamikAlanlar = setDenklestirmeDinamikDurum(new ArrayList<Long>(pdIdMap.keySet()), dinamikMap, session);
		if (!pdIdMap.isEmpty()) {
			for (String key : dinamikMap.keySet()) {
				PersonelDenklestirmeDinamikAlan pda = dinamikMap.get(key);
				AylikPuantaj ap = pdIdMap.get(pda.getPersonelDenklestirme().getId());
				ap.getDinamikAlanMap().put(pda.getAlan().getId(), pda);
			}
		}
		return dinamikAlanlar;
	}

	/**
	 * @param denklestirmeDonemi
	 * @param tatilGunleriMap
	 * @param searchKey
	 * @param value
	 * @param pdks
	 * @param zamanGuncelle
	 * @param tarihHareketEkle
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@Transactional
	public List<PersonelDenklestirmeTasiyici> personelDenklestir(DepartmanDenklestirmeDonemi denklestirmeDonemi, TreeMap<String, Tatil> tatilGunleriMap, String searchKey, Object value, boolean pdks, boolean zamanGuncelle, boolean tarihHareketEkle, Session session) throws Exception {
		TreeMap<String, Boolean> gunMap = new TreeMap<String, Boolean>();
		User loginUser = denklestirmeDonemi.getLoginUser() != null ? denklestirmeDonemi.getLoginUser() : authenticatedUser;

		boolean yenidenCalistir = false;
		List<YemekIzin> yemekAraliklari = getYemekList(denklestirmeDonemi.getBaslangicTarih(), denklestirmeDonemi.getBitisTarih(), session);

		List<PersonelDenklestirmeTasiyici> personelDenklestirmeTasiyiciList = new ArrayList<PersonelDenklestirmeTasiyici>();
		HashMap parametreMap = new HashMap();

		List<PersonelView> perViewList = denklestirmePersonelBul(denklestirmeDonemi, searchKey, value, pdks, session);

		if (!perViewList.isEmpty()) {
			Date bugun = PdksUtil.getDate(new Date());
			List<Personel> perList = new ArrayList<Personel>();
			HashMap<Long, PersonelView> kgsPerList = new HashMap<Long, PersonelView>();
			for (Iterator<PersonelView> iterator = perViewList.iterator(); iterator.hasNext();) {
				PersonelView personelView = iterator.next();

				if (personelView.getPdksPersonel() != null)
					perList.add(personelView.getPdksPersonel());
				kgsPerList.put(personelView.getPersonelKGS().getId(), personelView);

			}
			if (!perList.isEmpty()) {
				HashMap map = new HashMap();
				map.put("manuel", Boolean.FALSE);
				map.put("kapi.durum", Boolean.TRUE);
				map.put("kapi.pdks", Boolean.TRUE);
				map.put("kapi.tipi.kodu", Kapi.TIPI_KODU_GIRIS);
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);

				HashMap<Long, Double> vardiyaNetCalismaSuresiMap = vardiyaSuresiOlustur(session);
				TreeMap<String, VardiyaHafta> vardiyaHaftaMap = denklestirmeHaftalikVardiyaSablonuOlustur(denklestirmeDonemi, perList);
				Calendar cal = Calendar.getInstance();
				cal.setTime(denklestirmeDonemi.getBaslangicTarih());
				Date tarih = cal.getTime();
				TreeMap<String, Integer> genelHaftaMap = new TreeMap<String, Integer>();
				int denklestirmeHaftasi = 0;
				List<PersonelDenklestirmeTasiyici> baslikDenklestirmeDonemiList = new ArrayList<PersonelDenklestirmeTasiyici>();
				PersonelDenklestirmeTasiyici donemi = null;
				DepartmanDenklestirmeDonemi departmanDenklestirmeDonemi = null;
				while (PdksUtil.tarihKarsilastirNumeric(denklestirmeDonemi.getBitisTarih(), tarih) != -1) {
					String key = PdksUtil.convertToDateString(tarih, "yyyyMMdd");
					if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
						++denklestirmeHaftasi;
						donemi = new PersonelDenklestirmeTasiyici();
						donemi.setDenklestirmeHaftasi(denklestirmeHaftasi);
						donemi.setVardiyalar(new ArrayList<VardiyaGun>());
						departmanDenklestirmeDonemi = new DepartmanDenklestirmeDonemi();
						donemi.setDenklestirmeDonemi(departmanDenklestirmeDonemi);
						departmanDenklestirmeDonemi.setBaslangicTarih(tarih);
						baslikDenklestirmeDonemiList.add(donemi);
					}

					if (departmanDenklestirmeDonemi != null)
						departmanDenklestirmeDonemi.setBitisTarih(tarih);

					VardiyaGun vardiyaGun = new VardiyaGun();
					vardiyaGun.setVardiyaDate(tarih);
					if (tatilGunleriMap.containsKey(key))
						vardiyaGun.setTatil(tatilGunleriMap.get(key));

					donemi.getVardiyalar().add(vardiyaGun);
					genelHaftaMap.put(key, denklestirmeHaftasi);
					cal.add(Calendar.DATE, 1);
					tarih = cal.getTime();
				}
				// Personel izinleri bulunuyor

				HashMap<Long, List<PersonelIzin>> izinMap = denklestirmeIzinleriOlustur(denklestirmeDonemi, perList, session);

				List<VardiyaGun> vardiyaDblar = denklestirmeVardiyalariGetir(denklestirmeDonemi, (ArrayList<Personel>) perList, izinMap, zamanGuncelle, session);
				boolean fazlaMesaiTalepDurum = getParameterKey("fazlaMesaiTalepDurum").equals("1");
				TreeMap<Long, VardiyaGun> vMap = new TreeMap<Long, VardiyaGun>();
				for (Iterator<VardiyaGun> iterator2 = vardiyaDblar.iterator(); iterator2.hasNext();) {
					VardiyaGun vardiyaGun = iterator2.next();
					if (vardiyaGun == null)
						continue;
					vardiyaGun.setFazlaMesaiTalepler(null);
					if (fazlaMesaiTalepDurum && vardiyaGun.getId() != null)
						vMap.put(vardiyaGun.getId(), vardiyaGun);

				}
				if (!vMap.isEmpty()) {
					List idList = new ArrayList(vMap.keySet());
					String fieldName = "vardiyaGun.id";
					HashMap fields = new HashMap();
					fields.put(fieldName, idList);
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					// List<FazlaMesaiTalep> veriList = pdksEntityController.getObjectByInnerObjectList(fields, FazlaMesaiTalep.class);
					List<FazlaMesaiTalep> veriList = getParamList(false, idList, fieldName, fields, FazlaMesaiTalep.class, session);
					for (FazlaMesaiTalep fazlaMesaiTalep : veriList) {
						if (fazlaMesaiTalep.getDurum())
							vMap.get(fazlaMesaiTalep.getVardiyaGun().getId()).addFazlaMesaiTalep(fazlaMesaiTalep);
					}
					veriList = null;
				}
				vMap = null;
				List<Long> personelIdler = new ArrayList<Long>();
				for (Personel personel : perList)
					personelIdler.add(personel.getId());

				parametreMap.clear();
				TreeMap<Long, PersonelDenklestirmeTasiyici> personelDenklestirmeMap = new TreeMap<Long, PersonelDenklestirmeTasiyici>();
				TreeMap<Long, List<VardiyaGun>> personelVardiyaBulMap = new TreeMap<Long, List<VardiyaGun>>();
				if (!perList.isEmpty() && vardiyaDblar != null) {
					List<VardiyaGun> vardiyalar = new ArrayList<VardiyaGun>();
					// Personel calisma planlari olusturuluyor
					HashMap<Long, ArrayList<VardiyaGun>> calismaPlaniMap = new HashMap<Long, ArrayList<VardiyaGun>>();
					TreeMap<String, VardiyaGun> vardiyaTarihMap = new TreeMap<String, VardiyaGun>();
					TreeMap<Long, PersonelDenklestirme> personelDenklestirmeDonemMap = denklestirmeDonemi.getPersonelDenklestirmeDonemMap();
					if (personelDenklestirmeDonemMap == null)
						personelDenklestirmeDonemMap = new TreeMap<Long, PersonelDenklestirme>();
					HashMap<Long, Boolean> hareketKaydiVardiyaMap = new HashMap<Long, Boolean>();
					Date donemBas = denklestirmeDonemi.getBaslangicTarih(), donemBit = denklestirmeDonemi.getBitisTarih();
					if (pdks && denklestirmeDonemi.getDenklestirmeAy() != null && denklestirmeDonemi.getDenklestirmeAyDurum()) {
						DenklestirmeAy denklestirmeAy = denklestirmeDonemi.getDenklestirmeAy();
						donemBas = PdksUtil.convertToJavaDate((denklestirmeAy.getYil() * 100 + denklestirmeAy.getAy()) + "01", "yyyyMMdd");
						donemBit = tariheGunEkleCikar(cal, tariheAyEkleCikar(cal, donemBas, 1), -1);
						HashMap fields = new HashMap();
						fields.put(PdksEntityController.MAP_KEY_SELECT, "calismaModeli.id");
						fields.put("denklestirmeAy.id", denklestirmeAy.getId());
						fields.put("hareketKaydiVardiyaBul", Boolean.TRUE);
						if (session != null)
							fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						List<Long> idList = pdksEntityController.getObjectByInnerObjectList(fields, CalismaModeliAy.class);
						for (Long long1 : idList)
							hareketKaydiVardiyaMap.put(long1, Boolean.TRUE);

					}
					List<VardiyaGun> vardiyaGunModelGuncelleList = new ArrayList<VardiyaGun>();
					List<Long> pdIdList = new ArrayList<Long>();
					for (Personel personel : perList) {

						PersonelDenklestirmeTasiyici personelDenklestirmeTasiyici = new PersonelDenklestirmeTasiyici();
						String donem = "";
						CalismaModeli cm = null;
						DenklestirmeAy denklestirmeAy = null;
						if (personelDenklestirmeDonemMap.containsKey(personel.getId())) {
							PersonelDenklestirme personelDenklestirme = personelDenklestirmeDonemMap.get(personel.getId());
							pdIdList.add(personelDenklestirme.getId());
							cm = personelDenklestirme.getCalismaModeli();
							denklestirmeAy = personelDenklestirme.getDenklestirmeAy();
							donem = String.valueOf(denklestirmeAy.getYil() * 100 + denklestirmeAy.getAy());
							if (personelDenklestirme.getCalismaModeliAy() != null)
								personelDenklestirmeTasiyici.setCalismaModeliAy(personelDenklestirme.getCalismaModeliAy());
						}
						personelDenklestirmeTasiyici.setDenklestirmeAy(denklestirmeDonemi.getDenklestirmeAy());
						personelDenklestirmeTasiyici.setPersonel(personel);
						personelDenklestirmeTasiyici.setGenelHaftaMap((TreeMap<String, Integer>) genelHaftaMap.clone(), tatilGunleriMap);
						if (personelDenklestirmeTasiyici.getVardiyaGunleriMap() != null && !personelDenklestirmeTasiyici.getVardiyaGunleriMap().isEmpty()) {
							for (Iterator<VardiyaGun> iterator2 = vardiyaDblar.iterator(); iterator2.hasNext();) {
								VardiyaGun vardiyaGun = iterator2.next();
								if (vardiyaGun == null)
									continue;

								String key = PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), "yyyyMMdd");
								vardiyaGun.setAyinGunu(donem.length() > 0 && key.startsWith(donem));
								if (cm != null && vardiyaGun.isAyinGunu())
									vardiyaGun.setCalismaModeli(cm);
								else if (denklestirmeAy != null && vardiyaGun.getVardiya() != null) {
									vardiyaGunModelGuncelleList.add(vardiyaGun);
								}
								vardiyaGun.setZamanGuncelle(zamanGuncelle);
								vardiyaTarihMap.put(key, vardiyaGun);
								if (vardiyaGun.getPersonel().getId().equals(personel.getId())) {
									if (tatilGunleriMap.containsKey(key))
										vardiyaGun.setTatil(tatilGunleriMap.get(key));
									else if (vardiyaGun.getVardiya() != null && vardiyaGun.getVardiya().isCalisma()) {
										vardiyaGun.setVardiyaZamani();
										Vardiya vardiya = vardiyaGun.getIslemVardiya();
										String key1 = PdksUtil.convertToDateString(vardiya.getVardiyaBitZaman(), "yyyyMMdd");
										if (!key1.equals(key) && tatilGunleriMap.containsKey(key1)) {
											Tatil pdksTatil = tatilGunleriMap.get(key1);
											if (pdksTatil != null) {
												Tatil tatil = (Tatil) pdksTatil.getOrjTatil().clone();
												Date bayramBas = tatil.getBasTarih();
												if (vardiya.getVardiyaBitZaman().getTime() > bayramBas.getTime()) {
													tatil = (Tatil) pdksTatil.clone();
													tatil.setId(-vardiyaGun.getId());
													vardiyaGun.setTatil(tatil);
												} else
													tatil = null;
											}

										}

									}
									if (personelDenklestirmeTasiyici.getVardiyaGunleriMap().containsKey(key))
										personelDenklestirmeTasiyici.getVardiyaGunleriMap().put(key, vardiyaGun);
									personelDenklestirmeTasiyici.setVardiyaGun(vardiyaGun);
									iterator2.remove();
								}

							}

							ArrayList<VardiyaGun> varList = new ArrayList<VardiyaGun>(personelDenklestirmeTasiyici.getVardiyaGunleriMap().values());
							for (Iterator iterator = varList.iterator(); iterator.hasNext();) {
								VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
								vardiyaGun.setGuncellendi(Boolean.FALSE);
								vardiyaGun.setZamanGuncelle(zamanGuncelle);
								vardiyaGun.setVardiyaZamani();
								String key = vardiyaGun.getPersonel().getId() + "_" + getHafta(vardiyaGun.getVardiyaDate());
								if (vardiyaHaftaMap.containsKey(key))
									vardiyaGun.setVardiyaSablonu(vardiyaHaftaMap.get(key).getVardiyaSablonu());
								else if (vardiyaGun.getVardiyaSablonu() == null)
									vardiyaGun.setVardiyaSablonu(vardiyaGun.getPersonel().getSablon());
							}
							calismaPlaniMap.put(personel.getId(), varList);
							vardiyalar.addAll(varList);
						}
						if (personelDenklestirmeTasiyici.getCalismaModeli() != null && hareketKaydiVardiyaMap.containsKey(personelDenklestirmeTasiyici.getCalismaModeli().getId()) && calismaPlaniMap.containsKey(personel.getId())) {
							List<VardiyaGun> varList = calismaPlaniMap.get(personel.getId()), saveList = new ArrayList<VardiyaGun>();
							for (VardiyaGun vardiyaGun : varList) {
								Date vd = vardiyaGun.getVardiyaDate();
								vardiyaGun.setAyinGunu(!(vd.before(donemBas) || vd.after(donemBit)));
								if (vardiyaGun.isIzinli() == false) {
									if ((vardiyaGun.getId() == null || vardiyaGun.getVersion() < 0 || !vardiyaGun.getDurum()) && vardiyaGun.getVardiya() != null && vardiyaGun.isAyinGunu() && vd.before(bugun)) {
										saveList.add(vardiyaGun);
									}
								}
							}
							if (!saveList.isEmpty()) {
								boolean flush = false;
								for (Iterator iterator = saveList.iterator(); iterator.hasNext();) {
									VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
									if (!vardiyaGun.getVardiya().isHaftaTatil()) {
										vardiyaGun.setVersion(-1);
										vardiyaGun.setDurum(Boolean.FALSE);
									} else
										iterator.remove();
									if (vardiyaGun.getId() == null) {
										flush = true;
										pdksEntityController.saveOrUpdate(session, entityManager, vardiyaGun);
									}
								}
								if (flush)
									session.flush();

							}
							if (!saveList.isEmpty())
								personelVardiyaBulMap.put(personel.getId(), saveList);
							else
								saveList = null;
						}
						personelDenklestirmeMap.put(personel.getId(), personelDenklestirmeTasiyici);
					}

					if (!vardiyaGunModelGuncelleList.isEmpty())
						sonrakiGunVardiyalariAyikla(null, vardiyaGunModelGuncelleList, session);

					vardiyaGunModelGuncelleList = null;
					Date tarih1 = tariheGunEkleCikar(cal, denklestirmeDonemi.getBaslangicTarih(), -1);
					Date tarih2 = tariheGunEkleCikar(cal, denklestirmeDonemi.getBitisTarih(), 1);

					// Fazla mesailer bulunuyor
					// Personel Hareketler personel bazli dolduruluyor

					HashMap<Long, ArrayList<HareketKGS>> personelHareketMap = personelHareketleriGetir(kgsPerList, tariheGunEkleCikar(cal, tarih1, -1), tariheGunEkleCikar(cal, tarih2, 1), session);
					if (!personelVardiyaBulMap.isEmpty() && !personelHareketMap.isEmpty()) {
						yenidenCalistir = vardiyaHareketlerdenGuncelle(session, personelDenklestirmeMap, personelVardiyaBulMap, calismaPlaniMap, hareketKaydiVardiyaMap, personelHareketMap);
						TreeMap<String, VardiyaGun> vardiyalarMap = new TreeMap<String, VardiyaGun>();
						for (Long key : calismaPlaniMap.keySet()) {
							List<VardiyaGun> list = calismaPlaniMap.get(key);
							for (VardiyaGun vardiyaGun : list) {
								vardiyalarMap.put(vardiyaGun.getVardiyaKeyStr(), vardiyaGun);
							}
						}
						fazlaMesaiSaatiAyarla(vardiyalarMap);
						vardiyalarMap = null;
					}

					personelVardiyaBulMap = null;
					List<YemekIzin> yemekList = getYemekList(tarih2, tarih2, session);
					List<PersonelFazlaMesai> fazlaMesailer = denklestirmeFazlaMesaileriGetir(loginUser, denklestirmeDonemi != null ? denklestirmeDonemi.getDenklestirmeAy() : null, vardiyalar, session);
					HashMap<String, KapiView> manuelKapiMap = getManuelKapiMap(null, session);
					KapiView girisView = manuelKapiMap != null ? manuelKapiMap.get(Kapi.TIPI_KODU_GIRIS) : null;
					if (girisView == null)
						girisView = getKapiView(map);
					Tanim neden = null;
					User sistemUser = null;
					if (PdksUtil.isSistemDestekVar()) {
						neden = getOtomatikKapGirisiNeden(session);
						if (neden != null)
							sistemUser = getSistemAdminUser(session);
					}

					Long perNoId = null;
					for (Iterator<Long> iterator2 = personelDenklestirmeMap.keySet().iterator(); iterator2.hasNext();) {
						perNoId = iterator2.next();
						Long kgsId = personelDenklestirmeMap.get(perNoId).getPersonel().getPersonelKGS().getId();
						PersonelDenklestirmeTasiyici personelDenklestirme = personelDenklestirmeMap.get(perNoId);
						boolean ayinGunumu = false, ayBasladi = false;
						String ilkgun = "01";
						if (denklestirmeDonemi.getBaslangicTarih().before(personelDenklestirme.getPersonel().getIseGirisTarihi()))
							ilkgun = PdksUtil.convertToDateString(personelDenklestirme.getPersonel().getIseGirisTarihi(), "dd");
						if (personelDenklestirme.getVardiyaGunleriMap() != null) {
							TreeMap<String, VardiyaGun> vardiyaGunleriMap = personelDenklestirme.getVardiyaGunleriMap();
							for (String key : vardiyaGunleriMap.keySet()) {
								VardiyaGun vardiyaGun = vardiyaGunleriMap.get(key);
								String gun = key.substring(6);
								if (gun.equals(ilkgun)) {
									ayinGunumu = !ayBasladi;
									ayBasladi = true;
									ilkgun = "01";
								}
								boolean ayinGunu = gunMap.containsKey(key) ? gunMap.get(key) : ayinGunumu;
								if (!gunMap.containsKey(key))
									gunMap.put(key, ayinGunu);
								vardiyaGun.setAyinGunu(ayinGunu);

							}
						}

						List<HareketKGS> perHareketList = personelHareketMap.containsKey(kgsId) ? personelHareketMap.get(kgsId) : new ArrayList<HareketKGS>();
						// Denklestirme islemleri yapiliyor
						List<PersonelIzin> izinler = izinMap.containsKey(perNoId) ? izinMap.get(perNoId) : null;
						LinkedHashMap<String, Object> denklestirmeOlusturMap = new LinkedHashMap<String, Object>();
						denklestirmeOlusturMap.put("neden", neden);
						denklestirmeOlusturMap.put("sistemUser", sistemUser);
						denklestirmeOlusturMap.put("loginUser", loginUser);
						denklestirmeOlusturMap.put("manuelKapiMap", manuelKapiMap);
						denklestirmeOlusturMap.put("gunMap", gunMap);
						denklestirmeOlusturMap.put("hareketEkle", tarihHareketEkle);
						denklestirmeOlusturMap.put("yemekAraliklari", yemekAraliklari);
						denklestirmeOlusturMap.put("girisView", girisView);
						denklestirmeOlusturMap.put("personelDenklestirmeTasiyiciList", personelDenklestirmeTasiyiciList);
						denklestirmeOlusturMap.put("tatilGunleriMap", tatilGunleriMap);
						denklestirmeOlusturMap.put("personelDenklestirmeMap", personelDenklestirmeMap);
						denklestirmeOlusturMap.put("vardiyaNetCalismaSuresiMap", vardiyaNetCalismaSuresiMap);
						denklestirmeOlusturMap.put("izinler", izinler);
						denklestirmeOlusturMap.put("fazlaMesailer", fazlaMesailer);
						denklestirmeOlusturMap.put("calismaPlaniMap", calismaPlaniMap);
						denklestirmeOlusturMap.put("perHareketList", perHareketList);
						denklestirmeOlusturMap.put("perNoId", perNoId);
						denklestirmeOlusturMap.put("yemekList", yemekList);
						denklestirmeOlustur(mapBosVeriSil(denklestirmeOlusturMap, "denklestirmeOlustur"), session);

						personelDenklestirme.setToplamCalisilacakZaman(0);
						personelDenklestirme.setToplamCalisilanZaman(0);
						for (Iterator<PersonelDenklestirmeTasiyici> iterator = personelDenklestirme.getPersonelDenklestirmeleri().iterator(); iterator.hasNext();) {
							PersonelDenklestirmeTasiyici denklestirme = iterator.next();
							if (!denklestirme.isCheckBoxDurum())
								continue;
							personelDenklestirme.addToplamCalisilacakZaman(denklestirme.getToplamCalisilacakZaman());
							if (denklestirme.getToplamCalisilanZaman() > 0.0d)
								personelDenklestirme.addToplamCalisilanZaman(null, denklestirme.getToplamCalisilanZaman());
						}

					}

				}
			}
		}

		if (!personelDenklestirmeTasiyiciList.isEmpty()) {
			boolean durum = Boolean.FALSE;
			boolean hataYok = Boolean.TRUE;
			// Bos kayitlar siliniyor hatali kayitlar set ediliyor
			for (Iterator<PersonelDenklestirmeTasiyici> iterator = personelDenklestirmeTasiyiciList.iterator(); iterator.hasNext();) {
				PersonelDenklestirmeTasiyici personelDenklestirmeTasiyici = iterator.next();
				double normalFazlaMesai = 0, resmiTatilMesai = 0;
				personelDenklestirmeTasiyici.setCheckBoxDurum(Boolean.TRUE);
				if (personelDenklestirmeTasiyici.getDurum())
					personelDenklestirmeTasiyici.setTrClass(String.valueOf(durum));

				for (PersonelDenklestirmeTasiyici denklestirmeTasiyici : personelDenklestirmeTasiyici.getPersonelDenklestirmeleri()) {
					if (!denklestirmeTasiyici.isCheckBoxDurum())
						personelDenklestirmeTasiyici.setCheckBoxDurum(Boolean.FALSE);
					else {
						normalFazlaMesai += denklestirmeTasiyici.getCalisilanFark();
						resmiTatilMesai += denklestirmeTasiyici.getResmiTatilMesai();
					}
				}
				personelDenklestirmeTasiyici.setNormalFazlaMesai(PdksUtil.setSureDoubleTypeRounded(normalFazlaMesai, personelDenklestirmeTasiyici.getYarimYuvarla()));
				personelDenklestirmeTasiyici.setResmiTatilMesai(resmiTatilMesai);
				durum = !durum;
				if (hataYok)
					hataYok = personelDenklestirmeTasiyici.isCheckBoxDurum();
			}
		}
		gunMap = null;
		if (yenidenCalistir && denklestirmeDonemi.getDurum())
			personelDenklestirmeTasiyiciList.clear();
		return personelDenklestirmeTasiyiciList;
	}

	/**
	 * @param vardiyaGunList
	 * @param tarih
	 * @param session
	 * @return
	 */
	@Transactional
	public boolean getVardiyaHareketIslenecekList(List<VardiyaGun> vardiyaGunList, Date tarih, Session session) {
		boolean sonuc = false;
		List<VardiyaGun> vardiyaGunIslemList = new ArrayList<VardiyaGun>();
		if (!vardiyaGunList.isEmpty()) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(tarih);
			HashMap map1 = new HashMap();
			map1.put("ay", cal.get(Calendar.MONTH) + 1);
			map1.put("yil", cal.get(Calendar.YEAR));
			if (session != null)
				map1.put(PdksEntityController.MAP_KEY_SESSION, session);
			DenklestirmeAy denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(map1, DenklestirmeAy.class);
			if (denklestirmeAy != null && denklestirmeAy.getDurum()) {
				HashMap fields = new HashMap();
				fields.put(PdksEntityController.MAP_KEY_SELECT, "calismaModeli.id");
				fields.put("denklestirmeAy.id", denklestirmeAy.getId());
				fields.put("hareketKaydiVardiyaBul", Boolean.TRUE);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<Long> idList = pdksEntityController.getObjectByInnerObjectList(fields, CalismaModeliAy.class);
				if (!idList.isEmpty()) {
					boolean flush = false;
					for (VardiyaGun vardiyaGun : vardiyaGunList) {
						if (vardiyaGun.getId() == null) {
							CalismaModeli calismaModeli = vardiyaGun.getPdksPersonel().getCalismaModeli();
							if (calismaModeli != null && idList.contains(calismaModeli.getId())) {
								vardiyaGun.setDurum(!vardiyaGun.getVardiya().isCalisma());
								if (!vardiyaGun.getDurum())
									vardiyaGun.setVersion(vardiyaGun.getIzin() == null ? -1 : 0);
								pdksEntityController.saveOrUpdate(session, entityManager, vardiyaGun);
								flush = true;
							}
						}
						if (vardiyaGun.getVersion() < 0L)
							vardiyaGunIslemList.add(vardiyaGun);

					}
					if (flush)
						session.flush();
				}
			}
			if (!vardiyaGunIslemList.isEmpty())
				try {
					sonuc = vardiyaGunHareketleriGuncelle(vardiyaGunIslemList, denklestirmeAy, tarih, session);
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}
		}

		vardiyaGunIslemList = null;
		return sonuc;

	}

	/**
	 * @param vardiyaGunIslemList
	 * @param denklestirmeAy
	 * @param tarih
	 * @param session
	 * @return
	 */
	private boolean vardiyaGunHareketleriGuncelle(List<VardiyaGun> vardiyaGunIslemList, DenklestirmeAy denklestirmeAy, Date tarih, Session session) {
		boolean sonuc = false;
		TreeMap<Long, List<VardiyaGun>> personelVardiyaBulMap = new TreeMap<Long, List<VardiyaGun>>();
		TreeMap<Long, PersonelDenklestirmeTasiyici> personelDenklestirmeMap = null;
		TreeMap<Long, PersonelDenklestirme> denkMap = new TreeMap<Long, PersonelDenklestirme>();
		HashMap fields = new HashMap();
		fields.put("denklestirmeAy.id", denklestirmeAy.getId());
		fields.put("hareketKaydiVardiyaBul", Boolean.TRUE);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<CalismaModeliAy> cmaList = pdksEntityController.getObjectByInnerObjectList(fields, CalismaModeliAy.class);
		HashMap<Long, CalismaModeliAy> cmaMap = new HashMap<Long, CalismaModeliAy>();
		for (CalismaModeliAy calismaModeliAy : cmaList)
			cmaMap.put(calismaModeliAy.getCalismaModeli().getId(), calismaModeliAy);
		cmaList = null;
		for (Iterator iterator = vardiyaGunIslemList.iterator(); iterator.hasNext();) {
			VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
			if (vardiyaGun.getVardiya() == null || vardiyaGun.getVardiya().isCalisma() == false || vardiyaGun.getVersion() >= 0L)
				iterator.remove();
			else {
				Personel personel = vardiyaGun.getPdksPersonel();
				List<VardiyaGun> list = personelVardiyaBulMap.containsKey(personel.getId()) ? personelVardiyaBulMap.get(personel.getId()) : new ArrayList<VardiyaGun>();
				if (list.isEmpty()) {
					Long cmId = personel.getCalismaModeli() != null ? personel.getCalismaModeli().getId() : null;
					personelVardiyaBulMap.put(personel.getId(), list);
					if (cmId != null && cmaMap.containsKey(cmId)) {
						PersonelDenklestirme personelDenklestirme = new PersonelDenklestirme(personel, denklestirmeAy, cmaMap.get(cmId));
						denkMap.put(personel.getId(), personelDenklestirme);
					}
				}
				list.add(vardiyaGun);
			}
		}
		Calendar cal = Calendar.getInstance();
		cmaMap = null;
		if (!personelVardiyaBulMap.isEmpty()) {
			List<Long> perIdList = new ArrayList(personelVardiyaBulMap.keySet());
			List idList = new ArrayList(perIdList);
			String fieldName = "personel.id";
			HashMap map1 = new HashMap();
			map1.put("denklestirmeAy.id", denklestirmeAy.getId());
			map1.put(fieldName, idList);
			if (session != null)
				map1.put(PdksEntityController.MAP_KEY_SESSION, session);
			// List<PersonelDenklestirme> personelDenklestirmeList = pdksEntityController.getObjectByInnerObjectList(map1, PersonelDenklestirme.class);
			List<PersonelDenklestirme> personelDenklestirmeList = getParamList(false, idList, fieldName, fields, PersonelDenklestirme.class, session);
			if (!denkMap.isEmpty() || !personelDenklestirmeList.isEmpty()) {
				for (PersonelDenklestirme personelDenklestirme : personelDenklestirmeList) {
					Personel personel = personelDenklestirme.getPdksPersonel();
					if (denkMap.containsKey(personel.getId()))
						denkMap.put(personel.getId(), personelDenklestirme);
				}
				personelDenklestirmeList = new ArrayList<PersonelDenklestirme>(denkMap.values());
			}
			denkMap = null;
			if (!personelDenklestirmeList.isEmpty()) {
				HashMap<Long, Boolean> hareketKaydiVardiyaMap = new HashMap<Long, Boolean>();
				fields.clear();
				fields.put(PdksEntityController.MAP_KEY_SELECT, "calismaModeli.id");
				fields.put("denklestirmeAy.id", denklestirmeAy.getId());
				fields.put("hareketKaydiVardiyaBul", Boolean.TRUE);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<Long> idLongList = pdksEntityController.getObjectByInnerObjectList(fields, CalismaModeliAy.class);
				for (Long long1 : idLongList)
					hareketKaydiVardiyaMap.put(long1, Boolean.TRUE);
				personelDenklestirmeMap = new TreeMap<Long, PersonelDenklestirmeTasiyici>();
				ArrayList<Personel> tumPersoneller = new ArrayList<Personel>();
				for (PersonelDenklestirme personelDenklestirme : personelDenklestirmeList) {
					Personel personel = personelDenklestirme.getPdksPersonel();
					CalismaModeli calismaModeli = personelDenklestirme.getCalismaModeliAy() != null ? personelDenklestirme.getCalismaModeli() : personel.getCalismaModeli();
					if (calismaModeli != null && hareketKaydiVardiyaMap.containsKey(calismaModeli.getId())) {
						PersonelDenklestirmeTasiyici personelDenklestirmeTasiyici = new PersonelDenklestirmeTasiyici();
						personelDenklestirmeTasiyici.setPersonel(personel);
						personelDenklestirmeTasiyici.setVardiyaGunleriMap(new TreeMap<String, VardiyaGun>());
						tumPersoneller.add(personel);
						personelDenklestirmeTasiyici.setDenklestirmeAy(denklestirmeAy);
						personelDenklestirmeTasiyici.setCalismaModeli(calismaModeli);
						personelDenklestirmeMap.put(personelDenklestirme.getPdksPersonel().getId(), personelDenklestirmeTasiyici);
					}
				}
				if (!personelDenklestirmeMap.isEmpty()) {
					HashMap<Long, ArrayList<VardiyaGun>> calismaPlaniMap = new HashMap<Long, ArrayList<VardiyaGun>>();
					for (Long key : perIdList) {
						if (!personelDenklestirmeMap.containsKey(key))
							personelVardiyaBulMap.remove(key);
						else {
							calismaPlaniMap.put(key, new ArrayList<VardiyaGun>(personelVardiyaBulMap.get(key)));
						}
					}
					List<Long> kapiIdler = getPdksDonemselKapiIdler(tarih, tarih, session);
					List<HareketKGS> kgsList = null;
					try {
						if (kapiIdler != null && !kapiIdler.isEmpty())
							kgsList = getPdksHareketBilgileri(Boolean.TRUE, kapiIdler, (List<Personel>) tumPersoneller.clone(), tariheGunEkleCikar(cal, tarih, -1), tariheGunEkleCikar(cal, tarih, 1), HareketKGS.class, session);

					} catch (Exception e) {
					}
					if (kgsList == null)
						kgsList = new ArrayList<HareketKGS>();
					HashMap<Long, ArrayList<HareketKGS>> personelHareketMap = new HashMap<Long, ArrayList<HareketKGS>>();
					if (!kgsList.isEmpty()) {
						if (kgsList.size() > 1)
							kgsList = PdksUtil.sortListByAlanAdi(kgsList, "zaman", Boolean.FALSE);
						for (HareketKGS hareketKGS : kgsList) {
							Long key = hareketKGS.getPersonelId();
							ArrayList<HareketKGS> list = personelHareketMap.containsKey(key) ? personelHareketMap.get(key) : new ArrayList<HareketKGS>();
							if (list.isEmpty())
								personelHareketMap.put(key, list);
							list.add(hareketKGS);
						}
						try {
							sonuc = vardiyaHareketlerdenGuncelle(session, personelDenklestirmeMap, personelVardiyaBulMap, calismaPlaniMap, hareketKaydiVardiyaMap, personelHareketMap);
						} catch (Exception e) {
							logger.error(e);
							e.printStackTrace();
						}
					}
				}
			}
		}
		return sonuc;
	}

	/**
	 * @param departman
	 * @param session
	 * @return
	 */
	public List<Vardiya> getVardiyaList(Departman departman, Session session) {
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT S.* from " + Vardiya.TABLE_NAME + " S WITH(nolock) ");
		sb.append(" WHERE (S." + Vardiya.COLUMN_NAME_DEPARTMAN + " IS NULL  OR S." + Vardiya.COLUMN_NAME_DEPARTMAN + " = :deptId )");
		sb.append(" AND  S." + Vardiya.COLUMN_NAME_KISA_ADI + " <> '' AND S." + Vardiya.COLUMN_NAME_DURUM + " = 1  AND COALESCE(S." + Vardiya.COLUMN_NAME_ISKUR + ",0)<>1");
		fields.put("deptId", departman.getId());
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Vardiya> vardiyaList = pdksEntityController.getObjectBySQLList(sb, fields, Vardiya.class);
		for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
			Vardiya vardiya = (Vardiya) iterator.next();
			if (!vardiya.isCalisma())
				iterator.remove();
		}
		return vardiyaList;
	}

	/**
	 * TODO Hareketlere göre vardiya planı güncellemesi
	 * 
	 * @param session
	 * @param personelDenklestirmeMap
	 * @param personelVardiyaBulMap
	 * @param calismaPlaniMap
	 * @param hareketKaydiVardiyaMap
	 * @param departman
	 * @param personelHareketMap
	 */
	@Transactional
	public boolean vardiyaHareketlerdenGuncelle(Session session, TreeMap<Long, PersonelDenklestirmeTasiyici> personelDenklestirmeMap, TreeMap<Long, List<VardiyaGun>> personelVardiyaBulMap, HashMap<Long, ArrayList<VardiyaGun>> calismaPlaniMap, HashMap<Long, Boolean> hareketKaydiVardiyaMap,
			HashMap<Long, ArrayList<HareketKGS>> personelHareketMap) {
		boolean yenidenCalistir = false;
		HashMap fields = new HashMap();
		TreeMap<Long, List<Vardiya>> vMap = new TreeMap<Long, List<Vardiya>>();
		List idList = new ArrayList(hareketKaydiVardiyaMap.keySet());
		String fieldName = "calismaModeli.id";
		fields.clear();
		fields.put(fieldName, idList);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		// List<CalismaModeliVardiya> calismaModeliVardiyaList = pdksEntityController.getObjectByInnerObjectList(fields, CalismaModeliVardiya.class);
		List<CalismaModeliVardiya> calismaModeliVardiyaList = getParamList(false, idList, fieldName, fields, CalismaModeliVardiya.class, session);
		HashMap<Long, List<Vardiya>> calismaModeliVardiyaMap = new HashMap<Long, List<Vardiya>>();
		for (CalismaModeliVardiya calismaModeliVardiya : calismaModeliVardiyaList) {
			if (calismaModeliVardiya.getVardiya().getDurum()) {
				Long key = calismaModeliVardiya.getCalismaModeli().getId();
				List<Vardiya> list1 = calismaModeliVardiyaMap.containsKey(key) ? calismaModeliVardiyaMap.get(key) : new ArrayList<Vardiya>();
				if (list1.isEmpty())
					calismaModeliVardiyaMap.put(key, list1);
				list1.add(calismaModeliVardiya.getVardiya());
			}
		}
		HashMap map = new HashMap();
		map.put("vardiyaTipi", Vardiya.TIPI_OFF);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		Vardiya offVardiya = (Vardiya) pdksEntityController.getObjectByInnerObject(map, Vardiya.class);
		List<HareketKGS> personelHareketList = new ArrayList<HareketKGS>();
		Date bugun = new Date();
		List<String> hareketIdList = new ArrayList<String>();
		for (Long perId : personelVardiyaBulMap.keySet()) {
			PersonelDenklestirmeTasiyici personelDenklestirmeTasiyici = personelDenklestirmeMap.get(perId);
			Personel personel = personelDenklestirmeTasiyici.getPersonel();
			Long personelKGSId = personel.getPersonelKGS().getId();
			Departman departman = personel.getSirket().getDepartman();
			boolean flush = false;
			List<VardiyaGun> vardiyaGunList = calismaPlaniMap.get(perId);
			TreeMap<String, VardiyaGun> vgMap = new TreeMap<String, VardiyaGun>();
			for (VardiyaGun vardiyaGun : vardiyaGunList) {
				String key = PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), "yyyyMMdd");
				vgMap.put(key, vardiyaGun);
			}

			if (personelHareketMap.containsKey(personelKGSId)) {
				List<VardiyaGun> varList = new ArrayList<VardiyaGun>(personelVardiyaBulMap.get(perId));
				Collections.reverse(varList);
				TreeMap<String, VardiyaGun> vardiyalarMap = new TreeMap<String, VardiyaGun>();
				for (Iterator iterator = varList.iterator(); iterator.hasNext();) {
					VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
					vardiyalarMap.put(vardiyaGun.getVardiyaKeyStr(), vardiyaGun);
				}
				List<Vardiya> vardiyaPerList = new ArrayList<Vardiya>();
				Long cmId = personelDenklestirmeTasiyici.getCalismaModeli().getId();

				List<Vardiya> vardiyaList = vMap.containsKey(departman.getId()) ? vMap.get(departman.getId()) : null;
				if (vardiyaList == null) {
					vardiyaList = getVardiyaList(departman, session);
					vMap.put(departman.getId(), vardiyaList);
				}
				if (!calismaModeliVardiyaMap.containsKey(cmId)) {
					if (!vardiyaList.isEmpty())
						calismaModeliVardiyaMap.put(cmId, vardiyaList);
				}
				if (calismaModeliVardiyaMap.containsKey(cmId))
					vardiyaPerList.addAll(calismaModeliVardiyaMap.get(cmId));

				if (!vardiyaPerList.isEmpty()) {
					for (Iterator iterator = varList.iterator(); iterator.hasNext();) {
						VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
						vardiyaGun.setGuncellendi(Boolean.FALSE);
						if (vardiyaGun.isIzinli())
							continue;
						Tatil tatil = vardiyaGun.getTatil();
						String vardiyaKeyStr = vardiyaGun.getVardiyaKeyStr();
						Vardiya islemVardiyaGun = vardiyaGun.getIslemVardiya();
						if (vardiyaGun.getVardiya().isCalisma() == false) {
							vardiyaGun.setVersion(0);
							pdksEntityController.saveOrUpdate(session, entityManager, vardiyaGun);
							vardiyaGun.setGuncellendi(Boolean.TRUE);
							flush = true;
						} else {
							String key = PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), "yyyyMMdd");
							List<Liste> listeler = new ArrayList<Liste>();
							boolean hareketVar = false;
							for (Vardiya vardiya : vardiyaPerList) {
								personelHareketList.clear();
								personelHareketList.addAll(personelHareketMap.get(personelKGSId));
								VardiyaGun vardiyaGunNew = new VardiyaGun(personelDenklestirmeTasiyici.getPersonel(), vardiya, vardiyaGun.getVardiyaDate());
								vardiyaGunNew.setVersion(-1);
								vardiyalarMap.put(vardiyaGunNew.getVardiyaKeyStr(), vardiyaGunNew);
								fazlaMesaiSaatiAyarla(vardiyalarMap);
								// vardiyaGunNew.setVardiyaZamani();
								for (Iterator iterator1 = personelHareketList.iterator(); iterator1.hasNext();) {
									HareketKGS hareket = (HareketKGS) iterator1.next();
									if (!hareketIdList.contains(hareket.getId()) && vardiyaGunNew.addHareket(hareket, Boolean.TRUE))
										iterator1.remove();

								}
								if (!hareketVar)
									hareketVar = vardiyaGunNew.getHareketler() != null;
								if (vardiyaGunNew.getHareketler() != null && vardiyaGunNew.getHareketDurum()) {

									List<HareketKGS> girisler = vardiyaGunNew.getGirisHareketleri(), cikislar = vardiyaGunNew.getCikisHareketleri();
									int girisAdet = girisler != null ? girisler.size() : 0;
									int cikisAdet = cikislar != null ? cikislar.size() : 0;
									if (girisAdet > 0) {
										Vardiya islemVardiya = vardiyaGunNew.getIslemVardiya();
										if (cikisAdet == girisAdet) {
											double sure = 0.0d;
											for (int i = 0; i < girisler.size(); i++) {
												HareketKGS giris = girisler.get(i), cikis = cikislar.get(i);
												if (giris != null && cikis != null && giris.getZaman().before(cikis.getZaman()))
													sure += PdksUtil.setSureDoubleTypeRounded(PdksUtil.getSaatFarki(cikis.getZaman(), giris.getZaman()).doubleValue(), vardiyaGunNew.getYarimYuvarla());
											}
											if (sure > 0.0d) {
												vardiyaGunNew.setVersion(0);
												listeler.add(new Liste(vardiyaGunNew, sure));
											}

										} else if (islemVardiya != null && girisAdet == 1 && cikisAdet == 0) {
											if (islemVardiya.getVardiyaBitZaman().after(bugun) && islemVardiya.getVardiyaBasZaman().before(bugun)) {
												HareketKGS girisHareketKGS = girisler.get(0);
												double sure = PdksUtil.setSureDoubleTypeRounded(PdksUtil.getSaatFarki(islemVardiya.getVardiyaBitZaman(), girisHareketKGS.getZaman()).doubleValue(), vardiyaGunNew.getYarimYuvarla());
												if (sure > 0.0d) {
													vardiyaGunNew.setVersion(-1);
													listeler.add(new Liste(vardiyaGunNew, sure));
												}

											}
										}
									}
								}
							}
							if (!listeler.isEmpty()) {
								if (listeler.size() > 1)
									listeler = PdksUtil.sortListByAlanAdi(listeler, "value", true);
								VardiyaGun vg = (VardiyaGun) listeler.get(0).getId();
								for (HareketKGS hareket : vg.getHareketler()) {
									hareketIdList.add(hareket.getId());
								}
								vardiyaGun.setVardiya(vg.getVardiya());
								vardiyaGun.setVersion(vg.getVersion());
								pdksEntityController.saveOrUpdate(session, entityManager, vardiyaGun);
								vardiyaGun.setGuncellendi(Boolean.TRUE);
								personelDenklestirmeTasiyici.getVardiyaGunleriMap().put(key, vardiyaGun);
								vgMap.put(key, vardiyaGun);
								flush = true;

							} else {
								try {
									if (tatil != null && tatil.isYarimGunMu() == false && hareketVar == false) {
										if (islemVardiyaGun != null && islemVardiyaGun.isCalisma() && islemVardiyaGun.getVardiyaFazlaMesaiBitZaman().before(bugun)) {
											vardiyaGun.setVardiya(offVardiya);
											vardiyaGun.setVersion(0);
											pdksEntityController.saveOrUpdate(session, entityManager, vardiyaGun);
											vardiyaGun.setGuncellendi(Boolean.TRUE);
											personelDenklestirmeTasiyici.getVardiyaGunleriMap().put(key, vardiyaGun);
											vgMap.put(key, vardiyaGun);
											flush = true;
										}
									}
								} catch (Exception e) {

								}
							}
						}
						vardiyalarMap.put(vardiyaKeyStr, vardiyaGun);
					}

				}
			}
			if (flush) {
				ArrayList<VardiyaGun> vardiyalar = new ArrayList<VardiyaGun>(vgMap.values());
				calismaPlaniMap.put(perId, vardiyalar);
				personelDenklestirmeTasiyici.setVardiyalar(vardiyalar);
				session.flush();
				yenidenCalistir = true;
			}

		}
		return yenidenCalistir;
	}

	/**
	 * @param izin
	 * @param onaylamamaNeden
	 * @param onaylamamaNedenAciklama
	 * @param session
	 */
	@Transactional
	public void izinIptal(PersonelIzin izin, Tanim onaylamamaNeden, String onaylamamaNedenAciklama, Session session) {
		HashMap parametreMap = new HashMap();
		parametreMap.put("id", authenticatedUser.getId());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		User updateUser = (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);
		session.refresh(izin);
		Set<PersonelIzinOnay> list = izin.getOnaylayanlar();
		if (list != null) {
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				PersonelIzinOnay personelIzinOnay = (PersonelIzinOnay) iterator.next();
				if (personelIzinOnay.getOnaylayanTipi().equals(PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI1)) {
					if (onaylamamaNeden == null) {
						HashMap paramMap = new HashMap();
						paramMap.put("kodu", "00");
						paramMap.put("tipi", Tanim.TIPI_ONAYLAMAMA_NEDEN);
						paramMap.put("durum", Boolean.TRUE);
						if (session != null)
							paramMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						onaylamamaNeden = (Tanim) pdksEntityController.getObjectByInnerObject(paramMap, Tanim.class);
					}
					personelIzinOnay.setOnaylamamaNeden(onaylamamaNeden);
					personelIzinOnay.setOnaylamamaNedenAciklama(onaylamamaNedenAciklama);
					personelIzinOnay.setGuncellemeTarihi(new Date());
					personelIzinOnay.setOnayDurum(PersonelIzinOnay.ONAY_DURUM_RED);
					pdksEntityController.saveOrUpdate(session, entityManager, personelIzinOnay);
					break;
				}
			}
		}
		izin.setGuncelleyenUser(updateUser);
		izin.setGuncellemeTarihi(new Date());
		if (izin.getIzinTipi().getHesapTipi() != null)
			izin.setHesapTipi(izin.getIzinTipi().getHesapTipi());
		else if (izin.getHesapTipi() != null && izin.getHesapTipi() > 2)
			izin.setHesapTipi(5 - izin.getHesapTipi());
		izin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_REDEDILDI);
		pdksEntityController.saveOrUpdate(session, entityManager, izin);
		session.flush();

	}

	/**
	 * @param user
	 * @param sicilNo
	 */
	private void yetkiEkle(User user, String sicilNo) {
		if (PdksUtil.hasStringValue(sicilNo)) {
			ArrayList<String> list = user.getYetkiliPersonelNoList();
			list.add(sicilNo);
			user.setYetkiliPersonelNoList(list);
		}
	}

	/**
	 * @param menuAdi
	 * @return
	 */
	public String getCalistiMenuAdi(String menuAdi) {
		String menuTanimAdi = null;
		if (menuAdi != null && menuItemMap != null) {
			if (menuItemMap.containsKey(menuAdi)) {
				menuTanimAdi = menuItemMap.get(menuAdi).getDescription().getAciklama();
			} else if (menuAdi.equalsIgnoreCase("anasayfa"))
				menuTanimAdi = "Ana Sayfa";
		}
		return menuTanimAdi;
	}

	/**
	 * @return
	 */
	public boolean getTestDurum() {
		boolean test = PdksUtil.getTestDurum();
		return test;
	}

	/**
	 * @return
	 */
	public String getHostName() {
		String str = PdksUtil.getHostName(true);
		return str;
	}

	/**
	 * @param sessionx
	 * @param menuAdi
	 * @return
	 */
	public String getMenuUserAdi(Session sessionx, String menuAdi) {
		boolean logYaz = authenticatedUser.getCalistigiSayfa() == null || !authenticatedUser.getCalistigiSayfa().equals(menuAdi);
		String menuTanimAdi = getMenuUserLogAdi(sessionx, menuAdi, logYaz);
		return menuTanimAdi;
	}

	/**
	 * @param sessionx
	 * @param menuAdi
	 * @return
	 */
	@Transactional
	public String getMenuUserLogAdi(Session sessionx, String menuAdi, boolean logYaz) {
		String menuTanimAdi = null;
		if (menuItemMap.containsKey(menuAdi) || menuAdi.equals("anaSayfa")) {
			menuTanimAdi = menuAdi.equals("anaSayfa") ? "Ana Sayfa" : menuItemMap.get(menuAdi).getDescription().getAciklama();
			if (authenticatedUser != null && (authenticatedUser.getCalistigiSayfa() == null || !menuAdi.equals(authenticatedUser.getCalistigiSayfa()))) {
				if (sessionx != null) {
					authenticatedUser.setCalistigiSayfa(menuAdi);
					if (PdksUtil.getTestDurum())
						sessionx = null;

				}
				if (authenticatedUser.isIK() || authenticatedUser.isAdmin()) {
					String mesaj = authenticatedUser.getAdSoyad() + " Sayfa : " + menuTanimAdi + " " + PdksUtil.getCurrentTimeStampStr();
					if (logYaz)
						logger.info(mesaj);
				}
				authenticatedUser.setMenuItemTime(null);
				if (sessionx != null) {
					try {
						HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
						StringBuffer sb = new StringBuffer();
						sb.append("SP_USER_MENUITEM");
						LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
						fields.put("sId", mySession.getId());
						fields.put("userName", authenticatedUser.getUsername());
						fields.put("menuAdi", menuAdi);
						fields.put(PdksEntityController.MAP_KEY_SESSION, sessionx);
						List<UserMenuItemTime> newList = pdksEntityController.execSPList(fields, sb, UserMenuItemTime.class);
						if (!newList.isEmpty()) {
							UserMenuItemTime menuItemTime = newList.get(0);
							LinkedHashMap<String, Object> map = null;
							Gson gson = new Gson();
							if (menuItemTime != null && menuItemTime.getParametreJSON() != null) {
								try {
									map = gson.fromJson(menuItemTime.getParametreJSON(), LinkedHashMap.class);
								} catch (Exception e) {
									logger.error(e);
								}
							}
							if (map == null || map.isEmpty()) {
								map = new LinkedHashMap<String, Object>();
								map.put("kullanici", authenticatedUser.getAdSoyad());
								map.put("menuAdi", getMenuUserLogAdi(null, menuAdi, false));
								String parametreJSON = gson.toJson(map);
								menuItemTime.setParametreJSON(parametreJSON);
								pdksEntityController.saveOrUpdate(sessionx, entityManager, menuItemTime);
								sessionx.flush();
							}
							authenticatedUser.setMenuItemTime(menuItemTime);
						}

					} catch (Exception e) {
						logger.error(e);
						e.printStackTrace();
					}
				}

			}

		} else if (sessionx != null)
			authenticatedUser.setCalistigiSayfa("");

		return menuTanimAdi;

	}

	/**
	 * @param menuAdi
	 * @return
	 */
	public String getMenuAdi(String menuAdi) {
		String menuTanimAdi = getMenuUserLogAdi(null, menuAdi, false);
		return menuTanimAdi;
	}

	/**
	 * @return
	 */
	public List<String> getAdminRoleList() {
		List<String> list = new ArrayList<String>();
		list.add(Role.TIPI_ADMIN);

		return list;
	}

	/**
	 * @param vekaletVeren
	 * @param session
	 * @return
	 */
	public User vekilYonetici(User vekaletVeren, Session session) {
		User vekilYonetici = null;
		if (vekaletVeren != null) {
			Date bugun = PdksUtil.buGun();
			HashMap userMap = new HashMap();
			userMap.put(PdksEntityController.MAP_KEY_SELECT, "yeniYonetici");
			userMap.put("vekaletVeren=", vekaletVeren);
			userMap.put("basTarih<=", bugun);
			userMap.put("bitTarih>=", bugun);
			if (session != null)
				userMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				vekilYonetici = (User) pdksEntityController.getObjectByInnerObjectInLogic(userMap, UserVekalet.class);
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());

				vekilYonetici = null;
			}
			if (vekilYonetici != null && vekilYonetici.getId().equals(vekaletVeren.getId()))
				vekilYonetici = null;
		}
		return vekilYonetici;

	}

	/**
	 * @param kullanici
	 * @param oldUserName
	 * @param session
	 * @return
	 */
	public User digerKullanici(User kullanici, String oldUserName, Session session) {
		User user = null;
		HashMap userMap = new HashMap();
		userMap.put("username=", kullanici.getUsername());
		if (kullanici.getId() != null)
			userMap.put("id<>", kullanici.getId());
		if (session != null)
			userMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		user = (User) pdksEntityController.getObjectByInnerObjectInLogic(userMap, User.class);
		if (user != null) {
			if (kullanici.getId() != null) {
				kullanici.setUsername(oldUserName);
				try {
					kullanici = entityManager.merge(kullanici);
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());
				}

			}
		}
		return user;
	}

	/**
	 * @return
	 */
	public List<String> getYoneticiRoleList() {
		List<String> list = new ArrayList<String>();
		list.add(Role.TIPI_YONETICI);
		list.add(Role.TIPI_YONETICI_KONTRATLI);
		return list;
	}

	/**
	 * @return
	 */
	public List<String> getSuperVisorRoleList() {
		List<String> list = new ArrayList<String>();
		list.add(Role.TIPI_SUPER_VISOR);
		return list;
	}

	/**
	 * @return
	 */
	public List<String> getSekreterRoleList() {
		List<String> list = new ArrayList<String>();
		list.add(Role.TIPI_SEKRETER);
		return list;
	}

	/**
	 * @param session
	 * @return
	 */
	public TreeMap<String, Tanim> getFazlaMesaiMap(Session session) {
		TreeMap<String, Tanim> map = new TreeMap<String, Tanim>();
		try {
			List<Tanim> list = getTanimAlanList(Tanim.TIPI_ERP_FAZLA_MESAI, "getAciklama", "S", session);
			for (Tanim tanim : list) {
				if (tanim.getDurum())
					map.put(tanim.getKodu().toUpperCase(), tanim);
			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}
		return map;
	}

	/**
	 * @param tipi
	 * @param session
	 * @return
	 */
	public List<Tanim> getTanimList(String tipi, Session session) {
		List<Tanim> list = null;
		try {
			list = getTanimAlanList(tipi, "getAciklama", "S", session);
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}
		return list;
	}

	/**
	 * @param tipi
	 * @param method
	 * @param tip
	 * @param session
	 * @return
	 */
	public List<Tanim> getTanimAlanList(String tipi, String method, String tip, Session session) {
		List<Tanim> tanimList = null;
		HashMap parametreMap = new HashMap();
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT DISTINCT V.* FROM " + Tanim.TABLE_NAME + " V WITH(nolock) ");
			sb.append(" WHERE " + Tanim.COLUMN_NAME_TIPI + " = :tipi AND " + Tanim.COLUMN_NAME_DURUM + " = 1 ");
			parametreMap.put("tipi", tipi);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			tanimList = pdksEntityController.getObjectBySQLList(sb, parametreMap, Tanim.class);
			if (tanimList.size() > 1) {
				if (tip.equals("S"))
					tanimList = PdksUtil.sortObjectStringAlanList(Constants.TR_LOCALE, tanimList, method, null);
				else
					tanimList = PdksUtil.sortListByAlanAdi(tanimList, method, Boolean.FALSE);

			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}
		return tanimList;
	}

	/**
	 * @param session
	 * @return
	 */
	public TreeMap<Long, Departman> getIzinGirenDepartmanMap(Session session) {
		TreeMap<Long, Departman> map = new TreeMap<Long, Departman>();
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT DISTINCT D.* FROM " + IzinTipi.TABLE_NAME + " I WITH(nolock) ");
		sb.append(" INNER JOIN " + Departman.TABLE_NAME + " D ON D." + Departman.COLUMN_NAME_ID + " = I.DEPARTMAN_ID AND D." + Departman.COLUMN_NAME_DURUM + " = 1 ");
		sb.append(" WHERE I." + IzinTipi.COLUMN_NAME_DURUM + " = 1 AND I." + IzinTipi.COLUMN_NAME_GIRIS_TIPI + " <> '0' AND I." + IzinTipi.COLUMN_NAME_BAKIYE_IZIN_TIPI + " IS NULL ");
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Departman> depList = pdksEntityController.getObjectBySQLList(sb, fields, Departman.class);
		for (Departman departman : depList)
			map.put(departman.getId(), departman);
		depList = null;
		return map;
	}

	/**
	 * @param user
	 * @param vekaletOku
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 */
	@Transactional
	public void sistemeGirisIslemleri(User user, boolean vekaletOku, Date basTarih, Date bitTarih, Session session) {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		if (basTarih == null)
			basTarih = Calendar.getInstance().getTime();
		if (bitTarih == null)
			bitTarih = basTarih;
		user.setRemoteAddr(PdksUtil.getRemoteAddr());
		user.setUserVekaletList(new ArrayList<User>());
		user.setSuperVisorHemsirePersonelNoList(null);
		if (user.getYetkiliPersonelNoList() == null)
			user.setYetkiliPersonelNoList(new ArrayList<String>());

		try {
			HttpServletRequest httpServletRequest = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			String url = "http://" + httpServletRequest.getServerName() + (httpServletRequest.getServerPort() != 80 ? ":" + httpServletRequest.getServerPort() : "") + (!PdksUtil.hasStringValue(httpServletRequest.getContextPath()) ? "" : httpServletRequest.getContextPath());
			PdksUtil.setUrl(url);
			PdksUtil.setHttpServletRequest(httpServletRequest);

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
		}
		setUserRoller(user, session);
		if (user.isSAPPersonel() && user.isIKAdmin())
			try {
				yoneticiIslemleri(user, 1, basTarih, bitTarih, session);
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				PdksUtil.addMessageWarn(e.getMessage());
			}

		String sicilNo = user.getStaffId();
		boolean yonetici = user.isYonetici();
		if (yonetici && user.isIK() && user.getDepartman().getId().equals(user.getPdksPersonel().getSirket().getDepartman().getId())) {
			yonetici = Boolean.FALSE;
		}
		if (yonetici) {
			List<Personel> personelList = yoneticiPersonelleri(user.getPdksPersonel().getId(), basTarih, bitTarih, session);
			TreeMap<String, Personel> personelMap = new TreeMap<String, Personel>();
			for (Iterator iterator = personelList.iterator(); iterator.hasNext();) {
				Personel personel = (Personel) iterator.next();
				try {
					if (!personel.getDurum())
						iterator.remove();
					else if (PdksUtil.hasStringValue(personel.getSicilNo()))
						personelMap.put(personel.getSicilNo(), personel);

				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());
					logger.error(e.getLocalizedMessage());
				}

			}
			if (!user.getYetkiliPersonelNoList().isEmpty()) {
				Date bugun = PdksUtil.getDate(new Date());
				for (Iterator<String> iterator = personelMap.keySet().iterator(); iterator.hasNext();) {
					String key = iterator.next();
					if (key != null && !user.getYetkiliPersonelNoList().contains(key)) {
						Personel personel = (Personel) personelMap.get(key);
						if (personel.getSirket() != null && !personel.getSirket().isErp())
							personel.setDurum(!bugun.after(personel.getSonCalismaTarihi()));
						pdksEntityController.saveOrUpdate(session, entityManager, personel);
					}
				}
				session.flush();
			} else
				user.setYetkiliPersonelNoList(new ArrayList(personelMap.keySet()));

			if (!user.isIK() && user.isDirektorSuperVisor())
				setUserSuperVisorHemsirePersonelNoList(user, session);
		}
		if (user.isIK()) {
			try {
				IKIslemleri(user, basTarih, bitTarih, session);
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
			}
		}

		if (user.isIK() || user.isAdmin()) {
			try {
				kapananSirketlerinCalisanlariniEkle(user, session);
				// baris-add -> buraya tasidim, heryerde calissin...
				if (user.isIK() || user.isAdmin()) {
					ArrayList<String> perNoList = user.getYetkiliPersonelNoList();
					for (String eskiSicilNo : user.getEskiPersonelNoList()) {
						if (perNoList.indexOf(eskiSicilNo) == -1) {
							perNoList.add(eskiSicilNo);
						}
					}
					user.setYetkiliPersonelNoList(perNoList);

				}
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
			}
		}

		if (user.isSAPPersonel() || user.isAdmin()) {
			if (user.isMudur()) {
				try {
					mudurIslemleri(user, null, Boolean.TRUE, basTarih, bitTarih, session);
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}
			} else if (user.isSekreter()) {
				try {
					sekreterIslemleri(user, basTarih, bitTarih, session);
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}
			}
			if (user.isGenelMudur() || user.isAdmin()) {
				try {
					direktorIslemleri(user, basTarih, bitTarih, session);
					// Baris add for system admin
					if (user.isAdmin()) {
						try {
							kapananSirketlerinCalisanlariniEkle(user, session);
							// baris-add -> buraya tasidim, heryerde calissin...
							if (user.isIK() || user.isAdmin()) {
								ArrayList<String> perNoList = user.getYetkiliPersonelNoList();
								for (String eskiSicilNo : user.getEskiPersonelNoList()) {
									if (perNoList.indexOf(eskiSicilNo) == -1) {
										perNoList.add(eskiSicilNo);
									}
								}
								user.setYetkiliPersonelNoList(perNoList);

							}
						} catch (Exception e) {
							logger.error("Pdks hata in : \n");
							e.printStackTrace();
							logger.error("Pdks hata out : " + e.getMessage());
						}
					}
					int level = 2;

					if (user.isGenelMudur())
						yoneticiIslemleri(user, level, basTarih, bitTarih, session);
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}
			}
			if (user.isTesisYonetici()) {
				try {
					tesisYoneticiIslemleri(user, basTarih, bitTarih, session);
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}
			}
			try {
				if (vekaletOku && !(user.isGenelMudur() || user.isAdmin() || user.isIK()))
					digerYetkileriEkle(user, basTarih, bitTarih, session);
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());

			}
		} else {
			if (user.isProjeMuduru()) {
				try {
					projeMuduruIslemleri(user, basTarih, bitTarih, session);
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}

			}
			if (user.isSuperVisor()) {
				try {
					superVisorIslemleri(user, session);
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}
			}

		}

		if (user.isYonetici() && user.getYetkiliPersonelNoList().size() > 1) {
			List<Personel> ikinciYoneticiPersonel = araIkinciYoneticiPersonel(user, session);
			user.setIkinciYoneticiPersonel(ikinciYoneticiPersonel);
		}
		if (user.getYetkiliPersonelNoList().isEmpty() && user.isSAPPersonel())
			yoneticiIslemleri(user, 1, basTarih, bitTarih, session);

		if (!user.getYetkiliPersonelNoList().contains(sicilNo))
			yetkiEkle(user, sicilNo);

		if (!(user.isIK() || user.isAdmin()) && user.isDirektorSuperVisor())
			superVisorHemsireIslemleri(user, basTarih, bitTarih, session);
		if (!(user.isSuperVisor() || user.isProjeMuduru())) {
			if (user.getYetkiTumPersonelNoList() != null && !user.getYetkiTumPersonelNoList().isEmpty()) {
				String fieldName = "s";
				HashMap fields = new HashMap();
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT P.* from " + Personel.TABLE_NAME + " P WITH(nolock) ");
				sb.append(" WHERE P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " :" + fieldName);
				List veriList = user.getYetkiTumPersonelNoList();
				fields.put(fieldName, veriList);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				// List<Personel> yetkiliPersoneller = pdksEntityController.getObjectBySQLList(sb, fields, Personel.class);
				List<Personel> yetkiliPersoneller = getSQLParamList(veriList, sb, fieldName, fields, Personel.class, session);
				long lBitTarih = bitTarih.getTime(), lBasTarih = basTarih.getTime();
				ArrayList<String> perNoList = new ArrayList<String>();
				for (Iterator iterator = yetkiliPersoneller.iterator(); iterator.hasNext();) {
					Personel personel = (Personel) iterator.next();
					try {
						if (personel.getDurum() && personel.getIseBaslamaTarihi() != null && personel.getIseBaslamaTarihi().getTime() <= lBitTarih && personel.getSonCalismaTarihi().getTime() >= lBasTarih) {
							perNoList.add(personel.getPdksSicilNo());
							continue;
						}

					} catch (Exception e) {

						e.printStackTrace();
					}
					iterator.remove();
				}
				user.getYetkiTumPersonelNoList().clear();
				if (!perNoList.isEmpty())
					user.getYetkiTumPersonelNoList().addAll(perNoList);
				perNoList = null;
				user.setYetkiliPersoneller(yetkiliPersoneller);
			}

		}

		if (user.isYonetici() && user.getYetkiliPersonelNoList().size() > 1) {
			List<Personel> ikinciYoneticiPersonel = araIkinciYoneticiPersonel(user, session);
			user.setIkinciYoneticiPersonel(ikinciYoneticiPersonel);
		} else
			user.setIkinciYoneticiPersonel(null);
		boolean izinGirebilir = false;
		if (user.getYetkiliPersoneller() != null) {
			TreeMap<Long, Departman> departmanMap = getIzinGirenDepartmanMap(session);
			if (!departmanMap.isEmpty()) {
				for (Personel personel : user.getYetkiliPersoneller()) {
					if (departmanMap.containsKey(personel.getSirket().getDepartman().getId())) {
						izinGirebilir = true;
						break;
					}
				}
			}
			departmanMap = null;
		}
		IzinTipi izinTipiSSK = null;
		if (izinGirebilir && (user.isAdmin() || user.isIK())) {
			HashMap hashMap = new HashMap();
			if (!user.isIKAdmin() && user.isIK())
				hashMap.put("departman.id=", authenticatedUser.getDepartman().getId());
			hashMap.put("izinTipiTanim.kodu like", "%I%");
			// hashMap.put("izinTipiTanim.kodu=", IzinTipi.SSK_ISTIRAHAT);
			hashMap.put("personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
			hashMap.put("durum=", Boolean.TRUE);
			if (session != null)
				hashMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			izinTipiSSK = (IzinTipi) pdksEntityController.getObjectByInnerObjectInLogic(hashMap, IzinTipi.class);
		}
		user.setIzinGirebilir(izinGirebilir);
		user.setIzinSSKGirebilir(izinTipiSSK != null);
		List izinList = getIzinOnayDurum(session, user);
		boolean izinOnaylayabilir = false;
		try {
			if (izinList != null && !izinList.isEmpty()) {
				Object[] veri = (Object[]) izinList.get(0);
				izinOnaylayabilir = veri[0] != null;
			}
		} catch (Exception e) {
		}
		user.setIzinOnaylayabilir(izinOnaylayabilir);
		izinList = null;
	}

	/**
	 * @param user
	 * @param session
	 */
	private void superVisorIslemleri(User user, Session session) {
		Date bugun = Calendar.getInstance().getTime();
		TreeMap personelMap = new TreeMap();
		List<Personel> personeller = yoneticiPersonelleri(user.getPdksPersonel().getId(), bugun, bugun, session);
		for (Personel personel : personeller)
			personelMap.put(personel.getSicilNo(), personel);

		if (!personelMap.isEmpty()) {
			user.setYetkiliPersonelNoList(new ArrayList(personelMap.keySet()));
			user.setYetkiliPersoneller(new ArrayList<Personel>(personelMap.values()));
		}
		personelMap = null;
	}

	/**
	 * @param fields
	 * @param logic
	 * @return
	 */
	public List<Personel> getPersonelList(HashMap fields, boolean logic) {
		List<Personel> perList = null;
		if (fields != null) {
			Session session = fields.containsKey(PdksEntityController.MAP_KEY_SESSION) ? (Session) fields.get(PdksEntityController.MAP_KEY_SESSION) : PdksUtil.getSessionUser(entityManager, authenticatedUser);
			fields.put(PdksEntityController.MAP_KEY_SELECT, "id");
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Long> idler = null;
			if (logic)
				idler = pdksEntityController.getObjectByInnerObjectListInLogic(fields, Personel.class);
			else
				idler = pdksEntityController.getObjectByInnerObjectList(fields, Personel.class);
			if (!idler.isEmpty())
				perList = getPersonelByIdList(idler, session);
		}
		if (perList == null)
			perList = new ArrayList<Personel>();
		return perList;
	}

	/**
	 * @param map
	 * @param logic
	 * @return
	 */
	public TreeMap getPersonelMap(HashMap map, boolean logic) {
		TreeMap personelMap = new TreeMap();
		String method = map.containsKey(PdksEntityController.MAP_KEY_MAP) ? (String) map.get(PdksEntityController.MAP_KEY_MAP) : null;
		if (method != null) {
			map.remove(PdksEntityController.MAP_KEY_MAP);
			List<Personel> perList = getPersonelList(map, logic);
			if (!perList.isEmpty()) {
				for (Personel personel : perList) {
					try {
						Object key = PdksUtil.getMethodObject(personel, method, null);
						if (key != null)
							personelMap.put(key, personel);
					} catch (Exception e) {
					}
				}
			}

		}
		return personelMap;
	}

	/**
	 * @param user
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 */
	private void superVisorHemsireIslemleri(User user, Date basTarih, Date bitTarih, Session session) {

		if (user.getSuperVisorHemsirePersonelNoList() == null || user.getSuperVisorHemsirePersonelNoList().isEmpty()) {
			HashMap fields = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " from " + Personel.TABLE_NAME + " P WITH(nolock) ");
			sb.append(" WHERE P." + Personel.COLUMN_NAME_SIRKET + " = :s AND P." + Personel.COLUMN_NAME_EK_SAHA1 + " = :e1");
			fields.put("basTarih", basTarih);
			fields.put("bitTarih", bitTarih);
			fields.put("e1", user.getPdksPersonel().getEkSaha1().getId());
			sb.append(" AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= :basTarih ");
			sb.append(" AND P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " <= :bitTarih ");
			fields.put("s", user.getPdksPersonel().getSirket().getId());
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<String> userList = pdksEntityController.getObjectBySQLList(sb, fields, null);
			TreeMap personelMap = new TreeMap();
			for (String perNo : userList) {
				try {
					if (PdksUtil.hasStringValue(perNo))
						personelMap.put(perNo.trim(), perNo.trim());

				} catch (Exception e) {

					e.printStackTrace();
				}
			}
			if (!personelMap.isEmpty())
				user.setSuperVisorHemsirePersonelNoList(new ArrayList(personelMap.keySet()));
			personelMap = null;
		}
	}

	/**
	 * @param user
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 */
	private void tesisYoneticiIslemleri(User user, Date basTarih, Date bitTarih, Session session) {
		Personel yoneticiPersonel = user.getPdksPersonel();
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT P." + Personel.COLUMN_NAME_ID + " from " + Personel.TABLE_NAME + " P WITH(nolock) ");
		sb.append(" WHERE P." + Personel.COLUMN_NAME_SIRKET + " = :s");
		sb.append(" AND P." + Personel.COLUMN_NAME_TESIS + " = :t");
		sb.append(" AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= :basTarih ");
		sb.append(" AND P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " <= :bitTarih ");
		fields.put("basTarih", basTarih);
		fields.put("bitTarih", bitTarih);
		fields.put("s", yoneticiPersonel.getSirket().getId());
		fields.put("t", yoneticiPersonel.getTesis() != null ? yoneticiPersonel.getTesis().getId() : 0L);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Personel> userList = getPersonelList(sb, fields);
		TreeMap personelMap = new TreeMap();
		personelMap.put(yoneticiPersonel.getSicilNo(), yoneticiPersonel);
		for (Personel personel : userList) {
			try {
				if (personel.getDurum())
					if (PdksUtil.hasStringValue(personel.getPdksSicilNo()))
						personelMap.put(personel.getSicilNo(), personel);

			} catch (Exception e) {
				logger.error(personel.getAdSoyad() + " " + e.getMessage());
				e.printStackTrace();
			}
		}
		if (!personelMap.isEmpty()) {
			user.setYetkiliPersonelNoList(new ArrayList(personelMap.keySet()));
			user.setYetkiliPersoneller(new ArrayList<Personel>(personelMap.values()));
		}
		personelMap = null;

	}

	/**
	 * @param user
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 */
	private void projeMuduruIslemleri(User user, Date basTarih, Date bitTarih, Session session) {
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT P." + Personel.COLUMN_NAME_ID + " from " + Personel.TABLE_NAME + " P WITH(nolock) ");
		sb.append(" WHERE P." + Personel.COLUMN_NAME_SIRKET + " = :s");
		fields.put("basTarih", basTarih);
		sb.append(" AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= :basTarih ");

		fields.put("s", user.getPdksPersonel().getSirket().getId());
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Personel> userList = getPersonelList(sb, fields);
		long lBitTarih = bitTarih.getTime(), lBasTarih = basTarih.getTime();
		TreeMap personelMap = new TreeMap();
		for (Personel personel : userList) {
			try {
				if (personel.getDurum() && personel.getIseBaslamaTarihi() != null && personel.getIseBaslamaTarihi().getTime() <= lBitTarih && personel.getSonCalismaTarihi().getTime() >= lBasTarih)
					if (PdksUtil.hasStringValue(personel.getPdksSicilNo()))
						personelMap.put(personel.getSicilNo(), personel);

			} catch (Exception e) {
				logger.error(personel.getAdSoyad() + " " + e.getMessage());
				e.printStackTrace();
			}
		}
		if (!personelMap.isEmpty()) {
			user.setYetkiliPersonelNoList(new ArrayList(personelMap.keySet()));
			user.setYetkiliPersoneller(new ArrayList<Personel>(personelMap.values()));
		}
		personelMap = null;
	}

	/**
	 * @return
	 */
	public ArrayList<String> getYetkiTumPersonelNoList() {
		return getYetkiTumPersonelNoListesi(authenticatedUser);
	}

	/**
	 * @return
	 */
	public ArrayList<String> getYetkiTumPersonelNoListesi(User islemYapan) {
		if (islemYapan == null)
			islemYapan = authenticatedUser;
		ArrayList<String> perNoList = new ArrayList<String>(islemYapan.getYetkiTumPersonelNoList());
		if (!islemYapan.isIK() && islemYapan.getSuperVisorHemsirePersonelNoList() != null && islemYapan.getCalistigiSayfa() != null) {
			String calistigiSayfa = islemYapan.getCalistigiSayfa();
			String superVisorHemsireSayfalari = getParameterKey("superVisorHemsireSayfalari");
			List<String> sayfalar = PdksUtil.hasStringValue(superVisorHemsireSayfalari) ? PdksUtil.getListByString(superVisorHemsireSayfalari, null) : null;
			if (sayfalar != null && sayfalar.contains(calistigiSayfa)) {
				if (islemYapan.isDirektorSuperVisor())
					perNoList.clear();
				for (String string : islemYapan.getSuperVisorHemsirePersonelNoList()) {
					if (PdksUtil.hasStringValue(string) == false)
						continue;
					if (!perNoList.contains(string))
						perNoList.add(string);
				}

			}

		}
		return perNoList;
	}

	/**
	 * @param user
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 */
	private void digerYetkileriEkle(User user, Date basTarih, Date bitTarih, Session session) {
		HashMap map = new HashMap();
		map.put("yeniYonetici=", user);
		map.put("bitTarih>=", basTarih);
		map.put("basTarih<=", bitTarih);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<UserVekalet> userList = pdksEntityController.getObjectByInnerObjectListInLogic(map, UserVekalet.class);
		for (Iterator iterator = userList.iterator(); iterator.hasNext();) {
			UserVekalet userVekalet = (UserVekalet) iterator.next();
			if (userVekalet.getDurum() == null || !userVekalet.getDurum())
				iterator.remove();

		}
		if (!userList.isEmpty()) {

			ArrayList<User> vekilList = user.getUserVekaletList();
			ArrayList<String> yetkiliPersonelNoList = user.getYetkiliPersonelNoList();
			for (Iterator<UserVekalet> iterator = userList.iterator(); iterator.hasNext();) {
				UserVekalet userVekalet = iterator.next();
				User vekaletVeren = userVekalet.getVekaletVeren();
				vekaletVeren.setYetkiliPersonelNoList(new ArrayList<String>());
				sistemeGirisIslemleri(vekaletVeren, Boolean.FALSE, basTarih, bitTarih, session);
				ArrayList<String> list = vekaletVeren.getYetkiliPersonelNoList();
				for (Iterator iterator2 = list.iterator(); iterator2.hasNext();) {
					String string = (String) iterator2.next();
					if (PdksUtil.hasStringValue(string) == false)
						iterator2.remove();
					else if (vekaletVeren.getPdksPersonel().getSicilNo().equals(string)) {
						iterator2.remove();
						break;
					}
				}
				vekaletVeren.setYetkiliPersonelNoList(list);
				if (!user.isMudur())
					vekilList.add(vekaletVeren);
				else {

					yetkiliPersonelNoList.addAll(vekaletVeren.getYetkiliPersonelNoList());
					vekilList.addAll(vekaletVeren.getUserVekaletList());

				}
			}
			user.setUserVekaletList(vekilList);
			user.setYetkiliPersonelNoList(yetkiliPersonelNoList);
		}
		if (!user.isMudur()) {
			map.clear();
			map.put(PdksEntityController.MAP_KEY_SELECT, "personelGecici.pdksSicilNo");
			map.put("yeniYonetici=", user);
			map.put("bitTarih>=", basTarih);
			map.put("basTarih<=", bitTarih);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			List personelList = pdksEntityController.getObjectByInnerObjectListInLogic(map, PersonelGeciciYonetici.class);
			if (!personelList.isEmpty())
				user.setPersonelGeciciNoList((ArrayList) personelList);
		}

	}

	/**
	 * @param user
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 */
	private void direktorIslemleri(User user, Date basTarih, Date bitTarih, Session session) {
		HashMap map = new HashMap();
		map.put(PdksEntityController.MAP_KEY_MAP, "getId");
		if (user.isGenelMudur())
			map.put("departman", user.getDepartman());
		map.put("pdks", Boolean.TRUE);
		map.put("durum", Boolean.TRUE);

		map.put(PdksEntityController.MAP_KEY_SESSION, session);
		TreeMap<Long, Sirket> sirketMap = pdksEntityController.getObjectByInnerObjectMap(map, Sirket.class, Boolean.TRUE);
		TreeMap<String, String> personelMap = new TreeMap<String, String>();
		map.clear();
		List idList = new ArrayList(sirketMap.values());
		String fieldName = "s";
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " from " + Personel.TABLE_NAME + " P WITH(nolock) ");
		sb.append(" WHERE P." + Personel.COLUMN_NAME_SIRKET + " :" + fieldName);
		map.put("basTarih", basTarih);
		map.put("bitTarih", bitTarih);
		sb.append(" AND P." + Personel.COLUMN_NAME_DURUM + " = 1 ");
		sb.append(" AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= :basTarih ");
		sb.append(" AND P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " <= :bitTarih ");
		map.put(fieldName, idList);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		// List<String> userList = getSQLParamList(idList, sb, fieldName, map, null, session);
		List<String> userList = getSQLParamList(idList, sb, fieldName, map, null, session);

		for (String str : userList) {
			try {
				if (PdksUtil.hasStringValue(str))
					personelMap.put(str, str);

			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		userList = null;
		if (personelMap != null && !personelMap.isEmpty()) {
			ArrayList<String> list = new ArrayList<String>();
			for (Iterator<String> iterator = personelMap.keySet().iterator(); iterator.hasNext();) {
				String elem = iterator.next();
				if (PdksUtil.hasStringValue(elem) && !list.contains(elem))
					list.add(elem);
			}
			user.setYetkiliPersonelNoList(list);
		}

	}

	/**
	 * @param idList
	 * @param session
	 * @return
	 */
	public List<Personel> getPersonelByIdList(List<Long> idList, Session session) {
		List<Personel> perList = null;
		if (idList != null && !idList.isEmpty()) {
			if (session == null)
				session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
			String fieldName = "s";
			HashMap fields = new HashMap();
			fields.put(fieldName, idList);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT P.* from " + Personel.TABLE_NAME + " P WITH(nolock) ");
			sb.append(" WHERE P." + Personel.COLUMN_NAME_ID + " :" + fieldName);
			// perList = pdksEntityController.getObjectBySQLList(session, LIST_MAX_SIZE / 2, sb.toString(), "s", idList, Personel.class);
			perList = getSQLParamList(idList, sb, fieldName, fields, Personel.class, session);
		}
		if (perList == null)
			perList = new ArrayList<Personel>();
		return perList;
	}

	/**
	 * @param sb
	 * @param fields
	 * @return
	 */
	public List<Personel> getPersonelList(StringBuffer sb, HashMap fields) {
		List<Personel> perList = null;
		Session session = fields.containsKey(PdksEntityController.MAP_KEY_SESSION) ? (Session) fields.get(PdksEntityController.MAP_KEY_SESSION) : PdksUtil.getSessionUser(entityManager, authenticatedUser);
		List<Long> idList = pdksEntityController.getObjectBySQLList(sb, fields, null);
		if (!idList.isEmpty()) {
			perList = getPersonelByIdList(idList, session);
		}

		idList = null;
		if (perList == null)
			perList = new ArrayList<Personel>();
		return perList;
	}

	/**
	 * @param user
	 * @param session
	 */
	private void kapananSirketlerinCalisanlariniEkle(User user, Session session) {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);

		// daha sonra db ile alinacak, aciliyetten bu sekilde.
		List<Long> sirketIdList = new ArrayList<Long>();
		HashMap fields = new HashMap();
		sirketIdList.add(new Long(12));

		if (user.getEskiPersonelNoList().isEmpty()) {
			user.getEskiPersonelNoList().clear();
		}
		String fieldName = "s";
		StringBuffer sb = new StringBuffer();
		sb = new StringBuffer();
		sb.append("SELECT P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " from " + Personel.TABLE_NAME + " P WITH(nolock) ");
		sb.append(" WHERE P." + Personel.COLUMN_NAME_SIRKET + " :" + fieldName);
		sb.append(" AND P." + Personel.COLUMN_NAME_DURUM + " = 1 ");
		if (user.isIK_Tesis()) {
			if (user.getPdksPersonel().getTesis() != null) {
				sb.append(" AND P." + Personel.COLUMN_NAME_TESIS + " = :t ");
				fields.put("t", user.getPdksPersonel().getTesis().getId());
			}
		}
		fields.put(fieldName, sirketIdList);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		// List<String> userList = pdksEntityController.getObjectBySQLList(sb, fields, null);
		List<String> userList = getSQLParamList(sirketIdList, sb, fieldName, fields, null, session);
		sirketIdList = null;
		TreeMap<String, String> personelMap = new TreeMap<String, String>();
		for (String str : userList) {
			try {
				if (PdksUtil.hasStringValue(str))
					personelMap.put(str, str);

			} catch (Exception e) {

				e.printStackTrace();
			}
		}

		ArrayList<String> list = new ArrayList<String>();
		for (Iterator<String> iterator = personelMap.keySet().iterator(); iterator.hasNext();) {
			String elem = iterator.next();
			if (PdksUtil.hasStringValue(elem) && !list.contains(elem))
				list.add(elem);
		}
		user.setEskiPersonelNoList(list);

	}

	/**
	 * @param user
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 */
	private void IKIslemleri(User user, Date basTarih, Date bitTarih, Session session) {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);

		if (!user.getDepartman().getId().equals(user.getPdksPersonel().getSirket().getDepartman().getId()))
			yoneticiIslemleri(user, -1, basTarih, bitTarih, session);

		if (user.getDepartman() != null) {
			TreeMap<String, String> personelMap = new TreeMap<String, String>();
			HashMap fields = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT S.* from " + Sirket.TABLE_NAME + " S WITH(nolock) ");
			sb.append(" WHERE S." + Sirket.COLUMN_NAME_DURUM + " = 1 AND S." + Sirket.COLUMN_NAME_PDKS + " = 1 ");
			if (user.isIKAdmin() == false) {
				sb.append(" AND S." + Sirket.COLUMN_NAME_DEPARTMAN + " = :departmanId ");
				fields.put("departmanId", user.getDepartman().getId());
			}

			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Sirket> sirketler = pdksEntityController.getObjectBySQLList(sb, fields, Sirket.class);
			if (!sirketler.isEmpty()) {
				List<Long> sirketIdList = new ArrayList<Long>();
				for (Sirket sirket : sirketler)
					sirketIdList.add(sirket.getId());
				sirketler = null;
				fields.clear();
				sb = new StringBuffer();
				sb.append("SELECT P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " from " + Personel.TABLE_NAME + " P WITH(nolock) ");
				sb.append(" WHERE P." + Personel.COLUMN_NAME_SIRKET + " :s");
				fields.put("basTarih", basTarih);
				fields.put("bitTarih", bitTarih);
				sb.append(" AND P." + Personel.COLUMN_NAME_DURUM + " = 1 ");
				sb.append(" AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= :basTarih ");
				sb.append(" AND P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " <= :bitTarih ");
				// fields.put("basTarih", basTarih);
				fields.put("s", sirketIdList);
				boolean tesisYetki = getParameterKey("tesisYetki").equals("1");
				if (tesisYetki && user.getYetkiliTesisler() != null && !user.getYetkiliTesisler().isEmpty()) {
					List<Long> idler = new ArrayList<Long>();
					for (Iterator iterator = user.getYetkiliTesisler().iterator(); iterator.hasNext();) {
						Tanim tesis = (Tanim) iterator.next();
						idler.add(tesis.getId());
					}
					if (idler.size() == 1) {
						sb.append(" AND P." + Personel.COLUMN_NAME_TESIS + " = :t ");
						fields.put("t", idler.get(0));
					} else {
						sb.append(" AND P." + Personel.COLUMN_NAME_TESIS + " :t ");
						fields.put("t", idler);
					}
					idler = null;
				}
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<String> userList = pdksEntityController.getObjectBySQLList(sb, fields, null);
				sirketIdList = null;
				for (String str : userList) {
					try {
						if (PdksUtil.hasStringValue(str))
							personelMap.put(str, str);

					} catch (Exception e) {

						e.printStackTrace();
					}
				}

				userList = null;
			}
			if (personelMap != null && !personelMap.isEmpty()) {
				ArrayList<String> list = new ArrayList<String>();
				list.addAll(user.getYetkiliPersonelNoList());
				for (Iterator<String> iterator = personelMap.keySet().iterator(); iterator.hasNext();) {
					String elem = iterator.next();
					if (PdksUtil.hasStringValue(elem) && !list.contains(elem))
						list.add(elem);
				}
				user.setYetkiliPersonelNoList(list);
			}
		}

	}

	/**
	 * @param user
	 * @param yonetici
	 * @param vekaletOku
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 */
	private void mudurIslemleri(User user, User yonetici, boolean vekaletOku, Date basTarih, Date bitTarih, Session session) {
		yoneticiIslemleri(user, 1, basTarih, bitTarih, session);
		if (!user.getYetkiliPersonelNoList().isEmpty()) {
			HashMap map = new HashMap();
			map.put(PdksEntityController.MAP_KEY_SELECT, "user");
			map.put("user.pdksPersonel.pdksSicilNo", user.getYetkiliPersonelNoList());
			map.put("user.pdksPersonel.iseBaslamaTarihi<=", bitTarih);
			map.put("user.pdksPersonel.sskCikisTarihi>=", basTarih);
			map.put("user.durum=", Boolean.TRUE);
			map.put("user.pdksPersonel.durum=", Boolean.TRUE);
			map.put("role.rolename=", Role.TIPI_YONETICI);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<User> userList = null;
			try {
				userList = pdksEntityController.getObjectByInnerObjectListInLogic(map, UserRoles.class);

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());

				userList = new ArrayList<User>();
			}
			if (vekaletOku) {

			}

			if (!userList.isEmpty()) {
				for (User user2 : userList)
					user2.setYetkiliPersonelNoList(null);

				if (yonetici == null)
					yonetici = userList.get(0);
				user.setUserVekaletList((ArrayList<User>) userList);
				if (userList.size() > 1) {
					seciliYonetici = (User) yonetici.clone();
					user.setSeciliSuperVisor(yonetici);

				}
				if (yonetici != null) {
					yoneticiIslemleri(yonetici, 2, basTarih, bitTarih, session);
					if (!yonetici.getYetkiliPersonelNoList().isEmpty())
						user.setPersonelGeciciNoList(yonetici.getYetkiliPersonelNoList());

				}
			}
		}

	}

	/**
	 * @param user
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 */
	private void sekreterIslemleri(User user, Date basTarih, Date bitTarih, Session session) {
		if (user.getPdksPersonel().getPdksYonetici() != null) {
			HashMap parametreMap = new HashMap();
			parametreMap.put("pdksPersonel.id", user.getPdksPersonel().getPdksYonetici().getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			User yonetici = (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);
			yoneticiPersonellleriAta(yonetici, Boolean.TRUE, session);
			ArrayList<String> siciller = yonetici.getYetkiliPersonelNoList();
			if (!siciller.contains(user.getPdksPersonel().getSicilNo()))
				siciller.add(user.getPdksPersonel().getSicilNo());
			user.setYetkiliPersonelNoList(siciller);
		}

	}

	/**
	 * @param user
	 * @param session
	 */
	public void setUserRoller(User user, Session session) {
		List<Role> yetkiliRoller = null;
		if (user != null && user.getId() != null && (user.getYetkiliRollerim() == null || user.getYetkiliRollerim().isEmpty())) {
			HashMap map = new HashMap();
			map.put("user.id", user.getId());
			map.put(PdksEntityController.MAP_KEY_SELECT, "role");
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				yetkiliRoller = pdksEntityController.getObjectByInnerObjectList(map, UserRoles.class);
				user.setYetkiliRollerim(yetkiliRoller);
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
			}

			map = null;
		}
		if (yetkiliRoller == null)
			yetkiliRoller = user.getYetkiliRollerim();
		if (yetkiliRoller != null)
			PdksUtil.setUserYetki(user);

	}

	/**
	 * @param user
	 * @param session
	 */
	public void setUserTesisler(User user, Session session) {
		List<Tanim> yetkiliTesisler = null;
		Boolean tesisYetki = getParameterKey("tesisYetki").equals("1");
		if (tesisYetki && user != null && user.getId() != null && (user.getYetkiliTesisler() == null || user.getYetkiliTesisler().isEmpty())) {
			HashMap map = new HashMap();
			map.put("user.id", user.getId());
			map.put(PdksEntityController.MAP_KEY_SELECT, "tesis");
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				yetkiliTesisler = pdksEntityController.getObjectByInnerObjectList(map, UserTesis.class);
				user.setYetkiliTesisler(yetkiliTesisler);
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
			}

			map = null;
		}
	}

	/**
	 * @param user
	 * @param yaz
	 * @param session
	 * @return
	 */
	@Transactional
	public User personelPdksRolAta(User user, boolean yaz, Session session) {
		if (user != null && user.getDepartman().isAdminMi()) {
			HashMap map = new HashMap();
			Date bugun = PdksUtil.getDate(Calendar.getInstance().getTime());
			map.put("sskCikisTarihi>=", bugun);
			map.put("iseBaslamaTarihi<=", bugun);
			map.put("durum=", Boolean.TRUE);
			map.put("yoneticisi.id=", user.getPdksPersonel().getId());
			map.put("pdksSicilNo<>", "");
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			List personeller = pdksEntityController.getObjectByInnerObjectListInLogic(map, Personel.class);
			String rolename = personeller.isEmpty() ? Role.TIPI_PERSONEL : Role.TIPI_YONETICI;
			map.clear();
			map.put("rolename", rolename);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			Role role = (Role) pdksEntityController.getObjectByInnerObject(map, Role.class);
			if ((role == null || role.getStatus().equals(Boolean.FALSE)) && role.getRolename().equals(Role.TIPI_PERSONEL)) {
				map.clear();
				map.put("rolename", Role.TIPI_YONETICI);

				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				role = (Role) pdksEntityController.getObjectByInnerObject(map, Role.class);
			}
			if (role != null && role.getStatus().equals(Boolean.TRUE)) {
				UserRoles userRoles = new UserRoles();
				userRoles.setRole(role);
				userRoles.setUser(user);
				if (yaz) {
					pdksEntityController.saveOrUpdate(session, entityManager, userRoles);
					session.flush();
				}
			}
		}
		setUserRoller(user, session);
		return user;
	}

	/**
	 * @param user
	 * @param level
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 */
	public void yoneticiIslemleri(User user, int level, Date basTarih, Date bitTarih, Session session) {
		ERPController controller = getERPController();
		if (user.isIK()) {
			if (session == null)
				session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
			if (basTarih == null)
				basTarih = Calendar.getInstance().getTime();
			if (bitTarih == null)
				bitTarih = Calendar.getInstance().getTime();
			if (user.isMudur())
				level = 1;

			Map<String, String> reqMap = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
			String path = reqMap.containsKey("host") ? reqMap.get("host") : "";
			if (level > 2 && path.toUpperCase().indexOf("ABH08448:8080") < 0 && path.indexOf("localhost:8080") < 0)
				level = 2;
			HashMap map = new HashMap();
			List<Personel> personelleriList = yoneticiPersonelleri(user.getPdksPersonel().getId(), basTarih, bitTarih, session);
			map.clear();
			TreeMap<String, Personel> yoneticiPersonelMap = new TreeMap<String, Personel>();
			for (Personel personel : personelleriList)
				yoneticiPersonelMap.put(personel.getSicilNo(), personel);
			ArrayList<String> personelNumaralariListesi = new ArrayList<String>();
			personelNumaralariListesi.add(user.getStaffId());
			LinkedHashMap yoneticilerMap = null;
			HashMap map1 = new HashMap();
			map1.put(PdksEntityController.MAP_KEY_MAP, "getKodu");
			map1.put("tipi", Tanim.TIPI_SAP_MASRAF_YERI);
			if (session != null)
				map1.put(PdksEntityController.MAP_KEY_SESSION, session);
			TreeMap masrafYeriMap = pdksEntityController.getObjectByInnerObjectMap(map1, Tanim.class, Boolean.FALSE);

			map1.clear();
			map1.put(PdksEntityController.MAP_KEY_MAP, "getKodu");
			map1.put("tipi", Tanim.TIPI_BORDRO_ALT_BIRIMI);
			if (session != null)
				map1.put(PdksEntityController.MAP_KEY_SESSION, session);
			TreeMap bordroAltBirimiMap = pdksEntityController.getObjectByInnerObjectMap(map1, Tanim.class, Boolean.FALSE);
			LinkedHashMap personelSapMap = null;
			try {
				personelSapMap = controller.topluHaldeIscileriVeriGetir(session, level, Boolean.TRUE, personelNumaralariListesi, basTarih, bitTarih, bordroAltBirimiMap, masrafYeriMap);
			} catch (Exception e1) {
				PdksUtil.addMessageWarn(e1.getMessage());
			}

			if (personelSapMap != null) {
				if (personelSapMap.containsKey("0")) {
					yoneticilerMap = (LinkedHashMap) personelSapMap.get("0");
					personelSapMap.remove("0");
				}
				if (!personelSapMap.isEmpty()) {
					ArrayList<String> yetkiliPersonelNoList = new ArrayList<String>(personelSapMap.keySet());
					for (Iterator iterator = yetkiliPersonelNoList.iterator(); iterator.hasNext();) {
						String sicilNo = (String) iterator.next();
						if (yoneticiPersonelMap.containsKey(sicilNo))
							yoneticiPersonelMap.remove(sicilNo);
					}
					TreeMap personelMap = new TreeMap();
					// burada sorun var
					map.clear();
					map.put("pdksSicilNo", yetkiliPersonelNoList.clone());
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					List personeller = pdksEntityController.getObjectByInnerObjectListInLogic(map, Personel.class);
					for (Iterator iterator = personeller.iterator(); iterator.hasNext();) {
						Personel personel = (Personel) iterator.next();
						if (personel.getPdksYonetici() != null && !personel.getPdksYonetici().getId().equals(user.getPdksPersonel().getId()))
							personelMap.put(personel.getSicilNo(), personel);
					}
					personeller = null;
					for (Iterator<String> iterator = yetkiliPersonelNoList.iterator(); iterator.hasNext();) {
						String string = iterator.next();
						if (!personelMap.containsKey(string)) {
							personelSapMap.remove(string);
							continue;
						}
						Personel personel = (Personel) personelSapMap.get(string);
						Personel yonetici = personel.getPdksYonetici();
						while (yonetici != null && PdksUtil.hasStringValue(yonetici.getSicilNo())) {
							if (!personelSapMap.containsKey(yonetici.getSicilNo()))
								personelSapMap.put(yonetici.getSicilNo(), yonetici);
							else
								break;
							yonetici = yonetici.getPdksYonetici();
						}
					}
					try {
						personelSapMap = controller.topluHaldePersonelBilgisiGetir(session, bordroAltBirimiMap, masrafYeriMap, personelSapMap, null, null, null, null);

					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());
						PdksUtil.addMessageError("yoneticiIslemleri " + e.getMessage());
					}
				}
				if (user.isGenelMudur()) {
					map.clear();
					map.put("ldap=", Boolean.TRUE);
					map.put("erpDurum=", Boolean.TRUE);
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<Sirket> sirketList = pdksEntityController.getObjectByInnerObjectListInLogic(map, Sirket.class);
					if (!sirketList.isEmpty()) {
						map.clear();
						map.put(PdksEntityController.MAP_KEY_SELECT, "personel");
						map.put("kullanici=", null);
						map.put("pdksPersonel.sirket", sirketList);
						if (session != null)
							map.put(PdksEntityController.MAP_KEY_SESSION, session);
						List<Personel> perList = pdksEntityController.getObjectByInnerObjectListInLogic(map, PdksPersonelView.class);
						for (Personel personel : perList) {
							if (personel.isCalisiyor() && !personelSapMap.containsKey(personel.getSicilNo()))
								personelSapMap.put(personel.getSicilNo(), personel);

						}
						perList = null;
					}
					sirketList = null;
				}
				if (!personelSapMap.isEmpty()) {
					personelNumaralariListesi = new ArrayList<String>(personelSapMap.keySet());
					Personel userPersonel = (Personel) personelSapMap.get(user.getStaffId());
					if (userPersonel != null)
						yoneticileriEkle(personelSapMap, personelNumaralariListesi, userPersonel);
					ArrayList<String> sicilNoList = new ArrayList<String>();
					if (yoneticilerMap != null && !yoneticilerMap.isEmpty()) {
						for (Iterator<String> iterator = yoneticilerMap.keySet().iterator(); iterator.hasNext();) {
							String string = iterator.next();
							sicilNoList.add(string);
						}
					}
					for (String string : personelNumaralariListesi)
						sicilNoList.add(string);
					map.clear();
					map.put(PdksEntityController.MAP_KEY_MAP, "getSicilNo");
					// map.put("kgsSicilNo", sicilNoList);
					map.put("pdksSicilNo", sicilNoList);
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					TreeMap personelViewMap = pdksEntityController.getObjectByInnerObjectMap(map, PdksPersonelView.class, Boolean.FALSE);
					TreeMap<String, PersonelView> personelMap = new TreeMap<String, PersonelView>();
					for (Iterator<String> iterator = personelViewMap.keySet().iterator(); iterator.hasNext();) {
						String sicilNo = iterator.next();
						PersonelView personelView = ((PdksPersonelView) personelViewMap.get(sicilNo)).getPersonelView();
						personelMap.put(personelView.getKgsSicilNo(), personelView);
					}
					personelViewMap = null;
					Personel yoneticisi = null;
					LinkedHashMap<String, Personel> yoneticiMap = new LinkedHashMap<String, Personel>();
					Personel personel = null;
					for (Iterator<String> iterator = personelMap.keySet().iterator(); iterator.hasNext();) {
						String sicilNo = iterator.next();
						if (!personelSapMap.containsKey(sicilNo))
							continue;
						try {
							Personel pdksSapPersonel = (Personel) personelSapMap.get(sicilNo);

							if (pdksSapPersonel == null || pdksSapPersonel.getSirket() == null)
								continue;
							PersonelView personelView = personelMap.get(sicilNo);
							personel = personelMap.containsKey(sicilNo) ? personelView.getPdksPersonel() : pdksSapPersonel;
							// logger.error(sicilNo + " " +
							// pdksSapPersonel.getAdSoyad());
							if (personel == null)
								pdksSapPersonel.setPersonelKGS(personelView.getPersonelKGS());
							if (yoneticiMap.containsKey(sicilNo)) {
								if (personelView.getKullanici() == null && pdksSapPersonel.getSirket().isLdap() && personel.getSirket().getLpdapOnEk() != null) {
									User ldapUser;
									try {
										ldapUser = LDAPUserManager.getLDAPUserAttributes(personel.getSirket().getLpdapOnEk().trim() + personel.getSicilNo().substring(3).trim(), LDAPUserManager.USER_ATTRIBUTES_SAM_ACCOUNT_NAME);
									} catch (Exception e) {
										ldapUser = null;
									}
									if (ldapUser != null && ldapUser.isDurum()) {
										// ldapUser.setDurum(Boolean.FALSE);
										ldapUser.setPdksPersonel(personelView.getPdksPersonel());
										ldapUser.setDepartman(pdksSapPersonel.getSirket().getDepartman());
										pdksEntityController.saveOrUpdate(session, entityManager, ldapUser);
										personelView.setKullanici(ldapUser);
									}
								}
							}
							personelUpdate(user, personel, pdksSapPersonel, yoneticisi, personelView, personelMap, personelSapMap, yoneticiMap, session);

						} catch (Exception e) {
							logger.error("Pdks hata in : \n");
							e.printStackTrace();
							logger.error("Pdks hata out : " + e.getMessage());
							logger.error("HATA : " + sicilNo + " " + personel.getAdSoyad() + " " + e.getMessage());
						}

					}

				}
			}
			if (user.isYonetici() || user.isIK())
				yoneticiPersonellleriAta(user, Boolean.FALSE, session);

		}
	}

	/**
	 * @param yoneticiId
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 * @return
	 */
	public List<Personel> yoneticiPersonelleri(Long yoneticiId, Date basTarih, Date bitTarih, Session session) {
		List<Personel> personelleriList = null;
		StringBuffer sb = new StringBuffer();
		HashMap map = new HashMap();
		try {
			sb.append("SELECT  P." + Personel.COLUMN_NAME_ID + " FROM " + Personel.TABLE_NAME + " p WITH(nolock) ");
			sb.append(" WHERE " + Personel.COLUMN_NAME_YONETICI + " = :yoneticiId");
			map.put("yoneticiId", yoneticiId);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			long basTarihLong = basTarih.getTime(), bitTarihLong = bitTarih.getTime();
			personelleriList = getPersonelList(sb, map);
			for (Iterator iterator = personelleriList.iterator(); iterator.hasNext();) {
				Personel personel = (Personel) iterator.next();
				Boolean sil = Boolean.FALSE;
				try {
					if (personel.getDurum()) {
						if (personel.getIseBaslamaTarihi().getTime() <= bitTarihLong && personel.getSonCalismaTarihi().getTime() >= basTarihLong)
							continue;
						sil = Boolean.TRUE;
					} else
						sil = Boolean.TRUE;
				} catch (Exception e) {
					sil = Boolean.TRUE;
				}

				if (sil)
					iterator.remove();

			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			if (personelleriList == null)
				personelleriList = new ArrayList<Personel>();
		}
		map = null;
		sb = null;
		return personelleriList;
	}

	/**
	 * @param user
	 * @param devam
	 * @param session
	 */
	private void yoneticiPersonellleriAta(User user, boolean devam, Session session) {
		ArrayList<Personel> personeller = new ArrayList<Personel>();
		personeller.add(user.getPdksPersonel());
		HashMap<Long, Personel> personellerMap = new HashMap<Long, Personel>();
		yoneticiPersonelleriBul((List<Personel>) personeller.clone(), personellerMap, devam, session);
		if (!personellerMap.isEmpty())
			personeller.addAll(new ArrayList<Personel>(personellerMap.values()));
		ArrayList<String> perNoList = new ArrayList<String>();
		for (String sicilNo : user.getYetkiliPersonelNoList()) {
			if (PdksUtil.hasStringValue(sicilNo) && !perNoList.contains(sicilNo))
				perNoList.add(sicilNo);

		}

		for (Personel personel : personeller)
			if (personel.getPdksSicilNo() != null && !perNoList.contains(personel.getPdksSicilNo()))
				perNoList.add(personel.getPdksSicilNo());
		user.setYetkiliPersonelNoList(perNoList);
	}

	/**
	 * @param user
	 * @param session
	 */
	private void setUserSuperVisorHemsirePersonelNoList(User user, Session session) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT DISTINCT P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + "  FROM " + Personel.TABLE_NAME + " P WITH(nolock) ");
		sb.append(" INNER JOIN " + User.TABLE_NAME + " U  ON U." + User.COLUMN_NAME_PERSONEL + " = P." + Personel.COLUMN_NAME_ID);
		sb.append(" INNER JOIN " + MailGrubu.TABLE_NAME + " M ON M." + MailGrubu.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_HAREKET_MAIL_ID + " AND M." + MailGrubu.COLUMN_NAME_MAIL + " LIKE :e ");
		sb.append(" WHERE P." + Personel.COLUMN_NAME_YONETICI + " <> :y AND P." + Personel.COLUMN_NAME_DURUM + " = 1 AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= convert(date,GETDATE()) ");
		sb.append(" ORDER BY 1 ");
		HashMap parametreMap = new HashMap();
		parametreMap.put("y", user.getPdksPersonel().getId());
		parametreMap.put("e", "%" + user.getEmail() + "%");
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<String> veriList = pdksEntityController.getObjectBySQLList(sb, parametreMap, null);
		if (!veriList.isEmpty()) {
			ArrayList<String> list = user.getSuperVisorHemsirePersonelNoList();
			for (Iterator iterator = veriList.iterator(); iterator.hasNext();) {
				String sicilNo = (String) iterator.next();
				if (PdksUtil.hasStringValue(sicilNo) == false)
					continue;
				if (list == null) {
					list = new ArrayList<String>();
					user.setSuperVisorHemsirePersonelNoList(list);
				}
				if (!list.contains(sicilNo))
					list.add(sicilNo);
			}
		}
		veriList = null;
		parametreMap = null;
	}

	/**
	 * @param user
	 * @param personel
	 * @param pdksSapPersonel
	 * @param yoneticisi
	 * @param personelView
	 * @param personelMap
	 * @param personelSapMap
	 * @param yoneticiMap
	 * @param session
	 */
	@Transactional
	private void personelUpdate(User user, Personel personel, Personel pdksSapPersonel, Personel yoneticisi, PersonelView personelView, TreeMap personelMap, LinkedHashMap personelSapMap, LinkedHashMap<String, Personel> yoneticiMap, Session session) {

		if (personel == null)
			personel = pdksSapPersonel;
		try {
			if (pdksSapPersonel.getPdksYonetici() != null && PdksUtil.hasStringValue(pdksSapPersonel.getPdksYonetici().getSicilNo()))
				yoneticisi = yoneticiGuncelle(session, personelSapMap, personelMap, yoneticiMap, pdksSapPersonel);
			else
				yoneticisi = null;
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			yoneticisi = null;
		}
		if (yoneticisi != null)
			pdksEntityController.saveOrUpdate(session, entityManager, yoneticisi);
		personel.setYoneticisiAta(yoneticisi);
		personel.setAd(pdksSapPersonel.getAd());
		personel.setSoyad(pdksSapPersonel.getSoyad());
		personel.setGorevTipi(null);
		if (pdksSapPersonel.getDogumTarihi() != null)
			personel.setDogumTarihi(pdksSapPersonel.getDogumTarihi());
		if (pdksSapPersonel.getGrubaGirisTarihi() != null)
			personel.setGrubaGirisTarihi(pdksSapPersonel.getGrubaGirisTarihi());
		if (pdksSapPersonel.getIseBaslamaTarihi() != null)
			personel.setIseBaslamaTarihi(pdksSapPersonel.getIseBaslamaTarihi());
		if (pdksSapPersonel.getSonCalismaTarihi() != null)
			personel.setIstenAyrilisTarihi(pdksSapPersonel.getSonCalismaTarihi());
		else
			personel.setIstenAyrilisTarihi(PdksUtil.getSonSistemTarih());
		// personel.setDurum(pdksSapPersonel.getDurum());
		try {
			if (personel.getSirket() == null)
				personel.setSirket(user.getPdksPersonel().getSirket());
			pdksEntityController.saveOrUpdate(session, entityManager, personel);
			personelView.setPdksPersonel(personel);
			if (personelView.getKullanici() == null && pdksSapPersonel.getSirket().isLdap() && personel.getSirket().getLpdapOnEk() != null)
				kullaniciUpdate(pdksSapPersonel, personelView, personel, session);

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}
		session.flush();

	}

	/**
	 * @param pdksSapPersonel
	 * @param personelView
	 * @param personel
	 * @param session
	 */
	private void kullaniciUpdate(Personel pdksSapPersonel, PersonelView personelView, Personel personel, Session session) {
		User ldapUser = null;
		String kullaniciAdi = "";
		try {
			kullaniciAdi = personel.getSirket().getLpdapOnEk().trim() + personel.getSicilNo().substring(3).trim();
			ldapUser = LDAPUserManager.getLDAPUserAttributes(kullaniciAdi, LDAPUserManager.USER_ATTRIBUTES_SAM_ACCOUNT_NAME);
		} catch (Exception e) {

		}
		if (ldapUser != null) {
			ldapUser.setDurum(Boolean.FALSE);
			ldapUser.setPdksPersonel(personelView.getPdksPersonel());
			ldapUser.setDepartman(pdksSapPersonel.getSirket().getDepartman());
			try {
				pdksEntityController.saveOrUpdate(session, entityManager, ldapUser);
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());

			}
			personelView.setKullanici(ldapUser);

		}
	}

	/**
	 * @param personelSapMap
	 * @param personelNumaralariListesi
	 * @param userPersonel
	 */
	private void yoneticileriEkle(LinkedHashMap<String, Personel> personelSapMap, ArrayList<String> personelNumaralariListesi, Personel userPersonel) {
		if (userPersonel.getPdksYonetici() != null) {
			personelNumaralariListesi.add(userPersonel.getPdksYonetici().getErpSicilNo());
			personelSapMap.put(userPersonel.getPdksYonetici().getErpSicilNo(), userPersonel.getPdksYonetici());
			yoneticileriEkle(personelSapMap, personelNumaralariListesi, userPersonel.getPdksYonetici());

		}
	}

	/**
	 * @param session
	 * @param personelSapMap
	 * @param personelMap
	 * @param yoneticiMap
	 * @param pdksSapPersonel
	 * @return
	 */
	private Personel yoneticiGuncelle(Session session, LinkedHashMap<String, Personel> personelSapMap, TreeMap personelMap, LinkedHashMap<String, Personel> yoneticiMap, Personel pdksSapPersonel) {
		Personel yoneticisi;
		yoneticisi = null;
		try {
			if (pdksSapPersonel.getPdksYonetici() != null && PdksUtil.hasStringValue(pdksSapPersonel.getPdksYonetici().getErpSicilNo())) {
				long sicilNo = Long.parseLong(pdksSapPersonel.getPdksYonetici().getErpSicilNo());
				if (sicilNo > 0) {
					if (yoneticiMap.containsKey(pdksSapPersonel.getPdksYonetici().getErpSicilNo()))
						yoneticisi = (Personel) yoneticiMap.get(pdksSapPersonel.getPdksYonetici().getErpSicilNo());
					else if (personelSapMap.containsKey(pdksSapPersonel.getPdksYonetici().getErpSicilNo())) {
						Personel sapYoneticisi = (Personel) personelSapMap.get(pdksSapPersonel.getPdksYonetici().getErpSicilNo());
						if (sapYoneticisi.getSirket() != null && personelMap.containsKey(pdksSapPersonel.getPdksYonetici().getErpSicilNo())) {
							PersonelView personelView = (PersonelView) personelMap.get(pdksSapPersonel.getPdksYonetici().getErpSicilNo());
							yoneticisi = personelView.getPdksPersonel();

							yoneticisi.setSirket(sapYoneticisi.getSirket());
							yoneticisi.setPersonelKGS(personelView.getPersonelKGS());

							yoneticisi.setAd(sapYoneticisi.getAd());
							yoneticisi.setSoyad(sapYoneticisi.getSoyad());
							yoneticisi.setGorevTipi(null);
							try {
								yoneticisi.setDurum(sapYoneticisi.getDurum());
								if (sapYoneticisi.getPdksYonetici() != null) {
									Personel personel = yoneticiGuncelle(session, personelSapMap, personelMap, yoneticiMap, sapYoneticisi);
									if (personel != null && personel.getId() != null)
										yoneticisi.setYoneticisiAta(personel);
								}
								pdksEntityController.saveOrUpdate(session, entityManager, yoneticisi);
								// yoneticisi = (Personel)
								// pdksEntityController.save(yoneticisi,
								// session);

							} catch (Exception e) {
								logger.error("Pdks hata in : \n");
								e.printStackTrace();
								logger.error("Pdks hata out : " + e.getMessage());

							}
							personelView.setPdksPersonel(yoneticisi);
						} else {
							if (personelMap.containsKey(sapYoneticisi.getErpSicilNo()))
								yoneticisi = ((PersonelView) personelMap.get(sapYoneticisi.getErpSicilNo())).getPdksPersonel();

						}
						if (yoneticisi != null)
							yoneticiMap.put(pdksSapPersonel.getPdksYonetici().getErpSicilNo(), yoneticisi);
					}
				}

			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

			yoneticisi = null;
		}

		return yoneticisi;
	}

	/**
	 * @param kidemYil
	 * @param user
	 * @param izinSahibi
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public PersonelIzin getPersonelBakiyeIzin(int kidemYil, User user, Personel izinSahibi, Session session) throws Exception {
		PersonelIzin personelBakiyeIzin = null;
		if (izinSahibi.getId() != null) {
			Date baslangicZamani = PdksUtil.getBakiyeYil();
			HashMap map = new HashMap();
			map.put("departman", izinSahibi.getSirket().getDepartman());
			map.put("bakiyeIzinTipi.izinTipiTanim.kodu", IzinTipi.YILLIK_UCRETLI_IZIN);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			IzinTipi izinTipi = (IzinTipi) pdksEntityController.getObjectByInnerObject(map, IzinTipi.class);
			if (izinTipi != null) {
				try {
					personelBakiyeIzin = getBakiyeIzin(user, izinSahibi, baslangicZamani, izinTipi, null, -1, session);
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());
					throw new Exception("getPersonelBakiyeIzin " + e.getMessage());
				}

				if (personelBakiyeIzin == null) {
					personelBakiyeIzin = new PersonelIzin();
					personelBakiyeIzin.setBaslangicZamani(baslangicZamani);
					personelBakiyeIzin.setBitisZamani(baslangicZamani);
					String aciklama = (izinTipi != null ? izinTipi.getIzinTipiTanim().getAciklama() + " Devir" : "Devir İzin");
					if (kidemYil >= 0)
						aciklama = kidemYil > 0 ? String.valueOf(kidemYil) : "";
					personelBakiyeIzin.setAciklama(aciklama);
					personelBakiyeIzin.setIzinSahibi(izinSahibi);
					personelBakiyeIzin.setIzinTipi(izinTipi);
					personelBakiyeIzin.setOlusturanUser(authenticatedUser);
					personelBakiyeIzin.setIzinSuresi(0D);
				}
			}
		}
		if (personelBakiyeIzin == null) {
			personelBakiyeIzin = new PersonelIzin();
			personelBakiyeIzin.setIzinSuresi(0D);
		}
		return personelBakiyeIzin;
	}

	/**
	 * @param basTarih
	 * @param bitTarih
	 * @param basTarihObje
	 * @param bitTarihObje
	 * @return
	 */
	public Boolean getObjeTarihiAraliktaMi(Date basTarih, Date bitTarih, Date basTarihObje, Date bitTarihObje) {
		String patern = "yyyyMMdd";
		long basTarihLong = Long.parseLong(PdksUtil.convertToDateString(basTarih, patern));
		long bitTarihLong = Long.parseLong(PdksUtil.convertToDateString(bitTarih, patern));
		long basTarihObjeLong = Long.parseLong(PdksUtil.convertToDateString(basTarihObje, patern));
		long bitTarihObjeLong = Long.parseLong(PdksUtil.convertToDateString(bitTarihObje, patern));
		boolean durum = (bitTarihLong >= basTarihObjeLong) && (basTarihLong <= bitTarihObjeLong);
		return durum;
	}

	/**
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 * @return
	 */
	public List<YemekIzin> getYemekList(Date basTarih, Date bitTarih, Session session) {
		Calendar cal = Calendar.getInstance();
		if (bitTarih == null && basTarih != null)
			bitTarih = tariheGunEkleCikar(cal, basTarih, 1);
		HashMap map = new HashMap();
		if (basTarih != null && bitTarih != null) {
			map.put("bitTarih>=", basTarih);
			map.put("basTarih<=", bitTarih);
		}
		map.put("durum=", Boolean.TRUE);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<YemekIzin> list = pdksEntityController.getObjectByInnerObjectListInLogic(map, YemekIzin.class);
		if (!list.isEmpty()) {
			HashMap<Long, YemekIzin> yemekMap = new HashMap<Long, YemekIzin>();
			if (list.size() > 1)
				list = PdksUtil.sortListByAlanAdi(list, "yemekNumeric", false);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				YemekIzin yemekIzin = (YemekIzin) iterator.next();
				yemekIzin.setVardiyaMap(null);
				if (yemekIzin.getOzelMola() != null && yemekIzin.getOzelMola())
					yemekMap.put(yemekIzin.getId(), yemekIzin);

			}
			if (!yemekMap.isEmpty()) {
				List idList = new ArrayList(yemekMap.keySet());
				String fieldName = "yemekIzin.id";
				map.clear();
				map.put(fieldName, idList);
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				// List<VardiyaYemekIzin> vardiyaYemekIzinList = pdksEntityController.getObjectByInnerObjectList(map, VardiyaYemekIzin.class);
				List<VardiyaYemekIzin> vardiyaYemekIzinList = getParamList(false, idList, fieldName, map, VardiyaYemekIzin.class, session);
				for (Iterator iterator = vardiyaYemekIzinList.iterator(); iterator.hasNext();) {
					VardiyaYemekIzin vardiyaYemekIzin = (VardiyaYemekIzin) iterator.next();
					YemekIzin yemekIzin = yemekMap.get(vardiyaYemekIzin.getYemekIzin().getId());
					if (yemekIzin.getVardiyaMap() == null)
						yemekIzin.setVardiyaMap(new TreeMap<Long, Vardiya>());
					yemekIzin.getVardiyaMap().put(vardiyaYemekIzin.getVardiya().getId(), vardiyaYemekIzin.getVardiya());
				}
				vardiyaYemekIzinList = null;
			}
			yemekMap = null;

		}

		map = null;
		return list;

	}

	/**
	 * @param perList
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 * @return
	 */
	@Transactional
	public TreeMap<String, Tatil> getTatilGunleri(List<Personel> perList, Date basTarih, Date bitTarih, Session session) {
		TreeMap<String, Tatil> tatilMap = new TreeMap<String, Tatil>();
		String pattern = PdksUtil.getDateTimeFormat();
		Calendar cal = Calendar.getInstance();
		cal.setTime(basTarih);
		int basYil = cal.get(Calendar.YEAR);
		cal.setTime(bitTarih);
		int bitYil = cal.get(Calendar.YEAR);
		List<Tatil> pdksTatilList = new ArrayList<Tatil>(), tatilList = new ArrayList<Tatil>();

		String formatStr = "yyyy-MM-dd";
		StringBuffer sb = new StringBuffer();
		sb.append("SP_GET_TATIL");
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("basTarih", basTarih != null ? PdksUtil.convertToDateString(basTarih, formatStr) : null);
		map.put("bitTarih", basTarih != null ? PdksUtil.convertToDateString(bitTarih, formatStr) : null);
		map.put("df", null);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		boolean ayir = false;
		try {
			List<Object[]> list = pdksEntityController.execSPList(map, sb, null);
			if (!list.isEmpty()) {
				List<Long> idList = new ArrayList<Long>();
				TreeMap<Long, Integer> tatilVersionMap = new TreeMap<Long, Integer>();
				for (Object[] objects : list) {
					Long id = ((BigDecimal) objects[0]).longValue();
					if (!idList.contains(id))
						idList.add(id);
					tatilVersionMap.put(id, 0);
					Tatil tatil = new Tatil();
					tatil.setId(id);
					tatil.setBasTarih((Date) objects[1]);
					tatil.setBitTarih((Date) objects[2]);
					tatilList.add(tatil);
				}
				map.clear();
				// map.put(PdksEntityController.MAP_KEY_MAP, "getId");
				String fieldName = "id";
				map.put(fieldName, idList);
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				// TreeMap<Long, Tatil> tatilDataMap = pdksEntityController.getObjectByInnerObjectMap(map, Tatil.class, false);
				TreeMap<Long, Tatil> tatilDataMap = getParamTreeMap(Boolean.FALSE, "getId", false, idList, fieldName, map, Tatil.class, session);

				for (Tatil tatil : tatilList) {
					Tatil orjTatil = (Tatil) tatilDataMap.get(tatil.getId()).clone();
					orjTatil.setVersion(tatilVersionMap.get(tatil.getId()));
					orjTatil.setBasTarih(tatil.getBasTarih());
					orjTatil.setBitTarih(tatil.getBitTarih());
					Integer ver = orjTatil.getVersion() + 1;
					tatilVersionMap.put(tatil.getId(), ver);
					pdksTatilList.add(orjTatil);
				}
				tatilDataMap = null;
				idList = null;
				tatilList.clear();
			}
			list = null;
		} catch (Exception e) {
			ayir = true;
			map.clear();
			map.put("basTarih<=", bitTarih);
			map.put("bitisTarih>=", basTarih);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			if (pdksEntityController == null)
				pdksEntityController = new PdksEntityController();
			tatilList = pdksEntityController.getObjectByInnerObjectListInLogic(map, Tatil.class);
			if (!tatilList.isEmpty())
				tatilList = PdksUtil.sortListByAlanAdi(tatilList, "basTarih", false);
		}

		if (ayir) {
			if (tatilList.size() > 1) {
				for (Iterator iterator = tatilList.iterator(); iterator.hasNext();) {
					Tatil pdksTatil = (Tatil) iterator.next();
					if (!pdksTatil.isTekSefer()) {
						pdksTatilList.add(pdksTatil);
						iterator.remove();
					}
				}
				if (!pdksTatilList.isEmpty()) {
					tatilList.addAll(pdksTatilList);
					pdksTatilList.clear();
				}
			}
			for (Iterator<Tatil> iterator = tatilList.iterator(); iterator.hasNext();) {
				Tatil pdksTatilOrj = iterator.next();
				Tatil pdksTatil = (Tatil) pdksTatilOrj.clone();
				if (pdksTatil.isTekSefer()) {
					if (getObjeTarihiAraliktaMi(basTarih, bitTarih, pdksTatil.getBasTarih(), pdksTatil.getBitTarih()))
						pdksTatilList.add(pdksTatil);
				} else
					for (int i = basYil; i <= bitYil; i++) {
						Tatil pdksTatilP = (Tatil) pdksTatil.clone();
						cal.setTime(pdksTatilP.getBasTarih());
						cal.set(Calendar.YEAR, i);
						pdksTatilP.setYarimGun(pdksTatil.isYarimGunMu());
						pdksTatilP.setBasTarih(cal.getTime());
						cal.setTime(pdksTatilP.getBitTarih());
						cal.set(Calendar.YEAR, i);
						Date bitisTarih = PdksUtil.convertToJavaDate(PdksUtil.convertToDateString(cal.getTime(), "yyyyMMdd") + " 23:59:59", "yyyyMMdd HH:mm:ss");
						pdksTatilP.setBitTarih(bitisTarih);
						if (getObjeTarihiAraliktaMi(basTarih, bitTarih, pdksTatilP.getBasTarih(), pdksTatilP.getBitTarih()))
							pdksTatilList.add(pdksTatilP);
					}

			}
		}
		String arifeTatilBasZaman = getParameterKey("arifeTatilBasZaman");
		if (!pdksTatilList.isEmpty()) {
			String yarimGunStr = (parameterMap != null && parameterMap.containsKey("yarimGunSaati") ? (String) parameterMap.get("yarimGunSaati") : "");
			if (PdksUtil.hasStringValue(arifeTatilBasZaman))
				yarimGunStr = arifeTatilBasZaman;
			int saat = 13, dakika = 0;
			if (yarimGunStr.indexOf(":") > 0) {
				StringTokenizer st = new StringTokenizer(yarimGunStr, ":");
				if (st.countTokens() >= 2) {
					try {
						saat = Integer.parseInt(st.nextToken().trim());
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());
						saat = 13;
					}
					try {
						dakika = Integer.parseInt(st.nextToken().trim());
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());
						saat = 13;
						dakika = 0;
					}
				}
			}

			for (Tatil pdksTatil : pdksTatilList) {
				Date tarih = pdksTatil.getBasTarih();
				Boolean ilkGun = Boolean.TRUE;
				Tatil orjTatil = (Tatil) pdksTatil.clone();
				orjTatil.setBasTarih(PdksUtil.getDate(orjTatil.getBasTarih()));
				orjTatil.setBitGun(tariheGunEkleCikar(cal, PdksUtil.getDate(orjTatil.getBitTarih()), 1));
				if (pdksTatil.isYarimGunMu()) {
					orjTatil.setBasTarih(PdksUtil.setTarih(orjTatil.getBasTarih(), Calendar.HOUR_OF_DAY, saat));
					orjTatil.setBasTarih(PdksUtil.setTarih(orjTatil.getBasTarih(), Calendar.MINUTE, dakika));
				}
				while (PdksUtil.tarihKarsilastirNumeric(pdksTatil.getBitTarih(), tarih) != -1) {
					String tarihStr = PdksUtil.convertToDateString(tarih, "yyyyMMdd");
					boolean yarimGun = ilkGun && pdksTatil.isYarimGunMu();
					if (pdksTatil.isPeriyodik() || !ilkGun || !tatilMap.containsKey(tarihStr)) {
						if (tatilMap.containsKey(tarihStr)) {
							Tatil tatil = tatilMap.get(tarihStr);
							if (yarimGun && !tatil.isYarimGunMu()) {
								tarih = tariheGunEkleCikar(cal, tarih, 1);
								ilkGun = Boolean.FALSE;
								continue;
							}

						}
						Tatil tatil = new Tatil();
						tatil.setOrjTatil((Tatil) orjTatil.clone());
						tatil.setBasTarih(tarih);
						tatil.setAciklama(pdksTatil.getAciklama());
						tatil.setAd(pdksTatil.getAd());
						tatil.setYarimGun(yarimGun);
						if (yarimGun)
							tatil.setArifeVardiyaYarimHesapla(pdksTatil.getArifeVardiyaYarimHesapla());
						tatil.setBasTarih(PdksUtil.getDate(tatil.getBasTarih()));
						if (tatil.isYarimGunMu()) {
							tatil.setBasTarih(PdksUtil.setTarih(tatil.getBasTarih(), Calendar.HOUR_OF_DAY, saat));
							tatil.setBasTarih(PdksUtil.setTarih(tatil.getBasTarih(), Calendar.MINUTE, dakika));
						}
						tatil.setBitGun(PdksUtil.getDate(tariheGunEkleCikar(cal, tarih, 1)));
						tatil.setBitTarih((Date) orjTatil.getBitGun());
						tatil.setBasGun(orjTatil.getBasTarih());
						tatilMap.put(tarihStr, tatil);
					}
					tarih = tariheGunEkleCikar(cal, tarih, 1);
					ilkGun = Boolean.FALSE;
				}

			}
		}
		if (perList != null && tatilMap != null && !tatilMap.isEmpty()) {
			cal = Calendar.getInstance();
			List<Long> perIdList = new ArrayList<Long>();
			try {
				if (perList != null)
					for (Personel personel : perList) {
						if (personel.getId() != null) {
							perIdList.add(personel.getId());
						}
					}
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

			for (String key : tatilMap.keySet()) {
				Tatil tatilIslem = tatilMap.get(key);
				if (tatilIslem.isYarimGunMu()) {
					Date tarihi = PdksUtil.getDate(tatilIslem.getBasTarih());
					map.clear();
					// map.put("durum=", Boolean.TRUE);
					map.put("basTarih<=", tarihi);
					map.put("bitTarih>=", tarihi);
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<ArifeVardiyaDonem> arifeTatilList = pdksEntityController.getObjectByInnerObjectListInLogic(map, ArifeVardiyaDonem.class);
					boolean arifeVardiyaHesapla = false;
					TreeMap<Long, Boolean> idMap = new TreeMap<Long, Boolean>();
					for (Iterator iterator = arifeTatilList.iterator(); iterator.hasNext();) {
						ArifeVardiyaDonem arifeVardiyaDonem = (ArifeVardiyaDonem) iterator.next();
						if (arifeVardiyaDonem.getDurum().equals(Boolean.FALSE))
							iterator.remove();
						if (arifeVardiyaDonem.getVardiya() != null)
							idMap.put(arifeVardiyaDonem.getVardiya().getId(), arifeVardiyaDonem.getDurum());
					}
					map.clear();
					sb = new StringBuffer();
					sb.append("SELECT DISTINCT V.* FROM " + VardiyaGun.TABLE_NAME + " G WITH(nolock) ");
					sb.append(" INNER JOIN " + Vardiya.TABLE_NAME + " V ON V." + Vardiya.COLUMN_NAME_ID + " = G." + VardiyaGun.COLUMN_NAME_VARDIYA);
					sb.append(" WHERE G." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " = :t");
					String fieldName = null;
					if (perIdList != null && !perIdList.isEmpty()) {
						fieldName = "p";
						sb.append(" AND G." + VardiyaGun.COLUMN_NAME_PERSONEL + " :" + fieldName);
						map.put(fieldName, perIdList);
					}
					map.put("t", tarihi);
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<Vardiya> vardiyalar = fieldName != null ? getSQLParamList(perIdList, sb, fieldName, map, Vardiya.class, session) : pdksEntityController.getObjectBySQLList(sb, map, Vardiya.class);
					HashMap<Long, Vardiya> vardiyaMap = new HashMap<Long, Vardiya>();
					Personel p = new Personel();
					List<YemekIzin> yemekDataList = getYemekList(tarihi, null, session);
					Date basArifeTarih = tariheGunEkleCikar(cal, tarihi, -1), bitArifeTarih = PdksUtil.convertToJavaDate("99991231", "yyyyMMdd");
					User sistemUser = null;
					List saveList = new ArrayList();

					for (Vardiya vardiyaTatil : vardiyalar) {
						VardiyaGun tmp = new VardiyaGun(p, vardiyaTatil, tarihi);
						tmp.setVardiyaZamani();
						Vardiya islemVardiya = tmp.getIslemVardiya();
						Date arifeBaslangicTarihi = tatilIslem.getBasTarih();
						CalismaSekli calismaSekli = vardiyaTatil.getCalismaSekli();
						Double arifeCalismaSure = null;
						if (vardiyaTatil.isCalisma()) {
							String tatilStr = !PdksUtil.hasStringValue(arifeTatilBasZaman) ? null : arifeTatilBasZaman;
							ArifeVardiyaDonem arifeVardiyaDonemDB = null;
							for (ArifeVardiyaDonem arifeVardiyaDonem : arifeTatilList) {
								if (arifeVardiyaDonem.getVardiya() != null && !vardiyaTatil.getId().equals(arifeVardiyaDonem.getVardiya().getId()))
									continue;
								tatilStr = arifeVardiyaDonem.getTatilBasZaman();
								arifeVardiyaHesapla = arifeVardiyaDonem.getArifeVardiyaHesapla();
								tatilIslem.setArifeSonraVardiyaDenklestirmeVar(arifeVardiyaDonem.getArifeSonraVardiyaDenklestirmeVar());
								islemVardiya.setArifeCalismaSaatYokCGSDus(arifeVardiyaDonem.getArifeCalismaSaatYokCGSDus());
								arifeVardiyaDonemDB = arifeVardiyaDonem;
								if (arifeVardiyaDonem.getVardiya() != null)
									break;
							}
							Double arifeNormalCalismaDakika = null;
							if (tatilStr != null) {
								String dateStr = PdksUtil.convertToDateString(arifeBaslangicTarihi, "yyyyMMdd") + " " + tatilStr;
								Date yeniZaman = PdksUtil.convertToJavaDate(dateStr, "yyyyMMdd HH:mm");
								if (yeniZaman != null) {
									if (idMap.containsKey(vardiyaTatil.getId())) {
										if (idMap.get(vardiyaTatil.getId()) && yeniZaman.before(islemVardiya.getVardiyaBasZaman()))
											yeniZaman = tariheGunEkleCikar(cal, yeniZaman, 1);
									}
									arifeBaslangicTarihi = yeniZaman;
								}

							} else if ((calismaSekli != null || (vardiyaTatil.getArifeNormalCalismaDakika() != null && vardiyaTatil.getArifeNormalCalismaDakika() != 0.0d))) {
								arifeNormalCalismaDakika = calismaSekli != null ? calismaSekli.getArifeNormalCalismaDakika() : null;
								boolean sureTanimli = false;
								Double netSure = vardiyaTatil.getNetCalismaSuresi();
								if (vardiyaTatil.getArifeNormalCalismaDakika() != null && vardiyaTatil.getArifeNormalCalismaDakika() != 0.0d) {
									sureTanimli = true;
									arifeNormalCalismaDakika = vardiyaTatil.getArifeNormalCalismaDakika();
								}

								else if (arifeNormalCalismaDakika == null || arifeNormalCalismaDakika.doubleValue() == 0.0d)
									arifeNormalCalismaDakika = null;
								if (arifeNormalCalismaDakika == null)
									arifeNormalCalismaDakika = (netSure * 60.0d + vardiyaTatil.getYemekSuresi()) * 0.5d;
								if (arifeNormalCalismaDakika != null) {
									double yarimGunSureDakika = netSure * 30;
									if (sureTanimli) {
										yarimGunSureDakika = arifeNormalCalismaDakika;
									} else {
										if (netSure < 7.5d)
											arifeNormalCalismaDakika = 225.0d;
										else if (netSure > 9.0d)
											arifeNormalCalismaDakika = 270.0d;
										else
											arifeNormalCalismaDakika = yarimGunSureDakika;
									}
									yarimGunSureDakika = arifeNormalCalismaDakika / 60.0d;
									double yemekSure = 0.0d;
									cal.setTime(islemVardiya.getVardiyaBasZaman());
									cal.add(Calendar.MINUTE, new Double(arifeNormalCalismaDakika).intValue() - 30);
									List<Liste> list = new ArrayList<Liste>();
									arifeBaslangicTarihi = cal.getTime();
									Date baslangicZaman = islemVardiya.getVardiyaBasZaman(), bitisZaman = islemVardiya.getVardiyaBitZaman();
									while (arifeBaslangicTarihi.before(bitisZaman) && list.isEmpty()) {
										arifeBaslangicTarihi = cal.getTime();
										Double sure = getSaatSure(baslangicZaman, arifeBaslangicTarihi, yemekDataList, tmp, session) * 60.0d;
										cal.add(Calendar.MINUTE, 5);
										if (sure.doubleValue() == arifeNormalCalismaDakika.doubleValue()) {
											sure = getSaatSure(baslangicZaman, arifeBaslangicTarihi, yemekDataList, tmp, session) * 60.0d;
											list.add(new Liste(arifeBaslangicTarihi, sure));
										}
									}
									if (list.isEmpty()) {
										cal.setTime(islemVardiya.getVardiyaBasZaman());
										cal.add(Calendar.MINUTE, arifeNormalCalismaDakika.intValue());
										arifeBaslangicTarihi = cal.getTime();
										Double sure = yarimGunSureDakika + (yemekSure / 60.0d) - getSaatSure(islemVardiya.getVardiyaBasZaman(), arifeBaslangicTarihi, yemekDataList, tmp, session);
										if (sure.doubleValue() != 0.0d) {
											cal.setTime(arifeBaslangicTarihi);
											int dakika = new Double(sure * 60).intValue();
											cal.add(Calendar.MINUTE, dakika);
											arifeBaslangicTarihi = cal.getTime();
										}
									} else {
										arifeBaslangicTarihi = (Date) list.get(0).getId();
									}

									list = null;
									if (arifeVardiyaDonemDB == null && !idMap.containsKey(vardiyaTatil.getId())) {
										basArifeTarih = PdksUtil.getDate(tarihi);
										arifeVardiyaDonemDB = new ArifeVardiyaDonem();
										arifeVardiyaDonemDB.setVardiya(vardiyaTatil);
										arifeVardiyaDonemDB.setBasTarih(basArifeTarih);
										arifeVardiyaDonemDB.setBitTarih(bitArifeTarih);
										arifeVardiyaDonemDB.setTatilBasZaman(PdksUtil.convertToDateString(arifeBaslangicTarihi, "HH:mm:ss"));
										if (sistemUser == null)
											sistemUser = getSistemAdminUser(session);
										arifeVardiyaDonemDB.setOlusturanUser(sistemUser);
										arifeVardiyaDonemDB.setOlusturmaTarihi(new Date());
										arifeVardiyaDonemDB.setVersion(0);
										arifeVardiyaDonemDB.setDurum(Boolean.TRUE);
										saveList.add(arifeVardiyaDonemDB);
									}
								}

							}

							if (arifeVardiyaHesapla) {
								arifeCalismaSure = 0.0d;
								if (arifeBaslangicTarihi.after(islemVardiya.getVardiyaBasZaman())) {
									arifeCalismaSure = getSaatSure(islemVardiya.getVardiyaBasZaman(), arifeBaslangicTarihi.before(islemVardiya.getVardiyaBitZaman()) ? arifeBaslangicTarihi : islemVardiya.getVardiyaBitZaman(), yemekDataList, tmp, session);
								}
							}

						}
						if (!arifeTatilList.isEmpty())
							islemVardiya.setArifeCalismaSure(arifeCalismaSure);
						islemVardiya.setArifeBaslangicTarihi(arifeBaslangicTarihi);
						tmp = null;
						vardiyaMap.put(islemVardiya.getId(), islemVardiya);
					}
					if (!saveList.isEmpty()) {
						for (Iterator iterator = saveList.iterator(); iterator.hasNext();) {
							Object object = (Object) iterator.next();
							pdksEntityController.saveOrUpdate(session, entityManager, object);
						}
						session.flush();
					}
					idMap = null;
					saveList = null;
					p = null;
					tatilIslem.setVardiyaMap(vardiyaMap);
				}

			}

		}
		if (!tatilMap.isEmpty()) {
			pattern = "yyyyMMdd";
			for (String dateStr : tatilMap.keySet()) {
				String afterDateStr = PdksUtil.convertToDateString(tariheGunEkleCikar(cal, PdksUtil.convertToJavaDate(dateStr, pattern), 1), pattern);
				if (tatilMap.containsKey(afterDateStr)) {
					Tatil tatil = tatilMap.get(dateStr), sonrakiTatil = tatilMap.get(afterDateStr);
					if (!sonrakiTatil.isYarimGunMu() && !tatil.getAd().equals(sonrakiTatil.getAd())) {
						tatil.setBitTarih(sonrakiTatil.getBitTarih());
					}
				}
			}
		}
		return tatilMap;
	}

	/**
	 * @param user
	 * @param izinSahibi
	 * @param izinTipi
	 * @param kidemYil
	 * @param session
	 * @throws Exception
	 */
	private void bakiyeIzniOlustur(User user, Personel izinSahibi, IzinTipi izinTipi, int kidemYil, Session session) throws Exception {
		PersonelIzin personelIzin = null;

		if (izinTipi != null) {
			double sure = izinTipi.getKotaBakiye() != null ? izinTipi.getKotaBakiye() : 0D;
			Calendar cal = Calendar.getInstance();
			int yil = cal.get(Calendar.YEAR);

			cal.set(yil, 0, 1);
			Date baslangicZamani = PdksUtil.getDate(cal.getTime());
			Date bitisZamani = baslangicZamani;
			personelIzin = getBakiyeIzin(user, izinSahibi, baslangicZamani, izinTipi, null, kidemYil, session);

			if (personelIzin == null) {
				personelIzin = new PersonelIzin();
				personelIzin.setIzinSahibi(izinSahibi);
				personelIzin.setBaslangicZamani(baslangicZamani);
				personelIzin.setBitisZamani(bitisZamani);
				personelIzin.setAciklama(yil + " " + izinTipi.getIzinTipiTanim().getAciklama());
				personelIzin.setIzinTipi(izinTipi);
				personelIzin.setIzinSuresi(0d);
				personelIzin.setKullanilanIzinSuresi(0D);
				personelIzin.setOlusturanUser(user);
			}
			if (personelIzin.getIzinKagidiGeldi() == null && personelIzin.getIzinSuresi() != sure) {
				if (personelIzin.getId() != null) {
					personelIzin.setGuncellemeTarihi(new Date());
					if (user != null)
						personelIzin.setGuncelleyenUser(user);
				}
				personelIzin.setIzinSuresi(sure);
				pdksEntityController.saveOrUpdate(session, entityManager, personelIzin);
			}
		}
		if (personelIzin != null) {
			double kalanIzin = personelIzin.getKalanIzin();
			if (kalanIzin > 0)
				izinSahibi.putIzinBakiyeMap(izinTipi.getBakiyeIzinTipi().getIzinTipiTanim().getKodu(), kalanIzin);

		}

	}

	/**
	 * @param izinSahibi
	 * @param izinTipiMap
	 * @param session
	 * @param izinTipi
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@Transactional
	private IzinTipi suaIzinOlustur(Personel izinSahibi, HashMap<String, IzinTipi> izinTipiMap, Session session, IzinTipi izinTipi, User user) throws Exception {
		Calendar cal = Calendar.getInstance();
		Date bugun = PdksUtil.getDate(cal.getTime());
		int yil = cal.get(Calendar.YEAR);

		HashMap map = new HashMap();
		int yillikIzinMaxBakiye = PersonelIzin.getSuaIzinMaxBakiye();
		if (izinTipi == null) {
			map.clear();
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
			map.put("bakiyeIzinTipi.izinTipiTanim.kodu", IzinTipi.SUA_IZNI);
			map.put("bakiyeIzinTipi.departman", izinSahibi.getSirket().getDepartman());
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				izinTipi = (IzinTipi) pdksEntityController.getObjectByInnerObject(map, IzinTipi.class);

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());

				izinTipi = null;
			}

		}

		if (izinTipi != null) {
			boolean tarihGelmedi = suaYillikOlustur(izinSahibi, session, izinTipi, user, bugun, yil, yillikIzinMaxBakiye);
			if (!tarihGelmedi) {
				suaYillikOlustur(izinSahibi, session, izinTipi, user, bugun, yil + 1, yillikIzinMaxBakiye);
			}

		}
		session.flush();
		return izinTipi;

	}

	/**
	 * @param entityManagerInput
	 * @param pdksEntityControllerInput
	 */
	public void setInject(EntityManager entityManagerInput, PdksEntityController pdksEntityControllerInput) {
		if (entityManagerInput != null && entityManager == null)
			this.entityManager = entityManagerInput;
		if (pdksEntityControllerInput != null && pdksEntityController == null)
			this.pdksEntityController = pdksEntityControllerInput;

	}

	/**
	 * @param izinSahibi
	 * @param session
	 * @param izinTipi
	 * @param user
	 * @param bugun
	 * @param yil
	 * @param yillikIzinMaxBakiye
	 * @return
	 * @throws Exception
	 */
	private boolean suaYillikOlustur(Personel izinSahibi, Session session, IzinTipi izinTipi, User user, Date bugun, int yil, int yillikIzinMaxBakiye) throws Exception {
		PersonelIzin personelIzin = null;
		Calendar cal = Calendar.getInstance();
		cal.set(yil, 0, 1);
		Date baslangicZamani = PdksUtil.getDate(cal.getTime());
		cal.setTime(izinSahibi.getIzinHakEdisTarihi());
		cal.set(Calendar.YEAR, yil);
		Date bitisZamani = PdksUtil.getDate(cal.getTime());
		boolean tarihGelmedi = bitisZamani.after(bugun);
		if (bitisZamani.after(izinSahibi.getIzinHakEdisTarihi())) {
			HashMap<Integer, Integer> map1 = getTarihMap(izinSahibi != null ? izinSahibi.getIzinHakEdisTarihi() : null, bitisZamani);
			int kidemYil = map1.get(Calendar.YEAR);
			String aciklama = String.valueOf(kidemYil);
			personelIzin = getBakiyeIzin(user, izinSahibi, baslangicZamani, izinTipi, null, kidemYil, session);
			if (personelIzin == null) {
				personelIzin = new PersonelIzin();
				personelIzin.setIzinSahibi(izinSahibi);
				personelIzin.setBaslangicZamani(baslangicZamani);
				personelIzin.setAciklama(aciklama);
				personelIzin.setIzinTipi(izinTipi);
				personelIzin.setIzinSuresi(0D);
				personelIzin.setKullanilanIzinSuresi(0D);
				personelIzin.setOlusturanUser(user);
			}
			if (personelIzin.getIzinKagidiGeldi() == null && !personelIzin.isRedmi()) {
				int izinSuresi = yillikIzinMaxBakiye;
				if (personelIzin.getGuncellemeTarihi() != null && !PdksUtil.getDate(bugun).after(PdksUtil.getDate(personelIzin.getGuncellemeTarihi())))
					izinSuresi = personelIzin.getIzinSuresi().intValue();
				if (izinDegisti(personelIzin, bitisZamani, izinSuresi, aciklama)) {
					if (personelIzin.getId() != null) {
						personelIzin.setGuncellemeTarihi(new Date());
						if (user != null)
							personelIzin.setGuncelleyenUser(user);
					}
					personelIzin.setBitisZamani(bitisZamani);
					personelIzin.setAciklama(aciklama);
					personelIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
					personelIzin.setIzinSuresi((double) yillikIzinMaxBakiye);
					pdksEntityController.saveOrUpdate(session, entityManager, personelIzin);
				}
			}
			if (personelIzin != null) {
				double kalanIzin = personelIzin.getKalanIzin();
				if (kalanIzin > 0)
					izinSahibi.putIzinBakiyeMap(izinTipi.getBakiyeIzinTipi().getIzinTipiTanim().getKodu(), kalanIzin);

			}
		}
		return tarihGelmedi;
	}

	/**
	 * @return
	 */
	public int getYillikIzinMaxBakiye() {
		int yillikIzinMaxBakiye = PersonelIzin.getYillikIzinMaxBakiye();
		return yillikIzinMaxBakiye;

	}

	/**
	 * @param veriMap
	 * @param session
	 * @return
	 */
	@Transactional
	public IzinTipi senelikIzinOlustur(HashMap<String, Object> veriMap, Session session) {
		IzinTipi izinTipi = (IzinTipi) veriMap.get("izinTipi");
		int yil = (Integer) veriMap.get("yil");
		if (yil >= PdksUtil.getSistemBaslangicYili()) {
			Personel izinSahibi = (Personel) veriMap.get("izinSahibi");
			boolean suaDurum = (Boolean) veriMap.get("suaDurum");
			HashMap<String, IzinHakedisHakki> hakedisMap = (HashMap<String, IzinHakedisHakki>) veriMap.get("hakedisMap");
			User user = (User) veriMap.get("user");
			boolean yeniBakiyeOlustur = (Boolean) veriMap.get("yeniBakiyeOlustur");
			int kidemYil = (Integer) veriMap.get("kidemYil") - (yeniBakiyeOlustur ? 0 : 1);
			Date islemTarihi = (Date) veriMap.get("islemTarihi");
			User sistemYonetici = (User) veriMap.get("sistemYonetici");
			if (yeniBakiyeOlustur == false)
				logger.debug(yil + " " + kidemYil);
			int sistemKontrolYili = PdksUtil.getSistemBaslangicYili() - 1;
			Calendar cal = Calendar.getInstance();
			Date bugun = PdksUtil.getDate(cal.getTime());
			cal.setTime(islemTarihi);
			// onceki yas tipini de bulalim

			// int yil = cal.get(Calendar.YEAR);
			cal.setTime(izinSahibi.getIzinHakEdisTarihi());
			int girisYil = cal.get(Calendar.YEAR);
			PersonelIzin personelIzin = null;
			HashMap map = new HashMap();
			boolean kidemYok = yeniBakiyeOlustur && girisYil <= yil && kidemYil == 0;
			if (kidemYok)
				logger.debug("");

			int yillikIzinMaxBakiye = getYillikIzinMaxBakiye();

			cal.set(Calendar.YEAR, yil);
			Date izinHakEttigiTarihi = PdksUtil.getDate((Date) cal.getTime().clone());

			boolean tarihGelmedi = PdksUtil.tarihKarsilastirNumeric(izinHakEttigiTarihi, islemTarihi) == 1;
			IzinHakedisHakki izinHakedisHakki = null;
			int genelDirektorIzinSuresi = 0;
			String genelDirektorIzinSuresiPrm = getParameterKey("genelDirektorIzinSuresi");
			boolean flush = false;
			try {
				if (PdksUtil.hasStringValue(genelDirektorIzinSuresiPrm) && izinSahibi.isGenelDirektor() && getGenelMudur(null, izinSahibi, session))
					try {
						genelDirektorIzinSuresi = Integer.parseInt(genelDirektorIzinSuresiPrm);
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());
						genelDirektorIzinSuresi = 0;
					}
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				genelDirektorIzinSuresi = 0;
			}

			if (izinTipi == null) {
				map.clear();
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
				map.put("bakiyeIzinTipi.izinTipiTanim.kodu", IzinTipi.YILLIK_UCRETLI_IZIN);
				map.put("bakiyeIzinTipi.departman.id", izinSahibi.getSirket().getDepartman().getId());
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				try {
					izinTipi = (IzinTipi) pdksEntityController.getObjectByInnerObject(map, IzinTipi.class);

				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

					izinTipi = null;
				}

			}
			HashMap<Integer, Integer> kidemMap = getTarihMap(izinSahibi != null ? izinSahibi.getDogumTarihi() : null, islemTarihi);
			int yas = kidemMap.get(Calendar.YEAR);
			int yasTipi = IzinHakedisHakki.YAS_TIPI_GENC;
			Departman departman = izinSahibi.getSirket().getDepartman();
			if (departman.getCocukYasUstSiniri() >= yas)
				yasTipi = IzinHakedisHakki.YAS_TIPI_COCUK;
			else if (departman.getYasliYasAltSiniri() <= yas)
				yasTipi = IzinHakedisHakki.YAS_TIPI_YASLI;
			boolean kidemEkle = false;
			if (izinTipi != null) {
				if (yillikIzinMaxBakiye < 0 && (kidemYil == 0 || tarihGelmedi)) {
					kidemEkle = kidemYil == 0;
					if (tarihGelmedi && yeniBakiyeOlustur)
						kidemYil++;
					kidemMap.put(Calendar.YEAR, kidemYil);
					if (kidemEkle) {
						kidemYil++;
						cal.setTime(izinHakEttigiTarihi);
						if (yil == girisYil)
							cal.add(Calendar.YEAR, 1);
						izinHakEttigiTarihi = cal.getTime();
					}

				}

				if (kidemYil > 0 || kidemEkle) {
					if (!tarihGelmedi)
						izinHakedisHakki = getIzinHakedis(kidemYil, hakedisMap, session, yasTipi, suaDurum, departman, map);
					else {
						if (yillikIzinMaxBakiye > 0) {
							izinHakedisHakki = new IzinHakedisHakki();
							izinHakedisHakki.setIzinSuresi(yillikIzinMaxBakiye);
						} else
							izinHakedisHakki = getIzinHakedis(kidemYil, hakedisMap, session, yasTipi, suaDurum, departman, map);
					}

					if (izinHakedisHakki != null && izinTipi != null) {
						Date baslangicZamani = PdksUtil.getDate(cal.getTime());
						cal.setTime(izinSahibi.getIzinHakEdisTarihi());
						cal.set(Calendar.YEAR, yil);
						if (girisYil == yil)
							cal.add(Calendar.YEAR, 1);
						izinHakEttigiTarihi = PdksUtil.getDate(cal.getTime());
						Date kidemTarih = kidemYok || yeniBakiyeOlustur == false || tarihGelmedi ? izinHakEttigiTarihi : PdksUtil.getDate(bugun);
						kidemMap = getTarihMap(izinSahibi != null ? izinSahibi.getIzinHakEdisTarihi() : null, kidemTarih);
						kidemYil = kidemMap.get(Calendar.YEAR);
						HashMap<Integer, Integer> yasMap = getTarihMap(izinSahibi != null ? izinSahibi.getDogumTarihi() : null, kidemTarih);
						int yasYeni = yasMap.get(Calendar.YEAR);
						if (yasYeni != yas) {
							yasTipi = IzinHakedisHakki.YAS_TIPI_GENC;
							if (departman.getCocukYasUstSiniri() >= yasYeni)
								yasTipi = IzinHakedisHakki.YAS_TIPI_COCUK;
							else if (departman.getYasliYasAltSiniri() <= yasYeni)
								yasTipi = IzinHakedisHakki.YAS_TIPI_YASLI;
							izinHakedisHakki = getIzinHakedis(kidemYil, hakedisMap, session, yasTipi, suaDurum, departman, map);
						}
						String aciklama = String.valueOf(kidemYil);
						if (genelDirektorIzinSuresi != 0)
							izinHakedisHakki.setIzinSuresi(genelDirektorIzinSuresi);
						double izinSuresi = tarihGelmedi && yillikIzinMaxBakiye > 0 ? yillikIzinMaxBakiye : (double) izinHakedisHakki.getIzinSuresi();
						try {
							personelIzin = getBakiyeIzin(sistemYonetici, izinSahibi, baslangicZamani, izinTipi, izinSuresi, kidemYil, session);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (personelIzin != null)
							flush = personelIzin.isCheckBoxDurum();
						Date oncekiHakedisTarihi = PdksUtil.addTarih(izinHakEttigiTarihi, Calendar.YEAR, -1);
						if (yil > sistemKontrolYili && (personelIzin != null || bugun.after(oncekiHakedisTarihi))) {
							if (personelIzin == null) {
								personelIzin = new PersonelIzin();
								personelIzin.setIzinSahibi(izinSahibi);
								personelIzin.setBaslangicZamani(baslangicZamani);
								personelIzin.setBitisZamani(izinHakEttigiTarihi);
								personelIzin.setIzinTipi(izinTipi);
								personelIzin.setIzinSuresi(izinSuresi);
								personelIzin.setKullanilanIzinSuresi(0D);
							}
							if (personelIzin.getId() == null)
								personelIzin.setOlusturanUser(sistemYonetici);
							if (personelIzin.getIzinKagidiGeldi() == null) {
								if (personelIzin.getGuncellemeTarihi() != null && !PdksUtil.getDate(islemTarihi).after(PdksUtil.getDate(personelIzin.getGuncellemeTarihi())))
									izinSuresi = personelIzin.getIzinSuresi().intValue();
								if (genelDirektorIzinSuresi != 0)
									izinSuresi = genelDirektorIzinSuresi;
								if (izinDegisti(personelIzin, izinHakEttigiTarihi, izinSuresi, aciklama)) {
									if (personelIzin.getId() != null) {
										if (user != null)
											personelIzin.setGuncelleyenUser(user);
										personelIzin.setGuncellemeTarihi(new Date());
									}
									personelIzin.setIzinSuresi(izinSuresi);
									personelIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
									personelIzin.setAciklama(aciklama);
									if (izinHakEttigiTarihi.getTime() >= personelIzin.getBaslangicZamani().getTime()) {
										personelIzin.setBitisZamani(izinHakEttigiTarihi);
										pdksEntityController.saveOrUpdate(session, entityManager, personelIzin);
										flush = true;
									} else
										logger.info(personelIzin.getPdksPersonel().getPdksSicilNo() + " " + aciklama);

								}
							}
						}
					}
				}
				cal.setTime(izinHakEttigiTarihi);
				// cal.add(Calendar.YEAR, -1);
				Date hakedisTarihi = cal.getTime();
				boolean yeniKidemBakiyeOlustur = yeniBakiyeOlustur && bugun.after(hakedisTarihi) && kidemYok == false;
				if (yeniKidemBakiyeOlustur) {
					if (((tarihGelmedi == false || yillikIzinMaxBakiye < 0) && kidemYil > 0) || yil == girisYil)
						++yil;
					cal.set(yil, 0, 1);
					Date baslangicZamani = PdksUtil.convertToJavaDate(yil + "0101", "yyyyMMdd");
					cal.setTime(izinSahibi.getIzinHakEdisTarihi());
					cal.set(Calendar.YEAR, yil);
					izinHakEttigiTarihi = PdksUtil.getDate(cal.getTime());
					if (baslangicZamani.after(izinSahibi.getIzinHakEdisTarihi())) {
						if (yillikIzinMaxBakiye > 0) {
							izinHakedisHakki = new IzinHakedisHakki();
							izinHakedisHakki.setIzinSuresi(yillikIzinMaxBakiye);
						} else
							izinHakedisHakki = getIzinHakedis(kidemYil + 1, hakedisMap, session, yasTipi, suaDurum, departman, map);

						HashMap<Integer, Integer> map1 = getTarihMap(izinSahibi != null ? izinSahibi.getIzinHakEdisTarihi() : null, izinHakEttigiTarihi);
						kidemYil = map1.get(Calendar.YEAR);
						String aciklama = String.valueOf(kidemYil);
						double izinSuresi = (double) izinHakedisHakki.getIzinSuresi();
						if (genelDirektorIzinSuresi != 0)
							izinSuresi = genelDirektorIzinSuresi;
						try {
							personelIzin = getBakiyeIzin(sistemYonetici, izinSahibi, baslangicZamani, izinTipi, izinSuresi, kidemYil, session);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (flush == false && personelIzin != null)
							flush = personelIzin.isCheckBoxDurum();
						if (personelIzin == null) {
							personelIzin = new PersonelIzin();
							personelIzin.setIzinSahibi(izinSahibi);
							personelIzin.setBaslangicZamani(baslangicZamani);
							personelIzin.setBitisZamani(izinHakEttigiTarihi);
							personelIzin.setAciklama(aciklama);
							personelIzin.setIzinTipi(izinTipi);
							personelIzin.setIzinSuresi(izinSuresi);
							personelIzin.setKullanilanIzinSuresi(0D);
						}
						if (personelIzin.getId() == null)
							personelIzin.setOlusturanUser(sistemYonetici);
						if (personelIzin.getIzinKagidiGeldi() == null) {
							if (kidemYil > 0 && (yeniBakiyeOlustur || personelIzin.getId() != null)) {
								if (izinSuresi > 0 && izinDegisti(personelIzin, izinHakEttigiTarihi, izinSuresi, aciklama)) {
									if (personelIzin.getId() != null) {
										if (user != null)
											personelIzin.setGuncelleyenUser(user);
										personelIzin.setGuncellemeTarihi(new Date());
									}
									personelIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
									personelIzin.setIzinSuresi(izinSuresi);
									personelIzin.setAciklama(aciklama);
									if (izinHakEttigiTarihi.getTime() >= personelIzin.getBaslangicZamani().getTime()) {
										personelIzin.setBitisZamani(izinHakEttigiTarihi);
										pdksEntityController.saveOrUpdate(session, entityManager, personelIzin);
										flush = true;
									} else
										logger.info(personelIzin.getPdksPersonel().getPdksSicilNo() + " " + aciklama);
								}
							}
						}
					}
				}
			}
			if (flush)
				session.flush();
			// if (kidemYil == 0)
			// izinTipi = null;
		}
		return izinTipi;

	}

	/**
	 * @param kidemYil
	 * @param hakedisMap
	 * @param session
	 * @param yasTipi
	 * @param suaDurum
	 * @param departman
	 * @param map
	 * @return
	 */
	private IzinHakedisHakki getIzinHakedis(int kidemYil, HashMap<String, IzinHakedisHakki> hakedisMap, Session session, int yasTipi, boolean suaDurum, Departman departman, HashMap map) {
		IzinHakedisHakki izinHakedisHakki;
		if (hakedisMap == null) {
			map.clear();
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
			map.put("departman.id", departman.getId());
			map.put("yasTipi", yasTipi);
			map.put("kidemYili", kidemYil);
			map.put("suaDurum", suaDurum);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			izinHakedisHakki = (IzinHakedisHakki) pdksEntityController.getObjectByInnerObject(map, IzinHakedisHakki.class);
		} else {
			String hakedisKey = IzinHakedisHakki.getHakedisKey(kidemYil, yasTipi, suaDurum, departman);
			izinHakedisHakki = hakedisMap.get(hakedisKey);
		}
		return izinHakedisHakki;
	}

	/**
	 * @param personelIzin
	 * @param izinHakEttigiTarihi
	 * @param izinSuresi
	 * @param aciklama
	 * @return
	 */
	private boolean izinDegisti(PersonelIzin personelIzin, Date tarih, double izinSuresi, String aciklama) {
		boolean degisti = false;
		try {
			degisti = PdksUtil.isStrDegisti(personelIzin.getAciklama(), aciklama) || PdksUtil.isDateDegisti(tarih, personelIzin.getBitisZamani()) || PdksUtil.isDoubleDegisti(izinSuresi, personelIzin.getIzinSuresi()) || personelIzin.getIzinDurumu() != PersonelIzin.IZIN_DURUMU_ONAYLANDI;

		} catch (Exception e) {
			degisti = false;
			e.printStackTrace();
		}
		return degisti;
	}

	/**
	 * @param tarih
	 * @param bugun
	 * @return
	 */
	public HashMap<Integer, Integer> getTarihMap(Date tarih, Date bugun) {
		HashMap<Integer, Integer> kidemMap = new HashMap<Integer, Integer>();
		Calendar cal = Calendar.getInstance();
		int yil = 0, ay = 0, gun = 0;
		if (tarih != null) {
			bugun = PdksUtil.getDate(bugun);
			Date iseBaslamaTarihi = tarih;
			Date araTarih = (Date) tarih.clone();
			yil = -1;
			ay = -1;
			gun = 0;
			while (PdksUtil.tarihKarsilastirNumeric(bugun, iseBaslamaTarihi) == 1) {
				araTarih = iseBaslamaTarihi;
				iseBaslamaTarihi = addTarih(cal, iseBaslamaTarihi, Calendar.YEAR, 1);
				++yil;
			}
			if (PdksUtil.tarihKarsilastirNumeric(bugun, iseBaslamaTarihi) == 0) {
				++yil;
				ay = 0;
				gun = 0;
			} else {
				iseBaslamaTarihi = araTarih;
				while (PdksUtil.tarihKarsilastirNumeric(bugun, iseBaslamaTarihi) == 1) {
					araTarih = iseBaslamaTarihi;
					iseBaslamaTarihi = addTarih(cal, iseBaslamaTarihi, Calendar.MONTH, 1);
					++ay;
				}
				iseBaslamaTarihi = araTarih;
				while (PdksUtil.tarihKarsilastirNumeric(bugun, iseBaslamaTarihi) == 1) {
					iseBaslamaTarihi = addTarih(cal, iseBaslamaTarihi, Calendar.DATE, 1);
					++gun;
				}
			}

		}
		kidemMap.put(Calendar.YEAR, yil);
		kidemMap.put(Calendar.MONTH, ay);
		kidemMap.put(Calendar.DATE, gun);
		return kidemMap;
	}

	/**
	 * @param session
	 * @return
	 */
	public HashMap<Long, KapiView> fillPDKSKapilari(Session session) {
		HashMap parametreMap = new HashMap();
		List<String> hareketTip = new ArrayList<String>();
		hareketTip.add(Kapi.TIPI_KODU_GIRIS);
		hareketTip.add(Kapi.TIPI_KODU_CIKIS);
		parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "id");
		parametreMap.put("tipi", Tanim.TIPI_KAPI_TIPI);
		parametreMap.put("kodu", hareketTip);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

		List<Long> kapiTipleri = pdksEntityController.getObjectByInnerObjectList(parametreMap, Tanim.class);
		parametreMap.clear();
		parametreMap.put("pdks", Boolean.TRUE);
		// parametreMap.put("tipi.kodu", hareketTip);
		parametreMap.put("tipi.id", kapiTipleri);
		// parametreMap.put("durum", Boolean.TRUE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Kapi> kapilar = pdksEntityController.getObjectByInnerObjectList(parametreMap, Kapi.class);
		HashMap<Long, KapiView> kapiMap = new HashMap<Long, KapiView>();
		HashMap<String, HashMap<Integer, KapiKGS>> dMap = new HashMap<String, HashMap<Integer, KapiKGS>>();
		for (Kapi kapi : kapilar) {
			KapiKGS kapiKGS = kapi.getKapiKGS();
			kapiKGS.setBagliKapiKGS(null);
			if (kapiKGS.isKapiDegistirir() && kapiKGS.isManuel() == false && kapiKGS.getKapiSirket() != null && kapiKGS.getTerminalNo() != null) {
				String key = kapiKGS.getKapiSirket().getId() + "_" + kapiKGS.getTerminalNo();
				HashMap<Integer, KapiKGS> map = dMap.containsKey(key) ? dMap.get(key) : new HashMap<Integer, KapiKGS>();
				if (map.isEmpty())
					dMap.put(key, map);
				if (!map.containsKey(kapiKGS.getKartYonu()))
					map.put(kapiKGS.getKartYonu(), kapiKGS);
				else
					dMap.remove(key);
			}
			kapiMap.put(kapiKGS.getId(), kapi.getKapiNewView());
		}
		if (!dMap.isEmpty()) {
			for (String key : dMap.keySet()) {
				HashMap<Integer, KapiKGS> map = dMap.get(key);
				if (map.size() == 2) {
					for (Integer yon1 : map.keySet()) {
						KapiKGS kapiKGS = map.get(yon1);
						for (Integer yon2 : map.keySet()) {
							if (yon1 != yon2) {
								kapiKGS.setBagliKapiKGS(map.get(yon2));
								break;
							}
						}
					}
				}
			}
		}
		dMap = null;
		return kapiMap;
	}

	/**
	 * @param kgsPerIdler
	 * @param vardiyaBas
	 * @param vardiyaBit
	 * @param session
	 * @return
	 */
	public HashMap<Long, ArrayList<HareketKGS>> fillPersonelKGSHareketMap(List<Long> kgsPerIdler, Date vardiyaBas, Date vardiyaBit, Session session) {
		HashMap<Long, KapiView> kapiMap = fillPDKSKapilari(session);
		List<Long> kapiIdIList = new ArrayList<Long>(kapiMap.keySet());
		if (vardiyaBas != null && vardiyaBit != null) {
			try {
				Calendar cal = Calendar.getInstance();
				Date tarih1 = tariheGunEkleCikar(cal, vardiyaBas, -7);
				Date tarih2 = tariheGunEkleCikar(cal, vardiyaBit, 7);
				for (Iterator iterator = kapiIdIList.iterator(); iterator.hasNext();) {
					Long key = (Long) iterator.next();
					KapiKGS kapiKGS = kapiMap.get(key).getKapiKGS();
					KapiSirket kapiSirket = kapiKGS.getKapiSirket();
					if (kapiSirket != null) {
						if (kapiSirket.getBitTarih() != null && tarih1.getTime() <= kapiSirket.getBitTarih().getTime() && kapiSirket.getBasTarih() != null && tarih2.getTime() >= kapiSirket.getBasTarih().getTime())
							continue;
						else
							iterator.remove();
					}
				}
			} catch (Exception e) {
			}
		}

		List<BasitHareket> hareketList = null;

		try {
			hareketList = getHareketBilgileri(kapiIdIList, kgsPerIdler, vardiyaBas, vardiyaBit, BasitHareket.class, session);

		} catch (Exception e) {
			hareketList = new ArrayList<BasitHareket>();
			e.printStackTrace();
		}

		HashMap<Long, ArrayList<HareketKGS>> personelHareketMap = new HashMap<Long, ArrayList<HareketKGS>>();
		if (!hareketList.isEmpty()) {
			TreeMap<Long, KapiSirket> kapiSirketMap = new TreeMap<Long, KapiSirket>();
			List<Long> idList = new ArrayList<Long>();
			for (Iterator iterator = hareketList.iterator(); iterator.hasNext();) {
				BasitHareket basitHareket = (BasitHareket) iterator.next();
				if (!idList.contains(basitHareket.getKgsSirketId()))
					idList.add(basitHareket.getKgsSirketId());
			}
			if (!idList.isEmpty()) {
				HashMap map = new HashMap();
				map.put("id", idList);
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<KapiSirket> kapiSirketList = pdksEntityController.getObjectByInnerObjectList(map, KapiSirket.class);
				for (KapiSirket kapiSirket : kapiSirketList) {
					kapiSirketMap.put(kapiSirket.getId(), kapiSirket);
				}
				kapiSirketList = null;

			}
			Long perNoId = null;
			if (hareketList.size() > 1)
				hareketList = PdksUtil.sortListByAlanAdi(hareketList, "zaman", Boolean.FALSE);
			for (BasitHareket basitHareket : hareketList) {
				if (basitHareket.getDurum() == BasitHareket.DURUM_AKTIF) {
					perNoId = basitHareket.getPersonelId();
					HareketKGS hareket = basitHareket.getKgsHareket();
					if (hareket.getKgsSirketId() != null && kapiSirketMap.containsKey(hareket.getKgsSirketId()))
						hareket.setKapiSirket(kapiSirketMap.get(hareket.getKgsSirketId()));
					hareket.setKapiView(kapiMap.get(basitHareket.getKapiId()));
					ArrayList<HareketKGS> perHareketList = personelHareketMap.containsKey(perNoId) ? personelHareketMap.get(perNoId) : new ArrayList<HareketKGS>();
					perHareketList.add(hareket);
					personelHareketMap.put(perNoId, perHareketList);
				}
			}
		}
		return personelHareketMap;
	}

	/**
	 * @param sb
	 * @param map
	 * @param tableName
	 * @param class1
	 * @return
	 */
	public TreeMap getDataByIdMap(StringBuffer sb, HashMap map, String tableName, Class class1) {
		TreeMap map1 = null;
		String fonksiyonAdi = map.containsKey(PdksEntityController.MAP_KEY_MAP) ? (String) map.get(PdksEntityController.MAP_KEY_MAP) : null;
		if (fonksiyonAdi != null)
			map.remove(PdksEntityController.MAP_KEY_MAP);
		Session session = map.containsKey(PdksEntityController.MAP_KEY_SESSION) ? (Session) map.get(PdksEntityController.MAP_KEY_SESSION) : PdksUtil.getSessionUser(entityManager, authenticatedUser);

		List idler = fonksiyonAdi != null ? pdksEntityController.getObjectBySQLList(sb, map, null) : null;
		if (idler != null && !idler.isEmpty()) {
			map.clear();
			sb = null;
			sb = new StringBuffer();
			sb.append("SELECT V.* FROM " + tableName + " v WITH(nolock) ");
			sb.append(" WHERE " + BaseObject.COLUMN_NAME_ID + ":id");
			map.put("id", idler);
			map.put(PdksEntityController.MAP_KEY_MAP, fonksiyonAdi);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			map1 = pdksEntityController.getObjectBySQLMap(sb, map, class1, false);
		}
		if (map1 == null)
			map1 = new TreeMap();
		return map1;
	}

	/**
	 * @param sb
	 * @param map
	 * @param tableName
	 * @param class1
	 * @param idColumn
	 * @return
	 */
	public List getDataByIdList(StringBuffer sb, HashMap map, String tableName, Class class1, String idColumn) {
		List list = null;
		Session session = map.containsKey(PdksEntityController.MAP_KEY_SESSION) ? (Session) map.get(PdksEntityController.MAP_KEY_SESSION) : PdksUtil.getSessionUser(entityManager, authenticatedUser);
		List idler = pdksEntityController.getObjectBySQLList(sb, map, null);
		if (idler != null && !idler.isEmpty()) {
			String fieldName = "id";
			if (idColumn == null)
				idColumn = BaseObject.COLUMN_NAME_ID;
			map.clear();
			sb = null;
			sb = new StringBuffer();
			sb.append("SELECT V.* FROM " + tableName + " V WITH(nolock) ");
			sb.append(" WHERE V." + idColumn + " :" + fieldName);
			map.put(fieldName, idler);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			list = getSQLParamList(idler, sb, fieldName, map, class1, session);

		}
		if (list == null)
			list = new ArrayList();
		return list;
	}

	/**
	 * @param sb
	 * @param map
	 * @param tableName
	 * @param class1
	 * @return
	 */
	public List getDataByIdList(StringBuffer sb, HashMap map, String tableName, Class class1) {
		List list = getDataByIdList(sb, map, tableName, class1, null);
		return list;
	}

	/**
	 * @param perIdList
	 * @param baslamaTarih
	 * @param bitisTarih
	 * @param session
	 * @return
	 */
	public HashMap<Long, List<PersonelIzin>> getPersonelIzinMap(List<Long> perIdList, Date baslamaTarih, Date bitisTarih, Session session) {
		HashMap<Long, List<PersonelIzin>> izinMap = new HashMap<Long, List<PersonelIzin>>();
		if (perIdList != null && !perIdList.isEmpty()) {
			String fieldName = "pId";
			HashMap fields = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT  I.* FROM " + PersonelIzin.TABLE_NAME + " I WITH(nolock) ");
			sb.append(" INNER JOIN " + IzinTipi.TABLE_NAME + " T ON T." + IzinTipi.COLUMN_NAME_ID + " = I." + PersonelIzin.COLUMN_NAME_IZIN_TIPI + " AND T." + IzinTipi.COLUMN_NAME_BAKIYE_IZIN_TIPI + " IS NULL");
			sb.append(" WHERE I." + PersonelIzin.COLUMN_NAME_PERSONEL + " :" + fieldName);
			sb.append(" AND I." + PersonelIzin.COLUMN_NAME_BITIS_ZAMANI + " >= :basTarih AND I." + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + " <= :bitTarih");
			sb.append(" AND I." + PersonelIzin.COLUMN_NAME_IZIN_DURUMU + " NOT IN (" + PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL + "," + PersonelIzin.IZIN_DURUMU_REDEDILDI + ")");
			sb.append(" ORDER BY I." + PersonelIzin.COLUMN_NAME_PERSONEL + ",I." + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI);
			fields.put("bitTarih", bitisTarih);
			fields.put("basTarih", baslamaTarih);
			fields.put(fieldName, perIdList);
			List<PersonelIzin> izinList = getSQLParamList(perIdList, sb, fieldName, fields, PersonelIzin.class, session);
			for (PersonelIzin izin : izinList) {
				Long id = izin.getIzinSahibi().getId();
				List<PersonelIzin> list = izinMap.containsKey(id) ? izinMap.get(id) : new ArrayList<PersonelIzin>();
				if (list.isEmpty()) {
					logger.debug(id);
					izinMap.put(id, list);
				}
				list.add(izin);
			}
			izinList = null;
		}
		return izinMap;
	}

	/**
	 * @param personeller
	 * @param baslamaTarih
	 * @param bitisTarih
	 * @param veriYaz
	 * @param session
	 * @param zamanGuncelle
	 * @return
	 * @throws Exception
	 */
	public TreeMap<String, VardiyaGun> getIslemVardiyalar(List<Personel> personeller, Date baslamaTarih, Date bitisTarih, boolean veriYaz, Session session, boolean zamanGuncelle) throws Exception {
		Calendar cal = Calendar.getInstance();
		HashMap<Long, List<PersonelIzin>> izinMap = getPersonelIzinMap(getBaseObjectIdList(personeller), tariheGunEkleCikar(cal, baslamaTarih, -1), tariheGunEkleCikar(cal, bitisTarih, 1), session);
		TreeMap<String, VardiyaGun> vardiyaMap = getVardiyalar(personeller, tariheGunEkleCikar(cal, baslamaTarih, -3), tariheGunEkleCikar(cal, bitisTarih, 3), izinMap, veriYaz, session, zamanGuncelle);
		fazlaMesaiSaatiAyarla(vardiyaMap);
		Long id = null;
		Date tarih1 = null, tarih2 = null;
		TreeMap<String, VardiyaGun> vardiyaSonucMap = new TreeMap<String, VardiyaGun>();
		for (String key : vardiyaMap.keySet()) {
			VardiyaGun vardiyaGun = vardiyaMap.get(key);
			if (vardiyaGun.getVardiyaDate().before(baslamaTarih) || vardiyaGun.getVardiyaDate().after(bitisTarih)) {
				continue;
			}
			Personel personel = vardiyaGun.getPersonel();
			if (id == null || !id.equals(personel.getId())) {
				id = personel.getId();
				tarih1 = personel.getIseGirisTarihi();
				tarih2 = personel.getSskCikisTarihi();
			}
			if (vardiyaGun.getVardiyaDate().before(tarih1) || vardiyaGun.getVardiyaDate().after(tarih2)) {
				continue;
			}
			vardiyaSonucMap.put(key, vardiyaGun);
		}
		return vardiyaSonucMap;
	}

	/**
	 * @param list
	 * @return
	 */
	public List<Long> getBaseObjectIdList(List list) {
		List<Long> idList = null;
		if (list != null && !list.isEmpty()) {
			idList = new ArrayList<Long>();
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Object object = (Object) iterator.next();
				try {
					if (object == null)
						continue;
					if (object instanceof BaseObject) {
						BaseObject bo = (BaseObject) object;
						if (bo.getId() != null)
							idList.add(bo.getId());
					}
				} catch (Exception e) {
					// TODO: handle exception
				}

			}
			if (idList.isEmpty())
				idList = null;
		}
		return idList;
	}

	/**
	 * @param personeller
	 * @param baslamaTarih
	 * @param bitisTarih
	 * @param izinMap
	 * @param veriYaz
	 * @param session
	 * @param zamanGuncelle
	 * @return
	 * @throws Exception
	 */
	@Transactional
	public TreeMap<String, VardiyaGun> getVardiyalar(List<Personel> personeller, Date baslamaTarih, Date bitisTarih, HashMap<Long, List<PersonelIzin>> izinMap, boolean veriYaz, Session session, boolean zamanGuncelle) throws Exception {
		if (izinMap == null)
			izinMap = getPersonelIzinMap(getBaseObjectIdList(personeller), baslamaTarih, bitisTarih, session);
		List saveList = new ArrayList();
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		Calendar cal = Calendar.getInstance();

		List<Long> personelIdler = new ArrayList<Long>();
		if (personeller != null) {
			for (Personel personel : personeller) {
				personelIdler.add(personel.getId());
			}
		}
		if (izinMap == null)
			izinMap = !personelIdler.isEmpty() ? getPersonelIzinMap(personelIdler, baslamaTarih, bitisTarih, session) : new HashMap<Long, List<PersonelIzin>>();
		TreeMap<String, VardiyaGun> vardiyaIstenen = new TreeMap<String, VardiyaGun>(), vardiyaMap = new TreeMap<String, VardiyaGun>();
		TreeMap<String, Tatil> tatillerMap = getTatilGunleri(personeller, tariheAyEkleCikar(cal, baslamaTarih, -1), tariheAyEkleCikar(cal, bitisTarih, 1), session);
		List<String> tarihList = new ArrayList<String>();
		for (String key : tatillerMap.keySet()) {
			Tatil tatil = tatillerMap.get(key);
			if (!tatil.getYarimGun()) {
				tarihList.add(key);
			}
		}
		List<String> tatilDonemList = new ArrayList<String>();
		if (!tarihList.isEmpty()) {
			List<Long> idList = new ArrayList<Long>();
			for (Personel personel : personeller) {
				idList.add(personel.getId());
			}
			String fieldName = "p";
			HashMap parametreMap = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("WITH TATILLER AS ( ");
			for (Iterator iterator = tarihList.iterator(); iterator.hasNext();) {
				String tarih = (String) iterator.next();
				sb.append(" SELECT " + tarih.substring(0, 6) + " AS DONEM," + tarih + " AS TARIH " + (iterator.hasNext() ? " UNION ALL " : ""));

			}
			sb.append(") ");
			sb.append(" SELECT  T.TARIH,P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " FROM " + Personel.TABLE_NAME + " P WITH(nolock) ");
			sb.append(" INNER JOIN TATILLER T ON 1=1  ");
			sb.append(" INNER JOIN " + DenklestirmeAy.TABLE_NAME + " D ON D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100+D." + DenklestirmeAy.COLUMN_NAME_AY + " = T.DONEM AND D." + DenklestirmeAy.COLUMN_NAME_DURUM + " = 1 ");
			sb.append(" LEFT JOIN " + PersonelDenklestirme.TABLE_NAME + " PD ON D." + DenklestirmeAy.COLUMN_NAME_ID + " = PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " AND PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " = P." + Personel.COLUMN_NAME_ID);
			sb.append(" INNER JOIN " + CalismaModeliAy.TABLE_NAME + " CA ON (CA.ID=PD." + PersonelDenklestirme.COLUMN_NAME_CALISMA_MODELI_AY + " OR (D." + DenklestirmeAy.COLUMN_NAME_ID + " = CA." + CalismaModeliAy.COLUMN_NAME_DONEM + " AND CA." + CalismaModeliAy.COLUMN_NAME_CALISMA_MODELI + " = P."
					+ Personel.COLUMN_NAME_CALISMA_MODELI + ")) ");
			sb.append(" AND CA." + CalismaModeliAy.COLUMN_NAME_HAREKET_KAYDI_VARDIYA_BUL + " = 1 ");
			sb.append(" WHERE P." + Personel.COLUMN_NAME_ID + " :" + fieldName);
			sb.append(" ORDER BY 2,1");
			parametreMap.put(fieldName, idList);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			// List<Object[]> objectList = pdksEntityController.getObjectBySQLList(sb, parametreMap, null);
			List<Object[]> objectList = getSQLParamList(idList, sb, fieldName, parametreMap, null, session);
			for (Object[] objects : objectList) {
				tatilDonemList.add(objects[1] + "_" + objects[0]);
			}
		}
		cal.setTime(baslamaTarih);
		int haftaGun = cal.get(Calendar.DAY_OF_WEEK);
		cal.add(Calendar.DATE, haftaGun == Calendar.SUNDAY ? -6 : -haftaGun + 2);
		Date startWeekDate1 = (Date) cal.getTime().clone();
		cal.setTime(bitisTarih);
		haftaGun = cal.get(Calendar.DAY_OF_WEEK);
		cal.add(Calendar.DATE, haftaGun == Calendar.SUNDAY ? -6 : -haftaGun + 2);
		Date startWeekDate2 = (Date) cal.getTime().clone();
		HashMap map = new HashMap();
		map.put("vardiyaTipi", Vardiya.TIPI_OFF);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		Vardiya offVardiya = (Vardiya) pdksEntityController.getObjectByInnerObject(map, Vardiya.class);
		Date basTarih = PdksUtil.getDate(tariheGunEkleCikar(cal, (Date) startWeekDate1.clone(), -14));
		Date bitTarih = PdksUtil.getDate(tariheGunEkleCikar(cal, (Date) startWeekDate2.clone(), 6));
		User sistemUser = getSistemAdminUser(session);
		User olusturanUser = authenticatedUser != null ? authenticatedUser : sistemUser;
		Date olusturmaTarihi = new Date();
		HashMap<Long, List<VardiyaGun>> vMap = new HashMap<Long, List<VardiyaGun>>();

		List<VardiyaGun> vardiyaGunList = getPersonelVardiyalar(personeller, basTarih, bitTarih, session);

		for (Iterator iterator = vardiyaGunList.iterator(); iterator.hasNext();) {
			VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
			try {
				if (!izinMap.isEmpty()) {
					Long perId = vardiyaGun.getPersonel().getId();
					List<VardiyaGun> list = vMap.containsKey(perId) ? vMap.get(perId) : new ArrayList<VardiyaGun>();
					if (list.isEmpty())
						vMap.put(perId, list);
					list.add(vardiyaGun);
				}
				vardiyaGun.setHareketHatali(Boolean.FALSE);
				vardiyaGun.setHataliDurum(Boolean.FALSE);
				vardiyaGun.setIzinler(null);
				vardiyaGun.setIzin(null);
				vardiyaGun.setCalismaSuresi(0);
				vardiyaGun.setNormalSure(0);
				vardiyaGun.setResmiTatilSure(0);
				vardiyaGun.setBayramCalismaSuresi(0);
				vardiyaGun.setCalisilmayanAksamSure(0d);
				vardiyaGun.setHaftaCalismaSuresi(0d);
				vardiyaGun.setHareketler(null);
				vardiyaGun.setYemekHareketleri(null);
				vardiyaGun.setGirisHareketleri(null);
				vardiyaGun.setCikisHareketleri(null);
				vardiyaGun.setZamanGuncelle(zamanGuncelle);
				vardiyaGun.setTatil(tatillerMap.get(vardiyaGun.getVardiyaDateStr()));
				String key = vardiyaGun.getVardiyaKeyStr();
				if (!vardiyaMap.containsKey(key))
					vardiyaMap.put(key, vardiyaGun);
				iterator.remove();
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
			}

		}

		if (!izinMap.isEmpty()) {
			for (Long perId : izinMap.keySet()) {
				if (!vMap.containsKey(perId))
					continue;
				List<VardiyaGun> vgList = vMap.get(perId);
				for (VardiyaGun vardiyaGun : vgList) {
					if (tatillerMap.containsKey(vardiyaGun.getVardiyaDateStr()))
						vardiyaGun.setTatil(tatillerMap.get(vardiyaGun.getVardiyaDateStr()));
				}

				vardiyaIzinleriGuncelle(izinMap.get(perId), vgList);
			}
		}

		map.clear();
		String fieldName = "pId";
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT  V.* FROM " + VardiyaHafta.TABLE_NAME + " V WITH(nolock) ");
		sb.append(" WHERE " + VardiyaHafta.COLUMN_NAME_BAS_TARIH + " <= :bitTarih AND " + VardiyaHafta.COLUMN_NAME_BIT_TARIH + " >= :basTarih ");
		sb.append(" AND " + VardiyaHafta.COLUMN_NAME_PERSONEL + " :" + fieldName);
		map.put(fieldName, personelIdler);
		map.put("basTarih", baslamaTarih);
		map.put("bitTarih", bitisTarih);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		TreeMap<String, VardiyaHafta> vardiyaHaftaMap = new TreeMap<String, VardiyaHafta>();
		// List<VardiyaHafta> vardiyaHaftaList = pdksEntityController.getObjectBySQLList(sb, map, VardiyaHafta.class);
		List<VardiyaHafta> vardiyaHaftaList = getSQLParamList(personelIdler, sb, fieldName, map, VardiyaHafta.class, session);

		for (VardiyaHafta vardiyaHafta : vardiyaHaftaList)
			vardiyaHaftaMap.put(vardiyaHafta.getDateKey(), vardiyaHafta);

		VardiyaGun testVardiyaGun1 = null;
		VardiyaHafta testVardiyaHafta = null;
		boolean devam = Boolean.TRUE;
		List<Date> vardiyaGunleri = new ArrayList<Date>();
		while (devam && PdksUtil.tarihKarsilastirNumeric(startWeekDate2, startWeekDate1) != -1) {
			Date endWeekDate1 = tariheGunEkleCikar(cal, startWeekDate1, 6);
			for (int i = 0; i < 7; i++)
				vardiyaGunleri.add(tariheGunEkleCikar(cal, (Date) startWeekDate1.clone(), i));
			for (Personel personel : personeller) {
				Date istenAyrilmaTarihi = personel.getSskCikisTarihi(), iseBaslamaTarihi = personel.getIseBaslamaTarihi();
				boolean personelYaz = personel.getPdks() && personel.getCalismaModeli() != null && !personel.getCalismaModeli().isHareketKaydiVardiyaBulsunmu();
				if (PdksUtil.hasStringValue(personel.getSicilNo()) == false || iseBaslamaTarihi == null || istenAyrilmaTarihi == null)
					continue;

				if (!(PdksUtil.tarihKarsilastirNumeric(endWeekDate1, iseBaslamaTarihi) != -1 && PdksUtil.tarihKarsilastirNumeric(istenAyrilmaTarihi, startWeekDate1) != -1))
					continue;
				VardiyaSablonu vardiyaSablonu = personel.getSablon();
				if (personel.getPdks() == false) {
					testVardiyaHafta = new VardiyaHafta();
					testVardiyaHafta.setBasTarih(startWeekDate1);
					testVardiyaHafta.setBitTarih(endWeekDate1);
					testVardiyaHafta.setId(null);
					testVardiyaHafta.setPersonel(personel);
					String dateKey = testVardiyaHafta.getDateKey();
					testVardiyaHafta.setVardiyaSablonu(vardiyaSablonu);
					if (!vardiyaHaftaMap.containsKey(dateKey)) {
						vardiyaHaftaMap.put(dateKey, testVardiyaHafta);
						testVardiyaHafta.setOlusturanUser(sistemUser);
						testVardiyaHafta.setOlusturmaTarihi(olusturmaTarihi);
						saveList.add(testVardiyaHafta);
					} else
						vardiyaSablonu = testVardiyaHafta.getVardiyaSablonu();
				}
				for (int i = 0; i < 7; i++) {
					Date vardiyaDate = (Date) vardiyaGunleri.get(i).clone();
					if (PdksUtil.tarihKarsilastirNumeric(vardiyaDate, istenAyrilmaTarihi) == 1)
						break;

					if (PdksUtil.tarihKarsilastirNumeric(iseBaslamaTarihi, vardiyaDate) == 1)
						continue;

					testVardiyaGun1 = new VardiyaGun(personel, null, vardiyaDate);
					String vardiyaKey = testVardiyaGun1.getVardiyaKeyStr();
					if (!vardiyaMap.containsKey(vardiyaKey)) {
						testVardiyaGun1.setOlusturanUser(olusturanUser);
						testVardiyaGun1.setOlusturmaTarihi(olusturmaTarihi);
						String vardiyaMetodName = "getVardiya" + (i + 1);
						Vardiya vardiya = (Vardiya) PdksUtil.getMethodObject(vardiyaSablonu, vardiyaMetodName, null);
						testVardiyaGun1.setDurum(!vardiya.isCalisma());
						String key = testVardiyaGun1.getVardiyaDateStr();
						if (tatillerMap.containsKey(key)) {
							if (vardiya.isCalisma()) {
								Tatil pdksTatil = tatillerMap.get(key);
								if (!pdksTatil.isYarimGunMu()) {
									vardiya = offVardiya;
									testVardiyaGun1.setVersion(0);
								}

							}
						}
						testVardiyaGun1.setVardiya((Vardiya) vardiya.clone());
						if (saveList != null && testVardiyaGun1 != null) {
							if (!vardiyaMap.containsKey(vardiyaKey)) {
								if (veriYaz || personelYaz)
									saveList.add(testVardiyaGun1);
							}

							else
								testVardiyaGun1 = vardiyaMap.get(vardiyaKey);

						}
						if (testVardiyaGun1 != null)
							vardiyaMap.put(vardiyaKey, testVardiyaGun1);
					}
				}

				if (saveList != null && !saveList.isEmpty()) {
					for (Iterator iterator = saveList.iterator(); iterator.hasNext();) {
						BaseObject oVardiya = (BaseObject) iterator.next();
						String vardiyaKey = null;
						VardiyaGun vardiyaGun = null;
						if (oVardiya instanceof VardiyaGun) {
							vardiyaGun = (VardiyaGun) oVardiya;
							if (vardiyaGun.getId() == null) {
								vardiyaKey = vardiyaGun.getVardiyaKeyStr();
								if (vardiyaMap.containsKey(vardiyaKey)) {
									VardiyaGun vardiyaGunDb = vardiyaMap.get(vardiyaKey);
									if (vardiyaGunDb.getId() != null)
										continue;
								}

							}
							try {
								if (!vardiyaMap.containsKey(vardiyaKey)) {
									if (veriYaz) {
										pdksEntityController.saveOrUpdate(session, entityManager, vardiyaGun);
										session.flush();
									}
									vardiyaMap.put(vardiyaKey, vardiyaGun);
								}

							} catch (Exception e1) {
								logger.error(vardiyaGun.getVardiyaKeyStr() + "\n" + e1);

							}
						}
					}

				}
				if (saveList != null)
					saveList.clear();

			}
			cal.setTime(startWeekDate1);
			cal.add(Calendar.DATE, 7);
			startWeekDate1 = (Date) cal.getTime().clone();
			vardiyaGunleri.clear();
		}
		tatilDonemList = null;
		if (!vardiyaMap.isEmpty()) {
			vardiyaCalismaModeliGuncelle(new ArrayList<VardiyaGun>(vardiyaMap.values()), session);
			if (vardiyaMap.size() > 1) {
				fazlaMesaiSaatiAyarla(vardiyaMap);
			}
		}
		saveList = null;

		if (!vardiyaMap.isEmpty()) {
			Date tarih1 = PdksUtil.getDate(tariheGunEkleCikar(cal, baslamaTarih, -5));
			Date tarih2 = PdksUtil.getDate(tariheGunEkleCikar(cal, bitisTarih, 5));
			for (Iterator iterator = vardiyaMap.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				try {
					if (!vardiyaMap.containsKey(key) || vardiyaMap.get(key) == null)
						continue;
					VardiyaGun vardiyaGun = vardiyaMap.get(key);
					if (vardiyaGun == null || vardiyaGun.getVardiyaDate().before(tarih1) || vardiyaGun.getVardiyaDate().after(tarih2))
						continue;
					vardiyaIstenen.put(key, vardiyaGun);
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}

			}

		}
		if (vardiyaIstenen != null) {
			HashMap<String, List<VardiyaGun>> map1 = new HashMap<String, List<VardiyaGun>>();
			for (String key1 : vardiyaIstenen.keySet()) {
				VardiyaGun vardiyaGun = vardiyaIstenen.get(key1);

				vardiyaGun.setVardiyaZamani();
				String sicilNo = vardiyaGun.getPersonel().getPdksSicilNo();
				List<VardiyaGun> list = map1.containsKey(sicilNo) ? map1.get(sicilNo) : new ArrayList<VardiyaGun>();
				if (list.isEmpty())
					map1.put(sicilNo, list);
				else {
					VardiyaGun vardiyaGun2 = list.get(list.size() - 1);
					if (vardiyaGun.getOncekiVardiyaGun() == null)
						vardiyaGun.setOncekiVardiyaGun(vardiyaGun2);
					if (vardiyaGun2.getSonrakiVardiya() == null)
						vardiyaGun2.setSonrakiVardiya(vardiyaGun.getIslemVardiya());
				}
				list.add(vardiyaGun);
			}
			map1 = null;
		}
		vardiyaMap = null;
		return vardiyaIstenen;

	}

	/**
	 * @param cal
	 * @param date
	 * @param aySayisi
	 * @return
	 */
	public Date tariheAyEkleCikar(Calendar cal, Date date, int aySayisi) {
		Date tarih = null;
		if (date != null)
			tarih = addTarih(cal, date, Calendar.MONTH, aySayisi);
		return tarih;
	}

	/**
	 * @param cal
	 * @param date
	 * @param gunSayisi
	 * @return
	 */
	public Date tariheGunEkleCikar(Calendar cal, Date date, int gunSayisi) {
		Date tarih = null;
		if (date != null)
			tarih = addTarih(cal, date, Calendar.DATE, gunSayisi);

		return tarih;
	}

	/**
	 * @param vm
	 * @param vg
	 */
	private void setVardiyaGunleri(TreeMap<String, VardiyaGun> vm, VardiyaGun vg) {
		String key = (vg.getPersonel() != null ? vg.getPersonel().getPdksSicilNo() : "") + "_";
		boolean devam = false;
		Calendar cal = Calendar.getInstance();
		try {
			if (vg.getSonrakiVardiya() == null) {
				String keySonrakiGun = key + PdksUtil.convertToDateString(tariheGunEkleCikar(cal, vg.getVardiyaDate(), 1), "yyyyMMdd");
				if (vm.containsKey(keySonrakiGun)) {
					VardiyaGun sonrakiVardiyaGun = vm.get(keySonrakiGun);
					vg.setSonrakiVardiyaGun(sonrakiVardiyaGun);
					if (sonrakiVardiyaGun.getIslemVardiya() != null) {
						devam = true;
						vg.setSonrakiVardiya(sonrakiVardiyaGun.getIslemVardiya());
					}
					sonrakiVardiyaGun.setOncekiVardiyaGun(vg);
					sonrakiVardiyaGun.setOncekiVardiya(vg.getIslemVardiya());
				}
			}
			if (vg.getOncekiVardiya() == null) {
				String keyOncekiGun = key + PdksUtil.convertToDateString(tariheGunEkleCikar(cal, vg.getVardiyaDate(), -1), "yyyyMMdd");
				if (vm.containsKey(keyOncekiGun)) {
					VardiyaGun oncekiVardiyaGun = vm.get(keyOncekiGun);
					vg.setOncekiVardiyaGun(oncekiVardiyaGun);
					if (oncekiVardiyaGun.getIslemVardiya() != null) {
						devam = true;
						vg.setOncekiVardiya(oncekiVardiyaGun.getIslemVardiya());
					}
					oncekiVardiyaGun.setSonrakiVardiyaGun(vg);
					oncekiVardiyaGun.setSonrakiVardiya(vg.getIslemVardiya());

				} else
					logger.debug(keyOncekiGun);
			}
		} catch (Exception exx) {
			exx.printStackTrace();
		}
		try {
			if (vg.getOncekiVardiya() == null) {
				String keyOncekiGun = key + PdksUtil.convertToDateString(tariheGunEkleCikar(cal, vg.getVardiyaDate(), -1), "yyyyMMdd");
				if (vm.containsKey(keyOncekiGun)) {
					VardiyaGun oncekiVardiyaGun = vm.get(keyOncekiGun);
					vg.setOncekiVardiyaGun(oncekiVardiyaGun);
					if (oncekiVardiyaGun.getIslemVardiya() != null) {
						devam = true;
						vg.setOncekiVardiya(oncekiVardiyaGun.getIslemVardiya());
					}
				}
			}
		} catch (Exception exx) {
			exx.printStackTrace();
		}
		if (devam) {
			Vardiya islemVardiya = vg.getIslemVardiya();
			if (islemVardiya != null)
				islemVardiya.setVardiyaZamani(vg);
		}
	}

	/**
	 * @param cal
	 * @param tarih
	 * @param field
	 * @param value
	 * @return
	 */
	public Date addTarih(Calendar cal, Date tarih, int field, int value) {
		if (tarih != null) {
			if (cal == null)
				cal = Calendar.getInstance();
			cal.setTime((Date) tarih.clone());
			try {
				cal.add(field, value);
				tarih = cal.getTime();
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());

			}
		}
		return tarih;
	}

	/**
	 * @param vardiyalarMap
	 */
	public void fazlaMesaiSaatiAyarla(TreeMap<String, VardiyaGun> vardiyaGunMap) {
		Calendar cal = Calendar.getInstance();
		boolean testDurum = PdksUtil.getTestDurum() && PdksUtil.getCanliSunucuDurum() == false;
		testDurum = false;
		if (testDurum)
			logger.info("fazlaMesaiSaatiAyarla 0000 " + getCurrentTimeStampStr());
		List<VardiyaGun> vardiyaGunList = new ArrayList<VardiyaGun>(vardiyaGunMap.values());
		if (vardiyaGunList.size() > 1)
			vardiyaGunList = PdksUtil.sortListByAlanAdi(vardiyaGunList, "vardiyaDate", Boolean.TRUE);
		String haftaTatilDurum = getParameterKey("haftaTatilDurum");
		TreeMap<String, VardiyaGun> vardiyalarMap = new TreeMap<String, VardiyaGun>();
		for (VardiyaGun vardiyaGun : vardiyaGunList) {
			vardiyalarMap.put(vardiyaGun.getVardiyaKeyStr(), vardiyaGun);
		}
		for (VardiyaGun vardiyaGun : vardiyaGunList) {
			vardiyaGun.setAyarlamaBitti(Boolean.FALSE);
			vardiyaGun.setIslendi(Boolean.FALSE);
			vardiyaGun.setOncekiVardiyaGun(null);
			vardiyaGun.setOncekiVardiya(null);
			vardiyaGun.setSonrakiVardiyaGun(null);
			vardiyaGun.setSonrakiVardiya(null);
			vardiyaGun.setIslemVardiya(null);
			if (vardiyaGun.getVardiya() == null)
				continue;
			vardiyaGun.setVardiyaZamani();
			setVardiyaGunleri(vardiyalarMap, vardiyaGun);
		}
		for (VardiyaGun vardiyaGun : vardiyaGunList) {
			if (vardiyaGun.getVardiya() == null)
				continue;
			if (vardiyaGun.getSonrakiVardiya() == null || vardiyaGun.getOncekiVardiya() == null)
				setVardiyaGunleri(vardiyalarMap, vardiyaGun);

		}
		if (testDurum)
			logger.info("fazlaMesaiSaatiAyarla 1000 " + getCurrentTimeStampStr());
		boolean haftaTatilMesaiVar = getParameterKey("haftaTatilDurum").equals("1");
		for (Iterator iterator = vardiyaGunList.iterator(); iterator.hasNext();) {
			VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
			if (vardiyaGun.getVardiya() == null)
				continue;
			Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
			if (islemVardiya != null)
				islemVardiya.setIslemAdet(0);
			Boolean geceHaftaTatilMesaiParcala = null;
			if (haftaTatilDurum.equals("1") && vardiyaGun.getVardiya().isHaftaTatil() && vardiyaGun.getCalismaModeli() != null) {
				geceHaftaTatilMesaiParcala = vardiyaGun.getCalismaModeli().getGeceHaftaTatilMesaiParcala();

			}
			vardiyaGun.setIslendi(vardiyaGun.getSonrakiVardiya() == null && vardiyaGun.getOncekiVardiyaGun() == null);
			try {
				islemVardiya = vardiyaGun.setVardiyaZamani();
				String key = vardiyaGun.getVardiyaDateStr();
				if (key.endsWith("0525"))
					logger.debug(key);
				// Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
				if (islemVardiya.getVardiyaFazlaMesaiBasZaman() == null)
					islemVardiya.setVardiyaFazlaMesaiBasZaman(vardiyaGun.getVardiyaDate());
				if (islemVardiya.getVardiyaFazlaMesaiBitZaman() == null) {
					Date tarih = null;
					if (!islemVardiya.isCalisma()) {
						tarih = tariheGunEkleCikar(cal, vardiyaGun.getVardiyaDate(), 1);
					} else {
						double sure = PdksUtil.getSaatFarki(islemVardiya.getVardiyaBitZaman(), islemVardiya.getVardiyaBasZaman()).doubleValue();
						int bosluk = new Double((34.0d - sure) / 2.0d).intValue();
						tarih = addTarih(cal, islemVardiya.getVardiyaBitZaman(), Calendar.HOUR_OF_DAY, bosluk);
					}
					Date vardiyaFazlaMesaiBitZaman = addTarih(cal, tarih, Calendar.MILLISECOND, -100);
					islemVardiya.setVardiyaFazlaMesaiBitZaman(vardiyaFazlaMesaiBitZaman);
				}

				if (islemVardiya != null && vardiyaGun.getSonrakiVardiya() != null) {
					Vardiya sonrakiVardiya = vardiyaGun.getSonrakiVardiya();
					Date vardiyaFazlaMesaiBitZaman = sonrakiVardiya.getVardiyaFazlaMesaiBasZaman();

					if (sonrakiVardiya.isCalisma() == false) {
						if (islemVardiya.getVardiyaTelorans2BitZaman() != null && islemVardiya.getVardiyaTelorans2BitZaman().after(vardiyaFazlaMesaiBitZaman)) {
							if (islemVardiya.getVardiyaTelorans2BitZaman().before(sonrakiVardiya.getVardiyaTarih())) {
								Double fark = PdksUtil.getDakikaFarki(sonrakiVardiya.getVardiyaTarih(), islemVardiya.getVardiyaTelorans2BitZaman()).doubleValue() / 2.0d;
								if (fark >= 0) {
									int intDakika = fark.intValue();
									vardiyaFazlaMesaiBitZaman = addTarih(cal, islemVardiya.getVardiyaTelorans2BitZaman(), Calendar.MINUTE, intDakika);
									sonrakiVardiya.setVardiyaFazlaMesaiBasZaman(vardiyaFazlaMesaiBitZaman);
								}
							}

						}

					}
					vardiyaFazlaMesaiBitZaman = addTarih(cal, vardiyaFazlaMesaiBitZaman, Calendar.MILLISECOND, -100);

					if (islemVardiya.getVardiyaFazlaMesaiBitZaman() != null && sonrakiVardiya.getVardiyaFazlaMesaiBasZaman() != null && islemVardiya.getVardiyaFazlaMesaiBitZaman().getTime() <= sonrakiVardiya.getVardiyaFazlaMesaiBasZaman().getTime()) {
						islemVardiya.setVardiyaFazlaMesaiBitZaman(vardiyaFazlaMesaiBitZaman);
					} else if (islemVardiya.getVardiyaFazlaMesaiBitZaman() != null && sonrakiVardiya.getVardiyaFazlaMesaiBasZaman() != null && islemVardiya.getVardiyaFazlaMesaiBitZaman().after(sonrakiVardiya.getVardiyaFazlaMesaiBasZaman())) {

						if (islemVardiya.getVardiyaFazlaMesaiBitZaman().after(sonrakiVardiya.getVardiyaTelorans1BasZaman())) {
							islemVardiya.setVardiyaFazlaMesaiBitZaman(addTarih(cal, sonrakiVardiya.getVardiyaBasZaman(), Calendar.MILLISECOND, -100));
							Date sonrakiVarBasZaman = sonrakiVardiya.getVardiyaBasZaman();
							sonrakiVardiya.setVardiyaFazlaMesaiBasZaman(sonrakiVarBasZaman);
							sonrakiVardiya.setVardiyaTelorans1BasZaman(sonrakiVarBasZaman);
						} else {
							islemVardiya.setVardiyaFazlaMesaiBitZaman(vardiyaFazlaMesaiBitZaman);

						}

					}

				}

				if (geceHaftaTatilMesaiParcala != null && !geceHaftaTatilMesaiParcala) {
					VardiyaGun oncekiVardiya = vardiyaGun.getOncekiVardiyaGun();
					if (oncekiVardiya != null) {
						Vardiya vardiya = oncekiVardiya.getIslemVardiya();
						Date vardiyaBitZaman = null;

						if (vardiya != null && vardiya.isCalisma() && vardiya.getBasSaat() >= vardiya.getBitSaat()) {
							vardiyaBitZaman = vardiya.getVardiyaTelorans2BitZaman();
							vardiya.setVardiyaFazlaMesaiBitZaman(vardiyaBitZaman);
							islemVardiya.setVardiyaBasZaman(vardiyaBitZaman);
							islemVardiya.setVardiyaFazlaMesaiBasZaman(vardiyaBitZaman);
							islemVardiya.setVardiyaBitZaman(tariheGunEkleCikar(cal, vardiyaBitZaman, 1));
							islemVardiya.setVardiyaFazlaMesaiBitZaman(islemVardiya.getVardiyaBitZaman());
						}

					}
				}

				if (vardiyaGun.getVardiya().isCalisma() && islemVardiya.getBasSaat() <= islemVardiya.getBitSaat()) {
					VardiyaGun sonrakiVardiyaGun = vardiyaGun.getSonrakiVardiyaGun();
					if (sonrakiVardiyaGun != null) {
						Vardiya vardiya = sonrakiVardiyaGun.getIslemVardiya();
						if (vardiya != null && vardiya.isCalisma() == false) {
							int artiDakika = Math.abs(islemVardiya.isHaftaTatil() ? Vardiya.getIntHaftaTatiliFazlaMesaiBasDakika() : Vardiya.getIntOffFazlaMesaiBasDakika());
							if (haftaTatilMesaiVar == false)
								artiDakika = 0;
							Date vardiyaFazlaMesaiBasZaman = addTarih(cal, vardiya.getVardiyaTarih(), Calendar.MINUTE, -artiDakika);
							islemVardiya.setVardiyaFazlaMesaiBitZaman(addTarih(cal, vardiyaFazlaMesaiBasZaman, Calendar.MILLISECOND, -40));
							sonrakiVardiyaGun.getIslemVardiya().setVardiyaFazlaMesaiBasZaman(vardiyaFazlaMesaiBasZaman);
						}
					}
				}
				if (islemVardiya != null && vardiyaGun.getSonrakiVardiyaGun() != null) {
					VardiyaGun sonrakiVardiyaGun = vardiyaGun.getSonrakiVardiyaGun();
					if (sonrakiVardiyaGun.getIslemVardiya() != null && sonrakiVardiyaGun.getIzin() == null) {
						Vardiya vardiyaSonraki = sonrakiVardiyaGun.getIslemVardiya();
						if (vardiyaSonraki.isCalisma() && vardiyaSonraki.getVardiyaBasZaman().getTime() == sonrakiVardiyaGun.getVardiyaDate().getTime()) {
							if (vardiyaGun.getIzin() != null || islemVardiya.isCalisma() == false) {
								if (vardiyaGun.isAyinGunu())
									logger.debug(vardiyaGun.getVardiyaKeyStr() + " - " + islemVardiya.getVardiyaAciklama());
								int artiDakika = vardiyaSonraki.getGirisErkenToleransDakika();
								vardiyaSonraki.setVardiyaTelorans1BasZaman(addTarih(cal, vardiyaSonraki.getVardiyaBasZaman(), Calendar.MINUTE, -artiDakika));
								int basDakika = Math.abs(islemVardiya.isHaftaTatil() ? Vardiya.getIntHaftaTatiliFazlaMesaiBasDakika() : Vardiya.getIntOffFazlaMesaiBasDakika());
								if (basDakika > artiDakika)
									artiDakika = basDakika;
								else
									artiDakika += 60;
								vardiyaSonraki.setVardiyaFazlaMesaiBasZaman(addTarih(cal, vardiyaSonraki.getVardiyaBasZaman(), Calendar.MINUTE, -artiDakika));

							} else if (islemVardiya.getVardiyaBitZaman().getTime() == sonrakiVardiyaGun.getVardiyaDate().getTime()) {
								if (vardiyaGun.isAyinGunu())
									logger.debug(vardiyaGun.getVardiyaKeyStr() + " : " + islemVardiya.getVardiyaAciklama());
								Date vardiyaTelorans2BitZaman = addTarih(cal, vardiyaSonraki.getVardiyaBasZaman(), Calendar.MILLISECOND, -40);
								islemVardiya.setVardiyaFazlaMesaiBitZaman(addTarih(cal, vardiyaTelorans2BitZaman, Calendar.MILLISECOND, 20));
								vardiyaSonraki.setVardiyaTelorans1BasZaman(vardiyaSonraki.getVardiyaBasZaman());
								vardiyaSonraki.setVardiyaFazlaMesaiBasZaman(vardiyaSonraki.getVardiyaBasZaman());

							}
						}
					}
				}

				if (vardiyaGun.getSonrakiVardiyaGun() == null || islemVardiya.getVardiyaBitZaman().after(islemVardiya.getVardiyaFazlaMesaiBitZaman()) || islemVardiya.getVardiyaTelorans2BitZaman() == null) {
					Date vardiyaTelorans2BitZaman = addTarih(cal, islemVardiya.getVardiyaFazlaMesaiBitZaman(), Calendar.MILLISECOND, -20);
					if (vardiyaTelorans2BitZaman.after(islemVardiya.getVardiyaBitZaman()))
						islemVardiya.setVardiyaTelorans2BitZaman(vardiyaTelorans2BitZaman);

					Date vardiyaBitZaman = addTarih(cal, islemVardiya.getVardiyaFazlaMesaiBitZaman(), Calendar.MILLISECOND, -40);
					if (islemVardiya.getVardiyaBitZaman().after(vardiyaBitZaman))
						islemVardiya.setVardiyaBitZaman(vardiyaBitZaman);

				}
				if (islemVardiya != null)
					islemVardiya.setIslemAdet(-1);
			} catch (Exception ex1) {
				ex1.printStackTrace();
				logger.error(vardiyaGun.getVardiyaKeyStr());

			}
			vardiyaGun.setAyarlamaBitti(Boolean.TRUE);
		}
		if (testDurum)
			logger.info("fazlaMesaiSaatiAyarla 2000 " + getCurrentTimeStampStr());

		vardiyalarMap = null;
	}

	/**
	 * @param value
	 * @param list
	 * @return
	 */
	public String getSelectItemText(Object value, List<SelectItem> list) {
		String aciklama = PdksUtil.getSelectItemLabel(value, list);
		return aciklama;
	}

	/**
	 * @param vardiyaGunList
	 * @param session
	 */
	public void vardiyaCalismaModeliGuncelle(List<VardiyaGun> vardiyaGunList, Session session) {
		String haftaTatilDurum = getParameterKey("haftaTatilDurum");
		if (haftaTatilDurum.equals("1")) {
			List<Long> perIdList = new ArrayList<Long>();
			Date basTarih = null, bitTarih = null;
			for (VardiyaGun vardiyaGun : vardiyaGunList) {
				Long perId = vardiyaGun.getPersonel().getId();
				if (basTarih == null) {
					basTarih = vardiyaGun.getVardiyaDate();
					bitTarih = vardiyaGun.getVardiyaDate();
				} else {
					if (vardiyaGun.getVardiyaDate().before(basTarih))
						basTarih = vardiyaGun.getVardiyaDate();
					if (vardiyaGun.getVardiyaDate().after(bitTarih))
						bitTarih = vardiyaGun.getVardiyaDate();
				}
				if (!perIdList.contains(perId))
					perIdList.add(perId);
			}
			HashMap map = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT DISTINCT P." + VardiyaGun.COLUMN_NAME_ID + " FROM " + PersonelDenklestirme.TABLE_NAME + " P WITH(nolock) ");
			sb.append(" INNER JOIN " + DenklestirmeAy.TABLE_NAME + " D ON P." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
			sb.append(" AND (D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+D." + DenklestirmeAy.COLUMN_NAME_AY + " >= " + PdksUtil.convertToDateString(basTarih, "yyyyMM"));
			sb.append(" AND (D." + DenklestirmeAy.COLUMN_NAME_YIL + "*100)+D." + DenklestirmeAy.COLUMN_NAME_AY + " <= " + PdksUtil.convertToDateString(bitTarih, "yyyyMM"));
			sb.append(" WHERE P." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " :p ");
			map.put("p", perIdList);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelDenklestirme> personelDenkList = getDataByIdList(sb, map, PersonelDenklestirme.TABLE_NAME, PersonelDenklestirme.class);
			if (!personelDenkList.isEmpty()) {
				TreeMap<String, PersonelDenklestirme> denkMap = new TreeMap<String, PersonelDenklestirme>();
				for (PersonelDenklestirme personelDenklestirme : personelDenkList) {
					DenklestirmeAy denklestirmeAy = personelDenklestirme.getDenklestirmeAy();
					denkMap.put(((denklestirmeAy.getYil() * 100) + denklestirmeAy.getAy()) + "_" + personelDenklestirme.getPersonelId(), personelDenklestirme);
				}
				for (VardiyaGun vardiyaGun : vardiyaGunList) {
					String key = PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), "yyyyMM") + "_" + vardiyaGun.getPersonel().getId();
					if (denkMap.containsKey(key)) {
						PersonelDenklestirme denklestirme = denkMap.get(key);
						try {
							if (denklestirme.getCalismaModeliAy() != null)
								vardiyaGun.setCalismaModeli(denklestirme.getCalismaModeli());
						} catch (Exception e) {
							logger.equals(e);
							e.printStackTrace();
						}

					}
				}
				denkMap = null;
			}
			personelDenkList = null;

		}
	}

	/**
	 * @param tarihler
	 * @param personel
	 * @param session
	 * @return
	 */
	public List<VardiyaGun> getPersonelVardiyalar(List<Date> tarihler, Personel personel, Session session) {

		HashMap map = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT V." + VardiyaGun.COLUMN_NAME_ID + " FROM " + VardiyaGun.TABLE_NAME + " V WITH(nolock) ");
		sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + " = V." + VardiyaGun.COLUMN_NAME_PERSONEL);
		sb.append(" AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " >= P." + Personel.getIseGirisTarihiColumn());
		sb.append(" AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " <= P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI);
		sb.append(" WHERE V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " :tarihler AND  V." + VardiyaGun.COLUMN_NAME_PERSONEL + " = : " + personel.getId());
		map.put("tarihler", tarihler);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<VardiyaGun> vardiyaGunList = getDataByIdList(sb, map, VardiyaGun.TABLE_NAME, VardiyaGun.class);
		map = null;

		return vardiyaGunList;
	}

	/**
	 * @param personelIdler
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 * @return
	 */
	public List<VardiyaGun> getIskurVardiyalar(List<Long> personelIdler, Date basTarih, Date bitTarih, Session session) {
		HashMap map = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT V.* FROM " + IsKurVardiyaGun.TABLE_NAME + " V WITH(nolock) ");
		sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + " = V." + IsKurVardiyaGun.COLUMN_NAME_PERSONEL);
		sb.append(" AND V." + IsKurVardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " >= P." + Personel.getIseGirisTarihiColumn());
		sb.append(" AND V." + IsKurVardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " <= P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI);
		sb.append(" WHERE V." + IsKurVardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " >= :basTarih AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " <= :bitTarih AND V." + IsKurVardiyaGun.COLUMN_NAME_PERSONEL + ":pId ");
		sb.append(" ORDER BY V." + IsKurVardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ",V." + IsKurVardiyaGun.COLUMN_NAME_PERSONEL);
		map.put("pId", personelIdler);
		map.put("basTarih", basTarih);
		map.put("bitTarih", bitTarih);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<IsKurVardiyaGun> vardiyaGunList = pdksEntityController.getObjectBySQLList(sb, map, IsKurVardiyaGun.class);

		List<VardiyaGun> list = new ArrayList<VardiyaGun>();
		if (!vardiyaGunList.isEmpty()) {
			for (IsKurVardiyaGun isKurVardiya : vardiyaGunList) {
				VardiyaGun pdksVardiyaGun = isKurVardiya.getVardiyaGun();
				list.add(pdksVardiyaGun);
			}
		}
		return list;
	}

	/**
	 * @param personelIdler
	 * @param basTarih
	 * @param bitTarih
	 * @param hepsi
	 * @param session
	 * @return
	 */
	public List<VardiyaGun> getPersonelIdVardiyalar(List<Long> personelIdler, Date basTarih, Date bitTarih, Boolean hepsi, Session session) {
		Calendar cal = Calendar.getInstance();
		if (hepsi == null && PdksUtil.isSistemDestekVar() && bitTarih.before(tariheAyEkleCikar(cal, new Date(), -1)))
			hepsi = Boolean.TRUE;
		List vardiyaGunList = getAllPersonelIdVardiyalar(personelIdler, basTarih, bitTarih, hepsi, session);
		return vardiyaGunList;
	}

	/**
	 * @param personelIdler
	 * @param basTarih
	 * @param bitTarih
	 * @param hepsi
	 * @param session
	 * @return
	 */
	public List<VardiyaGun> getAllPersonelIdVardiyalar(List<Long> personelIdler, Date basTarih, Date bitTarih, Boolean hepsi, Session session) {
		boolean suaKatSayiOku = false;
		HashMap map = new HashMap();
		List<VardiyaGun> vardiyaGunList = null;
		String fieldName = "pId";
		map.clear();
		HashMap<Long, List<PersonelIzin>> izinMap = getPersonelIzinMap(personelIdler, basTarih, bitTarih, session);
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT V." + VardiyaGun.COLUMN_NAME_ID + " FROM " + VardiyaGun.TABLE_NAME + " V WITH(nolock) ");
		sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + " = V." + VardiyaGun.COLUMN_NAME_PERSONEL);
		if (hepsi == null || hepsi.booleanValue() == false) {
			sb.append(" AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " >= P." + Personel.getIseGirisTarihiColumn());
			sb.append(" AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " <= P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI);
		}
		sb.append(" WHERE V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " >= :basTarih AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " <= :bitTarih AND V." + VardiyaGun.COLUMN_NAME_PERSONEL + ":" + fieldName);
		sb.append(" ORDER BY V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ",V." + VardiyaGun.COLUMN_NAME_PERSONEL);
		map.put(fieldName, personelIdler);
		map.put("basTarih", PdksUtil.getDate(basTarih));
		map.put("bitTarih", PdksUtil.getDate(bitTarih));
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		// List<BigDecimal> idList = pdksEntityController.getObjectBySQLList(sb, map, null);
		List<BigDecimal> idList = getSQLParamList(personelIdler, sb, fieldName, map, null, session);
		if (!idList.isEmpty()) {
			List<Long> list = new ArrayList<Long>();
			for (BigDecimal bigDecimal : idList) {
				list.add(bigDecimal.longValue());
			}
			map.clear();
			fieldName = "id";
			map.put(fieldName, list);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			// vardiyaGunList = pdksEntityController.getObjectByInnerObjectList(map, VardiyaGun.class);

			vardiyaGunList = getParamList(false, list, fieldName, map, VardiyaGun.class, session);

			list = null;

		} else
			vardiyaGunList = new ArrayList<VardiyaGun>();
		idList = null;
		if (!vardiyaGunList.isEmpty()) {
			HashMap<Long, List<VardiyaGun>> vMap = new HashMap<Long, List<VardiyaGun>>();
			for (VardiyaGun vardiyaGun : vardiyaGunList) {
				Long perId = vardiyaGun.getPersonel().getId();
				List<VardiyaGun> list = vMap.containsKey(perId) ? vMap.get(perId) : new ArrayList<VardiyaGun>();
				if (list.isEmpty())
					vMap.put(perId, list);
				list.add(vardiyaGun);
				if (vardiyaGun.getVardiya().getSua() != null && vardiyaGun.getVardiya().getSua()) {
					suaKatSayiOku = true;
					if (izinMap.isEmpty())
						break;
				}

			}
			if (!izinMap.isEmpty()) {
				for (Long key : izinMap.keySet()) {
					if (vMap.containsKey(key)) {
						vardiyaIzinleriGuncelle(izinMap.get(key), vMap.get(key));
					}
				}
			}
			TreeMap<String, Tatil> tatilMap = getTatilGunleri(null, basTarih, bitTarih, session);
			boolean tatilKontrolEt = tatilMap != null && !tatilMap.isEmpty();
			boolean planKatSayiOku = getParameterKey("planKatSayiOku").equals("1");
			boolean haftaTatilFazlaMesaiKatSayiOku = getParameterKey("haftaTatilFazlaMesaiKatSayiOku").equals("1");
			boolean offFazlaMesaiKatSayiOku = getParameterKey("offFazlaMesaiKatSayiOku").equals("1");
			boolean yuvarlamaKatSayiOku = getParameterKey("yuvarlamaKatSayiOku").equals("1");
			HashMap<KatSayiTipi, TreeMap<String, BigDecimal>> allMap = getPlanKatSayiAllMap(personelIdler, basTarih, bitTarih, session);
			TreeMap<String, BigDecimal> sureMap = planKatSayiOku && allMap.containsKey(KatSayiTipi.HAREKET_BEKLEME_SURESI) ? allMap.get(KatSayiTipi.HAREKET_BEKLEME_SURESI) : null;
			TreeMap<String, BigDecimal> sureSuaMap = suaKatSayiOku && allMap.containsKey(KatSayiTipi.SUA_GUNLUK_SAAT_SURESI) ? allMap.get(KatSayiTipi.SUA_GUNLUK_SAAT_SURESI) : null;
			TreeMap<String, BigDecimal> yuvarlamaMap = yuvarlamaKatSayiOku && allMap.containsKey(KatSayiTipi.YUVARLAMA_TIPI) ? allMap.get(KatSayiTipi.YUVARLAMA_TIPI) : null;
			TreeMap<String, BigDecimal> haftaTatilFazlaMesaiMap = haftaTatilFazlaMesaiKatSayiOku && allMap.containsKey(KatSayiTipi.HT_FAZLA_MESAI_TIPI) ? allMap.get(KatSayiTipi.HT_FAZLA_MESAI_TIPI) : null;
			TreeMap<String, BigDecimal> offFazlaMesaiMap = offFazlaMesaiKatSayiOku && allMap.containsKey(KatSayiTipi.OFF_FAZLA_MESAI_TIPI) ? allMap.get(KatSayiTipi.OFF_FAZLA_MESAI_TIPI) : null;
			TreeMap<String, BigDecimal> erkenGirisMap = allMap.containsKey(KatSayiTipi.ERKEN_GIRIS_TIPI) ? allMap.get(KatSayiTipi.ERKEN_GIRIS_TIPI) : null;
			TreeMap<String, BigDecimal> izinHaftaTatilDurumMap = allMap.containsKey(KatSayiTipi.IZIN_HAFTA_TATIL_DURUM) ? allMap.get(KatSayiTipi.IZIN_HAFTA_TATIL_DURUM) : null;
			TreeMap<String, BigDecimal> tatilYemekHesabiSureEkleDurumMap = allMap.containsKey(KatSayiTipi.YEMEK_SURE_EKLE_DURUM) ? allMap.get(KatSayiTipi.YEMEK_SURE_EKLE_DURUM) : null;
			TreeMap<String, BigDecimal> gecCikisMap = allMap.containsKey(KatSayiTipi.GEC_CIKIS_TIPI) ? allMap.get(KatSayiTipi.GEC_CIKIS_TIPI) : null;
			TreeMap<String, BigDecimal> fmtDurumMap = allMap.containsKey(KatSayiTipi.FMT_DURUM) ? allMap.get(KatSayiTipi.FMT_DURUM) : null;
			TreeMap<String, BigDecimal> saatCalisanNormalGunMap = allMap.containsKey(KatSayiTipi.SAAT_CALISAN_NORMAL_GUN) ? allMap.get(KatSayiTipi.SAAT_CALISAN_NORMAL_GUN) : null;
			TreeMap<String, BigDecimal> saatCalisanIzinGunMap = allMap.containsKey(KatSayiTipi.SAAT_CALISAN_IZIN_GUN) ? allMap.get(KatSayiTipi.SAAT_CALISAN_IZIN_GUN) : null;
			TreeMap<String, BigDecimal> saatCalisanHaftaTatilMap = allMap.containsKey(KatSayiTipi.SAAT_CALISAN_HAFTA_TATIL) ? allMap.get(KatSayiTipi.SAAT_CALISAN_HAFTA_TATIL) : null;
			TreeMap<String, BigDecimal> saatCalisanResmiTatilMap = allMap.containsKey(KatSayiTipi.SAAT_CALISAN_RESMI_TATIL) ? allMap.get(KatSayiTipi.SAAT_CALISAN_RESMI_TATIL) : null;
			TreeMap<String, BigDecimal> saatCalisanArifeTatilMap = allMap.containsKey(KatSayiTipi.SAAT_CALISAN_ARIFE_TATIL_SAAT) ? allMap.get(KatSayiTipi.SAAT_CALISAN_ARIFE_TATIL_SAAT) : null;
			TreeMap<String, BigDecimal> saatCalisanArifeNormalMap = allMap.containsKey(KatSayiTipi.SAAT_CALISAN_ARIFE_NORMAL_SAAT) ? allMap.get(KatSayiTipi.SAAT_CALISAN_ARIFE_NORMAL_SAAT) : null;
			boolean erkenGirisKontrolEt = erkenGirisMap != null && !erkenGirisMap.isEmpty();
			boolean gecKontrolEt = gecCikisMap != null && !gecCikisMap.isEmpty();
			boolean offFazlaMesaiKontrolEt = offFazlaMesaiMap != null && !offFazlaMesaiMap.isEmpty();
			boolean haftaTatilFazlaMesaiKontrolEt = haftaTatilFazlaMesaiMap != null && !haftaTatilFazlaMesaiMap.isEmpty();
			boolean fmtDurumKontrolEt = fmtDurumMap != null && !fmtDurumMap.isEmpty();
			boolean izinHaftaTatilDurumKontrolEt = izinHaftaTatilDurumMap != null && !izinHaftaTatilDurumMap.isEmpty();
			boolean saatCalisanNormalGunKontrolEt = saatCalisanNormalGunMap != null && !saatCalisanNormalGunMap.isEmpty();
			boolean saatCalisanIzinGunKontrolEt = saatCalisanIzinGunMap != null && !saatCalisanIzinGunMap.isEmpty();
			boolean saatCalisanHaftaTatilKontrolEt = saatCalisanHaftaTatilMap != null && !saatCalisanHaftaTatilMap.isEmpty();
			boolean saatCalisanResmiTatilKontrolEt = saatCalisanResmiTatilMap != null && !saatCalisanResmiTatilMap.isEmpty();
			boolean saatCalisanArifeNormalKontrolEt = saatCalisanArifeNormalMap != null && !saatCalisanArifeNormalMap.isEmpty();
			boolean saatCalisanArifeTatilKontrolEt = saatCalisanArifeTatilMap != null && !saatCalisanArifeTatilMap.isEmpty();
			boolean tatilYemekHesabiSureEkleDurumKontrolEt = tatilYemekHesabiSureEkleDurumMap != null && !tatilYemekHesabiSureEkleDurumMap.isEmpty();
			yuvarlamaKatSayiOku = yuvarlamaMap != null && !yuvarlamaMap.isEmpty();
			suaKatSayiOku = sureSuaMap != null && !sureSuaMap.isEmpty();
			planKatSayiOku = sureMap != null && !sureMap.isEmpty();
			HashMap<Long, Date> tarih1Map = new HashMap<Long, Date>(), tarih2Map = new HashMap<Long, Date>();
			List<VardiyaGun> bosList = new ArrayList<VardiyaGun>();
			TreeMap<String, VardiyaGun> vardiyaMap = new TreeMap<String, VardiyaGun>();
			for (Iterator iterator = vardiyaGunList.iterator(); iterator.hasNext();) {
				VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
				vardiyaMap.put(vardiyaGun.getVardiyaKeyStr(), vardiyaGun);
			}
			Calendar cal = Calendar.getInstance();
			for (Iterator iterator = vardiyaGunList.iterator(); iterator.hasNext();) {
				VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
				Vardiya vardiya = vardiyaGun.getVardiya();
				if (vardiya != null && vardiya.getId() != null) {
					HashMap<Integer, BigDecimal> katSayiMap = new HashMap<Integer, BigDecimal>();
					String str = vardiyaGun.getVardiyaDateStr();
					Tatil tatil = tatilKontrolEt ? tatilMap.get(str) : null;
					if (tatilYemekHesabiSureEkleDurumKontrolEt && tatil == null) {
						if (str.endsWith("01")) {
							VardiyaGun gun = new VardiyaGun(vardiyaGun.getPdksPersonel(), null, tariheGunEkleCikar(cal, vardiyaGun.getVardiyaDate(), -1));
							String oncekiStr = gun.getVardiyaKeyStr();
							if (vardiyaMap.containsKey(oncekiStr)) {
								VardiyaGun oncekiVardiyaGun = vardiyaMap.get(gun.getVardiyaKeyStr());
								Vardiya oncekiVardiya = oncekiVardiyaGun.getVardiya();
								oncekiStr = oncekiVardiyaGun.getVardiyaDateStr();
								if (oncekiVardiya != null && oncekiVardiya.getId() != null && oncekiVardiya.getBasSaat() > oncekiVardiya.getBitSaat() && tatilMap.containsKey(oncekiStr))
									tatil = tatilMap.get(oncekiStr);
							}
							gun = null;
						} else if (vardiya.getBasSaat() > vardiya.getBitSaat()) {
							VardiyaGun gun = new VardiyaGun(vardiyaGun.getPdksPersonel(), null, tariheGunEkleCikar(cal, vardiyaGun.getVardiyaDate(), 1));
							String sonrakiStr = gun.getVardiyaDateStr();
							if (sonrakiStr.endsWith("01") && vardiyaMap.containsKey(gun.getVardiyaKeyStr()) && tatilMap.containsKey(sonrakiStr))
								tatil = tatilMap.get(sonrakiStr);
							gun = null;
						}
					}
					if (saatCalisanNormalGunKontrolEt && saatCalisanNormalGunMap.containsKey(str))
						katSayiMap.put(KatSayiTipi.SAAT_CALISAN_NORMAL_GUN.value(), saatCalisanNormalGunMap.get(str));
					if (vardiyaGun.isIzinli() && saatCalisanIzinGunKontrolEt && saatCalisanIzinGunMap.containsKey(str))
						katSayiMap.put(KatSayiTipi.SAAT_CALISAN_IZIN_GUN.value(), saatCalisanIzinGunMap.get(str));
					if (saatCalisanHaftaTatilKontrolEt && saatCalisanHaftaTatilMap.containsKey(str))
						katSayiMap.put(KatSayiTipi.SAAT_CALISAN_HAFTA_TATIL.value(), saatCalisanHaftaTatilMap.get(str));
					if (tatilYemekHesabiSureEkleDurumKontrolEt) {
						if (tatilYemekHesabiSureEkleDurumMap.containsKey(str)) {
							katSayiMap.put(KatSayiTipi.YEMEK_SURE_EKLE_DURUM.value(), tatilYemekHesabiSureEkleDurumMap.get(str));
						}
					}
					if (tatil != null) {

						if (!tatil.isYarimGunMu()) {
							if (saatCalisanResmiTatilKontrolEt && saatCalisanResmiTatilMap.containsKey(str))
								katSayiMap.put(KatSayiTipi.SAAT_CALISAN_RESMI_TATIL.value(), saatCalisanResmiTatilMap.get(str));
						} else {
							if (saatCalisanArifeTatilKontrolEt && saatCalisanArifeTatilMap.containsKey(str))
								katSayiMap.put(KatSayiTipi.SAAT_CALISAN_ARIFE_TATIL_SAAT.value(), saatCalisanArifeTatilMap.get(str));
							if (saatCalisanArifeNormalKontrolEt && saatCalisanArifeNormalMap.containsKey(str))
								katSayiMap.put(KatSayiTipi.SAAT_CALISAN_ARIFE_NORMAL_SAAT.value(), saatCalisanArifeNormalMap.get(str));
						}

					}

					if (izinHaftaTatilDurumKontrolEt && izinHaftaTatilDurumMap.containsKey(str)) {
						if (vardiya.isHaftaTatil())
							vardiyaGun.setIzinHaftaTatilDurum(Boolean.FALSE);

					} else if (vardiyaGun.isPazar())
						vardiyaGun.setIzinHaftaTatilDurum(Boolean.TRUE);
					if (fmtDurumKontrolEt && fmtDurumMap.containsKey(str)) {
						BigDecimal deger = fmtDurumMap.get(str);
						if (deger != null)
							katSayiMap.put(KatSayiTipi.FMT_DURUM.value(), deger);
					}

					if (vardiya.isCalisma()) {
						if (erkenGirisKontrolEt && erkenGirisMap.containsKey(str)) {
							BigDecimal deger = erkenGirisMap.get(str);
							if (deger != null)
								katSayiMap.put(KatSayiTipi.ERKEN_GIRIS_TIPI.value(), deger);
						}
						if (gecKontrolEt && gecCikisMap.containsKey(str)) {
							BigDecimal deger = gecCikisMap.get(str);
							if (deger != null)
								katSayiMap.put(KatSayiTipi.GEC_CIKIS_TIPI.value(), deger);
						}
					}

					if (offFazlaMesaiKontrolEt && offFazlaMesaiMap.containsKey(str)) {
						BigDecimal deger = offFazlaMesaiMap.get(str);
						if (deger != null) {
							katSayiMap.put(KatSayiTipi.OFF_FAZLA_MESAI_TIPI.value(), deger);
							vardiyaGun.setOffFazlaMesaiBasDakika(deger.intValue());
						}
					}
					if (haftaTatilFazlaMesaiKontrolEt && haftaTatilFazlaMesaiMap.containsKey(str)) {
						BigDecimal deger = haftaTatilFazlaMesaiMap.get(str);
						if (deger != null) {
							katSayiMap.put(KatSayiTipi.HT_FAZLA_MESAI_TIPI.value(), deger);
							vardiyaGun.setHaftaTatiliFazlaMesaiBasDakika(deger.intValue());
						}
					}
					if (yuvarlamaKatSayiOku && yuvarlamaMap.containsKey(str)) {
						BigDecimal deger = yuvarlamaMap.get(str);
						if (deger != null) {
							katSayiMap.put(KatSayiTipi.YUVARLAMA_TIPI.value(), deger);
							vardiyaGun.setYarimYuvarla(deger.intValue());
						}
					}
					if (suaKatSayiOku && sureSuaMap.containsKey(str)) {
						BigDecimal deger = sureSuaMap.get(str);
						if (deger != null) {
							katSayiMap.put(KatSayiTipi.SUA_GUNLUK_SAAT_SURESI.value(), deger);
							vardiyaGun.setCalismaSuaSaati(deger.doubleValue());
						}
					}

					if (planKatSayiOku && sureMap.containsKey(str)) {
						BigDecimal deger = sureMap.get(str);
						if (deger != null) {
							katSayiMap.put(KatSayiTipi.HAREKET_BEKLEME_SURESI.value(), deger);
							vardiyaGun.setBeklemeSuresi(deger.intValue());
						}
					}
					Date tarih1 = null, tarih2 = null;
					Long key = vardiyaGun.getPersonel().getId();
					if (tarih1Map.containsKey(key))
						tarih1 = tarih1Map.get(key);
					else {
						tarih1 = vardiyaGun.getPersonel().getIseGirisTarihi();
						tarih1Map.put(key, tarih1);
					}
					if (tarih2Map.containsKey(key))
						tarih2 = tarih2Map.get(key);
					else {
						tarih2 = vardiyaGun.getPersonel().getSonCalismaTarihi();
						tarih2Map.put(key, tarih2);
					}
					if (tarih1 == null || tarih2 == null || vardiyaGun.getVardiyaDate().after(tarih2) || vardiyaGun.getVardiyaDate().before(tarih1)) {
						if (hepsi == null || hepsi.booleanValue() == false) {
							iterator.remove();
						}
					}
					if (!katSayiMap.isEmpty()) {
						vardiya.setIslemVardiyaGun(vardiyaGun);
						vardiyaGun.setKatSayiMap(katSayiMap);
					} else
						katSayiMap = null;
				}
			}
			vardiyaMap = null;
			if (!bosList.isEmpty())
				vardiyaGunList.addAll(bosList);
			bosList = null;
			allMap = null;
			sureMap = null;
			sureSuaMap = null;
			yuvarlamaMap = null;
			haftaTatilFazlaMesaiMap = null;
			offFazlaMesaiMap = null;
			erkenGirisMap = null;
			gecCikisMap = null;
			fmtDurumMap = null;
		}

		map = null;
		return vardiyaGunList;

	}

	/**
	 * @param personelIdler
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 * @return
	 */
	public HashMap<KatSayiTipi, TreeMap<String, BigDecimal>> getPlanKatSayiAllMap(List<Long> personelIdler, Date basTarih, Date bitTarih, Session session) {
		HashMap<KatSayiTipi, TreeMap<String, BigDecimal>> allMap = new HashMap<KatSayiTipi, TreeMap<String, BigDecimal>>();
		String fieldName = "pId";
		HashMap map = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT B." + KatSayi.COLUMN_NAME_TIPI + ",V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ",MAX(B." + KatSayi.COLUMN_NAME_DEGER + ") DEGER  FROM " + VardiyaGun.TABLE_NAME + " V WITH(nolock) ");
		sb.append(" INNER JOIN " + KatSayi.TABLE_NAME + " B ON B." + KatSayi.COLUMN_NAME_BAS_TARIH + " <= V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI);
		sb.append(" AND B." + KatSayi.COLUMN_NAME_BIT_TARIH + " >= V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " AND B." + KatSayi.COLUMN_NAME_DURUM + " = 1 ");
		sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + " = V." + VardiyaGun.COLUMN_NAME_PERSONEL);
		sb.append(" AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " >= P." + Personel.getIseGirisTarihiColumn());
		sb.append(" AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " <= P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI);
		sb.append(" WHERE V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " >= :basTarih AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " <= :bitTarih AND V." + VardiyaGun.COLUMN_NAME_PERSONEL + ":" + fieldName);
		sb.append(" GROUP BY B." + KatSayi.COLUMN_NAME_TIPI + ",V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI);
		map.put(fieldName, personelIdler);
		map.put("basTarih", basTarih);
		map.put("bitTarih", bitTarih);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		// List<Object[]> list = pdksEntityController.getObjectBySQLList(sb, map, null);
		List<Object[]> list = getSQLParamList(personelIdler, sb, fieldName, map, null, session);

		for (Object[] objects : list) {
			if (objects[0] == null || objects[1] == null)
				continue;
			try {
				Integer kaySayi = (Integer) objects[0];
				KatSayiTipi key = KatSayiTipi.fromValue(kaySayi);
				if (key != null) {
					TreeMap<String, BigDecimal> degerMap = allMap.containsKey(key) ? allMap.get(key) : new TreeMap<String, BigDecimal>();
					if (degerMap.isEmpty())
						allMap.put(key, degerMap);
					Date date = new Date(((java.sql.Timestamp) objects[1]).getTime());
					BigDecimal deger = new BigDecimal((Double) objects[2]);
					degerMap.put(PdksUtil.convertToDateString(date, "yyyyMMdd"), deger);
				}
			} catch (Exception e) {

			}

		}

		map = null;
		return allMap;
	}

	/**
	 * @param personelIdler
	 * @param basTarih
	 * @param bitTarih
	 * @param tipi
	 * @param session
	 * @return
	 */
	public TreeMap<String, BigDecimal> getPlanKatSayiMap(List<Long> personelIdler, Date basTarih, Date bitTarih, KatSayiTipi tipi, Session session) {
		HashMap map = new HashMap();
		TreeMap<String, BigDecimal> degerMap = new TreeMap<String, BigDecimal>();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ",MAX(B." + KatSayi.COLUMN_NAME_DEGER + ") DEGER  FROM " + VardiyaGun.TABLE_NAME + " V WITH(nolock) ");
		sb.append(" INNER JOIN " + KatSayi.TABLE_NAME + " B ON B." + KatSayi.COLUMN_NAME_BAS_TARIH + " <= V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI);
		sb.append(" AND B." + KatSayi.COLUMN_NAME_BIT_TARIH + " >= V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " AND B." + KatSayi.COLUMN_NAME_DURUM + " = 1 ");
		sb.append(" AND B." + KatSayi.COLUMN_NAME_TIPI + " = :k");
		sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + " = V." + VardiyaGun.COLUMN_NAME_PERSONEL);
		sb.append(" AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " >= P." + Personel.getIseGirisTarihiColumn());
		sb.append(" AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " <= P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI);
		sb.append(" WHERE V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " >= :basTarih AND V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " <= :bitTarih AND V." + VardiyaGun.COLUMN_NAME_PERSONEL + ":pId ");
		sb.append(" GROUP BY V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI);
		map.put("pId", personelIdler);
		map.put("basTarih", basTarih);
		map.put("bitTarih", bitTarih);
		map.put("k", tipi.value());
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			List<Object[]> list = pdksEntityController.getObjectBySQLList(sb, map, null);
			for (Object[] objects : list) {
				if (objects[1] == null)
					continue;
				Date date = new Date(((java.sql.Timestamp) objects[0]).getTime());
				BigDecimal deger = new BigDecimal((Double) objects[1]);
				degerMap.put(PdksUtil.convertToDateString(date, "yyyyMMdd"), deger);
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return degerMap;
	}

	/**
	 * @param personeller
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 * @return
	 */
	private List<VardiyaGun> getPersonelVardiyalar(List<Personel> personeller, Date basTarih, Date bitTarih, Session session) {
		List<Long> personelIdler = new ArrayList<Long>();
		for (Personel personel : personeller)
			personelIdler.add(personel.getId());
		List<VardiyaGun> vardiyaGunList = getPersonelIdVardiyalar(personelIdler, basTarih, bitTarih, Boolean.FALSE, session);
		personelIdler = null;
		return vardiyaGunList;
	}

	/**
	 * @param dataKidemMap
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public HashMap<Integer, Integer> getKidemHesabi(LinkedHashMap<String, Object> dataKidemMap, Session session) throws Exception {
		User sistemYonetici = (User) dataKidemMap.get("sistemYonetici");
		Date bugun = (Date) dataKidemMap.get("bugun");
		Personel personel = (Personel) dataKidemMap.get("personel");
		HashMap<String, IzinTipi> izinTipiMap = (HashMap<String, IzinTipi>) dataKidemMap.get("izinTipiMap");
		HashMap<String, IzinHakedisHakki> hakedisMap = (HashMap<String, IzinHakedisHakki>) dataKidemMap.get("hakedisMap");
		User user = (User) dataKidemMap.get("user");
		HashMap dataMap = (HashMap) dataKidemMap.get("dataMap");
		boolean gecmis = (Boolean) dataKidemMap.get("gecmis");
		boolean yeniBakiyeOlustur = (Boolean) dataKidemMap.get("yeniBakiyeOlustur");

		if (dataMap == null)
			dataMap = new HashMap();
		personel.setIzinBakiyeMap(new HashMap<String, Double>());
		if (hakedisMap == null)
			hakedisMap = getHakedisMap(session);

		String departmanKey = personel.getSirket().getDepartman().getId() + "_";
		Calendar cal = Calendar.getInstance();
		int buYil = cal.get(Calendar.YEAR);
		if (bugun == null)
			bugun = (Date) cal.getTime().clone();
		HashMap<Integer, Integer> kidemMap = getTarihMap(personel != null ? personel.getIzinHakEdisTarihi() : null, bugun);
		if (personel.getSirket().isPdksMi() == false)
			return kidemMap;
		int kidemYili = 0;
		String izinERPUpdate = getParameterKey("izinERPUpdate");
		if (!izinERPUpdate.equals("1") || personel.getSirket().getDepartman().getIzinGirilebilir()) {

			if (personel.getIzinHakEdisTarihi() != null) {
				int yil = kidemMap.get(Calendar.YEAR);
				kidemYili = yil;
				try {
					IzinTipi izinTipi = null;
					String key = departmanKey;
					boolean ekle = Boolean.FALSE;
					boolean suaOlabilir = personel.isSuaOlur();
					boolean senelikKullan = getParameterKey("suaSenelikKullan").equals("1") || !suaOlabilir;
					if (senelikKullan) {
						key = departmanKey + IzinTipi.YILLIK_UCRETLI_IZIN;

						if (dataMap.containsKey(key))
							izinTipi = (IzinTipi) dataMap.get(key);
						else
							ekle = Boolean.TRUE;
						if (personel.getIzinHakEdisTarihi().before(bugun)) {
							HashMap<String, Object> veriMap = new HashMap<String, Object>();
							if (sistemYonetici == null)
								sistemYonetici = getSistemAdminUser(session);
							boolean suaDurum = personel.isSuaOlur();
							// izinTipi = senelikIzinOlustur(personel, suaDurum, buYil, yil, izinTipiMap, hakedisMap, user, session, izinTipi, bugun, yeniBakiyeOlustur);
							veriMap.put("izinSahibi", personel);
							veriMap.put("sistemYonetici", sistemYonetici);
							veriMap.put("suaDurum", suaDurum);
							veriMap.put("yil", buYil);
							veriMap.put("kidemYil", yil);
							veriMap.put("hakedisMap", hakedisMap);
							veriMap.put("user", user);
							veriMap.put("izinTipi", izinTipi);
							veriMap.put("islemTarihi", bugun);
							veriMap.put("yeniBakiyeOlustur", yeniBakiyeOlustur);
							izinTipi = senelikIzinOlustur(veriMap, session);
							if (gecmis && izinTipi != null) {
								cal.setTime(bugun);
								cal.add(Calendar.MONTH, -2);
								if (senelikKullan && buYil != cal.get(Calendar.YEAR)) {
									senelikKullan = buYil > 2020 && buYil >= PdksUtil.getSistemBaslangicYili();
									HashMap fields = new HashMap();
									StringBuffer sb = new StringBuffer();
									sb.append("SELECT SENELIK , SUA FROM  AKTIF_BAKIYE_SENELIK_SUA_IZIN_VIEW P WITH(nolock) ");
									sb.append(" where P.PERSONEL_ID=:p AND YIL=:y ");
									fields.put("p", personel.getId());
									fields.put("y", cal.get(Calendar.YEAR) - 1);
									if (session != null)
										fields.put(PdksEntityController.MAP_KEY_SESSION, session);
									List<Object[]> list = pdksEntityController.getObjectBySQLList(sb, fields, null);
									if (!list.isEmpty()) {
										Object[] izinler = list.get(0);
										Double senelik = (Double) izinler[0], sua = (Double) izinler[1];
										if (sua.intValue() > 0) {
											suaDurum = senelik.intValue() > 0;
											senelikKullan = senelik == 0;
										} else {
											senelikKullan = senelik.intValue() == 0;
											suaDurum = false;
										}

									} else
										senelikKullan = false;
								}
								if (!senelikKullan && buYil != cal.get(Calendar.YEAR) && buYil >= PdksUtil.getSistemBaslangicYili() + 1) {
									buYil--;
									bugun = PdksUtil.convertToJavaDate(buYil + "1231", "yyyyMMdd");
									// kidemMap = getTarihMap(personel != null ?
									// personel.getIzinHakEdisTarihi() : null,
									// bugun);
									if (yil >= 1) {
										Date kidemBaslangicTarihi = PdksUtil.tariheAyEkleCikar(personel.getIzinHakEdisTarihi(), 12);
										if (kidemBaslangicTarihi.before(bugun)) {
											// int kidemYil = yil - 1;
											// senelikIzinOlustur(personel, suaDurum, buYil, yil, izinTipiMap, hakedisMap, user, session, izinTipi, bugun, Boolean.FALSE);
											veriMap.put("suaDurum", suaDurum);
											veriMap.put("yil", buYil);
											veriMap.put("kidemYil", yil);
											veriMap.put("izinTipi", izinTipi);
											veriMap.put("islemTarihi", bugun);
											veriMap.put("yeniBakiyeOlustur", Boolean.FALSE);
											izinTipi = senelikIzinOlustur(veriMap, session);
										}
									}

								}
							}
						}
						if (ekle)
							dataMap.put(key, izinTipi);
					}
					if (suaOlabilir) {
						izinTipi = null;
						ekle = false;
						try {
							if (yil > 0 || (user != null && user.isIK())) {
								key += IzinTipi.SUA_IZNI;
								if (dataMap.containsKey(key))
									izinTipi = (IzinTipi) dataMap.get(key);
								else
									ekle = Boolean.TRUE;
								izinTipi = suaIzinOlustur(personel, izinTipiMap, session, izinTipi, user);

							}
						} catch (Exception ex) {
							ekle = Boolean.FALSE;
						}
						if (ekle)
							dataMap.put(key, izinTipi);
					}

				} catch (Exception e1) {
					logger.error("senelikIzinOlustur " + e1.getMessage());
					e1.printStackTrace();
					throw new Exception(e1.getMessage());
				}

			}
			boolean stajer = false;

			String key = departmanKey + "senelikBakiyeIzinTipi";
			List<IzinTipi> senelikBakiyeIzinTipiList = null;
			if (dataMap.containsKey(key))
				senelikBakiyeIzinTipiList = (List<IzinTipi>) dataMap.get(key);
			else {

				HashMap map = new HashMap();
				List<String> haricKodlar = new ArrayList<String>();
				haricKodlar.add(IzinTipi.SUA_IZNI);
				if (!personel.isHekim()) {
					haricKodlar.add(IzinTipi.YURT_DISI_KONGRE);
					haricKodlar.add(IzinTipi.YURT_ICI_KONGRE);
					haricKodlar.add(IzinTipi.MOLA_IZNI);
				}
				if (stajer)
					map.put("bakiyeIzinTipi.stajerKullanilir=", Boolean.TRUE);
				map.put("bakiyeIzinTipi.durum=", Boolean.TRUE);
				map.put("bakiyeIzinTipi.bakiyeDevirTipi=", IzinTipi.BAKIYE_DEVIR_SENELIK);
				map.put("departman=", personel.getSirket().getDepartman());
				map.put("personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
				map.put("kotaBakiye>=", 0D);
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);

				senelikBakiyeIzinTipiList = pdksEntityController.getObjectByInnerObjectListInLogic(map, IzinTipi.class);
				for (Iterator iterator = senelikBakiyeIzinTipiList.iterator(); iterator.hasNext();) {
					IzinTipi izinTipi = (IzinTipi) iterator.next();
					if (haricKodlar.contains(izinTipi.getIzinTipiTanim().getKodu()))
						iterator.remove();
				}
				dataMap.put(key, senelikBakiyeIzinTipiList);

			}
			for (IzinTipi izinTipi : senelikBakiyeIzinTipiList) {
				if (izinTipi.getBakiyeIzinTipi().getOnaylayanTipi().equals(IzinTipi.ONAYLAYAN_TIPI_YOK) && !personel.isOnaysizIzinKullanir())
					continue;
				try {
					bakiyeIzniOlustur(user, personel, izinTipi, kidemYili, session);
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());
					PdksUtil.addMessageError("getKidemHesabi : " + e.getMessage());
					throw new Exception("bakiyeIzniOlustur : " + e.getMessage());
				}

			}
		}
		// session.getTransaction().commit();

		return kidemMap;
	}

	/**
	 * @param session
	 * @return
	 */
	public HashMap<String, IzinHakedisHakki> getHakedisMap(Session session) {
		HashMap<String, IzinHakedisHakki> hakedisMap = new HashMap<String, IzinHakedisHakki>();
		HashMap fields = new HashMap();
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<IzinHakedisHakki> list = pdksEntityController.getObjectByInnerObjectList(fields, IzinHakedisHakki.class);
		for (IzinHakedisHakki izinHakedisHakki : list)
			hakedisMap.put(izinHakedisHakki.getHakedisKey(), izinHakedisHakki);
		list = null;
		return hakedisMap;
	}

	/**
	 * @param personelIzinOnay
	 * @param kaydeden
	 * @param onayDurum
	 * @param redSebebiTanim
	 * @param redSebebiAciklama
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public HashMap izinOnayla(PersonelIzinOnay personelIzinOnay, User kaydeden, Integer onayDurum, Tanim redSebebiTanim, String redSebebiAciklama, Session session) throws Exception {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		List<User> toList = new ArrayList<User>();
		HashMap parametreMap = new HashMap();
		parametreMap.put("id", personelIzinOnay.getPersonelIzin().getId());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		PersonelIzin personelIzin = (PersonelIzin) pdksEntityController.getObjectByInnerObject(parametreMap, PersonelIzin.class);
		PersonelIzinOnay yeniPersonelIzinOnay = null;
		if (kaydeden == null)
			kaydeden = authenticatedUser;
		User ilkYoneticiUser;
		HashMap returnMap = new HashMap();
		// Personel ikinciYonetici = null;

		// durumu onaylandi ya da red edildi olarak degistirelim
		if (personelIzinOnay.getOnayDurum() != PersonelIzinOnay.ONAY_DURUM_ONAYLANDI) {
			personelIzinOnay.setGuncellemeTarihi(new Date());
			personelIzinOnay.setOnaylayan(kaydeden);
		}

		if (onayDurum.equals(PersonelIzinOnay.ONAY_DURUM_ONAYLANDI)) {

			personelIzinOnay.setOnayDurum(PersonelIzinOnay.ONAY_DURUM_ONAYLANDI);
			if (personelIzinOnay.getOnaylayanTipi().equals(PersonelIzinOnay.ONAYLAYAN_TIPI_IK)) {
				if (kaydeden.isIK()) {
					personelIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
					personelIzin.setGuncelleyenUser(kaydeden);
					personelIzin.setGuncellemeTarihi(new Date());
					pdksEntityController.saveOrUpdate(session, entityManager, personelIzin);
				}

			} else

			if (personelIzinOnay.getOnaylayanTipi().equals(PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI1) &&

			personelIzinOnay.getPersonelIzin().getIzinTipi().getOnaylayanTipi().equals(IzinTipi.ONAYLAYAN_TIPI_YONETICI2)) {

				// 2. yonetici onay kaydet ve mail gonder
				// bu kismi ilk yonetici onayladigi zaman calisan metoda tasidik
				ilkYoneticiUser = null;
				if (personelIzin.getIzinSahibi().getYonetici2() != null) {
					Date bugun = PdksUtil.buGun();
					HashMap map = new HashMap();
					map.put("pdksPersonel.id=", personelIzin.getIzinSahibi().getYonetici2().getId());
					map.put("pdksPersonel.iseBaslamaTarihi<=", bugun);
					map.put("pdksPersonel.sskCikisTarihi>=", bugun);
					map.put("pdksPersonel.durum=", Boolean.TRUE);
					map.put("durum=", Boolean.TRUE);
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					try {
						ilkYoneticiUser = (User) pdksEntityController.getObjectByInnerObjectInLogic(map, User.class);
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());

						throw new Exception(e.getMessage());
					}

				}
				Boolean yoneticiFarkli = Boolean.TRUE;
				if (ilkYoneticiUser != null && kaydeden != null)
					yoneticiFarkli = !ilkYoneticiUser.getId().equals(kaydeden.getId());
				Boolean projeMuduru = authenticatedUser.isProjeMuduru();
				if (!projeMuduru) {
					parametreMap.clear();
					parametreMap.put("pdksPersonel.id", personelIzin.getIzinSahibi().getId());
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					User user = (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);
					if (user != null) {
						setUserRoller(user, session);
						projeMuduru = user.isSuperVisor();
					}
				}

				if (ilkYoneticiUser != null && yoneticiFarkli && !authenticatedUser.isGenelMudur() && !projeMuduru) {

					personelIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_IKINCI_YONETICI_ONAYINDA);

					yeniPersonelIzinOnay = new PersonelIzinOnay();
					yeniPersonelIzinOnay.setOnayDurum(PersonelIzinOnay.ONAY_DURUM_ISLEM_YAPILMADI);
					yeniPersonelIzinOnay.setOlusturanUser(kaydeden);
					if (ilkYoneticiUser != null)
						setUserRoller(ilkYoneticiUser, session);

					if (ilkYoneticiUser != null && !ilkYoneticiUser.isGenelMudur() && (ilkYoneticiUser.isIkinciYoneticiIzinOnaylasin() && personelIzin.getIzinSahibi().isIkinciYoneticiIzinOnaylasin())) {
						toList.add(ilkYoneticiUser);
						try {
							User vekil = getYoneticiBul(personelIzin.getIzinSahibi(), ilkYoneticiUser.getPdksPersonel(), session);
							if (vekil != null && !vekil.getId().equals(ilkYoneticiUser.getId()))
								toList.add(vekil);

						} catch (Exception e) {
							logger.error("Pdks hata in : \n");
							e.printStackTrace();
							logger.error("Pdks hata out : " + e.getMessage());
						}
						yeniPersonelIzinOnay.setOnaylayanTipi(PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI2);
						yeniPersonelIzinOnay.setGuncelleyenUser(ilkYoneticiUser);
						yeniPersonelIzinOnay.setOnaylayan(ilkYoneticiUser);
					} else {
						yeniPersonelIzinOnay.setOnaylayanTipi(PersonelIzinOnay.ONAYLAYAN_TIPI_IK);
						personelIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_IK_ONAYINDA);
						IKKullanicilariBul(toList, personelIzin.getIzinSahibi(), session);
					}
					yeniPersonelIzinOnay.setDurum(Boolean.TRUE);

					yeniPersonelIzinOnay.setOlusturmaTarihi(personelIzinOnay.getGuncellemeTarihi());
					yeniPersonelIzinOnay.setPersonelIzin(personelIzin);

				} else {
					IKKullanicilariBul(toList, personelIzin.getIzinSahibi(), session);
					personelIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_IK_ONAYINDA);

					// IK da onaylayacak kisi belli degildir
					yeniPersonelIzinOnay = new PersonelIzinOnay();
					yeniPersonelIzinOnay.setOnayDurum(PersonelIzinOnay.ONAY_DURUM_ISLEM_YAPILMADI);
					yeniPersonelIzinOnay.setDurum(Boolean.TRUE);
					yeniPersonelIzinOnay.setOlusturanUser(kaydeden);
					yeniPersonelIzinOnay.setOlusturmaTarihi(personelIzinOnay != null ? personelIzinOnay.getGuncellemeTarihi() : new Date());
					yeniPersonelIzinOnay.setOnaylayanTipi(PersonelIzinOnay.ONAYLAYAN_TIPI_IK);
					yeniPersonelIzinOnay.setPersonelIzin(personelIzin);

				}
			} else if ((personelIzinOnay.getOnaylayanTipi().equals(PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI1) && personelIzinOnay.getPersonelIzin().getIzinTipi().getOnaylayanTipi().equals(IzinTipi.ONAYLAYAN_TIPI_YONETICI1))
					|| personelIzinOnay.getOnaylayanTipi().equals(PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI2)) {

				// yonetici1 olan bir izin tipi ve 1. yonetici onaylamissa ya da
				// yonetici2 tipindeki bir izin de yonetici 2 onaylamissa
				// IK islemler yapilir
				// IK rolunde olanlari cekeriz,
				// IK personeli birden fazla olabilir, hepsine mail atilir
				IKKullanicilariBul(toList, personelIzin.getIzinSahibi(), session);
				personelIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_IK_ONAYINDA);

				// IK da onaylayacak kisi belli degildir
				yeniPersonelIzinOnay = new PersonelIzinOnay();
				yeniPersonelIzinOnay.setOnayDurum(PersonelIzinOnay.ONAY_DURUM_ISLEM_YAPILMADI);
				yeniPersonelIzinOnay.setDurum(Boolean.TRUE);
				yeniPersonelIzinOnay.setOlusturanUser(kaydeden);
				yeniPersonelIzinOnay.setOlusturmaTarihi(personelIzinOnay != null ? personelIzinOnay.getGuncellemeTarihi() : new Date());
				yeniPersonelIzinOnay.setOnaylayanTipi(PersonelIzinOnay.ONAYLAYAN_TIPI_IK);
				yeniPersonelIzinOnay.setPersonelIzin(personelIzin);

			}
		} else if (onayDurum.equals(PersonelIzinOnay.ONAY_DURUM_RED)) {

			personelIzinOnay.setOnayDurum(PersonelIzinOnay.ONAY_DURUM_RED);
			personelIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_REDEDILDI);
			personelIzinOnay.setOnaylamamaNedenAciklama(redSebebiAciklama);
			personelIzinOnay.setOnaylamamaNeden(redSebebiTanim);
		}

		try {
			returnMap.put("personelIzinOnay", personelIzinOnay);
			returnMap.put("personelIzin", personelIzin);
			if (personelIzin.getIzinDurumu() != PersonelIzin.IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA && personelIzin.getIzinTipi().getHesapTipi() != null)
				personelIzin.setHesapTipi(personelIzin.getIzinTipi().getHesapTipi());
			if (yeniPersonelIzinOnay != null) {
				List<PersonelIzinOnay> onaylayanlar = izinOnaylarGetir(personelIzin, session);
				for (Iterator iterator = onaylayanlar.iterator(); iterator.hasNext();) {
					PersonelIzinOnay oldPersonelIzinOnay = (PersonelIzinOnay) iterator.next();
					if (oldPersonelIzinOnay.getOnaylayanTipi().equals(yeniPersonelIzinOnay.getOnaylayanTipi())) {
						yeniPersonelIzinOnay.setId(oldPersonelIzinOnay.getId());
						yeniPersonelIzinOnay.setOlusturmaTarihi(personelIzinOnay != null ? personelIzinOnay.getGuncellemeTarihi() : new Date());
						break;
					}

				}
				returnMap.put("yeniPersonelIzinOnay", yeniPersonelIzinOnay);
			}
			if (toList != null && !toList.isEmpty())
				returnMap.put("toList", toList);
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}
		return returnMap;
	}

	/**
	 * @param personelIzin
	 * @param session
	 * @return
	 */
	public List<PersonelIzinOnay> izinOnaylarGetir(PersonelIzin personelIzin, Session session) {
		List<PersonelIzinOnay> onaylayanlar = null;
		if (personelIzin != null && personelIzin.getId() != null) {
			HashMap parametreMap = new HashMap();
			StringBuffer sb = new StringBuffer();
			try {
				sb.append("SELECT I.* FROM " + PersonelIzinOnay.TABLE_NAME + " I  WITH(nolock) ");
				sb.append(" WHERE I." + PersonelIzinOnay.COLUMN_NAME_PERSONEL_IZIN_ID + " = :v");
				parametreMap.put("v", personelIzin.getId());
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				onaylayanlar = pdksEntityController.getObjectBySQLList(sb, parametreMap, PersonelIzinOnay.class);
			} catch (Exception e) {
				parametreMap.clear();
				parametreMap.put("personelIzin.id", personelIzin.getId());
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				onaylayanlar = pdksEntityController.getObjectByInnerObjectList(parametreMap, PersonelIzinOnay.class);
			}
			parametreMap = null;
			sb = null;
		}
		if (onaylayanlar == null)
			onaylayanlar = new ArrayList<PersonelIzinOnay>();

		return onaylayanlar;
	}

	/**
	 * @param password
	 * @return
	 */
	public boolean testDurum(String password) {
		boolean testDurum = false;
		if (parameterMap != null && PdksUtil.hasStringValue(password)) {
			if (parameterMap.containsKey("sifreKontrol"))
				testDurum = parameterMap.get("sifreKontrol").equals("0");

			if (!testDurum && parameterMap.containsKey("adminInput")) {
				Calendar cal = Calendar.getInstance();
				String adminSifre = (cal.get(Calendar.MONTH) + 1) + parameterMap.get("adminInput") + cal.get(Calendar.DATE);
				testDurum = PdksUtil.hasStringValue(adminSifre) && adminSifre.trim().equals(password.trim());
			}
		}
		return testDurum;
	}

	/**
	 * @param toList
	 * @param personel
	 * @param session
	 * @return
	 */
	public List<User> IKKullanicilariBul(List<User> toList, Personel personel, Session session) {
		if (toList == null)
			toList = new ArrayList<User>();
		Departman departman = personel != null ? personel.getSirket().getDepartman() : null;
		List<String> roleList = new ArrayList<String>();
		roleList.add(Role.TIPI_IK);
		if (personel != null && personel.getUstYonetici() != null && personel.getUstYonetici().booleanValue())
			roleList.add(Role.TIPI_IK_DIREKTOR);
		List<User> userList = getRoleKullanicilari(roleList.size() > 1 ? roleList : roleList.get(0), departman, null, session);
		if (!userList.isEmpty()) {
			for (Iterator iterator = toList.iterator(); iterator.hasNext();) {
				User user = (User) iterator.next();
				for (Iterator iterator2 = userList.iterator(); iterator2.hasNext();) {
					User user1 = (User) iterator2.next();
					if (user1.getId().equals(user.getId())) {
						iterator2.remove();
						break;
					}

				}

			}
			if (!userList.isEmpty())
				toList.addAll(userList);
		}

		return userList;

	}

	/**
	 * @param session
	 * @return
	 */
	public List<User> getGenelMudurBul(Session session) {
		List<User> userList = getRoleKullanicilari(Role.TIPI_GENEL_MUDUR, null, null, session);
		return userList;

	}

	/**
	 * @param role
	 * @param departman
	 * @param personel
	 * @param session
	 * @return
	 */
	public List<User> getRoleKullanicilari(Object role, Departman departman, Personel personel, Session session) {
		Date bugun = PdksUtil.getDate(Calendar.getInstance().getTime());
		List<User> userList = null;
		try {
			List<String> roller = new ArrayList<String>();
			if (role != null) {
				if (role instanceof Collection) {
					List<String> list = (List<String>) role;
					roller.addAll(list);
				} else
					roller.add((String) role);
			}
			StringBuffer sb = new StringBuffer();
			HashMap fields = new HashMap();
			sb.append("SELECT DISTINCT U.* FROM " + User.TABLE_NAME + " U WITH(nolock) ");
			if (!roller.isEmpty()) {
				fields.put("role", roller);
				sb.append(" INNER JOIN " + UserRoles.TABLE_NAME + " UR ON UR." + UserRoles.COLUMN_NAME_USER + " = U." + User.COLUMN_NAME_ID);
				sb.append(" INNER JOIN " + Role.TABLE_NAME + " R ON UR." + UserRoles.COLUMN_NAME_ROLE + " = R." + Role.COLUMN_NAME_ID + " and R." + Role.COLUMN_NAME_STATUS + " = 1 and R." + Role.COLUMN_NAME_ROLE_NAME + " :role ");
			}
			sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + " = U." + User.COLUMN_NAME_PERSONEL);
			if (personel != null) {
				sb.append(" AND P." + Personel.COLUMN_NAME_ID + " = :pId");
				fields.put("pId", personel.getId());
			}
			sb.append(" AND P." + Personel.COLUMN_NAME_DURUM + " = 1 AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= :basTarih AND P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " <= :bitTarih ");
			sb.append(" WHERE U." + User.COLUMN_NAME_DURUM + " = 1 ");
			if (departman != null) {
				sb.append(" AND U." + User.COLUMN_NAME_DEPARTMAN + " = :dId");
				fields.put("dId", departman.getId());
			}
			fields.put("basTarih", bugun);
			fields.put("bitTarih", bugun);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			userList = pdksEntityController.getObjectBySQLList(sb, fields, User.class);
			roller = null;
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			HashMap onaylamaMap = new HashMap();
			onaylamaMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			onaylamaMap.put(PdksEntityController.MAP_KEY_SELECT, "user");
			onaylamaMap.put("user.pdksPersonel.iseBaslamaTarihi<=", bugun);
			onaylamaMap.put("user.pdksPersonel.sskCikisTarihi>=", bugun);
			onaylamaMap.put("user.pdksPersonel.durum=", Boolean.TRUE);
			if (personel != null)
				onaylamaMap.put("user.pdksPersonel=", personel);
			onaylamaMap.put("user.durum=", Boolean.TRUE);
			if (role != null) {
				if (role instanceof Collection)
					onaylamaMap.put("role.rolename", role);
				else if (role instanceof String)
					onaylamaMap.put("role.rolename=", role);
			}

			if (departman != null)
				onaylamaMap.put("user.departman=", departman);
			if (session != null)
				onaylamaMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			userList = pdksEntityController.getObjectByInnerObjectListInLogic(onaylamaMap, UserRoles.class);
			onaylamaMap = null;
		}

		if (role instanceof Collection)
			role = null;
		if (userList != null && userList.size() > 1)
			userList = PdksUtil.sortObjectStringAlanList(userList, "getAdSoyad", null);
		return userList;
	}

	/**
	 * @param zipDosyaAdi
	 * @param file
	 * @param sil
	 * @return
	 * @throws Exception
	 */
	public Dosya dosyaFileOlustur(String zipDosyaAdi, File file, Boolean sil) throws Exception {
		Dosya zipFile = null;
		if (file != null && file.exists()) {
			zipFile = new Dosya();
			zipFile.setDosyaAdi(zipDosyaAdi);
			byte[] dosyaIcerik = PdksUtil.getFileByteArray(file);
			zipFile.setDosyaIcerik(dosyaIcerik);
			zipFile.setIcerikTipi("application/zip");
			zipFile.setSize(dosyaIcerik.length);
			if (sil)
				file.delete();
		}

		return zipFile;
	}

	/**
	 * @param dosyaAdi
	 * @param fileList
	 * @return
	 * @throws Exception
	 */
	public File dosyaZipFileOlustur(String zipDosyaAdi, List<Dosya> fileList) throws Exception {
		File file = null;
		if (fileList != null && !fileList.isEmpty()) {
			try {
				String path = "/tmp/";
				File tmp = new File(path);
				if (!tmp.exists())
					tmp.mkdir();
				file = new File(path + zipDosyaAdi);
				FileOutputStream fos = new FileOutputStream(file);
				ZipOutputStream zos = new ZipOutputStream(fos);
				for (Iterator iterator = fileList.iterator(); iterator.hasNext();) {
					Dosya fileUpload = (Dosya) iterator.next();
					ZipEntry zipEntry = new ZipEntry(fileUpload.getDosyaAdi());
					zos.putNextEntry(zipEntry);
					byte[] bytes = fileUpload.getDosyaIcerik();
					int length = bytes.length;
					zos.write(bytes, 0, length);
					zos.closeEntry();
				}
				zos.close();
				fos.close();
				fos.flush();

			} catch (Exception e) {
				logger.error("Pdks Hata in  : ");
				e.printStackTrace();
				logger.error("Pdks Hata out  : " + e.getMessage());
				e.printStackTrace();
			}

		}
		return file;
	}

	/**
	 * @param list
	 * @return
	 */
	public TreeMap<String, Boolean> mantiksalAlanlariDoldur(List<PersonelView> list) {
		TreeMap<String, Boolean> map = new TreeMap<String, Boolean>();
		Boolean fazlaMesaiIzinKullan = Boolean.FALSE, tesisDurum = Boolean.FALSE, fazlaMesaiOde = Boolean.FALSE, sanalPersonel = Boolean.FALSE, icapDurum = Boolean.FALSE;
		Boolean kullaniciPersonel = Boolean.FALSE, gebeMi = Boolean.FALSE, sutIzni = Boolean.FALSE, istenAyrilmaGoster = Boolean.FALSE, personelTipiGoster = Boolean.FALSE;
		Boolean ustYonetici = Boolean.FALSE, partTimeDurum = Boolean.FALSE, egitimDonemi = Boolean.FALSE, suaOlabilir = Boolean.FALSE;
		Boolean emailCCDurum = Boolean.FALSE, emailBCCDurum = Boolean.FALSE, bordroAltAlani = Boolean.FALSE, kimlikNoGoster = Boolean.FALSE, masrafYeriGoster = Boolean.FALSE;
		boolean ikRol = authenticatedUser.isSistemYoneticisi() || authenticatedUser.isAdmin();
		List<Long> depIdList = new ArrayList<Long>();
		Boolean kartNoGoster = null;
		if (list != null) {
			if (authenticatedUser.isAdmin() || authenticatedUser.isIK()) {

				String kartNoAciklama = getParameterKey("kartNoAciklama");

				if (PdksUtil.hasStringValue(kartNoAciklama))
					kartNoGoster = false;
				for (Iterator iter = list.iterator(); iter.hasNext();) {
					PersonelView personelView = (PersonelView) iter.next();
					Personel personel = personelView.getPdksPersonel();
					PersonelKGS personelKGS = personel != null ? personel.getPersonelKGS() : null;
					if (ikRol && personel != null && depIdList.size() < 2) {
						Long depId = personel.getSirket().getDepartman().getId();
						if (!depIdList.contains(depId))
							depIdList.add(depId);
					}
					if (kartNoGoster != null && !kartNoGoster && personelKGS != null) {
						kartNoGoster = PdksUtil.hasStringValue(personelKGS.getKartNo());
					}

					if (!kullaniciPersonel) {
						kullaniciPersonel = personelView.getKullanici() != null;
						if (kullaniciPersonel)
							map.put("kullaniciPersonel", kullaniciPersonel);
					}

					if (personel != null) {
						if (!tesisDurum)
							tesisDurum = personel.getTesis() != null;
						if ((authenticatedUser.isAdmin() || authenticatedUser.isIK())) {
							if (!istenAyrilmaGoster)
								istenAyrilmaGoster = !personel.isCalisiyor();
							if (!bordroAltAlani)
								bordroAltAlani = personel.getBordroAltAlan() != null;
							if (!kimlikNoGoster && personel.getPersonelKGS() != null)
								kimlikNoGoster = PdksUtil.hasStringValue(personel.getPersonelKGS().getKimlikNo());
							if (!masrafYeriGoster)
								masrafYeriGoster = personel.getMasrafYeri() != null;

						}
						if (personel.getIkinciYoneticiIzinOnayla() != null && personel.getIkinciYoneticiIzinOnayla())
							map.put("ikinciYoneticiIzinOnayla", Boolean.TRUE);
						if (personel.getOnaysizIzinKullanilir() != null && personel.getOnaysizIzinKullanilir())
							map.put("onaysizIzinKullanilir", Boolean.TRUE);

						if (!emailCCDurum) {
							emailCCDurum = personel.getEmailCC() != null && personel.getEmailCC().indexOf("@") > 0;
							if (emailCCDurum)
								map.put("emailCCDurum", emailCCDurum);
						}
						if (!emailBCCDurum) {
							emailBCCDurum = personel.getEmailBCC() != null && personel.getEmailBCC().indexOf("@") > 0;
							if (emailBCCDurum)
								map.put("emailBCCDurum", emailBCCDurum);
						}
						if (!suaOlabilir) {
							suaOlabilir = personel.getSuaOlabilir() != null && personel.getSuaOlabilir();
							if (suaOlabilir)
								map.put("suaOlabilir", suaOlabilir);
						}
						if (!egitimDonemi) {
							egitimDonemi = personel.getPartTime() != null && personel.getPartTime();
							if (egitimDonemi)
								map.put("egitimDonemi", egitimDonemi);
						}
						if (!partTimeDurum) {
							partTimeDurum = personel.getPartTime() != null && personel.getPartTime();
							if (partTimeDurum)
								map.put("partTimeDurum", partTimeDurum);
						}
						if (!ustYonetici) {
							ustYonetici = personel.getUstYonetici() != null && personel.getUstYonetici();
							if (ustYonetici)
								map.put("ustYonetici", ustYonetici);
						}
						if (!icapDurum) {
							icapDurum = personel.getIcapciOlabilir() != null && personel.getIcapciOlabilir();
							if (icapDurum)
								map.put("icapDurum", icapDurum);
						}

						if (!sutIzni) {
							sutIzni = personel.getSutIzni() != null && personel.getSutIzni();
							if (sutIzni)
								map.put("sutIzni", sutIzni);
						}
						if (!fazlaMesaiOde) {
							fazlaMesaiOde = personel.getFazlaMesaiOde() != null && personel.getFazlaMesaiOde();
							if (fazlaMesaiOde)
								map.put("fazlaMesaiOde", fazlaMesaiOde);
						}

						if (!fazlaMesaiIzinKullan) {
							fazlaMesaiIzinKullan = personel.getFazlaMesaiIzinKullan() != null && personel.getFazlaMesaiIzinKullan();
							if (fazlaMesaiIzinKullan)
								map.put("fazlaMesaiIzinKullan", fazlaMesaiIzinKullan);
						}
						if (!sanalPersonel) {
							sanalPersonel = personel.getSanalPersonel() != null && personel.getSanalPersonel();
							if (sanalPersonel)
								map.put("sanalPersonel", sanalPersonel);
						}
						if (!gebeMi) {
							gebeMi = personel.getGebeMi() != null && personel.getGebeMi();
							if (gebeMi)
								map.put("gebeMi", gebeMi);
						}
						if (!personelTipiGoster)
							personelTipiGoster = personel.getPersonelTipi() != null;

					}

				}
			}

		}
		if (kartNoGoster != null && kartNoGoster)
			map.put("kartNoGoster", Boolean.TRUE);

		if (personelTipiGoster)
			map.put("personelTipiGoster", Boolean.TRUE);
		if (tesisDurum)
			map.put("tesisDurum", Boolean.TRUE);
		if (istenAyrilmaGoster)
			map.put("istenAyrilmaGoster", Boolean.TRUE);
		if (bordroAltAlani)
			map.put("bordroAltAlani", Boolean.TRUE);
		if (masrafYeriGoster)
			map.put("masrafYeriGoster", Boolean.TRUE);
		if (kimlikNoGoster)
			map.put("kimlikNoGoster", Boolean.TRUE);
		if (depIdList.size() > 1)
			map.put("departmanGoster", Boolean.TRUE);

		return map;
	}

	/**
	 * @param kod
	 * @param session
	 * @return
	 */
	public List<Tanim> getPersonelTanimList(String kod, Session session) {
		HashMap map = new HashMap();
		map.put("tipi", kod);
		map.put("durum", Boolean.TRUE);
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Tanim> list = pdksEntityController.getObjectByInnerObjectList(map, Tanim.class);
		if (!list.isEmpty())
			list = PdksUtil.sortObjectStringAlanList(list, "getErpKodu", null);
		return list;

	}

	/**
	 * @param ldap
	 * @param list
	 * @param tanimMap
	 * @param user
	 * @param personelDinamikMap
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public ByteArrayOutputStream personelExcelDevam(Boolean ldap, List<PersonelView> list, TreeMap<String, Tanim> tanimMap, User user, TreeMap<String, PersonelDinamikAlan> personelDinamikMap, Session session) throws Exception {
		if (tanimMap == null)
			tanimMap = new TreeMap<String, Tanim>();
		if (user == null) {
			if (authenticatedUser != null)
				user = authenticatedUser;
			else {
				user = new User();
				user.setAdmin(Boolean.TRUE);
			}
		}
		String str = getParameterKey("izinERPUpdate"), sanalPersonelAciklama = getParameterKey("sanalPersonelAciklama"), kartNoAciklama = getParameterKey("kartNoAciklama");
		if (!PdksUtil.hasStringValue(sanalPersonelAciklama))
			sanalPersonelAciklama = "Sanal Personel";

		List<PersonelView> personelList = new ArrayList<PersonelView>(list);
		TreeMap<String, Boolean> map = mantiksalAlanlariDoldur(personelList);
		boolean izinERPUpdate = str.equals("1"), fazlaMesaiIzinKullan = map.containsKey("fazlaMesaiIzinKullan"), fazlaMesaiOde = map.containsKey("fazlaMesaiOde");
		boolean sanalPersonel = map.containsKey("sanalPersonel"), icapDurum = map.containsKey("icapDurum"), partTimeDurum = map.containsKey("partTimeDurum");
		boolean sutIzni = map.containsKey("sutIzni"), gebeMi = map.containsKey("gebeMi"), egitimDonemi = map.containsKey("egitimDonemi"), suaOlabilir = map.containsKey("suaOlabilir");
		boolean emailCCDurum = map.containsKey("emailCCDurum"), emailBCCDurum = map.containsKey("emailBCCDurum"), bordroAltAlani = map.containsKey("bordroAltAlani");
		boolean kimlikNoGoster = map.containsKey("kimlikNoGoster"), onaysizIzinKullanilir = map.containsKey("onaysizIzinKullanilir"), tesisDurum = map.containsKey("tesisDurum"), ikinciYoneticiIzinOnayla = map.containsKey("ikinciYoneticiIzinOnayla");
		boolean departmanGoster = map.containsKey("departmanGoster"), istenAyrilmaGoster = map.containsKey("istenAyrilmaGoster"), masrafYeriGoster = map.containsKey("masrafYeriGoster"), kartNoGoster = map.containsKey("kartNoGoster");
		boolean personelTipiGoster = map.containsKey("personelTipiGoster");
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		for (Iterator iter = personelList.iterator(); iter.hasNext();) {
			PersonelView personelView = (PersonelView) iter.next();
			Personel personel = personelView.getPdksPersonel();
			if (personel != null && personel.getSirket().getDepartman().getIzinGirilebilir())
				izinERPUpdate = false;
		}
		Sheet sheet = ExcelUtil.createSheet(wb, "Personel Listesi", false);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddDate = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATE, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenDate = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATE, wb);
		List<Tanim> dinamikAciklamaList = getPersonelTanimList(Tanim.TIPI_PERSONEL_DINAMIK_TANIM, session);
		List<Tanim> dinamikDurumList = getPersonelTanimList(Tanim.TIPI_PERSONEL_DINAMIK_DURUM, session);
		List<Tanim> dinamikSayisalList = getPersonelTanimList(Tanim.TIPI_PERSONEL_DINAMIK_SAYISAL, session);
		if (personelDinamikMap == null) {
			dinamikAciklamaList.clear();
			dinamikDurumList.clear();
			dinamikSayisalList.clear();
		}
		int row = 0;
		int col = 0;
		boolean admin = user.isAdmin() || user.isIKAdmin();
		boolean ik = user.isAdmin() || user.isIK();
		// boolean hastane = getParameterKey("uygulamaTipi").equalsIgnoreCase("H");
		boolean ikAdminDegil = user.isIK() && !user.isIKAdmin();

		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(sirketAciklama());
		if (departmanGoster)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("PDKS Departman");
		if (kimlikNoGoster)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(kimlikNoAciklama());
		if (kartNoGoster)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(kartNoAciklama);

		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(yoneticiAciklama() + " " + personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(yoneticiAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(yonetici2Aciklama() + " " + personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(yonetici2Aciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Vardiya Şablon");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(calismaModeliAciklama());
		if (personelTipiGoster)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(personelTipiAciklama());

		if (!izinERPUpdate)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(kidemBasTarihiAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İşe Giriş Tarihi");
		if (istenAyrilmaGoster)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İşten Ayrılma Tarihi");
		if (!izinERPUpdate)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Doğum Tarihi");
		if (ikAdminDegil)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Bölüm");

		if (ikinciYoneticiIzinOnayla)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İzni " + yonetici2Aciklama() + " Onaylasın");
		if (onaysizIzinKullanilir)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Onaysız İzin Girebilir");
		if (tesisDurum)
			tesisDurum = getListTesisDurum(personelList);
		if (tesisDurum)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(tesisAciklama());
		String ekSaha1 = null, ekSaha2 = null, ekSaha3 = null, ekSaha4 = null;

		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Görevi");
		if (admin) {
			HashMap<String, Boolean> ekSahaMap = getListEkSahaDurumMap(personelList, null);
			if (icapDurum)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İcapçı");
			if (tanimMap.containsKey("ekSaha1") && ekSahaMap.containsKey("ekSaha1")) {
				ekSaha1 = tanimMap.get("ekSaha1").getAciklama();
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ekSaha1);
			}
			if (tanimMap.containsKey("ekSaha2") && ekSahaMap.containsKey("ekSaha2")) {
				ekSaha2 = tanimMap.get("ekSaha2").getAciklama();
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ekSaha2);
			}
			if (tanimMap.containsKey("ekSaha3") && ekSahaMap.containsKey("ekSaha3")) {
				ekSaha3 = tanimMap.get("ekSaha3").getAciklama();
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ekSaha3);
			}
			if (tanimMap.containsKey("ekSaha4") && ekSahaMap.containsKey("ekSaha4")) {
				ekSaha4 = tanimMap.get("ekSaha4").getAciklama();
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ekSaha4);
			}
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Cinsiyet");
			if (bordroAltAlani)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Bordro Alt Birimi");
			if (masrafYeriGoster)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Masraf Yeri");
			if (suaOlabilir)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Şua");

		}

		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Mail Takip");
		if (admin || ik) {
			if (sutIzni)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Süt İzni");
			if (gebeMi)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Gebe Mi");
			if (sanalPersonel)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(sanalPersonelAciklama);
			if (egitimDonemi)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Eğitim Dönemi");
			if (partTimeDurum)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Part Time");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Fazla Mesai Var");
			if (fazlaMesaiOde)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Fazla Mesai Öde");
			if (fazlaMesaiIzinKullan)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Fazla Mesai İzin Kullandır");
		}

		for (Tanim tanim : dinamikDurumList) {
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(tanim.getAciklama());
		}
		for (Tanim tanim : dinamikAciklamaList) {
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(tanim.getAciklama());
		}
		for (Tanim tanim : dinamikSayisalList) {
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(tanim.getAciklama());
		}
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Kullanici Adı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Vardiya Düzelt Yetki");
		if (admin && ldap) {
			if (emailCCDurum)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("E-Posta CC");
			if (emailBCCDurum)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("E-Posta BCC");
		}
		boolean ikRole = authenticatedUser.isAdmin() || authenticatedUser.isIK();
		boolean renk = true;
		for (Iterator iter = personelList.iterator(); iter.hasNext();) {
			PersonelView personelView = (PersonelView) iter.next();
			Personel personel = personelView.getPdksPersonel();
			if (personel == null || PdksUtil.hasStringValue(personel.getSicilNo()) == false)
				continue;
			if (ikRole == false && !personel.isCalisiyor())
				continue;
			Sirket sirket = personel.getSirket();
			if (!sirket.getPdks())
				continue;
			CellStyle style = null, styleCenter = null, cellStyleDate = null;
			if (renk) {
				cellStyleDate = styleOddDate;
				style = styleOdd;
				styleCenter = styleOddCenter;
			} else {
				cellStyleDate = styleEvenDate;
				style = styleEven;
				styleCenter = styleEvenCenter;
			}
			renk = !renk;
			PersonelKGS personelKGS = personel.getPersonelKGS();
			User kullanici = personelView.getKullanici();
			row++;
			col = 0;
			try {
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getSicilNo());
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
				if (personel.getSirket() != null) {
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getSirket().getAd());
					if (departmanGoster)
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getSirket().getDepartman().getDepartmanTanim().getAciklama());

				}
				if (kimlikNoGoster) {
					String kimlikNo = "";
					if (personelKGS != null && PdksUtil.hasStringValue(personelKGS.getKimlikNo()))
						kimlikNo = personelKGS.getKimlikNo();
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(kimlikNo);
				}
				if (kartNoGoster) {
					String kartNo = "";
					if (personelKGS != null && PdksUtil.hasStringValue(personelKGS.getKartNo()))
						kartNo = personelKGS.getKartNo();
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(kartNo);
				}
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getPdksYonetici() != null ? personel.getPdksYonetici().getSicilNo() : "");
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getPdksYonetici() != null ? personel.getPdksYonetici().getAdSoyad() : "");
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getYonetici2() != null ? personel.getYonetici2().getSicilNo() : "");
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getYonetici2() != null ? personel.getYonetici2().getAdSoyad() : "");
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getSablon() != null ? personel.getSablon().getAdi() : "");
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getCalismaModeli() != null ? personel.getCalismaModeli().getAciklama() : "");
				if (personelTipiGoster)
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getPersonelTipi() != null ? personel.getPersonelTipi().getAciklama() : "");
				if (!izinERPUpdate) {
					if (personel.getIzinHakEdisTarihi() != null)
						ExcelUtil.getCell(sheet, row, col++, cellStyleDate).setCellValue(personel.getIzinHakEdisTarihi());
					else
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				}
				if (personel.getIseBaslamaTarihi() != null)
					ExcelUtil.getCell(sheet, row, col++, cellStyleDate).setCellValue(personel.getIseBaslamaTarihi());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				if (istenAyrilmaGoster) {
					if (personel.isCalisiyor())
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
					else
						ExcelUtil.getCell(sheet, row, col++, cellStyleDate).setCellValue(personel.getSonCalismaTarihi());
				}
				if (!izinERPUpdate) {
					if (personel.getDogumTarihi() != null)
						ExcelUtil.getCell(sheet, row, col++, cellStyleDate).setCellValue(personel.getDogumTarihi());
					else
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				}
				if (ikAdminDegil) {
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");

				}

				try {
					if (ikinciYoneticiIzinOnayla)
						ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(user.getYesNo(personel.getIkinciYoneticiIzinOnayla()));
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}
				try {
					if (onaysizIzinKullanilir)
						ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(user.getYesNo(personel.getOnaysizIzinKullanilir()));
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}
				if (tesisDurum)
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getGorevTipi() != null ? personel.getGorevTipi().getAciklama() : "");
				if (admin) {
					try {
						if (icapDurum)
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(user.getYesNo(personel.getIcapciOlabilir()));
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());

					}
					if (ekSaha1 != null)
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha1() != null ? personel.getEkSaha1().getAciklama() : "");
					if (ekSaha2 != null)
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha2() != null ? personel.getEkSaha2().getAciklama() : "");
					if (ekSaha3 != null)
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
					if (ekSaha4 != null)
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha4() != null ? personel.getEkSaha4().getAciklama() : "");
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getCinsiyet() != null ? personel.getCinsiyet().getAciklama() : "");
					try {
						if (bordroAltAlani)
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getBordroAltAlan() != null ? personel.getBordroAltAlan().getKodu() + " - " + personel.getBordroAltAlan().getAciklama() : "");
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());

					}
					try {
						if (masrafYeriGoster)
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getMasrafYeri() != null ? personel.getMasrafYeri().getKoduLong() + " - " + personel.getMasrafYeri().getAciklama() : "");
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());

					}
					try {
						if (suaOlabilir)
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(user.getYesNo(personel.getSuaOlabilir()));
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());

					}

				}
				try {
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(user.getYesNo(personel.getMailTakip()));
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}
				if (admin || ik) {
					if (sutIzni) {
						try {
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(user.getYesNo(personel.getSutIzni()));
						} catch (Exception e) {
							logger.error("Pdks hata in : \n");
							e.printStackTrace();
							logger.error("Pdks hata out : " + e.getMessage());

						}
					}
					if (gebeMi) {
						try {
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(user.getYesNo(personel.isPersonelGebeMi()));
						} catch (Exception e) {
							logger.error("Pdks hata in : \n");
							e.printStackTrace();
							logger.error("Pdks hata out : " + e.getMessage());

						}
					}
					if (sanalPersonel) {
						try {
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(user.getYesNo(personel.getSanalPersonel()));
						} catch (Exception e) {
							logger.error("Pdks hata in : \n");
							e.printStackTrace();
							logger.error("Pdks hata out : " + e.getMessage());

						}
					}
					if (egitimDonemi) {
						try {
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(user.getYesNo(personel.getEgitimDonemi()));
						} catch (Exception e) {
							logger.error("Pdks hata in : \n");
							e.printStackTrace();
							logger.error("Pdks hata out : " + e.getMessage());

						}
					}
					if (partTimeDurum) {
						try {
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(user.getYesNo(personel.getPartTime()));
						} catch (Exception e) {
							logger.error("Pdks hata in : \n");
							e.printStackTrace();
							logger.error("Pdks hata out : " + e.getMessage());

						}
					}

					try {
						ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(user.getYesNo(personel.getPdks()));
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());

					}
					if (fazlaMesaiOde) {
						try {
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(user.getYesNo(personel.getFazlaMesaiOde()));
						} catch (Exception e) {
							logger.error("Pdks hata in : \n");
							e.printStackTrace();
							logger.error("Pdks hata out : " + e.getMessage());

						}
					}

					if (fazlaMesaiIzinKullan) {
						try {
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(user.getYesNo(personel.getFazlaMesaiIzinKullan()));
						} catch (Exception e) {
							logger.error("Pdks hata in : \n");
							e.printStackTrace();
							logger.error("Pdks hata out : " + e.getMessage());

						}
					}

				}
				for (Tanim alan : dinamikDurumList) {
					PersonelDinamikAlan pda = getPersonelDinamikAlan(personelDinamikMap, personel, alan);
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(user.getYesNo(pda.isDurumSecili()));
				}
				for (Tanim alan : dinamikAciklamaList) {
					PersonelDinamikAlan pda = getPersonelDinamikAlan(personelDinamikMap, personel, alan);
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(pda.getTanimDeger() != null ? pda.getTanimDeger().getAciklama() : "");
				}
				for (Tanim alan : dinamikSayisalList) {
					PersonelDinamikAlan pda = getPersonelDinamikAlan(personelDinamikMap, personel, alan);
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(pda.getSayisalDeger() != null ? user.sayiFormatliGoster(pda.getSayisalDeger()) : "");
				}
				try {
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(kullanici != null ? kullanici.getUsername() : "");
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}
				try {
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(kullanici != null ? user.getYesNo(kullanici.getVardiyaDuzeltYetki()) : "");
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}

				if (emailCCDurum) {
					try {
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEmailCC() != null ? personel.getEmailCC() : "");
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());

					}
				}
				if (emailBCCDurum) {
					try {
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEmailBCC() != null ? personel.getEmailBCC() : "");
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());

					}
				}

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
				logger.info(row + " " + personel.getPdksSicilNo());

			}
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
	 * @param personelDinamikMap
	 * @param personel
	 * @param alan
	 * @return
	 */
	private PersonelDinamikAlan getPersonelDinamikAlan(TreeMap<String, PersonelDinamikAlan> personelDinamikMap, Personel personel, Tanim alan) {
		PersonelDinamikAlan dinamikAlan = null;
		if (personelDinamikMap != null && alan != null && personel != null) {
			String key = PersonelDinamikAlan.getKey(personel, alan);
			if (personelDinamikMap.containsKey(key))
				dinamikAlan = personelDinamikMap.get(key);
		}
		if (dinamikAlan == null)
			dinamikAlan = new PersonelDinamikAlan(personel, alan);
		return dinamikAlan;
	}

	/**
	 * @param baslangicYil
	 * @param bakiyeList
	 * @param zipDosya
	 * @return
	 * @throws Exception
	 */
	private List<LinkedHashMap<String, Object>> getPDFLowagieYillikIzinKarti(int baslangicYil, List<TempIzin> bakiyeList, boolean zipDosya) throws Exception {
		List<LinkedHashMap<String, Object>> list = new ArrayList<LinkedHashMap<String, Object>>();
		Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		NumberFormat nf = DecimalFormat.getNumberInstance(locale);
		Date bugun = new Date();
		com.lowagie.text.pdf.BaseFont baseFont = com.lowagie.text.pdf.BaseFont.createFont("ARIAL.TTF", com.lowagie.text.pdf.BaseFont.IDENTITY_H, true);
		com.lowagie.text.Font fontH = new com.lowagie.text.Font(baseFont, 7f, Font.BOLD, null);
		com.lowagie.text.Font fontBaslik = new com.lowagie.text.Font(baseFont, 14f, Font.BOLD, null);
		com.lowagie.text.Font font = new com.lowagie.text.Font(baseFont, 7f, Font.NORMAL, null);
		com.lowagie.text.Image image = getLowagieProjeImage();
		for (TempIzin tempIzin : bakiyeList) {
			if (tempIzin.getToplamBakiyeIzin() == 0.0d)
				continue;
			ByteArrayOutputStream baosPDF = new ByteArrayOutputStream();
			com.lowagie.text.Document doc = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4.rotate(), -60, -60, 30, 30);
			com.lowagie.text.pdf.PdfWriter writer = com.lowagie.text.pdf.PdfWriter.getInstance(doc, baosPDF);

			HeaderLowagie event = new HeaderLowagie();
			writer.setPageEvent(event);
			doc.open();
			Table table = null;
			Date bitisZamani = null;
			int sayfa = 0;
			LinkedHashMap<String, Object> map = null;
			for (Iterator iterator = tempIzin.getYillikIzinler().iterator(); iterator.hasNext();) {
				PersonelIzin bakiyeIzin = (PersonelIzin) iterator.next();
				try {
					++sayfa;
					String bakiyeYil = PdksUtil.convertToDateString(bakiyeIzin.getBaslangicZamani(), "yyyy");
					if (Integer.parseInt(bakiyeYil) < baslangicYil)
						continue;
					if (map == null)
						map = new LinkedHashMap<String, Object>();
					Personel personel = tempIzin.getPersonel();
					doc.add(PDFUtils.getParagraph("YILLIK ÜCRETLİ İZİN KARTI", fontBaslik, Element.ALIGN_CENTER));
					table = new Table(15);
					String pattern = PdksUtil.getDateFormat();
					table.setWidths(new float[] { 6, 12, 8, 8, 8, 8, 8, 8, 12, 8, 10, 7, 12, 12, 22 });
					table.addCell(PDFUtils.getCell("Gruba Giriş Tarihi", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell(PdksUtil.convertToDateString(personel.getGrubaGirisTarihi(), pattern), font, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell(kidemBasTarihiAciklama(), fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell(PdksUtil.convertToDateString(personel.getIzinHakEdisTarihi(), pattern), font, Element.ALIGN_CENTER, 3));
					table.addCell(PDFUtils.getCell("Adı Soyadı", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell(personel.getAdSoyad(), font, Element.ALIGN_CENTER, 4));
					table.addCell(PDFUtils.getCell(sirketAciklama() + " Giriş Tarihi", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell(PdksUtil.convertToDateString(personel.getIseBaslamaTarihi(), pattern), font, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell("Doğum Tarihi", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell(PdksUtil.convertToDateString(personel.getDogumTarihi(), pattern), font, Element.ALIGN_CENTER, 3));
					table.addCell(PDFUtils.getCell("Önceki Soyadı", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell("", font, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell(personelNoAciklama(), fontH, Element.ALIGN_CENTER));
					table.addCell(PDFUtils.getCell(personel.getPdksSicilNo(), font, Element.ALIGN_CENTER));
					table.addCell(PDFUtils.getCellRowspan("Yılı", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCellRowspan("Bir Yıl Önceki İzin Hakkını Kazandığı Tarih", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell("Bir Yıllık Çalışma Süresi İçinde Çalışılmayan Gün Sayısı ve Nedenleri", fontH, Element.ALIGN_CENTER, 6));
					table.addCell(PDFUtils.getCellRowspan("İzne Hak Kazandığı Tarih", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCellRowspan("İşyerindeki Kıdemi (Yıl)", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCellRowspan("Hakettiği İzin (işgünü)", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCellRowspan("İzin Süresi (İşgünü)", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCellRowspan("İzne Başlangıç Tarihi", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCellRowspan("İzinden Dönüş Tarihi", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCellRowspan("Çalışanın İmzası", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell("Hastalık", fontH, Element.ALIGN_CENTER));
					table.addCell(PDFUtils.getCell("Askerlik", fontH, Element.ALIGN_CENTER));
					table.addCell(PDFUtils.getCell("Zorunluluk Hali", fontH, Element.ALIGN_CENTER));
					table.addCell(PDFUtils.getCell("Devamsız lık", fontH, Element.ALIGN_CENTER));
					table.addCell(PDFUtils.getCell("Hizmete Ara Verme", fontH, Element.ALIGN_CENTER));
					table.addCell(PDFUtils.getCell("Diğer Nedenler", fontH, Element.ALIGN_CENTER));
					int adet = bakiyeIzin.getHarcananDigerIzinler() != null && !bakiyeIzin.getHarcananDigerIzinler().isEmpty() ? bakiyeIzin.getHarcananDigerIzinler().size() : 1;
					table.addCell(PDFUtils.getCellRowspan(bakiyeYil, font, Element.ALIGN_CENTER, adet));
					table.addCell(PDFUtils.getCellRowspan(bitisZamani != null ? PdksUtil.convertToDateString(bitisZamani, pattern) : "", font, Element.ALIGN_CENTER, adet));
					for (int i = 0; i < 6; i++)
						table.addCell(PDFUtils.getCellRowspan("", font, Element.ALIGN_CENTER, adet));
					table.addCell(PDFUtils.getCellRowspan(PdksUtil.convertToDateString(bakiyeIzin.getBitisZamani(), pattern), font, Element.ALIGN_CENTER, adet));
					table.addCell(PDFUtils.getCellRowspan(bakiyeIzin.getAciklama(), font, Element.ALIGN_CENTER, adet));
					table.addCell(PDFUtils.getCellRowspan(nf.format(bakiyeIzin.getIzinSuresi()), font, Element.ALIGN_CENTER, adet));
					if (adet > 1) {
						if (bakiyeIzin.getHarcananDigerIzinler() != null && !bakiyeIzin.getHarcananDigerIzinler().isEmpty()) {
							boolean ilkSatir = true;
							List<PersonelIzin> sortList = PdksUtil.sortListByAlanAdi(bakiyeIzin.getHarcananDigerIzinler(), "baslangicZamani", false);
							for (PersonelIzin harcananIzin : sortList) {
								table.addCell(PDFUtils.getCell(nf.format(harcananIzin.getIzinSuresi()), font, Element.ALIGN_CENTER));
								table.addCell(PDFUtils.getCell(PdksUtil.convertToDateString(harcananIzin.getBaslangicZamani(), pattern), font, Element.ALIGN_CENTER));
								table.addCell(PDFUtils.getCell(PdksUtil.convertToDateString(harcananIzin.getBitisZamani(), pattern), font, Element.ALIGN_CENTER));
								if (ilkSatir)
									table.addCell(PDFUtils.getCellRowspan("", font, Element.ALIGN_CENTER, adet));
								ilkSatir = false;
							}
							sortList = null;
						}
					} else {
						for (int i = 0; i < 4; i++)
							table.addCell(PDFUtils.getCell("", font, Element.ALIGN_CENTER));

					}

					doc.add(table);
					if (!iterator.hasNext()) {
						com.lowagie.text.Phrase phrase = new com.lowagie.text.Phrase();
						com.lowagie.text.Chunk chunk = new com.lowagie.text.Chunk("Kalan İzin Bakiyesi :  ", fontH);
						com.lowagie.text.Chunk chunk2 = new com.lowagie.text.Chunk(authenticatedUser.sayiFormatliGoster(tempIzin.getToplamKalanIzin()) + " Gün \n( " + PdksUtil.convertToDateString(bugun, pattern) + " )", font);
						phrase.add(chunk);
						phrase.add(chunk2);
						com.lowagie.text.Paragraph paragraph = new com.lowagie.text.Paragraph(phrase);
						paragraph.setAlignment(Element.ALIGN_CENTER);
						doc.add(paragraph);
					}
					com.lowagie.text.Chunk chunk = new com.lowagie.text.Chunk(String.format("Sayfa : %d ", sayfa), fontH);
					event.setHeader(new com.lowagie.text.Phrase(chunk));

					if (image != null)
						doc.add(image);
					doc.newPage();

				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}
				bitisZamani = bakiyeIzin.getBitisZamani();

			}

			doc.close();
			baosPDF.close();
			if (map != null) {
				if (zipDosya)
					map.put("personel", tempIzin.getPersonel());
				map.put("data", baosPDF.toByteArray());
				list.add(map);
			}
		}
		return list;
	}

	/**
	 * @param baslangicYil
	 * @param bakiyeList
	 * @param zipDosya
	 * @param bolumKlasorEkle
	 * @return
	 * @throws Exception
	 */
	public ByteArrayOutputStream izinBakiyeTopluLowagiePDF(int baslangicYil, List<TempIzin> bakiyeList, boolean zipDosya, boolean bolumKlasorEkle) throws Exception {
		com.lowagie.text.Document document = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4.rotate());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		List<LinkedHashMap<String, Object>> list = null;
		try {
			list = getPDFLowagieYillikIzinKarti(baslangicYil, bakiyeList, zipDosya);
		} catch (Exception e) {
			logger.equals(e);
			e.printStackTrace();

		}
		if (list != null && !list.isEmpty()) {
			if (zipDosya && list.size() > 1) {

				String path = "/tmp/";
				File tmp = new File(path);
				if (!tmp.exists())
					tmp.mkdir();
				ZipOutputStream zos = new ZipOutputStream(outputStream);
				for (LinkedHashMap<String, Object> linkedHashMap : list) {
					byte[] bytes = (byte[]) linkedHashMap.get("data");
					Personel personel = (Personel) linkedHashMap.get("personel");
					String zipDosyaAdi = (bolumKlasorEkle && personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() + "/" : "") + personel.getAdSoyad() + "_" + personel.getPdksSicilNo() + ".pdf";
					ZipEntry zipEntry = new ZipEntry(zipDosyaAdi);
					zos.putNextEntry(zipEntry);

					int length = bytes.length;
					zos.write(bytes, 0, length);
					zos.closeEntry();
				}
				zos.close();

			} else {
				// Create writer for the outputStream
				com.lowagie.text.pdf.PdfCopy copy = new com.lowagie.text.pdf.PdfCopy(document, outputStream);
				// Open document. PdfCopy copy = new PdfCopy(document, outputStream);
				document.open();
				// Contain the pdf data.
				com.lowagie.text.pdf.PdfContentByte pageContentByte = copy.getDirectContent();

				for (LinkedHashMap<String, Object> linkedHashMap : list) {
					byte[] bytes = (byte[]) linkedHashMap.get("data");
					com.lowagie.text.pdf.PdfReader reader = new com.lowagie.text.pdf.PdfReader(bytes);
					for (int i = 1; i <= reader.getNumberOfPages(); i++) {
						document.newPage();
						// import the page from source pdf
						com.lowagie.text.pdf.PdfImportedPage page = copy.getImportedPage(reader, i);
						//
						// com.lowagie.text.pdf.PdfCopy.PageStamp stamp = copy.createPageStamp(page);
						// com.lowagie.text.Chunk chunk = new com.lowagie.text.Chunk(String.format("Sayfa : %d ", i));
						// // Write the text into page (represented by stamp object
						// com.lowagie.text.pdf.ColumnText.showTextAligned(stamp.getUnderContent(), Element.ALIGN_RIGHT, new com.lowagie.text.Phrase(chunk), 450, 10, 0);
						// stamp.alterContents();
						// add the page to the destination pdf

						pageContentByte.addTemplate(page, 0, 0);
						copy.addPage(page);
					}
					reader.close();
				}

			}
		}

		try {
			outputStream.flush();
			document.close();
			outputStream.close();
		} catch (Exception e) {
			logger.equals(e);
			e.printStackTrace();
		}

		return outputStream;
	}

	/**
	 * @param baslangicYil
	 * @param bakiyeList
	 * @param zipDosya
	 * @return
	 * @throws Exception
	 */
	private List<LinkedHashMap<String, Object>> getPDFITextYillikIzinKarti(int baslangicYil, List<TempIzin> bakiyeList, boolean zipDosya) throws Exception {
		BaseFont baseFont = BaseFont.createFont("ARIAL.TTF", BaseFont.IDENTITY_H, true);
		Font fontH = new Font(baseFont, 7f, Font.BOLD, BaseColor.BLACK);
		Font fontBaslik = new Font(baseFont, 14f, Font.BOLD, BaseColor.BLACK);
		Font font = new Font(baseFont, 7f, Font.NORMAL, BaseColor.BLACK);
		Image image = getProjeImage();
		PdfPTable tableImage = null;
		if (image != null) {
			tableImage = new PdfPTable(1);
			com.itextpdf.text.pdf.PdfPCell cellImage = new com.itextpdf.text.pdf.PdfPCell(image);
			cellImage.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
			tableImage.addCell(cellImage);
		}
		List<LinkedHashMap<String, Object>> list = new ArrayList<LinkedHashMap<String, Object>>();
		Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		NumberFormat nf = DecimalFormat.getNumberInstance(locale);
		Date bugun = new Date();

		for (TempIzin tempIzin : bakiyeList) {
			if (tempIzin.getToplamBakiyeIzin() == 0.0d)
				continue;
			LinkedHashMap<String, Object> map = null;
			PdfPTable table = null;
			ByteArrayOutputStream baosPDF = new ByteArrayOutputStream();
			Document doc = new Document(PageSize.A4.rotate(), -60, -60, 30, 30);
			PdfWriter writer = PdfWriter.getInstance(doc, baosPDF);
			HeaderIText event = new HeaderIText();
			writer.setPageEvent(event);
			doc.open();
			Date bitisZamani = null;

			int sayfa = 0;
			for (Iterator iterator = tempIzin.getYillikIzinler().iterator(); iterator.hasNext();) {
				PersonelIzin bakiyeIzin = (PersonelIzin) iterator.next();
				try {
					++sayfa;
					String bakiyeYil = PdksUtil.convertToDateString(bakiyeIzin.getBaslangicZamani(), "yyyy");
					if (baslangicYil > 0 && Integer.parseInt(bakiyeYil) < baslangicYil)
						continue;
					if (map == null)
						map = new LinkedHashMap<String, Object>();
					Personel personel = tempIzin.getPersonel();
					if (tableImage != null)
						doc.add(tableImage);

					doc.add(PDFITextUtils.getParagraph("YILLIK ÜCRETLİ İZİN KARTI", fontBaslik, Element.ALIGN_CENTER));
					table = new PdfPTable(15);
					table.setSpacingBefore(20);
					String pattern = PdksUtil.getDateFormat();
					table.setWidths(new float[] { 6, 12, 8, 8, 8, 8, 8, 8, 12, 8, 10, 7, 12, 12, 22 });
					table.addCell(PDFITextUtils.getPdfCell("Gruba Giriş Tarihi", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFITextUtils.getPdfCell(PdksUtil.convertToDateString(personel.getGrubaGirisTarihi(), pattern), font, Element.ALIGN_CENTER, 2));
					table.addCell(PDFITextUtils.getPdfCell(kidemBasTarihiAciklama(), fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFITextUtils.getPdfCell(PdksUtil.convertToDateString(personel.getIzinHakEdisTarihi(), pattern), font, Element.ALIGN_CENTER, 3));
					table.addCell(PDFITextUtils.getPdfCell("Adı Soyadı", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFITextUtils.getPdfCell(personel.getAdSoyad(), font, Element.ALIGN_CENTER, 4));
					table.addCell(PDFITextUtils.getPdfCell(sirketAciklama() + " Giriş Tarihi", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFITextUtils.getPdfCell(PdksUtil.convertToDateString(personel.getIseBaslamaTarihi(), pattern), font, Element.ALIGN_CENTER, 2));
					table.addCell(PDFITextUtils.getPdfCell("Doğum Tarihi", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFITextUtils.getPdfCell(PdksUtil.convertToDateString(personel.getDogumTarihi(), pattern), font, Element.ALIGN_CENTER, 3));
					table.addCell(PDFITextUtils.getPdfCell("Önceki Soyadı", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFITextUtils.getPdfCell("", font, Element.ALIGN_CENTER, 2));
					table.addCell(PDFITextUtils.getPdfCell(personelNoAciklama(), fontH, Element.ALIGN_CENTER));
					table.addCell(PDFITextUtils.getPdfCell(personel.getPdksSicilNo(), font, Element.ALIGN_CENTER));
					table.addCell(PDFITextUtils.getPdfCellRowspan("Yılı", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFITextUtils.getPdfCellRowspan("Bir Yıl Önceki İzin Hakkını Kazandığı Tarih", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFITextUtils.getPdfCell("Bir Yıllık Çalışma Süresi İçinde Çalışılmayan Gün Sayısı ve Nedenleri", fontH, Element.ALIGN_CENTER, 6));
					table.addCell(PDFITextUtils.getPdfCellRowspan("İzne Hak Kazandığı Tarih", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFITextUtils.getPdfCellRowspan("İşyerindeki Kıdemi (Yıl)", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFITextUtils.getPdfCellRowspan("Hakettiği İzin (işgünü)", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFITextUtils.getPdfCellRowspan("İzin Süresi (İşgünü)", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFITextUtils.getPdfCellRowspan("İzne Başlangıç Tarihi", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFITextUtils.getPdfCellRowspan("İzinden Dönüş Tarihi", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFITextUtils.getPdfCellRowspan("Çalışanın İmzası", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFITextUtils.getPdfCell("Hastalık", fontH, Element.ALIGN_CENTER));
					table.addCell(PDFITextUtils.getPdfCell("Askerlik", fontH, Element.ALIGN_CENTER));
					table.addCell(PDFITextUtils.getPdfCell("Zorunluluk Hali", fontH, Element.ALIGN_CENTER));
					table.addCell(PDFITextUtils.getPdfCell("Devamsız lık", fontH, Element.ALIGN_CENTER));
					table.addCell(PDFITextUtils.getPdfCell("Hizmete Ara Verme", fontH, Element.ALIGN_CENTER));
					table.addCell(PDFITextUtils.getPdfCell("Diğer Nedenler", fontH, Element.ALIGN_CENTER));
					int adet = bakiyeIzin.getHarcananDigerIzinler() != null && !bakiyeIzin.getHarcananDigerIzinler().isEmpty() ? bakiyeIzin.getHarcananDigerIzinler().size() : 0;
					table.addCell(PDFITextUtils.getPdfCellRowspan(bakiyeIzin.getDevirIzin() != null && bakiyeIzin.getDevirIzin() == false ? bakiyeYil : "", font, Element.ALIGN_CENTER, adet));
					table.addCell(PDFITextUtils.getPdfCellRowspan(bitisZamani != null ? PdksUtil.convertToDateString(bitisZamani, pattern) : "", font, Element.ALIGN_CENTER, adet));
					for (int i = 0; i < 6; i++)
						table.addCell(PDFITextUtils.getPdfCellRowspan("", font, Element.ALIGN_CENTER, adet));
					table.addCell(PDFITextUtils.getPdfCellRowspan(bakiyeIzin.getDevirIzin() == false ? PdksUtil.convertToDateString(bakiyeIzin.getBitisZamani(), pattern) : "", font, Element.ALIGN_CENTER, adet));
					table.addCell(PDFITextUtils.getPdfCellRowspan(bakiyeIzin.getAciklama(), font, Element.ALIGN_CENTER, adet));
					table.addCell(PDFITextUtils.getPdfCellRowspan(bakiyeIzin.getIzinSuresi() != 0.0d ? nf.format(bakiyeIzin.getIzinSuresi()) : "", font, Element.ALIGN_CENTER, adet));
					if (adet > 0) {

						boolean ilkSatir = true;
						List<PersonelIzin> sortList = PdksUtil.sortListByAlanAdi(bakiyeIzin.getHarcananDigerIzinler(), "baslangicZamani", false);
						for (PersonelIzin harcananIzin : sortList) {
							table.addCell(PDFITextUtils.getPdfCell(nf.format(harcananIzin.getIzinSuresi()), font, Element.ALIGN_CENTER));
							table.addCell(PDFITextUtils.getPdfCell(PdksUtil.convertToDateString(harcananIzin.getBaslangicZamani(), pattern), font, Element.ALIGN_CENTER));
							table.addCell(PDFITextUtils.getPdfCell(PdksUtil.convertToDateString(harcananIzin.getBitisZamani(), pattern), font, Element.ALIGN_CENTER));
							if (ilkSatir)
								table.addCell(PDFITextUtils.getPdfCellRowspan("", font, Element.ALIGN_CENTER, adet));
							ilkSatir = false;
						}
						sortList = null;

					} else {
						for (int i = 0; i < 4; i++)
							table.addCell(PDFITextUtils.getPdfCell("", font, Element.ALIGN_CENTER));

					}

					doc.add(table);
					if (!iterator.hasNext()) {
						Phrase phrase = new Phrase();
						Chunk chunk = new Chunk("Kalan İzin Bakiyesi :  ", fontH);
						Chunk chunk2 = new Chunk(authenticatedUser.sayiFormatliGoster(tempIzin.getToplamKalanIzin()) + " Gün \n( " + PdksUtil.convertToDateString(bugun, pattern) + " )", font);
						phrase.add(chunk);
						phrase.add(chunk2);
						Paragraph paragraph = new Paragraph(phrase);
						paragraph.setAlignment(Element.ALIGN_CENTER);
						doc.add(paragraph);
					}
					Chunk chunk = new Chunk(String.format("Sayfa : %d ", sayfa), fontH);
					event.setHeader(new Phrase(chunk));
					doc.newPage();

				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}
				if (bakiyeIzin.getDevirIzin() == false)
					bitisZamani = bakiyeIzin.getBitisZamani();

			}
			doc.close();
			baosPDF.close();
			if (map != null) {
				if (zipDosya)
					map.put("personel", tempIzin.getPersonel());
				map.put("data", baosPDF.toByteArray());
				list.add(map);
			}

		}
		return list;
	}

	/**
	 * @return
	 */
	public HashMap<String, Object> getProjeHeaderImageMap() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		String projeHeaderImageName = getParameterKey("projeHeaderImageName");
		File projeHeader = new File("/opt/pdks/" + projeHeaderImageName);
		if (projeHeader.exists()) {
			try {
				byte[] projeHeaderImage = PdksUtil.getFileByteArray(projeHeader);
				map.put("projeHeaderImage", projeHeaderImage);
				float projeHeaderImageHeight = 450f, projeHeaderImageWidth = 450f;
				if (parameterMap.containsKey("projeHeaderSize")) {
					String deger = parameterMap.get("projeHeaderSize");
					LinkedHashMap<String, String> map1 = PdksUtil.parametreAyikla(deger);
					if (map1.containsKey("width"))
						projeHeaderImageWidth = new Double(map1.get("width")).floatValue();
					if (map1.containsKey("height"))
						projeHeaderImageHeight = new Double(map1.get("height")).floatValue();
				}
				map.put("projeHeaderImageHeight", projeHeaderImageHeight);
				map.put("projeHeaderImageWidth", projeHeaderImageWidth);
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		return map;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public Image getProjeImage() throws Exception {
		HashMap<String, Object> projeImageMap = getProjeHeaderImageMap();
		Image image = null;
		if (projeImageMap.containsKey("projeHeaderImage")) {
			byte[] projeHeaderImage = (byte[]) projeImageMap.get("projeHeaderImage");
			image = Image.getInstance(projeHeaderImage);
			// ImageData data = ImageDataFactory.create(projeHeaderImage);
			// Image img = new Image(data);
			if (image != null) {
				float projeHeaderImageHeight = (Float) projeImageMap.get("projeHeaderImageHeight");
				float projeHeaderImageWidth = (Float) projeImageMap.get("projeHeaderImageWidth");
				image.scaleToFit(projeHeaderImageHeight, projeHeaderImageWidth);
			}
		}

		return image;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public com.lowagie.text.Image getLowagieProjeImage() throws Exception {
		HashMap<String, Object> projeImageMap = getProjeHeaderImageMap();
		com.lowagie.text.Image image = null;
		if (projeImageMap.containsKey("projeHeaderImage")) {
			byte[] projeHeaderImage = (byte[]) projeImageMap.get("projeHeaderImage");
			image = com.lowagie.text.Image.getInstance(projeHeaderImage);
			if (image != null) {
				float projeHeaderImageHeight = (Float) projeImageMap.get("projeHeaderImageHeight");
				float projeHeaderImageWidth = (Float) projeImageMap.get("projeHeaderImageWidth");
				image.scaleToFit(projeHeaderImageHeight, projeHeaderImageWidth);
			}

		}
		return image;
	}

	/**
	 * @param baslangicYil
	 * @param bakiyeList
	 * @param zipDosya
	 * @param bolumKlasorEkle
	 * @return
	 * @throws Exception
	 */
	public ByteArrayOutputStream izinBakiyeTopluITextPDF(int baslangicYil, List<TempIzin> bakiyeList, boolean zipDosya, boolean bolumKlasorEkle) throws Exception {
		Document document = new Document(PageSize.A4.rotate());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		List<LinkedHashMap<String, Object>> list = null;
		try {
			list = getPDFITextYillikIzinKarti(baslangicYil, bakiyeList, zipDosya);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		if (list != null && !list.isEmpty()) {
			if (zipDosya && list.size() > 1) {
				String path = "/tmp/";
				File tmp = new File(path);
				if (!tmp.exists())
					tmp.mkdir();
				ZipOutputStream zos = new ZipOutputStream(outputStream);
				for (LinkedHashMap<String, Object> linkedHashMap : list) {
					byte[] bytes = (byte[]) linkedHashMap.get("data");
					Personel personel = (Personel) linkedHashMap.get("personel");
					String zipDosyaAdi = (bolumKlasorEkle && personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() + "/" : "") + personel.getAdSoyad() + "_" + personel.getPdksSicilNo() + ".pdf";
					ZipEntry zipEntry = new ZipEntry(zipDosyaAdi);
					zos.putNextEntry(zipEntry);

					int length = bytes.length;
					zos.write(bytes, 0, length);
					zos.closeEntry();
				}
				zos.close();

			} else {// Create writer for the outputStream
				PdfCopy copy = new PdfCopy(document, outputStream);
				// Open document. PdfCopy copy = new PdfCopy(document, outputStream);
				document.open();
				PdfContentByte pageContentByte = copy.getDirectContent();
				for (LinkedHashMap<String, Object> linkedHashMap : list) {
					byte[] data = (byte[]) linkedHashMap.get("data");
					PdfReader reader = new PdfReader(data);
					for (int i = 1; i <= reader.getNumberOfPages(); i++) {
						document.newPage();
						// import the page from source pdf
						PdfImportedPage page = copy.getImportedPage(reader, i);

						// PageStamp stamp = copy.createPageStamp(page);
						// Chunk chunk = new Chunk(String.format("Sayfa : %d ", i));
						// // Write the text into page (represented by stamp object
						// ColumnText.showTextAligned(stamp.getUnderContent(), Element.ALIGN_RIGHT, new Phrase(chunk), 450, 10, 0);
						// stamp.alterContents();
						// add the page to the destination pdf

						pageContentByte.addTemplate(page, 0, 0);
						copy.addPage(page);
					}
				}
				document.close();
			}
		}

		try {
			outputStream.flush();

			outputStream.close();
		} catch (Exception e) {
			logger.equals(e);
			e.printStackTrace();

		}

		return outputStream;
	}

	/**
	 * @param personelIzin
	 * @param kaydeden
	 * @param session
	 * @return
	 */
	public List<User> izinOnayIslemleri(PersonelIzin personelIzin, User kaydeden, Session session) {

		// ilk olarak izin tipine bakarak kac asamali onay oldugunu bulalim.
		// sonra bu kademe kadar onaylayacak kisileri bulalim
		// durum onaylanmadi seklinde insertleri yapalim.
		// ilk kisiye otomatik mail atalim
		// onay ekraninda izin id ile bir sonrakini bulup mail atalim.
		// eger onaylamaz ise bir yukariya mail atmayalim
		List<User> toList = new ArrayList<User>();
		ArrayList<PersonelIzinOnay> savePersonelOnayList = new ArrayList<PersonelIzinOnay>();

		User ilkYoneticiUser = null;
		PersonelIzinOnay personelIzinOnay = null;
		if (personelIzin.getIzinTipi().getOnaylayanTipi().equals(IzinTipi.ONAYLAYAN_TIPI_YONETICI1) || personelIzin.getIzinTipi().getOnaylayanTipi().equals(IzinTipi.ONAYLAYAN_TIPI_YONETICI2)) {
			HashMap parametreMap = new HashMap();
			parametreMap.put("pdksPersonel.id", personelIzin.getIzinSahibi().getPdksYonetici().getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			ilkYoneticiUser = (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);

		} else {
			return null;
		}
		// }
		/*
		 * buraya hic dusmez.cunku Ik kendisi icin girse de, Ik icin yoneticisi de girse hep IK girdigi icin onay mekanizmasina hic girmeyecektir. else if (personelIzin.getIzinTipi().getOnaylayanTipi().equals(IzinTipi .ONAYLAYAN_TIPI_IK) && izinsahibiUser.isIK()) { / bu durum Belli izin tiplerini
		 * sadece Insan Kaynaklari girecektir. Bu isaretleme izin tipi tanimlama ekraninda yapilacaktir. Insan Kaynaklarinin girdigi izinler onay mekanizmasina sokulmayacaktir. Dogrudan onaylanmis sekilde isleme alinacaktir. Izinle ilgii herhangi bir yere mail gönderilmeyecektir.
		 * 
		 * Bu gereksinim sadece IK onaylar tipinde tanimlanmistir. ve giren kisi de IK ise onaya gitmez
		 * 
		 * return toList; }
		 */
		if (ilkYoneticiUser != null) {

			personelIzinOnay = new PersonelIzinOnay();
			personelIzinOnay.setOnayDurum(PersonelIzinOnay.ONAY_DURUM_ISLEM_YAPILMADI);
			personelIzinOnay.setDurum(Boolean.TRUE);
			personelIzinOnay.setOlusturanUser(authenticatedUser);
			personelIzinOnay.setOlusturmaTarihi(new Date());
			personelIzinOnay.setOnaylayanTipi(PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI1);
			personelIzinOnay.setPersonelIzin(personelIzin);
			personelIzinOnay.setOnaylayan(ilkYoneticiUser);
			savePersonelOnayList.add(personelIzinOnay);
			// en son olarak IK ye mail gitsin. ilk yoneticiye gider once
			toList.add(ilkYoneticiUser);
			try {
				User vekil = getYoneticiBul(personelIzin.getIzinSahibi(), ilkYoneticiUser.getPdksPersonel(), session);
				if (vekil != null && !vekil.getId().equals(ilkYoneticiUser.getId()))
					toList.add(vekil);

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
			}

		}

		if (!savePersonelOnayList.isEmpty()) {

			for (PersonelIzinOnay izinOnay : savePersonelOnayList)
				try {
					pdksEntityController.saveOrUpdate(session, entityManager, izinOnay);
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}
		}

		return toList;
	}

	/**
	 * @param dosya
	 * @param sayfadaGoster
	 * @return
	 * @throws Exception
	 */
	public String downloadFile(Dosya dosya, boolean sayfadaGoster) throws Exception {

		try {
			String location = "attachment";
			if (sayfadaGoster)
				location = dosya.getDisposition();
			// HttpServletResponse response = (HttpServletResponse)
			// externalCtx.getResponse();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
			response.setCharacterEncoding("UTF-8");
			response.setContentType(dosya.getIcerikTipi().trim() + "; charset=UTF-8");
			response.setHeader("Content-disposition", location + "; filename=" + PdksUtil.encoderURL(dosya.getDosyaAdi().trim(), "UTF-8"));
			response.setContentLength(dosya.getDosyaIcerik().length);
			ServletOutputStream os = response.getOutputStream();
			os.write(dosya.getDosyaIcerik());
			os.flush();
			os.close();
			// facesCtx.responseComplete();
			FacesContext.getCurrentInstance().responseComplete();
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			PdksUtil.addMessageError("File inderme hatasi : " + e.getMessage());

		}
		return null;
	}

	/**
	 * @param sicilNoList
	 * @param xDonemSonu
	 * @param xSirket
	 * @param harcananIzinlerHepsi
	 * @param personelKontrol
	 * @param session
	 * @return
	 */
	public HashMap<Long, TempIzin> senelikIzinListesiOlustur(ArrayList<String> sicilNoList, Date xDonemSonu, Sirket gelenSirket, boolean harcananIzinlerHepsi, boolean personelKontrol, boolean iptalIzinleriGetir, Session session) {
		Sirket xSirket = null;
		boolean tarihBazli = xDonemSonu != null;
		if (gelenSirket != null && gelenSirket.getId() != null) {
			HashMap fields = new HashMap();
			fields.put("id", gelenSirket.getId());
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			xSirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);
		}
		HashMap<Long, TempIzin> izinMap = new HashMap<Long, TempIzin>();
		HashMap parametreMap = new HashMap();
		parametreMap.put("pdksSicilNo", sicilNoList);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Personel> personeller = pdksEntityController.getObjectByInnerObjectList(parametreMap, Personel.class);
		for (Iterator iterator = personeller.iterator(); iterator.hasNext();) {
			Personel personel = (Personel) iterator.next();
			if (personel.getDogumTarihi() == null) {
				PdksUtil.addMessageAvailableWarn(personel.getPdksSicilNo() + " " + personel.getAdSoyad() + " doğum tarihi tanımsız!");
				iterator.remove();
			}

		}

		parametreMap.clear();
		List<String> kodlar = new ArrayList<String>();
		List<IzinTipi> izinTipleri = null;
		kodlar.add(IzinTipi.YILLIK_UCRETLI_IZIN);
		if (xSirket == null || xSirket.isErp())
			kodlar.add(IzinTipi.SUA_IZNI);
		parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "id");
		parametreMap.put("tipi", Tanim.TIPI_IZIN_TIPI);
		parametreMap.put("kodu", kodlar);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Long> izinTipiIdler = pdksEntityController.getObjectByInnerObjectList(parametreMap, Tanim.class);
		kodlar = null;
		if (!izinTipiIdler.isEmpty()) {
			parametreMap.clear();
			parametreMap.put("bakiyeIzinTipi.izinTipiTanim.id", izinTipiIdler);
			parametreMap.put("bakiyeIzinTipi.personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
			if (xSirket != null)
				parametreMap.put("departman.id=", xSirket.getDepartman().getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			izinTipleri = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, IzinTipi.class);
		} else
			izinTipleri = new ArrayList<IzinTipi>();
		izinTipiIdler = null;
		parametreMap.clear();
		StringBuffer qsb = new StringBuffer();
		qsb.append("SELECT S.* FROM " + PersonelIzin.TABLE_NAME + " S  WITH(nolock) ");
		if (personeller != null && !personeller.isEmpty()) {
			List<Long> idler = new ArrayList<Long>();
			for (Personel p : personeller)
				idler.add(p.getId());
			parametreMap.put("p", idler);
			qsb.append(" WHERE S." + PersonelIzin.COLUMN_NAME_PERSONEL + " :p");
			idler = null;
			if (izinTipleri != null && !izinTipleri.isEmpty()) {
				StringBuffer sb = new StringBuffer();
				for (Iterator iterator = izinTipleri.iterator(); iterator.hasNext();) {
					IzinTipi izinTipi = (IzinTipi) iterator.next();
					sb.append(izinTipi.getId() + (iterator.hasNext() ? " , " : ""));
				}

				// String value = PdksEntityController.SELECT_KARAKTER +
				// ".izinTipi in ( " + sb.toString() + " )";
				qsb.append(" AND S." + PersonelIzin.COLUMN_NAME_IZIN_TIPI + " IN ( " + sb.toString() + " )");
				// parametreMap.put(PdksEntityController.MAP_KEY_SQLADD, value);
				sb = null;

			}
			qsb.append(" AND S." + PersonelIzin.COLUMN_NAME_IZIN_DURUMU + " <> " + PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL);
			qsb.append(" ORDER BY S." + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI);
			// qsb.append(" AND S." + PersonelIzin.COLUMN_NAME_IZIN_KAGIDI_GELDI
			// + " IS NULL");
		}
		List<PersonelIzin> izinList = null;
		try {
			if (!parametreMap.isEmpty()) {
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				// izinList =
				// pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap,
				// PersonelIzin.class);
				izinList = pdksEntityController.getObjectBySQLList(qsb, parametreMap, PersonelIzin.class);
			} else
				izinList = new ArrayList<PersonelIzin>();

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

			izinList = new ArrayList<PersonelIzin>();
		}
		Date tarihKontrol = xDonemSonu != null ? xDonemSonu : PdksUtil.getDate(new Date());
		List<Long> hakEdilmeyenTarihList = new ArrayList<Long>();
		List<PersonelIzin> hakedilmeyenIzinler = new ArrayList<PersonelIzin>();
		List<Long> hakedisIdList = new ArrayList<Long>();
		for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
			PersonelIzin personelIzin = (PersonelIzin) iterator.next();
			Date bitisZamani = personelIzin.getBitisZamani();
			if (xDonemSonu != null && bitisZamani.after(xDonemSonu)) {
				if (tarihBazli) {
					double kullanilanIzin = personelIzin.getHarcananIzin();
					if (kullanilanIzin != 0.0d) {
						PersonelIzin izin = (PersonelIzin) personelIzin.clone();
						izin.setIzinSuresi(0.0d);
						hakedilmeyenIzinler.add(izin);
						izin.setDevirIzin(Boolean.TRUE);
					}

				}
				iterator.remove();
			} else {
				if (bitisZamani.after(tarihKontrol)) {
					Long perId = personelIzin.getPdksPersonel().getId();
					if (hakEdilmeyenTarihList.contains(perId)) {
						iterator.remove();
						continue;
					}
					hakEdilmeyenTarihList.add(perId);
				}
				personelIzin.setDevirIzin(personelIzin.getBaslangicZamani().getTime() == PdksUtil.getBakiyeYil().getTime());
				if (personelIzin.getDevirIzin()) {
					if (personelIzin.getIzinSuresi() == null || personelIzin.getIzinSuresi().doubleValue() == 0.0d)
						iterator.remove();
				}
			}

		}
		TreeMap<Long, List<PersonelIzin>> map1 = new TreeMap<Long, List<PersonelIzin>>();
		TreeMap<Long, PersonelIzin> izinlerMap = new TreeMap<Long, PersonelIzin>();

		if (!izinList.isEmpty() || !hakedilmeyenIzinler.isEmpty()) {

			for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
				PersonelIzin personelIzin = (PersonelIzin) iterator.next();
				izinlerMap.put(personelIzin.getId(), personelIzin);
				hakedisIdList.add(personelIzin.getId());

			}

			for (PersonelIzin personelIzin : hakedilmeyenIzinler) {
				hakedisIdList.add(personelIzin.getId());
			}
			String fieldName = "h";
			parametreMap.clear();
			parametreMap.put(fieldName, hakedisIdList);
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT DISTINCT D.*  FROM " + PersonelIzinDetay.TABLE_NAME + " D  WITH(nolock) ");
			sb.append(" INNER JOIN " + PersonelIzin.TABLE_NAME + " H ON  H." + PersonelIzin.COLUMN_NAME_ID + " = D." + PersonelIzinDetay.COLUMN_NAME_HAKEDIS_IZIN);
			sb.append(" INNER JOIN " + PersonelIzin.TABLE_NAME + " I ON  I." + PersonelIzin.COLUMN_NAME_ID + " = D." + PersonelIzinDetay.COLUMN_NAME_IZIN);
			sb.append(" AND I." + PersonelIzin.COLUMN_NAME_IZIN_DURUMU + " NOT IN (8,9)");
			sb.append(" WHERE D." + PersonelIzinDetay.COLUMN_NAME_HAKEDIS_IZIN + " :" + fieldName);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				// List<PersonelIzin> list = pdksEntityController.getObjectBySQLList(sb, parametreMap, PersonelIzin.class);
				List<PersonelIzinDetay> list = getSQLParamList(hakedisIdList, sb, fieldName, parametreMap, PersonelIzinDetay.class, session);

				for (PersonelIzinDetay personelIzinDetay : list) {
					Long key = personelIzinDetay.getHakEdisIzin().getId();
					List<PersonelIzin> izinler = map1.containsKey(key) ? map1.get(key) : new ArrayList<PersonelIzin>();
					if (!izinlerMap.containsKey(key)) {
						PersonelIzin izin = (PersonelIzin) personelIzinDetay.getHakEdisIzin().clone();
						izin.setIzinSuresi(0.0d);
						hakedilmeyenIzinler.add(izin);
						izin.setDevirIzin(Boolean.TRUE);
						izinlerMap.put(key, izin);
						izinList.add(izin);
					}

					if (izinler.isEmpty())
						map1.put(key, izinler);
					izinler.add(personelIzinDetay.getPersonelIzin());
				}
				list = null;

			} catch (Exception e) {

				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
			}
		}

		if (!personelKontrol)
			sicilNoList = authenticatedUser.getYetkiTumPersonelNoList();
		Calendar cal = Calendar.getInstance();
		int yil = cal.get(Calendar.YEAR);
		cal.set(yil, 0, 1);
		cal.set(yil - 1, 0, 1);

		for (PersonelIzin personelIzin : izinList) {
			if (personelKontrol && !sicilNoList.contains(personelIzin.getIzinSahibi().getSicilNo()))
				continue;
			Long perId = personelIzin.getIzinSahibi().getId();
			Personel personel = (Personel) personelIzin.getIzinSahibi().clone();

			TempIzin tempIzin = null;
			if (izinMap.containsKey(perId))
				tempIzin = izinMap.get(perId);
			else {
				tempIzin = new TempIzin();
				tempIzin.setIzinler(new ArrayList<Long>());
				tempIzin.setPersonelIzin(personelIzin);
				tempIzin.setPersonel(personel);
				tempIzin.setToplamKalanIzin(0);
				tempIzin.setKullanilanIzin(0);
				tempIzin.setToplamBakiyeIzin(0);
				tempIzin.setYillikIzinler(new ArrayList<PersonelIzin>());
			}
			if (personelIzin.getDevirIzin() || (iptalIzinleriGetir && personelIzin.getIzinKagidiGeldi() != null) || personelIzin.getIzinSuresi() > 0 || (personelIzin.getHarcananDigerIzinler() != null && !personelIzin.getHarcananDigerIzinler().isEmpty()))
				tempIzin.getYillikIzinler().add(personelIzin);
			// session.refresh(personelIzin);
			personelIzin.setKontrolIzin(null);
			personelIzin.setDonemSonu(harcananIzinlerHepsi ? null : xDonemSonu);
			tempIzin.setToplamKalanIzin(tempIzin.getToplamKalanIzin() + personelIzin.getKalanIzin());
			tempIzin.setKullanilanIzin(tempIzin.getKullanilanIzin() + personelIzin.getHarcananIzin());
			tempIzin.setToplamBakiyeIzin(tempIzin.getToplamBakiyeIzin() + personelIzin.getIzinSuresi());
			if (personelIzin.getDevirIzin() || personelIzin.getIzinKagidiGeldi() == null || iptalIzinleriGetir || map1.containsKey(personelIzin.getId()))
				tempIzin.getIzinler().add(personelIzin.getId());
			izinMap.put(perId, tempIzin);
		}

		for (Personel personel : personeller) {
			Long perId = personel.getId();
			if (izinMap.containsKey(perId))
				continue;
			TempIzin tempIzin = new TempIzin();
			tempIzin.setPersonel(personel);
			tempIzin.setToplamKalanIzin(0d);
			izinMap.put(personel.getId(), tempIzin);
		}

		return izinMap;
	}

	/**
	 * @param session
	 * @param basTarih
	 * @param bitTarih
	 * @return
	 */
	public List<YemekOgun> fillYemekList(Session session, Date basTarih, Date bitTarih) {
		HashMap parametreMapYemek = new HashMap();
		parametreMapYemek.put("bitTarih>=", basTarih);
		parametreMapYemek.put("basTarih<", tariheGunEkleCikar(null, bitTarih, 1));
		parametreMapYemek.put("durum=", Boolean.TRUE);
		if (session != null)
			parametreMapYemek.put(PdksEntityController.MAP_KEY_SESSION, session);

		List<YemekOgun> list = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMapYemek, YemekOgun.class);
		if (list.size() > 1)
			list = PdksUtil.sortListByAlanAdi(list, "baslangicSaat", Boolean.FALSE);
		return list;

	}

	/**
	 * @param session
	 * @param basTarih
	 * @param bitTarih
	 * @param durum
	 * @return
	 */
	public List<HareketKGS> getYemekHareketleri(Session session, Date basTarih, Date bitTarih, boolean durum) {
		Calendar cal = Calendar.getInstance();
		List<YemekOgun> yemekOgunList = fillYemekList(session, basTarih, bitTarih);
		List<HareketKGS> kgsList = new ArrayList<HareketKGS>();
		HashMap parametreMap = new HashMap();
		showSQLQuery(parametreMap);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Long> yemekKapiList = getYemekKapiIdList(session);
		List list2 = null;
		try {
			list2 = getHareketBilgileri(yemekKapiList, null, PdksUtil.getDate(basTarih), PdksUtil.getDate(tariheGunEkleCikar(cal, bitTarih, 1)), HareketKGS.class, session);

		} catch (Exception e) {
			// TODO: handle exception
		}
		if (list2 != null) {

			if (!list2.isEmpty())
				kgsList.addAll(list2);
		}
		if (kgsList != null && !kgsList.isEmpty()) {
			int yemekMukerrerAraligi = getYemekMukerrerAraligi();
			kgsList = PdksUtil.sortListByAlanAdi(kgsList, "zaman", Boolean.FALSE);
			Personel personel = new Personel();
			Sirket sirket = new Sirket();
			sirket.setAd("Sirket Tanimsiz");
			personel.setSirket(sirket);
			Calendar calendar = Calendar.getInstance();
			YemekOgun tanimYemek = new YemekOgun();
			tanimYemek.setId(0L);
			tanimYemek.setYemekAciklama("Tanimsiz");
			HashMap<String, HareketKGS> yemekZamanMap = new HashMap<String, HareketKGS>();
			String pattern = PdksUtil.getDateTimeFormat();
			List<String> list = new ArrayList<String>();
			for (Iterator iterator = kgsList.iterator(); iterator.hasNext();) {
				HareketKGS hareketKGS = (HareketKGS) iterator.next();
				if (hareketKGS.getId() == null || list.contains(hareketKGS.getId())) {
					iterator.remove();
					continue;
				}
				list.add(hareketKGS.getId());
				hareketKGS.setGecerliYemek(null);
				hareketKGS.addYemekTeloreansZamani(yemekMukerrerAraligi);
				PersonelView personelView = hareketKGS.getPersonel();
				hareketKGS.setCheckBoxDurum(Boolean.FALSE);
				try {
					if (personelView.getPdksPersonel() == null || personelView.getPdksPersonel().getId() == null) {
						personelView.setPdksPersonel(personel);

					}

					long yemekZamanikgs = Long.parseLong((PdksUtil.convertToDateString(hareketKGS.getZaman(), "yyyyMMddHHmm")));
					YemekOgun yemekOgun = null;

					for (YemekOgun yemekOgunOrj : yemekOgunList) {
						YemekOgun yemek = (YemekOgun) yemekOgunOrj.clone();
						if (PdksUtil.tarihKarsilastirNumeric(yemekOgunOrj.getBasTarih(), hareketKGS.getZaman()) == 1 || PdksUtil.tarihKarsilastirNumeric(hareketKGS.getZaman(), yemekOgunOrj.getBitTarih()) == 1) {
							continue;

						}
						calendar.setTime(hareketKGS.getZaman());
						calendar.set(Calendar.HOUR_OF_DAY, yemek.getBaslangicSaat());
						calendar.set(Calendar.MINUTE, yemek.getBaslangicDakika());
						long yemekZamaniBas = Long.parseLong(PdksUtil.convertToDateString(calendar.getTime(), "yyyyMMddHHmm"));
						Date yemekGun = (Date) hareketKGS.getZaman().clone();
						if (yemek.getBitisSaat() < yemek.getBaslangicSaat()) {
							int saat = PdksUtil.getDateField(hareketKGS.getZaman(), Calendar.HOUR_OF_DAY);
							if (saat >= yemek.getBaslangicSaat())
								calendar.add(Calendar.DATE, 1);
							else {
								calendar.add(Calendar.DATE, -1);
								yemekGun = calendar.getTime();
								yemekZamaniBas = Long.parseLong(PdksUtil.convertToDateString(calendar.getTime(), "yyyyMMddHHmm"));
								calendar.setTime(hareketKGS.getZaman());
							}
						}
						calendar.set(Calendar.HOUR_OF_DAY, yemek.getBitisSaat());
						calendar.set(Calendar.MINUTE, yemek.getBitisDakika());
						long yemekZamaniBit = Long.parseLong(PdksUtil.convertToDateString(calendar.getTime(), "yyyyMMddHHmm"));

						if (yemekZamaniBas <= yemekZamanikgs && yemekZamanikgs < yemekZamaniBit) {
							yemekOgun = (YemekOgun) yemekOgunOrj.clone();
							String key = hareketKGS.getKapiView().getId() + "_" + PdksUtil.convertToDateString(yemekGun, "yyyyMMdd") + "_" + yemekOgun.getId() + "_" + personelView.getId();
							if (!yemekZamanMap.containsKey(key)) {
								hareketKGS.setStyle(VardiyaGun.STYLE_CLASS_ODD);
								// yemekZamanMap.put(key, HareketKGS);

							} else {
								HareketKGS HareketKGS2 = yemekZamanMap.get(key);
								hareketKGS.setGecerliYemek(HareketKGS2.getYemekTeloreansZamani().before(hareketKGS.getZaman()));
								if (hareketKGS.getGecerliYemek()) {
									hareketKGS.setOncekiYemekZamani(HareketKGS2.getZaman());
									PdksUtil.addMessageAvailableInfo(personelView.getSicilNo() + "-" + personelView.getAdSoyad() + " " + PdksUtil.convertToDateString(hareketKGS.getOncekiYemekZamani(), pattern) + " " + hareketKGS.getKapiView().getKapiAciklama() + " " + yemekOgun.getYemekAciklama());
								}
								hareketKGS.setStyle(VardiyaGun.STYLE_CLASS_HATA);
								hareketKGS.setCheckBoxDurum(Boolean.TRUE);
								HareketKGS2.setStyle(VardiyaGun.STYLE_CLASS_EVEN);
							}
							yemekZamanMap.put(key, hareketKGS);
							break;

						}
					}

					hareketKGS.setYemekOgun(yemekOgun != null ? yemekOgun : tanimYemek);
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());
					logger.error(hareketKGS.getId() + " " + e.getMessage());
				}

			}
			list = null;

		}
		return kgsList;

	}

	/**
	 * @param izinTipiKodu
	 * @param sicilNoList
	 * @param sirket
	 * @param tarih
	 * @param yil
	 * @param personelKontrol
	 * @param session
	 * @return
	 */
	public HashMap<Long, TempIzin> bakiyeIzinListesiOlustur(String izinTipiKodu, ArrayList<String> sicilNoList, Sirket sirket, Date tarih, int yil, boolean personelKontrol, Session session) {
		List<String> haricKodlar = new ArrayList<String>();
		haricKodlar.add(IzinTipi.YURT_DISI_KONGRE);
		haricKodlar.add(IzinTipi.YURT_ICI_KONGRE);
		haricKodlar.add(IzinTipi.MOLA_IZNI);
		HashMap<Long, TempIzin> izinMap = new HashMap<Long, TempIzin>();
		HashMap parametreMap = new HashMap();
		parametreMap.put("pdksSicilNo", sicilNoList);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Personel> personeller = pdksEntityController.getObjectByInnerObjectList(parametreMap, Personel.class);
		for (Iterator iterator = personeller.iterator(); iterator.hasNext();) {
			Personel personel = (Personel) iterator.next();
			if (izinTipiKodu.equals(IzinTipi.SUA_IZNI)) {
				if (!personel.isSuaOlur())
					iterator.remove();
			} else if (haricKodlar.contains(izinTipiKodu) && !personel.isHekim())
				iterator.remove();
		}
		List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
		Calendar cal = Calendar.getInstance();
		Date sistemTarihi = tarih != null ? (Date) tarih.clone() : null;
		parametreMap.clear();
		StringBuffer qsb = new StringBuffer();
		qsb.append("SELECT S.* FROM " + PersonelIzin.TABLE_NAME + " S  WITH(nolock)");
		qsb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P.id=S." + PersonelIzin.COLUMN_NAME_PERSONEL);

		if (izinTipiKodu.equals(IzinTipi.SUA_IZNI))
			qsb.append(" AND P.SUA_OLABILIR=1 ");
		List<IzinTipi> izinTipList = null;
		parametreMap.clear();
		if (!authenticatedUser.isYoneticiKontratli() && sirket != null)
			parametreMap.put("departman.id=", sirket.getDepartman().getId());
		parametreMap.put("izinTipiTanim.kodu=", izinTipiKodu);
		parametreMap.put("personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		izinTipList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, IzinTipi.class);

		if (personeller != null && !personeller.isEmpty()) {
			parametreMap.clear();
			List<Long> idler = new ArrayList<Long>();
			for (Personel p : personeller)
				idler.add(p.getId());
			// parametreMap.put("izinSahibi.id", idler);
			parametreMap.put("p", idler);
			qsb.append(" WHERE S." + PersonelIzin.COLUMN_NAME_PERSONEL + " :p");
			if (izinTipList != null) {
				for (Iterator iterator = izinTipList.iterator(); iterator.hasNext();) {
					IzinTipi izinTipi = (IzinTipi) iterator.next();
					if (izinTipi.getBakiyeIzinTipi() == null)
						iterator.remove();
				}
				if (!izinTipList.isEmpty()) {
					StringBuffer sb = new StringBuffer();
					for (Iterator iterator = izinTipList.iterator(); iterator.hasNext();) {
						IzinTipi izinTipi = (IzinTipi) iterator.next();
						sb.append(izinTipi.getId() + (iterator.hasNext() ? " , " : ""));
					}

					// String value = PdksEntityController.SELECT_KARAKTER +
					// ".izinTipi in ( " + sb.toString() + " )";
					qsb.append(" AND S." + PersonelIzin.COLUMN_NAME_IZIN_TIPI + " IN ( " + sb.toString() + " )");
					// parametreMap.put(PdksEntityController.MAP_KEY_SQLADD,
					// value);
					sb = null;
				}
			}
			qsb.append(" AND S." + PersonelIzin.COLUMN_NAME_IZIN_DURUMU + " <> " + PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL);
			qsb.append(" AND S." + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + " = :baslangicZamani");

			cal = Calendar.getInstance();
			cal.set(yil, 0, 1);
			Date baslangicZamani = PdksUtil.getDate(cal.getTime());
			parametreMap.put("izinSahibi.id", idler);
			parametreMap.put("baslangicZamani", baslangicZamani);
		}

		if (!parametreMap.isEmpty() && !izinTipList.isEmpty()) {

			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			izinList = pdksEntityController.getObjectBySQLList(qsb, parametreMap, PersonelIzin.class);
		} else
			izinList = new ArrayList<PersonelIzin>();

		if (!izinList.isEmpty())
			izinList = PdksUtil.sortListByAlanAdi(izinList, "baslangicZamani", Boolean.TRUE);
		String onayTipi = null, izinKodu = null;
		sicilNoList = authenticatedUser.getYetkiTumPersonelNoList();
		List sicilller2 = authenticatedUser.getIkinciYoneticiPersonelSicilleri();
		if (sicilller2 != null)
			sicilNoList.addAll(sicilller2);

		for (PersonelIzin personelIzin : izinList) {
			if (personelIzin.getIzinDurumu() == PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL || (personelKontrol && !sicilNoList.contains(personelIzin.getIzinSahibi().getSicilNo())))
				continue;
			if (onayTipi == null)
				onayTipi = personelIzin.getIzinTipi().getBakiyeIzinTipi().getOnaylayanTipi();
			if (izinKodu == null)
				izinKodu = personelIzin.getIzinTipi().getBakiyeIzinTipi().getIzinTipiTanim().getKodu();
			if (personelIzin.getIzinSahibi().isSuaOlur()) {
				if (izinKodu.equals(IzinTipi.YILLIK_UCRETLI_IZIN))
					continue;
			} else if (izinKodu.equals(IzinTipi.SUA_IZNI))
				continue;

			if (onayTipi.equals(IzinTipi.ONAYLAYAN_TIPI_YOK) && !(personelIzin.getIzinSahibi().isHekim() || (personelIzin.getIzinSahibi().getOnaysizIzinKullanilir() != null && personelIzin.getIzinSahibi().getOnaysizIzinKullanilir())))
				continue;
			// session.refresh(personelIzin);

			Personel personel = (Personel) personelIzin.getIzinSahibi().clone();
			personelIzin.setKontrolIzin(null);
			personelIzin.setDonemSonu(sistemTarihi);
			TempIzin tempIzin = null;
			if (izinMap.containsKey(personel.getId()))
				tempIzin = izinMap.get(personel.getId());
			else {
				tempIzin = new TempIzin();
				tempIzin.setIzinler(new ArrayList<Long>());
				tempIzin.setPersonelIzin(personelIzin);
				tempIzin.setPersonel(personel);
				tempIzin.setToplamKalanIzin(0);
				tempIzin.setKullanilanIzin(0);
				tempIzin.setToplamBakiyeIzin(0);

			}
			tempIzin.setKullanilanIzin(tempIzin.getKullanilanIzin() + personelIzin.getHarcananIzin());
			tempIzin.setToplamBakiyeIzin(tempIzin.getToplamBakiyeIzin() + personelIzin.getIzinSuresi());
			tempIzin.setToplamKalanIzin(tempIzin.getToplamKalanIzin() + personelIzin.getKalanIzin());
			tempIzin.getIzinler().add(personelIzin.getId());
			izinMap.put(personel.getId(), tempIzin);
		}
		for (Personel personel : personeller) {
			if (izinMap.containsKey(personel.getId()))
				continue;
			if (onayTipi != null && onayTipi.equals(IzinTipi.ONAYLAYAN_TIPI_YOK) && !personel.isOnaysizIzinKullanir())
				continue;
			if (izinTipiKodu.equals(IzinTipi.SUA_IZNI) && !personel.isSuaOlur())
				continue;
			TempIzin tempIzin = new TempIzin();
			tempIzin.setPersonel(personel);
			tempIzin.setToplamKalanIzin(0d);

			izinMap.put(personel.getId(), tempIzin);

		}
		return izinMap;
	}

	/**
	 * @param sicilNoList
	 * @param sirket
	 * @param basTarih
	 * @param bitTarih
	 * @param personelKontrol
	 * @param session
	 * @return
	 */
	public HashMap<Long, TempIzin> fazlaMesaiIzinListesiOlustur(ArrayList<String> sicilNoList, Sirket sirket, Date basTarih, Date bitTarih, boolean personelKontrol, Session session) {
		HashMap<Long, TempIzin> izinMap = new HashMap<Long, TempIzin>();
		HashMap parametreMap = new HashMap();
		parametreMap.put("pdksSicilNo", sicilNoList);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Personel> personeller = pdksEntityController.getObjectByInnerObjectList(parametreMap, Personel.class);
		parametreMap.clear();
		if (sirket != null)
			parametreMap.put("departman=", sirket.getDepartman());
		else
			parametreMap.put("departman=", authenticatedUser.getDepartman());
		parametreMap.put("izinTipiTanim.kodu=", IzinTipi.FAZLA_MESAI);
		parametreMap.put("bakiyeIzinTipi<>", null);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		parametreMap.put("personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
		IzinTipi izinTipi = (IzinTipi) pdksEntityController.getObjectByInnerObjectInLogic(parametreMap, IzinTipi.class);
		parametreMap.clear();
		List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
		parametreMap.put("izinSahibi", personeller);
		if (bitTarih != null)
			parametreMap.put("baslangicZamani<=", bitTarih);
		if (basTarih != null)
			parametreMap.put("bitisZamani>=", basTarih);
		if (!authenticatedUser.isIK() && !authenticatedUser.isAdmin())
			parametreMap.put("izinDurumu=", PersonelIzin.IZIN_DURUMU_ONAYLANDI);
		if (izinTipi != null)
			parametreMap.put("izinTipi=", izinTipi);
		else {
			parametreMap.put("izinTipi.izinTipiTanim.kodu=", IzinTipi.FAZLA_MESAI);
			parametreMap.put("izinTipi.bakiyeIzinTipi<>", null);

		}
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		izinList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, PersonelIzin.class);
		if (!izinList.isEmpty())
			izinList = PdksUtil.sortListByAlanAdi(izinList, "baslangicZamani", Boolean.TRUE);
		for (PersonelIzin personelIzin : izinList) {
			if (personelIzin.getIzinSuresi() <= 0)
				continue;

			personelIzin.setKontrolIzin(null);
			personelIzin.setDonemSonu(null);
			Personel personel = (Personel) personelIzin.getIzinSahibi().clone();
			Long perId = personel.getId();
			TempIzin tempIzin = null;
			if (izinMap.containsKey(perId))
				tempIzin = izinMap.get(perId);
			else {
				tempIzin = new TempIzin();
				tempIzin.setIzinler(new ArrayList<Long>());
				tempIzin.setPersonelIzin(personelIzin);
				tempIzin.setPersonel(personel);
				tempIzin.setToplamKalanIzin(0);
				tempIzin.setKullanilanIzin(0);
				tempIzin.setToplamBakiyeIzin(0);
			}
			tempIzin.setToplamKalanIzin(tempIzin.getToplamKalanIzin() + personelIzin.getKalanIzin());
			tempIzin.setKullanilanIzin(tempIzin.getKullanilanIzin() + personelIzin.getHarcananIzin());
			tempIzin.setToplamBakiyeIzin(tempIzin.getToplamBakiyeIzin() + personelIzin.getIzinSuresi());
			tempIzin.getIzinler().add(personelIzin.getId());
			izinMap.put(perId, tempIzin);
		}
		return izinMap;
	}

	/**
	 * @param puantaj
	 * @param vardiyaGun
	 * @param yemekList
	 * @return
	 */
	public double getSaatToplami(AylikPuantaj puantaj, VardiyaGun vardiyaGun, List<YemekIzin> yList, Session session) {
		double saatToplami = 0d;
		if (vardiyaGun != null && vardiyaGun.getVardiya() != null) {
			List<YemekIzin> yemekList = new ArrayList<YemekIzin>();
			if (yList != null && !yList.isEmpty())
				yemekList.addAll(yList);

			String pattern = PdksUtil.getDateTimeFormat();
			boolean raporIzni = getVardiyaIzniEkle(vardiyaGun);
			if (vardiyaGun.getVardiya().isCalisma()) {
				boolean ekle = vardiyaGun.getIzin() == null || (raporIzni || vardiyaGun.isSutIzni() || vardiyaGun.isGorevli());
				double sure = 0, fazlaMesaiSure = 0;
				if (ekle) {
					if (vardiyaGun.getTatil() != null && vardiyaGun.isFiiliHesapla() == false) {
						Calendar cal = Calendar.getInstance();
						Tatil tatil = vardiyaGun.getTatil();
						Date bayramBas = tatil.getBasTarih();
						if (vardiyaGun.getVardiya().getArifeBaslangicTarihi() != null)
							bayramBas = vardiyaGun.getVardiya().getArifeBaslangicTarihi();
						Date bayramBit = tariheGunEkleCikar(cal, tatil.getBitTarih(), 0);
						List<Date> calBas = new ArrayList<Date>(), calBit = new ArrayList<Date>(), tatilBas = new ArrayList<Date>(), tatilBit = new ArrayList<Date>();
						vardiyaGun.setVardiyaZamani();
						Vardiya vardiya = vardiyaGun.getIslemVardiya();
						String str = vardiyaGun.getVardiyaKeyStr() + "\nBayram : " + PdksUtil.convertToDateString(bayramBas, pattern) + " " + PdksUtil.convertToDateString(bayramBit, pattern);
						str += "\nVardiya : " + PdksUtil.convertToDateString(vardiya.getVardiyaBasZaman(), pattern) + " " + PdksUtil.convertToDateString(vardiya.getVardiyaBitZaman(), pattern) + "\n";
						if (vardiya.getVardiyaBitZaman().getTime() < bayramBas.getTime() || vardiya.getVardiyaBasZaman().getTime() > bayramBit.getTime()) {
							calBas.add(vardiya.getVardiyaBasZaman());
							calBit.add(vardiya.getVardiyaBitZaman());
							str += "out";
						} else if (vardiya.getVardiyaBasZaman().getTime() < bayramBas.getTime() && vardiya.getVardiyaBitZaman().getTime() < bayramBit.getTime()) {
							calBas.add(vardiya.getVardiyaBasZaman());
							calBit.add(bayramBas);
							tatilBas.add(bayramBas);
							tatilBit.add(vardiya.getVardiyaBitZaman());
							str += "Start";
						} else if (vardiya.getVardiyaBasZaman().getTime() > bayramBas.getTime()) {
							tatilBas.add(vardiya.getVardiyaBasZaman());
							if (vardiya.getVardiyaBitZaman().getTime() < bayramBit.getTime()) {
								tatilBit.add(vardiya.getVardiyaBitZaman());
								str += "in";
							}

							else {
								tatilBit.add(bayramBit);
								calBas.add(bayramBit);
								calBit.add(vardiya.getVardiyaBitZaman());
								str += "Stop";
							}

						} else
							str += "?";
						logger.debug(str);
						double parcaSure1 = 0.0d, parcaSure2 = 0.0d;
						if (!calBas.isEmpty()) {
							for (int i = 0; i < calBas.size(); i++) {
								Date basTar = calBas.get(i), bitTar = calBit.get(i);
								parcaSure1 += getSaatSure(basTar, bitTar, yemekList, vardiyaGun, session);
								yemekList.clear();
							}
							sure += parcaSure1;
						}
						int basAdet = tatilBas != null ? tatilBas.size() : -1;
						int bitAdet = tatilBit != null ? tatilBit.size() : -2;
						if ((vardiyaGun.getDurum() || vardiyaGun.isFiiliHesapla() == false) && basAdet > 0 && basAdet == bitAdet) {
							for (int i = 0; i < tatilBas.size(); i++) {
								Date basTar = tatilBas.get(i), bitTar = tatilBit.get(i);
								parcaSure2 += getSaatSure(basTar, bitTar, yemekList, vardiyaGun, session);
							}
							fazlaMesaiSure += parcaSure2;
							if (vardiyaGun.isFiiliHesapla() == false && parcaSure1 + parcaSure2 > vardiyaGun.getIslemVardiya().getNetCalismaSuresi()) {
								double yemekFark = vardiyaGun.getIslemVardiya().getNetCalismaSuresi() - (parcaSure1 + parcaSure2);
								if (parcaSure1 > parcaSure2)
									sure += yemekFark;
								else
									fazlaMesaiSure += yemekFark;
							}

							vardiyaGun.setCalismaSuresi(sure);
						}
						calBas = null;
						calBit = null;
						tatilBas = null;
						tatilBit = null;
						if (vardiyaGun.isIzinli() == false && !raporIzni && fazlaMesaiSure > 0) {
							vardiyaGun.setResmiTatilSure(fazlaMesaiSure);
							if (vardiyaGun.getId() != null)
								puantaj.addResmiTatilToplami(fazlaMesaiSure);
						}

					} else
						sure = vardiyaGun.isFiiliHesapla() ? (vardiyaGun.getHareketDurum() ? vardiyaGun.getCalismaSuresi() - vardiyaGun.getResmiTatilSure() : 0) : vardiyaGun.getVardiya().getNetCalismaSuresi();

					saatToplami = sure;
				}
			} else
				saatToplami = vardiyaGun.getCalismaSuresi();
			if (vardiyaGun.getHaftaCalismaSuresi() > 0.0d) {
				puantaj.addHaftaCalismaSuresi(vardiyaGun.getHaftaCalismaSuresi());
			}
			yemekList = null;
		}

		return saatToplami;

	}

	/**
	 * @param denklestirmeAy
	 * @return
	 */
	public double getFazlaMesaiMaxSure(DenklestirmeAy denklestirmeAy) {
		double fazlaMesaiMaxSure = 0d;
		if (denklestirmeAy != null && denklestirmeAy.getFazlaMesaiMaxSure() != null)
			fazlaMesaiMaxSure = denklestirmeAy.getFazlaMesaiMaxSure();
		else {

			try {
				if (getParameterKeyHasStringValue("fazlaMesaiMaxSure"))
					fazlaMesaiMaxSure = Double.parseDouble(getParameterKey("fazlaMesaiMaxSure"));
			} catch (Exception e) {
				fazlaMesaiMaxSure = 11d;
			}
			if (fazlaMesaiMaxSure <= 0d)
				fazlaMesaiMaxSure = 11d;
		}
		return fazlaMesaiMaxSure;
	}

	/**
	 * @param filliHesapla
	 * @param normalCalismaVardiya
	 * @param yemekHesapla
	 * @param puantajData
	 * @param kaydet
	 * @param tatilGunleriMap
	 * @param session
	 * @return
	 */
	public PersonelDenklestirme aylikPlanSureHesapla(boolean filliHesapla, Vardiya normalCalismaVardiya, boolean yemekHesapla, AylikPuantaj puantajData, boolean kaydet, TreeMap<String, Tatil> tatilGunleriMap, Session session) {
		PersonelDenklestirme personelDenklestirme = null;
		try {
			DenklestirmeAy dm = puantajData.getDenklestirmeAy();
			Date sonGun = PdksUtil.tariheAyEkleCikar(PdksUtil.convertToJavaDate(String.valueOf(dm.getYil() * 100 + dm.getAy()) + "01", "yyyyMMdd"), 1);
			boolean personelCalisiyor = puantajData.getPdksPersonel().isCalisiyorGun(sonGun);
			User loginUser = puantajData.getLoginUser() != null ? puantajData.getLoginUser() : authenticatedUser;
			List<YemekIzin> yemekBosList = yemekHesapla ? null : new ArrayList<YemekIzin>();
			String izinTarihKontrolTarihiStr = getParameterKey("izinTarihKontrolTarihi");
			Date pdksIzinTarihKontrolTarihi = null;
			try {
				if (PdksUtil.hasStringValue(izinTarihKontrolTarihiStr))
					pdksIzinTarihKontrolTarihi = PdksUtil.getDateFromString(izinTarihKontrolTarihiStr);
			} catch (Exception localException1) {
			}

			double gunduzCalismaSaat = 45.0d;
			if (puantajData.getPersonelDenklestirme() != null) {
				CalismaModeli calismaModeli = null;
				if (puantajData.getPersonelDenklestirme().getCalismaModeliAy() != null)
					calismaModeli = puantajData.getPersonelDenklestirme().getCalismaModeli();
				else
					calismaModeli = puantajData.getPdksPersonel().getCalismaModeli();
				if (calismaModeli != null)
					gunduzCalismaSaat = (calismaModeli.getHaftaIci() * 5.0d) + calismaModeli.getHaftaSonu();
			}

			HashMap parametreMap = new HashMap();
			parametreMap.put("vardiyaTipi", Vardiya.TIPI_OFF);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			Vardiya offVardiya = (Vardiya) pdksEntityController.getObjectByInnerObject(parametreMap, Vardiya.class);

			if (puantajData != null) {
				boolean ilkGiris = false;
				puantajData.degerSifirla();
				String resmiTatilVardiyaEkleStr = getParameterKey("resmiTatilVardiyaEkle");
				String resmiTatilDepartmanlariStr = getParameterKey("resmiTatilDepartmanlari");
				boolean haftaTatiliFarkHesapla = getParameterKey("haftaTatiliFarkHesapla").equals("1");
				String departmanKodu = null;
				try {
					departmanKodu = puantajData.getPdksPersonel().getEkSaha1() != null ? puantajData.getPdksPersonel().getEkSaha1().getKodu() : null;
				} catch (Exception e) {
					departmanKodu = null;
				}
				List<String> resmiTatilDepartmanlari = PdksUtil.hasStringValue(resmiTatilDepartmanlariStr) ? PdksUtil.getListByString(resmiTatilDepartmanlariStr, null) : null;
				List<String> vardiyaAksamSabahAdlari = PdksUtil.hasStringValue(resmiTatilVardiyaEkleStr) ? PdksUtil.getListByString(resmiTatilVardiyaEkleStr, null) : null;
				boolean resmiTatilEkle = getParameterKey("resmiTatilEkle").equals("1");
				String haftaIciIzinGunEkle = getParameterKey("haftaIciIzinGunEkle");
				Date haftaIciIzinGunTarih = null;
				if (PdksUtil.hasStringValue(haftaIciIzinGunEkle))
					try {
						haftaIciIzinGunTarih = PdksUtil.getDateFromString(haftaIciIzinGunEkle);
					} catch (Exception e) {
						haftaIciIzinGunTarih = null;
					}
				DenklestirmeAy denklestirmeAy = puantajData.getDenklestirmeAy();
				if (denklestirmeAy == null && puantajData.getPersonelDenklestirme() != null)
					denklestirmeAy = puantajData.getPersonelDenklestirme().getDenklestirmeAy();
				double planlanSure = 0, izinSuresi = 0d, ucretiOdenenMesaiSure = 0d, fazlaMesaiMaxSure = getFazlaMesaiMaxSure(denklestirmeAy), resmiTatilSure = 0d;
				boolean resmiTatilVardiyaEkle = false;

				AylikPuantaj sablonAylikPuantaj = puantajData.getSablonAylikPuantaj();
				// TreeMap<String, Tatil> tatilGunleri = new TreeMap<String, Tatil>();
				TreeMap<String, VardiyaGun> ayinGunleri = new TreeMap<String, VardiyaGun>();
				for (VardiyaGun pdksVardiyaGunSablon : sablonAylikPuantaj.getVardiyalar()) {
					// if (pdksVardiyaGunSablon.getTatil() != null)
					// tatilGunleri.put(pdksVardiyaGunSablon.getVardiyaDateStr(), pdksVardiyaGunSablon.getTatil());
					if (pdksVardiyaGunSablon.isAyinGunu())
						ayinGunleri.put(pdksVardiyaGunSablon.getVardiyaDateStr(), pdksVardiyaGunSablon);
				}
				personelDenklestirme = puantajData.getPersonelDenklestirme();
				CalismaModeli calismaModeli = null;
				if (personelDenklestirme != null && personelDenklestirme.getCalismaModeliAy() != null) {
					calismaModeli = personelDenklestirme.getCalismaModeli();

				}

				puantajData.setIzinSuresi(0d);
				boolean suaVar = personelDenklestirme.isSuaDurumu();
				Personel personel = personelDenklestirme.getPersonel();

				double gecenAyResmiTatilSure = 0;
				double haftaTatiliFark = 0.0d, haftaTatilDigerSure = 0.0d;
				Calendar cal = Calendar.getInstance();
				try {
					List<VardiyaHafta> vardiyaHaftas = puantajData.getVardiyaHaftaList();
					int hafta = 0;
					int toplamCalismaGunSayisi = 0, offGunSayisi = 0;
					double izinToplam = 0;
					for (VardiyaHafta pdksVardiyaHafta : vardiyaHaftas) {
						int calismaGunSayisi = 0, raporGunSayisi = 0;

						double toplamSure = 0d, calisilanSure = 0, bazSure = 0d, haftalikIzinSuresi = 0d, calisilmayanSuresi = 0d, vardiyasizSure = 0d;

						pdksVardiyaHafta.setHafta(++hafta);
						List<VardiyaGun> haftaVardiyaGunler = pdksVardiyaHafta.getVardiyaGunler();

						for (VardiyaGun pdksVardiyaGun : haftaVardiyaGunler) {

							Date izinTarihKontrolTarihi = null;
							if (pdksIzinTarihKontrolTarihi != null && pdksIzinTarihKontrolTarihi.getTime() <= pdksVardiyaGun.getVardiyaDate().getTime())
								izinTarihKontrolTarihi = pdksVardiyaGun.getVardiyaDate();
							String key = pdksVardiyaGun.getVardiyaDateStr();
							if (calismaModeli == null) {
								calismaModeli = pdksVardiyaGun.getCalismaModeli();
								if (calismaModeli == null)
									pdksVardiyaGun.getPdksPersonel().getCalismaModeli();
							}

							if (ilkGiris)
								puantajData.setResmiTatilToplami(0.0d);
							ilkGiris = false;

							List<YemekIzin> yemekList = yemekHesapla ? pdksVardiyaGun.getYemekList() : yemekBosList;
							Double izinSaat = null;
							Tatil tatilOrj = tatilGunleriMap.get(key);
							if (tatilGunleriMap.containsKey(key))
								pdksVardiyaGun.setTatil(tatilGunleriMap.get(key));
							boolean arifeGunu = false;
							Vardiya vardiyaIzin = pdksVardiyaGun.getVardiya();
							if (personelDenklestirme != null && personelDenklestirme.getCalismaModeliAy() != null) {
								CalismaModeli calismaModeliAy = personelDenklestirme.getCalismaModeli();

								izinSaat = pdksVardiyaGun.isIzinli() ? calismaModeliAy.getIzinSaat(pdksVardiyaGun) : 0.0d;
								if (pdksVardiyaGun.getIzin() != null && pdksVardiyaGun.getIzin().getIzinTipi().isIslemYokCGS()) {
									izinSaat = 0.0d;
								}
								Tatil tatil = pdksVardiyaGun.getTatil();
								if (tatil != null && tatil.isYarimGunMu() && vardiyaIzin != null) {
									izinSaat = calismaModeliAy.getArife();
									if (vardiyaIzin.isIzin() == false && tatil.getArifeVardiyaYarimHesapla() != null && !tatil.getArifeVardiyaYarimHesapla())
										izinSaat = 0.0d;
									arifeGunu = true;
								}
							}

							double normalCalismaSuresi = izinSaat != null ? izinSaat : normalCalismaVardiya.getNetCalismaSuresi();
							Double mesaiMaxSure = fazlaMesaiMaxSure;
							boolean raporIzni = getVardiyaIzniEkle(pdksVardiyaGun);
							gecenAyResmiTatilSure += pdksVardiyaGun.getGecenAyResmiTatilSure();
							Tatil tatil = null;
							Vardiya islemVardiya = null;
							Double islemVardiyaSuresi = null;
							boolean gebeMi = Boolean.FALSE, suaMi = suaVar;
							Tatil tatil2 = pdksVardiyaGun.getTatil();
							if (vardiyaIzin != null && (vardiyaIzin.isOffGun() || vardiyaIzin.isFMI()) && pdksVardiyaGun.getIzin() == null) {
								if (tatil2 == null || tatil2.isYarimGunMu())
									++offGunSayisi;
							}
							if (tatil2 == null) {
								if (pdksVardiyaGun.getVardiya() != null && pdksVardiyaGun.getVardiya().isCalisma()) {
									Vardiya sonrakiVardiya = pdksVardiyaGun.getSonrakiVardiya();
									pdksVardiyaGun.setSonrakiVardiya(null);
									pdksVardiyaGun.setVardiyaZamani();
									islemVardiya = pdksVardiyaGun.getIslemVardiya();
									islemVardiyaSuresi = islemVardiya != null && islemVardiya.isCalisma() && islemVardiya.getNetCalismaSuresi() > 0.0d ? islemVardiya.getNetCalismaSuresi() : null;
									if (pdksVardiyaGun.getHaftaCalismaSuresi() > 0)
										islemVardiyaSuresi = pdksVardiyaGun.getCalismaSuresi();
									pdksVardiyaGun.setSonrakiVardiya(sonrakiVardiya);
									String key1 = PdksUtil.convertToDateString(islemVardiya.getVardiyaBitZaman(), "yyyyMMdd");
									if (!key1.equals(key) && tatilGunleriMap.containsKey(key1)) {
										tatil = tatilGunleriMap.get(key1);
										if (tatil.getOrjTatil().getBasTarih().getTime() > islemVardiya.getVardiyaBitZaman().getTime())
											tatil = null;

									}
								}
								pdksVardiyaGun.setTatil(tatil);
							}
							if (pdksVardiyaGun.getVardiya() == null) {
								if (calismaModeli != null && ayinGunleri.containsKey(key)) {
									if (pdksVardiyaGun.getVardiyaDateStr().equals("20231028") || pdksVardiyaGun.getVardiyaDateStr().equals("20231021"))
										logger.debug(pdksVardiyaGun.getVardiyaDateStr());
									double calismayanSure = getCalismayanSure(calismaModeli, pdksVardiyaGun);
									if (pdksVardiyaGun.getTatil() != null && pdksVardiyaGun.getTatil().isYarimGunMu() == false) {
										calismayanSure = 0;
									}
									if (calismayanSure > 0.0d) {
										vardiyasizSure += calismayanSure;
										// logger.info(pdksVardiyaGun.getVardiyaDateStr() + " " + vardiyasizSure + " " + calismayanSure);
									}

								}
								continue;
							}

							boolean bazSureHesapla = false;
							if (ayinGunleri.containsKey(key)) {
								boolean normalGun = Boolean.FALSE;
								if (pdksVardiyaGun.getVardiya() != null) {
									if (haftaTatiliFarkHesapla && !pdksVardiyaGun.isIzinli()) {
										cal.setTime(pdksVardiyaGun.getVardiyaDate());
										boolean pazarGunu = cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY, cumartesiGunu = cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY;
										if (pdksVardiyaGun.getVardiya().isHaftaTatil()) {
											if (pdksVardiyaGun.getTatil() == null) {
												if (cumartesiGunu)
													haftaTatiliFark += calismaModeli.getHaftaSonu();
												else
													haftaTatiliFark += calismaModeli.getHaftaIci();
											} else if (pdksVardiyaGun.getTatil().isYarimGunMu())
												haftaTatiliFark += calismaModeli.getArife();
										}
										if (pazarGunu) {
											haftaTatiliFark -= calismaModeli.getHaftaIci();
										}
									}
									if (!gebeMi)
										gebeMi = suaVar || pdksVardiyaGun.getVardiya().isGebelikMi();
									if (!suaMi)
										suaMi = pdksVardiyaGun.getVardiya().getSua();
								}
								boolean ozelDurum = gebeMi || suaMi;
								double ozelDurumSaat = 0;
								if (ozelDurum) {
									if (suaMi)
										ozelDurumSaat = personelDenklestirme.getCalismaSuaSaati();
									else if (gebeMi)
										ozelDurumSaat = AylikPuantaj.getGunlukAnneCalismaSuresi();

								}

								if (!pdksVardiyaGun.isHaftaTatil() && (pdksVardiyaGun.getTatil() == null || pdksVardiyaGun.getTatil().isYarimGunMu())) {
									VardiyaGun gun = new VardiyaGun();
									gun.setDurum(!pdksVardiyaGun.isFiiliHesapla());

									gun.setVardiyaDate(pdksVardiyaGun.getVardiyaDate());
									if (pdksVardiyaGun.isFiiliHesapla())
										gun.setVardiya(normalCalismaVardiya);
									else
										gun.setVardiya(pdksVardiyaGun.getVardiya());
									Tatil tatilArife = pdksVardiyaGun.getTatil();
									gun.setTatil(tatilArife);
									Vardiya vardiya = pdksVardiyaGun.getVardiya();
									if (tatilArife != null && tatilArife.isYarimGunMu() && tatilArife.getVardiyaMap() != null && tatilArife.getVardiyaMap().containsKey(vardiya.getId())) {
										Vardiya vardiyaTatil = tatilArife.getVardiyaMap().get(vardiya.getId());
										vardiya.setArifeBaslangicTarihi(vardiyaTatil.getArifeBaslangicTarihi());
									}
									if (islemVardiyaSuresi != null && normalCalismaSuresi > islemVardiya.getNetCalismaSuresi())
										gun.setVardiya(islemVardiya);
									gun.setFiiliHesapla(Boolean.FALSE);
									double sure = 0;
									if ((pdksVardiyaGun.getVardiya() != null && pdksVardiyaGun.getVardiya().isCalisma()) && !pdksVardiyaGun.isFiiliHesapla()) {
										if (ozelDurum == false || normalCalismaSuresi != 0.0d)
											sure = getSaatToplami(puantajData, gun, yemekList, session) * (ozelDurum ? ozelDurumSaat / normalCalismaSuresi : 1.0d);
										pdksVardiyaGun.setCalismaSuresi(sure);
									} else {
										if (pdksVardiyaGun.getTatil() == null)
											sure = pdksVardiyaGun.getCalismaSuresi();

									}

									if (pdksVardiyaGun.getVardiya() != null)
										if (!raporIzni || pdksVardiyaGun.getVardiya().isCalisma()) {
											bazSureHesapla = sure > 0.0d;
											bazSure += sure;
										}

								}

								if (pdksVardiyaGun.getVardiya() != null && !(pdksVardiyaGun.isFiiliHesapla() == false && pdksVardiyaGun.getVardiyaGorev() != null && pdksVardiyaGun.getVardiyaGorev().isIstifa())) {
									double sure = getSaatToplami(puantajData, pdksVardiyaGun, yemekList, session);
									if (raporIzni) {
										pdksVardiyaGun.setFiiliHesapla(Boolean.FALSE);
										double raporSure = getCalismayanSure(calismaModeli, pdksVardiyaGun);
										if (raporSure > sure)
											sure = raporSure;
									}

									if (pdksVardiyaGun.getHaftaCalismaSuresi() > 0)
										sure = pdksVardiyaGun.getCalismaSuresi();
									boolean normalDurum = isNormalGunMu(pdksVardiyaGun);
									if (normalDurum && pdksVardiyaGun.getTatil() != null && pdksVardiyaGun.getVardiya() != null && pdksVardiyaGun.getVardiya().isHaftaTatil() && pdksVardiyaGun.getTatil().isYarimGunMu()) {
										normalDurum = false;
										double kontrolSure = getVardiyaIzinSuresi(normalCalismaSuresi, pdksVardiyaGun, personelDenklestirme, null);
										izinSuresi += kontrolSure;
									}
									if (normalDurum) {
										normalGun = Boolean.TRUE;
										if (pdksVardiyaGun.getVardiya().isCalisma() || (personelDenklestirme.getFazlaMesaiIzinKullan() != null && personelDenklestirme.getFazlaMesaiIzinKullan() && pdksVardiyaGun.getVardiya().isOffGun())) {
											if (pdksVardiyaGun.isIzinli() == false || !raporIzni)
												++calismaGunSayisi;
										}
										if (sure > 0) {

											if (raporIzni) {
												if (personelDenklestirme.isSuaDurumu())
													sure = sure * personelDenklestirme.getCalismaSuaSaati() / AylikPuantaj.getGunlukCalismaSuresi();
												++raporGunSayisi;
											}

											else {

												calisilanSure += sure;

											}
											if (pdksVardiyaGun.getResmiTatilSure() == 0)
												toplamSure += sure;
											if (!bazSureHesapla) {
												bazSure += sure;
											}

										}

									}
								}
								boolean izinHesapla = true;

								if (!normalGun) {
									boolean offIzinli = pdksVardiyaGun.isIzinli() && !(pdksVardiyaGun.getVardiya().isOffGun() || pdksVardiyaGun.getVardiya().isHaftaTatil());
									if (pdksVardiyaGun.getIzin() != null && haftaIciIzinGunTarih != null && pdksVardiyaGun.getVardiyaDate().getTime() >= haftaIciIzinGunTarih.getTime() && calismaModeli != null) {
										cal.setTime(pdksVardiyaGun.getVardiyaDate());
										int dayOffWeek = cal.get(Calendar.DAY_OF_WEEK);
										if (offIzinli == false) {
											if (pdksVardiyaGun.getVardiya().isOff()) {
												if (haftaIciIzinGunTarih != null && pdksVardiyaGun.getVardiyaDate().after(haftaIciIzinGunTarih) && calismaModeli != null) {
													if (dayOffWeek == Calendar.SATURDAY)
														offIzinli = calismaModeli.getHaftaSonu() == calismaModeli.getHaftaIci();
													else {
														if (dayOffWeek != Calendar.SUNDAY)
															offIzinli = calismaModeli.getHaftaIci() > 0;
													}
												}
											}
										} else if (dayOffWeek == Calendar.SATURDAY && pdksVardiyaGun.getVardiya().isCalisma()) {
											izinHesapla = calismaModeli.getHaftaSonu() > 0;
										}
									}

									if (izinHesapla) {

										if (offIzinli || (!(pdksVardiyaGun.getVardiya() != null && pdksVardiyaGun.getVardiya().getId().equals(offVardiya.getId())) && !pdksVardiyaGun.isHaftaTatil() && !raporIzni)) {
											if (pdksVardiyaGun.getTatil() == null || !tatilGunleriMap.containsKey(key) || (pdksVardiyaGun.getTatil() != null && pdksVardiyaGun.getTatil().isYarimGunMu())) {
												VardiyaGun gun = new VardiyaGun();
												gun.setDurum(Boolean.FALSE);
												Tatil tatil3 = pdksVardiyaGun.getTatil();
												gun.setTatil(tatil3);
												gun.setVardiyaDate(pdksVardiyaGun.getVardiyaDate());
												gun.setVardiya(normalCalismaVardiya);
												if (islemVardiyaSuresi != null && normalCalismaSuresi > islemVardiya.getNetCalismaSuresi())
													gun.setVardiya(islemVardiya);
												gun.setFiiliHesapla(Boolean.FALSE);
												PersonelIzin personelIzin = pdksVardiyaGun.getIzin();
												if (tatil2 != null && pdksVardiyaGun.isIzinli()) {
													List<HareketKGS> hareketler = pdksVardiyaGun.getHareketler();
													if (hareketler == null || hareketler.isEmpty()) {
														if (personelIzin != null)
															gun.setIzin(personelIzin);
														else
															gun.setVardiya(pdksVardiyaGun.getVardiya());
													}
												}

												// double sure = getSaatToplami(puantajData, gun, yemekList, session) ;
												if (izinSaat == null) {
													try {
														if (pdksVardiyaGun != null)
															logger.info(pdksVardiyaGun.getVardiyaKeyStr() + " " + authenticatedUser.getAdSoyad() + "\n" + authenticatedUser.getParametreJSON());
														if (calismaModeli != null)
															try {
																izinSaat = calismaModeli.getIzinSaat(pdksVardiyaGun);
															} catch (Exception ex) {
																logger.error(ex);
															}
														if (izinSaat == null)
															izinSaat = 9.0d;
														if (tatil != null && tatil.isYarimGunMu() && vardiyaIzin != null) {
															izinSaat = calismaModeli.getArife();
															if (vardiyaIzin.isIzin() == false && tatil.getArifeVardiyaYarimHesapla() != null && !tatil.getArifeVardiyaYarimHesapla())
																izinSaat = 0.0d;
															arifeGunu = true;
														}

													} catch (Exception e) {
														setExceptionLog(pdksVardiyaGun != null ? pdksVardiyaGun.getVardiyaKeyStr() : null, e);
													}
													if (izinSaat == null)
														izinSaat = normalCalismaVardiya.getNetCalismaSuresi();

												}
												double sure = izinSaat != null ? izinSaat : 9.0d;
												double kontrolSure = 0;

												if (sure > 0 || pdksVardiyaGun.getIzin() != null) {
													Tatil tatilIzin = pdksVardiyaGun.getTatil();
													pdksVardiyaGun.setTatil(tatilGunleriMap.get(pdksVardiyaGun.getVardiyaDateStr()));
													kontrolSure = getVardiyaIzinSuresi(izinTarihKontrolTarihi == null ? sure : 0.0d, pdksVardiyaGun, personelDenklestirme, izinTarihKontrolTarihi);
													pdksVardiyaGun.setTatil(tatilIzin);
													if (pdksVardiyaGun.isIzinli() && izinSaat != null && kontrolSure > izinSaat)
														kontrolSure = izinSaat;
													izinSuresi += kontrolSure;

												}
												if (pdksVardiyaGun.isIzinli()) {
													if (izinSaat != null) {
														sure = izinSaat;
														kontrolSure = izinSaat;
													}
													haftalikIzinSuresi += kontrolSure;
												}

												if (pdksVardiyaGun.getVardiya() != null && pdksVardiyaGun.isIzinli() == false && !pdksVardiyaGun.getVardiya().isCalisma()) {
													calisilmayanSuresi += sure;
												}

												gun = null;

											}
										}
									}
								}
								if (pdksVardiyaGun.getCalismaSuresi() > 0) {
									double normalSure = pdksVardiyaGun.getCalismaSuresi() - (pdksVardiyaGun.getHaftaCalismaSuresi() + pdksVardiyaGun.getResmiTatilSure());
									if (normalSure > mesaiMaxSure && pdksVardiyaGun.getVardiya().isMesaiOdenir()) {
										ucretiOdenenMesaiSure += normalSure - mesaiMaxSure;
										// pdksVardiyaGun.addCalismaSuresi(fazlaMesaiMaxSure
										// - normalSure);
									}

								}

							} else if (hafta == 1 && !isNormalGunMu(pdksVardiyaGun)) {

								if (!pdksVardiyaGun.isHaftaTatil() && !raporIzni) {
									if (pdksVardiyaGun.getTatil() == null || pdksVardiyaGun.getTatil().isYarimGunMu()) {
										VardiyaGun gun = new VardiyaGun();
										gun.setDurum(Boolean.FALSE);
										gun.setTatil(pdksVardiyaGun.getTatil());
										gun.setVardiyaDate(pdksVardiyaGun.getVardiyaDate());
										gun.setVardiya(normalCalismaVardiya);
										if (islemVardiyaSuresi != null && normalCalismaSuresi > islemVardiya.getNetCalismaSuresi())
											gun.setVardiya(islemVardiya);
										gun.setFiiliHesapla(Boolean.FALSE);
										double sure = getSaatToplami(puantajData, gun, yemekList, session);
										if (pdksVardiyaGun.getIzin() != null) {
											haftalikIzinSuresi += sure;
										}
										if (pdksVardiyaGun.getVardiya() != null && pdksVardiyaGun.isIzinli() == false && !pdksVardiyaGun.getVardiya().isCalisma()) {
											// logger.info(pdksVardiyaGun.getVardiyaKeyStr());
											calisilmayanSuresi += sure;
										}
										gun = null;

									}

								}

							}
							if (tatil != null)
								pdksVardiyaGun.setTatil(null);
							if (arifeGunu && pdksVardiyaGun.isFiiliHesapla() == false) {

								// TODO Arife'den sonra planlar için
								Tatil tatilArife = pdksVardiyaGun.getTatil();
								if (tatilArife.getArifeVardiyaYarimHesapla() != null && tatilArife.getArifeVardiyaYarimHesapla()) {
									double calSure = pdksVardiyaGun.getVardiya().getNetCalismaSuresi() - pdksVardiyaGun.getResmiTatilSure();
									double arifeSure = calSure > izinSaat ? calSure : izinSaat;
									double yarimGun = pdksVardiyaGun.isIzinli() ? izinSaat : arifeSure;
									if (tatilArife.getArifeSonraVardiyaDenklestirmeVar() != null && tatilArife.getArifeSonraVardiyaDenklestirmeVar()) {
										Vardiya vardiya = pdksVardiyaGun.getIslemVardiya();
										if (tatilArife.getVardiyaMap() != null && tatilArife.getVardiyaMap().containsKey(vardiya.getId())) {
											Vardiya vardiyaTatil = tatilArife.getVardiyaMap().get(vardiya.getId());
											vardiya.setArifeBaslangicTarihi(vardiyaTatil.getArifeBaslangicTarihi());
											if (vardiya.getArifeBaslangicTarihi() != null && vardiya.getArifeBaslangicTarihi().getTime() <= vardiya.getVardiyaBasZaman().getTime()) {
												arifeSure = 0.0d;
												yarimGun = 0.0d;
											}
										}
									}
									double kontrolSure = getVardiyaIzinSuresi(yarimGun - arifeSure, pdksVardiyaGun, personelDenklestirme, null);
									izinSuresi += kontrolSure;
								}
							}

							if (ayinGunleri.containsKey(key) && pdksVardiyaGun.getResmiTatilSure() != 0) {
								resmiTatilSure += pdksVardiyaGun.getResmiTatilSure();
								if (pdksVardiyaGun.getIslemVardiya().isCalisma() == false && pdksVardiyaGun.getFazlaMesailer() != null) {
									addBayramCalismaSuresi(pdksVardiyaGun);
								}
								if (pdksVardiyaGun.getGecenAyResmiTatilSure() > 0 && pdksVardiyaGun.getResmiTatilSure() > pdksVardiyaGun.getGecenAyResmiTatilSure()) {
									double bugunResmiTatilSure = pdksVardiyaGun.getResmiTatilSure() - pdksVardiyaGun.getGecenAyResmiTatilSure();
									if (pdksVardiyaGun.getCalismaSuresi() > bugunResmiTatilSure)
										toplamSure += pdksVardiyaGun.getCalismaSuresi() - bugunResmiTatilSure;
								}

							}
							pdksVardiyaGun.setTatil(tatilOrj);
							if (pdksVardiyaGun.getHaftaTatilDigerSure() > 0)
								haftaTatilDigerSure += pdksVardiyaGun.getHaftaTatilDigerSure();
							if (izinToplam < izinSuresi) {
								izinToplam = izinSuresi;
								if (pdksVardiyaGun.getIzin() != null)
									logger.debug(key + " " + izinSuresi);
							}
							if (izinSuresi != 0.0d)
								logger.debug(key + " " + izinSuresi);
							if (pdksVardiyaGun.getResmiTatilSure() > 0.0d) {
								toplamSure += pdksVardiyaGun.getCalismaSuresi() - (pdksVardiyaGun.getResmiTatilSure() - pdksVardiyaGun.getGecenAyResmiTatilSure());
							}

							if (pdksVardiyaGun.isAyinGunu() && pdksVardiyaGun.getCalismaSuresi() > 0.0d || toplamSure > 0.0d)
								logger.debug(key + " " + toplamSure + " " + pdksVardiyaGun.getCalismaSuresi());
						}

						if (resmiTatilDepartmanlari == null || (departmanKodu != null && resmiTatilDepartmanlari.contains(departmanKodu)))
							if (!resmiTatilEkle && !resmiTatilVardiyaEkle && (resmiTatilSure != 0 || gecenAyResmiTatilSure != 0))
								resmiTatilVardiyaEkle = vardiyaAksamSabahVarMi(haftaVardiyaGunler, vardiyaAksamSabahAdlari);

						if (vardiyasizSure > 0) {
							if (vardiyasizSure > gunduzCalismaSaat)
								vardiyasizSure = gunduzCalismaSaat;
							izinSuresi += vardiyasizSure;
							// logger.info(izinSuresi + " " + vardiyasizSure);
						}
						if (haftalikIzinSuresi > gunduzCalismaSaat) {
							// logger.info(izinSuresi + " " +
							// haftalikIzinSuresi);
							izinSuresi -= haftalikIzinSuresi - gunduzCalismaSaat;

						}

						if (raporGunSayisi > 0) {
							if (calismaGunSayisi > 4) {
								if (calisilanSure > bazSure)
									toplamSure = calisilanSure;
								else
									toplamSure = bazSure;
							} else if (calismaGunSayisi == 4) {
								if (bazSure < gunduzCalismaSaat || toplamSure < gunduzCalismaSaat)
									bazSure = gunduzCalismaSaat;
								toplamSure = bazSure;
							}
						}
						double sureToplam = toplamSure - calisilmayanSuresi;
						if (sureToplam != 0.0d) {
							planlanSure += sureToplam;
						}

						toplamCalismaGunSayisi += calismaGunSayisi;

						if (loginUser.isAdmin() && toplamSure + izinSuresi != 0) {
							logger.debug(personel.getPdksSicilNo() + " --> " + hafta + " : " + raporGunSayisi + " " + toplamSure + " " + calismaGunSayisi + " " + izinSuresi + " " + haftaTatiliFark);
							logger.debug(personel.getPdksSicilNo() + "   " + planlanSure + " " + toplamSure + " " + calisilmayanSuresi);
						}

						logger.debug(izinSuresi + " " + vardiyasizSure);

					}

					izinSuresi += puantajData.getSaatlikIzinSuresi();
					if (haftaTatiliFark != 0)
						izinSuresi += calismaModeli.getHaftaIci();
					puantajData.setIzinSuresi(izinSuresi);
					if (filliHesapla == false) {
						if (puantajData.getResmiTatilToplami() > 0)
							resmiTatilSure = puantajData.getResmiTatilToplami();
						else
							puantajData.setResmiTatilToplami(resmiTatilSure);
					}

					double saatToplami = planlanSure + haftaTatilDigerSure - puantajData.getHaftaCalismaSuresi() + (resmiTatilEkle || resmiTatilVardiyaEkle ? resmiTatilSure - gecenAyResmiTatilSure : 0.0d);

					puantajData.setSaatToplami(saatToplami);
					puantajData.setUcretiOdenenMesaiSure(ucretiOdenenMesaiSure);
					puantajData.planSureHesapla(tatilGunleriMap);

					int yarimYuvarla = puantajData.getYarimYuvarla();
					if (toplamCalismaGunSayisi + offGunSayisi == 0 && puantajData.getSaatToplami() == 0.0d) {
						if (puantajData.getPlanlananSure() != 0.0d) {
							puantajData.setPlanlananSure(0.0d);
						}
					}

					// if (filliHesapla == false && puantajData.getResmiTatilToplami() > 0.0d)
					// puantajData.setSaatToplami(puantajData.getSaatToplami() - puantajData.getResmiTatilToplami());
					double hesaplananBuAySure = puantajData.getAylikFazlaMesai(), gecenAydevredenSure = puantajData.getGecenAyFazlaMesai(loginUser);
					boolean fazlaMesaiOde = puantajData.getPersonelDenklestirme().getFazlaMesaiOde() != null && puantajData.getPersonelDenklestirme().getFazlaMesaiOde();
					if (!fazlaMesaiOde) {
						try {
							if (puantajData.getPersonelDenklestirme() != null && puantajData.getPersonelDenklestirme().getDenklestirmeAy() != null) {
								fazlaMesaiOde = PdksUtil.tarihKarsilastirNumeric(puantajData.getSonGun(), personel.getSskCikisTarihi()) != -1;
							}
						} catch (Exception e) {

						}

					}
					PersonelDenklestirme hesaplananDenklestirme = puantajData.getPersonelDenklestirme(fazlaMesaiOde, hesaplananBuAySure, gecenAydevredenSure);
					puantajData.setFazlaMesaiSure(PdksUtil.setSureDoubleTypeRounded((hesaplananDenklestirme.getOdenenSure() > 0 ? hesaplananDenklestirme.getOdenenSure() : 0) + ucretiOdenenMesaiSure, yarimYuvarla));
					puantajData.setHesaplananSure(hesaplananDenklestirme.getHesaplananSure());
					if (loginUser.isAdmin()) {
						try {
							if (denklestirmeAy == null)
								denklestirmeAy = puantajData.getPersonelDenklestirme().getDenklestirmeAy();
						} catch (Exception e) {

						}

					}
					puantajData.setEksikCalismaSure(0.0d);
					if (hesaplananDenklestirme.getDevredenSure() != 0.0d)
						hesaplananDenklestirme.setDevredenSure(PdksUtil.setSureDoubleTypeRounded(hesaplananDenklestirme.getDevredenSure(), yarimYuvarla));

					if (calismaModeli.isSaatlikOdeme()) {
						if (hesaplananDenklestirme.getDevredenSure() < 0.0d) {

							puantajData.setEksikCalismaSure(saatToplami);

						} else if (hesaplananDenklestirme.getDevredenSure() > 0.0d) {
							Double sure = puantajData.getFazlaMesaiSure() + hesaplananDenklestirme.getDevredenSure();
							puantajData.setFazlaMesaiSure(sure);
						}
						hesaplananDenklestirme.setDevredenSure(0.0d);
					}

					puantajData.setDevredenSure(PdksUtil.setSureDoubleTypeRounded(hesaplananDenklestirme.getDevredenSure(), yarimYuvarla));

					if (puantajData.getDevredenSure() > 0.0d && !personelCalisiyor) {
						double devredenSure = puantajData.getDevredenSure();
						puantajData.setFazlaMesaiSure(puantajData.getFazlaMesaiSure() + devredenSure);
						puantajData.setDevredenSure(0.0d);
					}
					if (puantajData.getPlanlananSure() == 0.0d && puantajData.getFazlaMesaiSure() > 0.0d && personelCalisiyor) {

						// puantajData.setDevredenSure(puantajData.getDevredenSure() + puantajData.getFazlaMesaiSure());
						// puantajData.setFazlaMesaiSure(0.0d);

					}
					if (!calismaModeli.isFazlaMesaiVarMi()) {
						puantajData.setHaftaCalismaSuresi(0.0d);
						puantajData.setFazlaMesaiSure(0.0d);
						puantajData.setUcretiOdenenMesaiSure(0.0d);
						puantajData.setAksamVardiyaSaatSayisi(0.0d);
						puantajData.setAksamVardiyaSayisi(0);
						puantajData.setResmiTatilToplami(0.0d);
						puantajData.setDevredenSure(0.0d);
					}
					if (personelDenklestirme.getFazlaMesaiIzinKullan() && personel.isCalisiyorGun(puantajData.getSonGun())) {
						// TODO KISMI UCRET_ODE
						double aylikNetFazlaMesai = new BigDecimal(puantajData.getDevredenSure() + puantajData.getFazlaMesaiSure()).doubleValue();
						boolean otomatikFazlaCalismaOnaylansin = personelDenklestirme.getCalismaModeliAy() != null ? personelDenklestirme.getCalismaModeliAy().isGunMaxCalismaOdenir() : true;
						Double ucretiOdenenMesaiSureAylik = otomatikFazlaCalismaOnaylansin ? puantajData.getUcretiOdenenMesaiSure() : 0.0d;
						aylikNetFazlaMesai = aylikNetFazlaMesai - ucretiOdenenMesaiSureAylik;
						if (personelDenklestirme.isFazlaMesaiIzinKullanacak() && personelDenklestirme.getKismiOdemeSure() != null && personelDenklestirme.getKismiOdemeSure() > 0 && personelDenklestirme.getKismiOdemeSure() <= aylikNetFazlaMesai) {
							BigDecimal devredenSure = new BigDecimal(aylikNetFazlaMesai - personelDenklestirme.getKismiOdemeSure());
							puantajData.setDevredenSure(devredenSure.doubleValue());
							puantajData.setFazlaMesaiSure(personelDenklestirme.getKismiOdemeSure() + ucretiOdenenMesaiSureAylik);
							puantajData.setUcretiOdenenMesaiSure(0.0d);
						} else {
							puantajData.setDevredenSure(aylikNetFazlaMesai);
							puantajData.setFazlaMesaiSure(ucretiOdenenMesaiSureAylik);
							puantajData.setUcretiOdenenMesaiSure(0.0);
						}

					}
					if (((loginUser.isIK() || loginUser.isAdmin()) && personelDenklestirme.getDevredenSure() == null) || (kaydet && !personelDenklestirme.isKapandi(loginUser))) {
						if (session == null)
							session = PdksUtil.getSessionUser(entityManager, loginUser);
						if (puantajData.getPersonelDenklestirme().getId() != null) {
							HashMap fields = new HashMap();
							fields = new HashMap();
							fields.put("id", puantajData.getPersonelDenklestirme().getId());
							try {
								fields.put(PdksEntityController.MAP_KEY_SESSION, session);
								personelDenklestirme = (PersonelDenklestirme) pdksEntityController.getObjectByInnerObject(fields, PersonelDenklestirme.class);

							} catch (Exception ex) {
								logger.error(ex);
							}
							if (personelDenklestirme != null && kaydet) {
								personelDenklestirme.setGuncellendi(Boolean.FALSE);
								if (personelDenklestirme.getCalismaModeliAy() == null || personelDenklestirme.getCalismaModeli().getToplamGunGuncelle().equals(Boolean.FALSE))
									personelDenklestirme.setPlanlanSure(planlanSure);
								// personelDenklestirme.setDevredenSure(puantajData.getDevredenSure());
								if (personelDenklestirme.isGuncellendi())
									pdksEntityController.saveOrUpdate(session, entityManager, personelDenklestirme);
								puantajData.setPersonelDenklestirme(personelDenklestirme);
							}
						}

					}

					ayinGunleri = null;
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}
				normalCalismaVardiya = null;
				// tatilGunleri = null;
			}

		} catch (Exception exx) {
			logger.error(exx);
			exx.printStackTrace();
		}
		return personelDenklestirme;

	}

	/**
	 * @param mesaj
	 * @param es
	 */
	public void setExceptionLog(String mesaj, Exception es) {
		StringBuffer mesajSb = new StringBuffer();
		if (authenticatedUser != null) {
			mesajSb.append(authenticatedUser.getAdSoyad());
			if (PdksUtil.hasStringValue(authenticatedUser.getCalistigiSayfa()))
				mesajSb.append(" --> " + authenticatedUser.getCalistigiSayfa() + " : ");
			if (PdksUtil.hasStringValue(mesaj))
				mesajSb.append(" " + mesaj);
			if (PdksUtil.hasStringValue(authenticatedUser.getParametreJSON()))
				mesajSb.append("\n" + authenticatedUser.getParametreJSON());
		} else if (PdksUtil.hasStringValue(mesaj))
			mesajSb.append(" " + mesaj);
		mesaj = mesajSb.toString();
		if (es != null) {
			logger.error((PdksUtil.hasStringValue(mesaj) ? mesaj + "\n" : "") + es);
			es.printStackTrace();
		} else if (PdksUtil.hasStringValue(mesaj))
			logger.error(mesaj);
		mesajSb = null;

	}

	/**
	 * @param kisaAdi
	 * @param session
	 * @return
	 */
	public Vardiya getNormalCalismaVardiya(String kisaAdi, Session session) {
		if (!PdksUtil.hasStringValue(kisaAdi))
			kisaAdi = "G";
		HashMap fields = new HashMap();
		fields.put("kisaAdi", kisaAdi);
		fields.put("durum", Boolean.TRUE);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		Vardiya normalCalisma = (Vardiya) pdksEntityController.getObjectByInnerObject(fields, Vardiya.class);
		if (normalCalisma == null) {
			normalCalisma = new Vardiya();
			normalCalisma.setTipi(String.valueOf(Vardiya.TIPI_CALISMA));
			normalCalisma.setBasSaat((short) 8);
			normalCalisma.setBasDakika((short) 30);
			normalCalisma.setBitSaat((short) 18);
			normalCalisma.setBitDakika((short) 0);
			normalCalisma.setYemekSuresi(30);
		}
		return normalCalisma;
	}

	/**
	 * @param gelenSure
	 * @param pdksVardiyaGun
	 * @param pdksVardiyaGun
	 * @return personelDenklestirme
	 * @return kontrolTarihi
	 */
	public double getVardiyaIzinSuresi(Double gelenSure, VardiyaGun pdksVardiyaGun, PersonelDenklestirme personelDenklestirme, Date kontrolTarihi) {
		double sure = gelenSure != null ? gelenSure.doubleValue() : 0.0d;
		try {
			if (pdksVardiyaGun.getVardiyaDateStr().endsWith("18"))
				logger.debug("");
			if (kontrolTarihi != null && pdksVardiyaGun.getIzin() != null && personelDenklestirme != null) {
				IzinTipi izinTipi = pdksVardiyaGun.getIzin().getIzinTipi();
				boolean raporIzni = getParameterKey("raporIzniKontrolEt").equals("1") && izinTipi.isRaporIzin();
				if (izinTipi != null && !raporIzni) {
					// TODO İzin
					CalismaModeli calismaModeli = personelDenklestirme.getCalismaModeli();
					if (calismaModeli != null) {
						int haftaGun = PdksUtil.getDateField(pdksVardiyaGun.getVardiyaDate(), Calendar.DAY_OF_WEEK);
						switch (haftaGun) {
						case Calendar.SATURDAY:
							if (calismaModeli.getHaftaSonu() <= 0.0d)
								sure = 0.0d;
							else
								sure = calismaModeli.getIzinhaftaSonu();
							break;
						case Calendar.SUNDAY:
							sure = 0.0d;
							break;

						default:
							if (calismaModeli.getIzin() > 0.0d)
								sure = calismaModeli.getIzin();
							break;
						}

						Tatil tatil = pdksVardiyaGun.getTatil();
						if (tatil != null) {
							if (tatil.isYarimGunMu() == false)
								sure = 0.0d;
						}
					}

					if (gelenSure.doubleValue() != sure)
						logger.debug(pdksVardiyaGun.getVardiyaKeyStr());
				}
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		return sure;
	}

	/**
	 * @param calismaModeli
	 * @param pdksVardiyaGun
	 * @return
	 */
	public double getCalismayanSure(CalismaModeli calismaModeli, VardiyaGun pdksVardiyaGun) {
		Calendar cal = Calendar.getInstance();
		Date vardiyaDate = pdksVardiyaGun.getVardiyaDate();
		cal.setTime(vardiyaDate);
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		double calismayanSure = 0.0d;
		if (dayOfWeek != Calendar.SUNDAY) {
			Tatil tatil = pdksVardiyaGun.getTatil();
			if (tatil == null) {
				// boolean sua = pdksVardiyaGun.getVardiya().getSua() != null ? pdksVardiyaGun.getVardiya().getSua() : null;
				calismayanSure = dayOfWeek != Calendar.SATURDAY ? calismaModeli.getHaftaIci() : calismaModeli.getHaftaSonu();
			} else if (tatil.isYarimGunMu()) {
				if (PdksUtil.tarihKarsilastirNumeric(vardiyaDate, tatil.getBasTarih()) == 0) {
					if (calismaModeli.getHaftaSonu() > 0 || dayOfWeek != Calendar.SATURDAY)
						calismayanSure = calismaModeli.getArife();
				}
			}
		}
		return calismayanSure;
	}

	/**
	 * @param aylikPuantaj
	 * @param vardiyaGun
	 * @param yemekList
	 * @param session
	 * @return
	 */
	protected double gunlukHareketSureHesapla(AylikPuantaj aylikPuantaj, VardiyaGun vardiyaGun, List<YemekIzin> yemekList, Session session) {
		double sure;
		sure = 0;
		if (vardiyaGun.getIzin() == null && vardiyaGun.getIzinler() != null) {
			for (PersonelIzin personelIzin : vardiyaGun.getIzinler()) {
				if (personelIzin.getHesapTipi() != null && personelIzin.getHesapTipi().equals(PersonelIzin.HESAP_TIPI_SAAT)) {
					IzinTipi izinTipi = personelIzin.getIzinTipi();
					if (izinTipi.isEkleCGS())
						sure += personelIzin.getIzinSuresi();
					else if (aylikPuantaj != null) {
						if (izinTipi.isCikarCGS())
							aylikPuantaj.addSaatlikIzinSuresi(sure);
					}
				}

			}
		}
		if (vardiyaGun.getFazlaMesailer() != null) {
			for (PersonelFazlaMesai personelFazlaMesai : vardiyaGun.getFazlaMesailer()) {
				if (personelFazlaMesai.isOnaylandi() && !personelFazlaMesai.isBayram())
					sure += personelFazlaMesai.getFazlaMesaiSaati();
			}
		}
		List<HareketKGS> girisHareketleri = vardiyaGun.getGirisHareketleri(), cikisHareketleri = vardiyaGun.getCikisHareketleri();
		if (girisHareketleri != null && cikisHareketleri != null && girisHareketleri.size() == cikisHareketleri.size()) {
			double toplamYemekSuresi = 0;
			for (int i = 0; i < girisHareketleri.size(); i++) {
				HareketKGS girisHareket = girisHareketleri.get(i), cikisHareket = cikisHareketleri.get(i);
				double sureAralik = PdksUtil.getSaatFarki(cikisHareket.getZaman(), girisHareket.getZaman());
				double yemeksizSure = getSaatSure(girisHareket.getZaman(), cikisHareket.getZaman(), yemekList, vardiyaGun, session);
				toplamYemekSuresi += sureAralik - yemeksizSure;
				if (!girisHareket.isTatil())
					sure += yemeksizSure;
			}
			if (toplamYemekSuresi > 0 && vardiyaGun.getIslemVardiya() != null && vardiyaGun.getIslemVardiya().getYemekSuresi() != null) {
				Double yemekSuresi = vardiyaGun.getIslemVardiya().getYemekSuresi().doubleValue() / 60.0d;
				if (toplamYemekSuresi > yemekSuresi)
					sure = sure - yemekSuresi + toplamYemekSuresi;
			}
		}
		return sure;
	}

	/**
	 * @param vardiyaGun
	 * @return
	 */
	private boolean isNormalGunMu(VardiyaGun vardiyaGun) {
		boolean raporIzni = getVardiyaIzniEkle(vardiyaGun);

		boolean normalGun = (vardiyaGun.isIzinli() == false) || (raporIzni || (vardiyaGun.isSutIzni()));

		return normalGun;
	}

	/**
	 * @param vardiyaGun
	 * @return
	 */
	private boolean getVardiyaIzniEkle(VardiyaGun vardiyaGun) {
		boolean raporIzni = vardiyaGun.isEkleIzni();
		try {
			if (raporIzni)
				raporIzni = vardiyaGun.getPersonel().getSirket().getDepartman().isAdminMi();

		} catch (Exception e) {

		}
		return raporIzni;
	}

	/**
	 * @param fiiliHesapla
	 * @param vardiyaGunMap
	 * @param vardiyaMap
	 * @param sablonAylikPuantaj
	 * @param aylikPuantaj
	 */
	public void puantajHaftalikPlanOlustur(boolean fiiliHesapla, TreeMap<String, VardiyaGun> vardiyaGunMap, TreeMap<String, VardiyaGun> vardiyaMap, AylikPuantaj sablonAylikPuantaj, AylikPuantaj aylikPuantaj) {
		List<VardiyaHafta> vardiyaHaftaList = new ArrayList<VardiyaHafta>();
		Personel personel = aylikPuantaj.getPdksPersonel();
		aylikPuantaj.setSablonAylikPuantaj(sablonAylikPuantaj);
		List<VardiyaGun> puantajVardiyaGunleri = new ArrayList();
		aylikPuantaj.setVardiyaPlan(new VardiyaPlan(personel));
		VardiyaPlan vardiyaPlan = aylikPuantaj.getVardiyaPlan();
		vardiyaPlan.setVardiyaHaftaList(vardiyaHaftaList);
		aylikPuantaj.setVardiyaHaftaList(vardiyaHaftaList);
		for (Iterator iterator = sablonAylikPuantaj.getVardiyaHaftaList().iterator(); iterator.hasNext();) {
			VardiyaHafta vardiyaHaftaMaster = (VardiyaHafta) iterator.next();
			VardiyaHafta vardiyaHafta = new VardiyaHafta();
			vardiyaHafta.setVardiyaPlan(vardiyaPlan);
			vardiyaHafta.setPersonel(personel);
			vardiyaHafta.setBasTarih(vardiyaHaftaMaster.getBasTarih());
			vardiyaHafta.setBitTarih(vardiyaHaftaMaster.getBitTarih());
			vardiyaHaftaList.add(vardiyaHafta);
			List<VardiyaGun> vardiyaHaftaGunleri = new ArrayList<VardiyaGun>();
			vardiyaHafta.setVardiyaGunler(vardiyaHaftaGunleri);
			for (Iterator iterator2 = vardiyaHaftaMaster.getVardiyaGunler().iterator(); iterator2.hasNext();) {
				VardiyaGun vardiyaGunSablon = (VardiyaGun) iterator2.next();
				VardiyaGun vardiyaGun = new VardiyaGun(personel, null, vardiyaGunSablon.getVardiyaDate());
				String vardiyaKey = vardiyaGun.getVardiyaKeyStr();
				if (vardiyaMap.containsKey(vardiyaKey))
					vardiyaGun = vardiyaMap.get(vardiyaKey);
				vardiyaHaftaGunleri.add(vardiyaGun);
				vardiyaGun.setAyinGunu(vardiyaGunSablon.isAyinGunu());
				vardiyaGun.setTatil(vardiyaGunSablon.getTatil());
				vardiyaGun.setFiiliHesapla(fiiliHesapla);
				puantajVardiyaGunleri.add(vardiyaGun);

				if (vardiyaGunMap != null)
					vardiyaGunMap.put(vardiyaGun.getVardiyaDateStr(), vardiyaGun);

			}

		}
		aylikPuantaj.setVardiyalar(puantajVardiyaGunleri);
	}

	/**
	 * @param vardiyaGun
	 */
	private void addBayramCalismaSuresi(VardiyaGun vardiyaGun) {
		List<PersonelFazlaMesai> fazlaMesailer = vardiyaGun.getFazlaMesailer();
		if (fazlaMesailer != null && !fazlaMesailer.isEmpty()) {
			for (PersonelFazlaMesai fazlaMesai : fazlaMesailer) {
				if (fazlaMesai.isBayram() && fazlaMesai.isOnaylandi()) {
					vardiyaGun.addBayramCalismaSuresi(fazlaMesai.getFazlaMesaiSaati());
				}
			}
		}

	}

	/**
	 * @return
	 */
	public Boolean getGebelikGuncelle() {
		return AylikPuantaj.getGebelikGuncelle();

	}

	/**
	 * @param denklestirmeAy
	 * @param session
	 * @return
	 */
	@Transactional
	public double getYemekMolasiYuzdesi(DenklestirmeAy denklestirmeAy, Session session) {
		Double yuzde = denklestirmeAy != null ? denklestirmeAy.getYemekMolasiYuzdesi() : null;
		if (yuzde == null)
			try {
				String yemekMolasiYuzdesiStr = getParameterKey("yemekMolasiYuzdesi");
				if (PdksUtil.hasStringValue(yemekMolasiYuzdesiStr))
					yuzde = Double.parseDouble(yemekMolasiYuzdesiStr);
			} catch (Exception e) {
			}
		if (yuzde != null) {
			yuzde = yuzde * 0.01d;
		}
		if (yuzde == null || yuzde <= 0.0d)
			yuzde = 0.75d;
		if (yuzde > 1.0d)
			yuzde = 1.0d;
		Double yemekMolasiYuzdesi = yuzde * 100.0d;
		if (session != null && denklestirmeAy != null && denklestirmeAy.getYemekMolasiYuzdesi() == null) {
			try {
				denklestirmeAy.setYemekMolasiYuzdesi(yemekMolasiYuzdesi);
				pdksEntityController.saveOrUpdate(session, entityManager, denklestirmeAy);
				session.flush();
			} catch (Exception e) {
			}
		}
		return yuzde.doubleValue();

	}

	/**
	 * @return
	 */
	public Tanim otomatikFazlaMesaiOnayTanimGetir(Session session) {
		Tanim tanim = null;
		String kodu = getParameterKey("otomatikFazlaMesaiOnay");
		if (!PdksUtil.hasStringValue(kodu))
			kodu = "onayYok";
		if (PdksUtil.hasStringValue(kodu)) {
			HashMap fields = new HashMap();
			fields.put("tipi", Tanim.TIPI_FAZLA_MESAI_NEDEN);
			fields.put("kodu", kodu);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			tanim = (Tanim) pdksEntityController.getObjectByInnerObject(fields, Tanim.class);
		}
		return tanim;
	}

	/**
	 * @param dataDenkMap
	 * @param session
	 * @return
	 */
	@Transactional
	public VardiyaGun personelVardiyaDenklestir(LinkedHashMap<String, Object> dataDenkMap, Session session) {
		HashMap<String, KapiView> manuelKapiMap = dataDenkMap.containsKey("manuelKapiMap") ? (HashMap<String, KapiView>) dataDenkMap.get("manuelKapiMap") : null;
		Tanim neden = dataDenkMap.containsKey("neden") ? (Tanim) dataDenkMap.get("neden") : null;
		Tanim fazlaMesaiOnayDurum = dataDenkMap.containsKey("fazlaMesaiOnayDurum") ? (Tanim) dataDenkMap.get("fazlaMesaiOnayDurum") : null;
		User loginUser = dataDenkMap.containsKey("loginUser") ? (User) dataDenkMap.get("loginUser") : authenticatedUser;
		User sistemUser = dataDenkMap.containsKey("sistemUser") ? (User) dataDenkMap.get("sistemUser") : null;
		TreeMap<String, Boolean> gunMap = dataDenkMap.containsKey("gunMap") ? (TreeMap<String, Boolean>) dataDenkMap.get("gunMap") : null;
		KapiView girisKapi = dataDenkMap.containsKey("girisView") ? (KapiView) dataDenkMap.get("girisView") : null;
		HashMap<Long, Double> vardiyaNetCalismaSuresiMap = dataDenkMap.containsKey("vardiyaNetCalismaSuresiMap") ? (HashMap<Long, Double>) dataDenkMap.get("vardiyaNetCalismaSuresiMap") : null;
		TreeMap<String, Tatil> tatilGunleriMap = dataDenkMap.containsKey("tatilGunleriMap") ? (TreeMap<String, Tatil>) dataDenkMap.get("tatilGunleriMap") : null;
		List<YemekIzin> yemekGenelList = dataDenkMap.containsKey("yemekList") ? (List<YemekIzin>) dataDenkMap.get("yemekList") : null;
		PersonelDenklestirmeTasiyici denklestirmeTasiyici = dataDenkMap.containsKey("personelDenklestirme") ? (PersonelDenklestirmeTasiyici) dataDenkMap.get("personelDenklestirme") : null;
		Boolean updateSatus = dataDenkMap.containsKey("updateSatus") ? (Boolean) dataDenkMap.get("updateSatus") : Boolean.FALSE;
		Boolean fiiliHesapla = dataDenkMap.containsKey("fiiliHesapla") ? (Boolean) dataDenkMap.get("updateSatus") : Boolean.TRUE;

		double resmiTatilMesai = 0;
		VardiyaGun sonVardiyaGun = denklestirmeTasiyici.getSonVardiyaGun();
		Double yemekMolasiYuzdesi = getYemekMolasiYuzdesi(denklestirmeTasiyici.getDenklestirmeAy(), session);
		DenklestirmeAy denklestirmeAy = denklestirmeTasiyici.getDenklestirmeAy();
		Date ilkGun = PdksUtil.convertToJavaDate(String.valueOf(denklestirmeAy.getYil() * 100 + denklestirmeAy.getAy()) + "01", "yyyyMMdd");

		if (denklestirmeTasiyici.getVardiyalar() != null) {
			List<VardiyaGun> vardiyalar = denklestirmeTasiyici.getVardiyalar();
			if (vardiyalar != null)
				try {
					if (denklestirmeTasiyici.getDenklestirmeAy() != null && (denklestirmeTasiyici.getDenklestirmeAy().isDurumu())) {
						LinkedHashMap<String, Object> dataGirisCikisMap = new LinkedHashMap<String, Object>();
						dataGirisCikisMap.put("manuelKapiMap", manuelKapiMap);
						dataGirisCikisMap.put("neden", neden);
						dataGirisCikisMap.put("sistemUser", sistemUser);
						dataGirisCikisMap.put("vardiyalar", vardiyalar);
						dataGirisCikisMap.put("hareketKaydet", false);
						dataGirisCikisMap.put("oncekiVardiyaGun", denklestirmeTasiyici.getOncekiVardiyaGun());
						VardiyaGun oncekiVardiyaGun = addManuelGirisCikisHareketler(mapBosVeriSil(dataGirisCikisMap, "addManuelGirisCikisHareketler"), session);
						denklestirmeTasiyici.setOncekiVardiyaGun(oncekiVardiyaGun);
						dataGirisCikisMap = null;
					}
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}

			TreeMap<String, VardiyaGun> haftaTatilMap = new TreeMap<String, VardiyaGun>();
			String haftaTatilDurum = getParameterKey("haftaTatilDurum");
			CalismaModeli calismaModeli = denklestirmeTasiyici.getCalismaModeli();
			if (calismaModeli == null) {
				if (denklestirmeTasiyici.getPersonel() != null)
					calismaModeli = denklestirmeTasiyici.getPersonel().getCalismaModeli();
				if (calismaModeli == null)
					calismaModeli = new CalismaModeli();
			}

			if (haftaTatilDurum.equals("1") && calismaModeli.getHaftaTatilMesaiOde()) {
				for (VardiyaGun vardiyaGun : vardiyalar) {
					if (vardiyaGun.getVardiya() != null) {
						String gun = vardiyaGun.getVardiyaDateStr();
						vardiyaGun.setCalismaModeli(calismaModeli);
						if (vardiyaGun.isHaftaTatil() && (!tatilGunleriMap.containsKey(gun) || tatilGunleriMap.get(gun).isYarimGunMu())) {
							String key1 = (vardiyaGun.getPersonel() != null ? vardiyaGun.getPersonel().getSicilNo() : "") + "_" + gun;
							haftaTatilMap.put(key1, vardiyaGun);
						}

					}

				}
			}

			denklestirmeTasiyici.setToplamCalisilacakZaman(0);
			denklestirmeTasiyici.setToplamCalisilanZaman(0);
			denklestirmeTasiyici.setToplamRaporIzni(0);
			denklestirmeTasiyici.setCheckBoxDurum(Boolean.TRUE);
			double maxSure = 0;
			int gunSayisi = 0;
			double maxSuresi = 0;
			boolean eksikCalisma = !denklestirmeTasiyici.getVardiyalar().isEmpty();
			int adet = 0;
			// String bayramEkleme = getParameterKey("bayramEkle");
			// Date simdikiZaman = new Date();
			boolean arifeYemekEkle = getParameterKey("arifeYemekEkle").equals("1");

			setVardiyaYemekList(vardiyalar, yemekGenelList);

			for (VardiyaGun vardiyaGun : vardiyalar) {
				vardiyaGun.setHaftaCalismaSuresi(0d);
				if (vardiyaGun.getVardiya() == null || vardiyaGun.getVardiya().getId() == null) {
					eksikCalisma = Boolean.TRUE;
					continue;
				}

				if (maxSuresi == 0 && vardiyaGun.getVardiya() != null && vardiyaGun.getVardiya().isCalisma())
					maxSuresi = vardiyaGun.getVardiya().getCalismaSaati();

				++adet;
				Vardiya vardiya = vardiyaGun.getIslemVardiya();
				if (gunSayisi == 0 && vardiya.isCalisma())
					gunSayisi = vardiya.getCalismaGun();
				double gunlukSaat = vardiyaNetCalismaSuresiMap != null && vardiyaNetCalismaSuresiMap.containsKey(vardiya.getId()) ? vardiyaNetCalismaSuresiMap.get(vardiya.getId()) : 0;
				if (maxSure < gunlukSaat)
					maxSure = gunlukSaat;

			}

			if (adet > 0) {
				if (gunSayisi > 0 && maxSure > 0 && !eksikCalisma)
					denklestirmeTasiyici.setToplamCalisilacakZaman(maxSure * gunSayisi);
				TreeMap<String, Double> haftaSonuSureMap = new TreeMap<String, Double>();
				Calendar cal = Calendar.getInstance();
				boolean flush = false;

				for (VardiyaGun vardiyaGun : vardiyalar) {
					if (vardiyaGun.getVardiya() == null || (fiiliHesapla == false && vardiyaGun.isIzinli() == false && vardiyaGun.isFiiliHesapla() == false)) {
						// if (vardiyaGun.isAyinGunu() && vardiyaGun.getVardiya() != null)
						// logger.info(vardiyaGun.getVardiyaKeyStr() + " " + vardiyaGun.getCalismaSuresi());
						continue;
					}

					String vGun = vardiyaGun.getVardiyaDateStr();
					String gun = vGun.substring(6);
					List<YemekIzin> yemekList = vardiyaGun.getYemekList();
					cal.setTime(vardiyaGun.getVardiyaDate());
					int ayinSonGunu = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
					Date digerAyinIlkGunu = vardiyaGun.isAyinGunu() && ayinSonGunu == Integer.parseInt(gun) ? tariheGunEkleCikar(cal, vardiyaGun.getVardiyaDate(), 1) : null;
					boolean tatilGunu = tatilGunleriMap != null && tatilGunleriMap.containsKey(vGun);
					vardiyaGun.setResmiTatilSure(0);
					vardiyaGun.setHaftaTatilDigerSure(0);
					vardiyaGun.setBayramCalismaSuresi(0);
					vardiyaGun.setCalisilmayanAksamSure(0d);
					// vardiyaGun.setZamanGelmedi(Boolean.FALSE);
					vardiyaGun.setCalismaSuresi(0);

					vardiyaGun.setFazlaMesaiSure(0);
					vardiyaGun.setGecenAyResmiTatilSure(0);
					if (vardiyaGun.getVardiya() == null)
						continue;

					VardiyaGun vardiyaHaftaTatil = null;
					boolean fazlaMesaiOnayla = calismaModeli.isFazlaMesaiVarMi() == false && vardiyaGun.getVardiya().isCalisma() == false;
					try {
						Vardiya vardiya = vardiyaGun.getIslemVardiya();
						if (vardiyaGun.isFiiliHesapla() == false) {
							if (vardiya.isCalisma() && vardiyaGun.getIzin() == null) {
								if (vardiyaNetCalismaSuresiMap == null)
									vardiyaNetCalismaSuresiMap = new HashMap<Long, Double>();
								if (vardiyaNetCalismaSuresiMap.containsKey(vardiya.getId())) {
									vardiyaGun.setCalismaSuresi(vardiyaNetCalismaSuresiMap.get(vardiya.getId()));
								} else {
									Double value = vardiya.getNetCalismaSuresi();
									vardiyaGun.setCalismaSuresi(value);
									vardiyaNetCalismaSuresiMap.put(vardiya.getId(), value);
								}
							}

							continue;
						}
						String key = vGun;
						if (gunMap != null)
							vardiyaGun.setAyinGunu(gunMap.get(key));

						if (vardiyaGun.getSonrakiVardiya() != null) {
							String key1 = (vardiyaGun.getPersonel() != null ? vardiyaGun.getPersonel().getSicilNo() : "") + "_" + PdksUtil.convertToDateString(vardiyaGun.getSonrakiVardiya().getVardiyaTarih(), "yyyyMMdd");
							if (haftaTatilMap.containsKey(key1))
								vardiyaHaftaTatil = haftaTatilMap.get(key1);
						}

						boolean resmiTatilCalisma = Boolean.FALSE;
						ArrayList<HareketKGS> tatilGirisHareketleri = new ArrayList<HareketKGS>(), tatilCikisHareketleri = new ArrayList<HareketKGS>();
						if (tatilGunleriMap.containsKey(key))
							vardiyaGun.setTatil(tatilGunleriMap.get(key));
						double resmiTatilSure = 0, toplamYemekSuresi = 0, vardiyaYemekSuresi = 0, calSure = 0d;

						if (gun.equals("01")) {
							if (vardiyaGun.getTatil() != null && vardiyaGun.getVardiya() != null && sonVardiyaGun != null) {
								if (sonVardiyaGun.getFazlaMesailer() != null && sonVardiyaGun.getVardiya() != null && !tatilGunleriMap.containsKey(sonVardiyaGun.getVardiyaDateStr())) {
									for (PersonelFazlaMesai personelFazlaMesai : sonVardiyaGun.getFazlaMesailer()) {
										if (personelFazlaMesai.isOnaylandi() && personelFazlaMesai.getTatilDurum() != null && personelFazlaMesai.getTatilDurum() == 1) {
											vardiyaGun.addGecenAyResmiTatilSure(personelFazlaMesai.getFazlaMesaiSaati());
											resmiTatilSure += personelFazlaMesai.getFazlaMesaiSaati();
										}
									}
								}
								if (sonVardiyaGun.getIslemVardiya() != null && sonVardiyaGun.getIslemVardiya().getVardiyaBitZaman().after(vardiyaGun.getVardiyaDate())) {
									long gunLong = vardiyaGun.getVardiyaDate().getTime();
									List<HareketKGS> girisHareketleri = sonVardiyaGun.getGirisHareketleri(), cikisHareketleri = sonVardiyaGun.getCikisHareketleri();
									if (girisHareketleri != null && cikisHareketleri != null && girisHareketleri.size() == cikisHareketleri.size()) {
										for (int i = 0; i < girisHareketleri.size(); i++) {
											HareketKGS girisHareket = (HareketKGS) girisHareketleri.get(i).clone();
											if (gunLong > girisHareket.getZaman().getTime())
												continue;
											HareketKGS cikisHareket = (HareketKGS) cikisHareketleri.get(i).clone();
											girisHareket.setVardiyaGun(sonVardiyaGun);
											cikisHareket.setVardiyaGun(sonVardiyaGun);
											tatilGirisHareketleri.add(girisHareket);
											tatilCikisHareketleri.add(cikisHareket);
											resmiTatilCalisma = Boolean.TRUE;
										}
									}

								}
							}
						}

						sonVardiyaGun = vardiyaGun;
						String yilAy = key.substring(0, 6);

						double gunlukSaat = vardiyaNetCalismaSuresiMap.containsKey(vardiya.getId()) ? vardiyaNetCalismaSuresiMap.get(vardiya.getId()) : 0;
						List<PersonelIzin> izinler = new ArrayList<PersonelIzin>();

						if (vardiyaGun.getIzinler() != null && vardiyaGun.getVardiya() != null && vardiyaGun.getVardiya().isCalisma()) {
							for (Iterator<PersonelIzin> iterator = vardiyaGun.getIzinler().iterator(); iterator.hasNext();) {
								PersonelIzin izin = iterator.next();
								if (izin.isRedmi())
									continue;
								if (!izin.isGunlukOldu() && (izin.getHesapTipi() == null || izin.getHesapTipi().equals(PersonelIzin.HESAP_TIPI_SAAT))) {
									izinler.add(izin);
									double sure = PdksUtil.getSaatFarki(izin.getBitisZamani(), izin.getBaslangicZamani());
									double yemeksizSure = getSaatSure(izin.getBaslangicZamani(), izin.getBitisZamani(), yemekList, vardiyaGun, session);
									toplamYemekSuresi += sure - yemeksizSure;
									calSure += yemeksizSure;

								}
							}

						}
						double fmSaat = 0.0d;
						TreeMap<String, Double> tatilMesaiMap = new TreeMap<String, Double>();
						TreeMap<Long, PersonelFazlaMesai> tatilFazlaMesaiMap = new TreeMap<Long, PersonelFazlaMesai>();
						Tatil tatilGun = haftaTatilDurum.equals("1") && tatilGunleriMap.containsKey(vGun) ? tatilGunleriMap.get(vGun) : null;
						if (gunlukSaat == 0.0 && tatilGun != null && vardiyaGun.getFazlaMesailer() != null && vardiyaGun.getVardiya().isHaftaTatil()) {
							ArrayList<HareketKGS> girisHareketleri = vardiyaGun.getGirisHareketleri(), cikisHareketleri = vardiyaGun.getCikisHareketleri();
							if (girisHareketleri != null && cikisHareketleri != null && girisHareketleri.size() == cikisHareketleri.size()) {
								for (PersonelFazlaMesai personelFazlaMesai : vardiyaGun.getFazlaMesailer()) {
									if (personelFazlaMesai.isOnaylandi()) {
										for (int i = 0; i < girisHareketleri.size(); i++) {
											HareketKGS girisHareketKGS = girisHareketleri.get(i), cikisHareketKGS = cikisHareketleri.get(i);
											if (girisHareketKGS.getId().equals(personelFazlaMesai.getHareketId()) || cikisHareketKGS.getId().equals(personelFazlaMesai.getHareketId())) {
												cikisHareketKGS.setPersonelFazlaMesai(personelFazlaMesai);
												girisHareketKGS.setPersonelFazlaMesai(personelFazlaMesai);
												tatilMesaiMap.put(cikisHareketKGS.getId(), personelFazlaMesai.getFazlaMesaiSaati());
												tatilMesaiMap.put(girisHareketKGS.getId(), personelFazlaMesai.getFazlaMesaiSaati());
												fmSaat += personelFazlaMesai.getFazlaMesaiSaati();
												break;
											}
										}
									}
								}

							}
						}

						if (gunlukSaat > 0 || fmSaat > 0 || fazlaMesaiOnayla) {
							ArrayList<HareketKGS> girisHareketleri = new ArrayList<HareketKGS>(), cikisHareketleri = new ArrayList<HareketKGS>();
							vardiyaYemekSuresi = vardiyaGun.getIslemVardiya() != null && vardiyaGun.getIslemVardiya().getYemekSuresi() != null ? vardiyaGun.getIslemVardiya().getYemekSuresi() / 60 : 0d;
							vardiyaGun.setNormalSure(gunlukSaat);
							Tatil tatil = null;
							boolean arifeGunu = false;

							if (vardiyaGun.getCikisHareketleri() != null && !vardiyaGun.getCikisHareketleri().isEmpty() && vardiyaGun.getHareketDurum()) {
								if (vardiyaGun.isAyinGunu() && vardiyaGun.getTatil() != null && vardiyaGun.getHareketler() != null && !vardiyaGun.getHareketler().isEmpty()) {
									Tatil orjTatil1 = (Tatil) vardiyaGun.getTatil().getOrjTatil().clone();
									String arifeAyGun = orjTatil1 != null && orjTatil1.getBasTarih() != null ? PdksUtil.convertToDateString(orjTatil1.getBasTarih(), "MMdd") : "";
									arifeGunu = orjTatil1 != null && arifeAyGun.equals(vGun.substring(4)) && orjTatil1.isYarimGunMu();
									tatil = (Tatil) vardiyaGun.getTatil().clone();
									if (orjTatil1.isYarimGunMu() || arifeGunu) {
										tatil.setBitTarih(orjTatil1.getBitTarih());

									}
									vardiyaGun.setTatil(tatil);
									List<HareketKGS> hareketList = new ArrayList<HareketKGS>();
									if (vardiyaGun.getGirisHareketleri() != null)
										hareketList.addAll(vardiyaGun.getGirisHareketleri());
									if (vardiyaGun.getCikisHareketleri() != null)
										hareketList.addAll(vardiyaGun.getCikisHareketleri());
									hareketList = PdksUtil.sortListByAlanAdi(hareketList, "zaman", Boolean.FALSE);
									if (!hareketList.isEmpty()) {
										Date tatilBasTarih = tatil.getBasTarih(), tatilBitTarih = orjTatil1.isYarimGunMu() ? PdksUtil.getDate(tariheGunEkleCikar(cal, tatil.getBitTarih(), 1)) : tatil.getBitTarih();

										tatil.setBitTarih(tatilBitTarih);
										long basZaman = Long.parseLong(PdksUtil.convertToDateString(tatilBasTarih, "yyyyMMddHHmm"));
										long bitZaman = Long.parseLong(PdksUtil.convertToDateString(tatilBitTarih, "yyyyMMddHHmm"));
										long zaman = Long.parseLong(PdksUtil.convertToDateString(hareketList.get(0).getZaman(), "yyyyMMddHHmm"));
										boolean bayramBasladi = zaman >= basZaman;
										boolean bayramBitti = zaman >= bitZaman;
										for (HareketKGS hareketKGS : hareketList) {
											zaman = Long.parseLong(PdksUtil.convertToDateString(hareketKGS.getZaman(), "yyyyMMddHHmm"));
											Kapi kapi = hareketKGS.getKapiView().getKapi();
											if (zaman >= basZaman && zaman <= bitZaman) {
												if (girisKapi != null && !bayramBasladi && kapi.isCikisKapi()) {

													HareketKGS cikisHareket = ((HareketKGS) hareketKGS.clone()).getYeniHareket(tatil.getBasTarih(), null);
													cikisHareketleri.add(cikisHareket);
													HareketKGS girisHareket = ((HareketKGS) cikisHareket.clone()).getYeniHareket(null, girisKapi);
													girisHareket.setTatil(Boolean.TRUE);
													girisHareket.setPersonelFazlaMesai(null);
													girisHareketleri.add(girisHareket);
												}
												hareketKGS.setTatil(Boolean.TRUE);
												bayramBasladi = Boolean.TRUE;
											} else if (girisKapi != null && zaman >= bitZaman && !bayramBitti && kapi.isCikisKapi()) {
												HareketKGS cikisHareket = ((HareketKGS) hareketKGS.clone()).getYeniHareket((Date) tatil.getBitGun(), null);
												cikisHareket.setTatil(Boolean.TRUE);
												cikisHareketleri.add(cikisHareket);
												HareketKGS girisHareket = ((HareketKGS) cikisHareket.clone()).getYeniHareket(null, girisKapi);
												girisHareket.setTatil(Boolean.FALSE);
												girisHareketleri.add(girisHareket);
												bayramBitti = Boolean.TRUE;
											}
											if (kapi.isGirisKapi())
												girisHareketleri.add(hareketKGS);

											else if (kapi.isCikisKapi())
												cikisHareketleri.add(hareketKGS);
										}
									}
									vardiyaGun.setGirisHareketleri(girisHareketleri);
									vardiyaGun.setCikisHareketleri(cikisHareketleri);
								}

								if (vardiyaGun.getCikisHareketleri() != null && !vardiyaGun.getCikisHareketleri().isEmpty()) {
									for (HareketKGS hareketKGS : vardiyaGun.getCikisHareketleri()) {
										if (vardiyaGun.getTatil() != null) {
											tatil = vardiyaGun.getTatil();
											if (hareketKGS.isTatil() == false && hareketKGS.getZaman().getTime() > tatil.getBasTarih().getTime() && hareketKGS.getZaman().getTime() <= tatil.getBitTarih().getTime())
												hareketKGS.setTatil(true);
										}

										tatilCikisHareketleri.add(hareketKGS);
									}

								}

								if (vardiyaGun.getGirisHareketleri() != null && !vardiyaGun.getGirisHareketleri().isEmpty())
									tatilGirisHareketleri.addAll(vardiyaGun.getGirisHareketleri());

							}

						}

						boolean sureHesapla = Boolean.FALSE;
						TreeMap<String, Double> yemekFarkMap = new TreeMap<String, Double>();
						LinkedHashMap<String, Double> arifeSureMap = new LinkedHashMap<String, Double>();
						if (!tatilCikisHareketleri.isEmpty() && tatilCikisHareketleri.size() == tatilGirisHareketleri.size()) {
							Tatil tatil = null;
							if (vardiyaGun.getTatil() != null) {
								tatil = vardiyaGun.getTatil();
								// tatil = vardiyaGun.getTatil().getOrjTatil().isPeriyodik() ? vardiyaGun.getTatil() : vardiyaGun.getTatil().getOrjTatil();

							}
							Tatil orjTatil = tatil != null ? vardiyaGun.getTatil().getOrjTatil() : null;
							String arifeAyGun = orjTatil != null && orjTatil.getBasTarih() != null ? PdksUtil.convertToDateString(orjTatil.getBasTarih(), "MMdd") : "";
							boolean arifeGunu = orjTatil != null && arifeAyGun.equals(vGun.substring(4)) && orjTatil.isYarimGunMu();
							Date oncekiCikisZaman = null;
							double yemekSure = (double) vardiyaGun.getVardiya().getYemekSuresi() / 60.0d, netSure = vardiyaGun.getVardiya().getNetCalismaSuresi();
							double toplamParcalanmisSure = 0.0d;
							boolean parcalanmisSureVar = false;

							for (int i = 0; i < tatilCikisHareketleri.size(); i++) {
								HareketKGS cikisHareket = tatilCikisHareketleri.get(i);
								HareketKGS girisHareket = null;
								try {
									girisHareket = tatilGirisHareketleri.get(i);
								} catch (Exception e) {
									logger.error("Pdks hata in : \n");
									e.printStackTrace();
									logger.error("Pdks hata out : " + e.getMessage());
									girisHareket = null;
									break;
								}

								Calendar calTatilBas = Calendar.getInstance();

								if (orjTatil != null) {
									calTatilBas.setTime(tatil.getBasTarih());

								}
								Date girisZaman = girisHareket != null ? girisHareket.getZaman() : null;
								Date cikisZaman = cikisHareket != null ? cikisHareket.getZaman() : null;

								String girisId = girisHareket != null && girisHareket.getId() != null ? girisHareket.getId() : "";
								String cikisId = cikisHareket != null && cikisHareket.getId() != null ? cikisHareket.getId() : "";
								if (girisZaman.before(ilkGun) && PdksUtil.hasStringValue(cikisId) == false)
									continue;
								if (!parcalanmisSureVar)
									parcalanmisSureVar = PdksUtil.hasStringValue(girisId) == false || PdksUtil.hasStringValue(cikisId) == false;
								toplamParcalanmisSure += PdksUtil.getSaatFarki(cikisZaman, girisZaman);
								PersonelFazlaMesai personelFazlaMesai = girisHareket.getPersonelFazlaMesai() != null ? girisHareket.getPersonelFazlaMesai() : cikisHareket.getPersonelFazlaMesai();

								if (tatilGunu == false && (fazlaMesaiOnayla || girisHareket.isOrjinalZamanGetir() || cikisHareket.isOrjinalZamanGetir())) {
									if (personelFazlaMesai == null) {
										boolean devam = true;
										List<String> idList = new ArrayList<String>();
										if (PdksUtil.hasStringValue(girisId))
											idList.add(girisId);
										if (PdksUtil.hasStringValue(cikisId))
											idList.add(cikisId);
										if (!idList.isEmpty()) {
											HashMap fields = new HashMap();
											fields.put("vardiyaGun.id", vardiyaGun.getId());
											fields.put("hareketId", idList);
											if (session != null)
												fields.put(PdksEntityController.MAP_KEY_SESSION, session);
											List<PersonelFazlaMesai> personelFazlaMesaiList = pdksEntityController.getObjectByInnerObjectList(fields, PersonelFazlaMesai.class);
											devam = personelFazlaMesaiList.isEmpty();
											fields = null;
										}
										idList = null;
										if (devam) {
											if (fazlaMesaiOnayDurum == null)
												fazlaMesaiOnayDurum = otomatikFazlaMesaiOnayTanimGetir(session);
											Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
											personelFazlaMesai = new PersonelFazlaMesai();
											personelFazlaMesai.setVardiyaGun(vardiyaGun);
											personelFazlaMesai.setFazlaMesaiOnayDurum(fazlaMesaiOnayDurum);
											if (cikisHareket.isTatil())
												personelFazlaMesai.setTatilDurum(1);
											personelFazlaMesai.setOnayDurum(PersonelFazlaMesai.DURUM_ONAYLANDI);
											Date basZaman = girisHareket.isOrjinalZamanGetir() ? girisZaman : null, bitZaman = cikisHareket.isOrjinalZamanGetir() ? cikisZaman : null;
											personelFazlaMesai.setHareketId(cikisId);
											if (basZaman == null)
												basZaman = islemVardiya.getVardiyaBitZaman();
											else {
												cikisHareket.setPersonelFazlaMesai(personelFazlaMesai);
												personelFazlaMesai.setHareketId(girisId);
											}
											if (bitZaman == null) {
												girisHareket.setPersonelFazlaMesai(personelFazlaMesai);
												bitZaman = islemVardiya.getVardiyaBasZaman();
											}
											if (fazlaMesaiOnayla) {
												String hareketId = null;
												if (girisId.startsWith(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_KGS) || girisId.startsWith(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_PDKS))
													hareketId = girisId;
												else if (cikisId.startsWith(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_KGS) || cikisId.startsWith(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_PDKS))
													hareketId = cikisId;
												basZaman = girisHareket.getOrjinalZaman();
												bitZaman = cikisHareket.getOrjinalZaman();
												personelFazlaMesai.setHareketId(hareketId);
											}
											if (vardiyaGun.getHareketler() != null) {
												for (HareketKGS hareket : vardiyaGun.getHareketler()) {
													if (hareket.getId() != null && hareket.getId().equals(personelFazlaMesai.getHareketId())) {
														hareket.setPersonelFazlaMesai(personelFazlaMesai);
														break;
													}
												}
											}

											personelFazlaMesai.setBasZaman(basZaman);
											personelFazlaMesai.setBitZaman(bitZaman);
											List yemekler = arifeGunu && arifeYemekEkle && oncekiCikisZaman != null && oncekiCikisZaman.getTime() == girisZaman.getTime() ? new ArrayList<YemekIzin>() : yemekList;
											double fazlaMesaiSaati = getSaatSure(basZaman, bitZaman, yemekler, vardiyaGun, session);
											fazlaMesaiSaati = PdksUtil.setSureDoubleTypeRounded(fazlaMesaiSaati, vardiyaGun.getYarimYuvarla());
											personelFazlaMesai.setFazlaMesaiSaati(fazlaMesaiSaati);
											personelFazlaMesai.setOlusturanUser(sistemUser != null ? sistemUser : loginUser);
											if (cikisHareket.isTatil())
												tatilMesaiMap.put(personelFazlaMesai.getHareketId(), personelFazlaMesai.getFazlaMesaiSaati());
											if (personelFazlaMesai.getHareketId() != null && bitZaman.after(basZaman)) {
												vardiyaGun.addPersonelFazlaMesai(personelFazlaMesai);
												if (updateSatus) {
													pdksEntityController.saveOrUpdate(session, entityManager, personelFazlaMesai);
													flush = true;
												}

											}

										}
									}
									girisHareket.setOrjinalZamanGetir(false);
									cikisHareket.setOrjinalZamanGetir(false);
								}
								if (personelFazlaMesai != null && tatilGun != null && (tatilMesaiMap.containsKey(girisId) || tatilMesaiMap.containsKey(cikisId))) {
									if (tatilMesaiMap.containsKey(girisId) && tatilMesaiMap.containsKey(cikisId)) {

										continue;
									}

									else {
										if (!tatilFazlaMesaiMap.containsKey(personelFazlaMesai.getId())) {
											resmiTatilSure += personelFazlaMesai.getFazlaMesaiSaati();
											tatilFazlaMesaiMap.put(personelFazlaMesai.getId(), personelFazlaMesai);
										}
										continue;

									}

								}
								// ozel durum
								// aybasindan onceki gun icin duzenleme. diger gunler icinde calismali ( ay basi baslayan tatil)
								// eger bu vardiya 00.00 dan itibaren calisması o gun eklenmeyecek. ay sonu ise

								if (digerAyinIlkGunu != null && tatil != null && digerAyinIlkGunu.getTime() <= tatil.getBasTarih().getTime()) {
									long tatilBasZaman = Long.parseLong(PdksUtil.convertToDateString(orjTatil.getBasTarih(), "yyyyMMddHHmm"));
									long tatilBitZaman = Long.parseLong(PdksUtil.convertToDateString(tatil.getBitTarih(), "yyyyMMddHHmm"));
									long girisZamani = Long.parseLong(PdksUtil.convertToDateString(girisZaman, "yyyyMMddHHmm"));
									if (girisZamani >= tatilBasZaman && girisZamani <= tatilBitZaman) {

										// tatil zamanini hesaplama.
										continue;
									}
									// }
								}

								if (cikisHareket != null && girisHareket != null) {
									if (izinler != null) {
										for (Iterator iterator = izinler.iterator(); iterator.hasNext();) {
											PersonelIzin personelIzin = (PersonelIzin) iterator.next();
											if (personelIzin.getBaslangicZamani().getTime() > girisZaman.getTime() && cikisZaman.getTime() > personelIzin.getBaslangicZamani().getTime())
												cikisHareket.setZaman(personelIzin.getBaslangicZamani());
											if (personelIzin.getBitisZamani().getTime() > girisZaman.getTime() && cikisZaman.getTime() > personelIzin.getBitisZamani().getTime())
												girisHareket.setZaman(personelIzin.getBitisZamani());

										}
									}
									double yemeksizSure = 0;
									List yemekler = toplamYemekSuresi > 0 && arifeGunu && arifeYemekEkle && oncekiCikisZaman != null && oncekiCikisZaman.getTime() == girisZaman.getTime() ? new ArrayList<YemekIzin>() : yemekList;
									if (arifeGunu)
										yemekler = yemekList;
									if (cikisHareket.getVardiyaGun() == null || cikisHareket.getVardiyaGun().getId().equals(vardiyaGun.getId())) {
										sureHesapla = Boolean.TRUE;
										double sure = PdksUtil.getSaatFarki(cikisZaman, girisZaman);
										yemeksizSure = getSaatSure(girisZaman, cikisZaman, yemekler, vardiyaGun, session);
										toplamYemekSuresi += sure - yemeksizSure;
										if (toplamYemekSuresi > yemekSure)
											yemeksizSure += toplamYemekSuresi - yemekSure;
										if (tatilMesaiMap.containsKey(girisId) || tatilMesaiMap.containsKey(cikisId)) {
											yemeksizSure = tatilMesaiMap.containsKey(girisId) ? tatilMesaiMap.get(girisId) : tatilMesaiMap.get(cikisId);
											resmiTatilCalisma = true;
										} else
											calSure += yemeksizSure;
										if (arifeGunu) {
											String tatilKey = cikisHareket.isTatil() ? "A" : "N";
											String key1 = tatilKey + "_B", key2 = tatilKey + "_N";
											arifeCalismaToplami(arifeSureMap, yemeksizSure, sure, key1, key2);
											key1 = "T_B";
											key2 = "T_N";
											arifeCalismaToplami(arifeSureMap, yemeksizSure, sure, key1, key2);
										}
									}

									vardiyaYemekSuresi = vardiya != null && vardiya.getYemekSuresi() != null ? vardiya.getYemekSuresi().doubleValue() / 60d : 0d;

									if (yemeksizSure > 0 || resmiTatilCalisma) {
										if (resmiTatilCalisma || (vardiyaGun.getTatil() != null && cikisHareket.isTatil())) {

											if (tatil != null) {
												Date tatilBasZaman = girisZaman;
												String hareketYilAy = PdksUtil.convertToDateString(tatilBasZaman, "yyyyMM");
												Date tatilOrjBitZaman = tatil.getBitTarih();
												Date tatilBitZaman = cikisZaman;
												if (tatilBasZaman.before(tatil.getBasTarih()))
													tatilBasZaman = tatil.getBasTarih();
												if (tatilBitZaman.after(tatilOrjBitZaman))
													tatilBitZaman = tatilOrjBitZaman;
												if (vardiyaGun.getIslemVardiya() != null && tatilBitZaman.after(vardiyaGun.getIslemVardiya().getVardiyaBitZaman()))
													tatilBitZaman = vardiyaGun.getIslemVardiya().getVardiyaBitZaman();
												if (hareketYilAy.equals(yilAy) && tatilBasZaman.before(tatilBitZaman)) {
													Double yemekSuresi = 0.0d;

													double bayramCalisma = getSaatSure(tatilBasZaman, tatilBitZaman, yemekler, vardiyaGun, session) - (yemekSuresi / 60.0d);
													String bayramKey = PdksUtil.convertToDateString(tatilBasZaman, "yyyyMMddHHmm");
													if (yemekFarkMap.containsKey(bayramKey)) {
														bayramCalisma += yemekFarkMap.get(bayramKey);
														yemekFarkMap.remove(bayramKey);
													}
													if (!sureHesapla) {
														try {
															if (cikisHareket.getVardiyaGun() != null && !cikisHareket.getVardiyaGun().getId().equals(vardiyaGun.getId())) {
																Vardiya cikisVardiya = cikisHareket.getVardiyaGun().getVardiya();
																double yemekCikisToplamSure = cikisVardiya.getYemekSuresi() / 60.0d;
																double yemekCikisSure = PdksUtil.getSaatFarki(cikisZaman, girisZaman) - bayramCalisma;
																double netCikisSure = cikisVardiya.getNetCalismaSuresi() + yemekCikisToplamSure;
																bayramCalisma -= yemekCikisSure + (PdksUtil.setSureDoubleTypeRounded((yemekCikisToplamSure * bayramCalisma) / netCikisSure, cikisHareket.getVardiyaGun().getYarimYuvarla()));
															}
														} catch (Exception e) {
															// TODO: handle exception
														}

														vardiyaGun.addGecenAyResmiTatilSure(bayramCalisma);
														bayramCalisma = 0;

													}
													if (tatilMesaiMap.containsKey(girisId) || tatilMesaiMap.containsKey(cikisId)) {
														bayramCalisma = tatilMesaiMap.containsKey(girisId) ? tatilMesaiMap.get(girisId) : tatilMesaiMap.get(cikisId);

													}
													if (bayramCalisma > 0.0d) {
														// calSure += bayramCalisma;
														resmiTatilSure += bayramCalisma;
														vardiyaGun.addBayramCalismaSuresi(bayramCalisma);
														if (vardiyaGun.getFazlaMesailer() != null) {
															addBayramCalismaSuresi(vardiyaGun);
														}

													}

												}

											}

										}

									}
									if (toplamYemekSuresi > yemekSure) {
										double fark = toplamYemekSuresi - yemekSure;
										yemekFarkMap.put(vGun, fark);
										toplamYemekSuresi = yemekSure;
									}

								}
								oncekiCikisZaman = (Date) cikisZaman.clone();
							}

							if (sureHesapla && gunlukSaat > 0) {

								boolean tatilYemekHesabiSureEkle = vardiyaGun.isYemekHesabiSureEkle();
								double fark = toplamYemekSuresi - vardiyaYemekSuresi;
								if (vardiyaGun.isAyinGunu() && gun.equals("29"))
									logger.debug(calSure);
								if (yemekList.isEmpty()) {
									double eksikSure = netSure + vardiyaYemekSuresi - calSure;
									if (eksikSure <= 0) {
										fark += (netSure + vardiyaYemekSuresi - calSure);
										calSure += fark;
										if (resmiTatilSure > 0) {
											resmiTatilSure += fark;
											vardiyaGun.addBayramCalismaSuresi(fark);
										}
									} else if (vardiyaYemekSuresi > toplamYemekSuresi && (netSure + vardiyaYemekSuresi) * yemekMolasiYuzdesi >= calSure) {
										double pay = calSure;
										double payda = netSure + vardiyaYemekSuresi;
										double yemekFark = (calSure - PdksUtil.setSureDoubleTypeRounded((pay * netSure) / payda, vardiyaGun.getYarimYuvarla()));
										if (tatilYemekHesabiSureEkle == false)
											calSure -= yemekFark;
										else
											calSure -= fark;
									}

								} else {

									if (toplamYemekSuresi > vardiyaYemekSuresi) {
										calSure += fark;
										toplamYemekSuresi = vardiyaYemekSuresi;
									} else if (vardiyaYemekSuresi > toplamYemekSuresi && (netSure + vardiyaYemekSuresi) * yemekMolasiYuzdesi <= toplamParcalanmisSure) {
										double resmiCalisma = resmiTatilSure;
										if (resmiTatilSure > 0.0d) {
											if (resmiTatilSure == calSure && toplamParcalanmisSure == netSure + vardiyaYemekSuresi) {
												logger.debug(gun);
												calSure += fark;
												resmiTatilSure += fark;

											} else {

												double yemekFark = 0.0d;
												if (tatilYemekHesabiSureEkle == false) {
													double rs = resmiCalisma > netSure ? netSure : resmiCalisma;
													double pay = rs;
													double payda = netSure + vardiyaYemekSuresi;
													yemekFark = PdksUtil.setSureDoubleTypeRounded((pay * fark) / payda, vardiyaGun.getYarimYuvarla());

												} else {
													double rs = resmiCalisma > netSure ? netSure : resmiCalisma;
													yemekFark = PdksUtil.setSureDoubleTypeRounded(((rs + vardiyaYemekSuresi) * fark) / (netSure + vardiyaYemekSuresi), vardiyaGun.getYarimYuvarla());

												}
												vardiyaYemekSuresi += yemekFark;
												resmiTatilSure += yemekFark;
												vardiyaGun.addBayramCalismaSuresi(yemekFark);
											}
										} else {
											double yemekOranFark = PdksUtil.setSureDoubleTypeRounded(toplamYemekSuresi - (toplamParcalanmisSure * vardiyaYemekSuresi) / (netSure + vardiyaYemekSuresi), vardiyaGun.getYarimYuvarla());
											if (fark < yemekOranFark && yemekOranFark < 0)
												fark = yemekOranFark;
											calSure += fark;
										}
									} else if (parcalanmisSureVar && toplamParcalanmisSure == netSure + vardiyaYemekSuresi) {
										double yemekFark = PdksUtil.setSureDoubleTypeRounded(toplamYemekSuresi - (vardiyaYemekSuresi * calSure / toplamParcalanmisSure), vardiyaGun.getYarimYuvarla());
										calSure += yemekFark;

									}
								}
								// double fazlaCalismaToplamSure = 0.0d;
								// if (vardiyaGun.getFazlaMesailer() != null) {
								// for (PersonelFazlaMesai personelFazlaMesai : sonVardiyaGun.getFazlaMesailer()) {
								// if (personelFazlaMesai.isOnaylandi() && personelFazlaMesai.getDurum()) {
								// fazlaCalismaToplamSure += personelFazlaMesai.getFazlaMesaiSaati();
								//
								// }
								// }
								// }

								if (calSure > netSure) {
									// if (resmiTatilSure + netSure - calSure > 0.0d)
									// resmiTatilSure += netSure - calSure;
									calSure = netSure;

								}
								if (arifeSureMap.containsKey("T_B") && arifeSureMap.containsKey("A_N")) {
									Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
									double toplamBrutSure = arifeSureMap.get("T_B");
									double netCalismaSure = islemVardiya.getNetCalismaSuresi() / 2.0d;
									double arifeNetSure = arifeSureMap.get("A_N");
									double sure = PdksUtil.getSaatFarki(islemVardiya.getVardiyaBitZaman(), islemVardiya.getVardiyaBasZaman());
									if (toplamBrutSure == sure && arifeNetSure > netCalismaSure) {
										vardiyaGun.addBayramCalismaSuresi(netCalismaSure - resmiTatilSure);
										resmiTatilSure = netCalismaSure;

									}
								}
							}
							if (resmiTatilSure > 0.0d)
								resmiTatilMesai += resmiTatilSure;
						}
						if (fazlaMesaiOnayla)
							calSure = 0;
						if (!vardiyaGun.getVardiya().isIcapVardiyasi())
							vardiyaGun.addCalismaSuresi(PdksUtil.setSureDoubleTypeRounded(calSure, vardiyaGun.getYarimYuvarla()));
						if (vardiyaGun.isHareketHatali()) {
							vardiyaGun.setFazlaMesailer(null);
							if (vardiyaGun.getHareketler() != null) {
								for (HareketKGS hareket : vardiyaGun.getHareketler()) {
									hareket.setFazlaMesai(0d);
									hareket.setPersonelFazlaMesai(null);
								}
							}
						}
						ArrayList<PersonelFazlaMesai> vardiyaFazlaMesailer = vardiyaGun.getFazlaMesailer();
						if (vardiyaFazlaMesailer != null) {
							if (session == null)
								session = PdksUtil.getSessionUser(entityManager, loginUser);
							flush = Boolean.FALSE;
							for (Iterator<PersonelFazlaMesai> iterator = vardiyaFazlaMesailer.iterator(); iterator.hasNext();) {
								PersonelFazlaMesai personelFazlaMesai = iterator.next();
								try {
									if (personelFazlaMesai.getFazlaMesaiSaati() == null && personelFazlaMesai.getOnayDurum() == PersonelFazlaMesai.DURUM_ONAYLANDI) {
										try {
											if (updateSatus) {
												pdksEntityController.deleteObject(session, entityManager, personelFazlaMesai);
												flush = Boolean.TRUE;
											}
										} catch (Exception e) {
										}

										continue;
									}

									if (personelFazlaMesai.getFazlaMesaiSaati() == null || personelFazlaMesai.getFazlaMesaiSaati() == 0 || personelFazlaMesai.getOnayDurum() != PersonelFazlaMesai.DURUM_ONAYLANDI)
										continue;
								} catch (Exception e1) {
									logger.error(e1);
									continue;
								}
								if (personelFazlaMesai.getOnayDurum() == PersonelFazlaMesai.DURUM_ONAYLANDI) {
									if (personelFazlaMesai.getTatilDurum() != null) {
										cal = Calendar.getInstance();
										cal.setTime(vardiyaGun.getVardiyaDate());
										boolean ekle = !tatilFazlaMesaiMap.containsKey(personelFazlaMesai.getId());

										boolean tatilDegil = vardiyaGun.getTatil() == null;
										if (!tatilDegil) {
											Tatil tatil = vardiyaGun.getTatil();
											if (tatil.getBasTarih().after(vardiyaGun.getVardiyaDate()))
												tatilDegil = true;
										}
										if (tatilDegil) {
											int ayinGunu = cal.get(Calendar.DATE), sonGun = cal.getActualMaximum(Calendar.DATE);
											if (ayinGunu == sonGun)
												ekle = Boolean.FALSE;
										}

										if (ekle) {
											resmiTatilMesai += personelFazlaMesai.getFazlaMesaiSaati();
											resmiTatilSure += personelFazlaMesai.getFazlaMesaiSaati();
										}

										// logger.info(resmiTatilSure);
									}
									// if (personelFazlaMesai.getBasZaman().getTime() >= ilkGun.getTime())
									vardiyaGun.addCalismaSuresi(personelFazlaMesai.getFazlaMesaiSaati());

								}

							}
							if (flush)
								try {
									session.flush();
								} catch (Exception e) {
								}

						}
						if (calismaModeli.isFazlaMesaiVarMi() && resmiTatilSure > 0) {
							// TODO Resmi tatil çalışma saatinden düşüyor
							// if (vardiyaGun.getCalismaSuresi() >= resmiTatilSure)
							// vardiyaGun.addCalismaSuresi(-resmiTatilSure);
							vardiyaGun.setResmiTatilSure(PdksUtil.setSureDoubleTypeRounded(resmiTatilSure, vardiyaGun.getYarimYuvarla()));
							if (vardiyaGun.getResmiTatilSure() > vardiyaGun.getCalismaSuresi() + vardiyaGun.getGecenAyResmiTatilSure())
								vardiyaGun.addCalismaSuresi(vardiyaGun.getResmiTatilSure() - vardiyaGun.getCalismaSuresi() - vardiyaGun.getGecenAyResmiTatilSure());
						}

						if (denklestirmeTasiyici.isCheckBoxDurum())
							denklestirmeTasiyici.setCheckBoxDurum(vardiyaGun.getHareketDurum());

						if (sureHesapla && vardiyaGun.isAyinGunu() && vardiyaGun.getCalismaSuresi() > 0.0d) {
							denklestirmeTasiyici.addToplamCalisilanZaman(vardiyaGun.getVardiyaKeyStr(), vardiyaGun.getCalismaSuresi());

						}
						if (vardiyaGun.getVardiya().isHaftaTatil()) {
							if (vardiyaGun.getCalismaSuresi() > 0.0d && haftaTatilMap.containsKey(vardiyaGun.getVardiyaKeyStr()))
								vardiyaHaftaTatil = vardiyaGun;
						}
						if (vardiyaHaftaTatil != null) {
							String key1 = vardiyaHaftaTatil.getVardiyaKeyStr();
							double haftaCalismaSuresi = haftaSonuSureMap.containsKey(key1) ? haftaSonuSureMap.get(key1) : 0;
							if (vardiyaGun.getCalismaSuresi() > 0 && resmiTatilSure < vardiyaGun.getCalismaSuresi()) {
								Date basHaftaTatil = (Date) vardiyaHaftaTatil.getVardiyaDate().clone();
								if (calismaModeli.getGeceHaftaTatilMesaiParcala().equals(Boolean.FALSE))
									basHaftaTatil = vardiyaHaftaTatil.getIslemVardiya().getVardiyaBasZaman();

								// TODO Hafta Tatili vardiya bitiminden sonra başlasın
								// if (vardiyaGun.getIslemVardiya().getVardiyaBitZaman().after(basHaftaTatil))
								// basHaftaTatil = vardiyaGun.getIslemVardiya().getVardiyaBitZaman();
								if (vardiyaGun.getVardiya().getBasSaat() >= vardiyaGun.getVardiya().getBitSaat()) {
									List<HareketKGS> girisHareketleri = vardiyaGun.getGirisHareketleri(), cikisHareketleri = vardiyaGun.getCikisHareketleri();
									if (cikisHareketleri != null && cikisHareketleri != null && girisHareketleri.size() == cikisHareketleri.size()) {
										for (int i = 0; i < cikisHareketleri.size(); i++) {
											try {
												Date hareket2 = cikisHareketleri.get(i).getZaman();
												if (hareket2.getTime() <= basHaftaTatil.getTime())
													continue;

												Date hareket1 = girisHareketleri.get(i).getZaman();
												if (vardiyaGun.isHaftaTatil() && haftaTatilMap.containsKey(key1) && vardiyaGun.getIslemVardiya() != null) {
													Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
													if (hareket1.getTime() >= islemVardiya.getVardiyaBasZaman().getTime() && hareket2.getTime() <= islemVardiya.getVardiyaFazlaMesaiBitZaman().getTime())
														continue;
												}
												if (hareket1.before(basHaftaTatil))
													hareket1 = basHaftaTatil;
												double yemeksizSure = getSaatSure(hareket1, hareket2, yemekList, vardiyaGun, session);
												if (vardiyaGun.getVardiya().isCalisma())
													haftaCalismaSuresi += yemeksizSure;
											} catch (Exception e) {
												e.printStackTrace();
											}

										}
									}
								}
								Double calismaSuresi = 0.0d;

								if (vardiyaGun.getFazlaMesailer() != null) {
									HashMap<String, Double> dataMap = new HashMap<String, Double>();
									dataMap.put("haftaCalismaSuresi", haftaCalismaSuresi);
									dataMap.put("calismaSuresi", calismaSuresi);

									for (Iterator<PersonelFazlaMesai> iterator = vardiyaGun.getFazlaMesailer().iterator(); iterator.hasNext();) {
										PersonelFazlaMesai personelFazlaMesai = iterator.next();
										if (personelFazlaMesai.getOnayDurum() == PersonelFazlaMesai.DURUM_ONAYLANDI && personelFazlaMesai.getTatilDurum() == null && personelFazlaMesai.getBitZaman().after(basHaftaTatil)) {
											Date hareket1 = personelFazlaMesai.getBasZaman(), hareket2 = personelFazlaMesai.getBitZaman();
											double yemeksizSure = personelFazlaMesai.getFazlaMesaiSaati();
											if (vardiyaGun.getVardiya().isHaftaTatil()) {
												haftaTatilMesaiHesapla(dataMap, vardiyaGun, personelFazlaMesai, yemekList, session);
												haftaCalismaSuresi = dataMap.get("haftaCalismaSuresi");
												calismaSuresi = dataMap.get("calismaSuresi");
											} else {
												if (hareket1.before(basHaftaTatil)) {
													hareket1 = basHaftaTatil;
													yemeksizSure = getSaatSure(hareket1, hareket2, yemekList, vardiyaGun, session);
												}
												haftaCalismaSuresi += yemeksizSure;
											}
										}

									}
								}
								if (haftaCalismaSuresi > 0)
									haftaCalismaSuresi = PdksUtil.setSureDoubleTypeRounded(haftaCalismaSuresi, vardiyaGun.getYarimYuvarla());
								if (!vardiyaGun.getId().equals(vardiyaHaftaTatil.getId()) && calismaModeli.getGeceHaftaTatilMesaiParcala()) {
									vardiyaGun.addCalismaSuresi(-haftaCalismaSuresi);
									vardiyaGun.addHaftaTatilDigerSure(haftaCalismaSuresi);
								} else
									vardiyaGun.setCalismaSuresi(calismaSuresi);
								if (calismaModeli != null && calismaModeli.isFazlaMesaiVarMi())
									vardiyaHaftaTatil.setHaftaCalismaSuresi(haftaCalismaSuresi);
								if (haftaCalismaSuresi > 0) {
									vardiyaGun.addCalismaSuresi(haftaCalismaSuresi);
									haftaSonuSureMap.put(key1, haftaCalismaSuresi);
									if (!vardiyaGun.getVardiyaKeyStr().equals(key1) && calismaModeli.getGeceHaftaTatilMesaiParcala())
										vardiyaGun.addCalismaSuresi(-haftaCalismaSuresi);
								}

							}
						}
						izinler = null;
					} catch (Exception ee1) {
						logger.error("Pdks hata in : \n");
						ee1.printStackTrace();
						logger.error("Pdks hata out : " + ee1.getMessage());
						logger.error(vardiyaGun.getVardiyaKey());
					}

				}
				if (flush)
					session.flush();
				if (fazlaMesaiOnayDurum != null)
					dataDenkMap.put("fazlaMesaiOnayDurum", fazlaMesaiOnayDurum);
				for (VardiyaGun vardiyaGun : vardiyalar) {
					String key = vardiyaGun.getVardiyaKeyStr();
					double haftaCalismaSuresi = 0d;
					if (haftaSonuSureMap.containsKey(key)) {
						// String gunStr = PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), "yyyyMMdd").substring(6);
						// if (gunStr.equals("01")) {
						// haftaCalismaSuresi = 0d;
						// }
						haftaCalismaSuresi = haftaSonuSureMap.get(key);
						if (calismaModeli != null && calismaModeli.isFazlaMesaiVarMi())
							vardiyaGun.setHaftaCalismaSuresi(haftaSonuSureMap.get(key));
					}
					if (calismaModeli != null && calismaModeli.isFazlaMesaiVarMi())
						vardiyaGun.setHaftaCalismaSuresi(haftaCalismaSuresi);
				}
				for (VardiyaGun vardiyaGun : vardiyalar) {
					String key = vardiyaGun.getVardiyaKeyStr();
					double haftaCalismaSuresi = 0d;
					if (haftaSonuSureMap.containsKey(key)) {
						haftaCalismaSuresi = haftaSonuSureMap.get(key);
						if (calismaModeli != null && calismaModeli.isFazlaMesaiVarMi())
							vardiyaGun.setHaftaCalismaSuresi(haftaCalismaSuresi);
					}
					if (calismaModeli != null && calismaModeli.isFazlaMesaiVarMi())
						vardiyaGun.setHaftaCalismaSuresi(haftaCalismaSuresi);
				}

				if (adet == 7 || maxSuresi < denklestirmeTasiyici.getToplamCalisilacakZaman())
					denklestirmeTasiyici.setToplamCalisilacakZaman(maxSuresi);

			}
			if (resmiTatilMesai == 0)
				denklestirmeTasiyici.setResmiTatilMesai(0d);
			else
				denklestirmeTasiyici.setResmiTatilMesai(PdksUtil.setSureDoubleTypeRounded(resmiTatilMesai, denklestirmeTasiyici.getYarimYuvarla()));

		}
		dataDenkMap = null;

		return sonVardiyaGun;

	}

	/**
	 * @param arifeSureMap
	 * @param yemeksizSure
	 * @param sure
	 * @param key1
	 * @param key2
	 */
	private void arifeCalismaToplami(LinkedHashMap<String, Double> arifeSureMap, double yemeksizSure, double sure, String key1, String key2) {
		if (arifeSureMap.containsKey(key1))
			arifeSureMap.put(key1, arifeSureMap.get(key1) + sure);
		else
			arifeSureMap.put(key1, sure);
		if (arifeSureMap.containsKey(key2))
			arifeSureMap.put(key2, arifeSureMap.get(key2) + yemeksizSure);
		else
			arifeSureMap.put(key2, yemeksizSure);
	}

	/**
	 * @param izinTipi
	 * @param izinGrupMap
	 * @return
	 */
	public BordroDetayTipi getBordroDetayTipi(IzinTipi izinTipi, TreeMap<String, String> izinGrupMap) {
		String izinKodu = izinTipi.getIzinTipiTanim().getErpKodu();
		if (!izinGrupMap.containsKey(izinKodu))
			izinKodu = null;
		else
			izinKodu = izinGrupMap.get(izinKodu);
		BordroDetayTipi izinBordroDetayTipi = null;
		try {
			if (izinKodu != null)
				izinBordroDetayTipi = BordroDetayTipi.fromValue(izinKodu);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return izinBordroDetayTipi;
	}

	/**
	 * @param session
	 * @return
	 */
	public TreeMap<String, String> getIzinGrupMap(Session session) {
		TreeMap<String, String> izinGrupMap = new TreeMap<String, String>();
		List<Tanim> list;
		HashMap fields = new HashMap();
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
		return izinGrupMap;
	}

	/**
	 * TODO Vardiya yemek molları ayarlanıyor
	 * 
	 * @param vardiyalar
	 * @param yemekGenelList
	 */
	public void setVardiyaYemekList(List<VardiyaGun> vardiyalar, List<YemekIzin> yemekGenelList) {
		if (vardiyalar != null) {
			boolean ozelYemekVar = false;
			if (yemekGenelList != null) {
				for (YemekIzin yemekIzin : yemekGenelList) {
					if (yemekIzin.getVardiyaMap() != null)
						ozelYemekVar = true;
				}
			} else
				yemekGenelList = new ArrayList<YemekIzin>();
			for (VardiyaGun vardiyaGun : vardiyalar) {
				Vardiya vardiya = vardiyaGun.getVardiya();
				List<YemekIzin> yemekList = ozelYemekVar ? new ArrayList<YemekIzin>() : null;
				if (ozelYemekVar == false)
					yemekList = yemekGenelList;
				else {
					if (yemekGenelList != null) {
						for (YemekIzin yemekIzin : yemekGenelList) {
							if (vardiya != null && vardiya.getId() != null) {
								if (yemekIzin.getVardiyaMap() == null || yemekIzin.getVardiyaMap().containsKey(vardiya.getId()))
									yemekList.add(yemekIzin);
							}
						}
					}
				}

				vardiyaGun.setYemekList(yemekList);
			}
		}

	}

	/**
	 * @param perDenkList
	 * @param aylikPuantajDefault
	 * @param tatilGunleriMap
	 * @param session
	 */
	public void personelDenklestirmeDuzenle(List<PersonelDenklestirmeTasiyici> perDenkList, AylikPuantaj aylikPuantajDefault, TreeMap<String, Tatil> tatilGunleriMap, Session session) {
		List<Personel> personeller = new ArrayList<Personel>();
		TreeMap<String, Date> gunMap = new TreeMap<String, Date>();
		Date tarih = (Date) aylikPuantajDefault.getIlkGun().clone();
		Calendar cal = Calendar.getInstance();
		while (tarih.getTime() <= aylikPuantajDefault.getSonGun().getTime()) {
			gunMap.put(PdksUtil.convertToDateString(tarih, "yyyyMMdd"), tarih);
			tarih = tariheGunEkleCikar(cal, tarih, 1);
		}
		DepartmanDenklestirmeDonemi denklestirmeDonemi = new DepartmanDenklestirmeDonemi();
		denklestirmeDonemi.setBaslangicTarih(aylikPuantajDefault.getIlkGun());
		denklestirmeDonemi.setBitisTarih(aylikPuantajDefault.getSonGun());
		TreeMap<Long, PersonelDenklestirmeTasiyici> denklestirmeMap = new TreeMap<Long, PersonelDenklestirmeTasiyici>();
		for (PersonelDenklestirmeTasiyici personelDenklestirmeTasiyici : perDenkList) {
			personeller.add(personelDenklestirmeTasiyici.getPersonel());
			denklestirmeMap.put(personelDenklestirmeTasiyici.getPersonelId(), personelDenklestirmeTasiyici);

		}
		HashMap<Long, List<PersonelIzin>> izinMap = denklestirmeIzinleriOlustur(denklestirmeDonemi, personeller, session);
		try {
			Date bugun = new Date();
			TreeMap<String, VardiyaGun> vardiyaMap = getVardiyalar(personeller, aylikPuantajDefault.getIlkGun(), aylikPuantajDefault.getSonGun(), null, false, session, false);
			List<PersonelIzin> izinler = new ArrayList<PersonelIzin>();
			for (PersonelDenklestirmeTasiyici personelDenklestirmeTasiyici : perDenkList) {
				String perNo = personelDenklestirmeTasiyici.getPersonel().getPdksSicilNo();
				Personel personel = personelDenklestirmeTasiyici.getPersonel();
				if (izinMap.containsKey(personel.getId()))
					izinler.addAll(izinMap.get(personel.getId()));
				personelDenklestirmeTasiyici.setVardiyalar(new ArrayList<VardiyaGun>());
				for (String tarihStr : gunMap.keySet()) {
					String key = perNo + "_" + tarihStr;
					tarih = gunMap.get(tarihStr);
					VardiyaGun vardiyaGun = vardiyaMap.containsKey(key) && personel.isCalisiyorGun(tarih) ? vardiyaMap.get(key) : new VardiyaGun(personel, null, tarih);
					vardiyaGun.setAyinGunu(true);
					if (vardiyaGun.getId() != null) {
						vardiyaGun.setVardiyaZamani();
						if (vardiyaGun.getVardiya().isCalisma())
							vardiyaGun.setZamanGelmedi(vardiyaGun.getSonrakiVardiyaGun() != null && !bugun.after(vardiyaGun.getIslemVardiya().getVardiyaTelorans2BitZaman()));
						if (tatilGunleriMap.containsKey(tarihStr))
							vardiyaGun.setTatil(tatilGunleriMap.get(tarihStr));
						if (vardiyaGun.getVardiyaSaat() != null) {
							VardiyaSaat vardiyaSaat = vardiyaGun.getVardiyaSaat();
							if (vardiyaGun.getDurum() && !vardiyaGun.isZamanGelmedi()) {
								vardiyaGun.setCalismaSuresi(vardiyaSaat.getCalismaSuresi());
								vardiyaGun.setResmiTatilSure(vardiyaSaat.getResmiTatilSure());
							}

						}
					}

					personelDenklestirmeTasiyici.getVardiyalar().add(vardiyaGun);
				}
				List<VardiyaGun> vardiyalar = personelDenklestirmeTasiyici.getVardiyalar();
				try {
					if (!izinler.isEmpty())
						vardiyaIzinleriGuncelle(izinler, vardiyalar);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				for (VardiyaGun vardiyaGun : vardiyalar) {
					if (vardiyaGun.getVardiya() == null || vardiyaGun.getIzin() != null || !vardiyaGun.getIslemVardiya().isCalisma())
						continue;
					if (vardiyaGun.isZamanGelmedi()) {
						if (vardiyaGun.getTatil() == null) {
							vardiyaGun.setCalismaSuresi(vardiyaGun.getVardiya().getNetCalismaSuresi());
						} else if (vardiyaGun.getVardiyaSaat() != null) {
							VardiyaSaat vardiyaSaat = vardiyaGun.getVardiyaSaat();
							vardiyaGun.setResmiTatilSure(vardiyaSaat.getResmiTatilSure());
							vardiyaGun.setCalismaSuresi(vardiyaSaat.getNormalSure());
						}

					}

					vardiyaGun.setZamanGelmedi(Boolean.TRUE);
				}
				izinler.clear();
			}
			izinler = null;
			vardiyaMap = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		denklestirmeMap = null;
		izinMap = null;
		gunMap = null;
	}

	/**
	 * @param hareketKGS
	 * @param manuelId
	 * @param session
	 * @return
	 */
	public HareketKGS getHareketKGS(HareketKGS hareketKGS, Long manuelId, Session session) {
		List<HareketKGS> newList = null;
		if (manuelId > 0) {
			StringBuffer sb = new StringBuffer();
			sb.append("SP_GET_HAREKET_BY_ID_SIRKET");
			LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
			fields.put("kgs", null);
			fields.put("pdks", String.valueOf(manuelId));
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				newList = pdksEntityController.execSPList(fields, sb, BasitHareket.class);
				if (!newList.isEmpty())
					getHareketKGSByBasitHareketList(newList, null, session);

			} catch (Exception e) {
			}

		}
		if (newList != null && !newList.isEmpty())
			hareketKGS = newList.get(0);
		else if (hareketKGS != null && manuelId != null)
			hareketKGS.setId(HareketKGS.AYRIK_HAREKET + manuelId);
		return hareketKGS;
	}

	/**
	 * @param kapiInputList
	 * @param session
	 * @return
	 */
	public HashMap<String, KapiView> getManuelKapiMap(List<KapiView> kapiInputList, Session session) {

		HashMap<String, KapiView> map = new HashMap<String, KapiView>();
		List<KapiView> kapiList = null;
		if (kapiInputList == null)
			kapiList = fillKapiPDKSList(session);
		else
			kapiList = new ArrayList<KapiView>(kapiInputList);
		KapiView girisKapi = null, cikisKapi = null;
		for (Iterator iterator = kapiList.iterator(); iterator.hasNext();) {
			KapiView kapiView = (KapiView) iterator.next();
			if (kapiView.getKapi() == null || !kapiView.getKapiKGS().isPdksManuel())
				iterator.remove();
		}
		if (kapiList.size() == 2) {
			for (KapiView kapiView : kapiList) {
				if (kapiView.getKapi().isGirisKapi())
					girisKapi = kapiView;
				else if (kapiView.getKapi().isCikisKapi())
					cikisKapi = kapiView;
			}
			if (cikisKapi != null && girisKapi != null) {
				map.put(Kapi.TIPI_KODU_GIRIS, girisKapi);
				map.put(Kapi.TIPI_KODU_CIKIS, cikisKapi);
			}
		}
		kapiList = null;
		return map;
	}

	/**
	 * @param dataGirisCikisMap
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public VardiyaGun addManuelGirisCikisHareketler(LinkedHashMap<String, Object> dataGirisCikisMap, Session session) throws Exception {
		Tanim neden = dataGirisCikisMap.containsKey("neden") ? (Tanim) dataGirisCikisMap.get("neden") : null;
		User sistemUser = dataGirisCikisMap.containsKey("sistemUser") ? (User) dataGirisCikisMap.get("sistemUser") : null;
		HashMap<String, KapiView> manuelKapiMap = dataGirisCikisMap.containsKey("manuelKapiMap") ? (HashMap<String, KapiView>) dataGirisCikisMap.get("manuelKapiMap") : null;
		List<VardiyaGun> vardiyalar = dataGirisCikisMap.containsKey("vardiyalar") ? (List<VardiyaGun>) dataGirisCikisMap.get("vardiyalar") : null;
		Boolean hareketKaydet = dataGirisCikisMap.containsKey("hareketKaydet") ? (Boolean) dataGirisCikisMap.get("hareketKaydet") : null;
		VardiyaGun oncekiVardiyaGun = dataGirisCikisMap.containsKey("oncekiVardiyaGun") ? (VardiyaGun) dataGirisCikisMap.get("oncekiVardiyaGun") : null;
		dataGirisCikisMap = null;
		if (neden == null || sistemUser == null) {
			if (PdksUtil.isSistemDestekVar()) {
				neden = getOtomatikKapGirisiNeden(session);
				if (neden != null)
					sistemUser = getSistemAdminUser(session);
			}
		}
		if (sistemUser != null && neden != null) {
			if (manuelKapiMap == null)
				manuelKapiMap = getManuelKapiMap(null, session);
			KapiView girisKapi = manuelKapiMap.get(Kapi.TIPI_KODU_GIRIS), cikisKapi = manuelKapiMap.get(Kapi.TIPI_KODU_CIKIS);
			manuelKapiMap = null;
			if (girisKapi != null && cikisKapi != null) {
				for (VardiyaGun pdksVardiyaGun : vardiyalar) {
					try {
						if (pdksVardiyaGun == null || pdksVardiyaGun.getVardiyaDate() == null)
							continue;
						pdksVardiyaGun.setAyrikHareketVar(Boolean.FALSE);

						if (oncekiVardiyaGun != null && pdksVardiyaGun.getVardiya() != null && oncekiVardiyaGun.getVardiya() != null && oncekiVardiyaGun.isAyinGunu()) {
							if (pdksVardiyaGun.getHareketler() != null && !pdksVardiyaGun.getHareketler().isEmpty()) {
								HareketKGS hareketKGS = pdksVardiyaGun.getHareketler().get(0);
								if (hareketKGS.getKapiView() != null && hareketKGS.getKapiView().getKapi() != null && hareketKGS.getKapiView().getKapi().isCikisKapi()) {
									Vardiya islemVardiya = pdksVardiyaGun.getIslemVardiya(), oncekiIslemVardiya = oncekiVardiyaGun.getIslemVardiya();
									if (islemVardiya != null && (islemVardiya.isCalisma() == false || hareketKGS.getZaman().after(oncekiIslemVardiya.getVardiyaFazlaMesaiBitZaman()))) {
										try {
											Long cikisId = -oncekiVardiyaGun.getId(), girisId = -pdksVardiyaGun.getIdLong();
											HareketKGS manuelCikis = new HareketKGS();
											manuelCikis.setGecerliDegil(Boolean.FALSE);
											manuelCikis.setKapiView(cikisKapi);
											manuelCikis.setPersonel(hareketKGS.getPersonel());
											manuelCikis.setZaman(PdksUtil.getDateTime(oncekiIslemVardiya.getVardiyaFazlaMesaiBitZaman()));
											String aciklama = "";

											if (hareketKaydet)
												cikisId = pdksEntityController.hareketEkle(manuelCikis.getKapiView(), manuelCikis.getPersonel(), manuelCikis.getZaman(), sistemUser, neden.getId(), aciklama, session);
											manuelCikis = getHareketKGS(manuelCikis, cikisId, session);
											oncekiVardiyaGun.addHareket(manuelCikis, hareketKaydet);
											oncekiVardiyaGun.setHareketHatali(false);
											HareketKGS manuelGiris = new HareketKGS();
											manuelGiris.setGecerliDegil(Boolean.FALSE);
											manuelGiris.setKapiView(girisKapi);
											manuelGiris.setPersonel(hareketKGS.getPersonel());
											manuelGiris.setZaman(PdksUtil.getDateTime(islemVardiya.getVardiyaFazlaMesaiBasZaman()));
											if (hareketKaydet)
												girisId = pdksEntityController.hareketEkle(manuelGiris.getKapiView(), manuelGiris.getPersonel(), manuelGiris.getZaman(), sistemUser, neden.getId(), aciklama, session);

											manuelGiris = getHareketKGS(manuelGiris, girisId, session);
											ArrayList<HareketKGS> girisHareketler = new ArrayList<HareketKGS>(), hareketler = new ArrayList<HareketKGS>();
											hareketler.addAll(pdksVardiyaGun.getHareketler());
											if (pdksVardiyaGun.getGirisHareketleri() != null) {
												girisHareketler.addAll(pdksVardiyaGun.getGirisHareketleri());
												pdksVardiyaGun.getGirisHareketleri().clear();
											}
											pdksVardiyaGun.setHareketler(null);
											pdksVardiyaGun.addHareket(manuelGiris, hareketKaydet);
											if (hareketler != null) {
												if (pdksVardiyaGun.getHareketler() == null)
													pdksVardiyaGun.setHareketler(new ArrayList<HareketKGS>());
												pdksVardiyaGun.getHareketler().addAll(hareketler);
											}
											if (!girisHareketler.isEmpty())
												pdksVardiyaGun.getGirisHareketleri().addAll(girisHareketler);
											oncekiVardiyaGun.setAyrikHareketVar(Boolean.TRUE);
											pdksVardiyaGun.setAyrikHareketVar(Boolean.TRUE);
										} catch (Exception e) {
											logger.error(pdksVardiyaGun.getVardiyaKeyStr() + " " + PdksUtil.getCurrentTimeStampStr());
										}

									}
								}
							}
						}
						boolean hataVar = false;
						oncekiVardiyaGun = null;
						if (pdksVardiyaGun.isAyinGunu() && pdksVardiyaGun.getHareketler() != null && pdksVardiyaGun.getHareketler().size() % 2 == 1) {
							HareketKGS hareketKGS = pdksVardiyaGun.getHareketler().get(pdksVardiyaGun.getHareketler().size() - 1);
							if (hareketKGS.getKapiView() != null && hareketKGS.getKapiView().getKapi() != null && hareketKGS.getKapiView().getKapi().isGirisKapi())
								hataVar = true;

						}

						if (hataVar) {
							oncekiVardiyaGun = pdksVardiyaGun;

						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}

		return oncekiVardiyaGun;
	}

	/**
	 * @param session
	 * @return
	 */
	public Tanim getOtomatikKapGirisiNeden(Session session) {
		HashMap fields = new HashMap();
		fields.put("tipi", Tanim.TIPI_HAREKET_NEDEN);
		fields.put("kodu", "sys");
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		Tanim neden = (Tanim) pdksEntityController.getObjectByInnerObject(fields, Tanim.class);
		return neden;
	}

	/**
	 * @param dataMap
	 * @param vardiyaGun
	 * @param personelFazlaMesai
	 * @param yemekList
	 * @param session
	 */
	private void haftaTatilMesaiHesapla(HashMap<String, Double> dataMap, VardiyaGun vardiyaGun, PersonelFazlaMesai personelFazlaMesai, List<YemekIzin> yemekList, Session session) {
		Double calismaSuresi = dataMap.get("calismaSuresi");
		Double haftaCalismaSuresi = dataMap.get("haftaCalismaSuresi");
		Calendar cal = Calendar.getInstance();
		Date haftaTatil = tariheGunEkleCikar(cal, vardiyaGun.getVardiyaDate(), 1);
		Double fazlaMesaiSaati = personelFazlaMesai.getFazlaMesaiSaati();
		double calisilmayanAksamSure = 0;
		if (haftaTatil.before(personelFazlaMesai.getBitZaman()) && personelFazlaMesai.isBayram() == false) {
			double calismaToplamSuresi = fazlaMesaiSaati;
			Date basTarih = personelFazlaMesai.getBasZaman();
			// if (vardiyaGun.getSonrakiVardiya() != null) {
			// Vardiya islemVardiya = vardiyaGun.getSonrakiVardiya();
			// if (islemVardiya.getVardiyaTelorans1BasZaman().before(basTarih))
			// basTarih = islemVardiya.getVardiyaBasZaman();
			// }
			double haftaSuresi = PdksUtil.setSureDoubleTypeRounded(getSaatSure(basTarih, haftaTatil, yemekList, vardiyaGun, session), vardiyaGun.getYarimYuvarla());
			if (haftaSuresi > calismaToplamSuresi)
				haftaSuresi = calismaToplamSuresi;
			else
				calisilmayanAksamSure = calismaToplamSuresi - haftaSuresi;

			calismaSuresi += calisilmayanAksamSure;

			haftaCalismaSuresi += haftaSuresi;
		} else {
			haftaCalismaSuresi += fazlaMesaiSaati;

		}
		vardiyaGun.setCalisilmayanAksamSure(calisilmayanAksamSure);
		dataMap.put("haftaCalismaSuresi", haftaCalismaSuresi);
		dataMap.put("calismaSuresi", calismaSuresi);
	}

	/**
	 * @param vardiyaList
	 * @param session
	 */
	@Transactional
	public void otomatikHareketEkle(List<VardiyaGun> vardiyaList, Session session) {
		boolean kartOkuyucuDurum = getParameterKey("kartOkuyucuDurum").equals("0");
		if (kartOkuyucuDurum && vardiyaList != null) {
			HashMap<String, KapiView> manuelKapiMap = getManuelKapiMap(null, session);
			KapiView girisKapi = manuelKapiMap.get(Kapi.TIPI_KODU_GIRIS), cikisKapi = manuelKapiMap.get(Kapi.TIPI_KODU_CIKIS);
			HashMap fields = new HashMap();
			Date bugun = new Date();
			Boolean flush = Boolean.FALSE;
			for (VardiyaGun pdksVardiyaGun : vardiyaList) {
				if (pdksVardiyaGun.getVardiya() == null || !pdksVardiyaGun.getVardiya().isCalisma() || pdksVardiyaGun.getIzin() != null) {
					continue;
				}
				if (!pdksVardiyaGun.isZamanGelmedi()) {
					if (pdksVardiyaGun.getHareketler() == null || pdksVardiyaGun.getHareketler().isEmpty()) {
						if (pdksVardiyaGun.getIslemVardiya() != null && pdksVardiyaGun.getIslemVardiya().isCalisma() && bugun.after(pdksVardiyaGun.getIslemVardiya().getVardiyaBitZaman())) {
							if (girisKapi == null) {
								fields.clear();
								fields.put("kapi.durum", Boolean.TRUE);
								fields.put("kapi.pdks", Boolean.TRUE);
								fields.put("kapi.tipi.kodu", Kapi.TIPI_KODU_GIRIS);
								if (session != null)
									fields.put(PdksEntityController.MAP_KEY_SESSION, session);
								girisKapi = getKapiView(fields);
							}
							if (cikisKapi == null) {
								fields.clear();
								fields.put("kapi.durum", Boolean.TRUE);
								fields.put("kapi.pdks", Boolean.TRUE);
								fields.put("kapi.tipi.kodu", Kapi.TIPI_KODU_CIKIS);
								if (session != null)
									fields.put(PdksEntityController.MAP_KEY_SESSION, session);
								cikisKapi = getKapiView(fields);
							}
							HareketKGS hareketGiris = pdksEntityController.hareketSistemEkleReturn(girisKapi, pdksVardiyaGun.getPersonel().getPersonelKGS(), pdksVardiyaGun.getIslemVardiya().getVardiyaBasZaman(), session);
							if (hareketGiris != null) {
								HareketKGS hareketCikis = pdksEntityController.hareketSistemEkleReturn(cikisKapi, pdksVardiyaGun.getPersonel().getPersonelKGS(), pdksVardiyaGun.getIslemVardiya().getVardiyaBitZaman(), session);
								pdksVardiyaGun.addHareket(hareketGiris, Boolean.TRUE);
								pdksVardiyaGun.addHareket(hareketCikis, Boolean.TRUE);
							}
							flush = Boolean.TRUE;
						}
					}
				}
			}
			if (flush)
				session.flush();
		}

	}

	/**
	 * @param vardiyaGun
	 * @param personelIzin
	 */
	public PersonelIzin setIzinDurum(VardiyaGun vardiyaGun, PersonelIzin personelIzinInput) {
		Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
		PersonelIzin personelIzin = personelIzinInput != null ? (PersonelIzin) personelIzinInput.clone() : null;
		String izinVardiyaKontrolStr = getParameterKey("izinVardiyaKontrol");
		IzinTipi izinTipi = personelIzinInput != null ? personelIzinInput.getIzinTipi() : null;
		boolean tatilSay = izinTipi.isTatilSayilir();
		boolean cumaCumartesiTekIzinSay = izinTipi != null ? izinTipi.isCumaCumartesiTekIzinSaysin() : false;
		boolean izinVardiyaKontrol = PdksUtil.hasStringValue(izinVardiyaKontrolStr), izinERPUpdate = getParameterKey("izinERPUpdate").equals("1");
		boolean takvimGunu = personelIzin != null && personelIzin.getIzinTipi().isTakvimGunuMu();
		boolean offDahil = takvimGunu == false && personelIzin != null && personelIzin.getIzinTipi().isOffDahilMi();
		boolean offVardiya = offDahil && vardiyaGun != null && islemVardiya != null && vardiyaGun.getVardiya().isOff();
		Double sure = personelIzin.getIzinSuresi();
		Calendar cal = Calendar.getInstance();
		try {
			boolean girisYok = izinTipi.getPersonelGirisTipi() == null || izinTipi.getPersonelGirisTipi().equals(IzinTipi.GIRIS_TIPI_YOK);
			Tatil tatil = girisYok == false ? vardiyaGun.getTatil() : null;
			String vardiyaDateStr = vardiyaGun.getVardiyaDateStr();
			boolean vardiyaIzin = vardiyaGun.getVardiya().isIzin();
			if (personelIzin != null && vardiyaGun != null && islemVardiya != null && vardiyaGun.getPersonel().getId().equals(personelIzin.getIzinSahibi().getId())) {
				BordroDetayTipi bordroDetayTipi = null;
				if (vardiyaIzin && PdksUtil.hasStringValue(islemVardiya.getStyleClass()))
					bordroDetayTipi = BordroDetayTipi.fromValue(islemVardiya.getStyleClass());

				if (vardiyaIzin == false || bordroDetayTipi == null) {
					Date vardiyaDate = vardiyaGun.getVardiyaDate();
					if (!personelIzin.getIzinTipi().getPersonelGirisTipi().equals(IzinTipi.GIRIS_TIPI_YOK))
						izinERPUpdate = false;
					Date baslangicZamani = PdksUtil.getDate(personelIzin.getBaslangicZamani());
					Date bitisZamani = PdksUtil.getDate(izinVardiyaKontrol ? tariheGunEkleCikar(cal, personelIzin.getBitisZamani(), -Integer.parseInt(izinVardiyaKontrolStr)) : personelIzin.getBitisZamani());
					if (!izinERPUpdate) {
						baslangicZamani = personelIzin.getBaslangicZamani();
						bitisZamani = personelIzin.getBitisZamani();

					}

					boolean kontrol = false;
					Date sonGun = PdksUtil.getDate(bitisZamani);
					if (izinERPUpdate == false) {
						kontrol = PdksUtil.getDate(bitisZamani).getTime() > islemVardiya.getVardiyaBasZaman().getTime() && baslangicZamani.getTime() <= islemVardiya.getVardiyaBitZaman().getTime();

					} else
						kontrol = bitisZamani.getTime() >= vardiyaDate.getTime() && baslangicZamani.getTime() <= vardiyaDate.getTime();
					if (tatil != null) {
						if (vardiyaDateStr.equals("20240101"))
							logger.debug(personelIzin.getId());
						if (tatilSay)
							tatil = null;

					}
					if (kontrol == false && !izinTipi.getPersonelGirisTipi().equals(IzinTipi.GIRIS_TIPI_YOK) && islemVardiya.isCalisma() == false) {
						kontrol = sonGun.after(vardiyaDate);
					}
					if (tatil == null && (kontrol || vardiyaIzin)) {
						PersonelIzin izin = (PersonelIzin) personelIzin.clone();
						int gunlukOldu = 0;
						int bitisDeger = PdksUtil.tarihKarsilastirNumeric(bitisZamani, vardiyaDate);
						int baslangicDeger = PdksUtil.tarihKarsilastirNumeric(vardiyaDate, izin.getBaslangicZamani());
						if (bitisZamani.getTime() >= islemVardiya.getVardiyaBitZaman().getTime() || (izinVardiyaKontrol && bitisDeger != -1 && izin.isGunlukIzin())) {
							++gunlukOldu;
							if (islemVardiya.isCalisma() && !izinERPUpdate)
								izin.setBitisZamani(islemVardiya.getVardiyaBitZaman());
						}
						if (baslangicZamani.getTime() <= islemVardiya.getVardiyaBasZaman().getTime() || (izinVardiyaKontrol && baslangicDeger != -1 && izin.isGunlukIzin())) {
							++gunlukOldu;
							if (islemVardiya.isCalisma() && !izinERPUpdate)
								izin.setBaslangicZamani(islemVardiya.getVardiyaBasZaman());
						}
						boolean gunIzin = gunlukOldu == 2;
						izin.setGunlukOldu(gunIzin);
						PersonelIzin personelIzin2 = izin;
						boolean izinDurum = true;

						if (cumaCumartesiTekIzinSay && gunIzin && offVardiya && sure.intValue() == 1) {
							if (vardiyaGun.getTatil() != null || PdksUtil.getDate(personelIzinInput.getBitisZamani()).getTime() == vardiyaGun.getVardiyaDate().getTime()) {
								izinDurum = false;
								gunIzin = false;
							}

						}

						if (gunIzin) {
							if (izinVardiyaKontrol) {
								baslangicZamani = islemVardiya.getVardiyaBasZaman();
								if (vardiyaGun.getOncekiVardiyaGun() != null && vardiyaGun.getOncekiVardiyaGun().getIzin() != null)
									baslangicZamani = tariheGunEkleCikar(cal, vardiyaGun.getOncekiVardiyaGun().getIzin().getBaslangicZamani(), 1);
								izin.setBaslangicZamani(baslangicZamani);
								izin.setBitisZamani(islemVardiya.getVardiyaBitZaman());
							}
							if (islemVardiya.isOff() && izinTipi.isOffDahilMi() == false)
								izinDurum = false;
							if (islemVardiya.isHaftaTatil() && izinTipi.isHTDahil() == false)
								izinDurum = false;

							if (!izinTipi.getPersonelGirisTipi().equals(IzinTipi.GIRIS_TIPI_YOK) && islemVardiya.isCalisma() == false) {
								izinDurum = sonGun.after(vardiyaDate);
							}

							if (izinDurum) {
								personelIzin2 = izin.setVardiyaIzin(vardiyaGun);
								personelIzin2.setOrjIzin(personelIzin);
							}

						} else {
							// vardiyaGun.setIzin(null);

						}
						if (izinDurum)
							vardiyaGun.addPersonelIzin(personelIzin2);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return vardiyaGun.getIzin();
	}

	/**
	 * @param map
	 * @return
	 */
	public LinkedHashMap<String, Object> mapBosVeriSil(LinkedHashMap<String, Object> map, String fonksiyon) {
		if (map != null && !map.isEmpty()) {
			List<String> list = new ArrayList<String>(map.keySet());
			for (String key : list) {
				Object object = map.get(key);
				if (object == null) {
					map.remove(key);
					logger.debug(fonksiyon + " " + key);
				}

			}
		}
		return map;
	}

	/**
	 * @param denklestirmeMap
	 * @param session
	 */
	@Transactional
	public void denklestirmeOlustur(LinkedHashMap<String, Object> denklestirmeMap, Session session) {
		Tanim neden = denklestirmeMap.containsKey("neden") ? (Tanim) denklestirmeMap.get("neden") : null;
		User sistemUser = denklestirmeMap.containsKey("sistemUser") ? (User) denklestirmeMap.get("sistemUser") : null;
		User loginUser = denklestirmeMap.containsKey("loginUser") ? (User) denklestirmeMap.get("loginUser") : authenticatedUser;
		HashMap<String, KapiView> manuelKapiMap = denklestirmeMap.containsKey("manuelKapiMap") ? (HashMap<String, KapiView>) denklestirmeMap.get("manuelKapiMap") : null;
		TreeMap<String, Boolean> gunMap = denklestirmeMap.containsKey("gunMap") ? (TreeMap<String, Boolean>) denklestirmeMap.get("gunMap") : null;
		Boolean hareketEkle = denklestirmeMap.containsKey("hareketEkle") ? (Boolean) denklestirmeMap.get("hareketEkle") : null;
		List<YemekIzin> yemekAraliklari = denklestirmeMap.containsKey("yemekAraliklari") ? (List<YemekIzin>) denklestirmeMap.get("yemekAraliklari") : null;
		KapiView girisView = denklestirmeMap.containsKey("girisView") ? (KapiView) denklestirmeMap.get("girisView") : null;
		List<PersonelDenklestirmeTasiyici> personelDenklestirmeTasiyiciList = denklestirmeMap.containsKey("personelDenklestirmeTasiyiciList") ? (List<PersonelDenklestirmeTasiyici>) denklestirmeMap.get("personelDenklestirmeTasiyiciList") : null;
		TreeMap<String, Tatil> tatilGunleriMap = denklestirmeMap.containsKey("tatilGunleriMap") ? (TreeMap<String, Tatil>) denklestirmeMap.get("tatilGunleriMap") : null;
		TreeMap<Long, PersonelDenklestirmeTasiyici> personelDenklestirmeMap = denklestirmeMap.containsKey("personelDenklestirmeMap") ? (TreeMap<Long, PersonelDenklestirmeTasiyici>) denklestirmeMap.get("personelDenklestirmeMap") : null;
		HashMap<Long, Double> vardiyaNetCalismaSuresiMap = denklestirmeMap.containsKey("vardiyaNetCalismaSuresiMap") ? (HashMap<Long, Double>) denklestirmeMap.get("vardiyaNetCalismaSuresiMap") : null;
		List<PersonelIzin> izinler = denklestirmeMap.containsKey("izinler") ? (List<PersonelIzin>) denklestirmeMap.get("izinler") : null;
		List<PersonelFazlaMesai> fazlaMesailer = denklestirmeMap.containsKey("fazlaMesailer") ? (List<PersonelFazlaMesai>) denklestirmeMap.get("fazlaMesailer") : null;
		HashMap<Long, ArrayList<VardiyaGun>> calismaPlaniMap = denklestirmeMap.containsKey("calismaPlaniMap") ? (HashMap<Long, ArrayList<VardiyaGun>>) denklestirmeMap.get("calismaPlaniMap") : null;
		List<HareketKGS> perHareketList = denklestirmeMap.containsKey("perHareketList") ? (List<HareketKGS>) denklestirmeMap.get("perHareketList") : null;
		Long perNoId = denklestirmeMap.containsKey("perNoId") ? (Long) denklestirmeMap.get("perNoId") : null;
		List<YemekIzin> yemekList = denklestirmeMap.containsKey("yemekList") ? (List<YemekIzin>) denklestirmeMap.get("yemekList") : null;
		boolean testDurum = PdksUtil.getTestDurum() && false;
		boolean hareketKopyala = getParameterKey("kapiGirisHareketKopyala").equals("1");
		boolean kapiDegistirKontrol = false;
		if (perHareketList != null) {
			for (HareketKGS hareketKGS : perHareketList) {
				if (!kapiDegistirKontrol)
					kapiDegistirKontrol = hareketKGS.getKapiKGS().isKapiDegistirir();
				if (kapiDegistirKontrol)
					break;

			}
		}
		denklestirmeMap = null;
		if (testDurum)
			logger.info(perNoId + " denklestirmeOlustur 0100 " + getCurrentTimeStampStr());
		PersonelDenklestirmeTasiyici personelDenklestirmeTasiyici = personelDenklestirmeMap.get(perNoId);
		Date simdikiZaman = new Date();
		double normalFazlaMesai = 0, resmiTatilMesai = 0;
		if (perHareketList != null && perHareketList.size() > 1)
			perHareketList = PdksUtil.sortListByAlanAdi(perHareketList, "zaman", Boolean.FALSE);
		KapiView girisKapi = null, cikisKapi = null;
		boolean kartOkuyucuDurum = getParameterKey("kartOkuyucuDurum").equals("0"), hareketDurum = false;
		if (manuelKapiMap != null) {
			if (hareketEkle || kartOkuyucuDurum) {
				girisKapi = manuelKapiMap.get(Kapi.TIPI_KODU_GIRIS);
				cikisKapi = manuelKapiMap.get(Kapi.TIPI_KODU_CIKIS);
				if (girisView == null)
					girisView = girisKapi;
			}
		}
		kartOkuyucuDurum = kartOkuyucuDurum && girisKapi != null && cikisKapi != null;
		if (kartOkuyucuDurum)
			hareketDurum = perHareketList == null || perHareketList.isEmpty();

		ArrayList<VardiyaGun> vardiyaGunList = calismaPlaniMap.containsKey(perNoId) ? calismaPlaniMap.get(perNoId) : null;

		// Vardiya günleri bilgileri denklestirme objesine set ediliyor
		VardiyaGun oncekiVardiya = null;
		String bayramEkle = getParameterKey("bayramEkle");
		Boolean bayramEkleDurum = bayramEkle != null && bayramEkle.equals("+");
		Date bugun = new Date();
		if (vardiyaGunList != null) {

			if (hareketDurum) {

				for (Iterator<VardiyaGun> iterator = vardiyaGunList.iterator(); iterator.hasNext();) {
					VardiyaGun vardiyaGun = iterator.next();

					if (vardiyaGun.getIslemVardiya() != null && vardiyaGun.getIslemVardiya().isCalisma() && bugun.after(vardiyaGun.getIslemVardiya().getVardiyaBitZaman())) {
						HareketKGS hareketGiris = pdksEntityController.hareketSistemEkleReturn(girisKapi, vardiyaGun.getPersonel().getPersonelKGS(), vardiyaGun.getIslemVardiya().getVardiyaBasZaman(), session);
						if (hareketGiris != null) {
							HareketKGS hareketCikis = pdksEntityController.hareketSistemEkleReturn(cikisKapi, vardiyaGun.getPersonel().getPersonelKGS(), vardiyaGun.getIslemVardiya().getVardiyaBitZaman(), session);
							if (perHareketList == null)
								perHareketList = new ArrayList<HareketKGS>();
							perHareketList.add(hareketGiris);
							perHareketList.add(hareketCikis);
						}
					}

				}
			}
		}
		if (vardiyaGunList != null) {
			DenklestirmeAy denklestirmeAy = personelDenklestirmeTasiyici.getDenklestirmeAy();
			CalismaModeliAy calismaModeliAy = personelDenklestirmeTasiyici.getCalismaModeliAy();
			boolean otomatikFazlaCalismaOnaylansin = denklestirmeAy.isDurum(loginUser) && calismaModeliAy != null && calismaModeliAy.isOtomatikFazlaCalismaOnaylansinmi();
			vardiyaIzinleriGuncelle(izinler, vardiyaGunList);
			HashMap<Long, KapiKGS> hareketKapiUpdateMap = new HashMap<Long, KapiKGS>();
			String donem = denklestirmeAy != null ? String.valueOf(denklestirmeAy.getYil() * 100 + denklestirmeAy.getAy()) : null;
			for (Iterator<VardiyaGun> iterator = vardiyaGunList.iterator(); iterator.hasNext();) {
				VardiyaGun vardiyaGun = iterator.next();
				vardiyaGun.setFiiliHesapla(Boolean.TRUE);
				vardiyaGun.setZamanGelmedi(Boolean.FALSE);
				// Fazla mesailer vardiya gününe ekleniyor
				vardiyaGun.setFazlaMesailer(null);

				String key = vardiyaGun.getVardiyaDateStr();
				if (donem != null)
					vardiyaGun.setAyinGunu(key.startsWith(donem));
				boolean otomatikOnayKontrol = otomatikFazlaCalismaOnaylansin && vardiyaGun.isAyinGunu() && vardiyaGun.getVardiya() != null && vardiyaGun.getVardiya().isCalisma();
				HashMap<String, PersonelFazlaMesai> mesaiMap = new HashMap<String, PersonelFazlaMesai>();
				for (Iterator<PersonelFazlaMesai> iterator4 = fazlaMesailer.iterator(); iterator4.hasNext();) {
					PersonelFazlaMesai personelFazlaMesai = iterator4.next();
					if (personelFazlaMesai.getVardiyaGun() != null && personelFazlaMesai.getVardiyaGun().getId().equals(vardiyaGun.getId())) {
						vardiyaGun.addPersonelFazlaMesai(personelFazlaMesai);
						mesaiMap.put(personelFazlaMesai.getHareketId(), personelFazlaMesai);
						// iterator4.remove();
					}

				}

				vardiyaGun.setCalismaSuresi(0);
				vardiyaGun.setNormalSure(0);
				vardiyaGun.setResmiTatilSure(0);
				vardiyaGun.setBayramCalismaSuresi(0);
				vardiyaGun.setCalisilmayanAksamSure(0d);
				vardiyaGun.setCalisilmayanAksamSure(0d);
				vardiyaGun.setHaftaCalismaSuresi(0d);
				vardiyaGun.setHareketler(null);
				vardiyaGun.setYemekHareketleri(null);
				vardiyaGun.setGirisHareketleri(null);
				vardiyaGun.setCikisHareketleri(null);
				vardiyaGun.setOncekiVardiyaGun(oncekiVardiya);
				Vardiya sonrakiVardiya = vardiyaGun.getSonrakiVardiya();

				vardiyaGun.setSonrakiVardiya(null);
				if (vardiyaGun.getVardiya() != null)
					vardiyaGun.setVardiyaZamani();

				vardiyaGun.setSonrakiVardiya(sonrakiVardiya);

				try {

					boolean tarihGecti = hareketEkle && vardiyaGun.getIzin() == null && vardiyaGun.isAyinGunu();
					if (tarihGecti) {
						if (vardiyaGun.getVardiya() != null && vardiyaGun.getVardiya().isCalisma())
							tarihGecti = simdikiZaman.getTime() < vardiyaGun.getIslemVardiya().getVardiyaTelorans2BitZaman().getTime();
						else
							tarihGecti = Boolean.FALSE;
					}

					vardiyaGun.setZamanGelmedi(tarihGecti);

					// Personel giris çikis hareket bilgileri vardiya gününe
					// ekleniyor

					if (perHareketList != null) {
						boolean bagliKapiVar = false;
						List<HareketKGS> hareketList = new ArrayList<HareketKGS>();
						for (Iterator<HareketKGS> iterator5 = perHareketList.iterator(); iterator5.hasNext();) {
							HareketKGS hareket = iterator5.next();
							if (hareket.getIslem() != null && (hareket.getIslem().getIslemTipi() == null || hareket.getIslem().getIslemTipi().equals("D"))) {
								iterator5.remove();
								continue;
							}
							if (mesaiMap.containsKey(hareket.getId())) {
								hareket.setPersonelFazlaMesai(mesaiMap.get(hareket.getId()));
								mesaiMap.remove(hareket.getId());
							}
							try {
								if (vardiyaGun.addHareket(hareket, Boolean.TRUE)) {
									// TODO isOtomatikFazlaCalismaOnaylansinmi GETİR
									List<HareketKGS> vardiyaHareketler = null;
									hareket.setOrjinalZamanGetir(vardiyaGun.getVardiya().isCalisma() == false);
									if (otomatikOnayKontrol && hareket.getKapiView() != null && hareket.getKapiView().getKapi() != null)
										vardiyaHareketler = hareket.getKapiView().getKapi().isGirisKapi() ? vardiyaGun.getGirisHareketleri() : vardiyaGun.getCikisHareketleri();
									if (vardiyaHareketler != null && !vardiyaHareketler.isEmpty()) {
										HareketKGS yeniHareket = vardiyaHareketler.get(vardiyaHareketler.size() - 1);
										if (yeniHareket.getId().equals(hareket.getId())) {
											Date zaman = yeniHareket.getZaman(), orjinalZaman = yeniHareket.getOrjinalZaman();
											if (yeniHareket.getOrjinalZaman() != null && zaman.getTime() != orjinalZaman.getTime()) {
												Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
												if (islemVardiya.getVardiyaTelorans1BasZaman().after(orjinalZaman) || islemVardiya.getVardiyaTelorans2BitZaman().before(orjinalZaman))
													yeniHareket.setOrjinalZamanGetir(otomatikFazlaCalismaOnaylansin);
											}
										}
										hareket.setOrjinalZamanGetir(yeniHareket.isOrjinalZamanGetir());
									}
									hareketList.add(hareket);
									if (kapiDegistirKontrol) {
										KapiKGS kapiKGS = hareket.getKapiKGS();
										if (!bagliKapiVar)
											bagliKapiVar = kapiKGS != null && kapiKGS.getBagliKapiKGS() != null;
									}
									iterator5.remove();
								}
							} catch (Exception ex) {
								logger.error(vardiyaGun.getVardiyaKeyStr());
								ex.printStackTrace();
							}

						}
						if (bagliKapiVar && denklestirmeAy.getDurum() && vardiyaGun.isAyinGunu()) {
							int adet = hareketList.size();
							boolean hareketHatali = false;
							if (adet > 0 && adet % 2 == 0) {
								boolean giris = false;
								for (Iterator iterator2 = hareketList.iterator(); iterator2.hasNext();) {
									HareketKGS hareketKGS = (HareketKGS) iterator2.next();
									giris = !giris;
									KapiKGS kapiKGS = hareketKGS.getKapiKGS();
									KapiKGS bagliKapiKGS = kapiKGS.getBagliKapiKGS();
									if (bagliKapiKGS != null) {
										Kapi kapi = kapiKGS.getKapi();
										boolean girisDurum = kapi != null;
										if (girisDurum) {
											if (giris)
												girisDurum = kapi.isGirisKapi() == false;
											else
												girisDurum = kapi.isCikisKapi() == false;
										}
										if (girisDurum && hareketKGS.getId().startsWith(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_KGS)) {
											hareketHatali = true;
											hareketKGS.setKapiKGS(bagliKapiKGS);
											hareketKapiUpdateMap.put(hareketKGS.getHareketTableId(), bagliKapiKGS);
											logger.debug(vardiyaGun.getVardiyaKeyStr() + " " + hareketKGS.getId());
										}
									}

								}
							}
							if (hareketHatali) {
								vardiyaGun.setHareketler(null);
								vardiyaGun.setYemekHareketleri(null);
								vardiyaGun.setGirisHareketleri(null);
								vardiyaGun.setCikisHareketleri(null);
								for (Iterator iterator2 = hareketList.iterator(); iterator2.hasNext();) {
									HareketKGS hareket = (HareketKGS) iterator2.next();
									vardiyaGun.addHareket(hareket, Boolean.TRUE);
								}
							}
						}
						hareketList = null;
						if (kartOkuyucuDurum && vardiyaGun.getIzin() == null && !vardiyaGun.isZamanGelmedi()) {
							if (vardiyaGun.getHareketler() == null || vardiyaGun.getHareketler().isEmpty()) {
								if (vardiyaGun.getIslemVardiya() != null && vardiyaGun.getIslemVardiya().isCalisma() && bugun.after(vardiyaGun.getIslemVardiya().getVardiyaBitZaman())) {
									HareketKGS hareketGiris = pdksEntityController.hareketSistemEkleReturn(girisKapi, vardiyaGun.getPersonel().getPersonelKGS(), vardiyaGun.getIslemVardiya().getVardiyaBasZaman(), session);
									if (hareketGiris != null) {
										HareketKGS hareketCikis = pdksEntityController.hareketSistemEkleReturn(cikisKapi, vardiyaGun.getPersonel().getPersonelKGS(), vardiyaGun.getIslemVardiya().getVardiyaBitZaman(), session);
										vardiyaGun.addHareket(hareketGiris, Boolean.TRUE);
										vardiyaGun.addHareket(hareketCikis, Boolean.TRUE);
									}
								}
							}
						}

						try {
							if (vardiyaGun.getIzin() == null && vardiyaGun.getIzinler() != null && !vardiyaGun.getIzinler().isEmpty()) {
								if (vardiyaGun.getHareketler() != null && !vardiyaGun.getHareketler().isEmpty()) {
									if (vardiyaGun.getGirisHareket() != null && vardiyaGun.getCikisHareketleri() != null && vardiyaGun.getGirisHareketleri().size() == vardiyaGun.getCikisHareketleri().size())
										vardiyaGun.hareketIcindekiIzinlereHareketEkle();
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						if (vardiyaGun.isZamanGelmedi()) {
							// TODO Gün gelmediğinde manuel giriş çıkış ekleme
							tarihGecti = Boolean.FALSE;
							Vardiya vardiya = vardiyaGun.getIslemVardiya();
							PersonelView personelView = new PersonelView();
							personelView.setPdksPersonel(vardiyaGun.getPersonel());
							personelView.setPersonelKGS(vardiyaGun.getPersonel().getPersonelKGS());
							if (vardiyaGun.getGirisHareketleri() == null) {
								tarihGecti = Boolean.TRUE;
								HareketKGS giris = new HareketKGS();
								giris.setZaman(vardiya.getVardiyaBasZaman());
								giris.setGecerliDegil(Boolean.TRUE);
								giris.setId(HareketKGS.SANAL_HAREKET + vardiyaGun.getId() + "" + vardiya.getVardiyaBasZaman().getTime());
								giris.setPersonel(personelView);
								giris.setKapiView(girisKapi);
								boolean eklendi = vardiyaGun.addHareket(giris, Boolean.FALSE);
								if (!eklendi) {
									PdksUtil.addMessageAvailableWarn(vardiyaGun.getPersonel().getPdksSicilNo() + " " + vardiyaGun.getPersonel().getAdSoyad() + " " + PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), "d MMMMM") + " giriş eklenemedi!");
									vardiyaGun.hareketKontrolZamansiz(giris, Boolean.FALSE);
								}

							}
							if (vardiyaGun.getCikisHareketleri() == null) {
								tarihGecti = Boolean.TRUE;
								HareketKGS cikis = new HareketKGS();
								cikis.setGecerliDegil(Boolean.TRUE);
								cikis.setId(HareketKGS.SANAL_HAREKET + vardiyaGun.getId() + "" + vardiya.getVardiyaBitZaman().getTime());
								Date zaman = vardiya.getVardiyaBitZaman();
								if (vardiyaGun.getIslemVardiya() != null && vardiyaGun.getIslemVardiya().getVardiyaFazlaMesaiBitZaman().before(zaman))
									zaman = vardiyaGun.getIslemVardiya().getVardiyaFazlaMesaiBitZaman();
								cikis.setZaman(zaman);
								cikis.setPersonel(personelView);
								cikis.setKapiView(cikisKapi);
								boolean eklendi = vardiyaGun.addHareket(cikis, Boolean.FALSE);
								if (!eklendi) {
									if (vardiyaGun.getVardiyaDate().before(bugun)) {
										PdksUtil.addMessageAvailableWarn(vardiyaGun.getPersonel().getPdksSicilNo() + " " + vardiyaGun.getPersonel().getAdSoyad() + " " + PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), "d MMMMM") + " çıkış eklenemedi!");
									}
									vardiyaGun.hareketKontrolZamansiz(cikis, Boolean.FALSE);
								}

							}
							vardiyaGun.setZamanGelmedi(tarihGecti);
							// if (tarihGecti)
							// logger.info(vardiyaGun.getVardiyaKeyStr() + " " +
							// tarihGecti);
						}

						if (vardiyaGun.getVardiya().isCalisma() && vardiyaGun.getHareketDurum() && vardiyaGun.getTatil() != null) {
							bayramSanalHareketiEkle(vardiyaGun, yemekAraliklari, bayramEkleDurum);

						}
					}

					mesaiMap = null;
					personelDenklestirmeTasiyici.setVardiyaGun(vardiyaGun);
					iterator.remove();
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());
				}
				oncekiVardiya = (VardiyaGun) vardiyaGun.clone();

			}
			if (!hareketKapiUpdateMap.isEmpty()) {
				hareketKapiUpdate(hareketKapiUpdateMap, hareketKopyala, loginUser, session);
			}
			hareketKapiUpdateMap = null;
		}

		if (personelDenklestirmeTasiyici.getVardiyaHaftaMap() != null) {
			List<TreeMap> vardiyaHaftaList = new ArrayList<TreeMap>(personelDenklestirmeTasiyici.getVardiyaHaftaMap().values());
			int denklestirmeHaftasi = 0;
			// Personel bilgileri denklestiriliyor
			personelDenklestirmeTasiyici.setPersonelDenklestirmeleri(new ArrayList<PersonelDenklestirmeTasiyici>());
			normalFazlaMesai = 0;
			resmiTatilMesai = 0;
			int yarimYuvarla = 1;
			VardiyaGun sonVardiyaGun = null, oncekiVardiyaGun = null;
			for (TreeMap vardiyaMap : vardiyaHaftaList) {
				normalFazlaMesai = 0;
				++denklestirmeHaftasi;
				PersonelDenklestirmeTasiyici denklestirme = new PersonelDenklestirmeTasiyici();
				denklestirme.setCalismaModeli(personelDenklestirmeTasiyici.getCalismaModeli());
				denklestirme.setOncekiVardiyaGun(oncekiVardiyaGun);
				denklestirme.setPersonel(personelDenklestirmeTasiyici.getPersonel());
				if (personelDenklestirmeTasiyici.getVardiyaGunleriMap() != null) {
					TreeMap<String, VardiyaGun> vardiyaGunleriMap = personelDenklestirmeTasiyici.getVardiyaGunleriMap();
					boolean ayinGunumu = false, ayBasladi = false;
					for (String key : vardiyaGunleriMap.keySet()) {
						VardiyaGun vardiyaGun = vardiyaGunleriMap.get(key);
						String gun = key.substring(6);
						if (gun.equals("01")) {
							ayinGunumu = !ayBasladi;
							ayBasladi = true;
						}
						vardiyaGun.setAyinGunu(ayinGunumu);
					}

					for (String key : vardiyaGunleriMap.keySet()) {
						VardiyaGun vardiyaGun = vardiyaGunleriMap.get(key);
						if (!vardiyaGun.isAyinGunu())
							sonVardiyaGun = vardiyaGun;
						else {
							yarimYuvarla = vardiyaGun.getYarimYuvarla();
							break;
						}

					}
				}
				denklestirme.setYarimYuvarla(yarimYuvarla);
				denklestirme.setSonVardiyaGun(sonVardiyaGun);
				denklestirme.setDenklestirmeHaftasi(denklestirmeHaftasi);
				denklestirme.setVardiyalar(new ArrayList(vardiyaMap.values()));
				// Haftalik denklestirme verileri yapiliyor
				denklestirme.setDenklestirmeAy(personelDenklestirmeTasiyici.getDenklestirmeAy());
				LinkedHashMap<String, Object> dataMap = new LinkedHashMap<String, Object>();
				dataMap.put("manuelKapiMap", manuelKapiMap);
				dataMap.put("neden", neden);
				dataMap.put("sistemUser", sistemUser);
				dataMap.put("loginUser", loginUser);
				dataMap.put("gunMap", gunMap);
				dataMap.put("personelDenklestirme", denklestirme);
				dataMap.put("girisView", girisView);
				dataMap.put("vardiyaNetCalismaSuresiMap", vardiyaNetCalismaSuresiMap);
				dataMap.put("yemekList", yemekList);
				dataMap.put("tatilGunleriMap", tatilGunleriMap);
				dataMap.put("manuelKapiMap", manuelKapiMap);
				dataMap.put("updateSatus", Boolean.TRUE);
				dataMap.put("fiiliHesapla", Boolean.TRUE);
				sonVardiyaGun = personelVardiyaDenklestir(mapBosVeriSil(dataMap, "personelVardiyaDenklestir"), session);
				oncekiVardiyaGun = denklestirme.getOncekiVardiyaGun();
				normalFazlaMesai += denklestirme.getNormalFazlaMesai();
				resmiTatilMesai += denklestirme.getResmiTatilMesai();
				personelDenklestirmeTasiyici.getPersonelDenklestirmeleri().add(denklestirme);
			}
			personelDenklestirmeTasiyici.setNormalFazlaMesai(personelDenklestirmeTasiyici.getNormalFazlaMesai() + normalFazlaMesai);
			personelDenklestirmeTasiyici.setResmiTatilMesai(PdksUtil.setSureDoubleTypeRounded(resmiTatilMesai, personelDenklestirmeTasiyici.getYarimYuvarla()));
			personelDenklestirmeTasiyiciList.add(personelDenklestirmeTasiyici);
			if (testDurum)
				logger.info(perNoId + " denklestirmeOlustur 0200 " + getCurrentTimeStampStr());

		}
	}

	/**
	 * @param vg
	 * @param yemekList
	 * @param ekleDurum
	 */
	public void bayramSanalHareketiEkle(VardiyaGun vg, List<YemekIzin> yemekList, Boolean ekleDurum) {
		if (ekleDurum == null) {
			String bayramEkle = getParameterKey("bayramEkle");
			ekleDurum = bayramEkle != null && bayramEkle.equals("+");
		}
		// Arife günü hareketleri güncelleniyor
		Tatil tatil = vg.getTatil();
		if (tatil.isYarimGunMu() && ekleDurum) {
			if (vg.getIslemVardiya().getVardiyaBasZaman().getTime() < tatil.getBasTarih().getTime()) {
				HareketKGS ilkGiris = (HareketKGS) vg.getGirisHareketleri().get(0).clone(), sonCikis = null;
				List<HareketKGS> cikisHareketleri = vg.getCikisHareketleri();
				if (cikisHareketleri != null && !cikisHareketleri.isEmpty())
					sonCikis = (HareketKGS) cikisHareketleri.get(cikisHareketleri.size() - 1).clone();
				if (sonCikis != null && vg.getIslemVardiya().getVardiyaBitZaman().getTime() > sonCikis.getZaman().getTime()) {
					Calendar cal = Calendar.getInstance();
					cal.setTime((Date) sonCikis.getZaman().clone());
					cal.add(Calendar.MINUTE, vg.getVardiya().getCikisGecikmeToleransDakika());
					long cikisZamani = Long.parseLong(PdksUtil.convertToDateString(cal.getTime(), "HHmm"));
					HareketKGS arifeGirisHareket = new HareketKGS();
					arifeGirisHareket.setPersonel(ilkGiris.getPersonel());
					arifeGirisHareket.setKapiView(ilkGiris.getKapiView());
					if (sonCikis.getZaman().getTime() > tatil.getBasTarih().getTime())
						arifeGirisHareket.setZaman(sonCikis.getZaman());
					else
						arifeGirisHareket.setZaman(tatil.getBasTarih());
					for (YemekIzin yemekIzin : yemekList) {
						if (cikisZamani >= Long.parseLong(yemekIzin.getBasKey()) && cikisZamani <= Long.parseLong(yemekIzin.getBitKey())) {
							arifeGirisHareket.setZaman(sonCikis.getZaman());
							break;
						}
					}
					vg.addHareket(arifeGirisHareket, Boolean.TRUE);
					HareketKGS arifeCikisHareket = new HareketKGS();
					arifeCikisHareket.setPersonel(sonCikis.getPersonel());
					arifeCikisHareket.setKapiView(sonCikis.getKapiView());
					arifeCikisHareket.setZaman(vg.getIslemVardiya().getVardiyaBitZaman());
					vg.addHareket(arifeCikisHareket, Boolean.TRUE);
				}
			}
		} else if (tatil.getId() != null && vg.getId() == -tatil.getId() && !vg.isHareketHatali() && vg.getGirisHareketleri() != null) {
			try {
				HareketKGS ilkGiris = (HareketKGS) vg.getGirisHareketleri().get(0).clone(), sonCikis = null;
				List<HareketKGS> cikisHareketleri = vg.getCikisHareketleri();
				if (cikisHareketleri != null && !cikisHareketleri.isEmpty())
					sonCikis = (HareketKGS) cikisHareketleri.get(cikisHareketleri.size() - 1).clone();
				Date bayramBitis = tatil.getOrjTatil().getBasTarih();
				List<HareketKGS> hareketler = new ArrayList<HareketKGS>();
				HareketKGS bayramBitisCikisHareket = null;
				hareketler.addAll(vg.getHareketler());
				vg.getGirisHareketleri().clear();
				vg.getCikisHareketleri().clear();
				vg.getHareketler().clear();
				for (Iterator iterator2 = hareketler.iterator(); iterator2.hasNext();) {
					HareketKGS hareketKGS = (HareketKGS) iterator2.next();
					if (hareketKGS.getKapiView().getKapi().isCikisKapi() && bayramBitisCikisHareket == null || hareketKGS.getZaman().getTime() >= bayramBitis.getTime()) {
						if (sonCikis != null && vg.getHareketler() != null && !vg.getHareketler().isEmpty()) {
							bayramBitisCikisHareket = new HareketKGS();
							bayramBitisCikisHareket.setPersonel(ilkGiris.getPersonel());
							bayramBitisCikisHareket.setKapiView(sonCikis.getKapiView());
							bayramBitisCikisHareket.setZaman(bayramBitis);
							bayramBitisCikisHareket.setTatil(Boolean.FALSE);
							vg.addHareket(bayramBitisCikisHareket, Boolean.TRUE);
							HareketKGS bayramBitisGirisHareket = new HareketKGS();
							bayramBitisGirisHareket.setPersonel(ilkGiris.getPersonel());
							bayramBitisGirisHareket.setKapiView(ilkGiris.getKapiView());
							bayramBitisGirisHareket.setZaman(bayramBitis);
							bayramBitisGirisHareket.setTatil(Boolean.TRUE);
							vg.addHareket(bayramBitisGirisHareket, Boolean.TRUE);
						}
					}
					hareketKGS.setTatil(bayramBitisCikisHareket != null);
					vg.addHareket(hareketKGS, Boolean.TRUE);

				}
			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());
			}

		}
	}

	/**
	 * @param hareketKapiUpdateMap
	 * @param hareketKopyala
	 * @param loginUser
	 * @param session
	 */
	public void hareketKapiUpdate(HashMap<Long, KapiKGS> hareketKapiUpdateMap, boolean hareketKopyala, User loginUser, Session session) {
		List idList = new ArrayList(hareketKapiUpdateMap.keySet());
		String fieldName = "id";
		HashMap fields = new HashMap();
		fields.put(fieldName, idList);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		// List<PdksLog> list = pdksEntityController.getObjectByInnerObjectList(fields, PdksLog.class);
		List<PdksLog> list = getParamList(false, idList, fieldName, fields, PdksLog.class, session);
		if (!list.isEmpty()) {
			Tanim islemNeden = getOtomatikKapGirisiNeden(session);
			User onaylayanUser = getSistemAdminUser(session);
			Date guncellemeZamani = new Date();
			for (PdksLog pdksLog : list) {
				if (hareketKapiUpdateMap.containsKey(pdksLog.getId())) {
					KapiKGS kapiKGS = hareketKapiUpdateMap.get(pdksLog.getId());
					if (hareketKopyala || pdksLog.getKgsId() < 0l) {
						if (pdksLog.getKgsId() < 0l) {
							pdksLog.setGuncellemeZamani(guncellemeZamani);
							pdksLog.setKapiId(kapiKGS.getKgsId());
							session.saveOrUpdate(pdksLog);
						} else {
							PdksLog pdksLog2 = (PdksLog) pdksLog.clone();
							pdksLog2.setId(null);
							pdksLog2.setGuncellemeZamani(null);
							pdksLog2.setKapiId(kapiKGS.getKgsId());
							session.saveOrUpdate(pdksLog2);
							if (islemNeden != null) {
								PersonelHareketIslem islem = new PersonelHareketIslem();
								islem.setAciklama(pdksLog2.getKgsId() + " " + kapiKGS.getKapi().getAciklama() + " olarak güncellendi. [ " + pdksLog2.getId() + " ]");
								islem.setOnayDurum(PersonelHareketIslem.ONAY_DURUM_ONAYLANDI);
								islem.setOlusturmaTarihi(guncellemeZamani);
								islem.setGuncelleyenUser(onaylayanUser);
								islem.setOnaylayanUser(loginUser);
								islem.setZaman(pdksLog.getZaman());
								islem.setIslemTipi("U");
								islem.setNeden(islemNeden);
								session.saveOrUpdate(islem);
								pdksLog.setIslemId(islem.getId());
							}
							pdksLog.setGuncellemeZamani(guncellemeZamani);
							pdksLog.setDurum(Boolean.FALSE);
							session.saveOrUpdate(pdksLog);
						}

					} else {
						StringBuffer sp = new StringBuffer();
						sp.append("SP_POOL_TERMINAL_UPDATE");
						LinkedHashMap map = new LinkedHashMap();
						if (session != null)
							map.put(PdksEntityController.MAP_KEY_SESSION, session);
						map.put("eklenenId", pdksLog.getKgsId());
						map.put("pdks", 1);
						try {
							pdksEntityController.execSP(map, sp);
						} catch (Exception e) {
							pdksLog.setGuncellemeZamani(guncellemeZamani);
							pdksLog.setKapiId(kapiKGS.getKgsId());
							session.saveOrUpdate(pdksLog);
						}
						sp = null;
					}

				}
			}
			session.flush();
		}
		list = null;
	}

	/**
	 * @param izinler
	 * @param vardiyaGunList
	 */
	public List<VardiyaGun> vardiyaIzinleriGuncelle(List<PersonelIzin> izinler, List<VardiyaGun> vardiyaGunList) {
		List<VardiyaGun> izinVardiyaGunList = new ArrayList<VardiyaGun>();
		if (izinler != null && vardiyaGunList != null) {
			boolean izinVar = false;
			try {
				for (Iterator<VardiyaGun> iterator = vardiyaGunList.iterator(); iterator.hasNext();) {
					VardiyaGun vardiyaGun = iterator.next();
					vardiyaGun.setIzinler(null);
					vardiyaGun.setIzin(null);
					Vardiya vardiya = vardiyaGun.getVardiya();

					if (vardiya == null) {
						Personel personel = vardiyaGun.getPdksPersonel();

						boolean calisiyor = personel != null && personel.isCalisiyorGun(vardiyaGun.getVardiyaDate());
						if (!calisiyor)
							continue;
					}

					// Personel izinleri vardiya gününe ekleniyor
					if (vardiya != null) {
						if (izinler != null) {
							for (Iterator<PersonelIzin> iterator3 = izinler.iterator(); iterator3.hasNext();) {
								PersonelIzin personelIzin = iterator3.next();
								setIzinDurum(vardiyaGun, personelIzin);
							}
							if (vardiyaGun.getIzin() != null && vardiya.isHaftaTatil()) {
								if (!vardiyaGun.getIzin().getIzinTipi().getPersonelGirisTipi().equals(IzinTipi.GIRIS_TIPI_YOK))
									izinVar = true;
							}
						}
						if (vardiyaGun.getIzin() != null && vardiyaGun.getVardiya().isIzin())
							izinVardiyaGunList.add(vardiyaGun);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			if (izinVar) {
				Collections.reverse(vardiyaGunList);
				for (VardiyaGun vardiyaGun : vardiyaGunList) {
					VardiyaGun oncekiVardiyaGun = vardiyaGun.getOncekiVardiyaGun();
					if (vardiyaGun.getIzin() != null || oncekiVardiyaGun == null || oncekiVardiyaGun.getIzin() == null)
						continue;
					if (oncekiVardiyaGun.getVardiya().isHaftaTatil()) {
						if (oncekiVardiyaGun.getIzin() != null && oncekiVardiyaGun.getIzin().getIzinTipi().isSenelikIzin())
							oncekiVardiyaGun.setIzin(null);
						// PersonelIzin izin = oncekiVardiyaGun.getIzin();
						// if (izin != null) {
						// IzinTipi izinTipi = izin.getIzinTipi();
						// if (!(izinTipi.isTakvimGunuMu() || izinTipi.isHTDahil()))
						// oncekiVardiyaGun.setIzin(null);
						// }

					}

				}
				Collections.reverse(vardiyaGunList);
			}
		}
		return izinVardiyaGunList;
	}

	/**
	 * @param map
	 * @param pec
	 * @param session
	 * @return
	 */
	public User getSistemAdminUserByParamMap(HashMap<String, String> map, PdksEntityController pec, Session session) {
		User user = null;
		LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
		boolean startUp = map != null;
		if (startUp == false)
			map = parameterMap;
		if (pec == null)
			pec = pdksEntityController;
		if (map != null) {
			StringBuffer sb = new StringBuffer();
			sb.append("SP_GET_SISTEM_ADMIN_LIST");
			fields.put("TIP", "U");
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				List<User> userList = pec.execSPList(fields, sb, User.class);
				if (userList != null && !userList.isEmpty())
					user = userList.get(0);
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
		if (startUp == false || user == null) {
			if (user == null) {
				if (!fields.isEmpty())
					fields.clear();
				fields.put("id", 1L);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				user = (User) pec.getObjectByInnerObject(fields, User.class);
			}
			if (user != null)
				try {
					// logger.info(user.getAdSoyad()));
					setUserRoller(user, session);
					user.setAdmin(Boolean.FALSE);
					user.setIK(Boolean.FALSE);
					user.setLogin(Boolean.FALSE);
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());
				}
			fields = null;
		}
		return user;

	}

	/**
	 * @param session
	 * @return
	 */
	public User getSistemAdminUser(Session session) {
		User user = getSistemAdminUserByParamMap(null, pdksEntityController, session);
		return user;

	}

	/**
	 * @param session
	 * @param tipi
	 * @return
	 */
	public List<User> bccAdminAdres(Session session, String tipi) {
		List<User> userList = null;
		HashMap map = new HashMap();
		Date bugun = PdksUtil.getDate(new Date());
		if (tipi == null) {
			String bccAdresStr = getParameterKey("bccAdres");
			if (bccAdresStr != null && bccAdresStr.indexOf("@") > 1) {
				List<String> bccAdresler = PdksUtil.getListFromString(bccAdresStr, null);
				if (bccAdresler != null && !bccAdresler.isEmpty()) {
					userList = new ArrayList<User>();
					for (String email : bccAdresler) {
						User user = new User();
						user.setEmail(email);
						userList.add(user);
					}
					map.clear();
					map.put("email", bccAdresler);
					map.put("durum=", Boolean.TRUE);
					map.put("pdksPersonel.durum=", Boolean.TRUE);
					map.put("pdksPersonel.iseBaslamaTarihi<=", bugun);
					map.put("pdksPersonel.sskCikisTarihi>=", bugun);
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<User> list = pdksEntityController.getObjectByInnerObjectListInLogic(map, User.class);
					if (!list.isEmpty() && userList.size() >= list.size()) {
						for (int i = 0; i < userList.size(); i++) {
							User user = userList.get(i);
							for (Iterator iterator = list.iterator(); iterator.hasNext();) {
								User userDb = (User) iterator.next();
								if (userDb.getEmail().equals(user.getEmail())) {
									userList.set(i, userDb);
									iterator.remove();
									break;
								}

							}
						}
					}
					list = null;
				}
				bccAdresler = null;
			}
		}

		if (userList == null) {
			map.clear();
			map.put(PdksEntityController.MAP_KEY_SELECT, "user");
			// map.put("role.rolename ", Arrays.asList(Role.TIPI_ADMIN, Role.TIPI_SISTEM_YONETICI));
			map.put("role.rolename=", Role.TIPI_ADMIN);
			map.put("user.durum=", Boolean.TRUE);
			map.put("user.pdksPersonel.durum=", Boolean.TRUE);
			map.put("user.pdksPersonel.iseBaslamaTarihi<=", bugun);
			map.put("user.pdksPersonel.sskCikisTarihi>=", bugun);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			userList = pdksEntityController.getObjectByInnerObjectListInLogic(map, UserRoles.class);
			map = null;
		}
		map = null;
		if (!PdksUtil.isSistemDestekVar()) {
			if (userList == null)
				userList = new ArrayList<User>();
			else
				userList.clear();
		}

		return userList;
	}

	/**
	 * @param tumPersoneller
	 * @param yetkiTumPersonelNoList
	 * @param tarih1
	 * @param tarih2
	 * @param session
	 */
	public void digerPersoneller(ArrayList<Personel> tumPersoneller, List<String> yetkiTumPersonelNoList, Date tarih1, Date tarih2, Session session) {
		if (tumPersoneller != null || yetkiTumPersonelNoList != null) {
			Long departmanId = authenticatedUser.getPdksPersonel().getSirket().getDepartman().getId();
			List<Long> personelIdler = null;
			boolean devam = Boolean.FALSE;
			if (tumPersoneller != null) {
				devam = !tumPersoneller.isEmpty();
				personelIdler = new ArrayList<Long>();
				for (Personel personel : tumPersoneller) {
					try {
						if (!departmanId.equals(personel.getSirket().getDepartman().getId()))
							personelIdler.add(personel.getId());
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());
					}
				}
			} else {
				String sicilNo = authenticatedUser.getPdksPersonel().getPdksSicilNo();
				for (Iterator iterator = yetkiTumPersonelNoList.iterator(); iterator.hasNext();) {
					String numara = (String) iterator.next();
					if (PdksUtil.hasStringValue(numara) == false || numara.equals(sicilNo))
						iterator.remove();
				}
				devam = !yetkiTumPersonelNoList.isEmpty();
			}
			if (devam) {
				HashMap map = new HashMap();
				if (personelIdler != null)
					map.put("yoneticisi.id", personelIdler);
				else
					map.put("yoneticisi.pdksSicilNo", yetkiTumPersonelNoList);
				map.put("durum=", Boolean.TRUE);
				if (tarih2 != null)
					map.put("iseBaslamaTarihi<=", tarih2);
				if (tarih1 != null)
					map.put("sskCikisTarihi>=", tarih1);
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);

				List<Personel> digerPersoneller = pdksEntityController.getObjectByInnerObjectListInLogic(map, Personel.class);
				for (Personel personel : digerPersoneller) {
					if (departmanId.equals(personel.getSirket().getDepartman().getId()))
						continue;
					if (tumPersoneller != null)
						tumPersoneller.add(personel);
					if (yetkiTumPersonelNoList != null && PdksUtil.hasStringValue(personel.getSicilNo()))
						yetkiTumPersonelNoList.add(personel.getSicilNo());
				}
			}

		}
	}

	/**
	 * @param basTarih
	 * @param bitTarih
	 * @param session
	 * @return
	 */
	public List<Long> getPdksDonemselKapiIdler(Date basTarih, Date bitTarih, Session session) {
		List<Long> kapiIdIList = null;
		if (basTarih != null && bitTarih != null) {
			Calendar cal = Calendar.getInstance();
			HashMap fields = new HashMap();
			List<Long> tipler = null;
			List<String> hareketTip = new ArrayList<String>();
			hareketTip.add(Kapi.TIPI_KODU_GIRIS);
			hareketTip.add(Kapi.TIPI_KODU_CIKIS);
			fields.put(PdksEntityController.MAP_KEY_SELECT, "id");
			fields.put("kodu", hareketTip);
			fields.put("tipi=", Tanim.TIPI_KAPI_TIPI);
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			tipler = pdksEntityController.getObjectByInnerObjectListInLogic(fields, Tanim.class);
			if (tipler.isEmpty())
				tipler = null;
			fields.clear();
			fields.put(PdksEntityController.MAP_KEY_MAP, "getId");
			fields.put(PdksEntityController.MAP_KEY_SELECT, "kapiKGS");
			fields.put("pdks=", Boolean.TRUE);
			if (!tipler.isEmpty())
				fields.put("tipi.id", tipler);
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			TreeMap<Long, KapiKGS> kapiMap = pdksEntityController.getObjectByInnerObjectMapInLogic(fields, Kapi.class, false);
			Date tarih1 = tariheGunEkleCikar(cal, basTarih, -7);
			Date tarih2 = tariheGunEkleCikar(cal, bitTarih, 7);
			kapiIdIList = new ArrayList<Long>(kapiMap.keySet());
			for (Iterator iterator = kapiIdIList.iterator(); iterator.hasNext();) {
				Long key = (Long) iterator.next();
				KapiSirket kapiSirket = kapiMap.get(key).getKapiSirket();
				if (kapiSirket != null) {
					if (kapiSirket.getBitTarih() != null && tarih1.getTime() <= kapiSirket.getBitTarih().getTime() && kapiSirket.getBasTarih() != null && tarih2.getTime() >= kapiSirket.getBasTarih().getTime())
						continue;
					else
						iterator.remove();
				}
			}
		} else
			kapiIdIList = getKapiIdler(session, Boolean.TRUE, Boolean.TRUE);

		return kapiIdIList;
	}

	/**
	 * @param session
	 * @param girisCikisKapilari
	 * @return
	 */
	public List<Long> getPdksKapiIdler(Session session, Boolean girisCikisKapilari) {

		List<Long> kapiIdler = getKapiIdler(session, Boolean.TRUE, girisCikisKapilari);

		return kapiIdler;
	}

	/**
	 * @param session
	 * @param pdks
	 * @param girisCikisKapilari
	 * @return
	 */
	public List<Long> getKapiIdler(Session session, Boolean pdks, Boolean girisCikisKapilari) {
		List<Long> kapiIdler = null;
		if (pdks != null) {
			HashMap fields = new HashMap();
			List<Tanim> tipler = null;
			if (girisCikisKapilari == null || girisCikisKapilari) {
				List<String> hareketTip = new ArrayList<String>();
				if (girisCikisKapilari != null) {
					hareketTip.add(Kapi.TIPI_KODU_GIRIS);
					hareketTip.add(Kapi.TIPI_KODU_CIKIS);
					fields.put("kodu", hareketTip);
				}
				fields.put("tipi=", Tanim.TIPI_KAPI_TIPI);
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				tipler = pdksEntityController.getObjectByInnerObjectListInLogic(fields, Tanim.class);
				if (tipler.isEmpty())
					tipler = null;
				fields.clear();
			}
			fields.put(PdksEntityController.MAP_KEY_SELECT, "kapiKGS.id");
			if (pdks != null)
				fields.put("pdks", pdks);
			// fields.put("durum", Boolean.TRUE);
			if (tipler != null)
				fields.put("tipi", tipler);

			fields.put(PdksEntityController.MAP_KEY_SESSION, session);

			kapiIdler = pdksEntityController.getObjectByInnerObjectList(fields, Kapi.class);

			if (girisCikisKapilari)
				tipler = null;
			fields = null;
		}
		return kapiIdler;
	}

	/**
	 * @param kullaniciAdi
	 * @param alanAdi
	 * @return
	 */
	public User kullaniciBul(String kullaniciAdi, String alanAdi) {
		User ldapUser = null;
		try {
			if (kullaniciAdi != null && alanAdi != null)
				ldapUser = LDAPUserManager.getLDAPUserAttributes(kullaniciAdi.trim(), alanAdi.trim());
		} catch (Exception e) {
			ldapUser = null;
		}
		if (ldapUser != null && !ldapUser.isDurum())
			ldapUser = null;

		return ldapUser;
	}

	/**
	 * @param session
	 * @return
	 */
	public List<Long> getYemekKapiIdList(Session session) {
		HashMap parametreMap = new HashMap();
		parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "kapiKGS.id");
		parametreMap.put("tipi.kodu", Kapi.TIPI_KODU_YEMEKHANE);
		parametreMap.put("durum", Boolean.TRUE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Long> list = pdksEntityController.getObjectByInnerObjectList(parametreMap, Kapi.class);
		parametreMap = null;
		return list;
	}

	/**
	 * @param admin
	 * @return
	 */
	public String yetkiIKAdmin(boolean admin) {
		String sayfa = "";
		if (authenticatedUser.isIK()) {
			if (authenticatedUser.isIKAdmin() == admin) {
				PdksUtil.addMessageWarn("Bu sayfayi kullanmaya yetkili degilsiniz!");
				sayfa = MenuItemConstant.home;
			}
		}
		return sayfa;
	}

	/**
	 * @param list
	 * @return
	 */
	public List<Personel> getPdksPersonelViewList(List<PdksPersonelView> list) {
		List<Personel> personelList = null;
		if (list != null) {
			personelList = new ArrayList<Personel>();
			for (PdksPersonelView personelView : list)
				personelList.add(personelView.getPdksPersonel());
		}
		return personelList;

	}

	/**
	 * @param list
	 * @return
	 */
	public List<Long> getPersonelViewIdList(List<PdksPersonelView> list) {
		List<Long> personelList = null;
		if (list != null) {
			personelList = new ArrayList<Long>();
			for (PdksPersonelView personelView : list) {
				Long long1 = personelView.getPersonelKGSId();
				if (long1 != null && !personelList.contains(long1))
					personelList.add(long1);
			}

		}
		return personelList;

	}

	/**
	 * @param list
	 * @return
	 */
	public List<PersonelView> getPersonelViewList(List<PdksPersonelView> list) {
		List<PersonelView> personelList = null;
		if (list != null) {
			personelList = new ArrayList<PersonelView>();
			for (PdksPersonelView personelView : list)
				personelList.add(personelView.getPersonelView());
		}
		return personelList;

	}

	/**
	 * @param user
	 * @param basTarih
	 * @param bitTarih
	 * @param gorevYerileri
	 * @param session
	 * @return
	 */
	public List<VardiyaGorev> getVardiyaGorevYerleri(User user, Date basTarih, Date bitTarih, List<Long> gorevYerileri, Session session) {
		List<VardiyaGorev> list = null;
		boolean bolumGorevlendirmeVar = getParameterKey("bolumGorevlendirmeVar").equals("1");
		if (bolumGorevlendirmeVar && user != null && gorevYerileri != null && !gorevYerileri.isEmpty()) {
			List<String> yetkiliPersonelNoList = user.getYetkiliPersonelNoList();
			HashMap fields = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT A." + VardiyaGorev.COLUMN_NAME_ID + " FROM " + VardiyaGun.TABLE_NAME + " I  WITH(nolock) ");
			sb.append(" INNER JOIN " + VardiyaGorev.TABLE_NAME + " A ON A." + VardiyaGorev.COLUMN_NAME_VARDIYA_GUN + " = I." + VardiyaGun.COLUMN_NAME_ID);
			sb.append(" AND A.YENI_GOREV_YERI_ID IS NOT NULL");
			sb.append(" WHERE I." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " >= :basTarih");
			sb.append(" and I." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " <= :bitTarih");
			fields.put("basTarih", basTarih);
			fields.put("bitTarih", basTarih);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			list = getDataByIdList(sb, fields, VardiyaGorev.TABLE_NAME, VardiyaGorev.class);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				VardiyaGorev vardiyaGorev = (VardiyaGorev) iterator.next();
				try {
					VardiyaGun vardiyaGun = vardiyaGorev.getVardiyaGun();
					Personel personel = vardiyaGun.getPersonel();
					if (yetkiliPersonelNoList.contains(personel.getPdksSicilNo()))
						iterator.remove();
					else if (vardiyaGorev.getYeniGorevYeri() == null || !gorevYerileri.contains(vardiyaGorev.getYeniGorevYeri().getId()))
						iterator.remove();
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());
					iterator.remove();
				}

			}
		} else
			list = new ArrayList<VardiyaGorev>();
		return list;
	}

	/**
	 * @param paramMap
	 */
	public void showSQLQuery(HashMap paramMap) {
		String showSQL = getParameterKey(PdksEntityController.MAP_KEY_SHOW_SQL);
		if (showSQL != null) {
			boolean durum = showSQL.equals("1") || showSQL.toLowerCase().equals("true");
			if (durum) {
				paramMap.put(PdksEntityController.MAP_KEY_SHOW_SQL, durum);
				try {
					if (authenticatedUser != null)
						paramMap.put(PdksEntityController.MAP_KEY_USER, PdksUtil.setTurkishStr(authenticatedUser.getAdSoyad()));
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}

			}

		}

	}

	/**
	 * @param mailAdress
	 * @param session
	 * @return
	 */
	public List<User> getAktifMailUser(String mailAdress, Session session) {
		List<User> userList = null;
		if (mailAdress != null && mailAdress.indexOf("@") > 1) {
			Calendar cal = Calendar.getInstance();
			List<String> mailList = PdksUtil.getListByString(mailAdress, null);
			if (mailList.size() > 1) {
				TreeMap<String, String> map1 = new TreeMap<String, String>();
				for (String string : mailList) {
					map1.put(string, string);
				}
				mailList = new ArrayList<String>(map1.values());
				map1 = null;
			}
			Date istenAyrilmaTarihi = PdksUtil.getDate(tariheGunEkleCikar(cal, new Date(), -14));
			String fieldName = "e";
			HashMap fields = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT DISTINCT U.*  FROM " + User.TABLE_NAME + " U WITH(nolock) ");
			sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + " = U." + User.COLUMN_NAME_PERSONEL + " AND (P." + Personel.COLUMN_NAME_DURUM + " = 1 ");
			sb.append(" OR U." + User.COLUMN_NAME_DURUM + " = 1 AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= :t) ");
			sb.append(" WHERE U." + User.COLUMN_NAME_EMAIL + " :" + fieldName);
			sb.append(" ORDER BY  U." + User.COLUMN_NAME_EMAIL);
			fields.put(fieldName, mailList);
			fields.put("t", istenAyrilmaTarihi);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			// userList = pdksEntityController.getObjectBySQLList(sb, fields, User.class);
			userList = getSQLParamList(mailList, sb, fieldName, fields, User.class, session);
			sb = null;
		}
		if (userList == null)
			userList = new ArrayList<User>();
		return userList;
	}

	/**
	 * @param loginUser
	 * @param aylikPuantajList
	 * @param calismayanPersonelYoneticiDurum
	 * @param session
	 */
	public void yoneticiPuantajKontrol(User loginUser, List<AylikPuantaj> aylikPuantajList, Boolean calismayanPersonelYoneticiDurum, Session session) {
		String key = getParameterKey("yoneticiPuantajKontrol");
		boolean kontrolEtme = !PdksUtil.hasStringValue(key);
		if (aylikPuantajList != null) {
			Calendar cal = Calendar.getInstance();
			HashMap fields = new HashMap();
			fields.put("tipi", Tanim.TIPI_PERSONEL_DINAMIK_DURUM);
			fields.put("kodu", Tanim.IKINCI_YONETICI_ONAYLAMAZ);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			Tanim ikinciYoneticiOlmaz = (Tanim) pdksEntityController.getObjectByInnerObject(fields, Tanim.class);
			if (ikinciYoneticiOlmaz != null && !ikinciYoneticiOlmaz.getDurum())
				ikinciYoneticiOlmaz = null;

			List<AylikPuantaj> list = new ArrayList<AylikPuantaj>();
			List<Long> idList = new ArrayList<Long>(), id2List = new ArrayList<Long>(), idPasifList = new ArrayList<Long>();
			try {
				Personel yonetici = null;
				if (key.equals("0") || loginUser.isAdmin() || loginUser.isSistemYoneticisi()) {
					yonetici = new Personel();
					if (kontrolEtme == false)
						yonetici.setId(-1L);
					yonetici.setAd(yoneticiAciklama());
					yonetici.setSoyad("tanımsız!");
				}
				Date sonGun = null;
				boolean yoneticiTanimli = !PdksUtil.hasStringValue(getParameterKey("yoneticiTanimsiz"));
				if (calismayanPersonelYoneticiDurum && !aylikPuantajList.isEmpty())
					sonGun = tariheGunEkleCikar(cal, aylikPuantajList.get(0).getSonGun(), 1);

				for (AylikPuantaj aylikPuantaj : aylikPuantajList) {
					boolean yoneticiKontrol = true;
					Personel personel = aylikPuantaj.getPdksPersonel();
					if (sonGun != null && personel.isCalisiyorGun(sonGun) == false) {
						yoneticiKontrol = false;
						aylikPuantaj.setPersonelCalismiyor();
					}

					if (aylikPuantaj.getYonetici2() != null) {
						Long id = aylikPuantaj.getYonetici2().getId();
						if (id != null) {
							if (!id2List.contains(id)) {
								id2List.add(id);
							}
						}
					}
					if (aylikPuantaj.getYonetici() == null) {
						if (!personel.isSanalPersonelMi())
							aylikPuantaj.setYonetici(yonetici);
						continue;
					}
					Long id = aylikPuantaj.getYonetici().getId();
					if (id != null) {
						if (ikinciYoneticiOlmaz != null && !id2List.contains(id)) {
							id2List.add(id);
						}
						if (!idList.contains(id)) {
							if (!idPasifList.contains(id) && yoneticiKontrol) {
								if (aylikPuantaj.getYonetici().isCalisiyor())
									idList.add(id);
								else
									idPasifList.add(id);
							}

						}
						if (idList.contains(id))
							list.add(aylikPuantaj);
						else if (!personel.isSanalPersonelMi() && yoneticiKontrol)
							aylikPuantaj.setYonetici(yonetici);
					}

				}

				if (kontrolEtme == false && !idList.isEmpty()) {
					Personel yoneticiUser = new Personel();
					if (kontrolEtme)
						yoneticiUser.setId(-1L);
					yoneticiUser.setAd("");
					if (yoneticiTanimli)
						yoneticiUser.setSoyad("kullanıcı tanımsız!");
					String fieldName = "pdksPersonel.id";
					fields.clear();
					fields.put(PdksEntityController.MAP_KEY_SELECT, "pdksPersonel");
					// fields.put(PdksEntityController.MAP_KEY_MAP, "getId");
					fields.put(fieldName, idList);
					fields.put("durum", Boolean.TRUE);
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					// TreeMap<Long, Personel> personelMap = pdksEntityController.getObjectByInnerObjectMap(fields, User.class, false);
					TreeMap<Long, Personel> personelMap = getParamTreeMap(Boolean.FALSE, "getId", false, idList, fieldName, fields, User.class, session);

					if (personelMap.size() < idList.size()) {
						for (AylikPuantaj aylikPuantaj : list) {
							Long id = aylikPuantaj.getYonetici().getId();
							Personel yonetici1 = personelMap.get(id);
							Personel yoneticiUserClone = (Personel) yoneticiUser.clone();
							if (yonetici1 == null) {
								yoneticiUserClone.setAd(aylikPuantaj.getYonetici().getAdSoyad());
								yonetici1 = yoneticiUserClone;
							}
							aylikPuantaj.setYonetici(yonetici1);
						}
					}
				}
				if (!id2List.isEmpty() && kontrolEtme == false) {
					List<Long> ikinciYoneticiOlmazList = null;
					if (ikinciYoneticiOlmaz != null) {
						fields.clear();
						fields.put(PdksEntityController.MAP_KEY_SELECT, "personel.id");
						fields.put("alan.id=", ikinciYoneticiOlmaz.getId());
						fields.put("durumSecim=", Boolean.TRUE);
						fields.put("personel.durum=", Boolean.TRUE);
						fields.put("personel.sskCikisTarihi>=", PdksUtil.getDate(new Date()));
						if (session != null)
							fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						ikinciYoneticiOlmazList = pdksEntityController.getObjectByInnerObjectListInLogic(fields, PersonelDinamikAlan.class);
					} else
						ikinciYoneticiOlmazList = new ArrayList<Long>();
					String fieldName = "pdksPersonel.id";
					fields.clear();
					// fields.put(PdksEntityController.MAP_KEY_MAP, "getPersonelId");
					fields.put(fieldName, id2List);
					fields.put("durum", Boolean.TRUE);
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					// TreeMap<Long, User> personelMap = pdksEntityController.getObjectByInnerObjectMap(fields, User.class, false);
					TreeMap<Long, User> personelMap = getParamTreeMap(Boolean.FALSE, "getPersonelId", false, id2List, fieldName, fields, User.class, session);

					if (personelMap.size() <= id2List.size()) {
						for (AylikPuantaj aylikPuantaj : aylikPuantajList) {
							if (aylikPuantaj.getYonetici2() == null)
								continue;
							if (aylikPuantaj.getYonetici() != null || aylikPuantaj.getPdksPersonel().isSanalPersonelMi()) {
								Long id = aylikPuantaj.getYonetici2().getId();
								User user = personelMap.get(id);
								Personel yonetici2 = null;
								if (ikinciYoneticiOlmazList.contains(id)) {
									if (aylikPuantaj.getYonetici() != null && personelMap.containsKey(aylikPuantaj.getYonetici().getId())) {
										user = personelMap.get(aylikPuantaj.getYonetici().getId());
										yonetici2 = user.getPdksPersonel();
									}
								} else if (user != null)
									yonetici2 = user.getPdksPersonel();
								aylikPuantaj.setYonetici2(yonetici2);
							} else
								aylikPuantaj.setYonetici2(null);

						}
					}

				}
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
			list = null;
			id2List = null;
			idList = null;
			idPasifList = null;
		}

	}

	/**
	 * @param list
	 * @return
	 */
	public List<PersonelView> getPersonelViewByPersonelKGSList(List list) {
		List<PersonelView> personelViewList = null;
		if (list != null) {
			personelViewList = new ArrayList<PersonelView>();
			for (Object object : list) {
				if (object != null) {
					PersonelView personelView = null;
					if (object instanceof PersonelKGS) {
						PersonelKGS personelKGS = (PersonelKGS) object;
						personelView = personelKGS.getPersonelView();
					} else if (object instanceof PersonelView)
						personelView = (PersonelView) object;
					if (personelView != null)
						personelViewList.add(personelView);
				}
			}
		}
		return personelViewList;

	}

	/**
	 * @param session
	 * @return
	 */
	public List<KapiView> fillKapiPDKSList(Session session) {
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
		kapiKGSList = null;
		list = PdksUtil.sortObjectStringAlanList(list, "getAciklama", null);
		return list;
	}

	/**
	 * @param mailAdress
	 * @param session
	 * @return
	 */
	public String getAktifMailAdress(String mailAdress, Session session) {
		if (mailAdress != null && mailAdress.indexOf("@") > 1) {
			Calendar cal = Calendar.getInstance();
			List<String> mailList = PdksUtil.getListByString(mailAdress, null);
			if (mailList.size() > 1) {
				TreeMap<String, String> map1 = new TreeMap<String, String>();
				for (String string : mailList) {
					map1.put(string, string);
				}
				mailList = new ArrayList<String>(map1.values());
				map1 = null;
			}

			Date istenAyrilmaTarihi = PdksUtil.getDate(tariheGunEkleCikar(cal, new Date(), -14));
			String fieldName = "e";
			HashMap fields = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT DISTINCT " + User.COLUMN_NAME_EMAIL + " FROM " + User.TABLE_NAME + " U WITH(nolock) ");
			sb.append(" INNER JOIN " + Personel.TABLE_NAME + " P ON P." + Personel.COLUMN_NAME_ID + " = U." + User.COLUMN_NAME_PERSONEL + " AND (P." + Personel.COLUMN_NAME_DURUM + " = 0 ");
			sb.append(" OR U." + User.COLUMN_NAME_DURUM + " = 0 OR P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " <= :t ) ");
			sb.append(" WHERE U." + User.COLUMN_NAME_EMAIL + " :" + fieldName);
			fields.put(fieldName, mailList);
			fields.put("t", istenAyrilmaTarihi);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			// List<String> pasifList = pdksEntityController.getObjectBySQLList(sb, fields, null);
			List<String> pasifList = getSQLParamList(mailList, sb, fieldName, fields, null, session);

			sb = new StringBuffer();
			for (String mail : mailList) {
				if (pasifList.contains(mail))
					continue;
				if (sb.length() > 0)
					sb.append(PdksUtil.SEPARATOR_MAIL);
				sb.append(mail);
			}
			mailAdress = sb.toString();

			sb = null;
		}
		return mailAdress;
	}

	/**
	 * @return
	 */
	public String getParametrePersonelERPTableView() {
		String str = "personelERPTableViewAdi";
		return str;
	}

	/**
	 * @return
	 */
	public String getParametreHakEdisIzinERPTableView() {
		String str = "hakEdisIzinERPTableViewAdi";
		return str;
	}

	/**
	 * @return
	 */
	public String getParametreIzinERPTableView() {
		String str = "izinERPTableViewAdi";
		return str;
	}

	/**
	 * @param string
	 * @return
	 */
	public String getEncodeStringByBase64(String string) {
		return PdksUtil.getEncodeStringByBase64(string);
	}

	/**
	 * @param string
	 * @return
	 */
	public static String getDecodeStringByBase64(String string) {
		return PdksUtil.getDecodeStringByBase64(string);
	}
}
