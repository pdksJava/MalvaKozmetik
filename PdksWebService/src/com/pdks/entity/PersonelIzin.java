package com.pdks.entity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.pdks.genel.model.PdksUtil;

@Entity(name = PersonelIzin.TABLE_NAME)
public class PersonelIzin extends BaseObject {
	// seam-gen attributes (you should probably edit these)

	/**
	 * 
	 */
	private static final long serialVersionUID = -1508779437229330518L;
	static Logger logger = Logger.getLogger(PersonelIzin.class);

	public static final String TABLE_NAME = "PERSONELIZIN";
	public static final String COLUMN_NAME_PERSONEL = "PERSONEL_ID";
	public static final String COLUMN_NAME_IZIN_TIPI = "IZIN_TIPI_ID";
	public static final String COLUMN_NAME_BASLANGIC_ZAMANI = "BASLANGIC_ZAMANI";
	public static final String COLUMN_NAME_BITIS_ZAMANI = "BITIS_ZAMANI";
	public static final String COLUMN_NAME_IZIN_SURESI = "IZIN_SURESI";
	public static final String COLUMN_NAME_IZIN_DURUMU = "IZIN_DURUMU";
	public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";
	public static final String COLUMN_NAME_IZIN_KAGIDI_GELDI = "IZIN_KAGIDI_GELDI";
	public static final String COLUMN_NAME_VERSION = "VERSION";
	public static final String COLUMN_NAME_PERSONEL_NO = "PERSONEL_NO";

	public static final int IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA = 1;
	public static final int IZIN_DURUMU_IKINCI_YONETICI_ONAYINDA = 2;
	public static final int IZIN_DURUMU_IK_ONAYINDA = 3;
	public static final int IZIN_DURUMU_ONAYLANDI = 4;
	public static final int IZIN_DURUMU_SAP_GONDERILDI = 5;
	public static final int IZIN_DURUMU_SISTEM_IPTAL = 8;
	public static final int IZIN_DURUMU_REDEDILDI = 9;
	public static final int HESAP_TIPI_GUN = 1;
	public static final int HESAP_TIPI_SAAT = 2;
	public static final int HESAP_TIPI_GUN_SAAT_SECILDI = 3;
	public static final int HESAP_TIPI_SAAT_GUN_SECILDI = 4;
	public static final int ACIKLAMA_UZUNLUK = 256;

	public static final String IZIN_DURUMU_ACIKLAMA_BIRINCI_YONETICI_ONAYINDA = "Birinci Yönetici Onayında";
	public static final String IZIN_DURUMU_ACIKLAMA_IKINCI_YONETICI_ONAYINDA = "İkinci Yönetici Onayında";
	public static final String IZIN_DURUMU_ACIKLAMA_GENEL_MUDUR_ONAYINDA = "Genel Müdür Onayında";
	public static final String IZIN_DURUMU_ACIKLAMA_IK_ONAYINDA = "IK Onayında";
	public static final String IZIN_DURUMU_ACIKLAMA_ONAYLANDI = "Onaylandı";
	public static final String IZIN_DURUMU_ACIKLAMA_SAP_GONDERILDI = "SAP Gönderildi";
	public static final String IZIN_DURUMU_ACIKLAMA_REDEDILDI = "Reddedildi";
	public static final String IZIN_DURUMU_ACIKLAMA_IPTAL_EDILDI = "İptal Edildi";
	public static final String IZIN_DURUMU_ACIKLAMA_SISTEM_IPTAL = "Sistem Güncelleme İptali";
	private static int yillikIzinMaxBakiye, fazlaMesaiSure, suaIzinMaxBakiye;
	// private static int mazeretIzniToplamSaat, icEgitimToplamGun,
	// disEgitimToplamGun, yurtIciKongre, yurtDisiKongre;

	private Integer version = 0;

	private Date baslangicZamani = PdksUtil.ayinIlkGunu(), bitisZamani = PdksUtil.ayinSonGunu(), filtreBaslangicZamani, filtreBitisZamani, hakedisTarih, birOncekiHakedisTarih;

	private Double izinSuresi = 0D, kullanilanIzinSuresi = 0D, bakiyeSuresi;

	private int izinDurumu = IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA;

	private String aciklama, mesaj, personelNo;

	private Personel izinSahibi;

	private IzinTipi izinTipi;

