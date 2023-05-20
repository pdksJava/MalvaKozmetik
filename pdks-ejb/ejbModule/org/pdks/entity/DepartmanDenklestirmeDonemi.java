package org.pdks.entity;

import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;

import org.apache.log4j.Logger;
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

	private Boolean denklestirmeAyDurum = Boolean.FALSE;

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Date getBaslangicTarih() {
		return baslangicTarih;
	}

	public void setBaslangicTarih(Date baslangicTarih) {
		this.baslangicTarih = baslangicTarih;
	}

	public Date getBitisTarih() {
		return bitisTarih;
	}

	public void setBitisTarih(Date bitisTarih) {
		this.bitisTarih = bitisTarih;
	}

	public Date getFazlaMesaiBaslangicTarih() {
		return fazlaMesaiBaslangicTarih;
	}

	public void setFazlaMesaiBaslangicTarih(Date fazlaMesaiBaslangicTarih) {
		this.fazlaMesaiBaslangicTarih = fazlaMesaiBaslangicTarih;
	}

	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman departman) {
		this.departman = departman;
	}

	public String getIslemTipi() {
		return islemTipi;
	}

	public void setIslemTipi(String islemTipi) {
		this.islemTipi = islemTipi;
	}

	public Integer getAy() {
		return ay;
	}

	public void setAy(Integer ay) {
		this.ay = ay;
	}

	public Integer getYil() {
		return yil;
	}

	public void setYil(Integer yil) {
		this.yil = yil;
	}

	public boolean isDonemKapali() {
		return islemTipi != null && islemTipi.equals(ISLEM_TIPI_DONEM_KAPANDI);
	}

	public boolean isAcik() {
		return islemTipi == null || islemTipi.equals(ISLEM_TIPI_ACIK) || islemTipi.equals(ISLEM_TIPI_HESAPLANDI);
	}

	public String getIslemTipiAciklama() {
		String tip = islemTipi;
		if (tip == null)
			tip = ISLEM_TIPI_ACIK;
		String aciklama = PdksUtil.getMessageBundleMessage("puantaj.etiket.denklestirmeDonem.islemTipi" + tip);
		return aciklama;
	}

	public long getDepartmanId() {
		return departman != null ? departman.getId() : 0;
	}

	public String getAciklama() {
		String str = PdksUtil.convertToDateString(baslangicTarih, PdksUtil.getDateFormat()) + "-" + PdksUtil.convertToDateString(bitisTarih, PdksUtil.getDateFormat());
		return str;
	}

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

	public boolean isDenklestirmeYapilmadi() {
		return islemTipi.equals(ISLEM_TIPI_ACIK);
	}

	public boolean isDenklestirmeYapildi() {
		return islemTipi.equals(ISLEM_TIPI_HESAPLANDI);
	}

	public boolean isDonemKapandi() {
		return islemTipi.equals(ISLEM_TIPI_DONEM_KAPANDI);
	}

	public TreeMap<String, Tatil> getTatilGunleriMap() {
		return tatilGunleriMap;
	}

	public void setTatilGunleriMap(TreeMap<String, Tatil> tatilGunleriMap) {
		this.tatilGunleriMap = tatilGunleriMap;
	}

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

	public Boolean getDenklestirmeAyDurum() {
		return denklestirmeAyDurum;
	}

	public void setDenklestirmeAyDurum(Boolean denklestirmeAyDurum) {
		this.denklestirmeAyDurum = denklestirmeAyDurum;
	}

}
