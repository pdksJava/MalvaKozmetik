package org.pdks.entity;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
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
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.security.entity.User;
import org.pdks.session.PdksUtil;

@Entity(name = CalismaModeli.TABLE_NAME)
public class CalismaModeli extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -445686730459083532L;

	static Logger logger = Logger.getLogger(CalismaModeli.class);
	public static final String TABLE_NAME = "CALISMA_MODELI";
	public static final String COLUMN_NAME_DURUM = "DURUM";
	public static final String COLUMN_NAME_GENEL_VARDIYA = "GENEL_VARDIYA";
	public static final String COLUMN_NAME_HAFTA_TATIL_MESAI_ODE = "HAFTA_TATIL_MESAI_ODE";
	public static final String COLUMN_NAME_GECE_HAFTA_TATIL_MESAI_PARCALA = "GECE_HAFTA_TATIL_MESAI_PARCALA";
	public static final String COLUMN_NAME_OTOMATIK_FAZLA_CALISMA_ONAYLANSIN = "OTOMATIK_FAZLA_CALISMA_ONAYLANSIN";
	public static final String COLUMN_NAME_GECE_CALISMA_ODEME_VAR = "GECE_CALISMA_ODEME_VAR";
	public static final String COLUMN_NAME_OLUSTURAN = "OLUSTURANUSER_ID";
	public static final String COLUMN_NAME_GUNCELLEYEN = "GUNCELLEYENUSER_ID";
	public static final String COLUMN_NAME_OLUSTURMA_TARIHI = "OLUSTURMATARIHI";
	public static final String COLUMN_NAME_GUNCELLEME_TARIHI = "GUNCELLEMETARIHI";
	public static final String COLUMN_NAME_BAGLI_VARDIYA_SABLON = "BAGLI_VARDIYA_SABLON_ID";
	public static final String COLUMN_NAME_DEPARTMAN = "DEPARTMAN_ID";
	public static final String COLUMN_NAME_HAREKET_KAYDI_VARDIYA_BUL = "HAREKET_KAYDI_VARDIYA_BUL";
	public static final String COLUMN_NAME_MAAS_ODEME_TIPI = "MAAS_ODEME_TIPI";
	public static final String COLUMN_NAME_FAZLA_MESAI_VAR = "FAZLA_MESAI_VAR";
	public static final String COLUMN_NAME_ORTAK_VARDIYA = "ORTAK_VARDIYA";
	public static final String COLUMN_NAME_FAZLA_CALISMA_GORUNTULENSIN = "FAZLA_CALISMA_GORUNTULENSIN";
	public static final String COLUMN_NAME_TOPLAM_GUN_GUNCELLE = "TOPLAM_GUN_GUNCELLE";
	public static final String COLUMN_NAME_ILK_PLAN_ONAYLI = "ILK_PLAN_ONAYLI";
	public static final String COLUMN_NAME_GUN_MAX_CALISMA_SURESI_ODENIR = "GUN_MAX_CALISMA_SURESI_ODENIR";
	public static final String COLUMN_NAME_PERSONEL_TIPI = "PERSONEL_TIPI_ID";
	public static final String COLUMN_NAME_HAFTA_TATIL_PAZAR = "HAFTA_TATIL_PAZAR";
	public static final String COLUMN_NAME_GENEL_MODEL = "GENEL_MODEL";
	public static final String COLUMN_NAME_IDARI_MODEL = "IDARI_MODEL";
	public static final String COLUMN_NAME_HAFTA_ICI_SUT_IZNI_SURE = "HAFTA_ICI_SUT_IZNI_SURE";
	public static final String COLUMN_NAME_CUMARTESI_SUT_IZNI_SURE = "CUMARTESI_SUT_IZNI_SURE";
	public static final String COLUMN_NAME_CUMARTESI_SAAT = "CUMARTESI_SAAT";
	public static final String COLUMN_NAME_PAZAR_SAAT = "PAZAR_SAAT";

	public static final String COLUMN_NAME_IZIN_SAAT = "IZIN_SAAT";
	public static final String COLUMN_NAME_IZIN_CUMARTESI_SAAT = "IZIN_CUMARTESI_SAAT";
	public static final String COLUMN_NAME_IZIN_PAZAR_SAAT = "IZIN_PAZAR_SAAT";
	public static final String COLUMN_NAME_PAZAR_SUT_IZNI_SURE = "PAZAR_SUT_IZNI_SURE";

	public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";

	private String aciklama = "";
	private double haftaIci = 0.0d, arife = 0.0d, negatifBakiyeDenkSaat = 0.0d;
	private Double haftaIciSutIzniSure = 7.5d, cumartesiSaat = 0.0d, izin = 0.0d, cumartesiIzinSaat = 0.0d, cumartesiSutIzniSure = 0.0d, pazarSaat = 0.0d, pazarIzinSaat = 0.0d, pazarSutIzniSure = 0.0d;
	private Boolean fazlaMesaiVar = Boolean.TRUE, toplamGunGuncelle = Boolean.FALSE, durum = Boolean.TRUE, genelVardiya = Boolean.TRUE, hareketKaydiVardiyaBul = Boolean.FALSE;
	private Boolean haftaTatilMesaiOde = Boolean.FALSE, geceHaftaTatilMesaiParcala = Boolean.FALSE, geceCalismaOdemeVar = Boolean.FALSE, otomatikFazlaCalismaOnaylansin = Boolean.FALSE;
	private Boolean ortakVardiya = Boolean.FALSE, fazlaMesaiGoruntulensin = Boolean.TRUE, ilkPlanOnayliDurum = Boolean.FALSE, gunMaxCalismaOdemeDurum = Boolean.TRUE;
	private Boolean haftaTatilPazar = Boolean.FALSE, genelModel = Boolean.TRUE, idariModel = Boolean.FALSE;
	private VardiyaSablonu bagliVardiyaSablonu;
	private Departman departman;
	private Tanim personelTipi;
	private Boolean aylikMaas = Boolean.TRUE;

	private User guncelleyenUser, olusturanUser;
	private Date olusturmaTarihi = new Date(), guncellemeTarihi;
	private Set<CalismaModeliGun> calismaModeliGunler;

	@Column(name = COLUMN_NAME_ACIKLAMA)
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

	@Column(name = COLUMN_NAME_CUMARTESI_SAAT)
	public Double getCumartesiSaat() {
		return cumartesiSaat;
	}

	public void setCumartesiSaat(Double cumartesiSaat) {
		this.cumartesiSaat = cumartesiSaat;
	}

	@Column(name = COLUMN_NAME_PAZAR_SAAT)
	public Double getPazarSaat() {
		return pazarSaat;
	}

	public void setPazarSaat(Double pazarSaat) {
		this.pazarSaat = pazarSaat;
	}

	@OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER, mappedBy = "calismaModeli", targetEntity = CalismaModeliGun.class)
	public Set<CalismaModeliGun> getCalismaModeliGunler() {
		return calismaModeliGunler;
	}

	public void setCalismaModeliGunler(Set<CalismaModeliGun> calismaModeliGunler) {
		this.calismaModeliGunler = calismaModeliGunler;
	}

	@Transient
	// @Column(name = COLUMN_NAME_IZIN_SAAT)
	public Double getIzin() {
		return izin;
	}

	public void setIzin(Double izin) {
		this.izin = izin;
	}

	@Transient
	// @Column(name = COLUMN_NAME_IZIN_CUMARTESI_SAAT)
	public Double getCumartesiIzinSaat() {
		return cumartesiIzinSaat;
	}

	public void setCumartesiIzinSaat(Double cumartesiIzinSaat) {
		this.cumartesiIzinSaat = cumartesiIzinSaat;
	}

	@Transient
	// @Column(name = COLUMN_NAME_IZIN_PAZAR_SAAT)
	public Double getPazarIzinSaat() {
		return pazarIzinSaat;
	}

	public void setPazarIzinSaat(Double pazarIzinSaat) {
		this.pazarIzinSaat = pazarIzinSaat;
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

	@Column(name = COLUMN_NAME_IDARI_MODEL)
	public Boolean getIdariModel() {
		return idariModel;
	}

	public void setIdariModel(Boolean idariModel) {
		this.idariModel = idariModel;
	}

	@Column(name = COLUMN_NAME_HAFTA_TATIL_MESAI_ODE)
	public Boolean getHaftaTatilMesaiOde() {
		return haftaTatilMesaiOde;
	}

	public void setHaftaTatilMesaiOde(Boolean haftaTatilMesaiOde) {
		this.haftaTatilMesaiOde = haftaTatilMesaiOde;
	}

	@Column(name = COLUMN_NAME_OTOMATIK_FAZLA_CALISMA_ONAYLANSIN)
	public Boolean getOtomatikFazlaCalismaOnaylansin() {
		return otomatikFazlaCalismaOnaylansin;
	}

	public void setOtomatikFazlaCalismaOnaylansin(Boolean otomatikFazlaCalismaOnaylansin) {
		this.otomatikFazlaCalismaOnaylansin = otomatikFazlaCalismaOnaylansin;
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

	@Column(name = COLUMN_NAME_TOPLAM_GUN_GUNCELLE)
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

	@Column(name = COLUMN_NAME_MAAS_ODEME_TIPI)
	public Boolean getAylikMaas() {
		return aylikMaas;
	}

	public void setAylikMaas(Boolean aylikMaas) {
		this.aylikMaas = aylikMaas;
	}

	@Column(name = COLUMN_NAME_FAZLA_MESAI_VAR)
	public Boolean getFazlaMesaiVar() {
		return fazlaMesaiVar;
	}

	public void setFazlaMesaiVar(Boolean fazlaMesaiVar) {
		this.fazlaMesaiVar = fazlaMesaiVar;
	}

	@Column(name = COLUMN_NAME_ORTAK_VARDIYA)
	public Boolean getOrtakVardiya() {
		return ortakVardiya;
	}

	public void setOrtakVardiya(Boolean ortakVardiya) {
		this.ortakVardiya = ortakVardiya;
	}

	@Column(name = COLUMN_NAME_FAZLA_CALISMA_GORUNTULENSIN)
	public Boolean getFazlaMesaiGoruntulensin() {
		return fazlaMesaiGoruntulensin;
	}

	public void setFazlaMesaiGoruntulensin(Boolean fazlaMesaiGoruntulensin) {
		this.fazlaMesaiGoruntulensin = fazlaMesaiGoruntulensin;
	}

	@Column(name = COLUMN_NAME_ILK_PLAN_ONAYLI)
	public Boolean getIlkPlanOnayliDurum() {
		return ilkPlanOnayliDurum;
	}

	public void setIlkPlanOnayliDurum(Boolean ilkPlanOnayliDurum) {
		this.ilkPlanOnayliDurum = ilkPlanOnayliDurum;
	}

	@Column(name = COLUMN_NAME_GUN_MAX_CALISMA_SURESI_ODENIR)
	public Boolean getGunMaxCalismaOdemeDurum() {
		return gunMaxCalismaOdemeDurum;
	}

	public void setGunMaxCalismaOdemeDurum(Boolean gunMaxCalismaOdemeDurum) {
		this.gunMaxCalismaOdemeDurum = gunMaxCalismaOdemeDurum;
	}

	@Transient
	// @Column(name = COLUMN_NAME_HAFTA_TATIL_PAZAR)
	public Boolean getHaftaTatilPazar() {
		return haftaTatilPazar;
	}

	public void setHaftaTatilPazar(Boolean haftaTatilPazar) {
		this.haftaTatilPazar = haftaTatilPazar;
	}

	@Column(name = COLUMN_NAME_GENEL_MODEL)
	public Boolean getGenelModel() {
		return genelModel;
	}

	public void setGenelModel(Boolean genelModel) {
		this.genelModel = genelModel;
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

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = COLUMN_NAME_PERSONEL_TIPI)
	@Fetch(FetchMode.JOIN)
	public Tanim getPersonelTipi() {
		return personelTipi;
	}

	public void setPersonelTipi(Tanim personelTipi) {
		this.personelTipi = personelTipi;
	}

	public void setDepartman(Departman departman) {
		this.departman = departman;
	}

	@Column(name = COLUMN_NAME_HAFTA_ICI_SUT_IZNI_SURE)
	public Double getHaftaIciSutIzniSure() {
		return haftaIciSutIzniSure;
	}

	public void setHaftaIciSutIzniSure(Double haftaIciSutIzniSure) {
		this.haftaIciSutIzniSure = haftaIciSutIzniSure;
	}

	@Column(name = COLUMN_NAME_CUMARTESI_SUT_IZNI_SURE)
	public Double getCumartesiSutIzniSure() {
		return cumartesiSutIzniSure;
	}

	public void setCumartesiSutIzniSure(Double cumartesiSutIzniSure) {
		this.cumartesiSutIzniSure = cumartesiSutIzniSure;
	}

	@Column(name = COLUMN_NAME_PAZAR_SUT_IZNI_SURE)
	public Double getPazarSutIzniSure() {
		return pazarSutIzniSure;
	}

	public void setPazarSutIzniSure(Double pazarSutIzniSure) {
		this.pazarSutIzniSure = pazarSutIzniSure;
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
	public boolean isHaftaTatilVar() {
		return (cumartesiSaat != null && cumartesiSaat > 0.0d) || (pazarSaat != null && pazarSaat > 0.0d);
	}

	@Transient
	public boolean isHareketKaydiVardiyaBulsunmu() {
		return hareketKaydiVardiyaBul != null && hareketKaydiVardiyaBul.booleanValue();
	}

	@Transient
	public boolean isSaatlikOdeme() {
		return aylikMaas == null || aylikMaas.equals(Boolean.FALSE);
	}

	@Transient
	public boolean isAylikOdeme() {
		return aylikMaas != null && aylikMaas.equals(Boolean.TRUE);
	}

	@Transient
	public String getMaasOdemeTipiAciklama() {
		String str = "";
		if (aylikMaas != null) {
			if (isAylikOdeme())
				str = "Aylık";
			else if (isSaatlikOdeme())
				str = "Saatlik";
		}
		return str;
	}

	@Transient
	public boolean isFazlaMesaiVarMi() {
		return fazlaMesaiVar != null && fazlaMesaiVar.booleanValue();
	}

	@Transient
	public boolean isFazlaMesaiGoruntulensinMi() {
		return fazlaMesaiGoruntulensin != null && fazlaMesaiGoruntulensin.booleanValue();
	}

	@Transient
	public double getIzinSaat(VardiyaGun pdksVardiyaGun) {
		int dayOfWeek = PdksUtil.getDateField(pdksVardiyaGun.getVardiyaDate(), Calendar.DAY_OF_WEEK);
		double izinSure = this.getIzinSaat(dayOfWeek);

		// if (dayOfWeek == Calendar.SUNDAY) {
		// if (isHaftaTatilPazardir())
		// izinSure = 0.0d;
		// }

		return izinSure;
	}

	@Transient
	public boolean isOtomatikFazlaCalismaOnaylansinmi() {
		return otomatikFazlaCalismaOnaylansin != null && otomatikFazlaCalismaOnaylansin.booleanValue();
	}

	@Transient
	public boolean isUpdateCGS() {
		boolean updateCGS = toplamGunGuncelle != null && toplamGunGuncelle;
		return updateCGS;
	}

	@Transient
	public boolean isOrtakVardiyadir() {
		return ortakVardiya != null && ortakVardiya;
	}

	@Transient
	public boolean isIlkPlanOnaylidir() {
		return ilkPlanOnayliDurum != null && ilkPlanOnayliDurum;
	}

	@Transient
	public boolean isGunMaxCalismaOdenir() {
		return gunMaxCalismaOdemeDurum != null && gunMaxCalismaOdemeDurum;
	}

	@Transient
	public boolean isHaftaTatilPazardir() {
		return haftaTatilPazar != null && haftaTatilPazar;
	}

	@Transient
	public boolean isGenelModelGorunsun() {
		return genelModel != null && genelModel;
	}

	@Transient
	public double getIzinSaat(int dayOfWeek) {
		Double izinSure = null;
		switch (dayOfWeek) {
		case Calendar.SATURDAY:
			izinSure = this.getCumartesiIzinSaat();
			break;
		case Calendar.SUNDAY:
			izinSure = this.getPazarIzinSaat();
			break;

		default:
			izinSure = this.getIzin();
			break;
		}
		if (izinSure == null || izinSure.doubleValue() == 0.0d)
			izinSure = getSaat(dayOfWeek);
		if (izinSure > 0.0d)
			logger.debug(dayOfWeek + " : " + izinSure);
		return izinSure;
	}

	@Transient
	public double getSutIzinSaat(int dayOfWeek) {
		Double sutIzinSure = 0.0d;
		switch (dayOfWeek) {
		case Calendar.SATURDAY:
			sutIzinSure = this.getCumartesiSutIzniSure();
			break;
		case Calendar.SUNDAY:
			sutIzinSure = this.getPazarSutIzniSure();
			break;

		default:
			sutIzinSure = this.getHaftaIciSutIzniSure();
			break;
		}
		if (sutIzinSure == null) {
			if (this.getDepartman() != null) {
				Departman dm = this.getDepartman();
				switch (dayOfWeek) {
				case Calendar.SATURDAY:
					sutIzinSure = dm.getCumartesiSutIzniSure();
					break;
				case Calendar.SUNDAY:
					sutIzinSure = dm.getPazarSutIzniSure();
					break;

				default:
					sutIzinSure = this.getGunSure(CalismaModeliGun.GUN_IZIN, dayOfWeek);
					if (sutIzinSure == null)
						sutIzinSure = dm.getHaftaIciSutIzniSure();
					break;
				}
			}
			if (sutIzinSure == null)
				sutIzinSure = 0.0d;
		}

		if (sutIzinSure > 0.0d)
			logger.debug(dayOfWeek + " : " + sutIzinSure);
		return sutIzinSure;
	}

	/**
	 * @param gunTipi
	 * @param dayOfWeek
	 * @return
	 */
	@Transient
	public Double getGunSure(int gunTipi, int dayOfWeek) {
		Double sure = null;
		if (calismaModeliGunler != null && !calismaModeliGunler.isEmpty()) {
			for (Iterator iterator = calismaModeliGunler.iterator(); iterator.hasNext();) {
				CalismaModeliGun gun = (CalismaModeliGun) iterator.next();
				if (gun.getGunTipi() == gunTipi && gun.getHaftaGun() == dayOfWeek) {
					sure = gun.getSure();
					break;
				}
			}
		}
		return sure;
	}

	@Transient
	public double getSaat(int dayOfWeek) {
		Double gunSure = null;
		switch (dayOfWeek) {
		case Calendar.SATURDAY:
			gunSure = this.getCumartesiSaat();
			break;
		case Calendar.SUNDAY:
			gunSure = this.getPazarSaat();
			break;

		default:
			gunSure = this.getGunSure(CalismaModeliGun.GUN_SAAT, dayOfWeek);
			if (gunSure == null)
				gunSure = this.getHaftaIci();
			break;
		}
		if (gunSure == null)
			gunSure = 0.0d;
		if (gunSure > 0.0d)
			logger.debug(dayOfWeek + " : " + gunSure);
		return gunSure;
	}

	@Transient
	public Boolean isIdariModelMi() {
		return idariModel != null && idariModel;
	}

	public void entityRefresh() {
		// TODO entityRefresh

	}

}
