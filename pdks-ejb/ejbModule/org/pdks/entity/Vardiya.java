package org.pdks.entity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.Min;
import org.pdks.security.entity.User;
import org.pdks.session.PdksUtil;

@Entity(name = Vardiya.TABLE_NAME)
public class Vardiya extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1015477779883195923L;
	static Logger logger = Logger.getLogger(Vardiya.class);
	public static final String TABLE_NAME = "VARDIYA";
	public static final String COLUMN_NAME_VARDIYA_TIPI = "VARDIYATIPI";
	public static final String COLUMN_NAME_SUA = "SUA";
	public static final String COLUMN_NAME_ISKUR = "ISKUR";
	public static final String COLUMN_NAME_GEBELIK = "GEBELIK";
	public static final String COLUMN_NAME_ICAP = "ICAP_VARDIYA";
	public static final String COLUMN_NAME_KISA_ADI = "KISA_ADI";
	public static final String COLUMN_NAME_GENEL = "GENEL";
	public static final String COLUMN_NAME_DEPARTMAN = "DEPARTMAN_ID";
	public static final String COLUMN_NAME_EKRAN_SIRA = "EKRAN_SIRA";

	public static final char TIPI_CALISMA = ' ';
	public static final char TIPI_HAFTA_TATIL = 'H';
	public static final char TIPI_OFF = 'O';
	public static final char TIPI_FMI = 'F';
	public static final char TIPI_RADYASYON_IZNI = 'R';
	public static final char TIPI_IZIN = 'I';
	public static final char TIPI_HASTALIK_RAPOR = 'S';

	public static Date vardiyaKontrolTarih, vardiyaKontrolTarih2, vardiyaKontrolTarih3, vardiyaAySonuKontrolTarih;

	private static Integer fazlaMesaiBasSaati = 2, offFazlaMesaiBasDakika = -60, haftaTatiliFazlaMesaiBasDakika = -60;
	private String adi, kisaAdi, styleClass, vardiyaDateStr;
	private short basDakika, basSaat, bitDakika, bitSaat, girisErkenToleransDakika, girisGecikmeToleransDakika, cikisErkenToleransDakika, cikisGecikmeToleransDakika;
	private double calismaSaati;
	private Double arifeNormalCalismaDakika, arifeCalismaSure;
	private int calismaGun, islemAdet = 0;
	private Integer ekranSira = 5000, fazlaMesaiBasDakika;
	private Integer yemekSuresi, cikisMolaSaat = 0;
	private Departman departman;
	private List<Integer> gunlukList;
	private Boolean aksamVardiya = Boolean.FALSE, icapVardiya = Boolean.FALSE, gebelik = Boolean.FALSE, genel = Boolean.FALSE, isKur = Boolean.FALSE;
	private String tipi;

	private char vardiyaTipi;
	private Date vardiyaBasZaman, vardiyaBitZaman, vardiyaTarih, arifeBaslangicTarihi;
	private Date vardiyaTelorans1BasZaman, vardiyaTelorans2BasZaman, vardiyaTelorans1BitZaman, vardiyaTelorans2BitZaman;
	private Date vardiyaFazlaMesaiBasZaman, vardiyaFazlaMesaiBitZaman;
	private boolean farkliGun = Boolean.FALSE, ayinSonGunDurum = Boolean.FALSE;
	private Boolean mesaiOde, sua = Boolean.FALSE;
	private Vardiya sonrakiVardiya, oncekiVardiya;
	private CalismaSekli calismaSekli;
	private Integer version = 0;
	private List<YemekIzin> yemekIzinList;

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Column(name = "ADI")
	public String getAdi() {
		return adi;
	}

	public void setAdi(String adi) {
		adi = PdksUtil.convertUTF8(adi);
		this.adi = adi;
	}

	@Column(name = "MESAI_ODE")
	public Boolean getMesaiOde() {
		return mesaiOde;
	}

	public void setMesaiOde(Boolean mesaiOde) {
		this.mesaiOde = mesaiOde;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "CALISMA_SEKLI")
	@Fetch(FetchMode.JOIN)
	public CalismaSekli getCalismaSekli() {
		return calismaSekli;
	}

	public void setCalismaSekli(CalismaSekli calismaSekli) {
		this.calismaSekli = calismaSekli;
	}

	@Column(name = COLUMN_NAME_KISA_ADI, length = 16)
	public String getKisaAdi() {
		return kisaAdi;
	}

	public void setKisaAdi(String kisaAdi) {
		this.kisaAdi = kisaAdi;
	}

	@Column(name = "CLASS_ADI", length = 8)
	public String getStyleClass() {
		return styleClass;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	@Column(name = COLUMN_NAME_ISKUR)
	public Boolean getIsKur() {
		return isKur;
	}

	public void setIsKur(Boolean isKur) {
		this.isKur = isKur;
	}

	@Column(name = COLUMN_NAME_GEBELIK)
	public Boolean getGebelik() {
		return gebelik;
	}

	public void setGebelik(Boolean gebelik) {
		this.gebelik = gebelik;
	}

	@Column(name = COLUMN_NAME_SUA)
	public Boolean getSua() {
		return sua;
	}

	public void setSua(Boolean sua) {
		this.sua = sua;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_DEPARTMAN)
	@Fetch(FetchMode.JOIN)
	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman departman) {
		this.departman = departman;
	}

	@Column(name = "YEMEK_SURESI")
	@Min(value = 0, message = "Sıfırdan küçük değer giremezsiniz!")
	public Integer getYemekSuresi() {
		return yemekSuresi;
	}

	public void setYemekSuresi(Integer yemekSuresi) {
		this.yemekSuresi = yemekSuresi;
	}

	@Column(name = "ARIFE_CALISMA_DAKIKA")
	public Double getArifeNormalCalismaDakika() {
		return arifeNormalCalismaDakika;
	}

	public void setArifeNormalCalismaDakika(Double arifeNormalCalismaDakika) {
		this.arifeNormalCalismaDakika = arifeNormalCalismaDakika;
	}

	@Column(name = COLUMN_NAME_EKRAN_SIRA)
	public Integer getEkranSira() {
		if (ekranSira == null && id != null)
			ekranSira = id.intValue();
		return ekranSira;
	}

	public void setEkranSira(Integer ekranSira) {
		this.ekranSira = ekranSira;
	}

	@Column(name = "AKSAM_VARDIYA")
	public Boolean getAksamVardiya() {
		return aksamVardiya;
	}

	public void setAksamVardiya(Boolean aksamVardiya) {
		this.aksamVardiya = aksamVardiya;
	}

	@Column(name = COLUMN_NAME_ICAP)
	public Boolean getIcapVardiya() {
		return icapVardiya;
	}

	public void setIcapVardiya(Boolean icapVardiya) {
		this.icapVardiya = icapVardiya;
	}

	@Column(name = COLUMN_NAME_GENEL)
	public Boolean getGenel() {
		return genel;
	}

	public void setGenel(Boolean genel) {
		this.genel = genel;
	}

	@Column(name = "CIKIS_MOLA_DAKIKA")
	public Integer getCikisMolaSaat() {
		return cikisMolaSaat;
	}

	public void setCikisMolaSaat(Integer cikisMolaSaat) {
		this.cikisMolaSaat = cikisMolaSaat;
	}

	// (min = 21, max = 72)
	@Column(name = "CALISMASAATI")
	public double getCalismaSaati() {
		return calismaSaati;
	}

	public void setCalismaSaati(double calismaSaati) {
		this.calismaSaati = calismaSaati;
	}

	// (min = 3, max = 6)
	@Column(name = "CALISMAGUN")
	public int getCalismaGun() {

		return calismaGun;
	}

	public void setCalismaGun(int calismaGun) {
		this.calismaGun = calismaGun;
	}

	@Transient
	public Double getArifeCalismaSure() {
		return arifeCalismaSure;
	}

	public void setArifeCalismaSure(Double arifeCalismaSure) {
		this.arifeCalismaSure = arifeCalismaSure;
	}

	@Transient
	public String getTipi() {
		return tipi;
	}

	public void setTipi(String tipi) {
		this.vardiyaTipi = tipi.charAt(0);
		this.tipi = tipi;
	}

	@Transient
	public String getVardiyaTipiAciklama() {
		return getVardiyaTipiAciklama(vardiyaTipi, adi);
	}

	@Transient
	public static String getVardiyaTipiAciklama(char tipi, String orjAdi) {
		String aciklama = "";
		switch (tipi) {
		case TIPI_CALISMA:
			aciklama = "Normal Çalışma";
			break;
		case TIPI_HAFTA_TATIL:
			aciklama = orjAdi;
			break;
		case TIPI_OFF:
			aciklama = orjAdi;
			break;
		case TIPI_FMI:
			aciklama = orjAdi;
			break;
		case TIPI_RADYASYON_IZNI:
			aciklama = "Radyasyon İzni";
			break;
		case TIPI_IZIN:
			aciklama = orjAdi;
			break;
		case TIPI_HASTALIK_RAPOR:
			aciklama = orjAdi;
			break;
		default:
			break;
		}
		return aciklama;
	}

	@Column(name = "BASDAKIKA")
	public short getBasDakika() {
		return basDakika;
	}

	public void setBasDakika(short basDakika) {
		this.basDakika = basDakika;
	}

	@Column(name = "BASSAAT")
	public short getBasSaat() {
		return basSaat;
	}

	public void setBasSaat(short basSaat) {
		this.basSaat = basSaat;
	}

	@Column(name = "BITDAKIKA")
	public short getBitDakika() {
		return bitDakika;
	}

	public void setBitDakika(short bitDakika) {
		this.bitDakika = bitDakika;
	}

	@Column(name = "BITSAAT")
	public short getBitSaat() {
		return bitSaat;
	}

	public void setBitSaat(short bitSaat) {
		this.bitSaat = bitSaat;
	}

	@Column(name = COLUMN_NAME_VARDIYA_TIPI)
	public char getVardiyaTipi() {
		return vardiyaTipi;
	}

	public void setVardiyaTipi(char vardiyaTipi) {
		this.vardiyaTipi = vardiyaTipi;
	}

	@Column(name = "FAZLA_MESAI_BAS_SURE")
	public Integer getFazlaMesaiBasDakika() {
		return fazlaMesaiBasDakika;
	}

	public void setFazlaMesaiBasDakika(Integer fazlaMesaiBasDakika) {
		this.fazlaMesaiBasDakika = fazlaMesaiBasDakika;
	}

	@Transient
	public String getBasSaatDakikaStr() {
		StringBuilder aciklama = new StringBuilder(String.valueOf(basSaat));
		// aciklama = pdksUtil.textBaslangicinaKarakterEkle(aciklama, '0', 2);
		if (basDakika > 0)
			aciklama.append(":" + PdksUtil.textBaslangicinaKarakterEkle(String.valueOf(basDakika), '0', 2));
		String str = aciklama.toString();
		aciklama = null;
		return str;
	}

	@Transient
	public String getBitSaatDakikaStr() {
		StringBuilder aciklama = new StringBuilder(String.valueOf(bitSaat));
		// aciklama = pdksUtil.textBaslangicinaKarakterEkle(aciklama, '0', 2);
		if (bitDakika > 0)
			aciklama.append(":" + PdksUtil.textBaslangicinaKarakterEkle(String.valueOf(bitDakika), '0', 2));
		String str = aciklama.toString();
		aciklama = null;
		return str;
	}

	@Transient
	public String getAciklama() {
		String aciklama = isCalisma() ? adi : getVardiyaTipiAciklama();
		return aciklama;
	}

	@Transient
	public String getVardiyaAciklama() {
		String aciklama = isCalisma() ? getBasSaatDakikaStr() + " - " + getBitSaatDakikaStr() : getVardiyaTipiAciklama();
		return aciklama;
	}

	@Transient
	public boolean isCalisma() {
		return vardiyaTipi == TIPI_CALISMA;
	}

	@Transient
	public boolean isHaftaTatil() {
		return vardiyaTipi == TIPI_HAFTA_TATIL;
	}

	@Transient
	public boolean isOff() {
		return vardiyaTipi == TIPI_OFF;
	}

	@Transient
	public boolean isFMI() {
		return vardiyaTipi == TIPI_FMI;
	}

	@Transient
	public boolean isOffGun() {
		return vardiyaTipi == TIPI_OFF || vardiyaTipi == TIPI_FMI;
	}

	@Transient
	public boolean isRadyasyonIzni() {
		return vardiyaTipi == TIPI_RADYASYON_IZNI;
	}

	@Transient
	public boolean isIzin() {
		return vardiyaTipi == TIPI_IZIN || isHastalikRapor();
	}

	@Transient
	public boolean isHastalikRapor() {
		return vardiyaTipi == TIPI_HASTALIK_RAPOR;
	}

	@Transient
	public Date getVardiyaBasZaman() {
		return vardiyaBasZaman;
	}

	public void setVardiyaBasZaman(Date vardiyaBasZaman) {
		this.vardiyaBasZaman = vardiyaBasZaman;
	}

	@Transient
	public Date getVardiyaBitZaman() {
		return vardiyaBitZaman;
	}

	public void setVardiyaBitZaman(Date value) {
		this.vardiyaBitZaman = value;
	}

	// TODO Vardiyalar kontrol ediliyor
	@Transient
	public void setVardiyaZamani(VardiyaGun pdksVardiyaGun) {
		Date tarih = null;
		ayinSonGunDurum = false;
		if (pdksVardiyaGun != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(pdksVardiyaGun.getVardiyaDate());
			tarih = cal.getTime();
			vardiyaDateStr = PdksUtil.convertToDateString(tarih, "yyyyMMdd");
			if (vardiyaKontrolTarih3 != null && tarih.after(vardiyaKontrolTarih3))
				vardiyaKontrol3(pdksVardiyaGun, tarih);
			else if (vardiyaKontrolTarih2 != null && tarih.after(vardiyaKontrolTarih2))
				vardiyaKontrol2(pdksVardiyaGun, tarih);
			else if (vardiyaKontrolTarih != null && tarih.before(vardiyaKontrolTarih))
				vardiyaKontrol1(pdksVardiyaGun, tarih);
			else
				vardiyaKontrol(pdksVardiyaGun, tarih);

			tarih = cal.getTime();

			Date aySonuKontrolTarih = vardiyaAySonuKontrolTarih;
			if (aySonuKontrolTarih != null && tarih.after(aySonuKontrolTarih)) {
				if (pdksVardiyaGun.getVardiya() != null && pdksVardiyaGun.getVardiya().getId() != null)
					gunSonuAyir(pdksVardiyaGun);
			}
		}
		setVardiyaTarih(tarih);

	}

	/**
	 * @param pdksVardiyaGun
	 */
	private void gunSonuAyir(VardiyaGun pdksVardiyaGun) {
		Date tarih = (Date) pdksVardiyaGun.getVardiyaDate().clone();
		Vardiya vardiya = pdksVardiyaGun.getVardiya();
		vardiya.setVardiyaTarih(tarih);
		Calendar cal = Calendar.getInstance();
		cal.setTime(pdksVardiyaGun.getVardiyaDate());
		Personel personel = pdksVardiyaGun.getPersonel();
		if (personel != null && personel.getId() != null) {
			if (personel.getSskCikisTarihi() != null && personel.getIseGirisTarihi() != null && personel.getIseGirisTarihi().getTime() <= tarih.getTime() && personel.getSskCikisTarihi().getTime() >= tarih.getTime()) {
				Vardiya sonrakiVardiya = null, oncekiVardiya = null;
				if (pdksVardiyaGun.getSonrakiVardiyaGun() != null && pdksVardiyaGun.getSonrakiVardiyaGun().getVardiya() != null) {
					sonrakiVardiya = pdksVardiyaGun.getSonrakiVardiya();
					if (sonrakiVardiya != null) {
						if (sonrakiVardiya.getId() == null)
							sonrakiVardiya = null;
						else
							sonrakiVardiya.setOncekiVardiya(pdksVardiyaGun.getIslemVardiya());

					}
				}
				if (pdksVardiyaGun.getOncekiVardiyaGun() != null && pdksVardiyaGun.getOncekiVardiyaGun().getVardiya() != null) {
					oncekiVardiya = pdksVardiyaGun.getOncekiVardiya();
					if (oncekiVardiya != null) {
						if (oncekiVardiya.getId() == null)
							oncekiVardiya = null;
						else
							oncekiVardiya.setSonrakiVardiya(pdksVardiyaGun.getIslemVardiya());
					}
				}
				if (vardiya.isCalisma()) {
					Vardiya vardiyaCalisma = pdksVardiyaGun.getIslemVardiya();
					vardiyaCalisma.setVardiyaTarih(tarih);
					cal.setTime(tarih);

					if (vardiya.getBitSaat() > vardiya.getBasSaat()) {

						if (vardiyaCalisma.getVardiyaTelorans1BasZaman() != null && vardiyaCalisma.getVardiyaTelorans1BasZaman().before(tarih)) {
							int bosluk = -vardiyaCalisma.getGirisErkenToleransDakika();
							if (oncekiVardiya != null && oncekiVardiya.isCalisma() == false)
								bosluk = oncekiVardiya.isHaftaTatil() ? haftaTatiliFazlaMesaiBasDakika : offFazlaMesaiBasDakika;
							cal.add(Calendar.MINUTE, bosluk);
							vardiyaCalisma.setVardiyaFazlaMesaiBasZaman((Date) cal.getTime().clone());
							vardiyaCalisma.setVardiyaTelorans1BasZaman((Date) cal.getTime().clone());
							if (oncekiVardiya != null) {

								cal.add(Calendar.MILLISECOND, -100);
								oncekiVardiya.setVardiyaFazlaMesaiBitZaman((Date) cal.getTime().clone());
								if (oncekiVardiya.isCalisma() == false)
									oncekiVardiya.setVardiyaTelorans2BitZaman(oncekiVardiya.getVardiyaFazlaMesaiBitZaman());
							}
						} else {
							if (oncekiVardiya != null) {
								if (oncekiVardiya.isCalisma()) {
									if (oncekiVardiya.isCalisma() && oncekiVardiya.getBitSaat() > oncekiVardiya.getBasSaat()) {
										vardiyaCalisma.setVardiyaFazlaMesaiBasZaman(PdksUtil.getDate(tarih));
										cal.add(Calendar.MILLISECOND, -100);
										oncekiVardiya.setVardiyaFazlaMesaiBitZaman((Date) cal.getTime().clone());
										cal.add(Calendar.DATE, 1);
										vardiyaCalisma.setVardiyaFazlaMesaiBitZaman((Date) cal.getTime().clone());
									}
								}

							}

						}
						if (sonrakiVardiya != null) {
							if (sonrakiVardiya.isCalisma()) {
								if (sonrakiVardiya.getBitSaat() > sonrakiVardiya.getBasSaat()) {
									sonrakiVardiya.setVardiyaFazlaMesaiBasZaman(sonrakiVardiya.getVardiyaTarih());
									cal.setTime(sonrakiVardiya.getVardiyaTarih());
									if (sonrakiVardiya.getVardiyaTelorans1BasZaman() != null && sonrakiVardiya.getVardiyaTelorans1BasZaman().before(PdksUtil.tariheGunEkleCikar(tarih, 1))) {
										int bosluk = -sonrakiVardiya.getGirisErkenToleransDakika();
										if (sonrakiVardiya.isCalisma() == false)
											bosluk = sonrakiVardiya.isHaftaTatil() ? haftaTatiliFazlaMesaiBasDakika : offFazlaMesaiBasDakika;
										cal.add(Calendar.MINUTE, bosluk);
										sonrakiVardiya.setVardiyaFazlaMesaiBasZaman((Date) cal.getTime().clone());
									}
									cal.add(Calendar.MILLISECOND, -100);
									vardiyaCalisma.setVardiyaFazlaMesaiBitZaman((Date) cal.getTime().clone());
								}

							} else {
								// todo
								if (sonrakiVardiya.getVardiyaTarih().before(vardiyaCalisma.getVardiyaTelorans2BitZaman())) {
									cal.setTime(vardiyaCalisma.getVardiyaTelorans2BitZaman());
									sonrakiVardiya.setVardiyaFazlaMesaiBasZaman((Date) cal.getTime().clone());
									vardiyaCalisma.setVardiyaFazlaMesaiBitZaman(vardiyaCalisma.getVardiyaBitZaman());
									cal.add(Calendar.MILLISECOND, -100);
									vardiyaCalisma.setVardiyaTelorans2BitZaman((Date) cal.getTime().clone());
								} else {
									cal.setTime(sonrakiVardiya.getVardiyaTarih());
									int bosluk = sonrakiVardiya.isHaftaTatil() ? haftaTatiliFazlaMesaiBasDakika : offFazlaMesaiBasDakika;
									cal.add(Calendar.MINUTE, bosluk);
									sonrakiVardiya.setVardiyaTelorans1BasZaman((Date) cal.getTime().clone());
									sonrakiVardiya.setVardiyaFazlaMesaiBasZaman((Date) cal.getTime().clone());
									cal.add(Calendar.MILLISECOND, -100);
									vardiyaCalisma.setVardiyaFazlaMesaiBitZaman((Date) cal.getTime().clone());
								}

							}
						}
					} else {
						if (sonrakiVardiya != null) {

							if (sonrakiVardiya.isCalisma() == false) {
								try {
									int bosluk = sonrakiVardiya.isHaftaTatil() ? haftaTatiliFazlaMesaiBasDakika : offFazlaMesaiBasDakika;
									cal.setTime(vardiyaCalisma.getVardiyaBitZaman());
									cal.add(Calendar.MINUTE, -bosluk);
									Date fazlaMesaiBasSaat = cal.getTime();
									if (fazlaMesaiBasSaat.after(vardiyaCalisma.getVardiyaTelorans2BitZaman()))
										vardiyaCalisma.setVardiyaTelorans2BitZaman(fazlaMesaiBasSaat);
								} catch (Exception e) {
									logger.error(e);
									e.printStackTrace();
								}

								cal.setTime(vardiyaCalisma.getVardiyaTelorans2BitZaman());
								sonrakiVardiya.setVardiyaFazlaMesaiBasZaman((Date) cal.getTime().clone());
								vardiyaCalisma.setVardiyaFazlaMesaiBitZaman(vardiyaCalisma.getVardiyaBitZaman());
								cal.add(Calendar.MILLISECOND, -100);
								vardiyaCalisma.setVardiyaTelorans2BitZaman((Date) cal.getTime().clone());
							} else {
								Double sureDakika = PdksUtil.getSaatFarki(sonrakiVardiya.getVardiyaBasZaman(), vardiyaCalisma.getVardiyaBitZaman()).doubleValue() * 30.0d;
								if (sureDakika > 0) {
									cal.setTime(vardiyaCalisma.getVardiyaBitZaman());
									cal.add(Calendar.MINUTE, sureDakika.intValue());
									sonrakiVardiya.setVardiyaFazlaMesaiBasZaman((Date) cal.getTime().clone());
									cal.add(Calendar.MILLISECOND, -100);
									vardiyaCalisma.setVardiyaFazlaMesaiBitZaman((Date) cal.getTime().clone());
									logger.debug(vardiyaDateStr + " " + sureDakika);
								}

							}
						}
					}
				} else {
					Vardiya offCalisma = pdksVardiyaGun.getIslemVardiya();
					offCalisma.setVardiyaTarih(tarih);
					if (oncekiVardiya != null) {

						if (oncekiVardiya.isCalisma() == false || oncekiVardiya.getBitSaat() > oncekiVardiya.getBasSaat()) {
							offCalisma.setVardiyaFazlaMesaiBasZaman(PdksUtil.getDate(tarih));
							cal.setTime(tarih);
							cal.add(Calendar.MILLISECOND, -100);
							oncekiVardiya.setVardiyaFazlaMesaiBitZaman((Date) cal.getTime().clone());

						} else {
							// Date tarih2 = PdksUtil.addTarih(oncekiVardiya.getVardiyaBitZaman(), Calendar.MILLISECOND, 100);
							Date tarih2 = (Date) oncekiVardiya.getVardiyaBitZaman().clone();
							offCalisma.setVardiyaFazlaMesaiBasZaman(tarih2);
							offCalisma.setVardiyaBasZaman(tarih2);
							cal.setTime(oncekiVardiya.getVardiyaBitZaman());
							cal.add(Calendar.MILLISECOND, -100);
							Date tarih1 = (Date) cal.getTime().clone();
							oncekiVardiya.setVardiyaBitZaman(tarih1);
							oncekiVardiya.setVardiyaFazlaMesaiBitZaman(tarih1);
							oncekiVardiya.setVardiyaTelorans2BitZaman(tarih1);

						}

					}
					if (sonrakiVardiya != null) {
						Date sonrakiGun = PdksUtil.tariheGunEkleCikar(tarih, 1);
						cal.setTime(sonrakiGun);
						sonrakiVardiya.setVardiyaFazlaMesaiBasZaman((Date) cal.getTime().clone());
						if (sonrakiVardiya.isCalisma() && sonrakiVardiya.getVardiyaTelorans1BasZaman() != null && sonrakiVardiya.getVardiyaTelorans1BasZaman().before(sonrakiGun)) {
							cal.setTime(sonrakiGun);
							int bosluk = sonrakiVardiya.isHaftaTatil() ? haftaTatiliFazlaMesaiBasDakika : offFazlaMesaiBasDakika;
							cal.add(Calendar.MINUTE, bosluk);
							sonrakiVardiya.setVardiyaTelorans1BasZaman((Date) cal.getTime().clone());
							sonrakiVardiya.setVardiyaFazlaMesaiBasZaman((Date) cal.getTime().clone());
						}
						cal.add(Calendar.MILLISECOND, -100);
						offCalisma.setVardiyaFazlaMesaiBitZaman((Date) cal.getTime().clone());
					}
				}
				if (sonrakiVardiya == null) {
					Date sonrakiGun = PdksUtil.tariheGunEkleCikar(tarih, 1);
					cal.setTime(sonrakiGun);
					cal.add(Calendar.MILLISECOND, -100);
					vardiya.setVardiyaFazlaMesaiBitZaman((Date) cal.getTime().clone());
					if (vardiya.isCalisma() && vardiya.getBitSaat() < vardiya.getBasSaat()) {
						vardiya.setVardiyaFazlaMesaiBitZaman(vardiya.getVardiyaTelorans2BitZaman());
					}
				}
				if (oncekiVardiya == null && tarih.before(personel.getIseGirisTarihi()))
					vardiya.setVardiyaFazlaMesaiBasZaman((Date) tarih.clone());

			}
		}
		// if (personel != null && personel.getId() != null) {
		//
		// if (personel.getSskCikisTarihi() != null && personel.getSskCikisTarihi().getTime() <= tarih.getTime()) {
		// ayinSonGunDurum = vardiyaCalisma.isCalisma() == false || vardiyaCalisma.getBitSaat() > vardiyaCalisma.getBasSaat();
		// } else if (pdksVardiyaGun.getVardiya().isCalisma()) {
		// if (vardiyaCalisma.getBitSaat() > vardiyaCalisma.getBasSaat()) {
		// if (pdksVardiyaGun.getSonrakiVardiyaGun() != null && pdksVardiyaGun.getSonrakiVardiyaGun().getVardiya() != null) {
		// ayinSonGunDurum = true;
		// }
		// }
		// }
		// }
		// if (ayinSonGunDurum) {
		// cal.setTime(PdksUtil.tariheGunEkleCikar(tarih, 1));
		// int dakika = vardiyaCalisma.getCikisGecikmeToleransDakika();
		// if (pdksVardiyaGun.getSonrakiVardiyaGun() != null && pdksVardiyaGun.getSonrakiVardiyaGun().getVardiya().isCalisma())
		// dakika = pdksVardiyaGun.getSonrakiVardiyaGun().getVardiya().getGirisErkenToleransDakika();
		// cal.set(Calendar.MINUTE, -dakika);
		// vardiyaFazlaMesaiBasZaman = (Date) cal.getTime().clone();
		// cal.set(Calendar.MILLISECOND, -100);
		// vardiyaFazlaMesaiBitZaman = cal.getTime();
		// vardiyaCalisma.setVardiyaFazlaMesaiBitZaman((Date) vardiyaFazlaMesaiBitZaman.clone());
		// if (pdksVardiyaGun.getSonrakiVardiya() != null)
		// pdksVardiyaGun.getSonrakiVardiya().setVardiyaFazlaMesaiBasZaman((Date) vardiyaFazlaMesaiBasZaman.clone());
		//
		// if (pdksVardiyaGun.getOncekiVardiya() != null) {
		// Vardiya oncekiVardiya = pdksVardiyaGun.getOncekiVardiya();
		// if (oncekiVardiya != null) {
		// if (!oncekiVardiya.isCalisma() || oncekiVardiya.getBitSaat() > oncekiVardiya.getBasSaat()) {
		// dakika = vardiyaCalisma.getGirisErkenToleransDakika();
		// cal.setTime(tarih);
		// cal.set(Calendar.MINUTE, -dakika);
		// vardiyaCalisma.setVardiyaFazlaMesaiBasZaman((Date) cal.getTime().clone());
		// cal.set(Calendar.MILLISECOND, -100);
		// oncekiVardiya.setVardiyaFazlaMesaiBitZaman((Date) cal.getTime().clone());
		//
		// }
		// }
		//
		// }
		//
		// }
	}

	/**
	 * @param pdksVardiyaGun
	 * @param tarih
	 */
	private void vardiyaKontrol1(VardiyaGun pdksVardiyaGun, Date tarih) {
		if (pdksVardiyaGun.isZamanGuncelle()) {
			if (pdksVardiyaGun.getBasSaat() != null) {
				basSaat = pdksVardiyaGun.getBasSaat().shortValue();
				basDakika = pdksVardiyaGun.getBasDakika().shortValue();
			}
			if (pdksVardiyaGun.getBitSaat() != null) {
				bitSaat = pdksVardiyaGun.getBitSaat().shortValue();
				bitDakika = pdksVardiyaGun.getBitDakika().shortValue();
			}
		}

		long zamanBas = basSaat * 100 + basDakika;
		long zamanBit = bitSaat * 100 + bitDakika;
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(tarih);
		Date sonGun = null;
		if (bitSaat > basSaat) {
			if (sonrakiVardiya != null && sonrakiVardiya.isHaftaTatil()) {
				cal1.setTime(tarih);
				cal1.add(Calendar.MINUTE, haftaTatiliFazlaMesaiBasDakika);
				sonGun = cal1.getTime();
				// sonGun = PdksUtil.tariheGunEkleCikar(tarih, 1);
			}

			if (sonGun != null)
				logger.debug(pdksVardiyaGun.getVardiyaKeyStr() + " " + sonrakiVardiya.getKisaAciklama());
		}
		if (isCalisma()) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(tarih);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), bitSaat, bitDakika, 0);
			vardiyaBitZaman = cal.getTime();
			cal.setTime(tarih);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), basSaat, basDakika, 0);
			vardiyaBasZaman = cal.getTime();
			farkliGun = zamanBas >= zamanBit;
			if (farkliGun) {
				cal.setTime(vardiyaBitZaman);
				cal.add(Calendar.DATE, 1);
				vardiyaBitZaman = cal.getTime();
			} else if (zamanBas == zamanBit) {
				cal.setTime(vardiyaBitZaman);
				cal.add(Calendar.DATE, 1);
				vardiyaBitZaman = cal.getTime();

			}
			cal.setTime((Date) vardiyaBasZaman.clone());
			cal.add(Calendar.MINUTE, girisGecikmeToleransDakika);
			vardiyaTelorans2BasZaman = cal.getTime();
			cal.setTime((Date) vardiyaBasZaman.clone());
			cal.add(Calendar.MINUTE, -girisErkenToleransDakika);
			vardiyaTelorans1BasZaman = cal.getTime();

			cal.setTime((Date) vardiyaBitZaman.clone());
			cal.add(Calendar.MINUTE, cikisGecikmeToleransDakika);
			vardiyaTelorans2BitZaman = cal.getTime();
			cal.setTime((Date) vardiyaBitZaman.clone());
			cal.add(Calendar.MINUTE, -cikisErkenToleransDakika);
			vardiyaTelorans1BitZaman = cal.getTime();
			if (sonrakiVardiya == null && oncekiVardiya == null) {
				double sure = PdksUtil.getSaatFarki(vardiyaBitZaman, vardiyaBasZaman).doubleValue();
				int bosluk = new Double((33.0d - sure) / 2.0d).intValue();
				setVardiyaFazlaMesaiBasZaman(PdksUtil.addTarih(vardiyaBasZaman, Calendar.HOUR_OF_DAY, -bosluk / 2));
				setVardiyaFazlaMesaiBitZaman(PdksUtil.addTarih(vardiyaBitZaman, Calendar.HOUR_OF_DAY, bosluk));
			} else {
				if (oncekiVardiya != null) {
					if (oncekiVardiya.isCalisma() || oncekiVardiya.getVardiyaBitZaman() != null) {
						double sure1 = PdksUtil.getSaatFarki(vardiyaBasZaman, oncekiVardiya.getVardiyaBitZaman()).doubleValue();
						if (sure1 > 0 && vardiyaBasZaman != null) {
							int bosluk = new Double(sure1 / 2.0d).intValue();
							Date basZaman = PdksUtil.addTarih(vardiyaBasZaman, Calendar.HOUR_OF_DAY, -bosluk);
							setVardiyaFazlaMesaiBasZaman(basZaman);

						}
					} else
						logger.info(oncekiVardiya.getAdi());
				}
				if (sonrakiVardiya != null && sonrakiVardiya.getVardiyaBasZaman() != null) {
					if (sonGun != null) {
						setVardiyaFazlaMesaiBitZaman(sonGun);
						sonrakiVardiya.setVardiyaBasZaman(sonGun);
					} else {
						if (vardiyaBitZaman != null) {
							double sure2 = PdksUtil.getSaatFarki(sonrakiVardiya.getVardiyaBasZaman(), vardiyaBitZaman).doubleValue();
							if (sure2 > 0 && vardiyaBitZaman != null) {
								int bosluk = new Double(sure2 / 2.0d).intValue();
								Date bitZaman = PdksUtil.addTarih(vardiyaBitZaman, Calendar.HOUR_OF_DAY, bosluk);
								setVardiyaFazlaMesaiBitZaman(bitZaman);
							}
						} else
							logger.debug(sonrakiVardiya.getAdi());

					}

				}
			}

		} else if (tarih != null) {
			Vardiya oncekiIslemVardiya = pdksVardiyaGun.getOncekiVardiyaGun() != null && pdksVardiyaGun.getOncekiVardiyaGun().getIslemVardiya() != null ? pdksVardiyaGun.getOncekiVardiyaGun().getIslemVardiya() : null;
			Vardiya sonrakiIslemVardiya = pdksVardiyaGun.getSonrakiVardiyaGun() != null && pdksVardiyaGun.getSonrakiVardiyaGun().getIslemVardiya().isCalisma() ? pdksVardiyaGun.getSonrakiVardiyaGun().getIslemVardiya() : null;
			// if ((oncekiIslemVardiya != null || sonrakiIslemVardiya != null) && (pdksVardiyaGun.getVardiyaDateStr().equals("20200808") || pdksVardiyaGun.getVardiyaDateStr().equals("20200809")))
			// logger.info(pdksVardiyaGun.getVardiyaDateStr());
			vardiyaBasZaman = PdksUtil.convertToJavaDate(PdksUtil.convertToDateString(tarih, "yyyyMMdd") + " 13:00", "yyyyMMdd HH:mm");
			if (oncekiIslemVardiya != null && oncekiIslemVardiya.isCalisma() && vardiyaBasZaman.after(oncekiIslemVardiya.getVardiyaBitZaman())) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(oncekiIslemVardiya.getVardiyaBitZaman());
				cal.add(Calendar.HOUR_OF_DAY, 4);
				Date fazMesBitZaman = cal.getTime();
				if (fazMesBitZaman.before(pdksVardiyaGun.getVardiyaDate()))
					fazMesBitZaman = pdksVardiyaGun.getVardiyaDate();
				if (vardiyaBasZaman.after(fazMesBitZaman)) {
					vardiyaBasZaman = fazMesBitZaman;
					oncekiIslemVardiya.setVardiyaFazlaMesaiBitZaman(fazMesBitZaman);
				}
			}
			vardiyaBitZaman = PdksUtil.addTarih(vardiyaBasZaman, Calendar.HOUR_OF_DAY, 12);

			vardiyaTelorans1BasZaman = vardiyaBasZaman;
			vardiyaTelorans2BasZaman = vardiyaBasZaman;
			setVardiyaFazlaMesaiBasZaman(vardiyaBasZaman);

			vardiyaTelorans1BitZaman = vardiyaBitZaman;
			vardiyaTelorans2BitZaman = vardiyaBitZaman;
			setVardiyaFazlaMesaiBitZaman(vardiyaBitZaman);
			if (oncekiIslemVardiya != null && oncekiIslemVardiya.isCalisma()) {
				setVardiyaFazlaMesaiBasZaman(oncekiIslemVardiya.getVardiyaFazlaMesaiBitZaman());
				setVardiyaFazlaMesaiBitZaman(PdksUtil.addTarih(vardiyaFazlaMesaiBasZaman, Calendar.HOUR_OF_DAY, 12));
			}
			if (sonrakiIslemVardiya != null) {
				setVardiyaFazlaMesaiBitZaman(sonrakiIslemVardiya.getVardiyaFazlaMesaiBasZaman());

			}
		}
	}

	/**
	 * @param pdksVardiyaGun
	 * @param tarih
	 */
	private void vardiyaKontrol3(VardiyaGun pdksVardiyaGun, Date tarih) {
		if (pdksVardiyaGun.isZamanGuncelle()) {
			if (pdksVardiyaGun.getBasSaat() != null) {
				basSaat = pdksVardiyaGun.getBasSaat().shortValue();
				basDakika = pdksVardiyaGun.getBasDakika().shortValue();
			}
			if (pdksVardiyaGun.getBitSaat() != null) {
				bitSaat = pdksVardiyaGun.getBitSaat().shortValue();
				bitDakika = pdksVardiyaGun.getBitDakika().shortValue();
			}
		}
		String key = PdksUtil.convertToDateString(tarih, "yyyyMMdd");

		long zamanBas = basSaat * 100 + basDakika;
		long zamanBit = bitSaat * 100 + bitDakika;
		Calendar cal = Calendar.getInstance();
		cal.setTime(tarih);
		Date sonGun = null;
		if (bitSaat > basSaat) {
			if (sonrakiVardiya != null && sonrakiVardiya.isHaftaTatil()) {
				cal.setTime(tarih);
				cal.add(Calendar.MINUTE, haftaTatiliFazlaMesaiBasDakika);
				sonGun = cal.getTime();
				// sonGun = PdksUtil.tariheGunEkleCikar(tarih, 1);
			}

			if (sonGun != null)
				logger.debug(pdksVardiyaGun.getVardiyaKeyStr() + " " + sonrakiVardiya.getKisaAciklama());
		}
		if (isCalisma()) {
			cal = Calendar.getInstance();
			cal.setTime(tarih);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), bitSaat, bitDakika, 0);
			vardiyaBitZaman = cal.getTime();
			cal.setTime(tarih);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), basSaat, basDakika, 0);
			vardiyaBasZaman = cal.getTime();
			farkliGun = zamanBas >= zamanBit;
			if (farkliGun) {
				cal.setTime(vardiyaBitZaman);
				cal.add(Calendar.DATE, 1);
				vardiyaBitZaman = cal.getTime();
			} else if (zamanBas == zamanBit) {
				cal.setTime(vardiyaBitZaman);
				cal.add(Calendar.DATE, 1);
				vardiyaBitZaman = cal.getTime();

			}
			cal.setTime((Date) vardiyaBasZaman.clone());
			cal.add(Calendar.MINUTE, girisGecikmeToleransDakika);
			vardiyaTelorans2BasZaman = cal.getTime();
			cal.setTime((Date) vardiyaBasZaman.clone());
			cal.add(Calendar.MINUTE, -girisErkenToleransDakika);
			vardiyaTelorans1BasZaman = cal.getTime();

			cal.setTime((Date) vardiyaBitZaman.clone());
			cal.add(Calendar.MINUTE, cikisGecikmeToleransDakika);
			vardiyaTelorans2BitZaman = cal.getTime();
			cal.setTime((Date) vardiyaBitZaman.clone());
			cal.add(Calendar.MINUTE, -cikisErkenToleransDakika);
			vardiyaTelorans1BitZaman = cal.getTime();
			if (sonrakiVardiya == null && oncekiVardiya == null) {
				double sure = PdksUtil.getSaatFarki(vardiyaBitZaman, vardiyaBasZaman).doubleValue();
				int bosluk = new Double((33.0d - sure) / 2.0d).intValue();
				setVardiyaFazlaMesaiBasZaman(PdksUtil.addTarih(vardiyaBasZaman, Calendar.HOUR_OF_DAY, -bosluk / 2));
				setVardiyaFazlaMesaiBitZaman(PdksUtil.addTarih(vardiyaBitZaman, Calendar.HOUR_OF_DAY, bosluk));
			} else {
				if (oncekiVardiya != null && sonrakiVardiya != null && (key.equals("20210915") || key.equals("20210916")))
					logger.debug(key + " " + adi);
				if (oncekiVardiya != null) {

					if (oncekiVardiya.isCalisma() || oncekiVardiya.getVardiyaBitZaman() != null) {
						if (oncekiVardiya.isCalisma()) {
							double sure1 = PdksUtil.getSaatFarki(vardiyaBasZaman, oncekiVardiya.getVardiyaBitZaman()).doubleValue();
							if (sure1 > 0 && oncekiVardiya.isCalisma()) {
								if (vardiyaBasZaman != null) {
									int bosluk = new Double(sure1 / 2.0d).intValue();
									Date basZaman = PdksUtil.addTarih(vardiyaBasZaman, Calendar.HOUR_OF_DAY, -bosluk);
									setVardiyaFazlaMesaiBasZaman(basZaman);
								} else
									logger.debug(oncekiVardiya.getAdi());

							}
						} else {
							int bosluk = oncekiVardiya.getGirisErkenToleransDakika();
							if (bosluk != 0)
								logger.debug(key + " " + bosluk);
							Date vfmb = PdksUtil.addTarih(pdksVardiyaGun.getVardiyaDate(), Calendar.MINUTE, bosluk);

							if (!oncekiVardiya.isCalisma() && vardiyaBitZaman.after(oncekiVardiya.getVardiyaBitZaman())) {
								bosluk = -oncekiVardiya.getCikisGecikmeToleransDakika();
								if (bosluk != 0)
									logger.debug(key + " " + bosluk);
								vfmb = PdksUtil.addTarih(pdksVardiyaGun.getVardiyaDate(), Calendar.MINUTE, bosluk);

							}
							setVardiyaFazlaMesaiBasZaman(vfmb);
						}

					} else
						logger.debug(oncekiVardiya.getAdi());
				}
				if (sonrakiVardiya != null && sonrakiVardiya.getVardiyaBasZaman() != null) {
					if (sonGun != null) {
						setVardiyaFazlaMesaiBitZaman(sonGun);
						sonrakiVardiya.setVardiyaBasZaman(sonGun);
					} else if (vardiyaBitZaman != null) {
						double sure2 = PdksUtil.getSaatFarki(sonrakiVardiya.getVardiyaBasZaman(), vardiyaBitZaman).doubleValue();
						if (sure2 > 0) {
							int bosluk = new Double(sure2 / 2.0d).intValue();
							Date bitZaman = PdksUtil.addTarih(vardiyaBitZaman, Calendar.HOUR_OF_DAY, bosluk);
							setVardiyaFazlaMesaiBitZaman(bitZaman);
						}
						if (!sonrakiVardiya.isCalisma()) {
							if (!vardiyaBitZaman.after(sonrakiVardiya.getVardiyaBasZaman())) {
								int bosluk = -sonrakiVardiya.getCikisGecikmeToleransDakika();
								if (bosluk != 0)
									logger.debug(key + " " + bosluk);
								Date vfmb = PdksUtil.addTarih(sonrakiVardiya.getVardiyaBasZaman(), Calendar.MINUTE, bosluk);
								setVardiyaFazlaMesaiBitZaman(vfmb);
							}
						}
					}

				}
			}

		} else if (tarih != null) {
			Vardiya oncekiIslemVardiya = pdksVardiyaGun.getOncekiVardiyaGun() != null && pdksVardiyaGun.getOncekiVardiyaGun().getIslemVardiya() != null ? pdksVardiyaGun.getOncekiVardiyaGun().getIslemVardiya() : null;
			Vardiya sonrakiIslemVardiya = pdksVardiyaGun.getSonrakiVardiyaGun() != null && pdksVardiyaGun.getSonrakiVardiyaGun().getIslemVardiya().isCalisma() ? pdksVardiyaGun.getSonrakiVardiyaGun().getIslemVardiya() : null;
			vardiyaBasZaman = PdksUtil.convertToJavaDate(PdksUtil.convertToDateString(tarih, "yyyyMMdd") + " 13:00", "yyyyMMdd HH:mm");
			cal = Calendar.getInstance();
			cal.setTime(tarih);
			if (oncekiVardiya != null && sonrakiVardiya != null && (key.equals("20210915") || key.equals("20210916")))
				logger.debug(key + " " + adi);
			if (oncekiIslemVardiya != null && oncekiIslemVardiya.getVardiyaBasZaman() != null) {
				if (oncekiIslemVardiya.isCalisma()) {
					if (vardiyaBasZaman.after(oncekiIslemVardiya.getVardiyaBitZaman())) {
						cal = Calendar.getInstance();
						cal.setTime(oncekiIslemVardiya.getVardiyaBitZaman());
						cal.add(Calendar.HOUR_OF_DAY, 6);
						Date fazMesBitZaman = cal.getTime();
						// if (fazMesBitZaman.before(pdksVardiyaGun.getVardiyaDate()))
						// fazMesBitZaman = pdksVardiyaGun.getVardiyaDate();
						if (vardiyaBasZaman.after(fazMesBitZaman)) {
							// vardiyaBasZaman = fazMesBitZaman;
							oncekiIslemVardiya.setVardiyaFazlaMesaiBitZaman(fazMesBitZaman);
						}
					}
					if (!oncekiIslemVardiya.getVardiyaBitZaman().after(tarih)) {
						oncekiIslemVardiya.setVardiyaFazlaMesaiBitZaman(tarih);
					}
				}
			}
			vardiyaBasZaman = pdksVardiyaGun.getVardiyaDate();
			int bosluk = 0;
			if (!this.isCalisma())
				bosluk = this.isHaftaTatil() ? haftaTatiliFazlaMesaiBasDakika : offFazlaMesaiBasDakika;
			vardiyaTelorans1BasZaman = this.isCalisma() ? PdksUtil.addTarih(vardiyaBasZaman, Calendar.HOUR, -1) : PdksUtil.addTarih(vardiyaBasZaman, Calendar.MINUTE, bosluk);
			if (oncekiIslemVardiya != null) {
				if (oncekiIslemVardiya.isCalisma() && oncekiIslemVardiya.getVardiyaFazlaMesaiBitZaman() != null && vardiyaTelorans1BasZaman.after(oncekiIslemVardiya.getVardiyaFazlaMesaiBitZaman())) {
					if (vardiyaTelorans1BasZaman.after(oncekiIslemVardiya.getVardiyaFazlaMesaiBitZaman())) {
						oncekiIslemVardiya.setVardiyaFazlaMesaiBitZaman(vardiyaTelorans1BasZaman);
					} else {
						vardiyaTelorans1BasZaman = oncekiIslemVardiya.getVardiyaFazlaMesaiBitZaman();
					}
				}
			} else
				vardiyaTelorans1BasZaman = vardiyaBasZaman;
			vardiyaBitZaman = PdksUtil.addTarih(vardiyaBasZaman, Calendar.HOUR_OF_DAY, 18);
			vardiyaTelorans2BasZaman = vardiyaBasZaman;
			// setVardiyaFazlaMesaiBasZaman(vardiyaBasZaman);

			vardiyaTelorans1BitZaman = vardiyaBitZaman;
			vardiyaTelorans2BitZaman = vardiyaBitZaman;
			setVardiyaFazlaMesaiBitZaman(vardiyaBitZaman);
			if (oncekiIslemVardiya != null && oncekiIslemVardiya.isCalisma()) {
				setVardiyaFazlaMesaiBasZaman(oncekiIslemVardiya.getVardiyaFazlaMesaiBitZaman());
				setVardiyaFazlaMesaiBitZaman(PdksUtil.addTarih(vardiyaFazlaMesaiBasZaman, Calendar.HOUR_OF_DAY, 18));
			}
			if (sonrakiIslemVardiya != null) {
				setVardiyaFazlaMesaiBitZaman(sonrakiIslemVardiya.getVardiyaFazlaMesaiBasZaman());

			}
		}

	}

	/**
	 * @param pdksVardiyaGun
	 * @param tarih
	 */
	private void vardiyaKontrol(VardiyaGun pdksVardiyaGun, Date tarih) {
		if (pdksVardiyaGun.isZamanGuncelle()) {
			if (pdksVardiyaGun.getBasSaat() != null) {
				basSaat = pdksVardiyaGun.getBasSaat().shortValue();
				basDakika = pdksVardiyaGun.getBasDakika().shortValue();
			}
			if (pdksVardiyaGun.getBitSaat() != null) {
				bitSaat = pdksVardiyaGun.getBitSaat().shortValue();
				bitDakika = pdksVardiyaGun.getBitDakika().shortValue();
			}
		}
		String key = PdksUtil.convertToDateString(tarih, "yyyyMMdd");
		Calendar cal = Calendar.getInstance();

		long zamanBas = basSaat * 100 + basDakika;
		long zamanBit = bitSaat * 100 + bitDakika;

		Date sonGun = null;
		if (bitSaat > basSaat) {
			if (sonrakiVardiya != null && sonrakiVardiya.isHaftaTatil()) {
				cal.setTime(tarih);
				cal.add(Calendar.MINUTE, haftaTatiliFazlaMesaiBasDakika);
				sonGun = cal.getTime();
				// sonGun = PdksUtil.tariheGunEkleCikar(tarih, 1);
			}

			if (sonGun != null)
				logger.debug(pdksVardiyaGun.getVardiyaKeyStr() + " " + sonrakiVardiya.getKisaAciklama());
		}
		if (isCalisma()) {
			// if (sonGun == null && ayinSonGun)
			// sonGun = PdksUtil.tariheGunEkleCikar(tarih, 1);
			cal = Calendar.getInstance();
			cal.setTime(tarih);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), bitSaat, bitDakika, 0);
			vardiyaBitZaman = cal.getTime();
			cal.setTime(tarih);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), basSaat, basDakika, 0);
			vardiyaBasZaman = cal.getTime();
			farkliGun = zamanBas >= zamanBit;
			if (farkliGun) {
				cal.setTime(vardiyaBitZaman);
				cal.add(Calendar.DATE, 1);
				vardiyaBitZaman = cal.getTime();
			} else if (zamanBas == zamanBit) {
				cal.setTime(vardiyaBitZaman);
				cal.add(Calendar.DATE, 1);
				vardiyaBitZaman = cal.getTime();

			}
			cal.setTime((Date) vardiyaBasZaman.clone());
			cal.add(Calendar.MINUTE, girisGecikmeToleransDakika);
			vardiyaTelorans2BasZaman = cal.getTime();
			cal.setTime((Date) vardiyaBasZaman.clone());
			cal.add(Calendar.MINUTE, -girisErkenToleransDakika);
			vardiyaTelorans1BasZaman = cal.getTime();

			cal.setTime((Date) vardiyaBitZaman.clone());
			cal.add(Calendar.MINUTE, cikisGecikmeToleransDakika);
			vardiyaTelorans2BitZaman = cal.getTime();
			cal.setTime((Date) vardiyaBitZaman.clone());
			cal.add(Calendar.MINUTE, -cikisErkenToleransDakika);
			vardiyaTelorans1BitZaman = cal.getTime();
			if (sonrakiVardiya == null && oncekiVardiya == null) {
				double sure = PdksUtil.getSaatFarki(vardiyaBitZaman, vardiyaBasZaman).doubleValue();
				int bosluk = new Double((33.0d - sure) / 2.0d).intValue();
				setVardiyaFazlaMesaiBasZaman(PdksUtil.addTarih(vardiyaBasZaman, Calendar.HOUR_OF_DAY, -bosluk / 2));
				setVardiyaFazlaMesaiBitZaman(PdksUtil.addTarih(vardiyaBitZaman, Calendar.HOUR_OF_DAY, bosluk));
			} else {
				if (oncekiVardiya != null) {
					if (oncekiVardiya.isCalisma() || oncekiVardiya.getVardiyaBitZaman() != null) {
						if (oncekiVardiya.isCalisma()) {
							double sure1 = PdksUtil.getSaatFarki(vardiyaBasZaman, oncekiVardiya.getVardiyaBitZaman()).doubleValue();
							if (sure1 > 0 && oncekiVardiya.isCalisma()) {
								if (vardiyaBasZaman != null) {
									int bosluk = new Double(sure1 / 2.0d).intValue();
									Date basZaman = PdksUtil.addTarih(vardiyaBasZaman, Calendar.HOUR_OF_DAY, -bosluk);
									setVardiyaFazlaMesaiBasZaman(basZaman);
								} else
									logger.debug(oncekiVardiya.getAdi());

							}
						} else {
							int bosluk = pdksVardiyaGun.getVardiyaDate().after(vardiyaTelorans1BasZaman) ? -girisErkenToleransDakika : oncekiVardiya.getCikisGecikmeToleransDakika();
							if (bosluk != 0)
								logger.debug(key + " " + bosluk);
							Date vfmb = PdksUtil.addTarih(pdksVardiyaGun.getVardiyaDate(), Calendar.MINUTE, bosluk);
							setVardiyaFazlaMesaiBasZaman(vfmb);
						}

					} else
						logger.debug(oncekiVardiya.getAdi());
				}
				if (sonrakiVardiya != null && sonrakiVardiya.getVardiyaBasZaman() != null) {
					if (sonGun != null) {
						setVardiyaFazlaMesaiBitZaman(sonGun);
						sonrakiVardiya.setVardiyaBasZaman(sonGun);
					} else if (vardiyaBitZaman != null) {
						double sure2 = PdksUtil.getSaatFarki(sonrakiVardiya.getVardiyaBasZaman(), vardiyaBitZaman).doubleValue();
						if (sure2 > 0) {
							int bosluk = new Double(sure2 / 2.0d).intValue();
							Date bitZaman = PdksUtil.addTarih(vardiyaBitZaman, Calendar.HOUR_OF_DAY, bosluk);
							setVardiyaFazlaMesaiBitZaman(bitZaman);
						}
					}

				}
			}

		} else if (tarih != null) {
			Vardiya oncekiIslemVardiya = pdksVardiyaGun.getOncekiVardiyaGun() != null && pdksVardiyaGun.getOncekiVardiyaGun().getIslemVardiya() != null ? pdksVardiyaGun.getOncekiVardiyaGun().getIslemVardiya() : null;
			Vardiya sonrakiIslemVardiya = pdksVardiyaGun.getSonrakiVardiyaGun() != null && pdksVardiyaGun.getSonrakiVardiyaGun().getIslemVardiya() != null && pdksVardiyaGun.getSonrakiVardiyaGun().getIslemVardiya().isCalisma() ? pdksVardiyaGun.getSonrakiVardiyaGun().getIslemVardiya() : null;
			vardiyaBasZaman = PdksUtil.convertToJavaDate(PdksUtil.convertToDateString(tarih, "yyyyMMdd") + " 13:00", "yyyyMMdd HH:mm");
			cal = Calendar.getInstance();
			cal.setTime(tarih);

			if (oncekiIslemVardiya != null && oncekiIslemVardiya.getVardiyaBasZaman() != null) {
				if (oncekiIslemVardiya.isCalisma()) {
					if (vardiyaBasZaman.after(oncekiIslemVardiya.getVardiyaBitZaman())) {
						cal = Calendar.getInstance();
						cal.setTime(oncekiIslemVardiya.getVardiyaBitZaman());
						cal.add(Calendar.HOUR_OF_DAY, 6);
						Date fazMesBitZaman = cal.getTime();
						// if (fazMesBitZaman.before(pdksVardiyaGun.getVardiyaDate()))
						// fazMesBitZaman = pdksVardiyaGun.getVardiyaDate();
						if (vardiyaBasZaman.after(fazMesBitZaman)) {
							// vardiyaBasZaman = fazMesBitZaman;
							oncekiIslemVardiya.setVardiyaFazlaMesaiBitZaman(fazMesBitZaman);
						}
					}
				}
			}
			vardiyaBasZaman = pdksVardiyaGun.getVardiyaDate();
			int bosluk = 0;
			if (!this.isCalisma())
				bosluk = this.isHaftaTatil() ? haftaTatiliFazlaMesaiBasDakika : offFazlaMesaiBasDakika;
			vardiyaTelorans1BasZaman = this.isCalisma() ? PdksUtil.addTarih(vardiyaBasZaman, Calendar.HOUR, -1) : PdksUtil.addTarih(vardiyaBasZaman, Calendar.MINUTE, bosluk);
			if (oncekiIslemVardiya != null) {
				if (oncekiIslemVardiya.isCalisma() && oncekiIslemVardiya.getVardiyaFazlaMesaiBitZaman() != null && vardiyaTelorans1BasZaman.after(oncekiIslemVardiya.getVardiyaFazlaMesaiBitZaman())) {
					if (vardiyaTelorans1BasZaman.after(oncekiIslemVardiya.getVardiyaFazlaMesaiBitZaman())) {
						oncekiIslemVardiya.setVardiyaFazlaMesaiBitZaman(vardiyaTelorans1BasZaman);
					} else {
						vardiyaTelorans1BasZaman = oncekiIslemVardiya.getVardiyaFazlaMesaiBitZaman();
					}
				}
			} else
				vardiyaTelorans1BasZaman = vardiyaBasZaman;
			vardiyaBitZaman = PdksUtil.addTarih(vardiyaBasZaman, Calendar.HOUR_OF_DAY, 18);
			vardiyaTelorans2BasZaman = vardiyaBasZaman;
			// setVardiyaFazlaMesaiBasZaman(vardiyaBasZaman);

			vardiyaTelorans1BitZaman = vardiyaBitZaman;
			vardiyaTelorans2BitZaman = vardiyaBitZaman;
			setVardiyaFazlaMesaiBitZaman(vardiyaBitZaman);
			if (oncekiIslemVardiya != null && oncekiIslemVardiya.isCalisma()) {
				setVardiyaFazlaMesaiBasZaman(oncekiIslemVardiya.getVardiyaFazlaMesaiBitZaman());
				setVardiyaFazlaMesaiBitZaman(PdksUtil.addTarih(vardiyaFazlaMesaiBasZaman, Calendar.HOUR_OF_DAY, 18));
			}
			if (sonrakiIslemVardiya != null) {
				setVardiyaFazlaMesaiBitZaman(sonrakiIslemVardiya.getVardiyaFazlaMesaiBasZaman());

			}
		}

	}

	/**
	 * @param pdksVardiyaGun
	 * @param tarih
	 */
	private void vardiyaKontrol2(VardiyaGun pdksVardiyaGun, Date tarih) {
		if (pdksVardiyaGun.isZamanGuncelle()) {
			if (pdksVardiyaGun.getBasSaat() != null) {
				basSaat = pdksVardiyaGun.getBasSaat().shortValue();
				basDakika = pdksVardiyaGun.getBasDakika().shortValue();
			}
			if (pdksVardiyaGun.getBitSaat() != null) {
				bitSaat = pdksVardiyaGun.getBitSaat().shortValue();
				bitDakika = pdksVardiyaGun.getBitDakika().shortValue();
			}
		}
		long zamanBas = basSaat * 100 + basDakika;
		long zamanBit = bitSaat * 100 + bitDakika;
		Calendar cal = Calendar.getInstance();
		cal.setTime(tarih);
		Date sonGun = null;
		if (bitSaat > basSaat) {
			if (sonrakiVardiya != null && sonrakiVardiya.isCalisma() == false) {
				cal.setTime(tarih);
				cal.add(Calendar.MINUTE, sonrakiVardiya.isHaftaTatil() ? haftaTatiliFazlaMesaiBasDakika : offFazlaMesaiBasDakika);
				sonGun = cal.getTime();
				// sonGun = PdksUtil.tariheGunEkleCikar(tarih, 1);
			}

			if (sonGun != null)
				logger.debug(pdksVardiyaGun.getVardiyaKeyStr() + " " + sonrakiVardiya.getKisaAciklama());
		}
		if (isCalisma()) {
			cal = Calendar.getInstance();
			cal.setTime(tarih);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), bitSaat, bitDakika, 0);
			vardiyaBitZaman = cal.getTime();
			cal.setTime(tarih);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), basSaat, basDakika, 0);
			vardiyaBasZaman = cal.getTime();
			farkliGun = zamanBas >= zamanBit;
			if (farkliGun) {
				cal.setTime(vardiyaBitZaman);
				cal.add(Calendar.DATE, 1);
				vardiyaBitZaman = cal.getTime();
			} else if (zamanBas == zamanBit) {
				cal.setTime(vardiyaBitZaman);
				cal.add(Calendar.DATE, 1);
				vardiyaBitZaman = cal.getTime();

			}
			cal.setTime((Date) vardiyaBasZaman.clone());
			cal.add(Calendar.MINUTE, girisGecikmeToleransDakika);
			vardiyaTelorans2BasZaman = cal.getTime();
			cal.setTime((Date) vardiyaBasZaman.clone());
			cal.add(Calendar.MINUTE, -girisErkenToleransDakika);
			vardiyaTelorans1BasZaman = cal.getTime();

			cal.setTime((Date) vardiyaBitZaman.clone());
			cal.add(Calendar.MINUTE, cikisGecikmeToleransDakika);
			vardiyaTelorans2BitZaman = cal.getTime();
			cal.setTime((Date) vardiyaBitZaman.clone());
			cal.add(Calendar.MINUTE, -cikisErkenToleransDakika);
			vardiyaTelorans1BitZaman = cal.getTime();
			if (sonrakiVardiya == null && oncekiVardiya == null) {
				double sure = PdksUtil.getSaatFarki(vardiyaBitZaman, vardiyaBasZaman).doubleValue();
				int bosluk = new Double((36.0d - sure) / 2.0d).intValue();
				int basBosluk = bosluk / 2;
				setVardiyaFazlaMesaiBasZaman(PdksUtil.addTarih(vardiyaBasZaman, Calendar.HOUR_OF_DAY, -basBosluk));
				setVardiyaFazlaMesaiBitZaman(PdksUtil.addTarih(vardiyaBitZaman, Calendar.HOUR_OF_DAY, bosluk));
			} else {
				if (oncekiVardiya != null) {
					if (oncekiVardiya.isCalisma() || oncekiVardiya.getVardiyaBitZaman() != null) {
						double sure1 = PdksUtil.getSaatFarki(vardiyaBasZaman, oncekiVardiya.getVardiyaBitZaman()).doubleValue();
						if (sure1 > 0) {
							if (vardiyaBasZaman != null) {
								int bosluk = new Double(sure1 / 2.0d).intValue();
								if (bosluk > fazlaMesaiBasSaati)
									bosluk = fazlaMesaiBasSaati;
								int fBasDakika = bosluk * 60;
								if (fazlaMesaiBasDakika != null && fazlaMesaiBasDakika > 0 && bosluk * 60 > fazlaMesaiBasDakika)
									fBasDakika = fazlaMesaiBasDakika;
								Date basZaman = PdksUtil.addTarih(vardiyaBasZaman, Calendar.MINUTE, -fBasDakika);
								setVardiyaFazlaMesaiBasZaman(basZaman);
							} else
								logger.debug(oncekiVardiya.getAdi());

						}
					} else
						logger.debug(oncekiVardiya.getAdi());
				}
				if (sonrakiVardiya != null && sonrakiVardiya.getVardiyaBasZaman() != null) {
					if (sonGun != null) {
						setVardiyaFazlaMesaiBitZaman(sonGun);
						sonrakiVardiya.setVardiyaBasZaman(sonGun);
					} else if (vardiyaBitZaman != null) {
						double sure2 = PdksUtil.getSaatFarki(sonrakiVardiya.getVardiyaBasZaman(), vardiyaBitZaman).doubleValue();
						if (sure2 > 0) {
							int bosluk = sure2 > fazlaMesaiBasSaati ? new Double(sure2 - fazlaMesaiBasSaati).intValue() : new Double(sure2 / 2.0d).intValue();
							int fBasDakika = bosluk * 60;
							if (fazlaMesaiBasDakika != null && fazlaMesaiBasDakika > 0 && bosluk * 60 > fazlaMesaiBasDakika)
								fBasDakika = fazlaMesaiBasDakika;
							Date bitZaman = PdksUtil.addTarih(vardiyaBitZaman, Calendar.MINUTE, fBasDakika);
							setVardiyaFazlaMesaiBitZaman(bitZaman);
						}
					}

				}
			}

		} else if (tarih != null) {
			Vardiya oncekiIslemVardiya = pdksVardiyaGun.getOncekiVardiyaGun() != null && pdksVardiyaGun.getOncekiVardiyaGun().getIslemVardiya() != null ? pdksVardiyaGun.getOncekiVardiyaGun().getIslemVardiya() : null;
			Vardiya sonrakiIslemVardiya = pdksVardiyaGun.getSonrakiVardiyaGun() != null && pdksVardiyaGun.getSonrakiVardiyaGun().getIslemVardiya().isCalisma() ? pdksVardiyaGun.getSonrakiVardiyaGun().getIslemVardiya() : null;
			vardiyaBasZaman = PdksUtil.convertToJavaDate(PdksUtil.convertToDateString(tarih, "yyyyMMdd") + " 13:00", "yyyyMMdd HH:mm");
			cal = Calendar.getInstance();
			cal.setTime(tarih);

			if (oncekiIslemVardiya != null && oncekiIslemVardiya.getVardiyaBasZaman() != null) {
				if (oncekiIslemVardiya.isCalisma()) {
					if (vardiyaBasZaman.after(oncekiIslemVardiya.getVardiyaBitZaman())) {
						Double sure2 = PdksUtil.getSaatFarki(vardiyaBasZaman, oncekiIslemVardiya.getVardiyaBitZaman()).doubleValue();
						int fBasDakika = sure2.intValue() * 60;
						if (sure2 > fazlaMesaiBasSaati) {
							fBasDakika -= fazlaMesaiBasSaati * 60;

						}
						if (fazlaMesaiBasDakika != null && fazlaMesaiBasDakika > 0 && sure2 * 60 > fazlaMesaiBasSaati)
							fBasDakika = -fazlaMesaiBasDakika;
						cal = Calendar.getInstance();
						cal.setTime(oncekiIslemVardiya.getVardiyaBitZaman());
						cal.add(Calendar.MINUTE, fBasDakika);
						Date fazMesBitZaman = cal.getTime();
						// if
						// (fazMesBitZaman.before(pdksVardiyaGun.getVardiyaDate()))
						// fazMesBitZaman = pdksVardiyaGun.getVardiyaDate();
						if (vardiyaBasZaman.after(fazMesBitZaman)) {
							// vardiyaBasZaman = fazMesBitZaman;
							oncekiIslemVardiya.setVardiyaFazlaMesaiBitZaman(fazMesBitZaman);
						}
					}
				}
			}
			vardiyaBasZaman = pdksVardiyaGun.getVardiyaDate();
			int bosluk = 0;
			if (!this.isCalisma())
				bosluk = this.isHaftaTatil() ? haftaTatiliFazlaMesaiBasDakika : offFazlaMesaiBasDakika;
			vardiyaTelorans1BasZaman = this.isCalisma() ? PdksUtil.addTarih(vardiyaBasZaman, Calendar.HOUR, -1) : PdksUtil.addTarih(vardiyaBasZaman, Calendar.MINUTE, bosluk);
			if (oncekiIslemVardiya != null) {
				if (oncekiIslemVardiya.isCalisma() && oncekiIslemVardiya.getVardiyaFazlaMesaiBitZaman() != null && vardiyaTelorans1BasZaman.after(oncekiIslemVardiya.getVardiyaFazlaMesaiBitZaman())) {
					if (vardiyaTelorans1BasZaman.after(oncekiIslemVardiya.getVardiyaFazlaMesaiBitZaman())) {
						oncekiIslemVardiya.setVardiyaFazlaMesaiBitZaman(vardiyaTelorans1BasZaman);
					} else {
						vardiyaTelorans1BasZaman = oncekiIslemVardiya.getVardiyaFazlaMesaiBitZaman();
					}
				}
			} else
				vardiyaTelorans1BasZaman = vardiyaBasZaman;
			vardiyaBitZaman = PdksUtil.addTarih(vardiyaBasZaman, Calendar.HOUR_OF_DAY, 14);
			vardiyaTelorans2BasZaman = vardiyaBasZaman;
			// setVardiyaFazlaMesaiBasZaman(vardiyaBasZaman);

			vardiyaTelorans1BitZaman = vardiyaBitZaman;
			vardiyaTelorans2BitZaman = vardiyaBitZaman;
			setVardiyaFazlaMesaiBitZaman(vardiyaBitZaman);
			if (oncekiIslemVardiya != null && oncekiIslemVardiya.isCalisma()) {
				setVardiyaFazlaMesaiBasZaman(oncekiIslemVardiya.getVardiyaFazlaMesaiBitZaman());
				setVardiyaFazlaMesaiBitZaman(PdksUtil.addTarih(vardiyaFazlaMesaiBasZaman, Calendar.HOUR_OF_DAY, 14));
			}
			if (sonrakiIslemVardiya != null) {
				setVardiyaFazlaMesaiBitZaman(sonrakiIslemVardiya.getVardiyaFazlaMesaiBasZaman());

			}
		}

	}

	@Transient
	public Date getBasZaman() {
		Date zaman = this.isCalisma() ? User.getTime(basSaat, basDakika) : null;
		return zaman;
	}

	@Transient
	public Date getBitZaman() {
		Date zaman = this.isCalisma() ? User.getTime(bitSaat, bitDakika) : null;
		return zaman;
	}

	@Transient
	public void setVardiyaZamani(Date tarih) {
		if (tarih != null) {
			long zamanBas = basSaat * 100 + basDakika;
			long zamanBit = bitSaat * 100 + bitDakika;
			if (isCalisma()) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(tarih);
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), bitSaat, bitDakika, 0);
				vardiyaBitZaman = cal.getTime();
				cal.setTime(tarih);
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), basSaat, basDakika, 0);
				vardiyaBasZaman = cal.getTime();
				if (zamanBas > zamanBit) {
					cal.setTime(vardiyaBasZaman);
					cal.add(Calendar.DATE, -1);
					vardiyaBasZaman = cal.getTime();
				} else if (zamanBas == zamanBit) {
					cal.setTime(vardiyaBitZaman);
					cal.add(Calendar.DATE, 1);
					vardiyaBitZaman = cal.getTime();

				}
				cal.setTime((Date) vardiyaBasZaman.clone());
				cal.add(Calendar.MINUTE, girisGecikmeToleransDakika);
				vardiyaTelorans2BasZaman = cal.getTime();
				cal.setTime((Date) vardiyaBasZaman.clone());
				cal.add(Calendar.MINUTE, -girisErkenToleransDakika);
				vardiyaTelorans1BasZaman = cal.getTime();

				cal.setTime((Date) vardiyaBitZaman.clone());
				cal.add(Calendar.MINUTE, cikisGecikmeToleransDakika);
				vardiyaTelorans2BitZaman = cal.getTime();
				cal.setTime((Date) vardiyaBitZaman.clone());
				cal.add(Calendar.MINUTE, cikisErkenToleransDakika);
				vardiyaTelorans1BitZaman = cal.getTime();
				setVardiyaFazlaMesaiBasZaman(PdksUtil.addTarih(vardiyaBasZaman, Calendar.HOUR_OF_DAY, -6));
				setVardiyaFazlaMesaiBitZaman(PdksUtil.addTarih(vardiyaBitZaman, Calendar.HOUR_OF_DAY, 6));

			} else {
				Calendar cal = Calendar.getInstance();
				cal.setTime(tarih);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				vardiyaBasZaman = cal.getTime();
				vardiyaTelorans1BasZaman = vardiyaBasZaman;
				vardiyaTelorans2BasZaman = vardiyaBasZaman;
				setVardiyaFazlaMesaiBasZaman(vardiyaBasZaman);
				cal.add(Calendar.DATE, 1);
				vardiyaBitZaman = cal.getTime();
				vardiyaTelorans1BitZaman = vardiyaBitZaman;
				vardiyaTelorans2BitZaman = vardiyaBitZaman;
				setVardiyaFazlaMesaiBitZaman(vardiyaBitZaman);
			}
		}
		setVardiyaTarih(tarih);

	}

	@Transient
	public Date getVardiyaTelorans1BasZaman() {
		return vardiyaTelorans1BasZaman;
	}

	public void setVardiyaTelorans1BasZaman(Date vardiyaTelorans1BasZaman) {
		this.vardiyaTelorans1BasZaman = vardiyaTelorans1BasZaman;
	}

	@Transient
	public Date getVardiyaTelorans2BasZaman() {
		return vardiyaTelorans2BasZaman;
	}

	public void setVardiyaTelorans2BasZaman(Date vardiyaTelorans2BasZaman) {
		this.vardiyaTelorans2BasZaman = vardiyaTelorans2BasZaman;
	}

	@Transient
	public Date getVardiyaTelorans1BitZaman() {
		return vardiyaTelorans1BitZaman;
	}

	public void setVardiyaTelorans1BitZaman(Date vardiyaTelorans1BitZaman) {
		this.vardiyaTelorans1BitZaman = vardiyaTelorans1BitZaman;
	}

	@Transient
	public Date getVardiyaTelorans2BitZaman() {
		return vardiyaTelorans2BitZaman;
	}

	public void setVardiyaTelorans2BitZaman(Date value) {
		if (value != null) {
			if (vardiyaDateStr != null && vardiyaDateStr.equals("20221106")) {
				// ++islemAdet;
				logger.debug(islemAdet + " " + value);
			}

		}
		this.vardiyaTelorans2BitZaman = value;
	}

	@Transient
	public Date getVardiyaFazlaMesaiBasZaman() {
		return vardiyaFazlaMesaiBasZaman;
	}

	public void setVardiyaFazlaMesaiBasZaman(Date value) {
		if (value != null) {
			if (vardiyaDateStr != null && vardiyaDateStr.equals("20221130")) {
				logger.debug(islemAdet + " " + value);
			}
			this.vardiyaFazlaMesaiBasZaman = value;
		}

	}

	@Transient
	public Date getVardiyaFazlaMesaiBitZaman() {
		return vardiyaFazlaMesaiBitZaman;
	}

	public void setVardiyaFazlaMesaiBitZaman(Date value) {
		if (value != null) {
			if (vardiyaDateStr != null && vardiyaDateStr.equals("20221106")) {
				++islemAdet;
				logger.debug(islemAdet + " " + value);
			}
			this.vardiyaFazlaMesaiBitZaman = value;
		}

	}

	@Transient
	public Date getVardiyaTarih() {
		return vardiyaTarih;
	}

	public void setVardiyaTarih(Date value) {
		this.vardiyaTarih = value;
		this.vardiyaDateStr = value != null ? PdksUtil.convertToDateString(value, "yyyyMMdd") : null;
	}

	@Transient
	public double getNetCalismaSuresi() {
		double sure = 0;
		if (isCalisma()) {
			Calendar cal = Calendar.getInstance();
			Date tarih = cal.getTime();
			cal.setTime(tarih);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), bitSaat, bitDakika, 0);
			Date bitZaman = cal.getTime();
			cal.setTime(tarih);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), basSaat, basDakika, 0);
			Date basZaman = cal.getTime();
			if (basZaman.getTime() > bitZaman.getTime()) {
				cal.setTime(basZaman);
				cal.add(Calendar.DATE, -1);
				basZaman = cal.getTime();
			}

			double vardiyaCalismaDakika = PdksUtil.getDakikaFarkiHesapla(bitZaman, basZaman).doubleValue();
			sure = (vardiyaCalismaDakika - ((double) (yemekSuresi != null ? yemekSuresi.doubleValue() : 0d))) / 60;

		}
		return sure;
	}

	@Transient
	public boolean isFarkliGun() {
		return farkliGun;
	}

	public void setFarkliGun(boolean farkliGun) {
		this.farkliGun = farkliGun;
	}

	@Column(name = "GIRISERKENTOLERANSDAKIKA")
	public short getGirisErkenToleransDakika() {
		return girisErkenToleransDakika;
	}

	public void setGirisErkenToleransDakika(short girisErkenToleransDakika) {
		this.girisErkenToleransDakika = girisErkenToleransDakika;
	}

	@Column(name = "GIRISGECIKMETOLERANSDAKIKA")
	public short getGirisGecikmeToleransDakika() {
		return girisGecikmeToleransDakika;
	}

	public void setGirisGecikmeToleransDakika(short girisGecikmeToleransDakika) {
		this.girisGecikmeToleransDakika = girisGecikmeToleransDakika;
	}

	@Column(name = "CIKISERKENTOLERANSDAKIKA")
	public short getCikisErkenToleransDakika() {
		return cikisErkenToleransDakika;
	}

	public void setCikisErkenToleransDakika(short cikisErkenToleransDakika) {
		this.cikisErkenToleransDakika = cikisErkenToleransDakika;
	}

	@Column(name = "CIKISGECIKMETOLERANSDAKIKA")
	public short getCikisGecikmeToleransDakika() {
		return cikisGecikmeToleransDakika;
	}

	public void setCikisGecikmeToleransDakika(short cikisGecikmeToleransDakika) {
		this.cikisGecikmeToleransDakika = cikisGecikmeToleransDakika;
	}

	@Transient
	public Vardiya getSonrakiVardiya() {
		return sonrakiVardiya;
	}

	public void setSonrakiVardiya(Vardiya sonrakiVardiya) {
		this.sonrakiVardiya = sonrakiVardiya;
	}

	@Transient
	public Vardiya getOncekiVardiya() {
		return oncekiVardiya;
	}

	public void setOncekiVardiya(Vardiya oncekiVardiya) {
		this.oncekiVardiya = oncekiVardiya;
	}

	@Transient
	public String getKisaAciklama() {
		return kisaAdi != null && kisaAdi.trim().length() > 0 ? kisaAdi : getVardiyaAciklama();
	}

	@Transient
	public List<Integer> getGunlukList() {
		return gunlukList;
	}

	@Transient
	public boolean isAksamVardiyasi() {
		return aksamVardiya != null && aksamVardiya.booleanValue();
	}

	@Transient
	public boolean isIcapVardiyasi() {
		boolean icapVardiyasi = Boolean.FALSE;
		if (icapVardiya != null) {
			icapVardiyasi = icapVardiya.booleanValue();
			// if (!icapVardiyasi && kisaAdi != null)
			// icapVardiyasi = kisaAdi.equals("Gİ") || kisaAdi.equals("Sİ") ||
			// kisaAdi.equals("Aİ");
		}

		return icapVardiyasi;
	}

	public void setGunlukList(List<Integer> gunlukList) {
		this.gunlukList = gunlukList;
	}

	@Transient
	public String getVardiyaAdi() {
		String vardiyaAdi = null;
		Vardiya vTemp = this;
		vardiyaAdi = vTemp.getKisaAdi();
		if (vTemp.isCalisma()) {
			VardiyaGun tmp = new VardiyaGun(new Personel(), vTemp, new Date());
			tmp.setVardiyaZamani();
			if (vTemp.isCalisma()) {
				String pattern = PdksUtil.getSaatFormat();
				Vardiya tmpVardiya = tmp.getIslemVardiya();
				vardiyaAdi = PdksUtil.convertToDateString(tmpVardiya.getVardiyaBasZaman(), pattern) + " - " + PdksUtil.convertToDateString(tmpVardiya.getVardiyaBitZaman(), pattern) + " [ " + vTemp.getKisaAdi() + " ] ";
			}

			tmp = null;
		}

		return vardiyaAdi;
	}

	@Transient
	public boolean isMesaiOdenir() {
		return isCalisma() && mesaiOde != null && mesaiOde.booleanValue();
	}

	@Transient
	public boolean isGebelikMi() {
		return gebelik != null && gebelik.booleanValue();
	}

	@Transient
	public String getKisaAdiSort() {
		String kod = "20";
		if (genel) {
			kod = "10";
			if (isCalisma())
				kod = "00";

		}
		String aciklamaStr = (PdksUtil.textBaslangicinaKarakterEkle((ekranSira != null ? String.valueOf(ekranSira) : "0"), '0', 6)) + kod + (kisaAdi != null ? kisaAdi : "ZZZZZ");
		return aciklamaStr;
	}

	@Transient
	public String getOzelAdi() {
		VardiyaGun vg = new VardiyaGun();
		String ozelAdi = vg.getVardiyaAdi(this);
		vg = null;
		return ozelAdi;
	}

	@Transient
	public long getVardiyaNumeric() {
		long vardiyaNumeric = id;
		if (isCalisma()) {
			vardiyaNumeric = (basSaat * 1000000) + (basDakika * 10000) + (bitSaat * 100) + bitDakika;
		}
		return vardiyaNumeric;
	}

	public boolean equals(Vardiya obj) {
		boolean eq = Boolean.FALSE;
		if (obj != null)
			eq = this.id != null && this.id.equals(obj.getId());
		else
			eq = this.id == null;
		return eq;

	}

	@Transient
	public Date getArifeBaslangicTarihi() {
		return arifeBaslangicTarihi;
	}

	public void setArifeBaslangicTarihi(Date arifeBaslangicTarihi) {
		this.arifeBaslangicTarihi = arifeBaslangicTarihi;
	}

	@Transient
	public List<YemekIzin> getYemekIzinList() {
		return yemekIzinList;
	}

	public void setYemekIzinList(List<YemekIzin> yemekIzinList) {
		this.yemekIzinList = yemekIzinList;
	}

	@Transient
	public static Date getVardiyaKontrolTarih() {
		return vardiyaKontrolTarih;
	}

	public static void setVardiyaKontrolTarih(Date vardiyaKontrolTarih) {
		Vardiya.vardiyaKontrolTarih = vardiyaKontrolTarih;
	}

	public static Date getVardiyaKontrolTarih2() {
		return vardiyaKontrolTarih2;
	}

	public static void setVardiyaKontrolTarih2(Date vardiyaKontrolTarih2) {
		Vardiya.vardiyaKontrolTarih2 = vardiyaKontrolTarih2;
	}

	public static Integer getFazlaMesaiBasSaati() {
		return fazlaMesaiBasSaati;
	}

	public static void setFazlaMesaiBasSaati(Integer fazlaMesaiBasSaati) {
		Vardiya.fazlaMesaiBasSaati = fazlaMesaiBasSaati;
	}

	public static Integer getOffFazlaMesaiBasDakika() {
		return offFazlaMesaiBasDakika;
	}

	public static void setOffFazlaMesaiBasDakika(Integer offFazlaMesaiBasDakika) {
		Vardiya.offFazlaMesaiBasDakika = offFazlaMesaiBasDakika;
	}

	public static Integer getHaftaTatiliFazlaMesaiBasDakika() {
		return haftaTatiliFazlaMesaiBasDakika;
	}

	public static void setHaftaTatiliFazlaMesaiBasDakika(Integer haftaTatiliFazlaMesaiBasDakika) {
		Vardiya.haftaTatiliFazlaMesaiBasDakika = haftaTatiliFazlaMesaiBasDakika;
	}

	public static Date getVardiyaKontrolTarih3() {
		return vardiyaKontrolTarih3;
	}

	public static void setVardiyaKontrolTarih3(Date vardiyaKontrolTarih3) {
		Vardiya.vardiyaKontrolTarih3 = vardiyaKontrolTarih3;
	}

	@Transient
	public boolean isIsKurMu() {
		return isKur != null && isKur;
	}

	public static Date getVardiyaAySonuKontrolTarih() {
		return vardiyaAySonuKontrolTarih;
	}

	public static void setVardiyaAySonuKontrolTarih(Date vardiyaAySonuKontrolTarih) {
		Vardiya.vardiyaAySonuKontrolTarih = vardiyaAySonuKontrolTarih;
	}

	@Transient
	public boolean isAyinSonGunDurum() {
		return ayinSonGunDurum;
	}

	public void setAyinSonGunDurum(boolean ayinSonGunDurum) {
		this.ayinSonGunDurum = ayinSonGunDurum;
	}

	@Transient
	public String getVardiyaDateStr() {
		return vardiyaDateStr;
	}

	public void setVardiyaDateStr(String vardiyaDateStr) {
		this.vardiyaDateStr = vardiyaDateStr;
	}

	@Transient
	public int getIslemAdet() {
		return islemAdet;
	}

	public void setIslemAdet(int islemAdet) {
		this.islemAdet = islemAdet;
	}
}
