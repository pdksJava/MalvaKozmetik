package org.pdks.entity;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.security.entity.User;
import org.pdks.session.PdksUtil;

@Entity(name = CalismaModeli.TABLE_NAME)
public class CalismaModeli implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4015750209129001721L;
	public static final String TABLE_NAME = "CALISMA_MODELI";
	public static final String COLUMN_NAME_ID = "ID";
	public static final String COLUMN_NAME_DURUM = "DURUM";
	public static final String COLUMN_NAME_GENEL_VARDIYA = "GENEL_VARDIYA";
	public static final String COLUMN_NAME_HAFTA_TATIL_MESAI_ODE = "HAFTA_TATIL_MESAI_ODE";
	public static final String COLUMN_NAME_GECE_HAFTA_TATIL_MESAI_PARCALA = "GECE_HAFTA_TATIL_MESAI_PARCALA";
	public static final String COLUMN_NAME_GECE_CALISMA_ODEME_VAR = "GECE_CALISMA_ODEME_VAR";
	public static final String COLUMN_NAME_OLUSTURAN = "OLUSTURANUSER_ID";
	public static final String COLUMN_NAME_GUNCELLEYEN = "GUNCELLEYENUSER_ID";
	public static final String COLUMN_NAME_OLUSTURMA_TARIHI = "OLUSTURMATARIHI";
	public static final String COLUMN_NAME_GUNCELLEME_TARIHI = "GUNCELLEMETARIHI";
	public static final String COLUMN_NAME_BAGLI_VARDIYA_SABLON = "BAGLI_VARDIYA_SABLON_ID";
	public static final String COLUMN_NAME_DEPARTMAN = "DEPARTMAN_ID";
	public static final String COLUMN_NAME_HAREKET_KAYDI_VARDIYA_BUL = "HAREKET_KAYDI_VARDIYA_BUL";

	private Long id;

	private String aciklama = "";
	private double haftaIci = 0.0d, haftaSonu = 0.0d, arife = 0.0d, izin = 9.0d, izinhaftaSonu = 0.0d, negatifBakiyeDenkSaat = 0.0d;
	/**
	 * 
	 */
	private Boolean toplamGunGuncelle = Boolean.FALSE, durum = Boolean.TRUE, genelVardiya = Boolean.TRUE, hareketKaydiVardiyaBul = Boolean.FALSE;
	private Boolean haftaTatilMesaiOde = Boolean.FALSE, geceHaftaTatilMesaiParcala = Boolean.FALSE, geceCalismaOdemeVar = Boolean.FALSE;
	private VardiyaSablonu bagliVardiyaSablonu;
	private Departman departman;
	private User guncelleyenUser, olusturanUser;
	private Date olusturmaTarihi = new Date(), guncellemeTarihi;

	@Id
	@GeneratedValue
	@Column(name = COLUMN_NAME_ID)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "ACIKLAMA")
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	@Column(name = "HAFTA_ICI_SAAT")
	public double getHaftaIci() {
		return haftaIci;
	}

	public void setHaftaIci(double haftaIci) {
		this.haftaIci = haftaIci;
	}

	@Column(name = "CUMARTESI_SAAT")
	public double getHaftaSonu() {
		return haftaSonu;
	}

	public void setHaftaSonu(double haftaSonu) {
		this.haftaSonu = haftaSonu;
	}

	@Column(name = "IZIN_SAAT")
	public double getIzin() {
		return izin;
	}

	public void setIzin(double izin) {
		this.izin = izin;
	}

	@Column(name = "IZIN_CUMARTESI_SAAT")
	public double getIzinhaftaSonu() {
		return izinhaftaSonu;
	}

	public void setIzinhaftaSonu(double izinhaftaSonu) {
		this.izinhaftaSonu = izinhaftaSonu;
	}

	@Column(name = "ARIFE_SAAT")
	public double getArife() {
		return arife;
	}

	public void setArife(double arife) {
		this.arife = arife;
	}

	@Column(name = "NEGATIF_BAKIYE_SAAT")
	public double getNegatifBakiyeDenkSaat() {
		return negatifBakiyeDenkSaat;
	}

	public void setNegatifBakiyeDenkSaat(double negatifBakiyeDenkSaat) {
		this.negatifBakiyeDenkSaat = negatifBakiyeDenkSaat;
	}

	@Column(name = COLUMN_NAME_GENEL_VARDIYA)
	public Boolean getGenelVardiya() {
		return genelVardiya;
	}

	public void setGenelVardiya(Boolean genelVardiya) {
		this.genelVardiya = genelVardiya;
	}

	@Column(name = COLUMN_NAME_HAFTA_TATIL_MESAI_ODE)
	public Boolean getHaftaTatilMesaiOde() {
		return haftaTatilMesaiOde;
	}

	public void setHaftaTatilMesaiOde(Boolean haftaTatilMesaiOde) {
		this.haftaTatilMesaiOde = haftaTatilMesaiOde;
	}

	@Column(name = COLUMN_NAME_GECE_HAFTA_TATIL_MESAI_PARCALA)
	public Boolean getGeceHaftaTatilMesaiParcala() {
		return geceHaftaTatilMesaiParcala;
	}

	public void setGeceHaftaTatilMesaiParcala(Boolean geceHaftaTatilMesaiParcala) {
		this.geceHaftaTatilMesaiParcala = geceHaftaTatilMesaiParcala;
	}

	@Column(name = COLUMN_NAME_GECE_CALISMA_ODEME_VAR)
	public Boolean getGeceCalismaOdemeVar() {
		return geceCalismaOdemeVar;
	}

	public void setGeceCalismaOdemeVar(Boolean geceCalismaOdemeVar) {
		this.geceCalismaOdemeVar = geceCalismaOdemeVar;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Column(name = "TOPLAM_GUN_GUNCELLE")
	public Boolean getToplamGunGuncelle() {
		return toplamGunGuncelle;
	}

	public void setToplamGunGuncelle(Boolean toplamGunGuncelle) {
		this.toplamGunGuncelle = toplamGunGuncelle;
	}

	@Column(name = COLUMN_NAME_HAREKET_KAYDI_VARDIYA_BUL)
	public Boolean getHareketKaydiVardiyaBul() {
		return hareketKaydiVardiyaBul;
	}

	public void setHareketKaydiVardiyaBul(Boolean hareketKaydiVardiyaBul) {
		this.hareketKaydiVardiyaBul = hareketKaydiVardiyaBul;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = COLUMN_NAME_BAGLI_VARDIYA_SABLON)
	@Fetch(FetchMode.JOIN)
	public VardiyaSablonu getBagliVardiyaSablonu() {
		return bagliVardiyaSablonu;
	}

	public void setBagliVardiyaSablonu(VardiyaSablonu bagliVardiyaSablonu) {
		this.bagliVardiyaSablonu = bagliVardiyaSablonu;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "DEPARTMAN_ID")
	@Fetch(FetchMode.JOIN)
	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman departman) {
		this.departman = departman;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = COLUMN_NAME_GUNCELLEYEN, nullable = true)
	@Fetch(FetchMode.JOIN)
	public User getGuncelleyenUser() {
		return guncelleyenUser;
	}

	public void setGuncelleyenUser(User guncelleyenUser) {
		this.guncelleyenUser = guncelleyenUser;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = COLUMN_NAME_OLUSTURAN, nullable = true)
	@Fetch(FetchMode.JOIN)
	public User getOlusturanUser() {
		return olusturanUser;
	}

	public void setOlusturanUser(User olusturanUser) {
		this.olusturanUser = olusturanUser;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_OLUSTURMA_TARIHI)
	public Date getOlusturmaTarihi() {
		return olusturmaTarihi;
	}

	public void setOlusturmaTarihi(Date olusturmaTarihi) {
		this.olusturmaTarihi = olusturmaTarihi;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_GUNCELLEME_TARIHI)
	public Date getGuncellemeTarihi() {
		return guncellemeTarihi;
	}

	public void setGuncellemeTarihi(Date guncellemeTarihi) {
		this.guncellemeTarihi = guncellemeTarihi;
	}

	@Transient
	public User getSonIslemYapan() {
		return guncelleyenUser != null ? guncelleyenUser : olusturanUser;
	}

	@Transient
	public Date getSonIslemTarihi() {
		return guncellemeTarihi != null ? guncellemeTarihi : olusturmaTarihi;
	}

	@Transient
	public String getSonIslemTarihiStr() {
		Date date = getSonIslemTarihi();
		String str = date != null ? PdksUtil.convertToDateString(date, PdksUtil.getDateFormat() + " H:mm:ss") : "";
		return str;
	}

	@Transient
	public boolean isAciklamaVar() {
		return PdksUtil.hasStringValue(aciklama);
	}

	@Transient
	public boolean isHareketKaydiVardiyaBulsunmu() {
		return hareketKaydiVardiyaBul != null && hareketKaydiVardiyaBul.booleanValue();
	}

	@Transient
	public double getIzinSaat(VardiyaGun pdksVardiyaGun) {
		double izinSure = izin;
		if (izinhaftaSonu > 0.0d) {
			Calendar cal = Calendar.getInstance();
			Date vardiyaDate = pdksVardiyaGun.getVardiyaDate();
			cal.setTime(vardiyaDate);
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			if (dayOfWeek == Calendar.SATURDAY)
				izinSure = izinhaftaSonu;
		}

		return izinSure;
	}

}
