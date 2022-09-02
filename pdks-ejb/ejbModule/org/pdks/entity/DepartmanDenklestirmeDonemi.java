package org.pdks.entity;

import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.session.PdksUtil;

public class DepartmanDenklestirmeDonemi extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3806601326778807754L;
	static Logger logger = Logger.getLogger(DepartmanDenklestirmeDonemi.class);

	public static final String ISLEM_TIPI_ACIK = "1";

	public static final String ISLEM_TIPI_HESAPLANDI = "2";

	public static final String ISLEM_TIPI_DONEM_KAPANDI = "3";

	private Departman departman;

	private Date baslangicTarih, bitisTarih, fazlaMesaiBaslangicTarih;

	private TreeMap<Long, PersonelDenklestirme> personelDenklestirmeDonemMap;

	private Integer ay, yil;

	private String islemTipi = ISLEM_TIPI_ACIK;

	private TreeMap<String, Tatil> tatilGunleriMap;

	private Integer version = 0;

	private DenklestirmeAy denklestirmeAy;

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "BASLANGIC_TARIH", nullable = false)
	public Date getBaslangicTarih() {
		return baslangicTarih;
	}

	public void setBaslangicTarih(Date baslangicTarih) {
		this.baslangicTarih = baslangicTarih;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "BITIS_TARIH", nullable = false)
	public Date getBitisTarih() {
		return bitisTarih;
	}

	public void setBitisTarih(Date bitisTarih) {
		this.bitisTarih = bitisTarih;
	}

	@Column(name = "FAZLA_MESAI_BAS_TARIH", nullable = false)
	public Date getFazlaMesaiBaslangicTarih() {
		return fazlaMesaiBaslangicTarih;
	}

	public void setFazlaMesaiBaslangicTarih(Date fazlaMesaiBaslangicTarih) {
		this.fazlaMesaiBaslangicTarih = fazlaMesaiBaslangicTarih;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "DEPARTMAN_ID", nullable = false)
	@Fetch(FetchMode.JOIN)
	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman departman) {
		this.departman = departman;
	}

	@Column(name = "ISLEM_TIPI", nullable = false, length = 1)
	public String getIslemTipi() {
		return islemTipi;
	}

	public void setIslemTipi(String islemTipi) {
		this.islemTipi = islemTipi;
	}

	@Transient
	public boolean isDonemKapali() {
		return islemTipi != null && islemTipi.equals(ISLEM_TIPI_DONEM_KAPANDI);
	}

	@Transient
	public boolean isAcik() {
		return islemTipi == null || islemTipi.equals(ISLEM_TIPI_ACIK) || islemTipi.equals(ISLEM_TIPI_HESAPLANDI);
	}

	@Transient
	public String getIslemTipiAciklama() {
		String tip = islemTipi;
		if (tip == null)
			tip = ISLEM_TIPI_ACIK;
		String aciklama = PdksUtil.getMessageBundleMessage("puantaj.etiket.denklestirmeDonem.islemTipi" + tip);
		return aciklama;
	}

	@Transient
	public long getDepartmanId() {
		return departman != null ? departman.getId() : 0;
	}

	@Transient
	public String getAciklama() {
		String str = PdksUtil.convertToDateString(baslangicTarih, PdksUtil.getDateFormat()) + "-" + PdksUtil.convertToDateString(bitisTarih, PdksUtil.getDateFormat());
		return str;
	}

	@Column(name = "AY")
	public Integer getAy() {
		return ay;
	}

	public void setAy(Integer ay) {
		this.ay = ay;
	}

	@Column(name = "YIL")
	public Integer getYil() {
		return yil;
	}

	public void setYil(Integer yil) {
		this.yil = yil;
	}

	@Transient
	public Date getBordroTarihi() {
		Date bordroTarihi = null;
		Calendar cal = Calendar.getInstance();
		cal.set(yil, ay - 1, 1);
		try {
			bordroTarihi = cal.getTime();
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			bordroTarihi = null;
		}
		return bordroTarihi;
	}

	@Transient
	public boolean isDenklestirmeYapilmadi() {
		return islemTipi.equals(ISLEM_TIPI_ACIK);
	}

	@Transient
	public boolean isDenklestirmeYapildi() {
		return islemTipi.equals(ISLEM_TIPI_HESAPLANDI);
	}

	@Transient
	public boolean isDonemKapandi() {
		return islemTipi.equals(ISLEM_TIPI_DONEM_KAPANDI);
	}

	@Transient
	public TreeMap<String, Tatil> getTatilGunleriMap() {
		return tatilGunleriMap;
	}

	public void setTatilGunleriMap(TreeMap<String, Tatil> tatilGunleriMap) {
		this.tatilGunleriMap = tatilGunleriMap;
	}

	@Transient
	public DenklestirmeAy getDenklestirmeAy() {
		return denklestirmeAy;
	}

	public void setDenklestirmeAy(DenklestirmeAy denklestirmeAy) {
		this.denklestirmeAy = denklestirmeAy;
	}

	public TreeMap<Long, PersonelDenklestirme> getPersonelDenklestirmeDonemMap() {
		return personelDenklestirmeDonemMap;
	}

	public void setPersonelDenklestirmeDonemMap(TreeMap<Long, PersonelDenklestirme> personelDenklestirmeDonemMap) {
		this.personelDenklestirmeDonemMap = personelDenklestirmeDonemMap;
	}

}
