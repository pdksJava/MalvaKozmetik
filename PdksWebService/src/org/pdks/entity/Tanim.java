package org.pdks.entity;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.Length;

import org.pdks.genel.model.Constants;
import org.pdks.genel.model.PdksUtil;

@Entity(name = Tanim.TABLE_NAME)
public class Tanim extends BasePDKSObject implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1265931577160004548L;
	static Logger logger = Logger.getLogger(Tanim.class);

	private static boolean multiLanguage = Boolean.FALSE;
	// seam-gen attributes (you should probably edit these)
	public static final String TABLE_NAME = "TANIM";
	public static final String COLUMN_NAME_KODU = "KODU";
	public static final String COLUMN_NAME_ERP_KODU = "ERP_KODU";
	public static final String COLUMN_NAME_TIPI = "TIPI";

	public static final String COLUMN_NAME_PARENT_ID = "PARENTTANIM_ID";
	public static final String COLUMN_NAME_DURUM = "DURUM";
	public static final int DURUM_PASIF = 0;
	public static final int DURUM_AKTIF = 1;
	public static final String TIPI_GENEL_TANIM = "GENEL_TANIM";
	public static final String TIPI_UYGULAMA_DILLERI = "UYGULAMA_DILLERI";
	public static final String TIPI_IL = "IL";
	public static final String TIPI_ILCE = "ILCE";
	public static final String TIPI_MENU_BILESENI = "MENU_BILESENI";
	public static final String TIPI_BAGLI_DEPARTMANLAR = "BAGLI_DEPARTMAN";
	public static final String TIPI_TATIL_TIPI = "TATIL_TIPI";
	public static final String TIPI_IZIN_TIPI = "IZIN_TIPI";
	public static final String TIPI_SAP_IZIN_BILGI_TIPI = "SAP_IZIN_BILGI_TIPI";
	public static final String TIPI_BAKIYE_IZIN_TIPI = "BAKIYE_IZIN_TIPI";
	public static final String TIPI_GOREV_TIPI = "GOREV_TIPI";
	public static final String TIPI_PERSONEL_DINAMIK_DURUM = "PERSONEL_DINAMIK_DURUM";
	public static final String TIPI_KAPI_TIPI = "KAPI_TIPI";
	public static final String TIPI_SAP_DEPARTMAN = "SAP_DEPARTMAN";
	public static final String TIPI_PDKS_DEPARTMAN = "PDKS_DEPARTMAN";
	public static final String TIPI_IZIN_KODU_GRUPLARI = "IZIN_KODU_GRUBU";
	public static final String TIPI_TESIS = "TESIS";
	public static final String TIPI_CINSIYET = "CINSIYET";
	public static final String TIPI_HAREKET_NEDEN = "TIPI_HAREKET_NEDEN";
	public static final String TIPI_FAZLA_MESAI_NEDEN = "TIPI_FAZLA_MESAI_NEDEN";
	public static final String TIPI_ONAYLAMAMA_NEDEN = "ONAYLAMAMA_NEDEN";
	public static final String TIPI_IZIN_GOREV_TIPI = "IZIN_GOREV_TIPI";
	public static final String TIPI_ISTIRAHAT_KAYNAGI = "ISTIRAHAT_KAYNAGI";
	public static final String TIPI_PERSONEL_EK_SAHA = "PERSONEL_EK_SAHA";
	public static final String TIPI_PERSONEL_EK_SAHA_ACIKLAMA = "PERSONEL_EK_SAHA_ACIKLAMA";
	public static final String TIPI_ISTIRAHAT_KAYNAK = "ISTIRAHAT_KAYNAK";
	public static final String TIPI_BOLUM_DEPARTMAN = "BOLUM_DEPARTMAN";
	public static final String TIPI_PERSONEL_TIPI = "PERSONEL_TIPI";
	public static final String TIPI_SKIN = "SKIN";

	public static final String TIPI_ERP_MASRAF_YERI = "ERP_MASRAF_YERI";
	public static final String TIPI_BORDRO_ALT_BIRIMI = "BORDRO_ALT_BIRIMI";
	public static final String TIPI_GIRIS_TIPI = "GIRIS_TIPI";
	public static final String TIPI_YONETICI_VARDIYA = "YONETICI_VARDIYA";
	public static final String TIPI_GOREV_YERI = "GOREV_YERI";
	public static final String DEPARTMAN_KONTRATLI_HIZMETLER = "02";
	public static final String DEPARTMAN_INSAN_KAYNAKLARI = "01";
	public static final String TIPI_ERP_FAZLA_MESAI = "ERP_FAZLA_MESAI";

	public static final String GOREV_TIPI_PERSONEL = "01";
	public static final String GOREV_TIPI_PROJE_MUDURU = "02";
	public static final String GOREV_TIPI_SUPERVISOR = "03";
	public static final String IKINCI_YONETICI_ONAYLAMAZ = "ikinciYoneticiOlmaz";
	public static final String DEFAULT_DOVIZ_KODU = "TL";

	private String tipi;
	private Tanim parentTanim;
	private String kodu = "", erpKodu = "";
	private String aciklamatr;
	private String aciklamaen;
	private Boolean durum = Boolean.TRUE;
	private boolean guncelle = Boolean.FALSE;
	private Tanim childGenelTanim;
	private User islemYapan;
	private Date islemTarihi = Calendar.getInstance().getTime();

	// seam-gen attribute getters/setters with annotations (you probably should
	// edit)

	public Tanim() {
		super();

	}

	public Tanim(Long id) {
		super();
		this.id = id;
	}

	@Length(max = 60)
	@Column(name = "ACIKLAMATR")
	public String getAciklamatr() {
		return aciklamatr != null ? aciklamatr.trim() : "";
	}

	public void setAciklamatr(String aciklamatr) {
		aciklamatr = PdksUtil.convertUTF8(aciklamatr);
		this.aciklamatr = aciklamatr;
	}

	@Length(max = 60)
	@Column(name = "ACIKLAMAEN")
	public String getAciklamaen() {
		return aciklamaen != null ? aciklamaen.trim() : "";
	}

	public void setAciklamaen(String aciklamaen) {
		this.aciklamaen = aciklamaen;
	}

	@Length(max = 32)
	@Column(name = COLUMN_NAME_TIPI)
	public String getTipi() {
		return tipi;
	}

	public void setTipi(String tipi) {
		this.tipi = tipi;
	}

	@Column(name = COLUMN_NAME_ERP_KODU)
	public String getErpKodu() {
		return erpKodu;
	}

	public void setErpKodu(String erpKodu) {
		this.erpKodu = erpKodu;
	}

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PARENT_ID)
	@Fetch(FetchMode.JOIN)
	public Tanim getParentTanim() {
		return parentTanim;
	}

	public void setParentTanim(Tanim parentTanim) {
		this.parentTanim = parentTanim;
	}

	@Length(max = 32)
	@Column(name = COLUMN_NAME_KODU)
	public String getKodu() {
		return kodu;
	}

	public void setKodu(String kodu) {
		this.kodu = kodu;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	@JoinColumn(name = "ISLEM_YAPAN")
	@Fetch(FetchMode.JOIN)
	public User getIslemYapan() {
		return islemYapan;
	}

	public void setIslemYapan(User islemYapan) {
		this.islemYapan = islemYapan;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ISLEM_TARIHI")
	public Date getIslemTarihi() {
		return islemTarihi;
	}

	public void setIslemTarihi(Date islemTarihi) {
		this.islemTarihi = islemTarihi;
	}

	@Transient
	public String getDurumAciklama() {
		String aciklama = durum ? "AKTIF" : "PASIF";

		return aciklama;
	}

	@Transient
	public String getAciklama() {
		String aciklama = null;
		String dilKodu = "";
		try {
			if (Tanim.isMultilanguage()) {

				dilKodu = Constants.TR_LOCALE.getLanguage();
				aciklama = (String) PdksUtil.getMethodObject(this, "getAciklama" + dilKodu, null);
			} else
				aciklama = getAciklamatr();
		} catch (Exception e) {

			aciklama = null;
		}
		if (aciklama == null)
			aciklama = getAciklamatr() + " [ " + dilKodu + " ] ???";

		return aciklama;
	}

	@Transient
	public Tanim getChildGenelTanim() {
		return childGenelTanim;
	}

	public void setChildGenelTanim(Tanim childGenelTanim) {
		this.childGenelTanim = childGenelTanim;
	}

	@Transient
	public long getKoduLong() {
		long kod;
		try {
			kod = Long.parseLong(kodu.trim());
		} catch (Exception e) {
			kod = 0;
		}
		return kod;
	}

	@Column(name = "GUNCELLE")
	public boolean isGuncelle() {
		return guncelle;
	}

	public void setGuncelle(boolean guncelle) {
		this.guncelle = guncelle;
	}

	public static boolean isMultilanguage() {
		return multiLanguage;
	}

	public static boolean isMultiLanguage() {
		return multiLanguage;
	}

	public static void setMultiLanguage(boolean multiLanguage) {
		Tanim.multiLanguage = multiLanguage;
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

 

	public boolean equals(Tanim obj) {
		boolean eq = Boolean.FALSE;
		if (obj != null)
			eq = this.id != null && this.id.equals(obj.getId());
		else
			eq = this.id == null;
		return eq;

	}

}