	private Boolean izinKagidiGeldi, yilbasi = Boolean.FALSE;

	private PersonelIzin kontrolIzin;

	private Integer hesapTipi, sayfaNo = 0;

	private Tanim gorevTipi;

	private boolean gunlukOldu = Boolean.FALSE, iptalEdilir = Boolean.TRUE;

	private Date donemSonu;

	private boolean islemYapildi = Boolean.FALSE;

	private List<PersonelIzin> harcananDigerIzinler;

	@Column(name = COLUMN_NAME_VERSION)
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Personel getIzinSahibi() {
		return izinSahibi;
	}

	public void setIzinSahibi(Personel value) {
		if (this.isDegisti() == false)
			this.setDegisti(PdksUtil.isLongDegisti(value.getId(), izinSahibi.getId()));
		this.izinSahibi = value;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "GOREV_TIPI_ID", nullable = true)
	@Fetch(FetchMode.JOIN)
	public Tanim getGorevTipi() {
		return gorevTipi;
	}

	public void setGorevTipi(Tanim value) {
		if (this.isDegisti() == false)
			this.setDegisti(PdksUtil.isTanimDegisti(value, gorevTipi));
		this.gorevTipi = value;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_IZIN_TIPI, nullable = false)
	@Fetch(FetchMode.JOIN)
	public IzinTipi getIzinTipi() {
		return izinTipi;
	}

	public void setIzinTipi(IzinTipi value) {
		if (this.isDegisti() == false)
			this.setDegisti(PdksUtil.isLongDegisti(value.getId(), izinTipi.getId()));
		this.izinTipi = value;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_BASLANGIC_ZAMANI, nullable = false)
	public Date getBaslangicZamani() {
		return baslangicZamani;
	}

	public void setBaslangicZamani(Date value) {
		if (this.isDegisti() == false)
			this.setDegisti(PdksUtil.isDateDegisti(value, baslangicZamani));
		this.baslangicZamani = value;
	}

	@Column(name = "HESAP_TIPI")
	public Integer getHesapTipi() {
		return hesapTipi;
	}

	public void setHesapTipi(Integer value) {
		if (this.isDegisti() == false)
			this.setDegisti(PdksUtil.isIntegerDegisti(hesapTipi, value));
		this.hesapTipi = value;
	}

	@Column(name = COLUMN_NAME_PERSONEL_NO, insertable = false, updatable = false)
	public String getPersonelNo() {
		return personelNo;
	}

	public void setPersonelNo(String personelNo) {
		this.personelNo = personelNo;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_BITIS_ZAMANI, nullable = false)
	public Date getBitisZamani() {
		return bitisZamani;
	}

	public void setBitisZamani(Date value) {
		if (this.isDegisti() == false)
			this.setDegisti(PdksUtil.isDateDegisti(value, bitisZamani));
		this.bitisZamani = value;
	}

	@Column(name = COLUMN_NAME_IZIN_SURESI, nullable = false)
	public Double getIzinSuresi() {
		return izinSuresi;
	}

	public void setIzinSuresi(Double value) {
		if (this.isDegisti() == false)
			this.setDegisti(PdksUtil.isDoubleDegisti(value, izinSuresi));
		this.izinSuresi = value;
	}

	@Column(name = "KULLANILAN_IZIN_SURESI")
	public Double getKullanilanIzinSuresi() {
		return kullanilanIzinSuresi;
	}

	public void setKullanilanIzinSuresi(Double kullanilanIzinSuresi) {
		this.kullanilanIzinSuresi = kullanilanIzinSuresi;
	}

	@Column(name = "IZIN_KAGIDI_GELDI")
	public Boolean getIzinKagidiGeldi() {
		return izinKagidiGeldi;
	}

	public void setIzinKagidiGeldi(Boolean izinKagidiGeldi) {
		this.izinKagidiGeldi = izinKagidiGeldi;
	}

	@Column(name = COLUMN_NAME_IZIN_DURUMU)
	public int getIzinDurumu() {
		return izinDurumu;
	}

	public void setIzinDurumu(int value) {
		if (this.isDegisti() == false)
			this.setDegisti(PdksUtil.isIntegerDegisti(value, izinDurumu));
		this.izinDurumu = value;
	}

