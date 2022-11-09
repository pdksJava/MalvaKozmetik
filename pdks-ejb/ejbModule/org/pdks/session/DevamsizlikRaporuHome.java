package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.Tanim;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.security.entity.User;

@Name("devamsizlikRaporuHome")
public class DevamsizlikRaporuHome extends EntityHome<VardiyaGun> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4025960383128256337L;
	static Logger logger = Logger.getLogger(DevamsizlikRaporuHome.class);

	@RequestParameter
	Long kgsHareketId;
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false)
	User authenticatedUser;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	List<User> userList;
	Date date;
	List<Personel> devamsizlikList = new ArrayList<Personel>();
	List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
	List<Personel> personelList = new ArrayList<Personel>();

	List<HareketKGS> hareketList = new ArrayList<HareketKGS>();
	List<VardiyaGun> vardiyaGunList = new ArrayList<VardiyaGun>();
	private boolean izinliGoster = Boolean.FALSE, gelenGoster = Boolean.FALSE, hareketleriGoster = Boolean.TRUE;
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private String bolumAciklama;
	private Session session;

	@In(required = false)
	FacesMessages facesMessages;

	@Override
	public Object getId() {
		if (kgsHareketId == null) {
			return super.getId();
		} else {
			return kgsHareketId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		// default bugun icin ise gelmeyen raporu cekili olsun
		Date dateBas = PdksUtil.buGun();
		setDate(dateBas);
		vardiyaGunList.clear();
		// devamsizlikListeOlustur();

	}

	private void fillEkSahaTanim() {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
	}

	public Date getDate() {
		return date;
	}

	public String excelAktar() {
		ByteArrayOutputStream baosDosya = null;
		try {
			baosDosya = excelAktarDevam();
			if (baosDosya != null) {
				String dosyaAdi = "DevamsizlikRaporu_" + PdksUtil.convertToDateString(date, "yyyy_MM_dd") + ".xlsx";
				PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi);
			}
		} catch (Exception e) {

		}

		return "";
	}

	private ByteArrayOutputStream excelAktarDevam() {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, PdksUtil.convertToDateString(date, "d MMMMM yyyy") + " Devamsizlik Raporu", Boolean.TRUE);
		CellStyle style = ExcelUtil.getStyleData(wb);
		CellStyle styleCenter = ExcelUtil.getStyleData(wb);
		styleCenter.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle timeStamp = ExcelUtil.getCellStyleTimeStamp(wb);
		CreationHelper factory = wb.getCreationHelper();
		Drawing drawing = sheet.createDrawingPatriarch();
		ClientAnchor anchor = factory.createClientAnchor();
		int row = 0, col = 0;
		boolean aciklamaGoster = authenticatedUser.isAdmin() || izinliGoster || gelenGoster;
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.yoneticiAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Vardiya");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Giriş");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Çıkış");
		if (aciklamaGoster)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Açıklama");
		if (hareketleriGoster) {
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Kapı");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Zaman");
		}

		for (VardiyaGun vardiyaGun : vardiyaGunList) {
			Personel personel = vardiyaGun.getPersonel();
			Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
			List hareketler = hareketleriGoster ? vardiyaGun.getHareketler() : null;
			boolean sifirla = hareketler == null;
			if (sifirla) {
				hareketler = new ArrayList<HareketKGS>();
				hareketler.add(null);
			}
			for (Object hareket : hareketler) {
				HareketKGS hareketKGS = hareket != null ? (HareketKGS) hareket : null;
				row++;
				col = 0;
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getSirket().getAd());
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
				if (personel.getYoneticisi() != null) {
					Personel yonetici = personel.getYoneticisi();
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(yonetici.getPdksSicilNo() + " - " + yonetici.getAdSoyad());
				} else {
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				}
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getPdksSicilNo());
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
				Cell vardiyaCell = ExcelUtil.getCell(sheet, row, col++, styleCenter);
				vardiyaCell.setCellValue(islemVardiya.getKisaAdi());
				Comment commentVardiya = drawing.createCellComment(anchor);
				String vardiyaTitle = authenticatedUser.timeFormatla(islemVardiya.getVardiyaBasZaman()) + " - " + authenticatedUser.timeFormatla(islemVardiya.getVardiyaBitZaman());
				RichTextString strVardiya = factory.createRichTextString(vardiyaTitle);
				commentVardiya.setString(strVardiya);
				vardiyaCell.setCellComment(commentVardiya);
				if (vardiyaGun.getGirisHareketleri() != null)
					ExcelUtil.getCell(sheet, row, col++, timeStamp).setCellValue(vardiyaGun.getGirisHareket().getOrjinalZaman());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				if (vardiyaGun.getCikisHareketleri() != null)
					ExcelUtil.getCell(sheet, row, col++, timeStamp).setCellValue(vardiyaGun.getCikisHareket().getOrjinalZaman());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				if (aciklamaGoster) {
					String aciklama = getVardiyaAciklama(vardiyaGun);
					CellStyle styleIzin = vardiyaGun.getIzin() == null ? style : header;
					Cell createCell = ExcelUtil.getCell(sheet, row, col++, styleIzin);
					createCell.setCellValue(aciklama);
					if (vardiyaGun.getIzin() != null) {
						Comment commentIzin = drawing.createCellComment(anchor);
						String title = vardiyaGun.getIzin().getIzinTipiAciklama();
						RichTextString str1 = factory.createRichTextString(title);
						commentIzin.setString(str1);
						createCell.setCellComment(commentIzin);
					}
				}
				if (hareketleriGoster) {
					if (hareketKGS != null) {
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue(hareketKGS.getKapiView().getKapi().getAciklama());
						ExcelUtil.getCell(sheet, row, col++, timeStamp).setCellValue(hareketKGS.getZaman());
					} else {
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
					}
				}
			}
			if (sifirla)
				hareketler = null;
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
	 * @param vardiyaGun
	 * @return
	 */
	public String getVardiyaAciklama(VardiyaGun vardiyaGun) {
		String aciklama = null;
		if (vardiyaGun.getIzin() != null) {
			aciklama = "İzinli.";
			if (vardiyaGun.getNormalSure() > 0.0d)
				aciklama += " (Çalıştı)";
		} else {
			int girisAdet = vardiyaGun.getGirisHareketleri() != null ? vardiyaGun.getGirisHareketleri().size() : 0;
			int cikisAdet = vardiyaGun.getCikisHareketleri() != null ? vardiyaGun.getCikisHareketleri().size() : 0;
			if (vardiyaGun.getNormalSure() > 0.0) {
				aciklama = "";
			} else if (girisAdet == 0 && cikisAdet == 0) {
				aciklama = "Kart Basılmadı.";
			} else if (cikisAdet > girisAdet) {
				aciklama = "Hatalı Kart Basıldı.";
			} else if (girisAdet > 0) {
				if (girisAdet == 1 && vardiyaGun.getGirisHareket().getZaman().before(vardiyaGun.getIslemVardiya().getVardiyaTelorans2BasZaman()))
					aciklama = "";
				else
					aciklama = "Hatalı Kart Basıldı.";
			}
		}
		if (aciklama == null)
			logger.info(vardiyaGun.getVardiyaKeyStr());

		return aciklama;
	}

	/**
	 * @param pdksVardiyaGun
	 */
	public void hareketGoster(VardiyaGun pdksVardiyaGun) {
		setInstance(pdksVardiyaGun);
		List<HareketKGS> kgsList = pdksVardiyaGun.getHareketler();
		setHareketList(kgsList);

	}

	public void devamsizlikListeOlustur() throws Exception {
		/*
		 * yetkili oldugu Tum personellerin uzerinden dönülür,tek tarih icin cekilir. Vardiyadaki calismasi gereken saat ile hareketten calistigi saatler karsilastirilir. Eksik varsa izin var mi diye bakilir. Diyelim 4 saat eksik calisti 2 saat mazeret buldu. Hala 2 saat eksik vardir. Bunu
		 * gosteririrz. Diyelim hic mazeret girmemiş 4 saat gösteririz
		 */
		List<VardiyaGun> vardiyaList = new ArrayList<VardiyaGun>();
		List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
		List<HareketKGS> kgsList = new ArrayList<HareketKGS>();
		Date tarih1 = null;
		Date tarih2 = null;
		ArrayList<Personel> tumPersoneller = (ArrayList<Personel>) authenticatedUser.getTumPersoneller().clone();
		for (Iterator iterator = tumPersoneller.iterator(); iterator.hasNext();) {
			Personel pdksPersonel = (Personel) iterator.next();
			// if (!pdksPersonel.getPdksSicilNo().equals("0883"))
			// iterator.remove();
			// else
			if (pdksPersonel.getPdks() == null || !pdksPersonel.getPdks())
				iterator.remove();

		}
		if (!tumPersoneller.isEmpty()) {

			TreeMap<String, VardiyaGun> vardiyaMap = ortakIslemler.getIslemVardiyalar((List<Personel>) tumPersoneller, date, PdksUtil.tariheGunEkleCikar(date, 1), Boolean.FALSE, session, Boolean.TRUE);
			vardiyaList = new ArrayList<VardiyaGun>(vardiyaMap.values());

			// butun personeller icin hareket cekerken bu en kucuk tarih ile en
			// buyuk tarih araligini kullanacaktir
			// bu araliktaki tum hareketleri cekecektir.
			for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
				VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator.next();
				if (PdksUtil.tarihKarsilastirNumeric(pdksVardiyaGun.getVardiyaDate(), date) != 0) {
					iterator.remove();
					continue;

				}
				if (pdksVardiyaGun.getVardiya() == null || !pdksVardiyaGun.getVardiya().isCalisma()) {
					iterator.remove();
					continue;
				}

				if (tarih1 == null || pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans1BasZaman().getTime() < tarih1.getTime())
					tarih1 = pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans1BasZaman();

				if (tarih2 == null || pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans2BitZaman().getTime() > tarih2.getTime())
					tarih2 = pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans2BitZaman();

			}
			if (tarih1 != null && tarih2 != null) {
				try {
					HashMap parametreMap = new HashMap();
					parametreMap.put("bakiyeIzinTipi", null);
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<IzinTipi> izinler = pdksEntityController.getObjectByInnerObjectList(parametreMap, IzinTipi.class);
					if (!izinler.isEmpty()) {
						HashMap parametreMap2 = new HashMap();
						parametreMap2.put("baslangicZamani<=", tarih2);
						parametreMap2.put("bitisZamani>=", tarih1);
						parametreMap2.put("izinTipi", izinler);
						parametreMap2.put("izinSahibi", tumPersoneller.clone());
						if (session != null)
							parametreMap2.put(PdksEntityController.MAP_KEY_SESSION, session);
						izinList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap2, PersonelIzin.class);
						parametreMap2 = null;
					} else
						izinList = new ArrayList<PersonelIzin>();
					izinler = null;
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					logger.debug(e.getMessage());
				}
				List<Long> kapiIdler = ortakIslemler.getPdksKapiIdler(session, Boolean.TRUE);
				if (kapiIdler != null && !kapiIdler.isEmpty())
					kgsList = ortakIslemler.getPdksHareketBilgileri(Boolean.TRUE, kapiIdler, (List<Personel>) tumPersoneller.clone(), tarih1, tarih2, HareketKGS.class, session);
				else
					kgsList = new ArrayList<HareketKGS>();
				if (!kgsList.isEmpty()) {
					for (Iterator iterator = kgsList.iterator(); iterator.hasNext();) {
						HareketKGS kgsHareket = (HareketKGS) iterator.next();
						try {
							if (kgsHareket.getPersonel().getPdksPersonel() != null && !kgsHareket.getPersonel().getPdksPersonel().getPdks())
								iterator.remove();

						} catch (Exception e) {
							logger.error("PDKS hata in : \n");
							e.printStackTrace();
							logger.error("PDKS hata out : " + e.getMessage());
							iterator.remove();
						}

					}
					if (kgsList.size() > 1)
						kgsList = PdksUtil.sortListByAlanAdi(kgsList, "zaman", Boolean.FALSE);

				}

				try {

					for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
						VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
						if (!vardiyaGun.getIslemVardiya().isCalisma()) {
							iterator.remove();
							continue;
						}
						vardiyaGun.setHareketler(null);
						vardiyaGun.setGirisHareketleri(null);
						vardiyaGun.setCikisHareketleri(null);

						for (Iterator iterator1 = kgsList.iterator(); iterator1.hasNext();) {
							HareketKGS kgsHareket = (HareketKGS) iterator1.next();
							if (vardiyaGun.getPersonel().getId().equals(kgsHareket.getPersonel().getPdksPersonel().getId())) {
								if (vardiyaGun.addHareket(kgsHareket, Boolean.TRUE))
									iterator1.remove();

							}
						}

						PersonelIzin izin = null;
						for (Iterator iterator2 = izinList.iterator(); iterator2.hasNext();) {
							PersonelIzin personelIzin = (PersonelIzin) iterator2.next();
							if (vardiyaGun.getPersonel().getId().equals(personelIzin.getIzinSahibi().getId())) {
								izin = ortakIslemler.setIzinDurum(vardiyaGun, personelIzin);
								if (izin != null) {
									iterator2.remove();
									break;
								}

							}

						}
						boolean yaz = Boolean.TRUE;
						if (vardiyaGun.getVardiya().isCalisma()) {
							if (vardiyaGun.getHareketDurum()) {

								boolean izinDurum = Boolean.FALSE;
								if (izin != null) {
									long izinBaslangic = izin.getBaslangicZamani().getTime();
									long izinBitis = izin.getBitisZamani().getTime();
									izinDurum = vardiyaGun.getIslemVardiya().getVardiyaBasZaman().getTime() <= izinBitis && vardiyaGun.getIslemVardiya().getVardiyaBitZaman().getTime() >= izinBaslangic;
								}
								if (izinDurum) {
									izinDurum = izinliGoster || gelenGoster;
									yaz = izinDurum;
									vardiyaGun.setIzin(izin);
								}

								if (yaz && vardiyaGun.getHareketDurum()) {
									// butun kontrolleri gecmistir. adam calismistir.
									// Ancak burada calistigi saatlerin toplami bulunup
									// calismasi gereken saatle kaslastirilir.
									// calistigisaat<vardiyadaki saat ise yaz true olur
									double calismaSaati = 0;
									ArrayList<HareketKGS> girisHareketleriList = vardiyaGun.getGirisHareketleri();
									ArrayList<HareketKGS> cikisHareketleriList = vardiyaGun.getCikisHareketleri();
									if (girisHareketleriList != null && cikisHareketleriList != null) {
										if (girisHareketleriList.size() == cikisHareketleriList.size()) {
											for (int i = 0; i < girisHareketleriList.size(); i++) {
												HareketKGS girisHareket = girisHareketleriList.get(i);
												HareketKGS cikisHareket = cikisHareketleriList.get(i);
												if (girisHareket == null || cikisHareket == null)
													continue;
												if (girisHareket.getZaman() == null || cikisHareket.getZaman() == null)
													continue;
												calismaSaati += PdksUtil.getSaatFarki(cikisHareket.getZaman(), girisHareket.getZaman());
											}
											if (calismaSaati > 0) {
												double netSure = vardiyaGun.getVardiya().getNetCalismaSuresi();
												yaz = gelenGoster || izinDurum;
												if (calismaSaati > netSure)
													calismaSaati = netSure;
												// eksik saati bulunup ekranda gosterilmelidir.
												// double eksikSaat = netSure > 0 ? netSure - calismaSaati : 0.0d;
												// vardiyaGun.setNormalSure(eksikSaat);
											} else
												yaz = izinDurum || (girisHareketleriList != null && gelenGoster);
										} else
											yaz = true;

									} else {
										String aciklama = getVardiyaAciklama(vardiyaGun);
										yaz = (aciklama == null || !aciklama.equals("")) || gelenGoster || izinDurum;
									}

									vardiyaGun.setNormalSure(calismaSaati);
								}
							} else {
								String aciklama = getVardiyaAciklama(vardiyaGun);
								yaz = (aciklama == null || !aciklama.equals("")) || gelenGoster;
							}

						}
						if (!yaz)
							iterator.remove();
					}
					ortakIslemler.otomatikHareketEkle(new ArrayList<VardiyaGun>(vardiyaMap.values()), session);
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());

				}
			}
		}
		ortakIslemler.vardiyaGunSirala(vardiyaList, authenticatedUser, session);
		if (!vardiyaList.isEmpty())
			fillEkSahaTanim();
		setVardiyaGunList(vardiyaList);

	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<Personel> getDevamsizlikList() {
		return devamsizlikList;
	}

	public void setDevamsizlikList(List<Personel> devamsizlikList) {
		this.devamsizlikList = devamsizlikList;
	}

	public List<PersonelIzin> getIzinList() {
		return izinList;
	}

	public void setIzinList(List<PersonelIzin> izinList) {
		this.izinList = izinList;
	}

	public List<HareketKGS> getHareketList() {
		return hareketList;
	}

	public void setHareketList(List<HareketKGS> hareketList) {
		this.hareketList = hareketList;
	}

	public List<VardiyaGun> getVardiyaGunList() {
		return vardiyaGunList;
	}

	public void setVardiyaGunList(List<VardiyaGun> vardiyaGunList) {
		this.vardiyaGunList = vardiyaGunList;
	}

	public List<Personel> getPersonelList() {
		return personelList;
	}

	public void setPersonelList(List<Personel> personelList) {
		this.personelList = personelList;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public boolean isIzinliGoster() {
		return izinliGoster;
	}

	public void setIzinliGoster(boolean izinliGoster) {
		this.izinliGoster = izinliGoster;
	}

	public boolean isGelenGoster() {
		return gelenGoster;
	}

	public void setGelenGoster(boolean gelenGoster) {
		this.gelenGoster = gelenGoster;
	}

	public boolean isHareketleriGoster() {
		return hareketleriGoster;
	}

	public void setHareketleriGoster(boolean hareketleriGoster) {
		this.hareketleriGoster = hareketleriGoster;
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
