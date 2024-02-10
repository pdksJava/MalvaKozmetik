package org.pdks.entity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import org.pdks.genel.model.PdksUtil;

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
	private Vardiya vardiya, islemVardiya, sonrakiVardiya, yeniVardiya, eskiVardiya;
	private Date vardiyaDate;
	private ArrayList<PersonelIzin> izinler;
	private ArrayList<Vardiya> vardiyalar;
	private VardiyaGun oncekiVardiya;

	private boolean hareketHatali = Boolean.FALSE, kullaniciYetkili = Boolean.TRUE, zamanGuncelle = Boolean.TRUE, zamanGelmedi = Boolean.FALSE;
	private double normalSure = 0, resmiTatilSure = 0, gecenAyResmiTatilSure = 0, aksamVardiyaSaatSayisi = 0d, calisilmayanAksamSure = 0, fazlaMesaiSure = 0, bayramCalismaSuresi = 0;
	private Integer basSaat, basDakika, bitSaat, bitDakika;
	private String tdClass = "";
	private PersonelIzin izin;
	private VardiyaSablonu vardiyaSablonu;
	private boolean bitmemisGun = Boolean.TRUE, islendi = Boolean.FALSE;
	private boolean ayinGunu = Boolean.TRUE, onayli = Boolean.TRUE, guncellendi = Boolean.FALSE, fiiliHesapla = Boolean.FALSE, hataliDurum = Boolean.FALSE, donemAcik = Boolean.TRUE;
	private List<String> linkAdresler;
	private HashMap<String, Personel> gorevliPersonelMap;
	private Integer version = 0;

	public VardiyaGun() {
		super();

	}

	public VardiyaGun(Personel personel, Vardiya vardiya, Date vardiyaDate) {
		super();
		this.personel = personel;
		this.vardiya = vardiya;
		this.vardiyaDate = vardiyaDate;
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
		if (personel != null)
			setVardiyaSablonu(personel.getSablon());
		this.personel = personel;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_VARDIYA, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Vardiya getVardiya() {
		return vardiya;
	}

	public void setVardiya(Vardiya value) {
		this.vardiya = value;
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
		return vardiyaDate != null ? PdksUtil.convertToDateString(vardiyaDate, "yyyyMMdd") : "";

	}

	public void setVardiyaDate(Date vardiyaDate1) {
		boolean gunDurum = Boolean.TRUE;
		if (vardiyaDate1 != null)
			gunDurum = PdksUtil.tarihKarsilastirNumeric(Calendar.getInstance().getTime(), vardiyaDate1) != 1;
		setBitmemisGun(gunDurum);
		this.vardiyaDate = vardiyaDate1;
	}

	@Transient
	public String getTarihStr() {
		return vardiyaDate != null ? PdksUtil.convertToDateString(vardiyaDate, "yyyyMMdd") : "";
	}

	@Transient
	public Vardiya getIslemVardiya() {
		if (vardiyaDate != null && vardiya != null) {
			if (islemVardiya == null) {
				setVardiyaZamani();
				if (islemVardiya == null) {
					setIslemVardiya((Vardiya) vardiya.clone());
					if (islemVardiya != null) {
						if (oncekiVardiya != null && oncekiVardiya.getIslemVardiya() != null)
							islemVardiya.setOncekiVardiya(oncekiVardiya.getIslemVardiya());
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
	public Date getVardiyaFazlaMesaiBasZaman() {
		if (islemVardiya == null)
			setVardiyaZamani();
		return islemVardiya != null ? islemVardiya.getVardiyaFazlaMesaiBasZaman() : null;
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
	public PersonelIzin getIzin() {
		return izin;
	}

	public void setIzin(PersonelIzin value) {
		if (value != null) {
			logger.debug(value.getAciklama());
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
				if ((sonrakiVardiya == null && oncekiVardiya == null) || islemVardiya == null)
					setIslemVardiya((Vardiya) vardiya.clone());
				if (islemVardiya != null) {
					if (oncekiVardiya != null && oncekiVardiya.getIslemVardiya() != null)
						islemVardiya.setOncekiVardiya(oncekiVardiya.getIslemVardiya());
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
			setIslemVardiya((Vardiya) vardiya.clone());
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
				vardiyaAdi = islemVardiya.getBasSaatDakikaStr() + "-" + islemVardiya.getBitSaatDakikaStr();
		}
		return vardiyaAdi;
	}

	@Transient
	public String getVardiyaZamanAdi() {
		StringBuilder vardiyaAdi = new StringBuilder(PdksUtil.convertToDateString(vardiyaDate, "dd/MM/yyyy"));
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

	public void setVardiyalar(ArrayList<Vardiya> vardiyalar) {
		this.vardiyalar = vardiyalar;
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
			if ((izin != null && izin.isPlandaGoster()) || vardiya.isRadyasyonIzni())
				style = "izin";
			else if (!vardiya.isCalisma()) {
				if (vardiya.isHaftaTatil())
					style = "haftaTatil";
				else if (vardiya.isOffGun())
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
	private int getHaftaninGunu() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(PdksUtil.getDate(vardiyaDate));
		int gun = cal.get(Calendar.DAY_OF_WEEK);
		return gun;
	}

	@Transient
	public boolean isBitmemisGun() {
		return bitmemisGun;
	}

	public void setBitmemisGun(boolean bitmemisGun) {
		this.bitmemisGun = bitmemisGun;
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
			logger.info(value);
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

	public void setAyinGunu(boolean ayinGunu) {
		this.ayinGunu = ayinGunu;
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
	public boolean isGuncellendi() {
		return guncellendi;
	}

	public void setGuncellendi(boolean guncellendi) {
		this.guncellendi = guncellendi;
	}

	@Transient
	public boolean isCalismayaBaslamadi() {
		boolean iseBaslamadi = PdksUtil.tarihKarsilastirNumeric(personel.getIseBaslamaTarihi(), vardiyaDate) == 1;
		return iseBaslamadi;
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
					String pattern = "H:mm";
					Vardiya tmpVardiya = tmp.getIslemVardiya();
					str = PdksUtil.convertToDateString(tmpVardiya.getVardiyaBasZaman(), pattern) + " - " + PdksUtil.convertToDateString(tmpVardiya.getVardiyaBitZaman(), pattern) + " [ " + vTemp.getKisaAdi() + " ] ";
				}

				tmp = null;
			}
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
	public boolean isFiiliHesapla() {
		return fiiliHesapla;
	}

	public void setFiiliHesapla(boolean fiiliHesapla) {
		this.fiiliHesapla = fiiliHesapla;
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
	public VardiyaGun getOncekiVardiya() {
		return oncekiVardiya;
	}

	public void setOncekiVardiya(VardiyaGun oncekiVardiya) {
		this.oncekiVardiya = oncekiVardiya;
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
		this.bayramCalismaSuresi = value;
	}

	@Transient
	public void addBayramCalismaSuresi(double value) {
		if (value > 0.0d)
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
	public double getAksamVardiyaSaatSayisi() {
		return aksamVardiyaSaatSayisi;
	}

	public void setAksamVardiyaSaatSayisi(double aksamVardiyaSaatSayisi) {
		this.aksamVardiyaSaatSayisi = aksamVardiyaSaatSayisi;
	}

	public void addAksamVardiyaSaatSayisi(double vale) {
		if (vale > 0.0d)
			this.aksamVardiyaSaatSayisi += vale;
	}

	@Transient
	public void saklaVardiya() {
		this.eskiVardiya = vardiya != null ? (Vardiya) vardiya.clone() : null;
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
}
