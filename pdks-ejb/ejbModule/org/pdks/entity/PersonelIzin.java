package org.pdks.entity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import org.apache.log4j.Logger;
import org.pdks.security.entity.User;
import org.pdks.session.PdksUtil;
import org.pdks.session.OrtakIslemler;
import org.hibernate.Session;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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

	private String aciklama, mesaj, personelNo, referansERP;

	private Personel izinSahibi;

	private IzinTipi izinTipi;

	private Boolean izinKagidiGeldi, yilbasi = Boolean.FALSE, devirIzin;

	private PersonelIzin kontrolIzin, orjIzin;

	private Integer hesapTipi, sayfaNo = 0;

	private Tanim gorevTipi;

	private Set<PersonelIzinOnay> onaylayanlar;

	private Set<PersonelIzinDetay> hakEdisIzinler, personelIzinler;

	private boolean gunlukOldu = Boolean.FALSE, iptalEdilir = Boolean.TRUE;

	private Date donemSonu;

	private IzinIstirahat istirahat;

	private Dosya dosya;

	private boolean islemYapildi = Boolean.FALSE;

	private List<PersonelIzin> harcananDigerIzinler;

	private List<VardiyaGun> calisilanGunler;

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

	public void setIzinSahibi(Personel izinSahibi) {
		this.izinSahibi = izinSahibi;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "GOREV_TIPI_ID", nullable = true)
	@Fetch(FetchMode.JOIN)
	public Tanim getGorevTipi() {
		return gorevTipi;
	}

	public void setGorevTipi(Tanim gorevTipi) {
		this.gorevTipi = gorevTipi;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_IZIN_TIPI, nullable = false)
	@Fetch(FetchMode.JOIN)
	public IzinTipi getIzinTipi() {
		return izinTipi;
	}

	public void setIzinTipi(IzinTipi izinTipi) {
		this.izinTipi = izinTipi;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_BASLANGIC_ZAMANI, nullable = false)
	public Date getBaslangicZamani() {
		return baslangicZamani;
	}

	public void setBaslangicZamani(Date baslangicZamani) {
		this.baslangicZamani = baslangicZamani;
	}

	@Column(name = "HESAP_TIPI")
	public Integer getHesapTipi() {
		return hesapTipi;
	}

	public void setHesapTipi(Integer hesapTipi) {
		this.hesapTipi = hesapTipi;
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

	public void setBitisZamani(Date bitisZamani) {
		this.bitisZamani = bitisZamani;
	}

	@Column(name = COLUMN_NAME_IZIN_SURESI, nullable = false)
	public Double getIzinSuresi() {
		return izinSuresi;
	}

	public void setIzinSuresi(Double izinSuresi) {
		this.izinSuresi = izinSuresi;
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

	public void setIzinDurumu(int izinDurumu) {
		this.izinDurumu = izinDurumu;
	}

	@Transient
	public String getIzinDurumuAciklama(OrtakIslemler ortakIslemler, Session session) {
		String aciklama = "İzin durumu bilinmiyor";

		if (izinDurumu == IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA)
			aciklama = IZIN_DURUMU_ACIKLAMA_BIRINCI_YONETICI_ONAYINDA;
		else if (izinDurumu == IZIN_DURUMU_IK_ONAYINDA)
			aciklama = IZIN_DURUMU_ACIKLAMA_IK_ONAYINDA;
		else if (izinDurumu == IZIN_DURUMU_IKINCI_YONETICI_ONAYINDA)
			aciklama = IZIN_DURUMU_ACIKLAMA_IKINCI_YONETICI_ONAYINDA;
		else if (izinDurumu == IZIN_DURUMU_ONAYLANDI)
			aciklama = IZIN_DURUMU_ACIKLAMA_ONAYLANDI;
		else if (izinDurumu == IZIN_DURUMU_SAP_GONDERILDI)
			aciklama = IZIN_DURUMU_ACIKLAMA_SAP_GONDERILDI;
		else if (izinDurumu == IZIN_DURUMU_SISTEM_IPTAL)
			aciklama = IZIN_DURUMU_ACIKLAMA_SISTEM_IPTAL;
		else if (izinDurumu == IZIN_DURUMU_REDEDILDI) {
			aciklama = IZIN_DURUMU_ACIKLAMA_IPTAL_EDILDI;
			if (onaylayanlar != null) {
				boolean sureceGirdi = onaylayanlar.size() > 1;
				if (sureceGirdi) {
					boolean ikIptal = Boolean.FALSE;
					for (Iterator iterator = onaylayanlar.iterator(); iterator.hasNext();) {
						PersonelIzinOnay onaylayan = (PersonelIzinOnay) iterator.next();
						if (onaylayan.getOnayDurum() == PersonelIzinOnay.ONAY_DURUM_ONAYLANDI) {
							sureceGirdi = Boolean.TRUE;
						} else {
							User user = onaylayan.getPersonelIzin().getGuncelleyenUser();
							ortakIslemler.setUserRoller(user, session);
							ikIptal = onaylayan.getOnaylayanTipi().equals(PersonelIzinOnay.ONAYLAYAN_TIPI_IK) || user.isIK();
						}
					}
					if (sureceGirdi && !ikIptal)
						aciklama = IZIN_DURUMU_ACIKLAMA_REDEDILDI;
				}

			}
		}
		return aciklama;

	}

	@Transient
	public String getOnaylamamaNedenAciklama() {
		StringBuilder aciklama = new StringBuilder();
		if (isRedmi() && onaylayanlar != null) {

			for (PersonelIzinOnay personelIzinOnay : onaylayanlar) {
				if (personelIzinOnay.getOnayDurum() == PersonelIzinOnay.ONAY_DURUM_RED)
					aciklama.append(personelIzinOnay.getOnaylamamaNeden().getAciklama() + " " + personelIzinOnay.getOnaylamamaNedenAciklama());
			}

		}
		String str = aciklama.toString();
		aciklama = null;
		return str;

	}

	@Column(name = COLUMN_NAME_ACIKLAMA, length = ACIKLAMA_UZUNLUK)
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	@OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER, mappedBy = "personelIzin", targetEntity = PersonelIzinOnay.class)
	public Set<PersonelIzinOnay> getOnaylayanlar() {
		return onaylayanlar;
	}

	public void setOnaylayanlar(Set<PersonelIzinOnay> onaylayanlar) {
		this.onaylayanlar = onaylayanlar;
	}

	@OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER, mappedBy = "hakEdisIzin", targetEntity = PersonelIzinDetay.class)
	public Set<PersonelIzinDetay> getHakEdisIzinler() {
		return hakEdisIzinler;
	}

	public void setHakEdisIzinler(Set<PersonelIzinDetay> hakEdisIzinler) {
		this.hakEdisIzinler = hakEdisIzinler;
	}

	@OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER, mappedBy = "personelIzin", targetEntity = PersonelIzinDetay.class)
	public Set<PersonelIzinDetay> getPersonelIzinler() {
		return personelIzinler;
	}

	public void setPersonelIzinler(Set<PersonelIzinDetay> personelIzinler) {
		this.personelIzinler = personelIzinler;
	}

	@Transient
	public double getHarcananIzin() {
		double kullanilanIzin = kullanilanIzinSuresi != null ? kullanilanIzinSuresi : 0;
		if (harcananDigerIzinler == null)
			harcananDigerIzinler = new ArrayList<PersonelIzin>();
		else
			harcananDigerIzinler.clear();
		if (hakEdisIzinler != null && !hakEdisIzinler.isEmpty())
			for (PersonelIzinDetay personelIzinDetay : hakEdisIzinler)
				if (!personelIzinDetay.getPersonelIzin().isRedmi()) {

					double izinMiktar = 0;
					if (kontrolIzin == null || !kontrolIzin.getId().equals(personelIzinDetay.getPersonelIzin().getId())) {
						izinMiktar = personelIzinDetay.getIzinMiktari();
						if (izinMiktar != personelIzinDetay.getPersonelIzin().getIzinSuresi()) {
							izinMiktar = personelIzinDetay.getPersonelIzin().getIzinSuresi();
						}
					}
					if (donemSonu != null && personelIzinDetay.getPersonelIzin().getBaslangicZamani().getTime() > donemSonu.getTime())
						izinMiktar = 0;
					if (izinMiktar > 0)
						kullanilanIzin += izinMiktar;

					if (izinMiktar > 0 || personelIzinDetay.getIzinMiktari() != personelIzinDetay.getPersonelIzin().getIzinSuresi()) {
						PersonelIzin kopyaIzin = (PersonelIzin) personelIzinDetay.getPersonelIzin().clone();
						harcananDigerIzinler.add(kopyaIzin);
					}

				}

		return kullanilanIzin;
	}

	@Transient
	public double getKalanIzin() {
		double kalanIzin = izinSuresi - getHarcananIzin();
		return kalanIzin;
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
	public boolean isIptalEdilirmi(User user) {
		boolean islem = !isRedmi();
		if (islem) {
			if (user != null && !user.isIK() && !user.isAdmin()) {
				Personel yoneticiPersonel = izinSahibi.getPdksYonetici();
				if (yoneticiPersonel != null && user.getPersonelId().equals(yoneticiPersonel.getId()))
					islem = izinDurumu == IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA || izinDurumu == IZIN_DURUMU_IKINCI_YONETICI_ONAYINDA || izinDurumu == IZIN_DURUMU_IK_ONAYINDA;
				else
					islem = izinDurumu == IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA || (izinDurumu == IZIN_DURUMU_IK_ONAYINDA && izinTipi.getOnaylayanTipi().equals(IzinTipi.ONAYLAYAN_TIPI_YOK));

			}
			try {
				if (islem && izinSahibi.getId().equals(user.getPersonelId()) && user.isIK() && !user.isIKAdmin())
					islem = izinDurumu == IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA;
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
			}

		}

		return islem;
	}

	@Transient
	public boolean isPdfMi() {
		return izinTipi.getDokumAlmaDurum() && (izinDurumu == IZIN_DURUMU_IK_ONAYINDA || izinDurumu == IZIN_DURUMU_ONAYLANDI || izinDurumu == IZIN_DURUMU_SAP_GONDERILDI);
	}

	@Transient
	public boolean isBakiyeVar() {
		boolean durum = Boolean.TRUE;
		if (izinTipi.getIzinTipiTanim().getKodu().equals(IzinTipi.YILLIK_UCRETLI_IZIN) && personelIzinler != null && !personelIzinler.isEmpty()) {
			Calendar cal = Calendar.getInstance();
			int yil = cal.get(Calendar.YEAR);
			for (PersonelIzinDetay personelIzinDetay : personelIzinler) {
				cal.setTime(personelIzinDetay.getHakEdisIzin().getBaslangicZamani());
				if (cal.get(Calendar.YEAR) > yil) {
					durum = Boolean.FALSE;
					break;
				}
			}
		}
		return durum;
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
		String aciklama = (PdksUtil.isDoubleValueNotLong(izinSuresi) ? izinSuresi.doubleValue() : izinSuresi.longValue()) + " " + getSuresiAciklama();
		return aciklama;
	}

	@Transient
	public boolean isGirilen() {
		boolean girilen = izinTipi != null && izinTipi.getPersonelGirisTipi() != null && !izinTipi.getPersonelGirisTipi().equals(IzinTipi.GIRIS_TIPI_YOK);
		return girilen;
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
	public IzinIstirahat getIstirahat() {
		return istirahat;
	}

	public void setIstirahat(IzinIstirahat istirahat) {
		this.istirahat = istirahat;
	}

	@Transient
	public Double getBakiyeSuresi() {
		return bakiyeSuresi;
	}

	public void setBakiyeSuresi(Double bakiyeSuresi) {
		this.bakiyeSuresi = bakiyeSuresi;
	}

	@Transient
	public boolean isBakiyeSil(User user) {
		boolean bakiyeSil = Boolean.FALSE;
		if (user != null && (user.isAdmin() || user.isIK())) {
			double kullanilanIzin = 0;
			if (hakEdisIzinler != null && !hakEdisIzinler.isEmpty())
				for (PersonelIzinDetay personelIzinDetay : hakEdisIzinler)
					if (!personelIzinDetay.getPersonelIzin().isRedmi())
						kullanilanIzin += personelIzinDetay.getIzinMiktari();
			bakiyeSil = kullanilanIzin == 0;
		}
		return bakiyeSil;
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
	public List<PersonelIzinOnay> getPersonelIzinOnayList() {
		List<PersonelIzinOnay> personelIzinOnayList = new ArrayList<PersonelIzinOnay>();
		if (onaylayanlar != null) {
			for (PersonelIzinOnay personelIzinOnay : onaylayanlar)
				personelIzinOnayList.add(personelIzinOnay);

			if (personelIzinOnayList.size() > 1)
				personelIzinOnayList = PdksUtil.sortListByAlanAdi(personelIzinOnayList, "id", Boolean.FALSE);
		}
		return personelIzinOnayList;

	}

	@Transient
	public Dosya getDosya() {
		return dosya;
	}

	public void setDosya(Dosya dosya) {
		this.dosya = dosya;
	}

	@Transient
	public boolean isTekrarIkinciYoneticiOnayla(User user) {
		boolean islem = Boolean.FALSE;
		if (user != null && (user.isAdmin() || user.isIK()) && izinDurumu == IZIN_DURUMU_IKINCI_YONETICI_ONAYINDA) {
			if (onaylayanlar != null && onaylayanlar.size() == 1) {
				for (PersonelIzinOnay personelIzinOnay : onaylayanlar)
					if (personelIzinOnay.getOnaylayanTipi().equals(PersonelIzinOnay.ONAYLAYAN_TIPI_YONETICI1)) {
						islem = Boolean.TRUE;
						break;
					}
			}
		}
		return islem;
	}

	@Transient
	public List<PersonelIzinDetay> getHarcananIzinler() {
		List<PersonelIzinDetay> harcananIzinler = null;
		if (hakEdisIzinler != null && !hakEdisIzinler.isEmpty()) {
			for (PersonelIzinDetay personelIzinDetay : hakEdisIzinler) {
				if (!personelIzinDetay.getPersonelIzin().isRedmi()) {
					if (harcananIzinler == null)
						harcananIzinler = new ArrayList<PersonelIzinDetay>();
					harcananIzinler.add((PersonelIzinDetay) personelIzinDetay.clone());

				}
				if (harcananIzinler != null && harcananIzinler.size() > 1)
					harcananIzinler = PdksUtil.sortListByAlanAdi(harcananIzinler, "id", Boolean.FALSE);

			}

		}
		return harcananIzinler;
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
	public PersonelIzin setVardiyaIzin(VardiyaGun vardiyaGun) {
		PersonelIzin personelIzin = (PersonelIzin) this.clone();
		personelIzin.setOrjIzin(this);
		if (vardiyaGun != null) {
			if (vardiyaGun.getIslemVardiya() == null)
				vardiyaGun.setVardiyaZamani();
			Vardiya vardiya = vardiyaGun.getIslemVardiya();
			if (vardiya != null) {
				personelIzin.setIzinSuresi(1.0d);
				personelIzin.setBaslangicZamani(vardiya.getVardiyaBasZaman());
				personelIzin.setBitisZamani(vardiya.getVardiyaBitZaman());
			}
			vardiyaGun.setIzin(personelIzin);
		}
		return personelIzin;
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

	@Transient
	public boolean isAciklamaVar() {
		return PdksUtil.hasStringValue(aciklama);
	}

	@Transient
	public PersonelIzin getOrjIzin() {
		PersonelIzin izin = orjIzin != null ? orjIzin : this;
		return izin;
	}

	public void setOrjIzin(PersonelIzin orjIzin) {
		this.orjIzin = orjIzin;
	}

	@Transient
	public List<VardiyaGun> getCalisilanGunler() {
		return calisilanGunler;
	}

	public void setCalisilanGunler(List<VardiyaGun> calisilanGunler) {
		this.calisilanGunler = calisilanGunler;
	}

	public void addCalisilanGunler(VardiyaGun value) {
		if (calisilanGunler == null)
			calisilanGunler = new ArrayList<VardiyaGun>();
		calisilanGunler.add(value);
	}

	@Transient
	public Personel getPdksPersonel() {
		return izinSahibi;
	}

	@Transient
	public String getReferansERP() {
		return referansERP;
	}

	public void setReferansERP(String referansERP) {
		this.referansERP = referansERP;
	}

	@Transient
	public Boolean getDevirIzin() {
		if (devirIzin == null)  
			try {
				devirIzin = baslangicZamani.getTime() == PdksUtil.getBakiyeYil().getTime();
			} catch (Exception e) {
 			}
			
		 
		return devirIzin;
	}

	/**
	 * @param devirIzin
	 *            the devirIzin to set
	 */
	public void setDevirIzin(Boolean devirIzin) {
		this.devirIzin = devirIzin;
	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}
}
