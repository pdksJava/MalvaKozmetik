package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.Dosya;
import org.pdks.entity.FileUpload;
import org.pdks.entity.KapiSirket;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Sirket;
import org.pdks.security.entity.User;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

@Name("taseronPersonelKopyalaHome")
public class TaseronPersonelKopyalaHome extends EntityHome<PersonelView> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6022588534200961289L;
	static Logger logger = Logger.getLogger(TaseronPersonelKopyalaHome.class);
	/**
	 * 
	 */

	@RequestParameter
	Long perId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(create = true)
	Renderer renderer;
	@In(required = false, create = true)
	HashMap parameterMap;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	private List<PersonelView> personelList = new ArrayList<PersonelView>();
	private List<SelectItem> sirketList = new ArrayList<SelectItem>();

	private Long sirketId;
	private Date basSirketTarih;
	private boolean sorunYok;
	private FileUpload file;
	private Session session;

	@Override
	public Object getId() {
		if (perId == null) {
			return super.getId();
		} else {
			return perId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public String tipDegisti() {
		personelList.clear();
		return "";
	}

	public void dosyaSifirla() {
		if (file == null)
			file.setData(null);
	}

	public void listener(UploadEvent event) throws Exception {
		UploadItem item = event.getUploadItem();
		if (file == null)
			file = new FileUpload();
		file.setName(item.getFileName());
		file.setLength(item.getData().length);
		file.setData(item.getData());
		personelList.clear();
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		if (sirketList == null)
			sirketList = new ArrayList<SelectItem>();
		else
			sirketList.clear();
		HashMap fields = new HashMap();
		fields.put("durum", Boolean.TRUE);
		fields.put("pdks", Boolean.TRUE);
		if (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin())
			fields.put("departman.admin", Boolean.FALSE);
		else
			fields.put("departman.id", authenticatedUser.getDepartman().getId());

		fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Sirket> list = pdksEntityController.getObjectByInnerObjectList(fields, Sirket.class);
		fields = null;
		if (list.size() > 1)
			list = PdksUtil.sortObjectStringAlanList(list, "getAd", null);
		for (Sirket sirket : list)
			sirketList.add(new SelectItem(sirket.getId(), sirket.getAd()));

		if (file != null)
			file.setData(null);
		personelList.clear();
		return ortakIslemler.yetkiIKAdmin(Boolean.FALSE);
	}

	public String taseronVeriOku() {
		session.clear();
		personelList.clear();
		Dosya dosya = ortakIslemler.getDosyaFromFileUpload(file);
		List<String> perNoList = new ArrayList<String>();
		sorunYok = false;
		try {
			Workbook wb = ortakIslemler.getWorkbook(dosya);
			int COL_SICIL_NO = 0, COL_KIMLIK_NO = 1, COL_YENISICIL_NO = 2;
			if (wb != null) {
				try {
					Sheet sheet = wb.getSheetAt(0);
					for (int row = 1; COL_SICIL_NO >= 0 && row <= sheet.getLastRowNum(); row++) {
						String perSicilNo = "";
						String yeniPerSicilNo = "";
						try {
							perSicilNo = ExcelUtil.getSheetStringValueTry(sheet, row, COL_SICIL_NO);
							logger.debug(row + " " + perSicilNo);
							if (!PdksUtil.hasStringValue(perSicilNo))
								break;

							if (perNoList.contains(perSicilNo))
								continue;

						} catch (Exception e) {

						}

						try {
							yeniPerSicilNo = ExcelUtil.getSheetStringValueTry(sheet, row, COL_YENISICIL_NO);
							if (!PdksUtil.hasStringValue(yeniPerSicilNo) || yeniPerSicilNo.equals(perSicilNo))
								continue;

							if (perNoList.contains(yeniPerSicilNo))
								continue;
							logger.debug(row + ". " + perSicilNo + " " + yeniPerSicilNo);
							perNoList.add(perSicilNo);
							perNoList.add(yeniPerSicilNo);
						} catch (Exception e) {

						}
						PersonelView personelView = new PersonelView();
						Personel personel = new Personel();
						personelView.setPdksPersonel(personel);
						personel.setPdksSicilNo(perSicilNo);
						PersonelKGS personelKGS = new PersonelKGS();
						personelView.setPdksPersonelAciklama(ExcelUtil.getSheetStringValueTry(sheet, row, COL_KIMLIK_NO));
						personel.setPersonelKGS(personelKGS);
						personelView.setKgsSicilNo(yeniPerSicilNo);
						personelList.add(personelView);
					}
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
				}
				if (!personelList.isEmpty()) {
					HashMap map = new HashMap();
					Date bugun = PdksUtil.getDate(new Date());
					map.put("id>", 0L);
					map.put("basTarih<=", PdksUtil.tariheGunEkleCikar(bugun, +7));
					map.put("bitTarih>=", PdksUtil.tariheGunEkleCikar(bugun, -7));
					if (session != null)
						map.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<KapiSirket> list = pdksEntityController.getObjectByInnerObjectListInLogic(map, KapiSirket.class);
					if (list.size() == 1) {
						Long kapiSirketId = list.get(0).getId();
						map.clear();
						map.put(PdksEntityController.MAP_KEY_MAP, "getPdksSicilNo");
						map.put("pdksSicilNo", perNoList);
						if (session != null)
							map.put(PdksEntityController.MAP_KEY_SESSION, session);
						TreeMap<String, Personel> personelMap = pdksEntityController.getObjectByInnerObjectMap(map, Personel.class, false);
						map.clear();
						map.put("sicilNo", perNoList);
						if (session != null)
							map.put(PdksEntityController.MAP_KEY_SESSION, session);
						List<PersonelKGS> personelKGSList = pdksEntityController.getObjectByInnerObjectList(map, PersonelKGS.class);
						TreeMap<String, PersonelKGS> personelKGSMap = new TreeMap<String, PersonelKGS>();
						for (Iterator iterator = personelKGSList.iterator(); iterator.hasNext();) {
							PersonelKGS personelKGS = (PersonelKGS) iterator.next();
							if (personelKGS.getKapiSirket() == null || personelKGS.getDurum().booleanValue() == false || !personelKGS.getKapiSirket().getId().equals(kapiSirketId))
								iterator.remove();
							else {
								perNoList.remove(personelKGS.getSicilNo());
								personelKGSMap.put(personelKGS.getSicilNo(), personelKGS);
							}

						}
						sorunYok = perNoList.isEmpty();
						StringBuffer sb = new StringBuffer();
						List<PersonelView> hataList = new ArrayList<PersonelView>();
						for (PersonelView personelView : personelList) {
							String perNo = personelView.getPdksPersonel().getPdksSicilNo();
							Personel pdksPersonel = null;
							if (perNo != null) {

								if (personelMap.containsKey(perNo)) {
									personelView.setPdksPersonel(personelMap.get(perNo));
								} else if (personelKGSMap.containsKey(perNo)) {
									pdksPersonel = new Personel();
									pdksPersonel.setPdksSicilNo(perNo);
									PersonelKGS personelKGS = personelKGSMap.get(perNo);
									pdksPersonel.setPersonelKGS(personelKGS);
									pdksPersonel.setAd(personelKGS.getAd());
									pdksPersonel.setSoyad(personelKGS.getSoyad());
									personelView.setPdksPersonel(pdksPersonel);
									sb.append((sb.length() > 0 ? ", " : "") + perNo);
									sorunYok = false;
								}
							}
							String sicilNo = personelView.getKgsSicilNo();
							PersonelKGS personelKGS = null;
							if (sicilNo != null && personelKGSMap.containsKey(sicilNo)) {
								personelKGS = personelKGSMap.get(sicilNo);
								personelView.setPersonelKGS(personelKGS);
								personelView.setId(personelKGS.getId());
							} else {
								sb.append((sb.length() > 0 ? ", " : "") + sicilNo);
								sorunYok = false;
							}
							if (pdksPersonel != null && personelKGS != null && !pdksPersonel.getAdSoyad().equals(personelKGS.getAdSoyad()))
								hataList.add(personelView);
						}
						if (!hataList.isEmpty()) {
							sorunYok = false;
							for (PersonelView personelView : hataList) {
								Personel pdksPersonel = personelView.getPdksPersonel();
								PersonelKGS personelKGS = personelView.getPersonelKGS();
								PdksUtil.addMessageAvailableWarn(pdksPersonel.getPdksSicilNo() + " " + pdksPersonel.getAdSoyad() + " ile " + personelKGS.getSicilNo() + " " + personelKGS.getAdSoyad());
							}
							PdksUtil.addMessageAvailableWarn("isimleri farklıdır.");
						}
						hataList = null;
						if (!sorunYok && !perNoList.isEmpty()) {
							for (Iterator iterator = perNoList.iterator(); iterator.hasNext();) {
								String sicilNo = (String) iterator.next();
								sb.append((sb.length() > 0 ? ", " : "") + sicilNo);
							}
						}
						if (sb.length() > 0)
							PdksUtil.addMessageAvailableWarn(sb.toString() + " " + ortakIslemler.personelNoAciklama() + " bilgileri bulunamadı!");
						sb = null;
					}

				}
			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		dosya = null;
		perNoList = null;
		return "";
	}

	@Transactional
	public String taseronKopyala() {
		List<Long> kgsIdList = new ArrayList<Long>();
		for (Iterator iterator = personelList.iterator(); iterator.hasNext();) {
			PersonelView personelView = (PersonelView) iterator.next();
			if (personelView.getPdksPersonel().getId() == null)
				iterator.remove();
			else {
				if (PdksUtil.getCanliSunucuDurum()) {
					if (!personelView.getPdksPersonel().getAdSoyad().equals(personelView.getPersonelKGS().getAdSoyad())) {
						iterator.remove();
						continue;
					}
				}
				if (personelView.getId() != null)
					kgsIdList.add(personelView.getId());
			}
		}
		if (!kgsIdList.isEmpty()) {
			HashMap map = new HashMap();

			map.put(PdksEntityController.MAP_KEY_MAP, "getPdksSicilNo");
			map.put("personelKGS.id", kgsIdList);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			TreeMap<String, Personel> personelMap = pdksEntityController.getObjectByInnerObjectMap(map, Personel.class, false);
			Sirket sirket = null;
			if (sirketId != null) {
				map.clear();
				map.put("id", sirketId);
				if (session != null)
					map.put(PdksEntityController.MAP_KEY_SESSION, session);
				sirket = (Sirket) pdksEntityController.getObjectByInnerObject(map, Sirket.class);
			}
			Date istenAyrilisTarihi = basSirketTarih != null ? PdksUtil.tariheGunEkleCikar(basSirketTarih, -1) : null, olusturmaTarihi = new Date();
			User olusturanUser = ortakIslemler.getSistemAdminUser(session);
			if (olusturanUser == null)
				olusturanUser = authenticatedUser;
			boolean flush = false;
			for (Iterator iterator = personelList.iterator(); iterator.hasNext();) {
				PersonelView personelView = (PersonelView) iterator.next();
				String pdksSicilNo = personelView.getKgsSicilNo();
				Personel pdksPersonel = personelView.getPdksPersonel(), personel = personelMap.containsKey(pdksSicilNo) ? personelMap.get(pdksSicilNo) : null;
				if (personel == null) {
					personel = (Personel) pdksPersonel.clone();
					personel.setId(null);
					PersonelKGS personelKGS = personelView.getPersonelKGS();
					personel.setPersonelKGS(personelKGS);
					personel.setAd(personelKGS.getAd());
					personel.setSoyad(personelKGS.getSoyad());
					personel.setPdksSicilNo(pdksSicilNo);
					personel.setOlusturanUser(olusturanUser);
					personel.setOlusturmaTarihi(olusturmaTarihi);
				}
				personel.setGuncellemeTarihi(null);
				personel.setGuncelleyenUser(null);
				if (sirket != null)
					personel.setSirket(sirket);
				if (basSirketTarih != null) {
					pdksPersonel.setIstenAyrilisTarihi(istenAyrilisTarihi);
					personel.setIseBaslamaTarihi(basSirketTarih);
					if (PdksUtil.getCanliSunucuDurum())
						pdksEntityController.saveOrUpdate(session, entityManager, pdksPersonel);
				}
				pdksEntityController.saveOrUpdate(session, entityManager, personel);
				flush = true;
			}

			if (flush)
				session.flush();
		}
		return "";
	}

	public List<PersonelView> getPersonelList() {
		return personelList;
	}

	public void setPersonelList(List<PersonelView> personelList) {
		this.personelList = personelList;
	}

	public FileUpload getFile() {
		return file;
	}

	public void setFile(FileUpload file) {
		this.file = file;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public List<SelectItem> getSirketList() {
		return sirketList;
	}

	public void setSirketList(List<SelectItem> sirketList) {
		this.sirketList = sirketList;
	}

	public Long getSirketId() {
		return sirketId;
	}

	public void setSirketId(Long sirketId) {
		this.sirketId = sirketId;
	}

	public Date getBasSirketTarih() {
		return basSirketTarih;
	}

	public void setBasSirketTarih(Date basSirketTarih) {
		this.basSirketTarih = basSirketTarih;
	}

	public boolean isSorunYok() {
		return sorunYok;
	}

	public void setSorunYok(boolean sorunYok) {
		this.sorunYok = sorunYok;
	}
}
