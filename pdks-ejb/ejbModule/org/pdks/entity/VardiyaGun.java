package org.pdks.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.session.PdksUtil;

import com.pdks.notUse.IsKurVardiyaGun;

@Entity(name = VardiyaGun.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI, VardiyaGun.COLUMN_NAME_PERSONEL }) })
public class VardiyaGun extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4533512377234713436L;
	static Logger logger = Logger.getLogger(VardiyaGun.class);

	public static final String TABLE_NAME = "VARDIYA_GUN";
	public static final String COLUMN_NAME_PERSONEL = "PERSONEL_ID";
	public static final String COLUMN_NAME_VARDIYA_TARIHI = "VARDIYA_TARIHI";
	public static final String COLUMN_NAME_VARDIYA = "VARDIYA_ID";
	public static final String COLUMN_NAME_VARDIYA_SAAT = "VARDIYA_SAAT_ID";
	public static final String COLUMN_NAME_VARDIYA_ACIKLAMA = "VARDIYA_ACIKLAMA";
	public static final String COLUMN_NAME_PERSONEL_NO = "PERSONEL_NO";
	public static final String STYLE_CLASS_NORMAL_CALISMA = "calismaAylik";
	public static final String STYLE_CLASS_NORMAL_CALISMA_EVEN = "calismaAylikEven";
	public static final String STYLE_CLASS_OZEL_ISTEK = "ozelIstekAylik";
	public static final String STYLE_CLASS_OFF = "off";
	public static final String STYLE_CLASS_EGITIM = "ozelIstekEgitim";
	public static final String STYLE_CLASS_IZIN = "izinAylik";
	public static final String STYLE_CLASS_HAFTA_TATIL = "tatilAylik";
	public static final String STYLE_CLASS_DIGER_AY = "digerAy";
	public static final String STYLE_CLASS_ODD = "odd";
	public static final String STYLE_CLASS_EVEN = "even";
	public static final String STYLE_CLASS_HATA = "hata";
	public static boolean haftaTatilDurum;

	private Personel personel;
	private Vardiya vardiya, islemVardiya, oncekiVardiya, sonrakiVardiya, yeniVardiya, eskiVardiya;
	private Integer offFazlaMesaiBasDakika, haftaTatiliFazlaMesaiBasDakika;
	private Date vardiyaDate;
	private VardiyaGorev vardiyaGorev;
	private VardiyaSaat vardiyaSaat, vardiyaSaatDB;
	private ArrayList<HareketKGS> hareketler, girisHareketleri, cikisHareketleri, yemekHareketleri;
	private ArrayList<PersonelIzin> izinler;
	private ArrayList<PersonelFazlaMesai> fazlaMesailer;
	private ArrayList<Vardiya> vardiyalar;
	private VardiyaGun oncekiVardiyaGun, sonrakiVardiyaGun;
	private IsKurVardiyaGun isKurVardiya;
	private int beklemeSuresi = 6;
	private Double calismaSuaSaati = PersonelDenklestirme.getCalismaSaatiSua();
	private Boolean izinHaftaTatilDurum;
	private boolean hareketHatali = Boolean.FALSE, planHareketEkle = Boolean.TRUE, kullaniciYetkili = Boolean.TRUE, zamanGuncelle = Boolean.TRUE, zamanGelmedi = Boolean.FALSE;
	private boolean fazlaMesaiTalepOnayliDurum = Boolean.FALSE, fazlaMesaiTalepDurum = Boolean.FALSE, ayarlamaBitti = false;
	private double calismaSuresi = 0, normalSure = 0, resmiTatilSure = 0, haftaTatilDigerSure = 0, gecenAyResmiTatilSure = 0, aksamKatSayisi = 0d, aksamVardiyaSaatSayisi = 0d;
	private double calisilmayanAksamSure = 0, fazlaMesaiSure = 0, bayramCalismaSuresi = 0, haftaCalismaSuresi = 0d, yasalMaxSure = 11.0d;
	private Integer basSaat, basDakika, bitSaat, bitDakika;
	private String tdClass = "", style = "", manuelGirisHTML = "", vardiyaKisaAciklama, personelNo, vardiyaDateStr, donemStr;
	private Tatil tatil;
	private PersonelIzin izin;
	private VardiyaSablonu vardiyaSablonu;
	private HashMap<Integer, BigDecimal> katSayiMap;
	private boolean bitmemisGun = Boolean.TRUE, islendi = Boolean.FALSE, ayrikHareketVar = Boolean.FALSE;
	private int yarimYuvarla = PdksUtil.getYarimYuvarlaLast();
	private HareketKGS ilkGiris, sonCikis;
	private boolean ayinGunu = Boolean.TRUE, onayli = Boolean.TRUE, fiiliHesapla = Boolean.FALSE, hataliDurum = Boolean.FALSE, donemAcik = Boolean.TRUE;
	private List<String> linkAdresler;
	private HashMap<String, Personel> gorevliPersonelMap;
	private CalismaModeli calismaModeli = null;
	private Boolean fazlaMesaiOnayla;
	private Integer version = 0;
	private List<FazlaMesaiTalep> fazlaMesaiTalepler;
	private List<YemekIzin> yemekList;

	public VardiyaGun() {
		super();

	}

	/**
	 * @param xPersonel
	 * @param xVardiya
	 * @param xVardiyaDate
	 */
	public VardiyaGun(Personel xPersonel, Vardiya xVardiya, Date xVardiyaDate) {
		super();
		this.setPersonel(xPersonel);
		this.setVardiya(xVardiya);
		this.setVardiyaDate(xVardiyaDate);
		if (xVardiya != null)
			this.durum = !xVardiya.isCalisma();
	}

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Personel getPersonel() {
		return personel;
	}

	public void setPersonel(Personel personel) {
		if (personel != null) {
			setVardiyaSablonu(personel.getSablon());
			setCalismaModeli(personel.getCalismaModeli());
		}
		this.personel = personel;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_VARDIYA, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Vardiya getVardiya() {
		return vardiya;
	}

	public void setVardiya(Vardiya value) {
		Long oldId = eskiVardiya != null && eskiVardiya.getId() != null ? eskiVardiya.getId() : 0l;
		if (value != null && value.getId() != null)
			value.setIslemVardiyaGun(this);
		if (isKurVardiya != null)
			isKurVardiya.setVardiya(value);
		if (this.isGuncellendi() == false) {
			Long newId = value != null && value.getId() != null ? value.getId() : 0l;
			this.guncellendi = PdksUtil.isLongDegisti(oldId, newId);
		}

		this.vardiya = value;
	}

	@OneToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_VARDIYA_SAAT)
	@Fetch(FetchMode.JOIN)
	public VardiyaSaat getVardiyaSaat() {
		return vardiyaSaat;
	}

	public void setVardiyaSaat(VardiyaSaat value) {
		this.vardiyaSaat = value;
		if (value != null && this.vardiyaSaatDB == null)
			this.vardiyaSaatDB = (VardiyaSaat) value.clone();
	}

	@Column(name = COLUMN_NAME_VARDIYA_ACIKLAMA, insertable = false, updatable = false)
	public String getVardiyaKisaAciklama() {
		return vardiyaKisaAciklama;
	}

	public void setVardiyaKisaAciklama(String vardiyaKisaAciklama) {
		this.vardiyaKisaAciklama = vardiyaKisaAciklama;
	}

	@Column(name = COLUMN_NAME_PERSONEL_NO, insertable = false, updatable = false)
	public String getPersonelNo() {
		return personelNo;
	}

	public void setPersonelNo(String personelNo) {
		this.personelNo = personelNo;
	}

	// @Column(name = "VARDIYA_BAS_SAAT")
	// @Min(value = 0, message = "Sıfır altında değeri olamaz")
	// @Max(value = 23, message = "23 üstünde değeri olamaz")
	@Transient
	public Integer getBasSaat() {
		return basSaat;
	}

	public void setBasSaat(Integer basSaat) {
		this.basSaat = basSaat;
	}

	// @Column(name = "VARDIYA_BAS_DAKIKA")
	// @Min(value = 0, message = "Sıfır altında değeri olamaz")
	// @Max(value = 59, message = "59 üstünde değeri olamaz")
	@Transient
	public Integer getBasDakika() {
		return basDakika;
	}

	public void setBasDakika(Integer basDakika) {
		this.basDakika = basDakika;
	}

	// @Column(name = "VARDIYA_BIT_SAAT")
	// @Min(value = 0, message = "Sıfır altında değeri olamaz")
	// @Max(value = 23, message = "23 üstünde değeri olamaz")
	@Transient
	public Integer getBitSaat() {
		return bitSaat;
	}

	public void setBitSaat(Integer bitSaat) {
		this.bitSaat = bitSaat;
	}

	// @Column(name = "VARDIYA_BIT_DAKIKA")
	// @Min(value = 0, message = "Sıfır altında değeri olamaz")
	// @Max(value = 59, message = "59 üstünde değeri olamaz")
	@Transient
	public Integer getBitDakika() {
		return bitDakika;
	}

	public void setBitDakika(Integer bitDakika) {
		this.bitDakika = bitDakika;
	}

	@Temporal(value = TemporalType.DATE)
	@Column(name = COLUMN_NAME_VARDIYA_TARIHI, nullable = false)
	public Date getVardiyaDate() {
		return vardiyaDate;
	}

	@Transient
	public String getVardiyaDateStr() {
		if (vardiyaDateStr == null)
			vardiyaDateStr = vardiyaDate != null ? PdksUtil.convertToDateString(vardiyaDate, "yyyyMMdd") : "";
		return vardiyaDateStr;

	}

	public void setVardiyaDateStr(String vardiyaDateStr) {
		this.vardiyaDateStr = vardiyaDateStr;
	}

	@Transient
	public String getHeaderClass() {
		String str = "calismaGun";
		if (tatil != null)
			str = tatil.isYarimGunMu() ? "arife" : "bayram";
		return str;

	}

	public void setVardiyaDate(Date value) {
		boolean gunDurum = Boolean.TRUE;
		this.setVardiyaDateStr(null);
		if (value != null) {
			gunDurum = PdksUtil.tarihKarsilastirNumeric(Calendar.getInstance().getTime(), value) != 1;
			this.setVardiyaDateStr(PdksUtil.convertToDateString(value, "yyyyMMdd"));
		}
		setBitmemisGun(gunDurum);
		this.vardiyaDate = value;
	}

	@Transient
	public String getTarihStr() {
		return this.getVardiyaDateStr();
	}

	@Transient
	public Vardiya getIslemVardiya() {
		if (vardiyaDate != null && vardiya != null) {
			if (islemVardiya == null) {
				setVardiyaZamani();
				if (islemVardiya == null) {
					Vardiya vardiyaKopya = (Vardiya) vardiya.clone();
					vardiyaKopya.setKopya(Boolean.TRUE);
					setIslemVardiya(vardiyaKopya);
					if (islemVardiya != null) {
						if (oncekiVardiyaGun != null && oncekiVardiyaGun.getIslemVardiya() != null)
							islemVardiya.setOncekiVardiya(oncekiVardiyaGun.getIslemVardiya());
						islemVardiya.setVardiyaTarih(vardiyaDate);
						islemVardiya.setVardiyaZamani(this);
					}
				}
			}
		}
		return islemVardiya;
	}

	public void setIslemVardiya(Vardiya islemVardiya) {
		this.islemVardiya = islemVardiya;
	}

	@Transient
	public ArrayList<HareketKGS> getOrjinalHareketler() {
		ArrayList<HareketKGS> orjinalHareketler = null;
		if (hareketler != null) {
			orjinalHareketler = new ArrayList<HareketKGS>();
			for (HareketKGS kgsHareket : hareketler) {
				try {
					if (kgsHareket.getId() != null)
						if (kgsHareket.getId().startsWith(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_KGS) || kgsHareket.getId().startsWith(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_PDKS))
							orjinalHareketler.add(kgsHareket);
				} catch (Exception e) {
				}

			}
		}
		return orjinalHareketler;
	}

	@Transient
	public ArrayList<HareketKGS> getHareketler() {
		return hareketler;
	}

	public void setHareketler(ArrayList<HareketKGS> hareketler) {
		this.hareketler = hareketler;
	}

	@Transient
	public ArrayList<HareketKGS> getGirisHareketleri() {
		return girisHareketleri;
	}

	public void setGirisHareketleri(ArrayList<HareketKGS> girisHareketleri) {
		this.girisHareketleri = girisHareketleri;
	}

	@Transient
	public ArrayList<HareketKGS> getCikisHareketleri() {
		return cikisHareketleri;
	}

	public void setCikisHareketleri(ArrayList<HareketKGS> cikisHareketleri) {
		this.cikisHareketleri = cikisHareketleri;
	}

	@Transient
	public boolean isHareketHatali() {
		return hareketHatali;
	}

	public void setHareketHatali(boolean value) {
		if (value) {
			logger.debug(getVardiyaKeyStr());
		}
		this.hareketHatali = value;
	}

	@Transient
	public ArrayList<PersonelIzin> getIzinler() {
		return izinler;
	}

	public void setIzinler(ArrayList<PersonelIzin> izinler) {
		this.izinler = izinler;
	}

	@Transient
	public ArrayList<PersonelFazlaMesai> getFazlaMesailer() {
		if (fazlaMesailer != null)
			logger.debug(this.getVardiyaKeyStr() + " " + fazlaMesailer.size());
		return fazlaMesailer;
	}

	public void setFazlaMesailer(ArrayList<PersonelFazlaMesai> value) {
		if (value != null)
			logger.debug(this.getVardiyaKeyStr() + " " + value.size());
		this.fazlaMesailer = value;
	}

	@Transient
	public double getCalismaSuresi() {
		if (calismaSuresi > 0)
			calismaSuresi = PdksUtil.setSureDoubleTypeRounded(calismaSuresi, yarimYuvarla);
		return calismaSuresi;
	}

	public void setCalismaSuresi(double value) {
		if (this.getVardiyaDateStr().equals("20211028") && value != 0.0d)
			logger.debug(value);
		this.calismaSuresi = value;
	}

	@Transient
	public void addCalismaSuresi(double value) {
		if (value != 0.0d) {
			if (this.getVardiyaDateStr().equals("20230420"))
				logger.debug(value);
		}

		calismaSuresi += value;
	}

	@Transient
	public double getHaftaTatilDigerSure() {
		return haftaTatilDigerSure;
	}

	public void setHaftaTatilDigerSure(double value) {
		this.haftaTatilDigerSure = value;
	}

	public void addHaftaTatilDigerSure(double value) {
		if (value != 0.0d)
			logger.debug(value);
		this.haftaTatilDigerSure += value;
	}

	@Transient
	public double getToplamSure() {
		double toplamSure = getResmiTatilSure() + getCalismaSuresi();
		return toplamSure;
	}

	@Transient
	public double getHaftaCalismaSuresi() {
		return haftaCalismaSuresi;
	}

	public void setHaftaCalismaSuresi(double value) {
		this.haftaCalismaSuresi = value;
	}

	@Transient
	public void addHaftaCalismaSuresi(double value) {
		if (value > 0.0d && isFiiliHesapla())
			logger.debug(value);
		haftaCalismaSuresi += value;
	}

	@Transient
	public void addPersonelFazlaMesai(PersonelFazlaMesai personelFazlaMesai) {
		if (fazlaMesailer == null)
			fazlaMesailer = new ArrayList<PersonelFazlaMesai>();
		fazlaMesailer.add(personelFazlaMesai);
	}

	@Transient
	public void addFazlaMesaiTalep(FazlaMesaiTalep ft) {
		if (fazlaMesaiTalepler == null)
			fazlaMesaiTalepler = new ArrayList<FazlaMesaiTalep>();
		fazlaMesaiTalepler.add(ft);
	}

	@Transient
	public void addPersonelIzin(PersonelIzin personelIzin) {
		if (izinler == null)
			izinler = new ArrayList<PersonelIzin>();
		if (personelIzin.isGunlukOldu()) {
			this.setIzin(personelIzin);
			personelIzin.setGunlukOldu(Boolean.TRUE);
		}
		izinler.add(personelIzin);
	}

	@Transient
	public boolean addHareket(HareketKGS hareket, boolean hareketDuzelt) {
		boolean durum = Boolean.FALSE;
		boolean devam = Boolean.TRUE;
		try {
			Date tarihHareket = PdksUtil.getDate(PdksUtil.tariheGunEkleCikar(hareket.getZaman(), -1)), sonCalismaTarihi = PdksUtil.getDate(hareket.getPersonel().getPdksPersonel().getSskCikisTarihi());
			devam = PdksUtil.tarihKarsilastirNumeric(sonCalismaTarihi, tarihHareket) != -1;

			// !tarihHareket.after(hareket.getPersonel().getPdksPersonel().getSonCalismaTarihi());
		} catch (Exception e) {
			devam = Boolean.TRUE;
		}
		if (!devam) {
			logger.debug("");
		}
		if (devam) {

			if (this.getIslemVardiya() == null)
				setVardiyaZamani();
			Kapi kapi = null;
			try {
				kapi = hareket.getKapiView().getKapi();
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				kapi = null;
			}
			if (kapi != null) {

				if (kapi.isYemekHaneKapi()) {
					if (yemekHareketleri == null)
						yemekHareketleri = new ArrayList<HareketKGS>();
					yemekHareketleri.add(hareket);
				} else if (this.getIslemVardiya() != null) {
					try {
						if (this.getIslemVardiya().getVardiyaFazlaMesaiBasZaman() == null)
							this.getIslemVardiya().setVardiyaFazlaMesaiBasZaman(this.getIslemVardiya().getVardiyaTelorans1BasZaman());
						if (this.getIslemVardiya().getVardiyaFazlaMesaiBitZaman() == null)
							this.getIslemVardiya().setVardiyaFazlaMesaiBitZaman(this.getIslemVardiya().getVardiyaTelorans2BitZaman());
						if (hareket.getZaman().getTime() >= this.getIslemVardiya().getVardiyaFazlaMesaiBasZaman().getTime() && hareket.getZaman().getTime() <= this.getIslemVardiya().getVardiyaFazlaMesaiBitZaman().getTime()) {
							durum = hareketKontrolZamansiz(hareket, hareketDuzelt);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

			}
		}

		return durum;

	}

	/**
	 * @param hareket
	 * @param hareketDuzelt
	 * @return
	 */
	@Transient
	public boolean hareketKontrolZamansiz(HareketKGS hareket, Boolean hareketDuzelt) {
		Kapi kapi = null;

		try {
			kapi = hareket.getKapiView().getKapi();
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			kapi = null;
		}
		boolean durum = Boolean.TRUE;
		// if (this.getVardiyaDateStr().equals("20160409")) {
		// if (hareket.getId().equals("A10823"))
		// logger.info(hareket.getId() + " " + this.getVardiyaDateStr() + " " + hareket.getZaman() + " " + this.getIslemVardiya().getVardiyaFazlaMesaiBasZaman() + " " + this.getIslemVardiya().getVardiyaFazlaMesaiBitZaman());
		// }
		if (hareket != null && (kapi.isGirisKapi() || kapi.isCikisKapi())) {
			if (hareketler == null) {
				hareketler = new ArrayList<HareketKGS>();
				setHareketHatali(kapi.isCikisKapi());
			}

			boolean ekle = Boolean.TRUE;
			KapiSirket kapiSirket = hareket.getKapiView().getKapiKGS() != null ? hareket.getKapiView().getKapiKGS().getKapiSirket() : null;

			if (!hareketler.isEmpty()) {
				int indexSon = hareketler.size() - 1;
				HareketKGS oncekiHareket = hareketler.get(indexSon);
				Double fark = PdksUtil.getDakikaFarki(hareket.getZaman(), oncekiHareket.getZaman());
				if (kapi.isGirisKapi()) {
					if (oncekiHareket.getKapiView().getKapi().isGirisKapi() && fark < beklemeSuresi) {
						boolean duzelt = false;
						KapiSirket oncekiKapiSirket = oncekiHareket.getKapiView().getKapiKGS() != null ? oncekiHareket.getKapiView().getKapiKGS().getKapiSirket() : null;
						if (oncekiKapiSirket != null && kapiSirket != null) {
							duzelt = oncekiKapiSirket.getId().equals(kapiSirket.getId()) || hareket.getZaman().getTime() <= kapiSirket.getBitTarih().getTime();
						} else
							duzelt = true;

						if (duzelt) {
							hareketler.set(indexSon, hareket);

						}
						ekle = Boolean.FALSE;
					}

				} else if (oncekiHareket.getKapiView().getKapi().isCikisKapi()) {
					if (fark < beklemeSuresi) {
						ekle = Boolean.FALSE;
						KapiSirket oncekiKapiSirket = oncekiHareket.getKapiView().getKapiKGS() != null ? oncekiHareket.getKapiView().getKapiKGS().getKapiSirket() : null;
						HareketKGS yeniHareket = (HareketKGS) hareket.clone();
						boolean duzelt = false;
						if (oncekiKapiSirket != null && kapiSirket != null) {
							duzelt = oncekiKapiSirket.getId().equals(kapiSirket.getId()) || yeniHareket.getZaman().getTime() <= kapiSirket.getBitTarih().getTime();
						} else
							duzelt = true;
						if (duzelt) {
							if (hareketDuzelt) {
								yeniHareket.setOrjinalZaman((Date) yeniHareket.getZaman().clone());
								if (yeniHareket.getZaman().getTime() <= islemVardiya.getVardiyaTelorans2BasZaman().getTime())
									yeniHareket.setZaman(islemVardiya.getVardiyaBasZaman());
								else if (yeniHareket.getZaman().getTime() >= islemVardiya.getVardiyaTelorans1BitZaman().getTime())
									yeniHareket.setZaman(islemVardiya.getVardiyaBitZaman());

							}
							indexSon = cikisHareketleri.size() - 1;
							cikisHareketleri.set(indexSon, yeniHareket);
							indexSon = hareketler.size() - 1;
							hareketler.set(indexSon, yeniHareket);
						}

					}

				}

			}
			if (ekle) {

				if (!hareketHatali && !hareketler.isEmpty()) {
					HareketKGS sonHareket = hareketler.get(hareketler.size() - 1);
					boolean hh = sonHareket.getKapiView().getKapi().getTipi().getKodu().equals(kapi.getTipi().getKodu());
					if (hh) {
						logger.debug(getVardiyaKeyStr() + " " + hareket.getId() + " " + hareket.getZaman());
					}
					setHareketHatali(hh);
				}
				hareketler.add(hareket);
				HareketKGS yeniHareket = (HareketKGS) hareket.clone();
				yeniHareket.setOrjinalZaman((Date) yeniHareket.getZaman().clone());
				if (hareketDuzelt) {
					if (yeniHareket.getZaman().getTime() <= islemVardiya.getVardiyaTelorans2BasZaman().getTime())
						yeniHareket.setZaman(islemVardiya.getVardiyaBasZaman());
					else if (yeniHareket.getZaman().getTime() >= islemVardiya.getVardiyaTelorans1BitZaman().getTime())
						yeniHareket.setZaman(islemVardiya.getVardiyaBitZaman());
					if (izinler != null) {
						for (Iterator iterator = izinler.iterator(); iterator.hasNext();) {
							PersonelIzin izin = (PersonelIzin) iterator.next();
							if (izin.getBaslangicZamani().getTime() <= yeniHareket.getZaman().getTime() && izin.getBitisZamani().getTime() >= yeniHareket.getZaman().getTime()) {
								if (yeniHareket.getKapiView().getKapi().isGirisKapi())
									yeniHareket.setZaman(izin.getBitisZamani());
								else if (yeniHareket.getKapiView().getKapi().isCikisKapi())
									yeniHareket.setZaman(izin.getBaslangicZamani());
							}

						}
					}

				}
				if (yeniHareket.getKapiView().getKapi().isGirisKapi()) {
					if (girisHareketleri == null)
						girisHareketleri = new ArrayList<HareketKGS>();
					if (girisHareketleri.isEmpty())
						ilkGiris = yeniHareket;
					girisHareketleri.add(yeniHareket);

				} else if (yeniHareket.getKapiView().getKapi().isCikisKapi()) {
					if (cikisHareketleri == null)
						cikisHareketleri = new ArrayList<HareketKGS>();
					cikisHareketleri.add(yeniHareket);
					sonCikis = yeniHareket;
				}

			}

		}

		return durum;
	}

	@Transient
	public void addPersonelHareket(HareketKGS hareket) {
		if (hareket != null && (hareket.getKapiView().getKapi().isGirisKapi() || hareket.getKapiView().getKapi().isCikisKapi())) {
			if (hareketler == null) {
				hareketler = new ArrayList<HareketKGS>();
				setHareketHatali(hareket.getKapiView().getKapi().isCikisKapi());
			}
			if (!hareketHatali && !hareketler.isEmpty()) {
				HareketKGS sonHareket = hareketler.get(hareketler.size() - 1);
				setHareketHatali(sonHareket.getKapiView().getKapi().getTipi().getKodu().equals(hareket.getKapiView().getKapi().getTipi().getKodu()));
			}
			hareketler.add(hareket);
			HareketKGS yeniHareket = (HareketKGS) hareket.clone();
			long yeniHareketZaman = yeniHareket.getZaman().getTime();
			if ((yeniHareketZaman <= islemVardiya.getVardiyaTelorans2BasZaman().getTime()) && (yeniHareketZaman >= islemVardiya.getVardiyaTelorans1BasZaman().getTime()))
				yeniHareket.setZaman(islemVardiya.getVardiyaBasZaman());
			else if ((yeniHareketZaman >= islemVardiya.getVardiyaTelorans1BitZaman().getTime()) && (yeniHareketZaman <= islemVardiya.getVardiyaTelorans2BitZaman().getTime()))
				yeniHareket.setZaman(islemVardiya.getVardiyaBitZaman());

			if (yeniHareket.getKapiView().getKapi().isGirisKapi()) {
				if (girisHareketleri == null)
					girisHareketleri = new ArrayList<HareketKGS>();
				girisHareketleri.add(yeniHareket);
			} else if (yeniHareket.getKapiView().getKapi().isCikisKapi()) {
				if (cikisHareketleri == null)
					cikisHareketleri = new ArrayList<HareketKGS>();
				cikisHareketleri.add(yeniHareket);
			}

		}

	}

	@Transient
	public HareketKGS getGirisHareket() {
		HareketKGS hareket = girisHareketleri != null ? girisHareketleri.get(0) : null;
		return hareket;
	}

	@Transient
	public HareketKGS getCikisHareket() {
		HareketKGS hareket = cikisHareketleri != null ? cikisHareketleri.get(cikisHareketleri.size() - 1) : null;
		return hareket;
	}

	@Transient
	public HareketKGS getSonGirisHareket() {
		HareketKGS hareket = girisHareketleri != null ? girisHareketleri.get(girisHareketleri.size() - 1) : null;
		return hareket;
	}

	@Transient
	public Date getVardiyaFazlaMesaiBasZaman() {
		if (islemVardiya == null)
			setVardiyaZamani();
		return islemVardiya != null ? islemVardiya.getVardiyaFazlaMesaiBasZaman() : null;
	}

	@Transient
	public Boolean getHareketDurum() {
		boolean hareketDurum = (hareketler == null || !hareketHatali);
		if (hareketDurum && hareketler != null)
			hareketDurum = girisHareketleri != null && cikisHareketleri != null && girisHareketleri.size() == cikisHareketleri.size();
		if (islemVardiya != null) {
			try {
				if (!hareketDurum)
					hareketDurum = ayrikHareketVar == false && islemVardiya.getVardiyaBitZaman().getTime() > new Date().getTime();
				if (!hareketDurum && islemVardiya.isIcapVardiyasi()) {
					// String key = this.getVardiyaDateStr();

					hareketDurum = (hareketler == null || (girisHareketleri != null && cikisHareketleri != null && girisHareketleri.size() == cikisHareketleri.size()));
					// if (hareketDurum == false && key.equals("20151214"))
					// logger.info(this.getVardiyaKeyStr());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		boolean sonDurum = hareketDurum && onayli;

		return sonDurum;
	}

	@Transient
	public String getTdClass() {
		return tdClass;
	}

	public void setTdClass(String tdClass) {
		this.tdClass = tdClass;
	}

	@Transient
	public double getNormalSure() {
		return normalSure;
	}

	public void setNormalSure(double normalSure) {
		this.normalSure = normalSure;
	}

	@Transient
	public Tatil getTatil() {
		return tatil;
	}

	public void setTatil(Tatil value) {
		this.tatil = value;
		if (value != null && vardiya != null && value.getVardiyaMap() != null) {
			if (value.getVardiyaMap().containsKey(vardiya.getId())) {
				Tatil tatilNew = (Tatil) value.clone();
				Vardiya vardiyaTatil = value.getVardiyaMap().get(vardiya.getId());
				if (islemVardiya != null)
					islemVardiya.setArifeCalismaSure(vardiyaTatil.getArifeCalismaSure());
				vardiya.setArifeCalismaSure(vardiyaTatil.getArifeCalismaSure());
				tatilNew.setBasTarih(vardiyaTatil.getArifeBaslangicTarihi());
				this.tatil = tatilNew;
			}

		}

	}

	@Transient
	public boolean isIzinli() {
		boolean izinli = izin != null;
		if (!izinli)
			izinli = vardiya != null && vardiya.isIzin();
		return izinli;
	}

	@Transient
	public PersonelIzin getIzin() {

		return izin;
	}

	public void setIzin(PersonelIzin value) {
		if (vardiyaDateStr.equals("20231112")) {
			if (value != null)
				logger.debug(vardiyaDateStr + " " + value.getId() + " " + value.getAciklama());
			else
				logger.debug("");
		}
		this.izin = value;
	}

	@Transient
	public VardiyaSablonu getVardiyaSablonu() {
		return vardiyaSablonu;
	}

	public void setVardiyaSablonu(VardiyaSablonu vardiyaSablonu) {
		this.vardiyaSablonu = vardiyaSablonu;
	}

	public Vardiya setVardiyaZamani() {
		if (vardiya != null && vardiyaDate != null) {
			if (!islendi || islemVardiya == null) {
				setIslendi(Boolean.TRUE);
				if ((sonrakiVardiya == null && oncekiVardiyaGun == null) || islemVardiya == null) {
					Vardiya vardiyaKopya = (Vardiya) vardiya.clone();
					vardiyaKopya.setKopya(Boolean.TRUE);
					setIslemVardiya(vardiyaKopya);
				}
				if (islemVardiya != null) {
					if (oncekiVardiyaGun != null && oncekiVardiyaGun.getIslemVardiya() != null)
						islemVardiya.setOncekiVardiya(oncekiVardiyaGun.getIslemVardiya());
					islemVardiya.setSonrakiVardiya(sonrakiVardiya);
					islemVardiya.setVardiyaTarih(vardiyaDate);
					islemVardiya.setVardiyaZamani(this);
				}
			}
		}
		return islemVardiya;

	}

	public Vardiya setIslemVardiyaZamani() {
		if (vardiya != null && vardiyaDate != null && !islendi && islemVardiya == null) {
			setIslendi(Boolean.TRUE);
			Vardiya vardiyaKopya = (Vardiya) vardiya.clone();
			vardiyaKopya.setKopya(Boolean.TRUE);
			setIslemVardiya(vardiyaKopya);
			islemVardiya.setVardiyaTarih(vardiyaDate);
			islemVardiya.setVardiyaZamani(this);
		}
		return islemVardiya;

	}

	@Transient
	public String getBasSaatDakikaStr() {
		StringBuilder aciklama = new StringBuilder(PdksUtil.textBaslangicinaKarakterEkle(String.valueOf(basSaat), '0', 2));
		if (basDakika > 0)
			aciklama.append(":" + PdksUtil.textBaslangicinaKarakterEkle(String.valueOf(basDakika), '0', 2));
		String str = aciklama.toString();
		aciklama = null;
		return str;
	}

	@Transient
	public String getBitSaatDakikaStr() {
		StringBuilder aciklama = new StringBuilder(PdksUtil.textBaslangicinaKarakterEkle(String.valueOf(bitSaat), '0', 2));
		if (bitDakika > 0)
			aciklama.append(":" + PdksUtil.textBaslangicinaKarakterEkle(String.valueOf(bitDakika), '0', 2));
		String str = aciklama.toString();
		aciklama = null;
		return str;
	}

	@Transient
	public String getVardiyaAciklama() {
		String vardiyaAdi = vardiya != null ? vardiya.getVardiyaTipiAciklama() : "";
		if (vardiya != null && vardiya.isCalisma()) {
			setVardiyaZamani();
			if (getIslemVardiya() != null)
				vardiyaAdi = PdksUtil.convertToDateString(islemVardiya.getBasZaman(), PdksUtil.getSaatFormat()) + " - " + PdksUtil.convertToDateString(islemVardiya.getBitZaman(), PdksUtil.getSaatFormat());
		}
		return vardiyaAdi;
	}

	@Transient
	public String getVardiyaZamanAciklama() {
		StringBuilder vardiyaAdi = new StringBuilder();
		try {
			if (vardiya != null && vardiya.isCalisma()) {
				if (ilkGiris == null && sonCikis == null && islemVardiya == null)
					setVardiyaZamani();
				if (!islemVardiya.isFarkliGun()) {
					vardiyaAdi.append(PdksUtil.convertToDateString(islemVardiya.getVardiyaBasZaman(), PdksUtil.getDateFormat()) + " " + islemVardiya.getBasSaatDakikaStr() + "-");
					vardiyaAdi.append(islemVardiya.getBitSaatDakikaStr());
				} else {
					vardiyaAdi.append(PdksUtil.convertToDateString(islemVardiya.getVardiyaBasZaman(), PdksUtil.getDateFormat() + " H:mm") + "-");
					vardiyaAdi.append(PdksUtil.convertToDateString(islemVardiya.getVardiyaBitZaman(), PdksUtil.getDateFormat() + " H:mm"));
				}

			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			vardiyaAdi = null;
			vardiyaAdi = new StringBuilder();
		}

		String str = vardiyaAdi.toString();
		vardiyaAdi = null;
		return str;
	}

	@Transient
	public String getVardiyaZamanAdi() {
		StringBuilder vardiyaAdi = new StringBuilder(PdksUtil.convertToDateString(vardiyaDate, PdksUtil.getDateFormat()));
		String vardiyaZaman = getVardiyaAciklama();
		vardiyaAdi.append(" " + vardiyaZaman);
		String str = vardiyaAdi.toString();
		vardiyaAdi = null;
		return str;
	}

	@Transient
	public String getVardiyaAdi() {
		String vardiyaAdi = null;
		try {
			vardiyaAdi = getVardiyaAciklama();
			if (izin != null && izin.isPlandaGoster())
				vardiyaAdi = izin.getIzinTipi().getIzinTipiTanim().getAciklama();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return vardiyaAdi;
	}

	@Transient
	public ArrayList<Vardiya> getVardiyalar() {
		return vardiyalar;
	}

	public void setVardiyalar(ArrayList<Vardiya> value) {
		if (value != null)
			logger.debug(id);
		this.vardiyalar = value;
	}

	public void setKontrolVardiyalar(ArrayList<Vardiya> value) {
		this.vardiyalar = value;
		if (value != null && vardiya != null && (vardiya.isFMI() || vardiya.getDurum().equals(Boolean.FALSE)) && vardiya.getId() != null) {
			boolean ekle = true;
			Long vId = vardiya.getId();
			for (Vardiya vardiya1 : vardiyalar) {
				if (vardiya1.getId().equals(vId))
					ekle = false;
			}
			if (ekle) {
				ArrayList<Vardiya> yeniVardiyalar = new ArrayList<Vardiya>();
				yeniVardiyalar.addAll(value);
				yeniVardiyalar.add(vardiya);
				this.vardiyalar = yeniVardiyalar;
			}
		}

	}

	@Transient
	public List<Vardiya> getVardiyaList() {
		List<Vardiya> vardiyaList = null;
		if (tatil != null)
			vardiyaList = vardiyalar;
		else {
			vardiyaList = new ArrayList<Vardiya>();
			for (Iterator<Vardiya> iterator = vardiyaList.iterator(); iterator.hasNext();) {
				Vardiya vardiya = iterator.next();

				vardiyaList.add(vardiya);
			}
		}
		return vardiyaList;
	}

	@Transient
	public String getGunClass() {
		String classAdi = "";
		try {
			if (tatil != null)
				classAdi = !tatil.isYarimGunMu() ? "bayram" : "arife";
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			classAdi = "";
		}
		return classAdi;
	}

	@Transient
	public String getGunAdi() {
		String gunAdi = "";
		try {
			if (tatil != null)
				gunAdi = tatil.getAciklama();
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			gunAdi = "";
		}
		return gunAdi;
	}

	@Transient
	public String getVardiyaKeyStr() {

		String key = (personel != null ? personel.getSicilNo() : "") + "_" + getVardiyaDateStr();
		return key;

	}

	@Transient
	public String getVardiyaKey() {
		if (!fiiliHesapla)
			setIzin(null);
		String key = getVardiyaKeyStr();
		return key;

	}

	@Transient
	public String getVardiyaGunAciklama() {
		String aciklama = vardiyaDate != null ? PdksUtil.convertToDateString(vardiyaDate, "d MMMMM EEEEE") : "";
		return aciklama;

	}

	@Transient
	public String getVardiyaStyle() {
		String style = "calismaVar";
		if (vardiya != null) {
			if ((izin != null && izin.isPlandaGoster()) || vardiya.isRadyasyonIzni() || vardiya.isFMI())
				style = "izin";
			else if (!vardiya.isCalisma()) {
				if (vardiya.isHaftaTatil())
					style = "haftaTatil";
				else if (vardiya.isOff())
					style = "off";

			}

		}
		return style;

	}

	@Transient
	public boolean isPazar() {
		boolean pazar = false;
		if (vardiyaDate != null) {
			int gun = getHaftaninGunu();
			pazar = gun == Calendar.SUNDAY;
		}
		return pazar;
	}

	@Transient
	public boolean isHaftaTatil() {
		boolean tatilGunu = false;
		if (vardiyaDate != null && vardiya != null) {
			tatilGunu = vardiya.isHaftaTatil();
			if (!tatilGunu)
				tatilGunu = tatil != null && !tatil.isYarimGunMu();

		}

		return tatilGunu;
	}

	@Transient
	public boolean isTatilGunu() {
		boolean tatilGunu = false;
		if (vardiyaDate != null) {
			tatilGunu = !isHaftaIci();
			if (!tatilGunu)
				tatilGunu = tatil != null && !tatil.isYarimGunMu();

		}

		return tatilGunu;
	}

	@Transient
	public boolean isHaftaIci() {
		boolean haftaIci = false;
		if (vardiyaDate != null) {
			int gun = getHaftaninGunu();
			haftaIci = gun != Calendar.SATURDAY && gun != Calendar.SUNDAY;

		}

		return haftaIci;
	}

	@Transient
	public int getHaftaninGunu() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(PdksUtil.getDate(vardiyaDate));
		int gun = cal.get(Calendar.DAY_OF_WEEK);
		return gun;
	}

	@Transient
	public ArrayList<HareketKGS> getYemekHareketleri() {
		return yemekHareketleri;
	}

	public void setYemekHareketleri(ArrayList<HareketKGS> yemekHareketleri) {
		this.yemekHareketleri = yemekHareketleri;
	}

	@Transient
	public boolean isBitmemisGun() {
		return bitmemisGun;
	}

	public void setBitmemisGun(boolean bitmemisGun) {
		this.bitmemisGun = bitmemisGun;
	}

	@Transient
	public HareketKGS getIlkGiris() {
		return ilkGiris;
	}

	public void setIlkGiris(HareketKGS ilkGiris) {
		this.ilkGiris = ilkGiris;
	}

	@Transient
	public HareketKGS getSonCikis() {
		return sonCikis;
	}

	public void setSonCikis(HareketKGS sonCikis) {
		this.sonCikis = sonCikis;
	}

	@Transient
	public Vardiya getSonrakiVardiya() {
		return sonrakiVardiya;
	}

	public void setSonrakiVardiya(Vardiya sonrakVardiya) {
		this.sonrakiVardiya = sonrakVardiya;
	}

	@Transient
	public Vardiya getYeniVardiya() {
		return yeniVardiya;
	}

	public void setYeniVardiya(Vardiya yeniVardiya) {
		this.yeniVardiya = yeniVardiya;
	}

	@Transient
	public double getResmiTatilSure() {
		return resmiTatilSure;
	}

	public void setResmiTatilSure(double resmiTatilSure) {
		if (resmiTatilSure > 0.0d)
			logger.debug(resmiTatilSure);
		this.resmiTatilSure = resmiTatilSure;
	}

	public void addResmiTatilSure(double value) {
		if (value > 0.0d)
			logger.debug(value);
		this.resmiTatilSure += value;
	}

	@Transient
	public boolean isAylikGirisYap() {
		boolean aylikGirisYap = ayinGunu || donemAcik;
		return aylikGirisYap;
	}

	@Transient
	public boolean isAyinGunu() {
		return ayinGunu;
	}

	@Transient
	public boolean isTatilMesai() {
		double tatilSure = resmiTatilSure + haftaCalismaSuresi;
		return tatilSure > 0.0d;
	}

	@Transient
	public String getStyleClass() {
		String styleClass = "";
		if (tatil != null) {
			styleClass = tatil.isYarimGunMu() ? "Arife" : "Bayram";
		}
		return styleClass;
	}

	public void setAyinGunu(boolean value) {
		this.ayinGunu = value;
	}

	@Transient
	public String getTdClassYaz() {
		String classAdi = tdClass;
		if (vardiya != null && PdksUtil.hasStringValue(vardiya.getStyleClass()))
			classAdi = vardiya.getStyleClass();
		return classAdi;
	}

	@Transient
	public double getFazlaMesaiSure() {
		return fazlaMesaiSure;
	}

	public void setFazlaMesaiSure(double fazlaMesaiSure) {
		this.fazlaMesaiSure = fazlaMesaiSure;
	}

	@Transient
	public VardiyaGorev getVardiyaGorev() {
		return vardiyaGorev;
	}

	public void setVardiyaGorev(VardiyaGorev value) {

		this.vardiyaGorev = value;
	}

	@Transient
	public boolean isCalismayaBaslamadi() {
		boolean iseBaslamadi = PdksUtil.tarihKarsilastirNumeric(personel.getIseGirisTarihi(), vardiyaDate) == 1;
		return iseBaslamadi;
	}

	@Transient
	public String getAylikClassAdi(String anaClasss) {
		String classAd = "";
		Boolean ozelGorevVar = Boolean.FALSE;
		if (vardiyaDateStr.equals("20231212"))
			logger.debug("");
		if (vardiya != null && vardiya.getId() != null) {
			if (izin == null && !vardiya.isRadyasyonIzni()) {
				classAd = STYLE_CLASS_NORMAL_CALISMA;
				if (anaClasss != null && !anaClasss.equals(STYLE_CLASS_ODD))
					classAd = STYLE_CLASS_NORMAL_CALISMA_EVEN;
				if (vardiyaGorev != null && vardiyaGorev.getId() != null && vardiyaGorev.isOzelDurumYok() == false) {
					ozelGorevVar = Boolean.TRUE;
					if (vardiyaGorev.isOzelIstek())
						classAd = STYLE_CLASS_OZEL_ISTEK;
					else if (vardiyaGorev.isIstifa())
						classAd = STYLE_CLASS_OFF;
					else if (vardiyaGorev.isEgitim())
						classAd = STYLE_CLASS_EGITIM;
					else if (vardiyaGorev.isRaporIzni() || vardiyaGorev.isSutIzni())
						classAd = STYLE_CLASS_IZIN;
				} else if (vardiya.isIcapVardiyasi() || vardiya.isIzin() || vardiya.isFMI())
					classAd = STYLE_CLASS_IZIN;
				else if (isTatilGunu() || (tatil != null && !tatil.isYarimGunMu()))
					classAd = STYLE_CLASS_HAFTA_TATIL;

			} else if (izin != null || (vardiya != null && vardiya.isRadyasyonIzni())) {
				classAd = STYLE_CLASS_IZIN;
			}
			if (!ayinGunu && !ozelGorevVar)
				classAd = STYLE_CLASS_DIGER_AY;
		} else {

			if (PdksUtil.hasStringValue(anaClasss) && (personel == null || personel.isCalisiyorGun(vardiyaDate) == false))
				classAd = STYLE_CLASS_OFF;
		}
		if (ayinGunu && version < 0 && this.isIzinli() == false && fiiliHesapla == false)
			classAd = STYLE_CLASS_HATA;
		return classAd;

	}

	// @Transient
	// public String getAylikClassAdi() {
	// String classAd = getAylikClassAdi(null);
	// return classAd;
	// }

	@Transient
	public boolean isCalismayiBirakti() {
		boolean isiBirakti = (!isCalismayaBaslamadi()) && PdksUtil.tarihKarsilastirNumeric(vardiyaDate, personel.getSonCalismaTarihi()) == 1;
		return isiBirakti;
	}

	@Transient
	public boolean isCalisiyor() {
		boolean calisiyor = vardiyaDate != null && personel != null && personel.isCalisiyorGun(vardiyaDate);
		return calisiyor;
	}

	@Transient
	public boolean isKullaniciYetkili() {
		return kullaniciYetkili;
	}

	public void setKullaniciYetkili(boolean kullaniciYetkili) {
		this.kullaniciYetkili = kullaniciYetkili;
	}

	@Transient
	public String getVardiyaPlanAdi() {
		String str = getVardiyaAdi(this.vardiya);
		return str;
	}

	@Transient
	public String getVardiyaAdi(Vardiya vTemp) {
		String str = null;
		if (vTemp == null)
			vTemp = this.vardiya;
		if (vTemp != null) {
			str = vTemp.getKisaAdi();
			if (vTemp.isCalisma()) {
				if (this.vardiyaDate == null)
					this.vardiyaDate = new Date();
				VardiyaGun tmp = new VardiyaGun(this.personel, vTemp, this.vardiyaDate);
				tmp.setVardiyaZamani();
				if (vTemp.isCalisma()) {
					String pattern = PdksUtil.getSaatFormat();
					Vardiya tmpVardiya = tmp.getIslemVardiya();
					str = PdksUtil.convertToDateString(tmpVardiya.getVardiyaBasZaman(), pattern) + " - " + PdksUtil.convertToDateString(tmpVardiya.getVardiyaBitZaman(), pattern) + " [ " + vTemp.getKisaAdi() + " ] ";
					try {
						str += " Net Süre : " + PdksUtil.numericValueFormatStr(tmpVardiya.getNetCalismaSuresi(), null);
					} catch (Exception e) {

					}
				}

				tmp = null;
			} else if (!(vTemp.isOff() || vTemp.isHaftaTatil()))
				str += " - " + vTemp.getAdi();
		}

		return str;
	}

	@Transient
	public boolean isRaporIzni() {
		boolean raporIzni = Boolean.FALSE;
		if (vardiya != null) {
			if (izin != null)
				raporIzni = izin.getIzinTipi().isRaporIzin();
		}

		return raporIzni;
	}

	@Transient
	public boolean isEkleIzni() {
		boolean raporIzni = Boolean.FALSE;
		if (vardiya != null) {
			if (izin != null)
				raporIzni = izin.getIzinTipi().isEkleCGS();
		}

		return raporIzni;
	}

	@Transient
	public boolean isGorevli() {
		boolean gorevli = Boolean.FALSE;
		if (vardiya != null && izin != null)
			gorevli = izin.getIzinTipi().isGorevli();

		return gorevli;
	}

	@Transient
	public boolean isSutIzni() {
		boolean raporIzni = Boolean.FALSE;
		if (vardiya != null) {
			if (izin == null) {
				raporIzni = vardiyaGorev != null && vardiyaGorev.isSutIzni();
			} else
				raporIzni = izin.getIzinTipi().isSutIzin();
		}

		return raporIzni;
	}

	@Transient
	public boolean isFiiliHesapla() {
		return fiiliHesapla;
	}

	public void setFiiliHesapla(boolean fiiliHesapla) {
		this.fiiliHesapla = fiiliHesapla;
	}

	@Transient
	public String getFazlaMesaiTitle() {
		String title = null;
		try {
			title = getTitle();
			double fm = 0.0d;
			if (this.getFazlaMesailer() != null) {
				for (PersonelFazlaMesai personelFazlaMesai : this.getFazlaMesailer()) {
					if (personelFazlaMesai.isOnaylandi()) {
						if (!personelFazlaMesai.isBayram()) {
							fm += personelFazlaMesai.getFazlaMesaiSaati();
						}
					}
				}
			}
			if (title != null && fm > 0.0d)
				title += " FM : " + PdksUtil.numericValueFormatStr(fm, null);
			if (title != null && haftaCalismaSuresi > 0.0d)
				title += " HT : " + PdksUtil.numericValueFormatStr(haftaCalismaSuresi, null);
			if (title != null && resmiTatilSure > 0.0d)
				title += " RT: " + PdksUtil.numericValueFormatStr(resmiTatilSure, null);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
		}

		return title;
	}

	@Transient
	public String getTitle() {
		String title = null;
		if (vardiyaDateStr.equals("20230408"))
			logger.debug("a");
		if (vardiya != null && !(this.getVardiyaGorev() != null && this.getVardiyaGorev().isIstifa())) {
			if (this.getIzin() == null && !vardiya.isRadyasyonIzni()) {
				if (vardiya.isCalisma()) {
					title = this.getVardiyaPlanAdi();
					if (this.getVardiyaGorev() != null) {
						if (this.getVardiyaGorev().getBolumKat() != null)
							title += " ( " + this.getVardiyaGorev().getBolumKat().getBolum().getAciklama() + " )";
						else if (this.getVardiyaGorev().getYeniGorevYeri() != null)
							title += " ( " + this.getVardiyaGorev().getYeniGorevYeri().getAciklama() + " )";
					}

				} else {
					title = this.getVardiya().getVardiyaAciklama();
				}
				if (this.getVardiyaGorev() != null && this.getVardiyaGorev().getId() != null) {
					if (this.getVardiyaGorev().isOzelIstek())
						title += " - Özel İstek ";
					else if (this.getVardiyaGorev().isEgitim())
						title += " - Eğitim ";
					else if (this.getVardiyaGorev().isRaporIzni())
						title += " - Rapor ";
					else if (this.getVardiyaGorev().isSutIzni())
						title += " - Süt İzni ";
					if (this.getVardiyaGorev().isShiftGorevli())
						title += " ( Shift Sorumlusu ) ";
				}

			} else {

				if (!vardiya.isRadyasyonIzni()) {
					title = this.getIzin().getIzinTipi().getIzinTipiTanim().getAciklama();
					try {
						PersonelIzin orjIzin = this.getIzin().getOrjIzin();
						if (vardiyaDate != null && orjIzin != null) {
							String pattern = PdksUtil.getDateFormat() + " H:mm";
							title += " : " + PdksUtil.convertToDateString(orjIzin.getBaslangicZamani(), pattern) + " - " + PdksUtil.convertToDateString(orjIzin.getBitisZamani(), pattern);
						}
					} catch (Exception e) {

					}
					if (this.getIzin() != null && vardiya.isIzin() == false)
						title += " ( " + this.getVardiyaPlanAdi() + " )";
				} else
					title = vardiya.getAciklama();

			}
		}
		return title;
	}

	@Transient
	public String getFazlaMesaiOzelAciklama(boolean durum, String saat) {
		String aciklama = "";
		boolean istifa = vardiya != null && vardiyaGorev != null && vardiyaGorev.isIstifa();
		if (vardiya == null || istifa) {
			if (isAyinGunu() && (istifa || isCalismayiBirakti()))
				aciklama = "ISTIFA";
		} else if (vardiya != null && istifa == false) {
			Double calismaSure = getSaatDouble(saat);
			if (calismaSure != null && calismaSure.doubleValue() != 0.0d)
				aciklama = saat;
			else {
				if (izin != null) {
					if (isPazar())
						aciklama = vardiya.getKisaAdi();
					else
						try {
							aciklama = izin.getIzinTipi().getKisaAciklama();
						} catch (Exception e) {
							logger.error("PDKS hata in : \n");
							e.printStackTrace();
							logger.error("PDKS hata out : " + e.getMessage());
							aciklama = "";
						}

				} else {

					if (isSutIzni())
						aciklama = "Süt İzni";
					else if (isRaporIzni())
						aciklama = "Rapor";
					else if (vardiya.isRadyasyonIzni())
						aciklama = vardiya.getKisaAciklama();

					else if (vardiya.isCalisma()) {
						if (saat != null && saat.length() > 0)
							aciklama = saat;
						else
							aciklama = vardiya.getKisaAciklama();
					} else if (!vardiya.isIzin()) {
						aciklama = getHTveOFFAciklama();

					}

					else
						aciklama = vardiya.getKisaAdi();

					if (vardiyaGorev != null && ((durum && vardiyaGorev.isOzelDurumYok() == false) || vardiyaGorev.getBolumKat() != null || vardiyaGorev.getYeniGorevYeri() != null)) {
						String ekle = (vardiyaGorev.getYeniGorevYeri() != null ? vardiyaGorev.getYeniGorevYeri().getKodu() : "");
						if (vardiyaGorev.getBolumKat() != null)
							ekle = vardiyaGorev.getBolumKat().getKodu();
						if (durum) {
							if (this.getVardiyaGorev().isOzelIstek())
								ekle += (!PdksUtil.hasStringValue(ekle) ? "" : " - ") + "Ö";
							else if (this.getVardiyaGorev().isEgitim())
								ekle += (!PdksUtil.hasStringValue(ekle) ? "" : " - ") + "E";

						}
						aciklama = aciklama + " (" + ekle + " )";
						ekle = null;
					}
				}
			}

		}

		return aciklama;
	}

	private Double getSaatDouble(String saat) {
		Double calismaSure = null;
		try {
			if (saat != null) {
				String saatStr = saat;
				if (saatStr.indexOf(",") > 0)
					saatStr = PdksUtil.replaceAll(saat, ",", ".");
				calismaSure = Double.parseDouble(saatStr);
			}
		} catch (Exception e) {
			calismaSure = null;
		}
		return calismaSure;
	}

	@Transient
	public String getOzelAciklama(boolean durum) {
		String aciklama = "";
		boolean istifa = vardiya != null && vardiyaGorev != null && vardiyaGorev.isIstifa();
		if (vardiyaDateStr.equals("20230904"))
			logger.debug("");
		if (vardiya == null || istifa) {
			if (isAyinGunu() && (istifa || isCalismayiBirakti()))
				aciklama = "ISTIFA";
		} else if (vardiya != null && istifa == false) {
			if (izin != null) {
				IzinTipi izinTipi = izin.getIzinTipi();
				boolean haftaTatilDurum = false;
				if (izinHaftaTatilDurum != null) {
					if (izinHaftaTatilDurum)
						haftaTatilDurum = true;
					else if (izinTipi.isTakvimGunuMu() == false && izinTipi.isHTDahil() == false)
						haftaTatilDurum = true;
				}
				if (haftaTatilDurum) {
					aciklama = ".";
				} else
					try {
						aciklama = izinTipi.getKisaAciklama();
					} catch (Exception e) {
						logger.error("PDKS hata in : \n");
						e.printStackTrace();
						logger.error("PDKS hata out : " + e.getMessage());
						aciklama = "";
					}

			} else {
				if (isSutIzni())
					aciklama = "Süt İzni";
				else if (isRaporIzni())
					aciklama = "Rapor";
				else if (vardiya.isRadyasyonIzni())
					aciklama = vardiya.getKisaAciklama();

				else if (vardiya.isCalisma()) {
					aciklama = vardiya.getKisaAciklama();
				} else if (!vardiya.isIzin()) {
					aciklama = getHTveOFFAciklama();
				} else
					aciklama = vardiya.getKisaAdi();

				if (vardiyaGorev != null) {
					if ((durum && vardiyaGorev.isOzelDurumYok() == false) || vardiyaGorev.getBolumKat() != null || vardiyaGorev.getYeniGorevYeri() != null) {
						String ekle = (vardiyaGorev.getYeniGorevYeri() != null ? vardiyaGorev.getYeniGorevYeri().getKodu() : "");
						if (vardiyaGorev.getBolumKat() != null)
							ekle = vardiyaGorev.getBolumKat().getKodu();
						if (durum) {
							if (this.getVardiyaGorev().isOzelIstek())
								ekle += (!PdksUtil.hasStringValue(ekle) ? "" : " - ") + "Ö";
							else if (this.getVardiyaGorev().isEgitim())
								ekle += (!PdksUtil.hasStringValue(ekle) ? "" : " - ") + "E";

						}
						if (vardiyaGorev.isShiftGorevli())
							ekle += " * ";
						aciklama = aciklama + " (" + ekle + " )";

						ekle = null;
					} else if (vardiyaGorev.isShiftGorevli())
						aciklama = aciklama + " ( * )";
				}
			}
		}

		return aciklama;
	}

	/**
	 * @return
	 */
	@Transient
	private String getHTveOFFAciklama() {
		String aciklama = "";
		if (vardiya != null) {
			aciklama = ".";
			if (haftaTatilDurum || vardiya.isFMI()) {
				if (!vardiya.isOff())
					aciklama = vardiya.getKisaAdi();
			}
		}
		return aciklama;
	}

	@Transient
	public String getAciklama() {
		String aciklama = getOzelAciklama(false);
		return aciklama;
	}

	@Transient
	public boolean isHataliDurum() {
		return hataliDurum;
	}

	public void setHataliDurum(boolean hataliDurum) {
		this.hataliDurum = hataliDurum;
	}

	@Transient
	public List<String> getLinkAdresler() {

		return linkAdresler;
	}

	@Transient
	public void addLinkAdresler(String value) {
		if (PdksUtil.hasStringValue(value)) {
			if (linkAdresler == null)
				linkAdresler = new ArrayList<String>();
			if (!linkAdresler.contains(value))
				linkAdresler.add(value);

		}
	}

	public void setLinkAdresler(List<String> linkAdresler) {
		this.linkAdresler = linkAdresler;

	}

	@Transient
	public String getLinkAdresHtml() {
		String adres = null;
		if (linkAdresler != null) {
			StringBuffer sb = new StringBuffer();
			if (linkAdresler.size() > 1)
				sb.append("<B>Uyarılar</B></br>");
			for (String string : linkAdresler)
				if (string != null)
					sb.append("</br>" + string);
			adres = sb.toString();
			sb = null;
		}

		return adres;
	}

	@Transient
	public boolean isZamanGuncelle() {
		return zamanGuncelle;
	}

	public void setZamanGuncelle(boolean zamanGuncelle) {
		this.zamanGuncelle = zamanGuncelle;
	}

	@Transient
	public boolean isDonemAcik() {
		return donemAcik;
	}

	public void setDonemAcik(boolean donemAcik) {
		this.donemAcik = donemAcik;
	}

	@Transient
	private boolean helpPersonel(Personel personel) {
		return personel != null && gorevliPersonelMap != null && gorevliPersonelMap.containsKey(personel.getPdksSicilNo());

	}

	@Transient
	public boolean isCalisan() {
		boolean calisan = false;
		if (this.getVardiya() != null) {
			calisan = this.isKullaniciYetkili() || (this.getIzin() != null && !helpPersonel(this.getPersonel()));
		}
		return calisan;
	}

	@Transient
	public HashMap<String, Personel> getGorevliPersonelMap() {
		return gorevliPersonelMap;
	}

	public void setGorevliPersonelMap(HashMap<String, Personel> gorevliPersonelMap) {
		this.gorevliPersonelMap = gorevliPersonelMap;
	}

	@Transient
	public double getGecenAyResmiTatilSure() {
		return gecenAyResmiTatilSure;
	}

	public void setGecenAyResmiTatilSure(double value) {
		this.gecenAyResmiTatilSure = value;
	}

	@Transient
	public boolean isOnayli() {
		return onayli;
	}

	public void setOnayli(boolean onayli) {
		this.onayli = onayli;
	}

	@Transient
	public String getPlanKey() {
		String key = (personel != null ? "perId=" + personel.getId() : "");
		if (vardiyaDate != null) {
			if (personel != null)
				key += "&";
			key += "tarih=" + PdksUtil.convertToDateString(vardiyaDate, "yyyyMMdd");
		}
		String planKey = PdksUtil.getEncodeStringByBase64(key);
		return planKey;
	}

	@Transient
	public void hareketIcindekiIzinlereHareketEkle() {
		String pattern = PdksUtil.getDateFormat() + " H:mm";
		boolean güncelle = Boolean.FALSE;
		ArrayList<HareketKGS> yeniHareketler = new ArrayList<HareketKGS>(), yeniGirisHareketler = new ArrayList<HareketKGS>(), yeniCikisHareketler = new ArrayList<HareketKGS>();
		for (int i = 0; i < this.getGirisHareketleri().size(); i++) {
			HareketKGS orjHareketGiris = this.getGirisHareketleri().get(i), orjHareketCikis = this.getCikisHareketleri().get(i);
			for (PersonelIzin personelIzin : this.getIzinler()) {
				yeniHareketler.add(orjHareketGiris);
				yeniGirisHareketler.add(orjHareketGiris);
				if (personelIzin.getBaslangicZamani().getTime() >= orjHareketGiris.getZaman().getTime() && personelIzin.getBitisZamani().getTime() <= orjHareketCikis.getZaman().getTime()) {
					HareketKGS yeniHareketGiris = (HareketKGS) orjHareketGiris.clone(), yeniHareketCikis = (HareketKGS) orjHareketCikis.clone();
					yeniHareketGiris.setId(null);
					yeniHareketGiris.setZaman(personelIzin.getBitisZamani());
					yeniHareketCikis.setId(null);
					yeniHareketCikis.setZaman(personelIzin.getBaslangicZamani());
					yeniCikisHareketler.add(yeniHareketCikis);
					yeniGirisHareketler.add(yeniHareketGiris);
					yeniHareketler.add(yeniHareketCikis);
					yeniHareketler.add(yeniHareketGiris);
					güncelle = Boolean.TRUE;
					PdksUtil.addMessageWarn(personelIzin.getIzinSahibi().getPdksSicilNo() + " " + personelIzin.getIzinSahibi().getAdSoyad() + " " + personelIzin.getIzinTipi().getIzinTipiTanim().getAciklama() + " için " + PdksUtil.convertToDateString(personelIzin.getBaslangicZamani(), pattern)
							+ " - " + PdksUtil.convertToDateString(personelIzin.getBitisZamani(), pattern) + " çıkış ve giriş kayıtları girilmelidir!");
				}
				yeniHareketler.add(orjHareketCikis);
				yeniCikisHareketler.add(orjHareketCikis);
			}
		}
		if (!güncelle) {
			yeniHareketler = null;
			yeniGirisHareketler = null;
			yeniCikisHareketler = null;
		} else {
			this.setHareketler(yeniHareketler);
			this.setGirisHareketleri(yeniGirisHareketler);
			this.setCikisHareketleri(yeniCikisHareketler);
		}
	}

	@Transient
	public boolean isZamanGelmedi() {
		return zamanGelmedi;
	}

	public void setZamanGelmedi(boolean value) {
		if (value)
			logger.debug(this.getVardiyaKeyStr());
		this.zamanGelmedi = value;
	}

	@Transient
	public boolean isIslendi() {
		return islendi;
	}

	public void setIslendi(boolean islendi) {
		this.islendi = islendi;
	}

	public void addGecenAyResmiTatilSure(double value) {
		this.gecenAyResmiTatilSure += value;
	}

	@Transient
	public double getBayramCalismaSuresi() {
		return bayramCalismaSuresi;
	}

	public void setBayramCalismaSuresi(double value) {
		if (value > 0.0d)
			logger.debug(value);
		this.bayramCalismaSuresi = value;
	}

	@Transient
	public void addBayramCalismaSuresi(double value) {
		if (value != 0.0d)
			logger.debug(value);
		bayramCalismaSuresi += value;
	}

	@Transient
	public double getCalisilmayanAksamSure() {
		return calisilmayanAksamSure;
	}

	public void setCalisilmayanAksamSure(double calisilmayanAksamSure) {
		this.calisilmayanAksamSure = calisilmayanAksamSure;
	}

	@Transient
	public String getSortKey() {
		String sortKey = this.getPersonel().getSirket().getAd() + (this.getPersonel().getTesis() != null ? "_" + this.getPersonel().getTesis().getAciklama() : "") + "_" + this.getPersonel().getAdSoyad() + "_" + this.getVardiyaKeyStr();
		return sortKey;
	}

	@Transient
	public String getSortBolumKey() {
		Personel yonetici = this.getPersonel().getPdksYonetici();
		Sirket sirket = this.getPersonel().getSirket();
		Long departmanId = null, sirketId = null;
		String sirketIdStr = null;
		if (sirket != null) {
			Departman departman = sirket.getDepartman();
			departmanId = departman != null ? departman.getId() : null;
			if (sirket.getSirketGrup() != null)
				sirketId = -sirket.getSirketGrup().getId();
			else
				sirketId = sirket.getId();
		}
		if (departmanId == null)
			departmanId = 0L;
		if (sirketId != null)
			sirketIdStr = sirketId > 0L ? "S" + sirketId : "G" + (-sirketId);
		if (sirketIdStr == null)
			sirketIdStr = "";
		Tanim bolum = this.getPersonel().getEkSaha3(), altBolum = this.getPersonel().getEkSaha4();
		CalismaModeli calismaModeli = this.getPersonel().getCalismaModeli();
		String sortKey = departmanId + "_" + sirketIdStr + "_" + (yonetici != null ? "_" + yonetici.getAdSoyad() : "") + "_" + (bolum != null ? "_" + bolum.getAciklama() : "") + "_" + (calismaModeli != null ? "_" + calismaModeli.getAciklama() : "");
		sortKey += "_" + (altBolum != null ? "_" + altBolum.getAciklama() : "") + "_" + this.getPersonel().getAdSoyad() + "_" + this.getVardiyaKeyStr();
		return sortKey;
	}

	@Transient
	public double getAksamVardiyaSaatSayisi() {
		return aksamVardiyaSaatSayisi;
	}

	public void setAksamVardiyaSaatSayisi(double value) {
		if (value > 0) {
			logger.debug(value);
		}
		this.aksamVardiyaSaatSayisi = value;
	}

	public void addAksamVardiyaSaatSayisi(double vale) {
		if (vale > 0.0d)
			this.aksamVardiyaSaatSayisi += vale;
	}

	@Transient
	public List<FazlaMesaiTalep> getFazlaMesaiTalepler() {
		return fazlaMesaiTalepler;
	}

	public void setFazlaMesaiTalepler(List<FazlaMesaiTalep> fazlaMesaiTalepler) {
		this.fazlaMesaiTalepler = fazlaMesaiTalepler;
	}

	@Transient
	public void saklaVardiya() {
		Vardiya vardiyaKopya = null;
		if (vardiya != null) {
			vardiyaKopya = (Vardiya) vardiya.clone();
			vardiyaKopya.setKopya(Boolean.TRUE);
		}

		this.eskiVardiya = vardiyaKopya;
	}

	@Transient
	public Vardiya getEskiVardiya() {
		return eskiVardiya;
	}

	public void setEskiVardiya(Vardiya eskiVardiya) {
		this.eskiVardiya = eskiVardiya;
	}

	public static boolean isHaftaTatilDurum() {
		return haftaTatilDurum;
	}

	public static void setHaftaTatilDurum(boolean haftaTatilDurum) {
		VardiyaGun.haftaTatilDurum = haftaTatilDurum;
	}

	@Transient
	public Boolean getFazlaMesaiOnayla() {
		return fazlaMesaiOnayla;
	}

	public void setFazlaMesaiOnayla(Boolean value) {
		if (vardiyaDateStr.endsWith("01"))
			logger.debug(vardiyaDateStr + " " + value);
		this.fazlaMesaiOnayla = value;
	}

	@Transient
	public boolean isAyrikHareketVar() {
		return ayrikHareketVar;
	}

	public void setAyrikHareketVar(boolean ayrikHareketVar) {
		this.ayrikHareketVar = ayrikHareketVar;
	}

	@Transient
	public CalismaModeli getCalismaModeli() {
		return calismaModeli;
	}

	public void setCalismaModeli(CalismaModeli calismaModeli) {
		this.calismaModeli = calismaModeli;
	}

	@Transient
	public VardiyaSaat getVardiyaSaatDB() {
		return vardiyaSaatDB;
	}

	public void setVardiyaSaatDB(VardiyaSaat vardiyaSaatDB) {
		this.vardiyaSaatDB = vardiyaSaatDB;
	}

	@Transient
	public VardiyaGun getOncekiVardiyaGun() {
		return oncekiVardiyaGun;
	}

	public void setOncekiVardiyaGun(VardiyaGun oncekiVardiyaGun) {
		this.oncekiVardiyaGun = oncekiVardiyaGun;
	}

	@Transient
	public Vardiya getOncekiVardiya() {
		return oncekiVardiya;
	}

	public void setOncekiVardiya(Vardiya oncekiVardiya) {
		this.oncekiVardiya = oncekiVardiya;
	}

	@Transient
	public VardiyaGun getSonrakiVardiyaGun() {
		return sonrakiVardiyaGun;
	}

	public void setSonrakiVardiyaGun(VardiyaGun sonrakiVardiyaGun) {
		this.sonrakiVardiyaGun = sonrakiVardiyaGun;
	}

	@Transient
	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	@Transient
	public boolean isFazlaMesaiTalepOnayliDurum() {
		return fazlaMesaiTalepOnayliDurum;
	}

	public void setFazlaMesaiTalepOnayliDurum(boolean value) {
		if (vardiyaDateStr.endsWith("01"))
			logger.debug("" + value);
		this.fazlaMesaiTalepOnayliDurum = value;
	}

	@Transient
	public int getBeklemeSuresi() {
		return beklemeSuresi;
	}

	public void setBeklemeSuresi(int beklemeSuresi) {
		this.beklemeSuresi = beklemeSuresi;
	}

	@Transient
	public IsKurVardiyaGun getIsKurVardiya() {
		return isKurVardiya;
	}

	public void setIsKurVardiya(IsKurVardiyaGun isKurVardiya) {
		this.isKurVardiya = isKurVardiya;
	}

	@Transient
	public Double getCalismaSuaSaati() {
		return calismaSuaSaati;
	}

	public void setCalismaSuaSaati(Double calismaSuaSaati) {
		this.calismaSuaSaati = calismaSuaSaati;
	}

	@Transient
	public String getManuelGirisHTML() {
		return manuelGirisHTML;
	}

	public void setManuelGirisHTML(String manuelGirisHTML) {
		this.manuelGirisHTML = manuelGirisHTML;
	}

	@Transient
	public int getYarimYuvarla() {
		return yarimYuvarla;
	}

	public void setYarimYuvarla(int yarimYuvarla) {
		this.yarimYuvarla = yarimYuvarla;
	}

	@Transient
	public Integer getOffFazlaMesaiBasDakika() {
		return offFazlaMesaiBasDakika;
	}

	public void setOffFazlaMesaiBasDakika(Integer offFazlaMesaiBasDakika) {
		this.offFazlaMesaiBasDakika = offFazlaMesaiBasDakika;
	}

	@Transient
	public Integer getHaftaTatiliFazlaMesaiBasDakika() {
		return haftaTatiliFazlaMesaiBasDakika;
	}

	public void setHaftaTatiliFazlaMesaiBasDakika(Integer haftaTatiliFazlaMesaiBasDakika) {
		this.haftaTatiliFazlaMesaiBasDakika = haftaTatiliFazlaMesaiBasDakika;
	}

	@Transient
	public String getStyleGun() {
		String style = "";
		if (vardiya != null && vardiya.getId() != null) {
			if (ayinGunu)
				style = ";font-weight: bold;";
			else {
				style = ";color:red;";
				if (donemStr != null && vardiyaDateStr != null) {
					if (vardiyaDateStr.compareTo(donemStr + "01") == 1) {
						style = ";color:orange;";
					}
				}
			}

		}

		return style;
	}

	@Transient
	public BigDecimal getKatSayi(Integer tipi) {
		BigDecimal katSayi = null;
		try {
			if (tipi != null && katSayiMap != null) {
				if (katSayiMap.containsKey(tipi))
					katSayi = katSayiMap.get(tipi);
			}
		} catch (Exception e) {
			katSayi = null;
		}
		return katSayi;
	}

	@Transient
	public HashMap<Integer, BigDecimal> getKatSayiMap() {
		return katSayiMap;
	}

	public void setKatSayiMap(HashMap<Integer, BigDecimal> katSayiMap) {
		this.katSayiMap = katSayiMap;
	}

	@Transient
	public String getDonemStr() {
		return donemStr;
	}

	public void setDonemStr(String donemStr) {
		this.donemStr = donemStr;
	}

	// SAAT_CALISAN_NORMAL_GUN(91), SAAT_CALISAN_IZIN_GUN(92), SAAT_CALISAN_HAFTA_TATIL(93), SAAT_CALISAN_RESMI_TATIL(
	@Transient
	public double getSaatCalisanNormalGunKatsayisi() {
		double katSayi = 0.0d;
		if (ayinGunu) {
			BigDecimal decimal = getKatSayi(KatSayiTipi.SAAT_CALISAN_NORMAL_GUN.value());
			katSayi = decimal != null ? decimal.doubleValue() : 9.0d;
		}
		return katSayi;
	}

	@Transient
	public double getSaatCalisanIzinGunKatsayisi() {
		double katSayi = 0.0d;
		if (ayinGunu) {
			BigDecimal decimal = getKatSayi(KatSayiTipi.SAAT_CALISAN_IZIN_GUN.value());

			katSayi = decimal != null ? decimal.doubleValue() : 9.0d;
		}
		return katSayi;
	}

	@Transient
	public double getSaatCalisanHaftaTatilKatsayisi() {
		double katSayi = 0.0d;
		if (ayinGunu) {
			BigDecimal decimal = getKatSayi(KatSayiTipi.SAAT_CALISAN_HAFTA_TATIL.value());
			katSayi = decimal != null ? decimal.doubleValue() : 7.5d;
		}
		return katSayi;
	}

	@Transient
	public double getSaatCalisanResmiTatilKatsayisi() {
		double katSayi = 0.0d;
		if (ayinGunu) {
			BigDecimal decimal = getKatSayi(KatSayiTipi.SAAT_CALISAN_RESMI_TATIL.value());
			katSayi = decimal != null ? decimal.doubleValue() : 7.5d;
		}
		return katSayi;
	}

	@Transient
	public double getSaatCalisanGunlukKatsayisi() {
		double katSayi = 0.0d;
		if (ayinGunu) {
			BigDecimal decimal = getKatSayi(KatSayiTipi.SAAT_CALISAN_GUN.value());
			katSayi = decimal != null ? decimal.doubleValue() : 7.5d;
		}
		return katSayi;
	}

	@Transient
	public double getSaatCalisanArifeNormalKatsayisi() {
		double katSayi = 0.0d;
		if (ayinGunu) {
			BigDecimal decimal = getKatSayi(KatSayiTipi.SAAT_CALISAN_ARIFE_NORMAL_SAAT.value());
			katSayi = decimal != null ? decimal.doubleValue() : 3.75d;
		}
		return katSayi;
	}

	@Transient
	public double getSaatCalisanArifeTatilKatsayisi() {
		double katSayi = 0.0d;
		if (ayinGunu) {
			BigDecimal decimal = getKatSayi(KatSayiTipi.SAAT_CALISAN_ARIFE_TATIL_SAAT.value());
			katSayi = decimal != null ? decimal.doubleValue() : 3.75d;
		}
		return katSayi;
	}

	@Transient
	public boolean isFazlaMesaiTalepDurum() {
		if (!fazlaMesaiTalepDurum && izin == null && ayinGunu) {
			BigDecimal decimal = getKatSayi(KatSayiTipi.FMT_DURUM.value());
			fazlaMesaiTalepDurum = decimal != null && decimal.intValue() > 0;
		}
		return fazlaMesaiTalepDurum;
	}

	public void setFazlaMesaiTalepDurum(boolean fazlaMesaiTalepDurum) {
		this.fazlaMesaiTalepDurum = fazlaMesaiTalepDurum;
	}

	@Transient
	public Personel getPdksPersonel() {
		return personel;
	}

	@Transient
	public Boolean getIzinHaftaTatilDurum() {
		return izinHaftaTatilDurum;
	}

	public void setIzinHaftaTatilDurum(Boolean izinHaftaTatilDurum) {
		this.izinHaftaTatilDurum = izinHaftaTatilDurum;
	}

	@Transient
	public boolean isYemekHesabiSureEkle() {
		BigDecimal decimal = getKatSayi(KatSayiTipi.YEMEK_SURE_EKLE_DURUM.value());
		boolean tatilYemekHesabiSureEkle = decimal != null && decimal.intValue() > 0;
		return tatilYemekHesabiSureEkle;
	}

	@Transient
	public List<YemekIzin> getYemekList() {
		if (yemekList == null)
			yemekList = new ArrayList<YemekIzin>();
		return yemekList;
	}

	public void setYemekList(List<YemekIzin> yemekList) {
		this.yemekList = yemekList;
	}

	@Transient
	public boolean isAyarlamaBitti() {
		return ayarlamaBitti;
	}

	public void setAyarlamaBitti(boolean ayarlamaBitti) {
		this.ayarlamaBitti = ayarlamaBitti;
	}

	@Transient
	public double getAksamKatSayisi() {
		return aksamKatSayisi;
	}

	public void setAksamKatSayisi(double value) {
		if (value != 0.0d)
			logger.debug(value);
		this.aksamKatSayisi = value;
	}

	@Transient
	public double getYasalMaxSure() {
		return yasalMaxSure;
	}

	public void setYasalMaxSure(double yasalMaxSure) {
		this.yasalMaxSure = yasalMaxSure;
	}

	@Transient
	public boolean isPlanHareketEkle() {
		return planHareketEkle;
	}

	public void setPlanHareketEkle(boolean planHareketEkle) {
		this.planHareketEkle = planHareketEkle;
	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}

}
