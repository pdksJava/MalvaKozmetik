package com.pdks.entity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.Min;

import com.pdks.genel.model.PdksUtil;

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
	public static final String COLUMN_NAME_GEBELIK = "GEBELIK";
	public static final String COLUMN_NAME_ICAP = "ICAP_VARDIYA";
	public static final String COLUMN_NAME_KISA_ADI = "KISA_ADI";
	public static final String COLUMN_NAME_GENEL = "GENEL";
	public static final String COLUMN_NAME_DEPARTMAN = "DEPARTMAN_ID";

	public static final char TIPI_CALISMA = ' ';
	public static final char TIPI_HAFTA_TATIL = 'H';
	public static final char TIPI_OFF = 'O';
	public static final char TIPI_RADYASYON_IZNI = 'R';
	public static final char TIPI_FAZLA_MESAI = 'F';

	private String adi, kisaAdi, styleClass;
	private short basDakika, basSaat, bitDakika, bitSaat, girisErkenToleransDakika, girisGecikmeToleransDakika, cikisErkenToleransDakika, cikisGecikmeToleransDakika;
	private double calismaSaati;
	private int calismaGun;
	private Integer yemekSuresi, cikisMolaSaat = 0;
	private Departman departman;
	private List<Integer> gunlukList;
	private Boolean aksamVardiya = Boolean.FALSE, icapVardiya = Boolean.FALSE, gebelik = Boolean.FALSE, genel = Boolean.FALSE;
	private String tipi;

	private char vardiyaTipi;
	private Date vardiyaBasZaman, vardiyaBitZaman, vardiyaTarih;
	private Date vardiyaTelorans1BasZaman, vardiyaTelorans2BasZaman, vardiyaTelorans1BitZaman, vardiyaTelorans2BitZaman;
	private Date vardiyaFazlaMesaiBasZaman, vardiyaFazlaMesaiBitZaman;
	private boolean farkliGun = Boolean.FALSE;
	private Boolean mesaiOde, sua = Boolean.FALSE;
	private Vardiya sonrakiVardiya, oncekiVardiya;
	private Integer version = 0;

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
	public String getTipi() {
		return tipi;
	}

	public void setTipi(String tipi) {
		this.vardiyaTipi = tipi.charAt(0);
		this.tipi = tipi;
	}

	@Transient
	public String getVardiyaTipiAciklama() {
		return getVardiyaTipiAciklama(vardiyaTipi);
	}

	@Transient
	public static String getVardiyaTipiAciklama(char tipi) {
		String aciklama = "";
		switch (tipi) {
		case TIPI_CALISMA:
			aciklama = "Normal Çalışma";
			break;
		case TIPI_HAFTA_TATIL:
			aciklama = "HT";
			break;
		case TIPI_OFF:
			aciklama = "Off";
			break;
		case TIPI_RADYASYON_IZNI:
			aciklama = "Radyasyon İzni";
			break;
		case TIPI_FAZLA_MESAI:
			aciklama = "Fazla Mesai";
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
		String aciklama = isCalisma() ? getBasSaatDakikaStr() + "-" + getBitSaatDakikaStr() : getVardiyaTipiAciklama();
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
	public boolean isOffGun() {
		return vardiyaTipi == TIPI_OFF;
	}

	@Transient
	public boolean isRadyasyonIzni() {
		return vardiyaTipi == TIPI_RADYASYON_IZNI;
	}

	@Transient
	public boolean isFazlaMesai() {
		return vardiyaTipi == TIPI_FAZLA_MESAI;
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

	public void setVardiyaBitZaman(Date vardiyaBitZaman) {
		this.vardiyaBitZaman = vardiyaBitZaman;
	}

	@Transient
	public void setVardiyaZamani(VardiyaGun pdksVardiyaGun) {
		Date tarih = pdksVardiyaGun.getVardiyaDate();
		if (tarih != null) {
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
						double sure2 = PdksUtil.getSaatFarki(sonrakiVardiya.getVardiyaBasZaman(), vardiyaBitZaman).doubleValue();
						if (sure2 > 0 && vardiyaBitZaman != null) {
							int bosluk = new Double(sure2 / 2.0d).intValue();
							Date bitZaman = PdksUtil.addTarih(vardiyaBitZaman, Calendar.HOUR_OF_DAY, bosluk);
							setVardiyaFazlaMesaiBitZaman(bitZaman);
						}
					}
				}

			} else if (tarih != null) {
				Vardiya oncekiIslemVardiya = pdksVardiyaGun.getOncekiVardiya() != null && pdksVardiyaGun.getOncekiVardiya().getIslemVardiya() != null ? pdksVardiyaGun.getOncekiVardiya().getIslemVardiya() : null;
				Vardiya sonrakiIslemVardiya = pdksVardiyaGun.getSonrakiVardiya() != null && pdksVardiyaGun.getSonrakiVardiya().isCalisma() ? pdksVardiyaGun.getSonrakiVardiya() : null;
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
		setVardiyaTarih(tarih);

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

	public void setVardiyaTelorans2BitZaman(Date vardiyaTelorans2BitZaman) {
		this.vardiyaTelorans2BitZaman = vardiyaTelorans2BitZaman;
	}

	@Transient
	public Date getVardiyaFazlaMesaiBasZaman() {
		return vardiyaFazlaMesaiBasZaman;
	}

	public void setVardiyaFazlaMesaiBasZaman(Date value) {

		if (value != null) {

			this.vardiyaFazlaMesaiBasZaman = value;
		}

	}

	@Transient
	public Date getVardiyaFazlaMesaiBitZaman() {
		return vardiyaFazlaMesaiBitZaman;
	}

	public void setVardiyaFazlaMesaiBitZaman(Date value) {
		if (value != null) {

			this.vardiyaFazlaMesaiBitZaman = value;
		}

	}

	@Transient
	public Date getVardiyaTarih() {
		return vardiyaTarih;
	}

	public void setVardiyaTarih(Date vardiyaTarih) {
		this.vardiyaTarih = vardiyaTarih;
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
			sure = PdksUtil.getSaatFarki(bitZaman, basZaman).doubleValue() - ((double) (yemekSuresi != null ? yemekSuresi.doubleValue() : 0d) / 60);

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
		return PdksUtil.hasStringValue(kisaAdi) ? kisaAdi : getVardiyaAciklama();
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
			// icapVardiyasi = kisaAdi.equals("Gİ") || kisaAdi.equals("Sİ") || kisaAdi.equals("Aİ");
		}

		return icapVardiyasi;
	}

	public void setGunlukList(List<Integer> gunlukList) {
		this.gunlukList = gunlukList;
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
		String aciklamaStr = kod + (kisaAdi != null ? kisaAdi : "ZZZZZ");
		logger.debug(aciklamaStr + " " + adi);
		return aciklamaStr;
	}

	public boolean equals(Vardiya obj) {
		boolean eq = Boolean.FALSE;
		if (obj != null)
			eq = this.id != null && this.id.equals(obj.getId());
		else
			eq = this.id == null;
		return eq;

	}

}
