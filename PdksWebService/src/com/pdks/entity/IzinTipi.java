package com.pdks.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.pdks.genel.model.PdksUtil;

@Entity(name = IzinTipi.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { IzinTipi.COLUMN_NAME_DEPARTMAN, IzinTipi.COLUMN_NAME_IZIN_TIPI }) })
public class IzinTipi extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 56230242920057020L;
	public static final String TABLE_NAME = "IZINTIPI";
	public static final String COLUMN_NAME_DEPARTMAN = "DEPARTMAN_ID";
	public static final String COLUMN_NAME_IZIN_TIPI = "IZIN_TIPI_ID";
	public static final String COLUMN_NAME_BAKIYE_IZIN_TIPI = "BAKIYE_IZIN_TIPI_ID";
	public static final String COLUMN_NAME_MAIL_GONDERIM_DURUMU = "MAIL_GONDERIM_DURUMU";
	public static final String COLUMN_NAME_GIRIS_TIPI = "GIRIS_TIPI";

	public static final int MAIL_GONDERIM_DURUMU_ONAYSIZ = 0;

	public static final int MAIL_GONDERIM_DURUMU_ILK_ONAY = 1;

	public static final int MAIL_GONDERIM_DURUMU_IK_ONAY = 9;

	public static String YILLIK_UCRETLI_IZIN = "90";

	public static String UCRETSIZ_IZIN = "80";

	public static String EVLENME_IZNI = "20"; // max 7 takvim günü

	public static String DOGUM_IZNI = "30"; // max 5 takvim günü

	public static String SUNNET_IZNI = "130"; // max 2 takvim günü

	public static String MAZERET_IZNI = "MAZ"; //

	public static String IDARI_UCRETLI_IZIN = "10";

	public static String OLUM_IZNI = "60";

	public static String SUT_IZNI = "120";

	public static String ISYERI_DOKTORU_ISTIRAHATI = "40";

	public static String ISTIRAHAT = "110";

	public static String SSK_ISTIRAHAT = "70";

	public static String VIZITE_IZNI = "100";

	public static String EGITIM_IC = "+300";

	public static String EGITIM_DIS = "300";

	public static String IS_SEYAHATI = "310";

	public static String GOREVLI = "320";

	public static String FAZLA_MESAI = "FAZM";

	public static String GEBE_MUAYENE_IZNI = "GBMU";
	public static String YURT_DISI_KONGRE = "YDKONG";
	public static String YURT_ICI_KONGRE = "YIKONG";
	public static String SUA_IZNI = "ŞUA";
	public static String MOLA_IZNI = "MOLA";
	public static String RESMI_TATIL_IZNI = "RESTAT";
	public static String MESAI_DISI_UYGULAMA_IZNI = "MESAI_DISI";

	public static final String GIRIS_TIPI_YOK = "0";
	public static final String GIRIS_TIPI_PERSONEL = "1";
	public static final String GIRIS_TIPI_IK = "2";
	public static final String GIRIS_TIPI_YONETICI1 = "3";
	public static final String BAKIYE_DEVIR_YOK = "0"; // bu izin icin bakiye kontrolu yapilmaz
	public static final String BAKIYE_DEVIR_DEVAM_EDER = "1"; // devreder
	public static final String BAKIYE_DEVIR_SENELIK = "2"; // devretmez
	public static final String ONAYLAYAN_TIPI_YOK = "0";
	public static final String ONAYLAYAN_TIPI_YONETICI1 = "1";
	public static final String ONAYLAYAN_TIPI_YONETICI2 = "2";
	public static final String ONAYLAYAN_TIPI_IK = "3";
	public static final String DURUM_DEVAMLI = "2002";
	public static final String DURUM_DEVAMSIZ = "2001";
	public static final String mazeret_IK = "mazeret_IK";
	public static final String mazeret_KH = "mazeret_KH";
	public static final String yillik_KH = "yillik_KH";
	public static final String yillik_IK = "yillik_IK";
	public static final int CGS_DURUM_CIKAR = 1;
	public static final int CGS_DURUM_EKLE = 2;

	private Tanim izinTipiTanim;
	private Double maxGun = 0D, maxSaat = 0D, minGun = 0D, minSaat = 0D, kotaBakiye = 0D, artikIzinGun = 0D;
	private Departman departman;
	private Boolean dokumAlmaDurum, takvimGunumu, saatGosterilecek, gunGosterilecek, erpAktarim, izinKagidiGeldi, dosyaEkle = Boolean.FALSE;
	private Boolean gunSigortaDahil = Boolean.TRUE;
	private String personelGirisTipi = GIRIS_TIPI_PERSONEL;
	private String bakiyeDevirTipi = BAKIYE_DEVIR_YOK;
	private String onaylayanTipi = ONAYLAYAN_TIPI_YONETICI2;
	private String mesaj, kisaAciklama;
	private IzinTipi bakiyeIzinTipi;
	private Boolean denklestirmeDahil = Boolean.FALSE, offDahil = Boolean.FALSE;
	private Integer listeSira = 0, hesapTipi, durumCGS = CGS_DURUM_CIKAR, mailGonderimDurumu;

	private Integer version = 0;

	public IzinTipi() {
		super();
	}

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Column(name = "MAX_GUN")
	public Double getMaxGun() {
		return maxGun;
	}

	public void setMaxGun(Double maxGun) {
		this.maxGun = maxGun;
	}

	@Column(name = "MAX_SAAT")
	public Double getMaxSaat() {
		return maxSaat;
	}

	public void setMaxSaat(Double maxSaat) {
		this.maxSaat = maxSaat;
	}

	@Column(name = "MIN_GUN")
	public Double getMinGun() {
		return minGun;
	}

	public void setMinGun(Double minGun) {
		this.minGun = minGun;
	}

	@Column(name = "MIN_SAAT")
	public Double getMinSaat() {
		return minSaat;
	}

	public void setMinSaat(Double minSaat) {
		this.minSaat = minSaat;
	}

	@Column(name = "KOTA_BAKIYE")
	public Double getKotaBakiye() {
		return kotaBakiye;
	}

	public void setKotaBakiye(Double kotaBakiye) {
		this.kotaBakiye = kotaBakiye;
	}

	@Column(name = "DOKUM_ALMA_DURUM")
	public Boolean getDokumAlmaDurum() {
		return dokumAlmaDurum;
	}

	public void setDokumAlmaDurum(Boolean dokumAlmaDurum) {
		this.dokumAlmaDurum = dokumAlmaDurum;
	}

	@Column(name = "TAKVIM_GUNUMU")
	public Boolean getTakvimGunumu() {
		return takvimGunumu;
	}

	public void setTakvimGunumu(Boolean takvimGunumu) {
		this.takvimGunumu = takvimGunumu;
	}

	@Column(name = "GUN_SIGORTA_DAHIL")
	public Boolean getGunSigortaDahil() {
		return gunSigortaDahil;
	}

	public void setGunSigortaDahil(Boolean gunSigortaDahil) {
		this.gunSigortaDahil = gunSigortaDahil;
	}

	@Column(name = "SAAT_GOSTERILECEK")
	public Boolean getSaatGosterilecek() {
		return saatGosterilecek;
	}

	public void setSaatGosterilecek(Boolean saatGosterilecek) {
		this.saatGosterilecek = saatGosterilecek;
	}

	@Column(name = "GUN_GOSTERILECEK")
	public Boolean getGunGosterilecek() {
		return gunGosterilecek;
	}

	public void setGunGosterilecek(Boolean gunGosterilecek) {
		this.gunGosterilecek = gunGosterilecek;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_IZIN_TIPI, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Tanim getIzinTipiTanim() {
		return izinTipiTanim;
	}

	public void setIzinTipiTanim(Tanim izinTipiTanim) {
		this.izinTipiTanim = izinTipiTanim;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_DEPARTMAN, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman departman) {
		this.departman = departman;
	}

	@Column(name = "ERP_AKTARIM")
	public Boolean getErpAktarim() {
		return erpAktarim;
	}

	public void setErpAktarim(Boolean erpAktarim) {
		this.erpAktarim = erpAktarim;
	}

	@Column(name = "ARTIK_IZIN_GUN")
	public Double getArtikIzinGun() {
		return artikIzinGun;
	}

	public void setArtikIzinGun(Double artikIzinGun) {
		this.artikIzinGun = artikIzinGun;
	}

	@Column(name = "IZIN_KAGIDI_GELDI")
	public Boolean getIzinKagidiGeldi() {
		return izinKagidiGeldi;
	}

	public void setIzinKagidiGeldi(Boolean izinKagidiGeldi) {
		this.izinKagidiGeldi = izinKagidiGeldi;
	}

	@Column(name = COLUMN_NAME_GIRIS_TIPI, length = 1)
	public String getPersonelGirisTipi() {
		return personelGirisTipi;
	}

	public void setPersonelGirisTipi(String personelGirisTipi) {
		this.personelGirisTipi = personelGirisTipi;
	}

	@Column(name = "KISA_ACIKLAMA", length = 8)
	public String getKisaAciklama() {
		return kisaAciklama;
	}

	public void setKisaAciklama(String kisaAciklama) {
		this.kisaAciklama = kisaAciklama;
	}

	@Column(name = "MESAJ", length = 256, nullable = false)
	public String getMesaj() {
		return mesaj;
	}

	public void setMesaj(String mesaj) {
		mesaj = PdksUtil.convertUTF8(mesaj);
		this.mesaj = mesaj;
	}

	@Column(name = "BAKIYE_DEVIR_TIPI", length = 1)
	public String getBakiyeDevirTipi() {
		return bakiyeDevirTipi;
	}

	public void setBakiyeDevirTipi(String bakiyeDevirTipi) {
		this.bakiyeDevirTipi = bakiyeDevirTipi;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_BAKIYE_IZIN_TIPI, nullable = true)
	@Fetch(FetchMode.JOIN)
	public IzinTipi getBakiyeIzinTipi() {
		return bakiyeIzinTipi;
	}

	public void setBakiyeIzinTipi(IzinTipi bakiyeIzinTipi) {
		this.bakiyeIzinTipi = bakiyeIzinTipi;
	}

	@Column(name = "ONAYLAYAN_TIPI", length = 1)
	public String getOnaylayanTipi() {
		return onaylayanTipi;
	}

	public void setOnaylayanTipi(String onaylayanTipi) {
		this.onaylayanTipi = onaylayanTipi;
	}

	@Column(name = "OFF_DAHIL")
	public Boolean getOffDahil() {
		return offDahil;
	}

	public void setOffDahil(Boolean offDahil) {
		this.offDahil = offDahil;
	}

	@Column(name = "DENKLESTIRME_DAHIL")
	public Boolean getDenklestirmeDahil() {
		return denklestirmeDahil;
	}

	public void setDenklestirmeDahil(Boolean denklestirmeDahil) {
		this.denklestirmeDahil = denklestirmeDahil;
	}

	@Column(name = "DOSYA_EKLE")
	public Boolean getDosyaEkle() {
		return dosyaEkle;
	}

	public void setDosyaEkle(Boolean dosyaEkle) {
		this.dosyaEkle = dosyaEkle;
	}

	@Column(name = "HESAP_TIPI")
	public Integer getHesapTipi() {
		return hesapTipi;
	}

	public void setHesapTipi(Integer hesapTipi) {
		this.hesapTipi = hesapTipi;
	}

	@Column(name = "CGS_DURUMU")
	public Integer getDurumCGS() {
		return durumCGS;
	}

	public void setDurumCGS(Integer durumCGS) {
		this.durumCGS = durumCGS;
	}

	@Column(name = "LISTE_SIRA")
	public Integer getListeSira() {
		return listeSira;
	}

	public void setListeSira(Integer listeSira) {
		this.listeSira = listeSira;
	}

	@Column(name = COLUMN_NAME_MAIL_GONDERIM_DURUMU)
	public Integer getMailGonderimDurumu() {
		return mailGonderimDurumu;
	}

	public void setMailGonderimDurumu(Integer mailGonderimDurumu) {
		this.mailGonderimDurumu = mailGonderimDurumu;
	}

	@Transient
	public boolean isSenelikIzin() {
		return izinTipiTanim != null && izinTipiTanim.getKodu().equals(YILLIK_UCRETLI_IZIN);
	}

	@Transient
	public boolean isSSKIstirahat() {
		return izinTipiTanim != null && izinTipiTanim.getKodu().equals(SSK_ISTIRAHAT);
	}

	@Transient
	public boolean isGorevli() {
		return izinTipiTanim != null && izinTipiTanim.getKodu().equals(GOREVLI);
	}

	@Transient
	public boolean isSuaIzin() {
		return izinTipiTanim != null && izinTipiTanim.getKodu().equals(SUA_IZNI);
	}

	@Transient
	public boolean isBireyselMolaIzin() {
		return izinTipiTanim != null && izinTipiTanim.getKodu().equals(MOLA_IZNI);
	}

	@Transient
	public boolean isResmiTatilIzin() {
		return izinTipiTanim != null && izinTipiTanim.getKodu().equals(RESMI_TATIL_IZNI);
	}

	@Transient
	public boolean isFazlaMesai() {
		return izinTipiTanim != null && izinTipiTanim.getKodu().equals(FAZLA_MESAI);
	}

	@Transient
	public boolean isMazeretIzin() {
		return izinTipiTanim != null && izinTipiTanim.getKodu().equals(MAZERET_IZNI);
	}

	@Transient
	public boolean isRaporIzin() {
		return izinTipiTanim != null && (izinTipiTanim.getKodu().equals(SSK_ISTIRAHAT) || izinTipiTanim.getKodu().equals(ISTIRAHAT));
	}

	@Transient
	public boolean isSutIzin() {
		return izinTipiTanim != null && izinTipiTanim.getKodu().equals(SUT_IZNI);
	}

	@Transient
	public boolean isGebelikMuayeneIzin() {
		return izinTipiTanim != null && izinTipiTanim.getKodu().equals(GEBE_MUAYENE_IZNI);
	}

	@Transient
	public boolean isEgitim() {
		return isIcEgitim() || isDisEgitim();
	}

	@Transient
	public boolean isIcEgitim() {
		return izinTipiTanim != null && izinTipiTanim.getKodu().equals(EGITIM_IC);
	}

	@Transient
	public boolean isDisEgitim() {
		return izinTipiTanim != null && izinTipiTanim.getKodu().equals(EGITIM_DIS);
	}

	@Transient
	public boolean isDevamli() {
		return izinTipiTanim != null && izinTipiTanim.getParentTanim() != null && izinTipiTanim.getParentTanim().getKodu().equals(DURUM_DEVAMLI);
	}

	@Transient
	public boolean isDevamsiz() {
		return izinTipiTanim != null && izinTipiTanim.getParentTanim() != null && izinTipiTanim.getParentTanim().getKodu().equals(DURUM_DEVAMSIZ);
	}

	@Transient
	public boolean isYurtIciKongre() {
		return izinTipiTanim != null && izinTipiTanim.getKodu().equals(YURT_ICI_KONGRE);
	}

	@Transient
	public boolean isYurtDisiKongre() {
		return izinTipiTanim != null && izinTipiTanim.getKodu().equals(YURT_DISI_KONGRE);
	}

	@Transient
	public boolean isOnaysiz() {
		return onaylayanTipi != null && onaylayanTipi.equals(ONAYLAYAN_TIPI_YOK);
	}

	@Transient
	public String getSira() {
		int sira = listeSira == null || listeSira <= 0 ? 100000 : listeSira;
		String siraStr = departman.getDepartmanTanim().getAciklama() + PdksUtil.textBaslangicinaKarakterEkle(String.valueOf(sira), '0', 8) + izinTipiTanim.getAciklama();
		return siraStr;
	}

	@Transient
	public boolean isBakiyeSenelik() {
		return bakiyeDevirTipi != null && bakiyeDevirTipi.equals(BAKIYE_DEVIR_SENELIK);
	}

	@Transient
	public boolean isBakiyeDevamEder() {
		return bakiyeDevirTipi != null && bakiyeDevirTipi.equals(BAKIYE_DEVIR_DEVAM_EDER);
	}

	@Transient
	public static String getMailGonderimDurumAciklama(Integer tipi) {
		String str = "";
		if (tipi != null) {
			switch (tipi) {
			case MAIL_GONDERIM_DURUMU_ONAYSIZ:
				str = "Onaysız";
				break;
			case MAIL_GONDERIM_DURUMU_ILK_ONAY:
				str = "1. Yönetici onayı";
				break;
			case MAIL_GONDERIM_DURUMU_IK_ONAY:
				str = "İK onayı";
				break;
			default:
				break;
			}
		}
		return str;
	}

	@Transient
	public String getKodERP() {
		String str = izinTipiTanim != null ? izinTipiTanim.getErpKodu() : null;
		return str != null ? str.trim() : "";
	}

	@Transient
	public boolean isMailGonderimDurumIK() {
		return mailGonderimDurumu != null && mailGonderimDurumu.equals(MAIL_GONDERIM_DURUMU_IK_ONAY);
	}

	@Transient
	public boolean isMailGonderimDurumllkYonetici() {
		return mailGonderimDurumu != null && mailGonderimDurumu.equals(MAIL_GONDERIM_DURUMU_ILK_ONAY);
	}

	@Transient
	public boolean isMailGonderimDurumOnaysiz() {
		return mailGonderimDurumu != null && mailGonderimDurumu.equals(MAIL_GONDERIM_DURUMU_ONAYSIZ);
	}

	@Transient
	public boolean isOffDahilMi() {
		return (takvimGunumu) || (offDahil != null && offDahil.booleanValue());
	}

	@Transient
	public Boolean isDosyaEklenir() {
		return dosyaEkle != null && dosyaEkle.booleanValue();
	}

	@Transient
	public Boolean isEkleCGS() {
		return durumCGS != null && durumCGS.equals(CGS_DURUM_EKLE);
	}

	@Transient
	public Boolean isCikarCGS() {
		return durumCGS != null && durumCGS.equals(CGS_DURUM_CIKAR);
	}

	@Transient
	public String getDurumCGSAciklama() {
		String durumCGSAciklama = getDurumCGSAciklama(durumCGS);
		return durumCGSAciklama;
	}

	@Transient
	public static String getDurumCGSAciklama(Integer xDurumCGS) {
		String durumCGSAciklama = "";
		if (xDurumCGS != null) {
			switch (xDurumCGS) {
			case CGS_DURUM_EKLE:
				durumCGSAciklama = "ÇGS Ekle";
				break;
			case CGS_DURUM_CIKAR:
				durumCGSAciklama = "ÇGS Çıkar";
				break;
			default:
				break;
			}
		}
		return durumCGSAciklama;
	}

	@Transient
	public String getIzinKodu() {
		String kod = null;
		if (izinTipiTanim != null)
			kod = izinTipiTanim.getKodu();
		if (kod == null)
			kod = "";
		return kod.trim();
	}

}
