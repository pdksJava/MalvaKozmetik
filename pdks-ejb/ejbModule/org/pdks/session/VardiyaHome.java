package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
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
import org.pdks.entity.CalismaModeliVardiya;
import org.pdks.entity.CalismaSekli;
import org.pdks.entity.Departman;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.VardiyaSablonu;
import org.pdks.entity.VardiyaYemekIzin;
import org.pdks.entity.YemekIzin;
import org.pdks.security.entity.User;

@Name("vardiyaHome")
public class VardiyaHome extends EntityHome<Vardiya> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5118807122083827640L;
	static Logger logger = Logger.getLogger(VardiyaHome.class);
	@RequestParameter
	Long pdksVardiyaId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;

	@In(required = false, create = true)
	HashMap parameterMap;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false)
	FacesMessages facesMessages;

	private List<String> saatList = new ArrayList<String>();
	private List<String> dakikaList = new ArrayList<String>();
	private List<String> toleransDakikaList = new ArrayList<String>();
	private List<SelectItem> vardiyaTipiList = new ArrayList<SelectItem>();
	private List<Vardiya> vardiyaList = new ArrayList<Vardiya>(), izinCalismaVardiyaList = new ArrayList<Vardiya>();
	private List<VardiyaSablonu> sablonList = new ArrayList<VardiyaSablonu>();
	private List<Departman> departmanList = new ArrayList<Departman>();
	private List<CalismaModeli> calismaModeliList = new ArrayList<CalismaModeli>(), calismaModeliKayitliList = new ArrayList<CalismaModeli>();
	private List<YemekIzin> yemekIzinList = new ArrayList<YemekIzin>(), yemekIzinKayitliList = new ArrayList<YemekIzin>();
	private List<CalismaSekli> calismaSekliList;
	private List<YemekIzin> yemekList;
	private int saat = 13, dakika = 0;
	private boolean manuelVardiyaIzinGir = false;
	private Session session;

	@Override
	public Object getId() {
		if (pdksVardiyaId == null) {
			return super.getId();
		} else {
			return pdksVardiyaId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public void fillYemekList(Vardiya pdksVardiya) {
		if (pdksVardiya.getId() == null || pdksVardiya.isCalisma()) {
			yemekIzinList = ortakIslemler.getYemekList(new Date(), null, session);
			if (pdksVardiya.getId() != null) {
				HashMap parametreMap = new HashMap();
				parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "yemekIzin");
				parametreMap.put("vardiya.id", pdksVardiya.getId());
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				yemekIzinKayitliList = pdksEntityController.getObjectByInnerObjectList(parametreMap, VardiyaYemekIzin.class);
				if (yemekIzinKayitliList.size() > 1)
					yemekIzinKayitliList = PdksUtil.sortListByAlanAdi(yemekIzinKayitliList, "yemekNumeric", false);
			} else
				yemekIzinKayitliList = new ArrayList<YemekIzin>();
			for (Iterator iterator = yemekIzinList.iterator(); iterator.hasNext();) {
				YemekIzin veri = (YemekIzin) iterator.next();
				if (veri.getOzelMola() == null || veri.getOzelMola().equals(Boolean.FALSE))
					iterator.remove();
				else {
					for (Iterator iterator2 = yemekIzinKayitliList.iterator(); iterator2.hasNext();) {
						YemekIzin kayit = (YemekIzin) iterator2.next();
						if (kayit.getId().equals(veri.getId())) {
							iterator.remove();
							break;
						}
					}
				}
			}
		} else {
			yemekIzinList = new ArrayList<YemekIzin>();
			yemekIzinKayitliList = new ArrayList<YemekIzin>();
		}

	}

	public void fillCalismaModeliList(Vardiya pdksVardiya) {
		if (pdksVardiya.getId() == null || pdksVardiya.isCalisma()) {
			HashMap map = new HashMap();
			map.put("durum", Boolean.TRUE);
			map.put("genelVardiya", Boolean.FALSE);
			if (session != null)
				map.put(PdksEntityController.MAP_KEY_SESSION, session);
			calismaModeliList = pdksEntityController.getObjectByInnerObjectList(map, CalismaModeli.class);
			if (pdksVardiya.getId() != null) {
				HashMap parametreMap = new HashMap();
				parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "calismaModeli");
				parametreMap.put("vardiya.id", pdksVardiya.getId());
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				calismaModeliKayitliList = pdksEntityController.getObjectByInnerObjectList(parametreMap, CalismaModeliVardiya.class);
				if (calismaModeliKayitliList.size() > 1)
					calismaModeliKayitliList = PdksUtil.sortObjectStringAlanList(calismaModeliKayitliList, "getAciklama", null);
			} else
				calismaModeliKayitliList = new ArrayList<CalismaModeli>();
			for (Iterator iterator = calismaModeliList.iterator(); iterator.hasNext();) {
				CalismaModeli veri = (CalismaModeli) iterator.next();
				for (Iterator iterator2 = calismaModeliKayitliList.iterator(); iterator2.hasNext();) {
					CalismaModeli kayit = (CalismaModeli) iterator2.next();
					if (kayit.getId().equals(veri.getId())) {
						iterator.remove();
						break;
					}

				}
			}
			if (calismaModeliList.size() > 1)
				calismaModeliList = PdksUtil.sortObjectStringAlanList(calismaModeliList, "getAciklama", null);
		} else {
			calismaModeliList = new ArrayList<CalismaModeli>();
			calismaModeliKayitliList = new ArrayList<CalismaModeli>();
		}
	}

	public void fillBagliOlduguDepartmanTanimList() {

		List tanimList = null;
		HashMap parametreMap = new HashMap();
		parametreMap.put("durum", Boolean.TRUE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			tanimList = ortakIslemler.fillDepartmanTanimList(session);

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		} finally {
			parametreMap = null;
		}

		setDepartmanList(tanimList);
	}

	/**
	 * @param vardiya
	 * @return
	 */
	public String getArifeMesaiAciklama(Vardiya vardiya) {
		String title = "";
		if (vardiya != null) {
			try {
				Date tarih = PdksUtil.getDate(new Date());
				Calendar cal = Calendar.getInstance();
				cal.setTime(tarih);
				VardiyaGun tmp = new VardiyaGun(authenticatedUser.getPdksPersonel(), vardiya, tarih);
				tmp.setVardiyaZamani();
				Vardiya islemVardiya = tmp.getIslemVardiya();
				cal.set(Calendar.HOUR_OF_DAY, saat);
				cal.set(Calendar.MINUTE, dakika);
				Date arifeBaslangicTarihi = cal.getTime();
				Date bitisTarihi = null;
				CalismaSekli calismaSekli = vardiya.getCalismaSekli();
				Double arifeNormalCalismaDakika = null;
				if (vardiya.isCalisma() && (calismaSekli != null || (vardiya.getArifeNormalCalismaDakika() != null && vardiya.getArifeNormalCalismaDakika() > 0.0d))) {
					arifeNormalCalismaDakika = calismaSekli != null ? calismaSekli.getArifeNormalCalismaDakika() : null;
					if (vardiya.getArifeNormalCalismaDakika() != null && vardiya.getArifeNormalCalismaDakika() > 0.0d)
						arifeNormalCalismaDakika = vardiya.getArifeNormalCalismaDakika();
					if (arifeNormalCalismaDakika != null) {
						cal.setTime(islemVardiya.getVardiyaBasZaman());
						cal.add(Calendar.MINUTE, arifeNormalCalismaDakika.intValue());
						arifeBaslangicTarihi = cal.getTime();
					}
					bitisTarihi = islemVardiya.getVardiyaBitZaman();
				} else {
					if (vardiya.isCalisma()) {
						if (arifeBaslangicTarihi.before(islemVardiya.getVardiyaBasZaman()))
							arifeBaslangicTarihi = islemVardiya.getVardiyaBasZaman();
						bitisTarihi = islemVardiya.getVardiyaBitZaman();
					}

					else
						bitisTarihi = PdksUtil.tariheGunEkleCikar(tarih, 1);
				}
				List yemekArifeList = new ArrayList();
				islemVardiya.setArifeBaslangicTarihi(arifeBaslangicTarihi);
				Double sure = ortakIslemler.getSaatSure(arifeBaslangicTarihi, bitisTarihi, yemekArifeList, tmp, session), netSure = vardiya.isCalisma() ? islemVardiya.getNetCalismaSuresi() : 0;
				if (netSure > 0 && sure > netSure)
					sure = netSure;
				tmp = null;
				title = "Tatil başlama saati : " + authenticatedUser.timeFormatla(arifeBaslangicTarihi) + (sure > 0 ? "<br></br>Tatil süresi : " + authenticatedUser.sayiFormatliGoster(sure) : "");
				if (calismaSekli != null || (arifeNormalCalismaDakika != null && arifeNormalCalismaDakika > 0.0d)) {
					if (vardiya.isCalisma())
						sure = ortakIslemler.getSaatSure(islemVardiya.getVardiyaBasZaman(), arifeBaslangicTarihi, yemekList, tmp, session);
					else
						sure = 0.0d;
					title += (sure > 0 ? "<br></br>Çalışma süresi : " + authenticatedUser.sayiFormatliGoster(sure) : "");

					title += (sure > 0 ? "<br></br>Eklenen süre : " + authenticatedUser.sayiFormatliGoster(arifeNormalCalismaDakika / 60d) : "");
				}
				if (netSure > 0.0d)
					title += (sure > 0 ? "<br></br><b>Net süre : " + authenticatedUser.sayiFormatliGoster(netSure) + "</b>" : "");
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

		}
		return title;
	}

	/**
	 * @param pdksVardiya
	 * @return
	 */
	public String kayitKopyala(Vardiya pdksVardiya) {
		Vardiya pdksVardiyaYeni = (Vardiya) pdksVardiya.clone();
		if (authenticatedUser.isIK())
			pdksVardiyaYeni.setDepartman(authenticatedUser.getDepartman());
		pdksVardiyaYeni.setId(null);
		if (pdksVardiya.getKisaAdi() != null)
			pdksVardiyaYeni.setKisaAdi(pdksVardiya.getKisaAdi() + " kopya");
		pdksVardiyaYeni.setAdi(pdksVardiya.getAdi() + " kopya");
		vardiyaAlanlariDoldur(pdksVardiyaYeni);
		return "";

	}

	/**
	 * @param pdksVardiya
	 */
	public void kayitGuncelle(Vardiya pdksVardiya) {
		if (pdksVardiya == null) {
			pdksVardiya = new Vardiya();
			pdksVardiya.setTipi(String.valueOf(Vardiya.TIPI_CALISMA));
			pdksVardiya.setBasDakika((short) 0);
			pdksVardiya.setBitDakika((short) 0);
			pdksVardiya.setGirisErkenToleransDakika((short) 0);
			pdksVardiya.setGirisGecikmeToleransDakika((short) 0);
			pdksVardiya.setCikisErkenToleransDakika((short) 0);
			pdksVardiya.setCikisGecikmeToleransDakika((short) 0);
		}
		vardiyaAlanlariDoldur(pdksVardiya);
	}

	/**
	 * @param pdksVardiya
	 */
	private void vardiyaAlanlariDoldur(Vardiya pdksVardiya) {
		fillYemekList(pdksVardiya);
		fillCalismaModeliList(pdksVardiya);
		setInstance(pdksVardiya);
		pdksVardiya.setTipi(String.valueOf(pdksVardiya.getVardiyaTipi()));
		fillCalismaSekilleri();
		fillVardiyaTipiList();
	}

	@Transactional
	public String save() {
		String cikis = "";
		Vardiya pdksVardiya = getInstance();
		if (pdksVardiya.getId() == null && !authenticatedUser.isAdmin())
			pdksVardiya.setDepartman(authenticatedUser.getDepartman());
		boolean yaz = Boolean.TRUE;
		try {
			if (!pdksVardiya.isCalisma()) {
				pdksVardiya.setBasDakika((short) 0);
				pdksVardiya.setBasSaat((short) 0);
				pdksVardiya.setBitDakika((short) 0);
				pdksVardiya.setBitSaat((short) 0);
				pdksVardiya.setGirisErkenToleransDakika((short) 0);
				pdksVardiya.setGirisGecikmeToleransDakika((short) 0);
				pdksVardiya.setCikisErkenToleransDakika((short) 0);
				pdksVardiya.setCikisGecikmeToleransDakika((short) 0);
				if (!pdksVardiya.isIzin()) {
					pdksVardiya.setCalismaGun(0);
					pdksVardiya.setCalismaSaati(0);
				}
			}
			if (yaz) {
				HashMap parametreMap = new HashMap();
				parametreMap.put("id", authenticatedUser.getId());
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				User islemYapan = (User) pdksEntityController.getObjectByInnerObject(parametreMap, User.class);
				if (pdksVardiya.getId() == null) {
					pdksVardiya.setOlusturmaTarihi(new Date());
					pdksVardiya.setOlusturanUser(islemYapan);
				}

				else {
					pdksVardiya.setGuncelleyenUser(islemYapan);
					pdksVardiya.setGuncellemeTarihi(new Date());
				}
				if (!pdksVardiya.isCalisma())
					pdksVardiya.setMesaiOde(null);
				else if (pdksVardiya.getAksamVardiya() != null && pdksVardiya.getAksamVardiya().booleanValue())
					pdksVardiya.setAksamVardiya(pdksVardiya.getBasSaat() >= pdksVardiya.getBitSaat());
				if (pdksVardiya.getGenel() != null && pdksVardiya.getGenel()) {
					pdksVardiya.setIcapVardiya(Boolean.FALSE);
					pdksVardiya.setSua(Boolean.FALSE);
					pdksVardiya.setGebelik(Boolean.FALSE);
				}
				pdksEntityController.saveOrUpdate(session, entityManager, pdksVardiya);
				if (calismaModeliList.size() + calismaModeliKayitliList.size() > 0) {
					parametreMap.clear();
					parametreMap.put("vardiya.id", pdksVardiya.getId());
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<CalismaModeliVardiya> kayitliCalismaModeliVardiyaList = pdksEntityController.getObjectByInnerObjectList(parametreMap, CalismaModeliVardiya.class);
					for (Iterator iterator = calismaModeliKayitliList.iterator(); iterator.hasNext();) {
						CalismaModeli kayitli = (CalismaModeli) iterator.next();
						boolean ekle = true;
						for (Iterator iterator2 = kayitliCalismaModeliVardiyaList.iterator(); iterator2.hasNext();) {
							CalismaModeliVardiya vyi = (CalismaModeliVardiya) iterator2.next();
							if (vyi.getCalismaModeli().getId().equals(kayitli.getId())) {
								ekle = false;
								iterator2.remove();
								break;
							}
						}
						if (ekle) {
							CalismaModeliVardiya vyi = new CalismaModeliVardiya(pdksVardiya, kayitli);
							pdksEntityController.saveOrUpdate(session, entityManager, vyi);
						}
					}
					for (Iterator iterator2 = kayitliCalismaModeliVardiyaList.iterator(); iterator2.hasNext();) {
						CalismaModeliVardiya vyi = (CalismaModeliVardiya) iterator2.next();
						pdksEntityController.deleteObject(session, entityManager, vyi);
					}
					kayitliCalismaModeliVardiyaList = null;
				}
				if (yemekIzinList.size() + yemekIzinKayitliList.size() > 0) {
					parametreMap.clear();
					parametreMap.put("vardiya.id", pdksVardiya.getId());
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<VardiyaYemekIzin> list = pdksEntityController.getObjectByInnerObjectList(parametreMap, VardiyaYemekIzin.class);
					for (Iterator iterator = yemekIzinKayitliList.iterator(); iterator.hasNext();) {
						YemekIzin kayitli = (YemekIzin) iterator.next();
						boolean ekle = true;
						for (Iterator iterator2 = list.iterator(); iterator2.hasNext();) {
							VardiyaYemekIzin vyi = (VardiyaYemekIzin) iterator2.next();
							if (vyi.getYemekIzin().getId().equals(kayitli.getId())) {
								ekle = false;
								iterator2.remove();
								break;
							}

						}
						if (ekle) {
							VardiyaYemekIzin vyi = new VardiyaYemekIzin(pdksVardiya, kayitli);
							pdksEntityController.saveOrUpdate(session, entityManager, vyi);
						}
					}
					for (Iterator iterator2 = list.iterator(); iterator2.hasNext();) {
						VardiyaYemekIzin vyi = (VardiyaYemekIzin) iterator2.next();
						pdksEntityController.deleteObject(session, entityManager, vyi);
					}
					list = null;
				}
				session.flush();
				fillVardiyalar();
				cikis = "persisted";
			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			PdksUtil.addMessageWarn(e.getMessage());
		}

		return cikis;

	}

	public void fillSaatler() {

		List<String> saatListesi = PdksUtil.getSayilar(0, 24, 1);
		List<String> dakikaListesi = PdksUtil.getSayilar(0, 60, 5);
		List<String> toleransDakikaList = PdksUtil.getSayilar(0, 20, 5);

		setSaatList(saatListesi);
		setDakikaList(dakikaListesi);
		setToleransDakikaList(toleransDakikaList);
	}

	public void fillVardiyalar() {

		HashMap parametreMap = new HashMap();
		try {
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			manuelVardiyaIzinGir = ortakIslemler.isVardiyaIzinGir(session, authenticatedUser.getDepartman());
			List<Vardiya> vardiyalar = pdksEntityController.getObjectByInnerObjectList(parametreMap, Vardiya.class);
			yemekList = ortakIslemler.getYemekList(new Date(), null, session);
			HashMap<Long, Vardiya> vardiyaMap = new HashMap<Long, Vardiya>();
			Calendar cal = Calendar.getInstance();
			VardiyaGun vardiyaGun = new VardiyaGun();
			vardiyaGun.setVardiyaDate(PdksUtil.getDate(cal.getTime()));
			// boolean yemekTanimsiz = yemekList == null || yemekList.isEmpty();
			for (Iterator iterator = vardiyalar.iterator(); iterator.hasNext();) {
				Vardiya vardiya = (Vardiya) iterator.next();
				Double yarimGunSure = vardiya.getArifeNormalCalismaDakika() != null && vardiya.getArifeNormalCalismaDakika().doubleValue() > 0.0 ? vardiya.getArifeNormalCalismaDakika() : null;
				if (yarimGunSure == null) {
					yarimGunSure = vardiya.getNetCalismaSuresi() * 30;
				}
				if (vardiya.getDurum() && vardiya.isCalisma()) {

					vardiyaGun.setVardiya(vardiya);
					vardiyaGun.setIslemVardiya(null);
					vardiyaGun.setVardiyaZamani();
					Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
					cal.setTime(islemVardiya.getVardiyaBasZaman());
					cal.add(Calendar.MINUTE, yarimGunSure.intValue());
					Date arifeBaslangicTarihi = cal.getTime();
					Double sure = ortakIslemler.getSaatSure(arifeBaslangicTarihi, islemVardiya.getVardiyaBitZaman(), yemekList, vardiyaGun, session) - (vardiya.getNetCalismaSuresi() * 0.5d);
					if (authenticatedUser.isAdmin() && sure.doubleValue() != 0.0d)
						logger.debug(vardiya.getId() + " --> " + vardiya.getKisaAdi() + " : " + yarimGunSure + " " + sure);
				}
				vardiya.setYemekIzinList(null);
				if (vardiya.getDurum() && vardiya.isCalisma())
					vardiyaMap.put(vardiya.getId(), vardiya);
			}
			if (!vardiyaMap.isEmpty()) {
				try {
					parametreMap.clear();
					parametreMap.put("vardiya.id", new ArrayList(vardiyaMap.keySet()));
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<VardiyaYemekIzin> vardiyaYemekIzinList = pdksEntityController.getObjectByInnerObjectList(parametreMap, VardiyaYemekIzin.class);
					if (!vardiyaYemekIzinList.isEmpty()) {
						if (vardiyaYemekIzinList.size() > 1)
							vardiyaYemekIzinList = PdksUtil.sortListByAlanAdi(vardiyaYemekIzinList, "yemekNumeric", true);
						for (Iterator iterator = vardiyaYemekIzinList.iterator(); iterator.hasNext();) {
							VardiyaYemekIzin vardiyaYemekIzin = (VardiyaYemekIzin) iterator.next();
							YemekIzin yemekIzin = vardiyaYemekIzin.getYemekIzin();
							if (yemekIzin.getOzelMola() != null && yemekIzin.getOzelMola()) {
								Vardiya vardiya = vardiyaMap.get(vardiyaYemekIzin.getVardiya().getId());
								if (vardiya.getYemekIzinList() == null)
									vardiya.setYemekIzinList(new ArrayList<YemekIzin>());
								vardiya.getYemekIzinList().add(yemekIzin);
							}

						}
					}
					vardiyaYemekIzinList = null;
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}

			}
			if (!authenticatedUser.isAdmin()) {
				List<Vardiya> digerVardiyalar = new ArrayList<Vardiya>();
				if (!vardiyalar.isEmpty())
					vardiyalar = PdksUtil.sortListByAlanAdi(vardiyalar, "sonIslemTarihi", Boolean.TRUE);
				for (Iterator iterator = vardiyalar.iterator(); iterator.hasNext();) {
					Vardiya pdksVardiya = (Vardiya) iterator.next();
					if (pdksVardiya.getDepartman() == null || !authenticatedUser.getDepartman().getId().equals(pdksVardiya.getDepartman().getId())) {
						iterator.remove();
						if (pdksVardiya.getDepartman() != null && authenticatedUser.isIKAdmin())
							digerVardiyalar.add(pdksVardiya);
					}

				}
				if (!vardiyalar.isEmpty())
					vardiyalar = PdksUtil.sortListByAlanAdi(vardiyalar, "sonIslemTarihi", Boolean.TRUE);
				if (!digerVardiyalar.isEmpty())
					vardiyalar.addAll(digerVardiyalar);
				digerVardiyalar = null;
			}
			vardiyaList.clear();
			if (!vardiyalar.isEmpty()) {
				vardiyalar = PdksUtil.sortObjectStringAlanList(vardiyalar, "getKisaAdiSort", null);
				TreeMap<String, List<Vardiya>> map = new TreeMap<String, List<Vardiya>>();
				for (Iterator iterator = vardiyalar.iterator(); iterator.hasNext();) {
					Vardiya vardiya = (Vardiya) iterator.next();
					if (authenticatedUser.isIK() && (!vardiya.isCalisma())) {
						iterator.remove();
						continue;
					}
					if (vardiya.getDurum()) {
						if (!vardiya.isCalisma() && vardiya.getDepartman() == null) {
							vardiyaList.add(vardiya);
						} else {
							String key = vardiya.getDepartman() != null ? vardiya.getDepartman().getId().toString() : "";
							List<Vardiya> list = map.containsKey(key) ? map.get(key) : new ArrayList<Vardiya>();
							if (list.isEmpty())
								map.put(key, list);
							list.add(vardiya);
						}
						iterator.remove();
					}

				}
				for (String key : map.keySet())
					vardiyaList.addAll(map.get(key));

				if (!vardiyalar.isEmpty())
					vardiyaList.addAll(vardiyalar);
			}

			saat = 13;
			dakika = 0;
			String yarimGunStr = (parameterMap.containsKey("yarimGunSaati") ? (String) parameterMap.get("yarimGunSaati") : "");
			if (yarimGunStr.indexOf(":") > 0) {
				StringTokenizer st = new StringTokenizer(yarimGunStr, ":");
				if (st.countTokens() == 2) {
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
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.error("Hata : fillVardiyalar " + e.getMessage() + " " + new Date());
		} finally {
			parametreMap = null;
		}

	}

	public void fillCalismaSekilleri() {
		HashMap parametreMap = new HashMap();
		try {
			parametreMap.put("durum", Boolean.TRUE);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<CalismaSekli> list = pdksEntityController.getObjectByInnerObjectList(parametreMap, CalismaSekli.class);
			setCalismaSekliList(list);

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.error("Hata : fillSablonlar " + e.getMessage() + " " + new Date());
		} finally {
			parametreMap = null;
		}

	}

	public void fillSablonlar() {
		HashMap parametreMap = new HashMap();
		try {
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<VardiyaSablonu> sablonList = pdksEntityController.getObjectByInnerObjectList(parametreMap, VardiyaSablonu.class);
			setSablonList(sablonList);

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.error("Hata : fillSablonlar " + e.getMessage() + " " + new Date());
		} finally {
			parametreMap = null;
		}

	}

	public void instanceRefresh() {
		Vardiya pdksVardiya = getInstance();
		if (pdksVardiya.getId() != null)
			try {
				session.refresh(pdksVardiya);
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());

			}

	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		fillSaatler();
		fillVardiyalar();
		fillSablonlar();

		if (authenticatedUser.isAdmin())
			fillBagliOlduguDepartmanTanimList();
	}

	public void fillVardiyaTipiList() {
		List<SelectItem> list = new ArrayList<SelectItem>();
		list.add(new SelectItem(String.valueOf(Vardiya.TIPI_CALISMA), Vardiya.getVardiyaTipiAciklama(Vardiya.TIPI_CALISMA, null)));
		list.add(new SelectItem(String.valueOf(Vardiya.TIPI_HAFTA_TATIL), Vardiya.getVardiyaTipiAciklama(Vardiya.TIPI_HAFTA_TATIL, "HT")));
		list.add(new SelectItem(String.valueOf(Vardiya.TIPI_OFF), Vardiya.getVardiyaTipiAciklama(Vardiya.TIPI_OFF, "OFF")));
		Vardiya vardiya = getInstance();
		boolean adminUser = ortakIslemler.getAdminRole(authenticatedUser);
		if (adminUser) {
			String vardiyaTipi = vardiya.getId() != null ? String.valueOf(vardiya.getVardiyaTipi()) : "";
			if (ortakIslemler.getParameterKey("uygulamaTipi").equals("H"))
				list.add(new SelectItem(String.valueOf(Vardiya.TIPI_RADYASYON_IZNI), Vardiya.getVardiyaTipiAciklama(Vardiya.TIPI_RADYASYON_IZNI, null)));
			if (authenticatedUser.isAdmin()) {
				if (ortakIslemler.getParameterKey("fazlaMesaiIzinKullan").equals("1") || vardiyaTipi.equals(String.valueOf(Vardiya.TIPI_FMI)))
					list.add(new SelectItem(String.valueOf(Vardiya.TIPI_FMI), Vardiya.getVardiyaTipiAciklama(Vardiya.TIPI_FMI, "Fazla Mesai İzin")));
				boolean izinGiris = manuelVardiyaIzinGir || PdksUtil.getCanliSunucuDurum() == false;
				if (izinGiris || vardiyaTipi.equals(String.valueOf(Vardiya.TIPI_IZIN)) || vardiyaTipi.equals(String.valueOf(Vardiya.TIPI_HASTALIK_RAPOR))) {
					if (izinGiris || vardiyaTipi.equals(String.valueOf(Vardiya.TIPI_IZIN)))
						list.add(new SelectItem(String.valueOf(Vardiya.TIPI_IZIN), Vardiya.getVardiyaTipiAciklama(Vardiya.TIPI_IZIN, "İzin Tatil Hariç")));
					if (izinGiris || vardiyaTipi.equals(String.valueOf(Vardiya.TIPI_HASTALIK_RAPOR)))
						list.add(new SelectItem(String.valueOf(Vardiya.TIPI_HASTALIK_RAPOR), Vardiya.getVardiyaTipiAciklama(Vardiya.TIPI_HASTALIK_RAPOR, "İzin Tatil Dahil")));

				}
			}

		}
		setVardiyaTipiList(list);
	}

	public List<SelectItem> getVardiyaTipiList() {
		return vardiyaTipiList;
	}

	public void setVardiyaTipiList(List<SelectItem> vardiyaTipiList) {
		this.vardiyaTipiList = vardiyaTipiList;
	}

	public List<Departman> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<Departman> departmanList) {
		this.departmanList = departmanList;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public List<VardiyaSablonu> getSablonList() {
		return sablonList;
	}

	public void setSablonList(List<VardiyaSablonu> sablonList) {
		this.sablonList = sablonList;
	}

	public List<String> getSaatList() {
		return saatList;
	}

	public void setSaatList(List<String> saatList) {
		this.saatList = saatList;
	}

	public List<String> getDakikaList() {
		return dakikaList;
	}

	public void setDakikaList(List<String> dakikaList) {
		this.dakikaList = dakikaList;
	}

	public List<String> getToleransDakikaList() {
		return toleransDakikaList;
	}

	public void setToleransDakikaList(List<String> toleransDakikaList) {
		this.toleransDakikaList = toleransDakikaList;
	}

	public List<Vardiya> getVardiyaList() {
		return vardiyaList;
	}

	public void setVardiyaList(List<Vardiya> vardiyaList) {
		this.vardiyaList = vardiyaList;
	}

	public List<CalismaSekli> getCalismaSekliList() {
		return calismaSekliList;
	}

	public void setCalismaSekliList(List<CalismaSekli> calismaSekliList) {
		this.calismaSekliList = calismaSekliList;
	}

	public List<CalismaModeli> getCalismaModeliList() {
		return calismaModeliList;
	}

	public void setCalismaModeliList(List<CalismaModeli> calismaModeliList) {
		this.calismaModeliList = calismaModeliList;
	}

	public List<YemekIzin> getYemekIzinList() {
		return yemekIzinList;
	}

	public void setYemekIzinList(List<YemekIzin> yemekIzinList) {
		this.yemekIzinList = yemekIzinList;
	}

	public List<YemekIzin> getYemekList() {
		return yemekList;
	}

	public void setYemekList(List<YemekIzin> yemekList) {
		this.yemekList = yemekList;
	}

	public int getSaat() {
		return saat;
	}

	public void setSaat(int saat) {
		this.saat = saat;
	}

	public int getDakika() {
		return dakika;
	}

	public void setDakika(int dakika) {
		this.dakika = dakika;
	}

	public List<CalismaModeli> getCalismaModeliKayitliList() {
		return calismaModeliKayitliList;
	}

	public void setCalismaModeliKayitliList(List<CalismaModeli> calismaModeliKayitliList) {
		this.calismaModeliKayitliList = calismaModeliKayitliList;
	}

	public List<YemekIzin> getYemekIzinKayitliList() {
		return yemekIzinKayitliList;
	}

	public void setYemekIzinKayitliList(List<YemekIzin> yemekIzinKayitliList) {
		this.yemekIzinKayitliList = yemekIzinKayitliList;
	}

	public boolean isManuelVardiyaIzinGir() {
		return manuelVardiyaIzinGir;
	}

	public void setManuelVardiyaIzinGir(boolean manuelVardiyaIzinGir) {
		this.manuelVardiyaIzinGir = manuelVardiyaIzinGir;
	}

	public List<Vardiya> getIzinCalismaVardiyaList() {
		return izinCalismaVardiyaList;
	}

	public void setIzinCalismaVardiyaList(List<Vardiya> izinCalismaVardiyaList) {
		this.izinCalismaVardiyaList = izinCalismaVardiyaList;
	}
}