	@Column(name = COLUMN_NAME_ACIKLAMA, length = ACIKLAMA_UZUNLUK)
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String value) {
		if (this.isDegisti() == false)
			this.setDegisti(PdksUtil.isStrDegisti(value, aciklama));
		this.aciklama = value;
	}

	@Transient
	public Date getFiltreBaslangicZamani() {
		return filtreBaslangicZamani;
	}

	public void setFiltreBaslangicZamani(Date filtreBaslangicZamani) {
		this.filtreBaslangicZamani = filtreBaslangicZamani;
	}

	@Transient
	public Date getFiltreBitisZamani() {
		return filtreBitisZamani;
	}

	public void setFiltreBitisZamani(Date filtreBitisZamani) {
		this.filtreBitisZamani = filtreBitisZamani;
	}

	@Transient
	public String getIzinDonem() {
		int yil = PdksUtil.getDateField(baslangicZamani, Calendar.YEAR);
		return yil >= 1980 ? String.valueOf(yil) : "Önceki yıllardan devir";
	}

	@Transient
	public boolean isEditEdilebilir(User user) {
		return getIzinDurumu() == IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA;

	}

	@Transient
	public boolean isGunlukOldu() {
		return gunlukOldu;
	}

	public void setGunlukOldu(boolean gunlukOldu) {
		this.gunlukOldu = gunlukOldu;
	}

	@Transient
	public boolean egitimIznimi() {
		if (izinTipi.getIzinTipiTanim().getKodu().equals(IzinTipi.EGITIM_IC) || izinTipi.getIzinTipiTanim().getKodu().equals(IzinTipi.EGITIM_DIS))
			return true;
		else
			return false;
	}

	@Transient
	public boolean isOnaylandi() {
		return izinDurumu == IZIN_DURUMU_ONAYLANDI;
	}

	@Transient
	public boolean isRedmi() {

		return izinDurumu == IZIN_DURUMU_REDEDILDI || izinDurumu == IZIN_DURUMU_SISTEM_IPTAL;
	}

	@Transient
	public boolean isPdfMi() {
		return izinTipi.getDokumAlmaDurum() && (izinDurumu == IZIN_DURUMU_IK_ONAYINDA || izinDurumu == IZIN_DURUMU_ONAYLANDI || izinDurumu == IZIN_DURUMU_SAP_GONDERILDI);
	}

	public static int getYillikIzinMaxBakiye() {
		return yillikIzinMaxBakiye;
	}

	public static void setYillikIzinMaxBakiye(int yillikIzinMaxBakiye) {
		PersonelIzin.yillikIzinMaxBakiye = yillikIzinMaxBakiye;
	}

	public static int getFazlaMesaiSure() {
		return fazlaMesaiSure;
	}

	public static void setFazlaMesaiSure(int fazlaMesaiSure) {
		PersonelIzin.fazlaMesaiSure = fazlaMesaiSure;
	}

	@Transient
	public String getIzinSuresiAciklama() {
		String aciklama = (izinSuresi.doubleValue() > izinSuresi.longValue() ? izinSuresi.doubleValue() : izinSuresi.longValue()) + " " + getSuresiAciklama();
		return aciklama;
	}

	@Transient
	public boolean isIptalEdilir() {
		return iptalEdilir;
	}

	public void setIptalEdilir(boolean iptalEdilir) {
		this.iptalEdilir = iptalEdilir;
	}

	@Transient
	public boolean isGunlukIzin() {
		IzinTipi tipi = izinTipi.getBakiyeIzinTipi() == null ? izinTipi : izinTipi.getBakiyeIzinTipi();
		boolean gunlukIzin = !tipi.getSaatGosterilecek();
		if (hesapTipi != null)
			gunlukIzin = hesapTipi.equals(HESAP_TIPI_GUN) || hesapTipi.equals(HESAP_TIPI_SAAT_GUN_SECILDI);
		else if (tipi != null && tipi.getHesapTipi() != null)
			gunlukIzin = tipi.getHesapTipi().equals(HESAP_TIPI_GUN) || tipi.equals(HESAP_TIPI_SAAT_GUN_SECILDI);
		return gunlukIzin;
	}

	@Transient
	public String getSureAciklama() throws Exception {
		StringBuilder sureAciklama = new StringBuilder();
		if (izinTipi != null && !izinTipi.isResmiTatilIzin()) {
			double fark = izinSuresi.doubleValue() - izinSuresi.longValue();
			sureAciklama.append(fark > 0D ? PdksUtil.numericValueFormatStr(izinSuresi.doubleValue(), null) : String.valueOf(izinSuresi.longValue()));
			sureAciklama.append(isGunlukIzin() ? " Gün" : " Saat");
		}
		String str = sureAciklama.toString();
		sureAciklama = null;
		return str;

	}

	@Transient
	public boolean isPlandaGoster() {
		return !isRedmi() && izinDurumu != IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA;
	}

	@Transient
	public PersonelIzin getKontrolIzin() {
		return kontrolIzin;
	}

	public void setKontrolIzin(PersonelIzin kontrolIzin) {
		this.kontrolIzin = kontrolIzin;
	}

	@Transient
	public String getSuresiAciklama() {
		String aciklama = (!isGunlukIzin() ? "Saat" : "Gün");
		return aciklama;
	}

	@Transient
	public String getIzinTipiAciklama() {
		StringBuilder aciklama = new StringBuilder(izinTipi.getIzinTipiTanim().getAciklama());
		if (gorevTipi != null)
			aciklama.append(" - " + gorevTipi.getAciklama());
		return aciklama.toString();
	}

	public static int getSuaIzinMaxBakiye() {
		return suaIzinMaxBakiye;
	}

	public static void setSuaIzinMaxBakiye(int suaIzinMaxBakiye) {
		PersonelIzin.suaIzinMaxBakiye = suaIzinMaxBakiye;
	}

	@Transient
	public Double getBakiyeSuresi() {
		return bakiyeSuresi;
	}

	public void setBakiyeSuresi(Double bakiyeSuresi) {
		this.bakiyeSuresi = bakiyeSuresi;
	}

	@Transient
	public Date getDonemSonu() {
		return donemSonu;
	}

	public void setDonemSonu(Date donemSonu) {
		this.donemSonu = donemSonu;
	}

	@Transient
	public boolean isIzinOnaylandi() {
		boolean onaylandi = izinDurumu == IZIN_DURUMU_ONAYLANDI;
		return onaylandi;
	}

	@Transient
	public boolean isIslemYapildi() {
		return islemYapildi;
	}

	public void setIslemYapildi(boolean islemYapildi) {
		this.islemYapildi = islemYapildi;
	}

	@Transient
	public List<PersonelIzin> getHarcananDigerIzinler() {
		return harcananDigerIzinler;
	}

	public void setHarcananDigerIzinler(List<PersonelIzin> harcananDigerIzinler) {
		this.harcananDigerIzinler = harcananDigerIzinler;
	}

	@Transient
	public double getDigerHarcananIzinSuresi() {
		double harcananDigerIzinSuresi = 0d;
		if (harcananDigerIzinler != null) {
			for (PersonelIzin personelIzin : harcananDigerIzinler)
				harcananDigerIzinSuresi += personelIzin.getIzinSuresi();
		}

		return harcananDigerIzinSuresi;
	}

	@Transient
	public Date getHakedisTarih() {
		return hakedisTarih;
	}

	public void setHakedisTarih(Date hakedisTarih) {
		this.hakedisTarih = hakedisTarih;
	}

	@Transient
	public Date getBirOncekiHakedisTarih() {
		return birOncekiHakedisTarih;
	}

	public void setBirOncekiHakedisTarih(Date birOncekiHakedisTarih) {
		this.birOncekiHakedisTarih = birOncekiHakedisTarih;
	}

	@Transient
	public long getMinimumHeight() {
		int size = harcananDigerIzinler != null && !harcananDigerIzinler.isEmpty() ? harcananDigerIzinler.size() : 1;
		return size * 16;
	}

	@Transient
	public Integer getSayfaNo() {
		return sayfaNo;
	}

	public void setSayfaNo(Integer sayfaNo) {
		this.sayfaNo = sayfaNo;
	}

	@Transient
	public String getMesaj() {
		return mesaj;
	}

	public void setMesaj(String mesaj) {
		this.mesaj = mesaj;
	}

	@Transient
	public Boolean getYilbasi() {
		return yilbasi;
	}

	public void setYilbasi(Boolean yilbasi) {
		this.yilbasi = yilbasi;
	}

	@Transient
	public String getIzinKodu() {
		String kod = null;
		if (izinTipi != null)
			kod = izinTipi.getIzinKodu();
		if (kod == null)
			kod = "";
		return kod.trim();
	}

}
