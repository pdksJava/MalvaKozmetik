package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.CalismaModeli;
import org.pdks.entity.CalismaModeliAy;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Dosya;
import org.pdks.entity.KesintiTipi;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.VardiyaGun;
import org.pdks.security.entity.User;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

@Name("vardiyaTanimlaHome")
public class VardiyaTanimlamaHome extends EntityHome<DenklestirmeAy> implements Serializable {

	/**
	 * 
	 **/
	private static final long serialVersionUID = 5067953117682032644L;
	static Logger logger = Logger.getLogger(VardiyaTanimlamaHome.class);

	@RequestParameter
	Long pdksVardiyaGunId;

	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = false, create = true)
	HashMap parameterMap;
	@In(required = false)
	FacesMessages facesMessages;
	@In(create = true, required = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;

	private List<DenklestirmeAy> aylikList = null;
	private String maxYil = null, vardiyaTanimKodu = null;
	private int yilEdit, yilModal, yilSelect, yil;
	private boolean aktif;
	private double sure;
	private Dosya devredenBakiyeDosya = new Dosya();
	private List<PersonelDenklestirme> personelDenklestirmeler;

	private List<DenklestirmeAy> denklestirmeAylar;

	private DenklestirmeAy denklestirmeAy;
	private TreeMap<String, CalismaModeliAy> modelMap;
	boolean guncelle = Boolean.FALSE, denklestirmeKesintiYap = Boolean.FALSE, disabled = false;
	private TreeMap<String, PersonelDenklestirme> bakiySonrakiMap;
	private List<CalismaModeli> calismaModeliList = new ArrayList<CalismaModeli>();
	private List<SelectItem> kesintiTuruList = new ArrayList<SelectItem>();
	private Boolean hareketKaydiVardiyaBul = Boolean.FALSE, negatifBakiyeDenkSaat = Boolean.FALSE, otomatikFazlaCalismaOnaylansinVar = Boolean.FALSE;
	private Session session;

	public int getGirisKolonSayisi() {
		int artiAdet = 0;
		hareketKaydiVardiyaBul = Boolean.FALSE;
		negatifBakiyeDenkSaat = Boolean.FALSE;
		otomatikFazlaCalismaOnaylansinVar = Boolean.FALSE;
		if (authenticatedUser.isAdmin() && denklestirmeAy != null && denklestirmeAy.getModeller() != null) {

			for (CalismaModeliAy calismaModeliAy : denklestirmeAy.getModeller()) {
				CalismaModeli cm = calismaModeliAy.getCalismaModeli();
				if (calismaModeliAy.getNegatifBakiyeDenkSaat() < 0 || cm.getNegatifBakiyeDenkSaat() < 0)
					negatifBakiyeDenkSaat = Boolean.TRUE;
				if (cm.isHareketKaydiVardiyaBulsunmu() || calismaModeliAy.isHareketKaydiVardiyaBulsunmu())
					hareketKaydiVardiyaBul = Boolean.TRUE;
				if (cm.isOtomatikFazlaCalismaOnaylansinmi() || calismaModeliAy.isOtomatikFazlaCalismaOnaylansinmi())
					otomatikFazlaCalismaOnaylansinVar = Boolean.TRUE;

			}
			if (negatifBakiyeDenkSaat)
				artiAdet += 1;
			if (hareketKaydiVardiyaBul)
				artiAdet += 1;
			if (otomatikFazlaCalismaOnaylansinVar)
				artiAdet += 1;
		}
		int sayi = artiAdet + 2;
		return sayi;
	}

	/**
	 * @param dm
	 * @param cm
	 * @return
	 */
	public CalismaModeliAy getCalismaModeliAy(DenklestirmeAy dm, CalismaModeli cm) {
		String key = CalismaModeliAy.getKey(dm, cm);
		CalismaModeliAy calismaModeliAy = modelMap.containsKey(key) ? modelMap.get(key) : new CalismaModeliAy(dm, cm);
		return calismaModeliAy;

	}

	@Transactional
	public String fillPdksYoneticiDenklestirme(Session xSession) {
		HashMap map = new HashMap();
		map.put("yil=", yil);
		Calendar cal = Calendar.getInstance();
		if (cal.get(Calendar.YEAR) == yil)
			map.put("ay<=", cal.get(Calendar.MONTH) + 2);
		if (xSession != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, xSession);
		denklestirmeAylar = pdksEntityController.getObjectByInnerObjectListInLogic(map, DenklestirmeAy.class);
		denklestirmeAylar = PdksUtil.sortListByAlanAdi(denklestirmeAylar, "ay", false);
		List<Long> dmIdList = new ArrayList<Long>();
		for (DenklestirmeAy dm : denklestirmeAylar) {
			dmIdList.add(dm.getId());
		}
		map.clear();
		map.put(PdksEntityController.MAP_KEY_MAP, "getKey");
		map.put("denklestirmeAy.id", dmIdList);
		if (xSession != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, xSession);
		modelMap = pdksEntityController.getObjectByInnerObjectMap(map, CalismaModeliAy.class, false);
		TreeMap<Long, CalismaModeli> cmMap = new TreeMap<Long, CalismaModeli>();
		for (String key : modelMap.keySet()) {
			CalismaModeliAy cma = modelMap.get(key);
			CalismaModeli cm = modelMap.get(key).getCalismaModeli();
			if (cma.getDurum() && !cmMap.containsKey(cm.getId()))
				cmMap.put(cm.getId(), cm);
		}

		if (personelDenklestirmeler != null)
			personelDenklestirmeler.clear();
		if (devredenBakiyeDosya != null)
			devredenBakiyeDosya.setDosyaIcerik(null);

		if (calismaModeliList != null && !calismaModeliList.isEmpty()) {
			boolean flush = false, renk = Boolean.FALSE;
			try {
				cal.setTime(PdksUtil.getDate(cal.getTime()));
				Integer otomatikOnayIKGun = null;
				String str = ortakIslemler.getParameterKey("otomatikOnayIKGun");
				if (PdksUtil.hasStringValue(str))
					try {
						otomatikOnayIKGun = Integer.parseInt(str);
						if (otomatikOnayIKGun < 1 || otomatikOnayIKGun > 28)
							otomatikOnayIKGun = null;
					} catch (Exception e) {
						otomatikOnayIKGun = null;
					}
				if (otomatikOnayIKGun == null)
					otomatikOnayIKGun = 6;
				for (DenklestirmeAy da : denklestirmeAylar) {
					if (da.getOtomatikOnayIKTarih() == null) {
						cal.set(Calendar.YEAR, da.getYil());
						cal.set(Calendar.MONTH, da.getAy() - 1);
						cal.add(Calendar.MONTH, 1);
						cal.set(Calendar.DATE, otomatikOnayIKGun);
						da.setOtomatikOnayIKTarih(cal.getTime());
						pdksEntityController.saveOrUpdate(xSession, entityManager, da);
						flush = true;

					}
					TreeMap<Long, CalismaModeliAy> modelDenkMap = new TreeMap<Long, CalismaModeliAy>();
					da.setModelMap(modelDenkMap);
					da.setModeller(new ArrayList<CalismaModeliAy>());
					da.setTrClass(renk ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
					for (Iterator iterator = calismaModeliList.iterator(); iterator.hasNext();) {
						CalismaModeli cm = (CalismaModeli) iterator.next();
						CalismaModeliAy calismaModeliAy = null;
						String key = CalismaModeliAy.getKey(da, cm);
						if (cm.getDurum().booleanValue() == false) {
							continue;
						}
						if (!modelMap.containsKey(key)) {
							calismaModeliAy = new CalismaModeliAy(da, cm);
							calismaModeliAy.setDurum(Boolean.FALSE);
							pdksEntityController.saveOrUpdate(xSession, entityManager, calismaModeliAy);
							flush = true;
						} else
							calismaModeliAy = (CalismaModeliAy) modelMap.get(key);
						modelDenkMap.put(cm.getId(), calismaModeliAy);
						da.getModeller().add(calismaModeliAy);
					}
					renk = !renk;
				}
				if (flush)
					xSession.flush();
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

		}
		calismaModeliList = cmMap.isEmpty() ? null : new ArrayList<CalismaModeli>(cmMap.values());
		return "";
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		Calendar calendar = Calendar.getInstance();
		yil = calendar.get(Calendar.YEAR);
		calendar.add(Calendar.MONTH, 1);
		maxYil = String.valueOf(calendar.get(Calendar.YEAR));
		yilAyKontrol(session);

	}

	/**
	 * @param xSession
	 */
	private void fillCalismaModeller(Session xSession) {
		HashMap fields = new HashMap();
		fields.put("durum", Boolean.TRUE);
		if (xSession != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, xSession);
		calismaModeliList = pdksEntityController.getObjectByInnerObjectList(fields, CalismaModeli.class);
		if (calismaModeliList.size() > 1)
			calismaModeliList = PdksUtil.sortListByAlanAdi(calismaModeliList, "id", false);
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public String bakiyeDosyaSifirla() throws Exception {
		if (personelDenklestirmeler == null)
			personelDenklestirmeler = new ArrayList<PersonelDenklestirme>();
		else
			personelDenklestirmeler.clear();
		devredenBakiyeDosya.setDosyaIcerik(null);
		return "";
	}

	/**
	 * @param event
	 * @throws Exception
	 */
	public void listenerDevirBakiyeDosya(UploadEvent event) throws Exception {
		UploadItem item = event.getUploadItem();
		PdksUtil.getDosya(item, devredenBakiyeDosya);
		if (personelDenklestirmeler == null)
			personelDenklestirmeler = new ArrayList<PersonelDenklestirme>();
		else
			personelDenklestirmeler.clear();

	}

	/**
	 * @return
	 */
	@Transactional
	public String devredenBakiyeDosyaOku() {
		personelDenklestirmeler.clear();
		try {

			Workbook wb = ortakIslemler.getWorkbook(devredenBakiyeDosya);

			if (wb != null) {
				Sheet sheet = wb.getSheetAt(0);
				// logger.info(sheet.getSheetName());
				int COL_SICIL_NO = 0;
				int COL_YIL = 2;
				int COL_AY = 3;
				int COL_DEVIR_BAKIYE = 4;
				String perSicilNo = null;
				List<String> siciller = new ArrayList<String>();
				LinkedHashMap<String, HashMap<Integer, org.apache.poi.ss.usermodel.Cell>> hucreMap = new LinkedHashMap<String, HashMap<Integer, org.apache.poi.ss.usermodel.Cell>>();
				String sicilNoUzunlukStr = ortakIslemler.getParameterKey("sicilNoUzunluk");
				int maxTextLength = 0;
				try {
					if (PdksUtil.hasStringValue(sicilNoUzunlukStr))
						maxTextLength = Integer.parseInt(sicilNoUzunlukStr);
				} catch (Exception e) {
					maxTextLength = 0;
				}
				int sonSatir = sheet.getLastRowNum();
				for (int row = 1; row <= sonSatir; row++) {
					try {
						String key = null;
						try {
							perSicilNo = getSheetStringValue(sheet, row, COL_SICIL_NO);
							if (maxTextLength > 0 && perSicilNo != null && perSicilNo.trim().length() < maxTextLength)
								perSicilNo = PdksUtil.textBaslangicinaKarakterEkle(perSicilNo, '0', maxTextLength);
							key = perSicilNo + "_" + getSheetStringValue(sheet, row, COL_YIL) + "_" + getSheetStringValue(sheet, row, COL_AY);
						} catch (Exception e) {
							logger.error("PDKS hata in : \n");
							e.printStackTrace();
							logger.error("PDKS hata out : " + e.getMessage());
							break;
						}
						if (!siciller.contains(perSicilNo))
							siciller.add(perSicilNo);
						HashMap<Integer, org.apache.poi.ss.usermodel.Cell> veriMap = new HashMap<Integer, org.apache.poi.ss.usermodel.Cell>();
						for (Integer col = 0; col <= COL_DEVIR_BAKIYE; col++) {
							org.apache.poi.ss.usermodel.Cell cell = ExcelUtil.getCell(sheet, row, col);
							veriMap.put(col, cell);
						}
						hucreMap.put(key, veriMap);
					} catch (Exception e) {
						logger.error("PDKS hata in : \n");
						e.printStackTrace();
						logger.error("PDKS hata out : " + e.getMessage());

					}

				}
				if (!hucreMap.isEmpty()) {
					HashMap fields = new HashMap();
					StringBuffer sb = new StringBuffer();
					sb.append("SELECT  V." + Personel.COLUMN_NAME_ID + " FROM " + Personel.TABLE_NAME + " V WITH(nolock) ");
					sb.append(" WHERE " + Personel.COLUMN_NAME_PDKS_SICIL_NO + " :pId  ");
					sb.append(" AND V." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= :basTarih ");
					sb.append(" AND V." + Personel.COLUMN_NAME_GRUBA_GIRIS_TARIHI + " <= :bitTarih ");
					Calendar cal = Calendar.getInstance();
					cal.set(denklestirmeAy.getYil(), denklestirmeAy.getAy() - 1, 1);
					Date basTarih = PdksUtil.getDate(cal.getTime());
					cal.add(Calendar.MONTH, 1);
					cal.add(Calendar.DATE, -1);
					Date bitTarih = PdksUtil.getDate(cal.getTime());
					fields.put("pId", siciller);
					fields.put("basTarih", basTarih);
					fields.put("bitTarih", bitTarih);
					fields.put(PdksEntityController.MAP_KEY_MAP, "getPdksSicilNo");
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					TreeMap<String, Personel> personelMap = ortakIslemler.getDataByIdMap(sb, fields, Personel.TABLE_NAME, Personel.class);
					sb = null;
					if (!personelMap.isEmpty()) {
						int sonrakiYil = denklestirmeAy.getYil();
						int sonrakiAy = denklestirmeAy.getAy() + 1;
						if (sonrakiAy > 12) {
							sonrakiAy = 1;
							sonrakiYil++;
						}
						HashMap map = new HashMap();
						map.put("yil", sonrakiYil);
						map.put("ay", sonrakiAy);
						if (session != null)
							map.put(PdksEntityController.MAP_KEY_SESSION, session);
						DenklestirmeAy sonrakiDonem = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(map, DenklestirmeAy.class);

						List personelIdler = new ArrayList();
						for (Iterator iterator = personelMap.keySet().iterator(); iterator.hasNext();) {
							String key = (String) iterator.next();
							personelIdler.add(personelMap.get(key).getId());
						}
						fields.clear();
						sb = new StringBuffer();
						sb.append("SELECT  V." + PersonelDenklestirme.COLUMN_NAME_ID + " FROM " + PersonelDenklestirme.TABLE_NAME + " V WITH(nolock) ");
						sb.append(" WHERE " + PersonelDenklestirme.COLUMN_NAME_DONEM + " = :denklestirmeAy AND " + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " :pId  ");
						fields.put(PdksEntityController.MAP_KEY_MAP, "getSicilNo");
						fields.put("denklestirmeAy", denklestirmeAy.getId());
						fields.put("pId", personelIdler);
						if (session != null)
							fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						TreeMap<String, PersonelDenklestirme> bakiyeMap = ortakIslemler.getDataByIdMap(sb, fields, PersonelDenklestirme.TABLE_NAME, PersonelDenklestirme.class);
						bakiySonrakiMap = null;
						if (sonrakiDonem != null) {
							sb = new StringBuffer();
							sb.append("SELECT  V." + PersonelDenklestirme.COLUMN_NAME_ID + " FROM " + PersonelDenklestirme.TABLE_NAME + " V WITH(nolock) ");
							sb.append(" WHERE " + PersonelDenklestirme.COLUMN_NAME_DONEM + " = :denklestirmeAy AND " + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " :pId  ");
							sb.append(" AND GECEN_AY_DENKLESTIRME_ID IS NULL ");
							fields.put(PdksEntityController.MAP_KEY_MAP, "getSicilNo");
							fields.put("denklestirmeAy", sonrakiDonem.getId());
							fields.put("pId", personelIdler);
							if (session != null)
								fields.put(PdksEntityController.MAP_KEY_SESSION, session);
							bakiySonrakiMap = ortakIslemler.getDataByIdMap(sb, fields, PersonelDenklestirme.TABLE_NAME, PersonelDenklestirme.class);
						} else
							bakiySonrakiMap = new TreeMap<String, PersonelDenklestirme>();
						List<HashMap<Integer, org.apache.poi.ss.usermodel.Cell>> hucreler = new ArrayList<HashMap<Integer, org.apache.poi.ss.usermodel.Cell>>(hucreMap.values());
						boolean flush = false;
						for (Iterator iterator = hucreler.iterator(); iterator.hasNext();) {
							HashMap<Integer, org.apache.poi.ss.usermodel.Cell> veriMap = (HashMap<Integer, org.apache.poi.ss.usermodel.Cell>) iterator.next();
							try {
								perSicilNo = ExcelUtil.getSheetStringValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_SICIL_NO));
								if (maxTextLength > 0 && perSicilNo != null && perSicilNo.trim().length() < maxTextLength)
									perSicilNo = PdksUtil.textBaslangicinaKarakterEkle(perSicilNo, '0', maxTextLength);
								int yilExcel = Integer.parseInt(ExcelUtil.getSheetStringValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_YIL)));
								int ayExcel = Integer.parseInt(ExcelUtil.getSheetStringValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_AY)));
								if (yilExcel == denklestirmeAy.getYil() && ayExcel == denklestirmeAy.getAy() && personelMap.containsKey(perSicilNo)) {
									Personel pdksPersonel = personelMap.get(perSicilNo);
									PersonelDenklestirme personelDenklestirmeDB = null;
									double devredenSure = ExcelUtil.getSheetDoubleValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_DEVIR_BAKIYE)).doubleValue();
									if (!bakiyeMap.containsKey(perSicilNo)) {
										personelDenklestirmeDB = new PersonelDenklestirme();
										personelDenklestirmeDB.setDenklestirmeAy(denklestirmeAy);
										personelDenklestirmeDB.setPersonel(pdksPersonel);
										ortakIslemler.setCalismaModeliAy(personelDenklestirmeDB, session);
										personelDenklestirmeDB.setDevredenSure(-devredenSure);
										personelDenklestirmeDB.setErpAktarildi(Boolean.FALSE);
									} else {
										personelDenklestirmeDB = bakiyeMap.get(perSicilNo);
										if (bakiySonrakiMap.containsKey(perSicilNo)) {
											PersonelDenklestirme personelDenklestirmeYeni = bakiySonrakiMap.get(perSicilNo);
											personelDenklestirmeYeni.setPersonelDenklestirmeGecenAy(personelDenklestirmeDB);
											pdksEntityController.saveOrUpdate(session, entityManager, personelDenklestirmeYeni);
											bakiySonrakiMap.remove(perSicilNo);
											flush = true;
										}
									}

									PersonelDenklestirme pdksPersonelDenklestirme = new PersonelDenklestirme();
									pdksPersonelDenklestirme.setDenklestirmeAy(denklestirmeAy);
									pdksPersonelDenklestirme.setPersonel(pdksPersonel);
									ortakIslemler.setCalismaModeliAy(personelDenklestirmeDB, session);
									pdksPersonelDenklestirme.setSuaDurum(pdksPersonel.getSuaOlabilir());
									pdksPersonelDenklestirme.setOnaylandi(Boolean.TRUE);
									pdksPersonelDenklestirme.setDevredenSure(devredenSure);
									pdksPersonelDenklestirme.setPersonelDenklestirmeDB(personelDenklestirmeDB);
									personelDenklestirmeler.add(pdksPersonelDenklestirme);
								}
							} catch (Exception e1) {
								logger.info(e1.getMessage());
								e1.printStackTrace();
							}

						}
						if (flush)
							session.flush();
						personelIdler = null;
					}
				}

			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		if (personelDenklestirmeler.isEmpty())
			PdksUtil.addMessageWarn(denklestirmeAy.getYil() + " " + denklestirmeAy.getAyAdi() + " ayına ait devreden mesai bilgisi bulunamadı!");
		devredenBakiyeDosya.setDosyaIcerik(null);
		return "";
	}

	/**
	 * @return
	 */
	@Transactional
	public String kismiOdemeDosyaOku() {
		//

		personelDenklestirmeler.clear();
		try {

			Workbook wb = ortakIslemler.getWorkbook(devredenBakiyeDosya);

			if (wb != null) {
				Sheet sheet = wb.getSheetAt(0);
				// logger.info(sheet.getSheetName());
				int COL_SICIL_NO = 0;
				int COL_YIL = 2;
				int COL_AY = 3;
				int COL_KISMI_SAAT = 4;
				String perSicilNo = null;
				List<String> siciller = new ArrayList<String>();
				LinkedHashMap<String, HashMap<Integer, org.apache.poi.ss.usermodel.Cell>> hucreMap = new LinkedHashMap<String, HashMap<Integer, org.apache.poi.ss.usermodel.Cell>>();
				String sicilNoUzunlukStr = ortakIslemler.getParameterKey("sicilNoUzunluk");
				int maxTextLength = 0;
				try {
					if (PdksUtil.hasStringValue(sicilNoUzunlukStr))
						maxTextLength = Integer.parseInt(sicilNoUzunlukStr);
				} catch (Exception e) {
					maxTextLength = 0;
				}
				int sonSatir = sheet.getLastRowNum();
				for (int row = 1; row <= sonSatir; row++) {
					try {
						String key = null;
						try {
							perSicilNo = getSheetStringValue(sheet, row, COL_SICIL_NO);
							if (maxTextLength > 0 && perSicilNo != null && perSicilNo.trim().length() < maxTextLength)
								perSicilNo = PdksUtil.textBaslangicinaKarakterEkle(perSicilNo, '0', maxTextLength);
							key = perSicilNo + "_" + getSheetStringValue(sheet, row, COL_YIL) + "_" + getSheetStringValue(sheet, row, COL_AY);
						} catch (Exception e) {
							logger.error("PDKS hata in : \n");
							e.printStackTrace();
							logger.error("PDKS hata out : " + e.getMessage());
							break;
						}
						if (!siciller.contains(perSicilNo))
							siciller.add(perSicilNo);
						HashMap<Integer, org.apache.poi.ss.usermodel.Cell> veriMap = new HashMap<Integer, org.apache.poi.ss.usermodel.Cell>();
						for (Integer col = 0; col <= COL_KISMI_SAAT; col++) {
							org.apache.poi.ss.usermodel.Cell cell = ExcelUtil.getCell(sheet, row, col);
							veriMap.put(col, cell);
						}
						hucreMap.put(key, veriMap);
					} catch (Exception e) {
						logger.error("PDKS hata in : \n");
						e.printStackTrace();
						logger.error("PDKS hata out : " + e.getMessage());

					}

				}
				if (!hucreMap.isEmpty()) {
					HashMap fields = new HashMap();
					StringBuffer sb = new StringBuffer();
					sb.append("SELECT  V." + Personel.COLUMN_NAME_ID + " FROM " + Personel.TABLE_NAME + " V WITH(nolock) ");
					sb.append(" WHERE " + Personel.COLUMN_NAME_PDKS_SICIL_NO + " :pId  ");
					sb.append(" AND V." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= :basTarih ");
					sb.append(" AND V." + Personel.COLUMN_NAME_GRUBA_GIRIS_TARIHI + " <= :bitTarih ");
					Calendar cal = Calendar.getInstance();
					cal.set(denklestirmeAy.getYil(), denklestirmeAy.getAy() - 1, 1);
					Date basTarih = PdksUtil.getDate(cal.getTime());
					cal.add(Calendar.MONTH, 1);
					cal.add(Calendar.DATE, -1);
					Date bitTarih = PdksUtil.getDate(cal.getTime());
					fields.put("pId", siciller);
					fields.put("basTarih", basTarih);
					fields.put("bitTarih", bitTarih);
					fields.put(PdksEntityController.MAP_KEY_MAP, "getPdksSicilNo");
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					TreeMap<String, Personel> personelMap = ortakIslemler.getDataByIdMap(sb, fields, Personel.TABLE_NAME, Personel.class);
					sb = null;
					if (!personelMap.isEmpty()) {
						List personelIdler = new ArrayList();
						for (Iterator iterator = personelMap.keySet().iterator(); iterator.hasNext();) {
							String key = (String) iterator.next();
							personelIdler.add(personelMap.get(key).getId());
						}
						fields.clear();
						sb = new StringBuffer();
						sb.append("SELECT  V." + PersonelDenklestirme.COLUMN_NAME_ID + " FROM " + PersonelDenklestirme.TABLE_NAME + " V WITH(nolock) ");
						sb.append(" WHERE " + PersonelDenklestirme.COLUMN_NAME_DONEM + " = :denklestirmeAy AND " + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " :pId  ");
						fields.put(PdksEntityController.MAP_KEY_MAP, "getSicilNo");
						fields.put("denklestirmeAy", denklestirmeAy.getId());
						fields.put("pId", personelIdler);
						if (session != null)
							fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						TreeMap<String, PersonelDenklestirme> bakiyeMap = ortakIslemler.getDataByIdMap(sb, fields, PersonelDenklestirme.TABLE_NAME, PersonelDenklestirme.class);

						List<HashMap<Integer, org.apache.poi.ss.usermodel.Cell>> hucreler = new ArrayList<HashMap<Integer, org.apache.poi.ss.usermodel.Cell>>(hucreMap.values());
						boolean flush = false;
						for (Iterator iterator = hucreler.iterator(); iterator.hasNext();) {
							HashMap<Integer, org.apache.poi.ss.usermodel.Cell> veriMap = (HashMap<Integer, org.apache.poi.ss.usermodel.Cell>) iterator.next();
							try {
								perSicilNo = ExcelUtil.getSheetStringValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_SICIL_NO));
								if (maxTextLength > 0 && perSicilNo != null && perSicilNo.trim().length() < maxTextLength)
									perSicilNo = PdksUtil.textBaslangicinaKarakterEkle(perSicilNo, '0', maxTextLength);
								int yilExcel = Integer.parseInt(ExcelUtil.getSheetStringValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_YIL)));
								int ayExcel = Integer.parseInt(ExcelUtil.getSheetStringValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_AY)));
								if (yilExcel == denklestirmeAy.getYil() && ayExcel == denklestirmeAy.getAy() && personelMap.containsKey(perSicilNo)) {
									Personel pdksPersonel = personelMap.get(perSicilNo);
									PersonelDenklestirme personelDenklestirmeDB = null;
									double kismiOdemeSure = ExcelUtil.getSheetDoubleValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_KISMI_SAAT)).doubleValue();
									PersonelDenklestirme pdksPersonelDenklestirme = new PersonelDenklestirme();
									if (!bakiyeMap.containsKey(perSicilNo)) {
										personelDenklestirmeDB = new PersonelDenklestirme();
										personelDenklestirmeDB.setDenklestirmeAy(denklestirmeAy);
										personelDenklestirmeDB.setPersonel(pdksPersonel);
										ortakIslemler.setCalismaModeliAy(personelDenklestirmeDB, session);
										if (pdksPersonel.getFazlaMesaiIzinKullan())
											personelDenklestirmeDB.setKismiOdemeSure(-kismiOdemeSure);
										personelDenklestirmeDB.setErpAktarildi(Boolean.FALSE);
										pdksPersonelDenklestirme.setFazlaMesaiIzinKullan(false);
									} else {
										personelDenklestirmeDB = bakiyeMap.get(perSicilNo);
										pdksPersonelDenklestirme.setFazlaMesaiIzinKullan(personelDenklestirmeDB.getFazlaMesaiIzinKullan() && kismiOdemeSure >= 0.0d);
									}
									pdksPersonelDenklestirme.setDenklestirmeAy(denklestirmeAy);
									pdksPersonelDenklestirme.setPersonel(pdksPersonel);
									ortakIslemler.setCalismaModeliAy(personelDenklestirmeDB, session);
									pdksPersonelDenklestirme.setSuaDurum(pdksPersonel.getSuaOlabilir());
									pdksPersonelDenklestirme.setOnaylandi(Boolean.TRUE);
									pdksPersonelDenklestirme.setKismiOdemeSure(kismiOdemeSure);
									pdksPersonelDenklestirme.setPersonelDenklestirmeDB(personelDenklestirmeDB);
									personelDenklestirmeler.add(pdksPersonelDenklestirme);
								}
							} catch (Exception e1) {
								logger.info(e1.getMessage());
								e1.printStackTrace();
							}

						}
						if (flush)
							session.flush();
						personelIdler = null;
					}
				}

			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		if (personelDenklestirmeler.isEmpty())
			PdksUtil.addMessageWarn(denklestirmeAy.getYil() + " " + denklestirmeAy.getAyAdi() + " ayına ait devreden mesai bilgisi bulunamadı!");
		devredenBakiyeDosya.setDosyaIcerik(null);
		return "";

	}

	@Transactional
	public String kismiOdemeDosyaYaz() {
		boolean flush = Boolean.FALSE;
		int adet = 0;
		for (Iterator iterator = personelDenklestirmeler.iterator(); iterator.hasNext();) {
			PersonelDenklestirme personelDenklestirme = (PersonelDenklestirme) iterator.next();
			if (personelDenklestirme.getFazlaMesaiIzinKullan().equals(Boolean.FALSE))
				continue;
			PersonelDenklestirme personelDenklestirmeDB = personelDenklestirme.getPersonelDenklestirmeDB() != null ? personelDenklestirme.getPersonelDenklestirmeDB() : personelDenklestirme;
			adet++;
			if (personelDenklestirmeDB.getId() != null && personelDenklestirmeDB.getKismiOdemeSure().doubleValue() == personelDenklestirme.getKismiOdemeSure().doubleValue())
				continue;
			personelDenklestirmeDB.setDurum(personelDenklestirmeDB.getId() != null && personelDenklestirmeDB.getKismiOdemeSure().doubleValue() != personelDenklestirme.getKismiOdemeSure().doubleValue());
			personelDenklestirmeDB.setKismiOdemeSure(personelDenklestirme.getKismiOdemeSure());
			if (personelDenklestirmeDB.getId() != null) {
				personelDenklestirmeDB.setGuncellemeTarihi(new Date());
				personelDenklestirmeDB.setGuncelleyenUser(authenticatedUser);
			} else {
				personelDenklestirmeDB.setOlusturmaTarihi(new Date());
				personelDenklestirmeDB.setOlusturanUser(authenticatedUser);
			}
			pdksEntityController.saveOrUpdate(session, entityManager, personelDenklestirmeDB);

		}
		if (adet == 0)
			PdksUtil.addMessageAvailableWarn("İşlem yapılacak kayıt yok!");
		else if (flush)
			session.flush();
		return "";
	}

	@Transactional
	public String devredenBakiyeDosyaYaz() {
		boolean flush = Boolean.FALSE;
		for (Iterator iterator = personelDenklestirmeler.iterator(); iterator.hasNext();) {
			PersonelDenklestirme personelDenklestirme = (PersonelDenklestirme) iterator.next();
			PersonelDenklestirme personelDenklestirmeDB = personelDenklestirme.getPersonelDenklestirmeDB() != null ? personelDenklestirme.getPersonelDenklestirmeDB() : personelDenklestirme;
			if (personelDenklestirmeDB.isOnaylandi() == false || personelDenklestirmeDB.getDurum().equals(Boolean.FALSE) || personelDenklestirmeDB.getDevredenSure() == null || !personelDenklestirmeDB.getDevredenSure().equals(personelDenklestirme.getDevredenSure())) {
				Personel pdksPersonel = personelDenklestirmeDB.getPersonel();
				personelDenklestirmeDB.setDevredenSure(personelDenklestirme.getDevredenSure());
				personelDenklestirmeDB.setOnaylandi(Boolean.TRUE);
				personelDenklestirmeDB.setDenklestirme(Boolean.TRUE);
				personelDenklestirmeDB.setDenklestirme(pdksPersonel.getGebeMi() == null || !pdksPersonel.getGebeMi().booleanValue());
				personelDenklestirmeDB.setDurum(Boolean.TRUE);
				if (personelDenklestirmeDB.getId() != null) {
					personelDenklestirmeDB.setGuncellemeTarihi(new Date());
					personelDenklestirmeDB.setGuncelleyenUser(authenticatedUser);
				} else {
					personelDenklestirmeDB.setOlusturmaTarihi(new Date());
					personelDenklestirmeDB.setOlusturanUser(authenticatedUser);
				}
				pdksEntityController.saveOrUpdate(session, entityManager, personelDenklestirmeDB);
				String perSicilNo = personelDenklestirmeDB.getPersonel().getPdksSicilNo();
				if (bakiySonrakiMap.containsKey(perSicilNo)) {
					PersonelDenklestirme personelDenklestirmeYeni = bakiySonrakiMap.get(perSicilNo);
					personelDenklestirmeYeni.setPersonelDenklestirmeGecenAy(personelDenklestirmeDB);
					pdksEntityController.saveOrUpdate(session, entityManager, personelDenklestirmeYeni);
					bakiySonrakiMap.remove(perSicilNo);
					flush = true;
				}
				flush = Boolean.TRUE;
			}

		}
		if (flush)
			session.flush();
		personelDenklestirmeler.clear();
		devredenBakiyeDosya.setDosyaIcerik(null);
		return "";
	}

	private String getSheetStringValue(Sheet sheet, int row, int col) throws Exception {
		String value = null;

		try {
			value = ExcelUtil.getSheetStringValue(sheet, row, col);
			if (value == null)
				value = String.valueOf(ExcelUtil.getSheetDoubleValue(sheet, row, col).longValue());
		} catch (Exception e) {
			value = String.valueOf(ExcelUtil.getSheetDoubleValue(sheet, row, col).longValue());

		}
		return value;
	}

	/**
	 * @param xSession
	 * @return
	 */
	public String yilAyKontrol(Session xSession) {
		if (xSession == null)
			xSession = session;
		xSession.clear();
 		fillCalismaModeller(xSession);
		HashMap map = new HashMap();
		map.put(PdksEntityController.MAP_KEY_MAP, "getAy");
		map.put("yil=", yil);

		if (xSession != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, xSession);
		TreeMap<Integer, DenklestirmeAy> ayMap = pdksEntityController.getObjectByInnerObjectMapInLogic(map, DenklestirmeAy.class, false);

		try {
			kesintiTuruList.clear();
			denklestirmeKesintiYap = ortakIslemler.yilAyKontrol(yil, ayMap, xSession);
		} catch (Exception e) {
			e.printStackTrace();
		}

		fillPdksYoneticiDenklestirme(xSession);
		return "";
	}

	/**
	 * @param value
	 * @return
	 */
	public String guncelle(DenklestirmeAy value) {
		setInstance(value);
		if (personelDenklestirmeler != null)
			personelDenklestirmeler.clear();
		denklestirmeAy = getInstance();

		int donem = Integer.parseInt(PdksUtil.convertToDateString(new Date(), "yyyyMM")), seciliDonem = yil * 100 + denklestirmeAy.getAy();
		disabled = seciliDonem > donem;

		int adet = getGirisKolonSayisi();
		kesintiTuruList.clear();
		if (authenticatedUser.isAdmin() && adet > 2 && (!denklestirmeAy.isKesintiYok() || denklestirmeKesintiYap)) {
			kesintiTuruList.add(new SelectItem(KesintiTipi.KESINTI_YOK.value(), DenklestirmeAy.getKesintiAciklama(KesintiTipi.KESINTI_YOK.value())));
			kesintiTuruList.add(new SelectItem(KesintiTipi.SAAT.value(), DenklestirmeAy.getKesintiAciklama(KesintiTipi.SAAT.value())));
			kesintiTuruList.add(new SelectItem(KesintiTipi.GUN.value(), DenklestirmeAy.getKesintiAciklama(KesintiTipi.GUN.value())));
		}

		return "";
	}

	@Transactional
	public String kaydet() {

		try {
			denklestirmeAy.setGuncellemeTarihi(new Date());
			denklestirmeAy.setGuncelleyenUser(authenticatedUser);
			pdksEntityController.saveOrUpdate(session, entityManager, denklestirmeAy);
			session.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

		fillPdksYoneticiDenklestirme(session);
		return "";
	}

	public List<DenklestirmeAy> getAylikList() {
		return aylikList;
	}

	public void setAylikList(List<DenklestirmeAy> aylikList) {
		this.aylikList = aylikList;
	}

	public String getMaxYil() {
		return maxYil;
	}

	public void setMaxYil(String maxYil) {
		this.maxYil = maxYil;
	}

	public double getSure() {
		return sure;
	}

	public void setSure(double sure) {
		this.sure = sure;
	}

	public int getYilEdit() {
		return yilEdit;
	}

	public void setYilEdit(int yilEdit) {
		this.yilEdit = yilEdit;
	}

	public int getYilSelect() {
		return yilSelect;
	}

	public void setYilSelect(int yilSelect) {
		this.yilSelect = yilSelect;
	}

	public String getVardiyaTanimKodu() {
		return vardiyaTanimKodu;
	}

	public void setVardiyaTanimKodu(String vardiyaTanimKodu) {
		this.vardiyaTanimKodu = vardiyaTanimKodu;
	}

	public int getYilModal() {
		return yilModal;
	}

	public void setYilModal(int yilModal) {
		this.yilModal = yilModal;
	}

	public int getYil() {
		return yil;
	}

	public void setYil(int yil) {
		this.yil = yil;
	}

	public DenklestirmeAy getDenklestirmeAy() {
		return denklestirmeAy;
	}

	public void setDenklestirmeAy(DenklestirmeAy denklestirmeAy) {
		this.denklestirmeAy = denklestirmeAy;
	}

	public boolean isAktif() {
		return aktif;
	}

	public void setAktif(boolean aktif) {
		this.aktif = aktif;
	}

	public boolean isGuncelle() {
		return guncelle;
	}

	public void setGuncelle(boolean guncelle) {
		this.guncelle = guncelle;
	}

	public List<DenklestirmeAy> getDenklestirmeAylar() {
		return denklestirmeAylar;

	}

	public void setDenklestirmeAylar(List<DenklestirmeAy> denklestirmeAylar) {
		this.denklestirmeAylar = denklestirmeAylar;
	}

	public List<PersonelDenklestirme> getPersonelDenklestirmeler() {
		return personelDenklestirmeler;
	}

	public void setPersonelDenklestirmeler(List<PersonelDenklestirme> personelDenklestirmeler) {
		this.personelDenklestirmeler = personelDenklestirmeler;
	}

	public Dosya getDevredenBakiyeDosya() {
		return devredenBakiyeDosya;
	}

	public void setDevredenBakiyeDosya(Dosya devredenBakiyeDosya) {
		this.devredenBakiyeDosya = devredenBakiyeDosya;
	}

	public List<CalismaModeli> getCalismaModeliList() {
		return calismaModeliList;
	}

	public void setCalismaModeliList(List<CalismaModeli> calismaModeliList) {
		this.calismaModeliList = calismaModeliList;
	}

	public boolean isDenklestirmeKesintiYap() {
		return denklestirmeKesintiYap;
	}

	public void setDenklestirmeKesintiYap(boolean denklestirmeKesintiYap) {
		this.denklestirmeKesintiYap = denklestirmeKesintiYap;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public List<SelectItem> getKesintiTuruList() {
		return kesintiTuruList;
	}

	public void setKesintiTuruList(List<SelectItem> kesintiTuruList) {
		this.kesintiTuruList = kesintiTuruList;
	}

	public Boolean getHareketKaydiVardiyaBul() {
		return hareketKaydiVardiyaBul;
	}

	public void setHareketKaydiVardiyaBul(Boolean hareketKaydiVardiyaBul) {
		this.hareketKaydiVardiyaBul = hareketKaydiVardiyaBul;
	}

	public Boolean getNegatifBakiyeDenkSaat() {
		return negatifBakiyeDenkSaat;
	}

	public void setNegatifBakiyeDenkSaat(Boolean negatifBakiyeDenkSaat) {
		this.negatifBakiyeDenkSaat = negatifBakiyeDenkSaat;
	}

	/**
	 * @return the otomatikFazlaCalismaOnaylansinVar
	 */
	public Boolean getOtomatikFazlaCalismaOnaylansinVar() {
		return otomatikFazlaCalismaOnaylansinVar;
	}

	/**
	 * @param otomatikFazlaCalismaOnaylansinVar
	 *            the otomatikFazlaCalismaOnaylansinVar to set
	 */
	public void setOtomatikFazlaCalismaOnaylansinVar(Boolean otomatikFazlaCalismaOnaylansinVar) {
		this.otomatikFazlaCalismaOnaylansinVar = otomatikFazlaCalismaOnaylansinVar;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public TreeMap<String, CalismaModeliAy> getModelMap() {
		return modelMap;
	}

	public void setModelMap(TreeMap<String, CalismaModeliAy> modelMap) {
		this.modelMap = modelMap;
	}

}
