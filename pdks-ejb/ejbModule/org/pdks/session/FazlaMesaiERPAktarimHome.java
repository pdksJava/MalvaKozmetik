package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URLEncoder;
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
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.CalismaModeliAy;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Departman;
import org.pdks.entity.Dosya;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelDenklestirmeOnaylanmayan;
import org.pdks.entity.PersonelMesai;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.erp.action.ERPController;
import org.pdks.security.entity.User;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

import com.pdks.webservice.GetFazlaMesaiListResponse;
import com.pdks.webservice.PdksSoapVeriAktar;

/**
 * @author Hasan Sayar
 * 
 */
@Name("fazlaMesaiERPAktarimHome")
public class FazlaMesaiERPAktarimHome extends EntityHome<DenklestirmeAy> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2336227657117528337L;
	static Logger logger = Logger.getLogger(FazlaMesaiERPAktarimHome.class);

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
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;

	private List<PersonelDenklestirme> personelDenklestirmeList, onaysizPersonelDenklestirmeList, personelDenklestirmeler;

	private Boolean secimDurum = Boolean.FALSE, sureDurum, fazlaMesaiDurum, haftaTatilDurum, resmiTatilDurum, durumERP, onaylanmayanDurum, personelERP, modelGoster = Boolean.FALSE;

	private int ay, yil, maxYil, sanalPersonelDurum;

	private List<SelectItem> aylar;

	private String sicilNo = "", sanalPersonelAciklama, bolumAciklama;

	private Date basGun, bitGun;

	private Sirket sirket;

	private Long sirketId, departmanId;

	private List<SelectItem> sirketler, departmanList;

	private Departman departman;
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private Dosya fazlaMesaiDosya = new Dosya();
	private Boolean aksamGun = Boolean.FALSE, haftaCalisma = Boolean.FALSE, aksamSaat = Boolean.FALSE, denklestirmeAyDurum, erpAktarimDurum = Boolean.FALSE;
	private List<Vardiya> izinTipiVardiyaList;
	private TreeMap<String, TreeMap<String, List<VardiyaGun>>> izinTipiPersonelVardiyaMap;
	private TreeMap<Long, Personel> izinTipiPersonelMap;
	private Session session;
	private boolean tesisVar = Boolean.FALSE, bolumVar = Boolean.FALSE, bordroAltAlanVar = Boolean.FALSE;

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

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() {
		aylar = PdksUtil.getAyListesi(Boolean.TRUE);
		String sapControllerStr = ortakIslemler.getParameterKey("sapController");
		erpAktarimDurum = sapControllerStr.equals("2") || sapControllerStr.equals("3");
		Calendar cal = Calendar.getInstance();
		ortakIslemler.gunCikar(cal, 2);

		modelGoster = Boolean.FALSE;
		sanalPersonelAciklama = ortakIslemler.sanalPersonelAciklama();
		if (!sanalPersonelAciklama.equals("")) {
			sanalPersonelDurum = 0;
		}

		ay = cal.get(Calendar.MONTH) + 1;
		yil = cal.get(Calendar.YEAR);
		maxYil = yil + 1;
		sicilNo = "";
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		setDepartmanId(null);
		setDepartman(null);
		setInstance(new DenklestirmeAy());
		setPersonelDenklestirmeList(new ArrayList<PersonelDenklestirme>());
		setOnaysizPersonelDenklestirmeList(null);
		durumERP = Boolean.FALSE;
		personelERP = Boolean.FALSE;
		onaylanmayanDurum = null;
		sirket = null;
		sirketId = null;
		sirketler = null;
		if (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin())
			filDepartmanList();
		if (departmanList.size() == 1)
			setDepartmanId((Long) departmanList.get(0).getValue());
		if (!authenticatedUser.isAdmin()) {
			setDepartmanId(authenticatedUser.getDepartman().getId());
			if (authenticatedUser.isIK())
				fillSirketList();
		}

		// return ortakIslemler.yetkiIKAdmin(Boolean.FALSE);
		fillEkSahaTanim();
		return "";

	}

	private String getSheetStringValue(Sheet sheet, int row, int col) throws Exception {
		String value = null;

		try {
			value = ExcelUtil.getSheetStringValue(sheet, row, col);
		} catch (Exception e) {
			value = String.valueOf(ExcelUtil.getSheetDoubleValue(sheet, row, col).longValue());

		}
		return value;
	}

	public String fazlaMesaiDosyaOku() {

		personelDenklestirmeler.clear();
		DenklestirmeAy denklestirmeAy = getInstance();
		try {
			Workbook wb = ortakIslemler.getWorkbook(fazlaMesaiDosya);
			if (wb != null) {
				Sheet sheet = wb.getSheetAt(0);
				int COL_SICIL_NO = 0;
				int COL_YIL = 2;
				int COL_AY = 3;
				int COL_NORMAL_MESAI = 4;
				int COL_AKSAM_GUN = 5;
				int COL_AKSAM_SAAT = 6;
				int COL_RESMI_MESAI = 7;
				int COL_HAFTA_MESAI = 8;
				int colAdet = resmiTatilDurum ? COL_RESMI_MESAI : COL_AKSAM_SAAT;
				if (haftaTatilDurum)
					colAdet++;

				String perSicilNo = null;
				List<String> siciller = new ArrayList<String>();
				TreeMap<String, HashMap<Integer, org.apache.poi.ss.usermodel.Cell>> hucreMap = new TreeMap<String, HashMap<Integer, org.apache.poi.ss.usermodel.Cell>>();
				List<String> ypList = authenticatedUser.getYetkiliPersonelNoList();
				for (int row = 1; row <= sheet.getLastRowNum(); row++) {
					try {
						String key = null;
						try {
							perSicilNo = getSheetStringValue(sheet, row, COL_SICIL_NO);
							if (!ypList.contains(perSicilNo))
								continue;
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
						for (Integer col = 0; col <= colAdet; col++) {
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
					sb.append(" WHERE  " + Personel.COLUMN_NAME_PDKS_SICIL_NO + " :pId  ");
					sb.append(" AND V." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=:basTarih ");
					sb.append(" AND V." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + "<=:bitTarih ");
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
						sb.append(" WHERE " + PersonelDenklestirme.COLUMN_NAME_DONEM + "= " + denklestirmeAy.getId() + "  AND " + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " :pId  ");
						fields.put(PdksEntityController.MAP_KEY_MAP, "getSicilNo");
						// fields.put("denklestirmeAy", denklestirmeAy.getId());
						fields.put("pId", personelIdler);
						TreeMap<String, PersonelDenklestirme> bakiyeMap = ortakIslemler.getDataByIdMap(sb, fields, PersonelDenklestirme.TABLE_NAME, PersonelDenklestirme.class);
						List<HashMap<Integer, org.apache.poi.ss.usermodel.Cell>> hucreler = new ArrayList<HashMap<Integer, org.apache.poi.ss.usermodel.Cell>>(hucreMap.values());
						for (Iterator iterator = hucreler.iterator(); iterator.hasNext();) {
							HashMap<Integer, org.apache.poi.ss.usermodel.Cell> veriMap = (HashMap<Integer, org.apache.poi.ss.usermodel.Cell>) iterator.next();
							try {
								perSicilNo = ExcelUtil.getSheetStringValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_SICIL_NO));
								int yilExcel = Integer.parseInt(ExcelUtil.getSheetStringValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_YIL)));
								int ayExcel = Integer.parseInt(ExcelUtil.getSheetStringValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_AY)));
								if (yilExcel == denklestirmeAy.getYil() && ayExcel == denklestirmeAy.getAy() && personelMap.containsKey(perSicilNo)) {
									Personel pdksPersonel = personelMap.get(perSicilNo);
									if (!bakiyeMap.containsKey(perSicilNo))
										continue;
									PersonelDenklestirme pdksPersonelDenklestirme = new PersonelDenklestirme();
									Double odenenSure = ExcelUtil.getSheetDoubleValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_NORMAL_MESAI));
									Double aksamVardiyaSayisi = ExcelUtil.getSheetDoubleValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_AKSAM_GUN));
									Double aksamVardiyaSaatSayisi = ExcelUtil.getSheetDoubleValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_AKSAM_SAAT));
									Double haftaCalismaSuresi = ExcelUtil.getSheetDoubleValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_HAFTA_MESAI));
									pdksPersonelDenklestirme.setHaftaCalismaSuresi(haftaCalismaSuresi);
									pdksPersonelDenklestirme.setDenklestirmeAy(denklestirmeAy);
									pdksPersonelDenklestirme.setPersonel(pdksPersonel);
									ortakIslemler.setCalismaModeliAy(pdksPersonelDenklestirme, session);
									pdksPersonelDenklestirme.setOdenenSure(odenenSure);
									pdksPersonelDenklestirme.setAksamVardiyaSayisi(aksamVardiyaSayisi);
									pdksPersonelDenklestirme.setAksamVardiyaSaatSayisi(aksamVardiyaSaatSayisi);
									if (resmiTatilDurum) {
										Double resmiTatilSure = ExcelUtil.getSheetDoubleValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_RESMI_MESAI));
										pdksPersonelDenklestirme.setResmiTatilSure(resmiTatilSure);

									}
									pdksPersonelDenklestirme.setPersonelDenklestirmeDB(bakiyeMap.get(perSicilNo));

									personelDenklestirmeler.add(pdksPersonelDenklestirme);
								}
							} catch (Exception e1) {
								logger.info(e1.getMessage());
							}

						}
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
		fazlaMesaiDosya.setDosyaIcerik(null);

		return "";
	}

	@Transactional
	public String fazlaMesaiDosyaYaz() {
		try {
			ERPController controller = ortakIslemler.getERPController();
			controller.setFazlaMesaiUcretRFC(personelDenklestirmeler, authenticatedUser, session);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			PdksUtil.addMessageError(e.getMessage());
		}

		Date guncellemeTarihi = new Date();
		for (PersonelDenklestirme personelDenklestirmeERP : personelDenklestirmeler) {
			if (personelDenklestirmeERP.isErpAktarildi() && personelDenklestirmeERP.getPersonelDenklestirmeDB() != null) {
				PersonelDenklestirme pdksPersonelDenklestirme = personelDenklestirmeERP.getPersonelDenklestirmeDB();
				if (pdksPersonelDenklestirme.getId() != null) {
					try {
						pdksPersonelDenklestirme.setOdenenSure(personelDenklestirmeERP.getOdenenSure());
						pdksPersonelDenklestirme.setAksamVardiyaSayisi(personelDenklestirmeERP.getAksamVardiyaSayisi());
						pdksPersonelDenklestirme.setAksamVardiyaSaatSayisi(personelDenklestirmeERP.getAksamVardiyaSaatSayisi());
						if (resmiTatilDurum)
							pdksPersonelDenklestirme.setResmiTatilSure(personelDenklestirmeERP.getResmiTatilSure());
						if (haftaTatilDurum)
							pdksPersonelDenklestirme.setHaftaCalismaSuresi(personelDenklestirmeERP.getHaftaCalismaSuresi());
						pdksPersonelDenklestirme.setGuncelleyenUser(authenticatedUser);
						pdksPersonelDenklestirme.setGuncellemeTarihi(guncellemeTarihi);
						pdksEntityController.saveOrUpdate(session, entityManager, pdksPersonelDenklestirme);
						session.flush();
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}

				}

			}

		}
		return "";
	}

	public void listenerFazlaMesaiDosya(UploadEvent event) throws Exception {
		UploadItem item = event.getUploadItem();
		PdksUtil.getDosya(item, fazlaMesaiDosya);
		if (personelDenklestirmeler == null)
			personelDenklestirmeler = new ArrayList<PersonelDenklestirme>();
		else
			personelDenklestirmeler.clear();

	}

	public String fazlaMesaiDosyaSifirla() throws Exception {
		if (personelDenklestirmeler == null)
			personelDenklestirmeler = new ArrayList<PersonelDenklestirme>();
		else
			personelDenklestirmeler.clear();
		fazlaMesaiDosya.setDosyaIcerik(null);
		return "";
	}

	public void filDepartmanList() {
		List<SelectItem> departmanListe = new ArrayList<SelectItem>();
		List<Departman> list = ortakIslemler.fillDepartmanTanimList(session);
		if (list.size() == 1) {
			departmanId = list.get(0).getId();
			fillSirketList();

		}

		for (Departman pdksDepartman : list)
			departmanListe.add(new SelectItem(pdksDepartman.getId(), pdksDepartman.getDepartmanTanim().getAciklama()));

		setDepartmanList(departmanListe);
	}

	public void fillSirketList() {
		List<Sirket> list = null;
		HashMap parametreMap = new HashMap();
		parametreMap.put("id", departmanId);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		if (departmanId != null)
			departman = (Departman) pdksEntityController.getObjectByInnerObject(parametreMap, Departman.class);
		else
			departman = null;
		if (authenticatedUser.isAdmin() || authenticatedUser.isIK()) {
			HashMap map = new HashMap();
			map.put(PdksEntityController.MAP_KEY_MAP, "getId");
			map.put(PdksEntityController.MAP_KEY_SELECT, "sirket");
			map.put("sirket.durum", Boolean.TRUE);
			map.put("sirket.fazlaMesai", Boolean.TRUE);
			if (!authenticatedUser.isAdmin() && !authenticatedUser.isIKAdmin())
				map.put("sirket.departman", authenticatedUser.getDepartman());
			else if (departman != null)
				map.put("sirket.departman.id", departman.getId());
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			TreeMap sirketMap = pdksEntityController.getObjectByInnerObjectMap(map, Personel.class, Boolean.FALSE);
			setSirketler(null);
			if (!sirketMap.isEmpty())
				list = new ArrayList<Sirket>(sirketMap.values());
			if (list.size() > 1)
				list = PdksUtil.sortObjectStringAlanList(list, "getAd", null);
			if (!list.isEmpty()) {
				List<SelectItem> sirketler = new ArrayList<SelectItem>();
				for (Sirket sirket : list) {
					sirketler.add(new SelectItem(sirket.getId(), sirket.getAd()));
				}
				setSirketler(sirketler);
				if (list.size() == 1) {
					setSirket(list.get(0));
					sirketId = list.get(0).getId();
					list.clear();

				}

			}

			sirketMap = null;
		} else {
			setSirket(authenticatedUser.getPdksPersonel().getSirket());
		}

		setPersonelDenklestirmeList(new ArrayList<PersonelDenklestirme>());

	}

	public String islemSec() {
		for (Iterator iterator = personelDenklestirmeList.iterator(); iterator.hasNext();) {
			PersonelDenklestirme denklestirme = (PersonelDenklestirme) iterator.next();
			if (erpAktarimDurum.equals(Boolean.FALSE) || (denklestirme.getPersonel().getSirket().isErp() && !denklestirme.isErpAktarildi()))
				denklestirme.setCheckBoxDurum(secimDurum);
		}
		return "";

	}

	@Transactional
	public String erpAktarSil(PersonelDenklestirme pdksPersonelDenklestirme) {
		pdksPersonelDenklestirme.setErpAktarildi(Boolean.FALSE);
		pdksEntityController.saveOrUpdate(session, entityManager, pdksPersonelDenklestirme);
		session.flush();
		PdksUtil.addMessageInfo(pdksPersonelDenklestirme.getPersonel().getAdSoyad() + " ait fazla mesai blokesi açılmıştır.");
		return "";

	}

	@Transactional
	public String erpAktar() {
		List<PersonelDenklestirme> erpMesaiList = new ArrayList<PersonelDenklestirme>();
		for (PersonelDenklestirme denklestirme : personelDenklestirmeList) {
			if (denklestirme.isCheckBoxDurum())
				erpMesaiList.add(denklestirme);
		}
		if (!erpMesaiList.isEmpty()) {
			try {
				ERPController controller = ortakIslemler.getERPController();
				controller.setFazlaMesaiUcretRFC(erpMesaiList, authenticatedUser, session);
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				PdksUtil.addMessageError(e.getMessage());
			}
		} else
			PdksUtil.addMessageError("ERP'ye aktarılacak kayıt seçiniz!");

		erpMesaiList = null;
		return "";

	}

	public String denklestirmeDosyaAktar() {
		List<Long> erpMesaiList = new ArrayList<Long>();
		for (PersonelDenklestirme denklestirme : personelDenklestirmeList) {
			if (denklestirme.isCheckBoxDurum())
				erpMesaiList.add(denklestirme.getId());
		}
		if (!erpMesaiList.isEmpty()) {
			PdksSoapVeriAktar service = ortakIslemler.getPdksSoapVeriAktar();
			try {
				GetFazlaMesaiListResponse.Return rtn = service.getFazlaMesaiList(erpMesaiList);
				if (rtn != null && rtn.getEntry().size() == 3) {
					TreeMap<String, Object> map = new TreeMap<String, Object>();
					List<GetFazlaMesaiListResponse.Return.Entry> list = rtn.getEntry();
					for (GetFazlaMesaiListResponse.Return.Entry entry : list)
						map.put(entry.getKey(), entry.getValue());
					if (map.containsKey("content") && map.containsKey("contentType") && map.containsKey("dosyaAdi")) {
						String dosyaAdi = (String) map.get("dosyaAdi");
						String content = (String) map.get("content");
						String contentType = (String) map.get("contentType");
						HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
						ServletOutputStream sos = response.getOutputStream();
						response.setContentType(contentType + "; charset=UTF-8");
						response.setCharacterEncoding("UTF-8");
						response.setHeader("Expires", "0");
						response.setHeader("Pragma", "cache");
						response.setHeader("Cache-Control", "cache");
						response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(dosyaAdi, "UTF-8"));
						if (content != null) {
							response.setContentLength(content.length());
							byte[] bytes = content.getBytes();
							sos.write(bytes, 0, bytes.length);
							sos.flush();
							sos.close();
							FacesContext.getCurrentInstance().responseComplete();
						}

					}

				}
			} catch (Exception e) {

			}

		}
		PdksUtil.addMessageError("ERP'ye aktarılacak kayıt seçiniz!");
		return "";

	}

	public String denklestirmeExcelAktar() {
		try {
			ByteArrayOutputStream baosDosya = null;
			String dosyaAdi = null;
			if (onaylanmayanDurum == null || !onaylanmayanDurum.booleanValue()) {
				dosyaAdi = "fazlaMesai";
				baosDosya = denklestirmeExcelAktarDevam();
			}

			else {
				dosyaAdi = "fazlaMesaiOnaysiz";
				baosDosya = denklestirmeOnaylamayanExcelAktarDevam();
			}
			if (sirket != null)
				dosyaAdi += "_" + sirket.getAd() + "_";
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi + PdksUtil.convertToDateString(basGun, "_MMMMM_yyyy") + ".xlsx");

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

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
	 * @return
	 */
	private ByteArrayOutputStream denklestirmeOnaylamayanExcelAktarDevam() {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, PdksUtil.setTurkishStr(PdksUtil.convertToDateString(basGun, " MMMMM yyyy")) + " Liste", Boolean.TRUE);
		CellStyle style = ExcelUtil.getStyleData(wb);
		CellStyle styleCenter = ExcelUtil.getStyleData(wb);
		styleCenter.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		CellStyle stytleNumeric = ExcelUtil.getStyleData(wb);
		stytleNumeric.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		boolean bordroAltAlani = false;
		for (PersonelDenklestirme mesai : onaysizPersonelDenklestirmeList) {
			Personel personel = mesai.getPersonel();
			if (!bordroAltAlani)
				bordroAltAlani = personel.getBordroAltAlan() != null;
		}
		int row = 0, col = 0;

		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Personel");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.yoneticiAciklama());
		if (modelGoster)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Çalışma Modeli");
		if (bordroAltAlani)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Bordro Alt Birimi");
		if (!sanalPersonelAciklama.equals(""))
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(sanalPersonelAciklama);
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Durum");

		col = 0;
		double katsayi = 3.43;
		sheet.setColumnWidth(col++, (short) (1000 * katsayi));
		sheet.setColumnWidth(col++, (short) (2500 * katsayi));
		sheet.setColumnWidth(col++, (short) (2500 * katsayi));
		sheet.setColumnWidth(col++, (short) (2500 * katsayi));
		sheet.setColumnWidth(col++, (short) (2500 * katsayi));
		if (personelERP)
			sheet.setColumnWidth(col++, (short) (2500 * katsayi));
		sheet.setColumnWidth(col++, (short) (2500 * katsayi));

		col = 0;
		for (PersonelDenklestirme mesai : onaysizPersonelDenklestirmeList) {
			row++;
			col = 0;
			Personel personel = mesai.getPersonel();
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getPdksSicilNo());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getPdksYonetici() != null ? personel.getPdksYonetici().getAdSoyad() : "");
			if (modelGoster) {
				String modelAciklama = "";
				if (mesai.getCalismaModeliAy() != null) {
					CalismaModeliAy calismaModeliAy = mesai.getCalismaModeliAy();
					if (calismaModeliAy.getCalismaModeli() != null)
						modelAciklama = calismaModeliAy.getCalismaModeli().getAciklama();
				}
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(modelAciklama);
			}
			if (bordroAltAlani)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getBordroAltAlan() != null ? personel.getBordroAltAlan().getAciklama() : "");
			if (!sanalPersonelAciklama.equals(""))
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(authenticatedUser.getYesNo(personel.getSanalPersonel()));
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(mesai.getId() != null ? "Puantaj Onaylanmadı" : "Plan Oluşturulmadı");
			if (izinTipiVardiyaList != null) {
				for (Vardiya vardiya : izinTipiVardiyaList) {
					Integer adet = getVardiyaAdet(personel, vardiya);
					setCell(sheet, row, col++, style, adet);

				}
			}
		}

		try {
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
	private ByteArrayOutputStream denklestirmeExcelAktarDevam() {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, PdksUtil.setTurkishStr(PdksUtil.convertToDateString(basGun, " MMMMM yyyy")) + " Liste", Boolean.TRUE);
		CellStyle style = ExcelUtil.getStyleData(wb);
		CellStyle styleCenter = ExcelUtil.getStyleData(wb);
		styleCenter.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		CellStyle stytleNumeric = ExcelUtil.getStyleData(wb);
		stytleNumeric.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle tutarStyle = ExcelUtil.getCellStyleTutar(wb);
		int row = 0, col = 0;

		boolean bordroAltAlani = false;

		for (PersonelDenklestirme mesai : personelDenklestirmeList) {
			Personel personel = mesai.getPersonel();
			if (!bordroAltAlani)
				bordroAltAlani = personel.getBordroAltAlan() != null;
		}

		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Personel");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.yoneticiAciklama());
		if (modelGoster)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Çalışma Modeli");
		if (bordroAltAlani)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Bordro Alt Birimi");
		if (!sanalPersonelAciklama.equals(""))
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(sanalPersonelAciklama);
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Normal Mesai");
		if (haftaCalisma)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Hafta Tatil Mesai");
		if (resmiTatilDurum)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Resmi Tatil Mesai");
		if (aksamGun) {
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Akşam Gün Sayısı");
		}
		if (aksamSaat) {
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Akşam Saat Toplamı");
		}
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("ERP Aktarım");
		if (izinTipiVardiyaList != null) {
			for (Vardiya vardiya : izinTipiVardiyaList) {
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(vardiya.getAdi());

			}
		}

		col = 0;
		double katsayi = 3.43;
		sheet.setColumnWidth(col++, (short) (1000 * katsayi));
		sheet.setColumnWidth(col++, (short) (2500 * katsayi));
		sheet.setColumnWidth(col++, (short) (2500 * katsayi));
		sheet.setColumnWidth(col++, (short) (2500 * katsayi));
		sheet.setColumnWidth(col++, (short) (2500 * katsayi));
		if (bordroAltAlani)
			sheet.setColumnWidth(col++, (short) (2500 * katsayi));
		sheet.setColumnWidth(col++, (short) (1000 * katsayi));
		if (haftaCalisma)
			sheet.setColumnWidth(col++, (short) (1000 * katsayi));
		if (resmiTatilDurum)
			sheet.setColumnWidth(col++, (short) (1000 * katsayi));
		if (aksamGun) {
			sheet.setColumnWidth(col++, (short) (1000 * katsayi));
		}
		if (aksamSaat) {
			sheet.setColumnWidth(col++, (short) (1000 * katsayi));
		}
		sheet.setColumnWidth(col++, (short) (1000 * katsayi));
		col = 0;
		for (PersonelDenklestirme mesai : personelDenklestirmeList) {
			Personel personel = mesai.getPersonel();
			row++;
			col = 0;
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getPdksSicilNo());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getPdksYonetici() != null ? personel.getPdksYonetici().getAdSoyad() : "");
			if (modelGoster) {
				String modelAciklama = "";
				if (mesai.getCalismaModeliAy() != null) {
					CalismaModeliAy calismaModeliAy = mesai.getCalismaModeliAy();
					if (calismaModeliAy.getCalismaModeli() != null)
						modelAciklama = calismaModeliAy.getCalismaModeli().getAciklama();
				}
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(modelAciklama);
			}
			if (personelERP)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getBordroAltAlan() != null ? personel.getBordroAltAlan().getAciklama() : "");
			if (!sanalPersonelAciklama.equals(""))
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(authenticatedUser.getYesNo(personel.getSanalPersonel()));
			if (mesai.getOdenecekSure() != null && mesai.getOdenecekSure().doubleValue() > 0)
				ExcelUtil.getCell(sheet, row, col++, tutarStyle).setCellValue(User.getYuvarla(mesai.getOdenecekSure()));
			else
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
			if (haftaCalisma)
				ExcelUtil.getCell(sheet, row, col++, tutarStyle).setCellValue(User.getYuvarla(mesai.getHaftaCalismaSuresi()));
			if (resmiTatilDurum)
				ExcelUtil.getCell(sheet, row, col++, tutarStyle).setCellValue(User.getYuvarla(mesai.getResmiTatilSure()));
			if (aksamGun) {
				if (mesai.getAksamVardiyaSayisi() != null && mesai.getAksamVardiyaSayisi().doubleValue() > 0)
					ExcelUtil.getCell(sheet, row, col++, stytleNumeric).setCellValue(mesai.getAksamVardiyaSayisi().longValue());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");

			}
			if (aksamSaat) {
				if (mesai.getAksamVardiyaSayisi() != null && mesai.getAksamVardiyaSaatSayisi().doubleValue() > 0)
					ExcelUtil.getCell(sheet, row, col++, stytleNumeric).setCellValue(mesai.getAksamVardiyaSaatSayisi().longValue());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
			}
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(mesai.isErpAktarildi() ? "Evet" : "Hayır");
			if (izinTipiVardiyaList != null) {
				for (Vardiya vardiya : izinTipiVardiyaList) {
					Integer adet = getVardiyaAdet(personel, vardiya);
					setCell(sheet, row, col++, stytleNumeric, adet);

				}
			}
		}

		try {
			for (int i = 0; i < col; i++)
				sheet.autoSizeColumn(i);
			LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
			veriMap.put("sirketId", sirketId != null ? sirketId : 0L);
			veriMap.put("yil", getYil());
			veriMap.put("ay", getAy());
			StringBuffer sb = new StringBuffer("SP_GET_FAZLA_MESAI");
			if (session != null)
				veriMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelMesai> aylikFazlaMesaiTalepler = null;
			try {
				aylikFazlaMesaiTalepler = pdksEntityController.execSPList(veriMap, sb, PersonelMesai.class);
			} catch (Exception e1) {
			}

			if (aylikFazlaMesaiTalepler != null && !aylikFazlaMesaiTalepler.isEmpty()) {
				tesisVar = Boolean.FALSE;
				bolumVar = Boolean.FALSE;
				bordroAltAlanVar = Boolean.FALSE;
				for (Iterator iterator = aylikFazlaMesaiTalepler.iterator(); iterator.hasNext();) {
					PersonelMesai personelMesai = (PersonelMesai) iterator.next();
					Personel personel = personelMesai.getPersonel();
					if (sirketId != null && !personel.getSirket().getId().equals(sirketId)) {
						iterator.remove();
						continue;
					}
					if (!tesisVar)
						tesisVar = personel.getTesis() != null;
					if (!bolumVar)
						bolumVar = personel.getEkSaha3() != null;
					if (!bordroAltAlanVar)
						bordroAltAlanVar = personel.getBordroAltAlan() != null;
				}
				Sheet sheetERP = ExcelUtil.createSheet(wb, "ERP Liste", Boolean.TRUE);
				row = 0;
				col = 0;
				ExcelUtil.getCell(sheetERP, row, col++, header).setCellValue("Sıra No");
				ExcelUtil.getCell(sheetERP, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
				ExcelUtil.getCell(sheetERP, row, col++, header).setCellValue("Personel");
				ExcelUtil.getCell(sheetERP, row, col++, header).setCellValue("" + ortakIslemler.sirketAciklama() + " Kodu");
				ExcelUtil.getCell(sheetERP, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
				if (tesisVar) {
					ExcelUtil.getCell(sheetERP, row, col++, header).setCellValue(ortakIslemler.tesisAciklama() + " Kodu");
					ExcelUtil.getCell(sheetERP, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
				}
				if (bolumVar) {
					ExcelUtil.getCell(sheetERP, row, col++, header).setCellValue("Bölüm Kodu");
					ExcelUtil.getCell(sheetERP, row, col++, header).setCellValue(bolumAciklama);
				}
				if (bordroAltAlanVar) {
					ExcelUtil.getCell(sheetERP, row, col++, header).setCellValue("Bordro Alt Alan Kodu");
					ExcelUtil.getCell(sheetERP, row, col++, header).setCellValue("Bordro Alt Alan");
				}
				ExcelUtil.getCell(sheetERP, row, col++, header).setCellValue("Mesai Kodu");
				ExcelUtil.getCell(sheetERP, row, col++, header).setCellValue("Mesai");
				ExcelUtil.getCell(sheetERP, row, col++, header).setCellValue("Süre");
				List<Tanim> tanimList = ortakIslemler.getTanimList(Tanim.TIPI_ERP_FAZLA_MESAI, session);
				TreeMap<String, String> mesaiMap = new TreeMap<String, String>();
				for (Tanim tanim : tanimList) {
					mesaiMap.put(tanim.getErpKodu(), tanim.getAciklama());
				}
				Long perMesaiId = -1L;
				for (PersonelMesai personelMesai : aylikFazlaMesaiTalepler) {
					Personel personel = personelMesai.getPersonel();
					if (!personel.getId().equals(perMesaiId)) {
						if (perMesaiId.longValue() > 0L) {
							if (izinTipiPersonelMap.containsKey(perMesaiId))
								row = izinleriYaz(izinTipiPersonelMap.get(perMesaiId), row, sheetERP, styleCenter, style, tutarStyle);
						}
						perMesaiId = personel.getId();
					}
					col = 0;
					row++;
					ExcelUtil.getCell(sheetERP, row, col++, styleCenter).setCellValue(row);
					ExcelUtil.getCell(sheetERP, row, col++, styleCenter).setCellValue(personel.getPdksSicilNo());
					ExcelUtil.getCell(sheetERP, row, col++, style).setCellValue(personel.getAdSoyad());
					ExcelUtil.getCell(sheetERP, row, col++, styleCenter).setCellValue(personel.getSirket() != null ? personel.getSirket().getErpKodu() : "");
					ExcelUtil.getCell(sheetERP, row, col++, style).setCellValue(personel.getSirket() != null ? personel.getSirket().getAd() : "");
					if (tesisVar) {
						ExcelUtil.getCell(sheetERP, row, col++, styleCenter).setCellValue(personel.getTesis() != null ? personel.getTesis().getErpKodu() : "");
						ExcelUtil.getCell(sheetERP, row, col++, style).setCellValue(personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
					}
					if (bolumVar) {
						ExcelUtil.getCell(sheetERP, row, col++, styleCenter).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getErpKodu() : "");
						ExcelUtil.getCell(sheetERP, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");

					}
					if (bordroAltAlanVar) {
						ExcelUtil.getCell(sheetERP, row, col++, styleCenter).setCellValue(personel.getBordroAltAlan() != null ? personel.getBordroAltAlan().getErpKodu() : "");
						ExcelUtil.getCell(sheetERP, row, col++, style).setCellValue(personel.getBordroAltAlan() != null ? personel.getBordroAltAlan().getAciklama() : "");
					}
					String aciklama = "";
					if (personelMesai.getErpKodu() != null) {
						String erpKodu = personelMesai.getErpKodu();
						ExcelUtil.getCell(sheetERP, row, col++, styleCenter).setCellValue(erpKodu);
						if (mesaiMap.containsKey(erpKodu))
							aciklama = mesaiMap.get(erpKodu);
						else if (erpKodu.equals("UO"))
							aciklama = "Normal Mesai";
						else if (erpKodu.equals("RT"))
							aciklama = "Resmi Tatil Mesai";
						else if (erpKodu.equals("HT"))
							aciklama = "Hafta Tatil Mesai";
						else if (erpKodu.equals("AS"))
							aciklama = "Akşam Mesai";
						else if (erpKodu.equals("A"))
							aciklama = "Akşam Gün Mesai";

					} else
						ExcelUtil.getCell(sheetERP, row, col++, styleCenter).setCellValue("");

					ExcelUtil.getCell(sheetERP, row, col++, style).setCellValue(aciklama);
					ExcelUtil.getCell(sheetERP, row, col++, tutarStyle).setCellValue(User.getYuvarla(personelMesai.getSure()));
				}
				if (perMesaiId.longValue() > 0L) {
					if (izinTipiPersonelMap.containsKey(perMesaiId))
						row = izinleriYaz(izinTipiPersonelMap.get(perMesaiId), row, sheetERP, styleCenter, style, tutarStyle);
				}

				for (int i = 0; i < col; i++)
					sheetERP.autoSizeColumn(i);
			}
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

	/**
	 * @param personel
	 * @param row
	 * @param sheetERP
	 * @param styleCenter
	 * @param style
	 * @param tutarStyle
	 * @return
	 */
	private int izinleriYaz(Personel personel, int row, Sheet sheetERP, CellStyle styleCenter, CellStyle style, CellStyle tutarStyle) {
		for (Vardiya vardiya : izinTipiVardiyaList) {
			Integer adet = getVardiyaAdet(personel, vardiya);
			if (adet != null) {
				int col = 0;
				row++;
				ExcelUtil.getCell(sheetERP, row, col++, styleCenter).setCellValue(row);
				ExcelUtil.getCell(sheetERP, row, col++, styleCenter).setCellValue(personel.getPdksSicilNo());
				ExcelUtil.getCell(sheetERP, row, col++, style).setCellValue(personel.getAdSoyad());
				ExcelUtil.getCell(sheetERP, row, col++, styleCenter).setCellValue(personel.getSirket() != null ? personel.getSirket().getErpKodu() : "");
				ExcelUtil.getCell(sheetERP, row, col++, style).setCellValue(personel.getSirket() != null ? personel.getSirket().getAd() : "");
				if (tesisVar) {
					ExcelUtil.getCell(sheetERP, row, col++, styleCenter).setCellValue(personel.getTesis() != null ? personel.getTesis().getErpKodu() : "");
					ExcelUtil.getCell(sheetERP, row, col++, style).setCellValue(personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
				}
				if (bolumVar) {
					ExcelUtil.getCell(sheetERP, row, col++, styleCenter).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getErpKodu() : "");
					ExcelUtil.getCell(sheetERP, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");

				}
				if (bordroAltAlanVar) {
					ExcelUtil.getCell(sheetERP, row, col++, styleCenter).setCellValue(personel.getBordroAltAlan() != null ? personel.getBordroAltAlan().getErpKodu() : "");
					ExcelUtil.getCell(sheetERP, row, col++, style).setCellValue(personel.getBordroAltAlan() != null ? personel.getBordroAltAlan().getAciklama() : "");
				}
				String aciklama = vardiya.getAdi();
				String erpKodu = vardiya.getKisaAdi();
				ExcelUtil.getCell(sheetERP, row, col++, styleCenter).setCellValue(erpKodu);
				ExcelUtil.getCell(sheetERP, row, col++, style).setCellValue(aciklama);
				ExcelUtil.getCell(sheetERP, row, col++, tutarStyle).setCellValue(adet);

			}

		}
		return row;
	}

	public String fillPersonelDenklestirmeList() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.clear();
		aksamGun = Boolean.FALSE;
		aksamSaat = Boolean.FALSE;
		haftaCalisma = Boolean.FALSE;
		HashMap fields = new HashMap();
		fields.put("ay", ay);
		fields.put("yil", yil);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		personelDenklestirmeList.clear();
		DenklestirmeAy denklestirmeAy = (DenklestirmeAy) pdksEntityController.getObjectByInnerObject(fields, DenklestirmeAy.class);
		basGun = null;
		bitGun = null;
		denklestirmeAyDurum = denklestirmeAy != null && denklestirmeAy.getDurum();
		setOnaysizPersonelDenklestirmeList(null);
		durumERP = Boolean.FALSE;
		onaylanmayanDurum = null;
		personelERP = Boolean.FALSE;
		sirket = null;
		personelDenklestirmeler = null;
		modelGoster = ortakIslemler.getModelGoster(denklestirmeAy, session);
		if (denklestirmeAy != null) {
			basGun = PdksUtil.getYilAyBirinciGun(yil, ay);
			bitGun = PdksUtil.tariheAyEkleCikar(basGun, 1);

			fields.clear();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT  V." + PersonelDenklestirme.COLUMN_NAME_ID + " FROM " + PersonelDenklestirme.TABLE_NAME + " V WITH(nolock) ");
			Boolean ikSirket = departman == null || departman.isAdminMi();
			if (sirketId != null || (sicilNo != null && sicilNo.length() > 0) || sanalPersonelDurum != 0) {
				sb.append(" INNER JOIN  " + Personel.TABLE_NAME + " P ON  P." + Personel.COLUMN_NAME_ID + "=V." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
				sb.append(" AND  P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + "<:bitGun AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=:basGun ");
				fields.put("basGun", basGun);
				fields.put("bitGun", bitGun);
				if (sirketId != null) {
					HashMap parametreMap = new HashMap();

					parametreMap.put("id", sirketId);
					sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + "= " + sirketId);
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					sirket = (Sirket) pdksEntityController.getObjectByInnerObject(parametreMap, Sirket.class);
				}
				if (sicilNo != null && sicilNo.length() > 0) {
					sb.append(" AND P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + "=:sicilNo ");
					fields.put("sicilNo", sicilNo);
				}
				if (sanalPersonelDurum != 0)
					sb.append(" AND P." + Personel.COLUMN_NAME_SANAL_PERSONEL + "=1 ");
			}
			sb.append(" WHERE v." + PersonelDenklestirme.COLUMN_NAME_DONEM + "=:denklestirmeAy AND V." + PersonelDenklestirme.COLUMN_NAME_DURUM + "=1  ");
			sb.append(" AND V." + PersonelDenklestirme.COLUMN_NAME_ONAYLANDI + "=1  AND V." + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + "=1");
			sb.append(" AND (V." + PersonelDenklestirme.COLUMN_NAME_ODENEN_SURE + ">0  OR V." + PersonelDenklestirme.COLUMN_NAME_HAFTA_TATIL_SURE + ">0 OR V." + PersonelDenklestirme.COLUMN_NAME_RESMI_TATIL_SURE + ">0");
			sb.append("   OR V." + PersonelDenklestirme.COLUMN_NAME_AKSAM_VARDIYA_SAAT + ">0 OR V." + PersonelDenklestirme.COLUMN_NAME_AKSAM_VARDIYA_GUN_ADET + ">0 )");

			fields.put("denklestirmeAy", denklestirmeAy.getId());
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			personelDenklestirmeList = ortakIslemler.getDataByIdList(sb, fields, PersonelDenklestirme.TABLE_NAME, PersonelDenklestirme.class);
			// if (!ikSirket)
			// personelDenklestirmeList = (ArrayList<pdksPersonelDenklestirme>) pdksUtil.sortObjectStringAlanList(personelDenklestirmeList, "getKontratliSortKey", null);

			sb = null;
			if (denklestirmeAyDurum) {
				fields.clear();
				sb = new StringBuffer();
				sb.append("SELECT  V." + PersonelDenklestirmeOnaylanmayan.COLUMN_NAME_ID + " FROM " + PersonelDenklestirmeOnaylanmayan.TABLE_NAME + " V WITH(nolock) ");
				if (sirketId != null) {
					sb.append(" INNER JOIN  " + Personel.TABLE_NAME + " P ON  P." + Personel.COLUMN_NAME_ID + "=V." + PersonelDenklestirmeOnaylanmayan.COLUMN_NAME_PERSONEL_ID);
					sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + "= " + sirketId);
				}
				sb.append(" WHERE v." + PersonelDenklestirmeOnaylanmayan.COLUMN_NAME_DONEM + "=" + denklestirmeAy.getId());
				sb.append(" AND V." + PersonelDenklestirmeOnaylanmayan.COLUMN_NAME_PERSONEL_ID + " :p ");
				fields.put("p", authenticatedUser.getYetkiliPersonelIdler());
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<BigDecimal> list = pdksEntityController.getObjectBySQLList(sb, fields, null);
				if (!list.isEmpty())
					onaylanmayanDurum = Boolean.FALSE;
				list = null;
			}

			sureDurum = Boolean.FALSE;
			fazlaMesaiDurum = Boolean.FALSE;
			resmiTatilDurum = Boolean.FALSE;
			haftaTatilDurum = Boolean.FALSE;
			secimDurum = Boolean.FALSE;
			durumERP = Boolean.FALSE;
			personelERP = Boolean.FALSE;
			List<PersonelDenklestirme> erpAktarilanlar = new ArrayList<PersonelDenklestirme>();
			for (Iterator iterator = personelDenklestirmeList.iterator(); iterator.hasNext();) {
				PersonelDenklestirme denklestirme = (PersonelDenklestirme) iterator.next();
				if (!denklestirme.isOnaylandi() || !denklestirme.isDenklestirme())
					iterator.remove();
				else {
					if (denklestirme.getPersonel().getSirket().isErp()) {
						if (denklestirme.getPersonel().getBordroAltAlan() != null)
							personelERP = Boolean.TRUE;
						if (!durumERP)
							durumERP = !denklestirme.isErpAktarildi();
					}

					double resmiTatilSure = denklestirme.getResmiTatilSure();
					double odenecekSure = denklestirme.getOdenecekSure();
					double haftaTatilSure = denklestirme.getHaftaCalismaSuresi();
					if (!(haftaTatilSure > 0 || resmiTatilSure > 0 || odenecekSure > 0 || denklestirme.getAksamVardiyaSayisi() > 0 || denklestirme.getAksamVardiyaSaatSayisi() > 0))
						iterator.remove();
					else {
						denklestirme.setCheckBoxDurum(Boolean.FALSE);
						if (!sureDurum)
							sureDurum = odenecekSure > 0;
						if (!resmiTatilDurum)
							resmiTatilDurum = resmiTatilSure > 0;
						if (!haftaTatilDurum)
							haftaTatilDurum = haftaTatilSure > 0;
					}

					if (!haftaCalisma)
						haftaCalisma = denklestirme.getHaftaCalismaSuresi() != null && denklestirme.getHaftaCalismaSuresi().doubleValue() > 0.0d;

					if (!aksamGun)
						aksamGun = denklestirme.getAksamVardiyaSayisi() != null && denklestirme.getAksamVardiyaSayisi().doubleValue() > 0.0d;

					if (!aksamSaat)
						aksamSaat = denklestirme.getAksamVardiyaSaatSayisi() != null && denklestirme.getAksamVardiyaSaatSayisi().doubleValue() > 0.0d;
					if (denklestirme.isErpAktarildi()) {
						erpAktarilanlar.add(denklestirme);
						iterator.remove();
					}
				}

			}
			if (!personelDenklestirmeList.isEmpty()) {
				if (!ikSirket)
					personelDenklestirmeList = (ArrayList<PersonelDenklestirme>) PdksUtil.sortObjectStringAlanList(personelDenklestirmeList, "getKontratliSortKey", null);
				else
					personelDenklestirmeList = PdksUtil.sortObjectStringAlanList(personelDenklestirmeList, "getSortKey", null);
				List<Long> perIdList = new ArrayList<Long>();
				Date basTarih = PdksUtil.convertToJavaDate(denklestirmeAy.getYil() + "-" + denklestirmeAy.getAy() + "-01", "yyyy-M-dd");
				Date bitTarih = PdksUtil.tariheAyEkleCikar(basTarih, 1);

				for (PersonelDenklestirme personelDenklestirme : personelDenklestirmeList) {
					perIdList.add(personelDenklestirme.getPersonelId());
				}

				fields.clear();
				sb = new StringBuffer();
				sb.append("select DISTINCT G.* from " + VardiyaGun.TABLE_NAME + " G WITH(nolock) ");
				sb.append(" INNER JOIN " + Vardiya.TABLE_NAME + " V ON V." + Vardiya.COLUMN_NAME_ID + "=G." + VardiyaGun.COLUMN_NAME_VARDIYA);
				sb.append("  AND   V." + Vardiya.COLUMN_NAME_VARDIYA_TIPI + " IN ('" + Vardiya.TIPI_IZIN + "','" + Vardiya.TIPI_HASTALIK_RAPOR + "') ");
				sb.append(" WHERE G." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ">=:t1 AND G." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + "<:t2");
				sb.append(" AND G." + VardiyaGun.COLUMN_NAME_PERSONEL + " :p ");
				fields.put("p", perIdList);
				fields.put("t1", basTarih);
				fields.put("t2", bitTarih);
				List<VardiyaGun> vgList = null;
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				try {
					vgList = pdksEntityController.getObjectBySQLList(sb, fields, VardiyaGun.class);

				} catch (Exception e) {
					logger.error(sb.toString());
				}
				TreeMap<String, Object> ozetMap = fazlaMesaiOrtakIslemler.getIzinOzetMap(vgList, null, false);
				izinTipiVardiyaList = ozetMap.containsKey("izinTipiVardiyaList") ? (List<Vardiya>) ozetMap.get("izinTipiVardiyaList") : new ArrayList<Vardiya>();
				izinTipiPersonelVardiyaMap = ozetMap.containsKey("izinTipiPersonelVardiyaMap") ? (TreeMap<String, TreeMap<String, List<VardiyaGun>>>) ozetMap.get("izinTipiPersonelVardiyaMap") : new TreeMap<String, TreeMap<String, List<VardiyaGun>>>();
				izinTipiPersonelMap = ozetMap.containsKey("izinTipiPersonelMap") ? (TreeMap<Long, Personel>) ozetMap.get("izinTipiPersonelMap") : new TreeMap<Long, Personel>();
				perIdList = null;
				vgList = null;
			}

			if (!erpAktarilanlar.isEmpty()) {
				if (!ikSirket)
					erpAktarilanlar = (ArrayList<PersonelDenklestirme>) PdksUtil.sortObjectStringAlanList(erpAktarilanlar, "getKontratliSortKey", null);
				else
					erpAktarilanlar = PdksUtil.sortObjectStringAlanList(erpAktarilanlar, "getSortKey", null);
				personelDenklestirmeList.addAll(erpAktarilanlar);
			}

			erpAktarilanlar = null;
		}

		if (personelDenklestirmeList.isEmpty())
			PdksUtil.addMessageWarn("İlgili döneme ait fazla mesai bulunamadı!");
		setInstance(denklestirmeAy);

		return "";
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
	 * @return
	 */
	public String onaylanmayanPersonelleriGetir() {
		if (onaysizPersonelDenklestirmeList == null && onaylanmayanDurum) {
			HashMap fields = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT  V.* FROM " + PersonelDenklestirmeOnaylanmayan.TABLE_NAME + " V WITH(nolock) ");
			if (sirketId != null || sanalPersonelDurum != 0) {
				sb.append(" INNER JOIN  " + Personel.TABLE_NAME + " P ON  P." + Personel.COLUMN_NAME_ID + "=V." + PersonelDenklestirmeOnaylanmayan.COLUMN_NAME_PERSONEL_ID);
				if (sirketId != null)
					sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + "= " + sirketId);
				if (sanalPersonelDurum != 0)
					sb.append(" AND P." + Personel.COLUMN_NAME_SANAL_PERSONEL + "= 1");
			}
			sb.append(" WHERE v." + PersonelDenklestirmeOnaylanmayan.COLUMN_NAME_YIL + "=" + yil + " AND v." + PersonelDenklestirmeOnaylanmayan.COLUMN_NAME_AY + "=" + ay);

			sb.append(" AND V." + PersonelDenklestirmeOnaylanmayan.COLUMN_NAME_PERSONEL_ID + " :p ");
			fields.put("p", authenticatedUser.getYetkiliPersonelIdler());
			sb.append(" ORDER BY V." + PersonelDenklestirmeOnaylanmayan.COLUMN_NAME_YONETICI + ", V." + PersonelDenklestirmeOnaylanmayan.COLUMN_NAME_BOLUM + "," + PersonelDenklestirmeOnaylanmayan.COLUMN_NAME_PDKS_SICIL_NO);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelDenklestirmeOnaylanmayan> list = pdksEntityController.getObjectBySQLList(sb, fields, PersonelDenklestirmeOnaylanmayan.class);

			if (!list.isEmpty()) {
				onaysizPersonelDenklestirmeList = new ArrayList<PersonelDenklestirme>();
				List<PersonelDenklestirme> plansizList = new ArrayList<PersonelDenklestirme>();
				for (PersonelDenklestirmeOnaylanmayan onaylanmayan : list) {
					PersonelDenklestirme pdksPersonelDenklestirme = onaylanmayan.getPersonelDenklestirmeAy();
					onaysizPersonelDenklestirmeList.add(pdksPersonelDenklestirme);
				}
				onaysizPersonelDenklestirmeList = PdksUtil.sortObjectStringAlanList(onaysizPersonelDenklestirmeList, "getSortKey", null);
				for (Iterator iterator = onaysizPersonelDenklestirmeList.iterator(); iterator.hasNext();) {
					PersonelDenklestirme pdksPersonelDenklestirme = (PersonelDenklestirme) iterator.next();
					if (pdksPersonelDenklestirme.getId() == null || pdksPersonelDenklestirme.getId() < 0) {
						plansizList.add(pdksPersonelDenklestirme);
						iterator.remove();
					}
				}

				if (!plansizList.isEmpty())
					onaysizPersonelDenklestirmeList.addAll(plansizList);
				plansizList = null;
			}
			list = null;
		}

		return "";
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

	public List<PersonelDenklestirme> getPersonelDenklestirmeList() {
		return personelDenklestirmeList;
	}

	public void setPersonelDenklestirmeList(List<PersonelDenklestirme> personelDenklestirmeList) {
		this.personelDenklestirmeList = personelDenklestirmeList;
	}

	public Boolean getSecimDurum() {
		return secimDurum;
	}

	public void setSecimDurum(Boolean secimDurum) {
		this.secimDurum = secimDurum;
	}

	public Boolean getSureDurum() {
		return sureDurum;
	}

	public void setSureDurum(Boolean sureDurum) {
		this.sureDurum = sureDurum;
	}

	public Boolean getFazlaMesaiDurum() {
		return fazlaMesaiDurum;
	}

	public void setFazlaMesaiDurum(Boolean fazlaMesaiDurum) {
		this.fazlaMesaiDurum = fazlaMesaiDurum;
	}

	public Boolean getResmiTatilDurum() {
		return resmiTatilDurum;
	}

	public void setResmiTatilDurum(Boolean resmiTatilDurum) {
		this.resmiTatilDurum = resmiTatilDurum;
	}

	public Date getBasGun() {
		return basGun;
	}

	public void setBasGun(Date basGun) {
		this.basGun = basGun;
	}

	public Date getBitGun() {
		return bitGun;
	}

	public void setBitGun(Date bitGun) {
		this.bitGun = bitGun;
	}

	public List<PersonelDenklestirme> getOnaysizPersonelDenklestirmeList() {
		return onaysizPersonelDenklestirmeList;
	}

	public void setOnaysizPersonelDenklestirmeList(List<PersonelDenklestirme> onaysizPersonelDenklestirmeList) {
		this.onaysizPersonelDenklestirmeList = onaysizPersonelDenklestirmeList;
	}

	public Boolean getOnaylanmayanDurum() {
		return onaylanmayanDurum;
	}

	public void setOnaylanmayanDurum(Boolean onaylanmayanDurum) {
		this.onaylanmayanDurum = onaylanmayanDurum;
	}

	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket sirket) {
		this.sirket = sirket;
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

	public List<SelectItem> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<SelectItem> departmanList) {
		this.departmanList = departmanList;
	}

	public Long getDepartmanId() {
		return departmanId;
	}

	public void setDepartmanId(Long departmanId) {
		this.departmanId = departmanId;
	}

	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman departman) {
		this.departman = departman;
	}

	public Dosya getFazlaMesaiDosya() {
		return fazlaMesaiDosya;
	}

	public void setFazlaMesaiDosya(Dosya fazlaMesaiDosya) {
		this.fazlaMesaiDosya = fazlaMesaiDosya;
	}

	public List<PersonelDenklestirme> getPersonelDenklestirmeler() {
		return personelDenklestirmeler;
	}

	public void setPersonelDenklestirmeler(List<PersonelDenklestirme> personelDenklestirmeler) {
		this.personelDenklestirmeler = personelDenklestirmeler;
	}

	public Boolean getAksamGun() {
		return aksamGun;
	}

	public void setAksamGun(Boolean aksamGun) {
		this.aksamGun = aksamGun;
	}

	public Boolean getAksamSaat() {
		return aksamSaat;
	}

	public void setAksamSaat(Boolean aksamSaat) {
		this.aksamSaat = aksamSaat;
	}

	public Boolean getHaftaCalisma() {
		return haftaCalisma;
	}

	public void setHaftaCalisma(Boolean haftaCalisma) {
		this.haftaCalisma = haftaCalisma;
	}

	public Boolean getPersonelERP() {
		return personelERP;
	}

	public void setPersonelERP(Boolean personelERP) {
		this.personelERP = personelERP;
	}

	public Boolean getDurumERP() {
		return durumERP;
	}

	public void setDurumERP(Boolean durumERP) {
		this.durumERP = durumERP;
	}

	public Boolean getErpAktarimDurum() {
		return erpAktarimDurum;
	}

	public void setErpAktarimDurum(Boolean erpAktarimDurum) {
		this.erpAktarimDurum = erpAktarimDurum;
	}

	public Boolean getHaftaTatilDurum() {
		return haftaTatilDurum;
	}

	public void setHaftaTatilDurum(Boolean haftaTatilDurum) {
		this.haftaTatilDurum = haftaTatilDurum;
	}

	public int getSanalPersonelDurum() {
		return sanalPersonelDurum;
	}

	public void setSanalPersonelDurum(int sanalPersonelDurum) {
		this.sanalPersonelDurum = sanalPersonelDurum;
	}

	public String getSanalPersonelAciklama() {
		return sanalPersonelAciklama;
	}

	public void setSanalPersonelAciklama(String sanalPersonelAciklama) {
		this.sanalPersonelAciklama = sanalPersonelAciklama;
	}

	public Boolean getModelGoster() {
		return modelGoster;
	}

	public void setModelGoster(Boolean modelGoster) {
		this.modelGoster = modelGoster;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
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

	public TreeMap<Long, Personel> getIzinTipiPersonelMap() {
		return izinTipiPersonelMap;
	}

	public void setIzinTipiPersonelMap(TreeMap<Long, Personel> izinTipiPersonelMap) {
		this.izinTipiPersonelMap = izinTipiPersonelMap;
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
}
